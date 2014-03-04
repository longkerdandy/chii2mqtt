package org.chii2.mqtt.common.message;

/**
 * PINGRESP Message - PING response
 * <p/>
 * A PINGRESP message is the response sent by a server to a PINGREQ message and
 * means "yes I am alive".
 */
public class PingRespMessage extends MQTTMessage {

    public PingRespMessage() {
        // Set Message Type
        this.messageType = MessageType.PINGRESP;
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
