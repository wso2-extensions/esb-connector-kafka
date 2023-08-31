/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector;

public class KafkaConnectConstants {

    public static final String CONNECTOR_NAME = "kafka";
    public static final String NAME = "name";

    // Configuration properties keys
    public static final String BROKER_LIST = "bootstrap.servers";
    public static final String KEY_SERIALIZER_CLASS = "key.serializer";
    public static final String VALUE_SERIALIZER_CLASS = "value.serializer";
    public static final String ACK = "acks";
    public static final String BUFFER_MEMORY = "buffer.memory";
    public static final String COMPRESSION_TYPE = "compression.type";
    public static final String RETRIES = "retries";
    public static final String SSL_KEY_PASSWORD = "ssl.key.password";
    public static final String SSL_KEYSTORE_LOCATION = "ssl.keystore.location";
    public static final String SSL_KEYSTORE_PASSWORD = "ssl.keystore.password";
    public static final String SSL_TRUSTSTORE_LOCATION = "ssl.truststore.location";
    public static final String SSL_TRUSTSTORE_PASSWORD = "ssl.truststore.password";
    public static final String BATCH_SIZE = "batch.size";
    public static final String CLIENT_ID = "client.id";
    public static final String CONNECTION_MAX_IDLE_TIME = "connections.max.idle.ms";
    public static final String LINGER_TIME = "linger.ms";
    public static final String MAXIMUM_BLOCK = "max.block.ms";
    public static final String MAXIMUM_REQUEST_SIZE = "max.request.size";
    public static final String PARTITIONER_CLASS = "partitioner.class";
    public static final String RECEIVE_BUFFER_BYTES = "receive.buffer.bytes";
    public static final String REQUEST_TIMEOUT_MS = "request.timeout.ms";
    public static final String SASL_KERBEROS_SERVICE_NAME = "sasl.kerberos.service.name";
    public static final String SASL_MECHANISM = "sasl.mechanism";
    public static final String SECURITY_PROTOCOL = "security.protocol";
    public static final String SEND_BUFFER_BYTES = "send.buffer.bytes";
    public static final String SSL_ENABLED_PROTOCOLS = "ssl.enabled.protocols";
    public static final String SSL_KEYSTORE_TYPE = "ssl.keystore.type";
    public static final String SSL_PROTOCOL = "ssl.protocol";
    public static final String SSL_PROVIDER = "ssl.provider";
    public static final String SSL_TRUSTSTORE_TYPE = "ssl.truststore.type";
    public static final String TIMEOUT_TIME = "timeout.ms";
    public static final String SASL_JAAS_CONFIG = "sasl.jaas.config";
    public static final String BLOCK_ON_BUFFER_FULL = "block.on.buffer.full";
    public static final String MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = "max.in.flight.requests.per.connection";
    public static final String METADATA_FETCH_TIMEOUT = "metadata.fetch.timeout.ms";
    public static final String METADATA_MAXIMUM_AGE = "metadata.max.age.ms";
    public static final String METRIC_REPORTERS = "metric.reporters";
    public static final String METRICS_NUM_SAMPLES = "metrics.num.samples";
    public static final String METRICS_SAMPLE_WINDOW = "metrics.sample.window.ms";
    public static final String RECONNECT_BACKOFF_TIME = "reconnect.backoff.ms";
    public static final String RETRY_BACKOFF_TIME = "retry.backoff.ms";
    public static final String SASL_KERBEROS_KINIT_CMD = "sasl.kerberos.kinit.cmd";
    public static final String SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN = "sasl.kerberos.min.time.before.relogin";
    public static final String SASL_KERBEROS_TICKET_RENEW_JITTER = "sasl.kerberos.ticket.renew.jitter";
    public static final String SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR
            = "sasl.kerberos.ticket.renew.window.factor";
    public static final String SSL_CIPHER_SUITES = "ssl.cipher.suites";
    public static final String SSL_ENDPOINT_IDENTIFICATION_ALGORITHM = "ssl.endpoint.identification.algorithm";
    public static final String SSL_KEYMANAGER_ALGORITHM = "ssl.keymanager.algorithm";
    public static final String SSL_SECURE_RANDOM_IMPLEMENTATION = "";
    public static final String SSL_TRUSTMANAGER_ALGORITHM = "ssl.trustmanager.algorithm";
    // Confluence schema registry url
    public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

    // Configuration properties parameter
    public static final String PARAM_TOPIC = "topic";
    public static final String PARTITION_NO = "partitionNo";
    public static final String PARAM_KEY = "key";

    public static final String KAFKA_HEADER_PREFIX = "kafka.kafkaHeaderPrefix";
    public static final String DEFAULT_KAFKA_HEADER_PREFIX = "publishMessages:";

