package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseCategory;
import com.voxlearning.utopia.service.mizar.api.constants.microcourse.MicroCourseStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourse;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriodRef;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourseUserRef;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.loader.MicroCourseLoader;
import com.voxlearning.utopia.service.mizar.api.mapper.CoursePeriodMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.MicroCourseSummary;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarCourseMapper;
import lombok.Getter;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 微课堂相关 Loader Client
 * Created by yuechen.wang on 2016/12/12.
 */
public class MicroCourseLoaderClient {

    @Getter
    @ImportService(interfaceClass = MicroCourseLoader.class)
    private MicroCourseLoader courseLoader;

    @Inject private MizarUserLoaderClient mizarUserLoaderClient;

    public MicroCourseSummary loadCourseById(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return null;
        }
        return mapCourseDetail(courseLoader.loadMicroCourse(courseId));
    }

    public Page<MicroCourseSummary> findCoursesByParam(String courseName, String category, MicroCourseStatus status, Pageable pageable) {
        if (pageable == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        Page<MicroCourse> resultPage = courseLoader.findCoursesByParam(courseName, category, status, pageable);

        List<MicroCourseSummary> mapperList = resultPage
                .getContent()
                .stream()
                .map(this::mapCourseDetail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new PageImpl<>(mapperList, pageable, resultPage.getTotalElements());
    }

    public Page<MicroCourseSummary> loadUserCourses(String userId, Pageable pageable) {
        if (StringUtils.isBlank(userId) || pageable == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        // 根据用户获取到课程信息
        List<String> courseIds = courseLoader.findCourseUserRefByUser(userId).stream()
                .map(MicroCourseUserRef::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        List<MicroCourseSummary> mapperList = courseLoader.loadMicroCourses(courseIds).values()
                .stream()
                .filter(course -> MicroCourseStatus.ONLINE.getOrder() == course.getStatus())
                .map(this::mapCourseDetail)
                .filter(course -> MicroCourseStatus.ONLINE == course.getStatus() || MicroCourseStatus.LIVE == course.getStatus())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PageableUtils.listToPage(mapperList, pageable);
    }

    public boolean isLecturerBindCourse(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        // 根据用户获取到其主讲课程信息
        List<String> courseIds = courseLoader.findCourseUserRefByUser(userId).stream()
                .filter(ref -> MicroCourseUserRef.CourseUserRole.Lecturer == ref.getRole())
                .map(MicroCourseUserRef::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        return courseLoader.loadMicroCourses(courseIds).values()
                .stream().anyMatch(course -> !course.isDisabledTrue());
    }

    Page<MizarCourseMapper> loadMicroCoursePage(Long parentId, MizarCourseCategory category, String tag, Pageable pageable) {
        if (parentId == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        return courseLoader.loadMicroCoursePage(parentId, category, tag, pageable);
    }

    public CoursePeriodMapper loadPeriodById(String periodId) {
        if (StringUtils.isBlank(periodId)) {
            return null;
        }
        return courseLoader.loadPeriodMapperById(periodId);
    }

    public Set<Long> loadPeriodUnNotifiedUsers(String periodId) {
        if (StringUtils.isBlank(periodId)) {
            return Collections.emptySet();
        }
        return courseLoader.findPeriodUserRefByPeriod(periodId)
                .stream()
                .filter(ref -> !Boolean.TRUE.equals(ref.getNotified()))
                .map(ref -> SafeConverter.toLong(ref.getUserId()))
                .filter(id -> id > 0)
                .collect(Collectors.toSet());
    }

    private MicroCourseSummary mapCourseDetail(MicroCourse course) {
        if (course == null) {
            return null;
        }
        // 找到课程相关的课时信息
        List<String> periodIds = courseLoader.findCoursePeriodRefByCourse(course.getId())
                .stream()
                .map(MicroCoursePeriodRef::getPeriodId)
                .distinct()
                .collect(Collectors.toList());
        List<MicroCoursePeriod> periods = new LinkedList<>(courseLoader.loadCoursePeriods(periodIds).values());

        // 找到课程相关的用户信息
        Map<MicroCourseUserRef.CourseUserRole, List<MicroCourseUserRef>> userRefs = courseLoader.findCourseUserRefByCourse(course.getId())
                .stream()
                .filter(ref -> ref.getRole() != null)
                .collect(Collectors.groupingBy(MicroCourseUserRef::getRole));

        return MicroCourseSummary.newInstance()
                .withCourse(course)
                .withPeriods(periods, periodIds)
                .withLecturers(loadUserByRole(userRefs.get(MicroCourseUserRef.CourseUserRole.Lecturer)))
                .withAssistants(loadUserByRole(userRefs.get(MicroCourseUserRef.CourseUserRole.Assistant)));
    }

    private List<MizarUser> loadUserByRole(List<MicroCourseUserRef> userRefs) {
        if (CollectionUtils.isEmpty(userRefs)) {
            return Collections.emptyList();
        }
        List<String> users = userRefs.stream()
                .map(MicroCourseUserRef::getUserId)
                .distinct()
                .collect(Collectors.toList());
        return new LinkedList<>(mizarUserLoaderClient.loadUsers(users).values());
    }

    public Page<MicroCoursePeriod> findPeriodPage(String courseId, String theme, Pageable pageable) {
        if (pageable == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        return courseLoader.findPeriodPage(courseId, theme, pageable);
    }

    public List<MicroCoursePeriod> loadCoursePeriods(String id) {
        if (StringUtils.isBlank(id)) {
            return Collections.emptyList();
        }
        List<String> periodIds = courseLoader.findCoursePeriodRefByCourse(id)
                .stream()
                .map(MicroCoursePeriodRef::getPeriodId)
                .distinct()
                .collect(Collectors.toList());
        return courseLoader.loadCoursePeriods(periodIds).values()
                .stream()
                .collect(Collectors.toList());
    }

    public MicroCourseUserRef.CourseUserRole checkCourseUserRole(String courseId, String userId) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(userId)) {
            return null;
        }
        return courseLoader.findCourseUserRefByCourse(courseId).stream()
                .filter(ref -> StringUtils.equals(userId, ref.getUserId()))
                .map(MicroCourseUserRef::getRole)
                .findFirst().orElse(null);
    }

}
