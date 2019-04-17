package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.FinishLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.LiveCastGenerateDataService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLivecastLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish.FinishLiveCastHomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroup;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ThirdPartyGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/12/28
 */
@Named
@Service(interfaceClass = LiveCastGenerateDataService.class)
@ExposeServices({
        @ExposeService(interfaceClass = LiveCastGenerateDataService.class, version = @ServiceVersion(version = "20161228")),
        @ExposeService(interfaceClass = LiveCastGenerateDataService.class, version = @ServiceVersion(version = "20170712")),
        @ExposeService(interfaceClass = LiveCastGenerateDataService.class, version = @ServiceVersion(version = "20180319"))
})
public class LiveCastGenerateDataServiceImpl implements LiveCastGenerateDataService {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private LiveCastHomeworkDao liveCastHomeworkDao;
    @Inject private LiveCastHomeworkBookDao liveCastHomeworkBookDao;
    @Inject private LiveCastHomeworkResultDao liveCastHomeworkResultDao;
    @Inject private LiveCastHomeworkProcessResultDao liveCastHomeworkProcessResultDao;
    @Inject private NewHomeworkLivecastLoaderImpl newHomeworkLivecastLoader;
    @Inject private PracticeServiceClient practiceServiceClient;
    @Inject private PictureBookLoaderClient pictureBookLoaderClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private DubbingLoaderClient dubbingLoaderClient;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private QuestionLoaderClient questionLoaderClient;

    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private ThirdPartyGroupLoaderClient thirdPartyGroupLoaderClient;
    @Inject private FinishLiveCastHomeworkProcessor finishLiveCastHomeworkProcessor;

