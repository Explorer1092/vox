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

package com.voxlearning.utopia.entity.payment;

import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@DocumentTable(table = "VOX_PAYMENT_UMPAY_BILLING")
public class PaymentUmpayBilling implements Serializable, TimestampTouchable {

    private static final long serialVersionUID = -2715373475586619448L;
    @UtopiaSqlColumn(primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    private Long id;
    @UtopiaSqlColumn private Date createDatetime;
    @UtopiaSqlColumn private BillingType billingType;
    @UtopiaSqlColumn private String billingDay;
    @UtopiaSqlColumn private String billingContent;

    @Override
    public void touchCreateTime(long timestamp) {
        if (getCreateDatetime() == null) {
            setCreateDatetime(new Date(timestamp));
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
    }

    public static PaymentUmpayBilling newPaymentUmpayBilling(BillingType billingType, String billingDay, String billingContent) {
        PaymentUmpayBilling billing = new PaymentUmpayBilling();
        billing.createDatetime = new Date();
        billing.billingType = billingType;
        billing.billingDay = billingDay;
        billing.billingContent = billingContent;
        return billing;
    }

    public static enum BillingType {
        Transaction, Settlement;
    }
}
