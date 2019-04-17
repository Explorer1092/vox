package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.newhomework.api.AncientPoetryLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.AncientPoetryResultCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.StudentActivityStatistic;
import com.voxlearning.utopia.service.newhomework.api.util.DomainUtil;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.cache.AncientPoetryGlobalRankCacheManager;
import com.voxlearning.utopia.service.newhomework.consumer.cache.ViewPoetryActivityCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.AncientPoetryStudentGlobalStarPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryActivityDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryMissionDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryMissionResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryRegisterDao;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */

@Named
@Service(interfaceClass = AncientPoetryLoader.class)
@ExposeService(interfaceClass = AncientPoetryLoader.class)
public class AncientPoetryLoaderImpl extends SpringContainerSupport implements AncientPoetryLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private AncientPoetryActivityDao ancientPoetryActivityDao;
    @Inject private AncientPoetryMissionDao ancientPoetryMissionDao;
    @Inject private AncientPoetryMissionResultDao ancientPoetryMissionResultDao;
    @Inject private AncientPoetryRegisterDao ancientPoetryRegisterDao;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private AncientPoetryCacheLoaderImpl ancientPoetryCacheLoader;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private AncientPoetryStudentGlobalStarPersistence ancientPoetryStudentGlobalStarPersistence;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    @Override
    public MapMessage fetchPoetryActivityList(TeacherDetail teacher, Long clazzGroupId, Integer clazzLevel) {
        Date currentDate = new Date();
        List<AncientPoetryActivity> poetryActivities = ancientPoetryActivityDao.loadByDate(DateUtils.addDays(currentDate, 10), currentDate).stream()
                .filter(activity -> !activity.getDisabled() && CollectionUtils.isNotEmpty(activity.getRegions()) &&
                        activity.getRegions().stream().anyMatch(region ->
                                region.getRegionLevel().equals(AncientPoetryActivity.RegionLevel.province) && region.getRegionIds().contains(SafeConverter.toLong(teacher.getRootRegionCode()))
                                        || region.getRegionLevel().equals(AncientPoetryActivity.RegionLevel.city) && region.getRegionIds().contains(SafeConverter.toLong(teacher.getCityCode()))
                                        || region.getRegionLevel().equals(AncientPoetryActivity.RegionLevel.country) && region.getRegionIds().contains(SafeConverter.toLong(teacher.getRegionCode()))
                                        || region.getRegionLevel().equals(AncientPoetryActivity.RegionLevel.school) && region.getRegionIds().contains(SafeConverter.toLong(teacher.getTeacherSchoolId()))
                        )).collect(Collectors.toList());

        Set<String> registerIds = poetryActivities.stream().map(activity -> AncientPoetryRegister.generateId(activity.getCreateAt(), activity.getId(), clazzGroupId)).collect(Collectors.toSet());
        Map<String, AncientPoetryRegister> registerMap = ancientPoetryRegisterDao.loads(registerIds);
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, List<Map<String, Object>>> resultListMap = new HashMap<>();
        for (AncientPoetryActivity poetryActivity : poetryActivities) {
            if (!poetryActivity.getClassLevel().contains(clazzLevel)) {
                continue;
            }
            String registerId = AncientPoetryRegister.generateId(poetryActivity.getCreateAt(), poetryActivity.getId(), clazzGroupId);
            String status = poetryActivity.getStartDate().after(currentDate) ? "NOT_START" : (registerMap.containsKey(registerId) ? "REGISTERED" : "NOT_REGISTER");
            List<Map<String, Object>> results = resultListMap.getOrDefault(status, new ArrayList<>());
            results.add(MapUtils.m("activityId", poetryActivity.getId(),
                    "activityName", poetryActivity.getName(),
                    "labels", poetryActivity.getLabel(),
                    "joinCount", poetryActivity.getJoinCount(),
                    "startDate", poetryActivity.getStartDate(),
                    "coverImgUrl", DomainUtil.getRealCdnUrl(poetryActivity.getCoverImgUrl()),
                    "status", status));
            resultListMap.put(status, results);
        }
        List<String> statusSorts = Arrays.asList("NOT_REGISTER", "REGISTERED", "NOT_START");
        for (String status : statusSorts) {
            List<Map<String, Object>> results = resultListMap.get(status);
            if (results != null) {
                resultList.addAll(results.stream().sorted(Comparator.comparing(o -> DateUtils.stringToDate(SafeConverter.toString(o.get("startDate"))))).collect(Collectors.toList()));
            }
        }
        // 老师是否首次进入活动页面
        ViewPoetryActivityCacheManager cacheManager = newHomeworkCacheService.getViewPoetryActivityCacheManager();
        return MapMessage.successMessage().add("result", resultList).add("firstTime", cacheManager.load(cacheManager.getCacheKey(teacher.getId(), null)) == null);
    }


    @Override
    public MapMessage fetchGroupActivityList(Long clazzGroupId, Boolean needDetail, Long studentId) {
        List<AncientPoetryActivity> activityList = loadActivityByGroupId(clazzGroupId);
        List<Map<String, Object>> resultList = new ArrayList<>();
        double totalStar = 0;
        for (AncientPoetryActivity activity : activityList) {
            Map<String, Object> resultMap = MapUtils.m("activityId", activity.getId(), "activityName", activity.getName());
            if (needDetail) {
                AncientPoetryResultCacheMapper cacheMapper = ancientPoetryCacheLoader.loadAncientPoetryResultCacheMapper(activity.getId(), studentId);
                Double star = cacheMapper == null ? 0D : cacheMapper.getStar();
                totalStar += star;
                resultMap.put("star", star);
                resultMap.put("passed", cacheMapper != null && cacheMapper.isFinished());
            }
            resultList.add(resultMap);
        }
        MapMessage mapMessage = MapMessage.successMessage().add("result", resultList);
        if (needDetail) {
            mapMessage.add("totalStar", totalStar);
        }
        mapMessage.add("studentId", studentId);// 前端用
        return mapMessage;
    }

    @Override
    public List<AncientPoetryActivity> loadActivityByGroupId(Long clazzGroupId) {
        List<AncientPoetryRegister> poetryRegisters = loadAncientPoetryRegisterByGroupId(clazzGroupId);
        List<String> activityIds = poetryRegisters.stream()
                .sorted(Comparator.comparing(AncientPoetryRegister::getCreateAt).reversed())
                .map(AncientPoetryRegister::getActivityId)
                .collect(Collectors.toList());
        // 按活动开始时间排序
        // 过滤已下线的活动
        return ancientPoetryActivityDao.loads(activityIds).values()
                .stream()
                .filter(activity -> !SafeConverter.toBoolean(activity.getDisabled()))
                .sorted(Comparator.comparing(AncientPoetryActivity::getStartDate))
                .collect(Collectors.toList());
    }

    private List<AncientPoetryRegister> loadAncientPoetryRegisterByGroupId(Long groupId) {
        Map<Long, List<AncientPoetryRegister>> registerMap = ancientPoetryRegisterDao.loadByClazzGroupIds(Collections.singleton(groupId));
        return registerMap.get(groupId);
    }


    @Override
    public MapMessage fetchActivityMissions(User user, String activityId, Long groupId, Long studentId) {
        AncientPoetryActivity activity = ancientPoetryActivityDao.load(activityId);
        if (activity == null || activity.getDisabled()) {
            return MapMessage.errorMessage("活动不存在或者已下线");
        }
        List<String> missionIds = activity.getMissions().stream().map(AncientPoetryActivity.Mission::getMissionId).collect(Collectors.toList());
        Map<String, AncientPoetryMission> missionMap = ancientPoetryMissionDao.loads(missionIds);
        MapMessage mapMessage = MapMessage.successMessage().add("backgroundImgUrl", DomainUtil.getRealCdnUrl(activity.getBackgroundImgUrl()))
                .add("missionTopImgUrl", DomainUtil.getRealCdnUrl(activity.getMissionTopImgUrl()))
                .add("bgTopImgUrl", DomainUtil.getRealCdnUrl(activity.getBackgroundTopImgUrl()));

        if (user.isStudent()) {
            return studentMissions(activityId, groupId, studentId, activity, missionMap, mapMessage);
        } else if (user.isParent()) {
            return parentMissions(activityId, studentId, activity, missionMap, mapMessage);
        } else {
            return teacherMissions(activity, missionMap, mapMessage);
        }
    }

    private MapMessage studentMissions(String activityId, Long groupId, Long studentId, AncientPoetryActivity activity, Map<String, AncientPoetryMission> missionMap, MapMessage mapMessage) {
        // 家长助力关卡 && 错题订正
        AncientPoetryResultCacheMapper cacheMapper = ancientPoetryCacheLoader.loadAncientPoetryResultCacheMapper(activityId, studentId);
        AncientPoetryResultCacheMapper.PoetryMissionCacheMapper missionDetail = cacheMapper != null ? cacheMapper.getMissionCache().get(AncientPoetryMission.getHelpMissionId()) : null;
        boolean isFinish = missionDetail != null && missionDetail.isFinished();
        AncientPoetryRegister poetryRegister = ancientPoetryRegisterDao.load(AncientPoetryRegister.generateId(activity.getCreateAt(), activityId, groupId));
        String missionId = missionDetail != null ? missionDetail.getMissionId() : null;
        // 家长助力关卡
        mapMessage.put("parentMission", MapUtils.m("missionId", missionId, "star", 10,
                "status", isFinish ? "FINISH" : "TODO",
                "doUrl", UrlUtils.buildUrlQuery("/ancient/poetry/do" + Constants.AntiHijackExt,
                        MapUtils.m("activityId", activityId, "missionId", missionId, "modelType", ModelType.RECITE.name(), "isFinish", isFinish, "isParentMission", true))));
        // 订正关卡
        int noCorrectionNum = cacheMapper != null ? cacheMapper.getNoCorrectNum() : 0;
        double star = new BigDecimal(noCorrectionNum).divide(new BigDecimal(2), 1, BigDecimal.ROUND_HALF_UP).doubleValue();
        mapMessage.add("correctModel", MapUtils.m("correctCount", noCorrectionNum, "star", star,
                "status", cacheMapper == null ? "WITHOUT_CORRECT" : cacheMapper.getCorrectStatus(),
                "doUrl", UrlUtils.buildUrlQuery("/ancient/poetry/do" + Constants.AntiHijackExt, MapUtils.m("activityId", activity.getId(), "correct", true, "modelType", ModelType.FUN.name(), "isParentMission", false))));

        // 普通关卡
        List<Map<String, Object>> commonMissions = new ArrayList<>();
        boolean previousFinished = true;
        for (int i = 0; i < activity.getMissions().size(); i++) {
            AncientPoetryActivity.Mission mission = activity.getMissions().get(i);
            AncientPoetryMission poetryMission = missionMap.get(mission.getMissionId());
            if (poetryMission == null) {
                continue;
            }
            Map<String, Object> resultMap = MapUtils.m("missionId", poetryMission.getId(), "missionName", poetryMission.getTitle());
            if (cacheMapper != null && cacheMapper.getMissionCache().get(poetryMission.getId()) != null) {
                AncientPoetryResultCacheMapper.PoetryMissionCacheMapper missionCacheMapper = cacheMapper.getMissionCache().get(poetryMission.getId());
                resultMap.put("star", SafeConverter.toDouble(missionCacheMapper.getStar()));
                resultMap.put("status", missionCacheMapper.isFinished() ? "FINISH" : "DOING");// LOCK, FINISH, DOING, TODO
                previousFinished = missionCacheMapper.isFinished();
            } else {
                // 从活动开始时间计算, 每天开启一关(当前时间秒数 > 活动开始时间 + 关卡index天数)
                Date startDate = DateUtils.addDays(poetryRegister != null ? poetryRegister.getCreateAt() : activity.getStartDate(), i);
                // 灰度范围类学生打开全部关卡
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                boolean openAll = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "AncientPoetry", "OpenAllMission");
                boolean isLock = !openAll && DayRange.current().getEndDate().toInstant().getEpochSecond() < startDate.toInstant().getEpochSecond();
                resultMap.put("status", isLock ? "LOCK" : previousFinished ? "DOING" : "TODO");
                resultMap.put("startDate", DateUtils.dateToString(startDate, "MM月dd日开启"));
                resultMap.put("star", 0);
                previousFinished = false;
            }
            resultMap.put("coverImgUrl", DomainUtil.getRealCdnUrl(mission.getCoverImgUrl()));
            resultMap.put("backgroundImgUrl", DomainUtil.getRealCdnUrl(mission.getBackgroundImgUrl()));
            resultMap.put("signImgUrl", DomainUtil.getRealCdnUrl(mission.getSignImgUrl()));
            commonMissions.add(resultMap);
        }
        return mapMessage.add("commonMissions", commonMissions);
    }

    private MapMessage parentMissions(String activityId, Long studentId, AncientPoetryActivity activity, Map<String, AncientPoetryMission> missionMap, MapMessage mapMessage) {
        List<Map<String, Object>> commonMissions = new ArrayList<>();
        for (int i = 0; i < activity.getMissions().size(); i++) {
            AncientPoetryActivity.Mission mission = activity.getMissions().get(i);
            AncientPoetryMission poetryMission = missionMap.get(mission.getMissionId());
            if (poetryMission == null) {
                continue;
            }
            Map<String, Object> resultMap = MapUtils.m("missionId", poetryMission.getId(), "missionName", poetryMission.getTitle(),
                    "coverImgUrl", DomainUtil.getRealCdnUrl(mission.getCoverImgUrl()),
                    "backgroundImgUrl", DomainUtil.getRealCdnUrl(mission.getBackgroundImgUrl()),
                    "signImgUrl", DomainUtil.getRealCdnUrl(mission.getSignImgUrl()),
                    "doUrl", UrlUtils.buildUrlQuery("/ancient/poetry/do" + Constants.AntiHijackExt,
                            MapUtils.m("activityId", activityId, "missionId", mission.getMissionId(), "modelType", ModelType.RECITE.name(), "isParentMission", true)));
            commonMissions.add(resultMap);
        }

        // 家长助力是否已助力
        AncientPoetryResultCacheMapper cacheMapper = ancientPoetryCacheLoader.loadAncientPoetryResultCacheMapper(activityId, studentId);
        AncientPoetryResultCacheMapper.PoetryMissionCacheMapper missionDetail = cacheMapper != null ? cacheMapper.getMissionCache().get(AncientPoetryMission.getHelpMissionId()) : null;
        mapMessage.add("isParentMissionFinished", missionDetail != null && missionDetail.isFinished());
        return mapMessage.add("commonMissions", commonMissions);
    }

    private MapMessage teacherMissions(AncientPoetryActivity activity, Map<String, AncientPoetryMission> missionMap, MapMessage mapMessage) {
        List<Map<String, Object>> commonMissions = new ArrayList<>();
        for (int i = 0; i < activity.getMissions().size(); i++) {
            AncientPoetryActivity.Mission mission = activity.getMissions().get(i);
            AncientPoetryMission poetryMission = missionMap.get(mission.getMissionId());
            if (poetryMission == null) {
                continue;
            }
            Map<String, Object> resultMap = MapUtils.m("missionId", poetryMission.getId(),
                    "missionName", poetryMission.getTitle(),
                    "star", 0,
                    "status", "TODO",// 如果老师则全部开启
                    "coverImgUrl", DomainUtil.getRealCdnUrl(mission.getCoverImgUrl()),
                    "backgroundImgUrl", DomainUtil.getRealCdnUrl(mission.getBackgroundImgUrl()),
                    "signImgUrl", DomainUtil.getRealCdnUrl(mission.getSignImgUrl()));
            commonMissions.add(resultMap);
        }
        return mapMessage.add("commonMissions", commonMissions);
    }


    @Override
    public MapMessage fetchMissionDetail(User user, String activityId, String missionId) {
        AncientPoetryActivity poetryActivity = ancientPoetryActivityDao.load(activityId);
        if (poetryActivity == null || poetryActivity.getDisabled()) {
            return MapMessage.errorMessage("活动不存在或者已下线");
        }
        AncientPoetryMission mission = ancientPoetryMissionDao.load(missionId);
        Map<String, Object> resultMap = MapUtils.m("mission", MapUtils.m("id", mission.getId(),
                "title", mission.getTitle(),
                "goal_detail", mission.getGoalDetail(),
                "author", mission.getAuthor(),
                "audio_url", DomainUtil.getRealCdnUrl(mission.getAudioUrl()),
                "comment", mission.getComment(),
                "audio_seconds", mission.getAudioSeconds(),
                "content_list", mission.getContentList()));

        AncientPoetryMissionResult missionResult = null;
        if (user.isStudent()) {
            missionResult = ancientPoetryMissionResultDao.load(AncientPoetryMissionResult.generateId(activityId, missionId, user.getId(), false));
        }

        int finishModelSize = missionResult == null ? 0 : missionResult.getModelFinishAt().size();
        List<Map<String, Object>> modelList = new ArrayList<>();
        int i = 0;
        for (AncientPoetryMission.Model model : mission.getModels().values()) {
            Map<String, Object> modelMap = MapUtils.m("modelType", model.getModelType(),
                    "doUrl", UrlUtils.buildUrlQuery("/ancient/poetry/do" + Constants.AntiHijackExt,
                            MapUtils.m("activityId", activityId, "missionId", missionId, "modelType", model.getModelType())));
            if (user.isStudent()) {
                modelMap.put("status", finishModelSize == i ? "DOING" : finishModelSize > i ? "FINISH" : "LOCK");
            }
            modelList.add(modelMap);
            i++;
        }
        resultMap.put("models", modelList);
        return MapMessage.successMessage().add("result", resultMap);
    }

    @Override
    public MapMessage fetchMissionModelDetail(String activityId, String missionId, ModelType modelType) {
        AncientPoetryActivity poetryActivity = ancientPoetryActivityDao.load(activityId);
        if (poetryActivity == null || poetryActivity.getDisabled()) {
            return MapMessage.errorMessage("活动不存在或者已下线");
        }
        AncientPoetryMission mission = ancientPoetryMissionDao.load(missionId);
        AncientPoetryMission.Model model = mission.getModels().get(modelType);
        if (model == null) {
            return MapMessage.errorMessage("未找到对应关卡模块数据");
        }
        AncientPoetryActivity.Mission activityMission = poetryActivity.getMissions().stream().filter(m -> missionId.equals(m.getMissionId())).findFirst().orElse(null);
        return MapMessage.successMessage().add("missionCoverImgUrl", activityMission != null ? activityMission.getCoverImgUrl() : "")
                .add("result", MapUtils.m("model", model,
                        "title", mission.getTitle(),
                        "author", mission.getAuthor(),
                        "cdnUrl", DomainUtil.getRealCdnUrl()));
    }


    @Override
    public MapMessage fetchMissionModelResult(String activityId, String missionId, Long studentId, ModelType modelType, Boolean isParentMission, String cdnUrl) {
        Map<String, Object> resultMap = new HashMap<>();
        AncientPoetryMissionResult missionResult = ancientPoetryMissionResultDao.load(AncientPoetryMissionResult.generateId(activityId, missionId, studentId, isParentMission));
        boolean isFinished = missionResult != null && (isParentMission ? missionResult.isParentMissionFinished() : missionResult.getModelFinishAt().size() >= modelType.getValue());
        resultMap.put("isFinished", isFinished);

        if (ModelType.RECITE.equals(modelType) && isFinished) {
            resultMap.put("studentAudioUrls", missionResult.getStudentAudioUrls());
            if (missionResult.isParentMissionFinished()) {
                resultMap.put("parentAudioUrls", missionResult.getParentAudioUrls());
                Map<Long, User> userMap = userLoaderClient.loadUsers(Arrays.asList(studentId, missionResult.getParentId()));
                User student = userMap.get(studentId);
                User parent = userMap.get(missionResult.getParentId());
                resultMap.put("studentImage", NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, student != null ? student.fetchImageUrl() : ""));
                resultMap.put("parentImage", NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, parent != null ? parent.fetchImageUrl() : ""));
                resultMap.put("star", 10);
            }
        } else if (ModelType.FUN.equals(modelType)) {
            Map<String, Object> answer = new HashMap<>();
            if (missionResult != null && MapUtils.isNotEmpty(missionResult.getAnswers())) {
                for (AncientPoetryProcessResult baseProcessResult : missionResult.getAnswers().values()) {
                    AncientPoetryProcessResult.QuestionProcessResult processResult = baseProcessResult.getProcessResult();
                    Map<String, Object> value = MapUtils.m(
                            "subMaster", processResult.getSubGrasp(),
                            "master", processResult.getGrasp(),
                            "userAnswers", processResult.getAnswers()
                    );
                    answer.put(baseProcessResult.getDocId(), value);

                }
            }
            resultMap.put("answer", answer);
        }
        return MapMessage.successMessage().add("result", resultMap);
    }


    @Override
    public MapMessage fetchMissionResult(String activityId, String missionId, Long studentId) {
        AncientPoetryMission mission = ancientPoetryMissionDao.load(missionId);
        Map<String, Object> resultMap = new HashMap<>();
        AncientPoetryMissionResult missionResult = ancientPoetryMissionResultDao.load(AncientPoetryMissionResult.generateId(activityId, missionId, studentId, Boolean.FALSE));
        if (missionResult.isFinished() && missionResult.getModelFinishAt().size() == 4) {
            int reciteStar = missionResult.getStudentAudioUrls().size();
            resultMap.put("reciteStar", reciteStar);
            int graspCount = 0;
            long duration = 0;
            for (AncientPoetryProcessResult baseProcess : missionResult.getAnswers().values()) {
                AncientPoetryProcessResult.QuestionProcessResult process = baseProcess.getProcessResult();
                if (process.getGrasp()) {
                    graspCount++;
                }
                duration += NewHomeworkUtils.processDuration(process.getDuration());
            }
            resultMap.put("graspCount", graspCount);
            resultMap.put("funStar", graspCount);
            resultMap.put("totalCount", missionResult.getAnswers().size());
            resultMap.put("duration", duration);
            resultMap.put("totalStar", reciteStar + graspCount);
        }

        return MapMessage.successMessage().add("result", resultMap).add("missionTitle", mission.getTitle());
    }


    @Override
    public MapMessage clazzRankingList(User user, Long groupId, String activityId) {
        List<User> students = studentLoaderClient.loadGroupStudents(groupId);
        List<Long> studentIds = Lists.transform(students, User::getId);
        Map<Long, User> studentMap = students.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<String> activityIds = Collections.singletonList(activityId);
        if (StringUtils.isEmpty(activityId)) {
            List<AncientPoetryRegister> poetryRegisters = loadAncientPoetryRegisterByGroupId(groupId);
            activityIds = Lists.transform(poetryRegisters, AncientPoetryRegister::getActivityId);
        }

        Map<String, AncientPoetryActivity> activityMap = ancientPoetryActivityDao.loads(activityIds);
        List<AncientPoetryResultCacheMapper> cacheMappers = ancientPoetryCacheLoader.loadAncientPoetryResultCacheMapper(activityIds, studentIds);
        Map<Long, List<AncientPoetryResultCacheMapper>> studentCacheMappersMap = cacheMappers.stream().collect(Collectors.groupingBy(AncientPoetryResultCacheMapper::getStudentId));

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map.Entry<Long, List<AncientPoetryResultCacheMapper>> studentCacheMappersEntry : studentCacheMappersMap.entrySet()) {
            User student = studentMap.get(studentCacheMappersEntry.getKey());
            List<AncientPoetryResultCacheMapper> studentCacheMappers = studentCacheMappersEntry.getValue();
            if (student == null || CollectionUtils.isEmpty(studentCacheMappers)) {
                continue;
            }
            // 过滤0星学生
            if (studentCacheMappers.stream().noneMatch(result -> result.getStar() > 0)) {
                continue;
            }
            double parentStar = 0D;
            double totalStar = 0D;
            int finishMissionNum = 0;
            int totalMissionNum = 0;
            int needCorrect = 0;
            Long duration = 0L;
            for (AncientPoetryResultCacheMapper studentCacheMapper : studentCacheMappers) {
                AncientPoetryResultCacheMapper.PoetryMissionCacheMapper parentMission = studentCacheMapper.getMissionCache().get(AncientPoetryMission.getHelpMissionId());
                parentStar += (parentMission != null ? parentMission.getStar() : 0D);
                finishMissionNum += studentCacheMapper.getFinishMissionCount();
                AncientPoetryActivity activity = activityMap.get(studentCacheMapper.getActivityId());
                totalMissionNum += activity.getMissions().size();
                needCorrect += studentCacheMapper.getNoCorrectNum();
                totalStar += studentCacheMapper.getStar();
                duration += studentCacheMapper.getDuration();
            }
            resultList.add(MapUtils.m("studentId", student.getId(),
                    "studentName", student.fetchRealnameIfBlankId(),
                    "progress", StringUtils.join(finishMissionNum, "/", totalMissionNum),
                    "parentStar", parentStar,
                    "needCorrect", needCorrect,
                    "totalStar", totalStar,
                    "duration", duration));
        }
        // 老师按星排；学生、家长按首字母排
        if (user.isTeacher()) {
            resultList.sort(Comparator.comparingDouble((Map<String, Object> o) -> SafeConverter.toDouble(o.get("totalStar"))).reversed().thenComparing(o -> SafeConverter.toLong(o.get("duration"))));
        } else {
            resultList.sort(Comparator.comparing((Map<String, Object> o) -> {
                String fullSpell = NewHomeworkUtils.getFullSpell(SafeConverter.toString(o.get("studentName")));
                return fullSpell == null ? "" : fullSpell;
            }, Collator.getInstance(Locale.CHINA)));
        }
        return MapMessage.successMessage().add("result", resultList);
    }


    @Override
    public MapMessage globalRankingList(Integer provinceId, Integer regionCode, Long schoolId, Integer clazzLevel, String regionLevel, String cdnUrl, Long studentId) {
        List<Map<String, Object>> globalRankList = new ArrayList<>();
        AncientPoetryGlobalRankCacheManager cacheManager = newHomeworkCacheService.getAncientPoetryGlobalRankCacheManager();
        if (StringUtils.equals(regionLevel, "COUNTRY") && regionCode != null && clazzLevel != null) {
            String globalRankCacheKey = cacheManager.getRegionGlobalRankCacheKey(regionCode, clazzLevel);
            globalRankList = cacheManager.load(globalRankCacheKey);
        } else if (StringUtils.equals(regionLevel, "SCHOOL") && schoolId != null && clazzLevel != null) {
            String globalRankCacheKey = cacheManager.getSchoolGlobalRankCacheKey(schoolId, clazzLevel);
            globalRankList = cacheManager.load(globalRankCacheKey);
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(globalRankList)) {
            for (Map<String, Object> cacheMapper : globalRankList) {
                resultList.add(MapUtils.m(
                        "studentId", SafeConverter.toString(cacheMapper.get("studentId")),
                        "studentName", SafeConverter.toString(cacheMapper.get("studentName")),
                        "studentImage", cdnUrl + SafeConverter.toString(cacheMapper.get("studentImage")),
                        "address", SafeConverter.toString(cacheMapper.get("address")),
                        "totalStar", SafeConverter.toDouble(cacheMapper.get("totalStar")),
                        "duration", SafeConverter.toLong(cacheMapper.get("duration"))
                ));
            }
            MapMessage mapMessage = MapMessage.successMessage().add("result", resultList);
            if (studentId != null && studentId != 0L) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                if (studentDetail != null && studentDetail.getClazzLevelAsInteger() != null && studentDetail.getClazzLevelAsInteger().equals(clazzLevel)) {
                    String studentRankCacheKey = StringUtils.equals(regionLevel, "COUNTRY") ? cacheManager.getRegionStudentRankCacheKey(regionCode, clazzLevel, studentId) : cacheManager.getSchoolStudentRankCacheKey(schoolId, clazzLevel, studentId);
                    List<Map<String, Object>> studentRankCacheResult = cacheManager.load(studentRankCacheKey);
                    if (CollectionUtils.isNotEmpty(studentRankCacheResult)) {
                        Map<String, Object> cacheMapper = studentRankCacheResult.get(0);
                        mapMessage.add("self", MapUtils.m(
                                "studentId", SafeConverter.toString(cacheMapper.get("studentId")),
                                "studentName", SafeConverter.toString(cacheMapper.get("studentName")),
                                "studentImage", cdnUrl + SafeConverter.toString(cacheMapper.get("studentImage")),
                                "address", SafeConverter.toString(cacheMapper.get("address")),
                                "totalStar", SafeConverter.toDouble(cacheMapper.get("totalStar")),
                                "duration", SafeConverter.toLong(cacheMapper.get("duration")),
                                "rank", SafeConverter.toInt(cacheMapper.get("rank"))
                        ));
                    }
                }
            }
            return mapMessage;
        } else {
            return MapMessage.successMessage().add("result", Collections.emptyList());
        }
    }

    @Override
    public AncientPoetryActivity findActivityById(String activityId) {
        return ancientPoetryActivityDao.load(activityId);
    }

    @Override
    public List<AncientPoetryActivity> loadAllActivity() {
        return ancientPoetryActivityDao.loadAllActivity();
    }

    @Override
    public MapMessage parentChildActivityList(Long studentId, Long groupId) {
        List<AncientPoetryActivity> activityList = loadActivityByGroupId(groupId);
        List<Map<String, Object>> resultList = new ArrayList<>();
        Double totalStar = 0D;
        boolean studentBegin = false; // 学生是否参加活动
        for (AncientPoetryActivity activity : activityList) {
            AncientPoetryResultCacheMapper cacheMapper = ancientPoetryCacheLoader.loadAncientPoetryResultCacheMapper(activity.getId(), studentId);
            if (cacheMapper == null) {
                cacheMapper = new AncientPoetryResultCacheMapper();
            } else {
                studentBegin = true;
            }
            Map<String, Object> resultMap = MapUtils.m("activityId", activity.getId(),
                    "activityName", activity.getName(),
                    "coverImgUrl", DomainUtil.getRealCdnUrl(activity.getCoverImgUrl()),
                    "star", cacheMapper.getStar(),
                    "finishMissionCount", cacheMapper.getFinishMissionCount(),
                    "totalMissionCount", activity.getMissions().size());

            AncientPoetryResultCacheMapper.PoetryMissionCacheMapper parentMission = cacheMapper.getMissionCache().get(AncientPoetryMission.getHelpMissionId());
            boolean isFinish = parentMission != null && parentMission.isFinished();
            Map<String, Object> parentMissionMap = MapUtils.m("missionId", parentMission != null ? parentMission.getMissionId() : null, "star", 10, "status", isFinish ? "FINISH" : "TODO");
            if (isFinish) {
                parentMissionMap.put("doUrl", UrlUtils.buildUrlQuery("/ancient/poetry/do" + Constants.AntiHijackExt, MapUtils.m("activityId", activity.getId(), "missionId", parentMission.getMissionId(), "modelType", ModelType.RECITE.name(), "isFinish", true, "isParentMission", true)));
            }
            resultMap.put("parentMission", parentMissionMap);

            // 订正模块
            int wrongNum = cacheMapper.getWrongNum();
            int trueNum = cacheMapper.getCorrectTrueNum();
            int noCorrectNum = cacheMapper.getNoCorrectNum();
            double star = new BigDecimal(noCorrectNum).divide(new BigDecimal(2), 1, BigDecimal.ROUND_HALF_UP).doubleValue();
            int correctRate = wrongNum == 0 ? 0 : new BigDecimal(trueNum * 100).divide(new BigDecimal(wrongNum), 0, BigDecimal.ROUND_HALF_UP).intValue(); // 正确率, 四舍五入取整
            resultMap.put("correctModel", MapUtils.m("wrongNum", wrongNum, "trueNum", trueNum, "falseNum", cacheMapper.getCorrectFalseNum(), "noCorrectNum", noCorrectNum,
                    "star", star,
                    "status", cacheMapper.getCorrectStatus(),
                    "correctRate", correctRate,
                    "doUrl", UrlUtils.buildUrlQuery("/ancient/poetry/do" + Constants.AntiHijackExt, MapUtils.m("activityId", activity.getId(), "correct", true, "modelType", ModelType.FUN.name(), "isParentMission", true))));
            resultList.add(resultMap);
            totalStar += SafeConverter.toDouble(cacheMapper.getStar());
        }
        return MapMessage.successMessage().add("result", resultList).add("totalStar", totalStar).add("studentBegin", studentBegin);
    }

    @Override
    public Map<String, AncientPoetryMission> fetchAncientPoetryMissionByIds(List<String> poetryIds) {
        return ancientPoetryMissionDao.loads(poetryIds);
    }

    @Override
    public List<AncientPoetryMission> loadAllPoetry() {
        return ancientPoetryMissionDao.loadAllPoetry();
    }

    @Override
    public StudentActivityStatistic getStudentActivityStatistics(Long studentId) {
        Group groupMapper = raikouSystem.loadStudentGroups(studentId)
                .stream()
                .filter(group -> group.getSubject().equals(Subject.CHINESE))
                .findFirst()
                .orElse(null);
        StudentActivityStatistic statistic = new StudentActivityStatistic();
        if (groupMapper == null) {
            return statistic;
        }
        List<AncientPoetryActivity> activityList = loadActivityByGroupId(groupMapper.getId());
        List<AncientPoetryResultCacheMapper> cacheMappers = ancientPoetryCacheLoader.loadAncientPoetryResultCacheMapper(Lists.transform(activityList, AncientPoetryActivity::getId), Collections.singletonList(studentId));
        Integer leanPoetryNum = 0;
        Integer noCorrectionNum = 0;
        Integer finishParentChildNum = 0;
        for (AncientPoetryResultCacheMapper cacheMapper : cacheMappers) {
            leanPoetryNum += cacheMapper.getFinishMissionCount();
            noCorrectionNum += cacheMapper.getNoCorrectNum();
            if (cacheMapper.getMissionCache().containsKey(AncientPoetryMission.getHelpMissionId())) {
                finishParentChildNum++;
            }
        }
        statistic.setLeanPoetryNum(leanPoetryNum);
        statistic.setNoCorrectionNum(noCorrectionNum);
        statistic.setParentChildNum(activityList.size() - finishParentChildNum);
        return statistic;
    }

    @Override
    public MapMessage loadCorrectQuestions(String activityId, Long studentId) {
        AncientPoetryActivity activity = ancientPoetryActivityDao.load(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("活动不存在");
        }
        List<Map<String, Object>> correctQuestions = new ArrayList<>();
        List<AncientPoetryActivity.Mission> missions = activity.getMissions();
        if (CollectionUtils.isNotEmpty(missions)) {
            List<String> missionResultIds = missions.stream().map(mission ->
                    AncientPoetryMissionResult.generateId(activityId, mission.getMissionId(), studentId, false)
            ).collect(Collectors.toList());
            Map<String, AncientPoetryMissionResult> missionResultMap = ancientPoetryMissionResultDao.loads(missionResultIds);
            if (MapUtils.isNotEmpty(missionResultMap)) {
                for (AncientPoetryMissionResult missionResult : missionResultMap.values()) {
                    if (MapUtils.isNotEmpty(missionResult.getAnswers())) {
                        for (AncientPoetryProcessResult processResult : missionResult.getAnswers().values()) {
                            if (processResult.getProcessResult() != null && !SafeConverter.toBoolean(processResult.getProcessResult().getGrasp())) {
                                correctQuestions.add(MapUtils.m(
                                        "questionId", processResult.getDocId(),
                                        "activityId", activityId,
                                        "missionId", missionResult.getMissionId(),
                                        "corrected", processResult.getCorrectProcessResult() != null
                                ));
                            }
                        }
                    }
                }
            }
        }
        return MapMessage.successMessage().add("correctQuestions", correctQuestions);
    }

    @Override
    public MapMessage loadCorrectQuestionsAnswer(String activityId, Long studentId) {
        AncientPoetryActivity activity = ancientPoetryActivityDao.load(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("活动不存在");
        }
        boolean finished = true;
        Map<String, Object> answerMap = new HashMap<>();
        List<AncientPoetryActivity.Mission> missions = activity.getMissions();
        if (CollectionUtils.isNotEmpty(missions)) {
            List<String> missionResultIds = missions.stream().map(mission ->
                    AncientPoetryMissionResult.generateId(activityId, mission.getMissionId(), studentId, false)
            ).collect(Collectors.toList());
            Map<String, AncientPoetryMissionResult> missionResultMap = ancientPoetryMissionResultDao.loads(missionResultIds);
            if (MapUtils.isNotEmpty(missionResultMap)) {
                for (AncientPoetryMissionResult missionResult : missionResultMap.values()) {
                    if (MapUtils.isNotEmpty(missionResult.getAnswers())) {
                        for (AncientPoetryProcessResult processResult : missionResult.getAnswers().values()) {
                            // 找到需要订正的题目
                            if (processResult.getProcessResult() != null && !SafeConverter.toBoolean(processResult.getProcessResult().getGrasp())) {
                                // 判断是否已订正
                                if (processResult.getCorrectProcessResult() != null) {
                                    answerMap.put(
                                            processResult.getDocId(),
                                            MapUtils.m(
                                                    "subMaster", processResult.getCorrectProcessResult().getSubGrasp(),
                                                    "master", processResult.getCorrectProcessResult().getGrasp(),
                                                    "userAnswers", processResult.getCorrectProcessResult().getAnswers()
                                            )
                                    );
                                } else {
                                    finished = false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return MapMessage.successMessage().add("result", MapUtils.m("isFinished", finished, "answer", answerMap));
    }

    @Override
    public List<Long> loadAllJoinedStudentsSchoolIds() {
        return ancientPoetryStudentGlobalStarPersistence.loadAllSchoolIds();
    }

    @Override
    public List<Integer> loadAllJoinedStudentsRegionIds() {
        return ancientPoetryStudentGlobalStarPersistence.loadAllRegionIds();
    }
}
