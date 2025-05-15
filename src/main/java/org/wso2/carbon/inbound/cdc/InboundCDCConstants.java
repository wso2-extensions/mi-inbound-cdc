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

class InboundCDCConstants {

    /** Inbound Endpoint Parameters **/

    public static final String CONNECTOR_NAME = "connector.name";
    public static final String DEBEZIUM_NAME = "name";
    public static final String DEBEZIUM_INCLUDE_SCHEMA_CHANGES = "include.schema.changes";
    public static final String DEBEZIUM_OFFSET_STORAGE = "offset.storage";
    public static final String DEBEZIUM_OFFSET_STORAGE_FILE_FILENAME = "offset.storage.file.filename";
    public static final String DEBEZIUM_OFFSET_FLUSH_INTERVAL_MS = "offset.flush.interval.ms";
    public static final String FILE_OFFSET_STORAGE_CLASS = "org.apache.kafka.connect.storage.FileOffsetBackingStore";
    public static final String FILE_SCHEMA_HISTORY_STORAGE_CLASS = "io.debezium.storage.file.history.FileSchemaHistory";
    public static final String DEBEZIUM_DATABASE_PASSWORD = "database.password";
    public static final String DEBEZIUM_DATABASE_ALLOW_PUBLIC_KEY_RETRIEVAL = "database.allowPublicKeyRetrieval";
    public static final String DEBEZIUM_TOPIC_PREFIX = "topic.prefix";

    public static final String DEBEZIUM_VALUE_CONVERTER = "value.converter";
    public static final String DEBEZIUM_KEY_CONVERTER = "key.converter";
    public static final String DEBEZIUM_VALUE_CONVERTER_SCHEMAS_ENABLE = "value.converter.schemas.enable";
    public static final String DEBEZIUM_KEY_CONVERTER_SCHEMAS_ENABLE = "key.converter.schemas.enable";

    public static final String DEBEZIUM_SCHEMA_HISTORY_INTERNAL = "schema.history.internal";
    public static final String DEBEZIUM_SCHEMA_HISTORY_INTERNAL_FILE_FILENAME = "schema.history.internal.file.filename";
    public static final String DEBEZIUM_SKIPPED_OPERATIONS = "skipped.operations";
    public static final String DEBEZIUM_ALLOWED_OPERATIONS = "allowed.operations";

    public static final String CDC_PRESERVE_EVENT = "preserve.event";

    public static final String CDC_MAXIMUM_RETRY_COUNT = "maximum.retry.count";
    public static final String CDC_DEFAULT_RETRY_COUNT = "-1"; // Indefinite retry

    public static final String CDC_DEACTIVATE_SEQUENCE = "deactivate.sequence";
    public static final String WAIT_FOR_COMPLETION_BEFORE_INTERRUPT_MS =
            "debezium.embedded.shutdown.pause.before.interrupt.ms";

    /** Output Properties **/
    public static final String CDC_DATABASE_NAME = "cdc.database";
    public static final String CDC_TABLES ="cdc.tables";
    public static final String CDC_OPERATIONS ="cdc.operations";
    public static final String CDC_TS_MS = "cdc.ts_ms";
    public static final String TS_MS = "ts_ms";

    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String SOURCE = "source";
    public static final String OP = "op";
    public static final String PAYLOAD = "payload";
    public static final String DB = "db";
    public static final String TABLE = "table";

    public static final String TRUE = "true";
    public static final String FALSE = "false";

}
