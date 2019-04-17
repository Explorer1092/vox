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

package com.voxlearning.wechat.controller.parent;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.TrusteeShop;
import com.voxlearning.utopia.api.constant.TrusteeType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.ObjectIdEntity;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.o2o.TrusteeOrderRecord;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.afenti.consumer.AfentiLoaderClient;
import com.voxlearning.utopia.service.integral.api.entities.Integral;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.TrusteeOrderServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.AbtestMapper;
import com.voxlearning.utopia.service.user.api.mappers.AuthenticatedMobile;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeSnapshot;
import com.voxlearning.wechat.constants.WechatInfoCode;
import com.voxlearning.wechat.controller.AbstractParentWebController;
import com.voxlearning.wechat.support.UserAbtestLoaderClientHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.PicListen;

/**
 * @author Xin Xin
 * @since 10/29/15
 */
@Slf4j
@Controller
@RequestMapping(value = "/parent/ucenter")
public class ParentUcenterController extends AbstractParentWebController {

    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;

    @Inject private IntegralLoaderClient integralLoaderClient;

    @Inject private UserAbtestLoaderClientHelper userAbtestLoaderClientHelper;

    @Inject private TrusteeOrderServiceClient trusteeOrderServiceClient;

    //会员中心首页
    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Long parentId = getRequestContext().getUserId();
        // 验证是否全部选择了家长角色
        String url = callNameAvailable(parentId);
        if (StringUtils.isNotBlank(url)) {
            return "redirect:" + url;
        }
        List<User> children = studentLoaderClient.loadParentStudents(parentId);

        // test abtest
        try {
            AbtestMapper abtestMapper = userAbtestLoaderClientHelper.generateUserAbtestInfo(parentId, "583ac7b52ed9b64d4b8d140f");
            Map<String, String> logInfo = new HashMap<>();
            logInfo.put("module", "abtest");
            logInfo.put("op", "abtest_at_wechat_ucenter_page");
            logInfo.put("s0", getRequestContext().getAuthenticatedOpenId());
            logInfo.put("experimentId", abtestMapper.getExperimentId());
            logInfo.put("groupId", abtestMapper.getGroupId());
            logInfo.put("planId", abtestMapper.getPlanId());
            super.log(logInfo);
        } catch (Exception exp) {
            logger.debug("get abtest user info failed");
        }


        //查订单数
        List<UserOrder> orders = new ArrayList<>();
        if (children.size() > 0) {
            for (User child : children) {
                orders.addAll(userOrderLoaderClient.loadUserOrderList(child.getId()));
            }
        }
        List<TrusteeOrderRecord> records = trusteeOrderServiceClient.loadTrusteeOrderByParentId(parentId);
        model.addAttribute("orderCount", orders.size() + records.size());

        //查消息数
        List<WechatNoticeSnapshot> noticeSnapshots = getMessages(parentId);
        model.addAttribute("msgCount", noticeSnapshots.size());

        //手机号
        String mobileObscured = sensitiveUserDataServiceClient.loadUserMobileObscured(parentId);
        if (!StringUtils.isEmpty(mobileObscured)) {
            model.addAttribute("mobile", mobileObscured);
        }

        //学豆余额
        User user = userLoaderClient.loadUser(parentId);
        UserIntegral integral = integralLoaderClient.getIntegralLoader().loadUserIntegral(user.toSimpleUser());
        if (null != integral) {
            model.addAttribute("integral", integral.getIntegral().getUsableIntegral());
        }

        // 我的孩子 by wyc 2015-12-29
        model.addAttribute("childCount", children.size());

        model.addAttribute("pid", getRequestContext().getUserId());

