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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.business.api.constant.LearningGoalType;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.campaign.client.MothersDayServiceClient;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.FairylandLoaderClient;
import com.voxlearning.utopia.temp.LuckyBagActivity;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.flash.FlashVars;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform.STUDENT_APP;
import static com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType.APPS;


@Controller
@RequestMapping("/student/activity/")
public class StudentActivityController extends AbstractController {

    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;
    @Inject private FinanceServiceClient financeServiceClient;
    @Inject private FairylandLoaderClient fairylandLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private MothersDayServiceClient mothersDayServiceClient;

    /**
     * 活动页面统配入口
     */
    @RequestMapping(value = "{page}.vpage", method = RequestMethod.GET)
    public String page(@PathVariable("page") String page) {

        // FIXME 下线的活动自觉在这里加入自动跳转
        if (page.equals("christmaslottery") || page.equals("christmas")) {
            return "redirect:/index.vpage";
        }

        return "studentv3/activity/" + page;
    }

    /**
     * knewton Index
     */
    @RequestMapping(value = "knewton/index.vpage", method = RequestMethod.GET)
    public String knewton() {
        return "redirect:/student/index.vpage";
    }

    /**
     * 秒杀逻辑没有了
     */
    @RequestMapping(value = "seckillentrance.vpage", method = RequestMethod.GET)
    public String seckillEntrance() {
        return "redirect:/student/index.vpage";
    }

    /**
     * 秒杀逻辑没有了
     */
    @RequestMapping(value = "seckill.vpage", method = RequestMethod.POST)
    @Deprecated
    public String seckill() {
        return "redirect:/student/index.vpage";
    }

    /**
     * 开学季充值活动入口
     */
    @RequestMapping(value = "rechargeactivity.vpage", method = RequestMethod.GET)
    public String rechargeActivity(Model model) {

        BigDecimal recharge = new BigDecimal(0);
        Date beginDate;
        if (!RuntimeMode.isProduction()) {
            beginDate = DateUtils.stringToDate("2015-03-12 23:59:59");
        } else {
            beginDate = DateUtils.stringToDate("2015-03-15 23:59:59");
        }
        Date endDate = DateUtils.stringToDate("2015-04-16 00:00:00");
        List<FinanceFlow> financeFlows = financeServiceClient.getFinanceService()
                .findUserFinanceFlows(currentUserId())
                .getUninterruptibly();
        for (FinanceFlow financeFlow : financeFlows) {
            if (beginDate.before(financeFlow.getCreateDatetime()) &&
                    endDate.after(financeFlow.getCreateDatetime()) &&
                    "RECHARGE".equals(financeFlow.getType())) {
                recharge = recharge.add(financeFlow.getAmount());
            }
        }
        model.addAttribute("recharge", recharge);
        return "studentv3/activity/recharge";
    }

    /**
     * 爱儿优测验活动
     */
    @RequestMapping(value = "iandyou/index.vpage", method = RequestMethod.GET)
    public String iandyouIndex() {
        return "studentv3/activity/iandyou/index";
    }

    @RequestMapping(value = "iandyou/experience.vpage", method = RequestMethod.GET)
    public String iandyouExperience() {
        return "studentv3/activity/iandyou/experience";
    }

    @RequestMapping(value = "sanguo/index.vpage", method = RequestMethod.GET)
    public String sanguoExperience() {
        return "studentv3/activity/sanguo/index";
    }

