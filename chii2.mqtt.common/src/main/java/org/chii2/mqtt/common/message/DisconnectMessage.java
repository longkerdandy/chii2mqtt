package org.chii2.mqtt.common.message;

/**
 * DISCONNECT Message - Disconnect notification
 * <p/>
 * The DISCONNECT message is sent from the client to the server to indicate that it is
 * about to close its TCP/IP connection. This allows for a clean disconnection, rather than
 * just dropping the line.
 * <p/>
 * If the client had connected with the clean session flag set, then all previously
 * maintained information about the client will be discarded.
 * <p/>
 * A server should not rely on the client to close the TCP/IP connection after receiving a
 * DISCONNECT.
 */
public class DisconnectMessage extends MQTTMessage {

    public DisconnectMessage() {
        // Set message type
        this.messageType = MessageType.DISCONNECT;
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
