package com.voxlearning.luffy.controller.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserContentLoader;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserContentService;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.ChipsLessonRequest;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;
import com.voxlearning.utopia.service.ai.data.ChipsWechatUser;
import com.voxlearning.utopia.service.question.api.StoneDataLoader;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@Slf4j
@RequestMapping(value = "/chips/content")
public class ChipsContentController extends AbstractChipsController {

    @ImportService(interfaceClass = ChipsWechatUserContentLoader.class)
    private ChipsWechatUserContentLoader chipsWechatUserContentLoader;

    @ImportService(interfaceClass = ChipsWechatUserContentService.class)
    private ChipsWechatUserContentService chipsWechatUserContentService;

    @ImportService(interfaceClass = StoneDataLoader.class)
    private StoneDataLoader stoneDataLoader;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    private static String USER_VIDEO_PREFIX = "chips/mini/program/uservideo/";

    private static final String JUNIOR_VOICE_17URL = "http://vox_jun.17zuoye.com/compute";

    private static final String SCORE_APP_KEY = "afbad60f360c4e0db8baea08ca1b21a3";

    private static String HOST = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host"));

    @RequestMapping(value = "trial/courselist.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage trialCourseList() {
        ChipsWechatUser wechatUser = getWechatUser();
        return wrapper(mm -> mm.putAll(chipsWechatUserContentLoader.loadTrailCourse(wechatUser.getWechatUserId())));
    }

