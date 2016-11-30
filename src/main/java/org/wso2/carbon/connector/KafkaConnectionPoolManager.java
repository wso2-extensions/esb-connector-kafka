/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.Properties;
import java.util.Vector;

/**
 * Connection pool manager for Kafka producer connection.
 */
public class KafkaConnectionPoolManager {
    private static Log log = LogFactory.getLog(KafkaConnectionPoolManager.class);

    private static KafkaConnectionPoolManager instance = null;

    Vector<Producer<String, String>> connectionPool = new Vector<Producer<String, String>>();

    public KafkaConnectionPoolManager(MessageContext messageContext) {
        initialize(messageContext);
    }

    /**
     * Get single instance of ConnectionPoolManager.
     *
     * @param messageContext the message context
     * @return the connection pool manger
     */
    public static KafkaConnectionPoolManager getInstance(MessageContext messageContext) {
        if (instance == null) {
            instance = new KafkaConnectionPoolManager(messageContext);
        }
        return instance;
    }

    /**
     * Initialize the connection pool
     *
     * @param messageContext the message context
     */
    private void initialize(MessageContext messageContext) {
        //Here we can initialize all the information that we need
        initializeConnectionPool(messageContext);
    }

    /**
     * Initialize the connection pool.
     *
     * @param messageContext the message context
     */
    private void initializeConnectionPool(MessageContext messageContext) {
        while (!checkIfConnectionPoolIsFull(messageContext)) {
            log.info("Connection Pool is NOT full. Proceeding with adding new connections");
            //Adding new connection instance until the pool is full
            connectionPool.addElement(createNewConnectionForPool(messageContext));
        }
        log.info("Connection Pool is full.");
    }

    /**
     * Check whether the connection pool is full.
     *
     * @return true or false
     */
    private synchronized boolean checkIfConnectionPoolIsFull(MessageContext messageContext) {
        final int MAX_POOL_SIZE = Integer.parseInt(messageContext
                .getProperty(KafkaConnectConstants.CONNECTION_POOL_MAX_SIZE).toString());
        if (log.isDebugEnabled()) {
            log.debug("Maximum pool size is :" + MAX_POOL_SIZE);
        }
        //Check if the pool size
        if (connectionPool.size() < MAX_POOL_SIZE) {
            return false;
        }
        return true;
    }

    /**
     * The ProducerConfig class encapsulates the values required for establishing the connection with brokers such
     * as the broker list, message partition class, serializer class for the message, and partition key,etc.
     *
     * @param messageContext the message context
     * @return the connection
     */
    private Producer<String, String> createNewConnectionForPool(MessageContext messageContext) {
        Axis2MessageContext axis2mc = (Axis2MessageContext) messageContext;
        String brokers = (String) axis2mc.getAxis2MessageContext().getProperty(KafkaConnectConstants.KAFKA_BROKER_LIST);
        String serializationClass = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SERIALIZATION_CLASS);
        String requiredAck = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_REQUIRED_ACK);
        String producerType = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_PRODUCER_TYPE);
        String compressionCodec = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_COMPRESSION_TYPE);
        String keySerializerClass = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SERIALIZATION_CLASS);
        String partitionClass = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_PARTITION_CLASS);
        String compressedTopics = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_COMPRESSED_TOPIC);
        String messageSendMaxRetries = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_MESSAGE_SEND_MAX_RETRIES);
        String retryBackOff = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_TIME_REFRESH_METADATA);
        String refreshInterval = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_TIME_REFRESH_METADATA_AFTER_TOPIC);
        String bufferingMaxMessages = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_BUFFER_MAX_MESSAGES);
        String batchNoMessages = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_NO_MESSAGE_BATCHED_PRODUCER);
        String sendBufferSize = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_BUFFER_SIZE);
        String requestTimeout = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_REQUEST_TIMEOUT);
        String bufferingMaxTime = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_BUFFER_MAX_TIME);
        String enqueueTimeout = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_ENQUEUE_TIMEOUT);
        String clientId = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_CLIENT_ID);

        Properties producerConfigProperties = new Properties();
        producerConfigProperties.put(KafkaConnectConstants.BROKER_LIST, brokers);
        producerConfigProperties.put(KafkaConnectConstants.SERIALIZATION_CLASS, serializationClass);
        producerConfigProperties.put(KafkaConnectConstants.REQUIRED_ACK, requiredAck);
        producerConfigProperties.put(KafkaConnectConstants.PRODUCER_TYPE, producerType);
        producerConfigProperties.put(KafkaConnectConstants.COMPRESSION_TYPE, compressionCodec);
        producerConfigProperties.put(KafkaConnectConstants.KEY_SERIALIZER_CLASS, keySerializerClass);
        producerConfigProperties.put(KafkaConnectConstants.PARTITION_CLASS, partitionClass);
        producerConfigProperties.put(KafkaConnectConstants.COMPRESSED_TOPIC, compressedTopics);
        producerConfigProperties.put(KafkaConnectConstants.MESSAGE_SEND_MAX_RETRIES, messageSendMaxRetries);
        producerConfigProperties.put(KafkaConnectConstants.TIME_REFRESH_METADATA, retryBackOff);
        producerConfigProperties.put(KafkaConnectConstants.TIME_REFRESH_METADATA_AFTER_TOPIC, refreshInterval);
        producerConfigProperties.put(KafkaConnectConstants.BUFFER_MAX_MESSAGES, bufferingMaxMessages);
        producerConfigProperties.put(KafkaConnectConstants.NO_MESSAGE_BATCHED_PRODUCER, batchNoMessages);
        producerConfigProperties.put(KafkaConnectConstants.BUFFER_SIZE, sendBufferSize);
        producerConfigProperties.put(KafkaConnectConstants.REQUEST_TIMEOUT, requestTimeout);
        producerConfigProperties.put(KafkaConnectConstants.BUFFER_MAX_TIME, bufferingMaxTime);
        producerConfigProperties.put(KafkaConnectConstants.ENQUEUE_TIMEOUT, enqueueTimeout);
        producerConfigProperties.put(KafkaConnectConstants.CLIENT_ID, clientId);

        try {
            return new Producer<String, String>(new ProducerConfig(producerConfigProperties));
        } catch (Exception e) {
            throw new SynapseException("The Variable properties or values are not valid");
        }
    }

    /**
     * Get the connection from connection pool.
     *
     * @return the connection
     */
    public synchronized Producer<String, String> getConnectionFromPool() {
        Producer<String, String> connection = null;

        //Check if there is a connection available. There are times when all the connections in the pool may be used up
        if (connectionPool.size() > 0) {
            if (connectionPool.firstElement() != null) {
                connection = connectionPool.firstElement();
            }
            connectionPool.removeElementAt(0);
        }
        //Giving away the connection from the connection pool
        return connection;
    }

    /**
     * Adding the connection from the client back to the connection pool.
     *
     * @param connection the connection
     */
    public synchronized void returnConnectionToPool(Producer<String, String> connection) {
        connectionPool.addElement(connection);
    }
}
