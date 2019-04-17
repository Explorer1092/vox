package com.voxlearning.washington.controller;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.SaveAncientPoetryResultRequest;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.AncientPoetryLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.AncientPoetryServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.washington.controller.teacher.TeacherOutsideReadingController;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.flash.FlashVars;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/21
 */
@Controller
@RequestMapping("/ancient/poetry")
public class AncientPoetryController extends AbstractController {

    @Inject private AncientPoetryServiceClient ancientPoetryServiceClient;
    @Inject private AncientPoetryLoaderClient ancientPoetryLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;

    /**
     * 老师班级列表接口 {@link TeacherOutsideReadingController#loadReportClazzList()}
     */

    /**
     * 老师活动列表
     */
    @RequestMapping(value = "activity/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchPoetryActivityList() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        long clazzGroupId = getRequestLong("clazzGroupId");
        int clazzLevel = getRequestInt("clazzLevel");
        if (clazzGroupId == 0 || clazzLevel == 0) {
            return MapMessage.errorMessage("请提供正确的请求参数");
        }

        MapMessage message = ancientPoetryLoaderClient.fetchPoetryActivityList(teacher, clazzGroupId, clazzLevel);
        message.add("clazzGroupId", clazzGroupId);

        // 是否是四川老师
        boolean isSichuan = teacher.getRootRegionCode() != null && 510000 == teacher.getRootRegionCode();
        message.add("isSichuan", isSichuan);
        return message;
    }


    /**
     * 老师参加亲子古诗活动(点击立即参与)
     */
    @RequestMapping(value = "activity/view.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage viewActivity() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }

        return ancientPoetryServiceClient.viewActivity(teacher.getId());
    }


    /**
     * 报名亲子古诗活动
     */
    @RequestMapping(value = "activity/register.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage registerPoetryActivity() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail(Subject.CHINESE);
        if (teacher == null || Subject.CHINESE != teacher.getSubject()) {
            return MapMessage.errorMessage("请用语文学科老师登录");
        }
        String activityId = getRequestString("activityId");
        long clazzGroupId = getRequestLong("clazzGroupId");
        if (clazzGroupId == 0 || StringUtils.isEmpty(activityId)) {
            return MapMessage.errorMessage("请提供正确的请求参数");
        }

        return ancientPoetryServiceClient.registerPoetryActivity(teacher.getId(), activityId, clazzGroupId);
    }


    /**
     * 班级活动列表
     */
    @RequestMapping(value = "group/activity/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchGroupActivityList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long clazzGroupId = getRequestLong("clazzGroupId");
        if (user.isTeacher() && clazzGroupId == 0) {
            return MapMessage.errorMessage("clazzGroupId不能为空");
        }
        boolean needDetail = getRequestBool("needDetail");
        long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if ((needDetail || user.isParent()) && studentId == 0) {
            return MapMessage.errorMessage("studentId不允许为空");
        }
        if (!user.isTeacher()) {
            GroupMapper groupMapper = getChineseGroupMapper(studentId);
            if (groupMapper == null) {
                return MapMessage.errorMessage("当前学生不存在语文班组信息");
            }
            clazzGroupId = groupMapper.getId();
        }

        return ancientPoetryLoaderClient.fetchGroupActivityList(clazzGroupId, needDetail, studentId);
    }

    /**
     * pc活动首页（学生）
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        User user = currentUser();
        if (user != null && user.isStudent()) {
            String activityId = getRequestString("activityId");
            model.addAttribute("activityId", activityId);
            AncientPoetryActivity activity = ancientPoetryLoaderClient.findActivityById(activityId);
            if (activity != null) {
                model.addAttribute("gameUrl", UrlUtils.buildUrlQuery(NewHomeworkConstants.STUDENT_ANCIENT_POETRY_ACTIVITY_URL, MapUtils.m("activityId", activityId)));
                model.addAttribute("subject", "");
                return "studentv3/poetry/index";
            }
        }
        return "redirect:/student/index.vpage";
    }


    /**
     * 查询活动关卡列表（学生、教师、家长）
     */
    @RequestMapping(value = "activity/mission/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchActivityMissions() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        String activityId = getRequestString("activityId");
        if (StringUtils.isEmpty(activityId)) {
            return MapMessage.errorMessage("请提供正确的请求参数");
        }
        Long groupId = null;
        Long studentId = null;
        if (user.isStudent()) {
            GroupMapper groupMapper = getChineseGroupMapper(user.getId());
            if (groupMapper == null) {
                return MapMessage.errorMessage("当前学生不存在语文班组信息");
            }
            groupId = groupMapper.getId();
            studentId = user.getId();
        } else if (user.isParent()) {
            studentId = getRequestLong("studentId");
            if (studentId == 0L) {
                return MapMessage.errorMessage("studentId不能为空");
            }
        }
        return ancientPoetryLoaderClient.fetchActivityMissions(user, activityId, groupId,  studentId);
    }


