package com.voxlearning.wechat.controller.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.ai.client.AiLoaderClient;
import com.voxlearning.utopia.service.ai.client.AiServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.controller.AbstractParentWebController;
import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/ai")
public class AIController extends AbstractParentWebController {
    @Inject
    private AiLoaderClient aiLoaderClient;
    @Inject
    private AiServiceClient aiServiceClient;

    private static final String RES_RESULT = "result";
    private static final String RES_MESSAGE = "message";
    private static String FILE_VOCE_WECHAT = "http://file.api.weixin.qq.com/cgi-bin/media/get";

    // 课程详情
    @RequestMapping(value = "/1.0/classdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage classDetail() {
        String unitId = getRequestString("unitId");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("param error!");
        }
        MapMessage message = aiLoaderClient.getRemoteReference().loadTrialCourseUnitInfo();
        if (message.isSuccess()) {
            message.add(RES_RESULT, "success");
            return message;
        } else {
            message.add(RES_RESULT, "400");
            message.add(RES_MESSAGE, message.getInfo());
            return message;
        }
    }

    // 获取课程题目详情
    @RequestMapping(value = "/1.0/lesson/questions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage questions() {
        String id = getRequestString("id");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("param error!");
        }
        try {
            return aiLoaderClient.getRemoteReference().loadTrialCourseUnitLessonInfo(id);
        } catch (Exception e) {
            logger.error("loadQuestions error. lessonId :{} ,userId:{}", id, user.getId(), e);
            return MapMessage.errorMessage("服务器异常").set("result", "404");
        }

    }

    // 情景对话
    @RequestMapping(value = "/dialogue/scene.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dialogueScene() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no login!").set("result", "400");
        }

        String userid = user.getId() + "-" + getRequestContext().getAuthenticatedOpenId();
        String input = getRequestString("input");

        if (StringUtils.isAnyBlank(input, userid)) {
            return MapMessage.errorMessage("input param error!").set("result", "400");
        }

        return aiServiceClient.loadAndRecordDialogueTalk(user, userid, input, "");
    }

    //任务对话
    @RequestMapping(value = "/dialogue/task.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage taskScene() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no login!").set("result", "400");
        }

        String input = getRequestString("input");
        String name = getRequestString("name");
        String userid = user.getId() + "-" + getRequestContext().getAuthenticatedOpenId();
        if (StringUtils.isAnyBlank(input, userid, name)) {
            return MapMessage.errorMessage("input or name param error!").set("result", "400");
        }
        return aiServiceClient.loadAndRecordTaskTalk(user, userid, input, name, "");
    }


    //打分
    @RequestMapping(value = "/voice/score.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage voiceScore() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("没有登录");
        }
        String openId = getOpenId();
        String id = getRequestString("mediaId");
        String text = getRequestString("text");
        String coeffcient = getRequestString("coeffcient");
        String mode = getRequestString("mode");
        try {
            return  AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("loadAndRecordTaskTalk")
                    .keys(user.getId())
                    .callback(() -> doSocre(id, user.getId(), text, coeffcient, mode))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请勿重复提交").add("result", "400").add("message", "请勿重复提交");
        } catch (Exception ex) {
            logger.error("doSocre  error. openId:{}, mediaId:{}", openId, id, ex);
            return MapMessage.errorMessage("加载异常").add("result", "400").add("message", "加载异常");
        }
    }


    private MapMessage doSocre(String mediaId, Long userId, String text, String coeffcient, String mode) {
        String accessToken = tokenHelper.getAccessToken(WechatType.CHIPS);
        String url = FILE_VOCE_WECHAT + "?access_token=" + accessToken + "&media_id=" + mediaId;
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .get(url)
                .execute();
        if (response == null || response.getStatusCode() != 200) {
            logFetchMediaError(response, mediaId, accessToken, userId);
            return MapMessage.errorMessage("获取微信音频失败");
        }
        if (response.getContentType() == null || response.getContentType().getMimeType().equalsIgnoreCase("application/json")) {
            logFetchMediaError(response, mediaId, accessToken, userId);
            return MapMessage.errorMessage("获取微信音频失败");
        }
        if (RuntimeMode.lt(Mode.PRODUCTION)) {
            logger.info("socre media, url:{}", url);
        }
        logVoiceScore(mediaId, userId, text, coeffcient, mode);
        byte[] bytes = response.getOriginalResponse();
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            return internalGetVoiceScoreJson(inputStream, text,coeffcient, mode, userId);
        } catch (Exception e) {
            return MapMessage.errorMessage("评分异常");
        }

    }
    private MapMessage internalGetVoiceScoreJson(InputStream in, String text, String coefficient, String mode, Long userId) {
        String uuid = UUID.randomUUID().toString();
        if (StringUtils.isBlank(coefficient) || !StringUtils.isNumeric(coefficient)) {
            coefficient = "1.7";
        }
        if (StringUtils.isBlank(mode)) {
            mode = "G";
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("appkey", "zcdmqelsinu6i6dliln6nqowfq2v2euvduhzqxqx");
        headers.put("session-id", uuid);
        headers.put("device-id", userId.toString());
        headers.put("score-coefficient", coefficient);

        try {
            if (MapUtils.isEmpty(JsonUtils.fromJson(text))) {
                text = "{\"DisplayText\":\"" + text + "\",\"Language\":\"en\"}";
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("mode", StringUtils.upperCase(mode));
            builder.addTextBody("text", text, ContentType.create("text/plain", Consts.UTF_8));
            builder.addBinaryBody("voice", in, ContentType.DEFAULT_BINARY, RandomStringUtils.randomAlphanumeric(8));
            // connect_timeout = 5000, socket_timeout = 10000
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                        .post("http://edu.hivoice.cn:8085/eval/amrnb")
                        .headers(headers)
                        .entity(builder.build())
                        .execute();
            // 获取返回数据
            String sessionId = response.getFirstHeader("Session-Id").getValue();
            String[] session = StringUtils.split(sessionId, ":");
            String voiceUrl = "http://edu.hivoice.cn/WebAudio-1.0-SNAPSHOT/audio/play/" + session[2] + "/" + session[1] + "/" + session[0];
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod1", response.getStatusCode(),
                    "mod2", response.getResponseString(),
                    "mod3", text,
                    "mod4", coefficient,
                    "mod5", mode,
                    "mod6", voiceUrl,
                    "mod7", getRequestContext().getAuthenticatedOpenId(),
                    "mod8", getRequestContext().getRealRemoteAddress(),
                    "op", "UsingWechatBackgroundScore",
                    "time", System.currentTimeMillis()
            ));
            return MapMessage.successMessage().add("score_json", response.getResponseString()).add("file_url", voiceUrl);
        } catch (Exception e) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "op", "Wechat UsingBackgroundScore Exception",
                    "mod1", e
            ));
            return MapMessage.errorMessage("调用语音评测异常");
        }
    }


