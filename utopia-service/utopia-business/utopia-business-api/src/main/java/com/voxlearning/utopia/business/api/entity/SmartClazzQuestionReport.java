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


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.DateFormatUtils;
import com.voxlearning.utopia.business.api.mapper.SmartClazzStudentResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * TODO: 新班级体系需要处理数据
 * TODO: 这是谁加的注释？
 *
 * @author Maofeng Lu
 * @since 14-10-24 下午3:19
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-smartclazz")
@DocumentCollection(collection = "smartClazzQuestionReport")
@DocumentIndexes({
        @DocumentIndex(def = "{'clazzId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'updateAt':1}", background = true)
})
public class SmartClazzQuestionReport implements Serializable {
    private static final long serialVersionUID = 253138368462085396L;

    @DocumentId private String id;
    private Long clazzId;
    private String clazzName;
    private Subject subject;
    private String questionId;
    private String answer;                          //题的正确答案
    private Integer studentAnswerCount;             //扫到的答案总数量
    private Integer correctAnswerCount;             //回答正确的数量
    private Integer answerCountA;                   //选A的个数
    private Integer answerCountB;                   //选B的个数
    private Integer answerCountC;                   //选C的个数
    private Integer answerCountD;                   //选D的个数
    private List<SmartClazzStudentResult> students; //学生答题信息
    private Long groupId;                           // 分组ID
    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public String fetchUpdateDateTimeStr() {
        if (updateAt == null)
            return "";
        //没法调用core的DateUtils
        return DateFormatUtils.format(updateAt, "yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);
    }

    public static String generateCacheKeyByClazzIdSubjectQuestionId(Long clazzId, Subject subject, String questionId) {
        return CacheKeyGenerator.generateCacheKey(SmartClazzQuestionReport.class,
                new String[]{"C", "S", "Q"},
                new Object[]{clazzId, subject, questionId});
    }
}
