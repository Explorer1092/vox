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

package com.voxlearning.utopia.admin.dao.feedback;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = UserFeedback.class)
public class TestUserFeedbackPersistence {

    @Inject private UserFeedbackPersistence userFeedbackPersistence;

    @Test
    public void testExecuteUpdate() throws Exception {
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            UserFeedback feedback = new UserFeedback();
            feedback.setUserId(1L);
            feedback.setContent("");
            feedback.setFeedbackType("");
            feedback.setReply("");
            feedback.setComment("");
            userFeedbackPersistence.insert(feedback);
            ids.add(feedback.getId());
        }
        Update update = Update.update("STATE", 1).set("COMMENT", "UTOPIA");
        Criteria criteria = Criteria.where("ID").in(ids);
        userFeedbackPersistence.executeUpdate(update, criteria);
        for (Long id : ids) {
            UserFeedback feedback = userFeedbackPersistence.load(id);
            assertEquals(1, feedback.getState().intValue());
            assertEquals("UTOPIA", feedback.getComment());
        }
    }
}
