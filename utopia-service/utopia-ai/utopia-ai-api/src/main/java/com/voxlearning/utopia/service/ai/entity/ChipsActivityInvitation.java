package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import lombok.Data;

/**
 * 薯条班级
 */

@Data
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_ACTIVITY_INVITATION")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190306")
public class ChipsActivityInvitation extends LongIdEntityWithDisabledField {

    private static final long serialVersionUID = 1196362524382052882L;

    @UtopiaSqlColumn(name = "INVITER")
    private Long inviter;// '邀请人'
    @UtopiaSqlColumn(name = "INVITEE")
    private Long invitee;// '被邀请人'
    @UtopiaSqlColumn(name = "ACTIVITY_TYPE")
    private String activityType;
    @UtopiaSqlColumn(name = "STATUS")
    private Integer status;//下单未支付:1,成功购买: 2,退款:3',

    //缓存key
    public static String ck_type_inviter(String type, Long inviter) {
        return CacheKeyGenerator.generateCacheKey(ChipsActivityInvitation.class, new String[]{"TYPE", "INVIER"},
                new Object[]{type, inviter});
    }

    //缓存key
    public static String ck_inviter(Long inviter) {
        return CacheKeyGenerator.generateCacheKey(ChipsActivityInvitation.class, new String[]{"INVIER"}, new Object[]{inviter});
    }

}
