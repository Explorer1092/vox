package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.constants.microcourse.MicroCourseStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.CoursePeriodUserRef;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourse;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourseUserRef;
import com.voxlearning.utopia.service.mizar.api.service.MicroCourseService;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;

/**
 * 微课堂相关 Service Client
 * Created by yuechen.wang on 2016/12/12.
 */
public class MicroCourseServiceClient {

    @Getter
    @ImportService(interfaceClass = MicroCourseService.class)
    private MicroCourseService remoteReference;

    public MapMessage saveCourse(MicroCourse course) {
        if (course == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.saveCourse(course);
    }

    public MapMessage removeCourse(String courseId) {
        if (!ObjectId.isValid(courseId)) {
            return MapMessage.errorMessage("无效的课程ID:" + courseId);
        }
        return remoteReference.removeCourse(courseId);
    }

    public MapMessage updateCourseStatus(String courseId, MicroCourseStatus status) {
        if (StringUtils.isBlank(courseId) || status == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.updateCourseStatus(courseId, status);
    }

    public MapMessage appendTeacher(String courseId, Collection<String> teacherIds, MicroCourseUserRef.CourseUserRole role) {
        if (StringUtils.isBlank(courseId) || role == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.appendTeacher(courseId, teacherIds, role);
    }

    public MapMessage appendPeriod(String courseId, String periodId) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(periodId)) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.appendPeriod(courseId, periodId);
    }

    public MapMessage savePeriod(MicroCoursePeriod period) {
        if (period == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.savePeriod(period);
    }

    public MapMessage removeCoursePeriod(String courseId, String periodId) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(periodId)) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.removeCoursePeriod(courseId, periodId);
    }

    public void removePeriod(String periodId) {
        if (StringUtils.isBlank(periodId)) {
            return;
        }
        remoteReference.removePeriod(periodId);
    }

    public MapMessage savePeriodUserRef(String periodId, String userId, String targetId, CoursePeriodUserRef.UserPeriodRelation relation, boolean fromWechat) {
        if (StringUtils.isBlank(periodId) || StringUtils.isBlank(userId) || relation == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.savePeriodUserRef(periodId, String.valueOf(userId), String.valueOf(targetId), relation, fromWechat);
    }

    public MapMessage updateUserPeriodRef(String periodId, List<Long> userIds) {
        if (StringUtils.isBlank(periodId) || CollectionUtils.isEmpty(userIds)) {
            return MapMessage.successMessage();
        }
        return remoteReference.updateUserPeriodRef(periodId, userIds);
    }

}
