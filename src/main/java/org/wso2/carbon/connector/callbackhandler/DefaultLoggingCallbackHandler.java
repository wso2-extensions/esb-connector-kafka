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

public class DefaultLoggingCallbackHandler implements KafkaSendCallbackHandler {

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception, Producer<Object, Object> producer,
                             ProducerRecord<Object, Object> record, String dlqTopic) {
        if (exception == null) {
            log.info("Message successfully sent to topic " + metadata.topic() + " at offset "
                    + metadata.offset());
        } else {
            log.error("Failed to send message. Details:\n"
                    + "  Topic           = " + record.topic() + "\n"
                    + "  Partition Number= " + record.partition() + "\n"
                    + "  Key             = " + record.key() + "\n"
                    + "  Message         = " + record.value() + "\n"
                    + "  Exception       = " + exception.getMessage());
        }

    }
}
