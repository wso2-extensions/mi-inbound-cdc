/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CDCConsumerHandler implements DebeziumEngine.ChangeConsumer<ChangeEvent<String, String>> {

    private static final Log logger = LogFactory.getLog(CDCConsumerHandler.class);
    private final CDCInjectHandler injectHandler;
    private final String inboundEndpointName;
    private final long scanInterval;
    private final AtomicBoolean isShutdownRequested;
    private final CDCConsumerParent parent;

    public CDCConsumerHandler(CDCInjectHandler injectHandler, String inboundEndpointName, long scanInterval,
                              AtomicBoolean isShutdownRequested, CDCConsumerParent parent) {
        this.injectHandler = injectHandler;
        this.inboundEndpointName = inboundEndpointName;
        this.scanInterval = scanInterval;
        this.isShutdownRequested = isShutdownRequested;
        this.parent = parent;

    }

    @Override
    public void handleBatch(List<ChangeEvent<String, String>> list, DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> recordCommitter) throws InterruptedException {
        logger.debug("Start : listening to DB events : ");
        CDCUtils.handleChangeEvents(list, recordCommitter, inboundEndpointName, injectHandler, isShutdownRequested, parent);
        logger.debug("End : Listening to DB events : ");
        Thread.sleep(scanInterval);
    }
}
