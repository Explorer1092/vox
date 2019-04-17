/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.DataProvider;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.utopia.api.constant.Currency;
import com.voxlearning.utopia.api.constant.P2pProvinceRef;
import com.voxlearning.utopia.api.constant.TeacherPrivilegeCard;
import com.voxlearning.utopia.api.constant.TeacherTaskType;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.business.api.constant.TeacherCardType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.mapper.TeacherCardMapper;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.campaign.client.WechatUserCampaignServiceClient;
import com.voxlearning.utopia.service.conversation.client.ConversationLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.TeacherAgentLoaderClient;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.support.IntegralCalculator;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.utopia.service.popup.client.LegacyPopupServiceClient;
import com.voxlearning.utopia.service.privilege.client.BlackWhiteListManagerClient;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.constants.ActivityType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.LatestPagination;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatCodeServiceClient;
import com.voxlearning.utopia.service.zone.api.UserGiftService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Teacher controller implementation.
 * Long long time ago, teacher controller borned...
 * Look, this controller has a lot of parents.
 *
 * @author Jingwei Dong
 * @author Yaoheng Wu
 * @author Lin Zhu
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @author Rui Bao
 * @author Xingqiang Wang
 * @author Miao Yu
 * @author Xiaoguang Wang
 * @author Shuai Huan
 * @author Yizhou Zhang
 * @since 2011-08-03
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController extends AbstractTeacherController {

    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private LegacyPopupServiceClient legacyPopupServiceClient;
    @Inject private TeacherAgentLoaderClient teacherAgentLoaderClient;
    @Inject private BlackWhiteListManagerClient blackWhiteListManagerClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private ConversationLoaderClient conversationLoaderClient;
    @Inject private FeedbackServiceClient feedbackServiceClient;
    @Inject private FlowerServiceClient flowerServiceClient;
    @Inject private MessageServiceClient messageServiceClient;
    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;
    @Inject private WechatCodeServiceClient wechatCodeServiceClient;
    @Inject private WechatUserCampaignServiceClient wechatUserCampaignServiceClient;

    @ImportService(interfaceClass = UserGiftService.class) private UserGiftService userGiftService;
    @ImportService(interfaceClass = VerificationService.class) private VerificationService verificationService;

    // 2015寒假改版 -- 教师端首页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        TeacherDetail teacher = currentTeacherDetail();

        Map indexData = businessTeacherServiceClient.loadTeacherIndexData2(teacher);
        if (indexData.containsKey("userGuideFlow")) {
            model.addAttribute("data", indexData);
            String redirectUrl = ConversionUtils.toString(indexData.get("userGuideFlow"));
            if (redirectUrl.startsWith(ProductConfig.getUcenterUrl())) {// 这里根据header确定是否是https
                boolean isHttpsRequest = getWebRequestContext().isHttpsRequest();
                if (isHttpsRequest) {
                    redirectUrl = redirectUrl.replaceAll("^http://", "https://");
                }
            }
            return redirectUrl;
        }
        model.addAttribute("data", indexData);
        model.addAttribute("p2pRefs", P2pProvinceRef.toMap()); // 人盯人专属客服
        model.addAttribute("isBindTeacherApp", vendorLoaderClient.loadVendorAppUserRef("17Teacher", teacher.getId()) != null);//是否已绑定Teacher APP

        // 判断一个老师是否可以分组布置作业
        model.addAttribute("groupSupported", blackWhiteListManagerClient.hasActivity(teacher.getId(), ActivityType.允许分组布置作业)); // todo used authed

        //校园大使
        if (teacher.isSchoolAmbassador()) {
            model.addAttribute("isNewAmbassador", false);
            AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(teacher.getId())
                    .stream().findFirst().orElse(null);
            if (ref != null && DateUtils.stringToDate("2015-05-11 00:00:00").before(ref.getCreateDatetime())) {
                model.addAttribute("isNewAmbassador", true);
            }
        }
        //新注册的老师 7天之内没有完成新手任务的老师显示
        if (DateUtils.calculateDateDay(new Date(), -7).before(teacher.getCreateTime())) {
            //没领取过奖励的老师
            if (CollectionUtils.isEmpty(businessTeacherServiceClient.loadTeacherTaskRewardHistory(teacher.getId(), TeacherTaskType.NEW_HAND_TASK))) {
                //获取新手任务完成状态
                Map<String, Object> taskInfo = businessTeacherServiceClient.loadTeacherNewHandTaskInfo(teacher.getId());
                model.addAttribute("taskInfo", taskInfo);
            } else {
                //显示当前周布置作业天数
                Map<Long, Set<String>> dataMap = newHomeworkCacheServiceClient.getNewHomeworkCacheService().assignHomeworkAndQuizDayCountManager_currentDays(Collections.singletonList(teacher.getId()));
                if (dataMap != null && CollectionUtils.isNotEmpty(dataMap.get(teacher.getId()))) {
                    model.addAttribute("assignDayCount", dataMap.get(teacher.getId()).size());
                }
            }
        } else {
            //显示当前周布置作业天数
            Map<Long, Set<String>> dataMap = newHomeworkCacheServiceClient.getNewHomeworkCacheService().assignHomeworkAndQuizDayCountManager_currentDays(Collections.singletonList(teacher.getId()));
            if (dataMap != null && CollectionUtils.isNotEmpty(dataMap.get(teacher.getId()))) {
                model.addAttribute("assignDayCount", dataMap.get(teacher.getId()).size());
            }
        }
        //首页是否提示老师升级tip
        model.addAttribute("showLevelUpTip", asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherLevelUpRemindManager_isUp(teacher.getId())
                .getUninterruptibly());
        return "teacherv3/index";
    }

    @RequestMapping(value = "cardlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadTeacherIndexCardList() {
        Teacher teacher = currentTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("学科不能为空");
        }
        List<TeacherCardMapper> cardList = businessTeacherServiceClient.loadTeacherCardList(teacher, "", "", getCdnBaseUrlStaticSharedWithSep());
        for (TeacherCardMapper mapper : cardList) {
            // 处理一下广告图片
            if (mapper.getCardType() == TeacherCardType.ACTIVITY) {
                mapper.setImgUrl(getCdnBaseUrlStaticSharedWithSep() + mapper.getImgUrl());
            }
        }
        return MapMessage.successMessage().add("cardList", cardList);
    }

    // 教师切换手机号学号
    @RequestMapping(value = "mobileoraccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mobileOrAccount() {


        String method = getRequestParameter("method", "MOBILE");
        if (!StringUtils.equals("ACCOUNT", method) && !StringUtils.equals("MOBILE", method))
            return MapMessage.errorMessage();
        asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherMobileOrAccountCacheManager_setMethod(currentUserId(), method)
                .awaitUninterruptibly();
        return MapMessage.successMessage();
    }

    // 2015寒假改版 -- 教师周金币礼物数量
    @RequestMapping(value = "teacherweekggc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getTeacherWeekGoldGiftCount() throws InterruptedException {
        Long teacherId = currentUserId();

        AlpsFuture<Integer> giftFuture = userGiftService.getCurrentWeekReceivedGiftCount(teacherId);

        WeekRange weekRange = WeekRange.current();
        int gold = integralHistoryLoaderClient.getIntegralHistoryLoader()
                .loadUserIntegralHistories(teacherId)
                .stream()
                .filter(h -> h.getIntegralType() != null)
                .filter(h -> !Objects.equals(IntegralType.奖品相关.getType(), h.getIntegralType()))
                .filter(h -> h.getCreatetime() != null)
                .filter(h -> weekRange.contains(h.getCreatetime()))
                .filter(h -> h.getIntegral() != null)
                .map(h -> IntegralCalculator.calculateIntegral(h.getIntegral(), Currency.GOLD_COIN))
                .mapToInt(i -> i)
                .sum();

        // 包班制支持
        // 读取所有主副账号的鲜花
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        int flower = (int) flowerServiceClient.loadReceiverFlowers(relTeacherIds).values()
                .stream()
                .flatMap(Collection::stream)
                .filter(t -> t.fetchCreateTimestamp() >= DateUtils.calculateDateDay(new Date(), -7).getTime())// 一周鲜花数量
                .count();
        return MapMessage.successMessage().add("gold", gold).add("gift", giftFuture.get()).add("flower", flower);
    }

    // 2015寒假改版 -- 教师新鲜事
    @RequestMapping(value = "teacherlatestnews.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getTeacherJournal() {
        Long teacherId = currentUserId();
        int currentPage = ConversionUtils.toInt(getRequest().getParameter("currentPage"), 1);
        int page = currentPage - 1;
        int size = 10;
        LatestPagination lp = teacherLoaderClient.loadLatests(teacherId, page, size);
        return MapMessage.successMessage().add("lp", lp);
    }

    // 2014暑期改版 -- 标记公告栏消息已读
    @RequestMapping(value = "markreaded.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage markReaded() {
        long popupId = getRequestLong("recordId");
        deletePopup(popupId);
        return MapMessage.successMessage();
    }

    // 2014暑期改版 -- 首页换班获取所有班级信息
    @RequestMapping(value = "getclazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAllClazzs() {
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(currentUserId()).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("clazzs", clazzs);
    }

    // 2015暑期 -- 更新认证一个月之内教师的认证特权卡缓存
    @RequestMapping(value = "updatetpc.vpage", method = RequestMethod.GET)
    public String updateTeacherPrivilegeCache() {
        TeacherPrivilegeCard type = TeacherPrivilegeCard.valueOf(getRequestString("type"));
        if (type == null) return "redirect:/teacher/index.vpage";
        asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherPrivilegeCardCacheManager_update(currentUserId(), type)
                .awaitUninterruptibly();
        switch (type) {
            case doNotShowAgain:
                return "redirect:/teacher/index.vpage";
            case awardCenter:
                return "redirect:/reward/product/exclusive/index.vpage";
            case reading:
                return "redirect:/teacher/homework/batchassignhomework.vpage";
            case lottery:
                return "redirect:/campaign/teacherlottery.vpage";
            case smartClazz:
                return "redirect:/teacher/smartclazz/list.vpage";
            default:
                return "redirect:/teacher/index.vpage";
        }
    }

    //获取mentor 动态
    @RequestMapping(value = "getmentorinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMentorInfo() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage();
        }
        try {
            Map<String, Object> mentorLatestInfo = asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                    .MentorLatestCacheManager_pureLoad(teacher.getId())
                    .getUninterruptibly();
            if (mentorLatestInfo == null) {
                DataProvider<Long, Map<String, Object>> provider = tid -> businessTeacherServiceClient.getMentorLatestInfo(tid);
                mentorLatestInfo = provider.provide(teacher.getId());
                asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                        .MentorLatestCacheManager_pureAdd(teacher.getId(), mentorLatestInfo)
                        .awaitUninterruptibly();
            }
            return MapMessage.successMessage().add("mentorLatestInfo", mentorLatestInfo);
        } catch (Exception ex) {
            logger.error("MENTOR SYSTEM GET MENTOR LATEST INFO ERROR. TID {}", teacher.getId(), ex);
            return MapMessage.errorMessage();
        }
    }

    // 2014暑期改版 -- 教师端首页 -- 气泡信息
    @RequestMapping(value = "bubbles.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage indexBubble() {
        try {
            Teacher teacher = currentTeacher();
            if (!teacher.isPrimarySchool()) {
                return MapMessage.errorMessage();
            }
            // part - 1 未处理换班申请
            int pendingApplicationCount = teacherAlterationServiceClient.countPendingApplicationSendIn(teacher.getId());
            // part - 2 未处理系统通知
            int unreadMessageCount = messageServiceClient.getMessageService().getUnreadMessageCount(teacher.narrow());
            // part - 3 未处理信件以及回复
            int unreadLetterCount = conversationLoaderClient.getConversationLoader().getUnreadLetterCount(teacher.getId());
            return MapMessage.successMessage()
                    .add("pendingApplicationCount", pendingApplicationCount)
                    .add("unreadNoticeCount", unreadMessageCount)
                    .add("unreadLetterAndReplyCount", unreadLetterCount);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "showtip.vpage", method = RequestMethod.GET)
    public String showtip() {
        Long teacherId = currentUserId();
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (null == school) {
            return "redirect:/teacher/index.vpage";
        }
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(clazzs)) {
            return "redirect:/teacher/index.vpage";
        }
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/systemclazz/clazzindex.vpage?step=showtip";
    }

    /* --------------------------------------------------------
     * 英语老师为英语作业群发评语
     * -------------------------------------------------------- */
    @Deprecated
    @RequestMapping(value = "englishhomeworkcommentation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage englishHomeworkCommentation(@RequestBody Map<String, Object> mapper) {
        //noinspection unchecked
        return MapMessage.errorMessage("功能已下线");
    }

    /* --------------------------------------------------------
     * 数学老师为数学作业群发评语
     * -------------------------------------------------------- */
    @Deprecated
    @RequestMapping(value = "mathhomeworkcommentation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mathHomeworkCommentation(@RequestBody Map<String, Object> mapper) {
        //noinspection unchecked
        return MapMessage.errorMessage("功能已下线");
    }

    /* --------------------------------------------------------
     * 语文老师为数学作业群发评语
     * -------------------------------------------------------- */
    @Deprecated
    @RequestMapping(value = "chinesehomeworkcommentation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chineseHomeworkCommentation(@RequestBody Map<String, Object> mapper) {
        //noinspection unchecked
        return MapMessage.errorMessage("功能已下线");
    }

    /* --------------------------------------------------------
     * NEW -- 老师为作业群发评语
     * -------------------------------------------------------- */
    @RequestMapping(value = "writeHomeworkComment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage writeHomeworkComment(@RequestBody Map<String, Object> mapper) {
        //noinspection unchecked
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获得书籍相同的班级
     */
    @RequestMapping(value = "homeworkclazz/{clazzId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkClazz(@PathVariable("clazzId") Long clazzId) {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "nocertificationcodereceived.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage noCertificationCodeReceived() {
        try {
            String mobile = getRequest().getParameter("mobile");
            if (StringUtils.isBlank(mobile)) {
                return MapMessage.errorMessage("请填写手机号");
            }
            return verificationService.noCertificationCodeReceived(currentUserId(), mobile);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("请重新提交");
        }
    }

    /**
     * 提示老师绑定手机
     */
    @RequestMapping(value = "banding.vpage", method = RequestMethod.GET)
    public String binding(Model model) {
        model.addAttribute("userIntegral", getRequest().getParameter("userIntegral"));
        return "teacherv3/banding";
    }

    @RequestMapping(value = "activatepop.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage activatepop() {
        MapMessage message = new MapMessage();
        TeacherDetail detail = currentTeacherDetail();
        if (!detail.isSchoolAmbassador()) {
            message.setSuccess(false);
            message.setInfo("非校园大使用户");
            return message;
        }
        try {
            Map<String, Object> map = businessTeacherServiceClient.getActivateIntegralPopupContent(currentUserId());
            message.setSuccess(true);
            message.add("mapper", map);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            message.setSuccess(false);
        }
        return message;
    }

    @RequestMapping(value = "callambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage callAmbassador(@RequestParam String content) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentUserId());
        if (ua == null || !ua.isMobileAuthenticated()) {
            return MapMessage.errorMessage("请绑定手机");
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(currentUserId());
        if (!teacherDetail.isSchoolAmbassador()) {
            return MapMessage.errorMessage("请先申请校园大使");
        }
        return feedbackServiceClient.getFeedbackService()
                .callAmbassador2(currentUserId(), ua.getSensitiveMobile(), content)
                .getUninterruptibly();
    }

    @RequestMapping(value = "clazzexchangehelper.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzExchangeHelper(@RequestParam String content) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentUserId());
        if (ua == null || !ua.isMobileAuthenticated()) {
            return MapMessage.errorMessage("请绑定手机");
        }
        return feedbackServiceClient.getFeedbackService().clazzExchangeHelper(currentUserId(), ua.getSensitiveMobile(), content);
    }

    /**
     * 新功能
     */
    @RequestMapping(value = "newfeatures/index.vpage", method = RequestMethod.GET)
    public String newFeatures() {
        return "/teacherv3/newfeatures/index";
    }


    @RequestMapping(value = "qrcode.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage qrcode() {
        MapMessage message = new MapMessage();
        try {
            Integer campaignId = getRequestInt("campaignId", 0);
            wechatUserCampaignServiceClient.getWechatUserCampaignService().setUserCampaign(currentUserId(), campaignId).get();

            String url = wechatCodeServiceClient.getWechatCodeService()
                    .generateQRCode(String.valueOf(currentUserId()), WechatType.TEACHER)
                    .getUninterruptibly();
            message.setSuccess(true);
            message.add("qrcode_url", url);
        } catch (UtopiaRuntimeException ex) {
            logger.warn("生成二维码失败，msg:{}", ex.getMessage());
            message.setSuccess(false);
            message.setInfo("生成二维码失败");
        } catch (Exception ex) {
            logger.error("生成二维码失败,msg:{}", ex.getMessage(), ex);
            message.setSuccess(false);
            message.setInfo("生成二维码失败");
        }
        return message;
    }

    @RequestMapping(value = "saverecommendinviteflag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRecommendInviteFlag() {
        try {
            washingtonCacheSystem.CBS.flushable.set(MemcachedKeyConstants.RECOMMEND_INVITE_FLAG + "_" + currentUserId(), DateUtils.getCurrentToDayEndSecond(), "dummy");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("操作失败");
        }
        return MapMessage.successMessage();
    }

    // 记录老师首页点击了升级提示
    @RequestMapping(value = "recordlevelup.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage recordLevelUp() {
        Long teacherId = currentUserId();
        //记录点击调整按钮
        asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherLevelUpRemindManager_drop(teacherId)
                .awaitUninterruptibly();
        return MapMessage.successMessage();
    }

    // 记录老师首页点击了升级提示
    @RequestMapping(value = "teacheragent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTeacherAgent() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage("login required!");
        }

        return MapMessage.successMessage().add("agentList", teacherAgentLoaderClient.getSchoolManager(teacherDetail.getTeacherSchoolId()));
    }

    @RequestMapping(value = "termend.vpage", method = RequestMethod.GET)
    public String termEndAdvertise(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (Subject.ENGLISH != teacher.getSubject() && Subject.MATH != teacher.getSubject()) {
            return "redirect:/teacher/index.vpage";
        }
        return "/teacherv3/termend";
    }

    /**
     * 期末作业运营广告
     */
    @RequestMapping(value = "termendinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getTeacherMinLevelAndSubject() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId()).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        int level = CollectionUtils.isEmpty(clazzs) ? 0 : clazzs.get(0).getClazzLevel().getLevel();
        return MapMessage.successMessage().add("subject", teacher.getSubject()).add("level", level);
    }

    private void deletePopup(final Long id) {
        if (id == null) {
            return;
        }
        try {
            legacyPopupServiceClient.getLegacyPopupService()
                    .updatePopups(null, Collections.singleton(id), null, null);
        } catch (Exception ignored) {
        }
    }
}
