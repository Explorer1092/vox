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

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionLib;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionReport;
import com.voxlearning.utopia.business.api.mapper.SmartClazzStudentResult;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzQuestionRef;
import com.voxlearning.utopia.mapper.SmartClazzRank;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzStudentCardMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Maofeng Lu
 * @since 14-6-27 上午11:39
 */
@Controller
@RequestMapping("/teacher/smartclazz")
public class TeacherSmartClazzController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;

    /**
     * 智慧教室
     * ------班级列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String smartClazzList(Model model) {
        Long teacherId = currentUserId();

        // 包班制支持，显示所有班级
        List<Long> teacherIds = new ArrayList<>();
        teacherIds.add(teacherId);
        teacherIds.addAll(teacherLoaderClient.loadSubTeacherIds(teacherId));

        // 老师班级信息
        Map<Long, List<Clazz>> teacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherIds);

        // 显示班级列表
        List<Clazz> clazzList = teacherClazzs
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(c -> !c.isTerminalClazz())
                .filter(c -> !c.isDisabledTrue())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(clazzList)) {
            return "redirect:/teacher/showtip.vpage";
        }

        // 班级->学生id列表
        Map<Long, List<Long>> studentIds = new HashMap<>();
        // 班级->学科
        Map<Long, List<Subject>> clazzSubjects = new HashMap<>();

        boolean multiSubject = teacherIds.size() > 1;
        Map<Long, Teacher> teachers = new HashMap<>();
        if (multiSubject) {
            teachers.putAll(teacherLoaderClient.loadTeachers(teacherIds));
        }

        teacherClazzs.forEach((tid, clazzs) -> {
            List<Long> clazzIds = new ArrayList<>();
            clazzs.forEach(c -> {
                Long cid = c.getId();

                clazzIds.add(cid);

                if (multiSubject) {
                    List<Subject> list = clazzSubjects.get(cid);
                    if (list == null) {
                        list = new ArrayList<>();
                        clazzSubjects.put(cid, list);
                    }
                    list.add(teachers.get(tid).getSubject());
                }
            });
            studentIds.putAll(userAggregationLoaderClient.loadTeacherStudentIdsByClazzIds(clazzIds, tid));
        });

        List<Map<String, Object>> result = new ArrayList<>();
        for (Clazz clazz : clazzList) {
            // system clazz support
            int studentCnt = studentIds.containsKey(clazz.getId()) ? studentIds.get(clazz.getId()).size() : 0;
            //没有学生的班级，直接跳过
            if (studentCnt < 1) {
                continue;
            }
            Map<String, Object> mapper = new HashMap<>();
            mapper.put("clazzId", clazz.getId());
            mapper.put("clazzLevel", clazz.getClassLevel());
            mapper.put("clazzName", clazz.formalizeClazzName());
            mapper.put("studentCount", studentCnt);
            // 包班制情况，显示学科信息
            List<Subject> subjectList = clazzSubjects.get(clazz.getId());
            if (CollectionUtils.isNotEmpty(subjectList)) {
                mapper.put("firstSubject", subjectList.get(0));
                mapper.put("subjectText", StringUtils.join(subjectList.stream().map(Subject::getValue).collect(Collectors.toList()), ","));
            }
            result.add(mapper);
        }
        model.addAttribute("clazzList", result);
        return "teacherv3/smartclazz/list";
    }

    /**
     * 智慧教室
     * ------某班级详情页面
     */
    @RequestMapping(value = "clazzdetail.vpage", method = RequestMethod.GET)
    public String smartClazzDetail(Model model) {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS)
            return "redirect:/teacher/smartclazz/list.vpage";

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(getRequestLong("clazzId"));
        if (clazz == null) {
            return "redirect:/teacher/index.vpage";
        }

        GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazz.getId(), false);
        if (group == null) {
            logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazz.getId(), teacher.getId());
            return "redirect:/teacher/index.vpage";
        }

        //班级学豆池初始化
        SmartClazzIntegralPool pool = clazzIntegralServiceClient.getClazzIntegralService()
                .loadClazzIntegralPool(group.getId())
                .getUninterruptibly();

        if (pool == null) {
            logger.warn("{}学科的班级{}学豆池未初始化", teacher.getSubject(), clazz.getId());
            return "redirect:/teacher/smartclazz/list.vpage";
        }
        model.addAttribute("clazzIntegralPool", pool);

        //查询当前月奖励给该班级学生的学豆
        MonthRange monthRange = MonthRange.current();
        List<SmartClazzRank> clazzStudentIntegralList = businessTeacherServiceClient
                .findSmartClazzIntegralHistory(group.getId(), monthRange.getStartDate());

        //剔除无名字的学生
        clazzStudentIntegralList = clazzStudentIntegralList.stream().filter(source ->
                StringUtils.isNotBlank(source.getStudentName())
        ).collect(Collectors.toList());

        // 排序
        clazzStudentIntegralList = clazzStudentIntegralList.stream()
                .sorted((o1, o2) -> {
                    String n1 = o1.getStudentName();
                    String n2 = o2.getStudentName();
                    if (StringUtils.equals(n1, n2)) {// 名字相同，按id排序
                        return (o1.getStudentId().compareTo(o2.getStudentId()));
                    }
                    return Collator.getInstance(Locale.CHINESE).compare(n1, n2);// 按拼音排序
                })
                .collect(Collectors.toList());

        for (int idx = 0; idx < clazzStudentIntegralList.size(); idx++) {
            clazzStudentIntegralList.get(idx).setInitial(String.valueOf(idx));
        }

        model.addAttribute("studentList", clazzStudentIntegralList);
        model.addAttribute("clazz", clazz);

        //多学科支持
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
        model.addAttribute("specifiedSubjects", getSpecifiedSubjectsByTeacherIdAndClazzId(teacher.getId(), clazz.getId()));

        return "teacherv3/smartclazz/clazzdetail";
    }


    /**
     * 智慧教室
     * ---老师给学生奖励学豆
     * jsonMap包含KEYS：userIds,clazzId,integralCnt,rewardItem
     */
    @RequestMapping(value = "updaterewardintegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateRewardIntegral(@RequestBody Map<String, Object> jsonMap) {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.valueOf(SafeConverter.toString(jsonMap.get("subject"))));

        Long clazzId = ConversionUtils.toLong(jsonMap.get("clazzId"));
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在");
        }
        if (!hasClazzTeachingPermission(teacher.getId(), clazzId)) {
            logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazzId, teacher.getId());
            return MapMessage.errorMessage("你没有该班级管理权限");
        }

        try {
            return atomicLockManager.wrapAtomic(businessClazzIntegralServiceClient)
                    .keyPrefix("NEWSMARTCLAZZ:")
                    .keys(teacher.getId(), clazzId)
                    .proxy()
                    .rewardSmartClazzStudent(teacher.getId(), clazz, jsonMap);
        } catch (DuplicatedOperationException de) {
            return MapMessage.errorMessage("正在处理奖励,请稍候操作");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.formatMessage(e.getMessage(), e));
        }
    }

    /**
     * 智慧教室
     * ----兑换学豆
     * 参数传递：clazzId,integralCnt
     */
    @RequestMapping(value = "exchangeintegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage exchangeIntegral() {
        Long clazzId = ConversionUtils.toLong(getRequestLong("clazzId"));
        int integralCnt = ConversionUtils.toInt(getRequestInt("integralCnt"));
        boolean force = getRequestBool("force");

        Teacher teacher = getSubjectSpecifiedTeacher();

        if (!force && !asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_incUserBehaviorCountWithLimitCheck(
                        UserBehaviorType.SMARTCLAZZ_EXCHANGE_BEAN,
                        teacher.getId(),
                        SafeConverter.toLong(integralCnt),
                        SafeConverter.toLong(integralCnt),
                        DateUtils.getCurrentToDayEndSecond()).getUninterruptibly()) {
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(teacher.getId());
            if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {// 只有在用户绑定手机的情况下，才需要验证码验证，否则直接兑换
                String am = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "/teacher/smartclazz/exchangeintegral", SafeConverter.toString(teacher.getId()));
                return MapMessage.errorMessage("needCodeVerification").add("mobile", am);
            }
        }

        try {
            return atomicLockManager.wrapAtomic(clazzIntegralService)
                    .keyPrefix("SMARTCLAZZ_EX:")
                    .keys(teacher.getSubject(), teacher.getId(), clazzId)
                    .proxy()
                    .saveSmartClazzExchangeIntegral(teacher, clazzId, integralCnt);
        } catch (DuplicatedOperationException de) {
            return MapMessage.errorMessage("正在处理兑换结果,请稍候操作");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.formatMessage(e.getMessage(), e));
        }
    }

    @RequestMapping(value = "sendTEICode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendTeacherExchangeIntegralCode() {
        String mobile = getRequestString("mobile");
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("错误的手机号");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.TEACHER_SMARTCLAZZ_EXCHANGE_INTEGRAL.name());
    }

    @RequestMapping(value = "verifyTEICode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyTeacherExchangeIntegralCode() {
        String code = getRequestString("code");
        if (code == null) {
            return MapMessage.errorMessage("验证码错误");
        }

        return smsServiceClient.getSmsService().verifyValidateCode(currentUserId(), code, SmsType.TEACHER_SMARTCLAZZ_EXCHANGE_INTEGRAL.name());
    }

    /**
     * 智慧教室
     * ----重置显示数据
     * 参数传递：clazzId,userIds
     */
    @RequestMapping(value = "resetstudentdisplay.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetStudentDisplay() {
        Long clazzId = ConversionUtils.toLong(getRequestLong("clazzId"));
        List<Long> userIdList = StringHelper.toLongList(getRequestString("userIds"));
        Teacher teacher = getSubjectSpecifiedTeacher();

        if (!hasClazzTeachingPermission(teacher.getId(), clazzId)) {
            logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazzId, currentUserId());
            return MapMessage.errorMessage("你没有该班级管理权限");
        }

        try {
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                return MapMessage.errorMessage("班级不存在");
            }
            GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);
            if (group == null) {
                return MapMessage.errorMessage("组信息不存在");
            }
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("SMARTCLAZZ_RESET:")
                    .keys(group.getId())
                    .proxy()
                    .resetSmartClazzStudentDisplay(group.getId(), userIdList);
        } catch (DuplicatedOperationException de) {
            return MapMessage.errorMessage("正常重置学生显示数据,请稍候再操作");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.formatMessage(e.getMessage(), e));
        }
    }


    /**
     * 智慧教室
     * ------发放记录
     * get方法参数传递 : clazzId
     * post参数传递 ：clazzId,startDate,endDate
     */
    @RequestMapping(value = "rewardhistory.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getRewardHistory(Model model) {
        Long clazzId = getRequestLong("clazzId");
        Teacher teacher = currentTeacher();
        if (!hasClazzTeachingPermission(teacher.getId(), clazzId)) {
            logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazzId, teacher.getId());
            return "redirect:/teacher/index.vpage";
        }

        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            logger.warn("该老师未认证,teacherId:{}", teacher.getId());
            return "redirect:/teacher/index.vpage";
        }

        if (isRequestGet()) {
            //今天的开始时间和结束时间
            model.addAttribute("todayStartDate", DateUtils.dateToString(DayRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATE));
            model.addAttribute("todayEndDate", DateUtils.dateToString(DayRange.current().getEndDate(), DateUtils.FORMAT_SQL_DATE));
            //昨天的开始时间和结束时间
            model.addAttribute("yesterdayStartDate", DateUtils.dateToString(DayRange.current().previous().getStartDate(), DateUtils.FORMAT_SQL_DATE));
            model.addAttribute("yesterdayEndDate", DateUtils.dateToString(DayRange.current().previous().getEndDate(), DateUtils.FORMAT_SQL_DATE));
            //本周的开始时间和结束时间
            model.addAttribute("currentWeekStartDate", DateUtils.dateToString(WeekRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATE));
            model.addAttribute("currentWeekEndDate", DateUtils.dateToString(WeekRange.current().getEndDate(), DateUtils.FORMAT_SQL_DATE));
            //本月的开始时间和结束时间
            model.addAttribute("currentMonthStartDate", DateUtils.dateToString(MonthRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATE));
            model.addAttribute("currentMonthEndDate", DateUtils.dateToString(MonthRange.current().getEndDate(), DateUtils.FORMAT_SQL_DATE));

            model.addAttribute("clazz", raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId));
            return "teacherv3/smartclazz/rewardhistory";
        }

        Date startDate = DateUtils.stringToDate(getRequestString("startDate"), DateUtils.FORMAT_SQL_DATE);
        Date endDate = DateUtils.stringToDate(getRequestString("endDate"), DateUtils.FORMAT_SQL_DATE);
        if (startDate == null || endDate == null) {
            logger.warn("查询发放记录时，参数开始时间和结束时间不能为空");
            return "teacherv3/smartclazz/rewardhistorychip";
        }
        //结束时间设在查询范围之内
        endDate = DateUtils.calculateDateDay(endDate, 1);

        model.addAttribute("tabName", getRequestString("tabName"));
        try {
            Map<String, Object> resultMap = atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("SMARTCLAZZ_HS:")
                    .keys(teacher.getId(), clazzId)
                    .proxy()
                    .findSmartClazzRewardHistory(clazzId, teacher.getId(), teacher.getSubject(), startDate, endDate);
            model.addAttribute("rewardhistoryList", resultMap.get("rewardhistoryList"));
            model.addAttribute("smartClazzRankList", resultMap.get("smartClazzRankList"));
            //发放总学豆量
            model.addAttribute("totalIntegral", ConversionUtils.toInt(resultMap.get("totalIntegral")));
        } catch (DuplicatedOperationException de) {
            return "teacherv3/smartclazz/rewardhistorychip";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "teacherv3/smartclazz/rewardhistorychip";
    }

    @RequestMapping(value = "iwen.vpage", method = RequestMethod.GET)
    public String iwen() {
        return "teacherv3/smartclazz/iwen";
    }

    /**
     * 课堂奖励--计时工具
     */
    @RequestMapping(value = "timer.vpage", method = RequestMethod.GET)
    public String timer(Model model) {
        long clazzId = getRequestLong("clazzId");

        //多学科支持
        Teacher teacher = getSubjectSpecifiedTeacher();
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
        model.addAttribute("specifiedSubjects", getSpecifiedSubjectsByTeacherIdAndClazzId(teacher.getId(), clazzId));
        model.addAttribute("clazz", raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId));
        return "teacherv3/smartclazz/timer";
    }

    /**
     * 智慧教室
     * ---我的题目
     */
    @RequestMapping(value = "myquestion.vpage", method = RequestMethod.GET)
    public String findSmartClazzQuestion(HttpServletRequest request, Model model) {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return "redirect:/teacher/smartclazz/list.vpage";
        }
        try {

            Long clazzId = conversionService.convert(request.getParameter("clazzId"), Long.class);

            if (!hasClazzTeachingPermission(teacher.getId(), clazzId)) {
                logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazzId, teacher.getId());
                return "redirect:/teacher/smartclazz/list.vpage";
            }

            model.addAttribute("clazz", raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId));
            //多学科支持
            model.addAttribute("curSubject", teacher.getSubject());
            model.addAttribute("curSubjectText", teacher.getSubject().getValue());
            model.addAttribute("specifiedSubjects", getSpecifiedSubjectsByTeacherIdAndClazzId(teacher.getId(), clazzId));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "redirect:/teacher/smartclazz/list.vpage";
        }
        return "teacherv3/smartclazz/iwen/myquestion";
    }

    /**
     * 智慧教室
     * ---获取我的题目
     * jsonMap包含KEYS：clazzId,pageNo
     */
    @RequestMapping(value = "getmyquestion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getmyquestion(@RequestBody Map<String, Object> jsonMap) {
        try {
            Teacher teacher = getSubjectSpecifiedTeacher(Subject.valueOf(SafeConverter.toString(jsonMap.get("subject"))));
            Long clazzId = conversionService.convert(jsonMap.get("clazzId"), Long.class);
            int pageNo = ConversionUtils.toInt(jsonMap.get("pageNo"), 1);
            Pageable pageable = new PageRequest(pageNo - 1, 10);
            GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);
            Page<SmartClazzQuestionLib> libPage = businessTeacherServiceClient
                    .findSmartClazzQuestionPage(group.getId(), pageable);
            return MapMessage.successMessage("获取成功").add("questionPage", libPage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.formatMessage(e.getMessage(), e));
        }
    }

