package com.voxlearning.utopia.service.parent.homework.impl.template.correct;

import com.google.common.collect.Sets;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.model.Command;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.SupportCommand;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectBaseTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectContext;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 作业答案接口
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
@Slf4j
@SupportCommand(Command.ANSWERS)
public class AnswersCorrectTemplate extends CorrectBaseTemplate {

    //local variables
    @Inject private HomeworkLoader homeworkLoader;
    @Inject private HomeworkResultLoader homeworkResultLoader;

    /**
     * 获取答案
     *
     * @param c
     * @return 答案信息
     */
    @Override
    public MapMessage process(CorrectContext c){
        CorrectParam param = c.getParam();
        String homeworkId = param.getHomeworkId();
        Long studentId = param.getStudentId();
        String courseId = param.getCourseId();
        ObjectiveConfigType objectiveConfigType = param.getObjectiveConfigType();
        //订正作业结果
        String homeworkResultId = param.getHomeworkResultId();
        HomeworkResult correctHR = homeworkResultLoader.loadHomeworkResult(homeworkResultId);
        if(objectiveConfigType == ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS){
            Set<String> sqids = Sets.newHashSet();
            Set<String> qids = Sets.newHashSet();
            correctHR.getErrorDiagnostics().stream().filter(e->e.get("courseId").equals(courseId)).forEach(e->{
                sqids.add((String)e.get("similarQid"));
                qids.add((String)e.get("qId"));
            });
            List<HomeworkProcessResult> correctHPRs = homeworkResultLoader.loadHomeworkProcessResults(homeworkResultId)
                    .stream()
                    .filter(e->sqids.contains(e.getQuestionId())).collect(Collectors.toList());
            Map<String, Object> correctQuestionAnswers = this.build(correctHPRs);
            //原作业结果
            List<HomeworkProcessResult> hprs = homeworkResultLoader.loadHomeworkProcessResults(HomeworkUtil.generatorID(homeworkId, studentId))
                    .stream()
                    .filter(hpr->qids.contains(hpr.getQuestionId()))
                    .collect(Collectors.toList());
            Map<String, Object> sourceQuestionAnswers = this.build(hprs);
            return MapMessage.successMessage().add("result", MapUtils.m("sourceQuestionAnswers", sourceQuestionAnswers,
                    "doQuestionAnswers", correctQuestionAnswers));
        }else{
            List<HomeworkProcessResult> correctHPRs = homeworkResultLoader.loadHomeworkProcessResults(homeworkResultId)
                    .stream()
                    .filter(e->correctHR.getErrorQIds().contains(e.getQuestionId())).collect(Collectors.toList());
            Map<String, Object> correctQuestionAnswers = this.build(correctHPRs);
            return MapMessage.successMessage().add("result",correctQuestionAnswers);
        }

    }

    /**
     * 构建作业结果
     *
     * @param hprs
     * @return
     */
    private Map<String, Object> build(List<HomeworkProcessResult> hprs){
        return hprs.stream().collect(Collectors.toMap(HomeworkProcessResult::getQuestionId, e -> MapUtils.m(
                "sourceQuestion", e.getQuestionId(),
                "subMaster", e.getUserSubGrasp(),
                "master", e.getRight(),
                "userAnswers", e.getUserAnswers(),
                "fullScore", e.getScore(),
                "score",e.getUserScore()
        )));
    }

}
