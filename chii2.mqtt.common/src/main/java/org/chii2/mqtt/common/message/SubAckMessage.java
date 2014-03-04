package org.chii2.mqtt.common.message;

import java.util.ArrayList;
import java.util.List;

/**
 * SUBACK Message - Subscription Acknowledgment
 * <p/>
 * A SUBACK message is sent by the server to the client to confirm receipt of a
 * SUBSCRIBE message.
 * <p/>
 * A SUBACK message contains a list of granted QoS levels. The order of granted QoS
 * levels in the SUBACK message matches the order of the topic names in the
 * corresponding SUBSCRIBE message.
 */
public class SubAckMessage extends MQTTMessage {

    // Message ID
    protected int messageID;
    // Payload, Granted QoS Levels (in order)
    protected List<QoSLevel> grantedQoS = new ArrayList<>();

    /**
     * INTERNAL USE ONLY
     */
    public SubAckMessage() {
    }

    public SubAckMessage(int messageID, List<QoSLevel> grantedQoS) {
        this.messageType = MessageType.SUBACK;
        this.messageID = messageID;
        this.grantedQoS = grantedQoS;
        this.remainingLength = calculateRemainingLength();
    }

    @Override
    protected int calculateRemainingLength() {
        int length = 2;
        for (QoSLevel ignored : grantedQoS) {
            length++;
        }
        return length;
    }

    @Override
    public void validate() {
        if (messageID < 0) {
            throw new IllegalStateException("Negative Message ID.");
        }
        if (grantedQoS == null || grantedQoS.size() == 0) {
            throw new IllegalStateException("Granted QoS Level not provided.");
        }
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public List<QoSLevel> getGrantedQoS() {
        return grantedQoS;
    }

    public void setGrantedQoS(List<QoSLevel> grantedQoS) {
        this.grantedQoS = grantedQoS;
    }

    public void addGrantedQoS(QoSLevel qosLevel) {
        this.grantedQoS.add(qosLevel);
    }

    public void removeGrantedQoS(QoSLevel qosLevel) {
        this.grantedQoS.remove(qosLevel);
    }
}
