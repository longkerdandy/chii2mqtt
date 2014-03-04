package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.DisconnectMessage;

/**
 * DISCONNECT Message Encoder
 */
public class DisconnectEncoder extends BaseEncoder<DisconnectMessage> {

    @Override
    protected void encodeVariableHeader(DisconnectMessage message, ByteBuf out) {
        // No Variable Header
    }

    @Override
    protected void encodePayload(DisconnectMessage message, ByteBuf out) {
        // No Payload
    }
}
