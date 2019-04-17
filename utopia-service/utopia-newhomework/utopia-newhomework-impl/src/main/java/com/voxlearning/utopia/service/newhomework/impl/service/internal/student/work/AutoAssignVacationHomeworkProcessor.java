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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.impl.loader.VacationHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkContentServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.VacationHomeworkServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/1/20
 */
@Named
public class AutoAssignVacationHomeworkProcessor extends SpringContainerSupport {
    @Inject
    private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    private VacationHomeworkLoaderImpl vacationHomeworkLoader;
    @Inject
    private NewHomeworkContentServiceImpl newHomeworkContentService;
    @Inject
    private VacationHomeworkServiceImpl vacationHomeworkService;


    public MapMessage autoAssign(Teacher teacher) {
        if (teacher == null) {
            return MapMessage.errorMessage("老师不能为空");
        }
        Long teacherId = teacher.getId();
        if (!teacher.isPrimarySchool()) {
            return MapMessage.errorMessage(teacherId + "不是小学老师");
        }
        // 1.获取老师所教班级
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId()).stream()
                .filter(s -> !s.isTerminalClazz()).collect(Collectors.toList());
        if (clazzs.isEmpty()) {
            return MapMessage.errorMessage(teacherId + "暂无班级，请先创建班级");
        }

        // 2.获取可布置寒假作业的班级
        List<Map<String, Object>> canBeAssignedClazzList = loadCanBeAssignedClazzList(teacher, clazzs);
        if (CollectionUtils.isEmpty(canBeAssignedClazzList)) {
            return MapMessage.errorMessage(teacherId + "没有可布置的班级");
        }

        Map<String, Map<String, Object>> clazzBookMap = new LinkedHashMap<>();
        // 3.遍历可布置的班级，获取教材
        for (Map<String, Object> clazzListMap : canBeAssignedClazzList) {
            List<Map<String, Object>> clazzList = (List<Map<String, Object>>) clazzListMap.get("clazzs");
            if (CollectionUtils.isNotEmpty(clazzList)) {
                Map<Long, Long> clazzGroupMap = new LinkedHashMap<>();
                clazzList.stream()
                        .filter(MapUtils::isNotEmpty)
                        .forEach(clazzMap -> {
                            Long clazzId = SafeConverter.toLong(clazzMap.get("classId"));
                            Long groupId = SafeConverter.toLong(clazzMap.get("groupId"));
                            if (clazzId != 0 && groupId != 0) {
                                clazzGroupMap.put(clazzId, groupId);
                            }
                        });
                // 4.获取班级默认教材
                MapMessage clazzBookMessage = newHomeworkContentService.loadClazzBook(teacher, clazzGroupMap, true);
                if (clazzBookMessage.isSuccess()) {
                    Map<String, Object> bookMap = (Map<String, Object>) clazzBookMessage.get("clazzBook");
                    if (bookMap != null) {
                        String bookId = SafeConverter.toString(bookMap.get("bookId"));
                        if (StringUtils.isNotBlank(bookId)) {
                            // 5.判断默认教材下是否有假期作业内容
                            MapMessage weekPlanMessage = vacationHomeworkLoader.loadBookPlanInfo(bookId);
                            if (weekPlanMessage.isSuccess()) {
                                List<Map> weekPlans = (List<Map>) weekPlanMessage.get("weekPlans");
                                if (CollectionUtils.isNotEmpty(weekPlans)) {
                                    clazzGroupMap.forEach((clazzId, groupId) -> {
                                        String key = clazzId + "_" + groupId;
                                        clazzBookMap.put(key, MiscUtils.m("bookId", bookId, "subject", teacher.getSubject()));
                                    });
                                } else {
                                    // 获得错误教材的名字
                                    Map<String,Object> errBookMap = (Map<String,Object>)weekPlanMessage.get("book");
                                    String bookName = MapUtils.getString(errBookMap,"name");
                                    if(StringUtils.isEmpty(bookName)) {
                                        bookName = teacher.getSubject().getValue();
                                    }

                                    return MapMessage.errorMessage(bookName + "教材暂无假期作业内容，换一本教材试试吧");
                                }
                            } else {
                                return MapMessage.errorMessage(teacherId + "获取假期作业内容失败");
                            }
                        } else {
                            return MapMessage.errorMessage(teacherId + "没有找到默认教材");
                        }
                    } else {
                        return MapMessage.errorMessage(teacherId + "没有找到默认教材");
                    }
                } else {
                    return MapMessage.errorMessage(teacherId + "获取默认教材失败");
                }
            }
        }
        // 一键布置假期作业的默认起始时间
        Date nowDate = new Date();
        Date defaultStartTime = nowDate.after(NewHomeworkConstants.VH_START_DATE_DEFAULT) ? nowDate : NewHomeworkConstants.VH_START_DATE_DEFAULT;

