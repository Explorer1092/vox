package com.voxlearning.utopia.service.business.buffer;

import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import com.voxlearning.utopia.service.business.buffer.mapper.TeachingResourceList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface TeachingResourceBuffer {

    void attach(TeachingResourceList data);

    long getVersion();

    TeachingResourceList dump();

    default List<TeachingResource> loadTeachingResource() {
        return dump().getTeachingResourceList();
    }

    default List<TeachingResourceRaw> loadTeachingResourceRaw() {
        return dump().getTeachingResourceRawList();
    }

    default Map<String, TeachingResource> loadResourceMap() {
        return loadTeachingResource().stream().collect(Collectors.toMap(TeachingResource::getId, r -> r));
    }

    default Map<String, TeachingResourceRaw> loadResourceRawMap() {
        return loadTeachingResourceRaw().stream().collect(Collectors.toMap(TeachingResourceRaw::getId, r -> r));
    }

    default TeachingResource loadTeachingResourceById(String id) {
        return loadResourceMap().get(id);
    }

    interface Aware {
        TeachingResourceBuffer getTeachingResourceBuffer();

        void resetTeachingResourceBuffer();
    }

}
