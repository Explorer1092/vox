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
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.cdn.url2.config.CdnConfig;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.o2o.TrusteeReserveRecord;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseCategory;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.CoursePeriodUserRef;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourse;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriodRef;
import com.voxlearning.utopia.service.mizar.api.entity.oa.UserOfficialAccountsRef;
import com.voxlearning.utopia.service.mizar.api.mapper.CoursePeriodMapper;
import com.voxlearning.utopia.service.mizar.api.service.talkfun.TalkFunService;
import com.voxlearning.utopia.service.mizar.api.utils.MicroCourseMsgTemplate;
import com.voxlearning.utopia.service.mizar.consumer.loader.MicroCourseLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MicroCourseServiceClient;
import com.voxlearning.utopia.service.mizar.talkfun.TalkFunConstants;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.TrusteeOrderServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Yuechen.Wang on 2016/12/16.
 * 微课堂相关
 */
@Controller
@RequestMapping(value = "/mizar")
@Slf4j
public class MobileParentMicroCourseController extends AbstractMobileController {

    private final String imageSuffix = "@50w_1o_75q";

    @Inject private RaikouSystem raikouSystem;

    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private MicroCourseLoaderClient microCourseLoaderClient;
    @Inject private MicroCourseServiceClient microCourseServiceClient;
    @Inject private TrusteeOrderServiceClient trusteeOrderServiceClient;

    @ImportService(interfaceClass = TalkFunService.class)
    private TalkFunService talkFunService;

    // 线上1分钱测试家长ID账号
    private static final List<Long> freeParentIds = Arrays.asList(27398018L, 27398020L, 27398022L, 27398025L, 27398027L, 27398030L,
            27398035L, 27398036L, 27398040L, 27398042L, 27398041L, 27398038L, 27398029L, 27398032L, 27398047L, 27398049L,
            27398054L, 27398055L);

