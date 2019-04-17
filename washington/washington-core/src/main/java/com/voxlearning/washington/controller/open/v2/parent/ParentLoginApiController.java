package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.parent.constant.ParentLoginApiNextStep;
import com.voxlearning.utopia.service.parent.constant.ParentLoginType;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.helpers.ValidateWechatOpenIdHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2018-6-26
 */
@Controller
@RequestMapping(value = "/v2/parent/")
public class ParentLoginApiController extends AbstractParentApiController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private ValidateWechatOpenIdHelper validateWechatOpenIdHelper;

    /**
     * 3.0版本开始用
     * 所有登录方式最后走的短信验证码登录接口
     * 这一期这里只有两种登录方式。后续要加其它方式只需要改这个接口即可
     * 微信
     * 短信
     */
    @RequestMapping(value = "login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage login() {
        try {
            validateRequired(REQ_PARENT_LOGIN_TYPE, "登录类型");
            validateRequestNoSessionKey(REQ_PARENT_LOGIN_TYPE, REQ_WECHAT_UNION_ID, REQ_WECHAT_OPEN_ID, REQ_UUID, REQ_MOBILE, REQ_VERIFY_CODE);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String unionId = getRequestString(REQ_WECHAT_UNION_ID);
        String openId = getRequestString(REQ_WECHAT_OPEN_ID);
        String uuid = getRequestString(REQ_UUID);
        String mobile = getRequestString(REQ_MOBILE);
        String code = getRequestString(REQ_VERIFY_CODE);
        Integer loginType = getRequestInt(REQ_PARENT_LOGIN_TYPE);

        ParentLoginType parentLoginType = ParentLoginType.safeParse(loginType);
        if (parentLoginType == ParentLoginType.UNKNOWN) {
            return failMessage(RES_RESULT_PARENT_LOGIN_TYPE_ERROR_MSG);
        }
        boolean create = false;
        //1.这是微信登录且手机号为空，这是微信授权之后直接登录的情况
        if (parentLoginType == ParentLoginType.WECHAT && StringUtils.isBlank(mobile)) {
            User user = wechatLoaderClient.getWechatLoader().loadWechatUserByUnionId(unionId);
            //没绑定用户
            if (user == null) {
                return successMessage().add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.VALIDATE_SMS.getType());
            }
            //绑定的用户没有手机认证
            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(user.getId());
            if (authentication == null || StringUtils.isBlank(authentication.getSensitiveMobile())) {
                return successMessage().add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.VALIDATE_SMS.getType());
            }
            return doParentLogin(user).add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.LOGIN_SUCCESS.getType()).add(REQ_RESULT_PARENT_IS_CREATE, create);
        }
        //2.这是任何登录的手机号+验证码的情况
        User parent = userLoaderClient.loadUserByToken(mobile).stream().filter(p -> p.fetchUserType() == UserType.PARENT).findFirst().orElse(null);
        if (parent != null && StringUtils.isNotBlank(code)) {
            // 支持临时密码登录
            if (StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(parent.getId()), code)) {
                return doParentLogin(parent).add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.LOGIN_SUCCESS.getType()).add(REQ_RESULT_PARENT_IS_CREATE, create);
            }
            //固定一个手机号和验证码给苹果审核用
            if (isAccountForAppleAuth(mobile, code)) {
                return doParentLogin(parent).add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.LOGIN_SUCCESS.getType()).add(REQ_RESULT_PARENT_IS_CREATE, create);
            }
            // 银座九号小学支持id+pwd登录
            if (isMatchIdPwdLoginCondition(parent) || ((parent.getId().equals(20001L) || parent.getId().equals(214303936L)))) {
                UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(parent.getId());
                if (userAuthentication != null && userAuthentication.fetchUserPassword().match(code)) {
                    return doParentLogin(parent).add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.LOGIN_SUCCESS.getType()).add(REQ_RESULT_PARENT_IS_CREATE, create);
                }
            }
        }
        //这里开始就是真实用户的逻辑了。
        //验证验证码
        SmsType smsType = SmsType.APP_PARENT_VERIFY_MOBILE_LOGIN_MOBILE;
        MapMessage mapMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, smsType.name());
        if (!mapMessage.isSuccess()) {
            return failMessage(mapMessage.getInfo());
        }
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
        //手机号已注册用户
        if (userAuthentication != null) {
            User mobileUser = raikouSystem.loadUser(userAuthentication.getId());
            if (parentLoginType == ParentLoginType.MOBILE) {
                return doParentLogin(mobileUser).add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.LOGIN_SUCCESS.getType()).add(REQ_RESULT_PARENT_IS_CREATE, create);
            } else if (parentLoginType == ParentLoginType.WECHAT) {
                //1.unionId绑定的用户有手机，提示手机号占用。没有手机直接绑定当前手机登录
                User user = wechatLoaderClient.getWechatLoader().loadWechatUserByUnionId(unionId);
                if (user != null && !Objects.equals(user.getId(), userAuthentication.getId())) {
                    return failMessage(RES_RESULT_MOBILE_BIND_WECHAT_ERROR_MSG);
                }
                //2.手机号用户没有绑定当前微信，提示解绑。已绑定当前微信/未绑定任何微信直接登录
                List<UserWechatRef> userWechatRefs = wechatLoaderClient.getWechatLoader().loadUserWechatRefs(Collections.singleton(userAuthentication.getId()), WechatType.PARENT_APP).getOrDefault(userAuthentication.getId(), Collections.emptyList());
                if (CollectionUtils.isNotEmpty(userWechatRefs) && userWechatRefs.stream().noneMatch(p -> StringUtils.equals(p.getUnionId(), unionId))) {
                    //把unionId_openId暂存起来。如果用户选择更新的时候需要校验一下
                    validateWechatOpenIdHelper.storeOpenIdAndUnionId(mobile, openId, unionId);
                    return successMessage().add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.TELL_USER_UPDATE_WECHAT.getType());
                } else {
                    bindWechatUnionIdAndUserId(userAuthentication.getId(), unionId, openId);
                    return doParentLogin(mobileUser).add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.LOGIN_SUCCESS.getType()).add(REQ_RESULT_PARENT_IS_CREATE, create);
                }
            }
        }
        //手机号没注册用户。处理一下是绑定给微信绑定的用户还是新注册一个
        User user = wechatLoaderClient.getWechatLoader().loadWechatUserByUnionId(unionId);
        if (user != null) {
            //绑定已有用户
            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(user.getId());
            if (authentication == null || StringUtils.isBlank(authentication.getSensitiveMobile())) {
                MapMessage message = userServiceClient.activateUserMobile(user.getId(), mobile, true);
                if (!message.isSuccess()) {
                    return failMessage(message.getInfo());
                }
            }
            parent = user;
        } else {
            //新注册一个用户
            MapMessage message = parentRegisterHelper.registerChannelCParent(mobile, RoleType.ROLE_PARENT, uuid, "17Parent-c");
            if (!message.isSuccess()) {
                return failMessage(message.getInfo());
            }
            parent = (User) message.get("user");
            create = true;
        }

        //处理是否要绑定微信
        if (parentLoginType == ParentLoginType.WECHAT) {
            bindWechatUnionIdAndUserId(parent.getId(), unionId, openId);
        }
        return doParentLogin(parent)
                .add(RES_VERIFY_CHANNEL_C_RESULT, ParentLoginApiNextStep.LOGIN_SUCCESS.getType())
                .add(REQ_RESULT_PARENT_IS_CREATE, create);
    }

    private boolean isAccountForAppleAuth(String mobile, String code) {
        return "11122233311".equals(mobile) && "1788".equals(code);
    }

    @RequestMapping(value = "update_wechat.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateWechatUnionId() {
        String unionId = getRequestString(REQ_WECHAT_UNION_ID);
        String openId = getRequestString(REQ_WECHAT_OPEN_ID);
        String mobile = getRequestString(REQ_MOBILE);
        try {
            validateRequired(REQ_WECHAT_UNION_ID, "UNION_ID");
            validateRequired(REQ_WECHAT_OPEN_ID, "OPEN_ID");
            validateRequired(REQ_MOBILE, "手机号码");
            validateRequestNoSessionKey(REQ_WECHAT_UNION_ID, REQ_WECHAT_OPEN_ID, REQ_MOBILE);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        //校验微信号
        MapMessage message = validateWechatOpenIdHelper.validateOpenIdAndUnionId(mobile, openId, unionId);
        if (!message.isSuccess()) {
            return failMessage(message.getInfo());
        }
        //校验手机号
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
        if (userAuthentication == null || StringUtils.isBlank(userAuthentication.getSensitiveMobile())) {
            return failMessage(RES_RESULT_LOAD_USER_ERROR);
        }
        //解绑原微信
        MapMessage mapMessage = wechatServiceClient.getWechatService().unbindUserAndWechatWithUserIdAndType(userAuthentication.getId(), WechatType.PARENT_APP.getType());
        if (!mapMessage.isSuccess()) {
            return failMessage(RES_RESULT_UNBIND_WECHAT_ERROR_MSG);
        }
        //绑定新微信
        MapMessage bindMessage = wechatServiceClient.getWechatService().bindUserAndWechat(userAuthentication.getId(), openId, unionId, "", WechatType.PARENT_APP.getType());
        if (!bindMessage.isSuccess()) {
            return failMessage(RES_RESULT_BIND_WECHAT_ERROR_MSG);
        }
        //踢掉已登录的所有该账号
        expireAndCreateNewSessionKey(userAuthentication.getId());
        //登录该账号
        User parent = raikouSystem.loadUser(userAuthentication.getId());
        return doParentLogin(parent);
    }

}
