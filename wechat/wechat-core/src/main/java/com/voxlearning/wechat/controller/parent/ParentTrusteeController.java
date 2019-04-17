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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.o2o.TrusteeOrderRecord;
import com.voxlearning.utopia.service.order.consumer.TrusteeOrderServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.wechat.controller.AbstractParentWebController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2015/11/9.
 */
@Controller
@RequestMapping(value = "/parent/trustee")
public class ParentTrusteeController extends AbstractParentWebController {
    @Inject private TrusteeOrderServiceClient trusteeOrderServiceClient;


    private static String getTrusteeVerifyCodeMemKey(UserType userType, String userCode) {
        return SmsType.PARENT_TRUSTEE_SEND_VERIFY_CODE + "_" + userType.getType() + "_" + userCode;
    }

    // 线上1分钱测试家长ID账号
    private static final List<Long> freeParentIds = Arrays.asList(27398018L, 27398020L, 27398022L, 27398025L, 27398027L, 27398030L,
            27398035L, 27398036L, 27398040L, 27398042L, 27398041L, 27398038L, 27398029L, 27398032L, 27398047L, 27398049L,
            27398054L, 27398055L);

    //托管班首页
    //下线:2016-04-19
    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    //托管班首页--查询托管班列表及banner列表
    //下线:2016-04-19
    @RequestMapping(value = "/loadbranchs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadBranchs(@RequestParam Long studentId) {
        return MapMessage.errorMessage();
    }

