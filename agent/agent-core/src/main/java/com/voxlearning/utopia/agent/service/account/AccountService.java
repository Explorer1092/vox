/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.service.account;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.bean.incomes2016.UserIncomeS2016Bean;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016Persistence;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/15.
 */
@Named
public class AccountService extends AbstractAgentService {

    @Inject private BaseUserService baseUserService;
    @Inject private AgentUserKpiResultSpring2016Persistence agentUserKpiResultSpring2016Persistence;


    public UserIncomeS2016Bean getUserIncome(Integer salaryMonth, Long userId) {
        AgentUser user = baseUserService.getById(userId);
        if (user == null) {
            return null;
        }

        UserIncomeS2016Bean incomeBean = new UserIncomeS2016Bean(userId, user.getRealName());

        // 查询所有的VOX_USER_KPI_RESULT信息
        List<AgentUserKpiResultSpring2016> resultList = null;
        if (salaryMonth == 0) {
            resultList = agentUserKpiResultSpring2016Persistence.findUserIncome(userId);
        } else {
            resultList = agentUserKpiResultSpring2016Persistence.findUserIncome(salaryMonth, userId);
        }

        for (AgentUserKpiResultSpring2016 kpiResult : resultList) {
            incomeBean.appendIncome(kpiResult);
        }

        return incomeBean;
    }

    public Map<String, Object> getInvoiceList(Long userId) {
        AgentUser user = baseUserService.getById(userId);
        if (user == null) {
            return Collections.emptyMap();
        }

        List<AgentUserKpiResultSpring2016> resultList = agentUserKpiResultSpring2016Persistence.findUserIncome(userId);

        Map<String, Object> retData = new LinkedHashMap<>();
        for (AgentUserKpiResultSpring2016 kpiResult : resultList) {
            String key = String.valueOf(kpiResult.getSalaryMonth());
            if (!retData.containsKey(key)) {
                retData.put(key, key);
            }
        }

        return retData;
    }

    /**
     * FIXME 关于这个方法。。。说好的直接用SchoolLevel呢。。。
     */
    public UserIncomeS2016Bean getUserIncomeByType(Integer salaryMonth, Long userId, List<String> types) {
        AgentUser user = baseUserService.getById(userId);
        if (user == null) {
            return null;
        }
        if (CollectionUtils.isEmpty(types)) {
            return null;
        }

        UserIncomeS2016Bean incomeBean = new UserIncomeS2016Bean(userId, user.getRealName());

        // 查询所有的VOX_USER_KPI_RESULT信息
        List<AgentUserKpiResultSpring2016> resultList = null;
        if (salaryMonth == 0) {
            resultList = agentUserKpiResultSpring2016Persistence.findUserIncome(userId);
        } else {
            resultList = agentUserKpiResultSpring2016Persistence.findUserIncome(salaryMonth, userId);
        }

        resultList = resultList.stream()
                .filter(r -> r.getCpaType() != null && types.contains(r.getCpaType()))
                .collect(Collectors.toList());

        for (AgentUserKpiResultSpring2016 kpiResult : resultList) {
            incomeBean.appendIncome(kpiResult);
        }

        return incomeBean;
    }

}