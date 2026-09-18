package io.coderf.arklab.common.glide;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link UrlCacheKeyNormalizer} 单元测试。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/18
 */
public class UrlCacheKeyNormalizerTest {

    private static final String SIGNED_URL =
            "http://112.26.18.103:15506/file-api/bladex/upload/20260904/"
                    + "6ca39dc3ce9dd24f9cce78c94d58d61d.png"
                    + "?X-Amz-Algorithm=AWS4-HMAC-SHA256"
                    + "&X-Amz-Credential=minioadmin%2F20260918%2Fus-east-1%2Fs3%2Faws4_request"
                    + "&X-Amz-Date=20260918T070627Z"
                    + "&X-Amz-Expires=3600"
                    + "&X-Amz-SignedHeaders=host"
                    + "&X-Amz-Signature=d660b092b844fdbb8b14fc7e3f4703a2cfd2c30dd8fd8b90d18d3fb6cc3bdddf";

    private static final String WITHOUT_SIGNATURE =
            "http://112.26.18.103:15506/file-api/bladex/upload/20260904/"
                    + "6ca39dc3ce9dd24f9cce78c94d58d61d.png"
                    + "?X-Amz-Algorithm=AWS4-HMAC-SHA256"
                    + "&X-Amz-Credential=minioadmin%2F20260918%2Fus-east-1%2Fs3%2Faws4_request"
                    + "&X-Amz-Date=20260918T070627Z"
                    + "&X-Amz-Expires=3600"
                    + "&X-Amz-SignedHeaders=host";

    @Test
    public void normalize_stripsOnlyConfiguredSignature() {
        assertEquals(WITHOUT_SIGNATURE,
                UrlCacheKeyNormalizer.normalize(SIGNED_URL, Collections.singleton("X-Amz-Signature")));
    }

    @Test
    public void normalize_doesNotStripPathOnly() {
        String result = UrlCacheKeyNormalizer.normalize(
                SIGNED_URL, Collections.singleton("X-Amz-Signature"));
        assertNotEquals(
                "http://112.26.18.103:15506/file-api/bladex/upload/20260904/"
                        + "6ca39dc3ce9dd24f9cce78c94d58d61d.png",
                result);
        assertEquals(WITHOUT_SIGNATURE, result);
    }

    @Test
    public void normalize_sameKeyWhenOnlySignatureDiffers() {
        String otherSignature = SIGNED_URL.replace(
                "d660b092b844fdbb8b14fc7e3f4703a2cfd2c30dd8fd8b90d18d3fb6cc3bdddf",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        String ignored = "X-Amz-Signature";
        assertEquals(
                UrlCacheKeyNormalizer.normalize(SIGNED_URL, Collections.singleton(ignored)),
                UrlCacheKeyNormalizer.normalize(otherSignature, Collections.singleton(ignored)));
    }

    @Test
    public void normalize_emptyIgnoreListReturnsOriginal() {
        assertEquals(SIGNED_URL, UrlCacheKeyNormalizer.normalize(SIGNED_URL, Collections.emptySet()));
        assertEquals(SIGNED_URL, UrlCacheKeyNormalizer.normalize(SIGNED_URL, null));
    }

    @Test
    public void normalize_isCaseInsensitive() {
        assertEquals(WITHOUT_SIGNATURE,
                UrlCacheKeyNormalizer.normalize(SIGNED_URL, Collections.singleton("x-amz-signature")));
    }

    @Test
    public void normalize_keepsUnmatchedQuery() {
        String url = "https://cdn.example.com/a.png?w=200&X-Amz-Signature=abc";
        assertEquals("https://cdn.example.com/a.png?w=200",
                UrlCacheKeyNormalizer.normalize(url, Collections.singleton("X-Amz-Signature")));
    }

    @Test
    public void normalize_preservesFragment() {
        String url = "https://cdn.example.com/a.png?X-Amz-Signature=abc#section";
        assertEquals("https://cdn.example.com/a.png#section",
                UrlCacheKeyNormalizer.normalize(url, Collections.singleton("X-Amz-Signature")));
    }

    @Test
    public void normalize_multipleIgnoredKeys() {
        String url = SIGNED_URL;
        String result = UrlCacheKeyNormalizer.normalize(
                url, Arrays.asList("X-Amz-Signature", "X-Amz-Date"));
        assertEquals(
                "http://112.26.18.103:15506/file-api/bladex/upload/20260904/"
                        + "6ca39dc3ce9dd24f9cce78c94d58d61d.png"
                        + "?X-Amz-Algorithm=AWS4-HMAC-SHA256"
                        + "&X-Amz-Credential=minioadmin%2F20260918%2Fus-east-1%2Fs3%2Faws4_request"
                        + "&X-Amz-Expires=3600"
                        + "&X-Amz-SignedHeaders=host",
                result);
    }

    @Test
    public void normalize_nullAndBlankUrl() {
        assertNull(UrlCacheKeyNormalizer.normalize(null, Collections.singleton("X-Amz-Signature")));
        assertEquals("", UrlCacheKeyNormalizer.normalize("", Collections.singleton("X-Amz-Signature")));
        String noQuery = "https://cdn.example.com/a.png";
        assertEquals(noQuery,
                UrlCacheKeyNormalizer.normalize(noQuery, Collections.singleton("X-Amz-Signature")));
    }
}
