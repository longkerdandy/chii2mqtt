package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PingRespMessage;

/**
 * PINGRESP Message Encoder
 */
public class PingRespEncoder extends BaseEncoder<PingRespMessage> {

    @Override
    protected void encodeVariableHeader(PingRespMessage message, ByteBuf out) {
        // No Variable Header
    }

    @Override
    protected void encodePayload(PingRespMessage message, ByteBuf out) {
        // No Payload
    }
}
