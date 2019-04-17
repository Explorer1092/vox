package com.voxlearning.utopia.service.parent.homework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户作业关系表
 * @author chongfeng.qi
 * @date 2018-11-07
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-parent-homework")
@DocumentCollection(collection = "homework_user_ref")
@DocumentIndexes({
        @DocumentIndex(def = "{'homeworkId':1}", background = true),
        @DocumentIndex(def = "{'userId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181111")
public class HomeworkUserRef implements Serializable {

    private static final long serialVersionUID = 17L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    private String homeworkId; //作业id

    private Long userId; //学生id

    @DocumentCreateTimestamp
    private Date createTime; //创建日期，格式为yyyy-MM-dd

    /**
     * userId cache key
     *
     * @return
     */
    public String ckUserId(){
        return CacheKeyGenerator.generateCacheKey(HomeworkUserRef.class,"userId",userId);
    }

}
