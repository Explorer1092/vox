package com.voxlearning.washington.controller.ai;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentService;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.ChipsLessonRequest;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionBO;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_NEED_RELOGIN_CODE;

/**
 * Created by Summer on 2018/9/4
 * 薯条英语V2正式课内容部分接口
 */
@Controller
@RequestMapping("/chips/v2")
public class ChipsEnglishContentController extends AbstractAiController {

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    @ImportService(interfaceClass = ChipsEnglishContentService.class)
    private ChipsEnglishContentService chipsEnglishContentService;


    // 获取今日课程
    @RequestMapping(value = "content/dailycontent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyContent() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }
        String version = getRequestString("ver");
        String sys = getRequestString("sys");
        String checkVersion = Optional.ofNullable(chipsEnglishConfigServiceClient.loadChipsEnglishConfigByName(APP_VERSION + sys))
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .map(ChipsEnglishPageContentConfig::getValue)
                .orElse("");
        if (StringUtils.isNoneBlank(checkVersion, version) && VersionUtil.compareVersion(version, checkVersion) < 0) {
            String url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/parent_ai/download";
            return successMessage().add("data", "401").add("redirect", url);
        }
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        return wrapper(mm ->{
            mm.putAll(chipsEnglishContentLoader.loadDailyClassInfo(user, bookId, unitId));
        });

    }

    // 获取单元详情
    @RequestMapping(value = "content/unitdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage unitDetail() {
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return failMessage("参数异常!");
        }

