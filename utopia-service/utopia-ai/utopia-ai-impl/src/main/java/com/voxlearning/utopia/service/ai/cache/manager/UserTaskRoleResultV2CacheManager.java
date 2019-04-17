package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;


/**
 * 缓存任务对话每个角色的最终结果
 */
public class UserTaskRoleResultV2CacheManager extends PojoCacheObject<UserTaskRoleResultV2CacheManager.GeneratorKey, ChipsQuestionResultRequest> {

    public UserTaskRoleResultV2CacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void addRecord(Long userId, String lessonId, String roleName, ChipsQuestionResultRequest result) {
        String cacheKey = cacheKey(new GeneratorKey(userId, lessonId, roleName));
        getCache().set(cacheKey, expirationInSeconds(), result);
    }

    public Map<String, ChipsQuestionResultRequest> loadRecord(Long userId, String lessonId, List<String> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyMap();
        }
        Map<String, ChipsQuestionResultRequest> resultMap = new HashMap<>();
        List<String> cacheKeys = new ArrayList<>();
        roles.forEach(e -> cacheKeys.add(cacheKey(new GeneratorKey(userId, lessonId, e))));

        Map<String, ChipsQuestionResultRequest> cacheRes = getCache().loads(cacheKeys);
        roles.forEach(e -> {
            if (MapUtils.isNotEmpty(cacheRes) && cacheRes.get(cacheKey(new GeneratorKey(userId, lessonId, e))) != null) {
                resultMap.put(e, cacheRes.get(cacheKey(new GeneratorKey(userId, lessonId, e))));
            }
        });
        return resultMap;
    }


    public void deleteRecords(Long userId, String lessonId, Collection<String> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return;
        }
        List<String> cacheKeys = new ArrayList<>();
        roles.forEach(e -> {
            cacheKeys.add(cacheKey(new GeneratorKey(userId, lessonId, e)));
        });
        getCache().delete(cacheKeys);
    }
    @Override
    public int expirationInSeconds() {
        return 60 * 60 * 24 ;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userId", "lessonId", "roleName"})
    public static class GeneratorKey {
        private Long userId;
        private String lessonId;
        private String roleName;

        @Override
        public String toString() {
            return "UID=" + userId + ";LID=" + lessonId + ";RNA=" + roleName;
        }
    }
}
