#Publishing Messages using Kafka

The publishMessages operation allows you to publish messages to the Kafka brokers via Kafka topics.

**publishMessages**
````xml
<kafkaTransport.publishMessages>
    <topic>topicName</topic>
    <partitionNo>partitionNo</partitionNo>
</kafkaTransport.publishMessages>
````

**Properties**

| Property        | Description |
| ------------- |-------------|
| topic    | The name of the topic. |
| partitionNo      | The partition number of the topic. |

If required, you can add [custom headers](https://cwiki.apache.org/confluence/display/KAFKA/A+Case+for+Kafka+Headers) to the records in publishMessage operation:

````
<Content-Type>Value</Content-Type>
````
You can add the parameter as follows in the publishMessage operation:

````
<kafkaTransport.publishMessage configKey="kafka_init">
    <topic>topicName</topic>
    <partitionNo>partitionNo</partitionNo>
    <Content-Type>Value</Content-Type>
</kafkaTransport.publishMessage>
````

**Sample scenario**

Given below is a sample scenario that demonstrates how to send messages to a Kafka broker via Kafka topics.

**Prerequisites**

* Download and install [Apache Kafka](http://kafka.apache.org/downloads.html). For information, see [Apache Kafka
 documentation](http://kafka.apache.org/documentation.html).

* Copy the following client libraries from the <KAFKA_HOME>/lib directory to the <ESB_HOME>/repository/components/lib 
directory.

    * [kafka_2.12-1.0.0.jar](https://mvnrepository.com/artifact/org.apache.kafka/kafka_2.12/1.0.0)  
    * [kafka-clients-1.0.0.jar](https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients/1.0.0)
    * [metrics-core-2.2.0.jar](https://mvnrepository.com/artifact/com.yammer.metrics/metrics-core/2.2.0)
    * [scala-library-2.12.3.jar](https://mvnrepository.com/artifact/org.scala-lang/scala-library/2.12.3)
    * [zkclient-0.10.jar](https://mvnrepository.com/artifact/com.101tec/zkclient/0.10)
    * [zookeeper-3.4.10.jar](https://mvnrepository.com/artifact/org.apache.zookeeper/zookeeper/3.4.10)

* Run the following command to start the ZooKeeper server:

    ````
    bin/zookeeper-server-start.sh config/zookeeper.properties
    ````

* Run the following command to start the Kafka server:
    ````
    bin/kafka-server-start.sh config/server.properties
    ````

**Building the sample**

Given below is a sample proxy service that illustrates how you can connect to a Kakfa broker with the init operation and then use the publishMessages operation to publish messages via the topic without security.

   ````xml
    <proxy xmlns="http://ws.apache.org/ns/synapse"
       name="KafkaTransport"
       transports="https http"
       startOnLoad="true"
       trace="disable">
    <description/>
    <target>
        <inSequence>
        <kafkaTransport.init>
            <bootstrapServers>localhost:9092</bootstrapServers>
            <keySerializerClass>org.apache.kafka.common.serialization.StringSerializer</keySerializerClass>                                                                                             
            <valueSerializerClass>org.apache.kafka.common.serialization.StringSerializer</valueSerializerClass>
            <maxPoolSize>100</maxPoolSize>                                                                                      
        </kafkaTransport.init>
        <kafkaTransport.publishMessages>
            <topic>test</topic>
        </kafkaTransport.publishMessages>
    </inSequence>
    </target>
    </proxy>
  ````
Given below is a sample proxy service that illustrates how you can connect to a Kakfa broker with the init operation and then use the publishMessages operation to publish messages via the topic with security.

**proxy with security**

````xml
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="testKafka"
       startOnLoad="true"
       statistics="disable"
       trace="disable"
       transports="http,https">
   <target>
      <inSequence>
         <kafkaTransport.init>
            <bootstrapServers>localhost:9092</bootstrapServers>
            <keySerializerClass>org.apache.kafka.common.serialization.StringSerializer</keySerializerClass>
            <valueSerializerClass>org.apache.kafka.common.serialization.StringSerializer</valueSerializerClass>
            <securityProtocol>SSL</securityProtocol>
            <sslTruststoreLocation>{path-to-certificate}/kafka.server.truststore.jks</sslTruststoreLocation>
            <sslTruststorePassword>test1234</sslTruststorePassword>
            <sslKeystoreLocation>{path-to-certificate}/kafka.server.keystore.jks</sslKeystoreLocation>
            <sslKeystorePassword>test1234</sslKeystorePassword>
            <sslKeyPassword>test1234</sslKeyPassword>
            <maxPoolSize>100</maxPoolSize>
         </kafkaTransport.init>
         <kafkaTransport.publishMessages>
            <topic>test</topic>
         </kafkaTransport.publishMessages>
      </inSequence>
   </target>
   <description/>
</proxy>
````
Given below is a sample proxy service that illustrates how you can acknowledge the backend with Kafka metadata (e.g., topic, partition, offset) to check the response with publishMessages operation.

We use the [faultHandlerSeq](faulthandler.md) custom sequence to acknowledge the backend with an error status in case of a failure.

**Proxy with success or failiure status**
````xml
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="KafkaTransport"
       startOnLoad="true"
       statistics="disable"
       trace="disable"
       transports="http,https">
   <target faultSequence="faultHandlerSeq">
      <inSequence>
         <kafkaTransport.init>
            <bootstrapServers>localhost:9092</bootstrapServers>
            <keySerializerClass>org.apache.kafka.common.serialization.StringSerializer</keySerializerClass>
            <valueSerializerClass>org.apache.kafka.common.serialization.StringSerializer</valueSerializerClass>
            <maxPoolSize>50</maxPoolSize>
            <requestTimeout>10000</requestTimeout>
            <acks>all</acks>
            <timeout>8000</timeout>
            <metadataFetchTimeout>5000</metadataFetchTimeout>
         </kafkaTransport.init>
         <kafkaTransport.publishMessages>
            <topic>test</topic>
         </kafkaTransport.publishMessages>
         <payloadFactory media-type="json">
            <format>
                {"topic":"$1", "partition":"$2", "offset":"$3"}
          </format>
            <args>
               <arg evaluator="xml" expression="$ctx:topic"/>
               <arg evaluator="xml" expression="$ctx:partition"/>
               <arg evaluator="xml" expression="$ctx:offset"/>
            </args>
         </payloadFactory>
         <property name="messageType" scope="axis2" value="application/json"/>
         <respond/>
      </inSequence>
   </target>
   <description/>
</proxy>
````

**Executing the sample**

Send a message to the Kafka broker using a sample client.The following digram illustrates how to send a message using Jmeter:

![alt text](images/sampleJmeterScreen.png)

**Analyzing the output**

Run the following command to verify the messages:
````
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
````
See the following message content:
````
{"name":"sample"}
````
This demonstrates how the Kafka connector publishes messages to the Kafka brokers.
