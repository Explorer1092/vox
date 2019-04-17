package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Maps;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.DocumentAccessException;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkCorrectStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultExtendedInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkResultService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.HomeworkSelfStudyRefDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyAccomplishmentDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkReportDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultAnswerDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultExtendedInfoDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.HomeworkHBaseQueueServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkTransform;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.*;

/**
 * @author tanguohong
 * @since 2017/1/16
 */
@Named
@Service(interfaceClass = NewHomeworkResultService.class)
@ExposeService(interfaceClass = NewHomeworkResultService.class)
public class NewHomeworkResultServiceImpl extends SpringContainerSupport implements NewHomeworkResultService {
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private SubHomeworkResultDao subHomeworkResultDao;
    @Inject
    private SubHomeworkResultAnswerDao subHomeworkResultAnswerDao;
    @Inject
    private HomeworkHBaseQueueServiceImpl homeworkHBaseQueueService;
    @Inject
    private SubHomeworkResultExtendedInfoDao subHomeworkResultExtendedInfoDao;
    @Inject private HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;
    @Inject private SelfStudyHomeworkReportDao selfStudyHomeworkReportDao;
    @Inject private SelfStudyAccomplishmentDao selfStudyAccomplishmentDao;

    @Override
    public void initNewHomeworkResult(NewHomework.Location location, Long userId) {
        try {
            if (NewHomeworkUtils.isSubHomework(location.getId()) || NewHomeworkUtils.isShardHomework(location.getId())) {
                SubHomeworkResult result = subHomeworkResultDao.initSubHomeworkResult(HomeworkTransform.NewHomeworkLocationToSub(location), userId);
                if (result != null) {
                    homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
                }
            }
        } catch (DocumentAccessException ignored) {
            logger.error("initNewHomeworkResult error hid:{},uid:{}", location.getId(), userId);
        }
    }

    @Override
    public Boolean updateNewHomeworkResultUrge(NewHomeworkResult newHomeworkResult) {
        try {
            SubHomeworkResult result = subHomeworkResultDao.updateNewHomeworkResultUrge(HomeworkTransform.NewHomeworkResultToSub(newHomeworkResult));
            if (result != null) {
                homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
            }
            return result != null;
        } catch (Exception e) {
            logger.error("updateNewHomeworkResultUrge result id{} error", newHomeworkResult.getId());
            return false;
        }
    }

    @Override
    public Boolean finishHomeworkBasicAppPractice(NewHomework newHomework, Long studentId, ObjectiveConfigType objectiveConfigType, String key, Double score, Long duration) {
        SubHomeworkResult result = subHomeworkResultDao.finishHomeworkBasicAppPractice(HomeworkTransform.NewHomeworkLocationToSub(newHomework.toLocation()), studentId, objectiveConfigType, key, score, duration);
        if (result != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
        }
        return result != null;
    }

    @Override
    public Boolean finishCorrect(NewHomework newHomework, Long userId, ObjectiveConfigType type, Boolean finishCorrect, Boolean allFinishCorrect) {
        SubHomeworkResult result = subHomeworkResultDao.finishCorrect(HomeworkTransform.NewHomeworkLocationToSub(newHomework.toLocation()), userId, type, finishCorrect, allFinishCorrect);
        if (result != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
        }
        return result != null;
    }

    @Override
    public Boolean saveNewHomeworkComment(NewHomework newHomework, Long userId, String comment, String audioComment) {
        SubHomeworkResult result = subHomeworkResultDao.saveSubHomeworkComment(HomeworkTransform.NewHomeworkLocationToSub(newHomework.toLocation()), userId, comment, audioComment);
        if (result != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
        }
        return result != null;
    }

    @Override
    public Boolean saveHomeworkRewardIntegral(NewHomework.Location location, Long userId, Integer count) {
        SubHomeworkResult result = subHomeworkResultDao.saveHomeworkRewardIntegral(HomeworkTransform.NewHomeworkLocationToSub(location), userId, count);
        if (result != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
        }
        return result != null;
    }

    @Override
    public Boolean saveFinishHomeworkReward(NewHomework newHomework, Long userId, Integer integral, Integer energy, Integer credit) {
        SubHomeworkResult result = subHomeworkResultDao.saveFinishHomeworkReward(HomeworkTransform.NewHomeworkLocationToSub(newHomework.toLocation()), userId, integral, energy, credit);
        if (result != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
        }
        return result != null;
    }

