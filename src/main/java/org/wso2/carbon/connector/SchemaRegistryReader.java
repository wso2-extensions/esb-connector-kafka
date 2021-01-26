/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.avro.Schema;
import org.apache.synapse.SynapseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to connect to Schema Registry and retrieve schema using the schema id.
 */
public class SchemaRegistryReader {

    /**
     * Method to get the schema from the confluence schema registry.
     *
     * @param registryURL The confluence schema registry url.
     * @param authSource The authentication source type if schema resgistry is secured.
     * @param authCredentials The credentials for authentication
     * @param schemaID    The schema id.
     * @return The schema.
     */
    public static Schema getSchemaFromID(String registryURL, String authSource, String authCredentials, String schemaID) {

        Map<String, String> headers = new HashMap<>();
        headers.put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, authSource);
        headers.put(KafkaAvroSerializerConfig.USER_INFO_CONFIG, authCredentials);
        Schema jsonSchema;
        try {
            RestService service = new RestService(registryURL);

            CachedSchemaRegistryClient client = new CachedSchemaRegistryClient(service, 1000, headers);
            jsonSchema =  client.getById(Integer.parseInt(schemaID));

        } catch (RestClientException e) {
            throw new SynapseException("Schema not found or error in obtaining the schema from the confluence schema registry", e);
        } catch (IOException e) {
            throw new SynapseException("Error obtaining the schema from the confluence schema registry", e);
        }
        return jsonSchema;
    }
}
