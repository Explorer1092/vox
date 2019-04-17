package com.voxlearning.washington.controller.ai;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.api.ChipsTaskLoader;
import com.voxlearning.utopia.service.ai.api.ChipsTaskService;
import com.voxlearning.utopia.service.ai.api.ChipsUserVideoService;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserExtSplit;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chips/task")
public class ChipsTaskController extends AbstractAiController {

    @ImportService(interfaceClass = ChipsTaskLoader.class)
    private ChipsTaskLoader chipsTaskLoader;

    @ImportService(interfaceClass = ChipsTaskService.class)
    private ChipsTaskService chipsTaskService;

    @ImportService(interfaceClass = ChipsUserVideoService.class)
    private ChipsUserVideoService chipsUserVideoService;

    private UtopiaCache UNFLUSHABLE_CACHE;

    private static String QRCODE_CACHE_PRE = "chips_qrcode_pre_1_";


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        UNFLUSHABLE_CACHE = CacheSystem.CBS.getCacheBuilder().getCache("unflushable");
    }

    //红点页面详情
    @RequestMapping(value = "reddot/page/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage redDotPageDetail() {
        User user = currentUser();
        if (user == null) {
            return failMessage("400", "没有登录");
        }
        String code = getRequestString("code");
        if (StringUtils.isBlank(code)) {
            return failMessage("400", "参数错误");
        }
        return wrapper(message -> {
            message.putAll(chipsTaskLoader.loadUserPageRedDot(user.getId(), code));
            if ("course_begin_enroll_info".equals(code)) {
                message.set("wechatDomain", getWechatSiteUrl());
            }
        });
    }

    //红点页面点击阅读
    @RequestMapping(value = "reddot/page/read.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage redDotPageRead() {
        User user = currentUser();
        if (user == null) {
            return failMessage("400", "没有登录");
        }

        String code = getRequestString("code");
        if (StringUtils.isBlank(code)) {
            return failMessage("400", "参数错误");
        }
        return wrapper(message ->{
            MapMessage ret = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("chipsTaskService.processPageRedDotRead")
                    .keys(user.getId(), code)
                    .callback(() -> chipsTaskService.processPageRedDotRead(code, user.getId()))
                    .build()
                    .execute();
            message.putAll(ret);
        });
    }

    // 提交问卷
    @RequestMapping(value = "ugc/submit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ugcSubmit() {
        Long userId = getRequestContext().getUserId();
        if (userId == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String grade = getRequestString("grade");
        String studyDuration = getRequestString("studyDuration");
        String expect = getRequestString("expect");
        String weekPoints = getRequestString("weekPoints");
        String otherExtraRegistration = getRequestString("otherExtraRegistration");
        String recentlyScore = getRequestString("recentlyScore");
        Integer serviceScore = getRequestInt("serviceScore");
        String recipientName = getRequestString("recipientName");
        String recipientTel = getRequestString("recipientTel");
        String recipientAddr = getRequestString("recipientAddr");
        String courseLevel = getRequestString("courseLevel");

        return wrapper(mm -> {
            MapMessage re = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("loadAndRecordTaskTalk")
                    .keys(userId)
                    .callback(() -> {
                        ChipsEnglishUserExtSplit extSplit = chipsEnglishClazzService.selectChipsEnglishUserExtSplitByUserId(userId);
                        if (extSplit == null) {
                            extSplit = new ChipsEnglishUserExtSplit();
                            extSplit.setCreateTime(new Date());
                            extSplit.setId(userId);
                        }

                        extSplit.setUpdateTime(new Date());
                        if (StringUtils.isNotBlank(grade)) {
                            extSplit.setGrade(grade);
                        }

                        if (StringUtils.isNotBlank(studyDuration)) {
                            extSplit.setStudyDuration(studyDuration);
                        }

                        if (StringUtils.isNotBlank(weekPoints)) {
                            extSplit.setWeekPoints(weekPoints);
                        }

                        if (StringUtils.isNotBlank(expect)) {
                            extSplit.setExpect(expect);
                        }

                        if (StringUtils.isNotBlank(otherExtraRegistration)) {
                            extSplit.setOtherExtraRegistration(otherExtraRegistration);
                        }

                        if (serviceScore != null) {
                            extSplit.setServiceScore(serviceScore);
                        }

                        if (StringUtils.isNotBlank(recentlyScore)) {
                            extSplit.setRecentlyScore(recentlyScore);
                        }

                        if (StringUtils.isNotBlank(recipientName)) {
                            extSplit.setRecipientName(recipientName);
                        }

                        if (StringUtils.isNotBlank(recipientTel)) {
                            extSplit.setRecipientTel(recipientTel);
                        }

                        if (StringUtils.isNotBlank(recipientAddr)) {
                            extSplit.setRecipientAddr(recipientAddr);
                        }

                        if (StringUtils.isNotBlank(courseLevel)) {
                            extSplit.setCourseLevel(courseLevel);
                        }
                        return chipsEnglishClazzService.upsertChipsEnglishUserExtSplit(extSplit);
                    })
                    .build()
                    .execute();
            mm.putAll(re);
        });
    }

    //
    @RequestMapping(value = "drawing/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage drawingDetail() {
        User user = currentUser();
        if (user == null) {
            return failMessage("400", "没有登录");
        }
        long drawingTaskId = getRequestLong("drawingTaskId");
        if (drawingTaskId <= 0L) {
            return failMessage("400", "参数错误");
        }
        return wrapper(message -> message.putAll(chipsTaskLoader.loadMyDrawingTask(user.getId(), drawingTaskId)));
    }

    @RequestMapping(value = "drawing/tablist.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage drawingTabList() {
        User user = currentUser();
        if (user == null) {
            return failMessage("400", "没有登录");
        }

        String labelCode = getRequestParameter("labelCode", "");
        int page = getRequestInt("page", 1);
        if (page <= 0) {
            return failMessage("400", "参数错误");
        }
        int pageSize = getRequestInt("pageSize", 0);
        return wrapper(message -> message.putAll(chipsTaskLoader.loadDrawingTabList(user.getId(), labelCode, page, pageSize)));
    }

    @RequestMapping(value = "drawing/share.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage drawingShare() {
        User user = currentUser();
        if (user == null) {
            return failMessage("400", "没有登录");
        }

        long drawingTaskId = getRequestLong("drawingTaskId");
        if (drawingTaskId <= 0L) {
            return failMessage("400", "参数错误");
        }
        return wrapper(message -> {
            MapMessage res = chipsTaskLoader.loadDrawingShareInfo(drawingTaskId);
            if (res.isSuccess()) {
                String qrCode = Optional.ofNullable(SafeConverter.toString(res.get("qrUrl")))
                        .filter(StringUtils::isNotBlank)
                        .map(url -> url + "&ori=app")
                        .map(url -> {
                            Object cache = UNFLUSHABLE_CACHE.load(QRCODE_CACHE_PRE + url);
                            if (cache != null) {
                                return SafeConverter.toString(cache);
                            }
                            String qr = urlToQRCode(url);
                            UNFLUSHABLE_CACHE.set(QRCODE_CACHE_PRE + url, 60 * 60 * 24 * 15, qr);
                            return qr;
                        })
                        .orElse("");
                message.set("qrCode", qrCode);
            }
            message.putAll(res);
        });
    }


    @RequestMapping(value = "drawing/video/synthesis.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage drawingVideoSynthesis(@RequestParam(value = "drawingTaskId", required = false) long drawingTaskId,
                                            @RequestParam(value = "coverImage", required = false) String cover,
                                            @RequestParam(value = "videos", required = false) String[] videos) {
        User user = currentUser();
        if (user == null) {
            return failMessage("400", "没有登录");
        }

        if (drawingTaskId <= 0L || videos == null || videos.length <= 1 || StringUtils.isBlank(cover)) {
            return failMessage("400", "参数错误");
        }

        return wrapper(message -> {
                    MapMessage ret = AtomicCallbackBuilderFactory.getInstance()
                            .<MapMessage>newBuilder()
                            .keyPrefix("chipsUserVideoService.synthesisDrawingTaskVideo")
                            .keys(user.getId(), drawingTaskId)
                            .callback(() -> chipsUserVideoService.synthesisDrawingTaskVideo(user.getId(), drawingTaskId, cover, Arrays.stream(videos).collect(Collectors.toList())))
                            .build()
                            .execute();
                    message.putAll(ret);
                }
        );
    }

    private String urlToQRCode(String shortUrl) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            urlToQRCode(shortUrl, out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }
}
