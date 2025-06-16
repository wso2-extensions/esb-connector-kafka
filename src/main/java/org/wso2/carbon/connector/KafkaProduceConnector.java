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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.Gson;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.InlineExpressionUtil;
import org.jaxen.JaxenException;
import org.json.JSONObject;
import org.wso2.carbon.connector.callbackhandler.KafkaSendCallbackHandler;
import org.wso2.carbon.connector.connection.KafkaConnection;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.connection.ConnectionHandler;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.connector.utils.Error;
import org.wso2.carbon.connector.utils.KafkaConnectConstants;
import org.wso2.carbon.connector.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Produce the messages to the kafka brokers.
 */
public class KafkaProduceConnector extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        if (log.isDebugEnabled()) {
            log.auditDebug("SEND : send message to Broker lists");
        }
        try {
            // Read the parameters
            String topic = lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_TOPIC);
            String dlqTopic = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARAM_DLQ_TOPIC);
            Object key = Utils.removeQuotesIfExist(lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_KEY));
            Object value = Utils.removeQuotesIfExist(lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_VALUE));

            if (value == null) {
                // if the parameter "value" is not configured, read the message from the message
                // context and assign it to value.
                value = getMessage(messageContext);
            }

            if (StringUtils.isEmpty(dlqTopic)) {
                dlqTopic = topic + KafkaConnectConstants.DLQ_TOPIC_EXTENSION;
            }

            String keySerializerClass = (String) messageContext.getProperty(
                    KafkaConnectConstants.KAFKA_KEY_SERIALIZER_CLASS);
            String valueSerializerClass = (String) messageContext.getProperty(
                    KafkaConnectConstants.KAFKA_VALUE_SERIALIZER_CLASS);

            Schema keySchema = null;
            Schema valueSchema = null;
            if (KafkaConnectConstants.KAFKA_AVRO_SERIALIZER.equalsIgnoreCase(keySerializerClass)) {
                // Read keySchemaId, keySchema, keySchemaVersion, keySchemaSubject, keySchemaMetadata and whether soft deleted schemas
                // (https://docs.confluent.io/platform/current/schema-registry/schema-deletion-guidelines.html#recovering-a-soft-deleted-schema)
                // need to be considered from the parameters
                String keySchemaId = lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_KEY_SCHEMA_ID);
                String keySchemaString = Utils.removeQuotesIfExist(lookupTemplateParameter(messageContext,
                                                                 KafkaConnectConstants.KAFKA_KEY_SCHEMA));
                String keySchemaVersion = lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_KEY_SCHEMA_VERSION);
                String keySchemaSubject = lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_KEY_SCHEMA_SUBJECT);
                String needSoftDeletedKeySchema = lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_KEY_SCHEMA_SOFT_DELETED);
                String keySchemaMetadata = Utils.removeQuotesIfExist(lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_KEY_SCHEMA_METADATA));

                keySchema = parseSchema(messageContext, keySchemaId, keySchemaString, keySchemaVersion,
                        keySchemaSubject, needSoftDeletedKeySchema, keySchemaMetadata);
            }
            if (KafkaConnectConstants.KAFKA_AVRO_SERIALIZER.equalsIgnoreCase(valueSerializerClass)) {
                // Read valueSchemaId, valueSchema, valueSchemaVersion, valueSchemaMetadata and valueSchemaSubject from the parameters
                String valueSchemaId = lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_VALUE_SCHEMA_ID);
                String valueSchemaString = Utils.removeQuotesIfExist(lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_VALUE_SCHEMA));
                String valueSchemaVersion = lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_VALUE_SCHEMA_VERSION);
                String valueSchemaSubject = lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_VALUE_SCHEMA_SUBJECT);
                String needSoftDeletedValueSchema = lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_VALUE_SCHEMA_SOFT_DELETED);
                String valueSchemaMetadata = Utils.removeQuotesIfExist(lookupTemplateParameter(messageContext,
                        KafkaConnectConstants.KAFKA_VALUE_SCHEMA_METADATA));

                valueSchema = parseSchema(messageContext, valueSchemaId, valueSchemaString, valueSchemaVersion,
                        valueSchemaSubject, needSoftDeletedValueSchema, valueSchemaMetadata);
            }

            if (Objects.nonNull(keySchema)) {
                key = convertToAvroObject((String) key, keySchema);
            }

            if (Objects.nonNull(valueSchema)) {
                value = convertToAvroObject((String) value, valueSchema);
            }

            // If key does not exist, generate.
            if (key == null) {
                key = String.valueOf(UUID.randomUUID());
            }

            //Read the partition No from the parameter
            String partitionNo = lookupTemplateParameter(messageContext, KafkaConnectConstants.PARTITION_NO);

            Headers headers = populateKafkaHeaders(messageContext);
            publishMessage(messageContext, topic, key, partitionNo, value, headers, dlqTopic);

        } catch (Exception e) {
            handleError(messageContext, e, Utils.getErrorCode(e),
                    "Kafka producer connector : Error sending the message to broker");
        }
    }

    /**
     * Sets error to context and handle.
     *
     * @param msgCtx      Message Context to set info
     * @param e           Exception associated
     * @param error       Error code
     * @param errorDetail Error detail
     */
    private void handleError(MessageContext msgCtx, Exception e, Error error, String errorDetail) {

        Utils.setErrorPropertiesToMessage(msgCtx, e, error);
        handleException(errorDetail, e, msgCtx);
    }

    /**
     * Method to parse the json schema. If the schema is not provided, then the schema is obtained from the confluence
     * schema registry using the schemaId provided.
     *
     * @param messageContext    The Message Context
     * @param schemaId          The schema id
     * @param schemaString      The schema string
     * @param schemaVersion      The schema version string
     * @param schemaSubject      The schema subject string
     * @param needSoftDeletedSchema  Whether soft deleted subjects are also needed
     * @return Avro Schema from the provided json schema
     */
    private Schema parseSchema(MessageContext messageContext, String schemaId, String schemaString,
                               String schemaVersion, String schemaSubject, String needSoftDeletedSchema,
                               String schemaMetadata) {
        if (schemaString != null) {
            Schema.Parser parser = new Schema.Parser();
            return parser.parse(schemaString);
        } else if (schemaId != null) {
            return getSchemaFromID(messageContext, schemaId);
        } else if (schemaSubject != null) {
            boolean isSoftDeletedSchemaNeeded = false;
            if (needSoftDeletedSchema != null) {
                isSoftDeletedSchemaNeeded = Boolean.parseBoolean(needSoftDeletedSchema);
            }

            ObjectMapper objectMapper = new ObjectMapper();

            if (schemaVersion != null) {
                return getSchemaFromVersionAndSubject(messageContext, schemaVersion, schemaSubject,
                        isSoftDeletedSchemaNeeded);
            }
            Map<String, String> metadata = null;
            if (schemaMetadata != null && !StringUtils.isEmpty(schemaMetadata)) {
                try {
                    metadata = objectMapper.readValue(schemaMetadata, new TypeReference<Map<String, String>>() {});
                } catch (JsonProcessingException e) {
                    throw new SynapseException("Invalid schema metadata format. The metadata must be provided as a "
                            + "valid JSON object with key-value pairs where both keys and values are of type String. "
                            + "Received: " + schemaMetadata, e);
                }
            }

            if (metadata != null) {
                return getLatestSchemaWithMetadata(messageContext, schemaSubject, metadata, isSoftDeletedSchemaNeeded);
            } else {
                return getLatestSchema(messageContext, schemaSubject);
            }
        }
        return null;
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
     * @param dlqTopic       The topic name of the Dead Letter Queue
     */
    private void publishMessage(MessageContext messageContext, String topic, Object key, String partitionNo,
                                Object message, Headers headers, String dlqTopic) throws Exception {

        // Get connection to kafka producer
        String connectionName = null;
        KafkaConnection connection = null;
        ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
        try {
            connectionName = getConnectionName(messageContext);
            connection = (KafkaConnection) handler
                    .getConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName);
            KafkaProducer producer = connection.getProducer();
            KafkaProducer dlqProducer = connection.getDLQProducer();
            KafkaSendCallbackHandler kafkaSendCallbackHandler = connection.getKafkaSendCallbackHandlerClass();
            send(producer, topic, partitionNo, key, message, headers, messageContext, kafkaSendCallbackHandler,
                    dlqProducer, dlqTopic);
        } finally {
            // Close the producer connections to all kafka brokers.
            // Also closes the zookeeper client connection if any
            if (connection != null) {
                handler.returnConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName, connection);
            }
        }
    }

    /**
     * Retrieves connection name from message context if configured as configKey attribute or from the template
     * parameter
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
     * Populate kafka headers.
     *
     * @param messageContext The message contest
     * @return extract the value's from properties and make its as header
     */
    private org.apache.kafka.common.header.Headers populateKafkaHeaders(MessageContext messageContext)
            throws InvalidConfigurationException {

        org.apache.kafka.common.header.Headers headers = new RecordHeaders();
        String forwardExistingHeaders = lookupTemplateParameter(messageContext, KafkaConnectConstants.FORWARD_EXISTING_HEADERS);

        if (KafkaConnectConstants.ALL_OPTION.equalsIgnoreCase(forwardExistingHeaders)) {
            Map<String, String> transportHeaders = getTransportHeaders(messageContext);
            addHeaders(headers, transportHeaders);
        } else if (KafkaConnectConstants.FILTERED_OPTION.equalsIgnoreCase(forwardExistingHeaders)) {
            Map<String, String> transportHeaders = getTransportHeaders(messageContext);
            addFilteredHeaders(messageContext, headers, transportHeaders);
        }

        addCustomHeaders(headers, lookupTemplateParameter(messageContext, KafkaConnectConstants.CUSTOM_HEADERS),
                messageContext);
        addCustomHeaders(headers, lookupTemplateParameter(messageContext, KafkaConnectConstants.CUSTOM_HEADER_EXPRESSION),
                messageContext);
        // Maintain the older way of defining Kafka headers to preserve backward compatibility
        addStaticHeaders(messageContext, headers);

        return headers;
    }

    private Map<String, String> getTransportHeaders(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgCtx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        return (Map<String, String>) axis2MsgCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    private void addHeaders(Headers headers, Map<String, String> source) {
        if (source != null) {
            source.forEach((key, value) -> {
                if (key != null) {
                    headers.add(key, getSafeBytes(value));
                }
            });
        }
    }

    private void addFilteredHeaders(MessageContext messageContext, Headers headers, Map<String, String> source) {
        if (source == null) {
            if (log.isDebugEnabled()) {
                log.debug("No Transport headers are found");
            }
            return;
        }
        String kafkaHeaderPrefix = lookupTemplateParameter(messageContext, KafkaConnectConstants.KAFKA_HEADER_PREFIX);

        if (StringUtils.isEmpty(kafkaHeaderPrefix)) {
            if (log.isDebugEnabled()) {
                log.debug("No kafkaHeaderPrefix is configured, even though the filtered option is selected for forwardExistingHeaders");
            }
            return;
        }

        boolean removeHeaderPrefix = Boolean.parseBoolean(lookupTemplateParameter(messageContext,
                KafkaConnectConstants.REMOVE_HEADER_PREFIX));
        boolean removeFilteredAfterSend = Boolean.parseBoolean(lookupTemplateParameter(messageContext,
                KafkaConnectConstants.REMOVE_FILTERED_HEADERS_AFTER_SEND));

        source.forEach((key, value) -> {
            if (key.startsWith(kafkaHeaderPrefix)) {
                String headerKey = removeHeaderPrefix ? key.substring(kafkaHeaderPrefix.length()) : key;
                headers.add(headerKey, value.getBytes());

                if (removeFilteredAfterSend) {
                    source.remove(key);
                }
            }
        });
    }

    private void addCustomHeaders(Headers headers, String customHeaders, MessageContext messageContext)
            throws InvalidConfigurationException {

        if (customHeaders == null || customHeaders.trim().isEmpty()) return;

        try {
            customHeaders = InlineExpressionUtil.processInLineSynapseExpressionTemplate(messageContext, customHeaders);
        } catch (JaxenException e) {
            throw new InvalidConfigurationException("Error occurred while processing inline expressions "
                    + "in message properties.", e);
        } catch (NoSuchMethodError e) {
            if (InlineExpressionUtil.checkForInlineExpressions(customHeaders)) {
                customHeaders = InlineExpressionUtil.replaceDynamicValues(messageContext, customHeaders).trim();
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonArray = mapper.readTree(customHeaders);
            if (jsonArray != null && jsonArray.isArray()) {
                for (JsonNode node : jsonArray) {
                    if (node.isObject()) {
                        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> entry = fields.next();
                            String key = entry.getKey();
                            JsonNode value = entry.getValue();
                            if (key != null && value.isTextual()) {
                                headers.add(key, getSafeBytes(value.asText()));
                            } else {
                                log.warn("Skipping non-textual value or null key in custom header entry: " + entry);
                            }
                        }
                    } else {
                        log.warn("Skipping non-object item in customHeaders array: " + node);
                    }
                }
            } else {
                throw new InvalidConfigurationException("Custom Headers must be a JSON array of objects, where each "
                        + "object represents a Kafka header as a key-value pair. Received : " + customHeaders);
            }
        } catch (JsonProcessingException e) {
            throw new InvalidConfigurationException("Invalid Custom Headers format. Must be a JSON array of objects, "
                    + "where each object represents a Kafka header as a key-value pair. Received : " + customHeaders, e);
        }
    }

    private void addStaticHeaders(MessageContext messageContext, Headers headers) {
        // Maintain the older way of defining Kafka headers to preserve backward compatibility
        String key = KafkaConnectConstants.DEFAULT_KAFKA_HEADER_PREFIX;
        Map<String, Object> propertiesMap = (((Axis2MessageContext) messageContext).getProperties());
        for (String keyValue : propertiesMap.keySet()) {
            if (keyValue.startsWith(key)) {
                Value propertyValue = (Value) propertiesMap.get(keyValue);
                headers.add(keyValue.substring(key.length()), propertyValue.
                        evaluateValue(messageContext).getBytes());
            }
        }
    }

    private byte[] getSafeBytes(String value) {
        return value != null ? value.getBytes() : new byte[0];
    }

    /**
     * Send the messages to the kafka broker with topic and the key that is optional.
     *
     * @param producer             The Kafka producer instance used to send the message.
     * @param topic                The topic to which the message will be sent.
     * @param partitionNo          The partition number of the broker.
     * @param key                  The key for the message (optional).
     * @param message              The actual message to be sent to the Kafka broker.
     * @param headers              The Kafka message headers.
     * @param messageContext       The Synapse message context.
     * @param sendCallbackHandler  The Kafka send callback handler implementation.
     * @param dlqProducer          The Kafka producer instance used to publish messages to the Dead Letter Queue (DLQ).
     * @param dlqTopic             The topic name of the Dead Letter Queue.
     */
    private void send(KafkaProducer<Object, Object> producer, String topic, String partitionNo, Object key,
                      Object message, Headers headers, MessageContext messageContext,
                      KafkaSendCallbackHandler sendCallbackHandler, KafkaProducer<Object, Object> dlqProducer,
                      String dlqTopic) throws ExecutionException, InterruptedException {

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
        ProducerRecord<Object, Object> record = new ProducerRecord<>(topic, partitionNumber, key, message, headers);

        metaData = producer.send(record, (metadata, exception) -> {
            sendCallbackHandler.onCompletion(metadata, exception, dlqProducer, record, dlqTopic);
        });

        messageContext.setProperty("topic", metaData.get().topic());
        messageContext.setProperty("offset", metaData.get().offset());
        messageContext.setProperty("partition", metaData.get().partition());

        if (log.isDebugEnabled()) {
            log.debug("Flushing producer after sending the message");
        }
        producer.flush();
    }

    /**
     * Method to validate and convert json payload to Avro object. Casting would fail if the data types are not in sync
     * with the schema.
     *
     * @param jsonString The json payload.
     * @param schema     The schema.
     * @throws IOException On error.
     * @return The avro object.
     */
    private Object convertToAvroObject(String jsonString, Schema schema) {
        if (jsonString == null) {
            return null;
        }
        switch (schema.getType()) {
            case STRING:
                if (LogicalTypes.uuid().equals(schema.getLogicalType()) && !isValidUUID(jsonString.toString())) {
                    throw new SerializationException("Error serializing Avro message of type String"
                            + " with logicalType uuid for input: " + jsonString
                            + ". The input needs to be in the correct format for a UUID.");
                }
                return jsonString;
            case BOOLEAN:
                return Boolean.valueOf(jsonString);
            case INT:
                return handleTypeInt(jsonString, schema);
            case LONG:
                return handleTypeLong(jsonString, schema);
            case FLOAT:
                try {
                    return Float.valueOf(jsonString);
                } catch (NumberFormatException e) {
                    throw new SerializationException(
                            "Error serializing Avro message of type float for input: " + jsonString, e);
                }
            case DOUBLE:
                try {
                    return Double.valueOf(jsonString);
                } catch (NumberFormatException e) {
                    throw new SerializationException(
                            "Error serializing Avro message of type double for input: " + jsonString, e);
                }
            case BYTES:
            case RECORD:
            case ARRAY:
            case MAP:
            case UNION:
            case ENUM:
            case FIXED:
                return handleComplexAvroTypes(jsonString, schema);
            default:
                throw new SerializationException("Unsupported Avro type. Supported types are null, Boolean, "
                                                         + "Integer, Long, Float, Double, String, byte[], "
                                                         + "record, array, map, union, enum, and fixed");

        }

    }

    /**
     * Validate and convert the json payload with complex Avro types to Avro Objects.
     *
     * @param jsonString the json payload
     * @param schema     the schema
     * @return the Avro object
     */
    private Object handleComplexAvroTypes(Object jsonString, Schema schema) {
        if (jsonString == null) {
            return null;
        }
        Object defaultValueNode = schema.getObjectProp("default");
        boolean usingDefault = false;
        switch (schema.getType()) {
            case BYTES:
                if (KafkaConnectConstants.LOGICAL_TYPE_DECIMAL.equals(schema.getLogicalType().getName())) {
                    try {
                        return Utils.convertDecimalToBytes(schema, new BigDecimal(jsonString.toString()));
                    } catch (NumberFormatException e) {
                        throw new SerializationException(
                                "Error serializing Avro message of type bytes with logicalType decimal for input: " + jsonString);
                    }
                }
                return String.valueOf(jsonString).getBytes();
            case RECORD:
                return convertToGenericRecord(String.valueOf(jsonString), getNonNullUnionSchema(schema));
            case ARRAY:
                ObjectMapper objectMapper = new ObjectMapper();
                List<Object> objectList = new ArrayList<>();
                if (defaultValueNode != null && defaultValueNode instanceof JsonNode
                         && ((JsonNode)defaultValueNode).isArray()) {
                    usingDefault = true;
                    ArrayNode defaultValueArray = (ArrayNode) defaultValueNode;
                    for (int i = 0; i < defaultValueArray.size(); i++) {
                        objectList.add(defaultValueArray.get(i));
                    }
                }
                try {
                    objectList = objectMapper.readValue(String.valueOf(jsonString), List.class);
                } catch (IOException e) {
                    if (!usingDefault) {
                        throw new SerializationException(
                                "Error serializing Avro message of type array for input: " + jsonString, e);
                    }
                }

                List<Object> avroList = objectList.stream()
                        .map(element -> {
                            if (element instanceof LinkedHashMap) {
                                element = new Gson().toJson(element, Map.class);
                            }
                            return handleComplexAvroTypes(element,
                                                          getNonNullUnionSchema(schema).getElementType());
                        }).collect(Collectors.toList());

                return new GenericData.Array(schema, avroList);

            case MAP:
                ObjectMapper mapper = new ObjectMapper();
                Map<String, ?> map = new HashMap<>();
                if (defaultValueNode != null && defaultValueNode instanceof JsonNode
                        && ((JsonNode)defaultValueNode).isObject()) {
                    try {
                        map = mapper.readValue(String.valueOf(defaultValueNode), Map.class);
                        usingDefault = true;
                    } catch (IOException e) {
                        // Neglect this error since we are checking the jsonString in next step
                    }
                }
                try {
                    map = mapper.readValue(String.valueOf(jsonString), Map.class);
                } catch (IOException e) {
                    if (!usingDefault) {
                        throw new SerializationException(
                                "Error serializing Avro message of type map for input: " + jsonString, e);
                    }
                }

                return map.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                  e -> handleComplexAvroTypes(e.getValue(),
                                                                              getNonNullUnionSchema(schema)
                                                                                      .getValueType())));
            case UNION:
                List<Schema> types = schema.getTypes();
                int noOfTypes = types.size();
                for (int index = 0; index < noOfTypes; index++) {
                    Schema type = types.get(index);
                    if (type.getType() == Schema.Type.NULL) {
                        if (jsonString == null) {
                            return handleComplexAvroTypes(jsonString, type);
                        }
                    } else {
                        try {
                            return handleComplexAvroTypes(jsonString, type);
                        } catch (Exception e) {
                            // continue for other types in the schema
                            if (index + 1 == noOfTypes) {
                                throw new SerializationException(
                                        "Error serializing Avro message of type union. The input: " + jsonString +
                                                " should be one of the types from" + types.toString());
                            }
                        }
                    }
                }
                return null;
            case ENUM:
                if (StringUtils.isBlank(jsonString.toString()) && defaultValueNode != null
                        && defaultValueNode instanceof JsonNode) {
                    String enumDefault = ((JsonNode)defaultValueNode).textValue();
                    if (!schema.getEnumSymbols().contains(enumDefault)) {
                        throw new SerializationException(
                                "The Enum Default: " + enumDefault + " is not in the enum symbol set: " + schema.getEnumSymbols());
                    }
                    jsonString = enumDefault;
                }
                return new GenericData.EnumSymbol(schema, jsonString);
            case FIXED:
                if (KafkaConnectConstants.LOGICAL_TYPE_DECIMAL.equals(schema.getLogicalType().getName())) {
                    try {
                        return Utils.convertDecimalToFixed(schema, new BigDecimal(jsonString.toString()));
                    } catch (NumberFormatException e) {
                        throw new SerializationException(
                                "Error serializing Avro message of type fixed with logicalType decimal for input: " + jsonString);
                    }
                }
                return new GenericData.Fixed(schema, String.valueOf(jsonString).getBytes());
            case LONG:
                return handleTypeLong(jsonString.toString(), schema);
            case INT:
                return handleTypeInt(jsonString.toString(), schema);
            case FLOAT:
                try {
                    return Float.valueOf(jsonString.toString());
                } catch (NumberFormatException e) {
                    throw new SerializationException(
                            "Error serializing Avro message of type float for input: " + jsonString, e);
                }
            case DOUBLE:
                try {
                    return Double.valueOf(jsonString.toString());
                } catch (NumberFormatException e) {
                    throw new SerializationException(
                            "Error serializing Avro message of type double for input: " + jsonString, e);
                }
            case BOOLEAN:
                if (isBoolean(jsonString.toString())) {
                    return new Boolean(jsonString.toString());
                } else {
                    throw new SerializationException(
                            "Error serializing Avro message of type boolean for input: " + jsonString);
                }
            case STRING:
                if (jsonString instanceof String) {
                    if (LogicalTypes.uuid().equals(schema.getLogicalType()) && !isValidUUID(jsonString.toString())) {
                        throw new SerializationException("Error serializing Avro message of type String "
                                + "with logicalType uuid for input: " + jsonString
                                + ". The input needs to be in the correct format for a UUID.");
                    }
                    return jsonString.toString();
                } else {
                    throw new SerializationException(
                            "Error serializing Avro message of type String for input: " + jsonString);
                }
            default:
                return jsonString;

        }
    }

    private Object handleTypeInt(String input, Schema schema) {
        if (LogicalTypes.date().equals(schema.getLogicalType())) {
            try {
                Date date = DateUtils.parseDate(input, "yyyy-MM-dd");
                return Utils.convertFromDate(date);
            } catch (ParseException e) {
                throw new SerializationException("Error serializing Avro message of type int with logicalType "
                        + "date for input: " + input + ". The input needs to be in the 'yyyy-MM-dd' format.");
            }
        }
        if (LogicalTypes.timeMillis().equals(schema.getLogicalType())) {
            try {
                String timeString = preprocessTimeString(input, Utils.PATTERN_FOR_MILLIS_PART_WITHOUT_TIME_ZONE, "3");
                Date date = DateUtils.parseDate(timeString, "HH:mm:ss.SSS");
                return Utils.convertFromTimeMillis(date);
            } catch (ParseException e) {
                throw new SerializationException("Error serializing Avro message of type int with logicalType "
                        + "time-millis for input: " + input + ". The input needs to be in the 'HH:mm:ss.SSS' format.");
            }
        }
        try {
            return new Integer(input);
        } catch (NumberFormatException e) {
            throw new SerializationException(
                    "Error serializing Avro message of type int for input: " + input, e);
        }
    }

    private Object handleTypeLong(String input, Schema schema) {
        if (LogicalTypes.timeMicros().equals(schema.getLogicalType())) {
            try {
                String timeString = preprocessTimeString(input, Utils.PATTERN_FOR_MICROS_PART_WITHOUT_TIME_ZONE, "6");
                Date date = DateUtils.parseDate(timeString, "HH:mm:ss.SSSSSS");
                return Utils.convertFromTimeMicros(date);
            } catch (ParseException e) {
                throw new SerializationException("Error serializing Avro message of type long with logicalType "
                        + "time-micros for input: " + input + ". The input needs to be in the 'HH:mm:ss.SSSSSS' format.");
            }
        }
        if (LogicalTypes.timestampMillis().equals(schema.getLogicalType())) {
            try {
                String timestampString = preprocessTimeString(input, Utils.PATTERN_FOR_MILLIS_PART_WITH_TIME_ZONE, "3");
                // Parse the date string to an Instant
                Instant instant = Instant.parse(timestampString);
                // Convert the Instant to milliseconds since the Unix epoch
                return instant.toEpochMilli();

            } catch (DateTimeParseException e) {
                throw new SerializationException("Error serializing Avro message of type long with logicalType "
                        + "timestamp-millis for input: " + input
                        + ". The input needs to be in the yyyy-MM-dd'T'HH:mm:ss.SSS'Z' format.");
            }
        }
        if (LogicalTypes.timestampMicros().equals(schema.getLogicalType())) {
            try {
                String timestampString = preprocessTimeString(input, Utils.PATTERN_FOR_MICROS_PART_WITH_TIME_ZONE, "6");
                // Parse the date string to an Instant
                Instant instant = Instant.parse(timestampString);
                // Convert the Instant to microseconds since the Unix epoch
                return ChronoUnit.MICROS.between(Instant.EPOCH, instant);

            } catch (DateTimeParseException e) {
                throw new SerializationException("Error serializing Avro message of type long with logicalType "
                        + "timestamp-micros for input: " + input
                        + ". The input needs to be in the yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z' format.");
            }
        }
        if (LogicalTypes.localTimestampMillis().equals(schema.getLogicalType())) {
            try {
                String timestampString = preprocessTimeString(input, Utils.PATTERN_FOR_MILLIS_PART_WITHOUT_TIME_ZONE, "3");
                return Utils.getInstantForLocalTimestamp(timestampString, "yyyy-MM-dd'T'HH:mm:ss.SSS").toEpochMilli();

            } catch (DateTimeParseException e) {
                throw new SerializationException("Error serializing Avro message of type long with logicalType "
                        + "local-timestamp-millis for input: " + input
                        + ". The input needs to be in the 'yyyy-MM-dd'T'HH:mm:ss.SSS' format.");
            }
        }
        if (LogicalTypes.localTimestampMicros().equals(schema.getLogicalType())) {
            try {
                String timestampString = preprocessTimeString(input, Utils.PATTERN_FOR_MICROS_PART_WITHOUT_TIME_ZONE, "6");
                return ChronoUnit.MICROS.between(Instant.EPOCH, Utils.getInstantForLocalTimestamp(timestampString,
                        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));

            } catch (DateTimeParseException e) {
                throw new SerializationException("Error serializing Avro message of type long with logicalType "
                        + "local-timestamp-micros for input: " + input
                        + ". The input needs to be in the 'yyyy-MM-dd'T'HH:mm:ss.SSSSSS' format.");
            }
        }
        try {
            return Long.valueOf(input);
        } catch (NumberFormatException e) {
            throw new SerializationException(
                    "Error serializing Avro message of type long for input: " + input, e);
        }
    }

    /**
     * Preprocesses a time string to ensure that the milliseconds or microseconds part has exactly
     * the specified number of digits.
     * <p>
     * This method searches for the milliseconds or microseconds part in the given time string using
     * the provided regular expression. If the part is found, it pads it with zeros to ensure it has exactly
     * the specified number of digits.
     *
     * @param timeString the original time string to be processed.
     * @param pattern the pattern to find the milliseconds or microseconds part in the time string.
     * @param noOfDigit the number of digits that the milliseconds or microseconds part should have after processing.
     * @return the processed time string with the milliseconds or microseconds part formatted to the specified
     * number of digits, or the original time string if the part is not found.
     */
    private static String preprocessTimeString(String timeString, Pattern pattern, String noOfDigit) {
        Matcher matcher = pattern.matcher(timeString);

        if (matcher.find()) {
            // Extract milli or microseconds part without the dot
            String matchingPart = matcher.group(1).substring(1);
            // Pad with zeros to ensure it has exactly the given no of digits
            matchingPart = String.format("%-" + noOfDigit + "s", matchingPart).replace(' ', '0');
            // Replace the original milli/microseconds part with the formatted one
            return matcher.replaceFirst("." + matchingPart);
        } else {
            // If there's no milli/microseconds part, return the original string
            return timeString;
        }
    }

    public boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check whether the string is boolean type
     *
     * @param value the json string
     * @return true if the string is boolean type
     */
    private boolean isBoolean(String value) {

        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    /**
     * Check whether the schema is the type of Union and get the non null union schema.
     *
     * @param schema the schema
     * @return the non null union schema if the schema is the type of Union. Otherwise, return the provided schema
     * itself.
     */
    private Schema getNonNullUnionSchema(Schema schema) {
        if (schema.getType().equals(Schema.Type.UNION)) {
            List<Schema> types = schema.getTypes();
            // Two non-nullable types in a union is not yet supported.
            // Typically a nullable field's schema is configured as an union of Null and a Type.
            // This is to check whether the Union is a Nullable field
            if (types.size() == 2) {
                if (types.get(0).getType() == Schema.Type.NULL) {
                    return types.get(1);
                } else if (types.get(1).getType() == Schema.Type.NULL) {
                    return types.get(0);
                }
            }
        }
        return schema;
    }

    /**
     * Convert the json payload of type "record" to a GenericRecord object.
     *
     * @param jsonString the json payload
     * @param schema     the schema with type "record"
     * @return GenericRecord object obtained from the jsonString.
     */
    private GenericRecord convertToGenericRecord(String jsonString, Schema schema) {
        List<String> fieldNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        getFieldNamesAndValues(jsonString, fieldNames, values);

        GenericData.Record record = new GenericData.Record(schema);

        for (int index = 0; index < fieldNames.size(); index++) {
            String fieldName = fieldNames.get(index);

            if (schema.getField(fieldName) == null) {
                continue;
            }
            Object value = values.get(index);
            Schema fieldSchema = schema.getField(fieldName).schema();
            record.put(fieldName, handleComplexAvroTypes(value, getNonNullUnionSchema(fieldSchema)));
        }
        return new GenericRecordBuilder(record).build();
    }

    /**
     * Fetch fieldNames and values from the given json payload.
     *
     * @param jsonString the json payload
     * @param fieldNames keys of the json payload
     * @param values     values of each key
     */
    private static void getFieldNamesAndValues(String jsonString, List<String> fieldNames, List<Object> values) {
        JSONObject ob = new JSONObject(jsonString);
        Iterator<String> keysItr = ob.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            fieldNames.add(key);
            if (ob.isNull(key)) {
                values.add(null);
            } else {
                values.add(ob.get(key));
            }
        }
    }

    /**
     * Method to get the schema from the confluence schema registry.
     *
     * @param messageContext The MessageContext
     * @param schemaID    The schema id.
     * @return The schema.
     */
    private Schema getSchemaFromID(MessageContext messageContext, String schemaID) {

        Schema jsonSchema;
        String connectionName = null;
        KafkaConnection connection = null;
        ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
        try {
            connectionName = getConnectionName(messageContext);
            connection = (KafkaConnection) handler
                    .getConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName);
            CachedSchemaRegistryClient client = connection.getRegistryClient();
            jsonSchema =  client.getById(Integer.parseInt(schemaID));

        } catch (RestClientException e) {
            throw new SynapseException("Schema not found or error in obtaining the schema from the confluence schema registry", e);
        } catch (IOException | ConnectException | InvalidConfigurationException e) {
            throw new SynapseException("Error obtaining the schema from the confluence schema registry", e);
        } finally {
           //close the client connection to the schema registry
            if (connection != null) {
                handler.returnConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName, connection);
            }
        }
        return jsonSchema;
    }

    /**
     * Method to get the schema from the confluence schema registry.
     *
     * @param messageContext The MessageContext
     * @param schemaVersion  The schema version.
     * @param schemaSubject  The schema subject.
     * @param needSoftDeletedSchema  Whether soft deleted subjects are also needed.
     * @return The schema.
     */
    private Schema getSchemaFromVersionAndSubject(MessageContext messageContext, String schemaVersion,
                                                 String schemaSubject, boolean needSoftDeletedSchema) {
        io.confluent.kafka.schemaregistry.client.rest.entities.Schema schema;
        String connectionName = null;
        KafkaConnection connection = null;
        ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
        try {
            connectionName = getConnectionName(messageContext);
            connection = (KafkaConnection) handler
                    .getConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName);
            CachedSchemaRegistryClient client = connection.getRegistryClient();
            schema = client.
                    getByVersion(schemaSubject, Integer.parseInt(schemaVersion), needSoftDeletedSchema);
            if (schema == null) {
                throw new SynapseException("No schema reference found for subject \""
                        + schemaSubject
                        + "\" and version "
                        + schemaVersion);
            }
        } catch (ConnectException | InvalidConfigurationException e) {
            throw new SynapseException("Error obtaining the schema from the confluence schema registry", e);
        } finally {
            // close the client connection to the schema registry
            if (connection != null) {
                handler.returnConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName, connection);
            }
        }
        return new Schema.Parser().parse(schema.getSchema());
    }

    /**
     * Retrieves and parses the latest schema for the given schema subject from the Schema Registry.
     *
     * <p>This method fetches the latest schema metadata using {@code getLatestSchemaMetadata} and
     * then parses the schema into an Avro {@link Schema} object. If no schema is found,
     * it throws a {@link SynapseException}.</p>
     *
     * @param messageContext The message context.
     * @param schemaSubject The subject name of the schema in the Schema Registry.
     * @return The latest parsed {@link Schema} for the given schema subject.
     * @throws SynapseException If no schema is found for the given subject.
     */
    private Schema getLatestSchema(MessageContext messageContext, String schemaSubject) {
        SchemaMetadata schemaMetadata = getLatestSchemaMetadata(messageContext, schemaSubject, null, false);
        if (Objects.isNull(schemaMetadata.getSchema())) {
            throw new SynapseException("No schema found for schema subject: " + schemaSubject);
        }
        return new Schema.Parser().parse(schemaMetadata.getSchema());
    }

    /**
     * Retrieves and parses the latest schema with metadata for the given schema subject from the Schema Registry.
     *
     * <p>This method fetches the latest schema metadata using {@code getLatestSchemaMetadata}, including any
     * provided metadata and the option to look up deleted schemas. It then parses the schema into an Avro
     * {@link Schema} object. If no schema is found, a {@link SynapseException} is thrown.</p>
     *
     * @param messageContext The message context.
     * @param schemaSubject The subject name of the schema in the Schema Registry.
     * @param metadata A map of metadata parameters for schema retrieval.
     * @param lookupDeletedSchema A flag indicating whether to look up deleted schemas.
     * @return The latest parsed {@link Schema} for the given schema subject.
     * @throws SynapseException If no schema is found for the given subject.
     */
    private Schema getLatestSchemaWithMetadata(MessageContext messageContext, String schemaSubject,
                                               Map<String, String> metadata, boolean lookupDeletedSchema) {
        SchemaMetadata schemaMetadata = getLatestSchemaMetadata(messageContext, schemaSubject, metadata, lookupDeletedSchema);
        if (Objects.isNull(schemaMetadata.getSchema())) {
            throw new SynapseException("No schema found for schema subject: " + schemaSubject);
        }
        return new Schema.Parser().parse(schemaMetadata.getSchema());
    }

    /**
     * Retrieves the latest schema metadata from the Confluent Schema Registry.
     *
     * <p>This method attempts to fetch the latest schema metadata for a given schema subject.
     * If metadata parameters are provided, it retrieves the schema with metadata; otherwise,
     * it fetches the latest schema version. It ensures proper handling of the Kafka connection
     * and manages potential exceptions.</p>
     *
     * @param messageContext The message context.
     * @param schemaSubject The subject name of the schema in the Schema Registry.
     * @param metadata Optional metadata to be passed for retrieving schema information.
     * @param lookupDeletedSchema Whether to include deleted schemas in the lookup.
     * @return The latest {@link SchemaMetadata} associated with the given schema subject.
     * @throws SynapseException If there is an error retrieving schema metadata.
     */
    private SchemaMetadata getLatestSchemaMetadata(MessageContext messageContext, String schemaSubject,
                                                   Map<String, String> metadata, boolean lookupDeletedSchema) {
        SchemaMetadata schemaMetadata;
        String connectionName = null;
        KafkaConnection connection = null;
        ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
        try {
            connectionName = getConnectionName(messageContext);
            connection = (KafkaConnection) handler
                    .getConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName);
            CachedSchemaRegistryClient client = connection.getRegistryClient();

            if (Objects.isNull(metadata)) {
                schemaMetadata = client.getLatestSchemaMetadata(schemaSubject);
            } else {
                schemaMetadata = client.getLatestWithMetadata(schemaSubject, metadata, lookupDeletedSchema);
            }
            if (schemaMetadata == null) {
                throw new SynapseException("No schema metadata found for schema subject: " + schemaSubject);
            }
        } catch (InvalidConfigurationException | ConnectException | RestClientException | IOException e) {
            throw new SynapseException("Error obtaining the schema metadata from the confluence schema registry", e);
        } finally {
            // close the client connection to the schema registry
            if (connection != null) {
                handler.returnConnection(KafkaConnectConstants.CONNECTOR_NAME, connectionName, connection);
            }
        }
        return schemaMetadata;
    }
}
