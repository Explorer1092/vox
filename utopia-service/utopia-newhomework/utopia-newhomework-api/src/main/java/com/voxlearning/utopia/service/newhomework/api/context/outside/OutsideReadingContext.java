package com.voxlearning.utopia.service.newhomework.api.context.outside;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.context.AbstractContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author majianxin
 */
@Getter
@Setter
public class OutsideReadingContext extends AbstractContext<OutsideReadingContext> {

    private static final long serialVersionUID = -4963101428990358905L;
    // in
    private Long userId;                                        // 用户ID
    private User user;                                          // 用户
    private String readingId;                                   // 课外阅读ID
    private String missionId;                                   // 关卡ID
    private String clientType;                                  // 客户端类型:pc,mobile
    private String clientName;                                  // 客户端名称:***app
    private String userAgent;                                   // userAgent
    private StudentHomeworkAnswer studentHomeworkAnswer;        // 用户答案

    private String bookId;                                      // 图书ID
    private boolean missionFinished;                            // 当前关卡是否完成
    private boolean readingFinished;                            // 当前课外阅读是否完成
    private boolean addReadingCount;                            // 是否奖励字数

    private List<String> objectiveQuestionIds;                  // 应试题id列表
    private List<String> subjectiveQuestionIds;                 // 主观题id列表

    // middle
    private Long clazzGroupId;                                  // 组ID
    private OutsideReading outsideReading;
    private OutsideReadingResult readingResult;
    private Subject subject;                                    // 学科
    private Map<String, Double> standardScore;                  // 这道题目的标准分<doQid, 标准分>

    // CalculateScore 写入
    private Map<String, QuestionScoreResult> scoreResult;                               // 算分结果<doQid, QuestionScoreResult>
    private Map<String, List<List<String>>> standardAnswer = new LinkedHashMap<>();     // 标准答案<doQid, ...>
    private Map<String, List<List<Boolean>>> subGrasp = new HashMap<>();                // 作答区域的掌握情况<doQid, QuestionScoreResult>
    private Map<String, List<Double>> subScore = new HashMap<>();                       // 用于复合题子题得分情况

    private Map<String, List<List<NewHomeworkQuestionFile>>> files = new HashMap<>();   // 主观题文件信息

    private OutsideReadingProcessResult processResult;                                   // 作题结果

    // out
    private Map<String, Map<String, Object>> result = new HashMap<>();
}
