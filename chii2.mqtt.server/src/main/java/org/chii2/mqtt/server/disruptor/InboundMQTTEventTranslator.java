package org.chii2.mqtt.server.disruptor;

import com.lmax.disruptor.EventTranslator;
import io.netty.channel.ChannelHandlerContext;
import org.chii2.mqtt.common.message.MQTTMessage;

/**
 * MQTT Event Translator used by InboundDisruptor
 */
public class InboundMQTTEventTranslator implements EventTranslator<InboundMQTTEvent> {

    private ChannelHandlerContext context;
    private MQTTMessage message;

    public InboundMQTTEventTranslator(ChannelHandlerContext context, MQTTMessage message) {
        this.context = context;
        this.message = message;
    }

    @Override
    public void translateTo(InboundMQTTEvent event, long sequence) {
        event.setContext(context);
        event.setMQTTMessage(message);
    }
}
