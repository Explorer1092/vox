package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.data.AITalkScene;
import com.voxlearning.utopia.service.ai.data.AITalkSceneResult;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 缓存对话过程
 *
 * @author songtao
 * @since 2018-01-15
 */
public class UserDialogueTalkSceneResultCacheManager extends CollectionAtomicCacheManager<UserDialogueTalkSceneResultCacheManager.GeneratorKeyV2, List<AITalkSceneResult>, AITalkSceneResult> {

    public UserDialogueTalkSceneResultCacheManager(UtopiaCache cache) {
        super(cache);
    }


    public void addRecord(AITalkLessonInteractContext context, boolean user, String result) {
        String userCode = context.getUsercode();
        String lessonId = context.getLessonId();
        String cacheKey = cacheKey(new GeneratorKeyV2(userCode, lessonId));
        if (lessonId.equals(result)) {
            getCache().set(cacheKey, expirationInSeconds(), Collections.emptyList());
            // Generate a session id first join
            genSessionId(context.getUser().getId(), lessonId);
        } else {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setResult(result);
            aiTalkSceneResult.setUser(user);
            casAddList(cacheKey, aiTalkSceneResult);
        }
    }

    public void addRecordV2(Long userId, String lessonId, String qid, TalkResultInfo quesition, String input, TalkResultInfo feedback) {
        String session = getSessonId(userId, lessonId);
        if (StringUtils.isBlank(session)) {
            return;
        }
        String cacheKey = cacheKey(new GeneratorKeyV2(userId + session, lessonId));
        List<AITalkSceneResult> talkScenes = getCache().load(cacheKey);
        if (talkScenes == null) {
            talkScenes = new ArrayList<>();
        }

        boolean addQuestion = true;
        if (CollectionUtils.isNotEmpty(talkScenes)) {
            addQuestion = !qid.equals(talkScenes.get(talkScenes.size() - 1).getQid());
        }

        if (addQuestion && quesition != null) {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setResult(JsonUtils.toJson(quesition));
            aiTalkSceneResult.setQid(qid);
            aiTalkSceneResult.setUser(false);
            talkScenes.add(aiTalkSceneResult);
        }

        if (StringUtils.isNotBlank(input)) {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setResult(input);
            aiTalkSceneResult.setUser(true);
            aiTalkSceneResult.setQid(qid);
            talkScenes.add(aiTalkSceneResult);
        }

        if (feedback != null) {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setQid(qid);
            aiTalkSceneResult.setResult(JsonUtils.toJson(feedback));
            aiTalkSceneResult.setUser(false);
            talkScenes.add(aiTalkSceneResult);
        }

        getCache().set(cacheKey, expirationInSeconds(), talkScenes);
    }


    public List<AITalkSceneResult> loadRecord(String userCode, String lessonId) {
        String cacheKey = cacheKey(new GeneratorKeyV2(userCode, lessonId));
        return getCache().load(cacheKey);
    }

    //TODO 上线一段时间后删除本方法
    @Deprecated
    public String genSessionId(long userId, String lessonId) {
        String key = CacheKeyGenerator.generateCacheKey(UserDialogueTalkSceneResultCacheManager.class, new String[]{"USER_ID", "LESSON_ID"}, new Object[]{userId, lessonId});
        String sessionId = RandomUtils.nextObjectId();
        getCache().set(key, expirationInSeconds(), sessionId);
        return sessionId;
    }

    //TODO 上线一段时间后删除本方法
    @Deprecated
    public String getSessonId(long userId, String lessonId) {
        String key = CacheKeyGenerator.generateCacheKey(UserDialogueTalkSceneResultCacheManager.class, new String[]{"USER_ID", "LESSON_ID"}, new Object[]{userId, lessonId});
        return String.valueOf(getCache().get(key).getValue());
    }


    public void addTalkList(String uid, String lessonId, List<AITalkScene> talkSceneList) {
        String cacheKey = cacheKey(new GeneratorKeyV2(uid, lessonId));
        getCache().delete(cacheKey);
        getCache().set(cacheKey, expirationInSeconds(), talkSceneList);
    }

    public List<AITalkScene> loadTalkList(String uid, String lessonId) {
        String cacheKey = cacheKey(new GeneratorKeyV2(uid, lessonId));
        return getCache().load(cacheKey);
    }

    @Override
    public int expirationInSeconds() {
        return 60 * 60 * 2;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userCode", "lessonId"})
    public static class GeneratorKeyV2 {
        private String userCode;
        private String lessonId;

        @Override
        public String toString() {
            return "UCODE=" + userCode + ";LID=" + lessonId;
        }
    }
}
