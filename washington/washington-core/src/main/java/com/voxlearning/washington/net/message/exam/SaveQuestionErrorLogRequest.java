package com.voxlearning.washington.net.message.exam;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by tanguohong on 2016/7/26.
 */
@Data
public class SaveQuestionErrorLogRequest implements Serializable{

    private static final long serialVersionUID = 5283301449191869583L;

    private String questionId;//题ID
    private String errorCode;//错误码
    private Long userId;//用户ID
    private String bookId; //能拿到就传
    private String unitId;//能拿到就传
    private String lessonId;//能拿到就传
    private String sectionId;//能拿到就传
    private String studyType; //能拿到就传
    private String homeworkId; // 作业ID(作业才有)
    private String examId; // 考试ID(模考才有)
    private String practiceId; // 应用ID(基础作业才有)
    private String unitRank; // 单元关卡(阿分题才有)
    private String rank; // 关卡(阿分题才有)
    private String clientType; // 客户端类型:pc,mobile
    private String clientName; // 客户端名称:***app
    private String remark; // 备注没有考虑到的其他扩展信息都可以放到这(Json格式)

}
