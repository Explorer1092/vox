package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizReportContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
public class FTQR_LoadQuizStat extends SpringContainerSupport implements IAfentiTask<FetchTermQuizReportContext> {
    @Inject AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchTermQuizReportContext context) {
        context.setBookId(UtopiaAfentiConstants.CURRENT_QUIZ);
        context.setUnitId(UtopiaAfentiConstants.CURRENT_QUIZ);

        // 获取测试完成信息记录，如果该记录存在，表示测试已经完成了，但并不表示全部作对了
        AfentiQuizStat stat = afentiLoader.loadAfentiQuizStatByUserId(context.getStudent().getId())
                .stream()
                .filter(s -> StringUtils.equals(s.getNewBookId(), context.getBookId()))
                .filter(s -> StringUtils.equals(s.getNewUnitId(), context.getUnitId()))
                .filter(s -> s.getSubject() == context.getSubject())
                .findFirst()
                .orElse(null);

        if (stat == null) {
            logger.error("FTQR_LoadQuizStat Term quiz not finished, no report. user {}, subject {}",
                    context.getStudent().getId(), context.getSubject());
            context.errorResponse();
            return;
        }

        context.setStat(stat);
        context.getResult().put("score", stat.getScore());
        context.getResult().put("integral", stat.getSilver());
    }
}
