package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.cache.manager.UserTaskRoleResultV2CacheManager;
import com.voxlearning.utopia.service.ai.data.AITalkScene;
import com.voxlearning.utopia.service.ai.data.AITalkSceneResult;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.data.StoneTalkNpcQuestionData;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;
import com.voxlearning.utopia.service.ai.entity.AIDialogueTaskConfig;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.persistence.AIDialogueTaskConfigDao;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.impl.support.UserInfoSupport;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class CQR_SaveLessonResult_Task extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {
    @Inject
    private AICacheSystem aiCacheSystem;
    @Inject
    private AIDialogueTaskConfigDao dialogueTaskConfigDao;
    @Override
    public void execute(ChipsQuestionResultContext context) {
        if (!context.getLesson().getJsonData().getLesson_type().equals(LessonType.task_conversation)) {
            return;
        }
        String roleName = context.getChipsQuestionResultRequest().getRoleName();
        if (StringUtils.isBlank(roleName)) {
            context.errorResponse("角色为空");
            return;
        }
        String tip = "";
        UserTaskRoleResultV2CacheManager taskRoleResultCacheManager = aiCacheSystem.getUserTaskRoleResultV2CacheManager();
        boolean stopTask = true;
        List<String> npcNames;
        String lessonId = context.getLesson().getId();
        taskRoleResultCacheManager.addRecord(context.getUserId(), lessonId, roleName, context.getChipsQuestionResultRequest());

        if (context.getQuestionType() == ChipsQuestionType.task_conversation) {
            AIDialogueTaskConfig aiDialogueTaskConfig = dialogueTaskConfigDao.load(lessonId);
            if (aiDialogueTaskConfig == null || CollectionUtils.isEmpty(aiDialogueTaskConfig.getNpcs())) {
                context.errorResponse("对话配置为空");
                return;
            }
            npcNames = aiDialogueTaskConfig.getNpcs().stream().map(AIDialogueTaskConfig.Npc::getNpcName).collect(Collectors.toList());
            if (Boolean.TRUE.equals(aiDialogueTaskConfig.getAllNpc())) {
                Map<String, ChipsQuestionResultRequest> requestMap = taskRoleResultCacheManager.loadRecord(context.getUserId(), lessonId, npcNames);
                if (MapUtils.isNotEmpty(requestMap) && requestMap.size() == npcNames.size()) {
                    doSave(context, requestMap.values(), aiCacheSystem.getUserTaskTalkSceneResultCacheManager().loadRecords(context.getChipsQuestionResultRequest().getUsercode(), lessonId, npcNames), getRoleImageMap(aiDialogueTaskConfig));
                    stopTask = false;
                } else {
                    tip = "继续下一个目标进行对话吧~ ";
                }
            } else {
                String rightRpcName = aiDialogueTaskConfig.getNpcs().stream()
                        .filter(e -> "Success".equalsIgnoreCase(e.getStatus()) && roleName.equalsIgnoreCase(e.getNpcName()))
                        .findFirst()
                        .map(AIDialogueTaskConfig.Npc::getNpcName)
                        .orElse("");
                if (StringUtils.isNotBlank(rightRpcName)) {//对于寻找一个正确的人的任务对话只需要计算正确的人的对话
                    doSave(context, Arrays.asList(context.getChipsQuestionResultRequest()),
                            aiCacheSystem.getUserTaskTalkSceneResultCacheManager().loadRecords(context.getChipsQuestionResultRequest().getUsercode(), lessonId, Arrays.asList(rightRpcName)), getRoleImageMap(aiDialogueTaskConfig));
                    stopTask = false;
                } else {
                    tip = roleName + "不是你要找的人，换个人试试吧!";
                }
            }
        } else {
            List<StoneTalkNpcQuestionData> talkNpcQuestions = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singleton(lessonId)))
                    .map(e -> e.get(lessonId))
                    .map(StoneLessonData::newInstance)
                    .map(StoneLessonData::getJsonData)
                    .filter(e -> CollectionUtils.isNotEmpty(e.getContent_ids()))
                    .map(e -> stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(e.getContent_ids()))
                    .map(e -> e.values().stream().filter(e1 -> !ChipsQuestionType.task_conversation.name().equalsIgnoreCase(e1.getSchemaName())).map(StoneTalkNpcQuestionData::newInstance).collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
            if (CollectionUtils.isEmpty(talkNpcQuestions)) {
                context.errorResponse("对话题目为空");
                return;
            }
            boolean allNpc = (talkNpcQuestions.size() ==
                    talkNpcQuestions.stream().filter(e -> e.getJsonData() != null && Boolean.FALSE.equals(e.getJsonData().getStatus())).collect(Collectors.toList()).size());
            npcNames = talkNpcQuestions.stream().map(StoneTalkNpcQuestionData::getJsonData).map(StoneTalkNpcQuestionData.Npc::getNpc_name).collect(Collectors.toList());
            if (allNpc) {
                Map<String, ChipsQuestionResultRequest> requestMap = taskRoleResultCacheManager.loadRecord(context.getUserId(), lessonId, npcNames);
                if (requestMap.size() == npcNames.size()) {
                    stopTask = false;
                    doSaveV2(context, aiCacheSystem.getUserTaskTalkSceneResultCacheManager().loadRecords(context.getChipsQuestionResultRequest().getUsercode(), lessonId, npcNames));
                } else {
                    tip = "继续下一个目标进行对话吧~ ";
                }
            } else {
                boolean pass = talkNpcQuestions.stream()
                        .filter(e -> e.getJsonData() != null && roleName.equalsIgnoreCase(e.getJsonData().getNpc_name()))
                        .findFirst()
                        .map(StoneTalkNpcQuestionData::getJsonData)
                        .map(StoneTalkNpcQuestionData.Npc::getStatus)
                        .orElse(false);
                if (pass) {
                    stopTask = false;
                    doSaveV2(context, aiCacheSystem.getUserTaskTalkSceneResultCacheManager().loadRecords(context.getChipsQuestionResultRequest().getUsercode(), lessonId, Arrays.asList(roleName)));
                } else {
                    tip = roleName + "不是你要找的人，换个人试试吧!";
                }
            }
        }

        if (!stopTask) {
            taskRoleResultCacheManager.deleteRecords(context.getUserId(), lessonId, npcNames);
        } else {
            context.getResult().set("end", false).set("tip", tip);
        }
        context.setTerminateTask(stopTask);
    }

    private void doSaveV2(ChipsQuestionResultContext context, List<AITalkSceneResult> talkSceneResults) {
        List<AITalkScene> aiTalkScenes = calculateTalkScene(talkSceneResults, context, Collections.emptyMap());
        Map<String, Object> extMap = new HashMap<>();
        extMap.put("talkList", aiTalkScenes);
        // 计算lesson的结果
        List<AIUserQuestionResultHistory> lessonList = aiUserQuestionResultHistoryDao.loadByUidAndLessonId(context.getUserId(), context.getLesson().getId());
        if (CollectionUtils.isNotEmpty(lessonList)) {
            Long userId = context.getUserId();
            String lessonId= context.getLesson().getId();
            int score = calculateLessonScore(lessonList);
            int star = CourseRuleUtil.scoreToStar(score);

            AIUserLessonResultHistory lessonResultHistory = AIUserLessonResultHistory.build(context.getChipsQuestionResultRequest().getBookId(),
                    context.getChipsQuestionResultRequest().getUnitId(), lessonId, LessonType.task_conversation, userId);
//            lessonResultHistory.setStar(star);
            lessonResultHistory.setCurrentStar(star);
            AIUserLessonResultHistory lastHistory = aiUserLessonResultHistoryDao.load(userId, lessonId);
            Integer maxStar = Optional.ofNullable(lastHistory).map(e -> e.getStar()).map(e ->  e > star ? e : star).orElse(star);
            lessonResultHistory.setStar(maxStar);
            lessonResultHistory.setScore(score);
            lessonResultHistory.setExt(extMap);
            aiUserLessonResultHistoryDao.disableOld(userId,lessonId);
            aiUserLessonResultHistoryDao.insert(lessonResultHistory);
            context.getResult().add("star", maxStar).add("end", true);
//            context.getResult().add("star", star).add("end", true);
        }
    }

    private void doSave(ChipsQuestionResultContext context, Collection<ChipsQuestionResultRequest> requests, List<AITalkSceneResult> talkSceneResults, Map<String, String> npcImageMap) {
        int score = calculateLessonScore(requests);
        int star = CourseRuleUtil.scoreToStar(score);
        List<AITalkScene> aiTalkScenes = calculateTalkScene(talkSceneResults, context, npcImageMap);
        Map<String, Object> extMap = new HashMap<>();
        extMap.put("talkList", aiTalkScenes);
        // 计算lesson的结果
        List<AIUserQuestionResultHistory> lessonList = aiUserQuestionResultHistoryDao.loadByUidAndLessonId(context.getUserId(), context.getLesson().getId());
        if (CollectionUtils.isNotEmpty(lessonList)) {
            Long userId = context.getUserId();
            String lessonId= context.getLesson().getId();
            AIUserLessonResultHistory lessonResultHistory = AIUserLessonResultHistory.build(context.getChipsQuestionResultRequest().getBookId(),
                    context.getChipsQuestionResultRequest().getUnitId(), lessonId, LessonType.task_conversation, userId);
//            lessonResultHistory.setStar(star);
            lessonResultHistory.setCurrentStar(star);
            AIUserLessonResultHistory lastHistory = aiUserLessonResultHistoryDao.load(userId, lessonId);
            Integer maxStar = Optional.ofNullable(lastHistory).map(e -> e.getStar()).map(e ->  e > star ? e : star).orElse(star);
            lessonResultHistory.setStar(maxStar);
            lessonResultHistory.setScore(score);
            lessonResultHistory.setDisabled(false);
            lessonResultHistory.setExt(extMap);
            aiUserLessonResultHistoryDao.disableOld(userId,lessonId);
            aiUserLessonResultHistoryDao.insert(lessonResultHistory);
            context.getResult().add("star", maxStar).add("end", true);
//            context.getResult().add("star", star).add("end", true);
        }
    }

    private int calculateLessonScore(Collection<ChipsQuestionResultRequest> lessonList) {
        if (CollectionUtils.isEmpty(lessonList)) {
            return 0;
        }
        int totalScore = lessonList.stream().filter(e -> e != null && e.getScore() != null && Integer.compare(e.getScore(), 0) > 0).mapToInt(ChipsQuestionResultRequest::getScore).sum();
        int avg = new BigDecimal(totalScore).divide(new BigDecimal(lessonList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();

        return Math.min(Math.max(0, avg), 100);
    }

    private List<AITalkScene> calculateTalkScene(List<AITalkSceneResult> aiTalkSceneResultList, ChipsQuestionResultContext context, Map<String, String> npcImageMap) {
        if (CollectionUtils.isEmpty(aiTalkSceneResultList)) {
            return Collections.emptyList();
        }
        User user =  userLoaderClient.loadUser(context.getUserId());
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
                aiTalkScene.setRoleImage(UserInfoSupport.getUserRoleImage(user));
                aiTalkScene.setRoleType(AITalkScene.RoleType.Student);
                aiTalkScene.setMedia(voiceUrl);
                talkSceneList.add(aiTalkScene);
            } else {
                TalkResultInfo talkResultInfo = JsonUtils.fromJson(talkSceneResult.getResult(), TalkResultInfo.class);
                if (talkResultInfo == null || CollectionUtils.isEmpty(talkResultInfo.getData())) {
                    continue;
                }
                talkResultInfo.getData().forEach(e -> {
                    if (e.getContent() != null && !StringUtils.isAnyBlank(e.getContent().getAudio(), e.getContent().getCn_translation(), e.getContent().getTranslation())) {
                        AITalkScene aiTalkScene = new AITalkScene();
                        aiTalkScene.setRoleImage(MapUtils.isNotEmpty(npcImageMap) ? npcImageMap.get(e.getName()) : e.getContent().getRole_image());
                        aiTalkScene.setMedia(e.getContent().getAudio());
                        aiTalkScene.setDescription(e.getContent().getCn_translation());
                        aiTalkScene.setRoleType(AITalkScene.RoleType.AITeacher);

                        aiTalkScene.setOriginal(e.getContent().getTranslation());
                        if (StringUtils.isNotBlank(e.getContent().getLevel()) && e.getKnowledge() != null && CollectionUtils.isNotEmpty(e.getKnowledge().getSentences()) &&
                                !e.getContent().getLevel().matches("[e|f|E|F]\\S*")) {
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
                                    suggest.put("orginal", e0.getSentence());
                                    suggest.put("audio", e0.getSentence_audio());
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

    private Map<String, String> getRoleImageMap(AIDialogueTaskConfig aiDialogueTaskConfig) {
        Map<String, String> npcRoleMap = new HashMap<>();
        if (aiDialogueTaskConfig != null && CollectionUtils.isNotEmpty(aiDialogueTaskConfig.getNpcs())) {
            aiDialogueTaskConfig.getNpcs().forEach(e -> {
                if (e != null) {
                    npcRoleMap.put(e.getNpcName(), e.getRoleImage());
                }
            });
        }
        return npcRoleMap;
    }


    private int calculateLessonScore(List<AIUserQuestionResultHistory> lessonList) {
        if (CollectionUtils.isEmpty(lessonList)) {
            return 0;
        }
        int totalScore = lessonList.stream().filter(e -> e.getScore() != null && Integer.compare(e.getScore(), 0) > 0).mapToInt(AIUserQuestionResultHistory::getScore).sum();
        int score = new BigDecimal(totalScore).divide(new BigDecimal(lessonList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        return Math.min(Math.max(0, score), 100);
    }

}
