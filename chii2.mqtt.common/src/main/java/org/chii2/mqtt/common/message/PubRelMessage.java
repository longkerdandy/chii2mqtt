package org.chii2.mqtt.common.message;

/**
 * PUBREL Message - Assured Publish Release (Part 2)
 * <p/>
 * A PUBREL message is the response either from a publisher to a PUBREC message from
 * the server, or from the server to a PUBREC message from a subscriber. It is the third
 * message in the QoS 2 protocol flow.
 */
public class PubRelMessage extends MQTTMessage {

    // Message ID
    protected int messageID;

    /**
     * INTERNAL USE ONLY
     */
    public PubRelMessage() {
    }

    public PubRelMessage(boolean dupFlag, int messageID) {
        this.messageType = MessageType.PUBREL;
        // PUBREL messages use QoS level 1 as an acknowledgement is expected in the form
        // of a PUBCOMP. Retries are handled in the same way as PUBLISH messages.
        this.qosLevel = QoSLevel.LEAST_ONCE;
        this.dupFlag = dupFlag;
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
