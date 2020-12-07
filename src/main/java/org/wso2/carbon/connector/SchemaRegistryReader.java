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

import org.apache.avro.Schema;
import org.apache.synapse.SynapseException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class to connect to Schema Registry and retrieve schema using the schema id.
 */
public class SchemaRegistryReader {

    /**
     * Method to get the schema from the confluence schema registry.
     *
     * @param registryURL The confluence schema registry url.
     * @param schemaID    The schema id.
     * @return The schema.
     */
    public static Schema getSchemaFromID(String registryURL, String schemaID) {
        String requestPath = "/schemas/ids/";
        String jsonSchema;
        try {
            URL url = new URL(registryURL + requestPath + schemaID);
            jsonSchema = new JSONObject(doGet(url)).getString("schema");
        } catch (IOException e) {
            throw new SynapseException("Error obtaining the schema from the confluence schema registry", e);
        }
        return new Schema.Parser().parse(jsonSchema);
    }

    /**
     * Method to send http get request.
     *
     * @param url The endpoint url.
     * @return The response.
     * @throws IOException On error.
     */
    public static String doGet(URL url) throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            try {
                urlConnection.setRequestMethod("GET");
            } catch (ProtocolException e) {

            }
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setAllowUserInteraction(false);
            urlConnection.setReadTimeout(10000);
            for (Map.Entry<String, String> e : headers.entrySet()) {
                urlConnection.setRequestProperty(e.getKey(), e.getValue());
            }

            StringBuilder sb = new StringBuilder();

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.defaultCharset()))) {

                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            }
            Iterator<String> itr = urlConnection.getHeaderFields().keySet().iterator();
            Object responseHeaders = new HashMap();
            String key;
            while (itr.hasNext()) {
                key = itr.next();
                if (key != null) {
                    ((Map) responseHeaders).put(key, urlConnection.getHeaderField(key));
                }
            }
            return sb.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
