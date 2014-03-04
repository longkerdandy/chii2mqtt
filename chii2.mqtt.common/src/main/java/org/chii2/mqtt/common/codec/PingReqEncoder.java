package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PingReqMessage;

/**
 * PINGREQ Message Encoder
 */
public class PingReqEncoder extends BaseEncoder<PingReqMessage> {

    @Override
    protected void encodeVariableHeader(PingReqMessage message, ByteBuf out) {
        // No Variable Header
    }

    @Override
    protected void encodePayload(PingReqMessage message, ByteBuf out) {
        // No Payload
    }
}
