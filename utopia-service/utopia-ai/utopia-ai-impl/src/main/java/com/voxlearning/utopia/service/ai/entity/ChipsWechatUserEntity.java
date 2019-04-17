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
import lombok.NoArgsConstructor;

/**
 * 微信用户表：小程序、微信公众号
 */
@Data
@NoArgsConstructor
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_WECHAT_USER")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190218")
public class ChipsWechatUserEntity extends LongIdEntityWithDisabledField {
    private static final long serialVersionUID = -950058194329641828L;
    @UtopiaSqlColumn(name = "USER_ID")
    private Long userId;
    @UtopiaSqlColumn(name = "AVATAR")
    private String avatar;
    @UtopiaSqlColumn(name = "NICK_NAME")
    private String nickName;
    @UtopiaSqlColumn(name = "OPEN_ID")
    private String openId;
    @UtopiaSqlColumn(name = "TYPE")
    private Integer type;

    //缓存key
    public static String ck_openId_type(String openId, Integer type) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserEntity.class, new String[]{"O", "T"},
                new Object[]{openId, type});
    }

    //缓存key
    public static String ck_user(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserEntity.class, new String[]{"U"},
                new Object[]{userId});
    }

    //缓存key
    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ChipsWechatUserEntity.class, id);
    }

}
