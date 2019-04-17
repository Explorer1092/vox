package com.voxlearning.utopia.service.ai.entity.reddot;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_USER_RED_DOT_RECORD")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190318")
public class ChipsUserRedDotPageRecord extends LongIdEntityWithDisabledField {

    private static final long serialVersionUID = 1L;
    @UtopiaSqlColumn(name = "USER_ID") private Long userId;
    @UtopiaSqlColumn(name = "PAGE") private Long page;
    @UtopiaSqlColumn(name = "READ") private Boolean read;

    public ChipsUserRedDotPageRecord(Long userId, Long page, Boolean read) {
        this.setPage(page);
        this.setRead(read);
        this.setUserId(userId);
        this.setCreateTime(new Date());
        this.setDisabled(false);
        this.setUpdateTime(new Date());
    }

    //缓存key
    public static String ck_user(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ChipsUserRedDotPageRecord.class, new String[]{"U"},
                new Object[]{userId});
    }
}