    @RequestMapping(value = "lessonInfo.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage trialLessonInfo() {
        String lessonId = getRequestString("lessonId");
        String unitId = getRequestString("unitId");
        String bookId = getRequestString("bookId");
        if (StringUtils.isAnyBlank(lessonId, unitId, bookId)) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }
        ChipsLessonRequest lessonRequest = new ChipsLessonRequest();
        lessonRequest.setBookId(bookId);
        lessonRequest.setLessonId(lessonId);
        lessonRequest.setUnitId(unitId);
        return wrapper(mm -> mm.putAll(chipsWechatUserContentLoader.loadLessonInfo(getWechatUser().getWechatUserId(), lessonRequest)));
    }


    @RequestMapping(value = "dialogue/feedback.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage dialogueFeedback(@RequestParam("file") MultipartFile file) {
        if (file == null) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }
        String text = getRequestString("jsgf");
        String qid = getRequestString("qid");
        String lessonId = getRequestString("lessonId");
        String unitId = getRequestString("unitId");
        String bookId = getRequestString("bookId");
        if (StringUtils.isAnyBlank(text, qid, lessonId, unitId, bookId)) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }
        String mode = getRequestParameter("mode", "E");
        ChipsWechatUser user = getWechatUser();
        String agent = getRequestString("ua");
        String sys = getRequestString("sys");
        return wrapper(mm -> mm.putAll(AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("chipsWechatUserContentService.processDialogueFeedback")
                .keys(user.getWechatUserId())
                .callback(() -> {
                    Map<String, Object> resultMap = internalGet17ZYVoiceScoreJson(file, text, mode, user.getWechatUserId(), SCORE_APP_KEY, agent, sys);
                    if (MapUtils.isEmpty(resultMap)) {
                        return failMapMessage(ChipsErrorType.SCORE_ERROR);
                    }
                    ChipsLessonRequest chipsLessonRequest = new ChipsLessonRequest();
                    chipsLessonRequest.setUnitId(unitId);
                    chipsLessonRequest.setLessonId(lessonId);
                    chipsLessonRequest.setBookId(bookId);
                    chipsLessonRequest.setLessonType(LessonType.video_conversation);
                    String input = SafeConverter.toString(resultMap.get("score_json"));
                    MapMessage mapMessage = chipsWechatUserContentService.processDialogueFeedback(user.getWechatUserId(), input, qid, chipsLessonRequest);
                    if (mapMessage.isSuccess()) {
                        mapMessage.putAll(resultMap);
                    }
                    return mapMessage;
                })
                .build()
                .execute()));
    }


    @RequestMapping(value = "question/processresult.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage trailQuestionProcessResult() {
        String json = getRequestString("data");
        ChipsQuestionResultRequest request = JsonUtils.fromJson(json, ChipsQuestionResultRequest.class);
        if (request == null || request.getUnitLast() == null || request.getUnitLast() == null ||
                StringUtils.isAnyBlank(request.getBookId(), request.getLessonId(), request.getQid(), request.getUnitId(), request.getQuestionType())) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }
        ChipsWechatUser user = getWechatUser();
        return wrapper(mm -> mm.putAll(
                AtomicCallbackBuilderFactory.getInstance()
                        .<MapMessage>newBuilder()
                        .keyPrefix("chipsWechatUserContentService.processQuestionResult")
                        .keys(user.getWechatUserId(), request.getQid())
                        .callback(() -> chipsWechatUserContentService.processQuestionResult(user.getWechatUserId(), request))
                        .build()
                        .execute()
        ));

    }

    @RequestMapping(value = "trial/unitreport.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage trailUnitReport() {
        String unitId = getRequestString("unitId");
        String bookId = getRequestString("bookId");
        if (StringUtils.isAnyBlank(unitId, bookId)) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }

        return wrapper(mm -> mm.putAll(chipsWechatUserContentLoader.loadUnitResult(getWechatUser().getWechatUserId(), bookId, unitId)));
    }


    @RequestMapping(value = "trial/lessonresult.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage trailLessonResult() {
        String lessonId = getRequestString("lessonId");
        String unitId = getRequestString("unitId");
        String bookId = getRequestString("bookId");
        if (StringUtils.isAnyBlank(lessonId, unitId, bookId)) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }
        ChipsLessonRequest lessonRequest = new ChipsLessonRequest();
        lessonRequest.setBookId(bookId);
        lessonRequest.setLessonId(lessonId);
        lessonRequest.setUnitId(unitId);
        return wrapper(mm -> mm.putAll(chipsWechatUserContentLoader.loadLessonResult(getWechatUser().getWechatUserId(), lessonRequest)));
    }

    @RequestMapping(value = "trial/video/synthesis.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage trialSynthesisVideo() {
        String lessonId = getRequestString("lessonId");
        String unitId = getRequestString("unitId");
        String bookId = getRequestString("bookId");
        String videos = getRequestString("video");
        if (StringUtils.isAnyBlank(lessonId, unitId, bookId, videos)) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }
        ChipsWechatUser user = getWechatUser();
        ChipsLessonRequest lessonRequest = new ChipsLessonRequest();
        lessonRequest.setBookId(bookId);
        lessonRequest.setLessonId(lessonId);
        lessonRequest.setUnitId(unitId);
        return wrapper(mm -> mm.putAll(AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("chipsWechatUserContentService.synthesisUserVideo")
                .keys(user.getWechatUserId())
                .callback(() -> chipsWechatUserContentService.synthesisUserVideo(getWechatUser().getWechatUserId(), lessonRequest, Arrays.stream(videos.split(",")).collect(Collectors.toList())))
                .build()
                .execute()));
    }

    @RequestMapping(value = "video/upload.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage uploadVideo(@RequestParam("file") MultipartFile file) {
        String fileName = getWechatUser().getWechatUserId() + "_" + RandomUtils.nextObjectId() + ".mp4";
        String prefix = USER_VIDEO_PREFIX + (RuntimeMode.current().lt(Mode.STAGING) ? "test" : "");
        String fileUrl = HOST + prefix + "/" + fileName;
        try {
            @Cleanup InputStream in = file.getInputStream();
            StorageMetadata metadata = new StorageMetadata();
            metadata.setContentType("video/mp4");
            metadata.setContentLength(file.getSize());
            storageClient.upload(in, fileName, prefix, metadata);
        } catch (Exception e) {
            return failMapMessage(ChipsErrorType.SERVER_ERROR);
        }
        return MapMessage.successMessage().set("fileUrl", fileUrl);
    }

    private Map<String, Object> internalGet17ZYVoiceScoreJson(MultipartFile multipartFile, String text, String mode, Long userId, String appkey, String ua, String sys) {
        String uuid = UUID.randomUUID().toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("appkey", appkey);
        headers.put("session-id", uuid);
        headers.put("device-id", userId.toString());
        headers.put("protocol", "http");
        headers.put("sys", sys);

        String ip = getRequestContext().getRealRemoteAddress();
        String voiceScoreUrl = JUNIOR_VOICE_17URL;

        try {
            @Cleanup InputStream in = multipartFile.getInputStream();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("mode", StringUtils.upperCase(mode));
            builder.addTextBody("text", text);
            builder.addTextBody("codec", "mp3");
            builder.addBinaryBody("voice", in, ContentType.DEFAULT_BINARY, RandomStringUtils.randomAlphanumeric(8));
            // connect_timeout = 5000, socket_timeout = 10000
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .post(voiceScoreUrl)
                    .headers(headers)
                    .entity(builder.build())
                    .execute();

            // 获取返回数据
            String scoreJson = response.getResponseString();
            Map<String, Object> scoreMap = JsonUtils.convertJsonObjectToMap(scoreJson);
            String voiceUrl = MapUtils.isNotEmpty(scoreMap) ? SafeConverter.toString(scoreMap.get("voiceURI"), "") : "";
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("score_json", scoreJson);
            resultMap.put("file_url", voiceUrl);

            Map<String, String> logMap = MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "agent", ua,
                    "mod1", voiceUrl,
                    "mod2", multipartFile.getSize(),
                    "mod3", response.getStatusCode(),
                    "mod4", ip,
                    "mod5", sys,
                    "mod6", response.getResponseString(),
                    "mod7", appkey,
                    "mod8", voiceScoreUrl,
                    "op", "UsingBackgroundScoreInChipsMiniProgram",
                    "time", System.currentTimeMillis()
            );
            if (RuntimeMode.le(Mode.TEST)) {
                logger.info(JsonUtils.toJson(logMap));
            }
            LogCollector.info("backend-general", logMap);

            return resultMap;
        } catch (Exception e) {
            Map<String, String> logMap = MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod7", appkey,
                    "op", "UsingBackgroundScoreInChipsMiniProgram Exception",
                    "mod1", e
            );
            logger.error("backend-general exception: " + JsonUtils.toJson(logMap));
            LogCollector.info("backend-general", logMap);
            return Collections.emptyMap();
        }
    }
    
    @RequestMapping(value = "question/load.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage loadQuestion() {
        String qid = getRequestString("qid");

        if (StringUtils.isAnyBlank(qid)) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }

        return wrapper(mm -> {
            StoneData data = Optional.ofNullable(stoneDataLoader.loadStoneDataIncludeDisabled(Collections.singletonList(qid))).map(ma -> ma.get(qid)).orElse(null);
            mm.set("data", data);
        });
    }

    @RequestMapping(value = "voice/score.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage voiceScore(@RequestParam("file") MultipartFile file) {
        if (file == null) {
            return failMapMessage(ChipsErrorType.PARAMETER_ERROR);
        }
        ChipsWechatUser user = getWechatUser();

        String text = getRequestString("text");
        String mode = getRequestParameter("mode", "E");
        String agent = getRequestString("ua");
        String sys = getRequestString("sys");
        return wrapper(mm -> mm.putAll(AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("chipsWechatUserContentController.internalGet17ZYVoiceScore")
                .keys(user.getWechatUserId())
                .callback(() -> {
                    Map<String, Object> resultMap = internalGet17ZYVoiceScoreJson(file, text, mode, user.getWechatUserId(), SCORE_APP_KEY, agent, sys);
                    if (MapUtils.isEmpty(resultMap)) {
                        return failMapMessage(ChipsErrorType.SCORE_ERROR);
                    }
                    MapMessage mapMessage = MapMessage.successMessage();
                    mapMessage.putAll(resultMap);
                    return mapMessage;
                })
                .build()
                .execute()));
    }
}
