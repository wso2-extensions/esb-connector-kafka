package org.wso2.carbon.connector;

import org.apache.synapse.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.axis2.AxisFault;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestCaseOfKalfkaConnectConstant {
    private MessageContext ctx;
    private KafkaConnectConstants kafkaConnectConstants;
    ConfigurationContext configContext;
    SynapseConfiguration synapseConfig;

    @BeforeMethod
    public void setUp() throws Exception {
        ctx = createMessageContext();
        kafkaConnectConstants = new KafkaConnectConstants();
    }

    /**
     * Test case to check the initializing Kafka broker constant properties.
     *
     * @throws Exception
     */
    @Test
    public void testAckValue() throws Exception {
        ctx.setProperty("acks", "2");
        Assert.assertEquals(ctx.getProperty(kafkaConnectConstants.ACK),"2");
    }

    /**
     * Create Axis2 Message Context.
     *
     * @return msgCtx created message context.
     * @throws AxisFault
     */
    private MessageContext createMessageContext() throws AxisFault {
        MessageContext msgCtx = createSynapseMessageContext();
        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(org.apache.axiom.om.util.UUIDGenerator.getUUID());

        return msgCtx;
    }

    /**
     * Create Synapse Context.
     *
     * @return mc created message context.
     * @throws AxisFault
     */
    private MessageContext createSynapseMessageContext() throws AxisFault {
        org.apache.axis2.context.MessageContext axis2MC = new org.apache.axis2.context.MessageContext();
        axis2MC.setConfigurationContext(this.configContext);
        org.apache.axis2.context.ServiceContext svcCtx = new org.apache.axis2.context.ServiceContext();
        org.apache.axis2.context.OperationContext
                opCtx = new org.apache.axis2.context.OperationContext(new org.apache.axis2.description.InOutAxisOperation(), svcCtx);
        axis2MC.setServiceContext(svcCtx);
        axis2MC.setOperationContext(opCtx);
        Axis2MessageContext mc = new Axis2MessageContext(axis2MC, this.synapseConfig, null);
        mc.setMessageID(org.apache.axiom.util.UIDGenerator.generateURNString());
        mc.setEnvelope(org.apache.axiom.om.OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope());
        mc.getEnvelope().addChild(org.apache.axiom.om.OMAbstractFactory.getSOAP12Factory().createSOAPBody());

        return mc;
    }
}