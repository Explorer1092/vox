package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.galaxy.service.studyplanning.cache.StudyPlanningCacheManager;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.FlowerSourceType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.business.api.constant.AppUseNumCalculateType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.consumer.BusinessVendorServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.flower.api.FlowerConditionService;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.newhomework.api.AncientPoetryLoader;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkPartLoader;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkResultLoader;
import com.voxlearning.utopia.service.newhomework.api.VoiceRecommendLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.DubbingRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.StudentActivityStatistic;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.IntelligentTeachingReport;
import com.voxlearning.utopia.service.newhomework.api.service.DiagnoseReportService;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkReportService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.DubbingScoreRecommendLoaderClient;
import com.voxlearning.utopia.service.parent.api.DPGroupMessageConfirmInfoLoader;
import com.voxlearning.utopia.service.parent.api.DPGroupMessageConfirmInfoService;
import com.voxlearning.utopia.service.parent.api.DPScoreCircleLoader;
import com.voxlearning.utopia.service.parent.api.DPScoreCircleService;
import com.voxlearning.utopia.service.parent.api.entity.GroupMessageConfirmInfo;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.CircleContext;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContext;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContextStoreInfo;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleResponse;
import com.voxlearning.utopia.service.parent.constant.*;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardLog;
import com.voxlearning.utopia.service.parentreward.api.mapper.ParentRewardSendResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNoticeType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtFeedBack;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNotice;
import com.voxlearning.utopia.service.vendor.api.entity.OfflineHomeworkSignRecord;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.v1.util.ParentHomeworkUtil;
import com.voxlearning.washington.mapper.NewEaseMobBottomMenuConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.NOT_SHOW_SCORE_TYPE;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2018-3-9
 */
@Controller
@Slf4j
@RequestMapping(value = "/v2/parent/group_message/")
public class ParentGroupMessageApiController extends AbstractParentApiController {

    //学科Icon
    private static final Map<Subject, String> subjectIcon = new HashMap<>();
    private static final List<Subject> SUBJECT_LIST;
    private static final Map<Subject, String> SUBJECT_PRODUCT_MAP;
    private static final Map<AppUseNumCalculateType, String> CALCULATE_TYPE_MAP;
    private static final Map<Subject, String> SUBJECT_ICON_MAP;
    private static final List<GroupCircleType> GROUP_CIRCLE_TYPES;

    static {
        subjectIcon.put(Subject.ENGLISH, "/public/skin/parentMobile/images/group_circle/english.png");
        subjectIcon.put(Subject.MATH, "/public/skin/parentMobile/images/group_circle/math.png");
        subjectIcon.put(Subject.CHINESE, "/public/skin/parentMobile/images/group_circle/chinese.png");

        SUBJECT_LIST = new ArrayList<>();
        SUBJECT_LIST.add(Subject.ENGLISH);
        SUBJECT_LIST.add(Subject.MATH);
        SUBJECT_LIST.add(Subject.CHINESE);

        SUBJECT_PRODUCT_MAP = new HashMap<>();
        SUBJECT_PRODUCT_MAP.put(Subject.ENGLISH, OrderProductServiceType.AfentiExam.name());
        SUBJECT_PRODUCT_MAP.put(Subject.MATH, OrderProductServiceType.AfentiMath.name());
        SUBJECT_PRODUCT_MAP.put(Subject.CHINESE, OrderProductServiceType.AfentiChinese.name());

        CALCULATE_TYPE_MAP = new HashMap<>();
        CALCULATE_TYPE_MAP.put(AppUseNumCalculateType.CLAZZ, "同班");
        CALCULATE_TYPE_MAP.put(AppUseNumCalculateType.GRADE, "同年级");
        CALCULATE_TYPE_MAP.put(AppUseNumCalculateType.SCHOOL, "同校");
        CALCULATE_TYPE_MAP.put(AppUseNumCalculateType.NATION, "");

        SUBJECT_ICON_MAP = new HashMap<>();
        SUBJECT_ICON_MAP.put(Subject.ENGLISH, "/public/skin/parentMobile/images/report_english.png");
        SUBJECT_ICON_MAP.put(Subject.MATH, "/public/skin/parentMobile/images/report_math.png");
        SUBJECT_ICON_MAP.put(Subject.CHINESE, "/public/skin/parentMobile/images/report_chinese.png");

        GROUP_CIRCLE_TYPES = new ArrayList<>();
        GROUP_CIRCLE_TYPES.add(GroupCircleType.HOMEWORK_NEW);
        GROUP_CIRCLE_TYPES.add(GroupCircleType.HOMEWORK_CHECK);
        GROUP_CIRCLE_TYPES.add(GroupCircleType.OFFLINE_HOMEWORK);
        GROUP_CIRCLE_TYPES.add(GroupCircleType.HOMEWORK_REPORT);
        GROUP_CIRCLE_TYPES.add(GroupCircleType.HOMEWORK_WEEK_REPORT);
        GROUP_CIRCLE_TYPES.add(GroupCircleType.JXT_NOTICE);
        GROUP_CIRCLE_TYPES.add(GroupCircleType.COMMON);
    }

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject
    private GroupLoaderClient groupLoaderClient;

    @Inject
    private BusinessVendorServiceClient businessVendorServiceClient;
    @ImportService(interfaceClass = DPScoreCircleLoader.class)
    private DPScoreCircleLoader dpScoreCircleLoader;
    @ImportService(interfaceClass = DPScoreCircleService.class)
    private DPScoreCircleService dpScoreCircleService;

    @ImportService(interfaceClass = DPGroupMessageConfirmInfoLoader.class)
    private DPGroupMessageConfirmInfoLoader dpConfirmInfoLoader;
    @ImportService(interfaceClass = DPGroupMessageConfirmInfoService.class)
    private DPGroupMessageConfirmInfoService dpGroupMessageConfirmInfoService;
    @Inject
    private FlowerServiceClient flowerServiceClient;
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @ImportService(interfaceClass = NewHomeworkResultLoader.class)
    private NewHomeworkResultLoader newHomeworkResultLoader;
    @ImportService(interfaceClass = NewHomeworkPartLoader.class)
    private NewHomeworkPartLoader newHomeworkPartLoader;
    @ImportService(interfaceClass = FlowerConditionService.class)
    private FlowerConditionService flowerConditionService;
    @ImportService(interfaceClass = NewHomeworkReportService.class)
    private NewHomeworkReportService newHomeworkReportService;
    @ImportService(interfaceClass = VoiceRecommendLoader.class)
    private VoiceRecommendLoader voiceRecommendLoader;
    @ImportService(interfaceClass = DiagnoseReportService.class)
    private DiagnoseReportService diagnoseReportService;
    @ImportService(interfaceClass = AncientPoetryLoader.class)
    private AncientPoetryLoader ancientPoetryLoader;
    @Inject
    private DubbingScoreRecommendLoaderClient dubbingScoreRecommendLoaderClient;

    private String newEaseMobBottomMenuKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Mode current = RuntimeMode.current();
        if (current == Mode.DEVELOPMENT)
            current = Mode.TEST;
        newEaseMobBottomMenuKey = "newEaseMobBottomMenu_" + current.name();
    }

    /**
     * 本地测试用
     */
    @RequestMapping(value = "message/top/generate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage generateTopMessage() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        dpScoreCircleService.generateTopMessage(GroupCircleTopMessageType.ASSIGN_PARENT_HOMEWORK, studentId);
        return successMessage();
    }

    @RequestMapping(value = "button/request.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage buttonRequest() {
        String requestType = getRequestString("request_type");
        String requestParams = getRequestString("request_params");
        Map<String, Object> paramMap = JsonUtils.fromJson(requestParams);
        if (MapUtils.isEmpty(paramMap)) {
            return failMessage("请求参数错误");
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_RELOGIN);
        }
        if (RequestType.PARENT_REWARD.name().equals(requestType)) {
            Long studentId = getRequestLong(REQ_STUDENT_ID);
            String id = SafeConverter.toString(paramMap.get("id"));
            String key = SafeConverter.toString(paramMap.get("key"));
            String type = SafeConverter.toString(paramMap.get("type"));
            Integer count = SafeConverter.toInt(paramMap.get("count"));
            try {
                if (!parentRewardService.rewardSendAvailable(parent.getId(), studentId,isParentRewardNewVersionForFaceDetect(getClientVersion()))) {
                    return failMessage("现在不可发放奖励");
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
                            .sendParentRewards(getCurrentParentId(), studentId, Collections.singletonList(log));
                } catch (DuplicatedOperationException ex) {
                    return failMessage("当前奖励正在被发放，请稍候刷新页面重试");
                }
                if (null == sendResult) {
                    return failMessage("奖励发放失败");
                }
            } catch (Exception ex) {
                log.error("{}, sid:{}, pid:{}, params:{}", ex.getMessage(), studentId, getCurrentParentId(), requestParams, ex);
                return failMessage("发放奖励失败");
            }
        } else if (RequestType.FLOWER.name().equals(requestType)) {
            Long studentId = getRequestLong(REQ_STUDENT_ID);
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                return failMessage("学生信息错误");
            }
            if (studentDetail.getClazz() == null) {
                return failMessage("该学生当前无班级，不能点赞");
            }
            String homeworkId = SafeConverter.toString(paramMap.get("homework_id"));
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (newHomework == null) {
                return failMessage("要点赞的作业不存在");
            }
            try {
                long count = flowerServiceClient.getFlowerService().loadHomeworkFlowers(homeworkId).getUninterruptibly()
                        .stream()
                        .filter(f -> Objects.equals(studentId, f.getSenderId()) && FlowerSourceType.HOMEWORK.name().equals(f.getSourceType()))
                        .count();
                if (count > 0) {
                    return failMessage("不能重复点赞");
                }
                Long teacherId = newHomework.getTeacherId();
                Long clazzId = studentDetail.getClazzId();
                Long groupId = newHomework.getClazzGroupId();
                MapMessage mapMessage = flowerConditionService.sendFlower(studentId, parent.getId(), teacherId, clazzId, groupId, FlowerSourceType.HOMEWORK, homeworkId)
                        .getUninterruptibly();
                if (mapMessage.isSuccess()) {
                    return successMessage();
                } else {
                    return failMessage(mapMessage.getInfo());
                }
            } catch (Exception ex) {
                logger.error("send flower error. sid:{}, params:{}", studentId, requestParams, ex);
                return failMessage("点赞失败");
            }
        } else if (RequestType.READ.name().equals(requestType)) {
            String typeId = SafeConverter.toString(paramMap.get("type_id"));
            String groupCircleType = SafeConverter.toString(paramMap.get("group_circle_type"));
            GroupCircleType type = GroupCircleType.parse(groupCircleType);
            if (type == null) {
                return failMessage("业务类型错误");
            }
            dpGroupMessageConfirmInfoService.confirm(parent.getId(), type, typeId, new Date());
        }
        return successMessage();
    }

    @RequestMapping(value = "message_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getMessageList() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID, REQ_CREATE_TIME);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long createTime = getRequestLong(REQ_CREATE_TIME);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("获取孩子信息错误");
        }

        Date createDate = createTime == 0 ? new Date() : new Date(createTime - 1);
        Collection<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false)
                .stream()
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(groupIds)) {
            return successMessage().add("message_list", Collections.EMPTY_LIST);
        }

        generateAssignParentHomeworkMessage(studentId, groupIds);
        List<ScoreCircleGroupContext> returnContextList = new ArrayList<>();
        if (createTime == 0) {
            List<ScoreCircleGroupContext> topContextList = loadTopMessageContextList(studentId, groupIds);
            Comparator<ScoreCircleGroupContext> comparator = Comparator.comparing(ScoreCircleGroupContext::getOrder);
            comparator = comparator.thenComparing((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()));
            topContextList = topContextList.stream().sorted(comparator).collect(Collectors.toList());
            returnContextList.addAll(topContextList);
        }
        //获取数据
        Set<ScoreCircleGroupContext> contextSet = loadContext(studentId, groupIds, createDate, true, true);

        //排序并且取前10条数据
        //与最后一条数据时间相等的数据必须同时返回

        contextSet.stream()
                .sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()))
                .forEach(p -> {
                    if (returnContextList.size() < 10) {
                        returnContextList.add(p);
                    } else if (returnContextList.get(returnContextList.size() - 1).getCreateDate().equals(p.getCreateDate())) {
                        returnContextList.add(p);
                    }
                });
        if (CollectionUtils.isEmpty(returnContextList)) {
            return successMessage().add("message_list", Collections.EMPTY_LIST);
        }

        //获取学科Id
        Set<Long> returnGroupIds = returnContextList
                .stream()
                .map(ScoreCircleGroupContext::getGroupId)
                .collect(Collectors.toSet());
        //老师信息
        Map<Long, List<Teacher>> groupTeacher = teacherLoaderClient.loadGroupTeacher(returnGroupIds);

        Map<Long, Group> groupMap = groupLoaderClient.getGroupLoader()
                .loadGroups(returnGroupIds)
                .getUninterruptibly();

        //读取存储中的信息
        Set<String> ids = new HashSet<>();
        returnContextList.forEach(context -> ids.add(context.generateId()));
        Map<String, ScoreCircleGroupContextStoreInfo> storeInfoMap = dpScoreCircleLoader.loads(ids);

        List<Map<String, Object>> messageList = generateGroupMessageList(returnContextList, storeInfoMap, groupMap, groupTeacher, studentDetail);
        return successMessage().add("message_list", messageList);
    }

    @RequestMapping(value = "list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getGroupMessageList() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID, REQ_CARD_GROUP_NAME, REQ_CREATE_TIME);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String groupName = getRequestString(REQ_CARD_GROUP_NAME);
        Long createTime = getRequestLong(REQ_CREATE_TIME);
        //与上一次最后一条数据时间相等的数据排开。所以要-1
        Date createDate = createTime == 0 ? new Date() : new Date(createTime - 1);

        Collection<Long> groupIds = getGroupIds(studentId, groupName);
        if (CollectionUtils.isEmpty(groupIds)) {
            return successMessage().add(RES_CARD_LIST, Collections.EMPTY_LIST);
        }
        //忽略学科属性。需要通用消息和班级消息中不考虑学科的消息
        //这里忽略学科就变成了代表"全部"这个tab了。个人维度的消息也直接用这个字段判断了
        boolean withoutSubject = StringUtils.isBlank(groupName);

        //获取数据
        Set<ScoreCircleGroupContext> contextSet = loadContext(studentId, groupIds, createDate, withoutSubject, false);

        if (CollectionUtils.isEmpty(contextSet)) {
            return successMessage().add(RES_CARD_LIST, Collections.EMPTY_LIST);
        }

        //排序并且取前10条数据
        //与最后一条数据时间相等的数据必须同时返回
        List<ScoreCircleGroupContext> returnContextList = new ArrayList<>();
        contextSet.stream()
                .sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()))
                .forEach(p -> {
                    if (returnContextList.size() < 10) {
                        returnContextList.add(p);
                    } else if (returnContextList.get(returnContextList.size() - 1).getCreateDate().equals(p.getCreateDate())) {
                        returnContextList.add(p);
                    }
                });
        //获取学科Id
        Set<Long> returnGroupIds = returnContextList
                .stream()
                .map(ScoreCircleGroupContext::getGroupId)
                .collect(Collectors.toSet());

        //学科属性
        Map<Long, Group> groupMap = groupLoaderClient.getGroupLoader()
                .loadGroupsIncludeDisabled(returnGroupIds)
                .getUninterruptibly();
        //老师信息
        Map<Long, List<Teacher>> groupTeacher = teacherLoaderClient.loadGroupTeacher(returnGroupIds);

        //读取存储中的信息
        Set<String> ids = new HashSet<>();
        returnContextList.forEach(context -> ids.add(context.generateId()));
        Map<String, ScoreCircleGroupContextStoreInfo> storeInfoMap = dpScoreCircleLoader.loads(ids);


        //日期标签
        String lastTime = null;
        if (createTime > 0) {
            lastTime = DateUtils.dateToString(new Date(createTime), "MM月dd日");
        }


        //转换结果
        List<Map<String, Object>> contextResultList
                = convertResult(returnContextList, storeInfoMap, groupMap, groupTeacher, studentId, lastTime);

        //返回结果
        return successMessage().add(RES_CARD_LIST, contextResultList);
    }

    @RequestMapping(value = "read.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage readGroupMessage() {

        try {
            validateRequired(REQ_CARD_TYPE_ID, "业务id");
            validateRequired(REQ_CARD_TYPE_NAME, "业务类型");
            validateRequest(REQ_CARD_TYPE_ID, REQ_CARD_TYPE_NAME);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        //处理用户id
        Long userId = getApiRequestUser().getId();

        //处理业务id
        String typeId = getRequestString(REQ_CARD_TYPE_ID);

        //处理业务类型
        String typeStr = getRequestString(REQ_CARD_TYPE_NAME);
        GroupCircleType type = GroupCircleType.parse(typeStr);
        if (type == null) {
            return failMessage("业务类型错误");
        }
        if (type.isNeedBusinessConfirm()) {
            return successMessage();
        }

        dpGroupMessageConfirmInfoService.confirm(userId, type, typeId, new Date());
        ParentGroupMessageStatus messageStatus = ParentGroupMessageStatus.parse(type.getConfirmStatus());
        return successMessage().add(RES_CARD_TOP_TAG, messageStatus.getTag())
                .add(RES_CARD_TOP_TAG_COLOR, messageStatus.getColor());
    }

    @RequestMapping(value = "reload_card.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reloadMessageCard() {
        String typeId = getRequestString(REQ_CARD_TYPE_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String typeName = getRequestString("card_type");
        Long groupId = getRequestLong("group_id");
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_RELOGIN);
        }
        MapMessage mapMessage = successMessage();
        GroupCircleType circleType = GroupCircleType.parse(typeName);
        if (circleType == null) {
            return failMessage("业务类型错误");
        }
        if (circleType == GroupCircleType.HOMEWORK_CHECK) {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(typeId);
            if (newHomework != null) {
                setParentRewardInfo(mapMessage, newHomework, parent.getId(), studentId);
            }
            GroupMessageConfirmInfo confirmInfo = dpConfirmInfoLoader.load(circleType, typeId);
            //确认按钮
            ParentGroupMessageStatus status = getMessageStatus(circleType, confirmInfo, parent.getId(), studentId);
            if (status != null) {
                mapMessage.put("button_text", status.getTag());
                if (ParentGroupMessageStatus.unConfirmStatus().contains(status)) {
                    mapMessage.put("button_style", 0);
                    Map<String, Object> requestParams = new HashMap<>();
                    requestParams.put("type_id", typeId);
                    requestParams.put("group_circle_type", circleType);
                    mapMessage.put("request_params", JsonUtils.toJson(requestParams));
                    mapMessage.put("request_type", RequestType.READ);
                } else {

                    mapMessage.put("button_style", 1);
                }
            }
        } else if (circleType == GroupCircleType.HOMEWORK_NEW) {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(typeId);
            if (newHomework != null) {
                Flower flower = flowerServiceClient.getFlowerService().loadHomeworkFlowers(typeId).getUninterruptibly()
                        .stream()
                        .filter(f -> Objects.equals(studentId, f.getSenderId()) && FlowerSourceType.HOMEWORK.name().equals(f.getSourceType()))
                        .findFirst()
                        .orElse(null);
                if (flower == null) {
                    mapMessage.put("button_text", "给老师点赞");
                    mapMessage.put("can_request", true);
                    mapMessage.put("button_style", 0);
                    Map<String, Object> requestParams = new HashMap<>();
                    requestParams.put("homework_id", typeId);
                    mapMessage.put("request_params", JsonUtils.toJson(requestParams));
                    mapMessage.put("request_type", RequestType.FLOWER);
                } else {
                    mapMessage.put("can_request", false);
                    mapMessage.put("button_text", "已点赞");
                    String flowerDetailUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/send_flower/index.vpage?group_id=" + newHomework.getClazzGroupId();
//                    mapMessage.put("button_url", flowerDetailUrl);
                    mapMessage.put("button_style", 1);
                }
            }
        } else if (circleType == GroupCircleType.OFFLINE_HOMEWORK) {
            OfflineHomework offlineHomework = offlineHomeworkLoaderClient.loadOfflineHomework(typeId);
            if (offlineHomework != null && offlineHomework.getNeedSign()) {
                Map<String, List<OfflineHomeworkSignRecord>> offlineHomeworkSignMap = jxtLoaderClient.getSignRecordByOfflineHomeworkIds(Collections.singleton(typeId));
                List<OfflineHomeworkSignRecord> homeworkSignRecords = offlineHomeworkSignMap.containsKey(typeId) ? offlineHomeworkSignMap.get(typeId) : new ArrayList<>();
                if (CollectionUtils.isEmpty(homeworkSignRecords) || homeworkSignRecords.stream().noneMatch(p -> Objects.equals(p.getParentId(), parent.getId()) && Objects.equals(p.getStudentId(), studentId))) {
                    mapMessage.put("button_style", 0);
                    mapMessage.put("button_text", "去签字");
                    mapMessage.put("button_url", ProductConfig.getMainSiteBaseUrl() + "/view/offlinehomework/detail?needTitle=true&ohids=" + offlineHomework.getId());
                } else {
                    mapMessage.put("button_text", "已签字");
                    mapMessage.put("button_style", 1);
                }
            }
        } else if (circleType == GroupCircleType.JXT_NOTICE) {
            JxtNotice jxtNotice = jxtLoaderClient.getRemoteReference().getJxtNoticeById(typeId);
            if (jxtNotice != null && jxtNotice.getNeedFeedBack()) {
                Map<String, List<JxtFeedBack>> feedBackListByNoticeIds = jxtLoaderClient.getFeedBackListByNoticeIds(Collections.singleton(jxtNotice.getId()));
                List<JxtFeedBack> feedBackList = feedBackListByNoticeIds.containsKey(jxtNotice.getId()) ? feedBackListByNoticeIds.get(jxtNotice.getId()) : new ArrayList<>();
                List<JxtFeedBack> noticeFeedBackList = feedBackList.stream().filter(p -> p.getGroupId().equals(groupId)).sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
                Set<Long> hadFeedBackParentIds = noticeFeedBackList.stream().map(JxtFeedBack::getParentId).collect(Collectors.toSet());
                if (hadFeedBackParentIds.contains(parent.getId())) {
                    mapMessage.put("button_style", 1);
                    mapMessage.put("button_text", "已确认");
                } else {
                    mapMessage.put("button_text", "确认查收");
                }
                mapMessage.put("button_url", "/view/mobile/common/notice_detail?user_type=parent&notice_id=" + typeId + "&group_id=" + groupId);
            }
        }
        return mapMessage;
    }

    @RequestMapping(value = "reload_status.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reloadMessageStatus() {
        try {
            validateRequired(REQ_CARD_TYPE_ID, "业务id");
            validateRequired(REQ_CARD_TYPE_NAME, "业务类型");
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_CARD_TYPE_ID, REQ_CARD_TYPE_NAME, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        //处理业务id
        String typeId = getRequestString(REQ_CARD_TYPE_ID);
        //处理业务类型
        String typeStr = getRequestString(REQ_CARD_TYPE_NAME);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long parentId = getCurrentParentId();
        GroupCircleType circleType = GroupCircleType.parse(typeStr);
        if (circleType == null) {
            return failMessage("业务类型错误");
        }
        GroupMessageConfirmInfo confirmInfo = dpConfirmInfoLoader.load(circleType, typeId);
        ParentGroupMessageStatus messageStatus = getMessageStatus(circleType, confirmInfo, parentId, studentId);
        if (messageStatus == ParentGroupMessageStatus.UNKNOWN) {
            return failMessage("消息状态错误");
        }
        String topTag = messageStatus.getTag();
        String color = messageStatus.getColor();
        if (circleType == GroupCircleType.HOMEWORK_CHECK) {
            topTag = "已确认";
            color = "#9EA5B8";
        }
        return successMessage().add(RES_CARD_TOP_TAG, topTag)
                .add(RES_CARD_TOP_TAG_COLOR, color);
    }

    /**
     * 获取学生班群
     */
    @RequestMapping(value = "/clazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getStudentClazzGroup() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long noticeTime = getRequestLong(REQ_LATEST_NOTICE_TIME);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID, REQ_LATEST_NOTICE_TIME);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_RELOGIN);
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }
        Date queryDate;
        if (noticeTime == 0L) {
            queryDate = DateUtils.addDays(new Date(), -7);
        } else {
            queryDate = new Date(noticeTime);
        }
        return successMessage().add(RES_RESULT_CLAZZ_GROUPS, generateClazzGroupList(studentId, queryDate));
    }

    @RequestMapping(value = "/groups.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getGroups() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        List<Map<String, Object>> groupList = new ArrayList<>();
        groupMappers.stream()
                .sorted(Comparator.comparingInt(o -> o.getSubject().getKey()))
                .forEach(groupMapper -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put(RES_GROUP_SUBJECT_ENAME, groupMapper.getSubject().name());
                    map.put(RES_GROUP_SUBJECT_CNAME, groupMapper.getSubject().getValue());
                    groupList.add(map);
                });
        return successMessage().add(RES_RESULT_GROUP_LIST, groupList);
    }

    /**
     * 首页获取置顶消息
     */
    @RequestMapping(value = "top_notices.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getTopNotices() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_RELOGIN);
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT).add(RES_RESULT_STUDENT_ID_DYNAMIC, studentId);
        }

        Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false)
                .stream()
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(groupIds)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        MapMessage dynamicStudentMessage = getDynamicStudentId(groupIds, studentId);
        if (!isSuccess(dynamicStudentMessage)) {
            return dynamicStudentMessage;
        }
        Long dynamicStudentId = SafeConverter.toLong(dynamicStudentMessage.get("dynamicStudentId"));

        List<Map<String, Object>> topNotices = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(teachers)) {
            Set<Long> teacherIds = teachers.stream().map(Teacher::getId).collect(Collectors.toSet());
            Map<Long, Long> teacherMainIdMap = teacherLoaderClient.loadMainTeacherIds(teacherIds);
            teacherIds.addAll(teacherMainIdMap.values());
            List<JxtNotice> jxtNotices = jxtLoaderClient.getJxtNoticeListByTeacherIds(teacherIds)
                    .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            topNotices = generateTopNotice(dynamicStudentId, groupIds, jxtNotices);
        }

        return successMessage()
                .add(RES_RESULT_STUDENT_ID_DYNAMIC, dynamicStudentId)
                .add(RES_RESULT_TOP_NOTICES, topNotices);
    }


    @RequestMapping(value = "/menu.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getMenu() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_RELOGIN);
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }
        Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false)
                .stream()
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(groupIds)) {
            return successMessage();
        }
        String configContent;
        try {
            configContent = getPageBlockContentGenerator().getPageBlockContentHtml("newEaseMobBottomMenuConfig", newEaseMobBottomMenuKey);
            List<NewEaseMobBottomMenuConfig> newMenuConfigs = JsonUtils.fromJsonToList(configContent.replaceAll("\n|\r|\t", "").trim(), NewEaseMobBottomMenuConfig.class);
            return generateMenuList(newMenuConfigs, groupIds, ver);
        } catch (Exception e) {
            logger.warn("get menu config fail.sid:{}", studentId, e.getMessage());
            return successMessage().add("menu_list", Collections.emptyList());
        }
    }

    @RequestMapping(value = "clazz_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getClazzList() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_RELOGIN);
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("获取学生信息错误！");
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }
        if (studentDetail.getClazz() == null || studentDetail.isSeniorStudent() || studentDetail.isJuniorStudent()) {
            return successMessage().add(RES_RESULT_CLAZZ_LIST, Collections.emptyList());
        }
        Long parentId = parent.getId();
        List<Map<String, Object>> clazzList = generateClazzList(studentDetail, parentId);
        return successMessage().add(RES_RESULT_CLAZZ_LIST, clazzList);
    }

    /**
     * 班级列表去掉样式
     */
    @RequestMapping(value = "new_clazz_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getNewClazzList() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_RELOGIN);
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("获取学生信息错误！");
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }
        if (studentDetail.getClazz() == null || studentDetail.isSeniorStudent() || studentDetail.isJuniorStudent()) {
            return successMessage().add(RES_RESULT_CLAZZ_LIST, Collections.emptyList());
        }
        Long parentId = parent.getId();
        List<Map<String, Object>> clazzList = generateNewClazzList(studentDetail, parentId);
        return successMessage().add(RES_RESULT_CLAZZ_LIST, clazzList);
    }

    @RequestMapping(value = "report.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getReport() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_RELOGIN);
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return successMessage().add("message_list", Collections.emptyList());
        }

        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }
        Clazz clazz = studentDetail.getClazz();
        if (clazz == null || studentDetail.isSeniorStudent() || studentDetail.isJuniorStudent()) {
            return successMessage().add("message_list", Collections.emptyList());
        }
        List<Map<String, Object>> messageList = generateMessageList(studentDetail, parent.getId());
        return successMessage().add("message_list", messageList);
    }

    private List<Map<String, Object>> generateMessageList(StudentDetail studentDetail, Long parentId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Clazz clazz = studentDetail.getClazz();
        String commonIcon = getCdnBaseUrlStaticSharedWithSep() + "/public/skin/parentMobile/images/report_common.png";
        if (clazz == null) {
            return list;
        } else if (clazz.isTerminalClazz()) {
            Map<String, Object> map = new HashMap<>();
            map.put("icon", commonIcon);
            map.put("content", "该小学班级已毕业，暂不支持毕业账号");
            map.put("can_redirect", false);
            list.add(map);
            return list;
        }
        Long studentId = studentDetail.getId();
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        if (CollectionUtils.isEmpty(groupMappers)) {
            return list;
        }
        Map<Long, Subject> groupSubjectMap = groupMappers.stream()
                .collect(Collectors.toMap(GroupMapper::getId, GroupMapper::getSubject, (u1, u2) -> u1));
        Set<Long> groupIds = groupMappers.stream().map(GroupMapper::getId).collect(Collectors.toSet());
        Set<ScoreCircleGroupContext> contexts = loadGroupContexts(studentDetail.getId(), groupIds);
        Set<String> contextIds = new HashSet<>();
        contexts.forEach(context -> contextIds.add(context.generateId()));
        Map<String, ScoreCircleGroupContext> contextMap = contexts.stream().collect(Collectors.toMap(ScoreCircleGroupContext::generateId, Function.identity(), (u, v) -> u));
        List<ScoreCircleGroupContextStoreInfo> storeInfos = dpScoreCircleLoader.loads(contextIds).values().stream()
                .sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(storeInfos)) {
            Map<String, Object> map = new HashMap<>();
            map.put("icon", commonIcon);
            map.put("content", "点击这里查看最新作业消息");
            map.put("subject_ename", "COMMON");
            map.put("subject_cname", "通用");
            map.put("can_redirect", true);
            list.add(map);
            return list;
        }
        Set<String> confirmIds = new HashSet<>();
        storeInfos.forEach(storeInfo -> confirmIds.add(GroupMessageConfirmInfo.generateId(storeInfo.getGroupCircleType(), storeInfo.getTypeId())));
        Map<String, GroupMessageConfirmInfo> confirmInfoMap = dpConfirmInfoLoader.loadByIds(confirmIds);
        boolean showLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
        for (ScoreCircleGroupContextStoreInfo storeInfo : storeInfos) {
            ScoreCircleGroupContext context = contextMap.get(storeInfo.getInfoId());
            String icon;
            String messageType;
            String content;
            String confirmStatus = "";
            String confirmIcon = "";
            String finishStatus;
            String grade = "";
            String level = "";
            String messageDate;
            String subjectCName;
            String subjectEName;
            //学科icon
            GroupCircleType circleType = storeInfo.getGroupCircleType();
            Subject subject = groupSubjectMap.get(storeInfo.getGroupId());
            if (context.getTopMessageType() != null) {
                icon = commonIcon;
                subjectEName = "COMMON";
                subjectCName = "通用";
            } else if (subject != null && circleType.isNeedSubject()) {
                icon = getCdnBaseUrlStaticSharedWithSep() + SUBJECT_ICON_MAP.get(subject);
                subjectEName = subject.name();
                subjectCName = subject.getValue();
            } else {
                icon = commonIcon;
                subjectEName = "COMMON";
                subjectCName = "通用";
            }

            //消息类型
            messageType = generateMessageType(storeInfo, context);
            //消息内容
            content = generateContent(storeInfo, studentDetail);
            //家长确认状态
            String confirmId = GroupMessageConfirmInfo.generateId(circleType, storeInfo.getTypeId());
            GroupMessageConfirmInfo confirmInfo = confirmInfoMap.get(confirmId);
            ParentGroupMessageStatus messageStatus = getMessageStatus(confirmInfo, circleType, parentId, studentId);
            //完成状态
            finishStatus = getFinishStatus(storeInfo, studentDetail);
            boolean isFinish = MapUtils.isNotEmpty(storeInfo.getStudentScoreMap()) && storeInfo.getStudentScoreMap().containsKey(studentId);
            //其他消息只要已确认就过滤，指定消息过滤已确认并且已完成的
            if (messageStatus == ParentGroupMessageStatus.CONFIRMED || messageStatus == ParentGroupMessageStatus.READED || messageStatus == ParentGroupMessageStatus.CHECKED) {
                if (circleType == GroupCircleType.HOMEWORK_NEW || circleType == GroupCircleType.HOMEWORK_CHECK) {
                    if (isFinish) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            //指定消息只有在已完成才会显示确认状态
            if (circleType != GroupCircleType.HOMEWORK_NEW && circleType != GroupCircleType.HOMEWORK_CHECK || isFinish) {
                if (messageStatus == ParentGroupMessageStatus.UNCONFIRMED) {
                    confirmStatus = "待确认";
                } else {
                    confirmStatus = messageStatus != ParentGroupMessageStatus.UNKNOWN ? messageStatus.getTag() : "";
                }
                confirmIcon = getCdnBaseUrlStaticSharedWithSep() + messageStatus.getIcon();
            }

            //成绩或等级
            if (circleType == GroupCircleType.HOMEWORK_CHECK) {
                Map<Long, Integer> scoreMap = storeInfo.getStudentScoreMap();
                //已检查已完成，没有分数
                if (MapUtils.isNotEmpty(scoreMap) && scoreMap.get(studentId) != null) {
                    if (showLevel) {
                        level = generateHomeworkSourceLevel(scoreMap.get(studentId), studentDetail);
                    } else {
                        grade = generateHomeworkSourceLevel(scoreMap.get(studentId), studentDetail);
                    }
                }
            }

            if (circleType == GroupCircleType.COMMON && context.getCommonMessageType() == GroupCircleCommonMessageType.PLAN_WINTER_ACTIVITY) {
                content = storeInfo.getCardContent();
                finishStatus = StudyPlanningCacheManager.INSTANCE.hasStudentFinishActivity(studentId) ? "已完成" : "未完成";
            }

            if (context.getTopMessageType() == GroupCircleTopMessageType.POETRY_CONFERENCE) {
                content = storeInfo.getContent();
                finishStatus = "";
            }


            //消息产生日期
            messageDate = DateUtils.dateToString(storeInfo.getCreateDate(), "yy-MM-dd HH:mm");

            Map<String, Object> map = new HashMap<>();
            map.put("icon", icon);
            map.put("message_type", messageType);
            map.put("content", content);
            if (circleType == GroupCircleType.HOMEWORK_CHECK || context.getGroupCircleType() != null) {
                confirmStatus = "";
                confirmIcon = "";
            }
            map.put("confirm_status", confirmStatus);
            map.put("confirm_icon", confirmIcon);
            map.put("finish_status", finishStatus);
            map.put("grade", grade);
            map.put("level", level);
            map.put("message_date", messageDate);
            map.put("can_redirect", true);
            map.put("subject_ename", subjectEName);
            map.put("subject_cname", subjectCName);
            if (circleType == GroupCircleType.VOICE_RECOMMEND) {
                map.put("voice_icon", "语音推荐icon");
            }
            list.add(map);
        }
        if (CollectionUtils.isEmpty(list)) {
            Map<String, Object> map = new HashMap<>();
            map.put("icon", commonIcon);
            map.put("content", "点击这里查看最新作业消息");
            map.put("subject_ename", "COMMON");
            map.put("subject_cname", "通用");
            map.put("can_redirect", true);
            list.add(map);
        }
        return list;
    }

    private String generateMessageType(ScoreCircleGroupContextStoreInfo storeInfo, ScoreCircleGroupContext context) {
        String messageType;
        GroupCircleType circleType = storeInfo.getGroupCircleType();
        if (context.getTopMessageType() != null || context.getCommonMessageType() == GroupCircleCommonMessageType.PLAN_WINTER_ACTIVITY) {
            messageType = "";
        } else if (circleType == GroupCircleType.VOICE_RECOMMEND) {
            messageType = "[老师推荐了优秀语音] ";
        } else {
            messageType = "[" + circleType.getLeftTopTag() + "]";
        }
        return messageType;
    }

    private String generateContent(ScoreCircleGroupContextStoreInfo storeInfo, StudentDetail studentDetail) {
        String content = "";
        GroupCircleType circleType = storeInfo.getGroupCircleType();
        if (circleType == GroupCircleType.HOMEWORK_REPORT) {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(storeInfo.getTypeId());
            if (newHomework != null) {
                content = "分享咱们班" + DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日") + "的";
                if (newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
                    content += "纸质";
                }
                content += "作业情况";
            }
        } else if (circleType == GroupCircleType.HOMEWORK_WEEK_REPORT) {
            content = getHomeworkWeekReportContent(storeInfo);
        } else if (circleType == GroupCircleType.VOICE_RECOMMEND) {
            content += SafeConverter.toString(storeInfo.getCardSubContent(), "");
        } else {
            String unitName = SafeConverter.toString(storeInfo.getUnitName(), "");
            if (StringUtils.isNotBlank(unitName)) {
                unitName = "《" + unitName + "》";
            }
            content += unitName + " " + getContent(storeInfo, studentDetail);
        }
        return content;
    }

    private String getHomeworkWeekReportContent(ScoreCircleGroupContextStoreInfo storeInfo) {
        String content = "";
        String endTime = storeInfo.getEndTime();
        if (StringUtils.isNotBlank(endTime)) {
            Date endDate = DateUtils.stringToDate(endTime, "yyyyMMdd");
            Date beginDate = DateUtils.addDays(endDate, -6);
            String endTimeStr = DateUtils.dateToString(endDate, "MM.dd");
            String beginTimeStr = DateUtils.dateToString(beginDate, "MM.dd");
            content = "分享" + beginTimeStr + "-" + endTimeStr + "的班级作业周报，请各位家长查收";
        } else if (StringUtils.isNotBlank(storeInfo.getCardContent())) {
            //FIXME 作业周报的时间只能这样暂时兼容一下
            content = "分享" + storeInfo.getCardContent().substring(8, 19) + "的班级作业周报，请各位家长查收";
        }
        return content;
    }

    private List<Map<String, Object>> generateNewClazzList(StudentDetail studentDetail, Long parentId) {
        Long studentId = studentDetail.getId();
        List<Map<String, Object>> list = new ArrayList<>();
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        Map<Subject, Long> subjectGroupIdMap = groupMappers.stream()
                .collect(Collectors.toMap(GroupMapper::getSubject, GroupMapper::getId, (o1, o2) -> o1));
        Map<Long, CircleContext> contextMap = dpScoreCircleLoader.loadGroupLatestCircle(subjectGroupIdMap.values());
        Set<String> contextIds = new HashSet<>();
        for (CircleContext circleContext : contextMap.values()) {
            ScoreCircleGroupContext scoreCircleGroupContext = (ScoreCircleGroupContext) circleContext;
            contextIds.add(scoreCircleGroupContext.generateId());
        }
        Map<Long, ScoreCircleGroupContextStoreInfo> storeInfoMap = dpScoreCircleLoader.loads(contextIds).values()
                .stream()
                .collect(Collectors.toMap(ScoreCircleGroupContextStoreInfo::getGroupId, Function.identity()));
        Set<String> confirmIds = new HashSet<>();
        storeInfoMap.values().forEach(storeInfo -> confirmIds.add(GroupMessageConfirmInfo.generateId(storeInfo.getGroupCircleType(), storeInfo.getTypeId())));
        Map<String, GroupMessageConfirmInfo> confirmInfoMap = dpConfirmInfoLoader.loadByIds(confirmIds);
        boolean isTerminalClazz = studentDetail.getClazz().isTerminalClazz();
        boolean inBlackList = userBlacklistServiceClient.isInActivityBlackList(studentDetail);
        //当前作业卡片是否可以跳转
        for (Subject subject : SUBJECT_LIST) {
            //用该字段进行排序，有班级优先，整体按英语、数学、语文的顺序
            Integer subjectKey = subject.getKey();


            String title = "";
            String content;
            String finishStatus = "";
            String grade = "";
            String voiceContent = "";
            boolean canRedirect = true;
            String recommendUrl = "";
            String confirmStatus = "";
            boolean inGroup = true;
            if (isTerminalClazz) {
                content = "该小学班级已毕业，暂不支持毕业账号";
            } else {
                Long groupId = subjectGroupIdMap.get(subject);
                boolean showLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
                //有班级时显示消息内容，没班级时显示推荐内容
                if (groupId != null) {
                    //显示默认文案
                    //1.没有消息
                    //2.母亲节、儿童节作业未完成
                    ScoreCircleGroupContextStoreInfo storeInfo = storeInfoMap.get(groupId);
                    boolean useDefaultContent = useDefaultContent(storeInfo, studentId);
                    if (!useDefaultContent) {
                        GroupCircleType circleType = storeInfo.getGroupCircleType();
                        String confirmId = GroupMessageConfirmInfo.generateId(circleType, storeInfo.getTypeId());
                        GroupMessageConfirmInfo confirmInfo = confirmInfoMap.get(confirmId);
                        confirmStatus = getConfirmInfo(confirmInfo, circleType, parentId, studentId);
                        title = generateTitle(storeInfo);
                        content = getContent(storeInfo, studentDetail);
                        finishStatus = getFinishStatus(storeInfo, studentDetail);
                        grade = getGrade(storeInfo, studentDetail, showLevel);
                        voiceContent = getVoiceContent(storeInfo);
                    } else {
                        content = "在这里查看最新作业消息";
                    }
                } else {
                    inGroup = false;
                    subjectKey = subjectKey * 10;
                    title = "未加入" + subject.getValue() + "班级";
                    if (inBlackList) {
                        content = "推荐自学巩固" + subject.getValue() + "知识哦";
                        canRedirect = Boolean.FALSE;
                    } else {
                        String productType = SUBJECT_PRODUCT_MAP.get(subject);
                        content = getRecommendText(studentDetail, subject, productType);
                        recommendUrl = getRecommendUrl(subject);
                    }
                }

            }

            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_SUBJECT_ENGLISH_NAME, subject.name());
            map.put(RES_RESULT_SUBJECT_CHINESE_NAME, subject.getValue());
            map.put(RES_RESULT_TITLE, title);
            map.put(RES_RESULT_CONTENT, content);
            map.put(RES_RESULT_FINISH_STATUS, finishStatus);
            map.put(RES_RESULT_GRADE, grade);
            map.put(RES_RESULT_VOICE_RECOMMEND_CONTENT, voiceContent);
            map.put(RES_RESULT_CAN_REDIRECT, canRedirect);
            map.put(RES_RESULT_RECOMMEND_URL, recommendUrl);
            map.put(RES_RESULT_CONFIRM_STATUS, confirmStatus);
            map.put(RES_RESULT_IN_GROUP, inGroup);
            map.put(RES_RESULT_SUBJECT_KEY, subjectKey);
            list.add(map);
        }
        return list.stream()
                .sorted(Comparator.comparingInt(o -> SafeConverter.toInt(o.get(RES_RESULT_SUBJECT_KEY))))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> generateClazzList(StudentDetail studentDetail, Long parentId) {
        Long studentId = studentDetail.getId();
        List<Map<String, Object>> list = new ArrayList<>();
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        Map<Subject, Long> subjectGroupIdMap = groupMappers.stream()
                .collect(Collectors.toMap(GroupMapper::getSubject, GroupMapper::getId, (o1, o2) -> o1));
        Map<Long, CircleContext> contextMap = dpScoreCircleLoader.loadGroupLatestCircle(subjectGroupIdMap.values());
        Set<String> contextIds = new HashSet<>();
        for (CircleContext circleContext : contextMap.values()) {
            ScoreCircleGroupContext scoreCircleGroupContext = (ScoreCircleGroupContext) circleContext;
            contextIds.add(scoreCircleGroupContext.generateId());
        }
        Map<Long, ScoreCircleGroupContextStoreInfo> storeInfoMap = dpScoreCircleLoader.loads(contextIds).values()
                .stream()
                .collect(Collectors.toMap(ScoreCircleGroupContextStoreInfo::getGroupId, Function.identity()));
        Set<String> confirmIds = new HashSet<>();
        storeInfoMap.values().forEach(storeInfo -> confirmIds.add(GroupMessageConfirmInfo.generateId(storeInfo.getGroupCircleType(), storeInfo.getTypeId())));
        Map<String, GroupMessageConfirmInfo> confirmInfoMap = dpConfirmInfoLoader.loadByIds(confirmIds);
        boolean isTerminalClazz = studentDetail.getClazz().isTerminalClazz();
        boolean inBlackList = userBlacklistServiceClient.isInActivityBlackList(studentDetail);
        //当前作业卡片是否可以跳转
        boolean canRedirect = true;
        String defaultFontStart = "<font size='13px' color='#5C6681'>";
        String fontEnd = "</font>";
        String defaultSpan = "<span style='font-family:PingFang SC;font-size:13px;color:#5C6681;'>";

        for (Subject subject : SUBJECT_LIST) {
            //用该字段进行排序，有班级优先，整体按英语、数学、语文的顺序
            Integer subjectKey = subject.getKey();

            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_SUBJECT_ENGLISH_NAME, subject.name());
            map.put(RES_RESULT_SUBJECT_CHINESE_NAME, subject.getValue());
            String title = "";
            String content;
            if (isTerminalClazz) {
                if ("android".equals(getRequestString(REQ_SYS))) {
                    content = defaultFontStart + "该小学班级已毕业，暂不支持毕业账号" + fontEnd;
                } else {
                    content = defaultSpan + "该小学班级已毕业，暂不支持毕业账号";
                }
                canRedirect = Boolean.FALSE;
            } else {
                Long groupId = subjectGroupIdMap.get(subject);
                boolean showLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
                //有班级时显示消息内容，没班级时显示推荐内容
                if (groupId != null) {
                    //显示默认文案
                    //1.没有消息
                    //2.母亲节、儿童节作业未完成
                    ScoreCircleGroupContextStoreInfo storeInfo = storeInfoMap.get(groupId);
                    boolean useDefaultContent = useDefaultContent(storeInfo, studentId);
                    if (!useDefaultContent) {
                        GroupCircleType circleType = storeInfo.getGroupCircleType();
                        String confirmId = GroupMessageConfirmInfo.generateId(circleType, storeInfo.getTypeId());
                        GroupMessageConfirmInfo confirmInfo = confirmInfoMap.get(confirmId);
                        setConfirmInfo(map, confirmInfo, circleType, parentId, studentId);
                        title = generateTitle(storeInfo);
                        content = generateContent(storeInfo, studentDetail, showLevel);
                    } else {
                        if ("android".equals(getRequestString(REQ_SYS))) {
                            content = defaultFontStart + "在这里查看最新作业消息" + fontEnd;
                        } else {
                            content = defaultSpan + "在这里查看最新作业消息";
                        }
                    }
                } else {
                    subjectKey = subjectKey * 10;
                    title = "未加入" + subject.getValue() + "班级";
                    if (inBlackList) {
                        if ("android".equals(getRequestString(REQ_SYS))) {
                            content = defaultFontStart + "推荐自学巩固" + subject.getValue() + "知识哦" + fontEnd;
                        } else {
                            content = defaultSpan + "推荐自学巩固" + subject.getValue() + "知识哦";
                        }
                        canRedirect = Boolean.FALSE;
                    } else {
                        String productType = SUBJECT_PRODUCT_MAP.get(subject);
                        content = getRecommendText(studentDetail, subject, productType);
                        if ("android".equals(getRequestString(REQ_SYS))) {
                            content = defaultFontStart + content + fontEnd;
                        } else {
                            content = defaultSpan + content;
                        }
                        map.put(RES_RESULT_RECOMMEND_URL, getRecommendUrl(subject));
                    }
                }
            }
            map.put(RES_RESULT_SUBJECT_KEY, subjectKey);
            map.put(RES_RESULT_TITLE, title);
            map.put(RES_RESULT_CONTENT, content);
            map.put(RES_RESULT_CAN_REDIRECT, canRedirect);
            list.add(map);
        }
        return list.stream()
                .sorted(Comparator.comparingInt(o -> SafeConverter.toInt(o.get(RES_RESULT_SUBJECT_KEY))))
                .collect(Collectors.toList());
    }

    private boolean useDefaultContent(ScoreCircleGroupContextStoreInfo storeInfo, Long studentId) {
        boolean useDefaultContent = false;
        if (storeInfo == null) {
            useDefaultContent = true;
        } else if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_MOTHERS_DAY || storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_KIDS_DAY) {
            if (MapUtils.isEmpty(storeInfo.getStudentScoreMap()) || !storeInfo.getStudentScoreMap().containsKey(studentId)) {
                useDefaultContent = true;
            }
        }
        return useDefaultContent;
    }

    private String getRecommendUrl(Subject subject) {
        return ProductConfig.getMainSiteBaseUrl() + "/zion/nova-report?subject=" + subject.name();
    }

    //运营信息,小U使用人数
    private String getRecommendText(StudentDetail studentDetail, Subject subject, String productType) {
        String useDesc = "";
        for (AppUseNumCalculateType calculateType : AppUseNumCalculateType.values()) {
            Map<String, Integer> numMap = businessVendorServiceClient.loadUseNum(calculateType, Collections.singletonList(productType), studentDetail);
            Integer num = SafeConverter.toInt(numMap.get(productType));
            if (num > 10 && StringUtils.isBlank(useDesc)) {
                String numStr = num < 10000 ? String.valueOf(num) : String.valueOf((num + 5000) / 10000) + "万";
                useDesc = numStr + "名" + CALCULATE_TYPE_MAP.get(calculateType) + "同学正在使用小U进行" + subject.getValue() + "自学 >";
                break;
            }
        }
        if (StringUtils.isBlank(useDesc)) {
            useDesc = "推荐使用小U" + subject.getValue() + "进行自学";
        }
        return useDesc;
    }

    private String getFinishStatus(ScoreCircleGroupContextStoreInfo storeInfo, StudentDetail studentDetail) {
        String finishStatus = "";
        Long studentId = studentDetail.getId();
        GroupCircleType circleType = storeInfo.getGroupCircleType();
        Map<Long, Boolean> repairMap = storeInfo.getStudentIsRepairMap();
        switch (circleType) {
            case HOMEWORK_NEW:
                if (MapUtils.isNotEmpty(repairMap) && repairMap.containsKey(studentId)) {
                    finishStatus = "已完成";
                } else {
                    finishStatus = "未完成";
                }
                break;
            case HOMEWORK_CHECK:
                //已检查未完成，显示未完成
                if (MapUtils.isEmpty(repairMap) || !repairMap.containsKey(studentId)) {
                    finishStatus = "未完成";
                } else {
                    Map<Long, Integer> scoreMap = storeInfo.getStudentScoreMap();
                    if (MapUtils.isEmpty(scoreMap) || scoreMap.get(studentId) == null) {
                        finishStatus = "已完成";
                    }
                }
        }
        return finishStatus;
    }

    private String getGrade(ScoreCircleGroupContextStoreInfo storeInfo, StudentDetail studentDetail, boolean showLevel) {
        String grade = "";
        Long studentId = studentDetail.getId();
        GroupCircleType circleType = storeInfo.getGroupCircleType();
        if (circleType == GroupCircleType.HOMEWORK_CHECK) {
            Map<Long, Integer> scoreMap = storeInfo.getStudentScoreMap();
            //已检查已完成，没有分数
            if (MapUtils.isNotEmpty(scoreMap) && scoreMap.get(studentId) != null) {
                grade = generateHomeworkSourceLevel(scoreMap.get(studentId), studentDetail);
                if (!showLevel) {
                    grade += "分";
                }
            }
        }
        return grade;
    }

    private String getVoiceContent(ScoreCircleGroupContextStoreInfo storeInfo) {
        String voiceContent = "";
        //语音推荐 内容单独处理
        if (storeInfo.getGroupCircleType() == GroupCircleType.VOICE_RECOMMEND) {
            voiceContent = SafeConverter.toString(storeInfo.getCardSubContent(), "");
        }
        return voiceContent;
    }

    private String getContent(ScoreCircleGroupContextStoreInfo storeInfo, StudentDetail studentDetail) {
        String content = "";
        GroupCircleType circleType = storeInfo.getGroupCircleType();
        NewHomework newHomework;
        switch (circleType) {
            case HOMEWORK_NEW:
                newHomework = newHomeworkLoaderClient.loadNewHomework(storeInfo.getTypeId());
                if (newHomework != null && newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
                    content = "新纸质作业";
                }
                break;
            case HOMEWORK_CHECK:
                newHomework = newHomeworkLoaderClient.loadNewHomework(storeInfo.getTypeId());
                if (newHomework != null && newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
                    content = "纸质作业已检查";
                }
                break;
            case VOICE_RECOMMEND:
                break;
            case HOMEWORK_MOTHERS_DAY:
                content = "您的孩子" + studentDetail.fetchRealname() + "送您的母亲节礼物，祝您母亲节快乐～";
                break;
            case HOMEWORK_KIDS_DAY:
                content = "您的孩子" + studentDetail.fetchRealname() + "完成了儿童节趣味作业，快去奖励孩子并发送鼓励吧";
                break;
            case OFFLINE_HOMEWORK:
                content = SafeConverter.toString(storeInfo.getContent(), "").replace("线下作业", "");
                break;
            default:
                content = SafeConverter.toString(storeInfo.getCardContent());
                if (StringUtils.isBlank(content)) {
                    content = SafeConverter.toString(storeInfo.getContent(), "");
                }
                break;
        }
        return content.replaceAll("<br>", "");
    }

    //首页班级卡片content
    private String generateContent(ScoreCircleGroupContextStoreInfo storeInfo, StudentDetail studentDetail, boolean showLevel) {
        Long studentId = studentDetail.getId();
        String sys = getRequestString(REQ_SYS);
        String content;
        GroupCircleType circleType = storeInfo.getGroupCircleType();
        Map<Long, Boolean> repairMap = storeInfo.getStudentIsRepairMap();
        switch (circleType) {
            case HOMEWORK_NEW:
                if (MapUtils.isNotEmpty(repairMap) && repairMap.containsKey(studentId)) {
                    if ("android".equals(sys)) {
                        content = "<font color='#29B18A' size='16px'>已完成</font>";
                    } else {
                        content = "<span style='font-family:PingFang SC;color:#29B18A;font-size:16px;'>已完成";
                    }
                } else {
                    if ("android".equals(sys)) {
                        content = "<font color='#FC6C4F' size='16px'>未完成</font>";
                    } else {
                        content = "<span style='font-family:PingFang SC;color:#FC6C4F;font-size:16px;'>未完成";
                    }
                }
                break;
            case HOMEWORK_CHECK:
                //已检查未完成，显示未完成
                if (MapUtils.isEmpty(repairMap) || !repairMap.containsKey(studentId)) {
                    if ("android".equals(sys)) {
                        content = "<font color='#FC6C4F' size='16px'>未完成</font>";
                    } else {
                        content = "<span style='font-family:PingFang SC;color:#FC6C4F;font-size:16px'>未完成";
                    }
                } else {
                    Map<Long, Integer> scoreMap = storeInfo.getStudentScoreMap();
                    //已检查已完成，没有分数
                    if (MapUtils.isNotEmpty(scoreMap) && scoreMap.get(studentId) != null) {
                        String score = generateHomeworkSourceLevel(scoreMap.get(studentId), studentDetail);
                        if ("android".equals(sys)) {
                            content = "<font color='#00B38A' size='27px'>" + score + "</font>";
                            if (!showLevel) {
                                content += "<font color='#00B38A' size='12px'>分</font>";
                            }
                        } else {
                            content = "<span style='font-family:PingFang SC;color:#00B38A;font-size:27px'>" + score;
                            if (!showLevel) {
                                content += "<span style='font-family:PingFang SC;color:#00B38A;font-size:12px;'>分";
                            }
                        }
                    } else {
                        if ("android".equals(sys)) {
                            content = "<font color='#29B18A' size='16px'>已完成</font>";
                        } else {
                            content = "<span style='font-family:PingFang SC;color:#29B18A;font-size:16px;'>已完成";
                        }
                    }
                }
                break;
            case VOICE_RECOMMEND:
                String imgUrl = getCdnBaseUrlStaticSharedWithSep() + "/public/skin/parentMobile/images/new_icon/bofang.png";
                content = "<img src='" + imgUrl + "' height='39px' width='39px'>";
                break;
            default:
                if (circleType == GroupCircleType.HOMEWORK_MOTHERS_DAY) {
                    content = "您的孩子" + studentDetail.fetchRealname() + "送您的母亲节礼物，祝您母亲节快乐～";
                } else if (circleType == GroupCircleType.HOMEWORK_KIDS_DAY) {
                    content = "您的孩子" + studentDetail.fetchRealname() + "完成了儿童节趣味作业，快去奖励孩子并发送鼓励吧";
                } else {
                    content = SafeConverter.toString(storeInfo.getCardContent());
                }
                if (StringUtils.isBlank(content)) {
                    content = SafeConverter.toString(storeInfo.getContent(), "");
                }
                if ("android".equals(sys)) {
                    content = "<font size='13px' color='#5C6681'>" + content + "</font>";
                } else {
                    content = "<span style='font-family:PingFang SC;font-size:13px;color:#5C6681;'>" + content;
                }
                break;
        }
        String subContent = storeInfo.getCardSubContent();
        if (StringUtils.isNotBlank(subContent)) {
            if ("android".equals(sys)) {
                content += "<br><font size='12px' color='#5C6681'>" + subContent + "</font>";
            } else {
                content += "<br><span style='font-family:PingFang SC;font-size:12px;color:#5C6681;'>" + subContent + "";
            }
        }
        return content;
    }

    //首页班级卡片title
    private String generateTitle(ScoreCircleGroupContextStoreInfo storeInfo) {
        String title = "";
        GroupCircleType circleType = storeInfo.getGroupCircleType();
        if (circleType == GroupCircleType.VOICE_RECOMMEND) {
            title += "老师推荐了优秀语音";
        } else {
            title += "[" + circleType.getLeftTopTag() + "]" + SafeConverter.toString(storeInfo.getUnitName(), "");
        }
        return title;
    }

    //首页班级卡片确认信息
    private String getConfirmInfo(GroupMessageConfirmInfo confirmInfo, GroupCircleType circleType, Long parentId, Long studentId) {
        ParentGroupMessageStatus messageStatus = getMessageStatus(confirmInfo, circleType, parentId, studentId);
        return messageStatus != ParentGroupMessageStatus.UNKNOWN ? messageStatus.getTag() : "";
    }

    private ParentGroupMessageStatus getMessageStatus(GroupMessageConfirmInfo confirmInfo, GroupCircleType circleType, Long parentId, Long studentId) {
        int confirmStatus = circleType.getUnConfirmStatus();
        if (confirmInfo != null) {
            Set<Long> confirmUserIds = confirmInfo.getConfirmUserIds();
            if (CollectionUtils.isNotEmpty(confirmUserIds)) {
                if (confirmUserIds.contains(parentId)) {
                    confirmStatus = circleType.getConfirmStatus();
                } else {
                    Set<Long> parentIds = studentLoaderClient.loadStudentParentRefs(studentId)
                            .stream()
                            .map(StudentParentRef::getParentId)
                            .collect(Collectors.toSet());
                    if (parentIds.stream().anyMatch(confirmUserIds::contains)) {
                        confirmStatus = circleType.getConfirmStatus();
                    }
                }
            }
        }
        return ParentGroupMessageStatus.parse(confirmStatus);
    }

    //首页班级卡片确认信息
    private void setConfirmInfo(Map<String, Object> cardMap, GroupMessageConfirmInfo confirmInfo, GroupCircleType circleType, Long parentId, Long studentId) {
        ParentGroupMessageStatus messageStatus = getMessageStatus(confirmInfo, circleType, parentId, studentId);
        if (messageStatus != ParentGroupMessageStatus.UNKNOWN) {
            cardMap.put(RES_RESULT_CONFIRM_STATUS, messageStatus.getTag());
        }
    }

    private MapMessage generateMenuList(List<NewEaseMobBottomMenuConfig> menuConfigs, Set<Long> groupIds, String ver) {
        if (CollectionUtils.isEmpty(menuConfigs)) {
            return successMessage().add(RES_GROUP_MENU_LIST, Collections.EMPTY_LIST);
        }
        Map<Long, GroupMapper> groupMapperMap = deprecatedGroupLoaderClient.loadGroups(groupIds, false);
        GroupMapper groupMapper = groupMapperMap.values()
                .stream()
                .min(Comparator.comparingInt(o -> o.getSubject().getKey()))
                .orElse(null);
        if (groupMapper == null) {
            return failMessage("班级错误");
        }
        Long firstGroupId = groupMapper.getId();
        //聊天组内的所有学科
        Set<Subject> allSubjects = teacherLoaderClient.loadGroupTeacher(groupIds).values()
                .stream()
                .flatMap(Collection::stream)
                .filter(p -> p != null && p.getSubject() != null)
                .map(Teacher::getSubject)
                .collect(Collectors.toSet());
        List<Map<String, Object>> menuList = new ArrayList<>();
        for (NewEaseMobBottomMenuConfig menuConfig : menuConfigs) {
            //版本不匹配。跳过
            if (!VersionUtil.checkVersionConfig(menuConfig.getVersion(), ver)) {
                continue;
            }
            //学科不匹配。跳过
            if (StringUtils.isNotBlank(menuConfig.getSubject()) && !allSubjects.contains(Subject.of(menuConfig.getSubject()))) {
                continue;
            }
            //配置中的一级菜单——
            Map<String, Object> firstLevelConfigMap = menuConfig.getFirstLevel();
            if (MapUtils.isEmpty(firstLevelConfigMap)) {
                continue;
            }
            Map<String, Object> menuMap = new HashMap<>();
            Map<String, Object> firstLevelMap = new HashMap<>();
            firstLevelMap.put(RES_GROUP_MENU_ITEM_DESC, SafeConverter.toString(firstLevelConfigMap.get("desc"), ""));
            List<Map<String, Object>> secondLevelConfigList = menuConfig.getSecondLevel();
            if (CollectionUtils.isNotEmpty(secondLevelConfigList)) {
                List<Map<String, Object>> secondLevelList = new ArrayList<>();
                for (Map<String, Object> e : secondLevelConfigList) {
                    if (MapUtils.isEmpty(e)) {
                        continue;
                    }
                    if (!VersionUtil.checkVersionConfig(SafeConverter.toString(e.get("version")), ver)) {
                        continue;
                    }
                    String subject = SafeConverter.toString(e.get("subject"), "");
                    if (StringUtils.isNotBlank(subject) && !allSubjects.contains(Subject.of(subject))) {
                        continue;
                    }
                    Map<String, Object> secondLevelMap = new HashMap<>();
                    secondLevelMap.put(RES_GROUP_MENU_ITEM_DESC, SafeConverter.toString(e.get("desc"), ""));
                    secondLevelMap.put(RES_GROUP_MENU_ITEM_URL, generateUrl(SafeConverter.toString(e.get("url"), ""), firstGroupId));
                    secondLevelMap.put(RES_GROUP_MENU_ITEM_TYPE, SafeConverter.toString(e.get("type"), ""));
                    secondLevelMap.put(RES_GROUP_MENU_ITEM_COUNT, 0);
                    secondLevelList.add(secondLevelMap);
                }
                menuMap.put("second_level", secondLevelList);
            } else {
                //二级菜单配置为空，则一级菜单有跳转url
                String itemUrl = SafeConverter.toString(firstLevelConfigMap.get("url"), "");
                firstLevelMap.put(RES_GROUP_MENU_ITEM_URL, generateUrl(itemUrl, firstGroupId));
            }
            firstLevelMap.put(RES_GROUP_MENU_ITEM_COUNT, 0);
            menuMap.put(RES_GROUP_MENU_FIRST_LEVEL, firstLevelMap);
            menuList.add(menuMap);
        }
        return successMessage().add(RES_GROUP_MENU_LIST, menuList);
    }

    private String generateUrl(String baseUrl, Long groupId) {
        if (StringUtils.isBlank(baseUrl)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(baseUrl);
        if (baseUrl.contains("?")) {
            sb.append("&").append("group_id=").append(groupId);
        } else {
            sb.append("?group_id=").append(groupId);
        }
        return sb.toString();
    }

    /**
     * @param studentId 学生id
     * @return 首页班群信息
     */
    private List<Map<String, Object>> generateClazzGroupList(Long studentId, Date queryDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        if (CollectionUtils.isEmpty(groupMappers)) {
            return list;
        }
        Map<String, Object> map = new HashMap<>();
        List<Long> groupIds = groupMappers.stream()
                .sorted(Comparator.comparingInt(o -> o.getSubject().getKey()))
                .map(GroupMapper::getId)
                .collect(Collectors.toList());

        List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(groupIds).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        //班群学科名
        String groupSubjectName = RES_RESULT_GROUP_NO_TEACHER_SUBJECT_INFO;
        if (CollectionUtils.isNotEmpty(teachers)) {
            List<Subject> subjects = teachers.stream().map(Teacher::getSubject).sorted(Comparator.comparingInt(Subject::getKey)).collect(Collectors.toList());
            groupSubjectName = generateSubjectsName(subjects);
        }
        String sys = getRequestString(REQ_SYS);
        String groupExtInfo;
        if ("android".equals(sys)) {
            groupExtInfo = "<font color='#878E9F' size='16px'>老师布置的作业可在此查看，请及时关注</font>";
        } else {
            groupExtInfo = "<span style='color:#878E9F;font-size:16px;'>老师布置的作业可在此查看，请及时关注</span>";
        }

        Long groupExtInfoTime = 0L;
        ScoreCircleGroupContextStoreInfo storeInfo = getStoreInfo(groupIds);

        if (storeInfo != null) {
            String content;
            if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_MOTHERS_DAY) {
                User student = raikouSystem.loadUser(studentId);
                content = "您的孩子" + (student == null ? "" : student.fetchRealname()) + "送您的母亲节礼物，祝您母亲节快乐～";
            } else if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_KIDS_DAY) {
                User student = raikouSystem.loadUser(studentId);
                content = "您的孩子" + (student == null ? "" : student.fetchRealname()) + "完成了儿童节趣味作业，快去奖励孩子并发送鼓励吧";
            } else {
                content = storeInfo.getContent();
            }
            if ("android".equals(sys)) {
                groupExtInfo = "<font color='#4A5060' size='16px'>[" + storeInfo.getGroupCircleType().getLeftTopTag() + "]</font><font color='#878E9F' size='16px'>" + content + "</font>";
            } else {
                groupExtInfo = "<span style='color:#4A5060;font-size:16px;'>[" + storeInfo.getGroupCircleType().getLeftTopTag() + "]<span style='color:#878E9F;font-size:16px;'>" + content;
            }
            groupExtInfoTime = storeInfo.getCreateDate().getTime();
        }

        //学科名称组合
        map.put(RES_GROUP_SUBJECTS, groupSubjectName);
        //消息正文
        map.put(RES_GROUP_EXT_INFO, groupExtInfo);
        //消息产生时间
        map.put(RES_GROUP_EXT_INFO_TIME, groupExtInfoTime);
        map.put(RES_RESULT_NOTICE_COUNT, getNoticeCount(groupIds, queryDate));
        list.add(map);
        return list;
    }

    /**
     * @param subjects 学科列表
     * @return 学科名集合
     */
    private String generateSubjectsName(List<Subject> subjects) {
        StringBuilder subjectsName = new StringBuilder();
        if (CollectionUtils.isNotEmpty(subjects)) {
            for (Subject subject : subjects) {
                if (subject != null) {
                    if (subjectsName.length() > 0) {
                        subjectsName.append("/");
                    }
                    subjectsName.append(subject.getValue());
                }
            }
        }
        return subjectsName.toString();
    }

    /**
     * @param groupIds 班群ids
     * @return 班群消息
     */
    private ScoreCircleGroupContextStoreInfo getStoreInfo(List<Long> groupIds) {
        Map<Long, CircleContext> circleContextMap = dpScoreCircleLoader.loadGroupLatestCircle(groupIds);
        Map<Long, CircleContext> ignoreCircleContextMap = dpScoreCircleLoader.loadGroupIgnoreSubjectLatestCircle(groupIds);
        Set<String> contextIds = new HashSet<>();
        //有学科的部分
        for (CircleContext circleContext : circleContextMap.values()) {
            ScoreCircleGroupContext scoreCircleGroupContext = (ScoreCircleGroupContext) circleContext;
            contextIds.add(scoreCircleGroupContext.generateId());
        }
        //忽略学科的部分
        for (CircleContext circleContext : ignoreCircleContextMap.values()) {
            ScoreCircleGroupContext scoreCircleGroupContext = (ScoreCircleGroupContext) circleContext;
            contextIds.add(scoreCircleGroupContext.generateId());
        }
        return dpScoreCircleLoader.loads(contextIds).values()
                .stream()
                .max(Comparator.comparing(ScoreCircleGroupContextStoreInfo::getCreateDate))
                .orElse(null);
    }

    /**
     * @param studentId 动态学生id
     * @param groupIds  所有学科groupId
     * @return 置顶通知列表
     */
    private List<Map<String, Object>> generateTopNotice(Long studentId, Set<Long> groupIds, List<JxtNotice> jxtNotices) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(jxtNotices)) {
            return list;
        }

        //过滤得到这个班里所有通知
        Set<JxtNotice> thisClazzNotices = new HashSet<>();
        groupIds.forEach(groupId -> jxtNotices.stream().filter(jxtNotice -> jxtNotice.getGroupIds().contains(groupId)).forEach(thisClazzNotices::add));
        if (CollectionUtils.isEmpty(thisClazzNotices)) {
            return list;
        }
        //所有通知按创建时间倒序排序
        List<JxtNotice> sortedList = thisClazzNotices.stream()
                .filter(jxtNotice -> jxtNotice.getExpireTime() != null && jxtNotice.getExpireTime().after(new Date()))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
        JxtNotice topNotice = null;
        Teacher teacher = null;
        if (CollectionUtils.isNotEmpty(sortedList)) {
            for (JxtNotice jxtNotice : sortedList) {
                teacher = teacherLoaderClient.loadTeacher(jxtNotice.getTeacherId());
                if (teacher != null) {
                    topNotice = jxtNotice;
                    break;
                }
            }
        }
        if (topNotice != null) {
            Map<String, Object> map = new HashMap<>();
            String teacherName = teacher.fetchRealname();
            String teacherShowName = (StringUtils.isEmpty(teacherName) ? "" : teacherName.substring(0, 1)) + "老师";
            String subjectsName;
            List<Subject> subjects = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(teacher.getSubjects())) {
                subjects = teacher.getSubjects().stream().sorted(Comparator.comparing(Subject::getKey)).collect(Collectors.toList());
            }
            subjectsName = generateSubjectsName(subjects);
            JxtNoticeType noticeType = JxtNoticeType.ofWithUnKnow(topNotice.getNoticeType());
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append(teacherShowName)
                    .append("（")
                    .append(subjectsName)
                    .append("）：");
            switch (noticeType) {
                case OFFLINE_HOMEWORK:
                    contentBuilder.append("线下作业");
                    break;
                case ClAZZ_AFFAIR:
                    String img = CollectionUtils.isEmpty(topNotice.getImgUrl()) ? "" : "[图片]";
                    String voice = StringUtils.isBlank(topNotice.getVoiceUrl()) ? "" : "[语音]";
                    contentBuilder.append(img)
                            .append(voice)
                            .append(topNotice.getContent());
                    break;
                default:
                    break;

            }
            map.put(RES_RESULT_TOP_NOTICE_CONTENT, contentBuilder.toString());
            //置顶通知的类型不一样。打开的地址不一样
            Optional<Long> first = topNotice.getGroupIds().stream().filter(groupIds::contains).findFirst();
            if (first.isPresent()) {
                Long groupId = first.get();
                if (topNotice.getNoticeType() == JxtNoticeType.ClAZZ_AFFAIR.getType()) {
                    map.put(RES_RESULT_TOP_NOTICE_URL, "/view/mobile/common/notice_detail?user_type=parent&notice_id=" + topNotice.getId() + "&group_id=" + groupId);
                } else {
                    map.put(RES_RESULT_TOP_NOTICE_URL, "/view/offlinehomework/detail?ohids=" + topNotice.getGroupOfflineHomeworkIdMap().get(groupId) + "&sid=" + studentId);
                }
            }
            list.add(map);
        }
        return list;
    }

    /**
     * 获取对应班群的学生id
     *
     * @param groupIds  groupIds
     * @param studentId 当前选中学生id
     * @return 当前班群该家长的孩子id
     */
    private MapMessage getDynamicStudentId(Set<Long> groupIds, Long studentId) {
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
        Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
        Long dynamicStudentId;
        List<GroupStudentTuple> groupStudentRefs = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupIds(groupIds);
        Set<Long> groupStudentIds = groupStudentRefs.stream()
                .map(GroupStudentTuple::getStudentId)
                .collect(Collectors.toSet());
        //班群里只有一个孩子
        if (groupStudentIds.size() == 1) {
            dynamicStudentId = new ArrayList<>(groupStudentIds).get(0);
        } else if (groupStudentIds.size() > 1) {
            //如果班群中包含当前孩子
            if (studentId != null && groupStudentIds.stream().anyMatch(studentId::equals)) {
                dynamicStudentId = studentId;
            } else {
                //家长所有孩子中最早加入班群的
                GroupStudentTuple groupStudentRef = groupStudentRefs.stream()
                        .filter(p -> studentIds.contains(p.getStudentId()))
                        .min((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                        .orElse(null);
                if (groupStudentRef != null) {
                    dynamicStudentId = groupStudentRef.getStudentId();
                } else {
                    return failMessage(RES_RESULT_GROUP_REF_ERROR);
                }
            }
        } else {
            return failMessage(RES_RESULT_GROUP_REF_ERROR);
        }
        return successMessage().add("dynamicStudentId", dynamicStudentId);
    }

    private Set<ScoreCircleGroupContext> loadGroupContexts(Long studentId, Collection<Long> groupIds) {
        //时间处理
        Date current = new Date();
        //最多取两个月内的数据
        Date limitDate = DateUtils.addDays(current, -60);
        MonthRange monthRange = MonthRange.current();
        List<ScoreCircleResponse> scoreCircleResponses = new ArrayList<>();
        Set<ScoreCircleGroupContext> returnContexts;

        List<ScoreCircleGroupContext> topContexts = loadTopMessageContextList(studentId, groupIds);
        Set<ScoreCircleGroupContext> contexts = new HashSet<>(topContexts);
        ScoreCircleGroupContext context = new ScoreCircleGroupContext();
        context.setUserId(studentId);
        Date queryDate = new Date();
        do {
            context.setCreateDate(queryDate);
            for (Long groupId : groupIds) {
                context.setGroupId(groupId);
                //最新的3条
                ScoreCircleResponse circleResponse = dpScoreCircleLoader.loadGroupCircleByScoreDesc(context, 3);
                scoreCircleResponses.add(circleResponse);
                ScoreCircleResponse ignoreSubjectCircleResponse = dpScoreCircleLoader.loadGroupIgnoreSubjectCircleByScoreDesc(context, 3);
                scoreCircleResponses.add(ignoreSubjectCircleResponse);
            }
            ScoreCircleResponse noticeResponse = dpScoreCircleLoader.loadNoticeCircleByScoreDesc(context, 3);
            noticeResponse.getContextList()
                    .stream()
                    .filter(e -> e instanceof ScoreCircleGroupContext)
                    .map(e -> (ScoreCircleGroupContext) e)
                    .filter(e -> e.getUserId() == null)
                    .filter(e -> e.getCreateDate().after(limitDate))
                    .forEach(contexts::add);

            ScoreCircleResponse personalResponse = dpScoreCircleLoader.loadPersonalCircleByScoreDesc(context, 3);
            scoreCircleResponses.add(personalResponse);

            scoreCircleResponses.stream()
                    .filter(ScoreCircleResponse::isSuccess)
                    .forEach(response -> response.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(e -> e.getCreateDate().after(limitDate))
                            .forEach(contexts::add));

            ScoreCircleGroupContext activityContext = contexts.stream()
                    .filter(e -> e.getCommonMessageType() == GroupCircleCommonMessageType.PLAN_WINTER_ACTIVITY)
                    .min(Comparator.comparing(ScoreCircleGroupContext::getCreateDate))
                    .orElse(null);
            returnContexts = contexts.stream().filter(e -> e.getCommonMessageType() != GroupCircleCommonMessageType.PLAN_WINTER_ACTIVITY).collect(Collectors.toSet());
            if (activityContext != null) {
                returnContexts.add(activityContext);
            }

            if (returnContexts.size() >= 3) {
                break;
            }
            monthRange = monthRange.previous();
            queryDate = monthRange.getEndDate();
        } while (queryDate.after(limitDate));
        return returnContexts.stream().sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate())).limit(3).collect(Collectors.toSet());
    }

    private int getNoticeCount(Collection<Long> groupIds, Date queryDate) {
        int noticeCount = 0;
        Date now = new Date();
        if (queryDate.after(now)) {
            return noticeCount;
        }
        //最多查60天的数据
        Date limitDate = DateUtils.addDays(now, -60);
        if (queryDate.before(limitDate)) {
            queryDate = limitDate;
        }
        Set<ScoreCircleGroupContext> contexts = new HashSet<>();
        MonthRange monthRange = MonthRange.newInstance(queryDate.getTime());
        do {
            for (Long groupId : groupIds) {
                ScoreCircleGroupContext context = new ScoreCircleGroupContext();
                context.setGroupId(groupId);
                context.setCreateDate(queryDate);
                ScoreCircleResponse circleResponse = dpScoreCircleLoader.loadGroupCircleByScoreAsc(context, 30);
                if (circleResponse.isSuccess()) {
                    Date finalQueryDate = queryDate;
                    circleResponse.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(e -> e.getCreateDate().after(finalQueryDate))
                            .forEach(contexts::add);
                }
                //不需要学科属性的动态
                ScoreCircleResponse ignoreSubjectCircleResponse = dpScoreCircleLoader.loadGroupIgnoreSubjectCircleByScoreAsc(context, 30);
                if (ignoreSubjectCircleResponse.isSuccess()) {
                    Date finalQueryDate = queryDate;
                    ignoreSubjectCircleResponse.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(e -> e.getCreateDate().after(finalQueryDate))
                            .forEach(contexts::add);
                }
            }
            noticeCount += contexts.size();
            //超过30条，返回30
            if (noticeCount >= 30) {
                return 30;
            }
            monthRange = monthRange.next();
            queryDate = monthRange.getStartDate();
        } while (queryDate.before(now));
        return noticeCount;
    }

    private List<ScoreCircleGroupContext> loadTopMessageContextList(Long studentId, Collection<Long> groupIds) {
        List<ScoreCircleGroupContext> topContextList = new ArrayList<>();
        Date now = new Date();
        ScoreCircleGroupContext parameter = new ScoreCircleGroupContext();
        parameter.setUserId(studentId);
        ScoreCircleResponse userResponse = dpScoreCircleLoader.loadTopScoreCircle(parameter);
        if (userResponse.isSuccess()) {
            List<ScoreCircleGroupContext> contextList = userResponse.getContextList().stream()
                    .filter(e -> e instanceof ScoreCircleGroupContext)
                    .map(e -> (ScoreCircleGroupContext) e)
                    .filter(e -> e.getTopStartDate().before(now) && (e.getTopEndDate() == null || e.getTopEndDate().after(new Date())))
                    .collect(Collectors.toList());
            topContextList.addAll(contextList);
        }
        for (Long groupId : groupIds) {
            AncientPoetryActivity activity = ancientPoetryLoader.loadActivityByGroupId(groupId).stream()
                    .max(Comparator.comparing(AncientPoetryActivity::getEndDate))
                    .orElse(null);
            parameter.setGroupId(groupId);
            ScoreCircleResponse groupResponse = dpScoreCircleLoader.loadTopScoreCircle(parameter);
            List<ScoreCircleGroupContext> contextList = groupResponse.getContextList().stream()
                    .filter(e -> e instanceof ScoreCircleGroupContext)
                    .map(e -> (ScoreCircleGroupContext) e)
                    .filter(e -> e.getTopStartDate().before(now) && (e.getTopEndDate() == null || e.getTopEndDate().after(new Date())))
                    .collect(Collectors.toList());
            contextList = contextList.stream()
                    .filter(e -> e.getTopMessageType() != GroupCircleTopMessageType.POETRY_CONFERENCE ||
                            (activity != null && activity.getEndDate().after(now)))
                    .collect(Collectors.toList());

            topContextList.addAll(contextList);
        }
        return topContextList;
    }

    /**
     * 取置顶消息
     */
    private ScoreCircleGroupContext loadTopMessageContext(Long studentId, GroupCircleTopMessageType topMessageType) {
        ScoreCircleGroupContext topContext = null;
        ScoreCircleGroupContext topParam = new ScoreCircleGroupContext();
        topParam.setUserId(studentId);
        ScoreCircleResponse topResponse = dpScoreCircleLoader.loadTopScoreCircle(topParam);
        if (topResponse.isSuccess()) {
            Comparator<ScoreCircleGroupContext> comparator = Comparator.comparing(ScoreCircleGroupContext::getOrder);
            comparator = comparator.thenComparing((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()));
            topContext = topResponse.getContextList().stream()
                    .filter(e -> e instanceof ScoreCircleGroupContext)
                    .map(e -> (ScoreCircleGroupContext) e)
                    .filter(e -> e.getTopStartDate().before(new Date()) && (e.getTopEndDate() == null || e.getTopEndDate().after(new Date())))
                    .filter(e -> topMessageType == null || topMessageType == e.getTopMessageType())
                    .min(comparator)
                    .orElse(null);
        }
        return topContext;
    }

    /**
     * 拉取ScoreCircleGroupContext
     *
     * @param studentId  学生id
     * @param groupIds   学科id
     * @param createDate 截止时间
     */
    private Set<ScoreCircleGroupContext> loadContext(Long studentId,
                                                     Collection<Long> groupIds,
                                                     Date createDate,
                                                     boolean withoutSubject,
                                                     boolean needFilter) {
        //时间处理
        Date current = new Date();
        //最多取两个月内的数据
        Date oldestDate = DateUtils.addDays(current, -60);

        //60天前的数据不返回
        if (createDate.before(oldestDate)) {
            return Collections.emptySet();
        }
        //本月
        MonthRange monthRange = MonthRange.newInstance(createDate.getTime());

        Set<ScoreCircleGroupContext> contextSet = new HashSet<>();
        //返回结果
        Set<ScoreCircleGroupContext> returnContexts;
        //参数
        ScoreCircleGroupContext parameter = new ScoreCircleGroupContext();
        parameter.setUserId(studentId);

        do {
            parameter.setCreateDate(createDate);
            for (Long id : groupIds) {
                parameter.setGroupId(id);
                ScoreCircleResponse circleResponse = dpScoreCircleLoader.loadGroupCircleByScoreDesc(parameter, 10);
                if (circleResponse.isSuccess()) {
                    circleResponse.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(p -> !p.getCreateDate().before(oldestDate))
                            .forEach(contextSet::add);
                }
                //班级中忽略学科属性的动态
                if (withoutSubject) {
                    ScoreCircleResponse ignoreSubjectCircleResponse = dpScoreCircleLoader.loadGroupIgnoreSubjectCircleByScoreDesc(parameter, 10);
                    if (ignoreSubjectCircleResponse.isSuccess()) {
                        ignoreSubjectCircleResponse.getContextList()
                                .stream()
                                .filter(e -> e instanceof ScoreCircleGroupContext)
                                .map(e -> (ScoreCircleGroupContext) e)
                                .filter(p -> !p.getCreateDate().before(oldestDate))
                                .forEach(contextSet::add);
                    }
                }
            }
            //全局的通知
            if (withoutSubject) {
                ScoreCircleResponse noticeResponse = dpScoreCircleLoader.loadNoticeCircleByScoreDesc(parameter, 10);
                if (noticeResponse.isSuccess()) {
                    noticeResponse.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(e -> e.getUserId() == null)
                            .filter(p -> !p.getCreateDate().before(oldestDate))
                            .forEach(contextSet::add);
                }
                //个人维度
                ScoreCircleResponse personalResponse = dpScoreCircleLoader.loadPersonalCircleByScoreDesc(parameter, 10);
                if (personalResponse.isSuccess()) {
                    personalResponse.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(p -> !p.getCreateDate().before(oldestDate))
                            .forEach(contextSet::add);
                }
            }
            if (needFilter) {
                contextSet = contextSet.stream().filter(e -> GROUP_CIRCLE_TYPES.contains(e.getGroupCircleType())).collect(Collectors.toSet());
            } else {
                //旧版本，如果已经有作业已检查的消息，要把对应的新作业消息排除掉
                Set<String> homeworkCheckTypeIds = contextSet.stream()
                        .filter(e -> e.getGroupCircleType() == GroupCircleType.HOMEWORK_CHECK)
                        .map(ScoreCircleGroupContext::getTypeId)
                        .collect(Collectors.toSet());
                contextSet = contextSet.stream().filter(e -> e.getGroupCircleType() != GroupCircleType.HOMEWORK_NEW || !homeworkCheckTypeIds.contains(e.getTypeId()))
                        .collect(Collectors.toSet());
            }

            ScoreCircleGroupContext activityContext = contextSet.stream()
                    .filter(e -> e.getCommonMessageType() == GroupCircleCommonMessageType.PLAN_WINTER_ACTIVITY)
                    .min(Comparator.comparing(ScoreCircleGroupContext::getCreateDate))
                    .orElse(null);
            returnContexts = contextSet.stream().filter(e -> e.getCommonMessageType() != GroupCircleCommonMessageType.PLAN_WINTER_ACTIVITY).collect(Collectors.toSet());
            if (activityContext != null) {
                returnContexts.add(activityContext);
            }

            if (returnContexts.size() >= 10) {
                break;
            }

            //重新计算时间
            monthRange = monthRange.previous();
            createDate = monthRange.getEndDate();
        } while (!oldestDate.after(createDate));

        return returnContexts;
    }

    private void generateAssignParentHomeworkMessage(Long studentId, Collection<Long> groupIds) {
        ScoreCircleGroupContext topContext = loadTopMessageContext(studentId, GroupCircleTopMessageType.ASSIGN_PARENT_HOMEWORK);
        if (topContext != null) {
            return;
        }
        Date now = new Date();
        String dateStr = DateUtils.dateToString(now, "yyyy-MM-dd");
        Date startDate = DateUtils.stringToDate(dateStr + " 16:00:00");
        Date endDate = DateUtils.stringToDate(dateStr + " 20:30:00");
        if (now.after(startDate) && now.before(endDate)) {
            String homeworkId = newHomeworkReportService.fetchStudentNewestUnfinishedHomework(studentId, groupIds);
            boolean hasAssignParentHomework = dpScoreCircleLoader.hasAssignHomework(studentId);
            if (StringUtils.isBlank(homeworkId) && !hasAssignParentHomework) {
                dpScoreCircleService.generateTopMessage(GroupCircleTopMessageType.ASSIGN_PARENT_HOMEWORK, studentId);
            }
        }
    }

    private List<Map<String, Object>> generateGroupMessageList(List<ScoreCircleGroupContext> contextList,
                                                               Map<String, ScoreCircleGroupContextStoreInfo> storeInfoMap,
                                                               Map<Long, Group> groupMap,
                                                               Map<Long, List<Teacher>> groupTeacher,
                                                               StudentDetail studentDetail) {
        List<Map<String, Object>> messageList = new ArrayList<>();
        if (MapUtils.isNotEmpty(storeInfoMap) && CollectionUtils.isNotEmpty(contextList)) {
            Long parentId = getCurrentParentId();
            Set<String> confirmIds = new HashSet<>();
            contextList.forEach(c -> confirmIds.add(GroupMessageConfirmInfo.generateId(c.getGroupCircleType(), c.getTypeId())));
            Map<String, GroupMessageConfirmInfo> confirmInfoMap = dpConfirmInfoLoader.loadByIds(confirmIds);

            for (ScoreCircleGroupContext context : contextList) {
                ScoreCircleGroupContextStoreInfo storeInfo = storeInfoMap.get(context.generateId());
                if (storeInfo == null || storeInfo.getGroupCircleType() == null) {
                    continue;
                }
                Map<String, Object> messageMap = new HashMap<>();
                GroupCircleType circleType = storeInfo.getGroupCircleType();
                messageMap.put("card_id", storeInfo.getInfoId());

                String subjectName = "";
                Group group = groupMap.get(storeInfo.getGroupId());
                if (group != null && group.getSubject() != null) {
                    subjectName = group.getSubject().name();
                } else if (storeInfo.getSubject() != null) {
                    subjectName = storeInfo.getSubject().name();
                }
                messageMap.put("subject", subjectName);
                Date createDate = context.getCreateDate();
                if (createDate != null) {
                    messageMap.put("create_date", createDate.getTime());
                    messageMap.put("message_date", DateUtils.dateToString(createDate, "MM月dd日"));
                }
                if (context.getTopMessageType() != null) {
                    messageMap.put("message_date", DateUtils.dateToString(storeInfo.getCreateTime(), "置顶"));
                }
                messageMap.put("card_type", circleType);
                messageMap.put("type_id", storeInfo.getTypeId());
                messageMap.put("content_img", storeInfo.getImgUrl());
                messageMap.put("can_request", Boolean.FALSE);
                messageMap.put("button_style", 0);
                setPublisherInfo(messageMap, storeInfo, groupTeacher, studentDetail.getId());
                generateGroupMessageInfo(messageMap, storeInfo, context, confirmInfoMap, parentId, studentDetail);
                Object requestParams = messageMap.get("request_params");
                if (requestParams != null) {
                    messageMap.put("request_params", JsonUtils.toJson(requestParams));
                }
                messageList.add(messageMap);
            }
        }
        return messageList;
    }

    /**
     * 消息标题
     */
    private void generateGroupMessageInfo(Map<String, Object> messageMap,
                                          ScoreCircleGroupContextStoreInfo storeInfo,
                                          ScoreCircleGroupContext context,
                                          Map<String, GroupMessageConfirmInfo> confirmInfoMap,
                                          Long parentId,
                                          StudentDetail studentDetail) {
        GroupCircleType circleType = storeInfo.getGroupCircleType();
        switch (circleType) {
            case COMMON:
                if (context.getTopMessageType() == GroupCircleTopMessageType.ASSIGN_PARENT_HOMEWORK) {
                    generateAssignHomeworkMessageInfo(messageMap, storeInfo);
                } else if (context.getTopMessageType() == null) {
                    generateCommonMessageInfo(messageMap, storeInfo);
                }
                break;
            case HOMEWORK_NEW:
                if (context.getTopMessageType() == GroupCircleTopMessageType.POETRY_CONFERENCE) {
                    generatePoetryConferenceMessageInfo(messageMap, storeInfo, studentDetail.getId());
                } else {
                    generateHomeworkNewMessageInfo(messageMap, storeInfo, studentDetail);
                }
                break;
            case HOMEWORK_CHECK:
                generateHomeworkCheckedMessageInfo(messageMap, storeInfo, confirmInfoMap, parentId, studentDetail);
                break;
            case OFFLINE_HOMEWORK:
                generateOfflineMessageInfo(messageMap, storeInfo, parentId, studentDetail);
                break;
            case HOMEWORK_REPORT:
                generateHomeworkReportMessageInfo(messageMap, storeInfo);
                break;
            case HOMEWORK_WEEK_REPORT:
                generateWeekReportMessageInfo(messageMap, storeInfo);
                break;
            case JXT_NOTICE:
                generateJxtNoticeMessageInfo(messageMap, storeInfo, parentId);
                break;
//            case ELITE_HOMEWORK:
//                Long studentId = storeInfo.getUserId();
//                Student student = studentLoaderClient.loadStudent(studentId);
//                if (student != null && StringUtils.isNotBlank(student.fetchRealname())) {
//                    content = "给" + student.fetchRealname() + "布置了一份同步练习";
//                }
//                break;
        }
    }

    private String generateOcrUrl(NewHomework newHomework, String url) {
        Subject subject = newHomework.getSubject();
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.OCR_DICTATION;
        if (subject == Subject.MATH) {
            objectiveConfigType = ObjectiveConfigType.OCR_MENTAL_ARITHMETIC;
        }
        String params = "homeworkId=" + newHomework.getId() + "&subject=" + subject + "&objectiveConfigType=" + objectiveConfigType;
        String cardUrl;
        //测试环境产生了一些带参数的链接，这里处理一下
        if (url.contains("?")) {
            cardUrl = url.substring(0, url.indexOf("?") + 1) + params;
        } else {
            cardUrl = url + "?" + params;
        }
        return cardUrl;
    }

    private void generateCommonMessageInfo(Map<String, Object> messageMap, ScoreCircleGroupContextStoreInfo storeInfo) {
        messageMap.put("content", storeInfo.getContent());
        messageMap.put("content_img", storeInfo.getImgUrl());
        messageMap.put("button_text", "查看详情");
        messageMap.put("button_url", storeInfo.getLinkUrl());
        messageMap.put("card_url", storeInfo.getLinkUrl());
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("type_id", storeInfo.getTypeId());
        requestParams.put("group_circle_type", storeInfo.getGroupCircleType());
        messageMap.put("request_params", requestParams);
        messageMap.put("request_type", RequestType.READ);
    }

    /**
     * 通用卡片-诗词大会互动置顶卡片
     */
    private void generatePoetryConferenceMessageInfo(Map<String, Object> messageMap, ScoreCircleGroupContextStoreInfo storeInfo, Long studentId) {
        messageMap.put("content", storeInfo.getContent());
        messageMap.put("button_text", "去查看");
        StudentActivityStatistic studentActivityStatistics = ancientPoetryLoader.getStudentActivityStatistics(studentId);
        if (studentActivityStatistics != null) {
            List<Map<String, Object>> extContent = new ArrayList<>();
            int leanPoetryNum = SafeConverter.toInt(studentActivityStatistics.getLeanPoetryNum());
            int noCorrectionNum = SafeConverter.toInt(studentActivityStatistics.getNoCorrectionNum());
            int parentChildNum = SafeConverter.toInt(studentActivityStatistics.getParentChildNum());
            if (leanPoetryNum > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("style", ContentStyle.NORMAL);
                map.put("text", "已学古诗" + leanPoetryNum + "首");
                extContent.add(map);
            }
            if (noCorrectionNum > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("style", ContentStyle.NORMAL);
                map.put("text", "待订正" + noCorrectionNum + "题");
                extContent.add(map);
            }
            if (parentChildNum > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("style", ContentStyle.NORMAL);
                map.put("text", "有" + parentChildNum + "个亲子任务待完成");
                extContent.add(map);
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("style", ContentStyle.NORMAL);
                map.put("text", "所有亲子任务已完成");
                extContent.add(map);
            }
            messageMap.put("ext_info_list_map", extContent);
        }
        messageMap.put("button_url", storeInfo.getLinkUrl());
        messageMap.put("card_url", storeInfo.getLinkUrl());
    }

    /**
     * 通用卡片-布置作业置顶卡片
     */
    private void generateAssignHomeworkMessageInfo(Map<String, Object> messageMap, ScoreCircleGroupContextStoreInfo storeInfo) {
        messageMap.put("content", storeInfo.getContent());
        messageMap.put("button_text", "去布置");
        messageMap.put("publisher_name", "官方推荐");
        messageMap.put("publisher_avatar", getCdnBaseUrlStaticSharedWithSep() + "public/skin/parentMobile/images/new_icon/logo_icon.png");
        messageMap.put("button_url", storeInfo.getLinkUrl());
        messageMap.put("card_url", storeInfo.getLinkUrl());
    }

    /**
     * 班务通知卡片
     */
    private void generateJxtNoticeMessageInfo(Map<String, Object> messageMap,
                                              ScoreCircleGroupContextStoreInfo storeInfo,
                                              Long parentId) {
        JxtNotice jxtNotice = jxtLoaderClient.getRemoteReference().getJxtNoticeById(storeInfo.getTypeId());
        if (jxtNotice != null) {
            messageMap.put("content", storeInfo.getContent());
            if (jxtNotice.getNeedFeedBack()) {
                Map<String, List<JxtFeedBack>> feedBackListByNoticeIds = jxtLoaderClient.getFeedBackListByNoticeIds(Collections.singleton(jxtNotice.getId()));
                List<JxtFeedBack> feedBackList = feedBackListByNoticeIds.containsKey(jxtNotice.getId()) ? feedBackListByNoticeIds.get(jxtNotice.getId()) : new ArrayList<>();
                List<JxtFeedBack> noticeFeedBackList = feedBackList.stream().filter(p -> p.getGroupId().equals(storeInfo.getGroupId())).sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
                Set<Long> hadFeedBackParentIds = noticeFeedBackList.stream().map(JxtFeedBack::getParentId).collect(Collectors.toSet());
                if (hadFeedBackParentIds.contains(parentId)) {
                    messageMap.put("button_style", 1);
                    messageMap.put("button_text", "已确认");
                } else {
                    messageMap.put("button_text", "确认查收");
                }
                messageMap.put("button_url", storeInfo.getLinkUrl());
            }
            messageMap.put("bottom_text", "点击查看详情 >>>");
            messageMap.put("card_url", storeInfo.getLinkUrl());
            messageMap.put("group_id", storeInfo.getGroupId());
        }

    }

    /**
     * 作业周报卡片
     */
    private void generateWeekReportMessageInfo(Map<String, Object> messageMap, ScoreCircleGroupContextStoreInfo storeInfo) {
        messageMap.put("content", getHomeworkWeekReportContent(storeInfo));
        messageMap.put("card_url", storeInfo.getLinkUrl());
    }

    /**
     * 作业日报卡片
     */
    private void generateHomeworkReportMessageInfo(Map<String, Object> messageMap, ScoreCircleGroupContextStoreInfo storeInfo) {
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(storeInfo.getTypeId());
        if (newHomework == null) {
            return;
        }
        String content = "分享咱们班" + DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日") + "的";
        if (newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
            content += "纸质";
        }
        content += "作业情况";

        messageMap.put("content", content);

        boolean haveScore = false;
        for (ObjectiveConfigType objectiveConfigType : newHomework.findPracticeContents().keySet()) {
            if (!NOT_SHOW_SCORE_TYPE.contains(objectiveConfigType)) {
                haveScore = true;
                break;
            }
        }
        List<String> extInfo = new ArrayList<>();
        List<Long> userIds = studentLoaderClient.loadGroupStudentIds(newHomework.getClazzGroupId());
        if (haveScore) {
            Map<Long, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userIds, false);
            Integer totalScore = 0;
            Integer totalCount = 0;
            Integer maxScore = 0;
            for (NewHomeworkResult result : homeworkResultMap.values()) {
                if (result.isFinished()) {
                    Integer score = result.processScore();
                    totalScore += score;
                    totalCount++;
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
            }
            if (totalCount > 0) {
                Integer avgScore = new BigDecimal(totalScore).divide(new BigDecimal(totalCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
                extInfo.add("班级平均分" + ScoreLevel.processLevel(avgScore).getLevel() + "，班级最高分" + ScoreLevel.processLevel(maxScore).getLevel());
            }
        }
        NewAccomplishment newAccomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(newHomework.toLocation());
        int finishCount = 0;
        if (newAccomplishment != null && newAccomplishment.getDetails() != null) {
            finishCount = newAccomplishment.getDetails().size();
        }
        extInfo.add("班级完成率：" + finishCount + "/" + userIds.size());
//        NewHomeworkStudyMaster studyMaster = newHomeworkPartLoader.getNewHomeworkStudyMasterMap(Collections.singleton(newHomework.getId())).get(newHomework.getId());
//        if (studyMaster != null) {
//            if (CollectionUtils.isNotEmpty(studyMaster.getExcellentList())) {
//                Map<Long, Student> studentMap = studentLoaderClient.loadStudents(studyMaster.getExcellentList());
//                Set<String> studentNames = studentMap.values().stream().map(Student::fetchRealname).collect(Collectors.toSet());
//                extInfo.add("本次学科之星：" + StringUtils.join(studentNames, ","));
//            }
//            if (CollectionUtils.isNotEmpty(studyMaster.getFocusList())) {
//                Map<Long, Student> studentMap = studentLoaderClient.loadStudents(studyMaster.getFocusList());
//                Set<String> studentNames = studentMap.values().stream().map(Student::fetchRealname).collect(Collectors.toSet());
//                extInfo.add("本次专注之星：" + StringUtils.join(studentNames, ","));
//            }
//            if (CollectionUtils.isNotEmpty(studyMaster.getPositiveList())) {
//                Map<Long, Student> studentMap = studentLoaderClient.loadStudents(studyMaster.getPositiveList());
//                Set<String> studentNames = studentMap.values().stream().map(Student::fetchRealname).collect(Collectors.toSet());
//                extInfo.add("本次积极之星：" + StringUtils.join(studentNames, ","));
//            }
//        }
        if (newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
            messageMap.put("bottom_text", "具体的拍照结果和错题，请各位家长查收  >");
            messageMap.put("card_url", generateOcrUrl(newHomework, storeInfo.getLinkUrl()));
        } else {
            if (newHomework.subject == Subject.ENGLISH) {
                VoiceRecommend voiceRecommend = voiceRecommendLoader.loadByHomeworkIds(Collections.singleton(newHomework.getId())).get(newHomework.getId());
                if (voiceRecommend != null && CollectionUtils.isNotEmpty(voiceRecommend.getRecommendVoiceList())) {
                    List<String> studentNames = new ArrayList<>();
                    voiceRecommend.getRecommendVoiceList().forEach(e -> studentNames.add(e.getStudentName()));
                    extInfo.add("优秀语音：" + StringUtils.join(studentNames, "、"));
                }

                DubbingRecommend dubbingRecommend = dubbingScoreRecommendLoaderClient.loadByHomeworkIds(Collections.singleton(newHomework.getId())).get(newHomework.getId());
                if (dubbingRecommend != null && CollectionUtils.isNotEmpty(dubbingRecommend.getExcellentDubbingStu())) {
                    List<String> studentNames = new ArrayList<>();
                    dubbingRecommend.getExcellentDubbingStu().forEach(e -> studentNames.add(e.getUserName()));
                    extInfo.add("优秀趣配音：" + StringUtils.join(studentNames, "、"));
                }
            }

            IntelligentTeachingReport intelligentTeachingReport = diagnoseReportService.fetchIntelligentTeachingReport(newHomework.getId());
            if (intelligentTeachingReport != null && intelligentTeachingReport.getHasCourseGraspUserCount() > 0) {
                extInfo.add(intelligentTeachingReport.getHasCourseGraspUserCount() + "人在订正讲解中巩固了不会的知识");
            }
            messageMap.put("bottom_text", "还有一些待提高及错题订正的情况请各位家长重视 >>>");
            messageMap.put("card_url", "/view/mobile/parent/homework/report_detail?tab=clazz&hid=" + storeInfo.getTypeId());
        }
        messageMap.put("ext_info_list_string", extInfo);
    }

    /**
     * 作业单卡片
     */
    private void generateOfflineMessageInfo(Map<String, Object> messageMap, ScoreCircleGroupContextStoreInfo storeInfo, Long parentId, StudentDetail studentDetail) {
        OfflineHomework offlineHomework = offlineHomeworkLoaderClient.loadOfflineHomework(storeInfo.getTypeId());
        if (offlineHomework == null) {
            return;
        }
        messageMap.put("content", "以下补充练习建议家长监督孩子完成");
        messageMap.put("ext_info_img", getCdnBaseUrlStaticSharedWithSep() + "public/skin/parentMobile/images/new_icon/offline_homework.png");
        List<OfflineHomeworkPracticeContent> practiceContents = offlineHomework.getPractices();
        List<String> contents = new ArrayList<>();
        for (int i = 0; i < practiceContents.size(); i++) {
            contents.add((i + 1) + "." + practiceContents.get(i).toString());
        }
        messageMap.put("ext_info_string", StringUtils.join(contents, " "));
        if (offlineHomework.getNeedSign()) {
            Map<String, List<OfflineHomeworkSignRecord>> offlineHomeworkSignMap = jxtLoaderClient.getSignRecordByOfflineHomeworkIds(Collections.singleton(storeInfo.getTypeId()));
            List<OfflineHomeworkSignRecord> homeworkSignRecords = offlineHomeworkSignMap.containsKey(storeInfo.getTypeId()) ? offlineHomeworkSignMap.get(storeInfo.getTypeId()) : new ArrayList<>();
            if (CollectionUtils.isEmpty(homeworkSignRecords) || homeworkSignRecords.stream().noneMatch(p -> Objects.equals(p.getParentId(), parentId) && Objects.equals(p.getStudentId(), studentDetail.getId()))) {
                messageMap.put("button_text", "去签字");
            } else {
                messageMap.put("button_text", "已签字");
                messageMap.put("button_style", 1);
            }
            messageMap.put("button_url", ProductConfig.getMainSiteBaseUrl() + "/view/offlinehomework/detail?needTitle=true&ohids=" + offlineHomework.getId());
        }
        messageMap.put("card_url", storeInfo.getLinkUrl());
    }


    /**
     * 作业已检查卡片
     */
    private void generateHomeworkCheckedMessageInfo(Map<String, Object> messageMap,
                                                    ScoreCircleGroupContextStoreInfo storeInfo,
                                                    Map<String, GroupMessageConfirmInfo> confirmInfoMap,
                                                    Long parentId,
                                                    StudentDetail studentDetail) {
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(storeInfo.getTypeId());
        if (newHomework == null) {
            return;
        }
        Long studentId = studentDetail.getId();
        String content = "请家长查收" + DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日") + "的" + newHomework.getSubject().getValue();
        if (newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
            content += "纸质";
        }
        content += "作业报告";
        NewHomeworkResult homeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, false);
        if (homeworkResult != null && (homeworkResult.getComment() != null || homeworkResult.getAudioComment() != null)) {
            content += "（含老师评语）";
        }
        messageMap.put("content", content);
        messageMap.put("ext_info_img", getCdnBaseUrlStaticSharedWithSep() + "public/skin/parentMobile/images/new_icon/checked_pic.png");
        String cardUrl = storeInfo.getLinkUrl();
        String buttonUrl = storeInfo.getLinkUrl();
        List<Map<String, Object>> homeworkInfoList = new ArrayList<>();
        if (newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
            cardUrl = generateOcrUrl(newHomework, cardUrl);
            buttonUrl = generateOcrUrl(newHomework, cardUrl);
        } else {
            NewHomeworkBook newHomeworkBook = newHomeworkLoaderClient.loadNewHomeworkBook(storeInfo.getTypeId());
            String homeworkContent = newHomeworkBook == null ? "" : "作业内容：" + StringUtils.join(newHomeworkBook.processUnitNameList(), ",");
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("style", ContentStyle.NORMAL);
            contentMap.put("text", homeworkContent);
            homeworkInfoList.add(contentMap);
        }
        Map<String, List<Map<String, Object>>> wrongQuestionIds = newHomeworkLoaderClient.getStudentWrongQuestionIds(studentId, null, Collections.singleton(newHomework.getId()));
        int wrongCount = ParentHomeworkUtil.getWrongCountWithHomeworkId(newHomework.toLocation(), wrongQuestionIds);
        Map<Long, Boolean> repairMap = storeInfo.getStudentIsRepairMap();
        Map<Long, Integer> scoreMap = storeInfo.getStudentScoreMap();
        String finishStatus = "未完成";
        if (MapUtils.isNotEmpty(scoreMap) && scoreMap.get(studentId) != null) {
            Integer score = SafeConverter.toInt(scoreMap.get(studentId));
            boolean showLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            if (showLevel) {
                String level = generateHomeworkSourceLevel(score, studentDetail);
                finishStatus = "成绩：" + level;
            } else {
                finishStatus = "成绩：" + score + "分";
            }
        } else if (MapUtils.isNotEmpty(repairMap) && repairMap.containsKey(studentId)) {
            finishStatus = "已完成";
        }
        if (wrongCount > 0) {
            finishStatus += "，错题：" + wrongCount;
        }
        Map<String, Object> finishMap = new HashMap<>();
        finishMap.put("style", ContentStyle.BOLD);
        finishMap.put("text", finishStatus);
        homeworkInfoList.add(finishMap);
        messageMap.put("ext_info_list_map", homeworkInfoList);
        setParentRewardInfo(messageMap, newHomework, parentId, studentId);

        //确认按钮
        GroupMessageConfirmInfo confirmInfo = confirmInfoMap.get(GroupMessageConfirmInfo.generateId(storeInfo.getGroupCircleType(), storeInfo.getTypeId()));
        ParentGroupMessageStatus status = getMessageStatus(storeInfo.getGroupCircleType(), confirmInfo, parentId, studentId);
        if (status != null) {
            messageMap.put("button_text", status.getTag());
            if (ParentGroupMessageStatus.confirmStatus().contains(status)) {
                messageMap.put("button_style", 1);
            }
        }
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("type_id", storeInfo.getTypeId());
        requestParams.put("group_circle_type", storeInfo.getGroupCircleType());
        messageMap.put("request_params", requestParams);
        messageMap.put("request_type", RequestType.READ);
        messageMap.put("button_url", buttonUrl);
        messageMap.put("card_url", cardUrl);
    }

    /**
     * 新作业消息卡片
     */
    private void generateHomeworkNewMessageInfo(Map<String, Object> messageMap, ScoreCircleGroupContextStoreInfo storeInfo, StudentDetail studentDetail) {
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(storeInfo.getTypeId());
        if (newHomework == null) {
            return;
        }
        String content = DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日") + newHomework.getSubject().getValue();
        if (newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
            content += "纸质";
        }
        content += "作业已布置，请家长督促完成";
        messageMap.put("content", content);
        NewHomeworkBook newHomeworkBook = newHomeworkLoaderClient.loadNewHomeworkBook(storeInfo.getTypeId());
        String bookId = newHomeworkBook != null ? newHomeworkBook.processBookId() : "";
        if (StringUtils.isNotBlank(bookId)) {
            NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
            if (bookProfile != null) {
                String bookCoverImageUrl = NewHomeworkUtils.compressBookImg(bookProfile.getImgUrl());
                messageMap.put("ext_info_img", bookCoverImageUrl);
            }
        }
        String cardUrl = storeInfo.getLinkUrl();
        List<Map<String, Object>> homeworkInfoList = new ArrayList<>();
        if (newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
            cardUrl = generateOcrUrl(newHomework, cardUrl);

            messageMap.put("ext_info_img", getCdnBaseUrlStaticSharedWithSep() + "public/skin/parentMobile/images/new_icon/checked_pic.png");
            Map<ObjectiveConfigType, NewHomeworkPracticeContent> newHomeworkPracticeContents = newHomework.findPracticeContents();
            if (newHomeworkPracticeContents.containsKey(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = newHomeworkPracticeContents.get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
                String workBookName = newHomeworkPracticeContent.getWorkBookName();
                String homeworkDetail = newHomeworkPracticeContent.getHomeworkDetail();
                String[] bookNames = StringUtils.split(workBookName, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR);
                String[] homeworkDetails = StringUtils.split(homeworkDetail, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR);
                List<String> bookNameList = Arrays.asList(bookNames);
                List<String> homeworkDetailList = Arrays.asList(homeworkDetails);
                int length = Integer.max(bookNameList.size(), homeworkDetailList.size());
                for (int i = 0; i < length; i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("style", ContentStyle.NORMAL);
                    map.put("text", bookNameList.get(i) + "：" + homeworkDetailList.get(i));
                    homeworkInfoList.add(map);
                }
            } else if (newHomeworkPracticeContents.containsKey(ObjectiveConfigType.OCR_DICTATION)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = newHomeworkPracticeContents.get(ObjectiveConfigType.OCR_DICTATION);
                List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkPracticeContent.getQuestions();
                Set<String> lessonIds = newHomeworkQuestions.stream()
                        .map(NewHomeworkQuestion::getQuestionBoxId)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
                List<String> lessonNames = lessonMap.values().stream().map(NewBookCatalog::getAlias).collect(Collectors.toList());
                Map<String, Object> map = new HashMap<>();
                map.put("style", ContentStyle.NORMAL);
                map.put("text", "听写：" + StringUtils.join(lessonNames, "、"));
                homeworkInfoList.add(map);
            }
        } else {
            String homeworkContent = newHomeworkBook == null ? "" : StringUtils.join(newHomeworkBook.processUnitNameList(), ",");
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("style", ContentStyle.NORMAL);
            contentMap.put("text", homeworkContent);
            homeworkInfoList.add(contentMap);

            Map<Long, Boolean> repairMap = storeInfo.getStudentIsRepairMap();
            Map<Long, Integer> scoreMap = storeInfo.getStudentScoreMap();
            String finishStatus = "";
            if (MapUtils.isNotEmpty(scoreMap) && scoreMap.get(studentDetail.getId()) != null) {
                Integer score = SafeConverter.toInt(scoreMap.get(studentDetail.getId()));
                boolean showLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
                if (showLevel) {
                    String level = generateHomeworkSourceLevel(score, studentDetail);
                    finishStatus = "成绩：" + level;
                } else {
                    finishStatus = "成绩：" + score + "分";
                }
            } else if (MapUtils.isNotEmpty(repairMap) && repairMap.containsKey(studentDetail.getId())) {
                finishStatus = "已完成";
            }
            if (StringUtils.isNotBlank(finishStatus)) {
                Map<String, Object> gradeMap = new HashMap<>();
                gradeMap.put("style", ContentStyle.NORMAL);
                gradeMap.put("text", finishStatus);
                homeworkInfoList.add(gradeMap);
            } else {
                Long duration = ((newHomework.getDuration() % 60) > 0) ? (newHomework.getDuration() / 60) + 1 : newHomework.getDuration() / 60;
                Map<String, Object> durationMap = new HashMap<>();
                durationMap.put("style", ContentStyle.NORMAL);
                durationMap.put("text", "预计用时：" + duration + "分钟");
                homeworkInfoList.add(durationMap);
            }

            OfflineHomework offlineHomework = offlineHomeworkLoaderClient.loadByNewHomeworkIds(Collections.singleton(storeInfo.getTypeId()))
                    .get(storeInfo.getTypeId());
            if (offlineHomework != null && CollectionUtils.isNotEmpty(offlineHomework.getPractices())) {
                Map<String, Object> offlineMap = new HashMap<>();
                offlineMap.put("style", ContentStyle.SMALL);
                offlineMap.put("text", "*本次作业含线下练习，需家长监督");
                homeworkInfoList.add(offlineMap);
            }
            if (newHomework.getSubject() == Subject.MATH) {
                boolean hasOCRMentalArithmetic = newHomework.findPracticeContents().containsKey(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
                if (hasOCRMentalArithmetic) {
                    Map<String, Object> orcMap = new HashMap<>();
                    orcMap.put("style", ContentStyle.SMALL);
                    orcMap.put("text", "*本次作业含纸质拍照，需拍照上传");
                    homeworkInfoList.add(orcMap);
                }
            }
        }
        messageMap.put("ext_info_list_map", homeworkInfoList);
        setFlowerButtonInfo(messageMap, newHomework, studentDetail.getId());
        messageMap.put("card_url", cardUrl);
    }

    private void setFlowerButtonInfo(Map<String, Object> messageMap, NewHomework newHomework, Long studentId) {
        Flower flower = flowerServiceClient.getFlowerService().loadHomeworkFlowers(newHomework.getId()).getUninterruptibly()
                .stream()
                .filter(f -> Objects.equals(studentId, f.getSenderId()))
                .findFirst()
                .orElse(null);

        if (flower == null) {
            messageMap.put("button_text", "给老师点赞");
            messageMap.put("can_request", true);
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("homework_id", newHomework.getId());
            messageMap.put("request_params", requestParams);
            messageMap.put("request_type", RequestType.FLOWER);
        } else {
            messageMap.put("button_text", "已点赞");
//            String flowerDetailUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/send_flower/index.vpage?group_id=" + newHomework.getClazzGroupId();
//            messageMap.put("button_url", flowerDetailUrl);
            messageMap.put("button_style", 1);
        }


    }

    private void setParentRewardInfo(Map<String, Object> messageMap, NewHomework newHomework, Long parentId, Long studentId) {
        ParentRewardLog rewardLog = parentRewardLoader.getHomeworkRewardLog(studentId, newHomework.getId());
        //0-没奖励，1-有奖励可发，2-有奖励不可发或者已发
        int rewardStatus = 0;
        if (rewardLog != null) {
            String buttonText;
            String rewardDetailUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/rewards/detail.vpage?ref=hwReport";
            if (rewardLog.getStatus() == 0) {
                buttonText = "鼓励孩子";
                if (parentRewardService.rewardSendAvailable(parentId, studentId,isParentRewardNewVersionForFaceDetect(getClientVersion()))) {
                    rewardStatus = 1;
                    Map<String, Object> sendRewardInfoMap = new HashMap<>();
                    sendRewardInfoMap.put("id", rewardLog.getId());
                    sendRewardInfoMap.put("key", rewardLog.getKey());
                    sendRewardInfoMap.put("type", rewardLog.getType());
                    sendRewardInfoMap.put("count", rewardLog.getCount());
                    messageMap.put("reward_request_params", JsonUtils.toJson(sendRewardInfoMap));
                    messageMap.put("reward_request_type", RequestType.PARENT_REWARD);
                } else {
                    rewardStatus = 2;
                    messageMap.put("reward_detail_url", rewardDetailUrl);
                }
            } else {
                rewardStatus = 2;
                buttonText = "已鼓励";
                messageMap.put("reward_detail_url", rewardDetailUrl);
            }
            messageMap.put("reward_button_text", buttonText);
        }
        messageMap.put("reward_status", rewardStatus);
    }

    public enum ContentStyle {
        NORMAL,
        SMALL,
        BOLD
    }

    public enum RequestType {
        PARENT_REWARD,
        FLOWER,
        READ
    }

    /**
     * 消息发布者的头像的称呼
     */
    private void setPublisherInfo(Map<String, Object> messageMap, ScoreCircleGroupContextStoreInfo storeInfo, Map<Long, List<Teacher>> groupTeacher, Long studentId) {
        String publisherName = "";
        String publisherAvatar = "";
        if (storeInfo.getGroupCircleType() == GroupCircleType.ELITE_HOMEWORK) {
            publisherName = storeInfo.getPublishUserName();
            User user = raikouSystem.loadUser(storeInfo.getPublishUserId());
            if (user != null) {
                StudentParentRef studentParentRef = studentLoaderClient.loadStudentParentRefs(studentId)
                        .stream()
                        .filter(ref -> ref.getParentId().equals(user.getId()))
                        .findFirst()
                        .orElse(null);
                if (studentParentRef != null) {
                    String callName = studentParentRef.getCallName();
                    publisherName += callName;
                }
            }
            publisherAvatar = user == null ? getUserAvatarImgUrl("") : getUserAvatarImgUrl(user.fetchImageUrl());
        } else {
            List<Teacher> teachers = groupTeacher.get(storeInfo.getGroupId());
            if (CollectionUtils.isNotEmpty(teachers)) {
                Teacher teacher = teachers.get(0);
                publisherName = StringUtils.isEmpty(teacher.fetchRealname()) ? "" : teacher.fetchRealname() + "老师";
                publisherAvatar = getUserAvatarImgUrl(teacher);
            }
        }
        messageMap.put("publisher_name", publisherName);
        messageMap.put("publisher_avatar", publisherAvatar);
    }

    /**
     * ScoreCircleGroupContextStoreInfo数据转换成Map
     *
     * @param storeInfoMap 消息
     * @param groupMap     学科
     * @param groupTeacher 学科老师
     * @param studentId    学生ID
     * @param time         翻页日期控制
     * @return 返回map列表
     */
    private List<Map<String, Object>> convertResult(List<ScoreCircleGroupContext> contextList,
                                                    Map<String, ScoreCircleGroupContextStoreInfo> storeInfoMap,
                                                    Map<Long, Group> groupMap,
                                                    Map<Long, List<Teacher>> groupTeacher,
                                                    long studentId,
                                                    String time) {
        if (MapUtils.isEmpty(storeInfoMap) || CollectionUtils.isEmpty(contextList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> contextResultList = new ArrayList<>(storeInfoMap.size());

        Long parentId = getCurrentParentId();

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);

        Set<String> confirmIds = new HashSet<>();
        contextList.forEach(p -> confirmIds.add(GroupMessageConfirmInfo.generateId(p.getGroupCircleType(), p.getTypeId())));
        Map<String, GroupMessageConfirmInfo> confirmInfoMap = dpConfirmInfoLoader.loadByIds(confirmIds);

        //域名
        String hostname = getCdnBaseUrlStaticSharedWithSep();

        //针对同一个typeId可能存在多张卡片。比如作业报告分享。所以这里只能遍历contextList了
        for (ScoreCircleGroupContext context : contextList) {
            ScoreCircleGroupContextStoreInfo storeInfo = storeInfoMap.get(context.generateId());

            if (storeInfo == null || storeInfo.getGroupCircleType() == null) {
                continue;
            }
            //是母亲节的卡。但学生又未完成。跳过
            //儿童节卡片也类似
            if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_MOTHERS_DAY || storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_KIDS_DAY) {
                if (MapUtils.isEmpty(storeInfo.getStudentScoreMap()) || !storeInfo.getStudentScoreMap().containsKey(studentId)) {
                    continue;
                }
            }
            //定义卡片对象
            Map<String, Object> contentResult = new LinkedHashMap<>();

            //类型
            GroupCircleType circleType = storeInfo.getGroupCircleType();
            //加入id
            contentResult.put(RES_CARD_TYPE_ID, storeInfo.getTypeId());

            //时间处理，和上一张卡片时间不同加入时间，否则忽略
            Date createDate = context.getCreateDate();
            if (createDate != null) {
                contentResult.put(RES_CARD_CREATE_DATE, createDate.getTime());
                String createDateStr = DateUtils.dateToString(createDate, "MM月dd日");
                if (!StringUtils.equals(time, createDateStr)) {
                    contentResult.put(RES_CARD_SHOW_DATE, createDateStr);
                    time = createDateStr;
                }
            }

            //学科 无学科问题和学科非语数外问题========
            Group group = groupMap.get(storeInfo.getGroupId());
            if (storeInfo.getGroupCircleType() == GroupCircleType.ELITE_HOMEWORK) {
                if (storeInfo.getSubject() != null) {
                    String iconUrl = subjectIcon.get(storeInfo.getSubject());
                    contentResult.put(RES_CARD_SUBJECT, hostname + iconUrl);
                }
            } else if (group != null && GroupCircleType.specificSubjectTypes().contains(storeInfo.getGroupCircleType())) {
                Subject subject = group.getSubject();
                if (subject != null) {
                    String iconUrl = subjectIcon.get(subject);
                    contentResult.put(RES_CARD_SUBJECT, hostname + iconUrl);
                }
            }

            //左上角标题
            contentResult.put(RES_CARD_TITLE, storeInfo.getGroupCircleType().getLeftTopTag());

            contentResult.put(RES_CARD_TYPE_NAME, circleType.toString());

            //右上角状态
            GroupMessageConfirmInfo confirmInfo = confirmInfoMap.get(GroupMessageConfirmInfo.generateId(storeInfo.getGroupCircleType(), storeInfo.getTypeId()));

            //阅读或者确认的家长数量
            int readOrConfirmCount = confirmInfo != null && CollectionUtils.isNotEmpty(confirmInfo.getConfirmUserIds()) ? confirmInfo.getConfirmUserIds().size() : 0;
            //处理状态
            ParentGroupMessageStatus status = getMessageStatus(circleType, confirmInfo, parentId, studentId);
            if (status != ParentGroupMessageStatus.UNKNOWN) {
                if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_CHECK && status == ParentGroupMessageStatus.UNCONFIRMED) {
                    contentResult.put(RES_CARD_TOP_TAG, "确认查收");
                    contentResult.put(RES_CARD_TOP_TAG_COLOR, "#FC6C4F");
                } else {
                    contentResult.put(RES_CARD_TOP_TAG, status.getTag());
                    contentResult.put(RES_CARD_TOP_TAG_COLOR, status.getColor());
                }
            }

            //已读已确认
            boolean canConfirm = !ParentGroupMessageStatus.confirmStatus().contains(status) && !circleType.isNeedBusinessConfirm();
            contentResult.put(RES_CARD_CAN_CONFIRM, canConfirm);
            //有业务确认且未确认。客户端打开h5之后返回需要reload
            contentResult.put(RES_CARD_NEED_RELOAD, ParentGroupMessageStatus.unConfirmStatus().contains(status) && circleType.isNeedBusinessConfirm());

            contentResult.put(RES_CARD_CONTENT_IMG, storeInfo.getImgUrl());
            //中间内容
            String content = storeInfo.getContent();
            //作业完成情况 0、未做作业；1、完成作业；2、补做作业；4、有分数
            int finish = 0;
            if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_CHECK || storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_NEW) {
                Map<Long, Boolean> repairMap = storeInfo.getStudentIsRepairMap();
                //完成作业
                if (MapUtils.isNotEmpty(repairMap) && repairMap.containsKey(studentId)) {
                    finish = (finish | 1);
                    Boolean makeUp = repairMap.get(studentId);

                    if (makeUp != null && makeUp) {
                        //补做完成
                        finish = (finish | 2);
                    }
                    Map<Long, Integer> scoreMap = storeInfo.getStudentScoreMap();

                    content = StringUtils.isEmpty(content) ? "" : content + "<br/>";
                    if (MapUtils.isNotEmpty(scoreMap)) {
                        Integer score = scoreMap.get(studentId);
                        if (score != null) {
                            //作业有分数
                            content += "孩子的成绩：<span style='color:#2984F2;font-size:24px;'><font  color='#2984F2'>"
                                    + generateHomeworkSourceLevel(score, studentDetail)
                                    + "</font></span>";
                            finish = (finish | 4);
                        }
                    }
                    if ((finish & 4) != 4) {
                        content += "作业已完成";
                    }
                }
            } else if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_MOTHERS_DAY) {
                //最前面已经判断过完成状态了。直接生成文案即可
                content = "您的孩子" + studentDetail.fetchRealname() + "送您的母亲节礼物，祝您母亲节快乐～";
            } else if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_KIDS_DAY) {
                content = "您的孩子" + studentDetail.fetchRealname() + "完成了儿童节趣味作业，快去奖励孩子并发送鼓励吧";
            }

            //内容扩展
            List<ScoreCircleGroupContextStoreInfo.ExtStoreInfo> extInfoList = storeInfo.getExtInfoList();
            List<Map<String, String>> extResultList = new ArrayList<>();

            if (CollectionUtils.isNotEmpty(extInfoList)) {
                extInfoList.forEach(extStoreInfo -> {
                    if (StringUtils.isNotEmpty(extStoreInfo.getContent())) {
                        Map<String, String> extInfoMap = new HashMap<>(2);
                        GroupCircleExtType extType = extStoreInfo.getExtType();
                        if (extType != null) {
                            extInfoMap.put(RES_CARD_EXT_COLOR, extStoreInfo.getExtType().getContentColor());
                            extInfoMap.put(RES_CARD_EXT_ICON, hostname + extStoreInfo.getExtType().getIcon());
                        }
                        extInfoMap.put(RES_CARD_EXT_TITLE, extStoreInfo.getContent());
                        extResultList.add(extInfoMap);
                    }
                });
            }

            //作业已检查的特殊处理
            if (circleType == GroupCircleType.HOMEWORK_CHECK) {
                String title = null;
                if (finish == 0) {
                    title = "本次作业未完成，请家长督促补做";
                } else if ((finish & 2) == 2) {
                    title = "补做完成";
                }
                if (StringUtils.isNotEmpty(title)) {
                    wrapExt(extResultList, title,
                            GroupCircleExtType.EXPIRE_DATE.getContentColor(),
                            hostname + GroupCircleExtType.EXPIRE_DATE.getIcon());
                }

            } else if (circleType == GroupCircleType.JXT_NOTICE
                    || circleType == GroupCircleType.EXPAND_HOMEWORK) {
                if (readOrConfirmCount > 0) {
                    wrapExt(extResultList,
                            readOrConfirmCount + "位家长" + status.getTotalTag(),
                            GroupCircleExtType.USER_COUNT.getContentColor(),
                            hostname + GroupCircleExtType.USER_COUNT.getIcon());
                } else if (status == ParentGroupMessageStatus.UNCONFIRMED) {
                    wrapExt(extResultList,
                            "去第一个确认",
                            GroupCircleExtType.USER_COUNT.getContentColor(),
                            hostname + GroupCircleExtType.USER_COUNT.getIcon());
                } else if (status == ParentGroupMessageStatus.UNCHECKED) {
                    wrapExt(extResultList,
                            "去第一个检查",
                            GroupCircleExtType.USER_COUNT.getContentColor(),
                            hostname + GroupCircleExtType.USER_COUNT.getIcon());
                }
            }

            contentResult.put(RES_CARD_CONTENT, content);
            contentResult.put(RES_CARD_EXT_INFO, extResultList);


            //老师，业务上老师只有1个，所以只取第1条
            if (storeInfo.getGroupCircleType() == GroupCircleType.ELITE_HOMEWORK) {
                contentResult.put(RES_TEACHER_NAME, storeInfo.getPublishUserName() + "发布");
                User user = raikouSystem.loadUser(storeInfo.getPublishUserId());
                contentResult.put(RES_TEACHER_AVATAR, user == null ? getUserAvatarImgUrl("") : getUserAvatarImgUrl(user.fetchImageUrl()));
            } else {
                List<Teacher> teachers = groupTeacher.get(storeInfo.getGroupId());
                if (CollectionUtils.isNotEmpty(teachers)) {
                    Teacher teacher = teachers.get(0);
                    //老师名称显示第一个字符，忽略复姓问题，如欧阳显示欧。
                    String teacherName
                            = (teacher == null || StringUtils.isEmpty(teacher.fetchRealname()))
                            ? ""
                            : teacher.fetchRealname().substring(0, 1);
                    contentResult.put(RES_TEACHER_NAME, teacherName + "老师发布");
                    contentResult.put(RES_TEACHER_AVATAR, getUserAvatarImgUrl(teacher));
                }
            }

            //右下角按钮。没有连接不显示按钮
            if (StringUtils.isNotBlank(storeInfo.getLinkUrl())) {
                contentResult.put(RES_CARD_BTN_TAG, circleType.getRightBottomTag());
                contentResult.put(RES_CARD_LINK_URL, storeInfo.getLinkUrl());
            }


            //加入卡片
            contextResultList.add(contentResult);
        }
        return contextResultList;
    }

    /**
     * 学生成绩显示等级或者分数
     *
     * @param source        学生分数
     * @param studentDetail 学生信息
     * @return 返回字符串
     */
    private String generateHomeworkSourceLevel(Integer source, StudentDetail studentDetail) {
        if (source == null || studentDetail == null) {
            return "";
        }

        if (grayFunctionManagerClient
                .getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(studentDetail,
                        "ShowScoreLevel",
                        "WhiteList")) {

            return ScoreLevel.processLevel(source).getLevel();
        }

        return String.valueOf(source);
    }


    /**
     * 获取学生学科id
     *
     * @param studentId 学生id
     * @param groupName 学科名称
     * @return 返回学科id
     */
    private Collection<Long> getGroupIds(Long studentId, String groupName) {
        //获取学生学科
        Set<Long> studentIds = new HashSet<>();
        studentIds.add(studentId);

        //获取学生课程
        Map<Long, List<GroupMapper>> mapper = deprecatedGroupLoaderClient.loadStudentGroups(studentIds,
                false);
        if (MapUtils.isEmpty(mapper)) {
            return Collections.emptyList();
        }
        List<GroupMapper> studentGroups = mapper.get(studentId);
        if (CollectionUtils.isEmpty(studentGroups)) {
            return Collections.emptyList();
        }

        if (StringUtils.isEmpty(groupName)) {
            return studentGroups
                    .stream()
                    .map(GroupMapper::getId)
                    .collect(Collectors.toList());
        }

        List<GroupMapper> single = studentGroups
                .stream()
                .filter(x -> StringUtils.equalsIgnoreCase(x.getSubject().name(), groupName))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(single)) {
            return studentGroups
                    .stream()
                    .map(GroupMapper::getId)
                    .collect(Collectors.toList());
        } else {
            return single
                    .stream()
                    .map(GroupMapper::getId)
                    .collect(Collectors.toList());
        }
    }

    private void wrapExt(List<Map<String, String>> list, String title, String color, String icon) {
        Map<String, String> map = new HashMap<>();
        map.put(RES_CARD_EXT_TITLE, title);
        map.put(RES_CARD_EXT_COLOR, color);
        map.put(RES_CARD_EXT_ICON, icon);
        list.add(map);
    }

    //处理卡片状态
    private ParentGroupMessageStatus getMessageStatus(GroupCircleType circleType, GroupMessageConfirmInfo confirmInfo, Long parentId, Long studentId) {
        int confirmStatus = circleType.getUnConfirmStatus();
        if (confirmInfo != null && CollectionUtils.isNotEmpty(confirmInfo.getConfirmUserIds())) {
            //当前家长已确认
            if (confirmInfo.getConfirmUserIds().contains(parentId)) {
                confirmStatus = circleType.getConfirmStatus();
            } else {
                //其他家长已确认
                List<StudentParent> parents = parentLoaderClient.getParentLoader()
                        .loadStudentParents(studentId);
                if (CollectionUtils.isNotEmpty(parents)) {
                    Collection<Long> studentParentIds = parents.stream().map(StudentParent::getParentUser)
                            .map(User::getId)
                            .collect(Collectors.toSet());
                    if (CollectionUtils.containsAny(confirmInfo.getConfirmUserIds(), studentParentIds)) {
                        confirmStatus = circleType.getConfirmStatus();
                    }
                }
            }
        }
        return ParentGroupMessageStatus.parse(confirmStatus);
    }
}
