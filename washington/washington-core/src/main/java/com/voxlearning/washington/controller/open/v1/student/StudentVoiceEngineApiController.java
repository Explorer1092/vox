package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.api.monitor.PublishMonitorGenericCountEvent;
import com.voxlearning.alps.api.monitor.PublishMonitorGenericInvocationEvent;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.Cleanup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SkipPrettyLogger;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_SUBJECTIVE_UPLOAD_FAIL_MSG;

/**
 * app端语音打分接口
 * 注：这个接口变成通用的了，不仅仅适用于学生端
 *
 * @author xuesong.zhang
 * @since 2016-05-26
 */
@Controller
@RequestMapping(value = "/v1/student/voice/")
@Slf4j
public class StudentVoiceEngineApiController extends AbstractApiController {

    private static final String DEFAULT_APP_KEY = "5d702d58446a11e8a6888cec4b45cd20";
    private static final String AI_TEACHER_APP_KEY = "5d700648446a11e8a8a78cec4b45cd20";

    private static final String JUNIOR_VOICE_17URL = "http://vox_jun.17zuoye.com/compute";
    private static final String MIDDLE_VOICE_17URL = "http://vox_mid.17zuoye.com/compute";

    private static final String REQ_APP_KEY_JUNIOR = "17JuniorStu";

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    /**
     * 上传入口
     * file_info:{"size":"文件大小","text":"阅读文本","back_voice_coefficient","back_voice_mode"}
     */
    @SkipPrettyLogger
    @RequestMapping(value = "score.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage voiceScore() {
        MapMessage mapMessage = new MapMessage();
        try {
            validateRequest(REQ_FILE_INFO);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                mapMessage.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                mapMessage.add(RES_MESSAGE, e.getMessage());
                return mapMessage;
            }
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, e.getMessage());
            return mapMessage;
        }

        String reqAppKey = getRequestString(REQ_APP_KEY);
        String sys = getRequestString(REQ_SYS);
        String fileInfo = getRequestString(REQ_FILE_INFO);
        Map<String, Object> map = JsonUtils.fromJson(fileInfo);
        if (MapUtils.isEmpty(map)) {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
            return mapMessage;
        }

        User user = getApiRequestUser();
        if (user == null || user.getId() == null) {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, RES_RESULT_USER_ACCOUNT_NOT_EXIST_MSG);
            return mapMessage;
        }
        Map<String, Object> resultMap = new HashMap<>();
        try {

            String ua = getRequest().getHeader("User-Agent");
            String ip = getWebRequestContext().getRealRemoteAddress();
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            MultipartFile file = multipartRequest.getFile(REQ_FILE);
            if (file != null) {
                resultMap = atomicLockManager.wrapAtomic(this)
                        .keys(user.getId())
                        .proxy()
                        .doUploadResult(user.getId(), file, map, ua, ip, sys, reqAppKey);
            }

            if (MapUtils.isEmpty(resultMap) || resultMap.get("score_json") == null || StringUtils.isBlank((String) resultMap.get("score_json"))) {
                mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                mapMessage.add(RES_MESSAGE, RES_RESULT_SCORE_IDS_ERROR);
                return mapMessage;
            }

        } catch (DuplicatedOperationException ex) {
            logger.warn("Upload voice writing (DUPLICATED OPERATION): (userId={})", user.getId(), ex.getMessage());
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return mapMessage;
        } catch (RuntimeException ex) {
            logger.warn("Upload voice failed writing: (userId={})", user.getId(), ex.getMessage());
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, ex.getMessage());
            return mapMessage;
        } catch (Exception ex) {
            logger.warn("Upload voice failed writing: (userId={})", user.getId(), ex.getMessage());
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
            return mapMessage;
        }

        if (MapUtils.isEmpty(resultMap)) {
            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mapMessage.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
            return mapMessage;
        }

        mapMessage.add(RES_VOICE_SCORE_INFO, resultMap);
        mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
        return mapMessage;
    }

    /**
     * 云知声保存文件
     *
     * @param userId      用户id
     * @param file        文件
     * @param fileInfoMap 文件信息map
     * @return map
     */
    public Map<String, Object> doUploadResult(Long userId, MultipartFile file, Map<String, Object> fileInfoMap, String ua, String ip, String sys, String reqAppKey) {
        if (file.getSize() != Long.valueOf((Integer) fileInfoMap.get("size"))) {
            return Collections.emptyMap();
        }
        Map<String, Object> resultMap = new HashMap<>();
        String text = SafeConverter.toString(fileInfoMap.get("text"), "");
        String coefficient = SafeConverter.toString(fileInfoMap.get("back_voice_coefficient"), ""); // 打分系数
        if (StringUtils.isEmpty(coefficient)) {
            coefficient = SafeConverter.toString(fileInfoMap.get("voiceCoefficient"), "");
        }
        String mode = SafeConverter.toString(fileInfoMap.get("back_voice_mode"), ""); // 打分模式
        if (StringUtils.isEmpty(mode)) {
            mode = SafeConverter.toString(fileInfoMap.get("model"), "");
        }
        String language = SafeConverter.toString(fileInfoMap.get("back_voice_type"), "");
        if (StringUtils.isEmpty(language)) {
            language = SafeConverter.toString(fileInfoMap.get("voice_type"), "");
        }
        Map<String, Object> expand = (Map) fileInfoMap.get("expand"); // 扩展属性
        String appkey = DEFAULT_APP_KEY;
        if (MapUtils.isNotEmpty(expand)) {
            VoiceExpand voiceExpand = JsonUtils.safeConvertMapToObject(expand, VoiceExpand.class);
            if (voiceExpand != null && StringUtils.isNotBlank(voiceExpand.getSource())) {
                appkey = voiceExpand.getSource();
            }
        }

        // AI_Teacher打分不受灰度控制
        if (StringUtils.equalsIgnoreCase(AI_TEACHER_APP_KEY, appkey)) {
            get17ZYVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, appkey, sys, fileInfoMap, reqAppKey);
            return resultMap;
        }

        // 中学部分的打分
        if (StringUtils.equalsIgnoreCase(REQ_APP_KEY_JUNIOR, reqAppKey)) {
            middleVoice(file, text, coefficient, mode, userId, resultMap, ua, ip, language, appkey, sys, fileInfoMap, reqAppKey);
            return resultMap;
        }

        String use17VoiceScoreStr = SafeConverter.toString(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "USE_17VOICE_SCORE")
                , ""
        );

        String[] use17VoiceScore = StringUtils.split(use17VoiceScoreStr, ",");
        if (StringUtils.containsIgnoreCase(text, "#JSGF") || StringUtils.equalsIgnoreCase(language, Subject.CHINESE.name())) {
            // JSGF打分一律用云知声，这个事儿可以问章鱼或者俊杰
            getVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, fileInfoMap, reqAppKey);
        } else {
            if (StringUtils.isNotBlank(use17VoiceScoreStr) && use17VoiceScore.length == 2) {
                // use17VoiceScoreStr示例，(true，1|2|3)，使用自研打分，并且学生id最后一位包含在其中
                List<String> userIds = Arrays.asList(StringUtils.split(use17VoiceScore[1], "|"));
                String last = StringUtils.substring(String.valueOf(userId), String.valueOf(userId).length() - 1);
                if (CollectionUtils.containsAny(userIds, Collections.singletonList(last))) {
                    // 命中
                    get17ZYVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, appkey, sys, fileInfoMap, reqAppKey);
                } else {
                    // 没命中
                    getVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, fileInfoMap, reqAppKey);
                }
            } else if (StringUtils.isNotBlank(use17VoiceScoreStr)
                    && use17VoiceScore.length == 1
                    && SafeConverter.toBoolean(use17VoiceScore[0], false)) {
                // use17VoiceScoreStr示例，(true/false)，全量使用或不使用自研打分
                get17ZYVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, appkey, sys, fileInfoMap, reqAppKey);
            } else {
                // 用云知声把~
                getVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, fileInfoMap, reqAppKey);
            }
        }

        return resultMap;
    }

    /**
     * 中学打分的特殊处理
     */
    public void middleVoice(MultipartFile file, String text, String coefficient, String mode, Long userId, Map<String, Object> resultMap, String ua, String ip, String language, String appkey, String sys, Map<String, Object> fileInfoMap, String reqAppKey) {
        String use17VoiceScoreStr = SafeConverter.toString(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "JUNIOR_USE_17VOICE_SCORE")
                , ""
        );
        String[] use17VoiceScore = StringUtils.split(use17VoiceScoreStr, ",");
        if (StringUtils.isNotBlank(use17VoiceScoreStr) && use17VoiceScore.length == 2) {
            // use17VoiceScoreStr示例，(true，1|2|3)，使用自研打分，并且学生id最后一位包含在其中1
            List<String> userIds = Arrays.asList(StringUtils.split(use17VoiceScore[1], "|"));
            String last = StringUtils.substring(String.valueOf(userId), String.valueOf(userId).length() - 1);
            if (CollectionUtils.containsAny(userIds, Collections.singletonList(last))) {
                // 命中
                get17ZYVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, appkey, sys, fileInfoMap, reqAppKey);
            } else {
                // 没命中
                getVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, fileInfoMap, reqAppKey);
            }
        } else if (StringUtils.isNotBlank(use17VoiceScoreStr)
                && use17VoiceScore.length == 1
                && SafeConverter.toBoolean(use17VoiceScore[0], false)) {
            // use17VoiceScoreStr示例，(true/false)，全量使用或不使用自研打分
            get17ZYVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, appkey, sys, fileInfoMap, reqAppKey);
        } else {
            // 用云知声把~
            getVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language, fileInfoMap, reqAppKey);
        }
    }

    /**
     * 自己研发的语音打分
     */
    public void get17ZYVoiceScoreJson(MultipartFile multipartFile, String text, String coefficient, String mode, Long userId, Map<String, Object> resultMap, String ua, String ip, String language, String appkey, String sys, Map<String, Object> fileInfoMap, String reqAppKey) {
        Instant start = Instant.now();
        try {
            internalGet17ZYVoiceScoreJson(multipartFile, text, coefficient, mode, userId, resultMap, ua, ip, language, appkey, sys, fileInfoMap, reqAppKey);
        } finally {
            Instant stop = Instant.now();
            long duration = stop.toEpochMilli() - start.toEpochMilli();
            PublishMonitorGenericInvocationEvent.publish("YZYInvocation", stop.getEpochSecond(), duration);
            if (duration >= 3000) {
                PublishMonitorGenericCountEvent.publish("YZYTimeout", stop.getEpochSecond(), 1);
            }
        }
    }

    public void internalGet17ZYVoiceScoreJson(MultipartFile multipartFile, String text, String coefficient, String mode, Long userId, Map<String, Object> resultMap, String ua, String ip, String language, String appkey, String sys, Map<String, Object> fileInfoMap, String reqAppKey) {
        String uuid = UUID.randomUUID().toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("appkey", appkey);
        headers.put("session-id", uuid);
        headers.put("device-id", userId.toString());
        headers.put("protocol", "http");
        headers.put("sys", sys);

        // 打分默认用小学的
        String voiceScoreUrl = JUNIOR_VOICE_17URL;
        if (StringUtils.equalsIgnoreCase(REQ_APP_KEY_JUNIOR, reqAppKey)) {
            // 如果是中学的就改
            voiceScoreUrl = MIDDLE_VOICE_17URL;
        }

        try {
            @Cleanup InputStream in = multipartFile.getInputStream();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("mode", StringUtils.upperCase(mode));
            builder.addTextBody("text", text);
            builder.addTextBody("codec", "opus");
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
            resultMap.put("score_json", scoreJson);
            resultMap.put("file_url", voiceUrl);

            // 我是日志
            Map<String, String> logMap = MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "agent", ua,
                    "mod1", voiceUrl,
                    "mod2", multipartFile.getSize(),
                    "mod3", response.getStatusCode(),
                    "mod4", JsonUtils.toJson(fileInfoMap),
                    "mod5", ip,
                    "mod6", "17zuoye",
                    "mod7", language,
                    "mod8", response.getResponseString(),
                    "mod9", appkey,
                    "mod10", reqAppKey,
                    "mod11", voiceScoreUrl,
                    "op", "UsingBackgroundScore",
                    "time", System.currentTimeMillis()
            );
            if (RuntimeMode.le(Mode.TEST)) {
                logger.info(JsonUtils.toJson(logMap));
            }
            LogCollector.info("backend-general", logMap);
        } catch (Exception e) {
            Map<String, String> logMap = MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod6", "17zuoye",
                    "mod9", appkey,
                    "op", "UsingBackgroundScore Exception",
                    "mod1", e
            );
            logger.error("backend-general exception: " + JsonUtils.toJson(logMap));
            LogCollector.info("backend-general", logMap);
        }
    }

    /**
     * 云知声打分，配置的东西暂时先扔在这
     *
     * @param multipartFile 文件
     * @param text          参照文本
     * @param userId        用户id
     * @param resultMap     结果map
     */
    public void getVoiceScoreJson(MultipartFile multipartFile, String text, String coefficient, String mode, Long userId, Map<String, Object> resultMap, String ua, String ip, String language, Map<String, Object> fileInfoMap, String reqAppKey) {
        Instant start = Instant.now();
        try {
            internalGetVoiceScoreJson(multipartFile, text, coefficient, mode, userId, resultMap, ua, ip, language, fileInfoMap, reqAppKey);
        } finally {
            Instant stop = Instant.now();
            long duration = stop.toEpochMilli() - start.toEpochMilli();
            PublishMonitorGenericInvocationEvent.publish("YZSInvocation", stop.getEpochSecond(), duration);
            if (duration >= 3000) {
                PublishMonitorGenericCountEvent.publish("YZSTimeout", stop.getEpochSecond(), 1);
            }
        }
    }

    public void internalGetVoiceScoreJson(MultipartFile multipartFile, String text, String coefficient, String mode, Long userId, Map<String, Object> resultMap, String ua, String ip, String language, Map<String, Object> fileInfoMap, String reqAppKey) {
        String uuid = UUID.randomUUID().toString();
        if (StringUtils.isBlank(coefficient) || !StringUtils.isNumeric(coefficient)) {
            coefficient = "1.6";
        }

        if (StringUtils.isBlank(mode)) {
            mode = "E";
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("appkey", "zcdmqelsinu6i6dliln6nqowfq2v2euvduhzqxqx");
        headers.put("session-id", uuid);
        headers.put("device-id", userId.toString());
        headers.put("score-coefficient", coefficient);

        try {
            @Cleanup InputStream in = multipartFile.getInputStream();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("mode", StringUtils.upperCase(mode));
            builder.addTextBody("text", text, ContentType.create("text/plain", Consts.UTF_8));
            builder.addBinaryBody("voice", in, ContentType.DEFAULT_BINARY, RandomStringUtils.randomAlphanumeric(8));
            // connect_timeout = 5000, socket_timeout = 10000

            AlpsHttpResponse response;
            if (StringUtils.equalsIgnoreCase(language, Subject.CHINESE.name())) {
                headers.put("X-EngineType", "oral.zh_CH");
                // 中文打分
                response = HttpRequestExecutor.defaultInstance()
                        .post("http://cn-edu.hivoice.cn/eval/opus")
                        .headers(headers)
                        .entity(builder.build())
                        .execute();
            } else {
                // 英文打分
                response = HttpRequestExecutor.defaultInstance()
                        .post("http://edu.hivoice.cn:8085/eval/opus")
                        .headers(headers)
                        .entity(builder.build())
                        .execute();
            }

            // 获取返回数据
            String sessionId = response.getFirstHeader("Session-Id").getValue();
            String[] session = StringUtils.split(sessionId, ":");
            String voiceUrl = "http://edu.hivoice.cn/WebAudio-1.0-SNAPSHOT/audio/play/" + session[2] + "/" + session[1] + "/" + session[0];

            resultMap.put("score_json", response.getResponseString());
            resultMap.put("file_url", voiceUrl);

            Map<String, String> logMap = MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "agent", ua,
                    "mod1", voiceUrl,
                    "mod2", multipartFile.getSize(),
                    "mod3", response.getStatusCode(),
                    "mod4", JsonUtils.toJson(fileInfoMap),
                    "mod5", ip,
                    "mod6", "hivoice",
                    "mod7", language,
                    "mod8", response.getResponseString(),
                    "mod10", reqAppKey,
                    "op", "UsingBackgroundScore",
                    "time", System.currentTimeMillis()
            );
            if (RuntimeMode.le(Mode.TEST)) {
                logger.info(JsonUtils.toJson(logMap));
            }
            LogCollector.info("backend-general", logMap);
        } catch (Exception e) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod6", "hivoice",
                    "op", "UsingBackgroundScore Exception",
                    "mod1", e
            ));
        }
    }

    @Data
    public static class VoiceExpand implements Serializable {
        private static final long serialVersionUID = 8950345902599730244L;
        /**
         * 来源
         */
        public String source;
    }
}
