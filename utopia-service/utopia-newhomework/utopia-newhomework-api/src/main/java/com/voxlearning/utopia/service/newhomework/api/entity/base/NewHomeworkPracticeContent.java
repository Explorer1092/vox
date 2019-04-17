package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticTimeLimit;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class NewHomeworkPracticeContent implements Serializable {

    private static final long serialVersionUID = -5630598652911620750L;

    private ObjectiveConfigType type;
    private MentalArithmeticTimeLimit timeLimit;        // 口算训练限定时间
    private Boolean mentalAward;                        // 口算训练是否有奖励
    private Boolean recommend;                          // 口算训练是否推荐题目
    private List<NewHomeworkQuestion> questions;        // 非应用类的作业类型用这个
    private Boolean includeSubjective;                  // 是否包含需要主观作答的试题

    private List<NewHomeworkApp> apps;                  // 应用类作业类型用这个（APP、绘本、视频、新朗读背诵、趣味配音、口语交际）

    // 纸质口算专属属性，这种作业形式没有题
    private String workBookId;                          // 纸质口算练习册id
    private String workBookName;                        // 纸质口算练习册名称
    private String homeworkDetail;                      // 纸质口算作业详情(页码)

    /**
     * 获取该类型的题目
     *
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> processNewHomeworkQuestion(Boolean includeReadingOral) {
        List<NewHomeworkQuestion> newHomeworkQuestions = new LinkedList<>();
        if (questions != null) {
            newHomeworkQuestions.addAll(questions);
        }
        if (apps != null) {
            apps.stream()
                    .filter(Objects::nonNull)
                    .map(o -> {
                        List<NewHomeworkQuestion> list = new LinkedList<>();
                        if (o.getQuestions() != null) {
                            list.addAll(o.getQuestions());
                        }
                        if (includeReadingOral && o.getOralQuestions() != null) {
                            list.addAll(o.getOralQuestions());
                        }
                        if (o.getHardQuestions() != null) {
                            list.addAll(o.getHardQuestions());
                        }
                        if (o.getEasyQuestions() != null) {
                            list.addAll(o.getEasyQuestions());
                        }
                        if (o.getWordExerciseQuestions() != null) {
                            list.addAll(o.getWordExerciseQuestions());
                        }
                        if (o.getImageTextRhymeQuestions() != null) {
                            for (ImageTextRhymeHomework imageTextRhymeHomework : o.getImageTextRhymeQuestions()) {
                                if (imageTextRhymeHomework.getChapterQuestions() != null) {
                                    list.addAll(imageTextRhymeHomework.getChapterQuestions());
                                }
                            }
                        }
                        return list;
                    }).forEach(newHomeworkQuestions::addAll);
        }
        return newHomeworkQuestions;
    }


}
