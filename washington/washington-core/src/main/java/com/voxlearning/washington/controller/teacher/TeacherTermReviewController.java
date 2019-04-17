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
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/11/21
 */
@Controller
@RequestMapping("/teacher/termreview/")
public class TeacherTermReviewController extends AbstractTeacherController {

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        //return "redirect:/teacher/index.vpage";
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return "redirect:/teacher/showtip.vpage";
        }
        Subject subject = teacher.getSubject();
        Date startDate = new Date();
        model.addAttribute("startDate", DateUtils.dateToString(startDate, "yyyy-MM-dd"));
        model.addAttribute("startTime", DateUtils.dateToString(startDate, "HH:mm"));
        model.addAttribute("date", DateUtils.nowToString("yyyy-MM-dd"));
        Date endDate = new Date(System.currentTimeMillis() + 300000);
        endDate = DateUtils.nextDay(endDate, 1);
        model.addAttribute("endDate", DateUtils.dateToString(endDate, "yyyy-MM-dd"));
        model.addAttribute("endTime", DateUtils.dateToString(endDate, "HH:mm"));
        //作为作业的开始时间,用于前端显示,格式为yyyy-MM-dd HH:mm:ss,只能当天时间，因为页面结束时间语义为选择今天、明天、后天字样
        model.addAttribute("currentDateTime", DateUtils.dateToString(DayRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATETIME));
        model.addAttribute("subjectId", subject.getId());
        model.addAttribute("subject", subject);
        model.addAttribute("batchclazzs", JsonUtils.toJson(newHomeworkContentServiceClient.loadTeacherClazzList(teacher, Collections.singleton(NewHomeworkType.TermReview), true).get("clazzList")));
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return "teacherv3/termreview/index";
    }

    /**
     * 2017春季期末复习app入口
     */
    @RequestMapping(value = "app/index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage appIndex() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("请先设置学科和班级");
        }
        Long teacherId = teacher.getId();
        Long englishTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacherId, Subject.ENGLISH);
        // 判断是否有英语学科，控制模考入口
        boolean showNewExam = englishTeacherId != null;
        MapMessage mapMessage = MapMessage.successMessage();
        if (showNewExam) {
            Teacher englishTeacher = teacherLoaderClient.loadTeacher(englishTeacherId);
            mapMessage = newExamServiceClient.loadAppIndexData(englishTeacher);
        }
        mapMessage.add("showNewExam", showNewExam);
        return mapMessage;
    }

    /**
     * 获取班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadClazzList() {
        try {
            Teacher teacher = getSubjectSpecifiedTeacher();
            if (teacher == null || teacher.getSubject() == null) {
                return MapMessage.errorMessage("老师信息错误");
            }
            return newHomeworkContentServiceClient.loadTeacherClazzList(teacher, Collections.singleton(NewHomeworkType.TermReview), true);
        } catch (Exception ex) {
            logger.error("Failed to loadClazzList, error is:{}", ex);
            return MapMessage.errorMessage("获取班级列表异常");
        }
    }

    /**
     * 获取默认教材
     */
    @RequestMapping(value = "clazzbook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage loadClazzBook() {
        String clazzGroupIds = getRequestString("clazzGroupIds");
        try {
            List<String> clazzIdGroupIdList = Arrays.asList(clazzGroupIds.trim().split(","));
            Map<Long, Long> clazzIdGroupIdMap = new HashMap<>();
            clazzIdGroupIdList.forEach(str -> {
                String[] strs = str.split("_");
                if (strs.length == 2) {
                    long clazzId = SafeConverter.toLong(strs[0]);
                    long groupId = SafeConverter.toLong(strs[1]);
                    if (clazzId > 0 && groupId > 0) {
                        clazzIdGroupIdMap.put(clazzId, groupId);
                    }
                }
            });
            if (MapUtils.isEmpty(clazzIdGroupIdMap)) {
                return MapMessage.errorMessage("班组id列表错误");
            }
            Teacher teacher = getSubjectSpecifiedTeacher();
            MapMessage message = newHomeworkContentServiceClient.loadClazzBook(teacher, clazzIdGroupIdMap, false);
            if (message.isSuccess()) {
                Map<String, Object> clazzBook = (Map<String, Object>) message.get("clazzBook");
                Map<String, Object> clazzBookMap = new LinkedHashMap<>();
                if (MapUtils.isNotEmpty(clazzBook)) {
                    clazzBookMap.put("bookId", clazzBook.get("bookId"));
                    clazzBookMap.put("bookName", clazzBook.get("bookName"));
                    clazzBookMap.put("termType", clazzBook.get("termType"));
                }
                return MapMessage.successMessage().add("clazzbook", clazzBookMap);
            } else {
                return message;
            }
        } catch (Exception ex) {
            logger.error("Failed to loadClazzBook, error is: {}", ex);
            return MapMessage.errorMessage("获取默认教材异常");
        }
    }

    /**
     * 获取教材列表
     */
    @RequestMapping(value = "booklist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadBookList() {
        Integer term = getRequestInt("term");
        Integer clazzLevel = getRequestInt("level");
        try {
            if (term == 0) {
                return MapMessage.errorMessage("教材上下册错误");
            }
            if (clazzLevel == 0) {
                return MapMessage.errorMessage("年级信息错误");
            }
            Teacher teacher = getSubjectSpecifiedTeacher();
            Subject subject = teacher.getSubject();
            if (subject == null) {
                MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录");
            }
            List<NewBookProfile> newBookProfiles = newHomeworkContentServiceClient.loadBooks(teacher, clazzLevel, term);
            // 过滤期末复习不支持的教材
            if (CollectionUtils.isNotEmpty(newBookProfiles)) {
                newBookProfiles = newBookProfiles.stream()
                        .filter(book -> !NewHomeworkConstants.TERM_REVIEW_NOT_SUPPORTED_BOOK_SERIES.contains(book.getSeriesId()))
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(newBookProfiles)) {
                MapMessage.errorMessage("未找到合适的教材");
            }
            return MapMessage.successMessage().add("rows", newBookProfiles).add("total", newBookProfiles.size());
        } catch (Exception ex) {
            logger.error("Failed to loadBookList, error is: {}", ex);
            return MapMessage.errorMessage("获取教材列表异常");
        }
    }

    /**
     * 获取内容形式列表
     */
    @RequestMapping(value = "typelist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadContentTypeList() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String bookId = getRequestString("bookId");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("教材id为空");
        }
        String clazzGroupIdStr = getRequestString("clazzGroupIds");
        if (StringUtils.isBlank(clazzGroupIdStr)) {
            return MapMessage.errorMessage("班组id为空");
        }
        List<Long> groupIds = new ArrayList<>();
        try {
            List<String> clazzGroupIds = StringUtils.toList(clazzGroupIdStr, String.class);
            if (CollectionUtils.isNotEmpty(clazzGroupIds)) {
                for (String clazzGroupId : clazzGroupIds) {
                    String[] arr = clazzGroupId.split("_");
                    if (arr.length == 2) {
                        groupIds.add(SafeConverter.toLong(arr[1]));
                    }
                }
            }
            Subject subject;
            NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
            if (newBookProfile == null || newBookProfile.getSubjectId() == null) {
                subject = teacher.getSubject();
            } else {
                subject = Subject.fromSubjectId(newBookProfile.getSubjectId());
            }
            return newHomeworkContentServiceClient.loadTermReviewContentTypeList(subject, bookId, groupIds, true, getCdnBaseUrlStaticSharedWithSep(), teacher, null, null);
        } catch (Exception ex) {
            logger.error("Failed to load content type list, error is: {}", ex);
            return MapMessage.errorMessage("获取内容模块列表异常");
        }
    }

    /**
     * 获取内容
     */
    @RequestMapping(value = "content.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadContent() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String type = getRequestString("type");
        String bookId = getRequestString("bookId");
        TermReviewContentType termReviewContentType = TermReviewContentType.of(type);
        if (type == null) {
            return MapMessage.errorMessage("内容类型错误");
        }
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("教材id为空");
        }
        String clazzGroupIdStr = getRequestString("clazzGroupIds");
        if (StringUtils.isBlank(clazzGroupIdStr)) {
            return MapMessage.errorMessage("班组id为空");
        }
        try {
            List<Long> groupIds = new ArrayList<>();
            List<String> clazzGroupIds = StringUtils.toList(clazzGroupIdStr, String.class);
            if (CollectionUtils.isNotEmpty(clazzGroupIds)) {
                for (String clazzGroupId : clazzGroupIds) {
                    String[] arr = clazzGroupId.split("_");
                    if (arr.length == 2) {
                        groupIds.add(SafeConverter.toLong(arr[1]));
                    }
                }
            }
            if (CollectionUtils.isEmpty(groupIds)) {
                return MapMessage.errorMessage("班组id错误");
            }
            MapMessage mapMessage = newHomeworkContentServiceClient.loadTermReviewContent(teacher, groupIds, bookId, termReviewContentType);
            String version = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "BASIC_APP_VIEW_H5_VERSION");
            if (StringUtils.isBlank(version)) {
                version = "V2_5_0";
            }
            return mapMessage.add("selfViewUrl", "/flash/loader/newselfstudymobile.vpage").add("version", version);
        } catch (Exception ex) {
            logger.error("Failed to loadContent, error is:{}", ex);
            return MapMessage.errorMessage("获取期末复习内容异常");
        }
    }

    @RequestMapping(value = "basicreview/assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage assign() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String data = getRequestString("data");
        Map<String, Object> map = JsonUtils.fromJson(data);
        if (MapUtils.isEmpty(map)) {
            return MapMessage.errorMessage("作业内容错误");
        }
        try {
            HomeworkSource source = HomeworkSource.newInstance(map);
            return newHomeworkServiceClient.assignBasicReviewHomework(teacher, source, HomeworkSourceType.Web);
        } catch (Exception ex) {
            logger.error("Failed to assign basic review, error is:{}", ex);
            return MapMessage.errorMessage("布置期末复习基础必过异常");
        }
    }

    @RequestMapping(value = "basicreview/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String packageId = getRequestString("packageId");
        if (StringUtils.isBlank(packageId)) {
            return MapMessage.errorMessage("作业不存在");
        }
        try {
            return newHomeworkServiceClient.deleteBasicReviewHomework(teacher, packageId);
        } catch (Exception ex) {
            logger.error("Failed to delete basic review, error is:{}", ex);
            return MapMessage.errorMessage("删除期末复习基础必过异常");
        }
    }

    @RequestMapping(value = "basicreviewreport.vpage", method = RequestMethod.GET)
    public String basicReviewReport() {
        return "teacherv3/termreview/basicreviewreport";
    }

    /**
     * 数学基础必过内容预览
     */
    @RequestMapping(value = "basicreview/basicMathContent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadBasicMathContent() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String bookId = getRequestString("bookId");
        Integer stageId = getRequestInt("stageId");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("教材ID为空");
        }
        if (stageId == 0) {
            return MapMessage.errorMessage("知识点ID为空");
        }

        try {
            return newHomeworkContentServiceClient.loadBasicMathContent(bookId, stageId);
        } catch (Exception ex) {
            logger.error("Failed to load mathContent, error is:{}", ex);
            return MapMessage.errorMessage("获取数学基础必过内容异常");
        }
    }


    /**
     * 语文基础必过内容预览
     *
     * @return
     */
    @RequestMapping(value = "basicreview/basicChineseContent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadBasicChineseContent() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String bookId = getRequestString("bookId");
        Integer stageId = getRequestInt("stageId");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("教材ID为空");
        }
        if (stageId == 0) {
            return MapMessage.errorMessage("复习单元阶段为空");
        }
        try {
            MapMessage message = newHomeworkContentServiceClient.loadBasicChineseContent(teacher, bookId, stageId);
            message.add("domain", getWebRequestContext().getWebAppBaseUrl());
            message.add("env", RuntimeMode.current());
            message.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            return message;
        } catch (Exception ex) {
            logger.error("Failed to load chineseContent, error is:{}", ex);
            return MapMessage.errorMessage("获取语文基础必过内容异常");
        }

    }

}
