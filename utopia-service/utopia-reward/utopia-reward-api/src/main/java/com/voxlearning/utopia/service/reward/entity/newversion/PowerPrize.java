package com.voxlearning.utopia.service.reward.entity.newversion;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
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
@DocumentTable(table = "VOX_REWARD_POWER_PRIZE")
@UtopiaCacheRevision("20180730")
public class PowerPrize extends AbstractDatabaseEntity implements Serializable {
    private Integer type;
    private Long prize;//类型是碎片则放碎片数量，类型是实物则放实物id
    private String name;
    private Integer level;//奖品等级
    private String picterUrl;
    private Integer stock;
    private Integer initStock;
    private Boolean isReserve;

    public static String ck_level(Integer level) {
        return CacheKeyGenerator.generateCacheKey(PowerPrize.class, "LEVEL", level);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(PowerPrize.class, "ALL");
    }

    public enum PowerLevel{
        ONE(1),
        TWO(2),
        THREE(3);
        private int level;
        PowerLevel(int level) {
            this.level = level;
        }

        public int intValue() {
            return level;
        }
    }

    public enum PrizeType {
        FRAGMENT(1),
        REAL_GOODS(2);
        private int type;
        PrizeType(int type) {
            this.type = type;
        }

        public int intValue() {
            return type;
        }
    }

}
