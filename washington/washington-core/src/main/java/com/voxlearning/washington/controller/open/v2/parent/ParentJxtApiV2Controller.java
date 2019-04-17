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

package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.athena.api.jzt.entity.StudyProgress;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.parent.api.DPScoreCircleLoader;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.CircleContext;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContext;
import com.voxlearning.utopia.service.reminder.api.ReminderLoader;
import com.voxlearning.utopia.service.reminder.api.ReminderService;
import com.voxlearning.utopia.service.reminder.api.mapper.ReminderContext;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.consumer.MySelfStudyGlobalMsgServiceClient;
import com.voxlearning.washington.athena.ParentReportLoaderClient;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.v1.util.JxtNewsUtil;
import com.voxlearning.washington.support.StudyEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2017-5-8
 */
@Controller
@Slf4j
@RequestMapping(value = "/v2/parent/jxt/")
public class ParentJxtApiV2Controller extends AbstractParentApiController {

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject
    private UserAdvertisementServiceClient userAdvertisementServiceClient;

    @ImportService(interfaceClass = ReminderLoader.class)
    private ReminderLoader reminderLoader;
    @ImportService(interfaceClass = ReminderService.class)
    private ReminderService reminderService;
    @ImportService(interfaceClass = DPScoreCircleLoader.class)
    private DPScoreCircleLoader dpScoreCircleLoader;


    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;
    @Inject
    protected ParentReportLoaderClient parentReportLoaderClient;
    @Inject
    private UserBlacklistServiceClient userBlacklistServiceClient;
    @Inject
    private MySelfStudyGlobalMsgServiceClient mySelfStudyGlobalMsgServiceClient;

    private static final Set<String> subjectIds = new HashSet<>();

    static {
        if (RuntimeMode.lt(Mode.STAGING)) {
            subjectIds.add("591d45f9e92b1b58e1995ef9");
            subjectIds.add("591d4741e92b1b58e1995faf");
            subjectIds.add("5923e484e92b1baeeaa9f2aa");
        } else {
            subjectIds.add("5911678131eecb01bfceb0ee");
            subjectIds.add("59103f40aa0bab4cfdda0dbd");
            subjectIds.add("5923e56572118be571c6437a");
        }
    }


    @RequestMapping(value = "my_study.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage myStudyNew() {
        boolean isLogin = hasSessionKey();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            if (isLogin) {
                validateRequest(REQ_STUDENT_ID);
            } else {
                validateRequestNoSessionKey(REQ_STUDENT_ID);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        List<StudyEntry> entryList;
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        StudentDetail studentDetail = null;
        Boolean showMore;
        User parent = getCurrentParent();

        if (!isLogin) {  //没登录的,只显示点读机,绘本,和订阅专辑,都去登录
            entryList = noLoginEntryList();
            showMore = false;
        } else {
            if (parent == null)

                return failMessage(RES_RESULT_PARENT_ERROR_MSG);
            if (parent.getId().equals(20001L)) { //屏蔽20001审核帐号
                entryList = shenheEntryList();
                showMore = false;
            } else {
                if (studentId == 0L) { //没孩子的,显示点读机和订阅专辑,正常进入,还要显示
                    entryList = noChildEntryList();
                    showMore = false;
                } else {
                    studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                    if (studentDetail == null)
                        return failMessage(RES_RESULT_STUDENT_ID_ERROR_MSG);
                    entryList = allEntryList(parent, studentDetail);
                    showMore = studentDetail.getClazz() != null && !studentDetail.getClazz().isTerminalClazz() && studentDetail.isPrimaryStudent()
                            && !userBlacklistServiceClient.isInUserBlackList(parent); //学前学生也不显示加号
                }
                //加个外教小课堂的入口 一起学入口
                AlpsFuture<StudyAppData> smallClassDataFuture = mySelfStudyService.loadStudyAppData(parent.getId(), SelfStudyType.SMALL_CLASS);
                AlpsFuture<StudyAppData> refinedLessonDataFuture = mySelfStudyService.loadStudyAppData(parent.getId(), SelfStudyType.REFINED_LESSON);

                StudyAppData smallClassData = smallClassDataFuture.getUninterruptibly();
                if (smallClassData != null && smallClassData.fetchShow())
                    entryList.add(smallClassEntry());

                StudyAppData refinedLessonData = refinedLessonDataFuture.getUninterruptibly();
                if (refinedLessonData != null && refinedLessonData.fetchShow())
                    entryList.add(refinedLessonEntry());
            }
        }
        //如果Native 的 functionKey 版本不符合要求，过滤掉
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        entryList = entryList.stream().filter(t -> {
            if (t.getFunctionType() == StudyEntry.FunctionType.NATIVE) {
                StudyEntry.FunctionKey functionKey = StudyEntry.FunctionKey.of(t.getFunctionKey());
                if (functionKey == null || !functionKey.versionCheck(ver))
                    return false;
            }
            return true;
        }).collect(Collectors.toList());
        //添加全局的消息
        Map<SelfStudyType, MySelfStudyEntryGlobalMsg> mySelfStudyEntryGlobalMsgMap = mySelfStudyGlobalMsgServiceClient.getMySelfStudyEntryGlobalMsgMap();
        String currentCdnSchema = getCdnBaseUrlStaticSharedWithSep();
        entryList.forEach(t -> {
            t.setIconUrl(currentCdnSchema + t.getIconUrl());
            MySelfStudyEntryGlobalMsg mySelfStudyEntryGlobalMsg = mySelfStudyEntryGlobalMsgMap.get(t.getSelfStudyType());
            if (mySelfStudyEntryGlobalMsg != null && StringUtils.isNotBlank(mySelfStudyEntryGlobalMsg.getText())) {
                t.touchGlobalMsg(mySelfStudyEntryGlobalMsg);
            }
        });

        MapMessage mapMessage = successMessage();
        if (showMore) {
            Map<String, Object> more = new HashMap<>();
            more.put(RES_URL, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/learning_app/detail.vpage?rel=jiahao&order_refer=330087");
            more.put(RES_REMINDER_ID, "12312312312" + studentId);
            mapMessage.add(RES_MORE, more);
        }
        return mapMessage.add(RES_APP_ENTRY_LIST, entryList)
                .add(RES_PARENT_APP_MY_STUDY_TITLE, studentDetail == null ? "学习" : studentDetail.fetchRealname() + "的学习");

    }

    private StudyEntry refinedLessonEntry() {
        StudyEntry entry = StudyEntry.newInstance(SelfStudyType.REFINED_LESSON, "");
        entry.touchFunctionType(StudyEntry.FunctionType.H5);
        entry.touchFunctionKey(fetchMainsiteUrlByCurrentSchema() + "/redirector/goaoshu.vpage?returnURL=" + getRefinedLessonUrl());
        return entry;
    }


    private StudyEntry smallClassEntry() {
        StudyEntry entry = StudyEntry.newInstance(SelfStudyType.SMALL_CLASS, "");
        entry.touchFunctionType(StudyEntry.FunctionType.H5);
        entry.touchFunctionKey(fetchMainsiteUrlByCurrentSchema() + "/redirector/goaoshu.vpage?returnURL=" + getSmallClassUrl());
        return entry;
    }

    private String getSmallClassUrl() {
        Mode current = RuntimeMode.current();
        if (current == Mode.DEVELOPMENT || current == Mode.TEST)
            return "http://livecast-student.test.17zuoye.net/auth/ucourse/login.vpage";
        if (current == Mode.STAGING)
            return "http://livecast-student.staging.17zuoye.net/auth/ucourse/login.vpage";
        if (current == Mode.PRODUCTION)
            return "https://www.ustalk.com/auth/ucourse/login.vpage";
        return "https://www.ustalk.com/auth/ucourse/login.vpage";
    }

    private String getAoshuUrl() {
        Mode current = RuntimeMode.current();
        if (current == Mode.DEVELOPMENT || current == Mode.TEST)
            return "http://aoshu-student.test.17zuoye.net/auth/parent/login.vpage";
        if (current == Mode.STAGING)
            return "http://aoshu-student.staging.17zuoye.net/auth/parent/login.vpage";
        if (current == Mode.PRODUCTION)
            return "http://www.17xueaoshu.com/auth/parent/login.vpage";
        return "http://aoshu-student.test.17zuoye.net/auth/parent/login.vpage";
    }

    private String getRefinedLessonUrl() {
        Mode current = RuntimeMode.current();
        if (current == Mode.DEVELOPMENT || current == Mode.TEST)
            return "http://17xue-student.test.17zuoye.net/m/center/index.vpage";
        if (current == Mode.STAGING)
            return "http://17xue-student.staging.17zuoye.net/m/center/index.vpage";
        if (current == Mode.PRODUCTION)
            return "https://www.17xueba.com/m/center/index.vpage";
        return "http://17xue-student.test.17zuoye.net/m/center/index.vpage#/myCourse/0";
    }

    private List<StudyEntry> shenheEntryList() {
        List<StudyEntry> list = new ArrayList<>(2);
        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, "")
                .touchFunctionType(StudyEntry.FunctionType.H5)
                .touchFunctionKey(fetchMainsiteUrlByCurrentSchema() + SelfStudyType.ALBUM.getH5Url()));
        return list;
    }

