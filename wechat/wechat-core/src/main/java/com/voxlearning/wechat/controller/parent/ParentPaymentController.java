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

package com.voxlearning.wechat.controller.parent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.WechatInfoCode;
import com.voxlearning.wechat.context.PayConfig;
import com.voxlearning.wechat.context.WxConfig;
import com.voxlearning.wechat.controller.AbstractController;
import com.voxlearning.wechat.support.utils.WechatSignUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.utopia.payment.PaymentGateway.CallbackAction_Notify;
import static com.voxlearning.wechat.constants.WechatInfoCode.PARENT_ORDER_ALREADY_PAYD;
import static com.voxlearning.wechat.constants.WechatInfoCode.PARENT_ORDER_NOT_EXIST;

/**
 * @author Xin Xin
 * @since 11/2/15
 */
@Controller
@RequestMapping(value = "/parent/wxpay")
public class ParentPaymentController extends AbstractController {

    private static final String userOrderType = "order";

    //确认订单
    @RequestMapping(value = "/confirm.vpage", method = RequestMethod.GET)
    public String confirm(Model model) {
        String orderId = getRequestString("oid");
        try {
            if (StringUtils.isBlank(orderId)) return redirectWithMsg("参数错误", model);


            UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
            if (null == order) return infoPage(PARENT_ORDER_NOT_EXIST, model);

            if (order.getPaymentStatus() != PaymentStatus.Unpaid || order.getOrderStatus() != OrderStatus.New) {
                return infoPage(PARENT_ORDER_ALREADY_PAYD, model);
            }

            // 洛亚传说 三国 关付费 挂公告
            if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == A17ZYSPG || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == SanguoDmz
                    || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == TravelAmerica || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == PetsWar) {
                String url = ProductConfig.getMainSiteBaseUrl() + "/project/appoffline.vpage?type=" + order.getOrderProductServiceType();
                return "redirect:" + url;
            }

            // 获取VendorApps
            VendorApps vendorApps = vendorLoaderClient.loadVendor(order.getOrderProductServiceType());
            if (vendorApps == null || !vendorApps.isVisible(RuntimeMode.current().getLevel())) {
                return infoPage(PARENT_ORDER_NOT_EXIST, model);
            }

            model.addAttribute("productName", order.getProductName());
            model.addAttribute("info", vendorApps.getDescription());
            model.addAttribute("price", order.getOrderPrice().doubleValue());
            model.addAttribute("productType", order.getOrderProductServiceType());
            // 只有一个产品的时候取周期
            OrderProduct product = userOrderLoaderClient.loadOrderProductById(order.getProductId());
            if (product != null) {
                List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
                if (CollectionUtils.isNotEmpty(items) && items.size() == 1) {
                    model.addAttribute("period", items.get(0).getPeriod());
                }
            }
            model.addAttribute("orderId", orderId);
        } catch (Exception ex) {
            logger.error("Confirm order failed,orderId:{}", orderId, ex);
        }
        return "/parent/wxpay/confirm";
    }

    //学豆订单确认页
    @RequestMapping(value = "/integral_confirm.vpage", method = RequestMethod.GET)
    public String integralConfirm(Model model) {
        return redirectWithMsg("学豆购买功能已下线", model);
    }

    // 托管所订单确认页
    @RequestMapping(value = "/trustee_confirm.vpage", method = RequestMethod.GET)
    public String trusteeConfirm(Model model) {
        return redirectWithMsg("托管班功能已下线", model);
    }

    //新版托管班订单确认页 2016-1-4
    //下线:2016-04-19
    @RequestMapping(value = "/trusteecls_confirm.vpage", method = RequestMethod.GET)
    public String trusteeCfm(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    // 新版用户订单中心支付
    @RequestMapping(value = "/pay-{type}.vpage", method = RequestMethod.GET)
    public String payForType(@PathVariable String type, Model model) {
        String orderId = getRequestString("oid");
        if (StringUtils.isEmpty(orderId) || StringUtils.isBlank(type)) {
            return redirectWithMsg("无效参数", model);
        }
        if (StringUtils.isEmpty(type) || !userOrderType.equals(type)) {
            return redirectWithMsg("未知的支付类型", model);
        }
        try {
            MapMessage message = getPrepayIdForUserOrder(orderId);
            if (message.isSuccess()) {
                String prepayId = message.getInfo();
                String backUrl = message.get("backUrl").toString();
                WechatType wechatType = OrderType.chips_english.name().equals(message.get("orderType")) ? WechatType.CHIPS : WechatType.PARENT;
                WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
                PayConfig payConfig = new PayConfig(wxConfig, prepayId, backUrl);
                initModel(model, payConfig, wechatType);
                if (isPayForTest()) {
                    model.addAttribute("oid", orderId);
                }
            } else {
                if (null != message.get("err")) {
                    return infoPage((WechatInfoCode) message.get("err"), model);
                } else {
                    return infoPage(message.getInfo(), message.get("url").toString(), model);
                }
            }
        } catch (CannotAcquireLockException e) {
            //ticket过期后,加锁获取,加锁失败抛此异常
            return infoPage("调取微信接口失败,请返回重试", null, model);
        } catch (Exception ex) {
            logger.error("Generate pay params failed,oid:{},type:{}", orderId, type, ex);
            return redirectWithMsg("支付异常", model);
        }
        return "/parent/wxpay/pay";
    }

    //////////////////////////
    ///  PAY FOR TEST ////////
    //////////////////////////

    @RequestMapping(value = "payfortest-fail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payForTestFail(@RequestParam String oid) {
        String xml = generateWechatNotifyXml(oid, false);
        String notifyUrl = getNotifyUrl(oid);

        MapMessage message = notify(xml, notifyUrl);

        //较验1,返回结果得是false
        if (message.isSuccess()) return MapMessage.errorMessage();

        //较验2,检查订单状态
        UserOrder orderRecord = userOrderLoaderClient.loadUserOrder(oid);
        if (orderRecord != null) {
            if (orderRecord.getOrderStatus() != OrderStatus.New) return MapMessage.errorMessage();
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "payfortest-success.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payForTestSuccess(@RequestParam String oid) {
        String xml = generateWechatNotifyXml(oid, true);
        String notifyUrl = getNotifyUrl(oid);

        MapMessage message = notify(xml, notifyUrl);

        //较验1,返回结果得是true
        if (!message.isSuccess()) return MapMessage.errorMessage();

        UserOrder orderRecord = userOrderLoaderClient.loadUserOrder(oid);
        if (orderRecord != null) {
            if (orderRecord.getOrderStatus() != OrderStatus.Confirmed) return MapMessage.errorMessage();
        }
        String returnUrl = "/parent/ucenter/index.vpage";
        if (orderRecord != null && OrderType.micro_course == orderRecord.getOrderType()) { // 微课堂订单，支付成功跳转到详情页
            // 这个controller在华盛顿上，所以需要跳到华盛顿
            returnUrl = ProductConfig.getMainSiteBaseUrl() + "/mizar/course/courseperiod.vpage?track=WechatParent&period=" + orderRecord.getProductId();
        } else if (orderRecord != null && OrderType.chips_english == orderRecord.getOrderType()) {
            returnUrl = "/chips/order/"+ oid + "/paymentsuccess.vpage";
        }
        return MapMessage.successMessage().add("url", returnUrl);
    }

    @RequestMapping(value = "payfortest-repeat.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payForTestRepeat(@RequestParam String oid) {
        String xml = generateWechatNotifyXml(oid, true);
        String notifyUrl = getNotifyUrl(oid);

        UserOrder order = userOrderLoaderClient.loadUserOrder(oid);
        if (order != null) {
            MapMessage message = notify(xml, notifyUrl);

            //较验1,返回结果得是true
            if (!message.isSuccess()) return MapMessage.errorMessage();

            //较验2,检查订单状态
            UserOrder orderNotify = userOrderLoaderClient.loadUserOrder(oid);
            if (orderNotify.getPaymentStatus() != PaymentStatus.Paid) return MapMessage.errorMessage();

            //较验3,订单没有被更新
            if (!order.getUpdateDatetime().equals(orderNotify.getUpdateDatetime()))
                return MapMessage.errorMessage();
        }
        return MapMessage.errorMessage();
    }

    private String generateWechatNotifyXml(String orderId, boolean success) {
        Map<String, Object> params = new TreeMap<>();
        params.put("appid", ProductConfig.get(WechatType.CHIPS.getAppId()));
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

        params.put("sign", WechatSignUtils.md5Sign(params).toUpperCase());

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

    private String getNotifyUrl(String orderId) {
        String notifyUrl;
        UserOrder orderRecord = userOrderLoaderClient.loadUserOrder(orderId);
        if (null != orderRecord) {
            notifyUrl = ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order/" + PaymentConstants.PaymentGatewayName_Wechat_Chips + "-" + CallbackAction_Notify + ".vpage";
        } else {
            throw new RuntimeException("Invalid order type of " + orderId);
        }
        return notifyUrl;
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

    public static void main(String[] args) {
        System.out.println(DigestUtils.md5Hex("xxxxxxxerwerwerwerwr"));
    }
}
