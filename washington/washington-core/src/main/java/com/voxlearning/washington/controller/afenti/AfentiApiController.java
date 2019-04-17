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

package com.voxlearning.washington.controller.afenti;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.payment.PaymentRequestForm;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.mapper.NewClazzBookRefMapper;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wonderland.client.AsyncWonderlandCacheServiceClient;
import com.voxlearning.washington.mapper.GroupProductMapper;
import com.voxlearning.washington.support.WashingtonRequestContext;
import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;
import static com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil.testAmount;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by xinxin on 9/4/2016.
 * 用于学生APP产品购买相关的接口
 */
@Controller
@RequestMapping(value = "/api/1.0/afenti/")
public class AfentiApiController extends StudentAfentiBaseController {
    @Inject private AsyncWonderlandCacheServiceClient asyncWonderlandCacheServiceClient;
    @Inject private FinanceServiceClient financeServiceClient;
    @Inject private CouponLoaderClient couponLoaderClient;

    // 获取适合当前学生年级的产品列表
    @RequestMapping(value = "/products.vpage")
    @ResponseBody
    public MapMessage products() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        OrderProductServiceType type = getOrderProductServiceType(subject);
        if (type == null) return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            List<OrderProduct> pis;
            if (type == ValueAddedLiveTimesCard) {
                pis = userOrderLoaderClient.loadItemBaseProductsByType(type);
            } else {
                pis = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(student)
                        .stream()
                        .filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == type)
                        .collect(Collectors.toList());
            }

            // 获取商品tips
            Map<String, List<String>> product_tips_map = fetchProductTips(type);

            List<Map<String, Object>> products = new ArrayList<>();
            for (OrderProduct pi : pis) {
                Map<String, Object> product = new HashMap<>();
                product.put("id", pi.getId());
                product.put("name", pi.getName());
                product.put("desc", pi.getDesc()); // 标题
                product.put("price", pi.getPrice());
                product.put("originalPrice", pi.getOriginalPrice());
                product.put("type", pi.getProductType());
                product.put("attributes", pi.getAttributes());
                // todo 只有item是一个的时候  如果有捆绑销售的产品， 请做出修改
                List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(pi.getId());
                if (CollectionUtils.isNotEmpty(itemList) && itemList.size() == 1) {
                    OrderProductItem item = itemList.get(0);
                    product.put("rewardBeans", getLearningBeans(item.getPeriod()));
                    product.put("period", item.getPeriod());
                    String key = type.name() + "-" + item.getPeriod();
                    List<String> tips = new ArrayList<>();
                    if (product_tips_map.containsKey(key)) tips.addAll(product_tips_map.get(key));
                    product.put("tips", tips);
                }
                products.add(product);
            }
            MapMessage result = MapMessage.successMessage();
            // 判断当前用户是否满足捆绑点读机的要求
            boolean buyGroup = false;
            String bookId = "";
            // 是否满足灰度
            boolean grayFlag = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "GP", "Open");
            if ((type == AfentiChinese || type == AfentiExam) && grayFlag) {
                // 判断当前教材是人教版
                boolean bookFlag = false;
                if (student != null && student.getClazz() != null) {
                    List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false);
                    GroupMapper mapper = groupMappers.stream().filter(g -> g.getSubject() == subject)
                            .findFirst().orElse(null);
                    if (mapper != null) {
                        NewClazzBookRefMapper bookRefMapper = newClazzBookLoaderClient
                                .findNewClazzBookRefWithDefault(mapper.getId(), subject, student.getClazzLevel(), student.getClazz().getSchoolId());
                        if (bookRefMapper != null && StringUtils.isNotBlank(bookRefMapper.getBookId())) {
                            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookRefMapper.getBookId());
                            if (newBookProfile != null && StringUtils.equals("人教版", newBookProfile.getShortPublisher())) {
                                bookId = newBookProfile.getId();
                                bookFlag = true;
                            }
                        }

                    }
                }
                if (bookFlag) {
                    List<StudentParent> parents = parentLoaderClient.loadStudentParents(student.getId());
                    if (CollectionUtils.isNotEmpty(parents)) {
                        // 未购买过点读机
                        boolean hasOrder = false;
                        for (StudentParent parent : parents) {
                            AppPayMapper paidMapper = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.PicListenBook.name(), parent.getParentUser().getId());
                            if (paidMapper.hasPaid()) {
                                hasOrder = true;
                                break;
                            }
                        }
                        if (!hasOrder) {
                            buyGroup = true;
                        }
                    }
                }
            }
            result.add("buyGroup", buyGroup);

            // 寻找产品
            if (buyGroup) {
                GroupProductMapper mapper = loadGroupProduct(bookId);
                result.add("groupMapper", mapper);
            }

            try {
                List<Map<String, Object>> purchaseInfos = afentiLoaderClient.loadPurchaseInfos(student);
                result.add("purchaseInfos", purchaseInfos);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            return result.add("products", products);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    private GroupProductMapper loadGroupProduct(String bookId) {
        GroupProductMapper mapper = new GroupProductMapper();
        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductType(OrderProductServiceType.PicListenBook);
        OrderProductItem item = itemList.stream().filter(i -> StringUtils.equals(i.getAppItemId(), bookId))
                .findFirst().orElse(null);
        if (item != null) {
            List<OrderProduct> productList = userOrderLoaderClient.loadAvailableProduct()
                    .stream().filter(o -> o.getProductType() != null && OrderProductServiceType.safeParse(o.getProductType()) == GroupProduct)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(productList)) {
                Set<String> productIds = productList.stream().map(OrderProduct::getId).collect(Collectors.toSet());
                Map<String, List<OrderProductItem>> itemMap = userOrderLoaderClient.loadProductItemsByProductIds(productIds);
                for (Map.Entry<String, List<OrderProductItem>> entry : itemMap.entrySet()) {
                    OrderProductItem targetItem = entry.getValue().stream().filter(i -> Objects.equals(i.getId(), item.getId()))
                            .findFirst().orElse(null);
                    if (targetItem != null) {
                        OrderProduct product = productList.stream().filter(o -> Objects.equals(o.getId(), entry.getKey()))
                                .findFirst().orElse(null);
                        if (product != null) {
                            mapper.setProduct(product);
                            mapper.setPicListenBookItem(item);
                            break;
                        }
                    }
                }
            }
        }
        return mapper;
    }

    // 打包购买点读机  获取家长信息
    @RequestMapping(value = "/group/parents.vpage")
    @ResponseBody
    public MapMessage groupParents() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        // 放入家长信息
        List<StudentParent> parents = parentLoaderClient.loadStudentParents(student.getId());
        List<Map<String, Object>> parentList = new ArrayList<>();
        for (StudentParent studentParent : parents) {
            // load obscured mobile
            Long parentId = studentParent.getParentUser().getId();
            Map<String, Object> map = new HashMap<>();
            map.put("parentId", parentId);
            map.put("callName", studentParent.getCallName());
            String obscuredMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(parentId);
            map.put("mobile", obscuredMobile);
            parentList.add(map);
        }
        return MapMessage.successMessage().add("parentList", parentList);
    }

    // 获取适合当前学生年级的产品列表
    @RequestMapping(value = "/video/products.vpage")
    @ResponseBody
    public MapMessage videoProduct() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        OrderProductServiceType type = getVideoProductServiceType(subject);
        if (type == null) return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        try {
            List<OrderProduct> pis = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(student)
                    .stream()
                    .filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == type)
                    .filter(p -> StringUtils.equals(p.getStatus(), "ONLINE"))
                    .collect(Collectors.toList());


            List<Map<String, Object>> products = new ArrayList<>();
            for (OrderProduct pi : pis) {

                //获取当前年纪与学期的产品
                if (SafeConverter.toInt(pi.fetchAttribute("clazzLevel")) != student.getClazzLevel().getLevel()
                        || SafeConverter.toInt(pi.fetchAttribute("termType")) != SchoolYear.newInstance().currentTerm().getKey()) {
                    continue;
                }


                Map<String, Object> product = new HashMap<>();
                product.put("id", pi.getId());
                product.put("name", pi.getName());
                product.put("desc", pi.getDesc()); // 标题
                product.put("price", pi.getPrice());
                product.put("originalPrice", pi.getOriginalPrice());
                product.put("type", pi.getProductType());
                product.put("attributes", pi.getAttributes());
                List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(pi.getId());
                if (CollectionUtils.isNotEmpty(itemList) && itemList.size() == 1) {
                    OrderProductItem item = itemList.get(0);
                    product.put("period", item.getPeriod());
                }
                products.add(product);
            }

            return MapMessage.successMessage().add("products", products);
        } catch (Exception ex) {

            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    // 生成订单并跳转到支付确认页
    @RequestMapping(value = "/order/submit.vpage", method = RequestMethod.GET)
    public String submitOrder(Model model) {
        boolean mobile = isMobileRequest(getRequest());
        model.addAttribute("hideTopTitle", getRequestBool("hideTopTitle", false)); // 是否显示h5页面内的顶部title和返回
        model.addAttribute("hideAppTitle", getRequestBool("hideAppTitle", false));
        model.addAttribute("returnUrl", getRequestString("returnUrl"));
        model.addAttribute("appType",getRequestString("appType"));
        String appKey = getRequestString(REQ_APP_KEY);
        if(StringUtils.isNotBlank(appKey)){
            model.addAttribute("appKey",appKey);
        }else{
            model.addAttribute("appKey","");
        }

        try {
            // 判断用户登陆状态
            MapMessage mesg = currentAfentiStudentDetail();
            if (!mesg.isSuccess()) {
                if (mobile) {
                    model.addAttribute("error", "请使用学生帐号登录");
                    return "/paymentmobile/confirm";
                } else {
                    return "redirect:/";
                }
            }

            StudentDetail student = (StudentDetail) mesg.get("studentDetail");
            Clazz clazz = student.getClazz();
            if (clazz == null || clazz.isTerminalClazz()) {
                if (mobile) {
                    model.addAttribute("error", "班级信息有误，请联系客服");
                    return "/paymentmobile/confirm";
                } else {
                    return "redirect:/";
                }
            }

            // 判断产品
            List<String> productIds = Arrays.asList(StringUtils.split(getRequestString("productId"), ","));
            if (CollectionUtils.isEmpty(productIds)) {
                if (mobile) {
                    model.addAttribute("error", "请选择产品");
                    return "/paymentmobile/confirm";
                } else {
                    return "redirect:/";
                }
            }

            Map<String, OrderProduct> products = userOrderLoader.loadAllOrderProduct()
                    .stream()
                    .filter(OrderProduct::isOnline)
                    .filter(p -> !p.isDisabledTrue())
                    .filter(p -> productIds.contains(p.getId()))
                    .collect(Collectors.toMap(OrderProduct::getId, Function.identity()));

            if (products.size() != productIds.size()) {
                if (mobile) {
                    model.addAttribute("error", "你购买的产品已经下架，请重新选择");
                    return "/paymentmobile/confirm";
                } else {
                    return "redirect:/";
                }
            }

            for (OrderProduct product : products.values()) {
                // 如果是听课体验卡，每周只能买一次
                if (OrderProductServiceType.safeParse(product.getProductType()) == ValueAddedLiveTimesCard && StringUtils.equals(product.getAttributes(), "sample_sack")
                        && asyncWonderlandCacheServiceClient.getAsyncWonderlandCacheService().eagletSampleSackCacheManager_purchased(student.getId()).getUninterruptibly()) {
                    if (mobile) {
                        model.addAttribute("error", "超值体验卡每周只能购买一次哦~");
                        return "/paymentmobile/confirm";
                    } else {
                        return "redirect:/";
                    }
                }

                // 阿分提 提高版限制
                if (AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(product.getProductType()))) {
                    Date now = new Date();
                    Map<String, Object> attrMap = JsonUtils.fromJson(product.getAttributes());
                    Date endDate = SafeConverter.toDate(attrMap != null ? attrMap.get("endDate") : null);
                    if (endDate != null && endDate.before(now)) {
                        if (mobile) {
                            model.addAttribute("error", "产品已经过了有效期~");
                            return "/paymentmobile/confirm";
                        } else {
                            return "redirect:/";
                        }
                    }

                    // 检查是否有开通记录
                    AppPayMapper paidStatus = userOrderLoader.getUserAppPaidStatus(product.getProductType(), student.getId(), true);
                    if (paidStatus != null && CollectionUtils.isNotEmpty(paidStatus.getValidProducts()) && paidStatus.getValidProducts().contains(product.getId())) {
                        if (mobile) {
                            model.addAttribute("error", "该产品只能购买一次~");
                            return "/paymentmobile/confirm";
                        } else {
                            return "redirect:/";
                        }
                    }
                }

                // 校验悟空
                int grade = student.getClazzLevelAsInteger();
                if ((OrderProductServiceType.safeParse(product.getProductType()) == WukongShizi || OrderProductServiceType.safeParse(product.getProductType()) == WukongPinyin) && grade > 2) {
                    if (mobile) {
                        model.addAttribute("error", "悟空拼音和悟空识字只适合一二年级的学生使用");
                        return "/paymentmobile/confirm";
                    } else {
                        return "redirect:/";
                    }
                }
            }

            OrderProduct first = products.get(productIds.get(0));
            String name = productIds.size() == 1 ? first.getName() : first.getName() + "等" + productIds.size() + "个自学应用";
            if (OrderProductServiceType.safeParse(first.getProductType()) == ELevelReading) {
                name = "小U绘本（" + productIds.size() + "本）";
            }

            MapMessage order = userOrderService.createAppOrder(student.getId(), first.getProductType(),
                    productIds, name, getRequestString("refer"));
            model.addAttribute("orderId", order.get("orderId"));

            boolean IOSFinanceRecharge = true;
            if (student != null) {
                IOSFinanceRecharge = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "Order", "FinanceRecharge");
            }
            model.addAttribute("IOSFinanceRecharge", IOSFinanceRecharge);
            //H5支付灰度变量
            boolean isOpenH5payment = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "Order", "H5Pay");
            model.addAttribute("isOpenH5payment",isOpenH5payment);
            //学生端学贝支付灰度测试
            String userAgent = getRequest().getHeader("User-Agent").toLowerCase();
            boolean isFinancePayment = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "Order", "FinancePay");
            if(isFinancePayment){
                UserOrder userOrder = userOrderLoaderClient.loadUserOrder(SafeConverter.toString(order.get("orderId")));
                List<CouponShowMapper> mappers = userOrderLoaderClient.loadOrderUsableCoupons(userOrder, student.getId());
                model.addAttribute("isFinancePayment",userAgent.contains("17student"));
                //查询学贝够不够支付
                List<StudentParent> studentParentList = parentLoaderClient.loadStudentParents(student.getId());
                boolean isFinanceEnougthPay = false;
                List<Finance> finances = new ArrayList<>();
                for (StudentParent parent : studentParentList) {
                    Finance parFinance = financeServiceClient.getFinanceService()
                            .createUserFinanceIfAbsent(parent.getParentUser().getId())
                            .getUninterruptibly();
                    if (Objects.nonNull(parFinance)) {
                        finances.add(parFinance);
                    }
                }
                //查询自己的学贝
                Finance stuFinance = financeServiceClient.getFinanceService().createUserFinanceIfAbsent(student.getId())
                        .getUninterruptibly();
                if (Objects.nonNull(stuFinance)){
                    finances.add(stuFinance);
                }
                if(CollectionUtils.isNotEmpty(finances)){
                    for(Finance finance : finances){
                        if(CollectionUtils.isNotEmpty(mappers)){
                            for(CouponShowMapper mapper : mappers){
                                BigDecimal newOrderPrice = new BigDecimal(mapper.getDiscountPrice());
                                if(finance.getBalance().compareTo(newOrderPrice) >= 0){
                                    isFinanceEnougthPay = true;
                                    break;
                                }
                            }
                        }else{
                            if(finance.getBalance().compareTo(new BigDecimal(SafeConverter.toString(order.get("price")))) >= 0){
                                isFinanceEnougthPay = true;
                                break;
                            }
                        }
                    }
                }

                if(!isFinanceEnougthPay && userAgent.contains("17student")){
                    if(CollectionUtils.isNotEmpty(studentParentList)){
                        model.addAttribute("isBindParent",true);
                        sendPushMsgAndSms(student,studentParentList,name);
                    }else{
                        model.addAttribute("isBindParent",false);
                        List<String> mobiles = new ArrayList<>();
                        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(student.getId());
                        if(StringUtils.isNotBlank(authentication.getSensitiveMobile())){
                            mobiles.add(SensitiveLib.decodeMobile(authentication.getSensitiveMobile()));
                        }
                        sendMessage(student.fetchRealname(),name,mobiles);
                    }
                    return "/paymentmobile/finance";
                }
            }

            if (order.isSuccess()) {
                if (mobile) {
                    model.addAttribute("productName", name);
                    model.addAttribute("amount", order.get("price"));
                    model.addAttribute("type", "afenti");

                    // 学生是否开启支付权限
                    StudentExtAttribute attribute = studentLoaderClient.loadStudentExtAttribute(student.getId());
                    if (currentUser().fetchUserType() == UserType.PARENT || attribute == null || attribute.fetchPayFreeStatus()) {
                        return "/paymentmobile/confirm";
                    } else {
                        // 获取家长列表
                        List<StudentParent> parents = parentLoaderClient.loadStudentParents(student.getId());
                        if (CollectionUtils.isNotEmpty(parents)) {
                            List<Map<String, Object>> parentMaps = new ArrayList<>();
                            for (StudentParent parent : parents) {
                                Map<String, Object> p = new HashMap<>();
                                p.put("parentId", parent.getParentUser().getId());
                                p.put("callName", parent.getCallName());
                                parentMaps.add(p);
                            }
                            model.addAttribute("parentList", parentMaps);
                            return "/paymentmobile/authority";
                        } else {
                            return "/paymentmobile/confirm";
                        }
                    }
                } else {
                    return "redirect:/apps/afenti/order/confirm.vpage?orderId=" + order.get("orderId");
                }
            } else {
                if (mobile) {
                    model.addAttribute("error", StringUtils.defaultIfBlank(order.getInfo(), "生成订单失败"));
                    return "/paymentmobile/confirm";
                } else {
                    return "redirect:/";
                }
            }
        } catch (Exception ex) {
            if (mobile) {
                logger.error("Create afenti order error,pid:{}", getRequestString("productId"), ex);
                model.addAttribute("error", "生成订单失败");
                return "/paymentmobile/confirm";
            } else {
                return "redirect:/";
            }
        }
    }

    /**
     * 学生端取消订单
     */
    @RequestMapping(value = "order/cancel.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancel() {
        User user = currentUser();
        if (user == null || user.fetchUserType() != UserType.STUDENT) {
            return MapMessage.errorMessage("请重新登录");
        }
        String orderId = getRequestParameter("orderId", "");
        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        // 订单号不正确
        if (order == null) {
            return MapMessage.errorMessage("订单号错误");
        }
        if (!Objects.equals(order.getUserId(), user.getId())) {
            return MapMessage.errorMessage("对不起，你不能取消该订单");
        }
        // 已支付的订单不允许取消
        if (order.getPaymentStatus() == PaymentStatus.Paid) {
            return MapMessage.errorMessage("对不起，已支付的订单不能取消");
        }
        return userOrderServiceClient.cancelOrder(orderId);
    }

    /**
     * 学生端发送订单到家长端支付（发送提醒）
     */
    @RequestMapping(value = "order/remindparent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage remindParent() {
        User user = currentUser();
        if (user == null || user.fetchUserType() != UserType.STUDENT) {
            return MapMessage.errorMessage("请重新登录");
        }
        String orderId = getRequestParameter("orderId", "");
        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        // 订单号不正确
        if (order == null) {
            return MapMessage.errorMessage("订单号错误");
        }
        if (!Objects.equals(order.getUserId(), user.getId())) {
            return MapMessage.errorMessage("对不起，你不能操作该订单");
        }
        String parentIdStr = getRequestString("parentId");
        if (StringUtils.isBlank(parentIdStr)) {
            return MapMessage.errorMessage("请选择要发送的家长");
        }
        String[] parentArray = StringUtils.split(parentIdStr, ",");
        List<Long> parentIdList = new ArrayList<>();
        for (String parentId : parentArray) {
            parentIdList.add(SafeConverter.toLong(parentId));
        }
        try {
            return AtomicLockManager.instance().wrapAtomic(businessUserOrderServiceClient)
                    .keyPrefix("REMIND_ORDER")
                    .keys(order.genUserOrderId())
                    .proxy()
                    .remindParentForOrder(order, parentIdList);
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理，请不要重复提交！");
        }
    }

    // 确认支付,准备支付参数
    @RequestMapping(value = "/order/confirm.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage confirmOrder(@RequestParam String order_id, @RequestParam String payment_gateway) { //这个奇怪的参数名是为了与开放接口一致,因为前端用的是同一个ftl
        WashingtonRequestContext context = getRequestContext();
        if (RuntimeModeLoader.getInstance().isProduction()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://x.17zuoye.com");
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://x.staging.17zuoye.net");
        } else if (RuntimeModeLoader.getInstance().isTest()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://x.test.17zuoye.net");
        }
        context.getResponse().addHeader("Access-Control-Allow-Methods", "POST");
        context.getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with");
        context.getResponse().addHeader("Access-Control-Max-Age", "1800");
        context.getResponse().addHeader("Access-Control-Allow-Credentials", "true");

        if (StringUtils.isBlank(order_id)) return MapMessage.errorMessage("无效订单号");

        try {
            User user = currentUser();
            if (null == user || (!user.isStudent() && !user.isParent())) {
                return MapMessage.errorMessage("帐号未登录,不能支付");
            }

            Long orderUserId = 0l;
            FinanceFlow flow = null;
            UserOrder userOrder = null;
            String paySource = getRequestString("pay_source");
            if("finance".equals(paySource)){
                flow = financeServiceClient.getFinanceService().loadFinanceFlow(order_id).getUninterruptibly();
                if (flow == null) {
                    return MapMessage.errorMessage(RES_RESULT_ORDER_UNKNOW_MSG);
                }
                if (Objects.equals(flow.getState(), FinanceFlowState.SUCCESS.name())) {
                    return MapMessage.errorMessage(RES_RESULT_ORDER_HAD_PAID);
                }
                orderUserId = flow.getUserId();
            }else{
                userOrder = userOrderLoaderClient.loadUserOrder(order_id);
                if (null == userOrder) {
                    LoggerUtils.error("orderIsNull", null,order_id);
                    return MapMessage.errorMessage("未查询到订单信息");
                }

                if(StringUtils.isNotBlank(userOrder.getCouponRefId())){
                    CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(userOrder.getCouponRefId());
                    if(couponUserRef != null && couponUserRef.getStatus() == CouponUserStatus.Used){
                        return MapMessage.errorMessage("优惠券已被使用");
                    }
                }

                if (userOrder.getOrderStatus() != OrderStatus.New || userOrder.getPaymentStatus() != PaymentStatus.Unpaid) {
                    return MapMessage.errorMessage("订单不可支付");
                }

                // 下订单时check过了,这里再check一次,过滤从未支付订单页面过来的请求
                MapMessage mesg = userOrderService.checkPurchaseAllowed(userOrder);
                if (!mesg.isSuccess()) {
                    return MapMessage.errorMessage("您已经开通了" + userOrder.getProductName() + "，请在【孩子学情】-【我的自学应用】中使用哦 ");
                }
                orderUserId = userOrder.getUserId();
            }

            //添加支付日志
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(orderUserId);
            userServiceRecord.setOperatorId(user.getId().toString());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.订单支付.name());
            userServiceRecord.setOperationContent(order_id);
            String sys = getRequestString("mobile_sys");
            String model = getRequestString("mobile_model");
            userServiceRecord.setComments(StringUtils.formatMessage("手机系统:{}, 手机型号:{}", sys, model));
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            PaymentRequest paymentRequest = generatePaymentRequest(paySource, flow, userOrder, payment_gateway);
            if(paymentRequest.getPayAmount().compareTo(BigDecimal.ZERO) == 0){
                //如果支付金额为零的时候的特殊处理
                MapMessage result = new MapMessage();
                PaymentCallbackContext context1 = buildPaymentCallbackContext(paymentRequest);
                UserOrder zeroOrder = businessUserOrderServiceClient.processUserOrderPayment(context1);
                result.set(RES_RESULT, true);
                result.setSuccess(true);
                if (zeroOrder.getPaymentStatus() == PaymentStatus.Paid) {
                    result.set(RES_ORDER_STATUS, PaymentStatus.Paid);
                } else {
                    result.set(RES_ORDER_STATUS,PaymentStatus.Unpaid);
                }
                return result;
            }else{
                PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(payment_gateway);
                PaymentRequestForm paymentRequestForm = paymentGateway.getPaymentRequestForm(paymentRequest);
                if(PaymentConstants.PaymentGatewayName_Alipay_Wap_StudentApp.equals(payment_gateway)||
                        PaymentConstants.PaymentGatewayName_Alipay_Wap_ParentApp.equals(payment_gateway) ){
                    return MapMessage.successMessage().add("wapForm",paymentRequestForm.getWapForm());
                }else if(PaymentConstants.PaymentGatewayName_Wechat_H5_StudentApp.equals(payment_gateway)||
                        PaymentConstants.PaymentGatewayName_Wechat_H5_ParentApp.equals(payment_gateway)){
                    return MapMessage.successMessage().add("mwebUrl",paymentRequestForm.getFormFields().get("mwebUrl"));
                }
                return MapMessage.successMessage().add("payParams", paymentRequestForm.getFormFields());
            }
        } catch (Exception ex) {
            logger.error("order {} prepay failed", order_id, ex);
            return MapMessage.errorMessage("订单预支付失败");
        }
    }

    private PaymentRequest generatePaymentRequest(String paySource,FinanceFlow flow,UserOrder order, String paymentGateway) {
        PaymentRequest paymentRequest = new PaymentRequest();
        if("finance".equals(paySource)){
            paymentRequest.setTradeNumber(flow.getId());
            paymentRequest.setProductName("学贝充值");
            BigDecimal amount = flow.getPaymentAmount();
            if (PaymentGateway.getUsersForPaymentTest(flow.getUserId())) {
                amount = new BigDecimal(0.01);
            }
            paymentRequest.setPayAmount(amount);
            paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + "/payment/notify/recharge");
        }else{
            paymentRequest.setTradeNumber(order.genUserOrderId());
            paymentRequest.setProductName(order.getProductName());
            BigDecimal reallyPayAmount = userOrderServiceClient.getOrderCouponDiscountPrice(order);
            //考虑奖学金的情况
            if(Objects.nonNull(order.getGiveBalance())){
                reallyPayAmount = reallyPayAmount.subtract(order.getGiveBalance());
            }
            if (PaymentGateway.getUsersForPaymentTest(order.getUserId())) {
                if(reallyPayAmount.compareTo(BigDecimal.ZERO) == 0){
                    paymentRequest.setPayAmount(BigDecimal.ZERO);
                }else{
                    paymentRequest.setPayAmount(testAmount);
                }
            } else {
                paymentRequest.setPayAmount(reallyPayAmount);
            }
            paymentRequest.getExtParams().put("order_token",order.getOrderToken());
            paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order");
        }

        paymentRequest.setPayMethod(paymentGateway);
        if(PaymentConstants.PaymentGatewayName_Wechat_H5_StudentApp.equals(paymentGateway)||
                PaymentConstants.PaymentGatewayName_Wechat_H5_ParentApp.equals(paymentGateway)){
            if(RuntimeMode.isTest()){
                paymentRequest.setSpbillCreateIp("43.227.252.50");
            }else{
                paymentRequest.setSpbillCreateIp(getWebRequestContext().getRealRemoteAddress());
            }
        }else{
            paymentRequest.setSpbillCreateIp(getWebRequestContext().getRealRemoteAddress());
        }
        paymentRequest.getExtParams().put("return_url",getRequestString(REQ_RETURN_URL));
        paymentRequest.getExtParams().put("app_key",getRequestString(REQ_APP_KEY));
        paymentRequest.getExtParams().put("session_key", getRequestString(REQ_SESSION_KEY));
        String userAgent = getRequest().getHeader("User-Agent");
        if(userAgent.contains("iPhone")){
            userAgent = "IOS";
        }else{
            userAgent = "Android";
        }
        paymentRequest.getExtParams().put("scenceType",userAgent);
        paymentRequest.getExtParams().put("frontType",getRequestString("frontType"));
        paymentRequest.getExtParams().put("isSendAppInfo",getRequestString("isSendAppInfo"));
        paymentRequest.getExtParams().put("appType",getRequestString("appType"));
        paymentRequest.getExtParams().put("paySource",paySource);
        paymentRequest.getExtParams().put("orderRel",getRequestString("orderRel"));
        return paymentRequest;
    }

    //////////////////////////
    ///  PAY FOR TEST ////////
    //////////////////////////

    @RequestMapping(value = "payfortest-fail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payForTestFail(@RequestParam String oid) {
        if (RuntimeMode.current().gt(Mode.TEST)) return MapMessage.errorMessage("老实做人");

        String xml = generateWechatNotifyXml(oid, false);
        String notifyUrl = ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/wechatpay_studentapp-notify.vpage";

        MapMessage message = notify(xml, notifyUrl);

        // 较验1,返回结果得是false
        if (message.isSuccess()) return MapMessage.errorMessage();

        // 较验2,检查订单状态
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(oid);
        if (userOrder == null || userOrder.getPaymentStatus() != PaymentStatus.Unpaid)
            return MapMessage.errorMessage();

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "payfortest-success.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payForTestSuccess(@RequestParam String oid) {
        String xml = generateWechatNotifyXml(oid, true);
        String notifyUrl = ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/wechatpay_studentapp-notify.vpage";

        MapMessage message = notify(xml, notifyUrl);

        // 较验1,返回结果得是true
        if (!message.isSuccess()) return MapMessage.errorMessage();

        // 较验2,检查订单状态
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(oid);
        if (userOrder == null || userOrder.getPaymentStatus() != PaymentStatus.Paid)
            return MapMessage.errorMessage();

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "payfortest-repeat.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payForTestRepeat(@RequestParam String oid) {
        String xml = generateWechatNotifyXml(oid, true);
        String notifyUrl = ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/wechatpay_studentapp-notify.vpage";

        // 订单应该是已经支付过了的
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(oid);
        if (userOrder == null || userOrder.getPaymentStatus() != PaymentStatus.Paid)
            return MapMessage.errorMessage();

        MapMessage message = notify(xml, notifyUrl);

        // 较验1,返回结果得是true
        if (!message.isSuccess()) return MapMessage.errorMessage();


        // 较验3,订单没有被更新
        UserOrder userOrder1 = userOrderLoaderClient.loadUserOrder(oid);
        if (!userOrder.getUpdateDatetime().equals(userOrder1.getUpdateDatetime())) return MapMessage.errorMessage();

        return MapMessage.successMessage();
    }

    private String generateWechatNotifyXml(String orderId, boolean success) {
        Map<String, Object> params = new TreeMap<>();
        params.put("appid", ProductConfig.get(WechatType.PARENT.getAppId()));
        params.put("attach", "支付测试");
        params.put("bank_type", "CFT");
        params.put("fee_type", "CNY");
        params.put("is_subscribe", "Y");
        params.put("mch_id", "1219984501");
        params.put("nonce_str", "5d2b6c2a8db53831f7eda20af46e531c");
        params.put("openid", "oUpF8uMEb4qRXf22hE3X68TekukE");
        params.put("out_trade_no", orderId);
        params.put("result_code", success ? "SUCCESS" : "FAIL");
        params.put("return_code", success ? "SUCCESS" : "FAIL");
        params.put("sub_mch_id", "1219984501");

        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddhhmmss");
        params.put("time_end", formatter.format(new Date()));
        params.put("total_fee", "1");
        params.put("trade_type", "JSAPI");
        params.put("transaction_id", "1004400740201409030005092168" + RandomUtils.nextInt(1000, 9999));

        params.put("sign", "test");

        return "<xml>"
                + "<appid>" + params.get("appid") + "</appid>"
                + "<attach>" + params.get("attach") + "</attach>"
                + "<bank_type>" + params.get("bank_type") + "</bank_type>"
                + "<fee_type>" + params.get("fee_type") + "</fee_type>"
                + "<is_subscribe>" + params.get("is_subscribe") + "</is_subscribe>"
                + "<mch_id>" + params.get("mch_id") + "</mch_id>"
                + "<nonce_str>" + params.get("nonce_str") + "</nonce_str>"
                + "<openid>" + params.get("openid") + "</openid>"
                + "<out_trade_no>" + params.get("out_trade_no") + "</out_trade_no>"
                + "<result_code>" + params.get("result_code") + "</result_code>"
                + "<return_code>" + params.get("return_code") + "</return_code>"
                + "<sign>" + params.get("sign") + "</sign>"
                + "<sub_mch_id>" + params.get("sub_mch_id") + "</sub_mch_id>"
                + "<time_end>" + params.get("time_end") + "</time_end>"
                + "<total_fee>" + params.get("total_fee") + "</total_fee>"
                + "<trade_type>" + params.get("trade_type") + "</trade_type>"
                + "<transaction_id>" + params.get("transaction_id") + "</transaction_id>"
                + "</xml>";
    }

    private MapMessage notify(String xml, String notifyUrl) {
        StringEntity entity = new StringEntity(xml, ContentType.create("application/xml", Consts.UTF_8));
        notifyUrl = notifyUrl.replace("https://", "http://");
        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).post(notifyUrl).entity(entity).execute();
        if (null != response.getResponseString()) {
            if (response.getResponseString().contains("SUCCESS")) {
                return MapMessage.successMessage();
            }
        }

        return MapMessage.errorMessage();
    }

    /**
     * 支付页面　学豆奖励提示
     * 每天 3星最少45个
     * 15天	  675
     * 30天	  1350
     * 90天	  4050
     * 365天	  16425
     */
    private int getLearningBeans(Integer period) {
        switch (period) {
            case 15:
                return 675;
            case 30:
                return 1350;
            case 90:
                return 4050;
            case 365:
                return 16425;
            default:
                return 0;
        }
    }
}
