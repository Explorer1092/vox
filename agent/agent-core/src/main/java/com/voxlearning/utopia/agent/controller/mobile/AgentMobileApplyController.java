package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.apply.AgentApplyManagementService;
import com.voxlearning.utopia.agent.service.mobile.SchoolClueService;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;
import com.voxlearning.utopia.service.crm.api.loader.agent.DataReportApplyLoader;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AgentMobileApplyController
 *
 * @author song.wang
 * @date 2017/5/27
 */
@Named
@RequestMapping("/mobile/apply")
public class AgentMobileApplyController extends AbstractAgentController{

    @Inject
    private AgentApplyManagementService agentApplyManagementService;
    @Inject
    private SchoolClueService schoolClueService;
    @Inject
    private DataReportApplyLoader dataReportApplyLoader;

    @RequestMapping("index.vpage")
    public String index(Model model){
        List<AbstractBaseApply> applyList = agentApplyManagementService.fetchUserApplyList(getCurrentUserId(), ApplyStatus.PENDING, false);
        Map<String, Long> pendingMap = applyList.stream().collect(Collectors.groupingBy(p -> p.getApplyType().name(), Collectors.counting()));
        Map<String, List<CrmSchoolClue>> schoolClueMap = schoolClueService.userSchoolClues(getCurrentUserId());
        List<CrmSchoolClue> schoolClueList = schoolClueMap.get("待审核");
        pendingMap.put("AGENT_SCHOOL_AUTH", CollectionUtils.isEmpty(schoolClueList)? 0 : (long)schoolClueList.size());
        model.addAttribute("pendingMap", pendingMap);
        return "rebuildViewDir/mobile/apply/my_apply";
    }

    // 申请列表页
    @RequestMapping("list.vpage")
    public String list(Model model){
        Long userId = getCurrentUserId();
        String applyTypeStr = getRequestString("applyType");
        ApplyType applyType = ApplyType.nameOf(applyTypeStr);
        if(applyType == null){
            applyType = ApplyType.AGENT_MODIFY_DICT_SCHOOL;
        }

        List<ApplyWithProcessResultData> dataList = agentApplyManagementService.getApplyListByTypeAndStatus(userId, applyType, null);
        dataList = dataList.stream().filter(item ->{
            AbstractBaseApply apply = item.getApply();
            if (apply instanceof AgentOrder){
                AgentOrder agentOrder = (AgentOrder)apply;
                if (Objects.equals(AgentOrderStatus.DRAFT.getStatus(), agentOrder.getOrderStatus())){
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
        Map<String, List<ApplyWithProcessResultData>> dataMap = dataList.stream().collect(Collectors.groupingBy(p -> {
            ApplyStatus status = p.getApply().getStatus();
            if(status == ApplyStatus.PENDING){
                return "PENDING";
            }else if(status == ApplyStatus.APPROVED || status == ApplyStatus.REJECTED){
                return "COMPLETED";
            }else{
                return "REVOKED";
            }
        }, Collectors.toList()));

        model.addAttribute("dataMap", dataMap);
        model.addAttribute("applyType", applyType);

        if(applyType == ApplyType.AGENT_MODIFY_DICT_SCHOOL){  // 字典表调整
            return  "rebuildViewDir/mobile/my/dictionary";
        }else if(applyType == ApplyType.AGENT_MATERIAL_APPLY){ // 商品购买
            return  "rebuildViewDir/mobile/my/purchase";
        }else if(applyType == ApplyType.AGENT_UNIFIED_EXAM_APPLY){ // 统考申请
            return  "rebuildViewDir/mobile/my/examination";
        }else if(applyType == ApplyType.AGENT_DATA_REPORT_APPLY){ // 大数据报告
            return  "rebuildViewDir/mobile/my/dataReport";
        }
        return "rebuildViewDir/mobile/my/dictionary";
    }

    // 申请详情页
    @RequestMapping("apply_detail.vpage")
    public String applyDetail(Model model){
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
            } catch (Exception e) {
            }
        }

        ApplyWithProcessResultData applyDetail = null;
        if (applyId != null) {
            applyDetail = agentApplyManagementService.getApplyDetailWithProcessResultByApplyId(applyType, applyId);
        }else {
            applyDetail = agentApplyManagementService.getApplyDetailByWorkflowId(applyType, workflowId, true);
        }
        model.addAttribute("applyData", applyDetail);
        if (ApplyType.AGENT_MODIFY_DICT_SCHOOL == applyType) {  // 字典表申请
            return "rebuildViewDir/mobile/apply/dictionaryInfo";
        } else if (ApplyType.AGENT_MATERIAL_APPLY == applyType) {  // 物料购买申请
            return "rebuildViewDir/mobile/apply/purchaseInfo";
        } else if (ApplyType.AGENT_UNIFIED_EXAM_APPLY == applyType) {  // 统考申请
            return "rebuildViewDir/mobile/apply/examinationInfo";
        } else if (ApplyType.AGENT_DATA_REPORT_APPLY == applyType) {  // 大数据报告申请
            String account = applyDetail.getApply().getAccount();
            List<DataReportApply> historyApplies = dataReportApplyLoader.loadByAccount(SystemPlatformType.AGENT, account);
            model.addAttribute("historyApplies", historyApplies.size());
            return "rebuildViewDir/mobile/apply/dataReportInfo";
        }

        return "rebuildViewDir/mobile/apply/dataReportInfo";
    }

    @RequestMapping("application.vpage")
    public String application(Model model){
        return "rebuildViewDir/mobile/apply/application";
    }

}
