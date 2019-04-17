package com.voxlearning.washington.net.message.exam;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tanguohong on 2015/7/28.
 */
@Data
public class QuizExamResultRequest implements Serializable {

    private static final long serialVersionUID = -2343911855419199532L;

    private Long hid; //测验ID
//    private String pid; //试卷ID
    private String subject; //科目
    private String learningType; //学习类型
    private Boolean finished; //是否完成
    private String qid; //题ID
    private Long bookId; //课本ID
    private Long unitId; //单元ID
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String imei;

    List<QuestionResultMapper> homeworkExamResults;//做题结果
}
