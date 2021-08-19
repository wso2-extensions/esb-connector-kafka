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

/**
 * Contains error codes and details
 * related to kafka connector
 */
public enum Error {

    CONNECTION_ERROR("700501", "KAFKA:CONNECTION_ERROR"),
    INVALID_CONFIGURATION("700102", "FILE:INVALID_CONFIGURATION"),
    SERIALIZATION_ERROR("700503", "KAFKA:SERIALIZATION_ERROR"),
    AVRO_TYPE_ERROR("700504", "KAFKA:AVRO_TYPE_ERROR"),
    SCHEMA_BUILDER_ERROR("700505", "KAFKA:SCHEMA_BUILDER_ERROR"),
    SCHEMA_PARSE_ERROR("700506", "KAFKA:SCHEMA_PARSE_ERROR"),
    UNRESOLVED_UNION_ERROR("700507", "KAFKA:UNRESOLVED_UNION_ERROR"),
    AXIS_FAULT_ERROR("700508", "KAFKA:AXIS_FAULT_ERROR"),
    KAFKA_GENERAL_ERROR("700509", "KAFKA:KAFKA_GENERAL_ERROR");

    private final String code;
    private final String message;

    /**
     * Create an error code.
     *
     * @param code    error code represented by number
     * @param message error message
     */
    Error(String code, String message) {

        this.code = code;
        this.message = message;
    }

    public String getErrorCode() {

        return this.code;
    }

    public String getErrorDetail() {

        return this.message;
    }
}
