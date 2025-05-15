package org.wso2.carbon.inbound.cdc;


import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class CustomChangeConsumer implements DebeziumEngine.ChangeConsumer<ChangeEvent<String, String>> {
    private static final Log logger = LogFactory.getLog(CustomChangeConsumer.class);
    private CDCInjectHandler injectHandler;
    private String inboundEndpointName;

    public CustomChangeConsumer(CDCInjectHandler injectHandler, String inboundEndpointName) {
        this.injectHandler = injectHandler;
        this.inboundEndpointName = inboundEndpointName;
    }


    @Override
    public void handleBatch(List<ChangeEvent<String, String>> events, DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer) throws InterruptedException {
        injectHandler.handleEvents(events, committer, inboundEndpointName);
    }
}
