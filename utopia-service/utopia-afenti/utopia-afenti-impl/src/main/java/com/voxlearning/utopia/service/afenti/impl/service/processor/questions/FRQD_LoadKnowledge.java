package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.entity.content.KnowledgePoint;
import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.consumer.KnowledgePointLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/19
 */
@Named
public class FRQD_LoadKnowledge extends SpringContainerSupport implements IAfentiTask<FetchRankQuestionsContext> {
    @Inject NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject KnowledgePointLoaderClient knowledgePointLoaderClient;

    @Override
    public void execute(FetchRankQuestionsContext context) {
        Set<String> kps = context.getHistories().stream()
                .filter(h -> StringUtils.isNotBlank(h.getKnowledgePoint()))
                .map(AfentiLearningPlanPushExamHistory::getKnowledgePoint)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(kps)) {
            return;
        }

        // 区分新老更老知识点，更老的知识点就不要了~
        Set<String> kps_new = new HashSet<>();
        Set<Long> kps_old = new HashSet<>();

        for (String kp : kps) {
            if (StringUtils.startsWith(kp, "KP")) {
                kps_new.add(kp);
            } else if (kp.matches("^\\d+$")) {
                kps_old.add(SafeConverter.toLong(kp));
            }
        }

        // 名字为空的知识点不要了
        if (CollectionUtils.isNotEmpty(kps_new)) {
            Map<String, NewKnowledgePoint> nkps = newKnowledgePointLoaderClient.loadKnowledgePoints(kps_new);
            context.getKnowledges().addAll(nkps.values().stream().filter(p -> StringUtils.isNotBlank(p.getName()))
                    .map(NewKnowledgePoint::getName).collect(Collectors.toSet()));
        }
        if (CollectionUtils.isNotEmpty(kps_old)) {
            Map<Long, KnowledgePoint> okps = knowledgePointLoaderClient.loadKnowledgePoints(kps_old);
            context.getKnowledges().addAll(okps.values().stream().filter(p -> StringUtils.isNotBlank(p.getPointName()))
                    .map(KnowledgePoint::getPointName).collect(Collectors.toSet()));
        }
    }
}
