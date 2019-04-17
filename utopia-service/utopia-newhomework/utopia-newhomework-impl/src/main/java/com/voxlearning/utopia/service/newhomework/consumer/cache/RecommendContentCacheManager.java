package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.List;

public class RecommendContentCacheManager extends PojoCacheObject<String, List<String>> {

    private final static String RECOMMEND_PICTURE_BOOK_PLUS = "RECOMMEND_PICTURE_BOOK_PLUS_";
    private final static String RECOMMEND_DUBBING = "RECOMMEND_DUBBING_";

    public RecommendContentCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public Boolean saveRecommendPictureIds(List<String> recommendPictureBookIds, Long teacherId) {
        if (CollectionUtils.isEmpty(recommendPictureBookIds)) {
            return Boolean.FALSE;
        }

        String cacheKey = cacheKey(RECOMMEND_PICTURE_BOOK_PLUS + teacherId);
        return getCache().set(cacheKey, DateUtils.getCurrentToDayEndSecond(), recommendPictureBookIds);
    }

    public Boolean saveRecommendDubbingIds(List<String> recommendDubbingIds, Long teacherId) {
        if (CollectionUtils.isEmpty(recommendDubbingIds)) {
            return Boolean.FALSE;
        }

        String cacheKey = cacheKey(RECOMMEND_DUBBING + teacherId);
        return getCache().set(cacheKey, DateUtils.getCurrentToDayEndSecond(), recommendDubbingIds);
    }

    public List<String> loadRecommendPictureIds(Long teacherId) {
        return load(RECOMMEND_PICTURE_BOOK_PLUS + teacherId);
    }

    public List<String> loadRecommendDubbingIds(Long teacherId) {
        return load(RECOMMEND_DUBBING + teacherId);
    }
}
