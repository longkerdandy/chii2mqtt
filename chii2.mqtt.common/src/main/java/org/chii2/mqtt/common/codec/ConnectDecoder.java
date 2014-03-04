package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import org.chii2.mqtt.common.message.ConnectMessage;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.common.utils.MQTTUtils;

/**
 * CONNECT Message Decoder
 */
public class ConnectDecoder extends BaseDecoder<ConnectMessage> {

    @Override
    protected ConnectMessage createMessage() {
        return new ConnectMessage();
    }

    @Override
    protected void decodeVariableHeader(ConnectMessage message, ByteBuf in) {
        // Variable Header
        // Protocol Name 8 bytes
        message.setProtocolName(MQTTUtils.decodeString("Protocol Name", in));
        // Protocol Version 1 byte (value 0x03)
        message.setProtocolVersion(in.readByte());
        // Connection Flags 1 byte
        byte connFlags = in.readByte();
        boolean cleanSession = ((connFlags & 0x02) >> 1) == 1;
        boolean willFlag = ((connFlags & 0x04) >> 2) == 1;
        byte willQoS = (byte) ((connFlags & 0x18) >> 3);
        boolean willRetain = ((connFlags & 0x20) >> 5) == 1;
        boolean passwordFlag = ((connFlags & 0x40) >> 6) == 1;
        boolean userNameFlag = ((connFlags & 0x80) >> 7) == 1;
        message.setCleanSession(cleanSession);
        message.setWillFlag(willFlag);
        message.setWillQoS(MQTTMessage.QoSLevel.values()[willQoS]);
        message.setWillRetain(willRetain);
        message.setPasswordFlag(passwordFlag);
        message.setUserNameFlag(userNameFlag);
        // Keep Alive Timer 2 bytes
        int keepAliveTimer = in.readUnsignedShort();
        message.setKeepAlive(keepAliveTimer);
    }

    @Override
    protected void decodePayload(ConnectMessage message, ByteBuf in) {
        // Payload
        // ClientID
        message.setClientID(MQTTUtils.decodeString("Client ID", in));

        // Will Topic and Will Message
        if (message.isWillFlag()) {
            message.setWillTopic(MQTTUtils.decodeString("Will Topic", in));
            message.setWillMessage(MQTTUtils.decodeString("Will Message", in));
        }

        // User Name and Password
        if (message.isUserNameFlag()) {
            message.setUserName(MQTTUtils.decodeString("User Name", in));

            if (message.isPasswordFlag()) {
                message.setPassword(MQTTUtils.decodeString("Password", in));
            }
        }
    }
}
