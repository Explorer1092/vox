package com.voxlearning.utopia.service.ai.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Summer on 2018/3/27
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum LessonType {
    @Deprecated
    WarmUp("热身训练"),
    @Deprecated
    Dialogue("视频对话"),
    @Deprecated
    Task("任务对话"),
    lead_in("情境导入"),
    warm_up("热身训练"),
    vocab_charging("词汇拓展"),
    pattern_reserve("句型储备"),
    knowledge_extending("知识拓展"),
    video_lesson_to_pass("过考必备"),
    oral_mock_test("口语模拟考"),
    video_conversation("视频对话"),
    task_conversation("任务对话"),
    watch_video_lesson("看视频"),
    video_practice_lesson("做练习"),
    role_play_lesson("演角色"),
    review_learn_lesson("上课"),
    review_practice_lesson("练习"),
    mock_test_lesson_1("模考课程1"),
    mock_test_lesson_2("模考课程2"),
    unknown("未定义");
    private final String desc;
    public static LessonType safeOf(String name) {
        try {
            return LessonType.valueOf(name);
        } catch (Exception e) {
            return unknown;
        }
    }
}