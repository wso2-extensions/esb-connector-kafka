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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.synapse.MessageContext;

import java.util.Vector;

/**
 * Connection pool for Kafka producer connection.
 */
public class KafkaConnectionPool {
    private static Log log = LogFactory.getLog(KafkaConnectionPool.class);

    private static Vector<KafkaProducer<String, String>> connectionPool = new Vector<>();

    /**
     * Initialize the connection pool.
     *
     * @param messageContext the message context.
     */
    public static void initialize(MessageContext messageContext) {
        //Here we can initialize all the information that we need
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
     * @return true or false.
     */
    private static synchronized boolean checkIfConnectionPoolIsFull(MessageContext messageContext) {
        final int MAX_POOL_SIZE = Integer
                .parseInt(messageContext.getProperty(KafkaConnectConstants.CONNECTION_POOL_MAX_SIZE).toString());
        if (log.isDebugEnabled()) {
            log.debug("Maximum pool size is :" + MAX_POOL_SIZE);
        }
        //Check if the pool size
        return connectionPool.size() >= MAX_POOL_SIZE;
    }

    /**
     * The ProducerConfig class encapsulates the values required for establishing the connection with brokers such
     * as the broker list, message partition class, serializer class for the message, and partition key,etc.
     *
     * @param messageContext the message context.
     * @return the connection.
     */
    private static KafkaProducer createNewConnectionForPool(MessageContext messageContext) {
        KafkaConnection kafkaConnection = new KafkaConnection();
        return kafkaConnection.createNewConnection(messageContext);
    }

    /**
     * Get the connection from connection pool.
     *
     * @return the connection.
     */
    public static synchronized KafkaProducer<String, String> getConnectionFromPool() {
        KafkaProducer<String, String> connection = null;

        //Check if there is a connection available. There are times when all the connections in the pool may be used up
        if (connectionPool.size() > 0) {
            connection = connectionPool.remove(0);
        }
        //Giving away the connection from the connection pool
        return connection;
    }

    /**
     * Adding the connection from the client back to the connection pool.
     *
     * @param connection the connection.
     */
    public static synchronized void returnConnectionToPool(KafkaProducer<String, String> connection) {
        connectionPool.addElement(connection);
    }
}