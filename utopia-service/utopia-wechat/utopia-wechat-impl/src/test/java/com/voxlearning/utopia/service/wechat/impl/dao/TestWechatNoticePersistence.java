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

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * @author xin.xin
 * @since 14-5-23 上午10:48
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestWechatNoticePersistence {
    @Inject
    private WechatNoticePersistence wechatNoticePersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = WechatNotice.class)
    public void testFindByMessageTypeAndState() {
        wechatNoticePersistence.persist(WechatNotice.newInstance(1L, "testopenid 1", "test message 1", WechatNoticeType.HOMEWORK_CHECK, WechatNoticeState.WAITTING));
        assertEquals(1, wechatNoticePersistence.findByMessageTypeAndState(WechatNoticeType.HOMEWORK_CHECK.getType(), WechatNoticeState.WAITTING).size());
        wechatNoticePersistence.persist(WechatNotice.newInstance(2L, "testopenid 2", "test message 2", WechatNoticeType.HOMEWORK_CHECK, WechatNoticeState.WAITTING));
        assertEquals(2, wechatNoticePersistence.findByMessageTypeAndState(WechatNoticeType.HOMEWORK_CHECK.getType(), WechatNoticeState.WAITTING).size());
        wechatNoticePersistence.persist(WechatNotice.newInstance(3L, "testopenid 3", "test message 3", WechatNoticeType.HOMEWORK_CHECK, WechatNoticeState.WAITTING));
        assertEquals(3, wechatNoticePersistence.findByMessageTypeAndState(WechatNoticeType.HOMEWORK_CHECK.getType(), WechatNoticeState.WAITTING).size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = WechatNotice.class)
    public void testUpdateMessageState() {
        WechatNotice notice = WechatNotice.newInstance(1L, "testopenid 1", "test message 1", WechatNoticeType.HOMEWORK_CHECK, WechatNoticeState.WAITTING);
        notice.setMessageId("123");
        Long id = wechatNoticePersistence.persist(notice);
        notice = wechatNoticePersistence.loadFromDatabase(id);
        assertSame(WechatNoticeState.WAITTING, WechatNoticeState.of(notice.getState()));
        wechatNoticePersistence.updateMessageState("testopenid 1", "123", WechatNoticeState.SUCCESS, "");
        notice = wechatNoticePersistence.loadFromDatabase(id);
        assertSame(WechatNoticeState.SUCCESS, WechatNoticeState.of(notice.getState()));
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = WechatNotice.class)
    public void testUpdateMessageId() {
        Long id = wechatNoticePersistence.persist(WechatNotice.newInstance(1L, "testopenid", "testmessage", WechatNoticeType.TEMPLATE_HOMEWORK_CHECK, WechatNoticeState.WAITTING));
        wechatNoticePersistence.updateMessageId(id, "123456");
        WechatNotice notice = wechatNoticePersistence.loadFromDatabase(id);
        assertSame(WechatNoticeState.SENDED, WechatNoticeState.of(notice.getState()));
        assertTrue(notice.getMessageId().equals("123456"));
    }
}
