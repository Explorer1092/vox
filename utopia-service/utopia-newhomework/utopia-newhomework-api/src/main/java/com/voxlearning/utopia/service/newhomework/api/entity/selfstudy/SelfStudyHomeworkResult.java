package com.voxlearning.utopia.service.newhomework.api.entity.selfstudy;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultLight;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "self_study_homework_result")
@UtopiaCacheExpiration(172800)
@UtopiaCacheRevision("20180618")
public class SelfStudyHomeworkResult extends BaseHomeworkResultLight implements Serializable {

    private static final long serialVersionUID = 1515310430856056068L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;              // 这个id和相应的作业id相同
    private NewHomeworkType type;
    private HomeworkTag homeworkTag;

    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    @Getter
    @Setter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -7860980125622674083L;

        private String month;
        private String randomId;
        private Long studentId;

        @Override
        public String toString() {
            return month + "_" + randomId + "_" + studentId;
        }
    }

    public SelfStudyHomeworkResult.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 3) return null;
        String month = segments[0];
        String randomId = segments[1];
        Long studentId = SafeConverter.toLong(segments[2]);
        return new SelfStudyHomeworkResult.ID(month, randomId, studentId);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SelfStudyHomeworkResult.class, id);
    }

    public Integer processScore() {
        if (!isFinished())
            return null;
        Integer score = null;
        Integer totalScore = 0;
        int scoreCount = 0;
        if (practices != null && !practices.isEmpty()) {
            for (ObjectiveConfigType oct : practices.keySet()) {
                BaseHomeworkResultAnswer na = practices.get(oct);
                if (na != null && na.processScore() != null) {
                    scoreCount++;
                    totalScore += na.processScore();
                }
            }
        }
        if (scoreCount > 0) {
            score = new BigDecimal(totalScore).divide(new BigDecimal(scoreCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        return score;
    }

    public Long processDuration() {
        if (!isFinished())
            return null;
        Long duration = 0L;
        if (practices != null && !practices.isEmpty()) {
            for (ObjectiveConfigType oct : practices.keySet()) {
                BaseHomeworkResultAnswer na = practices.get(oct);
                duration += na.getDuration();
            }
        }
        return duration;
    }

    public LinkedHashMap<String, BaseHomeworkResultAppAnswer> findAppAnswer(Collection<ObjectiveConfigType> objectiveConfigTypes) {
        LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswerMap = new LinkedHashMap<>();
        if (MapUtils.isEmpty(practices)) {
            return appAnswerMap;
        }
        for (ObjectiveConfigType objectiveConfigType : objectiveConfigTypes) {
            appAnswerMap.putAll(findAppAnswer(objectiveConfigType));
        }
        return appAnswerMap;
    }

    public LinkedHashMap<String, BaseHomeworkResultAppAnswer> findAppAnswer(ObjectiveConfigType objectiveConfigType) {
        if (MapUtils.isNotEmpty(practices)) {
            BaseHomeworkResultAnswer baseHomeworkResultAnswer = practices.get(objectiveConfigType);
            if (baseHomeworkResultAnswer != null) {
                return baseHomeworkResultAnswer.getAppAnswers();
            }
        }
        return new LinkedHashMap<>();
    }
}
