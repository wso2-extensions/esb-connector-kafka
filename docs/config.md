# Configuring Kafka Operations

[[Prerequisites]](#Prerequisites) [[Initializing the Connector]](#initializing-the-connector)

## Prerequisites

To use the Kafka connector, download and install [Apache Kafka](http://kafka.apache.org/downloads.html).

>The recommended version is kafka 2.12-2.8.2. For all available versions of Kafka that you can download, see https://kafka.apache.org/downloads. The recommended Java version is 11.

To configure the Kafka connector, copy the following client libraries from the <KAFKA_HOME>/lib directory to the <MI_HOME>/lib directory.

* [kafka_2.12-2.8.2.jar](https://mvnrepository.com/artifact/org.apache.kafka/kafka_2.12/2.8.2)  
* [kafka-clients-2.8.2.jar](https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients/2.8.2)
* [metrics-core-2.2.0.jar](https://mvnrepository.com/artifact/com.yammer.metrics/metrics-core/2.2.0)
* [scala-library-2.12.13jar](https://mvnrepository.com/artifact/org.scala-lang/scala-library/2.12.13)
* [zkclient-0.10.jar](https://mvnrepository.com/artifact/com.101tec/zkclient/0.10)
* [zookeeper-3.5.9.jar](https://mvnrepository.com/artifact/org.apache.zookeeper/zookeeper/3.5.9)

Copy the following client libraries to the <MI_HOME>/lib directory when dealing with Kafka Avro Serialization (can be copied from the Confluent Platform),

* avro-1.11.3.jar
* common-config-5.4.0.jar
* common-utils-5.4.0.jar
* kafka-avro-serializer-5.3.0.jar
* kafka-schema-registry-client-5.3.0.jar

To use the Kafka connector, add the element <kafkaTransport.init> in your configuration before carrying out any other 
Kafka operation 
[with security](config.md) or [without security](#enabling-security).

### Enabling security

For  information on how to enable TLS authentication for the Kafka broker, producer, and consumer, see 
[Enabling Security for the Kafka Connector](security.md).

## Initializing the Connector

Given below is a sample configuration to create a producer without security.

**init**

````xml
    <kafkaTransport.init>
        <name>Sample_Kafka</name>
        <bootstrapServers>localhost:9092</bootstrapServers>
        <keySerializerClass>org.apache.kafka.common.serialization.StringSerializer</keySerializerClass>
        <valueSerializerClass>org.apache.kafka.common.serialization.StringSerializer</valueSerializerClass>
    </kafkaTransport.init>
    
````


> **security** : There is an additional feature for security found in Kafka version 0.9.0.0 and above. You can configure it using the element <kafkaTransport.init> as shown in the sample below:

**init with security**

````xml
<kafkaTransport.init>
    <name>Sample_Kafka</name>
     <bootstrapServers>localhost:9092</bootstrapServers>
     <keySerializerClass>org.apache.kafka.common.serialization.StringSerializer</keySerializerClass>
     <valueSerializerClass>org.apache.kafka.common.serialization.StringSerializer</valueSerializerClass>
     <securityProtocol>SSL</securityProtocol>
     <sslTruststoreLocation>/home/hariprasath/Desktop/kafkaNewJira/certKafka/kafka.server.truststore.jks</sslTruststoreLocation>
     <sslTruststorePassword>test1234</sslTruststorePassword>
     <sslKeystoreLocation>/home/hariprasath/Desktop/kafkaNewJira/certKafka/kafka.server.keystore.jks</sslKeystoreLocation>
     <sslKeystorePassword>test1234</sslKeystorePassword>
     <sslKeyPassword>test1234</sslKeyPassword>
 </kafkaTransport.init>

````

**init with kafka avro serializer**

````xml
<kafkaTransport.init>
    <name>Sample_Kafka</name>
    <bootstrapServers>localhost:9092</bootstrapServers>
    <keySerializerClass>io.confluent.kafka.serializers.KafkaAvroSerializer</keySerializerClass>
    <valueSerializerClass>io.confluent.kafka.serializers.KafkaAvroSerializer</valueSerializerClass>
    <schemaRegistryUrl>http://localhost:8081</schemaRegistryUrl>
</kafkaTransport.init>
````

**init with kafka avro serializer when schema registry is secured with basic auth**

````xml
<kafkaTransport.init>
    <name>Sample_Kafka</name>
    <bootstrapServers>localhost:9092</bootstrapServers>
    <keySerializerClass>io.confluent.kafka.serializers.KafkaAvroSerializer</keySerializerClass>
    <valueSerializerClass>io.confluent.kafka.serializers.KafkaAvroSerializer</valueSerializerClass>
    <schemaRegistryUrl>http://localhost:8081</schemaRegistryUrl>
    <basicAuthCredentialsSource>USER_INFO</basicAuthCredentialsSource>
    <basicAuthUserInfo>admin:admin</basicAuthUserInfo>
</kafkaTransport.init>
````

**Properties**
* name: Required. Unique name to identify the connection.
* bootstrapServers: Required. The Kafka brokers listed as host1:port1 and host2:port2.
* keySerializerClass: Required. The serializer class for the key that implements the serializer interface.
* valueSerializerClass: Required. The serializer class for the value that implements the serializer interface.
* schemaRegistryUrl: The URL of the confluent schema registry, only applicable when dealing with apache avro serializer class.
* basicAuthCredentialsSource: The source of basic auth credentials (e.g. USER_INFO, URL), when schema registry is secured to use basic auth.
* basicAuthUserInfo: The relevant basic auth credentials (should be used with basicAuthCredentialsSource).
* acks: The number of acknowledgments that the producer requires for the leader to receive before considering a 
request to be complete.
* bufferMemory: The total bytes of memory the producer can use to buffer records waiting to be sent to the server.
* compressionType: The compression type for the data generated by the producer.
* retries: Set a value greater than zero if you want the client to resent any records automatically when a request fails.
* sslKeyPassword: The password of the private key in the keystore file. Setting this for the client is optional.
* sslKeystoreLocation: The location of the key store file. Setting this for the client is optional. Set this when you want to have two-way authentication for the client.
* sslKeystorePassword: The store password for the keystore file. Setting this for the client is optional. Set it only if ssl.keystore.location is configured.
* sslTruststoreLocation: The location of the trust store file.
* sslTruststorePassword: The password for the trust store file.
* batchSize: Specify how many records the producer should batch together when multiple records are sent to the same partition.
* clientId: The client identifier that you pass to the server when making requests.
connectionsMaxIdleTime: The duration in milliseconds after which idle connections should be closed.
* lingerTime: The time, in milliseconds, to wait before sending a record. Set this property when you want the client to reduce the number of requests sent when the load is moderate. This adds a small delay rather than immediately sending out a record. Therefore, the producer waits up to allow other records to be sent so that the requests can be batched together.
* maxBlockTime: The maximum time in milliseconds that the KafkaProducer.send() and the KafkaProducer.partitionsFor() methods can be blocked.
* maxRequestSize: The maximum size of a request in bytes.
* partitionerClass: The partitioner class that implements the partitioner interface.
* receiveBufferBytes: The size of the TCP receive buffer (SO_RCVBUF) to use when reading data.
* requestTimeout: The maximum amount of time, in milliseconds, that a client waits for the server to respond.
* saslJaasConfig: JAAS login context parameters for SASL connections in the format used by JAAS configuration files. 
* saslKerberosServiceName: The Kerberos principal name that Kafka runs as.
* securityProtocol: The protocol used to communicate with brokers.
* sendBufferBytes: The size of the TCP send buffer (SO_SNDBUF) to use when sending data.
* sslEnabledProtocols: The list of protocols enabled for SSL connections.
* sslKeystoreType: The format of the keystore file. Setting this for the client is optional.
* sslProtocol: The SSL protocol used to generate the SSLContext.
* sslProvider: The name of the security provider used for SSL connections. The default value is the default security 
provider of the JVM.
* sslTruststoreType: The  format of the trust store file.
* timeout: The maximum amount of time, in milliseconds, that the server waits for the acknowledgments from followers to meet the acknowledgment requirements that the producer has specified with acks configuration.
* blockOnBufferFull: Set to true to stop accepting new records when the memory buffer is full. When blocking is not desirable, set this property to false, which causes the producer to throw an exception if a recrord is sent to the memory buffer when it is full.
* maxInFlightRequestsPerConnection: The maximum number of unacknowledged requests that the client can send via a 
single connection before blocking.
* metadataFetchTimeout: The maximum amount of time, in milliseconds, to block and wait for the metadata fetch to succeed before throwing an exception to the client.
* metadataMaxAge: The period of time, in milliseconds, after which you should refresh metadata even if there was no partition leadership changes to proactively discover any new brokers or partitions.
* metricReporters: A list of classes to use as metrics reporters.
* metricsNumSamples: The number of samples maintained to compute metrics.
* metricsSampleWindow: The window of time, in milliseconds, that a metrics sample is computed over.
* reconnectBackoff: The amount of time to wait before attempting to reconnect to a given host.
* retryBackoff: The amount of time, in milliseconds, to wait before attempting to retry a failed request to a given topic partition.
* saslKerberosKinitCmd: The kerberos kinit command path.
* saslKerberosMinTimeBeforeRelogin: Login thread's sleep time, in milliseconds, between refresh attempts. 
* saslKerberosTicketRenewJitter: Percentage of random jitter added to the renewal time.
* saslKerberosTicketRenewWindowFactor: The login thread sleeps until the specified window factor of time from the 
last refresh to the ticket's expiry is reached, after which it will try to renew the ticket.
* sslCipherSuites: A list of cipher suites.
* sslEndpointIdentificationAlgorithm: The endpoint identification algorithm to validate the server hostname using a server certificate.
* sslKeymanagerAlgorithm: The algorithm used by the key manager factory for SSL connections. The default value is the key
 manager factory algorithm configured for the Java Virtual Machine.
* sslSecureRandomImplementation: The SecureRandom PRNG implementation to use for SSL cryptography operations.
* sslTrustmanagerAlgorithm: The algorithm used by the trust manager factory for SSL connections. The default value is the trust manager factory algorithm configured for the Java Virtual Machine.
* maxPoolSize: The maximum number of message requests that can share the Kafka connection.

**Performance tuning tip**  
For better throughput, configure the <maxPoolSize> parameter as follows in the <init> configuration:
````
<maxPoolSize>20</maxPoolSize>
````
If you do not specify the maxPoolSizeparameter in the configuration, a Kafka connection is created for each message request.

Now that you have connected to Kafka, you can start publishing messages to the Kafka brokers. For more information, see 

[Publishing Messages using Kafka](publishmessage.md).
