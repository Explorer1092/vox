package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Named
public class ShortLevelHandler extends UnitResultHandler {

    @Override
    protected ChipsUnitType type() {
        return ChipsUnitType.short_lesson;
    }

    private static Set<LessonType> TALK_LESSON_TYPES = Arrays.asList(LessonType.video_conversation, LessonType.task_conversation, LessonType.mock_test_lesson_2).stream().collect(Collectors.toSet());

    @Override
    protected void doHandle(AIUserUnitResultHistory unitResultHistory, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList) {
        int totalStar = lessonResultHistoryList.stream().mapToInt(AIUserLessonResultHistory::getStar).sum();

        // 计算单元星星
        int unitStar = new BigDecimal(totalStar).divide(new BigDecimal(lessonResultHistoryList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();

        int totalScore = lessonResultHistoryList.stream().mapToInt(AIUserLessonResultHistory::getScore).sum();

        // 计算单元分数
        int unitSCore = new BigDecimal(totalScore).divide(new BigDecimal(lessonResultHistoryList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();


        // 发音--这里有个坑:发音APP传来的分数是8分制的需要转一下
        List<AIUserQuestionResultHistory> subPro = questionResultHistoryList.stream().filter(e -> e.getPronunciation() != null).collect(Collectors.toList());
        int t_p_score = subPro.stream().mapToInt(e -> {
            if (Integer.compare(e.getPronunciation(), 8) <= 0) {
                return new BigDecimal(e.getPronunciation()).multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP).intValue();
            }
            return e.getPronunciation();
        }).sum();
        int p_score = new BigDecimal(t_p_score).divide(new BigDecimal(subPro.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        p_score = Math.min(100, Math.max(59, p_score));

        // 过滤出情景对话跟任务的题目
        List<AIUserQuestionResultHistory> subHis = questionResultHistoryList.stream().filter(h -> TALK_LESSON_TYPES.contains(h.getLessonType()))
                .collect(Collectors.toList());
        int size = subHis.size() > 0 ? subHis.size() : 1;

        // 流利度--去掉热身环节的打分
        int t_f_score = subHis.stream().filter(e -> e.getFluency() != null).mapToInt(AIUserQuestionResultHistory::getFluency).sum();
        int f_score = new BigDecimal(t_f_score).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP).intValue();
        f_score = Math.max(59, f_score);

        // 回答总得分
        int t_q_score = subHis.stream().mapToInt(AIUserQuestionResultHistory::getScore).sum();
        // 总扣分
        int t_deduct_score = subHis.stream().filter(e -> e.getDeductScore() != null).mapToInt(AIUserQuestionResultHistory::getDeductScore).sum();
        // 完整性总得分
        int t_c_score = subHis.stream().filter(e -> e.getCompleteScore() != null).mapToInt(AIUserQuestionResultHistory::getCompleteScore).sum();

        // 听力
        int l_score = new BigDecimal(t_q_score).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP).intValue() - t_deduct_score;
        l_score = Math.max(59, l_score);

        // 表达
        int e_score = new BigDecimal(t_q_score).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP)
                .divide(new BigDecimal(2), 0, BigDecimal.ROUND_HALF_UP).intValue() +
                new BigDecimal(t_c_score).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP)
                        .divide(new BigDecimal(2), 0, BigDecimal.ROUND_HALF_UP).intValue();
        e_score = Math.max(59, e_score);
        // 独立完成
        int i_score = 100 - t_deduct_score;
        i_score = Math.max(59, i_score);

        unitResultHistory.setStar(unitStar);
        unitResultHistory.setScore(unitSCore);
        unitResultHistory.setExpress(e_score);
        unitResultHistory.setFluency(f_score);
        unitResultHistory.setIndependent(i_score);
        unitResultHistory.setListening(l_score);
        unitResultHistory.setPronunciation(p_score);
    }

}
