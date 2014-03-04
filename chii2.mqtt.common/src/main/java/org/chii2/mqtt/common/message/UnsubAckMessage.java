package org.chii2.mqtt.common.message;

/**
 * UNSUBACK Message - Unsubscribe Acknowledgment
 * <p/>
 * The UNSUBACK message is sent by the server to the client to confirm receipt of an
 * UNSUBSCRIBE message.
 */
public class UnsubAckMessage extends MQTTMessage {

    // Message ID
    protected int messageID;

    /**
     * INTERNAL USE ONLY
     */
    public UnsubAckMessage() {
    }

    public UnsubAckMessage(int messageID) {
        this.messageType = MessageType.UNSUBACK;
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
