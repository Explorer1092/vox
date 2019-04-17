package com.voxlearning.utopia.service.newhomework.api.entity.sub;

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
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkLocation;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2017/1/10
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "sub_homework")
@DocumentIndexes({
        @DocumentIndex(def = "{'type':1}", background = true),
        @DocumentIndex(def = "{'teacherId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'createAt':-1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'subject':1,'createAt':-1}", background = true),
        @DocumentIndex(def = "{'endTime':-1,'checked':1}", background = true),
        @DocumentIndex(def = "{'createAt':-1}", background = true),
        @DocumentIndex(def = "{'checked':1,'checkedAt':-1}", background = true)
})
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20180628")
public class SubHomework extends BaseHomeworkLocation implements Serializable {

    private static final long serialVersionUID = 1391367889704723184L;

    @DocumentId
    private String id;

    @DocumentCreateTimestamp
    private Date createAt;                                                  // 作业生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                                                  // 作业更新时间

    public SubHomework.Location toLocation() {
        SubHomework.Location location = new SubHomework.Location();
        location.id = id;
        location.type = type == null ? NewHomeworkType.Normal : type;
        location.homeworkTag = homeworkTag == null ? HomeworkTag.Normal : homeworkTag;
        location.teacherId = (teacherId == null ? 0 : teacherId);
        location.clazzGroupId = (clazzGroupId == null ? 0 : clazzGroupId);
        location.checked = Boolean.TRUE.equals(checked);
        location.createTime = (createAt == null ? 0 : createAt.getTime());
        location.checkedTime = (checkedAt == null ? 0 : checkedAt.getTime());
        location.startTime = (startTime == null ? 0 : startTime.getTime());
        location.endTime = (endTime == null ? 0 : endTime.getTime());
        location.actionId = actionId;
        location.subject = subject;
        location.includeSubjective = Boolean.TRUE.equals(includeSubjective);
        location.includeIntelligentTeaching = Boolean.TRUE.equals(includeIntelligentTeaching);
        location.remindCorrection = Boolean.TRUE.equals(remindCorrection);
        location.duration = (duration == null ? 0 : duration);
        location.disabled = (disabled == null ? false : disabled);
        return location;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {
        private static final long serialVersionUID = -6862857904720409006L;
        private String id;
        private NewHomeworkType type;
        private HomeworkTag homeworkTag;
        private long teacherId;
        private long clazzGroupId;
        private boolean checked;
        private long createTime;
        private long checkedTime;
        private long startTime;
        private long endTime;
        private String actionId;
        private Subject subject;
        private boolean includeSubjective;
        private boolean includeIntelligentTeaching;
        private boolean remindCorrection;
        private Long duration;
        private boolean disabled;
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SubHomework.class, id);
    }

    public static String ck_clazzGroupId(Long clazzGroupId) {
        return CacheKeyGenerator.generateCacheKey(SubHomework.class,
                new String[]{"CG"},
                new Object[]{clazzGroupId});
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -1851184704015821564L;

        private String month;
        private String randomId = RandomUtils.nextObjectId();
        private String version = "1";

        public ID(String month) {
            this.month = month;
        }

        @Override
        public String toString() {
            return month + "_" + randomId + "_" + version;
        }
    }

    public SubHomework.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 3) return null;
        String month = segments[0];
        String randomId = segments[1];
        String version = segments[2];
        return new SubHomework.ID(month, randomId, version);
    }

}
