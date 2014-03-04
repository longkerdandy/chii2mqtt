package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PubRecMessage;

/**
 * PUBREC Message Decoder
 */
public class PubRecDecoder extends BaseDecoder<PubRecMessage> {

    @Override
    protected PubRecMessage createMessage() {
        return new PubRecMessage();
    }

    @Override
    protected void decodeVariableHeader(PubRecMessage message, ByteBuf in) {
        // Variable Header
        // Message ID
        message.setMessageID(in.readUnsignedShort());
    }

    @Override
    protected void decodePayload(PubRecMessage message, ByteBuf in) {
        // No Payload
    }
}
