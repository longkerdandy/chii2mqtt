package org.chii2.mqtt.server.sample;


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Chii2 MQTT Client based on Eclipse Paho
 */
public class Chii2MQTTClient implements MqttCallback {

    // MQTT variables
    private MqttAsyncClient client;
    private String brokerUrl;
    private MqttConnectOptions conOpt;
    private boolean clean;
    private String password;
    private String userName;
    // The Logger
    private final Logger logger = LoggerFactory.getLogger(Chii2MQTTClient.class);

    /**
     * Constructs an instance of the sample client wrapper
     *
     * @param brokerUrl    the url to connect to
     * @param clientId     the client id to connect with
     * @param cleanSession clear state at end of connection or not (durable or non-durable subscriptions)
     * @param userName     the username to connect with
     * @param password     the password for the user
     */
    public Chii2MQTTClient(String brokerUrl, String clientId, boolean cleanSession,
                           String userName, String password) {
        this.brokerUrl = brokerUrl;
        this.clean = cleanSession;
        this.userName = userName;
        this.password = password;
        // Persistence Directory
        String dir = System.getProperty("user.dir") + File.separator + "data" + File.separator + "mqtt" + File.separator + "client";
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(dir);

        try {
            // Construct the connection options object that contains connection parameters
            // such as cleanSession and LWT
            conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(clean);
            if (password != null) {
                conOpt.setPassword(this.password.toCharArray());
            }
            if (userName != null) {
                conOpt.setUserName(this.userName);
            }

            // Construct a non-blocking MQTT client instance
            client = new MqttAsyncClient(this.brokerUrl, clientId, dataStore);

            // Set this wrapper as the callback handler
            client.setCallback(this);

        } catch (MqttException e) {
            logger.error("Error when create MQTT client: {}", ExceptionUtils.getMessage(e));
        }
    }

    /**
     * Connect to MQTT Server
     *
     * @throws MqttException
     */
    public void connect() throws MqttException {
        // Connect to the MQTT server
        // issue a non-blocking connect and then use the token to wait until the
        // connect completes. An exception is thrown if connect fails.
        IMqttToken conToken = client.connect(conOpt, null, null);
        conToken.waitForCompletion();
        logger.info("{} has connected to {}", client.getClientId(), brokerUrl);
    }

    /**
     * Publish / Send a message to an MQTT server
     *
     * @param topicName the name of the topic to publish to
     * @param qos       the quality of service to delivery the message at (0,1,2)
     * @param payload   the set of bytes to send to the MQTT server
     * @throws MqttException
     */
    public void publish(final String topicName, int qos, byte[] payload) throws MqttException {
        // Construct the message to send
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);

        // Setup a listener object to be notified when the publish completes.
        IMqttActionListener pubListener = new IMqttActionListener() {
            public void onSuccess(IMqttToken asyncActionToken) {
                logger.info("Publish to topic {} completed.", topicName);
            }

            public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                logger.warn("Publish to topic {} failed: {}", topicName, ExceptionUtils.getMessage(ex));
            }
        };

        // Send the message to the server, do not wait for answer
        client.publish(topicName, message, "Chii2 Publisher Content", pubListener);
    }

    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription. It continues listening for messages until the enter key is
     * pressed.
     *
     * @param topicName to subscribe to (can be wild carded)
     * @param qos       the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public void subscribe(final String topicName, int qos) throws MqttException {
        // Setup a listener object to be notified when the publish completes.
        IMqttActionListener subListener = new IMqttActionListener() {
            public void onSuccess(IMqttToken asyncActionToken) {
                logger.info("Subscribe to topic {} completed.", topicName);
            }

            public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                logger.warn("Subscribe to topic {} failed: {}", topicName, ExceptionUtils.getMessage(ex));
            }
        };

        // Subscribe to the requested topic, do not wait for answer
        client.subscribe(topicName, qos, "Chii2 Subscriber Content", subListener);
    }

    /**
     * Disconnect with MQTT Server
     *
     * @throws MqttException
     */
    public void disconnect() throws MqttException {
        // Disconnect the client
        // Issue the disconnect and then use the token to wait until
        // the disconnect completes.
        IMqttToken discToken = client.disconnect(null, null);
        discToken.waitForCompletion();
        logger.info("{} has disconnected to {}", client.getClientId(), brokerUrl);
    }


    /****************************************************************/
    /* Methods to implement the MqttCallback interface              */
    /****************************************************************/

    /**
     * Called when the connection to the server has been lost.
     * Override to provide real logic
     */
    public void connectionLost(Throwable ex) {
        logger.info("Connection to {} has lost: {}", brokerUrl, ExceptionUtils.getMessage(ex));
    }

    /**
     * Called when a message has been delivered to the server.
     * Override to provide real logic
     *
     * @param token the delivery token associated with the message.
     */
    public void deliveryComplete(IMqttDeliveryToken token) {
        // If the connection to the server breaks before delivery has completed
        // delivery of a message will complete after the client has re-connected.
        // The getPendinTokens method will provide tokens for any messages
        // that are still to be delivered.
    }

    /**
     * Called when a message arrives from the server that matches any subscription made by the client
     * Override to provide real logic
     */
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
        logger.info("Received a new message for topic: {}", topic);
    }

    /****************************************************************/
    /* End of MqttCallback methods                                  */
    /****************************************************************/
}
