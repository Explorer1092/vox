package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.outside.SaveOutsideReadingResultRequest;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.OutsideReadingLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.OutsideReadingServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.flash.FlashVars;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/student/outside/reading")
public class StudentOutsideReadingController extends AbstractController {

    @Inject private OutsideReadingLoaderClient outsideReadingLoader;
    @Inject private OutsideReadingServiceClient outsideReadingServiceClient;

    /**
     * 我的书架
     */
    @RequestMapping(value = "bookshelf.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage myBookshelf() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }
        return outsideReadingLoader.loadBookshelf(user.getId(), getCdnBaseUrlAvatarWithSep());
    }


    /**
     * 学生书籍详情
     */
    @RequestMapping(value = "book/detail.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage bookDetail() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }

        String outsideReadingId = getRequestString("outsideReadingId");
        if (StringUtils.isBlank(outsideReadingId)) {
            return MapMessage.errorMessage("请求参数异常");
        }
        return outsideReadingLoader.loadStudentBookDetail(user.getId(), outsideReadingId);
    }

    @RequestMapping(value = "do.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage doHomework(HttpServletRequest request) {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }

        String outsideReadingId = getRequestString("outsideReadingId");
        String missionId = getRequestString("missionId");
        OutsideReading outsideReading = outsideReadingLoader.findOutsideReadingById(outsideReadingId);
        if (outsideReading == null || outsideReading.isDisabledTrue()) {
            return MapMessage.errorMessage("阅读任务不存在，或者阅读任务已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED);
        }

        FlashVars vars = new FlashVars(request);
        vars.add("outsideReadingId", outsideReadingId);
        vars.add("missionId", missionId);
        vars.add("userId", user.getId());
        vars.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        vars.add("processResultUrl", "/student/outside/reading/processresult" + Constants.AntiHijackExt);
        vars.add("questionUrl", UrlUtils.buildUrlQuery("/student/outside/reading/questions" + Constants.AntiHijackExt, MapUtils.m("outsideReadingId", outsideReadingId, "missionId", missionId)));
        vars.add("completedUrl", UrlUtils.buildUrlQuery("/student/outside/reading/questions/answer" + Constants.AntiHijackExt, MapUtils.m("outsideReadingId", outsideReadingId, "missionId", missionId)));

        return MapMessage.successMessage().add("flashVars", vars);
    }

    @RequestMapping(value = "questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getQuestions() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }
        String outsideReadingId = getRequestString("outsideReadingId");
        String missionId = getRequestString("missionId");
        if(StringUtils.isBlank(outsideReadingId) || StringUtils.isBlank(missionId)){
            return MapMessage.errorMessage("参数不允许为空");
        }
        return outsideReadingLoader.loadQuestions(outsideReadingId, missionId);
    }

    @RequestMapping(value = "questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getQuestionsAnswer() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }
        String outsideReadingId = getRequestString("outsideReadingId");
        String missionId = getRequestString("missionId");
        if(StringUtils.isBlank(outsideReadingId) || StringUtils.isBlank(missionId)){
            return MapMessage.errorMessage("参数不允许为空");
        }

        return outsideReadingLoader.loadQuestionsAnswer(user.getId(), outsideReadingId, missionId);
    }

    /**
     * 学生提交课外阅读做题结果
     */
    @RequestMapping(value = "processresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processResult() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging() || RuntimeMode.isDevelopment())
                && validateAccessFreq(user.getId(), "PROCESS_OUTSIDE_READING_RESULT", "/student/outside/reading/processresult.vpage", 100)) {
            return MapMessage.errorMessage("您访问次数异常，过于频繁请联系客服或者技术人员。");
        }

        SaveOutsideReadingResultRequest request = getRequestObject(SaveOutsideReadingResultRequest.class);
        if (request == null || StringUtils.isBlank(request.getOutsideReadingId()) || StringUtils.isBlank(request.getQuestionId())
                || (CollectionUtils.isEmpty(request.getAnswer()) && CollectionUtils.isEmpty(request.getFileUrls()))) {
            return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(request));
        }

        OutsideReadingContext context = new OutsideReadingContext();
        context.setUserId(user.getId());
        context.setUser(user);
        context.setReadingId(request.getOutsideReadingId());
        context.setMissionId(request.getMissionId());
        context.setClientType(request.getClientType());
        context.setClientName(request.getClientName());
        context.setUserAgent(getRequest().getHeader("User-Agent"));
        StudentHomeworkAnswer sha = new StudentHomeworkAnswer();
        sha.setQuestionId(request.getQuestionId());
        sha.setAnswer(request.getAnswer());
        sha.setDurationMilliseconds(NewHomeworkUtils.processDuration(request.getDuration()));
        sha.setFileUrls(request.getFileUrls());
        context.setStudentHomeworkAnswer(sha);
        try {
            return outsideReadingServiceClient.processResult(context);
        } catch (Exception ex) {
            logger.error("Failed to save user {} outside reading request", user.getId(), ex);
            return MapMessage.errorMessage("提交结果数据异常");
        }
    }

    /**
     * 好词好句收藏
     */
    @RequestMapping(value = "goldenwords/index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage goldenWordsIndex() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }
        String outsideReadingId = getRequestString("outsideReadingId");
        String missionId = getRequestString("missionId");
        if(StringUtils.isBlank(outsideReadingId) || StringUtils.isBlank(missionId)){
            return MapMessage.errorMessage("参数不允许为空");
        }

        return outsideReadingLoader.goldenWordsIndex(user.getId(), outsideReadingId, missionId);
    }

    /**
     * 好词好句(提交收藏)
     */
    @RequestMapping(value = "save/goldenwords.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveGoldenWords() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }
        String outsideReadingId = getRequestString("outsideReadingId");
        String missionId = getRequestString("missionId");
        List<String> missionIndexes = Arrays.asList(getRequestString("missionIdIndex").trim().split(","));

        if(StringUtils.isBlank(outsideReadingId) || StringUtils.isBlank(missionId)){
            return MapMessage.errorMessage("参数不允许为空");
        }

        return outsideReadingServiceClient.saveGoldenWords(user.getId(), outsideReadingId, missionId, missionIndexes);
    }

    /**
     * 关卡阅读成就
     */
    @RequestMapping(value = "mission/achievement.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchMissionAchievement() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }
        String outsideReadingId = getRequestString("outsideReadingId");
        String missionId = getRequestString("missionId");

        if(StringUtils.isBlank(outsideReadingId) || StringUtils.isBlank(missionId)){
            return MapMessage.errorMessage("参数不允许为空");
        }

        return outsideReadingLoader.fetchMissionAchievement(user.getId(), outsideReadingId, missionId);
    }


    /**
     * 学生总阅读成就
     */
    @RequestMapping(value = "achievement.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchAchievement() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }

        return outsideReadingLoader.fetchAchievement(user.getId(), getCdnBaseUrlAvatarWithSep());
    }

    /**
     * 我的好词好句
     */
    @RequestMapping(value = "goldenWords/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchGoldenWordsList() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录!");
        }
        String label = getRequestString("label");
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        pageNum = pageNum < 1 ? 1 : pageNum;
        return outsideReadingLoader.fetchGoldenWordsList(user.getId(), label, pageNum, pageSize);
    }
}
