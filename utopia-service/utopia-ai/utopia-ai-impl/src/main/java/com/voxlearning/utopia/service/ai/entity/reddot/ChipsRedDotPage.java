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
@DocumentTable(table = "VOX_CHIPS_RED_DOT_PAGE")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190401")
public class ChipsRedDotPage extends LongIdEntityWithDisabledField {

    private static final long serialVersionUID = 1L;

    @UtopiaSqlColumn(name = "CODE") private String code;   //标识
    @UtopiaSqlColumn(name = "NAME") private String name;   //名称
    @UtopiaSqlColumn(name = "RANK") private Integer rank;   //排序
    @UtopiaSqlColumn(name = "PARENT") private Long parent; //子页面

    public ChipsRedDotPage(String name, String code, Long parent) {
        this.setCode(code);
        this.setName(name);
        this.setParent(parent);
        this.setDisabled(false);
        this.setCreateTime(new Date());
        this.setUpdateTime(new Date());
    }

    //缓存key
    public static String ck_code(String code) {
        return CacheKeyGenerator.generateCacheKey(ChipsRedDotPage.class, new String[]{"C"},
                new Object[]{code});
    }

    //缓存key
    public static String ck_parent(Long parent) {
        return CacheKeyGenerator.generateCacheKey(ChipsRedDotPage.class, new String[]{"P"},
                new Object[]{parent});
    }

}
