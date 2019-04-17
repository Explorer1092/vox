package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.AncientPoetryStudentGlobalStar;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryRegister;
import com.voxlearning.utopia.service.newhomework.api.service.AncientPoetryService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.cache.AncientPoetryGlobalRankCacheManager;
import com.voxlearning.utopia.service.newhomework.consumer.cache.ViewPoetryActivityCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.AncientPoetryStudentGlobalStarPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryActivityDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryMissionDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryRegisterDao;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.AncientPoetryActivityPublisher;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry.AncientPoetryResultProcessor;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */

@Named
@Service(interfaceClass = AncientPoetryService.class)
@ExposeService(interfaceClass = AncientPoetryService.class)
public class AncientPoetryServiceImpl extends SpringContainerSupport implements AncientPoetryService {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private AncientPoetryActivityDao ancientPoetryActivityDao;
    @Inject private AncientPoetryMissionDao ancientPoetryMissionDao;
    @Inject private AncientPoetryRegisterDao ancientPoetryRegisterDao;
    @Inject private AncientPoetryResultProcessor ancientPoetryResultProcessor;
    @Inject private AncientPoetryActivityPublisher ancientPoetryActivityPublisher;
    @Inject private AncientPoetryStudentGlobalStarPersistence ancientPoetryStudentGlobalStarPersistence;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;

    @Override
    public MapMessage registerPoetryActivity(Long teacherId, String activityId, Long clazzGroupId) {
        AncientPoetryActivity poetryActivity = ancientPoetryActivityDao.load(activityId);
        if (poetryActivity == null) {
            return MapMessage.errorMessage("活动不存在");
        }

        AncientPoetryRegister register = new AncientPoetryRegister();
        register.setId(AncientPoetryRegister.generateId(poetryActivity.getCreateAt(), activityId, clazzGroupId));
        register.setActivityId(activityId);
        register.setClazzGroupId(clazzGroupId);
        register.setTeacherId(teacherId);
        Date currentDate = new Date();
        register.setCreateAt(currentDate);
        register.setUpdateAt(currentDate);
        register.setBeenCanceled(false);
        ancientPoetryRegisterDao.upsert(register);

        // 增加参与人数
        int groupStudentSize = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupId(clazzGroupId)
                .size();

        poetryActivity.setJoinCount(poetryActivity.getJoinCount() + groupStudentSize);
        ancientPoetryActivityDao.upsert(poetryActivity);

        // 后处理
        AlpsThreadPool.getInstance().submit(() -> {
            String content = "亲子诗词大会火热进行中，老师已经报名，赶快闯关学习古诗吧！";
            String link = UrlUtils.buildUrlQuery(NewHomeworkConstants.STUDENT_ANCIENT_POETRY_ACTIVITY_URL, MapUtils.m("activityId", activityId));
            List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(clazzGroupId);
            appMessageServiceClient.sendAppJpushMessageByIds(
                    content,
                    AppMessageSource.STUDENT,
                    studentIds,
                    MapUtils.m("s", StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getType(), "link", link, "t", "h5", "key", "j",
                            "title", StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getDescription()));
            // 给家长端发kafka消息
            registerPoetryActivityPublishMessage(poetryActivity, clazzGroupId, teacherId);
        });
        return MapMessage.successMessage("报名成功");
    }

    private void registerPoetryActivityPublishMessage(AncientPoetryActivity activity, Long groupId, Long teacherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", HomeworkPublishMessageType.assign);
        map.put("groupId", groupId);
        map.put("activityId", activity.getId());
        map.put("subject", Subject.CHINESE);
        map.put("teacherId", teacherId);
        map.put("startTime", activity.getStartDate().getTime());
        map.put("endTime", activity.getEndDate().getTime());
        map.put("activityName", activity.getName());
        map.put("linkUrl", "/view/mobile/parent/poetry");
        ancientPoetryActivityPublisher.getPoetryAssignPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
    }

