package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.NaturalSpellingSentence;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.OralDiagnoseResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/11/25
 */
@Getter
@Setter
public class BaseHomeworkProcessResult implements Serializable {

    private static final long serialVersionUID = 442574399078934394L;

    public HomeworkTag homeworkTag;                    // 作业tag
    public NewHomeworkType type;                       // 作业类型
    public Long clazzGroupId;                          // 组ID
    public String homeworkId;                          // 作业ID
    public String bookId;                              // 课本ID
    public String unitId;                              // 单元ID
    public String unitGroupId;                         // 课本的组ID
    public String lessonId;                            // 课程ID
    public String sectionId;                           // 课时ID
    public Long userId;                                // 学生ID
    public String courseId;                            // 课程ID(字词讲练:汉字文化模块) || 巩固学习课程ID
    public String questionId;                          // 题ID
    public String questionDocId;                       // 试题DOC_ID
    public Long questionVersion;                       // 试题Version
    public Double standardScore;                       // 标准分保留四位小数
    public Double score;                               // 用户得分（跟读类应用是根据引擎处理后的分数）
    public Double actualScore;                         // 跟读类应用引擎的实际得分
    public Boolean grasp;                              // 是否掌握(全对/部分对)
    public List<List<Boolean>> subGrasp;               // 作答区域的掌握情况
    public List<List<String>> userAnswers;             // 用户答案
    public List<List<Integer>> scorePercent;           // 内容库的分值比例（后算分的话用这个可以避免进位问题，预留，xuesong.zhang 20160704）
    public List<Double> subScore;
    public Long duration;                              // 完成时长（单位：毫秒）
    public SchoolLevel schoolLevel;
    public Subject subject;                            // 学科
    public ObjectiveConfigType objectiveConfigType;    // 作业形式 ObjectiveConfigType
    public String clientType;                          // 客户端类型:pc,mobile
    public String clientName;                          // 客户端名称:***app
    public Map<String, String> additions;              // 扩展属性
    public Boolean needCorrect;                        // 是否需要批改，true需要，false/null不需要
    public List<List<NewHomeworkQuestionFile>> files;  // 主观作答类型试题的存储
    public Boolean review;                             // 已阅true，未阅false
    public CorrectType correctType;                    // 批改类型，实际分数，还是优良中差类的，或者是ABCD，如果说是实际分数，则取score的值，其他的取correction
    public Correction correction;                      // 批改信息
    public String teacherMark;                         // 评语

    // 基础应用类特有属性 begin
    public Integer categoryId;                          // 应用类型ID
    public Long practiceId;                             // 应用ID

    // 绘本特有属性
    public String pictureBookId;                        // 绘本id

    //语音类特有属性
    public VoiceEngineType voiceEngineType;             // 语音引擎
    public String voiceCoefficient;                     // 打分系数，默认1.6
    public String voiceMode;                            // 打分模式，请看http://wiki.17zuoye.net/pages/viewpage.action?pageId=22749531
    // voiceScoringMode = 'Normal'; //正常
    // voiceScoringMode = 'SkipByUser_NetworkError'; //由于网络错误，用户主动要求跳过麦克风
    // voiceScoringMode = 'SkipByUser_DeviceError'; //由于设备错误，用户主动要求跳过麦克风
    // voiceScoringMode = 'SkipByUser_LowScore';//由于读音打分过低，用户主动要求跳过麦克风
    // voiceScoringMode = 'SkipByUser_OtherError'; //由于其他错误，用户主动要求跳过麦克风
    // voiceScoringMode = 'ListenOnly'; //只听句
    // voiceScoringMode = ''; //由于其他错误，用户主动要求跳过麦克风
    public String voiceScoringMode;
    public String vest; // voiceEngineScoreType语音引擎分出区间类型 20150523加的
    public Integer st;      // sentenceType基础应用才有,20150523加的。同com.voxlearning.utopia.service.content.api.entity.Sentence.type
    //口语详情
    public List<List<BaseHomeworkProcessResult.OralDetail>> oralDetails;    // 应用跟读题、口语题详情
    public AppOralScoreLevel appOralScoreLevel;                             // 应用跟读口语分数等级
    public String oralAddition;                                             // json结构的口语专用扩展字段，目前仅用于绕口令

    // 数学视频作业属性
    public String videoId;                      // 视频id
    // 新课文读背作业属性
    public String questionBoxId;                // 题包id 新课文读背作业必填属性
    public QuestionBoxType questionBoxType;     // 题包类型 新课文读背作业必填属性
    // 趣味配音作业属性
    public String dubbingId;                    // 配音id
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
    /**
     * 应用跟读题和口语题详情数据结构
     */
    @Getter
    @Setter
    @EqualsAndHashCode(of = {"audio", "macScore", "fluency", "integrity", "pronunciation"})
    public static class OralDetail implements Serializable {
        private static final long serialVersionUID = 5249999186044689434L;
        @DocumentField("audio")
        private String audio;               // 音频地址
        @DocumentField("macScore")
        private Integer macScore;           // 引擎分
        @DocumentField("fluency")
        private Integer fluency;            // 流利程度
        @DocumentField("integrity")
        private Integer integrity;          // 完整度
        @DocumentField("pronunciation")
        private Integer pronunciation;      // 发音准确度
        @DocumentField("oralScore")
        private Integer oralScore;          // 口语分数
        @DocumentField("oralScoreInterval")
        private String oralScoreInterval;   // 口语分数等级
        //以下是8分制引擎新加的
        @DocumentField("businessLevel")
        private Float businessLevel;      // 打分系数
        @DocumentField("standardScore")
        private Integer standardScore;      // 8分制成绩

        //口语交际新加的
        @DocumentField("keyStandardScore")
        private Double keyStandardScore;    //各组关键词均值
        //口语交际新加的
        @DocumentField("is_has_key_words")
        private Boolean isHasKeyWords;  //是否有keywords
        //口语交际新加的
        @DocumentField("star")
        private Integer star;  //星级
        //口语交际新加的
        @DocumentField("duration")
        private Long duration;//录音音频时长

        @DocumentField("oralDiagnoseResult")
        private OralDiagnoseResult oralDiagnoseResult;  // 口语诊断结果

        @DocumentField("sentences")
        private List<NaturalSpellingSentence> sentences;    // 引擎打分结果
    }


    public List<NaturalSpellingSentence> findAllSentence() {
        if (CollectionUtils.isEmpty(oralDetails)) {
            return Collections.emptyList();
        }
        return oralDetails.stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(o -> CollectionUtils.isNotEmpty(o.getSentences()))
                .map(OralDetail::getSentences)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 是否包含录音文件
     */
    public boolean hasRecord() {
        return "Normal".equalsIgnoreCase(voiceScoringMode);
    }

    public List<NewHomeworkQuestionFile> findAllFiles() {
        if (getFiles() != null && (!getFiles().isEmpty())) {
            return getFiles().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    // 分数高的排在前面
    public static final Comparator<BaseHomeworkProcessResult> SORT_BY_SCORE_ASC = (o1, o2) -> {
        double s1 = o1.score == null ? 0 : o1.score;
        double s2 = o2.score == null ? 0 : o2.score;
        return Double.compare(s1, s2);
    };

    /**
     * 是否命中干预
     * @return
     */
    @JsonIgnore
    public Boolean isIntervention(){
        return MapUtils.isNotEmpty(additions) && additions.containsKey(NewHomeworkConstants.HINT_ID);
    }

    /**
     * 是否命中干预(不包含复合题)
     * @return
     */
    @JsonIgnore
    public Boolean isInterventionExcludeCompositeQuestion(){
        if (MapUtils.isNotEmpty(additions) && additions.containsKey(NewHomeworkConstants.HINT_ID)) {
            if (userAnswers != null && userAnswers.size() > 1) {
                return false;// fixme 复合题即时干预暂时不做报告展示, 需要展示的时候去掉这层if判断即可
            }
            return true;
        }
        return false;
    }

    @JsonIgnore
    public ImmediateInterventionType getImmediateInterventionType(){
        if (MapUtils.isNotEmpty(additions) && additions.containsKey(NewHomeworkConstants.HINT_ID)) {
            return ImmediateInterventionType.create(SafeConverter.toInt(additions.get(NewHomeworkConstants.HINT_ID)));
        }
        return null;
    }

    @JsonIgnore
    public String getInterventionStringAnswer() {
        if (MapUtils.isNotEmpty(additions) && additions.containsKey(NewHomeworkConstants.INTERVENTION_ANSWER)) {
            return additions.get(NewHomeworkConstants.INTERVENTION_ANSWER);
        }
        return null;
    }

    @JsonIgnore
    public String getInterventionHintId() {
        if (MapUtils.isNotEmpty(additions) && additions.containsKey(NewHomeworkConstants.HINT_ID)) {
            return additions.get(NewHomeworkConstants.HINT_ID);
        }
        return null;
    }

}
