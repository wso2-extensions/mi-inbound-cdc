# CDC Inbound Endpoint

## Introduction

The CDC inbound protocol is used to perform Change Data Capture in MI. The changes happening to any external database can be listened to using the CDC inbound endpoint. The CDC protocol uses Debezium to connect with the databases and capture the events. The protocol itself outputs the event via a sequence through the Inbound Endpoint. Currently, CDC Inbound Endpoint supports MySQL, SQL Server, Postgres, Oracle, and Db2 databases. You need to place the client JARs required for your CDC inside the Micro Integrator to use this inbound endpoint.

## Compatibility

| Inbound Endpoint version                                                        | Supported WSO2 MI version |
|---------------------------------------------------------------------------------|---------------------------|
| [1.1.0](https://github.com/wso2-extensions/esb-inbound-cdc/releases/tag/v1.1.0) | MI 4.3.0, MI 4.2.0        |

## Getting started

To get started with the inbound endpoint, go to [CDC Inbound Endpoint Overview](https://mi.docs.wso2.com/en/latest/reference/connectors/cdc-inbound-endpoint/cdc-inbound-endpoint-overview/).

## Building from the source

Follow the steps given below to build the CDC Inbound Endpoint from the source code.

1. Get a clone or download the source from [Github](https://github.com/wso2-extensions/esb-inbound-cdc).
2. Run the following Maven command from the `esb-inbound-cdc` directory: `mvn clean install`.
3. The JAR file for the CDC Inbound Endpoint is created in the `esb-inbound-cdc/target` directory.
