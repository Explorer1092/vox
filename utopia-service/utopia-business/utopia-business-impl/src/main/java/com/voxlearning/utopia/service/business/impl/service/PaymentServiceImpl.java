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

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.business.api.PaymentService;
import com.voxlearning.utopia.entity.payment.PaymentUmpayBilling;
import com.voxlearning.utopia.service.business.impl.dao.PaymentUmpayBillingPersistence;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@Service(interfaceClass = PaymentService.class)
@ExposeService(interfaceClass = PaymentService.class)
public class PaymentServiceImpl extends BusinessServiceSpringBean implements PaymentService {

    @Inject
    private PaymentUmpayBillingPersistence paymentUmpayBillingPersistence;

    @Override
    public Long recordUmpayBilling(PaymentUmpayBilling.BillingType billingType, String billingDay, String billingContent) {
        PaymentUmpayBilling billing = PaymentUmpayBilling.newPaymentUmpayBilling(billingType, billingDay, billingContent);
        return paymentUmpayBillingPersistence.persistWithoutDuplication(billing);
    }
}
