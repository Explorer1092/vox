package com.voxlearning.utopia.service.ai.util;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.ai.data.AITalkScene;
import com.voxlearning.utopia.service.ai.data.AITalkSceneResult;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CourseRuleUtil {
    private CourseRuleUtil(){}

    public static int scoreToStar(int score) {
        if (score < 40) {
            return 0;
        } else if (score < 60) {
            return 1;
        } else if (score <= 85) {
            return 2;
        } else if (score <= 100) {
            return 3;
        }
        return 0;
    }

    public static List<AITalkScene> handleUserTalk(List<AITalkSceneResult> aiTalkSceneResultList, String roleImage, boolean audio) {
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
                aiTalkScene.setRoleImage(roleImage);
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
                        aiTalkScene.setMedia(audio ? e.getContent().getAudio() : e.getContent().getVideo());
                        aiTalkScene.setRoleType(AITalkScene.RoleType.AITeacher);
                        aiTalkScene.setDescription(e.getContent().getCn_translation());
                        aiTalkScene.setOriginal(e.getContent().getTranslation());
                        if (StringUtils.isNotBlank(e.getContent().getLevel()) &&
                                !e.getContent().getLevel().matches("[e|f|E|F]\\S*") && e.getKnowledge() != null && CollectionUtils.isNotEmpty(e.getKnowledge().getSentences())) {
                            List<Map<String, Object>> suggests = new ArrayList<>();
                            e.getKnowledge().getSentences().stream().filter(e0 -> e0 != null && StringUtils.isNoneBlank(e0.getSentence(), e0.getSentence_audio()))
                                    .forEach(e0 -> {
                                        Map<String, Object> suggest = new HashMap<>();
                                        suggest.put("audio", e0.getSentence_audio());
                                        suggest.put("translate", "");
                                        suggest.put("orginal", e0.getSentence());
                                        suggests.add(suggest);
                                    });
                            aiTalkScene.setSuggestion(suggests);
                        }
                        talkSceneList.add(aiTalkScene);
                    } else if (e.getKnowledge() != null && CollectionUtils.isNotEmpty(e.getKnowledge().getSentences())) {
                        AITalkScene aiTalkScene = new AITalkScene();
                        aiTalkScene.setRoleType(AITalkScene.RoleType.K);
                        List<Map<String, Object>> suggests = new ArrayList<>();
                        e.getKnowledge().getSentences().stream()
                                .filter(e0 -> e0 != null && StringUtils.isNoneBlank(e0.getSentence(), e0.getSentence_audio()))
                                .forEach(e0 -> {
                                    Map<String, Object> suggest = new HashMap<>();
                                    suggest.put("audio", e0.getSentence_audio());
                                    suggest.put("orginal", e0.getSentence());
                                    suggest.put("translate", "");
                                    suggests.add(suggest);
                                });
                        aiTalkScene.setSuggestion(suggests);
                        talkSceneList.add(aiTalkScene);
                    }
                });
            }

        }
        return talkSceneList;
    }
}
