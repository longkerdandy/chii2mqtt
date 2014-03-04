package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.ConnAckMessage;

/**
 * CONNACK Message Encoder
 */
public class ConnAckEncoder extends BaseEncoder<ConnAckMessage> {

    @Override
    protected void encodeVariableHeader(ConnAckMessage message, ByteBuf out) {
        // Write Variable Header
        // Reserved
        out.writeByte(0);
        // Return Code
        out.writeByte(message.getReturnCode().byteValue());
    }

    @Override
    protected void encodePayload(ConnAckMessage message, ByteBuf out) {
        // No Payload
    }
}
