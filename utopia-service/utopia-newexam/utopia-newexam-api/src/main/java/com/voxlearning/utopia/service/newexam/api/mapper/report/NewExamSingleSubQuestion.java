package com.voxlearning.utopia.service.newexam.api.mapper.report;


import com.voxlearning.utopia.service.question.api.entity.NewQuestionOralDictAnswer;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.Collator;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class NewExamSingleSubQuestion implements Serializable {
    private static final long serialVersionUID = -6245337933399624788L;
    private double standardScore;//标准分
    private double totalScore;
    private double rate;//得分率
    private String answerStatType;
    private String fetchQuestionUrl = "/exam/flash/load/newquestion/byids.api";
    private List<OralStudent> students = new LinkedList<>();//
    private List<StudentAnswer> answerStudentsList;
    private List<NewQuestionOralDictAnswer> kouYuReferenceAnswers;    // 参考答案
    private Integer subIndexRank; //小题对应的题号
    private int rightCount;//正确数量
    private int wrongCount;//错误数量

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class BaseStudent implements Comparable<BaseStudent>, Serializable {
        private static final long serialVersionUID = 389062541509852867L;
        private Long userId;
        private String userName;

        //学生姓名拼音首字母排序
        @Override
        public int compareTo(@NotNull BaseStudent student) {
            Collator collator = Collator.getInstance(java.util.Locale.CHINA);
            return collator.compare(getUserName(), student.getUserName());
        }
    }

    @Getter
    @Setter
    public static class OralStudent extends BaseStudent {
        private static final long serialVersionUID = 2676438246304634993L;
        private double score;
        private List<String> voiceUrls = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class StudentAnswer implements Serializable {
        private static final long serialVersionUID = -8226462602111440942L;
        private List<String> views;
        private String master;      //是否掌握(RIGHT, WRONG, OTHER, NOT_ANSWER)
        private Boolean studentDetailShowAnswer;//学生详情页是否显示学生答案
        private List<Boolean> subMaster;
        private List<Student> students = new LinkedList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Student extends BaseStudent {
        private static final long serialVersionUID = -4178300816749646743L;
        //<其他答案>时以下字段不为空
        private List<String> answer;
        private Boolean master;
        private List<Boolean> subMaster;

        public Student(Long userId, String userName) {
            super(userId, userName);
        }
    }
}
