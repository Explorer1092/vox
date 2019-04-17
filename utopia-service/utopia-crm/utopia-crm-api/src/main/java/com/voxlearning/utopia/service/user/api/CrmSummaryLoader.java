/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.user.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.entity.crm.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 线上使用CrmTeacherSummary, CrmGroupSummary等数据时调用这里的接口
 * Created by Summer Yang on 2016/5/10.
 */
@ServiceVersion(version = "2.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CrmSummaryLoader extends IPingable {

    // ------------------------------------------------------------------------------------------
    // ---------------------        CrmTeacherSummary Related Loader               --------------
    // ------------------------------------------------------------------------------------------
    @CacheMethod(type = CrmTeacherSummary.class, writeCache = false)
    Map<Long, List<CrmTeacherSummary>> loadSchoolTeachers(@CacheParameter(value = "SID", multiple = true) Collection<Long> schoolIds);

    @CacheMethod(type = CrmTeacherSummary.class, writeCache = false)
    CrmTeacherSummary loadTeacherSummary(@CacheParameter("TID") Long teacherId);

    @CacheMethod(type = CrmTeacherSummary.class, writeCache = false)
    CrmTeacherSummary loadTeacherSummaryByMobile(@CacheParameter("MOB") String teacherMobile);

    @CacheMethod(type = CrmTeacherSummary.class, writeCache = false)
    Map<Long, CrmTeacherSummary> loadTeacherSummary(@CacheParameter(value = "TID", multiple = true) Collection<Long> teacherIds);

    @CacheMethod(type = CrmTeacherSummary.class, writeCache = false)
    List<CrmTeacherSummary> loadSchoolTeachers(@CacheParameter("SID") Long schoolId);

    List<CrmTeacherSummary> loadTeacherSummaryByFakeFlag(Boolean fakeTeacher,String validationType);

    List<CrmTeacherSummary> findByCountyCodes(Collection<Integer> countyCodes);

    // ------------------------------------------------------------------------------------------
    // ---------------------         CrmGroupSummary Related Loader                --------------
    // ------------------------------------------------------------------------------------------
    @CacheMethod(type = CrmGroupSummary.class, writeCache = false)
    CrmGroupSummary loadGroupSummary(@CacheParameter("GID") Long groupId);

    @CacheMethod(type = CrmGroupSummary.class, writeCache = false)
    Map<Long, CrmGroupSummary> loadGroupSummary(@CacheParameter(value = "GID", multiple = true) Collection<Long> groupIds);

    @CacheMethod(type = CrmGroupSummary.class, writeCache = false)
    List<CrmGroupSummary> loadTeacherGroupSummary(@CacheParameter("TID") Long teacherId);

    @CacheMethod(type = CrmGroupSummary.class, writeCache = false)
    Map<Long, List<CrmGroupSummary>> loadTeachersGroupSummary(@CacheParameter(value = "TID", multiple = true) Collection<Long> teacherIds);

    @CacheMethod(type = CrmGroupSummary.class, writeCache = false)
    List<CrmGroupSummary> loadSchoolGroupSummary(@CacheParameter("SID") Long schoolId);

    // ------------------------------------------------------------------------------------------
    // ---------------------         CrmSchoolSummary Related Loader               --------------
    // ------------------------------------------------------------------------------------------
    @CacheMethod(type = CrmSchoolSummary.class, writeCache = false)
    CrmSchoolSummary loadSchoolSummary(@CacheParameter("SID") Long schoolId);

    @CacheMethod(type = CrmSchoolSummary.class, writeCache = false)
    Map<Long, CrmSchoolSummary> loadSchoolSummary(@CacheParameter(value = "SID", multiple = true) Collection<Long> schoolIds);

    List<CrmSchoolSummary> findSchool(String schoolName, Integer limit);
    List<CrmSchoolSummary> findByCityCodesAndName(Collection<Integer> cityCodes, String schoolName);

    /**
     * 根据学校ID查年级
     * @param schoolId
     * @return
     */

    @CacheMethod(type = CrmClazzlevelSummary.class, writeCache = false)
    List<CrmClazzlevelSummary> getCrmClazzlevelSummaryList(Long schoolId);


    /**
     * 根据学校ID查班级
     * @param schoolId
     * @return
     */
    @CacheMethod(type = CrmClazzSummary.class, writeCache = false)
    List<CrmClazzSummary> getCrmClazzSummaryList(Long schoolId);

    /**
     *根据班级id获取班组
     * @param classId
     * @return
     */
    @CacheMethod(type = CrmClazzSummary.class, writeCache = false)
    List<CrmGroupSummary> loadClassGroupSummary(Long classId);

    /**
     * 根据学校ids查班级
     * @param schoolIds
     * @return
     */
    @CacheMethod(type = CrmClazzSummary.class, writeCache = false)
    Map<Long, List<CrmClazzSummary>> getCrmClazzSummaryBySchoolIds(Collection<Long> schoolIds);

    /**
     * 根据班级ids获取班组信息
     * @param classIds
     * @return
     */
    @CacheMethod(type = CrmGroupSummary.class, writeCache = false)
    Map<Long, List<CrmGroupSummary>> getCrmGroupSummaryByClassIds(Collection<Long> classIds);
}
