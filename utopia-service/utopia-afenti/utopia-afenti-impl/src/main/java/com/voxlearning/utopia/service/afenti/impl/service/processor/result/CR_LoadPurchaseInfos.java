package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanUserRankStatPersistence;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiOperationalInfoService;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.consumer.KnowledgePointLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * 加载同伴同学的获得学豆信息
 *
 * @author peng.zhang.a
 * @since 16-12-5
 */
@Named
public class CR_LoadPurchaseInfos extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {
    @Inject NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject KnowledgePointLoaderClient knowledgePointLoaderClient;
    @Inject AfentiOperationalInfoService afentiOperationalInfoService;
    @Inject AfentiLearningPlanUserRankStatPersistence rsp;

    @Override
    public void execute(CastleResultContext context) {
        try {
            if (!context.isAuthorized()) {
                //未开通用户加载同学
                List<Map<String, Object>> rewardInfos = afentiOperationalInfoService.loadUserRewardInfos(context.getStudent());
                context.getResult().put("rewardInfos", rewardInfos);
            }
        } catch (Exception e) {
            logger.error("addUserRewardInfo error", e);
        }
    }
}
