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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.api.constant.TrusteeShop;
import com.voxlearning.utopia.api.constant.TrusteeType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.o2o.TrusteeOrderRecord;
import com.voxlearning.utopia.entity.o2o.TrusteeReserveRecord;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.coupon.api.entities.Coupon;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.order.consumer.TrusteeOrderServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.open.ApiConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.campaign.api.constant.CampaignType.PICLISTENBOOK_ORDER_LOTTERY;


/**
 * Created by Summer Yang on 2016/06/13.
 * <p>
 * 各种导流 实验 田
 */
@Controller
@RequestMapping(value = "/parentMobile/activity")
public class MobileParentActivityController extends AbstractMobileParentController {
    private static final String PLB_SINGLESDAY_APPOINTMENT = "PLB_SINGLESDAY_APPOINTMENT_";
    private static final String PLB_SINGLESDAY_APPOINTMENT_TOTAL_COUNT = "PLB_SINGLESDAY_APPOINTMENT_TOTAL_COUNT";

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private CouponLoaderClient couponLoaderClient;
    @Inject
    private CouponServiceClient couponServiceClient;
    @Inject
    private CampaignServiceClient campaignServiceClient;
    @Inject
    private CampaignLoaderClient campaignLoaderClient;
    @Inject
    private TrusteeOrderServiceClient trusteeOrderServiceClient;

    /**
     * 点读机双十一预约
     */
    @RequestMapping(value = "/piclistenbook/singlesday/appointment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage singlesDayAppointment() {
        Long userId = currentUserId();
        if (null == userId) {
            return MapMessage.errorMessage("未登录不能预约");
        }

        try {
            //记录当前用户预约状态
            washingtonCacheSystem.CBS.persistence.incr(PLB_SINGLESDAY_APPOINTMENT + userId, 1, 1, DateUtils.getCurrentToMonthEndSecond());
            //记录预约总人数
            washingtonCacheSystem.CBS.persistence.incr(PLB_SINGLESDAY_APPOINTMENT_TOTAL_COUNT, 1, 1, DateUtils.getCurrentToMonthEndSecond());

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统错误");
        }
    }

    @RequestMapping(value = "/piclistenbook/singlesday/appointment/query.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryAppointment() {
        Long userId = currentUserId();
        if (null == userId) {
            return MapMessage.errorMessage("未登录不可查询预约状态");
        }

        try {
            boolean appointment = false;
            CacheObject<Object> objectCacheObject = washingtonCacheSystem.CBS.persistence.get(PLB_SINGLESDAY_APPOINTMENT + userId);
            if (null != objectCacheObject && null != objectCacheObject.getValue()) {
                appointment = true;
            }

            long totalCount = 0;
            CacheObject<Object> countCacheObject = washingtonCacheSystem.CBS.persistence.get(PLB_SINGLESDAY_APPOINTMENT_TOTAL_COUNT);
            if (null != countCacheObject && null != countCacheObject.getValue()) {
                totalCount = SafeConverter.toLong(countCacheObject.getValue());
            }

            return MapMessage.successMessage()
                    .add("appointment", appointment)
                    .add("totalCount", totalCount + 5000);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统错误");
        }
    }

