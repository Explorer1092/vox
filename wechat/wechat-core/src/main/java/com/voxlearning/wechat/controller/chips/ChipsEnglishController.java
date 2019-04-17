package com.voxlearning.wechat.controller.chips;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.payment.PaymentRequestForm;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.ai.api.*;
import com.voxlearning.utopia.service.ai.cache.UserInvitationRankCache;
import com.voxlearning.utopia.service.ai.cache.UserScoreRankCache;
import com.voxlearning.utopia.service.ai.cache.UserShareVideoRankCache;
import com.voxlearning.utopia.service.ai.client.AiChipsEnglishConfigServiceClient;
import com.voxlearning.utopia.service.ai.client.AiLoaderClient;
import com.voxlearning.utopia.service.ai.client.AiTodayLessonClient;
import com.voxlearning.utopia.service.ai.constant.*;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.support.ChipsInvitationHelper;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.wechat.api.constants.BooKConst;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.AuthType;
import com.voxlearning.wechat.context.WxConfig;
import com.voxlearning.wechat.controller.AbstractChipsController;
import com.voxlearning.wechat.controller.OAuthController;
import com.voxlearning.wechat.service.DailyLessonService;
import com.voxlearning.wechat.support.WechatConfig;
import com.voxlearning.wechat.support.WechatPictureUploader;
import com.voxlearning.wechat.support.utils.OAuthUrlGenerator;
import com.voxlearning.wechat.support.utils.StringExtUntil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2018/4/25
 */
