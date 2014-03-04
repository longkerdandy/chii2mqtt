package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PubRelMessage;

/**
 * PUBREL Message Decoder
 */
public class PubRelDecoder extends BaseDecoder<PubRelMessage> {

    @Override
    protected PubRelMessage createMessage() {
        return new PubRelMessage();
    }

    @Override
    protected void decodeVariableHeader(PubRelMessage message, ByteBuf in) {
        // Variable Header
        // Message ID
        message.setMessageID(in.readUnsignedShort());
    }

    @Override
    protected void decodePayload(PubRelMessage message, ByteBuf in) {
        // No Payload
    }
}
