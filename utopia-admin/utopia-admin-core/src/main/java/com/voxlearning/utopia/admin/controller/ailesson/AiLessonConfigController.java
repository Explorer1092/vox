package com.voxlearning.utopia.admin.controller.ailesson;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.core.util.ZipUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.AIMediaHelper;
import com.voxlearning.utopia.service.ai.api.ChipsActivityLoader;
import com.voxlearning.utopia.service.ai.api.ChipsActivityService;
import com.voxlearning.utopia.service.ai.client.AiLessonConfigServiceClient;
import com.voxlearning.utopia.service.ai.data.ChipsMiniProgramQRBO;
import com.voxlearning.utopia.service.ai.entity.*;
import lombok.Cleanup;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

/**
 * @author songtao
 * @since 2018/4/10
 */
@Controller
@RequestMapping("/chips/ailesson")
public class AiLessonConfigController extends AbstractAdminSystemController {

    @Inject
    private AiLessonConfigServiceClient aiLessonConfigServiceClient;

    @ImportService(interfaceClass = ChipsActivityLoader.class)
    private ChipsActivityLoader chipsActivityLoader;

    @ImportService(interfaceClass = ChipsActivityService.class)
    private ChipsActivityService chipsActivityService;

    @RequestMapping(value = "/dialogue/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveDialogue() {

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String dataJson = getRequestString("data");

        AIDialogueLessonConfig config = JSONObject.parseObject(dataJson, AIDialogueLessonConfig.class);
        if (config == null || StringUtils.isBlank(config.getId())) {
            return MapMessage.errorMessage("参数异常");
        }
        converToUrl(config);
        converPictures(config.getBegin());
        if (CollectionUtils.isNotEmpty(config.getTopic())) {
            for (AIDialogueLessonConfig.Topic topic : config.getTopic()) {
                converPictures(topic.getBegin());
                if (CollectionUtils.isEmpty(topic.getContents())) {
                    continue;
                }
                for (AIDialogueLessonConfig.Feedback feedback : topic.getContents()) {
                    if (CollectionUtils.isEmpty(feedback.getFeedback())) {
                        continue;
                    }
                    feedback.getFeedback().forEach(e -> {
                        converPictures(e);
                    });
                }
            }
        }
        converPictures(config.getEnd());

        addAdminLog("情景对话编辑", config.getId(), "情景对话编辑操作", config);
        return aiLessonConfigServiceClient.getRemoteReference().saveOrUpdateAIDialogueLessonConfigData(config);
    }

    @RequestMapping(value = "/dialogue/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteDialogue() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        AIDialogueLessonConfig aiDialogueLessonConfig = aiLessonConfigServiceClient.getRemoteReference().loadAIDialogueLessonConfigById(id);
        if (aiDialogueLessonConfig == null || Boolean.TRUE.equals(aiDialogueLessonConfig.getDisabled())) {
            return MapMessage.errorMessage("要删除的数据不存在");
        }
        return aiLessonConfigServiceClient.getRemoteReference().deleteAIDialogueLessonConfig(id);
    }

    @RequestMapping(value = "/dialogue/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadDialogueDetail() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        AIDialogueLessonConfig aiDialogueLessonConfig = aiLessonConfigServiceClient.getRemoteReference().loadAIDialogueLessonConfigById(id);
        if (aiDialogueLessonConfig != null && CollectionUtils.isNotEmpty(aiDialogueLessonConfig.getTopic())) {
            aiDialogueLessonConfig.getTopic().forEach(e -> {
                if (e.getKnowledge() == null) {
                    AIDialogueLessonConfig.Knowledge knowledge = new AIDialogueLessonConfig.Knowledge();
                    AIDialogueLessonConfig.KnowledgeSentence sentence = new AIDialogueLessonConfig.KnowledgeSentence();
                    knowledge.setSentences(Collections.singletonList(sentence));
                    e.setKnowledge(knowledge);
                }
            });
        }
        return MapMessage.successMessage().add("data", aiDialogueLessonConfig);
    }

