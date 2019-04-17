package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.sysconfig.ProductConfigService;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderPaymentMode;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderApplyHandler
 *
 * @author song.wang
 * @date 2017/1/15
 */
@Named
public class AgentOrderApplyHandler extends SpringContainerSupport {

    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private BaseUserService baseUserService;
    @Inject
    private ProductConfigService productConfigService;
    @Inject
    private AgentOrderLoaderClient agentOrderLoaderClient;
    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject
    private AgentNotifyService agentNotifyService;
    @Inject
    private AgentMaterialBudgetService agentMaterialBudgetService;
    @Inject
    private AgentTagService agentTagService;

    public void handle(Long workflowId, WorkFlowProcessResult processResult, Boolean hasFollowStatus){

        if(workflowId == null || processResult == null || hasFollowStatus == null){
            return;
        }
        AgentOrder agentOrder = agentOrderLoaderClient.findByWorkflowId(workflowId);
        if(agentOrder == null){
            return;
        }
        if(!hasFollowStatus){ // 工作流已审批结束，没有后续状态
            ApplyStatus applyStatus = null;
            AgentOrderStatus agentOrderStatus = null;
            if(WorkFlowProcessResult.agree == processResult){
                applyStatus = ApplyStatus.APPROVED;
                agentOrderStatus = AgentOrderStatus.APPROVED;
            }else if(WorkFlowProcessResult.reject == processResult){
                applyStatus = ApplyStatus.REJECTED;
                agentOrderStatus = AgentOrderStatus.REJECTED;
            }else if(WorkFlowProcessResult.revoke == processResult){
                applyStatus = ApplyStatus.REVOKED;
                agentOrderStatus = AgentOrderStatus.CANCELED;
            }
            if(applyStatus != null){ // 更新申请状态
                agentOrderServiceClient.updateApplyStatus(agentOrder.getId(), applyStatus);
            }
            if(agentOrderStatus != null){ // 更新订单状态
                agentOrderServiceClient.updateOrderStatus(agentOrder.getId(), agentOrderStatus);
            }

            if(ApplyStatus.REJECTED == applyStatus ||  ApplyStatus.REVOKED == applyStatus){ // 撤销或者被驳回的情况下

                // 获取审批历史
                List<WorkFlowProcessHistory> processHistoryList = workFlowLoaderClient.loadWorkFlowProcessHistoryByWorkFlowId(workflowId);
                if(CollectionUtils.isNotEmpty(processHistoryList)){
                    Collections.sort(processHistoryList, ((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())));
                }

                // 库存恢复
                Long operatorId = agentOrder.getCreator();
                String rejectNote = "";
                String rejectName = "";
                if(ApplyStatus.REJECTED == applyStatus && CollectionUtils.isNotEmpty(processHistoryList)){
                    operatorId = Long.valueOf(processHistoryList.get(0).getProcessorAccount()); // 获取驳回的用户Id
                    rejectNote = processHistoryList.get(0).getProcessNotes();
                    rejectName = processHistoryList.get(0).getProcessorName();
                }
                resetInventory(agentOrder, agentOrderStatus, operatorId);

                // 使用【物料费用】支付的订单，在驳回后需退还相应费用至申请人账户；
                // 使用【城市支持费用】、【自付】支付的订单，具体退款事宜通过线下处理，不在系统中进行；
                if(AgentOrderPaymentMode.MATERIAL_COST.getPayId().equals(agentOrder.getPaymentMode())){
                    // 订单金额退回
                    agentMaterialBudgetService.returnMaterialBalance(agentOrder);
                }
                if(AgentOrderPaymentMode.CITY_COST.getPayId().equals(agentOrder.getPaymentMode())){
                    // 订单金额退回
                    agentMaterialBudgetService.returnCityBalance(agentOrder);
                }

                if(ApplyStatus.REJECTED == applyStatus){ // 订单被驳回的情况下， 发送短信通知
                    // 给订单创建者发短信通知
                    Long creator = agentOrder.getCreator();
                    if (creator == null) {
                        return;
                    }
                    AgentUser agentUser = baseOrgService.getUser(agentOrder.getCreator());
                    if(agentUser != null && StringUtils.isNotBlank(agentUser.getTel())){
                        SmsMessage smsMessage = new SmsMessage();
                        smsMessage.setMobile(agentUser.getTel());
                        smsMessage.setType(SmsType.MARKET_ADD_AGENT_USER.name());
                        DateUtils.dateToString(agentOrder.getCreateDatetime(), "MM月dd日");
                        smsMessage.setSmsContent("您于" + DateUtils.dateToString(agentOrder.getCreateDatetime(), "MM月dd日") +"发起的物料申请由于「" + rejectNote +"」的原因被驳回，订单号为" + agentOrder.getId()+ "。");
                        smsServiceClient.getSmsService().sendSms(smsMessage);
                    }

                    // 给审批人员发短信
                    if(CollectionUtils.isNotEmpty(processHistoryList)){
                        List<String> telList = processHistoryList.stream().filter(p -> StringUtils.isNotBlank(p.getProcessorAccount())).map(p -> baseOrgService.getUser(Long.valueOf(p.getProcessorAccount())))
                                .filter(p -> p != null).map(AgentUser::getTel).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                        if(CollectionUtils.isNotEmpty(telList)){
                            for(String tel : telList){
                                SmsMessage smsMessage = new SmsMessage();
                                smsMessage.setMobile(tel);
                                smsMessage.setType(SmsType.MARKET_ADD_AGENT_USER.name());
                                DateUtils.dateToString(agentOrder.getCreateDatetime(), "MM月dd日");
                                smsMessage.setSmsContent((agentOrder.getCreatorName() == null ? "" : agentOrder.getCreatorName()) + "的物料申请由于「" + rejectNote + "」的原因被驳回，订单号为" + agentOrder.getId() + "。");
                                smsServiceClient.getSmsService().sendSms(smsMessage);
                            }
                        }
                    }

                    String content =StringUtils.formatMessage("您于{}提交的订单（订单号：{}，金额：{}元）申请被驳回。\r\n" +
                            "驳回原因：{}【驳回人：{}】。", DateUtils.dateToString(agentOrder.getCreateDatetime(), "YYYY年MM月dd日"),agentOrder.getId(),agentOrder.getOrderAmount(), rejectNote , rejectName);

                    // 发送站内通知
                    List<Long> tagIds = agentTagService.getNotifyTagIdsByName("驳回");
                    agentNotifyService.sendNotifyWithTags(AgentNotifyType.ORDER_NOTICE.getType(), "物料购买", content ,
                            Collections.singleton(agentOrder.getCreator()), null, null, null, tagIds);
                }
            }else if(ApplyStatus.APPROVED == applyStatus){
                String content=StringUtils.formatMessage("您于{}提交的订单（订单号：{}，金额：{}元）已审批通过，等待发货。",DateUtils.dateToString(agentOrder.getCreateDatetime(), "YYYY年MM月dd日"),agentOrder.getId(),agentOrder.getOrderAmount());
                // 发送站内通知
                agentNotifyService.sendNotify(AgentNotifyType.ORDER_NOTICE.getType(), "物料购买", content,
                        Collections.singleton(agentOrder.getCreator()), null);
            }

        }
    }

