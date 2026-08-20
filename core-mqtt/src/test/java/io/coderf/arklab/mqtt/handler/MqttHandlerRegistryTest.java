package io.coderf.arklab.mqtt.handler;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * [MqttHandlerRegistry] 单元测试。
 *
 * @author fz
 * @version 1.2
 * @since 1.2
 * @created 2026/7/27 10:10
 */
public class MqttHandlerRegistryTest {

    @Test
    public void dispatch_invokesMatchingHandlersOnly() {
        MqttHandlerRegistry registry = new MqttHandlerRegistry();
        CountingHandler h1 = new CountingHandler("osd");
        CountingHandler h2 = new CountingHandler("state");
        registry.register(h1);
        registry.register(h2);

        int hit = registry.dispatch("osd", "t/osd", "{}");

        assertEquals(1, hit);
        assertEquals(1, h1.count);
        assertEquals(0, h2.count);
    }

    @Test
    public void dispatch_blankKey_returnsZero() {
        MqttHandlerRegistry registry = new MqttHandlerRegistry();
        registry.register(new CountingHandler("osd"));
        assertEquals(0, registry.dispatch(" ", "t", "{}"));
    }

    private static final class CountingHandler extends AbstractMqttMessageHandler {
        private final String key;
        int count = 0;

        CountingHandler(String key) {
            this.key = key;
        }

        @Override
        public java.util.Set<String> supportedKeys() {
            return java.util.Collections.singleton(key);
        }

        @Override
        public void onMessage(String key, String topic, String payload) {
            count++;
        }
    }
}
