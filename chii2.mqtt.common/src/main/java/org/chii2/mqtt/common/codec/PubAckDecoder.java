package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PubAckMessage;

/**
 * PUBACK Message Decoder
 */
public class PubAckDecoder extends BaseDecoder<PubAckMessage> {

    @Override
    protected PubAckMessage createMessage() {
        return new PubAckMessage();
    }

    @Override
    protected void decodeVariableHeader(PubAckMessage message, ByteBuf in) {
        // Variable Header
        // Message ID
        message.setMessageID(in.readUnsignedShort());
    }

    @Override
    protected void decodePayload(PubAckMessage message, ByteBuf in) {
        // No Payload
    }
}
