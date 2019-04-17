package com.voxlearning.utopia.service.newhomework.api.entity.sub;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkPractice;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2017/1/10
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_practice")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20190123")
public class SubHomeworkPractice extends BaseHomeworkPractice implements Serializable {

    private static final long serialVersionUID = -341962470980649698L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SubHomeworkPractice.class, id);
    }

    public String parseIdMonth() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 3) return null;
        return segments[0];
    }
}
