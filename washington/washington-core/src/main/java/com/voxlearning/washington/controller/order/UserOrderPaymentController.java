package com.voxlearning.washington.controller.order;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.appalachian.org.apache.zookeeper.server.SessionTracker;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.FairylandProductService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.washington.support.AbstractOrderPaymentController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

@Controller
@RequestMapping(value = "/apps/orderpayment")
@Slf4j
public class UserOrderPaymentController extends AbstractOrderPaymentController {

    @Inject
    private AsyncVendorServiceClient asyncVendorServiceClient;

    /**
     * 移动端订单支付---进入支付确认页
     */
    @RequestMapping(value = "/mobile/confirm.vpage", method = RequestMethod.GET)
    public String mobileConfirm(Model model) {
        try {
            validateRequired(REQ_ORDER_ID, "订单号");

            User student = currentStudent();
            if (null == student){
                model.addAttribute("error", "请使用学生帐号登录");
                return "/paymentmobile/confirm";
            }

            UserOrder order = userOrderLoaderClient.loadUserOrder(getRequestString(REQ_ORDER_ID));
            if (null == order || !order.getOrderToken().equals(getRequestString(REQ_ORDER_TOKEN))) {
                model.addAttribute("error", "订单数据错误");
                return "/paymentmobile/confirm";
            }
            logger.info("登陆用户实体,{},用户订单实体,{}",JsonUtils.toJson(student),JsonUtils.toJson(order));

            if(!Objects.equals(student.getId(),order.getUserId())){
                model.addAttribute("error", "登录用户与下单用户不匹配");
                return "/paymentmobile/confirm";
            }

            if (order.getPaymentStatus() == PaymentStatus.Paid) {
                model.addAttribute("error", "这个订单已经被支付过了");
                return "/paymentmobile/confirm";
            }

            //毕业学生不能购买产品
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(student.getId());
            if (studentDetail != null && studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()) {
                model.addAttribute("error", "本产品不提供毕业班购买");
                return "/paymentmobile/confirm";
            }

            //以下为confirm页面必选参数
            model.addAttribute("orderId", getRequestString(REQ_ORDER_ID));
            model.addAttribute("showOrderId", order.getId());// 展示的orderId
            model.addAttribute("productName", order.getProductName());
            model.addAttribute("amount", order.getOrderPrice());
            //以下为confirm页面可选参数,只在apps订单才有
            model.addAttribute("orderSeq", order.getOrderSeq());
            model.addAttribute("orderToken", order.getOrderToken());
            model.addAttribute("returnUrl", getRequestString(REQ_RETURN_URL));
            String appKey = getRequestString(REQ_APP_KEY);
            model.addAttribute("appKey", appKey);
            //兼容一下中学各个端支付，下一次中学vipreport升级联调一下去掉这个
            if(Objects.equals(appKey, OrderProductServiceType.JuniorVipReport.name())){
                MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                        .registerVendorAppUserRef(getRequestString(REQ_APP_KEY), order.getUserId())
                        .getUninterruptibly();
                if (!message.isSuccess() || null == message.get("ref")) {
                    model.addAttribute("error", "注册sessionkey失败");
                    return "/paymentmobile/confirm";
                }
                VendorAppsUserRef ref = (VendorAppsUserRef) message.get("ref");
                model.addAttribute("sessionKey", ref.getSessionKey());
            }
            model.addAttribute("hideTopTitle",getRequestBool("hideTopTitle",false));
            //中学隐藏微信支付
            model.addAttribute("hideWechatpay",getRequestBool("hide_wechatpay"));
            model.addAttribute("type", getRequestString("type"));

            if (MapUtils.isNotEmpty(JsonUtils.fromJson(order.getProductAttributes()))) {
                model.addAttribute("productAttributes", JsonUtils.fromJson(order.getProductAttributes()));
            }

            // 学生是否开启支付权限
            StudentExtAttribute attribute = studentLoaderClient.loadStudentExtAttribute(student.getId());
            if ((currentUser() != null && currentUser().fetchUserType() == UserType.PARENT) || attribute == null || attribute.fetchPayFreeStatus()) {
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
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            model.addAttribute("error", "系统异常");
        }
        return "/paymentmobile/confirm";
    }
}
