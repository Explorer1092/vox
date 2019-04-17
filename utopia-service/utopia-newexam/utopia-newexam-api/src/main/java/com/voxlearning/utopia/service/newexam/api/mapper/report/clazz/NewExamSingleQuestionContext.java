package com.voxlearning.utopia.service.newexam.api.mapper.report.clazz;

import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.NewExamSingleSubQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 用于单小题，处理数据过程
 */
@Getter
@Setter
@AllArgsConstructor
public class NewExamSingleQuestionContext implements Serializable {
    private static final long serialVersionUID = 5185738441059592657L;
    private Long userId;
    private String userName;
    private double score;
    private NewExamProcessResult newExamProcessResult;//处理数据process
    private int subIndex;//小题号
    private NewExamSingleSubQuestion singleSubQuestion;//单个小题存储结构
    private NewQuestionsSubContents newQuestionsSubContents;
    private List<List<String>> top3Answer;
}
