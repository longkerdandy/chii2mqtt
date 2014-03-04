package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.common.utils.MQTTUtils;

import java.util.List;

/**
 * Base MQTT Message Decoder
 */
public abstract class BaseDecoder<T extends MQTTMessage> {

    /**
     * Netty decoder method
     *
     * @param ctx ChannelHandlerContext
     * @param in  Input ByteBuf
     * @param out Output
     */
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Reset index
        in.resetReaderIndex();

        // Message
        T message = createMessage();

        // Fixed Header
        if (!decodeFixedHeader(message, in)) {
            in.resetReaderIndex();
            return;
        }

        // Full length received to continue
        if (in.readableBytes() < message.getRemainingLength()) {
            in.resetReaderIndex();
            return;
        }

        // Variable Header
        decodeVariableHeader(message, in);

        // Payload
        decodePayload(message, in);

        // Validate
        message.validate();

        // Output
        out.add(message);
    }

    /**
     * Create a new instance of T Message
     *
     * @return Message instance
     */
    protected abstract T createMessage();

    /**
     * Decode the Variable Header part
     *
     * @param message Message to be set
     * @param in      Input
     */
    protected abstract void decodeVariableHeader(T message, ByteBuf in);

    /**
     * Decode the Payload part
     *
     * @param message Message to be set
     * @param in      Input
     */
    protected abstract void decodePayload(T message, ByteBuf in);

    /**
     * Decode MQTT Message Fixed Header
     *
     * @param message MQTT Message
     * @param in      Input ByteBuf
     * @return True if decode successful
     */
    protected boolean decodeFixedHeader(MQTTMessage message, ByteBuf in) {
        // Fixed Header should be at least 2 bytes long
        if (in.readableBytes() < 2) {
            return false;
        }
        // Byte1
        byte byte1 = in.readByte();
        byte messageType = (byte) ((byte1 & 0x00F0) >> 4);
        boolean dupFlag = ((byte) ((byte1 & 0x0008) >> 3) == 1);
        byte qosLevel = (byte) ((byte1 & 0x0006) >> 1);
        boolean retain = ((byte) (byte1 & 0x0001) == 1);
        // Byte 2 - 5
        int remainingLength = MQTTUtils.decodeRemainingLength(in);
        if (remainingLength < 0) {
            return false;
        }

        message.setMessageType(MQTTMessage.MessageType.values()[messageType]);
        message.setDupFlag(dupFlag);
        message.setQosLevel(MQTTMessage.QoSLevel.values()[qosLevel]);
        message.setRetain(retain);
        message.setRemainingLength(remainingLength);
        return true;
    }
}
