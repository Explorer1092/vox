/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.entity.activity.InterestingReport;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.campaign.api.CampaignService;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignErrorType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.temp.ActivityDateManager;
import com.voxlearning.utopia.temp.NewSchoolYearActivity;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by XiaoPeng.Yang on 15-2-26.
 * <p>
 * 通用活动类 没有角色限制
 */
@Controller
@Slf4j
@NoArgsConstructor
@RequestMapping("/activity")
public class ActivityController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;
    @Inject private CampaignLoaderClient campaignLoaderClient;
    @Inject private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;
    @Inject private ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;

    @ImportService(interfaceClass = CampaignService.class) private CampaignService campaignService;
    private Object purchaseCountAccordingTime;

    /* 2016教师节活动 分享页面 */
    @RequestMapping(value = "teachersdayshare.vpage", method = RequestMethod.GET)
    public String teachersDayShare(Model model) {
        // 活动下线
        return "redirect:/index.vpage";
    }


    // 单元报告
    @RequestMapping(value = "gur.vpage", method = RequestMethod.GET)
    public String groupUnitReport(String code, Model model) {
        return "redirect:/index.vpage";
    }

    //开学大礼包活动首页
    @RequestMapping(value = "termbegin.vpage", method = RequestMethod.GET)
    public String termBegin(Model model) {
        String requestSource = getRequestString("s");
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return "redirect:/teacher/index.vpage";
        }
        // 只有小学语数英认证老师才能看到活动
        if (!(teacher.isPrimarySchool() && (teacher.isMathTeacher() || teacher.isChineseTeacher() || teacher.isEnglishTeacher()))) {
            return "redirect:/teacher/index.vpage";
        }

        //抽奖情况
        int freeChance = campaignService.getTeacherLotteryFreeChance(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN, teacher.getId());
        List<Map<String, Object>> campaignLotteryResultsBig = campaignLoaderClient.loadRecentCampaignLotteryResultBig(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN.getId());
        //今天的不显示
        campaignLotteryResultsBig = campaignLotteryResultsBig.stream().filter(b -> b.get("datetime") == null ||
                DayRange.current().getStartDate().after((Date) b.get("datetime"))).collect(Collectors.toList());

        Map<Integer, CampaignLottery> lotteryMap = campaignLoaderClient.getCampaignLoader().findCampaignLotteries(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN.getId())
                .stream()
                .filter(e -> e.getAwardId() != null)
                .collect(Collectors.groupingBy(CampaignLottery::getAwardId))
                .values()
                .stream()
                .map(e -> e.iterator().next())
                .collect(Collectors.toMap(CampaignLottery::getAwardId, Function.identity()));
        List<CampaignLotteryHistory> histories = campaignLoaderClient.getCampaignLoader().findCampaignLotteryHistories(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN.getId(), teacher.getId());
        List<Map<String, Object>> myHistory = new ArrayList<>();
        for (CampaignLotteryHistory history : histories) {
            Map<String, Object> map = new HashMap<>();
            map.put("lotteryDate", history.getCreateDatetime());
            map.put("awardName", lotteryMap.get(history.getAwardId()).getAwardName());
            myHistory.add(map);
        }
        model.addAttribute("freeChance", freeChance);
        model.addAttribute("campaignLotteryResultsBig", campaignLotteryResultsBig);
        model.addAttribute("myHistory", myHistory);
        //唤醒中  已唤醒人数
        List<ActivateInfoMapper> mappers = businessTeacherServiceClient.getActivatingTeacher(teacher.getId());
        List<ActivateInfoMapper> sucessMappers = businessTeacherServiceClient.getActivatedTeacher(teacher.getId());
        //过滤出活动期间内唤醒的人
        if (RuntimeMode.le(Mode.STAGING)) {
            sucessMappers = sucessMappers.stream().filter(a -> a.getActivateSuccessDate().after(DateUtils.stringToDate("2016-08-20 23:59:59"))).collect(Collectors.toList());
        } else {
            sucessMappers = sucessMappers.stream().filter(a -> a.getActivateSuccessDate().after(NewSchoolYearActivity.getSummerStartDate())).collect(Collectors.toList());
        }
        model.addAttribute("sucessTeachers", sucessMappers);
        model.addAttribute("activatingTeachers", mappers);
