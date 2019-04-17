package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkCorrectStatus;
import com.voxlearning.utopia.service.newhomework.api.mapper.PronunciationRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * @author majianxin
 */
@Setter
@Getter
public class JztStudentHomeworkReport extends JztHomeworkReport {
    private static final long serialVersionUID = -3616320505391983523L;

    /** 学生完成情况概览 */
    private String score;                                                   // 分数或等级
    private Integer substandardSyllableCount;                               // 未达标音节数
    private Integer errorQuestionCount;                                     // 错题数量
    private Long duration;                                                  // 作业用时
    private List<ObjectiveConfigTypeDetail> objectiveConfigTypeDetails = new LinkedList<>();     // 作业形式完成详情
    private boolean hasOcrMentalArithmetic;                                 // 是否包含纸质口算

    private CorrectDetail correctDetail;                                   // 订正情况

    /** 薄弱巩固模块 */
    private List<Map<String, Object>> readRecite;                          // 读背模块(语文)
    private SyllableModule syllableModule;                                 // 音节未达标(英语)

    private Long teacherId;
    private boolean expired;//是否过期
    private boolean checked;//是否检查
    private boolean showJumpBtn;
    @JsonIgnore
    private String bookId;

    @Setter
    @Getter
    @AllArgsConstructor
    public static class ObjectiveConfigTypeDetail implements Serializable {
        private static final long serialVersionUID = -6425858726658817804L;

        private String configType;
        private String configTypeDesc;
        private String scoreDesc;           // 描述(xx分 or A or 已完成)
        private ScoreStatus scoreStatus;    // 作业得分状态
    }

    @Setter
    @Getter
    public static class CorrectDetail implements Serializable {
        private static final long serialVersionUID = 1908658504408460530L;

        private String correctDesc;                                             // 订正描述
        private Collection<String> knowledgeList;                               // 订正知识点or作业形式列表
        private HomeworkCorrectStatus correctStatus;                            // 订正状态
    }

    /**
     * 说模块
     */
    @Setter
    @Getter
    public static class SyllableModule implements Serializable {
        private static final long serialVersionUID = 9054532400025491550L;

        private boolean allPc;//是否在全部pc
        private boolean hasVoice;//是否有音频
        private List<PronunciationRecord.Word> words;
        private int weakCount;//未达标数量
        private List<PronunciationRecord.Line> lines;
        private List<Map> unitAndSentenceList;
        private boolean voiceFlag;//是否需要查询语音数据
        private String bookId;
        private boolean hasPicListenContent;//是否在点读机有匹配教材
        private boolean showTask;//未达标数据是否匹配达到75%
        private boolean finishTask;//是否完成了点读机任务
        private boolean needPay;    //是否需要付费
        private boolean hasPay;  //是否已付费
    }
}
