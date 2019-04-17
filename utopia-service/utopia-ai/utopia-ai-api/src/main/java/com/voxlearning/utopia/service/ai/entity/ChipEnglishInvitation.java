package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author songtao
 * @since 2018/5/3
 */
@Getter
@Setter
@ToString
@DocumentConnection(configName = "main")
@DocumentTable(table = "VOX_CHIP_ENGLISH_INVITATION")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180525")
public class ChipEnglishInvitation extends LongIdEntityWithDisabledField {
    private static final long serialVersionUID = 8181042243973257085L;
    @UtopiaSqlColumn(name = "INVITER") private Long inviter;//邀请人
    @UtopiaSqlColumn(name = "INVITEE") private Long invitee;//被邀请人
    @UtopiaSqlColumn(name = "PRODUCT_ITEM_ID") private String productItemId;
    @UtopiaSqlColumn(name = "PRODUCT_ID") private String productId;
    @UtopiaSqlColumn(name = "SEND") private Boolean send;//是否发红包


    public static String ck_inviter(Long inviter) {
        return CacheKeyGenerator.generateCacheKey(AIUserQuestionResultHistory.class, new String[]{"INVITER"},
                new Object[]{inviter});
    }

}
