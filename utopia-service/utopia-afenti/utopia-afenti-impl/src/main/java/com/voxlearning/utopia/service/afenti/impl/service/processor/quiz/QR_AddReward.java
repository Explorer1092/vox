package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiQuizStatDao;
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
 * @since 2016/10/17
 */
@Named
public class QR_AddReward extends SpringContainerSupport implements IAfentiTask<QuizResultContext> {
    @Inject private AfentiQuizStatDao afentiQuizStatDao;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;

    @Override
    public void execute(QuizResultContext context) {
        // 获取stat，如果没有表示是第一次完成测验，如果存在表示错题订正
        AfentiQuizStat stat = context.getStat();
        int integral;

        if (stat == null) {
            integral = context.getIntegral();
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
        } else {
            integral = context.getIntegral() - stat.getSilver();
            stat.setSilver(context.getIntegral());
            stat.setScore(context.getScore());
            afentiQuizStatDao.updateScoreAndSilver(stat);
        }
        context.setStat(stat);

        if (integral > 0) {
            IntegralType type;
            String text;
            switch (context.getSubject()) {
                case ENGLISH: {
                    text = "阿分题英语";
                    type = IntegralType.AFENTI_ENGLISH_QUIZ_REWARD;
                    break;
                }
                case MATH: {
                    text = "阿分题数学";
                    type = IntegralType.AFENTI_MATH_QUIZ_REWARD;
                    break;
                }
                default:
                    return;
            }
            IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(context.getStudent().getId(), type)
                    .withIntegral(integral)
                    .withComment("完成" + text + "测验获得奖励")
                    .build();
            userIntegralService.changeIntegral(context.getStudent(), history);
        }
    }
}
