package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PubCompMessage;

/**
 * PUBCOMP Message Decoder
 */
public class PubCompDecoder extends BaseDecoder<PubCompMessage> {

    @Override
    protected PubCompMessage createMessage() {
        return new PubCompMessage();
    }

    @Override
    protected void decodeVariableHeader(PubCompMessage message, ByteBuf in) {
        // Variable Header
        // Message ID
        message.setMessageID(in.readUnsignedShort());
    }

    @Override
    protected void decodePayload(PubCompMessage message, ByteBuf in) {
        // No Payload
    }
}
