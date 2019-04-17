package com.voxlearning.utopia.service.ai.impl.service.processor.v2.wechatresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.AITalkScene;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.context.ChipsWechatQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存
 */
@Named
public class CWQR_SaveLessonResult_Dialogue extends AbstractAiSupport implements IAITask<ChipsWechatQuestionResultContext> {

    @Inject
    private AICacheSystem aiCacheSystem;

    @Override
    public void execute(ChipsWechatQuestionResultContext context) {
        Long userId = context.getUserId();
        String lessonId = context.getLesson().getId();
        ChipsWechatUserLessonResultHistory lessonResultHistory = ChipsWechatUserLessonResultHistory.build(context.getChipsQuestionResultRequest().getBookId(),
                context.getChipsQuestionResultRequest().getUnitId(), lessonId, LessonType.video_conversation, userId);
        List<AITalkScene> talkList = aiCacheSystem.getWechatUserDialogueTalkSceneResultCacheManager().loadTalkList(String.valueOf(userId), lessonId);
        // 计算lesson的结果
        List<ChipsWechatUserQuestionResultHistory> lessonList = chipsWechatUserQuestionResultHistoryDao.loadByLessonId(userId, lessonId);
        if (CollectionUtils.isNotEmpty(lessonList)) {
            int score = calculateLessonScore(lessonList);
            lessonResultHistory.setScore(score);
            int star = CourseRuleUtil.scoreToStar(score);
            lessonResultHistory.setStar(star);
            Map<String, Object> extMap = new HashMap<>();
            extMap.put("talkList", talkList);
            lessonResultHistory.setExt(extMap);
            lessonResultHistory.setId(aiCacheSystem.getUserTalkFeedSessionCacheManager().getSessionId(userId, lessonId));
            context.getResult().add("star", star).add("end", true);
        }
        chipsWechatUserLessonResultHistoryDao.disableOld(userId, lessonId, context.getChipsQuestionResultRequest().getUnitId());
        chipsWechatUserLessonResultHistoryDao.upsert(lessonResultHistory);
    }


    private int calculateLessonScore(List<ChipsWechatUserQuestionResultHistory> lessonList ) {
        if (CollectionUtils.isEmpty(lessonList)) {
            return 0;
        }
        int totalScore = lessonList.stream().filter(e -> e.getScore() != null && Integer.compare(e.getScore(), 0) > 0).mapToInt(ChipsWechatUserQuestionResultHistory::getScore).sum();
        int score = new BigDecimal(totalScore).divide(new BigDecimal(lessonList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        return Math.min(Math.max(0, score), 100);
    }

}
