package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.data.AITalkSceneResult;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * 缓存对话过程
 *
 * @author songtao
 * @since 2018-01-15
 */
public class UserTaskTalkSceneResultCacheManager extends CollectionAtomicCacheManager<UserTaskTalkSceneResultCacheManager.GeneratorKeyV2, List<AITalkSceneResult>, AITalkSceneResult> {

    public UserTaskTalkSceneResultCacheManager(UtopiaCache cache) {
        super(cache);
    }


    public void addRecord(AITalkLessonInteractContext context, boolean user, String result) {
        String userCode = context.getUsercode();
        String lessonId = context.getLessonId();
        String roleName = context.getRoleName();
        String cacheKey = cacheKey(new GeneratorKeyV2(userCode, lessonId, roleName));
        if (lessonId.equals(result)) {
            getCache().set(cacheKey, expirationInSeconds(), Collections.emptyList());
            // Generate a session id first join
            genSessionId(context.getUser().getId(),lessonId);

        } else {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setResult(result);
            aiTalkSceneResult.setUser(user);
            casAddList(cacheKey, aiTalkSceneResult);
        }
    }

    public void addRecordV2(String userCode, String lessonId, String qid, String roleName, TalkResultInfo quesition, String input, TalkResultInfo feedback) {
        String cacheKey = cacheKey(new GeneratorKeyV2(userCode, lessonId, roleName));
        List<AITalkSceneResult> talkScenes = getCache().load(cacheKey);
        if (talkScenes == null) {
            talkScenes = new ArrayList<>();
        }

        boolean addQuestion = true;
        if (CollectionUtils.isNotEmpty(talkScenes)) {
            addQuestion = !qid.equals(talkScenes.get(talkScenes.size() - 1).getQid());
        }

        if (addQuestion) {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setUser(false);
            aiTalkSceneResult.setQid(qid);
            aiTalkSceneResult.setResult(JsonUtils.toJson(quesition));
            talkScenes.add(aiTalkSceneResult);
        }

        if (StringUtils.isNotBlank(input)) {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setUser(true);
            aiTalkSceneResult.setQid(qid);
            aiTalkSceneResult.setResult(input);
            talkScenes.add(aiTalkSceneResult);
        }

        if (feedback != null) {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setResult(JsonUtils.toJson(feedback));
            aiTalkSceneResult.setUser(false);
            aiTalkSceneResult.setQid(qid);
            talkScenes.add(aiTalkSceneResult);
        }

        getCache().set(cacheKey, expirationInSeconds(), talkScenes);
    }

    public void resetCache(long userId, String lessonId, String roleName, String userCode) {
        String cacheKey = cacheKey(new GeneratorKeyV2(userCode, lessonId, roleName));
        getCache().set(cacheKey, expirationInSeconds(), Collections.emptyList());
        genSessionId(userId, lessonId);
    }

    public void genSessionId(long userId, String lessonId) {
        String key = CacheKeyGenerator.generateCacheKey(UserTaskTalkSceneResultCacheManager.class, new String[]{"USER_ID", "LESSON_ID"}, new Object[]{userId, lessonId});
        String sessionId = UUID.randomUUID().toString();
        getCache().set(key, expirationInSeconds(), sessionId);
    }


    public String getSessonId(long userId, String lessonId) {
        String key = CacheKeyGenerator.generateCacheKey(UserTaskTalkSceneResultCacheManager.class, new String[]{"USER_ID", "LESSON_ID"}, new Object[]{userId, lessonId});
        return String.valueOf(getCache().get(key).getValue());
    }



    public List<AITalkSceneResult> loadRecords(String userCode, String lessonId, Collection<String> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }
        List<AITalkSceneResult> res = new ArrayList<>();
        roles.forEach(e -> {
            Object obj = getCache().load(cacheKey(new GeneratorKeyV2(userCode, lessonId, e)));
            if (obj != null && obj instanceof List && CollectionUtils.isNotEmpty((List<AITalkSceneResult>) obj)) {
                res.addAll((List<AITalkSceneResult>) obj);
            }
        });
        return res;
    }

    public Map<String, List<AITalkSceneResult>> loadRecordMap(String userCode, String lessonId, Collection<String> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyMap();
        }
        Map<String, List<AITalkSceneResult>> res = new HashMap<>();
        roles.forEach(e -> {
            Object obj = getCache().load(cacheKey(new GeneratorKeyV2(userCode, lessonId, e)));
            if (obj != null && obj instanceof List && CollectionUtils.isNotEmpty((List<AITalkSceneResult>) obj)) {
                res.put(e, ((List<AITalkSceneResult>) obj));
            }
        });
        return res;
    }

    @Override
    public int expirationInSeconds() {
        return 60 * 60 * 2;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userCode", "lessonId", "roleName"})
    public static class GeneratorKeyV2 {
        private String userCode;
        private String lessonId;
        private String roleName;

        @Override
        public String toString() {
            return "UCODE=" + userCode + ";LID=" + lessonId + ";RNA=" + roleName;
        }
    }
}
