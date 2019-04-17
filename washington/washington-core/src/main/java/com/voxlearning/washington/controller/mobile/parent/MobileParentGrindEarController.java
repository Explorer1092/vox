package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.piclisten.api.GrindEarService;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.cache.PiclistenCache;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.constants.UserConstants;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenReportDayResult;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRange;
import com.voxlearning.utopia.service.vendor.api.entity.StudentGrindEarRecord;
import com.voxlearning.utopia.temp.GrindEarActivity;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 声明:
 * 活动就这么几天, 实现很粗糙,快速实现了
 * 完善了一版实现
 *
 * @author jiangpeng
 * @since 2016-10-26 下午1:29
 **/
@Controller
@RequestMapping(value = "/parentMobile/grindear")
@Slf4j
public class MobileParentGrindEarController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = GrindEarService.class) private GrindEarService grindEarService;
    @ImportService(interfaceClass = ParentSelfStudyService.class) ParentSelfStudyService parentSelfStudyService;
    @ImportService(interfaceClass = ParentRewardService.class) private ParentRewardService parentRewardService;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;


    @RequestMapping(value = "/data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage data() {

        User user = currentUser();
//        User parent = parentLoaderClient.loadStudentKeyParent(333878710L).getParentUser();
        if (user == null || ( !user.isParent() && !user.isStudent() ) )
            return noLoginResult;

        Long studentId;
        if (user.isParent())
            studentId = getRequestLong("sid");
        else
            studentId = user.getId();
//        Long studentId = 333878710L;
        if (studentId == 0)
            return MapMessage.errorMessage("no sid");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage("学生 id 错误");
        StudentGrindEarRecord studentGrindEarRecord = grindEarService.loadGrindEarRecord(studentId);
        List<Date> recordDateList;
        if (studentGrindEarRecord != null)
            recordDateList = studentGrindEarRecord.getDateList();
        else
            recordDateList = new ArrayList<>();
        Set<DayRange> finishDayRangeSet = recordDateList.stream().map(date -> DayRange.newInstance(date.getTime())).collect(Collectors.toSet());

        List<Map<String, Object>> completeList = new ArrayList<>();
        Boolean todayIsFinish = false;
        DayRange todayDayRange = DayRange.current();
        Set<Integer> alreadyHadDaySet = new HashSet<>();
        LinkedList<DayRange> continuityDayRangeList = new LinkedList<>();
        Boolean startWait = false;
        for (int i = 0; i < GrindEarActivity.dayRangeList.size(); i++) {
            DayRange dayRange = GrindEarActivity.dayRangeList.get(i);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date", dayRange.getDay());
            if (dayRange.getDay() == 1) {
                map.put("month", dayRange.getMonth());
            }
            if (startWait) {
                map.put("status", GrindEarActivity.wait);
                completeList.add(map);
                continue;
            }
            int status;
            if (dayRange.getStartDate().before(todayDayRange.getStartDate())) {
                if (finishDayRangeSet.contains(dayRange)) {
                    status =  GrindEarActivity.finish;
                } else
                    status = GrindEarActivity.dead;
            } else if (dayRange.equals(todayDayRange)) {
                if (finishDayRangeSet.contains(dayRange)) {
                    todayIsFinish = true;
                    status = GrindEarActivity.finish;
                } else
                    status = GrindEarActivity.wait;
                startWait = true;
            } else
                status = GrindEarActivity.wait;
            map.put("status", status);
            if (status == GrindEarActivity.finish){//判断是否有家长奖励
                if (continuityDayRangeList.size() > 0  && !continuityDayRangeList.getLast().equals(dayRange.previous())) {
                    continuityDayRangeList.clear();
                }
                continuityDayRangeList.addLast(dayRange);
                Integer rewardDay = continuityDayRangeList.size();
                if (GrindEarActivity.days2RewardTypeMap.containsKey(rewardDay) &&!alreadyHadDaySet.contains(rewardDay)){
                    alreadyHadDaySet.add(rewardDay);
                    map.put("reward_key", rewardDay);
                    map.put("reward_is_send", parentRewardIsSend(studentId, rewardDay));
                }

            }
            completeList.add(map);
        }
        Boolean showRankEntry = studentHasRange(studentDetail);
        AlpsFuture<Boolean> todayIntegralIsSendFuture = grindEarService.todayIntegralIsSend(studentId, todayDayRange);
        MapMessage mapMessage = MapMessage.successMessage();
        if (showRankEntry) {
            Integer studentRank = grindEarService.loadStudentRank(studentDetail.getClazz().getSchoolId(), studentId);
            mapMessage.add("finish_day_rank", studentRank);
        }
        String status;
        Boolean isSend = todayIntegralIsSendFuture.getUninterruptibly();
        if (!isSend){
            if (todayIsFinish)
                status = "ok";
            else
                status = "not_yet";
        }else
            status = "none";

        String nowDay = DateUtils.dateToString(todayDayRange.getStartDate(), "yyyy.MM.dd");
        return mapMessage.add("week", GrindEarActivity.weekList)
                .add("complete_detail", completeList).add("now_date", nowDay)
                .add("student_name", studentDetail.fetchRealname())
                .add("finish_day_count",studentGrindEarRecord == null ? 0 : studentGrindEarRecord.dayCount())
                .add("show_rank_entry", showRankEntry)
                .add("reward_integral_status", status)
                .add("in_period", GrindEarActivity.isInActivityPeriod());
    }


    @RequestMapping(value = "/parent_reward/send.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendParentReward() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return MapMessage.errorMessage("no sid");
        if (!GrindEarActivity.isInActivityPeriod())
            return MapMessage.errorMessage("活动已过期，不能发送奖励了哦！");
        int day = getRequestInt("reward_key");
        String rewardKey = GrindEarActivity.days2RewardTypeMap.get(day);
        if (StringUtils.isBlank(rewardKey)){
            return MapMessage.errorMessage("没有此项奖励哦！");
        }
        StudentGrindEarRecord studentGrindEarRecord = grindEarService.loadGrindEarRecord(studentId);
        try {
            return AtomicLockManager.getInstance().wrapAtomic(this).keyPrefix("grindearParentReward")
                   .keys(studentId, rewardKey).proxy().sendParentReward(day, studentId, parent.getId(), parentRewardService, studentGrindEarRecord);
        }catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请稍候重试");
        }
    }


    private MapMessage sendParentReward(Integer day, Long studentId, Long parentId, ParentRewardService parentRewardService, StudentGrindEarRecord record){
        // 需要判断这个学生是否真的有这个奖励
        if (record == null || CollectionUtils.isEmpty(record.getDateList()))
            return MapMessage.errorMessage("您还有没该奖励呢！");
        Set<Integer> hadRewardDaySet = hadRewardDay(record.getDateList());
        if (!hadRewardDaySet.contains(day))
            return MapMessage.errorMessage("您还有没该奖励呢！");
        //奖励是否发放过了
        if (parentRewardIsSend(studentId, day)) {
            return MapMessage.errorMessage("该奖励已经发放过啦！");
        }
        String rewardKey = GrindEarActivity.days2RewardTypeMap.get(day);
        parentRewardService.generateParentReward(studentId, rewardKey, null);
        parentRewardService.sendParentReward(parentId, studentId, Collections.singleton(rewardKey));
        setParentRewardSend(studentId, day);
        // FIXME: 2017/8/23 如何判断发送成功
        return MapMessage.successMessage();
    }

    private static String keyPrefix = "grindEarBigParentReward18_";
    private void setParentRewardSend(Long studentId, Integer day){
        String key = generateParentRewardKey(studentId, day);
        int expireTime = Long.valueOf(GrindEarActivity.lastDate.getTime() / 1000).intValue();
        PiclistenCache.getPersistenceCache().set(key, expireTime, "true");
    }

    private String generateParentRewardKey(Long studentId, Integer day){
        return keyPrefix + studentId + "_" +day;
    }

    private boolean parentRewardIsSend(Long studentId, Integer day){
        Map<Integer, Boolean> map = parentRewardsIsSend(studentId, Collections.singleton(day));
        if (map == null)
            return false;
        Boolean aBoolean = map.get(day);
        return aBoolean != null && aBoolean;
    }

    private Map<Integer, Boolean> parentRewardsIsSend(Long studentId, Collection<Integer> days){
        Map<Integer, Boolean> map = new HashMap<>();
        for (Integer day : days) {
            CacheObject<Object> objectCacheObject = PiclistenCache.getPersistenceCache().get(generateParentRewardKey(studentId, day));
            if (objectCacheObject != null && objectCacheObject.getValue() != null && SafeConverter.toBoolean(objectCacheObject.getValue()))
                map.put(day, true);
            else
                map.put(day, false);
        }
        return map;
    }



    private Boolean studentHasRange(StudentDetail studentDetail){
       return studentDetail != null && studentDetail.isPrimaryStudent() && studentDetail.getClazz() != null
               && !studentDetail.getClazz().isTerminalClazz() && !studentDetail.getClazz().getSchoolId().equals(35204L)
                && !studentDetail.getClazz().getSchoolId().equals(2000L);
    }

    @RequestMapping(value = "/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rank() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return MapMessage.errorMessage("no sid");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage("sid error");
        if (!studentHasRange(studentDetail))
            return MapMessage.errorMessage("no range");
        int currentPage = getRequestInt("currentPage", 1);
        int pageSize = 100;
        if (RuntimeMode.lt(Mode.STAGING))
            pageSize = 10;
        Pageable page = new PageRequest(currentPage - 1, pageSize);
        Page<StudentGrindEarRange> studentGrindEarRangePage = grindEarService.loadSchoolRangePage(page, studentDetail.getClazz().getSchoolId());
        List<StudentGrindEarRange> studentGrindEarRanges = studentGrindEarRangePage.getContent().stream().filter(t -> t.getRank() <= 100).collect(Collectors.toList());
        List<Long> studentIds = studentGrindEarRanges.stream().map(StudentGrindEarRange::getStudentId).collect(Collectors.toList());
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);
        List<StudentGrindEarRange> rangeList = new ArrayList<>();
        studentGrindEarRanges.forEach((StudentGrindEarRange t) -> {
            Long sid = t.getStudentId();
            StudentDetail sd = studentDetailMap.get(sid);
            if (sd == null)
                return;
            if (sd.getClazz() == null)
                t.setClazzName("");
            else
                t.setClazzName(sd.getClazz().formalizeClazzName());
            t.setStudentName(sd.fetchRealname());
            rangeList.add(t);
        });
        return MapMessage.successMessage().add("rank_list", rangeList);
    }

    @RequestMapping(value = "/rank_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rankData() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return MapMessage.errorMessage("no sid");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage("sid error");
        AlpsFuture<PicListenReportDayResult> picListenReportDayResultAlpsFuture = parentSelfStudyService.loadReportDayResult(studentId, DayRange.current().previous());
        CacheObject<Object> objectCacheObject = PiclistenCache.getPersistenceCache().get(grindEarService.waiyanKey(studentId));
        boolean isWaiyan = objectCacheObject != null && objectCacheObject.getValue() != null && SafeConverter.toBoolean(objectCacheObject.getValue());
        Long studentCount = grindEarService.grindEarStudentCount();
        PicListenReportDayResult reportDayResult = picListenReportDayResultAlpsFuture.getUninterruptibly();
        return MapMessage.successMessage().add("national_total", studentCount)
                .add("student_name", studentDetail.fetchRealname())
                .add("student_time", millisSeconds2String(reportDayResult.getLearnTime()))
                .add("is_over_national", reportDayResult.getLearnTime() >519000)
                .add("is_waiyan", isWaiyan);

    }

    private String millisSeconds2String(Long millisSeconds){
        if (millisSeconds == null)
            return "0分0秒";
        long l = millisSeconds / 1000;
        return l/60 + "分" + l%60 + "秒";
    }


    @RequestMapping(value = "/today_integral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getIntegral() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return MapMessage.errorMessage("no sid");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage("sid error");
        if (!GrindEarActivity.isInActivityPeriod())
            return MapMessage.errorMessage("活动已过期，不能领取学豆了哦！");
        DayRange current = DayRange.current();
        StudentGrindEarRecord studentGrindEarRecord = grindEarService.loadGrindEarRecord(studentId);
        if (studentGrindEarRecord == null || CollectionUtils.isEmpty(studentGrindEarRecord.getDateList()))
            return MapMessage.successMessage().add("status", "not_yet");
        Set<DayRange> finishDayRanges = studentGrindEarRecord.getDateList().stream().map(t -> DayRange.newInstance(t.getTime())).collect(Collectors.toSet());
        if (!finishDayRanges.contains(current))
            return MapMessage.successMessage().add("status", "not_yet");
        try {
            Integer integral = AtomicLockManager.getInstance().wrapAtomic(grindEarService).keyPrefix("grindearIntegral").keys(studentId, current.toString())
                    .proxy().sendIntegral(studentDetail, current);
            if (integral == null)
                return MapMessage.successMessage().add("status", "none");
            return MapMessage.successMessage().add("status", "ok").add("integral_count", integral);
        }catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请稍候重试");
        }

    }



    @RequestMapping(value = "/book_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage bookList() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0L)
            return MapMessage.errorMessage("您没有孩子哦!");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage("学生数据错误");
        String sys = getRequestString("sys");
        if (StringUtils.isBlank(sys))
            return MapMessage.errorMessage("系统参数错误");
        Set<String> alreadyShowBookIdSet = new HashSet<>();
        String cdnBaseUrlStaticSharedWithSep = getCdnBaseUrlStaticSharedWithSep();
        List<Map<String, Object>> maps = parentSelfStudyPublicHelper.recommendPicListenBook(studentDetail, parent, sys);
        Map<Subject, List<Map<String, Object>>> subjectListMap = maps.stream().
                peek(t -> {
                    t.put("img", cdnBaseUrlStaticSharedWithSep + t.get("img"));
                    alreadyShowBookIdSet.add(SafeConverter.toString(t.get("book_id")));
                })
                .collect(Collectors.groupingBy(t -> Subject.valueOf(SafeConverter.toString(t.get("subject")))));
        MapMessage mapMessage = MapMessage.successMessage().add("book_list", subjectListMap);
        if (studentDetail.getClazz() != null
                && !studentDetail.getClazz().isTerminalClazz()
                && studentDetail.isPrimaryStudent()
           ){
            mapMessage.add("clazz", studentDetail.getClazzLevelAsInteger());
        }
        List<Map<String, Object>> buyedBookMapList = new ArrayList<>();
        Map<String, DayRange> dayRangeMap = picListenCommonService.parentBuyBookPicListenLastDayMap(parent.getId(), false);
        Set<String> buyBookIdSet = dayRangeMap.keySet();
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(buyBookIdSet);
        dayRangeMap.forEach((v, k) -> {
            if (alreadyShowBookIdSet.contains(v))
                return;
            NewBookProfile newBookProfile = bookProfileMap.get(v);
            if (newBookProfile == null)
                return;
            Map<String, Object> map = new HashMap<>();
            map.put("img", cdnBaseUrlStaticSharedWithSep + newBookProfile.getImgUrl());
            map.put("subject", Subject.fromSubjectId(newBookProfile.getSubjectId()).getValue());
            map.put("name", newBookProfile.getShortName());
            map.put("book_need_pay", true);
            map.put("book_id", newBookProfile.getId());
            map.put("isPurchased", true);
            map.put("url", "");
            buyedBookMapList.add(map);
        });
        if (CollectionUtils.isNotEmpty(buyedBookMapList))
            return mapMessage.add("purchased_book_list", buyedBookMapList);
        else
            return mapMessage;

    }

    @RequestMapping(value = "/mockData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mock() {
        Long sid = getRequestLong("sid");
        Date mockDate = DateUtils.stringToDate(getRequestString("date"), DateUtils.FORMAT_SQL_DATETIME);

        if (GrindEarActivity.isInActivityPeriod(mockDate)) {
//            //当前时间在上午9点05之前
            if (GrindEarActivity.beforeDeadLineTime(mockDate)) {
                grindEarService.mockPushRecord(sid, mockDate);
            }
        }
        return MapMessage.successMessage();
    }

    private Set<Integer> hadRewardDay(List<Date> dateList){
        List<DayRange> finishDayRangeSet = dateList.stream().map(date -> DayRange.newInstance(date.getTime())).distinct()
                .sorted(Comparator.comparing(DateRange::getStartDate)).collect(Collectors.toList());
        LinkedList<DayRange> continuityDayRangeList = new LinkedList<>();
        Set<Integer> daysSet = new HashSet<>();
        for (DayRange dayRange : finishDayRangeSet) {
            if (continuityDayRangeList.size() > 0 && !continuityDayRangeList.getLast().equals(dayRange.previous()))
                continuityDayRangeList.clear();
            continuityDayRangeList.addLast(dayRange);
            switch (continuityDayRangeList.size()) {
                case 1 : daysSet.add(1);break;
                case 3 : daysSet.add(3);break;
                case 7 : daysSet.add(7);break;
                case 14 : daysSet.add(14);break;
                case 20 : daysSet.add(20);break;
                case 30 : daysSet.add(30);break;
            }
        }
        return daysSet;
    }


    @RequestMapping(value = "/invite_student.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage invite() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        long studentId = getRequestLong("sid");
        if (studentId == 0 )
            return MapMessage.errorMessage("没有绑定孩子哦");

        User student = raikouSystem.loadUser(studentId);
        if (student == null)
            return MapMessage.errorMessage("没有绑定孩子哦");

        long inviterId = getRequestLong("inviter_id");
        if (inviterId == 0 )
            return MapMessage.errorMessage("没有邀请人 ID 哦");

        String key = studentInviteTimesKey(studentId);
        long inviteCount =  SafeConverter.toLong(PiclistenCache.getPersistenceCache().load(key));
        if (inviteCount >= 3 )
            return MapMessage.errorMessage("每天最多发出3次邀请");

        String studentInvitorKey = studentInvitorKey(inviterId);
        Long invtorId = PiclistenCache.getPersistenceCache().load(studentInvitorKey);
        if (invtorId != null){
            if (invtorId.equals(studentId)){
                return MapMessage.errorMessage("你今天已经给TA发过邀请了");
            }else {
                return MapMessage.errorMessage("TA今天已被邀请过了，去试试邀请别的同学吧！");
            }
        }

        sendInviteMsg(inviterId, student.fetchRealname());
        PiclistenCache.getPersistenceCache().set(studentInvitorKey, DateUtils.getCurrentToDayEndSecond(), studentId);
        PiclistenCache.getPersistenceCache().incr(key, 1, 1, DateUtils.getCurrentToDayEndSecond());
        return MapMessage.successMessage();
    }

    private void sendInviteMsg(long inviterId, String studentName) {

        String linkUrl = "/view/mobile/activity/parent/ear_plan_v2/strategy.vpage";
        String content = studentName + "同学邀请你去参加寒假磨耳朵活动，赢团体学豆奖励，快去看看吧！";
        AppMessage message = new AppMessage();
        message.setUserId(inviterId);
        message.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType());
        message.setTitle("通知");
        message.setContent(content);
        message.setLinkUrl(linkUrl);
        message.setLinkType(1);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        // 发送push
        Map<String, Object> extroInfo = MapUtils.m("s", StudentAppPushType.ACTIVITY_REMIND.getType(), "key", "j", "link", linkUrl, "t", "h5");
        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, Collections.singletonList(inviterId), extroInfo);
    }


    /**
     * 该学生被谁邀请的
     * @param studentId
     * @return
     */
    private String studentInvitorKey (Long studentId){
         return CacheKeyGenerator.generateCacheKey("grindEarInvitor18", new String[]{"sid"}, new Object[]{studentId});
    }

    private String studentInviteTimesKey(Long studentId){
        return CacheKeyGenerator.generateCacheKey("grindEarInviteTimes18", new String[]{"sid"}, new Object[]{studentId});
    }

    @RequestMapping(value = "/clazz_group_reward.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clazzGroupReward() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        long studentId = getRequestLong("sid");
        if (studentId == 0 )
            return MapMessage.errorMessage("没有绑定孩子哦");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage("没有绑定孩子哦");
        Long clazzId = studentDetail.getClazzId();
        if (clazzId == null){
            return MapMessage.errorMessage("孩子还没有班级，快去加入班级吧！");
        }
        ClazzRewardMapper mapper = clazzRewardInfo(clazzId, true);
        return MapMessage.successMessage().add("finish_count", mapper.getClazzFinishCount())
                .add("clazz_count", mapper.getClazzStudentCount())
                .add("clazz_remain", mapper.getClazzRemain())
                .add("integral", mapper.getClazzIntegral())
                .add("student_list", mapper.getStudentInfoMapList());

    }

    @RequestMapping(value = "/student_msg.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studentMsg() {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null)
            return noLoginResult;
        Long clazzId = studentDetail.getClazzId();
        if (clazzId == null)
            return MapMessage.errorMessage("孩子还没有班级，快去加入班级吧！");
        ClazzRewardMapper mapper = clazzRewardInfo(clazzId, false);
        return MapMessage.successMessage().add("clazz_count", mapper.getClazzFinishCount())
                .add("integral_count", mapper.getClazzIntegral())
                .add("sid", studentDetail.getId());
    }

    @Data
    private class ClazzRewardMapper{
        private Integer clazzFinishCount;
        private Integer clazzStudentCount;
        private Integer clazzRemain;
        private Integer clazzIntegral;
        private List<Map<String, Object>> studentInfoMapList;
    }

    private ClazzRewardMapper clazzRewardInfo(Long clazzId, boolean needDetail){
        List<Long> groupIds = deprecatedGroupLoaderClient.loadClazzGroups(clazzId).stream().map(GroupMapper::getId).collect(Collectors.toList());
        Map<Long, GroupMapper> groupMapperMap = deprecatedGroupLoaderClient.loadGroups(groupIds, true);
        List<GroupMapper.GroupUser> clazzUserList = groupMapperMap.values().stream().map(GroupMapper::getStudents).flatMap(Collection::stream).
                distinct().filter(t -> !t.getName().equals(UserConstants.EXPERIENCE_ACCOUNT_NAME)).
                sorted(Comparator.comparingLong(GroupMapper.GroupUser::getId)).collect(Collectors.toList());
        int clazzStudentCount = clazzUserList.size();
        Set<Long> studentIdSet = clazzUserList.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
        AlpsFuture<Map<Long, StudentGrindEarRecord>> mapAlpsFuture = grindEarService.loadStudentGrindEarRecords(studentIdSet);
        Map<Long, StudentGrindEarRecord> studentGrindEarRecordMap = mapAlpsFuture.getUninterruptibly();
        int clazzFinishCount = studentGrindEarRecordMap.size();

        List<Map<String, Object>> studentMapList = new ArrayList<>();
        if (needDetail) {
            clazzUserList.forEach(t -> {
                Long sid = t.getId();
                StudentGrindEarRecord studentGrindEarRecord = studentGrindEarRecordMap.get(sid);
                long dayCount = studentGrindEarRecord == null ? 0 : studentGrindEarRecord.dayCount();
                Map<String, Object> map = new HashMap<>();
                map.put("student_name", t.getName());
                map.put("student_id", sid);
                map.put("day_count", dayCount);
                studentMapList.add(map);
            });
        }

        ClazzRewardMapper mapper = new ClazzRewardMapper();
        mapper.setClazzFinishCount(clazzFinishCount);
        mapper.setClazzIntegral(getClazzIntegral(clazzStudentCount, clazzFinishCount));
        mapper.setClazzRemain(clazzStudentCount - clazzFinishCount);
        mapper.setClazzStudentCount(clazzStudentCount);
        mapper.setStudentInfoMapList(studentMapList);
        return mapper;
    }

    private int getClazzIntegral(int clazzStudentCount, int clazzFinishCount) {
        if (clazzStudentCount == 0 )
            return 0;
        if (clazzFinishCount == 0 )
            return 0;
        int levelA = new BigDecimal(clazzStudentCount * 0.2).setScale(0, RoundingMode.HALF_UP).intValue();
        int levelB = new BigDecimal(clazzStudentCount * 0.4).setScale(0, RoundingMode.HALF_UP).intValue();
        int levelC = new BigDecimal(clazzStudentCount * 0.8).setScale(0, RoundingMode.HALF_UP).intValue();
        if (clazzFinishCount < levelA)
            return 0;
        if (clazzFinishCount >= levelA && clazzFinishCount < levelB)
            return 5;
        if (clazzFinishCount >= levelB && clazzFinishCount < levelC)
            return 10;
        if (clazzFinishCount >= levelC)
            return 15;
        return 0;
    }


}