    // 微课堂课程中间页
    @RequestMapping(value = "/course/microcourse-{type}.vpage", method = RequestMethod.GET)
    public String microCourse(Model model, @PathVariable String type) {
        Long parentId = currentUserId();
        User parent = currentUser();
        model.addAttribute("logged", parentId != null && parent != null);
        if ("open".equals(type)) {
            model.addAttribute("category", MizarCourseCategory.MICRO_COURSE_OPENING.name());
        } else if ("normal".equals(type)) {
            model.addAttribute("category", MizarCourseCategory.MICRO_COURSE_NORMAL.name());
        } else {
            model.addAttribute("result", MapMessage.errorMessage("微课堂类型错误~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
//        // 微课堂目前仅支持三、四年级课程
//        Range<Integer> gradeRange = Range.between(3, 4);
//        // 根据家长的孩子所在年级
//        List<Long> childrenIds = parentLoaderClient.loadParentStudentRefs(parentId)
//                .stream().map(StudentParentRef::getStudentId).collect(Collectors.toList());
//        boolean available = studentLoaderClient.loadStudentDetails(childrenIds).values()
//                .stream().anyMatch(child -> gradeRange.contains(child.getClazzLevelAsInteger()));
//        model.addAttribute("available", available);
        // 进入微课堂 主动关注公众号
        autoAttentPublicNo(parentId);
        return "mizar/course/microcourselist";
    }

    // 短信打开app跳转链接
    @RequestMapping(value = "/course/microopenapp.vpage")
    public String microOpen() {
        return "mizar/course/microopenapp";
    }

    // 教研直播运营页
    @RequestMapping(value = "/course/liveopration.vpage")
    public String liveopration() {
        return "mizar/course/liveopration";
    }

    // 微课堂课时详情
    @RequestMapping(value = "/course/courseperiod.vpage", method = RequestMethod.GET)
    public String coursePeriod(Model model) {
        String periodId = getRequestString("period");
        Long parentId = currentUserId();
        String track = getRequestString("track");
        CoursePeriodMapper periodMapper = microCourseLoaderClient.loadPeriodById(periodId);
        if (periodMapper == null) {
            model.addAttribute("result", MapMessage.errorMessage("您访问的课程已下线").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "seattle/errorinfo";
        }
        int status = calStatus(parentId, periodMapper);
        // 如果已经购买/预约且结束了没到1个小时, 提示正在生成回放
        boolean waiting = DateUtils.minuteDiff(new Date(), periodMapper.getEndTime()) < 60 && !talkFunService.checkHasReplay(periodMapper.getPeriodId());

        // 参数加入 MODEL
        model.addAttribute("course", periodMapper);
        model.addAttribute("track", track);
        model.addAttribute("timeFlag", calTimeFlag(periodMapper));
        model.addAttribute("status", status);
        model.addAttribute("waiting", waiting);
        model.addAttribute("id", periodMapper.targetId());
        model.addAttribute("payAll", periodMapper.payAll());
        model.addAttribute("pid", parentId);
        return "mizar/course/microdetail";
    }

    // 教研员运营直播页
    @RequestMapping(value = "/course/courseperiods.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage coursePeriods() {
        Long parentId = currentUserId();
        if (parentId == null || parentId == 0) {
            return MapMessage.errorMessage("请登录后重试").set("login", false);
        }
        String periodId1 = getRequestString("period1");
        String periodId2 = getRequestString("period2");
        String periodId3 = getRequestString("period3");
        CoursePeriodMapper periodMapper1 = microCourseLoaderClient.loadPeriodById(periodId1);
        CoursePeriodMapper periodMapper2 = microCourseLoaderClient.loadPeriodById(periodId2);
        CoursePeriodMapper periodMapper3 = microCourseLoaderClient.loadPeriodById(periodId3);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.putAll(buildResult(periodMapper1, parentId, 1));
        mapMessage.putAll(buildResult(periodMapper2, parentId, 2));
        mapMessage.putAll(buildResult(periodMapper3, parentId, 3));
        mapMessage.set("pid", parentId);
        return mapMessage;
    }

    // 支付中间页
    @RequestMapping(value = "microcourse/pay.vpage", method = RequestMethod.GET)
    public String microCoursePayment(Model model) {
        // 获取参数
        String id = getRequestString("id");
        boolean payAll = getRequestBool("payAll");
        MapMessage message = validate(id, payAll);
        if (!message.isSuccess()) {
            model.addAttribute("result", message);
            return "seattle/errorinfo";
        }
        // 校验课程名称和价格
        if (StringUtils.isBlank(SafeConverter.toString(message.get("name"))) || SafeConverter.toDouble(message.get("price")) <= 0) {
            model.addAttribute("result", MapMessage.errorMessage("您访问的课程不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "seattle/errorinfo";
        }
        model.addAttribute("info", message);
        return "mizar/course/pay";
    }

    // 生成订单
    @RequestMapping(value = "microcourse/order.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createMicroCourseOrder() {
        // 接收参数
        String id = getRequestString("id");
        boolean payAll = getRequestBool("payAll");
        String comment = getRequestString("comment");
        String track = getRequestString("track");
        String refer = getRequestParameter("refer", "17parent");
        Long parentId = currentUserId();
        User parent = currentParent();
        // 校验信息
        if (StringUtils.isBlank(id) || parent == null) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage validMsg = validate(id, payAll);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderList(parentId);
        // 过滤是否已经购买过了 如果购买过， 给一个提示
        UserOrder paidOrder = userOrders.stream()
                .filter(o -> id.equals(o.getProductId()))
                .filter(o -> OrderType.micro_course == o.getOrderType())
                .filter(o -> OrderStatus.Confirmed == o.getOrderStatus())
                .filter(o -> PaymentStatus.Paid == o.getPaymentStatus())
                .findFirst()
                .orElse(null);
        if (paidOrder != null) {
            return MapMessage.errorMessage("您已成功购买该产品，请去我的课程查看。").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }

        // 看看该类型是否有未支付订单， 如果有，直接用现有的订单
        UserOrder order = userOrders.stream()
                .filter(o -> id.equals(o.getProductId()))
                .filter(o -> OrderType.micro_course == o.getOrderType())
                .filter(o -> OrderStatus.New == o.getOrderStatus())
                .filter(o -> PaymentStatus.Unpaid == o.getPaymentStatus())
                .findFirst()
                .orElse(null);
        if (order != null) {
            return MapMessage.successMessage().add("orderId", order.genUserOrderId());
        }

        // 生成订单
        order = UserOrder.newOrder(OrderType.micro_course, parent.getId());
        order.setUserId(parent.getId());
        order.setUserName(parent.getProfile().getRealname());
        order.setProductId(id);
        order.setProductName(SafeConverter.toString(validMsg.get("name")));
        order.setComment(comment);
        order.setProductAttributes(String.valueOf(payAll));
        order.setUserReferer(track);  // 用户来源
        order.setOrderReferer(refer); // 订单来源
        order.setOrderProductServiceType(OrderProductServiceType.MicroCourse.name());
        // 1分钱测试
        if (RuntimeMode.ge(Mode.PRODUCTION) && freeParentIds.contains(parentId)) {
            order.setOrderPrice(new BigDecimal(0.01));
        } else {
            order.setOrderPrice(new BigDecimal(SafeConverter.toDouble(validMsg.get("price"))));
        }
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
        order.setSensitiveMobile(ua.getSensitiveMobile());
        // 在此处多保存一些线索信息
        StudentParentRef userRef = parentLoaderClient.loadParentStudentRefs(parentId)
                .stream().findFirst().orElse(null);
        if (userRef != null) {
            Map<String, Object> ext = new HashMap<>();
            ext.put("sid", userRef.getStudentId());
            ext.put("callName", userRef.getCallName());
            User child = raikouSystem.loadUser(userRef.getStudentId());
            ext.put("sname", child == null ? null : child.getProfile().getRealname());
            order.setExtAttributes(JsonUtils.toJson(ext));
        }
        MapMessage returnMsg;
        try {
            returnMsg = AtomicLockManager.instance().wrapAtomic(userOrderServiceClient)
                    .keyPrefix("UserOrderService:saveUserOrder")
                    .keys(parentId, id)
                    .proxy().saveUserOrder(order);
            if (returnMsg.isSuccess()) {
                return MapMessage.successMessage().add("orderId", order.genUserOrderId());
            }
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("Create micro course order failed,parent={},id={},payAll={}", parentId, id, payAll, ex);
            returnMsg = MapMessage.errorMessage("订单生成失败");
        }
        return returnMsg;
    }

    @RequestMapping(value = "microcourse/reserve.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reserveMicroCourse() {
        Long parentId = currentUserId();
        User parent = currentParent();
        String id = getRequestString("id");
        boolean payAll = getRequestBool("payAll");
        String track = getRequestParameter("track", MicroCourseMsgTemplate.APP_DEFAULT_TRACK);
        String refer = getRequestParameter("refer", "17parent");
        if (parentId == null || parentId == 0 || parent == null) {
            return MapMessage.errorMessage("请登录后重试");
        }
        MapMessage validMsg = validate(id, payAll);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        // 查看是否已经有成功预约的记录，预约成功直接返回
        if (hasReserved(parentId, id)) {
            return MapMessage.successMessage();
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
        TrusteeReserveRecord reserveRecord = new TrusteeReserveRecord();
        reserveRecord.setParentId(parentId);
        reserveRecord.setStatus(TrusteeReserveRecord.Status.Success);
        reserveRecord.setActivityId(0L);
        reserveRecord.setTrack(track);
        reserveRecord.setTargetId(id);
        reserveRecord.setNeedPay(payAll);
        reserveRecord.setSensitiveMobile(ua.getSensitiveMobile());
        reserveRecord.setSignPics(refer); // 对于微课堂的订单，此字段表示来源
        // 在此处多保存一些线索信息
        StudentParentRef userRef = parentLoaderClient.loadParentStudentRefs(parentId)
                .stream().findFirst().orElse(null);
        if (userRef != null) {
            reserveRecord.setStudentId(userRef.getStudentId());
        }
        try {
            MapMessage message = AtomicLockManager.instance().wrapAtomic(trusteeOrderServiceClient)
                    .keyPrefix("ParentService:saveReserveRecord")
                    .keys(parentId, id)
                    .proxy().saveReserveRecord(reserveRecord);
            User user = currentUser();
            if (message.isSuccess() && user.fetchUserType() != UserType.TEACHER) {
                processAfterReserveSuccess(parentId, validMsg, id, payAll, "wechat_parent".equals(refer));
            }
            return message;
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("Failed to reserve micro course, user={}, id={}, payAll={}", parentId, id, payAll, ex);
        }
        return MapMessage.errorMessage("预约课程失败");
    }

    @RequestMapping(value = "microcourse/entrance.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage courseEntrance(Model model) {
        String periodId = getRequestString("period"); // 课时的ID
        Long userId = currentUserId();
        User user = currentUser();
        boolean https = Objects.isNull(getRequestParameter("_protocol", null));
        if (userId == null || user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        // 判断获取token开关是否关闭
        String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_PARENT.name(), "GET_TALK_FUN_TOKEN");
        if (!ConversionUtils.toBool(config)) {
            return MapMessage.errorMessage("获取token开关已关闭").set("code", "DOWNGRADE");
        }
        // 判断超时开关是否已开启
        CacheObject<Boolean> flag = washingtonCacheSystem.CBS.flushable.get("talk_fun_token_flag");
        if (flag.getValue() != null && flag.getValue()) {
            return MapMessage.errorMessage("请求超时，降级已开启").set("code", "DOWNGRADE");
        }
        try {
            Map<String, String> userInfo = fetchUserInfo(userId, user.fetchUserType());
            Map<String, Object> options = MapUtils.m("avatar", userInfo.get("avatar"), "ssl", https);
            MapMessage result = talkFunService.generateUrl(periodId, userId.toString(), userInfo.get("nickname"), TalkFunConstants.ROLE_USER, options);
            if (!result.isSuccess()) {
                return result;
            }
            String url = ConversionUtils.toString(result.get("url"));
            int mode = ConversionUtils.toBool(result.get("replay")) ? 2 : 1; //2.回访 1.直播
            AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING)
                    .post(url)
                    .socketTimeout(3 * 1000)
                    .execute();
            if (response.getHttpClientException() != null) {//无法直接捕获超时异常，故做此操作
                throw response.getHttpClientException();
            }
            MapMessage tmp = this.parseJson(response);
            if (!tmp.isSuccess()) {
                return tmp;
            }
            String token = ConversionUtils.toString(tmp.get("token"));
            return MapMessage.successMessage().set("accessToken", token).set("rMode", mode);
        } catch (SocketTimeoutException e) {
            // 超时次数加1,超过5次超时 开关置为true
            Long count = washingtonCacheSystem.CBS.flushable.incr("talk_fun_token_count", 1, 1, 60);
            if (count != null && ConversionUtils.toInt(count) == 5) {
                washingtonCacheSystem.CBS.flushable.add("talk_fun_token_flag", 60, true);
            }
            return MapMessage.errorMessage("当前访问的用户过多，请稍后再试");// 请求超时
        } catch (Exception ex) {
            logger.error("Failed get access token, id={}", periodId, ex);
            return MapMessage.errorMessage("获取Token失败");
        }
    }

    @RequestMapping(value = "microcourse/access.vpage", method = RequestMethod.GET)
    @SuppressWarnings("unchecked")
    public String accessCourse(Model model) {
        String periodId = getRequestString("period");
        String track = getRequestString("track");
        boolean https = Objects.isNull(getRequestParameter("_protocol", null));
        Long userId = currentUserId();
        User user = currentUser();
        if (userId == null || user == null) {
            return "redirect:/";
        }
        String redirectUrl = "redirect:" + UrlUtils.buildUrlQuery("/mizar/course/courseperiod.vpage", MapUtils.m("period", periodId, "track", track));
        try {
            Map<String, String> userInfo = fetchUserInfo(userId, user.fetchUserType());
            Map<String, Object> options = MapUtils.m("avatar", userInfo.get("avatar"), "ssl", https);
            MapMessage message = talkFunService.accessCourse(periodId, userId.toString(), userInfo.get("nickname"), TalkFunConstants.ROLE_USER, options);
            if (!message.isSuccess()) {
                model.addAttribute("result", MapMessage.errorMessage(message.getInfo()).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return "seattle/errorinfo";
            }
            model.addAttribute("text", message.get("text"));
            model.addAttribute("link", message.get("link"));
            model.addAttribute("periodId", periodId);
            model.addAttribute("periodName", message.get("title"));
            Map<String, Object> data = (Map<String, Object>) message.get("data");
            String liveUrl;
            if (SafeConverter.toBoolean(message.get("live"))) {
                liveUrl = SafeConverter.toString(data.get("liveUrl"));
            } else {
                liveUrl = SafeConverter.toString(data.get("playbackUrl"));
            }
            if (StringUtils.isBlank(liveUrl)) {
                logger.error("Failed access Talk-Fun Course, LiveUrl is Empty, id={}", periodId);
                return redirectUrl;
            }
            model.addAttribute("liveUrl", liveUrl);
            model.addAttribute("track", track);
            return "mizar/course/courselive";
        } catch (Exception ex) {
            logger.error("Failed access Talk-Fun Course, id={}", periodId, ex);
            return redirectUrl;
        }
    }

    @RequestMapping(value = "microcourse/newgate.vpage", method = RequestMethod.GET)
    public String accessCourseNew(Model model) {
        String periodId = getRequestString("period");
        String track = getRequestString("track");
        boolean https = Objects.isNull(getRequestParameter("_protocol", null));
        Long userId = currentUserId();
        User user = currentUser();
        if (userId == null || user == null) {
            return "redirect:/";
        }
        String redirectUrl = "redirect:" + UrlUtils.buildUrlQuery("/mizar/course/courseperiod.vpage", MapUtils.m("period", periodId, "track", track));
        try {
            Map<String, String> userInfo = fetchUserInfo(userId, user.fetchUserType());
            Map<String, Object> options = MapUtils.m("avatar", userInfo.get("avatar"), "ssl", https);
            MapMessage message = talkFunService.courseEntrance(periodId, userId.toString(), userInfo.get("nickname"), TalkFunConstants.ROLE_USER, options);
            if (!message.isSuccess()) {
                model.addAttribute("result", MapMessage.errorMessage(message.getInfo()).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return "seattle/errorinfo";
            }
            model.addAttribute("text", message.get("text"));
            model.addAttribute("link", message.get("link"));
            model.addAttribute("periodId", periodId);
            model.addAttribute("periodName", message.get("title"));

            String liveUrl = SafeConverter.toString(message.get("entrance"));
            if (StringUtils.isBlank(liveUrl)) {
                logger.error("Failed access Talk-Fun Course, LiveUrl is Empty, id={}", periodId);
                return redirectUrl;
            }
            model.addAttribute("liveUrl", liveUrl);
            model.addAttribute("track", track);
            return "mizar/course/courselive";
        } catch (Exception ex) {
            logger.error("Failed access Talk-Fun Course, id={}", periodId, ex);
            return redirectUrl;
        }
    }

    @RequestMapping(value = "microcourse/newentrance.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage entranceCourseNew(Model model) {
        // 判断是否降级
        int tmp = RandomUtils.nextInt(1, 10);
        String threshold = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_PARENT.name(), "DOWNGRADE_THRESHOLD");
        if (tmp < ConversionUtils.toInt(threshold)) { //降级 走H5逻辑 否则走壳的逻辑
            return MapMessage.errorMessage("降级到H5");
        }
        String periodId = getRequestString("period");
        String track = getRequestString("track");
        boolean https = Objects.isNull(getRequestParameter("_protocol", null));
        Long userId = currentUserId();
        User user = currentUser();
        if (userId == null || user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        try {
            Map<String, String> userInfo = fetchUserInfo(userId, user.fetchUserType());
            Map<String, Object> options = MapUtils.m("avatar", userInfo.get("avatar"), "ssl", https);
            MapMessage message = talkFunService.fetchAccessKey(periodId, userId.toString(), userInfo.get("nickname"), TalkFunConstants.ROLE_USER, options);
            return message;
        } catch (Exception ex) {
            logger.error("Failed to get access key, id={}", periodId, ex);
            return MapMessage.errorMessage("获取accessKey失败");
        }
    }

    private Map<String, String> fetchUserInfo(Long parentId, UserType userType) {
        String nickname = null;
        String avatar = null;
        if (userType == UserType.TEACHER) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(parentId);
            if (teacherDetail != null) {
                nickname = StringUtils.isNotBlank(teacherDetail.fetchRealname()) ? teacherDetail.fetchRealname() : null;
                avatar = StringUtils.isNotBlank(teacherDetail.fetchImageUrl()) ? CdnConfig.getAvatarDomain().getValue() + "/gridfs/" + teacherDetail.fetchImageUrl() + imageSuffix : "";
                return MapUtils.map("nickname", nickname, "avatar", avatar);
            }
        }
        StudentParentRef ref = parentLoaderClient.loadParentStudentRefs(parentId).stream()
                .findFirst().orElse(null);
        if (ref != null) {
            StudentDetail firstChild = studentLoaderClient.loadStudentDetail(ref.getStudentId());
            if (firstChild != null) {
                nickname = StringUtils.isNotBlank(firstChild.fetchRealname()) ? firstChild.fetchRealname() + ref.getCallName() : null;
                avatar = StringUtils.isNotBlank(firstChild.fetchImageUrl()) ? CdnConfig.getAvatarDomain().getValue() + "/gridfs/" + firstChild.fetchImageUrl() + imageSuffix : "";
            }
        }
        if (nickname == null) nickname = "一起作业用户";
        return MapUtils.map("nickname", nickname, "avatar", avatar);
    }

    private MapMessage validate(String id, boolean payAll) {
        if (!payAll) {
            // 按课时购买
            MicroCoursePeriod period = microCourseLoaderClient.getCourseLoader().loadCoursePeriod(id);
            if (period == null) {
                return MapMessage.errorMessage("您访问的课程不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
            return MapMessage.successMessage().add("name", period.getTheme())
                    .add("cnt", 1).add("price", period.getPrice())
                    .add("tip", period.getTip()).add("pid", period.getId());
        }

        // 按课程购买
        MicroCourse course = microCourseLoaderClient.getCourseLoader().loadMicroCourse(id);
        if (course == null) {
            return MapMessage.errorMessage("您访问的课程不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        List<MicroCoursePeriodRef> refs = microCourseLoaderClient.getCourseLoader().findCoursePeriodRefByCourse(id);
        String pid = refs.stream().sorted(Comparator.comparing(MicroCoursePeriodRef::getCreateTime))
                .map(MicroCoursePeriodRef::getPeriodId).findFirst().orElse(null);
        if (refs.size() <= 0 || StringUtils.isBlank(pid)) {
            return MapMessage.errorMessage("您访问的课程不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        return MapMessage.successMessage().add("name", course.getName())
                .add("cnt", refs.size()).add("price", course.getPrice())
                .add("tip", course.getTip()).add("pid", pid);
    }

    private void processAfterReserveSuccess(Long parentId, MapMessage validMsg, String id, boolean payAll, boolean fromWechat) {
        // 预约成功，关注公众号
        String prodName = SafeConverter.toString(validMsg.get("name"));
        Long accountId = MicroCourseMsgTemplate.accountId();
        if (!officialAccountsServiceClient.isFollow(accountId, parentId)) {
            officialAccountsServiceClient.updateFollowStatus(parentId, accountId, UserOfficialAccountsRef.Status.Follow);
        }
        // 发送一条预约成功的公众号消息
        String title = MicroCourseMsgTemplate.reserveTitle(prodName);
        String content;
        String pid = SafeConverter.toString(validMsg.get("pid"));
        String extInfo = MicroCourseMsgTemplate.extInfo();

        List<MicroCoursePeriod> periods;
        if (payAll) {
            // 如果是按照课程购买
            periods = microCourseLoaderClient.loadCoursePeriods(id);
            content = MicroCourseMsgTemplate.reserveContent(null, prodName);
        } else {
            MicroCoursePeriod period = microCourseLoaderClient.getCourseLoader().loadCoursePeriod(id);
            // 如果是按照课时购买
            content = MicroCourseMsgTemplate.reserveContent(period.getStartTime(), prodName);
            periods = Collections.singletonList(period);
        }
        officialAccountsServiceClient.sendMessage(Collections.singletonList(parentId), title, content, MicroCourseMsgTemplate.appLinkUrl(pid), extInfo, true);
        // 发送微信公众号消息
        if (fromWechat) {
            Map<String, Object> extensionInfo = MapUtils.m(
                    "first", "",
                    "keyword1", title,
                    "keyword2", content,
                    "remark", "",
                    "url", MicroCourseMsgTemplate.wechatLinkUrl(RuntimeMode.current(), pid)
            );
            wechatServiceClient.getWechatService().processWechatNoticeNoWait(WechatNoticeProcessorType.ParentOperationalNotice, parentId, extensionInfo, WechatType.PARENT);
        }
        registerPeriodNotify(periods, parentId, fromWechat);
    }

    private void registerPeriodNotify(List<MicroCoursePeriod> periods, Long parentId, boolean fromWechat) {
        if (CollectionUtils.isEmpty(periods) || parentId == null) {
            return;
        }
        periods.forEach(period -> {
            // 记录一条课时与用户的关联
            microCourseServiceClient.savePeriodUserRef(period.getId(), String.valueOf(parentId), null, CoursePeriodUserRef.UserPeriodRelation.Reserve, fromWechat);
            if (fromWechat) {
                registerWeChatNotify(parentId, period);   // 如果是通过微信注册一条定时发送的推送
            } else {
                registerSmsNotify(parentId, period);   // 如果是通过APP注册一条定时发送的短信
            }
        });
    }

    private void registerSmsNotify(Long parentId, MicroCoursePeriod period) {
        // 注册一条定时发送的短信
        if (!Boolean.TRUE.equals(period.getSmsNotify())) {
            return;
        }
        // 过期之后就不发送短信了
        if (new Date().after(period.getStartTime())) {
            return;
        }
        // 获取用户的手机号码
        String mobile = sensitiveUserDataServiceClient.loadUserMobile(parentId);
        if (!MobileRule.isMobile(mobile) || badWordCheckerClient.containsMobileNumBadWord(mobile)) {
            return;
        }
        String urlPrefix = ProductConfig.getMainSiteBaseUrl(); //获取地址前缀
        String longUrl = urlPrefix + "/mizar/course/microopenapp.vpage?period=" + period.getId() + "&track=message";
        String shortUrl = ShortUrlGenerator.generateShortUrl(longUrl, true).get();
        String sms = MicroCourseMsgTemplate.sms(period.getTheme(), period.getStartTime(), ShortUrlGenerator.getShortUrlSiteUrl() + "/" + shortUrl);
        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setSmsContent(sms);
        smsMessage.setType(SmsType.MICRO_COURSE_REMIND.name());
        smsMessage.setVoice(false);
        smsMessage.setMobile(mobile);
        // 预约短信发送时间是课程开始时间的前15分钟
        Date time = DateUtils.addMinutes(period.getStartTime(), -15);
        // 如果是开课前15分钟内购买，不设置发送时间
        if (new Date().before(time)) {
            smsMessage.setSendTime(DateUtils.dateToString(time, "yyyyMMddHHmmss"));
        }
        smsServiceClient.getSmsService().sendSms(smsMessage);
    }

    private void registerWeChatNotify(Long parentId, MicroCoursePeriod period) {
        // 注册一条定时发送的短信
        Map<String, Object> extensionInfo = MapUtils.m(
                "first", "",
                "keyword1", MicroCourseMsgTemplate.title(period.getTheme()),
                "keyword2", MicroCourseMsgTemplate.content(period.getTheme(), period.getStartTime()),
                "remark", "",
                "url", MicroCourseMsgTemplate.wechatLinkUrl(RuntimeMode.current(), period.getId())
        );
        Date time = DateUtils.addMinutes(period.getStartTime(), -15);
        // 如果是开课前15分钟内购买，不设置发送时间
        if (new Date().before(time)) {
            extensionInfo.put("sendTime", time);
        }
        wechatServiceClient.getWechatService().processWechatNoticeNoWait(
                WechatNoticeProcessorType.ParentOperationalNotice, parentId, extensionInfo, WechatType.PARENT
        );
    }

    // 定义一下 BEFORE表示课程还未开始，ING课程进行中，AFTER表示课程已经结束
    private String calTimeFlag(CoursePeriodMapper mapper) {
        Date now = new Date();
        // 开始前15分钟以前提示还未开始
        if (mapper.getStartTime().after(DateUtils.addMinutes(now, 15))) {
            return "BEFORE";
        }
        // 课程时间结束之后
        if (now.after(mapper.getEndTime())) {
            // 若老师已经下课 或 超出15分钟
            if (talkFunService.checkClassFinished(mapper.getPeriodId()) || now.after(DateUtils.addMinutes(mapper.getEndTime(), 15)))
                return "AFTER";
        }
        // 其他时间就当是正在上课了
        return "ING";
    }

    // 定义一下 0表示未购买未预约，1表示已经购买，2表示已经预约
    private int calStatus(Long parentId, CoursePeriodMapper mapper) {
        // 按课程购买时, 查看是否已经有成功预约的记录
        if (hasReserved(parentId, mapper) && mapper.isReserve()) {
            return 2;
        }
        // 查看当前课时是否已经下了订单
        if (hasPaid(parentId, mapper) && !mapper.isReserve()) {
            return 1;
        }
        return 0;
    }

    private boolean hasPaid(Long parentId, CoursePeriodMapper mapper) {
        if (parentId == null || mapper == null) {
            return false;
        }
        return userOrderLoaderClient.loadUserOrderList(parentId)
                .stream()
                .anyMatch(o -> (StringUtils.equals(mapper.getCourseId(), o.getProductId()) || StringUtils.equals(mapper.getPeriodId(), o.getProductId()))  // 兼容如果取消了按课程购买
                        && OrderType.micro_course == o.getOrderType()
                        && OrderStatus.Confirmed == o.getOrderStatus()
                        && PaymentStatus.Paid == o.getPaymentStatus()
                );
    }

    private boolean hasReserved(Long parentId, CoursePeriodMapper mapper) {
        if (parentId == null || mapper == null) {
            return false;
        }
        return trusteeOrderServiceClient.loadTrusteeReserveByParentId(parentId)
                .stream()
                .anyMatch(r -> (StringUtils.equals(mapper.getCourseId(), r.getTargetId()) || StringUtils.equals(mapper.getPeriodId(), r.getTargetId()))  // 兼容如果取消了按课程购买
                        && TrusteeReserveRecord.Status.Success == r.getStatus()
                );
    }

    private boolean hasReserved(Long parentId, String id) {
        if (parentId == null || StringUtils.isBlank(id)) {
            return false;
        }
        return trusteeOrderServiceClient.loadTrusteeReserveByParentId(parentId)
                .stream()
                .anyMatch(r -> StringUtils.equals(id, r.getTargetId())
                        && TrusteeReserveRecord.Status.Success == r.getStatus()
                );
    }

    private MapMessage parseJson(AlpsHttpResponse response) {
        String json = response.getResponseString(Charset.forName("UTF-8"));
        Map<String, Object> resMap = JsonUtils.convertJsonObjectToMap(json);
        if (ConversionUtils.toInt(resMap.get("code")) != 0) {
            return MapMessage.errorMessage(ConversionUtils.toString(resMap.get("msg")));
        }

        if (resMap.get("data") != null) {
            Map<String, Object> map = (Map<String, Object>) resMap.get("data");
            String token = ConversionUtils.toString(map.get("access_token"));
            if (token != null) {
                return MapMessage.successMessage().set("token", token);
            }
        }
        return MapMessage.errorMessage("解析token失败");
    }

    private void autoAttentPublicNo(Long parentId) {
        // app进入微课堂就关注公众号
        Long accountId = MicroCourseMsgTemplate.accountId();
        if (!officialAccountsServiceClient.isFollow(accountId, parentId)) {
            officialAccountsServiceClient.updateFollowStatus(parentId, accountId, UserOfficialAccountsRef.Status.Follow);
        }
    }

    private MapMessage buildResult(CoursePeriodMapper coursePeriodMapper, Long parentId, int suffix) {
        MapMessage mapMessage = MapMessage.successMessage();
        if (coursePeriodMapper != null && coursePeriodMapper.getEndTime() != null && coursePeriodMapper.getStartTime() != null && coursePeriodMapper.getPeriodId() != null) {
            int status = calStatus(parentId, coursePeriodMapper);
            // 如果已经购买/预约且结束了没到1个小时, 提示正在生成回放
            boolean waiting = DateUtils.minuteDiff(new Date(), coursePeriodMapper.getEndTime()) < 60 && !talkFunService.checkHasReplay(coursePeriodMapper.getPeriodId());
            mapMessage.set("status" + suffix, status);
            mapMessage.set("waiting" + suffix, waiting);
            mapMessage.set("timeFlag" + suffix, calTimeFlag(coursePeriodMapper));
            mapMessage.set("payAll" + suffix, coursePeriodMapper.payAll());
            mapMessage.set("offline" + suffix, false);
        } else {
            mapMessage.set("offline" + suffix, true);
        }
        return mapMessage;
    }

}
