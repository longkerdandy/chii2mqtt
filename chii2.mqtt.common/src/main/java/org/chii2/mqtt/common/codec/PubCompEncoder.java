package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PubCompMessage;

/**
 * PUBCOMP Message Encoder
 */
public class PubCompEncoder extends BaseEncoder<PubCompMessage> {

    @Override
    protected void encodeVariableHeader(PubCompMessage message, ByteBuf out) {
        // Write Variable Header
        // Message ID
        out.writeShort(message.getMessageID());
    }

    @Override
    protected void encodePayload(PubCompMessage message, ByteBuf out) {
        // No Payload
    }
}
