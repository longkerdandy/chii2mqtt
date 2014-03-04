package org.chii2.mqtt.common.message;

/**
 * PINGREQ Message - PING request
 * <p/>
 * The PINGREQ message is an "are you alive?" message that is sent from a connected
 * client to the server.
 */
public class PingReqMessage extends MQTTMessage {

    public PingReqMessage() {
        // Set Message Type
        this.messageType = MessageType.PINGREQ;
    }

    @Override
    protected int calculateRemainingLength() {
        return 0;
    }

    @Override
    public void validate() {
        // Nothing to validate
    }
}
