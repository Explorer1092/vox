package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorrectHomeworkContext extends AbstractContext<CorrectHomeworkContext> {

    private static final long serialVersionUID = 7777425883815153904L;

    private Long studentId;
    private String homeworkId;
    private String questionId;
    private Boolean review;
    private ObjectiveConfigType type;
    private CorrectType correctType;
    private Correction correction;
    private String teacherMark;
    private Boolean isBatch;

    private String questionBoxId;

    private boolean partFinished;

    private String processResultId;

    private NewHomework newHomework;

    private String newHomeworkResultId;


//  private Boolean result;


}
