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

package com.voxlearning.utopia.service.feedback.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.api.constant.RegisterFeedbackCategory;
import com.voxlearning.utopia.service.feedback.api.entities.RegisterFeedback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import static com.voxlearning.utopia.api.constant.RegisterFeedbackCategory.CALL_AMBASSADOR;
import static com.voxlearning.utopia.api.constant.RegisterFeedbackState.WAITTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author xin.xin
 * @since 2014-03-03
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(value = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RegisterFeedback.class)
public class TestRegisterFeedbackPersistence {

    @Autowired private RegisterFeedbackPersistence registerFeedbackPersistence;

    @Test
    public void registerFeedbackPersistenceTest() throws ParseException {
        RegisterFeedback feedback = new RegisterFeedback();
        feedback.setSensitiveMobile("13800138000");
        feedback.setState(1);
        feedback.setOperator("test");
        feedback.setOperation("unit test");
        feedback.setVerificationCode("032258");
        feedback.setContent("content");
        feedback.setCategory(RegisterFeedbackCategory.CALL_ME);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        feedback.setCreateDatetime(sdf.parse("2014-03-03 18:54:23"));
        feedback.setUpdateDatetime(sdf.parse("2014-03-03 18:54:23"));

        registerFeedbackPersistence.insert(feedback);
        Long id = feedback.getId();

        RegisterFeedback result = registerFeedbackPersistence.load(id);

        assertTrue(result.getSensitiveMobile().equals(feedback.getSensitiveMobile()));
        assertTrue(Objects.equals(result.getState(), feedback.getState()));
        assertTrue(result.getOperator().equals(feedback.getOperator()));
        assertTrue(result.getOperation().equals(feedback.getOperation()));
        assertTrue(result.getVerificationCode().equals(feedback.getVerificationCode()));
        assertTrue(result.getContent().equals(feedback.getContent()));
    }

    @Test
    public void testFind() throws ParseException {
        RegisterFeedback feedback = new RegisterFeedback();
        feedback.setSensitiveMobile("13800138000");
        feedback.setState(1);
        feedback.setOperator("test");
        feedback.setOperation("unit test");
        feedback.setVerificationCode("032258");
        feedback.setCategory(RegisterFeedbackCategory.CALL_ME);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        feedback.setCreateDatetime(sdf.parse("2014-03-03 18:54:23"));
        feedback.setUpdateDatetime(sdf.parse("2014-03-03 18:54:23"));

        registerFeedbackPersistence.insert(feedback);

        List<RegisterFeedback> result = registerFeedbackPersistence.find(1, null, null);
        assertTrue(result.size() > 0);
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        result = registerFeedbackPersistence.find(0, new Timestamp(sdf.parse("2014-03-03").getTime()), new Timestamp(sdf.parse("2014-03-04").getTime()));
        assertTrue(result.size() > 0);
    }

    @Test
    public void testFindByMobileAndCategoryAndStateWithinDayRange() throws Exception {
        RegisterFeedback feedback = new RegisterFeedback();
        feedback.setSensitiveMobile("13800138000");
        feedback.setState(1);
        feedback.setOperator("test");
        feedback.setOperation("unit test");
        feedback.setVerificationCode("");
        feedback.setCategory(CALL_AMBASSADOR);

        registerFeedbackPersistence.insert(feedback);

        assertEquals(1, registerFeedbackPersistence.findByMobileAndCategoryAndStateWithinDayRange("13800138000", CALL_AMBASSADOR, WAITTING).size());
    }
}
