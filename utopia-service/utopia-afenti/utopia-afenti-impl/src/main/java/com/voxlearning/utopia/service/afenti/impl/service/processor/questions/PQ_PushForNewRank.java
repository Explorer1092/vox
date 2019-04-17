package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.api.psr.entity.PsrReviewType;
import com.voxlearning.athena.api.psr.entity.PsrSectionPak;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.athena.AfentiPushQuestionServiceClient;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.NO_QUESTION;

/**
 * Created by Summer on 2017/8/17.
 */
@Named
public class PQ_PushForNewRank extends SpringContainerSupport implements IAfentiTask<PushQuestionContext> {
    @Inject private AfentiPushQuestionServiceClient afentiPushQuestionServiceClient;

    @Override
    public void execute(PushQuestionContext context) {
        UnitRankType unitRankType = UtopiaAfentiConstants.getUnitType(context.getUnitId());
        // 终极关卡不走这里的推题
        if (!context.getIsNewRankBook() || unitRankType == UnitRankType.ULTIMATE) {
            return;
        }
        List<PsrSectionPak> questions;
        try {
            if (unitRankType == UnitRankType.COMMON) {
                // 走正常单元推题接口
                questions = afentiPushQuestionServiceClient.getPsrLoader().loadPsrSectionRecommendation(context.getStudentId(),
                        context.getBookId(), context.getUnitId(), context.getSectionId(), context.getSubject(), context.getRank());
            } else {
                // 只有语文有期中期末
                questions = afentiPushQuestionServiceClient.getPsrLoader().loadPsrReviewRecommendation(context.getStudentId(), context.getBookId(),
                        context.getSubject(), context.getRank(), getPsrReviewType(unitRankType));
            }
        } catch (Exception ex) {
            logger.error("PQ_PushForNewRanks afenti call athena error  subject:" + context.getSubject().name() + ",studentId:" + context.getStudentId() + ",bookId:" + context.getBookId()
                    + ",unitId:" + context.getUnitId() + ",sectionId:" + context.getSectionId() + ",rank:" + context.getRank() + ",unitRankType:" + unitRankType, ex);
            context.setErrorCode(NO_QUESTION.getCode());
            context.errorResponse(NO_QUESTION.getInfo());
            return;
        }

        // 没题或者小于3道题 不进行
        if (CollectionUtils.isEmpty(questions) || questions.size() < 3) {
            logger.error("New afenti call athena no qid  subject:" + context.getSubject().name() + ",studentId:" + context.getStudentId() + ",bookId:" + context.getBookId()
            + ",unitId:" + context.getUnitId() + ",sectionId:" + context.getSectionId() + ",rank:" + context.getRank() + ",unitRankType:" + unitRankType);
            context.setErrorCode(NO_QUESTION.getCode());
            context.errorResponse(NO_QUESTION.getInfo());
            return;
        }
        context.setNewRankQuestions(questions);

    }

    private PsrReviewType getPsrReviewType(UnitRankType unitRankType) {
        if (unitRankType == UnitRankType.MIDTERM) {
            return PsrReviewType.MIDTERM_REVIEW;
        }
        if (unitRankType == UnitRankType.TERMINAL) {
            return PsrReviewType.FINALTERM_REVIEW;
        }
        return PsrReviewType.NONE;
    }
}
