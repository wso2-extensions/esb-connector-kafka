/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.utils;

import org.apache.avro.AvroTypeException;
import org.apache.avro.Conversions;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilderException;
import org.apache.avro.SchemaParseException;
import org.apache.avro.UnresolvedUnionException;
import org.apache.avro.generic.GenericFixed;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.connector.KafkaConnectConstants;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.regex.Pattern;

public class Utils {

     public static final Pattern PATTERN_FOR_MILLIS_PART_WITH_TIME_ZONE =
             Pattern.compile(KafkaConnectConstants.REGEX_FOR_MILLIS_PART_WITH_TIME_ZONE);
     public static final Pattern PATTERN_FOR_MICROS_PART_WITH_TIME_ZONE =
             Pattern.compile(KafkaConnectConstants.REGEX_FOR_MICROS_PART_WITH_TIME_ZONE);
     public static final Pattern PATTERN_FOR_MILLIS_PART_WITHOUT_TIME_ZONE =
             Pattern.compile(KafkaConnectConstants.REGEX_FOR_MILLIS_PART_WITHOUT_TIME_ZONE);
     public static final Pattern PATTERN_FOR_MICROS_PART_WITHOUT_TIME_ZONE =
             Pattern.compile(KafkaConnectConstants.REGEX_FOR_MICROS_PART_WITHOUT_TIME_ZONE);
    public static final String JSON_OBJECT_START = "'{";
    public static final String JSON_OBJECT_END = "}'";

