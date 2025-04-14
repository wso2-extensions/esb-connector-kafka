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

package org.wso2.carbon.connector;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.synapse.SynapseLog;

/**
 * KafkaProducerHandler is a handler class that implements the Kafka Callback interface to handle
 * the result of the message send operation. Upon completion of the Kafka message send operation,
 * the callback method {@link #onCompletion(RecordMetadata, Exception)} is invoked to handle success
 * and failure cases accordingly.
 */
public class KafkaProducerHandler implements Callback {

    private final String topic;
    private final Integer partitionNo;
    private final Object key;
    private final Object message;
    private final org.apache.kafka.common.header.Headers headers;
    private final SynapseLog log;

    public KafkaProducerHandler(String topic, Integer partitionNo, Object key, Object message,
                                org.apache.kafka.common.header.Headers headers, SynapseLog log) {
        this.topic = topic;
        this.partitionNo = partitionNo;
        this.key = key;
        this.message = message;
        this.headers = headers;
        this.log = log;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            if (log.isDebugEnabled()) {
                log.auditDebug("Message successfully sent to topic " + metadata.topic() + " at offset "
                        + metadata.offset());
            }
        } else {
            log.auditError("Error while sending message with the following details: Topic=" + topic
                    + ", Partition Number=" + partitionNo + ", Key=" + key + ", Message=" + message + ", Headers="
                    + headers + ", Exception=" + exception.getMessage());
        }
    }
}
