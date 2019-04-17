package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizInfoContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/10/18
 */
@Named
public class FTQI_LoadQuizStat extends SpringContainerSupport implements IAfentiTask<FetchTermQuizInfoContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchTermQuizInfoContext context) {
        if (context.getStudent().getClazz() == null) {
            logger.error("FTQI_LoadGrade student {} do not chose a clazz", context.getStudent().getId());
            context.errorResponse();
            return;
        }

        // 获取期末测验结果数据，最多有三条，每个学科一条
        List<AfentiQuizStat> stats = afentiLoader.loadAfentiQuizStatByUserId(context.getStudent().getId())
                .stream()
                .filter(s -> StringUtils.equals(s.getNewUnitId(), UtopiaAfentiConstants.CURRENT_QUIZ))
                .collect(Collectors.toList());

        context.getStats().addAll(stats);
    }
}
