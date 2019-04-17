package com.voxlearning.washington.net.message.exam;

import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 2016/6/27.
 */
@Data
public class SaveNewHomeworkResultRequest implements Serializable {

    private static final long serialVersionUID = -4807083770781494301L;

    private Long userId; // 用户ID
    private User user; // 用户
    private String homeworkId; // 作业ID
    private String objectiveConfigType; // 作业形式 ObjectiveConfigType
    private String bookId; // 课本ID
    private String unitGroupId; // 单元组ID
    private String unitId; // 单元ID
    private String lessonId; // 课ID
    private String sectionId; // 课时ID
    private Long duration;    // 字词讲练 汉字文化模块用时
    private Long practiceId; //应用ID
    private String pictureBookId; // 绘本ID
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String ipImei; // ip or imei
    private String learningType; // 学习类型
    private Boolean skipUploadVideo; // 配音专用属性（是否跳过上传视频）
    private Long consumeTime; // 绘本、配音的总耗时，包括阅读时间和做题时间（单位：毫秒）
    private String videoId; // 视频id
    private String dubbingId; // 配音id
    private String videoUrl;  // 配音作业url
    private String questionBoxId;   //课文读背题包id
    private String stoneId;//口语交际 ： 情景包id
    private String stoneType;//口语交际 ：情景包类型
    private String topicRoleId;//口语交际 ：人机交互用户所选择的情景包id
    private QuestionBoxType questionBoxType;   //课文读背题包类型
    private Map<String, Long> durations; // 新绘本阅读各模块耗时
    private List<StudentHomeworkAnswer> studentHomeworkAnswers;
    private List<StudentHomeworkAnswer> studentHomeworkOralAnswers; // 绘本专用属性，存口语跟读题用的
    private List<OcrMentalImageDetail> ocrMentalImageDetails; // 纸质口算专用属性，口算图片识别详情
    private String stoneDataId; //字词讲练
    private String wordTeachModuleType; //字词讲练模块类型
    private String courseId;
}
