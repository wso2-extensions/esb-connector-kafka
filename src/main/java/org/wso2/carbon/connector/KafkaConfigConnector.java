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
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.connector.connection.KafkaConnection;
import org.wso2.carbon.connector.connection.KafkaConnectionFactory;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.connection.ConnectionHandler;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

import static org.wso2.carbon.connector.core.util.ConnectorUtils.getPoolConfiguration;

/**
 * Kafka producer configuration.
 */
public class KafkaConfigConnector extends AbstractConnector implements ManagedLifecycle {

    @Override
    public void connect(MessageContext messageContext) {

        try {
            String ack = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_ACKS);
            String keySerializationClass = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_KEY_SERIALIZER_CLASS);
            String valueSerializationClass = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_VALUE_SERIALIZER_CLASS);
            String schemaRegistryUrl = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_URL);
            String bufferMemory = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_BUFFER_MEMORY);
            String compressionCodec = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_COMPRESSION_TYPE);
            String retries = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_RETRIES);
            String batchSize = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_BATCH_SIZE);
            String clientId = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_CLIENT_ID);
            String connectionMaxIdleTime = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_CONNECTION_MAX_IDLE_TIME);
            String lingerTime = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_LINGER_TIME);
            String maximumBlock = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_MAXIMUM_BLOCK);
            String maximumRequestSize = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_MAXIMUM_REQUEST_SIZE);
            String partitionerClass = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_PARTITIONER_CLASS);
            String receiveBufferBytes = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_RECEIVE_BUFFER_BYTES);
            String requestTimeout = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_REQUEST_TIMEOUT_MS);
            String sendBufferBytes = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_SEND_BUFFER_BYTES);
            String timeoutTime = (String) messageContext
                    .getProperty(KafkaConnectConstants.KAFKA_TIMEOUT_TIME);
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
            if (StringUtils.isEmpty(ack)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_ACKS,
                        KafkaConnectConstants.DEFAULT_ACK);
            }

            if (StringUtils.isEmpty(keySerializationClass)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_KEY_SERIALIZER_CLASS,
                        KafkaConnectConstants.DEFAULT_KEY_SERIALIZER_CLASS);
            }

            if (StringUtils.isEmpty(valueSerializationClass)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_VALUE_SERIALIZER_CLASS,
                        KafkaConnectConstants.DEFAULT_VALUE_SERIALIZER_CLASS);
            }

            if (StringUtils.isEmpty(schemaRegistryUrl)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_URL,
                        KafkaConnectConstants.DEFAULT_SCHEMA_REGISTRY_URL);
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
                messageContext.setProperty(KafkaConnectConstants.KAFKA_RETRIES,
                        KafkaConnectConstants.DEFAULT_RETRIES);
            }

            if (StringUtils.isEmpty(batchSize)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_BATCH_SIZE,
                        KafkaConnectConstants.DEFAULT_BATCH_SIZE);
            }

            if (StringUtils.isEmpty(clientId)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_CLIENT_ID,
                        KafkaConnectConstants.DEFAULT_CLIENT_ID);
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

            if (StringUtils.isEmpty(sendBufferBytes)) {
                messageContext.setProperty(KafkaConnectConstants.KAFKA_SEND_BUFFER_BYTES,
                        KafkaConnectConstants.DEFAULT_SEND_BUFFER_BYTES);
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
            createConnection(messageContext);
        } catch (Exception e) {
            handleException("Kafka producer connector:Error Initializing the kafka broker properties", e,
                    messageContext);
        }
    }

    /**
     * Creates a connection with the given configuration
     *
     * @param messageContext message context
     */
    private void createConnection(MessageContext messageContext) {

        String connectorName = KafkaConnectConstants.CONNECTOR_NAME;
        String connectionName = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                KafkaConnectConstants.NAME);
        String poolingEnabled = (String) messageContext.getProperty(KafkaConnectConstants.CONNECTION_POOLING_ENABLED);
        ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
        if (!handler.checkIfConnectionExists(connectorName, connectionName) && Boolean.parseBoolean(poolingEnabled)) {
            handler.createConnection(connectorName, connectionName, new KafkaConnectionFactory(messageContext),
                    getPoolConfiguration(messageContext));
        } else if (!handler.checkIfConnectionExists(connectorName, connectionName)) {
            KafkaConnection connection = new KafkaConnection(messageContext);
            handler.createConnection(connectorName, connectionName, connection);
        }
    }

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        // Nothing to do when initiating the connector
    }

    @Override
    public void destroy() {

        ConnectionHandler.getConnectionHandler().shutdownConnections(KafkaConnectConstants.CONNECTOR_NAME);
    }
}
