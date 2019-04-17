package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 处理学生大作业结果的context
 *
 * @author Ruib
 * @author xuesong.zhang
 * @version 0.1
 * @since 2016/1/13
 */
@Getter
@Setter
public class HomeworkResultContext extends AbstractContext<HomeworkResultContext> {
    private static final long serialVersionUID = -8724482491704739223L;

    // in
    private Long userId; // 用户ID
    private User user; // 用户
    private String homeworkId; // 作业ID
    private ObjectiveConfigType objectiveConfigType; // 作业形式 ObjectiveConfigType
    private String bookId; // 课本ID
    private String unitGroupId; // 单元组ID
    private String unitId; // 单元ID
    private String lessonId; // 课ID
    private String sectionId; // 课时ID
    private Long practiceId; //应用ID
    private String pictureBookId; // 绘本id
    private String videoId; // 视频绘本id
    private String questionBoxId;   //新语文读背练习题包id
    private QuestionBoxType questionBoxType;   //新语文读背练习题包类型
    private String dubbingId; // 配音id
    private String videoUrl;  // 配音作品url
    private Map<String, Long> durations; // 新绘本阅读各模块耗时
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String ipImei; // ip or imei
    private Long consumeTime; // 绘本、配音、纸质口算的总耗时，包括阅读时间和做题时间（单位：毫秒）
    private String userAgent; // userAgent
    private StudyType learningType; // 学习类型
    private Boolean skipUploadVideo; // 配音专用属性（是否跳过上传视频）
    private List<StudentHomeworkAnswer> studentHomeworkAnswers;
    private List<StudentHomeworkAnswer> studentHomeworkOralAnswers;
    private Boolean finished; //如果基础应用或者重难点专项采用一题一题提交的话表示这个应用或者这个重点难点专项完成了
    private List<OcrMentalImageDetail> ocrMentalImageDetails; // 纸质口算专用属性，口算图片识别详情
    private List<OcrMentalImageDetail> ocrDictationImageDetails; // 纸质听写专用属性，图片识别详情
    private String stoneId; // 口语交际：情景包id | 字词讲练
    private String stoneType;//口语交际 ：情景包类型
    private String topicRoleId;//口语交际 ：人机交互用户所选择的情景包id
    private WordTeachModuleType wordTeachModuleType; //字词讲练模块类型
    private String courseId;   // 字词讲练:汉字文化模块课程ID

    // middle
    private Long clazzGroupId; // 组ID
    private GroupMapper clazzGroup; // 组
    private NewHomework homework; // 作业
    private NewHomeworkType newHomeworkType; // 作业类型
    private Subject subject; // 学科
    private PracticeType practiceType; //基础应用
    private Boolean repair; // 是否是补做
    private Map<String, NewQuestion> userAnswerQuestionMap;  //用户提交的答案的题目数据（用于获取试题的版本和docId，题目中的version和_id中横线后面的那个版本号可能不一致）
    private Map<String, Double> standardScore; // 这道题目的标准分
    private Map<String, NewHomeworkQuestion> questions; // 题目信息
    private Map<String, QuestionScoreResult> scoreResult; // 算分结果
    private Map<String, List<List<String>>> standardAnswer = new LinkedHashMap<>(); // 标准答案
    private Map<String, List<List<Boolean>>> subGrasp = new HashMap<>(); // 作答区域的掌握情况
    private Map<String, List<Double>> subScore = new HashMap<>(); // 作答区域的得分情况
    private Map<String, List<List<NewHomeworkQuestionFile>>> files = new HashMap<>(); // 主观题文件信息
    private LinkedHashMap<String, NewHomeworkProcessResult> processResult; // 作业结果
    private LinkedHashMap<String, NewHomeworkProcessResult> processOralResult; // 绘本中的跟读题作业结果
    private List<NewHomeworkProcessResult> ocrMentalProcessResults; // 纸质口算作业结果
    private List<NewHomeworkProcessResult> ocrDictationProcessResults; // 纸质听写作业结果
    private Double dubbingScore; // 绘本配音得分
    private AppOralScoreLevel dubbingScoreLevel; // 绘本配音得分等级

    // out
    private Map<String, Map<String, Object>> result = new HashMap<>();

    // temp，这个属性在基础练习全部改成单题提交后可以去掉
    private Boolean isOneByOne;

    private NewHomeworkResult newHomeworkResult;

    private Long duration; // 完成时长
    private Date timestamp;//答题开始时间
    //即时干预
    private Integer hintId;// 提示ID--> ImmediateInterventionType
    private Integer hintOptType;//干预类型	 0=下一个，无交互；1=是/否选择
    private String hintTag;//信息标签	用于区分干预类型
    private List<List<String>> interventionAnswer;  // 干预答案
    private Boolean interventionReSubmit; // 命中干预后的重新提交(暂时只有语文用这个字段)
}
