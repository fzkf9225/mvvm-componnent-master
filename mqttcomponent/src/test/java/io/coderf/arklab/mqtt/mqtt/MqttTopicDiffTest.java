package io.coderf.arklab.mqtt.mqtt;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * [MqttTopicDiff] 单元测试。
 *
 * @author fz
 * @version 1.2
 * @since 1.2
 * @created 2026/7/27 10:10
 */
public class MqttTopicDiffTest {

    @Test
    public void diff_addsAndRemovesCorrectly() {
        Set<String> current = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> desired = new HashSet<>(Arrays.asList("b", "c", "d"));

        MqttTopicDiff.Result result = MqttTopicDiff.diff(current, desired);

        assertEquals(Collections.singleton("d"), result.getToSubscribe());
        assertEquals(Collections.singleton("a"), result.getToUnsubscribe());
        assertTrue(result.hasChanges());
    }

    @Test
    public void diff_emptyWhenUnchanged() {
        Set<String> topics = new HashSet<>(Arrays.asList("x", "y"));
        MqttTopicDiff.Result result = MqttTopicDiff.diff(topics, topics);
        assertTrue(result.getToSubscribe().isEmpty());
        assertTrue(result.getToUnsubscribe().isEmpty());
        assertFalse(result.hasChanges());
    }

    @Test
    public void diff_ignoresBlankTopics() {
        MqttTopicDiff.Result result = MqttTopicDiff.diff(
                Arrays.asList("a", "", " "),
                Arrays.asList("a", "b", "")
        );
        assertEquals(Collections.singleton("b"), result.getToSubscribe());
        assertTrue(result.getToUnsubscribe().isEmpty());
    }
}
