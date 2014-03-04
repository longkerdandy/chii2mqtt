package org.chii2.mqtt.server.storage;

import org.chii2.mqtt.common.message.PublishMessage;

import java.util.List;

/**
 * Storage Service
 */
public interface StorageService {

    /**
     * Start/Init the database(connection)
     */
    public void start();

    /**
     * Stop the database(connection) and clean up
     */
    public void stop();

    /**
     * Generate new Message ID
     *
     * @return new Message ID
     */
    public int getNextMessageID();

    /**
     * The server must discard any previously maintained information about the
     * client and treat the connection as "clean". The server must also discard any state when
     * the client disconnects.
     *
     * @param clientID Client ID
     */
    public void clean(String clientID);

    /**
     * Save the retain PUBLISH Message
     *
     * @param publishMessage Publish Message
     */
    public void putRetain(PublishMessage publishMessage);

    /**
     * Get the retain PUBLISH Messages for given Topic
     * TOPIC MAY CONTAINS WILDCARD
     *
     * @param topic Topic may contain wildcard
     * @return Publish Messages
     */
    public List<PublishMessage> getRetainWildcard(String topic);

    /**
     * Remove the retain PUBLISH Message for given Topic
     *
     * @param topic Topic may NOT contain wildcard
     */
    public void removeRetain(String topic);

    /**
     * Save the in-flight Message, which will be sent to subscriber
     *
     * @param subscriberID   Subscriber ID
     * @param publishMessage Publish Message
     */
    public void putInFlight(String subscriberID, PublishMessage publishMessage);

    /**
     * Get list of in-flight Messages with give Subscriber ID
     *
     * @param subscriberID Subscriber ID
     */
    public List<PublishMessage> getInFlights(String subscriberID);

    /**
     * Get the in-flight Message with give Subscriber ID and Message ID
     *
     * @param subscriberID Subscriber ID
     * @param messageID    Message ID
     */
    public PublishMessage getInFlight(String subscriberID, int messageID);

    /**
     * Remove the in-flight Message
     *
     * @param subscriberID Subscriber ID
     * @param messageID    Message ID
     */
    public void removeInFlight(String subscriberID, int messageID);

    /**
     * Remove all In-Flight Message with QoS Level 0 (Most Once)
     */
    public void removeInFLightQoS0();

    /**
     * Contains the in-flight Message
     *
     * @param subscriberID Subscriber ID
     * @param messageID    Message ID
     * @return True if the in-flight Message exist
     */
    public boolean containsInFlight(String subscriberID, int messageID);

    /**
     * Save QoS2 PUBLISH Message wait PUBREL Message to confirm
     *
     * @param publisherID    Publisher Client ID
     * @param publishMessage Publish Message
     */
    public void putQoS2(String publisherID, PublishMessage publishMessage);

    /**
     * Contains QoS2 PUBLISH Message
     *
     * @param publisherID Publisher Client ID
     * @param messageID   Message ID
     * @return True if already contains in storage
     */
    public boolean containsQoS2(String publisherID, int messageID);

    /**
     * Remove QoS2 PUBLISH Message
     *
     * @param publisherID Publisher Client ID
     * @param messageID   Message ID
     */
    public void removeQoS2(String publisherID, int messageID);

    /**
     * Save the Subscription
     *
     * @param subscription Subscription
     */
    public void putSubscription(Subscription subscription);

    /**
     * Contains the Subscription
     *
     * @param subscriberID Subscriber ID
     * @param topic        Topic
     * @return True if the Subscription exist
     */
    public boolean containsSubscription(String subscriberID, String topic);

    /**
     * Remove the Subscription
     *
     * @param subscriberID Subscriber ID
     * @param topic        Topic
     */
    public void removeSubscription(String subscriberID, String topic);

    /**
     * Get Subscriptions with given topic
     * TOPIC MUST NOT CONTAIN WILDCARD
     *
     * @param topic Topic
     * @return List of Subscription
     */
    public List<Subscription> getSubscriptions(String topic);
}
