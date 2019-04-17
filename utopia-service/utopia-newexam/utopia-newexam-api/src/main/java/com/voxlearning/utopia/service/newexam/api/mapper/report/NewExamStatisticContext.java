package com.voxlearning.utopia.service.newexam.api.mapper.report;

import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class NewExamStatisticContext implements Serializable {
    private static final long serialVersionUID = 6973614326192605492L;
    private Map<Long, User> userMap;
    private Map<String, NewExamResult> newExamResultMap;
    private Map<String, NewExamProcessResult> newExamProcessResultMap;
    private Map<String, NewPaper> newPaperMap;
    private NewExamStatistics newExamStatistics;
    private NewExam newExam;

    public NewExamStatisticContext(Map<Long, User> userMap,
                                   Map<String, NewExamResult> newExamResultMap,
                                   Map<String, NewExamProcessResult> newExamProcessResultMap,
                                   Map<String, NewPaper> newPaperMap,
                                   NewExamStatistics newExamStatistics,
                                   NewExam newExam
    ) {
        this.userMap = userMap;
        this.newExamResultMap = newExamResultMap;
        this.newExamProcessResultMap = newExamProcessResultMap;
        this.newPaperMap = newPaperMap;
        this.newExamStatistics = newExamStatistics;
        this.newExam = newExam;
    }
}
