package com.casic.otitan.wscomponent;

/**
 * Created by fz on 2023/5/5 09:52
 * describe:webSocket消息包
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
