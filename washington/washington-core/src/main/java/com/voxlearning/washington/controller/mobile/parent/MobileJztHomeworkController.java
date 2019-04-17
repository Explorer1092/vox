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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.FlowerSourceType;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.conversation.api.HomeworkThankLoader;
import com.voxlearning.utopia.service.conversation.api.HomeworkThankService;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;
import com.voxlearning.utopia.service.dubbing.consumer.DubbingHistoryLoaderClient;
import com.voxlearning.utopia.service.flower.api.FlowerConditionService;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.VacationHomeworkLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.JztClazzHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.JztHomeworkNotice;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.JztReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.JztStudentHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report.VacationReportToSubject;
import com.voxlearning.utopia.service.newhomework.consumer.DiagnoseReportServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkPartLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.WeekReportLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardLog;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.FeeCourseRecommend;
import com.voxlearning.utopia.service.question.api.entity.KnowledgePointFeature;
import com.voxlearning.utopia.service.question.consumer.FeatureLoaderClient;
import com.voxlearning.utopia.service.question.consumer.FeeCourseLoaderClient;
import com.voxlearning.utopia.service.question.consumer.TestMethodLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Flower;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.userlevel.api.mapper.UserActivationLevel;
import com.voxlearning.utopia.service.vendor.api.constant.HomeWorkReportMissionType;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsStyleType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.washington.athena.SelfStudyClient;
import com.voxlearning.washington.controller.open.v1.util.ParentHomeworkUtil;
import com.voxlearning.washington.controller.teacher.TeacherNewHomeworkController;
import com.voxlearning.washington.controller.teacher.TeacherNewHomeworkReportController;
import com.voxlearning.washington.mapper.BrilliantCourse;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.ENGLISH;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

@Controller
@RequestMapping(value = "/parentMobile/jzt")
@Slf4j
public class MobileJztHomeworkController extends AbstractMobileParentController {

    @Inject
    private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;

    @Inject
    @Getter
    private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject
    private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject
    private IntegralHistoryLoaderClient integralHistoryLoaderClient;
    @Inject
    private ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;
    @Inject
    private FeeCourseLoaderClient feeCourseLoaderClient;
    @Inject
    private SelfStudyClient selfStudyClient;
    @Inject
    private FeatureLoaderClient featureLoaderClient;
    @Inject
    private TestMethodLoaderClient testMethodLoaderClient;
    @Inject
    private NewHomeworkPartLoaderClient newHomeworkPartLoaderClient;
    @Inject
    private ActionServiceClient actionServiceClient;
    @Inject
    private WeekReportLoaderClient weekReportLoaderClient;
    @Inject
    private DubbingHistoryLoaderClient dubbingHistoryLoaderClient;
    @Inject
    private DiagnoseReportServiceClient diagnoseReportServiceClient;
    @Inject
    private FlowerServiceClient flowerServiceClient;
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @ImportService(interfaceClass = FlowerConditionService.class)
    private FlowerConditionService flowerConditionService;
    @ImportService(interfaceClass = HomeworkThankLoader.class)
    private HomeworkThankLoader homeworkThankLoader;
    @ImportService(interfaceClass = HomeworkThankService.class)
    private HomeworkThankService homeworkThankService;

    @RequestMapping(value = "/semesterChildren.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage semesterChildren() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登入").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        if (!parent.isParent()) {
            return MapMessage.errorMessage("不是家长登入").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        String baseUrl = getCdnBaseUrlAvatarWithSep();
        MapMessage mapMessage = newHomeworkReportServiceClient.semesterChildren(parent);
        mapMessage.set("baseUrl", baseUrl);
        return mapMessage;
    }


