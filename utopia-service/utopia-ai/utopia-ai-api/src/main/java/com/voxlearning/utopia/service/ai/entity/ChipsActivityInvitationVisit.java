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
@DocumentTable(table = "VOX_CHIPS_ACTIVITY_INVITATION_VISIT")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190306")
public class ChipsActivityInvitationVisit extends LongIdEntityWithDisabledField {

    private static final long serialVersionUID = 6563399136787704555L;

    @UtopiaSqlColumn(name = "INVITER")
    private Long inviter;// '邀请人'
    @UtopiaSqlColumn(name = "AUTHORIZATION_ID")
    private Long authorizationId;// '授权表id'
    @UtopiaSqlColumn(name = "ACTIVITY_TYPE")
    private String activityType;


    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ChipsActivityInvitationVisit.class, id);
    }

    //缓存key
    public static String ck_type_inviter(String type, Long inviter) {
        return CacheKeyGenerator.generateCacheKey(ChipsActivityInvitationVisit.class, new String[]{"TYPE", "INVIER"},
                new Object[]{type, inviter});
    }

    //缓存key
    public static String ck_inviter(Long inviter) {
        return CacheKeyGenerator.generateCacheKey(ChipsActivityInvitationVisit.class, new String[]{"INVIER"},
                new Object[]{inviter});
    }

}
