package com.voxlearning.washington.net.message.exam;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tanguohong on 2015/7/16.
 */
@Data
public class HomeworkExamResultRequest implements Serializable {

    private static final long serialVersionUID = 6475068066742729607L;

    private String hid; //作业ID
    private String packageId; //包ID，假期作业才有
    private String subject; //科目
    private String learningType; //学习类型
    private Boolean finished; //是否完成
    private String qid; //题ID
    private Long bookId; //课本ID
    private Long unitId; //单元ID
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String imei;
    private List<String> fileUrls;   // 文件地址 用于有作答过程的试题

    List<QuestionResultMapper> homeworkExamResults;//做题结果
}