    @Override
    public Map<String, Object> generateIndexData(String homeworkId, Long studentId, String token) {
        if (StringUtils.isBlank(homeworkId) || null == studentId) {
            return Collections.emptyMap();
        }
        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
        if (liveCastHomework == null) {
            return Collections.emptyMap();
        }

        Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceMap = liveCastHomework.findPracticeContents();
        Map<String, Object> result = new HashMap<>();
        result.put("homeworkId", liveCastHomework.getId());
        result.put("homeworkType", liveCastHomework.getNewHomeworkType());
        result.put("practiceCount", liveCastHomework.findPracticeContents().size());
        result.put("homeworkName", DateUtils.dateToString(liveCastHomework.getStartTime(), "MM月dd日") + liveCastHomework.getSubject().getValue() + "练习");
        result.put("remark", liveCastHomework.getRemark());
        result.put("subject", liveCastHomework.getSubject());

        LiveCastHomeworkResult liveCastHomeworkResult = newHomeworkLivecastLoader.loadLiveCastHomeworkResult(liveCastHomework.toLocation(), studentId);
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> doPractices = new LinkedHashMap<>();
        if (liveCastHomeworkResult != null && liveCastHomeworkResult.getPractices() != null) {
            doPractices = liveCastHomeworkResult.getPractices();
            if (liveCastHomeworkResult.isFinished()) {
                result.put("thirdPartyFinishedUrl", UrlUtils.buildUrlQuery(ProductConfig.getUSTalkUrl() + "/homework/finished.vpage", MapUtils.m("token", token)));
                result.put("showIntegral", false);
                result.put("integral", 0);
            }
        }

        int undoPracticesCount = 0;
        int totalQuestionCount = 0;
        int doTotalQuestionCount = 0;
        // 此属性用于修复数据
        boolean needFinish = false;
        List<Map<String, Object>> practiceInfos = new ArrayList<>();
        for (ObjectiveConfigType objectiveConfigType : practiceMap.keySet()) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = doPractices.get(objectiveConfigType);
            NewHomeworkPracticeContent newHomeworkPracticeContent = practiceMap.get(objectiveConfigType);

            if (objectiveConfigType == ObjectiveConfigType.BASIC_APP) {
                List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
                List<String> doHomeworkUrls = new ArrayList<>();
                List<String> finishedUrls = new ArrayList<>();
                List<String> unFinishedUrls = new ArrayList<>();
                int questionCount = 0;
                apps = apps.stream().sorted((c1, c2) -> {
                    Integer r1 = practiceServiceClient.getPracticeBuffer().loadPractice(SafeConverter.toLong(c1.getPracticeId())).getCategoryRank();
                    Integer r2 = practiceServiceClient.getPracticeBuffer().loadPractice(SafeConverter.toLong(c2.getPracticeId())).getCategoryRank();
                    return Integer.compare(r1, r2);
                }).collect(Collectors.toList());
                Map<String, NewHomeworkResultAppAnswer> answerMap = liveCastHomeworkResult != null
                        && liveCastHomeworkResult.getPractices() != null
                        && liveCastHomeworkResult.getPractices().get(objectiveConfigType) != null
                        ? liveCastHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers() : Collections.EMPTY_MAP;
                for (NewHomeworkApp newHomeworkApp : apps) {
                    questionCount += newHomeworkApp.getQuestions().size();
                    String key = StringUtils.join(Arrays.asList(newHomeworkApp.getCategoryId(), newHomeworkApp.getLessonId()), "-");
                    if (answerMap.keySet().contains(key)) {
                        finishedUrls.add(UrlUtils.buildUrlQuery("/livecast/student/homework/do.vpage",
                                MapUtils.m(
                                        "homeworkId", homeworkId,
                                        "objectiveConfigType", objectiveConfigType,
                                        "lessonId", newHomeworkApp.getLessonId(),
                                        "categoryId", newHomeworkApp.getCategoryId(),
                                        "practiceId", newHomeworkApp.getPracticeId(),
                                        "token", token)));
                    } else {
                        unFinishedUrls.add(UrlUtils.buildUrlQuery("/livecast/student/homework/do.vpage",
                                MapUtils.m(
                                        "homeworkId", homeworkId,
                                        "objectiveConfigType", objectiveConfigType,
                                        "lessonId", newHomeworkApp.getLessonId(),
                                        "categoryId", newHomeworkApp.getCategoryId(),
                                        "practiceId", newHomeworkApp.getPracticeId(),
                                        "token", token)));
                    }
                }
                doHomeworkUrls.addAll(finishedUrls);
                doHomeworkUrls.addAll(unFinishedUrls);
                int appCount = newHomeworkPracticeContent.getApps().size();
                int doAppCount = newHomeworkResultAnswer != null ? newHomeworkResultAnswer.getAppAnswers().size() : 0;
                Map<String, Object> practiceInfo = MapUtils.m("objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrls", doHomeworkUrls,
                        "middleResultUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/type/result.vpage",
                                MapUtils.m(
                                        "homeworkId", homeworkId,
                                        "objectiveConfigType", objectiveConfigType,
                                        "token", token)),
                        "doCount", doAppCount,
                        "practiceCount", appCount,
                        "questionCount", questionCount,
                        "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null);
                practiceInfos.add(practiceInfo);
                totalQuestionCount += appCount;
                doTotalQuestionCount += doAppCount;
                if (newHomeworkResultAnswer == null || doAppCount < apps.size()) {
                    undoPracticesCount++;
                }

                if (doAppCount == apps.size() && newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() == null) {
                    needFinish = true;
                }
            } else if (objectiveConfigType == ObjectiveConfigType.READING) {
                List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
                List<String> picBookIds = new ArrayList<>();
                int questionCount = 0;
                for (NewHomeworkApp newHomeworkApp : apps) {
                    questionCount += newHomeworkApp.getQuestions().size();
                    picBookIds.add(newHomeworkApp.getPictureBookId());
                }
                int readingCount = newHomeworkPracticeContent.getApps().size();
                int doReadingCount = newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null ? newHomeworkResultAnswer.getAppAnswers().size() : 0;

                Map<String, Object> practiceInfo = MapUtils.m("objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),

                        "doHomeworkUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/do.vpage",
                                MapUtils.m(
                                        "homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType,
                                        "pictureBookIds", StringUtils.join(picBookIds, ","),
                                        "token", token)),

                        "middleResultUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/type/result.vpage",
                                MapUtils.m(
                                        "homeworkId", homeworkId,
                                        "objectiveConfigType", objectiveConfigType,
                                        "token", token)),

                        "doCount", doReadingCount,
                        "practiceCount", readingCount,
                        "questionCount", questionCount,
                        "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null);
                practiceInfos.add(practiceInfo);
                totalQuestionCount += readingCount;
                doTotalQuestionCount += doReadingCount;
                if (newHomeworkResultAnswer == null || doReadingCount < apps.size()) {
                    undoPracticesCount++;
                }
                if (doReadingCount == apps.size() && newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() == null) {
                    needFinish = true;
                }
            } else if (objectiveConfigType == ObjectiveConfigType.LEVEL_READINGS) {
                List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
                int questionCount = 0;
                for (NewHomeworkApp newHomeworkApp : apps) {
                    if (CollectionUtils.isNotEmpty(newHomeworkApp.getQuestions())) {
                        questionCount += newHomeworkApp.getQuestions().size();
                    }
                    if (CollectionUtils.isNotEmpty(newHomeworkApp.getOralQuestions())) {
                        questionCount += newHomeworkApp.getOralQuestions().size();
                    }
                }
                int readingCount = apps.size();
                int doReadingCount = 0;

                if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                    for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                        if (appAnswer.isFinished()) {
                            doReadingCount++;
                        }
                    }
                }

                Map<String, Object> practiceInfo = MiscUtils.m("objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType,
                                "token", token)),
                        "middleResultUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType,
                                "token", token)),
                        "doCount", doReadingCount,
                        "practiceCount", readingCount,
                        "questionCount", questionCount,
                        "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null);
                practiceInfos.add(practiceInfo);
                totalQuestionCount += readingCount;
                doTotalQuestionCount += doReadingCount;
                if (newHomeworkResultAnswer == null || doReadingCount < apps.size()) {
                    undoPracticesCount++;
                }
                if (doReadingCount == apps.size() && newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() == null) {
                    needFinish = true;
                }

            } else if (objectiveConfigType == ObjectiveConfigType.DUBBING) {
                List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
                int dubbingCount = apps.size();
                int doDubbingCount = 0;
                if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                    for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                        if (appAnswer.isFinished()) {
                            doDubbingCount++;
                        }
                    }
                }

                Map<String, Object> practiceInfo = MapUtils.m(
                        "objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/do.vpage", MapUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType,
                                "token", token)),
                        "middleResultUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/type/result.vpage", MapUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType,
                                "token", token)),
                        "doCount", doDubbingCount,
                        "practiceCount", dubbingCount,
                        "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.isFinished()
                );
                practiceInfos.add(practiceInfo);
                totalQuestionCount += dubbingCount;
                doTotalQuestionCount += doDubbingCount;
                if (newHomeworkResultAnswer == null || doDubbingCount < apps.size()) {
                    undoPracticesCount++;
                }
                if (doDubbingCount == apps.size() && newHomeworkResultAnswer != null && !newHomeworkResultAnswer.isFinished()) {
                    needFinish = true;
                }

            } else {

                // 错题量
                int questionCount = newHomeworkPracticeContent.getQuestions().size();
                int doQuestionCount = newHomeworkResultAnswer != null ? newHomeworkResultAnswer.getAnswers().size() : 0;

                Map<String, Object> practiceInfo = MapUtils.m("objectiveConfigType", objectiveConfigType,
                        "objectiveConfigTypeName", objectiveConfigType.getValue(),
                        "doHomeworkUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/do.vpage",
                                MapUtils.m(
                                        "homeworkId", homeworkId,
                                        "objectiveConfigType", objectiveConfigType,
                                        "token", token)),
                        "middleResultUrl", UrlUtils.buildUrlQuery("/livecast/student/homework/type/result.vpage",
                                MapUtils.m("homeworkId", homeworkId,
                                        "objectiveConfigType", objectiveConfigType,
                                        "token", token)),
                        "doCount", doQuestionCount,
                        "questionCount", questionCount,
                        "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null);
                practiceInfos.add(practiceInfo);
                totalQuestionCount += questionCount;
                doTotalQuestionCount += doQuestionCount;
                if (newHomeworkResultAnswer == null || doQuestionCount < questionCount) {
                    undoPracticesCount++;
                }

                // 所有题都已做完，但缺少finishAt
                if (newHomeworkResultAnswer != null
                        && newHomeworkResultAnswer.getFinishAt() == null
                        && questionCount == doQuestionCount) {
                    needFinish = true;
                }
            }
        }

        boolean finished = liveCastHomeworkResult != null && liveCastHomeworkResult.isFinished();
        result.put("finished", finished);
        result.put("practices", practiceInfos);
        result.put("undoPracticesCount", undoPracticesCount);
        result.put("finishingRate", new BigDecimal(doTotalQuestionCount * 100).divide(new BigDecimal(totalQuestionCount), 0, BigDecimal.ROUND_HALF_UP).intValue());
        // 这步dao操作在实现里做了处理
        liveCastHomeworkResultDao.initLiveCastHomeworkResult(liveCastHomework.toLocation(), studentId);
        //当作业没有未做的作业类型且newHomeworkResult的finishAt为空的时候说明学生完成作业的时候数据，因此需要修复数据
        if ((undoPracticesCount == 0 && !finished) || needFinish) {
            finishHomework(liveCastHomework, liveCastHomeworkResult, studentId);
        }
        return result;
    }

    @Override
    public MapMessage homeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
        if (liveCastHomework == null) {
            return MapMessage.errorMessage().setInfo("练习不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        if (objectiveConfigType == null) {
            return MapMessage.errorMessage().setInfo("练习类型不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT_IS_NULL);
        }

        LiveCastHomework.Location location = liveCastHomework.toLocation();
        String month = MonthRange.newInstance(location.getCreateTime()).toString();
        LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
        LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultDao.load(id.toString());
        if (liveCastHomeworkResult == null) {
            return MapMessage.errorMessage().setInfo("liveCastHomeworkResult is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }

        switch (objectiveConfigType) {
            case BASIC_APP:
                return homeworkForBasicAppResult(objectiveConfigType, liveCastHomeworkResult);
            case READING:
                return homeworkForReadingResult(objectiveConfigType, liveCastHomeworkResult);
            case LEVEL_READINGS:
                return homeworkForLevelReadingsResult(objectiveConfigType, liveCastHomeworkResult);
            case DUBBING:
                return homeworkForDubbingResult(objectiveConfigType, liveCastHomeworkResult);
            default:
                return homeworkForExamResult(objectiveConfigType, liveCastHomeworkResult);
        }
    }


    @Deprecated
    @Override
    public Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType) {

        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }

        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
        if (null == liveCastHomework) {
            return Collections.emptyMap();
        }

        List<NewHomeworkQuestion> newHomeworkQuestions = liveCastHomework.findNewHomeworkQuestions(objectiveConfigType);

        if (CollectionUtils.isEmpty(newHomeworkQuestions)) {
            return Collections.emptyMap();
        }

        Map<String, Object> questionMap = new HashMap<>();
        Map<String, NewHomeworkBookInfo> bookInfoMap = new HashMap<>();
        LiveCastHomeworkBook liveCastHomeworkBook = liveCastHomeworkBookDao.load(homeworkId);
        if (liveCastHomeworkBook != null) {
            List<NewHomeworkBookInfo> newHomeworkBookInfos = liveCastHomeworkBook.getPractices().get(objectiveConfigType);
            if (newHomeworkBookInfos != null) {
                for (NewHomeworkBookInfo info : newHomeworkBookInfos) {
                    if (CollectionUtils.isNotEmpty(info.getQuestions())) {
                        info.getQuestions().forEach(o -> bookInfoMap.put(o, info));
                    }

                    if (CollectionUtils.isNotEmpty(info.getPapers())) {
                        info.getPapers().forEach(o -> bookInfoMap.put(o, info));
                    }
                }
            }
        }

        Set<String> eids = new LinkedHashSet<>();
        Map<String, Map<String, Object>> examUnitMap = new LinkedHashMap<>();

        int normalTime = 0;
        for (NewHomeworkQuestion question : newHomeworkQuestions) {
            String qid = question.getQuestionId();
            normalTime += question.getSeconds();
            NewHomeworkBookInfo bookInfo;
            if (StringUtils.isNoneBlank(question.getPaperId())) {
                bookInfo = bookInfoMap.get(question.getPaperId());
            } else {
                bookInfo = bookInfoMap.get(question.getQuestionId());
            }
            if (bookInfo != null) {
                examUnitMap.put(qid, MiscUtils.m("bookId", bookInfo.getBookId(),
                        "unitId", bookInfo.getUnitId(),
                        "lessonId", bookInfo.getLessonId(),
                        "unitGroupId", bookInfo.getUnitGroupId(),
                        "sectionId", bookInfo.getSectionId()
                ));
            }
            eids.add(qid);
        }
        questionMap.put("examUnitMap", examUnitMap);
        questionMap.put("normalTime", normalTime);
        questionMap.put("eids", eids);
        return questionMap;
    }

    @Override
    public Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, Integer categoryId, String lessonId, String videoId) {
        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }

        if (objectiveConfigType == ObjectiveConfigType.BASIC_APP && (categoryId == null || categoryId == 0 || StringUtils.isBlank(lessonId))) {
            return Collections.emptyMap();
        }

        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
        if (null == liveCastHomework) {
            return Collections.emptyMap();
        }

        List<NewHomeworkQuestion> newHomeworkQuestions;
        if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.BASIC_APP.name())) {
            newHomeworkQuestions = liveCastHomework.findNewHomeworkQuestions(objectiveConfigType, lessonId, categoryId);
        } else if (ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)) {
            newHomeworkQuestions = new ArrayList<>(liveCastHomework.findNewHomeworkQuestions(objectiveConfigType, videoId));
            newHomeworkQuestions.addAll(liveCastHomework.findNewHomeworkOralQuestions(objectiveConfigType, videoId));
        } else if (ObjectiveConfigType.DUBBING.equals(objectiveConfigType)) {
            newHomeworkQuestions = liveCastHomework.findNewHomeworkDubbingQuestions(objectiveConfigType, videoId);
        } else {
            newHomeworkQuestions = liveCastHomework.findNewHomeworkQuestions(objectiveConfigType);
        }

        if (CollectionUtils.isEmpty(newHomeworkQuestions)) {
            return Collections.emptyMap();
        }

        Map<String, Object> questionMap = new HashMap<>();
        Map<String, NewHomeworkBookInfo> bookInfoMap = new HashMap<>();
        LiveCastHomeworkBook liveCastHomeworkBook = liveCastHomeworkBookDao.load(homeworkId);
        if (liveCastHomeworkBook != null) {
            List<NewHomeworkBookInfo> newHomeworkBookInfos = liveCastHomeworkBook.getPractices().get(objectiveConfigType);
            if (newHomeworkBookInfos != null) {
                for (NewHomeworkBookInfo info : newHomeworkBookInfos) {
                    if (CollectionUtils.isNotEmpty(info.getQuestions())) {
                        info.getQuestions().forEach(o -> bookInfoMap.put(o, info));
                    }

                    if (CollectionUtils.isNotEmpty(info.getPapers())) {
                        info.getPapers().forEach(o -> bookInfoMap.put(o, info));
                    }
                }
            }
        }

        Set<String> eids = new LinkedHashSet<>();
        Map<String, Map<String, Object>> examUnitMap = new LinkedHashMap<>();

        int normalTime = 0;
        for (NewHomeworkQuestion question : newHomeworkQuestions) {
            String qid = question.getQuestionId();
            normalTime += question.getSeconds();
            NewHomeworkBookInfo bookInfo;
            if (StringUtils.isNoneBlank(question.getPaperId())) {
                bookInfo = bookInfoMap.get(question.getPaperId());
            } else {
                bookInfo = bookInfoMap.get(question.getQuestionId());
            }
            if (bookInfo != null) {
                examUnitMap.put(qid, MiscUtils.m("bookId", bookInfo.getBookId(),
                        "unitId", bookInfo.getUnitId(),
                        "lessonId", bookInfo.getLessonId(),
                        "unitGroupId", bookInfo.getUnitGroupId(),
                        "sectionId", bookInfo.getSectionId()
                ));
            }
            eids.add(qid);
        }
        if (objectiveConfigType == ObjectiveConfigType.DUBBING) {
            Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(videoId);
            List<String> questionIds = newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
            Map<String, NewQuestion> dubbingQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            if (dubbing != null && MapUtils.isNotEmpty(dubbingQuestionMap)) {
                List<Map<String, Object>> sentenceList = new ArrayList<>();
                dubbingQuestionMap.values()
                        .forEach(question -> {
                            NewQuestionOralDictOptions options = null;
                            NewQuestionsSubContents subContents = question.getContent().getSubContents().get(0);
                            if (subContents != null && subContents.getOralDict() != null && CollectionUtils.isNotEmpty(subContents.getOralDict().getOptions())) {
                                options = subContents.getOralDict().getOptions().get(0);
                            }
                            if (options != null) {
                                sentenceList.add(MapUtils.m(
                                        "sentenceChineseContent", options.getCnText(),
                                        "sentenceEnglishContent", options.getText(),
                                        "sentenceVideoStart", options.getVoiceStart(),
                                        "sentenceVideoEnd", options.getVoiceEnd(),
                                        "questionId", question.getId()
                                ));
                            }
                        });
                questionMap.put("dubbingId", videoId);
                questionMap.put("dubbingName", dubbing.getVideoName());
                questionMap.put("videoUrl", dubbing.getVideoUrl());
                questionMap.put("backgroundMusicUrl", dubbing.getBackgroundMusic());
                questionMap.put("coverImgUrl", dubbing.getCoverUrl());
                questionMap.put("sentenceList", sentenceList);
            }
        }
        if (objectiveConfigType == ObjectiveConfigType.LEVEL_READINGS) {
            List<NewHomeworkQuestion> examQuestions = liveCastHomework.findNewHomeworkQuestions(objectiveConfigType, videoId);
            List<NewHomeworkQuestion> oralQuestions = liveCastHomework.findNewHomeworkOralQuestions(objectiveConfigType, videoId);
            questionMap.put("examQuestionIds", examQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
            questionMap.put("oralQuestionIds", oralQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
        } else {
            questionMap.put("examUnitMap", examUnitMap);
            questionMap.put("normalTime", normalTime);
            questionMap.put("eids", eids);
        }
        return questionMap;
    }

    @Deprecated
    @Override
    public Map<String, Object> loadHomeworkQuestionsAnswer(ObjectiveConfigType objectiveConfigType, String homeworkId, Long studentId) {
        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> questionAnswerMap = new HashMap<>();
        if (StringUtils.isNotBlank(homeworkId) && studentId != null) {
            LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
            if (liveCastHomework == null) {
                return Collections.emptyMap();
            }
            LiveCastHomeworkResult liveCastHomeworkResult = newHomeworkLivecastLoader.loadLiveCastHomeworkResult(liveCastHomework.toLocation(), studentId);
            if (liveCastHomeworkResult == null) {
                return Collections.emptyMap();
            }

            Collection<String> resultIds = liveCastHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(objectiveConfigType);

            Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultDao.loads(resultIds);
            if (MapUtils.isNotEmpty(liveCastHomeworkProcessResultMap)) {
                Collection<LiveCastHomeworkProcessResult> processResults = liveCastHomeworkProcessResultMap.values();
                for (LiveCastHomeworkProcessResult processResult : processResults) {
                    String key = processResult.getQuestionId();
                    Map<String, Object> value = MiscUtils.m(
                            "files", processResult.getFiles(),
                            "subMaster", processResult.getSubGrasp(),
                            "master", processResult.getGrasp(),
                            "userAnswers", processResult.getUserAnswers(),
                            "fullScore", processResult.getStandardScore(),
                            "score", processResult.getScore()
                    );
                    questionAnswerMap.put(key, value);
                }
            }
        }
        return questionAnswerMap;
    }

    @Override
    public Map<String, Object> loadHomeworkQuestionsAnswer(ObjectiveConfigType objectiveConfigType, String homeworkId, Long studentId, Integer categoryId, String lessonId, String videoId) {
        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }

        if (objectiveConfigType == ObjectiveConfigType.BASIC_APP && (categoryId == null || categoryId == 0 || StringUtils.isBlank(lessonId))) {
            return Collections.emptyMap();
        }


        Map<String, Object> questionAnswerMap = new HashMap<>();
        if (StringUtils.isNotBlank(homeworkId) && studentId != null) {
            LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(homeworkId);
            if (liveCastHomework == null) {
                return Collections.emptyMap();
            }
            LiveCastHomeworkResult liveCastHomeworkResult = newHomeworkLivecastLoader.loadLiveCastHomeworkResult(liveCastHomework.toLocation(), studentId);
            if (liveCastHomeworkResult == null) {
                return Collections.emptyMap();
            }

            // 趣味配音这个接口不用查process，直接返回配音的相关信息
            if (ObjectiveConfigType.DUBBING == objectiveConfigType) {
                Map<ObjectiveConfigType, NewHomeworkResultAnswer> newHomeworkResultAnswerMap = liveCastHomeworkResult.getPractices();
                if (MapUtils.isEmpty(newHomeworkResultAnswerMap) || !newHomeworkResultAnswerMap.containsKey(ObjectiveConfigType.DUBBING)) {
                    return Collections.emptyMap();
                }
                NewHomeworkResultAnswer answer = newHomeworkResultAnswerMap.get(ObjectiveConfigType.DUBBING);
                if (MapUtils.isNotEmpty(answer.getAppAnswers()) && answer.getAppAnswers().containsKey(videoId)) {
                    NewHomeworkResultAppAnswer appAnswer = answer.getAppAnswers().get(videoId);
                    Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(videoId);
                    if (dubbing != null && MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                        return MapUtils.m(
                                "dubbingId", videoId,
                                "dubbingName", dubbing.getVideoName(),
                                "coverImgUrl", dubbing.getCoverUrl(),
                                "dubbingVideoUrl", appAnswer.getVideoUrl(),
                                "sentenceCount", appAnswer.getAnswers().size()
                        );
                    }
                }
                return Collections.emptyMap();
            }

            Collection<String> resultIds;
            if (objectiveConfigType == ObjectiveConfigType.BASIC_APP) {
                resultIds = liveCastHomeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(String.valueOf(categoryId), lessonId, objectiveConfigType);
            } else if (objectiveConfigType == ObjectiveConfigType.READING || objectiveConfigType == ObjectiveConfigType.LEVEL_READINGS) {
                // 根据configType类型，复用videoId
                resultIds = liveCastHomeworkResult.findHomeworkProcessIdsForReading(videoId, objectiveConfigType);
            } else {
                resultIds = liveCastHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(objectiveConfigType);
            }


            Map<String, LiveCastHomeworkProcessResult> liveCastHomeworkProcessResultMap = liveCastHomeworkProcessResultDao.loads(resultIds);
            if (MapUtils.isNotEmpty(liveCastHomeworkProcessResultMap)) {

                Collection<LiveCastHomeworkProcessResult> processResults = liveCastHomeworkProcessResultMap.values();
                for (LiveCastHomeworkProcessResult processResult : processResults) {
                    String key = processResult.getQuestionId();

                    List<List<String>> oralAudios = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(processResult.getOralDetails())) {

                        List<List<BaseHomeworkProcessResult.OralDetail>> oralDetailList = processResult.getOralDetails();
                        for (List<BaseHomeworkProcessResult.OralDetail> list1 : oralDetailList) {
                            List<String> audios = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(list1)) {
                                for (BaseHomeworkProcessResult.OralDetail oralDetail : list1) {
                                    audios.add(oralDetail.getAudio());
                                }
                            }
                            if (CollectionUtils.isNotEmpty(audios)) {
                                oralAudios.add(audios);
                            }
                        }
                    }

                    Set<String> imgList = new LinkedHashSet<>();
                    Set<String> correctList = new LinkedHashSet<>();
                    Set<String> correctVoiceList=new LinkedHashSet<>();
                    if (CollectionUtils.isNotEmpty(processResult.getFiles())) {
                        for (List<NewHomeworkQuestionFile> fileList : processResult.getFiles()) {
                            if (CollectionUtils.isNotEmpty(fileList)) {
                                for (NewHomeworkQuestionFile file : fileList) {
                                    imgList.add("https://oss-image.17zuoye.com/" + file.getRelativeUrl());
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(processResult.getCorrectionImg())) {
                        String[] correctImgs = StringUtils.split(processResult.getCorrectionImg(), ",");
                        if (correctImgs != null && correctImgs.length != 0) {
                            correctList.addAll(Arrays.asList(correctImgs));
                        }
                    }
                    String[] correctVoices = StringUtils.split(processResult.getCorrectionVoice(), ",");
                    if (ArrayUtils.isNotEmpty(correctVoices)) {
                        correctVoiceList.addAll(Arrays.asList(correctVoices));
                    }
                    List<List<NewHomeworkQuestionFile>> files = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(processResult.getFiles()) && StringUtils.isNotBlank(processResult.getCorrectionImg())) {
                        for (List<NewHomeworkQuestionFile> fileList : processResult.getFiles()) {
                            List<NewHomeworkQuestionFile> tempFile = new ArrayList<>();
                            for (NewHomeworkQuestionFile file : fileList) {
                                if (liveCastHomework.getType() == NewHomeworkType.YiQiXue) {
                                    file.setRelativeUrl(processResult.getCorrectionImg());
                                    tempFile.add(file);
                                } else {
                                    file.setRelativeUrl(processResult.getCorrectionImg());
                                    tempFile.add(file);
                                    // 这里也可以把原图一起塞进去
                                    // NewHomeworkQuestionFile orig = new NewHomeworkQuestionFile();
                                    // orig.setRelativeUrl(NewHomeworkQuestionFileHelper.getFileUrl(file));
                                    // tempFile.add(orig);
                                }
                            }
                            files.add(tempFile);
                        }
                    }

                    Map<String, Object> value = MapUtils.m(
                            "oralAudios", oralAudios,
                            "files", files,
                            "subMaster", processResult.getSubGrasp(),
                            "master", processResult.getGrasp(),
                            "userAnswers", processResult.getUserAnswers(),
                            "fullScore", processResult.getStandardScore(),
                            "score", processResult.getScore(),
                            "oralScoreLevel", processResult.getAppOralScoreLevel(),
                            "imgList", imgList,
                            "correctList", correctList,
                            "correctVoices", correctVoices
                    );
                    questionAnswerMap.put(key, value);
                }
            }
        }
        return questionAnswerMap;
    }

    private MapMessage homeworkForBasicAppResult(ObjectiveConfigType objectiveConfigType, LiveCastHomeworkResult liveCastHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = liveCastHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该练习类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        List<String> lessonIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            processResultIds.addAll(nraa.getAnswers().values());
            if (StringUtils.isNotBlank(nraa.getLessonId())) {
                lessonIds.add(nraa.getLessonId());
            }
        }
        Map<String, List<String>> unitLessonsMap = new LinkedHashMap<>();
        Map<String, String> lessonUnitMap = handle(lessonIds);
        for (Map.Entry<String, String> lessonUnitEntry : lessonUnitMap.entrySet()) {
            List<String> lids = unitLessonsMap.get(lessonUnitEntry.getValue());
            if (CollectionUtils.isEmpty(lids)) {
                lids = new ArrayList<>();
            }
            lids.add(lessonUnitEntry.getKey());
            unitLessonsMap.put(lessonUnitEntry.getValue(), lids);
        }

        Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(processResultIds);
        Map<String, List<Map<String, Object>>> lessonCategoryMap = new HashMap<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            String lessonId = nraa.getLessonId();
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(SafeConverter.toLong(nraa.getPracticeId()));
            if (practiceType != null) {
                int errorCount = 0;
                int rightCount = 0;
                boolean finished = nraa.isFinished();
                for (String processResultId : nraa.getAnswers().values()) {
                    LiveCastHomeworkProcessResult processResult = processResultMap.get(processResultId);
                    if (processResult == null) continue;
                    if (practiceType.getNeedRecord()) {
                        if ((processResult.getAppOralScoreLevel() != null && !AppOralScoreLevel.D.equals(processResult.getAppOralScoreLevel()))
                                || (processResult.getAppOralScoreLevel() == null && processResult.getScore() >= 40)) {
                            rightCount++;
                        } else {
                            errorCount++;
                        }
                    } else {
                        if (SafeConverter.toBoolean(processResult.getGrasp())) {
                            rightCount++;
                        } else {
                            errorCount++;
                        }
                    }
                }
                List<Map<String, Object>> categorys = lessonCategoryMap.get(lessonId);
                if (CollectionUtils.isEmpty(categorys)) {
                    categorys = new ArrayList<>();
                }
                categorys.add(
                        MapUtils.m(
                                "catetoryName", practiceType.getCategoryName(),
                                "needRecord", practiceType.getNeedRecord(),
                                "rightCount", rightCount,
                                "errorCount", errorCount,
                                "finished", finished)
                );
                lessonCategoryMap.put(lessonId, categorys);
            }
        }
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitLessonsMap.keySet());
        List<Map<String, Object>> results = new ArrayList<>();
        handleUnitLessonsMap(unitLessonsMap,
                unitMap,
                lessonMap,
                lessonCategoryMap,
                results);
        return MapMessage.successMessage().add("datas", results);
    }

    private MapMessage homeworkForReadingResult(ObjectiveConfigType objectiveConfigType, LiveCastHomeworkResult liveCastHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = liveCastHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该练习类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        List<String> readingIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            processResultIds.addAll(nraa.getAnswers().values());
            readingIds.add(nraa.getPictureBookId());
        }
        Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(processResultIds);
        Map<String, PictureBook> readingMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(readingIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int totalRightCount = 0;
        int totalErrorCount = 0;
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            PictureBook reading = readingMap.get(nraa.getPictureBookId());
            if (reading != null) {
                int errorCount = 0;
                int rightCount = 0;
                for (String processResultId : nraa.getAnswers().values()) {
                    LiveCastHomeworkProcessResult processResult = processResultMap.get(processResultId);
                    if (processResult == null) continue;
                    if (SafeConverter.toBoolean(processResult.getGrasp())) {
                        rightCount++;
                        totalRightCount++;
                    } else {
                        errorCount++;
                        totalErrorCount++;
                    }
                }
                results.add(
                        MapUtils.m(
                                "readingName", reading.getName(),
                                "rightCount", rightCount,
                                "errorCount", errorCount)
                );
            }
        }
        return MapMessage.successMessage()
                .add("datas", results)
                .add("rightCount", totalRightCount)
                .add("errorCount", totalErrorCount);
    }

    private MapMessage homeworkForLevelReadingsResult(ObjectiveConfigType objectiveConfigType, LiveCastHomeworkResult liveCastHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = liveCastHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该练习类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        List<String> readingIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            if (MapUtils.isNotEmpty(nraa.getAnswers())) {
                processResultIds.addAll(nraa.getAnswers().values());
            }
            readingIds.add(nraa.getPictureBookId());
        }
        Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(processResultIds);
        Map<String, PictureBookPlus> readingMap = pictureBookPlusServiceClient.loadByIds(readingIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int totalRightCount = 0;
        int totalErrorCount = 0;
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            PictureBookPlus reading = readingMap.get(nraa.getPictureBookId());
            if (reading != null) {
                int errorCount = 0;
                int rightCount = 0;
                for (String processResultId : nraa.getAnswers().values()) {
                    LiveCastHomeworkProcessResult processResult = processResultMap.get(processResultId);
                    if (processResult == null) continue;
                    if (SafeConverter.toBoolean(processResult.getGrasp())) {
                        rightCount++;
                        totalRightCount++;
                    } else {
                        errorCount++;
                        totalErrorCount++;
                    }
                }
                results.add(
                        MapUtils.m(
                                "readingName", reading.getEname(),
                                "rightCount", rightCount,
                                "errorCount", errorCount)
                );
            }
        }
        return MapMessage.successMessage()
                .add("datas", results)
                .add("rightCount", totalRightCount)
                .add("errorCount", totalErrorCount);
    }

    private MapMessage homeworkForDubbingResult(ObjectiveConfigType objectiveConfigType, LiveCastHomeworkResult liveCastHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = liveCastHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(liveCastHomeworkResult.getHomeworkId());
        if(liveCastHomework == null){
            return MapMessage.errorMessage().setInfo("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = resultAnswer.getAppAnswers();
        List<String> dubbingIds = appAnswers.values()
                .stream()
                .map(NewHomeworkResultAppAnswer::getDubbingId)
                .collect(Collectors.toList());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);
        // 生成合成配音ids
        String homeworkId = liveCastHomeworkResult.getHomeworkId();
        Long studentId = liveCastHomeworkResult.getUserId();
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dubbingIds)) {
            for (String dubbingId : dubbingIds) {
                ids.add(new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString());
            }
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(ids);
        List<Map<String, Object>> results = new ArrayList<>();
        for (NewHomeworkResultAppAnswer appAnswer : appAnswers.values()) {
            String dubbingId = appAnswer.getDubbingId();
            Dubbing dubbing = dubbingMap.get(dubbingId);
            String id = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
            DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(id);
            // 合成配音是否成功
            Boolean synthetic = dubbingSyntheticHistory == null || SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(liveCastHomework.getCreateAt()));
            results.add(MapUtils.m(
                    "videoName", dubbing != null ? dubbing.getVideoName() : "",
                    "videoUrl", appAnswer.getVideoUrl(),
                    "synthetic", synthetic));
        }
        return MapMessage.successMessage().add("datas", results);
    }


    private MapMessage homeworkForExamResult(ObjectiveConfigType objectiveConfigType, LiveCastHomeworkResult liveCastHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = liveCastHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该练习类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        Collection<String> processResultIds = resultAnswer.getAnswers().values();
        Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(processResultIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int rightCount = 0;
        int errorCount = 0;
        for (String processResultId : resultAnswer.getAnswers().values()) {
            LiveCastHomeworkProcessResult liveCastHomeworkProcessResult = processResultMap.get(processResultId);
            if (liveCastHomeworkProcessResult == null) continue;
            if (SafeConverter.toBoolean(liveCastHomeworkProcessResult.getGrasp())) {
                rightCount++;
            } else {
                errorCount++;
            }
            results.add(MapUtils.m(
                    "questionId", liveCastHomeworkProcessResult.getQuestionId(),
                    "score", new BigDecimal(SafeConverter.toDouble(liveCastHomeworkProcessResult.getScore())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                    "right", SafeConverter.toBoolean(liveCastHomeworkProcessResult.getGrasp())));
        }
        return MapMessage.successMessage()
                .add("datas", results)
                .add("rightCount", rightCount)
                .add("errorCount", errorCount)
                .add("duration", resultAnswer.getDuration());
    }

    private void handleUnitLessonsMap(Map<String, List<String>> unitLessonsMap,
                                      Map<String, NewBookCatalog> unitMap,
                                      Map<String, NewBookCatalog> lessonMap,
                                      Map<String, List<Map<String, Object>>> lessonCategorysMap,
                                      List<Map<String, Object>> results) {
        for (Map.Entry<String, List<String>> unitLessonEntry : unitLessonsMap.entrySet()) {
            NewBookCatalog unit = unitMap.get(unitLessonEntry.getKey());
            List<Map<String, Object>> lessonObjs = new ArrayList<>();
            if (unit == null) continue;
            for (String lessonId : unitLessonEntry.getValue()) {
                NewBookCatalog lesson = lessonMap.get(lessonId);
                List<Map<String, Object>> categorys = lessonCategorysMap.get(lessonId);
                if (lesson == null || categorys == null) continue;
                lessonObjs.add(MapUtils.m("lessonName", lesson.getAlias(),
                        "categorys", categorys));
            }
            if (CollectionUtils.isNotEmpty(lessonObjs)) {
                results.add(MapUtils.m("unitName", unit.getAlias(),
                        "lessons", lessonObjs));
            }
        }
    }

    private Map<String, String> handle(List<String> ids) {
        Map<String, NewBookCatalog> ms = newContentLoaderClient.loadBookCatalogByCatalogIds(ids);
        Map<String, String> data = new LinkedHashMap<>();
        for (String a : ids) {
            NewBookCatalog newBookCatalog = ms.get(a);
            if (newBookCatalog != null) {
                List<NewBookCatalogAncestor> l = newBookCatalog.getAncestors();
                Map<String, NewBookCatalogAncestor> m = l
                        .stream()
                        .collect(Collectors
                                .toMap(NewBookCatalogAncestor::getNodeType, Function.identity()));
                if (m.get("UNIT") != null) {
                    data.put(a, m.get("UNIT").getId());
                }
            }
        }
        return data;
    }

    private void finishHomework(LiveCastHomework homework, LiveCastHomeworkResult homeworkResult, Long studentId) {
        if (homework != null && !homeworkResult.getPractices().isEmpty()) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", studentId,
                    "mod1", homework.getId(),
                    "op", "repair LiveCastHomework result"
            ));
            AlpsThreadPool.getInstance().submit(() -> {
                // 按分组读取班级信息
                Long groupId = homework.getClazzGroupId();
                ThirdPartyGroup group = thirdPartyGroupLoaderClient.loadThirdPartyGroupsIncludeDisabled(Collections.singleton(groupId)).get(groupId);
//                Long clazzId = group == null ? null : group.getClazzId();
                ObjectiveConfigType objectiveConfigType = null;
                for (ObjectiveConfigType type : homeworkResult.getPractices().keySet()) {
                    NewHomeworkResultAnswer ra = homeworkResult.getPractices().get(type);
                    if (!ra.isFinished()) {
                        objectiveConfigType = type;
                        break;
                    }
                }
                // 所有类型都已完成，用最后一个类型来修复数据
                if (objectiveConfigType == null) {
                    for (ObjectiveConfigType type : homeworkResult.getPractices().keySet()) {
                        objectiveConfigType = type;
                    }
                }
                if (objectiveConfigType != null) {
                    if (ObjectiveConfigType.BASIC_APP == objectiveConfigType) {
                        NewHomeworkResultAnswer newHomeworkResultAnswer = homeworkResult.getPractices().get(objectiveConfigType);
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                                if (appAnswer.getFinishAt() == null && appAnswer.getAnswers() != null) {
                                    Collection<String> processIds = appAnswer.getAnswers().values();
                                    Integer categoryId = appAnswer.getCategoryId() != null ? appAnswer.getCategoryId() : 0;
                                    String lessonId = appAnswer.getLessonId();
                                    String key = StringUtils.join(Arrays.asList(categoryId, lessonId), "-");
                                    Double score = 0d;
                                    Long duration = 0L;
                                    if (CollectionUtils.isNotEmpty(processIds)) {
                                        // 布置的题目和做过的题一致，将剩下的属性补全
                                        Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(processIds);
                                        for (LiveCastHomeworkProcessResult npr : processResultMap.values()) {
                                            score += npr.getScore();
                                            duration += npr.getDuration();
                                        }
                                        Double avgScore = score;
                                        Long practiceId = appAnswer.getPracticeId();
                                        if (practiceId == null && MapUtils.isNotEmpty(processResultMap)) {
                                            // 处理新结构丢数据的情况
                                            LiveCastHomeworkProcessResult homeworkProcessResult = processResultMap.values().iterator().next();
                                            practiceId = homeworkProcessResult.getPracticeId();
                                            lessonId = homeworkProcessResult.getLessonId();
                                            categoryId = homeworkProcessResult.getCategoryId();
                                            key = StringUtils.join(Arrays.asList(categoryId, lessonId), "-");
                                            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = new NewHomeworkResultAppAnswer();
                                            newHomeworkResultAppAnswer.setPracticeId(practiceId);
                                            newHomeworkResultAppAnswer.setLessonId(lessonId);
                                            newHomeworkResultAppAnswer.setCategoryId(categoryId);
                                            newHomeworkResultAppAnswer.setAnswers(new LinkedHashMap<>());
                                            newHomeworkResultAppAnswer.setOralAnswers(new LinkedHashMap<>());
                                            liveCastHomeworkResultDao.doHomeworkBasicApp(homework.toLocation(), studentId, objectiveConfigType, key, newHomeworkResultAppAnswer);
                                        }
                                        PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(practiceId);
                                        //跟读题打分是根据引擎分数来的，每句话分数都是100制，所以需要求个平均分
                                        if (practiceType.getNeedRecord()) {
                                            avgScore = new BigDecimal(score).divide(new BigDecimal(processResultMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                        }
                                        liveCastHomeworkResultDao.finishHomeworkBasicAppPractice(homework.toLocation(), studentId, objectiveConfigType, key, avgScore, duration);
                                    } else {
                                        score = 100D;
                                        duration = NewHomeworkUtils.processDuration(0L);
                                        liveCastHomeworkResultDao.finishHomeworkBasicAppPractice(homework.toLocation(), studentId, objectiveConfigType, key, score, duration);
                                    }
                                }
                            }
                        }
                    } else if (ObjectiveConfigType.READING.equals(objectiveConfigType)) {
                        NewHomeworkResultAnswer newHomeworkResultAnswer = homeworkResult.getPractices().get(objectiveConfigType);
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                                if (appAnswer.getFinishAt() == null && MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                                    String pictureBookId = appAnswer.getPictureBookId();
                                    Set<String> processIds = new HashSet<>(appAnswer.getAnswers().values());
                                    Set<String> oralProcessIds = new HashSet<>();
                                    if (MapUtils.isNotEmpty(appAnswer.getOralAnswers())) {
                                        processIds.addAll(appAnswer.getOralAnswers().values());
                                        oralProcessIds.addAll(appAnswer.getOralAnswers().values());
                                    }
                                    if (pictureBookId == null) {
                                        Double score = 0d;
                                        Long duration = 0L;
                                        Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(processIds);
                                        for (LiveCastHomeworkProcessResult npr : processResultMap.values()) {
                                            if (!oralProcessIds.contains(npr.getId())) {
                                                score += npr.getScore();
                                            }
                                            duration += npr.getDuration();
                                        }
                                        LiveCastHomeworkProcessResult homeworkProcessResult = processResultMap.values().iterator().next();
                                        pictureBookId = homeworkProcessResult.getPictureBookId();

                                        NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = new NewHomeworkResultAppAnswer();
                                        newHomeworkResultAppAnswer.setScore(score);
                                        newHomeworkResultAppAnswer.setDuration(duration);
                                        newHomeworkResultAppAnswer.setPictureBookId(pictureBookId);
                                        newHomeworkResultAppAnswer.setConsumeTime(duration);
                                        newHomeworkResultAppAnswer.setAnswers(new LinkedHashMap<>());
                                        newHomeworkResultAppAnswer.setOralAnswers(new LinkedHashMap<>());
                                        newHomeworkResultAppAnswer.setFinishAt(new Date());
                                        liveCastHomeworkResultDao.doHomeworkBasicApp(homework.toLocation(), studentId, objectiveConfigType, pictureBookId, newHomeworkResultAppAnswer);
                                    }
                                }
                            }
                        }
                    }
                }
                FinishLiveCastHomeworkContext ctx = new FinishLiveCastHomeworkContext();
                ctx.setUserId(studentId);
                ctx.setUser(studentLoaderClient.loadStudent(studentId));
                ctx.setClazzGroupId(groupId);
                ctx.setHomeworkId(homework.getId());
                ctx.setLiveCastHomework(homework);
                ctx.setNewHomeworkType(homework.getNewHomeworkType());
                ctx.setObjectiveConfigType(objectiveConfigType);
                ctx.setClientType("pc");
                ctx.setClientName("pc");
                ctx.setSupplementaryData(true);
                finishLiveCastHomeworkProcessor.process(ctx);
            });
        }
    }

}
