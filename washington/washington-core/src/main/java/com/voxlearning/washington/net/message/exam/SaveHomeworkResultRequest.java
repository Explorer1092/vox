package com.voxlearning.washington.net.message.exam;

import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 保存作业结果
 * Created by tanguohong on 2016/1/19.
 */
@Data
public class SaveHomeworkResultRequest  implements Serializable {

    private static final long serialVersionUID = 4600704074994622102L;

    private Long userId; // 用户ID
    private User user; // 用户
    private String homeworkId; // 作业ID
    private String objectiveConfigType; // 作业形式 ObjectiveConfigType
    private String bookId; // 课本ID
    private String unitGroupId; // 单元组ID
    private String unitId; // 单元ID
    private String lessonId; // 课ID
    private String sectionId; // 课时ID
    private String questionId; // 题ID
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String ipImei; // ip or imei
    private List<List<String>> answer;  // 用户答案
    private Long duration; // 完成时长
    private List<List<String>> fileUrls; // 文件地址 用于有作答过程的试题
    private String learningType; // 学习类型
    private String videoId; // 视频id
    private String questionBoxId;   //新语文读背练习题包id
    private QuestionBoxType questionBoxType;   //新语文读背练习题包类型
    private String fileUrl; // 课外拓展任务1主观题，学生录音文件
    private String stoneDataId; //字词讲练
    private String wordTeachModuleType; //字词讲练模块类型

    //口语详情部分
    private List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails; //口语题详情

    private String courseId;        // 巩固课程ID || 字词讲练:汉字文化模块课程ID
    private Boolean courseGrasp;    // 课程是否掌握(只针对没有后测题的课程)

    private String subject;

    private Date timestamp;//答题开始时间
    //即时干预
    private Integer hintId;// 提示ID--> ImmediateInterventionType
    private List<List<String>> interventionAnswer;  // 干预答案
    private boolean interventionReSubmit; // 是否命中干预后的重新提交(字词讲练)
    private List<List<String>> hwTrajectory; //Handwriting trajectory 手写轨迹
}
