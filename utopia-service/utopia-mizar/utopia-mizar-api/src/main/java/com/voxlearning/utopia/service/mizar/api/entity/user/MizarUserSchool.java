package com.voxlearning.utopia.service.mizar.api.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用户学校关联
 *
 * @author chunlin.yu
 * 2017-06-22 10:31
 **/

@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_user_school")
@DocumentIndexes({
        @DocumentIndex(def = "{'userId':1}", background = true)
})
@UtopiaCacheRevision("20170627")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarUserSchool implements Serializable {
    private static final long serialVersionUID = 2637822121418870026L;

    @DocumentId
    private String id;

    //用户登录ID
    private String userId;
    //学校ID
    private Long schoolId;


    private Integer contractStartMonth; // 合同开始月份 201705
    private Integer contractEndMonth;   // 合同结束月份 201712
    //是否有效
    private Boolean disabled;

    @DocumentCreateTimestamp
    private Long createTime;
    @DocumentUpdateTimestamp
    private Long updateTime;


    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    public static String ck_user(String userId) {
        return CacheKeyGenerator.generateCacheKey(MizarUserSchool.class, "uid", userId);
    }

    public static String ck_school(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(MizarUserSchool.class, "sid", schoolId);
    }
    public MizarUserSchool(String userId, Long schoolId) {
        this.userId = userId;
        this.schoolId = schoolId;
        this.disabled = false;
    }
}