    // Configuration parameters for kafka connector
    public static final String KAFKA_BROKER_LIST = "kafka.bootstrapServers";
    public static final String KAFKA_KEY_SERIALIZER_CLASS = "kafka.keySerializerClass";
    public static final String KAFKA_VALUE_SERIALIZER_CLASS = "kafka.valueSerializerClass";
    public static final String KAFKA_ACKS = "kafka.acks";
    public static final String KAFKA_BUFFER_MEMORY = "kafka.bufferMemory";
    public static final String KAFKA_COMPRESSION_TYPE = "kafka.compressionType";
    public static final String KAFKA_RETRIES = "kafka.retries";
    public static final String KAFKA_SSL_KEY_PASSWORD = "kafka.sslKeyPassword";
    public static final String KAFKA_SSL_KEYSTORE_LOCATION = "kafka.sslKeystoreLocation";
    public static final String KAFKA_SSL_KEYSTORE_PASSWORD = "kafka.sslKeystorePassword";
    public static final String KAFKA_SSL_TRUSTSTORE_LOCATION = "kafka.sslTruststoreLocation";
    public static final String KAFKA_SSL_TRUSTSTORE_PASSWORD = "kafka.sslTruststorePassword";
    public static final String KAFKA_BATCH_SIZE = "kafka.batchSize";
    public static final String KAFKA_CLIENT_ID = "kafka.clientId";
    public static final String KAFKA_CONNECTION_MAX_IDLE_TIME = "kafka.connectionsMaxIdleTime";
    public static final String KAFKA_LINGER_TIME = "kafka.lingerTime";
    public static final String KAFKA_MAXIMUM_BLOCK = "kafka.maxBlockTime";
    public static final String KAFKA_MAXIMUM_REQUEST_SIZE = "kafka.maxRequestSize";
    public static final String KAFKA_PARTITIONER_CLASS = "kafka.partitionerClass";
    public static final String KAFKA_RECEIVE_BUFFER_BYTES = "kafka.receiveBufferBytes";
    public static final String KAFKA_REQUEST_TIMEOUT_MS = "kafka.requestTimeout";
    public static final String KAFKA_SASL_JAAS_CONFIG = "kafka.saslJaasConfig";
    public static final String KAFKA_SASL_KERBEROS_SERVICE_NAME = "kafka.saslKerberosServiceName";
    public static final String KAFKA_SASL_MECHANISM = "kafka.saslMechanism";
    public static final String KAFKA_SECURITY_PROTOCOL = "kafka.securityProtocol";
    public static final String KAFKA_SEND_BUFFER_BYTES = "kafka.sendBufferBytes";
    public static final String KAFKA_SSL_ENABLED_PROTOCOLS = "kafka.sslEnabledProtocols";
    public static final String KAFKA_SSL_KEYSTORE_TYPE = "kafka.sslKeystoreType";
    public static final String KAFKA_SSL_PROTOCOL = "kafka.sslProtocol";
    public static final String KAFKA_SSL_PROVIDER = "kafka.sslProvider";
    public static final String KAFKA_SSL_TRUSTSTORE_TYPE = "kafka.sslTruststoreType";
    public static final String KAFKA_TIMEOUT_TIME = "kafka.timeout";
    public static final String KAFKA_BLOCK_ON_BUFFER_FULL = "kafka.blockOnBufferFull";
    public static final String
            KAFKA_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = "kafka.maxInFlightRequestsPerConnection";
    public static final String KAFKA_METADATA_FETCH_TIMEOUT = "kafka.metadataFetchTimeout";
    public static final String KAFKA_METADATA_MAXIMUM_AGE = "kafka.metadataMaxAge";
    public static final String KAFKA_METRIC_REPORTERS = "kafka.metricReporters";
    public static final String KAFKA_METRICS_NUM_SAMPLES = "kafka.metricsNumSamples";
    public static final String KAFKA_METRICS_SAMPLE_WINDOW = "kafka.metricsSampleWindow";
    public static final String KAFKA_RECONNECT_BACKOFF_TIME = "kafka.reconnectBackoff";
    public static final String KAFKA_RETRY_BACKOFF_TIME = "kafka.retryBackoff";
    public static final String KAFKA_SASL_KERBEROS_KINIT_CMD = "kafka.saslKerberosKinitCmd";
    public static final String
            KAFKA_SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN = "kafka.saslKerberosMinTimeBeforeRelogin";
    public static final String KAFKA_SASL_KERBEROS_TICKET_RENEW_JITTER = "kafka.saslKerberosTicketRenewJitter";
    public static final String KAFKA_SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR
            = "kafka.saslKerberosTicketRenewWindowFactor";
    public static final String KAFKA_SSL_CIPHER_SUITES = "kafka.sslCipherSuites";
    public static final String KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM =
            "kafka.sslEndpointIdentificationAlgorithm";
    public static final String KAFKA_SSL_KEYMANAGER_ALGORITHM = "kafka.sslKeymanagerAlgorithm";
    public static final String KAFKA_SSL_SECURE_RANDOM_IMPLEMENTATION = "kafka.sslSecureRandomImplementation";
    public static final String KAFKA_SSL_TRUSTMANAGER_ALGORITHM = "kafka.sslTrustmanagerAlgorithm";
    public static final String KAFKA_USE_LATEST_VERSION = "kafka.useLatestVersion";
    public static final String KAFKA_AUTO_REGISTER_SCHEMAS = "kafka.autoRegisterSchemas";
    public static final String KAFKA_SUBJECT_NAME_STRATEGY = "kafka.subjectNameStrategy";

