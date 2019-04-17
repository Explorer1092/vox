/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertNotNull;

@DropMongoDatabase
public class TestNewAccomplishmentLoaderImpl extends NewHomeworkUnitTestSupport {

    @Test
    public void testLoadNewAccomplishment() throws Exception {
        NewHomework.Location location = new NewHomework.Location();
        location.setId(RandomUtils.nextObjectId());
        location.setSubject(Subject.ENGLISH);
        location.setCreateTime(Instant.now().toEpochMilli());
        NewAccomplishment accomplishment = newAccomplishmentLoader.loadNewAccomplishment(location);
        assertNotNull(accomplishment);
        accomplishment = newAccomplishmentLoader.loadNewAccomplishment(location);
        assertNotNull(accomplishment);
    }
}
