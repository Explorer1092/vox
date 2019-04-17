package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkShardMongoHelper;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/1/12
 */
@Named
public class SubHomeworkResultDao {
    @Inject private SubHomeworkResultAsyncDao subHomeworkResultAsyncDao;
    @Inject private SubHomeworkResultShardDao subHomeworkResultShardDao;


    public SubHomeworkResult load(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        if (NewHomeworkShardMongoHelper.isShardHomeworkResultId(id)) {
            return subHomeworkResultShardDao.load(id);
        } else {
            return subHomeworkResultAsyncDao.load(id);
        }
    }

    public Map<String, SubHomeworkResult> loads(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Map<String, SubHomeworkResult> subHomeworkResultMap = new HashMap<>();
        Set<String> shardIds = new HashSet<>();
        Set<String> asyncIds = new HashSet<>();
        for (String id : ids) {
            if (NewHomeworkShardMongoHelper.isShardHomeworkResultId(id)) {
                shardIds.add(id);
            } else {
                asyncIds.add(id);
            }
        }
        if (CollectionUtils.isNotEmpty(shardIds)) {
            Map<String, SubHomeworkResult> subHomeworkResultShardMap = subHomeworkResultShardDao.loads(shardIds);
            subHomeworkResultShardMap.forEach(subHomeworkResultMap::put);
        }
        if (CollectionUtils.isNotEmpty(asyncIds)) {
            Map<String, SubHomeworkResult> subHomeworkResultAsyncMap = subHomeworkResultAsyncDao.loads(asyncIds);
            subHomeworkResultAsyncMap.forEach(subHomeworkResultMap::put);
        }
        return subHomeworkResultMap;
    }

    public SubHomeworkResult upsert(SubHomeworkResult subHomeworkResult) {
        if (NewHomeworkShardMongoHelper.isShardHomeworkResultId(subHomeworkResult.getId())) {
            return subHomeworkResultShardDao.upsert(subHomeworkResult);
        } else {
            return subHomeworkResultAsyncDao.upsert(subHomeworkResult);
        }
    }

    public SubHomeworkResult updateNewHomeworkResultUrge(SubHomeworkResult subHomeworkResult) {
        if (subHomeworkResult == null || !subHomeworkResult.isFinished()) {
            return null;
        }
        if (NewHomeworkShardMongoHelper.isShardHomeworkResultId(subHomeworkResult.getId())) {
            return subHomeworkResultShardDao.updateNewHomeworkResultUrge(subHomeworkResult);
        } else {
            return subHomeworkResultAsyncDao.updateNewHomeworkResultUrge(subHomeworkResult);
        }
    }


