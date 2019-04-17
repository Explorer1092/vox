/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.support;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSAreaHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSSchoolHomeworkBehaviorStat;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkResultLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @since 2015/3/27
 */
abstract class AbstractResearchStaffBehaviorDataGenerator {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject protected NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;


//    @Data
//    static class HomeworkReport {
//        private Long schoolId;                              //学校ID
//        private Long teacherId;                             //老师ID
//        private List<Long> completeUid = new LinkedList<>();       // 完成人数
//    }

    @Data
    @AllArgsConstructor
    static class HomeworkQuiz {
        //        private Subject subject;                            // 数学专用
        private Long clazzId;                               // 班级ID
        private Long teacherId;                             // 老师ID
        private Set<Long> completeUid = new LinkedHashSet<>();       // 完成人数
    }

    public void generate(Date fromDate, Date endDate) {
        logger.info(" fetch english homework history report");
        SchoolYear schoolYear = SchoolYear.newInstance(fromDate);
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();

        // 获取作业数据，包括作业+测验
//        List<HomeworkReport> homeworkReports = getHomeworkReports(fromDate, endDate);
        List<HomeworkQuiz> homeworkQuizs = getHomeworkQuizsByCheckTime(fromDate, endDate);

        // 获得所有的class id
        List<Long> classIds = homeworkQuizs.stream().map(HomeworkQuiz::getClazzId).collect(Collectors.toList());

        // 获得所有school id
        Map<Long, Long> clazzIdSchoolIdMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzsIncludeDisabled(classIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Clazz::getSchoolId));
        Set<Long> schoolIds = clazzIdSchoolIdMap.values().stream().collect(Collectors.toSet());
        logger.info(" school id size: " + schoolIds.size());
        // 获得完成作业的学生id


        // 获得过去所有学校行为数据
        List<String> ids = schoolIds.stream().map(Object::toString).collect(Collectors.toList());
        Map<Long, AbstractRSSchoolHomeworkBehaviorStat> schoolIdBehaviorMap = getOldSchoolBehaviorStats(ids, year, term);

        // 初始化行为数据记录的map
        Map<Long, Set<Long>> updateStudentIdMap = new HashMap<>();  // 需要更新的school id => student id set map
        Map<Long, Set<Long>> updateTeacherIdMap = new HashMap<>();  // 需要更新的school id => teacher id set map
        Map<Long, Long> updateStudentTimes = new HashMap<>();       // 需要更新的人次school id => student times
        Map<Long, Long> updateTeacherTimes = new HashMap<>();       // 需要更新的人次school id => teacher times
        Set<Long> areaCodes = new HashSet<>();

        // 统计时间内的行为数据
        calculateBehaviorData(homeworkQuizs, clazzIdSchoolIdMap, updateStudentIdMap, updateTeacherIdMap, updateStudentTimes, updateTeacherTimes);

        // 更新校级行为数据
        for (Long schoolId : schoolIds) {
            logger.info(" update behavior data for school " + schoolId);

            Long areaCode;

            // 更新学校数据
            if (schoolIdBehaviorMap.containsKey(schoolId)) {
                AbstractRSSchoolHomeworkBehaviorStat rsHomeworkBehaviorStat = schoolIdBehaviorMap.get(schoolId);
                updateSchoolBehaviorDataInDB(
                        rsHomeworkBehaviorStat.getSchoolId(), updateStudentIdMap.get(schoolId),
                        updateTeacherIdMap.get(schoolId), updateStudentTimes.get(schoolId), updateTeacherTimes.get(schoolId), year, term);
                areaCode = rsHomeworkBehaviorStat.getAcode();
            } else {
                School school = raikouSystem.loadSchool(schoolId);
                if (school == null) {
                    logger.warn("No school info found for school " + schoolId);
                    continue;
                }
                ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
                if (exRegion == null) {
                    logger.warn("No region info found for school " + schoolId);
                    continue;
                }

                AbstractRSSchoolHomeworkBehaviorStat rsHomeworkBehaviorStat = newSchoolBehaviorStat();
                fillSchoolBehaviorData(rsHomeworkBehaviorStat, updateStudentIdMap, updateTeacherIdMap, updateStudentTimes, updateTeacherTimes, school, exRegion, year, term);

                saveBehaviorDataInDB(rsHomeworkBehaviorStat);

                areaCode = (long) exRegion.getCountyCode();
            }

            // 计算出区级数据的更新信息
            areaCodes.add(areaCode);
        }

