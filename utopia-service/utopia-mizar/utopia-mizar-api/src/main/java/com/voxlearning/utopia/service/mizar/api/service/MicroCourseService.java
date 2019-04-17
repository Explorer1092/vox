package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.constants.microcourse.MicroCourseStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.CoursePeriodUserRef;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourse;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourseUserRef;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 微课堂相关 Service
 * Created by yuechen.wang on 2016/12/12.
 */
@ServiceVersion(version = "20170228")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MicroCourseService extends IPingable {

    MapMessage saveCourse(MicroCourse course);

    MapMessage removeCourse(String courseId);

    MapMessage updateCourseStatus(String courseId, MicroCourseStatus status);

    MapMessage appendTeacher(String courseId, Collection<String> teacherIds, MicroCourseUserRef.CourseUserRole role);

    MapMessage appendPeriod(String courseId, String periodId);

    MapMessage removeCoursePeriod(String courseId, String periodId);

    void removePeriod(String periodId);

    MapMessage savePeriod(MicroCoursePeriod period);

    MapMessage savePeriodUserRef(String periodId, String userId, String targetId, CoursePeriodUserRef.UserPeriodRelation relation, boolean fromWechat);

    MapMessage updateUserPeriodRef(String periodId, List<Long> userIds);

}