    // Configuration properties default values
    public static final String DEFAULT_ACK = "1";
    public static final String DEFAULT_KEY_SERIALIZER_CLASS = "";
    public static final String DEFAULT_VALUE_SERIALIZER_CLASS = "";
    public static final String DEFAULT_BUFFER_MEMORY = "33554432";
    public static final String DEFAULT_COMPRESSION_TYPE = "none";
    public static final String DEFAULT_RETRIES = "0";
    public static final String DEFAULT_BATCH_SIZE = "16384";
    public static final String DEFAULT_CLIENT_ID = "";
    public static final String DEFAULT_CONNECTION_MAX_IDLE_TIME = "540000";
    public static final String DEFAULT_LINGER_TIME = "0";
    public static final String DEFAULT_MAXIMUM_BLOCK = "60000";
    public static final String DEFAULT_MAXIMUM_REQUEST_SIZE = "1048576";
    public static final String DEFAULT_PARTITIONER_CLASS
            = "org.apache.kafka.clients.producer.internals.DefaultPartitioner";
    public static final String DEFAULT_RECEIVE_BUFFER_BYTES = "32768";
    public static final String DEFAULT_REQUEST_TIMEOUT_MS = "30000";
    public static final String DEFAULT_SEND_BUFFER_BYTES = "131072";
    public static final String DEFAULT_TIMEOUT_TIME = "30000";
    public static final String DEFAULT_BLOCK_ON_BUFFER_FULL = "false";
    public static final String DEFAULT_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = "5";
    public static final String DEFAULT_METADATA_FETCH_TIMEOUT = "60000";
    public static final String DEFAULT_METADATA_MAXIMUM_AGE = "300000";
    public static final String DEFAULT_METRIC_REPORTERS = "";
    public static final String DEFAULT_METRICS_NUM_SAMPLES = "2";
    public static final String DEFAULT_METRICS_SAMPLE_WINDOW = "30000";
    public static final String DEFAULT_RECONNECT_BACKOFF_TIME = "50";
    public static final String DEFAULT_RETRY_BACKOFF_TIME = "100";
    public static final String DEFAULT_SCHEMA_REGISTRY_URL = "http://localhost:8081";

    // Maximum default connection pool size
    public static final String DEFAULT_CONNECTION_POOL_MAX_SIZE = "-1";

    // Whether connection pooling is enabled
    public static final String CONNECTION_POOLING_ENABLED = "kafka.poolingEnabled";

    // Avro message constants
    public static final String KAFKA_SCHEMA_REGISTRY_URL = "kafka.schemaRegistryUrl";
    public static final String KAFKA_SCHEMA_REGISTRY_BASIC_AUTH_CREDENTIALS_SOURCE = "kafka.basicAuthCredentialsSource";
    public static final String KAFKA_SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO = "kafka.basicAuthUserInfo";
    public static final String KAFKA_KEY_SCHEMA_ID = "keySchemaId";
    public static final String KAFKA_KEY_SCHEMA = "keySchema";
    public static final String KAFKA_KEY = "key";
    public static final String KAFKA_VALUE_SCHEMA_ID = "valueSchemaId";
    public static final String KAFKA_VALUE_SCHEMA = "valueSchema";
    public static final String KAFKA_VALUE = "value";
    public static final String KAFKA_AVRO_SERIALIZER = "io.confluent.kafka.serializers.KafkaAvroSerializer";

    public static final String KAFKA_KEY_SCHEMA_SUBJECT = "keySchemaSubject";

    public static final String KAFKA_KEY_SCHEMA_VERSION = "keySchemaVersion";

    public static final String KAFKA_VALUE_SCHEMA_SUBJECT = "valueSchemaSubject";

    public static final String KAFKA_VALUE_SCHEMA_VERSION = "valueSchemaVersion";

    public static final String KAFKA_KEY_SCHEMA_SOFT_DELETED = "keySchemaSoftDeleted";

    public static final String KAFKA_VALUE_SCHEMA_SOFT_DELETED = "valueSchemaSoftDeleted";

    public static final String USE_LATEST_VERSION = "use.latest.version";

    public static final String AUTO_REGISTER_SCHEMAS = "auto.register.schemas";

    public static final String SUBJECT_NAME_STRATEGY = "value.subject.name.strategy";
}
