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

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.support.LocationTransformer;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.HomeworkCommentLoader;
import com.voxlearning.utopia.service.newhomework.api.HomeworkCommentLocationLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import com.voxlearning.utopia.service.newhomework.api.entity.UnreadHomeworkComment;
import com.voxlearning.utopia.service.newhomework.api.mapper.ExHomeworkComment;
import com.voxlearning.utopia.service.newhomework.impl.dao.HomeworkCommentPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewUnreadHomeworkCommentDao;
import com.voxlearning.utopia.service.newhomework.impl.service.HomeworkCommentServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link HomeworkCommentLoader}.
 *
 * @author Xiaohai Zhang
 * @since Oct 19, 2015
 */
@Named
@Service(interfaceClass = HomeworkCommentLoader.class)
@ExposeServices({
        @ExposeService(interfaceClass = HomeworkCommentLoader.class, version = @ServiceVersion(version = "20151019")),
        @ExposeService(interfaceClass = HomeworkCommentLoader.class, version = @ServiceVersion(version = "20170607"))
})
public class HomeworkCommentLoaderImpl implements HomeworkCommentLoader {

    @Inject private HomeworkCommentPersistence homeworkCommentPersistence;
    @Inject private HomeworkCommentServiceImpl homeworkCommentService;
    @Inject private NewUnreadHomeworkCommentDao unreadHomeworkCommentDao;
    @Inject private UserLoaderClient userLoaderClient;

    @Override
    public long studentUnreadHomeworkCommentCount(Long studentId) {
        if (studentId == null) {
            return 0;
        }
        return unreadHomeworkCommentDao.countByStudentId(studentId);
    }

    @Override
    public List<ExHomeworkComment> studentUnreadHomeworkComments(StudentDetail student) {
        if (student == null) {
            return Collections.emptyList();
        }
        List<UnreadHomeworkComment> originalList = __studentUnreadHomeworkComments(student.getId());
        if (CollectionUtils.isEmpty(originalList)) {
            return Collections.emptyList();
        }

        homeworkCommentService.__readHomeworkComments(student.getId());  // 阅后即焚
        List<ExHomeworkComment> commentList = originalList.stream()
                .map(t -> {
                    HomeworkComment c = new HomeworkComment();
                    c.setStudentId(t.getStudentId());
                    c.setTeacherId(t.getTeacherId());
                    c.setComment(t.getComment());
                    c.setRewardIntegral(t.getReward());
                    if (t.getHomeworkLocation() != null) {
                        c.setHomeworkType(StringUtils.substringBefore(t.getHomeworkLocation(), "-"));
                        c.setHomeworkId(StringUtils.substringAfter(t.getHomeworkLocation(), "-"));
                    }
                    return c;
                })
                .map(ExHomeworkComment::newInstance)
                .collect(Collectors.toList());

        Set<Long> teacherIds = commentList.stream()
                .filter(t -> t.getTeacherId() != null)
                .map(ExHomeworkComment::getTeacherId)
                .collect(Collectors.toSet());

        Map<Long, User> teachers = userLoaderClient.loadUsersIncludeDisabled(teacherIds);
        commentList.forEach(t -> {
            t.setStudentImgUrl(student.fetchImageUrl());
            t.setStudentName(student.fetchRealname());
            User teacher = teachers.get(t.getTeacherId());
            if (teacher != null) {
                t.setTeacherImgUrl(teacher.fetchImageUrl());
                t.setTeacherName(teacher.fetchRealname());
            }
        });
        return commentList;
    }

    @Override
    public List<UnreadHomeworkComment> __studentUnreadHomeworkComments(Long studentId) {
        if (studentId == null) {
            return Collections.emptyList();
        }
        return unreadHomeworkCommentDao.findByStudentId(studentId);
    }

    @Override
    public Map<Long, HomeworkComment> __loadHomeworkCommentsIncludeDisabled(Collection<Long> ids) {
        return homeworkCommentPersistence.loads(ids);
    }

    @Override
    public Map<String, Set<HomeworkComment.Location>> __queryHomeworkCommentLocations(Collection<String> homeworkIds) {
        return homeworkCommentPersistence.queryByHomeworkIds(homeworkIds);
    }

    public HomeworkCommentLocationLoader loadHomeworkComments(String homeworkId) {
        return loadHomeworkComments(Collections.singleton(homeworkId));
    }

    public HomeworkCommentLocationLoader loadHomeworkComments(Collection<String> homeworkIds) {
        Set<HomeworkComment.Location> locations = __queryHomeworkCommentLocations(homeworkIds).values()
                .stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        return __newHomeworkCommentLocationLoader(locations);
    }

    public Map<Long, HomeworkComment> loadStudentHomeworkComment(String homeworkId, HomeworkType homeworkType) {
        return loadHomeworkComments(homeworkId)
                .homeworkType(homeworkType)
                .toList()
                .stream()
                .collect(Collectors.groupingBy(HomeworkComment::getStudentId))
                .values()
                .stream()
                .map(t -> t.stream()
                        .sorted((o1, o2) -> Long.compare(o2.fetchCreateTimestamp(), o1.fetchCreateTimestamp()))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(HomeworkComment::getStudentId, t -> t));
    }

    private HomeworkCommentLocationLoader __newHomeworkCommentLocationLoader(
            Collection<HomeworkComment.Location> locations) {
        LocationTransformer<HomeworkComment.Location, HomeworkComment> transformer = candidate -> {
            List<Long> idList = candidate.stream()
                    .map(HomeworkComment.Location::getId)
                    .collect(Collectors.toList());
            Map<Long, HomeworkComment> comments = __loadHomeworkCommentsIncludeDisabled(idList);
            return idList.stream()
                    .map(comments::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };
        return new HomeworkCommentLocationLoader(transformer, locations);
    }
}
