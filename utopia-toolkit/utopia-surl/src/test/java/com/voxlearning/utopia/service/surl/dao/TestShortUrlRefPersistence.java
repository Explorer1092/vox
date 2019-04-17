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

package com.voxlearning.utopia.service.surl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.surl.entity.ShortUrlRef;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author xin.xin
 * @since 9/28/15
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestShortUrlRefPersistence {
    @Inject
    private ShortUrlRefPersistence shortUrlRefPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = ShortUrlRef.class)
    public void testGetByShortUrl() {
        ShortUrlRef ref = new ShortUrlRef();
        ref.setShortUrl("Qv0fe8");
        ref.setLongUrl("www.17zuoye.com");
        ref.setCreateDatetime(new Date());
        ref.setUpdateDatetime(new Date());
        shortUrlRefPersistence.insert(ref);

        Assert.assertEquals(shortUrlRefPersistence.getByShortUrl("Qv0fe8").getLongUrl(), "www.17zuoye.com");
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ShortUrlRef.class)
    public void testGetByLongUrl() {
        ShortUrlRef ref = new ShortUrlRef();
        ref.setShortUrl("Qv0fe9");
        ref.setLongUrl("www.17zuoye.cn");
        ref.setSign(DigestUtils.md5Hex("www.17zuoye.cn"));
        ref.setCreateDatetime(new Date());
        ref.setUpdateDatetime(new Date());
        shortUrlRefPersistence.insert(ref);

        Assert.assertEquals(shortUrlRefPersistence.getByLongUrl("www.17zuoye.cn").getLongUrl(), "www.17zuoye.cn");
    }
}
