package org.chii2.mqtt.server.disruptor;

import com.lmax.disruptor.dsl.Disruptor;
import org.chii2.mqtt.server.MQTTServerConfiguration;
import org.chii2.mqtt.server.storage.StorageService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Inbound Disruptor
 * Receives MQTT Message from Netty handler and route to Processors.
 * Refer to LMX Disruptor web site for more information.
 */
public class InboundDisruptor {

    // Processor threads count
    private static final int EVENT_PROCESSORS_NUM = 1;

    private final ExecutorService executor;
    private final Disruptor<InboundMQTTEvent> disruptor;

    public InboundDisruptor(MQTTServerConfiguration configuration, StorageService storage, OutboundDisruptor outboundDisruptor) {
        executor = Executors.newFixedThreadPool(EVENT_PROCESSORS_NUM);
        disruptor = new Disruptor<>(InboundMQTTEvent.factory, configuration.getInboundRingBuffer(), executor);
        disruptor.handleEventsWith(new InboundProcessor(storage, outboundDisruptor));
    }

    public void start() {
        disruptor.start();
    }

    public void stop() {
        disruptor.shutdown();
        executor.shutdown();
    }

    public void pushEvent(InboundMQTTEventTranslator translator) {
        disruptor.publishEvent(translator);
    }
}
