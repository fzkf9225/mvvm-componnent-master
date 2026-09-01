package io.coderf.arklab.wscomponent;

/**
 * webSocket消息包
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/5/5 9:52
 */
public class WebSocketBean<H,B> {
   private B body;
   private H header;

    public B getBody() {
        return body;
    }

    public void setBody(B body) {
        this.body = body;
    }

    public H getHeader() {
        return header;
    }

    public void setHeader(H header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return "WebSocketBean{" +
                "body='" + body + '\'' +
                ", header=" + header +
                '}';
    }
}
