package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseCategory;
import com.voxlearning.utopia.service.mizar.api.constants.microcourse.MicroCourseStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.*;
import com.voxlearning.utopia.service.mizar.api.mapper.CoursePeriodMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarCourseMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 微课堂相关 Loader
 * Created by yuechen.wang on 2016/12/12.
 */
@ServiceVersion(version = "20161209")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MicroCourseLoader extends IPingable {

    //--------------------------------------------------------------------
    //------------------          课程相关               -----------------
    //--------------------------------------------------------------------
    default MicroCourse loadMicroCourse(String courseId) {
        return loadMicroCourses(Collections.singleton(courseId)).get(courseId);
    }

    @CacheMethod(type = MicroCourse.class, writeCache = false)
    Map<String, MicroCourse> loadMicroCourses(@CacheParameter(multiple = true) Collection<String> courseIds);

    Page<MicroCourse> findCoursesByParam(String courseName, String category, MicroCourseStatus status, Pageable pageable);

    Page<MizarCourseMapper> loadMicroCoursePage(Long parentId, MizarCourseCategory category, String tag, Pageable pageable);

    Set<String> loadOnlineCourses();

    //--------------------------------------------------------------------
    //------------------          课时相关               -----------------
    //--------------------------------------------------------------------
    default MicroCoursePeriod loadCoursePeriod(String periodId) {
        return loadCoursePeriods(Collections.singleton(periodId)).get(periodId);
    }

    @CacheMethod(type = MicroCoursePeriod.class, writeCache = false)
    Map<String, MicroCoursePeriod> loadCoursePeriods(@CacheParameter(multiple = true) Collection<String> periodIds);

    /**
     * 通过课程/课时ID加载出来详情信息
     */
    CoursePeriodMapper loadPeriodMapperById(String id);

    Page<MicroCoursePeriod> findPeriodPage(String courseId, String theme, Pageable pageable);

    //--------------------------------------------------------------------
    //------------------          课程用户关联           -----------------
    //--------------------------------------------------------------------
    @CacheMethod(type = MicroCourseUserRef.class, writeCache = false)
    List<MicroCourseUserRef> findCourseUserRefByUser(@CacheParameter("U") String userId);

    @CacheMethod(type = MicroCourseUserRef.class, writeCache = false)
    List<MicroCourseUserRef> findCourseUserRefByCourse(@CacheParameter("C") String courseId);

    //--------------------------------------------------------------------
    //------------------          课程课时关联           -----------------
    //--------------------------------------------------------------------
    @CacheMethod(type = MicroCoursePeriodRef.class, writeCache = false)
    List<MicroCoursePeriodRef> findCoursePeriodRefByCourse(@CacheParameter("C") String courseId);

    @CacheMethod(type = MicroCoursePeriodRef.class, writeCache = false)
    MicroCoursePeriodRef findCoursePeriodRefByPeriod(@CacheParameter("P") String periodId);

    //--------------------------------------------------------------------
    //------------------          课时用户关联           -----------------
    //--------------------------------------------------------------------
    @CacheMethod(type = CoursePeriodUserRef.class, writeCache = false)
    List<CoursePeriodUserRef> findPeriodUserRefByPeriod(@CacheParameter("P") String periodId);

    //--------------------------------------------------------------------
    //------------------             其  他              -----------------
    //--------------------------------------------------------------------


}
