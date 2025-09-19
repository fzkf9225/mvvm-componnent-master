package pers.fz.mvvm.utils.encode;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Base64Util {
    private static final char[] BASE64_ENCODE_CHARS = new char[]{'A', 'B', 'C',
            'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c',
            'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    private static final byte[] BASE64_DECODE_CHARS = new byte[]{-1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59,
            60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1,
            -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
            38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1,
            -1, -1};

    private Base64Util() {
    }

    public static void main(String[] args) {
        String encode = encode("hello world".getBytes());
        System.out.println("加密："+encode);
        System.out.println("解密："+new String(decode(encode), Charset.defaultCharset()));
        System.out.println("智能解码："+new String(smartDecode(encode), Charset.defaultCharset()));
    }

    /**
     * 将字节数组编码为字符串
     *
     * @param data
     */
    public static String encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int len = data.length;
        int i = 0;
        int b1, b2, b3;

        while (i < len) {
            b1 = data[i++] & 0xff;
            if (i == len) {
                sb.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
                sb.append(BASE64_ENCODE_CHARS[(b1 & 0x3) << 4]);
                sb.append("==");
                break;
            }
            b2 = data[i++] & 0xff;
            int i1 = ((b1 & 0x03) << 4)
                    | ((b2 & 0xf0) >>> 4);
            if (i == len) {
                sb.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
                sb.append(BASE64_ENCODE_CHARS[i1]);
                sb.append(BASE64_ENCODE_CHARS[(b2 & 0x0f) << 2]);
                sb.append("=");
                break;
            }
            b3 = data[i++] & 0xff;
            sb.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
            sb.append(BASE64_ENCODE_CHARS[i1]);
            sb.append(BASE64_ENCODE_CHARS[((b2 & 0x0f) << 2)
                    | ((b3 & 0xc0) >>> 6)]);
            sb.append(BASE64_ENCODE_CHARS[b3 & 0x3f]);
        }
        return sb.toString();
    }

    /**
     * 将base64字符串解码为字节数组
     *
     * @param str
     */
    public static byte[] decode(String str) {
        byte[] data = str.getBytes();
        int len = data.length;
        ByteArrayOutputStream buf = new ByteArrayOutputStream(len);
        int i = 0;
        int b1, b2, b3, b4;

        while (i < len) {

			/* b1 */
            do {
                b1 = BASE64_DECODE_CHARS[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1) {
                break;
            }

			/* b2 */
            do {
                b2 = BASE64_DECODE_CHARS[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1) {
                break;
            }
            buf.write((int) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

			/* b3 */
            do {
                b3 = data[i++];
                if (b3 == 61) {
                    return buf.toByteArray();
                }
                b3 = BASE64_DECODE_CHARS[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1) {
                break;
            }
            buf.write(((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2));

			/* b4 */
            do {
                b4 = data[i++];
                if (b4 == 61) {
                    return buf.toByteArray();
                }
                b4 = BASE64_DECODE_CHARS[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1) {
                break;
            }
            buf.write(((b3 & 0x03) << 6) | b4);
        }
        return buf.toByteArray();
    }

    /**
     * 通用智能解码：自动处理各种格式的Data URI前缀
     * @param input 可能包含Data URI前缀的字符串
     * @return 解码后的字节数组
     */
    public static byte[] smartDecode(String input) {
        if (input == null || input.isEmpty()) {
            return new byte[0];
        }

        // 检查是否是Data URI格式
        if (isDataUri(input)) {
            return extractAndDecodeDataUri(input);
        }

        // 不是Data URI，直接解码
        return decode(cleanBase64String(input));
    }

    /**
     * 判断是否是Data URI格式
     */
    public static boolean isDataUri(String input) {
        return input != null && input.startsWith("data:") && input.contains(",");
    }

    /**
     * 从Data URI中提取并解码数据
     */
    public static byte[] extractAndDecodeDataUri(String dataUri) {
        if (!isDataUri(dataUri)) {
            return decode(dataUri);
        }

        int commaIndex = dataUri.indexOf(',');
        String base64Data = dataUri.substring(commaIndex + 1);

        // 检查是否是base64编码
        boolean isBase64 = dataUri.substring(0, commaIndex).contains(";base64");

        if (isBase64) {
            return decode(cleanBase64String(base64Data));
        } else {
            // 如果不是base64编码，可能是URL编码的文本数据
            return urlDecodeToBytes(base64Data);
        }
    }

    /**
     * 提取Data URI的完整媒体类型（包含参数）
     */
    public static String extractMediaType(String dataUri) {
        if (!isDataUri(dataUri)) {
            return null;
        }

        int commaIndex = dataUri.indexOf(',');
        String header = dataUri.substring(0, commaIndex);

        // 移除 "data:" 前缀
        return header.substring(5);
    }

    /**
     * 提取Data URI的MIME类型（不包含参数）
     */
    public static String extractMimeType(String dataUri) {
        String mediaType = extractMediaType(dataUri);
        if (mediaType == null) {
            return null;
        }

        // 移除base64标识和参数
        int semicolonIndex = mediaType.indexOf(';');
        if (semicolonIndex != -1) {
            return mediaType.substring(0, semicolonIndex);
        }
        return mediaType;
    }

    /**
     * 检查Data URI是否是base64编码
     */
    public static boolean isBase64Encoded(String dataUri) {
        if (!isDataUri(dataUri)) {
            return false;
        }

        int commaIndex = dataUri.indexOf(',');
        String header = dataUri.substring(0, commaIndex);
        return header.contains(";base64");
    }

    /**
     * 创建通用的Data URI
     * @param data 原始数据
     * @param mediaType 媒体类型，如 "image/png", "video/mp4" 等
     * @param base64 是否使用base64编码
     * @return Data URI字符串
     */
    public static String createDataUri(byte[] data, String mediaType, boolean base64) {
        if (base64) {
            String encoded = encode(data);
            return "data:" + mediaType + (base64 ? ";base64" : "") + "," + encoded;
        } else {
            // 对于文本数据，可以直接使用URL编码
            String textData = new String(data, StandardCharsets.UTF_8);
            return "data:" + mediaType + "," + urlEncode(textData);
        }
    }

    /**
     * 清理Base64字符串
     */
    public static String cleanBase64String(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\n", "").replace("\r", "").replace(" ", "").replace("\t", "");
    }

    /**
     * URL编码（简单实现）
     */
    private static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

    /**
     * URL解码为字节数组
     */
    private static byte[] urlDecodeToBytes(String encoded) {
        try {
            String decoded = URLDecoder.decode(encoded, "UTF-8");
            return decoded.getBytes(StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return encoded.getBytes(StandardCharsets.UTF_8);
        }
    }
}

