package org.chii2.mqtt.server.sample;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample OSGi Client act as both Publisher and Subscriber
 */
public class OSGiClient extends Chii2MQTTClient {

    // The Logger
    private final Logger logger = LoggerFactory.getLogger(OSGiClient.class);

    /**
     * Constructor
     */
    public OSGiClient() {
        super("tcp://localhost:1883", "Chii2_HA_Server", false, null, null);
    }

    /**
     * OSGi life cycle init
     */
    public void init() {
        logger.info("Chii2 Home Automation Server Init.");
        // With a valid set of arguments, the real work of
        // driving the client API can begin
        try {
            // Perform the specified action
            connect();
            subscribe("/Chii2/Server", 1);
            publish("/Chii2/Server", 1, "Hello Home Automation. 1!".getBytes());
            publish("/Chii2/Server", 1, "Hello Home Automation. 2!".getBytes());
            publish("/Chii2/Server", 1, "Hello Home Automation. 3!".getBytes());
            publish("/Chii2/Server", 1, "Hello Home Automation. 4!".getBytes());
            publish("/Chii2/Server", 1, "Hello Home Automation. 5!".getBytes());

        } catch (MqttException me) {
            // Display full details of any exception that occurs
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        } catch (Throwable th) {
            System.out.println("Throwable caught " + th);
            th.printStackTrace();
        }
    }

    /**
     * OSGi life cycle destroy
     */
    public void destroy() {
        logger.info("Chii2 Home Automation Server Destroy.");
    }

}
