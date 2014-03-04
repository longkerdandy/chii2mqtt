package org.chii2.mqtt.common.message;

import org.chii2.mqtt.common.utils.MQTTUtils;

import java.nio.ByteBuffer;

/**
 * PUBLISH Message - Publish Message
 * <p/>
 * A PUBLISH message is sent by a client to a server for distribution to interested
 * subscribers. Each PUBLISH message is associated with a topic name (also known as the
 * Subject or Channel). This is a hierarchical name space that defines a taxonomy of
 * information sources for which subscribers can register an interest. A message that is
 * published to a specific topic name is delivered to connected subscribers for that topic.
 * <p/>
 * If a client subscribes to one or more topics, any message published to those topics are
 * sent by the server to the client as a PUBLISH message.
 */
public class PublishMessage extends MQTTMessage {

    // Topic Name
    // This must not contain Topic wildcard characters.
    protected String topicName;
    // Message ID, Not present if Qos level is 0
    protected int messageID;
    // Payload
    protected ByteBuffer content;

    /**
     * INTERNAL USE ONLY
     */
    public PublishMessage() {
    }

    public PublishMessage(boolean retain, QoSLevel qosLevel, boolean dupFlag, String topicName, int messageID, ByteBuffer content) {
        this.messageType = MessageType.PUBLISH;
        this.retain = retain;
        this.qosLevel = qosLevel;
        this.dupFlag = dupFlag;
        this.topicName = topicName;
        this.messageID = messageID;
        setContent(content);
        this.remainingLength = calculateRemainingLength();
    }

    /**
     * Clone from exist Publish Message
     * Note the content still reference to the same Object
     *
     * @param publishMessage Publish Message
     */
    public PublishMessage(PublishMessage publishMessage) {
        this.messageType = MessageType.PUBLISH;
        this.retain = publishMessage.retain;
        this.qosLevel = publishMessage.qosLevel;
        this.dupFlag = publishMessage.dupFlag;
        this.topicName = publishMessage.topicName;
        this.messageID = publishMessage.messageID;
        this.content = publishMessage.content;
        this.remainingLength = publishMessage.remainingLength;
    }

    @Override
    protected int calculateRemainingLength() {
        int length = 0;
        length = length + 2 + topicName.getBytes().length;
        length = length + 2;
        if (hasContent()) {
            length = length + content.limit();
        }
        return length;
    }

    @Override
    public void validate() {
        if (qosLevel == null || qosLevel == QoSLevel.RESERVED) {
            throw new IllegalStateException("QoS Level not provided or incorrect.");
        }
        if (!MQTTUtils.isTopicNormalValid(topicName)) {
            throw new IllegalStateException("Topic Name not provided or format incorrect.");
        }
        if (messageID < 0) {
            throw new IllegalStateException("Negative Message ID.");
        }
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public ByteBuffer getContent() {
        return content;
    }

    /**
     * Set Content
     * Make sure the ByteBuffer's position and limit is correct.
     * If the position is not 0, it will be flipped.
     *
     * @param content ByteBuffer Content
     */
    public void setContent(ByteBuffer content) {
        if (content.position() != 0) {
            content.flip();
        }
        this.content = content;
    }

    public boolean hasContent() {
        return content != null && content.limit() > 0;
    }
}
