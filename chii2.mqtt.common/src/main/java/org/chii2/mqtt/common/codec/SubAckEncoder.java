package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.common.message.SubAckMessage;

/**
 * SUBAVC Message Encoder
 */
public class SubAckEncoder extends BaseEncoder<SubAckMessage> {

    @Override
    protected void encodeVariableHeader(SubAckMessage message, ByteBuf out) {
        // Write Variable Header
        // Message ID
        out.writeShort(message.getMessageID());
    }

    @Override
    protected void encodePayload(SubAckMessage message, ByteBuf out) {
        // Write Payload
        // QoS Levels
        for (MQTTMessage.QoSLevel qos : message.getGrantedQoS()) {
            out.writeByte(qos.byteValue());
        }
    }
}
