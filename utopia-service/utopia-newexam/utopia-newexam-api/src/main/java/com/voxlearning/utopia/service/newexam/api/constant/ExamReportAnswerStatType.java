package com.voxlearning.utopia.service.newexam.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 考试报告答案统计类型枚举
 *
 * @author majianxin
 * @version V1.0
 * @date 2018/8/24
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ExamReportAnswerStatType {

    CHOICE(Arrays.asList(1, 2, 3, 5)),
    TOP_3(Arrays.asList(4, 7, 8, 9, 10, 27, 28, 30)),
    SCORES(Arrays.asList(16, 26)),
    RIGHT_WRONG(Arrays.asList(25, 29)),
    ORAL(Arrays.asList(11, 12, 13, 14, 15, 18, 19, 20, 21));

    @Getter
    private final List<Integer> subContentTypeIds;

    /**
     * 根据基础题型ID获取枚举
     *
     * @param subContentTypeId 基础题型ID
     * @return {@link ExamReportAnswerStatType}
     */
    public static ExamReportAnswerStatType get(Integer subContentTypeId) {
        try {
            for (ExamReportAnswerStatType type : ExamReportAnswerStatType.values()) {
                if (type.subContentTypeIds.contains(subContentTypeId)) {
                    return type;
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}
