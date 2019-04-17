package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.data.AITalkScene;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 保存
 */
@Named
public class CQR_SaveLessonResult_Dialogue extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @Inject
    private AICacheSystem aiCacheSystem;

    @Override
    public void execute(ChipsQuestionResultContext context) {
        if (!(context.getLesson().getJsonData().getLesson_type() == LessonType.video_conversation)) {
            return;
        }

        Long userId = context.getUserId();
        String lessonId = context.getLesson().getId();
        AIUserLessonResultHistory lessonResultHistory = AIUserLessonResultHistory.build(context.getChipsQuestionResultRequest().getBookId(),
                context.getChipsQuestionResultRequest().getUnitId(), lessonId, LessonType.video_conversation, userId);
        List<AITalkScene> talkList = aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().loadTalkList(String.valueOf(userId), lessonId);
        // 计算lesson的结果
        List<AIUserQuestionResultHistory> lessonList = aiUserQuestionResultHistoryDao.loadByUidAndLessonId(userId, lessonId);
        if (CollectionUtils.isNotEmpty(lessonList)) {
            int score = calculateLessonScore(lessonList);
            int star = CourseRuleUtil.scoreToStar(score);
//            lessonResultHistory.setStar(star);
            lessonResultHistory.setCurrentStar(star);
            AIUserLessonResultHistory lastHistory = aiUserLessonResultHistoryDao.load(userId, lessonId);
            Integer maxStar = Optional.ofNullable(lastHistory).map(e -> e.getStar()).map(e ->  e > star ? e : star).orElse(star);
            lessonResultHistory.setStar(maxStar);
            lessonResultHistory.setScore(score);
            Map<String, Object> extMap = new HashMap<>();
            extMap.put("talkList", talkList);
            lessonResultHistory.setExt(extMap);
            lessonResultHistory.setId(aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().getSessonId(userId, lessonId));
            context.getResult().add("star", maxStar).add("end", true);
//            context.getResult().add("star", star).add("end", true);
        }
        aiUserLessonResultHistoryDao.disableOld(userId, lessonId);
        aiUserLessonResultHistoryDao.upsert(lessonResultHistory);
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
