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
package org.wso2.carbon.connector.connection;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.pool.ConnectionFactory;

/**
 * Kafka Connection Factory used to initialize the Kafka Connection Pool
 */
public class KafkaConnectionFactory implements ConnectionFactory {

    private MessageContext messageContext;

    public KafkaConnectionFactory(MessageContext messageContext) {

        this.messageContext = messageContext;
    }

    @Override
    public KafkaConnection makeObject() {

        return new KafkaConnection(messageContext);
    }

    @Override
    public void destroyObject(Object connection) {

        ((KafkaConnection) connection).disconnect();
    }

    @Override
    public boolean validateObject(Object connection) {

        // There is no way we can validate whether the connection is valid or not.
        return true;
    }

    @Override
    public void activateObject(Object connection) {
        // Nothing to do here
    }

    @Override
    public void passivateObject(Object o) {
        // Nothing to do here
    }
}
