package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.QuestionWrongReason;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@DocumentConnection(configName = "mongo-journal")
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "homework_process_result_{}", dynamic = true)
@DocumentRangeable(range = DateRangeType.D)
public class JournalNewHomeworkProcessResult implements Serializable {

    private static final long serialVersionUID = -4064294234124299182L;

    @DocumentId
    private String id;
    private String processResultId;                     // 原表id
    @DocumentCreateTimestamp
    private Date createAt;                              // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                              // 修改时间
    private Long clazzId;                               // 班级ID
    private Long clazzGroupId;                          // 组ID
    public HomeworkTag homeworkTag;                     // 作业tag
    public NewHomeworkType type;                        // 作业类型
    private Integer clazzLevel;                         // 年级
    private String homeworkId;                          // 作业ID
    private StudyType studyType;                        // 阿分题/作业
    private String bookId;                              // 课本ID
    private String unitId;                              // 单元ID
    private String unitGroupId;                         // 课本的组ID
    private String lessonId;                            // 课程ID
    private String sectionId;                           // 课时ID
    private Long userId;                                // 学生ID
    private String questionId;                          // 试题ID
    private String questionDocId;                       // 试题DOC_ID
    private Long questionVersion;                       // 试题Version
    private Double standardScore;                       // 标准分保留四位小数
    private Double score;                               // 用户实际得分
    private Boolean grasp;                              // 是否掌握(全对/部分对)
    private List<List<Boolean>> subGrasp;               // 作答区域的掌握情况
    private List<List<String>> userAnswers;             // 用户答案
    private List<List<Integer>> scorePercent;           // 内容库的分值比例（后算分的话用这个可以避免进位问题，预留，xuesong.zhang 20160704）
    private Long duration;                              // 完成时长（单位：毫秒）
    private SchoolLevel schoolLevel;
    private Subject subject;                            // 学科
    private ObjectiveConfigType objectiveConfigType;    // 作业形式 ObjectiveConfigType
    private String clientType;                          // 客户端类型:pc,mobile
    private String clientName;                          // 客户端名称:***app
    private Map<String, String> additions;              // 扩展属性
    private Boolean needCorrect;                        // 是否需要批改，true需要，false/null不需要
    private List<List<NewHomeworkQuestionFile>> files;  // 主观作答类型试题的存储
    private Boolean review;                             // 已阅true，未阅false
    private String teacherMark;                         // 评语
    private Boolean repair;                             // 重做标识

    // 阿分题特有属性
    private String algoW;                              // 算法权重
    private String algoV;                              // 算法版本
    private String clientId;                           // 当前用户id，可能是学生也可能是家长
    private String questionPattern;                    // 阿分题语文题型：character("生字"),word("词语"),reading("阅读"),apply("运用"),unknown("未知")

    // 基础应用类特有属性 begin
    private Integer categoryId;                 // 应用类型ID
    private Long practiceId;                    // 应用ID

    // 绘本特有属性
    public String pictureBookId;                // 绘本id

    //语音类特有属性
    private VoiceEngineType voiceEngineType;    // 语音引擎
    private String voiceCoefficient;            // 打分系数
    private String voiceScoringMode;            // 打分模式
    public String vest; // voiceEngineScoreType语音引擎分出区间类型 20150523加的
    private Integer st;      // sentenceType基础应用才有,20150523加的。同com.voxlearning.utopia.service.content.api.entity.Sentence.type
    //口语详情
    private List<List<NewHomeworkProcessResult.OralDetail>> oralDetails;   // 应用跟读题、口语题详情

    private String sourceQuestion;              // 原题id
    private QuestionWrongReason wrongReason;    // 错题原因

    private AppOralScoreLevel appOralScoreLevel; //应用跟读口语分数等级
    private Double actualScore;                  // 跟读类应用引擎的实际得分

    // 数学视频作业属性
    private String videoId;                     // 视频id

    // 新课文读背作业属性
    private String questionBoxId;              // 题包id 新课文读背作业必填属性

    private QuestionBoxType questionBoxType;  // 题包类型 新课文读背作业必填属性
    // 趣味配音作业属性
    private String dubbingId;                   // 配音id
    // 纸质口算作业专用属性
    public OcrMentalImageDetail ocrMentalImageDetail;
    // 英语-纸质听写专用
    public OcrMentalImageDetail ocrDictationImageDetail;

    //口语交际 : 情景包id / 字词讲练
    private String stoneId;
    //口语交际 : 对话id
    private String dialogId;
    //口语交际 : 类型
    private OralCommunicationContentType stoneType;
    //口语交际 : 人机交互，用于主题角色的保存
    private String roleTopicId;

    //字词讲练：模块类型
    private WordTeachModuleType wordTeachModuleType;
    private String env;                         // 此字段在NewHomeworkQueueServiceImpl中上报前赋的值
}
