package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ReportPersonalRateContext implements Serializable {
    private static final long serialVersionUID = 6915826502583400665L;
    private User user;
    private NewHomeworkResult newHomeworkResult;
    private NewHomework newHomework;
    private Map<String, NewQuestion> allNewQuestionMap;
    private Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap;
    private Map<Integer, NewContentType> contentTypeMap;
    private ObjectiveConfigType type;
    private Map<ObjectiveConfigType, Object> resultMap = new LinkedHashMap<>();


    public ReportPersonalRateContext(User user,
                             NewHomeworkResult newHomeworkResult,
                             NewHomework newHomework,
                             Map<String, NewQuestion> allNewQuestionMap,
                             Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap,
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
