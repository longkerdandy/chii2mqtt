package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.PingReqMessage;

/**
 * PINGREQ Message Decoder
 */
public class PingReqDecoder extends BaseDecoder<PingReqMessage> {

    @Override
    protected PingReqMessage createMessage() {
        return new PingReqMessage();
    }

    @Override
    protected void decodeVariableHeader(PingReqMessage message, ByteBuf in) {
        // No Variable Header
    }

    @Override
    protected void decodePayload(PingReqMessage message, ByteBuf in) {
        // No Payload
    }
}
