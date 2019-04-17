package com.voxlearning.utopia.service.newhomework.api.constant;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2016/11/21
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TermReviewContentType {
    BASIC_WORD("单词表听认多维复习", "五分钟复习20个单词，7天复习整本教材", null, TermReviewType.BASIC, "resources/app/17teacher/res/assignment/basic.png", ObjectiveConfigType.BASIC_APP, "apps"),
    BASIC_SENTENCE("课文重点句温习", "连词成句，温习课文重点句", null, TermReviewType.BASIC, "resources/app/17teacher/res/assignment/basic.png", ObjectiveConfigType.BASIC_APP, "apps"),
    WORD("词汇短语专项", "单元词汇短语应试练习", "ENGLISH_REVIEW_WORD", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/word.png", ObjectiveConfigType.EXAM, "questions"),
    GRAMMAR("语法句型专项", "单元语法句型应试练习", "ENGLISH_REVIEW_GRAMMAR", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/grammar.png", ObjectiveConfigType.EXAM, "questions"),
    LISTENING("听力专项", "单元听力专项练习", "ENGLISH_REVIEW_LISTENING", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/listening.png", ObjectiveConfigType.EXAM, "questions"),
    READING("阅读专项", "阅读综合应试练习", "ENGLISH_REVIEW_READING", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/reading.png", ObjectiveConfigType.EXAM, "questions"),
    EN_NATION_ERROR("全市易错题", "教研员关注的特色模块", "ENGLISH_REVIEW_NATION_ERROR", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/nationerror.png", ObjectiveConfigType.EXAM, "questions"),
    EN_CLAZZ_ERROR("班级错题", "受老师欢迎的错题重练", "ENGLISH_REVIEW_CLAZZ_ERROR", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/clazzerror.png", ObjectiveConfigType.UNIT_QUIZ, "questions"),
    UNIT_PAPER("单元模拟卷", "单元应试练习", "ENGLISH_REVIEW_UNIT_PAPER", TermReviewType.PAPER, "resources/app/17teacher/res/assignment/paper.png", ObjectiveConfigType.UNIT_QUIZ, "questions"),

    BASIC_CALCULATION("学期必需计算练习回顾", "10天过一遍学期计算题型", null, TermReviewType.BASIC, "resources/app/17teacher/res/assignment/basic.png", ObjectiveConfigType.MENTAL, "questions"),
    UNIT_KEY_POINTS("单元典型例题回顾", "10天过一遍全部典型例题", "MATH_REVIEW_UNIT_KEY_POINTS", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/unitkeypoints.png", ObjectiveConfigType.EXAM, "questions"),
    MATH_NATION_ERROR("全市易错题", "教研员关注的特色模块", "MATH_REVIEW_NATION_ERROR", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/nationerror.png", ObjectiveConfigType.EXAM, "questions"),
    NUMBER("数与代数专项", "数与代数专项训练", "MATH_REVIEW_NUMBER", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/number.png", ObjectiveConfigType.EXAM, "questions"),
    GEOMETRY("图形几何专项", "图形几何专项训练", "MATH_REVIEW_GEOMETRY", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/geometry.png", ObjectiveConfigType.EXAM, "questions"),
    STATISTICS("统计概率专项", "统计概率专项训练", "MATH_REVIEW_STATISTICS", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/statistics.png", ObjectiveConfigType.EXAM, "questions"),
    PRACTICE("综合实践专项", "综合实践专项训练", "MATH_REVIEW_PRACTICE", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/practice.png", ObjectiveConfigType.EXAM, "questions"),
    MATH_CLAZZ_ERROR("班级错题", "受老师欢迎的错题重练", "MATH_REVIEW_CLAZZ_ERROR", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/clazzerror.png", ObjectiveConfigType.EXAM, "questions"),
    FINAL_PAPER("期末模拟卷", "期末应试模拟卷", "MATH_REVIEW_FINAL_PAPER", TermReviewType.PAPER, "resources/app/17teacher/res/assignment/paper.png", ObjectiveConfigType.UNIT_QUIZ, "questions"),
    UNIT_DIAGNOSIS("单元重点讲练测", "重点题目，做错推微课学习", "MATH_REVIEW_UNIT_DIAGNOSIS", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/paper.png", ObjectiveConfigType.INTELLIGENT_TEACHING, "questions"),

    BASIC_READ_RECITE_WITH_SCORE("重点课文读背", "重点课文，必背段落，温故而知新", null, TermReviewType.BASIC, "resources/app/17teacher/res/assignment/basic.png", ObjectiveConfigType.READ_RECITE_WITH_SCORE, "apps"),
    CH_WORD("生字", "生字练习", "WORD", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/chword.png", ObjectiveConfigType.BASIC_KNOWLEDGE, "questions"),
    APPLICATION("积累运用", "词语、句子积累运用", "APPLY", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/application.png", ObjectiveConfigType.BASIC_KNOWLEDGE, "questions"),
    CH_READING("阅读", "阅读题练习", "READING", TermReviewType.FOCUS, "resources/app/17teacher/res/assignment/chreading.png", ObjectiveConfigType.CHINESE_READING, "questions"),
    CH_UNIT_PAPER("单元模考卷", "单元应试练习", "UNIT_REVIEW", TermReviewType.PAPER, "resources/app/17teacher/res/assignment/paper.png", ObjectiveConfigType.UNIT_QUIZ, "questions"),
    CH_FINAL_PAPER("期末模考卷", "期末应试模拟卷", "PAPER", TermReviewType.PAPER, "resources/app/17teacher/res/assignment/paper.png", ObjectiveConfigType.UNIT_QUIZ, "questions");

    @Getter private final String value;
    @Getter private final String description;                       // 一句话描述
    @Getter private final String athenaName;                        // 对应大数据返回的二级模块
    @Getter private final TermReviewType termReviewType;            // 所属的一级分类
    @Getter private final String iconUrl;                           // icon地址
    @Getter private final ObjectiveConfigType objectiveConfigType;  // 对应的作业形式
    @Getter private final String keyName;                           // 保存时对应的key

    public static TermReviewContentType of(String type) {
        try {
            return valueOf(type);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static List<TermReviewContentType> getSubjectTypes(Subject subject) {
        if (Subject.ENGLISH == subject) {
            return Arrays.asList(BASIC_WORD, BASIC_SENTENCE, WORD, GRAMMAR, LISTENING, READING,
                    EN_NATION_ERROR, EN_CLAZZ_ERROR, UNIT_PAPER);
        } else if (Subject.MATH == subject) {
            return Arrays.asList(BASIC_CALCULATION, UNIT_KEY_POINTS, NUMBER, GEOMETRY, STATISTICS,
                    PRACTICE, MATH_NATION_ERROR, MATH_CLAZZ_ERROR, FINAL_PAPER, UNIT_DIAGNOSIS);
        } else if (Subject.CHINESE == subject) {
            return Arrays.asList(CH_WORD, APPLICATION, CH_READING, CH_UNIT_PAPER, CH_FINAL_PAPER, BASIC_READ_RECITE_WITH_SCORE);
        }
        return Collections.emptyList();
    }
}
