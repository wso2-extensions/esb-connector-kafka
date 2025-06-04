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
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.callbackhandler.DefaultLoggingCallbackHandler;
import org.wso2.carbon.connector.callbackhandler.KafkaSendCallbackHandler;
import org.wso2.carbon.connector.utils.KafkaConnectConstants;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.connection.Connection;
import org.wso2.carbon.connector.core.connection.ConnectionConfig;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.wso2.carbon.connector.utils.KafkaConnectConstants.KAFKA_AVRO_SERIALIZER;

/**
 * The kafka producer connection.
 */
public class KafkaConnection implements Connection {
    private static final Log log = LogFactory.getLog(KafkaConnection.class);
    private KafkaProducer producer;
    private KafkaProducer dlqProducer;
    private CachedSchemaRegistryClient client;
    private Constructor kafkaSendCallbackHandlerClass;

    public KafkaConnection(MessageContext messageContext){

        Properties producerConfigProperties = new Properties();

        // Mandatory properties
        addToProducerConfigProperties(producerConfigProperties, ProducerConfig.BOOTSTRAP_SERVERS_CONFIG ,
                (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                        .getProperty(KafkaConnectConstants.KAFKA_BROKER_LIST), true);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                KafkaConnectConstants.KAFKA_KEY_SERIALIZER_CLASS, true);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                KafkaConnectConstants.KAFKA_VALUE_SERIALIZER_CLASS, true);

