package org.chii2.mqtt.server.disruptor;

import com.lmax.disruptor.EventTranslator;
import org.chii2.mqtt.common.message.MQTTMessage;

/**
 * MQTT Event Translator used by OutboundDisruptor
 */
public class OutboundMQTTEventTranslator implements EventTranslator<OutboundMQTTEvent> {

    // Subscriber's Client ID
    private String subscriberID;
    // Should resend ot not
    private boolean resend;
    // Last sending time in milliseconds
    private long sendingTime;
    // Resend times
    private int resendTimes;
    // In-Flight Message ID
    private int messageID;
    // QoS Level
    private int qos;
    // Message Type
    private MQTTMessage.MessageType messageType;

    public OutboundMQTTEventTranslator(String subscriberID, boolean resend, long sendingTime, int resendTimes, int messageID, int qos, MQTTMessage.MessageType messageType) {
        this.subscriberID = subscriberID;
        this.resend = resend;
        this.sendingTime = sendingTime;
        this.resendTimes = resendTimes;
        this.messageID = messageID;
        this.qos = qos;
        this.messageType = messageType;
    }

    @Override
    public void translateTo(OutboundMQTTEvent event, long sequence) {
        event.setSubscriberID(subscriberID);
        event.setResend(resend);
        event.setSendingTime(sendingTime);
        event.setResendTimes(resendTimes);
        event.setMessageID(messageID);
        event.setQoS(qos);
        event.setMessageType(messageType);
    }
}
