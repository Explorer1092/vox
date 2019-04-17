package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class CQR_SaveLessonResult_Common extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {
    private static List<String> SPECIAL_TYPES = Arrays.asList("video_conversation", "task_conversation");

    @Inject
    private AICacheSystem aiCacheSystem;

    @Override
    public void execute(ChipsQuestionResultContext context) {
        if (SPECIAL_TYPES.contains(context.getLesson().getJsonData().getLesson_type().name())) {
            return;
        }
        Set<String> qids = Optional.ofNullable(context.getLesson().getJsonData())
                .map(StoneLessonData.Lesson::getContent_ids)
                .map(ids -> ids.stream().collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        // 计算lesson的结果
        List<AIUserQuestionResultHistory> lessonList = aiUserQuestionResultHistoryDao.loadByUidAndLessonId(context.getUserId(), context.getChipsQuestionResultRequest().getLessonId())
                .stream()
                .filter(res -> qids.contains(res.getQid()))
                .collect(Collectors.toList());
        AIUserLessonResultHistory lessonResultHistory = AIUserLessonResultHistory.build(context.getChipsQuestionResultRequest().getBookId(),
                context.getChipsQuestionResultRequest().getUnitId(),
                context.getChipsQuestionResultRequest().getLessonId(),
                context.getLesson().getJsonData().getLesson_type(), context.getUserId());
        int star = 0;
        if (CollectionUtils.isNotEmpty(lessonList)) {
            int score = calculateLessonScore(lessonList);
            lessonResultHistory.setScore(score);
            star = CourseRuleUtil.scoreToStar(score);
        } else  {
            switch (context.getLesson().getJsonData().getLesson_type()) {
                case watch_video_lesson:
                case review_learn_lesson:
                    star = 3;
                    break;
            }
        }
//        lessonResultHistory.setStar(star);
        lessonResultHistory.setCurrentStar(star);
        AIUserLessonResultHistory lastHistory = aiUserLessonResultHistoryDao.load(context.getUserId(), context.getChipsQuestionResultRequest().getLessonId());
        int tempStar = star;
        Integer maxStar = Optional.ofNullable(lastHistory).map(e -> e.getStar()).map(e ->  e > tempStar ? e : tempStar).orElse(tempStar);
//        context.getResult().add("star", star).add("end", true);
        context.getResult().add("star", maxStar).add("end", true);
        lessonResultHistory.setStar(maxStar);
        aiUserLessonResultHistoryDao.disableOld(context.getUserId(), context.getChipsQuestionResultRequest().getLessonId());
        String sessionId = aiCacheSystem.getUserTalkFeedSessionCacheManager().getSessionId(context.getUserId(), context.getLesson().getId());
        if (StringUtils.isNotBlank(sessionId)) {
            lessonResultHistory.setId(sessionId);
            aiUserLessonResultHistoryDao.upsert(lessonResultHistory);
        } else {
            aiUserLessonResultHistoryDao.insert(lessonResultHistory);
        }
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
