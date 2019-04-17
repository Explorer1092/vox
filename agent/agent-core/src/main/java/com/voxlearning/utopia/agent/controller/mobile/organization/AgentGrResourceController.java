/**
 * Author:   xianlong.zhang
 * Date:     2019/1/17 11:14
 * Description: 上层资源接口
 * History:
 */
package com.voxlearning.utopia.agent.controller.mobile.organization;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.EmailRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.agent.constants.Gender;
import com.voxlearning.utopia.agent.constants.ResearchersJobType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.AgentResearchersService;
import com.voxlearning.utopia.agent.service.organization.AgentOuterResourceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = "/mobile/grResource")
public class AgentGrResourceController extends AbstractAgentController {

    @Inject private AgentResearchersService agentResearchersService;
    @Inject private AgentOuterResourceService agentOuterResourceService;
    // 新建或更新学校资源
    @RequestMapping(value = "upsert_school_resource.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertSchoolResource() {

        Long id = requestLong("id");
        String name = getRequestString("name");
        Integer gender = getRequestInt("gender");
        Long schoolId = getRequestLong("schoolId");
        Integer job = getRequestInt("job");
        ResearchersJobType researchersJobType = ResearchersJobType.typeOf(job);
        if(researchersJobType == null){
            return MapMessage.errorMessage("职位类型不正确");
        }
        String department = getRequestString("department"); //部门

        String gradeStr = getRequestString("gradeList");
        Subject subject = Subject.fromSubjectId(getRequestInt("subject"));
        String specificJob = getRequestString("specificJob"); //备注

        if(schoolId == 0L){
            return MapMessage.errorMessage("学校不能为空");
        }
        String phone = getRequestString("phone");
        String telephone = getRequestString("telephone");
        String weChatOrQq = getRequestString("weChatOrQq");
        String email = getRequestString("email");
        String photoUrl = getRequestString("photoUrls"); //名片/照片
        List<String> photoUrls = new ArrayList<>();
        if(StringUtils.isNotBlank(photoUrl)){
            photoUrls = Arrays.asList(photoUrl.split(","));
        }
        if(StringUtils.isEmpty(phone) && StringUtils.isEmpty(telephone)){
            return MapMessage.errorMessage("手机、座机不能同时为空");
        }

        if(StringUtils.isNotBlank(telephone) && !Pattern.matches("0\\d{2,3}-\\d{7,8}", telephone)){
            return MapMessage.errorMessage("座机号格式不正确");
        }
        if (StringUtils.isBlank(name)) {
            return MapMessage.errorMessage("请姓名");
        }
        if (Gender.typeOf(gender) == null) {
            return MapMessage.errorMessage("请选择性别");
        }
        if (StringUtils.isNotBlank(phone) && !MobileRule.isMobile(phone)) {
            return MapMessage.errorMessage("请填写正确的手机号");
        }
        if (StringUtils.isNotBlank(email) && !EmailRule.isEmail(email)) {
            return MapMessage.errorMessage("请填写正确的邮箱地址");
        }
        if (!agentResearchersService.isRepetitionPhone(getCurrentUserId(), phone, id).isSuccess()) {
            return agentResearchersService.isRepetitionPhone(getCurrentUserId(), phone, id).add("info","已有同一手机号码的教研员");
        }
        return agentOuterResourceService.upsertSchoolResource(id, name, gender, phone, job, gradeStr, subject,specificJob,telephone,schoolId,department,weChatOrQq,email,photoUrls);
    }

    // 获取上级管理
    @RequestMapping(value = "get_user_manager_ingo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUserManagerInfo(){
        return agentOuterResourceService.getUserManagerInfo();
    }

    // 确定申请权限
    @RequestMapping(value = "apply_resource_authority.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage applyResourceAuthority(){
        Long resourceId = getRequestLong("resourceId");
        Long managerId = getRequestLong("managerId");
        return agentOuterResourceService.createApply(resourceId,managerId);
    }

    // 上层资源权限申请审核
    @RequestMapping(value = "audit_resource_authority.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage auditResourceAuthority(){
        Long resourceId = getRequestLong("resourceId");
        String applyId = getRequestString("applyId");
        Integer result = getRequestInt("result");
        String opinions = getRequestString("opinions");
        return agentOuterResourceService.auditResourceApply(resourceId,applyId,result,opinions);
    }

    // 申请详情
    @RequestMapping(value = "apply_detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage applyDetail(){
        String applyId = getRequestString("applyId");
        if(StringUtils.isEmpty(applyId)){
            return MapMessage.errorMessage("申请id不能为空");
        }
        return agentOuterResourceService.getApplyInfo(applyId);
    }

    // 资源详情页查看获取资源人员列表
    @RequestMapping(value = "authority_user_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage authorityUserList(){
        Long resourceId = getRequestLong("resourceId");
        if(resourceId == null || resourceId == 0){
            return MapMessage.errorMessage("资源id不能为空");
        }
        return agentOuterResourceService.authorityUserList( resourceId);
    }
}
