/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author zhangbin
 * @since 2017/7/3 14:43
 */

@Named
public class NewHomeworkTypeResultProcessExam extends NewHomeworkTypeResultProcessTemplate {
    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.EXAM;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        //此处newhomework应该从前面传过来好些，因为前面已经查过一次了，不过还好有缓存，先紧急处理下，待优化...
        NewHomework newHomework = newHomeworkLoader.load(baseHomeworkResult.homeworkId);
        List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkQuestions(objectiveConfigType);
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        Collection<String> processResultIds = new ArrayList<>();
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        if (MapUtils.isEmpty(resultAnswer.getAnswers())) {
            logger.error("processHomeworkTypeResult EXAM error, homeworkId:{}, studentId:{}",
                    baseHomeworkResult.getHomeworkId(),
                    baseHomeworkResult.getUserId());
        } else {
            processResultIds = resultAnswer.getAnswers().values();
        }
        Map<String, NewHomeworkProcessResult> nhprMap = newHomeworkProcessResultLoader.loads(
                baseHomeworkResult.getHomeworkId(),
                processResultIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int rightCount = 0;
        int errorCount = 0;
        LinkedHashMap<String, String> answerMap = resultAnswer.getAnswers();

        Map<String, NewQuestion> allQuestionMap = new HashMap<>();
        if (ObjectiveConfigType.ONLINE_DICTATION.equals(objectiveConfigType)) {
           allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(answerMap.keySet());
        }

        //按照布置作业的题的顺序，返回答案。
        for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
            if (MapUtils.isNotEmpty(answerMap) && MapUtils.isNotEmpty(nhprMap)) {
                NewHomeworkProcessResult nhpr = nhprMap.get(answerMap.get(newHomeworkQuestion.getQuestionId()));
                if (nhpr != null) {
                    if (SafeConverter.toBoolean(nhpr.getGrasp())) {
                        rightCount++;
                    } else {
                        errorCount++;
                    }
                    Boolean finished = true;
                    // 有一个答案为空则表示该题未完成
                    List<List<String>> userAnswers = nhpr.getUserAnswers();
                    if (CollectionUtils.isEmpty(userAnswers)) {
                        finished = false;
                    } else {
                        for (List<String> list : userAnswers) {
                            if (!finished) {
                                break;
                            }
                            if (CollectionUtils.isEmpty(list)) {
                                finished = false;
                                break;
                            } else {
                                for (String answer : list) {
                                    if (StringUtils.isBlank(answer)) {
                                        finished = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    results.add(MapUtils.m(
                            "questionId", nhpr.getQuestionId(),
                            "score", new BigDecimal(SafeConverter.toDouble(nhpr.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                            "userAnswers", nhpr.getUserAnswers(),
                            "questionAnswers", allQuestionMap.get(nhpr.getQuestionId()) != null ? allQuestionMap.get(nhpr.getQuestionId()).getContent().getSubContents().get(0).getAnswers().get(0).getAnswer() : "",
                            "right", SafeConverter.toBoolean(nhpr.getGrasp()),
                            "intervention",  nhpr.isInterventionExcludeCompositeQuestion(),//是否命中干预
                            "finished", finished));
                }
            }
        }

        MapMessage mapMessage = MapMessage.successMessage()
                .add("datas", results)
                .add("rightCount", rightCount)
                .add("errorCount", errorCount)
                .add("duration", resultAnswer.getDuration());
        String durationStr = "";    // 用时
        Integer timeLimit = 0;      // 限时
        Boolean mentalAward = false;// 奖励
        if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(objectiveConfigType)) {
            // 用时
            Long duration = 0L;
            if (resultAnswer.getDuration() != null) {
                duration = SafeConverter.toLong(resultAnswer.processDuration());
            }
            if (duration > 0) {
                long minute = duration / 60;
                long second = duration % 60;
                if (minute == 0) {
                    durationStr = second + "\"";
                } else {
                    durationStr = minute + "'" + second + "\"";
                }
            }

            // 是否限时及奖励
            List<NewHomeworkPracticeContent> practices = newHomework.getPractices();
            if (CollectionUtils.isNotEmpty(practices)) {
                for (NewHomeworkPracticeContent practiceContent : practices) {
                    if (practiceContent.getType().equals(objectiveConfigType)) {
                        timeLimit = practiceContent.getTimeLimit() != null ? practiceContent.getTimeLimit().getTime() : 0;
                        mentalAward = practiceContent.getMentalAward() != null ? practiceContent.getMentalAward() : false;
                    }
                }
            }
            mapMessage.add("durationStr", durationStr)
                    .add("score", new BigDecimal(SafeConverter.toDouble(resultAnswer.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue())
                    .add("credit", baseHomeworkResult.getCredit())
                    .add("timeLimit", timeLimit)
                    .add("mentalAward", mentalAward);
        }
        return mapMessage;
    }
}
