package com.voxlearning.utopia.service.parent.homework.impl.template.correct;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.ErrorQuestion;
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
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisErrorFactor;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.DETAIL_COURSE_NAME;
import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.DEFAULT_ERROR_CAUSE;

/**
 * 作业题目接口
 *
 * @author Wenlong Meng
 * @since Mar 18,2019
 */
@Named
@Slf4j
@SupportCommand(Command.QUESTIONS)
public class QuestionsCorrectTemplate extends CorrectBaseTemplate {

    //local variables
    @Inject private HomeworkResultLoader homeworkResultLoader;
    @Inject private IntelDiagnosisClient intelDiagnosisClient;

    /**
     * 题目
     *
     * @param c
     * @return
     */
    @Override
    public MapMessage process(CorrectContext c){
        CorrectParam param = c.getParam();
        String homeworkId = param.getHomeworkId();
        Long studentId = param.getStudentId();
        String homeworkResultId = param.getHomeworkResultId();
        ObjectiveConfigType objectiveConfigType = param.getObjectiveConfigType();
        try{
            //作业结果及明细
            HomeworkResult correctHR = homeworkResultLoader.loadHomeworkResult(homeworkResultId);
            final Map<String, Object> examUnitMap = new HashMap<>();

            final List<String> eids = Lists.newArrayList();
            final List<String> sQids = Lists.newArrayList();
            if(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS == objectiveConfigType){
                correctHR.getErrorDiagnostics().stream().filter(e->e.get("courseId").equals(param.getCourseId())).collect(Collectors.groupingBy(m->m.get("courseId"))).values().stream().forEach(ds->{
                    String courseId = (String)ds.get(0).get("courseId");
                    Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(Collections.singleton(courseId));
                    String courseName = ObjectUtils.anyBlank(intelDiagnosisCourseMap) ? DETAIL_COURSE_NAME : intelDiagnosisCourseMap.get(courseId).getName();
                    String advice = StringUtils.join("认真学习1-3分钟课程《", courseName, "》，并完成后测题。");
                    List<Map<String, Object>> sourceQuestions = ds.stream().map(e->{
                        String qId = (String)e.get("qId");
                        eids.add(qId);
                        sQids.add((String)e.get("similarQid"));
                        return MapUtils.m("sourceQuestionId", qId,
                            "errorCause", errorCause((String)e.get("errorCause")),
                            "advice", advice);
                    }).collect(Collectors.toList());
                    List<Map<String, Object>> similarQuestions = ds.stream().map(e->
                        MapUtils.m(
                            "bookId", correctHR.getAdditions().get("bookId"),
                            "unitId", correctHR.getAdditions().get("unitId"),
                            "similarQuestionId", e.get("similarQid"))
                    ).collect(Collectors.toList());

                    examUnitMap.put(courseId, MapUtils.m("sourceQuestions", sourceQuestions, "similarQuestions", similarQuestions));
                });
            }else{
                eids.addAll(correctHR.getErrorQIds());
                sQids.addAll(correctHR.getErrorQIds());
                examUnitMap.putAll(eids.stream()
                        .collect(Collectors.toMap(Function.identity(), id->MapUtils.m(
                                "bookId", correctHR.getAdditions().get("bookId"),
                                "unitId", correctHR.getAdditions().get("unitId"),
                                "similarQuestionId", id))));
            }
            List<HomeworkProcessResult> hprs = homeworkResultLoader.loadHomeworkProcessResults(HomeworkUtil.generatorID(homeworkId, studentId));
            long duration = hprs.stream().filter(d->eids.contains(d.getQuestionId())).mapToLong(HomeworkProcessResult::getDuration).sum();//统计预计时间

            Map<String, Object> result = new HashMap<>();
            result.put("normalTime", duration);
            result.put("homeworkTag", "Correct");
            result.put("homeworkType", StudyType.selfstudy);
            result.put("examUnitMap", examUnitMap);
            result.put("eids", eids);
            result.put("sQids", sQids);
            return MapMessage.successMessage().add("result" , result);
        }catch (Exception e) {
            log.error("{}", JsonUtils.toJson(param), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 错因
     *
     * @param errorCauseId
     * @return
     */
    private String errorCause(String errorCauseId) {
            IntelDiagnosisErrorFactor intelDiagnosisErrorFactor= intelDiagnosisClient.loadErrorFactorsByIdIncludeDisabled(Collections.singleton(errorCauseId)).getOrDefault(errorCauseId, null);
            return ObjectUtils.get(()->intelDiagnosisErrorFactor.getDescription(), DEFAULT_ERROR_CAUSE);
    }

}
