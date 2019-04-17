package com.voxlearning.utopia.service.newhomework.api.mapper.avenger;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/6/20
 */
@Getter
@Setter
public class AvengerHomeworkQuestion implements Serializable {

    private static final long serialVersionUID = 5218300372654902249L;

    private String questionId;              // 试题id
    private Long questionVersion;           // 试题版本号（xx_online_question中的ol_updated_at属性的getTime()时间戳）
    private String similarQuestionId;       // 类题id（错题订正特有）
    private Double score;                   // 题目分值
    private List<List<Integer>> submitWay;  // 属性说明来源于内容库，提交方式，0：直接作答；1：拍照；2：录音；
    private Integer seconds;                // 建议作答时间（单位：秒）

    // 以下是教材信息，只记录最低级别的教材结构id
    private String bookId;
    private String bookName;
    private String unitId;
    private String unitName;
    private String unitGroupId;
    private String unitGroupName;
    private String sectionId;
    private String sectionName;
    private String lessonId;
    private String lessonName;
    private String objectiveId;
    private String objectiveName;
    private Integer categoryId;             // 练习类型id（VOX_PRACTICE_TYPE中的）
    private Long practiceId;                // 应用id（VOX_PRACTICE_TYPE中的主键）

    // 以下属性为各个作业形式特有属性
    private String knowledgePointId;        // 知识点id
    private String questionBoxId;           // 作业包id
    private String paperId;                 // 试卷id
    private String pictureBookId;           // 阅读绘本id
    private String videoId;                 // 视频绘本的视频id
    private String dubbingId;               // 趣味配音的配音id
    private List<List<Integer>> answerWay;  // 语文朗读背诵,1000朗读，1001背诵

    // 下面这个属性为期末复习特有属性
    private String sourceType;              // 题目来源（期末复习作业有三个模块数据都写到EXAM这个作业形式下，用来区分来源）
}
