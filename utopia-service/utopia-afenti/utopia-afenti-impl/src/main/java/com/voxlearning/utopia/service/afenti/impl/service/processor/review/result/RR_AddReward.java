package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanUserRankStatPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Date;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class RR_AddReward extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {

    @Inject
    private AfentiLearningPlanUserRankStatPersistence rsp;

    @ImportService(interfaceClass = UserIntegralService.class)
    protected UserIntegralService userIntegralService;

    @Override
    public void execute(ReviewResultContext context) {
        AfentiLearningPlanUserRankStat stat = context.getStat();

        int silver = context.getSilver();
        int bonus = context.getBonus();

        String bookId = AfentiUtils.getBookId(context.getBookId(), AfentiLearningType.review);
        if (stat == null) {
            stat = new AfentiLearningPlanUserRankStat();
            stat.setCreateTime(new Date());
            stat.setUpdateTime(new Date());
            stat.setUserId(context.getStudent().getId());
            stat.setNewBookId(bookId);
            stat.setNewUnitId(context.getUnitId());
            stat.setRank(1);
            stat.setStar(context.getStar());
            stat.setSilver(silver);
            stat.setSuccessiveSilver(0);
            stat.setBonus(bonus);
            stat.setSubject(context.getSubject());
            try {
                rsp.persist(stat);
            } catch (Exception ignored) {
            }
        } else {
            int delta_star = context.getStar() - stat.getStar();
            rsp.updateStat(stat.getId(), context.getStudent().getId(), bookId, context.getSubject(),
                    delta_star, silver, 0, bonus);
        }

        int integral = silver + bonus;
        if (integral > 0) {
            IntegralType type;
            String text;
            switch (context.getSubject()) {
                case ENGLISH: {
                    text = "阿分题英语";
                    type = IntegralType.阿分提获星;
                    break;
                }
                case MATH: {
                    text = "阿分题数学";
                    type = IntegralType.AFENTI_MATH_LEARNING_PLAN;
                    break;
                }
                case CHINESE: {
                    text = "阿分题语文";
                    type = IntegralType.AFENTI_CHINESE_LEARNING_PLAN;
                    break;
                }
                default:
                    return;
            }
            IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(context.getStudent().getId(), type)
                    .withIntegral(integral)
                    .withComment(text + "获得星星奖励")
                    .build();
            userIntegralService.changeIntegral(context.getStudent(), history);
        }

        context.getResult().put("silver", silver);
        context.getResult().put("successiveSilver", 0);
        context.getResult().put("bonus", bonus);
        context.getResult().put("rewardInfos", Collections.emptyList());
    }
}
