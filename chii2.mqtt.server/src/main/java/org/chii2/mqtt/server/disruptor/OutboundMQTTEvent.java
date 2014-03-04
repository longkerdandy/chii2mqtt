package org.chii2.mqtt.server.disruptor;

import com.lmax.disruptor.EventFactory;
import org.chii2.mqtt.common.message.MQTTMessage;

/**
 * Event used by InboundDisruptor
 */
public class OutboundMQTTEvent {

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

    /**
     * Event Factory used by disruptor
     */
    public final static EventFactory<OutboundMQTTEvent> factory = new EventFactory<OutboundMQTTEvent>() {
        public OutboundMQTTEvent newInstance() {
            return new OutboundMQTTEvent();
        }
    };

    public String getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(String subscriberID) {
        this.subscriberID = subscriberID;
    }

    public boolean isResend() {
        return resend;
    }

    public void setResend(boolean resend) {
        this.resend = resend;
    }

    public long getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(long sendingTime) {
        this.sendingTime = sendingTime;
    }

    public int getResendTimes() {
        return resendTimes;
    }

    public void setResendTimes(int resendTimes) {
        this.resendTimes = resendTimes;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public int getQoS() {
        return qos;
    }

    public void setQoS(int qos) {
        this.qos = qos;
    }

    public MQTTMessage.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MQTTMessage.MessageType messageType) {
        this.messageType = messageType;
    }
}
