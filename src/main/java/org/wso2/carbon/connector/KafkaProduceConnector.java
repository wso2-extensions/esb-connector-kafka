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

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

/**
 * Produce the messages to the kafka brokers.
 */
public class KafkaProduceConnector extends AbstractConnector {

    private KafkaConnectionPool connectionPool;

    public void connect(MessageContext messageContext) throws ConnectException {

        SynapseLog log = getLog(messageContext);
        log.auditLog("SEND : send message to  Broker lists");
        try {
            // Get the maximum pool size
            String maxPoolSize = (String) messageContext.getProperty(KafkaConnectConstants.CONNECTION_POOL_MAX_SIZE);
            // Read the topic from the parameter
            String topic = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARAM_TOPIC);
            //Read the key from the parameter
            String key = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARAM_KEY);
            //Read the partition No from the parameter
            String partitionNo = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARTITION_NO);
            String message = getMessage(messageContext);
            if (StringUtils.isEmpty(maxPoolSize) || KafkaConnectConstants.DEFAULT_CONNECTION_POOL_MAX_SIZE
                    .equals(maxPoolSize)) {
                //Make the producer connection without connection pool
                sendWithoutPool(messageContext, topic, partitionNo, key, message);
            } else {
                //Make the producer connection with connection pool
                sendWithPool(messageContext, topic, partitionNo, key, message);
            }
        } catch (AxisFault axisFault) {
            handleException("Kafka producer connector : Error sending the message to broker lists", axisFault,
                    messageContext);
        }
    }

    /**
     * Get the messages from the message context and format the messages.
     */
    private String getMessage(MessageContext messageContext) throws AxisFault {
        Axis2MessageContext axisMsgContext = (Axis2MessageContext) messageContext;
        org.apache.axis2.context.MessageContext msgContext = axisMsgContext.getAxis2MessageContext();
        return formatMessage(msgContext);
    }

    /**
     * Format the messages when the messages are sent to the kafka broker
     */
    private static String formatMessage(org.apache.axis2.context.MessageContext messageContext) throws AxisFault {
        OMOutputFormat format = BaseUtils.getOMOutputFormat(messageContext);
        MessageFormatter messageFormatter = MessageProcessorSelector.getMessageFormatter(messageContext);
        StringWriter stringWriter = new StringWriter();
        OutputStream out = new WriterOutputStream(stringWriter, format.getCharSetEncoding());
        try {
            messageFormatter.writeTo(messageContext, format, out, true);
        } catch (IOException e) {
            throw new AxisFault("The Error occurs while formatting the message", e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                throw new AxisFault("The Error occurs while closing the output stream", e);
            }
        }

        return stringWriter.toString();
    }

    /**
     * Send the messages to the kafka broker with topic and the key that is optional.
     *
     * @param producer    The instance of the Kafka producer.
     * @param topic       The topic to send the message.
     * @param partitionNo The partition Number of the broker.
     * @param key         The key.
     * @param message     The message that send to kafka broker.
     */
    private void send(KafkaProducer<String, String> producer, String topic, String partitionNo, String key,
            String message) {
        if (key == null && partitionNo == null) {
            producer.send(new ProducerRecord<String, String>(topic, message));
            producer.flush();
        } else {
            producer.send(new ProducerRecord<>(topic, Integer.parseInt(partitionNo), key, message));
            producer.flush();
        }
    }

    /**
     * Send the messages with connection pool.
     *
     * @param messageContext The message context.
     * @param topic          The topic.
     * @param partitionNo    The partition Number of the broker.
     * @param key            The key.
     * @param message        The message.
     * @throws ConnectException The Exception while create the connection from the Connection pool.
     */
    private void sendWithPool(MessageContext messageContext, String topic, String partitionNo, String key,
            String message) throws ConnectException {

        if (connectionPool == null) {
            synchronized (KafkaConnectionPool.class) {
                if (connectionPool == null) {
                    connectionPool = new KafkaConnectionPool(messageContext);
                }
            }
        }
        KafkaProducer<String, String> producer = connectionPool.getConnectionFromPool();
        try {
            if (producer != null) {
                send(producer, topic, partitionNo, key, message);
            } else {
                //If any error occurs while getting the connection from the pool.
                sendWithoutPool(messageContext, topic, partitionNo, key, message);
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
     * @param messageContext The message context.
     * @param topic          The topic.
     * @param partitionNo    The partition number of the broker.
     * @param key            The key.
     * @param message        The message.
     * @throws ConnectException The Exception while create the Kafka Connection.
     */
    private void sendWithoutPool(MessageContext messageContext, String topic, String partitionNo, String key,
            String message) throws ConnectException {
        KafkaConnection kafkaConnection = new KafkaConnection();
        KafkaProducer<String, String> producer = kafkaConnection.createNewConnection(messageContext);
        try {
            send(producer, topic, partitionNo, key, message);
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

    /**
     * Read the value from the input parameter
     */
    private static String lookupTemplateParameter(MessageContext messageContext, String paramName) {
        return (String) ConnectorUtils.lookupTemplateParamater(messageContext, paramName);
    }
}