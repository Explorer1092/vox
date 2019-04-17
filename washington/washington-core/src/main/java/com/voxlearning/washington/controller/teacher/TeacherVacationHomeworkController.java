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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/11/30.
 */
@Controller
@RequestMapping("/teacher/vacation")
public class TeacherVacationHomeworkController extends AbstractTeacherController {

    /**
     * 点击布置假期作业，选择开始结束时间以及年级班级
     * @param model
     * @return
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String batchAssignVacationHomework_SelectDateAndClazz(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        Date currentDate = new Date();
        if (currentDate.getTime() >= NewHomeworkConstants.VH_START_DATE_LATEST.getTime()) {
            return "redirect:/teacher/index.vpage";
        }

        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId()).stream()
                .filter(s -> !s.isTerminalClazz()).collect(Collectors.toList());
        if (clazzs.isEmpty()) return "redirect:/teacher/showtip.vpage";
        model.addAttribute("subject", teacher.getSubject());
        //作为作业的开始时间,用于前端显示,格式为yyyy-MM-dd HH:mm:ss
        Date specialDate = NewHomeworkConstants.earliestVacationHomeworkStartDate(RuntimeMode.current());
        Date sDate = currentDate.getTime() > specialDate.getTime() ? currentDate : specialDate;
        model.addAttribute("earliestStartDateTime", DateUtils.dateToString(sDate));
        Date benchmarkingDate = NewHomeworkConstants.VH_START_DATE_DEFAULT;
        Date dsDate = currentDate.getTime() > benchmarkingDate.getTime() ? currentDate : benchmarkingDate;
        model.addAttribute("defaultStartDateTime", DateUtils.dateToString(dsDate));
        model.addAttribute("latestStartDateTime", DateUtils.dateToString(NewHomeworkConstants.VH_START_DATE_LATEST));

        model.addAttribute("earliestEndDateTime", DateUtils.dateToString(NewHomeworkConstants.VH_END_DATE_EARLIEST));
        model.addAttribute("defaultEndDateTime", DateUtils.dateToString(NewHomeworkConstants.VH_END_DATE_DEFAULT));
        model.addAttribute("latestEndDateTime", DateUtils.dateToString(NewHomeworkConstants.VH_END_DATE_LATEST));

        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        model.addAttribute("adValidaty", false);
        return "teacherv3/vacation/index";
    }

    /**
     * 获取可布置作业的年级+班级
     * @return
     */
    @RequestMapping(value = "vacationclazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getVacationClazzList() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference()
                .loadTeacherClazzs(teacher.getId())
                .stream()
                .filter(s -> !s.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        if (clazzs.isEmpty()) {
            return MapMessage.errorMessage("暂无班级，请先创建班级").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        // 可以布置假期作业的年级班级列表
        // 可布置假期作业班级列表
        List<ExClazz> canBeAssignedClazzList = vacationHomeworkLoaderClient.findTeacherClazzsCanBeAssignedHomework(teacher);
        Map<Long, ExClazz> canBeAssignedClazzMap = canBeAssignedClazzList.stream()
                .collect(Collectors.toMap(ExClazz::getId, e -> e));

        // 将clazz信息组织好加到年级map中
        Map<Integer, List<Map<String, Object>>> batchClazzs = new LinkedHashMap<>();

        for (Clazz clazz : clazzs) {
            int clazzLevel = clazz.getClazzLevel().getLevel();
//            // 过滤54制5年级班级
//            if (EduSystemType.P5 == clazz.getEduSystem() && clazzLevel == 5) {
//                continue;
//            }
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
            List<Map<String, Object>> clazzList = batchClazzs.computeIfAbsent(clazzLevel, k -> new ArrayList<>());
            clazzList.add(clazzMap);
        }

        // 生成各年级信息
        List<Map<String, Object>> batchClazzsList = new ArrayList<>();
        // 1~6年级
        for (int i = 1; i <= 6; i++) {
            List<Map<String, Object>> clazzList = batchClazzs.getOrDefault(i, Collections.emptyList());
            if (CollectionUtils.isNotEmpty(clazzList)) {
                Map<String, Object> batchClazzsMap = new LinkedHashMap<>();

                boolean canBeAssigned = clazzList.stream().anyMatch(c -> c.get("canBeAssigned").equals(true));
                batchClazzsMap.put("canBeAssigned", canBeAssigned);
                batchClazzsMap.put("clazzs", clazzList);
                batchClazzsMap.put("classLevel", i);
                batchClazzsList.add(batchClazzsMap);
            }
        }
        return MapMessage.successMessage().add("batchclazzs", batchClazzsList);
    }

    /**
     * 通过bookId取布置假期作业周内容
     * 默认显示6周的内容
     */
    @RequestMapping(value = "weektab.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadVacationHomeworkPackage() {
        String bookId = getRequestString("bookId");
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.safeParse(getRequestString("subject")));
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ErrorCodeConstants.ERROR_CODE_AUTH);
        }
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("假期作业课本id为空").setErrorCode(ErrorCodeConstants.ERROR_CODE_BOOK_ID_IS_NULL);
        }
        return vacationHomeworkLoaderClient.loadBookPlanInfo(bookId);
    }

    /**
     * 查看作业内容
     * @return
     */
    @RequestMapping(value = "day/planelements.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dayPlanElements() {
        String bookId = getRequestString("bookId");
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail(Subject.safeParse(getRequestString("subject")));
        if (teacherDetail == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ErrorCodeConstants.ERROR_CODE_AUTH);
        }
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("假期作业课本id为空").setErrorCode(ErrorCodeConstants.ERROR_CODE_BOOK_ID_IS_NULL);
        }
        int weekRank = getRequestInt("weekRank");
        if (weekRank <= 0) {
            return MapMessage.errorMessage("假期作业周不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_WEEK_RANK_NOT_EXISTS);
        }
        int dayRank = getRequestInt("dayRank");
        if (dayRank <= 0) {
            return MapMessage.errorMessage("假期作业周不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_DAY_RANK_NOT_EXISTS);
        }
        return vacationHomeworkLoaderClient.loadDayPlanElements(teacherDetail, bookId, weekRank, dayRank);
    }


    /**
     * 布置假期作业
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveHomework(@RequestBody Map<String, Object> map) {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.of(SafeConverter.toString(map.get("subject"))));
        map.put("User-Agent", getRequest().getHeader("User-Agent")); // ?
        //添加布置作业ip
        String ip = getWebRequestContext().getRealRemoteAddress();
        map.put("ip", ip);
        HomeworkSource source = HomeworkSource.newInstance(map);
        return vacationHomeworkServiceClient.assignHomework(teacher, source, HomeworkSourceType.Web);
    }

    /**
     * 删除作业
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteNewHomework() {
        String homeworkId = getRequestString("vacationPackageId");
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ErrorCodeConstants.ERROR_CODE_AUTH);
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("假期作业id为空").setErrorCode(ErrorCodeConstants.ERROR_CODE_SOURCE_HOMEWORK_ID_IS_NULL);
        }
        return vacationHomeworkServiceClient.deleteHomework(teacher.getId(), homeworkId);
    }

    /**
     * 17年暑假作业活动接口，已下线
     */
    @RequestMapping(value = "activity/index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadActivityIndexData() {
        return MapMessage.errorMessage("活动已下线");
    }

    /**
     * 点击布置暑假作业
     * 17年暑假作业活动接口，已下线
     */
    @RequestMapping(value = "clickassign.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clickAssign() {
        return MapMessage.errorMessage("活动已下线");
    }

    /**
     * 检查作业
     * 17年暑假作业活动接口，已下线
     */
    @RequestMapping(value = "check.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkVacationHomework() {
        return MapMessage.errorMessage("活动已下线");
    }
}
