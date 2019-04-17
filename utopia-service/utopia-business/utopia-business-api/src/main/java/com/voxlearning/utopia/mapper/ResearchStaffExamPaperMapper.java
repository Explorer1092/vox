/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.utopia.api.constant.ExamPaperCategory;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Maofeng Lu
 * @since 13-8-8 下午9:23
 */
public class ResearchStaffExamPaperMapper implements Serializable {
    private static final long serialVersionUID = -6862682654450096849L;

    @Getter
    @Setter
    @UtopiaSqlColumn(name = "BOOK_ID")
    private Long bookId;
    @Getter
    @Setter
    @UtopiaSqlColumn(name = "BOOK_NAME")
    private String bookName;
    //教材版本
    @Getter
    @Setter
    @UtopiaSqlColumn(name = "PRESS")
    private String press;
    @Getter
    @Setter
    @UtopiaSqlColumn(name = "CLASS_LEVEL")
    private Integer classLevel;
    @Getter
    @Setter
    @UtopiaSqlColumn(name = "TERM_TYPE")
    private Integer termType;
    @Getter
    @Setter
    @UtopiaSqlColumn(name = "EXAM_PAPER_ID")
    private String examPaperId;
    @Getter
    @Setter
    @UtopiaSqlColumn(name = "EXAM_PAPER_CATEGORY")
    private String examPaperCategory;
    @Getter
    @Setter
    //是否组卷
    private String examPaperName;

    @JsonIgnore
    public Term fetchtTermType() {
        return Term.of(termType);
    }

    //use web ftl
    @JsonIgnore
    public ExamPaperCategory fetchExamPaperCategory() {
        return ExamPaperCategory.of(examPaperCategory);
    }

    //use web ftl
    @JsonIgnore
    public ClazzLevel fetchClassLevel() {
        return ClazzLevel.parse(classLevel);
    }


}
