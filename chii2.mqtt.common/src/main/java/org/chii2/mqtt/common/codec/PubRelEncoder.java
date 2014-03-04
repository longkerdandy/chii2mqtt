package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PubRelMessage;

/**
 * PUBREL Message Encoder
 */
public class PubRelEncoder extends BaseEncoder<PubRelMessage> {

    @Override
    protected void encodeVariableHeader(PubRelMessage message, ByteBuf out) {
        // Write Variable Header
        // Message ID
        out.writeShort(message.getMessageID());
    }

    @Override
    protected void encodePayload(PubRelMessage message, ByteBuf out) {
        // No Payload
    }
}
