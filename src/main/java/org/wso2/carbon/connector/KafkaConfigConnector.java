/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.connector.connection.KafkaConnection;
import org.wso2.carbon.connector.connection.KafkaConnectionFactory;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.connection.ConnectionHandler;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.utils.KafkaConnectConstants;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.wso2.carbon.connector.core.util.ConnectorUtils.getPoolConfiguration;

/**
 * Kafka producer configuration.
 */
public class KafkaConfigConnector extends AbstractConnector implements ManagedLifecycle {
    private static final Lock lock = new ReentrantLock();

    @Override
    public void connect(MessageContext messageContext) {

        String schemaRegistryUrl = (String) messageContext
                .getProperty(KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_URL);

        if (StringUtils.isEmpty(schemaRegistryUrl)) {
            messageContext.setProperty(KafkaConnectConstants.KAFKA_SCHEMA_REGISTRY_URL,
                    KafkaConnectConstants.DEFAULT_SCHEMA_REGISTRY_URL);
        }

        try {
            createConnection(messageContext);
        } catch (Exception e) {
            handleException("Kafka producer connector: Failed to create the Kafka connection", e,
                    messageContext);
        }
    }

    /**
     * Creates a connection with the given configuration
     *
     * @param messageContext message context
     */
    private void createConnection(MessageContext messageContext) {

        String connectorName = KafkaConnectConstants.CONNECTOR_NAME;
        String connectionName = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                KafkaConnectConstants.NAME);
        String poolingEnabled = (String) messageContext.getProperty(KafkaConnectConstants.CONNECTION_POOLING_ENABLED);
        ConnectionHandler handler = ConnectionHandler.getConnectionHandler();

        if (!handler.checkIfConnectionExists(connectorName, connectionName)) {
            lock.lock();
            try {
                if (!handler.checkIfConnectionExists(connectorName, connectionName)) {
                    if (Boolean.parseBoolean(poolingEnabled)) {
                        handler.createConnection(connectorName, connectionName, new KafkaConnectionFactory(messageContext),
                                getPoolConfiguration(messageContext));
                    } else {
                        KafkaConnection connection = new KafkaConnection(messageContext);
                        handler.createConnection(connectorName, connectionName, connection);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Connection '" + connectionName + "' successfully created for connector '"
                                + connectorName + "'.");
                    }
                    return;
                }
            } finally {
                lock.unlock();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Connection '" + connectionName + "' successfully created for connector '" + connectorName + "'.");
        }
    }

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        // Nothing to do when initiating the connector
    }

    @Override
    public void destroy() {

        ConnectionHandler.getConnectionHandler().shutdownConnections(KafkaConnectConstants.CONNECTOR_NAME);
    }
}
