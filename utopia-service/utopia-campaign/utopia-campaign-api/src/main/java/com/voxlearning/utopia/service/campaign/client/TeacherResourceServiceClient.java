package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.campaign.api.TeacherResourceService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherResourceRef;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TeacherResourceServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TeacherResourceServiceClient.class);

    @ImportService(interfaceClass = TeacherResourceService.class)
    private TeacherResourceService remoteReference;

    public List<TeacherResourceRef> loadTeacherResourceByUserId(Long teacherId) {
        return remoteReference.loadTeacherResourceByUserId(teacherId);
    }

    public List<TeacherResourceRef> loadCoursewareResourceByUserId(Long teacherId) {
        return remoteReference.loadCoursewareResourceByUserId(teacherId);
    }

    public Set<String> loadCoursewareResourceIdByUserId(Long teacherId) {
        return remoteReference.loadCoursewareResourceByUserId(teacherId).stream().map(TeacherResourceRef::getResourceId).collect(Collectors.toSet());
    }

    public MapMessage saveCoursewareResource(Long teacherId, String coursewareId, String coursewareName, String resourceUrl) {
        return remoteReference.saveCoursewareResource(teacherId, coursewareId, coursewareName, resourceUrl);
    }

    public MapMessage updateCoursewareUrl(String coursewardId, String resourceUrl) {
        return remoteReference.updateCoursewareUrl(coursewardId, resourceUrl);
    }

}
