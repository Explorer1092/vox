package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.data.AITalkSceneResult;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户对话session缓存
 *
 * @author songtao
 * @since 2019-01-25
 */
public class UserTalkFeedSessionCacheManager extends CollectionAtomicCacheManager<UserTalkFeedSessionCacheManager.GeneratorKey, List<AITalkSceneResult>, AITalkSceneResult> {

    public UserTalkFeedSessionCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void genSessionId(long userId, String lessonId) {
        String key = CacheKeyGenerator.generateCacheKey(UserTalkFeedSessionCacheManager.class, new String[]{"USER_ID", "LESSON_ID"}, new Object[]{userId, lessonId});
        String sessionId = RandomUtils.nextObjectId();
        getCache().set(key, expirationInSeconds(), sessionId);
    }

    public void saveSessionId(long userId, String lessonId, String sessionId) {
        String key = CacheKeyGenerator.generateCacheKey(UserTalkFeedSessionCacheManager.class, new String[]{"USER_ID", "LESSON_ID"}, new Object[]{userId, lessonId});
        getCache().set(key, expirationInSeconds(), sessionId);
    }

    public String getSessionId(long userId, String lessonId) {
        String key = CacheKeyGenerator.generateCacheKey(UserTalkFeedSessionCacheManager.class, new String[]{"USER_ID", "LESSON_ID"}, new Object[]{userId, lessonId});
        return getCache().load(key);
    }


    @Override
    public int expirationInSeconds() {
        return 60 * 60 * 3;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userCode", "lessonId"})
    public static class GeneratorKey {
        private String userCode;
        private String lessonId;

        @Override
        public String toString() {
            return "UCODE=" + userCode + ";LID=" + lessonId;
        }
    }
}
