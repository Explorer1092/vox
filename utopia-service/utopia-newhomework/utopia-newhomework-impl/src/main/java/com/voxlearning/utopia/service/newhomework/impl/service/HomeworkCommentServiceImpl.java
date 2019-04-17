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

package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkLocation;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.HomeworkCommentService;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import com.voxlearning.utopia.service.newhomework.api.entity.UnreadHomeworkComment;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link HomeworkCommentService}.
 *
 * @author Xiaohai Zhang
 * @since Oct 19, 2015
 */
@Named
@Service(interfaceClass = HomeworkCommentService.class)
@ExposeService(interfaceClass = HomeworkCommentService.class)
public class HomeworkCommentServiceImpl extends NewHomeworkSpringBean implements HomeworkCommentService {

    @Override
    public void createHomeworkComments(Collection<HomeworkComment> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }
        comments = comments.stream()
                .filter(t -> t.getStudentId() != null)
                .collect(Collectors.toList());
        homeworkCommentPersistence.inserts(comments);

        List<UnreadHomeworkComment> list = comments.stream()
                .map(t -> {
                    HomeworkType type = HomeworkType.of(t.getHomeworkType());
                    HomeworkLocation location = HomeworkLocation.newInstance(type, t.getHomeworkId());
                    UnreadHomeworkComment u = new UnreadHomeworkComment();
                    u.setStudentId(t.getStudentId());
                    u.setTeacherId(t.getTeacherId());
                    u.setHomeworkLocation(location.toString());
                    u.setComment(t.getComment());
                    u.setReward(t.getRewardIntegral());
                    return u;
                })
                .collect(Collectors.toList());
        unreadHomeworkCommentDao.inserts(list);
    }

    @Override
    public void __readHomeworkComments(Long studentId) {
        if (studentId == null) {
            return;
        }
        unreadHomeworkCommentDao.deleteByStudentId(studentId);
    }
}
