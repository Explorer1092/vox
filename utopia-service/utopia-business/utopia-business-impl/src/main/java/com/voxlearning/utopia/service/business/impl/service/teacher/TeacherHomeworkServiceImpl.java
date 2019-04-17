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

package com.voxlearning.utopia.service.business.impl.service.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.mapper.HomeworkMapper;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkState;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Slf4j
public class TeacherHomeworkServiceImpl extends BusinessServiceSpringBean {

    @Inject private AmbassadorServiceClient ambassadorServiceClient;


    /**
     * 根据原有的getHomeworkMapperList方法重构
     * 修改地方：
     * 1. 只取未检查作业
     * 2. 使用HomeworkService中的validateHomeworkAssignable方法来
     *
     * @param teacherId
     * @param subject
     * @return
     * @author changyuan.liu
     */
    public List<HomeworkMapper> getClazzGroupHomeworkMappers(Long teacherId, Subject subject) {
        if (teacherId == null || subject == null) {
            return Collections.emptyList();
        }

        // 获得老师下面的有效班级
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId).stream()
                .filter(c -> !c.isTerminalClazz())
                .collect(Collectors.toList());
        // 班级id集合
        Set<Long> clazzIds = clazzs.stream().map(Clazz::getId).collect(Collectors.toSet());
        // 班级id=>clazz map
        Map<Long, Clazz> clazzMap = clazzs.stream().collect(Collectors.toMap(LongIdEntity::getId, c -> c));
        // 获得自建班级老师分组信息
        Map<Long, GroupMapper> teacherGroupMap = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacherId, clazzIds, false);
        // 分组id=>group mapper map
        Map<Long, GroupMapper> groupMap = teacherGroupMap.values().stream().collect(Collectors.toMap(GroupMapper::getId, g -> g));
        // 班级/分组学生关系
        List<Long> groupIds = new ArrayList<>(groupMap.keySet());
        Map<Long, List<Long>> groupStuIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        // 读取作业
        Map<Long, NewHomework> homeworkMap = newHomeworkLoaderClient.loadGroupHomeworks(groupIds, subject)
                .unchecked()
                .toList((l1, l2) -> Long.compare(l2.getCreateAt().getTime(), l1.getCreateAt().getTime()))
                .stream()
                .collect(Collectors.toMap(NewHomework::getClazzGroupId, h -> h, (h1, h2) -> h1));
        // 当前时间
        Date nowDate = new Date();

        List<HomeworkMapper> result = new LinkedList<>();
        for (GroupMapper group : teacherGroupMap.values()) {
            Long clazzId = group.getClazzId();
            Long groupId = group.getId();

            Clazz clazz = clazzMap.get(clazzId);
            if (clazz == null) {
                continue;
            }

            HomeworkMapper mapper = HomeworkMapper.bulidNeonatalMapper();
            result.add(mapper);

            mapper.setClazzId(clazzId);
            mapper.setClazzName(clazz.formalizeClazzName());
            mapper.setClazzLevel(String.valueOf(clazz.getClazzLevel().getLevel()));
            mapper.setGroupId(groupId);
            mapper.setGroupName(group.getGroupName());
            mapper.setStudentCount(groupStuIds.get(groupId) != null ? groupStuIds.get(groupId).size() : 0);

            int studentCount = mapper.getStudentCount();
            if (studentCount == 0) {
                mapper.setState(HomeworkState.NOT_APPLICABLE_ASSIGN_HOMEWORK);
                mapper.setMessage("学生数量为0，需先添加学生后再布置作业");
                mapper.setCanAssign(false);
                continue;
            }

            if (conversionService.convert(clazz.getClassLevel(), Integer.class) > 6) {
                mapper.setState(HomeworkState.NOT_APPLICABLE_ASSIGN_HOMEWORK);
                mapper.setMessage("班级年级大于6");
                mapper.setCanAssign(false);
                continue;
            }


            if (homeworkMap != null && homeworkMap.containsKey(groupId)) {
                NewHomework homework = homeworkMap.get(groupId);
                // 当存在未检查作业时，设置作业相关信息
                mapper.setHomeWorkId(String.valueOf(homework.getId()));
                mapper.setCanAssign(false);
                mapper.setStartDate(DateUtils.dateToString(homework.getStartTime(), "yyyy年M月d日"));
                mapper.setEndDate(DateUtils.dateToString(homework.getEndTime(), "yyyy年M月d日 HH:mm"));
                mapper.setCreateDatetime(homework.getCreateAt());
                mapper.setNormalFinishTime(SafeConverter.toLong(homework.getDuration()));
                mapper.setPastdue(nowDate.after(homework.getEndTime()));
                mapper.setEmptyContentJson(StringUtils.isBlank(homework.getRemark()));

                NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(homework.toLocation());
                int finishSize = accomplishment == null ? 0 : accomplishment.size();
                mapper.setFinishCount(finishSize);
                mapper.setUnfinishCount((studentCount - finishSize < 0 ? 0 : (studentCount - finishSize)));
                mapper.setFinishPercent(new BigDecimal(finishSize).multiply(new BigDecimal(100)).divide(new BigDecimal(studentCount), 0, BigDecimal.ROUND_HALF_UP).intValue());
                mapper.setPracticeCount(homework.getPractices().size());
                mapper.setExamPaperQuestionCount(0);
                mapper.setHomeWorkName(homework.getTitle());
                mapper.setOldHomework(DateUtils.stringToDate("2014-09-01 00:00:00").after(homework.getCreateAt()));
                if ((DateUtils.dateToString(nowDate, DateUtils.FORMAT_SQL_DATE).equals(DateUtils.dateToString(homework.getEndTime(), DateUtils.FORMAT_SQL_DATE)) || nowDate.after(homework.getEndTime())) && !homework.isHomeworkChecked()) {
                    mapper.setState(HomeworkState.CHECK_HOMEWORK);
                } else if (mapper.getUnfinishCount() == 0) {
                    mapper.setState(HomeworkState.CHECK_HOMEWORK);
                } else {
                    mapper.setState(HomeworkState.ADJUST_DELETE_HOMEWORK);
                }
            }
        }

        // 按班级名称排序
        result = result.stream().sorted((o1, o2) -> {
            int ret = Integer.compare(SafeConverter.toInt(o1.getClazzLevel()), SafeConverter.toInt(o2.getClazzLevel()));
            if (ret != 0) {
                return ret;
            }
            try {
                int ind1 = o1.getClazzName().lastIndexOf("班");
                int ind2 = o2.getClazzName().lastIndexOf("班");
                if (ind1 != -1 && ind2 != -1) {
                    String c1 = o1.getClazzName().substring(0, ind1);
                    String c2 = o2.getClazzName().substring(0, ind2);
                    return Integer.compare(SafeConverter.toInt(c1), SafeConverter.toInt(c2));
                } else if (ind1 == -1) {
                    return 1;
                } else {
                    return -1;
                }
            } catch (StringIndexOutOfBoundsException ex) {
                return ret;
            }
        }).collect(Collectors.toList());

        return result;
    }

}
