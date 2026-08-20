package io.coderf.arklab.mqtt.session;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Overlay 主题簿纯逻辑单测（无 Android 依赖）。
 */
public class OverlayTopicBookTest {

    @Test
    public void bindUnbindAndUnion() {
        OverlayTopicBook book = new OverlayTopicBook();
        Object dialog = new Object();
        Object sheet = new Object();

        Set<String> afterBind = book.bind(dialog, Arrays.asList(" a/osd ", "", "b/cmd"));
        assertEquals(setOf("a/osd", "b/cmd"), afterBind);
        assertEquals(setOf("a/osd", "b/cmd"), book.get(dialog));

        book.bind(sheet, Collections.singletonList("c/event"));
        assertEquals(setOf("a/osd", "b/cmd", "c/event"), book.all());

        assertEquals(
                setOf("page/1", "a/osd", "b/cmd", "c/event"),
                OverlayTopicBook.union(setOf("page/1"), book.all())
        );

        assertTrue(book.unbind(dialog));
        assertEquals(setOf("c/event"), book.all());
        assertFalse(book.unbind(dialog));

        book.clear();
        assertTrue(book.isEmpty());
        assertEquals(setOf("page/1"), OverlayTopicBook.union(
                setOf("page/1"), book.all()));
        assertEquals(setOf("page/1"), OverlayTopicBook.union(
                setOf("page/1"), Collections.emptySet()));
        assertEquals(setOf("only-overlay"), OverlayTopicBook.union(
                Collections.emptySet(), setOf("only-overlay")));
    }

    @Test
    public void bindEmptyRemovesToken() {
        OverlayTopicBook book = new OverlayTopicBook();
        Object token = "t1";
        book.bind(token, Collections.singletonList("x"));
        book.bind(token, Collections.emptyList());
        assertTrue(book.isEmpty());
        assertEquals(Collections.emptySet(), book.get(token));
    }

    private static Set<String> setOf(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
