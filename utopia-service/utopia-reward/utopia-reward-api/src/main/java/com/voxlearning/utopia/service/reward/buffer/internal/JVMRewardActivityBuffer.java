package com.voxlearning.utopia.service.reward.buffer.internal;

import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.utopia.service.reward.api.mapper.RewardActivityList;
import com.voxlearning.utopia.service.reward.buffer.RewardActivityBuffer;
import com.voxlearning.utopia.service.reward.entity.RewardActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class JVMRewardActivityBuffer implements RewardActivityBuffer {
    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();
    private List<RewardActivity> originalList;
    private long version;

    @Override
    public void attach(RewardActivityList data) {
        Assertions.notNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            originalList = new LinkedList<>();

            if (data.getRewardActivityList() != null) {
                data.getRewardActivityList()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(originalList::add);
            }
            version = data.getVersion();
        });
    }

    @Override
    public RewardActivityList dump() {
        return locker.withinReadLock(() -> {
            RewardActivityList data = new RewardActivityList();
            data.setVersion(version);

            List<RewardActivity> list = new ArrayList<>(this.originalList.size());
            this.originalList.forEach((e) -> list.add(e.clone()));
            data.setRewardActivityList(list);
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }
}
