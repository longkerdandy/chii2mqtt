package org.chii2.mqtt.common.message;

import org.chii2.mqtt.common.utils.MQTTUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * UNSUBSCRIBE Message - Unsubscribe from named topics
 * <p/>
 * An UNSUBSCRIBE message is sent by the client to the server to unsubscribe from
 * named topics.
 */
public class UnsubscribeMessage extends MQTTMessage {

    // Message ID
    protected int messageID;
    // Payload, Topic Names
    protected List<String> topicNames = new ArrayList<>();

    /**
     * INTERNAL USE ONLY
     */
    public UnsubscribeMessage() {
    }

    public UnsubscribeMessage(boolean dupFlag, int messageID, List<String> topicNames) {
        this.messageType = MessageType.UNSUBSCRIBE;
        // UNSUBSCRIBE messages use QoS level 1 to acknowledge multiple unsubscribe requests.
        this.qosLevel = QoSLevel.LEAST_ONCE;
        this.dupFlag = dupFlag;
        this.messageID = messageID;
        this.topicNames = topicNames;
        this.remainingLength = calculateRemainingLength();
    }

    @Override
    protected int calculateRemainingLength() {
        int length = 2;
        for (String topicName : topicNames) {
            length = length + 2 + topicName.getBytes().length;
        }
        return length;
    }

    @Override
    public void validate() {
        if (messageID < 0) {
            throw new IllegalStateException("Negative Message ID.");
        }
        if (topicNames == null || topicNames.size() == 0) {
            throw new IllegalStateException("Topic Names not provided.");
        }
        for (String topic : topicNames) {
            if (!MQTTUtils.isTopicWildcardValid(topic)) {
                throw new IllegalStateException("Topic Name not provided or format incorrect.");
            }
        }
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public List<String> getTopicNames() {
        return topicNames;
    }

    public void setTopicNames(List<String> topicNames) {
        this.topicNames = topicNames;
    }

    public void addTopicName(String topicName) {
        this.topicNames.add(topicName);
    }

    public void removeTopicName(String topicName) {
        this.topicNames.remove(topicName);
    }
}
