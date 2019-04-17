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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.dao.jdbc.persistence.AbstractEntityPersistence;
import com.voxlearning.utopia.entity.payment.PaymentUmpayBilling;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Named;


@Named
public class PaymentUmpayBillingPersistence extends AbstractEntityPersistence<Long, PaymentUmpayBilling> {

    public Long persistWithoutDuplication(PaymentUmpayBilling billing) {

        String contentMd5 = DigestUtils.md5Hex(billing.getBillingContent());
        PaymentUmpayBilling existingBill = withSelectFromTable("ID", "WHERE BILLING_TYPE=? AND BILLING_DAY=? AND MD5(BILLING_CONTENT) = ?")
                .useParamsArgs(billing.getBillingType(), billing.getBillingDay(), contentMd5).queryObject();

        if (existingBill == null) {
            return persist(billing);
        }

        return null;
    }
}
