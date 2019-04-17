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

package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.annotation.TruncateVendors;
import com.voxlearning.utopia.service.vendor.api.constant.VendorNotifyChannel;
import com.voxlearning.utopia.service.vendor.api.entity.VendorNotify;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;
import com.voxlearning.utopia.service.vendor.impl.dao.VendorNotifyPersistence;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestVendorServiceImpl {

    @Inject private VendorNotifyPersistence vendorNotifyPersistence;
    @Inject private VendorServiceClient vendorServiceClient;
    @Inject private DPVendorServiceImpl dpVendorService;

    @Test
    @TruncateVendors
    public void test_sendHttpNotify_viaQueue() throws Exception {
        VendorNotify notify = VendorNotify.mockInstance();
        vendorNotifyPersistence.insert(notify);
        Long N = notify.getId();
        assertEquals(0, vendorNotifyPersistence.load(N).getStatus().intValue());

        vendorServiceClient.createVendorNotify(String.valueOf(N))
                .channel(VendorNotifyChannel.HTTP)
                .targetUrl("TARGET_URL")
                .params(new HashMap<String, Object>())
                .send();
        assertEquals(1, vendorNotifyPersistence.load(N).getStatus().intValue());
    }

    @Test
    @TruncateVendors
    public void test_sendJpushNotify__viaQueue() throws Exception {
        VendorNotify notify = VendorNotify.mockInstance();
        vendorNotifyPersistence.insert(notify);
        Long N = notify.getId();
        assertEquals(0, vendorNotifyPersistence.load(N).getStatus().intValue());

        vendorServiceClient.createVendorNotify(String.valueOf(N))
                .channel(VendorNotifyChannel.JPUSH)
                .targetUrl("TARGET_URL")
                .jsonContent("{}")
                .send();
        assertEquals(1, vendorNotifyPersistence.load(N).getStatus().intValue());
    }

    @Test
    public void testSetUserYiQiXueTag(){
        Long userId = 0L;
        Set<String> tags = new HashSet<>();
        tags.add("test_1");
        tags.add("test_2");
        tags.add("test_3");
        dpVendorService.setUserYiQiXuePushTag(userId,tags);
        Set<String> pushTag = dpVendorService.loadUserYiQiXuePushTag(userId);
        Assert.assertTrue(pushTag.contains("test_1"));
        Assert.assertTrue(pushTag.contains("test_2"));
        Assert.assertTrue(pushTag.contains("test_3"));
        dpVendorService.setUserYiQiXuePushTag(userId,null);
        pushTag = dpVendorService.loadUserYiQiXuePushTag(userId);
        Assert.assertEquals(0,pushTag.size());

    }
}