    /**
     * 点读机打包购买抽奖活动----抽奖
     */
    @RequestMapping(value = "/piclistenbook/lottery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage piclistenbookLottery() {
        try {
            User user = currentUser();
            if (null == user) {
                return MapMessage.errorMessage("请登录后再试");
            }
            return campaignServiceClient.getCampaignService().drawLottery(PICLISTENBOOK_ORDER_LOTTERY, user, LotteryClientType.APP);
        } catch (Exception ex) {
            logger.error("uid:{}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 点读机打包购买抽奖－－查询抽奖概要信息
     */
    @RequestMapping(value = "/piclistenbook/lottery/summary.vpage")
    @ResponseBody
    public MapMessage piclistenbookLotterySummary() {
        try {
            int freeChance = campaignServiceClient.getCampaignService().getTeacherLotteryFreeChance(PICLISTENBOOK_ORDER_LOTTERY, currentUserId());
            List<Map<String, Object>> recentLotteryResults = campaignLoaderClient.loadRecentCampaignLotteryResultForWeek(CampaignType.PICLISTENBOOK_ORDER_LOTTERY.getId());
            List<Map<String, Object>> recentLotteryBigResults = campaignLoaderClient.loadCampaignLotteryResultBigForTime(CampaignType.PICLISTENBOOK_ORDER_LOTTERY.getId());

            List<Map<String, Object>> lotteryResults = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(recentLotteryBigResults)) {
                lotteryResults.addAll(recentLotteryBigResults);
            }
            if (CollectionUtils.isNotEmpty(recentLotteryResults) && lotteryResults.size() < 10) {
                for (Map<String, Object> result : recentLotteryResults) {
                    lotteryResults.add(result);

                    if (lotteryResults.size() >= 10) break;
                }
            }

            List<Map<String, Object>> ret = new ArrayList<>();
            for (Map<String, Object> result : lotteryResults) {
                Map<String, Object> info = new HashMap<>();
                info.put("awardName", result.get("awardName"));

                //随便取一个孩子构造出"XX妈妈"这种名称
                Long parentId = SafeConverter.toLong(result.get("userId"));
                if (0 == parentId) continue;
                List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
                if (CollectionUtils.isEmpty(studentParentRefs)) continue;
                StudentParentRef studentParentRef = studentParentRefs.get(0);
                User user = raikouSystem.loadUser(studentParentRef.getStudentId());
                if (null == user) continue;
                info.put("userName", String.format("%s%s", user.fetchRealname(), StringUtils.isBlank(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName()));
                ret.add(info);
            }

            return MapMessage.successMessage().add("chance", freeChance).add("recent", ret);
        } catch (Exception ex) {
            logger.error("uid:{}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/coupon/afentirenew.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage afentiRenewCoupon() {
        String sbj = getRequestString("subject");
        Subject subject = Subject.ofWithUnknown(sbj);
        if (subject == Subject.UNKNOWN) {
            return MapMessage.errorMessage("未知学科");
        }

        try {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(currentUserId());
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                return MapMessage.errorMessage("没有关联孩子，不符合领取条件");
            }

            String couponId = getCouponId(subject);
            if (StringUtils.isBlank(couponId)) {
                return MapMessage.errorMessage("未找到有效的优惠券");
            }
            Coupon coupon = couponLoaderClient.loadCouponById(couponId);
            if (null == coupon) {
                return MapMessage.errorMessage("优惠券已失效");
            }

            List<CouponShowMapper> couponShowMappers = couponLoaderClient.loadUserCoupons(currentUserId());
            if (CollectionUtils.isNotEmpty(couponShowMappers)) {
                CouponShowMapper couponShowMapper = couponShowMappers.stream().filter(mapper -> mapper.getCouponId().equals(couponId)).findFirst().orElse(null);
                if (couponShowMapper != null) {
                    return MapMessage.successMessage().add("fresh", false);
                }
            }

            MapMessage ret = couponServiceClient.sendCoupon(couponId, currentUserId());
            if (!ret.isSuccess()) {
                return MapMessage.errorMessage("优惠券发放失败");
            }
            return MapMessage.successMessage().add("fresh", true);
        } catch (Exception ex) {
            logger.error("uid:{},subject:{},info:{}", currentUserId(), sbj, ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/getStudentList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getStudentList() {
        try {
            List<User> students = studentLoaderClient.loadParentStudents(currentUserId());
            //获取学生列表
            if (students.size() == 0) {
                //跳去绑学生页面
                return MapMessage.successMessage().add("students", new LinkedList<>());
            }
            List<Map<String, Object>> stdInfos = new ArrayList<>();
            students.stream().forEach(t -> {
                Map<String, Object> infoMap = new HashMap<>();
                infoMap.put("id", t.getId());
                infoMap.put("name", t.getProfile().getRealname());
                infoMap.put("img", getUserAvatarImgUrl(t.getProfile().getImgUrl()));
                stdInfos.add(infoMap);
            });
            return MapMessage.successMessage().add("students", stdInfos);
        } catch (Exception ex) {
            return MapMessage.errorMessage("获取孩子列表错误").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    // 付费课程推广介绍页 -- 凌晨 翻转课堂
    @RequestMapping(value = "/paycourse.vpage", method = RequestMethod.GET)
    public String payCourse(Model model) {
        Long studentId = getRequestLong("sid");
        // 这里对于学生ID为0的做一个兼容
        if (studentId == 0L) {
            // 获取孩子三四年级
            List<User> students = studentLoaderClient.loadParentStudents(currentUserId());
            if (CollectionUtils.isEmpty(students)) {
                // 跳转首页
                return "redirect:/parentMobile/home/index.vpage";
            }
            Long tempId = students.stream().findFirst().get().getId();
            for (User user : students) {
                Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
                if (clazz != null && (clazz.getClazzLevel().getLevel() == 3 || clazz.getClazzLevel().getLevel() == 4)) {
                    studentId = user.getId();
                    break;
                }
            }
            // 学生没找到
            if (studentId == 0L) {
                // 跳转首页
                return "redirect:/parentMobile/home/index.vpage?sid=" + tempId;
            }
        }

        // 过滤该学生是否已经购买成功了
        List<TrusteeOrderRecord> orderRecords = trusteeOrderServiceClient.loadTrusteeOrderByStudentId(studentId);
        orderRecords = orderRecords.stream().filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid)
                .filter(o -> o.getTrusteeType() == TrusteeType.FZKTZBK_4)
                .collect(Collectors.toList());
        model.addAttribute("hasPaid", CollectionUtils.isNotEmpty(orderRecords));
        model.addAttribute("trusteeType", TrusteeType.FZKTZBK_4);
        model.addAttribute("studentId", studentId);
        model.addAttribute("shop", TrusteeShop.FZKTZBK);
        return "/parentmobile/activity/paycourse";
    }


    // 暑期营导流专题 -- 获取孩子预约记录
    @RequestMapping(value = "loadstudentreserve.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadStudentReserve() {
        Integer shopId = getRequestInt("shopId");
        Long studentId = getRequestLong("studentId");
        List<TrusteeReserveRecord> reserveRecords = trusteeOrderServiceClient.loadTrusteeReserveByParentId(currentUserId(), shopId);
        if (CollectionUtils.isNotEmpty(reserveRecords) &&
                reserveRecords.stream().filter(r -> Objects.equals(r.getStudentId(), studentId)).count() > 0) {
            return MapMessage.successMessage().add("reserve", true);
        }
        return MapMessage.successMessage().add("reserve", false);
    }

    // 暑期营导流专题 -- 发送验证码
    @RequestMapping(value = "/sendrcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendReserveSmsCode() {
        String mobile = getRequestString("mobile");
        try {
            if (!MobileRule.isMobile(mobile)) {
                return MapMessage.errorMessage("请输入正确的手机号");
            }
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.PARENT_TRUSTEE_SEND_VERIFY_CODE.name(), false);
        } catch (Exception ex) {
            logger.error("send verify code failed", ex);
            return MapMessage.successMessage("发送验证码失败，请稍后再试");
        }
    }

    // 暑期营导流专题 -- 验证码验证并生成预约记录
    @RequestMapping(value = "verifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCode() {
        String mobile = getRequestString("mobile");
        String code = getRequestString("code");
        Long studentId = getRequestLong("studentId");
        Integer shopId = getRequestInt("shopId");

        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code)) {
            return MapMessage.errorMessage("请填写正确的验证码");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号不正确");
        }
        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
        if (shopId == 0 || shop == null) {
            return MapMessage.errorMessage("机构不存在");
        }
        MapMessage message = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.PARENT_TRUSTEE_SEND_VERIFY_CODE.name());
        if (!message.isSuccess()) {
            return message;
        }
        // 验证通过 生成预约单
        List<TrusteeReserveRecord> reserveRecords = trusteeOrderServiceClient.loadTrusteeReserveByParentId(currentUserId(), shopId);
        TrusteeReserveRecord reserveRecord = reserveRecords.stream().filter(r -> Objects.equals(r.getStudentId(), studentId)).findAny().orElse(null);
        if (reserveRecord != null) {
            return MapMessage.successMessage();
        } else {
            reserveRecord = new TrusteeReserveRecord();
            reserveRecord.setSensitiveMobile(sensitiveUserDataServiceClient.encodeMobile(mobile));
            reserveRecord.setNeedPay(false);
            reserveRecord.setParentId(currentUserId());
            reserveRecord.setStudentId(studentId);
            reserveRecord.setStatus(TrusteeReserveRecord.Status.Success);
            reserveRecord.setTrusteeId(shop.getShopId());
            reserveRecord.setSignPics(mobile);
            try {
                return AtomicLockManager.instance().wrapAtomic(trusteeOrderServiceClient)
                        .keyPrefix("ParentService:saveReserveRecord")
                        .keys(mobile, shop.getShopId())
                        .proxy().saveReserveRecord(reserveRecord);
            } catch (DuplicatedOperationException ex) {
                return MapMessage.errorMessage("您点击太快了，请重试");
            }
        }
    }


    // 付费课程推广介绍页 -- 小主持人课堂
    @RequestMapping(value = "/littlehost.vpage", method = RequestMethod.GET)
    public String littleHost(Model model) {
        Long studentId = getRequestLong("sid");
        Integer period = getRequestInt("period");
        String page = "littlehost";
        TrusteeType type = TrusteeType.XZCRKT_1;
        if (period > 0) {
            page = page + period;
            type = TrusteeType.XZCRKT_2;
        }
        // 这里对于学生ID为0的做一个兼容
        if (studentId == 0L) {
            // 获取孩子1-4年级
            List<User> students = studentLoaderClient.loadParentStudents(currentUserId());
            if (CollectionUtils.isEmpty(students)) {
                // 跳转首页
                return "redirect:/parentMobile/home/index.vpage";
            }
            Long tempId = students.stream().findFirst().get().getId();
            for (User user : students) {
                Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
                if (clazz != null && clazz.getClazzLevel().getLevel() <= 4) {
                    studentId = user.getId();
                    break;
                }
            }
            // 学生没找到
            if (studentId == 0L) {
                // 跳转首页
                return "redirect:/parentMobile/home/index.vpage?sid=" + tempId;
            }
        }

        // 过滤该学生是否已经购买成功了
        List<TrusteeOrderRecord> orderRecords = trusteeOrderServiceClient.loadTrusteeOrderByStudentId(studentId);
        final TrusteeType finalType = type;
        orderRecords = orderRecords.stream().filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid)
                .filter(o -> o.getTrusteeType() == finalType)
                .collect(Collectors.toList());
        model.addAttribute("hasPaid", CollectionUtils.isNotEmpty(orderRecords));
        model.addAttribute("trusteeType", finalType);
        model.addAttribute("studentId", studentId);
        model.addAttribute("shop", TrusteeShop.XZCRKT);
        return "/parentmobile/activity/" + page;
    }

    /**
     * 家长通《阿分题》产品介绍专题页
     */
    @RequestMapping(value = "sundry/parentIntro.vpage", method = RequestMethod.GET)
    public String fairylandIntro() {
        String url = "/resources/apps/hwh5/Sundry/V1_0_0/index.html?module=ParentIntro";
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m("domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "server_type", RuntimeMode.current().getStageMode(),
                "sid", getRequestString("sid")));

        return "redirect:" + url;
    }


    /**
     * 走遍美国家长端活动入口页面
     */
    @RequestMapping(value = "usaadventure/sundry/activity.vpage", method = RequestMethod.GET)
    public String activitys() {
        String url = "/resources/apps/hwh5/Sundry/V1_0_0/index.html";
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m("domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "server_type", RuntimeMode.current().getStageMode(),
                "sid", getRequestString("sid"),
                "module", getRequestString("module")));

        return "redirect:" + url;
    }

    /**
     * 家长通单元易错知识点导流给错题精讲&阿分题数据(41952)
     */
    @RequestMapping(value = "/easyWrongKnowPoint.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage easyWrongKnowPoint() {
        // 获取学生ID
        Long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            studentId = Long.valueOf(getCookieManager().getCookie("sid", "0"));
        }
        // 设置默认年级
        int clazzLevel = 3;

        if (studentId == 0L) {
            // 获取孩子列表
            List<User> students = studentLoaderClient.loadParentStudents(currentUserId());
            if (CollectionUtils.isNotEmpty(students)) {
                for (User user : students) {
                    Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
                    if (clazz != null && clazz.getClazzLevel() != null && clazz.getClazzLevel().getLevel() <= 6) {
                        studentId = user.getId();
                        clazzLevel = clazz.getClazzLevel().getLevel();
                        break;
                    }
                }
            }
        } else {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            if (clazz != null && clazz.getClazzLevel() != null && clazz.getClazzLevel().getLevel() <= 6) {
                clazzLevel = clazz.getClazzLevel().getLevel();
            }
        }

        MapMessage result = MapMessage.successMessage();
        result.put("studentId", studentId);
        result.put("clazzLevel", clazzLevel);

        List<MapMessage> infoList;

        switch (clazzLevel) {
            case 1:
                result.set("unitTitle", "第一单元 : 认识图形");
                result.set("unitDescription", "最易错知识点回顾");
                result.set("originCount", 19281902);
                infoList = new ArrayList<>();
                infoList.add(new MapMessage()
                        .set("title", "认识长方体和圆柱体")
                        .set("wrongCount", 278432)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c140c5f3051f1d0cf43cf2.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "认识平面图形")
                        .set("wrongCount", 217647)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c140cdf3051f1d0cf43cf3.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "认识七巧板")
                        .set("wrongCount", 169087)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c140d7f3051f1d0cf43cf4.mp4"));
                result.set("unitInfoList", infoList);
                break;
            case 2:
                result.set("unitTitle", "第一单元 : 数据收集整理");
                result.set("unitDescription", "最易错知识点回顾");
                result.set("originCount", 19281902);
                infoList = new ArrayList<>();
                infoList.add(new MapMessage()
                        .set("title", "认识统计表")
                        .set("wrongCount", 313905)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c140e7f3051f1d0cf43cf5.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "用画“正”字的方法进行数据统计")
                        .set("wrongCount", 259421)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c140edf3051f1d0cf43cf6.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "根据统计表进行数据分析")
                        .set("wrongCount", 225893)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c140f7f3051f1d0cf43cf7.mp4"));
                result.set("unitInfoList", infoList);
                break;
            case 4:
                result.set("unitTitle", "第一单元 : 四则运算");
                result.set("unitDescription", "最易错知识点回顾");
                result.set("originCount", 19281902);
                infoList = new ArrayList<>();
                infoList.add(new MapMessage()
                        .set("title", "含有小括号的运算顺序")
                        .set("wrongCount", 215421)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c14116f3051f1d0cf43cfb.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "含有小括号四则混合运算")
                        .set("wrongCount", 178942)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c1411cf3051f1d0cf43cfc.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "倍数的认识")
                        .set("wrongCount", 167894)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c14127f3051f1d0cf43cfd.mp4"));
                result.set("unitInfoList", infoList);
                break;
            case 5:
                result.set("unitTitle", "第一单元 : 观察物体（三）");
                result.set("unitDescription", "最易错知识点回顾");
                result.set("originCount", 19281902);
                infoList = new ArrayList<>();
                infoList.add(new MapMessage()
                        .set("title", "从两个方向观察物体")
                        .set("wrongCount", 289645)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c14134f3051f1d0cf43cfe.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "从不同角度观察物体")
                        .set("wrongCount", 243225)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c1413af3051f1d0cf43cff.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "从同一角度观察物体")
                        .set("wrongCount", 232541)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c14140f3051f1d0cf43d00.mp4"));
                result.set("unitInfoList", infoList);
                break;
            case 6:
                result.set("unitTitle", "第一单元 : 负数");
                result.set("unitDescription", "最易错知识点回顾");
                result.set("originCount", 19281902);
                infoList = new ArrayList<>();
                infoList.add(new MapMessage()
                        .set("title", "在直线上表示出正负数")
                        .set("wrongCount", 358963)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c1414bf3051f1d0cf43d01.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "负数的计算")
                        .set("wrongCount", 325678)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c14152f3051f1d0cf43d02.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "认识正负数")
                        .set("wrongCount", 267832)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c14157f3051f1d0cf43d03.mp4"));
                result.set("unitInfoList", infoList);
                break;
            default:
                result.set("unitTitle", "第一单元 : 位置与方向（一）");
                result.set("unitDescription", "最易错知识点回顾");
                result.set("originCount", 19281902);
                infoList = new ArrayList<>();
                infoList.add(new MapMessage()
                        .set("title", "认识东南、西南、东北、西北")
                        .set("wrongCount", 167886)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c14100f3051f1d0cf43cf8.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "认识东、南、西、北")
                        .set("wrongCount", 153125)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c14103f3051f1d0cf43cf9.mp4"));
                infoList.add(new MapMessage()
                        .set("title", "关于东、南、西、北的实际问题")
                        .set("wrongCount", 123421)
                        .set("videoThumbUrl", "")
                        .set("videoUrl", "http://v.17zuoye.cn/content/58c1410af3051f1d0cf43cfa.mp4"));
                result.set("unitInfoList", infoList);
                break;
        }
        return result;
    }

    private String getCouponId(Subject subject) {
        if (RuntimeMode.current().ge(Mode.STAGING)) {
            switch (subject) {
                case ENGLISH:
                    return "595e0ad95d9d4b13e2c8741d";
                case MATH:
                    return "595e0b815d9d4b13e2c87926";
                case CHINESE:
                    return "595e0c12e485abed92900da8";
                default:
                    return null;
            }
        } else {
            switch (subject) {
                case ENGLISH:
                    return "595e0d42777487283bb95c76";
                case MATH:
                    return "595e0c8b777487283bb95c6d";
                case CHINESE:
                    return "595e0d01e92b1b7503a39e4e";
                default:
                    return null;
            }
        }
    }
}
