package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class NewExamPaperInfo implements Serializable {
    private static final long serialVersionUID = 7995085848373140395L;
    private String paperId;
    private String paperName;
    private int joinNum;
    private int submitNum;
    private List<NewExamDetailToTheme> themes;
}
