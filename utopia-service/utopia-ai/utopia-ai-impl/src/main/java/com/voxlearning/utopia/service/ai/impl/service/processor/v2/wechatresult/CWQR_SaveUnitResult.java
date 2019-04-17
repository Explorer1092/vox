package com.voxlearning.utopia.service.ai.impl.service.processor.v2.wechatresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsWechatQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class CWQR_SaveUnitResult extends AbstractAiSupport implements IAITask<ChipsWechatQuestionResultContext> {

    @Override
    public void execute(ChipsWechatQuestionResultContext context) {
        Long userId = context.getUserId();
        // 课程结束 判断是否已有单元结果
        ChipsWechatUserUnitResultHistory unitResultHistory = chipsWechatUserUnitResultHistoryDao.load(context.getUserId(), context.getUnit().getId());
        if ((unitResultHistory != null) || (context.getChipsQuestionResultRequest().getUnitLast())) {
            ChipsWechatUserUnitResultHistory newUnitResult = ChipsWechatUserUnitResultHistory.build(userId, context.getChipsQuestionResultRequest().getUnitId(), ChipsUnitType.dialogue_practice,
                    context.getChipsQuestionResultRequest().getBookId());
            doHandle(newUnitResult, context.getUnitLessonResultList(), context.getUnitQuestionResultList());
            chipsWechatUserUnitResultHistoryDao.disabled(userId, context.getUnit().getId());
            chipsWechatUserUnitResultHistoryDao.insert(newUnitResult);
        }
    }


    protected void doHandle(ChipsWechatUserUnitResultHistory newUnitResult, List<ChipsWechatUserLessonResultHistory> lessonResultHistoryList, List<ChipsWechatUserQuestionResultHistory> questionResultHistoryList) {
        int totalScore = lessonResultHistoryList.stream().mapToInt(ChipsWechatUserLessonResultHistory::getScore).sum();
        // 计算单元分数
        int unitSCore = new BigDecimal(totalScore).divide(new BigDecimal(lessonResultHistoryList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        unitSCore = Math.min(100, Math.max(0, unitSCore));
        newUnitResult.setScore(unitSCore);
        newUnitResult.setStar(CourseRuleUtil.scoreToStar(unitSCore));
        newUnitResult.setFluency(calFluency(questionResultHistoryList));
        newUnitResult.setExpress(calExpress(questionResultHistoryList));
        newUnitResult.setIndependent(calSelf(questionResultHistoryList));
        newUnitResult.setListening(calListening(questionResultHistoryList));
        newUnitResult.setPronunciation(calPronunciation(questionResultHistoryList));
    }


    private int calListening(List<ChipsWechatUserQuestionResultHistory>  questionResultHistoryList) {
        List<ChipsWechatUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue)
                .filter(h -> h.getScore() != null && h.getScore() > 0 )
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        int score = new BigDecimal(list.stream().mapToInt(ChipsWechatUserQuestionResultHistory::getScore).sum()).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        return Math.min(100, Math.max(60, score));
    }

    private int calPronunciation(List<ChipsWechatUserQuestionResultHistory> questionResultHistoryList) {
        List<ChipsWechatUserQuestionResultHistory> list = questionResultHistoryList.stream().filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue|| e.getQuestionType() == ChipsQuestionType.sentence_repeat).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        // 发音--这里有个坑:发音APP传来的分数是8分制的需要转一下
        int t_p_score = list.stream().mapToInt(e -> {
            if (Integer.compare(e.getPronunciation(), 8) <= 0) {
                return new BigDecimal(e.getPronunciation()).multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP).intValue();
            }
            return e.getPronunciation();
        }).sum();
        int p_score = new BigDecimal(t_p_score).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        p_score = Math.min(100, Math.max(60, p_score));
        return p_score;
    }

    private int calFluency(List<ChipsWechatUserQuestionResultHistory> questionResultHistoryList) {
        List<ChipsWechatUserQuestionResultHistory> fluencyList = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue)
                .filter(h -> h.getFluency() != null && h.getFluency() > 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fluencyList)) {
            return 0;
        }
        return Math.min(100, Math.max(60, new BigDecimal(fluencyList.stream().mapToInt(ChipsWechatUserQuestionResultHistory::getFluency).sum())
                .divide(new BigDecimal(fluencyList.size()), 2, BigDecimal.ROUND_HALF_UP).intValue()));
    }

    private int calSelf(List<ChipsWechatUserQuestionResultHistory> questionResultHistoryList) {
        List<ChipsWechatUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue)
                .filter(h -> h.getScore() != null && h.getScore() > 0 )
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        int deductScore = new BigDecimal(list.stream().filter(e -> e.getDeductScore() != null).mapToInt(ChipsWechatUserQuestionResultHistory::getDeductScore).sum()).intValue();
        return Math.min(100, Math.max(60, 100 - deductScore));
    }

    private int calExpress(List<ChipsWechatUserQuestionResultHistory> questionResultHistoryList) {
        List<ChipsWechatUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue)
                .filter(h -> h.getScore() != null && h.getScore() > 0 )
                .filter(h -> h.getCompleteScore() != null && h.getCompleteScore() > 0 )
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        int score = new BigDecimal(list.stream().mapToInt(ChipsWechatUserQuestionResultHistory::getScore).sum()).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(0.5D)).intValue();
        int complete = new BigDecimal(list.stream().mapToInt(ChipsWechatUserQuestionResultHistory::getCompleteScore).sum()).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(0.5D)).intValue();
        return Math.min(100, Math.max(60, score + complete));
    }
}
