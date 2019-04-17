package com.voxlearning.utopia.service.reward.entity.newversion;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.reward.entity.RewardActivityRecord;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_GAME_PRIZE_CLAW")
@UtopiaCacheRevision("20180730")
public class PrizeClaw extends AbstractDatabaseEntity implements Serializable {
    @UtopiaSqlColumn(name = "PRIZE_NAME") private String prizeName;
    @UtopiaSqlColumn(name = "ODDS") private Integer odds;
    @UtopiaSqlColumn(name = "SITE") private Integer site;
    @UtopiaSqlColumn(name = "PRIZE_TYPE") private Integer prizeType;
    @UtopiaSqlColumn(name = "PRIZE") private Long prize;
    @UtopiaSqlColumn(name = "CONSUMER_NUM") private Integer consumerNum;
    @UtopiaSqlColumn(name = "PRIZE_PICTER_URL") private String prizePicterUrl;

    public static String ck_site(Integer site) {
        return CacheKeyGenerator.generateCacheKey(PrizeClaw.class, "SITE", site);
    }

    public enum PrizeType {
        DEFAULT(0),
        INTEGRAL(1),//积分奖品
        TOPKNOT(2),//头饰奖品
        TOBY_IMAGE(3),//托比形象奖品
        ;
        private int type;
        private PrizeType(int type) {
            this.type = type;
        }
        public int getType() {
            return type;
        }
    }
}
