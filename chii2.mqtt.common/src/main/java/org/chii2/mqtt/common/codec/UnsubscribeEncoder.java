package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.UnsubscribeMessage;
import org.chii2.mqtt.common.utils.MQTTUtils;

/**
 * UNSUBSCRIBE Message Encoder
 */
public class UnsubscribeEncoder extends BaseEncoder<UnsubscribeMessage> {

    @Override
    protected void encodeVariableHeader(UnsubscribeMessage message, ByteBuf out) {
        // Write Variable Header
        // Message ID
        out.writeShort(message.getMessageID());
    }

    @Override
    protected void encodePayload(UnsubscribeMessage message, ByteBuf out) {
        // Write Payload
        // Topics
        for (String topic : message.getTopicNames()) {
            out.writeBytes(MQTTUtils.encodeString("Topic Name", topic));
        }
    }
}
