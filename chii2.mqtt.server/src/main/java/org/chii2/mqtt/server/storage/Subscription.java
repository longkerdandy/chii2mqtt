package org.chii2.mqtt.server.storage;

import java.io.Serializable;

/**
 * MQTT Subscriber in Storage
 */
public class Subscription implements Serializable {

    private String topic;

    private String subscriberID;

    private int qosLevel;

    public Subscription(String topic, String subscriberID, int qosLevel) {
        this.topic = topic;
        this.subscriberID = subscriberID;
        this.qosLevel = qosLevel;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(String subscriberID) {
        this.subscriberID = subscriberID;
    }

    public int getQosLevel() {
        return qosLevel;
    }

    public void setQosLevel(int qosLevel) {
        this.qosLevel = qosLevel;
    }
}
