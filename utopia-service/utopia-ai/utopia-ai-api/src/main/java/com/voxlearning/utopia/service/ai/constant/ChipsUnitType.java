package com.voxlearning.utopia.service.ai.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ChipsUnitType {
    topic_learning("话题学习"),
    special_consolidation("专项巩固"),
    dialogue_practice("对话实战"),
    essential_to_pass("过考必备"),
    mock_test("模拟考"),
    short_lesson("短期旅行口语"),
    role_play_unit("角色扮演"),
    review_unit("复习单元"),
    mock_test_unit_1("模考单元1"),
    mock_test_unit_2("模考单元2"),
    unknown("未定义");

    private final String desc;
    public static ChipsUnitType safeOf(String name) {
        try {
            return ChipsUnitType.valueOf(name);
        } catch (Exception e) {
            return unknown;
        }
    }
}
