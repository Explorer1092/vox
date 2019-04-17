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

import java.util.Date;

@Data
@NoArgsConstructor
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_GROUP_SHOPPING")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190218")
public class ChipsGroupShopping  extends LongIdEntityWithDisabledField {
    private static final long serialVersionUID = -950058194329641828L;
    @UtopiaSqlColumn(name = "SPONSOR")
    private Long sponsor;   //发起人
    @UtopiaSqlColumn(name = "ORDER_ID")
    private String orderId;
    @UtopiaSqlColumn(name = "CODE")
    private String code;
    @UtopiaSqlColumn(name = "NUMBER")
    private Integer number;


    public ChipsGroupShopping(Long sponsor, String orderId, String code) {
        this.code = code;
        this.sponsor = sponsor;
        this.orderId = orderId;
        this.number = 0;
        this.setDisabled(false);
        Date now = new Date();
        this.setCreateTime(now);
        this.setUpdateTime(now);
    }

    //缓存key
    public static String ck_code(String code) {
        return CacheKeyGenerator.generateCacheKey(ChipsGroupShopping.class, "C", code);
    }

    //缓存key
    public static String ck_active() {
        return CacheKeyGenerator.generateCacheKey(ChipsGroupShopping.class, "active");
    }

}
