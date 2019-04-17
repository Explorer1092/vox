package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.entity.content.KnowledgePoint;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.consumer.KnowledgePointLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/22
 */
@Named
public class CR_CalculateKnowledge extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {
    @Inject
    NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject
    KnowledgePointLoaderClient knowledgePointLoaderClient;

    @Override
    public void execute(CastleResultContext context) {
        // 将本关卡的题目按照知识点分组
        Map<String, List<AfentiLearningPlanPushExamHistory>> kp_questions_map = context.getHistories()
                .stream()
                .filter(h -> StringUtils.isNotBlank(h.getKnowledgePoint()))
                .collect(Collectors.groupingBy(AfentiLearningPlanPushExamHistory::getKnowledgePoint));

        // 区分新老更老知识点
        Set<String> kps_new = new HashSet<>();
        Set<Long> kps_old = new HashSet<>();

        for (String kp : kp_questions_map.keySet()) {
            if (StringUtils.startsWith(kp, "KP")) {
                kps_new.add(kp);
            } else if (kp.matches("^\\d+$")) {
                kps_old.add(SafeConverter.toLong(kp));
            }
        }

        Map<String, NewKnowledgePoint> nkps = new HashMap<>();
        Map<Long, KnowledgePoint> okps = new HashMap<>();
        if (CollectionUtils.isNotEmpty(kps_new)) nkps = newKnowledgePointLoaderClient.loadKnowledgePoints(kps_new);
        if (CollectionUtils.isNotEmpty(kps_old)) okps = knowledgePointLoaderClient.loadKnowledgePoints(kps_old);

        List<Map<String, Object>> knowledges = new ArrayList<>();
        Integer wkpc = 0;
        for (String kp : kp_questions_map.keySet()) {
            Map<String, Object> knowledge = new HashMap<>();
            String knowledgeName;
            if (StringUtils.startsWith(kp, "KP")) {
                NewKnowledgePoint nkp = nkps.get(kp);
                knowledgeName = nkp == null ? "" : nkp.getName();
            } else if (kp.matches("^\\d+$")) {
                KnowledgePoint okp = okps.get(SafeConverter.toLong(kp));
                knowledgeName = okp == null ? "" : okp.getPointName();
            } else {
                knowledgeName = "";
            }
            // 名字为空的知识点不要了
            if (StringUtils.isBlank(knowledgeName)) continue;

            List<AfentiLearningPlanPushExamHistory> histories = kp_questions_map.get(kp);
            knowledge.put("rc", histories.stream().filter(h -> h.getRightNum() > 0).count());
            knowledge.put("tc", histories.size());
            knowledge.put("knowledgeName", knowledgeName);
            knowledges.add(knowledge);

            // 统计错误知识点数目
            if (histories.stream().filter(h -> h.getRightNum() == 0).count() > 0) wkpc++;
        }

        context.setWkpc(wkpc);
        context.getResult().put("knowledges", knowledges);
    }
}
