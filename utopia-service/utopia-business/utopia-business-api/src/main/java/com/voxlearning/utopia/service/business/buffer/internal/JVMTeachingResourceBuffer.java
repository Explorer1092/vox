package com.voxlearning.utopia.service.business.buffer.internal;

import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import com.voxlearning.utopia.service.business.buffer.TeachingResourceBuffer;
import com.voxlearning.utopia.service.business.buffer.mapper.TeachingResourceList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class JVMTeachingResourceBuffer implements TeachingResourceBuffer {
    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private List<TeachingResource> originalList;
    private List<TeachingResourceRaw> originalRawList;
    private long version;

    @Override
    public void attach(TeachingResourceList data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            originalList = new LinkedList<>();
            originalRawList = new LinkedList<>();

            if (data.getTeachingResourceList() != null) {
                data.getTeachingResourceList()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(originalList::add);
            }
            if (data.getTeachingResourceRawList() != null) {
                data.getTeachingResourceRawList()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(originalRawList::add);
            }
            version = data.getVersion();
        });
    }

    @Override
    public TeachingResourceList dump() {
        return locker.withinReadLock(() -> {
            TeachingResourceList data = new TeachingResourceList();
            data.setVersion(version);

            List<TeachingResource> list = new ArrayList<>(this.originalList.size());
            this.originalList.forEach((e) -> list.add(e.clone()));
            data.setTeachingResourceList(list);

            List<TeachingResourceRaw> rawList = new ArrayList<>(this.originalRawList.size());
            this.originalRawList.forEach((e) -> rawList.add(e.clone()));
            data.setTeachingResourceRawList(rawList);
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }
}
