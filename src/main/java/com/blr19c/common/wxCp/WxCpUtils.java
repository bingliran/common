package com.blr19c.common.wxCp;

import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.wxCp.config.WxCp;
import com.blr19c.common.wxCp.enums.MediaTypeEnum;
import com.blr19c.common.wxCp.enums.MsgTypeEnum;
import com.blr19c.common.wxCp.model.RequestStyle;
import com.blr19c.common.wxCp.model.ResponseStyle;
import com.blr19c.common.wxCp.model.msg.*;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.blr19c.common.io.WebFluxUtils.*;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

/**
 * 发送工具
 *
 * @author blr
 */
@SuppressWarnings(value = "unused")
public class WxCpUtils {

    /**
     * 发送给所有人
     */
    public static final String ALL_USER = "@all";

    private static WxCp wxCp;

    public static void setWxCp(WxCp wxCp) {
        WxCpUtils.wxCp = wxCp;
    }

    /**
     * 推送企业消息
     *
     * @param msg   消息体
     * @param user  发送给的用户列表 @all 为所有
     * @param tag   发送给的消息标签(用户所有时失效)
     * @param party 发送给的部门(用户所有时失效)
     */
    public static ResponseStyle send(Msg msg, List<String> user, List<String> tag, List<String> party) {
        RequestStyle requestStyle = new RequestStyle();
        requestStyle.setAgentId(wxCp.getAgentId());
        requestStyle.setMsg(msg);
        requestStyle.setMsgType(getMsgTypeEnum(msg));
        requestStyle.setToParty(party);
        requestStyle.setToTag(tag);
        requestStyle.setToUser(user);
        return send(requestStyle);
    }

    /**
     * 发送完整的requestStyle
     */
    public static ResponseStyle send(RequestStyle requestStyle) {
        return toResponseStyle(
                postLaunch(getSendUrl(), requestStyle.getRequest(wxCp).getMap(), HashMap.class, WxCpUtils::toError)
        );
    }

    /**
     * 给一定用户发送文本消息 所有用@all 可以使用|拼接用户
     */
    public static ResponseStyle send(String text, String user) {
        return send(text, Lists.newArrayList(user));
    }

    /**
     * 给一定用户发送文本消息 user为列表
     * 参数可以为 {userId,userId|userId,@all} 这几种
     */
    public static ResponseStyle send(String text, List<String> user) {
        return send(new Text(text), user, null, null);
    }

    /**
     * 给一定用户发送文本卡片 user为列表
     * 参数可以为 {userId,userId|userId,@all} 这几种
     */
    public static ResponseStyle send(TextCard textCard, List<String> user) {
        return send(textCard, user, null, null);
    }

    /**
     * 给一定用户发送文本卡片 所有用@all 可以使用|拼接用户
     */
    public static ResponseStyle send(TextCard textCard, String user) {
        return send(textCard, Lists.newArrayList(user));
    }

    /**
     * 给用户发送一个文件
     */
    public static ResponseStyle send(InputStream file, String fileName, MediaTypeEnum typeEnum, String user) throws IOException {
        return send(file, fileName, typeEnum, Lists.newArrayList(user));
    }

    /**
     * 给用户发送一个文件 user为列表
     * 参数可以为 {userId,userId|userId,@all} 这几种
     */
    public static ResponseStyle send(InputStream file, String fileName, MediaTypeEnum typeEnum, List<String> user) throws IOException {
        return send(file, fileName, null, null, typeEnum, user);
    }

    /**
     * 给用户发送一个文件 user为列表 description和title 仅在视频文件时生效
     * 参数可以为 {userId,userId|userId,@all} 这几种
     */
    public static ResponseStyle send(InputStream file, String fileName, String title, String description, MediaTypeEnum typeEnum, List<String> user) throws IOException {
        try {
            File msgFile = (File) MsgTypeEnum.value(typeEnum).getClsType().newInstance();
            msgFile.setMediaId(getMediaId(toByteArray(file), String.valueOf(fileName), typeEnum));
            if (msgFile instanceof Video) {
                ((Video) msgFile).setDescription(description);
                ((Video) msgFile).setTitle(title);
            }
            return send(msgFile, user, null, null);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Serialization error", e);
        } finally {
            if (file != null) file.close();
        }
    }

    /**
     * 获取消息类型
     */
    private static MsgTypeEnum getMsgTypeEnum(Msg msg) {
        for (MsgTypeEnum value : MsgTypeEnum.values()) {
            if (value.getClsType().equals(msg.getClass()))
                return value;
        }
        throw new IllegalArgumentException("Message type does not exist");
    }

    /**
     * 生成ResponseStyle
     */
    private static ResponseStyle toResponseStyle(Map<?, ?> launch) {
        try {
            return PictogramMap.toPictogramMap(launch)
                    .toModel(ResponseStyle.class, (map, name) -> MapUtils.getString(map, name.toLowerCase()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Serialization error", e);
        }
    }

    /**
     * 上传素材并获取mediaId
     */
    private static String getMediaId(byte[] data, String fileName, MediaTypeEnum mediaTypeEnum) {
        try {
            checkMediaSize(mediaTypeEnum, data);
            PictogramMap pictogramMap = PictogramMap.toPictogramMap(
                    upFileCall(getLoadUrl(mediaTypeEnum), "media", fileName, data, HashMap.class)
            );
            if (pictogramMap.equalsValue("errcode", 0)) return pictogramMap.getString("media_id");
            throw new IllegalArgumentException("Failed to upload material");
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to upload material", e);
        }
    }

    /**
     * 根据类型校验素材大小是否正常
     */
    private static void checkMediaSize(MediaTypeEnum mediaTypeEnum, byte[] data) {
        if (data.length < 5)
            throw new IllegalArgumentException("The material file is too small");
        if (mediaTypeEnum.getMaxMB() * 1024 * 1024 < data.length)
            throw new IllegalArgumentException("The material file is too large");
    }

    /**
     * 上传素材的url
     */
    private static String getLoadUrl(MediaTypeEnum mediaTypeEnum) {
        return fromHttpUrl(wxCp.getLoadUrl())
                .queryParam("access_token", getToken())
                .queryParam("type", mediaTypeEnum.getType())
                .toUriString();
    }

    /**
     * 发送消息的url
     */
    private static String getSendUrl() {
        return fromHttpUrl(wxCp.getSendUrl())
                .queryParam("access_token", getToken())
                .build()
                .toString();
    }

    /**
     * token的url
     */
    private static String getTokenUrl() {
        return fromHttpUrl(wxCp.getTokenUrl())
                .queryParam("corpid", wxCp.getCorpId())
                .queryParam("corpsecret", wxCp.getCorpSecret())
                .build()
                .toString();
    }

    /**
     * 获取token
     */
    private static Object getToken() {
        PictogramMap tokenInfo = PictogramMap.toPictogramMap(getLaunch(getTokenUrl(), null, HashMap.class));
        if (tokenInfo.equalsValue("errcode", 0)) return tokenInfo.getString("access_token");
        throw new IllegalArgumentException("Failed to obtain token");
    }

    /**
     * 消息推送失败
     */
    private static void toError(Throwable throwable) {
        throw new IllegalArgumentException("Failed to push enterprise message", throwable);
    }
}
