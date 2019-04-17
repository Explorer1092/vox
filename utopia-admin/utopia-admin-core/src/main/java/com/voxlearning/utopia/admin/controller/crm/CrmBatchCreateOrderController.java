package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.service.business.consumer.BusinessUserOrderServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author peng.zhang.a
 * @since 16-11-14
 * 批量创建订单
 */
@Controller
@RequestMapping("/crm/batch/order")
public class CrmBatchCreateOrderController extends CrmAbstractController {

    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private UserOrderServiceClient userOrderServiceClient;
    @Inject private BusinessUserOrderServiceClient businessUserOrderServiceClient;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String batchIndex(Model model) {
        List<OrderProduct> availableProductInfoList = userOrderLoaderClient.loadAvailableProductForCrm();
        List<Map<String, Object>> infoMaps = new ArrayList<>();
        for (OrderProduct info : availableProductInfoList) {
            if (OrderProductServiceType.safeParse(info.getProductType()) == A17ZYSPG
                    || OrderProductServiceType.safeParse(info.getProductType()) == TravelAmerica
                    || OrderProductServiceType.safeParse(info.getProductType()) == SanguoDmz) {
                continue;
            }
            Map<String, Object> infoMap = new HashMap<>();
            String productName = info.getName();
            infoMap.put("productName", productName);
            infoMap.put("productServiceType", info.getProductType());
            infoMap.put("productKey", info.getId());
            infoMaps.add(infoMap);
        }
        model.addAttribute("availableProducts", infoMaps);

        return "site/batch/batchcreateorderspage";
    }


    @RequestMapping(value = "create.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage createOrder() {
        String productId = getRequestString("productKey");
        String appKey = getRequestString("appKey");
        String content = getRequestString("content");
        String externalTrade = getRequestString("externalTrade");
        String[] lines = content.split("[\n]");
        OrderProductServiceType orderProductServiceType = OrderProductServiceType.safeParse(appKey);

        if (lines.length > 3000) {
            return MapMessage.errorMessage("每次导入用户量不超过3000个");
        }
        //基本参数验证
        if (orderProductServiceType == OrderProductServiceType.Unknown) {
            return MapMessage.errorMessage("appKey错误");
        }
        if (StringUtils.isBlank(content) || StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("数据不能为空");
        }
        if (StringUtils.isBlank(externalTrade) || externalTrade.length() > 30) {
            return MapMessage.errorMessage("备注不能为空,并且长度不超过30");
        }

        //验证用户id
        Set<Long> userIds = new HashSet<>();
        for (String line : lines) {
            try {
                Long uid = Long.valueOf(line);
                userIds.add(uid);
            } catch (Exception e) {
                return MapMessage.errorMessage("userId解析错误");
            }
        }
        if (userIds.size() != lines.length) {
            return MapMessage.errorMessage("有重复用户");
        }
        //验证用户是否存在
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(userIds);
        if (studentDetailMap.values().size() != userIds.size()) {
            Long userId = userIds.stream().filter(p -> !studentDetailMap.containsKey(p)).findFirst().orElse(null);
            return MapMessage.errorMessage("找不到指定账号" + userId + "信息");
        }

        //验证班级是否存在或者是否是毕业班级
//        StudentDetail midSd = studentDetailMap.values().stream()
//                .filter(p -> p.getClazz() == null
//                        || p.getClazz().isTerminalClazz()
//                        || SafeConverter.toInt(p.getClazzLevelAsInteger(), 0) <= 0
//                        || SafeConverter.toInt(p.getClazzLevelAsInteger(), 0) > 6)
//                .findFirst()
//                .orElse(null);
//        if (midSd != null) {
//            return MapMessage.errorMessage("包含已经毕业的用户,或者找不到指定班级userId=" + midSd.getId());
//        }

        //加载学校信息
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        int successSum = 0;
        for (Long userId : userIds) {
            try {
                StudentDetail studentDetail = studentDetailMap.get(userId);
                UserOrder afentiOrder = UserOrder.newOrder(OrderType.app, studentDetail.getId());
                if (orderProductServiceType == PicListen || orderProductServiceType == PicListenBook || orderProductServiceType == FollowRead) {
                    afentiOrder.setOrderType(OrderType.pic_listen);
                }
                afentiOrder.setProductId(productId);
                afentiOrder.setProductName(product.getName());
                afentiOrder.setUserId(studentDetail.getId());
                afentiOrder.setUserName(studentDetail.getProfile().getRealname());
                afentiOrder.setOrderProductServiceType(product.getProductType());
                afentiOrder.setProductAttributes(product.getAttributes());
                afentiOrder.setOrderPrice(product.getPrice());
                afentiOrder.setOrderReferer("crm-batch");
                afentiOrder.setComment(externalTrade);
                MapMessage message = userOrderServiceClient.saveUserOrder(afentiOrder);
                if (!message.isSuccess()) {
                    return message.add("successSum", successSum)
                            .add("errorUserId", userId);
                }

                /* ****************模拟支付*****************/

                //模拟支付
                PaymentCallbackContext paymentCallbackContext = new PaymentCallbackContext("manually", PaymentGateway.CallbackAction_Notify);
                paymentCallbackContext.setVerifiedPaymentData(new PaymentVerifiedData());
                paymentCallbackContext.getVerifiedPaymentData().setExternalTradeNumber(externalTrade);
                paymentCallbackContext.getVerifiedPaymentData().setExternalUserId("");
                paymentCallbackContext.getVerifiedPaymentData().setPayAmount(BigDecimal.ZERO);
                paymentCallbackContext.getVerifiedPaymentData().setTradeNumber(afentiOrder.genUserOrderId());
                businessUserOrderServiceClient.processUserOrderPayment(paymentCallbackContext);
                successSum++;
            } catch (Exception e) {
                return MapMessage.errorMessage("创建有异常,成功数量,sum=" + successSum + ",错误用户userId=" + userId);
            }
        }
        String message = "成功给" + successSum + "位用户创建好(" + orderProductServiceType.name() + ")订单";
        return MapMessage.successMessage().set("sum", successSum)
                .set("message", message);
    }

}

