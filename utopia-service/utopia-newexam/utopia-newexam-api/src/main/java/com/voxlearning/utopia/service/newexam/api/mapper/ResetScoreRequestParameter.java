package com.voxlearning.utopia.service.newexam.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ResetScoreRequestParameter implements Serializable {
    private static final long serialVersionUID = -3285614004405574541L;
    private String newExamId; //测试ID
    private String questionDocId; //题Id
    private List<List<String>> answer;//用户答案
    private String answerStr;
    private List<List<String>> errorAnswer;//错误答案
    private String errorAnswerStr;
    private boolean allUser;//是否全部用户
    private String studentIdsStr;
    private Set<Long> studentIds;//学生账号
    private String paperId;
}