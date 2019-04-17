package com.voxlearning.utopia.service.parent.homework.impl.template.correct.processor;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectContext;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 初始化上下文
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
public class InitSubmitCorrectProcessor implements IProcessor<CorrectContext> {

    //Local variables
    @Inject private HomeworkResultLoader homeworkResultLoader;
    @Inject private QuestionLoaderClient questionLoaderClient;

    //Logic
    /**
     * 初始化上下文
     *
     * @param  c
     * @return result
     */
    public void process(CorrectContext c) {
        CorrectParam param = c.getParam();
        if(param.getData()==null){
            LoggerUtils.info("data is null", param);
            c.setMapMessage(MapMessage.errorMessage("data is null"));
            return;
        }
        Map<String, Object> data = MapUtils.m("userAnswers", param.getData().get("answer"));
        data.putAll(param.getData());
        HomeworkProcessResult hpr = JsonUtils.safeConvertMapToObject(data, HomeworkProcessResult.class);

        HomeworkResult correctHR = homeworkResultLoader.loadHomeworkResult(param.getHomeworkResultId());
        correctHR.setFinished(ObjectUtils.get(()->(Boolean) data.get("finished"), Boolean.FALSE));
        //完成状态及时间设置
        if(correctHR.getFinished()){
            int size = homeworkResultLoader.loadHomeworkProcessResults(param.getHomeworkResultId()).size();
            correctHR.setFinished(size + 1 >= correctHR.getQuestionCount() + ObjectUtils.get(()->correctHR.getErrorDiagnostics().size(), 0));
            if(correctHR.getFinished()){
                correctHR.setEndTime(new Date());
            }
        }

        //构建作业结果详情
        hpr.setGrade(correctHR.getGrade());
        hpr.setHomeworkTag("Correct");
        hpr.setSubject(correctHR.getSubject());
        hpr.setType(StudyType.selfstudy.name());
        hpr.setHomeworkResultId(correctHR.getId());
        hpr.setUserId(param.getStudentId());
        //查询题目信息
        NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(hpr.getQuestionId());
        hpr.setQuestionDocId(question.getDocId());
        hpr.setQuestionVersion(question.getVersion());
        hpr.setHomeworkId(param.getHomeworkId());

        String sourceQuestionId = hpr.getQuestionId();
        if(param.getObjectiveConfigType() == ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS){//类题
            sourceQuestionId = (String)correctHR.getErrorDiagnostics().stream().filter(e->e.get("similarQid").equals(hpr.getQuestionId())).findFirst().get().get("qId");
        }
        Map<String, HomeworkProcessResult> hprMap = homeworkResultLoader.loadHomeworkProcessResults(HomeworkUtil.generatorID(param.getHomeworkId(), param.getStudentId())).stream().collect(Collectors.toMap(p->p.getQuestionId(), Function.identity()));
        hpr.setScore(hprMap.get(sourceQuestionId).getScore());
        hpr.setDuration(hprMap.get(sourceQuestionId).getDuration());
        c.setHomeworkResult(correctHR);
        c.setHomeworkProcessResults(Lists.newArrayList(hpr));
    }

}
