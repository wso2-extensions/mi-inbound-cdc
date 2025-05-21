/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.cdc;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.util.resolver.SecureVaultResolver;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericPollingConsumer;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.CDC_PRESERVE_EVENT;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_ALLOWED_OPERATIONS;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_DATABASE_PASSWORD;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_KEY_CONVERTER;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_OFFSET_STORAGE;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_SCHEMA_HISTORY_INTERNAL;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_TOPIC_PREFIX;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_VALUE_CONVERTER;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_SKIPPED_OPERATIONS;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.FILE_SCHEMA_HISTORY_STORAGE_CLASS;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.FILE_OFFSET_STORAGE_CLASS;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.TRUE;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.FALSE;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_NAME;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.CONNECTOR_NAME;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.DEBEZIUM_INCLUDE_SCHEMA_CHANGES;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.CDC_MAXIMUM_RETRY_COUNT;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.CDC_DEFAULT_RETRY_COUNT;
import static org.wso2.carbon.inbound.cdc.InboundCDCConstants.CDC_DEACTIVATE_SEQUENCE;

/**
 * This class implement the processing logic related to inbound CDC protocol.
 * Common functionalities are include in synapse
 * util that is found in synapse commons
 */
public class CDCPollingConsumer extends GenericPollingConsumer {

    private static final Log logger = LogFactory.getLog(CDCPollingConsumer.class);
    private Properties cdcProperties;
    private String inboundEndpointName;
    private SynapseEnvironment synapseEnvironment;
    private long scanInterval;
    private Long lastRanTime;
    private CDCInjectHandler injectHandler;
    private ExecutorService executorService = null;
    private DebeziumEngine<ChangeEvent<String, String>> engine = null;

    private enum operations {create, update, delete, truncate};
    private enum opCodes {c, u, d, t};

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final AtomicBoolean isShutdownRequested = new AtomicBoolean(false);
    private final AtomicBoolean allRecordsProcessed = new AtomicBoolean(true);

    public CDCPollingConsumer(Properties cdcProperties, String inboundEndpointName, SynapseEnvironment synapseEnvironment,
                              long scanInterval, String inSequence, String onErrorSeq, boolean coordination, boolean sequential) {
        super(cdcProperties, inboundEndpointName, synapseEnvironment, scanInterval, inSequence, onErrorSeq, coordination, sequential);
        this.cdcProperties = cdcProperties;
        this.inboundEndpointName = inboundEndpointName;
        this.synapseEnvironment = synapseEnvironment;
        this.scanInterval = scanInterval;
        this.lastRanTime = null;

        boolean preserveEvent = Boolean.parseBoolean(
                cdcProperties.getProperty(CDC_PRESERVE_EVENT));
        int maxRetryCount = getMaxRetryCount(cdcProperties);
        String onDeactivateSeq = cdcProperties.getProperty(CDC_DEACTIVATE_SEQUENCE);
        registerHandler(new CDCInjectHandler(injectingSeq, onErrorSeq, onDeactivateSeq, sequential, synapseEnvironment,
                preserveEvent, maxRetryCount));
        setProperties();
    }

    private static int getMaxRetryCount(Properties cdcProperties) {
        String maxRetryCount = CDC_DEFAULT_RETRY_COUNT;
        if (cdcProperties.getProperty(CDC_MAXIMUM_RETRY_COUNT) != null) {
            maxRetryCount = cdcProperties.getProperty(CDC_MAXIMUM_RETRY_COUNT);
        }
        try {
            return Integer.parseInt(maxRetryCount);
        } catch (NumberFormatException e) {
            logger.warn("Invalid value for maximum retry count. Using default value of " + CDC_DEFAULT_RETRY_COUNT);
            return Integer.parseInt(CDC_DEFAULT_RETRY_COUNT);
        }
    }

    /**
     * Register a handler to process the file stream after reading from the
     * source
     *
     * @param injectHandler
     */
    public void registerHandler(CDCInjectHandler injectHandler) {
        this.injectHandler = injectHandler;
    }

    /**
     * This will be called by the task scheduler. If a cycle execution takes
     * more than the schedule interval, tasks will call this method ignoring the
     * interval. Timestamp based check is done to avoid that.
     */
    public void execute() {
        if (logger.isDebugEnabled()) {
            logger.debug("Start : CDC Inbound EP : " + inboundEndpointName);
        }
        // Check if the cycles are running in correct interval and start
        // scan
        long currentTime = (new Date()).getTime();
        if (lastRanTime == null || ((lastRanTime + (scanInterval)) <= currentTime)) {
            lastRanTime = currentTime;
            poll();
        } else if (logger.isDebugEnabled()) {
            logger.debug(
                    "Skip cycle since concurrent rate is higher than the scan interval : CDC Inbound EP : " + inboundEndpointName);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("End : CDC Inbound EP : " + inboundEndpointName);
        }
    }

    /**
     * Do the CDC processing operation for the given set of properties. Then inject
     * according to the registered handler
     */
    public ChangeEvent<String, String> poll() {
        logger.debug("Start : listening to DB events : ");
        listenDataChanges();
        logger.debug("End : Listening to DB events : ");
        return null;
    }

    private void listenDataChanges () {
        executorService = Executors.newSingleThreadExecutor();
        try {
            if (engine == null || executorService.isShutdown()) {
                engine = DebeziumEngine.create(Json.class)
                        .using(this.cdcProperties)
                        .notifying((records, committer) -> {
                            if (!isShutdownRequested.get()) {
                                allRecordsProcessed.set(false);
                                boolean success = injectHandler.handleEvents(records, committer, inboundEndpointName,
                                        isShutdownRequested);
                                allRecordsProcessed.set(true);
                                if (isShutdownRequested.get()) {
                                    shutdownLatch.countDown();
                                    return;
                                }
                                if (!success) {
                                    deactivate();
                                    injectHandler.invokeDeactivateSequence();
                                }
                            }
                        })
                        .build();

                executorService.execute(engine);
            }
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }

    }

