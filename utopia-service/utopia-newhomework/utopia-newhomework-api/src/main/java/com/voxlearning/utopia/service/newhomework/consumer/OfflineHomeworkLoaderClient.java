package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.OfflineHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;

import java.util.*;

/**
 * @author guoqiang.li
 * @since 2016/9/8
 */
public class OfflineHomeworkLoaderClient implements OfflineHomeworkLoader {
    @ImportService(interfaceClass = OfflineHomeworkLoader.class)
    private OfflineHomeworkLoader remoteReference;

    @Override
    public OfflineHomework loadOfflineHomework(String id) {
        return remoteReference.loadOfflineHomework(id);
    }

    @Override
    public Map<String, OfflineHomework> loadOfflineHomeworks(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadOfflineHomeworks(ids);
    }

    @Override
    public Map<String, OfflineHomework> loadByNewHomeworkIds(Collection<String> newHomeworkIds) {
        if (CollectionUtils.isEmpty(newHomeworkIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadByNewHomeworkIds(newHomeworkIds);
    }

    @Override
    public Map<Long, List<OfflineHomework>> loadGroupOfflineHomeworks(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadGroupOfflineHomeworks(groupIds);
    }

    @Override
    public MapMessage loadOfflineHomeworkDetail(String offlineHomeworkId) {
        return remoteReference.loadOfflineHomeworkDetail(offlineHomeworkId);
    }

    @Override
    public Page<OfflineHomework> loadGroupOfflineHomeworks(Collection<Long> groupIds, Date startDate, Date endDate, Pageable pageable) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return remoteReference.loadGroupOfflineHomeworks(groupIds, startDate, endDate, pageable);
    }
}
