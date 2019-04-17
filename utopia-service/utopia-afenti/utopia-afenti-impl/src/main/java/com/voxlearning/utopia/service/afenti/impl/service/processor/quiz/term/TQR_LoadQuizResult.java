package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
public class TQR_LoadQuizResult extends SpringContainerSupport implements IAfentiTask<TermQuizResultContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(TermQuizResultContext context) {
        Long studentId = context.getStudent().getId();

        Map<Long, AfentiQuizResult> qrm = afentiLoader.loadAfentiQuizResultByUserIdAndNewBookId(studentId, context.getBookId())
                .stream()
                .filter(r -> StringUtils.equals(r.getNewUnitId(), context.getUnitId()))
                .filter(r -> r.getSubject() == context.getSubject())
                .collect(Collectors.toMap(AfentiQuizResult::getId, Function.identity()));

        context.getQrm().putAll(qrm);

        context.setQuestionId(StringUtils.substringBefore(context.getQuestionId(), "-"));
    }
}
