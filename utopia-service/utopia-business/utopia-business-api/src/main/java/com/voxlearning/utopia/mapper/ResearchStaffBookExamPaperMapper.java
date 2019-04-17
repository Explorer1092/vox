package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Maofeng Lu
 * @since 13-12-4 下午5:55
 */
@Data
public class ResearchStaffBookExamPaperMapper implements Serializable {
    private static final long serialVersionUID = 5011131346281781414L;

    private Long bookId;
    private String examPaperId;
    private String examPaperName;
//    private ExamPaperCategory examPaperCategory;
}
