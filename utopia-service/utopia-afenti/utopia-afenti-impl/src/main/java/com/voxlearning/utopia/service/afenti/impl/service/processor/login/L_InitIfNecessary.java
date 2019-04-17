package com.voxlearning.utopia.service.afenti.impl.service.processor.login;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanRankResult;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanRewardHistory;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanRankResultPersistence;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanRewardHistoryPersistence;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanUserRankStatPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class L_InitIfNecessary extends SpringContainerSupport implements IAfentiTask<LoginContext> {
    @Inject private AfentiLearningPlanRankResultPersistence rrp;
    @Inject private AfentiLearningPlanRewardHistoryPersistence rhp;
    @Inject private AfentiLearningPlanUserRankStatPersistence rsp;

    @Override
    public void execute(LoginContext context) {
        // 获取标志位数据
        Long sid = context.getStudent().getId();
        AfentiLearningPlanUserRankStat sign = rsp.queryByUserIdAndNewBookId(sid, "init").stream().findFirst().orElse(null);
        if (sign != null) return;

        // 转移数据
        List<AfentiLearningPlanRankResult> rrs = rrp.findByUserId(context.getStudent().getId())
                .stream().filter(e -> StringUtils.isNotBlank(e.getNewBookId()))
                .filter(e -> StringUtils.isNotBlank(e.getNewUnitId()))
                .collect(Collectors.toList());
        Map<String, List<AfentiLearningPlanRewardHistory>> rhs = rhp.findByUserId(context.getStudent().getId())
                .stream()
                .filter(e -> StringUtils.isNotBlank(e.getNewBookId()))
                .filter(e -> StringUtils.isNotBlank(e.getNewUnitId()))
                .filter(e -> Boolean.TRUE.equals(e.getReceived()))
                .collect(Collectors.groupingBy(e -> e.getNewBookId() + "|" + e.getNewUnitId() + "|" + e.getRank()));

        for (AfentiLearningPlanRankResult rr : rrs) {
            AfentiLearningPlanUserRankStat stat = new AfentiLearningPlanUserRankStat();
            stat.setCreateTime(new Date());
            stat.setUpdateTime(new Date());
            stat.setUserId(context.getStudent().getId());
            stat.setNewBookId(rr.getNewBookId());
            stat.setNewUnitId(rr.getNewUnitId());
            stat.setRank(rr.getRank());
            stat.setStar(rr.getMaxStarNum());
            stat.setSilver(0);
            stat.setSuccessiveSilver(0);
            stat.setBonus(0);
            stat.setSubject(rr.getSubject());

            String key = rr.getNewBookId() + "|" + rr.getNewUnitId() + "|" + rr.getRank();
            List<AfentiLearningPlanRewardHistory> histories = rhs.get(key);
            if (CollectionUtils.isNotEmpty(histories)) {
                stat.setSilver(histories.stream().mapToInt(AfentiLearningPlanRewardHistory::getSilver).sum());
                stat.setSuccessiveSilver(histories.stream().mapToInt(AfentiLearningPlanRewardHistory::getSuccessiveSilver).sum());
            }

            try {
                rsp.persist(stat);
            } catch (Exception ignored) {
            }
        }

        // 存入标志数据
        sign = new AfentiLearningPlanUserRankStat();
        sign.setCreateTime(new Date());
        sign.setUpdateTime(new Date());
        sign.setUserId(context.getStudent().getId());
        sign.setNewBookId("init");
        sign.setNewUnitId("");
        sign.setRank(0);
        sign.setStar(0);
        sign.setSilver(0);
        sign.setSuccessiveSilver(0);
        sign.setBonus(0);
        sign.setSubject(context.getSubject());
        try {
            rsp.persist(sign);
        } catch (Exception ignored) {
        }
    }
}
