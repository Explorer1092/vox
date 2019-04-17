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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.lang.util.RealnameRule;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.SchoolAmbassadorSource;
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.business.consumer.BusinessTeacherServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupClazzMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.OpenApiReturnCode.*;

/**
 * Created by Summer Yang on 2015/7/3.
 */
@Controller
@RequestMapping(value = "/open/wechat/teacher/activity")
@Slf4j
public class WechatTeacherActivityController extends AbstractOpenController {

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;

    @Inject private BusinessTeacherServiceClient businessTeacherServiceClient;

//    @Inject
//    private CampaignLoaderClient campaignLoaderClient;
//
//    // 开学大礼包  是否显示活动卡片
//    @RequestMapping(value = "showcard.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public OpenAuthContext showCard(HttpServletRequest request) {
//        OpenAuthContext openAuthContext = getOpenAuthContext(request);
//        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
//        if (teacherId == 0L) {
//            openAuthContext.setCode(SYSTEM_ERROR_CODE);
//            return openAuthContext;
//        }
//        try {
//            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//            if (teacher == null) {
//                openAuthContext.setCode(SYSTEM_ERROR_CODE);
//                openAuthContext.setError("用户不存在");
//                return openAuthContext;
//            }
//            //是否显示老师开学大礼包卡片 在活动期间内
//            boolean showTermBeginCard = false;
//            if (RuntimeMode.lt(Mode.STAGING) || NewSchoolYearActivity.isInTermBeginPeriod()) {
//                showTermBeginCard = true;
//            }
//            openAuthContext.add("showTermBeginCard", showTermBeginCard);
//            openAuthContext.setCode(SUCCESS_CODE);
//        } catch (Exception ex) {
//            log.error(ex.getMessage(), ex);
//            openAuthContext.setCode(SYSTEM_ERROR_CODE);
//        }
//        return openAuthContext;
//    }
//
//    // 开学大礼包  微信版
//    @RequestMapping(value = "termbeginindex.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public OpenAuthContext termBeginIndex(HttpServletRequest request) {
//        OpenAuthContext openAuthContext = getOpenAuthContext(request);
//        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
//        if (teacherId == 0L) {
//            openAuthContext.setCode(SYSTEM_ERROR_CODE);
//            return openAuthContext;
//        }
//        try {
//            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//            if (teacher == null) {
//                openAuthContext.setCode(SYSTEM_ERROR_CODE);
//                openAuthContext.setError("用户不存在");
//                return openAuthContext;
//            }
//            //抽奖情况
//            int freeChance = miscServiceClient.getTeacherLotteryFreeChance(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2016_SUMMER, teacher.getId());
//            List<Map<String, Object>> campaignLotteryResultsBig = miscLoaderClient.loadRecentCampaignLotteryResultBig(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2016_SUMMER.getId());
//            //今天的不显示
//            campaignLotteryResultsBig = campaignLotteryResultsBig.stream().filter(b -> b.get("datetime") == null ||
//                    DayRange.current().getStartDate().after((Date) b.get("datetime"))).collect(Collectors.toList());
//
//            Map<Integer, CampaignLottery> lotteryMap = campaignLoaderClient.findCampaignLotteries(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2016_SUMMER.getId())
//                    .stream()
//                    .filter(e -> e.getAwardId() != null)
//                    .collect(Collectors.groupingBy(CampaignLottery::getAwardId))
//                    .values()
//                    .stream()
//                    .map(e -> e.iterator().next())
//                    .collect(Collectors.toMap(CampaignLottery::getAwardId, Function.identity()));
//
//
//            List<CampaignLotteryHistory> histories = campaignLoaderClient.findCampaignLotteryHistories(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2016_SUMMER.getId(), teacher.getId());
//            List<Map<String, Object>> myHistory = new ArrayList<>();
//            for (CampaignLotteryHistory history : histories) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("lotteryDate", history.getCreateDatetime());
//                map.put("awardName", lotteryMap.get(history.getAwardId()).getAwardName());
//                myHistory.add(map);
//            }
//            openAuthContext.add("freeChance", freeChance);
//            openAuthContext.add("campaignLotteryResultsBig", campaignLotteryResultsBig);
//            openAuthContext.add("myHistory", myHistory);
//            // 完成寒假作业的学生数
//            int vacationCount = businessTeacherServiceClient.loadTeacherFinishVacationHomeworkCount(teacher);
//            openAuthContext.add("vacationCount", vacationCount);
//            boolean isReward = homeworkCacheClient.getVhRewardCacheManager().isReward(teacherId);
//            openAuthContext.add("isReward", isReward);
//            boolean hasAdjustClazz = businessCacheClient.getTeacherAdjustClazzRemindCacheManager().done(teacherId);
//            openAuthContext.add("hasAdjustClazz", hasAdjustClazz);
//            openAuthContext.setCode(SUCCESS_CODE);
//        } catch (Exception ex) {
//            log.error(ex.getMessage(), ex);
//            openAuthContext.setCode(SYSTEM_ERROR_CODE);
//        }
//        return openAuthContext;
//    }
//
//    // 暑假大礼包  抽奖
//    @RequestMapping(value = "dolottery.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public OpenAuthContext doLottery(HttpServletRequest request) {
//        OpenAuthContext openAuthContext = getOpenAuthContext(request);
//        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
//        if (teacherId == 0L) {
//            openAuthContext.setCode(SYSTEM_ERROR_CODE);
//            openAuthContext.setError("请刷新页面重试");
//            return openAuthContext;
//        }
//        try {
//            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//            if (teacher == null) {
//                openAuthContext.setCode(BUSINESS_ERROR_CODE);
//                openAuthContext.setError("用户不存在");
//                return openAuthContext;
//            }
//            if (RuntimeMode.gt(Mode.STAGING) && !NewSchoolYearActivity.isInTermBeginPeriod()) {
//                openAuthContext.setCode(BUSINESS_ERROR_CODE);
//                openAuthContext.setError("活动未开始或已过期");
//                return openAuthContext;
//            }
//            try {
//                MapMessage message = atomicLockManager.wrapAtomic(miscServiceClient)
//                        .expirationInSeconds(30)
//                        .keyPrefix("TEACHER_TERM_BEGIN_LOTTERY_DO")
//                        .keys(teacher.getId())
//                        .proxy()
//                        .drawLottery(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2016_SUMMER, teacher, LotteryClientType.WECHAT);
//                if (message.isSuccess()) {
//                    openAuthContext.setCode(SUCCESS_CODE);
//                    openAuthContext.add("message", message);
//                } else {
//                    openAuthContext.setCode(BUSINESS_ERROR_CODE);
//                    openAuthContext.setError(message.getInfo());
//                }
//            } catch (DuplicatedOperationException ex) {
//                openAuthContext.setCode(BUSINESS_ERROR_CODE);
//                openAuthContext.setError("您点击太快了，请重试");
//                return openAuthContext;
//            }
//        } catch (Exception ex) {
//            log.error(ex.getMessage(), ex);
//            openAuthContext.setCode(SYSTEM_ERROR_CODE);
//            openAuthContext.setError("出错了，请重试");
//        }
//        return openAuthContext;
//    }
//
//    // 领取假期作业礼包
//    @RequestMapping(value = "receivevhreward.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public OpenAuthContext receiveVacationReward(HttpServletRequest request) {
//        OpenAuthContext openAuthContext = getOpenAuthContext(request);
//        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
//        if (teacherId == 0L) {
//            openAuthContext.setCode(SYSTEM_ERROR_CODE);
//            openAuthContext.setError("用户不存在");
//            return openAuthContext;
//        }
//        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//        if (teacher == null) {
//            openAuthContext.setCode(BUSINESS_ERROR_CODE);
//            openAuthContext.setError("用户不存在");
//            return openAuthContext;
//        }
//        if (RuntimeMode.gt(Mode.STAGING) && !NewSchoolYearActivity.isInTermBeginPeriod()) {
//            openAuthContext.setCode(BUSINESS_ERROR_CODE);
//            openAuthContext.setError("活动未开始或已过期");
//            return openAuthContext;
//        }
//
//        if (homeworkCacheClient.getVhRewardCacheManager().isReward(teacher.getId())) {
//            openAuthContext.setCode(BUSINESS_ERROR_CODE);
//            openAuthContext.setError("你已经领取过了，请不要重复领取");
//            return openAuthContext;
//        }
//        try {
//            MapMessage message = atomicLockManager.wrapAtomic(businessTeacherServiceClient)
//                    .expirationInSeconds(30)
//                    .keyPrefix("TEACHER_TERM_BEGIN_RECEIVE_REWARD")
//                    .keys(teacher.getId())
//                    .proxy()
//                    .receiveVacationHomeworkReward(teacher);
//            if (message.isSuccess()) {
//                openAuthContext.setCode(SUCCESS_CODE);
//            } else {
//                openAuthContext.setCode(BUSINESS_ERROR_CODE);
//                openAuthContext.setError(message.getInfo());
//            }
//        } catch (DuplicatedOperationException ex) {
//            openAuthContext.setCode(BUSINESS_ERROR_CODE);
//            openAuthContext.setError("您点击太快了，请重试");
//            return openAuthContext;
//        }
//        return openAuthContext;
//    }
//
//
//    // 暑假大礼包 记录点击不调整班级按钮
//    @RequestMapping(value = "recordadjust.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public OpenAuthContext recordAdjust(HttpServletRequest request) {
//        OpenAuthContext openAuthContext = getOpenAuthContext(request);
//        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
//        if (teacherId == 0L) {
//            openAuthContext.setCode(SYSTEM_ERROR_CODE);
//            return openAuthContext;
//        }
//        try {
//            //记录点击调整按钮
//            businessCacheClient.getTeacherAdjustClazzRemindCacheManager().record(teacherId);
//            openAuthContext.setCode(SUCCESS_CODE);
//        } catch (Exception ex) {
//            log.error(ex.getMessage(), ex);
//            openAuthContext.setCode(SYSTEM_ERROR_CODE);
//        }
//        return openAuthContext;
//    }


