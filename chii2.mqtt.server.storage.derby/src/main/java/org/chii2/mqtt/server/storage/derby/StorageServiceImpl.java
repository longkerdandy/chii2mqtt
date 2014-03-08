package org.chii2.mqtt.server.storage.derby;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.chii2.mqtt.common.message.MQTTMessage;
import org.chii2.mqtt.common.message.PublishMessage;
import org.chii2.mqtt.common.utils.MQTTUtils;
import org.chii2.mqtt.server.MQTTServerUtils;
import org.chii2.mqtt.server.storage.StorageService;
import org.chii2.mqtt.server.storage.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MQTT Server Storage based on JDBC & SQL
 */
public class StorageServiceImpl implements StorageService {

    // Data Source
    private DataSource dataSource;
    // The Logger
    private final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

    /**
     * Injected DataSource
     *
     * @param dataSource DataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * OSGi life cycle init
     */
    public void init() {
        logger.info("MQTT Server JDBC Storage Init.");
    }

    /**
     * OSGi life cycle destroy
     */
    public void destroy() {
        logger.info("MQTT Server JDBC Storage Destroy.");
    }

    @Override
    public void start() {
        // Automatically create database (tables)
        StorageUntils.createTables(dataSource);
    }

    @Override
    public void stop() {
    }

    @Override
    public int getNextMessageID() {
        // Next Message ID
        int nextID = 0;
        // SQL
        String selectSQL = "SELECT * FROM MESSAGE_ID mi WHERE mi.UID = ?";
        PreparedStatement select = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            // Query
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            select = connection.prepareStatement(selectSQL,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            select.setString(1, "id");
            rs = select.executeQuery();

            // Calculate next id
            if (!rs.next()) {
                nextID = getNextMessageID(nextID);
                rs.moveToInsertRow();
                rs.updateString("UID", "id");
                rs.updateInt("NUMBER", nextID);
                rs.insertRow();
                rs.moveToCurrentRow();
            } else {
                nextID = getNextMessageID(rs.getInt("NUMBER"));
                rs.updateInt("NUMBER", nextID);
                rs.updateRow();
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when get next Message ID: {}", ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(select);
            DbUtils.closeQuietly(connection);
        }
        return nextID;
    }

    @Override
    public void clean(String clientID) {
        // Query Runner
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            // Delete Subscription
            runner.update("DELETE FROM SUBSCRIPTION WHERE SUBSCRIBERID = ?", clientID);
            // Delete In-Flight Messages
            runner.update("DELETE FROM INFLIGHT WHERE SUBSCRIBERID = ?", clientID);
        } catch (SQLException e) {
            logger.error("Error when clear saved information with client {}: {}", clientID, ExceptionUtils.getMessage(e));
        }
    }

