package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.AncientPoetryResultCacheMapper;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @author majianxin
 */
@Getter
@Setter
public class AncientPoetryProcessContext extends AbstractContext<AncientPoetryProcessContext> {

    private static final long serialVersionUID = 2763752630040430739L;
    // in
    private Long studentId;                                     // 学生ID
    private Integer regionId;                                   // 区ID
    private Long schoolId;                                      // 学校ID
    private Integer clazzLevel;                                 // 年级
    private String activityId;                                  // 活动ID
    private String missionId;                                   // 关卡ID
    private ModelType modelType;                                // 模块类型
    private String questionId;                                  // 题ID
    private String docId;                                       // 题docId
    private List<List<String>> answer;                          // 用户答案
    private Long durationMilliseconds;                          // 完成时长
    private List<String> studentAudioUrls;                      // 学生录音地址
    private List<String> parentAudioUrls;                       // 家长录音地址
    private boolean parentMission;                              // 是否是亲子助力关卡
    private Long parentId;                                      // 家长ID
    private String clientType;                                  // 客户端类型:pc,mobile
    private String clientName;                                  // 客户端名称:***app

    private String userAgent;                                   // userAgent
    private boolean correct;                                    // 是否是订正错题

    // middle
    private Date currentDate;
    private AncientPoetryActivity poetryActivity;
    private String missionResultId;
    private AncientPoetryMissionResult missionResult;
    private AncientPoetryMission mission;
    private AncientPoetryResultCacheMapper cacheMapper;
    private Double addStar;
    private Long addDuration;

    // CalculateScore 写入
    private QuestionScoreResult scoreResult;                    // 算分结果
    private List<List<String>> standardAnswer;                  // 标准答案
    private List<List<Boolean>> subGrasp;                       // 作答区域的掌握情况
    private Boolean grasp;                                      // 作答区域的掌握情况

    // out
    private Map<String, Map<String, Object>> result = new HashMap<>();
}
