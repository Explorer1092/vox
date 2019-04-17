package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 * Created by alex on 2016/9/12.
 */
@Getter
@Setter
@UtopiaCacheRevision("20160912")
public class SchoolVisitResultInfo implements Serializable{

    private Long schoolId;
    private Integer schoolTeacherCount;  // 学校老师总数
    private Integer visitedUsedCount;    // 拜访已使用老师数
    private Integer visitedUnusedCount;  // 拜访未使用老师数
    private Integer unvisitedUsedCount;  // 未拜访已使用的老师数
    private Integer unvisitedUnusedCount;// 未拜访未使用老师数

    private SchoolVisitResultInfo(Long schoolId) {
        this.schoolId = schoolId;
        schoolTeacherCount = 0;
        visitedUsedCount = 0;
        visitedUnusedCount = 0;
        unvisitedUsedCount = 0;
        unvisitedUnusedCount = 0;
    }

    public static SchoolVisitResultInfo newInstance(Long schoolId) {
        return new SchoolVisitResultInfo(schoolId);
    }

    public static String ck_school(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(SchoolVisitResultInfo.class, "sid", schoolId);
    }

    public void addVisitedUsedCount(Integer count) {
        this.visitedUsedCount += count;
    }

    public void addVisitedUnusedCount(Integer count) {
        this.visitedUnusedCount += count;
    }

    public void addUnvisitedUsedCount(Integer count) {
        this.unvisitedUsedCount += count;
    }

    public void addUnvisitedUnusedCount(Integer count) {
        this.unvisitedUnusedCount += count;
    }

}