        Map<String, Object> homeworkDataMap = MiscUtils.m(
                "startTime", defaultStartTime.getTime(),
                "endTime", NewHomeworkConstants.VH_END_DATE_LATEST.getTime(),
                "plannedDays", 40,
                "clazzBookMap", clazzBookMap);
        // 6.布置
        HomeworkSource source = HomeworkSource.newInstance(homeworkDataMap);
        return assign(teacher, source);
    }

    private MapMessage assign(Teacher teacher, HomeworkSource source) {
        try {
            // 注意：这里是没有原子锁
            return vacationHomeworkService.assignHomework(teacher, source, HomeworkSourceType.CRM);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("假期作业布置中，请不要重复布置!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception ex) {
            logger.error("failed to save vacation homework, teacher id {}, homework_data {}", teacher.getId(), source, ex);
        }
        return MapMessage.errorMessage("布置假期作业失败").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
    }

    private List<Map<String, Object>> loadCanBeAssignedClazzList(Teacher teacher, List<Clazz> clazzs) {
        // 可以布置假期作业的年级班级列表
        // 可布置假期作业班级列表
        List<ExClazz> canBeAssignedClazzList = vacationHomeworkLoader.findTeacherClazzsCanBeAssignedHomework(teacher);
        Map<Long, ExClazz> canBeAssignedClazzMap = canBeAssignedClazzList.stream()
                .collect(Collectors.toMap(ExClazz::getId, e -> e));
        // 将clazz信息组织好加到年级map中
        Map<Integer, List<Map<String, Object>>> batchClazzs = new LinkedHashMap<>();

        clazzs.forEach(clazz -> {
            Map<String, Object> clazzMap = new LinkedHashMap<>();
            clazzMap.put("classId", clazz.getId());
            clazzMap.put("className", clazz.getClassName());
            if (canBeAssignedClazzMap.containsKey(clazz.getId()) && canBeAssignedClazzMap.get(clazz.getId()) != null &&
                    CollectionUtils.isNotEmpty(canBeAssignedClazzMap.get(clazz.getId()).getCurTeacherArrangeableGroups())) {
                clazzMap.put("canBeAssigned", true);
                clazzMap.put("groupId", MiscUtils.firstElement(canBeAssignedClazzMap.get(clazz.getId()).getCurTeacherArrangeableGroups()).getId());
            } else {
                clazzMap.put("canBeAssigned", false);
            }
            int clazzLevel = clazz.getClazzLevel().getLevel();
            List<Map<String, Object>> clazzList = batchClazzs.computeIfAbsent(clazzLevel, k -> new ArrayList<>());
            clazzList.add(clazzMap);
        });

        // 生成各年级信息
        List<Map<String, Object>> batchClazzsList = new ArrayList<>();
        // 1~6年级
        for (int i = 1; i <= 6; i++) {
            List<Map<String, Object>> clazzList = batchClazzs.getOrDefault(i, Collections.emptyList());
            if (CollectionUtils.isNotEmpty(clazzList)) {
                Map<String, Object> batchClazzsMap = new LinkedHashMap<>();
                boolean canBeAssigned = clazzList.stream().anyMatch(c -> c.get("canBeAssigned").equals(true));
                if (canBeAssigned) {
                    batchClazzsMap.put("clazzs", clazzList);
                    batchClazzsMap.put("classLevel", i);
                    batchClazzsList.add(batchClazzsMap);
                }
            }
        }
        return batchClazzsList;
    }
}
