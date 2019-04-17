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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.UnreadHomeworkComment;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@DropMongoDatabase
public class TestUnreadHomeworkCommentDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testUnreadHomeworkCommentDao() throws Exception {
        long studentId = 30009;
        assertEquals(0, unreadHomeworkCommentDao.findByStudentId(studentId).size());
        assertEquals(0, unreadHomeworkCommentDao.countByStudentId(studentId));

        for (int i = 0; i < 10; i++) {
            UnreadHomeworkComment comment = new UnreadHomeworkComment();
            comment.setStudentId(studentId);
            unreadHomeworkCommentDao.inserts(Collections.singleton(comment));

            assertEquals(i + 1, unreadHomeworkCommentDao.findByStudentId(studentId).size());
            assertEquals(i + 1, unreadHomeworkCommentDao.countByStudentId(studentId));
        }

        unreadHomeworkCommentDao.deleteByStudentId(studentId);
        assertEquals(0, unreadHomeworkCommentDao.findByStudentId(studentId).size());
        assertEquals(0, unreadHomeworkCommentDao.countByStudentId(studentId));
    }
}
