/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import kafka.javaapi.producer.Producer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;

import java.util.Vector;

/**
 * Connection pool manager for Kafka producer connection.
 */
public class KafkaConnectionPoolManager {
    private static Log log = LogFactory.getLog(KafkaConnectionPoolManager.class);

    private static KafkaConnectionPoolManager kafkaConnectionPoolManager = null;

    Vector<Producer<String, String>> connectionPool = new Vector<Producer<String, String>>();

    public KafkaConnectionPoolManager(MessageContext messageContext) {
        initialize(messageContext);
    }

    /**
     * Get single instance of ConnectionPoolManager.
     *
     * @param messageContext the message context
     * @return the connection pool manger
     */
    public static synchronized KafkaConnectionPoolManager getInstance(MessageContext messageContext) {
        if (kafkaConnectionPoolManager == null) {
            kafkaConnectionPoolManager = new KafkaConnectionPoolManager(messageContext);
        }
        return kafkaConnectionPoolManager;
    }

    /**
     * Initialize the connection pool
     *
     * @param messageContext the message context
     */
    private void initialize(MessageContext messageContext) {
        //Here we can initialize all the information that we need
        initializeConnectionPool(messageContext);
    }

    /**
     * Initialize the connection pool.
     *
     * @param messageContext the message context
     */
    private void initializeConnectionPool(MessageContext messageContext) {
        while (!checkIfConnectionPoolIsFull(messageContext)) {
            log.info("Connection Pool is NOT full. Proceeding with adding new connections");
            //Adding new connection instance until the pool is full
            connectionPool.addElement(createNewConnectionForPool(messageContext));
        }
        log.info("Connection Pool is full.");
    }

    /**
     * Check whether the connection pool is full.
     *
     * @return true or false
     */
    private synchronized boolean checkIfConnectionPoolIsFull(MessageContext messageContext) {
        final int MAX_POOL_SIZE = Integer.parseInt(messageContext
                .getProperty(KafkaConnectConstants.CONNECTION_POOL_MAX_SIZE).toString());
        if (log.isDebugEnabled()) {
            log.debug("Maximum pool size is :" + MAX_POOL_SIZE);
        }
        //Check if the pool size
        if (connectionPool.size() < MAX_POOL_SIZE) {
            return false;
        }
        return true;
    }

    /**
     * The ProducerConfig class encapsulates the values required for establishing the connection with brokers such
     * as the broker list, message partition class, serializer class for the message, and partition key,etc.
     *
     * @param messageContext the message context
     * @return the connection
     */
    private Producer<String, String> createNewConnectionForPool(MessageContext messageContext) {
        KafkaConnection kafkaConnection = new KafkaConnection();
        return kafkaConnection.createNewConnection(messageContext);
    }

    /**
     * Get the connection from connection pool.
     *
     * @return the connection
     */
    public synchronized Producer<String, String> getConnectionFromPool() {
        Producer<String, String> connection = null;

        //Check if there is a connection available. There are times when all the connections in the pool may be used up
        if (connectionPool.size() > 0) {
            if (connectionPool.firstElement() != null) {
                connection = connectionPool.firstElement();
            }
            connectionPool.removeElementAt(0);
        }
        //Giving away the connection from the connection pool
        return connection;
    }

    /**
     * Adding the connection from the client back to the connection pool.
     *
     * @param connection the connection
     */
    public synchronized void returnConnectionToPool(Producer<String, String> connection) {
        connectionPool.addElement(connection);
    }
}
