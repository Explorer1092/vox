package com.voxlearning.utopia.service.newhomework.api.entity.ocr;

import lombok.AllArgsConstructor;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/9
 */
@AllArgsConstructor
public enum OcrFeedbackProblemType {
    @Deprecated RIGHT_CORRECT_WRONG("对的题被判错"),
    //@Deprecated HAS_QUESTION_UNCHECK("有题目没检查"),
    @Deprecated CHECK_ERROR_RANGE("检查范围有误"),
    @Deprecated WRONG_CORRECT_RIGHT("错的题被判对"),
    @Deprecated CHECK_UNRELATED_CONTENT("检查了无关内容"),
    @Deprecated OTHERS("其他问题/意见"),

    // 三期反馈和报错类型
    CHECK_ERROR("检查有错误"),
    HAS_QUESTION_UNCHECK("有题目未识别"),
    ANSWER_OR_ANALYSIS_ERROR("答案解析有错");

    public String desc;
}
