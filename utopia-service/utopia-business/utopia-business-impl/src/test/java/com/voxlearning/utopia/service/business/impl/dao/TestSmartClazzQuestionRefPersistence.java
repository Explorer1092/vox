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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzQuestionRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

/**
 * @author Maofeng Lu
 * @since 14-10-24 下午4:52
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestSmartClazzQuestionRefPersistence {
    @Inject
    SmartClazzQuestionRefPersistence smartClazzQuestionRefPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = SmartClazzQuestionRef.class)
    public void saveSmartClazzQuestionRef() {
        SmartClazzQuestionRef ref = new SmartClazzQuestionRef();
        ref.setClazzId(31203L);
        ref.setSubject(Subject.ENGLISH);
        ref.setQuestionId("123456");
        smartClazzQuestionRefPersistence.persist(ref);
        SmartClazzQuestionRef ref2 = new SmartClazzQuestionRef();
        ref2.setClazzId(31203L);
        ref2.setSubject(Subject.ENGLISH);
        ref2.setQuestionId("1234567");
        smartClazzQuestionRefPersistence.persist(ref2);
        Pageable pageable = new PageRequest(0, 2);
        Page<SmartClazzQuestionRef> page = smartClazzQuestionRefPersistence.pagingFindRefByClazzIdAndSubject(31203L, Subject.ENGLISH, pageable);
        Assert.assertNotNull(page);
        Assert.assertTrue(page.getContent().size() == 2);


    }
}
