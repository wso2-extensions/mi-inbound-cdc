# Configuring CDC inbound Endpoint

The CDC inbound protocol is used to perform Change Data Capture in MI. The changes happening to any external database can be listened to using the CDC inbound endpoint. The CDC protocol uses Debezium to connect with the databases and capture the events. The protocol itself outputs the event via a sequence through the Inbound Endpoint. Currently CDC Inbound Endpoint supports MySQL, SQL Server, Postgres and Oracle databases. You need to place the client JARs required for your CDC inside the Micro Integrator, to use this inbound endpoint.

## Sample configuration

Given below is a sample CDC Inbound Endpoint configuration that can be used capture events from a MySQL database:

```
<inboundEndpoint xmlns="http://ws.apache.org/ns/synapse"
                 name="cdc-inbound-endpoint"
                 sequence="request"
                 onError="fault"
                 class="org.wso2.carbon.inbound.cdc.CDCPollingConsumer"
                 suspend="false">
   <parameters>
      <parameter name="interval">1000</parameter>
      <parameter name="name">engine</parameter>
      <parameter name="snapshot.mode">initial</parameter>
      <parameter name="offset.storage">org.apache.kafka.connect.storage.FileOffsetBackingStore</parameter>
      <parameter name="offset.storage.file.filename">cdc/offsetStorage/offsets1_.dat</parameter>
      <parameter name="connector.class">io.debezium.connector.mysql.MySqlConnector</parameter>
      <parameter name="database.hostname">localhost</parameter>
      <parameter name="database.port">3306</parameter>
      <parameter name="database.user">root</parameter>
      <parameter name="database.password">wso2:vault-lookup('mysql_password')</parameter>
      <parameter name="database.dbname">db_name</parameter>
      <parameter name="database.server.id">8574444</parameter>
      <parameter name="database.server.name">server_1</parameter>
      <parameter name="topic.prefix">topic2</parameter>
      <parameter name="schema.history.internal">io.debezium.storage.file.history.FileSchemaHistory</parameter>
      <parameter name="schema.history.internal.file.filename">cdc/schemaHistory/schema_history1_.dat</parameter>
      <parameter name="table.include.list">students.marks</parameter>
      <parameter name="allowed.operations">create</parameter>
   </parameters>
</inboundEndpoint>
```

## Properties

Listed below are the properties used for creating a CDC inbound endpoint.

All the params can be specified in the synapse config files for the CDC Inbound Endpoint, using the following format,


```
<parameter name="param_name">param_value</parameter>
```

### Required Properties [for the inbound endpoint]

The following properties are required when creating a CDC inbound endpoint.


<table>
  <tr>
   <td>Property
   </td>
   <td>Description
   </td>
  </tr>
  <tr>
   <td>interval
   </td>
   <td>The polling interval for the inbound endpoint to execute each cycle. This value is set in milliseconds.
   </td>
  </tr>
  <tr>
   <td>coordination
   </td>
   <td>This optional property is only applicable in a cluster environment. In a clustered environment, an inbound endpoint will only be executed in worker nodes. If set to <strong><code>true</code></strong> in a cluster setup, this will run the inbound only in a single worker node. Once the running worker is down, the inbound starts on another available worker in the cluster. By default, coordination is enabled.
   </td>
  </tr>
  <tr>
   <td>sequential
   </td>
   <td>Whether the messages need to be polled and injected sequentially or not. By default this is set to “True”.
   </td>
  </tr>
</table>



### Required Properties [for Debezium]

The following properties are required when creating a CDC inbound endpoint.


<table>
  <tr>
   <td>Property
   </td>
   <td>Description
   </td>
  </tr>
  <tr>
   <td>name
   </td>
   <td>Unique name for the connector
   </td>
  </tr>
  <tr>
   <td>snapshot.mode
   </td>
   <td>Specifies the criteria for running a snapshot when the connector starts. Possible settings are: initial, initial_only,  when_needed, never, schema_only, schema_only_recovery. (By default initial)
   </td>
  </tr>
  <tr>
   <td>connector.class
   </td>
   <td>The name of the Java class for the connector.
<p>
Ex : for MySQL database, <code>io.debezium.connector.mysql.MySqlConnector</code>
   </td>
  </tr>
  <tr>
   <td>topic.prefix
   </td>
   <td>Topic prefix that provides a namespace for the database server that you want Debezium to capture. The prefix should be unique across all other connectors, since it is used as the prefix for all Kafka topic names that receive records from this connector. Only alphanumeric characters, hyphens, dots and underscores must be used in the database server logical name.
   </td>
  </tr>
  <tr>
   <td>schema.history.internal
   </td>
   <td>The name of the Java class that is responsible for persistence of the database schema history.