    @RequestMapping(value = "/semesterReport.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage semesterReport() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId == 0) {
            return MapMessage.errorMessage("studentId is 0").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        String subject = getRequestString(REQ_SUBJECT);
        if (!StringUtils.isNotBlank(subject) || !(subject.equals("MATH") || subject.equals("ENGLISH"))) {
            return MapMessage.errorMessage("subject is wrong").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        SemesterReport semesterReport = newHomeworkReportServiceClient.semesterReport(studentId, subject);
        MapMessage mapMessage = MapMessage.successMessage();
        if (semesterReport != null) {
            mapMessage.putAll(JsonUtils.safeConvertObjectToMap(semesterReport));
        }
        return mapMessage;
    }

    //to do 是否购买
    @RequestMapping(value = "/brilliantCourse.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getBrilliantCourse() {
        User parent = currentParent();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String hid = getRequestString(REQ_PARENT_APP_HOMEWORK_ID);

        try {
            if (parent == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }
            if (studentId == 0) {
                return MapMessage.errorMessage("获取精讲课程信息失败:学生ID缺失");
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                return MapMessage.errorMessage("获取精讲课程信息失败:学生错误");
            }
            if (StringUtils.isBlank(hid)) {
                return MapMessage.errorMessage("获取精讲课程信息失败:作业ID缺失");
            }
            NewHomeworkBook newHomeworkBook = newHomeworkLoaderClient.loadNewHomeworkBook(hid);
            if (newHomeworkBook == null) {
                return MapMessage.errorMessage("获取精讲课程信息失败:作业信息缺失");
            }

            String bookId = newHomeworkBook.processBookId();
            Set<String> unitIds = newHomeworkBook.processUnitIds();
            if (StringUtils.isBlank(bookId)) {
                return MapMessage.errorMessage("获取精讲课程信息失败:课本ID信息缺失");
            }

            if (CollectionUtils.isEmpty(unitIds)) {
                return MapMessage.errorMessage("获取精讲课程信息失败:单元信息缺失");
            }
            String unitId = null;
            if (unitIds.size() == 1) {
                unitId = unitIds.iterator().next();
            } else {
                Map<String, NewBookCatalog> lessonNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIds);
                NewBookCatalog newBookCatalog = lessonNewBookCatalogMap.values()
                        .stream()
                        .filter(Objects::nonNull)
                        .sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.getRank()), SafeConverter.toInt(o1.getRank())))
                        .findFirst()
                        .orElse(null);
                if (newBookCatalog != null) {
                    unitId = newBookCatalog.getId();
                }
            }
            if (unitId == null) {
                return MapMessage.errorMessage("获取精讲课程信息失败:单元信息缺失");
            }
            BrilliantCourse brilliantCourse = new BrilliantCourse();
            List<String> kpIds = new LinkedList<>();


            if (newHomeworkBook.getSubject() == ENGLISH) {
                kpIds = selfStudyClient.getSelfStudy().loadStudentUnitInfos(studentId, unitId);
                //根据于振的接口获取name
                Map<String, String> knowledgePointName = testMethodLoaderClient.getNameById(kpIds);

                Set<String> knowledgePoints = knowledgePointName.values()
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                brilliantCourse.setKnowledgePoints(knowledgePoints);

            } else {
                List<Map<String, List<String>>> mapList = selfStudyClient.getSelfStudy().queryAfentiMathGoal(unitId, studentId);
                Set<String> kpfIds = new HashSet<>();
                for (Map<String, List<String>> map : mapList) {
                    for (String k : map.keySet()) {
                        kpIds.add(k);
                        if (CollectionUtils.isNotEmpty(map.get(k))) {
                            kpfIds.addAll(map.get(k));
                        }
                    }
                }
                Set<String> knowledgePoints = new HashSet<>();
                //根据于振的接口获取name
                Map<String, String> knowledgePointName = testMethodLoaderClient.getNameById(kpIds);

                Map<String, KnowledgePointFeature> knowledgePointFeatureMap = featureLoaderClient.loadKnowledgePointFeatureIncludeDisabled(kpfIds);


                for (Map<String, List<String>> map : mapList) {
                    for (String k : map.keySet()) {
                        if (knowledgePointName.containsKey(k)) {
                            if (CollectionUtils.isNotEmpty(map.get(k))) {
                                for (String kf : map.get(k)) {
                                    KnowledgePointFeature knowledgePointFeature = knowledgePointFeatureMap.get(kf);
                                    if (knowledgePointFeature != null) {
                                        knowledgePoints.add(SafeConverter.toString(knowledgePointName.get(k)) + "(" + knowledgePointFeature.getName() + ")");
                                    } else {
                                        knowledgePoints.add(SafeConverter.toString(knowledgePointName.get(k)));
                                    }
                                }
                            } else {
                                knowledgePoints.add(SafeConverter.toString(knowledgePointName.get(k)));
                            }
                        }
                    }
                }
                brilliantCourse.setKnowledgePoints(knowledgePoints);
            }
            FeeCourseRecommend feeCourseRecommend = feeCourseLoaderClient.recommendByBookAndKnowledgePoints(studentId, bookId, kpIds);
            if (feeCourseRecommend == null) {
                return MapMessage.errorMessage("不存在推荐精讲课程信息");

            }
            if (feeCourseRecommend.getRecommendFeeCourse() == null) {
                return MapMessage.errorMessage("不存在推荐精讲课本信息");
            }
            String courseListUrl = UrlUtils.buildUrlQuery("/app/redirect/jump.vpage",
                    MapUtils.m(
                            "appKey", "FeeCourse",
                            "platform", "PARENT_APP",
                            "productType", "APPS",
                            "sid", studentId,
                            "refer", 250002));
            courseListUrl += "&position=/courseCatalog/" + feeCourseRecommend.getRecommendFeeCourse().getId();
            brilliantCourse.setCourseListUrl(courseListUrl);

            brilliantCourse.setBookName(SafeConverter.toString(feeCourseRecommend.getRecommendFeeCourse().getName(), ""));
            brilliantCourse.setCoverUrl(SafeConverter.toString(feeCourseRecommend.getRecommendFeeCourse().getImgUrl(), ""));//url 是否需要添加前缀
            List<String> courseBrief = new LinkedList<>();
            if (SafeConverter.toString(feeCourseRecommend.getRecommendFeeCourse().getBookSeries(), "").equals("RENJIAO")) {
                courseBrief.add("人教版");
            } else {
                courseBrief.add("适合所有教材");
            }
            courseBrief.add(SafeConverter.toInt(feeCourseRecommend.getRecommendFeeCourse().getLessonNum()) + "课时+" + SafeConverter.toInt(feeCourseRecommend.getRecommendFeeCourse().getQuestionNum()) + "道巩固练习");
            brilliantCourse.setBought(SafeConverter.toBoolean(feeCourseRecommend.getPayed()));
            if (!brilliantCourse.isBought()) {//未购买
                if (feeCourseRecommend.getFreeFeeCourseContent() == null) {
                    return MapMessage.errorMessage("免费试听信息缺失");
                }
                brilliantCourse.setFreeCourseCoverUrl(SafeConverter.toString(feeCourseRecommend.getFreeFeeCourseContent().getImgUrl(), ""));//url 是否需要添加前缀
                brilliantCourse.setFreeCourseName(SafeConverter.toString(feeCourseRecommend.getFreeFeeCourseContent().getName(), ""));
                String goInFreeCourseUrl = UrlUtils.buildUrlQuery("/app/redirect/jump.vpage",
                        MapUtils.m(
                                "appKey", "FeeCourse",
                                "platform", "PARENT_APP",
                                "productType", "APPS",
                                "sid", studentId,
                                "refer", 250002));
                goInFreeCourseUrl += "&position=/lesson/" + feeCourseRecommend.getFreeFeeCourseContent().getId();
                brilliantCourse.setGoInFreeCourseUrl(goInFreeCourseUrl);


                String unlockCourseUrl = UrlUtils.buildUrlQuery("/app/redirect/jump.vpage",
                        MapUtils.m(
                                "appKey", "FeeCourse",
                                "platform", "PARENT_APP",
                                "productType", "APPS",
                                "sid", studentId,
                                "refer", 250002));
                unlockCourseUrl += "&position=/order/" + feeCourseRecommend.getRecommendFeeCourse().getId() + "/0";
                brilliantCourse.setUnlockCourseUrl(unlockCourseUrl);
            } else {
                if (feeCourseRecommend.getRecommendFeeCourseContent() != null) {

                    String goInRecommendCourseUrl = UrlUtils.buildUrlQuery("/app/redirect/jump.vpage",
                            MapUtils.m(
                                    "appKey", "FeeCourse",
                                    "platform", "PARENT_APP",
                                    "productType", "APPS",
                                    "sid", studentId,
                                    "refer", 250002));
                    goInRecommendCourseUrl += "&position=/lesson/" + feeCourseRecommend.getRecommendFeeCourseContent().getId();
                    brilliantCourse.setGoInRecommendCourseUrl(goInRecommendCourseUrl);
                }
            }
            if (feeCourseRecommend.getRecommendFeeCourseContent() == null) {
                return MapMessage.errorMessage("推荐信息缺失");
            }
            brilliantCourse.setCourseBrief(courseBrief);
            brilliantCourse.setCourseName(SafeConverter.toString(feeCourseRecommend.getRecommendFeeCourseContent().getName(), ""));
            brilliantCourse.setCourseCoverUrl(SafeConverter.toString(feeCourseRecommend.getRecommendFeeCourseContent().getImgUrl(), ""));
            brilliantCourse.setStudentName(studentDetail.fetchRealname());
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.put("brilliantCourse", brilliantCourse);
            return mapMessage;
        } catch (Exception e) {
            log.error("get homework report error :{}, studentId: {}", studentId, e);
            return MapMessage.errorMessage("获取精讲课程信息失败");
        }
    }


    @RequestMapping(value = "/week/report/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage weekReportList() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);

        if (studentId == 0L) {
            return MapMessage.errorMessage(" 请退出重试");
        }
        User parent = currentParent();
        if (parent == null || !parent.isParent()) {
            return MapMessage.errorMessage("请登入家长账号");
        }
        return weekReportLoaderClient.fetchWeekReportBrief(studentId);
    }


    @RequestMapping(value = "/newHomeworkReport.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getHomeworkReport() {
        String homeworkId = getRequestString(REQ_PARENT_APP_HOMEWORK_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String sys = getRequestString(REQ_SYS);
        User parent = currentParent();
        try {
            if (parent == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }
            if (studentId <= 0) {
                return MapMessage.errorMessage("sid error");
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                return MapMessage.errorMessage("student error");
            }
            boolean webGrayFunctionAvailable = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jzt", "newHomework");
            if (!webGrayFunctionAvailable) {
                return MapMessage.errorMessage("作业报告暂时无法查看，请明天查看");
            }
            if (StringUtils.isBlank(homeworkId)) {
                return MapMessage.errorMessage("hid error");
            }

            if (!studentIsParentChildren(parent.getId(), studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联");
            }
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            //作业已经被删除
            if (newHomework == null) {
                return MapMessage.successMessage(RES_RESULT_HOMEWORK_HAD_DELETE).add("is_delete", true);
            }
            //作业报告过期
            Date currentDate = new Date();
            if (DateUtils.dayDiff(currentDate, newHomework.getEndTime()) > 60) {
                return MapMessage.successMessage("该作业已截止超过60天，我们暂时仅支持60天以内的作业内容哦").add("is_expired", true);
            }
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
            JztReport jztReport = newHomeworkReportServiceClient.buildNewHomeworkReportV1(newHomeworkResult, parent, newHomework, studentDetail);
            if (jztReport != null && jztReport.isSuccess()) {
                if (StringUtils.isNoneBlank(sys, jztReport.getBookId())) {
                    Boolean parentAuth = picListenCommonService.userIsAuthForPicListen(parent);
                    if (parentAuth != null) {
                        jztReport.setShowJumpBtn(textBookManagementLoaderClient.picListenShow(jztReport.getBookId(), sys, parentAuth));
                    }
                }
                jztReport.setGroupId(newHomework.getClazzGroupId());
                //查看作业报告 成长体系
                actionServiceClient.lookHomeworkReport(jztReport.getSid(), newHomework.getSubject());

                if (!Objects.equals(jztReport.getSid(), studentDetail.getId())) {
                    studentDetail = studentLoaderClient.loadStudentDetail(jztReport.getSid());
                    newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), jztReport.getSid(), true);
                }

                jztReport.setStudentName(studentDetail.fetchRealname());
                //拼老师头像
                String teacher_default_icon = "/public/skin/parentMobile/images/new_icon/avatar_teacher_deafult.png";
                String img = SafeConverter.toString(jztReport.getTeacherSummaryPart().getTeacherPicUrl(), "");
                String imgUrl;
                if (StringUtils.isBlank(img)) {
                    imgUrl = getCdnBaseUrlStaticSharedWithSep() + teacher_default_icon;
                } else {
                    imgUrl = getUserAvatarImgUrl(img);
                }
                jztReport.getTeacherSummaryPart().setTeacherPicUrl(imgUrl);
                if (newHomeworkResult != null) {
                    jztReport.getTeacherSummaryPart().setTeacherComment(SafeConverter.toString(newHomeworkResult.getComment(), ""));
                    jztReport.setHasSign(SafeConverter.toBoolean(newHomeworkResult.getUrge()));
                }
                //如果在灰度内且错题数大于0，家长签字会记录可领取学豆,作业检查后才有该任务
                if (jztReport.isChecked()) {
                    JztReport.TeacherSummaryPart.ErrorModule errorModule = jztReport.getTeacherSummaryPart().getErrorModule();
                    if (errorModule.isFlag() && errorModule.getWrongQuestionNum() > 0) {
                        asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                                .JztHomeworkReportCacheManager_recordReportMissionIntegral(homeworkId, jztReport.getSid(), HomeWorkReportMissionType.ERROR_CORRECTION, 1)
                                .awaitUninterruptibly();
                    }
                }

                //趣味配音的historyId
                if (MapUtils.isNotEmpty(jztReport.getDubbingInfo())) {
                    String dubbingId = SafeConverter.toString(jztReport.getDubbingInfo().get("dubbingId"));
                    if (StringUtils.isNotBlank(dubbingId)) {
                        DubbingHistory dubbingHistory = dubbingHistoryLoaderClient.getDubbingHistoryByHomeworkId(studentId, dubbingId, homeworkId);
                        if (dubbingHistory != null) {
                            jztReport.getDubbingInfo().put("historyId", dubbingHistory.getId());
                        }
                    }
                }

                //作业是否过期
                jztReport.setExpired(newHomework.getEndTime().before(currentDate));
                jztReport.setTeacherId(newHomework.getTeacherId());
                String assignDate = DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日");
                jztReport.setAssignDate(assignDate);
                jztReport.setAssignedToNow(DateUtils.dayDiff(currentDate, newHomework.getCreateAt()));

                //处理作业简介中的同步内容
                generateJxtNewsBookInfo(jztReport.getHomeworkDescription(), newHomework, studentDetail);
                //处理语音相关的内容
                generateSyllableInfo(jztReport.getTeacherSummaryPart().getTalkingModule(), newHomework, studentDetail, getRequestString(REQ_SYS));
                //作业小结与奖励
                generateSummaryRewardInfo(jztReport, newHomework, newHomeworkResult, studentDetail);
                //鲜花信息
                generateFlowerInfo(jztReport);

                //即时干预导流
                if (Subject.MATH.equals(newHomework.getSubject()) && currentDate.before(DateUtils.stringToDate("2018-09-26 19:30:00"))) {
                    long interventionGraspQuestionCount = diagnoseReportServiceClient.countInterventionGraspQuestion(newHomework, studentId);
                    jztReport.setInterventionGraspQuestionCount(interventionGraspQuestionCount);
                }

                //加家长活跃值
                userLevelService.parentViewHomeworkReport(parent.getId(), homeworkId);

                MapMessage message = MapMessage.successMessage().add("jztReport", jztReport);

                Map<String, Object> popInfo = getActivationInfoForPopup(homeworkId, parent.getId());
                message.add("popInfo", popInfo);

                return message;
            } else {
                if (jztReport != null && Objects.equals(ErrorCodeConstants.ERROR_CODE_COMMON, jztReport.getErrorCode())) {
                    return MapMessage.errorMessage("报告内容与当前选择的孩子帐号不符，请检查后重试~");
                } else {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", studentId,
                            "mod1", homeworkId,
                            "mod2", "jztHomeworkReport",
                            "mod3", jztReport != null ? jztReport.getError() : "",
                            "op", "homework report"
                    ));
                    return MapMessage.errorMessage("获取作业报告失败，请稍后再试");
                }
            }
        } catch (Exception e) {
            log.error("get homework report error. homeworkId:{}, studentId: {}", homeworkId, studentId, e);
            return MapMessage.errorMessage("获取作业报告失败，请稍后再试");
        }
    }

    @RequestMapping(value = "/student/homework/report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studentHomeworkReport() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        String homeworkId = getRequestString(REQ_PARENT_APP_HOMEWORK_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String sys = getRequestString(REQ_SYS);
        if (studentId <= 0) {
            return MapMessage.errorMessage("sid error");
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("hid error");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("student error");
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联");
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.successMessage(RES_RESULT_HOMEWORK_HAD_DELETE).add("is_delete", true);
        }
        Long groupId = newHomework.getClazzGroupId();
        List<Long> groupStudentIds = studentLoaderClient.loadGroupStudentIds(Collections.singleton(groupId)).get(groupId);
        if (CollectionUtils.isEmpty(groupStudentIds) || !groupStudentIds.contains(studentId)) {
            return MapMessage.errorMessage("报告内容与当前选择的孩子帐号不符，请检查后重试~");
        }
        Date currentDate = new Date();
        if (DateUtils.dayDiff(currentDate, newHomework.getEndTime()) > 60) {
            return MapMessage.successMessage("该作业已截止超过60天，我们暂时仅支持60天以内的作业内容哦").add("is_expired", true);
        }
        boolean webGrayFunctionAvailable = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jzt", "newHomework");
        if (!webGrayFunctionAvailable) {
            return MapMessage.errorMessage("作业报告暂时无法查看，请明天查看");
        }

        try {
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
            JztStudentHomeworkReport jztReport = newHomeworkReportServiceClient.loadJztStudentHomeworkReport(newHomeworkResult, newHomework, studentDetail);

            if (StringUtils.isNoneBlank(sys, jztReport.getBookId())) {
                Boolean parentAuth = picListenCommonService.userIsAuthForPicListen(parent);
                if (parentAuth != null) {
                    jztReport.setShowJumpBtn(textBookManagementLoaderClient.picListenShow(jztReport.getBookId(), sys, parentAuth));
                }
            }
            //查看作业报告 成长体系
            actionServiceClient.lookHomeworkReport(studentId, newHomework.getSubject());

            //处理语音相关的内容
            handleSyllableInfo(jztReport.getSyllableModule(), newHomework, studentDetail, sys);

            //加家长活跃值
            userLevelService.parentViewHomeworkReport(parent.getId(), homeworkId);

            MapMessage message = MapMessage.successMessage().add("jztReport", jztReport);

            Map<String, Object> popInfo = getActivationInfoForPopup(homeworkId, parent.getId());
            message.add("popInfo", popInfo);

            //家长奖励按钮信息
            generateRewardInfo(message, parent.getId(), studentId, homeworkId);
            //老师评语及家长回复情况
            getCommentsAndReplay(message, parent, studentId, newHomework, newHomeworkResult);
            //送花按钮信息
            getFlowerInfo(message, studentId, newHomework);
            return message;
        } catch (Exception e) {
            log.error("get homework report error. homeworkId:{}, studentId: {}", homeworkId, studentId, e);
            return MapMessage.errorMessage("获取作业报告失败，请稍后再试");
        }
    }

    private void getCommentsAndReplay(MapMessage message,  User parent ,Long studentId, NewHomework newHomework, NewHomeworkResult newHomeworkResult) {
        Map<String, Object> comments = new HashMap<>();
        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
        comments.put("clazzGroupId", student != null && student.getClazz() != null ? student.getClazz().getId() : "");
        comments.put("teacherId", newHomework.getTeacherId());
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(newHomework.getTeacherId());
        comments.put("teacherName", teacherDetail.fetchRealnameIfBlankId());
        comments.put("teacherImage", getUserAvatarImgUrl(teacherDetail));
        comments.put("teacherAudioComment", newHomeworkResult == null ? "" : SafeConverter.toString(newHomeworkResult.getAudioComment(), ""));
        comments.put("teacherComment", newHomeworkResult == null ? "" : SafeConverter.toString(newHomeworkResult.getComment(), ""));
        comments.put("rewardIntegral", newHomeworkResult == null ? 0 : SafeConverter.toInt(newHomeworkResult.getRewardIntegral(), 0));
        MapMessage thanks = homeworkThankLoader.loadHomeworkThanks(parent.getId(), studentId, newHomework.getId());
        String replay = SafeConverter.toString(thanks.get("reply_content"));
        if (SafeConverter.toBoolean(thanks.get("isReply"), false)
                && StringUtils.isNotEmpty(replay)) {
            comments.put("parentReplay", replay);
            comments.put("isReplayed", true);
        } else {
            comments.put("parentReplay", "");
            comments.put("isReplayed", false);
        }
        comments.put("isLike", SafeConverter.toBoolean(thanks.get("isLike"), false));
        comments.put("parentId", parent.getId());
        comments.put("parentName", parent.fetchRealnameIfBlankId());
        comments.put("parentImage", getUserAvatarImgUrl(parent));
        message.add("comments", comments);
    }

    @RequestMapping(value = "/student/homework/commentreplay.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studentHomeworkCommentReplay() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long tid = getRequestLong(REQ_TEACHER_ID);
        Long pid = getRequestLong(REQ_PARENT_ID);
        String homeworkId = getRequestString(REQ_PARENT_APP_HOMEWORK_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String content = getRequestString(REQ_REPLAY_CONTENT);
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        if (studentId <= 0) {
            return MapMessage.errorMessage("sid error");
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("hid error");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("student error");
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联");
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.successMessage(RES_RESULT_HOMEWORK_HAD_DELETE).add("is_delete", true);
        }
        return homeworkThankService.createHomeworkThank(tid, pid, studentId, homeworkId, content, 1, clazzId);
    }

    private Map<String, Object> getActivationInfoForPopup(String homeworkId, Long parentId) {
        Map<String, Object> popInfo = new HashMap<>();

        Long parentViewCountToday = washingtonCacheSystem.CBS.persistence.incr("PARENT_VIEW_HOMEWORK_REPORT_" + parentId, 1, 1, DateUtils.getCurrentToDayEndSecond());
        if (null != parentViewCountToday && parentViewCountToday == 1) {
            Long parentViewHomeworkCount = washingtonCacheSystem.CBS.persistence.incr("PARENT_VIEW_HOMEWORK_REPORT_" + parentId + "_" + homeworkId, 1, 1, (int) (SchoolYear.newInstance().currentTermDateRange().getEndDate().getTime() / 1000));
            if (null != parentViewHomeworkCount && 1 == parentViewHomeworkCount) {
                popInfo.put("pop", true);
                popInfo.put("title", "关注孩子的每一次学习");
                popInfo.put("count", 1);
                popInfo.put("action", "查看作业报告");
                popInfo.put("value", 2);
                UserActivationLevel parentLevel = userLevelLoader.getParentLevel(currentUserId());
                if (null != parentLevel) {
                    popInfo.put("level", parentLevel.getLevel());
                    popInfo.put("levelName", parentLevel.getName());
                    popInfo.put("activation", parentLevel.getValue());
                    popInfo.put("maxActivation", parentLevel.getLevelEndValue() + 1);
                    popInfo.put("minActivation", parentLevel.getLevelStartValue());
                }
            } else {
                washingtonCacheSystem.CBS.persistence.decr("PARENT_VIEW_HOMEWORK_REPORT_" + parentId, 1, 0, DateUtils.getCurrentToDayEndSecond());
                popInfo.put("pop", false);
            }
        } else {
            popInfo.put("pop", false);
        }
        return popInfo;
    }

    /**
     * 作业报告家长签字任务学豆奖励
     */
    @RequestMapping(value = "/report/mission/integral/draw.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reportMissionIntegralDraw() {
        String homeworkId = getRequestString(REQ_PARENT_APP_HOMEWORK_ID);
        String homeworkType = getRequestString(REQ_PARENT_APP_HOMEWORK_TYPE);
        Long studentId = getRequestLong(REQ_STUDENT_ID);

        try {
            User parent = currentParent();
            if (parent == null) {
                return noLoginResult;
            }

            if (StringUtils.isEmpty(homeworkId)) {
                return MapMessage.errorMessage("作业id错误");
            }

            if (studentId < 0) {
                return MapMessage.errorMessage("学生id错误");
            }
            if (HomeworkType.of(homeworkType) == HomeworkType.UNKNOWN) {
                return MapMessage.errorMessage("作业类型错误");
            }
            if (!studentIsParentChildren(currentUserId(), studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联");
            }

            List<IntegralHistory> integralHistories = integralHistoryLoaderClient.getIntegralHistoryLoader().loadUserIntegralHistories(studentId);
            IntegralHistory received = integralHistories.stream()
                    .filter(e -> e.getIntegralType() == IntegralType.STUDENT_HOMEWORK_REPORT_CHECK_BY_PARENT_REWARD.getType())
                    .filter(e -> ("homeworkType:" + homeworkType + "," + "homeworkId:" + homeworkId).equals(e.getUniqueKey()))
                    .findFirst()
                    .orElse(null);
            if (received == null) {
                Integer integralPrize = asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                        .JztHomeworkReportCacheManager_loadIntegralCount(homeworkId, studentId)
                        .take();
                if (integralPrize > 0) {
                    IntegralHistory integralHistory = new IntegralHistory();
                    integralHistory.setIntegral(integralPrize);
                    integralHistory.setComment("作业报告家长签字任务学豆奖励");
                    integralHistory.setIntegralType(IntegralType.STUDENT_HOMEWORK_REPORT_CHECK_BY_PARENT_REWARD.getType());
                    integralHistory.setUserId(studentId);
                    integralHistory.setHomeworkUniqueKey(homeworkType, homeworkId);
                    try {
                        MapMessage mapMessage = AtomicLockManager.instance()
                                .wrapAtomic(userIntegralService)
                                .keyPrefix("receiveIntegralRewardAfterFinishReportMission")
                                .keys(studentId, homeworkId, homeworkType)
                                .proxy()
                                .changeIntegral(integralHistory);
                        if (!mapMessage.isSuccess()) {
                            return MapMessage.errorMessage("领取奖励失败");
                        }
                    } catch (DuplicatedOperationException e) {
                        return MapMessage.errorMessage("重复领取");
                    }
                }

                NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
                if (newHomework != null) {
                    NewHomework.Location location = newHomework.toLocation();
                    //学豆领取成功，调用作业那边的接口，在作业结果中存储领取信息和学豆数量
                    newHomeworkServiceClient.updateNewHomeworkResultUrge(location, studentId, parent.getId(), integralPrize);
                }
            }
            return MapMessage.successMessage("领取奖励成功");
        } catch (Exception e) {
            log.error("receive homework report mission integral error. homeworkId:{}, homeworkType:{}, studentId:{}", homeworkId, homeworkType, studentId);
            return MapMessage.errorMessage("领取作业报告任务奖励失败");
        }
    }

    /**
     * 作业报告
     * 领取学豆奖励
     */
    @RequestMapping(value = "/homework/integral/draw.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentDrawIntegral() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String homeworkId = getRequestString(REQ_PARENT_APP_HOMEWORK_ID);
        String homeworkType = getRequestString(REQ_PARENT_APP_HOMEWORK_TYPE);

        try {
            if (studentId <= 0) {
                return MapMessage.errorMessage("学生号错误");
            }

            if (HomeworkType.of(homeworkType).equals(HomeworkType.UNKNOWN)) {
                return MapMessage.errorMessage("作业类型错误");
            }

            User parent = currentParent();
            if (parent == null) {
                return noLoginResult;
            }

            Student student = studentLoaderClient.loadStudent(studentId);
            //判断是否是父子关系
            if (!studentIsParentChildren(currentUserId(), studentId)) {
                return MapMessage.errorMessage("此学生和家长无关联");
            }

            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            //作业是否完成超过15天，超过15天后不能领取额外学豆
            if (newHomework != null) {
                NewHomework.Location location = newHomework.toLocation();
                String day = DayRange.newInstance(location.getCreateTime()).toString();
                Subject subject = newHomework.getSubject();
                NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, subject, location.getId(), studentId.toString());
                NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loads(Collections.singleton(id.toString()), false).get(id.toString());
                //完成作业后15天，不能再领取学豆
                boolean expire_over_fifty_days = DateUtils.dayDiff(new Date(), newHomeworkResult.getFinishAt()) > 15;
                if (expire_over_fifty_days) {
                    return MapMessage.successMessage().add("expire", true);
                }
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
                return MapMessage.successMessage("领取奖励成功")
                        .add("studentName", student.fetchRealname())
                        .add("integralPrize", integralPrize)
                        .add("rewardRank", rewardRank);
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
                        return MapMessage.errorMessage("领取奖励失败");
                    }
                    MapMessage addIntegralResult = AtomicLockManager.instance()
                            .wrapAtomic(userIntegralService)
                            .keyPrefix("receiveIntegralRewardAfterCheckHomework")
                            .keys(studentId, homeworkId, homeworkType)
                            .proxy()
                            .changeIntegral(integralHistory);
                    if (!addIntegralResult.isSuccess()) {
                        return MapMessage.errorMessage("领取奖励失败");
                    }
                } catch (DuplicatedOperationException e) {
                    return MapMessage.errorMessage("重复领取");
                }
            }

            return MapMessage.successMessage("领取奖励成功")
                    .add("studentName", student.fetchRealname())
                    .add("integralPrize", integralPrize)
                    .add("rewardRank", rewardRank);
        } catch (Exception ex) {
            log.error("receiveIntegralReward failed. homeworkId:{}, homeworkType:{}",
                    homeworkId, homeworkType);
            return MapMessage.errorMessage("领取奖励失败");
        }

    }

    @RequestMapping(value = "/homework/reportList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getHomeworkReportList() {
        String subjectName = getRequestString(REQ_SUBJECT);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Integer currentPage = getRequestInt("current_page");
        Subject subject = Subject.safeParse(subjectName);
        if (subject == null || subject == Subject.UNKNOWN) {
            return MapMessage.errorMessage("学科错误");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (studentId == 0) {
            return MapMessage.errorMessage("学生ID错误");
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        Date endDate = new Date();
        Date startDate = DateUtils.addDays(new Date(), -60);

        Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false).stream()
                .filter(groupMapper -> groupMapper.getSubject() == subject)
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        List<NewHomework.Location> allLocations = newHomeworkPartLoaderClient.loadNewHomeworkByClazzGroupId(groupIds, startDate, endDate);
        if (CollectionUtils.isEmpty(allLocations)) {
            return MapMessage.successMessage().add("current_page", currentPage).add("total_pages", 0);
        }
        //过滤纸质作业形式
        List<NewHomework.Location> locations = allLocations.stream()
                .filter(n -> !NewHomeworkType.OCR.equals(n.getType()))
                .collect(Collectors.toList());
        currentPage = currentPage < 1 ? 1 : currentPage;
        PageRequest pageable = new PageRequest(currentPage - 1, 10);
        Page<NewHomework.Location> locationPage = PageableUtils.listToPage(locations, pageable);
        int totalPages = locationPage.getTotalPages();
        if (CollectionUtils.isEmpty(locationPage.getContent())) {
            return MapMessage.successMessage().add("current_page", currentPage).add("total_pages", totalPages);
        }
        List<NewHomework.Location> pageLocations = locationPage.getContent();

        Map<WeekRange, List<NewHomework.Location>> weekLocationMap = pageLocations.stream()
                .collect(Collectors.groupingBy(p -> WeekRange.newInstance(p.getCreateTime())));
        Set<String> accomplishmentIds = new HashSet<>();
        pageLocations.forEach(location -> accomplishmentIds.add(NewAccomplishment.ID.build(location.getCreateTime(),
                location.getSubject(), location.getId()).toString()));
        Map<String, NewAccomplishment> accomplishmentMap = newAccomplishmentLoaderClient.loadNewAccomplishments(accomplishmentIds);

        boolean isHitScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
        List<Map<String, Object>> dataList = new ArrayList<>();
        weekLocationMap.keySet().stream()
                .sorted((w1, w2) -> w2.getStartDate().compareTo(w1.getStartDate()))
                .forEach(weekRange -> {
                    Map<String, Object> weekMap = new HashMap<>();
                    String weekTitle;
                    if (weekRange.equals(WeekRange.current())) {
                        weekTitle = "本周";
                    } else if (weekRange.equals(WeekRange.current().previous())) {
                        weekTitle = "上周";
                    } else {
                        weekTitle = DateUtils.dateToString(weekRange.getStartDate(), "MM月dd日") + "-" + DateUtils.dateToString(weekRange.getEndDate(), "MM月dd日");
                    }
                    weekMap.put("week_title", weekTitle);
                    List<Map<String, Object>> homeworkList = new ArrayList<>();
                    List<NewHomework.Location> weekLocations = weekLocationMap.get(weekRange);
                    for (NewHomework.Location location : weekLocations) {
                        Map<String, Object> homeworkMap = new HashMap<>();
                        String name = location.getSubject().getValue() + "作业";
                        String dateTitle = DateUtils.dateToString(new Date(location.getCreateTime()), "MM月dd日");
                        String accomplishmentId = NewAccomplishment.ID.build(location.getCreateTime(), location.getSubject(), location.getId()).toString();
                        NewAccomplishment newAccomplishment = accomplishmentMap.get(accomplishmentId);
                        boolean finish = newAccomplishment == null ? Boolean.FALSE : newAccomplishment.contains(studentId);
                        String desc;
                        if (!location.isChecked() && !finish && new Date(location.getEndTime()).after(new Date())) {
                            desc = "待完成";
                        } else if (!location.isChecked() && finish) {
                            desc = "待老师检查";
                        } else if (location.isChecked() && finish) {
                            NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(location, studentId, false);
                            Integer score = newHomeworkResult == null ? Integer.valueOf(0) : newHomeworkResult.processScore();
                            if (score == null) {
                                desc = "已完成";
                            } else {
                                desc = "成绩";
                                if (isHitScoreLevel) {
                                    desc += ScoreLevel.processLevel(score).getLevel();
                                } else {
                                    desc += String.valueOf(score);
                                }
                            }
                        } else {
                            desc = "未按时完成";
                        }
                        homeworkMap.put("name", name);
                        homeworkMap.put("date_title", dateTitle);
                        homeworkMap.put("desc", desc);
                        String url;
                        if (location.isChecked()) {
                            url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/homework/report_detail?tab=personal&hid=" + location.getId();
                        } else {
                            url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/homework/report_notice?hid=" + location.getId();
                        }
                        homeworkMap.put("url", url);
                        homeworkList.add(homeworkMap);
                    }
                    weekMap.put("homework_list", homeworkList);
                    dataList.add(weekMap);
                });
        return MapMessage.successMessage().add("data_list", dataList)
                .add("current_page", currentPage)
                .add("total_pages", totalPages);
    }

    @RequestMapping(value = "/homework/dateLine.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getHomeworkBySubject() {
        String subjectName = getRequestString(REQ_SUBJECT);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long time = getRequestLong(REQ_CREATE_TIME);
        Integer page = getRequestInt("currentPage");
        Subject subject = null;
        if (StringUtils.isNotBlank(subjectName)) {
            subject = Subject.ofWithUnknown(subjectName);
            if (subject == Subject.UNKNOWN) {
                return MapMessage.errorMessage("学科错误");
            }
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (studentId == 0) {
            return MapMessage.errorMessage("学生ID错误");
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联");
        }

        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        //学生拥有的学科列表
        List<Map<String, Object>> subjectMapList = new ArrayList<>();
        //避免有重复记录。先收集成set再处理
        groupMappers.stream().filter(p -> p.getSubject() != null).sorted(Comparator.comparingInt(o -> o.getSubject().getKey())).map(GroupMapper::getSubject).forEach(p -> {
            if (p != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", p.getValue());
                map.put("subject", p.name());
                subjectMapList.add(map);
            }
        });

        //需要查询的学科转化为groupId
        Subject filterSubject = subject;
        Set<Long> groupIds = groupMappers.stream().filter(p -> filterSubject == null || p.getSubject() == filterSubject).map(GroupMapper::getId).collect(Collectors.toSet());
        //起止时间
        Date endDate = new Date();
        Date startDate = DateUtils.calculateDateDay(endDate, -60);
        //分页
        page = page < 1 ? 1 : page;
        PageRequest pageable = new PageRequest(page - 1, 10);
        List<NewHomework.Location> totalLocations = newHomeworkPartLoaderClient.loadNewHomeworkByClazzGroupId(groupIds, startDate, endDate);
        //mock一个作业来代表寒假作业进去排序和分页
        DayRange conditionDay = DayRange.parse("20170212");
        NewHomework.Location vacationLocation = new NewHomework.Location();
        vacationLocation.setId("vacationHomework");
        vacationLocation.setCreateTime(conditionDay.getEndTime());
        if (CollectionUtils.isEmpty(totalLocations)) {
            totalLocations = new ArrayList<>();
        }
        totalLocations.add(vacationLocation);
        totalLocations.sort((o1, o2) -> ((Long) o2.getCreateTime()).compareTo(o1.getCreateTime()));

        Page<NewHomework.Location> locationPage = PageableUtils.listToPage(totalLocations, pageable);
        List<NewHomework.Location> newHomeworkLocations = new ArrayList<>(locationPage.getContent());

        //处理寒假作业
        Boolean needVacationHomework = newHomeworkLocations.stream().anyMatch(p -> p.getId().equals("vacationHomework"));
        //把mock的移除
        newHomeworkLocations = newHomeworkLocations.stream().filter(p -> !p.getId().equals("vacationHomework")).collect(Collectors.toList());

        int totalPage = locationPage.getTotalPages();
        //完成情况
        Set<String> accomplishmentIds = new HashSet<>();
        newHomeworkLocations.forEach(p -> accomplishmentIds.add(NewAccomplishment.ID.build(p.getCreateTime(),
                p.getSubject(), p.getId()).toString()));
        Map<String, NewAccomplishment> accomplishmentMap = newAccomplishmentLoaderClient.loadNewAccomplishments(accomplishmentIds);
        List<Map<String, Object>> mapList = new ArrayList<>();
        //需要二次处理的index
        int i = 0;
        List<Integer> needRedoIndex = new ArrayList<>();
        //需要load成绩和错题的作业Id
        Map<String, NewHomework.Location> needReLoadHomeworkLocations = new HashMap<>();

        for (NewHomework.Location location : newHomeworkLocations) {
            if (location == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            NewAccomplishment newAccomplishment = accomplishmentMap.get(NewAccomplishment.ID.build(location.getCreateTime(), location.getSubject(), location.getId()).toString());
            boolean selfFinish = newAccomplishment == null ? Boolean.FALSE : newAccomplishment.contains(studentId);
            if (!location.isChecked() && !selfFinish && new Date(location.getEndTime()).after(new Date())) {
                //待完成
                map.put("desc", "待完成");
                map.put("finish", false);
            } else if (!location.isChecked() && selfFinish) {
                //已完成未检查
                map.put("desc", "待老师检查");
                map.put("finish", true);
            } else if (location.isChecked() && selfFinish) {
                //已完成已检查
                map.put("desc", "");
                map.put("finish", true);
                needRedoIndex.add(i);
                needReLoadHomeworkLocations.put(location.getId(), location);
            } else {
                //未按时完成
                map.put("desc", "未按时完成");
                map.put("finish", false);
            }
            map.put("homework_id", location.getId());
            map.put("create_time", location.getCreateTime());
            map.put("name", location.getSubject().getValue() + "作业");
            map.put("is_vacation", Boolean.FALSE);
            i++;
            mapList.add(map);
        }
        if (CollectionUtils.isNotEmpty(needRedoIndex)) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            boolean isHitScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            Map<String, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoaderClient.loadNewHomeworkResult(needReLoadHomeworkLocations.values(), studentId, false).stream().collect(Collectors.toMap(NewHomeworkResult::getHomeworkId, Function.identity()));
            Map<String, List<Map<String, Object>>> wrongQuestionIds = newHomeworkLoaderClient.getStudentWrongQuestionIds(studentId, null, needReLoadHomeworkLocations.keySet());
            for (Integer index : needRedoIndex) {
                Map<String, Object> map = mapList.get(index);
                String homeworkId = SafeConverter.toString(map.get("homework_id"));
                if (StringUtils.isBlank(homeworkId)) {
                    continue;
                }
                if (needReLoadHomeworkLocations.get(homeworkId) == null) {
                    continue;
                }
                //错题数
                NewHomework.Location location = needReLoadHomeworkLocations.get(homeworkId);
                int wrongCount = ParentHomeworkUtil.getWrongCountWithHomeworkId(location, wrongQuestionIds);
                //分数
                NewHomeworkResult newHomeworkResult = homeworkResultMap.get(homeworkId);
                Integer score = newHomeworkResult == null ? Integer.valueOf(0) : newHomeworkResult.processScore();
                map.put("desc", generateHomeworkDesc(wrongCount, score, isHitScoreLevel));
            }
        }
        //日期时间线
        String today = DateUtils.dateToString(DayRange.current().getStartDate(), "MM月dd日");
        String yesterday = DateUtils.dateToString(DayRange.current().previous().getStartDate(), "MM月dd日");
        //需要处理上一页最后一条记录的日期
        String lastPageDay = "";
        if (time != 0) {
            lastPageDay = DateUtils.dateToString(new Date(time), "MM月dd日");
        }
        Map<String, List<Map<String, Object>>> dayMap = mapList.stream().collect(Collectors.groupingBy(e -> DateUtils.dateToString(new Date(SafeConverter.toLong(e.get("create_time"))), "MM月dd日")));
        for (String day : dayMap.keySet()) {
            //上一页已经有这一天的了。不处理
            if (lastPageDay.equals(day)) {
                continue;
            }
            if (today.equals(day)) {
                List<Map<String, Object>> dayMapList = dayMap.get(day);
                if (CollectionUtils.isNotEmpty(dayMapList)) {
                    dayMapList.get(0).put("date_title", "今天");
                }

            } else if (yesterday.equals(day)) {
                List<Map<String, Object>> dayMapList = dayMap.get(day);
                if (CollectionUtils.isNotEmpty(dayMapList)) {
                    dayMapList.get(0).put("date_title", "昨天");
                }
            } else {
                List<Map<String, Object>> dayMapList = dayMap.get(day);
                if (CollectionUtils.isNotEmpty(dayMapList)) {
                    dayMapList.get(0).put("date_title", day);
                }
            }
        }
        //寒假作业只是暂时的。不想去改普通作业正常的逻辑。在这里额外处理吧。
        //还是按照作业分页。寒假作业的createTime永远摆在2月12日的序列里面
        //最后显示日期 4.12号
        DayRange lastDay = DayRange.parse("20170412");
        DayRange startDay;
        if (RuntimeMode.isProduction()) {
            startDay = DayRange.newInstance(NewHomeworkConstants.earliestVacationHomeworkStartDate(RuntimeMode.current()).getTime());
        } else {
            startDay = DayRange.current();
        }
        List<VacationReportForParent> vacationReport = new ArrayList<>();
        if (DayRange.current().getStartTime() >= startDay.getStartTime() && DayRange.current().getEndTime() <= lastDay.getEndTime()) {
            if (needVacationHomework) {
                try {
                    vacationReport = vacationHomeworkReportLoaderClient.loadVacationReportForParent(studentId);
                } catch (Exception e) {
                    logger.error("get vacation homework dateLine failed : sid of {}", studentId, e);
                    return MapMessage.errorMessage();
                }
            }
        }
        vacationReport = vacationReport.stream().filter(p -> filterSubject == null || p.getSubject() == filterSubject).sorted(Comparator.comparingInt(o -> o.getSubject().getKey())).collect(Collectors.toList());
        //查询学豆记录
        List<IntegralHistory> integralHistories = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(vacationReport)) {
            integralHistories = integralHistoryLoaderClient.getIntegralHistoryLoader().loadUserIntegralHistories(studentId);
        }
        for (VacationReportForParent report : vacationReport) {
            Map<String, Object> map = new HashMap<>();
            map.put("date_title", "寒假作业");
            map.put("name", report.getSubject().getValue() + "寒假作业");
            String desc;
            if (!report.isBegin()) {
                desc = "未开始";
            } else if (report.isFinish()) {
                desc = "全部完成";
            } else {
                desc = "完成进度" + report.getFinishedVacationHomework() + "/" + report.getTotalHomeworkNum();
            }
            map.put("desc", desc);
            map.put("homework_id", report.getLocation().getId());
            map.put("finish", report.isFinish());
            map.put("create_time", conditionDay.getEndDate().getTime());
            map.put("is_vacation", Boolean.TRUE);
            map.put("had_received", integralHistories.stream().anyMatch(p -> ("homeworkType:" + NewHomeworkType.WinterVacation.name() + "," + "homeworkId:" + report.getLocation().getId()).equals(p.getUniqueKey())));
            map.put("vacation_date", DateUtils.dateToString(new Date(report.getLocation().getStartTime()), DateUtils.FORMAT_SQL_DATE) + "至" + DateUtils.dateToString(new Date(report.getLocation().getEndTime()), DateUtils.FORMAT_SQL_DATE));
            mapList.add(map);
        }
        mapList.sort((o1, o2) -> ((Long) o2.get("create_time")).compareTo(SafeConverter.toLong(o1.get("create_time"))));
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        return MapMessage.successMessage().add("homework_list", mapList).add("subject_list", subjectMapList).add("totalPage", totalPage).add("isGraduate", clazz != null && clazz.isTerminalClazz());
    }


    @RequestMapping(value = "/homework/loadVacationReportToSubject.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadVacationReportToSubject() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (studentId <= 0L) {
            logger.warn("sid {} is error with parent {}", studentId, parent.getId());
            return MapMessage.errorMessage("参数错误");
        }
        try {
            List<VacationReportToSubject> vacationReportToSubjects = vacationHomeworkReportLoaderClient.loadVacationReportToSubject(studentId);
            MapMessage mapMessage = MapMessage.successMessage();

            mapMessage.add("vacationReportToSubjects", vacationReportToSubjects);
            return mapMessage;
        } catch (Exception e) {
            logger.error("load VacationReport To Subject failed of sid {}", studentId, e);
            return MapMessage.errorMessage();
        }
    }


    @RequestMapping(value = "/homework/receiveVacationReward.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage receiveVacationReward() {
        User parent = currentParent();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String vacationHomeworkId = getRequestString(REQ_VACATION_HOMEWORK_ID);
        Integer vacationHomeworkLevel = getRequestInt(REQ_VACATION_HOMEWORK_LEVEL);
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(vacationHomeworkId)) {
            return MapMessage.errorMessage("假期作业ID不能为空").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        VacationHomeworkLevel level = VacationHomeworkLevel.fromVacationHomeworkLevel(vacationHomeworkLevel);
        if (level == null) {
            return MapMessage.errorMessage("假期作业关卡参数错误").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }

        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        List<VacationReportForParent> reportList;
        try {
            reportList = vacationHomeworkReportLoaderClient.loadVacationReportForParent(studentId);
        } catch (Exception e) {
            logger.error("get vacation homework dateLine failed : sid of {}", studentId, e);
            return MapMessage.errorMessage();
        }

        VacationReportForParent report = reportList.stream().filter(p -> p.getLocation().getId().equals(vacationHomeworkId)).findFirst().orElse(null);
        if (report == null || report.getLocation() == null) {
            return MapMessage.errorMessage().setErrorCode("此学生没有此ID的假期作业").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        int planDays = SafeConverter.toInt(report.getLocation().getPlannedDays(), 30);
        int level1EndDay = planDays == 30 ? 15 : 10;
        int currentLevelEndDay = level == VacationHomeworkLevel.LEVEL_1 ? level1EndDay : planDays;
        if (report.getFinishedVacationHomework() < currentLevelEndDay) {
            return MapMessage.errorMessage("未完成关卡任务，不能领取奖励");
        }

        String uniqueKey = StringUtils.join(Arrays.asList(NewHomeworkType.WinterVacation.name(), vacationHomeworkId, vacationHomeworkLevel), ":");
        List<IntegralHistory> integralHistories = integralHistoryLoaderClient.getIntegralHistoryLoader().loadUserIntegralHistories(studentId);
        boolean hadReceived = integralHistories.stream().anyMatch(p -> uniqueKey.equals(p.getUniqueKey()));
        if (hadReceived) {
            newHomeworkCacheServiceClient.getNewHomeworkCacheService().vacationHomeworkIntegralCacheManager_recordStudentReward(studentId, vacationHomeworkId, vacationHomeworkLevel);
            return MapMessage.errorMessage("对不起，您已经领取奖励");
        }

        IntegralHistory integralHistory = new IntegralHistory();
        integralHistory.setIntegral(level.getStudentIntegral());
        integralHistory.setComment("完成假期作业奖励");
        integralHistory.setIntegralType(IntegralType.FINISH_VACATION_HOMEWORK_STUDENT_REWARD.getType());
        integralHistory.setUserId(studentId);
        integralHistory.setUniqueKey(uniqueKey);
        try {
            userIntegralService.changeIntegral(integralHistory);
            newHomeworkCacheServiceClient.getNewHomeworkCacheService().vacationHomeworkIntegralCacheManager_recordStudentReward(studentId, vacationHomeworkId, vacationHomeworkLevel);
        } catch (Exception ex) {
            if (ex instanceof DuplicateKeyException) {
                newHomeworkCacheServiceClient.getNewHomeworkCacheService().vacationHomeworkIntegralCacheManager_recordStudentReward(studentId, vacationHomeworkId, vacationHomeworkLevel);
                return MapMessage.errorMessage("已领取奖励");
            } else {
                logger.error("Failed to change integral userId{} vacationHomeworkId{} vacationHomeworkLevel{}", studentId, vacationHomeworkId, vacationHomeworkLevel);
            }
        }
        return MapMessage.successMessage();

    }

    private String generateHomeworkDesc(int wrongCount, Integer score, boolean isHitGray) {
        String desc = "已检查";
        //拼接分数
        if (score != null) {
            if (isHitGray) {
                desc += "，成绩" + ScoreLevel.processLevel(score).getLevel();
            } else {
                desc += "，成绩" + String.valueOf(score);
            }
        }
        //拼接错题
        if (wrongCount > 0) {
            desc += "，错题" + wrongCount + "题";
        }
        return desc;
    }

    private void generateJxtNewsBookInfo(JztReport.HomeworkDescription homeworkDescription, NewHomework newHomework, StudentDetail studentDetail) {
        MapMessage mapMessage = newHomeworkPartLoaderClient.getStudentHomeworkProgress(studentDetail.getId(), newHomework.getSubject(), newHomework.getId());
        if (mapMessage.isSuccess()) {
            Map<String, Object> jxtNewsBookInfoMap = new HashMap<>();

            String bookId = SafeConverter.toString(mapMessage.get("bookId"), "");
            String unitId = SafeConverter.toString(mapMessage.get("unitId"), "");
            String sectionId = SafeConverter.toString(mapMessage.get("sectionId"), "");
            List<String> newsIds = jxtNewsLoaderClient.getJxtNewsBookRefByUnitOrSection(unitId, sectionId);
            Map<String, JxtNews> jxtNewsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);

            Set<Long> studentIds = studentLoaderClient.loadParentStudents(currentUserId()).stream().map(User::getId).collect(Collectors.toSet());
            Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
            Set<Integer> parentRegionCodes = getParentRegionCode(studentDetails);
            //同步内容列表
            List<Map<String, Object>> jxtNewsBookInfo = jxtNewsMap.values().stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getOnline() != null && p.getOnline())
                    .filter(p -> p.generateStyleType().equals(JxtNewsStyleType.EXTERNAL_SYNC_TEACHING_MATERIAL.name()) || p.generateStyleType().equals(JxtNewsStyleType.SYNC_TEACHING_MATERIAL.name()))
                    //北京、上海不显示清大百年和爱学堂
                    .filter(e -> CollectionUtils.isEmpty(parentRegionCodes)
                            || (CollectionUtils.isNotEmpty(parentRegionCodes) && !(parentRegionCodes.contains(110000) || parentRegionCodes.contains(310000)))
                            || (CollectionUtils.isNotEmpty(parentRegionCodes) && (parentRegionCodes.contains(110000) || parentRegionCodes.contains(310000)) && !(StringUtils.equals(e.getSource(), "清大百年学习网") || StringUtils.equals(e.getSource(), "爱学堂"))))

                    .limit(2)
                    .map(e -> MapUtils.m(
                            "title", e.getTitle(),
                            "contentType", e.getJxtNewsContentType()))
                    .collect(Collectors.toList());

            jxtNewsBookInfoMap.put("bookId", bookId);
            jxtNewsBookInfoMap.put("unitId", unitId);
            jxtNewsBookInfoMap.put("sectionId", sectionId);
            jxtNewsBookInfoMap.put("jxtNewsBookInfo", jxtNewsBookInfo);

            Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(Collections.singleton(unitId));
            if (MapUtils.isNotEmpty(newBookCatalogMap) && newBookCatalogMap.get(unitId) != null) {
                NewBookCatalog newBookCatalog = newBookCatalogMap.get(unitId);
                if (newBookCatalog != null) {
                    String unitName;
                    if (newBookCatalog.getSubjectId() == Subject.ENGLISH.getId()) {
                        unitName = newBookCatalog.getAlias();
                    } else {
                        unitName = newBookCatalog.getName();
                    }
                    jxtNewsBookInfoMap.put("unitName", unitName);
                }
            }
            homeworkDescription.setJxtNewsBookInfoMap(jxtNewsBookInfoMap);
        }
    }

    private void handleSyllableInfo(JztStudentHomeworkReport.SyllableModule syllableModule, NewHomework newHomework, StudentDetail studentDetail, String sys) {
        //在灰度内才处理
        if (syllableModule != null && syllableModule.isVoiceFlag()) {
            //点读机是否有匹配教材
            String bookId = SafeConverter.toString(syllableModule.getBookId(), "");
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
            boolean hasPicListenContent = textBookManagementLoaderClient.hasPicListenContent(newBookProfile, sys);
            syllableModule.setHasPicListenContent(hasPicListenContent);

            Map<String, PicListenBookPayInfo> itemMap = picListenCommonService.userBuyBookPicListenLastDayMap(studentDetail, false);
            //是否已付费
            boolean hasPay = itemMap.containsKey(bookId);
            syllableModule.setHasPay(hasPay);
            //教材是否需要付费
            boolean needPay = true;
            if (!hasPay) {
                needPay = textBookManagementLoaderClient.picListenBookNeedPay(bookId);
            }
            syllableModule.setNeedPay(needPay);

            boolean finishTask = false;
            boolean showTask = false;
            if (hasPicListenContent) {
                //是否完成任务
                finishTask = parentSelfStudyPublicHelper.taskIsFinish(newHomework.getId(), studentDetail.getId());

                List<Map> unitAndSentence = syllableModule.getUnitAndSentenceList();
                List<Map> list = CollectionUtils.isNotEmpty(unitAndSentence) ? unitAndSentence : new ArrayList<>();
                showTask = parentSelfStudyPublicHelper.showTask(list, 75);
            }
            syllableModule.setFinishTask(finishTask);
            //是否匹配到75%
            syllableModule.setShowTask(showTask);

            if (hasPicListenContent && syllableModule.isHasVoice() && showTask && (!needPay || hasPay)) {
                //作业报告有点读机任务，家长签字任务中存上这种类型的学豆奖励
                asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                        .JztHomeworkReportCacheManager_recordReportMissionIntegral(newHomework.getId(), getRequestLong(REQ_STUDENT_ID), HomeWorkReportMissionType.PIC_LISTEN_MISSION, 1)
                        .awaitUninterruptibly();
            }
        }
    }

    private void generateSyllableInfo(JztReport.TeacherSummaryPart.TalkingModule talkingModule, NewHomework newHomework, StudentDetail studentDetail, String sys) {
        //在灰度内才处理
        if (talkingModule.isVoiceFlag()) {
            //点读机是否有匹配教材
            String bookId = SafeConverter.toString(talkingModule.getBookId(), "");
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
            boolean hasPicListenContent = textBookManagementLoaderClient.hasPicListenContent(newBookProfile, sys);
            talkingModule.setHasPicListenContent(hasPicListenContent);

            Map<String, PicListenBookPayInfo> itemMap = picListenCommonService.userBuyBookPicListenLastDayMap(studentDetail, false);
            String itemId = bookId;
            //是否已付费
            boolean hasPay = itemMap.containsKey(itemId);
            talkingModule.setHasPay(hasPay);
            //教材是否需要付费
            boolean needPay = true;
            if (!hasPay) {
                needPay = textBookManagementLoaderClient.picListenBookNeedPay(itemId);
            }
            talkingModule.setNeedPay(needPay);

            boolean finishTask = false;
            boolean showTask = false;
            if (hasPicListenContent) {
                //是否完成任务
                finishTask = parentSelfStudyPublicHelper.taskIsFinish(newHomework.getId(), studentDetail.getId());

                List<Map> unitAndSentence = talkingModule.getUnitAndSentenceList();
                List<Map> list = CollectionUtils.isNotEmpty(unitAndSentence) ? unitAndSentence : new ArrayList<>();
                showTask = parentSelfStudyPublicHelper.showTask(list, 75);
            }
            talkingModule.setFinishTask(finishTask);
            //是否匹配到75%
            talkingModule.setShowTask(showTask);

            if (hasPicListenContent && talkingModule.isHasVoice() && showTask && (!needPay || hasPay)) {
                //作业报告有点读机任务，家长签字任务中存上这种类型的学豆奖励
                asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                        .JztHomeworkReportCacheManager_recordReportMissionIntegral(newHomework.getId(), getRequestLong(REQ_STUDENT_ID), HomeWorkReportMissionType.PIC_LISTEN_MISSION, 1)
                        .awaitUninterruptibly();
            }
        }
    }

    private void generateSummaryRewardInfo(JztReport jztReport, NewHomework newHomework, NewHomeworkResult newHomeworkResult, StudentDetail studentDetail) {
        if (jztReport != null && jztReport.isFinished()) {
            JztReport.SummaryReward summaryReward = jztReport.getSummaryReward();
            String homeworkId = newHomework.getId();
            //完成作业领取额外学豆
            Integer integralPrize = 0;
            NewHomeworkFinishRewardInParentApp rewardInParentApp = newHomeworkPartLoaderClient.getRewardInParentApp(studentDetail.getId());
            if (rewardInParentApp != null) {
                if (MapUtils.isNotEmpty(rewardInParentApp.getNotReceivedRewardMap()) && rewardInParentApp.getNotReceivedRewardMap().containsKey(homeworkId)) {
                    integralPrize = rewardInParentApp.getNotReceivedRewardMap().get(homeworkId).getRewardCount();
                } else if (MapUtils.isNotEmpty(rewardInParentApp.getHadReceivedRewardMap()) && rewardInParentApp.getHadReceivedRewardMap().containsKey(homeworkId)) {
                    integralPrize = rewardInParentApp.getHadReceivedRewardMap().get(homeworkId);
                }
            }

            List<IntegralHistory> integralHistories = integralHistoryLoaderClient.getIntegralHistoryLoader().loadUserIntegralHistories(studentDetail.getId());
            IntegralHistory received = integralHistories.stream()
                    .filter(p -> p.getIntegralType().equals(IntegralType.作业检查后家长领取学豆奖励_产品平台.getType()))
                    .filter(p -> ("homeworkType:" + newHomework.getSubject() + "," + "homeworkId:" + homeworkId).equals(p.getUniqueKey()))
                    .findFirst().orElse(null);
            summaryReward.setHasReceived(received != null);
            summaryReward.setExtraIntegral(integralPrize);

            //作业完成获得学豆
            IntegralHistory integralHistory = integralHistories.stream()
                    .filter(e -> e.getIntegralType() == IntegralType.学生完成作业.getType())
                    .filter(e -> ("homeworkType:" + newHomework.getSubject() + "," + "homeworkId:" + homeworkId).equals(e.getUniqueKey()))
                    .findFirst()
                    .orElse(null);
            summaryReward.setHomeworkIntegral(integralHistory != null ? SafeConverter.toInt(integralHistory.getIntegral()) : 0);

            //老师奖励学豆和评语
            if (newHomeworkResult != null) {
                summaryReward.setTeacherIntegral(SafeConverter.toInt(newHomeworkResult.getRewardIntegral()));
            }
            jztReport.setSummaryReward(summaryReward);
        }
    }

    @Deprecated
    private void generateFlowerInfo(JztReport jztReport) {
        // 灰度关闭鲜花 http://project.17zuoye.net/redmine/issues/34943
//        boolean closeFlower = getGrayFunctionManagerClient().getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "Flower", "Close");
        JztReport.FlowerModule flowerModule = jztReport.getFlowerModule();
        boolean today = jztReport.isToday();
        flowerModule.setCloseFlower(!today);
        if (today) {
            String secondFlowerText = null;
            String thirdFlowerText = null;
            String title = "感谢师恩";
            String flowerText = "点击鲜花，感谢老师的辛勤付出吧！";
            String startTimeStr = "2017-09-07";
            Date startTime = DateUtils.stringToDate(startTimeStr, DateUtils.FORMAT_SQL_DATE);
            Date endTime = DateUtils.stringToDate("2017-09-13", DateUtils.FORMAT_SQL_DATE);
            Date currentTime = new Date();
            if (currentTime.after(startTime) && currentTime.before(endTime)) {
                title = "感恩教师节活动";
                flowerText = "点击鲜花，祝老师教师节快乐！";
                secondFlowerText = "活动期间老师布置作业鲜花双倍";
                thirdFlowerText = "活动时间：" + DateUtils.dateToString(startTime, "MM月dd日") + "—9月12日";
            }
            flowerModule.setSendFlower(false);
            flowerModule.setSenderCount(0);
            flowerModule.setFlowerText(flowerText);
            flowerModule.setTitle(title);
            flowerModule.setSecondFlowerText(secondFlowerText);
            flowerModule.setThirdFlowerText(thirdFlowerText);
        }
    }

    //获取家长的regionCode
    private Set<Integer> getParentRegionCode(Collection<StudentDetail> studentDetails) {
        if (CollectionUtils.isEmpty(studentDetails)) {
            return new HashSet<>();
        }
        Set<Integer> parentRegionIds = new HashSet<>();
        for (StudentDetail studentDetail : studentDetails) {
            parentRegionIds.add(studentDetail.getStudentSchoolRegionCode());
            parentRegionIds.add(studentDetail.getCityCode());
            parentRegionIds.add(studentDetail.getRootRegionCode());
        }
        return parentRegionIds;
    }

    @RequestMapping(value = "clazz/homework/report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getClazzHomeworkReport() {
        String homeworkId = getRequestString(REQ_PARENT_APP_HOMEWORK_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("sid error");
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("hid error");
        }
        try {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                return MapMessage.errorMessage("student error");
            }
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage(RES_RESULT_HOMEWORK_HAD_DELETE).add("is_delete", true);
            }
            Long groupId = newHomework.getClazzGroupId();
            List<Long> groupStudentIds = studentLoaderClient.loadGroupStudentIds(Collections.singleton(groupId)).get(groupId);
            if (CollectionUtils.isEmpty(groupStudentIds) || !groupStudentIds.contains(studentId)) {
                return MapMessage.errorMessage("报告内容与当前选择的孩子帐号不符，请检查后重试~");
            }
            Date currentDate = new Date();
            if (DateUtils.dayDiff(currentDate, newHomework.getEndTime()) > 60) {
                return MapMessage.errorMessage("该作业已截止超过60天，我们暂时仅支持60天以内的作业内容哦").add("is_expired", true);
            }
            boolean webGrayFunctionAvailable = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jzt", "newHomework");
            if (!webGrayFunctionAvailable) {
                return MapMessage.errorMessage("作业报告暂时无法查看，请明天查看");
            }

            JztClazzHomeworkReport jztClazzReport = newHomeworkReportServiceClient.loadJztClazzHomeworkReport(newHomework, studentDetail, getCdnBaseUrlAvatarWithSep());
            MapMessage mapMessage = new MapMessage();
            mapMessage.add("jztClazzReport", jztClazzReport);
            //如果作业未分享状态则显示班级学生奖励情况
            if (JztClazzHomeworkReport.ReportStatus.unshared.equals(jztClazzReport.getReportStatus())) {
                Clazz clazz = studentDetail.getClazz();
                if (clazz != null) {
                    List<User> students = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazz.getId(), studentId);
                    Map<Long, User> studentMap = students.stream().collect(Collectors.toMap(User::getId, Function.identity(), (u, v) -> u));
                    Map<Long, ParentRewardLog> rewardLogMap = parentRewardLoader.getHomeworkRewardLogs(studentMap.keySet(), homeworkId);
                    List<Map<String, Object>> rewardInfoList = new ArrayList<>();
                    rewardLogMap.forEach((key, value) -> {
                        User student = studentMap.get(key);
                        if (student != null && StringUtils.isNotBlank(student.fetchRealname()) && value.getStatus() >= 1) {
                            Map<String, Object> map = new HashMap<>();
                            String avatar = getUserAvatarImgUrl(student);
                            map.put("student_avatar", avatar);
                            map.put("student_name", student.fetchRealname());
                            map.put("reward_count", value.getCount());
                            rewardInfoList.add(map);
                        }
                    });
                    mapMessage.add("rewardInfoList", rewardInfoList);
                }
            }
            generateRewardInfo(mapMessage, parent.getId(), studentId, homeworkId);
            return mapMessage.setSuccess(true);
        } catch (Exception e) {
            log.error("get homework report error. homeworkId:{}, studentId: {}", homeworkId, studentId, e);
            return MapMessage.errorMessage("获取作业报告失败，请稍后再试");
        }
    }

    private void getFlowerInfo(MapMessage mapMessage, Long studentId, NewHomework newHomework) {
        Flower flower = flowerServiceClient.getFlowerService().loadHomeworkFlowers(newHomework.getId()).getUninterruptibly()
                .stream()
                .filter(f -> Objects.equals(studentId, f.getSenderId()) && FlowerSourceType.HOMEWORK.name().equals(f.getSourceType()))
                .findFirst()
                .orElse(null);
        mapMessage.add("hasSentFlower", flower != null);
        mapMessage.add("flowerDetailUrl", ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/send_flower/index.vpage?group_id=" + newHomework.getClazzGroupId());
    }

    private void generateRewardInfo(MapMessage mapMessage, Long parentId, Long studentId, String homeworkId) {
        //奖励按钮状态，0-没有奖励可发；1-有奖励可发；2有奖励可发且已经发放
        int rewardButtonStatus;
        String rewardDetailUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/rewards/detail.vpage?ref=hwReport";
        ParentRewardLog rewardLog = parentRewardLoader.getHomeworkRewardLog(studentId, homeworkId);
        if (rewardLog == null) {
            rewardButtonStatus = 0;
        } else if (rewardLog.getStatus() == 0) {
            boolean sendAvailable = parentRewardService.rewardSendAvailable(parentId, studentId, isParentRewardNewVersionForFaceDetect(getAppVersion()));
            if (sendAvailable) {
                //调用发奖励接口需要传sid和sendRewardInfoMap中的参数
                Map<String, Object> sendRewardInfoMap = new HashMap<>();
                sendRewardInfoMap.put("id", rewardLog.getId());
                sendRewardInfoMap.put("key", rewardLog.getKey());
                sendRewardInfoMap.put("type", rewardLog.getType());
                sendRewardInfoMap.put("count", rewardLog.getCount());
                mapMessage.add("sendRewardMap", sendRewardInfoMap);
                rewardButtonStatus = 1;
            } else {
                rewardButtonStatus = 2;
            }
        } else {
            rewardButtonStatus = 3;
        }
        mapMessage.add("rewardButtonStatus", rewardButtonStatus);
        mapMessage.add("rewardDetailUrl", rewardDetailUrl);
    }

    @RequestMapping(value = "homework/notice.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getHomeworkNotice() {
        User parent = currentParent();
        long studentId = getRequestLong(REQ_STUDENT_ID);
        String hid = getRequestString(REQ_PARENT_APP_HOMEWORK_ID);
        try {
            if (parent == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }
            if (studentId == 0L) {
                return MapMessage.errorMessage("学生id错误");
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                return MapMessage.errorMessage("学生id不存在");
            }
            if (StringUtils.isBlank(hid)) {
                return MapMessage.errorMessage("作业id错误");
            }
            NewHomework newHomework = newHomeworkLoaderClient.load(hid);
            if (newHomework == null) {
                return MapMessage.errorMessage("作业id不存在");
            }
            if (newHomework.isDisabledTrue()) {
                return MapMessage.errorMessage("作业已删除");
            }
            Long groupId = newHomework.getClazzGroupId();
            List<Long> groupStudentIds = studentLoaderClient.loadGroupStudentIds(Collections.singleton(groupId)).get(groupId);
            if (CollectionUtils.isEmpty(groupStudentIds) || !groupStudentIds.contains(studentId)) {
                return MapMessage.errorMessage("报告内容与当前选择的孩子帐号不符，请检查后重试~");
            }
            JztHomeworkNotice jztHomeworkNotice = newHomeworkReportServiceClient.loadJztHomeworkNotice(newHomework, studentDetail, getCdnBaseUrlAvatarWithSep(), parent.getId());
            if (jztHomeworkNotice != null) {
                Flower flower = flowerServiceClient.getFlowerService().loadHomeworkFlowers(hid).getUninterruptibly()
                        .stream()
                        .filter(f -> Objects.equals(studentId, f.getSenderId()) && FlowerSourceType.HOMEWORK.name().equals(f.getSourceType()))
                        .findFirst()
                        .orElse(null);
                jztHomeworkNotice.setHasSentFlower(flower != null);
                jztHomeworkNotice.setFlowerDetailUrl(ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/send_flower/index.vpage?group_id=" + newHomework.getClazzGroupId());
                return MapMessage.successMessage().add("notice", jztHomeworkNotice);
            } else {
                return MapMessage.errorMessage("获取作业通知失败");
            }
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "/flower/send.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendFlower() {
        Long studentId = getRequestLong("sid");
        String homeworkId = getRequestString("hid");
        FlowerSourceType flowerSourceType=FlowerSourceType.HOMEWORK;
        String flowerType = getRequestString("flowerSourceType");
        if (StringUtils.isNotEmpty(flowerType) && "HOMEWORK_COMMENT".equals(flowerType)) {
            flowerSourceType = FlowerSourceType.HOMEWORK_COMMENT;
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (studentId == 0L) {
            return MapMessage.errorMessage("学生id错误");
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id错误");
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return MapMessage.errorMessage("此学生和家长无关联");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生信息错误");
        }
        if (studentDetail.getClazz() == null) {
            return MapMessage.errorMessage("该学生当前无班级，不能送花");
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("要送花的作业不存在");
        }
        try {
            Long teacherId = newHomework.getTeacherId();
            Long clazzId = studentDetail.getClazzId();
            Long groupId = newHomework.getClazzGroupId();
            return flowerConditionService.sendFlower(studentId, parent.getId(), teacherId, clazzId, groupId, flowerSourceType, homeworkId)
                    .getUninterruptibly();
        } catch (Exception ex) {
            logger.error("homework report send flower error. sid:{}, hid:{}", studentId, homeworkId, ex);
            return MapMessage.errorMessage("作业报告送花失败");
        }
    }

    private Map processQuestionUrl(String homeworkId) {
        Map questionUrlMap = MapUtils.m(
                "questionUrl", UrlUtils.buildUrlQuery("/parentMobile/jzt/homework/report/questions" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "homeworkId", homeworkId,
                                "type", "")),
                "completedUrl", UrlUtils.buildUrlQuery("/parentMobile/jzt/homework/report/questions/answer" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "homeworkId", homeworkId,
                                "type", ""))
        );
        return questionUrlMap;
    }

    /**
     * 作业报告班级报告同步详情
     * copy from {@link TeacherNewHomeworkReportController#reportErrorRate()}
     */
    @RequestMapping(value = "homework/report/error/rate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage reportErrorRate() {
        String homeworkId = getRequestString("homeworkId");
        Long studentId = getRequestLong("studentId");
        MapMessage mapMessage = new MapMessage();
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework != null) {
            mapMessage = newHomeworkReportServiceClient.loadNewHomeworkReportExamErrorRates(homeworkId, studentId, newHomework.getTeacherId());
            mapMessage.putAll(processQuestionUrl(homeworkId));
            if (mapMessage.isSuccess()) {
                mapMessage.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            }
            return mapMessage;
        } else {
            return mapMessage.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }

    }

    /**
     * 获取作业应试试题信息
     * copy from {@link TeacherNewHomeworkController#questionsAnswer()}
     */
    @RequestMapping(value = "homework/report/questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map questionsAnswer() {
        String homeworkId = getRequestString("homeworkId");
        String type = getRequestString("type");
        Long studentId = getRequestLong("studentId");
        String stoneDataId = getRequestString("stoneDataId");

        if (StringUtils.isAnyBlank(homeworkId, type)) {
            return MapMessage.errorMessage("作业不存在");
        }

        if (studentId == 0L) {
            return MapMessage.errorMessage("请选择学生");
        }

        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
        request.setHomeworkId(homeworkId);
        request.setObjectiveConfigType(objectiveConfigType);
        request.setStudentId(studentId);
        request.setCategoryId(getRequestInt("categoryId", 0));
        request.setLessonId(getRequestString("lessonId"));
        request.setVideoId(getRequestString("videoId"));
        request.setStoneDataId(stoneDataId);
        return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadQuestionAnswer(request));
    }

    /**
     * 获取作业应试试题信息
     * copy from {@link TeacherNewHomeworkController#questions()}
     */
    @RequestMapping(value = "homework/report/questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map questions() {
        String homeworkId = getRequestString("homeworkId");
        String type = getRequestString("type");

        if (StringUtils.isAnyBlank(homeworkId, type)) {
            return MapMessage.errorMessage("作业不存在");
        }

        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
        request.setHomeworkId(homeworkId);
        request.setObjectiveConfigType(objectiveConfigType);
        request.setCategoryId(getRequestInt("categoryId", 0));
        request.setLessonId(getRequestString("lessonId"));
        return MapMessage.successMessage().add("result", newHomeworkLoaderClient.loadHomeworkQuestions(request));
    }

    /**
     * base_app category type details
     * 基础类型的详情
     * copy form {@link TeacherNewHomeworkReportController#detailsBaseApp()}
     */
    @RequestMapping(value = "homework/report/detailsbaseapp.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage detailsBaseApp() {
        String homeworkId = getRequestString("homeworkId");
        String categoryId = getRequestString("categoryId");
        String lessonId = getRequestString("lessonId");
        Long studentId = getRequestLong("studentId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.BASIC_APP.name()));

        if (StringUtils.isAnyBlank(homeworkId, categoryId, lessonId)
                || objectiveConfigType == null) {
            return MapMessage.errorMessage("fetch baseApp detail failed hid of {},categoryId of {},lessonId of {}", homeworkId, categoryId, lessonId);
        }
        return studentId == 0L ?
                newHomeworkReportServiceClient.reportDetailsBaseApp(homeworkId, categoryId, lessonId, objectiveConfigType) :
                newHomeworkReportServiceClient.reportDetailsBaseApp(homeworkId, categoryId, lessonId, studentId, objectiveConfigType);
    }

    @RequestMapping(value = "homework/report/personalreadingdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalReadingDetail() {
        String homeworkId = getRequestString("homeworkId");
        String readingId = getRequestString("readingId");
        Long studentId = getRequestLong("studentId");
        if (StringUtils.isAnyBlank(homeworkId, readingId)
                || studentId <= 0) {
            return MapMessage.errorMessage("homeworkId or studentId or readingId is null");
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.READING.name()));
        MapMessage mapMessage = newHomeworkReportServiceClient.personalReadingDetail(homeworkId, studentId, readingId, newHomework.getTeacherId(), objectiveConfigType);
        mapMessage.putAll(processQuestionUrl(homeworkId));
        return mapMessage;
    }

    /**
     * 趣味配音个人二级详情页面
     * copy form {@link TeacherNewHomeworkReportController#personalDubbingDetail()}
     */
    @RequestMapping(value = "homework/report/personaldubbingdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalDubbingDetail() {
        Long studentId = getRequestLong("studentId");
        String homeworkId = getRequestString("homeworkId");
        String dubbingId = getRequestString("dubbingId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(dubbingId)) {
            return MapMessage.errorMessage("配音id为空");
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        try {
            return newHomeworkReportServiceClient.personalDubbingDetail(homeworkId, studentId, dubbingId, newHomework.getTeacherId());
        } catch (Exception ex) {
            logger.error("Failed to load personalDubbingDetail homeworkId:{},studentId{},dubbingId{}", homeworkId, studentId, dubbingId, ex);
            return MapMessage.errorMessage("获取趣味配音个人二级详情异常");
        }
    }

    /**
     * 口语交际个人二级详情页面
     *
     * @return
     */
    @RequestMapping(value = "report/personaloralcommunicationdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalOralCommunicationScoreDetail() {
        Long studentId = getRequestLong("studentId");
        String homeworkId = getRequestString("homeworkId");
        String stoneId = getRequestString("stoneId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(stoneId)) {
            return MapMessage.errorMessage("题包为空");
        }
        try {
            return newHomeworkReportServiceClient.personalOralCommunicationDetail(homeworkId, studentId, stoneId, null);
        } catch (Exception ex) {
            logger.error("Failed to load personalOralCommunicationScoreDetail homeworkId:{},studentId{},stoneId{}", homeworkId, stoneId, ex);
            return MapMessage.errorMessage("获取口语交际个人二级详情异常");
        }

    }

    /**
     * 生子认读个人二级详情页面
     * copy form {@link TeacherNewHomeworkReportController#personalWordRecognitionAndReading()}
     *
     * @return
     */
    @RequestMapping(value = "homework/report/personalwordrecognitionandreading.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalWordRecognitionAndReading() {
        String hid = getRequestString("hid");
        String questionBoxId = getRequestString("questionBoxId");
        Long sid = getRequestLong("sid");
        if (StringUtils.isAnyBlank(hid) || sid == 0) {
            return MapMessage.errorMessage("参数错误");
        }

        MapMessage mapMessage = newHomeworkReportServiceClient.personalWordRecognitionAndReading(hid, questionBoxId, sid);
        mapMessage.putAll(processQuestionUrl(hid));
        return mapMessage;
    }

    /**
     * 教师APP-学生课文读背答题详情
     * copy form {@link TeacherNewHomeworkReportController#personalReadReciteWithScore()}
     *
     * @return
     */
    @RequestMapping(value = "homework/report/personalreadrecitewithscore.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalReadReciteWithScore() {
        String hid = getRequestString("hid");
        String questionBoxId = getRequestString("questionBoxId");
        Long sid = getRequestLong("sid");
        if (StringUtils.isAnyBlank(hid) || sid == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.personalReadReciteWithScore(hid, questionBoxId, sid);
        mapMessage.putAll(processQuestionUrl(hid));
        return mapMessage;
    }

    /**
     * 趣味配音( with score )个人二级详情页面
     * copy form {@link TeacherNewHomeworkReportController#personalDubbingWithScoreDetail()}
     */
    @RequestMapping(value = "homework/report/personaldubbingwithscoredetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalDubbingWithScoreDetail() {
        Long studentId = getRequestLong("studentId");
        String homeworkId = getRequestString("homeworkId");
        String dubbingId = getRequestString("dubbingId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(dubbingId)) {
            return MapMessage.errorMessage("配音id为空");
        }
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        try {
            return newHomeworkReportServiceClient.personalDubbingWithScoreDetail(homeworkId, studentId, dubbingId, newHomework.getTeacherId());
        } catch (Exception ex) {
            logger.error("Failed to load personalDubbingWithScoreDetail homeworkId:{},studentId{},dubbingId{}", homeworkId, dubbingId, ex);
            return MapMessage.errorMessage("获取趣味配音个人二级详情异常");
        }
    }

    /**
     * 获取作业中间结果表（暂时纸质拍照专用）
     * copy from {@link TeacherNewHomeworkReportController#reportForObjectiveConfigTypeResult()}
     */
    @RequestMapping(value = "homework/report/type/result.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage reportForObjectiveConfigTypeResult() {
        String homeworkId = getRequestString("homeworkId");
        String objectiveConfigType = getRequestString("objectiveConfigType");
        Long studentId = getRequestLong("studentId");
        if (StringUtils.isEmpty(homeworkId) || StringUtils.isEmpty(objectiveConfigType)) {
            return MapMessage.errorMessage("参数错误");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生id错误");
        }
        return newHomeworkReportServiceClient.homeworkForObjectiveConfigTypeResult(homeworkId, ObjectiveConfigType.of(objectiveConfigType), studentDetail);
    }


}