    /**
     * 增值app的入口,原谅我起了个这个名字
     */
    public static Set<SelfStudyType> moneyEntrySSTSet = new HashSet<>();
    public static Map<SelfStudyType, Integer> moneySST2OrderMap = new HashMap<>();

    static {
//        moneyEntrySSTSet.add(SelfStudyType.AFENTI_ENGLISH);moneyEntrySSTSet.add(SelfStudyType.AFENTI_CHINESE);
//        moneyEntrySSTSet.add(SelfStudyType.AFENTI_MATH);moneyEntrySSTSet.add(SelfStudyType.ZOUMEI_ENGLISH);moneyEntrySSTSet.add(SelfStudyType.KUPAOWORD_ENGLISH);
//        moneyEntrySSTSet.add(SelfStudyType.ARITHMETIC);moneyEntrySSTSet.add(SelfStudyType.FEE_COURSE_UNKUNOW);moneyEntrySSTSet.add(SelfStudyType.SYN_PRACTICE_CHINESE);
//        moneyEntrySSTSet.add(SelfStudyType.CHINESE_HERO);moneyEntrySSTSet.add(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW);moneyEntrySSTSet.add(SelfStudyType.Sudoku);

        moneySST2OrderMap.put(SelfStudyType.AFENTI_ENGLISH, 1);
        moneySST2OrderMap.put(SelfStudyType.AFENTI_MATH, 2);
        moneySST2OrderMap.put(SelfStudyType.AFENTI_CHINESE, 3);
        moneySST2OrderMap.put(SelfStudyType.ZOUMEI_ENGLISH, 4);
        moneySST2OrderMap.put(SelfStudyType.KUPAOWORD_ENGLISH, 5);
        moneySST2OrderMap.put(SelfStudyType.ARITHMETIC, 6);
        moneySST2OrderMap.put(SelfStudyType.FEE_COURSE_UNKUNOW, 7);
        moneySST2OrderMap.put(SelfStudyType.SYN_PRACTICE_CHINESE, 8);
        moneySST2OrderMap.put(SelfStudyType.CHINESE_HERO, 9);
        moneySST2OrderMap.put(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW, 10);
        moneySST2OrderMap.put(SelfStudyType.Sudoku, 11);

        moneyEntrySSTSet = moneySST2OrderMap.keySet();
    }


