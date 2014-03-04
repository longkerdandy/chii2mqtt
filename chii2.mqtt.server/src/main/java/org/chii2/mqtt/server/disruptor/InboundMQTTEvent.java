package org.chii2.mqtt.server.disruptor;

import com.lmax.disruptor.EventFactory;
import io.netty.channel.ChannelHandlerContext;
import org.chii2.mqtt.common.message.MQTTMessage;

/**
 * Event used by InboundDisruptor
 */
public class InboundMQTTEvent {

    // Channel Handler Context
    private ChannelHandlerContext context;
    // MQTT Message
    private MQTTMessage mqttMessage;

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    public MQTTMessage getMQTTMessage() {
        return mqttMessage;
    }

    public void setMQTTMessage(MQTTMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }

    /**
     * Event Factory used by disruptor
     */
    public final static EventFactory<InboundMQTTEvent> factory = new EventFactory<InboundMQTTEvent>() {
        public InboundMQTTEvent newInstance() {
            return new InboundMQTTEvent();
        }
    };
}
