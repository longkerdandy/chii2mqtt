package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.chii2.mqtt.common.message.MQTTMessage;

/**
 * MQTT Message Encoder
 */
public abstract class BaseEncoder<T extends MQTTMessage> {

    /**
     * Netty encoder method
     *
     * @param ctx     ChannelHandlerContext
     * @param message MQTT Message
     * @param out     Output
     */
    public void encode(ChannelHandlerContext ctx, T message, ByteBuf out) {
        // Validate
        message.validate();

        // Write Fixed Header
        out.writeBytes(message.getFixedHeader());

        // Write Variable Header
        encodeVariableHeader(message, out);

        // Write Payload
        encodePayload(message, out);
    }

    /**
     * Encode Variable Header
     *
     * @param message MQTT Message encode from
     * @param out     Output
     */
    protected abstract void encodeVariableHeader(T message, ByteBuf out);

    /**
     * Encode Payload
     *
     * @param message MQTT Message encode from
     * @param out     Output
     */
    protected abstract void encodePayload(T message, ByteBuf out);
}
