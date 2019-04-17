package com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework;

import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


@Setter
@Getter
public class ReportRateContext implements Serializable {

    private static final long serialVersionUID = 3632662226454859990L;
    private User user;
    private VacationHomework newHomework;
    private VacationHomeworkResult newHomeworkResult;
    private ObjectiveConfigType type;
    private Map<String, NewQuestion> allNewQuestionMap;
    private Map<String, VacationHomeworkProcessResult> newHomeworkProcessResultMap;
    private Map<Integer, NewContentType> contentTypeMap;
    private Map<ObjectiveConfigType, Object> resultMap = new LinkedHashMap<>();


    public ReportRateContext(User user,
                             VacationHomeworkResult newHomeworkResult,
                             VacationHomework newHomework,
                             Map<String, NewQuestion> allNewQuestionMap,
                             Map<String, VacationHomeworkProcessResult> newHomeworkProcessResultMap,
                             Map<Integer, NewContentType> contentTypeMap
    ) {
        this.user = user;
        this.newHomeworkResult = newHomeworkResult;
        this.newHomework = newHomework;
        this.allNewQuestionMap = allNewQuestionMap;
        this.newHomeworkProcessResultMap = newHomeworkProcessResultMap;
        this.contentTypeMap = contentTypeMap;
    }
}