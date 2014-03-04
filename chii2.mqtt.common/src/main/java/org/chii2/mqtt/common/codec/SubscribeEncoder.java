package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.SubscribeMessage;
import org.chii2.mqtt.common.utils.MQTTUtils;

/**
 * SUBSCRIBE Message Encoder
 */
public class SubscribeEncoder extends BaseEncoder<SubscribeMessage> {

    @Override
    protected void encodeVariableHeader(SubscribeMessage message, ByteBuf out) {
        // Write Variable Header
        // Message ID
        out.writeShort(message.getMessageID());
    }

    @Override
    protected void encodePayload(SubscribeMessage message, ByteBuf out) {
        // Write Payload
        // Topics
        for (SubscribeMessage.Topic topic : message.getTopics()) {
            out.writeBytes(MQTTUtils.encodeString("Topic Name", topic.getTopicName()));
            out.writeByte(topic.getQosLevel().byteValue());
        }
    }
}
