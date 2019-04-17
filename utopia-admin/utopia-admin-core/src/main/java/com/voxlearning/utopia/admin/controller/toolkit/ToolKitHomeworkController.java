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

package com.voxlearning.utopia.admin.controller.toolkit;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkCrmService;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkBlackWhiteList;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkDict;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkStudentAuthDict;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.RepairHomeworkDataParam;
import com.voxlearning.utopia.service.newhomework.consumer.HomeworkDictServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType.CRM;

/**
 * Created with IntelliJ IDEA.
 * User: tanguohong
 * Date: 13-10-14
 * Time: 上午11:21
 * To change this template use File | Settings | File Templates.
 */
@Controller
@Slf4j
@RequestMapping("/toolkit/homework")
@NoArgsConstructor
public class ToolKitHomeworkController extends ToolKitAbstractController {

    @ImportService(interfaceClass = NewHomeworkCrmService.class)
    private NewHomeworkCrmService newHomeworkCrmService;

    @Inject private RaikouSDK raikouSDK;

    @Inject private HomeworkDictServiceClient homeworkDictServiceClient;

    @RequestMapping(value = "removeCache.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String removeCache() {
        String methodName = this.getRequestString("methodName");
        Long teacherId = this.getRequestLong("tid");
        Date curTime = new Date();
        String todayPermissionKey = StringUtils.join(Arrays.asList("API_DAY_ACCESS_PERMISSION_" + methodName, DateUtils.dateToString(curTime, "yyyyMMdd"), teacherId), ":");
        String dayLimitationKey = StringUtils.join(Arrays.asList("API_DAY_LIMIT_ACCESS_KEY_" + methodName, DateUtils.dateToString(curTime, "yyyyMMdd"), teacherId), ":");
        String hourLimitationKey = StringUtils.join(Arrays.asList("API_HOUR_LIMIT_ACCESS_KEY_" + methodName, DateUtils.dateToString(curTime, "yyyyMMddHH"), teacherId), ":");
        String minuteLimitationKey = StringUtils.join(Arrays.asList("API_MINUTE_LIMIT_ACCESS_KEY_" + methodName, DateUtils.dateToString(curTime, "yyyyMMddHHmm"), teacherId), ":");
        for (String key : Arrays.asList(todayPermissionKey, dayLimitationKey, hourLimitationKey, minuteLimitationKey)) {
            CacheObject<String> cacheObject = adminCacheSystem.CBS.flushable.get(key);
            if (cacheObject != null && cacheObject.getValue() != null) {
                adminCacheSystem.CBS.flushable.delete(key);
            }
        }

        return "toolkit/toolkit";
    }

