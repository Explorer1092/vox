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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.CrmAudioData;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Longlong Yu
 * @since 下午3:47,13-11-7.
 */
@Controller
@RequestMapping("/crm/homework")
public class CrmHomeworkController extends CrmAbstractController {
    /**
     * ***********************查询相关*****************************************************************
     */


    @RequestMapping(value = "newcopyhomework.vpage", method = RequestMethod.POST)
    @SuppressWarnings("unchecked")
    public String newCopyHomework() {
        String homeworkId = this.getRequestString("homeworkId");
        long studentId = this.getRequestLong("studentId", 0L);
        if (StringUtils.isBlank(homeworkId)) {
            getAlertMessageManager().addMessageError("作业不存在");
            return "redirect:/crm/homework/newhomeworkhomepage.vpage";
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            getAlertMessageManager().addMessageError("作业不存在");
            return "redirect:/crm/homework/newhomeworkhomepage.vpage";
        }
        if (studentId == 0L) {
            getAlertMessageManager().addMessageError("学生ID错误");
            return "redirect:/crm/homework/newhomeworkhomepage.vpage";
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            getAlertMessageManager().addMessageError("学生不存在");
            return "redirect:/crm/homework/newhomeworkhomepage.vpage";
        }
        NewHomeworkBook newHomeworkBook = newHomeworkLoaderClient.loadNewHomeworkBook(homeworkId);
        List<Long> groupIds = new LinkedList<>();
        Map<Long, List<GroupMapper>> longListMap = deprecatedGroupLoaderClient.loadStudentGroups(Collections.singleton(studentId), false);
        if (longListMap.get(studentId) != null) {
            groupIds = longListMap.get(studentId)
                    .stream()
                    .map(GroupMapper::getId)
                    .collect(Collectors.toList());
        }
        List<Long> successClazzIds = new ArrayList<>();
        //查询30天未检查的数据：30天时间待定
        Date currentTime = new Date();
        Date startDate = DateUtils.calculateDateDay(currentTime, -30);
        for (Long groupId : groupIds) {
            List<Teacher> teachers = teacherLoaderClient
                    .loadGroupTeacher(groupId)
                    .stream()
                    .filter(t -> t.getSubject() == newHomework.getSubject())
                    .collect(Collectors.toList());
            Subject subject = newHomework.getSubject();
            boolean flag = teachers.stream().allMatch(t -> t.getSubject() != subject);
            if (flag)
                continue;
            List<NewHomework.Location> newHomeworkIds = newHomeworkLoaderClient
                    .loadNewHomeworksByClazzGroupIds(Collections.singleton(groupId), startDate, currentTime)
                    .get(groupId)
                    .stream()
                    .filter(o -> !o.isChecked())
                    .collect(Collectors.toList());
            for (NewHomework.Location location : newHomeworkIds) {
                newHomeworkServiceClient.deleteHomework(location.getTeacherId(), location.getId());
            }
            GroupMapper group = deprecatedGroupLoaderClient.loadGroup(groupId, false);
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("duration", newHomework.getDuration());
            jsonMap.put("practices", newHomeworkServiceClient.findAppsFromHomework(newHomework, Collections.singleton(groupId), newHomework.findPracticeContents()));

            Map<String, List<Map>> books = new LinkedHashMap<>();
            if (newHomeworkBook != null && newHomeworkBook.getPractices() != null) {
                LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practices = newHomeworkBook.getPractices();
                for (ObjectiveConfigType objectiveConfigType : practices.keySet()) {
                    List<NewHomeworkBookInfo> bookInfos = practices.get(objectiveConfigType);
                    List<Map> maps = bookInfos
                            .stream()
                            .map(JsonUtils::safeConvertObjectToMap)
                            .collect(Collectors.toList());
                    books.put(objectiveConfigType.name(), maps);
                }
            }
            jsonMap.put("books", books);
            jsonMap.put("clazzIds", group.getClazzId() + "_" + group.getId());
            jsonMap.put("des", newHomework.getDes());
            jsonMap.put("endTime", DateUtils.dateToString(DayRange.current().getEndDate()));
            jsonMap.put("homeworkType", newHomework.getHomeworkTag());
            jsonMap.put("remark", "阿娟工具箱");
            jsonMap.put("startTime", DateUtils.dateToString(DayRange.current().getStartDate()));
            jsonMap.put("subject", newHomework.getSubject());
            com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource source = com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource.newInstance(jsonMap);
            MapMessage mapMessage = newHomeworkServiceClient.assignHomework(teachers.get(0), source, HomeworkSourceType.CRM, newHomework.getNewHomeworkType(), newHomework.getHomeworkTag());
            if (mapMessage.isSuccess()) {
                successClazzIds.add(groupId);
            }
        }
        getAlertMessageManager().addMessageSuccess("成功" + successClazzIds.size() + "个");
        return "redirect:/crm/homework/newhomeworkhomepage.vpage";
    }