//        // 完成假期作业小包的学生数
//        int vacationCount = businessTeacherServiceClient.loadTeacherFinishVacationHomeworkCount(teacher);
//        model.addAttribute("vacationCount", vacationCount);
        // 是否领取过假期作业礼包
//        boolean hasReward = homeworkCacheClient.getVhRewardCacheManager().isReward(teacher.getId());
//        model.addAttribute("hasReward", hasReward);
        // 是否调整过班级
        boolean hasAdjustClazz = asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                .TeacherAdjustClazzRemindCacheManager_done(teacher.getId())
                .getUninterruptibly();
        model.addAttribute("hasAdjustClazz", hasAdjustClazz);
        model.addAttribute("s", requestSource);
        if (isMobileRequest(getRequest())) {
            // 移动端页面
            return "teacherv3/reward/termbeginapp";
        } else {
            return "teacherv3/reward/termbegin";
        }
    }

    //开学大礼包活动首页V2
    @RequestMapping(value = "v2/termbegin.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage termBeginV2() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage(CampaignErrorType.NEED_LOGIN.getInfo()).setErrorCode(CampaignErrorType.NEED_LOGIN.getCode());
        }
        // 只有小学语数英老师才能看到活动
        if (!(teacher.isPrimarySchool() && (teacher.isMathTeacher() || teacher.isChineseTeacher() || teacher.isEnglishTeacher()))) {
            return MapMessage.errorMessage("本次活动只支持小学老师参与，请您关注其他活动哦~").setErrorCode(CampaignErrorType.ACTIVITY_NOT_AVAILABLE.getCode());
        }

        //抽奖情况
        int freeChance = campaignService.getTeacherLotteryFreeChance(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN, teacher.getId());
        List<Map<String, Object>> campaignLotteryResultsBig = campaignLoaderClient.loadRecentCampaignLotteryResultBig(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN.getId());
        //今天的不显示
