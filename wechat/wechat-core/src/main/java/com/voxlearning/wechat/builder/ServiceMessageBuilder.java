package com.voxlearning.wechat.builder;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.wechat.constants.MessageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客服消息builder
 *
 * @author Xin Xin
 * @since 10/20/15
 */
public class ServiceMessageBuilder {
    private final Map<String, Object> context = new HashMap<>();

    public ServiceMessageBuilder(String toUser){
        buildToUser(toUser);
    }

    public ServiceMessageBuilder buildTxtMsg(String content){
        buildMsgType(MessageType.TEXT);
        buildContent(content);
        return this;
    }

    public ServiceMessageBuilder buildArticleMsg(String title, String description,String picUrl, String url){
        buildMsgType(MessageType.NEWS);
        buildArticle(title,description,url,picUrl);
        return this;
    }

    private ServiceMessageBuilder buildToUser(String openId) {
        context.put("touser", openId);
        return this;
    }

    private ServiceMessageBuilder buildMsgType(MessageType type) {
        switch (type) {
            case TEXT:
            case IMAGE:
            case VOICE:
            case VIDEO:
            case MUSIC:
            case NEWS:
                context.put("msgtype", type.getType());
                break;
            default:
                throw new IllegalStateException("invalid message type");
        }
        return this;
    }

    private ServiceMessageBuilder buildContent(String content) {
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("content", content);

        context.put("text", contentMap);
        return this;
    }

    private ServiceMessageBuilder buildImageMediaId(String mediaId) {
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("media_id", mediaId);

        context.put("image", contentMap);
        return this;
    }

    private ServiceMessageBuilder buildVoiceMediaId(String mediaId) {
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("media_id", mediaId);

        context.put("voice", contentMap);
        return this;
    }

    private ServiceMessageBuilder buildVideoMediaId(String mediaId) {
        checkVideoMap();

        ((Map<String, String>) context.get("video")).put("media_id", mediaId);
        return this;
    }

    private ServiceMessageBuilder buildVideoThumbMediaId(String mediaId) {
        checkVideoMap();

        ((Map<String, String>) context.get("video")).put("thumb_media_id", mediaId);
        return this;
    }

    private ServiceMessageBuilder buildVideoTitle(String title) {
        checkVideoMap();

        ((Map<String, String>) context.get("video")).put("title", title);
        return this;
    }

    private ServiceMessageBuilder buildVideoDesciption(String description) {
        checkVideoMap();

        ((Map<String, String>) context.get("video")).put("description", description);
        return this;
    }

    private ServiceMessageBuilder buildMusicTitle(String title) {
        checkMusicMap();

        ((Map<String, String>) context.get("music")).put("title", title);
        return this;
    }

    private ServiceMessageBuilder buildMusicDesciption(String desc) {
        checkMusicMap();

        ((Map<String, String>) context.get("music")).put("desciption", desc);
        return this;
    }

    private ServiceMessageBuilder buildMusicUrl(String url) {
        checkMusicMap();

        ((Map<String, String>) context.get("music")).put("musicurl", url);
        return this;
    }

    private ServiceMessageBuilder buildHqMusicUrl(String hqUrl) {
        checkMusicMap();

        ((Map<String, String>) context.get("music")).put("hqmusicurl", hqUrl);
        return this;
    }

    private ServiceMessageBuilder buildThumbMediaId(String mediaId) {
        checkMusicMap();

        ((Map<String, String>) context.get("music")).put("thumb_media_id", mediaId);
        return this;
    }

    private ServiceMessageBuilder buildArticle(String title, String description, String url, String picUrl) {
        checkArticleMap();

        Map<String, String> articleMap = new HashMap<>();
        articleMap.put("title", title);
        articleMap.put("description", description);
        articleMap.put("url", url);
        articleMap.put("picurl", picUrl);

        ((List<Map<String, String>>) ((Map<String, Object>) context.get("news")).get("articles")).add(articleMap);
        return this;
    }


    private void checkVideoMap() {
        Map<String, String> contentMap = (Map<String, String>) context.get("video");
        if (null == contentMap) {
            Map<String, String> map = new HashMap<>();
            context.put("video", map);
        }
    }

    private void checkMusicMap() {
        Map<String, String> contentMap = (Map<String, String>) context.get("music");

        if (null == contentMap) {
            context.put("music", new HashMap<>());
        }
    }

    private void checkArticleMap() {
        Map<String, Object> newsMap = (Map<String, Object>) context.get("news");
        if (null == newsMap) {
            context.put("news", new HashMap<>());

            ((Map<String, Object>) context.get("news")).put("articles", new ArrayList<>());
        } else {
            List articleList = (List) newsMap.get("articles");
            if (null == articleList) {
                newsMap.put("articles", new ArrayList<>());
            }
        }
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(context);
    }
}
