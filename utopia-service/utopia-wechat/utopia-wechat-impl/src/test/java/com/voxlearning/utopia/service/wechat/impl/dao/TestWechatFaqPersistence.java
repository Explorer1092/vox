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
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaq;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author xin.xin
 * @since 2014-04-18
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = WechatFaq.class)
public class TestWechatFaqPersistence {

    @Inject private WechatFaqPersistence wechatFaqPersistence;

    @Test
    public void testFindByCatalog() {
        for (int i = 0; i < 5; i++) {
            WechatFaq faq = new WechatFaq();
            faq.setCatalogId(1L);
            faq.setTitle("");
            faq.setKeyWord("");
            faq.setContent("");
            faq.setStatus("published");
            faq.setType(1);
            wechatFaqPersistence.insert(faq);
            assertEquals(i + 1, wechatFaqPersistence.findByCatalog(1L, 1).size());
        }
    }

    @Test
    public void testFindByTitleLike() throws Exception {
        for (int i = 0; i < 5; i++) {
            WechatFaq faq = new WechatFaq();
            faq.setCatalogId(0L);
            faq.setTitle("");
            faq.setKeyWord("XaX");
            faq.setContent("");
            faq.setStatus("published");
            faq.setType(1);
            wechatFaqPersistence.insert(faq);
            assertEquals(i + 1, wechatFaqPersistence.findByTitleLike("a", 1).size());
        }
    }

    @Test
    public void testChangeFaqsStatus() throws Exception {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            WechatFaq faq = new WechatFaq();
            faq.setCatalogId(1L);
            faq.setTitle("");
            faq.setKeyWord("");
            faq.setContent("");
            faq.setStatus("published");
            faq.setType(1);
            wechatFaqPersistence.insert(faq);
            ids.add(faq.getId());
        }
        assertEquals(5, wechatFaqPersistence.findByCatalog(1L, 1).size());
        assertEquals(5, wechatFaqPersistence.changeFaqsStatus(ids, "unpublished"));
        assertEquals(0, wechatFaqPersistence.findByCatalog(1L, 1).size());
    }
}
