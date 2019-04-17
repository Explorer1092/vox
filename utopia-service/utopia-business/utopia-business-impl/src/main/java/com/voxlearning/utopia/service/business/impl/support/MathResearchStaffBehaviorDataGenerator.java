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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.business.api.entity.RSMathAreaHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.RSMathSchoolHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSAreaHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSSchoolHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.impl.dao.RSMathAreaHomeworkBehaviorStatDao;
import com.voxlearning.utopia.service.business.impl.dao.RSMathSchoolHomeworkBehaviorStatDao;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkCrmLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author changyuan.liu
 * @since 2015/3/27
 */
@Named
public class MathResearchStaffBehaviorDataGenerator extends AbstractResearchStaffBehaviorDataGenerator {

    @Inject private RSMathSchoolHomeworkBehaviorStatDao rsMathSchoolHomeworkBehaviorStatDao;
    @Inject private RSMathAreaHomeworkBehaviorStatDao rsMathAreaHomeworkBehaviorStatDao;
    @Inject protected NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject protected NewHomeworkCrmLoaderClient newHomeworkCrmLoaderClient;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;

    public void clearData(Integer year, Term term) {
        rsMathAreaHomeworkBehaviorStatDao.clearData(year, term);
        rsMathSchoolHomeworkBehaviorStatDao.clearData(year, term);
    }

    @Override
    protected List<HomeworkQuiz> getHomeworkQuizsByCheckTime(Date fromDate, Date endDate) {
        List<HomeworkQuiz> result = new LinkedList<>();

        logger.info(" fetch Math newhomeworks");
        Collection<String> ids = newHomeworkCrmLoaderClient.findIdsByCheckedTimes(fromDate, endDate);
        List<NewHomework> homeworks = newHomeworkLoaderClient.loadNewHomeworks(ids)
                .values()
                .stream()
                .filter(h-> Subject.MATH.equals(h.getSubject()))
                .collect(Collectors.toList());

//        Collection<Long> ids = homeworkManagementClient.queryMathHomeworkIdsByCheckedTimes(fromDate, endDate);
//        List<MathHomework> homeworks = homeworkLoaderClient.loadMathHomeworksIncludeDisabled(ids)
//                .values()
//                .stream()
//                .collect(Collectors.toList());
        Set<Long> groupClazzIds = homeworks.stream().map(NewHomework::getClazzGroupId).collect(Collectors.toSet());
        Map<Long, GroupMapper> clazzGroupMap = groupLoaderClient.loadGroups(groupClazzIds, false);
        homeworks.forEach(
                e -> result.add(new HomeworkQuiz(clazzGroupMap.get(e.getClazzGroupId()).getClazzId(), e.getTeacherId(), getCompleteUid(e)))
        );
        logger.info("Math newhomework size: " + homeworks.size());

//        logger.info(" fetch quizs");
//        List<Quiz> quizs = homeworkManagementClient.findQuizsWithCheckedTimeInRangeWithSubject(fromDate, endDate, Subject.MATH, QuizCategory.NORMAL_QUIZ);
//        quizs.forEach(e -> result.add(new HomeworkQuiz(e.getClazzId(), e.getTeacherId(), getCompleteUid(e, HomeworkType.QUIZ_MATH))));
//        logger.info(" quiz size: " + quizs.size());

        return result;
    }

    @Override
    protected Map<Long, AbstractRSSchoolHomeworkBehaviorStat> getOldSchoolBehaviorStats(Collection<String> schoolIds, Integer year, Term term) {
        List<RSMathSchoolHomeworkBehaviorStat> rsHomeworkBehaviorStats = rsMathSchoolHomeworkBehaviorStatDao.findBySchoolIds_withoutIdSet(schoolIds, year, term);
        Map<Long, AbstractRSSchoolHomeworkBehaviorStat> schoolIdBehaviorMap = new HashMap<>();
        for (RSMathSchoolHomeworkBehaviorStat stat : rsHomeworkBehaviorStats) {
            schoolIdBehaviorMap.put(Long.valueOf(stat.getSchoolId()), stat);
        }
        return schoolIdBehaviorMap;
    }

    @Override
    protected Map<Long, AbstractRSAreaHomeworkBehaviorStat> getOldAreaBehaviorStats(Collection<String> areaCodes, Integer year, Term term) {
        List<RSMathAreaHomeworkBehaviorStat> areaBehaviorStats = rsMathAreaHomeworkBehaviorStatDao.findByAreaCodes(areaCodes, year, term);
        Map<Long, AbstractRSAreaHomeworkBehaviorStat> areaCodeBehaviorMap = new HashMap<>();
        for (RSMathAreaHomeworkBehaviorStat stat : areaBehaviorStats) {
            areaCodeBehaviorMap.put(Long.valueOf(stat.getAcode()), stat);
        }
        return areaCodeBehaviorMap;
    }

    @Override
    protected AbstractRSSchoolHomeworkBehaviorStat newSchoolBehaviorStat() {
        return new RSMathSchoolHomeworkBehaviorStat();
    }

    @Override
    protected AbstractRSAreaHomeworkBehaviorStat newAreaBehaviorStat() {
        return new RSMathAreaHomeworkBehaviorStat();
    }

    @Override
    protected void updateSchoolBehaviorDataInDB(String schoolId, Collection<Long> studentIds, Collection<Long> teacherIds, Long studentTimes, Long teacherTimes, Integer year, Term term) {
        rsMathSchoolHomeworkBehaviorStatDao.updateStudentAndTeacherDataById(schoolId, studentIds, teacherIds, studentTimes, teacherTimes, year, term);
    }

    @Override
    protected void saveBehaviorDataInDB(AbstractRSSchoolHomeworkBehaviorStat stat) {
        if (CollectionUtils.isEmpty(stat.getStuIds()) && CollectionUtils.isEmpty(stat.getTeacherIds())) {// 没有老师或学生就不存了
            return;
        }
        if (rsMathSchoolHomeworkBehaviorStatDao.load(stat.getId()) == null) {
            rsMathSchoolHomeworkBehaviorStatDao.insert((RSMathSchoolHomeworkBehaviorStat) stat);
        } else {
            rsMathSchoolHomeworkBehaviorStatDao.update(stat.getId(), (RSMathSchoolHomeworkBehaviorStat) stat);
        }
    }

    @Override
    protected void saveBehaviorDataInDB(AbstractRSAreaHomeworkBehaviorStat stat) {
        if (rsMathAreaHomeworkBehaviorStatDao.load(stat.getId()) == null) {
            rsMathAreaHomeworkBehaviorStatDao.insert((RSMathAreaHomeworkBehaviorStat) stat);
        } else {
            rsMathAreaHomeworkBehaviorStatDao.update(stat.getId(), (RSMathAreaHomeworkBehaviorStat) stat);
        }
    }
}
