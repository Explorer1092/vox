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
 * 用户公众号关联表
 * Created by xiang.lv on 2016/11/3.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_user_official_accounts")
@DocumentIndexes({
        @DocumentIndex(def = "{'userId':1}", background = true)
})
@UtopiaCacheRevision("20161104")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarUserOfficialAccounts implements Serializable {
    private static final long serialVersionUID = 2209172134431659375L;
    @DocumentId
    private String id;
    private String userId;                  // 用户登录ID
    private String accountsKey;              // 公众号唯一标识
    private Boolean disabled;               // 是否有效

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    public static String ck_user(String userId) {
        return CacheKeyGenerator.generateCacheKey(MizarUserOfficialAccounts.class, "uid", userId);
    }

    public static String ck_accounts_key(String accountsKey) {
        return CacheKeyGenerator.generateCacheKey(MizarUserOfficialAccounts.class, "accounts_key", accountsKey);
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    public MizarUserOfficialAccounts(String userId,String accountsKey ) {
        this.userId = userId;
        this.accountsKey = accountsKey;
        this.disabled = false;
    }

}
