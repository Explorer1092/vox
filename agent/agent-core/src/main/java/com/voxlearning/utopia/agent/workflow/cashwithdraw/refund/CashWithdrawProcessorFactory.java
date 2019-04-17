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

package com.voxlearning.utopia.agent.workflow.cashwithdraw.refund;

import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.agent.workflow.WorkFlowProcessor;
import com.voxlearning.utopia.agent.workflow.WorkFlowProcessorFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Yuechen.Wang on 2016/4/1.
 */
@Named
public class CashWithdrawProcessorFactory implements WorkFlowProcessorFactory {

    @Inject private CashWithdrawInitProcessor cashWithdrawInitProcessor;
    @Inject private CashWithdrawFinancialProcessor cashWithdrawFinancialProcessor;

    public static Long CRM_ADMIN_TEST_GROUP_ID = -1L; // FIXME
    public static Long CRM_ADMIN_GROUP_ID = -1L; // FIXME

    @Override
    public WorkFlowProcessor getProcessor(WorkFlowContext context) {
        switch (AgentOrderStatus.of(context.getOrder().getOrderStatus())) {
            case INIT:
                return cashWithdrawInitProcessor;
            case PENDING_FINANCIAL:
                return cashWithdrawFinancialProcessor;
            default:
                return null;
        }
    }

    public static Long getCrmAdminGroupId(){
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            return  CRM_ADMIN_GROUP_ID;
        }
        else if (RuntimeMode.isProduction() || RuntimeMode.isProduction()) {
            return CRM_ADMIN_TEST_GROUP_ID;
        } else {
            return 0L;
        }
    }

}
