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

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import com.voxlearning.utopia.service.newhomework.impl.dao.HomeworkCommentPersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestHomeworkCommentLoaderImpl {

    @Inject private HomeworkCommentLoaderImpl homeworkCommentLoader;

    @Test
    @TruncateDatabaseTable(databaseEntities = HomeworkComment.class)
    @MockBinder(
            type = HomeworkComment.class,
            jsons = {
                    "{'studentId':1,'homeworkId':'A','homeworkType':'ENGLISH'}",
                    "{'studentId':2,'homeworkId':'A','homeworkType':'ENGLISH'}",
                    "{'studentId':3,'homeworkId':'A','homeworkType':'ENGLISH'}",
            },
            persistence = HomeworkCommentPersistence.class
    )
    public void testLoadStudentHomeworkComment() throws Exception {
        Map<Long, HomeworkComment> map = homeworkCommentLoader.loadStudentHomeworkComment("A", HomeworkType.ENGLISH);
        assertEquals(3, map.size());
        assertNotNull(map.get(1L));
        assertNotNull(map.get(2L));
        assertNotNull(map.get(3L));
    }
}
