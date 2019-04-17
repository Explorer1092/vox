package com.voxlearning.utopia.service.ai.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChipsQuestionBO implements Serializable {

    private static final long serialVersionUID = -1L;
    private String qId;
    private String lessonId;
    private String unitId;
    private String bookId;
}
