package com.voxlearning.utopia.service.newhomework.api.entity.sub;


import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkBook;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 教材维度的试题信息存储，可用于报告等
 * 这个实体的主键与NewHomework实体主键一致
 *
 * @author xuesong.zhang
 * @since 2017/1/13
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_book")
@DocumentIndexes({
        @DocumentIndex(def = "{'clazzGroupId':1}", background = true),
        @DocumentIndex(def = "{'createAt':-1}", background = true)
})
@UtopiaCacheExpiration(172800)
@UtopiaCacheRevision("20190123")
public class SubHomeworkBook extends BaseHomeworkBook implements Serializable {

    private static final long serialVersionUID = -8783768460227456077L;


    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SubHomeworkBook.class, id);
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

    public SubHomeworkBook.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 3) return null;
        String month = segments[0];
        String randomId = segments[1];
        String version = segments[2];
        return new SubHomeworkBook.ID(month, randomId, version);
    }
}
