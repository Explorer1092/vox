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
import com.voxlearning.utopia.service.business.api.entity.RSEnglishAreaHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.RSEnglishSchoolHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSAreaHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSSchoolHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.impl.dao.RSEnglishAreaHomeworkBehaviorStatDao;
import com.voxlearning.utopia.service.business.impl.dao.RSEnglishSchoolHomeworkBehaviorStatDao;
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
 * @author Administrator
 * @since 2015/3/27
 */
@Named
public class EnglishResearchStaffBehaviorDataGenerator extends AbstractResearchStaffBehaviorDataGenerator {

    @Inject private RSEnglishSchoolHomeworkBehaviorStatDao rsEnglishSchoolHomeworkBehaviorStatDao;
    @Inject private RSEnglishAreaHomeworkBehaviorStatDao rsEnglishAreaHomeworkBehaviorStatDao;
    @Inject protected NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject protected NewHomeworkCrmLoaderClient newHomeworkCrmLoaderClient;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;

    public void clearData(Integer year, Term term) {
        rsEnglishAreaHomeworkBehaviorStatDao.clearData(year, term);
        rsEnglishSchoolHomeworkBehaviorStatDao.clearData(year, term);
    }

    @Override
    protected List<HomeworkQuiz> getHomeworkQuizsByCheckTime(Date fromDate, Date endDate) {
        List<HomeworkQuiz> result = new LinkedList<>();

        logger.info(" fetch Math newhomeworks");
        Collection<String> ids = newHomeworkCrmLoaderClient.findIdsByCheckedTimes(fromDate, endDate);
        List<NewHomework> homeworks = newHomeworkLoaderClient.loadNewHomeworks(ids)
                .values()
                .stream()
                .filter(h-> Subject.ENGLISH.equals(h.getSubject()))
                .collect(Collectors.toList());

        Set<Long> groupClazzIds = homeworks.stream().map(NewHomework::getClazzGroupId).collect(Collectors.toSet());
        Map<Long, GroupMapper> clazzGroupMap = groupLoaderClient.loadGroups(groupClazzIds, false);
        homeworks.forEach(
                e -> result.add(new HomeworkQuiz(clazzGroupMap.get(e.getClazzGroupId()).getClazzId(), e.getTeacherId(), getCompleteUid(e)))
        );
        logger.info("Math newhomework size: " + homeworks.size());

        return result;
    }

    @Override
    protected Map<Long, AbstractRSSchoolHomeworkBehaviorStat> getOldSchoolBehaviorStats(Collection<String> schoolIds, Integer year, Term term) {
        List<RSEnglishSchoolHomeworkBehaviorStat> rsEngSchoolHomeworkBehaviorStats = rsEnglishSchoolHomeworkBehaviorStatDao.findBySchoolIds_withoutIdSet(schoolIds, year, term);
        Map<Long, AbstractRSSchoolHomeworkBehaviorStat> schoolIdBehaviorMap = new HashMap<>();
        for (RSEnglishSchoolHomeworkBehaviorStat stat : rsEngSchoolHomeworkBehaviorStats) {
            schoolIdBehaviorMap.put(Long.valueOf(stat.getSchoolId()), stat);
        }
        return schoolIdBehaviorMap;
    }

    @Override
    protected Map<Long, AbstractRSAreaHomeworkBehaviorStat> getOldAreaBehaviorStats(Collection<String> areaCodes, Integer year, Term term) {
        List<RSEnglishAreaHomeworkBehaviorStat> areaBehaviorStats = rsEnglishAreaHomeworkBehaviorStatDao.findByAreaCodes(areaCodes, year, term);
        Map<Long, AbstractRSAreaHomeworkBehaviorStat> areaCodeBehaviorMap = new HashMap<>();
        for (RSEnglishAreaHomeworkBehaviorStat stat : areaBehaviorStats) {
            areaCodeBehaviorMap.put(Long.valueOf(stat.getAcode()), stat);
        }
        return areaCodeBehaviorMap;
    }

    @Override
    protected AbstractRSSchoolHomeworkBehaviorStat newSchoolBehaviorStat() {
        return new RSEnglishSchoolHomeworkBehaviorStat();
    }

    @Override
    protected AbstractRSAreaHomeworkBehaviorStat newAreaBehaviorStat() {
        return new RSEnglishAreaHomeworkBehaviorStat();
    }

    @Override
    protected void updateSchoolBehaviorDataInDB(String schoolId, Collection<Long> studentIds, Collection<Long> teacherIds, Long studentTimes, Long teacherTimes, Integer year, Term term) {
        rsEnglishSchoolHomeworkBehaviorStatDao.updateStudentAndTeacherDataById(
                schoolId, studentIds, teacherIds, studentTimes, teacherTimes, year, term);
    }

    @Override
    protected void saveBehaviorDataInDB(AbstractRSSchoolHomeworkBehaviorStat stat) {
        if (CollectionUtils.isEmpty(stat.getStuIds()) && CollectionUtils.isEmpty(stat.getTeacherIds())) {// 没有老师或学生就不存了
            return;
        }
        if (rsEnglishSchoolHomeworkBehaviorStatDao.load(stat.getId()) == null) {
            rsEnglishSchoolHomeworkBehaviorStatDao.insert((RSEnglishSchoolHomeworkBehaviorStat) stat);
        } else {
            rsEnglishSchoolHomeworkBehaviorStatDao.update(stat.getId(), (RSEnglishSchoolHomeworkBehaviorStat) stat);
        }
    }

    @Override
    protected void saveBehaviorDataInDB(AbstractRSAreaHomeworkBehaviorStat stat) {
        if (rsEnglishAreaHomeworkBehaviorStatDao.load(stat.getId()) == null) {
            rsEnglishAreaHomeworkBehaviorStatDao.insert((RSEnglishAreaHomeworkBehaviorStat) stat);
        } else {
            rsEnglishAreaHomeworkBehaviorStatDao.update(stat.getId(), (RSEnglishAreaHomeworkBehaviorStat) stat);
        }
    }
}
