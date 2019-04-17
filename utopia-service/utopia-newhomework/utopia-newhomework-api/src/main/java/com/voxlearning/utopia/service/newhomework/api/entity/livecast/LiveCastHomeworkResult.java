package com.voxlearning.utopia.service.newhomework.api.entity.livecast;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Getter
@Setter
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-livecast-homework")
@DocumentCollection(collection = "livecast_homework_result_{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'homeworkId':1}", background = true),
        @DocumentIndex(def = "{'userId':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'createAt':-1}", background = true),
        @DocumentIndex(def = "{'createAt':-1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180319")
public class LiveCastHomeworkResult extends BaseHomeworkResult implements Serializable {

    private static final long serialVersionUID = 6949371980854432165L;

    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;                                                          // 作业生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                                                          // 作业更新时间
    private Boolean finishCorrect;                                                  // 全部完成批改
    private Boolean repair;                                                         // 是否补做完成（true/false） 慎用，16年9月1日之前数据为空
    private Date correctAt;                                                       // 完成批改的时候

//    private String comment;                                                         //评语

    @JsonIgnore
    public boolean isCorrected() {
        return finishCorrect != null && finishCorrect;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"month", "subject", "hid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -8584124834740846242L;

        private String month;
        private Subject subject;
        private String hid;
        private Long userId;

        @Override
        public String toString() {
            return month + "-" + subject + "-" + hid + "-" + userId;
        }
    }

    public LiveCastHomeworkResult.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "-");
        if (segments.length != 4) return null;
        String day = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String hid = segments[2];
        Long uid = SafeConverter.toLong(segments[3]);
        return new LiveCastHomeworkResult.ID(day, subject, hid, uid);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {

        private static final long serialVersionUID = -8775767643149611396L;

        private String id;
        private String homeworkId;
        private Subject subject;        // 学科
        private String actionId;        // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
        private Long clazzGroupId;      // 班组id，有问题问长远
        private Long userId;            // 用户id，根据大作业的趋势，以后做题的会变成各种角色
        private Date createAt;          // 作业生成时间
        private Date updateAt;          // 作业更新时间
        private Date finishAt;
    }

    /**
     * 只有在作业完成的情况下返回数据，不然返回是NUll，四舍五入取整
     * 当有未批改的时候返回-1
     */
    public Integer liveCastProcessScore() {
        if (!isFinished())
            return null;
        Integer score;
        Integer totalScore = 0;
        int scoreCount = 0;
        int needScoreCount = 0;
        if (practices != null && !practices.isEmpty()) {
            for (ObjectiveConfigType oct : practices.keySet()) {
                //不需要显示分数的不显示
                if (NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(oct)) {
                    continue;
                }
                needScoreCount++;
                NewHomeworkResultAnswer na = practices.get(oct);
                if (na != null && na.processScore(oct) != null) {
                    totalScore += na.processScore(oct);
                    scoreCount++;
                }
            }
        } else {
            return null;
        }

        if (needScoreCount == 0) {
            return -2;//-2表示都是没有分数的类型
        }

        //当有未批改的时候返回-1
        if (scoreCount < needScoreCount) {
            return -1;
        }
        score = new BigDecimal(totalScore).divide(new BigDecimal(needScoreCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
        return score;
    }


    public LiveCastHomeworkResult.Location toLocation() {
        LiveCastHomeworkResult.Location location = new LiveCastHomeworkResult.Location();
        location.id = getId();
        location.homeworkId = homeworkId;
        location.subject = subject;
        location.actionId = actionId;
        location.clazzGroupId = clazzGroupId;
        location.userId = userId;
        location.createAt = createAt;
        location.updateAt = updateAt;
        location.finishAt = finishAt;
        return location;
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(LiveCastHomeworkResult.class, id);
    }
}
