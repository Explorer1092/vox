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

package com.voxlearning.utopia.service.mizar.impl.dao.oa;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccountsArticle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = OfficialAccountsArticle.class)
public class TestOfficialAccountsArticlePersistence {

    @Inject private OfficialAccountsArticlePersistence officialAccountsArticlePersistence;

    @Test
    public void testLoadByAccountsId() throws Exception {
        long accountId = 10000;
        for (int i = 0; i < 3; i++) {
            OfficialAccountsArticle article = new OfficialAccountsArticle();
            article.setAccountId(accountId);
            officialAccountsArticlePersistence.insert(article);
            assertEquals(i + 1, officialAccountsArticlePersistence.loadByAccountsId(accountId).size());
        }
    }

    @Test
    public void testUpdateStatus() throws Exception {
        OfficialAccountsArticle article = new OfficialAccountsArticle();
        article.setAccountId(10000L);
        article.setBundleId("");
        article.setStatus(OfficialAccountsArticle.Status.Offline);
        officialAccountsArticlePersistence.insert(article);
        Long id = article.getId();
        assertEquals(1, officialAccountsArticlePersistence.updateStatus(10000L, "", OfficialAccountsArticle.Status.Online));
        article = officialAccountsArticlePersistence.load(id);
        assertSame(OfficialAccountsArticle.Status.Online, article.getStatus());
    }
}