    private void resetInventory(AgentOrder agentOrder, AgentOrderStatus agentOrderStatus, Long operatorId){
        // 更新库存
        List<AgentOrderProduct> orderProductList = agentOrder.getOrderProductList();
        if (CollectionUtils.isEmpty(orderProductList)) {
            return;
        }

        String quantityChangeDesc;
        if(AgentOrderStatus.REJECTED == agentOrderStatus ){
            quantityChangeDesc = "【驳回订单】";
        }else if(AgentOrderStatus.CANCELED == agentOrderStatus){
            quantityChangeDesc = "【取消订单】";
        }else {
            return;
        }

        List<AgentGroup> groupList = baseOrgService.getUserGroups(agentOrder.getCreator());
        String groupName = "";
        if(CollectionUtils.isNotEmpty(groupList)){
            groupName = groupList.get(0).getGroupName();
        }
        AgentUser user = baseOrgService.getUser(agentOrder.getCreator());

        quantityChangeDesc += "订单号：" + agentOrder.getId() + "(" + (StringUtils.isBlank(groupName)? "" : groupName + " - ") + (user == null? "" : user.getRealName()) + ")";

        for(AgentOrderProduct p : orderProductList) {
            AgentProduct product = productConfigService.getById(p.getProductId());
            if(product == null){
                continue;
            }
            Integer preQuantity = product.getInventoryQuantity() == null ? 0: product.getInventoryQuantity();
            product.setInventoryQuantity(product.getInventoryQuantity() + p.getProductQuantity());
            productConfigService.updateProduct(product);

            // 添加库存变更记录
            productConfigService.addAgentProductInventoryRecord(operatorId, product.getId(), preQuantity, product.getInventoryQuantity(), p.getProductQuantity(), quantityChangeDesc);
        }
    }

    /*private void updateUserCash(AgentOrder agentOrder, Long operatorId){
        // 更新用户账户可用余额
        Long creator = agentOrder.getCreator();
        if (creator == null || creator == 0) {
            return;
        }
        AgentUser agentUser = baseOrgService.getUser(agentOrder.getCreator());
        if (agentUser != null) {
            float preCashAmount = agentUser.getCashAmount();
            agentUser.setUsableCashAmount(MathUtils.floatAdd(agentUser.getUsableCashAmount(), agentOrder.getOrderAmount()));
            agentUser.setCashAmount(MathUtils.floatAdd(agentUser.getCashAmount(), agentOrder.getOrderAmount()));
            baseUserService.updateAgentUser(agentUser);

            // 添加用户余额变动记录
            baseUserService.addAgentUserCashDataRecord(2, agentUser.getId(), operatorId, preCashAmount, agentUser.getCashAmount(), agentOrder.getOrderAmount() , "【物料退款】订单号：" + agentOrder.getId());
        }
    }*/


}
