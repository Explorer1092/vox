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
@DocumentTable(table = "VOX_REWARD_LEAVEWORD_GOODS")
@UtopiaCacheRevision("20180730")
public class LeaveWordGoods extends AbstractDatabaseEntity implements Serializable {
    @UtopiaSqlColumn(name = "NAME") private String name;
    @UtopiaSqlColumn(name = "PORTRAIT_URL") private String portraitUrl;
    @UtopiaSqlColumn(name = "PRICE") private Integer price;
    @UtopiaSqlColumn(name = "SPEND_TYPE") private Integer spendType;

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(PrizeClaw.class, "ALL");
    }

    public enum SpendType {
        INTEGRAL(1),
        FRAGMENT(2),
        ;
        private int type;
        SpendType(int type) {
            this.type = type;
        }

        public int intValue() {
            return type;
        }
    }
}
