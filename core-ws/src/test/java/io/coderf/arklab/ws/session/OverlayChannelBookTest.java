package io.coderf.arklab.ws.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * {@link OverlayChannelBook} 单元测试。
 */
public class OverlayChannelBookTest {

    @Test
    public void bindUnbind_union() {
        OverlayChannelBook book = new OverlayChannelBook();
        book.bind("d1", Arrays.asList("x", "y"));
        book.bind("d2", Collections.singletonList("z"));
        assertEquals(3, book.all().size());
        assertTrue(book.unbind("d1"));
        assertEquals(Collections.singleton("z"), book.all());
    }

    @Test
    public void normalize_trimsBlanks() {
        assertEquals(
                Collections.singleton("a"),
                OverlayChannelBook.normalizeChannels(Arrays.asList(" a ", "", "  "))
        );
    }
}
