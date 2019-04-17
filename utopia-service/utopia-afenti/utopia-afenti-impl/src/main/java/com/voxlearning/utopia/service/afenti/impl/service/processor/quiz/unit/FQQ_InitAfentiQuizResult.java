package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiQuizResultDao;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class FQQ_InitAfentiQuizResult extends SpringContainerSupport implements IAfentiTask<FetchQuizQuestionContext> {
    @Inject private AfentiLoaderImpl afentiLoader;
    @Inject private AfentiQuizResultDao afentiQuizResultDao;

    @Override
    public void execute(FetchQuizQuestionContext context) {
        Long studentId = context.getStudent().getId();
        String bookId = context.getBook().book.getId();
        String unitId = context.getUnitId();

        List<AfentiQuizResult> results = afentiLoader.loadAfentiQuizResultByUserIdAndNewBookId(studentId, bookId)
                .stream()
                .filter(r -> StringUtils.equals(r.getNewUnitId(), unitId))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(results)) {
            results = new ArrayList<>();
            Date current = new Date();
            for (Map.Entry<String, String> entry : context.getQid_kpid_map().entrySet()) {
                AfentiQuizResult result = new AfentiQuizResult();
                result.setCreateTime(current);
                result.setUpdateTime(current);
                result.setUserId(studentId);
                result.setNewBookId(bookId);
                result.setNewUnitId(unitId);
                result.setKnowledgePoint(entry.getValue());
                result.setExamId(entry.getKey());
                result.setRightNum(0);
                result.setErrorNum(0);
                result.setSubject(context.getSubject());
                results.add(result);
            }
            afentiQuizResultDao.inserts(results);
        }

        context.getResults().addAll(results);
    }
}
