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

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.payment.PaymentUmpayBilling;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestPaymentUmpayBillingPersistence {

    @Inject private PaymentUmpayBillingPersistence paymentUmpayBillingPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = PaymentUmpayBilling.class)
    public void testPersistWithoutDuplication() throws Exception {
        PaymentUmpayBilling billing = new PaymentUmpayBilling();
        billing.setCreateDatetime(new Date());
        billing.setBillingType(PaymentUmpayBilling.BillingType.Settlement);
        billing.setBillingDay("2013-12-03");
        billing.setBillingContent("CONTENT");
        Long id = paymentUmpayBillingPersistence.persistWithoutDuplication(billing);
        billing = paymentUmpayBillingPersistence.loadFromDatabase(id);
        assertNotNull(billing);
    }
}
