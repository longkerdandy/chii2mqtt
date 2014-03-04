package org.chii2.mqtt.server.storage.derby;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.common.message.PublishMessage;
import org.chii2.mqtt.server.storage.Subscription;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Storage Service Test
 */
public class StorageServiceImplTest {

    StorageServiceImpl storage;

    @BeforeClass
    public void before() {
        storage = new StorageServiceImpl();
        EmbeddedDataSource dataSource = new EmbeddedConnectionPoolDataSource();
        // It's different working directory when build with maven and test with IntelliJ IDEA
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp")) {
            dataSource.setDatabaseName("assembly/data/mqtt/server"); // Debug Mode
        } else {
            dataSource.setDatabaseName("../assembly/data/mqtt/server"); // Normal Mode
        }
        storage.setDataSource(dataSource);
        storage.clearTable("MESSAGE_ID");
        storage.clearTable("RETAIN");
        storage.clearTable("INFLIGHT");
        storage.clearTable("QOS2");
        storage.clearTable("SUBSCRIPTION");
        storage.start();
    }

    @Test
    public void messageIDTest() {
        assert storage.getNextMessageID() == 1;
        assert storage.getNextMessageID() == 2;
        assert storage.getNextMessageID() == 3;
    }

    @Test
    public void cleanTest() {
        String clientID1 = "Chii2-C8-60-00-E2-1D-5C";
        Subscription subscription1 = new Subscription("/SmartHome/Server", clientID1, 2);
        Subscription subscription2 = new Subscription("/SmartHome/Client", clientID1, 1);
        storage.putSubscription(subscription1);
        storage.putSubscription(subscription2);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put((byte) 1);
        buffer.put((byte) 3);
        buffer.put((byte) 5);
        buffer.put((byte) 7);
        buffer.put((byte) 9);
        buffer.flip();
        PublishMessage message1 = new PublishMessage(
                true,
                MQTTMessage.QoSLevel.MOST_ONCE,
                false,
                "/SmartHome/Server",
                1111,
                buffer
        );
        PublishMessage message2 = new PublishMessage(
                true,
                MQTTMessage.QoSLevel.LEAST_ONCE,
                false,
                "/SmartHome/Server",
                2222,
                buffer
        );
        storage.putInFlight(clientID1, message1);
        storage.putInFlight(clientID1, message2);

        assert storage.containsSubscription(clientID1, "/SmartHome/Server");
        assert storage.containsSubscription(clientID1, "/SmartHome/Client");
        assert storage.getInFlights(clientID1).size() == 2;
        storage.clean(clientID1);
        assert !storage.containsSubscription(clientID1, "/SmartHome/Server");
        assert !storage.containsSubscription(clientID1, "/SmartHome/Client");
        assert storage.getInFlights(clientID1).size() == 0;
    }

    @Test
    public void retainTest() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put((byte) 1);
        buffer.put((byte) 3);
        buffer.put((byte) 5);
        buffer.put((byte) 7);
        buffer.put((byte) 9);
        buffer.flip();
        PublishMessage message1 = new PublishMessage(
                true,
                MQTTMessage.QoSLevel.MOST_ONCE,
                false,
                "/SmartHome/Server",
                2020,
                buffer
        );
        PublishMessage message2 = new PublishMessage(
                true,
                MQTTMessage.QoSLevel.MOST_ONCE,
                false,
                "/SmartHome/Server/MyServer1",
                2020,
                buffer
        );

        storage.putRetain(message1);
        storage.putRetain(message2);
        PublishMessage result = storage.getRetainWildcard("/SmartHome/Server").get(0);
        assert result != null;
        assert result.getMessageID() == 2020;
        assert result.getQosLevel() == MQTTMessage.QoSLevel.MOST_ONCE;
        assert result.getTopicName().equals("/SmartHome/Server");
        assert result.isRetain();
        assert !result.isDupFlag();
        assert result.getContent().limit() == 5;
        assert result.getContent().get(3) == 7;
        assert storage.getRetainWildcard("/SmartHome/Server/#").size() == 2;
        storage.removeRetain("/SmartHome/Server");
        assert storage.getRetainWildcard("/SmartHome/Server/#").size() == 1;
    }

    @Test
    public void inFlightTest() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put((byte) 1);
        buffer.put((byte) 3);
        buffer.put((byte) 5);
        buffer.put((byte) 7);
        buffer.put((byte) 9);
        buffer.flip();
        String clientID1 = "Chii2-C8-60-00-E2-1D-5C";
        String clientID2 = "Chii2-C8-60-00-E2-1D";
        PublishMessage message1 = new PublishMessage(
                true,
                MQTTMessage.QoSLevel.MOST_ONCE,
                false,
                "/SmartHome/Server",
                1111,
                buffer
        );
        PublishMessage message2 = new PublishMessage(
                true,
                MQTTMessage.QoSLevel.LEAST_ONCE,
                false,
                "/SmartHome/Server",
                2222,
                buffer
        );

        storage.putInFlight(clientID1, message1);
        storage.putInFlight(clientID1, message2);
        storage.putInFlight(clientID2, message2);

        PublishMessage result = storage.getInFlight(clientID1, 1111);
        assert result != null;
        assert result.getMessageID() == 1111;
        assert result.getQosLevel() == MQTTMessage.QoSLevel.MOST_ONCE;
        assert result.getTopicName().equals("/SmartHome/Server");
        assert result.isRetain();
        assert !result.isDupFlag();
        assert result.getContent().limit() == 5;
        assert result.getContent().get(3) == 7;
        List<PublishMessage> results = storage.getInFlights(clientID1);
        assert results.size() == 2;
        assert storage.containsInFlight(clientID1, 1111);
        assert storage.containsInFlight(clientID1, 2222);
        assert storage.containsInFlight(clientID2, 2222);
        storage.removeInFlight(clientID1, 2222);
        results = storage.getInFlights(clientID1);
        assert results.size() == 1;
        assert storage.containsInFlight(clientID1, 1111);
        assert !storage.containsInFlight(clientID1, 2222);
        assert storage.containsInFlight(clientID2, 2222);
        storage.removeInFlight(clientID1, 1111);
        results = storage.getInFlights(clientID1);
        assert results.size() == 0;
    }

    @Test
    public void qos2Test() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put((byte) 1);
        buffer.put((byte) 3);
        buffer.put((byte) 5);
        buffer.put((byte) 7);
        buffer.put((byte) 9);
        buffer.flip();
        String clientID = "Chii2-C8-60-00-E2-1D-5C";
        PublishMessage message = new PublishMessage(
                true,
                MQTTMessage.QoSLevel.MOST_ONCE,
                false,
                "/SmartHome/Server",
                1111,
                buffer
        );
        storage.putQoS2(clientID, message);
        assert storage.containsQoS2(clientID, 1111);
        storage.removeQoS2(clientID, 2222);
        storage.removeQoS2(clientID, 1111);
        assert !storage.containsQoS2(clientID, 1111);
    }

    @Test
    public void subscriptionTest() {
        String clientID1 = "Chii2-C8-60-00-E2-1D-5C";
        String clientID2 = "Chii2-C8-60-00-E2-1D";
        Subscription subscription1 = new Subscription("/SmartHome/Server", clientID1, 2);
        Subscription subscription2 = new Subscription("/SmartHome/Client", clientID1, 1);
        Subscription subscription3 = new Subscription("/SmartHome/Server", clientID2, 1);

        storage.putSubscription(subscription1);
        storage.putSubscription(subscription2);
        storage.putSubscription(subscription3);
        assert storage.containsSubscription(clientID1, "/SmartHome/Server");
        assert storage.containsSubscription(clientID2, "/SmartHome/Server");
        assert storage.containsSubscription(clientID1, "/SmartHome/Client");
        assert !storage.containsSubscription(clientID2, "/SmartHome/Client");

        Subscription result = storage.getSubscriptions("/SmartHome/Server").get(0);
        assert result != null;
        assert result.getSubscriberID().equals(clientID1);
        assert result.getTopic().equals("/SmartHome/Server");
        assert result.getQosLevel() == 2;
        List<Subscription> results = storage.getSubscriptions("/SmartHome/Server");
        assert results.size() == 2;
        storage.removeSubscription(clientID2, "/SmartHome/Server");
        assert storage.containsSubscription(clientID1, "/SmartHome/Server");
        assert !storage.containsSubscription(clientID2, "/SmartHome/Server");
        assert storage.containsSubscription(clientID1, "/SmartHome/Client");
    }

    @AfterClass
    public void after() {
        storage.clearTable("MESSAGE_ID");
        storage.clearTable("RETAIN");
        storage.clearTable("INFLIGHT");
        storage.clearTable("QOS2");
        storage.clearTable("SUBSCRIPTION");
        storage.stop();
    }
}
