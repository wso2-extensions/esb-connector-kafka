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

package org.wso2.carbon.connector.utils;

import java.util.TimeZone;

public class KafkaConnectConstants {

    public static final String CONNECTOR_NAME = "kafka";
    public static final String NAME = "name";

    // Configuration properties keys
    public static final String TIMEOUT_TIME = "timeout.ms";
    public static final String BLOCK_ON_BUFFER_FULL = "block.on.buffer.full";
    public static final String METADATA_FETCH_TIMEOUT = "metadata.fetch.timeout.ms";
    public static final String SASL_OAUTHBEARER_TOKEN_ENDPOINT = "sasl.oauthbearer.token.endpoint.url";
    public static final String SASL_OAUTHBEARER_SCOPE_CLAIM_NAME = "sasl.oauthbearer.scope.claim.name";
    public static final String SASL_LOGIN_CONNECT_TIMEOUT = "sasl.login.connect.timeout.ms";
    public static final String SASL_LOGIN_READ_TIMEOUT = "sasl.login.read.timeout.ms";
    public static final String SASL_LOGIN_RETRY_BACKOFF = "sasl.login.retry.backoff.ms";
    public static final String SASL_LOGIN_RETRY_BACKOFF_MAX = "sasl.login.retry.backoff.max.ms";
    public static final String MESSAGE_SEND_MAX_RETRIES = "message.send.max.retries";

    // Configuration properties parameter
    public static final String PARAM_DLQ_TOPIC = "dlqTopic";
    public static final String DLQ_TOPIC_EXTENSION = ".dlq";

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
    public static final String KAFKA_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = "kafka.maxInFlightRequestsPerConnection";
    public static final String KAFKA_METADATA_FETCH_TIMEOUT = "kafka.metadataFetchTimeout";
    public static final String KAFKA_METADATA_MAXIMUM_AGE = "kafka.metadataMaxAge";
    public static final String KAFKA_METRIC_REPORTERS = "kafka.metricReporters";
    public static final String KAFKA_METRICS_NUM_SAMPLES = "kafka.metricsNumSamples";
    public static final String KAFKA_METRICS_SAMPLE_WINDOW = "kafka.metricsSampleWindow";
    public static final String KAFKA_RECONNECT_BACKOFF_TIME = "kafka.reconnectBackoff";
    public static final String KAFKA_RECONNECT_BACKOFF_MAX_TIME = "kafka.reconnectBackoffMax";
    public static final String KAFKA_RETRY_BACKOFF_TIME = "kafka.retryBackoff";
    public static final String KAFKA_SASL_KERBEROS_KINIT_CMD = "kafka.saslKerberosKinitCmd";
    public static final String KAFKA_SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN = "kafka.saslKerberosMinTimeBeforeRelogin";
    public static final String KAFKA_SASL_KERBEROS_TICKET_RENEW_JITTER = "kafka.saslKerberosTicketRenewJitter";
    public static final String KAFKA_SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR
            = "kafka.saslKerberosTicketRenewWindowFactor";
    public static final String KAFKA_ENABLE_IDEMPOTENCE = "kafka.enableIdempotence";
    public static final String KAFKA_MESSAGE_SEND_MAX_RETRIES = "kafka.messageSendMaxRetries";
    public static final String KAFKA_SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL = "kafka.saslOauthbearerTokenEndpointUrl";
    public static final String KAFKA_SASL_LOGIN_CALLBACK_HANDLER_CLASS = "kafka.saslLoginCallbackHandlerClass";
    public static final String KAFKA_SASL_OAUTHBEARER_SCOPE_CLAIM_NAME = "kafka.saslOauthbearerScopeClaimName";
    public static final String KAFKA_SASL_LOGIN_CONNECT_TIMEOUT = "kafka.saslLoginConnectTimeout";
    public static final String KAFKA_SASL_LOGIN_READ_TIMEOUT = "kafka.saslLoginReadTimeout";
    public static final String KAFKA_SASL_LOGIN_RETRY_BACKOFF = "kafka.saslLoginRetryBackoff";
    public static final String KAFKA_SASL_LOGIN_RETRY_BACKOFF_MAX = "kafka.saslLoginRetryBackoffMax";
    public static final String KAFKA_SSL_CIPHER_SUITES = "kafka.sslCipherSuites";
    public static final String KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM = "kafka.sslEndpointIdentificationAlgorithm";
    public static final String KAFKA_SSL_KEYMANAGER_ALGORITHM = "kafka.sslKeymanagerAlgorithm";
    public static final String KAFKA_SSL_SECURE_RANDOM_IMPLEMENTATION = "kafka.sslSecureRandomImplementation";
    public static final String KAFKA_SSL_TRUSTMANAGER_ALGORITHM = "kafka.sslTrustmanagerAlgorithm";
    public static final String KAFKA_SEND_CALLBACK_HANDLER = "kafka.sendCallbackHandlerClass";

