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
 * \* Date: 2018/11/23
 * \* Time: 11:43 AM
 * \* Description: 口语交际题目详情
 * \
 */
@Getter
@Setter
public class OralCommunicationQuestionResult implements Serializable {

    private static final long serialVersionUID = -825606334032700023L;

    @JsonProperty("stone_id")
    private String stoneId;         //内容库stoneId

    @JsonProperty("stone_type")
    private OralCommunicationContentType stoneType;       //情景包类型 ： INTERACTIVE_CONVERSATION(互动会话）/INTERACTIVE_PICTURE_BOOK(互动绘本)/INTERACTIVE_VIDEO(互动视频)

    @JsonProperty("thumb_url")
    private String thumbUrl;        //情景包封面图（刚开始放的是缩略图，后来让改成封面图，为了客户端不用再改动，只是后端改了取值，其实这里的值实际上取的是封面图）

    @JsonProperty("topic_trans")
    private String topicTrans;      //话题名称翻译

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

    @JsonProperty("question_content")
    private Map<String,Object> questionContent;  //题干详情

}
