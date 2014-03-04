package org.chii2.mqtt.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.server.disruptor.InboundDisruptor;
import org.chii2.mqtt.server.disruptor.InboundMQTTEventTranslator;
import org.chii2.mqtt.server.storage.ChannelRepository;

/**
 * Netty Server Handler
 */
public class MQTTServerHandler extends ChannelInboundHandlerAdapter {

    // ChannelHandlerContext Attachment
    protected static final AttributeKey<Boolean> AUTH = AttributeKey.valueOf("AUTH");
    protected static final AttributeKey<String> CLIENT_ID = AttributeKey.valueOf("CLIENT_ID");
    // Inbound Disruptor
    InboundDisruptor disruptor;

    public MQTTServerHandler(InboundDisruptor disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        // Received the MQTT Message , push to disruptor
        disruptor.pushEvent(new InboundMQTTEventTranslator(ctx, (MQTTMessage) message));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Remove the context from ChannelRepository
        ChannelRepository.removeClientChannel(getClientID(ctx));
    }

    /**
     * Get Authorization state from ChannelHandlerContext's Attachment
     *
     * @param context ChannelHandlerContext
     * @return Authorization State
     */
    public static boolean getAuthorization(ChannelHandlerContext context) {
        Attribute<Boolean> attr = context.attr(AUTH);
        return attr.get();
    }

    /**
     * Set/Save Authorization state to ChannelHandlerContext's Attachment
     *
     * @param context ChannelHandlerContext
     * @param auth    Authorization State
     */
    public static void setAuthorization(ChannelHandlerContext context, boolean auth) {
        Attribute<Boolean> attr = context.attr(AUTH);
        attr.set(auth);
    }

    /**
     * Get Client ID from ChannelHandlerContext's Attachment
     *
     * @param context ChannelHandlerContext
     * @return Client ID
     */
    public static String getClientID(ChannelHandlerContext context) {
        Attribute<String> attr = context.attr(CLIENT_ID);
        return attr.get();
    }

    /**
     * Set/Save Client ID to ChannelHandlerContext's Attachment
     *
     * @param context  ChannelHandlerContext
     * @param clientID Client ID
     */
    public static void setClientID(ChannelHandlerContext context, String clientID) {
        Attribute<String> attr = context.attr(CLIENT_ID);
        attr.set(clientID);
    }
}