    // Configuration properties default values
    public static final String DEFAULT_SCHEMA_REGISTRY_URL = "http://localhost:8081";
    public static final String DEFAULT_SEND_LOGGING_CALLBACK_HANDLER_CLASS =
            "org.wso2.carbon.connector.callbackhandler.DefaultLoggingCallbackHandler";

    // Maximum default connection pool size
    public static final String DEFAULT_CONNECTION_POOL_MAX_SIZE = "-1";

    // Whether connection pooling is enabled
    public static final String CONNECTION_POOLING_ENABLED = "kafka.poolingEnabled";

    // Avro message constants
    public static final String KAFKA_AVRO_SERIALIZER = "io.confluent.kafka.serializers.KafkaAvroSerializer";
    public static final String KAFKA_SCHEMA_REGISTRY_URL = "kafka.schemaRegistryUrl";
    public static final String KAFKA_SCHEMA_REGISTRY_BASIC_AUTH_CREDENTIALS_SOURCE = "kafka.basicAuthCredentialsSource";
    public static final String KAFKA_SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO = "kafka.basicAuthUserInfo";
    public static final String KAFKA_USE_LATEST_VERSION = "kafka.useLatestVersion";
    public static final String KAFKA_AUTO_REGISTER_SCHEMAS = "kafka.autoRegisterSchemas";
    public static final String KAFKA_SUBJECT_NAME_STRATEGY = "kafka.subjectNameStrategy";
    public static final String KAFKA_KEY_SUBJECT_NAME_STRATEGY = "kafka.keySubjectNameStrategy";

    // Configuration properties parameter
    public static final String KAFKA_TOPIC = "topic";
    public static final String PARTITION_NO = "partitionNo";
    public static final String KAFKA_KEY = "key";
    public static final String KAFKA_KEY_SCHEMA = "keySchema";
    public static final String KAFKA_KEY_SCHEMA_ID = "keySchemaId";
    public static final String KAFKA_KEY_SCHEMA_VERSION = "keySchemaVersion";
    public static final String KAFKA_KEY_SCHEMA_SUBJECT = "keySchemaSubject";
    public static final String KAFKA_KEY_SCHEMA_METADATA = "keySchemaMetadata";
    public static final String KAFKA_KEY_SCHEMA_SOFT_DELETED = "keySchemaSoftDeleted";

    public static final String KAFKA_VALUE = "value";
    public static final String KAFKA_VALUE_SCHEMA = "valueSchema";
    public static final String KAFKA_VALUE_SCHEMA_ID = "valueSchemaId";
    public static final String KAFKA_VALUE_SCHEMA_VERSION = "valueSchemaVersion";
    public static final String KAFKA_VALUE_SCHEMA_SUBJECT = "valueSchemaSubject";
    public static final String KAFKA_VALUE_SCHEMA_METADATA = "valueSchemaMetadata";
    public static final String KAFKA_VALUE_SCHEMA_SOFT_DELETED = "valueSchemaSoftDeleted";

    // Kafka headers related properties
    public static final String FORWARD_EXISTING_HEADERS = "forwardExistingHeaders";
    public static final String ALL_OPTION = "all";
    public static final String FILTERED_OPTION = "filtered";
    public static final String KAFKA_HEADER_PREFIX = "kafkaHeaderPrefix";
    public static final String REMOVE_HEADER_PREFIX = "removeHeaderPrefix";
    public static final String REMOVE_FILTERED_HEADERS_AFTER_SEND = "removeFilteredAfterSend";
    public static final String DEFAULT_KAFKA_HEADER_PREFIX = "publishMessages:";
    public static final String CUSTOM_HEADERS = "customHeaders";
    public static final String CUSTOM_HEADER_EXPRESSION = "customHeaderExpression";


    public static final TimeZone LOCAL_TZ = TimeZone.getDefault();
    public static final String REGEX_FOR_MILLIS_PART_WITH_TIME_ZONE = "(\\.\\d{1,3})(?=Z|[-+]\\d{2}:?\\d{2}$|$)";
    public static final String REGEX_FOR_MICROS_PART_WITH_TIME_ZONE = "(\\.\\d{1,6})(?=Z|[-+]\\d{2}:?\\d{2}$|$)";
    public static final String REGEX_FOR_MILLIS_PART_WITHOUT_TIME_ZONE = "(\\.\\d{1,3})(?=\\D|$)";
    public static final String REGEX_FOR_MICROS_PART_WITHOUT_TIME_ZONE = "(\\.\\d{1,6})(?=\\D|$)";
    public static final String LOGICAL_TYPE_DECIMAL = "decimal";

}