    @RequestMapping(value = "/dialogue/index.vpage", method = RequestMethod.GET)
    public String loadDialogueListIndex(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        List<Map<String, Object>> result = new ArrayList<>();
        aiLessonConfigServiceClient.getRemoteReference().loadAllAIDialogueLessonConfigs().forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("title", e.getTitle());
            map.put("updateTime", DateUtils.dateToString(e.getUpdateDate()));
            result.add(map);
        });
        model.addAttribute("result", result);
        return "ailesson/index";
    }

    @RequestMapping(value = "/dialogue/addform.vpage", method = RequestMethod.GET)
    public String loadDialogueAddForm(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
        return "ailesson/addform";
    }

    /**
     * 　* @Description:
     * 　* @author zhiqi.yao
     * 　* @date 2018/4/13 14:28
     */
    @RequestMapping(value = "/task/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTaskDialogue() {

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String content = getRequestString("content");
        String pagePath = getRequestString("path");
        if (StringUtils.isAnyBlank(content, pagePath)) {
            return MapMessage.errorMessage("参数为空");
        }

        return chipsActivityService.processAddMiniProgramQR(content, pagePath);
    }

    @RequestMapping(value = "/task/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTaskDialogue() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        return chipsActivityService.processDeleteMiniProgramQR(id);
    }

    //jsgf格式的字符串检查
    @RequestMapping(value = "/jsgf/check.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkJsgf() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String data = getRequestString("data");
        if (StringUtils.isBlank(data)) {
            return MapMessage.errorMessage("参数错误");
        }
        try (InputStream in = AiLessonConfigController.class.getClassLoader().getResourceAsStream("test/dinner.opus")) {
            Map<String, String> headers = new HashMap<>();
            headers.put("appkey", "test");
            headers.put("score-coefficient", "");
            headers.put("device-id", "test");
            headers.put("session-id", "axbyxqrt38");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("mode", "general");
            builder.addTextBody("text", data);
            builder.addTextBody("codec", "opus");
            builder.addBinaryBody("voice", in, ContentType.DEFAULT_BINARY, "dinner");
            String resString = HttpRequestExecutor.defaultInstance()
                    .post("http://10.7.13.71:8087/compute")
                    .headers(headers)
                    .entity(builder.build())
                    .execute().getResponseString();

            if (StringUtils.isBlank(resString)) {
                return MapMessage.errorMessage("语音引擎返回参数为空");
            }
            Map<String, Object> resmap = JsonUtils.fromJson(resString);
            if (MapUtils.isEmpty(resmap) || SafeConverter.toInt(resmap.get("code"), -1) < 0 || SafeConverter.toInt(resmap.get("Error_Code"), -1) < 0) {
                return MapMessage.errorMessage("语音引擎返回参数为空");
            }
            int code = SafeConverter.toInt(resmap.get("code"), -1);
            int errorCode = SafeConverter.toInt(resmap.get("Error_Code"), -1);
            if (code != 0) {
                return MapMessage.errorMessage("语音引擎调用失败");
            }
            switch (errorCode) {
                case 8:
                    return MapMessage.errorMessage("输入的单词或句子不在语音的词表里，请找语音引擎的同事添加");
                case 20:
                    return MapMessage.errorMessage("jsgf格式不正确");
                case 0:
                    return MapMessage.successMessage();
                default:
                    return MapMessage.errorMessage("语音引擎其他错误");
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("语音引擎调用失败");
        }
    }

    // 获取JSGF内容
    @RequestMapping(value = "/jsgf/create.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createJsgf() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String data = getRequestString("data");
        if (StringUtils.isBlank(data)) {
            return MapMessage.errorMessage("参数错误");
        }
        Map<String, Object> dataMap = JsonUtils.fromJson(data);
        if (dataMap.get("styleText") == null || dataMap.get("keywordText") == null || dataMap.get("varText") == null) {
            return MapMessage.errorMessage("参数错误");
        }
        List<String> styleText = (List) dataMap.get("styleText");
        List<String> keywordText = (List) dataMap.get("keywordText");
        List<String> varText = (List) dataMap.get("varText");

        try {
            POST post = HttpRequestExecutor.defaultInstance()
                    .post("http://101.231.106.182:5000/process.php")
                    .headers(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
            for (String s : styleText) {
                post = post.addParameter("styleText[]", s);
            }
            for (String s : keywordText) {
                post = post.addParameter("keywordText[]", s);
            }
            for (String s : varText) {
                post = post.addParameter("varText[]", s);
            }
            return MapMessage.successMessage().add("result", post.execute().getResponseString());
        } catch (Exception e) {
            logger.error("create jsgf error", e);
            return MapMessage.errorMessage("对话语料展开失败, 调用对话系统接口失败");
        }
    }

    //jsgf格式的字符串展开
    @RequestMapping(value = "/jsgf/expand.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage extendJsgf() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String data = getRequestString("data");
        if (StringUtils.isBlank(data)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            String response = HttpRequestExecutor.defaultInstance()
                    .post("http://10.7.13.75:9090/jsgf/expand")
                    .headers(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"))
                    .addParameter("jsgf", data)
                    .execute().getResponseString();
            Map<String, Object> map = JsonUtils.fromJson(response);
            if (MapUtils.isEmpty(map) || !"success".equals(map.get("result")) || map.get("data") == null) {
                return MapMessage.errorMessage("对话语料展开失败，调用对话展开返回异常");
            }
            MapMessage res = MapMessage.successMessage();
            res.putAll(map);
            return res;
        } catch (Exception e) {
            logger.error("call error. jsgf:{}", data, e);
            return MapMessage.errorMessage("对话语料展开失败, 调用对话系统接口失败");
        }
    }

    /**
     * 同步对话系统数据
     *
     * @return
     */
    @RequestMapping(value = "/talk/sync.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage syncTalk() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        String type = getRequestString("type");
        if (StringUtils.isAnyBlank(id, type)) {
            return MapMessage.errorMessage("参数为空");
        }
        String url = null;
        switch (type) {
            case "task":
                if (RuntimeMode.current().le(Mode.TEST)) {
                    url = "http://10.7.13.75:31001/aiteacher/admin/task";
                } else {
                    url = "http://dialogue.17zuoye.com/aiteacher/admin/task";
                }
                break;
            case "dialogue":
                if (RuntimeMode.current().le(Mode.TEST)) {
                    url = "http://10.7.13.75:31001/aiteacher/admin/scene";
                } else {
                    url = "http://dialogue.17zuoye.com/aiteacher/admin/scene";
                }
                break;
            default:
                break;
        }
        if (StringUtils.isNotBlank(url)) {
            try {
                String response = HttpRequestExecutor.defaultInstance()
                        .post(url)
                        .headers(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"))
                        .addParameter("lesson_id", id)
                        .execute().getResponseString();
                Map<String, Object> resmap = JsonUtils.fromJson(response);
                if (MapUtils.isEmpty(resmap)) {
                    return MapMessage.errorMessage("调用对话系统返回为空");
                }
                if (StringUtils.compare(SafeConverter.toString(resmap.get("result")), "success") != 0) {
                    return MapMessage.errorMessage(SafeConverter.toString(resmap.get("message")));
                }
                return MapMessage.successMessage();
            } catch (Exception e) {
                logger.error("talk sync error. url:{}, type:{}, id:{}", url, type, id, e);
                return MapMessage.errorMessage("调用对话系统异常");
            }
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/task/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadTaskDialogueDetail() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        AIDialogueTaskConfig aiDialogueTaskConfig = aiLessonConfigServiceClient.getRemoteReference().loadAIDialogueTaskConfigById(id);
        if (aiDialogueTaskConfig != null && CollectionUtils.isNotEmpty(aiDialogueTaskConfig.getNpcs())) {
            for (AIDialogueTaskConfig.Npc npc : aiDialogueTaskConfig.getNpcs()) {
                if (CollectionUtils.isEmpty(npc.getTopic())) {
                    continue;
                }
                npc.getTopic().forEach(e -> {
                    if (e.getKnowledge() == null) {
                        AIDialogueTaskConfig.Knowledge knowledge = new AIDialogueTaskConfig.Knowledge();
                        AIDialogueTaskConfig.KnowledgeSentence sentence = new AIDialogueTaskConfig.KnowledgeSentence();
                        knowledge.setSentences(Collections.singletonList(sentence));
                        e.setKnowledge(knowledge);
                    }
                });
            }
        }
        return MapMessage.successMessage().add("data", aiDialogueTaskConfig);
    }


    @RequestMapping(value = "/task/index.vpage", method = RequestMethod.GET)
    public String loadTaskDialogueListIndex(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        int page = getRequestInt("page", 1);
        if (page <= 0) {
            page = 1;
        }
        MapMessage result = chipsActivityLoader.loadChipsMiniProgramPageable(page);
        model.addAllAttributes(result);
        return "chips/miniprogram/index";
    }

    @RequestMapping(value = "/task/addform.vpage", method = RequestMethod.GET)
    public String loadTaskDialogueAddForm(Model model) {
        return "chips/miniprogram/add";
    }

    @RequestMapping(value = "/data/import.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importData(@RequestParam String type) {
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            MultipartFile file = multipartRequest.getFile("file");
            String content = new String(file.getBytes(), "utf-8");
            if (StringUtils.isAnyBlank(type, content)) {
                return MapMessage.errorMessage("参数为空");
            }
            switch (type) {
                case "dialogue":
                    List<AIDialogueLessonConfig> configList = JSONArray.parseArray(content, AIDialogueLessonConfig.class);
                    for (AIDialogueLessonConfig config : configList) {
                        try {
                            aiLessonConfigServiceClient.getRemoteReference().saveOrUpdateAIDialogueLessonConfigData(config);
                        } catch (Exception e) {
                            logger.error("importData persistence error. config:{}", config, e);
                        }
                    }
                    break;
                case "task":
                    List<AIDialogueTaskConfig> configs = JSONArray.parseArray(content, AIDialogueTaskConfig.class);
                    for (AIDialogueTaskConfig config : configs) {
                        try {
                            aiLessonConfigServiceClient.getRemoteReference().saveOrUpdateAIDialogueTaskConfigData(config);
                        } catch (Exception e) {
                            logger.error("importData persistence error. config:{}", config, e);
                        }
                    }
                    break;
                default:
                    break;
            }
            return MapMessage.successMessage("导入成功");
        } catch (Exception e) {
            logger.error("importData error. type:{}", type, e);
            return MapMessage.errorMessage("服务器异常");
        }
    }

    @RequestMapping(value = "/data/export.vpage", method = RequestMethod.GET)
    public void exportData(HttpServletResponse response) {
        String type = getRequestParameter("type", "dialogue");
        String json = getRequestParameter("json", "");
        int page = getRequestInt("page", 1);
        String data = null;
        switch (type) {
            case "dialogue":
                List<AIDialogueLessonConfig> res = aiLessonConfigServiceClient.getRemoteReference().loadAllAIDialogueLessonConfigs();
                if (StringUtils.isNotBlank(json)) {
                    data = JsonUtils.toJson(parseToStoneData(res.stream().filter(e -> e.getId().startsWith("SD_")).collect(Collectors.toList())));
                } else {
                    data = JsonUtils.toJson(res);
                }
                break;
            case "task":
                List<AIDialogueTaskConfig> taskres = aiLessonConfigServiceClient.getRemoteReference().loadAllAIDialogueTaskConfigs();
                if (StringUtils.isNotBlank(json)) {
                    data = JsonUtils.toJson(parseTaskToStoneData(taskres.stream().filter(e -> e.getId().startsWith("SD_")).collect(Collectors.toList())));
                } else {
                    data = JsonUtils.toJson(taskres);
                }
                break;
            case "miniprogram":
                List<ChipsMiniProgramQRBO> result = (List<ChipsMiniProgramQRBO>) chipsActivityLoader.loadChipsMiniProgramPageable(page).get("result");
                if (CollectionUtils.isEmpty(result)) {
                    return;
                }
                try (ZipArchiveOutputStream out = getZipOutputStreamForDownloading("小程序码第" + page + "页.zip")) {
                    for (ChipsMiniProgramQRBO bo : result) {
                        try (ByteArrayOutputStream imageOut = new ByteArrayOutputStream(); InputStream imageInput = getRemoteStream(bo.getImage())) {
                            byte[] buffer = new byte[1024 * 4];
                            int n = 0;
                            while ((n = imageInput.read(buffer)) != -1) {
                                imageOut.write(buffer, 0, n);
                            }
                            imageOut.flush();
                            ZipUtils.addZipEntry(out, bo.getContent() + ".jpg", imageOut.toByteArray());
                        } catch (Exception e1) {
                            logger.error("miniprogram qrcode to zip.", e1);
                        }
                    }
                } catch (Exception e) {
                    logger.error("miniprogram qrcode to zip. page:{}", page, e);
                }
                return;
            default:
                break;
        }

        if (StringUtils.isBlank(data)) {
            return;
        }
        data = formatJson(data);
        String fileName = type + json + "-data.txt";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            outStream.write(data.getBytes("utf-8"));
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "text/plain",
                    outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

    private InputStream getRemoteStream(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(300 * 1000);
        return conn.getInputStream();
    }

    private List<Map<String, Object>> parseToStoneData(List<AIDialogueLessonConfig> res) {
        List<Map<String, Object>> jsonList = new ArrayList<>();
        res.forEach(e -> {
            if (e.getBegin() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("custom_name", e.getTitle() + "-开场");
                map.put("schema_name", "scene_interlude");
                map.put("created_at", e.getCreateDate());
                map.put("updated_at", e.getUpdateDate());
                Map<String, String> jsonDataMap = new HashMap<>();
                jsonDataMap.put("cn_translation", e.getBegin().getCnTranslation());
                jsonDataMap.put("translation", e.getBegin().getTranslation());
                jsonDataMap.put("video", e.getBegin().getVideo());
                jsonDataMap.put("role_image", e.getBegin().getRoleImage());
                jsonDataMap.put("feedback", e.getBegin().getFeedback());
                jsonDataMap.put("cover_pic", e.getBegin().getFirstFrame());
                map.put("json_data", JsonUtils.toJson(jsonDataMap));
                jsonList.add(map);
            }

            if (CollectionUtils.isNotEmpty(e.getTopic())) {
                for (int i = 0; i < e.getTopic().size(); i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("custom_name", e.getTitle() + "-topic" + (i + 1));
                    map.put("schema_name", "video_dialogue");
                    map.put("created_at", e.getCreateDate());
                    map.put("updated_at", e.getUpdateDate());
                    AIDialogueLessonConfig.Topic topic = e.getTopic().get(i);
                    if (topic == null) {
                        continue;
                    }
                    Map<String, Object> jsonDataMap = new HashMap<>();

                    jsonDataMap.put("cn_translation", Optional.ofNullable(topic.getBegin()).map(AIDialogueLessonTopicBegin::getCnTranslation).orElse(""));
                    jsonDataMap.put("translation", Optional.ofNullable(topic.getBegin()).map(AIDialogueLessonTopicBegin::getTranslation).orElse(""));
                    jsonDataMap.put("video", Optional.ofNullable(topic.getBegin()).map(AIDialogueLessonTopicBegin::getVideo).orElse(""));
                    jsonDataMap.put("role_image", Optional.ofNullable(topic.getBegin()).map(AIDialogueLessonTopicBegin::getRoleImage).orElse(""));
                    jsonDataMap.put("cover_pic", Optional.ofNullable(topic.getBegin()).map(AIDialogueLessonTopicBegin::getFirstFrame).orElse(""));
                    jsonDataMap.put("tip", Optional.ofNullable(topic.getBegin()).map(AIDialogueLessonTopicBegin::getTip).orElse(""));
                    jsonDataMap.put("pop_tip", Optional.ofNullable(topic.getBegin()).map(AIDialogueLessonTopicBegin::getPopTip).orElse(""));
                    jsonDataMap.put("pop_tip_audio", Optional.ofNullable(topic.getBegin()).map(AIDialogueLessonTopicBegin::getPopTipAudio).orElse(""));
                    jsonDataMap.put("help_cn", Optional.ofNullable(topic.getHelp()).map(AIDialogueLessonConfig.Help::getHelpCn).orElse(""));
                    jsonDataMap.put("help_en", Optional.ofNullable(topic.getHelp()).map(AIDialogueLessonConfig.Help::getHelpEn).orElse(""));
                    jsonDataMap.put("help_title", Optional.ofNullable(topic.getHelp()).map(AIDialogueLessonConfig.Help::getHelpTitle).orElse(""));
                    jsonDataMap.put("help_audio", Optional.ofNullable(topic.getHelp()).map(AIDialogueLessonConfig.Help::getHelpAudio).orElse(""));
                    jsonDataMap.put("explain", Optional.ofNullable(topic.getKnowledge()).map(AIDialogueLessonConfig.Knowledge::getExplain).orElse(""));
                    jsonDataMap.put("explain_audio", Optional.ofNullable(topic.getKnowledge()).map(AIDialogueLessonConfig.Knowledge::getExplainAudio).orElse(""));
                    jsonDataMap.put("sentences", Optional.ofNullable(topic.getKnowledge())
                            .map(AIDialogueLessonConfig.Knowledge::getSentences)
                            .map(e1 -> e1.stream().map(e2 -> toUnderlineJsonString(e2)).collect(Collectors.toList()))
                            .orElse(Collections.emptyList()));

                    List<Map<String, String>> jsgfContent = new ArrayList<>();
                    topic.getContents().forEach(e1 -> {
                        Map<String, String> map1 = new HashMap<>();
                        map1.put("jsgf", e1.getPattern());
                        if (CollectionUtils.isNotEmpty(e1.getFeedback())) {
                            AIDialogueLessonTopicContent content = e1.getFeedback().get(0);
                            map1.put("cn_translation", Optional.ofNullable(content).map(AIDialogueLesson::getCnTranslation).orElse(""));
                            map1.put("translation", Optional.ofNullable(content).map(AIDialogueLesson::getTranslation).orElse(""));
                            map1.put("video", Optional.ofNullable(content).map(AIDialogueLesson::getVideo).orElse(""));
                            map1.put("role_image", Optional.ofNullable(content).map(AIDialogueLesson::getRoleImage).orElse(""));
                            map1.put("feedback_cover_pic", Optional.ofNullable(content).map(AIDialogueLesson::getFirstFrame).orElse(""));
                            map1.put("tip", Optional.ofNullable(content).map(AIDialogueLesson::getTip).orElse(""));
                            map1.put("level", Optional.ofNullable(content).map(AIDialogueLesson::getLevel).orElse(""));
                            map1.put("feedback", Optional.ofNullable(content).map(AIDialogueLesson::getFeedback).orElse(""));
                            jsgfContent.add(map1);
                        }

                    });
                    jsonDataMap.put("jsgf_content", jsgfContent);

                    map.put("json_data", JsonUtils.toJson(jsonDataMap));
                    jsonList.add(map);
                }
            }
            if (e.getEnd() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("custom_name", e.getTitle() + "-结束");
                map.put("schema_name", "scene_interlude");
                map.put("created_at", e.getCreateDate());
                map.put("updated_at", e.getUpdateDate());
                Map<String, String> jsonDataMap = new HashMap<>();
                jsonDataMap.put("cn_translation", e.getEnd().getCnTranslation());
                jsonDataMap.put("translation", e.getEnd().getTranslation());
                jsonDataMap.put("video", e.getEnd().getVideo());
                jsonDataMap.put("role_image", e.getEnd().getRoleImage());
                jsonDataMap.put("feedback", e.getEnd().getFeedback());
                jsonDataMap.put("cover_pic", e.getEnd().getFirstFrame());
                map.put("json_data", JsonUtils.toJson(jsonDataMap));
                jsonList.add(map);
            }
        });
        return jsonList;
    }

    private List<Map<String, Object>> parseTaskToStoneData(List<AIDialogueTaskConfig> res) {
        List<Map<String, Object>> jsonList = new ArrayList<>();
        res.forEach(n -> {
            n.getNpcs().forEach(e -> {
                String cust_pre = n.getTitle() + "-" + e.getNpcName();
//                Map<String, Object> roleMap = new HashMap<>();
//                roleMap.put("custom_name",  cust_pre);
//                roleMap.put("schema_name", "task_npc");
//                roleMap.put("created_at", n.getCreateDate());
//                roleMap.put("updated_at", n.getUpdateDate());
//
//                Map<String, Object> roleDataMap = new HashMap<>();
//                roleDataMap.put("npc_name",  e.getNpcName());
//                roleDataMap.put("status", "success".equalsIgnoreCase(e.getStatus()));
//                roleDataMap.put("right_tip", e.getRightTip());
//                roleDataMap.put("background_image", e.getBackgroundImage());
//                roleDataMap.put("role_image", e.getRoleImage());
//                roleDataMap.put("content_ids", Collections.emptyList());
//                roleMap.put("json_data", JsonUtils.toJson(roleDataMap));
//                jsonList.add(roleMap);

                if (e.getBegin() != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("custom_name", cust_pre + "-开场");
                    map.put("schema_name", "task_interlude");
                    map.put("created_at", n.getCreateDate());
                    map.put("updated_at", n.getUpdateDate());
                    Map<String, String> jsonDataMap = new HashMap<>();
                    jsonDataMap.put("cn_translation", e.getBegin().getCnTranslation());
                    jsonDataMap.put("translation", e.getBegin().getTranslation());
                    jsonDataMap.put("audio", e.getBegin().getAudio());
                    jsonDataMap.put("role_image", e.getRoleImage());
                    jsonDataMap.put("feedback", e.getBegin().getFeedback());
                    jsonDataMap.put("background_image", e.getBackgroundImage());
                    map.put("json_data", JsonUtils.toJson(jsonDataMap));
                    jsonList.add(map);
                }

                if (CollectionUtils.isNotEmpty(e.getTopic())) {
                    for (int i = 0; i < e.getTopic().size(); i++) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("custom_name", cust_pre + "-topic" + (i + 1));
                        map.put("schema_name", "task_topic");
                        map.put("created_at", n.getCreateDate());
                        map.put("updated_at", n.getUpdateDate());
                        AIDialogueTaskConfig.Topic topic = e.getTopic().get(i);
                        if (topic == null) {
                            continue;
                        }
                        Map<String, Object> jsonDataMap = new HashMap<>();

                        jsonDataMap.put("cn_translation", Optional.ofNullable(topic.getBegin()).map(AITaskLessonTopicBegin::getCnTranslation).orElse(""));
                        jsonDataMap.put("translation", Optional.ofNullable(topic.getBegin()).map(AITaskLessonTopicBegin::getTranslation).orElse(""));
                        jsonDataMap.put("audio", Optional.ofNullable(topic.getBegin()).map(AITaskLessonTopicBegin::getAudio).orElse(""));
                        jsonDataMap.put("role_image", e.getRoleImage());
                        jsonDataMap.put("background_image", e.getBackgroundImage());
                        jsonDataMap.put("tip", Optional.ofNullable(topic.getBegin()).map(AITaskLessonTopicBegin::getTip).orElse(""));
                        jsonDataMap.put("pop_tip", Optional.ofNullable(topic.getBegin()).map(AITaskLessonTopicBegin::getPopTip).orElse(""));
                        jsonDataMap.put("pop_tip_audio", Optional.ofNullable(topic.getBegin()).map(AITaskLessonTopicBegin::getPopTipAudio).orElse(""));
                        jsonDataMap.put("help_cn", Optional.ofNullable(topic.getHelp()).map(AIDialogueTaskConfig.Help::getHelpCn).orElse(""));
                        jsonDataMap.put("help_en", Optional.ofNullable(topic.getHelp()).map(AIDialogueTaskConfig.Help::getHelpEn).orElse(""));
                        jsonDataMap.put("help_title", Optional.ofNullable(topic.getHelp()).map(AIDialogueTaskConfig.Help::getHelpTitle).orElse(""));
                        jsonDataMap.put("help_audio", Optional.ofNullable(topic.getHelp()).map(AIDialogueTaskConfig.Help::getHelpAudio).orElse(""));
                        jsonDataMap.put("explain", Optional.ofNullable(topic.getKnowledge()).map(AIDialogueTaskConfig.Knowledge::getExplain).orElse(""));
                        jsonDataMap.put("explain_audio", Optional.ofNullable(topic.getKnowledge()).map(AIDialogueTaskConfig.Knowledge::getExplainAudio).orElse(""));
                        jsonDataMap.put("sentences", Optional.ofNullable(topic.getKnowledge())
                                .map(AIDialogueTaskConfig.Knowledge::getSentences)
                                .map(e1 -> e1.stream().map(e2 -> toUnderlineJsonString(e2)).collect(Collectors.toList()))
                                .orElse(Collections.emptyList()));

                        List<Map<String, String>> jsgfContent = new ArrayList<>();
                        topic.getContents().forEach(e1 -> {
                            Map<String, String> map1 = new HashMap<>();
                            map1.put("jsgf", e1.getPattern());
                            if (CollectionUtils.isNotEmpty(e1.getFeedback())) {
                                AITaskLessonTopicContent content = e1.getFeedback().get(0);
                                map1.put("level", Optional.ofNullable(content).map(AITaskLessonTopicContent::getLevel).orElse(""));
                                map1.put("cn_translation", Optional.ofNullable(content).map(AITaskLessonTopicContent::getCnTranslation).orElse(""));
                                map1.put("translation", Optional.ofNullable(content).map(AITaskLessonTopicContent::getTranslation).orElse(""));
                                map1.put("audio", Optional.ofNullable(content).map(AITaskLessonTopicContent::getAudio).orElse(""));
                                map1.put("tip", Optional.ofNullable(content).map(AITaskLessonTopicContent::getTip).orElse(""));
                                map1.put("role_image", e.getRoleImage());
                                map1.put("feedback", Optional.ofNullable(content).map(AITaskLessonTopicContent::getFeedback).orElse(""));
                                jsgfContent.add(map1);
                            }

                        });
                        jsonDataMap.put("jsgf_content", jsgfContent);
                        map.put("json_data", JsonUtils.toJson(jsonDataMap));
                        jsonList.add(map);
                    }
                }
                if (e.getEnd() != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("custom_name", cust_pre + "-结束");
                    map.put("schema_name", "task_interlude");
                    map.put("created_at", n.getCreateDate());
                    map.put("updated_at", n.getUpdateDate());
                    Map<String, String> jsonDataMap = new HashMap<>();
                    jsonDataMap.put("cn_translation", e.getEnd().getCnTranslation());
                    jsonDataMap.put("background_image", e.getBackgroundImage());
                    jsonDataMap.put("translation", e.getEnd().getTranslation());
                    jsonDataMap.put("audio", e.getEnd().getAudio());
                    jsonDataMap.put("role_image", e.getRoleImage());
                    jsonDataMap.put("feedback", e.getEnd().getFeedback());
                    map.put("json_data", JsonUtils.toJson(jsonDataMap));
                    jsonList.add(map);
                }
            });

        });
        return jsonList;
    }

    @RequestMapping(value = "/play/list.vpage", method = RequestMethod.GET)
    public String savePlay(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        List<Map<String, Object>> result = new ArrayList<>();
        aiLessonConfigServiceClient.getRemoteReference().loadAllAILessonPlay().forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("title", e.getLessonName());
            map.put("updateTime", DateUtils.dateToString(e.getUpdateDate()));
            result.add(map);
        });
        model.addAttribute("result", result);
        return "ailesson/play_list";
    }

    @RequestMapping(value = "/play/adddrama.vpage", method = RequestMethod.GET)
    public String addDrama(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
        return "ailesson/add_drama";
    }

    @RequestMapping(value = "/play/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage savePlay() {

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String dataJson = getRequestString("data");

        AILessonPlay config = JSONObject.parseObject(dataJson, AILessonPlay.class);
        if (config == null || StringUtils.isBlank(config.getId())) {
            return MapMessage.errorMessage("参数异常");
        }

        return aiLessonConfigServiceClient.getRemoteReference().saveOrUpdateAILessonPlayData(config);
    }

    @RequestMapping(value = "/play/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deletePlay() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        AILessonPlay aiLessonPlay = aiLessonConfigServiceClient.getRemoteReference().loadAILessonPlayById(id);
        if (aiLessonPlay == null || Boolean.TRUE.equals(aiLessonPlay.getDisabled())) {
            return MapMessage.errorMessage("要删除的数据不存在");
        }
        return aiLessonConfigServiceClient.getRemoteReference().deleteAILessonPlay(id);
    }

    @RequestMapping(value = "/play/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPlayDetail() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        AILessonPlay aiLessonPlay = aiLessonConfigServiceClient.getRemoteReference().loadAILessonPlayById(id);
        return MapMessage.successMessage().add("data", aiLessonPlay);
    }


    private String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '"':
                    if (last != '\\') {
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    sb.append(current);
                    break;
                case '{':
                case '[':
                    sb.append(current);
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent++;
                        addIndentBlank(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent--;
                        addIndentBlank(sb, indent);
                    }
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }

    private void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }

    private void converToUrl(AIDialogueTaskConfig config) {
        if (config == null) {
            return;
        }
        if (CollectionUtils.isNotEmpty(config.getNpcs())) {
            for (AIDialogueTaskConfig.Npc npc : config.getNpcs()) {
                converToUrl(npc.getBegin());
                converToUrl(npc.getEnd());
                if (CollectionUtils.isEmpty(npc.getTopic())) {
                    continue;
                }
                for (AIDialogueTaskConfig.Topic topic : npc.getTopic()) {
                    converToUrl(topic.getBegin());
                    if (topic.getHelp() != null && StringUtils.isNotBlank(topic.getHelp().getHelpAudio())
                            && !topic.getHelp().getHelpAudio().contains("http:") && StringUtils.isNotBlank(AIMediaHelper.getUrlFromTag(topic.getHelp().getHelpAudio()))) {
                        topic.getHelp().setHelpAudio(AIMediaHelper.getUrlFromTag(topic.getHelp().getHelpAudio()));
                    }
                    if (CollectionUtils.isEmpty(topic.getContents())) {
                        continue;
                    }
                    for (AIDialogueTaskConfig.Feedback feedback : topic.getContents()) {
                        for (AITaskLesson lesson : feedback.getFeedback()) {
                            converToUrl(lesson);
                        }
                    }
                }
            }
        }
    }

    private void converToUrl(AIDialogueLessonConfig config) {
        if (config == null) {
            return;
        }
        converToUrl(config.getBegin());

        if (CollectionUtils.isNotEmpty(config.getTopic())) {
            for (AIDialogueLessonConfig.Topic topic : config.getTopic()) {
                converToUrl(topic.getBegin());
                if (topic.getHelp() != null && StringUtils.isNotBlank(topic.getHelp().getHelpAudio()) &&
                        !topic.getHelp().getHelpAudio().contains("http:") && StringUtils.isNotBlank(AIMediaHelper.getUrlFromTag(topic.getHelp().getHelpAudio()))) {
                    topic.getHelp().setHelpAudio(AIMediaHelper.getUrlFromTag(topic.getHelp().getHelpAudio()));
                }
                if (CollectionUtils.isEmpty(topic.getContents())) {
                    continue;
                }
                for (AIDialogueLessonConfig.Feedback feedback : topic.getContents()) {
                    for (AIDialogueLesson lesson : feedback.getFeedback()) {
                        converToUrl(lesson);
                    }
                }
            }
        }
        converToUrl(config.getEnd());
    }


    private void converToUrl(AITaskLesson lesson) {
        if (lesson != null && StringUtils.isNotBlank(lesson.getVideo())
                && !lesson.getVideo().contains("http:") && StringUtils.isNotBlank(AIMediaHelper.getUrlFromTag(lesson.getVideo()))) {
            lesson.setVideo(AIMediaHelper.getUrlFromTag(lesson.getVideo()));
        }
        if (lesson != null && StringUtils.isNotBlank(lesson.getAudio()) && !lesson.getAudio().contains("http:")
                && StringUtils.isNotBlank(AIMediaHelper.getUrlFromTag(lesson.getAudio()))) {
            lesson.setAudio(AIMediaHelper.getUrlFromTag(lesson.getAudio()));
        }
    }

    private void converToUrl(AIDialogueLesson lesson) {
        if (lesson != null && StringUtils.isNotBlank(lesson.getVideo())
                && !lesson.getVideo().contains("http:") && StringUtils.isNotBlank(AIMediaHelper.getUrlFromTag(lesson.getVideo()))) {
            lesson.setVideo(AIMediaHelper.getUrlFromTag(lesson.getVideo()));
        }
        if (lesson != null && StringUtils.isNotBlank(lesson.getAudio()) && !lesson.getAudio().contains("http:")
                && StringUtils.isNotBlank(AIMediaHelper.getUrlFromTag(lesson.getAudio()))) {
            lesson.setAudio(AIMediaHelper.getUrlFromTag(lesson.getAudio()));
        }
    }

    private void converPictures(AITaskLesson lesson) {
        if (lesson != null && CollectionUtils.isNotEmpty(lesson.getPicture()) && lesson.getPicture().size() == 1) {
            lesson.setPicture(Arrays.asList(lesson.getPicture().get(0).split(",")));
        }
    }

    private void converPictures(AIDialogueLesson lesson) {
        if (lesson != null && CollectionUtils.isNotEmpty(lesson.getPicture()) && lesson.getPicture().size() == 1) {
            lesson.setPicture(Arrays.asList(lesson.getPicture().get(0).split(",")));
        }
    }

    private JSONObject toUnderlineJsonString(Object config) {
        if (config == null) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            String reqJson = mapper.writeValueAsString(config);
            JSONObject jsonObject = JSONObject.parseObject(reqJson);
            return jsonObject;
        } catch (Exception e) {
            return null;
        }
    }
}
