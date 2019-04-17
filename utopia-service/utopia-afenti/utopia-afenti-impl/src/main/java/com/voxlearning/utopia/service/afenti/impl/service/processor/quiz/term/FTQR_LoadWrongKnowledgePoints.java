package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizReportContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
public class FTQR_LoadWrongKnowledgePoints extends SpringContainerSupport implements IAfentiTask<FetchTermQuizReportContext> {
    @Inject private AfentiLoaderImpl afentiLoader;
    @Inject NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;

    @Override
    public void execute(FetchTermQuizReportContext context) {
        // 如果是满分，不需要查询出错知识点
        if (context.getStat().getScore() == 100) {
            context.getResult().put("knowledges", new ArrayList<>());
            return;
        }

        Long studentId = context.getStudent().getId();
        String bookId = context.getBookId();
        String unitId = context.getUnitId();

        // 按知识点分组
        Map<String, List<AfentiQuizResult>> kp_results_map = afentiLoader
                .loadAfentiQuizResultByUserIdAndNewBookId(studentId, bookId)
                .stream()
                .filter(r -> StringUtils.equals(r.getNewUnitId(), unitId))
                .filter(r -> r.getSubject() == context.getSubject())
                .collect(Collectors.groupingBy(AfentiQuizResult::getKnowledgePoint));

        // 获取还没有全对的知识点
        Map<String, Integer> kp_accuracy_map = new HashMap<>();
        for (Map.Entry<String, List<AfentiQuizResult>> entry : kp_results_map.entrySet()) {
            // 计算正确率
            int accuracy = new BigDecimal(entry.getValue().stream().filter(r -> r.getRightNum() >= 1).count())
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(entry.getValue().size()), 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
            if (accuracy >= 100) continue;
            kp_accuracy_map.put(entry.getKey(), accuracy);
        }

        // 获取知识点名称
        Map<String, NewKnowledgePoint> kps = newKnowledgePointLoaderClient.loadKnowledgePoints(kp_accuracy_map.keySet());

        List<Map<String, Object>> knowledges = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : kp_accuracy_map.entrySet()) {
            Map<String, Object> knowledge = new HashMap<>();
            knowledge.put("id", entry.getKey());
            NewKnowledgePoint nkp = kps.getOrDefault(entry.getKey(), null);
            knowledge.put("name", nkp == null ? "" : nkp.getName());
            knowledge.put("accuracy", entry.getValue());
            knowledges.add(knowledge);
        }

        Collections.sort(knowledges, (o1, o2) -> {
            int accuracy1 = SafeConverter.toInt(o1.get("accuracy"));
            int accuracy2 = SafeConverter.toInt(o2.get("accuracy"));
            return Integer.compare(accuracy1, accuracy2);
        });

        context.getResult().put("knowledges", knowledges);
    }
}
