package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkLivecastService;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.LiveCastHomeworkPublisher;
import com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.LiveCastHomeworkResultProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.constants.ThirdPartyGroupType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

import static com.voxlearning.utopia.api.constant.ErrorCodeConstants.*;

/**
 * @author xuesong.zhang
 * @since 2016/9/12
 */

@Named
@Service(interfaceClass = NewHomeworkLivecastService.class)
@ExposeService(interfaceClass = NewHomeworkLivecastService.class)
public class NewHomeworkLivecastServiceImpl extends SpringContainerSupport implements NewHomeworkLivecastService {

    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private NewHomeworkServiceImpl newHomeworkService;
    @Inject
    private LiveCastHomeworkPublisher liveCastHomeworkPublisher;
    @Inject
    private LiveCastHomeworkDao liveCastHomeworkDao;
    @Inject
    private LiveCastHomeworkBookDao liveCastHomeworkBookDao;
    @Inject
    private LiveCastHomeworkResultDao liveCastHomeworkResultDao;
    @Inject
    private LiveCastHomeworkProcessResultDao liveCastHomeworkProcessResultDao;

    @Inject
    private LiveCastHomeworkResultProcessor liveCastHomeworkResultProcessor;

    @Override
    public MapMessage assignHomework4LiveCast(Long teacherId, Subject subject, HomeworkSource homeworkSource, String newHomeworkType, ThirdPartyGroupType thirdPartyGroupType) {
        if (teacherId == null || subject == null || homeworkSource == null || newHomeworkType == null) {
            return MapMessage.errorMessage("参数错误！");
        }
        MapMessage mapMessage;
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setSubject(subject);
        NewHomeworkType type = NewHomeworkType.of(newHomeworkType);

        if (StringUtils.equalsIgnoreCase(type.name(), NewHomeworkType.Unknown.name())
                || type.getTypeId() == NewHomeworkType.PlatformType) {
            return MapMessage.errorMessage("作业类型错误！");
        }

        mapMessage = newHomeworkService.assignHomework(teacher, homeworkSource, HomeworkSourceType.Web, type, HomeworkTag.Normal);
        return mapMessage;
    }

