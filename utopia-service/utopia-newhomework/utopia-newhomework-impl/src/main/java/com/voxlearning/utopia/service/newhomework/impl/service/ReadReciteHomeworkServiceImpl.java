package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.entity.ReadReciteSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkQuestionAnswerRequest;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.ReadReciteHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.VacationHomeworkLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2017/6/2.
 */
@Named
@Service(interfaceClass = ReadReciteHomeworkService.class)
@ExposeService(interfaceClass = ReadReciteHomeworkService.class)
public class ReadReciteHomeworkServiceImpl extends SpringContainerSupport implements ReadReciteHomeworkService {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private VacationHomeworkDao vacationHomeworkDao;
    @Inject private VacationHomeworkResultDao vacationHomeworkResultDao;
    @Inject private VacationHomeworkLoaderImpl vacationHomeworkLoader;

    @Override
    public List<ReadReciteSummaryResult> getReadReciteSummaryInfo(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyList();
        }
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return Collections.emptyList();
        }

        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (newHomeworkResult != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(objectiveConfigType) != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers())
                ) {
            appAnswerMap = newHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
        }

        List<ReadReciteSummaryResult> resultList = new ArrayList<>();
        List<String> lessonIds = newHomework.findNewHomeworkApps(objectiveConfigType).stream().map(NewHomeworkApp::getLessonId).collect(Collectors.toList());
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        List<NewHomeworkApp> homeworkApps = newHomework.findNewHomeworkApps(objectiveConfigType);
        if (CollectionUtils.isEmpty(homeworkApps)) {
            return Collections.emptyList();
        }
        for(NewHomeworkApp homeworkApp : homeworkApps) {
            String questionBoxId = homeworkApp.getQuestionBoxId();
            String questionBoxType = homeworkApp.getQuestionBoxType().name();
            List<NewHomeworkQuestion> questions = homeworkApp.getQuestions();
            ReadReciteSummaryResult result = new ReadReciteSummaryResult();
            NewBookCatalog newBookCatalog = lessonMap.get(homeworkApp.getLessonId());
            result.setLessonId(homeworkApp.getLessonId());
            result.setQuestionBoxId(questionBoxId);
            result.setLessonName(newBookCatalog == null ? "课文不存在" : newBookCatalog.getName());
            result.setQuestionBoxType(questionBoxType);
            result.setQuestionCount(questions.size());
            if (ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)) {
                result.setProcessResultUrl(UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", studentId)));
            } else {
                result.setProcessResultUrl(UrlUtils.buildUrlQuery("/exam/flash/newhomework/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", studentId)));
            }
            result.setQuestionUrl(UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "questionBoxId", questionBoxId, "sid", studentId)));
            result.setCompletedUrl(UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "questionBoxId", questionBoxId, "sid", studentId)));
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(questionBoxId, null);
            if (appAnswer != null && appAnswer.isFinished()) {
                result.setFinishQuestionCount(appAnswer.getAnswers().size());
                result.setFinished(Boolean.TRUE);
            } else {
                result.setFinishQuestionCount(0);
                result.setFinished(Boolean.FALSE);
            }
            List<Integer> paragraphs = new ArrayList<>();

            HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
            request.setHomeworkId(homeworkId);
            request.setObjectiveConfigType(objectiveConfigType);
            request.setCategoryId(0);
            request.setQuestionBoxId(questionBoxId);

            Map<String, Object> questionsDetail = newHomeworkLoader.loadHomeworkQuestions(request);
            if (MapUtils.isNotEmpty(questionsDetail)) {
                Map<String, Object> extraInfo = (Map<String, Object>) questionsDetail.get("extraInfo");
                if (MapUtils.isNotEmpty(extraInfo)) {
                    for (Map.Entry<String, Object> entry : extraInfo.entrySet()) {
                        Map<String, Object> qInfo = (Map<String, Object>) entry.getValue();
                        if (MapUtils.isNotEmpty(qInfo)) {
                            Integer paragraph = SafeConverter.toInt(qInfo.get("paragraph"));
                            if (paragraph > 0) {
                                paragraphs.add(paragraph);
                            }
                        }
                    }
                }
            }
            Collections.sort(paragraphs);
            result.setParagraphInfo("第" + StringUtils.join(paragraphs, ",") + "段");
            resultList.add(result);
        }

        return resultList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ReadReciteSummaryResult> getVacationReadReciteSummaryInfo(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        if (StringUtils.isBlank(homeworkId)) {
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
                && vacationHomeworkResult.getPractices().get(objectiveConfigType) != null
                && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers())
                ) {
            appAnswerMap = vacationHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
        }

        List<ReadReciteSummaryResult> resultList = new ArrayList<>();
        List<String> lessonIds = vacationHomework.findNewHomeworkApps(objectiveConfigType).stream().map(NewHomeworkApp::getLessonId).collect(Collectors.toList());
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        List<NewHomeworkApp> homeworkApps = vacationHomework.findNewHomeworkApps(objectiveConfigType);
        if (CollectionUtils.isEmpty(homeworkApps)) {
            return Collections.emptyList();
        }

        for(NewHomeworkApp homeworkApp : homeworkApps) {
            String questionBoxId = homeworkApp.getQuestionBoxId();
            String questionBoxType = homeworkApp.getQuestionBoxType().name();
            List<NewHomeworkQuestion> questions = homeworkApp.getQuestions();
            ReadReciteSummaryResult result = new ReadReciteSummaryResult();
            NewBookCatalog newBookCatalog = lessonMap.get(homeworkApp.getLessonId());
            result.setLessonId(homeworkApp.getLessonId());
            result.setQuestionBoxId(questionBoxId);
            result.setLessonName(newBookCatalog == null ? "课文不存在" : newBookCatalog.getName());
            result.setQuestionBoxType(questionBoxType);
            result.setQuestionCount(questions.size());
            if (ObjectiveConfigType.READ_RECITE_WITH_SCORE == objectiveConfigType) {
                result.setProcessResultUrl("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt);
            } else {
                result.setProcessResultUrl("/exam/flash/newhomework/processresult" + Constants.AntiHijackExt);
            }
            result.setQuestionUrl(UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "questionBoxId", questionBoxId)));
            result.setCompletedUrl(UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "questionBoxId", questionBoxId)));
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(questionBoxId, null);
            if (appAnswer != null && appAnswer.isFinished()) {
                result.setFinishQuestionCount(appAnswer.getAnswers().size());
                result.setFinished(Boolean.TRUE);
            } else {
                result.setFinishQuestionCount(0);
                result.setFinished(Boolean.FALSE);
            }
            List<Integer> paragraphs = new ArrayList<>();
            Map<String, Object> questionsDetail = vacationHomeworkLoader.loadHomeworkQuestions(homeworkId, objectiveConfigType, 0, "", "", questionBoxId);
            if (MapUtils.isNotEmpty(questionsDetail)) {
                Map<String, Object> extraInfo = (Map<String, Object>) questionsDetail.get("extraInfo");
                if (MapUtils.isNotEmpty(extraInfo)) {
                    for (Map.Entry<String, Object> entry : extraInfo.entrySet()) {
                        Map<String, Object> qInfo = (Map<String, Object>) entry.getValue();
                        if (MapUtils.isNotEmpty(qInfo)) {
                            Integer paragraph = SafeConverter.toInt(qInfo.get("paragraph"));
                            if (paragraph > 0) {
                                paragraphs.add(paragraph);
                            }
                        }
                    }
                }
            }
            Collections.sort(paragraphs);
            result.setParagraphInfo("第" + StringUtils.join(paragraphs, ",") + "段");
            resultList.add(result);
        }
        return resultList;
    }

}
