package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.data.AITalkScene;
import com.voxlearning.utopia.service.ai.data.AITalkSceneResult;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存对话过程
 *
 * @author songtao
 */
public class WechatUserDialogueTalkSceneResultCacheManager extends CollectionAtomicCacheManager<WechatUserDialogueTalkSceneResultCacheManager.GeneratorKey, List<AITalkSceneResult>, AITalkSceneResult> {

    public WechatUserDialogueTalkSceneResultCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void addRecord(Long userId, String lessonId, String qid, TalkResultInfo quesition, String input, TalkResultInfo feedback, String session) {
        if (StringUtils.isBlank(session)) {
            return;
        }
        String cacheKey = cacheKey(new GeneratorKey(userId + session, lessonId));
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
            aiTalkSceneResult.setUser(false);
            aiTalkSceneResult.setResult(JsonUtils.toJson(quesition));
            aiTalkSceneResult.setQid(qid);

            talkScenes.add(aiTalkSceneResult);
        }

        if (StringUtils.isNotBlank(input)) {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setUser(true);
            aiTalkSceneResult.setResult(input);
            aiTalkSceneResult.setQid(qid);
            talkScenes.add(aiTalkSceneResult);
        }

        if (feedback != null) {
            AITalkSceneResult aiTalkSceneResult = new AITalkSceneResult();
            aiTalkSceneResult.setQid(qid);
            aiTalkSceneResult.setUser(false);
            aiTalkSceneResult.setResult(JsonUtils.toJson(feedback));
            talkScenes.add(aiTalkSceneResult);
        }

        getCache().set(cacheKey, expirationInSeconds(), talkScenes);
    }


    public List<AITalkSceneResult> loadRecord(String userCode, String lessonId) {
        String cacheKey = cacheKey(new GeneratorKey(userCode, lessonId));
        return getCache().load(cacheKey);
    }


    public void addTalkList(String uid, String lessonId, List<AITalkScene> talkSceneList) {
        String cacheKey = cacheKey(new GeneratorKey(uid, lessonId));
        getCache().delete(cacheKey);
        getCache().set(cacheKey, expirationInSeconds(), talkSceneList);
    }

    public List<AITalkScene> loadTalkList(String uid, String lessonId) {
        String cacheKey = cacheKey(new GeneratorKey(uid, lessonId));
        return getCache().load(cacheKey);
    }

    @Override
    public int expirationInSeconds() {
        return 60 * 60 * 2;
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
