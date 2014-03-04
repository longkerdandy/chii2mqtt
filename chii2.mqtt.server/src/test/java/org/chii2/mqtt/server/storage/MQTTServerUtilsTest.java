package org.chii2.mqtt.server.storage;

import org.chii2.mqtt.server.MQTTServerUtils;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

/**
 * Storage Utils Test
 */
public class MQTTServerUtilsTest {

    @Test
    public void getByteBufferTest() {
        // Init ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put((byte) 1);
        buffer.put((byte) 3);
        buffer.put((byte) 5);
        buffer.put((byte) 7);
        buffer.put((byte) 9);
        buffer.flip();

        assert buffer.position() == 0;
        assert buffer.limit() == 5;

        byte[] bytes = MQTTServerUtils.ByteBufferToByteArray(buffer);

        assert buffer.position() == 0;
        assert buffer.limit() == 5;

        assert bytes.length == 5;
        assert bytes[0] == 1;
        assert bytes[1] == 3;
        assert bytes[2] == 5;
        assert bytes[3] == 7;
        assert bytes[4] == 9;
    }

    @Test
    public void putByteBufferTest() {
        // Content
        byte[] bytes = new byte[]{1, 3, 5, 7, 9};

        ByteBuffer buffer = MQTTServerUtils.ByteArrayToByteBuffer(bytes);

        assert buffer.position() == 0;
        assert buffer.limit() == 5;

        assert buffer.get(0) == 1;
        assert buffer.get(1) == 3;
        assert buffer.get(2) == 5;
        assert buffer.get(3) == 7;
        assert buffer.get(4) == 9;
    }

    @Test
    public void uidTest() {
        String uid = MQTTServerUtils.generateUID("1234567890", String.valueOf(2014));
        assert uid.equals("1234567890-------------2014");
    }
}
