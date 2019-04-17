package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.entity.WordRecognitionSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.WordRecognitionHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = WordRecognitionHomeworkService.class)
@ExposeService(interfaceClass = WordRecognitionHomeworkService.class)
public class WordRecognitionHomeworkServiceImpl implements WordRecognitionHomeworkService {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private NewContentLoaderClient newContentLoaderClient;

    @Override
    public List<WordRecognitionSummaryResult> getWordRecognitionSummaryInfo(String homeworkId, Long studentId) {
        if (StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return Collections.emptyList();
        }
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.WORD_RECOGNITION_AND_READING;
        if (newHomeworkResult != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(objectiveConfigType) != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers())) {
            appAnswerMap = newHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
        }
        List<WordRecognitionSummaryResult> resultList = new ArrayList<>();
        List<String> lessonIds = newHomework.findNewHomeworkApps(objectiveConfigType).stream().map(NewHomeworkApp::getLessonId).collect(Collectors.toList());
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        List<NewHomeworkApp> homeworkApps = newHomework.findNewHomeworkApps(objectiveConfigType);
        if (CollectionUtils.isEmpty(homeworkApps)) {
            return Collections.emptyList();
        }
        for (NewHomeworkApp homeworkApp : homeworkApps) {
            String questionBoxId = homeworkApp.getQuestionBoxId();
            List<NewHomeworkQuestion> questions = homeworkApp.getQuestions();
            WordRecognitionSummaryResult result = new WordRecognitionSummaryResult();
            NewBookCatalog newBookCatalog = lessonMap.get(homeworkApp.getLessonId());
            result.setLessonId(homeworkApp.getLessonId());
            result.setQuestionBoxId(questionBoxId);
            result.setLessonName(newBookCatalog == null ? "课文不存在" : newBookCatalog.getName());
            result.setQuestionCount(questions.size());
            result.setProcessResultUrl(UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", studentId)));
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

            result.setParagraphInfo("共" + homeworkApp.getQuestions().size() + "个生字");
            resultList.add(result);
        }
        return resultList;
    }
}