    @Override
    public Boolean finishHomeworkBasicAppPractice(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String key, Double avgScore, Long duration) {
        SubHomeworkResult result = subHomeworkResultDao.finishHomeworkBasicAppPractice(HomeworkTransform.NewHomeworkLocationToSub(location), userId, objectiveConfigType, key, avgScore, duration);
        if (result != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
        }
        return result != null;
    }

    @Override
    public NewHomeworkResult doHomeworkBasicAppPractice(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String key, NewHomeworkResultAppAnswer nhraa) {
        try {
            // 加个校验，如果作业已完成，基础练习错题重做不再更新answer表和result表
            // 重难点视频也加上这个校验，保证分数和报告里面的答题详情一致
            if (ObjectiveConfigType.BASIC_APP == objectiveConfigType
                    || ObjectiveConfigType.NATURAL_SPELLING == objectiveConfigType
                    || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW == objectiveConfigType
                    || ObjectiveConfigType.KEY_POINTS == objectiveConfigType
                    || ObjectiveConfigType.NEW_READ_RECITE == objectiveConfigType
                    || ObjectiveConfigType.READ_RECITE_WITH_SCORE == objectiveConfigType
                    || ObjectiveConfigType.DUBBING == objectiveConfigType
                    || ObjectiveConfigType.DUBBING_WITH_SCORE == objectiveConfigType
                    || ObjectiveConfigType.ORAL_COMMUNICATION == objectiveConfigType
                    || ObjectiveConfigType.WORD_RECOGNITION_AND_READING == objectiveConfigType
                    || ObjectiveConfigType.WORD_TEACH_AND_PRACTICE == objectiveConfigType) {
                String day = DayRange.newInstance(location.getCreateTime()).toString();
                SubHomeworkResult.ID id = new SubHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());
                SubHomeworkResult oldSubHomeworkResult = subHomeworkResultDao.load(id.toString());
                if (oldSubHomeworkResult != null && oldSubHomeworkResult.isFinished()) {
                    return null;
                }
            }
            Boolean upsertSuccess = upsertNewHomeworkAppAnswers(location, userId, objectiveConfigType, nhraa);
            if (!upsertSuccess) {
                return null;
            }
            SubHomeworkResult subHomeworkResult = subHomeworkResultDao.doHomeworkBasicApp(HomeworkTransform.NewHomeworkLocationToSub(location), userId, objectiveConfigType, key, HomeworkTransform.NewHomeworkResultAppAnswerToBase(nhraa));
            if (subHomeworkResult != null) {
                homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(subHomeworkResult));
                // 重难点视频才需要resultAnswer，基础练习和视频绘本就不查了
                if (ObjectiveConfigType.KEY_POINTS == objectiveConfigType
                        || ObjectiveConfigType.NEW_READ_RECITE == objectiveConfigType
                        || ObjectiveConfigType.READ_RECITE_WITH_SCORE == objectiveConfigType
                        || ObjectiveConfigType.WORD_RECOGNITION_AND_READING == objectiveConfigType
                        || ObjectiveConfigType.WORD_TEACH_AND_PRACTICE == objectiveConfigType) {
                    NewHomework newHomework = newHomeworkLoader.load(location.getId());
                    return HomeworkTransform.SubHomeworkResultToNew(subHomeworkResult, subHomeworkResultAnswerDao.loads(newHomeworkResultLoader.initSubHomeworkResultAnswerIds(newHomework, userId)).values());
                } else {
                    return HomeworkTransform.SubHomeworkResultToNew(subHomeworkResult, Collections.emptyList());
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            logger.error("doHomeworkBasicAppPractice fail studetId:{},homeworkId:{},objectiveConfigType:{},newHomeworkResultAppAnswer:{}", userId, location.getId(), objectiveConfigType, JsonUtils.toJson(nhraa), ex);
            return null;
        }
    }

