package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.ConnAckMessage;

/**
 * CONNACK Message Decoder
 */
public class ConnAckDecoder extends BaseDecoder<ConnAckMessage> {

    @Override
    protected ConnAckMessage createMessage() {
        return new ConnAckMessage();
    }

    @Override
    protected void decodeVariableHeader(ConnAckMessage message, ByteBuf in) {
        // Variable Header
        // Skip reserved byte
        in.skipBytes(1);
        // Return code
        message.setReturnCode(ConnAckMessage.ReturnCode.values()[in.readByte()]);
    }

    @Override
    protected void decodePayload(ConnAckMessage message, ByteBuf in) {
        // No Payload
    }
}
