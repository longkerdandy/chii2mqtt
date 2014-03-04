package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PingRespMessage;

/**
 * PINGRESP Message Decoder
 */
public class PingRespDecoder extends BaseDecoder<PingRespMessage> {

    @Override
    protected PingRespMessage createMessage() {
        return new PingRespMessage();
    }

    @Override
    protected void decodeVariableHeader(PingRespMessage message, ByteBuf in) {
        // No Variable Header
    }

    @Override
    protected void decodePayload(PingRespMessage message, ByteBuf in) {
        // No Payload
    }
}
