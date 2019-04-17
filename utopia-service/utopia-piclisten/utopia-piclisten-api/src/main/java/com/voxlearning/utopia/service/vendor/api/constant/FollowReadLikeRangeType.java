package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 跟读点赞排榜榜类型
 *
 * @author jiangpeng
 * @since 2017-03-28 下午5:23
 **/
@Getter
@AllArgsConstructor
public enum FollowReadLikeRangeType {


    YESTERDAY_GLOBAL(1, "yesterday", "global", false),
    YESTERDAY_CITY(2, "yesterday", "city", true),
    YESTERDAY_SCHOOL(3, "yesterday", "school", true),

    LAST_WEEK_GLOBAL(4, "last_week", "global", false),
    LAST_WEEK_CITY(5, "last_week", "city", true),
    LAST_WEEK_SCHOOL(6, "last_week", "school", true)

    ;


    private Integer id;

    private String period;

    private String dimension;

    private boolean needClazz;



    public static FollowReadLikeRangeType paresFromPeriodDimension(String period, String dimension){
        return Arrays.stream(FollowReadLikeRangeType.values())
                .filter(t -> t.getDimension().equals(dimension) && t.getPeriod().equals(period)).findFirst().orElse(null);
    }

    public static Boolean validatePeriod(String period){
        for (FollowReadLikeRangeType followReadLikeRangeType : FollowReadLikeRangeType.values()) {
            if (followReadLikeRangeType.getPeriod().equals(period))
                return true;
        }
        return false;
    }

    public static Boolean validateDimension(String dimension){
        for (FollowReadLikeRangeType followReadLikeRangeType : FollowReadLikeRangeType.values()) {
            if (followReadLikeRangeType.getDimension().equals(dimension))
                return true;
        }
        return false;
    }

}
