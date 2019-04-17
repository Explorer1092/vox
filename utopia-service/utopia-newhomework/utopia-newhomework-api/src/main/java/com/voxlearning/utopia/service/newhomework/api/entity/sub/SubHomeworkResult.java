package com.voxlearning.utopia.service.newhomework.api.entity.sub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultLight;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2017/1/11
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_result_{}", dynamic = true)
@UtopiaCacheExpiration(259200)
@UtopiaCacheRevision("20190125")
public class SubHomeworkResult extends BaseHomeworkResultLight implements Serializable {

    private static final long serialVersionUID = 1423316551559782158L;

    @DocumentId
    private String id;

    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    private Boolean finishCorrect;
    private Boolean repair;
    private Boolean urge;//家长通报告：家长是否确定催促
    private Integer beanNum;//家长通报告：家长确认催促得到奖励学豆数

    @JsonIgnore
    public boolean isCorrected() {
        return finishCorrect != null && finishCorrect;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"day", "subject", "hid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -7858910543012743779L;
        private String day;
        private Subject subject;
        private String hid;
        private String userId;

        @Override
        public String toString() {
            return day + "-" + subject + "-" + hid + "-" + userId;
        }
    }

    public SubHomeworkResult.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "-");
        if (segments.length != 4) return null;
        String day = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String hid = segments[2];
        String uid = segments[3];
        return new SubHomeworkResult.ID(day, subject, hid, uid);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {

        private static final long serialVersionUID = -5899989111549885513L;
        private String id;
        private String homeworkId;
        private Subject subject;                                                        // 学科
        private String actionId;                                                        // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
        private Long clazzGroupId;                                                      // 班组id，有问题问长远
        private Long userId;                                                            // 用户id，根据大作业的趋势，以后做题的会变成各种角色
        private Date createAt;                                                          // 作业生成时间
        private Date updateAt;                                                          // 作业更新时间
        private Date finishAt;
    }

    public SubHomeworkResult.Location toLocation() {
        SubHomeworkResult.Location location = new SubHomeworkResult.Location();
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
        return CacheKeyGenerator.generateCacheKey(SubHomeworkResult.class, id);
    }

    public static String ck_homework(String homeworkId) {
        return CacheKeyGenerator.generateCacheKey(SubHomeworkResult.class,
                new String[]{"HID"},
                new Object[]{homeworkId});
    }
}
