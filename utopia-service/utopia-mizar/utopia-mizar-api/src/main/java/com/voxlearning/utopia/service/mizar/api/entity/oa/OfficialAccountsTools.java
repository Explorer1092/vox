package com.voxlearning.utopia.service.mizar.api.entity.oa;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer Yang on 2016/8/2.
 */
@DocumentTable(table = "VOX_OFFICIAL_ACCOUNTS_TOOLS")
@NoArgsConstructor
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160802")
@DocumentConnection(configName = "hs_misc")
public class OfficialAccountsTools extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 4430891325640551593L;

    @UtopiaSqlColumn @Getter @Setter private Long accountId;
    @UtopiaSqlColumn @Getter @Setter private String toolUrl;
    @UtopiaSqlColumn @Getter @Setter private String toolName;
    @UtopiaSqlColumn @Getter @Setter private Boolean bindSid;  // 是否拼接SID
    @UtopiaSqlColumn @Getter @Setter private Boolean disabled;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(OfficialAccountsTools.class, id);
    }

    public static String ck_AccountId(Long accountId) {
        return CacheKeyGenerator.generateCacheKey(OfficialAccountsTools.class, "accountId", accountId);
    }
}
