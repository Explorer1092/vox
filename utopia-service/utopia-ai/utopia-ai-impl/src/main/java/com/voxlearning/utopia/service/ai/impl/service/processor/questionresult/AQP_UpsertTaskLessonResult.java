package com.voxlearning.utopia.service.ai.impl.service.processor.questionresult;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.cache.manager.UserTaskRoleResultCacheManager;
import com.voxlearning.utopia.service.ai.data.AIQuestion;
import com.voxlearning.utopia.service.ai.data.AITalkScene;
import com.voxlearning.utopia.service.ai.data.AITalkSceneResult;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.data.AIUserQuestionResultRequest;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;
import com.voxlearning.utopia.service.ai.entity.AIDialogueTaskConfig;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.persistence.AIDialogueTaskConfigDao;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.impl.support.UserInfoSupport;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务课程的需要单独
 */
@Named
public class AQP_UpsertTaskLessonResult extends AbstractAiSupport implements IAITask<AIUserQuestionContext> {
    @Inject
    private AIDialogueTaskConfigDao dialogueTaskConfigDao;

    @Inject
    private AICacheSystem aiCacheSystem;

    @Override
    public void execute(AIUserQuestionContext context) {

        String roleName = context.getAiUserQuestionResultRequest().getRoleName();
        if (context.getAiUserQuestionResultRequest().getLessonType() != LessonType.Task || StringUtils.isBlank(roleName)) {
            return;
        }

        UserTaskRoleResultCacheManager taskRoleResultCacheManager = aiCacheSystem.getUserTaskRoleResultCacheManager();

        String lessonId = context.getAiUserQuestionResultRequest().getLessonId();
        taskRoleResultCacheManager.addRecord(context.getUser().getId(), lessonId, roleName, context.getAiUserQuestionResultRequest());

        boolean stopTask = true;
        AIDialogueTaskConfig aiDialogueTaskConfig = dialogueTaskConfigDao.load(lessonId);
        if (aiDialogueTaskConfig == null || CollectionUtils.isEmpty(aiDialogueTaskConfig.getNpcs())) {
            context.errorResponse("对话配置为空");
            return;
        }
        List<String> npcNames = aiDialogueTaskConfig.getNpcs().stream().map(AIDialogueTaskConfig.Npc::getNpcName).collect(Collectors.toList());
        if (Boolean.TRUE.equals(aiDialogueTaskConfig.getAllNpc())) {
             Map<String, AIUserQuestionResultRequest> requestMap = taskRoleResultCacheManager.loadRecord(context.getUser().getId(), lessonId, npcNames);
             if (MapUtils.isNotEmpty(requestMap) && requestMap.size() == npcNames.size()) {
                 doSave(context, requestMap.values(),
                         aiCacheSystem.getUserTaskTalkSceneResultCacheManager().loadRecords(context.getAiUserQuestionResultRequest().getUsercode(), lessonId, npcNames), getRoleImageMap(lessonId));
                 stopTask = false;
             }
        } else {
           String rightRpcName = aiDialogueTaskConfig.getNpcs().stream()
                   .filter(e -> "Success".equalsIgnoreCase(e.getStatus())).findFirst()
                   .map(AIDialogueTaskConfig.Npc::getNpcName).orElse("");
           if (roleName.equalsIgnoreCase(rightRpcName)) {//对于寻找一个正确的人的任务对话只需要计算正确的人的对话
               doSave(context, Arrays.asList(context.getAiUserQuestionResultRequest()),
                       aiCacheSystem.getUserTaskTalkSceneResultCacheManager().loadRecords(context.getAiUserQuestionResultRequest().getUsercode(), lessonId, Arrays.asList(rightRpcName)), getRoleImageMap(lessonId));
               stopTask = false;
           }
        }

        if (!stopTask) {
            taskRoleResultCacheManager.deleteRecords(context.getUser().getId(), lessonId, npcNames);
        } else {
            context.getResult().add("end", false);
        }
        context.setTerminateTask(stopTask);
    }

