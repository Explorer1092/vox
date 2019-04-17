package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 缓存视频对话要合成的视频
 *
 * @author songtao
 * @since 2018-01-15
 */
public class UserDialogueVideoCacheManager extends PojoCacheObject<UserDialogueVideoCacheManager.GeneratorKey, List<String>> {

    public UserDialogueVideoCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void addRecord(Long userId, String lessonId, String sessionId, String video) {
        String cacheKey = cacheKey(new GeneratorKey(userId, lessonId, sessionId));
        List<String> recordList = getCache().load(cacheKey);
        if (recordList == null) {
            recordList = new LinkedList<>();
        }
        String record = recordList.stream().filter(e -> video.equals(e)).findFirst().orElse(null);
        if (StringUtils.isNotBlank(record) && record.matches("^http\\s*$")) {
            return;
        }
        recordList.add(video);
        getCache().set(cacheKey, expirationInSeconds(), recordList);
    }

    public void replaceUserRecord(Long userId, String lessonId, String sessionId, String video, String qid) {
        String cacheKey = cacheKey(new GeneratorKey(userId, lessonId, sessionId));
        List<String> recordList = getCache().load(cacheKey);
        if (CollectionUtils.isEmpty(recordList)) {
            return;
        }
        recordList = recordList.stream().map(e -> {
            if (qid.equals(e)) {
                return video;
            }
            return e;
        }).collect(Collectors.toList());

        getCache().set(cacheKey, expirationInSeconds(), recordList);
    }

    public List<String> loadRecordList(Long userId, String lessonId, String sessionId) {
        String cacheKey = cacheKey(new GeneratorKey(userId, lessonId, sessionId));
        return getCache().load(cacheKey);
    }


    @Override
    public int expirationInSeconds() {
        return 60 * 60 * 24 * 2;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userId", "lessonId", "sessionId"})
    public static class GeneratorKey {
        private Long userId;
        private String lessonId;
        private String sessionId;

        @Override
        public String toString() {
            return "U=" + userId + ";LID=" + lessonId + ";SEID=" + sessionId;
        }
    }

}
