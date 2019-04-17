package com.voxlearning.utopia.service.newhomework.api.entity.shard;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkBook;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_book")
@UtopiaCacheExpiration(172800)
@UtopiaCacheRevision("20180821")
public class ShardHomeworkBook extends BaseHomeworkBook implements Serializable {
    private static final long serialVersionUID = -8098200503814931688L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ShardHomeworkBook.class, id);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -8266798792332369425L;

        private String month;
        private String randomId = RandomUtils.nextObjectId();
        private String version = "2";

        public ID(String month) {
            this.month = month;
        }

        @Override
        public String toString() {
            return month + "_" + randomId + "_" + version;
        }
    }

    public ShardHomeworkBook.ID parseId() {
        if (id == null || id.trim().length() == 0) {
            return null;
        }
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 3) {
            return null;
        }
        String month = segments[0];
        String randomId = segments[1];
        String version = segments[2];
        return new ShardHomeworkBook.ID(month, randomId, version);
    }
}
