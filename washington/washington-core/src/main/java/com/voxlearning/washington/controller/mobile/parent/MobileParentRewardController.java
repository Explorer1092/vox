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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.IterableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.business.api.constant.AppUseNumCalculateType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.mapper.MissionMapper;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkReportService;
import com.voxlearning.utopia.service.order.api.entity.UserOrderFaceDetectRecord;
import com.voxlearning.utopia.service.order.consumer.UserOrderFaceDetectRecordLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardBusinessType;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardStatus;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardSubject;
import com.voxlearning.utopia.service.parentreward.api.entity.*;
import com.voxlearning.utopia.service.parentreward.api.mapper.*;
import com.voxlearning.utopia.service.parentreward.cache.ParentRewardCache;
import com.voxlearning.utopia.service.parentreward.cache.ParentRewardCacheManager;
import com.voxlearning.utopia.service.parentreward.helper.ParentRewardHelper;
import com.voxlearning.utopia.service.parentreward.helper.ParentRewardScoreLevelCalculator;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.userlevel.api.mapper.UserActivationLevel;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Hailong Yang
 * @version 0.1
 * @since 2015/09/14
 */
@Controller
@RequestMapping(value = "/parentMobile/parentreward")
@Slf4j
public class MobileParentRewardController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @ImportService(interfaceClass = NewHomeworkReportService.class)
    private NewHomeworkReportService newHomeworkReportService;
    @ImportService(interfaceClass = NewHomeworkLoader.class)
    private NewHomeworkLoader newHomeworkLoader;
    @Inject
    private ParentRewardLoaderClient parentRewardLoaderClient;
    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private UserOrderFaceDetectRecordLoaderClient faceDetectRecordLoaderClient;

    private static final List<Subject> SUBJECT_LIST = Arrays.asList(Subject.ENGLISH, Subject.MATH, Subject.CHINESE);
    private static final Map<Subject, String> SUBJECT_PRODUCT_MAP;
    private static final Map<AppUseNumCalculateType, String> CALCULATE_TYPE_MAP;
    private static final List<String> NOTICE_LIST;

    static {
        SUBJECT_PRODUCT_MAP = new HashMap<>();
        SUBJECT_PRODUCT_MAP.put(Subject.ENGLISH, OrderProductServiceType.AfentiExam.name());
        SUBJECT_PRODUCT_MAP.put(Subject.MATH, OrderProductServiceType.AfentiMath.name());
        SUBJECT_PRODUCT_MAP.put(Subject.CHINESE, OrderProductServiceType.AfentiChinese.name());

        CALCULATE_TYPE_MAP = new HashMap<>();
        CALCULATE_TYPE_MAP.put(AppUseNumCalculateType.CLAZZ, "同班");
        CALCULATE_TYPE_MAP.put(AppUseNumCalculateType.GRADE, "同年级");
        CALCULATE_TYPE_MAP.put(AppUseNumCalculateType.SCHOOL, "同校");
        CALCULATE_TYPE_MAP.put(AppUseNumCalculateType.NATION, "");

        NOTICE_LIST = new ArrayList<>();
        NOTICE_LIST.add("studentName的表现值得你的鼓励哦");
        NOTICE_LIST.add("查看奖励详情可了解studentName的每一次进步哦");
        ;
        NOTICE_LIST.add("studentName表现不错，快给Ta发奖励吧");
        NOTICE_LIST.add("快看studentName有多棒，还不奖励Ta~");
        NOTICE_LIST.add("studentName的优秀一定离不开你的鼓励~");
    }

    @RequestMapping(value = "summary.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage summary() {
        Long studentId = getRequestLong("sid");
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", studentId,
                "mod1", "parentReward",
                "mod2", currentUserId(),
                "mod5", "default",
                "op", "parentRewardSummary"
        ));
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "query.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage query() {
        Long studentId = getRequestLong("sid");
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", studentId,
                "mod1", "parentReward",
                "mod2", currentUserId(),
                "mod5", "default",
                "op", "parentRewardQuery"
        ));
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "send.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage send() {
        Long studentId = getRequestLong("sid");
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", studentId,
                "mod1", "parentReward",
                "mod2", currentUserId(),
                "mod5", "default",
                "op", "parentRewardSend"
        ));
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage list() {
        Long studentId = getRequestLong("sid");
        boolean expire = getRequestBool("expire");
        if (studentId == 0) {
            return MapMessage.errorMessage("学生ID错误");
        }
        try {
            MapMessage resultMap = MapMessage.successMessage();
            Integer currentPage = getRequestInt("currentPage", 1);
            Pageable page = new PageRequest(currentPage - 1, 10);
            Page<ParentRewardLog> rewardLogPage = parentRewardLoaderClient.loadParentRewardLogPage(studentId, page, expire);
            List<ParentRewardRecordWrapper> rewardList = new ArrayList<>();
            int totalPages = 0;
            long expireTotalCount;
            long totalCount = 0;
            if (rewardLogPage != null && CollectionUtils.isNotEmpty(rewardLogPage.getContent())) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                List<ParentRewardLog> rewardLogList = rewardLogPage.getContent();
                boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
                List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
                rewardLogList.forEach(log -> rewardList.add(convert(log, studentParentRefs, showScoreLevel)));
                totalPages = rewardLogPage.getTotalPages();
                totalCount = rewardLogPage.getTotalElements();
            }
            if (expire) {
                expireTotalCount = totalCount;
                totalCount = parentRewardLoaderClient.getTotalCount(studentId, Boolean.FALSE);
            } else {
                expireTotalCount = parentRewardLoaderClient.getTotalCount(studentId, Boolean.TRUE);
            }

            //家庭关爱值
            Set<String> ids = new HashSet<>();
            ids.add(StudentRewardSendCount.generateId(studentId, ParentRewardHelper.lastTermDateRange().getStartDate()));
            Map<String, StudentRewardSendCount> studentRewardSendCountMap = parentRewardLoader.getStudentRewardSendCount(ids);
            long itemCount = studentRewardSendCountMap.values().stream().mapToLong(StudentRewardSendCount::getItemCount).sum();
            if (itemCount > 0) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                if (studentDetail != null) {
                    resultMap.add("student_name", studentDetail.fetchRealname());
                }
                resultMap.add("last_item_count", itemCount);
                resultMap.add("rank", parentRewardLoader.getParentRewardStudentTermRank(studentId, ParentRewardHelper.lastTermDateRange()));
            }


            return resultMap.add("totalPage", totalPages)
                    .add("currentPage", currentPage)
                    .add("totalCount", totalCount)
                    .add("expireTotalCount", expireTotalCount)
                    .add("send_available", parentRewardService.rewardSendAvailable(currentUserId(), studentId, isParentRewardNewVersionForFaceDetect(getAppVersion())))
                    .add("reward_list", rewardList);
        } catch (Exception e) {
            logger.error("{}, sid:{}", e.getMessage(), studentId, e);
            return MapMessage.errorMessage("获取家长奖励列表失败");
        }

    }

    /**
     * 排行榜
     */
    @RequestMapping(value = "range.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage range() {
        long studentId = getRequestLong("sid");
        if (studentId == 0) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            Integer currentPage = getRequestInt("currentPage", 1);
            Pageable page = new PageRequest(currentPage - 1, 10);
            AlpsFuture<Integer> parentRewardStudentRank = parentRewardLoader.getParentRewardStudentRank(studentId);

            Page<ParentRewardStudentCountWrapper> parentRewardStudentRangeListPage = parentRewardLoader.getParentRewardStudentRangeList(studentId, page);
            parentRewardStudentRangeListPage.forEach(t -> t.setAvatar(getUserAvatarImgUrl(t.getAvatar())));

            return MapMessage.successMessage()
                    .add("range_list", parentRewardStudentRangeListPage.getContent())
                    .add("total_count", parentRewardStudentRangeListPage.getTotalElements())
                    .add("currentPage", currentPage)
                    .add("month", LocalDateTime.now().getMonth().getValue())
                    .add("totalPage", parentRewardStudentRangeListPage.getTotalPages())
                    .add("range", null == parentRewardStudentRank ? 0 : parentRewardStudentRank.getUninterruptibly());
        } catch (Exception ex) {
            log.error("sid:{},pid:{}", studentId, currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }


    /**
     * 查询可发的亲子信方案列表
     */
    @RequestMapping(value = "/letter/template.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage letterTemplate() {
        Long studentId = getRequestLong("sid");
        if (0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            Set<String> texts = new HashSet<>();

            //计算家长身份
            String callName = "";
            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
            if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                StudentParentRef studentParentRef = studentParentRefs.stream().filter(ref -> Objects.equals(ref.getParentId(), currentUserId())).findFirst().orElse(null);
                if (null == studentParentRef) {
                    return MapMessage.errorMessage("未关联的孩子");
                }
                callName = studentParentRef.getCallName();
                if (CallName.其它监护人.name().equals(callName)) {
                    callName = "我";
                }
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            List<ParentRewardLog> parentRewardList = parentRewardLoader.getParentRewardList(studentId, ParentRewardStatus.INIT.getType());

            //查找100分的奖励项
            boolean has100ScoreReward = ParentRewardCacheManager.INSTANCE.hasHomeworkScore100Today(studentId);
            if (!has100ScoreReward) {
                List<ParentRewardLog> score100List = parentRewardList.stream().filter(log -> MapUtils.isNotEmpty(log.getExt()) && log.getExt().containsKey("score") && SafeConverter.toInt(log.getExt().get("score")) == 100).collect(Collectors.toList());
                has100ScoreReward = CollectionUtils.isNotEmpty(score100List);
            }
            if (has100ScoreReward) {
                if (RandomUtils.nextInt(0, 10) > 5) {
                    texts.add("很厉害啊，拿了满分，继续努力吧！");
                } else {
                    if (showScoreLevel) {
                        texts.add(callName + "看见你这次拿了A+哦，真棒！");
                    } else {
                        texts.add(callName + "看见你这次拿了100分哦，真棒！");
                    }
                }
            }
            //生成奖励项文案
            Set<String> randomTexts = new HashSet<>();
            //固定文案
            randomTexts.add("很不错！看来你的努力付出有回报了，要继续坚持哦！");
            randomTexts.add("你真棒！" + callName + "希望你继续坚持好习惯，再接再厉");
            randomTexts.add("Good，你的努力" + callName + "都看到了，继续加油");
            randomTexts.add("你表现的很好哦，在" + callName + "心里你是最棒的！");
            randomTexts.add(callName + "为你感到骄傲，但是你自己不能太骄傲，要继续努力哦！");
            randomTexts.add("做的不错！" + callName + "给你100个赞");
            randomTexts.add("你的每一次进步" + callName + "都知道，加油！");
            randomTexts.add("非常棒！");
            randomTexts.add("不错！");
            randomTexts.add("有进步！");
            randomTexts.add("看到你的表现我很开心！");

            int count = texts.size();
            do {
                int index = RandomUtils.nextInt(0, randomTexts.size() - 1);
                texts.add(IterableUtils.get(randomTexts, index));
            } while (texts.size() - count < 2);

            return MapMessage.successMessage().add("letters", texts);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/letter/send.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendLetters() {
        Long studentId = getRequestLong("sid");
        String content = getRequestString("ctn");

        if (0 == studentId || StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            boolean available = ParentRewardCacheManager.INSTANCE.isParentNeverSendLetterToday(currentUserId(), studentId);
            if (!available) {
                return MapMessage.errorMessage("每天只能发送一封亲子信，你今天已经发过了");
            }

            parentRewardService.sendLetter(currentUserId(), studentId, content, content);

            //清理亲子信箱历史缓存
            parentRewardService.evictLetterPageCache(studentId);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("sid:{},pid:{},error:{}", studentId, currentUserId(), ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询亲子信箱历史记录
     */
    @RequestMapping(value = "/letter/history.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage letterHistory() {
        Long studentId = getRequestLong("sid");
        Integer pageIndex = getRequestInt("index", 1);
        if (0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }
        Long parentId = currentUserId();
        if (null == parentId) {
            return MapMessage.errorMessage("请登录后再试");
        }

        try {
            List<ParentRewardLetter> letters = parentRewardLoader.getLettersByStudentId(studentId, pageIndex);
            parentRewardService.clearUnreadLetters(studentId, parentId);

            boolean sendAvailable = ParentRewardCacheManager.INSTANCE.isParentNeverSendLetterToday(parentId, studentId);
            MapMessage message = MapMessage.successMessage()
                    .add("sendAvailable", sendAvailable);

            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
            if (CollectionUtils.isEmpty(studentParentRefs)) return message;
            Map<Long, String> callNameMap = studentParentRefs.stream().collect(Collectors.toMap(StudentParentRef::getParentId, StudentParentRef::getCallName, (u, v) -> u));

            Set<Long> senderIds = letters.stream().map(ParentRewardLetter::getSenderId).collect(Collectors.toSet());
            Map<Long, User> senderUserMap = userLoaderClient.loadUsers(senderIds);

            List<Map<String, Object>> letterList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            letters.stream().sorted(Comparator.comparing(ParentRewardLetter::getCreateTime))    //正序
                    .forEach(letter -> {
                        Map<String, Object> letterMap = new HashMap<>();
                        if (Objects.equals(letter.getSenderId(), studentId)) {
                            letterMap.put("name", senderUserMap.get(studentId).getProfile().getRealname());
                            letterMap.put("isChild", true);
                        } else {
                            letterMap.put("name", callNameMap.get(letter.getSenderId()));
                            letterMap.put("isChild", false);
                        }
                        letterMap.put("content", letter.getContent());
                        letterMap.put("time", LocalDateTime.ofInstant(letter.getCreateTime().toInstant(), ZoneId.systemDefault()).format(formatter));
                        letterMap.put("avatar", getUserAvatarImgUrl(senderUserMap.get(letter.getSenderId())));

                        letterList.add(letterMap);
                    });

            return message.add("letters", letterList);
        } catch (Exception ex) {
            log.error("sid:{},index:{},{}", studentId, pageIndex, ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询亲子信息的概要信息
     */
    @RequestMapping(value = "/letter/summary.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage letterSummary() {
        Long studentId = getRequestLong("sid");
        if (0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }
        if (null == currentUserId()) {
            return MapMessage.errorMessage("未登录");
        }

        try {
            RedisLetterWrapper recentLetter = parentRewardLoader.getUnreadLetterInfo(currentUserId(), studentId);
            if (null == recentLetter) {
                return MapMessage.successMessage();
            }
            MapMessage message = MapMessage.successMessage()
                    .add("count", recentLetter.getLetterCount())
                    .add("id", recentLetter.getId().toString())
                    .add("content", recentLetter.getContent());

            User user = raikouSystem.loadUser(recentLetter.getUserId());
            if (null != user) {
                message.add("avatar", getUserAvatarImgUrl(user));
            }
            return message;
        } catch (Exception ex) {
            log.error("pid:{}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    //parentReward-2.5
    @RequestMapping(value = "/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rewardDetail() {
        Long studentId = getRequestLong("sid");
        if (studentId == 0) {
            return MapMessage.errorMessage("学生id错误");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录后再试");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        try {
            MapMessage mapMessage = MapMessage.successMessage();

            Pageable page = new PageRequest(0, 200);
            Page<ParentRewardStudentCountWrapper> rangeListPage = parentRewardLoader.getParentRewardStudentRangeList(studentId, page);
            List<ParentRewardStudentCountWrapper> rangeList = rangeListPage.getContent();
            boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");

            mapMessage.add("student_name", studentDetail.fetchRealname());

            //全国和全班发奖人数
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            mapMessage.add("in_clazz", clazz != null);

            long clazzSendCount = 0;
            if (clazz != null) {
                List<Long> studentIds = ParentRewardCache.getPersistenceCache().load(ParentRewardHelper.clazzSentStudentList(clazz.getId()));
                clazzSendCount = CollectionUtils.isEmpty(studentIds) ? 0 : studentIds.size();
            }
            mapMessage.add("clazz_send_count", clazzSendCount);
            int countrySendCount = SafeConverter.toInt(ParentRewardCache.getPersistenceCache().load(ParentRewardHelper.countryRewardCountKey()));
            mapMessage.add("country_send_count", countrySendCount);

            //规则弹窗
            boolean showRuleTip = false;
            boolean flag = SafeConverter.toInt(CacheSystem.CBS.getCache("persistence").load("SHOW_RULE_TIP_" + parent.getId())) == 0;
            Date now = new Date();
            if (flag && now.before(DateUtils.stringToDate("2019-03-15 25:59:59"))) {
                showRuleTip = true;
            }
            mapMessage.add("show_rule_tip", showRuleTip);
            CacheSystem.CBS.getCache("persistence").incr("SHOW_RULE_TIP_" + parent.getId(), 1, 1, 0);

            //上学期奖励信息
            lastTermInfo(mapMessage, studentDetail);

            //家庭关爱值
            handleItemCount(mapMessage, studentId, clazz);

            //亲子信
            mapMessage.add("letter_send_available", ParentRewardCacheManager.INSTANCE.isParentNeverSendLetterToday(currentUserId(), studentId));
            int unreadLetterCount = parentRewardLoader.getUnreadLetterCount(currentUserId(), studentId);
            mapMessage.add("letter_count", unreadLetterCount);

            List<ParentRewardLog> parentRewardLogs = parentRewardLoader.getSevenDaysRewardList(studentId);

            //今天是否发放过奖励
            boolean sentToday = !ParentRewardCacheManager.INSTANCE.isStudentNeverBeenSentParentReward(studentId);
            mapMessage.add("sent_today", sentToday);

            //待发奖励列表
            boolean sendAvailable = parentRewardService.rewardSendAvailable(parent.getId(), studentId, isParentRewardNewVersionForFaceDetect(getAppVersion()));
            List<ParentRewardLog> initLogs = parentRewardLogs.stream()
                    .filter(rewardLog -> rewardLog.getStatus() == ParentRewardStatus.INIT.getType())
                    .filter(rewardLog -> rewardLog.getSendExpire().after(new Date()))
                    .collect(Collectors.toList());
            if (initLogs.size() > 0) {
                //是不是第一次发放奖励
                boolean firstSend = ParentRewardCacheManager.INSTANCE.isParentNeverSendParentReward(parent.getId());
                String notice;
                if (firstSend) {
                    notice = "快来发放你在这里给孩子的第一次奖励吧";
                } else if (sendAvailable) {
                    Collections.shuffle(NOTICE_LIST);
                    notice = NOTICE_LIST.get(0).replace("studentName", studentDetail.fetchRealname());
                } else {
                    notice = "16:00-22:00为作业时间，暂不可发放奖励哦。家长奖励达LV3即可开启不限时特权";
                }
                mapMessage.add("notice", notice);
                mapMessage.add("rewards", generateInitRewardList(initLogs, studentDetail));
            } else {
                List<ParentRewardLog> todayLogs = parentRewardLogs.stream().filter(rewardLog -> rewardLog.getCreateTime().after(DayRange.current().getStartDate())).collect(Collectors.toList());
                mapMessage.add("subject_count_list", generateRewardCountBySubject(todayLogs, studentDetail));
            }

            Map<String, Integer> recommendFlagMap = new HashMap<>();
            //奖励解读
            List<ParentRewardLog> todayLogs = parentRewardLogs.stream().filter(rewardLog -> rewardLog.getCreateTime().after(DayRange.current().getStartDate())).collect(Collectors.toList());
            List<Map<String, Object>> explainList;
            explainList = generateExplainList(studentDetail, todayLogs, recommendFlagMap);
            mapMessage.add("explain_list", explainList);

            //奖励记录
            List<Map<String, Object>> rewardHistory = generateRewardHistory(parentRewardLogs, studentDetail, showScoreLevel, sendAvailable, recommendFlagMap);
            mapMessage.add("reward_history", rewardHistory);

            //奖励排名
            AlpsFuture<Integer> parentRewardStudentRank = parentRewardLoader.getParentRewardStudentRank(studentId);
            mapMessage.add("rank", null == parentRewardStudentRank ? 0 : SafeConverter.toInt(parentRewardStudentRank.getUninterruptibly()));

            if (rangeList.size() > 4) {
                rangeList = rangeList.subList(0, 4);
            }
            rangeList.forEach(e -> e.setAvatar(getUserAvatarImgUrl(e.getAvatar())));
            mapMessage.add("range_list", rangeList);

            return mapMessage;
        } catch (Exception ex) {
            log.error("获取奖励详情失败.pid:{}. sid:{}, {}", parent.getId(), studentId, ex.getMessage(), ex);
            return MapMessage.errorMessage("获取奖励详情失败");
        }
    }

    @RequestMapping(value = "/sendreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendParentReward() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId == 0) {
            return MapMessage.errorMessage("学生id错误");
        }
        if (null == currentUserId()) {
            return MapMessage.errorMessage("未登录");
        }
        String id = getRequestString(REQ_PARENT_REWARD_ID);
        String key = getRequestString(REQ_PARENT_REWARD_KEY);
        String type = getRequestString(REQ_PARENT_REWARD_TYPE);
        Integer count = getRequestInt(REQ_PARENT_REWARD_COUNT);
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(key) || StringUtils.isEmpty(type)) {
            return MapMessage.errorMessage("没有可领取的奖励");
        }
        try {
            boolean sendAvailable = parentRewardService.rewardSendAvailable(currentUserId(), studentId, isParentRewardNewVersionForFaceDetect(getAppVersion()));

            if (isParentRewardNewVersionForFaceDetect(getAppVersion()) && !sendAvailable) {
                List<UserOrderFaceDetectRecord> faceDetectRecordList = faceDetectRecordLoaderClient.loadUserOrderFaceDetectRecordsByUserId(currentUserId());
                //人脸识别
                if (CollectionUtils.isNotEmpty(faceDetectRecordList)) {
                    UserOrderFaceDetectRecord faceDetectRecord = faceDetectRecordList.stream()
                            .filter(record -> record.getUserId().equals(currentUserId()))
                            .filter(record -> "reward".equalsIgnoreCase(record.getSource()))
                            .sorted((r1, r2) -> r2.getCreateTime().compareTo(r1.getCreateTime()))
                            .findFirst()
                            .orElse(null);
                    //校验识别结果
                    if (null == faceDetectRecord ||
                            !faceDetectRecord.getRecognitionResult()) {
                        return MapMessage.errorMessage("人脸识别失败，识别通过后可发放奖励哦");
                    }
                }
            } else if (!sendAvailable && !"FINISH_WONDERLAND_MISSION".equals(key)) {
                return MapMessage.errorMessage("为鼓励家长发放奖励，发奖时间将在每天08:00-16:00开启");
            }

            ParentRewardLog log = new ParentRewardLog();
            log.setId(id);
            log.setKey(key);
            log.setStudentId(studentId);
            log.setType(type);
            log.setCount(count);
            ParentRewardSendResult sendResult;
            try {
                sendResult = atomicLockManager.wrapAtomic(parentRewardService)
                        .keyPrefix("SEND_STUDENT_REWARD")
                        .keys(studentId)
                        .proxy()
                        .sendParentRewards(currentUserId(), studentId, Collections.singletonList(log));
            } catch (DuplicatedOperationException ex) {
                return MapMessage.errorMessage("当前奖励正在被发放，请稍候刷新页面重试");
            }
            if (null == sendResult) {
                logger.error("send parentReward fail, id:{}", id);
                return MapMessage.errorMessage("奖励发放失败");
            }
            MapMessage message = MapMessage.successMessage()
                    .add("sent_by_others", sendResult.getCount() == 0);

            //补充一下壳需要的家长等级弹窗信息
            if (sendResult.getCount() > 0) {
                Map<String, Object> popInfo = getActivationInfoForPopup(currentUserId(), studentId);
                message.add("popInfo", popInfo);
            }
            return message;
        } catch (Exception ex) {
            log.error("{}, sid:{}, pid:{}", ex.getMessage(), studentId, currentUserId(), ex);
            return MapMessage.errorMessage("领取奖励失败");
        }

    }

    private Map<String, Object> getActivationInfoForPopup(Long parentId, Long studentId) {
        Map<String, Object> popInfo = new HashMap<>();

        Long sendCountToday = washingtonCacheSystem.CBS.persistence.incr("PARENT_SEND_REWARD_COUNT_" + currentUserId(), 1, 1, DateUtils.getCurrentToDayEndSecond());
        if (null != sendCountToday && sendCountToday <= 3) {
            popInfo.put("pop", true);
            popInfo.put("title", "孩子的点滴进步都值得鼓励");
            popInfo.put("count", sendCountToday);
            popInfo.put("action", "发放家长奖励");
            UserActivationLevel parentLevel = userLevelLoader.getParentLevel(parentId);
            if (null != parentLevel) {
                popInfo.put("level", parentLevel.getLevel());
                popInfo.put("levelName", parentLevel.getName());
                popInfo.put("activation", parentLevel.getValue());
                popInfo.put("maxActivation", parentLevel.getLevelEndValue() + 1);
                popInfo.put("minActivation", parentLevel.getLevelStartValue());
                popInfo.put("parentLevelUrl", "/view/mobile/parent/grade/detail.vpage");
                if (parentLevel.getLevel() > 1 && LocalDateTime.now().getHour() >= 8 && LocalDateTime.now().getHour() < 16) {
                    popInfo.put("value", 2L);
                    popInfo.put("desc", "*每日8:00-16:00发奖励可得双倍活跃值");
                } else {
                    popInfo.put("value", 1L);
                }
            }
        } else {
            popInfo.put("pop", false);
        }
        return popInfo;
    }

    //所有可发放奖励列表
    private List<ParentRewardLogView> generateInitRewardList(List<ParentRewardLog> rewardLogs, StudentDetail studentDetail) {
        boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
        boolean sendAvailableNotFirst = parentRewardService.sendAvailableWithOutFirst(currentUserId(), studentDetail.getId(), isParentRewardNewVersionForFaceDetect(getAppVersion()));
        List<ParentRewardLogView> views = new ArrayList<>();
        boolean firstSend = ParentRewardCacheManager.INSTANCE.isParentNeverSendParentReward(currentUserId());

        rewardLogs.forEach(rewardLog -> {
            ParentRewardLogView view = new ParentRewardLogView();
            ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
            if (item != null) {
                ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
                if (category != null) {
                    view.setRewardId(rewardLog.getId());
                    view.setCount(rewardLog.getCount());
                    view.setType(rewardLog.getType());
                    view.setItemKey(rewardLog.getKey());
                    view.setCategoryRank(category.getRank());
                    view.setCreateTime(rewardLog.getCreateTime());
                    view.setCreateTimeString(DateUtils.dateToString(rewardLog.getCreateTime(), "MM-dd"));
                    view.setBusiness(generateBusiness(rewardLog, item, category, showScoreLevel));
                    view.setSendUrl(item.getSecondaryPageUrl());
                    String icon = item.getIcon();
                    if (showScoreLevel && StringUtils.isNotBlank(item.getLevelIcon())) {
                        icon = item.getLevelIcon();
                    }
                    view.setIcon(icon);
                    view.setColor(item.getColor());
                    view.setSendExpireDays(item.getSendExpireDays());
                    views.add(view);
                }
            }
        });
        Comparator<ParentRewardLogView> comparator = Comparator.comparingInt(ParentRewardLogView::getCategoryRank);
        comparator = comparator.thenComparing((o1, o2) -> {
            Date date1 = o1.getCreateTime();
            Date date2 = o2.getCreateTime();
            return date2.compareTo(date1);
        });
        views.stream().sorted(comparator).forEach(view -> {
            //如果第一次发放奖励，返回的列表仅第一个显示能发，其他返回倒计时
            if (!sendAvailableNotFirst && view.getSendExpireDays() != 0 && (!firstSend || views.indexOf(view) > 0)) {
                view.setSendStartTime(parentRewardService.timeToSendReward());
            }
        });
        return views;
    }

    //可发放列表奖励下方显示的文案
    private String generateBusiness(ParentRewardLog rewardLog, ParentRewardItem item, ParentRewardCategory category, Boolean showScoreLevel) {
        String business = "";
        if ("HOMEWORK".equals(category.getKey())) {
            if (showScoreLevel && StringUtils.isNotBlank(item.getLevelTitle())) {
                business = rewardLog.realTitle(item.getLevelTitle());
            } else {
                business = rewardLog.realTitle(item.getTitle());
            }
        } else {
            ParentRewardBusinessType businessType = ParentRewardBusinessType.of(item.getBusiness());
            if (businessType != null) {
                business = businessType.getValue();
            }
        }
        return business;
    }

    //今天获得奖励数量按学科分类展示
    private List<Map<String, Object>> generateRewardCountBySubject(List<ParentRewardLog> rewardLogs, StudentDetail studentDetail) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<Subject, Long> subjectGroupMap = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false)
                .stream()
                .collect(Collectors.toMap(GroupMapper::getSubject, GroupMapper::getId, (o1, o2) -> o1));
        List<Subject> subjectList = Arrays.asList(Subject.ENGLISH, Subject.MATH, Subject.CHINESE, Subject.UNKNOWN);
        List<String> subjectNameList = subjectList.stream().map(Subject::name).collect(Collectors.toList());
        for (Subject subject : subjectList) {
            List<ParentRewardLog> subjectRewards = rewardLogs.stream()
                    .filter(rewardLog -> {
                        ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                        return item != null && (subject.name().equals(item.getSubject()) || "UNKNOWN".equals(subject.name()) && !subjectNameList.contains(item.getSubject()));
                    })
                    .collect(Collectors.toList());
            if (subject == Subject.UNKNOWN && subjectRewards.size() <= 0) {
                break;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("subject", subject == Subject.UNKNOWN ? "拓展" : subject.getValue());
            map.put("count", subjectRewards.size());
            //未加入该学科班级
            if (subject != Subject.UNKNOWN && !subjectGroupMap.containsKey(subject)) {
                String recommendText = "未加入" + subject.getValue() + "老师班级，可通过自学进行同步知识巩固>";
                String recommendUrl = ProductConfig.getMainSiteBaseUrl() + "/zion/nova-report?subject=" + subject.name();
                map.put("recommend_text", recommendText);
                map.put("recommend_url", recommendUrl);
                setUseInfo(map, studentDetail, subject, SUBJECT_PRODUCT_MAP.get(subject));
            }
            list.add(map);
        }
        return list;
    }

    //xiaoU使用情况
    private void setUseInfo(Map<String, Object> map, StudentDetail studentDetail, Subject subject, String productType) {
        String useDesc = "";
        for (AppUseNumCalculateType calculateType : AppUseNumCalculateType.values()) {
            Map<String, Integer> numMap = businessVendorServiceClient.loadUseNum(calculateType, Collections.singletonList(productType), studentDetail);
            Integer num = SafeConverter.toInt(numMap.get(productType));
            if (num > 10 && StringUtils.isBlank(useDesc)) {
                String numStr = num < 10000 ? String.valueOf(num) : String.valueOf((num + 5000) / 10000) + "万";
                useDesc = "等" + numStr + "名" + CALCULATE_TYPE_MAP.get(calculateType) + "同学正在使用小U" + subject.getValue() + "自学";
                break;
            }
        }
        if (StringUtils.isNotBlank(useDesc)) {
            List<UserAvatar> userAvatarList = UserAvatar.getPrimaryStudentAvatars();
            Collections.shuffle(userAvatarList);
            List<String> avatarList = new ArrayList<>();
            userAvatarList.stream()
                    .limit(3)
                    .forEach(avatar -> avatarList.add(getUserAvatarImgUrl(avatar.getUrl())));
            map.put("use_desc", useDesc);
            map.put("avatar_list", avatarList);
        }
    }


    private List<Map<String, Object>> generateRewardHistory(List<ParentRewardLog> rewardLogs, StudentDetail studentDetail, Boolean showScoreLevel, Boolean sendAvailable, Map<String, Integer> recommendFlagMap) {
        List<Map<String, Object>> list = new ArrayList<>();
        Date startDate = DateUtils.addDays(DayRange.current().getStartDate(), -6);
        DayRange dayRange = DayRange.current();
        while (true) {
            if (dayRange.getStartDate().before(startDate)) {
                break;
            }
            List<ParentRewardLog> rewardLogList = new ArrayList<>();
            for (ParentRewardLog rewardLog : rewardLogs) {
                if (dayRange.contains(rewardLog.getCreateTime())) {
                    rewardLogList.add(rewardLog);
                }
            }
            rewardLogList = rewardLogList.stream()
                    .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                    .collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("count", rewardLogList.size());
            map.put("date", DateUtils.dateToString(dayRange.getStartDate(), "MM.dd"));
            if (!dayRange.equals(DayRange.current())) {
                recommendFlagMap = new HashMap<>();
            }
            map.put("subject_reward_list", generateSubjectRewardList(rewardLogList, recommendFlagMap, showScoreLevel, sendAvailable, studentDetail.getId()));

            //除了学科，其他所有奖励列表
            Map<String, ParentRewardCategory> categoryKeyMap = parentRewardBufferLoaderClient.getParentRewardCategoryMap()
                    .values()
                    .stream()
                    .collect(Collectors.toMap(ParentRewardCategory::getKey, Function.identity(), (o1, o2) -> o1));
            List<Map<String, Object>> rewardCategoryList = new ArrayList<>();
            //成长世界智慧岛-百科
            Map<String, Object> wisdomMap = new HashMap<>();
            wisdomMap.put("category_title", ParentRewardSubject.ENCYCLOPEDIA.getValue());
            wisdomMap.put("rewards", generateWisdomRewardList(rewardLogList, showScoreLevel, sendAvailable));
            rewardCategoryList.add(wisdomMap);

            //成长世界
            List<ParentRewardCategory> categoryList = new ArrayList<>();
            categoryList.add(categoryKeyMap.get("GROWTH_WORLD"));
            categoryList.add(categoryKeyMap.get("QUALITY"));
            categoryList.add(categoryKeyMap.get("COMPETITION"));
            categoryList.add(categoryKeyMap.get("STUDY_PLANNING"));
            categoryList.add(categoryKeyMap.get("PICTURE_BOOK"));
            categoryList.add(categoryKeyMap.get("ACTIVITY"));
            categoryList.add(categoryKeyMap.get("PARENT_PRACTICE"));
            categoryList.add(categoryKeyMap.get("ONLINE_MENTAL"));
            for (ParentRewardCategory category : categoryList) {
                Map<String, Object> categoryRewardMap = new HashMap<>();
                categoryRewardMap.put("category_title", category.getTitle());
                categoryRewardMap.put("rewards", generateCategoryRewardList(rewardLogList, showScoreLevel, sendAvailable, category.getKey()));
                rewardCategoryList.add(categoryRewardMap);
            }
            map.put("category_reward_list", rewardCategoryList);
            list.add(map);
            dayRange = dayRange.previous();
        }
        return list;
    }

    /**
     * 智慧岛列表
     */
    private List<Map<String, Object>> generateWisdomRewardList(List<ParentRewardLog> rewardLogs, Boolean showScoreLevel, Boolean sendAvailable) {
        List<Map<String, Object>> list = new ArrayList<>();
        rewardLogs.stream().sorted(Comparator.comparing(ParentRewardLog::getCreateTime))
                .filter(rewardLog -> {
                    ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                    return item != null && ParentRewardSubject.ENCYCLOPEDIA.name().equals(item.getSubject());
                })
                .forEach(rewardLog -> {
                    Map<String, Object> map = generateRewardMap(rewardLog, showScoreLevel, sendAvailable, "");
                    if (MapUtils.isNotEmpty(map)) {
                        list.add(map);
                    }
                });
        return list;
    }

    /**
     * 学科奖励列表
     */
    private List<Map<String, Object>> generateSubjectRewardList(List<ParentRewardLog> rewardLogs, Map<String, Integer> recommendFlagMap, Boolean showScoreLevel, Boolean sendAvailable, Long studentId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Subject subject : SUBJECT_LIST) {
            Map<String, Object> subjectMap = new HashMap<>();
            subjectMap.put("subject", subject.getValue());
            List<Map<String, Object>> subjectRewardList = new ArrayList<>();
            rewardLogs.stream()
                    .filter(rewardLog -> {
                        ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                        return item != null && subject.name().equals(item.getSubject());
                    })
                    .sorted(Comparator.comparing(ParentRewardLog::getCreateTime))
                    .forEach(rewardLog -> {
                        Map<String, Object> map = generateRewardMap(rewardLog, showScoreLevel, sendAvailable, "");
                        if (MapUtils.isNotEmpty(map)) {
                            subjectRewardList.add(map);
                        }
                    });
            subjectMap.put("rewards", subjectRewardList);

            //recommendFlag  0表示不用推荐，1用文案1，2用文案2
            int recommendFlag = SafeConverter.toInt(recommendFlagMap.get(subject.getValue()));
            if (CollectionUtils.isEmpty(subjectRewardList) && recommendFlag != 0) {
                String recommendTitle;
                String recommendText = "";
                if (recommendFlag == 1) {
                    switch (subject) {
                        case ENGLISH:
                            recommendText = "增加练习量，巩固薄弱点";
                            break;
                        case MATH:
                            recommendText = "检验薄弱环节，完善知识体系";
                            break;
                        case CHINESE:
                            recommendText = "夯实字词基础，强化课文理解";
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (subject) {
                        case ENGLISH:
                            recommendText = "回顾今日同步知识，检验课堂掌握情况";
                            break;
                        case MATH:
                            recommendText = "回顾今日课堂所学，巩固必考知识点";
                            break;
                        case CHINESE:
                            recommendText = "复习今日课本知识，完善文学语感培养";
                            break;
                        default:
                            break;
                    }
                }

                if (isPayUser(studentId, subject)) {
                    recommendTitle = "去小U" + subject.getValue() + "练习";
                } else {
                    recommendTitle = "开通小U" + subject.getValue();
                }
                String recommendUrl = "/zion/nova-report?subject=" + subject.name();
                subjectMap.put("recommend_title", recommendTitle);
                subjectMap.put("recommend_text", isStudentInActivityBlacklist() ? "本日未获得" + subject.getValue() + "奖励" : recommendText);
                subjectMap.put("recommend_url", isStudentInActivityBlacklist() ? "" : recommendUrl);
                subjectMap.put("recommend_icon", getCdnBaseUrlStaticSharedWithSep() + "/public/skin/parentMobile/images/new_icon/parent_reward_recommend.png");
            }
            list.add(subjectMap);
        }
        return list;
    }

    /**
     * 是否是付费用户
     */
    private boolean isPayUser(Long studentId, Subject subject) {
        boolean isPayUser = false;
        Map<SelfStudyType, DayRange> map = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentId, false);
        Map<SelfStudyType, Boolean> payMap = new HashMap<>();
        map.forEach((key, value) -> {
            if (value.getEndDate().after(DayRange.current().getEndDate())) {
                payMap.put(key, Boolean.TRUE);
            } else {
                payMap.put(key, Boolean.FALSE);
            }
        });
        switch (subject) {
            case ENGLISH:
                isPayUser = SafeConverter.toBoolean(payMap.get(SelfStudyType.AFENTI_ENGLISH));
                break;
            case MATH:
                isPayUser = SafeConverter.toBoolean(payMap.get(SelfStudyType.AFENTI_MATH));
                break;
            case CHINESE:
                isPayUser = SafeConverter.toBoolean(payMap.get(SelfStudyType.AFENTI_CHINESE));
                break;
            default:
                break;
        }
        return isPayUser;
    }

    private Map<String, Object> generateRewardMap(ParentRewardLog rewardLog, boolean showScoreLevel, boolean sendAvailable, String categoryKey) {
        Map<String, Object> map = new HashMap<>();
        ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
        if (item != null) {
            ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
            if (category != null && (StringUtils.isEmpty(categoryKey) || categoryKey.equals(category.getKey()))) {
                boolean expire = rewardLog.sendExpired() || rewardLog.receiveExpired();
                map.put("id", rewardLog.getId());
                map.put("key", rewardLog.getKey());
                map.put("type", rewardLog.getType());
                map.put("count", rewardLog.getCount());
                map.put("status", rewardLog.getStatus());
                map.put("title", item.getTitle());
                map.put("icon", category.getIconUrl());
                map.put("item_desc", getParentRewardDescription(rewardLog, item, category, showScoreLevel));
                map.put("create_time", DateUtils.dateToString(rewardLog.getCreateTime(), "MM月dd日 HH:mm"));
                map.put("expire", expire);
                map.put("send_url", item.getSecondaryPageUrl());
                map.put("view_url", rewardLog.realRedirectUrl(item.getRedirectUrl()));
                if (rewardLog.getStatus() == ParentRewardStatus.INIT.getType() && !sendAvailable && item.getSendExpireDays() != 0) {
                    map.put("send_start_time", parentRewardService.timeToSendReward());
                }
            }
        }
        return map;
    }

    /**
     * 品德奖励列表
     */
    private List<Map<String, Object>> generateCategoryRewardList(List<ParentRewardLog> rewardLogs, Boolean showScoreLevel, Boolean sendAvailable, String categoryKey) {
        List<Map<String, Object>> list = new ArrayList<>();
        rewardLogs.stream().sorted(Comparator.comparing(ParentRewardLog::getCreateTime))
                .filter(rewardLog -> {
                    ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                    return item != null && StringUtils.isEmpty(item.getSubject());
                })
                .forEach(rewardLog -> {
                    Map<String, Object> map = generateRewardMap(rewardLog, showScoreLevel, sendAvailable, categoryKey);
                    if (MapUtils.isNotEmpty(map)) {
                        list.add(map);
                    }
                });
        return list;
    }

    private List<Map<String, Object>> generateExplainList(StudentDetail studentDetail, List<ParentRewardLog> rewardLogs, Map<String, Integer> recommendFlagMap) {
        List<Map<String, Object>> list = new ArrayList<>();

        for (Subject subject : SUBJECT_LIST) {
            List<ParentRewardLog> logList = rewardLogs.stream()
                    .filter(rewardLog -> {
                        ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
                        return item != null && subject.name().equals(item.getSubject());
                    }).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("subject", subject.getValue());
            map.put("explain", generateExplain(studentDetail, subject, logList, recommendFlagMap));
            list.add(map);
        }
        return list;
    }

    //是否有未做的作业
    private boolean hasUnFinishHomework(Set<Long> groupIds, StudentDetail studentDetail) {
        String homeworkId = newHomeworkReportService.fetchStudentNewestUnfinishedHomework(studentDetail.getId(), groupIds);
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(homeworkId);
        Date date = DateUtils.addDays(new Date(), -7);
        return newHomework != null && newHomework.getCreateAt().after(date);
    }

    //作业奖励数量
    private long homeworkRewardCount(List<ParentRewardLog> rewardLogs, String subjectName) {
        return rewardLogs.stream().filter(rewardLog -> {
            ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
            ParentRewardCategory category = null;
            if (item != null) {
                category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
            }
            return item != null && subjectName.equals(item.getSubject()) && category != null && "HOMEWORK".equals(category.getKey());
        }).count();
    }

    //自学奖励数量
    private long selfStudyRewardCount(List<ParentRewardLog> rewardLogs, String subjectName) {
        return rewardLogs.stream().filter(rewardLog -> {
            ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardLog.getKey());
            ParentRewardCategory category = null;
            if (item != null) {
                category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
            }
            return item != null && subjectName.equals(item.getSubject()) && category != null && "SELF_STUDY".equals(category.getKey());
        }).count();
    }

    private String generateExplain(StudentDetail studentDetail, Subject subject, List<ParentRewardLog> rewardLogs, Map<String, Integer> recommendFlagMap) {
        int recommendFlag = 0;
        long homeworkRewardCount = homeworkRewardCount(rewardLogs, subject.name());
        long selfStudyRewardCount = selfStudyRewardCount(rewardLogs, subject.name());
        boolean hasHomeworkReward = (homeworkRewardCount > 0);
        boolean hasSelfStudyReward = (selfStudyRewardCount > 0);
        StringBuilder explain = new StringBuilder();
        Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false)
                .stream()
                .filter(groupMapper -> subject.name().equals(groupMapper.getSubject().name()))
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());

        boolean hasUnFinishHomework = hasUnFinishHomework(groupIds, studentDetail);
        boolean hasFinishHomework = ParentRewardCacheManager.INSTANCE.isStudentFinishHomeworkToday(studentDetail.getId(), subject.name());
        boolean hasCheckHomework = groupIds.stream().anyMatch(ParentRewardCacheManager.INSTANCE::isTeacherCheckHomeworkToday);

        boolean hasHomework = (hasUnFinishHomework || hasFinishHomework || hasCheckHomework);
        //是否有班级
        if (CollectionUtils.isNotEmpty(groupIds)) {
            if (hasHomework) {
                if (hasHomeworkReward) {
                    explain.append(subject.getValue()).append("作业表现很好，获得了").append(homeworkRewardCount).append("个奖励，值得鼓励");
                } else {
                    if (hasSelfStudyReward) {
                        explain.append("自学表现不错，继续坚持养成自学好习惯吧！");
                    } else {
                        explain.append("未获得奖励，请帮孩子分析原因");
                        recommendFlag = 1;
                    }
                }
            } else {
                //没有作业&&有自学奖励
                if (hasSelfStudyReward) {
                    switch (subject) {
                        case ENGLISH:
                            explain.append("没有作业但主动自学，离国际化小学生又进一步！");
                            break;
                        case MATH:
                            explain.append("没有作业但主动自学，爱数学的孩子成绩一定好");
                            break;
                        case CHINESE:
                            explain.append("没有作业但主动自学，口才一定棒棒哒");
                            break;
                        default:
                            break;
                    }
                } else {
                    explain.append("今日没有作业，建议自学巩固课堂内容");
                }
                recommendFlag = 2;
            }
        } else {
            if (hasSelfStudyReward) {
                explain.append("自学效果不错！获得了").append(selfStudyRewardCount).append("个奖励");
            } else {
                explain.append("未加入").append(subject.getValue()).append("班级，").append("需自学练习").append(subject.getValue()).append("知识");
                recommendFlag = 2;
            }
        }
        recommendFlagMap.put(subject.getValue(), recommendFlag);
        return explain.toString();
    }

    private void lastTermInfo(MapMessage mapMessage, StudentDetail studentDetail) {
        Set<String> ids = new HashSet<>();
        ids.add(StudentRewardSendCount.generateId(studentDetail.getId(), ParentRewardHelper.lastTermDateRange().getStartDate()));
        Map<String, StudentRewardSendCount> studentRewardSendCountMap = parentRewardLoader.getStudentRewardSendCount(ids);
        long itemCount = studentRewardSendCountMap.values().stream().mapToLong(StudentRewardSendCount::getItemCount).sum();
        if (itemCount > 0 && ParentRewardHelper.showLastTermInfo() && !ParentRewardCacheManager.INSTANCE.parentHasShowLastTermInfo(currentUserId(), studentDetail.getId())) {
            mapMessage.add("last_item_count", itemCount);
            mapMessage.add("last_term_rank", parentRewardLoader.getParentRewardStudentTermRank(studentDetail.getId(), ParentRewardHelper.lastTermDateRange()));
            ParentRewardCache.getPersistenceCache().incr(ParentRewardHelper.lastTermParentStatisticsFlag(currentUserId(), studentDetail.getId()), 1, 1, ParentRewardHelper.termCacheExpireTime());
        }
    }

    //家庭关爱值、家庭关爱值等级、下一个等级人数
    private void handleItemCount(MapMessage mapMessage, Long studentId, Clazz clazz) {
        long itemCount;
        int level;
        Set<String> ids = new HashSet<>();
        if (clazz != null) {
            Map<Long, List<User>> studentMap = studentLoaderClient.loadClazzStudents(Collections.singletonList(clazz.getId()));
            List<User> students = studentMap.get(clazz.getId());
            students.forEach(student -> ids.add(StudentRewardSendCount.generateId(student.getId(), ParentRewardHelper.currentTermDateRange().getStartDate())));
        } else {
            ids.add(StudentRewardSendCount.generateId(studentId, ParentRewardHelper.currentTermDateRange().getStartDate()));
        }
        List<StudentRewardSendCount> sendCounts = new ArrayList<>(parentRewardLoader.getStudentRewardSendCount(ids).values());
        itemCount = sendCounts.stream().filter(sendCount -> Objects.equals(sendCount.getStudentId(), studentId)).mapToLong(StudentRewardSendCount::getItemCount).sum();
        level = ParentRewardScoreLevelCalculator.getLevel(itemCount);

        //下一个等级的学生，需要随机返回三个学生的信息
        List<Long> nextLevelStudentIds = ParentRewardScoreLevelCalculator.nextLevelStudentIds(level, sendCounts);
        if (CollectionUtils.isNotEmpty(nextLevelStudentIds)) {
            Collections.shuffle(nextLevelStudentIds);
            List<Long> randomClazzMateIds = nextLevelStudentIds.stream().limit(3).collect(Collectors.toList());
            List<Student> clazzMates = new ArrayList<>(studentLoaderClient.loadStudents(randomClazzMateIds).values());
            List<Map<String, Object>> clazzMatesList = new ArrayList<>();
            for (Student student : clazzMates) {
                Map<String, Object> map = new HashMap<>();
                String name = student.fetchRealname();
                String avatar = getUserAvatarImgUrl(student.fetchImageUrl());
                map.put("next_level_student_name", name);
                map.put("next_level_student_avatar", avatar);
                clazzMatesList.add(map);
            }
            mapMessage.add("next_level_count", nextLevelStudentIds.size());
            mapMessage.add("next_level_students", clazzMatesList);
        }

        CacheObject<Object> cacheObject = ParentRewardCache.getPersistenceCache().get("PARENT_REWARD_LEVEL_REMIND_" + studentId + "_" + currentUserId());
        if (cacheObject == null || cacheObject.getValue() == null) {
            ParentRewardCache.getPersistenceCache().set("PARENT_REWARD_LEVEL_REMIND_" + studentId + "_" + currentUserId(), (int) ((ParentRewardHelper.currentTermDateRange().getEndTime() - Instant.now().toEpochMilli()) / 1000), level);
            mapMessage.add("new_level", false);
        } else {
            int oldLevel = SafeConverter.toInt(cacheObject.getValue());
            //升到3级以上才会弹窗
            if (oldLevel <= 2 && level > 2) {
                mapMessage.add("new_level", true);
                ParentRewardCache.getPersistenceCache().set("PARENT_REWARD_LEVEL_REMIND_" + studentId + "_" + currentUserId(), (int) ((ParentRewardHelper.currentTermDateRange().getEndTime() - Instant.now().toEpochMilli()) / 1000), level);
            } else {
                mapMessage.add("new_level", false);
            }
        }

        mapMessage.add("item_count", itemCount);
        mapMessage.add("level", level);
    }


    ////////////////////////
    //以下是旧的家长奖励，即将下线

    /**
     * 创建奖励
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "createTask.vpage", method = RequestMethod.GET)
    public String createTask(Model model) {
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
        }
        setRouteParameter(model);

        Long studentId = getRequestLong("sid");
        model.addAttribute("isNotArranged", !businessStudentServiceClient.isCurrentMonthIntegralMissionArranged(studentId));

        return "parentmobile/createTask";
    }

    /**
     * 孩子心愿接口
     */
    @RequestMapping(value = "childWish.vpage", method = RequestMethod.GET)
    public String childWish(Model model) {

        Long studentId = getRequestLong("sid");
        Integer page = getRequestInt("cp", 1);
        Long parentId = currentUserId();
        setRouteParameter(model);

        String pageAddr = "parentmobile/childWish";

        try {
            if (parentId == null || studentId == 0) {
                model.addAttribute("result", MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            if (currentParent() == null) {
                model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
                return pageAddr;
            }
            if (!studentIsParentChildren(parentId, studentId)) {
                model.addAttribute("result", MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            // 学生信息
            Student student = studentLoaderClient.loadStudent(studentId);

            // 进行中
            Pageable pageable = new PageRequest(page - 1, 10);
//            Page<Mission> missionPage = missionLoaderClient.loadStudentMissions(studentId)
//                    .filter(t -> t.getState() == MissionState.ONGOING)
//                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
//                    .toPage(pageable);
//            List<MissionMapper> mapperList = missionPage.getContent()
//                    .stream()
//                    .map(t -> missionLoaderClient.transformMission(t, studentId, UserType.PARENT))
//                    .collect(Collectors.toList());
//            Page<MissionMapper> inProgressWishes = new UtopiaPageImpl<>(mapperList, pageable, missionPage.getTotalElements());
//
//            List<MissionMapper> inProgressWishesList = new ArrayList<>();
//
//            if (!CollectionUtils.isEmpty(inProgressWishes.getContent())) {
//                inProgressWishes.forEach(m -> {
//                    m.setImg(getUserAvatarImgUrl(m.getImg()));
//                    inProgressWishesList.add(m);
//                });
//            }

            // 新的愿望
//            pageable = new UtopiaPageRequest(page - 1, 10);
            Page<Mission> missionPage = missionLoaderClient.loadStudentMissions(studentId)
                    .filter(t -> t.getState() == MissionState.WISH)
                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
                    .toPage(pageable);
            List<MissionMapper> mapperList = missionPage.getContent()
                    .stream()
                    .map(t -> missionLoaderClient.transformMission(t, studentId, UserType.PARENT))
                    .collect(Collectors.toList());
            Page<MissionMapper> newWishes = new PageImpl<>(mapperList, pageable, missionPage.getTotalElements());

            List<MissionMapper> newWishList = new ArrayList<>();

            if (!CollectionUtils.isEmpty(newWishes.getContent())) {
                newWishes.forEach(m -> {
                    m.setImg(getUserAvatarImgUrl(m.getImg()));
                    newWishList.add(m);
                });
            }

            List<MissionMapper> totalWishList = new ArrayList<>();
            totalWishList.addAll(newWishList);
//            totalWishList.addAll(inProgressWishesList);

            model.addAttribute(
                    "result",
                    MapMessage.successMessage()
                            .add("wishes", totalWishList)
                            .add("studentName", student.fetchRealname())
            );

        } catch (Exception ex) {
            log.error("Parent {} get student {} wish failed.", parentId, studentId, ex);
            model.addAttribute("result", MapMessage.errorMessage("查询失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
        }

        return pageAddr;
    }

    /**
     * 家长奖励进行中
     */
    @RequestMapping(value = "getmissions.vpage", method = RequestMethod.GET)
    public String parentGetMissions(Model model) {
        Long studentId = getRequestLong("sid");
        Integer page = getRequestInt("cp", 1);
        Long parentId = currentUserId();
        setRouteParameter(model);
        try {
            if (parentId == null || studentId == 0) {
                model.addAttribute("result", MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return "parentmobile/getmissions";
            }

            if (currentParent() == null) {
                model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
                return "parentmobile/getmissions";
            }
            if (!studentIsParentChildren(parentId, studentId)) {
                model.addAttribute("result", MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return "parentmobile/getmissions";
            }

            Pageable pageable = new PageRequest(page - 1, 10);
            Page<Mission> missionPage = missionLoaderClient.loadStudentMissions(studentId)
                    .filter(t -> t.getState() == MissionState.ONGOING)
                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
                    .toPage(pageable);
            List<MissionMapper> mapperList = missionPage.getContent()
                    .stream()
                    .map(t -> missionLoaderClient.transformMission(t, studentId, UserType.PARENT))
                    .collect(Collectors.toList());
            Page<MissionMapper> missions = new PageImpl<>(mapperList, pageable, missionPage.getTotalElements());
            List<MissionMapper> missionsList = new ArrayList<>();

            missions.forEach(m -> {
                if (m != null) {
                    m.setImg(getUserAvatarImgUrl(m.getImg()));
                    missionsList.add(m);
                }
            });
            model.addAttribute("result", MapMessage.successMessage().add("missions", missionsList));
        } catch (Exception ex) {
            log.error("Parent {} get student {} missions failed.", parentId, studentId, ex);
            model.addAttribute("result", MapMessage.errorMessage("查询失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
        }
        return "parentmobile/getmissions";
    }

    /**
     * 家长奖励除了愿望的全部
     */
    @RequestMapping(value = "getfootprint.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage parentGetFootPrint() {
        Long studentId = getRequestLong("sid");
        Integer page = getRequestInt("cp", 1); // 默认是第一页
        Long parentId = currentUserId();
        try {
            if (studentId == 0) {
                return MapMessage.errorMessage("参数错误").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            if (!studentIsParentChildren(parentId, studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            Pageable pageable = new PageRequest(page - 1, 10);
            Page<Mission> missionPage = missionLoaderClient.loadStudentMissions(studentId)
                    .filter(t -> t.getState() != MissionState.WISH)
                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
                    .toPage(pageable);
            List<MissionMapper> mapperList = missionPage.getContent()
                    .stream()
                    .map(t -> missionLoaderClient.transformMission(t, studentId, UserType.PARENT))
                    .collect(Collectors.toList());

            Page<MissionMapper> missions = new PageImpl<>(mapperList, pageable, missionPage.getTotalElements());
            if (CollectionUtils.isEmpty(missions.getContent())) {
                return MapMessage.errorMessage("查询失败").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            } else {
                List<MissionMapper> missionsList = new ArrayList<>();
                missions.forEach(m -> {
                    m.setImg(getUserAvatarImgUrl(m.getImg()));
                    missionsList.add(m);
                });
                return MapMessage.successMessage().add("missions", missionsList);
            }
        } catch (Exception ex) {
            log.error("Parent {} get student {} foot print failed.", parentId, studentId, ex);
            return MapMessage.errorMessage("查询失败").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }


    /**
     * 创建目标
     */
    @RequestMapping(value = "setmission.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentSetMissions() {
        Long parentId = currentUserId();
        Long studentId = getRequestLong("sid");
        WishType wishType = WishType.of(getRequestString("wishType"));
        MissionType missionType = MissionType.of(getRequestString("missionType"));
        String wish = StringUtils.filterEmojiForMysql(getRequestString("wish"));
        String mission = StringUtils.filterEmojiForMysql(getRequestString("mission"));
        Integer totalCount = getRequestInt("totalCount");
        Long missionId = getRequestLong("missionId");

        try {
            if (studentId == 0 || wishType == null || missionType == null
                    || StringUtils.isBlank(mission) || totalCount < 1 || totalCount > 20) {
                return MapMessage.errorMessage("参数不正确").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            if (!studentIsParentChildren(parentId, studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
            if (mission.length() > 20) {
                return MapMessage.errorMessage("目标内容过长，请重新输入").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            MapMessage mesg = atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .keyPrefix("MAKE_WISH_OR_MISSION").keys(studentId).proxy()
                    .parentSetMission(parentId, studentId, wishType, wish, totalCount, mission, missionType, missionId);
            if (!mesg.isSuccess()) {
                return MapMessage.errorMessage(mesg.getInfo()).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            } else {
                return MapMessage.successMessage();
            }
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        } catch (Exception ex) {
            log.error("Parent {} set missions failed.", parentId, ex);
            return MapMessage.errorMessage("设置任务失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    /**
     * 更新进度
     */
    @RequestMapping(value = "updateprogress.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentUpdateProgress() {
        Long parentId = currentUserId();
        Long missionId = getRequestLong("missionId", -1);
        try {
            if (parentId == 0 || missionId < 0) {
                return MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            MapMessage mesg = atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .proxy().parentUpdateProgress(parentId, missionId);
            if (!mesg.isSuccess()) {
                return MapMessage.errorMessage(mesg.getInfo()).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            } else {
                return MapMessage.successMessage();
            }
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        } catch (Exception ex) {
            log.error("Parent {} update mission progress failed.", parentId, ex);
            return MapMessage.errorMessage("更新进度失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    /**
     * 更新完成
     */
    @RequestMapping(value = "updatecomplete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentUpdateComplete() {
        Long parentId = currentUserId();
        Long missionId = getRequestLong("missionId", -1);
        try {
            if (parentId == 0 || missionId < 0) {
                return MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            MapMessage mesg = atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .proxy().parentUpdateComplete(parentId, missionId);
            if (!mesg.isSuccess()) {
                return MapMessage.errorMessage(mesg.getInfo()).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            } else {
                return MapMessage.successMessage();
            }
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        } catch (Exception ex) {
            log.error("Parent {} update mission progress failed.", parentId, ex);
            return MapMessage.errorMessage("发奖励失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    /**
     * 上传头像
     */
    @RequestMapping(value = "uploadpicture.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadMissionPicture() {
        Long parentId = currentUserId();
        Long missionId = getRequestLong("missionId", -1);
        String filedata = getRequestString("filedata");
        try {
            if (parentId == 0 || missionId < 0 || StringUtils.isEmpty(filedata)) {
                return MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            Mission mission = missionLoaderClient.loadMission(missionId);
            if (mission == null) {
                return MapMessage.errorMessage("mission is null").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            String filename_ori = mission.getImg();
            String filename = missionPictureUploader.upload(missionId, filedata);
            if (filename == null) {
                logger.warn("Upload mission picture failed. parentId {}, missionId {}", parentId, missionId);
                throw new RuntimeException("上传失败");
            }
            if (businessStudentServiceClient.updateMissionImg(missionId, filename)) {
                missionPictureUploader.delete(filename_ori);
                return MapMessage.successMessage().add("filename", getUserAvatarImgUrl(filename));
            } else {
                logger.warn("Update mission picture failed. parentId {}, missionId {}", parentId, missionId);
                throw new RuntimeException("上传失败");
            }
        } catch (Exception ex) {
            logger.warn("Upload mission picture failed. parentId {}, missionId {}", parentId, missionId, ex);
            return MapMessage.errorMessage(ex.getMessage()).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    @RequestMapping(value = "isIntegralArranged.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage isIntegralArranged() {
        Long parentId = currentUserId();
        Long studentId = getRequestLong("sid");
        try {
            if (studentId == 0) {
                return MapMessage.errorMessage("参数错误").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            if (!studentIsParentChildren(parentId, studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            boolean arranged = businessStudentServiceClient.isCurrentMonthIntegralMissionArranged(studentId);
            return MapMessage.successMessage().add("arranged", arranged);
        } catch (Exception ex) {
            log.error("Parent {} student {}: index failed.", parentId, studentId, ex);
            return MapMessage.errorMessage(ex.getMessage()).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
    }
}

