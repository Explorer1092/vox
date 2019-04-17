package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.dao.jdbc.*;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@Getter
@Setter
@MysqlShard(
        tableName = "VOX_AFENTI_LEARNING_PLAN_USER_RANK_STAT",
        key = "userId",
        count = @MysqlShardCounts(
                defaultCount = 1000,
                extraCounts = {
                        @MysqlShardCount(mode = Mode.DEVELOPMENT, count = 2),
                        @MysqlShardCount(mode = Mode.TEST, count = 2)
                }
        ),
        type = "long"
)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class AfentiLearningPlanUserRankStat implements PrimaryKeyAccessor<Long>, Serializable {
    private static final long serialVersionUID = -6783936542119446375L;

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC) private Long id;
    @UtopiaSqlColumn(name = "CREATETIME") private Date createTime;
    @UtopiaSqlColumn(name = "UPDATETIME") private Date updateTime;

    @UtopiaSqlColumn(name = "USER_ID") private Long userId;
    @UtopiaSqlColumn(name = "NEW_BOOK_ID") private String newBookId;
    @UtopiaSqlColumn(name = "NEW_UNIT_ID") private String newUnitId;
    @UtopiaSqlColumn(name = "RANK") private Integer rank; // 某本书的某个单元的第rank关
    @UtopiaSqlColumn(name = "STAR") private Integer star; // 当前关卡获得几颗星
    @UtopiaSqlColumn(name = "SILVER") private Integer silver; // 学豆奖励
    @UtopiaSqlColumn(name = "SUCCESSIVE_SILVER") private Integer successiveSilver; // 连续奖励
    @UtopiaSqlColumn(name = "BONUS") private Integer bonus; // 总结关卡的宝箱奖励
    @UtopiaSqlColumn(name = "SUBJECT") private Subject subject; // 学科

    public static String ck_uid_nbid(Long userId, String newBookId) {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUserRankStat.class,
                new String[]{"UID", "NBID"},
                new Object[]{userId, newBookId});
    }

    public static String ck_uid_s(Long userId, Subject subject) {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUserRankStat.class,
                new String[]{"UID", "S"},
                new Object[]{userId, subject});
    }

    public static String ck_iuid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUserRankStat.class,
                new String[]{"UID_INTEGRAL"},
                new Object[]{userId});
    }

}
