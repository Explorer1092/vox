/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.service.legacy.AfentiAdminService;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowRefer;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.FinanceFlowContext;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.utopia.service.userlevel.api.UserLevelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Finance Controller
 * Created by Shuai Huan on 2014/9/23.
 */
@Controller
@RequestMapping("/crm/finance")
public class CrmFinanceController extends CrmAbstractController {
    public static final String CONFIG_CATEGORY_NAME = "PRIMARY_PLATFORM_GENERAL";
    public static final String CONFIG_KEY_ORDER_REFUND_NOTIFY_EMAIL_KEFU = "ORDER_REFUND_EMAIL_KEFU";

    @Inject private AfentiAdminService afentiAdminService;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;
    @ImportService(interfaceClass = UserLevelService.class)
    private UserLevelService userLevelService;
    @Inject private FinanceServiceClient financeServiceClient;

    @RequestMapping(value = "/financedetail.vpage", method = RequestMethod.GET)
    public String index(Model model) {

        Long userId = getRequestLong("userId");
        User user = userLoaderClient.loadUser(userId);
        Finance finance = financeServiceClient.getFinanceService()
                .loadUserFinance(userId)
                .getUninterruptibly();
        model.addAttribute("balance", finance == null ? 0 : finance.getBalance());
        List<FinanceFlow> flows = financeServiceClient.getFinanceService()
                .findUserFinanceFlows(userId)
                .getUninterruptibly();
        Collections.sort(flows, (o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime()));
        List<Map<String, Object>> flowList = new ArrayList<>();
        for (FinanceFlow flow : flows) {
            Map<String, Object> flowMap = new HashMap<>();
            flowMap.put("financeFlow", flow);
            // 兼容旧数据,数据里outerId有可能是订单ID
            if (StringUtils.isNotBlank(flow.getOrderId())) {
                String[] orderIdArray = StringUtils.split(flow.getOrderId(), UserOrder.SEP);
                if (orderIdArray.length > 1) {
                    UserOrder userOrder = userOrderLoaderClient.loadUserOrder(flow.getOrderId());
                    if (null == userOrder && StringUtils.isNotBlank(flow.getOuterId())) {
                        orderIdArray = StringUtils.split(flow.getOuterId(), UserOrder.SEP);
                        if (orderIdArray.length > 1) {
                            userOrder = userOrderLoaderClient.loadUserOrder(flow.getOuterId());
                        }
                    }
                    flowMap.put("afentiOrder", userOrder);
                }
            }
            flowList.add(flowMap);
        }
        model.addAttribute("user", user);
        model.addAttribute("flows", flowList);
        return "crm/finance/financedetail";
    }


    @RequestMapping(value = "/addbalance.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addIntegralHistory() {

        Long userId = getRequestLong("userId");
        Double balance = getRequestDouble("balance", 0);
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return MapMessage.errorMessage("用户不存在！");
        }

        try {
            FinanceFlowContext context = FinanceFlowContext.instance()
                    .userId(userId)
                    .type(balance > 0 ? FinanceFlowType.Deposit : FinanceFlowType.Debit)
                    .state(FinanceFlowState.SUCCESS)
                    .amount(new BigDecimal(Math.abs(balance)))
                    .payAmount(new BigDecimal(Math.abs(balance)))
                    .payMethod("admin")
                    .refer(FinanceFlowRefer.CRM);
            boolean result = balance > 0 ? financeServiceClient.getFinanceService()
                    .deposit(context).getUninterruptibly() : financeServiceClient.getFinanceService()
                    .debit(context).getUninterruptibly();
            if (!result) return MapMessage.errorMessage("修改余额失败");
        } catch (Exception ex) {
            return MapMessage.errorMessage("操作失败！");
        }

        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() +
                "为用户：" + user.getProfile().getRealname() + " 增加用户作业币：" + balance;
        addAdminLog(operation, userId);

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("增加作业币");
        userServiceRecord.setComments(operation);
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        return MapMessage.successMessage("操作成功！");
    }

    @RequestMapping(value = "/withdraw.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage withdraw(@RequestParam Long userId, @RequestParam Double amount) {
        try {
            if (amount <= 0) {
                return MapMessage.errorMessage("请输入有效金额");
            }

            User user = userLoaderClient.loadUser(userId);
            if (user == null) {
                return MapMessage.errorMessage("用户不存在！");
            }

            Finance finance = financeServiceClient.getFinanceService()
                    .createUserFinanceIfAbsent(userId)
                    .getUninterruptibly();
            if (null == finance || finance.getBalance().subtract(BigDecimal.valueOf(amount)).doubleValue() < 0) {
                return MapMessage.errorMessage("用户余额不足");
            }

            List<FinanceFlow> flows = afentiAdminService.getDepositFinanceFlow(userId);
            if (CollectionUtils.isEmpty(flows)) {
                return MapMessage.errorMessage("提现失败,用户没有充值流水");
            }

            if (generateWithdrawFlow(userId, amount)) {
                String operation = "管理员" + getCurrentAdminUser().getAdminUserName() +
                        "为用户：" + user.getProfile().getRealname() + " 提现作业币：" + amount;
                addAdminLog(operation, userId);
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(userId);
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("作业币提现");
                userServiceRecord.setComments(operation);
                userServiceClient.saveUserServiceRecord(userServiceRecord);

                if (user.fetchUserType() == UserType.PARENT) {
                    userLevelService.parentWithdraw(userId, BigDecimal.valueOf(amount));
                }
                
                return MapMessage.successMessage("提现成功，请联系财务走退款流程。");
            } else {
                return MapMessage.errorMessage("提现失败");
            }
        } catch (Exception ex) {
            logger.error("withdraw from user balance error:{},{}", userId, amount, ex);
            return MapMessage.errorMessage("余额提现错误");
        }
    }

    private boolean generateWithdrawFlow(@RequestParam Long userId, @RequestParam Double amount) {
        FinanceFlowContext context = FinanceFlowContext.instance()
                .amount(BigDecimal.valueOf(amount))
                .payAmount(BigDecimal.valueOf(amount))
                .type(FinanceFlowType.Withdraw)
                .refer(FinanceFlowRefer.CRM)
                .state(FinanceFlowState.SUCCESS)
                .userId(userId);

        return financeServiceClient.getFinanceService().withdraw(context).getUninterruptibly();
    }

