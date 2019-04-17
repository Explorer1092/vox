package com.voxlearning.wechat.builder;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.context.ReplyMessageContext;
import com.voxlearning.wechat.support.Article;
import com.voxlearning.wechat.support.Articles;
import com.voxlearning.wechat.support.utils.MessageParser;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;

/**
 * 被动回复消息builder
 * @author Xin Xin
 * @since 10/19/15
 */
@Slf4j
public class ReplyMessageBuilder {

    private ReplyMessageContext context;

    public ReplyMessageBuilder() {
        this.context = new ReplyMessageContext();
        buildCreateTime((long) Instant.now().getNano());
    }

    public ReplyMessageBuilder(MessageContext context) {
        this.context = new ReplyMessageContext();

        buildFromUserName(context.getToUserName());
        buildToUserName(context.getFromUserName());
        buildCreateTime((long) Instant.now().getNano());
    }

    public ReplyMessageBuilder buildContent(String content) {
        context.setContent(content);
        return this;
    }

    public ReplyMessageBuilder buildToUserName(String toUser) {
        context.setToUserName(toUser);
        return this;
    }

    public ReplyMessageBuilder buildFromUserName(String fromUser) {
        context.setFromUserName(fromUser);
        return this;
    }

    public ReplyMessageBuilder buildCreateTime(Long createTime) {
        context.setCreateTime(createTime);
        return this;
    }

    public ReplyMessageBuilder buildMsgType(MessageType type) {
        context.setMsgType(type);
        return this;
    }

    public ReplyMessageBuilder buildImageMediaId(String mediaId) {
        context.setImage_MediaId(mediaId);
        return this;
    }

    public ReplyMessageBuilder buildVoiceMediaId(String mediaId) {
        context.setVoice_Media_id(mediaId);
        return this;
    }

    public ReplyMessageBuilder buildVideoMediaId(String mediaId) {
        context.setVideo_MediaId(mediaId);
        return this;
    }

    public ReplyMessageBuilder buildVideoTitle(String title) {
        context.setVideo_Title(title);
        return this;
    }

    public ReplyMessageBuilder buildVideoDescription(String description) {
        context.setVideo_Description(description);
        return this;
    }

    public ReplyMessageBuilder buildVideoMusicTitle(String title) {
        context.setMusic_Title(title);
        return this;
    }

    public ReplyMessageBuilder buildMusicDescription(String description) {
        context.setMusic_Description(description);
        return this;
    }

    public ReplyMessageBuilder buildMusicMusicUrl(String musicUrl) {
        context.setMusic_MusicUrl(musicUrl);
        return this;
    }

    public ReplyMessageBuilder bulidMusicHQMusicUrl(String hqMusicUrl) {
        context.setMusic_HQMusicUrl(hqMusicUrl);
        return this;
    }

    public ReplyMessageBuilder buildMusicThumbMediaId(String thumbMediaId) {
        context.setMusic_ThumbMediaId(thumbMediaId);
        return this;
    }

    public ReplyMessageBuilder buildArticle(String title, String description, String picUrl, String url) {
        if (null == context.getArticles()) {
            context.setArticles(new Articles());
        }

        Article article = new Article();
        article.setTitle(title);
        article.setDescription(description);
        article.setPicUrl(picUrl);
        article.setUrl(url);

        context.getArticles().getArticles().add(article);
        context.setArticleCount(context.getArticles().getArticles().size());
        return this;
    }

    @Override
    public String toString() {
        try {
            return MessageParser.parse(context);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("build reply msg error,msg:{}", JsonUtils.toJson(context));
            return "success";
        }
    }
}
