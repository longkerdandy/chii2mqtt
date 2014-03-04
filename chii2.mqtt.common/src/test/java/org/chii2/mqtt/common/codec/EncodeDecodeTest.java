package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.chii2.mqtt.common.message.*;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Encode & Decode Test
 */
public class EncodeDecodeTest {

    @Test
    public void pingReqTest() {
        // Encode
        ByteBuf pingReqBuf = Unpooled.buffer();
        PingReqMessage pingReq = new PingReqMessage();
        PingReqEncoder pingReqEncoder = new PingReqEncoder();
        pingReqEncoder.encode(null, pingReq, pingReqBuf);
        // Decode
        List<Object> pingReqOutput = new ArrayList<>();
        PingReqDecoder pingReqDecoder = new PingReqDecoder();
        pingReqDecoder.decode(null, pingReqBuf, pingReqOutput);

        assert pingReqOutput.size() == 1;
        assert pingReqOutput.get(0).getClass() == PingReqMessage.class;
    }

    @Test
    public void connectTest() {
        // Encode
        ByteBuf buf = Unpooled.buffer();
        ConnectMessage message = new ConnectMessage.Builder("Chii2 Client")
                .keepAlive(10)
                .cleanSession(true)
                .willQoS(MQTTMessage.QoSLevel.LEAST_ONCE)
                .willTopic("/Chii2/Server")
                .willMessage("A Test Message")
                .userName("longkerdandy").build();
        ConnectEncoder encoder = new ConnectEncoder();
        encoder.encode(null, message, buf);
        // Decode
        List<Object> output = new ArrayList<>();
        ConnectDecoder decoder = new ConnectDecoder();
        decoder.decode(null, buf, output);

        assert output.size() == 1;
        assert output.get(0).getClass() == ConnectMessage.class;
        ConnectMessage connectMessage = (ConnectMessage) output.get(0);
        assert connectMessage.isUserNameFlag();
        assert !connectMessage.isPasswordFlag();
        assert !connectMessage.isWillRetain();
        assert connectMessage.getWillQoS() == MQTTMessage.QoSLevel.LEAST_ONCE;
        assert connectMessage.isWillFlag();
        assert connectMessage.isCleanSession();
        assert connectMessage.getKeepAlive() == 10;
        assert connectMessage.getClientID().equals("Chii2 Client");
        assert connectMessage.getWillTopic().equals("/Chii2/Server");
        assert connectMessage.getWillMessage().equals("A Test Message");
        assert connectMessage.getUserName().equals("longkerdandy");
        assert connectMessage.getPassword() == null;
    }

    @Test
    public void connackTest() {
        // Encode
        ByteBuf buf = Unpooled.buffer();
        ConnAckMessage message = new ConnAckMessage(ConnAckMessage.ReturnCode.NOT_AUTHORIZED);
        ConnAckEncoder encoder = new ConnAckEncoder();
        encoder.encode(null, message, buf);
        // Decode
        List<Object> output = new ArrayList<>();
        ConnAckDecoder decoder = new ConnAckDecoder();
        decoder.decode(null, buf, output);

        assert output.size() == 1;
        assert output.get(0).getClass() == ConnAckMessage.class;
        ConnAckMessage connAckMessage = (ConnAckMessage) output.get(0);
        assert connAckMessage.getReturnCode() == ConnAckMessage.ReturnCode.NOT_AUTHORIZED;
    }

    @Test
    public void publishTest() throws UnsupportedEncodingException {
        // Encode
        ByteBuf buf = Unpooled.buffer();
        ByteBuffer content = ByteBuffer.allocate(100);
        content.put("A very long content. Blah blah blah blah blah!".getBytes("UTF-8"));
        PublishMessage message = new PublishMessage(true, MQTTMessage.QoSLevel.EXACTLY_ONCE, false, "/Chii2/Client", 77, content);
        PublishEncoder encoder = new PublishEncoder();
        encoder.encode(null, message, buf);
        // Decode
        List<Object> output = new ArrayList<>();
        PublishDecoder decoder = new PublishDecoder();
        decoder.decode(null, buf, output);

        assert output.size() == 1;
        assert output.get(0).getClass() == PublishMessage.class;
        PublishMessage publishMessage = (PublishMessage) output.get(0);
        assert publishMessage.isRetain();
        assert publishMessage.getQosLevel() == MQTTMessage.QoSLevel.EXACTLY_ONCE;
        assert !publishMessage.isDupFlag();
        assert publishMessage.getTopicName().equals("/Chii2/Client");
        assert publishMessage.getMessageID() == 77;
        ByteBuffer result = publishMessage.getContent();
        assert "A very long content. Blah blah blah blah blah!".equals(new String(result.array(), "UTF-8"));
    }

    @Test
    public void subscribeTest() {
        // Encode
        ByteBuf buf = Unpooled.buffer();
        List<SubscribeMessage.Topic> topics = new ArrayList<>();
        topics.add(new SubscribeMessage.Topic("/Chii2/Miao", MQTTMessage.QoSLevel.LEAST_ONCE));
        topics.add(new SubscribeMessage.Topic("/Chii2/Wang", MQTTMessage.QoSLevel.EXACTLY_ONCE));
        SubscribeMessage message = new SubscribeMessage(true, 3000, topics);
        SubscribeEncoder encoder = new SubscribeEncoder();
        encoder.encode(null, message, buf);
        // Decode
        List<Object> output = new ArrayList<>();
        SubscribeDecoder decoder = new SubscribeDecoder();
        decoder.decode(null, buf, output);

        assert output.size() == 1;
        assert output.get(0).getClass() == SubscribeMessage.class;
        SubscribeMessage subscribeMessage = (SubscribeMessage) output.get(0);
        assert subscribeMessage.isDupFlag();
        assert subscribeMessage.getMessageID() == 3000;
        assert subscribeMessage.getTopics().get(0).getTopicName().equals("/Chii2/Miao");
        assert subscribeMessage.getTopics().get(0).getQosLevel() == MQTTMessage.QoSLevel.LEAST_ONCE;
        assert subscribeMessage.getTopics().get(1).getTopicName().equals("/Chii2/Wang");
        assert subscribeMessage.getTopics().get(1).getQosLevel() == MQTTMessage.QoSLevel.EXACTLY_ONCE;
    }
}
