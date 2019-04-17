package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.newhomework.api.entity.VideoSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.VideoHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Video;
import com.voxlearning.utopia.service.question.consumer.VideoLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @since 2016/11/25
 */
@Named
@Service(interfaceClass = VideoHomeworkService.class)
@ExposeService(interfaceClass = VideoHomeworkService.class)
public class VideoHomeworkServiceImpl implements VideoHomeworkService {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private VideoLoaderClient videoLoaderClient;
    @Inject private VacationHomeworkDao vacationHomeworkDao;
    @Inject private VacationHomeworkResultDao vacationHomeworkResultDao;

    @Override
    public List<VideoSummaryResult> getVideoSummaryInfo(String homeworkId, Long studentId) {
        if (StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.KEY_POINTS;
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

        List<VideoSummaryResult> resultList = new ArrayList<>();
        List<String> videoIds = newHomework.findNewHomeworkApps(objectiveConfigType).stream().map(NewHomeworkApp::getVideoId).collect(Collectors.toList());
        Map<String, Video> videoMap = videoLoaderClient.loadVideoIncludeDisabled(videoIds);
        for (String vid : videoIds) {
            List<NewHomeworkQuestion> questions = newHomework.findNewHomeworkKeyPointQuestions(objectiveConfigType, vid);
            VideoSummaryResult result = new VideoSummaryResult();
            Video video = videoMap.get(vid);
            result.setVideoId(vid);
            result.setVideoName(video.getVideoName());
            result.setVideoSummary(video.getVideoSummary());
            result.setVideoSeconds(video.getVideoSeconds());
            result.setVideoUrl(video.getVideoUrl());
            result.setSolutionTricks(video.getSolutionTricks());
            result.setCoverUrl(video.getCoverUrl());
            result.setQuestionCount(questions.size());
            result.setProcessResultUrl(UrlUtils.buildUrlQuery("/exam/flash/newhomework/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", studentId)));
            result.setQuestionUrl(UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", vid, "sid", studentId)));
            result.setCompletedUrl(UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", vid, "sid", studentId)));
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(vid, null);
            if (appAnswer != null) {
                result.setFinishQuestionCount(MapUtils.isNotEmpty(appAnswer.getAnswers()) ? appAnswer.getAnswers().size() : 0);
                result.setFinished(appAnswer.isFinished());
            } else {
                result.setFinishQuestionCount(0);
                result.setFinished(Boolean.FALSE);
            }

            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public List<VideoSummaryResult> getVacationVideoSummaryInfo(String homeworkId, Long studentId) {
        if (StringUtils.isBlank(homeworkId)) {
            return Collections.emptyList();
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.KEY_POINTS;
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (vacationHomework == null) {
            return Collections.emptyList();
        }

        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (vacationHomeworkResult != null
                && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices())
                && vacationHomeworkResult.getPractices().get(objectiveConfigType) != null
                && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers())) {
            appAnswerMap = vacationHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
        }

        List<VideoSummaryResult> resultList = new ArrayList<>();
        List<String> videoIds = vacationHomework.findNewHomeworkApps(objectiveConfigType).stream().map(NewHomeworkApp::getVideoId).collect(Collectors.toList());
        Map<String, Video> videoMap = videoLoaderClient.loadVideoIncludeDisabled(videoIds);
        for (String vid : videoIds) {
            List<NewHomeworkQuestion> questions = vacationHomework.findNewHomeworkKeyPointQuestions(objectiveConfigType, vid);
            VideoSummaryResult result = new VideoSummaryResult();
            Video video = videoMap.get(vid);
            appAnswerMap.get(vid);
            result.setVideoId(vid);
            result.setVideoName(video.getVideoName());
            result.setVideoSummary(video.getVideoSummary());
            result.setVideoSeconds(video.getVideoSeconds());
            result.setVideoUrl(video.getVideoUrl());
            result.setSolutionTricks(video.getSolutionTricks());
            result.setCoverUrl(video.getCoverUrl());
            result.setQuestionCount(questions.size());
            result.setProcessResultUrl("/exam/flash/newhomework/processresult" + Constants.AntiHijackExt);
            result.setQuestionUrl(UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", vid)));
            result.setCompletedUrl(UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "videoId", vid)));
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(vid, null);
            if (appAnswer != null && appAnswer.isFinished()) {
                result.setFinishQuestionCount(appAnswer.getAnswers().size());
                result.setFinished(Boolean.TRUE);
            } else {
                result.setFinishQuestionCount(0);
                result.setFinished(Boolean.FALSE);
            }
            resultList.add(result);
        }
        return resultList;
    }
}
