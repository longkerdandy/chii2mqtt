package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PubRecMessage;

/**
 * PUBREC Message Encoder
 */
public class PubRecEncoder extends BaseEncoder<PubRecMessage> {

    @Override
    protected void encodeVariableHeader(PubRecMessage message, ByteBuf out) {
        // Write Variable Header
        // Message ID
        out.writeShort(message.getMessageID());
    }

    @Override
    protected void encodePayload(PubRecMessage message, ByteBuf out) {
        // No Payload
    }
}
