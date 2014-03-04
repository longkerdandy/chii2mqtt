package org.chii2.mqtt.server.disruptor;

import com.lmax.disruptor.EventHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.chii2.mqtt.common.message.*;
import org.chii2.mqtt.server.storage.ChannelRepository;
import org.chii2.mqtt.server.storage.StorageService;
import org.chii2.mqtt.server.storage.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.chii2.mqtt.server.MQTTServerHandler.*;

/**
 * MQTT Message Inbound Journal Processor
 */
public class InboundProcessor implements EventHandler<InboundMQTTEvent> {
    // The Logger
    private final Logger logger = LoggerFactory.getLogger(InboundProcessor.class);
    // Storage
    private StorageService storage;
    // Outbound Disruptor
    private OutboundDisruptor outboundDisruptor;

    public InboundProcessor(StorageService storage, OutboundDisruptor outboundDisruptor) {
        this.storage = storage;
        this.outboundDisruptor = outboundDisruptor;
    }

    @Override
    public void onEvent(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) throws Exception {
        MQTTMessage message = event.getMQTTMessage();
        if (message instanceof ConnectMessage) {
            onConnect(event, sequenceNumber, endOfBatch);
        } else if (message instanceof DisconnectMessage) {
            onDisconnect(event, sequenceNumber, endOfBatch);
        } else {
            // Discard if NOT Authorized
            if (!getAuthorization(event.getContext())) {
                return;
            }

            if (message instanceof PublishMessage) {
                onPublish(event, sequenceNumber, endOfBatch);
            } else if (message instanceof PubAckMessage) {
                onPubAck(event, sequenceNumber, endOfBatch);
            } else if (message instanceof PubRecMessage) {
                onPubRec(event, sequenceNumber, endOfBatch);
            } else if (message instanceof PubRelMessage) {
                onPubRel(event, sequenceNumber, endOfBatch);
            } else if (message instanceof PubCompMessage) {
                onPubComp(event, sequenceNumber, endOfBatch);
            } else if (message instanceof SubscribeMessage) {
                onSubscribe(event, sequenceNumber, endOfBatch);
            } else if (message instanceof UnsubscribeMessage) {
                onUnsubscribe(event, sequenceNumber, endOfBatch);
            } else if (message instanceof PingReqMessage) {
                onPingReq(event, sequenceNumber, endOfBatch);
            }
        }
    }

