package com.blr19c.common.wxCp.model.msg;

/**
 * 文本消息
 *
 * @author blr
 */
public class Text implements Msg {
    private String content;

    public Text(String content) {
        this.content = content;
    }

    public Text() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
