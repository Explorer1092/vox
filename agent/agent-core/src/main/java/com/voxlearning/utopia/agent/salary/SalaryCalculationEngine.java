/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.salary;

import lombok.NoArgsConstructor;

import javax.inject.Named;

/**
 * 代理工资结算的计算引擎
 * Created by Alex on 15-2-27.
 */
@Named
@NoArgsConstructor
public class SalaryCalculationEngine {

//    @Inject
//    private AgentRegionService agentRegionService;
//    @Inject
//    private BaseUserService baseUserService;
//    @Inject
//    private UserKpiService userKpiService;
//    @Inject
//    private AuthnumSalaryCalculator authnumSalaryCalculator;
//    @Inject
//    private OnlineShareSalaryCalculator onlineShareSalaryCalculator;
//
//    @Getter
//    @Setter
//    private SalaryCalculatorContext context;
//
//    // 计算用户工资
//    public void calculateSalary(List<AgentUser> userList) {
//        /* 此处代码冗余，CalSalaryJob.executeTask - userKpiService.deleteOnlineShareResult(runDate) 已做了历史数据的批量删除
//        // 1.清理所有用户旧的历史数据
//        for (AgentUser user : userList) {
//            // 工资计算相关交由SalaryCalculator完成
//            List<AgentUserKpiResult> userKpiResultList = userKpiService.getConfirmedKpiResult(context.getRunTime(), user.getId());
//            if (CollectionUtils.isNotEmpty(userKpiResultList)) {
//                for (AgentUserKpiResult userKpiResult : userKpiResultList) {
//                    baseUserService.updateUserPointAmountForRecalSalary(userKpiResult);
//                }
//            }
//            userKpiService.deleteUserKpiResult(context.getRunTime(), user.getId());
//            *//*
//            userKpiService.deleteOnlineShareResult(context.getRunTime(), user.getId());
//        }
//        */
//
//        // 2. 过滤用户
//        userList = userList.stream()
//                .filter(source -> {
//                    if ((!source.isValidUser() && source.getContractEndDate() == null)
//                            || (source.getContractEndDate() != null && source.getContractEndDate().before(context.getStartTime()))) {
//                        return false;
//                    }
//                    return true;
//                })
//                .collect(Collectors.toList());
//
//        /* 工资计算相关交由SalaryCalculator完成
//        // 3. 计算新增认证用户的工资
//        for (AgentUser user : userList) {
//            authnumSalaryCalculator.calculate(user, context);
//        }
//        */
//
//        // 4. 计算线上付费分成的工资
//        for (AgentUser user : userList) {
//            onlineShareSalaryCalculator.calculateCityShare(user, context);
//        }
//
//        // 只计算代理的付费分成
////        for (AgentUser user : userList) {
////            onlineShareSalaryCalculator.calculateProvinceShare(user, context);
////        }
//    }
//
//
////    public void calculateSalary(AgentUser user) {
////        // 清除旧的记录数据
////        List<AgentUserKpiResult> userKpiResultList = userKpiService.getConfirmedKpiResult(context.getRunTime(), user.getId());
////        if (CollectionUtils.isNotEmpty(userKpiResultList)) {
////            for (AgentUserKpiResult userKpiResult : userKpiResultList) {
////                baseUserService.updateUserPointAmountForRecalSalary(userKpiResult);
////            }
////        }
////
////        userKpiService.deleteUserKpiResult(context.getRunTime(), user.getId());
////        userKpiService.deleteOnlineShareResult(context.getRunTime(), user.getId());
////
////        // 计算用户是否需要计算工资
////        if ((!user.isValidUser() && user.getContractEndDate() == null)
////                || (user.getContractEndDate() != null && user.getContractEndDate().before(context.getStartTime()))) {
////            return;
////        }
////
////        // 计算该代理的新增认证用户的工资
////        authnumSalaryCalculator.calculate(user, context);
////
////        // 计算线上付费分成的工资
////        onlineShareSalaryCalculator.calculate(user, context);
////    }
//
//    /**
//     * 初始化工资结算的计算引擎, 从系统中Load各种信息保存到SalaryCalculatorContext中,以便于后续计算
//     */
//    public void init(Date runTime, List<AgentKpiEval> kpiEvalList) {
//
//        context = new SalaryCalculatorContext();
//        context.setKpiEvals(kpiEvalList);
//        context.setRunTime(runTime);
//        context.setStartTime(kpiEvalList.get(0).getEvalDurationFrom());
//        context.setEndTime(kpiEvalList.get(0).getEvalDurationTo());
//
//        // 所有的区域信息
//        context.setRegionTree(agentRegionService.getAllRegionTreeCopy());
//
//        // 所有地区的月付费率信息
//        Integer startMonth = ConversionUtils.toInt(DateUtils.dateToString(context.getStartTime(), "yyyyMM"));
//        context.setMonthPaymentRates(userKpiService.loadAllMonthPaymentRateData(startMonth));
//    }

}
