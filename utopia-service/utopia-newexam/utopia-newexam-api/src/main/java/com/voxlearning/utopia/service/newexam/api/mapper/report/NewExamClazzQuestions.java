package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NewExamClazzQuestions implements Serializable {
    private static final long serialVersionUID = -3596646391597897670L;
    private String newExamName;
    private String clazzName;
    private boolean hasOral;
    private String examEndTime;
    private String examCorrectEndTime;
    private List<NewExamPaperInfo> newExamPaperInfos = new LinkedList<>();

    private Map<String, List<NewExamDetailToTheme>> themes = new LinkedHashMap<>();

}
