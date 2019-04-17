package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
public class TypePartContext implements Serializable {
    private static final long serialVersionUID = -2149879284862226021L;
    private NewHomework newHomework;
    private Map<Long, NewHomeworkResult> newHomeworkResultMap;
    private Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap;
    private ObjectiveConfigType type;
    private String cdnBaseUrl;
    private Map<ObjectiveConfigType, Object> result = new LinkedHashMap<>();

    public TypePartContext(NewHomework newHomework,
                           Map<Long, NewHomeworkResult> newHomeworkResultMap,
                           Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap,
                           String cdnBaseUrl

    ) {
        this.newHomework = newHomework;
        this.newHomeworkResultMap = newHomeworkResultMap;
        this.newHomeworkProcessResultMap = newHomeworkProcessResultMap;
        this.cdnBaseUrl = cdnBaseUrl;
    }
}
