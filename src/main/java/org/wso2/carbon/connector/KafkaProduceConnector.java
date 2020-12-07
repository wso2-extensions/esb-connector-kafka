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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.util.Utf8;
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

import java.io.ByteArrayOutputStream;
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

    private static final DecoderFactory decoderFactory = DecoderFactory.get();
    private Object key = null;
    private Object message = null;

    @Override
    public void connect(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        log.auditLog("SEND : send message to  Broker lists");
        try {
            // Read the topic from the parameter
            String topic = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARAM_TOPIC);

            //Read the key from parameters
            Object key = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARAM_KEY);

            Object message = lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_VALUE);

            if (message == null) {
                message = getMessage(messageContext);
            }

            String keySerializerClass = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_KEY_SERIALIZER_CLASS);
            String valueSerializerClass = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_VALUE_SERIALIZER_CLASS);
            // Read schema registry url the parameter
            String schemaRegistryUrl = (String) messageContext.getProperty(KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_URL);

            Schema keySchema = null;
            Schema valueSchema = null;
            String valuePayload = null;
            if (keySerializerClass.equalsIgnoreCase("io.confluent.kafka.serializers.KafkaAvroSerializer")) {
                // Read keySchemaId, keySchema and key from the parameters
                String keySchemaId = lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_KEY_SCHEMA_ID);
                String keySchemaString = lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_KEY_SCHEMA);

                keySchema = parseSchema(schemaRegistryUrl, keySchemaId, keySchemaString, messageContext);
            }
            if (valueSerializerClass.equalsIgnoreCase("io.confluent.kafka.serializers.KafkaAvroSerializer")) {
                // Read valueSchemaId, valueSchema and value from the parameters
                String valueSchemaId = lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_VALUE_SCHEMA_ID);
                String valueSchemaString = lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_VALUE_SCHEMA);

                valueSchema = parseSchema(schemaRegistryUrl, valueSchemaId, valueSchemaString, messageContext);
            }

            if (keySchema != null && keySchema.getType() == Schema.Type.RECORD) {
                key = jsonToAvro(key, keySchema);
            }

            if (valueSchema != null && valueSchema.getType() == Schema.Type.RECORD) {
                message = jsonToAvro(message, valueSchema);
            }

            // If key does not exist, generate.
            if (key == null) {
                key = String.valueOf(UUID.randomUUID());
            }

            //Read the partition No from the parameter
            String partitionNo = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARTITION_NO);

            org.apache.kafka.common.header.Headers headers = getDynamicParameters(messageContext);
            publishMessage(messageContext, topic, key, partitionNo, message, headers);

        } catch (AxisFault axisFault) {
            handleException("Kafka producer connector " +
                    ": Error sending the message to broker lists", axisFault, messageContext);
        } catch (IOException e) {
            handleException("Error sending avro message to broker", e, messageContext);
        }
    }

    /**
     * Method to parse the json schema. If the schema is not provided, then the schema
     * is obtained from the confluence schema registry
     *
     * @param schemaRegistryUrl The confluence schema registry url
     * @param schemaId          The schema id
     * @param schemaString      The schema string
     * @return Avro Schema from the provided json schema
     */
    public Schema parseSchema(String schemaRegistryUrl, String schemaId, String schemaString,
                              MessageContext messageContext) {
        Schema.Parser parser = new Schema.Parser();
        Schema schema = null;
        if (schemaString != null) {
            schema = parser.parse(schemaString);
        } else if (schemaId != null) {
            SchemaRegistryReader reader = new SchemaRegistryReader();
            schema = reader.getSchemaFromID(schemaRegistryUrl, schemaId);
        } /*else {
            handleException("Error parsing json schema.", new SynapseException("Either schema or schemaId must be " +
                    "provided. Both schema and schemaId can not be null."), messageContext);
        }*/
        return schema;
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
    private void publishMessage(MessageContext messageContext, String topic, Object key, String partitionNo,
                                Object message, Headers headers) {

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
    private void send(KafkaProducer<Object, Object> producer, String topic, String partitionNo, Object key,
                      Object message, org.apache.kafka.common.header.Headers headers, MessageContext messageContext)
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

    /**
     * Method to validate and deserialize json payload to avro object.
     *
     * @param jsonString The json payload.
     * @param schema     The schema.
     * @throws IOException On error.
     * @returns The avro object.
     */
    public Object jsonToAvro(Object jsonString, Schema schema) throws IOException {
        Object object = null;

        GenericRecord datum = null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
        Encoder encoder = EncoderFactory.get().binaryEncoder(output, null);

        try {
            DatumReader<Object> reader = new GenericDatumReader(schema);
            object = reader.read(null, decoderFactory.jsonDecoder(schema, (String) jsonString));

            if (schema.getType().equals(Schema.Type.STRING)) {
                object = ((Utf8) object).toString();
            }
            writer.write(object, encoder);
            encoder.flush();
            output.close();

        } catch (IOException e) {
            throw new IOException("Error deserializing json to Avro schema", e);
        }
        return output.toByteArray();
    }
}