    @RequestMapping(value = "vacationhomeworkhomepage.vpage", method = RequestMethod.GET)
    public String vacationHomeworkNewHomepage(Model model) {
        String hid = this.getRequestString("hid");
        String path = "crm/homework/vacationnewhomeworkhomepage";
        if (StringUtils.isBlank(hid)) {
            model.addAttribute("success", false);
            return path;
        }
        model.addAllAttributes(newHomeworkCrmLoaderClient.vacationHomeworkNewHomepage(hid));
        return path;
    }


    @RequestMapping(value = "newhomeworkhomepage.vpage", method = RequestMethod.GET)
    public String homeworkNewHomepage(Model model) {
        String homeworkId = getRequestString("homeworkId").replaceAll("\\s", "");

        Map<String, Object> attributes = newHomeworkCrmLoaderClient.homeworkNewHomepage(homeworkId);
        if (MapUtils.isEmpty(attributes)) {
            getAlertMessageManager().addMessageError("作业ID:" + homeworkId + "不存在");
        }
        model.addAllAttributes(attributes);
        return "crm/homework/newhomeworkhomepage";
    }

    @RequestMapping(value = "resumeHomework.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage resumeHomework() {
        String homeworkId = this.getRequestString("homeworkId");
        return newHomeworkCrmServiceClient.resumeNewHomework(homeworkId);
    }

    /**
     * 某个学生指定作业的做题详情
     */
    @RequestMapping(value = "usernewhomeworkresultdetail.vpage", method = RequestMethod.GET)
    public String studentSpecNewHomeworkDetail(Model model) {
        Long studentId = getRequestLong("userId");
        String homeworkId = getRequestParameter("homeworkId", "");
        model.addAllAttributes(newHomeworkCrmLoaderClient.studentSpecNewHomeworkDetail(studentId, homeworkId));
        return "crm/homework/usernewhomeworkresultdetail";
    }

    /**
     * 替换涉黄图片
     *
     * @return
     */
    @RequestMapping(value = "replaceimage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage replaceOcrImage() {
        String processId = this.getRequestString("processId");
        String homeworkId = getRequestString("homeworkId");
        Long userId = getRequestLong("userId");
        if (StringUtils.isBlank(processId) || StringUtils.isEmpty(homeworkId) || userId < 1L) {
            return MapMessage.errorMessage("参数错误");
        }
        Boolean isSucceed = newHomeworkCrmService.repairOcrMentalPractiseImage(homeworkId, userId, processId);
        if (isSucceed) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("不存在数据");
    }

    /**
     * 替换英语纸质听写图片
     * @return
     */
    @RequestMapping(value = "replaceOcrDictationImage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage replaceOcrDictationImage() {
        String processId = this.getRequestString("processId");
        String homeworkId = getRequestString("homeworkId");
        Long userId = getRequestLong("userId");
        if (StringUtils.isBlank(processId) || StringUtils.isEmpty(homeworkId) || userId < 1L) {
            return MapMessage.errorMessage("参数错误");
        }
        Boolean isSucceed = newHomeworkCrmService.repairOcrDictationPracticeImage(homeworkId, userId, processId);
        if (isSucceed) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("不存在数据");
    }

    @RequestMapping(value = "voiceanalysis.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage voiceAnalysis() {
        String voiceUrl = this.getRequestString("voiceUrl");
        if (StringUtils.isBlank(voiceUrl)) {
            return MapMessage.errorMessage("参数错误");
        }
        String voiceData = NewHomeworkUtils.sendGet(voiceUrl.replaceFirst("audio/play", "result"));
        if (StringUtils.isNotBlank(voiceData)) {
            CrmAudioData crmAudioData = JsonUtils.fromJson(voiceData, CrmAudioData.class);
            if (crmAudioData != null) {
                CrmAudioData.Summary summary = crmAudioData.refineInfo();
                if (summary.isFlag()) {
                    MapMessage mapMessage = MapMessage.successMessage();
                    mapMessage.add("summary", summary);
                    return mapMessage;
                }
            }
        }
        return MapMessage.errorMessage("不存在数据");
    }
}