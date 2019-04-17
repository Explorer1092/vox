package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.constants.microcourse.MicroCourseStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.*;
import com.voxlearning.utopia.service.mizar.api.service.MicroCourseService;
import com.voxlearning.utopia.service.mizar.impl.dao.microcourse.*;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 微课堂相关
 * Created by yuechen.wang on 2016/12/12.
 */
@Named
@Service(interfaceClass = MicroCourseService.class)
@ExposeService(interfaceClass = MicroCourseService.class)
public class MicroCourseServiceImpl extends SpringContainerSupport implements MicroCourseService {

    @Inject protected UtopiaSqlFactory utopiaSqlFactory;
    protected UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Inject private MicroCourseDao microCourseDao;
    @Inject private MicroCoursePeriodDao microCoursePeriodDao;
    @Inject private MicroCourseUserRefDao microCourseUserRefDao;
    @Inject private MicroCoursePeriodRefDao microCoursePeriodRefDao;
    @Inject private CoursePeriodUserRefDao coursePeriodUserRefDao;

    @Override
    public MapMessage saveCourse(MicroCourse course) {
        if (course == null) {
            return MapMessage.errorMessage("参数错误");
        }
        MicroCourse upsert = microCourseDao.upsert(course);
        boolean success = upsert != null && upsert.getId() != null;
        MapMessage msg = new MapMessage();
        msg.setSuccess(success);
        msg.setInfo(success ? "保存成功" : "保存失败");
        if (success) msg.add("courseId", upsert.getId());
        return msg;
    }

    @Override
    public MapMessage removeCourse(String courseId) {
        if (!ObjectId.isValid(courseId)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return utopiaSql.withTransaction(status -> {
                List<String> periodIds = microCoursePeriodRefDao.findByCourse(courseId)
                        .stream()
                        .map(MicroCoursePeriodRef::getPeriodId)
                        .collect(Collectors.toList());
                // 1. 课时置为 disabled
                microCoursePeriodDao.disable(periodIds);
                // 2. 课程置为 disabled
                microCourseDao.disable(courseId);
                return MapMessage.successMessage();
            });
        } catch (Exception ex) {
            logger.error("Failed remove Micro Course, course={}", courseId, ex);
            return MapMessage.errorMessage("课程删除失败：" + ex.getMessage());
        }
    }


    @Override
    public MapMessage updateCourseStatus(String courseId, MicroCourseStatus status) {
        if (StringUtils.isBlank(courseId) || status == null) {
            return MapMessage.errorMessage("参数错误");
        }
        boolean success = microCourseDao.updateStatus(courseId, status);
        MapMessage msg = new MapMessage();
        msg.setSuccess(success);
        msg.setInfo(success ? "状态更新成功" : "状态更新失败");
        return msg;
    }

    @Override
    public MapMessage appendTeacher(String courseId, Collection<String> teacherIds, MicroCourseUserRef.CourseUserRole role) {
        if (StringUtils.isBlank(courseId) || role == null) {
            return MapMessage.errorMessage("参数错误");
        }
        // 无论如何，都先清除之前的关联记录重新保存，方便交互
        microCourseUserRefDao.removeByCourse(courseId, role);
        teacherIds.stream().distinct()
                .forEach(tid -> microCourseUserRefDao.insertSpecificRef(courseId, tid, role));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage appendPeriod(String courseId, String periodId) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(periodId)) {
            return MapMessage.errorMessage("参数错误");
        }

        List<MicroCoursePeriodRef> refList = microCoursePeriodRefDao.findByCourse(courseId);
        MicroCoursePeriodRef ref = refList.stream().filter(p -> Objects.equals(p.getPeriodId(), periodId)).findFirst().orElse(null);
        if (ref == null) {
            microCoursePeriodRefDao.insertSpecificRef(courseId, periodId);
        }

        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        msg.setInfo("课时添加成功");
        return msg;
    }

    @Override
    public MapMessage removeCoursePeriod(String courseId, String periodId) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(periodId)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return utopiaSql.withTransaction(status -> {
                // 1. 删除课程课时关联
                microCoursePeriodRefDao.removeCoursePeriod(courseId, periodId);
                // 2. 课时置为 disabled
                microCoursePeriodDao.disable(Collections.singletonList(periodId));
                return MapMessage.successMessage();
            });
        } catch (Exception ex) {
            logger.error("Failed remove Micro Course Period, course={}, period={}", courseId, periodId, ex);
            return MapMessage.errorMessage("课时删除失败：" + ex.getMessage());
        }
    }

    @Override
    public void removePeriod(String periodId) {
        if (StringUtils.isBlank(periodId)) {
            return;
        }
        try {
            microCoursePeriodDao.remove(periodId);
        } catch (Exception ex) {
            logger.error("Failed remove Micro Course Period, period={}", periodId, ex);
        }
    }

    @Override
    public MapMessage savePeriod(MicroCoursePeriod period) {
        if (period == null) {
            return MapMessage.errorMessage("参数错误");
        }
        MicroCoursePeriod upsert = microCoursePeriodDao.upsert(period);
        boolean success = upsert != null && upsert.getId() != null;
        MapMessage msg = new MapMessage();
        msg.setSuccess(success);
        msg.setInfo(success ? "保存成功" : "保存失败");
        if (success) msg.add("periodId", upsert.getId());
        return msg;
    }

    @Override
    public MapMessage savePeriodUserRef(String periodId, String userId, String targetId, CoursePeriodUserRef.UserPeriodRelation relation, boolean fromWechat) {
        if (StringUtils.isBlank(periodId) || StringUtils.isBlank(userId) || relation == null) {
            return MapMessage.errorMessage("参数错误");
        }

        List<CoursePeriodUserRef> periodUserRefs = coursePeriodUserRefDao.findByPeriodAndUser(periodId, userId);
        CoursePeriodUserRef exist = periodUserRefs.stream()
                .filter(p -> p.getRelation() == relation)
                .findFirst().orElse(null);

        if (exist == null) {
            coursePeriodUserRefDao.insertSpecificRef(periodId, userId, targetId, relation, fromWechat);
        }

        MapMessage msg = new MapMessage();
        msg.setSuccess(true);
        msg.setInfo("用户关联成功");
        return msg;
    }

    @Override
    public MapMessage updateUserPeriodRef(String periodId, List<Long> userIds) {
        if (StringUtils.isBlank(periodId) || CollectionUtils.isEmpty(userIds)) {
            return MapMessage.errorMessage("参数错误");
        }
        coursePeriodUserRefDao.updateUserPeriodRef(periodId, userIds);
        return MapMessage.successMessage();
    }

}