    /**
     * 初始化作业结果中间表
     *
     * @param location 作业的location
     * @param userId   用户ID
     */
    public SubHomeworkResult initSubHomeworkResult(SubHomework.Location location, Long userId) {
        if (location == null || userId == null) {
            return null;
        }
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.initSubHomeworkResult(location, userId);
        } else {
            return subHomeworkResultAsyncDao.initSubHomeworkResult(location, userId);
        }
    }

    /**
     * 做作业的写入操作，用于绘本
     *
     * @param type                        作业形式
     * @param key                         categoryId+lessonId
     * @param baseHomeworkResultAppAnswer 做题明细
     * @return SubHomeworkResult
     */
    public SubHomeworkResult doHomeworkBasicApp(SubHomework.Location location,
                                                Long userId,
                                                ObjectiveConfigType type,
                                                String key,
                                                BaseHomeworkResultAppAnswer baseHomeworkResultAppAnswer) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.doHomeworkBasicApp(location, userId, type, key, baseHomeworkResultAppAnswer);
        } else {
            return subHomeworkResultAsyncDao.doHomeworkBasicApp(location, userId, type, key, baseHomeworkResultAppAnswer);
        }
    }

    /**
     * 基础训练某一个类型完成
     *
     * @param location 作业
     * @param userId   用户id
     * @param type     作业类型
     * @param key      基础训练（categoryId+lessonId）
     * @param score    分数
     * @param duration 时长
     * @return 是否成功
     */
    public SubHomeworkResult finishHomeworkBasicAppPractice(SubHomework.Location location,
                                                            Long userId,
                                                            ObjectiveConfigType type,
                                                            String key,
                                                            Double score,
                                                            Long duration) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.finishHomeworkBasicAppPractice(location, userId, type, key, score, duration);
        } else {
            return subHomeworkResultAsyncDao.finishHomeworkBasicAppPractice(location, userId, type, key, score, duration);
        }
    }

    /**
     * 重难点专项某一个视频绘本完成
     *
     * @param location 作业
     * @param userId   用户id
     * @param type     作业类型
     * @param videoId  //视频绘本ID
     * @param score    分数
     * @param duration 时长
     * @return 是否成功
     */
    public SubHomeworkResult finishHomeworkKeyPoint(SubHomework.Location location,
                                                    Long userId,
                                                    ObjectiveConfigType type,
                                                    String videoId,
                                                    Double score,
                                                    Long duration) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.finishHomeworkKeyPoint(location, userId, type, videoId, score, duration);
        } else {
            return subHomeworkResultAsyncDao.finishHomeworkKeyPoint(location, userId, type, videoId, score, duration);
        }
    }

    /**
     * 语文读背练习题的一个题包完成
     *
     * @param location      作业
     * @param userId        用户id
     * @param type          作业类型
     * @param questionBoxId //语文读背练习题包ID
     * @param duration      时长
     * @return 是否成功
     */
    public SubHomeworkResult finishHomeworkNewReadRecite(SubHomework.Location location,
                                                         Long userId,
                                                         ObjectiveConfigType type,
                                                         String questionBoxId,
                                                         Double score,
                                                         Long duration) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.finishHomeworkNewReadRecite(location, userId, type, questionBoxId, score, duration);
        } else {
            return subHomeworkResultAsyncDao.finishHomeworkNewReadRecite(location, userId, type, questionBoxId, score, duration);
        }
    }

    /**
     * 语文读背(打分)题的一个题包完成
     *
     * @param location      作业
     * @param userId        用户id
     * @param type          作业类型
     * @param questionBoxId 题包ID
     * @param duration      时长
     * @return 是否成功
     */
    public SubHomeworkResult finishHomeworkReadReciteWithScore(SubHomework.Location location,
                                                               Long userId,
                                                               ObjectiveConfigType type,
                                                               String questionBoxId,
                                                               Double score,
                                                               Long duration,
                                                               Integer standardNum,
                                                               Integer appQuestionNum) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.finishHomeworkReadReciteWithScore(location, userId, type, questionBoxId, score, duration, standardNum, appQuestionNum);
        } else {
            return subHomeworkResultAsyncDao.finishHomeworkReadReciteWithScore(location, userId, type, questionBoxId, score, duration, standardNum, appQuestionNum);
        }
    }

    /**
     * 字词讲练题的一个题包完成
     */
    public SubHomeworkResult finishHomeworkWordTeachAndPractice(SubHomework.Location location,
                                                                Long userId,
                                                                ObjectiveConfigType type,
                                                                String stoneId,
                                                                Double score,
                                                                Long duration,
                                                                Double wordExerciseScore,
                                                                Double finalImageTextRhymeScore) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.finishHomeworkWordTeachAndPractice(location, userId, type, stoneId, score, duration, wordExerciseScore, finalImageTextRhymeScore);
        } else {
            return subHomeworkResultAsyncDao.finishHomeworkWordTeachAndPractice(location, userId, type, stoneId, score, duration, wordExerciseScore, finalImageTextRhymeScore);
        }
    }

    /**
     * 作业完成
     *
     * @param location      作业location
     * @param userId        用户id
     * @param type          作业形式
     * @param score         本次作业形式的总分
     * @param duration      本次作业形式的总用时
     * @param isFinish      本次作业形式是否完成
     * @param isFinishAll   所有作业完成
     * @param finishCorrect 是否批改完
     * @param isRepaired    是否是补做
     * @return SubHomeworkResult
     */
    public SubHomeworkResult finishHomework(SubHomework.Location location,
                                            Long userId,
                                            ObjectiveConfigType type,
                                            Double score,
                                            Long duration,
                                            Boolean isFinish,
                                            Boolean isFinishAll,
                                            Boolean finishCorrect,
                                            Boolean isRepaired,
                                            SchoolLevel schoolLevel,
                                            List<String> ocrMentalAnswers,
                                            Integer ocrMentalQuestionCount,
                                            Integer ocrMentalCorrectQuestionCount,
                                            List<String> ocrDictationAnswers,
                                            Integer ocrDictationQuestionCount,
                                            Integer ocrDictationCorrectQuestionCount) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.finishHomework(location, userId, type, score, duration, isFinish, isFinishAll, finishCorrect, isRepaired, schoolLevel, ocrMentalAnswers, ocrMentalQuestionCount, ocrMentalCorrectQuestionCount, ocrDictationAnswers, ocrDictationQuestionCount, ocrDictationCorrectQuestionCount);
        } else {
            return subHomeworkResultAsyncDao.finishHomework(location, userId, type, score, duration, isFinish, isFinishAll, finishCorrect, isRepaired, schoolLevel, ocrMentalAnswers, ocrMentalQuestionCount, ocrMentalCorrectQuestionCount, ocrDictationAnswers, ocrDictationQuestionCount, ocrDictationCorrectQuestionCount);
        }
    }

    public Set<SubHomeworkResult.Location> findByHomework(String day, Subject subject, String homeworkId, List<Long> groupStudentIds) {
        if (NewHomeworkUtils.isShardHomework(homeworkId)) {
            return subHomeworkResultShardDao.findByHomework(day, subject, homeworkId, groupStudentIds);
        } else {
            return subHomeworkResultAsyncDao.findByHomework(day, subject, homeworkId);
        }
    }

    /**
     * 奖励学豆
     *
     * @param location       作业location
     * @param userId         用户id
     * @param rewardIntegral 奖励学豆
     * @return boolean
     */
    public SubHomeworkResult saveHomeworkRewardIntegral(SubHomework.Location location, Long userId, Integer rewardIntegral) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.saveHomeworkRewardIntegral(location, userId, rewardIntegral);
        } else {
            return subHomeworkResultAsyncDao.saveHomeworkRewardIntegral(location, userId, rewardIntegral);
        }
    }

    /**
     * 完成作业奖励学豆、能量、学分
     *
     * @param location 作业location
     * @param userId   用户id
     * @param integral 奖励学豆
     * @param energy   奖励能量
     * @param credit   奖励学分
     * @return boolean
     */
    public SubHomeworkResult saveFinishHomeworkReward(SubHomework.Location location, Long userId, Integer integral, Integer energy, Integer credit) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.saveFinishHomeworkReward(location, userId, integral, energy, credit);
        } else {
            return subHomeworkResultAsyncDao.saveFinishHomeworkReward(location, userId, integral, energy, credit);
        }
    }

    /**
     * 保存老师对此作业的评语
     *
     * @param location 作业location
     * @param userId   用户id
     * @param comment  评语
     * @return boolean
     */
    public SubHomeworkResult saveSubHomeworkComment(SubHomework.Location location, Long userId, String comment, String audioComment) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.saveSubHomeworkComment(location, userId, comment, audioComment);
        } else {
            return subHomeworkResultAsyncDao.saveSubHomeworkComment(location, userId, comment, audioComment);
        }
    }

    public SubHomeworkResult finishCorrectToApp(String homeworkId,
                                                String id,
                                                ObjectiveConfigType type,
                                                String appId,
                                                Boolean review,
                                                CorrectType correctType,
                                                Correction correction,
                                                String teacherMark,
                                                Boolean isBatch) {
        if (NewHomeworkUtils.isShardHomework(homeworkId)) {
            return subHomeworkResultShardDao.finishCorrectToApp(homeworkId, id, type, appId, review, correctType, correction, teacherMark, isBatch);
        } else {
            return subHomeworkResultAsyncDao.finishCorrectToApp(homeworkId, id, type, appId, review, correctType, correction, teacherMark, isBatch);
        }
    }


    /**
     * 完成某种作业形式的批改或者完成所有作业的批改
     *
     * @param location         作业
     * @param userId           用户id
     * @param type             作业类型
     * @param finishCorrect    某种作业形式的完成
     * @param allFinishCorrect 所有作业完成
     * @return boolean
     */
    public SubHomeworkResult finishCorrect(SubHomework.Location location,
                                           Long userId,
                                           ObjectiveConfigType type,
                                           Boolean finishCorrect,
                                           Boolean allFinishCorrect) {
        if (NewHomeworkUtils.isShardHomework(location.getId())) {
            return subHomeworkResultShardDao.finishCorrect(location, userId, type, finishCorrect, allFinishCorrect);
        } else {
            return subHomeworkResultAsyncDao.finishCorrect(location, userId, type, finishCorrect, allFinishCorrect);
        }
    }
}
