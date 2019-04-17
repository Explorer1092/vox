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
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.ExamTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
public class AppExamObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.EXAM;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {

        //*********** begin 初始化数据准备 *********** //
        /**
         * 1:type =》 类型
         * 2:result =》 返回数据
         * 3:newHomeworkResults =》 完成该类型的中间表数据
         */
        ObjectiveConfigType type = typePartContext.getType();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        NewHomework newHomework = typePartContext.getNewHomework();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());
        //*********** end 初始化数据准备 *********** //


        //*********** begin ExamTypePart 是返回数据结构 *********** //
        //1>设置类型数据
        //2>判断是否有学生完成
        ExamTypePart examTypePart = new ExamTypePart();
        examTypePart.setType(type);
        examTypePart.setTypeName(type.getValue());
        examTypePart.setTapType(2);
        examTypePart.setShowScore(true);
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            examTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, examTypePart);
            return;
        }
        //*********** end ExamTypePart 是返回数据结构 *********** //

        List<NewHomeworkQuestion> newHomeworkQuestions = typePartContext.getNewHomework().findNewHomeworkQuestions(type);
        Map<String, ExamTypePart.Question> map = newHomeworkQuestions.stream()
                .collect(Collectors.toMap(NewHomeworkQuestion::getQuestionId, o -> {
                    ExamTypePart.Question question = new ExamTypePart.Question();
                    question.setQid(o.getQuestionId());
                    return question;
                }));
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = typePartContext.getNewHomeworkProcessResultMap();
        int totalScore = 0;
        long totalDuration = 0;
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            totalScore += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
            totalDuration += SafeConverter.toLong(newHomeworkResultAnswer.processDuration());
        }

        for (NewHomeworkProcessResult newHomeworkProcessResult : newHomeworkProcessResultMap.values()){
            if (map.containsKey(newHomeworkProcessResult.getQuestionId())){
                ExamTypePart.Question question = map.get(newHomeworkProcessResult.getQuestionId());
                question.setNum(1 + question.getNum());
                if (SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                    question.setRightNum(1 + question.getRightNum());
                }
            }
        }

        //******* begin 每一题的正确率*************//
        map.values()
                .stream()
                .filter(o -> o.getNum() > 0)
                .filter(o -> o.getRightNum() > 0)
                .forEach(o -> {
                    int rate = new BigDecimal(100 * o.getRightNum()).divide(new BigDecimal(o.getNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    o.setRate(rate);
                });
        //******* end 每一题的正确率*************//

        List<ExamTypePart.Question> questions = newHomeworkQuestions.stream().filter(o -> map.containsKey(o.getQuestionId())).map(o -> map.get(o.getQuestionId())).collect(Collectors.toList());

        //******* begin 每一题的分享地址添加*************//
        for (int i = 0; i < questions.size(); i++) {
            ExamTypePart.Question question = questions.get(i);
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
        //******* end 每一题的分享地址添加*************//

        int averScore = new BigDecimal(totalScore).divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        long averDuration = new BigDecimal(totalDuration).divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP).longValue();
        examTypePart.setQuestion(questions);
        examTypePart.setAverScore(averScore);
        examTypePart.setAverDuration(averDuration);
        examTypePart.setSubContent("班平均分" + averScore + " 平均用时" + averDuration + "min");
        examTypePart.setHasFinishUser(true);
        ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
        String pageUrl;
        if (ObjectiveConfigType.ONLINE_DICTATION.equals(type)) {
            pageUrl = "/view/reportv5/clazz/dictation";
        } else {
            pageUrl = "/view/reportv5/clazz/questionsdetail";
        }
        String url = UrlUtils.buildUrlQuery(pageUrl,
                MapUtils.m(
                        "homeworkId", newHomework.getId(),
                        "type", type,
                        "subject", newHomework.getSubject(),
                        "param", JsonUtils.toJson(param)));
        examTypePart.setUrl(url);
        examTypePart.setShowUrl(true);
        result.put(type, examTypePart);
    }
}
