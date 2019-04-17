/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.business.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.utopia.business.api.mapper.TtsListeningQuestion;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSentence;
import com.voxlearning.utopia.business.api.mapper.TtsListeningTag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * TTS听力试卷
 *
 * @author Junjie Zhang
 * @since 2014-08-12
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "WashingtonDatabase")
@DocumentCollection(collection = "ttsListeningPaper")
@DocumentIndexes({
        @DocumentIndex(def = "{'share':1,'bookId':1,'updateDatetime':-1}", background = true),
        @DocumentIndex(def = "{'author':1,'updateDatetime':-1}", background = true),
        @DocumentIndex(def = "{'share':1,'classLevel':1,'bookId':1,'updateDatetime':-1}", background = true),
        @DocumentIndex(def = "{'share':1,'updateDatetime':1}", background = true)
})
public class TtsListeningPaper implements Serializable {
    private static final long serialVersionUID = -5077633456615561690L;

    @DocumentId private String id;
    private String title;                                   //听力标题
    private Long author;                                    //作者
    private String authorName;                              //作者名称
    private String beginningVoice;                          //考前播放音地址
    private TtsListeningSentence beginningSentence;         //制作考前播放音数据
    private String endingVoice;                             //考后播放音地址
    private TtsListeningSentence endingSentence;            //制作考后播放音数据
    private String intervalVoice;                           //间隔音地址
    private List<TtsListeningQuestion> questions;           //听力大题
    private Float duration;                                 //听力时长
    private Integer userType;                               //用户类型，1老师，2教研员
    private Integer share;                                  //是否分享  0不分享，1分享
    private Long bookId;                                    //教材
    private String bookName;                                //教材名字
    private Integer classLevel;                             //年级， 使用整型，json处理方便
    private Integer format;                                 //1为新格式flash，其他为旧格式html
    private String richText;                                //富文本内容
    private List<TtsListeningTag> tagList;                  //富文本对应标签
    @DocumentCreateTimestamp private Date createDatetime;
    @DocumentUpdateTimestamp private Date updateDatetime;

    public String fetchDurationString() {
        if (duration == null)
            return "暂无";
        if (duration >= 60)
            return String.format("%.0f分%.0f秒", duration / 60, duration % 60);
        return String.format("%.0f秒", duration);
    }

    public String fetchClassLevelString() {
        if (classLevel == null || classLevel <= 0)
            return "";
        ClazzLevel cl = ClazzLevel.parse(classLevel);
        if (cl == null)
            return "";
        return cl.getDescription();
    }

    @JsonIgnore
    public String getRichTextNewFormat() {
        if (richText == null)
            return null;
        richText = richText.replaceAll("(SOUND[^\"]*)\" height=\"([^\"]*)\" width=\"([^\"]*)\"", "$1\" height=\"13\" width=\"7\"");
        richText = richText.replaceAll("(MUSIC[^\"]*)\" height=\"([^\"]*)\" width=\"([^\"]*)\"", "$1\" height=\"13\" width=\"14\"");
        richText = richText.replaceAll("(VOICE[^\"]*)\" height=\"([^\"]*)\" width=\"([^\"]*)\"", "$1\" height=\"14\" width=\"12\"");
        richText = richText.replaceAll("(LOOP[^\"]*)\" typeName=\"([^\"]*)\" height=\"([^\"]*)\" width=\"([^\"]*)\"", "$1\" typeName=\"$2\" height=\"16\" width=\"24\"");
        richText = richText.replaceAll("(PAUSE[^\"]*)\" height=\"([^\"]*)\" width=\"([^\"]*)\"", "$1\" height=\"12\" width=\"24\"");
        richText = richText.replaceAll("(SPEED[^\"]*)\" typeName=\"([^\"]*)\" height=\"([^\"]*)\" width=\"([^\"]*)\"", "$1\" typeName=\"$2\" height=\"12\" width=\"24\"");
        richText = richText.replaceAll("(VOLUME[^\"]*)\" typeName=\"([^\"]*)\" height=\"([^\"]*)\" width=\"([^\"]*)\"", "$1\" typeName=\"$2\" height=\"13\" width=\"24\"");
        richText = richText.replaceAll("(ROLE[^\"]*)\" typeName=\"([^\"]*)\" height=\"([^\"]*)\" width=\"([^\"]*)\"", "$1\" typeName=\"$2\" height=\"19\" width=\"36\"");
        return richText;
    }
}
