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
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author xin.xin
 * @since 14-4-16 上午10:21
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = UserWechatRef.class)
public class TestUserWechatRefPersistence {
    @Inject private UserWechatRefPersistence userWechatRefPersistence;

    @Test
    public void testFindByUserIds() {
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        List<UserWechatRef> documents = new LinkedList<>();
        for (Long userId : userIds) {
            for (int i = 0; i < 3; i++) {
                UserWechatRef document = new UserWechatRef();
                document.setUserId(userId);
                document.setOpenId("");
                document.setType(1);
                documents.add(document);
            }
        }
        userWechatRefPersistence.inserts(documents);
        Map<Long, List<UserWechatRef>> map = userWechatRefPersistence.findByUserIdsFromCache(userIds);
        assertEquals(userIds.size(), map.size());
        for (Long userId : userIds) {
            assertEquals(3, map.get(userId).size());
        }
        map = userWechatRefPersistence.findByUserIds(userIds, 1);
        assertEquals(userIds.size(), map.size());
        for (Long userId : userIds) {
            assertEquals(3, userWechatRefPersistence.findByUserId(userId, 1).size());
            assertEquals(3, map.get(userId).size());
        }
    }

    @Test
    public void  testUnionId(){
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        List<UserWechatRef> documents = new LinkedList<>();
        for (Long userId : userIds) {
            for (int i = 0; i < 3; i++) {
                UserWechatRef document = new UserWechatRef();
                document.setUserId(userId);
                document.setOpenId("");
                document.setType(1);
                document.setUnionId("unionId" + i);
                documents.add(document);
            }
        }
        userWechatRefPersistence.inserts(documents);
        UserWechatRef wechatRef = userWechatRefPersistence.findByUnionId("unionId1");
        Assert.assertEquals(1,wechatRef.getUserId().intValue());

    }

}
