package com.voxlearning.utopia.service.ai.impl.service.processor.questionresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.entity.AIUserBookResult;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultPlan;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;

import javax.inject.Named;
import java.util.List;
import java.util.Optional;


@Named
public class AQP_SaveBookResult extends AbstractAiSupport implements IAITask<AIUserQuestionContext> {

    @Override
    public void execute(AIUserQuestionContext context) {
        if (!context.getAiUserQuestionResultRequest().getLessonLast()) {
            return;
        }

        String bookId = Optional.ofNullable(context.getAiUserQuestionResultRequest().getUnitId())
                .map(e -> newContentLoaderClient.loadBookCatalogByCatalogId(e))
                .map(NewBookCatalog::bookId)
                .orElse("");
        if (!chipCourseSupport.TRAVEL_ENGLISH_BOOK_ID.equals(bookId)) { //fixme 不是旅行口语的暂时不生成报告
            return;
        }
        List<AIUserUnitResultPlan> aiUserUnitResultPlans = context.getAiUserUnitResultPlans();
        if (CollectionUtils.isEmpty(aiUserUnitResultPlans) || aiUserUnitResultPlans.size() < 10) {
            return;
        }

        List<AIUserBookResult> bookResults = aiUserBookResultDao.loadByUserId(context.getUser().getId());
        if (CollectionUtils.isNotEmpty(bookResults) &&
                bookResults.stream().filter(e -> e.getBookId().equals(bookId)).findFirst().orElse(null) != null) {
            return;
        }

        AIUserBookResult aiUserBookResult = chipsContentService.initBookResult(context.getUser(), aiUserUnitResultPlans, bookId);
        if (aiUserBookResult == null) {
            return;
        }

        aiUserBookResultDao.insert(aiUserBookResult);


        notifyBookResult(context.getUser().getId(), aiUserBookResult.getBookId());
    }

}
