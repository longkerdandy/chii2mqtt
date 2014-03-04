package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.DisconnectMessage;

/**
 * DISCONNECT Message Decoder
 */
public class DisconnectDecoder extends BaseDecoder<DisconnectMessage> {

    @Override
    protected DisconnectMessage createMessage() {
        return new DisconnectMessage();
    }

    @Override
    protected void decodeVariableHeader(DisconnectMessage message, ByteBuf in) {
        // No Variable Header
    }

    @Override
    protected void decodePayload(DisconnectMessage message, ByteBuf in) {
        // No Payload
    }
}
