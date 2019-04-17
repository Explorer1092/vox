package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningLoader;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningService;
import com.voxlearning.galaxy.service.studyplanning.api.constant.StudyPlanningQuantum;
import com.voxlearning.galaxy.service.studyplanning.api.constant.StudyPlanningType;
import com.voxlearning.galaxy.service.studyplanning.api.data.StudyPlanningItemMapper;
import com.voxlearning.galaxy.service.studyplanning.api.entity.StudyPlanningConfig;
import com.voxlearning.galaxy.service.studyplanning.api.entity.StudyPlanningFinishRecord;
import com.voxlearning.galaxy.service.studyplanning.api.entity.StudyPlanningItem;
import com.voxlearning.galaxy.service.studyplanning.api.entity.UserDailyStudyPlanningConfig;
import com.voxlearning.galaxy.service.studyplanning.cache.StudyPlanningCacheManager;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parent.api.support.PalaceMuseumProductSupport;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardStatus;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardCategory;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardLog;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author wei.jiang
 * @since 2018/10/16
 */
@Controller
@RequestMapping(value = "/v1/parent/study_planning")
@Slf4j
public class ParentStudyPlanningApiController extends AbstractParentApiController {
    @ImportService(interfaceClass = StudyPlanningLoader.class)
    private StudyPlanningLoader studyPlanningLoader;
    @ImportService(interfaceClass = StudyPlanningService.class)
    private StudyPlanningService studyPlanningService;
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;

    private static final List<SelfStudyType> selfStudyTypeList;
    private static List<String> practiceList;
    private static Map<String, String> recommendTextMap;
    private static List<Long> mathLessonList;

    static {
        selfStudyTypeList = new ArrayList<>();
        selfStudyTypeList.add(SelfStudyType.AFENTI_ENGLISH);
        selfStudyTypeList.add(SelfStudyType.AFENTI_MATH);
        selfStudyTypeList.add(SelfStudyType.AFENTI_CHINESE);
        practiceList = new ArrayList<>();
        practiceList.add(StudyPlanningType.ENGLISH_PRACTICE.name());
        practiceList.add(StudyPlanningType.CHINESE_PRACTICE.name());
        practiceList.add(StudyPlanningType.MATH_PRACTICE.name());
        recommendTextMap = new HashMap<>();
        recommendTextMap.put(StudyPlanningType.PICLISTEN.name(), "点读磨耳朵");
        recommendTextMap.put(StudyPlanningType.READING_ENGLISH.name(), "读绘本学英语");
        recommendTextMap.put(StudyPlanningType.DUBBING.name(), "趣配音练口语");
        recommendTextMap.put(StudyPlanningType.AFENTI_ENGLISH.name(), "同步练小U");
        recommendTextMap.put(StudyPlanningType.AFENTI_MATH.name(), "同步练小U");
        recommendTextMap.put(StudyPlanningType.AFENTI_CHINESE.name(), "同步练小U");
        recommendTextMap.put(StudyPlanningType.STUDY_TOGETHER.name(), "跟随训练营拓展视野");
        mathLessonList = new ArrayList<>();
        mathLessonList.add(18001L);
        mathLessonList.add(19001L);
        mathLessonList.add(18002L);
        mathLessonList.add(19002L);
    }

