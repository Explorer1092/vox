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

package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookLayoutType;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookPracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookSubPage;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookDraft;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusDubbing;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.PictureBookHomeworkService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.PictureBookPlusDubbingDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLivecastLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016-07-14
 */
@Named
@Service(interfaceClass = PictureBookHomeworkService.class)
@ExposeService(interfaceClass = PictureBookHomeworkService.class)
public class PictureBookHomeworkServiceImpl extends SpringContainerSupport implements PictureBookHomeworkService {

    @Inject private RaikouSystem raikouSystem;

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private PictureBookLoaderClient pictureBookLoaderClient;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private VacationHomeworkDao vacationHomeworkDao;
    @Inject private VacationHomeworkResultDao vacationHomeworkResultDao;

    @Inject private NewHomeworkLivecastLoaderImpl newHomeworkLivecastLoader;
    @Inject private PictureBookPlusDubbingDao pictureBookPlusDubbingDao;
    @Inject private StudentLoaderClient studentLoaderClient;

    // Hard code xuesong.zhang
    private static final String Picture_Book_Practice = "67";

    @Override
    public List<PictureBookSummaryResult> getPictureBookSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId) {
        if (CollectionUtils.isEmpty(picBookIds) || StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return Collections.emptyList();
        }

        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, false);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (newHomeworkResult != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(ObjectiveConfigType.READING) != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.READING).getAppAnswers())
        ) {
            appAnswerMap = newHomeworkResult.getPractices().get(ObjectiveConfigType.READING).getAppAnswers();
        }

        List<PictureBookSummaryResult> resultList = new ArrayList<>();
        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(picBookIds);
        for (String pbid : picBookIds) {
            PictureBookSummaryResult result = new PictureBookSummaryResult();
            PictureBook picBook = pictureBookMap.get(pbid);
            String author = buildAuthorName(picBook.getUgcAuthor());
            result.setName(picBook.getName());
            result.setPracticeId(Picture_Book_Practice);
            result.setAuthor(author);
            result.setFrontCoverPic(picBook.getCoverUrl());
            result.setFrontCoverPicThumb(picBook.getCoverThumbnail1Uri());
            String appUrl = UrlUtils.buildUrlQuery("/flash/loader/newhomework" + Constants.AntiHijackExt, MapUtils.m("practiceId", Picture_Book_Practice, "pictureBookId", pbid, "hid", homeworkId, "newHomeworkType", newHomework.getNewHomeworkType()));
            String appMobileUrl = UrlUtils.buildUrlQuery("/flash/loader/newhomeworkmobile" + Constants.AntiHijackExt, MapUtils.m("practiceId", Picture_Book_Practice, "pictureBookId", pbid, "hid", homeworkId, "newHomeworkType", newHomework.getNewHomeworkType()));
            // videoId，复用key，省得改接口了
            // String questionUrl = UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.READING, "homeworkId", homeworkId, "videoId", pbid));
            String completedUrl = UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.READING, "homeworkId", homeworkId, "videoId", pbid));
            result.setAppUrl(appUrl);
            result.setAppMobileUrl(appMobileUrl);
            // result.setQuestionUrl(questionUrl);
            result.setCompletedUrl(completedUrl);
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(pbid, null);
            if (appAnswer != null && appAnswer.isFinished()) {
                result.setFinished(Boolean.TRUE);
            } else {
                result.setFinished(Boolean.FALSE);
            }

            resultList.add(result);
        }
        return resultList.stream().sorted((s1, s2) -> s2.getFinished().compareTo(s1.getFinished())).collect(Collectors.toList());
    }

    @Override
    public List<PictureBookSummaryResult> getVacationPictureBookSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId) {
        if (CollectionUtils.isEmpty(picBookIds) || StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (vacationHomework == null) {
            return Collections.emptyList();
        }

        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (vacationHomeworkResult != null
                && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices())
                && vacationHomeworkResult.getPractices().get(ObjectiveConfigType.READING) != null
                && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices().get(ObjectiveConfigType.READING).getAppAnswers())
        ) {
            appAnswerMap = vacationHomeworkResult.getPractices().get(ObjectiveConfigType.READING).getAppAnswers();
        }

        List<PictureBookSummaryResult> resultList = new ArrayList<>();
        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(picBookIds);
        for (String pbid : picBookIds) {
            PictureBookSummaryResult result = new PictureBookSummaryResult();
            PictureBook picBook = pictureBookMap.get(pbid);
            String author = buildAuthorName(picBook.getUgcAuthor());
            result.setName(picBook.getName());
            result.setPracticeId(Picture_Book_Practice);
            result.setAuthor(author);
            result.setFrontCoverPic(picBook.getCoverUrl());
            result.setFrontCoverPicThumb(picBook.getCoverThumbnail1Uri());
            String appUrl = UrlUtils.buildUrlQuery("/flash/loader/newhomework" + Constants.AntiHijackExt, MiscUtils.m("practiceId", Picture_Book_Practice, "pictureBookId", pbid, "hid", homeworkId, "newHomeworkType", vacationHomework.getNewHomeworkType()));
            String appMobileUrl = UrlUtils.buildUrlQuery("/flash/loader/newhomeworkmobile" + Constants.AntiHijackExt, MiscUtils.m("practiceId", Picture_Book_Practice, "pictureBookId", pbid, "hid", homeworkId, "newHomeworkType", vacationHomework.getNewHomeworkType()));
            String completedUrl = UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.READING, "homeworkId", homeworkId, "videoId", pbid));
            result.setAppUrl(appUrl);
            result.setAppMobileUrl(appMobileUrl);
            result.setCompletedUrl(completedUrl);
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(pbid, null);
            if (appAnswer != null && appAnswer.isFinished()) {
                result.setFinished(Boolean.TRUE);
            } else {
                result.setFinished(Boolean.FALSE);
            }

            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public List<PictureBookPlusSummaryResult> getPictureBookPlusSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId) {
        if (CollectionUtils.isEmpty(picBookIds) || StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return Collections.emptyList();
        }

        Subject subject = newHomework.getSubject();
        boolean isEnglish = Subject.ENGLISH == subject;

        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, false);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (newHomeworkResult != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS) != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS).getAppAnswers())
        ) {
            appAnswerMap = newHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS).getAppAnswers();
        }
        List<PictureBookPlusSummaryResult> resultList = new ArrayList<>();
        Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(picBookIds);
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookLoaderClient.loadAllPictureBookSeries()
                .stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookLoaderClient.loadAllPictureBookTopics()
                .stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        Map<String, NewHomeworkApp> newHomeworkAppMap = new HashMap<>();
        NewHomeworkPracticeContent practiceContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.LEVEL_READINGS);
        if (practiceContent != null && CollectionUtils.isNotEmpty(practiceContent.getApps())) {
            for (NewHomeworkApp newHomeworkApp : practiceContent.getApps()) {
                if (StringUtils.isNotBlank(newHomeworkApp.getPictureBookId())) {
                    newHomeworkAppMap.put(newHomeworkApp.getPictureBookId(), newHomeworkApp);
                }
            }
        }

        for (String pbId : picBookIds) {
            PictureBookPlusSummaryResult result = new PictureBookPlusSummaryResult();
            PictureBookPlus picBook = pictureBookPlusMap.get(pbId);
            result.setPictureBookId(pbId);
            result.setPictureBookName(picBook.pbName());
            result.setWordsCount(picBook.getWordsLength());

            PictureBookNewClazzLevel clazzLevel = null;
            if (CollectionUtils.isNotEmpty(picBook.getNewClazzLevels())) {
                clazzLevel = picBook.getNewClazzLevels().get(0);
            }
            if (Objects.equals(Subject.CHINESE.getId(), picBook.getSubjectId())) {
                result.setLevel(clazzLevel == null ? "" : NewHomeworkUtils.processChinesePictureBookClazzLevel(clazzLevel.name()));
            } else {
                result.setLevel(clazzLevel == null ? "" : clazzLevel.name());
            }

            List<Map<String, Object>> keyWords = picBook.allNewWords()
                    .stream()
                    .map(word -> MapUtils.m("enText", word.getEntext(), "cnText", word.getCntext()))
                    .collect(Collectors.toList());
            result.setKeyWords(keyWords);

            String seriesId = picBook.getSeriesId();
            String seriesName = "";
            if (pictureBookSeriesMap.containsKey(seriesId)) {
                seriesName = pictureBookSeriesMap.get(seriesId).getName();
            }
            result.setSeries(seriesName);

            List<String> pictureBookTopicIdList = picBook.getTopicIds();
            List<String> pictureBookTopicNameList = Collections.emptyList();
            if (CollectionUtils.isNotEmpty(pictureBookTopicIdList)) {
                pictureBookTopicNameList = pictureBookTopicIdList.stream()
                        .filter(pictureBookTopicMap::containsKey)
                        .map(id -> pictureBookTopicMap.get(id).getName())
                        .collect(Collectors.toList());
            }
            result.setTopics(pictureBookTopicNameList);

            // 默认一定会有阅读，高频词练习这两个类型
            List<PictureBookPracticeType> pictureBookPracticeTypes = new ArrayList<>();
            pictureBookPracticeTypes.add(PictureBookPracticeType.READING);
            if (isEnglish) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.WORDS);
            }
            NewHomeworkApp newHomeworkApp = newHomeworkAppMap.get(pbId);
            if (newHomeworkApp == null) {
                continue;
            }
            // 计算绘本时长
            // 阅读模块 + 高频词模块时长
            int seconds = SafeConverter.toInt(picBook.getRecommendTime(), 300) + 10 * picBook.allOftenUsedWords().size();

            // 跟读
            if (isEnglish && CollectionUtils.isNotEmpty(newHomeworkApp.getOralQuestions())) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.ORAL);
                seconds += SafeConverter.toInt(picBook.getOralSeconds(), 300);
            }
            // 练习
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getQuestions())) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.EXAM);
                seconds += 20 * newHomeworkApp.getQuestions().size();
            }
            // 配音
            if (isEnglish && newHomeworkApp.containsDubbing()) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.DUBBING);
                seconds += SafeConverter.toInt(picBook.getPredictedDubbingTime(), 300);
            }

            List<Map<String, Object>> practiceTypes = pictureBookPracticeTypes.stream()
                    .map(practiceType -> MapUtils.m("type", practiceType.name(), "typeName", practiceType.getTypeName()))
                    .collect(Collectors.toList());
            result.setPracticeTypes(practiceTypes);
            result.setSeconds(seconds);

            result.setSpellingPb(picBook.getIsSpellingPb());
            result.setCoverUrl(picBook.getCoverUrl());
            result.setScreenMode(picBook.getScreenMode());
            result.setBookSummary(picBook.getBookSummary());

            String questionUrl = UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS, "homeworkId", homeworkId, "videoId", pbId, "sid", studentId));
            String completedUrl = UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS, "homeworkId", homeworkId, "videoId", pbId, "sid", studentId));
            result.setQuestionUrl(questionUrl);
            result.setCompletedUrl(completedUrl);

            result.setAppDataUrl(UrlUtils.buildUrlQuery("/appdata/obtain/picturebook" + Constants.AntiHijackExt, MapUtils.m("pictureBookId", pbId)));
            result.setProcessResultUrl(UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MapUtils.m("sid", studentId)));
            result.setUploadDubbingUrl(UrlUtils.buildUrlQuery("/exam/flash/picturebookplus/uploaddubbing" + Constants.AntiHijackExt, MapUtils.m("sid", studentId)));
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(pbId, null);
            if (appAnswer != null && appAnswer.isFinished()) {
                result.setFinished(Boolean.TRUE);
                result.setScore(new BigDecimal(appAnswer.getScore()).setScale(0, RoundingMode.HALF_UP).intValue());
                if (newHomeworkApp.containsDubbing()) {
                    result.setDubbingId(appAnswer.getDubbingId());
                }
            } else {
                result.setFinished(Boolean.FALSE);
            }

            resultList.add(result);
        }
        return resultList.stream().sorted((s1, s2) -> s2.getFinished().compareTo(s1.getFinished())).collect(Collectors.toList());
    }

    @Override
    public List<PictureBookPlusSummaryResult> getVacationPictureBookPlusSummaryInfo(String homeworkId, Collection<String> picBookIds, Long studentId) {
        if (CollectionUtils.isEmpty(picBookIds) || StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (vacationHomework == null) {
            return Collections.emptyList();
        }

        Subject subject = vacationHomework.getSubject();
        boolean isEnglish = Subject.ENGLISH == subject;

        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (vacationHomeworkResult != null
                && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices())
                && vacationHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS) != null
                && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS).getAppAnswers())
        ) {
            appAnswerMap = vacationHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS).getAppAnswers();
        }
        List<PictureBookPlusSummaryResult> resultList = new ArrayList<>();
        Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(picBookIds);
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookLoaderClient.loadAllPictureBookSeries()
                .stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookLoaderClient.loadAllPictureBookTopics()
                .stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        Map<String, NewHomeworkApp> newHomeworkAppMap = new HashMap<>();
        NewHomeworkPracticeContent practiceContent = vacationHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.LEVEL_READINGS);
        if (practiceContent != null && CollectionUtils.isNotEmpty(practiceContent.getApps())) {
            for (NewHomeworkApp newHomeworkApp : practiceContent.getApps()) {
                if (StringUtils.isNotBlank(newHomeworkApp.getPictureBookId())) {
                    newHomeworkAppMap.put(newHomeworkApp.getPictureBookId(), newHomeworkApp);
                }
            }
        }

        for (String pbId : picBookIds) {
            PictureBookPlusSummaryResult result = new PictureBookPlusSummaryResult();
            PictureBookPlus picBook = pictureBookPlusMap.get(pbId);
            result.setPictureBookId(pbId);
            result.setPictureBookName(picBook.pbName());
            result.setWordsCount(picBook.getWordsLength());

            PictureBookNewClazzLevel clazzLevel = null;
            if (CollectionUtils.isNotEmpty(picBook.getNewClazzLevels())) {
                clazzLevel = picBook.getNewClazzLevels().get(0);
            }
            result.setLevel(clazzLevel == null ? "" : clazzLevel.name());

            List<Map<String, Object>> keyWords = picBook.allNewWords()
                    .stream()
                    .map(word -> MapUtils.m("enText", word.getEntext(), "cnText", word.getCntext()))
                    .collect(Collectors.toList());
            result.setKeyWords(keyWords);

            String seriesId = picBook.getSeriesId();
            String seriesName = "";
            if (pictureBookSeriesMap.containsKey(seriesId)) {
                seriesName = pictureBookSeriesMap.get(seriesId).getName();
            }
            result.setSeries(seriesName);

            List<String> pictureBookTopicIdList = picBook.getTopicIds();
            List<String> pictureBookTopicNameList = Collections.emptyList();
            if (CollectionUtils.isNotEmpty(pictureBookTopicIdList)) {
                pictureBookTopicNameList = pictureBookTopicIdList.stream()
                        .filter(pictureBookTopicMap::containsKey)
                        .map(id -> pictureBookTopicMap.get(id).getName())
                        .collect(Collectors.toList());
            }
            result.setTopics(pictureBookTopicNameList);

            // 默认一定会有阅读，高频词练习这两个类型
            List<PictureBookPracticeType> pictureBookPracticeTypes = new ArrayList<>();
            pictureBookPracticeTypes.add(PictureBookPracticeType.READING);
            if (isEnglish) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.WORDS);
            }
            NewHomeworkApp newHomeworkApp = newHomeworkAppMap.get(pbId);
            if (newHomeworkApp == null) {
                continue;
            }
            // 计算绘本时长
            // 阅读模块 + 高频词模块时长
            int seconds = SafeConverter.toInt(picBook.getRecommendTime(), 300) + 10 * picBook.allOftenUsedWords().size();

            // 跟读
            if (isEnglish && CollectionUtils.isNotEmpty(newHomeworkApp.getOralQuestions())) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.ORAL);
                seconds += SafeConverter.toInt(picBook.getOralSeconds(), 300);
            }
            // 练习
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getQuestions())) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.EXAM);
                seconds += 20 * newHomeworkApp.getQuestions().size();
            }
            // 配音
            if (isEnglish && newHomeworkApp.containsDubbing()) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.DUBBING);
                seconds += SafeConverter.toInt(picBook.getPredictedDubbingTime(), 300);
            }

            List<Map<String, Object>> practiceTypes = pictureBookPracticeTypes.stream()
                    .map(practiceType -> MapUtils.m("type", practiceType.name(), "typeName", practiceType.getTypeName()))
                    .collect(Collectors.toList());
            result.setPracticeTypes(practiceTypes);
            result.setSeconds(seconds);

            result.setSpellingPb(picBook.getIsSpellingPb());
            result.setCoverUrl(picBook.getCoverUrl());
            result.setScreenMode(picBook.getScreenMode());
            result.setBookSummary(picBook.getBookSummary());

            String questionUrl = UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS, "homeworkId", homeworkId, "videoId", pbId));
            String completedUrl = UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS, "homeworkId", homeworkId, "videoId", pbId));
            result.setQuestionUrl(questionUrl);
            result.setCompletedUrl(completedUrl);

            result.setAppDataUrl(UrlUtils.buildUrlQuery("/appdata/obtain/picturebook" + Constants.AntiHijackExt, MapUtils.m("pictureBookId", pbId)));
            result.setProcessResultUrl("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt);
            result.setUploadDubbingUrl("/exam/flash/picturebookplus/uploaddubbing" + Constants.AntiHijackExt);
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(pbId, null);
            if (appAnswer != null && appAnswer.isFinished()) {
                result.setFinished(Boolean.TRUE);
                result.setScore(new BigDecimal(appAnswer.getScore()).setScale(0, RoundingMode.HALF_UP).intValue());
                if (newHomeworkApp.containsDubbing()) {
                    result.setDubbingId(appAnswer.getDubbingId());
                }
            } else {
                result.setFinished(Boolean.FALSE);
            }

            resultList.add(result);
        }
        return resultList.stream().sorted((s1, s2) -> s2.getFinished().compareTo(s1.getFinished())).collect(Collectors.toList());
    }

    @Override
    public MapMessage getPictureBookPlusDraft(String picBookId) {
        PictureBookPlus pictureBookPlus = pictureBookPlusServiceClient.loadById(picBookId);
        if (pictureBookPlus == null) {
            return MapMessage.errorMessage("绘本不存在");
        }
        Subject subject = Subject.fromSubjectId(pictureBookPlus.getSubjectId());
        Set<PictureBookPlus.SentenceWord> allOftenUsedWords = pictureBookPlus.allOftenUsedWords();
        if (Subject.ENGLISH == subject && CollectionUtils.isEmpty(allOftenUsedWords)) {
            return MapMessage.errorMessage("绘本高频词为空");
        }
        List<PictureBookPlus.Content> contents = pictureBookPlus.getContents();
        if (CollectionUtils.isEmpty(contents)) {
            return MapMessage.errorMessage("绘本内容为空");
        }
        List<PictureBookPlus.Content> sortedContents = contents.stream()
                .sorted(Comparator.comparingInt(a -> SafeConverter.toInt(a.getRank())))
                .collect(Collectors.toList());
        Map<String, Object> pictureBookPlusDraft = JsonUtils.safeConvertObjectToMap(pictureBookPlus);
        pictureBookPlusDraft.put("wordList", allOftenUsedWords);
        pictureBookPlusDraft.put("contents", sortedContents);
        return MapMessage.successMessage().add("pictureBook", pictureBookPlusDraft);
    }

    @Override
    public List<PictureBookSummaryResult> getLiveCastPictureBookSummaryInfo(LiveCastHomework liveCastHomework, Collection<String> picBookIds, Long studentId, String token) {
        if (CollectionUtils.isEmpty(picBookIds) || liveCastHomework == null) {
            return Collections.emptyList();
        }

        LiveCastHomeworkResult homeworkResult = newHomeworkLivecastLoader.loadLiveCastHomeworkResult(liveCastHomework.toLocation(), studentId);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (homeworkResult != null
                && MapUtils.isNotEmpty(homeworkResult.getPractices())
                && homeworkResult.getPractices().get(ObjectiveConfigType.READING) != null
                && MapUtils.isNotEmpty(homeworkResult.getPractices().get(ObjectiveConfigType.READING).getAppAnswers())
        ) {
            appAnswerMap = homeworkResult.getPractices().get(ObjectiveConfigType.READING).getAppAnswers();
        }

        String homeworkId = liveCastHomework.getId();
        List<PictureBookSummaryResult> resultList = new ArrayList<>();
        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(picBookIds);
        for (String pbid : picBookIds) {
            PictureBookSummaryResult result = new PictureBookSummaryResult();
            PictureBook picBook = pictureBookMap.get(pbid);
            String author = buildAuthorName(picBook.getUgcAuthor());
            result.setName(picBook.getName());
            result.setPracticeId(Picture_Book_Practice);
            result.setAuthor(author);
            result.setFrontCoverPic(picBook.getCoverUrl());
            result.setFrontCoverPicThumb(picBook.getCoverThumbnail1Uri());
            String appUrl = UrlUtils.buildUrlQuery("/livecast/student/homework/pc" + Constants.AntiHijackExt,
                    MapUtils.m(
                            "practiceId", Picture_Book_Practice,
                            "pictureBookId", pbid,
                            "hid", homeworkId,
                            "newHomeworkType", liveCastHomework.getNewHomeworkType(),
                            "token", token));

            String appMobileUrl = UrlUtils.buildUrlQuery("/livecast/student/homework/mobile" + Constants.AntiHijackExt,
                    MapUtils.m(
                            "practiceId", Picture_Book_Practice,
                            "pictureBookId", pbid,
                            "hid", homeworkId,
                            "newHomeworkType", liveCastHomework.getNewHomeworkType(),
                            "token", token));

            String completedUrl = UrlUtils.buildUrlQuery("/livecast/student/homework/questions/answer" + Constants.AntiHijackExt,
                    MapUtils.m("objectiveConfigType", ObjectiveConfigType.READING,
                            "homeworkId", homeworkId,
                            "videoId", pbid,
                            "token", token));

            result.setAppUrl(appUrl);
            result.setAppMobileUrl(appMobileUrl);
            result.setCompletedUrl(completedUrl);
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(pbid, null);
            if (appAnswer != null && appAnswer.isFinished()) {
                result.setFinished(Boolean.TRUE);
            } else {
                result.setFinished(Boolean.FALSE);
            }
            resultList.add(result);
        }
        return resultList.stream().sorted((s1, s2) -> s2.getFinished().compareTo(s1.getFinished())).collect(Collectors.toList());
    }

    @Override
    public List<PictureBookPlusSummaryResult> getLiveCastPictureBookPlusSummaryInfo(LiveCastHomework liveCastHomework, Collection<String> picBookIds, Long studentId, String token) {
        if (CollectionUtils.isEmpty(picBookIds) || liveCastHomework == null) {
            return Collections.emptyList();
        }

        LiveCastHomeworkResult newHomeworkResult = newHomeworkLivecastLoader.loadLiveCastHomeworkResult(liveCastHomework.toLocation(), studentId);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (newHomeworkResult != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS) != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS).getAppAnswers())
        ) {
            appAnswerMap = newHomeworkResult.getPractices().get(ObjectiveConfigType.LEVEL_READINGS).getAppAnswers();
        }
        String homeworkId = liveCastHomework.getId();
        List<PictureBookPlusSummaryResult> resultList = new ArrayList<>();
        Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(picBookIds);
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookLoaderClient.loadAllPictureBookSeries()
                .stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookLoaderClient.loadAllPictureBookTopics()
                .stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        Map<String, NewHomeworkApp> newHomeworkAppMap = new HashMap<>();
        NewHomeworkPracticeContent practiceContent = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.LEVEL_READINGS);
        if (practiceContent != null && CollectionUtils.isNotEmpty(practiceContent.getApps())) {
            for (NewHomeworkApp newHomeworkApp : practiceContent.getApps()) {
                if (StringUtils.isNotBlank(newHomeworkApp.getPictureBookId())) {
                    newHomeworkAppMap.put(newHomeworkApp.getPictureBookId(), newHomeworkApp);
                }
            }
        }

        for (String pbId : picBookIds) {
            PictureBookPlusSummaryResult result = new PictureBookPlusSummaryResult();
            PictureBookPlus picBook = pictureBookPlusMap.get(pbId);
            result.setPictureBookId(pbId);
            result.setPictureBookName(picBook.pbName());
            result.setWordsCount(picBook.getWordsLength());

            PictureBookNewClazzLevel clazzLevel = null;
            if (CollectionUtils.isNotEmpty(picBook.getNewClazzLevels())) {
                clazzLevel = picBook.getNewClazzLevels().get(0);
            }
            result.setLevel(clazzLevel == null ? "" : clazzLevel.name());

            List<Map<String, Object>> keyWords = picBook.allNewWords()
                    .stream()
                    .map(word -> MapUtils.m("enText", word.getEntext(), "cnText", word.getCntext()))
                    .collect(Collectors.toList());
            result.setKeyWords(keyWords);

            String seriesId = picBook.getSeriesId();
            String seriesName = "";
            if (pictureBookSeriesMap.containsKey(seriesId)) {
                seriesName = pictureBookSeriesMap.get(seriesId).getName();
            }
            result.setSeries(seriesName);

            List<String> pictureBookTopicIdList = picBook.getTopicIds();
            List<String> pictureBookTopicNameList = Collections.emptyList();
            if (CollectionUtils.isNotEmpty(pictureBookTopicIdList)) {
                pictureBookTopicNameList = pictureBookTopicIdList.stream()
                        .filter(pictureBookTopicMap::containsKey)
                        .map(id -> pictureBookTopicMap.get(id).getName())
                        .collect(Collectors.toList());
            }
            result.setTopics(pictureBookTopicNameList);

            // 默认一定会有阅读，高频词练习这两个类型
            List<PictureBookPracticeType> pictureBookPracticeTypes = new ArrayList<>();
            pictureBookPracticeTypes.add(PictureBookPracticeType.READING);
            pictureBookPracticeTypes.add(PictureBookPracticeType.WORDS);
            NewHomeworkApp newHomeworkApp = newHomeworkAppMap.get(pbId);
            if (newHomeworkApp == null) {
                continue;
            }
            // 计算绘本时长
            // 阅读模块 + 高频词模块时长
            int seconds = SafeConverter.toInt(picBook.getRecommendTime(), 300) + 10 * picBook.allOftenUsedWords().size();

            // 跟读
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getOralQuestions())) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.ORAL);
                seconds += SafeConverter.toInt(picBook.getOralSeconds(), 300);
            }
            // 练习
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getQuestions())) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.EXAM);
                seconds += 20 * newHomeworkApp.getQuestions().size();
            }
            // 配音
            if (newHomeworkApp.containsDubbing()) {
                pictureBookPracticeTypes.add(PictureBookPracticeType.DUBBING);
                seconds += SafeConverter.toInt(picBook.getPredictedDubbingTime(), 300);
            }

            List<Map<String, Object>> practiceTypes = pictureBookPracticeTypes.stream()
                    .map(practiceType -> MapUtils.m("type", practiceType.name(), "typeName", practiceType.getTypeName()))
                    .collect(Collectors.toList());
            result.setPracticeTypes(practiceTypes);
            result.setSeconds(seconds);

            result.setSpellingPb(picBook.getIsSpellingPb());
            result.setCoverUrl(picBook.getCoverUrl());
            result.setScreenMode(picBook.getScreenMode());
            result.setBookSummary(picBook.getBookSummary());

            String questionUrl = UrlUtils.buildUrlQuery("/livecast/student/homework/questions" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS, "homeworkId", homeworkId, "videoId", pbId, "token", token));
            String completedUrl = UrlUtils.buildUrlQuery("/livecast/student/homework/questions/answer" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS, "homeworkId", homeworkId, "videoId", pbId, "token", token));
            result.setQuestionUrl(questionUrl);
            result.setCompletedUrl(completedUrl);

            result.setAppDataUrl(UrlUtils.buildUrlQuery("/livecast/student/homework/picturebook" + Constants.AntiHijackExt, MapUtils.m("pictureBookId", pbId, "token", token)));
            result.setProcessResultUrl(UrlUtils.buildUrlQuery("/livecast/student/homework/batch/processresult" + Constants.AntiHijackExt, MapUtils.m("objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS,
                    "homeworkId", homeworkId,
                    "token", token)));

            result.setUploadDubbingUrl(UrlUtils.buildUrlQuery("/livecast/student/homework/picturebookplus/uploaddubbing" + Constants.AntiHijackExt, MapUtils.m("token", token)));

            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(pbId, null);
            if (appAnswer != null && appAnswer.isFinished()) {
                result.setFinished(Boolean.TRUE);
                result.setScore(new BigDecimal(appAnswer.getScore()).setScale(0, RoundingMode.HALF_UP).intValue());
                if (newHomeworkApp.containsDubbing()) {
                    result.setDubbingId(appAnswer.getDubbingId());
                }
            } else {
                result.setFinished(Boolean.FALSE);
            }

            resultList.add(result);
        }
        return resultList.stream().sorted((s1, s2) -> s2.getFinished().compareTo(s1.getFinished())).collect(Collectors.toList());
    }


    private String buildAuthorName(Long ugcAuthorId) {
        StringBuilder result = new StringBuilder();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(ugcAuthorId);
        if (teacherDetail != null) {
            ExRegion region = raikouSystem.loadRegion(teacherDetail.getRegionCode());
            if (region != null) {
                String name = StringUtils.isNotBlank(teacherDetail.getProfile().getRealname()) ? teacherDetail.getProfile().getRealname().substring(0, 1) + "老师" : "老师";
                if (region.isMunicipalitiy()) {
                    result.append("作者: ").append(region.getProvinceName()).append(" ").append(name);
                } else {
                    result.append("作者: ").append(region.getProvinceName()).append(" ").append(region.getCityName()).append(" ").append(name);
                }
            }
        }
        return result.toString();
    }

    @Override
    public MapMessage getPictureBookDraftByPicBookId(String picBookId) {
        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(Collections.singleton(picBookId));
        if (MapUtils.isEmpty(pictureBookMap)) {
            return MapMessage.errorMessage("阅读绘本不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_PICTURE_BOOK_IS_NULL);
        }
        PictureBookDraft draft;
        try {
            PictureBook pictureBook = pictureBookMap.get(picBookId);
            List<PictureBookContent> pictureBookContentList = pictureBook.getContents();
            draft = pictureBookToDraft(pictureBook, pictureBookContentList);
            String authorName = buildAuthorName(pictureBook.getUgcAuthor());
            draft.getContent().put("authorInfo", authorName);
        } catch (Exception ex) {
            logger.error("Failed to getPictureBookDraftByPicBookId (picBookId={})", picBookId, ex);
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage().add("readingDraft", draft);

    }

    @Override
    public MapMessage getPictureBookPlusDubbingDraft(String dubbingId) {
        if (StringUtils.equalsIgnoreCase(dubbingId, "undefined")) {
            return MapMessage.errorMessage("配音id错误");
        }
        PictureBookPlusDubbing pictureBookPlusDubbing = pictureBookPlusDubbingDao.load(dubbingId);
        if (pictureBookPlusDubbing == null) {
            return MapMessage.errorMessage("配音不存在");
        }
        PictureBookPlusDubbing.ID id = pictureBookPlusDubbing.parseID();
        String pictureBookId = id.getPictureBookId();
        Student student = studentLoaderClient.loadStudent(id.getUserId());
        PictureBookPlus pictureBookPlus = pictureBookPlusServiceClient.loadByIds(Collections.singleton(pictureBookId)).get(pictureBookId);
        return MapMessage.successMessage()
                .add("contents", pictureBookPlusDubbing.getContents())
                .add("screenMode", pictureBookPlusDubbing.getScreenMode())
                .add("studentName", student != null ? student.fetchRealname() : "")
                .add("pictureBookName", pictureBookPlus != null ? pictureBookPlus.getEname() : "")
                .add("authorized", pictureBookPlus != null && SafeConverter.toBoolean(pictureBookPlus.getAuthorized()));
    }

    private PictureBookDraft pictureBookToDraft(PictureBook pictureBook, List<PictureBookContent> contentList) {
        String cname = pictureBook.getAlias();
        String ename = pictureBook.getName();
        String coverUri = pictureBook.getCoverUrl();
        String coverUri1 = pictureBook.getCoverThumbnail1Uri();
        Integer difficultyLevel = pictureBook.getDifficultyLevel();
        Long ugcAuthor = pictureBook.getUgcAuthor();
        Integer wordCount = pictureBook.getWordsCount();
        Integer recommendTime = pictureBook.getSeconds();
        PictureBookDraft draft = new PictureBookDraft();

        Map<String, List<PictureBookContent>> contentPageMap = new LinkedHashMap<>();
        //按页码归类
        for (PictureBookContent content : contentList) {
            StringBuilder key = new StringBuilder();
            key.append(content.getPageNum()).append("|").append(content.getLayoutType());
            if (contentPageMap.get(key.toString()) != null) {
                contentPageMap.get(key.toString()).add(content);
            } else {
                List<PictureBookContent> tempContents = new ArrayList<>();
                tempContents.add(content);
                contentPageMap.put(key.toString(), tempContents);
            }
        }

        List<Map<String, Object>> readingPages = buildDraftContent(contentPageMap);
        List<String> points = pictureBook.findPointIds();
        Map<String, Object> content = MiscUtils.m(
                "cname", cname,
                "ename", ename,
                "difficultyLevel", difficultyLevel,
                "points", points,
                "coverUri", coverUri,
                "coverUri1", coverUri1,
                "ugcAuthor", ugcAuthor,
                "readingPages", readingPages,
                "oralQuestions", questionLoaderClient.loadQuestionsIncludeDisabledAsList(pictureBook.getOralQuestions()),
                "questions", questionLoaderClient.loadQuestionsIncludeDisabledAsList(pictureBook.getPracticeQuestions())
        );
        draft.setPictureBookId(pictureBook.getId());
        draft.setCname(cname);
        draft.setEname(ename);
        draft.setPoints(points);
        draft.setDifficultyLevel(difficultyLevel);
        draft.setUgcAuthor(ugcAuthor);
        draft.setWordsCount(wordCount);
        draft.setContent(content);
        draft.setRecommendTime(recommendTime);
        return draft;
    }

    private List<Map<String, Object>> buildDraftContent(Map<String, List<PictureBookContent>> contentPageMap) {
        List<Map<String, Object>> contentPages = new ArrayList<>();
        for (String key : contentPageMap.keySet()) {
            List<PictureBookContent> contentList = contentPageMap.get(key);
            if (CollectionUtils.isEmpty(contentList)) {
                continue;
            }
            contentList.sort((o1, o2) -> {
                int subPage1 = SafeConverter.toInt(o1.getSubPageNum());
                int subPage2 = SafeConverter.toInt(o2.getSubPageNum());
                if (subPage1 != subPage2) {
                    return subPage1 - subPage2;
                }

                int paragraph1 = SafeConverter.toInt(o1.getParagraph());
                int paragraph2 = SafeConverter.toInt(o2.getParagraph());
                if (paragraph1 != paragraph2) {
                    return paragraph1 - paragraph2;
                }

                int rank1 = SafeConverter.toInt(o1.getRank());
                int rank2 = SafeConverter.toInt(o2.getRank());
                return rank1 - rank2;
            });
            String layout = StringUtils.substringAfter(key, "|");
            Integer pageNum = conversionService.convert(StringUtils.substringBefore(key, "|"), Integer.class);
            Map<String, Object> pageMap = new LinkedHashMap<>();
            pageMap.put("pageNum", pageNum);
            pageMap.put("pageLayout", layout);
            if (PictureBookLayoutType.ptpt.name().equals(layout) || PictureBookLayoutType.tt.name().equals(layout)) {
                Map<String, Object> firstHalfPageMap = new LinkedHashMap<>();
                Map<String, Object> afterHalfPageMap = new LinkedHashMap<>();
                String firstHalfPicUri = null;
                String afterHalfPicUri = null;
                List<PictureBookContent.EmbedKeyword> firstHalfKeyWords = new ArrayList<>();
                List<PictureBookContent.EmbedKeyword> afterHalfKeyWords = new ArrayList<>();
                List<Map<String, Object>> firstHalfReadingSentences = new ArrayList<>();
                List<Map<String, Object>> afterHalfReadingSentences = new ArrayList<>();

                for (PictureBookContent content : contentList) {
                    List<PictureBookContent.EmbedKeyword> keyWordMaps = content.getKeyWords();
                    String picUri = content.getPictureUrl();
                    String dialogRole = content.getRole();
                    Integer paragraph = content.getParagraph();
                    String entext = content.getEntext();
                    String cntext = content.getCntext();
                    Integer rank = content.getRank();
                    String audioUri = content.getAudioUrl();
                    Map<String, Object> sentenceMap = MiscUtils.m(
                            "dialogRole", dialogRole,
                            "paragraph", paragraph,
                            "entext", entext + " ",
                            "cntext", cntext,
                            "rank", rank,
                            "audioUri", audioUri
                    );

                    if (StringUtils.equals(PictureBookSubPage.firstHalfPage.getIndex(), content.getSubPageNum())) {
                        firstHalfPicUri = picUri;
                        firstHalfKeyWords.addAll(keyWordMaps);
                        firstHalfReadingSentences.add(sentenceMap);
                    } else {
                        afterHalfPicUri = picUri;
                        afterHalfKeyWords.addAll(keyWordMaps);
                        afterHalfReadingSentences.add(sentenceMap);
                    }
                }
                firstHalfPageMap.put("picUri", firstHalfPicUri);
                afterHalfPageMap.put("picUri", afterHalfPicUri);
                firstHalfPageMap.put("keyWords", firstHalfKeyWords);
                afterHalfPageMap.put("keyWords", afterHalfKeyWords);
                firstHalfPageMap.put("readingSentences", firstHalfReadingSentences);
                afterHalfPageMap.put("readingSentences", afterHalfReadingSentences);
                pageMap.put("firstHalfPage", firstHalfPageMap);
                pageMap.put("afterHalfPage", afterHalfPageMap);

            } else {
                String picUri = MiscUtils.firstElement(contentList).getPictureUrl();
                List<PictureBookContent.EmbedKeyword> keyWords = contentList
                        .stream()
                        .flatMap(p -> p.getKeyWords().stream())
                        .collect(Collectors.toList());
                List<Map<String, Object>> readingSentences = contentList.stream()
                        .map(content -> {
                            String dialogRole = content.getRole();
                            Integer paragraph = content.getParagraph();
                            String entext = content.getEntext();
                            String cntext = content.getCntext();
                            Integer rank = content.getRank();
                            String audioUri = content.getAudioUrl();
                            return MiscUtils.m(
                                    "dialogRole", dialogRole,
                                    "paragraph", paragraph,
                                    "entext", entext + " ",
                                    "cntext", cntext,
                                    "rank", rank,
                                    "audioUri", audioUri
                            );
                        })
                        .collect(Collectors.toList());
                pageMap.put("picUri", picUri);
                pageMap.put("keyWords", keyWords);
                pageMap.put("readingSentences", readingSentences);
            }
            contentPages.add(pageMap);
        }
        return contentPages;
    }
}
