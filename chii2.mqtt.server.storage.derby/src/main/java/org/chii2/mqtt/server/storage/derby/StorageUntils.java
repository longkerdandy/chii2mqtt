package org.chii2.mqtt.server.storage.derby;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Create Database Tables
 */
public class StorageUntils {

    // Database Version
    public static final String DATABASE_VERSION = "1.0.0";
    // The Logger
    private static final Logger logger = LoggerFactory.getLogger(StorageUntils.class);

    public static final String TABLE_EXIST = "SELECT * FROM SYS.SYSTABLES WHERE TABLENAME = ?";
    public static final String CREATE_TABLE_VERSION = "CREATE TABLE" +
            "    APP.VERSION" +
            "    (" +
            "        UID VARCHAR(255) NOT NULL," +
            "        VERSION VARCHAR(255) NOT NULL," +
            "        PRIMARY KEY (UID)" +
            "    )";
    public static final String INSERT_VERSION = "INSERT INTO VERSION(UID, VERSION) VALUES ('CHII2_MQTT_SERVER', ?)";
    public static final String CREATE_TABLE_MESSAGE_ID = "CREATE TABLE" +
            "    APP.MESSAGE_ID" +
            "    (" +
            "        UID VARCHAR(255) NOT NULL," +
            "        NUMBER INTEGER NOT NULL," +
            "        PRIMARY KEY (UID)" +
            "    )";
    public static final String CREATE_TABLE_RETAIN = "CREATE TABLE" +
            "    APP.RETAIN" +
            "    (" +
            "        TOPIC VARCHAR(32672) NOT NULL," +
            "        MESSAGEID INTEGER NOT NULL," +
            "        QOS INTEGER NOT NULL," +
            "        CONTENT BLOB NOT NULL," +
            "        PRIMARY KEY (TOPIC)" +
            "    )";
    public static final String CREATE_TABLE_INFLIGHT = "CREATE TABLE" +
            "    APP.INFLIGHT" +
            "    (" +
            "        UID VARCHAR(255) NOT NULL," +
            "        SUBSCRIBERID VARCHAR(255) NOT NULL," +
            "        MESSAGEID INTEGER NOT NULL," +
            "        RETAIN BOOLEAN NOT NULL," +
            "        QOS INTEGER NOT NULL," +
            "        TOPIC VARCHAR(32672) NOT NULL," +
            "        CONTENT BLOB NOT NULL," +
            "        PRIMARY KEY (UID)" +
            "    )";
    public static final String CREATE_TABLE_QOS2 = "CREATE TABLE" +
            "    APP.QOS2" +
            "    (" +
            "        UID VARCHAR(255) NOT NULL," +
            "        PUBLISHERID VARCHAR(255) NOT NULL," +
            "        MESSAGEID INTEGER NOT NULL," +
            "        PRIMARY KEY (UID)" +
            "    )";
    public static final String CREATE_TABLE_SUBSCRIPTION = "CREATE TABLE" +
            "    APP.SUBSCRIPTION" +
            "    (" +
            "        UID VARCHAR(32672) NOT NULL," +
            "        TOPIC VARCHAR(32672) NOT NULL," +
            "        SUBSCRIBERID VARCHAR(255) NOT NULL," +
            "        QOS INTEGER NOT NULL," +
            "        PRIMARY KEY (UID)" +
            "    )";

    /**
     * Create tables if not exist
     *
     * @param dataSource DataSource
     */
    public static void createTables(DataSource dataSource) {
        // SQL
        PreparedStatement exist = null;
        PreparedStatement version = null;
        Statement create = null;
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            // Check tables exist
            exist = connection.prepareStatement(TABLE_EXIST,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            exist.setString(1, "VERSION");
            rs = exist.executeQuery();
            // TODO: Check version and alter table if necessary
            if (!rs.next()) {
                // Create new tables
                create = connection.createStatement();
                create.addBatch(CREATE_TABLE_VERSION);
                create.addBatch(CREATE_TABLE_MESSAGE_ID);
                create.addBatch(CREATE_TABLE_RETAIN);
                create.addBatch(CREATE_TABLE_INFLIGHT);
                create.addBatch(CREATE_TABLE_QOS2);
                create.addBatch(CREATE_TABLE_SUBSCRIPTION);
                create.executeBatch();
                version = connection.prepareStatement(INSERT_VERSION);
                version.setString(1, DATABASE_VERSION);
                version.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                DbUtils.rollback(connection);
            } catch (SQLException ignore) {
            }
            logger.error("Error when create tables: {}", ExceptionUtils.getMessage(e));
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(exist);
            DbUtils.closeQuietly(create);
            DbUtils.closeQuietly(version);
            DbUtils.closeQuietly(connection);
        }
    }
}