    private List<StudyEntry> allEntryList(User parent, StudentDetail studentDetail) {
        boolean zuoyecuotiIn = studentHasZuoyecuotiEntry(studentDetail);
        List<FairylandProduct> parentAvailableFairylandProducts =
                businessVendorServiceClient.getParentAvailableFairylandProducts(parent, studentDetail, FairyLandPlatform.PARENT_APP, FairylandProductType.APPS);
        Map<SelfStudyType, FairylandProduct> sst2FairyLandProductMap = new HashMap<>();
        for (FairylandProduct fairylandProduct : parentAvailableFairylandProducts) {
            SelfStudyType selfStudyType = SelfStudyType.fromOrderType(OrderProductServiceType.valueOf(fairylandProduct.getAppKey()));
            if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
                continue;
            sst2FairyLandProductMap.put(selfStudyType, fairylandProduct);
        }
        Set<SelfStudyType> availableSSTSet = sst2FairyLandProductMap.keySet();
        List<SelfStudyType> showMoneySSTList = moneyEntrySSTSet.stream().filter(availableSSTSet::contains).collect(Collectors.toList());
        showMoneySSTList.add(SelfStudyType.READING_ENGLISH);

        Map<SelfStudyType, AlpsFuture<StudyAppData>> studyTypeFutureMap = getUserStudyDataFutureMap(showMoneySSTList, parent, studentDetail, zuoyecuotiIn);

        List<StudyEntry> entryList = new ArrayList<>();
        String currentSchema = fetchMainsiteUrlByCurrentSchema();
        //作业错题本 可能没有
        if (zuoyecuotiIn) {
            StudyEntry zyctEntry = StudyEntry.newInstance(SelfStudyType.ZUOYECUOITI, "").touchFunctionType(StudyEntry.FunctionType.H5)
                    .touchFunctionKey(currentSchema + SelfStudyType.ZUOYECUOITI.getH5Url());
//            String zyctProgress = getProgressFromData(studyTypeFutureMap, SelfStudyType.ZUOYECUOITI);
            zyctEntry.setLabel("");
            entryList.add(zyctEntry);
        }
        //点读机 点读机如有有记录要先进点读报告页面 md
        StudyEntry picListenEntry = StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, "");
        AlpsFuture<StudyAppData> picListenDataFuture = studyTypeFutureMap.get(SelfStudyType.PICLISTEN_ENGLISH);
        String picListenProgress = getProgressFromData(picListenDataFuture.getUninterruptibly());
        picListenEntry.setLabel(picListenProgress);
        if (StringUtils.isNotBlank(picListenProgress)) {
            picListenEntry.touchFunctionType(StudyEntry.FunctionType.H5).touchFunctionKey(currentSchema + "/view/mobile/parent/read_report/index.vpage?order_referrer=330019");
        } else
            picListenEntry.touchFunctionType(StudyEntry.FunctionType.NATIVE).touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN);
        entryList.add(picListenEntry);

        //绘本
        StudyEntry readingEntry = StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, "");
        AlpsFuture<StudyAppData> readingDataFuture = studyTypeFutureMap.get(SelfStudyType.READING_ENGLISH);
        String readingProgress = getProgressFromData(readingDataFuture.getUninterruptibly());
        readingEntry.setLabel(readingProgress);
        readingEntry.touchFunctionType(StudyEntry.FunctionType.H5).touchFunctionKey(currentSchema + SelfStudyType.READING_ENGLISH.getH5Url());
        entryList.add(readingEntry);
        //然后是订阅专辑
        StudyEntry albumEntry = StudyEntry.newInstance(SelfStudyType.ALBUM, "").touchFunctionType(StudyEntry.FunctionType.H5)
                .touchFunctionKey(currentSchema + SelfStudyType.ALBUM.getH5Url());
        AlpsFuture<StudyAppData> albumDataFuture = studyTypeFutureMap.get(SelfStudyType.ALBUM);
        StudyAppData albumData = albumDataFuture.getUninterruptibly();
        String albumProgress = getProgressFromData(albumData);
        albumEntry.setLabel(albumProgress);
        //如果专辑的有图片
        String iconUrl = albumData.getIconUrl();
        if (StringUtils.isNotBlank(iconUrl)) {
            albumEntry.setIconUrl(iconUrl);
        }

        entryList.add(albumEntry);

        //然后是直播的 直播的是否显示要看数据里面的是否预约过。
        AlpsFuture<StudyAppData> liveCastDataFuture = studyTypeFutureMap.get(SelfStudyType.LIVECAST);
        if (liveCastDataFuture != null) {
            StudyAppData liveCastData = liveCastDataFuture.getUninterruptibly();
            if (liveCastData != null && liveCastData.fetchShow()) {
                StudyEntry liveCastEntry = StudyEntry.newInstance(SelfStudyType.LIVECAST, "").touchFunctionType(StudyEntry.FunctionType.NATIVE)
                        .touchFunctionKey(StudyEntry.FunctionKey.OFFICIAL_ACCOUNT);
                String progress = liveCastData.getProgress();
                liveCastEntry.setLabel(progress == null ? "" : progress);
                Map<String, Object> extraMap = new HashMap<>();
                extraMap.put(RES_OFFICIAL_ACCOUNT_ID, "13");
                extraMap.put(RES_OFFICIAL_ACCOUNT_NAME, "直播课");
                liveCastEntry.setExtra(extraMap);
                entryList.add(liveCastEntry);
            }
        }

        // #47847 阿芬提。。单双号入口
        // 接下来是所有增值产品的入口
        Map<SelfStudyType, DayRange> selfStudyTypeDayRangeMap = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentDetail.getId(), false);
        Date now = new Date();
        showMoneySSTList = showMoneySSTList.stream().filter(t -> {
            DayRange dayRange = selfStudyTypeDayRangeMap.get(t);
            if (dayRange == null)
                return false;
            long dayDiff = DateUtils.dayDiff(now, dayRange.getStartDate());
            return dayDiff <= 10;
        }).sorted((o1, o2) ->
                Integer.compare(moneySST2OrderMap.get(o1), moneySST2OrderMap.get(o2))
        ).collect(Collectors.toList());
        String reportUrl = "/view/mobile/common/activity/afentireport/index";
        for (SelfStudyType selfStudyType : showMoneySSTList) {
            FairylandProduct fairylandProduct = sst2FairyLandProductMap.get(selfStudyType);
            if (fairylandProduct == null)
                continue;
            DayRange lastDayRange = selfStudyTypeDayRangeMap.get(selfStudyType);
            if (lastDayRange == null)
                continue;
            StudyEntry studyEntry = StudyEntry.newInstance(selfStudyType, "");
            AlpsFuture<StudyAppData> appDataAlpsFuture = studyTypeFutureMap.get(selfStudyType);
            StudyAppData studyAppData = appDataAlpsFuture.getUninterruptibly();
            String progress = getProgressFromData(studyAppData);
            studyEntry.setLabel(progress);
            //如果是阿芬提语数英,进报告页面
            if (selfStudyType.isAfenti()) {
                if (selfStudyType == SelfStudyType.AFENTI_ENGLISH) {
                    //#54835 安卓2.0.0以下进入老的 报告页 url
                    if (clientIsAndroid() && VersionUtil.compareVersion(getClientVersion(), "2.0.0.0") < 0) {
                        studyEntry.touchFunctionKey(currentSchema + reportUrl + "?productType=" + selfStudyType.getOrderProductServiceType() + "&sid=" + studentDetail.getId());
                    } else
                        studyEntry.touchFunctionKey(getAfentiEnglishReportUrl() + "?sid=" + studentDetail.getId());
                } else
                    studyEntry.touchFunctionKey(currentSchema + reportUrl + "?productType=" + selfStudyType.getOrderProductServiceType() + "&sid=" + studentDetail.getId());
                studyEntry.touchFunctionType(StudyEntry.FunctionType.H5);
            } else if (selfStudyType == SelfStudyType.FEE_COURSE_UNKUNOW) { //错题精讲进 H5
                String redirectUrl = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
                if (StringUtils.isNotBlank(redirectUrl)) {
                    studyEntry.touchFunctionType(StudyEntry.FunctionType.H5);
                    studyEntry.touchFunctionKey(currentSchema + redirectUrl);
                }
            } else {
                String redirectUrl = fairylandProduct.fetchRedirectUrl(RuntimeMode.current()); //如果获取不到,则跳转到详情页。否则跳转到游戏内
                if (StringUtils.isNotBlank(redirectUrl)) {
                    VendorApps vendorApps = vendorLoaderClient.loadVendor(fairylandProduct.getAppKey());
                    studyEntry.touchFunctionType(StudyEntry.FunctionType.NATIVE).touchFunctionKey(StudyEntry.FunctionKey.FAIRYLAND_APP);
                    studyEntry.setExtra(generateFairylandExtra(fairylandProduct, vendorApps));
                } else {
                    studyEntry.touchFunctionType(StudyEntry.FunctionType.H5)
                            .touchFunctionKey(generateJumpUrl("/parentMobile/ucenter/shoppinginfo.vpage", studentDetail.getId(), fairylandProduct, fetchMainsiteUrlByCurrentSchema(), ""));
                }
            }
            //如果有 notify，则把 notify 优先显示
            if (StringUtils.isNotBlank(studyAppData.getNotifyContent()) && StringUtils.isNotBlank(studyAppData.getNotifyUniqueId())) {
                studyEntry.touchLabelReminder(studyAppData.getNotifyContent(), studyAppData.getNotifyUniqueId());
            }
            studyEntry.setIconUrl("gridfs/" + fairylandProduct.getProductIcon());
            entryList.add(studyEntry);
        }

        return entryList;
    }


    /**
     * 读取 自学类型对应的数据
     *
     * @param showMoneySSTList 我也不知道这是啥
     * @param parent           家长
     * @param studentDetail    学生
     * @param hasZuoyecuoti    是否有错题
     * @return 自学产品
     */
    private Map<SelfStudyType, AlpsFuture<StudyAppData>> getUserStudyDataFutureMap(List<SelfStudyType> showMoneySSTList, User parent, StudentDetail studentDetail, Boolean hasZuoyecuoti) {
        Map<SelfStudyType, AlpsFuture<StudyAppData>> map = new HashMap<>();
        for (SelfStudyType selfStudyType : showMoneySSTList) {
            AlpsFuture<StudyAppData> appDataFuture = mySelfStudyService.loadStudyAppData(studentDetail.getId(), selfStudyType);
            map.put(selfStudyType, appDataFuture);
        }
        AlpsFuture<StudyAppData> picListenFuture = mySelfStudyService.loadStudyAppData(studentDetail.getId(), SelfStudyType.PICLISTEN_ENGLISH);
        map.put(SelfStudyType.PICLISTEN_ENGLISH, picListenFuture);
        AlpsFuture<StudyAppData> albumFuture = mySelfStudyService.loadStudyAppData(parent.getId(), SelfStudyType.ALBUM);
        map.put(SelfStudyType.ALBUM, albumFuture);
        AlpsFuture<StudyAppData> liveCastFuture = mySelfStudyService.loadStudyAppData(parent.getId(), SelfStudyType.LIVECAST);
        map.put(SelfStudyType.LIVECAST, liveCastFuture);
        if (hasZuoyecuoti) {
            map.put(SelfStudyType.ZUOYECUOITI, mySelfStudyService.loadStudyAppData(studentDetail.getId(), SelfStudyType.ZUOYECUOITI));
        }
        return map;
    }

    private List<StudyEntry> noLoginEntryList() {
        List<StudyEntry> list = new ArrayList<>(2);
        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, "")
                .touchFunctionType(StudyEntry.FunctionType.NATIVE)
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN));

        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, "")
                .touchFunctionType(StudyEntry.FunctionType.NATIVE)
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN));

        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, "")
                .touchFunctionType(StudyEntry.FunctionType.NATIVE)
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN));

        return list;

    }

    private List<StudyEntry> noChildEntryList() {
        List<StudyEntry> list = new ArrayList<>(2);

        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, "")
                .touchFunctionType(StudyEntry.FunctionType.NATIVE)
                .touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN));

        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, "")
                .touchFunctionType(StudyEntry.FunctionType.H5)
                .touchFunctionKey(fetchMainsiteUrlByCurrentSchema() + SelfStudyType.ALBUM.getH5Url()));

        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, "")
                .touchFunctionType(StudyEntry.FunctionType.H5)
                .touchFunctionKey(fetchMainsiteUrlByCurrentSchema() + SelfStudyType.READING_ENGLISH.getH5Url()));


        return list;
    }

    /**
     * 已登录、未登录都能访问
     * 新版家校通首页的互动模块
     * 包含资讯专题和广告位
     *
     * @since 1.9.0
     */
    @RequestMapping(value = "interaction.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage interaction() {
        try {
            if (hasSessionKey()) {
                validateRequest(REQ_STUDENT_ID);
            } else {
                validateRequestNoSessionKey(REQ_STUDENT_ID);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getApiRequestUser();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        //互动信息
        Map<String, JxtNewsSubject> jxtNewsSubjectMap = jxtNewsLoaderClient.getJxtNewsSubjectBySubjectIds(subjectIds);
        List<Map<String, Object>> interactionMapList = new ArrayList<>();
        jxtNewsSubjectMap.values().forEach(jxtNewsSubject -> {
            if (MapUtils.isNotEmpty(jxtNewsSubject.getNewsRankMap()) && MapUtils.isNotEmpty(jxtNewsSubject.getCategoryNewsMap())) {
                Map<String, Integer> categoryRankMap = jxtNewsSubject.getCategoryRankMap();
                //先去排序最小的分类
                String categoryName = categoryRankMap.keySet().stream().sorted((o1, o2) -> categoryRankMap.get(o1).compareTo(categoryRankMap.get(o2))).findFirst().orElse("");
                if (StringUtils.isNotBlank(categoryName) && CollectionUtils.isNotEmpty(jxtNewsSubject.getCategoryNewsMap().get(categoryName))) {
                    //再取这个分类里面排序最大的
                    List<String> categoryNewsIds = jxtNewsSubject.getCategoryNewsMap().get(categoryName);
                    Map<String, Integer> newsRankMap = jxtNewsSubject.getNewsRankMap();
                    String newsId = categoryNewsIds.stream().sorted((o1, o2) -> newsRankMap.get(o2).compareTo(newsRankMap.get(o1))).findFirst().orElse(null);
                    JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(newsId);
                    if (jxtNews != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(RES_RESULT_NEWS_SUBJECT_ID, jxtNewsSubject.getId());
                        map.put(RES_RESULT_NEWS_SUBJECT_NAME, jxtNewsSubject.getTitle());
                        map.put(RES_RESULT_NEWS_SUBJECT_TITLE, jxtNews.getTitle());
                        map.put(RES_RESULT_NEWS_SUBJECT_URL, JxtNewsUtil.generateJxtNewsDetailView(jxtNews, "reportpage-interaction", getRequestString(REQ_APP_NATIVE_VERSION)));
                        interactionMapList.add(map);
                    }
                }
            }
        });
        MapMessage message = successMessage().add(RES_RESULT_INTERACTION_LIST, interactionMapList);
        //登录用户获取广告信息
        if (parent != null) {
            Long sid = getRequestLong(REQ_STUDENT_ID);
            String adSlotId = "220102";
            List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(parent.getId(), adSlotId, getRequestString(REQ_SYS), ver);
            if (CollectionUtils.isNotEmpty(newAdMappers)) {
                //新版本要轮播广告
                List<Map<String, Object>> adList = new ArrayList<>();
                Boolean isOldVer = VersionUtil.compareVersion(ver, "2.0.0") <= 0;
                for (int index = 0; index < newAdMappers.size(); index++) {
                    NewAdMapper mapper = newAdMappers.get(index);
                    //先打点
                    if (Boolean.TRUE.equals(mapper.getLogCollected())) {
                        LogCollector.info("sys_new_ad_show_logs",
                                MiscUtils.map(
                                        "user_id", parent.getId(),
                                        "env", RuntimeMode.getCurrentStage(),
                                        "version", getRequestString("version"),
                                        "aid", mapper.getId(),
                                        "acode", mapper.getCode(),
                                        "index", index,
                                        "slotId", "220102",
                                        "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                        "time", DateUtils.dateToString(new Date()),
                                        "agent", getRequest().getHeader("User-Agent"),
                                        "uuid", UUID.randomUUID().toString(),
                                        "system", getRequestString("sys"),
                                        "system_version", getRequestString("sysVer")
                                ));
                    }
                    if (isOldVer) {
                        Map<String, Object> adMap = new HashMap<>();
                        adMap.put(RES_RESULT_AD_IMG, combineCdbUrl(mapper.getImg()));
                        String link = AdvertiseRedirectUtils.redirectUrl(mapper.getId(), 0, ver, getRequestString(REQ_SYS), "", sid);
                        adMap.put(RES_RESULT_AD_URL, ProductConfig.getMainSiteBaseUrl() + link);
                        message.add(RES_RESULT_AD_INFO, adMap);
                        break;
                    } else {
                        Map<String, Object> map = new HashMap<>();
                        map.put(RES_RESULT_AD_IMG, combineCdbUrl(mapper.getImg()));
                        String adLink = AdvertiseRedirectUtils.redirectUrl(mapper.getId(), 0, ver, getRequestString(REQ_SYS), "", sid);
                        map.put(RES_RESULT_AD_URL, ProductConfig.getMainSiteBaseUrl() + adLink);
                        adList.add(map);
                    }
                }
                message.add(RES_RESULT_PARENT_API_AD, adList);
            }
        }
        return message;
    }

    /**
     * 学生列表上的红点提醒
     */
    @RequestMapping(value = "child_notify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getChildNotify() {
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getApiRequestUser();
        //每个孩子的提醒
        Set<Long> studentIds = studentLoaderClient.loadParentStudents(parent.getId()).stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, ReminderContext> studentReminderMap = reminderLoader.loadUsersReminder(studentIds, ReminderPosition.PARENT_APP_NEW_INDEX_CHILD);
        Map<Long, List<GroupMapper>> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentIds, false);
        Set<Long> groupIds = studentGroups.values()
                .stream()
                .flatMap(Collection::stream)
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        //每个group的提醒
        Map<Long, ReminderContext> clazzGroupsReminderMap = reminderLoader.loadClazzGroupsReminder(groupIds, ReminderPosition.PARENT_APP_NEW_INDEX_CHILD);

        //每个group新版消息提醒,分为两部分，有学科和无关学科
        Map<Long, CircleContext> subjectCircleContextMap = dpScoreCircleLoader.loadGroupLatestCircle(groupIds);
        Map<Long, CircleContext> circleContextMap = dpScoreCircleLoader.loadGroupIgnoreSubjectLatestCircle(groupIds);

        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        Map<Long, String> notifyCountMap = new HashMap<>();
        for (Long studentId : studentIds) {
            Set<Date> messageDateSet = new HashSet<>();
            //学生个人提醒
            ReminderContext studentContext = studentReminderMap.get(studentId);
            if (studentContext != null && studentContext.getLastUpdateDate() != null) {
                messageDateSet.add(studentContext.getLastUpdateDate());
            }
            //学生所在班级的全部提醒
            List<GroupMapper> groupMappers = studentGroups.get(studentId);
            if (CollectionUtils.isEmpty(groupMappers)) {
                continue;
            }
            for (GroupMapper mapper : groupMappers) {
                if (mapper == null) {
                    continue;
                }
                ReminderContext groupContext = clazzGroupsReminderMap.get(mapper.getId());
                if (groupContext != null) {
                    messageDateSet.add(groupContext.getLastUpdateDate());
                }

                if (VersionUtil.compareVersion(ver, "2.2.0") >= 0) {
                    CircleContext subjectCircleContext = subjectCircleContextMap.get(mapper.getId());
                    if (subjectCircleContext != null) {
                        messageDateSet.add(((ScoreCircleGroupContext) subjectCircleContext).getCreateDate());
                    }
                    CircleContext circleContext = circleContextMap.get(mapper.getId());
                    if (circleContext != null) {
                        messageDateSet.add(((ScoreCircleGroupContext) circleContext).getCreateDate());
                    }
                }
            }
            Date update = messageDateSet.stream().min(Comparator.reverseOrder()).orElse(null);

            notifyCountMap.put(studentId, update == null ? "" : SafeConverter.toString(update.getTime()));
        }
        return successMessage().add(RES_RESULT_PARENT_APP_INDEX_NOTIFY, notifyCountMap);
    }

    /**
     * appTab上的红点/数字提醒
     *
     * @since 1.9.0
     */
    @RequestMapping(value = "tab_notify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getAppTabNotify() {
        try {
            //学生ID传上来。怕以后啥时候也需求需要用了
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getApiRequestUser();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (ParentAppTab tab : ParentAppTab.values()) {
            ReminderContext reminderContext = reminderLoader.loadUserReminder(parent.getId(), tab.getPosition());
            if (reminderContext != null) {
                Map<String, Object> map = new HashMap<>();
                map.put(RES_RESULT_TAB_REMINDER_NAME, tab.getName());
                map.put(RES_RESULT_TAB_REMINDER_SHOW_NUMBER, tab.getPosition().getShowNumber());
                map.put(RES_RESULT_TAB_REMINDER_COUNT, reminderContext.getReminderCount());
                map.put(RES_RESULT_TAB_REMINDER_ID, SafeConverter.toString(reminderContext.getLastUpdateDate().getTime()));
                mapList.add(map);
                //要删掉历史。不能一直累计这个count
                reminderService.clearUserReminder(parent.getId(), tab.getPosition());
            }
        }
        return successMessage().add(RES_RESULT_TAB_REMINDER_LIST, mapList);
    }

    /**
     * XX学生的进步
     */
    @RequestMapping(value = "student_progress.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getStudentStudyProgress() {
        Long parentId = getCurrentParentId();
        if (parentId == null) {
            return successMessage();
        }
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId <= 0) {
            return failMessage("当前客户端版本过低，请您升级至最新版本使用，谢谢！");
        }
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        //group对应的subject
        Map<Long, Integer> groupSubject = new HashMap<>();
        groupMappers.forEach(group -> groupSubject.put(group.getId(), group.getSubject().getId()));
        List<Map<String, Object>> studyMapList = new ArrayList<>();
        Boolean needScoreLevel = Boolean.FALSE;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        //非小学账号不显示。所以直接返回了
        //审核账号不显示这个是用审核账号没有班级来控制的
        if (studentDetail == null || !studentDetail.isPrimaryStudent()) {
            return successMessage().add(RES_RESULT_STUDY_PROGRESS_SHOW, Boolean.FALSE)
                    .add(RES_RESULT_STUDY_PROGRESS_INFO_LIST, studyMapList);
        }
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList")) {
            needScoreLevel = Boolean.TRUE;
        }
        //有正确数据的学科
        Set<Subject> rightSubject = new HashSet<>();
        try {
            Map<Integer, StudyProgress> studyProgressMap = parentReportLoaderClient.getParentReportLoader().getStudyProgress(studentId, groupSubject);
            //TODO 假期实在找不到测试换据。给赵影造点。
            if (RuntimeMode.current().le(Mode.STAGING) && Objects.equals(studentId, 361240469L)) {
                StudyProgress englishProgress = new StudyProgress();
                englishProgress.setScore(100f);
                englishProgress.setHomeworkRate(1.00f);
                englishProgress.setScoreLastweek(89f);
                englishProgress.setHomeworkLastweekRate(0.66f);
                englishProgress.setWeakKp("词汇量");
                englishProgress.setSubjectId(Subject.ENGLISH.getId());
                studyProgressMap.put(Subject.ENGLISH.getId(), englishProgress);
                StudyProgress mathProgress = new StudyProgress();
                mathProgress.setScore(34f);
                mathProgress.setHomeworkRate(0.3f);
                mathProgress.setScoreLastweek(60f);
                mathProgress.setHomeworkLastweekRate(0.6f);
                mathProgress.setWeakKp("平行四边形的初步认识");
                mathProgress.setSubjectId(Subject.MATH.getId());
                studyProgressMap.put(Subject.MATH.getId(), mathProgress);
                StudyProgress chineseProgress = new StudyProgress();
                chineseProgress.setScore(65f);
                chineseProgress.setHomeworkRate(0.65f);
                chineseProgress.setScoreLastweek(65f);
                chineseProgress.setHomeworkLastweekRate(0.65f);
                chineseProgress.setSubjectId(Subject.CHINESE.getId());
                studyProgressMap.put(Subject.CHINESE.getId(), chineseProgress);
            }

            if (MapUtils.isNotEmpty(studyProgressMap)) {
                for (Integer subjectId : studyProgressMap.keySet()) {
                    StudyProgress studyProgress = studyProgressMap.get(subjectId);
                    if (studyProgress == null || studyProgress.getSubjectId() == null) {
                        continue;
                    }
                    //每个学科的进度总数据
                    Map<String, Object> progressMap = new HashMap<>();
                    //显示的分行信息
                    List<Map<String, Object>> contentList = generateContentList(studyProgress, needScoreLevel);
                    //最终的一个进度信息
                    MapMessage mapMessage = generateStudyProgressResult(studyProgress, needScoreLevel);
                    if (mapMessage.isSuccess()) {
                        progressMap.putAll(mapMessage);
                    }
                    progressMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT_LIST, contentList);
                    Subject subject = Subject.fromSubjectId(studyProgress.getSubjectId());
                    progressMap.put(RES_RESULT_STUDY_PROGRESS_SUBJECT, subject.name());
                    progressMap.put(RES_RESULT_STUDY_PROGRESS_URL, "/view/mobile/parent/progress/index.vpage?sid=" + studentId + "&subject=" + Subject.fromSubjectId(studyProgress.getSubjectId()).name());
                    studyMapList.add(progressMap);
                    //记录已经有正确结果的学科
                    rightSubject.add(subject);
                }
            }
        } catch (Exception e) {
            logger.error("athena study progress api error,{}", e);
        }
        //把没有的学科补充数据进去
        groupSubject.values().forEach(subjectId -> {
            Subject subject = Subject.fromSubjectId(subjectId);
            if (subject != Subject.UNKNOWN && !rightSubject.contains(subject)) {
                List<Map<String, Object>> contentList = new ArrayList<>();
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT, "数据出错了，请稍后刷新重试");
                contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT_COLOR, "#9B9B9B");
                contentList.add(contentMap);
                //每个学科的进度总数据
                Map<String, Object> progressMap = new HashMap<>();
                progressMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT_LIST, contentList);
                progressMap.put(RES_RESULT_STUDY_PROGRESS_SUBJECT, subject.name());
                progressMap.put(RES_RESULT_STUDY_PROGRESS_URL, "");
                studyMapList.add(progressMap);
            }
        });
        //按学科排序
        Collections.sort(studyMapList, (o1, o2) -> Subject.valueOf(SafeConverter.toString(o2.get(RES_RESULT_STUDY_PROGRESS_SUBJECT))).getId() - Subject.valueOf(SafeConverter.toString(o1.get(RES_RESULT_STUDY_PROGRESS_SUBJECT))).getId());
        //        String rankText;
