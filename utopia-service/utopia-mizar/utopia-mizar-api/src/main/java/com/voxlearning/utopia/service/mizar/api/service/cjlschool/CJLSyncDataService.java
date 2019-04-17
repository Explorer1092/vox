package com.voxlearning.utopia.service.mizar.api.service.cjlschool;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLClass;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLStudent;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacherCourse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
@ServiceVersion(version = "20170715")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CJLSyncDataService {

    //============================================================
    //================         Class           ===================
    //============================================================

    @NoResponseWait
    void syncClass(List<CJLClass> classList);

    @NoResponseWait
    void modifyClass(CJLClass sourceClass);

    //============================================================
    //================        Teacher          ===================
    //============================================================

    @NoResponseWait
    void syncSchoolTeacher(String sourceSchoolId, List<CJLTeacher> sourceTeacherList);

    @NoResponseWait
    void modifyTeacher(CJLTeacher sourceTeacher);

    @CacheMethod(type = CJLTeacher.class, writeCache = false)
    CJLTeacher findUserByLoginName(@CacheParameter("L") String loginName);

    //============================================================
    //=============        Teacher Course          ===============
    //============================================================

    @NoResponseWait
    void syncTeacherCourse(CJLTeacherCourse course);

    @NoResponseWait
    void modifyTeacherCourse(CJLTeacherCourse course);

    //============================================================
    //================        Student          ===================
    //============================================================

    @NoResponseWait
    void syncStudents(List<CJLStudent> students);

    @NoResponseWait
    void modifyStudent(CJLStudent sourceStudent);

    //============================================================
    //==============     Only For Schedule        ================
    //============================================================
    List<CJLTeacher> findAllTeacher();

    List<CJLClass> findAllClass();

    List<CJLStudent> findAllStudent();

    List<CJLTeacherCourse> findAllTeacherCourseForJob();

}
