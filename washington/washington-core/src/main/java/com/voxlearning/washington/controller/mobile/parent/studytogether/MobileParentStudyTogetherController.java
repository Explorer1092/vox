package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSpu;
import com.voxlearning.utopia.service.parent.api.StudyTogetherJoinGroupV2Service;
import com.voxlearning.utopia.service.parent.api.cache.ParentCache;
import com.voxlearning.utopia.service.parent.api.constants.StudyTogetherJoinActiveConstants;
import com.voxlearning.utopia.service.parent.api.constants.StudyTogetherSummerPackageActivity;
import com.voxlearning.utopia.service.parent.api.constants.StudyTogetherXBPayConstants;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.*;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.CardLessonMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.upload.OSSManageUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2018-04-13 下午9:20
 **/
@Controller
@RequestMapping(value = "/parentMobile/study_together/")
public class MobileParentStudyTogetherController extends AbstractMobileParentStudyTogetherController {


    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;

    @ImportService(interfaceClass = StudyTogetherJoinGroupV2Service.class)
    private StudyTogetherJoinGroupV2Service studyTogetherJoinGroupV2Service;


    @RequestMapping(value = "lb_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage liebianInfo() {
        User user = currentParent();
        if (user == null)
            return noLoginResult;
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("lesson_id ?");
        String key = generateParentShareImgKey(user.getId(), lessonId);
        String fileUrl = ParentCache.getParentPersistenceCache().load(key);
        String userMobile = sensitiveUserDataServiceClient.loadUserMobile(user.getId());
        userMobile = userMobile == null || userMobile.length() < 4 ? "" : userMobile.substring(7, 11);
        MapMessage mapMessage = MapMessage.successMessage();
        if (StringUtils.isNotBlank(fileUrl))
            mapMessage.add("share_img_url", fileUrl);
        mapMessage.add("parent_id", user.getId());
        mapMessage.add("phone", userMobile);
        mapMessage.add("nickname", studyTogetherServiceClient.getStudyTogetherHulkService().fetchParentName(user.getId()));
        return mapMessage;
    }


    @RequestMapping(value = "lb_upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadShareImage(HttpServletRequest request) {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        String lessonId = getRequestString("lesson_id");
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null || studyLesson.getCloseDate().before(new Date()))
            return MapMessage.errorMessage(" 活动已过期哦！");
        try {
            String file = getRequestString("file");
            if (file == null)
                return MapMessage.errorMessage("文件不存在");
            return AtomicLockManager.getInstance().wrapAtomic(this).keyPrefix("studyTogether").keys(parent.getId())
                    .proxy().uploadImage(file, parent.getId(), studyLesson, studyTogetherServiceClient);
        } catch (Exception e) {
            return MapMessage.errorMessage("上传错误！");
        }
    }

    private String generateParentShareImgKey(Long parentId, String lessonId) {
        return CacheKeyGenerator.generateCacheKey("studyTogetherShareImage", new String[]{"pid", "lid"}, new Object[]{parentId, lessonId});
    }

    private MapMessage uploadImage(String file, Long parntId, StudyLesson studyLesson, StudyTogetherServiceClient studyTogetherServiceClient) {
        String key = generateParentShareImgKey(parntId, SafeConverter.toString(studyLesson.getLessonId()));
        String fileUrl = ParentCache.getParentPersistenceCache().load(key);
        if (StringUtils.isNotBlank(fileUrl))
            return MapMessage.successMessage().add("share_img_url", fileUrl);
        fileUrl = OSSManageUtils.uploadBase64Image(file, "studytogether", "jpeg");
        ParentCache.getParentPersistenceCache().set(key, (int) (studyLesson.getCloseDate().getTime() / 1000), fileUrl);
        studyTogetherServiceClient.getStudyTogetherInviteFissionService().firstScanCode(parntId);
        return MapMessage.successMessage().add("share_img_url", fileUrl);
    }


    @RequestMapping(value = "testJoin.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage testJoin() {
        if (RuntimeMode.isUsingProductionData())
            return MapMessage.successMessage("调皮哦~");
        User user = currentParent();
        if (user == null)
            return noLoginResult;
        return studyTogetherServiceClient.parentSignUpLesson(getRequestString("lesson_id"), user.getId(), SafeConverter.toBoolean(getRequestString("channel_c")), getRequestLong("inviter_id"));
    }

    @RequestMapping(value = "testJoinGroup.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage testJoinGroup() {
        if (RuntimeMode.isUsingProductionData())
            return MapMessage.successMessage("调皮哦~");
        User user = currentParent();
        if (user == null)
            return noLoginResult;
        long studentId = getRequestLong("sid");
        String lessonId = getRequestString("lesson_id");
        String code = getRequestString("code");
        String groupId = studyTogetherServiceClient.studentJoinStudyGroupByCode(studentId, user.getId(), lessonId, code);
        return MapMessage.successMessage().add("groupId", groupId);
    }


    @RequestMapping(value = "join_lesson.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinLesson() {
        User parent = currentParent();
        if (parent == null)
            return go2LoginPageResult;
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程ID呢");
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null)
            return MapMessage.errorMessage("课程不存在");
        //fixme 后面这里要修改一下，太不灵活了
        if (studyLesson.safeGetJoinWay() != 0
                && studyLesson.safeGetJoinWay() != 7
                && studyLesson.safeGetJoinWay() != 8)
            return MapMessage.errorMessage("非法的报名方式哦！");
        Long studentId = getRequestLong("sid");
