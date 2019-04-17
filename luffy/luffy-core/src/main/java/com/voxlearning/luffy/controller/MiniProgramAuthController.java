package com.voxlearning.luffy.controller;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public abstract class MiniProgramAuthController extends MiniProgramAbstractController {


    protected abstract void afterLogin(User use, MapMessage mmr);

    protected abstract void firstLogin(User user, MapMessage mm);

    protected abstract void outOfSystemUser(User user,boolean outOfSystem, MapMessage mm);


    // 通过wx.login获取code 换取openid sessionKey 缓存
    public MapMessage auth() {
        String code = getRequestString("code");
        if (StringUtils.isBlank(code))
            return MapMessage.errorMessage("没有 code");
        String openId = null;
        String sessionKey;
        try {
            FlightRecorder.dot("beforeGetOpenIdByCode");
            Map<String, String> data = getOpenIdAndSessionKeyByCode(code);
            openId = data.get("openId");
            sessionKey = data.get("sessionKey");
            FlightRecorder.dot("afterGetOpenIdByCode");
            if (null == openId || null == sessionKey) {
                return MapMessage.errorMessage("获取数据失败，请重试");
            }
            // 设置登录cookie
            getRequestContext().setAuthenticatedOpenId(openId, 24 * 60 * 60);
            return MapMessage.successMessage().setInfo(openId);
        } catch (Exception ex) {
            log.error("MiniProgram auth error,openId:{} code:{}", openId, code, ex);
        }
        return MapMessage.errorMessage("获取数据失败，请稍候重试");
    }

    // 通过授权手机号一键登录
    public MapMessage authLogin() {
        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("openId is not exist!").setErrorCode(ApiConstants.RES_RESULT_NO_OPEN_ID_CODE);
        }
        String iv = getRequestString("iv");
        String encryptedData = getRequestString("encryptedData");
        String nickName = getRequestString("nickName");  // 微信昵称
        String avatar = getRequestString("avatar");      // 微信头像
        if (StringUtils.isBlank(iv) || StringUtils.isBlank(encryptedData))
            return MapMessage.errorMessage("参数错误");
        String sessionKey = getSessionKeyByOpenId(openId,type());
        if (StringUtils.isBlank(sessionKey)) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NO_SESSION_KEY_CODE).setInfo("no sessionKey!");
        }
        try {
            // 解密
            String phoneNo = getDecryptData(iv, encryptedData, sessionKey, "purePhoneNumber");
            if (StringUtils.isBlank(phoneNo) || !MobileRule.isMobile(phoneNo)) {
                log.error("MiniProgram decode mobile faild, please check, {}, {}, {}, {}", sessionKey, iv, encryptedData, phoneNo);
                return MapMessage.errorMessage("手机号授权失败");
            }
            return doLogin(phoneNo, openId, nickName, avatar);
        } catch (Exception ex) {
            log.error("MiniProgram authlogin error,openId:{}", openId, ex);
        }
        return MapMessage.errorMessage("登录失败，请稍候重试");
    }


    public MapMessage getCid() {
        return MapMessage.successMessage().add("cid", tokenHelper.generateContextId(getRequestContext()));
    }


    public MapMessage getVerifyCode() {
        String mobile = getRequestString("mobile");
        String contextId = getRequestString("cid");
        try {
            if (!MobileRule.isMobile(mobile)) {
                return MapMessage.errorMessage("手机号码有误");
            }
            if (!tokenHelper.verifyAndConsumeContextId(contextId)) {
                return MapMessage.errorMessage("您发送的太频繁了,请稍侯再试~");
            }
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.PARENT_VERIFY_MOBILE_XCX_REGISTER.name(),
                    false);
        } catch (Exception ex) {
            log.error("Send sms code for parent mobile bind failed,mobile:{},contextId:{}", mobile, ex);
            return MapMessage.errorMessage("发送失败");
        }
    }


    public MapMessage verifiedLoginPost() {
        String mobileNumber = getRequestString("mobile");
        String verifyCode = getRequestString("code");
        String nickName = getRequestString("nickname");  // 微信昵称
        String avatar = getRequestString("avatar");      // 微信头像

        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("openId无效").setErrorCode(ApiConstants.RES_RESULT_NO_OPEN_ID_CODE);
        }

        if (!MobileRule.isMobile(mobileNumber)) {
            return MapMessage.errorMessage("请输入正确的手机号");
        }

        if (StringUtils.isBlank(verifyCode)) {
            return MapMessage.errorMessage("请输入正确验证码");
        }
        try {
            // 验证短信验证码
            MapMessage verifySmsMessage = smsServiceClient.getSmsService().verifyValidateCode(mobileNumber, verifyCode, SmsType.PARENT_VERIFY_MOBILE_XCX_REGISTER.name());
            if (!verifySmsMessage.isSuccess()) {
                return verifySmsMessage;
            }

            return doLogin(mobileNumber, openId, nickName, avatar);
        } catch (Exception ex) {
            log.error("Verified Login Failed, mobile:{},code:{}", mobileNumber, verifyCode, ex);
            return MapMessage.errorMessage("登录失败，请稍候重试");
        }
    }


    // 执行登录逻辑 根据手机号
    private MapMessage doLogin(String mobileNumber, String openId, String nickName, String avatar) {
        String source = "MiniProgram_" + type();
        User parent;
        MapMessage mm=MapMessage.successMessage();

        // Check out of system user
        boolean oos = CollectionUtils.isEmpty(userLoaderClient.loadMobileAuthentications(mobileNumber));

        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobileNumber, UserType.PARENT);
        if (userAuthentication == null) {

            parent = newChannelCParentLogin(mobileNumber);
            if (parent == null) {
                return MapMessage.errorMessage("创建新用户失败");
            }
            userAuthentication = userLoaderClient.loadMobileAuthentication(mobileNumber, UserType.PARENT);
            //
            // First login user
            firstLogin(parent,mm);


        } else {
            parent = userLoaderClient.loadUser(userAuthentication.getId());
        }
        if (parent == null || userAuthentication == null){
            return MapMessage.errorMessage("获取用户信息失败");
        }

        //
        // Out-of-system user
        outOfSystemUser(parent, oos, mm);


        wechatServiceClient.bindUserAndMiniProgramOrRelogin(parent.getId(), openId, source, type());

        // 更新用户头像昵称
        updateParentWechatInfo(nickName, avatar, parent.getId());
        //设置登录cookie
        getRequestContext().saveAuthenticateState(24 * 60 * 60, parent.getId(), userAuthentication.getPassword(), openId, RoleType.ROLE_PARENT);
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(parent.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wechat);
        afterLogin(parent, mm);
        return mm;
    }



    private void updateParentWechatInfo(String nickName, String avatar, Long userId) {
        ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(userId);
        if (parentExtAttribute == null) {
            parentExtAttribute = new ParentExtAttribute(userId);
        }
        if (StringUtils.isNotBlank(nickName)) {
            parentExtAttribute.setWechatNick(nickName);
        }
        if (StringUtils.isNotBlank(avatar)) {
            parentExtAttribute.setWechatImage(avatar);
        }
        parentServiceClient.updateParentExtAttribute(parentExtAttribute);
    }


    private User newChannelCParentLogin(String mobile) {
        MapMessage mapMessage = registerChannelCParent(mobile);

        Object user = mapMessage.get("user");
        if (!(user instanceof User))
            return null;
        return (User) user;
    }

    private MapMessage registerChannelCParent(String mobile) {
        //初始化要注册的用户
        NeonatalUser neonatalUser = initChannelCUser(UserType.PARENT, RoleType.ROLE_PARENT);
        MapMessage message = userServiceClient.registerUser(neonatalUser);
        if (!message.isSuccess()) {
            return message;
        }
        User newUser = (User) message.get("user");
        //绑定手机号
        MapMessage activeResult = userServiceClient.activateUserMobile(newUser.getId(), mobile, true);
        if (!activeResult.isSuccess()) {
            return activeResult;
        }

        ParentExtAttribute parentExtAttribute = new ParentExtAttribute(newUser.getId());
        parentExtAttribute.setBrandFlag(true);//默认c端家长同意:家长通产品商业区隔协议书
        MapMessage extResult = parentServiceClient.generateParentExtAttribute(parentExtAttribute);
        if (!extResult.isSuccess()) {
            logger.error("插入c端家长附加信息失败");
            return extResult;
        }
        return message;
    }



    //初始化用户
    private NeonatalUser initChannelCUser(UserType userType, RoleType roleType) {
        //用户名和昵称为空字符串,密码为随机数,
        //下列数据为空时是否违背底层数据库设计
        // Save User Info
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(roleType);
        neonatalUser.setUserType(userType);
        neonatalUser.setRealname("");
        neonatalUser.setNickName("");
        neonatalUser.setPassword(RandomGenerator.generatePlainPassword());
        neonatalUser.setInviter(null);
        neonatalUser.setWebSource(type().getWebSource().name());
        return neonatalUser;
    }


}
