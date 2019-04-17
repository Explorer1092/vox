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
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReportRateContext implements Serializable {
    private static final long serialVersionUID = -3432632239560748631L;

    private Map<String, Object> result = new LinkedHashMap<>();
    private Map<String, NewQuestion> allNewQuestionMap;
    private Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap;
    private Map<Long, NewHomeworkResult> newHomeworkResultMap;
    private Map<Long, User> userMap;
    private NewHomework newHomework;
    private boolean isPcWay;
    private Map<Integer, NewContentType> contentTypeMap;
    private ObjectiveConfigType type;
//    private Map<ObjectiveConfigType, List<NewHomeworkProcessResult>> tempMap;
    private List<String> questions;


    public ReportRateContext(Map<String, NewQuestion> allNewQuestionMap,
                             Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap,
                             Map<Long, NewHomeworkResult> newHomeworkResultMap,
                             Map<Long, User> userMap,
                             NewHomework newHomework,
                             boolean isPcWay,
                             Map<Integer, NewContentType> contentTypeMap
    ) {
        this.allNewQuestionMap = allNewQuestionMap;
        this.newHomeworkProcessResultMap = newHomeworkProcessResultMap;
        this.newHomeworkResultMap = newHomeworkResultMap;
        this.userMap = userMap;
        this.newHomework = newHomework;
        this.isPcWay = isPcWay;
        this.contentTypeMap = contentTypeMap;
//        this.tempMap = tempMap;
    }

}
