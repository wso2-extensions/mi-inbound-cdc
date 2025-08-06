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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Util class for storing common utils.
 */
public class CDCUtils {

    public static void handleChangeEvents(List<ChangeEvent<String, String>> records,
                                   DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer,
                                   String inboundEndpointName, CDCInjectHandler injectHandler,
                                   AtomicBoolean isShutdownRequested,
                                   CDCConsumerParent parent) throws InterruptedException {
        if (!isShutdownRequested.get()) {
            parent.setAllRecordsProcessed(false);
            boolean success = injectHandler.handleEvents(records, committer, inboundEndpointName, isShutdownRequested);
            parent.setAllRecordsProcessed(true);
            if (isShutdownRequested.get()) {
                parent.getShutdownLatch().countDown();
                return;
            }
            if (!success) {
                parent.deactivate();
                injectHandler.invokeDeactivateSequence();
            }
        }
    }
}
