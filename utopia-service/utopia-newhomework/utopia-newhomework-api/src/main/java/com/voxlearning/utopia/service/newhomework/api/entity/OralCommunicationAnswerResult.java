package com.voxlearning.utopia.service.newhomework.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/11/24
 * \* Time: 3:51 PM
 * \* Description:
 * \
 */
@Getter
@Setter
public class OralCommunicationAnswerResult implements Serializable {
    private static final long serialVersionUID = 2711092383551078016L;

    @JsonProperty("stone_id")
    private String stoneId;         //内容库stoneId

    @JsonProperty("stone_type")
    private OralCommunicationContentType stoneType;       //情景包类型 ： INTERACTIVE_CONVERSATION(互动会话）/INTERACTIVE_PICTURE_BOOK(互动绘本)/INTERACTIVE_VIDEO(互动视频)

    @JsonProperty("thumb_url")
    private String thumbUrl;        //情景包缩略图

    @JsonProperty("topic_name")
    private String topicName;       //情景包名称

    @JsonProperty("topic_desc")
    private String topicDesc;       //情景包简介

    @JsonProperty("video_seconds")
    private int videoSeconds;       //视频时长

    @JsonProperty("video_url")
    private String videoUrl;       //url 视频地址

    @JsonProperty("key_words")
    private List<Map<String,Object>> keyWords;       //重点单词

    @JsonProperty("key_sentences")
    private List<Map<String,Object>> keySentences;       //重点句型

    @JsonProperty("first_dialog")
    private List<Map<String,Object>> firstDialog; // 人机互动-- 第一阶段会话

    @JsonProperty("roles")
    private List<Map<String,Object>> roles;    // 人机互动-- 用户选择的角色

    @JsonProperty("topics")
    private List<Map<String,Object>> topics;       // 人机互动--topic

    @JsonProperty("video_contents")
    private List<Map<String,Object>> videoContents;       //互动视频 -- 内容

    @JsonProperty("screen_mode")
    private String screenMode;      // 互动绘本 --  屏幕：‘landscape’横屏 | 'portait' 竖屏

    @JsonProperty("pages")
    private List<Map<String,Object>> pages; //互动绘本 -- 内容
}
