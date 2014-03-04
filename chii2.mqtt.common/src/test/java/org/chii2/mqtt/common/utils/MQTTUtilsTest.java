package org.chii2.mqtt.common.utils;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

/**
 * MQTT Message Codec Utils Test
 */
public class MQTTUtilsTest {

    @Test
    public void decodeRemainingLengthTest() {
        ByteBuf byteBuf;
        byteBuf = MQTTUtils.encodeRemainingLength(64);
        assert 64 == MQTTUtils.decodeRemainingLength(byteBuf);
        byteBuf = MQTTUtils.encodeRemainingLength(127);
        assert 127 == MQTTUtils.decodeRemainingLength(byteBuf);
        byteBuf = MQTTUtils.encodeRemainingLength(8000);
        assert 8000 == MQTTUtils.decodeRemainingLength(byteBuf);
        byteBuf = MQTTUtils.encodeRemainingLength(16383);
        assert 16383 == MQTTUtils.decodeRemainingLength(byteBuf);
        byteBuf = MQTTUtils.encodeRemainingLength(150000);
        assert 150000 == MQTTUtils.decodeRemainingLength(byteBuf);
        byteBuf = MQTTUtils.encodeRemainingLength(2097151);
        assert 2097151 == MQTTUtils.decodeRemainingLength(byteBuf);
        byteBuf = MQTTUtils.encodeRemainingLength(50000000);
        assert 50000000 == MQTTUtils.decodeRemainingLength(byteBuf);
        byteBuf = MQTTUtils.encodeRemainingLength(268435455);
        assert 268435455 == MQTTUtils.decodeRemainingLength(byteBuf);
    }

    @Test
    public void encodeRemainingLengthTest() {
        ByteBuf byteBuf;
        byteBuf = MQTTUtils.encodeRemainingLength(64);
        assert byteBuf.readableBytes() == 1;
        byteBuf = MQTTUtils.encodeRemainingLength(127);
        assert byteBuf.readableBytes() == 1;
        byteBuf = MQTTUtils.encodeRemainingLength(8000);
        assert byteBuf.readableBytes() == 2;
        byteBuf = MQTTUtils.encodeRemainingLength(16383);
        assert byteBuf.readableBytes() == 2;
        byteBuf = MQTTUtils.encodeRemainingLength(150000);
        assert byteBuf.readableBytes() == 3;
        byteBuf = MQTTUtils.encodeRemainingLength(2097151);
        assert byteBuf.readableBytes() == 3;
        byteBuf = MQTTUtils.encodeRemainingLength(50000000);
        assert byteBuf.readableBytes() == 4;
        byteBuf = MQTTUtils.encodeRemainingLength(268435455);
        assert byteBuf.readableBytes() == 4;
    }

    @Test(expectedExceptions = EncoderException.class)
    public void encodeRemainingLengthWithExceptionTest() {
        MQTTUtils.encodeRemainingLength(268435456);
    }

    @Test
    public void decodeStringTest() throws UnsupportedEncodingException {
        ByteBuf bytebuf = MQTTUtils.encodeString("Test", "OTWP");
        assert "OTWP".equals(MQTTUtils.decodeString("Test", bytebuf));
    }

    @Test
    public void encodeStringTest() {
        ByteBuf bytebuf = MQTTUtils.encodeString("Test", "OTWP");
        bytebuf.skipBytes(2);
        assert bytebuf.readableBytes() == 4;
        assert bytebuf.readByte() == 0b1001111;
        assert bytebuf.readByte() == 0b1010100;
        assert bytebuf.readByte() == 0b1010111;
        assert bytebuf.readByte() == 0b1010000;
    }

    @Test
    public void topicValidTest() {
        assert MQTTUtils.isTopicWildcardValid("finance");
        assert MQTTUtils.isTopicWildcardValid("finance/stock/ibm");
        assert MQTTUtils.isTopicWildcardValid("finance/stock/ibm/closingprice");
        assert MQTTUtils.isTopicWildcardValid("/finance/stock/ibm/currentprice");
        assert MQTTUtils.isTopicWildcardValid("finance/#");
        assert MQTTUtils.isTopicWildcardValid("#");
        assert MQTTUtils.isTopicWildcardValid("/#");
        assert MQTTUtils.isTopicWildcardValid("finance/stock/ibm/+");
        assert MQTTUtils.isTopicWildcardValid("finance/+/ibm");
        assert MQTTUtils.isTopicWildcardValid("finance/+/+/ibm/+");
        assert MQTTUtils.isTopicWildcardValid("/+");
        assert MQTTUtils.isTopicWildcardValid("+");
        assert !MQTTUtils.isTopicWildcardValid("finance/stock/ibm+");
        assert !MQTTUtils.isTopicWildcardValid("finance/#/ibm");
        assert !MQTTUtils.isTopicWildcardValid("finance/#/ibm/#");
    }

    @Test
    public void topicMatchTest() {
        assert MQTTUtils.isTopicMatch("finance/#", "finance", true);
        assert MQTTUtils.isTopicMatch("finance/#", "finance/stock", true);
        assert MQTTUtils.isTopicMatch("finance/#", "finance/stock/ibm", true);
        assert !MQTTUtils.isTopicMatch("finance/+", "finance", true);
        assert MQTTUtils.isTopicMatch("finance/+", "finance/stock", true);
        assert !MQTTUtils.isTopicMatch("finance/+", "finance/stock/ibm", true);
        assert MQTTUtils.isTopicMatch("finance/+/ibm", "finance/stock/ibm", true);
        assert MQTTUtils.isTopicMatch("finance", "finance", true);
        assert MQTTUtils.isTopicMatch("finance/stock", "finance/stock", true);
        assert MQTTUtils.isTopicMatch("finance/stock/ibm", "finance/stock/ibm", true);
        assert !MQTTUtils.isTopicMatch("finance", "finance/stock/ibm", true);
        assert !MQTTUtils.isTopicMatch("/finance/#", "finance", true);
        assert !MQTTUtils.isTopicMatch("/finance/#", "finance/stock", true);
        assert !MQTTUtils.isTopicMatch("/finance/#", "finance/stock/ibm", true);
        assert !MQTTUtils.isTopicMatch("hr/#", "finance", true);
        assert !MQTTUtils.isTopicMatch("hr/#", "finance/stock", true);
        assert !MQTTUtils.isTopicMatch("hr/#", "finance/stock/ibm", true);
    }
}
