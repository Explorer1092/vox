package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class ObjectiveConfigTypePartContext implements Serializable {
    private static final long serialVersionUID = -6603031852543568277L;
    private Teacher teacher;
    private ObjectiveConfigType type;
    private Map<Long, NewHomeworkResult> newHomeworkResultMap;
    private NewHomework newHomework;
    private ObjectiveConfigTypeParameter parameter;
    private MapMessage mapMessage;
    private Map<Long, User> userMap;
    private NewHomeworkPracticeContent target;
    private String questionId;
    private String stoneDataId;

    public ObjectiveConfigTypePartContext(Teacher teacher,
                                          ObjectiveConfigType type,
                                          Map<Long, NewHomeworkResult> newHomeworkResultMap,
                                          NewHomework newHomework,
                                          ObjectiveConfigTypeParameter parameter,
                                          Map<Long, User> userMap,
                                          NewHomeworkPracticeContent target) {
        this.teacher = teacher;
        this.type = type;
        this.newHomeworkResultMap = newHomeworkResultMap;
        this.newHomework = newHomework;
        this.parameter = parameter;
        this.userMap = userMap;
        this.target = target;
    }
}
