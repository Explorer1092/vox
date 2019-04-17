package com.voxlearning.utopia.service.ai.impl.service.processor.questionresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.data.AITalkScene;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Named
public class AQP_UpsertDialogueLessonResult extends AbstractAiSupport implements IAITask<AIUserQuestionContext> {

    @Inject
    private AICacheSystem aiCacheSystem;

    @Override
    public void execute(AIUserQuestionContext context) {
        if (context.getAiUserQuestionResultRequest().getLessonType() != LessonType.Dialogue) {
            return;
        }

        Long userId = context.getUser().getId();
        String lessonId = context.getAiUserQuestionResultRequest().getLessonId();

        List<AITalkScene> talkList = aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().loadTalkList(String.valueOf(userId), lessonId);

        // 计算lesson的结果
        List<AIUserQuestionResultHistory> lessonList = aiUserQuestionResultHistoryDao.loadByUidAndLessonId(context.getUser().getId(), context.getAiUserQuestionResultRequest().getLessonId());
        if (CollectionUtils.isNotEmpty(lessonList)) {
            int score = calculateLessonScore(lessonList);
            int star = CourseRuleUtil.scoreToStar(score);
            AIUserLessonResultHistory lessonResultHistory = new AIUserLessonResultHistory();
            lessonResultHistory.setUserId(userId);
            lessonResultHistory.setUnitId(context.getAiUserQuestionResultRequest().getUnitId());
            lessonResultHistory.setLessonId(lessonId);
            lessonResultHistory.setFinished(true);
            lessonResultHistory.setLessonType(context.getAiUserQuestionResultRequest().getLessonType());
            lessonResultHistory.setStar(star);
            lessonResultHistory.setScore(score);
            lessonResultHistory.setDisabled(false);
            Map<String, Object> extMap = new HashMap<>();
            extMap.put("talkList", talkList);
            lessonResultHistory.setExt(extMap);

            Integer independent = lessonList.stream().filter(e -> e.getIndependent() != null).mapToInt(AIUserQuestionResultHistory::getIndependent).sum();
            Integer listening = lessonList.stream().filter(e -> e.getListening() != null).mapToInt(AIUserQuestionResultHistory::getListening).sum();
            Integer express=lessonList.stream().filter(e -> e.getExpress() != null).mapToInt(AIUserQuestionResultHistory::getExpress).sum();
            Integer fluency = lessonList.stream().filter(e -> e.getFluency() != null).mapToInt(AIUserQuestionResultHistory::getFluency).sum();
            Integer pronunciation = lessonList.stream().filter(e -> e.getPronunciation() != null).mapToInt(AIUserQuestionResultHistory::getPronunciation).sum();
            // cast to 100
            pronunciation = new BigDecimal(pronunciation).multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP).intValue();
            lessonResultHistory.setIndependent(independent);
            lessonResultHistory.setListening(listening);
            lessonResultHistory.setExpress(express);
            lessonResultHistory.setFluency(new BigDecimal(fluency));
            lessonResultHistory.setPronunciation(new BigDecimal(pronunciation));
            lessonResultHistory.setCreateDate(new Date());
            lessonResultHistory.setUpdateDate(new Date());
            lessonResultHistory.setId(context.getSessionId());
            aiUserLessonResultHistoryDao.disableOld(context.getUser().getId(), lessonId);
            aiUserLessonResultHistoryDao.upsert(lessonResultHistory);

            context.getResult().add("star", star).add("end", true);
        }

    }


    private int calculateLessonScore(List<AIUserQuestionResultHistory> lessonList) {
        if (CollectionUtils.isEmpty(lessonList)) {
            return 0;
        }
        int totalScore = lessonList.stream().filter(e -> e.getScore() != null && Integer.compare(e.getScore(), 0) > 0).mapToInt(AIUserQuestionResultHistory::getScore).sum();
        int avg = new BigDecimal(totalScore).divide(new BigDecimal(lessonList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        int totalDeductScore = lessonList.stream().filter(e -> e.getDeductScore() != null).mapToInt(AIUserQuestionResultHistory::getDeductScore).sum();
        return avg - totalDeductScore > 0 ? avg - totalDeductScore : 0;
    }
}
