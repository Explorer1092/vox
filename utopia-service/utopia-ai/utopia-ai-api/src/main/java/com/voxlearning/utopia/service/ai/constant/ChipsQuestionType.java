package com.voxlearning.utopia.service.ai.constant;

import lombok.Getter;

/**
 * 薯条英语v2题型类型
 */
@Getter
public enum ChipsQuestionType {

    choice_lead_in("情境导入选择", true),
    word_repeat( "单词跟读", true),
    sentence_repeat( "句子跟读", true),
    @Deprecated
    video_conversation( "视频对话", true),
    choice_sentence2pic("听句子选图", true),
    choice_word2pic("听单词选图", true),
    choice_word2trans("听单词选翻译", true),
    choice_sentence2audio("句子选音频", true),
    qa_sentence("句子问答", true),
    @Deprecated
    task_conversation("任务对话", true),
    choice_cultural("文化题单选", true),
    choice_cultural2pic("文化题选图", true),
    mock_qa("模拟考问答", true),
    mock_choice("模拟考选择", true),
    warm_up_vocabs("热身训练单词封面", false),
    chip_english_tips("小贴士", false),
    knowledge_expand_review("知识拓展回顾", false),
    video_lesson_to_pass("过考必备", false),
    result_page_to_pass("过考必备结果", false),
    scene_interlude("情景过场", false),
    video_dialogue("视频对话", true),
    task_npc("任务NPC", false),
    task_topic("任务话题", true),
    task_interlude("任务过场", false),
    video_question_to_pass("过考必备视频选择", true),
    audio_illustration("音频讲解", false),
    image_illustration("图片讲解", false),
    mock_qa_audio("问答练习题", true),
    mock_choice_audio("选择练习题", true),
    dialogue_video("对话视频", false),
    video_practice("视频练习", false),
    video_practice_cover("视频练习封面", false),
    role_play_cover("角色扮演封面", false),
    role_play_practice("角色扮演实践", true),
    unknown("未定义", false);


    private String desc;

    private boolean persistentResult;

    ChipsQuestionType(String desc, boolean persistentResult) {
        this.desc = desc;
        this.persistentResult = persistentResult;
    }



    public static ChipsQuestionType of(String str) {
        try {
            return ChipsQuestionType.valueOf(str);
        } catch (Exception e) {
            return unknown;
        }
    }
}
