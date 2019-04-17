package com.voxlearning.utopia.service.newhomework.api.context.selfstudy;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.context.AbstractContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author steven
 * @since 2017/2/3
 */
@Getter
@Setter
public class SelfStudyHomeworkContext extends AbstractContext<SelfStudyHomeworkContext> {

    private static final long serialVersionUID = -8080131357074727830L;

    // in
    private Long userId;                                        // 用户ID
    private User user;                                          // 用户
    private String homeworkId;                                  // 作业ID
    private ObjectiveConfigType objectiveConfigType;            // 作业形式 ObjectiveConfigType
    private String bookId;                                      // 课本ID
    private String unitGroupId;                                 // 单元组ID
    private String unitId;                                      // 单元ID
    private String lessonId;                                    // 课ID
    private String sectionId;                                   // 课时ID
    private Long practiceId;                                    // 应用ID
    private String clientType;                                  // 客户端类型:pc,mobile
    private String clientName;                                  // 客户端名称:***app
    private String ipImei;                                      // ip or imei
    private String userAgent;                                   // userAgent
    private StudyType learningType;                             // 学习类型，这个东西应该没用了
    private StudentHomeworkAnswer studentHomeworkAnswer; // 用户答案

    // middle
    private Long clazzId;                                       // 班级ID
    private Long clazzGroupId;                                  // 组ID
    private GroupMapper clazzGroup;                             // 组
    private SelfStudyHomework selfStudyHomework;                // 作业
    private Subject subject;                                    // 学科
    private PracticeType practiceType;                          // 基础应用

    // WQ_LoadWrongQuestion写入
//    private SelfStudyHomeworkResult selfStudyHomeworkResult;        // 作业中间结果
    // private Map<String, NewHomeworkProcessResult> wrongQuestions;   // 原作业错题<sourceQid, processResult>
    private Map<String, Double> standardScore;                  // 这道题目的标准分<doQid, 标准分>

    // CalculateScore 写入
    private Map<String, QuestionScoreResult> scoreResult;                               // 算分结果<doQid, QuestionScoreResult>
    private Map<String, List<List<String>>> standardAnswer = new LinkedHashMap<>();     // 标准答案<doQid, ...>
    private Map<String, List<List<Boolean>>> subGrasp = new HashMap<>();                // 作答区域的掌握情况<doQid, QuestionScoreResult>
    private Map<String, List<Double>> subScore = new HashMap<>();                       // 用于复合题子题得分情况

    private Map<String, List<List<NewHomeworkQuestionFile>>> files = new HashMap<>();   // 主观题文件信息

    // WQ_CreateHomeworkProcessResult 写入
    //private Map<String, SubHomeworkProcessResult> processResult;                        // 作业结果<doQid>
    private SubHomeworkProcessResult processResult;                                     // 作业结果<doQid>

    // out
    private Map<String, Map<String, Object>> result = new HashMap<>();
}
