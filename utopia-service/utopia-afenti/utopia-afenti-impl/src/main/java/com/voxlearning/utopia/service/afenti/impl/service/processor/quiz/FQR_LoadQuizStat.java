package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizReportContext;
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
public class FQR_LoadQuizStat extends SpringContainerSupport implements IAfentiTask<FetchQuizReportContext> {
    @Inject AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchQuizReportContext context) {
        if (context.isSkipStat()) return; // 如果从提交最后一题答案后进入报告，此处不用计算

        // 获取测试完成信息记录，如果该记录存在，表示测试已经完成了，但并不表示全部作对了
        AfentiQuizStat stat = afentiLoader.loadAfentiQuizStatByUserId(context.getStudent().getId())
                .stream()
                .filter(s -> StringUtils.equals(s.getNewBookId(), context.getBookId()))
                .filter(s -> StringUtils.equals(s.getNewUnitId(), context.getUnitId()))
                .findFirst()
                .orElse(null);

        if (stat == null) {
            logger.error("FQR_LoadQuizStat Quiz not finished, no report. user {}, subject {}, book {}, unit {}",
                    context.getStudent().getId(), context.getSubject(), context.getBookId(), context.getUnitId());
            context.errorResponse();
            return;
        }

        context.setStat(stat);
        context.getResult().put("score", stat.getScore());
        context.getResult().put("integral", stat.getSilver());
    }
}
