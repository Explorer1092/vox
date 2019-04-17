package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/1/22
 */
@Getter
@Setter
public class AssignSelfStudyHomeworkContext extends AbstractContext<AssignSelfStudyHomeworkContext> {

    private static final long serialVersionUID = -7869579967216411353L;

    private Long studentId;     // 学生id
    private String sourceHomeworkId;// 源作业id
    private Subject subject;    // 学科
    private Long groupId;       // 本次作业所属组id，根据学生和学科拿到组id
    private HomeworkSource source;
    private HomeworkSourceType homeworkSourceType;
    private Long duration;
    private String remark;
    private String des;
    private NewHomeworkType newHomeworkType;
    private HomeworkTag homeworkTag;
    private List<NewHomeworkPracticeContent> practices = new ArrayList<>();
    private boolean includeSubjective = false;
    private LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practicesBooksMap = new LinkedHashMap<>();
    private String homeworkId;
    private SelfStudyHomework assignedHomework;
}
