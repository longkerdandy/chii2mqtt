package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.common.message.PublishMessage;
import org.chii2.mqtt.common.utils.MQTTUtils;

/**
 * PUBLISH Message Decoder
 */
public class PublishDecoder extends BaseDecoder<PublishMessage> {

    @Override
    protected PublishMessage createMessage() {
        return new PublishMessage();
    }

    @Override
    protected void decodeVariableHeader(PublishMessage message, ByteBuf in) {
        // Variable Header
        // Topic Name
        message.setTopicName(MQTTUtils.decodeString("Topic Name", in));
        // Message ID
        if (message.getQosLevel() == MQTTMessage.QoSLevel.LEAST_ONCE ||
                message.getQosLevel() == MQTTMessage.QoSLevel.EXACTLY_ONCE) {
            message.setMessageID(in.readUnsignedShort());
        }
    }

    @Override
    protected void decodePayload(PublishMessage message, ByteBuf in) {
        // Payload as ByteBuffer
        ByteBuf payload = Unpooled.buffer(in.readableBytes());
        in.readBytes(payload);
        message.setContent(payload.nioBuffer());
    }
}
