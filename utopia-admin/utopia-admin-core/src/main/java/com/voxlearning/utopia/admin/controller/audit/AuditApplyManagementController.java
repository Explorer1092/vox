package com.voxlearning.utopia.admin.controller.audit;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.admin.service.audit.AuditApplyManagementService;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CrmApplyManagementController
 *
 * @author song.wang
 * @date 2017/1/8
 */
@Controller
@RequestMapping("/audit/apply")
public class AuditApplyManagementController extends CrmAbstractController {

    @Inject private AuditApplyManagementService auditApplyManagementService;
    @Inject private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject private WorkFlowLoaderClient workFlowLoaderClient;

    private List<AgentRoleType> USER_ROLE_LIST = Arrays.asList(
            AgentRoleType.Region,
            AgentRoleType.Country,
            AgentRoleType.CityManager,
            AgentRoleType.BusinessDeveloper
    );

    @RequestMapping(value = "apply.vpage", method = RequestMethod.GET)
    public String createApply(Model model) {
        return "audit/apply/create";
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String viewListPage(Model model) {
        int currentPage = Integer.max(1, getRequestInt("currentPage"));
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        List<WorkFlowRecord> recordList = workFlowLoaderClient.loadWorkFlowRecordsByCreatorAccount("admin", adminUser.getAdminUserName())
                .stream().sorted(Comparator.comparing(WorkFlowRecord::getCreateDatetime).reversed()).collect(Collectors.toList());
        int pageSize = 20;
        Pageable page = new PageRequest(currentPage - 1, pageSize);
        Page<WorkFlowRecord> workFlowRecords = PageableUtils.listToPage(recordList, page);
        model.addAttribute("workFlowRecordList", workFlowRecords.getContent());
        model.addAttribute("totalPage", workFlowRecords.getTotalPages());
        model.addAttribute("currentPage", currentPage);
        return "audit/apply/list";
    }

    @RequestMapping(value = "apply_detail.vpage")
    public String viewApplyDetail(Model model) {
        Long applyId = requestLong("applyId");
        Long workflowId = requestLong("workflowId");
        String applyTypeStr = getRequestString("applyType");
        ApplyType applyType = null;
        if (StringUtils.isNotBlank(applyTypeStr)) {
            applyType = ApplyType.nameOf(applyTypeStr);
        }
        if (applyType == null || (applyId == null && workflowId == null)) {
            try {
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return "";
            } catch (Exception ignored) {
            }
        }

        ApplyWithProcessResultData applyDetail = null;
        if (applyId != null) {
            applyDetail = auditApplyManagementService.getApplyDetailWithProcessResultByApplyId(applyType, applyId);
        } else {
            applyDetail = auditApplyManagementService.getApplyDetailByWorkflowId(applyType, workflowId, true);
        }
        model.addAttribute("applyData", applyDetail);
        model.addAttribute("prePath", RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net");
        if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {
            return "audit/apply/dictschool_apply_detail";
        }
        if (ApplyType.AGENT_PRODUCT_FEEDBACK == applyType) {
            AgentProductFeedback agentProductFeedback = (AgentProductFeedback) applyDetail.getApply();
            if (agentProductFeedback != null) {
                String phone = sensitiveUserDataServiceClient.showUserMobile(agentProductFeedback.getTeacherId(), "CRM" + getCurrentAdminUser().getRealName() + "查看产品反馈的老师详情", SafeConverter.toString(agentProductFeedback.getTeacherId()));
                if (phone != null) {
                    model.addAttribute("teacherMobile", phone);
                }
                String account = agentProductFeedback.getAccount();
                List<AgentGroupUser> groupUsers = agentGroupUserLoaderClient.findByUserId(SafeConverter.toLong(account));
                groupUsers = groupUsers.stream().filter(p -> USER_ROLE_LIST.contains(p.getUserRoleType())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(groupUsers)) {
                    AgentGroup group = agentGroupLoaderClient.load(groupUsers.get(0).getGroupId());
                    if (group != null) {
                        model.addAttribute("groupName", group.getGroupName());
                        AgentGroup parentGroup = agentGroupLoaderClient.load(group.getParentId());
                        if (parentGroup != null) {
                            model.addAttribute("prentGroupName", parentGroup.getGroupName());
                        }
                    }
                }
            }
            return "audit/apply/product_feedback_detail";
        }
        if (ApplyType.ADMIN_SEND_APP_PUSH == applyType) {
            return "audit/apply/send_apppush_detail";
        }
        return "audit/apply/list";
    }

}
