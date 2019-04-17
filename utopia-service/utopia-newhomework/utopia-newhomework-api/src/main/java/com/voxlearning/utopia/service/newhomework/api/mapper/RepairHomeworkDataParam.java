package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class RepairHomeworkDataParam implements Serializable {
    private static final long serialVersionUID = 302102644923319103L;
    private String hid;
    private ObjectiveConfigType type;
    //学生ID不存在的时候整份作业一起处理
    private Long userId;
    private String qid;
    //原生的答案
    private String formerAnswer;

    private List<List<String>> userAnswers;
    private Boolean grasp;
    private List<List<Boolean>> subGrasp;
    private List<Double> subScore;
    private double score;
    // 用于应用类和绘本类做题结果存储 BaseHomeworkResultAnswer.appAnswers.key
    // app类 <{categoryId-lessonId}, BaseHomeworkResultAppAnswer>
    // 绘本类 <{readingId}, BaseHomeworkResultAppAnswer>
    // 重难点视频 <{video}, BaseHomeworkResultAppAnswer>
    // 新朗读背诵 <{questionBoxId}, BaseHomeworkResultAppAnswer>
    // 巩固课程 <{courseId}, BaseHomeworkResultAppAnswer>
    private String keyStr;
    private String oralScoreLevel;
}
