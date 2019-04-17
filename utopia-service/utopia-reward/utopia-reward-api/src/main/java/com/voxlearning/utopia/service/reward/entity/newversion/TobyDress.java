package com.voxlearning.utopia.service.reward.entity.newversion;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_TOBY_DRESS")
@UtopiaCacheRevision("20180730")
public class TobyDress extends AbstractDatabaseEntity implements Serializable {
    @UtopiaSqlColumn
    private String url;
    @UtopiaSqlColumn
    private String name;
    @UtopiaSqlColumn
    private Integer type;

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(TobyDress.class, "ALL");
    }

    public static String ck_Id(Long id) {
        return CacheKeyGenerator.generateCacheKey(TobyCountenanceCVRecord.class, "ID", id);
    }
}
