package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PubAckMessage;

/**
 * PUBACK Mesage Encoder
 */
public class PubAckEncoder extends BaseEncoder<PubAckMessage> {

    @Override
    protected void encodeVariableHeader(PubAckMessage message, ByteBuf out) {
        // Write Variable Header
        // Message ID
        out.writeShort(message.getMessageID());
    }

    @Override
    protected void encodePayload(PubAckMessage message, ByteBuf out) {
        // No Payload
    }
}
