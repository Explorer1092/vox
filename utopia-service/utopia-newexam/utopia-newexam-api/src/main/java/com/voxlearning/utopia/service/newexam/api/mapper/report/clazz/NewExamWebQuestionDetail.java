package com.voxlearning.utopia.service.newexam.api.mapper.report.clazz;

import com.voxlearning.utopia.service.newexam.api.mapper.report.NewExamSingleSubQuestion;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class NewExamWebQuestionDetail implements Serializable {

    private static final long serialVersionUID = 657905821176998969L;
    private String qid;//题ID
    private List<NewExamSingleSubQuestion> subQuestions = new LinkedList<>();//小题列表
}
