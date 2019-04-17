package com.voxlearning.utopia.service.mizar.api.entity.oa;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Summer Yang on 2016/10/24.
 * 公众号用户关注关系
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_USER_OFFICIAL_ACCOUNTS_REF_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161024")
public class UserOfficialAccountsRef implements CacheDimensionDocument {
    private static final long serialVersionUID = 2408581265166494947L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    private Long id;
    @DocumentCreateTimestamp
    private Date createDatetime;
    @DocumentUpdateTimestamp
    private Date updateDatetime;
    private Long userId;                // 用户ID
    private Long officialAccountsId;    // 公众号ID
    private String accountsKey;         // 公众号唯一Key
    private Status status;              // 状态

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey("userId", userId)};
    }

    public enum Status {
        AutoFollow, // 自动关注
        Follow,     // 主动关注
        UnFollow    // 主动取消关注
    }
}
