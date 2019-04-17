package com.voxlearning.utopia.service.afenti.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookAchieve;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.core.util.CacheKeyGenerator.generateCacheKey;

public class UserPicBookCache {

    // 绘本本周阅读成就近端缓存的存活时间
    public static final int ACHIEVE_CACHE_TTL = 900;

    private UtopiaCache cache;
    private UtopiaCache flushableCache;

    public UserPicBookCache(UtopiaCache cache) {
        this.cache = cache;
        flushableCache = CacheSystem.CBS.getCache("flushable");
    }

    public UtopiaCache getCache() {
        return this.cache;
    }

    public UserPicBookAchieve loadAchieve(Long userId) {
        Function<Long, UserPicBookAchieve> newAchieveFunc = uId -> {
            UserPicBookAchieve achieve = new UserPicBookAchieve();
            achieve.setUserId(uId);
            return achieve;
        };

        return getCache()
                .<Long, UserPicBookAchieve>createCacheValueLoader()
                .keyGenerator(UserPicBookCache::genAchieveCacheKey)
                .keys(Collections.singletonList(userId))
                .loads()
                .externalLoader(uIds -> uIds.stream().collect(Collectors.toMap(uId -> uId, newAchieveFunc)))
                .loadsMissed()
                .expiration(DateUtils.getCurrentToWeekEndSecond())
                .write()
                .getAndResortResult()
                .get(userId);
    }

    public void modifyAchieveCache(Long userId, UserPicBookAchieve newAchieve) {
        getCache().<UserPicBookAchieve>createCacheValueModifier()
                .key(genAchieveCacheKey(userId))
                .expiration(DateUtils.getCurrentToWeekEndSecond())
                .modifier(orgVal -> newAchieve)
                .execute();

        String clientCacheKey = generateCacheKey(UserPicBookAchieve.class, "USER_ID", userId);
        this.flushableCache.<UserPicBookAchieve>createCacheValueModifier()
                .key(clientCacheKey)
                .expiration(ACHIEVE_CACHE_TTL)
                .modifier(oldAchieve -> newAchieve)
                .execute();
    }

    public Map<String, UserPicBookProgress> loadProgress(Long userId, List<String> bookIds) {
        Function<String, UserPicBookProgress> initProgressFunc = bookId -> {
            UserPicBookProgress progress = new UserPicBookProgress();
            progress.setUserId(userId);
            progress.setBookId(bookId);

            return progress;
        };

        return getCache()
                .<String, UserPicBookProgress>createCacheValueLoader()
                .keyGenerator(bookId -> genProgressCacheKey(userId, bookId))
                .keys(bookIds)
                .loads()
                .externalLoader(uIds -> uIds.stream().collect(Collectors.toMap(uId -> uId, initProgressFunc)))
                .loadsMissed()
                .expiration(0) // 永久的
                .write()
                .getAndResortResult();
    }

    public void modifyProgressCache(Long userId, String bookId, UserPicBookProgress progress) {
        getCache().<UserPicBookProgress>createCacheValueModifier()
                .key(genProgressCacheKey(userId, bookId))
                .expiration(0) // 永久的
                .modifier(orgVal -> progress)
                .execute();

        // Persist里面的进度缓存更新后，要刷新客户端的缓存
        String clientCacheKey = generateCacheKey(UserPicBookProgress.class, "USER_ID", userId);
        this.flushableCache.<Map<String, UserPicBookProgress>>createCacheValueModifier()
                .key(clientCacheKey)
                .expiration(DateUtils.getCurrentToDayEndSecond())
                .modifier(progressMap -> {
                    progressMap.put(progress.getBookId(), progress);
                    return progressMap;
                })
                .execute();
    }

    private static String genAchieveCacheKey(Long userId) {
        return generateCacheKey(UserPicBookAchieve.class, "USER_ID", userId);
    }

    private static String genProgressCacheKey(Long userId, String bookId) {
        return generateCacheKey(UserPicBookProgress.class,
                new String[]{"USER_ID", "BOOK_ID"},
                new Object[]{userId, bookId});
    }
}
