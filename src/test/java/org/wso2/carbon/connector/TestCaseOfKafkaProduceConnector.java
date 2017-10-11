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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.mediators.template.TemplateContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.connector.core.ConnectException;

public class TestCaseOfKafkaProduceConnector {
    ConfigurationContext configContext;
    SynapseConfiguration synapseConfig;
    private org.apache.synapse.MessageContext ctx;
    private KafkaConnectionPool kafkaConnectionPool;
    private KafkaConnectConstants kafkaConnectConstants;
    private KafkaConfigConnector kafkaConfigConnector;
    private KafkaProduceConnector kafkaProduceConnector;
    private org.apache.synapse.mediators.template.TemplateContext templateContext;
    private java.util.Stack functionStack;

    @BeforeMethod
    public void setUp() throws Exception {
        kafkaConfigConnector = new KafkaConfigConnector();
        kafkaProduceConnector = new KafkaProduceConnector();
        kafkaConnectionPool = new KafkaConnectionPool();
        ctx = createMessageContext();
    }

    @Test
    public void testKafakaProduceConnector() throws ConnectException, AxisFault {
        ctx.setProperty("kafka.maxPoolSize", "20");
        ctx.setProperty(KafkaConnectConstants.KAFKA_KEY_SERIALIZER_CLASS,
                "org.apache.kafka.common.serialization.StringSerializer");
        ctx.setProperty(KafkaConnectConstants.KAFKA_VALUE_SERIALIZER_CLASS,
                "org.apache.kafka.common.serialization.StringSerializer");
        ctx.setProperty(KafkaConnectConstants.KAFKA_ACKS, "all");
        ctx.setProperty(KafkaConnectConstants.KAFKA_COMPRESSION_TYPE, "gzip");
        ctx.setProperty(KafkaConnectConstants.KAFKA_RETRIES, "2");
        ctx.setProperty(KafkaConnectConstants.KAFKA_BUFFER_MEMORY, "20");

        ctx.setProperty(KafkaConnectConstants.KAFKA_BATCH_SIZE, "1");
        ctx.setProperty(KafkaConnectConstants.KAFKA_CLIENT_ID, "123");
        ctx.setProperty(KafkaConnectConstants.KAFKA_CONNECTION_MAX_IDLE_TIME, "3");
        ctx.setProperty(KafkaConnectConstants.KAFKA_LINGER_TIME, "3");
        ctx.setProperty(KafkaConnectConstants.KAFKA_MAXIMUM_BLOCK, "4");
        ctx.setProperty(KafkaConnectConstants.KAFKA_MAXIMUM_REQUEST_SIZE, "5");
        ctx.setProperty(KafkaConnectConstants.KAFKA_PARTITIONER_CLASS,
                "org.apache.kafka.clients.producer.internals.DefaultPartitioner");
        ctx.setProperty(KafkaConnectConstants.KAFKA_RECEIVE_BUFFER_BYTES, "8");
        ctx.setProperty(KafkaConnectConstants.KAFKA_REQUEST_TIMEOUT_MS, "3");
        ctx.setProperty(KafkaConnectConstants.KAFKA_SEND_BUFFER_BYTES, "8");

        ctx.setProperty(KafkaConnectConstants.KAFKA_TIMEOUT_TIME, "3");
        ctx.setProperty(KafkaConnectConstants.KAFKA_BLOCK_ON_BUFFER_FULL, "3");
        ctx.setProperty(KafkaConnectConstants.KAFKA_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "2");
        ctx.setProperty(KafkaConnectConstants.KAFKA_METADATA_FETCH_TIMEOUT, "3");
        ctx.setProperty(KafkaConnectConstants.KAFKA_METADATA_MAXIMUM_AGE, "4");
        ctx.setProperty(KafkaConnectConstants.KAFKA_METRIC_REPORTERS, "");
        ctx.setProperty(KafkaConnectConstants.KAFKA_METRICS_NUM_SAMPLES, "2");
        ctx.setProperty(KafkaConnectConstants.KAFKA_METRICS_SAMPLE_WINDOW, "2");
        ctx.setProperty(KafkaConnectConstants.KAFKA_RETRY_BACKOFF_TIME, "3");
        ctx.setProperty(KafkaConnectConstants.KAFKA_RECONNECT_BACKOFF_TIME, "3");

        ((org.apache.synapse.core.axis2.Axis2MessageContext) ctx).getAxis2MessageContext()
                .setProperty(KafkaConnectConstants.KAFKA_BROKER_LIST, "localhost:9092");

        templateContext = new TemplateContext("authenticate", null);
        templateContext.getMappedValues().put(KafkaConnectConstants.PARAM_TOPIC, "test");
        templateContext.getMappedValues().put(KafkaConnectConstants.PARTITION_NO, "2");
        functionStack = new java.util.Stack();
        functionStack.push(templateContext);
        ctx.setProperty("_SYNAPSE_FUNCTION_STACK", functionStack);

        kafkaConfigConnector.connect(ctx);
        kafkaProduceConnector.connect(ctx);
    }

    /**
     * Create Axis2 Message Context.
     *
     * @return msgCtx created message context.
     * @throws org.apache.axis2.AxisFault
     */
    private MessageContext createMessageContext() throws AxisFault {
        MessageContext msgCtx = createSynapseMessageContext();
        org.apache.axis2.context.MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx)
                .getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(org.apache.axiom.om.util.UUIDGenerator.getUUID());

        return msgCtx;
    }

    /**
     * Create Synapse Context.
     *
     * @return mc created message context.
     * @throws org.apache.axis2.AxisFault
     */
    private org.apache.synapse.MessageContext createSynapseMessageContext() throws org.apache.axis2.AxisFault {
        org.apache.axis2.context.MessageContext axis2MC = new org.apache.axis2.context.MessageContext();
        axis2MC.setConfigurationContext(this.configContext);
        org.apache.axis2.context.ServiceContext svcCtx = new org.apache.axis2.context.ServiceContext();
        org.apache.axis2.context.OperationContext opCtx = new org.apache.axis2.context.OperationContext(
                new org.apache.axis2.description.InOutAxisOperation(), svcCtx);
        axis2MC.setServiceContext(svcCtx);
        axis2MC.setOperationContext(opCtx);
        org.apache.synapse.core.axis2.Axis2MessageContext mc = new org.apache.synapse.core.axis2.Axis2MessageContext(
                axis2MC, this.synapseConfig, null);
        mc.setMessageID(org.apache.axiom.util.UIDGenerator.generateURNString());
        mc.setEnvelope(org.apache.axiom.om.OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope());
        mc.getEnvelope().addChild(org.apache.axiom.om.OMAbstractFactory.getSOAP12Factory().createSOAPBody());

        return mc;
    }
}