package org.chii2.mqtt.server;

import java.nio.ByteBuffer;

import static org.chii2.mqtt.common.message.MQTTMessage.QoSLevel;

/**
 * Utils for MQTT Server
 */
public class MQTTServerUtils {

    /**
     * Compare two QoS Level and return the lower one
     *
     * @param qos1 QoS Level 1
     * @param qos2 QoS Level 2
     * @return Lower QoS Level
     */
    public static QoSLevel getLowerQoS(QoSLevel qos1, QoSLevel qos2) {
        return qos1.byteValue() <= qos2.byteValue() ? qos1 : qos2;
    }

    /**
     * ByteBuffer to byte[]
     *
     * @param byteBuffer ByteBuffer
     * @return byte[]
     */
    public static byte[] ByteBufferToByteArray(ByteBuffer byteBuffer) {
        // Read ByteBuffer into byte[]
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        byteBuffer.rewind();
        return bytes;
    }

    /**
     * byte[] to ByteBuffer
     *
     * @param bytes byte[]
     * @return ByteBuffer
     */
    public static ByteBuffer ByteArrayToByteBuffer(byte[] bytes) {
        // Write byte[] to ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return byteBuffer;
    }

    /**
     * Generate UID based on Client ID and Message ID
     * MQTT Message Client ID must less than 23 characters
     * So complemented the Client ID to 23 length and appended given suffix
     *
     * @param clientID Client ID
     * @param suffix   Suffix String
     * @return UID
     */
    public static String generateUID(String clientID, String suffix) {
        StringBuilder builder = new StringBuilder(clientID);
        builder.append("-----------------------");
        return builder.substring(0, 23) + suffix;
    }
}
