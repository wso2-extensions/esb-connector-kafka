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

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.Properties;

/**
 * The kafka producer connection.
 */
public class KafkaConnection {
    /**
     * Create new connection with kafka broker.
     *
     * @param messageContext the message context
     * @return the producer
     */
    public Producer<String, String> createNewConnection(MessageContext messageContext) {
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
            throw new SynapseException("The Variable properties or values are not valid", e);
        }
    }
}