    public MapMessage deleteHomework(String id) {
        NewHomework newHomework = newHomeworkLoader.load(id);
        if (newHomework == null || !Objects.equals(newHomework.getType().getTypeId(), NewHomeworkType.ThirdPartyType)) {
            return MapMessage.errorMessage("作业不存在");
        }
        try {
            Boolean delete = newHomeworkService.updateDisabledTrue(id);
            if (delete) {
                return MapMessage.successMessage("删除作业成功");
            } else {
                return MapMessage.errorMessage("删除作业失败");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public MapMessage deleteHomework(String id, String newHomeworkType) {
        NewHomeworkType type = NewHomeworkType.of(newHomeworkType);
        if (type.getTypeId() != NewHomeworkType.ThirdPartyType) {
            return MapMessage.errorMessage("作业类型错误！");
        }
        if (liveCastHomeworkDao.updateDisabledTrue(id)) {
            return MapMessage.successMessage("删除作业成功");
        } else {
            return MapMessage.errorMessage("删除作业失败");
        }
    }


    /**
     * correctInfo 的格式如下
     * {
     * 　　"qid1": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语"
     * 　　},
     * 　　"qid2": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语"
     * 　　},
     * 　　"qid3": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语"
     * 　　}
     * }
     */
    //TODO 保存小数的位数
    //TODO 測試接口
    //TODO 是否需要扩展支持另外的作业类型类型

    public MapMessage newCorrectQuestions(String homeworkId, Long studentId, Map<String, Object> correctInfoMap, ObjectiveConfigType type) {
        if (StringUtils.isBlank(homeworkId) || studentId == null || MapUtils.isEmpty(correctInfoMap)) {
            return MapMessage.errorMessage("参数错误").setErrorCode(ERROR_CODE_PARAMETER);
        }
        LiveCastHomework homework = liveCastHomeworkDao.load(homeworkId);
        if (homework == null) {
            return MapMessage.errorMessage("作业不存在").setErrorCode(ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        NewHomeworkPracticeContent target = homework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (target == null || CollectionUtils.isEmpty(target.processNewHomeworkQuestion(true))) {
            return MapMessage.errorMessage("作业不存在该类型").setErrorCode(ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        double scoreToQuestion = new BigDecimal(100).divide(new BigDecimal(target.processNewHomeworkQuestion(true).size()), 6, BigDecimal.ROUND_HALF_UP).doubleValue();

        String month = MonthRange.newInstance(homework.getCreateAt().getTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, homework.getSubject(), homeworkId, studentId);
        LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultDao.load(id.toString());
        if (liveCastHomeworkResult == null) {
            return MapMessage.errorMessage("学生未开始作业").setErrorCode(ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }

        List<String> processIds = liveCastHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(type);
        Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(processIds);
        if (MapUtils.isEmpty(liveCastHomeworkResult.getPractices()) || MapUtils.isEmpty(processResultMap)) {
            return MapMessage.errorMessage("学生未开始作业").setErrorCode(ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }

        NewHomeworkResultAnswer resultAnswer = liveCastHomeworkResult.getPractices().getOrDefault(type, null);
        if (resultAnswer == null) {
            return MapMessage.errorMessage("学生未开始作业").setErrorCode(ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }

        Map<String, String> answerMap = resultAnswer.getAnswers();
        // 组装一下数据，key是qid
        Map<String, LiveCastHomeworkProcessResult> questionProcessResultMap = new HashMap<>();
        for (Map.Entry<String, String> entry : answerMap.entrySet()) {
            String key = entry.getKey();
            LiveCastHomeworkProcessResult value = processResultMap.get(entry.getValue());
            if (StringUtils.isNotBlank(key) && value != null) {
                questionProcessResultMap.put(key, value);
            }
        }

        double changeScore = 0;
        Map<String, Boolean> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : correctInfoMap.entrySet()) {
            String qid = entry.getKey();
            if (questionProcessResultMap.getOrDefault(qid, null) != null && entry.getValue() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> detailMap = (Map<String, Object>) entry.getValue();
                    String imgUrl = SafeConverter.toString(detailMap.get("imgUrl"), "");
                    Double score = SafeConverter.toDouble(detailMap.get("score"), -1D);
                    String voice = SafeConverter.toString(detailMap.get("voice"), "");
                    Double percentage = SafeConverter.toDouble(detailMap.get("percentage"), -1D);
                    if (score >= 0D) {
                        if (score > scoreToQuestion) {
                            logger.warn("score {} go beyond scoreToQuestion {} of detail {} hid of {} ,sid of {}", score, scoreToQuestion, correctInfoMap, homeworkId, studentId);
                            score = scoreToQuestion;
                        }
                        LiveCastHomeworkProcessResult liveCastHomeworkProcessResult = questionProcessResultMap.get(qid);

                        changeScore = changeScore + score - SafeConverter.toDouble(liveCastHomeworkProcessResult.getScore());
                    }
                    String comment = SafeConverter.toString(detailMap.get("comment"), "");
                    LiveCastHomeworkProcessResult result = questionProcessResultMap.get(qid);
                    boolean b = liveCastHomeworkProcessResultDao.updateCorrection(
                            result.getId(),
                            result.getHomeworkId(),
                            qid,
                            studentId,
                            score,
                            comment,
                            imgUrl,
                            voice,
                            percentage
                            );

                    resultMap.put(qid, b);
                } catch (Exception e) {
                    logger.warn("correctInfo Error, info:" + JsonUtils.toJson(correctInfoMap));
                }
            }
        }


        double score = changeScore + SafeConverter.toDouble(resultAnswer.getScore());
        liveCastHomeworkResultDao.changeScore(liveCastHomeworkResult, type, score);

        if (liveCastHomeworkResult.isFinished()) {
            //第二次批改完成
            if (liveCastHomeworkResult.isCorrected()){
                liveCastHomeworkResult = liveCastHomeworkResultDao.load(liveCastHomeworkResult.getId());
                //发送广播
                Map<String, Object> map = new HashMap<>();
                map.put("liveCastHomeworkResult", liveCastHomeworkResult);
                map.put("messageType", HomeworkPublishMessageType.corrected);
                liveCastHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));

            }else{
                //第一次批改完成
                NewHomeworkResultAnswer newHomeworkResultAnswer = liveCastHomeworkResult.getPractices().get(type);
                if (!newHomeworkResultAnswer.isCorrected()) {
                    boolean typeFinishCorrected = true;
                    for (LiveCastHomeworkProcessResult liveCastHomeworkProcessResult : processResultMap.values()) {
                        //是这一次批改的题目，而且这次批改成功的
                        if (resultMap.containsKey(liveCastHomeworkProcessResult.getQuestionId()) && SafeConverter.toBoolean(resultMap.get(liveCastHomeworkProcessResult.getQuestionId())))
                            continue;
                        //如果是未批改
                        if (!SafeConverter.toBoolean(liveCastHomeworkProcessResult.getReview())) {
                            typeFinishCorrected = false;
                            break;
                        }
                    }
                    if (typeFinishCorrected) {
                        boolean allFinishCorrect = true;
                        for (Map.Entry<ObjectiveConfigType, NewHomeworkResultAnswer> entry : liveCastHomeworkResult.getPractices().entrySet()) {
                            if (entry.getKey() != type && entry.getKey().isSubjective()) {
                                if (!entry.getValue().isCorrected()) {
                                    allFinishCorrect = false;
                                }
                            }
                        }
                        liveCastHomeworkResultDao.finishCorrect(liveCastHomeworkResult.getId(), type, true, allFinishCorrect);
                        if (allFinishCorrect) {
                            liveCastHomeworkResult = liveCastHomeworkResultDao.load(liveCastHomeworkResult.getId());
                            //发送广播
                            Map<String, Object> map = new HashMap<>();
                            map.put("liveCastHomeworkResult", liveCastHomeworkResult);
                            map.put("messageType", HomeworkPublishMessageType.corrected);
                            liveCastHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
                        }
                    }

                }
            }
        }

        return MapMessage.successMessage().add("questionMap", resultMap);
    }


    /**
     * correctInfo 的格式如下
     * {
     * 　　"qid1": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语"
     * 　　},
     * 　　"qid2": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语"
     * 　　},
     * 　　"qid3": {
     * 　　　　"imgUrl":"// 批改图片",
     * 　　　　"score": // 分数（整数）,
     * 　　　　"comment":"// 评语"
     * 　　}
     * }
     */
    //TODO 保存小数的位数
    //TODO 是否需要扩展支持另外的作业类型类型
    @Override
    public MapMessage correctQuestions(String homeworkId, Long studentId, Map<String, Object> correctInfoMap) {
        if (StringUtils.isBlank(homeworkId) || studentId == null || MapUtils.isEmpty(correctInfoMap)) {
            return MapMessage.errorMessage("参数错误").setErrorCode(ERROR_CODE_PARAMETER);
        }

        LiveCastHomework homework = liveCastHomeworkDao.load(homeworkId);
        if (homework == null) {
            return MapMessage.errorMessage("作业不存在").setErrorCode(ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        NewHomeworkPracticeContent target = homework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.EXAM);
        if (target == null || CollectionUtils.isEmpty(target.getQuestions())) {
            return MapMessage.errorMessage("作业不存在该类型").setErrorCode(ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        double scoreToQuestion = new BigDecimal(100).divide(new BigDecimal(target.getQuestions().size()), 6, BigDecimal.ROUND_HALF_UP).doubleValue();

        String month = MonthRange.newInstance(homework.getCreateAt().getTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, homework.getSubject(), homeworkId, studentId);
        LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultDao.load(id.toString());
        if (liveCastHomeworkResult == null) {
            return MapMessage.errorMessage("学生未开始作业").setErrorCode(ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }

        List<String> processIds = liveCastHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(ObjectiveConfigType.EXAM);
        Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(processIds);
        if (MapUtils.isEmpty(liveCastHomeworkResult.getPractices()) || MapUtils.isEmpty(processResultMap)) {
            return MapMessage.errorMessage("学生未开始作业").setErrorCode(ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }

        NewHomeworkResultAnswer resultAnswer = liveCastHomeworkResult.getPractices().getOrDefault(ObjectiveConfigType.EXAM, null);
        if (resultAnswer == null) {
            return MapMessage.errorMessage("学生未开始作业").setErrorCode(ERROR_CODE_HOMEWORK_NOT_FINISHED);
        }

        Map<String, String> answerMap = resultAnswer.getAnswers();
        // 组装一下数据，key是qid
        Map<String, LiveCastHomeworkProcessResult> questionProcessResultMap = new HashMap<>();
        for (Map.Entry<String, String> entry : answerMap.entrySet()) {
            String key = entry.getKey();
            LiveCastHomeworkProcessResult value = processResultMap.get(entry.getValue());
            if (StringUtils.isNotBlank(key) && value != null) {
                questionProcessResultMap.put(key, value);
            }
        }

        double changeScore = 0;
        Map<String, Boolean> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : correctInfoMap.entrySet()) {
            String qid = entry.getKey();
            if (questionProcessResultMap.getOrDefault(qid, null) != null && entry.getValue() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> detailMap = (Map<String, Object>) entry.getValue();
                    String imgUrl = SafeConverter.toString(detailMap.get("imgUrl"), "");
                    Double score = SafeConverter.toDouble(detailMap.get("score"), -1D);
                    String voice = SafeConverter.toString(detailMap.get("voice"), "");
                    Double percentage = SafeConverter.toDouble(detailMap.get("percentage"), -1D);
                    if (score >= 0D) {
                        if (score > scoreToQuestion) {
                            logger.warn("score {} go beyond scoreToQuestion {} of detail {} hid of {} ,sid of {}", score, scoreToQuestion, correctInfoMap, homeworkId, studentId);
                            score = scoreToQuestion;
                        }
                        LiveCastHomeworkProcessResult liveCastHomeworkProcessResult = questionProcessResultMap.get(qid);

                        changeScore = changeScore + score - SafeConverter.toDouble(liveCastHomeworkProcessResult.getScore());
                    }

                    String comment = SafeConverter.toString(detailMap.get("comment"), "");
                    LiveCastHomeworkProcessResult result = questionProcessResultMap.get(qid);
                    boolean b = liveCastHomeworkProcessResultDao.updateCorrection(
                            result.getId(),
                            result.getHomeworkId(),
                            qid,
                            studentId,
                            score,
                            comment,
                            imgUrl,
                            voice,
                            percentage
                    );

                    resultMap.put(qid, b);
                } catch (Exception e) {
                    logger.warn("correctInfo Error, info:" + JsonUtils.toJson(correctInfoMap));
                }
            }
        }

        if (changeScore != 0) {
            double score = changeScore + SafeConverter.toDouble(resultAnswer.getScore());
            liveCastHomeworkResultDao.changeScore(liveCastHomeworkResult, ObjectiveConfigType.EXAM, score);
        }


        return MapMessage.successMessage().add("questionMap", resultMap);
    }

    @Override
    public MapMessage processorHomeworkResult(LiveCastHomeworkResultContext homeworkResultContext) {
        try {
            LiveCastHomeworkResultContext context = liveCastHomeworkResultProcessor.process(homeworkResultContext);
            return context.transform().add("result", context.getResult());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public void insertsLiveCastHomeworkWithoutCache(Collection<LiveCastHomework> entities) {
        liveCastHomeworkDao.$inserts(entities);
    }

    @Override
    public void insertsLiveCastHomeworkBookWithoutCache(Collection<LiveCastHomeworkBook> entities) {
        liveCastHomeworkBookDao.$inserts(entities);
    }

    @Override
    public void insertsLiveCastHomeworkResultWithoutCache(Collection<LiveCastHomeworkResult> entities) {
        liveCastHomeworkResultDao.$inserts(entities);
    }

    @Override
    public void insertsLiveCastHomeworkProcessResultWithoutCache(Collection<LiveCastHomeworkProcessResult> entities) {
        liveCastHomeworkProcessResultDao.$inserts(entities);
    }
}
