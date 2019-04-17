package com.voxlearning.utopia.service.campaign.api.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherCoursewareBuffer extends NearBuffer<List<TeacherCourseware>> {

    private final Map<String, TeacherCourseware> map = new HashMap<>();

    @Override
    protected int calculateBufferSize() {
        return map.size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<TeacherCourseware> data) {
        map.clear();
        data.stream().filter(i -> i.getId() != null).forEach(i -> map.put(i.getId(), i));
    }

    public TeacherCourseware loadById(String id) {
        return supplyWithReadLock(() -> map.get(id));
    }
}
