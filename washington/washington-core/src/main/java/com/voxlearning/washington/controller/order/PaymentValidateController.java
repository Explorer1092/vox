package com.voxlearning.washington.controller.order;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderFaceDetectRecord;
import com.voxlearning.utopia.service.order.consumer.UserOrderFaceDetectRecordLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderFaceDetectRecordServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.DPUserService;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.ApiConstants.REQ_APP_KEY;

/**
 * describe:
 *
 * @author yong.liu
 * @date 2019/01/21
 */

@Controller
@RequestMapping(value = "/payment/validate")
@Slf4j
public class PaymentValidateController extends AbstractController {

    @Inject private UserOrderFaceDetectRecordLoaderClient userOrderFaceDetectRecordLoaderClient;
    @Inject private UserOrderFaceDetectRecordServiceClient userOrderFaceDetectRecordServiceClient;
    @Inject private FinanceServiceClient financeServiceClient;
    @Inject private CouponLoaderClient couponLoaderClient;

    @ImportService(interfaceClass = DPUserService.class)
    private DPUserService dpUserService;

    @RequestMapping(value = "/mobile/faceRecognition.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage faceRecognition(String source, String imgUrls) {
        MapMessage result = new MapMessage();
        try {
            User user = currentUser();
            if (Objects.isNull(user)) {
                result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                result.add(RES_MESSAGE, "用户未登录");
                return result;
            }

            String upSource = StringUtils.upperCase(source);
            if(StringUtils.isBlank(upSource) || StringUtils.isEmpty(imgUrls)){
                result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                result.add(RES_MESSAGE, "参数错误");
                return result;
            }

            if(!Objects.equals(upSource,"ORDER") && !Objects.equals(upSource,"FINANCE") && !Objects.equals(upSource,"REWARD")){
                result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                result.add(RES_MESSAGE, "来源错误");
                return result;
            }

            List<String> imgUrlList = JsonUtils.fromJsonToList(imgUrls,String.class);
            if(CollectionUtils.isEmpty(imgUrlList)){
                result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                result.add(RES_MESSAGE, "参数错误");
                return result;
            }

            String orderId = getRequestString("orderId");
            if(Objects.equals("ORDER",upSource)){
                if(StringUtils.isBlank(orderId)){
                    result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    result.add(RES_MESSAGE, "订单号为空");
                    return result;
                }

                UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
                if(Objects.isNull(order)){
                    result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    result.add(RES_MESSAGE, "订单号错误");
                    return result;
                }

                if(order.getPaymentStatus() == PaymentStatus.Paid){
                    result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    result.add(RES_MESSAGE, "订单已支付");
                    return result;
                }
            }else if(Objects.equals("FINANCE",upSource)){
                FinanceFlow financeFlow = financeServiceClient.getFinanceService().loadFinanceFlow(orderId).getUninterruptibly();
                if(Objects.isNull(financeFlow)){
                    result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    result.add(RES_MESSAGE, "订单号错误");
                    return result;
                }

                if(Objects.equals(financeFlow.getState(), FinanceFlowState.SUCCESS.name())){
                    result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    result.add(RES_MESSAGE, "订单已支付");
                    return result;
                }
            }else{
                //其他的业务，如果没说明不做特定的校验
            }

            MapMessage valRes = userOrderFaceDetectRecordServiceClient.faceRecognition(source, orderId, user.getId(), imgUrlList);
            if(valRes.isSuccess()) {
                result.add(RES_RESULT, RES_RESULT_SUCCESS);
            }else{
                result.add(RES_RESULT,RES_RESULT_BAD_REQUEST_CODE);
            }
            result.add("code",valRes.get("code"));
            result.add(RES_MESSAGE,valRes.get("info"));
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            result.add(RES_MESSAGE, "人脸识别接口调用异常");
            return result;
        }
    }

    @RequestMapping(value = "/mobile/allowPay.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage payValidate(String source, String couponRefId) {
        try {
            User user = currentUser();
            if (Objects.isNull(user)) {
                return MapMessage.errorMessage("用户未登录");
            }
            String orderId = getRequestString("orderId");
            String upSource = StringUtils.upperCase(source);
            if(StringUtils.isBlank(upSource) || StringUtils.isBlank(orderId)){
                return MapMessage.errorMessage("参数错误");
            }

            if(!(Objects.equals(upSource,"ORDER")||Objects.equals(upSource,"FINANCE"))){
                return MapMessage.errorMessage("参数错误");
            }

            CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(couponRefId);
            MapMessage result = null;
            if(Objects.nonNull(couponUserRef)){
                result = userOrderFaceDetectRecordServiceClient.payValidate(upSource ,orderId,user.getId(),couponRefId);
            }else{
                result = userOrderFaceDetectRecordServiceClient.payValidate(upSource ,orderId,user.getId());
            }
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }

    //首次弹人脸识别校验接口
    @RequestMapping(value = "/mobile/popFaceRecognition.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage popFaceRecognition() {
        try {
            User user = currentUser();
            if (null == user) {
                return MapMessage.errorMessage("用户未登录");
            }
            //用户设置过密码，且用户没有人脸识别的记录
            boolean passwordExist = dpUserService.isSetPaymentPassword(user.getId());
            List<UserOrderFaceDetectRecord> records = userOrderFaceDetectRecordLoaderClient.loadUserOrderFaceDetectRecordsByUserId(user.getId());
            if(passwordExist && CollectionUtils.isEmpty(records)){
                return MapMessage.successMessage().set("popFace",true);
            }else{
                return MapMessage.successMessage().set("popFace",false);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "/mobile/skipValidate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage skipValidate(String orderId, String couponRefId) {
        try {
            User user = currentUser();
            if (Objects.isNull(user)) {
                return MapMessage.errorMessage("用户未登录");
            }

            if(StringUtils.isBlank(orderId) || StringUtils.isBlank(couponRefId)){
                return MapMessage.errorMessage("参数错误");
            }

            CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(couponRefId);
            if(Objects.isNull(couponUserRef)){
                return MapMessage.errorMessage("参数错误");
            }

            UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
            if(Objects.isNull(userOrder)){
                return MapMessage.errorMessage("参数错误");
            }
            userOrder.setCouponRefId(couponRefId);
            BigDecimal reallyAmount = userOrderServiceClient.getOrderCouponDiscountPrice(userOrder);
            if(BigDecimal.ZERO.compareTo(reallyAmount) == 0){
                return MapMessage.successMessage().set("skipValidate",true);
            }else{
                return MapMessage.successMessage().set("skipValidate",false);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }
}
