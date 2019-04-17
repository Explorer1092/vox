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

package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkCacheLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkCacheMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.*;

/**
 * 学生App假期作业卡
 * Created by Shuai Huan on 2016/1/20.
 */
@Named
public class LoadStudentAppVacationHomeworkCard extends AbstractStudentAppIndexDataLoader {
    @ImportService(interfaceClass = VacationHomeworkCacheLoader.class)
    private VacationHomeworkCacheLoader vacationHomeworkCacheLoader;

    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {
        Date currentDate = new Date();
        if (currentDate.after(NewHomeworkConstants.VH_END_DATE_LATEST)) return context;
        List<Long> groupIds = context.__studentGroups.stream().map(GroupMapper::getId).collect(Collectors.toList());
        Map<Long, List<VacationHomeworkPackage.Location>> groupVacationHomeworkPackage = vacationHomeworkLoaderClient.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
        List<VacationHomeworkPackage.Location> vacationHomeworks = new ArrayList<>();
        for (List<VacationHomeworkPackage.Location> locations : groupVacationHomeworkPackage.values()) {
            vacationHomeworks.addAll(locations);
        }
        vacationHomeworks = vacationHomeworks.stream()
                .filter(v -> Objects.nonNull(v.getSubject()))
                .sorted(Comparator.comparingInt(v -> v.getSubject().getKey()))
                .collect(Collectors.toList());
        for (VacationHomeworkPackage.Location location : vacationHomeworks) {
            HomeworkType homeworkType = VACATION_ENGLISH;
            String desc = "英语假期作业";
            if (Subject.MATH.equals(location.getSubject())) {
                homeworkType = VACATION_MATH;
                desc = "数学假期作业";
            } else if (Subject.CHINESE.equals(location.getSubject())) {
                homeworkType = VACATION_CHINESE;
                desc = "语文假期作业";
            }
            boolean started = currentDate.getTime() > location.getStartTime();
            if ((RuntimeMode.isTest() || started
                    || grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(context.getStudent(), "VacationHW", "WhiteList"))) {
                VacationHomeworkCacheMapper vacationHomeworkCacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkCacheMapper(location.getClazzGroupId(), context.getStudent().getId());
                boolean finished = vacationHomeworkCacheMapper != null && vacationHomeworkCacheMapper.isFinished();

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("homeworkId", location.getId()); // 将packageId传入
                Date endDate = new Date(location.getEndTime());
                resultMap.put("endDate", endDate);
                String endDateStr = "结束时间：" + DateUtils.dateToString(endDate, "MM-dd HH:mm");
                if (currentDate.after(endDate)) {
                    endDateStr = "补做截止：" + DateUtils.dateToString(NewHomeworkConstants.VH_END_DATE_LATEST, "MM-dd HH:mm");
                }
                // iOS老版本endData字段可以为字符串
                if (StringUtils.equalsIgnoreCase("ios", context.sys) && VersionUtil.compareVersion(context.ver, "2.9.4") < 0) {
                    resultMap.put("endDate", endDateStr);
                }
                resultMap.put("endDateStr", endDateStr);
                resultMap.put("homeworkType", homeworkType);
                resultMap.put("desc", desc);
                resultMap.put("makeup", false);
                resultMap.put("subject", location.getSubject().name());
                resultMap.put("types", Collections.singletonList("VACATION"));
                resultMap.put("startComment", finished ? "已完成" : "开始作业");
                context.__homeworkCards.add(resultMap);
            }
        }
        return context;
    }
}
