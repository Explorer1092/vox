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
 * 薯条英语班级用户关系
 *
 * @Author songtao
 */

@Getter
@Setter
@ToString
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_ENGLISH_CLASS_USER_REF")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180823")
public class ChipsEnglishClassUserRef extends LongIdEntityWithDisabledField {
    @UtopiaSqlColumn(name = "USER_ID")
    private Long userId;
    @UtopiaSqlColumn(name = "CHIPS_CLASS_ID")
    private Long chipsClassId;
    @UtopiaSqlColumn(name = "IN_GROUP")
    private Boolean inGroup;              //是否在微信群
    @UtopiaSqlColumn(name = "LOGIN_WX")
    private Boolean loginWX;              //是否登录微信公众号
    @UtopiaSqlColumn(name = "QUESTIONNAIRES")
    private Boolean questionnaires; //是否填写调查问卷
    @UtopiaSqlColumn(name = "ORDER_REF")
    private String orderRef;             //订单来源

    //缓存key
    public static String ck_class_id(Long chipsClassId) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishClassUserRef.class, "CID", chipsClassId);
    }

    //缓存key
    public static String ck_user_id(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishClassUserRef.class, "UID", userId);
    }
}
