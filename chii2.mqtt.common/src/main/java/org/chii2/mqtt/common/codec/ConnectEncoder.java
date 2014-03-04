package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.ConnectMessage;
import org.chii2.mqtt.common.utils.MQTTUtils;

/**
 * CONNECT Message encoder
 */
public class ConnectEncoder extends BaseEncoder<ConnectMessage> {

    @Override
    protected void encodeVariableHeader(ConnectMessage message, ByteBuf out) {
        // Write Variable Header
        // Protocol Name 8 bytes
        out.writeBytes(MQTTUtils.encodeString("Protocol Name", message.getProtocolName()));
        // Protocol Version 1 byte
        out.writeByte(message.getProtocolVersion());
        // Connection Flags 1 byte
        byte connectionFlags = 0;
        if (message.isCleanSession()) {
            connectionFlags |= 0x02;
        }
        if (message.isWillFlag()) {
            connectionFlags |= 0x04;
        }
        connectionFlags |= ((message.getWillQoS().byteValue() & 0x03) << 3);
        if (message.isWillRetain()) {
            connectionFlags |= 0x020;
        }
        if (message.isPasswordFlag()) {
            connectionFlags |= 0x040;
        }
        if (message.isUserNameFlag()) {
            connectionFlags |= 0x080;
        }
        out.writeByte(connectionFlags);
        // Keep Alive Timer 2 bytes
        out.writeShort(message.getKeepAlive());
    }

    @Override
    protected void encodePayload(ConnectMessage message, ByteBuf out) {
        // Write Payload
        // Client ID
        out.writeBytes(MQTTUtils.encodeString("Client ID", message.getClientID()));
        // Will Topic & Will Message
        if (message.isWillFlag()) {
            out.writeBytes(MQTTUtils.encodeString("Will Topic", message.getWillTopic()));
            out.writeBytes(MQTTUtils.encodeString("Will Message", message.getWillMessage()));
        }
        // User Name & Password
        if (message.isUserNameFlag()) {
            out.writeBytes(MQTTUtils.encodeString("User Name", message.getUserName()));
            if (message.isPasswordFlag()) {
                out.writeBytes(MQTTUtils.encodeString("Password", message.getPassword()));
            }
        }
    }
}
