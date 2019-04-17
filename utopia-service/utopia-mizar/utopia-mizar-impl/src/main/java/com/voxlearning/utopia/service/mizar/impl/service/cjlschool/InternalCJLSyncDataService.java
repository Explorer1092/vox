package com.voxlearning.utopia.service.mizar.impl.service.cjlschool;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLClass;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLStudent;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacherCourse;
import com.voxlearning.utopia.service.mizar.impl.dao.cjlschool.CJLClassDao;
import com.voxlearning.utopia.service.mizar.impl.dao.cjlschool.CJLStudentDao;
import com.voxlearning.utopia.service.mizar.impl.dao.cjlschool.CJLTeacherCourseDao;
import com.voxlearning.utopia.service.mizar.impl.dao.cjlschool.CJLTeacherDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
@Named("syncDataService")
public class InternalCJLSyncDataService extends SpringContainerSupport {

    @Inject private CJLClassDao cjlClassDao;
    @Inject private CJLTeacherDao cjlTeacherDao;
    @Inject private CJLTeacherCourseDao cjlTeacherCourseDao;
    @Inject private CJLStudentDao cjlStudentDao;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    //============================================================
    //================         Class           ===================
    //============================================================
    public MapMessage syncClass(List<CJLClass> classList) {
        if (CollectionUtils.isEmpty(classList)) {
            return MapMessage.successMessage();
        }
        System.out.println(JsonUtils.toJson(classList));
        cjlClassDao.syncBatch(classList);
        return MapMessage.successMessage();
    }

    //============================================================
    //================        Teacher          ===================
    //============================================================
    public MapMessage syncTeacher(List<CJLTeacher> teacherList) {
        if (CollectionUtils.isEmpty(teacherList)) {
            return MapMessage.successMessage();
        }
        teacherList.forEach(t -> System.out.println(JsonUtils.toJson(t)));
        cjlTeacherDao.syncBatch(teacherList);
        return MapMessage.successMessage();
    }

    //============================================================
    //=============        Teacher Course          ===============
    //============================================================
    public MapMessage syncTeacherCourse(List<CJLTeacherCourse> courseList) {
        if (CollectionUtils.isEmpty(courseList)) {
            return MapMessage.successMessage();
        }
        System.out.println(JsonUtils.toJson(courseList));
        cjlTeacherCourseDao.syncBatch(courseList);
        return MapMessage.successMessage();
    }

    //============================================================
    //================        Student          ===================
    //============================================================
    public MapMessage syncStudents(List<CJLStudent> studentList) {
        if (CollectionUtils.isEmpty(studentList)) {
            return MapMessage.successMessage();
        }
        studentList.forEach(t -> System.out.println(JsonUtils.toJson(t)));
        cjlStudentDao.syncBatch(studentList);
        return MapMessage.successMessage();
    }

    //============================================================
    //================         Others          ===================
    //============================================================

    /**
     * 通过 CommonConfig 获取学校的映射关系
     * 测试 : 陈经纶中学(高中部)(414008)
     * 线上 : 陈经纶中学(高中部)(405492)
     */
    public Map<String, Long> getSchoolIdMapping() {
        String schoolMapConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.MIDDLE_PLATFORM_GENERAL.getType(), "CJL_SCHOOL_MAP"
        );

        Map<String, Long> schoolIdMap = new HashMap<>();

        Stream.of(schoolMapConfig.split(",")).forEach(pair -> {
            String[] split = pair.split(":");
            schoolIdMap.put(split[0], SafeConverter.toLong(split[1]));
        });
        return schoolIdMap;
    }
}
