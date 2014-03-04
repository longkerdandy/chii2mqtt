package org.chii2.mqtt.server;

import org.testng.annotations.Test;

/**
 * Server Configuration Test
 */
public class MQTTServerConfigurationTest {

    @Test
    public void serverIDTest() {
        String id = new MQTTServerConfiguration().generateServeriD();
        assert id.length() == 23;
    }
}
