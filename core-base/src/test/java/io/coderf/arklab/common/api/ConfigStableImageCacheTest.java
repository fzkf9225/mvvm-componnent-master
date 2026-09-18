package io.coderf.arklab.common.api;

import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Glide 稳定缓存 key 开关逻辑。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/18
 */
public class ConfigStableImageCacheTest {

    private final Config config = Config.getInstance();
    private final boolean originalEnabled = config.isStableImageCacheKeyEnabled();
    private final Set<String> originalKeys =
            new LinkedHashSet<>(config.getStableImageCacheIgnoredQueryParams());

    @After
    public void tearDown() {
        config.setStableImageCacheKeyEnabled(originalEnabled)
                .setStableImageCacheIgnoredQueryParams(originalKeys);
    }

    @Test
    public void strategyInactive_whenDisabledEvenIfKeysConfigured() {
        config.setStableImageCacheKeyEnabled(false)
                .setStableImageCacheIgnoredQueryParams(Collections.singleton("X-Amz-Signature"));
        assertFalse(config.isStableImageCacheStrategyActive());
    }

    @Test
    public void strategyInactive_whenEnabledWithoutIgnoredKeys() {
        config.setStableImageCacheKeyEnabled(true)
                .setStableImageCacheIgnoredQueryParams(Collections.emptySet());
        assertFalse(config.isStableImageCacheStrategyActive());
    }

    @Test
    public void strategyActive_whenEnabledAndIgnoredKeysConfigured() {
        config.setStableImageCacheKeyEnabled(true)
                .setStableImageCacheIgnoredQueryParams(Collections.emptySet())
                .addStableImageCacheIgnoredQueryParams("X-Amz-Signature");
        assertTrue(config.isStableImageCacheStrategyActive());
    }
}
