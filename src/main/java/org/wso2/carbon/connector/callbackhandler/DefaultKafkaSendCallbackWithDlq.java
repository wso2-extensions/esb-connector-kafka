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

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class DefaultKafkaSendCallbackWithDlq implements KafkaSendCallbackHandler {

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception, Producer<Object, Object> producer,
                             ProducerRecord<Object, Object> record, String dlqTopic) {
        if (exception == null) {
            if (log.isDebugEnabled()) {
                log.debug("Message successfully sent to topic " + metadata.topic() + " at offset "
                        + metadata.offset());
            }
        } else {
            log.error("Redirecting message to DLQ topic: " + dlqTopic + "due to send failure. Details of original message:\n"
                    + "  Topic           = " + record.topic() + "\n"
                    + "  Partition Number= " + record.partition() + "\n"
                    + "  Key             = " + record.key() + "\n"
                    + "  Message         = " + record.value() + "\n"
                    + "  Exception       = " + exception.getMessage());

            // Create DLQ record
            ProducerRecord<Object, Object> dlqRecord = new ProducerRecord<>(dlqTopic, record.key(), record.value());

            // Optionally preserve headers
            if (record.headers() != null) {
                record.headers().forEach(h -> dlqRecord.headers().add(h));
            }

            // Send to DLQ
            producer.send(dlqRecord, (recordMetadata, ex) -> {
                if (ex != null) {
                    log.error("Failed to send to DLQ: Details:\n"
                            + "  Topic           = " + dlqTopic + "\n"
                            + "  Partition Number= " + dlqRecord.partition() + "\n"
                            + "  Key             = " + dlqRecord.key() + "\n"
                            + "  Message         = " + dlqRecord.value() + "\n"
                            + "  Exception       = " + exception.getMessage());
                }
            });
        }
    }
}
