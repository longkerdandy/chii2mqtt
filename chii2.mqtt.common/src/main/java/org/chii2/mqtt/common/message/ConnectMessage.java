package org.chii2.mqtt.common.message;

import org.apache.commons.lang3.StringUtils;

/**
 * CONNECT Message - Client requests a connection to a server
 * <p/>
 * When a TCP/IP socket connection is established from a client to a server, a protocol
 * level session must be created using a CONNECT flow.
 */
public class ConnectMessage extends MQTTMessage {

    // Protocol Name (Should be MQIsdp)
    protected String protocolName;
    // Protocol Version (Should be 0x03)
    protected byte protocolVersion;
    // Connect flags
    protected boolean cleanSession;
    protected boolean willFlag;
    protected QoSLevel willQoS;
    protected boolean willRetain;
    protected boolean passwordFlag;
    protected boolean userNameFlag;
    protected int keepAlive;

    // Payload (in order if present)
    protected String clientID;
    protected String willTopic;
    protected String willMessage;
    protected String userName;
    protected String password;

    /**
     * INTERNAL USE ONLY
     */
    public ConnectMessage() {
    }

    private ConnectMessage(Builder builder) {
        this.messageType = builder.messageType;
        this.protocolName = builder.protocolName;
        this.protocolVersion = builder.protocolVersion;
        this.willRetain = builder.willRetain;
        this.willQoS = builder.willQoS;
        this.cleanSession = builder.cleanSession;
        this.keepAlive = builder.keepAlive;
        this.clientID = builder.clientID;
        this.willTopic = builder.willTopic;
        this.willFlag = StringUtils.isNotBlank(willTopic);
        this.willMessage = builder.willMessage;
        this.userName = builder.userName;
        this.userNameFlag = StringUtils.isNotBlank(userName);
        this.password = builder.password;
        this.passwordFlag = StringUtils.isNotBlank(password);
        // Calculate the remaining length
        this.remainingLength = calculateRemainingLength();
    }

    @Override
    protected int calculateRemainingLength() {
        int length = 12;
        length = length + 2 + clientID.getBytes().length;
        if (willFlag) {
            length = length + 2 + willTopic.getBytes().length;
            length = length + 2 + willMessage.getBytes().length;
        }
        if (userNameFlag) {
            length = length + 2 + userName.getBytes().length;
            if (passwordFlag) {
                length = length + 2 + password.getBytes().length;
            }
        }
        return length;
    }

    public boolean isAcceptableProtocolVersion() {
        return protocolVersion == PROTOCOL_VERSION;
    }

    public boolean isAcceptableClientID() {
        // The Client Identifier (Client ID) is between 1 and 23 characters long
        return StringUtils.isNotBlank(clientID) && clientID.getBytes().length <= 23;
    }

    @Override
    public void validate() {
        if (StringUtils.isBlank(clientID)) {
            throw new IllegalStateException("Client ID not provided.");
        }
        if (passwordFlag && !userNameFlag) {
            throw new IllegalStateException("User Name not provided.");
        }
        if (willFlag && (qosLevel == null || qosLevel == QoSLevel.RESERVED)) {
            throw new IllegalStateException("Will QoS Level not provided or incorrect.");
        }
        if (keepAlive < 0) {
            throw new IllegalStateException("Negative Keep Alive Timer.");
        }
        if (userNameFlag && userName.getBytes().length > 12) {
            throw new IllegalStateException("User Name must kept to 12 characters or fewer.");
        }
        if (passwordFlag && password.getBytes().length > 12) {
            throw new IllegalStateException("Password must kept to 12 characters or fewer.");
        }
        if (willFlag && willTopic.getBytes().length > 32767) {
            throw new IllegalStateException("Topic name has an upper length limit of 32,767 characters.");
        }
    }

    public static class Builder {
        private MessageType messageType = MessageType.CONNECT;
        private String protocolName = PROTOCOL_NAME;
        private byte protocolVersion = PROTOCOL_VERSION;
        // Connect flags
        private boolean cleanSession = false;
        private QoSLevel willQoS = QoSLevel.RESERVED;
        private boolean willRetain = false;
        private int keepAlive = 0;
        // Payload (in order if present)
        private String clientID;
        private String willTopic;
        private String willMessage;
        private String userName;
        private String password;

        public Builder(String clientID) {
            this.clientID = clientID;
        }

        public Builder keepAlive(int keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public Builder cleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        public Builder willQoS(QoSLevel willQoS) {
            this.willQoS = willQoS;
            return this;
        }

        public Builder willRetain(boolean willRetain) {
            this.willRetain = willRetain;
            return this;
        }

        public Builder willTopic(String willTopic) {
            this.willTopic = StringUtils.trim(willTopic);
            return this;
        }

        public Builder willMessage(String willMessage) {
            this.willMessage = StringUtils.trim(willMessage);
            return this;
        }

        public Builder userName(String userName) {
            this.userName = StringUtils.trim(userName);
            return this;
        }

        public Builder password(String password) {
            this.password = StringUtils.trim(password);
            return this;
        }

        public ConnectMessage build() {
            ConnectMessage message = new ConnectMessage(this);
            message.validate();
            return message;
        }
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public boolean isWillFlag() {
        return willFlag;
    }

    public void setWillFlag(boolean willFlag) {
        this.willFlag = willFlag;
    }

    public QoSLevel getWillQoS() {
        return willQoS;
    }

    public void setWillQoS(QoSLevel willQoS) {
        this.willQoS = willQoS;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public boolean isPasswordFlag() {
        return passwordFlag;
    }

    public void setPasswordFlag(boolean passwordFlag) {
        this.passwordFlag = passwordFlag;
    }

    public boolean isUserNameFlag() {
        return userNameFlag;
    }

    public void setUserNameFlag(boolean userNameFlag) {
        this.userNameFlag = userNameFlag;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public String getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(String willMessage) {
        this.willMessage = willMessage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
