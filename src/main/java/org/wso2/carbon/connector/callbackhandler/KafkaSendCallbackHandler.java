/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.connector.callbackhandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public interface KafkaSendCallbackHandler {

    Log log = LogFactory.getLog(KafkaSendCallbackHandler.class);

    /**
     * Called when the Kafka message send operation completes.
     *
     * @param metadata   Record metadata if send was successful; null otherwise.
     * @param exception  Exception thrown during send; null if successful.
     * @param producer   Kafka producer instance for optional retry or DLQ.
     * @param record     The original record that was sent.
     * @param dlqTopic   Optional DLQ topic to which user can redirect the failed message.
     */
    void onCompletion(RecordMetadata metadata, Exception exception, Producer<Object, Object> producer,
                      ProducerRecord<Object, Object> record, String dlqTopic);

}