    /**
     * 母亲节贺卡宣传页
     */
    @RequestMapping(value = "mothersday/index.vpage", method = RequestMethod.GET)
    public String mothersDay(Model model, HttpServletRequest request) {
        User student = currentStudent();
        FlashVars vars = new FlashVars(request);
        vars.add("studentId", student.getId());
        MapMessage mesg = mothersDayServiceClient.getMothersDayService()
                .getMothersDayCard(student, false)
                .getUninterruptibly();
        vars.add("card", mesg.isSuccess() ? JsonUtils.toJson(mesg.get("card")) : null);
        String flashURL = "/resources/apps/flash/MumsDayCard.swf";
        String flashGameCoreUrl = "/resources/apps/flash/future/game.swf";
        String flashLogicUrl = "/resources/apps/flash/future/logics/EnglishReadLogic.swf";
        String flashEngineUrl = "/resources/apps/flash/future/gameengines/EnglishReadEngine.swf";
        vars.add("flashURL", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashURL));
        vars.add("flashGameCoreUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashGameCoreUrl));
        vars.add("flashLogicUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashLogicUrl));
        vars.add("flashEngineUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashEngineUrl));
        vars.add("gameDataURL", getWebRequestContext().getWebAppBaseUrl() + "/appdata/flash/Snail/obtain-ENGLISH-21670001.vpage");
        String flashVars = vars.getJsonParam();
        model.addAttribute("flashVars", flashVars);
        return "studentv3/activity/mothersday/index";
    }

    /**
     * 赠送母亲节贺卡
     */
    @RequestMapping(value = "sendmdcard.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage giveMothersDayCardAsGift() {
        User student = currentUser();
        String image = getRequestString("image");
        String voice = getRequestString("voice");
        try {
            return AtomicCallbackBuilderFactory.getInstance().<MapMessage>newBuilder()
                    .keyPrefix("mothersdaycard")
                    .keys(currentUserId())
                    .callback(() -> mothersDayServiceClient.getMothersDayService()
                            .giveMothersDayCardAsGift(student, image, voice)
                            .getUninterruptibly())
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        }
    }

    @RequestMapping(value = "sanguo/sendbeans.vpage", method = RequestMethod.GET)
    public String sanguoSendBeansExperience() {
        return "studentv3/activity/sanguo/sendbeans";
    }

    // 星星榜
    @RequestMapping(value = "starrank.vpage", method = RequestMethod.GET)
    public String starRank(Model model) {
        return "redirect:/student/index.vpage";
    }

    // 福袋传传
    @RequestMapping(value = "luckybag.vpage", method = RequestMethod.GET)
    public String luckyBag(Model model) {
        if (!LuckyBagActivity.isInPeriod()) {
            return "redirect:/student/index.vpage";
        }
        Long studentId = currentUserId();
        // 没有班级的校验
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        if (clazz == null) {
            return "redirect:/student/index.vpage";
        }
        Map<String, Object> data = businessStudentServiceClient.loadStudentLuckyBagIndexData(studentId, clazz.getId());
        model.addAttribute("data", data);

        //yiFei
        if (isMobileRequest(getRequest())) {
            return "studentmobile/activity/luckybaga";
        }

        return "studentv3/activity/luckybag";
    }

    // 分享福袋
    @RequestMapping(value = "sendluckybag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendLuckyBag() {
        if (!LuckyBagActivity.isInPeriod()) {
            return MapMessage.errorMessage("活动已结束");
        }
        Long studentId = currentUserId();
        String receiverIds = getRequestString("receiverIds");
        try {
            if (StringUtils.isBlank(receiverIds)) {
                return MapMessage.errorMessage("请选择要传递的同学");
            }
            // 没有班级的校验
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            if (clazz == null) {
                return MapMessage.errorMessage("请先加入班级");
            }
            // 这里按班级ID加锁， 防止并发的情况导致错误数据
            return atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .keyPrefix("STUDENT_SEND_LUCKY_BAG")
                    .keys(clazz.getId())
                    .proxy()
                    .sendLuckyBag(studentId, receiverIds, clazz.getId());
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请稍后重试");
        } catch (Exception ex) {
            logger.error("student send lucky bag error, studentId {}, error is {}", studentId, ex.getMessage());
            return MapMessage.errorMessage("提交失败，请重试");
        }
    }

    // 帮助同学打开福袋
    @RequestMapping(value = "openluckybag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage openLuckyBag() {
        if (!LuckyBagActivity.isInPeriod()) {
            return MapMessage.errorMessage("活动已结束");
        }
        Long studentId = currentUserId();
        try {
            return atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .keyPrefix("STUDENT_OPEN_LUCKY_BAG")
                    .keys(studentId)
                    .proxy()
                    .openLuckyBag(studentId);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请稍后重试");
        } catch (Exception ex) {
            logger.error("student open lucky bag error, studentId {}, error is {}", studentId, ex.getMessage());
            return MapMessage.errorMessage("提交失败，请重试");
        }
    }

    // 领取奖励
    @RequestMapping(value = "receiveluckybag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage receiveLuckyBag() {
        if (!LuckyBagActivity.isInPeriod()) {
            return MapMessage.errorMessage("活动已结束");
        }
        Long studentId = currentUserId();
        try {
            return atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .keyPrefix("STUDENT_RECEIVE_LUCKY_BAG")
                    .keys(studentId)
                    .proxy()
                    .receiveLuckyBag(studentId);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.successMessage("正在处理，请稍后重试");
        } catch (Exception ex) {
            logger.error("student receive lucky bag error, studentId {}, error is {}", studentId, ex.getMessage());
            return MapMessage.errorMessage("提交失败，请重试");
        }
    }


    // 阳春三月活动
    @RequestMapping(value = "threemonth.vpage", method = RequestMethod.GET)
    public String threeMonth(Model model) {
        Long studentId = currentUserId();
        if (studentId == 0) {
            return "redirect:/student/index.vpage";
        }
        return "studentv3/activity/threemonthgift/threemonth";
    }

    // 阳春三月活动领取奖励
    @RequestMapping(value = "receivethreemonthreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage receiveThreeMonthReward() {
//        Long studentId = currentUserId();
//        if (studentId == 0) {
//            return MapMessage.errorMessage("参数错误");
//        }
//        if (RuntimeMode.gt(Mode.STAGING) && !ActivityDateManager.isInActivity(ActivityDateManager.ActivityType.阳春三月领取奖励)) {
//            return MapMessage.errorMessage("活动已结束");
//        }
//        if (asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
//                .unflushable_getUserBehaviorCount(UserBehaviorType.THREE_MONTH_INTEGRAL_REWARD_COUNT, studentId)
//                .getUninterruptibly() > 0) {
//            return MapMessage.errorMessage("已经领取过了");
//        }
//        if (asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
//                .unflushable_getUserBehaviorCount(UserBehaviorType.THREE_MONTH_NO_INTEGRAL_REWARD_COUNT, studentId)
//                .getUninterruptibly() > 0) {
//            return MapMessage.errorMessage("对不起，你不满足领取资格");
//        }
//        try {
//            return atomicLockManager.wrapAtomic(businessStudentServiceClient)
//                    .keyPrefix("STUDENT_RECEIVE_THREE_MONTH_REWARD")
//                    .keys(studentId)
//                    .proxy()
//                    .receiveThreeMonthReward(studentId);
//        } catch (CannotAcquireLockException ex) {
//            return MapMessage.successMessage("正在处理，请稍后重试");
//        } catch (Exception ex) {
//            logger.error("student receive three month reward error, studentId {}, error is {}", studentId, ex.getMessage());
//            return MapMessage.errorMessage("提交失败，请重试");
//        }
        return MapMessage.errorMessage("活动已结束");
    }


    /**
     * 走遍美国学英语开学季宣传活动
     * return :
     * 　　　learningGoalType已经选择目标类型，为空表示未选择
     */
    @RequestMapping(value = "usaadventure/newtermactivity.vpage", method = RequestMethod.GET)
    public String newTermActivity(Model model) {
        User user = currentUser();

        if (user != null) {
            LearningGoalType learningGoalType = asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                    .UsaAdventureActivityCacheManager_loadRecord(user.getId())
                    .getUninterruptibly();
            model.addAttribute("learningGoalType", learningGoalType);
        }

        VendorApps vendorApps = vendorLoaderClient.loadVendor(OrderProductServiceType.UsaAdventure.name());
        if (vendorApps != null) {
            model.addAttribute("orientation", vendorApps.getOrientation());
            model.addAttribute("browser", vendorApps.getBrowser());
        }
        return "studentv3/activity/usaadventure/newtermactivity";
    }

    /**
     * 走遍美国学英语开学季宣传活动
     * 参加活动
     */
    @RequestMapping(value = "usaadventure/newtermactivity/receivegift.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage newTermActivityJoin() {
        User user = currentUser();
        LearningGoalType learningGoalType = LearningGoalType.of(getRequestString("learningGoalType"));

        if (user == null) {
            return MapMessage.errorMessage("请重新登陆");
        }
        if (learningGoalType == null) {
            return MapMessage.errorMessage("请选择目标");
        }

        Date date = new Date();
        if (date.after(DateUtils.stringToDate("2016-09-30 23:59:59"))) {
            return MapMessage.errorMessage("活动已经结束");
        }

        boolean flag = asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                .UsaAdventureActivityCacheManager_addRecord(user.getId(), learningGoalType)
                .getUninterruptibly();
        if (!flag) {
            return MapMessage.successMessage();
        }

        Map<Long, List<StudentParent>> parentsMap = parentLoaderClient.loadStudentParents(Collections.singletonList(user.getId()));
        List<Long> parentIds = parentsMap.getOrDefault(user.getId(), Collections.emptyList())
                .stream()
                .map(p -> p.getParentUser().getId())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(parentIds)) {

            Map<String, Object> extras = new HashMap<>();
            String url = "/parentMobile/activity/usaadventure/newtermgoal.vpage?learningGoalWordsNum=" + learningGoalType.num + "&sid=" + user.getId();
            extras.put("studentId", user.getId());
            extras.put("tag", ParentMessageTag.通知.name());
            extras.put("url", url);
            extras.put("s", ParentAppPushType.NOTICE.name());
            appMessageServiceClient.sendAppJpushMessageByIds("您的孩子在新学期，为自己设置了新的目标",
                    AppMessageSource.PARENT, parentIds, extras);

            parentIds.forEach(p -> {
                AppMessage appUserMessage = new AppMessage();
                appUserMessage.setMessageType(ParentMessageType.REMINDER.type);
                appUserMessage.setUserId(p);
                appUserMessage.setLinkType(1);
                appUserMessage.setContent("快来支持鼓励孩子完成新学期的目标吧");
                appUserMessage.setLinkUrl(url);
                appUserMessage.setImageUrl("vendorimg-20160824-57bd8437e92b1b3ba2763a9e.jpg");
                appUserMessage.setExtInfo(extras);
                // use ASYNC mode for we don't need response.
                messageCommandServiceClient.getMessageCommandService().createAppMessage(appUserMessage);
            });
        }
        return MapMessage.successMessage();
    }

    /**
     * 走遍美国变形魔法活动
     */
    @RequestMapping(value = "sundry/usaadventure/usaMagic.vpage", method = RequestMethod.GET)
    public String usaMagic() {
        String url = "/resources/apps/hwh5/Sundry/V1_0_0/index.html?module=USAMagic";
        VendorApps vendorApps = vendorLoaderClient.loadVendor(OrderProductServiceType.UsaAdventure.name());
        String orientation = null;
        String browser = null;
        if (vendorApps != null) {
            orientation = vendorApps.getOrientation();
            browser = vendorApps.getBrowser();
        }
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m("domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "server_type", RuntimeMode.current().getStageMode(),
                "useNewCore", browser,
                "orientation", orientation));
        return "redirect:" + url;
    }

