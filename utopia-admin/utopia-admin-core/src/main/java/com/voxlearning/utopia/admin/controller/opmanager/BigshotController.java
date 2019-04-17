package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.persist.entity.AdminLog;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.crm.client.AdminLogServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.parent.api.CrmTelecastLoader;
import com.voxlearning.utopia.service.parent.api.CrmTelecastService;
import com.voxlearning.utopia.service.parent.api.entity.bigshot.EnterType;
import com.voxlearning.utopia.service.parent.api.entity.bigshot.Telecast;
import com.voxlearning.utopia.service.parent.api.entity.bigshot.TelecastIntro;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping(value = "/opmanager/bigshot")
public class BigshotController extends OpManagerAbstractController {

    @ImportService(interfaceClass = CrmTelecastService.class)
    private CrmTelecastService crmTelecastService;

    @ImportService(interfaceClass = CrmTelecastLoader.class)
    private CrmTelecastLoader crmTelecastLoader;

    @Inject
    private AdminLogServiceClient adminLogServiceClient;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @RequestMapping(value = "/list.vpage")
    public String list(Model model) {
        String title = requestString("title");
        String admin = requestString("admin");
        String online = requestString("onlineStatus");

        String url = "?";

        if (StringUtils.isNotBlank(title)) {
            model.addAttribute("title", title);
            url += "title=" + title + "&";
        }

        if (StringUtils.isNotBlank(admin)) {
            model.addAttribute("admin", admin);
            url += "admin=" + admin + "&";
        }

        if (StringUtils.isNotBlank(online)) {
            model.addAttribute("online", online);
            url += "online=" + online + "&";
        }

        url += "page=";

        int pageIndex = getRequestInt("pageIndex", 1);
        if (pageIndex <= 0) {
            pageIndex = 1;
        }

        int size = 20;

        PageRequest request = new PageRequest(pageIndex - 1, size);

        Page<Telecast> telecastPage = crmTelecastLoader.find(title, StringUtils.isEmpty(online) ? null : StringUtils.equals("true", online), admin, request);

        List<Map<String, Object>> maps = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(telecastPage.getContent())) {
            List<Telecast> telecasts = telecastPage.getContent();
            List<String> telecastIds = telecasts.stream().map(Telecast::getId).collect(Collectors.toList());
            Map<String, Long> enterTotal = crmTelecastLoader.loadEnterTotal(telecastIds);

            telecasts.forEach(telecast -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", telecast.getId());
                map.put("title", telecast.getTitle());
                map.put("issue", telecast.getIssue());
                map.put("start", DateUtils.dateToString(telecast.getEnterStart()));
                map.put("end", DateUtils.dateToString(telecast.getEnterEnd()));
                map.put("admin", telecast.getAdmin());
                map.put("total", enterTotal.get(telecast.getId()));
                map.put("create", DateUtils.dateToString(telecast.getCreateTime()));
                map.put("status", telecast.getOnlineStatus());
                maps.add(map);
            });
        }

        int start = ((pageIndex - 1) / 10) * 10 + 1;
        int end = ((pageIndex - 1) / 10 + 1) * 10;

        model.addAttribute("query", url)
                .addAttribute("pageIndex", pageIndex)
                .addAttribute("pageCount", telecastPage.getTotalPages())
                .addAttribute("telecasts", maps)
                .addAttribute("pageCount", telecastPage.getTotalPages())
                .addAttribute("start", start)
                .addAttribute("end", end);

        if (pageIndex > (telecastPage.getTotalPages() / 10) * 10) {
            model.addAttribute("end", telecastPage.getTotalPages());
        }

        return "opmanager/telecast/list";
    }

    @RequestMapping(value = "/details.vpage")
    public String details(Model model) {

        String id = requestString("id");

        model.addAttribute("types", EnterType.values());

        if (StringUtils.isNotBlank(id)) {
            Telecast telecast = crmTelecastLoader.load(id);
            if (null != telecast) {
                //model.addAttribute("telecast", JsonUtils.toJson(telecast));
                model.addAttribute("id", telecast.getId())
                        .addAttribute("title", telecast.getTitle())
                        .addAttribute("recomBackImg", telecast.getRecomBackImg())
                        .addAttribute("onlineStatus", telecast.getOnlineStatus())
                        .addAttribute("liveEnd", DateUtils.dateToString(telecast.getLiveEnd()))
                        .addAttribute("titleImg", telecast.getListTitleImg())
                        .addAttribute("listBackImg", telecast.getListBackImg())
                        .addAttribute("listTitleImg", telecast.getListTitleImg())
                        .addAttribute("issue", telecast.getIssue())
                        .addAttribute("headImg", telecast.getHeadImg())
                        .addAttribute("enterType", telecast.getEnterType().name())
                        .addAttribute("enterText", telecast.getEnterText())
                        .addAttribute("enterEnd", DateUtils.dateToString(telecast.getEnterEnd()))
                        .addAttribute("enterStart", DateUtils.dateToString(telecast.getEnterStart()))
                        .addAttribute("liveId", telecast.getLiveId())
                        .addAttribute("enterPage", telecast.getEnterPage())
                        .addAttribute("liveStart", DateUtils.dateToString(telecast.getLiveStart()))
                        .addAttribute("playbackPage", telecast.getPlaybackPage())
                        .addAttribute("recomButtonImg", telecast.getRecomButtonImg())
                        .addAttribute("recomTitleImg", telecast.getRecomTitleImg())
                        .addAttribute("recomStatus", telecast.getRecomStatus() ? "true" : "false")
                        .addAttribute("shardImg", telecast.getShardImg())
                        .addAttribute("gray", telecast.getGray())
                        .addAttribute("stage", telecast.onProduction() ? Telecast.Stage.production.name() : Telecast.Stage.testing.name());
            }
        }

        return StringUtils.equals(requestString("action"), "review") ? "opmanager/telecast/review" : "opmanager/telecast/details";
    }

    @ResponseBody
    @RequestMapping(value = "/save.vpage", method = {RequestMethod.POST})
    public MapMessage save() {

        String id = requestString("id", RandomUtils.nextObjectId());
        String title = requestString("title");
        if (StringUtils.isEmpty(title)) {
            return MapMessage.errorMessage();
        }
        Integer issue = requestInteger("issue");
        if (null == issue || 0 > issue) {
            return MapMessage.errorMessage();
        }
        Date liveStart = requestDate("liveStart", "yyyy-MM-dd hh:mm", null);
        if (null == liveStart) {
            return MapMessage.errorMessage();
        }
        Date liveEnd = requestDate("liveEnd", "yyyy-MM-dd hh:mm", null);
        if (null == liveEnd) {
            return MapMessage.errorMessage();
        }
        Date enterStart = requestDate("enterStart", "yyyy-MM-dd hh:mm", null);
        if (null == enterStart) {
            return MapMessage.errorMessage();
        }
        Date enterEnd = requestDate("enterEnd", "yyyy-MM-dd hh:mm", null);
        if (null == enterEnd) {
            return MapMessage.errorMessage();
        }
        String liveId = requestString("liveId");
        if (StringUtils.isEmpty(liveId)) {
            return MapMessage.errorMessage();
        }
        String headImg = requestString("headImg");
        if (StringUtils.isEmpty(headImg)) {
            return MapMessage.errorMessage();
        }
        /*Boolean onlineStatus = requestBoolean("onlineStatus");
        if (null == onlineStatus) {
            return MapMessage.errorMessage();
        }*/
        List<TelecastIntro> enterPages = requestIntro("enterPageTitle", "enterPageContent");
        if (CollectionUtils.isEmpty(enterPages)) {
            return MapMessage.errorMessage();
        }
        List<TelecastIntro> playbackPages = requestIntro("playbackPageTitle", "playbackPageContent");
        if (CollectionUtils.isEmpty(playbackPages)) {
            return MapMessage.errorMessage();
        }
        EnterType enterType = EnterType.valueOf(requestString("enterType"));
        /*if (null == enterType) {
            return MapMessage.errorMessage();
        }*/
        String enterText = requestString("enterText");
        if (StringUtils.isEmpty(enterText)) {
            return MapMessage.errorMessage();
        }
        String recomBackImg = requestString("recomBackImg");
        if (StringUtils.isEmpty(recomBackImg)) {
            return MapMessage.errorMessage();
        }
        String recomTitleImg = requestString("recomTitleImg");
        if (StringUtils.isEmpty(recomTitleImg)) {
            return MapMessage.errorMessage();
        }
        String recomButtonImg = requestString("recomButtonImg");
        if (StringUtils.isEmpty(recomButtonImg)) {
            return MapMessage.errorMessage();
        }
        Boolean recomStatus = requestBoolean("recomStatus");
        if (null == recomStatus) {
            recomStatus = false;
        }
        /*String listBackImg = requestString("listBackImg");
        if (StringUtils.isEmpty(listBackImg)) {
            return MapMessage.errorMessage();
        }*/

        String listTitleImg = requestString("listTitleImg");
        if (StringUtils.isEmpty(listTitleImg)) {
            return MapMessage.errorMessage();
        }

        String shareImg = requestString("shardImg");
        if (StringUtils.isEmpty(shareImg)) {
            return MapMessage.errorMessage();
        }

        String stage = requestString("stage");

        String admin = getCurrentAdminUser().getAdminUserName();

        String gray = getRequestString("gray");

        Telecast telecast = new Telecast();
        if(StringUtils.isBlank(requestString("id"))){
            telecast.setOnlineStatus(false);
        }
        telecast.setId(id);
        telecast.setAdmin(admin);
        telecast.setEnterEnd(enterEnd);
        telecast.setEnterPage(enterPages);
        telecast.setEnterStart(enterStart);
        telecast.setEnterText(enterText);
        telecast.setEnterType(enterType);
        telecast.setHeadImg(headImg);
        telecast.setIssue(issue);
        //telecast.setListBackImg(listBackImg);
        telecast.setListTitleImg(listTitleImg);
        telecast.setLiveEnd(liveEnd);
        telecast.setLiveId(liveId);
        telecast.setLiveStart(liveStart);
        telecast.setPlaybackPage(playbackPages);
        telecast.setRecomBackImg(recomBackImg);
        telecast.setRecomButtonImg(recomButtonImg);
        telecast.setRecomStatus(recomStatus);
        telecast.setRecomTitleImg(recomTitleImg);
        telecast.setShardImg(shareImg);
        telecast.setTitle(title);
        telecast.setGray(gray);
        telecast.setStage(Telecast.Stage.production.name().equals(stage) ? Telecast.Stage.production : Telecast.Stage.testing);

        crmTelecastService.save(telecast);

        AdminLog adminLog = new AdminLog();
        adminLog.setAdminUserName(getCurrentAdminUser().getAdminUserName());
        adminLog.setComment(!StringUtils.isEmpty(requestString("id")) ? "修改基础信息，课程详情、报名流程" : "创建课程");
        adminLog.setTargetStr(telecast.getId());
        adminLog.setOperation("telecast-edit");
        adminLogServiceClient.getAdminLogService().persistAdminLog(adminLog);
        return MapMessage.successMessage().set("id", telecast.getId());
    }

    @ResponseBody
    @RequestMapping(value = "/online.vpage", method = RequestMethod.POST)
    public MapMessage online() {
        String id = requestString("id");
        if (StringUtils.isEmpty(id)) {
            return MapMessage.errorMessage();
        }
        Telecast telecast = crmTelecastLoader.load(id);
        if (null == telecast) {
            return MapMessage.errorMessage();
        }

        if (telecast.getOnlineStatus()) {
            crmTelecastService.offline(id);
            AdminLog adminLog = new AdminLog();
            adminLog.setAdminUserName(getCurrentAdminUser().getAdminUserName());
            adminLog.setComment("课程下线");
            adminLog.setTargetStr(telecast.getId());
            adminLog.setOperation("telecast-edit");
            adminLogServiceClient.getAdminLogService().persistAdminLog(adminLog);
        } else {
            crmTelecastService.online(id);
            AdminLog adminLog = new AdminLog();
            adminLog.setAdminUserName(getCurrentAdminUser().getAdminUserName());
            adminLog.setComment("课程上线");
            adminLog.setTargetStr(telecast.getId());
            adminLog.setOperation("telecast-edit");
            adminLogServiceClient.getAdminLogService().persistAdminLog(adminLog);
        }

        return MapMessage.successMessage();
    }

    @ResponseBody
    @RequestMapping(value = "/delete.vpage", method = RequestMethod.POST)
    public MapMessage delete() {
        String id = requestString("id");
        if (StringUtils.isEmpty(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        Telecast telecast = crmTelecastLoader.load(id);
        if(null == telecast){
            return MapMessage.errorMessage("课程不存在");
        }

        if(telecast.onDeleted()){
            return MapMessage.errorMessage("课程已经删除");
        }

        if(telecast.getOnlineStatus()){
            return MapMessage.errorMessage("上线课程不允许删除");
        }

        crmTelecastService.delete(id);
        AdminLog adminLog = new AdminLog();
        adminLog.setAdminUserName(getCurrentAdminUser().getAdminUserName());
        adminLog.setComment("删除课程");
        adminLog.setTargetStr(id);
        adminLog.setOperation("telecast-edit");
        adminLogServiceClient.getAdminLogService().persistAdminLog(adminLog);
        return MapMessage.successMessage();
    }

    @ResponseBody
    @RequestMapping(value = "/notice.vpage", method = RequestMethod.POST)
    public MapMessage notice() {
        String telecastId = getRequestString("id");
        long userId = getRequestLong("uid");
        if (StringUtils.isEmpty(telecastId) || 0 > userId) {
            return MapMessage.errorMessage("参数错误");
        }

        Telecast telecast = crmTelecastLoader.load(telecastId);
        if (null == telecast) {
            return MapMessage.errorMessage("参数错误");
        }

        String title = "大咖讲座";
        String domain = RuntimeMode.isProduction()
                ? "https://parent.17zuoye.com"
                : RuntimeMode.isStaging()
                ? "https://parent.staging.17zuoye.net"
                : "https://parent.test.17zuoye.net";

        String url = domain + "/karp/parent_university/index/IntroductoryCourses?id=" + telecastId;

        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(userId);
        appMessage.setTitle(title);
        appMessage.setMessageType(ParentMessageType.REMINDER.type);
        appMessage.setContent(telecast.getTitle() + "预览页面");
        appMessage.setLinkUrl(url);
        appMessage.setLinkType(0);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);


        return MapMessage.successMessage();
    }

    private List<TelecastIntro> requestIntro(String title, String content) {

        String[] titleArray = requestArray(title);
        String[] contentArray = requestArray(content);
        if (titleArray.length != contentArray.length) {
            return Collections.emptyList();
        }

        int length = titleArray.length;
        List<TelecastIntro> intros = new LinkedList<>();
        for (int i = 0; i < length; i++) {
            TelecastIntro intro = new TelecastIntro();
            intro.setTitle(titleArray[i]);
            intro.setContent(contentArray[i]);
            intros.add(intro);
        }

        return intros;
    }

    @RequestMapping(value = "/upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadBackground(MultipartFile inputFile) {
        try {
            String path = AdminOssManageUtils.upload(inputFile, "telecast");
            String target = getRequestString("target");
            return MapMessage.successMessage().add("path", path).add("target", target);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        String action = getRequestString("action");
        MapMessage mapMessage = new MapMessage();

        switch (action) {
            case "config":
                mapMessage.set("imageActionName", "uploadimage");
                mapMessage.set("imageFieldName", "upfile");
                mapMessage.set("imageInsertAlign", "none");
                mapMessage.set("imageMaxSize", 2048000);
                mapMessage.set("imageUrlPrefix", "");
                mapMessage.set("imageAllowFiles", new String[]{".png", ".jpg", ".jpeg", ".gif"});
                mapMessage.set("videoActionName", "uploadvideo");
                mapMessage.set("videoFieldName", "upfile");
                mapMessage.set("videoUrlPrefix", "");
                mapMessage.set("videoMaxSize", 20971520);
                mapMessage.set("videoAllowFiles", new String[]{".flv", ".swf", ".mkv", ".avi", ".rm", ".rmvb", ".mpeg", ".mpg", ".ogg", ".ogv", ".mov", ".wmv", ".mp4", ".webm", ".mp3", ".wav", ".mid"});
                mapMessage.setSuccess(true);
                break;
            case "uploadimage":
            case "uploadvideo":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");

                if (imgFile.isEmpty()) {
                    mapMessage.setSuccess(false);
                    mapMessage.setInfo("没有文件上传");
                } else {
                    try {
                        String filename = imgFile.getOriginalFilename();
                        String path = AdminOssManageUtils.upload(imgFile, "17shuo/editor");
                        mapMessage.add("url", path)
                                .add("title", filename)
                                .add("state", "SUCCESS")
                                .add("original", filename);
                        mapMessage.setSuccess(true);
                    } catch (Exception e) {
                        mapMessage.setSuccess(false);
                        mapMessage.setInfo("文件上传异常");
                    }
                }
                break;
        }

        return mapMessage;
    }

}
