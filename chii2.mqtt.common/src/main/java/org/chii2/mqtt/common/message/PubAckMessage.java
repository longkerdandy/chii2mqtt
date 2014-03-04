package org.chii2.mqtt.common.message;

/**
 * PUBACK Message - Publish Acknowledgment
 * <p/>
 * A PUBACK message is the response to a PUBLISH message with QoS level 1. A PUBACK
 * message is sent by a server in response to a PUBLISH message from a publishing client,
 * and by a subscriber in response to a PUBLISH message from the server.
 */
public class PubAckMessage extends MQTTMessage {

    // Message ID
    protected int messageID;

    /**
     * INTERNAL USE ONLY
     */
    public PubAckMessage() {
    }

    public PubAckMessage(int messageID) {
        this.messageType = MessageType.PUBACK;
        this.messageID = messageID;
        this.remainingLength = calculateRemainingLength();
    }

    @Override
    protected int calculateRemainingLength() {
        return 2;
    }

    @Override
    public void validate() {
        if (messageID < 0) {
            throw new IllegalStateException("Negative Message ID.");
        }
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }
}