//    private MapMessage sendWithdrawTask(Long userId, Double amount) {
//        Map<String, String> paymethods = new HashMap<>();
//        List<FinanceFlow> flows = afentiAdminService.getDepositFinanceFlow(userId);
//        for (FinanceFlow flow : flows) {
//            paymethods.put(flow.getOuterId(), flow.getSource());
//        }
//
//        User user = userLoaderClient.loadUser(userId);
//        Map<Object, Object> postData = new HashMap<>();
//        postData.put("userId", userId);
//        postData.put("userName", user.getProfile().getRealname());
//        postData.put("adminUser", getCurrentAdminUser().getAdminUserName());
//        postData.put("adminUserName", getCurrentAdminUser().getRealName());
//        postData.put("amount", amount);
//        postData.put("transactionIds", JsonUtils.toJson(paymethods));
//
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
//                .post(getMarketingUrl() + "/task/crm/cashwithdraw.vpage")
//                .addParameter(postData)
//                .execute();
//
//        if (response.hasHttpClientException()) {
//            return MapMessage.errorMessage("提现失败,未能生成财务提现任务");
//        }
//
//        return JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
//    }

//    //财务操作完成,给客服发邮件
//    private void sendWithdrawSuccessEmail(Long userId, Double amount) {
//        String to = getNotifyEmailConfig_KeFu();
//        Objects.requireNonNull(to);
//
//        new EmailServiceClient(emailService).createPlainEmail()
//                .to(to)
//                .cc("chen.ling@17zuoye.com;li.xiao@17zuoye.com")
//                .subject("余额提现成功")
//                .body("用户(" + userId + ")提现成功,金额:" + amount + ",财务已处理完毕")
//                .send();
//    }
//
//    private void sendWithdrawFailEmail(Long userId, Double amount, String memo) {
//        String to = getNotifyEmailConfig_KeFu();
//        Objects.requireNonNull(to);
//
//        new EmailServiceClient(emailService).createPlainEmail()
//                .to(to)
//                .cc("chen.ling@17zuoye.com;li.xiao@17zuoye.com")
//                .subject("余额提现失败")
//                .body("用户(" + userId + ")提现失败,金额:" + amount + ",财务已拒绝,原因:" + memo)
//                .send();
//    }

    @RequestMapping(value = "/financeflows.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage financeFlows() {
        Long userId = getRequestLong("userId");
        String transactionIds = getRequestString("transactionIds");

        if (0 == userId || StringUtils.isBlank(transactionIds)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            List<String> tids = JsonUtils.fromJsonToList(transactionIds, String.class);

            List<FinanceFlow> flows = financeServiceClient.getFinanceService()
                    .findUserFinanceFlows(userId)
                    .getUninterruptibly()
                    .stream()
                    .filter(f -> f.getType().equals(FinanceFlowType.Deposit.name())
                            && StringUtils.isNotBlank(f.getOuterId())
                            && tids.contains(f.getOuterId()))
                    .collect(Collectors.toList());

            return MapMessage.successMessage().add("flows", flows);
        } catch (Exception ex) {
            logger.error("Get finance flow failed,tids:{}", transactionIds, ex);
            return MapMessage.errorMessage("查询充值流水失败");
        }
    }
}
