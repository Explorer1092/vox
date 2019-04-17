package com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach;

import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 字词训练模块Data
 * @author: Mr_VanGogh
 * @date: 2018/12/14 下午4:42
 */
@Getter
@Setter
public class WordExerciseModuleData implements Serializable {

    private static final long serialVersionUID = -6338050698358121658L;

    private WordTeachModuleType moduleType;
    private String moduleName;      //模块名称
    private Double firstScore;      //首次得分
    private Double finalScore;      //最终得分
    private boolean hasIntervention; //是否存在干预
    private List<WordExerciseQuestionData> wordExerciseQuestionData;    //题目信息

    @Getter
    @Setter
    public static class WordExerciseQuestionData implements Serializable {
        private static final long serialVersionUID = -4267877079203318411L;
        private String qid;
        private String contentType;     //题目类型
        private int difficulty;         //难度
        private String standardAnswers; //标准答案
        private String userAnswers;     //学生答案
    }
}
