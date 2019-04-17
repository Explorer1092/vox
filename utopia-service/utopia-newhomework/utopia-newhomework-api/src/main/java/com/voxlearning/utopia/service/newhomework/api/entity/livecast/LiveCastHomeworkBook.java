package com.voxlearning.utopia.service.newhomework.api.entity.livecast;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkBook;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@DocumentCollection(collection = "livecast_homework_book")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161216")
public class LiveCastHomeworkBook extends BaseHomeworkBook implements Serializable {

    private static final long serialVersionUID = -5789556200189769122L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                                                      // 此主键与NewHomework的主键一致
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;


    public LiveCastHomeworkBook.Location toLocation() {
        LiveCastHomeworkBook.Location location = new LiveCastHomeworkBook.Location();
        location.id = id;
        location.teacherId = (teacherId == null ? 0 : teacherId);
        location.clazzGroupId = (clazzGroupId == null ? 0 : clazzGroupId);
        location.actionId = actionId;
        location.subject = subject;
        return location;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {
        private static final long serialVersionUID = -6862857904720409006L;
        private String id;
        private long teacherId;
        private long clazzGroupId;
        private String actionId;
        private Subject subject;
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(LiveCastHomeworkBook.class, id);
    }
}