    @Override
    public void putRetain(PublishMessage publishMessage) {
        // SQL
        String updateSQL = "UPDATE RETAIN SET MESSAGEID = ?, QOS = ?, CONTENT = ? WHERE TOPIC = ?";
        PreparedStatement update = null;
        String insertSQL = "INSERT INTO RETAIN(TOPIC, MESSAGEID, QOS, CONTENT) VALUES (?, ?, ?, ?)";
        PreparedStatement insert = null;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            update = connection.prepareStatement(updateSQL);
            update.setInt(1, publishMessage.getMessageID());
            update.setInt(2, publishMessage.getQosLevel().byteValue());
            update.setBytes(3, MQTTServerUtils.ByteBufferToByteArray(publishMessage.getContent()));
            update.setString(4, publishMessage.getTopicName());
            if (update.executeUpdate() <= 0) {
                insert = connection.prepareStatement(insertSQL);
                insert.setString(1, publishMessage.getTopicName());
                insert.setInt(2, publishMessage.getMessageID());
                insert.setInt(3, publishMessage.getQosLevel().byteValue());
                insert.setBytes(4, MQTTServerUtils.ByteBufferToByteArray(publishMessage.getContent()));
                insert.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when put retain Message with topic {}: {}", publishMessage.getTopicName(), ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(update);
            DbUtils.closeQuietly(insert);
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<PublishMessage> getRetainWildcard(String topic) {
        // Result
        List<PublishMessage> messages = new ArrayList<>();
        // SQL
        String selectTopicsSQL = "SELECT DISTINCT TOPIC FROM RETAIN";
        PreparedStatement selectTopics = null;
        String selectSQL = "SELECT * FROM RETAIN r WHERE r.TOPIC = ?";
        PreparedStatement select = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            // Get distinct topics from retain
            List<String> retainTopics = new ArrayList<>();
            selectTopics = connection.prepareStatement(selectTopicsSQL);
            rs = selectTopics.executeQuery();
            while (rs.next()) {
                retainTopics.add(rs.getString("TOPIC"));
            }
            // Get retain messages match given topic
            for (String retainTopic : retainTopics) {
                if (MQTTUtils.isTopicMatch(topic, retainTopic, false)) {
                    select = connection.prepareStatement(selectSQL);
                    select.setString(1, retainTopic);
                    rs = select.executeQuery();
                    if (rs.next()) {
                        messages.add(new PublishMessage(
                                true,
                                MQTTMessage.QoSLevel.values()[rs.getInt("QOS")],
                                false,
                                rs.getString("TOPIC"),
                                rs.getInt("MESSAGEID"),
                                MQTTServerUtils.ByteArrayToByteBuffer(rs.getBytes("CONTENT"))));
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when get retain Message with topic {}: {}", topic, ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(selectTopics);
            DbUtils.closeQuietly(select);
            DbUtils.closeQuietly(connection);
        }
        return messages;
    }

    @Override
    public void removeRetain(String topic) {
        // Query Runner
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            runner.update("DELETE FROM RETAIN WHERE TOPIC = ?", topic);
        } catch (SQLException e) {
            logger.error("Error when remove retain Message with topic {}: {}", topic, ExceptionUtils.getMessage(e));
        }
    }

    @Override
    public void putInFlight(String subscriberID, PublishMessage publishMessage) {
        // SQL
        String updateSQL = "UPDATE INFLIGHT SET SUBSCRIBERID = ?, MESSAGEID = ?, RETAIN = ?, QOS = ?, TOPIC = ?, CONTENT = ? WHERE UID = ?";
        PreparedStatement update = null;
        String insertSQL = "INSERT INTO INFLIGHT(UID, SUBSCRIBERID, MESSAGEID, RETAIN, QOS, TOPIC, CONTENT) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement insert = null;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            update = connection.prepareStatement(updateSQL);
            update.setString(1, subscriberID);
            update.setInt(2, publishMessage.getMessageID());
            update.setBoolean(3, publishMessage.isRetain());
            update.setInt(4, publishMessage.getQosLevel().byteValue());
            update.setString(5, publishMessage.getTopicName());
            update.setBytes(6, MQTTServerUtils.ByteBufferToByteArray(publishMessage.getContent()));
            update.setString(7, MQTTServerUtils.generateUID(subscriberID, String.valueOf(publishMessage.getMessageID())));
            if (update.executeUpdate() <= 0) {
                insert = connection.prepareStatement(insertSQL);
                insert.setString(1, MQTTServerUtils.generateUID(subscriberID, String.valueOf(publishMessage.getMessageID())));
                insert.setString(2, subscriberID);
                insert.setInt(3, publishMessage.getMessageID());
                insert.setBoolean(4, publishMessage.isRetain());
                insert.setInt(5, publishMessage.getQosLevel().byteValue());
                insert.setString(6, publishMessage.getTopicName());
                insert.setBytes(7, MQTTServerUtils.ByteBufferToByteArray(publishMessage.getContent()));
                insert.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when put in-flight Message with Subscriber ID {} and Message ID {}: {}", subscriberID, publishMessage.getMessageID(), ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(update);
            DbUtils.closeQuietly(insert);
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<PublishMessage> getInFlights(String subscriberID) {
        // Result
        List<PublishMessage> messages = new ArrayList<>();
        // SQL
        String selectSQL = "SELECT * FROM INFLIGHT if WHERE if.SUBSCRIBERID = ?";
        PreparedStatement select = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            select = connection.prepareStatement(selectSQL);
            select.setString(1, subscriberID);
            rs = select.executeQuery();
            while (rs.next()) {
                messages.add(new PublishMessage(
                        rs.getBoolean("RETAIN"),
                        MQTTMessage.QoSLevel.values()[rs.getInt("QOS")],
                        false,
                        rs.getString("TOPIC"),
                        rs.getInt("MESSAGEID"),
                        MQTTServerUtils.ByteArrayToByteBuffer(rs.getBytes("CONTENT"))));
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when get in-flight Messages with Subscriber ID {}: {}", subscriberID, ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(select);
            DbUtils.closeQuietly(connection);
        }
        return messages;
    }

    @Override
    public PublishMessage getInFlight(String subscriberID, int messageID) {
        // Result
        PublishMessage message = null;
        // SQL
        String selectSQL = "SELECT * FROM INFLIGHT if WHERE if.UID = ?";
        PreparedStatement select = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            select = connection.prepareStatement(selectSQL);
            select.setString(1, MQTTServerUtils.generateUID(subscriberID, String.valueOf(messageID)));
            rs = select.executeQuery();
            if (rs.next()) {
                message = new PublishMessage(
                        rs.getBoolean("RETAIN"),
                        MQTTMessage.QoSLevel.values()[rs.getInt("QOS")],
                        false,
                        rs.getString("TOPIC"),
                        rs.getInt("MESSAGEID"),
                        MQTTServerUtils.ByteArrayToByteBuffer(rs.getBytes("CONTENT")));
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when get in-flight Message with Subscriber ID {} and Message ID {}: {}", subscriberID, messageID, ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(select);
            DbUtils.closeQuietly(connection);
        }
        return message;
    }

    @Override
    public void removeInFlight(String subscriberID, int messageID) {
        // Query Runner
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            runner.update("DELETE FROM INFLIGHT WHERE UID = ?", MQTTServerUtils.generateUID(subscriberID, String.valueOf(messageID)));
        } catch (SQLException e) {
            logger.error("Error when remove in-flight Message with Subscriber ID {} and Message ID {}: {}", subscriberID, messageID, ExceptionUtils.getMessage(e));
        }
    }

    @Override
    public void removeInFLightQoS0() {
        // Query Runner
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            runner.update("DELETE FROM INFLIGHT WHERE QOS = 0");
        } catch (SQLException e) {
            logger.error("Error when remove in-flight Messages with QoS Level 0: {}", ExceptionUtils.getMessage(e));
        }
    }

    @Override
    public boolean containsInFlight(String subscriberID, int messageID) {
        // Result
        boolean contain = false;
        // SQL
        String selectSQL = "SELECT * FROM INFLIGHT if WHERE if.UID = ?";
        PreparedStatement select = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            select = connection.prepareStatement(selectSQL);
            select.setString(1, MQTTServerUtils.generateUID(subscriberID, String.valueOf(messageID)));
            rs = select.executeQuery();
            if (rs.next()) {
                contain = true;
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when test contain in-flight Message with Subscriber ID {} and Message ID {}: {}", subscriberID, messageID, ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(select);
            DbUtils.closeQuietly(connection);
        }
        return contain;
    }

    @Override
    public void putQoS2(String publisherID, PublishMessage publishMessage) {
        // SQL
        String updateSQL = "UPDATE QOS2 SET PUBLISHERID = ?, MESSAGEID = ? WHERE UID = ?";
        PreparedStatement update = null;
        String insertSQL = "INSERT INTO QOS2(UID, PUBLISHERID, MESSAGEID) VALUES (?, ?, ?)";
        PreparedStatement insert = null;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            update = connection.prepareStatement(updateSQL);
            update.setString(1, publisherID);
            update.setInt(2, publishMessage.getMessageID());
            update.setString(3, MQTTServerUtils.generateUID(publisherID, String.valueOf(publishMessage.getMessageID())));
            if (update.executeUpdate() <= 0) {
                insert = connection.prepareStatement(insertSQL);
                insert.setString(1, MQTTServerUtils.generateUID(publisherID, String.valueOf(publishMessage.getMessageID())));
                insert.setString(2, publisherID);
                insert.setInt(3, publishMessage.getMessageID());
                insert.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when put QoS Level 2 Message with Publisher ID {} and Message ID {}: {}", publisherID, publishMessage.getMessageID(), ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(update);
            DbUtils.closeQuietly(insert);
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public boolean containsQoS2(String publisherID, int messageID) {
        // Result
        boolean contain = false;
        // SQL
        String selectSQL = "SELECT * FROM QOS2 q WHERE q.UID = ?";
        PreparedStatement select = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            select = connection.prepareStatement(selectSQL);
            select.setString(1, MQTTServerUtils.generateUID(publisherID, String.valueOf(messageID)));
            rs = select.executeQuery();
            if (rs.next()) {
                contain = true;
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when test contain QoS Level 2 Message with Publisher ID {} and Message ID {}: {}", publisherID, messageID, ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(select);
            DbUtils.closeQuietly(connection);
        }
        return contain;
    }

    @Override
    public void removeQoS2(String publisherID, int messageID) {
        // Query Runner
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            runner.update("DELETE FROM QOS2 WHERE UID = ?", MQTTServerUtils.generateUID(publisherID, String.valueOf(messageID)));
        } catch (SQLException e) {
            logger.error("Error when remove QOS Level 2 Message with Publisher ID {} and Message ID {}: {}", publisherID, messageID, ExceptionUtils.getMessage(e));
        }
    }

    @Override
    public void putSubscription(Subscription subscription) {
        // SQL
        String updateSQL = "UPDATE SUBSCRIPTION SET TOPIC = ?, SUBSCRIBERID = ?, QOS = ? WHERE UID = ?";
        PreparedStatement update = null;
        String insertSQL = "INSERT INTO SUBSCRIPTION(UID, TOPIC, SUBSCRIBERID, QOS) VALUES (?, ?, ?, ?)";
        PreparedStatement insert = null;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            update = connection.prepareStatement(updateSQL);
            update.setString(1, subscription.getTopic());
            update.setString(2, subscription.getSubscriberID());
            update.setInt(3, subscription.getQosLevel());
            update.setString(4, MQTTServerUtils.generateUID(subscription.getSubscriberID(), subscription.getTopic()));
            if (update.executeUpdate() <= 0) {
                insert = connection.prepareStatement(insertSQL);
                insert.setString(1, MQTTServerUtils.generateUID(subscription.getSubscriberID(), subscription.getTopic()));
                insert.setString(2, subscription.getTopic());
                insert.setString(3, subscription.getSubscriberID());
                insert.setInt(4, subscription.getQosLevel());
                insert.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when put Subscription with Topic {} and Subscriber ID {}: {}", subscription.getTopic(), subscription.getSubscriberID(), ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(update);
            DbUtils.closeQuietly(insert);
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public boolean containsSubscription(String subscriberID, String topic) {
        // Result
        boolean contain = false;
        // SQL
        String selectSQL = "SELECT * FROM SUBSCRIPTION s WHERE s.UID = ?";
        PreparedStatement select = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            select = connection.prepareStatement(selectSQL);
            select.setString(1, MQTTServerUtils.generateUID(subscriberID, topic));
            rs = select.executeQuery();
            if (rs.next()) {
                contain = true;
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when test contain Subscription with Topic {} and Subscriber ID {}: {}", topic, subscriberID, ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(select);
            DbUtils.closeQuietly(connection);
        }
        return contain;
    }

    @Override
    public void removeSubscription(String subscriberID, String topic) {
        // Query Runner
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            runner.update("DELETE FROM SUBSCRIPTION WHERE UID = ?", MQTTServerUtils.generateUID(subscriberID, topic));
        } catch (SQLException e) {
            logger.error("Error when remove Subscription with Topic {} and Subscriber ID {}: {}", topic, subscriberID, ExceptionUtils.getMessage(e));
        }
    }

    @Override
    public List<Subscription> getSubscriptions(String topic) {
        // Result
        List<Subscription> subscriptions = new ArrayList<>();
        // SQL
        String selectTopicsSQL = "SELECT DISTINCT TOPIC FROM SUBSCRIPTION";
        PreparedStatement selectTopics = null;
        String selectSQL = "SELECT * FROM SUBSCRIPTION s WHERE s.TOPIC = ?";
        PreparedStatement select = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            // Get distinct topics from subscription
            List<String> subTopics = new ArrayList<>();
            selectTopics = connection.prepareStatement(selectTopicsSQL);
            rs = selectTopics.executeQuery();
            while (rs.next()) {
                subTopics.add(rs.getString("TOPIC"));
            }
            // Get subscriptions match given topic
            for (String subTopic : subTopics) {
                if (MQTTUtils.isTopicMatch(subTopic, topic, false)) {
                    select = connection.prepareStatement(selectSQL);
                    select.setString(1, subTopic);
                    rs = select.executeQuery();
                    while (rs.next()) {
                        subscriptions.add(new Subscription(
                                topic,
                                rs.getString("SUBSCRIBERID"),
                                rs.getInt("QOS")));
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error when get subscriptions with topic {}: {}", topic, ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(selectTopics);
            DbUtils.closeQuietly(select);
            DbUtils.closeQuietly(connection);
        }
        return subscriptions;
    }

    /**
     * Generate next Message ID, Range [1, 65535]
     *
     * @param number Number
     * @return Next Message ID
     */
    protected int getNextMessageID(int number) {
        if (number == 65535) {
            return 1;
        } else {
            return number + 1;
        }
    }

    /**
     * Clear specific table data
     *
     * @param table Table
     * @return Number of rows affected
     */
    protected int clearTable(String table) {
        // Affected rows
        int rows = 0;
        // Query Runner
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            switch (table.toUpperCase()) {
                case "MESSAGE_ID":
                    rows = runner.update("DELETE FROM MESSAGE_ID");
                    break;
                case "RETAIN":
                    rows = runner.update("DELETE FROM RETAIN");
                    break;
                case "INFLIGHT":
                    rows = runner.update("DELETE FROM INFLIGHT");
                    break;
                case "QOS2":
                    rows = runner.update("DELETE FROM QOS2");
                    break;
                case "SUBSCRIPTION":
                    rows = runner.update("DELETE FROM SUBSCRIPTION");
                    break;
            }
        } catch (SQLException e) {
            logger.error("Error when clear table {}: {}", table.toUpperCase(), ExceptionUtils.getMessage(e));
        }
        return rows;
    }
}