    /**
     * 查询活动关卡详情（学生、教师）
     */
    @RequestMapping(value = "activity/mission/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchMissionDetail() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        String activityId = getRequestString("activityId");
        String missionId = getRequestString("missionId");
        if (StringUtils.isEmpty(activityId) || StringUtils.isEmpty(missionId)) {
            return MapMessage.errorMessage("请提供正确的请求参数");
        }

        return ancientPoetryLoaderClient.fetchMissionDetail(user, activityId, missionId);
    }


    /**
     * 查询关卡模块详情(学生、教师、家长)
     */
    @RequestMapping(value = "mission/model/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchMissionModelDetail() {
        User user = currentUser();
        boolean isFinish = getRequestBool("isFinish");
        if (user == null && !isFinish) {
            return MapMessage.errorMessage("请重新登录!");
        }
        String activityId = getRequestString("activityId");
        String missionId = getRequestString("missionId");
        ModelType modelType = ModelType.of(getRequestString("modelType"));
        if (StringUtils.isEmpty(activityId) || StringUtils.isEmpty(missionId) || modelType == null) {
            return MapMessage.errorMessage("请提供正确的请求参数");
        }

        return ancientPoetryLoaderClient.fetchMissionModelDetail(activityId, missionId, modelType);
    }


    /**
     * 查询模块完成结果
     */
    @RequestMapping(value = "mission/model/result.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchMissionModelResult() {
        User user = currentUser();
        boolean isFinish = getRequestBool("isFinish");
        if (user == null && !isFinish) {
            return MapMessage.errorMessage("请重新登录!");
        }
        String activityId = getRequestString("activityId");
        String missionId = getRequestString("missionId");
        ModelType modelType = ModelType.of(getRequestString("modelType"));
        Long studentId = getRequestLong("studentId");
        boolean isParentMission = getRequestBool("isParentMission");
        if (user != null && user.isStudent()) {
            studentId = user.getId();
        } else if (user != null && (user.isTeacher() || user.isParent()) && !isParentMission) {
            return MapMessage.successMessage().add("result", MapUtils.m("isFinished", false));
        }
        if (StringUtils.isEmpty(activityId) || StringUtils.isEmpty(missionId) || studentId == 0 || modelType == null) {
            return MapMessage.errorMessage("请提供正确的请求参数");
        }

        return ancientPoetryLoaderClient.fetchMissionModelResult(activityId, missionId, studentId, modelType, isParentMission, getCdnBaseUrlAvatarWithSep());
    }


