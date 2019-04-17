package com.voxlearning.utopia.agent.view.unifiedexam;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ExamControlView
 *
 * @author song.wang
 * @date 2018/9/17
 */
@Data
public class ExamControlView implements Serializable {
    private Long applyId;                                         // 申请ID

    private Long userId;
    private String userName;

    private Date unifiedExamBeginTime;                            // 考试开始时间
    private Date unifiedExamEndTime;                              //考试结束时间
    private Date correctingTestPaper;                              //批改试卷时间
    private Date achievementReleaseTime;                           // 成绩发布时间
    private Integer minSubmittedTestPaper;                         // 最短上交试卷时间 分钟
    private Integer oralLanguageFrequency;                         // 口语可答题次数
    private Integer maxSubmittedTestPaper;                         //最长上交试卷时间
    private Integer gradeType;                                       //分级制
    private String ranks;                                            //等级制内容是 ExamRank List 对象的 Json

    public static class UpdateRequestParamBuilder{

        public static Map<Object, Object> build(ExamControlView examControlView){
            Map<Object, Object> map = new HashMap<>();
            map.put("applyId", examControlView.getApplyId());
            map.put("agentId", examControlView.getUserId());
            map.put("agentName", examControlView.getUserName());

            map.put("examStartAt", examControlView.getUnifiedExamBeginTime().toString());
            map.put("examStopAt", examControlView.getUnifiedExamEndTime().toString());
            map.put("correctStopAt", examControlView.getCorrectingTestPaper().toString());
            map.put("resultIssueAt", examControlView.getAchievementReleaseTime().toString());
            map.put("submitAfterMinutes", examControlView.getMinSubmittedTestPaper());
            map.put("oralRepeatCount", examControlView.getOralLanguageFrequency());
            map.put("durationMinutes", examControlView.getMaxSubmittedTestPaper());

            map.put("gradeType", examControlView.getGradeType());
            map.put("ranks", examControlView.getRanks());
            return map;
        }
    }

}
