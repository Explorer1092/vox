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

package com.voxlearning.utopia.service.nekketsu.adventure.impl.service;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Administrator
 * @since 2016/5/3
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAdventureServiceImpl extends AdventureSupport{

    @Inject
    private AdventureServiceImpl adventureServiceImpl;

    @Test
    public void testPushLearningReport() throws Exception {
        long userId = 30003;
        int wordsNum = 10;
        long bookId = 1521;
        Integer stageOrder = 1;
        boolean flag = adventureServiceImpl.pushLearningReport(userId, wordsNum, bookId, stageOrder);
        System.out.println(flag);
        Assert.assertTrue(flag);
    }

    @Test
    public void test(){
//        System.out.println(this.afentiOrderLoaderClient.getUserPaidOrder(OrderProductServiceType.Walker.name(),30003L));
//        System.out.println(this.userLoaderClient.loadUser(30003L));
//        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(30003L);
//        System.out.println(studentParents.size());
        Long userId = 30003L;
        User user = this.userLoaderClient.loadUser(userId);
        IntegralHistory integralHistory = new IntegralHistory();
        integralHistory.setUserId(userId);
        integralHistory.setIntegral(1);
        integralHistory.setIntegralType(IntegralType.WALKE_PROBATION_REWARD.getType());
        integralHistory.setComment(IntegralType.WALKE_PROBATION_REWARD.getDescription());

        MapMessage message = userIntegralService.changeIntegral(user, integralHistory);
        System.out.println(message.getInfo());
    }

    @Test
    public void tesdt(){
        List<String> list = new ArrayList<String>();
        list.add("sss");
        list.add("ddd");
        MapMessage mapMessage = MapMessage.errorMessage().set("words", list);

        list = (List<String>)mapMessage.get("words");
        System.out.println(list);

    }
}
