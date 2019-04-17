package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.athena.api.psr.entity.PsrSectionPak;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.data.SectionInfo;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.psr.entity.PsrExamContent;
import com.voxlearning.utopia.service.question.api.entity.AfentiPreviewQuestion;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ruib
 * @since 16/7/17
 */
@Getter
@Setter
@NoArgsConstructor
public class PushQuestionContext extends AbstractAfentiContext<PushQuestionContext> {
    private static final long serialVersionUID = 6504387646285432082L;

    // in
    private Long studentId;
    private Subject subject;
    private String bookId;
    private String unitId;
    private String sectionId;
    private Integer rank;
    private boolean authorized; // 是否付费用户
    private boolean ultimate;  // 是否终极关卡
    private AfentiLearningType learningType;  // 学习类型  城堡 or 预习
    private Boolean isNewRankBook = false; // 是否是新生成关卡的教材

    // middle
    private StudentDetail student;
    private long count = 0; // 当天推送了几个关卡
    private PsrExamContent psr;
    private Integer unitRank;
    private List<AfentiPreviewQuestion> previewQuestions; // 预习题目
    private List<PsrSectionPak> newRankQuestions;         // 新推题逻辑题目

    // out
    private List<AfentiLearningPlanPushExamHistory> histories = new ArrayList<>();

    public PushQuestionContext(Long studentId,
                               Subject subject,
                               String bookId,
                               String unitId,
                               String sectionId,
                               Integer rank,
                               boolean authorized,
                               boolean ultimate,
                               AfentiLearningType learningType,
                               boolean isNewRankBook) {
        this.studentId = Objects.requireNonNull(studentId);
        this.subject = Objects.requireNonNull(subject);
        this.bookId = Objects.requireNonNull(bookId);
        this.unitId = Objects.requireNonNull(unitId);
        this.sectionId = sectionId;
        this.rank = Objects.requireNonNull(rank);
        this.authorized = authorized;
        this.ultimate = ultimate;
        this.learningType = learningType;
        this.isNewRankBook = isNewRankBook;
    }
}
