package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class QR_LoadQuizResult extends SpringContainerSupport implements IAfentiTask<QuizResultContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(QuizResultContext context) {
        Long studentId = context.getStudent().getId();

        List<AfentiQuizResult> qrs = afentiLoader.loadAfentiQuizResultByUserIdAndNewBookId(studentId, context.getBookId())
                .stream()
                .filter(r -> StringUtils.equals(r.getNewUnitId(), context.getUnitId()))
                .collect(Collectors.toList());

        context.getQrs().addAll(qrs);
    }
}
