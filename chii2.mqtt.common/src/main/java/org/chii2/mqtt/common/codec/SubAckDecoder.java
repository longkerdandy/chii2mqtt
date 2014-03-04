package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.common.message.SubAckMessage;

/**
 * SUBACK Message Decoder
 */
public class SubAckDecoder extends BaseDecoder<SubAckMessage> {

    @Override
    protected SubAckMessage createMessage() {
        return new SubAckMessage();
    }

    @Override
    protected void decodeVariableHeader(SubAckMessage message, ByteBuf in) {
        // Variable Header
        // Message ID
        message.setMessageID(in.readUnsignedShort());
    }

    @Override
    protected void decodePayload(SubAckMessage message, ByteBuf in) {
        // Payload
        // Topics
        while (in.readableBytes() > 0) {
            byte qos = in.readByte();
            message.addGrantedQoS(MQTTMessage.QoSLevel.values()[qos]);
        }
    }
}
