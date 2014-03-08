package org.chii2.mqtt.server.sample;

import org.chii2.mqtt.server.MQTTServer;
import org.chii2.mqtt.server.MQTTServerConfiguration;
import org.chii2.mqtt.server.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample OSGi Server
 */
public class OSGiServer {

    // MQTT Server Configuration
    private MQTTServerConfiguration brokerConfiguration;
    // MQTT Server Storage
    private StorageService brokerStorage;
    // MQTT Broker
    private MQTTServer broker;
    // The Logger
    private final Logger logger = LoggerFactory.getLogger(OSGiServer.class);

    /**
     * Injected Storage Service
     */
    public void setBrokerStorage(StorageService brokerStorage) {
        this.brokerStorage = brokerStorage;
    }

    /**
     * OSGi life cycle init
     */
    public void init() {
        logger.info("Chii2 MQTT Server Init.");
        // Init MQTT Server Configuration
        brokerConfiguration = new MQTTServerConfiguration();
        brokerConfiguration.setServerName("Chii2 MQTT Server");
        // Init MQTT Server
        broker = new MQTTServer(brokerConfiguration, brokerStorage);
        broker.start();
    }

    /**
     * OSGi life cycle destroy
     */
    public void destroy() {
        logger.info("Chii2 MQTT Server Destroy.");
        broker.stop();
    }
}
