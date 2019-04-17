package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
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
public class LiveCastReportRateContext implements Serializable {
    private static final long serialVersionUID = -3628824562939140371L;

    private Map<String, Object> result = new LinkedHashMap<>();
    private Map<String, NewQuestion> allNewQuestionMap;
    private Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap;
    private Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap;
    private Map<Long, User> userMap;
    private LiveCastHomework liveCastHomework;
    private boolean isPcWay;
    private Map<Integer, NewContentType> contentTypeMap;
    private ObjectiveConfigType type;
    private Map<ObjectiveConfigType, List<LiveCastHomeworkProcessResult>> tempMap;



    private Map<ObjectiveConfigType, Object> resultMap = new LinkedHashMap<>();
    private User user;
    private LiveCastHomeworkResult liveCastHomeworkResult;

    public LiveCastReportRateContext(User user,
                                     LiveCastHomeworkResult liveCastHomeworkResult,
                                     LiveCastHomework liveCastHomework,
                             Map<String, NewQuestion> allNewQuestionMap,
                             Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap,
                             Map<Integer, NewContentType> contentTypeMap
    ) {
        this.user = user;
        this.liveCastHomeworkResult = liveCastHomeworkResult;
        this.liveCastHomework = liveCastHomework;
        this.allNewQuestionMap = allNewQuestionMap;
        this.liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultMap;
        this.contentTypeMap = contentTypeMap;
    }


    public LiveCastReportRateContext(Map<String, NewQuestion> allNewQuestionMap,
                             Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap,
                             Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap,
                             Map<Long, User> userMap,
                                     LiveCastHomework liveCastHomework,
                             Map<Integer, NewContentType> contentTypeMap
    ) {
        this.allNewQuestionMap = allNewQuestionMap;
        this.liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultMap;
        this.liveCastHomeworkResultMap = liveCastHomeworkResultMap;
        this.userMap = userMap;
        this.liveCastHomework = liveCastHomework;
        this.contentTypeMap = contentTypeMap;
    }
}
