package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizInfoContext;
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
public class FQI_QuizAccomplishmentCheck extends SpringContainerSupport implements IAfentiTask<FetchQuizInfoContext> {
    @Inject AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchQuizInfoContext context) {
        String bookId = context.getBook().book.getId();
        String unitId = context.getUnitId();
        Long studentId = context.getStudent().getId();

        // 获取单元测试完成信息记录，如果该记录存在，表示单元测试已经完成了，但并不表示全部作对了
        AfentiQuizStat stat = afentiLoader.loadAfentiQuizStatByUserId(studentId)
                .stream()
                .filter(s -> StringUtils.equals(s.getNewBookId(), bookId))
                .filter(s -> StringUtils.equals(s.getNewUnitId(), unitId))
                .findFirst()
                .orElse(null);

        context.getResult().put("accomplished", stat != null);
        context.getResult().put("score", stat != null ? stat.getScore() : 0);
    }
}
