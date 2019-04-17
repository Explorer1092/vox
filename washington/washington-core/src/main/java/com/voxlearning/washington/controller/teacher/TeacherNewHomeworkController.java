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
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceLoaderClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.constant.NewBookType;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.feedback.api.entities.VoiceFeedback;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkQuestionAnswerRequest;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.mapper.PictureBookQuery;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroup;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.ThirdPartyGroupLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @since 2015/12/7
 */
@Controller
@RequestMapping("/teacher/new/homework")
public class TeacherNewHomeworkController extends AbstractTeacherController {

    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;
    @Inject private ThirdPartyGroupLoaderClient thirdPartyGroupLoaderClient;
    @Inject private DubbingLoaderClient dubbingLoaderClient;
    @Inject private TeachingResourceLoaderClient teachingResourceLoaderClient;
    @Inject private FeedbackServiceClient feedbackServiceClient;

    /**
     * NEW -- 批量布置作业
     */
    @RequestMapping(value = "batchassignhomework.vpage", method = RequestMethod.GET)
    public String clickBatchAssignHomework(Model model, HttpServletRequest request) {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        Subject subject = teacher.getSubject();
        try {
            //菜鸟进来就看到作业引导流程了
            String step = getRequestParameter("step", "xxx");
            boolean first = step.equals("showtip");
            model.addAttribute("first", first);

            List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId()).stream()
                    .filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .sorted(new Clazz.ClazzLevelAndNameComparator())
                    .collect(Collectors.toList());
            if (clazzs.isEmpty()) {
                return "redirect:/teacher/showtip.vpage";
            }
            Date startDate = new Date();
            model.addAttribute("startDate", DateUtils.dateToString(startDate, "yyyy-MM-dd"));
            model.addAttribute("startTime", DateUtils.dateToString(startDate, "HH:mm"));
            model.addAttribute("date", DateUtils.nowToString("yyyy-MM-dd"));

            Date endDate = new Date(System.currentTimeMillis() + 300000);
            endDate = DateUtils.nextDay(endDate, 1);

            model.addAttribute("endDate", DateUtils.dateToString(endDate, "yyyy-MM-dd"));
            model.addAttribute("endTime", DateUtils.dateToString(endDate, "HH:mm"));
            model.addAttribute("ids", getRequestParameter("ids", ""));
            model.addAttribute("level", getRequestParameter("l", "0"));
            //作为作业的开始时间,用于前端显示,格式为yyyy-MM-dd HH:mm:ss,只能当天时间，因为页面结束时间语义为选择今天、明天、后天字样
            model.addAttribute("currentDateTime", DateUtils.dateToString(DayRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATETIME));
            model.addAttribute("subjectId", subject.getId());
            model.addAttribute("subject", subject);

            // 可布置作业班级列表
            List<ExClazz> canBeAssignedClazzList = newHomeworkContentServiceClient.findTeacherClazzsCanBeAssignedHomework(teacher);
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
                batchClazzs.computeIfAbsent(clazzLevel, k -> new ArrayList<>())
                        .add(clazzMap);
            });

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

            List<GroupTeacherMapper> groupTeacherMappers = deprecatedGroupLoaderClient.loadTeacherGroups(teacher.getId(), true);
            boolean hasStudents = groupTeacherMappers.stream().anyMatch(group -> CollectionUtils.isNotEmpty(group.getStudents()));
            model.addAttribute("hasStudents", hasStudents);
            model.addAttribute("batchclazzs", JsonUtils.toJson(batchClazzsList));
            model.addAttribute("step", getRequest().getParameter("step"));
            model.addAttribute("bookid", getRequest().getParameter("bookid"));
            model.addAttribute("unitid", getRequest().getParameter("unitid"));
            model.addAttribute("homeworkType", subject.name());
            model.addAttribute("tts_url", ProductConfig.getMainSiteBaseUrl() + "/tts.vpage");
            // 判断是否需要展示二维码
            model.addAttribute("wxbinded", wechatLoaderClient.isBinding(teacher.getId(), WechatType.TEACHER.getType()));

            // 提交类型，new是第一次布置作业
            model.addAttribute("submitType", getRequestParameter("t", "old"));

            // 判断老师是否提示换班提示
            model.addAttribute("hasAdjust", asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                    .TeacherAdjustClazzRemindCacheManager_done(teacher.getId())
                    .getUninterruptibly());
            model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            model.addAttribute("readingFlashUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, "/resources/apps/flash/Reading.swf"));
            SchoolYear schoolYear = SchoolYear.newInstance();
            model.addAttribute("term", schoolYear.currentTerm().getKey());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if(grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "PCHomework", "UseVenus")){
            if (Subject.ENGLISH == subject) {
                return "teacherv3/homeworkv5/englishhomework";
            } else {
                return "teacherv3/homeworkv5/homework";
            }
        }else{
            if (Subject.ENGLISH == subject) {
                return "teacherv3/homeworkv3/englishhomework";
            } else {
                return "teacherv3/homeworkv3/homework";
            }
        }
    }


    /**
     * NEW -- 批量布置17xue作业
     */
    @RequestMapping(value = "assign17xuehomework.vpage", method = RequestMethod.GET)
    public String assign17xueHomework(Model model, HttpServletRequest request) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        Subject subject = Subject.valueOf(getRequestString("subject"));
        String groupIds = getRequestString("groupId");
        Long groupLevel = getRequestLong("groupLevel", 0);

        try {
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

            List<Long> thirdPartyGroupIds = new ArrayList<>();
            for (String groupId : groupIds.split(",")) {
                thirdPartyGroupIds.add(SafeConverter.toLong(groupId));
            }
            Map<Long, ThirdPartyGroup> thirdPartyGroupMap = thirdPartyGroupLoaderClient.loadThirdPartyGroupsIncludeDisabled(thirdPartyGroupIds);
            // 生成各年级信息
            List<Map<String, Object>> batchClazzsList = new ArrayList<>();
            Map<String, Object> batchClazzsMap = new LinkedHashMap<>();
            List<Map<String, Object>> clazzList = new ArrayList<>();
            for (ThirdPartyGroup thirdPartyGroup : thirdPartyGroupMap.values()) {
                Map<String, Object> clazzMap = new LinkedHashMap<>();
                clazzMap.put("classId", thirdPartyGroup.getClazzId());
                clazzMap.put("className", StringUtils.isBlank(thirdPartyGroup.getGroupName()) ? "无名称" : thirdPartyGroup.getGroupName());
                clazzMap.put("canBeAssigned", true);
                clazzMap.put("groupId", thirdPartyGroup.getId());
                clazzList.add(clazzMap);
            }
            batchClazzsMap.put("canBeAssigned", true);
            batchClazzsMap.put("clazzs", clazzList);
            batchClazzsMap.put("classLevel", groupLevel);
            batchClazzsList.add(batchClazzsMap);
            List<GroupTeacherMapper> groupTeacherMappers = deprecatedGroupLoaderClient.loadTeacherGroups(teacher.getId(), true);
            boolean hasStudents = groupTeacherMappers.stream().anyMatch(group -> CollectionUtils.isNotEmpty(group.getStudents()));
            model.addAttribute("hasStudents", hasStudents);
            model.addAttribute("batchclazzs", JsonUtils.toJson(batchClazzsList));
            model.addAttribute("homeworkType", subject.name());
            model.addAttribute("extData", getRequestString("extData"));
            model.addAttribute("tts_url", ProductConfig.getMainSiteBaseUrl() + "/tts.vpage");
            model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            model.addAttribute("readingFlashUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, "/resources/apps/flash/Reading.swf"));
            SchoolYear schoolYear = SchoolYear.newInstance();
            model.addAttribute("term", schoolYear.currentTerm().getKey());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (Subject.ENGLISH == subject) {
            return "teacherv3/homeworkv3/englishhomework";
        } else {
            return "teacherv3/homeworkv3/homework";
        }
    }

    /**
     * 获取默认教材
     *
     * @param clazzs 班级
     */
    @RequestMapping(value = "clazz/book.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getClazzBook(@RequestParam("clazzs") String clazzs) {
        if (StringUtils.isEmpty(clazzs)) {
            return MapMessage.errorMessage("信息不全");
        }
        Teacher teacher = getSubjectSpecifiedTeacher();
        List<String> clazzIdGroupIdList = Arrays.asList(clazzs.trim().split(","));
        Map<Long, Long> clazzIdGroupIdMap = new HashMap<>();
        clazzIdGroupIdList.forEach(str -> {
            String[] strs = str.split("_");
            if (strs.length == 2) {
                clazzIdGroupIdMap.put(SafeConverter.toLong(strs[0]), SafeConverter.toLong(strs[1]));
            }
        });

        if (MapUtils.isEmpty(clazzIdGroupIdMap)) {
            return MapMessage.errorMessage("信息不全");
        }

        //长远提供17xue判断方法
        if (teacher.is17XueTeacher()) {
            return newHomeworkContentServiceClient.load17XueBook(getSubjectSpecifiedTeacher(), clazzIdGroupIdMap.values(), NewBookType.YIQIXUE);
        } else {
            return newHomeworkContentServiceClient.loadClazzBook(getSubjectSpecifiedTeacher(), clazzIdGroupIdMap, StringUtils.equalsIgnoreCase("vacation", getRequestString("from")));
        }
    }

    /**
     * 根据年级，上下册获取教材
     */
    @RequestMapping(value = "sortbook.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage sortBook() {
        int clazzLevel = getRequestInt("level");
        int term = getRequestInt("term");
        Teacher teacher = getSubjectSpecifiedTeacher();
        Subject subject = teacher.getSubject();
        if (subject == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }

        if (noAccessPermission(teacher)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        List<NewBookProfile> newBookProfiles;
        //长远提供17xue判断方法
        if (teacher.is17XueTeacher()) {
            newBookProfiles = newContentLoaderClient.loadBookBySubjectAndNewBookType(teacher.getSubject(), NewBookType.YIQIXUE);
        } else {
            newBookProfiles = newHomeworkContentServiceClient.loadBooks(teacher, clazzLevel, term);
        }

        if (CollectionUtils.isNotEmpty(newBookProfiles)) {
            MapMessage message = MapMessage.successMessage();
            message.add("total", newBookProfiles.size());
            message.add("rows", newBookProfiles);
            return message;
        }
        return MapMessage.errorMessage("未找到合适的教材");
    }

    /**
     * 更换教材
     */
    @RequestMapping(value = "changebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBook(@RequestParam String clazzs, @RequestParam String bookId) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(teacher.getId(), "WEB_CHANGEBOOK", "teacher/new/homework/changebook.vpage", 10)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }
        List<String> clazzIdGroupIdSet = Arrays.asList(clazzs.trim().split(","));
        Set<Long> clazzIdSet = new LinkedHashSet<>();
        Set<Long> groupIdSet = new LinkedHashSet<>();
        clazzIdGroupIdSet.forEach(str -> {
            String[] strs = str.split("_");
            if (strs.length == 2) {
                clazzIdSet.add(SafeConverter.toLong(strs[0]));
                groupIdSet.add(SafeConverter.toLong(strs[1]));
            }
        });
        StringBuilder clazzIds = new StringBuilder();
        StringBuilder groupIds = new StringBuilder();
        if (CollectionUtils.isNotEmpty(clazzIdSet)) {
            clazzIdSet.forEach(clazzId -> {
                if (StringUtils.isBlank(clazzIds)) {
                    clazzIds.append(clazzId);
                } else {
                    clazzIds.append(",").append(clazzId);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(groupIdSet)) {
            groupIdSet.forEach(groupId -> {
                if (StringUtils.isBlank(groupIds)) {
                    groupIds.append(groupId);
                } else {
                    groupIds.append(",").append(groupId);
                }
            });
        }
        ChangeBookMapper command = new ChangeBookMapper();
        command.setBooks(bookId);
        command.setClazzs(clazzIds.toString());
        command.setGroups(groupIds.toString());
        MapMessage message;
        //长远提供17xue判断方法
        if (teacher.is17XueTeacher()) {
            message = newContentServiceClient.getRemoteReference().setGroupBook(teacher, command);
        } else {
            message = newContentServiceClient.getRemoteReference().setClazzBook(teacher, command);
        }

        if (message.isSuccess()) {
            message.setInfo("教材更换成功");
        } else {
            logger.error("Failed to change book: {}", message.getInfo());
        }
        return message;
    }

    /**
     * 作业形式
     *
     * @param unitId 单元id
     * @param bookId 教材id
     */
    @RequestMapping(value = "type.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkType(@RequestParam("unitId") String unitId,
                                   @RequestParam("bookId") String bookId) {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        if (StringUtils.isBlank(unitId) || StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("参数错误");
        }
        String sections = getRequestString("sections");
        List<String> sectionIds = Arrays.asList(sections.trim().split(","));
        return newHomeworkContentServiceClient.getHomeworkType(teacher, sectionIds, unitId, bookId, "pc", null, getCdnBaseUrlStaticSharedWithSep());
    }

    /**
     * 作业内容
     */
    @RequestMapping(value = "content.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkContent() {
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String type = getRequestString("type");
        if (StringUtils.isBlank(unitId) || StringUtils.isBlank(bookId) || StringUtils.isBlank(type)) {
            return MapMessage.errorMessage("参数错误");
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        if (objectiveConfigType == null) {
            return MapMessage.errorMessage("类型错误");
        }
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        String sections = getRequestString("sections");
        List<String> sectionIds = Arrays.asList(sections.trim().split(","));
        int currentPageNum = getRequestInt("currentPageNum", 1);
        MapMessage mapMessage = newHomeworkContentServiceClient.getHomeworkContent(teacher, Collections.emptySet(), sectionIds, unitId, bookId, objectiveConfigType, currentPageNum);
        String version = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "BASIC_APP_VIEW_H5_VERSION");
        if (StringUtils.isBlank(version)) {
            version = "V2_5_0";
        }
        return mapMessage.add("selfViewUrl", "/flash/loader/newselfstudymobile.vpage").add("version", version);
    }

    /**
     * 口算取题
     */
    @RequestMapping(value = "mental/question.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getMentalQuestion(@RequestParam("knowledgePoint") String knowledgePoint,
                                        @RequestParam(value = "contentTypeId") Integer contentTypeId,
                                        @RequestParam(value = "chosenQuestions", required = false) String chosenQuestions,
                                        @RequestParam("newQuestionCount") Integer newQuestionCount) {
        if (StringUtils.isEmpty(knowledgePoint)) {
            return MapMessage.errorMessage("信息不全");
        }
        List<String> chosenQuestionIds;
        if (StringUtils.isEmpty(chosenQuestions)) {
            chosenQuestionIds = new ArrayList<>();
        } else {
            chosenQuestionIds = Arrays.asList(chosenQuestions.trim().split(","));
        }

        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        if (noAccessPermission(teacher)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        return newHomeworkContentServiceClient.getMentalQuestion(knowledgePoint, contentTypeId, chosenQuestionIds, newQuestionCount);
    }

    /**
     * 纸质口算获取教辅列表
     */
    @RequestMapping(value = "ocrmental/workbook/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ocrMentalWorkbookList() {
        String bookId = getRequestString("bookId");
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null || teacherDetail.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("教材id错误");
        }
        return newHomeworkContentServiceClient.loadOcrMentalWorkBookList(teacherDetail, bookId);
    }

    /**
     * 同步习题智能组题
     */
    @RequestMapping(value = "intelligence/question.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadIntelligenceExamQuestion() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        String clazzs = getRequestString("clazzs");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String sections = getRequestString("sections");
        String algoType = getRequestString("algoType");
        int difficulty = getRequestInt("difficulty");
        int questionCount = getRequestInt("questionCount");
        String kpIds = getRequestString("kpIds");
        String contentTypeIds = getRequestString("contentTypeIds");
        String objectiveConfigId = getRequestString("objectiveConfigId");
        String type = getRequestString("type");
        if (StringUtils.isBlank(clazzs) || StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("参数错误");
        }
        if (teacher.getSubject() == Subject.MATH && (difficulty <= 0 || difficulty > 5)) {
            return MapMessage.errorMessage("难度错误");
        }
        if (questionCount <= 0 || questionCount > 50) {
            return MapMessage.errorMessage("题量错误");
        } else if (questionCount < 3) {
            return MapMessage.errorMessage("暂无合适的试题");
        }
        Set<Long> groupIds = new LinkedHashSet<>();
        List<String> clazzIdGroupIdList = Arrays.asList(clazzs.trim().split(","));
        clazzIdGroupIdList.forEach(str -> {
            String[] strs = str.split("_");
            if (strs.length == 2) {
                groupIds.add(SafeConverter.toLong(strs[1]));
            }
        });
        List<String> sectionIds = StringUtils.toList(sections, String.class);
        if (Subject.MATH == teacher.getSubject() && CollectionUtils.isEmpty(sectionIds)) {
            return MapMessage.errorMessage("参数错误");
        }
        List<String> kpIdList = StringUtils.toList(kpIds, String.class);
        List<Integer> contentTypeIdList = StringUtils.toList(contentTypeIds, Integer.class);

        if (noAccessPermission(teacher)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        return newHomeworkContentServiceClient.loadIntelligenceQuestion(teacher, groupIds, sectionIds, bookId, unitId, algoType, difficulty, questionCount, kpIdList, contentTypeIdList, objectiveConfigId, type);
    }

    /**
     * 查询绘本
     */
    @RequestMapping(value = "reading/search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readingSearch() {
        String readingName = getRequestString("readingName");
        String clazzLevels = getRequestString("clazzLevels");
        String topicIds = getRequestString("topicIds");
        String seriesIds = getRequestString("seriesIds");
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("信息不全");
        }
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }

        List<String> clazzLevelStringList = Arrays.asList(clazzLevels.trim().split(","));
        List<Integer> clazzLevelList = clazzLevelStringList.stream()
                .filter(str -> PictureBookClazzLevel.of(str) != null)
                .map(str -> PictureBookClazzLevel.valueOf(str).getClazzLevel())
                .collect(Collectors.toList());
        List<String> topicIdList = Arrays.stream(topicIds.trim().split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        List<String> seriesIdList = Arrays.stream(seriesIds.trim().split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        PictureBookQuery pictureBookQuery = new PictureBookQuery();
        pictureBookQuery.setName(readingName);
        pictureBookQuery.setClazzLevels(clazzLevelList);
        pictureBookQuery.setTopicIds(topicIdList);
        pictureBookQuery.setSeriesIds(seriesIdList);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        return newHomeworkContentServiceClient.searchReading(pictureBookQuery, pageable, bookId, unitId, teacher);
    }

    /**
     * 作业预览
     * 同步习题精选包预览，试卷预览，作业整体预览
     */
    @RequestMapping(value = "preview.vpage", method = RequestMethod.POST)
    public String previewHomework(Model model) {
        String contents = getRequestParameter("contents", "");
        String bookId = getRequestString("bookId");
        Map<String, List> contentMap = JsonUtils.fromJsonToMap(contents, String.class, List.class);
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher != null && teacher.getSubject() != null && MapUtils.isNotEmpty(contentMap)) {
            MapMessage message = newHomeworkContentServiceClient.previewContent(teacher, bookId, contentMap);
            if (message.isSuccess()) {
                model.addAttribute("contents", JsonUtils.toJson(message.get("contents")));
            } else {
                model.addAttribute("contents", JsonUtils.toJson(Collections.emptyList()));
            }
        }
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return "teacherv3/homeworkv3/viewquestions";
    }

    /**
     * 作业预览
     * 同步习题精选包预览，试卷预览，作业整体预览
     */
    @RequestMapping(value = "contentpreview.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage homeworkPreview() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        String contents = getRequestParameter("contents", "");
        String bookId = getRequestString("bookId");
        Map<String, List> contentMap = new HashMap<>();
        try {
            contentMap = JsonUtils.fromJsonToMap(contents, String.class, List.class);
        } catch (Exception e) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", teacher.getId(),
                    "mod1", contents,
                    "op", "homeworkPreview"
            ));
        }
        MapMessage message = newHomeworkContentServiceClient.previewContent(teacher, bookId, contentMap);
        message.add("domain", getWebRequestContext().getWebAppBaseUrl());
        message.add("env", RuntimeMode.current());
        message.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return message;
    }

    /**
     * 根据班级Ids获取可用学豆最大值
     */
    @RequestMapping(value = "maxic.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage calculateMaxIntegralCount() {
        String clazzIds = getRequestString("clazzIds");
        int dc = 0; // 默认值
        int mc = 0; // 最大值
        Object overTimeGids = null;
        Object limitTime = null;

        if (StringUtils.isNotBlank(clazzIds)) {
            Set<Long> cids = Arrays.stream(StringUtils.split(clazzIds, ","))
                    .map(e -> ConversionUtils.toLong(StringUtils.split(e, "_")[0])).collect(Collectors.toSet());
            Map<String, Object> map = new HashMap<>();
            try {
                map = businessTeacherServiceClient
                        .calculateHomeworkMaxIntegralCount(getSubjectSpecifiedTeacherDetail(), cids);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
            dc = SafeConverter.toInt(map.get("dc"));
            mc = SafeConverter.toInt(map.get("mc"));
            overTimeGids = map.get("overTimeGids");
            limitTime = map.get("limitTime");
        }

        return MapMessage.successMessage().add("dc", dc).add("mc", mc).add("maxDurationMinutes", NewHomeworkConstants.ASSIGN_HOMEWORK_MAX_DURATION_MINUTES).add("overTimeGids", overTimeGids).add("limitTime", limitTime);
    }


    /**
     * NEW HOMEWORK-- 布置作业
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveHomework(@RequestBody Map<String, Object> map) {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.of(SafeConverter.toString(map.get("subject"))));
        map.put("User-Agent", getRequest().getHeader("User-Agent"));
        //添加布置作业ip
        String ip = getWebRequestContext().getRealRemoteAddress();
        map.put("ip", ip);
        HomeworkSource source = HomeworkSource.newInstance(map);
        NewHomeworkType newHomeworkType = NewHomeworkType.of(SafeConverter.toString(source.get("homeworkType")));
        if (NewHomeworkType.Unknown.equals(newHomeworkType)) {
            return MapMessage.errorMessage().setInfo("没有homeworkType参数。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST);
        }
        MapMessage mapMessage;
        HomeworkTag homeworkTag = HomeworkTag.of(SafeConverter.toString(source.get("homeworkTag")));
        //长远提供17xue判断方法
        if (teacher.is17XueTeacher()) {
            mapMessage = newHomeworkServiceClient.assignHomework(teacher, source, HomeworkSourceType.Web, NewHomeworkType.YiQiXue, homeworkTag);
            mapMessage.add("homeworkType", NewHomeworkType.YiQiXue);
            if (mapMessage.isSuccess()) {
                getWebRequestContext().cleanupAuthenticationStates();
            }
        } else {
            mapMessage = newHomeworkServiceClient.assignHomework(teacher, source, HomeworkSourceType.Web, newHomeworkType, homeworkTag);
        }
        return mapMessage;
    }

    /**
     * NEW HOMEWORK 调整作业
     */
    @RequestMapping(value = "adjust/index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage adjustHomeworkInfo(@RequestParam("homeworkId") String homeworkId) {
        MapMessage mapMessage = new MapMessage();
        try {
            NewHomework homework = newHomeworkLoaderClient.loadNewHomework(homeworkId);

            if (homework == null) {
                return MapMessage.errorMessage("作业不存在");
            }

            // 权限检查
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(currentUserId(), homework.getClazzGroupId())) {
                return MapMessage.errorMessage("没有权限调整此作业");
            }

            if (homework.getCreateAt() != null && homework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
                return MapMessage.errorMessage("此份作业已不允许调整");
            }

            List<Map<String, Object>> practices = homework.getPractices()
                    .stream()
                    .map(hc -> MapUtils.m(
                            "objectiveConfigType", hc.getType(),
                            "typeName", hc.getType().getValue(),
                            "questionCount", ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == hc.getType() ? 1 : (hc.getQuestions() != null ? hc.getQuestions().size() : hc.getApps().size()))
                    )
                    .collect(Collectors.toList());
            mapMessage.add("practices", practices);
            mapMessage.add("finishTime", homework.getDuration());
            mapMessage.add("startDateTime", DateUtils.dateToString(homework.getStartTime(), DateUtils.FORMAT_SQL_DATE));
            mapMessage.add("endDateTime", DateUtils.dateToString(homework.getEndTime()));
            Date endDate = new Date(System.currentTimeMillis() + 300000);
            mapMessage.add("currentDate", DateUtils.dateToString(endDate, DateUtils.FORMAT_SQL_DATE));
            mapMessage.add("nowEndTime", DateUtils.dateToString(endDate, "HH:mm"));
            mapMessage.setSuccess(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
        return mapMessage;
    }

    /**
     * NEW HOMEWORK-- 调整作业
     */
    @RequestMapping(value = "adjust.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adjust(@RequestParam("homeworkId") String homeworkId,
                             @RequestParam("endDate") String endDate) {
        try {
            Date endDateTime = DateUtils.stringToDate(endDate);
            long currentTime = new Date().getTime();
            if (endDateTime == null || endDateTime.getTime() < currentTime) {
                return MapMessage.errorMessage("结束时间错误");
            }
            return newHomeworkServiceClient.adjustHomework(currentUserId(), homeworkId, endDateTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * NEW HOMEWORK-- 检查作业
     */
    @RequestMapping(value = "check.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage check(@RequestParam("homeworkId") String homeworkId) {
        Teacher teacher = currentTeacher();
        return newHomeworkServiceClient.checkHomework(teacher, homeworkId, HomeworkSourceType.Web);
    }

    /**
     * 获取作业应试试题信息
     * xuesong.zhang
     */
    @RequestMapping(value = "questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map questions() {
        String homeworkId = getRequestString("homeworkId");
        String type = getRequestString("type");
        Integer categoryId = getRequestInt("categoryId", 0);
        String lessonId = getRequestString("lessonId");

        if (StringUtils.isAnyBlank(homeworkId, type)) {
            return MapMessage.errorMessage("作业不存在");
        }

        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
        request.setHomeworkId(homeworkId);
        request.setObjectiveConfigType(objectiveConfigType);
        request.setCategoryId(categoryId);
        request.setLessonId(lessonId);
        return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadHomeworkQuestions(request));
    }

    /**
     * 获取作业应试试题信息
     * xuesong.zhang
     */
    @RequestMapping(value = "questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map questionsAnswer() {
        String homeworkId = getRequestString("homeworkId");
        String type = getRequestString("type");
        Long studentId = getRequestLong("studentId");
        Integer categoryId = getRequestInt("categoryId", 0);
        String lessonId = getRequestString("lessonId");
        String videoId = getRequestString("videoId");
        String stoneDataId = getRequestString("stoneDataId");
        WordTeachModuleType wordTeachModuleType = WordTeachModuleType.of(getRequestString("wordTeachModuleType"));
        if (StringUtils.isAnyBlank(homeworkId, type)) {
            return MapMessage.errorMessage("作业不存在");
        }

        if (studentId == 0L) {
            return MapMessage.errorMessage("请选择学生");
        }

        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
        request.setHomeworkId(homeworkId);
        request.setObjectiveConfigType(objectiveConfigType);
        request.setStudentId(studentId);
        request.setCategoryId(categoryId);
        request.setLessonId(lessonId);
        request.setVideoId(videoId);
        request.setStoneDataId(stoneDataId);
        request.setWordTeachModuleType(wordTeachModuleType);
        return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadQuestionAnswer(request));
    }

    /**
     * 获取字词讲练作业应试试题信息
     * 字词讲练-字词训练模块PC报告专用
     */
    @RequestMapping(value = "word/teach/questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage wordTeachQuestionsAnswer() {
        String homeworkId = getRequestString("homeworkId");
        String type = getRequestString("type");
        Long studentId = getRequestLong("studentId");
        WordTeachModuleType wordTeachModuleType = WordTeachModuleType.of(getRequestString("wordTeachModuleType"));
        if (StringUtils.isAnyBlank(homeworkId, type)) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (studentId == 0L) {
            return MapMessage.errorMessage("请选择学生");
        }

        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
        request.setHomeworkId(homeworkId);
        request.setObjectiveConfigType(objectiveConfigType);
        request.setStudentId(studentId);
        request.setWordTeachModuleType(wordTeachModuleType);
        return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadWordTeachQuestionsAnswer(request));
    }

    /**
     * 删除作业
     * xuesong.zhang
     */
    @RequestMapping(value = "disablenewhomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage disableNewHomework() {
        String homeworkId = getRequestString("homeworkId");
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("没有权限删除作业").setErrorCode(ErrorCodeConstants.ERROR_CODE_PERMISSION);
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        return newHomeworkServiceClient.deleteHomework(teacher.getId(), homeworkId);
    }

    /**
     * 作业任务领取奖励
     */
    @RequestMapping(value = "task/rewardintegral.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rewardTaskIntegral() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登录后再查看");
        }
        String taskId = getRequestString("taskId");
        if (StringUtils.isEmpty(taskId)) {
            return MapMessage.errorMessage("任务id为空");
        }
        return newHomeworkServiceClient.rewardHomeworkTaskIntegral(teacher, taskId);
    }

    /**
     * 根据教材，单元，课时(小数或小语)获取教学目标子目标列表
     */
    @RequestMapping(value = "objective/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadObjectiveList() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isBlank(unitId) || StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("参数错误");
        }
        String sections = getRequestString("sections");
        List<String> sectionIds = Arrays.asList(StringUtils.split(sections, ","));
        Subject subject = teacher.getSubject();
        // 数学和语文需要传sectionIds，不然提示课时错误
        if ((subject == Subject.MATH || subject == Subject.CHINESE) && CollectionUtils.isEmpty(sectionIds)) {
            return MapMessage.errorMessage("课时错误");
        }

        if (noAccessPermission(teacher)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        return newHomeworkContentServiceClient.loadObjectiveList(teacher, sectionIds, unitId, bookId, HomeworkSourceType.Web, "");
    }

    /**
     * 根据子目标和作业形式获取内容
     */
    @RequestMapping(value = "objective/content.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadObjectiveContent() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        String type = getRequestString("type");
        String objectiveConfigId = getRequestString("objectiveConfigId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        if (objectiveConfigType == null || StringUtils.isBlank(objectiveConfigId)) {
            return MapMessage.errorMessage("参数错误");
        }
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isBlank(unitId) || StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("参数错误");
        }
        String sections = getRequestString("sections");
        List<String> sectionIds = Arrays.asList(StringUtils.split(sections, ","));
        String clazzs = getRequestString("clazzs");
        Set<Long> groupIdSet = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(clazzs)) {
            List<String> clazzIdGroupIdList = Arrays.asList(clazzs.trim().split(","));
            clazzIdGroupIdList.forEach(str -> {
                String[] strs = str.split("_");
                if (strs.length == 2) {
                    groupIdSet.add(SafeConverter.toLong(strs[1]));
                }
            });
        }

        if (noAccessPermission(teacher)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        int currentPageNum = getRequestInt("currentPageNum", 1);
        MapMessage mapMessage = newHomeworkContentServiceClient.loadObjectiveContent(teacher, groupIdSet, sectionIds, unitId, bookId, objectiveConfigType, objectiveConfigId, currentPageNum, HomeworkSourceType.Web, "", "");
        String version = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "BASIC_APP_VIEW_H5_VERSION");
        if (StringUtils.isBlank(version)) {
            version = "V2_5_0";
        }
        return mapMessage.add("selfViewUrl", "/flash/loader/newselfstudymobile.vpage").add("version", version).add("type", type);
    }

    /**
     * 根据子目标获取瀑布流的数据
     */
    @RequestMapping(value = "objective/waterfall/content.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadObjectiveWaterfallContent() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        String objectiveId = getRequestString("objectiveId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isBlank(objectiveId) || StringUtils.isBlank(unitId) || StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("参数错误");
        }

        String sections = getRequestString("sections");
        List<String> sectionIds = Arrays.asList(StringUtils.split(sections, ","));
        String clazzs = getRequestString("clazzs");
        Set<Long> groupIdSet = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(clazzs)) {
            List<String> clazzIdGroupIdList = Arrays.asList(clazzs.trim().split(","));
            clazzIdGroupIdList.forEach(str -> {
                String[] strs = str.split("_");
                if (strs.length == 2) {
                    groupIdSet.add(SafeConverter.toLong(strs[1]));
                }
            });
        }
        String sys = getRequestString("sys");
        String appVersion = getRequestString("ver");
        if (noAccessPermission(teacher)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        return newHomeworkContentServiceClient.loadObjectiveWaterfallContent(teacher, objectiveId, groupIdSet, sectionIds, bookId, unitId, sys, appVersion)
                .add("domain", getWebRequestContext().getWebAppBaseUrl());
    }

    /**
     * 获取基础练习瀑布流详细内容
     */
    @RequestMapping(value = "basicapp/waterfall/content.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadBasicAppWaterfallContent() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        String objectiveConfigId = getRequestString("objectiveConfigId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String categoryGroup = getRequestString("categoryGroup");
        if (StringUtils.isBlank(objectiveConfigId) || StringUtils.isBlank(unitId) || StringUtils.isBlank(bookId) || StringUtils.isBlank(categoryGroup)) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage mapMessage = newHomeworkContentServiceClient.loadBasicAppWaterfallContent(teacher, objectiveConfigId, bookId, unitId, categoryGroup);
        String version = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "BASIC_APP_VIEW_H5_VERSION");
        if (StringUtils.isBlank(version)) {
            version = "V2_5_0";
        }
        return mapMessage.add("selfViewUrl", "/flash/loader/newselfstudymobile.vpage").add("version", version);
    }


    /**
     * 作业报告查看作业内容
     */
    @RequestMapping(value = "practice/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPracticeDetail() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("参数错误");
        }
        return newHomeworkContentServiceClient.loadPracticeDetail(teacher, homeworkId);
    }

    /**
     * 获取自然拼读作业形式内容
     */
    @RequestMapping(value = "naturalspelling/content.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadNaturalSpellingContent() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        String objectiveConfigId = getRequestString("objectiveConfigId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");

        if (noAccessPermission(teacher)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        int level = getRequestInt("level");
        if (level < 1 || level > 3) {
            return MapMessage.errorMessage("等级错误");
        }
        return newHomeworkContentServiceClient.loadNaturalSpellingContent(teacher, bookId, unitId, objectiveConfigId, level);
    }

    /**
     * 查询趣味配音
     */
    @RequestMapping(value = "dubbing/search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchDubbing() {
        int clazzLevel = getRequestInt("clazzLevel");
        String searchWord = getRequestString("searchWord");
        String channelIds = getRequestString("channelIds");
        String albumIds = getRequestString("albumIds");
        String themeIds = getRequestString("themeIds");
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("信息不全");
        }
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null || teacherDetail.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录");
        }
        String type = getRequestParameter("type", "DUBBING");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        List<String> channelIdList = Arrays.stream(channelIds.trim().split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        List<String> albumIdList = Arrays.stream(albumIds.trim().split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        List<String> themeIdList = Arrays.stream(themeIds.trim().split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);

        if (noAccessPermission(teacherDetail)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        return newHomeworkContentServiceClient.searchDubbing(teacherDetail, clazzLevel, searchWord, channelIdList, albumIdList, themeIdList, bookId, unitId, pageable, objectiveConfigType);
    }

    /**
     * 趣味配音详情
     */
    @RequestMapping(value = "dubbing/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadDubbingDetail() {
        try {
            TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
            if (teacherDetail == null || teacherDetail.getSubject() == null) {
                return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录");
            }
            String dubbingId = getRequestString("dubbingId");
            if (StringUtils.isBlank(dubbingId)) {
                return MapMessage.errorMessage("参数错误");
            }
            String unitId = getRequestString("unitId");
            if (StringUtils.isBlank(unitId)) {
                return MapMessage.errorMessage("参数错误");
            }
            String bookId = getRequestString("bookId");
            if (StringUtils.isBlank(bookId)) {
                return MapMessage.errorMessage("参数错误");
            }
            String type = getRequestParameter("type", "DUBBING");
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
            return newHomeworkContentServiceClient.loadDubbingDetail(teacherDetail, bookId, unitId, dubbingId, objectiveConfigType);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 收藏趣味配音
     */
    @RequestMapping(value = "dubbing/collection.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage collectDubbing() {
        try {
            TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
            if (teacherDetail == null || teacherDetail.getSubject() == null) {
                return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录");
            }
            String dubbingId = getRequestString("dubbingId");
            if (StringUtils.isBlank(dubbingId)) {
                return MapMessage.errorMessage("参数错误");
            }
            return newHomeworkServiceClient.collectDubbing(teacherDetail, dubbingId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 趣味配音:我的收藏
     */
    @RequestMapping(value = "dubbing/collection/record.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadDubbingCollectionRecord() {
        try {
            int pageNum = getRequestInt("pageNum", 1);
            int pageSize = getRequestInt("pageSize", 10);
            String bookId = getRequestString("bookId");
            String unitId = getRequestString("unitId");
            TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
            if (teacherDetail == null || teacherDetail.getSubject() == null) {
                return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录");
            }
            Pageable pageable = new PageRequest(pageNum - 1, pageSize);
            return newHomeworkContentServiceClient.loadDubbingCollectionRecord(teacherDetail, bookId, unitId, pageable, "", "");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 新绘本主题列表
     */
    @RequestMapping(value = "picturebookplus/topiclist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPictureBookPlusTopicList() {
        return newHomeworkContentServiceClient.loadPictureBookPlusTopicList();
    }

    /**
     * 新绘本系列列表
     */
    @RequestMapping(value = "picturebookplus/serieslist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPictureBookPlusSeriesList() {
        return newHomeworkContentServiceClient.loadPictureBookPlusSeriesList();
    }

    /**
     * 新绘本阅读推荐搜索词
     */
    @RequestMapping(value = "picturebookplus/recommendsearchwords.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadRecommendSearchWords() {
        return newHomeworkContentServiceClient.loadPictureBookPlusRecommendSearchWords();
    }

    /**
     * 新绘本阅读搜索
     */
    @RequestMapping(value = "picturebookplus/search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchPictureBookPlus() {
        String clazzLevel = getRequestString("clazzLevel");
        String topicIds = getRequestString("topicIds");
        String seriesIds = getRequestString("seriesIds");
        String searchWord = getRequestString("searchWord");
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("信息不全");
        }
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null || teacherDetail.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录");
        }
        List<String> topicIdList = Arrays.stream(topicIds.trim().split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        List<String> seriesIdList = Arrays.stream(seriesIds.trim().split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);

        if (noAccessPermission(teacherDetail)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        return newHomeworkContentServiceClient.searchPictureBookPlus(teacherDetail, clazzLevel, topicIdList, seriesIdList, searchWord, bookId, unitId, pageable, "", "");
    }

    /**
     * 新绘本阅读布置历史
     */
    @RequestMapping(value = "picturebookplus/history.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPictureBookPlusHistory() {
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("信息不全");
        }
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null || teacherDetail.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录");
        }
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        return newHomeworkContentServiceClient.loadPictureBookPlusHistory(teacherDetail, bookId, unitId, pageable, "", "");
    }

    /**
     * 感恩母亲节作业
     */
    @RequestMapping(value = "mothersdayhomework.vpage", method = RequestMethod.GET)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage loadMothersDayHomework() {
        Map<Integer, String> clazzLevelDubbingId = new HashMap<>();
        clazzLevelDubbingId.put(1, "D_10300004981908");
        clazzLevelDubbingId.put(2, "D_10300004981908");
        clazzLevelDubbingId.put(3, "D_10300004983822");
        clazzLevelDubbingId.put(4, "D_10300004982300");
        clazzLevelDubbingId.put(5, "D_10300004979696");
        clazzLevelDubbingId.put(6, "D_10300004980742");
        MapMessage mapMessage;
        try {
            Teacher teacher = getSubjectSpecifiedTeacher(Subject.ENGLISH);
            if (teacher == null || teacher.getSubject() == null || teacher.getSubject() != Subject.ENGLISH) {
                return MapMessage.errorMessage("老师信息错误");
            }
            mapMessage = newHomeworkContentServiceClient.loadTeacherClazzList(teacher, Collections.singleton(NewHomeworkType.MothersDay), true);
        } catch (Exception ex) {
            logger.error("Failed to loadClazzList, error is:{}", ex);
            return MapMessage.errorMessage("获取班级列表异常");
        }
        List<Map<String, Object>> clazzList = new ArrayList<>();
        if ((mapMessage.get("clazzList")) instanceof List) {
            clazzList = (List) mapMessage.get("clazzList");
        }
        List<String> dubbingIdList = new ArrayList<>();
        int clazzLevel;
        for (Map<String, Object> clazz : clazzList) {
            clazzLevel = SafeConverter.toInt(clazz.get("clazzLevel"));
            dubbingIdList.add(clazzLevelDubbingId.get(clazzLevel));
        }
        MapMessage mapMessageNew = MapMessage.successMessage();
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByDocIds(dubbingIdList);
        List<Map<String, Object>> dubbingList = new ArrayList<>();
        if (dubbingMap.size() > 0) {
            for (Map<String, Object> clazz : clazzList) {
                boolean canBeAssigned = SafeConverter.toBoolean(clazz.get("canBeAssigned"));
                clazzLevel = SafeConverter.toInt(clazz.get("clazzLevel"));
                Dubbing dubbing = dubbingMap.get(clazzLevelDubbingId.get(clazzLevel));
                if (canBeAssigned && dubbing != null) {
                    clazz.put("dubbingId", dubbing.getId());
                    clazz.put("name", dubbing.getVideoName());
                    clazz.put("videoUrl", dubbing.getVideoUrl());
                    clazz.put("sentenceSize", dubbing.getSentences().size());
                    clazz.put("coverUrl", dubbing.getCoverUrl());
                    clazz.put("seconds", dubbing.getVideoSeconds());
                    dubbingList.add(clazz);
                }
            }
        }
        mapMessageNew.add("clazzList", dubbingList);
        return mapMessageNew;
    }

    /**
     * 教师APP首页推荐板块
     */
    @RequestMapping(value = "index/recommend.vpage", method = RequestMethod.GET)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage loadIndexRecommendContent() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        Subject subject = teacher.getSubject();
        String domain = getWebRequestContext().getWebAppBaseUrl();
        if (!teacher.isPrimarySchool()) {
            return MapMessage.successMessage()
                    .add("desc", "更多功能即将开放")
                    .add("imgUrl", getCdnBaseUrlStaticSharedWithSep() + "resources/app/17teacher/res/firstpage/first_page_default_icon_1.png")
                    .add("domain", domain);
        }
        List<Map<String, Object>> allRecommendModules = new ArrayList<>();
        String previewUrl = "";
        String objectiveId = "";
        String sys = getRequestString("sys");
        String appVersion = getRequestString("ver");
        if (Subject.MATH == subject || Subject.ENGLISH == subject || Subject.CHINESE == subject) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            MapMessage result = newHomeworkContentServiceClient.loadNewIndexRecommendContent(teacherDetail, sys, appVersion, getCdnBaseUrlAvatarWithSep());
            if (result.isSuccess()) {
                List<Map<String, Object>> recommendModules = (List<Map<String, Object>>) result.get("recommendModules");
                if (CollectionUtils.isNotEmpty(recommendModules)) {
                    allRecommendModules.addAll(recommendModules);
                    previewUrl = SafeConverter.toString(result.get("previewUrl"));
                    objectiveId = SafeConverter.toString(result.get("objectiveId"));
                }
            }
        }
        // 获取教学资源推荐列表
        MapMessage resourcesMessage = teachingResourceLoaderClient.loadHomePageChoicestResources(teacher.getId());
        if (resourcesMessage.isSuccess()) {
            List<Map<String, Object>> resourceList = (List<Map<String, Object>>) resourcesMessage.get("result");
            if (CollectionUtils.isNotEmpty(resourceList)) {
                allRecommendModules.add(MapUtils.m(
                        "module", "ChoicestResources",
                        "moduleName", "教学专题推荐",
                        "resources", resourceList,
                        "moreUrl", SafeConverter.toString(resourcesMessage.get("moreUrl")),
                        "objectiveConfigType", "ChoicestResources",
                        "typeName", "教学专题推荐"
                ));
            }
        }
        if (CollectionUtils.isEmpty(allRecommendModules)) {
            return MapMessage.successMessage()
                    .add("desc", "更多功能即将开放")
                    .add("imgUrl", getCdnBaseUrlStaticSharedWithSep() + "resources/app/17teacher/res/firstpage/first_page_default_icon_1.png")
                    .add("domain", domain);
        } else {
            return MapMessage.successMessage()
                    .add("recommendModules", allRecommendModules)
                    .add("previewUrl", previewUrl)
                    .add("objectiveId", objectiveId)
                    .add("domain", domain);
        }
    }

    @RequestMapping(value = "recommend/jump.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadRecommendJumpParams() {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null || teacherDetail.getSubject() == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        Long englishTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacherDetail.getId(), Subject.ENGLISH);
        if (englishTeacherId == null) {
            return MapMessage.errorMessage("暂只支持英语学科");
        }
        teacherDetail = teacherLoaderClient.loadTeacherDetail(englishTeacherId);
        String type = getRequestString("objectiveConfigType");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        String sys = getRequestString("sys");
        String appVersion = getRequestString("ver");
        String id = getRequestString("id");
        return newHomeworkContentServiceClient.loadRecommendJumpParams(teacherDetail, objectiveConfigType, sys, appVersion, id)
                .add("domain", getWebRequestContext().getWebAppBaseUrl());
    }

    /**
     * 儿童节作业
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "kidsdayhomework.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadKidsDayHomework() {
        Map<Integer, String> clazzLevelDubbingId = new HashMap<>();
        clazzLevelDubbingId.put(1, "D_10300004988120");
        clazzLevelDubbingId.put(2, "D_10300004986864");
        clazzLevelDubbingId.put(3, "D_10300004989757");
        clazzLevelDubbingId.put(4, "D_10300004989757");
        clazzLevelDubbingId.put(5, "D_10300004987472");
        clazzLevelDubbingId.put(6, "D_10300004987472");
        MapMessage mapMessage;
        try {
            Teacher teacher = getSubjectSpecifiedTeacher(Subject.ENGLISH);
            if (teacher == null || teacher.getSubject() == null || teacher.getSubject() != Subject.ENGLISH) {
                return MapMessage.errorMessage("老师信息错误");
            }
            mapMessage = newHomeworkContentServiceClient.loadNewTeacherClazzList(teacher, Collections.singleton(NewHomeworkType.Activity), Collections.singleton(HomeworkTag.KidsDay), true);
        } catch (Exception ex) {
            logger.error("Failed to loadClazzList, error is:{}", ex);
            return MapMessage.errorMessage("获取班级列表异常");
        }
        List<Map<String, Object>> clazzList = new ArrayList<>();
        if ((mapMessage.get("clazzList")) instanceof List) {
            clazzList = (List) mapMessage.get("clazzList");
        }
        List<String> dubbingIdList = new ArrayList<>();
        int clazzLevel;
        for (Map<String, Object> clazz : clazzList) {
            clazzLevel = SafeConverter.toInt(clazz.get("clazzLevel"));
            dubbingIdList.add(clazzLevelDubbingId.get(clazzLevel));
        }
        MapMessage mapMessageNew = MapMessage.successMessage();
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByDocIds(dubbingIdList);
        List<Map<String, Object>> dubbingList = new ArrayList<>();
        if (dubbingMap.size() > 0) {
            for (Map<String, Object> clazz : clazzList) {
                boolean canBeAssigned = SafeConverter.toBoolean(clazz.get("canBeAssigned"));
                clazzLevel = SafeConverter.toInt(clazz.get("clazzLevel"));
                Dubbing dubbing = dubbingMap.get(clazzLevelDubbingId.get(clazzLevel));
                if (canBeAssigned && dubbing != null) {
                    clazz.put("dubbingId", dubbing.getId());
                    clazz.put("name", dubbing.getVideoName());
                    clazz.put("videoUrl", dubbing.getVideoUrl());
                    clazz.put("sentenceSize", dubbing.getSentences().size());
                    clazz.put("coverUrl", dubbing.getCoverUrl());
                    clazz.put("seconds", dubbing.getVideoSeconds());
                    dubbingList.add(clazz);
                }
            }
        }
        mapMessageNew.add("clazzList", dubbingList);
        return mapMessageNew;
    }

    /**
     * 课时讲练测课程预览
     */
    @RequestMapping(value = "previewteachingcourse.vpage", method = RequestMethod.GET)
    public String previewTeachingCourse(Model model) {
        String courseId = getRequestParameter("courseId", "");
        model.addAttribute("courseId", courseId);
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return "teacherv3/homeworkv3/previewteachingcourse";
    }


    /**
     * 老师端作业报告增加语音题目反馈
     *
     * @return
     */
    @RequestMapping(value = "voicefeedback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage voiceFeedback() {
        MapMessage resultMap;
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        Subject subject = teacher.getSubject();
        if (subject == null || Subject.MATH == subject) {
            return MapMessage.errorMessage("目前只支持英语和语文学科");
        }
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isEmpty(homeworkId)) {
            return MapMessage.errorMessage("提交参数不完整");
        }
        String questionId = getRequestString("questionId");
        if (StringUtils.isEmpty(questionId)) {
            return MapMessage.errorMessage("提交参数不完整");
        }
        String voiceUrl = getRequestString("voiceUrl");
        String voiceText = getRequestString("voiceText");
        int type = getRequestInt("type");//问题类型
        if (StringUtils.isEmpty(VoiceFeedback.fetchVoiceFeedbackType(type))) {
            return MapMessage.errorMessage("暂无此反馈类型");
        }
        String content = getRequestString("content");//问题类型
        String enginName = getRequestString("enginName");//引擎名称
        Double enginScore = getRequestDouble("enginScore");//引擎评分
        String objectiveConfigType = getRequestString("objectiveConfigType");//作业类型
        Integer categoryId = getRequestInt("categoryId", 0);
        VoiceFeedback voiceFeedback = new VoiceFeedback();
        voiceFeedback.setSubject(subject.getValue());
        voiceFeedback.setTeacherId(teacher.getId());
        voiceFeedback.setHomeworkId(homeworkId);
        voiceFeedback.setQuestionId(questionId);
        voiceFeedback.setVoiceUrl(voiceUrl);
        voiceFeedback.setVoiceText(voiceText);
        voiceFeedback.setType(type);
        if (StringUtils.isEmpty(content)) {
            content = VoiceFeedback.fetchVoiceFeedbackType(type);
        }
        voiceFeedback.setContent(content);
        voiceFeedback.setEngineName(enginName);
        voiceFeedback.setEngineScore(enginScore);
        voiceFeedback.setObjectiveConfigType(objectiveConfigType);
        voiceFeedback.setCategoryId(categoryId);
        MapMessage message = new MapMessage();
        try {
            message = feedbackServiceClient.getFeedbackService().persistVoiceFeedback(voiceFeedback);
        } catch (Exception ex) {
            logger.error("failed to record voice feedback, teacher id {}, questionId {}", teacher.getId(), questionId, ex);
        }
        if (!message.isSuccess()) {
            resultMap = MapMessage.errorMessage(message.getInfo());
        } else {
            resultMap = MapMessage.successMessage();
        }
        return resultMap;
    }


    /**
     * 获取国庆假期作业是否能布置状态
     */
    @RequestMapping(value = "nationalday/status.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadNationalDayHomeworkAssignStatus() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        return newHomeworkServiceClient.loadNationalDayHomeworkAssignStatus(teacher);
    }

    /**
     * 一键布置国庆假期作业
     */
    @RequestMapping(value = "nationalday/assign.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage autoAssignNationalDayHomework() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        return newHomeworkServiceClient.autoAssignNationalDayHomework(teacher);
    }

    /**
     * 国庆假期作业布置过的班级列表
     */
    @RequestMapping(value = "nationalday/clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadNationalDayClazzList() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        return newHomeworkServiceClient.loadNationalDayClazzList(teacher);
    }

    /**
     * 国庆假期作业单个包的概要报告
     */
    @RequestMapping(value = "nationalday/report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadNationalDaySummaryReport() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师信息错误");
        }
        String packageId = getRequestString("packageId");
        return newHomeworkServiceClient.loadNationalDaySummaryReport(teacher, packageId);
    }

    /**
     * 预览单个应试题页面
     */
    @RequestMapping(value = "viewquestion.vpage", method = RequestMethod.GET)
    public String viewQuestion() {
        return "teacherv3/homeworkv5/viewquestion";
    }

    /**
     * 口语交际推荐搜索词
     */
    @RequestMapping(value = "oralcommunication/recommendsearchwords.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadOralCommunicationRecommendSearchWords() {
        return newHomeworkContentServiceClient.loadOralCommunicationSearchWords();
    }

    /**
     * 口语交际搜索
     */
    @RequestMapping(value = "oralcommunication/search.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage searchOralCommunication() {
        String clazzLevel = getRequestString("clazzLevel");
        String type = getRequestString("type");
        String searchWord = getRequestString("searchWord");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);

        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null || teacherDetail.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录");
        }
        return newHomeworkContentServiceClient.searchOralCommunication(teacherDetail, clazzLevel, type, searchWord, bookId, unitId, pageable);
    }

    /**
     * 口语交际详情
     */
    @RequestMapping(value = "oralcommunication/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadOralCommunicationDetail() {
        try {
            TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
            if (teacherDetail == null || teacherDetail.getSubject() == null) {
                return MapMessage.errorMessage("宁海没有设置学科及班级，请完成设置后再登录");
            }
            String bookId = getRequestString("bookId");
            String unitId = getRequestString("unitId");
            String oralCommunicationId = getRequestString("oralCommunicationId");
            if (StringUtils.isBlank(oralCommunicationId) || StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
                return MapMessage.errorMessage("参数错误");
            }
            return newHomeworkContentServiceClient.loadOralCommunicationDetail(teacherDetail, bookId, unitId, oralCommunicationId);
        } catch (Exception e) {
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }
}