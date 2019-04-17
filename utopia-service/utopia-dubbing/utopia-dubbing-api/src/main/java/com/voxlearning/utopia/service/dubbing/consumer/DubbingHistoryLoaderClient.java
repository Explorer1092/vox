package com.voxlearning.utopia.service.dubbing.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.dubbing.api.DubbingHistoryLoader;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2017-8-23
 */
public class DubbingHistoryLoaderClient implements DubbingHistoryLoader {

    @ImportService(interfaceClass = DubbingHistoryLoader.class)
    private DubbingHistoryLoader dubbingHistoryLoader;


    @Override
    public List<DubbingHistory> getDubbingHistoryByUserId(Long userId) {
        return dubbingHistoryLoader.getDubbingHistoryByUserId(userId);
    }

    @Override
    public List<DubbingHistory> getDubbingHistoryByClazzIdAndDubbingId(Long clazzId, String dubbingId) {
        return dubbingHistoryLoader.getDubbingHistoryByClazzIdAndDubbingId(clazzId, dubbingId);
    }

    @Override
    public Long getDubbingHistoryCountByUserIdAndDubbingId(Long userId, String dubbingId) {
        return dubbingHistoryLoader.getDubbingHistoryCountByUserIdAndDubbingId(userId, dubbingId);
    }

    @Override
    public Map<String, Long> getDubbingHistoryCountByUserIdAndDubbingIds(Long userId, Collection<String> dubbingIds) {
        return dubbingHistoryLoader.getDubbingHistoryCountByUserIdAndDubbingIds(userId, dubbingIds);
    }

    @Override
    public Long getDubbingHistoryCountByUserId(Long userId) {
        return dubbingHistoryLoader.getDubbingHistoryCountByUserId(userId);
    }

    @Override
    public DubbingHistory getDubbingHistoryById(String id) {
        return dubbingHistoryLoader.getDubbingHistoryById(id);
    }

    @Override
    public Map<String, DubbingHistory> getDubbingHistoriesByIds(Collection<String> ids) {
        return dubbingHistoryLoader.getDubbingHistoriesByIds(ids);
    }

    @Override
    public List<DubbingHistory> jobQueryBySecondary() {
        return dubbingHistoryLoader.jobQueryBySecondary();
    }

    @Override
    public Map<String, Integer> getDubbingHistoryCountByUserIdAndCategoryIds(Long userId, Collection<String> categoryIds) {
        return dubbingHistoryLoader.getDubbingHistoryCountByUserIdAndCategoryIds(userId, categoryIds);
    }

    @Override
    public DubbingHistory getDubbingHistoryByHomeworkId(Long userId, String dubbingId, String homeworkId) {
        return dubbingHistoryLoader.getDubbingHistoryByHomeworkId(userId, dubbingId, homeworkId);
    }

}
