package com.voxlearning.utopia.service.mizar.api.entity.microcourse;

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
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.mizar.api.mapper.talkfun.TK_CourseData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

import static com.voxlearning.utopia.service.mizar.api.entity.microcourse.TalkFunCourse.Status.FINISHED;
import static com.voxlearning.utopia.service.mizar.api.entity.microcourse.TalkFunCourse.Status.REPLAY_DONE;

/**
 * 微课堂-欢拓后台对应实体
 * Created by Wang Yuechen on 2016/01/11.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "talk_fun_course")
@UtopiaCacheRevision("20170315")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class TalkFunCourse implements CacheDimensionDocument {

    private static final long serialVersionUID = 1L;

    // 以课时ID为主键
    @DocumentId private String periodId;        // 课时ID
    private String partnerId;       // 合作方ID
    private String bid;             // 主播ID
    private String periodName;      // 课程名称
    private String zhuboKey;        // 主播登录秘钥
    private String adminKey;        // 助教登录秘钥
    private String userKey;         // 学生登录秘钥
    private Date addTime;           // 课程创建时间
    private Date startTime;         // 开始时间
    private Date endTime;           // 结束时间
    private String courseId;        // 欢拓课程ID
    private String courseStatus;    // 欢拓课程状态
    private Boolean isManual;       // 是否手动

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;


    @JsonIgnore
    public boolean isReplayDone() {
        return REPLAY_DONE.name().equals(courseStatus);
    }

    @JsonIgnore
    public boolean isCourseFinished() {
        return FINISHED.name().equals(courseStatus);
    }

    @JsonIgnore
    public boolean isManually() {
        return Boolean.TRUE.equals(isManual);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(periodId)
        };
    }

    public static TalkFunCourse newInstance(TK_CourseData data) {
        TalkFunCourse course = new TalkFunCourse();
        if (data != null) {
            course.setPartnerId(data.getPartnerId());
            course.setBid(data.getBid());
            course.setCourseId(data.getCourseId());
            course.setPeriodName(data.getCourseName());
            course.setZhuboKey(data.getZhuboKey());
            course.setAdminKey(data.getAdminKey());
            course.setUserKey(data.getUserKey());
            course.setStartTime(data.getStartTime());
            course.setEndTime(data.getEndTime());
            course.setAddTime(data.getAddTime());
        }
        return course;
    }

    public enum Status {
        FINISHED,          // 已结束
        REPLAY_DONE        // 回放已生成
    }

}