    /**
     * 增值9月产品宣传宣传页
     */
    @RequestMapping(value = "sundry/fairyland/intro.vpage", method = RequestMethod.GET)
    public String fairylandIntro() {
        String url = "/resources/apps/hwh5/Sundry/V1_0_0/index.html?module=FairylandIntro";
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m("domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "server_type", RuntimeMode.current().getStageMode()));
        return "redirect:" + url;
    }


    /**
     * 走遍美国家学生端活动入口页面
     */
    @RequestMapping(value = "usaadventure/sundry/activity.vpage", method = RequestMethod.GET)
    public String activitys() {
        String url = "/resources/apps/hwh5/Sundry/V1_0_0/index.html";
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m("domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "server_type", RuntimeMode.current().getStageMode(),
                "module", getRequestString("module")));

        return "redirect:" + url;
    }

    @RequestMapping(value = "/hd/hd.vpage", method = RequestMethod.GET)
    public String holidayActivity(Model model) {

        VendorApps vendorApps = vendorLoaderClient.loadVendor(OrderProductServiceType.AfentiExam.name());
        String orientation = null;
        String browser = null;
        if (vendorApps != null) {
            orientation = vendorApps.getOrientation();
            browser = vendorApps.getBrowser();
        }
        List<FairylandProduct> fps = fairylandLoaderClient.loadFairylandProducts(STUDENT_APP, APPS);
        FairylandProduct math = fps.stream()
                .filter(p -> p.getAppKey().equals(OrderProductServiceType.AfentiMath.name()))
                .findFirst()
                .orElse(null);
        FairylandProduct chinese = fps.stream()
                .filter(p -> p.getAppKey().equals(OrderProductServiceType.AfentiChinese.name()))
                .findFirst()
                .orElse(null);
        FairylandProduct exam = fps.stream()
                .filter(p -> p.getAppKey().equals(OrderProductServiceType.AfentiExam.name()))
                .findFirst()
                .orElse(null);

        model.addAttribute("useNewCore", browser);
        model.addAttribute("orientation", orientation);
        String AfentiMathUrl = math == null ? "" : math.fetchRedirectUrl(RuntimeMode.current());
        String AfentiExamUrl = exam == null ? "" : exam.fetchRedirectUrl(RuntimeMode.current());
        String AfentiChineseUrl = chinese == null ? "" : chinese.fetchRedirectUrl(RuntimeMode.current());
        model.addAttribute("AfentiMathUrl", AfentiMathUrl);
        model.addAttribute("AfentiExamUrl", AfentiExamUrl);
        model.addAttribute("AfentiChineseUrl", AfentiChineseUrl);
        return "/studentv3/activity/hd";
    }

