package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.ai.data.GradeReportConfig;
import com.voxlearning.utopia.service.ai.entity.AIUserBookResult;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultPlan;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Named
public class CQR_SaveBookResult extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @Override
    public void execute(ChipsQuestionResultContext context) {
        if (!context.getChipsQuestionResultRequest().getLessonLast()) {
            return;
        }

        String bookId = Optional.ofNullable(context.getChipsQuestionResultRequest().getBookId())
                .orElse("");
        GradeReportConfig gradeReportConfig = chipsContentService.loadGradeReportConfig().stream().filter(e -> e.getBook().equals(bookId)).findFirst().orElse(null);
        if (gradeReportConfig == null) { //fixme 不是旅行口语的暂时不生成报告
            return;
        }

        List<AIUserUnitResultPlan> aiUserUnitResultPlans = context.getAiUserUnitResultPlans();
        if (CollectionUtils.isEmpty(aiUserUnitResultPlans)) {
            return;
        }

        aiUserUnitResultPlans = aiUserUnitResultPlans.stream().filter(e -> CollectionUtils.isEmpty(gradeReportConfig.getUnits()) || gradeReportConfig.getUnits().contains(e.getUnitId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(gradeReportConfig.getUnits()) || aiUserUnitResultPlans.size() < gradeReportConfig.getUnits().size()) {
            return;
        }

        List<AIUserBookResult> bookResults = aiUserBookResultDao.loadByUserId(context.getUserId());
        if (CollectionUtils.isNotEmpty(bookResults) &&
                bookResults.stream().filter(e -> e.getBookId().equals(bookId)).findFirst().orElse(null) != null) {
            return;
        }

        AIUserBookResult aiUserBookResult = chipsContentService.initBookResult(userLoaderClient.loadUser(context.getUserId()), aiUserUnitResultPlans, bookId);
        if (aiUserBookResult != null) {
            aiUserBookResultDao.insert(aiUserBookResult);
        }

        notifyBookResult(context.getUserId(), bookId);
    }
}