<p>
It must implement <code>&lt;…​>.SchemaHistory</code> interface.
<p>
Default value is <code>KafkaSchemaHistory</code>
<p>
<code>Refer : <a href="https://debezium.io/documentation/reference/stable/development/engine.html">https://debezium.io/documentation/reference/stable/development/engine.html</a> for more information.</code>
<code>For database related options : see the example </code>For RedisOffsetBackingStore  :<a href="https://debezium.io/documentation/reference/stable/operations/debezium-server.html#debezium-source-configuration-properties">https://debezium.io/documentation/reference/stable/operations/debezium-server.html#debezium-source-configuration-properties</a>
   </td>
  </tr>
  <tr>
   <td>schema.history.internal.file.filename
   </td>
   <td>This value is required only if   <strong>io.debezium.storage.file.history.FileSchemaHistory</strong> was provided for the schema.history.internal value. you need to specify the path to a file where the database schema history is stored.
<p>
By default, the file will be stored at `&lt;Product home>/cdc/schemaHistory` directory
   </td>
  </tr>
  <tr>
   <td><code>schema.history.internal.kafka.topic</code>
   </td>
   <td>The Kafka topic where the database schema history is stored.
<p>
Required when <code>schema.history.internal</code> is set to the <code>&lt;…​>.KafkaSchemaHistory</code>.
   </td>
  </tr>
  <tr>
   <td><code>schema.history.internal.kafka.bootstrap.servers</code>
   </td>
   <td>The initial list of Kafka cluster servers to connect to. The cluster provides the topic to store the database schema history.
<p>
Required when <code>schema.history.internal</code> is set to the <code>&lt;…​>.KafkaSchemaHistory</code>.
   </td>
  </tr>
  <tr>
   <td><code>offset.storage</code>
   </td>
   <td>The name of the Java class that is responsible for persistence of connector offsets. It must implement <code>&lt;…​>.OffsetBackingStore</code> interface.
   </td>
  </tr>
  <tr>
   <td><code>offset.storage.file.filename</code>
   </td>
   <td>Path to file where offsets are to be stored. Required when offset.storage is set to the &lt;…​>.FileOffsetBackingStore.
<p>
By default, the file will be stored at `&lt;Product home>/cdc/offsetStorage` directory
   </td>
  </tr>
  <tr>
   <td><code>offset.storage.topic</code>
   </td>
   <td>The name of the Kafka topic where offsets are to be stored. Required when <code>offset.storage</code> is set to the <code>&lt;…​>.KafkaOffsetBackingStore</code>.
   </td>
  </tr>
  <tr>
   <td><code>offset.storage.partitions</code>
   </td>
   <td>The number of partitions used when creating the offset storage topic. Required when <code>offset.storage</code> is set to the <code>&lt;…​>.KafkaOffsetBackingStore</code>.
   </td>
  </tr>
  <tr>
   <td><code>offset.storage.replication.factor</code>
   </td>
   <td>Replication factor used when creating the offset storage topic. Required when <code>offset.storage</code> is set to the <code>&lt;…​>.KafkaOffsetBackingStore</code>.
   </td>
  </tr>
  <tr>
   <td>database.hostname
   </td>
   <td>IP address or host name of the database server
   </td>
  </tr>
  <tr>
   <td>database.port
   </td>
   <td>Port number (Integer) of the database server
   </td>
  </tr>
  <tr>
   <td>database.user
   </td>
   <td>Name of the database user to use when connecting to the database server.
   </td>
  </tr>
  <tr>
   <td>database.password
   </td>
   <td>The password itself to connect to the database
