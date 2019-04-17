package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.MentalTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
public class AppMentalObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.MENTAL;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {


        //*********** begin 初始化数据准备 *********** //
        /**
         * 1:type =》 类型
         * 2:result =》 返回数据
         * 3:newHomeworkResults =》 完成该类型的中间表数据
         * 4:newHomeworkProcessResultMap
         */
        ObjectiveConfigType type = typePartContext.getType();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());
        NewHomework newHomework = typePartContext.getNewHomework();
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = typePartContext.getNewHomeworkProcessResultMap();
        List<NewHomeworkQuestion> mentalQuestion = newHomework.findNewHomeworkQuestions(type);
        Map<String, String> qidToKidMap = mentalQuestion
                .stream()
                .filter(o -> StringUtils.isNotEmpty(o.getKnowledgePointId()))
                .collect(Collectors.toMap(NewHomeworkQuestion::getQuestionId,
                        NewHomeworkQuestion::getKnowledgePointId
                ));

        //*********** begin 初始化数据准备 *********** //


        MentalTypePart mentalTypePart = new MentalTypePart();
        mentalTypePart.setType(type);
        mentalTypePart.setTypeName(type.getValue());
        mentalTypePart.setShowScore(true);
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            mentalTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, mentalTypePart);
            return;
        }


        //这部分数据准备放在这:避免没必要查询，前面有return
        Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePointsIncludeDeleted(qidToKidMap.values());


        // *********** begin 初始化MentalTypePart.Point 是每个知识点的的结构****//
        Map<String, MentalTypePart.Point> pointMap = new LinkedHashMap<>();
        for (String kid : qidToKidMap.values()) {
            if (!newKnowledgePointMap.containsKey(kid)) continue;
            if (pointMap.containsKey(kid)) continue;
            MentalTypePart.Point point = new MentalTypePart.Point();
            point.setKid(kid);
            pointMap.put(kid, point);
            NewKnowledgePoint newKnowledgePoint = newKnowledgePointMap.get(kid);
            point.setTabName(newKnowledgePoint.getName());
        }
        // *********** end 初始化MentalTypePart.Point 是每个知识点的的结构****//


        int totalScore = 0;
        long totalDuration = 0;

        //******** begin newHomeworkResults 数据处理 *******//
        //pointMap key 对于知识点ID
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            totalScore += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
            totalDuration += SafeConverter.toLong(newHomeworkResultAnswer.processDuration());

        }

        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (qidToKidMap.containsKey(p.getQuestionId()) && pointMap.containsKey(qidToKidMap.get(p.getQuestionId()))) {
                MentalTypePart.Point point = pointMap.get(qidToKidMap.get(p.getQuestionId()));
                point.setNum(1 + point.getNum());
                if (SafeConverter.toBoolean(p.getGrasp())) {
                    point.setRightNum(1 + point.getRightNum());
                }
            }
        }


        //******** end newHomeworkResults 数据处理 *******//


        //**********begin 知识点的后处理(分数和时间) ***************//

        pointMap.values().stream()
                .filter(o -> o.getNum() > 0)
                .forEach(o -> {
                    int t = new BigDecimal(o.getRightNum() * 100).divide(new BigDecimal(o.getNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    o.setRightRate(t);
                    o.setTabValue(t + "%");
                });

        //**********end 知识点的后处理(分数和时间)***************//
        List<MentalTypePart.Point> points = new ArrayList<>(pointMap.values());
        mentalTypePart.setTabs(points);
        int averScore = new BigDecimal(totalScore).divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        long averDuration = new BigDecimal(totalDuration).divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP).longValue();
        mentalTypePart.setAverScore(averScore);
        mentalTypePart.setHasFinishUser(true);
        mentalTypePart.setAverDuration(averDuration);
        mentalTypePart.setSubContent("班平均分" + averScore + " 平均用时" + averDuration + "min");
        ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
        String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/questionsdetail",
                MapUtils.m(
                        "homeworkId", newHomework.getId(),
                        "type", type,
                        "subject", newHomework.getSubject(),
                        "param", JsonUtils.toJson(param)));
        mentalTypePart.setUrl(url);
        mentalTypePart.setShowUrl(true);
        result.put(type, mentalTypePart);

    }
}
