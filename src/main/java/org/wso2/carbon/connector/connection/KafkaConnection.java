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

package org.wso2.carbon.connector.connection;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.KafkaConnectConstants;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.connection.Connection;
import org.wso2.carbon.connector.core.connection.ConnectionConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.wso2.carbon.connector.KafkaConnectConstants.KAFKA_AVRO_SERIALIZER;

/**
 * The kafka producer connection.
 */
public class KafkaConnection implements Connection {
    private static Log log = LogFactory.getLog(KafkaConnection.class);
    private KafkaProducer producer;
    private CachedSchemaRegistryClient client;

    public KafkaConnection(MessageContext messageContext){
        Axis2MessageContext axis2mc = (Axis2MessageContext) messageContext;
        String brokers = (String) axis2mc.getAxis2MessageContext()
                .getProperty(KafkaConnectConstants.KAFKA_BROKER_LIST);
        String keySerializationClass = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_KEY_SERIALIZER_CLASS);
        String valueSerializationClass = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_VALUE_SERIALIZER_CLASS);
        String basicAuthCredentialsSource = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_BASIC_AUTH_CREDENTIALS_SOURCE);
        String basicAuthUserInfo = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO);
        String schemaRegistryUrl = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_URL);
        String ack = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_ACKS);
        String bufferMemory = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_BUFFER_MEMORY);
        String compressionCodec = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_COMPRESSION_TYPE);
        String retries = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_RETRIES);
        String sslKeyPassword = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_KEY_PASSWORD);
        String sslKeystoreLocation = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_KEYSTORE_LOCATION);
        String sslKeystorePassword = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_KEYSTORE_PASSWORD);
        String sslTruststoreLocation = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_TRUSTSTORE_LOCATION);
        String sslTruststorePassword = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_TRUSTSTORE_PASSWORD);
        String batchSize = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_BATCH_SIZE);
        String clientId = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_CLIENT_ID);
        String connectionMaxIdleTime = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_CONNECTION_MAX_IDLE_TIME);
        String lingerTime = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_LINGER_TIME);
        String maximumBlock = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_MAXIMUM_BLOCK);
        String maximumRequestSize = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_MAXIMUM_REQUEST_SIZE);
        String partitionerClass = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_PARTITIONER_CLASS);
        String receiveBufferBytes = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_RECEIVE_BUFFER_BYTES);
        String requestTimeout = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_REQUEST_TIMEOUT_MS);
        String saslJaasConfig = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_JAAS_CONFIG);
        String saslKerberosServiceName = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_SERVICE_NAME);
        String saslMechanism = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_MECHANISM);
        String securityProtocol = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SECURITY_PROTOCOL);
        String sendBufferBytes = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SEND_BUFFER_BYTES);
        String sslEnabledProtocols = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_ENABLED_PROTOCOLS);
        String sslKeystoreType = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_KEYSTORE_TYPE);
        String sslProtocol = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SSL_PROTOCOL);
        String sslProvider = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SSL_PROVIDER);
        String sslTruststoreType = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_TRUSTSTORE_TYPE);
        String timeoutTime = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_TIMEOUT_TIME);
        String blockOnBufferFull = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_BLOCK_ON_BUFFER_FULL);
        String maxInFlightRequestsPerConnection = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION);
        String metadataFetchTimeout = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_METADATA_FETCH_TIMEOUT);
        String metadataMaximumAge = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_METADATA_MAXIMUM_AGE);
        String metricReporters = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_METRIC_REPORTERS);
        String metricsNumSamples = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_METRICS_NUM_SAMPLES);
        String metricsSampleWindow = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_METRICS_SAMPLE_WINDOW);
        String reconnectBackoffTime = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_RECONNECT_BACKOFF_TIME);
        String retryBackoffTime = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_RETRY_BACKOFF_TIME);
        String reconnectBackoffTimeMax = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_RECONNECT_BACKOFF_MAX_TIME);
        String saslKerberosKinitCmd = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_KINIT_CMD);
        String saslKerberosMinTimeBeforeLogin = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN);
        String saslTicketRenewJitter = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_TICKET_RENEW_JITTER);
        String saslKerberosTicketRenewWindowFactor = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR);
        String sslCipherSuites = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_CIPHER_SUITES);
        String sslEndpointIdentificationAlgorithm = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM);
        String sslKeymanagerAlgorithm = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_KEYMANAGER_ALGORITHM);
        String sslSecureRandomImplementation = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_SECURE_RANDOM_IMPLEMENTATION);
        String sslTrustmanagerAlgorithm = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_TRUSTMANAGER_ALGORITHM);
        String saslOauthbearerTokenEndpointUrl = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL);
        String saslLoginCallbackHandlerClass = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_LOGIN_CALLBACK_HANDLER_CLASS);
        String saslOauthbearerScopeClaimName = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_OAUTHBEARER_SCOPE_CLAIM_NAME);
        String saslLoginConnectTimeout = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_LOGIN_CONNECT_TIMEOUT);
        String saslLoginReadTimeout = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_LOGIN_READ_TIMEOUT);
        String saslLoginRetryBackoff = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_LOGIN_RETRY_BACKOFF);
        String saslLoginRetryBackoffMax = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SASL_LOGIN_RETRY_BACKOFF_MAX);
        String enableIdempotence = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_ENABLE_IDEMPOTENCE);
        String messageSendMaxRetries = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_MESSAGE_SEND_MAX_RETRIES);
        String subjectNameStrategy = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SUBJECT_NAME_STRATEGY);
        String useLatestVersion = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_USE_LATEST_VERSION);
        String autoRegisterSchemas = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_AUTO_REGISTER_SCHEMAS);

        Properties producerConfigProperties = new Properties();
        producerConfigProperties.put(KafkaConnectConstants.BROKER_LIST, brokers);
        producerConfigProperties.put(KafkaConnectConstants.KEY_SERIALIZER_CLASS, keySerializationClass);
        producerConfigProperties.put(KafkaConnectConstants.VALUE_SERIALIZER_CLASS, valueSerializationClass);
        producerConfigProperties.put(KafkaConnectConstants.ACK, ack);
        producerConfigProperties.put(KafkaConnectConstants.BUFFER_MEMORY, bufferMemory);
        producerConfigProperties.put(KafkaConnectConstants.COMPRESSION_TYPE, compressionCodec);
        producerConfigProperties.put(KafkaConnectConstants.RETRIES, retries);

        if (StringUtils.isNotEmpty(enableIdempotence)) {
            producerConfigProperties.put(KafkaConnectConstants.ENABLE_IDEMPOTENCE, enableIdempotence);
        }
        if (StringUtils.isNotEmpty(messageSendMaxRetries)) {
            producerConfigProperties.put(KafkaConnectConstants.MESSAGE_SEND_MAX_RETRIES, messageSendMaxRetries);
        }
        if (StringUtils.isNotEmpty(saslOauthbearerTokenEndpointUrl)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_OAUTHBEARER_TOKEN_ENDPOINT, saslOauthbearerTokenEndpointUrl);
        }
        if (StringUtils.isNotEmpty(saslOauthbearerScopeClaimName)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_OAUTHBEARER_SCOPE_CLAIM_NAME, saslOauthbearerScopeClaimName);
        }
        if (StringUtils.isNotEmpty(saslLoginConnectTimeout)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_LOGIN_CONNECT_TIMEOUT, saslLoginConnectTimeout);
        }
        if (StringUtils.isNotEmpty(saslLoginReadTimeout)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_LOGIN_READ_TIMEOUT, saslLoginReadTimeout);
        }
        if (StringUtils.isNotEmpty(saslLoginRetryBackoff)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_LOGIN_RETRY_BACKOFF, saslLoginRetryBackoff);
        }
        if (StringUtils.isNotEmpty(saslLoginRetryBackoffMax)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_LOGIN_RETRY_BACKOFF_MAX, saslLoginRetryBackoffMax);
        }
        if (StringUtils.isNotEmpty(saslLoginCallbackHandlerClass)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_LOGIN_CALLBACK_HANDLER_CLASS, saslLoginCallbackHandlerClass);
        }
        if (StringUtils.isNotEmpty(basicAuthCredentialsSource)) {
            producerConfigProperties.put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, basicAuthCredentialsSource);
        }
        if (StringUtils.isNotEmpty(basicAuthUserInfo)) {
            producerConfigProperties.put(KafkaAvroSerializerConfig.USER_INFO_CONFIG, basicAuthUserInfo);
        }
        if (StringUtils.isNotEmpty(schemaRegistryUrl)) {
            producerConfigProperties.put(KafkaConnectConstants.SCHEMA_REGISTRY_URL, schemaRegistryUrl);
        }
        if (StringUtils.isNotEmpty(sslKeyPassword)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_KEY_PASSWORD, sslKeyPassword);
        }

        if (StringUtils.isNotEmpty(sslKeystoreLocation)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_KEYSTORE_LOCATION, sslKeystoreLocation);
        }

        if (StringUtils.isNotEmpty(sslKeystorePassword)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_KEYSTORE_PASSWORD, sslKeystorePassword);
        }

        if (StringUtils.isNotEmpty(sslTruststoreLocation)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_TRUSTSTORE_LOCATION, sslTruststoreLocation);
        }

        if (StringUtils.isNotEmpty(sslTruststorePassword)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_TRUSTSTORE_PASSWORD, sslTruststorePassword);
        }

        producerConfigProperties.put(KafkaConnectConstants.BATCH_SIZE, batchSize);
        producerConfigProperties.put(KafkaConnectConstants.CLIENT_ID, clientId);
        producerConfigProperties.put(KafkaConnectConstants.CONNECTION_MAX_IDLE_TIME, connectionMaxIdleTime);
        producerConfigProperties.put(KafkaConnectConstants.LINGER_TIME, lingerTime);
        producerConfigProperties.put(KafkaConnectConstants.MAXIMUM_BLOCK, maximumBlock);
        producerConfigProperties.put(KafkaConnectConstants.MAXIMUM_REQUEST_SIZE, maximumRequestSize);
        producerConfigProperties.put(KafkaConnectConstants.PARTITIONER_CLASS, partitionerClass);
        producerConfigProperties.put(KafkaConnectConstants.RECEIVE_BUFFER_BYTES, receiveBufferBytes);
        producerConfigProperties.put(KafkaConnectConstants.REQUEST_TIMEOUT_MS, requestTimeout);

        if (StringUtils.isNotEmpty(saslJaasConfig)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_JAAS_CONFIG, saslJaasConfig);
        }

        if (StringUtils.isNotEmpty(saslKerberosServiceName)) {
            producerConfigProperties
                    .put(KafkaConnectConstants.SASL_KERBEROS_SERVICE_NAME, saslKerberosServiceName);
        }

        if (StringUtils.isNotEmpty(saslMechanism)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_MECHANISM, saslMechanism);
        }

        if (StringUtils.isNotEmpty(securityProtocol)) {
            producerConfigProperties.put(KafkaConnectConstants.SECURITY_PROTOCOL, securityProtocol);
        }

        producerConfigProperties.put(KafkaConnectConstants.SEND_BUFFER_BYTES, sendBufferBytes);

        if (StringUtils.isNotEmpty(sslEnabledProtocols)) {
            String[] sslEnabledProtocolsArray = sslEnabledProtocols.split(",");
            producerConfigProperties
                    .put(KafkaConnectConstants.SSL_ENABLED_PROTOCOLS, Arrays.asList(sslEnabledProtocolsArray));
        }

        if (StringUtils.isNotEmpty(sslKeystoreType)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_KEYSTORE_TYPE, sslKeystoreType);
        }

        if (StringUtils.isNotEmpty(sslProtocol)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_PROTOCOL, sslProtocol);
        }

        if (StringUtils.isNotEmpty(sslProvider)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_PROVIDER, sslProvider);
        }

        if (StringUtils.isNotEmpty(sslTruststoreType)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_TRUSTSTORE_TYPE, sslTruststoreType);
        }

        if (StringUtils.isNotEmpty(reconnectBackoffTimeMax)) {
            producerConfigProperties.put(KafkaConnectConstants.RECONNECT_BACKOFF_MAX_MS, reconnectBackoffTimeMax);
        }

        producerConfigProperties.put(KafkaConnectConstants.TIMEOUT_TIME, timeoutTime);
        producerConfigProperties.put(KafkaConnectConstants.BLOCK_ON_BUFFER_FULL, blockOnBufferFull);
        producerConfigProperties
                .put(KafkaConnectConstants
                        .MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequestsPerConnection);
        producerConfigProperties.put(KafkaConnectConstants.METADATA_FETCH_TIMEOUT, metadataFetchTimeout);
        producerConfigProperties.put(KafkaConnectConstants.METADATA_MAXIMUM_AGE, metadataMaximumAge);
        producerConfigProperties.put(KafkaConnectConstants.METRIC_REPORTERS, metricReporters);
        producerConfigProperties.put(KafkaConnectConstants.METRICS_NUM_SAMPLES, metricsNumSamples);
        producerConfigProperties.put(KafkaConnectConstants.METRICS_SAMPLE_WINDOW, metricsSampleWindow);
        producerConfigProperties.put(KafkaConnectConstants.RECONNECT_BACKOFF_TIME, reconnectBackoffTime);
        producerConfigProperties.put(KafkaConnectConstants.RETRY_BACKOFF_TIME, retryBackoffTime);

        if (StringUtils.isNotEmpty(saslKerberosKinitCmd)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_KERBEROS_KINIT_CMD, saslKerberosKinitCmd);
        }

        if (StringUtils.isNotEmpty(saslKerberosMinTimeBeforeLogin)) {
            producerConfigProperties
                    .put(KafkaConnectConstants
                            .SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN, saslKerberosMinTimeBeforeLogin);
        }

        if (StringUtils.isNotEmpty(saslTicketRenewJitter)) {
            producerConfigProperties
                    .put(KafkaConnectConstants.SASL_KERBEROS_TICKET_RENEW_JITTER, saslTicketRenewJitter);
        }

        if (StringUtils.isNotEmpty(saslKerberosTicketRenewWindowFactor)) {
            producerConfigProperties.put(KafkaConnectConstants.SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR,
                    saslKerberosTicketRenewWindowFactor);
        }

        if (StringUtils.isNotEmpty(sslCipherSuites)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_CIPHER_SUITES, sslCipherSuites);
        }

        if (StringUtils.isNotEmpty(sslEndpointIdentificationAlgorithm)) {
            producerConfigProperties.put(KafkaConnectConstants.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,
                    sslEndpointIdentificationAlgorithm);
        }

        if (StringUtils.isNotEmpty(sslKeymanagerAlgorithm)) {
            producerConfigProperties.put(KafkaConnectConstants
                    .SSL_KEYMANAGER_ALGORITHM, sslKeymanagerAlgorithm);
        }

        if (StringUtils.isNotEmpty(sslSecureRandomImplementation)) {
            producerConfigProperties
                    .put(KafkaConnectConstants.SSL_SECURE_RANDOM_IMPLEMENTATION, sslSecureRandomImplementation);
        }

        if (StringUtils.isNotEmpty(sslTrustmanagerAlgorithm)) {
            producerConfigProperties.put(KafkaConnectConstants
                    .SSL_TRUSTMANAGER_ALGORITHM, sslTrustmanagerAlgorithm);
        }

        if (StringUtils.isNotEmpty(subjectNameStrategy)) {
            producerConfigProperties.put(KafkaConnectConstants.SUBJECT_NAME_STRATEGY, subjectNameStrategy);
        }

        if (StringUtils.isNotEmpty(useLatestVersion)) {
            producerConfigProperties.put(KafkaConnectConstants.USE_LATEST_VERSION, useLatestVersion);
        }

        if (StringUtils.isNotEmpty(autoRegisterSchemas)) {
            producerConfigProperties.put(KafkaConnectConstants.AUTO_REGISTER_SCHEMAS, autoRegisterSchemas);
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating Kafka producer connection with the following Kafka configuration properties");
            log.debug(producerConfigProperties);
        }

        try {
            if ((KAFKA_AVRO_SERIALIZER.equals(keySerializationClass) || KAFKA_AVRO_SERIALIZER.equals(valueSerializationClass)) && schemaRegistryUrl != null) {
                Map<String, String> headers = null;
                if (basicAuthCredentialsSource != null || basicAuthUserInfo != null) {
                    headers = new HashMap<>();
                    if (basicAuthCredentialsSource != null) {
                        headers.put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, basicAuthCredentialsSource);
                    }
                    if (basicAuthUserInfo != null) {
                        headers.put(KafkaAvroSerializerConfig.USER_INFO_CONFIG, basicAuthUserInfo);
                    }
                }
                RestService service = new RestService(schemaRegistryUrl);

                this.client = new CachedSchemaRegistryClient(service, 1000, headers);
            }

            this.producer = new KafkaProducer<>(producerConfigProperties);
        } catch (Exception e) {
            log.error("Error creating Kafka producer with Kafka configuration properties", e);
            throw new SynapseException("The Variable properties or values are not valid", e);
        }
    }

    public KafkaProducer getProducer(){
        return this.producer;
    }

    public CachedSchemaRegistryClient getRegistryClient(){
        return this.client;
    }

    void disconnect() {
        // Close the producer pool connections to all kafka brokers.
        // Also closes the zookeeper client connection if any
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    public void connect(ConnectionConfig connectionConfig) throws ConnectException {
        //no requirement to implement for now
    }

    @Override
    public void close() throws ConnectException {
        //no requirement to implement for now
    }
}