    private void doSave(AIUserQuestionContext context, Collection<AIUserQuestionResultRequest> requests, List<AITalkSceneResult> talkSceneResults, Map<String, String> npcImageMap) {
        int score = calculateLessonScore(requests);
        int star = CourseRuleUtil.scoreToStar(score);
        List<AITalkScene> aiTalkScenes = calculateTalkScene(talkSceneResults, context, npcImageMap);
        Map<String, Object> extMap = new HashMap<>();
        extMap.put("talkList", aiTalkScenes);

        // 计算lesson的结果
        List<AIUserQuestionResultHistory> lessonList = aiUserQuestionResultHistoryDao.loadByUidAndLessonId(context.getUser().getId(), context.getAiUserQuestionResultRequest().getLessonId());
        if (CollectionUtils.isNotEmpty(lessonList)) {

            AIUserQuestionResultRequest req =context.getAiUserQuestionResultRequest();

            Long userId = context.getUser().getId();
            String lessonId= context.getAiUserQuestionResultRequest().getLessonId();

            AIUserLessonResultHistory lessonResultHistory = new AIUserLessonResultHistory();
            lessonResultHistory.setUserId(userId);
            lessonResultHistory.setUnitId(context.getAiUserQuestionResultRequest().getUnitId());
            lessonResultHistory.setLessonId(lessonId);
            lessonResultHistory.setFinished(true);
            lessonResultHistory.setLessonType(context.getAiUserQuestionResultRequest().getLessonType());
            lessonResultHistory.setUserVideo("");
            lessonResultHistory.setStar(star);
            lessonResultHistory.setScore(score);
            lessonResultHistory.setDisabled(false);


            Integer independent = lessonList.stream().filter(e -> e.getIndependent() != null).mapToInt(AIUserQuestionResultHistory::getIndependent).sum();
            Integer listening = lessonList.stream().filter(e -> e.getListening() != null).mapToInt(AIUserQuestionResultHistory::getListening).sum();
            Integer express=lessonList.stream().filter(e -> e.getExpress() != null).mapToInt(AIUserQuestionResultHistory::getExpress).sum();
            Integer fluency = lessonList.stream().filter(e -> e.getFluency() != null).mapToInt(AIUserQuestionResultHistory::getFluency).sum();
            Integer pronunciation = lessonList.stream().filter(e -> e.getPronunciation() != null).mapToInt(AIUserQuestionResultHistory::getPronunciation).sum();


            // cast to 100
            pronunciation= new BigDecimal(pronunciation).multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP).intValue();

            lessonResultHistory.setIndependent(independent);
            lessonResultHistory.setListening(listening);
            lessonResultHistory.setExpress(express);
            lessonResultHistory.setFluency(new BigDecimal(fluency));
            lessonResultHistory.setPronunciation(new BigDecimal(pronunciation));
            lessonResultHistory.setCreateDate(new Date());
            lessonResultHistory.setUpdateDate(new Date());
            lessonResultHistory.setExt(extMap);

            aiUserLessonResultHistoryDao.disableOld(userId,lessonId);
            aiUserLessonResultHistoryDao.insert(lessonResultHistory);


            context.getResult().add("star", star).add("end", true);
        }
    }

    private int calculateLessonScore(Collection<AIUserQuestionResultRequest> lessonList) {
        if (CollectionUtils.isEmpty(lessonList)) {
            return 0;
        }
        int totalScore = lessonList.stream().filter(e -> e != null && e.getScore() != null && Integer.compare(e.getScore(), 0) > 0).mapToInt(e -> {
            int sc = e.getScore() != null ? e.getScore() : 0;
            int re = e.getDeductScore() != null ? e.getDeductScore() : 0;
            return sc - re > 0 ? sc - re : 0;
        }).sum();
        int avg = new BigDecimal(totalScore).divide(new BigDecimal(lessonList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();

        return avg;
    }

    private List<AITalkScene> calculateTalkScene(List<AITalkSceneResult> aiTalkSceneResultList, AIUserQuestionContext context, Map<String, String> npcImageMap) {
        if (CollectionUtils.isEmpty(aiTalkSceneResultList)) {
            return Collections.emptyList();
        }
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
                aiTalkScene.setRoleImage(UserInfoSupport.getUserRoleImage(context.getUser()));
                aiTalkScene.setMedia(voiceUrl);
                talkSceneList.add(aiTalkScene);
            } else {
                TalkResultInfo talkResultInfo = JsonUtils.fromJson(talkSceneResult.getResult(), TalkResultInfo.class);
                if (talkResultInfo == null || CollectionUtils.isEmpty(talkResultInfo.getData())) {
                    continue;
                }
//                int index = talkSceneList.size();
                talkResultInfo.getData().forEach(e -> {
                    if (e.getContent() != null && !StringUtils.isAnyBlank(e.getContent().getAudio(), e.getContent().getCn_translation(), e.getContent().getTranslation())) {
                        AITalkScene aiTalkScene = new AITalkScene();
                        aiTalkScene.setRoleImage(MapUtils.isNotEmpty(npcImageMap) ? npcImageMap.get(e.getName()) : e.getContent().getRole_image());
                        aiTalkScene.setMedia(e.getContent().getAudio());
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
//                    if (index > 0 && talkSceneList.get(index - 1).getRoleType() == AITalkScene.RoleType.Student && e.getKnowledge() != null
//                            && CollectionUtils.isEmpty(talkSceneList.get(index - 1).getSuggestion())) {
//                        List<Map<String, Object>> suggests = new ArrayList<>();
//                        e.getKnowledge().getSentences().stream().filter(e0 -> e0 != null &&
//                                StringUtils.isNoneBlank(e0.getSentence(), e0.getSentence_audio())).forEach(e0 -> {
//                            Map<String, Object> suggest = new HashMap<>();
//                            suggest.put("orginal", e0.getSentence());
//                            suggest.put("translate", "");
//                            suggest.put("audio", e0.getSentence_audio());
//                            suggests.add(suggest);
//                        });
//                        talkSceneList.get(index - 1).setSuggestion(suggests);
//                    }
                });
            }
        }
        return talkSceneList;
    }

    private Map<String, String> getRoleImageMap(String lessonId) {
        List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionsByLessonIds(Collections.singletonList(lessonId), Subject.ENGLISH.getId());
        List<AIQuestion> aiQuestions = new ArrayList<>();
        for (NewQuestion newQuestion : newQuestionList) {
            if (newQuestion.getContent() == null || CollectionUtils.isEmpty(newQuestion.getContent().getSubContents())) {
                continue;
            }
            if (MapUtils.isEmpty(newQuestion.getContent().getSubContents().get(0).getExtras())) {
                continue;
            }
            String content = newQuestion.getContent().getSubContents().get(0).getExtras().get("ai_teacher");
            if (StringUtils.isBlank(content)) {
                continue;
            }
            AIQuestion aiQuestion = JsonUtils.fromJson(content, AIQuestion.class);
            if (aiQuestion == null || aiQuestion.getType() == null) {
                continue;
            }
            aiQuestion.setId(newQuestion.getId());
            aiQuestions.add(aiQuestion);
        }
        AIQuestion aiQuestion = aiQuestions.stream().filter(e -> CollectionUtils.isNotEmpty(e.getRoles())).findFirst().orElse(null);
        Map<String, String> npcRoleMap = new HashMap<>();
        if (aiQuestion != null) {
            aiQuestion.getRoles().forEach(e -> {
                if (e != null) {
                    npcRoleMap.put(e.getName(), e.getImage());
                }
            });
        }
        return npcRoleMap;
    }
}
