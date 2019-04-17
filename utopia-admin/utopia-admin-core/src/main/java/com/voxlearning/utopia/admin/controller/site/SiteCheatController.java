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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherService;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingHomework;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author RuiBao
 * @version 0.1
 * @since 6/18/2015
 */
@Controller
@RequestMapping("/site/cheat")
public class SiteCheatController extends SiteAbstractController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Resource protected CrmTeacherService crmTeacherService;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    String firstGradeCommentList(Model model) {
        int pageNumber = getRequestInt("pageNumber", 1);
        PageRequest pageable = new PageRequest(pageNumber - 1, 20);
        String start = getRequestString("startDate");
        String end = getRequestString("endDate");
        DateRange range;
        if (StringUtils.isBlank(start) || StringUtils.isBlank(end)) {
            range = new DateRange(DayRange.current().getStartDate(), DayRange.current().getEndDate());
        } else {
            range = new DateRange(DateUtils.stringToDate(start, DateUtils.FORMAT_SQL_DATE), DateUtils.stringToDate(end, DateUtils.FORMAT_SQL_DATE));
        }

        Page<PossibleCheatingHomework> pchp = crmTeacherService.pageGetByDateRange(range, pageable);
        List<PossibleCheatingHomework> pchs = pchp.getContent();
        Set<Long> teacherIds = pchs.stream().map(PossibleCheatingHomework::getTeacherId).collect(Collectors.toSet());
        Set<Long> clazzIds = pchs.stream().map(PossibleCheatingHomework::getClazzId).collect(Collectors.toSet());
        Map<Long, User> teachers = userLoaderClient.loadUsers(teacherIds);
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Set<Long> schoolIds = clazzs.values().stream().map(Clazz::getSchoolId).collect(Collectors.toSet());
        Map<Long, School> schools = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();

        List<Map<String, Object>> content = new ArrayList<>();
        for (PossibleCheatingHomework pch : pchp.getContent()) {
            Map<String, Object> map = new HashMap<>();
            map.put("teacherId", pch.getTeacherId());
            map.put("teacherName", teachers.get(pch.getTeacherId()) == null ? "" : teachers.get(pch.getTeacherId()).fetchRealname());
            map.put("clazzId", pch.getClazzId());
            map.put("ClazzName", clazzs.get(pch.getClazzId()) == null ? "" : clazzs.get(pch.getClazzId()).formalizeClazzName());
            map.put("homeworkId", pch.getHomeworkId());
            map.put("homeworkSubject", pch.getHomeworkType().name());
            map.put("schoolName", clazzs.get(pch.getClazzId()) == null ? "" : schools.get(clazzs.get(pch.getClazzId()).getSchoolId()) == null ? "" : schools.get(clazzs.get(pch.getClazzId()).getSchoolId()).getCname());
            map.put("reason", pch.getReason());
            map.put("freeze", !Boolean.TRUE.equals(pch.getRecordOnly()));
            map.put("date", DateUtils.dateToString(pch.getCreateDatetime(), DateUtils.FORMAT_SQL_DATE));
            content.add(map);
        }
        Page<Map<String, Object>> commentPage = new PageImpl<>(content, pageable, pchp.getTotalElements());
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "site/cheat/index";
    }
}