//        long inviterId = getRequestLong("inviter_id");
        String joinSourceStr = SafeConverter.toString(getRequestString("join_source"));
        ParentJoinLessonRef.JoinSource joinSource = ParentJoinLessonRef.JoinSource.safeOf(joinSourceStr);
        if (joinSource == null){
            joinSource = ParentJoinLessonRef.JoinSource.FREE;
        }
        return AtomicLockManager.getInstance().wrapAtomic(studyTogetherServiceClient)
                .keyPrefix("parentJoin17xueLesson")
                .keys(parent, lessonId)
                .proxy()
                .parentSignUpLesson(lessonId, parent.getId(), studentId, false, joinSource, "");

    }


    @RequestMapping(value = "lesson_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lessonList() {
        User parent = currentParent();
        boolean isLogin = parent != null;
        long studentId = getRequestLong("sid");
        boolean hasChild = isLogin && studentId != 0L;
        List<CardLessonMapper> mapList = new ArrayList<>();

        List<CourseStructSpu> courseStructSpus = studyCourseStructLoaderClient.loadAllCourseStructSpu();
        Set<Integer> skuIdSet = courseStructSpus.stream().map(t -> t.getId().intValue()).collect(Collectors.toSet());

        Map<Integer, List<ParentJoinLessonRef>> parentJoinSkuLessonsMap = !isLogin ? Collections.emptyMap()
                : studyTogetherServiceClient.loadParentJoinSkuLessons(parent.getId(), skuIdSet);
        Map<Integer, List<StudyGroup>> studentJoinSkuGroupsMap = !hasChild ? Collections.emptyMap()
                : studyTogetherServiceClient.loadStudentSkuGroups(studentId, skuIdSet);

        /**
         * 恶心的傻逼的临时处理逻辑来了：如果激活过skuId=1的课程，则不显示skuId=2的课程
         */
        boolean activeSku1 = studentJoinSkuGroupsMap.values().stream().flatMap(Collection::stream).anyMatch(t -> {
            String lessonId = t.getLessonId();
            StudyLesson skuLatestLesson = getStudyLesson(lessonId);
            return skuLatestLesson != null && skuLatestLesson.getSpuId() == 1L;
        });

        Date nowDate = new Date();
        Integer studentClazzLevel = studyTogetherServiceClient.getStudentClazzLevel(studentId);
        List<StudyLesson> showLessonList = new ArrayList<>();
        //没登录，
        if (!isLogin) {
            for (CourseStructSpu studySku : courseStructSpus) {
                StudyLesson skuLatestSignLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestSignUpLesson(studySku.getId());
                if (skuLatestSignLesson != null)
                    showLessonList.add(skuLatestSignLesson);
            }
            if (showLessonList.size() == 0) {
                CourseStructSpu studySku = courseStructSpus.stream().findAny().orElse(null);
                if (studySku != null) {
                    StudyLesson skuLatestLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestLesson(studySku.getId());
                    StudyLesson skuLatestSignLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestSignUpLesson(studySku.getId());
                    if (skuLatestSignLesson == null) {
                        if (skuLatestLesson != null) {
                            showLessonList.add(skuLatestLesson);
                        }
                    } else {
                        showLessonList.add(skuLatestSignLesson);
                    }
                }
            }
        } else {
            courseStructSpus.forEach(studySku -> {
                if (studySku == null) {
                    return;
                }
                Integer skuId = studySku.getId().intValue();
                List<ParentJoinLessonRef> joinLessonRefs = parentJoinSkuLessonsMap.get(skuId);
                List<StudyGroup> studyGroups = studentJoinSkuGroupsMap.get(skuId);
                boolean isJoin = CollectionUtils.isNotEmpty(joinLessonRefs);
                boolean isActive = CollectionUtils.isNotEmpty(studyGroups);
                boolean skuHasLessoon = false;
                if (isActive) {
                    studyGroups.forEach(t -> {
                        StudyLesson studyLesson = getStudyLesson(t.getLessonId());
                        if (studyLesson != null)
                            showLessonList.add(studyLesson);
                    });
                    skuHasLessoon = true;
                } else if (isJoin) {
                    for (ParentJoinLessonRef t : joinLessonRefs) {
                        StudyLesson studyLesson = getStudyLesson(t.getStudyLessonId());
                        if (studyLesson != null) {
                            if (DateUtils.calculateDateDay(studyLesson.getOpenDate(), 5).after(nowDate)) {
                                showLessonList.add(studyLesson);
                                skuHasLessoon = true;
                            }
                        }
                    }
                }
                if (!skuHasLessoon) {
                    if (skuId == 2 && activeSku1)
                        return;
                    if (studentClazzLevel == null || studySku.getGrades() == null || studySku.getGrades().isEmpty() || studySku.getGrades().contains(studentClazzLevel)) {
                        StudyLesson skuLatestLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestLesson(Long.valueOf(skuId));
                        StudyLesson skuLatestSignLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getSpuCardShowLatestSignUpLesson(Long.valueOf(skuId));
                        if (skuLatestSignLesson == null) {
                            if (skuLatestLesson != null) {
                                showLessonList.add(skuLatestLesson);
                            }
                        } else {
                            showLessonList.add(skuLatestSignLesson);
                        }
                    }
                }
            });
        }

        Map<String, AlpsFuture<Map<String, Integer>>> studyInfoFutureMap = new HashMap<>();
        Map<String, AlpsFuture<Long>> lessonJoinCountFutureMap = new HashMap<>();
        Map<String, ParentJoinGroup> lessonJoinGroupMap = new HashMap<>();

        showLessonList.forEach(t -> {
            String lessonId = SafeConverter.toString(t.getLessonId());
            if (hasChild) {
                AlpsFuture<Map<String, Integer>> mapAlpsFuture = studyTogetherServiceClient.loadStudentTodayFinishInfo(lessonId, studentId);
                studyInfoFutureMap.put(lessonId, mapAlpsFuture);
            }
            AlpsFuture<Long> lessonJoinCountFuture = studyTogetherServiceClient.loadLessonJoinCount(lessonId);
            lessonJoinCountFutureMap.put(lessonId, lessonJoinCountFuture);
            if (isLogin && t.joinNeedGroup()) {
                ParentJoinGroup parentJoinGroup = studyTogetherJoinGroupV2Service.loadParentJoinGroup(lessonId, parent.getId());
                if (parentJoinGroup != null)
                    lessonJoinGroupMap.put(lessonId, parentJoinGroup);
            }

        });

        Set<String> lessonIdSet = new HashSet<>();
        for (StudyLesson studyLesson : showLessonList) {
            if (studyLesson == null)
                continue;
            String lessonId = SafeConverter.toString(studyLesson.getLessonId());
            if (lessonIdSet.contains(lessonId))
                continue;
            if (studyLesson.getShowDate().after(nowDate))
                continue;
            int spuId = studyLesson.getSpuId().intValue();

            List<ParentJoinLessonRef> joinLessonRefs = parentJoinSkuLessonsMap.get(spuId);
            boolean isJoin = joinLessonRefs != null && joinLessonRefs.stream().anyMatch(t -> t.getStudyLessonId().equals(lessonId));
            List<StudyGroup> studyGroups = studentJoinSkuGroupsMap.get(spuId);
            boolean isOpen = studyGroups != null && studyGroups.stream().anyMatch(t -> t.getLessonId().equals(lessonId));

            AlpsFuture<Long> joinCountFuture = lessonJoinCountFutureMap.get(lessonId);
            long joinCount = joinCountFuture == null ? 0 : SafeConverter.toLong(joinCountFuture.getUninterruptibly());
            AlpsFuture<Map<String, Integer>> mapAlpsFuture = studyInfoFutureMap.get(lessonId);
            Map<String, Integer> map = mapAlpsFuture == null ? Collections.emptyMap() : mapAlpsFuture.getUninterruptibly();
            Map<String, Integer> finishInfoMap = map == null ? Collections.emptyMap() : map;
            boolean hasGroup = isLogin && lessonJoinGroupMap.get(lessonId) != null;
            CardLessonMapper cardLessonMapper = lessonMap(studyLesson, isJoin, isOpen, joinCount, hasChild, finishInfoMap, hasGroup);
            mapList.add(cardLessonMapper);
            lessonIdSet.add(lessonId);
        }
        Collections.sort(mapList);

        Set<String> activeLessonIdSet = studentJoinSkuGroupsMap.values().stream().flatMap(Collection::stream).map(StudyGroup::getLessonId).collect(Collectors.toSet());
        StudyLesson payLesson = getStudyLesson(StudyTogetherJoinActiveConstants.payTestSkuId);
        boolean showPayLessonEntry = payLesson.inSignUpPeriod() && activeLessonIdSet.contains(StudyTogetherJoinActiveConstants.payTestOldSkuId);
        payLesson = getStudyLesson(StudyTogetherJoinActiveConstants.newPaySkuId);
        if (payLesson != null) {
//            boolean isCorrectPeotryUser = activeLessonIdSet.stream().anyMatch(e -> Arrays.asList(StudyTogetherJoinActiveConstants.newPayOldPeotrySkuIds).contains(e)) && studentClazzLevel >= 2 && studentClazzLevel <= 5;
//            boolean isCorrectAnalectsUser = activeLessonIdSet.stream().anyMatch(e -> Arrays.asList(StudyTogetherJoinActiveConstants.newPayOldAnalectsSkuIds).contains(e));
//            showPayLessonEntry = payLesson.inSignUpPeriod() && (isCorrectPeotryUser || isCorrectAnalectsUser);
            showPayLessonEntry = payLesson.inSignUpPeriod() && (studentClazzLevel == 0 || (studentClazzLevel >= 1 && studentClazzLevel <= 4));
        }
        return MapMessage.successMessage().add("study_lesson_list", mapList)
                .add("summer_activity_entry", StudyTogetherSummerPackageActivity.inPeriod())
                .add("show_pay_lesson_entry", showPayLessonEntry)
                .add("discount_price_countdown", StudyTogetherXBPayConstants.countDown());
    }

    private CardLessonMapper lessonMap(StudyLesson studyLesson, boolean isJoin, boolean isOpen, Long joinCount, boolean hasChild, Map<String, Integer> finishInfoMap, boolean hasGroup) {
        Date nowDate = new Date();
        DayRange todayRange = DayRange.current();
        CardLessonMapper map = new CardLessonMapper();
//        map.setStudyLesson(studyLesson);
        String lessonId = SafeConverter.toString(studyLesson.getLessonId());
        map.setLessonId(lessonId);
        map.setCloseDate(studyLesson.getCloseDate());
        map.setSignUpEndDate(studyLesson.getSighUpEndDate());
        map.setName(studyLesson.getTitle());
        map.setPhase(studyLesson.getPhase());
        map.setSubject(studyLesson.getSubject().getValue());
        map.setStartDate(DateUtils.dateToString(studyLesson.getOpenDate(), "M月dd日"));
        map.setTimes(studyLesson.getTimes());
        map.setIsJoin(isJoin || isOpen);
        map.setIsOpen(isOpen);
        map.setLessonStatus(getStatus(studyLesson.getOpenDate(), studyLesson.getCloseDate()));
        map.setLessonJoinStatus(getStatus(studyLesson.getShowDate(), studyLesson.getSighUpEndDate()));
        map.setLessonStartCountdown(DateUtils.dayDiff(studyLesson.getOpenDate(), nowDate) + 1);
        map.setJoinCount(joinCount);
        map.setJoinLimit(studyLesson.getPersonLimited());
        map.setJoinWay(studyLesson.safeGetJoinWay());
        map.setCourseType(studyLesson.getCourseType());
        map.setSkuId(studyLesson.getSpuId().intValue());
        map.setHasVideoCompetition(studyLesson.getCloseDate().getTime() >= System.currentTimeMillis() && lessonId.equals("1") && studyLesson.getSpuId().intValue()== 1);
        map.setActiveType(SafeConverter.toInt(studyLesson.getActiveType()));
        if (studyLesson.joinNeedGroup()) {
            map.setHasGroup(hasGroup);
        }
        List<DayRange> studyDaySortedRangeList = studyLesson.getStudyDaySortedRangeList() == null ? Collections.emptyList() : studyLesson.getStudyDaySortedRangeList();
        /**
         * today is 4
         * 1,2,3,5,7,8,9
         */
        boolean todayHasTask = false;
        int currentTimes = 0;
        for (int i = 0; i < studyDaySortedRangeList.size(); i++) {
            DayRange studyDayRange = studyDaySortedRangeList.get(i);
            //最后一个
            DayRange nextStudyDayRange;
            if (i == studyDaySortedRangeList.size() - 1)
                nextStudyDayRange = null;
            else
                nextStudyDayRange = studyDaySortedRangeList.get(i + 1);
            if (todayRange.equals(studyDayRange))
                todayHasTask = true;
            if (nextStudyDayRange != null) {
                if (todayRange.getStartTime() >= studyDayRange.getStartTime() && todayRange.getStartTime() < nextStudyDayRange.getStartTime()) {
                    currentTimes = i + 1;
                    break;
                }
            } else
                currentTimes = studyDaySortedRangeList.size();
        }
        map.setCurrentTime(currentTimes);
        map.setTodayHasTask(todayHasTask);

        map.setTodayTaskFinishCount(SafeConverter.toInt(finishInfoMap.get("finishCount")));
        if (!hasChild) {
            map.setTodayTaskScore(-1);
        } else {
            map.setTodayTaskScore(SafeConverter.toInt(finishInfoMap.get("star")));
        }
        return map;
    }

    @RequestMapping(value = "lesson_join_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lessonJoinInfo() {
        User parent = currentParent();
        if (parent == null)
            return go2LoginPageResult;
        String lessonId = getRequestString("lesson_id");
        ParentJoinLessonRef parentJoinLessonRef = studyTogetherServiceClient.loadParentJoinLessonRef(lessonId, parent.getId());
        if (parentJoinLessonRef == null)
            return MapMessage.errorMessage("您尚未报名哦");
        String sorecOpWechatId = parentJoinLessonRef.getSourceOpWechatId();
        StudyOpWechatAccount studyOpWechatAccount = studyTogetherServiceClient.getStudyTogetherBuffer().getStudyOpWechatAccount(sorecOpWechatId);
        if (studyOpWechatAccount == null)
            return MapMessage.errorMessage("哦哦，出错了！");
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null)
            return MapMessage.errorMessage("哦哦，当前课程不存在哦~");
        String startDateStr = DateUtils.dateToString(studyLesson.getOpenDate(), "M月dd日");
        long countdown = DateUtils.dayDiff(studyLesson.getOpenDate(), new Date());

        MapMessage successMessage = MapMessage.successMessage();

        ParentJoinLessonRef.JoinSource joinSource = parentJoinLessonRef.safeGetJoinSource();
        if (joinSource == ParentJoinLessonRef.JoinSource.GROUP) {
            ParentJoinGroup parentJoinGroup = studyTogetherJoinGroupV2Service.loadParentJoinGroup(lessonId, parent.getId());
            if (parentJoinGroup != null ) {
                List<Map<String, Object>> memberMapList = generateGroupMemberMapList(parentJoinGroup, parent.getId());
                if (CollectionUtils.isNotEmpty(memberMapList))
                    successMessage.add("group_members", memberMapList);
            }
        }
        if (StringUtils.isNotBlank(studyLesson.getActivePagePic())) {
            successMessage.add("head_img_url", studyLesson.getActivePagePic());
        }
        int qrcodeType = SafeConverter.toInt(studyLesson.getQrcodeType(), 2);
        return successMessage.add("qr_code", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + studyOpWechatAccount.getQrCodeUrl())
                .add("name", studyOpWechatAccount.getWechatNumber())
                .add("lesson_name", studyLesson.getTitle() + "（第" + studyLesson.getPhase() + "期）")
                .add("lesson_id", studyLesson.getLessonId())
                .add("start_date", startDateStr)
                .add("lesson_start_countdown", countdown + 1)
                .add("is_finish", studyLesson.getCloseDate().before(new Date()))
                .add("course_type", studyLesson.getCourseType())
                .add("active_type", studyLesson.getActiveType())
                .add("qrcode_type", (qrcodeType < 1 || qrcodeType > 2) ? 2 : qrcodeType)
                .add("sku_type", studyLesson.getSkuType())
                .add("confirm_active_in_one_day", studyLesson.safeIsLightSpu());
    }

    @RequestMapping(value = "wechat_lesson_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage wechatLessonList() {
        User parent = currentParent();
        if (parent == null) {
            return go2LoginPageResult;
        }
        //查家长报名的课程
        Map<String, ParentJoinLessonRef> joinLessonRefMap = studyTogetherServiceClient.loadParentJoinLessonRefs(parent.getId());
        List<User> parentStudents = studentLoaderClient.loadParentStudents(parent.getId());
        Set<String> activeLessonIds = new HashSet<>();
        //查孩子激活的课程
        for (User student : parentStudents) {
            List<StudyGroup> studyGroups = studyTogetherServiceClient.loadStudentActiveLessonGroups(student.getId());
            if (CollectionUtils.isNotEmpty(studyGroups)) {
                Set<String> lessonIds = studyGroups.stream().map(StudyGroup::getLessonId).collect(Collectors.toSet());
                activeLessonIds.addAll(lessonIds);
            }
        }
        Set<String> lessonIds = joinLessonRefMap.keySet().stream().filter(e -> !activeLessonIds.contains(e)).collect(Collectors.toSet());
        List<StudyLesson> lessonList = new ArrayList<>();
        Date currentDate = new Date();
        lessonIds.forEach(e -> {
            StudyLesson studyLesson = getStudyLesson(e);
            if (studyLesson != null
                    && currentDate.after(studyLesson.getShowDate())
                    && currentDate.before(studyLesson.getCloseDate())
                    && DateUtils.calculateDateDay(studyLesson.getOpenDate(), 5).after(currentDate)) {
                lessonList.add(studyLesson);
            }
        });
        List<Map<String, Object>> returnList = new ArrayList<>();
        List<StudyLesson> sortLessonList = lessonList.stream().sorted((o1, o2) -> o2.getShowDate().compareTo(o1.getShowDate())).collect(Collectors.toList());
        for (StudyLesson studyLesson : sortLessonList) {
            AlpsFuture<Long> lessonJoinCountFuture = studyTogetherServiceClient.loadLessonJoinCount(SafeConverter.toString(studyLesson.getLessonId()));
            Long joinCount = lessonJoinCountFuture == null ? 0L : SafeConverter.toLong(lessonJoinCountFuture.getUninterruptibly());
            Map<String, Object> map = new HashMap<>();
            map.put("lesson_id", studyLesson.getLessonId());
            map.put("title", studyLesson.getTitle());
            map.put("phase", studyLesson.getPhase());
            map.put("sku_id", studyLesson.getSpuId());
            map.put("subject", studyLesson.getSubject().getValue());
            map.put("lesson_status", getStatus(studyLesson.getOpenDate(), studyLesson.getCloseDate()));
            map.put("lesson_join_status", getStatus(studyLesson.getShowDate(), studyLesson.getSighUpEndDate()));
            map.put("times", studyLesson.getTimes());
            map.put("join_count", joinCount);
            map.put("is_start", studyLesson.getOpenDate().getTime() - currentDate.getTime() <= 0);
            map.put("lesson_start_countdown", DateUtils.dayDiff(studyLesson.getOpenDate(), currentDate) + 1);
            map.put("start_date", DateUtils.dateToString(studyLesson.getOpenDate(), "M月dd日"));
            ParentJoinLessonRef parentJoinLessonRef = joinLessonRefMap.get(studyLesson.getLessonId());
            if (parentJoinLessonRef != null) {
                map.put("wechat_id", parentJoinLessonRef.getSourceOpWechatId());
            }
            returnList.add(map);
        }
        return MapMessage.successMessage().add("lesson_list", returnList).add("parent_id", parent.getId());
    }

    @RequestMapping(value = "get_opwechat_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getOpWechatInfo() {
        String wechatId = getRequestString("wechat_id");
        if (StringUtils.isBlank(wechatId)) {
            return MapMessage.errorMessage("老师微信Id为空");
        }
        StudyOpWechatAccount studyOpWechatAccount = studyTogetherServiceClient.getStudyTogetherBuffer().getStudyOpWechatAccount(wechatId);
        if (studyOpWechatAccount == null) {
            return MapMessage.errorMessage("未找到对应的老师微信号");
        }
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("wechat_number", studyOpWechatAccount.getWechatNumber());
        returnMap.put("wechat_code", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + studyOpWechatAccount.getQrCodeUrl());

        return MapMessage.successMessage().add("wechat_info", returnMap);
    }

    @RequestMapping(value = "get_active_type.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getActiveType() {
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("课程Id不能为空");
        }
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null) {
            return MapMessage.errorMessage("未找到对应课程");
        }
        return MapMessage.successMessage()
                .add("active_type", SafeConverter.toInt(studyLesson.getActiveType(), 1))
                .add("lesson_title", studyLesson.getTitle());
    }
}