    @RequestMapping(value = "/redirect/openapp/detailpage-{page}.vpage", method = RequestMethod.GET)
    public String redirectOpenApp(Model model, @PathVariable("page") String page) {

        OrderProductServiceType productServiceType = OrderProductServiceType.safeParse(getRequestString("appKey"));
        if (productServiceType == OrderProductServiceType.Unknown) {
            return "redirect:/student/index.vpage";
        }
        FairylandProduct fairylandProduct = fairylandLoaderClient.loadFairylandProducts(STUDENT_APP, APPS)
                .stream()
                .filter(p -> productServiceType.name().equals(p.getAppKey()))
                .findFirst()
                .orElse(null);

        VendorApps vendorApps = vendorLoaderClient.loadVendor(productServiceType.name());
        if (vendorApps != null && fairylandProduct != null) {
            String url = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
            model.addAttribute("useNewCore", vendorApps.getBrowser());
            model.addAttribute("orientation", vendorApps.getOrientation());
            model.addAttribute("appKey", productServiceType.name());
            model.addAttribute("url", url);
        }
        return "/studentv3/activity/" + page;
    }

    @RequestMapping(value = "/oralcommunication/status.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadOralCommunicationHomeworkStatus() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请登录后再访问");
        }
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        GroupMapper englishGroup = groupMappers.stream()
                .filter(g -> Subject.ENGLISH == g.getSubject())
                .findFirst()
                .orElse(null);
        if (englishGroup == null) {
            return MapMessage.errorMessage("没有英语班组");
        }
        List<NewHomework.Location> groupHomeworks = newHomeworkLoaderClient.loadGroupHomeworks(englishGroup.getId(), Subject.ENGLISH).originalLocationsAsList();
        if (CollectionUtils.isNotEmpty(groupHomeworks)) {
            // 获取今天的所有作业
            List<NewHomework.Location> todayHomeworkLocations = new ArrayList<>();
            Date today = new Date();
            String todayDate = DateUtils.dateToString(today, FORMAT_SQL_DATE);
            for (NewHomework.Location location : groupHomeworks) {
                Date createDate = new Date(location.getCreateTime());
                String date = DateUtils.dateToString(createDate, FORMAT_SQL_DATE);
                if (StringUtils.equals(todayDate, date)) {
                    todayHomeworkLocations.add(location);
                }
            }
            if (CollectionUtils.isNotEmpty(todayHomeworkLocations)) {
                Map<String, NewHomework> newHomeworkMap = newHomeworkLoaderClient.loadNewHomeworks(todayHomeworkLocations.stream().map(NewHomework.Location::getId).collect(Collectors.toList()));
                // 找到所有包含口语交际的作业
                if (MapUtils.isNotEmpty(newHomeworkMap)) {
                    List<NewHomework.Location> oralCommunicationHomeworkLocations = newHomeworkMap.values()
                            .stream()
                            .filter(h -> h.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.ORAL_COMMUNICATION) != null)
                            .map(NewHomework::toLocation)
                            .collect(Collectors.toList());
                    Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoaderClient.loadNewHomeworkResult(oralCommunicationHomeworkLocations, studentDetail.getId(), false)
                            .stream()
                            .collect(Collectors.toMap(NewHomeworkResult::getHomeworkId, Function.identity()));
                    String unFinishedHomeworkId = null;
                    for (NewHomework.Location location : oralCommunicationHomeworkLocations) {
                        NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(location.getId());
                        // 找到第一个未完成的口语交际作业
                        if (newHomeworkResult == null
                                || newHomeworkResult.getPractices() == null
                                || newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION) == null
                                || newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION).getFinishAt() == null) {
                            unFinishedHomeworkId = location.getId();
                            break;
                        }
                    }
                    if (unFinishedHomeworkId != null) {
                        return MapMessage.successMessage()
                                .add("status", "doHomework")
                                .add("homeworkId", unFinishedHomeworkId);
                    }
                }
            }
        }
        return MapMessage.successMessage().add("status", "remindAssign");
    }

    @RequestMapping(value = "/oralcommunication/remindassign.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage remindAssignOralCommunicationHomework() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请登录后再访问");
        }
        return newHomeworkServiceClient.remindAssignOralCommunicationHomework(studentDetail.getId());
    }
}