    protected Properties getInboundProperties() {
        return cdcProperties;
    }

    public void deactivate(){
        logger.warn("Deactivating the CDC Inbound EP : " + inboundEndpointName);
        destroy();
    }

    public void destroy() {
        isShutdownRequested.set(true);
        if (!allRecordsProcessed.get()) {
            try {
                shutdownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
        try {
            if (engine != null) {
                engine.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while closing the Debezium Engine", e);
        }
    }

    private void setProperties () {
        logger.info("Initializing the CDC properties");
        try {
            // Set to false since the CDC inbound endpoint currently doesn't support capturing schema changes.
            this.cdcProperties.setProperty(DEBEZIUM_INCLUDE_SCHEMA_CHANGES, FALSE);

            if (this.cdcProperties.getProperty(CONNECTOR_NAME) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_NAME, this.inboundEndpointName);
            } else {
                this.cdcProperties.setProperty(DEBEZIUM_NAME, this.cdcProperties.getProperty(CONNECTOR_NAME));
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_OFFSET_STORAGE) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_OFFSET_STORAGE, FILE_OFFSET_STORAGE_CLASS);
            }
            if (this.cdcProperties.getProperty(DEBEZIUM_OFFSET_STORAGE).equals(FILE_OFFSET_STORAGE_CLASS)) {
                String filePath;
                if (this.cdcProperties.getProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME) == null) {
                    filePath = "cdc/offsetStorage/" + this.name + "_.dat";
                } else {
                    filePath = this.cdcProperties.getProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME);
                }
                createFile(filePath);
                this.cdcProperties.setProperty(DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME, filePath);
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS, "1000");
            }

            String passwordString = this.cdcProperties.getProperty(DEBEZIUM_DATABASE_PASSWORD);
            SynapseEnvironment synapseEnvironment = this.synapseEnvironment;

            this.cdcProperties.setProperty(DEBEZIUM_DATABASE_PASSWORD, SecureVaultResolver.resolve(synapseEnvironment, passwordString));

            if (this.cdcProperties.getProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL, TRUE);
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_ALLOWED_OPERATIONS) != null) {
                this.cdcProperties.setProperty(DEBEZIUM_SKIPPED_OPERATIONS,
                        getSkippedOperationsString(this.cdcProperties.getProperty(DEBEZIUM_ALLOWED_OPERATIONS)));
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_TOPIC_PREFIX) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_TOPIC_PREFIX, this.name +"_topic");
            }

            if (this.cdcProperties.getProperty(CDC_PRESERVE_EVENT) == null) {
                boolean preserveEvent = Boolean.parseBoolean(this.cdcProperties.getProperty(CDC_PRESERVE_EVENT));
                this.cdcProperties.setProperty(CDC_PRESERVE_EVENT, String.valueOf(preserveEvent));
            }

            // set the output format as json in a way a user cannot override
            this.cdcProperties.setProperty(DEBEZIUM_VALUE_CONVERTER, "org.apache.kafka.connect.json.JsonConverter");
            this.cdcProperties.setProperty(DEBEZIUM_KEY_CONVERTER, "org.apache.kafka.connect.json.JsonConverter");
            this.cdcProperties.setProperty(DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE, TRUE);
            this.cdcProperties.setProperty(DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE, TRUE);

            if (this.cdcProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL) == null) {
                this.cdcProperties.setProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL, FILE_SCHEMA_HISTORY_STORAGE_CLASS);
            }

            if (this.cdcProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL).equals(FILE_SCHEMA_HISTORY_STORAGE_CLASS)) {
                String filePath;
                if (this.cdcProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME) == null) {
                    filePath = "cdc/schemaHistory/" + this.name + "_.dat";
                } else {
                    filePath = this.cdcProperties.getProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME);
                }
                createFile(filePath);
                this.cdcProperties.setProperty(DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME, filePath);
            }

        } catch (IOException e) {
            String msg = "Error while setting the CDC Properties";
            logger.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    private void createFile (String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if(!file.exists()) {
            file.createNewFile();
        }
    }

    private String getOpCode(String op) {
        if (op != null) {
            switch (CDCPollingConsumer.operations.valueOf(op)) {
                case create:
                    return CDCPollingConsumer.opCodes.c.toString();
                case update:
                    return CDCPollingConsumer.opCodes.u.toString();
                case delete:
                    return CDCPollingConsumer.opCodes.d.toString();
                case truncate:
                    return CDCPollingConsumer.opCodes.t.toString();
            }
        }
        return "";
    }

    /**
     * Get the comma separated list containing allowed operations and returns the string of skipped operation codes
     * @param allowedOperationsString string
     * @return the coma separated string of skipped operation codes
     */
    private String getSkippedOperationsString(String allowedOperationsString) {
        List<String> allOperations = Stream.of(CDCPollingConsumer.opCodes.values()).map(Enum :: toString).collect(Collectors.toList());
        Set<String> allowedOperationsSet = Stream.of(allowedOperationsString.split(",")).
                map(String :: trim).map(String :: toLowerCase).map(op -> getOpCode(op)).
                collect(Collectors.toSet());
        allOperations.removeAll(allowedOperationsSet);
        return String.join(",", allOperations);
    }
}