//    private static MapMessage internalGetOpusVoiceScoreJson(InputStream in, String text, String coefficient, String mode, Long userId) {
//        String uuid = UUID.randomUUID().toString();
//        if (StringUtils.isBlank(coefficient) || !StringUtils.isNumeric(coefficient)) {
//            coefficient = "1.6";
//        }
//        if (StringUtils.isBlank(mode)) {
//            mode = "E";
//        }
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("appkey", "zcdmqelsinu6i6dliln6nqowfq2v2euvduhzqxqx");
//        headers.put("session-id", uuid);
//        headers.put("device-id", userId.toString());
//        headers.put("score-coefficient", coefficient);
//
//        try {
//            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//            builder.addTextBody("mode", StringUtils.upperCase(mode));
//            builder.addTextBody("text", text, ContentType.create("text/plain", Consts.UTF_8));
//            builder.addBinaryBody("voice", in, ContentType.DEFAULT_BINARY, RandomStringUtils.randomAlphanumeric(8));
//            // connect_timeout = 5000, socket_timeout = 10000
//            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
//                    .post("http://edu.hivoice.cn:8085/eval/opus")
//                    .headers(headers)
//                    .entity(builder.build())
//                    .execute();
//            // 获取返回数据
//            String sessionId = response.getFirstHeader("Session-Id").getValue();
//            String[] session = StringUtils.split(sessionId, ":");
//            String voiceUrl = "http://edu.hivoice.cn/WebAudio-1.0-SNAPSHOT/audio/play/" + session[2] + "/" + session[1] + "/" + session[0];
//            LogCollector.info("backend-general", MapUtils.map(
//                    "env", RuntimeMode.getCurrentStage(),
//                    "usertoken", userId,
//                    "mod1", response.getStatusCode(),
//                    "mod2", response.getResponseString(),
//                    "mod3", text,
//                    "mod4", coefficient,
//                    "mod5", mode,
//                    "mod6", voiceUrl,
////                    "mod7", getRequestContext().getAuthenticatedOpenId(),
////                    "mod8", getRequestContext().getRealRemoteAddress(),
//                    "op", "UsingWechatBackgroundScore",
//                    "time", System.currentTimeMillis()
//            ));
//            return MapMessage.successMessage().add("score_json", response.getResponseString()).add("file_url", voiceUrl);
//        } catch (Exception e) {
//            LogCollector.info("backend-general", MapUtils.map(
//                    "env", RuntimeMode.getCurrentStage(),
//                    "usertoken", userId,
//                    "op", "Wechat UsingBackgroundScore Exception",
//                    "mod1", e
//            ));
//            return MapMessage.errorMessage("调用语音评测异常");
//        }
//    }


    private User currentUser() {
//        String openId = getRequestContext().getAuthenticatedOpenId();
//        return wechatLoaderClient.loadWechatUser(openId);
        User user = new User();
        user.setId(262598L);
        return user;
    }

    private void logFetchMediaError(AlpsHttpResponse response, String mediaId, String accessToken, Long userId) {
        if (RuntimeMode.lt(Mode.STAGING)) {
           logger.error("fetch wechat voice media error. response:{}, mediaid:{},access_token:{}", response, mediaId, accessToken);
        } else {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "op", "fetch wechat voice media error",
                    "mod1", response,
                    "mod2", mediaId,
                    "mod3", accessToken
            ));
        }
    }

    private void logVoiceScore(String mediaId, Long userId, String text, String coeffcient, String mode)  {
        if (RuntimeMode.lt(Mode.STAGING)) {
            logger.info("wechat voice score. mediaId:{}, userId:{},text:{},coeffcient:{}, mode:{} ", mediaId, userId, text, coeffcient, mode);
        } else {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod1", mediaId,
                    "mod3", text,
                    "mod4", coeffcient,
                    "mod5", mode,
                    "mod7", getRequestContext().getAuthenticatedOpenId(),
                    "mod8", getRequestContext().getRealRemoteAddress(),
                    "op", "wechat voice score",
                    "time", System.currentTimeMillis()
            ));
        }
    }
    public static void main(String[] args) {
//        String requestUrl = "http://10.6.3.241:1889/?group=alps-hydra-production&method=generateAccessToken&version=2017.02.09&service=com.voxlearning.utopia.service.wechat.api.WechatCodeService";
//        String requestParams = "{\"paramValues\":[\""+ WechatType.CHIPS.name() + "\"]}";
//        String response = HttpRequestExecutor.defaultInstance()
//                .post(requestUrl)
//                .headers(new BasicHeader("Content-Type", "application/json"))
//                .json(requestParams)
//                .execute().getResponseString();
//        System.out.println(response);

//        try(InputStream in = new FileInputStream(new File("D://aat3.amr"))) {
//            Map<String, Object> res = internalGetVoiceScoreJson(in, "where is your jacket", null, "G", 30023L);
//            System.out.println(JsonUtils.toJson(res));
//        } catch (Exception e) {
//
//        }

//        String url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=10_u-OvuXj8e6xMTC-ZraITuuJhp1522G4RDYfA0OKJgNIddEnigpF6egCFOCsJUZK8krPoV_bys-z5UYV1vc64G9VuT6QKpeABx050Lx1SteXaJjClgyUqOKwPSPgYUMjAAAGCU&media_id=fE7bfDpROfkIEuepqfskb7pfjxDB70uzuX9kXyoL-QX2HJ7c4f81uiuZG94mZbWx";
//        String text1 = "{\\\"Version\\\":\\\"1\\\",\\\"DisplayText\\\":\\\"Jsgf Grammar Tool Generated\\\",\\\"GrammarWeight\\\":\\\"{\\\\\\\"weight_struct\\\\\\\":[]}\\\",\\\"Grammar\\\":\\\"#JSGF V1.0 utf-8 cn;\\\\ngrammar main;\\\\npublic <main> = \\\\\\\"<s>\\\\\\\"(<c>where is your jacket|<c>where's your jacket|<c>do you bring your jacket|<c>did you bring your jacket|<c>is your jacket in your luggage|<c>is your jacket in your backpack|where is your jacket|where's your jacket|do you bring your jacket|did you bring your jacket|is your jacket in your luggage|is your jacket in your backpack|is your jacket ready|is your jacket ok|check your jacket|your jacket|your jacket ready|your jacket ok|jacket ok|jacket ready|what do you bring|what did you bring|what you bring|what bring|<q>your<n>|your<n>)\\\\\\\"</s>\\\\\\\";\\\\n<c> = (let me help you|let me help|let me help you check|let me check);\\\\n<n> = (sneakers|passport);\\\\n<q> = (where is|where's|where are|where're|is|are|do you bring|did you bring|check);\\\\n\\\"}";
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
//                .get(url)
//                .execute();
//        try (InputStream inputStream = new ByteArrayInputStream(response.getOriginalResponse())) {
//            System.out.println(JsonUtils.toJson(internalGetVoiceScoreJson(inputStream, "{\"DisplayText\":\"Great.See you in Beijing\",\"Language\":\"en\"}",null, "G", 30012L)));
//        } catch (Exception e) {
//            System.err.println("error e:" + e );
//        }

    }
}
