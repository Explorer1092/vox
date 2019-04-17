package com.voxlearning.utopia.agent.controller.api.v1;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.Password;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialCost;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.support.AgentUserSupport;
import com.voxlearning.utopia.agent.utils.ApiMapMessage;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

/**
 * AgentAuthApiController
 *
 * @author song.wang
 * @date 2018/8/8
 */
@Controller
@RequestMapping("/v1/auth")
public class AgentAuthApiController extends AbstractAgentController {

    @Inject
    private AgentUserSupport agentUserSupport;
    @Inject private AgentRequestSupport agentRequestSupport;
    @Inject
    private AgentMaterialBudgetService agentMaterialBudgetService;



    @RequestMapping("index.vpage")
    public String index(){
        return "redirect:/mobile/performance/index.vpage";
    }


    @RequestMapping(value = "loginByPwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public ApiMapMessage loginByPwd(){
        String userName = getRequestString("userName");
        String password = getRequestString("password");
        if (userName.length() >= 20 || password.length() >= 20){
            return ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "用户名或密码长度不能超过20");
        }

        String captchaToken = getRequestString("captchaToken");
        String captchaCode = getRequestString("captchaCode");
        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "验证码失效或错误，请重新输入");
        }

        AgentUser agentUser = baseUserService.getByAccountName(userName);
        // staging环境超级密码
        boolean isSuperPwd = isSuperPwd(userName, password);

        if(agentUser == null){
            return ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "用户名错误，请重新输入");
        }
        if (!isSuperPwd && !Password.obscurePassword(password, agentUser.getPasswdSalt()).equals(agentUser.getPasswd())){
            return ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "密码错误，请重新输入");
        }

        // 设置设备ID
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging())) {
            String deviceId = agentRequestSupport.getDeviceId(getRequest());
            if (StringUtils.isNotBlank(deviceId)) {
                if (StringUtils.isBlank(agentUser.getDeviceId())) {
                    agentUser.setDeviceId(deviceId);
                    baseUserService.updateAgentUser(agentUser);
                } else if (!Objects.equals(deviceId, agentUser.getDeviceId())) {
                    return ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "该用户已经绑定其他设备，请使用手机号登录或联系管理员");
                }
            }
        }

        return afterCheckLoginData(agentUser);
    }

    // 客户端短信验证码登录
    @RequestMapping(value = "loginBySmsCode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public ApiMapMessage loginByMobile(){
        String mobile = getRequestString("mobile");
        String smsCode = getRequestString("smsCode");
        if (!MobileRule.isMobile(mobile)) {
            return ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "手机号输入有误！");
        }
        if (!verifySmsCode(mobile, smsCode, SmsType.APP_CRM_MOBILE_LOGIN).isSuccess()) {
            return ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "验证码失效或错误，请重新输入");
        }

        AgentUser agentUser = baseUserService.getByMobile(mobile);
        if(agentUser == null){
            return ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "用户不存在，请重新输入");
        }


        // 设置设备ID
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging())) {
            String deviceId = agentRequestSupport.getDeviceId(getRequest());
            if (StringUtils.isNotBlank(deviceId) && !Objects.equals(deviceId, agentUser.getDeviceId())) {
                agentUser.setDeviceId(deviceId);
                baseUserService.updateAgentUser(agentUser);
            }
        }
        return afterCheckLoginData(agentUser);
    }

    public ApiMapMessage afterCheckLoginData(AgentUser agentUser){

        AuthCurrentUser currentUser = agentUserSupport.createCurrentUserById(agentUser.getId());
        if(currentUser == null){
            return ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "用户不存在，请重新输入");
        }
        agentCacheSystem.setAuthCurrentUser(currentUser.getUserId(), currentUser);
        agentCacheSystem.updateUserAuthRefreshTime(currentUser.getUserId());

        // 设置cookie
        setUserAndSignToCookie(SafeConverter.toString(currentUser.getUserId()), agentApiAuth.getUserSign(currentUser.getUserId()));

        ApiMapMessage message = getUserInfo(agentUser);
        message.put(AgentApiAuth.PARAM_SESSION_KEY, agentApiAuth.generateSessionKey(agentUser.getId()));
        message.put("unread_notify_count", agentUserSupport.getUnreadNotifyCount(agentUser.getId()));
        return message;
    }

    // 判断是否是通用密码
    private boolean isSuperPwd(String userName, String password){
        boolean superPwd = false;
        if (RuntimeMode.isStaging() && ((!Objects.equals(userName, "admin") && password.equals("_17zy2017")) || (Objects.equals(userName, "admin") && password.equals("_17zyAdmin")))) {
            superPwd = true;
        }
        if (RuntimeMode.isTest() && password.equals("test")) {
            superPwd = true;
        }
        if (RuntimeMode.isDevelopment()) {
            superPwd = true;
        }
        return superPwd;
    }

    @RequestMapping(value = "getUserInfo.vpage")
    @ResponseBody
    public ApiMapMessage getUserInfo(){
        AuthCurrentUser currentUser = getCurrentUser();
        if(currentUser == null){
            return ApiMapMessage.errorMessage(AgentConstants.API_NEED_LOGIN, "请登录！");
        }
        AgentUser user = baseOrgService.getUser(getCurrentUserId());
        ApiMapMessage message = getUserInfo(user);
        if(message.isSuccess()){
            message.put("unread_notify_count", agentUserSupport.getUnreadNotifyCount(user.getId()));
        }
        return message;
    }

    @RequestMapping(value = "getUnreadNotifyCount.vpage")
    @ResponseBody
    public ApiMapMessage getUserUnreadNotifyCount(){
        ApiMapMessage message = ApiMapMessage.successMessage();
        message.put("unread_notify_count", agentUserSupport.getUnreadNotifyCount(getCurrentUserId()));
        return message;
    }

    private ApiMapMessage getUserInfo(AgentUser user){
        if(user == null){
            return ApiMapMessage.errorMessage(AgentConstants.API_NEED_LOGIN, "请登录！");
        }
        ApiMapMessage message = ApiMapMessage.successMessage();

        message.put("user_id", user.getId());
        message.put("user_name", user.getRealName());
        message.put("user_avatar", user.getAvatar());
        message.put("jpush_tag_list", agentUserSupport.getUserJpushTagList(user.getId()));

        // 设置部门角色信息
        AgentGroup group = null;
        AgentRoleType role = null;
        Map<Long, Integer> groupRoleMap = baseOrgService.getGroupUserRoleMapByUserId(user.getId());
        if(MapUtils.isNotEmpty(groupRoleMap)){
            Long groupId = groupRoleMap.keySet().stream().findFirst().orElse(null);
            group = baseOrgService.getGroupById(groupId);
            role = AgentRoleType.of(groupRoleMap.get(groupId));
        }

        message.put("group_name", group == null ? "" :group.getGroupName());
        message.put("role_name", role == null ? "" : role.getRoleName());

        Long groupId = null;
        boolean isGroupManager = false;
        if(group != null){
            groupId = group.getId();
            isGroupManager = baseOrgService.isGroupManager(user.getId(), group.getId());
        }
        message.put("group_id", groupId);
        message.put("is_group_manager", isGroupManager);

        // 设置物料费用
        AgentMaterialCost userMaterialCost = agentMaterialBudgetService.getUserMaterialCostByUserId(user.getId());
        if (null != userMaterialCost) {
            message.put("material_cost_id",userMaterialCost.getId());//物料ID
            message.put("material_balance", userMaterialCost.getBalance());   //可用余额
        }else {
            message.put("material_cost_id", "");//物料ID
            message.put("material_balance", 0);   //可用余额
        }
        return message;
    }
}
