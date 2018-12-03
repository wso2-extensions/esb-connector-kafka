## Integration tests for WSO2 EI Kafka connector

### Pre-requisites:

- Maven 3.x
- Java 1.8

### Tested Platform:

- Ubuntu 16.04
- WSO2 EI 6.4.0

STEPS:

1. Download EI 6.4.0 by navigating the following the URL: https://wso2.com/integration/.

2. Download Apache Kafka from https://www.apache.org/dyn/closer.cgi?path=/kafka/1.0.0/kafka_2.12-1.0.0.tgz.
   Here kafka_2.12-1.0.0 is refered as <KAFKA_HOME>.

3. Copy the following client libraries from the <KAFKA_HOME>/lib directory to the <EI_HOME>/lib directory.

    * kafka_2.12-1.0.0.jar
    * kafka-clients-1.0.0.jar
    * metrics-core-2.2.0.jar
    * scala-library-2.12.3.jar
    * zkclient-0.10.jar
    * zookeeper-3.4.10.jar

3. Compress the EI zip and place it in "{CONNECTOR_HOME}/repository/"

4. Update "{CONNECTOR_HOME}/repository/esb-connector-kafkaTransport.properties" and "{CONNECTOR_HOME}/src/test/resources/artifacts/ESB/connector/config/kafkaTransport.properties" file.

5. Navigate to <KAFKA_HOME>,
    1. Run the following command to start the ZooKeeper server: `bin/zookeeper-server-start.sh config/zookeeper.properties`
    2. Run the following command to start the Kafka server: `bin/kafka-server-start.sh config/server.properties`

6. Go to "{CONNECTOR_HOME}/" and type "mvn clean install -Dskip-tests=false" to test and build.

7. Analyzing the output from Kafka brokers.
    Run the following command to verify the messages: `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning`