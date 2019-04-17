package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 给永磊的，NewHomeworkProcessResult的一个子集
 *
 * @author xuesong.zhang
 * @since 2016/8/4
 */
@Getter
@Setter
public class HomeworkProcessMapper implements Serializable {

    private static final long serialVersionUID = -7741290278483999993L;

    private String processId;                           // processId
    private Long clazzGroupId;                          // 组ID
    private String homeworkId;                          // 作业ID
    private String bookId;                              // 课本ID
    private String unitId;                              // 单元ID
    private String sectionId;                           // 课时ID
    private String lessonId;                            // lessonId
    private Long userId;                                // 学生ID
    private String questionId;                          // 题ID
    private Double standardScore;                       // 标准分保留四位小数
    private Double score;                               // 用户实际得分
    private Boolean grasp;                              // 是否掌握(全对/部分对)
    private List<List<Boolean>> subGrasp;               // 作答区域的掌握情况
    private List<List<String>> userAnswers;             // 用户答案
    private Long duration;                              // 完成时长（单位：毫秒）
    private SchoolLevel schoolLevel;                    //
    private Subject subject;                            // 学科
    private ObjectiveConfigType objectiveConfigType;    // 作业形式 ObjectiveConfigType
    private Date createAt;                              // 创建时间

    // 中学特有字段
    private Integer contentTypeId;                      // 题目的最子级题型
    private Integer oralScore;                          // 口语得分
    private List<Integer> subOralScores;                // 口语各小题得分
    private List<Long> isRightList;                     // 各小题正误状态
    private String wordId;                              // 题目对应的词汇ID
    private Integer tagId;                              // 本地化题型字段

    //语音类特有属性
    private VoiceEngineType voiceEngineType;    // 语音引擎
    private String voiceCoefficient;            // 打分系数，默认1.6
    private String voiceMode;                   // 打分模式，请看http://wiki.17zuoye.net/pages/viewpage.action?pageId=22749531
    private String voiceScoringMode;
    //口语详情
    private List<List<NewHomeworkProcessResult.OralDetail>> oralDetails;   // 应用跟读题、口语题详情

}
