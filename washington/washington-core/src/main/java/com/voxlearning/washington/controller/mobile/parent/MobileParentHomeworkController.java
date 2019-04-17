
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

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralHistory;
import com.voxlearning.utopia.service.clazz.client.SmartClazzServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.UNKNOWN;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Hailong Yang
 * @since 2015/09/14
 */
@Controller
@RequestMapping(value = "/parentMobile/homework")
@Slf4j
public class MobileParentHomeworkController extends AbstractMobileParentController {

    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;
    @Inject private SmartClazzServiceClient smartClazzServiceClient;

    @Inject
    private NewHomeworkLoaderClient newHomeworkLoaderClient;

    public static Set<Integer> cuotijinglianClazzLevelSet = new HashSet<>();

    static {
        cuotijinglianClazzLevelSet.add(1);
    }

    // 错题本列表
    @RequestMapping(value = "wrongquestionlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage wrongQuestionList() {
        Long studentId = getRequestLong("sid");
        Subject subject = Subject.ofWithUnknown(getRequestString("subject"));
        User parent = currentParent();
        try {
            if (studentId <= 0 || subject == Subject.UNKNOWN) {
                return MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            if (parent == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            if (!studentIsParentChildren(currentUserId(), studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            //新体系的所有作业和测验
            Map<String, List<Map<String, Object>>> homeworkWrongMap = newHomeworkLoaderClient.getStudentWrongQuestionIds(studentId, subject, null);
            //排序日期
            Set<String> dates = new HashSet<>();
            dates.addAll(homeworkWrongMap.keySet());
            List<String> dateList = new ArrayList<>(dates);
            Collections.sort(dateList, (o1, o2) -> {
                long t1 = com.voxlearning.alps.calendar.DateUtils.stringToDate(o1, "yyyy.MM.dd").getTime();
                long t2 = com.voxlearning.alps.calendar.DateUtils.stringToDate(o2, "yyyy.MM.dd").getTime();
                return Long.compare(t2, t1);
            });
            List<Map<String, Object>> returnMapList = new ArrayList<>();
            for (String date : dateList) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", date);
                List<Map<String, Object>> homeworkMapList = new ArrayList<>();
                List<Map<String, Object>> homeworkWrongList = homeworkWrongMap.get(date);
                //每天的作业错题列表
                if (CollectionUtils.isNotEmpty(homeworkWrongList)) {
                    homeworkWrongList.stream().forEach(p -> {
                        Map<String, Object> wrongCountMap = new HashMap<>();
                        wrongCountMap.put("wrongCount", ((Set) p.get("qid")).size());
                        wrongCountMap.put("homeworkId", p.get("homeworkId"));
                        wrongCountMap.put("homeworkType", p.get("homeworkType"));
                        homeworkMapList.add(wrongCountMap);
                    });
                }
                map.put("homeworkMapList", homeworkMapList);
                returnMapList.add(map);
            }
            //根据学科判断是否是vip
            boolean isVip;
            if (subject == Subject.CHINESE) {
                isVip = false;
            } else {
                OrderProductServiceType type = subject == Subject.ENGLISH ? OrderProductServiceType.AfentiExam : OrderProductServiceType.AfentiMath;
                UserActivatedProduct product = userOrderLoaderClient.loadUserActivatedProductList(studentId).stream()
                        .filter(p -> OrderProductServiceType.safeParse(p.getProductServiceType()) == type)
                        .sorted((o1, o2) -> Long.compare(o2.getUpdateDatetime().getTime(), o1.getUpdateDatetime().getTime()))
                        .findFirst().orElse(null);
                isVip = null != product && product.getServiceEndTime().getTime() > System.currentTimeMillis();
            }

            return MapMessage.successMessage().add("wrongList", returnMapList)
                    .add("subject", subject)
                    //是否开通阿凡提，目前微信是按是否购买过学豆判断的
                    .add("isVip", isVip);
        } catch (Exception ex) {
            logger.error("load student's wrong questions failed. studentId:{},subject:{}", studentId, subject, ex);
            return MapMessage.errorMessage("查看错题本列表失败").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    //错题本详情=某次作业具体的错题Id
    @RequestMapping(value = "wrongQuestionDetail.vpage", method = RequestMethod.GET)
    public String wrongQuestionDetail(Model model) {
        Long studentId = getRequestLong("sid");
        HomeworkType homeworkType = HomeworkType.parse(getRequestString("homeworkType"));
        String homeworkId = getRequestString("homeworkId");
        setRouteParameter(model);
        String pageAddr = "parentmobile/wrongQuestionDetail";

        if (studentId <= 0 || homeworkType == null || StringUtils.isBlank(homeworkId)) {
            model.addAttribute("result", MapMessage.errorMessage("非法参数").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return pageAddr;
        }
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
            return pageAddr;
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            model.addAttribute("result", MapMessage.errorMessage("学生号错误").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
            return pageAddr;
        }
        try {
            Map<String, List<Map<String, Object>>> wrongListMap = newHomeworkLoaderClient.getStudentWrongQuestionIds(studentId, homeworkType.getSubject(), Collections.singleton(homeworkId));
            if (MapUtils.isEmpty(wrongListMap)) {
                model.addAttribute("result", MapMessage.errorMessage("该作业没有错题").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }
            List<Map<String, Object>> mapList = wrongListMap.get(new ArrayList<>(wrongListMap.keySet()).get(0));
            if (CollectionUtils.isEmpty(mapList)) {
                model.addAttribute("result", MapMessage.errorMessage("该作业没有错题").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }
            Map<String, Object> wrongMap = mapList.get(0);
            Set<String> wrongQuestionIds = (Set) wrongMap.get("qid");

            //根据学科判断是否是vip
            boolean isVip;
            if (homeworkType.getSubject() == Subject.CHINESE) {
                isVip = false;
            } else {
                OrderProductServiceType type = homeworkType.getSubject() == Subject.ENGLISH ? OrderProductServiceType.AfentiExam : OrderProductServiceType.AfentiMath;
                UserActivatedProduct product = userOrderLoaderClient.loadUserActivatedProductList(studentId).stream()
                        .filter(p -> OrderProductServiceType.safeParse(p.getProductServiceType()) == type)
                        .sorted((o1, o2) -> Long.compare(o2.getUpdateDatetime().getTime(), o1.getUpdateDatetime().getTime()))
                        .findFirst().orElse(null);
                isVip = null != product && product.getServiceEndTime().getTime() > System.currentTimeMillis();
            }
            String completeDetailUrl = "/parentMobile/homework/answers.vpage?sid=" + studentId + "&homeworkType=" + homeworkType + "&homeworkId=" + homeworkId;
            model.addAttribute("result", MapMessage.successMessage().add("eids", wrongQuestionIds).add("completeUrl", completeDetailUrl)).addAttribute("isVip", isVip);

            //跳阿芬提
            Map<String, Object> jumpKey = generateJumpKey(homeworkType.getSubject(), studentDetail, currentParent());
            if (jumpKey != null)
                model.addAttribute("jump_afenti", jumpKey);
        } catch (Exception ex) {
            logger.error("Parent {} get child {} unit {} report detail error.", currentParent().getId(), ex);
            model.addAttribute("result", MapMessage.errorMessage("错题详情详情错误").setErrorCode(ApiConstants.RES_RESULT_INTERNAL_ERROR_CODE));
        }

        return pageAddr;
    }

    private Map<String, Object> generateJumpKey(Subject subject, StudentDetail studentDetail, User parent) {
        String appKey = "";
        if (subject == Subject.ENGLISH)
            appKey = SelfStudyType.AFENTI_ENGLISH.getOrderProductServiceType();
        else if (subject == Subject.CHINESE)
            appKey = SelfStudyType.AFENTI_CHINESE.getOrderProductServiceType();
        else if (subject == Subject.MATH)
            appKey = SelfStudyType.AFENTI_MATH.getOrderProductServiceType();
        else
            return null;
        List<FairylandProduct> parentAvailableFairylandProducts = businessVendorServiceClient.getParentAvailableFairylandProducts(parent
                , studentDetail, FairyLandPlatform.PARENT_APP, FairylandProductType.APPS);
        Map<String, FairylandProduct> fairylandProductMap = parentAvailableFairylandProducts.stream().collect(Collectors.toMap(FairylandProduct::getAppKey, Function.identity()));
        FairylandProduct fairylandProduct = fairylandProductMap.get(appKey);
        if (fairylandProduct == null) {
            return null;
        }
        Map<String, VendorApps> vendorAppsMap = vendorLoaderClient.loadVendorAppsIncludeDisabled()
                .values()
                .stream()
                .collect(Collectors.toMap(VendorApps::getAppKey, e -> e));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("appKey", fairylandProduct.getAppKey());
        map.put("launchUrl", fairylandProduct.fetchRedirectUrl(RuntimeMode.current()));
        VendorApps vendorApps = vendorAppsMap.get(fairylandProduct.getAppKey());
        if (vendorApps == null)
            return null;
        map.put("orientation", vendorApps.getOrientation());
        map.put("browser", vendorApps.getBrowser());
        return map;
    }


    /**
     * 帮助H5解决跨域访问
     * TODO 修改名称  这个名称太大了  或者修改函数 让其支持各种类型  (暂时没有想到好名字  以后持续优化吧ve)
     */
    @RequestMapping(value = "getBody.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getBody() throws IOException {
        String url = getRequestString("url");
        String method = getRequestParameter("method", "get");
        String stringJson = getRequestString("data");

        try {
            if (url == null) {
                return MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            Map<String, String> map = new HashMap<>();
            if (stringJson != null) {
                map.put("data", stringJson);
            }


            AlpsHttpResponse response;
            switch (method) {
                case "get":
                    String URL = UrlUtils.buildUrlQuery(url, map);
                    response = HttpRequestExecutor.defaultInstance().get(URL).execute();
                    break;
                case "post":
                    POST post = HttpRequestExecutor.defaultInstance().post(url);
                    map.entrySet().forEach(e -> {
                        String name = e.getKey();
                        String value = e.getValue();
                        post.addParameter(name, value);
                    });
                    response = post.execute();
                    break;
                default:
                    throw new IllegalArgumentException("Unrecoginized method: " + method);
            }


            if (response.getStatusCode() == 200) {
                return MapMessage.successMessage().add("body", response.getResponseString());
            }
            return MapMessage.errorMessage(response.getStatusCode() + ":" + response.getResponseString()).setErrorCode(String.valueOf(response.getStatusCode()));
        } catch (Exception ex) {
            return MapMessage.errorMessage("调取url错误").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

    }

    //学习轨迹中的“查看错题本”按钮的url
    //只做路由功能
    @RequestMapping(value = "homeworkReport.vpage", method = RequestMethod.GET)
    public String homeworkReport(Model model) {

        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
        }

        model.addAttribute("result", MapMessage.successMessage());
        setRouteParameter(model);
        return "parentmobile/homeworkReport";
    }


    //分享成绩单的路由
    @RequestMapping(value = "share.vpage", method = RequestMethod.GET)
    public String share(Model model) {
        setRouteParameter(model);
        return "parentmobile/share";
    }

    //贡献班级学豆
    @RequestMapping(value = "giveBean.vpage", method = RequestMethod.GET)
    public String giveBean(Model model) {
        Long studentId = getRequestLong("sid");
        Boolean isActivity = getRequestBool("isActivity");
        if (currentParent() == null) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
        }
        if (!studentIsParentChildren(currentUserId(), studentId)) {
            model.addAttribute("result", MapMessage.errorMessage("此学生与家长号无关联").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
        }


        setRouteParameter(model);
        List<Map<String, Object>> teacherMaps = new ArrayList<>();
        userAggregationLoaderClient.loadStudentTeachers(studentId)
                .stream()
                .map(ClazzTeacher::getTeacher)
                .filter(t -> t.getSubject() != null)
                .filter(t -> isActivity || t.fetchCertificationState() == AuthenticationState.SUCCESS)
                .forEach(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("teacherId", t.getId());
                    map.put("teacherName", t.fetchRealname());
                    map.put("subject", t.getSubject());
                    teacherMaps.add(map);
                });
        model.addAttribute("teachers", teacherMaps);
        model.addAttribute("isActivity", isActivity);
        return "parentmobile/giveBean";
    }


    // 家长假期作业入口
    @RequestMapping(value = "vhindex.vpage", method = RequestMethod.GET)
    public String parentVacationHomeworkIndex(Model model) {
        Long studentId = getRequestLong("sid");
        setRouteParameter(model);
        List<Map<String, Object>> result = new ArrayList<>();
//        List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(studentId, false);
//        for (GroupMapper group : groups) {
//            switch (group.getSubject()) {
//                case ENGLISH: {
//                    CollectionUtils.addNonNullElement(result, englishVacationHomeworkService
//                            .englishVacationHomeworkProgress(studentId, group.getId()));
//                    break;
//                }
//                case MATH: {
//                    CollectionUtils.addNonNullElement(result, mathVacationHomeworkService
//                            .mathVacationHomeworkProgress(studentId, group.getId()));
//                    break;
//                }
//                default:
//            }
//        }
        model.addAttribute("result", result);

        MapMessage selfChoosePracticeResult = getOneSelfChoosePractice(studentId);
        if (selfChoosePracticeResult.isSuccess()) {
            model.addAttribute("product", selfChoosePracticeResult.get("product"));
        }
        return "parentmobile/activity/holiday/index";
    }

    // 领取学豆奖励
    @RequestMapping(value = "completereward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage completeReward() {
        return MapMessage.errorMessage("功能已下线");
    }


    //作业详情
    @RequestMapping(value = "homeworkdetail.vpage", method = RequestMethod.GET)
    public String homeworkdetail(Model model) throws ParseException {
        Long studentId = getRequestLong("sid");
        String homeworkId = getRequestString("hid");
        HomeworkType homeworkType = HomeworkType.of(ConversionUtils.toString(getRequestString("ht")));
        String timeFormat = getRequestString("ct");
        setRouteParameter(model);

        String pageAddr = "parentmobile/homeworkDetail";

        try {
            model.addAttribute("success", true);

            if (studentId <= 0 || StringUtils.isBlank(homeworkId) || homeworkType == HomeworkType.UNKNOWN || StringUtils.isEmpty(timeFormat)) {
                model.addAttribute("result", MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            if (currentParent() == null) {
                model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
                return pageAddr;
            }
            model.addAttribute("pid", currentParent().getId());
            if (!studentIsParentChildren(currentParent().getId(), studentId)) {
                model.addAttribute("result", MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
            if (student == null) {
                model.addAttribute("result", MapMessage.errorMessage("invalid parameters").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            Map<String, Object> detail = new HashMap<>();
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (newHomework == null) {
                model.addAttribute("result", MapMessage.errorMessage(RES_RESULT_HOMEWORK_HAD_DELETE).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return pageAddr;
            }

            List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(newHomework.getClazzGroupId());
            if (CollectionUtils.isNotEmpty(teachers)) {
                detail.put("teacherName", StringUtils.substring(teachers.get(0).fetchRealname(), 0, 1) + "老师");
            }
            // 音频
            fillAudiosIfNecessary(newHomework, studentId, detail);

            //現在前端頁面不展示數學詳情
            mathHomeworkDetail(studentId, homeworkId, detail);
            model.addAttribute("result", MapMessage.successMessage().add("detail", detail));
            // 是否显示付费推广  郑州市 6年级的学生 410100
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail != null && studentDetail.getCityCode() != null && studentDetail.getClazzLevel() != null) {
                if (studentDetail.getCityCode() == 410100 && studentDetail.getClazzLevel().getLevel() == 6) {
                    model.addAttribute("showPayCourse", true);
                }
            }
            return pageAddr;
        } catch (Exception ex) {
            log.error("get homework detail failed. sid:{},hid:{},ht:{},pid:{}", studentId, homeworkId, homeworkType.name(), currentParent().getId(), ex);
            model.addAttribute("result", MapMessage.errorMessage("查询作业报告失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
        }

        return pageAddr;
    }

    private void fillAudiosIfNecessary(NewHomework newHomework, Long studentId, Map<String, Object> detail) {
        if (Subject.ENGLISH.equals(newHomework.getSubject())) {
            Map<String, List<String>> basicAppVoiceUrl = newHomeworkPartLoaderClient.getBasicAppVoiceUrl(newHomework, studentId);
            if (MapUtils.isNotEmpty(basicAppVoiceUrl)) {
                List<Map<String, Object>> audios = new ArrayList<>();
                for (String key : basicAppVoiceUrl.keySet()) {
                    Map<String, Object> obj = new HashMap<>();
                    String[] elements = StringUtils.split(key, "#");
                    obj.put("practiceType", elements[2]);
                    obj.put("audio", StringUtils.join(basicAppVoiceUrl.get(key), "|"));
                    audios.add(obj);
                }
                detail.put("audios", audios);
            }
        }
    }

    /**
     * 查询学校表现
     */
    @RequestMapping(value = "loadsmart.vpage", method = RequestMethod.GET)
    public String smartList(Model model) {
        Long userId = getRequestLong("sid");

        setRouteParameter(model);

        model.addAttribute("isBindClazz", isBindClazz(userId));

        try {
            if (userId <= 0) {
                model.addAttribute("result", MapMessage.errorMessage("invalid userid").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return "parentmobile/smart";
            }

            if (currentParent() == null) {
                model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
                return "parentmobile/smart";
            }
            if (!studentIsParentChildren(currentUserId(), userId)) {
                model.addAttribute("result", MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return "parentmobile/smart";
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            model.addAttribute("studentName", studentDetail.fetchRealname());

            List<SmartClazzIntegralHistory> all = smartClazzServiceClient.getSmartClazzService()
                    .findSmartClazzIntegralHistoryListByUserId(userId)
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
            histories = histories.stream().filter(p -> !p.getCreateDatetime().before(MonthRange.current().getStartDate())).sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())).collect(Collectors.toList());

            boolean hasAuthentication = userAggregationLoaderClient.loadStudentTeachers(userId)
                    .stream()
                    .map(ClazzTeacher::getTeacher)
                    .filter(t -> t.getSubject() != null)
                    .filter(t -> t.fetchCertificationState() == AuthenticationState.SUCCESS)
                    .count() > 0;
            model.addAttribute("result", MapMessage.successMessage().add("histories", histories)
                    .add("hasAuthentication", hasAuthentication)
                    .add("closeContributionButton", true));
        } catch (Exception ex) {
            logger.error("get smart list failed,studentId:{}", userId, ex);
            model.addAttribute("result", MapMessage.errorMessage("获取智慧课堂奖励记录失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
        }

        return "parentmobile/smart";
    }


    /**
     * 作业完成后在家长端领取奖励的接口
     */
    @RequestMapping(value = "receiveIntegralReward.vpage", method = RequestMethod.GET)
    public String receiveIntegralRewardAfterCheckHomework(Model model) {

        String redirectUrl = "parentmobile/integralReward";

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String homeworkId = getRequestString(REQ_PARENT_APP_HOMEWORK_ID);
        String homeworkType = getRequestString(REQ_PARENT_APP_HOMEWORK_TYPE);

        try {
            if (studentId <= 0) {
                model.addAttribute("result", MapMessage.errorMessage("学生号错误").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return redirectUrl;
            }

            if (HomeworkType.of(homeworkType).equals(HomeworkType.UNKNOWN)) {
                model.addAttribute("result", MapMessage.errorMessage("作业类型错误").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return redirectUrl;
            }

            User parent = currentParent();
            if (parent == null) {
                model.addAttribute("result", MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return redirectUrl;
            }

            Student student = studentLoaderClient.loadStudent(studentId);
            //判断是否是父子关系
            if (!studentIsParentChildren(currentUserId(), studentId)) {
                model.addAttribute("result", MapMessage.errorMessage("此学生和家长无关联").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return redirectUrl;
            }

            List<Map<String, Object>> rewardRank = afterCheckHomeworkIntegralRank(homeworkId);
            Integer integralPrize = 0;
            NewHomeworkFinishRewardInParentApp rewardInParentApp = newHomeworkPartLoaderClient.getRewardInParentApp(student.getId());
            if (rewardInParentApp != null) {
                if (MapUtils.isNotEmpty(rewardInParentApp.getNotReceivedRewardMap()) && rewardInParentApp.getNotReceivedRewardMap().containsKey(homeworkId)) {
                    integralPrize = rewardInParentApp.getNotReceivedRewardMap().get(homeworkId).getRewardCount();
                } else if (MapUtils.isNotEmpty(rewardInParentApp.getHadReceivedRewardMap()) && rewardInParentApp.getHadReceivedRewardMap().containsKey(homeworkId)) {
                    integralPrize = rewardInParentApp.getHadReceivedRewardMap().get(homeworkId);
                }
            }
            if (integralPrize.equals(0)) {
                model.addAttribute("result", MapMessage.successMessage("领取奖励成功").add("studentName", student.fetchRealname()).add("integralPrize", integralPrize).add("rewardRank", rewardRank));
                return redirectUrl;
            }

            IntegralHistory integralHistory = new IntegralHistory();
            integralHistory.setIntegral(integralPrize);
            integralHistory.setComment("作业检查后家长领取学豆奖励");
            integralHistory.setIntegralType(IntegralType.作业检查后家长领取学豆奖励_产品平台.getType());
            integralHistory.setUserId(studentId);
            integralHistory.setHomeworkUniqueKey(homeworkType, homeworkId);

            List<IntegralHistory> integralHistories = integralHistoryLoaderClient.getIntegralHistoryLoader().loadUserIntegralHistories(studentId);
            IntegralHistory received = integralHistories.stream()
                    .filter(p -> p.getIntegralType().equals(IntegralType.作业检查后家长领取学豆奖励_产品平台.getType()))
                    .filter(p -> ("homeworkType:" + homeworkType + "," + "homeworkId:" + homeworkId).equals(p.getUniqueKey()))
                    .findFirst().orElse(null);

            //没领取过该homeworkId的该类奖励，则去领取
            if (received == null) {
                try {
                    //先去这里NewHomeworkFinishRewardInParentApp标记已领取
                    MapMessage mapMessage = newHomeworkPartLoaderClient.updateBeforeReceivedInteger(studentId, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        model.addAttribute("result", MapMessage.errorMessage("领取奖励失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                        return redirectUrl;
                    }
                    MapMessage addIntegralResult = AtomicLockManager.instance()
                            .wrapAtomic(userIntegralService)
                            .keyPrefix("receiveIntegralRewardAfterCheckHomework")
                            .keys(studentId, homeworkId, homeworkType)
                            .proxy()
                            .changeIntegral(integralHistory);
                    if (!addIntegralResult.isSuccess()) {
                        model.addAttribute("result", MapMessage.errorMessage("领取奖励失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                        return redirectUrl;
                    }
                } catch (DuplicatedOperationException e) {
                    model.addAttribute("result", MapMessage.errorMessage("重复领取").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                }
            }

            model.addAttribute("result", MapMessage.successMessage("领取奖励成功").add("studentName", student.fetchRealname()).add("integralPrize", integralPrize).add("rewardRank", rewardRank));
            return redirectUrl;
        } catch (Exception ex) {
            log.error("receiveIntegralReward failed. homeworkId:{}, homeworkType:{}",
                    homeworkId, homeworkType);
        }
        model.addAttribute("result", MapMessage.errorMessage("领取奖励失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
        return redirectUrl;
    }


    /**
     * 预留了错题数量等信息计算，在对应的client里。
     *
     * @param studentId  学生id
     * @param homeworkId 作业id
     * @param detail     返回结果
     */
    private void mathHomeworkDetail(Long studentId, String homeworkId, Map<String, Object> detail) {
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, true);

        if (CollectionUtils.isEmpty(groupMappers)) return;

        List<GroupMapper.GroupUser> userList = new ArrayList<>();
        groupMappers.stream()
                .filter(p -> p != null)
                .filter(p -> p.getStudents() != null)
                .forEach(p -> userList.addAll(p.getStudents()));

        if (CollectionUtils.isEmpty(userList)) return;

        Set<Long> studentIds = userList.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
        newHomeworkLoaderClient.mathHomeworkDetail(studentId, homeworkId, studentIds, detail);
    }

    //错题本中获取学生做题结果
    @RequestMapping(value = "/answers.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> loadAnswers() {
        Long studentId = getRequestLong("sid");
        HomeworkType ht = HomeworkType.of(getRequestString("homeworkType"));
        String homeworkId = getRequestString("homeworkId");

        if (ht == UNKNOWN) {
            return MapMessage.errorMessage("未知作业类型");
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业ID不能为空");
        }

        try {
            Map<String, Object> paperExamMap = new HashMap<>();
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            Set<String> processIds = new HashSet<>();
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
            if (newHomeworkResult != null) {

                LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();
                if (MapUtils.isNotEmpty(practices)) {
                    practices.values().stream().filter(p -> p.getAnswers() != null).forEach(p -> processIds.addAll(p.getAnswers().values()));
                }
            }
            Map<String, NewHomeworkProcessResult> processId2ProcessMap = newHomeworkProcessResultLoaderClient.loads(homeworkId, processIds);

            if (MapUtils.isEmpty(processId2ProcessMap)) {
                return Collections.emptyMap();
            }

            Map<String, NewHomeworkProcessResult> wrongQuestionId2ProcessMap = processId2ProcessMap.values().stream()
                    .filter(p -> Boolean.FALSE.equals(p.getGrasp()))
                    .filter(p -> p.getScore() != null)
                    .collect(Collectors.toMap(NewHomeworkProcessResult::getQuestionId, Function.identity(), (a, b) -> a));

            Map<String, NewQuestion> wrongQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(wrongQuestionId2ProcessMap.keySet());

            for (Map.Entry<String, NewHomeworkProcessResult> entry : wrongQuestionId2ProcessMap.entrySet()) {
                List<List<String>> standerAnswer = wrongQuestionMap.get(entry.getKey()).getContent().getSubContents()
                        .stream()
                        .map(o -> o.getAnswerList(newHomework.getSubject()))
                        .collect(Collectors.toList());

                NewHomeworkProcessResult processResult = entry.getValue();
                if (processResult != null) {
                    Map<String, Object> mapExam = new HashMap<>();
                    mapExam.put("subMaster", processResult.getSubGrasp());
                    mapExam.put("master", processResult.getGrasp());
                    mapExam.put("userAnswers", processResult.getUserAnswers());
                    mapExam.put("answers", standerAnswer);
                    mapExam.put("fullScore", processResult.getStandardScore());
                    mapExam.put("score", processResult.getScore());
                    paperExamMap.put(entry.getKey(), mapExam);
                }
            }
            return paperExamMap;
        } catch (Exception ex) {
            logger.error("Load answers failed,hid:{},ht:{}", homeworkId, ht, ex);
            return Collections.emptyMap();
        }
    }
}