@Controller
@RequestMapping(value = "/chips")
public class ChipsEnglishController extends AbstractChipsController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private WechatPictureUploader uploader;

    @Inject
    private CouponLoaderClient couponLoaderClient;

    @Inject
    private AiLoaderClient aiLoaderClient;

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private AiChipsEnglishConfigServiceClient aiChipsEnglishConfigServiceClient;

    @ImportService(interfaceClass = ChipsOrderProductLoader.class)
    private ChipsOrderProductLoader chipsOrderProductLoader;

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @ImportService(interfaceClass = ChipsUserOralScheduleService.class)
    private ChipsUserOralScheduleService chipsUserOralScheduleService;

    @Inject
    private DailyLessonService dailyLessonService;

    @Inject
    private AiTodayLessonClient aiTodayLessonClient;
    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    @ImportService(interfaceClass = ChipsEnglishUserLoader.class)
    private ChipsEnglishUserLoader chipsEnglishUserLoader;

    @ImportService(interfaceClass = ChipsActiveService.class)
    private ChipsActiveService chipsActiveService;

    @ImportService(interfaceClass = ChipsOrderLoader.class)
    private ChipsOrderLoader chipsOrderLoader;

    @ImportService(interfaceClass = ChipsWechatUserLoader.class)
    private ChipsWechatUserLoader chipsWechatUserLoader;

    @ImportService(interfaceClass = ChipsInvitionRewardService.class)
    private ChipsInvitionRewardService chipsInvitionRewardService;

    @ImportService(interfaceClass = ChipsInvitionRewardLoader.class)
    private ChipsInvitionRewardLoader chipsInvitionRewardLoader;

    @AlpsQueueProducer(queue = "utopia.chips.wechat.user.group.shopping.visit.queue")
    private MessageProducer wechatUserGroupShoppingVisitQueueProducer;

    @ImportService(interfaceClass = ChipsUserPageViewLogService.class)
    private ChipsUserPageViewLogService chipsUserPageViewLogService;

    // 个人中心
    @RequestMapping(value = "/center/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }
        List<UserOrder> chipsOrder = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user.getId());
        model.addAttribute("Paid", CollectionUtils.isNotEmpty(chipsOrder));
        return "/parent/chips/index";
    }

    // 我的课程（已购课程）
    @RequestMapping(value = "/center/mycourse.vpage", method = RequestMethod.GET)
    public String myCourse(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }
        List<ChipsUserCourseMapper> userCourseMapperList = chipsEnglishUserLoader.loadUserEffectiveCourse(user.getId());
        List<Map<String, Object>> res = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userCourseMapperList)) {
            userCourseMapperList.forEach(e -> {
                Map<String, Object> bean = new HashMap<>();
                bean.put("productName", e.getProductName());
                bean.put("rank", e.getRank());
                res.add(bean);
            });
        }
        model.addAttribute("orders", res);
        return "/parent/chips/mycourse";
    }

    //音频demo页
    @RequestMapping(value = "/center/voicedemo.vpage", method = RequestMethod.GET)
    public String voicedemo(Model model) {
        try {
            WechatType wechatType = WechatType.CHIPS;
            WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
            initWechatConfigModel(model, wxConfig, wechatType);
            return "/parent/chips/voicedemo";
        } catch (Exception ex) {
            return redirectWithMsg("生成失败", model);
        }
    }

    //音频demo上报音频
    @RequestMapping(value = "/center/voiceupload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadvoicedemo() {
        String id = getRequestString("serverId");
        if (RuntimeMode.lt(Mode.STAGING)) {
            logger.info("uploadvoicedemo id:{}", id);
        }
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", 30012,
                "mod1", id,
                "op", "chips center voice demo"
        ));
        return MapMessage.successMessage();
    }

    // 优秀视频
    @RequestMapping(value = "/center/excellentvideo.vpage", method = RequestMethod.GET)
    public String excellentVideo(Model model) {
        return "/parent/chips/excellentvideo";
    }

    // 敬请期待
    @RequestMapping(value = "/center/comingsoon.vpage", method = RequestMethod.GET)
    public String comingSoon(Model model) {
        return "/parent/chips/comingsoon";
    }

    // app下载页
    @RequestMapping(value = "/center/downloadapp.vpage", method = RequestMethod.GET)
    public String downloadApp(Model model) {
        return "/parent/chips/downloadapp";
    }

    // app下载介绍页
    @RequestMapping(value = "/center/downloadappintro.vpage", method = RequestMethod.GET)
    public String downloadappintro(Model model) {
        return "/parent/chips/downloadappintro";
    }

    // 登录页
    @RequestMapping(value = "/center/login.vpage", method = RequestMethod.GET)
    public String login(Model model) {
        return "/parent/chips/login";
    }

    // 正式课广告页
    @RequestMapping(value = "/center/robinnormal.vpage", method = RequestMethod.GET)
    public String robinnormal(Model model) {
//        String inviter = getRequestString("inviter");
//        model.addAttribute("inviter", inviter);
//        return "/parent/chips/courseoffline";
        return redirectWithMsg("活动已经下线", model);
    }

    // 地推广告页 -- 旅行口语
    @RequestMapping(value = "/center/ground_be_travel.vpage", method = RequestMethod.GET)
    public String groundBeTravel(Model model) {
//        return "/parent/chips/ground_be_travel";
        return redirectWithMsg("活动已经下线", model);
    }

    // 针对往期“低转正”未续费用户－模版消息推送
    @RequestMapping(value = "/center/waiting_for_you.vpage", method = RequestMethod.GET)
    public String waitingForYou(Model model) {
        return "/parent/chips/temporary/waiting_for_you";
        // return redirectWithMsg("活动已经下线", model);
    }

    // 邀请有奖
    @RequestMapping(value = "/center/invite_award_activity.vpage", method = RequestMethod.GET)
    public String inviteAwardactivity(Model model) {
        String openId = getOpenId();
        boolean redirect = Optional.ofNullable(openId)
                .filter(StringUtils::isNotBlank)
                .map(e -> {
                    ChipsWechatUser wechatUser = chipsWechatUserLoader.loadByOpenId(e, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
                    return wechatUser == null || StringUtils.isBlank(wechatUser.getNickName());
                }).orElse(true);
        if (redirect) {
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChips(AuthType.CHIPS_INVITATION2, "");
        }
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);

        MapMessage mapMessage = chipsInvitionRewardLoader.loadInvitionConfig();
        String beginDate = Optional.ofNullable(mapMessage)
                .map(e -> SafeConverter.toString(e.get("acBeginDate")))
                .map(DateUtils::stringToDate)
                .map(e -> DateUtils.dateToString(e, "MM月dd日"))
                .orElse("");
        model.addAttribute("beginDate", beginDate);
        String endDate = Optional.ofNullable(mapMessage)
                .map(e -> SafeConverter.toString(e.get("acEndDate")))
                .map(DateUtils::stringToDate)
                .map(e -> DateUtils.dateToString(e, "MM月dd日"))
                .orElse("");
        model.addAttribute("endDate", endDate);
        model.addAttribute("productId", SafeConverter.toString(mapMessage.get("productId")));
        return "/parent/chips/invite_award_activity";
    }

    // 邀请有奖保存图片
    @RequestMapping(value = "/center/invite_award_pic.vpage", method = RequestMethod.GET)
    public String inviteAwardactivityP(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChipsLogin(AuthType.CHIPS_INVITATION_PIC, "");
        }
        MapMessage mapMessage = chipsInvitionRewardLoader.loadInvitionConfig();
        String beginDate = Optional.ofNullable(mapMessage)
                .map(e -> SafeConverter.toString(e.get("acBeginDate")))
                .map(DateUtils::stringToDate)
                .map(e -> DateUtils.dateToString(e, "MM月dd日"))
                .orElse("");
        model.addAttribute("beginDate", beginDate);
        String endDate = Optional.ofNullable(mapMessage)
                .map(e -> SafeConverter.toString(e.get("acEndDate")))
                .map(DateUtils::stringToDate)
                .map(e -> DateUtils.dateToString(e, "MM月dd日"))
                .orElse("");
        model.addAttribute("endDate", endDate);
        String linkUrl = SafeConverter.toString(mapMessage.get("linkUrl")) + "?inviter=" + user.getId() + "&refer=330352&channel=wechat&type=invite";

        try {
            model.addAttribute("linkUrl", URLEncoder.encode(linkUrl, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("linkUrl:{} error", linkUrl, e);
        }
        ChipsWechatUser wechatUser = chipsWechatUserLoader.loadByOpenId(getOpenId(), WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
        String avatar = Optional.ofNullable(wechatUser)
                .map(ChipsWechatUser::getAvatar)
                .orElse("");
        model.addAttribute("avatar", avatar);
        String nickName = Optional.ofNullable(wechatUser)
                .map(ChipsWechatUser::getNickName)
                .orElse("");
        model.addAttribute("nickName", nickName);

        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/invite_award_pic";
    }

    // 邀请有奖广告页
    @RequestMapping(value = "/center/invite_be.vpage", method = RequestMethod.GET)
    public String inviteBe(Model model) {
        String openId = getOpenId();
        long inviter = getRequestLong("inviter");
        String refer = getRequestString("refer");
        String channel = getRequestString("channel");
        boolean redirect = Optional.ofNullable(openId)
                .filter(StringUtils::isNotBlank)
                .map(e -> {
                    ChipsWechatUser wechatUser = chipsWechatUserLoader.loadByOpenId(e, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
                    return wechatUser == null || StringUtils.isBlank(wechatUser.getNickName());
                }).orElse(true);
        if (redirect) {
            String param = "inviter=" + inviter + "&refer=" + refer + "&channel=" + channel;
            String key = StringExtUntil.md5(param);
            persistenceCache(key, param);
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChips(AuthType.CHIPS_INVITATION_BE, key);
        }

        String productId = Optional.ofNullable(chipsInvitionRewardLoader.loadInvitionConfig())
                .map(message -> message.get("productId"))
                .map(SafeConverter::toString)
                .orElse("");
        chipsInvitionRewardService.processInvitionPageVisit(openId, inviter, productId);

        model.addAttribute("inviter", inviter);
        model.addAttribute("channel", channel);
        model.addAttribute("refer", refer);
        return "/parent/chips/ground_be_travel";
    }

    // 邀请有奖广告页
    @RequestMapping(value = "/activity/invite.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviteActivity() {
        User user = currentChipsUser();
        return chipsInvitionRewardLoader.loadInvitionConfigDetail(user != null ? user.getId() : null);
    }

    // 邀请有奖-个人中心
    @RequestMapping(value = "/center/invite_personal_center.vpage", method = RequestMethod.GET)
    public String invitePersonalCenter(Model model) {
        User user = currentChipsUser();
        String activity = getRequestString("activity");
        if (user == null) {
            String param = "activity=" + activity;
            String key = StringExtUntil.md5(param);
            persistenceCache(key, param);
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChipsLogin(AuthType.CHIPS_PERSONAL_REWARD, key);
        }
        ChipsWechatUser wechatUser = chipsWechatUserLoader.loadByOpenId(getOpenId(), WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
        String avatar = Optional.ofNullable(wechatUser)
                .map(ChipsWechatUser::getAvatar)
                .orElse("");
        model.addAttribute("avatar", avatar);
        model.addAttribute("activityType", activity);
        return "/parent/chips/invite_personal_center";
    }

    // 邀请有奖-个人中心-邀请人数
    @RequestMapping(value = "/center/invite_personal.vpage", method = RequestMethod.GET)
    public String invitePersonalNum(Model model) {
        return "/parent/chips/invite_personal_num";
    }

    // 邀请有奖-我的收入
    @RequestMapping(value = "/center/invite_my_income.vpage", method = RequestMethod.GET)
    public String inviteMyIncome(Model model) {
        return "/parent/chips/invite_my_income";
    }


    @RequestMapping(value = "/center/ground_be_advanced.vpage", method = RequestMethod.GET)
    public String groundBeAdvanced(Model model) {
//        return "/parent/chips/ground_be_advanced";
        return redirectWithMsg("活动已经下线", model);
    }

    // 地推广告页 -- 选择学习英语年限
    @RequestMapping(value = "/center/{sid}/learning_duration.vpage", method = RequestMethod.GET)
    public String learningDuration(Model model, @PathVariable("sid") String sid) {
//        model.addAttribute("staffId", sid);
//        return "/parent/chips/learning_duration";
        return redirectWithMsg("活动已经下线", model);
    }

    // 广告页
    @RequestMapping(value = "/center/be_normal.vpage", method = RequestMethod.GET)
    public String be_normal(Model model) {
//        User user = currentChipsUser();
//        if (user == null) {
//            return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_FORMAL_AD_4);
//        }
//        return "/parent/chips/benormal";
        return redirectWithMsg("活动已经下线", model);
    }

    // 正式课拼团
    @RequestMapping(value = "/center/formal_group_buy.vpage", method = RequestMethod.GET)
    public String formal_group_buy(Model model) {
        String openId = getOpenId();
        boolean redirect = Optional.ofNullable(openId)
                .filter(StringUtils::isNotBlank)
                .map(e -> {
                    ChipsWechatUser wechatUser = chipsWechatUserLoader.loadByOpenId(e, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
                    return wechatUser == null || StringUtils.isBlank(wechatUser.getNickName());
                }).orElse(true);
        if (redirect) {
            String code = getRequestString("code");
            String origin = getRequestString("origin");
            String paramVal = "origin=" + origin + "&code=" + code;
            String key = StringExtUntil.md5(paramVal);
            persistenceCache(key, paramVal);
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChips(AuthType.CHIPS_GROUP_SHOPPING, key);
        }
        groupByPageViewLog();
        Map<String, Object> message = new HashMap<>();
        message.put("ID", getOpenId());
        wechatUserGroupShoppingVisitQueueProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));

        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);

        return "/parent/chips/formal_group_buy";
    }

    private void groupByPageViewLog() {
        User user = currentChipsUser();
        if (user != null) {
            ChipsUserPageViewLog log = new ChipsUserPageViewLog();
            String uniqueKey = PageViewType.GROUP_BUY.name();
            log.setId(ChipsUserPageViewLog.genId(user.getId(), uniqueKey));
            log.setUserId(user.getId());
            log.setUniqueKey(uniqueKey);
            log.setType(PageViewType.GROUP_BUY);
            log.setDisabled(false);
            chipsUserPageViewLogService.upsertChipsUserPageViewLog(log);
        }
    }

    // 正式课拼团消息页
    @RequestMapping(value = "/center/formal_group_message.vpage", method = RequestMethod.GET)
    public String formal_group_message(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_FORMAL_AD_1);
        }

        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        model.addAttribute("userName", "Mic");
        model.addAttribute("image", "https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=1672982947,1831013618&fm=179&app=42&f=JPEG?w=121&h=140");
        model.addAttribute("surplusTime", "10");
        model.addAttribute("code", "code");
        initWechatConfigModel(model, wxConfig, wechatType);

        return "/parent/chips/formal_group_message";
    }

    // 正式课广告页G1/G2/G3
    @RequestMapping(value = "/center/{grade}/formal_robin.vpage", method = RequestMethod.GET)
    public String formalAdvertisement(Model model, @PathVariable("grade") int grade) {
//        User user = currentChipsUser();
//        if (user == null) {
//            switch (grade) {
//                case 1:
//                    return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_FORMAL_AD_1);
//                case 2:
//                    return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_FORMAL_AD_2);
//                case 3:
//                    return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_FORMAL_AD_3);
//                default:
//                    return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_FORMAL_AD_1);
//            }
//
//        }
//        MapMessage mapMessage = chipsOrderProductLoader.loadRecommendGradeProductInfo(grade);
//        if (!mapMessage.isSuccess()) {
//            return redirectWithMsg(mapMessage.getInfo(), model);
//        }
//        model.addAllAttributes(mapMessage);
//        model.addAttribute("grade", grade);
//        return "/parent/chips/formal_advertisement";
        return redirectWithMsg("活动已经下线", model);
    }

    // 正式课第三期广告页
    @RequestMapping(value = "/center/{type}/formal_3.vpage", method = RequestMethod.GET)
    public String formalAdvertisement3(Model model, @PathVariable("type") String type) {
//        User user = currentChipsUser();
//        if (user == null) {
//            switch (type) {
//                case "prod4":
//                    return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_FORMAL_AD_5_2);
//                default:
//                    return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_FORMAL_AD_5_1);
//            }
//
//        }
//        model.addAttribute("type", type);
//        return "/parent/chips/formal_3";
        return redirectWithMsg("活动已经下线", model);
    }

    // 今日学习内容
    @RequestMapping(value = "/center/todaystudy.vpage", method = RequestMethod.GET)
    public String todaystudy(Model model) {
        Long clazzId = getRequestLong("clazzId");
        String unitId = getRequestString("unitId");
        if (clazzId != 0L) {
            model.addAttribute("clazzId", clazzId);
        }
        if (StringUtils.isNotBlank(unitId)) {
            model.addAttribute("unitId", unitId);
        }
        return "/parent/chips/todaystudy";
    }

    // 正式课今日学习内容
    @RequestMapping(value = "/center/todaystudynormal.vpage", method = RequestMethod.GET)
    public String todaystudynormal() {
        return "/parent/chips/todaystudynormal";
    }

    // 主动服务预览
    @RequestMapping(value = "/center/activeServicePreview.vpage", method = RequestMethod.GET)
    public String activeServicePreview(Model model) {
        Long userId = getRequestLong("userId");
        String qid = getRequestString("qid");
        model.addAttribute("userId", userId);
        model.addAttribute("qid", qid);
        String bookId = getRequestString("bookId");
        model.addAttribute("bookId", bookId);
        int unitIndex = getUnitIndex(bookId, getRequestString("unitId"));
        Map<String, StoneData> qMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(qid));
        model.addAttribute("name", Optional.ofNullable(qMap).map(m -> m.get(qid)).map(StoneQuestionData::newInstance).map(StoneQuestionData::getSchemaName).map(ChipsQuestionType::getDesc)
                .map(e -> "Day " + unitIndex + "," + e).orElse(""));
        try {
            WechatType wechatType = WechatType.CHIPS;
            WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
            initWechatConfigModel(model, wxConfig, wechatType);
            model.addAttribute("userName", obtainUserName(userId, false));
//            return "/parent/chips/voicedemo";
        } catch (Exception ex) {
            return redirectWithMsg("生成失败", model);
        }
        return "/parent/chips/activeServicePreview";
    }

    private String redirectActiveService() {
        return null;
    }

    /**
     * @return chips_active_serviceserviceType=binding_clazzId=101
     */
    private String buildActiveServiceTypeRedirectUrl() {
        StringBuffer sb = new StringBuffer();
        sb.append("chips_active_service");
        String qid = getRequestString("qid");
        long userId = getRequestLong("userId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String aid = getRequestString("aid");
        String lessonId = getRequestString("lessonId");
        String questionName = getRequestString("questionName");
        sb.append("qid=").append(qid);
        sb.append(OAuthController.activeServiceRedirectUrlSeperator).append("bookId=").append(bookId);
        sb.append(OAuthController.activeServiceRedirectUrlSeperator).append("unitId=").append(unitId);
        sb.append(OAuthController.activeServiceRedirectUrlSeperator).append("aid=").append(aid);
        sb.append(OAuthController.activeServiceRedirectUrlSeperator).append("lessonId=").append(lessonId);
        sb.append(OAuthController.activeServiceRedirectUrlSeperator).append("questionName=").append(questionName);
        if (userId != 0l) {
            sb.append(OAuthController.activeServiceRedirectUrlSeperator).append("userId=").append(userId);
        }
        return sb.toString();
    }

    // 主动服务预览
    @RequestMapping(value = "/center/activeServicePreviewV2.vpage", method = RequestMethod.GET)
    public String activeServicePreviewV2(Model model) {
//        if(RuntimeMode.isTest() || RuntimeMode.isProduction()){
//            User user = currentChipsUser();
//            if (user == null) {
//                return "redirect:" + OAuthUrlGenerator.generatorForChips(buildActiveServiceTypeRedirectUrl());
//            }
//        }
        Long userId = getRequestLong("userId");
        String qid = getRequestString("qid");
        model.addAttribute("userId", userId);
        model.addAttribute("qid", qid);
        String bookId = getRequestString("bookId");
        model.addAttribute("bookId", bookId);
        int unitIndex = getUnitIndex(bookId, getRequestString("unitId"));
        Map<String, StoneData> qMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(qid));
        model.addAttribute("name", Optional.ofNullable(qMap).map(m -> m.get(qid)).map(StoneQuestionData::newInstance).map(StoneQuestionData::getSchemaName).map(ChipsQuestionType::getDesc)
                .map(e -> "Day " + unitIndex + "," + e).orElse(""));
        model.addAttribute("sharePrimeTitle", obtainUserName(userId, false) + "DAY" + unitIndex + "的学习点评");
        model.addAttribute("shareSubTitle", "点击查看老师针对本题的发音及语法点评");
        String questionName = getRequestString("questionName");
        model.addAttribute("questionName", questionName);
        try {
            WechatType wechatType = WechatType.CHIPS;
            WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
            initWechatConfigModel(model, wxConfig, wechatType);
            model.addAttribute("userName", obtainUserName(userId, false));
        } catch (Exception ex) {
            return redirectWithMsg("生成失败", model);
        }
        return "/parent/chips/activeServicePreviewV2";
    }

    @RequestMapping(value = "activeService/queryActiveServiceUserTemplate.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage queryActiveServiceUserTemplate() {
        String qid = getRequestString("qid");
        Long userId = getRequestLong("userId");
        String bookId = getRequestString("bookId");
        MapMessage message = chipsActiveService.loadActiveServicePreviewTemplate(userId, qid, bookId);
        AIActiveServiceUserTemplateItem item = handleUserAnswer(getRequestString("unitId"), getRequestString("lessonId"), qid, getRequestString("aid"));
        if (item != null) {
            message.put("userAnswer", item);
        }
        return message;
    }

    /**
     * @return chips_other_serviceserviceType=binding_clazzId=101
     */
    private String buildOtherServiceTypeRedirectUrl() {
        StringBuffer sb = new StringBuffer();
        sb.append("chips_other_service");
        String serviceType = getRequestString("serviceType");
        long clazzId = getRequestLong("clazzId");
        String templateId = getRequestString("templateId");
        long userId = getRequestLong("userId");
        sb.append("serviceType=").append(serviceType);
        if (StringUtils.isNotBlank(templateId)) {
            sb.append(OAuthController.activeServiceRedirectUrlSeperator).append("templateId=").append(templateId);
        }
        if (clazzId != 0l) {
            sb.append(OAuthController.activeServiceRedirectUrlSeperator).append("clazzId=").append(clazzId);
        }
        if (userId != 0l) {
            sb.append(OAuthController.activeServiceRedirectUrlSeperator).append("userId=").append(userId);
        }
        return sb.toString();
    }

    @RequestMapping(value = "/center/otherServiceTypePreview.vpage", method = RequestMethod.GET)
    public String otherServiceTypePreview(Model model) {
//        if(RuntimeMode.isTest() || RuntimeMode.isProduction()){
//            User user = currentChipsUser();
//            if (user == null) {
//                return "redirect:" + OAuthUrlGenerator.generatorForChips(buildOtherServiceTypeRedirectUrl());
//            }
//        }

        String serviceTypeStr = getRequestString("serviceType");
        long userId = getRequestLong("userId");
        long clazzId = getRequestLong("clazzId");
        String renewType = getRequestString("renewType");
        ChipsActiveServiceType serviceType = ChipsActiveServiceType.of(serviceTypeStr);

        try {
            WechatType wechatType = WechatType.CHIPS;
            WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
            initWechatConfigModel(model, wxConfig, wechatType);

            model.addAttribute("userName", obtainUserName(userId, false));
            MapMessage message = chipsActiveService.loadPageShareTitle(serviceTypeStr, userId, clazzId, renewType);
            model.addAttribute("sharePrimeTitle", message.get("sharePrimeTitle"));
            model.addAttribute("shareSubTitle", message.get("shareSubTitle"));
        } catch (Exception ex) {
            return redirectWithMsg("生成失败", model);
        }
        if (serviceType.equals(ChipsActiveServiceType.RENEWREMIND)) {
            String templateId = getRequestString("templateId");
            if (StringUtils.isBlank(templateId)) {//不是通用模板
                String redirect = renewPageAuth(serviceTypeStr, userId, clazzId, renewType);
                if (redirect != null) return redirect;
            }
            if ("v1".equals(renewType)) {
                return "/parent/chips/renewPreviewV1";
            } else {
                return "/parent/chips/renewPreviewV2";
            }
        }
        return "/parent/chips/otherServiceTypePreview";
    }

    @Nullable
    private String renewPageAuth(String serviceTypeStr, long userId, long clazzId, String renewType) {
        String openId = getOpenId();
        boolean redirect = Optional.ofNullable(openId)
                .filter(StringUtils::isNotBlank)
                .map(e -> {
                    ChipsWechatUser wechatUser = chipsWechatUserLoader.loadByOpenId(e, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
                    return wechatUser == null;
                }).orElse(true);
        if (redirect) {
            String param = buildOtherServiceTypePreviewRedirect(serviceTypeStr, userId, clazzId, renewType);
            String key = StringExtUntil.md5(param);
            persistenceCache(key, param);
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChips(AuthType.CHIPS_RENEW, key);
        }
        User user = currentChipsUser();
        if (user != null) {
            ChipsUserPageViewLog log = new ChipsUserPageViewLog();
            String uniqueKey = renewType + "-" + userId + "-" + clazzId;
            log.setId(ChipsUserPageViewLog.genId(user.getId(), uniqueKey));
            log.setUserId(user.getId());
            log.setUniqueKey(uniqueKey);
            log.setType(PageViewType.RENEW);
            log.setDisabled(false);
            chipsUserPageViewLogService.upsertChipsUserPageViewLog(log);
        }
        return null;
    }

    private String buildOtherServiceTypePreviewRedirect(String serviceType, long userId, long clazzId, String renewType) {
        return "serviceType=" + serviceType + "&userId=" + userId + "&clazzId=" + clazzId + "&renewType=" + renewType;
    }

    @RequestMapping(value = "activeService/queryOtherServiceTypeUserTemplate.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage queryOtherServiceTypeUserTemplate() {
        return chipsActiveService.loadPreviewTemplate(getRequestString("serviceType"), getRequestLong("userId")
                , getRequestString("templateId"), getRequestLong("clazzId"), getRequestString("renewType"));
    }

    private int getUnitIndex(String bookId, String unitId) {
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(unitId)) {
            return 0;
        }
        Map<String, StoneData> bookMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(bookId));
        List<StoneBookData.Node> unitList = Optional.of(bookMap).map(m -> m.get(bookId)).map(StoneBookData::newInstance).map(StoneBookData::getJsonData).map(StoneBookData.Book::getChildren).orElse(null);
        if (CollectionUtils.isEmpty(unitList)) {
            return 0;
        }
        for (int i = 0; i < unitList.size(); i++) {
            StoneBookData.Node unit = unitList.get(i);
            if (unit != null && StringUtils.isNotBlank(unit.getStone_data_id()) && unit.getStone_data_id().equals(unitId)) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * 获取用户姓名
     *
     * @param isReal true: 获取真实姓名  false：获取nickName
     * @return userName
     */
    private String obtainUserName(Long userId, boolean isReal) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return "孩子";
        }
        if (isReal) {
            return user.fetchRealname();
        } else {
            UserProfile userProfile = user.getProfile();
            return userProfile == null ? "孩子" : userProfile.getNickName() != null ? userProfile.getNickName() : "孩子";
        }
    }

    // 新广告页
    @RequestMapping(value = "/center/robin.vpage", method = RequestMethod.GET)
    public String robin(Model model) {
        MapMessage message;
        User user = currentChipsUser();
        if (user != null) {
            message = chipsOrderProductLoader.loadOnSaleShortLevelProductInfo(user.getId(), null, false);
        } else {
            message = chipsOrderProductLoader.loadOnSaleShortLevelProductInfo();
        }
        if (!message.isSuccess()) {
            return redirectWithMsg(message.getInfo(), model);
        }

        if (user != null && PaymentStatus.Paid.name().equalsIgnoreCase(SafeConverter.toString(message.get("status")))) {
            return "redirect:/chips/center/hope.vpage";
        }

        model.addAllAttributes(message);
        String inviter = getRequestString("inviter");
        model.addAttribute("inviter", inviter);
        boolean showBuy = true;
        model.addAttribute("showBuy", showBuy);
        return "/parent/chips/robin";
    }


    //
    @RequestMapping(value = "/short/product.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage shortProduct(Model model) {
        boolean primary = getRequestBool("primary");
        String type = getRequestString("type");
        return chipsOrderProductLoader.loadOnSaleShortLevelProductInfo(null, null, primary, type);
    }

    // 邀请页面
    @RequestMapping(value = "/center/invite.vpage", method = RequestMethod.GET)
    public String invite(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_INVITATION);
        }
        List<UserOrder> chipsOrder = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user.getId());
        if (CollectionUtils.isEmpty(chipsOrder)) {
            return "redirect:/chips/center/robin.vpage";
        }
        try {
            String authUrl = WechatConfig.getBaseSiteUrl() + "/chips/center/" + user.getId() + "/invition.vpage";
            model.addAttribute("url", authUrl);
            String nickName = Optional.ofNullable(parentLoaderClient.loadParentExtAttribute(user.getId()))
                    .filter(e -> StringUtils.isNotBlank(e.getWechatNick()))
                    .map(ParentExtAttribute::getWechatNick)
                    .orElse("");
            model.addAttribute("nickName", nickName);
            return "/parent/chips/invite";
        } catch (CannotAcquireLockException ex) {
            return redirectWithMsg("正在生成,请稍后", model);
        } catch (Exception ex) {
            logger.error("invite error. userid:{}", user.getId(), ex);
            return redirectWithMsg("系统异常，请稍后", model);
        }
    }

    //二维码生成
    @RequestMapping(value = "qrcode.vpage", method = RequestMethod.GET)
    public void captcha(HttpServletResponse resp) throws IOException {
        try (OutputStream out = resp.getOutputStream()) {
            String url = getRequestParameter("url", "");
            //解码
            url = url.contains("http://") || url.contains("https://") ? url : URLDecoder.decode(url, "UTF-8");
            //生成短连接
            String shortUrl = ShortUrlGenerator.generateShortUrl(url, true).orElse("");
            if (StringUtils.isNotBlank(shortUrl)) {
                url = ShortUrlGenerator.getShortUrlSiteUrl() + "/" + shortUrl;
            }
            String icon = getRequestString("icon");
            int color = getRequestInt("color");

            int imgWidth = getRequestInt("width", 300);
            int imgHeight = getRequestInt("height", 300);
            BitMatrix byteMatrix;
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            byteMatrix = new MultiFormatWriter().encode(
                    new String(url.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.QR_CODE,
                    imgWidth,
                    imgHeight,
                    hints);
            BufferedImage image = (color == 0 ? uploader.toBufferedImage(byteMatrix) : uploader.toBufferedImage(byteMatrix, color));

            if (StringUtils.isNotBlank(icon)) {
                BufferedImage logo = ImageIO.read(new URL(icon));
                Graphics2D graphics = image.createGraphics();
                graphics.drawImage(logo, imgWidth * 2 / 5, imgHeight * 2 / 5, imgWidth * 2 / 10, imgHeight * 2 / 10, null);
                graphics.dispose();
                logo.flush();
            }

            ImageIO.write(image, "png", out);
        } catch (Exception e) {
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getOutputStream().write("生成二维码内容失败!".getBytes("utf-8"));
        }
    }

    // 邀请路由
    @RequestMapping(value = "/center/{user}/invition.vpage", method = RequestMethod.GET)
    public String invition(@PathVariable("user") String user) {
        String userId = StringUtils.isNumeric(user) ? user : "";
        //response.sendRedirect("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxeb7629934ebf8482&redirect_uri=https%3A%2F%2Fwechat.test.17zuoye.net%2fchips_open.vpage&response_type=code&scope=snsapi_base&state=chips_center264449#wechat_redirect");
        return "redirect:/chips/center/robin.vpage?inviter=" + userId;
    }


    @Deprecated
    // 支付成功
    @RequestMapping(value = "/center/paymentsuccess.vpage", method = RequestMethod.GET)
    public String paymentsuccess(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }
        Long clazzId = aiLoaderClient.getRemoteReference().loadMyVirtualClazz(user.getId());
        model.addAttribute("clazz", clazzId);
        return "/parent/chips/paymentsuccess";
    }

    //购买页
    @RequestMapping(value = "/center/reservepay.vpage", method = RequestMethod.GET)
    public String reservePay(Model model) {
        try {
            String productId = getRequestString("productId");
            if (StringUtils.isBlank(productId)) {
                return redirectWithMsg("活动不存在", model);
            }

            OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
            if (product == null || product.isDisabledTrue() || !product.isOnline()
                    || OrderProductServiceType.safeParse(product.getProductType()) != OrderProductServiceType.ChipsEnglish) {
                return redirectWithMsg("活动不存在或已经下线", model);
            }
            User user = currentChipsUser();
            long inviter = getRequestLong("inviter");
            if (user == null || !user.isParent() || StringUtils.isBlank(getOpenId())) {
                int grade = Optional.of(product).filter(e -> StringUtils.isNotBlank(e.getAttributes()))
                        .map(e -> JsonUtils.fromJson(e.getAttributes()))
                        .filter(MapUtils::isNotEmpty)
                        .map(e -> SafeConverter.toInt(e.get("grade")))
                        .orElse(0);
                return "redirect:" + OAuthUrlGenerator.generatorForChips(grade != 0 ? (AuthType.CHIPS_OFFICIAL_PRODUCT_AD.getType() + inviter) : (AuthType.CHIPS_CENTER.getType() + inviter));
            }

            AppPayMapper appPayMapper = userOrderLoaderClient.getUserAppPaidStatus(product.getProductType(), user.getId(), true);
            List<OrderProductItem> orderProductItem = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
            if (CollectionUtils.isEmpty(orderProductItem)) {
                return redirectWithMsg("活动不存在或已经下线", model);
            }

            if (appPayMapper != null && CollectionUtils.isNotEmpty(appPayMapper.getValidItems()) &&
                    orderProductItem.stream().filter(e -> appPayMapper.getValidItems().contains(e.getId())).findFirst().orElse(null) != null) {
                return "redirect:/chips/center/hope.vpage";
            }

            List<UserOrder> userOrderList = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user.getId());
            if (CollectionUtils.isNotEmpty(userOrderList)) {
                Map<String, OrderProduct> paidOrderProduct = userOrderLoaderClient.loadOrderProducts(userOrderList.stream().map(UserOrder::getProductId).collect(Collectors.toList()));
                Date now = new Date();
                if (MapUtils.isNotEmpty(paidOrderProduct)) {
                    for (Map.Entry<String, OrderProduct> entry : paidOrderProduct.entrySet()) {
                        String attr = entry.getValue().getAttributes();
                        if (StringUtils.isBlank(attr)) {
                            continue;
                        }
                        Map<String, Object> attrMap = JsonUtils.fromJson(attr);
                        if (MapUtils.isEmpty(attrMap)) {
                            continue;
                        }
                        Date beginDate = SafeConverter.toDate(attrMap.get("beginDate"));
                        Date endDate = SafeConverter.toDate(attrMap.get("endDate"));
                        if (beginDate == null || endDate == null) {
                            continue;
                        }

                        if (now.before(beginDate) || DateUtils.addDays(now, 2).before(endDate)) {
                            return "redirect:/chips/center/hope.vpage";
                        }
                    }
                }
            }

            String extStr = null;
            if (Long.compare(inviter, 0L) > 0 && Long.compare(inviter, user.getId()) == 0) {
                return redirectWithMsg("不能接受自己的邀请哦", model);
            } else if (Long.compare(inviter, 0L) > 0) {
                Map<String, Object> extMap = new HashMap<>();
                extMap.put("inviter", inviter);
                extStr = JsonUtils.toJson(extMap);
            }

            String refer = getRequestString("refer");
            if (StringUtils.isBlank(refer)) {
                refer = "330223";
            }
            UserOrder order = UserOrder.newOrder(OrderType.chips_english, user.getId());
            order.setUserId(user.getId());
            order.setProductAttributes(product.getAttributes());
            order.setOrderPrice(product.getPrice());
            order.setProductId(product.getId());
            order.setProductName(product.getName());
            order.setOrderProductServiceType(product.getProductType());
            order.setOrderReferer(refer);//薯条英语公众号
            order.setUserReferer(SafeConverter.toString(user.getId()));
            order.setExtAttributes(extStr);
            MapMessage message = userOrderServiceClient.saveUserOrder(order);
            if (message.isSuccess()) {
//                return "redirect:/parent/wxpay/pay-order.vpage?oid=" + order.genUserOrderId();
                try {
                    if (isPayForTest()) {
                        model.addAttribute("oid", order.genUserOrderId());
                    } else {
                        PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(PaymentConstants.PaymentGatewayName_Wechat_Chips);
                        PaymentRequest paymentRequest = new PaymentRequest();
                        paymentRequest.setTradeNumber(order.genUserOrderId());
                        paymentRequest.setProductName(order.getProductName());
                        paymentRequest.setPayMethod(PaymentConstants.PaymentGatewayName_Wechat_Chips);
                        paymentRequest.setSpbillCreateIp(getRequestContext().getRealRemoteAddress());
                        BigDecimal amount = userOrderServiceClient.getOrderCouponDiscountPrice(order);
                        if (PaymentGateway.getUsersForPaymentTest(order.getUserId()) || ChipsTestUser.contains(order.getUserId())) {
                            amount = new BigDecimal(0.01);
                        }
                        paymentRequest.setPayAmount(amount);
                        paymentRequest.setPayUser(user.getId());
                        paymentRequest.setCallbackBaseUrl(ProductConfig.getMainSiteBaseUrl() + "/payment/notify/order");
                        paymentRequest.setOpenid(getOpenId());

                        PaymentRequestForm paymentRequestForm = paymentGateway.getPaymentRequestForm(paymentRequest);
                        if (MapUtils.isNotEmpty(paymentRequestForm.getFormFields())) {
                            Map<String, Object> payMap = paymentRequestForm.getFormFields();
                            String returnUrl = "/chips/center/paymentsuccess.vpage";
                            WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(WechatType.CHIPS));
                            initChipsPayModel(model,
                                    wxConfig,
                                    WechatType.CHIPS,
                                    SafeConverter.toLong(payMap.get("timeStamp")),
                                    SafeConverter.toString(payMap.get("nonceStr")),
                                    SafeConverter.toString(payMap.get("package")),
                                    SafeConverter.toString(payMap.get("sign")),
                                    returnUrl);
                        } else {
                            return redirectWithMsg("调用第三方支付失败", model);
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Chips wechat pay order failed. orderId:{}",
                            order.genUserOrderId(), ex);
                    return redirectWithMsg("调用第三方支付失败", model);
                }
            } else {
                return redirectWithMsg(message.getInfo(), model);
            }
        } catch (Exception ex) {
            logger.error("reservePay order fail, error is {}", ex.getMessage());
            return redirectWithMsg("生成订单失败", model);
        }

        return "/parent/wxpay/pay";
    }

    // 我的老师
    @RequestMapping(value = "/center/myteacher.vpage", method = RequestMethod.GET)
    public String myteacher(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }
        ChipsEnglishClass chipsEnglishClass = chipsEnglishClazzService.loadMyDefaultClass(user.getId());
        String wxCode = Optional.ofNullable(chipsEnglishClass)
                .map(ChipsEnglishClass::getTeacherInfo)
                .map(ChipsEnglishTeacher::getWxCode)
                .orElse("");
        String qrCode = Optional.ofNullable(chipsEnglishClass)
                .map(ChipsEnglishClass::getTeacherInfo)
                .map(ChipsEnglishTeacher::getQrImage)
                .orElse("");
        String companyQrImage = Optional.ofNullable(chipsEnglishClass)
                .map(ChipsEnglishClass::getTeacherInfo)
                .map(ChipsEnglishTeacher::getCompanyQrImage)
                .orElse("");
        model.addAttribute("wxCode", wxCode);
        model.addAttribute("qrCode", qrCode);
        model.addAttribute("companyQrCode", companyQrImage);
        return "/parent/chips/myteacher";
    }

    // 我的老师V2--模板消息用的
    @RequestMapping(value = "/center/myteacherV2.vpage", method = RequestMethod.GET)
    public String myteacherV2(Model model) {
        User user = currentChipsUser();
        String productId = getRequestString("product");
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }

        Optional<ChipsEnglishTeacher> chipsEnglishTeacherOptional = Optional.ofNullable(productId)
                .map(e -> chipsEnglishClazzService.loadClazzIdByUserAndProduct(user.getId(), e))
                .map(ChipsEnglishClass::getTeacherInfo);
        String wxCode = chipsEnglishTeacherOptional.map(ChipsEnglishTeacher::getWxCode).orElse(null);
        String qrCode = chipsEnglishTeacherOptional.map(ChipsEnglishTeacher::getQrImage).orElse(null);
        String companyQRImage = chipsEnglishTeacherOptional.map(ChipsEnglishTeacher::getCompanyQrImage).orElse(null);
        model.addAttribute("wxCode", wxCode);
        model.addAttribute("qrCode", qrCode);
        model.addAttribute("companyQrCode", companyQRImage);
        return "/parent/chips/myteacher";
    }

    // 我的证书
    @RequestMapping(value = "/center/getcertificate.vpage", method = RequestMethod.GET)
    public String getcitificate(Model model) {
        String userName = getRequestString("user");
        model.addAttribute("userName", userName);
        return "/parent/chips/getcertificate";
    }

    // 学习列表
    @RequestMapping(value = "/center/studylist.vpage", method = RequestMethod.GET)
    public String studylist(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/studylist";
    }

    // 跟读单词列表（热身）
    @RequestMapping(value = "/center/followwordlist.vpage", method = RequestMethod.GET)
    public String followwordlist(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/followwordlist";
    }

    // 跟读单词（热身）
    @RequestMapping(value = "/center/followword.vpage", method = RequestMethod.GET)
    public String followword(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/followword";
    }

    // 跟读句子列表（热身）
    @RequestMapping(value = "/center/followsentencelist.vpage", method = RequestMethod.GET)
    public String followsentencelist(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/followsentencelist";
    }

    // 跟读句子列表（热身）
    @RequestMapping(value = "/center/followsentence.vpage", method = RequestMethod.GET)
    public String followsentence(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/followsentence";
    }

    // 情景对话目标
    @RequestMapping(value = "/center/sceneintro.vpage", method = RequestMethod.GET)
    public String sceneintro(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/sceneintro";
    }


    // 任务对话目标
    @RequestMapping(value = "/center/taskintro.vpage", method = RequestMethod.GET)
    public String taskintro(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/taskintro";
    }

    // 对话实录
    @RequestMapping(value = "/center/dialogue.vpage", method = RequestMethod.GET)
    public String dialogue(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/dialogue";
    }

    // 内推页面
    @RequestMapping(value = "/center/internalrecommend.vpage", method = RequestMethod.GET)
    public String internalrecommend(Model model) {
        if (currentChipsUser() == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }
        return "/parent/chips/internalrecommend";
    }

    // 我的推荐
    @RequestMapping(value = "/center/recommend.vpage", method = RequestMethod.GET)
    public String recommend(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }
        MapMessage mapMessage = aiLoaderClient.getRemoteReference().loadInvitationInfoByUserId(user.getId());
        if (mapMessage.isSuccess()) {
            model.addAllAttributes(mapMessage);
        }
        return "/parent/chips/recommend";
    }

    // 我的优惠券
    @RequestMapping(value = "/center/coupon.vpage", method = RequestMethod.GET)
    public String coupon(Model model) {
        model.addAttribute("exchangeDate", "2018-08-15");
        return "/parent/chips/coupon";
    }

    // 我的电子教材目录
    @RequestMapping(value = "/center/bookcatalog.vpage", method = RequestMethod.GET)
    public String bookcatalog(Model model) {
        return "/parent/chips/bookcatalog";
    }

    // 我的电子教材详情
    @RequestMapping(value = "/center/bookdrama.vpage", method = RequestMethod.GET)
    public String bookdrama(Model model) {
        return "/parent/chips/bookdrama";
    }


    // 总结
    @RequestMapping(value = "/center/summary.vpage", method = RequestMethod.GET)
    public String summary(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/summary";
    }

    // 跳转到今日学习
    @RequestMapping(value = "/center/tostudy.vpage", method = RequestMethod.GET)
    public String tostudy(Model model) {
        return "/parent/chips/tostudy";
    }

    // 定级报告
    @RequestMapping(value = "/center/report.vpage", method = RequestMethod.GET)
    public String report(Model model) {
        if (currentChipsUser() == null) {
            return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_STUDY_FINAL_REPORT);
        }
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/report";
    }

    // 定级报告V2 不需要用户登录,用户id在链接中传入
    @RequestMapping(value = "/center/reportV2.vpage", method = RequestMethod.GET)
    public String reportV2(Model model) {
        long userId = getRequestLong("userId");
        String bookId = getRequestString("bookId");
        String redirect = reportV2Auth(userId, bookId);
        if (redirect != null) return redirect;
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
//        model.addAttribute("sharePrimeTitle", obtainUserName(getRequestLong("userId"), false)+ "同学的定级报告");

//        model.addAttribute("shareSubTitle", "学习薄弱环节出现在｛薄弱点｝方面，建议学习｛级别｝级别");
        MapMessage message = chipsActiveService.loadGradeReport(userId, bookId);
        model.addAttribute("sharePrimeTitle", message.get("sharePrimeTitle"));

        model.addAttribute("shareSubTitle", message.get("shareSubTitle"));
        return "/parent/chips/reportV2";
    }

    @Nullable
    private String reportV2Auth(long userId, String bookId) {
        String openId = getOpenId();
        boolean redirect = Optional.ofNullable(openId)
                .filter(StringUtils::isNotBlank)
                .map(e -> {
                    ChipsWechatUser wechatUser = chipsWechatUserLoader.loadByOpenId(e, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
                    return wechatUser == null;
                }).orElse(true);
        if (redirect) {
            String param = "userId=" + userId + "&bookId=" + bookId;
            String key = StringExtUntil.md5(param);
            persistenceCache(key, param);
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChips(AuthType.CHIPS_REPORT_V2, key);
        }
        User user = currentChipsUser();
        if (user != null) {
            ChipsUserPageViewLog log = new ChipsUserPageViewLog();
            String uniqueKey = bookId + "-" + userId;
            log.setId(ChipsUserPageViewLog.genId(user.getId(), uniqueKey));
            log.setUserId(user.getId());
            log.setUniqueKey(uniqueKey);
            log.setType(PageViewType.REPORT);
            log.setDisabled(false);
            chipsUserPageViewLogService.upsertChipsUserPageViewLog(log);
        }
        return null;
    }

    // 学习方案
    @RequestMapping(value = "/center/planmethod.vpage", method = RequestMethod.GET)
    public String planmethod(Model model) {
        if (currentChipsUser() == null) {
            return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_STUDY_SUMMARY);
        }
        model.addAttribute("id", getRequestString("id"));
        model.addAttribute("book", getRequestString("book"));
        return "/parent/chips/planmethod";
    }

    // 邮寄地址问卷调查
    @RequestMapping(value = "/center/emailQuestionnaire.vpage", method = RequestMethod.GET)
    public String emailQuestionnaire(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorForChips(AuthType.CHIPS_UGC_MAIL.getType());
        }
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/emailQuestionnaire";
    }

    // 排行榜
    @RequestMapping(value = "/center/ranking.vpage", method = RequestMethod.GET)
    public String ranking(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/ranking";
    }

    // 神秘奖励
    @RequestMapping(value = "/center/reward.vpage", method = RequestMethod.GET)
    public String reward(Model model) {
        if (currentChipsUser() == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }
        String id = getRequestString("id");
        if (StringUtils.isNotBlank(id)) {
            PageBlockContent page = pageBlockContentServiceClient.getPageBlockContentBuffer().findByPageName("chipsEnglish")
                    .stream()
                    .filter(e -> e.getDisabled() == null || !e.getDisabled())
                    .filter(p -> StringUtils.isNotBlank(p.getBlockName()) && p.getBlockName().contains(id))
                    .findFirst().orElse(null);
            model.addAttribute("content", page != null ? page.getContent() : "");
        }

        return "/parent/chips/reward";
    }

    // 课程完成列表
    @RequestMapping(value = "/center/travelcatalog.vpage", method = RequestMethod.GET)
    public String travelcatalog(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorAuthUrlForChips(AuthType.CHIPS_STUDY_LIST);
        }
        String book = getRequestParameter("book", "BKC_10300227052789");
        String pname = getRequestParameter("pname", "薯条英语");
        MapMessage mapMessage = chipsEnglishContentLoader.loadUnitMapByBook(user.getId(), book);
        if (!mapMessage.isSuccess()) {
            model.addAttribute("mapList", Collections.emptyList());
        } else {
            model.addAllAttributes(mapMessage);
        }
        model.addAttribute("pname", pname);
        return "/parent/chips/travelcatalog";
    }

    // 已经购买的页面
    @RequestMapping(value = "/center/hope.vpage", method = RequestMethod.GET)
    public String hope(Model model) {
        return "/parent/chips/hope";
    }


    // 薯条英语视频分享
    @RequestMapping(value = "/center/chipsshare.vpage", method = RequestMethod.GET)
    public String chipsshare(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);

        String id = getRequestString("id");
        AIUserVideo aiUserVideo = aiLoaderClient.getRemoteReference().loadUserVideoById(id);
        if (aiUserVideo != null) {
            NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(aiUserVideo.getUnitId());
            String unitImage = Optional.ofNullable(newBookCatalog)
                    .filter(e -> MapUtils.isNotEmpty(e.getExtras()))
                    .map(NewBookCatalog::getExtras)
                    .map(e -> e.get("ai_teacher"))
                    .map(e -> JsonUtils.fromJson(SafeConverter.toString(e)))
                    .map(e -> SafeConverter.toString(e.get("cardImgUrl"), "")).orElse("");
            model.addAttribute("unitImage", unitImage);
            String unitName = Optional.ofNullable(newBookCatalog)
                    .filter(e -> MapUtils.isNotEmpty(e.getExtras()))
                    .map(NewBookCatalog::getExtras)
                    .map(e -> e.get("ai_teacher"))
                    .map(e -> JsonUtils.fromJson(SafeConverter.toString(e)))
                    .map(e -> SafeConverter.toString(e.get("cardTitle"), "")).orElse("");
            model.addAttribute("unitName", unitName);
            model.addAttribute("commentAudio", aiUserVideo.getCommentAudio());
            model.addAttribute("url", aiUserVideo.getVideo());
            model.addAttribute("userName", aiUserVideo.getUserName());
            model.addAttribute("dateTime", DateUtils.dateToString(aiUserVideo.getCreateTime()));
            model.addAttribute("comment", aiUserVideo.getComment());
            model.addAttribute("labels", CollectionUtils.isNotEmpty(aiUserVideo.getLabels()) ? aiUserVideo.getLabels() : Collections.emptyList());

            String title = "薯条英语" + DateUtils.dateToString(aiUserVideo.getCreateTime(), "MM月dd日");
            int rank = Optional.ofNullable(chipsEnglishClazzService.loadClazzIdByUserAndUnit(aiUserVideo.getUserId(), aiUserVideo.getUnitId()))
                    .map(e -> UserShareVideoRankCache.load(e + "", aiUserVideo.getUnitId()))
                    .map(e -> e.stream().filter(e1 -> e1.getUserId().equals(aiUserVideo.getUserId())).findFirst().orElse(null))
                    .map(ChipsRank::getRank)
                    .orElse(0);
            if (rank > 0) {
                title = title + "第" + rank + "名";
            }
            model.addAttribute("title", title);
        }
        return "/parent/chips/chipsshare";
    }


    // 优惠券数据
    @RequestMapping(value = "/center/mycoupon.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage myCoupon() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("no login");
        }

        Map<CouponUserStatus, List<CouponShowMapper>> resMap = couponLoaderClient.loadUserCoupons(user.getId()).stream()
                .filter(e -> ChipsInvitationHelper.getCouponId().equals(e.getCouponId()))
                .collect(Collectors.groupingBy(CouponShowMapper::getCouponUserStatus));

        return MapMessage.successMessage()
                .add(CouponUserStatus.NotUsed.name(), MapUtils.isNotEmpty(resMap) && CollectionUtils.isNotEmpty(resMap.get(CouponUserStatus.NotUsed)) ? resMap.get(CouponUserStatus.NotUsed) : Collections.emptyList())
                .add(CouponUserStatus.Used.name(), MapUtils.isNotEmpty(resMap) && CollectionUtils.isNotEmpty(resMap.get(CouponUserStatus.Used)) ? resMap.get(CouponUserStatus.Used) : Collections.emptyList())
                .add(CouponUserStatus.Expired.name(), MapUtils.isNotEmpty(resMap) && CollectionUtils.isNotEmpty(resMap.get(CouponUserStatus.Expired)) ? resMap.get(CouponUserStatus.Expired) : Collections.emptyList());
    }

    //邀请排行榜
    @RequestMapping(value = "/invitation/rank/load.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage loadRedpackRank() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.successMessage().add("rankList", Collections.emptyList()).add("showPlay", false);
        }

        // 1、现在短期课电子教材是对所有用户开放的，需要关闭掉未开课用户。
        // 2、现在可以得到电子教材的是三种用户1.推荐好友报名的用户；2已经完课的用户，3旅行口语二期的用户(此条件不用判断)。
        boolean showPlay;
        ChipEnglishInvitation chipEnglishInvitation = aiLoaderClient.getRemoteReference().loadInvitationByInviterId(user.getId()).stream().findFirst().orElse(null);
        if (chipEnglishInvitation != null) {
            showPlay = true;
        } else {
            showPlay = aiLoaderClient.getRemoteReference().ifAllUnitFinished(user.getId(), BooKConst.CHIPS_ENGLISH_BOOK_ID);
        }

        List<ChipsRank> ranks = UserInvitationRankCache.load();

        if (CollectionUtils.isEmpty(ranks)) {
//            return MapMessage.successMessage().add("rankList", Collections.emptyList()).add("showPlay", CollectionUtils.isNotEmpty(invitations));
            return MapMessage.successMessage().add("rankList", Collections.emptyList()).add("showPlay", true);
        }
        List<UserOrder> chipsOrder = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user.getId());

        return MapMessage.successMessage().add("rankList", ranks)
                .add("showPlay", showPlay)
                .add("paid", CollectionUtils.isNotEmpty(chipsOrder));
    }

    //我的邀请
    @RequestMapping(value = "/invitation/my.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage myRedpack() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("没有登录");
        }
        return aiLoaderClient.getRemoteReference().loadInvitationInfoByUserId(user.getId());
    }

    //我的优惠券提现
    @RequestMapping(value = "/coupon/redpack.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage myRedpack(@RequestParam(name = "couponIds", required = false) String[] couponIds) {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("没有登录");
        }

        if (couponIds == null) {
            return MapMessage.errorMessage("参数错误");
        }
        if (couponIds.length < 10) {
            return MapMessage.errorMessage("优惠券的数量少于10张");
        }

        return MapMessage.errorMessage("暂时未开放优惠券提现功能");
    }

    //课程列表
    @RequestMapping(value = "/lesson/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage lessonList() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("没有登录");
        }

        // 1、现在短期课电子教材是对所有用户开放的，需要关闭掉未开课用户。
        // 2、现在可以得到电子教材的是三种用户1.推荐好友报名的用户；2已经完课的用户，3旅行口语二期的用户(此条件不用判断)。
        boolean showPlay;
        ChipEnglishInvitation chipEnglishInvitation = aiLoaderClient.getRemoteReference().loadInvitationByInviterId(user.getId()).stream().findFirst().orElse(null);
        if (chipEnglishInvitation != null) {
            showPlay = true;
        } else {
            showPlay = aiLoaderClient.getRemoteReference().ifAllUnitFinished(user.getId(), BooKConst.CHIPS_ENGLISH_BOOK_ID);
        }

        if (!showPlay) {
            return MapMessage.errorMessage("没有权限");
        }

        return aiLoaderClient.getRemoteReference().loadCourseList();
    }

    //剧本
    @RequestMapping(value = "/lesson/play.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage lessonPlayInfo() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("没有登录");
        }

        String unitId = getRequestString("id");
        if (StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("参数异常");
        }

        // 1、现在短期课电子教材是对所有用户开放的，需要关闭掉未开课用户。
        // 2、现在可以得到电子教材的是三种用户1.推荐好友报名的用户；2已经完课的用户，3旅行口语二期的用户(此条件不用判断)。
        List<ChipEnglishInvitation> chipEnglishInvitations = aiLoaderClient.getRemoteReference().loadInvitationByInviterId(user.getId());
        if (CollectionUtils.isEmpty(chipEnglishInvitations)) {
            NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
            boolean ifAllUnitFinished = Optional.ofNullable(unit)
                    .map(u -> aiLoaderClient.getRemoteReference().ifAllUnitFinished(user.getId(), u.bookId()))
                    .orElse(false);
            if (!ifAllUnitFinished) {
                return MapMessage.errorMessage("没有权限");
            }
        }
        return aiLoaderClient.getRemoteReference().loadLessonPlay(unitId);
    }

    // 每日的排行榜
    @RequestMapping(value = "daily/rank/load.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage dailyRank() {
//        String clazz = getRequestString("clazz");
        Long clazz = getRequestLong("clazz");
        String unit = getRequestString("id");
        MapMessage mapMessage = MapMessage.successMessage();
        if (StringUtils.isBlank(unit) || clazz == 0L) {
            mapMessage.add("hotVideoRank", Collections.emptyList())
                    .add("scoreRank", Collections.emptyList());
        } else {
            List<ChipsRank> videoRanks = UserShareVideoRankCache.load(clazz + "", unit);
            List<ChipsRank> scoreRanks = UserScoreRankCache.load(clazz + "", unit);
            mapMessage.add("hotVideoRank", CollectionUtils.isNotEmpty(videoRanks) ? videoRanks : Collections.emptyList())
                    .add("scoreRank", CollectionUtils.isNotEmpty(scoreRanks) ? scoreRanks : Collections.emptyList());
        }
        String name = Optional.ofNullable(chipsEnglishClazzService.selectChipsEnglishClassById(clazz)).map(ChipsEnglishClass::getName).orElse("");
        String date = DateUtils.dateToString(new Date(), "MM月dd日");

        return mapMessage.add("date", date).add("name", name);
    }

    // 学习方案
    @RequestMapping(value = "daily/study/summary.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage dailySummry() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("没有登录");
        }
        String unit = getRequestString("unitId");
        String book = getRequestString("bookId");
        if (StringUtils.isAnyBlank(unit, book)) {
            return MapMessage.errorMessage("参数为空");
        }

        return chipsEnglishContentLoader.loadCourseStudyPlanInfo(user.getId(), unit, book);
    }

    // 定级报告
    @RequestMapping(value = "finish/summary.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage finishSummary() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("没有登录");
        }
        String book = getRequestString("book");
        return chipsEnglishContentLoader.loadBookResultInfo(user.getId(), book);
    }

    // 定级报告,不需要用户登录,用户id在链接中传入
    @RequestMapping(value = "finish/summaryV2.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage finishSummaryV2() {
        long userId = getRequestLong("userId");
        if (userId == 0l) {
            return MapMessage.errorMessage("没有用户：" + userId);
        }
        String bookId = getRequestString("bookId");
        return chipsEnglishContentLoader.loadBookResultInfo(userId, bookId);
    }

    //正价课的产品
    @RequestMapping(value = "/center/officialproducts.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage officialProducts() {
        List<OrderProduct> products = userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream()
                .filter(OrderProduct::isOnline)
                .filter(e -> OrderProductServiceType.ChipsEnglish == OrderProductServiceType.safeParse(e.getProductType()))
                .filter(e -> {
                    if (StringUtils.isBlank(e.getAttributes())) {
                        return false;
                    }
                    Map<String, Object> map = JsonUtils.fromJson(e.getAttributes());
                    if (MapUtils.isEmpty(map)) {
                        return false;
                    }
                    return map.get("grade") != null && SafeConverter.toInt(map.get("grade")) > 0;
                }).collect(Collectors.toList());
        int gradeOneLimit = Optional.ofNullable(aiChipsEnglishConfigServiceClient.loadChipsEnglishConfigByName("chipsOfficialProductLimitGradeOne"))
                .filter(e -> e.getDisabled() == null || !e.getDisabled())
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(StringUtils::isNotBlank)
                .map(e -> SafeConverter.toInt(e, 50))
                .orElse(50);
        int gradeTwoLimit = Optional.ofNullable(aiChipsEnglishConfigServiceClient.loadChipsEnglishConfigByName("chipsOfficialProductLimitGradeTwo"))
                .filter(e -> e.getDisabled() == null || !e.getDisabled())
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(StringUtils::isNotBlank)
                .map(e -> SafeConverter.toInt(e, 50))
                .orElse(50);
        int gradeThreeLimit = Optional.ofNullable(aiChipsEnglishConfigServiceClient.loadChipsEnglishConfigByName("chipsOfficialProductLimitGradeThree"))
                .filter(e -> e.getDisabled() == null || !e.getDisabled())
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(StringUtils::isNotBlank)
                .map(e -> SafeConverter.toInt(e, 50))
                .orElse(50);
        int remaining = 0;
        Map<String, Object> grade1Map = getProduct(1, products, gradeOneLimit);
        Map<String, Object> grade2Map = getProduct(2, products, gradeTwoLimit);
        Map<String, Object> grade3Map = getProduct(3, products, gradeThreeLimit);
        List<GradeRemaining> list = new ArrayList<>();
        list.add(new GradeRemaining(1, SafeConverter.toInt(grade1Map.get("remaining"))));
        list.add(new GradeRemaining(2, SafeConverter.toInt(grade2Map.get("remaining"))));
        list.add(new GradeRemaining(3, SafeConverter.toInt(grade3Map.get("remaining"))));
        list = list.stream().filter(e -> e.getRemaining().compareTo(0) > 0)
                .sorted(Comparator.comparing(GradeRemaining::getRemaining)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(list)) {
            remaining = list.get(0).getRemaining();
        }

        return MapMessage.successMessage()
                .add("remaining", remaining)
                .add("endDate", "7月20日")
                .add("grade1", grade1Map)
                .add("grade2", grade2Map)
                .add("grade3", grade3Map);
    }

    // 今日学习
    @RequestMapping(value = "dailyLesson/messageReport.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage dailyLessonMessageReport() {
        Long clazzId = getRequestLong("clazzId");
        String unitId = getRequestString("unitId");
        String bookId = getRequestString("bookId");
        if (StringUtils.isBlank(bookId)) {
            bookId = BooKConst.CHIPS_ENGLISH_BOOK_ID;
        }
        if (StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("没有输入unitId");
        }
        if (clazzId == 0L) {//预览
            Map<String, Object> dataMap = dailyLessonService.buildDataMapForPreview(bookId, unitId);
            if (MapUtils.isEmpty(dataMap)) {
                return MapMessage.errorMessage("没有取到录入的该unit(" + unitId + ")的信息.");
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("data", dataMap);
            return mapMessage;
        }
        Map<String, Object> dataMap = dailyLessonService.buildDataMap(bookId, clazzId, unitId);
        if (MapUtils.isEmpty(dataMap)) {
            return MapMessage.errorMessage("没有取到录入的该unit(" + unitId + ")的信息.");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("data", dataMap);
        return mapMessage;
    }

    // 我的打卡记录
    @RequestMapping(value = "/center/mysharerecord.vpage", method = RequestMethod.GET)
    public String myShareRecord(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorLoginCenterUrlForChips();
        }
        String bookId = getRequestString("bookId");
        model.addAttribute("bookId", bookId);
        return "/parent/chips/mysharerecord";
    }

    // 我的打卡记录 获取数据
    @RequestMapping(value = "/center/mysharerecorddata.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage myShareRecordData(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        String bookId = getRequestString("bookId");
        Map<String, Object> data = aiLoaderClient.getRemoteReference().loadUserShareRecords(user.getId(), bookId);
        return MapMessage.successMessage().add("data", data);
    }

    // 拼团列表接口
    @RequestMapping(value = "/group/shopping/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupShoppingList() {
        return chipsOrderLoader.loadGroupShoppingList();
    }

    // 拼团发起人信息接口
    @RequestMapping(value = "/group/sponsor.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupSponsorDetail() {
        String groupCode = getRequestString("groupCode");
        if (StringUtils.isBlank(groupCode)) {
            return MapMessage.errorMessage("拼团码为空");
        }
        return wrapper(message -> message.putAll(chipsOrderLoader.loadGroupSponsorInfo(groupCode)));
    }


    private Map<String, Object> getProduct(Integer grade, List<OrderProduct> products, int limit) {
        if (CollectionUtils.isEmpty(products)) {
            return Collections.emptyMap();
        }
        Map<String, Object> resMap = new HashMap<>();
        Collection<Long> buyUsers = aiLoaderClient.getRemoteReference().loadOfficialProductUser(grade);
        if (CollectionUtils.isNotEmpty(buyUsers)) {
            resMap.put("remaining", Math.max(0, limit - buyUsers.size()));
        } else {
            resMap.put("remaining", limit);
        }
        List<Map<String, Object>> res = new ArrayList<>();
        products.stream().filter(e -> {
            Map<String, Object> map = JsonUtils.fromJson(e.getAttributes());
            if (MapUtils.isEmpty(map)) {
                return false;
            }
            return SafeConverter.toInt(map.get("grade")) == grade;
        }).forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", e.getId());
            map.put("productName", e.getName());
            map.put("price", e.getPrice());
            map.put("originalPrice", e.getOriginalPrice());
            res.add(map);
        });
        resMap.put("products", res);
        return resMap;
    }

    // 获取问卷
    @RequestMapping(value = "/ugc/collect.vpage", method = RequestMethod.GET)
    public String collect() {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorForChips(AuthType.CHIPS_UGC.getType());
        }
        return "/parent/chips/ugc_collect";
    }

    // 提交问卷
    @RequestMapping(value = "/ugc/submit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ugcSubmit() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        String grade = getRequestString("grade");
        String studyDuration = getRequestString("studyDuration");
        String expect = getRequestString("expect");
        String weekPoints = getRequestString("weekPoints");
        String otherExtraRegistration = getRequestString("otherExtraRegistration");
        String recentlyScore = getRequestString("recentlyScore");
        Integer serviceScore = getRequestInt("serviceScore");

        ChipsEnglishUserExtSplit extSplit = chipsEnglishClazzService.selectChipsEnglishUserExtSplitByUserId(user.getId());
        if (extSplit == null) {
            extSplit = new ChipsEnglishUserExtSplit();
            extSplit.setId(user.getId());
            extSplit.setCreateTime(new Date());
            extSplit.setUpdateTime(new Date());
            extSplit.setGrade(grade);
            extSplit.setStudyDuration(studyDuration);
            extSplit.setExpect(expect);
            extSplit.setWeekPoints(weekPoints);
            extSplit.setOtherExtraRegistration(otherExtraRegistration);
            extSplit.setRecentlyScore(recentlyScore);
            extSplit.setServiceScore(serviceScore);
        } else {
            extSplit.setUpdateTime(new Date());
            extSplit.setGrade(grade);
            extSplit.setStudyDuration(studyDuration);
            extSplit.setExpect(expect);
            extSplit.setWeekPoints(weekPoints);
            extSplit.setOtherExtraRegistration(otherExtraRegistration);
            extSplit.setRecentlyScore(recentlyScore);
            extSplit.setServiceScore(serviceScore);
        }
        try {
            return AtomicLockManager.instance().wrapAtomic(chipsEnglishClazzService).keyPrefix("UGC_COLLECT").keys(user.getId())
                    .proxy().upsertChipsEnglishUserExtSplit(extSplit);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    /**
     * 轻运营班主任后台，主动服务模板
     *
     * @return
     */
    @RequestMapping(value = "activeService/template.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage activeServiceTemplate() {
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        return wrapper(mm -> {

            AIActiveServiceTemplate template = aiTodayLessonClient.getRemoteReference().loadAIActiveServiceTemplateByBookIdUnitId(bookId, unitId);
            if (template == null) {
                mm.setSuccess(false).setInfo("记录不存在");
            } else {
                String json = template.getJson();
                Map<String, Object> jsonMap = JsonUtils.fromJson(json);
                jsonMap.put("unitId", template.getUnitId());
                jsonMap.put("title", template.getTitle());
                mm.add("data", jsonMap);
            }
        });
    }

    @RequestMapping(value = "activeService/questionTemplate.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage activeServiceQuestionTemplate() {
        String qid = getRequestString("qid");
        Long userId = getRequestLong("userId");
        String bookId = getRequestString("bookId");
        return wrapper(mm -> {
            AiChipsEnglishTeacher teacher = chipsEnglishClazzService.loadTeacherByUserIdAndBookId(userId, bookId);
            if (teacher == null) {//随便选一个
                teacher = chipsEnglishClazzService.loadAllChipsEnglishTeacher().stream().findFirst().orElse(null);
            }
            if (teacher != null) {
                mm.put("teacherName", teacher.getName());
                mm.put("headPortrait", teacher.getHeadPortrait());
            }
            AIActiveServiceUserTemplateItem item = handleUserAnswer(getRequestString("unitId"), getRequestString("lessonId"), qid, getRequestString("aid"));
            if (item != null) {
                mm.put("userAnswer", item);
            }
            mm.putAll(buildQuestionTemplate(userId, qid));
        });
    }

    /**
     * 主动服务用户模板中的"用户回答"数据
     */
    private AIActiveServiceUserTemplateItem handleUserAnswer(String unitId, String lessonId, String qid, String aid) {
        if (StringUtils.isBlank(aid) || StringUtils.isBlank(unitId) || StringUtils.isBlank(lessonId) || StringUtils.isBlank(qid)) {
            return null;
        }
        return chipsEnglishUserLoader.buildUserAnswer(unitId, lessonId, qid, aid, "孩子的回答");
    }

    private MapMessage buildQuestionTemplate(Long userId, String qid) {
        if (StringUtils.isBlank(qid)) {
            return MapMessage.successMessage();
        }
        Map<String, StoneData> qMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(qid));
        ActiveServiceUserQuestionTemplate userTemplate = null;
        MapMessage mapMessage = MapMessage.successMessage();
        if (userId != null && userId != 0L) {
            userTemplate = aiTodayLessonClient.getRemoteReference().queryActiveServiceUserQuestionTemplateByUserIdQid(userId, qid);
            if (userTemplate != null && StringUtils.isNotBlank(userTemplate.getJson())) {
                mapMessage.add("itemList", parseAIActiveServiceUserTemplateItem(userTemplate.getJson(), false));
            } else {
                mapMessage.add("itemList", Collections.emptyList());
            }
        } else {
            ActiveServiceQuestionTemplate questionTemplate = aiTodayLessonClient.getRemoteReference().loadActiveServiceQuestionTemplateById(qid);
            if (questionTemplate != null && StringUtils.isNotBlank(questionTemplate.getJson())) {
                mapMessage.add("itemList", parseAIActiveServiceUserTemplateItem(questionTemplate.getJson(), true));
            } else {
                mapMessage.add("itemList", Collections.emptyList());
            }
        }
        mapMessage.add("qid", qid);
        mapMessage.add("name", Optional.ofNullable(qMap).map(m -> m.get(qid)).map(StoneData::getCustomName).orElse(""));
        return mapMessage;
    }

    private List<AIActiveServiceUserTemplateItem> parseAIActiveServiceUserTemplateItem(String json, boolean general) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        List<AIActiveServiceUserTemplateItem> result = new ArrayList<>();
        List list = JsonUtils.fromJson(json, List.class);
        for (Object obj : list) {
            Map<String, Object> map = (Map<String, Object>) obj;
            AIActiveServiceUserTemplateItem item = new AIActiveServiceUserTemplateItem((String) map.get("name"),
                    (String) map.get("value"), (String) map.get("type"), (Integer) map.get("index"), general ? true : (Boolean) map.get("checkBox"));
            result.add(item);
        }
        return result;
    }

    @RequestMapping(value = "regionlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage regionList(@RequestParam Integer regionCode) {
        MapMessage message = MapMessage.successMessage();
        List<Region> regionList = new ArrayList<>();
        if (regionCode != null && regionCode >= 0) {
            regionList.addAll(raikouSystem.getRegionBuffer().loadChildRegions(regionCode));
        }
        message.put("regionList", regionList.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", e.getCode());
            map.put("name", e.getName());
            return map;
        }).collect(Collectors.toList()));
        return message;
    }

    // 提交问卷
    @RequestMapping(value = "/ugc/email_save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage emailSave() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long userId = user.getId();
//        Long userId = getRequestLong("userId");
        String recipientName = getRequestString("recipientName");
        String recipientTel = getRequestString("recipientTel");
        String recipientAddr = getRequestString("recipientAddr");
        String courseLevel = getRequestString("courseLevel");

        try {
            return AtomicLockManager.instance().wrapAtomic(chipsEnglishUserLoader).keyPrefix("UGC_MAIL_ADDRESS").keys(userId)
                    .proxy().updateMailAddrAndCourseLevel(userId, recipientName, recipientTel, recipientAddr, courseLevel);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 获取口语测试预约问卷
    @RequestMapping(value = "/ugc/oral_test.vpage", method = RequestMethod.GET)
    public String oralTestIndex(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorForChips(AuthType.CHIPS_UGC_ORAL.getType());
        }
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/oral_test";
    }

    //获取口语测试预约问卷数据
    @RequestMapping(value = "/ugc/oral_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage oralData() {
        MapMessage message = MapMessage.successMessage();
        Map<String, String> dayMap = new HashMap<>();
        dayMap.put("2019-03-13", "3月13日");
        dayMap.put("2019-03-14", "3月14日");
        dayMap.put("2019-03-15", "3月15日");
        dayMap.put("2019-03-16", "3月16日");
        dayMap.put("2019-03-17", "3月17日");
        message.add("dayList", dayMap);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("10:00:00", "10:00-11:00");
        map.put("11:00:00", "11:00-12:00");
        map.put("14:00:00", "14:00-15:00");
        map.put("15:00:00", "15:00-16:00");
        map.put("16:00:00", "16:00-17:00");
        map.put("17:00:00", "17:00-18:00");
        map.put("18:00:00", "18:00-19:00");
        map.put("19:00:00", "19:00-20:00");
        message.add("timeRegionList", map);
        return message;
    }

    // 提交问卷
    @RequestMapping(value = "/ugc/oral_submit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage oralTestSubmit() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long userId = user.getId();
//        Long userId = getRequestLong("userId");
        String beginDate = getRequestString("beginDate");

        Date testBeginTime;
        try {
            testBeginTime = DateUtils.parseDate(beginDate, DateUtils.FORMAT_SQL_DATETIME);

        } catch (ParseException e) {
            return MapMessage.errorMessage("日期格式不正确");
        }
        try {
            return AtomicLockManager.instance().wrapAtomic(chipsUserOralScheduleService).keyPrefix("UGC_ORAL_COLLECT").keys(userId)
                    .proxy().updateChipsUserOralTestSchedule(userId, testBeginTime, DateUtils.addHours(testBeginTime, 1));
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    @RequestMapping(value = "/center/invite_award_rolling.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviteAwardRolling(Model model) {
        String activityType = getRequestString("activityType");
        return wrapper(mm -> mm.putAll(chipsInvitionRewardLoader.loadInvitionActivityTopUser(activityType)));
    }

    @RequestMapping(value = "/center/invite_award_myreward.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviteAwardMyReward() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录");
        }
        return wrapper(mm -> mm.putAll(chipsInvitionRewardLoader.loadMyReward(user.getId())));
    }

    /**
     * type :已浏览:0,下单未支付:1,成功购买: 2
     *
     * @return
     */
    @RequestMapping(value = "/center/invite_award_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviteAwardDetail() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录");
        }
        int type = getRequestInt("type");
        return wrapper(mm -> mm.putAll(chipsInvitionRewardLoader.loadInvitionDetail(user.getId(), type)));
    }

    @Getter
    @Setter
    private class GradeRemaining {
        private Integer grade;
        private Integer remaining;

        public GradeRemaining(Integer grade, Integer remaining) {
            this.grade = grade;
            this.remaining = remaining;
        }
    }

}
