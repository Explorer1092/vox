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

import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import com.voxlearning.utopia.service.newhomework.api.mapper.ExHomeworkComment;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class TestExHomeworkComment {
    @Test
    public void testExHomeworkComment() throws Exception {
        HomeworkComment source = new HomeworkComment();
        source.setId(1L);
        source.setDisabled(Boolean.FALSE);
        source.setCreateDatetime(new Date(10000));
        source.setUpdateDatetime(new Date(20000));
        source.setTeacherId(2L);
        source.setStudentId(3L);
        source.setComment("COMMENT");
        source.setHomeworkId("HOMEWORK_ID");
        source.setHomeworkType("HOMEWORK_TYPE");

        ExHomeworkComment target = ExHomeworkComment.newInstance(source);
        assertEquals(1, target.getId().longValue());
        assertEquals(Boolean.FALSE, target.getDisabled());
        assertEquals(10000, target.getCreateDatetime().getTime());
        assertEquals(20000, target.getUpdateDatetime().getTime());
        assertEquals(2, target.getTeacherId().longValue());
        assertEquals(3, target.getStudentId().longValue());
        assertEquals("COMMENT", target.getComment());
        assertEquals("HOMEWORK_ID", target.getHomeworkId());
        assertEquals("HOMEWORK_TYPE", target.getHomeworkType());
    }
}
