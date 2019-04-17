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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.api.constant.OperationType;
import com.voxlearning.utopia.api.constant.ReferenceType;
import com.voxlearning.utopia.service.business.api.entity.UserBalanceLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")

public class TestUserBalanceLogPersistence {

    @Inject private UserBalanceLogPersistence userBalanceLogPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = UserBalanceLog.class)
    public void testIsUserBalanceLogOperationExtIdExisting() throws Exception {

        UserBalanceLog log = new UserBalanceLog();

        log.setUserId(10001L);
        log.setOperationMethod("OPERATION METHOD");
        log.setOperationExtId("OPERATION EXT ID");
        log.setOperationType(OperationType.Credit);
        log.setAmount(10.0D);
        log.setReferenceType(ReferenceType.AfentiOrder);
        userBalanceLogPersistence.insert(log);

        log = new UserBalanceLog();
        log.setUserId(10001L);
        log.setOperationMethod("OPERATION METHOD");
        log.setOperationExtId("OPERATION EXT ID");
        assertTrue(userBalanceLogPersistence.isUserBalanceLogOperationExtIdExisting(log));
    }
}
