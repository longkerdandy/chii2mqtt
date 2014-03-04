package org.chii2.mqtt.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.chii2.mqtt.common.codec.MQTTDecoder;
import org.chii2.mqtt.common.codec.MQTTEncoder;
import org.chii2.mqtt.server.disruptor.InboundDisruptor;
import org.chii2.mqtt.server.disruptor.OutboundDisruptor;
import org.chii2.mqtt.server.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQTT Message Server
 */
public class MQTTServer {

    // Configuration
    private final MQTTServerConfiguration configuration;
    // Storage
    private final StorageService storage;
    // The Logger
    private final Logger logger = LoggerFactory.getLogger(MQTTServer.class);

    // Disruptor
    private InboundDisruptor inboundDisruptor;
    private OutboundDisruptor outboundDisruptor;
    // Configure the Netty server.
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public MQTTServer(MQTTServerConfiguration configuration, StorageService storage) {
        this.configuration = configuration;
        this.storage = storage;
    }

    /**
     * Start the MQTT Message Server
     */
    public void start() {
        try {
            // Init Storage
            storage.start();
            storage.removeInFLightQoS0();
            // Init Disruptor
            outboundDisruptor = new OutboundDisruptor(configuration, storage);
            outboundDisruptor.start();
            inboundDisruptor = new InboundDisruptor(configuration, storage, outboundDisruptor);
            inboundDisruptor.start();
            // Using single handler and attachments to store stateful information
            final MQTTServerHandler mqttServerHandler = new MQTTServerHandler(inboundDisruptor);
            // Init Netty server
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new MQTTEncoder(),
                                    new MQTTDecoder(),
                                    mqttServerHandler);
                        }
                    });

            // Start the server.
            ChannelFuture f = b.bind(configuration.getPort()).sync();
            logger.info("{} has successfully started.", configuration.getServerName());
            // Server socket closed listener.
            f.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.info("{} has successfully stopped.", configuration.getServerName());
                }
            });
        } catch (Exception e) {
            logger.error("{} encountered fatal error: {}", configuration.getServerName(), ExceptionUtils.getMessage(e));
        }
    }

    /**
     * Stop the MQTT Message Server
     */
    public void stop() {
        // Shutdown the Disruptor
        inboundDisruptor.stop();
        outboundDisruptor.stop();
        // Shutdown all event loops to terminate all threads.
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        // Shutdown Storage
        storage.stop();
    }
}
