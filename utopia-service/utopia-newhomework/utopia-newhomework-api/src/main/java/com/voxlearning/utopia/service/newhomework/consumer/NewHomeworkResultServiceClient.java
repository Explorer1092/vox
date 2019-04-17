package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultExtendedInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkResultService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.List;
import java.util.Map;

/**
 * @author tanguohong
 * @since 2017/1/16
 */
public class NewHomeworkResultServiceClient implements NewHomeworkResultService {
    @ImportService(interfaceClass = NewHomeworkResultService.class)
    private NewHomeworkResultService remoteReference;

    public void initNewHomeworkResult(NewHomework.Location location, Long userId){
        remoteReference.initNewHomeworkResult(location, userId);
    }

    @Override
    public Boolean updateNewHomeworkResultUrge(NewHomeworkResult newHomeworkResult) {
        return remoteReference.updateNewHomeworkResultUrge(newHomeworkResult);
    }

    public Boolean finishHomeworkBasicAppPractice(NewHomework newHomework, Long studentId, ObjectiveConfigType objectiveConfigType, String key, Double score, Long duration){
        return remoteReference.finishHomeworkBasicAppPractice(newHomework, studentId, objectiveConfigType, key, score, duration);
    }

    public Boolean finishCorrect(NewHomework newHomework, Long userId, ObjectiveConfigType type, Boolean finishCorrect, Boolean allFinishCorrect){
       return remoteReference.finishCorrect(newHomework, userId, type, finishCorrect, allFinishCorrect);
    }

    public Boolean saveNewHomeworkComment(NewHomework newHomework, Long userId, String comment, String audioComment){
        return remoteReference.saveNewHomeworkComment(newHomework, userId, comment,audioComment);
    }

    public Boolean saveHomeworkRewardIntegral(NewHomework.Location location, Long userId, Integer count){
        return remoteReference.saveHomeworkRewardIntegral(location, userId, count);
    }

    public Boolean saveFinishHomeworkReward(NewHomework newHomework, Long userId, Integer integral, Integer energy, Integer credit){
        return remoteReference.saveFinishHomeworkReward(newHomework, userId, integral, energy, credit);
    }

    public Boolean finishHomeworkBasicAppPractice(NewHomework.Location location , Long userId, ObjectiveConfigType objectiveConfigType, String key, Double avgScore, Long duration){
        return remoteReference.finishHomeworkBasicAppPractice(location , userId, objectiveConfigType, key, avgScore, duration);
    }

    public NewHomeworkResult doHomeworkBasicAppPractice(NewHomework.Location location , Long userId, ObjectiveConfigType objectiveConfigType, String key, NewHomeworkResultAppAnswer nhraa){
        return remoteReference.doHomeworkBasicAppPractice(location , userId, objectiveConfigType, key, nhraa);
    }

    public NewHomeworkResult doHomeworkExamAnswer(NewHomework.Location location , Long userId, ObjectiveConfigType objectiveConfigType, String qid, String processResultId){
        return remoteReference.doHomeworkExamAnswer(location, userId, objectiveConfigType, qid, processResultId);
    }

    public boolean finishHomeworkKeyPoint(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String videoId, Double score, Long duration){
        return remoteReference.finishHomeworkKeyPoint(location, userId, objectiveConfigType, videoId, score, duration);
    }

    public boolean finishHomeworkNewReadRecite(NewHomework.Location location,
                                               Long userId,
                                               ObjectiveConfigType objectiveConfigType,
                                               String questionBoxId,
                                               Double score,
                                               Long duration) {
        return remoteReference.finishHomeworkNewReadRecite(location, userId, objectiveConfigType, questionBoxId, score, duration);
    }

    public boolean finishHomeworkReadReciteWithScore(NewHomework.Location location,
                                                     Long userId,
                                                     ObjectiveConfigType objectiveConfigType,
                                                     String questionBoxId,
                                                     Double score,
                                                     Long duration,
                                                     Integer standardNum,
                                                     Integer appQuestionNum) {
        return remoteReference.finishHomeworkReadReciteWithScore(location, userId, objectiveConfigType, questionBoxId, score, duration, standardNum, appQuestionNum);
    }

    public boolean finishHomeworkWordTeachAndPractice(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String stoneDataId, Double score, Long duration,
                                                      Double wordExerciseScore,
                                                      Double actualImageTextRhymeScore) {
        return remoteReference.finishHomeworkWordTeachAndPractice(location, userId, objectiveConfigType, stoneDataId, score, duration, wordExerciseScore, actualImageTextRhymeScore);
    }

    public NewHomeworkResult finishHomework(NewHomework newHomework, Long userId, ObjectiveConfigType objectiveConfigType, Double score, Long duration, Boolean isPracticeFinished, Boolean isHomeworkFinished, Boolean isIncludeSubjective, Boolean isRepaired,
                                            List<String> ocrMentalAnswers, Integer ocrMentalQuestionCount, Integer ocrMentalCorrectQuestionCount,
                                            List<String> ocrDictationAnswers, Integer ocrDictationQuestionCount, Integer ocrDictationCorrectQuestionCount) {
        return remoteReference.finishHomework(newHomework, userId, objectiveConfigType, score, duration, isPracticeFinished, isHomeworkFinished, isIncludeSubjective, isRepaired, ocrMentalAnswers, ocrMentalQuestionCount, ocrMentalCorrectQuestionCount, ocrDictationAnswers, ocrDictationQuestionCount, ocrDictationCorrectQuestionCount);
    }

    public SubHomeworkResultExtendedInfo updateSubHomeworkResultExtendedInfo(String id, Map<String, String> info){
        return remoteReference.updateSubHomeworkResultExtendedInfo(id, info);
    }
}


