package com.voxlearning.utopia.agent.controller.mobile.my;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.agent.bean.AgentGroupHeadCountInfo;
import com.voxlearning.utopia.agent.bean.AgentGroupRegionInfo;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBalanceChangeRecord;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.service.mobile.AgentPersonalInfoService;
import com.voxlearning.utopia.agent.service.user.OrgConfigService;
import com.voxlearning.utopia.agent.utils.ApiMapMessage;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 我的-个人信息Controller
 * @author delinag.che
 * @date 2018-04-26
 */
@Controller
@RequestMapping(value = "/mobile/personal_info")
public class AgentPersonalInfoController extends AbstractAgentController {
    @Inject
    private AgentPersonalInfoService agentPersonalInfoService;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private AgentMaterialBudgetService agentMaterialBudgetService;
    @Inject
    private OrgConfigService orgConfigService;
    /**
     * 个人信息详情接口
     * @return
     */
    @RequestMapping(value = "/user_detail.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userDetail() {
        Long userId = getRequestLong("userId");
        if (userId == 0L) {
            userId = getCurrentUserId();
        }
        Map<String, Object> dataMap = agentPersonalInfoService.userDetail(userId);
        return MapMessage.successMessage().add("data",dataMap);
    }

    /**
     * 更换手机号发送验证码
     * @return
     */
    @RequestMapping(value = "/getSMSCode.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSMSCode() {
        String newMobile = getRequestString("newMobile");
        if (!MobileRule.isMobile(newMobile)) {
            return MapMessage.errorMessage("请填写正确的手机号码");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(newMobile, SmsType.APP_CRM_MOBILE_LOGIN.name(), false);
    }

    /**
     * 更改手机号
     * @return
     */
    @RequestMapping(value = "/change_mobile.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeMobile() {
        //旧手机号
        String oldMobile = getRequestString("oldMobile");
        //新手机号
        String newMobile = getRequestString("newMobile");
        //验证码
        String verificationCode = getRequestString("verificationCode");
        if (oldMobile.equals(newMobile)){
            return MapMessage.errorMessage("新手机号与已绑定手机号相同");
        }
        if (!verifySmsCode(newMobile, verificationCode, SmsType.APP_CRM_MOBILE_LOGIN).isSuccess()) {
            return MapMessage.errorMessage("验证码失效或错误，请重新输入");
        }
        AgentUser agentUser = baseUserService.getByMobile(newMobile);
        if (null != agentUser){
            return MapMessage.errorMessage("该手机号已经被占用");
        }
//        if(StringUtils.isBlank(oldMobile)){
//            agentUser = baseUserService.getByAccountName(getCurrentUser().getUserName());
//        }else {
//            agentUser = baseUserService.getByMobile(oldMobile);
//        }
        agentUser = baseUserService.getByAccountName(getCurrentUser().getUserName());
        agentUser.setTel(newMobile);
        baseUserService.updateAgentUser(agentUser);
        return MapMessage.successMessage();
    }

    /**
     * 验证手机验证码
     * @param mobile
     * @param code
     * @param smsType
     * @return
     */
    public MapMessage verifySmsCode(String mobile, String code, SmsType smsType) {
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效手机号");
        }
        return smsServiceClient.getSmsService().verifyValidateCode(mobile, code, smsType.name());
    }

    /**
     * 部门详情
     * @return
     */
    @RequestMapping(value = "/department_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage departmentDetail() {
        Long groupId = getRequestLong("groupId");
        if (groupId == 0L) {
            return MapMessage.errorMessage("部门信息不存在,请刷新页面");
        }
        Map<String, Object> dataMap = agentPersonalInfoService.departmentDetail(groupId);
        return MapMessage.successMessage().add("data",dataMap);
    }

    /**
     * 专员情况列表
     * @return
     */
    @RequestMapping(value = "/businessDeveloper_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage businessDeveloperList() {
        Long groupId = getRequestLong("groupId");
        if (groupId == 0L) {
            return MapMessage.errorMessage("部门信息不存在,请刷新页面");
        }
        List<AgentGroupHeadCountInfo> dataList = agentPersonalInfoService.businessDeveloperList(groupId);
        return MapMessage.successMessage().add("dataList",dataList);
    }

    /**
     * 负责城市列表
     * @return
     */
    @RequestMapping(value = "/city_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage cityList() {
        Long groupId = getRequestLong("groupId");
        if (groupId == 0L) {
            return MapMessage.errorMessage("部门信息不存在,请刷新页面");
        }
        List<AgentGroupRegion> agentGroupRegionList = baseOrgService.getGroupRegionByGroup(groupId);
        List<AgentGroupRegionInfo> dataList = agentPersonalInfoService.createAgentGroupRegionInfo(agentGroupRegionList,groupId);
        //按照省份排序
        dataList = dataList.stream().filter(item -> null != item && null != item.getProvinceName()).sorted(Comparator.comparing(AgentGroupRegionInfo::getProvinceName, Collator.getInstance(java.util.Locale.CHINA))).collect(Collectors.toList());
        return MapMessage.successMessage().add("dataList",dataList);
    }

    /**
     * 物料余额修改记录
     */
    @RequestMapping(value = "/material_change_record.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMaterialChangeRecord(){
        String budgetId = getRequestString("budgetId");
        List<AgentMaterialBalanceChangeRecord> budgetChangeRecords = agentMaterialBudgetService.getBalanceChangeRecords(budgetId);
        return MapMessage.successMessage().add("budgetChangeRecords",budgetChangeRecords);
    }


    /**
     * 人员物料费用列表
     * @return
     */
    @RequestMapping(value = "user_material_cost_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupMaterialCost(){
        String materialCostId = getRequestString("materialCostId");
        List<Map<String,Object>> dataList = new ArrayList<>();
        if (StringUtils.isNotBlank(materialCostId)){
            dataList = agentMaterialBudgetService.getUserMaterialCostByGroup(materialCostId);
        }
        return MapMessage.successMessage().add("dataList",dataList);
    }

    /**
     * 分配物料余额
     * @return
     */
    @RequestMapping(value = "distribute_material_cost.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage distributeMaterialCost(){
        String materialCostId = getRequestString("materialCostId");
        Long userId = getRequestLong("userId");
        Double distributeMaterialCost = getRequestDouble("distributeMaterialCost");
        return agentMaterialBudgetService.distributeMaterialCost(materialCostId,userId,distributeMaterialCost);
    }

    @RequestMapping(value = "bind_honeycomb_account.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindHoneycombAccount(){
        Long userId = getCurrentUserId();

        String mobile = getRequestString("mobile");
        String smsCode = getRequestString("smsCode");
        if (!com.voxlearning.alps.core.util.MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号输入有误！");
        }
        if (!verifySmsCode(mobile, smsCode, SmsType.APP_CRM_MOBILE_LOGIN).isSuccess()) {
            return MapMessage.errorMessage("验证码失效或错误，请重新输入");
        }

        return orgConfigService.bindHoneycombAccount(userId, mobile);
    }
}


