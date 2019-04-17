package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanUserRankStatPersistence;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiOperationalInfoService;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.integral.api.constants.CreditType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.CreditHistory;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Ruib
 * @since 2016/7/22
 */
@Named
public class CR_AddReward extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {
    @Inject
    private AfentiLearningPlanUserRankStatPersistence rsp;
    @Inject
    private AfentiOperationalInfoService afentiOperationalInfoService;

    @ImportService(interfaceClass = UserIntegralService.class)
    protected UserIntegralService userIntegralService;

    @Override
    public void execute(CastleResultContext context) {
        AfentiLearningPlanUserRankStat stat = context.getStat();

        int silver = context.isAuthorized() ? context.getSilver() : new BigDecimal(context.getSilver())
                .divide(new BigDecimal(10), 0, BigDecimal.ROUND_HALF_UP).intValue();
        int bonus = context.isAuthorized() ? context.getBonus() : new BigDecimal(context.getBonus())
                .divide(new BigDecimal(10), 0, BigDecimal.ROUND_HALF_UP).intValue();
        // 学分奖励(如果不是会员，奖励为1/10，即1学分)
        int creditCount = context.isAuthorized() ? context.getCreditCount() : new BigDecimal(context.getCreditCount())
                .divide(new BigDecimal(10), 0, BigDecimal.ROUND_HALF_UP).intValue();

        // 翻倍
        silver = context.getAfentiLearningType() == AfentiLearningType.preparation ? silver : new BigDecimal(silver).multiply(context.getMultiple()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        bonus = context.getAfentiLearningType() == AfentiLearningType.preparation ? bonus : new BigDecimal(bonus).multiply(context.getMultiple()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

        String bookId = AfentiUtils.getBookId(context.getBookId(), context.getAfentiLearningType());
        if (context.getIsNewRankBook()) {
            bookId = context.getNewRankBookId();
        }
        if (stat == null) {
            stat = new AfentiLearningPlanUserRankStat();
            stat.setCreateTime(new Date());
            stat.setUpdateTime(new Date());
            stat.setUserId(context.getStudent().getId());
            stat.setNewBookId(bookId);
            stat.setNewUnitId(context.getUnitId());
            stat.setRank(context.getRank());
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

        // 发放自学积分
        if (creditCount > 0) {
            CreditHistory creditHistory = new CreditHistory();
            creditHistory.setUserId(context.getStudent().getId());
            creditHistory.setAmount(creditCount);
            creditHistory.setComment("阿分题:学习城堡发奖" + "(" + context.getSubject().getValue() + ")");
            creditHistory.setType(CreditType.afenti_receive.getType());
            MapMessage creditMapMessage = userIntegralService.changeCredit(creditHistory);

            if (!creditMapMessage.isSuccess())
                logger.error("afenti receive credit error::sid:{}creditCount:{}", context.getStudent().getId(), creditCount);
        }

        try {
            if (context.isBoughtAfenti()) {
                afentiOperationalInfoService.addUserRewardInfo(context.getStudent());
            }
        } catch (Exception e) {
            logger.error("addUserRewardInfo error");
        }

        context.getResult().put("silver", silver);
        context.getResult().put("successiveSilver", 0);
        context.getResult().put("bonus", bonus);
        context.getResult().put("creditCount", creditCount);
    }
}
