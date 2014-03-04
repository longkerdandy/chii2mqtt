package org.chii2.mqtt.server.storage;

import io.netty.channel.ChannelHandlerContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Netty Channel Repository, Map Client ID to ChannelHandlerContext
 */
public class ChannelRepository {
    // Thread safe map
    private static Map<String, ChannelHandlerContext> REPOSITORY = Collections.synchronizedMap(new HashMap<String, ChannelHandlerContext>());

    public static void putClientChannel(String clientID, ChannelHandlerContext context) {
        REPOSITORY.put(clientID, context);
    }

    public static ChannelHandlerContext getClientChannel(String clientID) {
        return REPOSITORY.get(clientID);
    }

    public static ChannelHandlerContext removeClientChannel(String clientID) {
        return REPOSITORY.remove(clientID);
    }

    public static boolean containsClientChannel(String clientID) {
        return REPOSITORY.containsKey(clientID);
    }
}