    // 感恩节 获取老师所有的组
    @RequestMapping(value = "getteacherclazzs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getTeacherClazzs(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
        if (teacherId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            return openAuthContext;
        }
        try {
            // 获取老师名下所有组
            List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);
            clazzs = clazzs.stream().filter(c -> c.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED).collect(Collectors.toList());
            List<Map<String, Object>> clazzInfo = new ArrayList<>();
            for (Clazz clazz : clazzs) {
                Map<String, Object> clazzMap = new HashMap<>();
                clazzMap.put("clazzId", clazz.getId());
                clazzMap.put("clazzName", clazz.formalizeClazzName());
                clazzInfo.add(clazzMap);
            }
            openAuthContext.add("clazzInfo", clazzInfo);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
        }
        return openAuthContext;
    }

    // 是否显示校园大使申请活动
    @RequestMapping(value = "showambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext showAmbassadorCard(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
        if (teacherId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            return openAuthContext;
        }
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacher == null) {
                openAuthContext.setCode(SYSTEM_ERROR_CODE);
                openAuthContext.setError("用户不存在");
                return openAuthContext;
            }
            boolean showAmbassador = false;
            AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), teacher.getTeacherSchoolId());
            if (ref == null && teacher.getSubject() != Subject.CHINESE) {
                showAmbassador = true;
            }
            openAuthContext.add("showAmbassador", showAmbassador);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
        }
        return openAuthContext;
    }

    // 是否辞任过大使
    @RequestMapping(value = "havebeambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext haveBeAmbassador(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
        if (teacherId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            return openAuthContext;
        }
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacher == null) {
                openAuthContext.setCode(SYSTEM_ERROR_CODE);
                openAuthContext.setError("用户不存在");
                return openAuthContext;
            }
            // 是否90天内辞任过大使
            boolean haveBeAmbassador = ambassadorLoaderClient.getAmbassadorLoader().haveBeAmbassador(teacher.getId());
            openAuthContext.add("haveBeAmbassador", haveBeAmbassador);
            // 申请时间做过滤
            // 1 2月不开放
            int month = MonthRange.current().getMonth();
            boolean inWinter = false;
            if (month == 1 || month == 2) {
                inWinter = true;
            }
            openAuthContext.add("inWinter", inWinter);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
        }
        return openAuthContext;
    }

    /**
     * 校园大使申请提交
     */
    @RequestMapping(value = "schoolambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext schoolAmbassador(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
        String ambassadorInfo = ConversionUtils.toString(openAuthContext.getParams().get("ambassadorInfo"));
        if (teacherId == 0L || StringUtils.isBlank(ambassadorInfo)) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            return openAuthContext;
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("用户不存在");
            return openAuthContext;
        }

        boolean haveBeAmbassador = ambassadorLoaderClient.getAmbassadorLoader().haveBeAmbassador(teacher.getId());
        if (haveBeAmbassador) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("您先前辞任过大使，目前尚无法申请，谢谢。");
            return openAuthContext;
        }

        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
        int teacherLevel = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevel());

        // 前置条件 认证老师 & >=Lv2
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS || teacherLevel < 2) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("对不起，您还不满足申请条件，请满足后再来申请！");
            return openAuthContext;
        }
        // 手动虚假老师直接返回
        CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacher.getId());
        if (teacherSummary != null && SafeConverter.toBoolean(teacherSummary.getFakeTeacher()) && CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(teacherSummary.getValidationType())) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("对不起，您目前无法申请！");
            return openAuthContext;
        }
        Map<String, Object> ambassadorInfoMap = JsonUtils.fromJson(ambassadorInfo);
        String name = ConversionUtils.toString(ambassadorInfoMap.get("name"));
        String mobile = ConversionUtils.toString(ambassadorInfoMap.get("mobile"));
        String qq = ConversionUtils.toString(ambassadorInfoMap.get("qq"));
        String leader = ConversionUtils.toString(ambassadorInfoMap.get("leader"));
        String gender = ConversionUtils.toString(ambassadorInfoMap.get("gender"));
        String address = ConversionUtils.toString(ambassadorInfoMap.get("address"));
        String pname = ConversionUtils.toString(ambassadorInfoMap.get("pname"));
        String cname = ConversionUtils.toString(ambassadorInfoMap.get("cname"));
        String aname = ConversionUtils.toString(ambassadorInfoMap.get("aname"));
        Integer englishCount = ConversionUtils.toInt(ambassadorInfoMap.get("englishCount"));
        Integer mathCount = ConversionUtils.toInt(ambassadorInfoMap.get("mathCount"));
        Integer chineseCount = ConversionUtils.toInt(ambassadorInfoMap.get("chineseCount"));
        Integer clazzCount = ConversionUtils.toInt(ambassadorInfoMap.get("clazzCount"));
        String eduSystemType = ConversionUtils.toString(ambassadorInfoMap.get("eduSystemType"));
        String source = ConversionUtils.toString(ambassadorInfoMap.get("source"));
        Integer bYear = ConversionUtils.toInt(ambassadorInfoMap.get("bYear"));
        Integer tYear = ConversionUtils.toInt(ambassadorInfoMap.get("tYear"));
        Integer bMonth = ConversionUtils.toInt(ambassadorInfoMap.get("bMonth"));
        Integer bDay = ConversionUtils.toInt(ambassadorInfoMap.get("bDay"));
        boolean isFx = ConversionUtils.toBool(ambassadorInfoMap.get("isFx"));
        String fxClass = ConversionUtils.toString(ambassadorInfoMap.get("fxClass"));
        String schoolName = ConversionUtils.toString(ambassadorInfoMap.get("schoolName"));
        String schoolLevel = ConversionUtils.toString(ambassadorInfoMap.get("schoolLevel"));
        Integer oneClazzStudentCountBegin = ConversionUtils.toInt(ambassadorInfoMap.get("oneClazzStudentCountBegin"));
        Integer oneClazzStudentCountEnd = ConversionUtils.toInt(ambassadorInfoMap.get("oneClazzStudentCountEnd"));
        try {
            MapMessage message = atomicLockManager.wrapAtomic(this)
                    .expirationInSeconds(30)
                    .keyPrefix("APPLY_AMBASSADOR")
                    .keys(teacherId)
                    .proxy()
                    .applySchoolAmbassador(teacher, name, mobile, qq, "", leader,
                            0, 0, null, gender, address,
                            englishCount, mathCount, chineseCount, 0, clazzCount, eduSystemType, source,
                            pname, cname, aname, bYear, tYear, bMonth, bDay, isFx, fxClass, schoolName, schoolLevel,
                            oneClazzStudentCountBegin, oneClazzStudentCountEnd, 0, 0);
            if (message.isSuccess()) {
                // 申请成功 直接设置为大使 判断有没有同学科的大使
                AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), teacher.getTeacherSchoolId());
                if (ref == null) {
                    //直接为本校设置校园大使
                    message = businessTeacherServiceClient.setAmbassador(teacher);
                    if (message.isSuccess()) {
                        openAuthContext.setCode(SUCCESS_CODE);
                        return openAuthContext;
                    } else {
                        openAuthContext.setCode(SYSTEM_ERROR_CODE);
                        openAuthContext.setError(message.getInfo());
                        return openAuthContext;
                    }
                } else {
                    openAuthContext.setCode(SYSTEM_ERROR_CODE);
                    openAuthContext.setError("已经存在同学科校园大使");
                    return openAuthContext;
                }
            } else {
                openAuthContext.setCode(SYSTEM_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
                return openAuthContext;
            }
        } catch (DuplicatedOperationException ignore) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("您点击太快了，请重试");
            return openAuthContext;
        }
    }

    // 福袋活动 老师端
    @RequestMapping(value = "loadluckybagclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadLuckyBagClazz(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"));
        if (teacherId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            return openAuthContext;
        }
        try {
            // 没有班级的校验

            Map<String, Object> data = teacherSystemClazzServiceClient
                    .loadSystemClazzManagementIndexData(teacherId);
            List<GroupClazzMapper> teachClazzs = (List<GroupClazzMapper>) data.get("teachClazzs");
            List<Map<String, Object>> result = new ArrayList<>();
            for (GroupClazzMapper teachClazz : teachClazzs) {
                long clazzId = teachClazz.getClazzId();
                String clazzName = teachClazz.getClazzName();
                long groupId = teachClazz.getGroupId();
                result.add(MiscUtils.m("clazzId", clazzId, "clazzName", clazzName, "groupId", groupId));
            }
            openAuthContext.add("clazzs", result);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
        }
        return openAuthContext;
    }

    // 福袋活动 老师端
    @RequestMapping(value = "loadgroupluckybaginfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadGroupLuckyBagInfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"));
        Long groupId = SafeConverter.toLong(openAuthContext.getParams().get("groupId"));
        if (teacherId == 0L || groupId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            return openAuthContext;
        }
        try {
            Map<String, Object> indexData = businessTeacherServiceClient.loadTeacherLuckyBagInfo(groupId, teacherId);
            openAuthContext.add("indexData", indexData);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
        }
        return openAuthContext;
    }

    // 领取班级奖励
    @RequestMapping(value = "receivegroupreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext receiveClazzReward(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"));
        Long groupId = SafeConverter.toLong(openAuthContext.getParams().get("groupId"));
        if (teacherId == 0L || groupId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            return openAuthContext;
        }
        try {
            if (asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .unflushable_getUserBehaviorCount(UserBehaviorType.LUCKY_BAG_CLAZZ_REWARD_COUNT, groupId)
                    .getUninterruptibly() > 0) {
                openAuthContext.setCode(SYSTEM_ERROR_CODE);
                openAuthContext.setError("你已经领取过了");
                return openAuthContext;
            }
            MapMessage message = atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("TEACHER_RECEIVE_LUCKY_BAG")
                    .keys(groupId)
                    .proxy()
                    .receiveLuckyBagClazzReward(groupId, teacherId);
            if (message.isSuccess()) {
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError("领取失败");
            }
        } catch (CannotAcquireLockException ex) {
            openAuthContext.setCode(BUSINESS_ERROR_CODE);
            openAuthContext.setError("正在提交，请稍后重试");
        } catch (Exception ex) {
            openAuthContext.setCode(BUSINESS_ERROR_CODE);
            openAuthContext.setError("领取失败");
        }
        return openAuthContext;
    }


    public MapMessage applySchoolAmbassador(User teacher,
                                            String name,
                                            String mobile,
                                            String qq,
                                            String email,
                                            String leader,
                                            Integer totalCount,
                                            Integer usingCount,
                                            String suggestion,
                                            String gender,
                                            String address,
                                            Integer englishCount,
                                            Integer mathCount,
                                            Integer chineseCount,
                                            Integer studentCount,
                                            Integer clazzCount,
                                            String eduSystemType,
                                            String source,
                                            String pname,
                                            String cname,
                                            String aname,
                                            Integer bYear,
                                            Integer tYear,
                                            Integer bMonth,
                                            Integer bDay,
                                            Boolean isFx,
                                            String fxClass,
                                            String schoolName,
                                            String schoolLevel,
                                            Integer oneClazzStudentCountBegin,
                                            Integer oneClazzStudentCountEnd,
                                            Integer oneGradeClazzCountBegin,
                                            Integer oneGradeClazzCountEnd) {
        if (!User.isTeacherUser(teacher)) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (!RealnameRule.isValidRealName(name)) {
            return MapMessage.errorMessage("姓名不正确");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号不正确");
        }
        if (StringUtils.isBlank(qq) || !qq.matches("^\\d+$")) {
            return MapMessage.errorMessage("QQ不正确");
        }
        EduSystemType systemType = null;
        if (StringUtils.isNotBlank(eduSystemType)) {
            systemType = EduSystemType.of(eduSystemType);
        }
        SchoolAmbassadorSource ambassadorSource = SchoolAmbassadorSource.of(source);
        totalCount = totalCount == null ? 0 : totalCount;
        usingCount = usingCount == null ? 0 : usingCount;
        totalCount = Math.max(totalCount, 0);
        usingCount = Math.max(usingCount, 0);

        try {

            SchoolAmbassador ambassador = SchoolAmbassador.of(teacher.getId(), name, leader,
                    totalCount, usingCount, suggestion, gender, address, englishCount,
                    mathCount, chineseCount, studentCount, clazzCount, systemType, ambassadorSource,
                    pname, cname, aname, bYear, bMonth, bDay, tYear, isFx, fxClass, schoolName, schoolLevel,
                    oneClazzStudentCountBegin, oneClazzStudentCountEnd, oneGradeClazzCountBegin, oneGradeClazzCountEnd);

            ambassador.setSensitiveEmail(sensitiveUserDataServiceClient.encodeEmail(email));
            ambassador.setSensitiveMobile(sensitiveUserDataServiceClient.encodeMobile(mobile));
            ambassador.setSensitiveQq(sensitiveUserDataServiceClient.encodeQq(qq));

            return businessTeacherServiceClient.getRemoteReference().applySchoolAmbassador(ambassador);
        } catch (Exception ex) {
            logger.error("Failed to apply school ambassador", ex);
            return MapMessage.errorMessage("申请失败");
        }
    }
}
