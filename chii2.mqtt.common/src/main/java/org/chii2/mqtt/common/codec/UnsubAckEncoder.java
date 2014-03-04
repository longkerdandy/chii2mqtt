package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.UnsubAckMessage;

/**
 * UNSUBACK Message Encoder
 */
public class UnsubAckEncoder extends BaseEncoder<UnsubAckMessage> {

    @Override
    protected void encodeVariableHeader(UnsubAckMessage message, ByteBuf out) {
        // Write Variable Header
        // Message ID
        out.writeShort(message.getMessageID());
    }

    @Override
    protected void encodePayload(UnsubAckMessage message, ByteBuf out) {
        // No Payload
    }
}
