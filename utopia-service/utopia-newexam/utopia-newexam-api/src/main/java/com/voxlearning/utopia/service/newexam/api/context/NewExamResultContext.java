/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newexam.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamQuestionFile;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/9.
 */
@Getter
@Setter
public class NewExamResultContext extends AbstractContext<NewExamResultContext> {

    private static final long serialVersionUID = -7458759708407629313L;

    // in
    private Long userId; // 用户ID
    private User user; // 用户
    private String newExamId; // 考试ID
    private String paperId; // 试卷ID
    private String partId; // 模块ID
    private String questionId; // 题ID
    private String questionDocId; // 题DocID
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String ipImei; // ip or imei
    private String userAgent; // userAgent
    private List<List<String>> answer;  // 用户答案
    private Long durationMilliseconds; // 完成时长
    private List<List<String>> fileUrls; // 文件地址 用于有作答过程的试题
    private StudyType learningType; // 学习类型
    //口语部分
    private List<List<NewExamProcessResult.OralDetail>> oralScoreDetails; //口语题详情


    // middle
    private Long clazzId; // 班级ID
    private Clazz clazz; // 班级
    private Long clazzGroupId; // 班组ID
    private NewExam newExam; // 模拟考试
    private Subject subject; // 学科
    private Double standardScore; // 这道题目的标准分
    private NewPaper newPaper; // 题目信息
    private Map<String, Double> questionScoreMap; // 题目信息
    private QuestionScoreResult scoreResult; // 算分结果
    private List<List<String>> standardAnswer = new ArrayList<>(); // 标准答案
    private List<List<Boolean>> subGrasp = new ArrayList<>(); // 作答区域的掌握情况
    private List<Double> subScore = new ArrayList<>(); // 作答区域的得分情况
    private List<List<NewExamQuestionFile>> files = new ArrayList<>(); // 主观题文件信息
    private NewExamResult newExamResult; // 考试中间结果
    private NewExamProcessResult oldProcessResult; // 原来考试结果
    private NewExamProcessResult currentProcessResult; // 当前考试结果

    private boolean newExamFinished = false; // 当前考试是否全部完成
    private Double totalScore;  // 分数
    private Long totalDureation; // 消耗时长
    private double intervalScore = 0.5;
    private int oralGradeType;//0向上；1四舍五入

    // out
    private Map<String, Map<String, Object>> result = new HashMap<>();

    // 口语题计算分值
    public List<Double> calculateStudentOralScore() {
        double stdScore = SafeConverter.toDouble(standardScore);
        Double subStdScore = new BigDecimal(stdScore).divide(new BigDecimal(oralScoreDetails.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

        List<Double> scoreResult = new ArrayList<>();

        for (List<NewExamProcessResult.OralDetail> oralDetails : oralScoreDetails) {
            int macScore = 0;
            int oralScoreDetailsSize = 0;
            for (NewExamProcessResult.OralDetail oralDetail : oralDetails) {
                if (oralDetail != null && oralDetail.getMacScore() != null) {
                    macScore += oralDetail.getMacScore();
                    oralScoreDetailsSize++;
                }
            }
            BigDecimal avgMacScore = new BigDecimal(0);
            if (macScore != 0 && oralScoreDetailsSize != 0) {
                avgMacScore = new BigDecimal(macScore).divide(new BigDecimal(oralScoreDetailsSize), 0, BigDecimal.ROUND_HALF_UP);
            }

            BigDecimal bigDecimal = new BigDecimal(subStdScore);

            bigDecimal = bigDecimal.multiply(avgMacScore);
            double sysScore = bigDecimal.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            //0四舍五入算法
            //1向上取整算法
            sysScore = new BigDecimal(sysScore).divide(new BigDecimal(intervalScore), 0, oralGradeType == 0 ? BigDecimal.ROUND_HALF_UP : BigDecimal.ROUND_UP).intValue() * intervalScore;
            //意外情况：当题库不给跨度分，或者跨度分有问题不可整除
            //意外情况：产品要求继续考试
            if (sysScore - subStdScore > 0) {
                sysScore = subStdScore;
            }
            sysScore = new BigDecimal(sysScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            scoreResult.add(sysScore);
        }
        return scoreResult;
    }
}
