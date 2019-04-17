package com.voxlearning.utopia.agent.mockexam.service.dto.enums;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测评计划中的枚举值
 *
 * @author xiaolei.li
 * @version 2018/8/16
 */
public interface ExamPlanEnums {

    /**
     * 试卷类型
     */
    @AllArgsConstructor
    enum PaperType {
        OLD("已有试卷"),
        NEW("录入新试卷");
        public final String desc;
    }

    /**
     * 状态
     */
    @AllArgsConstructor
    enum Status {
        PLAN_AUDITING("销运审核中"),
        PLAN_WITHDRAW("已撤回"),
        PLAN_REJECT("销运已驳回"),
        PAPER_CHECKING("试卷审核中"),
        PAPER_REJECT("试卷被驳回"),
        PAPER_PROCESSING("试卷录入中"),
        PAPER_READY("试卷已录入"),
        EXAM_PUBLISHED("测评已上线"),
        EXAM_OFFLINE("测评已下线"),;
        public final String desc;
    }

    @AllArgsConstructor
    enum DistributeType {
        RANDOM("随机"),
        TAKE_TURNS("轮流");
        public final String desc;
    }


    @AllArgsConstructor
    enum ScoreRuleType {
        SCORE("分数制"),
        GRADE("等第制");
        public final String desc;
    }

    @AllArgsConstructor
    enum Grade {

        FIRST("一年级", ClazzLevel.FIRST_GRADE),
        SECOND("二年级", ClazzLevel.SECOND_GRADE),
        THIRD("三年级", ClazzLevel.THIRD_GRADE),
        FOURTH("四年级", ClazzLevel.FOURTH_GRADE),
        FIFTH("五年级", ClazzLevel.FIFTH_GRADE),
        SIXTH("六年级", ClazzLevel.SIXTH_GRADE);

        public final String desc;
        public final ClazzLevel clazzLevel;

        /**
         * 通过com.voxlearning.alps.annotation.meta.ClazzLevel#level获取枚举
         *
         * @param clazzLevel com.voxlearning.alps.annotation.meta.ClazzLevel#level
         * @return 枚举
         */
        public static Grade of(int clazzLevel) {
            ClazzLevel cl = ClazzLevel.parse(clazzLevel);
            return Arrays.stream(values()).filter(i -> i.clazzLevel == cl).findFirst().orElse(null);
        }


    }

    @AllArgsConstructor
    enum Form {
        MONTHLY("月考"),
        UNITE("单元考"),
        MID("期中考"),
        FINAL("期末考"),
        OTHER("其他");
        public final String desc;
        public static Form of(String name) {
            return Arrays.stream(values()).filter(i -> i.name().equals(name)).findFirst().orElse(null);
        }
    }


    @AllArgsConstructor
    enum Scene {
        ONLINE("在线"),
        FOCUS("集中");
        public final String desc;
    }

    @AllArgsConstructor
    enum RegionLevel {
        PROVINCE("省级", com.voxlearning.utopia.service.region.api.constant.RegionType.PROVINCE),
        CITY("市级", com.voxlearning.utopia.service.region.api.constant.RegionType.CITY),
        COUNTY("区级", com.voxlearning.utopia.service.region.api.constant.RegionType.COUNTY),
        SCHOOL("校级", com.voxlearning.utopia.service.region.api.constant.RegionType.UNKNOWN);

        public final String desc;
        public final com.voxlearning.utopia.service.region.api.constant.RegionType regionType;

    }

    @AllArgsConstructor
    enum SpokenScoreType {
        CEIL("向上取整"),
        ROUND("四舍五入");
        public final String desc;
    }

    @AllArgsConstructor
    enum SpokenAnswerTimes {
        ONE("一次"),
        TWO("二次"),
        TREE("三次"),
        INFINITE("无限制");
        public final String desc;
    }

    /**
     * 学科
     *
     * @see com.voxlearning.alps.annotation.meta.Subject
     */
    @AllArgsConstructor
    enum Subject {
        MATH("小学数学", com.voxlearning.alps.annotation.meta.Subject.MATH),
        ENGLISH("小学英语", com.voxlearning.alps.annotation.meta.Subject.ENGLISH),
        CHINESE("小学语文", com.voxlearning.alps.annotation.meta.Subject.CHINESE);
        public final String desc;
        public final com.voxlearning.alps.annotation.meta.Subject subject;

        public static Subject of(String name) {
            return Arrays.stream(values()).filter(i -> i.name().equals(name)).findFirst().orElse(null);
        }

        public static Subject of(int subjectId) {
            return Arrays.stream(values()).filter(i -> i.subject.getId() == subjectId).findFirst().orElse(null);
        }
    }

    @AllArgsConstructor
    enum Type {
        GENERAL("普通"),
        SPOKEN("口语"),
        AUDITION("听力"),
        CALCULATION("计算类");

        public final String desc;

        public static List<Type> of(List<String> names) {
            return Arrays.stream(values()).filter(i -> names.contains(i.name())).collect(Collectors.toList());
        }

    }

    @AllArgsConstructor
    enum Pattern {
        GENERAL("普通"),
        REGISTER("报名考"),
        ;
        public final String desc;

        public static Pattern of(String name) {
            return Arrays.stream(values()).filter(i -> i.name().equals(name)).findFirst().orElse(null);
        }
    }
}