    /**
     * 阿娟工具箱-复制作业
     *
     * @param homeworkId
     * @param strGroupIds
     * @return
     */
    @RequestMapping(value = "newcopyhomework.vpage", method = RequestMethod.POST)
    public String newCopyHomework(@RequestParam(value = "homeworkId", required = false) String homeworkId, @RequestParam(value = "groupId", required = false) String strGroupIds) {
        if (StringUtils.isBlank(homeworkId)) {
            getAlertMessageManager().addMessageError("hid is blank");
            return "toolkit/toolkit";
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            getAlertMessageManager().addMessageError("作业不存在");
            return "toolkit/toolkit";
        }
        NewHomeworkBook newHomeworkBook = newHomeworkLoaderClient.loadNewHomeworkBook(homeworkId);
        List<Long> groupIds = new LinkedList<>();
        for (String s : StringUtils.split(strGroupIds, ",")) {
            groupIds.add(SafeConverter.toLong(s));
        }
        List<Long> successClazzIds = new ArrayList<>();
        List<Long> failClazzIds = new ArrayList<>();
        //查询30天未检查的数据：30天时间待定
        Date currentTime = new Date();
        Date startDate = DateUtils.calculateDateDay(currentTime, -30);
        for (Long groupId : groupIds) {
            List<Teacher> teachers = teacherLoaderClient
                    .loadGroupTeacher(groupId)
                    .stream()
                    .filter(t -> t.getSubject() == newHomework.getSubject())
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(teachers)) {
                getAlertMessageManager().addMessageError("要复制的组没有该科目老师");
                return "toolkit/toolkit";
            }
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

            LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practices = newHomeworkBook.getPractices();
            Map<String, List<Map>> books = new LinkedHashMap<>();
            for (ObjectiveConfigType objectiveConfigType : practices.keySet()) {
                List<NewHomeworkBookInfo> bookInfos = practices.get(objectiveConfigType);
                List<Map> maps = bookInfos
                        .stream()
                        .map(JsonUtils::safeConvertObjectToMap)
                        .collect(Collectors.toList());
                books.put(objectiveConfigType.name(), maps);
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
            } else {
                failClazzIds.add(groupId);
            }
        }
        getAlertMessageManager().addMessageSuccess("成功" + successClazzIds.size() + "个，有" + failClazzIds.size() + "个班级布置作业失败。" +
                "成功班级是：" + JsonUtils.toJson(successClazzIds) +
                "失败班级是：" + JsonUtils.toJson(failClazzIds));
        return "toolkit/toolkit";
    }

    /**
     * 指定试题和ClassGroupId布置作业
     * xuesong.zhang 2016-03-18
     */
    @RequestMapping(value = "userdefinedassign.vpage", method = RequestMethod.POST)
    public String userDefinedAssign() {
        String qids = getRequestString("qids").replace("\r", "").replace("\t", "");
        Long clazzGroupId = getRequestLong("cgid");

        GroupTeacherTuple gtr = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByGroupId(clazzGroupId)
                .stream()
                .findFirst()
                .orElse(null);
        if (gtr == null || gtr.getTeacherId() == null) {
            getAlertMessageManager().addMessageError("组中没有老师");
            return "toolkit/toolkit";
        }

        List<String> questionIds = Arrays.asList(StringUtils.split(qids, "\n"));
        if (CollectionUtils.isEmpty(questionIds)) {
            getAlertMessageManager().addMessageError("没有试题");
            return "toolkit/toolkit";
        }
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(clazzGroupId, false);
        Teacher teacher = teacherLoaderClient.loadTeacher(gtr.getTeacherId());
        if (groupMapper == null || teacher == null) {
            getAlertMessageManager().addMessageError("班级组对应关系错误");
            return "toolkit/toolkit";
        }

        if (teacher.getSubject() == null /*|| StringUtils.equalsIgnoreCase(teacher.getSubject().name(), Subject.ENGLISH.name())*/) {
            getAlertMessageManager().addMessageError("老师没有学科");
            return "toolkit/toolkit";
        }


        List<NewQuestion> newQuestions = new ArrayList<>(questionLoaderClient.loadQuestionByDocIds0(questionIds).values());
//        if (CollectionUtils.isEmpty(newQuestions)) {
//            newQuestions = new ArrayList<>(questionLoaderClient.loadQuestionByDocIds0(questionIds).values());
//        }
        // 校验一下题和老师的学科是否一致
        Set<Integer> subjectSet = newQuestions.stream().map(NewQuestion::getSubjectId).collect(Collectors.toSet());
        if (subjectSet.size() != 1 || !subjectSet.contains(teacher.getSubject().getId())) {
            getAlertMessageManager().addMessageError("题目所属科目不统一，或者老师所教学科与题目所属学科不一致");
            return "toolkit/toolkit";
        }
        Long clazzId = groupMapper.getClazzId();
        // 新体系部分
        // 作业题的内容部分
        Map<String, Object> map = buildNewHomeworkJsonByQid(newQuestions, teacher);
        if (MapUtils.isEmpty(map)) {
            getAlertMessageManager().addMessageError("试题或学科存在问题");
            return "toolkit/toolkit";
        }

        // 校验检查作业
        checkNewHomework(clazzGroupId, teacher);

        // 其他必要属性
        map.put("startTime", DateUtils.dateToString(DayRange.current().getStartDate()));
        map.put("endTime", DateUtils.dateToString(DayRange.current().getEndDate()));
        map.put("subject", teacher.getSubject());
        map.put("remark", "阿娟工具箱");
        // clazzId_clazzGroupId拼接
        map.put("clazzIds", clazzId + "_" + clazzGroupId);

        // 布置作业的标准流程
        com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource source = com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource.newInstance(map);
        NewHomeworkType newHomeworkType = NewHomeworkType.Normal;
        HomeworkTag homeworkTag = HomeworkTag.of(SafeConverter.toString(source.get("homeworkTag")));
        MapMessage mapMessage = newHomeworkServiceClient.assignHomework(teacher, source, CRM, newHomeworkType, homeworkTag);
        if (mapMessage.isSuccess()) {
            getAlertMessageManager().addMessageSuccess("布置成功");
        } else {
            getAlertMessageManager().addMessageError(mapMessage.getInfo());
        }
        return "toolkit/toolkit";
//        }
    }

    /**
     * xuesong.zhang 2016-03-18
     */
    private void checkNewHomework(Long clazzGroupId, Teacher teacher) {
        List<NewHomework.Location> newhomeworks = newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(clazzGroupId, teacher.getSubject());
        if (CollectionUtils.isNotEmpty(newhomeworks)) {
            boolean haveUncheckHomework = newhomeworks.stream().anyMatch(nh -> !nh.isChecked());
            if (haveUncheckHomework) {
                for (NewHomework.Location location : newhomeworks) {
                    try {
                        newHomeworkServiceClient.checkHomework(teacher, location.getId(), CRM);
                    } catch (Exception ex) {
                        logger.error("", ex);
                    }
                }
            }
        }
    }

    /**
     * xuesong.zhang 2016-03-18
     */
    private Map<String, Object> buildNewHomeworkJsonByQid(List<NewQuestion> newQuestions, Teacher teacher) {
        Map<String, Object> map = new HashMap<>();
        // 将题分为两种类型，同步习题和主观作业类型，其他题型暂时不管。
        // 同步习题
        List<NewHomeworkQuestion> examContentQuestions = new ArrayList<>();
        // 主观类
        List<NewHomeworkQuestion> objectiveContentQuestions = new ArrayList<>();
        // 建议总需要耗时
        long duration = 0L;
        for (NewQuestion question : newQuestions) {
            if (question != null && question.getContent() != null && CollectionUtils.isNotEmpty(question.getContent().getSubContents())) {

                if (question.getContent().getSubContents().size() <= 0) {
                    // 试题没有题目详细内容
                    logger.warn("Question without content, qid:{}", question.getId());
                    return Collections.emptyMap();
                }

                NewQuestionsSubContents subContents = question.getContent().getSubContents().get(0);
                NewHomeworkQuestion nhq = new NewHomeworkQuestion();
                nhq.setQuestionId(question.getId());
                if (question.getSubjectId() == Subject.MATH.getId()) {
                    // TODO 需要支持数学的类题自定义,xuesong.zhang
                    nhq.setSimilarQuestionId(question.getId());
                }
                nhq.setSeconds(question.getSeconds());
                nhq.setSubmitWay(question.getSubmitWays());
                if (question.getSeconds() != null) {
                    duration += question.getSeconds();
                }

                if (question.getContent().getSubContents().size() > 1 || subContents.getSubContentTypeId() != 16) {
                    // 复合题统一放入同步习题，不做题型判断;基础题型不为16的
                    examContentQuestions.add(nhq);
                } else {
                    // 非复合题，如果基本题型是16，放进主观类题型
                    objectiveContentQuestions.add(nhq);
                }
            }
        }

        Map<String, Object> practices = new HashMap<>();
        Map<String, Object> books = new HashMap<>();
        Subject subject = teacher.getSubject();
        ObjectiveConfigType type = subject == Subject.CHINESE ? ObjectiveConfigType.BASIC_KNOWLEDGE : ObjectiveConfigType.INTELLIGENCE_EXAM;
        String bookId = subject == Subject.MATH ? "BK_10200001281813" : (subject == Subject.ENGLISH ? "BK_10300000265057" : "BK_10100000006594");
        String unitId = subject == Subject.MATH ? "BKC_10200065121114" : (subject == Subject.ENGLISH ? "BKC_10300009498508" : "BKC_10100000225556");

        if (CollectionUtils.isNotEmpty(examContentQuestions)) {
            practices.put(type.name(), MiscUtils.m("questions", examContentQuestions));

            Map<String, Object> bookInfo = new HashMap<>();
            bookInfo.put("bookId", bookId);
            bookInfo.put("unitId", unitId);
            bookInfo.put("includeQuestions", examContentQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
            List<Map<String, Object>> bookInfoList = new ArrayList<>();
            bookInfoList.add(bookInfo);
            books.put(type.name(), bookInfoList);
        }

        if (CollectionUtils.isNotEmpty(objectiveContentQuestions)) {
            practices.put(ObjectiveConfigType.PHOTO_OBJECTIVE.name(), MiscUtils.m("questions", objectiveContentQuestions));

            Map<String, Object> bookInfo = new HashMap<>();
            bookInfo.put("bookId", bookId);
            bookInfo.put("unitId", unitId);
            bookInfo.put("includeQuestions", objectiveContentQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
            List<Map<String, Object>> bookInfoList = new ArrayList<>();
            bookInfoList.add(bookInfo);
            books.put(ObjectiveConfigType.PHOTO_OBJECTIVE.name(), bookInfoList);
        }
        map.put("practices", practices);
        map.put("books", books);
        map.put("duration", duration);
        return map;
    }

    @RequestMapping(value = "repairhomeworkdata.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage repairHomeworkData() {
        String param = this.getRequestString("param");
        RepairHomeworkDataParam repairHomeworkDataParam = JsonUtils.fromJson(param, RepairHomeworkDataParam.class);
        if (StringUtils.isAnyBlank(repairHomeworkDataParam.getHid(), repairHomeworkDataParam.getQid())) {
            return MapMessage.errorMessage("参数错误");
        }
        if (repairHomeworkDataParam == null) {
            return MapMessage.errorMessage("参数格式错误");
        }
        if (repairHomeworkDataParam.getType() == null) {
            return MapMessage.errorMessage("参数错误");
        }
        if (repairHomeworkDataParam.getGrasp() == null) {
            return MapMessage.errorMessage("参数错误");
        }
        if (repairHomeworkDataParam.getSubGrasp() == null) {
            return MapMessage.errorMessage("参数错误");
        }
        if (repairHomeworkDataParam.getSubScore() == null) {
            return MapMessage.errorMessage("参数错误");
        }

        return newHomeworkCrmService.repairHomeworkData(repairHomeworkDataParam);
    }

    @RequestMapping(value = "changehomeworkendtime.vpage", method = RequestMethod.POST)
    public String changeHomeworkEndTime() {
        // 作业结束时间
        String startDateStr = getRequestString("startDate");
        String endDateStr = getRequestString("endDate");
        // 我是个后门参数
        String homeworkId = getRequestString("homeworkId");

        // 延后时间
        int day = getRequestInt("days", 0);
        String endTimeStr = getRequestString("endTime");

        if (StringUtils.isBlank(startDateStr) || StringUtils.isBlank(endDateStr)) {
            getAlertMessageManager().addMessageError("作业结束时间查询条件为空");
            return "toolkit/toolkit";
        }

        Date startDate;
        Date endDate;
        Date endTime;
        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        try {
            startDate = sdf.parse(startDateStr);
            endDate = sdf.parse(endDateStr);
            endTime = sdf.parse(endTimeStr);
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("时间格式错误");
            return "toolkit/toolkit";
        }

        if (day == 0 && StringUtils.isBlank(endTimeStr)) {
            getAlertMessageManager().addMessageError("延后时间不能为空");
            return "toolkit/toolkit";
        }

        // 查询作业的结束时间限制在12小时的跨度内
        if (startDate.getTime() > endDate.getTime() || DateUtils.addHours(startDate, 12).getTime() < endDate.getTime()) {
            getAlertMessageManager().addMessageError("起始时间大于结束时间 || 作业结束时间间隔不建议超过12小时");
            return "toolkit/toolkit";
        }

        // 延后的截止时间 比 作业结束时间的查询时间还要小
        if (endTime.getTime() < endDate.getTime()) {
            getAlertMessageManager().addMessageError("延后的截止时间 比 作业结束时间的查询时间还要小");
            return "toolkit/toolkit";
        }

        // 换算时间
        if (day > 0) {
            Date date = DateUtils.addDays(endDate, day);
            endDate = DayRange.newInstance(date.getTime()).getEndDate();
        }

        MapMessage mapMessage;
        // 校验的差不多了，可以开始干活了
        if (StringUtils.isNotBlank(homeworkId)) {
            // 我是个后门
            mapMessage = newHomeworkCrmServiceClient.changeHomeworkEndTime(homeworkId, endTime);
        } else {
            mapMessage = newHomeworkCrmServiceClient.changeHomeworkEndTime(startDate, endDate, endTime);
        }
        getAlertMessageManager().addMessageSuccess("操作完成，作业总数[" + mapMessage.get("allCount") + "]，成功[" + mapMessage.get("successCount") + "]，失败[" + mapMessage.get("failedCount") + "]");
        return "toolkit/toolkit";
    }

    @RequestMapping(value = "vacation/autoassign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage autoAssignVacationHomework() {
        String teacherIds = getRequestString("teacherIds");
        if (StringUtils.isBlank("teacherIds")) {
            return MapMessage.errorMessage("参数错误");
        }
        String[] teacherIdArray = teacherIds.split("\n");
        Set<Long> teacherIdSet = new LinkedHashSet<>();
        for (String teacherIdStr : teacherIdArray) {
            Long teacherId = SafeConverter.toLong(teacherIdStr);
            if (teacherId != 0) {
                teacherIdSet.add(teacherId);
            }
        }
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIdSet);
        if (MapUtils.isEmpty(teacherMap)) {
            return MapMessage.errorMessage("老师列表为空");
        } else {
            if (teacherMap.size() > 50) {
                return MapMessage.errorMessage("一次最多50个老师");
            } else {
                Map<Long, String> resultMap = new LinkedHashMap<>();
                teacherMap.forEach((id, teacher) -> {
                    try {
                        MapMessage result = vacationHomeworkServiceClient.autoAssign(teacher);
                        if (result.isSuccess()) {
                            resultMap.put(id, "布置成功");
                        } else {
                            resultMap.put(id, "布置失败" + "(" + result.getInfo() + ")");
                        }
                    } catch (Exception e) {
                        resultMap.put(id, "执行异常" + e.getMessage());
                    }
                });
                return MapMessage.successMessage().add("results", resultMap);
            }
        }
    }

    @RequestMapping(value = "nationalday/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteNationalDayHomework() {
        Long teacherId = getRequestLong("teacherId");
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        return newHomeworkServiceClient.deleteNationalDayHomework(teacher);
    }

    ;

    @RequestMapping(value = "/addrewardinparentapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addHomeworkRewardInParentApp() {
        Long studentId = getRequestLong("studentId");
        String homeworkId = getRequestString("homeworkId");
        Integer count = getRequestInt("count");
        if (count <= 0) {
            return MapMessage.errorMessage("奖励数量不能为0");
        }
        if (count > 5) {
            return MapMessage.errorMessage("奖励数量不能超过5个");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("请输入学生ID");
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("请输入作业ID");
        }
        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业" + homeworkId + "不存在");
        }
        NewHomeworkFinishRewardInParentApp rewardInParentApp = newHomeworkPartLoaderClient.getRewardInParentApp(studentId);
        if (rewardInParentApp != null && (
                (rewardInParentApp.getNotReceivedRewardMap() != null && rewardInParentApp.getNotReceivedRewardMap().containsKey(homeworkId))
                        || (rewardInParentApp.getHadReceivedRewardMap() != null && rewardInParentApp.getHadReceivedRewardMap().containsKey(homeworkId))
                        || (rewardInParentApp.getChangeGroupRewardMap() != null && rewardInParentApp.getChangeGroupRewardMap().containsKey(homeworkId))
                        || (rewardInParentApp.getTimeoutRewardMap() != null && rewardInParentApp.getTimeoutRewardMap().containsKey(homeworkId))
        )) {
            return MapMessage.errorMessage("该学生已获得过作业" + homeworkId + "的奖励");
        }
        Date expire = DateUtils.addDays(new Date(), 30);
        return newHomeworkCrmServiceClient.addHomeworkRewardInParentApp(studentId, homeworkId, newHomework.getClazzGroupId(), count, expire);
    }

    /**
     * 消息
     */
    @RequestMapping(value = "/resenddubbingsynthetic.vpage")
    @ResponseBody
    public MapMessage resendDubbingSynthetic() {
        String paramIds = getRequestString("ids");
        String[] ids = StringUtils.split(paramIds, ",");
        if (ids == null || ids.length <= 0) {
            return MapMessage.errorMessage("参数错误");
        }
        return newHomeworkCrmService.crmResendDubbingSynthetic(Arrays.asList(ids));
    }

    @RequestMapping(value = "/repairselfstudycorrecthomework.vpage")
    public String repairSelfStudyCorrectHomework() {
        String homeworkId = getRequestString("homeworkId");
        Long studentId = getRequestLong("studentId", 0L);

        if (StringUtils.isBlank(homeworkId) || studentId == 0L) {
            getAlertMessageManager().addMessageError("参数有空的");
        }
        MapMessage mapMessage = newHomeworkCrmServiceClient.repairSelfStudyCorrectHomework(homeworkId, studentId);
        if (mapMessage.isSuccess()) {
            getAlertMessageManager().addMessageSuccess("作业修复完成");
        } else {
            getAlertMessageManager().addMessageError(mapMessage.getInfo());
        }
        return "toolkit/toolkit";
    }

    @RequestMapping(value = "/fetchHomeworkDictList.vpage")
    public String fetchHomeworkDictList(Model model) {
        List<HomeworkDict> homeworkDicts = homeworkDictServiceClient.fetchHomeworkDictList();

        model.addAttribute("homeworkDicts", homeworkDicts);
        return "toolkit/dict/homework_dict_list";
    }

    @RequestMapping(value = "/upsertHomeworkDict.vpage", method = RequestMethod.GET)
    public String findHomeworkDict(Model model) {
        String id = getRequestString("id");
        HomeworkDict homeworkDict;
        if (StringUtils.isBlank(id)) {
            homeworkDict = new HomeworkDict();
        } else {
            homeworkDict = homeworkDictServiceClient.findHomeworkDict(id);
        }

        model.addAttribute("homeworkDict", homeworkDict);
        return "toolkit/dict/homework_dict_edit";
    }

    @RequestMapping(value = "/deleteHomeworkDict.vpage")
    @ResponseBody
    public MapMessage deleteHomeworkDict() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("id不能为空");
        }

        MapMessage message = homeworkDictServiceClient.deleteHomeworkDict(id);
        if (message.isSuccess()) {
            message.setInfo("删除成功!");
        } else {
            message.setInfo(message.getInfo());
        }
        return message;
    }

    @RequestMapping(value = "/upsertHomeworkDict.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertHomeworkDict() {

        String id = getRequestString("id");
        String name = getRequestString("name");
        String level = getRequestString("level");
        String levelId = getRequestString("levelId");
        String parentId = getRequestString("parentId");
        String prefix = getRequestString("prefix");
        String enumValue = getRequestString("enumValue");
        String beAuthed = getRequestString("beAuthed");
        if (StringUtils.isBlank(id) || StringUtils.isBlank(name) || StringUtils.isBlank(prefix)) {
            return MapMessage.errorMessage("有参数为空");
        }

        HomeworkDict dict = new HomeworkDict();
        dict.setId(id);
        dict.setName(name);
        dict.setLevel(level);
        dict.setLevelId(levelId);
        dict.setParentId(parentId);
        dict.setPrefix(prefix);
        dict.setEnumValue(enumValue);
        dict.setBeAuthed(beAuthed);

        MapMessage message = homeworkDictServiceClient.upsertHomeworkDict(dict);
        if (message.isSuccess()) {
            return message.setInfo("操作成功!");
        }
        return message.setInfo(message.getInfo());
    }

    @RequestMapping(value = "/fetchHomeworkStudentAuthDictList.vpage")
    public String fetchHomeworkStudentAuthDictList(Model model) {
        List<HomeworkStudentAuthDict> homeworkStudentAuthDicts = homeworkDictServiceClient.fetchHomeworkStudentAuthDictList();

        model.addAttribute("homeworkStudentAuthDicts", homeworkStudentAuthDicts);
        return "toolkit/dict/homework_student_auth_dict_list";
    }

    @RequestMapping(value = "/deleteHomeworkStudentAuthDict.vpage")
    @ResponseBody
    public MapMessage deleteHomeworkStudentAuthDict() {
        Long id = getRequestLong("id");
        if (id == 0L) {
            return MapMessage.errorMessage("id不能为空");
        }

        MapMessage message = homeworkDictServiceClient.deleteHomeworkStudentAuthDict(id);
        if (message.isSuccess()) {
            message.setInfo("删除成功!");
        } else {
            message.setInfo(message.getInfo());
        }
        return message;
    }

    @RequestMapping(value = "/upsertHomeworkStudentAuthDict.vpage", method = RequestMethod.GET)
    public String findHomeworkStudentAuthDict(Model model) {
        HomeworkStudentAuthDict homeworkStudentAuthDict;
        Long id = getRequestLong("id");
        if (id == 0L) {
            homeworkStudentAuthDict = new HomeworkStudentAuthDict();
        } else {
            homeworkStudentAuthDict = homeworkDictServiceClient.findHomeworkStudentAuthDict(id);
        }

        model.addAttribute("homeworkStudentAuthDict", homeworkStudentAuthDict);
        return "toolkit/dict/homework_student_auth_dict_edit";
    }

    @RequestMapping(value = "/upsertHomeworkStudentAuthDict.vpage", method = RequestMethod.POST)
    public String upsertHomeworkStudentAuthDict() {

        Long id = getRequestLong("id");
        String homeworkType = getRequestString("homeworkType");
        String homeworkTypeName = getRequestString("homeworkTypeName");
        String homeworkFormType = getRequestString("homeworkFormType");
        String homeworkFormName = getRequestString("homeworkFormName");
        if (StringUtils.isBlank(homeworkType) || StringUtils.isBlank(homeworkTypeName) || StringUtils.isBlank(homeworkFormType) || StringUtils.isBlank(homeworkFormName)) {
            getAlertMessageManager().addMessageError("有参数为空");
        } else {
            HomeworkStudentAuthDict dict = new HomeworkStudentAuthDict();
            dict.setId(id);
            dict.setHomeworkType(homeworkType);
            dict.setHomeworkTypeName(homeworkTypeName);
            dict.setHomeworkFormType(homeworkFormType);
            dict.setHomeworkFormName(homeworkFormName);

            MapMessage message = homeworkDictServiceClient.upsertHomeworkStudentAuthDict(dict);
            if (message.isSuccess()) {
                getAlertMessageManager().addMessageSuccess("操作成功!");
            } else {
                getAlertMessageManager().addMessageError(message.getInfo());
            }
        }

        return "redirect: /toolkit/homework/fetchHomeworkStudentAuthDictList.vpage";
    }

    /**
     * 添加小学作业白名单
     */
    @RequestMapping(value = "/createHomeworkBlackWhiteList.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String createHomeworkBlackWhiteList() {
        String businessType = getRequestString("businessType");
        String idType = getRequestString("idType");
        String blackWhiteId = getRequestString("blackWhiteId");
        if (StringUtils.isBlank(blackWhiteId)) {
            getAlertMessageManager().addMessageError("黑(白)名单ID不能为空");
            return "toolkit/toolkit";
        }

        if (newHomeworkCrmServiceClient.addNewHomeworkBlackWhiteList(businessType, idType, blackWhiteId)) {
            addAdminLog(StringUtils.join("createHomeworkBlackWhiteList-", getCurrentAdminUser().getAdminUserName(), ", id：", HomeworkBlackWhiteList.generateId(businessType, idType, blackWhiteId)));
            getAlertMessageManager().addMessageSuccess("添加成功!");
        } else {
            getAlertMessageManager().addMessageError("添加失败");
        }
        return "toolkit/toolkit";
    }

    /**
     * 小学作业白名单列表
     */
    @RequestMapping(value = "/loadHomeworkBlackWhiteList.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadHomeworkBlackWhiteList() {
        String businessType = getRequestString("businessType");
        String idType = getRequestString("idType");
        String blackWhiteId = getRequestString("blackWhiteId");
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 20);
        Pageable pageable = new PageRequest(pageNum, pageSize);
        PageImpl<HomeworkBlackWhiteList> page = newHomeworkCrmLoaderClient.loadNewHomeworkBlackWhiteLists(businessType, idType, blackWhiteId, pageable);
        return MapMessage.successMessage().set("homeworkBlackWhiteLists", page.getContent()).set("currentPage", page.getNumber()).set("totalPages", page.getTotalPages()).set("totalCount", page.getTotalElements());
    }

    /**
     * 小学作业白名单
     */
    @RequestMapping(value = "/deleteHomeworkBlackWhiteList.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteHomeworkBlackWhiteList() {

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("id不能为空");
        }

        if (newHomeworkCrmServiceClient.deleteNewHomeworkBlackWhiteList(id)) {
            addAdminLog(StringUtils.join("deleteHomeworkBlackWhiteList-", getCurrentAdminUser().getAdminUserName(), ", id: ", id));
            return MapMessage.successMessage("删除成功!");
        } else {
            return MapMessage.errorMessage("删除失败!");
        }
    }
}
