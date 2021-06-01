package com.blr19c.common.wxCp.model.msg;


import com.blr19c.common.collection.PictogramMap;

import java.util.Map;

/**
 * 文本卡片消息
 *
 * @author blr
 */
public class TextCard implements Msg, Escape {
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 点击链接
     */
    private String url;
    /**
     * 按钮显示文字
     */
    private String btnTxt;

    public TextCard(String title, String description, String url, String btnTxt) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.btnTxt = btnTxt;
    }

    public TextCard() {
    }

    @Override
    public Map<String, String> toMap() {
        return PictogramMap.toPictogramMapAsModel(this, this::toLowerCaseKey).getMap();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBtnTxt() {
        return btnTxt;
    }

    public void setBtnTxt(String btnTxt) {
        this.btnTxt = btnTxt;
    }
}
