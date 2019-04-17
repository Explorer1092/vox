package com.voxlearning.washington.net.message.exam;

import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 保存考试结果
 * Created by tanguohong on 2016/3/9.
 */
@Data
public class SaveNewExamResultRequest implements Serializable {

    private static final long serialVersionUID = -6107167457963418050L;

    private String newExamId; // 考试ID
    private String paperId;       // 试卷Id
    private String partId;        // 模块ID
    private String questionId;    // 题ID
    private String questionDocId; // 题DocID
    private String clientType;    // 客户端类型:pc,mobile
    private String clientName;    // 客户端名称:***app
    private String ipImei;        // ip or imei
    private List<List<String>> answer;  // 用户答案
    private Long durationMilliseconds;  // 完成时长
    private List<List<String>> fileUrls; // 文件地址 用于有作答过程的试题
    private String learningType;  // 学习类型

    //口语部分
    private List<List<NewExamProcessResult.OralDetail>> oralScoreDetails; //口语题详情
}
