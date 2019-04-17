package com.voxlearning.utopia.service.rstaff.api.constans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-09-12 12:24
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ItemAnalysisModule {
    ItemAnalysisStruct("loadItemAnalysisStruct","获得报告目录"),
    ExamPaperReliability("loadExamPaperReliability","获得试卷效度和信度"),
    ItemAnalysisResult("loadItemAnalysisResult","获得题目分析"),
    NotGoodItemAnalysisResult("loadNotGoodItemAnalysisResult","获得题目分析之指标不佳的题目"),
    SetAQuestionSuggest("loadSetAQuestionSuggest","获得总体命题建议")
    ;

    public String moduleFunc;
    public String desc;

}
