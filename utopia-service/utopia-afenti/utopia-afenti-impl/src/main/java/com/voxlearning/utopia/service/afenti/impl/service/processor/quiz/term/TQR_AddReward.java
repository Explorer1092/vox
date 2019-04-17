package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiQuizStatDao;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
public class TQR_AddReward extends SpringContainerSupport implements IAfentiTask<TermQuizResultContext> {
    @Inject AfentiLoaderImpl afentiLoader;
    @Inject private AfentiQuizStatDao afentiQuizStatDao;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;

    @Override
    public void execute(TermQuizResultContext context) {
        Long studentId = context.getStudent().getId();

        // 获取单元测试完成信息记录
        AfentiQuizStat stat = afentiLoader.loadAfentiQuizStatByUserId(studentId)
                .stream()
                .filter(s -> StringUtils.equals(s.getNewBookId(), context.getBookId()))
                .filter(s -> StringUtils.equals(s.getNewUnitId(), context.getUnitId()))
                .filter(s -> s.getSubject() == context.getSubject())
                .findFirst()
                .orElse(null);

        int integral;

        if (stat == null) {
            stat = new AfentiQuizStat();
            stat.setCreateTime(new Date());
            stat.setUpdateTime(new Date());
            stat.setUserId(context.getStudent().getId());
            stat.setNewBookId(context.getBookId());
            stat.setNewUnitId(context.getUnitId());
            stat.setSubject(context.getSubject());
            stat.setSilver(context.getIntegral());
            stat.setScore(context.getScore());
            try {
                afentiQuizStatDao.insert(stat);
            } catch (Exception ignored) {
            }

            integral = context.getIntegral();
        } else {
            stat.setSilver(context.getIntegral());
            stat.setScore(context.getScore());
            afentiQuizStatDao.updateScoreAndSilver(stat);

            integral = context.getIntegral() - stat.getSilver();
        }
        context.setStat(stat);

        if (integral > 0) {
            IntegralType type;
            switch (context.getSubject()) {
                case ENGLISH: {
                    type = IntegralType.阿分提获星;
                    break;
                }
                case MATH: {
                    type = IntegralType.AFENTI_MATH_LEARNING_PLAN;
                    break;
                }
                case CHINESE: {
                    type = IntegralType.AFENTI_CHINESE_LEARNING_PLAN;
                    break;
                }
                default:
                    return;
            }
            IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(context.getStudent().getId(), type)
                    .withIntegral(integral)
                    .withComment("完成阿分题" + context.getSubject().getValue() + "期末测评获得奖励")
                    .build();
            userIntegralService.changeIntegral(context.getStudent(), history);
        }
    }
}
