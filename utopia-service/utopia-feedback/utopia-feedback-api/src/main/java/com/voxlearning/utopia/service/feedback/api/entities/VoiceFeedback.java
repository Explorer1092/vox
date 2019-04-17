package com.voxlearning.utopia.service.feedback.api.entities;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/9/17
 * \* Time: 下午12:16
 * \* Description: 老师端作业报告增加语音题目反馈
 * \
 */
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_VOICE_FEEDBACK")
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class VoiceFeedback implements Serializable, PrimaryKeyAccessor<Long>, TimestampTouchable {

    @UtopiaSqlColumn(primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    private Long id;
    @DocumentCreateTimestamp
    @UtopiaSqlColumn
    private Date createDatetime;
    @UtopiaSqlColumn
    private String subject;//学科
    @UtopiaSqlColumn
    private Long teacherId; //老师id
    @UtopiaSqlColumn
    private String homeworkId; //作业id
    @UtopiaSqlColumn
    private String questionId; //题目id
    @UtopiaSqlColumn
    private String voiceUrl; //学生语音记录 url
    @UtopiaSqlColumn
    private String voiceText; //学生语音记录 text
    @UtopiaSqlColumn
    private Integer type;           //问题类型
    @UtopiaSqlColumn
    private String content;  //问题描述
    @UtopiaSqlColumn
    private String engineName;  //引擎名称
    @UtopiaSqlColumn
    private Double engineScore;  //引擎评分
    @UtopiaSqlColumn
    private String objectiveConfigType;  //作业类型
    @UtopiaSqlColumn
    private Integer categoryId; //类型id
    @DocumentUpdateTimestamp
    @UtopiaSqlColumn
    private Date updateDatetime;   //更新时间

    @Override
    public void touchCreateTime(long timestamp) {
        if (getCreateDatetime() == null) {
            setCreateDatetime(new Date(timestamp));
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        updateDatetime = new Date(timestamp);
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(VoiceFeedback.class, id);
    }

    public static final Map<Integer, String> voiceFeedbackTypeMap;

    static {
        voiceFeedbackTypeMap = new LinkedHashMap<>();
        voiceFeedbackTypeMap.put(1, "打分略高");
        voiceFeedbackTypeMap.put(2, "打分明显偏高");
        voiceFeedbackTypeMap.put(3, "打分略低");
        voiceFeedbackTypeMap.put(4, "打分明显偏低");
        voiceFeedbackTypeMap.put(5, "评分错误");//只有语文有这个问题
        voiceFeedbackTypeMap.put(6, "其他");
    }

    public static String fetchVoiceFeedbackType(Integer typeValue) {
        return voiceFeedbackTypeMap.get(typeValue);
    }
}
