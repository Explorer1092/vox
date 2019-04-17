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

package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newhomework.api.HomeworkCommentService;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import lombok.Getter;

import java.util.Collection;

/**
 * Client implementation of {@link HomeworkCommentService}.
 *
 * @author Xiaohai Zhang
 * @since Oct 19, 2015
 */
public class HomeworkCommentServiceClient implements HomeworkCommentService {

    @Getter
    @ImportService(interfaceClass = HomeworkCommentService.class)
    private HomeworkCommentService remoteReference;

    @Override
    public void createHomeworkComments(Collection<HomeworkComment> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }
        remoteReference.createHomeworkComments(comments);
    }

    @Override
    public void __readHomeworkComments(Long studentId) {
        if (studentId == null) {
            return;
        }
        remoteReference.__readHomeworkComments(studentId);
    }
}
