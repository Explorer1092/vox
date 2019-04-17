package com.voxlearning.utopia.service.newhomework.api.context.livecast;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AbstractContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Getter
@Setter
public class LiveCastHomeworkResultContext extends AbstractContext<LiveCastHomeworkResultContext> {

    private static final long serialVersionUID = 2668139115736965097L;

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
    private Long consumeTime; // 绘本的总耗时，包括阅读时间和做题时间（单位：毫秒）
    private String dubbingId; // 配音id
    private String videoUrl;  // 配音作品url
    private Map<String, Long> durations; // 新绘本阅读各模块耗时
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String ipImei; // ip or imei
    private String userAgent; // userAgent
    private StudyType learningType; // 学习类型
    private List<StudentHomeworkAnswer> studentHomeworkAnswers;
    private List<StudentHomeworkAnswer> studentHomeworkOralAnswers;
    private Boolean finished; //如果基础应用或者重难点专项采用一题一题提交的话表示这个应用或者这个重点难点专项完成了

    // middle
    // private Long clazzId; // 班级ID
    // private Clazz clazz; // 班级
    private Long clazzGroupId; // 组ID
    // private GroupMapper clazzGroup; // 组
    // private ThirdPartyGroupMapper thirdPartyGroupMapper; // 第三方
    private LiveCastHomework liveCastHomework; // 作业
    private NewHomeworkType newHomeworkType; // 作业类型
    private Subject subject; // 学科
    private PracticeType practiceType; //基础应用
    // private Boolean repair; // 是否是补做
    private Map<String, Double> standardScore; // 这道题目的标准分
    private Map<String, NewHomeworkQuestion> questions; // 题目信息
    private Map<String, QuestionScoreResult> scoreResult; // 算分结果
    private Map<String, List<List<String>>> standardAnswer = new LinkedHashMap<>(); // 标准答案
    private Map<String, List<List<Boolean>>> subGrasp = new HashMap<>(); // 作答区域的掌握情况
    private Map<String, List<Double>> subScore = new HashMap<>(); // 作答区域的得分情况
    private Map<String, List<List<NewHomeworkQuestionFile>>> files = new HashMap<>(); // 主观题文件信息
    private LinkedHashMap<String, LiveCastHomeworkProcessResult> processResult; // 作业结果
    private LinkedHashMap<String, LiveCastHomeworkProcessResult> processOralResult; // 绘本中的跟读题作业结果
    private Double dubbingScore; // 绘本配音得分
    private AppOralScoreLevel dubbingScoreLevel; // 绘本配音得分等级

    // out
    private Map<String, Map<String, Object>> result = new HashMap<>();
    private LiveCastHomeworkResult liveCastHomeworkResult;

}
