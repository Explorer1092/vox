package com.voxlearning.utopia.service.business.impl.service.internal;

import com.voxlearning.utopia.service.business.api.entity.TeacherResourceRef;
import com.voxlearning.utopia.service.business.impl.dao.TeacherResourceRefDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;

@Named
public class InternalTeacherResourceRefService {

    @Inject
    private TeacherResourceRefDao teacherResourceRefDao;

    public boolean getShareParentStatus(Long teacherId, String resourceId) {
        List<TeacherResourceRef> teacherResourceRefs = teacherResourceRefDao.loadByUserId(teacherId);
        TeacherResourceRef teacherResourceRef = teacherResourceRefs.stream()
                .filter(i -> Objects.equals(resourceId, i.getResourceId()))
                .findFirst().orElse(null);

        if (teacherResourceRef == null || teacherResourceRef.getShareParent() == null) {
            return false;
        }
        return teacherResourceRef.getShareParent();
    }

    public void shareParent(Long teacherId, String resourceId) {
        List<TeacherResourceRef> teacherResourceRefs = teacherResourceRefDao.loadByUserId(teacherId);
        TeacherResourceRef teacherResourceRef = teacherResourceRefs.stream()
                .filter(i -> Objects.equals(resourceId, i.getResourceId()))
                .findFirst().orElse(null);

        if (teacherResourceRef == null) {
            teacherResourceRef = new TeacherResourceRef();
            teacherResourceRef.setTeacherId(teacherId);
            teacherResourceRef.setResourceId(resourceId);
            teacherResourceRef.setShareParent(true);
        } else if (teacherResourceRef.getShareParent() == null || Objects.equals(teacherResourceRef.getShareParent(), false)) {
            teacherResourceRef.setShareParent(true);
        }
        teacherResourceRefDao.upsert(teacherResourceRef);
    }
}
