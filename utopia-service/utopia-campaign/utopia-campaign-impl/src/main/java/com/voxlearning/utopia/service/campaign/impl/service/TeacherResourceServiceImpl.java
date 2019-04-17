package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.campaign.api.TeacherResourceService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherResourceRef;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherResourceRefDao;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = TeacherResourceService.class)
public class TeacherResourceServiceImpl implements TeacherResourceService {

    private static final Logger log = LoggerFactory.getLogger(TeacherResourceServiceImpl.class);

    @Inject
    private TeacherResourceRefDao teacherResourceRefDao;

    @Override
    public List<TeacherResourceRef> loadTeacherResourceByUserId(Long teacherId) {
        return teacherResourceRefDao.getByUserId(teacherId);
    }

    public List<TeacherResourceRef> loadCoursewareResourceByUserId(Long teacherId) {
        return loadTeacherResourceByUserId(teacherId).stream()
                .filter(i -> Objects.equals(TeacherResourceRef.Type.COURSEWARE, i.getResourceType()))
                .collect(Collectors.toList());
    }

    @Override
    public MapMessage saveCoursewareResource(Long teacherId, String coursewareId, String coursewareName, String resourceUrl) {
        return saveTeacherResource(teacherId, TeacherResourceRef.Type.COURSEWARE, coursewareId, coursewareName, resourceUrl);
    }

    @Override
    public MapMessage updateCoursewareUrl(String coursewardId, String resourceUrl) {
        boolean result = teacherResourceRefDao.updateCoursewareUrl(coursewardId, resourceUrl);
        if (result) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }

    private List<TeacherResourceRef> loadTeacherResourceByUserIdType(Long teacherId, TeacherResourceRef.Type resourceType) {
        return loadTeacherResourceByUserId(teacherId).stream()
                .filter(i -> Objects.equals(i.getResourceType(), resourceType))
                .collect(Collectors.toList());
    }

    private MapMessage saveTeacherResource(Long teacherId, TeacherResourceRef.Type resourceType, String resourceId, String resourceName, String resourceUrl) {
        try {
            AtomicCallback<MapMessage> callback = () -> {
                try {
                    TeacherResourceRef resourceRef = new TeacherResourceRef();
                    resourceRef.setUserId(teacherId);
                    resourceRef.setResourceType(resourceType);
                    resourceRef.setResourceId(resourceId);
                    resourceRef.setResourceName(resourceName);
                    resourceRef.setUrl(resourceUrl);

                    List<TeacherResourceRef> teacherResourceRefs = loadTeacherResourceByUserIdType(teacherId, resourceType);
                    if (teacherResourceRefs.contains(resourceRef)) {
                        return MapMessage.errorMessage("您已拥有该资源");
                    }
                    teacherResourceRefDao.upsert(resourceRef);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return MapMessage.errorMessage();
                }
                return MapMessage.successMessage();
            };
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherResource:saveTeacherResource")
                    .keys(teacherId)
                    .callback(callback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }
}
