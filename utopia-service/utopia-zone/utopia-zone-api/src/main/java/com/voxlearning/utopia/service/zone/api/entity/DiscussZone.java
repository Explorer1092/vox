package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.zone.data.DiscussResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 讨论区配置
 * @author chensn
 * @date 2018-10-23 14:27
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_discuss")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20181030")
public class DiscussZone implements Serializable {
    private static final long serialVersionUID = -3369267588082396341L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private Integer id;
    /**
     * 1.童话 2.配音 3绘本
     */
    private Integer type;
    /**
     * 内容ids 童话列表 配音列表，绘本列表等  “，”分割
     */
    private List<String> ids;
    @DocumentFieldIgnore
    private List<DiscussResult> detail;
    /**
     * 文案
     */
    private String content;
    /**
     * 内容生效时间
     */
    private Date startDate;
    /**
     * 内容生效时间
     */
    private Date endDate;
    /**
     * 是否展示
     */
    private Boolean isShow;
    /**
     * 用户信息
     * key: pic,name
     */
    private List<Map<String, String>> user;

    public String ck_used_discuss() {
        return CacheKeyGenerator.generateCacheKey(DiscussZone.class, "findUsedDiscuss");
    }

}
