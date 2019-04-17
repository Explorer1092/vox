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

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.UserAvatar;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralHistory;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.SmartClazzServiceClient;
import com.voxlearning.utopia.service.homework.api.mapper.WechatHomeworkMapper;
import com.voxlearning.utopia.service.integral.api.entities.Integral;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegralHistoryPagination;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.DisplayStudentHomeWorkHistoryMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.FairylandLoaderClient;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.ENGLISH;
import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.MATH;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.*;

/**
 * @author Hailong Yang
 * @since 09/06/2015
 */
@Controller
@RequestMapping(value = "/parentMobile/home")
@Slf4j
public class MobileParentCenterController extends AbstractMobileParentController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject private SmartClazzServiceClient smartClazzServiceClient;
    @Inject private FairylandLoaderClient fairylandLoaderClient;

    /**
     * 用来测试一下分享的页面
     */
    @RequestMapping(value = "test_share.vpage", method = RequestMethod.GET)
    public String testFeature() {
        if (RuntimeMode.isProduction()) {
            return "redirect:/";
        }

        return "mobile/test_feature/share";
    }

    /**
     * 提供一个错误页面
     */
    @RequestMapping(value = "error.vpage", method = RequestMethod.GET)
    public String common_error(Model model) {
        setRouteParameter(model);
        return "parentmobile/error";
    }

    /**
     * 新首页 -- 宝贝表现
     */
    @RequestMapping(value = "index2.vpage", method = RequestMethod.GET)
    public String Index2(Model model) {
        Long studentId = getRequestLong("sid");

        setRouteParameter(model);

        String pageAddr = "parentmobile/index2";

        //毕业账号宝贝表现提示暂不支持小学毕业账号
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        model.addAttribute("isGraduate", clazz != null && clazz.isTerminalClazz());
        model.addAttribute("hasClazz", clazz != null);

        try {
            if (currentParent() == null) {
                model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
                return pageAddr;
            }

            //1.5.3的需求=先判断家长是否有孩子
            List<User> parentStudents = studentLoaderClient.loadParentStudents(currentParent().getId());
            boolean had_kids = CollectionUtils.isNotEmpty(parentStudents);
            model.addAttribute("had_kids", had_kids);
            if (!had_kids) {
                return pageAddr;
            }
            if (studentId <= 0) {
                model.addAttribute("result", MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            if (!studentIsParentChildren(currentUserId(), studentId)) {
                model.addAttribute("result", MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                model.addAttribute("result", MapMessage.errorMessage("没有此学生").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }
            Date endDate = new Date();
            Date startDate = DateUtils.calculateDateDay(endDate, -30);
            Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false).stream()
                    .filter(e -> e != null && e.getId() != null)
                    .map(GroupMapper::getId)
                    .collect(Collectors.toSet());

            //英语作业数
            long englishHomeworkCount = newHomeworkLoaderClient.loadGroupHomeworks(groupIds, Subject.ENGLISH)
                    .filter(p -> p.getCreateTime() > startDate.getTime())
                    .count();
            //新体系数学作业数
            long mathHomeworkCount = newHomeworkLoaderClient.loadGroupHomeworks(groupIds, Subject.MATH)
                    .filter(p -> p.getCreateTime() > startDate.getTime())
                    .count();
            //新体系语文作业数
            long chineseHomeworkCount = newHomeworkLoaderClient.loadGroupHomeworks(groupIds, Subject.CHINESE)
                    .filter(p -> p.getCreateTime() > startDate.getTime())
                    .count();

            long totalCount = englishHomeworkCount + chineseHomeworkCount + mathHomeworkCount;

            Integer finishCount = studentAccomplishmentLoaderClient
                    .countByStudentIdAndAccomplishTime(studentId, startDate, endDate);

            model
                    // 使用一起作业天数
                    .addAttribute("passDaysCount", DateUtils.dayDiff(new Date(), studentDetail.getCreateTime()))
                    // 30天内完成作业
                    .addAttribute("monthFinishCount", finishCount)
                    //本月未按时完成次数
                    .addAttribute("monthUnFinishCount", totalCount < finishCount ? 0 : totalCount - finishCount)
                    // 学生姓名
                    .addAttribute("studentName", studentDetail.getProfile().getRealname())
                    // 学生头像
                    .addAttribute("studentImgUrl", getUserAvatarImgUrl(studentDetail.getProfile().getImgUrl()));

            //推荐的付费产品
            MapMessage selfChoosePracticeResult = getOneSelfChoosePractice(studentId);
            if (selfChoosePracticeResult.isSuccess()) {
                model.addAttribute("product", selfChoosePracticeResult.get("product"));
            }
        } catch (Exception ex) {
            logger.error("load student's homeworks failed. studentId:{}", studentId, ex);
            model.addAttribute("result", MapMessage.errorMessage(ex.getMessage()).setErrorCode(ApiConstants.RES_RESULT_ORDER_UNKNOWN_CODE));
            return pageAddr;
        }

        return pageAddr;
    }

    /**
     * 首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String Index(Model model) {
        Long studentId = getRequestLong("sid");

        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
        }
        model.addAttribute("isBindClazz", isBindClazz(studentId));

        //毕业账号宝贝表现提示暂不支持小学毕业账号
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        model.addAttribute("isGraduate", clazz != null && clazz.isTerminalClazz());
        model.addAttribute("hasClazz", clazz != null);

        //TODO 这个是不是可以去掉了。
        String version = getRequestString("app_version");
        model.addAttribute("isNotSupportTrust", StringUtils.isNotBlank(version) && VersionUtil.compareVersion(version, "1.3.6") < 0);

        List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        Boolean hadVhHomework = Boolean.FALSE;
//        for (GroupMapper group : groups) {
//            switch (group.getSubject()) {
//                case ENGLISH: {
//                    EnglishVacationHomework englishVacationHomework = englishVacationHomeworkLoaderClient.loadCurrentTermHomework(group.getId());
//                    hadVhHomework = englishVacationHomework != null;
//                    break;
//                }
//                case MATH: {
//                    MathVacationHomework mathVacationHomework = mathVacationHomeworkLoaderClient.loadCurrentTermHomework(group.getId());
//                    hadVhHomework = mathVacationHomework != null;
//                    break;
//                }
//                default:
//            }
//            if (hadVhHomework) {
//                break;
//            }
//        }
        model.addAttribute("hadVhHomework", hadVhHomework);

        setRouteParameter(model);

        return "parentmobile/index";
    }

    /*
     * 家长通客户端 微下载
     */
    @RequestMapping(value = "/dimensionCodeIndex.vpage", method = RequestMethod.GET)
    public String dimensionCodeIndex() {
        return "redirect:/appDownload.vpage?source=JZT&cid=" + getRequestString("cid");
    }


    /**
     * 最新作业路由
     * 貌似已经没用了。
     * TODO  这个貌似也没用了。跟路伟确认下删除吧。
     */
    @Deprecated
    @RequestMapping(value = "homework.vpage", method = RequestMethod.GET)
    //@ResponseBody
    public String homework(Model model) {
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
        }
        setRouteParameter(model);
        model.addAttribute("subject", "ENGLISH");
        return "parentmobile/homework";
    }


    /**
     * 最新作业         *
     * V3.0
     */
    @RequestMapping(value = "lastHomework.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lastHomework() {
        Long studentId = getRequestLong("sid");
        try {

            if (studentId <= 0) {
                return MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            if (!studentIsParentChildren(currentUserId(), studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);

            if (studentDetail.getClazz() == null) {
                return MapMessage.successMessage().add("englishState", "暂无").add("mathState", "暂无");
            }
            //获取学生信息

            Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false).stream().filter(p -> p.getSubject() != Subject.CHINESE).map(GroupMapper::getId).collect(Collectors.toSet());
            List<Subject> subjects = new ArrayList<>(Arrays.asList(Subject.ENGLISH, Subject.MATH));
            List<NewHomework.Location> homeworksByClazzGroupIds = newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(groupIds, subjects)
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(p -> p.getStartTime() < System.currentTimeMillis())
                    .collect(Collectors.toList());
            NewHomework.Location englishHomework = homeworksByClazzGroupIds.stream().filter(p -> p.getSubject() == Subject.ENGLISH).findFirst().orElse(null);
            NewHomework.Location mathHomework = homeworksByClazzGroupIds.stream().filter(p -> p.getSubject() == Subject.MATH).findFirst().orElse(null);

            /*
            英语和数学都是同样的逻辑，需要获取作业和测验中的最后一条作为作业，所以有作业无测验就显示作业，有测验无作业就显示测验，并列存在就比对创建时间选择最后一条
             */
            String englishState = "暂无";
            if (englishHomework != null) {
                NewAccomplishment newAccomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(englishHomework);
                boolean selfFinish = newAccomplishment == null ? Boolean.FALSE : newAccomplishment.contains(studentId);
                if (!englishHomework.isChecked() && !selfFinish && englishHomework.getEndTime() > System.currentTimeMillis()) {
                    englishState = "有作业";
                } else if ((englishHomework.isChecked() && !selfFinish) || (englishHomework.getEndTime() < System.currentTimeMillis() && !selfFinish)) {
                    englishState = "需补做";
                } else {
                    englishState = "暂无";
                }
            }

            String mathState = "暂无";
            if (mathHomework != null) {
                NewAccomplishment newAccomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(mathHomework);
                boolean selfFinish = newAccomplishment == null ? Boolean.FALSE : newAccomplishment.contains(studentId);
                if (!mathHomework.isChecked() && !selfFinish && mathHomework.getEndTime() > System.currentTimeMillis()) {
                    mathState = "有作业";
                } else if ((mathHomework.isChecked() && !selfFinish) || (mathHomework.getEndTime() < System.currentTimeMillis() && !selfFinish)) {
                    mathState = "需补做";
                } else {
                    mathState = "暂无";
                }
            }

            return MapMessage.successMessage().add("englishState", englishState).add("mathState", mathState);
        } catch (Exception ex) {
            logger.error("load student's homeworks failed. studentId:{}", studentId, ex);
            return MapMessage.errorMessage("load student's homeworks failed.").setErrorCode(ApiConstants.RES_RESULT_ORDER_UNKNOWN_CODE);
        }
    }

    /**
     * 学业报告
     */
    @RequestMapping(value = "studyTrack.vpage", method = RequestMethod.GET)
    public String studyTrack(Model model) {
        setRouteParameter(model);
        Long studentId = getRequestLong("sid");

        String referrer = getRequestString("referrer");

        String pageAddr = "parentmobile/person/studyTrack";

        if (referrer.equals("index")) {
            pageAddr = "parentmobile/homeReport/index";
        }

        try {

            if (studentId <= 0) {
                model.addAttribute("result", MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            if (currentParent() == null) {
                model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
                return pageAddr;
            }
            if (!studentIsParentChildren(currentUserId(), studentId)) {
                model.addAttribute("result", MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                model.addAttribute("result", MapMessage.errorMessage("没有此学生").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }
            //毕业账号学业报告提示暂不支持小学毕业账号
            model.addAttribute("isGraduate", studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz());
            Map<String, Object> resultMap = new HashMap<>();
            List<ClazzTeacher> clazzTeachers = userAggregationLoaderClient.loadStudentTeachers(studentId);
            boolean hasEnglishTeacher = false;
            boolean hasMathTeacher = false;
            if (CollectionUtils.isNotEmpty(clazzTeachers)) {
                for (ClazzTeacher clazzTeacher : clazzTeachers) {
                    if (clazzTeacher.getTeacher().getSubject() == Subject.ENGLISH) {
                        hasEnglishTeacher = true;
                    }

                    if (clazzTeacher.getTeacher().getSubject() == Subject.MATH) {
                        hasMathTeacher = true;
                    }
                }
            }
            resultMap.put("hasEnglishTeacher", hasEnglishTeacher);
            resultMap.put("hasMathTeacher", hasMathTeacher);
            long integral = 0;
            long gold = 0;
            if (studentDetail.getUserIntegral() != null && studentDetail.getUserIntegral().getIntegral() != null) {
                Integral studentIntegral = studentDetail.getUserIntegral().getIntegral();
                integral = studentDetail.getUserIntegral().getUsable();
                if (studentIntegral.getTotalIntegral() != null && studentIntegral.getUsableIntegral() != null) {
                    gold = studentIntegral.getTotalIntegral() - studentIntegral.getUsableIntegral();
                } else {
                    gold = studentIntegral.getTotalIntegral() == null ? 0 : studentIntegral.getTotalIntegral();
                }
            }
            Clazz clazz = studentDetail.getClazz();
            if (clazz != null) {

                //获取学霸榜
                List<Map<String, Object>> rankList;
                if (!clazz.isSystemClazz()) {
                    rankList = washingtonCacheSystem.CBS.flushable
                            .wrapCache(zoneLoaderClient.getZoneLoader())
                            .expiration(1800)
                            .keyPrefix("CLAZZ_SMCOUNT_RANK")
                            .keys(clazz.getId())
                            .proxy()
                            .studyMasterCountRank(clazz, studentId);
                } else {
                    rankList = washingtonCacheSystem.CBS.flushable
                            .wrapCache(zoneLoaderClient.getZoneLoader())
                            .expiration(1800)
                            .keyPrefix("CLAZZ_SMCOUNT_RANK")
                            .keys(clazz.getId(), studentId)
                            .proxy()
                            .studyMasterCountRank(clazz, studentId);
                }

                Map<String, Object> first = null;
                Map<String, Object> my = null;
                for (int i = 0; i < rankList.size(); i++) {
                    Map<String, Object> map = rankList.get(i);
                    map.put("index", i + 1);
                    if (i == 0) {
                        first = map;
                    }
                    if (studentId.equals(map.get("studentId"))) {
                        my = map;
                    }
                }

                if (my != null) {
                    resultMap.put("myRank", my);
                    resultMap.put("firstRank", first);
                }
            }

            long passDaysCount = TimeUnit.MILLISECONDS.toDays(DayRange.current().getEndTime() - studentDetail.getCreateTime().getTime());

            Date endDate = new Date();
            Date startDate = DateUtils.calculateDateDay(endDate, -30);
            List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
            Set<Long> groupIds = GroupMapper.filter(groupMappers).idSet();

            long englishHomeworkCount = newHomeworkLoaderClient.loadGroupHomeworks(groupIds, Subject.ENGLISH)
                    .filter(p -> p.getCreateTime() > startDate.getTime())
                    .count();
            // 数学
            long mathHomeworkCount = newHomeworkLoaderClient.loadGroupHomeworks(groupIds, Subject.MATH)
                    .filter(p -> p.getCreateTime() > startDate.getTime())
                    .count();
            // 语文
            long chineseHomeworkCount = newHomeworkLoaderClient.loadGroupHomeworks(groupIds, Subject.CHINESE)
                    .filter(p -> p.getCreateTime() > startDate.getTime())
                    .count();

            long totalCount = englishHomeworkCount + mathHomeworkCount + chineseHomeworkCount;
            //这个包含英语作业和新作业体系里的所有
            Integer finishCount = studentAccomplishmentLoaderClient.countByStudentIdAndAccomplishTime(studentId, startDate, endDate);
            //英语错题数
            Map<String, List<Map<String, Object>>> englishHomeworkWrongMap = newHomeworkLoaderClient.getStudentWrongQuestionIds(studentId, Subject.ENGLISH, null);
            //数学错题数
            Map<String, List<Map<String, Object>>> mathHomeworkWrongMap = newHomeworkLoaderClient.getStudentWrongQuestionIds(studentId, Subject.MATH, null);
            //语文的错题
            Map<String, List<Map<String, Object>>> chineseHomeworkWrongMap = newHomeworkLoaderClient.getStudentWrongQuestionIds(studentId, Subject.CHINESE, null);
            Integer totalWrongCount = 0;
            //累计英语作业错题
            for (String key : englishHomeworkWrongMap.keySet()) {
                List<Map<String, Object>> list = englishHomeworkWrongMap.get(key);
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                for (Map<String, Object> map : list) {
                    totalWrongCount += ((Set) map.get("qid")).size();
                }
            }
            //累计数学错题
            for (String key : mathHomeworkWrongMap.keySet()) {
                List<Map<String, Object>> list = mathHomeworkWrongMap.get(key);
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                for (Map<String, Object> map : list) {
                    totalWrongCount += ((Set) map.get("qid")).size();
                }
            }
            //累计语文的错题
            for (String key : chineseHomeworkWrongMap.keySet()) {
                List<Map<String, Object>> list = chineseHomeworkWrongMap.get(key);
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                for (Map<String, Object> map : list) {
                    totalWrongCount += ((Set) map.get("qid")).size();
                }
            }
            resultMap.put("totalWrongCount", totalWrongCount);
            resultMap.put("monthFinishCount", finishCount);      //30天内按时完成次数
            //TODO 这里的未完成数因为都是count。是不完全准确的。先临时处理一下。已经提需求给大数据出这个数据了。
            resultMap.put("monthUnFinishCount", totalCount < finishCount ? 0 : totalCount - finishCount);      //30天未按时完成次数
            resultMap.put("passDaysCount", passDaysCount);      //经历天数
            resultMap.put("studentImgUrl", getUserAvatarImgUrl(studentDetail.getProfile().getImgUrl()));
            resultMap.put("studentName", studentDetail.getProfile().getRealname());
            resultMap.put("studentId", studentDetail.getId());      //学号
            resultMap.put("integral", integral);//学豆
            resultMap.put("gold", gold);//作业比
            resultMap.put("englishEvaluating", getAvgScoreByHomeworkType(studentDetail, Subject.ENGLISH));//英语评测
            resultMap.put("mathEvaluating", getAvgScoreByHomeworkType(studentDetail, Subject.MATH));//数学评测
            //智慧课堂奖励学豆
            List<SmartClazzIntegralHistory> all = smartClazzServiceClient.getSmartClazzService()
                    .findSmartClazzIntegralHistoryListByUserId(studentId)
                    .getUninterruptibly()
                    .stream()
                    .filter(SmartClazzIntegralHistory::isDisplayTrue)
                    .collect(Collectors.toList());

            List<SmartClazzIntegralHistory> histories = all.stream()
                    .filter(e -> e.getSubject() == Subject.ENGLISH)
                    .collect(Collectors.toList());
            List<SmartClazzIntegralHistory> mathHistories = all.stream()
                    .filter(e -> e.getSubject() == Subject.MATH)
                    .collect(Collectors.toList());
            List<SmartClazzIntegralHistory> chineseHistories = all.stream()
                    .filter(e -> e.getSubject() == Subject.CHINESE)
                    .collect(Collectors.toList());
            histories.addAll(mathHistories);
            histories.addAll(chineseHistories);
            double smartIntegralTotal = histories.stream().filter(p -> !p.getCreateDatetime().before(MonthRange.current().getStartDate())).mapToDouble(SmartClazzIntegralHistory::getIntegral).summaryStatistics().getSum();
            resultMap.put("reward_integral", smartIntegralTotal);
            //本月学习概况
            mothStudySummary(studentDetail, resultMap);
            //英语和数学成绩详情
            //TODO 这里要再修正的时候。跪求大哥不要再把WechatHomeworkMapper这个对象引进来了。
            studyInfo(studentDetail, new ArrayList<>(), resultMap);


            //获取历史
            model.addAttribute("isBindClazz", isBindClazz(studentId))
                    .addAttribute("result", MapMessage.successMessage())
                    .addAttribute("infos", resultMap);


            return pageAddr;
        } catch (Exception ex) {
            logger.error("load student's homeworks failed. studentId:{}", studentId, ex);
            model.addAttribute("result", MapMessage.errorMessage(ex.getMessage()).setErrorCode(ApiConstants.RES_RESULT_ORDER_UNKNOWN_CODE));
            return pageAddr;
        }
    }

    /**
     * 学习轨迹查看曲线图
     * V2.0
     * TODO 看页面js已经把这个请求给注掉了。跟路伟确认下。前后端一起干掉吧。这玩意儿要做也是从大数据取数据了。
     */
    @RequestMapping(value = "studyTrackHistory.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studyTrackHistory() {
        Long studentId = getRequestLong("sid");

        try {

            if (studentId <= 0) {
                return MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            if (!studentIsParentChildren(currentUserId(), studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            //需要上周结尾时间向前推28天（前四周）的数据
            Long endTime = WeekRange.current().previous().getEndTime();
            Long startTime = DayRange.newInstance(endTime).getEndTime() - TimeUnit.DAYS.toMillis(28);
            List<Map<String, Object>> homeworks = new ArrayList<>();
            /*
            homeworkScoreStudentClassavgLoaderClient.getRemoteReference().findByHomeworkIdByTime(studentId, startTime, endTime)
                    .stream()
                    .sorted((h1, h2) -> Long.compare(h1.getHomeworkCreatetime(), h2.getHomeworkCreatetime()))
                    .forEach(h -> {
                        Map<String, Object> homework = new HashMap<>();
                        homework.put("homeworkCreatetime", h.getHomeworkCreatetime());
                        homework.put("clazzAvgScore", h.getClazzAvgScore());
                        homework.put("score", h.getScore());
                        homework.put("ht", "ENGLISH".equals(h.getSubject()) ? ENGLISH : "MENTAL_ARITHMETIC".equals(h.getSubject()) ? MATH : UNKNOWN);
                        homeworks.add(homework);
                    });
            */

            return MapMessage.successMessage()
                    .add("englishs", homeworks.stream()
                            .filter(h -> ENGLISH.equals(h.get("ht")))
                            .collect(Collectors.toList()))
                    .add("maths", homeworks.stream()
                            .filter(h -> MATH.equals(h.get("ht")))
                            .collect(Collectors.toList()));
        } catch (Exception ex) {
            logger.error("load student's homeworks failed. studentId:{}", studentId, ex);
            return MapMessage.errorMessage("查询历史数据错误").setErrorCode(ApiConstants.RES_RESULT_ORDER_UNKNOWN_CODE);
        }
    }

    /**
     * 孩子个人中心
     */
    @RequestMapping(value = "getkidsinfo.vpage", method = RequestMethod.GET)
    public String getKidsInfo(Model model) {
        Long studentId = getRequestLong(REQ_STUDENT_ID);

        String pageAddr = "parentmobile/person/childCenter";

        setRouteParameter(model);

        try {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                model.addAttribute("result", MapMessage.errorMessage("学生不存在").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }
            Clazz clazz = studentDetail.getClazz();
            School school = (clazz != null)
                    ? (schoolLoaderClient.getSchoolLoader().loadSchool(clazz.getSchoolId()).getUninterruptibly())
                    : (asyncStudentServiceClient.getAsyncStudentService().loadStudentSchool(studentId).getUninterruptibly());
            Map<String, Object> kidsMap = new HashMap<>();

            Map<String, Object> studentInfoMap = new HashMap<>();
            List<Map<String, Object>> teacherInfoList = new ArrayList<>();
            List<Map<String, Object>> productInfoList = new ArrayList<>();
            kidsMap.put("studentInfo", studentInfoMap);
            kidsMap.put("teacherInfo", teacherInfoList);
            kidsMap.put("productList", productInfoList);
            model.addAttribute("success", true);
            model.addAttribute("studentInfo", kidsMap);

            //c端学生信息
            ChannelCUserAttribute channelCUserAttribute = studentLoaderClient.loadStudentChannelCAttribute(studentId);

            //学生信息
            studentInfoMap.put("studentId", studentDetail.getId());
            studentInfoMap.put("studentImg", getParentAppImgUrlWithDefault(studentDetail));
            studentInfoMap.put("studentName", studentDetail.fetchRealname());
            studentInfoMap.put("useDayCount", DateUtils.dayDiff(new Date(), studentDetail.getCreateTime()));
            if (studentDetail.getProfile() != null) {
                if (studentDetail.getProfile().getYear() != null) {
                    //岁数精确到按天算
                    DayRange current = DayRange.current();
                    Integer studentMonth = SafeConverter.toInt(studentDetail.getProfile().getMonth());
                    Integer studentDay = SafeConverter.toInt(studentDetail.getProfile().getDay());
                    Integer studentAge;
                    if (current.getMonth() > studentMonth || (studentMonth == current.getMonth() && current.getDay() >= studentDay)) {
                        studentAge = Calendar.getInstance().get(Calendar.YEAR) - studentDetail.getProfile().getYear();
                    } else {
                        studentAge = Calendar.getInstance().get(Calendar.YEAR) - studentDetail.getProfile().getYear() - 1;
                    }
                    studentInfoMap.put("studentAge", studentAge.toString());

                }
                if (StringUtils.isNotBlank(studentDetail.getProfile().getGender())) {
                    Gender gender = Gender.fromCode(studentDetail.getProfile().getGender());
                    studentInfoMap.put("studentGender", gender == Gender.NOT_SURE ? "" : gender.getDescription());
                }
            } else {
                studentInfoMap.put("studentAge", "");
                studentInfoMap.put("studentGender", "");
            }
            //学校信息
            if (school != null) {
                studentInfoMap.put("schoolName", school.getCname());
                studentInfoMap.put("schoolId", school.getId());
            } else if (channelCUserAttribute != null) {
                //可能是个C端学生。没有School。去channelCUserAttribute里面取
                String schoolName;
                if (channelCUserAttribute.getSchoolId() != null) {
                    School s = schoolLoaderClient.getSchoolLoader()
                            .loadSchool(channelCUserAttribute.getSchoolId())
                            .getUninterruptibly();
                    schoolName = s == null ? "" : s.getShortName();
                } else {
                    schoolName = channelCUserAttribute.getSchoolName();
                }
                studentInfoMap.put("schoolName", schoolName);
                studentInfoMap.put("schoolId", SafeConverter.toLong(channelCUserAttribute.getSchoolId()));
            } else {
                studentInfoMap.put("schoolName", "");
                studentInfoMap.put("schoolId", "");
            }
            //班级信息
            if (clazz != null) {
                List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                        .findStudentIdsByClazzId(clazz.getId());
                studentInfoMap.put("hadClazz", true);
                studentInfoMap.put("canChangeClazzLevel", false);
                studentInfoMap.put("clazzId", clazz.getId().toString());
                studentInfoMap.put("clazzLevel", clazz.getClassLevel());
                studentInfoMap.put("clazzName", clazz.isTerminalClazz() ? clazz.getJie() + "届毕业班" : clazz.formalizeClazzName());
                studentInfoMap.put("clazzStudentCount", studentIds.size());
                //班级不为空才会有老师
                //老师信息
                List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
                if (CollectionUtils.isNotEmpty(groupMappers)) {
                    Set<Long> groupIds = groupMappers.stream().map(GroupMapper::getId).collect(Collectors.toSet());
                    Map<Long, List<GroupTeacherTuple>> groupTeacherMaps = raikouSDK.getClazzClient()
                            .getGroupTeacherTupleServiceClient()
                            .groupByGroupIds(groupIds);
                    if (MapUtils.isNotEmpty(groupTeacherMaps)) {
                        List<GroupTeacherTuple> teacherRefList = groupTeacherMaps
                                .values()
                                .stream()
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());

                        if (CollectionUtils.isNotEmpty(teacherRefList)) {
                            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherRefList.stream().map(GroupTeacherTuple::getTeacherId).collect(Collectors.toSet()));
                            teacherMap.values().stream().forEach(p -> {
                                if (p != null) {
                                    Map<String, Object> info = new HashMap<>();
                                    info.put("teacherImg", p.fetchImageUrl());
                                    info.put("teacherName", p.fetchRealname());
                                    info.put("teacherId", p.getId());
                                    info.put("subject", p.getSubject());
                                    teacherInfoList.add(info);
                                }
                            });
                        }
                    }
                }


            } else if (channelCUserAttribute != null) {
                //可能是个C端学生。没有Clazz。去StudentChannelCAttribute里面取
                studentInfoMap.put("hadClazz", false);
                studentInfoMap.put("canChangeClazzLevel", channelCUserAttribute.getJoinClazzTime() == null);
                studentInfoMap.put("clazzId", "");
                ChannelCUserAttribute.ClazzCLevel clazzCLevel = ChannelCUserAttribute.getClazzCLevelByClazzJie(channelCUserAttribute.getClazzJie());
                studentInfoMap.put("clazzLevel", "");
                studentInfoMap.put("clazzName", clazzCLevel == null ? "" : clazzCLevel.getDescription());
                studentInfoMap.put("clazzStudentCount", "");
            } else {
                studentInfoMap.put("hadClazz", false);
                studentInfoMap.put("canChangeClazzLevel", false);
                studentInfoMap.put("clazzId", "");
                studentInfoMap.put("clazzLevel", "");
                studentInfoMap.put("clazzName", "");
                studentInfoMap.put("clazzStudentCount", "");
            }

            MapMessage message = MapMessage.successMessage().add("studentInfo", studentInfoMap).add("teacherInfo", teacherInfoList);
            //订单信息
            User user = raikouSystem.loadUser(studentId);
            Map<Long, Boolean> blackSchoolOrBlackUser = isBlackSchoolOrBlackUser(Collections.singleton(user));
            if (MapUtils.isEmpty(blackSchoolOrBlackUser) || !blackSchoolOrBlackUser.containsKey(user.getId()) || Boolean.FALSE.equals(blackSchoolOrBlackUser.get(user.getId()))) {
                List<String> appKeys = vendorLoaderClient.loadVendorAppsIncludeDisabled().values().stream()
                        .filter(v -> v.isVisible(RuntimeMode.current().getLevel()))
                        .filter(VendorApps::getWechatBuyFlag)
                        .map(VendorApps::getAppKey)
                        .collect(Collectors.toList());

                if (CollectionUtils.isEmpty(appKeys)) {
                    model.addAttribute("result", MapMessage.successMessage().add("studentInfo", studentInfoMap).add("teacherInfo", teacherInfoList).add("productList", productInfoList));
                    return pageAddr;
                }
                Map<String, AppPayMapper> appPaidStatus = userOrderLoaderClient.getUserAppPaidStatus(appKeys, studentId, false);
                if (MapUtils.isEmpty(appPaidStatus)) {
                    model.addAttribute("result", MapMessage.successMessage().add("studentInfo", studentInfoMap).add("teacherInfo", teacherInfoList).add("productList", productInfoList));
                    return pageAddr;
                }

                List<FairylandProduct> fairylandProducts = fairylandLoaderClient.loadFairylandProducts(FairyLandPlatform.PARENT_APP, FairylandProductType.APPS);
                Map<String, String> usePlatforms = fairylandProducts.stream().filter(p -> appPaidStatus.keySet().contains(p.getAppKey())).collect(Collectors.toMap(FairylandProduct::getAppKey, FairylandProduct::getUsePlatformDesc));
                appPaidStatus.keySet().stream().forEach(p -> {
                    AppPayMapper appStatus = appPaidStatus.get(p);
                    int status = appStatus == null ? 0 : SafeConverter.toInt(appStatus.getAppStatus());
                    Map<String, Object> map = new HashMap<>();
                    map.put("appKey", p);
                    FairylandProduct product = fairylandProducts.stream().filter(t -> StringUtils.equals(t.getAppKey(), p)).findFirst().orElse(null);
                    String productName = product == null ? "" : product.getProductName();
                    map.put("productName", productName);
                    if (StringUtils.isNotBlank(productName) && productName.contains(" ")) {
                        map.put("productName", productName.substring(0, productName.indexOf(" ")));
                    }
                    Boolean isExpire = status == 1;
                    map.put("isExpire", isExpire);
                    if (appStatus.isActive()) {
                        map.put("dayToExpire", appStatus.getDayToExpire());
                    }
                    map.put("usePlatformDesc", SafeConverter.toString(usePlatforms.get(p)));

                    Boolean isNotBought = status == 0;
                    if (!isNotBought) {
                        productInfoList.add(map);
                    }
                });
                message.add("productList", productInfoList);
            }
            model.addAttribute("isGraduate", clazz != null && clazz.isTerminalClazz());
            model.addAttribute("result", message);
            model.addAttribute("isNewVer", VersionUtil.compareVersion(getRequestString("app_version"), "1.5.5") >= 0);
            return pageAddr;
        } catch (Exception ex) {
            String info;
            if (ex instanceof UtopiaRuntimeException || ex instanceof IllegalArgumentException) {
                info = ex.getMessage();
            } else {
                info = "获取子女信息失败";
            }

            model.addAttribute("result", MapMessage.errorMessage(info).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return pageAddr;
        }
    }

    @RequestMapping(value = "update_student_gender.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateStudentGender() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("获取用户信息失败").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String genderStr = getRequestString(REQ_USER_GENDER);
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        if (StringUtils.isBlank(genderStr)) {
            return MapMessage.errorMessage("学生性别不能为空").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        Gender gender = Gender.fromCode(genderStr);
        //update gender
        MapMessage mapMessage = userServiceClient.changeGender(studentId, gender.getCode());
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage("更新学生性别失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        return MapMessage.successMessage();
    }


    @RequestMapping(value = "update_student_birthday.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateStudentBirthday() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("获取用户信息失败").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String birthDay = getRequestString(REQ_BIRTHDAY);
        if (StringUtils.isBlank(birthDay)) {
            return MapMessage.errorMessage("学生生日不能为空").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        Integer year, month, day;
        try {
            //设置生日
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(birthDay));
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            day = calendar.get(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            return MapMessage.errorMessage("学生生日错误").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        //update birthday
        MapMessage mapMessage = userServiceClient.changeUserBirthday(studentId, year, month, day);
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage("更新学生生日失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "update_channel_c_student_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateChannelCStudentSchool() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("获取用户信息失败").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        String schoolName = getRequestString(REQ_SCHOOL_NAME);
        Integer regionCode = getRequestInt(REQ_REGION_CODE);
        // schoolId!=0时存schoolId。schoolId==0.存schoolName
        if (schoolId == 0 && StringUtils.isBlank(schoolName)) {
            return MapMessage.errorMessage(RES_RESULT_SCHOOL_INFO_ERROR_MSG).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        MapMessage mapMessage = studentServiceClient.updateChannelCStudentSchoolIdOrName(studentId, schoolId, schoolName, regionCode);
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage("更新学生学校失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "update_channel_c_student_clazz_level.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateChannelCStudentClazzLevel() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Integer clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL_PARENT, -1);
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("获取用户信息失败").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (clazzLevel == -1)
            return MapMessage.errorMessage("参数错误");
        if (clazzLevel != 0) {
            ClazzLevel level = ClazzLevel.parse(clazzLevel);
            if (level == null || level.getLevel() > 9) {
                return MapMessage.errorMessage(RES_RESULT_CLAZZ_LEVEL_ERROR).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
        }
        MapMessage mapMessage = studentServiceClient.updateChannelCStudentClazzLevel(studentId, clazzLevel);
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage("更新学生年级失败");
        }
        return MapMessage.successMessage();
    }

    /**
     * 学豆榜 页面路由
     */
    @RequestMapping(value = "integralchip.vpage", method = RequestMethod.GET)
    public String integralChip(Model model) {
        setRouteParameter(model);
        Long studentId = getRequestLong("sid");
        Integer pageNumber = getRequestInt("pageIndex", 1);

        String pageAddr = "parentmobile/integralChip";

        model.addAttribute("result", MapMessage.successMessage());
        model.addAttribute("currentPage", pageNumber);

        if (studentId <= 0) {
            model.addAttribute(
                    "result",
                    MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE)
            );

            return pageAddr;
        }

        User user = currentUser();

        if (user == null) {
            model.addAttribute(
                    "result",
                    MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE)
            );
            return pageAddr;
        }

        if (!studentIsParentChildren(user.getId(), studentId)) {
            model.addAttribute(
                    "result",
                    MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE)
            );
            return pageAddr;
        }

        return pageAddr;
    }

    /**
     * 家长通学豆榜
     */
    @RequestMapping(value = "getIntegralChip.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getIntegralChip() {
        Long studentId = getRequestLong("sid");
        if (studentId <= 0) {
            return MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }

        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        if (!studentIsParentChildren(user.getId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        User student = userLoaderClient.loadUser(studentId, UserType.STUDENT);
        Integer pageNumber = getRequestInt("pageIndex", 1);
        // 获取银币前三个月的历史数据
        UserIntegralHistoryPagination pagination = userLoaderClient.loadUserIntegralHistories(
                student, 3, pageNumber - 1, 10);
        return MapMessage.successMessage()
                .add("pagination", pagination)
                .add("integral", pagination.getUsableIntegral())
                .add("currentPage", pageNumber);
    }


    @RequestMapping(value = "default_avatar_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getDefaultAvatarList() {
        Long userId = currentUserId();
        if (userId == null) {
            return noLoginResult;
        }
        User user = raikouSystem.loadUser(userId);
        if (user == null) {
            return noLoginResult;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (UserAvatar avatar : UserAvatar.getParentAvatars()) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", avatar.getKey());
            map.put("url", getUserAvatarImgUrl(avatar.getUrl()));
            map.put("is_used", avatar.getUrl().equals(user.fetchImageUrl()));
            mapList.add(map);
        }
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(userId);
        Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
        Boolean uploadAvatar = userLevelLoader.hasPrivilegeForUploadAvatar(studentIds);
        return MapMessage.successMessage()
                .add("default_avatar_list", mapList)
                .add("user_avatar", getUserAvatarImgUrl(user.fetchImageUrl()))
                .add("can_upload_avatar", uploadAvatar);
    }


    private Integer getAvgScoreByHomeworkType(StudentDetail studentDetail, Subject subject) {
        Date currentDate = new Date();
        Date startDate = DateUtils.calculateDateDay(currentDate, -30);
        List<DisplayStudentHomeWorkHistoryMapper> homeworkHistoryList = newHomeworkReportServiceClient.loadStudentNewHomeworkHistory(studentDetail, startDate, currentDate);

        int totalScore = 0;
        int finishCount = 0;
        for (Iterator<DisplayStudentHomeWorkHistoryMapper> it = homeworkHistoryList.iterator(); it.hasNext(); ) {
            DisplayStudentHomeWorkHistoryMapper hm = it.next();
            if (hm.getSubject().equals(subject)) {
                if (hm.getHomeworkScore() != null) {
                    totalScore += hm.getHomeworkScore();
                    finishCount += 1;
                }
            } else {
                it.remove();
            }
        }
        Integer avgScore = null;
        if (finishCount > 0) {
            avgScore = new BigDecimal(totalScore).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
        }
        return avgScore;
    }

    //英语成绩和数学成绩详情
    private void studyInfo(StudentDetail studentDetail, List<WechatHomeworkMapper> mathHomeworks, Map<String, Object> resultMap) {
        //TODO 这些平均是先这样直接返回。需要有大数据去出数据
        resultMap.put("currentWeekAvgScore", 0);  //本周数学平均分
        resultMap.put("beforeWeekAvgScore", 0);    //上周数学平均分
        resultMap.put("needLearnWordsCount", 0);         //班级要求掌握词汇量
        resultMap.put("learnWordsCount", 0);             //此学生掌握的词汇量
        resultMap.put("firstWordsCount", 0);             //班平均掌握词汇量
        resultMap.put("avglearnwords", 0);              //班平均掌握词汇量

//        mathHomeworks = mathHomeworks.stream()
//                .filter(h -> h.getCreateTime().after(DateUtils.calculateDateDay(new Date(), -14)))
//                .filter(h -> h.getEndTime().before(new Date()) || h.isChecked())
//                .collect(Collectors.toList());
//        List<WechatHomeworkMapper> currentWeek = new ArrayList<>(); //本周数学作业
//        List<WechatHomeworkMapper> beforeWeek = new ArrayList<>(); //上周数学作业
//        mathHomeworks.stream().forEach(h -> {
//            //这个方法里面就是算个人得分和班级平均分
//            //涉及依赖问题被我直接干掉了。啥时候这个方法打开还需要算的话。这里算一下。
////            getScore(h);
//            if (h.getCreateTime().after(DateUtils.calculateDateDay(new Date(), -7))) {
//                currentWeek.add(h);
//            } else {
//                beforeWeek.add(h);
//            }
//        });
//        int currentWeekAvgScore = currentWeek.stream().mapToInt(WechatHomeworkMapper::getScore).reduce((o1, o2) -> o1 + o2).orElseGet(() -> 0);
//        int beforeWeekAvgScore = beforeWeek.stream().mapToInt(WechatHomeworkMapper::getScore).reduce((o1, o2) -> o1 + o2).orElseGet(() -> 0);
//        resultMap.put("currentWeekAvgScore", currentWeekAvgScore != 0 ? currentWeekAvgScore / currentWeek.size() : 0);  //本周数学平均分
//        resultMap.put("beforeWeekAvgScore", beforeWeekAvgScore != 0 ? beforeWeekAvgScore / beforeWeek.size() : 0);    //上周数学平均分
//
////        SchoolYear schoolYear = SchoolYear.newInstance(DayRange.current().previous().getEndDate());   //前一天所对应的学年
////        int termId = schoolYear.year() * 10 + schoolYear.currentTerm().ordinal(); //学期:年份*10 + 学期
////        HomeworkWeekReport homeworkWeekReport = homeworkLoaderClient.findReportByTermIdAndStudentId(termId, studentDetail.getId());
//
//        resultMap.put("needLearnWordsCount", 0);         //班级要求掌握词汇量
//        resultMap.put("learnWordsCount", 0);             //此学生掌握的词汇量
//        resultMap.put("firstWordsCount", 0);             //班平均掌握词汇量
//        resultMap.put("avglearnwords", 0);              //班平均掌握词汇量

        //TODO 第一期英语迁移。先不考虑报告了。所以这里先这样处理着
//        if (homeworkWeekReport != null) {
//            resultMap.put("needLearnWordsCount", homeworkWeekReport.getWordsCnt());         //班级要求掌握词汇量
//            long learnWordsCount = 0;
//            if (homeworkWeekReport.getWordsPracticeCnt() > 0) {
//                learnWordsCount = homeworkWeekReport.getWordsPracticeScoreSum() * homeworkWeekReport.getWordsCnt() / homeworkWeekReport.getWordsPracticeCnt() / 100;
//            }
//            resultMap.put("learnWordsCount", learnWordsCount);    //此学生掌握的词汇量
//            //查询班级第一名
//            /*Set<Long> studentIds = studentLoaderClient.loadStudentClassmates(studentDetail.getId()).stream()
//                    .map(User::getId)
//                    .collect(Collectors.toSet());*/
//            //求班级平均分
//            Set<Long> studentIds = studentLoaderClient.loadClazzStudents(studentDetail.getClazzId()).stream().map(User::getId)
//                    .collect(Collectors.toSet());
//            List<HomeworkWeekReport> reports = homeworkLoaderClient.findReportByTermIdAndStudentIds(termId, studentIds);
//            long firstWordsCount = 0;
//            long sum = 0;
//            for (HomeworkWeekReport report : reports) {
//                if (homeworkWeekReport.getWordsPracticeCnt() <= 0) {
//                    continue;
//                }
//                if (report.getWordsPracticeCnt() > 0) {
//                    long temp = report.getWordsPracticeScoreSum() * report.getWordsCnt() / report.getWordsPracticeCnt() / 100;
//                    firstWordsCount = Math.max(temp, firstWordsCount);
//                    sum += temp;
//                }
//            }
//            resultMap.put("firstWordsCount", Math.max(learnWordsCount, firstWordsCount)); //班级第一词汇量
//            resultMap.put("avglearnwords", sum != 0 ? sum / reports.size() : 0); //班平均掌握词汇量
//        }
    }

    //本月学习概况
    private void mothStudySummary(StudentDetail studentDetail, Map<String, Object> resultMap) {
        //本月多少
        int days = SafeConverter.toInt(DateUtils.dayDiff(MonthRange.current().getStartDate(), new Date())) - 1;
        //緩存1小时
//        Map<Long, Map<String, MutableInt>> studentInfoMap = newHomeworkCacheClient.cacheSystem.CBS.flushable.wrapCache(newHomeworkResultLoaderClient)
//                .expiration(60 * 60 * 1)
//                .keyPrefix("studentHomeworkCurrentMonthRank")
//                .keys(studentDetail.getClazzId(), studentDetail.getId())
//                .proxy().getCurrentMonthHomeworkRankByGroupId(studentDetail.getClazzId(), studentDetail.getId());
        Map<Long, Map<String, Integer>> studentInfoMap = newHomeworkResultLoaderClient.getCurrentMonthHomeworkRankByGroupId(studentDetail.getId());
        Map<String, Integer> studentInfo = studentInfoMap.get(studentDetail.getId());
        if (studentInfoMap.isEmpty() || studentInfo == null) {
            resultMap.put(RES_SURPASS_COUNT, 0);
            resultMap.put(RES_HOMEWORK_AVG_SCORE, 0);
            resultMap.put(RES_HOMEWORK_FINISH_COUNT, 0);
        } else {
            int surpassCount = 0;
            int myAvgScore = 0;
            if (studentInfo.get("finishCount").intValue() > 0) {
                myAvgScore = new BigDecimal(studentInfo.get("finishTotalScore").intValue()).divide(new BigDecimal(studentInfo.get("finishCount").intValue()), 0, BigDecimal.ROUND_HALF_UP).intValue();
            }

            for (Long uid : studentInfoMap.keySet()) {
                Map<String, Integer> si = studentInfoMap.get(uid);
                int avgScore = 0;
                if (si.get("finishCount").intValue() > 0) {
                    avgScore = new BigDecimal(si.get("finishTotalScore").intValue()).divide(new BigDecimal(si.get("finishCount").intValue()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                }
                if (avgScore < myAvgScore) {
                    surpassCount++;
                }
            }
            resultMap.put("surpassCount", surpassCount);         //超过同学数据量
            resultMap.put("hwAvgScore", myAvgScore);      //准确率
            resultMap.put("hwFinishCount", studentInfo.get("finishCount"));   //完成作业次数===貌似前台没用
            resultMap.put("questionNum", studentInfo.get("questionNum"));   //完成题数
        }
    }


    private String getParentAppImgUrlWithDefault(User user) {
        String parentDefaultUrl = "/public/skin/parentMobile/images/new_icon/avatar_parent_default.png";
        String studentDefaultUrl = "/public/skin/parentMobile/images/new_icon/avatar_child_default.png";
        String img;
        if (StringUtils.isBlank(user.fetchImageUrl())) {
            if (user.fetchUserType() == UserType.PARENT) {
                img = parentDefaultUrl;
            } else {
                img = studentDefaultUrl;
            }
            return getCdnBaseUrlStaticSharedWithSep() + img;
        } else {
            return getUserAvatarImgUrl(user.fetchImageUrl());
        }
    }
}
