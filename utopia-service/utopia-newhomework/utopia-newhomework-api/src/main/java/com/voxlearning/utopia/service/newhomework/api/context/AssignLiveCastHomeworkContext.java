package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2016/12/19
 */
@Getter
@Setter
public class AssignLiveCastHomeworkContext extends AbstractContext<AssignLiveCastHomeworkContext> {

    private static final long serialVersionUID = 5026179963520635505L;

    private HomeworkSource source;
    private Teacher teacher;
    private HomeworkSourceType homeworkSourceType;
    private Date homeworkStartTime;
    private Date homeworkEndTime;
    private Long duration;
    private String remark;
    private NewHomeworkType newHomeworkType;
    private HomeworkTag homeworkTag;
    private List<NewHomeworkPracticeContent> practices = new ArrayList<>();
    private boolean includeSubjective = false;
    private LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practicesBooksMap = new LinkedHashMap<>();
    private Set<Long> groupIds = new HashSet<>();
    private final LinkedHashMap<Long, LiveCastHomework> assignedGroupHomework = new LinkedHashMap<>();
}
