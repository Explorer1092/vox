package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class PushReviewQuestionContext extends AbstractAfentiContext<PushReviewQuestionContext> {
    private static final long serialVersionUID = 6504387646285432082L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;
    @NonNull private String bookId;
    @NonNull private String unitId;

    // middle
    private long count = 0; // 当天推送了几个关卡
    private List<NewQuestion> reviewQuestions; // 预习题目

    // out
    private List<AfentiLearningPlanPushExamHistory> histories = new ArrayList<>();

}
