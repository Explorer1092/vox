package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/2/7 16:08
 */

@Getter
@Setter
@DocumentConnection(configName = "mongo-mexico")
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_syllable_result_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170207")
public class NewHomeworkSyllable implements Serializable {
    private static final long serialVersionUID = -5217820489513083226L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    private Long userId;    //学生ID
    private String homeworkId;  //作业ID
    private String objectiveConfigType; //作业类型
    private String bookId;  //课本ID
    private String unitId;  //单元ID
    private String lessonId;    //课ID
    private String practiceId;  //应用ID
    private String sentenceId;  //句子ID
    private String questionId;  //题ID
    private String clientType;  //客户端类型:pc,mobile
    private String clientName;  //客户端名称:***app
    private String ipImei;  //ip or imei
    private String audio;   //音频地址
    private List<Sentence> lines;   //语音引擎打分详情

    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(NewHomeworkSyllable.class, id);
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -3233142723031771460L;

        private String day;
        private Long userId;
        private String hid;
        private String audio;

        @Override
        public String toString() {
            return day + "|" + userId + "|" + hid + "|" + audio;
        }
    }

    public NewHomeworkSyllable.ID parseID() {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        String[] segments = StringUtils.split(id, "|");
        if (segments.length != 4) {
            return null;
        }
        String day = SafeConverter.toString(segments[0]);
        Long userId = SafeConverter.toLong(segments[1]);
        String hid = SafeConverter.toString(segments[2]);
        String audio = SafeConverter.toString(segments[3]);
        return new NewHomeworkSyllable.ID(day, userId, hid, audio);
    }

    @Getter
    @Setter
    @ToString
    public static class Sentence implements Serializable {
        private static final long serialVersionUID = 7407551704246680152L;
        private String sample;
        private String usertext;
        private Float begin;
        private Float end;
        private Double score;
        private Float fluency;
        private Float integrity;
        private Float pronunciation;
        private List<Word> words;
    }

    @Getter
    @Setter
    @ToString
    public static class Word implements Serializable {
        private static final long serialVersionUID = 587161622982278897L;
        private String text;
        private Integer type;
        private Float score;
        private List<Map<String, Object>> subwords;
    }
}