    //托管班详情页
    //下线:2016-04-19
    @RequestMapping(value = "/branchdetail.vpage", method = RequestMethod.GET)
    public String branchDetail(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    //托管班相册
    //下线:2016-04-19
    @RequestMapping(value = "/branchalbum.vpage", method = RequestMethod.GET)
    public String branchAlbum(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    //托管班--下订单
    //下线:2016-04-19
    @RequestMapping(value = "/createorder.vpage", method = RequestMethod.GET)
    public String createOrder(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    //托管班--生成订单
    //下线:2016-04-19
    @RequestMapping(value = "/createorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createOrder_Post(@RequestParam Long studentId, @RequestParam Long goodsId, @RequestParam Long branchId, @RequestParam Integer count) {
        return MapMessage.errorMessage("生成订单失败");
    }

    //托管班--退款
    //下线:2016-04-19
    @RequestMapping(value = "/refund.vpage", method = RequestMethod.GET)
    public String refund(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    /**
     * 发起退款申请
     *
     * @param orderId    订单编号
     * @param reasonId   退款原因编号
     * @param refundDesc 退款理由
     * @return
     */
    //下线:2016-04-19
    @RequestMapping(value = "/refund.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refund_Post(@RequestParam String orderId, @RequestParam Long reasonId, @RequestParam String refundDesc) {
        return MapMessage.errorMessage("退款失败");
    }

    //下线:2016-04-19
    @RequestMapping(value = "/order/detail.vpage", method = RequestMethod.GET)
    public String refundDetail(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    //下线:2016-04-19
    @RequestMapping(value = "/order/detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getOrderDetail(@RequestParam String orderId) {
        return MapMessage.errorMessage("查询订单详情失败");
    }

    //托管班--订单列表
    //下线:2016-04-19
    @RequestMapping(value = "/orderlist.vpage", method = RequestMethod.GET)
    public String orderList(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    //托管班--订单详情
    //下线:2016-04-19
    @RequestMapping(value = "/orderdetail.vpage", method = RequestMethod.GET)
    public String orderDetail(Model model) {
        return "/parent/mytrustee/orderdetail";
    }

    //托管班--倒计时活动页
    //下线:2016-04-19
    @RequestMapping(value = "/countdown.vpage", method = RequestMethod.GET)
    public String countDown(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    //托管班--法律申明
    //下线:2016-04-19
    @RequestMapping(value = "/lawstates.vpage", method = RequestMethod.GET)
    public String lawStates(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    /**************************************************
     * 以下为实验部分 除了夏令营的在用生成订单和支付接口意外， 其他的都下线了。
     ********************************************************/

    // 活动介绍页面
    @RequestMapping(value = "/present.vpage", method = RequestMethod.GET)
    public String present(Model model) {
        // 跳转热门活动页 已下线
        return "redirect:/parent/activity/list.vpage";

//        Integer shopId = getRequestInt("shopId");
//        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//        if (shopId == 0 || shop == null) {
//            // 跳转首页
//            return "redirect:/parent/activity/list.vpage";
//        }
//        // 报名成功 直接跳转到预约成功页面
//        Long parentId = getRequestContext().getUserId();
//        TrusteeReserveRecord reserveRecord = parentServiceClient.loadTrusteeReserveByParentId(parentId, shopId);
//        if (reserveRecord != null && reserveRecord.getStatus() == TrusteeReserveRecord.Status.Success) {
//            return "redirect:/parent/trustee/reservesuccess.vpage?shopId=" + shopId;
//        }
//        // 找到的话 跳转到对应的活动介绍
//        model.addAttribute("shop", shop);
//        // 获取报名的人数以及学校名称
//        List<TrusteeReserveRecord> reserveRecords = parentServiceClient.loadTrusteeReservesByShopId(shopId);
//        if (CollectionUtils.isNotEmpty(reserveRecords)) {
//            reserveRecords = reserveRecords.stream().filter(d -> d.getStatus() == TrusteeReserveRecord.Status.Success).collect(Collectors.toList());
//            model.addAttribute("reserveCount", reserveRecords.size());
//            // 学校名称
//            String[] schoolIds = StringUtils.split(shop.getSchoolIds(), ",");
//            if (schoolIds.length > 1) {
//                model.addAttribute("schoolName", "附近学校");
//            } else {
//                Long schoolId = SafeConverter.toLong(schoolIds[0]);
//                School school = schoolLoaderClient.loadSchool(schoolId);
//                if (school != null) {
//                    model.addAttribute("schoolName", school.getShortName());
//                }
//            }
//        }
        // 公开课的要判断截止日期
//        if (StringUtils.equals(shop.getType(), "openclass")) {
//            // 判断截止时间
//            Date endDate = DateUtils.stringToDate(shop.getEndDate());
//            if (new Date().after(endDate)) {
//                model.addAttribute("endFlag", true);
//            }
//            // 显示报名日期
//            String beginDateStr = DateUtils.dateToString(DateUtils.stringToDate(shop.getBeginDate()), "MM.dd");
//            String endDateStr = DateUtils.dateToString(endDate, "MM.dd");
//            model.addAttribute("beginDateStr", beginDateStr);
//            model.addAttribute("endDateStr", endDateStr);
//        }
//        return "/parent/" + shop.getType() + "/present";
    }

    @RequestMapping(value = "/ruledesc.vpage", method = RequestMethod.GET)
    public String ruledesc(Model model) {
        return "redirect:/parent/activity/list.vpage";
//        Integer shopId = getRequestInt("shopId");
//        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//        if (shopId == 0 || shop == null) {
//            // 跳转首页
//            return "redirect:/parent/activity/list.vpage";
//        }
//        model.addAttribute("shopId", shopId);
//        return "/parent/" + shop.getType() + "/ruledesc";
    }


    // 家长点击模板消息， 跳转到预约页面
    @RequestMapping(value = "/reserve.vpage", method = RequestMethod.GET)
    public String reserve(Model model) {
        return "redirect:/parent/activity/list.vpage";
    }

    // 机构介绍
    @RequestMapping(value = "/detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        return "redirect:/parent/activity/list.vpage";
//        Integer shopId = getRequestInt("shopId");
//        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//        if (shopId == 0 || shop == null) {
//            // 跳转首页
//            return "redirect:/parent/activity/list.vpage";
//        }
//        // 公开课介绍页需要显示多少人已经报名了
//        if (StringUtils.equals(shop.getType(), "openclass")) {
//            List<TrusteeReserveRecord> reserveRecords = parentServiceClient.loadTrusteeReservesByShopId(shopId);
//            if (CollectionUtils.isNotEmpty(reserveRecords)) {
//                reserveRecords = reserveRecords.stream().filter(d -> d.getStatus() == TrusteeReserveRecord.Status.Success).collect(Collectors.toList());
//                model.addAttribute("reserveCount", reserveRecords.size());
//            }
//        }
//        model.addAttribute("shopId", shopId);
//        return "/parent/" + shop.getType() + "/detail";
    }

    // 公开课老师介绍
    @RequestMapping(value = "/teacherinfo.vpage", method = RequestMethod.GET)
    public String teacherInfo(Model model) {
        return "redirect:/parent/activity/list.vpage";
//        Integer shopId = getRequestInt("shopId");
//        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//        if (shopId == 0 || shop == null || !StringUtils.equals(shop.getType(), "openclass")) {
//            // 跳转首页
//            return "redirect:/parent/activity/list.vpage";
//        }
//        model.addAttribute("shopId", shopId);
//        return "/parent/openclass/teacherinfo";
    }


    // 预约成功页
    @RequestMapping(value = "/reservesuccess.vpage", method = RequestMethod.GET)
    public String signPic(Model model) {
        return "redirect:/parent/activity/list.vpage";
//        // 获取用户预约记录
//        Integer shopId = getRequestInt("shopId");
//        Long parentId = getRequestContext().getUserId();
//        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//        if (shopId == 0 || shop == null) {
//            // 跳转首页
//            return "redirect:/parent/activity/list.vpage";
//        }
//        TrusteeReserveRecord reserveRecord = parentServiceClient.loadTrusteeReserveByParentId(parentId, shopId);
//        if (reserveRecord == null || reserveRecord.getStatus() != TrusteeReserveRecord.Status.Success) {
//            return "redirect:/parent/trustee/reserve.vpage";
//        }
//        model.addAttribute("shop", shop);
//        return "/parent/" + shop.getType() + "/reservesuccess";
    }

    // 跳转到预约支付页面
    @RequestMapping(value = "/reservepay.vpage", method = RequestMethod.GET)
    public String reservePay(Model model) {
        return "redirect:/parent/activity/list.vpage";
//        try {
//            Integer shopId = getRequestInt("shopId");
//            Long parentId = getRequestContext().getUserId();
//            TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//            if (shopId == 0 || shop == null) {
//                // 跳转首页
//                return "redirect:/parent/activity/list.vpage";
//            }
//            List<TrusteeType> types = TrusteeType.getByTrusteeShopId(shopId);
//            if (CollectionUtils.isEmpty(types)) {
//                return "redirect:/parent/trustee/reserve.vpage?shopId=" + shop.getShopId();
//            }
//            // 直接去支付页面
//            TrusteeType reserveType = types.stream().filter(t -> StringUtils.equals(t.getSkuType(), "reserve")).findFirst().orElse(null);
//            // 生成订单
//            List<TrusteeOrderRecord> orderRecordList = parentServiceClient.loadTrusteeOrderByParentId(parentId);
//            TrusteeOrderRecord record = orderRecordList.stream().filter(o -> StringUtils.equals(o.getTrusteeType().getSkuType(), "reserve"))
//                    .filter(o -> Objects.equals(shopId, o.getTrusteeType().getTrusteeShopId())).findFirst().orElse(null);
//            if (record != null) {
//                if (record.getStatus() == TrusteeOrderRecord.Status.Paid) {
//                    return redirectWithMsg("预约订单已支付", model);
//                } else {
//                    return "redirect:/parent/wxpay/pay-trustee.vpage?oid=" + record.getId();
//                }
//            }
//            TrusteeOrderRecord orderRecord = TrusteeOrderRecord.newOrder();
//            orderRecord.setParentId(getRequestContext().getUserId());
//            // 1分钱测试
//            if (RuntimeMode.ge(Mode.PRODUCTION) && freeParentIds.contains(parentId)) {
//                orderRecord.setPrice(new BigDecimal(0.01));
//            } else {
//                orderRecord.setPrice(new BigDecimal(reserveType.getDiscountPrice()));
//            }
//            orderRecord.setPayMethod("wechatpay");
//            orderRecord.setStudentId(0L);
//            orderRecord.setTrusteeType(reserveType);
//            MapMessage message = parentServiceClient.saveTrusteeOrder(orderRecord);
//            if (message.isSuccess()) {
//                return "redirect:/parent/wxpay/pay-trustee.vpage?oid=" + message.get("id");
//            } else {
//                return redirectWithMsg("生成订单失败", model);
//            }
//        } catch (Exception ex) {
//            logger.error("pay trustee reserve order fail, error is {}", ex.getMessage());
//            return redirectWithMsg("生成订单失败", model);
//        }
    }

    @RequestMapping(value = "/order.vpage", method = RequestMethod.POST)
    public String orderPost(Model model) {
        return redirectWithMsg("活动已结束", model);
//        String type = getRequestString("trusteeType");
//        Long studentId = getRequestLong("sid");
//        Long parentId = getRequestContext().getUserId();
//        TrusteeType trusteeType = TrusteeType.valueOf(type);
//        if (trusteeType == null) {
//            return redirectWithMsg("生成订单失败", model);
//        }
//        TrusteeShop shop = TrusteeShop.getByShopId(trusteeType.getTrusteeShopId());
//        if (shop == null) {
//            return redirectWithMsg("生成订单失败", model);
//        }
//        try {
//            List<TrusteeOrderRecord> orderRecordList = parentServiceClient.loadTrusteeOrderByParentId(parentId);
//            // 看看该类型是否有未支付订单， 如果有，直接用现有的订单
//            TrusteeOrderRecord record = orderRecordList.stream().filter(o -> o.getTrusteeType() == trusteeType)
//                    .filter(o -> Objects.equals(o.getStudentId(), studentId))
//                    .filter(o -> o.getStatus() == TrusteeOrderRecord.Status.New).findFirst().orElse(null);
//            if (record != null) {
//                return "redirect:/parent/wxpay/trustee_confirm.vpage?oid=" + record.getId();
//            } else {
//                TrusteeOrderRecord orderRecord = TrusteeOrderRecord.newOrder();
//                orderRecord.setParentId(getRequestContext().getUserId());
//                // 1分钱测试
//                if (RuntimeMode.ge(Mode.PRODUCTION) && freeParentIds.contains(parentId)) {
//                    orderRecord.setPrice(new BigDecimal(0.01));
//                } else {
//                    // 对价格做处理
//                    String discountDate = shop.getDiscountDate();
//                    if (StringUtils.isNotBlank(discountDate) && DateUtils.stringToDate(discountDate).before(new Date())) {
//                        orderRecord.setPrice(new BigDecimal(trusteeType.getPrice()));
//                    } else {
//                        orderRecord.setPrice(new BigDecimal(trusteeType.getDiscountPrice()));
//                    }
//                }
//                orderRecord.setPayMethod("wechatpay");
//                orderRecord.setStudentId(studentId);
//                orderRecord.setTrusteeType(trusteeType);
//                MapMessage message = parentServiceClient.saveTrusteeOrder(orderRecord);
//                if (message.isSuccess()) {
//                    return "redirect:/parent/wxpay/trustee_confirm.vpage?oid=" + message.get("id");
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("Create trustee order failed,parentId:{},studentId:{},trusteeType:{}", getRequestContext().getUserId(), studentId, trusteeType.name(), ex);
//        }
//        return redirectWithMsg("生成订单失败", model);
    }

    // order for app
    @RequestMapping(value = "/orderforapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage orderForApp() {
        return MapMessage.errorMessage("接口已经停用");

//        String type = getRequestString("trusteeType");
//        Long studentId = getRequestLong("sid");
//        Long parentId = getRequestContext().getUserId();
//        TrusteeType trusteeType = TrusteeType.valueOf(type);
//        if (trusteeType == null) {
//            return MapMessage.errorMessage("生成订单失败");
//        }
//        TrusteeShop shop = TrusteeShop.getByShopId(trusteeType.getTrusteeShopId());
//        if (shop == null) {
//            return MapMessage.errorMessage("生成订单失败");
//        }
//        try {
//            List<TrusteeOrderRecord> orderRecordList = parentServiceClient.loadTrusteeOrderByParentId(parentId);
//            // 看看该类型是否有未支付订单， 如果有，直接用现有的订单
//            TrusteeOrderRecord record = orderRecordList.stream().filter(o -> o.getTrusteeType() == trusteeType)
//                    .filter(o -> Objects.equals(o.getStudentId(), studentId))
//                    .filter(o -> o.getStatus() == TrusteeOrderRecord.Status.New).findFirst().orElse(null);
//            if (record != null) {
//                return MapMessage.successMessage().add("orderId", record.getId());
//            } else {
//                TrusteeOrderRecord orderRecord = TrusteeOrderRecord.newOrder();
//                orderRecord.setParentId(getRequestContext().getUserId());
//                // 1分钱测试
//                if (RuntimeMode.ge(Mode.PRODUCTION) && freeParentIds.contains(parentId)) {
//                    orderRecord.setPrice(new BigDecimal(0.01));
//                } else {
//                    // 对价格做处理
//                    String discountDate = shop.getDiscountDate();
//                    if (StringUtils.isNotBlank(discountDate) && DateUtils.stringToDate(discountDate).before(new Date())) {
//                        orderRecord.setPrice(new BigDecimal(trusteeType.getPrice()));
//                    } else {
//                        orderRecord.setPrice(new BigDecimal(trusteeType.getDiscountPrice()));
//                    }
//                }
//                orderRecord.setPayMethod("wechat_parent");
//                orderRecord.setStudentId(studentId);
//                orderRecord.setTrusteeType(trusteeType);
//                MapMessage message = parentServiceClient.saveTrusteeOrder(orderRecord);
//                if (message.isSuccess()) {
//                    return MapMessage.successMessage().add("orderId", message.get("id"));
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("Create trustee order failed,parentId:{},studentId:{},trusteeType:{}", getRequestContext().getUserId(), studentId, trusteeType.name(), ex);
//        }
//        return MapMessage.errorMessage("生成订单失败");
    }

    // 跳转到选择产品支付页面
    @RequestMapping(value = "/skupay.vpage", method = RequestMethod.GET)
    public String skuPay(Model model) {
        return redirectWithMsg("活动已结束", model);
//        // 根据当前用户的属性 判断展示的商品类型
//        Long parentId = getRequestContext().getUserId();
//        Integer shopId = getRequestInt("shopId");
//        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//        if (shopId == 0 || shop == null) {
//            // 跳转首页
//            return "redirect:/parent/activity/list.vpage";
//        }
////        if (!StringUtils.equals("wintercamp", shop.getType()) && !StringUtils.equals("summercamp", shop.getType())) {
////            TrusteeReserveRecord reserveRecord = parentServiceClient.loadTrusteeReserveByParentId(parentId, shopId);
////            if (reserveRecord == null || reserveRecord.getStatus() != TrusteeReserveRecord.Status.Success) {
////                // 没有预约成功 不允许支付 返回预约页面
////                return "redirect:/parent/trustee/reserve.vpage?shopId=" + shop.getShopId();
////            }
////        }
//        // 以及家长孩子
//        List<User> students = studentLoaderClient.loadParentStudents(parentId);
//        if (students.size() == 0) {
//            //跳去绑学生页面
//            return "redirect:/parent/ucenter/bindchild.vpage";
//        }
//        // 获取可购买的列表信息
//        List<TrusteeType> trusteeTypes = TrusteeType.getByTrusteeShopId(shopId);
//        trusteeTypes = trusteeTypes.stream().filter(t -> StringUtils.equals(t.getSkuType(), "buy")).collect(Collectors.toList());
//        // 转化为前端展示价格
//        model.addAttribute("trusteeTypes", convertSkuMap(shop, trusteeTypes));
//        List<Map<String, Object>> stdInfos = new ArrayList<>();
//        students.stream().forEach(t -> {
//            Map<String, Object> infoMap = new HashMap<>();
//            infoMap.put("id", t.getId());
//            infoMap.put("name", t.getProfile().getRealname());
//            infoMap.put("img", t.getProfile().getImgUrl());
//            stdInfos.add(infoMap);
//        });
//        model.addAttribute("students", stdInfos);
//        model.addAttribute("shop", shop);
//        return "/parent/" + shop.getType() + "/skupay";
    }

//    private List<Map<String, Object>> convertSkuMap(TrusteeShop shop, List<TrusteeType> trusteeTypes) {
//        if (shop == null || CollectionUtils.isEmpty(trusteeTypes)) {
//            return Collections.emptyList();
//        }
//        List<Map<String, Object>> skuList = new ArrayList<>();
//        // 对价格做处理
//        String discountDate = shop.getDiscountDate();
//        boolean isDiscount = true;
//        if (StringUtils.isNotBlank(discountDate) && DateUtils.stringToDate(discountDate).before(new Date())) {
//            isDiscount = false;
//        }
//        for (TrusteeType type : trusteeTypes) {
//            Map<String, Object> skuMap = new MapMessage();
//            skuMap.put("name", type.name());
//            skuMap.put("description", type.getDescription());
//            skuMap.put("price", type.getPrice());
//            if (isDiscount) {
//                skuMap.put("discountPrice", type.getDiscountPrice());
//            } else {
//                skuMap.put("discountPrice", type.getPrice());
//            }
//            skuMap.put("trusteeShopId", type.getTrusteeShopId());
//            skuMap.put("skuType", type.getSkuType());
//            skuList.add(skuMap);
//        }
//        return skuList;
//    }

    // 获取学生购买过的信息
    @RequestMapping(value = "loadstudentorders.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadStudentOrders() {
        Long studentId = getRequestLong("studentId");
        List<TrusteeOrderRecord> orderRecords = trusteeOrderServiceClient.loadTrusteeOrderByStudentId(studentId);
        orderRecords = orderRecords.stream().filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid)
                .filter(o -> StringUtils.equals(o.getTrusteeType().getSkuType(), "buy"))
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("orderRecords", orderRecords);
    }

    //法律声明
    @RequestMapping(value = "/legalnotice.vpage", method = RequestMethod.GET)
    public String legalnotice(Model model) {
        return "/parent/trustee/legalnotice";
    }

    // 验证码验证并生成预约记录
    @RequestMapping(value = "verifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCode() {
        return MapMessage.errorMessage("活动已结束");
//        String mobile = getRequestString("mobile");
//        String code = getRequestString("code");
//        String email = getRequestString("email");
//        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code)) {
//            return MapMessage.errorMessage("请填写正确的验证码");
//        }
//        if (!MobileRule.isMobile(mobile)) {
//            return MapMessage.errorMessage("手机号不正确");
//        }
//        Integer shopId = getRequestInt("shopId");
//        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//        if (shopId == 0 || shop == null) {
//            return MapMessage.errorMessage("机构不存在");
//        }
//        // 从缓存中获取验证码并验证
//        String cacheKey = getTrusteeVerifyCodeMemKey(UserType.PARENT, mobile);
//        String cachedVerifyCode = wechatWebCacheSystem.CBS.flushable.load(cacheKey);
//        if (StringUtils.isBlank(cachedVerifyCode)) {
//            return MapMessage.errorMessage("验证码失效，请重新获取验证码");
//        }
//        String[] verifyCodes = cachedVerifyCode.split("_");
//        String verifyCode = verifyCodes[0];
//        if (StringUtils.isEmpty(verifyCode) || !verifyCode.equals(code)) {
//            return MapMessage.errorMessage("验证码失效，请重新获取验证码");
//        }
//        String trackId = verifyCodes[1];
//        if (StringUtils.isNotBlank(trackId)) {
//            smsServiceClient.consumeVerificationCode(trackId);
//        }
//        // 清除缓存中验证码
//        wechatWebCacheSystem.CBS.flushable.delete(cacheKey);
//        // 验证通过 生成预约单
//        TrusteeReserveRecord reserveRecord = parentServiceClient.loadTrusteeReserveByParentId(getRequestContext().getUserId(), shopId);
//        if (reserveRecord != null) {
//            return MapMessage.successMessage();
//        } else {
//            reserveRecord = new TrusteeReserveRecord();
//            reserveRecord.setSensitiveMobile(sensitiveUserDataServiceClient.encodeMobile(mobile));
//            reserveRecord.setSensitiveEmail(sensitiveUserDataServiceClient.encodeEmail(email));
//            reserveRecord.setNeedPay(true);
//            reserveRecord.setParentId(getRequestContext().getUserId());
//            reserveRecord.setStatus(TrusteeReserveRecord.Status.New);
//            reserveRecord.setTrusteeId(shop.getShopId());
//            parentServiceClient.saveReserveRecord(reserveRecord);
//        }
//        return MapMessage.successMessage();
    }

    // 直接预约 生成预约单  公开课ABTEST
    @RequestMapping(value = "reservewithoutcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reserveWithoutCode() {
        return MapMessage.errorMessage("活动已结束");
//        String mobile = getRequestString("mobile");
//        Integer shopId = getRequestInt("shopId");
//        if (!MobileRule.isMobile(mobile)) {
//            return MapMessage.errorMessage("手机号不正确");
//        }
//        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//        if (shopId == 0 || shop == null) {
//            return MapMessage.errorMessage("机构不存在");
//        }
//        TrusteeReserveRecord reserveRecord = parentServiceClient.loadTrusteeReserveByParentId(getRequestContext().getUserId(), shopId);
//        if (reserveRecord != null) {
//            return MapMessage.successMessage();
//        } else {
//            reserveRecord = new TrusteeReserveRecord();
//            reserveRecord.setSensitiveMobile(sensitiveUserDataServiceClient.encodeMobile(mobile));
//            reserveRecord.setNeedPay(true);
//            reserveRecord.setParentId(getRequestContext().getUserId());
//            reserveRecord.setStatus(TrusteeReserveRecord.Status.New);
//            reserveRecord.setTrusteeId(shop.getShopId());
//            parentServiceClient.saveReserveRecord(reserveRecord);
//        }
//        return MapMessage.successMessage();
    }

    // 发送验证码
    @RequestMapping(value = "/sendtrusteesmscode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendTrusteeSmsCode() {
        return MapMessage.errorMessage("活动已结束");
    }

    /*冬令营实验专题页*/
    @RequestMapping(value = "wintercamp/detail.vpage", method = RequestMethod.GET)
    public String winterCampDetail(Model model) {
        return "redirect:/parent/activity/list.vpage";
//        Integer shopId = getRequestInt("shopId");
//        TrusteeShop shop = TrusteeShop.getByShopId(shopId);
//        if (shopId == 0 || shop == null) {
//            // 跳转首页
//            return "redirect:/parent/activity/list.vpage";
//        }
//        model.addAttribute("shopId", shopId);
//        return "/parent/wintercamp/detail";
    }

    /*冬令营实验常见问题*/
    @RequestMapping(value = "wintercamp/question.vpage", method = RequestMethod.GET)
    public String winterCampQuestion(Model model) {
        return "/parent/wintercamp/question";
    }
}
