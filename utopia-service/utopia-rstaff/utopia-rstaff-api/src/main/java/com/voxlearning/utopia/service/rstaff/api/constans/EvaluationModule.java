package com.voxlearning.utopia.service.rstaff.api.constans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-09-12 12:24
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EvaluationModule {
    EvaluationStruct("loadEvaluationStruct","测评报告主体结构"),
    EvaluationSurvey("loadEvaluationSurvey","测评报告情况"),
    ProjectImplSituation("loadProjectImplSituation","获得项目实施情况"),
    StudentWholeSituation("loadStudentWholeSituation","获得学生整体情况"),
    StudentSubjectSituation("loadStudentSubjectSituation","获得学生学科表现"),
    Suggest("loadSuggest","获得评价总结与建议"),
    AttachedList("loadAttachedList","获得附表内容")

    ;

    public String moduleFunc;
    public String desc;
}
