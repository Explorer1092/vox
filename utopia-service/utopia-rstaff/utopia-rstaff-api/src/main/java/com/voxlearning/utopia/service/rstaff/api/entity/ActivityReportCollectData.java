package com.voxlearning.utopia.service.rstaff.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.ObjectIdEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentTable(table = "VOX_ACTIVITY_REPORT_COLLECT_DATA")
@DocumentConnection(configName = "hs_misc")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ActivityReportCollectData extends ObjectIdEntityWithDisabledField implements CacheDimensionDocument {

    private static final long serialVersionUID = -8152408146315381861L;

    private Integer provinceCode;
    private String provinceName;
    private Integer cityCode;
    private String cityName;
    private Integer regionCode;
    private String regionName;
    private Long schoolId;
    private String schoolName;
    private Integer clazzLevel;
    private Long clazzId;
    private String clazzName;
    private String activityId;          //活动code
    private Integer participantStuds;    //班级维度参与人数
    private Integer participantTimes;    //班级维度参与次数
    private Integer topScoreSum;          //班级维度最高分总和
    private Integer topScoreStudSum;      //班级维度最高分学生数量总和
    private Integer clazzTakeTimes;       //班级所有学生做题目总花费时间
    private Integer clazzExercises;       //班级所有学生所做的题目数
    private Integer scoreLevelStuds1;     //班级维度最高分在且分数在0-9的学上数
    private Integer scoreLevelStuds2;
    private Integer scoreLevelStuds3;
    private Integer scoreLevelStuds4;
    private Integer scoreLevelStuds5;
    private Integer scoreLevelStuds6;

    public String ck_aid_cid(String activityId) {
        return CacheKeyGenerator.generateCacheKey(ActivityReportCollectData.class, new String[]{"AID"}, new Object[]{activityId});
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ck_aid_cid(activityId)
        };
    }
}