        // 获得过去需要更新的区级行为数据
        ids = new ArrayList<>();
        for (Long id : areaCodes) {
            ids.add(id.toString());
        }
        Map<Long, AbstractRSAreaHomeworkBehaviorStat> areaCodeBehaviorMap = getOldAreaBehaviorStats(ids, year, term);

        // 更新区级数据
        logger.info(" update behavior data for county");
        for (Long areaCode : areaCodes) {
            logger.info(" update behavior data for county " + areaCode);

            List<School> schools = raikouSystem.loadSchools(schoolLoaderClient.getSchoolLoader()
                    .querySchoolLocations(areaCode.intValue())
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabled())
                    .map(School.Location::getId)
                    .collect(Collectors.toSet()))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(School::getId))
                    .collect(Collectors.toList());

            Set<String> areaSchoolIds = new HashSet<>();
            for (School school : schools) {
                if (school == null) {
                    continue;
                }
                areaSchoolIds.add(school.getId().toString());
            }
            Map<Long, AbstractRSSchoolHomeworkBehaviorStat> schoolBehaviorStats = getOldSchoolBehaviorStats(areaSchoolIds, year, term);
            updateAreaBehaviorData(areaCodeBehaviorMap, areaCode, schoolBehaviorStats, year, term);
        }
    }


//    @Deprecated
//    abstract protected List<HomeworkReport> getHomeworkReports(Date fromDate, Date endDate);

    abstract protected List<HomeworkQuiz> getHomeworkQuizsByCheckTime(Date fromDate, Date endDate);

    abstract protected Map<Long, AbstractRSSchoolHomeworkBehaviorStat> getOldSchoolBehaviorStats(Collection<String> schoolIds, Integer year, Term term);

    abstract protected Map<Long, AbstractRSAreaHomeworkBehaviorStat> getOldAreaBehaviorStats(Collection<String> areaCodes, Integer year, Term term);

    abstract protected AbstractRSSchoolHomeworkBehaviorStat newSchoolBehaviorStat();

    abstract protected AbstractRSAreaHomeworkBehaviorStat newAreaBehaviorStat();

    abstract protected void updateSchoolBehaviorDataInDB(String id, Collection<Long> studentIds, Collection<Long> teacherIds, Long studentTimes, Long teacherTimes, Integer year, Term term);

    abstract protected void saveBehaviorDataInDB(AbstractRSSchoolHomeworkBehaviorStat stat);

    abstract protected void saveBehaviorDataInDB(AbstractRSAreaHomeworkBehaviorStat stat);

