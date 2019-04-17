package com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public  class QuestionData implements Serializable {
    private static final long serialVersionUID = 2963115701224630949L;
    private String questionId; //題ID
    private String score; //分數
    private String answerResultWord; //每题显示文案
    private List<Map<String, Object>> sentences;
    private Boolean needRecord; //是否是口语题
    private Boolean answerInfo;
    private RecordData recordInfo;
}