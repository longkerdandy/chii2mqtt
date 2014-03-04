package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.common.message.SubscribeMessage;
import org.chii2.mqtt.common.utils.MQTTUtils;

/**
 * SUBSCRIBE Message Decoder
 */
public class SubscribeDecoder extends BaseDecoder<SubscribeMessage> {

    @Override
    protected SubscribeMessage createMessage() {
        return new SubscribeMessage();
    }

    @Override
    protected void decodeVariableHeader(SubscribeMessage message, ByteBuf in) {
        // Variable Header
        // Message ID
        message.setMessageID(in.readUnsignedShort());
    }

    @Override
    protected void decodePayload(SubscribeMessage message, ByteBuf in) {
        // Payload
        // Topics
        while (in.readableBytes() > 0) {
            String topic = MQTTUtils.decodeString("Topic Name", in);
            byte qos = (byte) (in.readByte() & 0x03);
            message.addTopic(new SubscribeMessage.Topic(topic, MQTTMessage.QoSLevel.values()[qos]));
        }
    }
}
