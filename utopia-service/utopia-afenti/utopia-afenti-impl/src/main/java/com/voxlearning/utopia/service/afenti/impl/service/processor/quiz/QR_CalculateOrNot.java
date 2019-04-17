package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class QR_CalculateOrNot extends SpringContainerSupport implements IAfentiTask<QuizResultContext> {
    @Inject AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(QuizResultContext context) {
        String bookId = context.getBookId();
        String unitId = context.getUnitId();
        Long studentId = context.getStudent().getId();

        // 获取单元测试完成信息记录，如果该记录存在，表示此次提交是错题订正
        AfentiQuizStat stat = afentiLoader.loadAfentiQuizStatByUserId(studentId)
                .stream()
                .filter(s -> StringUtils.equals(s.getNewBookId(), bookId))
                .filter(s -> StringUtils.equals(s.getNewUnitId(), unitId))
                .findFirst()
                .orElse(null);

        // 如果既不是最后一题，也不是错题订正的提交答案，不计算分数
        if (stat == null && !Boolean.TRUE.equals(context.getFinished())) {
            context.terminateTask();
            return;
        }

        context.setStat(stat); // 可能是null
    }
}