//        campaignLotteryResultsBig = campaignLotteryResultsBig.stream().filter(b -> b.get("datetime") == null ||
//                DayRange.current().getStartDate().after((Date) b.get("datetime"))).collect(Collectors.toList());

        Map<Integer, CampaignLottery> lotteryMap = campaignLoaderClient.getCampaignLoader().findCampaignLotteries(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN.getId())
                .stream()
                .filter(e -> e.getAwardId() != null)
                .collect(Collectors.groupingBy(CampaignLottery::getAwardId))
                .values()
                .stream()
                .map(e -> e.iterator().next())
                .collect(Collectors.toMap(CampaignLottery::getAwardId, Function.identity()));
        List<CampaignLotteryHistory> histories = campaignLoaderClient.getCampaignLoader().findCampaignLotteryHistories(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN.getId(), teacher.getId());
        List<Map<String, Object>> myHistory = new ArrayList<>();
        for (CampaignLotteryHistory history : histories) {
            Map<String, Object> map = new HashMap<>();
            map.put("lotteryDate", history.getCreateDatetime());
            map.put("awardName", lotteryMap.get(history.getAwardId()).getAwardName());
            myHistory.add(map);
        }
        MapMessage mesg = MapMessage.successMessage();
        mesg.put("freeChance", freeChance);
        mesg.put("campaignLotteryResultsBig", campaignLotteryResultsBig);
        mesg.put("myHistory", myHistory);
        String requestSource = getRequestString("s");
        mesg.put("s", requestSource);
        return mesg;
    }

    // 开学大抽奖互动
    @RequestMapping(value = "dolottery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doLottery() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (RuntimeMode.gt(Mode.STAGING) && !NewSchoolYearActivity.isInTermBeginPeriod()) {
            return MapMessage.errorMessage("活动未开始或已过期");
        }
        if (!(teacher.isPrimarySchool() && (teacher.isMathTeacher() || teacher.isChineseTeacher() || teacher.isEnglishTeacher()))) {
            return MapMessage.errorMessage("本次活动只支持小学老师参与，请您关注其他活动哦~").setErrorCode(CampaignErrorType.ACTIVITY_NOT_AVAILABLE.getCode());
        }

        if (teacher.fetchCertificationState() == null || teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage("认证老师才能参与活动哦\n快去达成认证吧~").setErrorCode(CampaignErrorType.NOT_AUTHENTICATION.getCode());
        }

        try {
            return atomicLockManager.wrapAtomic(campaignService)
                    .expirationInSeconds(30)
                    .keyPrefix("TEACHER_TERM_BEGIN_LOTTERY_DO")
                    .keys(teacher.getId())
                    .proxy()
                    .drawLottery(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN, teacher, LotteryClientType.PC);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 17奖学金PC页面
    @RequestMapping(value = "teacherlotterynew.vpage", method = RequestMethod.GET)
    public String teacherLotteryNew(Model model) {
        return "/teacherv3/activity/lottery/teacherlotterynew";
    }

    // 17奖学金活动页面参数
    @RequestMapping(value = "loadscholarshipdata.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadScholarshipData() {
        // 活动过期判断
        if (DateUtils.stringToDate("2017-05-31 23:59:59").before(new Date())) {
            return MapMessage.errorMessage("活动已过期");
        }
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        MapMessage message = MapMessage.successMessage();
        //抽奖情况
        int freeChance = campaignService.getTeacherLotteryFreeChance(CampaignType.TEACHER_SCHOLARSHIP_GOLD_LOTTERY, teacher.getId());
        List<Map<String, Object>> campaignLotteryResultsBig = campaignLoaderClient.loadCampaignLotteryResultBigForScholarship(CampaignType.TEACHER_SCHOLARSHIP_GOLD_LOTTERY.getId());
        List<Map<String, Object>> campaignLotteryResults = campaignLoaderClient.loadRecentCampaignLotteryResultForWeek(CampaignType.TEACHER_SCHOLARSHIP_GOLD_LOTTERY.getId());
        // 剩余抽奖次数
        message.put("freeChance", freeChance);
        // 大奖纪录
        message.put("campaignLotteryResultsBig", campaignLotteryResultsBig);
        // 园丁豆纪录
        message.put("campaignLotteryResults", campaignLotteryResults);
        // 获取钥匙总数
        message.put("teacherKeyInfo", newHomeworkServiceClient.processScholarship(teacher.getId(), -1, 0).get("data"));
        return message;
    }

    // 17奖学金执行抽奖
    @RequestMapping(value = "doscholarshiplottery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doScholarshipLottery() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        // 活动过期判断
        if (NewSchoolYearActivity.getScholarshipEndDate().before(new Date())) {
            return MapMessage.errorMessage("活动已过期");
        }
        if (teacher.fetchCertificationState() == null || teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage("认证老师才能参与抽奖哦");
        }
        // 奖池判断
        Integer campaignId = getRequestInt("campaignId");
        CampaignType campaignType = CampaignType.of(campaignId);
        if (campaignType == null) {
            return MapMessage.errorMessage("无效的活动!");
        }
        // 校验钥匙数
        int totalKey = 0;
        MapMessage message = newHomeworkServiceClient.processScholarship(teacher.getId(), -1, 0);
        if (message.isSuccess()) {
            Map<String, Object> dataMap = (Map<String, Object>) message.get("data");
            if (dataMap != null) {
                totalKey = SafeConverter.toInt(dataMap.get("totalKeyNum"));
            }
        }
        if (campaignType == CampaignType.TEACHER_SCHOLARSHIP_GOLD_LOTTERY && totalKey < 15) {
            return MapMessage.errorMessage("对不起，您的钥匙数不符合条件");
        }
        if (campaignType == CampaignType.TEACHER_SCHOLARSHIP_SILVER_LOTTERY && totalKey < 10) {
            return MapMessage.errorMessage("对不起，您的钥匙数不符合条件");
        }
        if (campaignType == CampaignType.TEACHER_SCHOLARSHIP_COPPER_LOTTERY && totalKey < 5) {
            return MapMessage.errorMessage("对不起，您的钥匙数不符合条件");
        }

        String clientType = getRequestString("clientType");
        LotteryClientType type = LotteryClientType.APP;
        if (StringUtils.isNotBlank(clientType)) {
            type = LotteryClientType.valueOf(clientType);
        }
        try {
            return atomicLockManager.wrapAtomic(campaignService)
                    .expirationInSeconds(30)
                    .keyPrefix("TEACHER_SCHOLARSHIP_LOTTERY_DO")
                    .keys(teacher.getId())
                    .proxy()
                    .drawLottery(campaignType, teacher, type);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 61活动点读机抽奖
    @RequestMapping(value = "loadpiclistenlotterydata.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPicListenLotteryData() {
        // 活动过期判断
        if (!ActivityDateManager.isInActivity(ActivityDateManager.ActivityType.六一点读机抽奖)) {
            return MapMessage.errorMessage("活动已过期");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        MapMessage message = MapMessage.successMessage();
        CampaignType campaignType = CampaignType.PARENT_PICLISTEN_LOTTERY_201761;
        // 抽奖情况
        int freeChance = campaignService.getTeacherLotteryFreeChance(campaignType, parent.getId());
        // 最近中奖纪录
        List<Map<String, Object>> campaignLotteryResults = campaignLoaderClient.loadRecentCampaignLotteryResult(campaignType.getId());
        // 大奖纪录
        List<Map<String, Object>> campaignLotteryResultsBig = campaignLoaderClient.loadCampaignLotteryResultBigForScholarship(campaignType.getId());
        message.put("campaignLotteryResultsBig", campaignLotteryResultsBig);
        // 剩余抽奖次数
        message.put("freeChance", freeChance);
        // 滚动记录
        message.put("campaignLotteryResults", campaignLotteryResults);
        // 是否抽过奖
        List<CampaignLotteryHistory> historyList = campaignLoaderClient.findCampaignLotteryHistories(campaignType.getId(), parent.getId());
        message.put("hasHistory", CollectionUtils.isNotEmpty(historyList));
        return message;
    }

    // 阿分题预习抽奖活动
    @RequestMapping(value = "loadafentilotterydata.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadAfentiLotteryData() {
        // 活动过期判断
        if (!ActivityDateManager.isInActivity(ActivityDateManager.ActivityType.阿分提预习抽奖)) {
            return MapMessage.errorMessage("活动已过期");
        }
        User student = currentStudent();
        if (student == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        MapMessage message = MapMessage.successMessage();
        CampaignType campaignType = CampaignType.AFENTI_PREPARATION_LOTTERY;
        // 抽奖情况
        int freeChance = campaignService.getTeacherLotteryFreeChance(campaignType, student.getId());
        // 最近中奖纪录
        List<Map<String, Object>> campaignLotteryResults = campaignLoaderClient.loadRecentCampaignLotteryResultForWeek(campaignType.getId());
        // 大奖纪录
        List<Map<String, Object>> campaignLotteryResultsBig = campaignLoaderClient.loadCampaignLotteryResultBigForTime(campaignType.getId());
        message.put("campaignLotteryResultsBig", campaignLotteryResultsBig);
        // 剩余抽奖次数
        message.put("freeChance", freeChance);
        // 滚动记录
        message.put("campaignLotteryResults", campaignLotteryResults);
        // 是否抽过奖
        List<CampaignLotteryHistory> historyList = campaignLoaderClient.findCampaignLotteryHistories(campaignType.getId(), student.getId());
        message.put("historyList", historyList);
        return message;
    }

    // 61活动点读机获取购买动态
    @RequestMapping(value = "loadpurchaselist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPicListenPurchaseList() {
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录");
        }
        // 活动过期判断
        if (!ActivityDateManager.isInActivity(ActivityDateManager.ActivityType.六一点读机抽奖)) {
            return MapMessage.errorMessage("活动已过期");
        }
        List<Map<String, Object>> purchaseList = asyncOrderCacheServiceClient.getAsyncOrderCacheService().
                PicListenBookPurchaseCacheManager_fetch().getUninterruptibly();
        // 获取推荐教材
//        List<Map<String, Object>> bookList = parentSelfStudyPublicHelper.parentRecomandBook(user.getId());
        MapMessage message = MapMessage.successMessage();
        message.put("purchaseList", purchaseList);
        message.put("bookList", new ArrayList<>());
        return message;
    }

    // 外研点读机活动获取购买动态
    @RequestMapping(value = "loadpurchaselist_waiyan.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPicListenPurchaseListWaiyan() {
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录");
        }
        // 活动过期判断
        if (!ActivityDateManager.isInActivity(ActivityDateManager.ActivityType.外研点读机特价活动)) {
            return MapMessage.successMessage().add("expire", true);
        }
        List<Map<String, Object>> purchaseList = asyncOrderCacheServiceClient.getAsyncOrderCacheService().
                PicListenBookPurchaseCacheManager_fetch().getUninterruptibly();
        // 获取推荐教材
        List<Map<String, Object>> bookList = new ArrayList<>();
        Integer clazz = parentSelfStudyPublicHelper.parentRecomandBook(user.getId(), bookList);
        bookList.forEach(t -> {
            t.put("img", getCdnBaseUrlAvatarWithSep() + t.get("img"));
            t.put("url", fetchMainsiteUrlByCurrentSchema() + t.get("url"));
        });
        MapMessage message = MapMessage.successMessage().add("expire", false);
        message.put("purchase_list", purchaseList);
        message.put("book_list", bookList);
        message.put("purchase_count", getPurchaseCount(0L));
        if (clazz != null)
            message.put("clazz", clazz);
        return message;
    }

    // 外研点读机活动获取购买动态
    // http://project.17zuoye.net/redmine/issues/47928
    @RequestMapping(value = "loadpurchaselist_shuangyu.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPicListenPurchaseListShuangyu() {
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录");
        }
        // 活动过期判断
        if (!ActivityDateManager.isInActivity(ActivityDateManager.ActivityType.点读机双语故事)) {
            return MapMessage.successMessage().add("expire", true);
        }
        AlpsFuture<List<Map<String, Object>>> listAlpsFuture = asyncOrderCacheServiceClient.getAsyncOrderCacheService().
                PicListenBookPurchaseCacheManager_fetch();
        AlpsFuture<Long> buyCountFuture = asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_loadBuyCount();
        // 获取推荐教材
        List<String> bookIdList = Arrays.asList("BK_10300002937999", "BK_10300002936551", "BK_10300002935563", "BK_10300002934053");
        List<Map<String, Object>> bookList = parentSelfStudyPublicHelper.bookListMapForActivity(user.getId(), bookIdList);
        bookList.forEach(t -> {
            t.put("img", getCdnBaseUrlAvatarWithSep() + t.get("img"));
            t.put("url", fetchMainsiteUrlByCurrentSchema() + t.get("url"));
        });
        MapMessage message = MapMessage.successMessage().add("expire", false);
        message.put("purchase_list", listAlpsFuture.getUninterruptibly());
        message.put("book_list", bookList);
        message.put("purchase_count", getPurchaseCount(buyCountFuture.getUninterruptibly()));

        return message;
    }

    // 领取假期作业礼包
    @RequestMapping(value = "receivevacationreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage receiveVacationReward() {
        return MapMessage.errorMessage("功能已下线");
    }

    // 记录老师班级调整弹窗 点击不调整按钮动作
    @RequestMapping(value = "recordadjust.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage recordAdjustClazz() {
        Long teacherId = currentUserId();
        //记录点击调整按钮
        asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                .TeacherAdjustClazzRemindCacheManager_record(teacherId)
                .awaitUninterruptibly();
        return MapMessage.successMessage();
    }

    // 家长端或者学生端获取学生的基本信息
    @RequestMapping(value = "studentinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchStudentInfo() {
        User user = currentUser();
        if (null == user || (!user.isStudent() && !user.isParent())) return MapMessage.errorMessage();

        StudentDetail student;
        if (user.isParent()) {
            Long studentId = SafeConverter.toLong(getRequestString("studentId"), Long.MIN_VALUE);
            if (studentId == Long.MIN_VALUE) {
                studentId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"), Long.MIN_VALUE);
            }
            if (studentId == Long.MIN_VALUE) return MapMessage.errorMessage();
            Set<Long> children = parentLoaderClient.loadParentStudentRefs(user.getId())
                    .stream()
                    .map(StudentParentRef::getStudentId)
                    .collect(Collectors.toSet());
            if (!children.contains(studentId)) return MapMessage.errorMessage();
            student = studentLoaderClient.loadStudentDetail(studentId);
        } else {
            student = user instanceof StudentDetail ? (StudentDetail) user : currentStudentDetail();
        }

        MapMessage mesg = MapMessage.successMessage();
        mesg.put("studentId", student.getId().toString());
        mesg.put("studentName", student.fetchRealnameIfBlankId());
        if (student.getClazz() != null) {
            mesg.put("clazzId", student.getClazz().getId().toString());
            mesg.put("clazzName", student.getClazz().formalizeClazzName());
            mesg.put("grade", student.getClazz().getClazzLevel());
        }
        return mesg;
    }

    /* 2016年度趣味报告 */
    @RequestMapping(value = "interestingreport.vpage", method = RequestMethod.GET)
    public String interestingReport(Model model) {
        User user = currentUser();
        if (user == null) {
            return "common/mobileerrorinfo";
        }
        if (user.fetchUserType() == UserType.PARENT) {
            Long userId = getRequestLong("sid");
            if (userId == 0) {
                userId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"));
                if (userId == 0) {
                    return "common/mobileerrorinfo";
                }
            }
            user = raikouSystem.loadUser(userId);
        }

        // 获取内容
        InterestingReport report = miscLoaderClient.loadUserInterestingReport(user.getId());
        model.addAttribute("report", report);

        if (user.fetchUserType() == UserType.PARENT || user.fetchUserType() == UserType.STUDENT) {
            Student currentStudent = studentLoaderClient.loadStudent(user.getId());
            model.addAttribute("currentStudentName", currentStudent.fetchRealname());//获取学生名称
            model.addAttribute("currentUserAvatarUrl", getUserAvatarImgUrl(currentStudent)); //当前学生头像
        }

        if (user.fetchUserType() == UserType.TEACHER) {
            return "activity/teacherreport/index";
        } else {
            return "activity/studentreport/index";
        }
    }

    /* 2016年度趣味报告 -- 记录用户愿望 */
    @RequestMapping(value = "recordwish.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recordNewYearWish() {
        if (new Date().after(DateUtils.stringToDate("2017-01-11 00:00:00"))) {
            return MapMessage.errorMessage("对不起，活动已过期");
        }
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (user.fetchUserType() == UserType.PARENT) {
            Long userId = getRequestLong("sid");
            if (userId == 0) {
                userId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"));
                if (userId == 0) {
                    return MapMessage.errorMessage("用户不存在");
                }
            }
            user = raikouSystem.loadUser(userId);
        }
        String wishContent = getRequestString("wishContent");
        try {
            return atomicLockManager.wrapAtomic(miscServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("INTERESTING_REPORT_WISH")
                    .keys(user.getId())
                    .proxy()
                    .recordNewYearWish(user, wishContent);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    /* 2016年度趣味报告 -- 领取礼物 */
    @RequestMapping(value = "getaward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getAward() {
        if (new Date().after(DateUtils.stringToDate("2017-01-11 00:00:00"))) {
            return MapMessage.errorMessage("对不起，活动已过期");
        }
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (user.fetchUserType() == UserType.PARENT) {
            Long userId = getRequestLong("sid");
            if (userId == 0) {
                userId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"));
                if (userId == 0) {
                    return MapMessage.errorMessage("用户不存在");
                }
            }
            user = raikouSystem.loadUser(userId);
        }
        // 是否领取过了
        if (asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                .InterestingReportCacheManager_done(user.getId())
                .getUninterruptibly()) {
            return MapMessage.errorMessage("已经领取过了");
        }
        try {
            return atomicLockManager.wrapAtomic(miscServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("INTERESTING_REPORT_GETAWARD")
                    .keys(user.getId())
                    .proxy()
                    .getInterestingReportAward(user);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    public Integer getPurchaseCount(Long buyCount) {
        return 5000 + buyCount.intValue();
    }
}
