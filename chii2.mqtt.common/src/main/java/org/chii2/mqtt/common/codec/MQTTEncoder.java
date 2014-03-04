package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * MQTT Message Encoder
 * This will be called by Netty pipeline, and route to specific message encoder.
 */
public class MQTTEncoder extends MessageToByteEncoder<MQTTMessage> {

    // Message Type <---> Decoder Map
    protected Map<MQTTMessage.MessageType, BaseEncoder> encoderMap = new HashMap<>();
    // The Logger
    private final Logger logger = LoggerFactory.getLogger(MQTTEncoder.class);

    public MQTTEncoder() {
        super();

        // Init Map
        encoderMap.put(MQTTMessage.MessageType.CONNECT, new ConnectEncoder());
        encoderMap.put(MQTTMessage.MessageType.CONNACK, new ConnAckEncoder());
        encoderMap.put(MQTTMessage.MessageType.PUBLISH, new PublishEncoder());
        encoderMap.put(MQTTMessage.MessageType.PUBACK, new PubAckEncoder());
        encoderMap.put(MQTTMessage.MessageType.SUBSCRIBE, new SubscribeEncoder());
        encoderMap.put(MQTTMessage.MessageType.SUBACK, new SubAckEncoder());
        encoderMap.put(MQTTMessage.MessageType.UNSUBSCRIBE, new UnsubscribeEncoder());
        encoderMap.put(MQTTMessage.MessageType.DISCONNECT, new DisconnectEncoder());
        encoderMap.put(MQTTMessage.MessageType.PINGREQ, new PingReqEncoder());
        encoderMap.put(MQTTMessage.MessageType.PINGRESP, new PingRespEncoder());
        encoderMap.put(MQTTMessage.MessageType.UNSUBACK, new UnsubAckEncoder());
        encoderMap.put(MQTTMessage.MessageType.PUBCOMP, new PubCompEncoder());
        encoderMap.put(MQTTMessage.MessageType.PUBREC, new PubRecEncoder());
        encoderMap.put(MQTTMessage.MessageType.PUBREL, new PubRelEncoder());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void encode(ChannelHandlerContext ctx, MQTTMessage msg, ByteBuf out) throws Exception {
        // Encoder
        BaseEncoder encoder = encoderMap.get(msg.getMessageType());

        // Encode
        try {
            encoder.encode(ctx, msg, out);
        } catch (IllegalStateException e) {
            logger.info("{} format incorrect, message dropped:{}", msg.getMessageType(), ExceptionUtils.getMessage(e));
        }
    }
}
