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

import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

/**
 * Kafka producer configuration.
 */
public class KafkaConfigConnector extends AbstractConnector {

    public void connect(MessageContext messageContext) throws ConnectException {
        try {
            String ack = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_ACKS);
            String keySerializationClass = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_KEY_SERIALIZER_CLASS);
            String valueSerializationClass = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_VALUE_SERIALIZER_CLASS);
            String bufferMemory = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_BUFFER_MEMORY);
            String compressionCodec = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_COMPRESSION_TYPE);
            String retries = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_RETRIES);
            String sslKeyPassword = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SSL_KEY_PASSWORD);
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
            String requestTimeout = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_REQUEST_TIMEOUT_MS);
            String saslJaasConfig = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SASL_JAAS_CONFIG);
            String saslKerberosServiceName = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_SERVICE_NAME);
            String securityProtocol = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SECURITY_PROTOCOL);
            String sendBufferBytes = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SEND_BUFFER_BYTES);
            String sslEnabledProtocols = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SSL_ENABLED_PROTOCOLS);
            String sslKeystoreType = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SSL_KEYSTORE_TYPE);
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
            String metricReporters = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_METRIC_REPORTERS);
            String metricsNumSamples = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_METRICS_NUM_SAMPLES);
            String metricsSampleWindow = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_METRICS_SAMPLE_WINDOW);
            String reconnectBackoffTime = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_RECONNECT_BACKOFF_TIME);
            String retryBackoffTime = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_RETRY_BACKOFF_TIME);
            String saslKerberosKinitCmd = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_KINIT_CMD);
            String saslKerberosMinTimeBeforeLogin = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN);
            String saslTicketRenewJitter = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_TICKET_RENEW_JITTER);
            String saslKerberosTicketRenewWindowFactor = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR);
            String sslCipherSuites = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SSL_CIPHER_SUITES);
            String sslEndpointIdentificationAlgorithm = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM);
            String sslKeymanagerAlgorithm = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SSL_KEYMANAGER_ALGORITHM);
            String sslSecureRandomImplementation = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SSL_SECURE_RANDOM_IMPLEMENTATION);
            String sslTrustmanagerAlgorithm = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SSL_TRUSTMANAGER_ALGORITHM);

            if (StringUtils.isEmpty(ack)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_ACKS, KafkaConnectConstants.DEFAULT_ACK);
            }

            if (StringUtils.isEmpty(keySerializationClass)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_KEY_SERIALIZER_CLASS,
                        KafkaConnectConstants.DEFAULT_KEY_SERIALIZER_CLASS);
            }

            if (StringUtils.isEmpty(valueSerializationClass)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_VALUE_SERIALIZER_CLASS,
                        KafkaConnectConstants.DEFAULT_VALUE_SERIALIZER_CLASS);
            }

            if (StringUtils.isEmpty(bufferMemory)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_BUFFER_MEMORY,
                        KafkaConnectConstants.DEFAULT_BUFFER_MEMORY);
            }

            if (StringUtils.isEmpty(compressionCodec)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_COMPRESSION_TYPE,
                        KafkaConnectConstants.DEFAULT_COMPRESSION_TYPE);
            }

            if (StringUtils.isEmpty(retries)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_RETRIES, KafkaConnectConstants.DEFAULT_RETRIES);
            }

            if (StringUtils.isEmpty(sslKeyPassword)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_KEY_PASSWORD,
                        KafkaConnectConstants.DEFAULT_SSL_KEY_PASSWORD);
            }

            if (StringUtils.isEmpty(sslKeystoreLocation)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_KEYSTORE_LOCATION,
                        KafkaConnectConstants.DEFAULT_SSL_KEYSTORE_LOCATION);
            }

            if (StringUtils.isEmpty(sslKeystorePassword)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_KEYSTORE_PASSWORD,
                        KafkaConnectConstants.DEFAULT_SSL_KEYSTORE_PASSWORD);
            }

            if (StringUtils.isEmpty(sslTruststoreLocation)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_TRUSTSTORE_LOCATION,
                        KafkaConnectConstants.DEFAULT_SSL_TRUSTSTORE_LOCATION);
            }

            if (StringUtils.isEmpty(sslTruststorePassword)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_TRUSTSTORE_PASSWORD,
                        KafkaConnectConstants.DEFAULT_SSL_TRUSTSTORE_PASSWORD);
            }

            if (StringUtils.isEmpty(batchSize)) {
                messageContext
                        .setProperty(KafkaConnectConstants.KAFKA_BATCH_SIZE, KafkaConnectConstants.DEFAULT_BATCH_SIZE);
            }

            if (StringUtils.isEmpty(clientId)) {
                messageContext
                        .setProperty(KafkaConnectConstants.KAFKA_CLIENT_ID, KafkaConnectConstants.DEFAULT_CLIENT_ID);
            }

            if (StringUtils.isEmpty(connectionMaxIdleTime)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_CONNECTION_MAX_IDLE_TIME,
                        KafkaConnectConstants.DEFAULT_CONNECTION_MAX_IDLE_TIME);
            }

            if (StringUtils.isEmpty(lingerTime)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_LINGER_TIME,
                        KafkaConnectConstants.DEFAULT_LINGER_TIME);
            }

            if (StringUtils.isEmpty(maximumBlock)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_MAXIMUM_BLOCK,
                        KafkaConnectConstants.DEFAULT_MAXIMUM_BLOCK);
            }

            if (StringUtils.isEmpty(maximumRequestSize)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_MAXIMUM_REQUEST_SIZE,
                        KafkaConnectConstants.DEFAULT_MAXIMUM_REQUEST_SIZE);
            }

            if (StringUtils.isEmpty(partitionerClass)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_PARTITIONER_CLASS,
                        KafkaConnectConstants.DEFAULT_PARTITIONER_CLASS);
            }

            if (StringUtils.isEmpty(receiveBufferBytes)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_RECEIVE_BUFFER_BYTES,
                        KafkaConnectConstants.DEFAULT_RECEIVE_BUFFER_BYTES);
            }

            if (StringUtils.isEmpty(requestTimeout)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_REQUEST_TIMEOUT_MS,
                        KafkaConnectConstants.DEFAULT_REQUEST_TIMEOUT_MS);
            }

            if (StringUtils.isEmpty(saslJaasConfig)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SASL_JAAS_CONFIG,
                        KafkaConnectConstants.DEFAULT_SASL_JAAS_CONFIG);
            }

            if (StringUtils.isEmpty(saslKerberosServiceName)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_SERVICE_NAME,
                        KafkaConnectConstants.DEFAULT_SASL_KERBEROS_SERVICE_NAME);
            }

            if (StringUtils.isEmpty(securityProtocol)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SECURITY_PROTOCOL,
                        KafkaConnectConstants.DEFAULT_SECURITY_PROTOCOL);
            }

            if (StringUtils.isEmpty(sendBufferBytes)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SEND_BUFFER_BYTES,
                        KafkaConnectConstants.DEFAULT_SEND_BUFFER_BYTES);
            }

            if (StringUtils.isEmpty(sslEnabledProtocols)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_ENABLED_PROTOCOLS,
                        KafkaConnectConstants.DEFAULT_SSL_ENABLED_PROTOCOLS);
            }

            if (StringUtils.isEmpty(sslKeystoreType)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_KEYSTORE_TYPE,
                        KafkaConnectConstants.DEFAULT_SSL_KEYSTORE_TYPE);
            }

            if (StringUtils.isEmpty(sslProtocol)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_PROTOCOL,
                        KafkaConnectConstants.DEFAULT_SSL_PROTOCOL);
            }

            if (StringUtils.isEmpty(sslProvider)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_PROVIDER,
                        KafkaConnectConstants.DEFAULT_SSL_PROVIDER);
            }

            if (StringUtils.isEmpty(sslTruststoreType)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_TRUSTSTORE_TYPE,
                        KafkaConnectConstants.DEFAULT_SSL_TRUSTSTORE_TYPE);
            }

            if (StringUtils.isEmpty(timeoutTime)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_TIMEOUT_TIME,
                        KafkaConnectConstants.DEFAULT_TIMEOUT_TIME);
            }

            if (StringUtils.isEmpty(blockOnBufferFull)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_BLOCK_ON_BUFFER_FULL,
                        KafkaConnectConstants.DEFAULT_BLOCK_ON_BUFFER_FULL);
            }

            if (StringUtils.isEmpty(maxInFlightRequestsPerConnection)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                        KafkaConnectConstants.DEFAULT_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION);
            }

            if (StringUtils.isEmpty(metadataFetchTimeout)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_METADATA_FETCH_TIMEOUT,
                        KafkaConnectConstants.DEFAULT_METADATA_FETCH_TIMEOUT);
            }

            if (StringUtils.isEmpty(metadataMaximumAge)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_METADATA_MAXIMUM_AGE,
                        KafkaConnectConstants.DEFAULT_METADATA_MAXIMUM_AGE);
            }

            if (StringUtils.isEmpty(metricReporters)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_METRIC_REPORTERS,
                        KafkaConnectConstants.DEFAULT_METRIC_REPORTERS);
            }

            if (StringUtils.isEmpty(metricsNumSamples)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_METRICS_NUM_SAMPLES,
                        KafkaConnectConstants.DEFAULT_METRICS_NUM_SAMPLES);
            }

            if (StringUtils.isEmpty(metricsSampleWindow)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_METRICS_SAMPLE_WINDOW,
                        KafkaConnectConstants.DEFAULT_METRICS_SAMPLE_WINDOW);
            }

            if (StringUtils.isEmpty(reconnectBackoffTime)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_RECONNECT_BACKOFF_TIME,
                        KafkaConnectConstants.DEFAULT_RECONNECT_BACKOFF_TIME);
            }

            if (StringUtils.isEmpty(retryBackoffTime)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_RETRY_BACKOFF_TIME,
                        KafkaConnectConstants.DEFAULT_RETRY_BACKOFF_TIME);
            }

            if (StringUtils.isEmpty(saslKerberosKinitCmd)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_KINIT_CMD,
                        KafkaConnectConstants.DEFAULT_SASL_KERBEROS_KINIT_CMD);
            }

            if (StringUtils.isEmpty(saslKerberosMinTimeBeforeLogin)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN,
                        KafkaConnectConstants.DEFAULT_SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN);
            }

            if (StringUtils.isEmpty(saslTicketRenewJitter)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_TICKET_RENEW_JITTER,
                        KafkaConnectConstants.DEFAULT_SASL_KERBEROS_TICKET_RENEW_JITTER);
            }

            if (StringUtils.isEmpty(saslKerberosTicketRenewWindowFactor)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR,
                        KafkaConnectConstants.DEFAULT_SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR);
            }

            if (StringUtils.isEmpty(sslCipherSuites)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_CIPHER_SUITES,
                        KafkaConnectConstants.DEFAULT_SSL_CIPHER_SUITES);
            }

            if (StringUtils.isEmpty(sslEndpointIdentificationAlgorithm)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,
                        KafkaConnectConstants.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM);
            }

            if (StringUtils.isEmpty(sslKeymanagerAlgorithm)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_KEYMANAGER_ALGORITHM,
                        KafkaConnectConstants.DEFAULT_SSL_KEYMANAGER_ALGORITHM);
            }

            if (StringUtils.isEmpty(sslSecureRandomImplementation)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_SECURE_RANDOM_IMPLEMENTATION,
                        KafkaConnectConstants.DEFAULT_SSL_SECURE_RANDOM_IMPLEMENTATION);
            }

            if (StringUtils.isEmpty(sslTrustmanagerAlgorithm)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SSL_TRUSTMANAGER_ALGORITHM,
                        KafkaConnectConstants.DEFAULT_SSL_TRUSTMANAGER_ALGORITHM);
            }

        } catch (Exception e) {
            handleException("Kafka producer connector:Error Initializing the kafka broker properties", e,
                    messageContext);
        }
    }
}