    private StudyLesson getStudyLesson(String lessonId) {
        return studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(lessonId));
    }

    @RequestMapping(value = "/info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage info() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage("请重新登录");
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId == 0L) {
            return successMessage().add("gray_flag", false);
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("学生信息错误");
        }

        String dailyId = UserDailyStudyPlanningConfig.generateId(studentId, new Date());
        UserDailyStudyPlanningConfig dailyConfig = studyPlanningLoader.loadDailyConfig(dailyId);
        if (dailyConfig == null) {
            StudyPlanningConfig config = studyPlanningLoader.loadUserStudyPlanningConfig(studentId);
            if (config == null) {
                List<StudyPlanningItemMapper> itemMappers = generateItemMappers(studentId, parent.getId());
                dailyConfig = studyPlanningService.recommendPlanning(studentId, itemMappers);
            } else {
                dailyConfig = studyPlanningService.generateDailyConfig(config);
            }
        }
        List<Map<String, Object>> planningList = new ArrayList<>();
        Map<String, Object> recommendMap = new HashMap<>();
        String detailUrl = ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/adjustplan?useNewCore=wk&is_router=false&rel=1";
        if (dailyConfig != null && MapUtils.isNotEmpty(dailyConfig.getItemIdMap())) {
            List<String> itemIds = new ArrayList<>(dailyConfig.getItemIdMap().keySet());
            List<StudyPlanningItem> items = studyPlanningLoader.loadStudyPlanningItems(itemIds)
                    .stream()
                    .sorted((o1, o2) -> {
                        StudyPlanningType type1 = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(o1.getId()));
                        StudyPlanningType type2 = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(o2.getId()));
                        if (type1 == null || type2 == null) {
                            return -1;
                        }
                        return Integer.compare(type1.getOrder(), type2.getOrder());
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(items)) {
                List<StudyPlanningFinishRecord> todayFinishRecords = studyPlanningLoader.loadMonthFinishRecords(studentId).stream()
                        .filter(e -> e.getFinishDate().after(DayRange.current().getStartDate()))
                        .collect(Collectors.toList());
                List<String> finishItems = todayFinishRecords.stream().map(StudyPlanningFinishRecord::getItemId).collect(Collectors.toList());
                String version = getRequestString(REQ_APP_NATIVE_VERSION);
                planningList = generateNewPlanningList(items, finishItems, version, studentDetail);
                recommendMap = generateRecommendMap(studentId, dailyConfig, items, finishItems);
            }

        }
        MapMessage mapMessage = successMessage();
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "STUDY_PLANNING_TITLE");
        Map<String, Object> configMap = JsonUtils.fromJson(configValue);
        String title = SafeConverter.toString(configMap.get("titleB"), "");
        String detailTitle = SafeConverter.toString(configMap.get("detailTitle"), "");
        mapMessage.add("main_title", title);
        mapMessage.add("detail_title", detailTitle);

        String bannerConfigValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "NEW_TERM_BANNER_DATE");
        Map<String, Object> bannerConfigMap = JsonUtils.fromJson(bannerConfigValue);
        String startDateStr = SafeConverter.toString(bannerConfigMap.get("startDate"), "");
        Date startDate = DateUtils.stringToDate(startDateStr);
        String endDateStr = SafeConverter.toString(bannerConfigMap.get("endDate"), "");
        Date endDate = DateUtils.stringToDate(endDateStr);
        Date now = new Date();
        Map<String, Object> bannerMap = new HashMap<>();
        if (now.after(startDate) && now.before(endDate)) {
            bannerMap.put("banner_content", SafeConverter.toString(bannerConfigMap.get("bannerContent"), ""));
            boolean joinFlag = StudyPlanningCacheManager.INSTANCE.getWarmHeartJoinFlag(studentId);
            if (joinFlag) {
                bannerMap.put("banner_url", ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/WarmPlan?useNewCore=wk&rel=");
            } else {
                bannerMap.put("banner_url", ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/parentchild_plan/index.vpage?referrer=");
            }
        }

        mapMessage.add("detail_url", detailUrl)
                .add("planning_list", planningList)
                .add("recommend_info", recommendMap)
                .add("gray_flag", true)
                .add("banner_info", bannerMap);
        return mapMessage;
    }

    @RequestMapping(value = "/index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage index() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage("请重新登录");
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId == 0L) {
            return successMessage();
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("学生信息错误");
        }
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
        Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
        List<StudentDetail> studentDetails = new ArrayList<>(studentLoaderClient.loadStudentDetails(studentIds).values());
        boolean grayFlag = studentDetails.stream()
                .anyMatch(s -> grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(s, "Study", "Planning"));
        if (!grayFlag) {
            return failMessage("不在灰度内");
        }
        boolean parentHomeworkGray = studentDetails.stream()
                .anyMatch(s -> grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(s, "Parent", "Homework"));
        String dailyId = UserDailyStudyPlanningConfig.generateId(studentId, new Date());
        UserDailyStudyPlanningConfig dailyConfig = studyPlanningLoader.loadDailyConfig(dailyId);
        if (dailyConfig == null) {
            StudyPlanningConfig config = studyPlanningLoader.loadUserStudyPlanningConfig(studentId);
            if (config == null) {
                List<StudyPlanningItemMapper> itemMappers = generateItemMappers(studentId, parent.getId());
                dailyConfig = studyPlanningService.recommendPlanning(studentId, itemMappers);
            } else {
                dailyConfig = studyPlanningService.generateDailyConfig(config);
            }
        }
        List<Map<String, Object>> planningList = new ArrayList<>();
        Map<String, Object> recommendMap = new HashMap<>();
        String detailUrl;
        CacheObject<Object> cacheObject = CacheSystem.CBS.getCache("persistence").get("STUDY_PLANNING_INTRODUCE_FLAG_" + parent.getId());
        if (cacheObject != null && cacheObject.getValue() != null) {
            detailUrl = ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/adjustplan?useNewCore=wk&is_router=false";
        } else {
            detailUrl = ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/introduce?rel=jieshao&useNewCore=wk";
            CacheSystem.CBS.getCache("persistence").incr("STUDY_PLANNING_INTRODUCE_FLAG_" + parent.getId(), 1, 1, 0);
        }
        if (dailyConfig != null && MapUtils.isNotEmpty(dailyConfig.getItemIdMap())) {
            List<String> itemIds = new ArrayList<>(dailyConfig.getItemIdMap().keySet());
            List<StudyPlanningItem> items = studyPlanningLoader.loadStudyPlanningItems(itemIds)
                    .stream()
                    .sorted((o1, o2) -> {
                        StudyPlanningType type1 = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(o1.getId()));
                        StudyPlanningType type2 = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(o2.getId()));
                        if (type1 == null || type2 == null) {
                            return -1;
                        }
                        return Integer.compare(type1.getOrder(), type2.getOrder());
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(items)) {
                List<StudyPlanningFinishRecord> todayFinishRecords = studyPlanningLoader.loadMonthFinishRecords(studentId).stream()
                        .filter(e -> e.getFinishDate().after(DayRange.current().getStartDate()))
                        .collect(Collectors.toList());
                List<String> finishItems = todayFinishRecords.stream().map(StudyPlanningFinishRecord::getItemId).collect(Collectors.toList());
                planningList = generatePlanningList(items, finishItems, studentDetail);
                recommendMap = getRecommendInfo(studentDetail, parentHomeworkGray, items, finishItems);
            }

        }
        return successMessage()
                .add("detail_url", detailUrl)
                .add("planning_list", planningList)
                .add("recommend_info", recommendMap);
    }

    @RequestMapping(value = "/finish.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage finish() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_STUDY_PLANNING_TYPE, "学习计划类型");
            validateRequest(REQ_STUDENT_ID, REQ_STUDY_PLANNING_TYPE);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage("请重新登录");
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String type = getRequestString(REQ_STUDY_PLANNING_TYPE);
        StudyPlanningItemMapper itemMapper = new StudyPlanningItemMapper();
        itemMapper.setType(type);
        studyPlanningService.finishPlanning(studentId, null == getCurrentParent() ? null : getCurrentParent().getId(), itemMapper);
        return MapMessage.successMessage();
    }

    private List<StudyPlanningItemMapper> generateItemMappers(Long studentId, Long parentId) {
        List<StudyPlanningItemMapper> itemMappers = new ArrayList<>();
        List<Integer> weekDayList = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
        //早晨
        if (!Objects.equals(parentId, 20001L)) {
            StudyPlanningItemMapper morningMapper = new StudyPlanningItemMapper();
            morningMapper.setQuantum(StudyPlanningQuantum.MORNING.name());
            morningMapper.setType(StudyPlanningType.PICLISTEN.name());
            morningMapper.setWeekDayList(weekDayList);
            itemMappers.add(morningMapper);
        }
        //放学后和睡前
        StudyPlanningQuantum studyTogetherQuantum = null;
        StudyPlanningQuantum readingQuantum = null;
        Map<SelfStudyType, DayRange> xiaoUMap = xiaoUPaidMap(studentId);
        List<StudyLesson> studyLessons = getActiveLessons(studentId);
        if (MapUtils.isNotEmpty(xiaoUMap)) {
            for (SelfStudyType selfStudyType : xiaoUMap.keySet()) {
                StudyPlanningItemMapper mapper = new StudyPlanningItemMapper();
                mapper.setQuantum(StudyPlanningQuantum.AFTERNOON.name());
                mapper.setType(StudyPlanningType.valueOf(selfStudyType.name()).name());
                mapper.setWeekDayList(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
                mapper.setEndDate(xiaoUMap.get(selfStudyType).getEndDate());
                itemMappers.add(mapper);
            }
            if (CollectionUtils.isEmpty(studyLessons)) {
                readingQuantum = StudyPlanningQuantum.EVENING;
            } else {
                studyTogetherQuantum = StudyPlanningQuantum.EVENING;
            }
        } else {
            if (CollectionUtils.isEmpty(studyLessons)) {
                readingQuantum = StudyPlanningQuantum.AFTERNOON;
            } else {
                studyTogetherQuantum = StudyPlanningQuantum.AFTERNOON;
                readingQuantum = StudyPlanningQuantum.EVENING;
            }
        }
        if (studyTogetherQuantum != null) {
            for (StudyLesson lesson : studyLessons) {
                StudyPlanningItemMapper mapper = new StudyPlanningItemMapper();
                mapper.setQuantum(studyTogetherQuantum.name());
                mapper.setTypeId(SafeConverter.toString(lesson.getLessonId()));
                mapper.setType(StudyPlanningType.STUDY_TOGETHER.name());
                mapper.setWeekDayList(weekDayList);
                mapper.setEndDate(lesson.getCloseDate());
                itemMappers.add(mapper);
            }
        }
        if (readingQuantum != null) {
            StudyPlanningItemMapper mapper = new StudyPlanningItemMapper();
            mapper.setQuantum(readingQuantum.name());
            mapper.setType(StudyPlanningType.READING_ENGLISH.name());
            mapper.setWeekDayList(weekDayList);
            itemMappers.add(mapper);
        }

        if (studyTogetherQuantum != StudyPlanningQuantum.EVENING && readingQuantum != StudyPlanningQuantum.EVENING) {
            StudyPlanningItemMapper mapper = new StudyPlanningItemMapper();
            mapper.setQuantum(StudyPlanningQuantum.EVENING.name());
            mapper.setType(StudyPlanningType.DUBBING.name());
            mapper.setWeekDayList(weekDayList);
            itemMappers.add(mapper);
        }

        if (itemMappers.size() > 5) {
            itemMappers = itemMappers.subList(0, 5);
        }
        itemMappers.forEach(itemMapper -> itemMapper.setRecommend(1));
        return itemMappers;
    }

    private void setQuantumIconAndCrossBandColor(Map<String, Object> quantumMap, StudyPlanningQuantum quantum) {
        if (quantum == null) {
            if (LocalTime.now().getHour() >= 22) {
                quantumMap.put("quantum_icon_ver1", "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110105904315936.png");
            } else {
                quantumMap.put("quantum_icon_ver1", "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110105918027688.png");
            }
            quantumMap.put("quantum_icon_ver2", "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110113828427622.png");
            return;
        }
        StudyPlanningQuantum lightenQuantum = getLightenQuantum();
        String quantumIconVer1 = "";
        String quantumIconVer2 = "";
        String crossBandColor = "";
        switch (quantum) {
            case MORNING:
                if (lightenQuantum == StudyPlanningQuantum.MORNING) {
                    quantumIconVer1 = "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110105723529835.png";
                } else {
                    quantumIconVer1 = "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110105749067931.png";
                }
                quantumIconVer2 = "https://oss-image.17zuoye.com/stuy/planning/2019/01/10/20190110113654696412.png";
                crossBandColor = "#FF7C60";
                break;
            case AFTERNOON:
                if (lightenQuantum == StudyPlanningQuantum.AFTERNOON) {
                    quantumIconVer1 = "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110105806567131.png";
                } else {
                    quantumIconVer1 = "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110105820427092.png";
                }
                quantumIconVer2 = "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110113710728410.png";
                crossBandColor = "#F5A623";
                break;
            case EVENING:
                if (lightenQuantum == StudyPlanningQuantum.EVENING) {
                    quantumIconVer1 = "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110105834507287.png";
                } else {
                    quantumIconVer1 = "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110105849226073.png";
                }
                quantumIconVer2 = "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110113758586772.png";
                crossBandColor = "#2C99FF";
                break;
        }
        quantumMap.put("quantum_icon_ver1", quantumIconVer1);
        quantumMap.put("quantum_icon_ver2", quantumIconVer2);
        quantumMap.put("cross_band_color", crossBandColor);
    }

    private List<Map<String, Object>> generateNewPlanningList(List<StudyPlanningItem> items, List<String> finishItems, String version, StudentDetail studentDetail) {
        List<Map<String, Object>> list = new ArrayList<>();
        boolean itemNeedLighten = true;
        String addItemQuantum = null;
        List<StudyPlanningQuantum> quantumList = Arrays.asList(StudyPlanningQuantum.values());
        Collections.reverse(quantumList);
        for (StudyPlanningQuantum quantum : quantumList) {
            List<StudyPlanningItem> quantumItems = items.stream().filter(item -> quantum.name().equals(item.getTimeQuantum())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(quantumItems)) {
                addItemQuantum = quantum.name();
                break;
            }
        }

        for (StudyPlanningQuantum quantum : StudyPlanningQuantum.values()) {
            List<StudyPlanningItem> quantumItems = items.stream().filter(item -> quantum.name().equals(item.getTimeQuantum())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(quantumItems)) {
                Map<String, Object> quantumMap = new HashMap<>();
                setQuantumIconAndCrossBandColor(quantumMap, quantum);
                quantumMap.put("quantum_desc", getQuantumDesc(quantum));
                //计划列表
                List<Map<String, Object>> planningList = new ArrayList<>();
                Date now = new Date();
                String dateStr = DateUtils.dateToString(now, "yyyy-MM-dd");
                Date endDate = DateUtils.stringToDate(dateStr + " " + studyPlanningLoader.getTimeQuantumNewEndTime(quantum) + ":00");
                boolean itemCanLighten = now.before(endDate);
                for (StudyPlanningItem item : quantumItems) {
                    Map<String, Object> map = new HashMap<>();
                    //任务状态
                    boolean isFinished = finishItems.contains(item.getId());
                    boolean lighten = itemCanLighten && itemNeedLighten && !isFinished;
                    if (lighten) {
                        itemNeedLighten = false;
                    }
                    map.put("lighten", lighten);
                    generateNewItemMap(map, item, finishItems, studentDetail);
                    planningList.add(map);
                }
                if (StringUtils.isNotBlank(addItemQuantum) && quantum.name().equals(addItemQuantum) && VersionUtil.compareVersion(version, "2.6.0") < 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", "SCHEDULE_ADD");
                    map.put("function_type", FunctionType.H5);
                    map.put("function_key", ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/adjustplan?useNewCore=wk&is_router=false");
                    planningList.add(map);
                }
                quantumMap.put("planning_list", planningList);
                list.add(quantumMap);
            }
        }
        if (VersionUtil.compareVersion(version, "2.6.0") >= 0) {
            Map<String, Object> quantumMap = new HashMap<>();
            quantumMap.put("quantum_desc", "晚安");
            setQuantumIconAndCrossBandColor(quantumMap, null);
            List<Map<String, Object>> planningList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("type", "SIGN_CARD");
            map.put("function_type", FunctionType.H5);
            map.put("icon", "https://oss-image.17zuoye.com/study/planning/2019/01/10/20190110111131841681.png");
            setCalendarCardInfo(map, studentDetail);
            planningList.add(map);
            quantumMap.put("planning_list", planningList);
            list.add(quantumMap);
        }

        return list;
    }

    private void setCalendarCardInfo(Map<String, Object> map, StudentDetail studentDetail) {
        String cardText;
        String buttonText = "发奖励";
        int finishStatus = 1;
        String functionKey = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/rewards/detail.vpage?ref=schedule";
        List<ParentRewardLog> rewardLogs = parentRewardLoader.getParentRewardList(studentDetail.getId(), ParentRewardStatus.INIT.getType())
                .stream()
                .filter(log -> {
                    ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(log.getKey());
                    if (item != null) {
                        ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
                        return category != null && "STUDY_PLANNING".equals(category.getKey());
                    }
                    return false;
                })
                .filter(rewardLog -> rewardLog.getCreateTime().after(DateUtils.addHours(new Date(), -24)))
                .collect(Collectors.toList());
        if (rewardLogs.size() > 1) {
            cardText = studentDetail.fetchRealname() + "完成打卡任务";
        } else if (rewardLogs.size() == 1) {
            ParentRewardLog rewardLog = rewardLogs.get(0);
            cardText = studentDetail.fetchRealname() + rewardLog.getTitle();
        } else {
            cardText = "明天继续加油！";
            buttonText = "打卡日历";
            finishStatus = 0;
            functionKey = ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/studyDate?useNewCore=wk&is_router=false";
        }
        map.put("card_text", cardText);
        map.put("button_text", buttonText);
        map.put("finish_status", finishStatus);
        map.put("function_key", functionKey);
    }

    private void generateNewItemMap(Map<String, Object> map, StudyPlanningItem item, List<String> finishItems, StudentDetail studentDetail) {
        StudyPlanningType type = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(item.getId()));
        if (type == null) {
            return;
        }
        map.put("id", item.getId());
        map.put("type", type.name());
        map.put("type_id", StudyPlanningItem.getTypeId(item.getId()));
        map.put("name", getItemName(item));
        int finishStatus = finishItems.contains(item.getId()) ? 1 : 0;
        map.put("finish_status", finishStatus);
        map.put("expect_time", getExpectTime(item));
        int expectTime = getExpectTime(item);
        if (finishStatus != 1) {
            if (type == StudyPlanningType.PARENT_SYNC_PRACTICE || type == StudyPlanningType.ONLINE_MENTAL_ARITHMETIC || type == StudyPlanningType.PAPER_MENTAL_ARITHMETIC) {
                Set<String> homeworkIds;
                switch (type) {
                    case ONLINE_MENTAL_ARITHMETIC:
                        homeworkIds = StudyPlanningCacheManager.INSTANCE.loadOnlineMentalArithmeticAssignIds(studentDetail.getId(), DayRange.current());
                        if (CollectionUtils.isNotEmpty(homeworkIds)) {
                            map.put("card_text", "有新练习");
                        } else {
                            map.put("card_text", "预计10分钟");
                        }
                        break;
                    case PARENT_SYNC_PRACTICE:
                        homeworkIds = StudyPlanningCacheManager.INSTANCE.loadParentSyncPracticeAssignIds(studentDetail.getId(), DayRange.current());
                        if (CollectionUtils.isNotEmpty(homeworkIds)) {
                            map.put("card_text", "有新练习");
                        } else {
                            map.put("card_text", "预计10分钟");
                        }
                        break;
                    default:
                        map.put("card_text", "有新练习");

                }
            } else if ((type == StudyPlanningType.USER_DEFINED || type == StudyPlanningType.NEW_TERM_ACTIVITY) && StringUtils.isNotBlank(item.getConfigStartTime())) {
                map.put("card_text", item.getConfigStartTime() + "开始");
            } else if (expectTime != 0) {
                map.put("card_text", "预计" + expectTime + "分钟");
            }
        }
        setUrlAndIcon(map, item, finishItems.contains(item.getId()), studentDetail);
    }

    private List<Map<String, Object>> generatePlanningList(List<StudyPlanningItem> items, List<String> finishItems, StudentDetail studentDetail) {
        List<Map<String, Object>> list = new ArrayList<>();
        boolean itemNeedLighten = true;
        StudyPlanningQuantum lightenQuantum = getLightenQuantum();
        //如果只有一个计划，要在那个计划的时间段加上一个添加的卡片
        String addItemQuantum = null;
        if (items.size() == 1) {
            addItemQuantum = items.get(0).getTimeQuantum();
        }
        for (StudyPlanningQuantum quantum : StudyPlanningQuantum.values()) {
            List<StudyPlanningItem> quantumItems = items.stream().filter(item -> quantum.name().equals(item.getTimeQuantum())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(quantumItems)) {
                Map<String, Object> quantumMap = new HashMap<>();
                quantumMap.put("quantum_desc", quantum.getDesc());
                quantumMap.put("quantum_time", "");
                //是否点亮
                quantumMap.put("lighten", quantum == lightenQuantum);
                //计划列表
                List<Map<String, Object>> planningList = new ArrayList<>();
                Date now = new Date();

                String dateStr = DateUtils.dateToString(now, "yyyy-MM-dd");
                Date endDate = DateUtils.stringToDate(dateStr + " " + studyPlanningLoader.getTimeQuantumEndTime(quantum) + ":00");
                boolean itemCanLighten = now.before(endDate) || quantum == lightenQuantum;
                for (StudyPlanningItem item : quantumItems) {
                    Map<String, Object> map = new HashMap<>();
                    //任务状态
                    boolean isFinished = finishItems.contains(item.getId());
                    boolean lighten = itemCanLighten && itemNeedLighten && !isFinished;
                    if (lighten) {
                        itemNeedLighten = false;
                    }
                    map.put("lighten", lighten);
                    generateItemMap(map, item, finishItems, studentDetail);
                    planningList.add(map);
                }
                if (StringUtils.isNotBlank(addItemQuantum) && quantum.name().equals(addItemQuantum)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", "SCHEDULE_ADD");
                    map.put("function_type", FunctionType.H5);
                    map.put("function_key", ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/adjustplan?useNewCore=wk&is_router=false");
                    planningList.add(map);
                }
                quantumMap.put("planning_list", planningList);
                list.add(quantumMap);
            }
        }
        return list;
    }

    private String getItemName(StudyPlanningItem item) {
        StudyPlanningType type = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(item.getId()));
        if (type != null) {
            String version = getRequestString(REQ_APP_NATIVE_VERSION);
            String name = type.getDesc();
            if (VersionUtil.compareVersion(version, "2.8.2") < 0 && type == StudyPlanningType.DUBBING) {
                name = "一起配音";
            }
            switch (type) {
                case ENGLISH_PRACTICE:
                    name = "英语作业";
                    break;
                case CHINESE_PRACTICE:
                    name = "语文作业";
                    break;
                case MATH_PRACTICE:
                    name = "数学作业";
                    break;
                case USER_DEFINED:
                case NEW_TERM_ACTIVITY:
                    name = item.getPlanningName();
                    break;
                case STUDY_TOGETHER:
                    String lessonId = StudyPlanningItem.getTypeId(item.getId());
                    StudyLesson studyLesson = getStudyLesson(lessonId);
                    if (studyLesson != null) {
                        // FIXME: 蒋壕说不合理，先这么用着
                        name = studyLesson.getTitle();
                    }
                    break;
                case PARENT_ASSIGN_HOMEWORK:
                    name = getSubjectName(item.getSubject()) + type.getDesc();
                    break;
            }
            return name;
        }
        return "";
    }

    private int getExpectTime(StudyPlanningItem item) {
        int expectTime = 0;
        StudyPlanningType type = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(item.getId()));
        if (type != null) {
            switch (type) {
                case PICLISTEN:
                case AFENTI_CHINESE:
                case AFENTI_ENGLISH:
                case AFENTI_MATH:
                    expectTime = 15;
                    break;
                case READING_ENGLISH:
                case DUBBING:
                case STUDY_TOGETHER_PALACE:
                case STUDY_TOGETHER:
                case UNCLE_PEI:
                case DIFFICULTY_TUTORSHIP:
                    expectTime = 10;
                    break;
                case ENGLISH_PRACTICE:
                case MATH_PRACTICE:
                case CHINESE_PRACTICE:
                case PARENT_ASSIGN_HOMEWORK:
                    Long duration = SafeConverter.toLong(item.getDuration());
                    expectTime = SafeConverter.toInt(duration / 60);
                    long remainder = duration % 60;
                    if (remainder > 0) {
                        expectTime += 1;
                    }
            }
        }
        return expectTime;
    }

    private String getQuantumDesc(StudyPlanningQuantum quantum) {
        String desc;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            desc = quantum.getWeekendDesc();
        } else {
            desc = quantum.getDesc();
        }
        return desc;
    }

    private void generateItemMap(Map<String, Object> map, StudyPlanningItem item, List<String> finishItems, StudentDetail studentDetail) {
        map.put("id", item.getId());
        map.put("type", StudyPlanningItem.getTypeFromId(item.getId()));
        map.put("type_id", StudyPlanningItem.getTypeId(item.getId()));
        //计划名称
        map.put("name", getName(item));
        //右上角标签
        int recommend = SafeConverter.toInt(item.getRecommend());
        map.put("tag", recommend == 2 ? "New" : "");
        //任务状态
        boolean isFinished = finishItems.contains(item.getId());
        String status = isFinished ? "已完成" : "待完成";
        map.put("status", status);
        //跳转信息
        setUrlAndIcon(map, item, isFinished, studentDetail);
    }

    private StudyPlanningQuantum getLightenQuantum() {
        Date now = new Date();
        String dateStr = DateUtils.dateToString(now, "yyyy-MM-dd");
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "STUDY_PLANNING_QUANTUM_LIGHTEN");
        Map<String, String> timeQuantumMap = JsonUtils.fromJsonToMapStringString(configValue);
        for (StudyPlanningQuantum quantum : StudyPlanningQuantum.values()) {
            Date startDate = DateUtils.stringToDate(dateStr + " " + timeQuantumMap.get(quantum.name()).split("-")[0] + ":00");
            Date endDate = DateUtils.stringToDate(dateStr + " " + timeQuantumMap.get(quantum.name()).split("-")[1] + ":00");
            if (now.after(startDate) && now.before(endDate)) {
                return quantum;
            }
        }
        return null;
    }

    private Map<String, Object> getRecommendInfo(StudentDetail studentDetail, Boolean parentHomeworkGary, List<StudyPlanningItem> items, List<String> finishItems) {
        Map<String, Object> map = new HashMap<>();
        String helloText = "";
        String text = "";
        String finishText;
        String color = "";
        FunctionType functionType;
        String functionKey;
        Map<String, Object> extra = new HashMap<>();
        String typeId = "";
        StudyPlanningType type;
        StudyPlanningQuantum quantum = studyPlanningLoader.getTimeQuantum(new Date());
        if (quantum == null) {
            return map;
        }
        List<StudyPlanningItem> practiceItems = items.stream()
                .filter(item -> practiceList.contains(StudyPlanningItem.getTypeFromId(item.getId())))
                .collect(Collectors.toList());
        boolean noPrentHomework = items.stream().noneMatch(e -> StudyPlanningType.PARENT_ASSIGN_HOMEWORK.name().equals(StudyPlanningItem.getTypeFromId(e.getId())));
        //B端小学用户和C端用户显示家长布置作业入口
        boolean canAssignParentHomework = studentDetail.getClazz() == null || (studentDetail.getClazz().isPrimaryClazz() && !studentDetail.getClazz().isTerminalClazz());
        if (quantum == StudyPlanningQuantum.AFTERNOON && CollectionUtils.isEmpty(practiceItems) && noPrentHomework && canAssignParentHomework && parentHomeworkGary) {
            String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "PARENT_HOMEWORK_ENTRANCE");
            Map<String, Object> configMap = JsonUtils.fromJson(configValue);
            text = SafeConverter.toString(configMap.get("text"), "");
            finishText = SafeConverter.toString(configMap.get("finishText"), "");
            color = SafeConverter.toString(configMap.get("color"), "");
            functionType = FunctionType.H5;
            functionKey = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/homework_parent/homework_list.vpage?useNewCore=wk";
            type = StudyPlanningType.PARENT_ASSIGN_HOMEWORK;
        } else {
            StudyPlanningItem recommendItem = items.stream()
                    .filter(item -> quantum.name().equals(item.getTimeQuantum())
                            && !finishItems.contains(item.getId())
                            && !StudyPlanningType.USER_DEFINED.name().equals(StudyPlanningItem.getTypeFromId(item.getId())))
                    .findFirst()
                    .orElse(null);
            if (recommendItem == null || StudyPlanningType.PARENT_ASSIGN_HOMEWORK.name().equals(StudyPlanningItem.getTypeFromId(recommendItem.getId())))
                return map;
            typeId = StudyPlanningItem.getTypeId(recommendItem.getId());
            if (quantum == StudyPlanningQuantum.MORNING) {
                helloText = "早上好~";
            } else if (quantum == StudyPlanningQuantum.AFTERNOON) {
                helloText = "下午好~";
            } else {
                helloText = "晚上好~";
            }
            type = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(recommendItem.getId()));
            List<String> randomTexts = new ArrayList<>();
            int finishCount = StudyPlanningCacheManager.INSTANCE.getTodayFinishCount(type.name());
            finishText = "当前有" + finishCount + "名小学生正在使用《" + type.getDesc() + "》";
            switch (type) {
                case PICLISTEN:
                    randomTexts.add("每天的英文输入让孩子脱口而出，快来打开课本点读练习英文吧");
                    randomTexts.add("孩子今天的点读任务还没完成哦，打开点读机轻松磨耳朵");
                    randomTexts.add("纯正的英文发音是孩子的必备技能，跟随点读机练习英文吧");
                    break;
                case READING_ENGLISH:
                    randomTexts.add("孩子学习英文的语境是非常重要的，打开绘本边听边读吧");
                    randomTexts.add("有趣的绘本能够帮助孩子轻松记单词，点击绘本开始学习吧");
                    randomTexts.add("好玩儿的英文故事让孩子快乐学习，快来读英文绘本吧");
                    break;
                case DUBBING:
                    randomTexts.add("身临其境学英语让孩子突飞猛进，打开一起配音练口语吧");
                    randomTexts.add("孩子课堂学习的单词巩固了吗？来一起配音边玩边学吧");
                    randomTexts.add("想让孩子的英文口语更加地道吗？跟着一起配音来模仿吧");
                    break;
                case AFENTI_ENGLISH:
                case AFENTI_CHINESE:
                case AFENTI_MATH:
                    randomTexts.add("每日的练习必不可少哦，跟随智能的小U查漏补缺吧");
                    randomTexts.add("孩子新学的知识要及时巩固，今天的小U练习已经准备好啦");
                    randomTexts.add("孩子今天的小U同步练还没有完成哦，打开小U开始学习吧");
                    break;
                case STUDY_TOGETHER:
                    randomTexts.add("课外知识可以拓宽孩子的视野，来训练营学习有趣的知识吧");
                    randomTexts.add("想让孩子变成上知天文下知地理的小学霸吗？快来训练营补充知识吧");
                    randomTexts.add("孩子今日的训练营课程还没有完成，鼓励孩子要继续坚持哦");
                    break;
                case ENGLISH_PRACTICE:
                case CHINESE_PRACTICE:
                case MATH_PRACTICE:
                    text = "老师推荐的新作业已经发布啦，记得让孩子按时完成哦";
                    finishText = "";
                    break;
                default:
                    break;
            }
            functionType = FunctionType.H5;
            functionKey = ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/adjustplan?useNewCore=wk";
            if (StringUtils.isBlank(text) && CollectionUtils.isNotEmpty(randomTexts)) {
                Collections.shuffle(randomTexts);
                text = randomTexts.get(0);
            }
        }
        map.put("function_key", functionKey);
        map.put("function_type", functionType);
        map.put("extra", extra);
        map.put("finish_text", finishText);
        map.put("background_color", color);
        map.put("type", type.name());
        map.put("type_id", typeId);
        map.put("text", helloText + text);
        return map;
    }

    private String getName(StudyPlanningItem item) {
        StudyPlanningType type = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(item.getId()));
        if (type != null) {
            String name = type.getDesc();
            switch (type) {
                case USER_DEFINED:
                case NEW_TERM_ACTIVITY:
                    name = item.getPlanningName();
                    break;
                case STUDY_TOGETHER:
                    String lessonId = StudyPlanningItem.getTypeId(item.getId());
                    StudyLesson studyLesson = getStudyLesson(lessonId);
                    if (studyLesson != null) {
                        // FIXME: 蒋壕说不合理，先这么用着
                        name = studyLesson.getParent().getParent().getName();
                    }
                    break;
                case PARENT_ASSIGN_HOMEWORK:
                    name = getSubjectName(item.getSubject()) + type.getDesc();
                    break;
            }
            return name;
        }
        return "";
    }

    private String getSubjectName(String subject) {
        String name = "";
        switch (subject) {
            case "ENGLISH":
                name = "英语";
                break;
            case "MATH":
                name = "数学";
                break;
            case "CHINESE":
                name = "语文";
                break;
        }
        return name;
    }

    private void setUrlAndIcon(Map<String, Object> map, StudyPlanningItem item, boolean finish, StudentDetail studentDetail) {
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        StudyPlanningType type = StudyPlanningType.parse(StudyPlanningItem.getTypeFromId(item.getId()));
        FunctionType functionType = null;
        String functionKey = "";
        Map<String, Object> extra = new HashMap<>();
        switch (type) {
            case PICLISTEN:
                functionType = FunctionType.NATIVE;
                functionKey = FunctionKey.PIC_LISTEN.name();
                break;
            case READING_ENGLISH:
                functionType = FunctionType.H5;
                functionKey = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/picture_books/index.vpage?useNewCore=wk&rel=xxgh";
                break;
            case DUBBING:
                functionType = FunctionType.NATIVE;
                functionKey = FunctionKey.DUBBING.name();
                break;
            case ENGLISH_PRACTICE:
            case CHINESE_PRACTICE:
            case MATH_PRACTICE:
                functionType = FunctionType.H5;
                NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(StudyPlanningItem.getTypeId(item.getId()));
                if (newHomework != null) {
                    if (newHomework.isHomeworkChecked()) {
                        functionKey = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/homework/report_detail?tab=personal&hid=" + StudyPlanningItem.getTypeId(item.getId());
                    } else {
                        functionKey = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/homework/report_notice?hid=" + StudyPlanningItem.getTypeId(item.getId());
                    }
                }
                break;
            case PARENT_ASSIGN_HOMEWORK:
            case PARENT_SYNC_PRACTICE:
                functionType = FunctionType.H5;
                functionKey = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/homework_parent/homework_list.vpage?referrer=xxgh";
                break;
            case ONLINE_MENTAL_ARITHMETIC:
                functionType = FunctionType.H5;
                functionKey = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/homework_parent/homework_list.vpage?type=mental&referrer=xxgh&sid=" + item.getStudentId();
                break;
            case PAPER_MENTAL_ARITHMETIC:
                if (VersionUtil.compareVersion(version, "2.6.2") >= 0) {
                    functionType = FunctionType.ROUTER;
                    functionKey = "a17parent://platform.17zuoye.client/common/picture_ident_tool";
                } else {
                    functionType = FunctionType.H5;
                    functionKey = "karp/study_plan/index/PracticePhotos?useNewCore=wk";
                }
                break;
            case AFENTI_ENGLISH:
            case AFENTI_CHINESE:
            case AFENTI_MATH:
                if (studentDetail.getClazz() == null || !finish) {
                    functionType = FunctionType.H5;
                    functionKey = ProductConfig.getMainSiteBaseUrl() + "/app/redirect/openapp.vpage?appKey=" + getAppKey(type) + "&platform=PARENT_APP";
                } else {
                    functionType = FunctionType.H5;
                    functionKey = ProductConfig.getMainSiteBaseUrl() + "/zion/nova-report?subject=" + getSubject(type) + "&sid=" + item.getStudentId();
                }
                break;
            case STUDY_TOGETHER_PALACE:
                functionType = FunctionType.H5;
                functionKey = getGalaxySiteUrl() + "/karp/gugong/index/solartermindex?useNewCore=wk&series_id=" + PalaceMuseumProductSupport.SPRING_SERIES_ID;
                break;
            case STUDY_TOGETHER:
                StudyLesson lesson = getStudyLesson(StudyPlanningItem.getTypeId(item.getId()));
                if (lesson == null) {
                    break;
                }
                if (lesson.getCourseType() == 3) {
                    functionType = FunctionType.H5;
                    functionKey = ProductConfig.getMainSiteBaseUrl() + "/karp/chinese_reading/index/read_index?useNewCore=wk&course_id=" + lesson.getLessonId() + "&rel=hzxqtraining";
                } else if (lesson.getCourseType() == 5) {
                    functionType = FunctionType.H5;
                    if (mathLessonList.contains(lesson.getId())) {
                        functionKey = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/math_program/index_two.vpage?lesson_id=" + lesson.getId();
                    } else {
                        functionKey = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/math_program/index.vpage?lesson_id=" + lesson.getLessonId();
                    }
                } else if (lesson.getCourseType() == 7) {
                    functionType = FunctionType.H5;
                    functionKey = getGalaxySiteUrl() + "/karp/course_components/index/map_project/index?useNewCore=wk&sku_id=" + lesson.getLessonId() + "&rel=hzxqplanning";
                } else {
                    extra = getStudyTogetherJumpUrl(item, lesson);
                    functionKey = FunctionKey.FAIRYLAND_APP.name();
                    functionType = FunctionType.NATIVE;
                }
                break;
            case USER_DEFINED:
            case NEW_TERM_ACTIVITY:
                functionType = FunctionType.H5;
                if (finish) {
                    functionKey = ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/successpunch?useNewCore=wk&id=" + item.getId();
                } else {
                    functionKey = ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/signin?useNewCore=wk&id=" + item.getId();
                }
                break;
            case UNCLE_PEI:
                functionType = FunctionType.NATIVE;
                functionKey = FunctionKey.FAIRYLAND_APP.name();
                extra = getUnclePeiExtraMap();
                break;
            case DIFFICULTY_TUTORSHIP:
                functionType = FunctionType.H5;
                functionKey = ProductConfig.getMainSiteBaseUrl() + type.getUrl();
                break;
            default:
                break;
        }
        map.put("function_key", functionKey);
        map.put("function_type", functionType);
        map.put("extra", extra);
        map.put("icon", type.getIcon());
        map.put("icon_ver1", finish ? type.getFinishIconVer1() : type.getIconVer1());
        map.put("icon_ver2", finish ? type.getFinishIconVer2() : type.getIconVer2());
    }

    private String getSubject(StudyPlanningType type) {
        if (type == StudyPlanningType.AFENTI_ENGLISH) {
            return "ENGLISH";
        } else if (type == StudyPlanningType.AFENTI_MATH) {
            return "MATH";
        } else if (type == StudyPlanningType.AFENTI_CHINESE) {
            return "CHINESE";
        }
        return "";
    }

    private String getAppKey(StudyPlanningType type) {
        if (type == StudyPlanningType.AFENTI_ENGLISH) {
            return "AfentiExam";
        } else if (type == StudyPlanningType.AFENTI_MATH) {
            return "AfentiMath";
        } else if (type == StudyPlanningType.AFENTI_CHINESE) {
            return "AfentiChinese";
        }
        return "";
    }

    private Map<String, Object> getStudyTogetherJumpUrl(StudyPlanningItem item, StudyLesson lesson) {
        Map<String, Object> map = new HashMap<>();
        String url = "";
        if (lesson != null) {
            String project = lesson.getCourseType() == 2 ? "learnenglishtogether" : "learntogether";
            String domain = ProductConfig.get("galaxy.domain");
            if (StringUtils.isNotBlank(domain)) {
                if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
                    domain = "https://" + domain;
                }
                url = domain + "/parentMobile/study_together/report/share.vpage?project=" + project + "&action=index&hash=%3FkeepHash%3Dtrue&course_id=" + lesson.getLessonId() + "&sid=" + item.getStudentId();
            }
            map.put("fullScreen", true);
            map.put("hideTitle", true);
            map.put("initParams", "{\"webview_screen_on\":true}");
            map.put("name", lesson.getTitle());
            map.put("orientation", "portrait");
            map.put("url", url);
            map.put("useNewCore", "crossWalk");
        }
        return map;
    }

    private Map<String, Object> getUnclePeiExtraMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullScreen", true);
        map.put("hideTitle", true);
        map.put("initParams", "{\"webview_screen_on\":true}");
        map.put("name", "佩叔英语");
        map.put("orientation", "portrait");
        map.put("url", ProductConfig.getMainSiteBaseUrl() + "/resources/apps/hwh5/uncle_pei/V1_0_0/index.html?from=5");
        map.put("useNewCore", "crossWalk");
        return map;
    }

    private Map<SelfStudyType, DayRange> xiaoUPaidMap(Long studentId) {
        Map<SelfStudyType, DayRange> xiaoUMap = new HashMap<>();
        Map<SelfStudyType, DayRange> map = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentId, false);
        map.entrySet().stream()
                .filter(entrySet -> selfStudyTypeList.contains(entrySet.getKey()))
                .filter(entrySet -> entrySet.getValue().getEndDate().after(DayRange.current().getEndDate()))
                .forEach(entrySet -> xiaoUMap.put(entrySet.getKey(), entrySet.getValue()));
        return xiaoUMap;
    }

    private List<StudyLesson> getActiveLessons(Long studentId) {
        List<StudyGroup> studyGroups = studyTogetherServiceClient.loadStudentActiveLessonGroups(studentId);
        List<Long> lessonIds = studyGroups.stream().map(t -> SafeConverter.toLong(t.getLessonId())).collect(Collectors.toList());
        List<StudyLesson> studyLessons = new ArrayList<>(studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLessons(lessonIds).values());
        Date now = new Date();
        return studyLessons.stream()
                .filter(studyLesson -> now.after(studyLesson.getOpenDate()) && now.before(studyLesson.getCloseDate()))
                .filter(studyLesson -> studyLesson.getCourseType() != 6 || studyLesson.getSeriesType() != 15)
                .collect(Collectors.toList());
    }

    private Map<String, Object> generateRecommendMap(Long studentId, UserDailyStudyPlanningConfig dailyConfig, List<StudyPlanningItem> items, List<String> finishItems) {
        Map<String, Object> map = new HashMap<>();
        map.put("button_text", "学习日历");
        map.put("button_url", ProductConfig.getMainSiteBaseUrl() + "/karp/study_plan/index/studyDate?useNewCore=wk&is_router=false");
        boolean userHasSetItem = studyPlanningLoader.userHasSetItem(studentId);
        //用户未设置从未设置过学习规划并且今天所有计划都未完成
        if (!userHasSetItem && finishItems.size() == 0) {
            List<StudyPlanningItem> recommendItems = items.stream()
                    .filter(item -> recommendTextMap.keySet().contains(StudyPlanningItem.getTypeFromId(item.getId())))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(recommendItems)) {
                String recommendCommonText = "基于平台热门学习内容，推荐今日规划：";
                StringBuilder recommendText = new StringBuilder();
                for (StudyPlanningQuantum quantum : StudyPlanningQuantum.values()) {
                    recommendItems.stream()
                            .filter(item -> quantum.name().equals(item.getTimeQuantum()))
                            .findFirst().ifPresent(recommendItem -> recommendText.append(generateRecommendItemDesc(recommendItem)));
                }
                String recommendTextStr = recommendText.substring(0, recommendText.length() - 1);
                map.put("recommend_common_text", recommendCommonText);
                map.put("recommend_text", recommendTextStr);
            }
        } else {
            long unFinshItemCount = dailyConfig.getItemIdMap().keySet()
                    .stream()
                    .filter(itemId -> !finishItems.contains(itemId))
                    .count();
            String signText = "";
            int signDays = StudyPlanningCacheManager.INSTANCE.getFinishSerialDays(studentId);
            if (signDays > 0) {
                signText = "你已连续打卡" + signDays + "天，继续加油！";
            }
            map.put("sign_text", signText);
            map.put("not_finish_count", unFinshItemCount);
        }
        return map;
    }

    private String generateRecommendItemDesc(StudyPlanningItem item) {
        StudyPlanningQuantum quantum = StudyPlanningQuantum.parse(item.getTimeQuantum());
        if (quantum != null) {
            String quantumDesc = getQuantumDesc(quantum);
            return quantumDesc + recommendTextMap.get(StudyPlanningItem.getTypeFromId(item.getId())) + "，";
        }
        return "";
    }

    public enum FunctionType {
        NATIVE, H5, ROUTER
    }

    public enum FunctionKey {
        PIC_LISTEN,
        FAIRYLAND_APP,
        DUBBING
    }

}
