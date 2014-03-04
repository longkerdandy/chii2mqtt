package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.UnsubAckMessage;

/**
 * UNSUBACK Message Decoder
 */
public class UnsubAckDecoder extends BaseDecoder<UnsubAckMessage> {

    @Override
    protected UnsubAckMessage createMessage() {
        return new UnsubAckMessage();
    }

    @Override
    protected void decodeVariableHeader(UnsubAckMessage message, ByteBuf in) {
        // Variable Header
        // Message ID
        message.setMessageID(in.readUnsignedShort());
    }

    @Override
    protected void decodePayload(UnsubAckMessage message, ByteBuf in) {
        // No Payload
    }
}
