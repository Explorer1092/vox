package com.voxlearning.utopia.service.newhomework.impl.template;


import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import java.util.Map;

abstract public class VacationInternalProcessHomeworkAnswerTemple extends SpringContainerSupport {
    abstract public ObjectiveConfigType getObjectiveConfigType();

    abstract public void internalProcessHomeworkAnswer(Map<ObjectiveConfigType, Object> resultMap, Map<String, VacationHomeworkProcessResult> allProcessResultMap, Map<Integer, NewContentType> contentTypeMap, Map<String, NewQuestion> allQuestionMap, VacationHomework vacationHomework, VacationHomeworkResult vacationHomeworkResult, ObjectiveConfigType type);
}
