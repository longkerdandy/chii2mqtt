package org.chii2.mqtt.server.disruptor;

import com.lmax.disruptor.dsl.Disruptor;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.common.message.PublishMessage;
import org.chii2.mqtt.server.MQTTServerConfiguration;
import org.chii2.mqtt.server.MQTTServerUtils;
import org.chii2.mqtt.server.storage.StorageService;
import org.chii2.mqtt.server.storage.Subscription;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.chii2.mqtt.common.message.MQTTMessage.QoSLevel;

/**
 * Outbound Disruptor
 * Receive Outbound event from InboundProcessor and handled by OutboundProcessor.
 * Refer to LMX Disruptor web site for more information.
 */
public class OutboundDisruptor {

    // Processor threads count
    private static final int EVENT_PROCESSORS_NUM = 2;
    // Storage
    private StorageService storage;
    // LMX Disruptor
    private final ExecutorService executor;
    private final Disruptor<OutboundMQTTEvent> disruptor;

    public OutboundDisruptor(MQTTServerConfiguration configuration, StorageService storage) {
        this.storage = storage;
        executor = Executors.newFixedThreadPool(EVENT_PROCESSORS_NUM);
        disruptor = new Disruptor<>(OutboundMQTTEvent.factory, configuration.getOutboundRingBuffer(), executor);
        disruptor.handleEventsWith(new OutboundProcessor(storage)).then(new OutboundResendProcessor(configuration.getInterval(), configuration.getMaxRetryTimes(), storage, this));
    }

    public void start() {
        disruptor.start();
    }

    public void stop() {
        disruptor.shutdown();
        executor.shutdown();
    }

    /**
     * Push the Publish Message to all the subscribers
     *
     * @param publishMessage Publish Message
     */
    public void pushPublish(PublishMessage publishMessage) {
        // Get subscriptions
        List<Subscription> subscriptions = storage.getSubscriptions(publishMessage.getTopicName());
        // New Message ID
        int messageID = storage.getNextMessageID();
        // Publish to every subscribers
        for (Subscription subscription : subscriptions) {
            // In-Flight Message use Subscription QoS Level and Server generated Message ID
            QoSLevel qos = MQTTServerUtils.getLowerQoS(QoSLevel.values()[subscription.getQosLevel()], publishMessage.getQosLevel());
            PublishMessage inFlightMessage = new PublishMessage(publishMessage);
            inFlightMessage.setRetain(false);
            inFlightMessage.setMessageID(messageID);
            inFlightMessage.setDupFlag(false);
            inFlightMessage.setQosLevel(qos);
            // Save to in-flight messages
            storage.putInFlight(subscription.getSubscriberID(), inFlightMessage);
            // Push the event
            pushEvent(new OutboundMQTTEventTranslator(subscription.getSubscriberID(), false, 0, 0, messageID, qos.byteValue(), MQTTMessage.MessageType.PUBLISH));
        }
    }

    /**
     * Push the retained Publish Message to the subscriber
     * This only happens when new client subscribed to a topic
     *
     * @param subscriberID   Subscriber ID
     * @param qosLevel       Subscribed QoS Level
     * @param publishMessage Publish Message
     */
    public void pushRetain(String subscriberID, QoSLevel qosLevel, PublishMessage publishMessage) {
        // New Message ID
        int messageID = storage.getNextMessageID();
        // In-Flight Message use Subscription QoS Level and Server generated Message ID
        QoSLevel qos = MQTTServerUtils.getLowerQoS(qosLevel, publishMessage.getQosLevel());
        PublishMessage inFlightMessage = new PublishMessage(publishMessage);
        inFlightMessage.setRetain(true);
        inFlightMessage.setMessageID(messageID);
        inFlightMessage.setDupFlag(false);
        inFlightMessage.setQosLevel(qos);
        // Save to in-flight messages
        storage.putInFlight(subscriberID, inFlightMessage);
        // Push the event
        pushEvent(new OutboundMQTTEventTranslator(subscriberID, false, 0, 0, messageID, qos.byteValue(), MQTTMessage.MessageType.PUBLISH));
    }

    /**
     * Push the Outbound Event to the disruptor's ring buffer
     *
     * @param translator Outbound Event Translator
     */
    public void pushEvent(OutboundMQTTEventTranslator translator) {
        disruptor.publishEvent(translator);
    }
}
