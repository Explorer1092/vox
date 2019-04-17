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

package com.voxlearning.washington.controller.reward;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.api.constant.FlowChargeType;
import com.voxlearning.utopia.api.constant.TeacherTaskType;
import com.voxlearning.utopia.api.legacy.WellKnownCacheKeyGenerator;
import com.voxlearning.utopia.entity.ucenter.TeacherTaskRewardHistory;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.finance.api.FlowPacketConvertService;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.base.support.RewardWishOrderLoader;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardServiceClient;
import com.voxlearning.utopia.service.reward.constant.*;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.mapper.HistoryOrderMapper;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reward order controller implementation.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Jul 22, 2014
 */
@Controller
@RequestMapping("/reward/order")
public class RewardOrderController extends AbstractRewardController {

    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;

    @Inject
    private AmbassadorServiceClient ambassadorServiceClient;
    @Inject
    private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject
    private PrivilegeServiceClient privilegeServiceClient;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private NewRewardServiceClient newRewardServiceClient;
    @Inject
    private RewardCenterClient rewardCenterClient;
    @Inject
    private UserLoaderClient userLoaderClient;

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @ImportService(interfaceClass = VerificationService.class)
    private VerificationService verificationService;

    @ImportService(interfaceClass = FlowPacketConvertService.class)
    private FlowPacketConvertService flowPacketConvertService;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private CouponLoaderClient couponLoaderClient;
    @Inject
    private CouponServiceClient couponServiceClient;

    //判断有没有愿望盒
    @RequestMapping(value = "haswishorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage hasWishOrder() {
        RewardWishOrderLoader rewardWishOrderLoader = rewardLoaderClient.getRewardWishOrderLoader();
        List<RewardWishOrder> wishOrders = rewardWishOrderLoader.loadUserRewardWishOrders(currentUserId());
        RewardWishOrder wishOrder = MiscUtils.firstElement(wishOrders);
        if (wishOrder != null) {
            return MapMessage.errorMessage("已有愿望");
        } else {
            return MapMessage.successMessage();
        }
    }

    //判断学生家长有没有绑定微信
    @RequestMapping(value = "hasbindwechat.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage hasBindWeChat() {
        User user = currentRewardUser();
        if (user.fetchUserType() != UserType.STUDENT) {
            return MapMessage.errorMessage("错误的用户类型");
        }
        Map<Long, Set<Long>> bindWeChatMap = wechatServiceClient.studentBindWechatParentMap(Collections.singletonList(user.getId()));
        // 是否绑定了家长APP
        boolean isBindApp = studentMagicCastleServiceClient.hasBindParentApp(user.getId());
        return MapMessage.successMessage().add("bindFlag", CollectionUtils.isNotEmpty(bindWeChatMap.get(user.getId())) || isBindApp);
    }

    //添加愿望盒
    @RequestMapping(value = "addwishorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addWishOrder() {
        User user = currentRewardUser();
        Long productId = getRequestLong("productId");
        RewardProductDetail productDetail = newRewardLoaderClient.generateRewardProductDetail(user, productId);
        if (productDetail == null || !productDetail.getOnline()) {
            return MapMessage.errorMessage("对不起，您不能兑换当前礼物，请刷新页面重试");
        }
        return rewardServiceClient.createRewardWishOrder(user, productDetail);
    }

    /**
     * 订单创建(兑换成功)的后续操作
     *
     * @param productDetail
     */
    private MapMessage dealAfterOrderCreated(User user, RewardProductDetail productDetail, Long orderId, int quantity) {

        // 处理虚拟奖品
        if (newRewardLoaderClient.isSHIWU(productDetail.getOneLevelCategoryId())) {
            return MapMessage.successMessage();
        }

        // 如果是头饰的话，插入头饰记录
        if (newRewardLoaderClient.isHeadWear(productDetail.getOneLevelCategoryId())) {
            String headWearId = productDetail.getRelateVirtualItemId();
            if (StringUtils.isEmpty(headWearId))
                return MapMessage.errorMessage("头饰装扮失败!");

            Privilege p = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(headWearId);
            if (p == null)
                return MapMessage.errorMessage("头饰不存在!");

            Date now = new Date();
            Date expiryDate;
            Integer expiryLength = productDetail.getExpiryDate();
            if (expiryLength == null || expiryLength == 0)
                expiryDate = null;
            else {
                expiryDate = new Date(DateUtils.roundDateToDay235959InMillis(now, expiryLength * quantity));
            }

            // 购买后，再装扮上
            privilegeServiceClient.getPrivilegeService().grantPrivilege(user.getId(), p, expiryDate);
            personalZoneServiceClient.getPersonalZoneService().changeHeadWear(user.getId(), headWearId);
        }
        // 流量包 调用第三方接口发放
        else if (newRewardLoaderClient.isFlowPacket(productDetail.getOneLevelCategoryId())) {
            int flowSize = SafeConverter.toInt(productDetail.getRelateVirtualItemContent());
            if (flowSize > 0) {
                String orgMobile = null;

                String am = sensitiveUserDataServiceClient.showUserMobile(user.getId(),
                        "/reward/order/createOrder", SafeConverter.toString(user.getId()));
                if (am != null) {
                    orgMobile = am;
                }

                if (orgMobile != null) {
                    orgMobile = sensitiveUserDataServiceClient.encodeMobile(orgMobile);

                    flowPacketConvertService.saveFlowPacketConvert(
                            SafeConverter.toString(orderId),
                            FlowChargeType.REWARD_CENTER.getType(),
                            user.getId(),
                            orgMobile,
                            flowSize);
                }
            }
        }
        //托比装扮 插入记录
        else if (newRewardLoaderClient.isTobyWear(productDetail.getOneLevelCategoryId())) {
            rewardCenterClient.ctTobyDress(user.getId(), productDetail);
        }
        //如果是教学资源，站内提示
        else if (newRewardLoaderClient.isTeachingResources(productDetail.getOneLevelCategoryId())) {
            newRewardServiceClient.sendTeachingResourceMsg(user, productDetail.getId());
        }
        return MapMessage.successMessage();
    }

    /**
     * 通知家长
     *
     * @param studentId
     * @param content
     */
    private void notifyParent(Long studentId, String content, String linkUrl, boolean sendSmsFlag) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        List<Long> parentIds = parentLoaderClient.loadStudentParents(studentId)
                .stream()
                .map(p -> p.getParentUser().getId())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(parentIds)) {
            return;
        }