        return "/parent/ucenter/index";
    }

    // 我的孩子页面 by wyc 2015-12-29
    @RequestMapping(value = "/childreninfo.vpage", method = RequestMethod.GET)
    public String childrenInfo(Model model) {
        Long parentId = getRequestContext().getUserId();

        // 可爱的孩子们
        List<User> children = studentLoaderClient.loadParentStudents(parentId);

        if (CollectionUtils.isEmpty(children)) {
            model.addAttribute("childrenInfo", Collections.emptyList());
        } else {
            // 获取学豆的数量
            List<Long> childrenIDs = children.stream().map(child -> child.getId()).collect(Collectors.toList());
            Map<Long, Integral> childIntegralMap = integralLoaderClient.getIntegralLoader().loadIntegrals(childrenIDs);
            List<Map<String, Object>> childrenList = new ArrayList<>();
            children.stream().forEach(t -> {
                Map<String, Object> infoMap = new HashMap<>();
                infoMap.put("childId", t.getId());                                                       // 孩子Id
                infoMap.put("childName", t.getProfile().getRealname());                                  // 孩子姓名
                infoMap.put("childAvatar", t.getProfile().getImgUrl());                                  // 孩子头像
                infoMap.put("childIntegral", null == childIntegralMap.get(t.getId()) ?
                        0 : childIntegralMap.get(t.getId()).getUsableIntegral()); // 学豆余额
                childrenList.add(infoMap);
            });
            model.addAttribute("childrenInfo", childrenList);
        }
        return "/parent/ucenter/childreninfo";
    }

    //会员权限
    @RequestMapping(value = "/right.vpage", method = RequestMethod.GET)
    public String right() {
        return "/parent/ucenter/right";
    }

    //重置孩子密码
    @RequestMapping(value = "/resetstudentpwd.vpage", method = RequestMethod.GET)
    public String resetStudentPwd(Model model) {
        List<User> students = studentLoaderClient.loadParentStudents(getRequestContext().getUserId());
        if (students.size() == 0) {
            //跳去绑学生页面
            return "redirect:/parent/ucenter/bindchild.vpage";
        }

        //FIXME:重置孩子密码的页面，为啥要显示学豆数量？？？
        Map<Long, Integer> integralMap = new HashMap<>();
        for (User student : students) {
            StudentDetail detail = studentLoaderClient.loadStudentDetail(student.getId());
            integralMap.put(student.getId(), (int) detail.getUserIntegral().getUsable());
        }

        List<Map<String, Object>> stdInfos = new ArrayList<>();
        students.stream().forEach(t -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", t.getId());
            info.put("name", t.getProfile().getRealname());
            info.put("img", t.getProfile().getImgUrl());
            info.put("integral", integralMap.get(t.getId()));
            stdInfos.add(info);
        });
        model.addAttribute("students", stdInfos);

        return "/parent/ucenter/resetstudentpwd";
    }

    @RequestMapping(value = "/resetstudentpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetStudentPwd_Post() {
        Long studentId = getRequestLong("sid");
        String pwd = getRequestString("pwd");
        Long parentId = getRequestContext().getUserId();

        if (0 == studentId || StringUtils.isBlank(pwd)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            //验证家长与孩子关系
            List<User> children = studentLoaderClient.loadParentStudents(parentId);
            User childCandidate = null;
            for (User child : children) {
                if (Objects.equals(child.getId(), studentId)) {
                    childCandidate = child;
                    break;
                }
            }
            if (childCandidate == null) {
                return MapMessage.errorMessage("学生与家长无关联关系");
            }

            //验证家长是否已绑手机
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(parentId);
            if (userAuthentication == null || !userAuthentication.isMobileAuthenticated()) {
                return MapMessage.errorMessage("家长未绑定手机");
            }

            //修改密码
            MapMessage message = userServiceClient.setPassword(childCandidate, pwd);
            if (message.isSuccess()) {
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(studentId);
                userServiceRecord.setOperatorId(SafeConverter.toString(parentId));
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("修改密码");
                userServiceRecord.setComments("家长修改学生密码，操作端wechat");
                userServiceRecord.setAdditions("refer:ParentUcenterController.resetStudentPwd_Post");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("修改学生密码失败");
            }
        } catch (Exception ex) {
            log.error("reset student {}'s passwd to '{}' failed by parent {}", studentId, pwd, parentId, ex);
            return MapMessage.errorMessage("修改密码失败");
        }
    }

    //家长绑手机
    @RequestMapping(value = "/bindmobile.vpage", method = RequestMethod.GET)
    public String bindMobile(Model model) {
        String returnUrl = getRequestString("returnUrl");
        if (!StringUtils.isBlank(returnUrl)) {
            model.addAttribute("returnUrl", returnUrl);
        }

        model.addAttribute("cid", tokenHelper.generateContextId(getRequestContext()));
        return "/parent/ucenter/bindmobile";
    }

    @RequestMapping(value = "/bindmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindMobilePost() {
        String mobile = getRequestString("mobile");
        String code = getRequestString("code");

        if (!MobileRule.isMobile(mobile) || StringUtils.isBlank(code)) {
            return MapMessage.errorMessage("请输入正确的手机号与验证码");
        }

        try {
            MapMessage mapMessage = userService.verifySmsCode(mobile, code, WechatType.PARENT);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }

            MapMessage message = userServiceClient.activateUserMobile(getRequestContext().getUserId(), mobile);
            if (!message.isSuccess()) {
                return MapMessage.errorMessage("绑定家长手机失败");
            }

            //如果孩子还没有关键家长，则设置关键家长
            message = parentServiceClient.setKeyParent(getRequestContext().getUserId());
            if (!message.isSuccess()) {
                return MapMessage.errorMessage("设置关键家长失败"); //FIXME:如果绑定成功了，设置关键家长失败了，难道算绑定失败？？？
            }

            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("Bind mobile for parent failed,mobile:{},code:{}", mobile, code, ex);
            return MapMessage.errorMessage("绑定手机失败");
        }
    }

    //更换绑定的手机号 by wyc 2015-12-23
    @RequestMapping(value = "/changebindmobile.vpage", method = RequestMethod.GET)
    public String changeBindMobile(Model model) {
        // 如果家长未绑定手机或邮箱，跳转去绑定页面
        String returnUrl = getRequestString("returnUrl");
        if (!StringUtils.isBlank(returnUrl)) {
            model.addAttribute("returnUrl", returnUrl);
        }

        //检查当前家长是否已绑手机
        Long parentId = getRequestContext().getUserId();
        UserAuthentication parentAuthentication = userLoaderClient.loadUserAuthentication(parentId);

        if (parentAuthentication != null && parentAuthentication.isMobileAuthenticated()) {
            String mobileObscured = sensitiveUserDataServiceClient.loadUserMobileObscured(parentId);
            model.addAttribute("mobile", mobileObscured);
        } else {
            // 没有绑定手机的用户跳转去绑定手机页面
            model.addAttribute("source", "ucenter");
            return "redirect:/parent/ucenter/bindmobile.vpage";
        }

        model.addAttribute("cid", tokenHelper.generateContextId(getRequestContext()));
        return "/parent/signup/mobilelogin";
    }

    //绑手机发验证码
    @RequestMapping(value = "/sendchangecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendChangeCode() {
        String mobile = getRequestString("mobile");
        String contextId = getRequestString("cid");

        try {
            if (!MobileRule.isMobile(mobile) || StringUtils.isBlank(contextId)) {
                return MapMessage.errorMessage("参数错误");
            }

            if (!tokenHelper.verifyContextId(contextId)) {
                return MapMessage.errorMessage("您发送的太频繁了,请稍侯再试~");
            }

            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.PARENT_VERIFY_MOBILE_WEIXIN_REGISTER.name(),
                    false);
        } catch (Exception ex) {
            log.error("Send sms code for parent mobile bind failed,mobile:{},contextId:{}", mobile, contextId, ex);
            return MapMessage.errorMessage("发送失败");
        }
    }

    @RequestMapping(value = "/changebindmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBindMobilePost() {
        Long parentId = getRequestContext().getUserId();
        String mobileNumber = getRequestString("mobile");
        String verifyCode = getRequestString("code");

        if (!MobileRule.isMobile(mobileNumber) || StringUtils.isBlank(verifyCode)) {
            return MapMessage.errorMessage("请输入正确的手机号与验证码");
        }

        try {
            // 如果能够通过手机号码得到其他用户，就意味着手机号已经被认证了
            UserAuthentication authenticatedUser = userLoaderClient.loadMobileAuthentication(mobileNumber, UserType.PARENT);
            if (null != authenticatedUser && !parentId.equals(authenticatedUser.getId())) {
                return MapMessage.errorMessage("此手机号已被占用");
            }

            // 验证短信验证码
            MapMessage verifySmsMessage = userService.verifySmsCode(mobileNumber, verifyCode, WechatType.PARENT);
            if (!verifySmsMessage.isSuccess()) {
                return verifySmsMessage;
            }

            // 更新激活新的手机号码
            MapMessage activeMessage = userServiceClient.activateUserMobile(parentId, mobileNumber, true);
            if (!activeMessage.isSuccess()) {
                return MapMessage.errorMessage("家长更换绑定手机失败");
            }

            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("Change bound mobile for parent failed, mobile:{},code:{}", mobileNumber, verifyCode, ex);
            return MapMessage.errorMessage("更换绑定手机失败");
        }
    }

    //绑手机发验证码
    @RequestMapping(value = "/sendcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendCode() {
        String mobile = getRequestString("mobile");
        String contextId = getRequestString("cid");

        try {
            if (!MobileRule.isMobile(mobile) || StringUtils.isBlank(contextId)) {
                return MapMessage.errorMessage("参数错误");
            }

            if (!tokenHelper.verifyContextId(contextId)) {
                return MapMessage.errorMessage("您发送的太频繁了,请稍侯再试~");
            }

            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT) != null ||
                    userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                return MapMessage.errorMessage("手机号已被占用，不能绑定");
            }

            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.PARENT_VERIFY_MOBILE_WEIXIN_REGISTER.name(),
                    false);
        } catch (Exception ex) {
            log.error("Send sms code for parent mobile bind failed,mobile:{},contextId:{}", mobile, contextId, ex);
            return MapMessage.errorMessage("发送失败");
        }
    }

    //订单列表
    @RequestMapping(value = "/orderlist.vpage", method = RequestMethod.GET)
    public String orderList(Model model) {
        model.addAttribute("pid", getRequestContext().getUserId());
        return "/parent/ucenter/orderlist";
    }

    @RequestMapping(value = "/orders.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage orders() {
        Long parentId = getRequestContext().getUserId();
        String type = getRequestString("type");
        Integer pageIndex = getRequestInt("index");
        Integer pageSize = 2;

        try {
            List<User> children = studentLoaderClient.loadParentStudents(parentId);
            List<UserOrder> orders = new ArrayList<>();
            List<TrusteeOrderRecord> trusteeOrders = trusteeOrderServiceClient.loadTrusteeOrderByParentId(parentId);

            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient
                    .loadStudentDetails(children.stream().map(User::getId).collect(Collectors.toList()));
            if (MapUtils.isNotEmpty(studentDetailMap)) {
                for (StudentDetail student : studentDetailMap.values()) {
                    //有未支付的订单　价格与产品不相等直接跳过
                    Map<String, OrderProduct> productInfoMap = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(student)
                            .stream()
                            .collect(Collectors.toMap(ObjectIdEntity::getId, s -> s));
                    List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderList(student.getId())
                            .stream()
                            .filter(order -> OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == PicListen || !(order.getPaymentStatus() == PaymentStatus.Unpaid
                                    && order.getOrderStatus() == OrderStatus.New
                                    && productInfoMap.containsKey(order.getProductId())
                                    && Math.abs(productInfoMap.get(order.getProductId()).getPrice().doubleValue() - order.getOrderPrice().doubleValue()) > 1e-6))
                            .collect(Collectors.toList());
                    orders.addAll(userOrders);
                }
            }
            // 过滤出家长app支持的应用列表的订单
            Map<String, VendorApps> vam = vendorLoaderClient.loadVendorAppsIncludeDisabled().values().stream()
                    .filter(v -> v.isVisible(RuntimeMode.current().getLevel()))
                    .filter(VendorApps::getWechatBuyFlag)
                    .collect(Collectors.toMap(VendorApps::getAppKey, Function.identity()));
            orders = orders.stream().filter(o -> vam.containsKey(o.getOrderProductServiceType())).collect(Collectors.toList());
            if (type.equals("paid")) {
                orders = orders.stream()
                        .filter(source -> source.getPaymentStatus() == PaymentStatus.Paid ||
                                (source.getPaymentStatus() == PaymentStatus.Unpaid && source.getOrderStatus() == OrderStatus.Canceled))
                        .collect(Collectors.toList());
                trusteeOrders = trusteeOrders.stream().filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid).collect(Collectors.toList());
            } else {
                orders = orders.stream()
                        .filter(source -> source.getPaymentStatus() == PaymentStatus.Unpaid && source.getOrderStatus() == OrderStatus.New)
                        .collect(Collectors.toList());
                trusteeOrders = trusteeOrders.stream().filter(o -> o.getStatus() == TrusteeOrderRecord.Status.New).collect(Collectors.toList());
            }

            Map<Long, User> users = children.stream().collect(Collectors.toMap(User::getId, Function.identity(), (u, v) -> {
                throw new IllegalStateException("Duplicate key " + u);
            }, HashMap::new));

            // 将两个订单合起来
            List<Map<String, Object>> orderInfos = new ArrayList<>();
            for (UserOrder order : orders) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", order.genUserOrderId());
                info.put("name", users.get(order.getUserId()).getProfile().getRealname());
                info.put("price", order.getOrderPrice().toString());
//                info.put("period", order.getValidPeriod() + "天");
                info.put("productName", order.getProductName());
                info.put("createTime", order.getCreateDatetime().getTime());
                info.put("orderType", "afenti");
                orderInfos.add(info);
            }
            for (TrusteeOrderRecord record : trusteeOrders) {
                TrusteeType trusteeType = record.getTrusteeType();
                // 新做的家长端通用支付功能 在微信端暂时不支持查看
                if (trusteeType == null || trusteeType.getTrusteeShopId() == null) {
                    continue;
                }
                Map<String, Object> info = new HashMap<>();
                info.put("id", record.getId());
                info.put("name", users.get(record.getStudentId()) == null ? "预约" : users.get(record.getStudentId()).getProfile().getRealname());
                info.put("price", record.getPrice().toString());
                info.put("period", "-");
                TrusteeShop shop = TrusteeShop.getByShopId(record.getTrusteeType().getTrusteeShopId());
                String productName = "";
                if (StringUtils.equals(shop.getType(), "trustee")) {
                    productName = "托管班";
                }
                if (StringUtils.equals(shop.getType(), "trusteenew")) {
                    productName = "托管班";
                }
                if (StringUtils.equals(shop.getType(), "openclass")) {
                    productName = "一起公开课";
                }
                if (StringUtils.equals(shop.getType(), "wintercamp")) {
                    productName = "冬令营";
                }
                if (StringUtils.equals(shop.getType(), "summercamp")) {
                    productName = "夏令营";
                }
                if (StringUtils.equals(shop.getType(), "paycourse")) {
                    productName = "三小时搞定小升初数学测评";
                }
                info.put("productName", productName);
                info.put("createTime", record.getCreateTime().getTime());
                info.put("orderType", "trustee");
                orderInfos.add(info);
            }
            // 排序
            orderInfos = orderInfos.stream()
                    .filter(p -> DateUtils.dayDiff(new Date(), new Date(SafeConverter.toLong(p.get("createTime")))) < 30)
                    .sorted((o1, o2) -> {
                        long t1 = SafeConverter.toLong(o1.get("createTime"));
                        long t2 = SafeConverter.toLong(o2.get("createTime"));
                        return Long.compare(t2, t1);
                    }).collect(Collectors.toList());

            List<Map<String, Object>> results = new ArrayList<>();
            for (int i = (pageIndex - 1) * pageSize; i < orderInfos.size() && i < pageIndex * pageSize; i++) {
                results.add(orderInfos.get(i));
            }

            log();

            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.put("orders", results);
            mapMessage.put("count", orderInfos.size());
            return mapMessage;
        } catch (Exception ex) {
            log.error("Load orders failed,type:{},index:{},parentId:{}", type, pageIndex, parentId, ex);
            return MapMessage.errorMessage("查询订单失败");
        }
    }

    private void log() {
        String _from = getRequestString("_from");
        if (StringUtils.isNotBlank(_from) && "ucenter_click_load_more_orders".equals(_from)) {
            Map<String, String> log = new HashMap<>();
            log.put("module", "ucenter");
            log.put("op", "ucenter_click_load_more_orders");
            log.put("s0", getRequestContext().getAuthenticatedOpenId());
            super.log(log);
        }
    }

    //星星奖励页
    @RequestMapping(value = "/starreward.vpage", method = RequestMethod.GET)
    public String starReward(Model model) {
        /*该功能已下线*/
        return "redirect:/parent/homework/index.vpage";
        /*Long parentId = getRequestContext().getUserId();
        Long studentId = getRequestLong("sid"); //可选

        try {
            List<User> users = studentLoaderClient.loadParentStudents(parentId);

            List<Map<String, Object>> stdInfos = mapChildInfos(users);
            model.addAttribute("students", stdInfos);
            model.addAttribute("isAvailableMonth", LocalDate.now().getMonthValue() == 3 || LocalDate.now().getMonthValue() == 9);
            if (0 != studentId) {
                model.addAttribute("currentStd", studentId);
            }

        } catch (Exception ex) {
            logger.error("Get star reward failed by parent {}", parentId, ex);
        }

        return "/parent/ucenter/starreward";*/
    }

    @RequestMapping(value = "/msgcenter.vpage", method = RequestMethod.GET)
    public String msgCenter(Model model) {
        Long parentId = getRequestContext().getUserId();
        try {
            List<WechatNoticeSnapshot> noticeSnaps = getMessages(parentId);

            List<Map<String, String>> notices = new LinkedList<>();
            if (!CollectionUtils.isEmpty(noticeSnaps)) {
                for (WechatNoticeSnapshot notice : noticeSnaps) {
                    if (notice.getMessageType() == 31 || notice.getMessageType() == 33 || !notice.getOpenId().equals(getRequestContext().getAuthenticatedOpenId())) {
                        continue;
                    }

                    Map<String, String> info = buildMsg(notice.getMessageType(), notice.getMessage());
                    if (MapUtils.isEmpty(info)) {
                        continue;
                    }

                    info.put("createTime", LocalDateTime.ofInstant(Instant.ofEpochSecond(notice.getCreateTime().getTime() / 1000), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T", " "));
                    info.put("tip", "查看详情");

                    if (notice.getMessageType() == 44) {
                        notices.add(0, info);
                    } else {
                        notices.add(info);
                    }
                }
            }
            if (!noticeSnaps.isEmpty()) {
                model.addAttribute("notices", notices);
            }
        } catch (Exception ex) {
            logger.error("Load messages failed,parentId:{}", parentId);
        }
        return "/parent/ucenter/msgcenter";
    }

    //绑定孩子页
    @RequestMapping(value = "/bindchild.vpage", method = RequestMethod.GET)
    public String bindChild(Model model) {
        Long parentId = getRequestContext().getUserId();

        List<User> children = studentLoaderClient.loadParentStudents(parentId);
        if (!CollectionUtils.isEmpty(children) && children.size() >= 3) {
            return infoPage(WechatInfoCode.PARENT_BIND_CHILD_COUNT_LIMIT, model);
        }

        model.addAttribute("source", "ucenter");
        return "/parent/signup/login";
    }

    //验证输入的孩子帐号与密码
    @RequestMapping(value = "/bindchild.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindChild_Post() {
        Long parentId = getRequestContext().getUserId(); // 获取家长ID by wyc 2015-12-23
        String studentId = getRequestString("sid"); //这里传的是token,可能是ID或手机号
        String password = getRequestString("pwd");
        if (StringUtils.isBlank(studentId) || StringUtils.isBlank(password)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            List<User> users = userLoaderClient.loadUserByToken(studentId);
            if (CollectionUtils.isEmpty(users)) {
                return MapMessage.errorMessage("请输入孩子的帐号或绑定手机号");
            }
            Optional<User> student = users.stream().filter(u -> u.getUserType() == UserType.STUDENT.getType()).findFirst();

            if (!student.isPresent()) {
                return MapMessage.errorMessage("请输入学生帐号或手机号");
            }


            List<User> children = studentLoaderClient.loadParentStudents(parentId);
            if (!CollectionUtils.isEmpty(children)) {
                // 提示家长最多只能绑定3个孩子 By Wyc 2015-12-31
                if (children.size() >= 3) {
                    return MapMessage.errorMessage("家长号最多关联3个孩子,请联系客服操作");
                }

                // 提示家长无需重复绑定孩子 By Wyc 2015-12-30
                List<Long> childrenIds = children.stream().map(child -> child.getId()).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(childrenIds)) {
                    if (childrenIds.stream().anyMatch(id -> student.get().getId().equals(id))) {
                        return MapMessage.errorMessage("家长无需重复绑定您的孩子");
                    }
                }
            }

            // 临时密码校验 xuesong.zhang 2015-12-3
            if (StringUtils.isBlank(password) || !StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(student.get().getId()), password)) {
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(student.get().getId());
                if (ua == null || ua.fetchUserPassword().match(password)) {
                    return MapMessage.errorMessage("帐号或密码错误");
                }
            }

            //不支持绑定中学孩子,如果孩子还没有学校,也不让绑
            School school = asyncStudentServiceClient.getAsyncStudentService()
                    .loadStudentSchool(student.get().getId())
                    .getUninterruptibly();
            if (null == school || school.getLevel() != 1) {
                return MapMessage.errorMessage("暂时只支持小学学生");
            }

            saveCookie(student.get().getId(), null, null);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Child validate failed,studentId:{},parentId:{}", studentId, ex);
        }
        return MapMessage.errorMessage("帐号验证失败");
    }

    @RequestMapping(value = "/selectparent.vpage", method = RequestMethod.GET)
    public String selectParent(Model model) {
        try {
            Optional<Long> sid = getStudentIdFromCookie();
            if (!sid.isPresent()) {
                return infoPage(WechatInfoCode.PARENT_BIND_CACHE_EXPIRED, model);
            }

            User user = userLoaderClient.loadUser(sid.get());
            if (null != user) {
                model.addAttribute("name", user.getProfile().getRealname());
            }
            model.addAttribute("source", "ucenter");
            return "/parent/signup/selectparent";
        } catch (Exception ex) {
            logger.error("Select parent error", ex);
            return redirectWithMsg("系统异常", model);
        }
    }

    /**
     * 1. 家长最多只能绑定3个孩子
     * 2. 验证家长身份是否已经存在
     * 3. 选择的身份与性别不符
     * By Wyc 2016-01-04
     */

    @RequestMapping(value = "/selectparent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage selectParentPost() {
        Integer callNameCode = getRequestInt("callNameCode");
        if (0 == callNameCode) {
            return MapMessage.errorMessage("未知身份");
        }

        CallName callName = CallName.of(callNameCode);
        if (null == callName) {
            return MapMessage.errorMessage("无效身份");
        }

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return MapMessage.errorMessage("openId无效");
        }

        Long parentId = getRequestContext().getUserId();
        Long studentId = null;
        try {
            Optional<Long> studentOptional = getStudentIdFromCookie();
            if (!studentOptional.isPresent()) {
                return MapMessage.errorMessage("未查询到孩子帐号,请返回重试");
            }
            List<User> children = studentLoaderClient.loadParentStudents(parentId);
            // 提示家长最多只能绑定3个孩子 By Wyc 2019-01-04
            if (children.size() >= 3) {
                return MapMessage.errorMessage("家长号最多关联3个孩子,请联系客服操作");
            }

            studentId = studentOptional.get();
            // 判断学生是否已经存在这个称呼的家长 By Wyc 2016-01-04
            Optional<Long> parentOptional = userService.getParentByCallName(studentId, callName.getValue());
            if (parentOptional.isPresent()) {
                return MapMessage.errorMessage("孩子已经绑定了{}", callName.getValue());
            }

            // 验证家长身份的合理性 By Wyc 2016-01-04
            MapMessage checkGenderMsg = userService.checkCallNameGender(parentId, callName);
            if (!checkGenderMsg.isSuccess()) {
                return checkGenderMsg;
            }

            MapMessage msg = userService.bindStudentToParent(studentId, parentId, callName.getValue());

            log(studentId.toString());
            return msg;
        } catch (Exception ex) {
            logger.error("sid:{},callName:{}", studentId, callName, ex);
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "/selectparentwithstudentid.vpage", method = RequestMethod.GET)
    public String selectParentWithStudentId(Model model) {
        try {
            Long studentId = getRequestLong("studentId");
            User user = userLoaderClient.loadUser(studentId);
            if (null != user) {
                model.addAttribute("name", user.getProfile().getRealname());
            }
            model.addAttribute("studentId", studentId);
            model.addAttribute("source", "validate");
            return "/parent/signup/selectparent";
        } catch (Exception ex) {
            logger.error("Select parent error", ex);
            return redirectWithMsg("系统异常", model);
        }
    }

    @RequestMapping(value = "/selectparentwithstudentid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage selectParentWithStudentIdPost() {
        Integer callNameCode = getRequestInt("callNameCode");
        if (0 == callNameCode) {
            return MapMessage.errorMessage("未知身份");
        }

        CallName callName = CallName.of(callNameCode);
        if (null == callName) {
            return MapMessage.errorMessage("无效身份");
        }

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return MapMessage.errorMessage("openId无效");
        }

        Long parentId = getRequestContext().getUserId();
        Long studentId = getRequestLong("studentId");
        try {
            if (studentId == 0L) {
                return MapMessage.errorMessage("未查询到孩子帐号,请重新登录");
            }
            // 判断学生是否已经存在这个称呼的家长 By Wyc 2016-01-04
            Optional<Long> parentOptional = userService.getParentByCallName(studentId, callName.getValue());
            if (parentOptional.isPresent()) {
                return MapMessage.errorMessage("孩子已经绑定了{}", callName.getValue());
            }
            // 验证家长身份的合理性 By Wyc 2016-01-04
            MapMessage checkGenderMsg = userService.checkCallNameGender(parentId, callName);
            if (!checkGenderMsg.isSuccess()) {
                return checkGenderMsg;
            }
            MapMessage msg = parentServiceClient.setParentCallName(parentId, studentId, callName);
            log(studentId.toString());
            return msg;
        } catch (Exception ex) {
            logger.error("sid:{},callName:{}", studentId, callName, ex);
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "/verify.vpage", method = RequestMethod.GET)
    public String verify(Model model) {
        Integer callNameCode = getRequestInt("callNameCode");
        if (0 == callNameCode) {
            return redirectWithMsg("参数错误", model);
        }
        CallName callName = CallName.of(callNameCode);
        if (null == callName) {
            return redirectWithMsg("未知身份", model);
        }

        Optional<Long> sid = getStudentIdFromCookie();
        if (!sid.isPresent()) {
            return infoPage(WechatInfoCode.PARENT_BIND_CACHE_EXPIRED, model);
        }

        User user = userLoaderClient.loadUser(sid.get());
        if (null != user) {
            model.addAttribute("name", user.getProfile().getRealname());
        }

        try {
            Long parentId = getRequestContext().getUserId();
            String userPhone = sensitiveUserDataServiceClient.loadUserMobileObscured(parentId);
            if (StringUtils.isNoneBlank(userPhone)) {
                model.addAttribute("mobile", userPhone);
            }
            model.addAttribute("callName", callName.getValue());
            model.addAttribute("cid", tokenHelper.generateContextId(getRequestContext()));
            model.addAttribute("source", "ucenter");
            return "/parent/signup/verify";// fix 页面展示完全相同，共用signup。
        } catch (Exception ex) {
            logger.error("Verify for bind failed,callName:{}", callNameCode, ex);
        }

        return "/parent/signup/verify";
    }

    @RequestMapping(value = "/verify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyPost() {
        Integer callNameCode = getRequestInt("callNameCode");
        if (0 == callNameCode) {
            return MapMessage.errorMessage("参数错误");
        }
        CallName callName = CallName.of(callNameCode);
        if (null == callName) {
            return MapMessage.errorMessage("未知身份");
        }

        String code = getRequestString("code");
        String mobile = getRequestString("mobile");
        if (StringUtils.isBlank(code) || !MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("参数错误");
        }

        Optional<Long> sid = getStudentIdFromCookie();
        if (!sid.isPresent()) {
            return MapMessage.errorMessage("未查询到孩子帐号,请返回重试");
        }

        Long parentId = getRequestContext().getUserId();
        try {
            List<User> children = studentLoaderClient.loadParentStudents(parentId);
            if (!CollectionUtils.isEmpty(children)) {
                // 提示家长最多只能绑定3个孩子 By Wyc 2019-01-04
                if (children.size() >= 3) {
                    return MapMessage.errorMessage("家长号最多关联3个孩子,请联系客服操作");
                }

                // 判断学生是否已经存在这个称呼的家长 By Wyc 2016-01-04
                Optional<Long> parentOptional = userService.getParentByCallName(sid.get(), callName.getValue());
                if (parentOptional.isPresent()) {
                    return MapMessage.errorMessage("孩子已经绑定了{}", callName.getValue());
                }

                MapMessage checkGenderMsg = userService.checkCallNameGender(parentId, callName);
                if (!checkGenderMsg.isSuccess()) {
                    return checkGenderMsg;
                }
            }

            //检查当前家长是否已绑手机
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(parentId);
            if (null != userAuthentication) {
                if (!sensitiveUserDataServiceClient.mobileEquals(userAuthentication.getSensitiveMobile(), mobile)) {
                    return MapMessage.errorMessage("手机号不正确");
                }
            }

            MapMessage message = userService.verifySmsCode(mobile, code, WechatType.PARENT);
            if (!message.isSuccess()) {
                return message;
            }

            if (null == userAuthentication || !userAuthentication.isMobileAuthenticated()) {
                userServiceClient.activateUserMobile(getRequestContext().getUserId(), mobile);
            }

            MapMessage msg = userService.bindStudentToParent(sid.get(), getRequestContext().getUserId(), callName.getValue());

            log(sid.get().toString());

            return msg;
        } catch (Exception ex) {
            logger.error("Verify for bind failed,CallNameCode:{},mobile:{},code:{},sid:{}", callNameCode, mobile, code, sid);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private void log(String sid) {
        Map<String, String> log = new HashMap<>();
        log.put("module", "ucenter");
        log.put("op", "bindchild");
        log.put("parentId", getRequestContext().getUserId().toString());
        log.put("studentId", sid);
        super.log(log);
    }

    private Map<String, String> buildMsg(int type, String message) {
        Map<String, Object> msg = JsonUtils.fromJson(message);

        Map<String, String> map = new HashMap<>();
        switch (type) {
            case 21:
            case 22:
            case 24:
            case 26:
            case 28:
                if (message.contains("studentName")) {
                    return null;
                }
                return buildHomeworkCheckMsg(msg);
            case 20:
                map.put("title", "英语作业已布置");
                map.put("url", "/parent/homework/index.vpage");
                map.put("content", StringUtils.formatMessage("{}\n学科:{}\n内容:{}\n{}", msg.get("first"), msg.get("keyword2"), msg.get("keyword3"), msg.get("keyword1")));
                break;
            case 23:
                map.put("title", "语文作业已布置");
                map.put("url", "/parent/homework/index.vpage");
                map.put("content", StringUtils.formatMessage("{}", msg.get("first")));
                break;
            case 25:
                map.put("title", "数学作业已布置");
                map.put("url", "/parent/homework/index.vpage");
                map.put("content", StringUtils.formatMessage("{}\n学科:{}\n内容:{}\n{}", msg.get("first"), msg.get("keyword2"), msg.get("keyword3"), msg.get("keyword1")));
                break;
            case 27:
                map.put("title", "数学测验已布置");
                map.put("url", "/parent/homework/index.vpage");
                map.put("content", StringUtils.formatMessage("{}", msg.get("first")));
                break;
            case 29:
                map.put("title", "英语测验已布置");
                map.put("url", "/parent/homework/index.vpage");
                map.put("content", StringUtils.formatMessage("{}\n学科:{}\n内容:{}\n{}", msg.get("first"), msg.get("keyword2"), msg.get("keyword3"), msg.get("keyword1")));
                break;
            case 30:
                map.put("title", "智慧课堂奖励");
                map.put("url", "/parent/homework/index.vpage");
                map.put("content", msg.get("first").toString());
                break;
            case 32:
                map.put("title", StringUtils.formatMessage("{}家长您好", msg.get("studentName")));
                map.put("url", null == msg.get("picUrl") ? "/parent/homework/index.vpage" : msg.get("picUrl").toString());
                map.put("content", msg.get("msg").toString());
                break;
            case 34:
                map.put("title", "未支付订单");
                map.put("url", "/parent/wxpay/confirm.vpage?oid=" + msg.get("orderId"));
                map.put("content", msg.get("first").toString());
                break;
            default:
                return null;
        }
        return map;
    }

    private Map<String, String> buildHomeworkCheckMsg(Map<String, Object> msg) {
        String subject;
        switch (msg.get("subject").toString()) {
            case "ENGLISH":
                subject = "英语";
                break;
            case "MATH":
                subject = "数学";
                break;
            case "CHINESE":
                subject = "语文";
                break;
            default:
                throw new IllegalArgumentException("Unknow subject in the message:" + JsonUtils.toJson(msg));
        }

        boolean isQuiz = (null != msg.get("isQuiz") && Boolean.valueOf(msg.get("isQuiz").toString()));
        String type;
        if (isQuiz) {
            type = "测验";
        } else {
            type = "作业";
        }
        String title = subject + type + "已检查";

        String studentName = StringUtils.formatMessage("孩子(ID:{})", msg.get("studentId"));
        String teacherName = null == msg.get("teacherName") ? "" : msg.get("teacherName").toString();
        String startDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(((Date) msg.get("startDate")).getTime() / 1000), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(((Date) msg.get("endDate")).getTime() / 1000), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String content;
        if (null == msg.get("finished") || !Boolean.valueOf(msg.get("finished").toString())) {
            if (isQuiz) {
                content = StringUtils.formatMessage("家长您好,{}的{}测验未按时完成.{}", studentName, subject, teacherName);
            } else {
                content = StringUtils.formatMessage("家长您好,{}的{}作业未按时完成,请让孩子在电脑登录一起作业网,到[学习中心]查找[作业历史]补做作业~.{}", studentName, subject, teacherName);
            }
        } else {
            if (startDate.equals(endDate)) {
                content = StringUtils.formatMessage("家长您好,{}{}的{}{}已检查~{}", studentName, startDate, subject, type, teacherName);
            } else {
                content = StringUtils.formatMessage("家长您好,{}{}-{}的{}{}已检查~{}", studentName, startDate, endDate, subject, type, teacherName);
            }
        }

        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("url", "/parent/homework/index.vpage");
        map.put("content", content);
        return map;
    }

    private List<WechatNoticeSnapshot> getMessages(Long parentId) {
        List<WechatNoticeSnapshot> noticeSnaps = new ArrayList<>();
        List<WechatNoticeSnapshot> latestSnaps = wechatLoaderClient.loadWechatNoticeSnapshotByUserId(parentId, false);
        if (latestSnaps != null && !latestSnaps.isEmpty()) {
            noticeSnaps.addAll(latestSnaps);
        }
        List<WechatNoticeSnapshot> historySnaps = wechatLoaderClient.loadWechatNoticeSnapshotByUserId(parentId, true);
        if (historySnaps != null && !historySnaps.isEmpty()) {
            noticeSnaps.addAll(historySnaps);
        }
        return noticeSnaps;
    }

    /**
     * 帮助H5解决跨域访问
     * TODO  直接从家长端拷贝过来的
     */
    @RequestMapping(value = "getBody.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getBody() throws IOException {
        String url = getRequestString("url");
        String method = getRequestParameter("method", "get");
        String stringJson = getRequestString("data");
        try {
            if (url == null) {
                return MapMessage.errorMessage("参数非法");
            }
            URIBuilder builder = new URIBuilder(url);
            String host = builder.getHost();

            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = getRequest().getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = headerNames.nextElement();
                headers.put(key, getRequest().getHeader(key));
            }
            headers.put("Host", host);
            Map<Object, Object> map = new HashMap<>();
            if (stringJson != null) {
                map = JsonUtils.fromJsonToMap(stringJson, Object.class, Object.class);
            }


            AlpsHttpResponse response;
            switch (method) {
                case "get":
                    String URL = UrlUtils.buildUrlQuery(url, map);
                    response = HttpRequestExecutor.defaultInstance()
                            .get(URL)
                            .headers(headers)
                            .execute();
                    break;
                case "post":
                    POST post = HttpRequestExecutor.defaultInstance()
                            .post(url)
                            .addParameter(map)
                            .headers(headers);
                    response = post.execute();
                    break;
                default:
                    throw new IllegalArgumentException("Unrecoginized method: " + method);
            }

            if (response.getStatusCode() == 200) {
                return MapMessage.successMessage()
                        .add(
                                "body",
                                JsonUtils.fromJsonToMap(
                                        response.getResponseString(),
                                        Object.class,
                                        Object.class
                                )
                        );
            }

            return MapMessage.errorMessage(response.getStatusCode() + ":" + response.getResponseString()).setErrorCode(String.valueOf(response.getStatusCode()));

        } catch (Exception ex) {
            return MapMessage.errorMessage("调取url错误");
        }
    }
}
