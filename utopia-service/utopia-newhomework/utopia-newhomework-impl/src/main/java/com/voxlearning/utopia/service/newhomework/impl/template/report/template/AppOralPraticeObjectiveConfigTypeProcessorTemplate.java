package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.OralPracticeTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
public class AppOralPraticeObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_PRACTICE;
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
        NewHomework newHomework = typePartContext.getNewHomework();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = typePartContext.getNewHomeworkProcessResultMap();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());

        //*********** end 初始化数据准备 *********** //


        //*********** begin OralPracticeTypePart 是返回数据结构 *********** //
        //1>设置类型数据
        //2>判断是否有学生完成
        /**
         * tapType 壳渲染方式
         * 1: 基础练习
         * 2：同步练习
         * 3：口算
         * 4：绘本
         * 5：朗读背诵
         */
        OralPracticeTypePart oralPracticeTypePart = new OralPracticeTypePart();
        oralPracticeTypePart.setType(type);
        oralPracticeTypePart.setTypeName(type.getValue());
        oralPracticeTypePart.setShowScore(true);
         if (CollectionUtils.isEmpty(newHomeworkResults)) {
             oralPracticeTypePart.setSubContent("班平均分-- 平均用时--");
             result.put(type, oralPracticeTypePart);
            return;
        }
        //*********** end OralPracticeTypePart 是返回数据结构 *********** //

        // *********** begin 初始化OralPracticeTypePart.Question 是每个题的的结构****//
        List<NewHomeworkQuestion> newHomeworkQuestions = typePartContext.getNewHomework().findNewHomeworkQuestions(type);
        Map<String, OralPracticeTypePart.Question> map = newHomeworkQuestions.stream()
                .collect(Collectors.toMap(NewHomeworkQuestion::getQuestionId, o -> {
                    OralPracticeTypePart.Question question = new OralPracticeTypePart.Question();
                    question.setQid(o.getQuestionId());
                    return question;
                }));
        // *********** end 初始化OralPracticeTypePart.Question 是每个题的的结构****//

        int totalScore = 0;
        long totalDuration = 0;


        //******** begin newHomeworkResults 数据处理 *******//
        //map key 对于QID
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            totalScore += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
            totalDuration += SafeConverter.toLong(newHomeworkResultAnswer.processDuration());
        }

        for (NewHomeworkProcessResult newHomeworkProcessResult : newHomeworkProcessResultMap.values()){
            if (map.containsKey(newHomeworkProcessResult.getQuestionId())){
                OralPracticeTypePart.Question question = map.get(newHomeworkProcessResult.getQuestionId());
                question.setNum(1 + question.getNum());
                double s = 0;
                if (SafeConverter.toDouble(newHomeworkProcessResult.getStandardScore()) > 0) {
                    s = SafeConverter.toDouble(newHomeworkProcessResult.getScore()) * 100 / SafeConverter.toDouble(newHomeworkProcessResult.getStandardScore());
                }
                question.setTotalScore(question.getTotalScore() + s);
            }
        }



        //******** end newHomeworkResults 数据处理 *******//

        //***** begin OralPracticeTypePart.Question 后处理 ******//
        //每题分数
        map.values().stream()
                .filter(o -> o.getNum() > 0)
                .filter(o -> o.getTotalScore() > 0)
                .forEach(o -> {
                    int averScore1 = new BigDecimal(o.getTotalScore()).divide(new BigDecimal(o.getNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    o.setAverScore(averScore1);
                    o.setRate(averScore1); //前端只取这个字段展示
                });

        List<OralPracticeTypePart.Question> questions = newHomeworkQuestions.stream()
                .filter(o -> map.containsKey(o.getQuestionId()))
                .map(o -> map.get(o.getQuestionId()))
                .collect(Collectors.toList());
        //每题链接和序号
        for (int i = 0; i < questions.size(); i++) {
            OralPracticeTypePart.Question question = questions.get(i);
            question.setIndex(i);
            String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/singlequestiondetail",
                    MapUtils.m(
                            "homeworkId", newHomework.getId(),
                            "type", type,
                            "subject", newHomework.getSubject(),
                            "qid", question.getQid(),
                            "index", question.getIndex()));
            question.setUrl(url);
        }
        //***** end OralPracticeTypePart.Question 后处理 ******//

        int averScore = new BigDecimal(totalScore).divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        long averDuration = new BigDecimal(totalDuration).divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP).longValue();
        oralPracticeTypePart.setQuestion(questions);
        oralPracticeTypePart.setAverScore(averScore);
        oralPracticeTypePart.setAverDuration(averDuration);
        oralPracticeTypePart.setSubContent("班平均分" + averScore + " 平均用时" + averDuration + "min");
        oralPracticeTypePart.setHasFinishUser(true);
        ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
        String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/questionsdetail",
                MapUtils.m(
                        "homeworkId", newHomework.getId(),
                        "type", type,
                        "subject", newHomework.getSubject(),
                        "param", JsonUtils.toJson(param)));
        oralPracticeTypePart.setUrl(url);
        oralPracticeTypePart.setShowUrl(true);
        result.put(type, oralPracticeTypePart);
    }
}