    /**
     * 关卡完成结果页接口
     */
    @RequestMapping(value = "mission/result.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchMissionResult() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号重新登录");
        }
        String activityId = getRequestString("activityId");
        String missionId = getRequestString("missionId");
        if (StringUtils.isEmpty(activityId) || StringUtils.isEmpty(missionId)) {
            return MapMessage.errorMessage("请提供正确的请求参数");
        }

        return ancientPoetryLoaderClient.fetchMissionResult(activityId, missionId, user.getId());
    }


    @RequestMapping(value = "do.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage doIt(HttpServletRequest request) {
        User user = currentUser();
        boolean isFinish = getRequestBool("isFinish");
        // 已完成则不校验是否登录(用于分享)
        if (user == null && !isFinish) {
            return MapMessage.errorMessage("请重新登录!");
        }

        String activityId = getRequestString("activityId");
        String missionId = getRequestString("missionId");
        ModelType modelType = ModelType.of(getRequestString("modelType"));
        boolean isParentMission = getRequestBool("isParentMission");
        long studentId = getRequestLong("studentId"); // 家长通传studentId

        AncientPoetryActivity activity = ancientPoetryLoaderClient.findActivityById(activityId);
        if (activity == null || activity.getDisabled()) {
            return MapMessage.errorMessage("活动不存在或者已下线").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED);
        }
        FlashVars vars = new FlashVars(request);
        vars.add("activityId", activityId);
        vars.add("missionId", missionId);
        vars.add("modelType", modelType);
        vars.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        vars.add("processResultUrl", "/ancient/poetry/processresult" + Constants.AntiHijackExt);
        // 订正错题
        if (getRequestBool("correct")) {
            vars.add("questionUrl", UrlUtils.buildUrlQuery("/ancient/poetry/correct/questions" + Constants.AntiHijackExt, MapUtils.m("activityId", activityId, "isParentMission", isParentMission)));
            Map<String, Object> params = MapUtils.m("activityId", activityId, "isParentMission", isParentMission);
            if (studentId != 0) {
                params.put("studentId", studentId);
            }
            vars.add("completedUrl", UrlUtils.buildUrlQuery("/ancient/poetry/correct/questions/answer" + Constants.AntiHijackExt, params));
        } else {
            vars.add("modelDetailUrl", UrlUtils.buildUrlQuery("/ancient/poetry/mission/model/detail" + Constants.AntiHijackExt, MapUtils.m("activityId", activityId, "missionId", missionId, "modelType", modelType, "isFinish", isFinish, "isParentMission", isParentMission)));
            Map<String, Object> params = MapUtils.m("activityId", activityId, "missionId", missionId, "modelType", modelType, "isFinish", isFinish, "isParentMission", isParentMission);
            if (studentId != 0) {
                params.put("studentId", studentId);
            }
            vars.add("completedUrl", UrlUtils.buildUrlQuery("/ancient/poetry/mission/model/result" + Constants.AntiHijackExt, params));
        }
        return MapMessage.successMessage().add("flashVars", vars);
    }


    /**
     * 提交结果
     */
    @RequestMapping(value = "processresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage processResult() {
        User user = currentUser();
        if (user == null || user.isTeacher()) {
            return MapMessage.errorMessage("请登录正确角色账号!");
        }

        SaveAncientPoetryResultRequest request = getRequestObject(SaveAncientPoetryResultRequest.class);
        if (request == null || StringUtils.isBlank(request.getActivityId()) || request.getMissionId() == null || ModelType.of(request.getModelType()) == null) {
            return MapMessage.errorMessage("提交结果数据异常" + JsonUtils.toJson(request));
        }
        ModelType modelType = ModelType.of(request.getModelType());
        if (Objects.equals(modelType, ModelType.FUN) && (request.getQuestionId() == null || CollectionUtils.isEmpty(request.getAnswer()) || CollectionUtils.isEmpty(request.getAnswer().get(0)) || request.getDuration() == null)) {
            return MapMessage.errorMessage("答题数据异常" + JsonUtils.toJson(request));
        }
        if (Objects.equals(modelType, ModelType.RECITE) && CollectionUtils.isEmpty(request.getStudentAudioUrls())) {
            return MapMessage.errorMessage("参数studentAudioUrls不能为空");
        }
        if (user.isParent() && (request.getStudentId() == null || (request.isParentMission() && Objects.equals(modelType, ModelType.RECITE) && CollectionUtils.isEmpty(request.getParentAudioUrls())))) {
            return MapMessage.errorMessage("studentId或parentAudioUrls不能为空");
        }

        AncientPoetryProcessContext context = new AncientPoetryProcessContext();
        context.setStudentId(request.getStudentId());
        if (user.isStudent()) {
            context.setStudentId(user.getId());
        }
        if (user.isParent()) {
            context.setParentId(user.getId());
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getStudentId());
        if (studentDetail != null) {
            context.setRegionId(studentDetail.getStudentSchoolRegionCode());
            context.setSchoolId(studentDetail.getClazz() == null ? null : studentDetail.getClazz().getSchoolId());
            context.setClazzLevel(studentDetail.getClazzLevelAsInteger());
        }
        context.setActivityId(request.getActivityId());
        context.setMissionId(request.getMissionId());
        context.setModelType(modelType);
        context.setParentMission(request.isParentMission());
        context.setCorrect(request.isCorrect());
        context.setClientType(request.getClientType());
        context.setClientName(request.getClientName());
        context.setUserAgent(getRequest().getHeader("User-Agent"));
        context.setQuestionId(request.getQuestionId());
        context.setAnswer(request.getAnswer());
        context.setDurationMilliseconds(NewHomeworkUtils.processDuration(request.getDuration()));
        context.setStudentAudioUrls(request.getStudentAudioUrls());
        context.setParentAudioUrls(request.getParentAudioUrls());
        try {
            return ancientPoetryServiceClient.processResult(context);
        } catch (Exception ex) {
            logger.error("Failed to save user {} ancient poetry request", user.getId(), ex);
            return MapMessage.errorMessage("提交结果数据异常");
        }
    }


    /**
     * 家长学生列表(用于切换学生)
     */
    @RequestMapping(value = "parent/students.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadParentStudents() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return MapMessage.errorMessage("请用家长账号重新登录");
        }
        List<Map<String, Object>> results = new ArrayList<>();
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(Collections.singleton(user.getId())).get(user.getId());
        if (CollectionUtils.isNotEmpty(studentParentRefs)) {
            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(Lists.transform(studentParentRefs, StudentParentRef::getStudentId));
            for (StudentParentRef studentParentRef : studentParentRefs) {
                StudentDetail studentDetail = studentDetailMap.get(studentParentRef.getStudentId());
                if (studentDetail != null) {
                    results.add(MapUtils.m("studentId", studentDetail.getId(),
                            "studentName", studentDetail.fetchRealnameIfBlankId(),
                            "callName", studentParentRef.getCallName(),
                            "imageUrl", NewHomeworkUtils.getUserAvatarImgUrl(getCdnBaseUrlAvatarWithSep(), studentDetail.fetchImageUrl())));
                }
            }
        }

        return MapMessage.successMessage().add("result", results);
    }


    /**
     * 班级排行榜（学生、教师、家长）
     */
    @RequestMapping(value = "clazz/ranking.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage clazzRankingList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        String activityId = getRequestString("activityId");
        long clazzGroupId = getRequestLong("clazzGroupId");
        long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");

        if (user.isStudent() || user.isParent()) {
            if (studentId == 0) {
                return MapMessage.errorMessage("studentId不允许为空");
            }
            GroupMapper groupMapper = getChineseGroupMapper(studentId);
            if (groupMapper == null) {
                return MapMessage.errorMessage("当前学生不存在语文班组信息");
            }
            clazzGroupId = groupMapper.getId();
        }
        if (clazzGroupId == 0) {
            return MapMessage.errorMessage("clazzGroupId不允许为空");
        }

        return ancientPoetryLoaderClient.clazzRankingList(user, clazzGroupId, activityId);
    }


    /**
     * 总榜（学生、教师、家长）
     */
    @RequestMapping(value = "global/ranking.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage globalRankingList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        int clazzLevel = getRequestInt("clazzLevel");
        String regionLevel = getRequestString("regionLevel");// PROVINCE, COUNTY, SCHOOL
        if (clazzLevel == 0 || StringUtils.isEmpty(regionLevel)) {
            return MapMessage.errorMessage("请提供正确的请求参数");
        }
        Integer provinceId;
        Integer regionCode;
        Long schoolId;
        long studentId = 0L;
        if (user.isStudent()) {
            StudentDetail studentDetail = (StudentDetail) user;
            provinceId = studentDetail.getRootRegionCode();
            regionCode = studentDetail.getStudentSchoolRegionCode();
            schoolId = studentDetail.getClazz() == null ? null : studentDetail.getClazz().getSchoolId();
            studentId = studentDetail.getId();
        } else if (user.isTeacher()) {
            TeacherDetail teacherDetail = (TeacherDetail) user;
            provinceId = teacherDetail.getRootRegionCode();
            regionCode = teacherDetail.getRegionCode();
            schoolId = teacherDetail.getTeacherSchoolId();
        } else {
            studentId = getRequestLong("studentId");
            if (studentId == 0) {
                return MapMessage.errorMessage("studentId不能为空");
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            provinceId = studentDetail.getRootRegionCode();
            regionCode = studentDetail.getStudentSchoolRegionCode();
            schoolId = studentDetail.getClazz() == null ? null : studentDetail.getClazz().getSchoolId();
        }

        return ancientPoetryLoaderClient.globalRankingList(provinceId, regionCode, schoolId, clazzLevel, regionLevel, getCdnBaseUrlAvatarWithSep(), studentId);
    }

    /**
     * 家长查询孩子已参加活动列表
     */
    @RequestMapping(value = "parent/activity/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage parentChildActivityList() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return MapMessage.errorMessage("请用家长账号重新登录");
        }
        long studentId = getRequestLong("studentId");
        if (studentId == 0) {
            return MapMessage.successMessage().add("result", Collections.emptyList()).add("totalStar", 0).add("studentBegin", false);
        }
        GroupMapper groupMapper = getChineseGroupMapper(studentId);
        if (groupMapper == null) {
            return MapMessage.successMessage().add("result", Collections.emptyList()).add("totalStar", 0).add("studentBegin", false);
        }

        return ancientPoetryLoaderClient.parentChildActivityList(studentId, groupMapper.getId());
    }

    /**
     * 发送家长端Push消息
     */
    @RequestMapping(value = "jzt/push.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage jztPushMessage() {
        User user = currentUser();
        if (user == null || !user.isStudent()) {
            return MapMessage.errorMessage("请用学生账号登录");
        }
        String messageType = getRequestString("messageType");
        String activityId = getRequestString("activityId");
        if (StringUtils.isEmpty(activityId)) {
            return MapMessage.errorMessage("请求参数错误");
        }
        if (StringUtils.isEmpty(messageType)) {
            return MapMessage.errorMessage("消息类型错误");
        }

        // 当天超过5次不再发送push
        if (validateAccessFreqDays(user.getId(), "ANCIENT_POETRY_JZT_PUSH_MESSAGE",  5)) {
            return MapMessage.successMessage("今天家长通push消息已超过5次");
        }

        Map<Long, List<StudentParent>> parentsMap = parentLoaderClient.loadStudentParents(Collections.singletonList(user.getId()));
        List<Long> parentIds = parentsMap.getOrDefault(user.getId(), Collections.emptyList())
                .stream()
                .map(p -> p.getParentUser().getId())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(parentIds)) {
            Map<String, Object> urlMap = MapUtils.m("activity_id", activityId, "student_id_source", user.getId());
            String messageContent = null;
            switch (messageType) {
                case "PARENT_CHILD_ACTIVITY":
                    messageContent = "您的孩子正在参加亲子诗词大会，和孩子一起朗读古诗，完成亲子诵读吧。";
                    urlMap.put("goPage", "parent");
                    break;
                case "CORRECT_WRONG":
                    messageContent = "您的孩子正在参加亲子诗词大会，协助孩子一起订正错题吧。";
                    urlMap.put("goPage", "correct");
                    urlMap.put("renderType", "input"); // 前端用于判断跳转订正错题还是巩固错题
                    urlMap.put("flashvarsUrl", UrlUtils.buildUrlQuery("/ancient/poetry/do" + Constants.AntiHijackExt, MapUtils.m("activityId", activityId, "correct", true)));
                    break;
                default:
            }

            Map<String, Object> extras = new HashMap<>();
            extras.put("studentId", user.getId());
            extras.put("tag", ParentMessageTag.通知.name());
            extras.put("url", UrlUtils.buildUrlQuery("/view/mobile/parent/poetry", urlMap));
            extras.put("s", ParentAppPushType.NOTICE.name());
            if (messageContent != null) {
                appMessageServiceClient.sendAppJpushMessageByIds(messageContent, AppMessageSource.PARENT, parentIds, extras);
            }
        }

        return MapMessage.successMessage();
    }

    /**
     * 学生年级信息
     */
    @RequestMapping(value = "student/class.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage studentClazzLevel() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if (studentId == 0) {
            return MapMessage.errorMessage("studentId不能为空");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        ClazzLevel clazzLevel = studentDetail.getClazzLevel();

        return MapMessage.successMessage().add("clazzLevel", clazzLevel.getLevel());
    }

    /**
     * 订正题目信息
     */
    @RequestMapping(value = "correct/questions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage correctQuestionList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (!user.isParent()) {
            return MapMessage.errorMessage("只支持家长端订正");
        }
        long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            return MapMessage.errorMessage("学生id错误");
        }
        String activityId = getRequestString("activityId");
        if (StringUtils.isEmpty(activityId)) {
            return MapMessage.errorMessage("活动id错误");
        }
        return ancientPoetryLoaderClient.loadCorrectQuestions(activityId, studentId);
    }

    /**
     * 订正答案信息
     */
    @RequestMapping(value = "correct/questions/answer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage correctQuestionAnswer() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (!user.isParent()) {
            return MapMessage.errorMessage("只支持家长端订正");
        }
        long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            return MapMessage.errorMessage("学生id错误");
        }
        String activityId = getRequestString("activityId");
        if (StringUtils.isEmpty(activityId)) {
            return MapMessage.errorMessage("活动id错误");
        }
        return ancientPoetryLoaderClient.loadCorrectQuestionsAnswer(activityId, studentId);
    }

    @Nullable
    private GroupMapper getChineseGroupMapper(Long studentId) {
        return groupLoaderClient.loadStudentGroups(studentId, false)
                .stream()
                .filter(group -> group.getSubject().equals(Subject.CHINESE))
                .findFirst()
                .orElse(null);
    }

}