        // Avro schema registry related properties
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
                KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_URL, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE,
                KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_BASIC_AUTH_CREDENTIALS_SOURCE, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaAvroSerializerConfig.USER_INFO_CONFIG,
                KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaAvroSerializerConfig.USE_LATEST_VERSION,
                KafkaConnectConstants.KAFKA_USE_LATEST_VERSION, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS,
                KafkaConnectConstants.KAFKA_AUTO_REGISTER_SCHEMAS, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY,
                KafkaConnectConstants.KAFKA_SUBJECT_NAME_STRATEGY, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaAvroSerializerConfig.KEY_SUBJECT_NAME_STRATEGY,
                KafkaConnectConstants.KAFKA_KEY_SUBJECT_NAME_STRATEGY, false);

        // Other advanced producer configurations
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.ACKS_CONFIG,
                KafkaConnectConstants.KAFKA_ACKS, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.BUFFER_MEMORY_CONFIG,
                KafkaConnectConstants.KAFKA_BUFFER_MEMORY, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.COMPRESSION_TYPE_CONFIG,
                KafkaConnectConstants.KAFKA_COMPRESSION_TYPE, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.RETRIES_CONFIG,
                KafkaConnectConstants.KAFKA_RETRIES, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.BATCH_SIZE_CONFIG,
                KafkaConnectConstants.KAFKA_BATCH_SIZE, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.CLIENT_ID_CONFIG,
                KafkaConnectConstants.KAFKA_CLIENT_ID, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG,
                KafkaConnectConstants.KAFKA_CONNECTION_MAX_IDLE_TIME, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.LINGER_MS_CONFIG,
                KafkaConnectConstants.KAFKA_LINGER_TIME, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.MAX_BLOCK_MS_CONFIG,
                KafkaConnectConstants.KAFKA_MAXIMUM_BLOCK, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.MAX_REQUEST_SIZE_CONFIG,
                KafkaConnectConstants.KAFKA_MAXIMUM_REQUEST_SIZE, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.PARTITIONER_CLASS_CONFIG,
                KafkaConnectConstants.KAFKA_PARTITIONER_CLASS, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.RECEIVE_BUFFER_CONFIG,
                KafkaConnectConstants.KAFKA_RECEIVE_BUFFER_BYTES, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                KafkaConnectConstants.KAFKA_REQUEST_TIMEOUT_MS, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.SEND_BUFFER_CONFIG,
                KafkaConnectConstants.KAFKA_SEND_BUFFER_BYTES, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.BLOCK_ON_BUFFER_FULL,
                KafkaConnectConstants.KAFKA_BLOCK_ON_BUFFER_FULL, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                KafkaConnectConstants.KAFKA_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.METADATA_FETCH_TIMEOUT,
                KafkaConnectConstants.KAFKA_METADATA_FETCH_TIMEOUT, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.METADATA_MAX_AGE_CONFIG,
                KafkaConnectConstants.KAFKA_METADATA_MAXIMUM_AGE, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG,
                KafkaConnectConstants.KAFKA_METRIC_REPORTERS, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.METRICS_NUM_SAMPLES_CONFIG,
                KafkaConnectConstants.KAFKA_METRICS_NUM_SAMPLES, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG,
                KafkaConnectConstants.KAFKA_METRICS_SAMPLE_WINDOW, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG,
                KafkaConnectConstants.KAFKA_RECONNECT_BACKOFF_TIME, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG,
                KafkaConnectConstants.KAFKA_RECONNECT_BACKOFF_MAX_TIME, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.RETRY_BACKOFF_MS_CONFIG,
                KafkaConnectConstants.KAFKA_RETRY_BACKOFF_TIME, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                KafkaConnectConstants.KAFKA_ENABLE_IDEMPOTENCE, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.MESSAGE_SEND_MAX_RETRIES,
                KafkaConnectConstants.KAFKA_MESSAGE_SEND_MAX_RETRIES, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.TIMEOUT_TIME,
                KafkaConnectConstants.KAFKA_TIMEOUT_TIME, false);


        // Secure Kafka connection properties
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_PROTOCOL_CONFIG,
                KafkaConnectConstants.KAFKA_SECURITY_PROTOCOL, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_PROVIDER_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_PROVIDER, false);

        // SSL - Keystore related configurations
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_KEY_PASSWORD_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_KEY_PASSWORD, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_KEYSTORE_LOCATION, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_KEYSTORE_PASSWORD, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_KEYSTORE_TYPE_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_KEYSTORE_TYPE, false);

        // SSL - Truststore related configurations
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_TRUSTSTORE_LOCATION, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_TRUSTSTORE_PASSWORD, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_TRUSTSTORE_TYPE, false);

        // Advanced SSL configurations
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_PROTOCOL_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_PROTOCOL, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_CIPHER_SUITES_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_CIPHER_SUITES, false);
        String sslEnabledProtocols = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SSL_ENABLED_PROTOCOLS);
        if (StringUtils.isNotEmpty(sslEnabledProtocols)) {
            String[] sslEnabledProtocolsArray = sslEnabledProtocols.split(",");
            producerConfigProperties.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG,
                    Arrays.asList(sslEnabledProtocolsArray));
        }
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_KEYMANAGER_ALGORITHM, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_SECURE_RANDOM_IMPLEMENTATION, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SslConfigs.SSL_TRUSTMANAGER_ALGORITHM_CONFIG,
                KafkaConnectConstants.KAFKA_SSL_TRUSTMANAGER_ALGORITHM, false);


        // SASL related configurations
        addToProducerConfigProperties(producerConfigProperties, messageContext, SaslConfigs.SASL_MECHANISM,
                KafkaConnectConstants.KAFKA_SASL_MECHANISM, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SaslConfigs.SASL_JAAS_CONFIG,
                KafkaConnectConstants.KAFKA_SASL_JAAS_CONFIG, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SaslConfigs.SASL_KERBEROS_SERVICE_NAME,
                KafkaConnectConstants.KAFKA_SASL_KERBEROS_SERVICE_NAME, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SaslConfigs.SASL_KERBEROS_KINIT_CMD,
                KafkaConnectConstants.KAFKA_SASL_KERBEROS_KINIT_CMD, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SaslConfigs.SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN,
                KafkaConnectConstants.KAFKA_SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SaslConfigs.SASL_KERBEROS_TICKET_RENEW_JITTER,
                KafkaConnectConstants.KAFKA_SASL_KERBEROS_TICKET_RENEW_JITTER, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SaslConfigs.SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR,
                KafkaConnectConstants.KAFKA_SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.SASL_OAUTHBEARER_TOKEN_ENDPOINT,
                KafkaConnectConstants.KAFKA_SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.SASL_OAUTHBEARER_SCOPE_CLAIM_NAME,
                KafkaConnectConstants.KAFKA_SASL_OAUTHBEARER_SCOPE_CLAIM_NAME, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS,
                KafkaConnectConstants.KAFKA_SASL_LOGIN_CALLBACK_HANDLER_CLASS, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.SASL_LOGIN_CONNECT_TIMEOUT,
                KafkaConnectConstants.KAFKA_SASL_LOGIN_CONNECT_TIMEOUT, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.SASL_LOGIN_READ_TIMEOUT,
                KafkaConnectConstants.KAFKA_SASL_LOGIN_READ_TIMEOUT, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.SASL_LOGIN_RETRY_BACKOFF,
                KafkaConnectConstants.KAFKA_SASL_LOGIN_RETRY_BACKOFF, false);
        addToProducerConfigProperties(producerConfigProperties, messageContext, KafkaConnectConstants.SASL_LOGIN_RETRY_BACKOFF_MAX,
                KafkaConnectConstants.KAFKA_SASL_LOGIN_RETRY_BACKOFF_MAX, false);

        if (log.isDebugEnabled()) {
            log.debug("Creating Kafka producer connection with the following Kafka configuration properties");
            log.debug(producerConfigProperties);
        }

        setCachedRegistrySchemaClient(messageContext);

        setKafkaSendCallbackHandlerClass(messageContext, producerConfigProperties);

        try {
            this.producer = new KafkaProducer<>(producerConfigProperties);
        } catch (Exception e) {
            throw new SynapseException("Failed to create the Kafka producer, possibly due to invalid producer"
                    + " configuration", e);
        }
    }

    private void addToProducerConfigProperties(Properties producerConfigProperties, MessageContext messageContext,
                                               String propertyName, String propertyNameInMessageContext,
                                               boolean required) {
        String propertyValue = (String) messageContext.getProperty(propertyNameInMessageContext);
        this.addToProducerConfigProperties(producerConfigProperties, propertyName, propertyValue, required);
    }

    private void addToProducerConfigProperties(Properties producerConfigProperties, String propertyName,
                                               String propertyValue, boolean required) {
        if (StringUtils.isNotEmpty(propertyValue)) {
            producerConfigProperties.put(propertyName, propertyValue);
        } else if (required) {
            throw new SynapseException("Missing mandatory property: " + propertyName + ". Received: " + propertyValue);
        }
    }

    private void setCachedRegistrySchemaClient(MessageContext messageContext) {
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

        if (schemaRegistryUrl != null && (KAFKA_AVRO_SERIALIZER.equals(keySerializationClass)
                || KAFKA_AVRO_SERIALIZER.equals(valueSerializationClass))) {
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

            try {
                RestService service = new RestService(schemaRegistryUrl);
                this.client = new CachedSchemaRegistryClient(service, 1000, headers);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully created the Cached Schema Registry Client!");
                }
            } catch (Exception e) {
                throw new SynapseException("Failed to create the Cached Schema Registry Client.");
            }
        }
    }

    private void setKafkaSendCallbackHandlerClass(MessageContext messageContext, Properties producerConfigProperties) {
        String kafkaSendCallbackHandler = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SEND_CALLBACK_HANDLER);

        kafkaSendCallbackHandler = StringUtils.trim(kafkaSendCallbackHandler);
        if (StringUtils.isNotEmpty(kafkaSendCallbackHandler)) {
            try {
                Class<?> c = Class.forName(kafkaSendCallbackHandler);
                kafkaSendCallbackHandlerClass = c.getConstructor();

                if (!KafkaConnectConstants.DEFAULT_SEND_LOGGING_CALLBACK_HANDLER_CLASS.equals(kafkaSendCallbackHandler)) {
                    this.dlqProducer = new KafkaProducer<>(producerConfigProperties);
                }
            } catch (Exception e) {
                throw new SynapseException("Error creating the KafkaSendCallbackHandler instance", e);
            }
        }
    }

    public KafkaProducer getProducer(){
        return this.producer;
    }

    public KafkaProducer getDLQProducer(){
        return this.dlqProducer;
    }

    public CachedSchemaRegistryClient getRegistryClient(){
        return this.client;
    }

    public KafkaSendCallbackHandler getKafkaSendCallbackHandlerClass() {

        try {
            if (Objects.nonNull(kafkaSendCallbackHandlerClass)) {
                return (KafkaSendCallbackHandler) kafkaSendCallbackHandlerClass.newInstance();
            } else {
                return new DefaultLoggingCallbackHandler();
            }
        } catch (Exception e) {
            throw new SynapseException("Unable to create the Kafka producer callback handler", e);
        }
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