    /**
     * Received MQTT CONNECT Message from a client
     *
     * @param event          InboundMQTTEvent which contains a CONNECT Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    protected void onConnect(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        ChannelHandlerContext context = event.getContext();
        ConnectMessage connectMessage = (ConnectMessage) event.getMQTTMessage();
        ConnAckMessage connAckMessage;
        String clientID = connectMessage.getClientID();
        boolean auth = false;
        // Unacceptable Protocol Version
        if (!connectMessage.isAcceptableProtocolVersion()) {
            connAckMessage = new ConnAckMessage(ConnAckMessage.ReturnCode.UNACCEPTABLE_PROTOCOL_VERSION);
        }
        // Unacceptable Client ID
        else if (!connectMessage.isAcceptableClientID()) {
            connAckMessage = new ConnAckMessage(ConnAckMessage.ReturnCode.IDENTIFIER_REJECTED);
        }
        // User Name and Password Provided
        else if (connectMessage.isUserNameFlag()) {
            // Authorized
            if (authConnect(clientID, connectMessage.getUserName(), connectMessage.getPassword())) {
                // Has been authorized
                auth = true;
                connAckMessage = new ConnAckMessage(ConnAckMessage.ReturnCode.CONNECTION_ACCEPTED);
            }
            // Rejected
            else {
                connAckMessage = new ConnAckMessage(ConnAckMessage.ReturnCode.BAD_USERNAME_OR_PASSWORD);
            }
        }
        // User Name and Password NOT Provided
        else {
            // Authorized
            if (authConnect(clientID, null, null)) {
                // Has been authorized
                auth = true;
                connAckMessage = new ConnAckMessage(ConnAckMessage.ReturnCode.CONNECTION_ACCEPTED);
            }
            // Rejected
            else {
                connAckMessage = new ConnAckMessage(ConnAckMessage.ReturnCode.NOT_AUTHORIZED);
            }
        }

        // Save state
        setAuthorization(context, auth);
        setClientID(context, clientID);

        // Send Message
        sendMessage(context, connAckMessage, clientID);

        // Send In-Flight Messages
        if (auth) {
            // Save the context to ChannelRepository
            ChannelRepository.putClientChannel(clientID, context);
            // Clean Session
            if (connectMessage.isCleanSession()) {
                storage.clean(clientID);
            }
            // TODO: Handle Will Flag
            // TODO: Handle Keep Alive Timer
            // Send In-Flight Messages
            List<PublishMessage> inFlightMessages = storage.getInFlights(clientID);
            for (PublishMessage inFlightMessage : inFlightMessages) {
                outboundDisruptor.pushEvent(new OutboundMQTTEventTranslator(clientID, false, 0, 0, inFlightMessage.getMessageID(), inFlightMessage.getQosLevel().byteValue(), MQTTMessage.MessageType.PUBLISH));
            }
        }
    }

    /**
     * Received MQTT PUBLISH Message from a publisher
     * The action of the recipient when it receives a message depends on the
     * QoS level of the message:
     * QoS 0
     * Make the message available to any interested parties.
     * QoS 1
     * Log the message to persistent storage, make it available to any interested parties,
     * and return a PUBACK message to the sender.
     * QoS 2
     * Log the message to persistent storage, do not make it available to interested
     * parties yet, and return a PUBREC message to the sender.
     * <p/>
     * If the server receives the message, interested parties means subscribers to the topic of
     * the PUBLISH message.
     *
     * @param event          InboundMQTTEvent which contains a PUBLISH Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    protected void onPublish(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        ChannelHandlerContext context = event.getContext();
        String publisherID = getClientID(context);
        PublishMessage publishMessage = (PublishMessage) event.getMQTTMessage();
        MQTTMessage.QoSLevel qos = publishMessage.getQosLevel();
        // Retain flag is only used on PUBLISH messages. When a client sends a PUBLISH to a
        // server, if the Retain flag is set (1), the server should hold on to the message after
        // it has been delivered to the current subscribers.
        // When a new subscription is established on a topic, the last retained message on
        // that topic should be sent to the subscriber with the Retain flag set. If there is no
        // retained message, nothing is sent
        // When a server sends a PUBLISH to a client as a result of a subscription that
        // already existed when the original PUBLISH arrived, the Retain flag should not be
        // set, regardless of the Retain flag of the original PUBLISH. This allows a client to
        // distinguish messages that are being received because they were retained and
        // those that are being received "live".
        // Retained messages should be kept over restarts of the server.
        // A server may delete a retained message if it receives a message with a zero-length
        // payload and the Retain flag set on the same topic.
        if (publishMessage.isRetain()) {
            if (publishMessage.hasContent()) {
                // Save the retain message to persistent storage
                storage.putRetain(publishMessage);
            } else {
                // Remove the retain message to from persistent storage
                storage.removeRetain(publishMessage.getTopicName());
            }
        }

        if (qos == MQTTMessage.QoSLevel.MOST_ONCE) {
            // Notify OutboundDisruptor to publish message to clients.
            outboundDisruptor.pushPublish(publishMessage);
        } else if (qos == MQTTMessage.QoSLevel.LEAST_ONCE) {
            // Send PUBACK Message
            PubAckMessage pubAckMessage = new PubAckMessage(publishMessage.getMessageID());
            sendMessage(context, pubAckMessage, publisherID);
            // Notify OutboundDisruptor to publish message to clients.
            outboundDisruptor.pushPublish(publishMessage);
        } else if (qos == MQTTMessage.QoSLevel.EXACTLY_ONCE) {
            // Send PUBREC Message
            PubRecMessage pubRecMessage = new PubRecMessage(publishMessage.getMessageID());
            sendMessage(context, pubRecMessage, publisherID);
            // Only NOT Duplicated
            if (!publishMessage.isDupFlag() || !storage.containsQoS2(publisherID, publishMessage.getMessageID())) {
                // Save QoS2 Message
                storage.putQoS2(publisherID, publishMessage);
                // Notify OutboundDisruptor to publish message to clients.
                outboundDisruptor.pushPublish(publishMessage);
            }
        }
    }

    /**
     * Received MQTT PUBACK Message from a subscriber in response to a PUBLISH message from the server
     * Step1: QoS 1 PUBLISH Message send from Sever to Subscriber
     * Step2: PUBACK Message send from Subscriber to Server
     *
     * @param event          InboundMQTTEvent which contains a PUBACK Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    protected void onPubAck(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        ChannelHandlerContext context = event.getContext();
        String subscriberID = getClientID(context);
        PubAckMessage pubAckMessage = (PubAckMessage) event.getMQTTMessage();
        // Message received by subscriber, no need to resend
        storage.removeInFlight(subscriberID, pubAckMessage.getMessageID());
    }

    /**
     * Received MQTT PUBREC Message from a subscriber in response to a PUBLISH message from the server
     * Step1: QoS 2 PUBLISH Message send from Sever to Subscriber
     * Step2: PUBREC Message send from Subscriber to Server
     * <p/>
     * When it receives a PUBREC message, the recipient sends a PUBREL message to the
     * sender with the same Message ID as the PUBREC message.
     *
     * @param event          InboundMQTTEvent which contains a PUBREC Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    private void onPubRec(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        ChannelHandlerContext context = event.getContext();
        String subscriberID = getClientID(context);
        PubRecMessage pubRecMessage = (PubRecMessage) event.getMQTTMessage();
        // TODO: Use OutboundDisruptor to send/resend PUBREL Message
        // Send PUBREL Message
        PubRelMessage pubRelMessage = new PubRelMessage(false, pubRecMessage.getMessageID());
        sendMessage(context, pubRelMessage, subscriberID);
        // Message received by subscriber, no need to resend
        storage.removeInFlight(subscriberID, pubRecMessage.getMessageID());
    }

    /**
     * Received MQTT PUBREL Message from a publisher to a PUBREC message from the server
     * Step1: QoS 2 PUBLISH Message send from Publisher to Server
     * Step2: PUBREC Message send from Server to Publisher
     * Step3: PUBREL Message send from Publisher to Server
     * <p/>
     * When the server receives a PUBREL message from a publisher, the server makes the
     * original message available to interested subscribers, and sends a PUBCOMP message
     * with the same Message ID to the publisher.
     *
     * @param event          InboundMQTTEvent which contains a PUBREL Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    protected void onPubRel(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        ChannelHandlerContext context = event.getContext();
        String publisherID = getClientID(context);
        PubRelMessage pubRelMessage = (PubRelMessage) event.getMQTTMessage();
        // Send PUBCOMP Message
        PubCompMessage pubCompMessage = new PubCompMessage(pubRelMessage.getMessageID());
        sendMessage(context, pubCompMessage, publisherID);
        // Remove cached QoS2 PUBLISH Message
        storage.removeQoS2(publisherID, pubRelMessage.getMessageID());
    }

    /**
     * Received MQTT PUBCOMP Message from a subscriber to a PUBREL message from the server
     * Step1: QoS 1 PUBLISH Message send from Sever to Subscriber
     * Step2: PUBREC Message send from Subscriber to Server
     * Step3: PUBREL Message send from Server to Subscriber
     * Step4: PUBCOMP Message send from Subscriber to Server
     *
     * @param event          InboundMQTTEvent which contains a PUBCOMP Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    protected void onPubComp(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        // Nothing to do
    }

    /**
     * Received MQTT SUBSCRIBE Message from a subscriber
     * The SUBSCRIBE message allows a client to register an interest in one or more topic
     * names with the server. Messages published to these topics are delivered from the
     * server to the client as PUBLISH messages. The SUBSCRIBE message also specifies the
     * QoS level at which the subscriber wants to receive published messages.
     * <p/>
     * Assuming that the requested QoS level is granted, the client receives PUBLISH
     * messages at less than or equal to this level, depending on the QoS level of the original
     * message from the publisher. For example, if a client has a QoS level 1 subscription to a
     * particular topic, then a QoS level 0 PUBLISH message to that topic is delivered to the
     * client at QoS level 0. A QoS level 2 PUBLISH message to the same topic is downgraded
     * to QoS level 1 for delivery to the client.
     *
     * @param event          InboundMQTTEvent which contains a SUBSCRIBE Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    protected void onSubscribe(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        ChannelHandlerContext context = event.getContext();
        String subscriberID = getClientID(context);
        SubscribeMessage subscribeMessage = (SubscribeMessage) event.getMQTTMessage();
        List<SubscribeMessage.Topic> topics = subscribeMessage.getTopics();
        List<MQTTMessage.QoSLevel> grantedQoS = new ArrayList<>();
        // Change the QoS Level based on Server side logic
        for (SubscribeMessage.Topic topic : topics) {
            topic.setQosLevel(getGrantedQoS(context, topic.getTopicName(), topic.getQosLevel()));
            grantedQoS.add(topic.getQosLevel());
        }
        // Send SUBACK Message
        SubAckMessage subAckMessage = new SubAckMessage(subscribeMessage.getMessageID(), grantedQoS);
        sendMessage(context, subAckMessage, subscriberID);

        // Save the subscriptions and publish the retain message
        for (SubscribeMessage.Topic topic : topics) {
            // Only NOT Duplicated
            if (!subscribeMessage.isDupFlag() || !storage.containsSubscription(subscriberID, topic.getTopicName())) {
                Subscription subscription = new Subscription(topic.getTopicName(), subscriberID, topic.getQosLevel().byteValue());
                // Save subscriptions
                storage.putSubscription(subscription);
                // Notify OutboundDisruptor to publish retain message with given Topics.
                List<PublishMessage> retainMessages = storage.getRetainWildcard(topic.getTopicName());
                for (PublishMessage retainMessage : retainMessages) {
                    outboundDisruptor.pushRetain(subscriberID, topic.getQosLevel(), retainMessage);
                }
            }
        }
    }

    /**
     * Received MQTT UNSUBSCRIBE Message from a subscriber
     * An UNSUBSCRIBE message is sent by the client to the server to unsubscribe from
     * named topics.
     *
     * @param event          InboundMQTTEvent which contains a UNSUBSCRIBE Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    protected void onUnsubscribe(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        ChannelHandlerContext context = event.getContext();
        String subscriberID = getClientID(context);
        UnsubscribeMessage unsubscribeMessage = (UnsubscribeMessage) event.getMQTTMessage();
        // Send UNSUBACK Message
        UnsubAckMessage unsubAckMessage = new UnsubAckMessage(unsubscribeMessage.getMessageID());
        sendMessage(context, unsubAckMessage, subscriberID);
        // Remove subscriptions
        for (String topic : unsubscribeMessage.getTopicNames()) {
            storage.removeSubscription(subscriberID, topic);
        }
    }

    /**
     * Received MQTT PINGREQ Message from a client
     * The PINGREQ message is an "are you alive?" message that is sent from a connected
     * client to the server.
     *
     * @param event          InboundMQTTEvent which contains a PINGREQ Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    protected void onPingReq(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        ChannelHandlerContext context = event.getContext();
        sendMessage(context, new PingRespMessage(), getClientID(context));
    }

    /**
     * Received MQTT DISCONNECT Message from a client
     * The DISCONNECT message is sent from the client to the server to indicate that it is
     * about to close its TCP/IP connection. This allows for a clean disconnection, rather than
     * just dropping the line.
     * If the client had connected with the clean session flag set, then all previously
     * maintained information about the client will be discarded.
     * A server should not rely on the client to close the TCP/IP connection after receiving a
     * DISCONNECT.
     *
     * @param event          InboundMQTTEvent which contains a DISCONNECT Message
     * @param sequenceNumber Disruptor sequence number
     * @param endOfBatch     Disruptor is end of batch
     */
    protected void onDisconnect(InboundMQTTEvent event, long sequenceNumber, boolean endOfBatch) {
        ChannelHandlerContext context = event.getContext();
        final String clientID = getClientID(context);
        setAuthorization(context, false);
        // Try to close the channel
        ChannelFuture future = context.disconnect();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                logger.info("{} connection has been closed.", clientID);
            }
        });
    }

    /**
     * Override this method to provide real connect authorization logic
     *
     * @param clientID Client ID
     * @param userName User Name
     * @param password Password
     * @return True if connect authorized
     */
    protected boolean authConnect(String clientID, String userName, String password) {
        return true;
    }

    /**
     * Determine Topic's granted QoS Level for given Client ID
     * Current logic simply returns the input QoS Level.
     * Override this method to provide vendor specific validation logic.
     *
     * @param context   ChannelHandlerContext
     * @param topicName Topic Name client subscribed to
     * @param qos       QoS Level client required
     * @return Granted QoS Level
     */
    protected MQTTMessage.QoSLevel getGrantedQoS(ChannelHandlerContext context, String topicName, MQTTMessage.QoSLevel qos) {
        return qos;
    }

    /**
     * Send MQTT Message to client
     * Because using Netty, this should not blocking
     *
     * @param context  ChannelHandlerContext
     * @param message  MQTT Message
     * @param clientID Client ID
     */
    protected void sendMessage(ChannelHandlerContext context, final MQTTMessage message, final String clientID) {
        ChannelFuture future = context.writeAndFlush(message);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                logger.info("{} Message has been sent to {}.", message.getMessageType(), clientID);
            }
        });
    }
}
