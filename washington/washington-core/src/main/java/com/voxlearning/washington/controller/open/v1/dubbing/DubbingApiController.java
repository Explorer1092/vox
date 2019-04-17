package com.voxlearning.washington.controller.open.v1.dubbing;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningService;
import com.voxlearning.galaxy.service.studyplanning.api.constant.StudyPlanningType;
import com.voxlearning.galaxy.service.studyplanning.api.data.StudyPlanningItemMapper;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.dubbing.api.DubbingCacheService;
import com.voxlearning.utopia.service.dubbing.api.DubbingHistoryLoader;
import com.voxlearning.utopia.service.dubbing.api.DubbingHistoryService;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.DubbingLoader;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.DubbingCategory;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.washington.controller.open.v1.util.JxtNewsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2017-9-7
 */
@Controller
@RequestMapping("/v1/dubbing/")
@Slf4j
public class DubbingApiController extends AbstractDubbingApiController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = DubbingHistoryLoader.class)
    private DubbingHistoryLoader dubbingHistoryLoader;
    @ImportService(interfaceClass = DubbingHistoryService.class)
    private DubbingHistoryService dubbingHistoryService;
    @ImportService(interfaceClass = DubbingLoader.class)
    private DubbingLoader dubbingLoader;
    @ImportService(interfaceClass = DubbingCacheService.class)
    private DubbingCacheService dubbingCacheService;
    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;
    @ImportService(interfaceClass = NewHomeworkLoader.class)
    private NewHomeworkLoader newHomeworkLoader;
    @ImportService(interfaceClass = StudyPlanningService.class)
    private StudyPlanningService studyPlanningService;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    private static final String DUBBING_PRIVILEGE = "DUBBING_PRIVILEGE";

    /**
     * 频道列表
     */
    @RequestMapping(value = "category_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCategoryList() {
        List<DubbingCategory> categoryList = dubbingLoader.loadAllChannels();
        //取出七天之内更新的频道，做更新数用
        Map<String, DubbingCategory> updateCategoryMap = categoryList.stream().filter(e -> e.getOlUpdatedAtOfV1() != null && DateUtils.dayDiff(new Date(), e.getOlUpdatedAtOfV1()) <= 7).collect(Collectors.toMap(DubbingCategory::getId, Function.identity()));
        if (CollectionUtils.isEmpty(categoryList)) {
            return successMessage().add(RES_RESULT_DUBBING_CATEGORY_LIST, Collections.emptyList());
        }
        Set<String> categoryIds = categoryList.stream().map(DubbingCategory::getId).collect(Collectors.toSet());
        Map<String, Long> dubbingUserCounts = dubbingCacheService.loadDubbingUserCounts(categoryIds);
        List<Map<String, Object>> list = new ArrayList<>();
        for (DubbingCategory category : categoryList) {
            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_DUBBING_CATEGORY_ID, category.getId());
            map.put(RES_RESULT_DUBBING_CATEGORY_NAME, category.getName());
            map.put(RES_RESULT_DUBBING_CATEGORY_COVER_IMG, category.getCover());
            map.put(RES_RESULT_DUBBING_CATEGORY_USER_COUNT, JxtNewsUtil.countFormat(SafeConverter.toLong(dubbingUserCounts.get(category.getId()))));
            map.put(RES_RESULT_DUBBING_CATEGORY_DUBBING_COUNT, MapUtils.isNotEmpty(updateCategoryMap) && updateCategoryMap.get(category.getId()) != null ? SafeConverter.toInt(category.getDubbingCount()) : 0);
            list.add(map);
        }

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId != 0L) {
            String ver = getRequestString(REQ_APP_NATIVE_VERSION);
            String sys = getRequestString(REQ_SYS);
            if (VersionUtil.compareVersion(ver, "2.8.0") >= 0 && VersionUtil.compareVersion(ver, "2.8.2") < 0 && StringUtils.equalsIgnoreCase(sys, "ios")) {
                StudyPlanningItemMapper itemMapper = new StudyPlanningItemMapper();
                itemMapper.setType(StudyPlanningType.DUBBING.name());
                studyPlanningService.finishPlanning(studentId, null, itemMapper);
            }
        }
        return successMessage().add(RES_RESULT_DUBBING_CATEGORY_LIST, list);
    }

    /**
     * 每个频道的专辑列表
     */
    @RequestMapping(value = "dubbing_category_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getDubbingCategoryList() {
        try {
            validateRequired(REQ_CATEGORY_ID, "分类ID");
            validateRequest(REQ_CATEGORY_ID, REQ_DUBBING_LEVEL);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getApiStudentId();
        String categoryId = getRequestString(REQ_CATEGORY_ID);
        Integer difficult = getRequestInt(REQ_DUBBING_LEVEL);
        //难度
        List<Integer> difficultList = dubbingLoader.loadDubbingCategoryDifficulties(categoryId);
        if (CollectionUtils.isEmpty(difficultList)) {
            return failMessage(RES_RESULT_DUBBING_NOT_EXIST_MSG);
        }
        //这个难度默认取最低难度
        if (difficult == 0) {
            difficult = difficultList.get(0);
        }
        //专辑
        List<DubbingCategory> dubbingCategoryList = dubbingLoader.loadDubbingCategoriesByCategoryIdAndDifficulty(categoryId, difficult);
        //配音列表
        List<Map<String, Object>> categoryMapList = new ArrayList<>();
        //难度列表
        List<Map<String, Object>> levelList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(difficultList)) {
            difficultList.forEach(level -> {
                Map<String, Object> map = new HashMap<>();
                map.put(RES_RESULT_DUBBING_LEVEL, level);
                map.put(RES_RESULT_DUBBING_LEVEL_NAME, "等级 " + level);
                levelList.add(map);
            });
        }
        //一个难度下的专辑列表
        if (CollectionUtils.isNotEmpty(dubbingCategoryList)) {
            //专辑ID
            Set<String> categoryIds = dubbingCategoryList.stream().map(DubbingCategory::getId).collect(Collectors.toSet());
            //每个专辑的配音数量
            Map<String, Integer> userCategoryDubbingCountMap = dubbingHistoryLoader.getDubbingHistoryCountByUserIdAndCategoryIds(studentId, categoryIds);
//            Map<String, Long> userCategoryDubbingCountMap = dubbingCacheService.loadUserDubbingCountInCategories(studentId, categoryIds);
            //按照进度倒序排列
            Comparator<DubbingCategory> c = (o1, o2) -> SafeConverter.toInt(userCategoryDubbingCountMap.get(o2.getId())) - SafeConverter.toInt(userCategoryDubbingCountMap.get(o1.getId()));
            c = c.thenComparing((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
            //取出七天之内更新的专辑，做更新数用
            Map<String, DubbingCategory> updateCategoryMap = dubbingCategoryList.stream().filter(e -> e.getOlUpdatedAtOfV1() != null && DateUtils.dayDiff(new Date(), e.getOlUpdatedAtOfV1()) <= 7).collect(Collectors.toMap(DubbingCategory::getId, Function.identity()));
            //去掉配音数为0的专辑
            dubbingCategoryList.stream().filter(p -> SafeConverter.toInt(p.getDubbingCount()) > 0).sorted(c).forEach(category -> {
                Map<String, Object> map = new HashMap<>();
                map.put(RES_RESULT_DUBBING_CATEGORY_ID, category.getId());
                map.put(RES_RESULT_DUBBING_CATEGORY_COVER_IMG, category.getCover());
                map.put(RES_RESULT_DUBBING_CATEGORY_NAME, category.getName());
                map.put(RES_RESULT_DUBBING_COUNT, category.getDubbingCount());
                map.put(RES_RESULT_DUBBING_CURRENT_COUNT, JxtNewsUtil.countFormat(SafeConverter.toLong(userCategoryDubbingCountMap.get(category.getId()))));
                map.put(RES_RESULT_DUBBING_CATEGORY_DUBBING_COUNT, MapUtils.isNotEmpty(updateCategoryMap) && updateCategoryMap.get(category.getId()) != null ? category.getDubbingCount() : 0);
                categoryMapList.add(map);
            });
        }
        return successMessage().add(RES_RESULT_DUBBING_LEVEL_LIST, levelList).add(RES_RESULT_DUBBING_CATEGORY_LIST, categoryMapList).add(RES_RESULT_DUBBING_CURRENT_LEVEL, difficult);
    }

    /**
     * 专辑下的视频列表
     */
    @RequestMapping(value = "dubbing_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getDubbingListByCategoryId() {
        try {
            validateRequired(REQ_CATEGORY_ID, "分类ID");
            validateRequest(REQ_CATEGORY_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getApiStudentId();
        String categoryId = getRequestString(REQ_CATEGORY_ID);
        DubbingCategory category = null;
        Map<String, DubbingCategory> categoryMap = dubbingLoader.loadDubbingCategoriesByIds(Collections.singleton(categoryId));
        if (MapUtils.isNotEmpty(categoryMap)) {
            category = categoryMap.get(categoryId);
        }
        List<Dubbing> dubbingList = dubbingLoader.loadDubbingsByCategoryId(categoryId);
        if (CollectionUtils.isEmpty(dubbingList)) {
            return successMessage().add(RES_RESULT_DUBBING_LIST, Collections.emptyList());
        }
        List<Long> privilegeAccounts = getPrivilegeAccount();
        boolean isPrivilege = Boolean.FALSE;
        if (CollectionUtils.isNotEmpty(privilegeAccounts) && privilegeAccounts.contains(studentId)) {
            isPrivilege = Boolean.TRUE;
        }
        Set<String> dubbingIds = dubbingList.stream().map(Dubbing::getId).collect(Collectors.toSet());
        Map<String, Long> userDubbingCountMap = dubbingHistoryLoader.getDubbingHistoryCountByUserIdAndDubbingIds(studentId, dubbingIds);
        List<Map<String, Object>> mapList = new ArrayList<>();
        boolean finalIsPrivilege = isPrivilege;
        dubbingList.forEach(dubbing -> {
            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_DUBBING_ID, dubbing.getId());
            map.put(RES_RESULT_DUBBING_NAME, dubbing.getVideoName());
            map.put(RES_RESULT_DUBBING_COVER_IMG, dubbing.getCoverUrl());
            map.put(RES_RESULT_DUBBING_SUMMARY, dubbing.getVideoSummary());
            map.put(RES_RESULT_DUBBING_VIDEO_URL, dubbing.getVideoUrl());
            if (finalIsPrivilege) {
                map.put(RES_RESULT_DUBBING_IS_FINISHED, Boolean.TRUE);
            } else {
                map.put(RES_RESULT_DUBBING_IS_FINISHED, SafeConverter.toLong(userDubbingCountMap.get(dubbing.getId())) > 0);
            }
            mapList.add(map);
        });
        return successMessage().add(RES_RESULT_DUBBING_LIST, mapList).add(RES_RESULT_DUBBING_CATEGORY_NAME, category == null ? "" : category.getName());
    }

    /**
     * 配音原声页
     */
    @RequestMapping(value = "dubbing_original_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getOriginalDubbingInfo() {
        try {
            validateRequired(REQ_DUBBING_ID, "配音ID");
            validateRequest(REQ_DUBBING_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        String dubbingId = getRequestString(REQ_DUBBING_ID);
        Long studentId = getApiStudentId();
        Dubbing dubbing = dubbingLoader.loadDubbingById(dubbingId);
        if (dubbing == null) {
            return failMessage(RES_RESULT_DUBBING_NOT_EXIST_MSG);
        }
        Map<String, Object> dubbingInfoMap = new HashMap<>();
        dubbingInfoMap.put(RES_RESULT_DUBBING_ID, dubbing.getId());
        dubbingInfoMap.put(RES_RESULT_DUBBING_NAME, dubbing.getVideoName());
        dubbingInfoMap.put(RES_RESULT_DUBBING_COVER_IMG, dubbing.getCoverUrl());
        dubbingInfoMap.put(RES_RESULT_DUBBING_VIDEO_URL, dubbing.getVideoUrl());
        dubbingInfoMap.put(RES_RESULT_DUBBING_LEVEL, dubbing.getDifficult());
        dubbingInfoMap.put(RES_RESULT_DUBBING_SUMMARY, dubbing.getVideoSummary());
        //话题
        dubbingInfoMap.put(RES_RESULT_DUBBING_TOPIC_LIST, CollectionUtils.isEmpty(dubbing.getTopics()) ? Collections.emptyList() : dubbing.getTopics().stream().filter(p -> StringUtils.isNotBlank(p.getName())).map(Dubbing.DubbingTopic::getName).collect(Collectors.toList()));
        //词汇
        List<Map<String, Object>> keyWordList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dubbing.getKeyWords())) {
            dubbing.getKeyWords().forEach(dubbingKeyWord -> {
                Map<String, Object> map = new HashMap<>();
                map.put(RES_RESULT_DUBBING_KEY_WORD_CHINESE, dubbingKeyWord.getChineseWord());
                map.put(RES_RESULT_DUBBING_KEY_WORD_ENGLISH, dubbingKeyWord.getEnglishWord());
                keyWordList.add(map);
            });
        }
        dubbingInfoMap.put(RES_RESULT_DUBBING_KEY_WORD_LIST, keyWordList);
        //知识点
        List<Map<String, Object>> grammarList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dubbing.getKeyGrammars())) {
            dubbing.getKeyGrammars().forEach(dubbingGrammar -> {
                Map<String, Object> map = new HashMap<>();
                map.put(RES_RESULT_DUBBING_KEY_GRAMMAR_NAME, dubbingGrammar.getGrammarName());
                map.put(RES_RESULT_DUBBING_KEY_GRAMMAR_EXAMPLE, dubbingGrammar.getExampleSentence());
                grammarList.add(map);
            });
        }
        dubbingInfoMap.put(RES_RESULT_DUBBING_KEY_GRAMMAR_LIST, grammarList);
        //用户已完成的配音数量。前端做限制
        Long thisDubbingCount = dubbingHistoryLoader.getDubbingHistoryCountByUserIdAndDubbingId(studentId, dubbingId);
        Long userTotalCount = dubbingHistoryLoader.getDubbingHistoryCountByUserId(studentId);
        dubbingInfoMap.put(RES_RESULT_DUBBING_HISTORY_COUNT, thisDubbingCount);
        dubbingInfoMap.put(RES_RESULT_DUBBING_HISTORY_TOTAL_COUNT, userTotalCount);
        //班级动态
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        List<DubbingHistory> clazzHistories = new ArrayList<>();
        Boolean isClassMates = Boolean.FALSE;
        //班级动态
        if (clazz != null) {
            clazzHistories = dubbingHistoryLoader.getDubbingHistoryByClazzIdAndDubbingId(clazz.getId(), dubbingId);
            isClassMates = Boolean.TRUE;
        }
        Set<Long> studentIds = clazzHistories.stream().map(DubbingHistory::getUserId).collect(Collectors.toSet());
        //班级动态为空||只有自己完成了
        if (CollectionUtils.isEmpty(clazzHistories) || studentIds.stream().allMatch(p -> Objects.equals(p, studentId))) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            int i = calendar.get(Calendar.WEEK_OF_YEAR);
            Set<String> historyIds = dubbingCacheService.loadWeekRank(i, dubbingId);
            if (CollectionUtils.isEmpty(historyIds)) {
                //本周没找到。如果不是一年第一周。取上一周
                if (i > 1) {
                    historyIds = dubbingCacheService.loadWeekRank(i - 1, dubbingId);
                }
            }
            clazzHistories = dubbingHistoryLoader.getDubbingHistoriesByIds(historyIds).values().stream().filter(DubbingHistory::getIsPublished).collect(Collectors.toList());
            isClassMates = Boolean.FALSE;
        }
        //过滤作业配音中，未完成合成的配音
        Set<String> homeworkDubbingIds = clazzHistories
                .stream()
                .filter(e -> StringUtils.isNotBlank(e.getHomeworkId()))
                .map(p -> new DubbingSyntheticHistory.ID(p.getHomeworkId(), p.getUserId(), p.getDubbingId()).toString())
                .collect(Collectors.toSet());
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistories = newHomeworkLoader.loadDubbingSyntheticHistories(homeworkDubbingIds);
        //未完成合成的作业配音
        Set<String> unfinishIds = dubbingSyntheticHistories.values().stream().filter(e -> e != null && !e.getSyntheticSuccess()).map(DubbingSyntheticHistory::getId).collect(Collectors.toSet());
        clazzHistories = clazzHistories
                .stream()
                .filter(e -> StringUtils.isBlank(e.getHomeworkId())
                        || (StringUtils.isNotBlank(e.getHomeworkId())
                        && !unfinishIds.contains(new DubbingSyntheticHistory.ID(e.getHomeworkId(), e.getUserId(), e.getDubbingId()).toString())))
                .collect(Collectors.toList());
        if (VersionUtil.checkVersionConfig("<2.1.5", version)) {
            clazzHistories = clazzHistories.stream().filter(e -> !e.getIsHomework()).collect(Collectors.toList());
        }
        //组织班级动态数据
        studentIds = clazzHistories.stream().map(DubbingHistory::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userLoaderClient.loadUsers(studentIds);
        List<Map<String, Object>> mapList = new ArrayList<>();
        clazzHistories.stream().filter(p -> p.getDisabled() != null && p.getDisabled() == Boolean.FALSE)
                .sorted((o1, o2) -> o1.getCreateTime().compareTo(o2.getCreateTime())).forEach(history -> {
            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_DUBBING_HISTORY_ID, history.getId());
            map.put(RES_STUDENT_ID, history.getUserId());
            User user = userMap.get(history.getUserId());
            String userName;
            if (user == null || StringUtils.isBlank(user.fetchRealname())) {
                userName = "小学生";
            } else {
                userName = user.fetchRealname();
            }
            map.put(RES_STUDENT_NAME, userName);
            map.put(RES_AVATAR_URL, getUserAvatarImgUrl(user));
            map.put(RES_RESULT_DUBBING_IS_HOMEWORK, history.getIsHomework() != null ? history.getIsHomework() : Boolean.FALSE);
            mapList.add(map);
        });
        dubbingInfoMap.put(RES_RESULT_DUBBING_HISTORY_IS_CLASSMATE, isClassMates);
        dubbingInfoMap.put(RES_RESULT_DUBBING_HISTORY_LIST, mapList);

        String shareUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/dubbing/share.vpage?dubbing_id=" + dubbingId;
        String shareContent = "我分享了" + dubbing.getVideoName() + "视频";
        String shareTitle = "动漫配音学英语";
        String shareDubbingImg = dubbing.getCoverUrl();
        return successMessage().add(RES_RESULT_DUBBING_INFO, dubbingInfoMap)
                .add(RES_RESULT_DUBBING_SHARE_URL, shareUrl)
                .add(RES_RESULT_DUBBING_SHARE_TITLE, shareTitle)
                .add(RES_RESULT_DUBBING_SHARE_CONTENT, shareContent)
                .add(RES_RESULT_DUBBING_SHARE_IMG, shareDubbingImg);
    }

    /**
     * 配音页
     */
    @RequestMapping(value = "dubbing_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getDubbingInfo() {
        try {
            validateRequired(REQ_DUBBING_ID, "配音ID");
            validateRequest(REQ_DUBBING_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String dubbingId = getRequestString(REQ_DUBBING_ID);
        Dubbing dubbing = dubbingLoader.loadDubbingById(dubbingId);
        if (dubbing == null) {
            return failMessage(RES_RESULT_DUBBING_NOT_EXIST_MSG);
        }
        Map<String, Object> map = new HashMap<>();
        dubbing.setBackgroundMusic(StringUtils.replace(dubbing.getBackgroundMusic(), "http://", "https://"));
        map.put(RES_RESULT_DUBBING_ID, dubbing.getId());
        map.put(RES_RESULT_DUBBING_NAME, dubbing.getVideoName());
        map.put(RES_RESULT_DUBBING_VIDEO_URL, dubbing.getVideoUrl());
        map.put(RES_RESULT_DUBBING_BACKGROUND_VIDEO_URL, dubbing.getBackgroundMusic());
        map.put(RES_RESULT_DUBBING_COVER_IMG, dubbing.getCoverUrl());
        if (CollectionUtils.isNotEmpty(dubbing.getSentences())) {
            List<Map<String, Object>> sentences = new ArrayList<>();
            dubbing.getSentences().forEach(sentence -> {
                Map<String, Object> sentenceMap = new HashMap<>();
                sentenceMap.put(RES_RESULT_SENTENCE_CHINESE_CONTENT, sentence.getChineseText());
                sentenceMap.put(RES_RESULT_SENTENCE_ENGLISH_CONTENT, sentence.getEnglishText());
                sentenceMap.put(RES_RESULT_SENTENCE_VIDEO_START, sentence.getVoiceStart());
                sentenceMap.put(RES_RESULT_SENTENCE_VIDEO_END, sentence.getVoiceEnd());
                sentences.add(sentenceMap);
            });
            map.put(RES_RESULT_SENTENCE_LIST, sentences);
        }
        return successMessage().add(RES_RESULT_DUBBING_INFO, map);
    }

    /**
     * 保存配音历史
     */
    @RequestMapping(value = "save_history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveDubbingHistory() {
        try {
            validateRequired(REQ_DUBBING_ID, "视频ID");
            validateRequired(REQ_DUBBING_VIDEO_URL, "配音地址");
            validateRequired(REQ_DUBBING_IS_PUBLISH, "是否公开");
            validateRequest(REQ_DUBBING_ID, REQ_DUBBING_VIDEO_URL, REQ_DUBBING_IS_PUBLISH);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getApiStudentId();
        String dubbingId = getRequestString(REQ_DUBBING_ID);
        String videoUrl = getRequestString(REQ_DUBBING_VIDEO_URL);
        Boolean isPublish = getRequestInt(REQ_DUBBING_IS_PUBLISH) == 1;
        Dubbing dubbing = dubbingLoader.loadDubbingById(dubbingId);
        if (dubbing == null) {
            return failMessage(RES_RESULT_DUBBING_NOT_EXIST_MSG);
        }
        Long totalCount = dubbingHistoryLoader.getDubbingHistoryCountByUserId(studentId);
        if (totalCount == null || totalCount >= 300) {
            return failMessage(RES_RESULT_DUBBING_TOTAL_COUNT_ERROR);
        }
        Long dubbingHistoryCount = dubbingHistoryLoader.getDubbingHistoryCountByUserIdAndDubbingId(studentId, dubbingId);
        if (dubbingHistoryCount == null) {
            return failMessage(RES_RESULT_DUBBING_CURRENT_COUNT_ERROR);
        }
        //学生班级
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        DubbingHistory history = new DubbingHistory();
        history.setUserId(studentId);
        history.setDubbingId(dubbingId);
        history.setVideoUrl(videoUrl);
        history.setIsPublished(isPublish);
        history.setCategoryId(dubbing.getCategoryId());
        Long clazzId = null;
        if (clazz != null) {
            clazzId = clazz.getId();
        }
        history.setClazzId(clazzId);
        String id = DubbingHistory.generateId(studentId, dubbingId, clazzId, dubbing.getCategoryId());
        history.setId(id);
        String fixId = DubbingHistory.generateFixId(studentId, dubbingId);
        history.setFixId(fixId);
        history.setIsHomework(Boolean.FALSE);
        dubbingHistoryService.saveDubbingHistory(history);
        //累计总的完成人次
        if (StringUtils.isNotBlank(dubbing.getCategoryId())) {
            Map<String, DubbingCategory> categoryMap = dubbingLoader.loadDubbingCategoriesByIds(Collections.singleton(dubbing.getCategoryId()));
            if (MapUtils.isNotEmpty(categoryMap) && categoryMap.get(dubbing.getCategoryId()) != null) {
                //这里需要的是记录在频道的ID上面
                dubbingCacheService.addDubbingUserCount(categoryMap.get(dubbing.getCategoryId()).getParentId());
                mySelfStudyService.updateSelfStudyProgress(studentId, SelfStudyType.DUBBING, categoryMap.get(dubbing.getCategoryId()).getName());
            }
        }
        //每周一累计周排行。作为班级动态的备选方案
        if (isPublish) {
            Date current = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(current);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            int i = calendar.get(Calendar.DAY_OF_WEEK);
            //是周一
            if (i == Calendar.MONDAY) {
                int a = calendar.get(Calendar.WEEK_OF_YEAR);
                Set<String> historyIds = dubbingCacheService.loadWeekRank(a, dubbingId);
                if (historyIds.size() < 10) {
                    dubbingCacheService.addWeekRank(a, dubbingId, id);
                }
            }
        }
        User user = raikouSystem.loadUser(history.getUserId());
        String userName;
        if (user == null || StringUtils.isBlank(user.fetchRealname())) {
            userName = "小学生";
        } else {
            userName = user.fetchRealname();
        }
        String shareUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/dubbing/share.vpage?dubbing_history_id=" + id;
        String shareContent = "我分享了" + userName + "的" + dubbing.getVideoName() + "视频";
        String shareTitle = "动漫配音学英语";
        String shareDubbingImg = dubbing.getCoverUrl();
        return successMessage().add(RES_RESULT_DUBBING_HISTORY_ID, id)
                .add(RES_RESULT_DUBBING_SHARE_URL, shareUrl)
                .add(RES_RESULT_DUBBING_SHARE_TITLE, shareTitle)
                .add(RES_RESULT_DUBBING_SHARE_CONTENT, shareContent)
                .add(RES_RESULT_DUBBING_SHARE_IMG, shareDubbingImg);
    }

    @RequestMapping(value = "publish_history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage publishDubbingHistory() {
        try {
            validateRequired(REQ_DUBBING_HISTORY_ID, "配音历史ID");
            validateRequest(REQ_DUBBING_HISTORY_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String historyId = getRequestString(REQ_DUBBING_HISTORY_ID);
        DubbingHistory dubbingHistory = dubbingHistoryLoader.getDubbingHistoryById(historyId);
        if (dubbingHistory == null || dubbingHistory.getDisabled()) {
            return failMessage(RES_RESULT_DUBBING_HISTORY_NOT_EXIST_MSG);
        }
        if (dubbingHistory.getIsPublished()) {
            return failMessage(RES_RESULT_DUBBING_HISTORY_HAD_PUBLISHED);
        }
        Dubbing dubbing = dubbingLoader.loadDubbingById(dubbingHistory.getDubbingId());
        if (dubbing == null) {
            return failMessage(RES_RESULT_DUBBING_NOT_EXIST_MSG);
        }
        dubbingHistory.setIsPublished(Boolean.TRUE);
        dubbingHistoryService.saveDubbingHistory(dubbingHistory);
        User user = raikouSystem.loadUser(dubbingHistory.getUserId());
        String userName;
        if (user == null || StringUtils.isBlank(user.fetchRealname())) {
            userName = "小学生";
        } else {
            userName = user.fetchRealname();
        }
        String shareUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/dubbing/share.vpage?dubbing_history_id=" + historyId;
        String shareContent = "我分享了" + userName + "的" + dubbing.getVideoName() + "视频";
        String shareTitle = "动漫配音学英语";
        String shareDubbingImg = dubbing.getCoverUrl();
        return successMessage().add(RES_RESULT_DUBBING_SHARE_URL, shareUrl)
                .add(RES_RESULT_DUBBING_SHARE_TITLE, shareTitle)
                .add(RES_RESULT_DUBBING_SHARE_CONTENT, shareContent)
                .add(RES_RESULT_DUBBING_SHARE_IMG, shareDubbingImg);
    }

    /**
     * 孩子的配音历史列表
     */
    @RequestMapping(value = "dubbing_history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getDubbingHistory() {
        try {
            validateRequired(REQ_DUBBING_HISTORY_PAGE, "页码");
            validateRequired(REQ_DUBBING_IS_PUBLISH, "是否公开");
            validateRequest(REQ_DUBBING_HISTORY_PAGE, REQ_DUBBING_IS_PUBLISH);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getApiStudentId();
        Integer page = getRequestInt(REQ_DUBBING_HISTORY_PAGE);
        Boolean isPublish = getRequestInt(REQ_DUBBING_IS_PUBLISH) == 1;
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        page = page < 1 ? 1 : page;
        List<DubbingHistory> totalHistories = dubbingHistoryLoader.getDubbingHistoryByUserId(studentId)
                .stream()
                .filter(e -> VersionUtil.checkVersionConfig(">2.1.5", version) || !e.getIsHomework())
                .collect(Collectors.toList());
        List<DubbingHistory> matchHistories = totalHistories.stream()
                .filter(p -> Objects.equals(p.getIsPublished(), isPublish))
                .sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()))
                .collect(Collectors.toList());
        //取作业的配音数据中没有上传成功的数据，然后拼id，从作业接口取相应的合成记录
        Set<DubbingHistory> homeworkDubbingSet = matchHistories.stream().filter(e -> e.getIsHomework() && StringUtils.isNotBlank(e.getHomeworkId())).collect(Collectors.toSet());
        Set<String> dubbingSyncIds = new HashSet<>();
        homeworkDubbingSet.forEach(e -> {
            String dubbingSyncId = new DubbingSyntheticHistory.ID(e.getHomeworkId(), e.getUserId(), e.getDubbingId()).toString();
            dubbingSyncIds.add(dubbingSyncId);
        });
        Map<String, DubbingSyntheticHistory> syntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(dubbingSyncIds);
        //已发布数量
        Long publishedCount = totalHistories.stream().filter(p -> Objects.equals(p.getIsPublished(), Boolean.TRUE)).count();
        //未发布数量
        Long unPublishedCount = totalHistories.size() - publishedCount;
        //分页处理
        Pageable pageable = PageableUtils.startFromOne(page, 10);
        Page<DubbingHistory> historyPage = PageableUtils.listToPage(matchHistories, pageable);
        //需要返回的数据
        matchHistories = historyPage.getContent();
        Set<String> dubbingIds = matchHistories.stream().map(DubbingHistory::getDubbingId).collect(Collectors.toSet());
        Map<String, Dubbing> dubbingMap = dubbingLoader.loadDubbingByIdsIncludeDisabled(dubbingIds);
        List<Map<String, Object>> mapList = new ArrayList<>();
        matchHistories.forEach(history -> {
            Map<String, Object> map = new HashMap<>();
            Dubbing dubbing = dubbingMap.get(history.getDubbingId());
            map.put(RES_RESULT_DUBBING_HISTORY_ID, history.getId());
            map.put(RES_RESULT_DUBBING_ID, history.getDubbingId());
            map.put(RES_RESULT_DUBBING_LEVEL, dubbing == null ? Integer.valueOf(0) : dubbing.getDifficult());
            map.put(RES_RESULT_DUBBING_NAME, dubbing == null ? "" : dubbing.getVideoName());
            map.put(RES_RESULT_DUBBING_IS_HOMEWORK, history.getIsHomework() == null ? Boolean.FALSE : history.getIsHomework());
            String synId = "";
            if (StringUtils.isNotBlank(history.getHomeworkId())) {
                synId = new DubbingSyntheticHistory.ID(history.getHomeworkId(), history.getUserId(), history.getDubbingId()).toString();
            }
            map.put(RES_RESULT_DUBBING_VIDEO_IS_SYNTHESISE, syntheticHistoryMap.get(synId) == null || syntheticHistoryMap.get(synId).getSyntheticSuccess());
            map.put(RES_RESULT_DUBBING_COVER_IMG, dubbing == null ? "" : dubbing.getCoverUrl());
            map.put(RES_RESULT_DUBBING_HISTORY_CREATE_TIME, history.getUpdateTime().getTime());
            mapList.add(map);
        });
        return successMessage().add(RES_RESULT_DUBBING_HISTORY_LIST, mapList).add(RES_RESULT_DUBBING_HISTORY_PUBLISHED_COUNT, publishedCount).add(RES_RESULT_DUBBING_HISTORY__UN_PUBLISHED_COUNT, unPublishedCount);
    }

    @RequestMapping(value = "dubbing_history_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getDubbingHistoryInfo() {
        try {
            validateRequired(REQ_DUBBING_HISTORY_ID, "配音历史ID");
            validateRequest(REQ_DUBBING_HISTORY_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String historyId = getRequestString(REQ_DUBBING_HISTORY_ID);
        DubbingHistory dubbingHistory = dubbingHistoryLoader.getDubbingHistoryById(historyId);
        if (dubbingHistory == null) {
            return failMessage(RES_RESULT_DUBBING_HISTORY_NOT_EXIST_MSG);
        }

        Map<String, Object> info = new HashMap<>();
        if (StringUtils.isNotBlank(dubbingHistory.getHomeworkId())) {
            NewHomework newHomework = newHomeworkLoader.load(dubbingHistory.getHomeworkId());
            if (newHomework == null) {
                return failMessage(ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
            String synId = new DubbingSyntheticHistory.ID(dubbingHistory.getHomeworkId(), dubbingHistory.getUserId(), dubbingHistory.getDubbingId()).toString();
            Map<String, DubbingSyntheticHistory> syntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(Collections.singleton(synId));
            DubbingSyntheticHistory dubbingSyntheticHistory = syntheticHistoryMap.get(synId);
            if (dubbingSyntheticHistory == null || dubbingSyntheticHistory.isSyntheticSuccess(newHomework.getCreateAt())) {
                info.put(RES_RESULT_DUBBING_VIDEO_IS_SYNTHESISE, Boolean.TRUE);
            } else {
                info.put(RES_RESULT_DUBBING_VIDEO_IS_SYNTHESISE, Boolean.FALSE);
            }
        }
        //TODO 这里是不是需要按照是否自己看来区分原音被disabled的数据？
        Dubbing dubbing = dubbingLoader.loadDubbingByIdIncludeDisabled(dubbingHistory.getDubbingId());
        User user = raikouSystem.loadUser(dubbingHistory.getUserId());
        info.put(RES_RESULT_DUBBING_ID, dubbingHistory.getDubbingId());
        info.put(RES_RESULT_DUBBING_NAME, dubbing == null ? "" : dubbing.getVideoName());
        info.put(RES_RESULT_DUBBING_LEVEL, dubbing == null ? Integer.valueOf(0) : dubbing.getDifficult());
        info.put(RES_RESULT_DUBBING_COVER_IMG, dubbing == null ? "" : dubbing.getCoverUrl());
        info.put(RES_RESULT_DUBBING_VIDEO_URL, dubbingHistory.getVideoUrl());
        info.put(RES_RESULT_DUBBING_SUMMARY, dubbing == null ? "" : dubbing.getVideoSummary());
        info.put(RES_RESULT_SENTENCE_COUNT, dubbing == null || CollectionUtils.isEmpty(dubbing.getSentences()) ? 0 : dubbing.getSentences().size());
        info.put(RES_RESULT_DUBBING_CATEGORY_ID, dubbing == null ? "" : dubbing.getCategoryId());
        info.put(RES_RESULT_DUBBING_HAD_DELETED, dubbing == null ? Boolean.TRUE : dubbing.isDeleted());
        String userName;
        if (user == null || StringUtils.isBlank(user.fetchRealname())) {
            userName = "小学生";
        } else {
            userName = user.fetchRealname();
        }
        info.put(RES_STUDENT_NAME, userName);
        info.put(RES_AVATAR_URL, getUserAvatarImgUrl(user));
        String shareUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/dubbing/share.vpage?dubbing_history_id=" + historyId;
        String shareContent = "我分享了" + userName + "的" + (dubbing == null ? "" : dubbing.getVideoName()) + "视频";
        String shareTitle = "动漫配音学英语";
        String shareDubbingImg = dubbing == null ? "" : dubbing.getCoverUrl();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long totalCount = dubbingHistoryLoader.getDubbingHistoryCountByUserId(studentId);
        Long dubbingHistoryCount = dubbingHistoryLoader.getDubbingHistoryCountByUserIdAndDubbingId(studentId, dubbingHistory.getDubbingId());
        if (dubbingHistory.getIsHomework()) {
            String createDate = DateUtils.dateToString(dubbingHistory.getCreateTime(), "MM月dd日");
            String homeworkText = MessageFormat.format("{0}的配音作业", createDate);
            info.put(RES_RESULT_DUBBING_HOMEWORK_TEXT, homeworkText);
            info.put(RES_RESULT_DUBBING_IS_HOMEWORK, dubbingHistory.getIsHomework());
        }
        return successMessage().add(RES_RESULT_DUBBING_HISTORY_INFO, info)
                .add(RES_RESULT_DUBBING_SHARE_URL, shareUrl)
                .add(RES_RESULT_DUBBING_SHARE_TITLE, shareTitle)
                .add(RES_RESULT_DUBBING_SHARE_CONTENT, shareContent)
                .add(RES_RESULT_DUBBING_SHARE_IMG, shareDubbingImg)
                .add(RES_RESULT_DUBBING_HISTORY_COUNT, dubbingHistoryCount)
                .add(RES_RESULT_DUBBING_HISTORY_TOTAL_COUNT, totalCount);
    }

    /**
     * 删除配音
     */
    @RequestMapping(value = "disabled_history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage disabledDubbingHistory() {
        try {
            validateRequired(REQ_DUBBING_HISTORY_IDS, "配音历史ID");
            validateRequest(REQ_DUBBING_HISTORY_IDS);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String json = getRequestString(REQ_DUBBING_HISTORY_IDS);
        List<String> ids = JsonUtils.fromJsonToList(json, String.class);
        if (CollectionUtils.isEmpty(ids)) {
            return successMessage();
        }
        dubbingHistoryService.disabledDubbingHistory(ids);
        return successMessage();
    }


    private List<Long> getPrivilegeAccount() {
        String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), DUBBING_PRIVILEGE);
        if (StringUtils.isEmpty(config)) {
            return Collections.emptyList();
        }
        String[] configArray = StringUtils.split(config, ",");
        List<Long> accountList = new ArrayList<>();
        for (String e : configArray) {
            long account = SafeConverter.toLong(e);
            if (account == 0L) {
                continue;
            }
            accountList.add(account);
        }
        return accountList;
    }
}
