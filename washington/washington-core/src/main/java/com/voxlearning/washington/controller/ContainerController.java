/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.question.api.entity.XxWorkbookContent;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author tanguohong on 14-3-19.
 */
@Controller
@RequestMapping("/container")
@Slf4j
@NoArgsConstructor
public class ContainerController extends AbstractController {

    /**
     * 随堂练题目预览
     * 提供给后台管理或者未登录用户预览试卷使用
     *
     * @param model   model
     * @param request request
     * @return string
     */
    @RequestMapping(value = "viewpaper.vpage", method = RequestMethod.GET)
    public String viewPaper(Model model, HttpServletRequest request) {
        String qid = request.getParameter("qid");
        String pid = request.getParameter("pid");
        String wcoid = request.getParameter("wcoid");

        if (StringUtils.isNotBlank(pid) && StringUtils.startsWithIgnoreCase(pid, "P_")) {
            List<String> qridList = paperLoaderClient.loadQidRidAsMapByPaperIds(Collections.singleton(pid), true, true, false).get(pid);
            qid = StringUtils.join(qridList, ",");
        } else if (StringUtils.isNotBlank(pid)) {
            List<String> qridList = paperLoaderClient.loadQidRidAsListByPaperId(pid, true, true, false);
            qid = StringUtils.join(qridList, ",");
        } else if (StringUtils.isNotBlank(wcoid)) {
            XxWorkbookContent content = xxWorkbookContentLoaderClient.getRemoteReference().loadXxWorkbookContents((Collections.singleton(wcoid))).get(wcoid);
            if (Objects.nonNull(content) && CollectionUtils.isNotEmpty(content.loadQuestionIds())) {
                List<String> qridList = questionLoaderClient.buildQidRidByQuestionId(content.loadQuestionIds(), true, true, false);
                qid = StringUtils.join(qridList, ",");
            }
        }

        try {
            model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            model.addAttribute("qids", StringUtils.split(qid, ","));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "studentv3/exam/viewpaper";
    }

    /**
     * 一个对外的预览，有ip白名单和访问次数计数
     *
     * @param model   model
     * @param request request
     * @return string
     */
    @RequestMapping(value = "preview.vpage", method = RequestMethod.GET)
    public String preview(Model model, HttpServletRequest request) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 一个特别的接口放在这里，预计用不了多久就会消失了。标注个时间，掐指算算 2015-11-27 xuesong.zhang
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=22743199
     */
    @RequestMapping(value = "question/byid.vpage")
    @ResponseBody
    public MapMessage questionById(HttpServletRequest request) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 带有试卷的完整信息预览，不过需要登录相应学科的老师账号
     */
    @RequestMapping(value = "quickview.vpage", method = RequestMethod.GET)
    public String quickview(Model model, HttpServletRequest request) {
        return "redirect:/teacher/index.vpage";
    }
//
//    /**
//     * =============================== 以下业务给人教测试用，不上线 ===============================
//     */
//    @Inject private CommonConfigServiceClient commonConfigServiceClient;
//    private static final List<Integer> percentList = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
//
//    /**
//     * 上传入口
//     * file_info:{"size":"文件大小","text":"阅读文本","back_voice_coefficient","back_voice_mode"}
//     */
//    @RequestMapping(value = "score.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage voiceScore() {
//        MapMessage mapMessage = new MapMessage();
//
//        String fileInfo = getRequestString(REQ_FILE_INFO);
//        Map<String, Object> map = JsonUtils.fromJson(fileInfo);
//        if (MapUtils.isEmpty(map)) {
//            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            mapMessage.add(RES_MESSAGE, RES_RESULT_DATA_ERROR_MSG);
//            return mapMessage;
//        }
//
//        Map<String, Object> resultMap = new HashMap<>();
//        // 控制一下速度
//        Long userId = -1L;
//        try {
//
//            String ua = getRequest().getHeader("User-Agent");
//            String ip = getWebRequestContext().getRealRemoteAddress();
//            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
//            MultipartFile file = multipartRequest.getFile(REQ_FILE);
//            if (file != null) {
//                resultMap = atomicLockManager.wrapAtomic(this)
//                        .keys(userId)
//                        .proxy()
//                        .doUploadResult(userId, file, map, ua, ip);
//            }
//        } catch (DuplicatedOperationException ex) {
//            logger.warn("Upload voice writing (DUPLICATED OPERATION): (userId={})", userId, ex.getMessage());
//            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            mapMessage.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
//            return mapMessage;
//        } catch (RuntimeException ex) {
//            logger.warn("Upload voice failed writing: (userId={})", userId, ex.getMessage());
//            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            mapMessage.add(RES_MESSAGE, ex.getMessage());
//            return mapMessage;
//        } catch (Exception ex) {
//            logger.warn("Upload voice failed writing: (userId={})", userId, ex.getMessage());
//            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            mapMessage.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
//            return mapMessage;
//        }
//
//        if (MapUtils.isEmpty(resultMap)) {
//            mapMessage.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            mapMessage.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
//            return mapMessage;
//        }
//
//        mapMessage.add(RES_VOICE_SCORE_INFO, resultMap);
//        mapMessage.add(RES_RESULT, RES_RESULT_SUCCESS);
//        return mapMessage;
//    }
//
//    /**
//     * 云知声保存文件
//     *
//     * @param userId      用户id
//     * @param file        文件
//     * @param fileInfoMap 文件信息map
//     * @return map
//     */
//    public Map<String, Object> doUploadResult(Long userId, MultipartFile file, Map<String, Object> fileInfoMap, String ua, String ip) {
//        if (file.getSize() != Long.valueOf((Integer) fileInfoMap.get("size"))) {
//            return Collections.emptyMap();
//        }
//        Map<String, Object> resultMap = new HashMap<>();
//        String text = SafeConverter.toString(fileInfoMap.get("text"), "");
//        String coefficient = SafeConverter.toString(fileInfoMap.get("back_voice_coefficient"), ""); // 打分系数
//        String mode = SafeConverter.toString(fileInfoMap.get("back_voice_mode"), ""); // 打分模式
//        String language = SafeConverter.toString(fileInfoMap.get("back_voice_type"), "");
//
//        // true，100%自研；
//        // true|1，10%自研，90%云知声
//        String use17VoiceScoreStr = SafeConverter.toString(
//                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "USE_RJ_17VOICE_SCORE")
//                , ""
//        );
//
//        if (StringUtils.containsIgnoreCase(text, "#JSGF") || StringUtils.equalsIgnoreCase(language, Subject.CHINESE.name())) {
//            // JSGF打分一律用云知声，这个事儿可以问章鱼或者俊杰
//            getVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language);
//        } else {
//            String[] use17VoiceScore = StringUtils.split(use17VoiceScoreStr, "|");
//            if (use17VoiceScore.length > 0) {
//                boolean use17 = SafeConverter.toBoolean(use17VoiceScore[0], false);
//                if (use17 && use17VoiceScore.length == 2) {
//                    int percent = SafeConverter.toInt(use17VoiceScore[1], 10);
//                    // 随机0-9
//                    int temp = RandomUtils.nextInt(0, 9);
//                    List<Integer> tempList = percentList.subList(0, percent);
//                    if (tempList.contains(temp)) {
//                        get17ZYVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language);
//                    } else {
//                        getVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language);
//                    }
//                } else if (use17 && use17VoiceScore.length == 1) {
//                    get17ZYVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language);
//                } else {
//                    getVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language);
//                }
//            } else {
//                getVoiceScoreJson(file, text, coefficient, mode, userId, resultMap, ua, ip, language);
//            }
//        }
//        return resultMap;
//    }
//
//    /**
//     * 自己研发的语音打分
//     */
//    public void get17ZYVoiceScoreJson(MultipartFile multipartFile, String text, String coefficient, String mode, Long userId, Map<String, Object> resultMap, String ua, String ip, String language) {
//        Instant start = Instant.now();
//        try {
//            internalGet17ZYVoiceScoreJson(multipartFile, text, coefficient, mode, userId, resultMap, ua, ip, language);
//        } finally {
//            Instant stop = Instant.now();
//            long duration = stop.toEpochMilli() - start.toEpochMilli();
//            PublishMonitorGenericInvocationEvent.publish("RJYZYInvocation", stop.getEpochSecond(), duration);
//            if (duration >= 3000) {
//                PublishMonitorGenericCountEvent.publish("RJYZYTimeout", stop.getEpochSecond(), 1);
//            }
//        }
//    }
//
//    public void internalGet17ZYVoiceScoreJson(MultipartFile multipartFile, String text, String coefficient, String mode, Long userId, Map<String, Object> resultMap, String ua, String ip, String language) {
//        String uuid = UUID.randomUUID().toString();
//        Map<String, String> headers = new HashMap<>();
//        headers.put("appkey", "RJVoice");
//        headers.put("session-id", uuid);
//        headers.put("device-id", userId.toString());
//
//        try {
//            @Cleanup InputStream in = multipartFile.getInputStream();
//            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//            builder.addTextBody("mode", StringUtils.upperCase(mode));
//            builder.addTextBody("text", text);
//            builder.addTextBody("codec", "opus");
//            builder.addBinaryBody("voice", in, ContentType.DEFAULT_BINARY, RandomStringUtils.randomAlphanumeric(8));
//            // connect_timeout = 5000, socket_timeout = 10000
//            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
//                    .post("http://vox.17zuoye.com/compute")
//                    .headers(headers)
//                    .entity(builder.build())
//                    .execute();
//
//            // 获取返回数据
//            String scoreJson = response.getResponseString();
//            Map<String, Object> scoreMap = JsonUtils.convertJsonObjectToMap(scoreJson);
//            String voiceUrl = MapUtils.isNotEmpty(scoreMap) ? SafeConverter.toString(scoreMap.get("voiceURI"), "voiceURI") : "";
//            resultMap.put("score_json", scoreJson);
//            resultMap.put("file_url", voiceUrl);
//
//            // 我是日志
//            Map<String, String> logMap = MapUtils.map(
//                    "env", RuntimeMode.getCurrentStage(),
//                    "usertoken", userId,
//                    "agent", ua,
//                    "mod1", voiceUrl,
//                    "mod2", multipartFile.getSize(),
//                    "mod3", response.getStatusCode(),
//                    "mod4", text,
//                    "mod5", ip,
//                    "mod6", "17zuoye",
//                    "mod7", language,
//                    "mod8", response.getResponseString(),
//                    "op", "UsingBackgroundScore",
//                    "time", System.currentTimeMillis()
//            );
//            LogCollector.info("backend-general", logMap);
//        } catch (Exception e) {
//            Map<String, String> logMap = MapUtils.map(
//                    "env", RuntimeMode.getCurrentStage(),
//                    "usertoken", userId,
//                    "mod6", "17zuoye",
//                    "op", "UsingBackgroundScore Exception",
//                    "mod1", e
//            );
//            logger.error("backend-general exception: " + JsonUtils.toJson(logMap));
//            LogCollector.info("backend-general", logMap);
//        }
//    }
//
//    /**
//     * 云知声打分，配置的东西暂时先扔在这
//     *
//     * @param multipartFile 文件
//     * @param text          参照文本
//     * @param userId        用户id
//     * @param resultMap     结果map
//     */
//    public void getVoiceScoreJson(MultipartFile multipartFile, String text, String coefficient, String mode, Long userId, Map<String, Object> resultMap, String ua, String ip, String language) {
//        Instant start = Instant.now();
//        try {
//            internalGetVoiceScoreJson(multipartFile, text, coefficient, mode, userId, resultMap, ua, ip, language);
//        } finally {
//            Instant stop = Instant.now();
//            long duration = stop.toEpochMilli() - start.toEpochMilli();
//            PublishMonitorGenericInvocationEvent.publish("RJYZSInvocation", stop.getEpochSecond(), duration);
//            if (duration >= 3000) {
//                PublishMonitorGenericCountEvent.publish("RJYZSTimeout", stop.getEpochSecond(), 1);
//            }
//        }
//    }
//
//    public void internalGetVoiceScoreJson(MultipartFile multipartFile, String text, String coefficient, String mode, Long userId, Map<String, Object> resultMap, String ua, String ip, String language) {
//        String uuid = UUID.randomUUID().toString();
//        if (StringUtils.isBlank(coefficient)) {
//            coefficient = "1.6";
//        }
//
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
//            @Cleanup InputStream in = multipartFile.getInputStream();
//            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//            builder.addTextBody("mode", StringUtils.upperCase(mode));
//            builder.addTextBody("text", text);
//            builder.addBinaryBody("voice", in, ContentType.DEFAULT_BINARY, RandomStringUtils.randomAlphanumeric(8));
//            // connect_timeout = 5000, socket_timeout = 10000
//            AlpsHttpResponse response;
//            if (StringUtils.equalsIgnoreCase(language, Subject.CHINESE.name())) {
//                headers.put("X-EngineType", "oral.zh_CH");
//                // 中文打分
//                response = HttpRequestExecutor.defaultInstance()
//                        .post("http://cn-edu.hivoice.cn/eval/opus")
//                        .headers(headers)
//                        .entity(builder.build())
//                        .execute();
//            } else {
//                // 英文打分
//                response = HttpRequestExecutor.defaultInstance()
//                        .post("http://edu.hivoice.cn:8085/eval/opus")
//                        .headers(headers)
//                        .entity(builder.build())
//                        .execute();
//            }
//
//            // 获取返回数据
//            String sessionId = response.getFirstHeader("Session-Id").getValue();
//            String[] session = StringUtils.split(sessionId, ":");
//            String voiceUrl = "http://edu.hivoice.cn/WebAudio-1.0-SNAPSHOT/audio/play/" + session[2] + "/" + session[1] + "/" + session[0];
//
//            resultMap.put("score_json", response.getResponseString());
//            resultMap.put("file_url", voiceUrl);
//            LogCollector.info("backend-general", MapUtils.map(
//                    "env", RuntimeMode.getCurrentStage(),
//                    "usertoken", userId,
//                    "agent", ua,
//                    "mod1", voiceUrl,
//                    "mod2", multipartFile.getSize(),
//                    "mod3", response.getStatusCode(),
//                    "mod4", text,
//                    "mod5", ip,
//                    "mod6", "hivoice",
//                    "mod7", language,
//                    "mod8", response.getResponseString(),
//                    "op", "UsingBackgroundScore",
//                    "time", System.currentTimeMillis()
//            ));
//        } catch (Exception e) {
//            LogCollector.info("backend-general", MapUtils.map(
//                    "env", RuntimeMode.getCurrentStage(),
//                    "usertoken", userId,
//                    "mod6", "hivoice",
//                    "op", "UsingBackgroundScore Exception",
//                    "mod1", e
//            ));
//        }
//    }
}
