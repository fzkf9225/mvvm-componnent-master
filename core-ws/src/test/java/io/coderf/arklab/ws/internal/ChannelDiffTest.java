package io.coderf.arklab.ws.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * {@link ChannelDiff} 单元测试。
 */
public class ChannelDiffTest {

    @Test
    public void diff_addsAndRemoves() {
        ChannelDiff.Result result = ChannelDiff.diff(
                Arrays.asList("a", "b"),
                Arrays.asList("b", "c")
        );
        assertEquals(Collections.singleton("c"), result.getToSubscribe());
        assertEquals(Collections.singleton("a"), result.getToUnsubscribe());
        assertTrue(result.hasChanges());
    }

    @Test
    public void diff_noChanges() {
        ChannelDiff.Result result = ChannelDiff.diff(
                Arrays.asList("a", "b"),
                Arrays.asList("b", "a")
        );
        assertTrue(result.getToSubscribe().isEmpty());
        assertTrue(result.getToUnsubscribe().isEmpty());
        assertFalse(result.hasChanges());
    }
}
