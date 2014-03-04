package org.chii2.mqtt.server;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

/**
 * Server Configuration
 */
public class MQTTServerConfiguration {

    // TCP/IP port 1883 is reserved with IANA for use with MQTT.
    private int port = 1883;
    // TCP/IP port 8883 is reserved with IANA for use with MQTT over SSL.
    private int sslPort = 8883;
    // Server ID
    private final String serverID;
    // Server Name
    private String serverName;
    // Resend Internal in millisecond
    private long interval = 10000;
    // Max Resend Times
    private int maxRetryTimes = 5;
    // Inbound Disruptor Ring Buffer Size
    private int inboundRingBuffer = 1024;
    // Outbound Disruptor Ring Buffer Size
    private int outboundRingBuffer = 1024;

    // The Logger
    private final Logger logger = LoggerFactory.getLogger(MQTTServerConfiguration.class);

    public MQTTServerConfiguration() {
        this.serverID = generateServeriD();
    }

    /**
     * Generate Server ID based on Mac Address
     * Override this method to provide vendor's specific Server ID
     *
     * @return Server ID
     */
    public String generateServeriD() {
        String result = "Chii2";
        try {
            Socket socket = new Socket();
            SocketAddress endpoint = new InetSocketAddress("www.bing.com", 80);
            socket.connect(endpoint);
            InetAddress localAddress = socket.getLocalAddress();
            socket.close();
            NetworkInterface ni = NetworkInterface.getByInetAddress(localAddress);
            byte[] mac = ni.getHardwareAddress();
            StringBuilder builder = new StringBuilder();
            if (mac != null) {
                for (int j = 0; j < mac.length; j++) {
                    String part = String.format("%02X%s", mac[j], (j < mac.length - (1)) ? "-" : "");
                    builder.append(part);
                }
                result = result + "-" + builder.toString();
            }

        } catch (IOException e) {
            logger.warn("Generate Server ID based on Mac address with error: {}", ExceptionUtils.getMessage(e));
        }
        return result;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSSLPort() {
        return sslPort;
    }

    public void setSSLPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public String getServerID() {
        return serverID;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public int getInboundRingBuffer() {
        return inboundRingBuffer;
    }

    public void setInboundRingBuffer(int inboundRingBuffer) {
        this.inboundRingBuffer = inboundRingBuffer;
    }

    public int getOutboundRingBuffer() {
        return outboundRingBuffer;
    }

    public void setOutboundRingBuffer(int outboundRingBuffer) {
        this.outboundRingBuffer = outboundRingBuffer;
    }
}
