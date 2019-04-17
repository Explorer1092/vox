package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultExtendedInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by tanguohong on 2017/1/16.
 */
@ServiceVersion(version = "20190125")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface NewHomeworkResultService extends IPingable {

    void initNewHomeworkResult(NewHomework.Location location, Long userId);

    Boolean updateNewHomeworkResultUrge(NewHomeworkResult newHomeworkResult);

    Boolean finishHomeworkBasicAppPractice(NewHomework newHomework, Long studentId, ObjectiveConfigType objectiveConfigType, String key, Double score, Long duration);

    Boolean finishCorrect(NewHomework newHomework, Long userId, ObjectiveConfigType type, Boolean finishCorrect, Boolean allFinishCorrect);

    Boolean saveNewHomeworkComment(NewHomework newHomework, Long userId, String comment, String audioComment);

    Boolean saveHomeworkRewardIntegral(NewHomework.Location location, Long userId, Integer count);

    Boolean saveFinishHomeworkReward(NewHomework newHomework, Long userId, Integer integral, Integer energy, Integer credit);

    Boolean finishHomeworkBasicAppPractice(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String key, Double avgScore, Long duration);

    NewHomeworkResult doHomeworkBasicAppPractice(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String key, NewHomeworkResultAppAnswer nhraa);

    NewHomeworkResult doHomeworkExamAnswer(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String qid, String processResultId);

    boolean finishHomeworkKeyPoint(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String videoId, Double score, Long duration);

    boolean finishHomeworkNewReadRecite(NewHomework.Location location,
                                        Long userId,
                                        ObjectiveConfigType objectiveConfigType,
                                        String questionBoxId,
                                        Double score,
                                        Long duration);

    boolean finishHomeworkReadReciteWithScore(NewHomework.Location location,
                                              Long userId,
                                              ObjectiveConfigType objectiveConfigType,
                                              String questionBoxId,
                                              Double score,
                                              Long duration,
                                              Integer standardNum,
                                              Integer appQuestionNum);
    boolean finishHomeworkWordTeachAndPractice(NewHomework.Location location,
                                              Long userId,
                                              ObjectiveConfigType objectiveConfigType,
                                              String stoneDataId,
                                              Double score,
                                              Long duration,
                                               Double wordExerciseScore,
                                               Double actualImageTextRhymeScore);

    NewHomeworkResult finishHomework(NewHomework newHomework, Long userId, ObjectiveConfigType objectiveConfigType, Double score, Long dureation, Boolean isPracticeFinished, Boolean isHomeworkFinished, Boolean isIncludeSubjective, Boolean isRepaired,
                                     List<String> ocrMentalAnswers, Integer ocrMentalQuestionCount, Integer ocrMentalCorrectQuestionCount,
                                     List<String> ocrDictationAnswers, Integer ocrDictationQuestionCount, Integer ocrDictationCorrectQuestionCount
    );

    SubHomeworkResultExtendedInfo updateSubHomeworkResultExtendedInfo(String id, Map<String, String> info);
}
