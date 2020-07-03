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
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.Value;
import org.wso2.carbon.connector.connection.KafkaConnection;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.connection.ConnectionHandler;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Produce the messages to the kafka brokers.
 */
public class KafkaProduceConnector extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        log.auditLog("SEND : send message to  Broker lists");
        try {
            // Read the topic from the parameter
            String topic = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARAM_TOPIC);
            
            //Read the key from parameters
            String key = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARAM_KEY);
            // If key does not exist, generate.
            if (key == null || key.isEmpty() ) {
                key = String.valueOf(UUID.randomUUID());
            }

            //Read the partition No from the parameter
            String partitionNo = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARTITION_NO);
            String message = getMessage(messageContext);
            org.apache.kafka.common.header.Headers headers = getDynamicParameters(messageContext);
            publishMessage(messageContext, topic, key, partitionNo, message, headers);

        } catch (AxisFault axisFault) {
            handleException("Kafka producer connector " +
                    ": Error sending the message to broker lists", axisFault, messageContext);
        }
    }

    /**
     * Read the value from the input parameter
     *
     * @param messageContext Message Context
     * @param paramName      Name of the parameter
     * @return parameter
     */
    private static String lookupTemplateParameter(MessageContext messageContext, String paramName) {

        return (String) ConnectorUtils.lookupTemplateParamater(messageContext, paramName);
    }

    /**
     * Format the messages when the messages are sent to the kafka broker
     *
     * @param messageContext Message Context
     * @return formatted message
     * @throws AxisFault if failed to format
     */
    private static String formatMessage(org.apache.axis2.context.MessageContext messageContext)
            throws AxisFault {

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
     * Initiates a connection and publishes the message
     *
     * @param messageContext Message Context
     * @param topic          Name of the topic
     * @param key            The key
     * @param partitionNo    The partition Number
     * @param message        Message that is sent to the kafka broker
     * @param headers        The kafka headers
     */
    private void publishMessage(MessageContext messageContext, String topic, String key, String partitionNo,
                                String message, Headers headers) {

        // Get connection to kafka producer
        String connectionName = null;
        KafkaConnection connection = null;
        ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
        try {
            connectionName = getConnectionName(messageContext);
            connection = (KafkaConnection) handler
                    .getConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName);
            KafkaProducer producer = connection.getProducer();
            send(producer, topic, partitionNo, key, message, headers, messageContext);
        } catch (Exception e) {
            handleException("Kafka producer connector:" +
                    "Error sending the message to broker lists with connection Pool", e, messageContext);
        } finally {
            // Close the producer connections to all kafka brokers.
            // Also closes the zookeeper client connection if any
            if (connection != null) {
                handler.returnConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName, connection);
            }
        }
    }

    /**
     * Retrieves connection name from message context if configured as configKey attribute
     * or from the template parameter
     *
     * @param messageContext Message Context from which the parameters should be extracted from
     * @return connection name
     */
    private String getConnectionName(MessageContext messageContext) throws InvalidConfigurationException {

        String connectionName = (String) messageContext.getProperty(KafkaConnectConstants.NAME);
        if (connectionName == null) {
            throw new InvalidConfigurationException("Connection name is not set.");
        }
        return connectionName;
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
     * Will generate the dynamic parameters from message context parameter
     *
     * @param messageContext The message contest
     * @return extract the value's from properties and make its as header
     */
    private org.apache.kafka.common.header.Headers getDynamicParameters(MessageContext messageContext) {

        org.apache.kafka.common.header.Headers headers = new RecordHeaders();
        String key = KafkaConnectConstants.METHOD_NAME;
        Map<String, Object> propertiesMap = (((Axis2MessageContext) messageContext).getProperties());
        for (String keyValue : propertiesMap.keySet()) {
            if (keyValue.startsWith(key)) {
                Value propertyValue = (Value) propertiesMap.get(keyValue);
                headers.add(keyValue.substring(key.length(), keyValue.length()), propertyValue.
                        evaluateValue(messageContext).getBytes());
            }
        }
        return headers;
    }

    /**
     * Send the messages to the kafka broker with topic and the key that is optional.
     *
     * @param producer    The instance of the Kafka producer.
     * @param topic       The topic to send the message.
     * @param partitionNo The partition Number of the broker.
     * @param key         The key.
     * @param message     The message that send to kafka broker.
     * @param headers     The kafka headers
     */
    private void send(KafkaProducer<String, String> producer, String topic, String partitionNo, String key,
                      String message, org.apache.kafka.common.header.Headers headers, MessageContext messageContext)
            throws ExecutionException, InterruptedException {

        Integer partitionNumber = null;
        try {
            if (!StringUtils.isEmpty(partitionNo)) {
                partitionNumber = Integer.parseInt(partitionNo);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid Partition Number, hence passing null as the partition number", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending message with the following properties : Topic=" + topic + ", Partition Number=" +
                    partitionNumber + ", Key=" + key + ", Message=" + message + ", Headers=" + headers);
        }

        Future<RecordMetadata> metaData;
        metaData = producer.send(new ProducerRecord<>(topic, partitionNumber, key, message, headers));
        messageContext.setProperty("topic", metaData.get().topic());
        messageContext.setProperty("offset", metaData.get().offset());
        messageContext.setProperty("partition", metaData.get().partition());

        if (log.isDebugEnabled()) {
            log.debug("Flushing producer after sending the message");
        }
        producer.flush();
    }
}
