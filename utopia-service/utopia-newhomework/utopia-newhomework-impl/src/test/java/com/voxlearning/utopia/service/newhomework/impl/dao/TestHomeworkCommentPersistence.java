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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.context.MDP;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestHomeworkCommentPersistence {
    @Inject HomeworkCommentPersistence homeworkCommentPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = HomeworkComment.class)
    @MockBinder(
            type = HomeworkComment.class,
            jsons = {
                    "{'studentId':1,'homeworkId':'A'}",
                    "{'studentId':2,'homeworkId':'A'}",
                    "{'studentId':3,'homeworkId':'A'}",
                    "{'studentId':1,'homeworkId':'B'}",
                    "{'studentId':2,'homeworkId':'B'}",
                    "{'studentId':3,'homeworkId':'B'}",
                    "{'studentId':1,'homeworkId':'C'}",
                    "{'studentId':2,'homeworkId':'C'}",
                    "{'studentId':3,'homeworkId':'C'}"
            },
            persistence = HomeworkCommentPersistence.class
    )
    public void testQueryByHomeworkIds() throws Exception {
        Set<String> homeworkIds = MDP.groupingBy(HomeworkComment.class, HomeworkComment::getHomeworkId).keySet();
        Map<String, Set<HomeworkComment.Location>> map = homeworkCommentPersistence.queryByHomeworkIds(homeworkIds);
        assertEquals(homeworkIds.size(), map.size());
        homeworkIds.stream()
                .map(map::get)
                .mapToInt(Set::size)
                .forEach(t -> assertEquals(3, t));
    }

}