//        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
//        ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(parentId);
////        Boolean isHitStudyMasterGray;
//        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
//        if (CollectionUtils.isEmpty(studentParentRefs)) {
//            return mapMessage;
//        }
//        Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
//        if (CollectionUtils.isEmpty(studentIds)) {
//            return mapMessage;
//        }
//        Map<Long, StudentDetail> detailMap = studentLoaderClient.loadStudentDetails(studentIds);
//        if (MapUtils.isEmpty(detailMap)) {
//            return mapMessage;
//        }
//        isHitStudyMasterGray = detailMap.values().stream().anyMatch(e -> grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(e, "ParentStudyMaster", "studyMasterGray"));
//        if (clazz != null && (parentExtAttribute == null || parentExtAttribute.getStudyMasterIsOpen() == null || parentExtAttribute.getStudyMasterIsOpen()) && !isHitStudyMasterGray) {
//            List<Map<String, Object>> masterCountRank = zoneLoaderClient.getZoneLoader().studyMasterCountRank(clazz, studentId);
//            Map<String, Object> masterCountMap = masterCountRank.stream().filter(e -> studentId.equals(SafeConverter.toLong(e.get("studentId")))).findFirst().orElse(null);
//            Integer selfRank = masterCountRank.indexOf(masterCountMap) + 1;
//            rankText = MessageFormat.format("班级学霸排名第{0}名", selfRank);
//            mapMessage.add(RES_RESULT_STUDY_MASTER_CONTENT, rankText).add(RES_RESULT_STUDY_MASTER_URL, "view/mobile/parent/rank/study.vpage?sid=" + studentId);
//        }
        return successMessage().add(RES_RESULT_STUDY_PROGRESS_SHOW, CollectionUtils.isNotEmpty(groupMappers))
                .add(RES_RESULT_STUDY_PROGRESS_INFO_LIST, studyMapList);
    }

    @RequestMapping(value = "background_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage backgroundInfo() {
        String imgUrl = "/public/skin/parentMobile/images/new_icon/homebg2.png";
        String bigImgUrl = "/public/skin/parentMobile/images/new_icon/homebgpad.png";
        String content = "这是一个测试文案";
        Date start = DateUtils.stringToDate("2017-07-06", DateUtils.FORMAT_SQL_DATE);
        Date end = DateUtils.stringToDate("2017-07-10 20:00:00", DateUtils.FORMAT_SQL_DATETIME);
        return successMessage().add(RES_RESULT_BACKGROUND_IMG, getCdnBaseUrlStaticSharedWithSep() + imgUrl)
                .add(RES_RESULT_BACKGROUND_BIG_IMG, getCdnBaseUrlStaticSharedWithSep() + bigImgUrl)
                .add(RES_RESULT_BACKGROUND_CONTENT, content)
                .add(RES_RESULT_BACKGROUND_START, start)
                .add(RES_RESULT_BACKGROUND_END, end);
    }

    private List<Map<String, Object>> generateContentList(StudyProgress progress, Boolean needScoreLevel) {
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (progress == null) {
            //大数据没有返回结果
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT, "上周老师未布置作业\n查看进度自主练习吧");
            contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT_COLOR, "#9EA5B8");
            mapList.add(contentMap);
            return mapList;
        }
        if (progress.getHomeworkRate() != null) {
            Map<String, Object> contentMap = new HashMap<>();
            String rate = SafeConverter.toString(new BigDecimal(progress.getHomeworkRate() * 100).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
            contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT, "上周完成率：" + rate + "%");
            contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT_COLOR, "#666666");
            mapList.add(contentMap);
        }
        if (progress.getScore() != null && VersionUtil.compareVersion(ver, "1.9.5") < 0) {
            Map<String, Object> contentMap = new HashMap<>();
            String score;
            if (needScoreLevel) {
                score = ScoreLevel.processLevel(progress.getScore().intValue()).getLevel();
            } else {
                //四舍五入
                score = SafeConverter.toString(new BigDecimal(progress.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
            }
            contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT, "上周成绩：" + score);
            contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT_COLOR, "#666666");
            mapList.add(contentMap);
        }
        if (StringUtils.isNotBlank(progress.getWeakKp())) {
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT, "建议提升：" + SafeConverter.toString(progress.getWeakKp()));
            contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT_COLOR, "#9B9B9B");
            mapList.add(contentMap);
        }
        //还有可能虽然progress不为空。但是里面数据全是空的。所以加个容错
        if (CollectionUtils.isEmpty(mapList)) {
            if (progress.getLayoutTimes() != null) {
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT, "上周没完成作业哦\n查看进度巩固练习吧");
                contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT_COLOR, "#9B9B9B");
                mapList.add(contentMap);
            } else {
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT, "上周老师未布置作业\n查看进度自主练习吧");
                contentMap.put(RES_RESULT_STUDY_PROGRESS_CONTENT_COLOR, "#9EA5B8");
                mapList.add(contentMap);
            }
        }
        return mapList;
    }

    //生成成绩的最终结果
    private MapMessage generateStudyProgressResult(StudyProgress progress, Boolean needScoreLevel) {
        if (progress == null) {
            return MapMessage.errorMessage();
        }
        MapMessage mapMessage = MapMessage.successMessage();
        if (progress.getScore() != null) {
            //分数
            String score;
            if (needScoreLevel) {
                score = ScoreLevel.processLevel(progress.getScore().intValue()).getLevel();
            } else {
                //四舍五入
                score = SafeConverter.toString(new BigDecimal(progress.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
            }
            mapMessage.add(RES_RESULT_STUDY_PROGRESS_SCORE, score);
            //算进步状态====上上周有数据才算
            if (progress.getScoreLastweek() != null) {
                if (progress.getScore() < progress.getScoreLastweek()) {
                    return mapMessage.add(RES_RESULT_STUDY_PROGRESS_RESULT_CONTENT, "仍需努力").add(RES_RESULT_STUDY_PROGRESS_RESULT_COLOR, "#FF8972");
                } else if (progress.getScore() > progress.getScoreLastweek() && progress.getHomeworkRate() != null) {
                    if (progress.getHomeworkRate() >= 0.65f) {
                        return mapMessage.add(RES_RESULT_STUDY_PROGRESS_RESULT_CONTENT, "进步").add(RES_RESULT_STUDY_PROGRESS_RESULT_COLOR, "#25CE9C");
                    } else {
                        return mapMessage.add(RES_RESULT_STUDY_PROGRESS_RESULT_CONTENT, "完成率低，仍需努力").add(RES_RESULT_STUDY_PROGRESS_RESULT_COLOR, "#FF8972");
                    }
                } else if (Objects.equals(progress.getScore(), progress.getScoreLastweek()) && progress.getHomeworkRate() != null && progress.getHomeworkLastweekRate() != null) {
                    if (progress.getHomeworkRate() >= 0.65f) {
                        if (progress.getHomeworkRate() > progress.getHomeworkLastweekRate()) {
                            return mapMessage.add(RES_RESULT_STUDY_PROGRESS_RESULT_CONTENT, "进步").add(RES_RESULT_STUDY_PROGRESS_RESULT_COLOR, "#25CE9C");
                        } else if (Objects.equals(progress.getHomeworkRate(), progress.getHomeworkLastweekRate())) {
                            return mapMessage.add(RES_RESULT_STUDY_PROGRESS_RESULT_CONTENT, "持平").add(RES_RESULT_STUDY_PROGRESS_RESULT_COLOR, "#4A90E2");
                        }
                    } else {
                        return mapMessage.add(RES_RESULT_STUDY_PROGRESS_RESULT_CONTENT, "完成率低，仍需努力").add(RES_RESULT_STUDY_PROGRESS_RESULT_COLOR, "#FF8972");
                    }
                }
            }
            return mapMessage;
        }
        return MapMessage.errorMessage();
    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    enum ParentAppTab {
        GROWTH("growth", ReminderPosition.PARENT_APP_TAB_GROWTH),
        STUDY("study", ReminderPosition.PARENT_APP_TAB_STUDY),
        LIVE("live", ReminderPosition.PARENT_APP_TAB_LIVE),
        TALK("talk", ReminderPosition.PARENT_APP_TAB_TALK),
        PERSON("person", ReminderPosition.PARENT_APP_TAB_PERSON);

        private String name;
        private ReminderPosition position;

        ParentAppTab(String tabName, ReminderPosition reminderPosition) {
            this.name = tabName;
            this.position = reminderPosition;
        }
    }
}
