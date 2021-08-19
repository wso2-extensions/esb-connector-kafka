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
import org.apache.avro.SchemaBuilderException;
import org.apache.avro.SchemaParseException;
import org.apache.avro.UnresolvedUnionException;
import org.apache.axis2.AxisFault;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.exception.InvalidConfigurationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

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
}
