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
import java.util.Date;

public class Utils {

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
     * converts a given java.sql.Timestamp object to the number of microseconds since the Unix epoch (January 1, 1970),
     * adjusted for the local timezone.
     *
     * @param date Timestamp object
     * @return number of microseconds since the Unix epoch (January 1, 1970)
     */
    public static long toEpochMicros(java.sql.Timestamp date) {
        long millis = date.getTime();
        long micros = millis * 1000 + (date.getNanos() % 1_000_000 / 1000);
        long offset = KafkaConnectConstants.LOCAL_TZ.getOffset(millis) * 1000L;
        return micros + offset;
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
}
