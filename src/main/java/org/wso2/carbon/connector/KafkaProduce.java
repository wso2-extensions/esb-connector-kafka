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

import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

/**
 * Produce the messages to the kafka brokers.
 */
public class KafkaProduce extends AbstractConnector {
    public void connect(MessageContext messageContext) throws ConnectException {

        SynapseLog log = getLog(messageContext);
        log.auditLog("SEND : send message to  Broker lists");
        try {
            // Get the maximum pool size
            String maxPoolSize = (String) messageContext.getProperty(KafkaConnectConstants.CONNECTION_POOL_MAX_SIZE);
            // Read the topic from the parameter
            String topic = KafkaUtils.lookupTemplateParameter(messageContext, KafkaConnectConstants.PARAM_TOPIC);
            //Read the key from the parameter
            String key = KafkaUtils.lookupTemplateParameter(messageContext, KafkaConnectConstants.PARAM_KEY);
            String message = this.getMessage(messageContext);
            if (StringUtils.isEmpty(maxPoolSize) || KafkaConnectConstants.DEFAULT_CONNECTION_POOL_MAX_SIZE
                    .equals(maxPoolSize)) {
                //Make the producer connection without connection pool
                sendWithoutPool(messageContext, topic, key, message);
            } else {
                //Make the producer connection with connection pool
                sendWithPool(messageContext, topic, key, message);
            }
        } catch (AxisFault axisFault) {
            handleException("Kafka producer connector : Error sending the message to broker lists", axisFault,
                    messageContext);
        }
    }

    /**
     * Get the messages from the message context and format the messages
     */
    private String getMessage(MessageContext messageContext) throws AxisFault {
        Axis2MessageContext axisMsgContext = (Axis2MessageContext) messageContext;
        org.apache.axis2.context.MessageContext msgContext = axisMsgContext.getAxis2MessageContext();
        return KafkaUtils.formatMessage(msgContext);
    }

    /**
     * Send the messages to the kafka broker with topic and the key that is optional
     */
    private void send(KafkaProducer<String, String> producer, String topic, String key, String message) {
        if (key == null) {
            producer.send(new ProducerRecord<String, String>(topic, message));
            producer.flush();
        } else {
            producer.send(new ProducerRecord<String, String>(topic, key, message));
            producer.flush();
        }
    }

    /**
     * Send the messages with connection pool.
     *
     * @param messageContext the message context
     * @param topic          the topic
     * @param key            the key
     * @param message        the message
     * @throws ConnectException
     */
    private void sendWithPool(MessageContext messageContext, String topic, String key, String message)
            throws ConnectException {
        KafkaConnectionPool connectionPool = KafkaConnectionPool.getInstance(messageContext);
        KafkaProducer<String, String> producer = connectionPool.getConnectionFromPool();
        try {
            if (producer != null) {
                send(producer, topic, key, message);
            } else {
                //If any error in while getting the connection from the pool
                sendWithoutPool(messageContext, topic, key, message);
            }
        } catch (Exception e) {
            handleException("Kafka producer connector:Error sending the message to broker lists with connection Pool",
                    e, messageContext);
        } finally {
            //Close the producer pool connections to all kafka brokers.Also closes the zookeeper client connection if any
            if (producer != null) {
                connectionPool.returnConnectionToPool(producer);
            }
        }
    }

    /**
     * Send the messages without connection pool.
     *
     * @param messageContext the message context
     * @param topic          the topic
     * @param key            the key
     * @param message        the message
     * @throws ConnectException
     */
    private void sendWithoutPool(MessageContext messageContext, String topic, String key, String message)
            throws ConnectException {
        KafkaConnection kafkaConnection = new KafkaConnection();
        KafkaProducer<String, String> producer = kafkaConnection.createNewConnection(messageContext);
        try {
            send(producer, topic, key, message);
        } catch (Exception e) {
            handleException(
                    "Kafka producer connector:Error sending the message to broker lists without connection Pool", e,
                    messageContext);
        } finally {
            //Close the producer pool connections to all kafka brokers.Also closes the zookeeper client connection if any
            if (producer != null) {
                producer.close();
            }
        }
    }
}