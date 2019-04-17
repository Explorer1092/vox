package com.voxlearning.utopia.service.newhomework.api.context.bonus;

import com.voxlearning.utopia.service.newhomework.api.constant.AssignmentConfigType;
import com.voxlearning.utopia.service.newhomework.api.context.AbstractContext;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Getter
@Setter
public class AbilityExamAnswerContext extends AbstractContext<AbilityExamAnswerContext> {

    private static final long serialVersionUID = 1788079361933486776L;

    // in
    private String id;                                          // 作业ID
    private Long userId;                                        // 用户ID
    private QuestionDataAnswer answer;                          // 用户答案
    private String learningType;                                // 学习类型

    // middle
    private AssignmentConfigType type;                          // 类型
    private AbilityExamBasic abilityExamBasic;
    private Map<String, QuestionResult> questionResultMap;

    // out,key=qid
    private Map<String, Map<String, Object>> resultMap = new HashMap<>();

}
