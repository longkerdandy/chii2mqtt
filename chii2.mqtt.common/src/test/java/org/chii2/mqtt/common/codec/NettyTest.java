package org.chii2.mqtt.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Netty Test
 */
public class NettyTest {

    @Test
    public void ByteBufTest() {
        // Test ByteBuf.readByte
        byte[] bytes = {(byte) 16, (byte) 127};
        ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
        assert byteBuf.readableBytes() == 2;
        assert 16 == byteBuf.readByte();
        assert byteBuf.readableBytes() == 1;
        assert 127 == byteBuf.readByte();
        assert byteBuf.readableBytes() == 0;

        // Test NIO ByteBuffer
        byteBuf.resetReaderIndex();
        ByteBuffer byteBuffer = byteBuf.nioBuffer();
        assert byteBuffer.position() == 0;
        assert byteBuffer.limit() == 2;
    }

    @Test
    public void UTF8Test() throws UnsupportedEncodingException {
        byte[] bytes = {(byte) 0b1001101, (byte) 0b1010001, (byte) 0b1001001, (byte) 0b1110011, (byte) 0b1100100, (byte) 0b1110000};
        ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
        byte[] encodedProtocolName = new byte[6];
        byteBuf.readBytes(encodedProtocolName);
        String protocolName = new String(encodedProtocolName, "UTF-8");
        assert "MQIsdp".equals(protocolName);
    }
}
