package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiQuizResultDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
public class TQR_UpdateQuizResult extends SpringContainerSupport implements IAfentiTask<TermQuizResultContext> {
    @Inject private AfentiQuizResultDao afentiQuizResultDao;

    @Override
    public void execute(TermQuizResultContext context) {
        // 获取当前题目的AfentiQuizResult
        AfentiQuizResult qr = context.getQrm().values().stream()
                .filter(r -> StringUtils.equals(r.getExamId(), context.getQuestionId()))
                .findFirst().orElse(null);

        if (qr == null) {
            logger.error("TQR_UpdateQuizResult Cannot update AfentiQuizResult. context is {}", JsonUtils.toJson(context));
            context.errorResponse();
            return;
        }

        if (Boolean.TRUE.equals(context.getMaster())) {
            qr.setRightNum(1);
        } else {
            qr.increaseErrorNum();
        }

        if (afentiQuizResultDao.updateRightAndErrorNums(qr)) context.getQrm().put(qr.getId(), qr); // 写回到qrm中
    }
}
