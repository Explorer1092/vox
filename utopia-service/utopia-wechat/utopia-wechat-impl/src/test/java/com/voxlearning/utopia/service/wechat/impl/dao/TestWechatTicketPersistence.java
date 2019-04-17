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

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTicketType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = WechatTicket.class)
public class TestWechatTicketPersistence {
    @Inject private WechatTicketPersistence wechatTicketPersistence;

    @Test
    public void testLoadByUserIdAndTicketType() throws Exception {
        WechatTicket document = new WechatTicket();
        document.setUserId(0L);
        document.setTicketType(WechatTicketType.TEACHER_FACE2FACE_INVITE);
        document.setTicket("");
        wechatTicketPersistence.insert(document);
        assertNotNull(wechatTicketPersistence.loadByUserIdAndTicketType(0L, "TEACHER_FACE2FACE_INVITE"));
    }
}