<p>
Example : <code>&lt;parameter name="database.password">your_password&lt;/parameter></code>
<p>
or
<p>
The <a href="https://apim.docs.wso2.com/en/latest/install-and-setup/setup/mi-setup/security/encrypting_plain_text/">secure vault</a> String  where the password is encoded.
<p>
Example : <code>&lt;parameter name="database.password">{wso2:vault-lookup(password_alias')}&lt;/parameter></code>
   </td>
  </tr>
  <tr>
   <td>database.dbname
   </td>
   <td>The name of the database that needs to be listened to.
<p>
*This is applicable only for MySQL, Postgres and Oracle
   </td>
  </tr>
  <tr>
   <td>database.instance
   </td>
   <td>Specifies the instance name of the SQL Server named instance.
<p>
*This is applicable only for SQL Server
   </td>
  </tr>
  <tr>
   <td>database.names
   </td>
   <td>The comma-separated list of the SQL Server database names from which to stream the changes.
<p>
*This is applicable only for SQL Server
   </td>
  </tr>
  <tr>
   <td>database.server.id
   </td>
   <td>A numeric ID of this database client, which must be unique across all currently-running database processes in the MySQL cluster
<p>
*This is applicable only for MySQL
   </td>
  </tr>
  <tr>
   <td>table.include.list
   </td>
   <td>The list of tables from the selected database that the changes for them need to be captured.
<p>
Tables should be provided in the format of,
<p>
By default, all operations are listened to.
   </td>
  </tr>
  <tr>
   <td>allowed.operations
   </td>
   <td>Operations that the user needs to listen to, in the specified database tables.
<p>
Should provide comma separated values for create/update/delete/truncate
<p>
By default, truncate operations are skipped.
   </td>
  </tr>
  <tr>
   <td>database.out.server.name
   </td>
   <td>Name of the XStream outbound server configured in the database.
<p>
*Only applicable if using Oracle database.
   </td>
  </tr>
</table>



### Optional Properties

Other than the above required properties, you can add properties defined by Debezium, based on the Database type you are listening to.

To see the properties available for each DBMS type, please follow the below documentations : 

MySQL - [https://debezium.io/documentation/reference/stable/connectors/mysql.html#mysql-connector-properties](https://debezium.io/documentation/reference/stable/connectors/mysql.html#mysql-connector-properties)

SQL Server - [https://debezium.io/documentation/reference/stable/connectors/sqlserver.html#sqlserver-connector-properties](https://debezium.io/documentation/reference/stable/connectors/sqlserver.html#sqlserver-connector-properties)

Postgres - [https://debezium.io/documentation/reference/stable/connectors/postgresql.html#postgresql-connector-properties](https://debezium.io/documentation/reference/stable/connectors/postgresql.html#postgresql-connector-properties)

Oracle -  [https://debezium.io/documentation/reference/stable/connectors/oracle.html#oracle-connector-properties](https://debezium.io/documentation/reference/stable/connectors/oracle.html#oracle-connector-properties)



Please note the following points :

* If you define the **allowed.operations** property to listen to specific operations, please do not use the **skipped.operations** property that Debezium provides.
* You cannot change the event output format of Debezium, because the captured event is handled internally by the inbound endpoint and mediated to outside via a sequence.
* It is mandatory to keep the server.id property unique across CDC Inbound endpoints. This is applicable to the clustering scenarios as well. (Even if all artifacts are expected to be identical in MI nodes in a cluster, still the server.id property for each CDC inbound endpoint must be unique inside the cluster.)
* To enable database storage options for schema history and offset storage, see For RedisOffsetBackingStore  :[https://debezium.io/documentation/reference/stable/operations/debezium-server.html#debezium-source-configuration-properties](https://debezium.io/documentation/reference/stable/operations/debezium-server.html#debezium-source-configuration-properties)

## How to use

Download the inbound endpoint `org.apache.synapse.cdc.poll-*.jar` JAR file and add it in the `&lt;Product Home>/dropins` directory.

### Setting up the databases

Apart from the above steps, you need to do the additional configurations in the database level,  to facilitate CDC. 


#### 1. MySQL
1. Create a user. Check [https://debezium.io/documentation/reference/stable/connectors/mysql.html#mysql-creating-user](https://debezium.io/documentation/reference/stable/connectors/mysql.html#mysql-creating-user)
2. Create the database to listen to, capture changes or consider the existing database that you need to listen to.

        Let’s assume you have a database called “Inventory” which contains tables called “products” and “suppliers”.

3. Further, let's assume that you need to listen to the data changes corresponding to insert and delete operations in the products table.
4. Download the JDBC driver from the MySQL website.
5. Unzip the archive and Copy the `mysql-connector-java-*-bin.jar` JAR and place it in the `&lt;Product Home>/wso2/lib` directory.
6. Download the latest Debezium orbit jar from [nexus](https://maven.wso2.org/nexus/content/repositories/public/org/wso2/orbit/debezium/debezium/) and place in `&lt;Product Home>/dropins`.

7. Enable binlog

        [https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-mysql-binlog](https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-mysql-binlog)

8. Enable GTIDs.  [https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-mysql-gtids](https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-mysql-gtids)
9. Configure session timeouts. [https://debezium.io/documentation/reference/stable/connectors/mysql.html#mysql-session-timeouts](https://debezium.io/documentation/reference/stable/connectors/mysql.html#mysql-session-timeouts)
10. Enable query log events : 

        [https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-query-log-events](https://debezium.io/documentation/reference/stable/connectors/mysql.html#enable-query-log-events)

11. Validating binlog row value operations : [https://debezium.io/documentation/reference/stable/connectors/mysql.html#validate-binlog-row-value-options](https://debezium.io/documentation/reference/stable/connectors/mysql.html#validate-binlog-row-value-options)
12. Create the synapse config file `<inbound_endpoint_name>.xml`  to the inbound endpoint as follows : 

    1. Update the host, user name, password for the database.
    2. You can choose a preferred schema history and offset storage option and provide the file locations or kafka topics based on the chosen storage mechanism.

```
<inboundEndpoint name="cdc-inbound-endpoint" onError="fault" protocol="cdc" sequence="cdc_process_seq" suspend="false" xmlns="http://ws.apache.org/ns/synapse">
   <parameters>
      <parameter name="interval">1000</parameter>
      <parameter name="name">engine</parameter>
      <parameter name="snapshot.mode">initial</parameter>
      <parameter name="sequential">true</parameter>
      <parameter name="snapshot.max.threads">1</parameter>
      <parameter name="offset.storage">org.apache.kafka.connect.storage.FileOffsetBackingStore</parameter>
      <parameter name="offset.storage.file.filename">cdc/offsetStorage/offsets1_.dat</parameter>
      <parameter name="connector.class">io.debezium.connector.mysql.MySqlConnector</parameter>
      <parameter name="database.hostname">localhost</parameter>
      <parameter name="database.port">3306</parameter>
      <parameter name="database.user">root</parameter>
      <parameter name="database.password">your_password</parameter>
      <parameter name="database.dbname">db_name</parameter>
      <parameter name="database.server.id">8574444</parameter>
      <parameter name="topic.prefix">topic1</parameter>
      <parameter name="schema.history.internal">io.debezium.storage.file.history.FileSchemaHistory</parameter>
      <parameter name="schema.history.internal.file.filename">cdc/schemaHistory/schema_history1_.dat</parameter>
      <parameter name="table.include.list">inventory.products</parameter>
      <parameter name="allowed.operations">create, delete</parameter>
   </parameters>
</inboundEndpoint>

```

13. Place the `<inbound_endpoint_name>.xml` file inside `<product home>/repository/deployment/server/synapse-configs/default/inbound-endpoints`.
14. Place the following sequence file inside `<product home>/repository/deployment/server/synapse-configs/default/sequences`.
15. Start the Micro Integrator.

#### 2. Postgres

 1. Setup the postgres server referring to [https://debezium.io/documentation/reference/stable/connectors/postgresql.html#setting-up-postgresql](https://debezium.io/documentation/reference/stable/connectors/postgresql.html#setting-up-postgresql).
 2. Download the postgres jdbc jar and place in `<Product Home>/lib`.
 3. Download the cdc-orbit jar from [here](https://maven.wso2.org/nexus/content/repositories/public/org/wso2/orbit/debezium/debezium/) and place in `<Product Home>/dropins`.
 4. Follow the steps 12, 13, 14, 15 under [Mysql](#1-mysql), modifying the params in Synapse config file.

#### 3.SQL Server
1. Setup SQL server referring to [https://debezium.io/documentation/reference/stable/connectors/sqlserver.html#setting-up-sqlserver](https://debezium.io/documentation/reference/stable/connectors/sqlserver.html#setting-up-sqlserver).
2. Download the Mssql jdbc jar and place in `<Product Home>/lib`.
3. Download the cdc-orbit jar from [here](https://maven.wso2.org/nexus/content/repositories/public/org/wso2/orbit/debezium/debezium/) and place in `<Product Home>/dropins`.
   Follow the steps 12, 13, 14, 15 under [Mysql](#1-mysql), modifying the params in Synapse config file.

#### 4.Oracle
1. Set up the Oracle database referring to [https://debezium.io/documentation/reference/stable/connectors/oracle.html#setting-up-oracle](https://debezium.io/documentation/reference/stable/connectors/oracle.html#setting-up-oracle).
2. Download the Oracle jdbc jar and place in `<Product Home>/lib`.
3. Download the cdc-orbit jar from [here](https://maven.wso2.org/nexus/content/repositories/public/org/wso2/orbit/debezium/debezium/) and place in `<Product Home>/dropins`.
4. Follow the steps 12, 13, 14, 15 under [Mysql](#1-mysql), modifying the params in Synapse config file.


### Clustering scenarios	

1. Setup MI cluster refering to [https://apim.docs.wso2.com/en/latest/install-and-setup/setup/mi-setup/deployment/deploying_wso2_ei/#](https://apim.docs.wso2.com/en/latest/install-and-setup/setup/mi-setup/deployment/deploying_wso2_ei/#).
2. Make sure that the server.id param is unique across all the CDC Inbound endpoints in the cluster. If not, change the server.id param (When listening to  MySQL databases)
3. Ensure that the coordination param is set to ture
4. Start the MI nodes in the cluster
5. You may notice that, only one (coordinator)  MI instance is mediating the events. Once the coordinator is down, one of the members will carry on the mediation. 
**Important** :  In clustering , it is mandatory to point the same data source for schema history and offset storage for all the MI instances in the cluster.  
