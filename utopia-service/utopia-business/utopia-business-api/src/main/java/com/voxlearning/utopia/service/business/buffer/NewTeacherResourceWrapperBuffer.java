package com.voxlearning.utopia.service.business.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.business.api.mapper.NewTeacherResourceWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewTeacherResourceWrapperBuffer extends NearBuffer<List<NewTeacherResourceWrapper>> {

    private Map<String, NewTeacherResourceWrapper> map = new HashMap<>();

    @Override
    protected int calculateBufferSize() {
        return map.size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<NewTeacherResourceWrapper> newData) {
        map.clear();
        newData.forEach(i -> map.put(i.getId(), i));
    }

    public NewTeacherResourceWrapper loadById(String id) {
        return supplyWithReadLock(() -> map.get(id));
    }
}
