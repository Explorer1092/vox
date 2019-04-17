package com.voxlearning.utopia.service.parent.homework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户活动信息
 *
 * @author Wenlong Meng
 * @since Feb 23, 2019
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-parent-homework")
@DocumentCollection(collection = "user_activity")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 24*60*60)
@UtopiaCacheRevision("20190303")
public class UserActivity implements Serializable {

    @DocumentId
    private String id;
    private String activityId; //活动id
    private Long studentId; //学生id
    private Long parentId;//家长id
    private Integer status;//状态
    private Boolean finished;//已完成
    private Date createTime;//创建时间
    private Date updateTime;//更新时间
    private Map<String, Object> extInfo;//扩展信息

    /**
     * cache key
     *
     * @return
     */
    public String ckId(){
        return CacheKeyGenerator.generateCacheKey(Activity.class,null, id);
    }


}
