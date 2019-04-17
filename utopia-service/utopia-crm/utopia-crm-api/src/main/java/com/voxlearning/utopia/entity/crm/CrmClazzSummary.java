package com.voxlearning.utopia.entity.crm;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CrmClazzSummary 班级summary
 *
 * @author song.wang
 * @date 2017/7/14
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_clazz_summary_{}", dynamic = true)
@DocumentRangeable(range = DateRangeType.D)
@UtopiaCacheExpiration
@UtopiaCacheRevision("201708041")
public class CrmClazzSummary implements CacheDimensionDocument {
    private static final long serialVersionUID = 763021966256981335L;

    @DocumentId
    private String id;

    //基础信息
    private Long clazzId;                                     // 班级ID
    private Integer clazzLevel;                               // 年级
    private String clazzName;                                 // 班级名称
    private Long schoolId;                                    // 学校ID
    private String schoolName;                                // 学校名称
    private SchoolLevel schoolLevel;                          // 学校阶段
    private Integer regStuCount;                              // 累计注册学生数
    private Integer authStuCount;                             // 累计认证学生数

    //17作业
    private Integer finSglSubjHwEq1AuStuCount;                // 认证学生当月完成1套任一科目作业学生数   =1
    private Integer finSglSubjHwEq2AuStuCount;                // 认证学生当月完成2套任一科目作业学生数   =2
    private Integer finSglSubjHwGte3AuStuCount;               // 认证学生当月完成3套及以上任一科目作业学生数   ≥3

    //快乐学
    private Integer stuKlxTnCount;                            // 考号数
    private Integer tmMathAnshEq1StuCount;                    // 当月答题卡作答1次数学试卷学生数   =1
    private Integer tmMathAnshGte2StuCount;                   // 当月答题卡作答2次及以上数学试卷学生数   ≥2


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                CacheKeyGenerator.generateCacheKey(CrmClazzSummary.class, "SID", schoolId)
        };
    }
}