        return wrapper(mm -> {
            MapMessage ret = chipsEnglishContentLoader.loadUnitDetail(user.getId(), bookId, unitId);
            mm.putAll(ret);
        });


    }

    // 获取lesson详情
    @RequestMapping(value = "content/lessondetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage lessonDetail() {
        // todo
        return null;
    }

    // 获取题目详情
    @RequestMapping(value = "content/questiondetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage questionDetail() {
        // todo
        return null;
    }

    // 获取lesson题目列表(词汇拓展专用)
    @RequestMapping(value = "content/questionlistForVocabCharging.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage questionlistForVocabCharging() {
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("id");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(bookId)
                || StringUtils.isBlank(unitId) || StringUtils.isBlank(lessonId)) {
            return failMessage("参数异常!");
        }

        return wrapper(mm -> {

            MapMessage ret = chipsEnglishContentLoader.loadLessonDetail(user.getId(), bookId, unitId, lessonId, "");

            LessonType lessonType = (LessonType) ret.get("lessonType");
            if (LessonType.vocab_charging.equals(lessonType)) {
                List<StoneData> questionList = (List<StoneData>) ret.get("questions");
                if (questionList != null && questionList.size() > 0) {
                    Map<String, List<StoneData>> map = questionList.stream().collect(Collectors.groupingBy(StoneData::getSchemaName));
                    ret.put("questions", map);
                }
            }
            mm.putAll(ret);
        });
    }


    // 获取lesson题目列表
    @RequestMapping(value = "content/questionlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage questionList() {
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("id");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(bookId)
                || StringUtils.isBlank(unitId) || StringUtils.isBlank(lessonId)) {
            return failMessage("参数异常!");
        }
        String version = getRequestString("ver");

        return wrapper(mm -> {

            MapMessage ret = chipsEnglishContentLoader.loadLessonDetail(user.getId(), bookId, unitId, lessonId, version);
            mm.putAll(ret);
        });
    }

    // 上传答题结果
    @RequestMapping(value = "content/processresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processResult() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }
        String json = getRequestString("data");
        ChipsQuestionResultRequest request = JsonUtils.fromJson(json, ChipsQuestionResultRequest.class);
        if (request == null || request.getUnitLast() == null || request.getUnitLast() == null ||
                StringUtils.isAnyBlank(request.getBookId(), request.getLessonId(), request.getQid(), request.getUnitId(), request.getQuestionType())) {
            return failMessage("参数异常![data:" + json + ", request:" + request + "]");
        }

        return wrapper(mm -> mm.putAll(
                AtomicCallbackBuilderFactory.getInstance()
                        .<MapMessage>newBuilder()
                        .keyPrefix("processAIQuestionResult")
                        .keys(user.getId(), request.getQid())
                        .callback(() -> chipsEnglishContentService.processQuestionResult(user.getId(), request))
                        .build()
                        .execute()
        ));

    }

    // 提交视频结果
    @RequestMapping(value = "user/video.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage questionVideoResult(@RequestParam(value = "qid", required = false) String qid,
                                          @RequestParam(value = "lessonId", required = false) String lessonId,
                                          @RequestParam(value = "bookId", required = false) String bookId,
                                          @RequestParam(value = "unitId", required = false) String unitId,
                                          @RequestParam(value = "video", required = false) String[] videos) {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        if (StringUtils.isAnyBlank(lessonId, bookId, unitId) || videos == null || videos.length <= 1) {
            return failMessage("参数异常!");
        }

        return wrapper(mm -> {
            MapMessage ret = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("processAIQuestionResult")
                    .keys(user.getId(), qid)
                    .callback(() -> chipsEnglishContentService.processUserVideo(user.getId(), qid, lessonId, unitId, bookId, Arrays.asList(videos)))
                    .build()
                    .execute();
            mm.putAll(ret);
        });
    }

    // 今日总结
    @RequestMapping(value = "/content/unitsum.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unitSum() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isAnyBlank(bookId, unitId)) {
            return failMessage("参数异常!");
        }

        return wrapper(mm ->
                mm.putAll(chipsEnglishContentLoader.loadUnitResult(user, bookId, unitId))
        );
    }

    // 今日总结剧本
    @RequestMapping(value = "content/unitplay.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unitPlay() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String bookId = getRequestString("bookId");
        String unitId = getRequestString("id");
        if (StringUtils.isAnyBlank(bookId, unitId)) {
            return failMessage("参数异常!");
        }

        return wrapper(mm ->
                mm.putAll(chipsEnglishContentLoader.loadLessonPlay(user.getId(), bookId, unitId))
        );
    }

    // lesson结果页
    @RequestMapping(value = "content/lessonsum.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lessonSum() {
        // todo
        return null;
    }

    // 地图列表
    @RequestMapping(value = "content/maplist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mapList() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }
        String version = getRequestString("app_version");
        return wrapper(mm -> mm.putAll(chipsEnglishContentLoader.loadUnitMap(user.getId(), version)));
    }


    // 数据收集
    @RequestMapping(value = "/data/collect.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage collect() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String input = getRequestString("input");
        String lessonId = getRequestString("lessonId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String qid = getRequestString("qid");
        String userVideo = getRequestString("userVideo");
        String questionType = getRequestString("questionType");
        if (StringUtils.isAnyBlank(input, lessonId, bookId, unitId, qid, questionType)) {
            return failMessage("参数异常!");
        }

        ChipsQuestionBO bo = new ChipsQuestionBO();
        bo.setQId(qid);
        bo.setLessonId(lessonId);
        bo.setUnitId(unitId);
        bo.setBookId(bookId);
        return wrapper(mm -> {
            mm.putAll(chipsEnglishContentService.collectData(user.getId(), ChipsQuestionType.of(questionType), bo, input, userVideo));
        });
    }

    // 获取问答等级
    @RequestMapping(value = "interlocution/level.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage interlocutionLevel() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String input = getRequestString("input");
        String lessonId = getRequestString("lessonId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String qid = getRequestString("qid");
        if (StringUtils.isAnyBlank(qid, unitId, bookId, lessonId, input)) {
            return failMessage("参数异常!");
        }
        return wrapper(mm -> mm.putAll(
                AtomicCallbackBuilderFactory.getInstance()
                        .<MapMessage>newBuilder()
                        .keyPrefix("chipsEnglishContentService.processInterlocutionLevel")
                        .keys(user.getId(), qid)
                        .callback(() -> chipsEnglishContentService.processInterlocutionLevel(user.getId(), bookId, lessonId, unitId, qid, input))
                        .build()
                        .execute()
        ));
    }

    // 获取模考问答等级
    @RequestMapping(value = "mockqa/level.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mockQALevel() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String input = getRequestString("input");
        String lessonId = getRequestString("lessonId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String qid = getRequestString("qid");
        String userVideo = getRequestString("userVideo");
        if (StringUtils.isAnyBlank(qid, unitId, bookId, lessonId, input)) {
            return failMessage("参数异常!");
        }
        ChipsQuestionBO bo = new ChipsQuestionBO();
        bo.setBookId(bookId);
        bo.setLessonId(lessonId);
        bo.setUnitId(unitId);
        bo.setQId(qid);
        return wrapper(mm -> mm.putAll(
                AtomicCallbackBuilderFactory.getInstance()
                        .<MapMessage>newBuilder()
                        .keyPrefix("chipsEnglishContentService.processMockQAQuestionLevel")
                        .keys(user.getId(), qid)
                        .callback(() -> chipsEnglishContentService.processMockQAQuestionLevel(user.getId(), bo, input, userVideo))
                        .build()
                        .execute()
        ));
    }

    // 情景对话
    @RequestMapping(value = "dialogue/scene.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dialogueScene() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String input = getRequestString("input");
        String userid = getRequestString("userid");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("lessonId");
        String qid = getRequestString("qid");
        if (StringUtils.isAnyBlank(input, userid, bookId, unitId, lessonId, qid)) {
            return failMessage("input param error!");
        }

        return wrapper(mm -> mm.putAll(AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("chipsEnglishContentService.processDialogueTalk")
                .keys(userid, lessonId)
                .callback(() -> chipsEnglishContentService.processDialogueTalk(user, userid, input, lessonId, unitId, bookId))
                .build()
                .execute()
        ));
    }

    //任务对话
    @RequestMapping(value = "dialogue/task.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage taskScene() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no login!").set("result", "400");
        }

        String input = getRequestString("input");
        String name = getRequestString("name");
        String userid = getRequestString("userid");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String qid = getRequestString("qid");
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isAnyBlank(input, userid, name, bookId, unitId, lessonId, qid)) {
            return failMessage("input param error!");
        }
        return wrapper(mm -> mm.putAll(
                AtomicCallbackBuilderFactory.getInstance()
                        .<MapMessage>newBuilder()
                        .keyPrefix("chipsEnglishContentService.processTaskTalk")
                        .keys(userid, lessonId)
                        .callback(() -> chipsEnglishContentService.processTaskTalk(user, userid, input, name, lessonId, unitId, bookId))
                        .build()
                        .execute()
        ));
    }

    // 分享打卡获取信息
    @RequestMapping(value = "share/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage shareInfo() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isAnyBlank(bookId, unitId)) {
            return failMessage("参数为空");
        }
        return wrapper(mm ->
                mm.putAll(chipsEnglishContentLoader.loadUnitShareInfo(user.getId(), unitId, bookId))
        );
    }


    // 分享打卡回调
    @RequestMapping(value = "user/share.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage shareUser() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        if (StringUtils.isAnyBlank(bookId, unitId)) {
            return failMessage("参数为空");
        }
        return wrapper(mm -> mm.putAll(AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("chipsEnglishContentService.recordUserShare")
                .keys(bookId, unitId)
                .callback(() -> chipsEnglishContentService.recordUserShare(bookId, unitId, user.getId()))
                .build()
                .execute())
        );
    }

    // 情景对话V2
    @RequestMapping(value = "dialogue/feedback.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dialogueFeedback() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String input = getRequestString("input");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("lessonId");
        String qid = getRequestString("qid");
        String uservideo = getRequestString("userVideo");
        if (StringUtils.isAnyBlank(input, bookId, unitId, lessonId, qid)) {
            return failMessage("input param error!");
        }
        ChipsLessonRequest request = new ChipsLessonRequest();
        request.setUnitId(unitId);
        request.setLessonType(LessonType.video_conversation);
        request.setLessonId(lessonId);
        request.setBookId(bookId);
        return wrapper(mm -> mm.putAll(AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("chipsEnglishContentService.processDialogueTalkV2")
                .keys(user.getId(), qid)
                .callback(() -> chipsEnglishContentService.processDialogueTalkV2(user.getId(), input, qid, uservideo, request))
                .build()
                .execute()
        ));
    }

    // 任务对话load question V2
    @RequestMapping(value = "task/content/questionlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage taskQuetionList() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("lessonId");
        String usercode = getRequestString("usercode");
        String roleName = getRequestString("roleName");
        if (StringUtils.isAnyBlank(roleName, bookId, unitId, lessonId, usercode)) {
            return failMessage("input param error!");
        }
        return wrapper(mm -> mm.putAll(chipsEnglishContentLoader.loadTalkRoleQuestionList(user.getId(), usercode, roleName, lessonId, unitId, bookId)));

    }

    // 任务对话V2
    @RequestMapping(value = "task/feedback.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage taskFeedback() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String input = getRequestString("input");
        String bookId = getRequestString("bookId");
        String usercode = getRequestString("usercode");
        String roleName = getRequestString("roleName");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("lessonId");
        String qid = getRequestString("qid");
        if (StringUtils.isAnyBlank(input, bookId, unitId, lessonId, qid, usercode, roleName)) {
            return failMessage("input param error!");
        }

        return wrapper(mm -> mm.putAll(AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("chipsEnglishContentService.processTaskTalkV2")
                .keys(user.getId(), qid)
                .callback(() -> chipsEnglishContentService.processTaskTalkV2(user.getId(), usercode, input, roleName, qid, lessonId, unitId, bookId))
                .build()
                .execute()
        ));
    }


    // 用户微信资料
    @RequestMapping(value = "/user/wxinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage wxInfo() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        String code = getRequestString("wxCode");
        String name = getRequestString("wxName");
        if (StringUtils.isAnyBlank(code)) {
            return failMessage("参数异常!");
        }
        return wrapper(mm -> {
            mm.putAll(chipsEnglishContentService.updateUserWxInfo(user.getId(), code, name));
        });
    }




    // 用户收货信息
    @RequestMapping(value = "/user/recipient.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recipientInfo() {
        User user = currentUser();
        if (user == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请登录!");
        }

        //, String recipientName, String recipientTel, String recipientAddr
        String recipientName = getRequestString("recipientName");
        String recipientTel = getRequestString("recipientTel");
        String recipientAddr = getRequestString("recipientAddr");
        if (StringUtils.isAnyBlank(recipientName)) {
            return failMessage("参数异常!");
        }
        return wrapper(mm -> {
            mm.putAll(chipsEnglishContentService.updateUserRecipientInfo(user.getId(), recipientName, recipientTel, recipientAddr));
        });
    }
}
