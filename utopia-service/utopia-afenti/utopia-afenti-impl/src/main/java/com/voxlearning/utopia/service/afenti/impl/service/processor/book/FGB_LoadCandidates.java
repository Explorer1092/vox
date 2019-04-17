package com.voxlearning.utopia.service.afenti.impl.service.processor.book;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchGradeBookContext;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanUnitRankManagerPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_GRADE;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;

/**
 * 获取已经生成阿分题学习城堡关卡的教材id
 *
 * @author Ruib
 * @since 2016/7/13
 */
@Named
public class FGB_LoadCandidates extends SpringContainerSupport implements IAfentiTask<FetchGradeBookContext> {
    @Inject private AfentiLearningPlanUnitRankManagerPersistence afentiLearningPlanUnitRankManagerPersistence;

    @Override
    public void execute(FetchGradeBookContext context) {
        ClazzLevel clazzLevel = context.getClazzLevel();
        Subject subject = context.getSubject();
        if (!AVAILABLE_SUBJECT.contains(subject) || (!(clazzLevel.getLevel() > 6 && clazzLevel.getLevel() < 10 && subject == Subject.ENGLISH) //中学英语
                && !AVAILABLE_GRADE.contains(clazzLevel))) {
            logger.error("FGB_LoadBookIdsWithAfentiRank Wrong clazzLevel {} or subject {}", clazzLevel, subject);
            context.errorResponse();
            return;
        }

        List<String> bookIds = new ArrayList<>();
        if (context.getAfentiLearningType() == AfentiLearningType.preparation) {
            bookIds = afentiLearningPlanUnitRankManagerPersistence.findAllBookIdsForPreparation();
        } else if (context.getAfentiLearningType() == AfentiLearningType.castle) {
            bookIds = afentiLearningPlanUnitRankManagerPersistence.findAllBookIds();
        } else if (context.getAfentiLearningType() == AfentiLearningType.review) {
            bookIds = afentiLearningPlanUnitRankManagerPersistence.findAllBookIdsForReview();
        }
        if (CollectionUtils.isEmpty(bookIds)) {
            logger.error("FGB_LoadBookIdsWithAfentiRank Afenti rank not exist!");
            context.errorResponse();
            return;
        }
        // 处理新的bookId
        Set<String> bookIdSet = new HashSet<>();
        for (String bookId : bookIds) {
            // 屏蔽语文的老的关卡教材数据
            if (context.getAfentiLearningType() == AfentiLearningType.castle && context.getSubject() == Subject.CHINESE && !StringUtils.contains(bookId, "N_")) {
                continue;
            }
            bookIdSet.add(AfentiUtils.getBookIdByNewBookId(bookId));
        }
        context.getCandidates().addAll(bookIdSet);
    }
}
