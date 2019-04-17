package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiQuizResultDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class QR_UpdateQuizResult extends SpringContainerSupport implements IAfentiTask<QuizResultContext> {
    @Inject private AfentiQuizResultDao afentiQuizResultDao;

    @Override
    public void execute(QuizResultContext context) {
        // 获取当前题目的AfentiQuizResult
        AfentiQuizResult qr = context.getQrs().stream()
                .filter(r -> StringUtils.equals(r.getExamId(), context.getQuestionId()))
                .findFirst().orElse(null);

        if (qr == null) {
            logger.error("QR_UpdateQuizResult Cannot update AfentiQuizResult. context is {}", JsonUtils.toJson(context));
            context.errorResponse();
            return;
        }

        if (Boolean.TRUE.equals(context.getMaster())) {
            qr.setRightNum(1);
        } else {
            qr.increaseErrorNum();
        }

        if (afentiQuizResultDao.updateRightAndErrorNums(qr)) {
            // 写回到qrs中
            Map<Long, AfentiQuizResult> map = context.getQrs().stream()
                    .collect(Collectors.toMap(AfentiQuizResult::getId, Function.identity()));
            map.put(qr.getId(), qr);
            context.getQrs().clear();
            context.getQrs().addAll(map.values());
        }
    }
}
