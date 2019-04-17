package com.voxlearning.utopia.service.newhomework.api.entity.livecast;

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
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomework;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-livecast-homework")
@DocumentCollection(collection = "livecast_homework")
@DocumentIndexes({
        @DocumentIndex(def = "{'teacherId':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'createAt':-1}", background = true),
        @DocumentIndex(def = "{'createAt':-1}", background = true),
        @DocumentIndex(def = "{'disabled':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161216")
public class LiveCastHomework extends BaseHomework implements Serializable {

    private static final long serialVersionUID = 235940810664288567L;

    @DocumentId
    private String id;

    @DocumentCreateTimestamp
    private Date createAt;                                                  // 作业生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                                                  // 作业更新时间


    public LiveCastHomework.Location toLocation() {
        LiveCastHomework.Location location = new LiveCastHomework.Location();
        location.id = id;
        location.type = type;
        location.teacherId = (teacherId == null ? 0 : teacherId);
        location.clazzGroupId = (clazzGroupId == null ? 0 : clazzGroupId);
        location.createTime = (createAt == null ? 0 : createAt.getTime());
        location.startTime = (startTime == null ? 0 : startTime.getTime());
        location.endTime = (endTime == null ? 0 : endTime.getTime());
        location.actionId = actionId;
        location.subject = subject;
        location.includeSubjective = Boolean.TRUE.equals(includeSubjective);
        return location;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {

        private static final long serialVersionUID = 2504187101885956734L;
        private String id;
        private NewHomeworkType type;
        private long teacherId;
        private long clazzGroupId;
        private long createTime;
        private long startTime;
        private long endTime;
        private String actionId;
        private Subject subject;
        private boolean includeSubjective;
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(LiveCastHomework.class, id);
    }

    public static String ck_clazzGroupId(Long clazzGroupId) {
        return CacheKeyGenerator.generateCacheKey(LiveCastHomework.class,
                new String[]{"CG"},
                new Object[]{clazzGroupId});
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = 4608790383933975513L;
        private String month;
        private String randomId = RandomUtils.nextObjectId();

        public ID(String month) {
            this.month = month;
        }

        @Override
        public String toString() {
            return month + "_" + randomId;
        }
    }

    public LiveCastHomework.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 2) return null;
        String month = segments[0];
        String randomId = segments[1];
        return new LiveCastHomework.ID(month, randomId);
    }
}
