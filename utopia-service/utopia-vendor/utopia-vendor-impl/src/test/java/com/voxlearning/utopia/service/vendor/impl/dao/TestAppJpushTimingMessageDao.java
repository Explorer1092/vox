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

package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.support.RangeableId;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.entity.AppJpushTimingMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.time.Instant;

/**
 * @author xinxin
 * @since 27/7/2016
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAppJpushTimingMessageDao {
    @Inject
    private AppJpushTimingMessageDao appJpushTimingMessageDao;

    @Test
    public void testGetTimingMessage() {
        long sendTime = Instant.now().plusSeconds(5 * 60).toEpochMilli();

        for (int i = 0; i < 3; i++) {
            AppJpushTimingMessage message = new AppJpushTimingMessage();
            message.setId(RangeableId.newInstance(DateRangeType.M, sendTime * 1000).toString());
            message.setCreateTime(Instant.now().toEpochMilli());
            message.setMessageSource(AppMessageSource.STUDENT.name());
            message.setNotify(String.valueOf(i));
            message.setSendTime(sendTime);

            appJpushTimingMessageDao.insert(message);
        }

        Pageable pageable = PageableUtils.startFromOne(1, 4);
        Page<AppJpushTimingMessage> page = appJpushTimingMessageDao.getTimingMessage(sendTime, pageable);
        Assert.assertEquals(3, page.getContent().size());


        pageable = PageableUtils.startFromOne(2, 2);
        page = appJpushTimingMessageDao.getTimingMessage(sendTime, pageable);
        Assert.assertEquals(1, page.getContent().size());

    }
}
