package com.voxlearning.wechat.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.wechat.context.WxConfig;
import org.springframework.ui.Model;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Summer on 2018/7/19
 */
public class AbstractChipsController extends AbstractController {

    @Inject protected PaymentGatewayManager paymentGatewayManager;


    protected static List<Long> ChipsTestUser = Arrays.asList(24680223L, 215602147L, 225508803L);

    /////////////////////////
    //错误页
    @Override
    protected String redirectWithMsg(String msg, Model model) {
        String url = getRequestContext().getFullRequestUrl();
        model.addAttribute("errmsg", msg);
        model.addAttribute("reffer", url);
        return "/parent/chips/error";
    }

    protected User currentChipsUser() {
        Long userId = getRequestContext().getUserId();
//        userId = 268117L;
        if (userId == null || userId == 0) {
            return null;
        }
//         加入是否登录状态校验
        UserWechatRef ref = wechatLoaderClient.loadUserWechatRefByUserIdAndWechatType(userId, WechatType.CHIPS.getType());
        if (ref == null) {
            return null;
        }
        return userLoaderClient.loadUser(userId);
    }

    protected void initWechatConfigModel(Model model, WxConfig wxConfig, WechatType wechatType) {
        model.addAttribute("signature", wxConfig.sha1Sign());
        model.addAttribute("appid", ProductConfig.get(wechatType.getAppId()));
        model.addAttribute("timestamp", wxConfig.getTimestamp());
        model.addAttribute("nonceStr", wxConfig.getNonce());
    }

    protected void initChipsPayModel(Model model,
                                     WxConfig wxConfig,
                                     WechatType wechatType,
                                     Long payTimestamp,
                                     String payNonceStr,
                                     String payPackage,
                                     String paySign,
                                     String backUrl) {
        model.addAttribute("config_signature", wxConfig.sha1Sign());
        model.addAttribute("appid", ProductConfig.get(wechatType.getAppId()));
        model.addAttribute("config_timestamp", wxConfig.getTimestamp());
        model.addAttribute("config_nonceStr", wxConfig.getNonce());

        model.addAttribute("pay_timestamp", payTimestamp);
        model.addAttribute("pay_nonceStr", payNonceStr);
        model.addAttribute("pay_package", payPackage);
        model.addAttribute("pay_signType", "MD5");
        model.addAttribute("pay_paySign", paySign);
        model.addAttribute("pay_backUrl", backUrl);
    }


    protected MapMessage wrapper(Consumer<MapMessage> wrapper) {

        MapMessage mm = MapMessage.successMessage();
        try {
            wrapper.accept(mm);
        } catch (Exception e) {
            mm = MapMessage.errorMessage(e.getMessage());
            logger.error(e.getMessage());

        }
        return mm;
    }
}