//    protected Set<Long> getCompleteUid(IHomework homework, HomeworkType homeworkType) {
//        String month = MonthRange.newInstance(homework.getHomeworkCreateTime().getTime()).toString();
//
//        return homeworkResultLoaderClient.loadStudentHomeworkResults(month, homeworkType, homework.getHomeworkId())
//                .originalLocationsAsList()
//                .stream()
//                .filter(t -> t.getUserId() != 0)
//                .map(StudentHomeworkResult.Location::getUserId)
//                .collect(Collectors.toSet());
//    }
//
//    protected Set<Long> getCompleteUid(Quiz quiz, HomeworkType quizType) {
//        String month = MonthRange.newInstance(quiz.getCreateDatetime().getTime()).toString();
//        return quizResultLoaderClient.loadStudentQuizResults(month, quizType, quiz.getId())
//                .toList()
//                .stream()
//                .filter(e -> SafeConverter.toBoolean(e.getFinished()))
//                .map(StudentQuizResult::toLocation)
//                .filter(t -> t.getUserId() != 0)
//                .map(StudentQuizResult.Location::getUserId)
//                .collect(Collectors.toSet());
//    }

    protected Set<Long> getCompleteUid(NewHomework newHomework) {
        return newHomeworkResultLoaderClient.findByHomeworkForReport(newHomework)
                .values()
                .stream()
                .filter(NewHomeworkResult::isFinished)
                .map(NewHomeworkResult::getUserId)
                .collect(Collectors.toSet());
    }


    private void calculateBehaviorData(List<HomeworkQuiz> homeworkQuizs,
                                       Map<Long, Long> clazzIdSchoolIdMap,
                                       Map<Long, Set<Long>> updateStudentIdMap,
                                       Map<Long, Set<Long>> updateTeacherIdMap,
                                       Map<Long, Long> updateStudentTimes,
                                       Map<Long, Long> updateTeacherTimes) {
        for (HomeworkQuiz homeworkQuiz : homeworkQuizs) {
            AuthenticationState authenticationState = raikouSystem.loadUser(homeworkQuiz.getTeacherId()).fetchCertificationState();
            if (authenticationState != AuthenticationState.SUCCESS)
                continue;
            Long schoolId = clazzIdSchoolIdMap.get(homeworkQuiz.getClazzId());

            Set<Long> updateStudentIdSet = updateStudentIdMap.get(schoolId);
            if (updateStudentIdSet == null) {
                updateStudentIdSet = new HashSet<>();
                updateStudentIdMap.put(schoolId, updateStudentIdSet);
            }
            updateStudentIdSet.addAll(homeworkQuiz.getCompleteUid());

            Set<Long> updateTeacherIdSet = updateTeacherIdMap.get(schoolId);
            if (updateTeacherIdSet == null) {
                updateTeacherIdSet = new HashSet<>();
                updateTeacherIdMap.put(schoolId, updateTeacherIdSet);
            }

            updateTeacherIdSet.add(homeworkQuiz.getTeacherId());

            if (updateStudentTimes.containsKey(schoolId)) {
                updateStudentTimes.put(schoolId, updateStudentTimes.get(schoolId) + homeworkQuiz.getCompleteUid().size());
            } else {
                updateStudentTimes.put(schoolId, (long) homeworkQuiz.getCompleteUid().size());
            }

            if (updateTeacherTimes.containsKey(schoolId)) {
                updateTeacherTimes.put(schoolId, updateTeacherTimes.get(schoolId) + 1);
            } else {
                updateTeacherTimes.put(schoolId, 1L);
            }
        }
    }

    private AbstractRSSchoolHomeworkBehaviorStat fillSchoolBehaviorData(AbstractRSSchoolHomeworkBehaviorStat rsHomeworkBehaviorStat,
                                                                        Map<Long, Set<Long>> updateStudentIdMap,
                                                                        Map<Long, Set<Long>> updateTeacherIdMap,
                                                                        Map<Long, Long> updateStudentTimes,
                                                                        Map<Long, Long> updateTeacherTimes,
                                                                        School school, ExRegion exRegion, Integer year, Term term) {
        Long schoolId = school.getId();
        rsHomeworkBehaviorStat.setId(AbstractRSSchoolHomeworkBehaviorStat.generateId(year, term, schoolId.toString()));
        rsHomeworkBehaviorStat.setSchoolId(schoolId.toString());
        rsHomeworkBehaviorStat.setSchoolName(school.getCname());
        rsHomeworkBehaviorStat.setAcode((long) exRegion.getCountyCode());
        rsHomeworkBehaviorStat.setAreaName(exRegion.getCountyName());
        rsHomeworkBehaviorStat.setCcode((long) exRegion.getCityCode());
        rsHomeworkBehaviorStat.setCityName(exRegion.getCityName());
        rsHomeworkBehaviorStat.setPcode((long) exRegion.getProvinceCode());
        rsHomeworkBehaviorStat.setProvinceName(exRegion.getProvinceName());
        if (updateStudentIdMap.get(schoolId) != null)
            rsHomeworkBehaviorStat.setStuNum(updateStudentIdMap.get(schoolId).size());
        else
            rsHomeworkBehaviorStat.setStuNum(0);
        if (updateTeacherIdMap.get(schoolId) != null)
            rsHomeworkBehaviorStat.setTeacherNum(updateTeacherIdMap.get(schoolId).size());
        else
            rsHomeworkBehaviorStat.setTeacherNum(0);
        if (updateStudentTimes.get(schoolId) != null)
            rsHomeworkBehaviorStat.setStuTimes(updateStudentTimes.get(schoolId));
        else
            rsHomeworkBehaviorStat.setStuTimes(0L);
        if (updateTeacherTimes.get(schoolId) != null)
            rsHomeworkBehaviorStat.setTeacherTimes(updateTeacherTimes.get(schoolId));
        else
            rsHomeworkBehaviorStat.setTeacherTimes(0L);
        if (updateStudentIdMap.get(schoolId) != null)
            rsHomeworkBehaviorStat.setStuIds(updateStudentIdMap.get(schoolId));
        else
            rsHomeworkBehaviorStat.setStuIds(new HashSet<>());
        if (updateTeacherIdMap.get(schoolId) != null)
            rsHomeworkBehaviorStat.setTeacherIds(updateTeacherIdMap.get(schoolId));
        else
            rsHomeworkBehaviorStat.setTeacherIds(new HashSet<>());

        return rsHomeworkBehaviorStat;
    }

    private void updateAreaBehaviorData(Map<Long, AbstractRSAreaHomeworkBehaviorStat> areaCodeBehaviorMap, Long areaCode, Map<Long, AbstractRSSchoolHomeworkBehaviorStat> schoolBehaviorStats, Integer year, Term term) {
        AbstractRSAreaHomeworkBehaviorStat areaHomeworkBehaviorStat = areaCodeBehaviorMap.get(areaCode);
        if (areaHomeworkBehaviorStat == null) {
            ExRegion exRegion = raikouSystem.loadRegion(areaCode.intValue());
            areaHomeworkBehaviorStat = newAreaBehaviorStat();
            areaHomeworkBehaviorStat.setId(AbstractRSAreaHomeworkBehaviorStat.generateId(year, term, areaCode.toString()));
            areaHomeworkBehaviorStat.setAcode(String.valueOf(exRegion.getCountyCode()));
            areaHomeworkBehaviorStat.setAreaName(exRegion.getCountyName());
            areaHomeworkBehaviorStat.setCcode((long) exRegion.getCityCode());
            areaHomeworkBehaviorStat.setCityName(exRegion.getCityName());
            areaHomeworkBehaviorStat.setPcode((long) exRegion.getProvinceCode());
            areaHomeworkBehaviorStat.setProvinceName(exRegion.getProvinceName());
        }
        areaHomeworkBehaviorStat.setStuNum(0);
        areaHomeworkBehaviorStat.setTeacherNum(0);
        areaHomeworkBehaviorStat.setStuTimes(0L);
        areaHomeworkBehaviorStat.setTeacherTimes(0L);
        areaHomeworkBehaviorStat.setSchoolNum(0);
        for (AbstractRSSchoolHomeworkBehaviorStat stat : schoolBehaviorStats.values()) {
            areaHomeworkBehaviorStat.setStuNum(areaHomeworkBehaviorStat.getStuNum() + stat.getStuNum());
            areaHomeworkBehaviorStat.setTeacherNum(areaHomeworkBehaviorStat.getTeacherNum() + stat.getTeacherNum());
            areaHomeworkBehaviorStat.setStuTimes(areaHomeworkBehaviorStat.getStuTimes() + stat.getStuTimes());
            areaHomeworkBehaviorStat.setTeacherTimes(areaHomeworkBehaviorStat.getTeacherTimes() + stat.getTeacherTimes());
            areaHomeworkBehaviorStat.setSchoolNum(areaHomeworkBehaviorStat.getSchoolNum() + 1);
        }
        saveBehaviorDataInDB(areaHomeworkBehaviorStat);
    }
}
