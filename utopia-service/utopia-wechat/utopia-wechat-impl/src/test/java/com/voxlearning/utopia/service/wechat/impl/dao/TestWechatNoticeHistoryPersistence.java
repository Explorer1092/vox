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

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNoticeHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = WechatNoticeHistory.class)
public class TestWechatNoticeHistoryPersistence {

    @Inject private WechatNoticeHistoryPersistence wechatNoticeHistoryPersistence;

    @Test
    public void testListAllByUserId() throws Exception {
        long userId = 30009;
        for (int i = 0; i < 5; i++) {
            WechatNoticeHistory history = new WechatNoticeHistory();
            history.setUserId(userId);
            history.setMessage("");
            history.setMessageType(0);
            history.setState(0);
            history.setSendTime(new Date());
            history.setExpireTime(new Date());
            wechatNoticeHistoryPersistence.insert(history);
            assertEquals(i + 1, wechatNoticeHistoryPersistence.listAllByUserId(userId).size());
        }
    }
}
