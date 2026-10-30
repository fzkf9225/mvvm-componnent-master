package io.coderf.arklab.ws.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link WsHandlerRegistry} 单元测试。
 */
public class WsHandlerRegistryTest {

    @Test
    public void dispatch_matchesSupportedKeys() {
        WsHandlerRegistry registry = new WsHandlerRegistry();
        AtomicInteger hits = new AtomicInteger();
        registry.register(new AbstractWsMessageHandler() {
            @Override
            public Set<String> supportedKeys() {
                return new HashSet<>(Collections.singletonList("ping"));
            }

            @Override
            public void onMessage(String key, String payload) {
                hits.incrementAndGet();
            }
        });
        assertEquals(1, registry.dispatch("ping", "{}"));
        assertEquals(0, registry.dispatch("other", "{}"));
        assertEquals(1, hits.get());
    }
}