    @Override
    public MapMessage processResult(AncientPoetryProcessContext poetryProcessContext) {
        try {
            AncientPoetryProcessContext context = ancientPoetryResultProcessor.process(poetryProcessContext);
            return context.transform().add("result", context.getResult());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }


    @Override
    public MapMessage viewActivity(Long teacherId) {
        ViewPoetryActivityCacheManager cacheManager = newHomeworkCacheService.getViewPoetryActivityCacheManager();
        if (cacheManager.add(teacherId, null)) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    @Override
    public MapMessage updateActivityStatus(String activityId, boolean status) {
        AncientPoetryActivity activity = ancientPoetryActivityDao.load(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("活动不存在");
        }
        if (status == activity.getDisabled()) {
            return MapMessage.successMessage();
        }
        ancientPoetryActivityDao.updateActivityStatus(activityId, status);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage upsertAncientPoetryActivity(AncientPoetryActivity ancientPoetryActivity) {
        ancientPoetryActivityDao.upsert(ancientPoetryActivity);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage upsertAncientPoetryMission(AncientPoetryMission ancientPoetryMission) {
        Date currentDate = new Date();
        ancientPoetryMission.setUpdateDate(currentDate);

        if (StringUtils.isNotBlank(ancientPoetryMission.getId())) {
            ancientPoetryMissionDao.upsert(ancientPoetryMission);
        } else {
            ancientPoetryMission.setId(AncientPoetryMission.generateId());
            ancientPoetryMission.setCreateDate(currentDate);
            ancientPoetryMissionDao.insert(ancientPoetryMission);
        }
        return MapMessage.successMessage().add("id", ancientPoetryMission.getId());
    }

    @Override
    public MapMessage insertsAncientPoetryMission(String jsonStr) {
        List<AncientPoetryMission> poetryMissions = JSON.parseArray(jsonStr, AncientPoetryMission.class);
        if (CollectionUtils.isNotEmpty(poetryMissions)) {
            Date currentDate = new Date();
            for (AncientPoetryMission poetryMission : poetryMissions) {
                poetryMission.setId(AncientPoetryMission.generateId());
                poetryMission.setCreateDate(currentDate);
                poetryMission.setUpdateDate(currentDate);
                ancientPoetryMissionDao.insert(poetryMission);
            }
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    @Override
    public void generateGlobalRankBySchoolIdAndClazzLevel(Long schoolId, Integer clazzLevel) {
        List<AncientPoetryStudentGlobalStar> globalStarList = ancientPoetryStudentGlobalStarPersistence.loadBySchoolIdAndClazzLevel(schoolId, clazzLevel, 0);
        if (CollectionUtils.isNotEmpty(globalStarList)) {
            AncientPoetryGlobalRankCacheManager cacheManager = newHomeworkCacheService.getAncientPoetryGlobalRankCacheManager();

            // 获取所有的学生
            List<Long> studentIds = globalStarList.stream().map(AncientPoetryStudentGlobalStar::getId).collect(Collectors.toList());
            Map<Long, StudentDetail> studentDetailMap = loadAllStudentDetails(studentIds);

            // 把前20名的数据放入总榜缓存
            List<AncientPoetryStudentGlobalStar> topTwentyGlobalStarList = globalStarList.stream().limit(20).collect(Collectors.toList());
            List<Map<String, Object>> globalCacheMapperList = buildCacheMapperList(topTwentyGlobalStarList, studentDetailMap);
            String globalRankCacheKey = cacheManager.getSchoolGlobalRankCacheKey(schoolId, clazzLevel);
            cacheManager.set(globalRankCacheKey, globalCacheMapperList);

            Map<Long, School> schoolMap = raikouSystem.loadSchoolsIncludeDisabled(Collections.singleton(schoolId));
            // 获取到的每条数据都放入学生缓存
            for (int rank = 1; rank <= topTwentyGlobalStarList.size(); rank++) {
                AncientPoetryStudentGlobalStar studentGlobalStar = topTwentyGlobalStarList.get(rank - 1);
                Long studentId = studentGlobalStar.getId();
                StudentDetail studentDetail = studentDetailMap.get(studentId);
                Map<String, Object> cacheMapper = buildCacheMapper(studentGlobalStar, rank, studentDetail, schoolMap);
                String studentRankCacheKey = cacheManager.getSchoolStudentRankCacheKey(schoolId, clazzLevel, studentId);
                cacheManager.set(studentRankCacheKey, Collections.singletonList(cacheMapper));
            }
        }
    }

    @Override
    public void generateGlobalRankByRegionIdAndClazzLevel(Integer regionId, Integer clazzLevel) {
        List<AncientPoetryStudentGlobalStar> globalStarList = ancientPoetryStudentGlobalStarPersistence.loadByRegionIdAndClazzLevel(regionId, clazzLevel, 0);
        if (CollectionUtils.isNotEmpty(globalStarList)) {
            AncientPoetryGlobalRankCacheManager cacheManager = newHomeworkCacheService.getAncientPoetryGlobalRankCacheManager();

            // 获取所有的学生
            List<Long> studentIds = globalStarList.stream().map(AncientPoetryStudentGlobalStar::getId).collect(Collectors.toList());
            Map<Long, StudentDetail> studentDetailMap = loadAllStudentDetails(studentIds);

            // 把前20名的数据放入总榜缓存
            List<AncientPoetryStudentGlobalStar> topTwentyGlobalStarList = globalStarList.stream().limit(20).collect(Collectors.toList());
            List<Map<String, Object>> globalCacheMapperList = buildCacheMapperList(topTwentyGlobalStarList, studentDetailMap);
            String globalRankCacheKey = cacheManager.getRegionGlobalRankCacheKey(regionId, clazzLevel);
            cacheManager.set(globalRankCacheKey, globalCacheMapperList);

            List<Long> schoolIds = studentDetailMap.values().stream().filter(student -> student.getClazz() != null).map(student -> student.getClazz().getSchoolId()).collect(Collectors.toList());
            Map<Long, School> schoolMap = raikouSystem.loadSchoolsIncludeDisabled(schoolIds);
            // 获取到的每条数据都放入学生缓存
            for (int rank = 1; rank <= topTwentyGlobalStarList.size(); rank++) {
                AncientPoetryStudentGlobalStar studentGlobalStar = topTwentyGlobalStarList.get(rank - 1);
                Long studentId = studentGlobalStar.getId();
                StudentDetail studentDetail = studentDetailMap.get(studentId);
                Map<String, Object> cacheMapper = buildCacheMapper(studentGlobalStar, rank, studentDetail, schoolMap);
                String studentRankCacheKey = cacheManager.getRegionStudentRankCacheKey(regionId, clazzLevel, studentId);
                cacheManager.set(studentRankCacheKey, Collections.singletonList(cacheMapper));
            }
        }
    }

    private Map<Long, StudentDetail> loadAllStudentDetails(List<Long> studentIds) {
        Map<Long, StudentDetail> studentDetailMap = new HashMap<>();
        List<List<Long>> studentIdsList = CollectionUtils.splitList(studentIds, 1000);
        for (List<Long> tempStudentIds : studentIdsList) {
            studentDetailMap.putAll(studentLoaderClient.loadStudentDetails(tempStudentIds));
        }
        return studentDetailMap;
    }

    private List<Map<String, Object>> buildCacheMapperList(List<AncientPoetryStudentGlobalStar> globalStarList, Map<Long, StudentDetail> studentDetailMap) {
        List<Long> schoolIds = studentDetailMap.values().stream().filter(student -> student.getClazz() != null).map(student -> student.getClazz().getSchoolId()).collect(Collectors.toList());
        Map<Long, School> schoolMap = raikouSystem.loadSchoolsIncludeDisabled(schoolIds);
        List<Map<String, Object>> cacheMapperList = new ArrayList<>();
        for (int rank = 1; rank <= globalStarList.size(); rank++) {
            AncientPoetryStudentGlobalStar studentGlobalStar = globalStarList.get(rank - 1);
            StudentDetail studentDetail = studentDetailMap.get(studentGlobalStar.getId());
            Map<String, Object> cacheMapper = buildCacheMapper(studentGlobalStar, rank, studentDetail, schoolMap);
            cacheMapperList.add(cacheMapper);
        }
        return cacheMapperList;
    }

    private Map<String, Object> buildCacheMapper(AncientPoetryStudentGlobalStar studentGlobalStar, int rank, StudentDetail studentDetail, Map<Long, School> schoolMap) {
        String schoolName = studentDetail.getStudentSchoolName();
        if (studentDetail.getClazz() != null && schoolMap.containsKey(studentDetail.getClazz().getSchoolId())) {
            schoolName = schoolMap.get(studentDetail.getClazz().getSchoolId()).getShortName();
        }
        return MapUtils.m(
                "studentId", studentGlobalStar.getId(),
                "studentName", studentDetail.fetchRealname(),
                "studentImage", NewHomeworkUtils.getUserAvatarImgUrl("", studentDetail.fetchImageUrl()),
                "address", schoolName,
                "totalStar", studentGlobalStar.getTotalStar(),
                "duration", studentGlobalStar.getTotalDuration(),
                "rank", rank
        );
    }
}
