package com.voxlearning.utopia.service.dubbing.impl.loader;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.dubbing.api.DubbingHistoryLoader;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;
import com.voxlearning.utopia.service.dubbing.impl.dao.DubbingHistoryDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by jiang wei on 2017/8/25.
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = DubbingHistoryLoader.class, version = @ServiceVersion(version = "2.0")),
        @ExposeService(interfaceClass = DubbingHistoryLoader.class, version = @ServiceVersion(version = "3.0"))
})
public class DubbingHistoryLoaderImpl implements DubbingHistoryLoader {

    @Inject
    private DubbingHistoryDao dubbingHistoryDao;


    @Override
    public List<DubbingHistory> getDubbingHistoryByUserId(Long userId) {
        if (userId == null || userId == 0L) {
            return Collections.emptyList();
        }
        return dubbingHistoryDao.getDubbingHistoryByUserId(userId);
    }

    @Override
    public List<DubbingHistory> getDubbingHistoryByClazzIdAndDubbingId(Long clazzId, String dubbingId) {
        if (clazzId == null || clazzId == 0L || StringUtils.isBlank(dubbingId)) {
            return Collections.emptyList();
        }
        return dubbingHistoryDao.getDubbingHistoryByClazzIdAndDubbingId(clazzId, dubbingId);
    }

    @Override
    public Long getDubbingHistoryCountByUserIdAndDubbingId(Long userId, String dubbingId) {
        if (userId == null || userId == 0L || StringUtils.isBlank(dubbingId)) {
            return 0L;
        }
        return dubbingHistoryDao.getDubbingHistoryCountByUserIdAndDubbingIds(userId, Collections.singleton(dubbingId)).get(dubbingId);
    }

    @Override
    public Map<String, Long> getDubbingHistoryCountByUserIdAndDubbingIds(Long userId, Collection<String> dubbingIds) {
        if (CollectionUtils.isEmpty(dubbingIds) || userId == null) {
            return Collections.emptyMap();
        }
        return dubbingHistoryDao.getDubbingHistoryCountByUserIdAndDubbingIds(userId, dubbingIds);
    }

    @Override
    public Long getDubbingHistoryCountByUserId(Long userId) {
        if (userId == null || userId == 0L) {
            return 0L;
        }
        return dubbingHistoryDao.getDubbingHistoryCountByUserId(userId);
    }

    @Override
    public DubbingHistory getDubbingHistoryById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return dubbingHistoryDao.load(id);
    }

    @Override
    public Map<String, DubbingHistory> getDubbingHistoriesByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return dubbingHistoryDao.loads(ids);
    }

    @Override
    public List<DubbingHistory> jobQueryBySecondary() {
        return dubbingHistoryDao.jobQueryBySecondary();
    }

    @Override
    public Map<String, Integer> getDubbingHistoryCountByUserIdAndCategoryIds(Long userId, Collection<String> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds) || userId == null) {
            return Collections.emptyMap();
        }

        return dubbingHistoryDao.getDubbingHistoryCountByUserIdAndCategoryId(userId, categoryIds);
    }

    @Override
    public DubbingHistory getDubbingHistoryByHomeworkId(Long userId, String dubbingId, String homeworkId) {
        return dubbingHistoryDao.getDubbingHistoryByHomeworkId(userId, dubbingId, homeworkId);
    }
}