//        // 跳站外链接，默认传零
//        parentMessageServiceClient.postParentMessage(parentIds, studentId, content, "", 0, linkUrl, "",
//                ParentMessageTag.奖品中心, ParentMessageType.REMINDER);

        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("studentId", studentId);
        extInfo.put("tag", ParentMessageTag.奖品中心.name());
        extInfo.put("type", ParentMessageType.REMINDER.name());
        extInfo.put("senderName", "");
        List<AppMessage> messageList = new ArrayList<>();
        for (Long parentId : parentIds) {
            //新消息中心
            AppMessage message = new AppMessage();
            message.setUserId(parentId);
            message.setContent(content);
            message.setLinkType(0);
            message.setLinkUrl(linkUrl);
            message.setImageUrl("");
            message.setExtInfo(extInfo);
            message.setMessageType(ParentMessageType.REMINDER.getType());
            messageList.add(message);
        }
        messageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        //发送jpush
        Map<String, Object> extras = new HashMap<>();
        extras.put("studentId", studentId);
        extras.put("url", linkUrl);
        extras.put("tag", ParentMessageTag.奖品中心.name());
        //新的push参数
        extras.put("s", ParentAppPushType.REWARD_CENTER.name());
        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, parentIds, extras);


        if (sendSmsFlag) {
            StudentParent studentParent = parentLoaderClient.loadStudentKeyParent(studentId);
            if (studentParent != null) {
                String am = sensitiveUserDataServiceClient.showUserMobile(studentParent.getParentUser().getId(), "/reward/product/experience/detail", SafeConverter.toString(studentParent.getParentUser().getId()));
                if (am != null) {
                    // 发短信
                    smsServiceClient.createSmsMessage(am)
                            .content(content)
                            .type(SmsType.LIVECAST_REWARD_PROMOT.name())
                            .send();
                }
            }
        }
    }

    //兑换奖品
    @RequestMapping(value = "createorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createOrder() {
        User user = currentRewardUser();
        Long productId = getRequestLong("productId");
        Long skuId = getRequestLong("skuId");
        int quantity = getRequestInt("quantity");
        String couponUserRefId = getRequestString("couponUserRefId");
        if (quantity <= 0) {
            return MapMessage.errorMessage("请输入正确的奖品数量");
        }

        // 记录订单来源
        String sourceStr = getRequestString("source");
        RewardOrder.Source source = RewardOrder.Source.parse(sourceStr);

        RewardProductDetail productDetail = rewardLoaderClient.generateUserRewardProductDetail(user, productId);
        if (productDetail == null || !productDetail.getOnline()) {
            return MapMessage.errorMessage("对不起！因厂商原因，你兑换的奖品已经下架，请去挑选其他奖品哦！");
        }
        RewardSku sku = null;
        if (newRewardLoaderClient.isSHIWU(productDetail.getOneLevelCategoryId())) {
            sku = crmRewardService.$loadRewardSku(skuId);
            if (sku == null) {
                return MapMessage.errorMessage("单品不存在");
            }

            if (sku.getInventorySellable() < quantity) {
                return MapMessage.errorMessage("奖品数量不足！");
            }
        }

        // 校验是否满足兑换资格
        MapMessage mapMessage = validateUser(user, productDetail);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }

        // 如果是头饰或者是托比装扮并且数量是2的话 打9折
        if (quantity == 2 && (newRewardLoaderClient.isTobyWear(productDetail.getOneLevelCategoryId()) || newRewardLoaderClient.isHeadWear(productDetail.getOneLevelCategoryId()))) {
            BigDecimal discountPrice = new BigDecimal(productDetail.getDiscountPrice()).multiply(new BigDecimal(0.9));
            productDetail.setDiscountPrice(discountPrice.doubleValue());
        }

        TeacherCouponEntity coupon = null;
        if (user.isTeacher() && StringUtils.isNotBlank(couponUserRefId)) {
            List<CouponShowMapper> couponShowMapperList = couponLoaderClient.loadUserRewardCoupons(user.getId());
            if (CollectionUtils.isEmpty(couponShowMapperList)) {//无兑换券
                return MapMessage.errorMessage("该优惠券已失效，请检查并重新兑换!");
            }
            for (CouponShowMapper mapper : couponShowMapperList) {
                if (Objects.equals(mapper.getCouponUserStatus().getDesc(), CouponUserStatus.NotUsed.getDesc()) && Objects.equals(mapper.getCouponUserRefId(), couponUserRefId)) {
                    coupon = new TeacherCouponEntity(mapper.getTypeValue().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(), mapper.getCouponUserRefId());
                    break;
                }
            }
            //兑换券已失效
            if (coupon == null) {
                return MapMessage.errorMessage("该优惠券已失效，请检查并重新兑换!");
            }
        }

        MapMessage message = newRewardServiceClient.createRewardOrder(user, productDetail, sku, quantity, null, source, coupon);
        if (message.isSuccess()) {
            //记录老师点亮标签
            if (user.fetchUserType() == UserType.TEACHER) {
                ambassadorServiceClient.getAmbassadorService().recordAmbassadorMentor(user.getId(),
                        MiscUtils.map(UserTagType.AMBASSADOR_MENTOR_REWARD_ORDER, UserTagEventType.AMBASSADOR_MENTOR_REWARD_ORDER));
            }

            //使用掉优惠券
            if (user.isTeacher() && coupon != null) {
                couponServiceClient.updateCouponUserRefStatus(couponLoaderClient.loadCouponUserRefById(coupon.getCouponUserRefId()), CouponUserStatus.Used);
            }

            Long orderId = SafeConverter.toLong(message.get("orderId"));
            // 后续处理
            message = dealAfterOrderCreated(user, productDetail, orderId, quantity);
        }

        return message;
    }

    // 兑换捐赠奖品
    @RequestMapping(value = "createpresentorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createPresentOrder() {
        User user = currentRewardUser();
        Long productId = getRequestLong("productId");
        Long skuId = getRequestLong("skuId");
        int quantity = getRequestInt("quantity");
        if (quantity <= 0) {
            return MapMessage.errorMessage("请输入正确的奖品数量");
        }
        RewardProductDetail productDetail = newRewardLoaderClient.generateRewardProductDetail(user, productId);
        if (productDetail == null || !productDetail.getOnline()) {
            return MapMessage.errorMessage("对不起！因厂商原因，你兑换的奖品已经下架，请去挑选其他奖品哦！");
        }
        RewardSku sku = crmRewardService.$loadRewardSku(skuId);
        if (sku == null) {
            return MapMessage.errorMessage("单品不存在");
        }
        if (sku.getInventorySellable() < quantity) {
            return MapMessage.errorMessage("奖品数量不足！");
        }
        // 捐赠的 再此不做校验了
        return rewardServiceClient.createPresentRewardOrder(user, productDetail, sku, quantity, null, RewardOrder.Source.pc);
    }

    //愿望盒 放入 兑换盒
    @RequestMapping(value = "achievewishorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage achieveWishOrder() {
        User user = currentRewardUser();

        Long wishOrderId = getRequestLong("wishOrderId");
        RewardWishOrderLoader rewardWishOrderLoader = rewardLoaderClient.getRewardWishOrderLoader();
        RewardWishOrder wishOrder = rewardWishOrderLoader.loadRewardWishOrder(wishOrderId);
        if (wishOrder == null || !user.getId().equals(wishOrder.getUserId())) {
            return MapMessage.errorMessage("参数错误");
        }
        Long productId = getRequestLong("productId");
        Long skuId = getRequestLong("skuId");
        int quantity = getRequestInt("quantity", 1);

        if (quantity <= 0) {
            return MapMessage.errorMessage("请输入正确的奖品数量");
        }

        RewardProductDetail productDetail = rewardLoaderClient.generateUserRewardProductDetail(user, productId);
        if (productDetail == null || !productDetail.getOnline()) {
            return MapMessage.errorMessage("对不起！因厂商原因，你兑换的奖品已经下架，请去挑选其他奖品哦！");
        }

        RewardSku sku = crmRewardService.$loadRewardSku(skuId);
        if (sku == null) {
            return MapMessage.errorMessage("单品不存在");
        }
        if (sku.getInventorySellable() < quantity) {
            return MapMessage.errorMessage("奖品数量不足！");
        }
        // 校验是否满足兑换资格
        MapMessage mapMessage = validateUser(user, productDetail);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        MapMessage message = newRewardServiceClient.createRewardOrder(user, productDetail, sku, quantity, wishOrder, RewardOrder.Source.pc, null);
        if (message.isSuccess()) {
            //记录老师点亮标签
            if (user.fetchUserType() == UserType.TEACHER) {
                ambassadorServiceClient.getAmbassadorService().recordAmbassadorMentor(user.getId(),
                        MiscUtils.map(UserTagType.AMBASSADOR_MENTOR_REWARD_ORDER, UserTagEventType.AMBASSADOR_MENTOR_REWARD_ORDER));
            }

            Long orderId = SafeConverter.toLong(message.get("orderId"));
            // 后续处理
            message = dealAfterOrderCreated(user, productDetail, orderId, quantity);
        }
        return message;
    }

    // 校验兑换方法
    private MapMessage validateUser(User user, RewardProductDetail productDetail) {
        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(user.getId());
        UserShippingAddress shippingAddress;
        switch (user.fetchUserType()) {
            case STUDENT:
                // 如果是虚拟奖品的话，不走下面这些个校验步骤
                if (!newRewardLoaderClient.isSHIWU(productDetail.getOneLevelCategoryId())) {
                    break;
                }

                // 下线城市的灰度地区
                if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable((StudentDetail) user, "Reward", "OfflineShiWu", true))
                    return MapMessage.errorMessage("你所在区域这个奖品已经无法兑换啦⊙︿⊙");

                TeacherDetail detail = userAggregationLoaderClient.loadStudentTeacherForRewardSending(user.getId());
                if (detail == null) {
                    return MapMessage.errorMessage("你的班级还没有收货老师，无法寄送哦！");
                }

                //此处新增逻辑， 有这个灰度的老师的学生兑换， 不判断学生绑定手机的情况  2015-05-29
                if (!grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(detail, "Reward", "OrderCheck")) {
                    if (!authentication.isMobileAuthenticated() && parentLoaderClient.loadStudentKeyParent(user.getId()) == null) {
                        return MapMessage.errorMessage("你还没有绑定手机，无法寄送哦！").add("bindMobile", false);
                    }
                }
                // 毕业班不允许兑换
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
                if (rewardLoaderClient.isGraduate(studentDetail)) {
                    return MapMessage.errorMessage("毕业班学生暂时不能兑换奖品哦！");
                }
                break;
            case TEACHER:
                if (user.getAuthenticationState() != AuthenticationState.SUCCESS.getState()) {
                    return MapMessage.errorMessage("你还没有认证，不能兑换哦！").add("authentication", false);
                }

                // 假老师不能兑换
                if (teacherLoaderClient.isFakeTeacher(user.getId())) {
                    return MapMessage.errorMessage("对不起，您不能兑换！");
                }

                //判断用户绑手机
                if (!authentication.isMobileAuthenticated()) {
                    return MapMessage.errorMessage("你还没有绑定手机，无法寄送哦！").add("bindMobile", false);
                }

                // 只有实物才校验地址
                if (newRewardLoaderClient.isSHIWU(productDetail.getOneLevelCategoryId())) {
                    // 收获地址为空
                    shippingAddress = userLoaderClient.loadUserShippingAddress(user.getId());
                    if (shippingAddress == null || StringUtils.isBlank(shippingAddress.getDetailAddress())) {
                        return MapMessage.errorMessage("你还没有填写收货地址，无法寄送哦！").add("address", false);
                    }
                }
                if (!newRewardLoaderClient.checkCanExchangePrivilegeProduct(productDetail.getId(), (TeacherDetail) user)) {
                    return MapMessage.errorMessage("当前级别不可兑换");
                }
                TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(user.getId());
                int teacherLevel = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevel());
                //等级判断
                if (productDetail.getTeacherLevel() > 0 && teacherLevel < productDetail.getTeacherLevel()) {
                    return MapMessage.errorMessage("对不起，你的等级不能兑换该奖品");
                }

                // 流量包校验手机号运营商是否匹配
                if (newRewardLoaderClient.isFlowPacket(productDetail.getOneLevelCategoryId())) {
                    String am = sensitiveUserDataServiceClient.showUserMobile(user.getId(),
                            "/reward/product/experience/detail", SafeConverter.toString(user.getId()));
                    if (am != null) {
                        String mobile = am;

                        ISPNumberRule numberRule = ISPNumberRule.parse(productDetail.getRelateVirtualItemId());
                        if (!numberRule.isLegalNumber(mobile)) {
                            return MapMessage.errorMessage("兑换失败，您的手机号格式不正确，请重新输入");
                        }
                    }
                }

                break;
            case RESEARCH_STAFF:
                shippingAddress = userLoaderClient.loadUserShippingAddress(user.getId());
                if (shippingAddress == null || StringUtils.isBlank(shippingAddress.getDetailAddress())) {
                    return MapMessage.errorMessage("你还没有填写收货地址，无法寄送哦！").add("address", false);
                }
                if (StringUtils.isBlank(shippingAddress.getSensitivePhone())) {
                    return MapMessage.errorMessage("你还没有填写手机，无法寄送哦！").add("bindMobile", false);
                }
                break;
            default:
                return MapMessage.errorMessage("角色错误");
        }
        return MapMessage.successMessage();
    }

    //我的礼物
    @RequestMapping(value = "myorder.vpage", method = RequestMethod.GET)
    public String myOrder(Model model) {
        User user = currentRewardUser();

        // 黑名单控制
        if (user.isTeacher()) {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "Reward", "Close")) {
                return "redirect:/";
            }
        }

        List<RewardOrder> allOrders = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(user.getId());
        Collection<Long> productIds = new LinkedHashSet<>();
        for (RewardOrder order : allOrders) {
            CollectionUtils.addNonNullElement(productIds, order.getProductId());
        }
        Map<Long, List<RewardImage>> rewardImages = rewardLoaderClient.loadProductRewardImages(productIds);

        List<RewardOrder> orderList = allOrders.stream()
                .filter(o -> !Objects.equals(RewardOrderStatus.DELIVER.name(), o.getStatus()))
                .filter(o -> Objects.equals(OneLevelCategoryType.JPZX_SHIWU.intType(), NumberUtils.toInt(o.getProductType())) || Objects.equals(RewardProductType.JPZX_SHIWU.name(), o.getProductType()))
                .collect(Collectors.toList());
        List<Map<String, Object>> orderMapLists = new LinkedList<>();
        BigDecimal currentTotalPrice = new BigDecimal(0);
        for (RewardOrder order : orderList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("orderId", order.getId());
            List<RewardImage> images = rewardImages.get(order.getProductId());
            if (CollectionUtils.isNotEmpty(images)) {
                map.put("image", images.get(0).getLocation());
            }
            map.put("productName", order.getProductName());
            map.put("skuName", order.getSkuName());
            map.put("price", order.getPrice());
            map.put("quantity", order.getQuantity());
            map.put("unit", order.getUnit());
            map.put("statusCode", RewardOrderStatus.valueOf(order.getStatus()));
            map.put("status", getOrderStatusDesc(RewardOrderStatus.valueOf(order.getStatus())));
            map.put("createTime", DateUtils.dateToString(order.getCreateDatetime(), "yyyy年MM月dd日"));
            map.put("discount", order.getDiscount());
            map.put("totalPrice", order.getTotalPrice());
            if (order.getSource() != null) {
                map.put("source", order.getSource().name());
            }

            // 实物、兑换成功状态，并且是非抽奖来源的才可以取消兑换
            if ((Objects.equals(order.getProductType(), RewardProductType.JPZX_SHIWU.name()) || newRewardLoaderClient.isSHIWU(NumberUtils.toLong(order.getProductCategory())))
                    && Objects.equals(order.getStatus(), RewardOrderStatus.SUBMIT.name())
                    && !Objects.equals(order.getSource(), RewardOrder.Source.gift)
                    && !Objects.equals(order.getSource(), RewardOrder.Source.power_pillar)
                    && !Objects.equals(order.getSource(), RewardOrder.Source.moonlightbox)
                    && !Objects.equals(order.getSource(), RewardOrder.Source.claw)) {
                map.put("returnable", true);
            } else {
                map.put("returnable", false);
            }

            orderMapLists.add(map);
            currentTotalPrice = currentTotalPrice.add(new BigDecimal(order.getTotalPrice() == null ? 0D : order.getTotalPrice()));
        }
        model.addAttribute("orderMapList", orderMapLists);
        model.addAttribute("totalPrice", currentTotalPrice.setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        return "reward/order/myordernew";
    }

    private String getOrderStatusDesc(RewardOrderStatus status) {
        switch (status) {
            case SUBMIT:
                return "兑换成功";
            case IN_AUDIT:
                return "审核中";
            case PREPARE:
                return "配货中";
            case SORTING:
                return "分拣中";
            case DELIVER:
                return "已发货";
            case EXCEPTION:
                return "用户信息异常";
            default:
                return "";
        }
    }

    @RequestMapping(value = "/mywish.vpage")
    public String mywish() {
        return "reward/order/mywish";
    }


    //我的愿望
    @RequestMapping(value = "mywishpc.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage myWish(Model model) {
        User user = currentUser();
        if (user == null)
            return MapMessage.errorMessage("未登录!");

        int pageNumber = getRequestInt("page", 0);
        int pageSize = getRequestInt("pageSize", 5);

        PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
        List<Map<String, Object>> wishOrders = rewardLoaderClient.getWishDetails(user);
        Page<Map<String, Object>> page = PageableUtils.listToPage(wishOrders, pageRequest);

        // 电话信息列表
        List<Map<String, Object>> mobileList = newRewardLoaderClient.loadMobileInfo(user);

        return MapMessage.successMessage()
                .add("totalPage", page.getTotalPages())
                .add("currPage", pageNumber)
                .add("content", page.getContent())
                .add("mobileList", mobileList);
    }

    //我的历史兑换
    @RequestMapping(value = "history.vpage", method = RequestMethod.GET)
    public String history(Model model) {
        User user = currentUser();
        List<RewardOrder> orderList = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(user.getId())
                .stream()
                .filter(source -> RewardOrderStatus.DELIVER.name().equals(source.getStatus()))
                .filter(o -> Objects.equals(OneLevelCategoryType.JPZX_SHIWU.intType(), NumberUtils.toInt(o.getProductType())) || Objects.equals(RewardProductType.JPZX_SHIWU.name(), o.getProductType()))
                .collect(Collectors.toList());
        Collection<Long> productIds = new LinkedHashSet<>();
        for (RewardOrder order : orderList) {
            CollectionUtils.addNonNullElement(productIds, order.getProductId());
        }
        Map<Long, List<RewardImage>> rewardImages = rewardLoaderClient.loadProductRewardImages(productIds);
        Set<Long> productIdSet = rewardLoaderClient.generateUserRewardProductDetails(user, productIds).stream().filter(e -> e.getCouponResource() != null && e.getCouponResource() != RewardCouponResource.ZUOYE).map(e -> e.getId()).collect(Collectors.toSet());
        List<Map<String, Object>> orderMapLists = new LinkedList<>();
        BigDecimal totalPrice = new BigDecimal(0);
        for (RewardOrder order : orderList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("orderId", order.getId());
            List<RewardImage> images = rewardImages.get(order.getProductId());
            if (CollectionUtils.isNotEmpty(images)) {
                map.put("image", images.get(0).getLocation());
            }
            map.put("productName", order.getProductName());
            map.put("skuName", order.getSkuName());
            map.put("price", order.getPrice());
            map.put("quantity", order.getQuantity());
            map.put("unit", order.getUnit());
            map.put("statusCode", RewardOrderStatus.valueOf(order.getStatus()));
            map.put("status", getOrderStatusDesc(RewardOrderStatus.valueOf(order.getStatus())));
            map.put("createTime", DateUtils.dateToString(order.getCreateDatetime(), "yyyy年MM月dd日"));
            map.put("logisticId", order.getLogisticsId());
            map.put("rewardInfo", CollectionUtils.isNotEmpty(productIdSet) && productIdSet.contains(order.getProductId()) ? "兑换详情请登录手机app查看" : "");
            orderMapLists.add(map);
            totalPrice = totalPrice.add(new BigDecimal(order.getTotalPrice() == null ? 0D : order.getTotalPrice()));
        }
        // 分组返回数据 根据订单
        List<HistoryOrderMapper> dataList = new ArrayList<>();
        Map<Long, List<Map<String, Object>>> orderMap = orderMapLists.stream().collect(Collectors.groupingBy(o -> SafeConverter.toLong(o.get("logisticId"))));
        for (Map.Entry<Long, List<Map<String, Object>>> entry : orderMap.entrySet()) {
            HistoryOrderMapper mapper = new HistoryOrderMapper();
            Long logisticId = entry.getKey();
            // 根据角色区分
            if (logisticId == 0) {
                // 历史订单
                mapper.setHistoryOrder(true);
                mapper.setOrders(entry.getValue());
            } else {
                RewardLogistics logistics = crmRewardService.$loadRewardLogistics(logisticId);
                if (logistics == null)
                    continue;

                Date deliveredTime = logistics.getDeliveredTime();
                // 老数据这个字段为空，兼容一下
                if (deliveredTime == null)
                    deliveredTime = logistics.getUpdateDatetime();

                if (user.fetchUserType() == UserType.STUDENT) {
                    // 学生的 需要获取相关订单信息
                    Long teacherId = logistics.getReceiverId();
                    TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
                    if (teacherDetail != null) {
                        mapper.setSubject(teacherDetail.getSubject().getValue());
                        mapper.setTeacherName(teacherDetail.fetchRealname());
                        mapper.setDeliverDate(DateUtils.dateToString(deliveredTime, "yyyy年MM月dd日"));
                    }
                } else {
                    // 老师的 需要获取相关物流信息
                    mapper.setLogisticNo(logistics.getLogisticNo());
                    mapper.setCompanyName(logistics.getCompanyName());
                    mapper.setDeliverDate(DateUtils.dateToString(deliveredTime, "yyyy年MM月dd日"));
                }

                mapper.setHistoryOrder(false);
                mapper.setOrders(entry.getValue());
            }
            dataList.add(mapper);
        }
        // 排序 历史订单排后边
        dataList.sort((o1, o2) -> {
            Boolean b1 = SafeConverter.toBoolean(o1.isHistoryOrder());
            Boolean b2 = SafeConverter.toBoolean(o2.isHistoryOrder());
            return b1.compareTo(b2);
        });
        // 查看老师是否是本月收货人
//        if (user.fetchUserType() != null && user.fetchUserType() == UserType.TEACHER) {
//            Map<String, Object> studentLogisticInfo = new HashMap<>();
//            String month = DateUtils.dateToString(new Date(), "yyyyMM");
//            RewardLogistics logistics = crmRewardService.$findRewardLogistics(user.getId(), RewardLogistics.Type.STUDENT, month);
//            if (logistics != null) {
//
//                Date deliveredTime = logistics.getDeliveredTime();
//                // 老数据这个字段为空，兼容一下
//                if (deliveredTime == null)
//                    deliveredTime = logistics.getUpdateDatetime();
//
//                studentLogisticInfo.put("deliverDate", DateUtils.dateToString(deliveredTime, "yyyy年MM月dd日"));
//                studentLogisticInfo.put("companyName", logistics.getCompanyName());
//                studentLogisticInfo.put("logisticNo", logistics.getLogisticNo());
//            }
//            model.addAttribute("studentLogisticInfo", studentLogisticInfo);
//        }
        model.addAttribute("orderDataList", dataList);
        model.addAttribute("totalPrice", totalPrice.setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        return "reward/order/historynew";
    }

    //我的代收
    @RequestMapping(value = "substreceive.vpage", method = RequestMethod.GET)
    public String substituteReceive(Model model) {
        User user = currentUser();
        // 分组返回数据 根据订单
        List<Map<String, Object>> dataList = new ArrayList<>();
        // 查看老师是否是本月收货人
        if (user.isTeacher()) {
            List<RewardLogistics> logisticsList = crmRewardService.$findRewardLogisticsList(user.getId(), RewardLogistics.Type.STUDENT).stream().sorted((e1, e2) -> {
                return e1.getCreateDatetime() != null && e1.getCreateDatetime().before(e2.getCreateDatetime()) ? 1 : -1;
            }).collect(Collectors.toList());
            String rewardInfo = "发货后您将获得" + (currentTeacherDetail() != null && currentTeacherDetail().isPrimarySchool() ? "50园丁豆" : "500学豆") + "奖励";
            for (RewardLogistics logistics : logisticsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("image", "http://17zy-content-video.oss-cn-beijing.aliyuncs.com/Prize/chanpin/%E4%BB%A3%E6%94%B6.png");
                map.put("productName", "【学生包裹】" + logistics.getSchoolName());
                Date deliveredTime = logistics.getDeliveredTime();
                RewardOrderStatus status = logistics.getIsBack() ? RewardOrderStatus.DELIVER : RewardOrderStatus.PREPARE;
                map.put("deliverDate", deliveredTime != null ? DateUtils.dateToString(deliveredTime, "yyyy年MM月dd日") : "");
                map.put("companyName", logistics.getCompanyName());
                map.put("logisticNo", logistics.getLogisticNo());
                map.put("status", status.name());
                map.put("createTime", DateUtils.dateToString(logistics.getCreateDatetime(), "yyyy年MM月dd日"));
                map.put("statusName", status.getDesc());
                map.put("rewardInfo", status == RewardOrderStatus.PREPARE ? rewardInfo : "");
                dataList.add(map);
            }
        }
        model.addAttribute("orderDataList", dataList);
        return "reward/order/substreceive";
    }

    //我的体验
    @RequestMapping(value = "myexperience.vpage", method = RequestMethod.GET)
    public String myExperience(Model model) {

        User user = currentRewardUser();

        Map<Long, RewardCouponDetail> myCoupon = rewardLoaderClient.getRewardCouponDetailLoader()
                .loadUserRewardCouponDetails(user.getId())
                .stream()
                .collect(Collectors.toMap(RewardCouponDetail::getProductId, Function.identity(), (o1, o2) -> o2));

        Map<Long, RewardCouponDetail> myOrderCoupon = rewardLoaderClient.getRewardCouponDetailLoader()
                .loadUserRewardCouponDetails(user.getId())
                .stream()
                .filter(coupon -> Objects.nonNull(coupon.getOrderId()) && !Objects.equals(coupon.getOrderId(), 0L))
                .collect(Collectors.toMap(RewardCouponDetail::getOrderId, Function.identity(), (o1, o2) -> o2));

        List<RewardOrder> allOrders = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(user.getId());
        Collection<Long> productIds = new LinkedHashSet<>();
        for (RewardOrder order : allOrders) {
            CollectionUtils.addNonNullElement(productIds, order.getProductId());
        }
        for (Long productId : myCoupon.keySet()) {
            CollectionUtils.addNonNullElement(productIds, productId);
        }
        Map<Long, List<RewardImage>> rewardImages = rewardLoaderClient.loadProductRewardImages(productIds);

        List<RewardOrder> orderList = allOrders.stream()
                .filter(o -> !Objects.equals(OneLevelCategoryType.JPZX_SHIWU.intType(), NumberUtils.toInt(o.getProductType())) && !Objects.equals(RewardProductType.JPZX_SHIWU.name(), o.getProductType()))
                .collect(Collectors.toList());
        List<Map<String, Object>> orderMapLists = new LinkedList<>();
        BigDecimal currentTotalPrice = new BigDecimal(0);
        for (RewardOrder order : orderList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("orderId", order.getId());
            List<RewardImage> images = rewardImages.get(order.getProductId());
            if (CollectionUtils.isNotEmpty(images)) {
                map.put("image", images.get(0).getLocation());
            }
            map.put("productName", order.getProductName());
            map.put("skuName", order.getSkuName());
            map.put("price", order.getPrice());
            map.put("quantity", order.getQuantity());
            map.put("unit", order.getUnit());
            map.put("statusCode", RewardOrderStatus.valueOf(order.getStatus()));
            map.put("status", getOrderStatusDesc(RewardOrderStatus.valueOf(order.getStatus())));
            map.put("createTime", DateUtils.dateToString(order.getCreateDatetime(), "yyyy年MM月dd日"));
            map.put("discount", order.getDiscount());
            map.put("totalPrice", order.getTotalPrice());
            map.put("oneLevelCategoryType", newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(order));
            if (order.getSource() != null) {
                map.put("source", order.getSource().name());
            }
            map.put("returnable", false);
            if (myOrderCoupon.containsKey(order.getId())) {
                RewardCouponDetail couponDetail = myOrderCoupon.get(order.getId());
                RewardProductDetail product = newRewardLoaderClient.generateRewardProductDetail(user, order.getProductId());
                if (Objects.nonNull(product)) {
                    map.put("detailId", couponDetail.getId());
                    map.put("couponNo", couponDetail.getCouponNo());
                    map.put("used", couponDetail.getUsed());
                    map.put("rebated", couponDetail.getRebated());

                    map.put("productId", product.getId());
                    map.put("productName", product.getProductName());
                    map.put("productRebated", product.getRebated());
                    map.put("url", product.getUsedUrl());
                    map.put("couponResource", product.getCouponResource());
                }
            } else if (Objects.equals(newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(order), OneLevelCategoryType.JPZX_COUPON.intType())) {
                RewardProductDetail product = newRewardLoaderClient.generateRewardProductDetail(user, order.getProductId());
                if (Objects.nonNull(product)) {
                    map.put("couponResource", product.getCouponResource());
                }
            } else if (Objects.equals(newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(order), OneLevelCategoryType.JPZX_TEACHING_RESOURCES.intType())) {
                RewardProductDetail product = newRewardLoaderClient.generateRewardProductDetail(user, order.getProductId());
                if (Objects.nonNull(product)) {
                    map.put("url", product.getUsedUrl());
                }
            }

            orderMapLists.add(map);
            currentTotalPrice = currentTotalPrice.add(new BigDecimal(order.getTotalPrice() == null ? 0D : order.getTotalPrice()));
        }

        Set<Long> orderProductIdSet = orderList.stream().map(RewardOrder::getProductId).collect(Collectors.toSet());
        myCoupon.values()
                .stream()
                .filter(coupon -> !orderProductIdSet.contains(coupon.getProductId()))
                .forEach(coupon -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    RewardCouponDetail couponDetail = myCoupon.get(coupon.getProductId());
                    RewardProductDetail product = newRewardLoaderClient.generateRewardProductDetail(user, coupon.getProductId());
                    if (Objects.nonNull(product)) {
                        List<RewardImage> images = rewardImages.get(product.getId());
                        if (CollectionUtils.isNotEmpty(images)) {
                            map.put("image", images.get(0).getLocation());
                        }
                        map.put("detailId", couponDetail.getId());
                        map.put("productId", product.getId());
                        map.put("productName", product.getProductName());
                        map.put("couponNo", couponDetail.getCouponNo());
                        map.put("used", couponDetail.getUsed());
                        map.put("productRebated", product.getRebated());
                        map.put("rebated", couponDetail.getRebated());
                        map.put("url", product.getUsedUrl());
                        map.put("createTime", DateUtils.dateToString(couponDetail.getCreateDatetime(), "yyyy年MM月dd日"));
                        map.put("oneLevelCategoryType", newRewardLoaderClient.fetchOnelevelCategoryTypeIncludeOldCategory(product));
                        map.put("couponResource", product.getCouponResource());
                        orderMapLists.add(map);
                    }
                });
        orderMapLists.stream().sorted((o1, o2) -> o2.get("createTime").toString().compareTo(o1.get("createTime").toString()));
        model.addAttribute("orderMapList", orderMapLists);
        model.addAttribute("totalPrice", currentTotalPrice.setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        return "reward/order/myexperience";
    }

    //修改已兑换奖品数量
    @RequestMapping(value = "updateorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateOrder() {
        User user = currentRewardUser();

        Long orderId = getRequestLong("orderId");
        int quantity = getRequestInt("quantity");
        RewardOrder order = rewardLoaderClient.getRewardOrderLoader().loadRewardOrder(orderId);
        if (order == null) {
            return MapMessage.errorMessage("订单不存在");
        }
        if (!user.getId().equals(order.getBuyerId())) {
            return MapMessage.errorMessage("订单与用户不一致");
        }
        if (quantity <= 0) {
            return MapMessage.errorMessage("订单数量必须大于0个");
        }
        if (quantity == order.getQuantity()) {
            return MapMessage.errorMessage("没有修改");
        }
        RewardProduct product = crmRewardService.$loadRewardProduct(order.getProductId());
        RewardSku sku = crmRewardService.$loadRewardSku(order.getSkuId());
        return rewardServiceClient.updateRewardOrder(user, order, product, sku, quantity);
    }

    //删除已兑换奖品
    @RequestMapping(value = "removeorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeOrder() {
        Long orderId = getRequestLong("orderId");
        RewardOrder order = rewardLoaderClient.getRewardOrderLoader().loadRewardOrder(orderId);
        return rewardServiceClient.deleteRewardOrder(currentRewardUser(), order);
    }

    //删除愿望盒
    @RequestMapping(value = "removewishorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeWishOrder() {
        Long wishOrderId = getRequestLong("wishOrderId");
        return rewardServiceClient.deleteRewardWishOrder(currentUserId(), wishOrderId);
    }

    /**
     * 体验详情页 -- 绑定手机 -- 发送验证码
     */
    @RequestMapping(value = "sendmobilecodecoupon.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendMobileCodeCoupon() {
        try {
            String mobile = getRequest().getParameter("mobile");
            return getSmsServiceHelper().sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.VERIFY_MOBILE_COUPON);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    /*
     * 兑换优惠码
     */
    @RequestMapping(value = "exchangedcoupon.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage exchangedCoupon() {
        try {

            User user = currentRewardUser();

            String mobile = getRequestParameter("mobile", "");
            String smsCode = getRequestParameter("smsCode", "");
            Long productId = getRequestLong("productId");
            String couponUserRefId = getRequestString("couponUserRefId");

            // 是否校验手机的标志
            Boolean ignoreMobile = getRequestBool("ignoreMobile", false);
            if (!ignoreMobile) {
                if (StringUtils.isBlank(mobile)) {
                    return MapMessage.errorMessage("请填写正确的手机号");
                }
                if (StringUtils.isBlank(smsCode)) {
                    return MapMessage.errorMessage("验证码为空");
                }
            }

            if (!ignoreMobile) {
                MapMessage verifySmsResult = verifyCouponMobile(user, mobile, smsCode);
                if (!verifySmsResult.isSuccess())
                    return verifySmsResult;
            } else {
                // 如果不验证手机的话，取得家长的手机号，后续发短信提醒
                if (user.isStudent()) {
                    mobile = getParentMobile(user.getId());
                }
                // 老师兑换某些虚拟商品需要发短信提醒
                else if (user.isTeacher()) {
                    String am = sensitiveUserDataServiceClient.showUserMobile(
                            user.getId(), "reward/exchangecoupon", SafeConverter.toString(user.getId()));
                    if (am != null)
                        mobile = am;
                }
            }

            TeacherCouponEntity teacherCoupon = null;
            if (user.isTeacher() && StringUtils.isNotBlank(couponUserRefId)) {
                List<CouponShowMapper> couponShowMapperList = couponLoaderClient.loadUserRewardCoupons(user.getId());
                if (CollectionUtils.isEmpty(couponShowMapperList)) {//无兑换券
                    return MapMessage.errorMessage("该优惠券已失效，请检查并重新兑换!");
                }
                for (CouponShowMapper mapper : couponShowMapperList) {
                    if (Objects.equals(mapper.getCouponUserStatus().getDesc(), CouponUserStatus.NotUsed.getDesc()) && Objects.equals(mapper.getCouponUserRefId(), couponUserRefId)) {
                        teacherCoupon = new TeacherCouponEntity(mapper.getTypeValue().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(), mapper.getCouponUserRefId());
                        break;
                    }
                }
                //兑换券已失效
                if (teacherCoupon == null) {
                    return MapMessage.errorMessage("该优惠券已失效，请检查并重新兑换!");
                }
            }

            //执行兑换
            MapMessage resultMsg = atomicLockManager.wrapAtomic(rewardServiceClient)
                    .keys(currentUserId())
                    .keyPrefix("ExchangedCoupon")
                    .proxy()
                    .exchangedCoupon(productId, user, mobile, teacherCoupon);

            // 后续处理
            if (resultMsg.isSuccess()) {
                //使用掉优惠券
                if (user.isTeacher() && teacherCoupon != null) {
                    couponServiceClient.updateCouponUserRefStatus(couponLoaderClient.loadCouponUserRefById(teacherCoupon.getCouponUserRefId()), CouponUserStatus.Used);
                }
            }

            return resultMsg;

        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.errorMessage("请不要重复兑换");
            }
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("兑换失败");
        }
    }

    private String getParentMobile(Long id) {

        StudentParent studentParent = parentLoaderClient.loadStudentKeyParent(id);
        if (studentParent != null) {
            String am = sensitiveUserDataServiceClient.showUserMobile(
                    studentParent.getParentUser().getId(), "/reward/order/exchangedcoupon",
                    SafeConverter.toString(studentParent.getParentUser().getId()));
            if (StringUtils.isNotEmpty(am)) {
                return am;
            }
        }

        List<StudentParent> studentParentList = parentLoaderClient.loadStudentParents(id);

        if (CollectionUtils.isEmpty(studentParentList)) {
            return null;
        }

        for (StudentParent sp : studentParentList) {
            if (!sp.isKeyParent()) {
                String am = sensitiveUserDataServiceClient.showUserMobile(
                        sp.getParentUser().getId(), "/reward/order/exchangedcoupon",
                        SafeConverter.toString(sp.getParentUser().getId()));
                if (StringUtils.isNotEmpty(am)) {
                    return am;
                }
            }
        }

        return null;
    }

    /**
     * 优惠券验证手机短信
     *
     * @param user
     * @param mobile
     * @param smsCode
     * @return
     */
    private MapMessage verifyCouponMobile(User user, String mobile, String smsCode) {
        //false : 表示手机是自己输入的,true表示来自于自己或自己的关键家长
        boolean authMobile = false;

        if (user.isResearchStaff()) {
            authMobile = true;
        } else {
            // 如果传入的手机是不是自己的或者自己的关键家长的手机
            List<UserAuthentication> uas = userLoaderClient.loadMobileAuthentications(mobile);
            if (!uas.isEmpty()) {
                Set<Long> mobileUids = new HashSet<>();
                for (UserAuthentication ua : uas) {
                    mobileUids.add(ua.getId());
                }
                if (user.isStudent()) {
                    StudentParent studentParent = parentLoaderClient.loadStudentKeyParent(currentUserId());
                    if (mobileUids.contains(user.getId()) || (studentParent != null && mobileUids.contains(studentParent.getParentUser().getId()))) {
                        authMobile = true;
                    }
                }
                if (user.isTeacher()) {
                    if (user.getAuthenticationState() != 1) {
                        return MapMessage.errorMessage("你还没有认证，不能兑换哦！").add("authentication", false);
                    }
                    if (mobileUids.contains(user.getId())) {
                        authMobile = true;
                    }
                }
            }
        }

        if (authMobile) {
            //如果手机号来自于自己或者自己的关键家长，则验证码从 CouchbaseMemcachedClient 的 STUDENT_BEILE_MOBILE_CODE_PREFIX 中取出
            String key = WellKnownCacheKeyGenerator.generateCouponMobileCodeCacheKey(user.getId(), mobile);
            String cacheSmsCode = washingtonCacheSystem.CBS.unflushable.load(key);
            if (StringUtils.isBlank(cacheSmsCode)) {
                return MapMessage.errorMessage("获取验证码失败，兑换失败");
            }
            //测试环境后门
            if (RuntimeMode.le(Mode.STAGING)) {
                cacheSmsCode = "123456";
            }
            if (!smsCode.equals(cacheSmsCode)) {
                return MapMessage.errorMessage("验证码输入错误");
            } else {
                // 更新消费状态
                smsServiceClient.getSmsService().verifyValidateCode(mobile, cacheSmsCode, SmsType.COUPON_SEND_CERTIFICATION_CODE.name());
                washingtonCacheSystem.CBS.unflushable.delete(key);
            }
        } else {
            //否则就验证绑定手机号
            //验证手机
            MapMessage verifyMessage = verificationService.verifyMobile(currentUserId(), smsCode, SmsType.VERIFY_MOBILE_COUPON.name());
            if (!verifyMessage.isSuccess()) {
                return verifyMessage;
            }
        }

        return MapMessage.successMessage();
    }

    /**
     * 返利
     */
    @RequestMapping(value = "rebate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rebate() {
        Long couponDetailId = getRequestLong("detailId");
        CouponProductionName couponProductionName = CouponProductionName.of(getRequestParameter("name", ""));
        if (null == couponProductionName) {
            return MapMessage.errorMessage("该产品不存在");
        }
        try {
            return atomicLockManager.wrapAtomic(rewardServiceClient)
                    .keys(couponDetailId)
                    .proxy()
                    .couponRebate(currentUserId(), couponDetailId, couponProductionName);
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.errorMessage("请不要重复申请返利");
            }
            logger.error("FAILED TO REBATE, STUDENT '{}', COUPONDETAILID '{}'", currentUserId(), couponDetailId, ex);
            return MapMessage.errorMessage("返利失败");
        }
    }

    /**
     * 体验详情页 -- 给手机发送验证码（手机号不与用户绑定）
     */
    @RequestMapping(value = "sendmobilecodemessage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendmobilecodemessage() {
        try {
            String mobile = getRequest().getParameter("mobile");
            if (StringUtils.isBlank(mobile) || currentUserId() == null) {
                return MapMessage.errorMessage("请输入手机号码或重新登录");
            }
            String key = WellKnownCacheKeyGenerator.generateCouponMobileCodeCacheKey(currentUserId(), mobile);
            CacheObject<String> cacheObject = washingtonCacheSystem.CBS.unflushable.get(key);
            if (cacheObject == null) {
                return MapMessage.errorMessage("出错了，请重试一次吧~");
            }
            String content = cacheObject.getValue();
            if (content == null) {
                // 发送短信
                String code = RandomUtils.randomNumeric(6); // 验证码
                Boolean ret = washingtonCacheSystem.CBS.unflushable.add(key, 180, code);
                if (!Boolean.TRUE.equals(ret)) {
                    return MapMessage.errorMessage("出错了，请重试一次吧~");
                }
                String sms = StringUtils.formatMessage("尊敬的用户，你的短信验证码：{}", code);
                smsServiceClient.createSmsMessage(mobile)
                        .content(sms)
                        .type(SmsType.COUPON_SEND_CERTIFICATION_CODE.name())
                        .send();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
        return MapMessage.successMessage("验证码发送成功");
    }

    //本周奖励
    @RequestMapping(value = "weekreward.vpage", method = RequestMethod.GET)
    public String weekReward(Model model) {
        User user = currentRewardUser();
        //只有老师才能进入
        if (user.fetchUserType() != UserType.TEACHER) {
            return "redirect:/reward/index.vpage";
        }
        //是否满足领取条件
        boolean canReward = false;
        Map<Long, Set<String>> dataMap = newHomeworkCacheServiceClient.getNewHomeworkCacheService().assignHomeworkAndQuizDayCountManager_currentDays(Collections.singletonList(user.getId()));
        if (dataMap.get(user.getId()) != null && dataMap.get(user.getId()).size() >= 3) {
            List<TeacherTaskRewardHistory> histories = businessTeacherServiceClient.loadTeacherTaskRewardHistory(user.getId(), TeacherTaskType.WEEK_ASSIGN_TASK);
            if (CollectionUtils.isEmpty(histories)) {
                canReward = true;
            } else {
                histories = histories.stream().filter(h -> h.getCreateDatetime().after(WeekRange.current().getStartDate())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(histories)) {
                    canReward = true;
                }
            }
        }
        model.addAttribute("canReward", canReward);
        return "reward/order/weekreward";
    }

    // 开启月光宝盒
    @RequestMapping(value = "openmoonlightbox.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage openMoonLightBox() {
        User user = currentRewardUser();
        Long productId = getRequestLong("productId");
        Long skuId = getRequestLong("skuId");
        if (user.fetchUserType() == null || user.fetchUserType() != UserType.TEACHER) {
            return MapMessage.errorMessage("只有老师才可以试手气！");
        }

        //假老师处理
        if (teacherLoaderClient.isFakeTeacher(user.getId())) {
            return MapMessage.errorMessage("您的账号使用存在异常，该功能受限。如有疑议，请联系客服400-606-1717");
        }

        RewardProductDetail productDetail = newRewardLoaderClient.generateRewardProductDetail(user, productId);
        if (productDetail == null || !productDetail.getOnline()) {
            return MapMessage.errorMessage("对不起！奖品已经下架，请去挑选其他奖品试试手气哦！");
        }
        RewardSku sku = null;
        if (newRewardLoaderClient.isSHIWU(productDetail.getOneLevelCategoryId())) {
            sku = crmRewardService.$loadRewardSku(skuId);
            if (sku == null) {
                return MapMessage.errorMessage("对不起！奖品不存在!");
            }
            if (sku.getInventorySellable() < 1) {
                return MapMessage.errorMessage("奖品数量不足！");
            }
        }
        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(user.getId());
        if (user.getAuthenticationState() != AuthenticationState.SUCCESS.getState()) {
            return MapMessage.errorMessage("对不起，认证老师才可以试手气！").add("authentication", true);
        }
        //判断用户绑手机
        if (!authentication.isMobileAuthenticated()) {
            return MapMessage.errorMessage("对不起，绑定手机后才可以试手气！").add("bindMobile", true);
        }
        //收获地址为空
        UserShippingAddress shippingAddress = userLoaderClient.loadUserShippingAddress(user.getId());
        if (shippingAddress == null || StringUtils.isBlank(shippingAddress.getDetailAddress())) {
            return MapMessage.errorMessage("对不起，填写地址后才可以试手气！").add("address", true);
        }
        TeacherDetail teacherDetail = (TeacherDetail) user;
        //等级判断
        if (productDetail.getAmbassadorLevel() > 0) {
            //大使判断
            if (!teacherDetail.isSchoolAmbassador()) {
                return MapMessage.errorMessage("对不起，该奖品为大使奖品");
            }
        }
        return rewardServiceClient.openMoonLightBox(teacherDetail, productDetail, sku);
    }

    @RequestMapping(value = "checkteacherexistforstu.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkTeacherExistForstu() {
        User user = currentRewardUser();
        if (user.fetchUserType() == UserType.STUDENT) {
            TeacherDetail teacher = userAggregationLoaderClient.loadStudentTeacherForRewardSending(user.getId());
            if (teacher == null) {
                return MapMessage.errorMessage("你的班级还没有收货老师，无法寄送哦！");
            }
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "notify.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage notifyShow() {
        User user = currentRewardUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录");
        }
        boolean showDeductNotify = false;
        if (user.isTeacher()) {
            showDeductNotify = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(currentTeacherDetail(),
                    "Reward", "ExchangeReduction", true);
        }
        return MapMessage.successMessage().add("showDeductNotify", showDeductNotify);
    }
}
