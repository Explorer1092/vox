package com.voxlearning.utopia.service.parent.homework.impl.template.correct;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.model.Command;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.SupportCommand;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectBaseTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectContext;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订正作业接口
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
@Slf4j
@SupportCommand(Command.DO)
public class DoCorrectTemplate extends CorrectBaseTemplate {

    //local variables
    @Inject private HomeworkResultLoader homeworkResultLoader;

    /**
     * do
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
        //作业
        HomeworkResult hr = homeworkResultLoader.loadHomeworkResult(homeworkResultId);
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> flashVars = new HashMap<>();
        flashVars.put("homeworkId", homeworkId);
        flashVars.put("objectiveConfigType", objectiveConfigType.name());
        flashVars.put("objectiveConfigTypeName", objectiveConfigType.getValue());
        flashVars.put("subject", hr.getSubject());
        flashVars.put("userId", studentId);
        flashVars.put("learningType", StudyType.selfstudy);
        flashVars.put("questionUrl", url(objectiveConfigType.name(),Command.QUESTIONS, homeworkId,studentId,homeworkResultId));
        flashVars.put("processResultUrl", url(objectiveConfigType.name(),Command.SUBMIT, homeworkId,studentId,homeworkResultId));
        flashVars.put("completedUrl", url(objectiveConfigType.name(),Command.ANSWERS, homeworkId,studentId,homeworkResultId));
        List<Map<String, Object>> practices = Lists.newArrayList();
        if(objectiveConfigType == ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS){
            Map<String, HomeworkProcessResult> hprs = homeworkResultLoader.loadHomeworkProcessResults(homeworkResultId).stream().collect(Collectors.toMap(HomeworkProcessResult::getQuestionId, Function.identity()));
            hr.getErrorDiagnostics().stream().collect(Collectors.groupingBy(r->r.get("courseId"))).values().forEach(e->{
                boolean finished = e.stream().filter(e1->hprs.containsKey(e1.get("qId"))).count() == e.size();
                String courseId = (String)e.get(0).get("courseId");
                practices.add(MapUtils.m("id", courseId,
                        "questionUrl",url(objectiveConfigType.name(),Command.QUESTIONS, homeworkId,studentId,homeworkResultId, courseId),
                        "completedUrl",url(objectiveConfigType.name(),Command.ANSWERS, homeworkId,studentId,homeworkResultId, courseId),
                        "finished",finished
                        ));
            });
            flashVars.put("practices", practices);
            flashVars.put("courseUrl", "exam/flash/light/interaction/v2/course.api");
        }

        data.put("flashVars", JsonUtils.toJson(flashVars));
        return MapMessage.successMessage().add("data", data);
    }

}