    /**
     * Sets the error code and error detail to the message context
     *
     * @param messageContext Message Context
     * @param exception      Exception associated
     * @param error          Error to be set
     */
    public static void setErrorPropertiesToMessage(MessageContext messageContext, Exception exception, Error error) {

        messageContext.setProperty(SynapseConstants.ERROR_CODE, error.getErrorCode());
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, exception.getMessage());
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, getStackTrace(exception));
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION, exception);
    }

    /**
     * Get the stack trace into a String
     *
     * @param throwable
     * @return the stack trace as a string
     */
    private static String getStackTrace(Throwable throwable) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }

    /**
     * Get the Error Code related to the exception
     *
     * @param exception
     * @return the Error related to the exception
     */
    public static Error getErrorCode(Exception exception) {

        if (exception instanceof AxisFault) {
            return Error.AXIS_FAULT_ERROR;
        }
        if (exception instanceof ConnectException) {
            return Error.CONNECTION_ERROR;
        }
        if (exception instanceof InvalidConfigurationException) {
            return Error.INVALID_CONFIGURATION;
        }
        if (exception instanceof UnresolvedUnionException) {
            return Error.UNRESOLVED_UNION_ERROR;
        }
        if (exception instanceof SchemaParseException) {
            return Error.SCHEMA_PARSE_ERROR;
        }
        if (exception instanceof SchemaBuilderException) {
            return Error.SCHEMA_BUILDER_ERROR;
        }
        if (exception instanceof AvroTypeException) {
            return Error.AVRO_TYPE_ERROR;
        }
        if (exception instanceof SerializationException) {
            return Error.SERIALIZATION_ERROR;
        }
        return Error.KAFKA_GENERAL_ERROR;
    }

    /**
     * Converts a given Date object to an integer representing the number of milliseconds
     * since midnight (00:00:00.000) of the same day, adjusted for the local timezone.
     *
     * @param date Date object
     * @return number of milliseconds since midnight (00:00:00.000)
     */
    public static int convertFromTimeMillis(Date date) {
        final long converted = toEpochMillis(date);
        return (int) (converted % 86400000L);
    }

    /**
     * Converts a given Date object to an integer representing the number of days
     * since the Unix epoch (January 1, 1970).
     *
     * @param date Date object
     * @return number of days since the Unix epoch (January 1, 1970)
     */
    public static int convertFromDate(Date date) {
        final long converted = toEpochMillis(date);
        return (int) (converted / 86400000L);
    }

    /**
     * Converts a given Date object to a long representing the number of microseconds since midnight (00:00:00.000000)
     * of the same day, adjusted for the local timezone.
     *
     * @param date Date object
     * @return a long representing the number of microseconds since midnight (00:00:00.000000)
     */
    public static long convertFromTimeMicros(Date date) {
        final long converted = toEpochMillis(date);
        return (converted % 86400000L) * 1000L;
    }

    /**
     * Converts a given Date object to the number of milliseconds since the Unix epoch (January 1, 1970),
     * adjusted for the local timezone.
     *
     * @param date Date object
     * @return number of milliseconds since the Unix epoch (January 1, 1970)
     */
    public static long toEpochMillis(java.util.Date date) {
        final long time = date.getTime();
        return time + (long) KafkaConnectConstants.LOCAL_TZ.getOffset(time);
    }

    /**
     * Converts a local timestamp string to an {@link Instant} object.
     * <p>
     * This method takes a timestamp string and a date-time pattern, parses the string into a {@link LocalDateTime}
     * object using the provided pattern, then converts it to a {@link ZonedDateTime} in the system's default time zone,
     * and finally converts it to an {@link Instant} representing a point in time in UTC.
     * </p>
     *
     * @param timestampString the local timestamp string to be converted, not null
     * @param pattern the date-time pattern that matches the input timestamp string, not null
     * @return the corresponding {@link Instant} representing the point in time in UTC
     * @throws DateTimeParseException if the timestamp string cannot be parsed
     */
    public static Instant getInstantForLocalTimestamp(String timestampString, String pattern) {
        // Define the DateTimeFormatter to match the input format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        // Parse the local timestamp string to a LocalDateTime object
        LocalDateTime localDateTime = LocalDateTime.parse(timestampString, formatter);

        // Convert LocalDateTime to ZonedDateTime with the system's default time zone
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());

        // Convert ZonedDateTime to an Instant (representing a point in time in UTC)
        return zonedDateTime.toInstant();
    }

    /**
     * Converts a BigDecimal to a ByteBuffer using Avro's decimal logical type.
     *
     * @param schema an Avro schema object
     * @param decimal the BigDecimal value to be converted to bytes
     * @return byte buffer representing the decimal
     */
    public static ByteBuffer convertDecimalToBytes(Schema schema, BigDecimal decimal) {

        Conversions.DecimalConversion conversion = new Conversions.DecimalConversion();
        return conversion.toBytes(decimal, schema, schema.getLogicalType());
    }

    /**
     * Converts a BigDecimal to a GenericFixed object using Avro's decimal logical type.
     *
     * @param schema  an Avro schema object
     * @param decimal the BigDecimal value to be converted to a GenericFixed object
     * @return GenericFixed object representing the decimal
     */
    public static GenericFixed convertDecimalToFixed(Schema schema, BigDecimal decimal) {

        Conversions.DecimalConversion conversion = new Conversions.DecimalConversion();
        return conversion.toFixed(decimal, schema, schema.getLogicalType());
    }

    /**
     * Processes a string to determine if it is a JSON object wrapped in single quotes,
     * and if so, returns the unwrapped JSON string.
     *
     * <p>This method checks whether the input string:
     * <ul>
     *   <li>Is not null</li>
     *   <li>Starts and ends with a single quote (')</li>
     *   <li>Contains a valid JSON object structure (starts with '{' and ends with '}')</li>
     * </ul>
     * If all conditions are met, it strips the single quotes and returns the JSON string.
     * Otherwise, it returns the original input.
     *
     * @param content the input string to be processed
     * @return the unwrapped JSON string if valid, otherwise the original input string
     */
    public static String removeQuotesIfExist(String content) {

        if (StringUtils.length(StringUtils.trim(content)) > 4
                && content.startsWith(JSON_OBJECT_START) && content.endsWith(JSON_OBJECT_END)) {
            return content.substring(1, content.length() - 1);
        }
        return content;
    }
}
