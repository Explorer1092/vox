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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.support.LocationTransformer;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.HomeworkCommentLoader;
import com.voxlearning.utopia.service.newhomework.api.HomeworkCommentLocationLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import com.voxlearning.utopia.service.newhomework.api.entity.UnreadHomeworkComment;
import com.voxlearning.utopia.service.newhomework.api.mapper.ExHomeworkComment;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Client implementation of {@link HomeworkCommentLoader}.
 *
 * @author Xiaohai Zhang
 * @since Oct 19, 2015
 */
public class HomeworkCommentLoaderClient implements HomeworkCommentLoader {

    @ImportService(interfaceClass = HomeworkCommentLoader.class)
    private HomeworkCommentLoader remoteReference;

    @Override
    public long studentUnreadHomeworkCommentCount(Long studentId) {
        if (studentId == null) {
            return 0;
        }
        Long count = remoteReference.studentUnreadHomeworkCommentCount(studentId);
        return SafeConverter.toLong(count);
    }

    @Override
    public List<ExHomeworkComment> studentUnreadHomeworkComments(StudentDetail student) {
        return remoteReference.studentUnreadHomeworkComments(student);
    }

    @Override
    public List<UnreadHomeworkComment> __studentUnreadHomeworkComments(Long studentId) {
        if (studentId == null) {
            return Collections.emptyList();
        }
        return remoteReference.__studentUnreadHomeworkComments(studentId);
    }

    @Override
    public Map<Long, HomeworkComment> __loadHomeworkCommentsIncludeDisabled(Collection<Long> ids) {
        return remoteReference.__loadHomeworkCommentsIncludeDisabled(CollectionUtils.toLinkedHashSet(ids));
    }

    @Override
    public Map<String, Set<HomeworkComment.Location>> __queryHomeworkCommentLocations(Collection<String> homeworkIds) {
        return remoteReference.__queryHomeworkCommentLocations(CollectionUtils.toLinkedHashSet(homeworkIds));
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