    @Override
    public NewHomeworkResult doHomeworkExamAnswer(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String qid, String processResultId) {
        try {
            // NewHomework newHomework = newHomeworkLoader.load(location.getId());
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            String hid = location.getId();
            List<String> joinKeys = new ArrayList<>();
            SubHomeworkResultAnswer.ID aid = new SubHomeworkResultAnswer.ID();
            aid.setDay(day);
            aid.setHid(hid);
            aid.setJoinKeys(joinKeys);
            aid.setType(objectiveConfigType);
            aid.setUserId(SafeConverter.toString(userId));
            aid.setQuestionId(qid);
            SubHomeworkResultAnswer answer = new SubHomeworkResultAnswer();
            answer.setId(aid.toString());
            answer.setProcessId(processResultId);
            try {
                subHomeworkResultAnswerDao.insert(answer);
                homeworkHBaseQueueService.sendSubHomeworkResultAnswer(Collections.singletonList(answer));
            } catch (MongoWriteException ex) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", userId,
                        "mod1", hid,
                        "mod2", objectiveConfigType,
                        "mod3", qid,
                        "mod4", processResultId,
                        "mod5", ex.getMessage(),
                        "mod6", "doHomeworkExamAnswer",
                        "op", "SaveSubHomeworkResultAnswer"
                ));
            }
            NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());
            SubHomeworkResult subHomeworkResult = subHomeworkResultDao.load(id.toString());
            if (subHomeworkResult != null) {
                // 这块其实没必要拿resultAnswer，调用的地方都没用到返回的NewHomeworkResult对象
                // return NewHomeworkResult.of(subHomeworkResult, subHomeworkResultAnswerDao.loads(newHomeworkResultLoader.initSubHomeworkResultAnswerIds(newHomework, userId)).values());
                return HomeworkTransform.SubHomeworkResultToNew(subHomeworkResult, Collections.emptyList());
            } else {
                return null;
            }
        } catch (Exception ex) {
            logger.error("doHomeworkExamAnswer fail studetId:{},homeworkId:{},objectiveConfigType:{},qid:{}, processResultId:{}", userId, location.getId(), objectiveConfigType, qid, processResultId, ex);
            return null;
        }
    }

    private boolean upsertNewHomeworkAppAnswers(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, NewHomeworkResultAppAnswer nhraa) {
        LinkedHashMap<String, String> questionAnswers = nhraa.getAnswers();
        LinkedHashMap<String, String> oralAnswers = nhraa.getOralAnswers();
        LinkedHashMap<String, String> imageTextRhymeAnswers = nhraa.getImageTextRhymeAnswers();
        LinkedHashMap<String, String> chineseCourses = nhraa.getChineseCourses();
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();

        if (questionAnswers != null && !questionAnswers.isEmpty()) {
            answers.putAll(questionAnswers);
        }
        if (imageTextRhymeAnswers != null && !imageTextRhymeAnswers.isEmpty()) {
            answers.putAll(imageTextRhymeAnswers);
        }
        if (chineseCourses != null && !chineseCourses.isEmpty()) {
            answers.putAll(chineseCourses);
        }
        if (oralAnswers != null && !oralAnswers.isEmpty()) {
            answers.putAll(oralAnswers);
        }

        List<String> joinKeys = new ArrayList<>();
        switch (objectiveConfigType) {
            case BASIC_APP:
            case LS_KNOWLEDGE_REVIEW:
            case NATURAL_SPELLING:
                Integer categoryId = nhraa.getCategoryId();
                if (categoryId == null || categoryId == 0) return false;
                joinKeys.add(SafeConverter.toString(categoryId));
                String lessonId = nhraa.getLessonId();
                if (StringUtils.isBlank(lessonId)) return false;
                joinKeys.add(lessonId);
                break;
            case READING:
            case LEVEL_READINGS:
                String pictureBookId = nhraa.getPictureBookId();
                if (StringUtils.isBlank(pictureBookId)) return false;
                joinKeys.add(pictureBookId);
                break;
            case KEY_POINTS:
                String videoId = nhraa.getVideoId();
                if (StringUtils.isBlank(videoId)) return false;
                joinKeys.add(videoId);
                break;
            case DUBBING:
            case DUBBING_WITH_SCORE:
                String dubbingId = nhraa.getDubbingId();
                if (StringUtils.isBlank(dubbingId)) return false;
                joinKeys.add(dubbingId);
                break;
            case ORAL_COMMUNICATION:
            case WORD_TEACH_AND_PRACTICE:
                String stoneId = nhraa.getStoneId();
                if (StringUtils.isBlank(stoneId)) return false;
                joinKeys.add(stoneId);
                break;
            case NEW_READ_RECITE:
            case READ_RECITE_WITH_SCORE:
            case WORD_RECOGNITION_AND_READING:
                String questionBoxId = nhraa.getQuestionBoxId();
                if (StringUtils.isBlank(questionBoxId)) return false;
                joinKeys.add(questionBoxId);
                break;
            default:
        }

        String day = DayRange.newInstance(location.getCreateTime()).toString();
        String hid = location.getId();
        for (String qid : answers.keySet()) {
            SubHomeworkResultAnswer.ID aid = new SubHomeworkResultAnswer.ID();
            aid.setDay(day);
            aid.setHid(hid);
            aid.setJoinKeys(joinKeys);
            aid.setType(objectiveConfigType);
            aid.setUserId(SafeConverter.toString(userId));
            aid.setQuestionId(qid);
            SubHomeworkResultAnswer answer = new SubHomeworkResultAnswer();
            answer.setId(aid.toString());
            answer.setProcessId(answers.get(qid));
            answer.setIsOral(oralAnswers != null && oralAnswers.get(qid) != null);
            answer.setIsImageTextRhyme(imageTextRhymeAnswers != null && imageTextRhymeAnswers.get(qid) != null);
            answer.setIsChineseCourse(chineseCourses != null && chineseCourses.get(qid) != null);
            try {
                SubHomeworkResultAnswer resultAnswer = subHomeworkResultAnswerDao.upsert(answer);
                if (resultAnswer != null) {
                    homeworkHBaseQueueService.sendSubHomeworkResultAnswer(Collections.singletonList(resultAnswer));
                }
            } catch (MongoCommandException ex) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", userId,
                        "mod1", hid,
                        "mod2", objectiveConfigType,
                        "mod3", qid,
                        "mod4", aid.toString(),
                        "mod5", ex.getMessage(),
                        "mod6", "upsertNewHomeworkAppAnswers",
                        "op", "SaveSubHomeworkResultAnswer"
                ));
            }
        }
        return true;
    }

    @Override
    public boolean finishHomeworkKeyPoint(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType, String videoId, Double score, Long duration) {
        SubHomeworkResult result = subHomeworkResultDao.finishHomeworkKeyPoint(HomeworkTransform.NewHomeworkLocationToSub(location), userId, objectiveConfigType, videoId, score, duration);
        if (result != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
        }
        return result != null;
    }

    @Override
    public boolean finishHomeworkNewReadRecite(NewHomework.Location location,
                                               Long userId,
                                               ObjectiveConfigType objectiveConfigType,
                                               String questionBoxId,
                                               Double score,
                                               Long duration) {
        if (NewHomeworkUtils.isSubHomework(location.getId()) || NewHomeworkUtils.isShardHomework(location.getId())) {
            SubHomeworkResult result = subHomeworkResultDao.finishHomeworkNewReadRecite(
                    HomeworkTransform.NewHomeworkLocationToSub(location),
                    userId,
                    objectiveConfigType,
                    questionBoxId,
                    score,
                    duration);

            if (result != null) {
                homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
            }
            return result != null;
        }
        //不再判断老的作业形式 newHomeworkResultDao
        return false;
    }

    @Override
    public boolean finishHomeworkReadReciteWithScore(NewHomework.Location location,
                                                     Long userId,
                                                     ObjectiveConfigType objectiveConfigType,
                                                     String questionBoxId,
                                                     Double score,
                                                     Long duration,
                                                     Integer standardNum,
                                                     Integer appQuestionNum) {
        if (NewHomeworkUtils.isSubHomework(location.getId()) || NewHomeworkUtils.isShardHomework(location.getId())) {
            SubHomeworkResult result = subHomeworkResultDao.finishHomeworkReadReciteWithScore(
                    HomeworkTransform.NewHomeworkLocationToSub(location),
                    userId,
                    objectiveConfigType,
                    questionBoxId,
                    score,
                    duration,
                    standardNum,
                    appQuestionNum);
            if (result != null) {
                homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
            }
            return result != null;
        }
        return false;
    }

    @Override
    public boolean finishHomeworkWordTeachAndPractice(NewHomework.Location location, Long userId, ObjectiveConfigType objectiveConfigType,
                                                      String stoneDataId,
                                                      Double score,
                                                      Long duration,
                                                      Double wordExerciseScore,
                                                      Double finalImageTextRhymeScore) {
        if (NewHomeworkUtils.isSubHomework(location.getId()) || NewHomeworkUtils.isShardHomework(location.getId())) {
            SubHomeworkResult result = subHomeworkResultDao.finishHomeworkWordTeachAndPractice(
                    HomeworkTransform.NewHomeworkLocationToSub(location),
                    userId,
                    objectiveConfigType,
                    stoneDataId,
                    score,
                    duration,
                    wordExerciseScore,
                    finalImageTextRhymeScore);
            if (result != null) {
                homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
            }
            return result != null;
        }
        return false;
    }

    @Override
    public NewHomeworkResult finishHomework(NewHomework newHomework, Long userId, ObjectiveConfigType objectiveConfigType, Double score, Long duration, Boolean isPracticeFinished, Boolean isHomeworkFinished, Boolean isIncludeSubjective, Boolean isRepaired,
                                            List<String> ocrMentalAnswers, Integer ocrMentalQuestionCount, Integer ocrMentalCorrectQuestionCount,
                                            List<String> ocrDictationAnswers, Integer ocrDictationQuestionCount, Integer ocrDictationCorrectQuestionCount
    ) {
        SubHomeworkResult subHomeworkResult = subHomeworkResultDao.finishHomework(HomeworkTransform.NewHomeworkLocationToSub(newHomework.toLocation()), userId, objectiveConfigType, score, duration, isPracticeFinished, isHomeworkFinished,
                (isHomeworkFinished && !isIncludeSubjective),
                isRepaired,
                newHomework.getSchoolLevel(),
                ocrMentalAnswers,
                ocrMentalQuestionCount,
                ocrMentalCorrectQuestionCount,
                ocrDictationAnswers,
                ocrDictationQuestionCount,
                ocrDictationCorrectQuestionCount
        );
        if (subHomeworkResult != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(subHomeworkResult));
            return HomeworkTransform.SubHomeworkResultToNew(subHomeworkResult, subHomeworkResultAnswerDao.loads(newHomeworkResultLoader.initSubHomeworkResultAnswerIds(newHomework, userId)).values());
        } else {
            return null;
        }
    }

    public void saveSubHomeworkResultAnswers(Collection<SubHomeworkResultAnswer> entities) {
        try {
            if (CollectionUtils.isNotEmpty(entities)) {
                subHomeworkResultAnswerDao.inserts(entities);
                homeworkHBaseQueueService.sendSubHomeworkResultAnswer(new ArrayList<>(entities));
            }
        } catch (MongoBulkWriteException ex) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "mod5", ex.getMessage(),
                    "mod6", "saveSubHomeworkResultAnswers",
                    "op", "SaveSubHomeworkResultAnswer"
            ));
        }
    }

    public Boolean finishCorrectToAppForSubHomeworkResult(String homeworkId,
                                                          String id,
                                                          ObjectiveConfigType type,
                                                          String appId,
                                                          Boolean review,
                                                          CorrectType correctType,
                                                          Correction correction,
                                                          String teacherMark,
                                                          Boolean isBatch) {

        SubHomeworkResult result = subHomeworkResultDao.finishCorrectToApp(homeworkId, id, type, appId, review, correctType, correction, teacherMark, isBatch);
        if (result != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
        }
        return result != null;
    }

    public Boolean finishCorrect(NewHomework.Location location,
                                 Long userId,
                                 ObjectiveConfigType type,
                                 Boolean finishCorrect,
                                 Boolean allFinishCorrect) {
        SubHomeworkResult result = subHomeworkResultDao.finishCorrect(HomeworkTransform.NewHomeworkLocationToSub(location), userId, type, finishCorrect, allFinishCorrect);
        if (result != null) {
            homeworkHBaseQueueService.sendSubHomeworkResult(Collections.singletonList(result));
        }
        return result != null;
    }

    @Override
    public SubHomeworkResultExtendedInfo updateSubHomeworkResultExtendedInfo(String id, Map<String, String> info) {
        if (StringUtils.isEmpty(id) || MapUtils.isEmpty(info)) {
            return null;
        }
        return subHomeworkResultExtendedInfoDao.updateSubHomeworkResultExtendedInfo(id, info);
    }

    public void initCorrectStatus(NewHomeworkResult homeworkResult) {
        //判断自学作业是否生成
        HomeworkSelfStudyRef.ID ID = new HomeworkSelfStudyRef.ID(homeworkResult.getHomeworkId(), homeworkResult.getUserId());
        HomeworkSelfStudyRef homeworkSelfStudyRef = homeworkSelfStudyRefDao.load(ID.toString());
        if (homeworkSelfStudyRef != null) {
            // 更新SelfStudyAccomplishment表
            selfStudyAccomplishmentDao.generateSelfStudyHomework(homeworkResult.getHomeworkId(), homeworkResult.getUserId(), homeworkSelfStudyRef.getSelfStudyId());
            Map<String, String> infoMap = Maps.newHashMap();
            infoMap.put(NewHomeworkConstants.HOMEWORK_CORRECT_STATUS, HomeworkCorrectStatus.NOT_STARTED_CORRECT.name());
            this.updateSubHomeworkResultExtendedInfo(homeworkResult.getId(), infoMap);
        }
    }

    /**
     * 查询作业订正状态
     */
    public HomeworkCorrectStatus getHomeworkCorrectStatus(NewHomework homework, NewHomeworkResult homeworkResult) {
        if (!(NewHomeworkUtils.isSubHomework(homework.getId()) || NewHomeworkUtils.isShardHomework(homework.getId()))
                || homeworkResult == null
                || homeworkResult.getPractices() == null
                || !NeedSelfStudyHomeworkSubjects.contains(homework.getSubject())
                || Objects.equals(homework.getSchoolLevel(), SchoolLevel.INFANT)
                || !GenerateHomeworkTypes.contains(homework.getType())
                || !GenerateHomeworkTags.contains(homework.getHomeworkTag())
                || !CollectionUtils.containsAny(homeworkResult.getPractices().keySet(), GenerateSelfStudyHomeworkConfigTypes)) {
            return HomeworkCorrectStatus.WITHOUT_CORRECT;
        }

        String hCorrectStatus = subHomeworkResultExtendedInfoDao.loadExtendedInfo(homeworkResult.getId(), NewHomeworkConstants.HOMEWORK_CORRECT_STATUS);
        if (StringUtils.isEmpty(hCorrectStatus)) {
            //说明是老数据, 走原始方法查询订正状态
            String homeworkToSelfStudyId = new HomeworkSelfStudyRef.ID(homework.getId(), homeworkResult.getUserId()).toString();
            HomeworkSelfStudyRef homeworkSelfStudyRef = homeworkSelfStudyRefDao.load(homeworkToSelfStudyId);
            if (homeworkSelfStudyRef == null) {
                return HomeworkCorrectStatus.WITHOUT_CORRECT;
            }
            SelfStudyHomeworkReport selfStudyHomeworkReport = selfStudyHomeworkReportDao.load(homeworkSelfStudyRef.getSelfStudyId());
            if (selfStudyHomeworkReport == null) {
                return HomeworkCorrectStatus.NOT_STARTED_CORRECT;
            }
            return HomeworkCorrectStatus.CORRECT_FINISH;
        }
        return HomeworkCorrectStatus.of(hCorrectStatus);
    }

    /**
     * 批量查询作业订正状态
     */
    //fixme 订正课程上线之前的数据没有(PM说可以不要)
    public Map<String, HomeworkCorrectStatus> fetchHomeworkCorrectStatus(List<String> homeworkResultIds) {
        Map<String, SubHomeworkResultExtendedInfo> subHomeworkResultExtendedInfoMap = subHomeworkResultExtendedInfoDao.loadExtendedInfos(homeworkResultIds);
        Map<String, HomeworkCorrectStatus> hCorrectStatusMap = Maps.newHashMap();
        if (MapUtils.isNotEmpty(subHomeworkResultExtendedInfoMap)) {
            Map<String, Map<String, String>> transform = MapUtils.transform(subHomeworkResultExtendedInfoMap, SubHomeworkResultExtendedInfo::getInfo);
            for (Map.Entry<String, Map<String, String>> mapEntry : transform.entrySet()) {
                String hCorrectStatus = mapEntry.getValue().get(NewHomeworkConstants.HOMEWORK_CORRECT_STATUS);
                hCorrectStatusMap.put(mapEntry.getKey(), HomeworkCorrectStatus.of(hCorrectStatus));
            }
        }
        return hCorrectStatusMap;
    }
}
