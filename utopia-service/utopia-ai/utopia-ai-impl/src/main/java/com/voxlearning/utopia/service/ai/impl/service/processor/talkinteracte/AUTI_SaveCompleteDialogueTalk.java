package com.voxlearning.utopia.service.ai.impl.service.processor.talkinteracte;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.data.AITalkScene;
import com.voxlearning.utopia.service.ai.data.AITalkSceneResult;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;
import com.voxlearning.utopia.service.ai.data.TalkResultInfoData;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.impl.support.UserInfoSupport;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class AUTI_SaveCompleteDialogueTalk extends AbstractAiSupport implements IAITask<AITalkLessonInteractContext> {

    @Inject
    private AICacheSystem aiCacheSystem;

    @Override
    public void execute(AITalkLessonInteractContext context) {
        if (context.getType() == LessonType.Task || context.getType() == LessonType.task_conversation) {//任务课程的就不直接入库了
            return;
        }

        if (context.getResultInfo() == null || CollectionUtils.isEmpty(context.getResultInfo().getData())) {
            return;
        }

        TalkResultInfoData data = context.getResultInfo().getData().stream().filter(e -> "end".equals(e.getStatus())).findFirst().orElse(null);
        if (data == null) {
            return;
        }

        List<AITalkSceneResult> aiTalkSceneResultList = aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().loadRecord(context.getUsercode(), context.getLessonId());

        if (CollectionUtils.isEmpty(aiTalkSceneResultList)) {
            logger.error("no AITalkSceneResult. userId:{}, usercode:{}, lessonId:{}", context.getUser().getId(), context.getUsercode(), context.getLessonId());
            return;
        }


        Long userId = context.getUser().getId();
        String lessonId = context.getLessonId();

        List<AITalkScene> talkList = handleUserTalk(aiTalkSceneResultList, context.getUser());
        aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().addTalkList(String.valueOf(userId), lessonId, talkList);

    }

    private List<AITalkScene> handleUserTalk(List<AITalkSceneResult> aiTalkSceneResultList, User user) {
        List<AITalkScene> talkSceneList = new ArrayList<>();
        for (int i = 0; i < aiTalkSceneResultList.size(); i++) {
            AITalkSceneResult talkSceneResult = aiTalkSceneResultList.get(i);
            if (talkSceneResult == null) {
                continue;
            }
            Map<String, Object> map = JsonUtils.fromJson(talkSceneResult.getResult());
            if (MapUtils.isEmpty(map)) {
                continue;
            }

            if (talkSceneResult.isUser()) {
                String voiceUrl = SafeConverter.toString(map.get("voiceURI"));
                if (StringUtils.isBlank(voiceUrl)) {
                    continue;
                }
                AITalkScene aiTalkScene = new AITalkScene();
                aiTalkScene.setRoleType(AITalkScene.RoleType.Student);
                aiTalkScene.setRoleImage(UserInfoSupport.getUserRoleImage(user));
                aiTalkScene.setMedia(voiceUrl);
                talkSceneList.add(aiTalkScene);
            } else {
                TalkResultInfo talkResultInfo = JsonUtils.fromJson(talkSceneResult.getResult(), TalkResultInfo.class);
                if (talkResultInfo == null || CollectionUtils.isEmpty(talkResultInfo.getData())) {
                    continue;
                }
                talkResultInfo.getData().forEach(e -> {
                    if (e.getContent() != null && !StringUtils.isAnyBlank(e.getContent().getVideo(), e.getContent().getCn_translation(), e.getContent().getTranslation())) {
                        AITalkScene aiTalkScene = new AITalkScene();
                        aiTalkScene.setRoleImage(e.getContent().getRole_image());
                        aiTalkScene.setMedia(e.getContent().getVideo());
                        aiTalkScene.setRoleType(AITalkScene.RoleType.AITeacher);
                        aiTalkScene.setDescription(e.getContent().getCn_translation());
                        aiTalkScene.setOriginal(e.getContent().getTranslation());
                        if (StringUtils.isNotBlank(e.getContent().getLevel()) &&
                                !e.getContent().getLevel().matches("[e|f|E|F]\\S*") && e.getKnowledge() != null && CollectionUtils.isNotEmpty(e.getKnowledge().getSentences())) {
                            List<Map<String, Object>> suggests = new ArrayList<>();
                            e.getKnowledge().getSentences().stream().filter(e0 -> e0 != null && StringUtils.isNoneBlank(e0.getSentence(), e0.getSentence_audio()))
                                    .forEach(e0 -> {
                                        Map<String, Object> suggest = new HashMap<>();
                                        suggest.put("orginal", e0.getSentence());
                                        suggest.put("translate", "");
                                        suggest.put("audio", e0.getSentence_audio());
                                        suggests.add(suggest);
                                    });
                            aiTalkScene.setSuggestion(suggests);
                        }
                        talkSceneList.add(aiTalkScene);
                    }
                });
            }

        }
        return talkSceneList;
    }
}