//    /**
//     * 智慧教室
//     * ---添加题目
//     */
//    @RequestMapping(value = "addmyquestion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    public String addMyquestion(Model model) {
//        Teacher teacher = currentTeacher();
//
//        //多学科支持
//        model.addAttribute("curSubject", teacher.getSubject());
//        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
//        return "teacherv3/smartclazz/iwen/htmlchip/myquestionchip";
//    }


    /**
     * 智慧教室
     * ---老师创建题目
     * jsonMap包含KEYS：clazzId,topicContent,answer,options,questionType
     */
    @RequestMapping(value = "saveSmartClazzQuestion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSmartClazzQuestion(@RequestBody Map<String, Object> jsonMap) {
        String json = JsonUtils.toJson(jsonMap);
        /**
         * 过滤XSS攻击
         */
//        json = StringUtils.cleanXSS(json); //因业务需要前端可插入复文本内容
        jsonMap = JsonUtils.fromJson(json, Map.class);
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail(Subject.valueOf(SafeConverter.toString(jsonMap.get("subject"))));
        Long clazzId = ConversionUtils.toLong(jsonMap.get("clazzId"));
        if (!hasClazzTeachingPermission(teacher.getId(), clazzId)) {
            logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazzId, teacher.getId());
            return MapMessage.errorMessage("你没有该班级管理权限");
        }

        try {
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("SMARTCLAZZ_QUESTION:")
                    .keys(teacher.getSubject(), clazzId)
                    .proxy()
                    .saveSmartClazzQuestion(teacher, teacher, jsonMap);
        } catch (DuplicatedOperationException de) {
            return MapMessage.errorMessage("正在创建题目,请稍候操作");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.formatMessage(e.getMessage(), e));
        }
    }


    /**
     * 智慧教室
     * ---获取用户下班级与指定题不存在关系的班级
     */
    @RequestMapping(value = "findclazzquestionref.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findclazzquestionref(@RequestBody Map<String, Object> jsonMap) {
        try {
            List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(currentUserId()).stream()
                    .filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .collect(Collectors.toList());

            String questionId = conversionService.convert(jsonMap.get("questionId"), String.class);
            if (StringUtils.isBlank(questionId)) {
                return MapMessage.errorMessage("参数不对");
            }
            List<SmartClazzQuestionRef> refList = businessTeacherServiceClient.findSmartClazzQuestionRefByQId(questionId);
            Set<Long> clazzSet = new LinkedHashSet<>();
            for (SmartClazzQuestionRef ref : refList) {
                if (!ref.isDisabledTrue()) {
                    clazzSet.add(ref.getClazzId());
                }
            }
            List<Map<String, Object>> noRefClazz = new LinkedList<>();
            for (Clazz clazz : clazzList) {
                if (!clazzSet.contains(clazz.getId())) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", clazz.getId());
                    map.put("clazzName", clazz.formalizeClazzName());
                    noRefClazz.add(map);
                }
            }
            return MapMessage.successMessage("").add("noRefClazz", noRefClazz);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.formatMessage(e.getMessage(), e));
        }
    }


    /**
     * 智慧教室
     * ---增加班级与题的关系
     */
    @RequestMapping(value = "saveclazzquestionref.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveclazzquestionref(@RequestBody Map<String, Object> jsonMap) {
        try {
            String questionId = conversionService.convert(jsonMap.get("questionId"), String.class);
            String clazzIdStr = conversionService.convert(jsonMap.get("clazzIds"), String.class);
            String[] clazzIds = StringUtils.split(clazzIdStr, ",");
            if (StringUtils.isBlank(questionId)) {
                return MapMessage.errorMessage("题ID不能为空");
            }
            if (clazzIds == null) {
                return MapMessage.errorMessage("请选择班级");
            }
            List<Long> clazzList = new LinkedList<>();
            for (String id : clazzIds) {
                CollectionUtils.addNonNullElement(clazzList, conversionService.convert(id, Long.class));
            }
            Teacher teacher = getSubjectSpecifiedTeacher(Subject.valueOf(SafeConverter.toString(jsonMap.get("subject"))));
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("SMARTCLAZZ_QUESTION_REF:")
                    .keys(teacher.getId(), questionId)
                    .proxy()
                    .addSmartClazzQuestionRef(teacher, questionId, clazzList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.join("更新失败，", e.getMessage()));
        }
    }


    /**
     * 智慧教室
     * ---删除班级与题目的关系
     * jsonMap包含KEYS：id : 班级与题目的主键ID
     */
    @RequestMapping(value = "deleteclazzquestionref.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteclazzquestionref(@RequestBody Map<String, Object> jsonMap) {
        try {
            Long teacherId = getSubjectSpecifiedTeacherId(Subject.valueOf(SafeConverter.toString(jsonMap.get("subject"))));
            String questionId = ConversionUtils.toString(jsonMap.get("questionId"));
            Long clazzId = conversionService.convert(jsonMap.get("clazzId"), Long.class);
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("SMARTCLAZZ_QUESTION_REF:")
                    .keys(teacherId, questionId)
                    .proxy()
                    .disabledSmartClazzQuestionRef(teacherId, clazzId, questionId);
        } catch (DuplicatedOperationException de) {
            return MapMessage.errorMessage("正在处理请求,请稍候操作");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.formatMessage(e.getMessage(), e));
        }
    }


    /**
     * 智慧教室
     * ---班级题目扫描
     * 参数qId,clazzId,pageNo
     */
    @RequestMapping(value = "questionscan.vpage", method = RequestMethod.GET)
    public String questionScan(HttpServletRequest request, Model model) {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return "redirect:/teacher/smartclazz/list.vpage";
        }
        // 题目ID
        String qId = getRequestString("qId");
        Long clazzId = conversionService.convert(request.getParameter("clazzId"), Long.class);
        if (!hasClazzTeachingPermission(teacher.getId(), clazzId)) {
            logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazzId, teacher.getId());
            return "redirect:/teacher/smartclazz/list.vpage";
        }

        try {
            Map<String, Object> paramMap = new LinkedHashMap<>();
            paramMap.put("uid", teacher.getId());
            paramMap.put("token", openApiAuth.generateSign(teacher.getId()));
            paramMap.put("clazzId", clazzId);
            model.addAttribute("clazz", raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId));
            model.addAttribute("param", JsonUtils.toJson(paramMap));
            model.addAttribute("studentList", userAggregationLoaderClient.loadTeacherStudentsByClazzId(clazzId, teacher.getId()));
            if (StringUtils.isNotBlank(qId)) {
                //表示题在记录中是第几道
                model.addAttribute("questionIndex", ConversionUtils.toInt(request.getParameter("questionIndex"), 1));
                //总共多少个题
                model.addAttribute("total", ConversionUtils.toInt(request.getParameter("totalEle"), 1));
                model.addAttribute("questionId", qId);
            }
            //多学科支持
            model.addAttribute("curSubject", teacher.getSubject());
            model.addAttribute("curSubjectText", teacher.getSubject().getValue());
            model.addAttribute("specifiedSubjects", getSpecifiedSubjectsByTeacherIdAndClazzId(teacher.getId(), clazzId));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "redirect:/teacher/smartclazz/list.vpage";
        }
        return "teacherv3/smartclazz/iwen/questionscan";
    }


    /**
     * 智慧教室
     * ---获取题的详细信息
     * 参数qId 或 clazzId,pageNo
     */
    @RequestMapping(value = "getquestion.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String getQuestionInfo(HttpServletRequest request, Model model) {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            model.addAttribute("msg", "非认证用户，无权限使用");
            return "teacherv3/smartclazz/iwen/htmlchip/singlequestionchip";
        }
        // 题目ID
        String qId = getRequestString("qId");
        SmartClazzQuestionLib question = null;
        if (StringUtils.isBlank(qId)) {
            Long clazzId = conversionService.convert(request.getParameter("clazzId"), Long.class);
            int pageNo = getRequestInt("pageNo", 1);
            pageNo = (pageNo < 1 ? 1 : pageNo);
            Pageable pageable = new PageRequest(pageNo - 1, 1);
            GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);
            Page<SmartClazzQuestionLib> libPage = businessTeacherServiceClient
                    .findSmartClazzQuestionPage(group.getId(), pageable);
            if (libPage != null && CollectionUtils.isNotEmpty(libPage.getContent())) {
                question = libPage.getContent().get(0);
            }
        } else {
            List<SmartClazzQuestionLib> questionList = businessTeacherServiceClient
                    .findSmartClazzQuestionById(Collections.singleton(qId));
            if (CollectionUtils.isNotEmpty(questionList)) {
                question = questionList.get(0);
            }
        }
        model.addAttribute("question", question);
        return "teacherv3/smartclazz/iwen/htmlchip/singlequestionchip";
    }


    /**
     * 智慧教室
     * ---答题详情
     */
    @RequestMapping(value = "getanswerdetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAnswerDetail(HttpServletRequest request) {
        Long clazzId = conversionService.convert(request.getParameter("clazzId"), Long.class);
        Map dataMap = null;
        if (clazzId != null) {
            String key = StringUtils.join(new Object[]{"SMARTCLAZZ_SCAN_ANSWER", currentUserId(), clazzId}, ":");
            CacheObject<Map> cacheObject = washingtonCacheSystem.CBS.unflushable.get(key);
            if (cacheObject == null) {
                return MapMessage.errorMessage();
            }
            dataMap = cacheObject.getValue();
        }
        if (dataMap == null) {
            dataMap = new LinkedHashMap();
        }
        return MapMessage.successMessage("获取成功").add("detail", dataMap);
    }


    /**
     * 清除题答案的缓存
     */
    @RequestMapping(value = "resetdatacache.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetdatacache(HttpServletRequest request) {
        Long clazzId = conversionService.convert(request.getParameter("clazzId"), Long.class);
        if (clazzId != null) {
            String key = StringUtils.join(new Object[]{"SMARTCLAZZ_SCAN_ANSWER", currentUserId(), clazzId}, ":");
            Boolean ret = washingtonCacheSystem.CBS.unflushable.delete(key);
            if (!Boolean.TRUE.equals(ret)) {
                return MapMessage.errorMessage();
            }
        }
        return MapMessage.successMessage("删除答案成功");
    }


    /**
     * 智慧教室
     * ---生成题的报告
     */
    @RequestMapping(value = "savequestionreport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateQuestionReport(@RequestBody Map<String, Object> jsonMap) {
        try {
            Teacher teacher = currentTeacher();
            SmartClazzQuestionReport smartClazzQuestionReport = JsonUtils.fromJson(JsonUtils.toJson(jsonMap), SmartClazzQuestionReport.class);
            if (smartClazzQuestionReport == null) {
                return MapMessage.errorMessage("param must not be null");
            }
            //设置学科
            smartClazzQuestionReport.setSubject(teacher.getSubject());
            //设置老师分组
            GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), smartClazzQuestionReport.getClazzId(), false);
            smartClazzQuestionReport.setGroupId(group.getId());
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("SMARTCLAZZ_QUESTION_REPORT:")
                    .keys(currentUserId(), smartClazzQuestionReport.getQuestionId())
                    .proxy()
                    .generateSmartClazzQuestionReport(currentUserId(), smartClazzQuestionReport);
        } catch (DuplicatedOperationException de) {
            return MapMessage.errorMessage("正在生成报告,请稍候操作");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.formatMessage(e.getMessage(), e));
        }
    }


    /**
     * 爱提问
     * ---我的报告
     */
    @RequestMapping(value = "questionreport.vpage", method = RequestMethod.GET)
    public String questionReport(Model model) {
        Long clazzId = getRequestLong("clazzId");
        model.addAttribute("clazz", raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId));

        //今天的开始时间和结束时间
        model.addAttribute("todayStartDate", DateUtils.dateToString(DayRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATE));
        model.addAttribute("todayEndDate", DateUtils.dateToString(DayRange.current().getEndDate(), DateUtils.FORMAT_SQL_DATE));
        //昨天的开始时间和结束时间
        model.addAttribute("yesterdayStartDate", DateUtils.dateToString(DayRange.current().previous().getStartDate(), DateUtils.FORMAT_SQL_DATE));
        model.addAttribute("yesterdayEndDate", DateUtils.dateToString(DayRange.current().previous().getEndDate(), DateUtils.FORMAT_SQL_DATE));
        //本周的开始时间和结束时间
        model.addAttribute("currentWeekStartDate", DateUtils.dateToString(WeekRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATE));
        model.addAttribute("currentWeekEndDate", DateUtils.dateToString(WeekRange.current().getEndDate(), DateUtils.FORMAT_SQL_DATE));
        //本月的开始时间和结束时间
        model.addAttribute("currentMonthStartDate", DateUtils.dateToString(MonthRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATE));
        model.addAttribute("currentMonthEndDate", DateUtils.dateToString(MonthRange.current().getEndDate(), DateUtils.FORMAT_SQL_DATE));

        //多学科支持
        Teacher teacher = getSubjectSpecifiedTeacher();
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
        model.addAttribute("specifiedSubjects", getSpecifiedSubjectsByTeacherIdAndClazzId(teacher.getId(), clazzId));

        return "teacherv3/smartclazz/iwen/questionreport";
    }

    /**
     * 智慧教室
     * ---我的报告内容
     */
    @RequestMapping(value = "findquestionreport.vpage", method = RequestMethod.POST)
    public String findQuestionReport(HttpServletRequest request, Model model) {
        try {
            Teacher teacher = getSubjectSpecifiedTeacher();
            Long clazzId = conversionService.convert(request.getParameter("clazzId"), Long.class);
            Date startDate = DateUtils.stringToDate(request.getParameter("startDate"), DateUtils.FORMAT_SQL_DATE);
            Date endDate = DateUtils.stringToDate(request.getParameter("endDate"), DateUtils.FORMAT_SQL_DATE);
            if (startDate == null || endDate == null) {
                model.addAttribute("msg", "开始时间和结束时间不能为空");
            } else {
                endDate = DayRange.newInstance(endDate.getTime()).getEndDate();
                int pageNo = ConversionUtils.toInt(request.getParameter("pageNo"), 1);
                pageNo = (pageNo < 1 ? 1 : pageNo);
                Pageable pageable = new PageRequest(pageNo - 1, 10);
                GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);
                Page<SmartClazzQuestionReport> reports = businessTeacherServiceClient
                        .findSmartClazzQuestionReport(group.getId(), startDate, endDate, pageable);
                model.addAttribute("reports", reports);
                //题的详情
                Map<String, SmartClazzQuestionLib> libMap = new LinkedHashMap<>();
                //答对学生 key:题ID,value:答对的学生列表
                Map<String, List<SmartClazzStudentResult>> questionMap = new LinkedHashMap<>();
                if (reports != null && CollectionUtils.isNotEmpty(reports.getContent())) {
                    //题的id
                    Set<String> qIdSet = new LinkedHashSet<>();

                    for (SmartClazzQuestionReport report : reports.getContent()) {
                        if (CollectionUtils.isEmpty(report.getStudents())) {
                            continue;
                        }
                        //答对学生列表
                        List<SmartClazzStudentResult> results = new LinkedList<>();
                        for (SmartClazzStudentResult result : report.getStudents()) {
                            if (StringUtils.equals(result.getStudentAnswer(), report.getAnswer())) {
                                results.add(result);
                            }
                        }
                        questionMap.put(report.getQuestionId(), results);
                        CollectionUtils.addNonNullElement(qIdSet, report.getQuestionId());
                    }

                    List<SmartClazzQuestionLib> questionList = businessTeacherServiceClient.findSmartClazzQuestionById(qIdSet);

                    if (CollectionUtils.isNotEmpty(questionList)) {
                        for (SmartClazzQuestionLib lib : questionList) {
                            libMap.put(lib.getId(), lib);
                        }
                    }
                }
                model.addAttribute("questionMap", libMap);
                model.addAttribute("rightStudents", questionMap);
            }
            model.addAttribute("clazzId", clazzId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("msg", StringUtils.formatMessage("获取数据失败，失败原因：{},请稍后再试", e.getMessage()));
        }
        return "teacherv3/smartclazz/iwen/htmlchip/report";
    }

    /**
     * 获取某个报告的学生详情
     */
    @RequestMapping(value = "studentanswer.vpage", method = RequestMethod.GET)
    public String findStudentanswer(HttpServletRequest request, Model model) {
        try {
            Teacher teacher = currentTeacher();
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
                model.addAttribute("msg", "非认证用户，无权限使用");
                return "redirect:/teacher/smartclazz/list.vpage";
            }
            Long clazzId = conversionService.convert(request.getParameter("clazzId"), Long.class);
            if (!hasClazzTeachingPermission(currentUserId(), clazzId)) {
                logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazzId, teacher.getId());
                return "redirect:/teacher/smartclazz/list.vpage";
            }
            //题报告的ID
            String reportId = request.getParameter("id");
            SmartClazzQuestionReport report = businessTeacherServiceClient.findQuestionReportById(reportId);
            model.addAttribute("report", report);
            model.addAttribute("clazz", raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("msg", StringUtils.formatMessage("获取数据失败，失败原因：{},请稍后再试", e.getMessage()));
        }
        return "teacherv3/smartclazz/iwen/studentanswer";
    }


    /**
     * 爱提问
     * ----学生卡片
     */
    @RequestMapping(value = "iwencard.vpage", method = RequestMethod.GET)
    public String iwenCard(Model model) {
        Long clazzId = getRequestLong("clazzId");
        //初始化卡号
        MapMessage mapMessage;
        try {
            Teacher teacher = getSubjectSpecifiedTeacher();
            Long teacherId = teacher.getId();
            if (!hasClazzTeachingPermission(teacherId, clazzId)) {
                logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazzId, teacherId);
                return "redirect:/teacher/smartclazz/list.vpage";
            }
            // 只获取id，减少传输数据量
            List<Long> studentList = userAggregationLoaderClient.loadTeacherStudentIdsByClazzId(clazzId, teacherId);
            //卡片只有312张
            if (CollectionUtils.isEmpty(studentList)) {
                model.addAttribute("message", "班级没有学生,请去班级管理添加学生");
            } else if (studentList.size() > 312) {
                model.addAttribute("message", "班级人数太多");
            } else {
                //初始化卡号
                mapMessage = atomicLockManager.wrapAtomic(clazzServiceClient)
                        .keyPrefix("SMARTCLAZZ_CARD:")
                        .keys(clazzId)
                        .proxy()
                        .initClazzStudentCard(clazzId, teacherId);
                List<ClazzStudentCardMapper> mapperList = (List<ClazzStudentCardMapper>) mapMessage.get("studentCardList");
                if (mapMessage.isSuccess() && CollectionUtils.isNotEmpty(mapperList)) {

                    Collections.sort(mapperList, new Comparator<ClazzStudentCardMapper>() {
                        @Override
                        public int compare(ClazzStudentCardMapper o1, ClazzStudentCardMapper o2) {
                            int card1 = ConversionUtils.toInt(o1.getCardNo());
                            int card2 = ConversionUtils.toInt(o2.getCardNo());
                            return card1 - card2;
                        }
                    });
                    model.addAttribute("maxCardNo", mapperList.get(mapperList.size() - 1).getCardNo());
                    model.addAttribute("studentCardList", mapperList);
                } else {
                    model.addAttribute("message", mapMessage.getInfo());
                }
            }
            //多学科支持
            model.addAttribute("curSubject", teacher.getSubject());
            model.addAttribute("curSubjectText", teacher.getSubject().getValue());
            model.addAttribute("specifiedSubjects", getSpecifiedSubjectsByTeacherIdAndClazzId(teacher.getId(), clazzId));

        } catch (Exception e) {
            //ignore
        }
        model.addAttribute("clazz", raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId));
        return "teacherv3/smartclazz/iwen/iwencard";
    }

    /**
     * 爱提问
     * -- 使用帮助
     */
    @RequestMapping(value = "help.vpage", method = RequestMethod.GET)
    public String iwenHelp(Model model) {
        long clazzId = getRequestLong("clazzId");

        //多学科支持
        Teacher teacher = getSubjectSpecifiedTeacher();
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
        model.addAttribute("specifiedSubjects", getSpecifiedSubjectsByTeacherIdAndClazzId(teacher.getId(), clazzId));

        model.addAttribute("clazz", raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId));
        return "teacherv3/smartclazz/iwen/help";
    }


}
