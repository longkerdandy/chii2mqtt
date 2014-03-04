package org.chii2.mqtt.common.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.chii2.mqtt.common.utils.MQTTUtils;

/**
 * MQTT V3.1 Message
 */
public abstract class MQTTMessage {

    // Strings in MQTT now support full UTF-8, instead of just the US-ASCII subset
    public static final String PROTOCOL_NAME = "MQIsdp";          // Protocol Name
    public static final byte PROTOCOL_VERSION = 0x03;             // Protocol Version (v3.1)

    // Message Type
    public static enum MessageType {
        RESERVED(0),     // Reserved
        CONNECT(1),      // Client request to connect to Server
        CONNACK(2),      // Connect Acknowledgment
        PUBLISH(3),      // Publish message
        PUBACK(4),       // Publish Acknowledgment
        PUBREC(5),       // Publish Received (assured delivery part 1)
        PUBREL(6),       // Publish Release (assured delivery part 2)
        PUBCOMP(7),      // Publish Complete (assured delivery part 3)
        SUBSCRIBE(8),    // Client Subscribe request
        SUBACK(9),       // Subscribe Acknowledgment
        UNSUBSCRIBE(10), // Client Unsubscribe request
        UNSUBACK(11),    // Unsubscribe Acknowledgment
        PINGREQ(12),     // PING Request
        PINGRESP(13),    // PING Response
        DISCONNECT(14);  // Client is Disconnecting

        private byte value;

        MessageType(int value) {
            this.value = (byte) value;
        }

        public byte byteValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.name();
        }
    }

    // QoS Level
    public static enum QoSLevel {
        MOST_ONCE(0),
        LEAST_ONCE(1),
        EXACTLY_ONCE(2),
        RESERVED(3);

        private byte value;

        QoSLevel(int value) {
            this.value = (byte) value;
        }

        public byte byteValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.name();
        }
    }

    // Fixed Header
    //    This flag is only used on PUBLISH messages. When a client sends a PUBLISH to a
    //    server, if the Retain flag is set (1), the server should hold on to the message after
    //    it has been delivered to the current subscribers.
    //    When a new subscription is established on a topic, the last retained message on
    //    that topic should be sent to the subscriber with the Retain flag set. If there is no
    //    retained message, nothing is sent
    //    This is useful where publishers send messages on a "report by exception" basis,
    //    where it might be some time between messages. This allows new subscribers to
    //    instantly receive data with the retained, or Last Known Good, value.
    //    When a server sends a PUBLISH to a client as a result of a subscription that
    //    already existed when the original PUBLISH arrived, the Retain flag should not be
    //    set, regardless of the Retain flag of the original PUBLISH. This allows a client to
    //    distinguish messages that are being received because they were retained and
    //    those that are being received "live".
    //    Retained messages should be kept over restarts of the server.
    //    A server may delete a retained message if it receives a message with a zero-length
    //    payload and the Retain flag set on the same topic.
    protected boolean retain;                                     // Position:byte 1, bits 0
    //    This flag indicates the level of assurance for delivery of a PUBLISH message.
    protected QoSLevel qosLevel = QoSLevel.MOST_ONCE;             // Position:byte 1, bits 2-1
    //    This flag is set when the client or server attempts to re-deliver a PUBLISH,
    //    PUBREL, SUBSCRIBE or UNSUBSCRIBE message. This applies to messages where
    //    the value of QoS is greater than zero (0), and an acknowledgment is required.
    //    When the DUP bit is set, the variable header includes a Message ID.
    //    The recipient should treat this flag as a hint as to whether the message may have
    //    been previously received. It should not be relied on to detect duplicates.
    protected boolean dupFlag;                                    // Position:byte 1, bits 3
    //    Represented as a 4-bit unsigned value.
    protected MessageType messageType;                            // Position:byte 1, bits 7-4
    //    Represents the number of bytes remaining within the current message, including data in
    //    the variable header and the payload.
    protected int remainingLength;                                // Position:byte 2, could be multiple bytes

    /**
     * Get Message Fixed Header byte 1
     *
     * @return Fixed Header Byte 1
     */
    protected byte getFixedHeaderByte1() {
        byte flags = 0;
        if (retain) {
            flags |= 0x01;
        }
        flags |= ((qosLevel.value & 0x03) << 1);
        if (dupFlag) {
            flags |= 0x08;
        }
        flags = (byte) (messageType.value << 4 | flags);
        return flags;
    }

    /**
     * Get Message Fixed Header
     *
     * @return Fixed Header
     */
    public ByteBuf getFixedHeader() {
        ByteBuf fixedHeader = Unpooled.buffer(2);
        fixedHeader.writeByte(getFixedHeaderByte1());
        fixedHeader.writeBytes(MQTTUtils.encodeRemainingLength(remainingLength));
        return fixedHeader;
    }

    /**
     * Calculate Remaining Length based on Variable Header Length + Payload Length
     *
     * @return Remaining Length
     */
    protected abstract int calculateRemainingLength();

    /**
     * Validate the Message
     * Throws IllegalStateException if not in correct format
     */
    public abstract void validate();

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public QoSLevel getQosLevel() {
        return qosLevel;
    }

    public void setQosLevel(QoSLevel qosLevel) {
        this.qosLevel = qosLevel;
    }

    public boolean isDupFlag() {
        return dupFlag;
    }

    public void setDupFlag(boolean dupFlag) {
        this.dupFlag = dupFlag;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public int getRemainingLength() {
        return remainingLength;
    }

    public void setRemainingLength(int remainingLength) {
        this.remainingLength = remainingLength;
    }
}
