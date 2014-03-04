package org.chii2.mqtt.common.message;

import org.testng.annotations.Test;

/**
 * MQTTMessage Test
 */
public class MQTTMessageTest {

    @Test
    public void getFixedHeaderTest() {
        PublishMessage message = new PublishMessage();
        message.setRetain(false);
        message.setQosLevel(MQTTMessage.QoSLevel.LEAST_ONCE);
        message.setDupFlag(false);
        message.setMessageType(MQTTMessage.MessageType.PUBLISH);
        assert 0b00110010 == message.getFixedHeaderByte1();
    }

    @Test
    public void enumValueConvertTest() {
        assert MQTTMessage.QoSLevel.values()[1] == MQTTMessage.QoSLevel.LEAST_ONCE;
        assert MQTTMessage.MessageType.values()[8] == MQTTMessage.MessageType.SUBSCRIBE;
        assert ConnAckMessage.ReturnCode.values()[2] == ConnAckMessage.ReturnCode.IDENTIFIER_REJECTED;
    }
}
