package com.voxlearning.washington.controller.open.v2.parent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.galaxy.service.ambush.api.VicePositionLoader;
import com.voxlearning.galaxy.service.ambush.api.entity.VicePosition;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.client.FairylandProductServiceClient;
import com.voxlearning.washington.cache.HomeworkCacheManager;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.support.StudyEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author jiangpeng
 * http://wiki.17zuoye.net/pages/viewpage.action?pageId=31937388
 * 学习工具货架部分接口
 * @since 2017-10-13 上午11:57
 **/
@Controller
@Slf4j
@RequestMapping(value = "/v2/parent/study/")
public class ParentStudyIndexController extends AbstractParentApiController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private FairylandProductServiceClient fairylandProductServiceClient;
    @ImportService(interfaceClass = VicePositionLoader.class)
    private VicePositionLoader vicePositionLoader;

    private static final String iconImageHeader = "https://oss-image.17zuoye.com";
    private static List<SelfStudyType> optionsFreeSST = new ArrayList<>();

    static {
        optionsFreeSST.add(SelfStudyType.PICLISTEN_ENGLISH);
        optionsFreeSST.add(SelfStudyType.DUBBING);
        optionsFreeSST.add(SelfStudyType.READING_ENGLISH);
        optionsFreeSST.add(SelfStudyType.ALBUM);
//        optionsFreeSST.add(SelfStudyType.LIVECAST); // FIXME: 2017/10/27 暂时过滤掉直播客
    }

    private static final Map<SelfStudyType, Integer> SSTOrderMap = new HashMap<>();

    static {
        SSTOrderMap.put(SelfStudyType.ZUOYECUOITI, 5);
        SSTOrderMap.put(SelfStudyType.PICLISTEN_ENGLISH, 10);
        SSTOrderMap.put(SelfStudyType.DUBBING, 20);
        SSTOrderMap.put(SelfStudyType.ALBUM, 30);
        SSTOrderMap.put(SelfStudyType.READING_ENGLISH, 40);
        SSTOrderMap.put(SelfStudyType.LIVECAST, 60);

        SSTOrderMap.put(SelfStudyType.AFENTI_ENGLISH, 70);
        SSTOrderMap.put(SelfStudyType.AFENTI_MATH, 80);
        SSTOrderMap.put(SelfStudyType.AFENTI_CHINESE, 90);
        SSTOrderMap.put(SelfStudyType.ZOUMEI_ENGLISH, 100);
        SSTOrderMap.put(SelfStudyType.KUPAOWORD_ENGLISH, 110);
        SSTOrderMap.put(SelfStudyType.ARITHMETIC, 120);
        SSTOrderMap.put(SelfStudyType.CHINESE_HERO, 130);
        SSTOrderMap.put(SelfStudyType.SYN_PRACTICE_CHINESE, 140);
        SSTOrderMap.put(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW, 150);
        SSTOrderMap.put(SelfStudyType.ANIMAL_LAND, 160);
        SSTOrderMap.put(SelfStudyType.DINOSAUR_LAND, 170);
        SSTOrderMap.put(SelfStudyType.SCIENCE_LAND, 180);
        SSTOrderMap.put(SelfStudyType.MATH_GARDEN, 190);
    }

    private static final Map<SelfStudyType, String> SSTDescMap = new HashMap<>();

    static {
        SSTDescMap.put(SelfStudyType.PICLISTEN_ENGLISH, "会发音的电子课本");
        SSTDescMap.put(SelfStudyType.ALBUM, "超千集学习资源");
        SSTDescMap.put(SelfStudyType.DUBBING, "动画配音趣练口语");
        SSTDescMap.put(SelfStudyType.READING_ENGLISH, "海量绘本提升阅读");
        SSTDescMap.put(SelfStudyType.ZOUMEI_ENGLISH, "单词专练");
        SSTDescMap.put(SelfStudyType.ARITHMETIC, "速算能力");
        SSTDescMap.put(SelfStudyType.CHINESE_HERO, "丰富语文词汇");
        SSTDescMap.put(SelfStudyType.SYN_PRACTICE_CHINESE, "语文基础字词");
        SSTDescMap.put(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW, "8大类知识");
        SSTDescMap.put(SelfStudyType.ANIMAL_LAND, "6大类动物");
        SSTDescMap.put(SelfStudyType.DINOSAUR_LAND, "恐龙帝国探秘");
        SSTDescMap.put(SelfStudyType.SCIENCE_LAND, "身边科技常识");
        SSTDescMap.put(SelfStudyType.CHIPS_ENGLISH, "让英语脱口而出");
        SSTDescMap.put(SelfStudyType.ELEVEL_READING, "阅读中练习语感");
        SSTDescMap.put(SelfStudyType.CLEVEL_READING, "孩子天生的最佳读本");
    }

    private static final List<SelfStudyType> SSTOrderList = new LinkedList<>();

    static {
        SSTOrderList.add(SelfStudyType.CHIPS_ENGLISH);
        SSTOrderList.add(SelfStudyType.AFENTI_ENGLISH);
        SSTOrderList.add(SelfStudyType.AFENTI_MATH);
        SSTOrderList.add(SelfStudyType.AFENTI_CHINESE);
        SSTOrderList.add(SelfStudyType.AFENTI_ENGLISH_IMPROVE);
        SSTOrderList.add(SelfStudyType.AFENTI_MATH_IMPROVE);
        SSTOrderList.add(SelfStudyType.AFENTI_CHINESE_IMPROVE);
        SSTOrderList.add(SelfStudyType.PICLISTEN_ENGLISH);
        SSTOrderList.add(SelfStudyType.ZUOYECUOITI);
        SSTOrderList.add(SelfStudyType.ZOUMEI_ENGLISH);
        SSTOrderList.add(SelfStudyType.SYN_PRACTICE_CHINESE);
        SSTOrderList.add(SelfStudyType.CHINESE_HERO);
        SSTOrderList.add(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW);
        SSTOrderList.add(SelfStudyType.ANIMAL_LAND);
        SSTOrderList.add(SelfStudyType.DINOSAUR_LAND);
        SSTOrderList.add(SelfStudyType.SCIENCE_LAND);
        SSTOrderList.add(SelfStudyType.KUPAOWORD_ENGLISH);
        SSTOrderList.add(SelfStudyType.XUEBA_VIDEO);

        //免费
        SSTOrderList.add(SelfStudyType.DUBBING);
        SSTOrderList.add(SelfStudyType.ALBUM);
        SSTOrderList.add(SelfStudyType.READING_ENGLISH);

        //收费
        SSTOrderList.add(SelfStudyType.WORD_BUILDER);
        SSTOrderList.add(SelfStudyType.MATH_GARDEN);
        SSTOrderList.add(SelfStudyType.CHINESE_PILOT);
        SSTOrderList.add(SelfStudyType.LISTEN_WORLD);
        SSTOrderList.add(SelfStudyType.ELEVEL_READING);
        SSTOrderList.add(SelfStudyType.CLEVEL_READING);
    }

    public static final List<String> freeResourceRecomdList;

    static {
        freeResourceRecomdList = Lists.newLinkedList();
        freeResourceRecomdList.add("PARENT_HOMEWORK_ENTRY_NEW");
        freeResourceRecomdList.add("PARENT_MATH_PRACTICE_ENTRY_NEW");
        freeResourceRecomdList.add("PARENT_QUICK_ORAL_ENTRY_NEW");
        freeResourceRecomdList.add("PARENT_ORAL_EXERCISE_ENTRY_NEW");
    }

    private static final String indexChannel = "330094";
    private static final String editChannel = "330095";

    private StudyEntry.Tag fetchTag(SelfStudyType selfStudyType, IndexDataContext.Position position, StudyEntry.Tag specificTag, boolean isMySelected) {
        if (position == null || selfStudyType == null) {
            return null;
        }
        if (position == IndexDataContext.Position.index && isMySelected) { //首页-我的学习资源 只显示配音的 tag
            switch (selfStudyType) {
                case PICLISTEN_ENGLISH:
                    return StudyEntry.Tag.rec;
                case UNKNOWN:
                    return StudyEntry.Tag.rec;
                default:
                    return null;
            }
        } else if (position == IndexDataContext.Position.edit || !isMySelected) {
            if (specificTag != null) {
                return specificTag;
            }
            switch (selfStudyType) {
                case PICLISTEN_ENGLISH:
                    return StudyEntry.Tag.fresh;
                case ALBUM:
                    return StudyEntry.Tag.free;
                case READING_ENGLISH:
                    return StudyEntry.Tag.free;
                default:
                    return null;
            }
        }
        return null;
    }

    private StudyEntry.Tag fetchTag(SelfStudyType selfStudyType, IndexDataContext.Position position, boolean isMySelected) {
        return fetchTag(selfStudyType, position, null, isMySelected);
    }


    @RequestMapping(value = "tool_options.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage optionsTools() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage(RES_RESULT_STUDENT_ID_ERROR_MSG);
        }

        IndexDataContext indexDataContext = asyncLoadIndexDataContext(parent, studentDetail);
        indexDataContext.setPosition(IndexDataContext.Position.edit);
        indexDataContext.setChannel(editChannel);
        StudyToolShelf studyToolShelf = parentSelfStudyService.loadStudentStudyToolShelf(studentId);

        List<SelfStudyType> selectedSStList = studyToolShelf == null ? Collections.EMPTY_LIST : studyToolShelf.toSelfStudyTypeList();
        Map<SelfStudyType, DayRange> selfStudyTypeDayRangeMap = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentId, false);
        DayRange todayDayRange = DayRange.current();
        List<SelfStudyType> moneySSTList = selfStudyTypeDayRangeMap.entrySet().stream().filter(t -> {
            DayRange endDayRange = t.getValue();
            if (endDayRange.getEndDate().after(todayDayRange.getEndDate())) {
                return true;
            } else {
                return DateUtils.calculateDateDay(endDayRange.getEndDate(), 10).after(todayDayRange.getEndDate());
            }
        }).map(Map.Entry::getKey).collect(Collectors.toList());


        List<SelfStudyType> optionsSStList = new ArrayList<>();
        Boolean hasZuoyecuotiEntry = studentHasZuoyecuotiEntry(studentDetail);
        if (hasZuoyecuotiEntry) {
            optionsSStList.add(SelfStudyType.ZUOYECUOITI);
        }
        optionsSStList.addAll(moneySSTList);
        optionsSStList.addAll(optionsFreeSST);
        if (indexDataContext.isShenhe()) {
            optionsSStList.remove(SelfStudyType.PICLISTEN_ENGLISH);
        }
        List<SelfStudyType> finalSelectedSStList = selectedSStList;
        optionsSStList = optionsSStList.stream().filter(t -> !finalSelectedSStList.contains(t)).collect(Collectors.toList());

        Set<SelfStudyType> allSSTSet = new HashSet<>(selectedSStList);
        allSSTSet.addAll(optionsSStList);


        Map<SelfStudyType, StudyEntry> studyEntryMap = allSSTSet.stream()
                .map(sst -> processEntry(sst, indexDataContext, false, false, false))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(StudyEntry::getSelfStudyType, Function.identity()));
        List<StudyEntry> selectedEntryList = selectedSStList.stream()
                .filter(t -> t != SelfStudyType.LIVECAST) // FIXME: 2017/10/27 暂时过滤掉直播客
                .map(studyEntryMap::get).collect(Collectors.toList());
        List<StudyEntry> optionEntryList = optionsSStList.stream()
                .sorted(Comparator.comparing(o -> SSTOrderMap.getOrDefault(o, 100000)))
                .map(studyEntryMap::get).collect(Collectors.toList());
        dealIconCdnAndGlobalMSg(optionEntryList, indexDataContext);
        dealIconCdnAndGlobalMSg(selectedEntryList, indexDataContext);
        optionEntryList = optionEntryList.stream().filter(Objects::nonNull).peek(t -> dealDefaultLabel2Desc(t, indexDataContext, false)).collect(Collectors.toList());
        selectedEntryList = selectedEntryList.stream().filter(Objects::nonNull).peek(t -> dealDefaultLabel2Desc(t, indexDataContext, false)).collect(Collectors.toList());
        return successMessage().add("my_selected_list", selectedEntryList)
                .add("option_list", optionEntryList);

    }

    //高于2.2.2版本，如果已选的大于3个，则直接把多余的删掉
    private List<SelfStudyType> dealV222MoreThan3(List<SelfStudyType> selectedSStList, IndexDataContext indexDataContext) {

        if (indexDataContext.isAbove223() && selectedSStList.size() > 3) {
            String lock = "deleteMoreSSTOn222_" + indexDataContext.getStudentDetail().getId();
            List<SelfStudyType> selfStudyTypes1 = new ArrayList<>(selectedSStList.subList(0, 3));
            try {
                AtomicLockManager.getInstance().acquireLock(lock);
                parentSelfStudyService.updateStudentStudyToolShelf(indexDataContext.getStudentDetail().getId(), selfStudyTypes1);
            } catch (CannotAcquireLockException ignore) {

            } finally {
                AtomicLockManager.getInstance().releaseLock(lock);
                return selfStudyTypes1;
            }
        }
        return selectedSStList;
    }

    @RequestMapping(value = "/save_selected.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSelected() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_SELF_STUDY_TYPES, "类型");
            validateRequest(REQ_STUDENT_ID, REQ_SELF_STUDY_TYPES);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId == 0) {
            return failMessage(RES_RESULT_STUDENT_ID_ERROR_MSG);
        }
        String selfStudyTypeStr = getRequestString(REQ_SELF_STUDY_TYPES);
        List<SelfStudyType> selfStudyTypeList = JsonUtils.fromJsonToList(selfStudyTypeStr, SelfStudyType.class);
        if (CollectionUtils.isEmpty(selfStudyTypeList)) {
            return failMessage("至少留一个哦！");
        }
        parentSelfStudyService.updateStudentStudyToolShelf(studentId, selfStudyTypeList);

        return successMessage();
    }

    /**
     * 我的学习必备
     *
     * @since v3.0
     */
    @RequestMapping(value = "/common.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage common() {
        User parent = getCurrentParent();
        long studentId = getRequestLong(REQ_STUDENT_ID);
        try {
            Map<String, String> tagTextMap = getTagConfig();

            boolean isShenhe = parent != null && parent.getId().equals(20001L);
            Clazz clazz = null;
            if (parent != null && studentId != 0) {
                clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            }
            //对①未登录；②登录无孩子；③孩子无班级；⑤孩子有班级（黑名单）⑥非小学账号，屏蔽【更多】按钮
            boolean showMore = true;
            if (null == parent || 0 == studentId || null == clazz) {
                showMore = false;
            } else {
                User user = raikouSystem.loadUser(studentId);
                Map<Long, Boolean> inBlackListByStudent = userBlacklistServiceClient.isInBlackListByStudent(Collections.singleton(user));
                if (inBlackListByStudent.containsKey(studentId) && inBlackListByStudent.get(studentId)) {
                    showMore = false;
                } else if (!clazz.isTerminalClazz() && clazz.getEduSystem().getKtwelve() != Ktwelve.PRIMARY_SCHOOL) {
                    showMore = false;
                }
            }

            //查询主位置金刚位
            List<StudyEntry> entries = new LinkedList<>();
            List<StudyEntry> secondaryEntries = new LinkedList<>();
            List<VicePosition> mainVicePositionList = vicePositionLoader.loadMSVicePosition("MP", false);
            if (CollectionUtils.isNotEmpty(mainVicePositionList)) {
                //20001用户处理
                List<VicePosition> qkVicePositionList = mainVicePositionList.stream()
                        .filter(vicePosition -> "READING_ENGLISH".equals(vicePosition.getSelfStudyType()))
                        .collect(Collectors.toList());
                entries = sortVicePostion(qkVicePositionList, parent);

                if (!isShenhe) {
                    List<VicePosition> allVicePositionList = mainVicePositionList.stream()
                            .filter(vicePosition -> !"READING_ENGLISH".equals(vicePosition.getSelfStudyType()))
                            .collect(Collectors.toList());
                    entries.addAll(sortVicePostion(allVicePositionList, parent));

                    //副金刚位设置
                    List<VicePosition> vicePositions = vicePositionLoader.loadMSVicePosition("SP", false);
                    secondaryEntries = sortVicePostion(vicePositions, parent);
                }
            }

            Map<String, Object> entryMap = new HashMap<>(4);
            entryMap.put("entry_list", entries);
            entryMap.put("title", "我的学习必备");
            entryMap.put("show_more", showMore);
            entryMap.put("aux_list", secondaryEntries);

            return successMessage().add("common_app", entryMap);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return failMessage("系统异常");
        }
    }

    public List<StudyEntry> sortVicePostion(List<VicePosition> vicePositions, User parent) {
        List<StudyEntry> entries = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(vicePositions)) {
            entries = vicePositions.stream()
                    .distinct()
                    .sorted(Comparator.comparing(VicePosition::getOrder)
                            .thenComparing(VicePosition::getCreateTime))
                    .map(vicePosition -> {
                        StudyEntry studyEntry = new StudyEntry().touchName(vicePosition.getMainTitle())
                                .touchLabel(vicePosition.getLable())
                                .touchTagText(vicePosition.getTagText())
                                .touchLabelColor(vicePosition.getLabelColor())
                                .touchSubheading(vicePosition.getSubheading())
                                .touchLabelTextColor(vicePosition.getTagTextColor())
                                .touchIcon(StringUtils.isBlank(vicePosition.getIconUrl())?"":getUserAvatarImgUrl(vicePosition.getIconUrl()))
                                .touchBackImgUrl(StringUtils.isBlank(vicePosition.getBackImgUrl())?"":getUserAvatarImgUrl(vicePosition.getBackImgUrl()));
                        if (vicePosition.getLoginStatus()) {
                            if (null != parent) {
                                cardingStudyEntry(studyEntry, vicePosition);
                            } else {
                                studyEntry.touchFunctionTypeNative()
                                        .touchFunctionKey(StudyEntry.FunctionKey.LOGIN);
                            }
                        } else {
                            cardingStudyEntry(studyEntry, vicePosition);
                        }
                        return studyEntry;
                    })
                    .collect(Collectors.toList());
        }
        return entries;
    }

    public void cardingStudyEntry(StudyEntry studyEntry, VicePosition vicePosition) {
        if ("H5".equals(vicePosition.getFunctionType())) {
            //H5跳转
            if (RuntimeMode.isStaging()) {
                if (StringUtils.isNotBlank(vicePosition.getStagingJumpUrl())) {
                    studyEntry.touchFunctionTypeH5()
                            .touchFunctionKey(vicePosition.getStagingJumpUrl());
                } else {
                    studyEntry.touchFunctionTypeH5()
                            .touchFunctionKey(vicePosition.getJumpUrl());
                }
            } else {
                studyEntry.touchFunctionTypeH5()
                        .touchFunctionKey(vicePosition.getJumpUrl());
            }
        } else {
            //壳跳转
            studyEntry.touchFunctionTypeNative()
                    .touchFunctionKey(vicePosition.getJumpUrl());
        }
    }

    private Map<String, String> getTagConfig() {
        try {
            String tagTextConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "SELF_STUDY_RECOMMEND_TEXT");
            Map<String, String> tagTextMap = JsonUtils.fromJsonToMapStringString(tagTextConfig);
            if (MapUtils.isNotEmpty(tagTextMap)) {
                return tagTextMap;
            }
        } catch (IllegalArgumentException ignore) {
        }

        return new HashMap<>();
    }

    @RequestMapping(value = "/my_app.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage myApp() {
        try {
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_PARENT_ERROR_MSG);
        }

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        StudentDetail studentDetail = null;

        try {
            if (studentId != 0L) {
                studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            }

            if (studentDetail == null) {
                return successMessage().add("my_app", new HashMap<>());
            }

            IndexDataContext context = getIndexDataContext(parent, studentDetail);
            if (parent.getId().equals(20001L)) {
                //album dubbing  huiben
                List<StudyEntry> entries = Lists.newArrayList();
                StudyEntry albumEntry = processEntry(SelfStudyType.ALBUM, context, false, false, true);
                if (albumEntry != null) {
                    albumEntry.touchIcon(getCdnBaseUrlStaticSharedWithSep() + albumEntry.getIconUrl());
                    entries.add(albumEntry);
                }
                StudyEntry dubbingEntry = processEntry(SelfStudyType.DUBBING, context, false, false, true);
                if (dubbingEntry != null) {
                    dubbingEntry.touchIcon(getCdnBaseUrlStaticSharedWithSep() + dubbingEntry.getIconUrl());
                    entries.add(dubbingEntry);
                }
                StudyEntry readingEntry = processEntry(SelfStudyType.READING_ENGLISH, context, false, false, true);
                if (readingEntry != null) {
                    readingEntry.touchIcon(getCdnBaseUrlStaticSharedWithSep() + readingEntry.getIconUrl());
                    entries.add(readingEntry);
                }
                Map<String, Object> result = new HashMap<>();
                result.put("title", "我的自学应用");
                result.put("entry_list", entries);

                return successMessage().add("my_app", result);

            }

            Set<OrderProductServiceType> productServiceTypes = new HashSet<>();

            List<UserActivatedProduct> activatedProducts = new ArrayList<>();

            List<UserActivatedProduct> parentActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(parent.getId());
            if (CollectionUtils.isNotEmpty(parentActivatedProducts)) {
                activatedProducts.addAll(parentActivatedProducts);
            }
            if (0 != studentId) {
                List<UserActivatedProduct> studentActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(studentId);
                if (CollectionUtils.isNotEmpty(studentActivatedProducts)) {
                    activatedProducts.addAll(studentActivatedProducts);
                }
            }
            activatedProducts = activatedProducts.stream()
                    .filter(activated -> activated.getServiceEndTime().after(new Date()) || DateUtils.calculateDateDay(activated.getServiceEndTime(), 10).after(DayRange.current().getEndDate()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(activatedProducts)) {
                productServiceTypes = activatedProducts.stream().map(o -> OrderProductServiceType.safeParse(o.getProductServiceType())).collect(Collectors.toSet());
            }
            productServiceTypes.add(OrderProductServiceType.PicListenBook);

            Set<SelfStudyType> studyTypes = new HashSet<>();
            productServiceTypes.forEach(t -> {
                SelfStudyType selfStudyType = SelfStudyType.fromOrderType(t);
                if (null != selfStudyType && selfStudyType != SelfStudyType.UNKNOWN) {
                    studyTypes.add(selfStudyType);
                }
                if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) {
                    studyTypes.add(SelfStudyType.ZUOYECUOITI);
                }
                if (t == OrderProductServiceType.AfentiExamImproved) {
                    studyTypes.add(SelfStudyType.AFENTI_ENGLISH);
                }
                if (t == OrderProductServiceType.AfentiMathImproved) {
                    studyTypes.add(SelfStudyType.AFENTI_MATH);
                }
                if (t == OrderProductServiceType.AfentiChineseImproved) {
                    studyTypes.add(SelfStudyType.AFENTI_CHINESE);
                }
            });


            List<StudyEntry> entries = Lists.newLinkedList();
            //家长布置作业入口
            if (null != studentDetail) {
                for (String key : freeResourceRecomdList) {
                    boolean startHomeWorkStatus = HomeworkCacheManager.getHomeWorkCache(studentId, key);
                    StudyEntry parentHomeworkEntry = getParentHomeworkEntry(key, studentId);
                    if (parentHomeworkEntry != null && startHomeWorkStatus) {
                        if (studentDetail.getClazz() == null ||
                                (studentDetail.getClazz().isPrimaryClazz() && !studentDetail.getClazz().isTerminalClazz())) {
                            entries.add(parentHomeworkEntry);
                        }
                    }
                }
            }

            //其他应用处理
            for (SelfStudyType selfStudyType : SSTOrderList) {
                if (selfStudyType == SelfStudyType.ZUOYECUOITI) {
                    //判断家长是否有孩子
                    List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
                    long studentNum = students.stream().filter(Objects::nonNull).count();
                    if (studentNum <= 0) {
                        continue;
                    }
                }
                if (studyTypes.contains(selfStudyType)) {
                    StudyEntry studyEntry = processEntry(selfStudyType, context, false, false, true);
                    if (null != studyEntry) {
                        studyEntry.touchIcon(getCdnBaseUrlStaticSharedWithSep() + studyEntry.getIconUrl());
                        entries.add(studyEntry);

                        if (selfStudyType == SelfStudyType.CHIPS_ENGLISH) {
                            studyEntry.touchIcon(getCdnBaseUrlStaticSharedWithSep() + SelfStudyType.CHIPS_ENGLISH.getIconUrl())
                                    .touchFunctionTypeNative()
                                    .touchFunctionKey(StudyEntry.FunctionKey.FRIESENGLISH);
                        }
                        if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) {
                            studyEntry.touchFunctionTypeNative()
                                    .touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN);
                        }
                    }
                }
            }

            //如果已付费，且未过期或过期10天以内，则必须显示薯条英语
            if (studyTypes.contains(SelfStudyType.CHIPS_ENGLISH)) {
                boolean exists = entries.stream().anyMatch(entry -> entry.getSelfStudyType() == SelfStudyType.CHIPS_ENGLISH);
                if (!exists && !userBlacklistServiceClient.isInUserBlackList(context.getParent())) {
                    entries.add(StudyEntry.newInstance(SelfStudyType.CHIPS_ENGLISH, context.getVersion())
                            .touchIcon(getCdnBaseUrlStaticSharedWithSep() + SelfStudyType.CHIPS_ENGLISH.getIconUrl())
                            .touchFunctionTypeNative()
                            .touchFunctionKey(StudyEntry.FunctionKey.FRIESENGLISH));
                }
            }
            //小U绘本和小U语文绘本设置
            if (null != studentDetail && null != studentDetail.getClazz() &&
                    !userBlacklistServiceClient.isInUserBlackList(context.getParent())) {
                if (StringUtils.isNotBlank(context.getVersion()) && VersionUtil.compareVersion(context.getVersion(), "2.8.2") >= 0) {
                    if (studyTypes.contains(SelfStudyType.ELEVEL_READING)) {
                        boolean exists = entries.stream().anyMatch(entry -> entry.getSelfStudyType() == SelfStudyType.ELEVEL_READING);
                        if (!exists) {
                            entries.add(StudyEntry.newInstance(SelfStudyType.ELEVEL_READING)
                                    .touchLabel(SelfStudyType.ELEVEL_READING.getSubheading())
                                    .touchIcon(iconImageHeader + SelfStudyType.ELEVEL_READING.getIconUrl())
                                    .touchFunctionType(StudyEntry.FunctionType.ROUTER)
                                    .touchFunctionKey("a17parent://platform.17zuoye.client/levelreading?name=ELevelReading&unityType=ELevelReading&page_viewable=true&source=native&url="
                                            + URLEncoder.encode("u3d://ELevelReading?orientation=Portrait&productid=500_501&alias=小U绘本", "utf-8")));
                        }
                    }
                    if (studyTypes.contains(SelfStudyType.CLEVEL_READING)) {
                        boolean exists = entries.stream().anyMatch(entry -> entry.getSelfStudyType() == SelfStudyType.CLEVEL_READING);
                        if (!exists) {
                            entries.add(StudyEntry.newInstance(SelfStudyType.CLEVEL_READING)
                                    .touchLabel(SelfStudyType.CLEVEL_READING.getSubheading())
                                    .touchIcon(iconImageHeader + SelfStudyType.CLEVEL_READING.getIconUrl())
                                    .touchFunctionType(StudyEntry.FunctionType.ROUTER)
                                    .touchFunctionKey("a17parent://platform.17zuoye.client/levelreading?name=CLevelReading&unityType=CLevelReading&page_viewable=true&source=native&url="
                                            + URLEncoder.encode("u3d://CLevelReading?orientation=Portrait&productid=100502_101502&alias=小U语文", "utf-8")));
                        }
                    }
                }
            }

            Map<String, Object> result = Maps.newLinkedHashMap();
            result.put("title", "我的自学应用");
            result.put("entry_list", entries);

            return successMessage().add("my_app", result);
        } catch (Exception ex) {
            log.error("pid:{},sid:{}", currentUserId(), studentId, ex);
            return failMessage("系统异常");
        }
    }

    private StudyEntry getParentHomeworkEntry(String key, Long sid) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), key);
        if (StringUtils.isNotBlank(configValue)) {
            Map<String, Object> configMap = JsonUtils.fromJson(configValue);
            if (com.voxlearning.alps.repackaged.org.apache.commons.collections4.MapUtils.isNotEmpty(configMap)) {
                String name = SafeConverter.toString(configMap.get("name"));
                String label = SafeConverter.toString(configMap.get("label"));
                String icon = SafeConverter.toString(configMap.get("icon"));
                String url = ProductConfig.getMainSiteBaseUrl() + SafeConverter.toString(configMap.get("url"));
                if ("PARENT_HOMEWORK_ENTRY_NEW".equals(key)) {
                    if (url.contains("?")) {
                        url += "&referrer=4";
                    } else {
                        url += "?referrer=4";
                    }
                }
                if ("PARENT_MATH_PRACTICE_ENTRY_NEW".equals(key)) {
                    if (url.contains("?")) {
                        url += "&referrer=2&useNewCore=wk";
                    } else {
                        url += "?referrer=2&useNewCore=wk";
                    }
                }
                if (null != sid) {
                    if ("PARENT_QUICK_ORAL_ENTRY_NEW".equals(key)) {
                        if (url.contains("?")) {
                            url += "&sid=" + sid + "&type=mental&referrer=3";
                        } else {
                            url += "?sid=" + sid + "&type=mental&referrer=3";
                        }
                    }
                    if ("PARENT_ORAL_EXERCISE_ENTRY_NEW".equals(key)) {
                        if (url.contains("?")) {
                            url += "&sid=" + sid + "&referrer=2&useNewCore=wk";
                        } else {
                            url += "?sid=" + sid + "&referrer=2&useNewCore=wk";
                        }
                    }
                }
                String labelColor = SafeConverter.toString(configMap.get("labelColor"));
                String labelTextColor = SafeConverter.toString(configMap.get("labelTextColor"));
                StudyEntry studyEntry = new StudyEntry()
                        .touchName(name)
                        .touchLabel(label)
                        .touchIcon(icon);
                if (null != sid) {
                    studyEntry.touchFunctionTypeH5()
                            .touchFunctionKey(url);
                } else {
                    studyEntry.touchFunctionTypeNative()
                            .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD);
                }
                studyEntry.setLabelColor(labelColor);
                studyEntry.setLabelTextColor(labelTextColor);
                return studyEntry;
            }
        }
        return null;
    }

    @RequestMapping(value = "hot_activities.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage hotActivities() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            if (hasSessionKey()) {
                validateRequest(REQ_STUDENT_ID);
            } else {
                validateRequestNoSessionKey(REQ_STUDENT_ID);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_PARENT_ERROR_MSG);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Map<StudyEntry.ActivityPosition, NewAdMapper> adMapperMap = new HashMap<>();
        Arrays.stream(StudyEntry.ActivityPosition.values()).forEach(position -> {
            List<NewAdMapper> adMappers = userAdvertisementServiceClient.getUserAdvertisementService().loadNewAdvertisementData(parent.getId(), position.getAdSlotId(), getClientSys(), getClientVersion());
            if (CollectionUtils.isNotEmpty(adMappers)) {
                adMapperMap.put(position, adMappers.get(0));
            }
        });
        List<Map<String, Object>> adList = new ArrayList<>();
        if (adMapperMap.size() >= 2) {
            for (StudyEntry.ActivityPosition position : StudyEntry.ActivityPosition.values()) {
                NewAdMapper adMapper = adMapperMap.get(position);
                if (adMapper != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", adMapper.getId());
                    map.put("title", adMapper.getName());
                    map.put("sub_title", adMapper.getContent());
                    map.put("tag", adMapper.getDescription());
                    map.put("img", getCdnBaseUrlStaticSharedWithSep() + "gridfs/" + adMapper.getImg());
                    map.put("url", fetchMainsiteUrlByCurrentSchema() + AdvertiseRedirectUtils.redirectUrl(adMapper.getId(), 0, getClientVersion(), getClientSys(), "", studentId));
                    adList.add(map);
                    if (Boolean.TRUE.equals(adMapper.getLogCollected())) {
                        LogCollector.info("sys_new_ad_show_logs",
                                MiscUtils.map(
                                        "user_id", parent.getId(),
                                        "env", RuntimeMode.getCurrentStage(),
                                        "version", getRequestString("version"),
                                        "aid", adMapper.getId(),
                                        "acode", adMapper.getCode(),
                                        "index", 0,
                                        "slotId", position.getAdSlotId(),
                                        "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                        "time", DateUtils.dateToString(new Date()),
                                        "agent", getRequest().getHeader("User-Agent"),
                                        "uuid", UUID.randomUUID().toString(),
                                        "system", getRequestString(REQ_SYS),
                                        "system_version", getRequestString("sysVer")
                                ));
                    }
                }
                if (adMapperMap.size() < 4 && adList.size() == 2) {
                    break;
                }
            }
        }
        return successMessage().add("list", adList);
    }

    @RequestMapping(value = "study_resource.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage myStudyTool() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            if (hasSessionKey()) {
                validateRequest(REQ_STUDENT_ID);
            } else {
                validateRequestNoSessionKey(REQ_STUDENT_ID);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        User parent = getCurrentParent();
        if (hasSessionKey() && parent == null) {
            return failMessage(RES_RESULT_PARENT_ERROR_MSG);
        }

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        StudentDetail studentDetail = null;
        if (hasSessionKey() && studentId != 0) {
            studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        }

        IndexDataContext context = getIndexDataContext(parent, studentDetail);

        // remindContentAlpsFuture
        AlpsFuture<LiveCastIndexRemind> remindContentAlpsFuture = mySelfStudyService.loadStudentLiveCastRemindV2(studentDetail);

        MapMessage successMessage = successMessage();
        //多线程异步处理。本想用 hydra 做异步会更自然，但由于实现的依赖问题，只能在 wsd 里做异步
        final CountDownLatch downLatch = new CountDownLatch(6);
        AlpsThreadPool.getInstance().submit(() -> {
            try {
                Map<String, Object> mySelectedStudyToolsMap = mySelectedStudyToolsMap(context);
                if (mySelectedStudyToolsMap != null) {
                    successMessage.add("my_selected", mySelectedStudyToolsMap);
                }
            } catch (Exception e) {
                logger.error("generate mySelectedStudyToolsMap error : {}", e);
            } finally {
                downLatch.countDown();
            }
        });
        AlpsThreadPool.getInstance().submit(() -> {
            try {
                Map<String, Object> refinedLessonMap = refinedLessonMap(context);
                if (refinedLessonMap != null) {
                    successMessage.add("recommend_refined_lesson", refinedLessonMap);
                }
            } catch (Exception e) {
                logger.error("generate refinedLessonMap error : {}", e);
            } finally {
                downLatch.countDown();
            }
        });

        if (hasSessionKey() && context.getStudentDetail() != null) {
            //2.2.2新增固定4个常规活动入口
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    Map<String, Object> hotActivitiesMap = hotActivityMap(context);
                    if (hotActivitiesMap != null) {
                        successMessage.add("hot_activitie", hotActivitiesMap);
                    }
                } catch (Exception e) {
                    logger.error("generate hotActivitiesMap error : {}", e);
                } finally {
                    downLatch.countDown();
                }
            });
        } else {
            downLatch.countDown();
        }

        if (hasSessionKey() && studentDetail != null && !context.isInBlackList()) {
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    Map<String, Object> subjectSyncTrainingMap = subjectSyncTrainingMap(context);
                    successMessage.add("subject_synchronous_training", subjectSyncTrainingMap);
                } catch (Exception e) {
                    logger.error("generate subjectSyncTrainingMap error : {}", e);
                } finally {
                    downLatch.countDown();
                }

            });
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    Map<String, Object> capabilityUpgradingMap = capabilityUpgradingMap(context);
                    successMessage.add("capability_upgrading", capabilityUpgradingMap);
                } catch (Exception e) {
                    logger.error("generate capabilityUpgradingMap error : {}", e);
                } finally {
                    downLatch.countDown();
                }
            });
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    Map<String, Object> afterSchoolExtendMap = afterSchoolExtendMap(context);
                    successMessage.add("after_school_extend", afterSchoolExtendMap);
                } catch (Exception e) {
                    logger.error("generate afterSchoolExtendMap error : {}", e);
                } finally {
                    downLatch.countDown();
                }
            });

        } else {
            downLatch.countDown();
            downLatch.countDown();
            downLatch.countDown();
        }

        //直播活动入口
        if (!context.isHitLiveCastAdIgnoreGray() && context.isLogin() && context.isHasChild()) {
            LiveCastIndexRemind liveCastRemind = remindContentAlpsFuture.getUninterruptibly();
            if (liveCastRemind != null) {
                LiveCastIndexRemind.ImageContent imageContent = liveCastRemind.getImageContent();
                if (context.isAbove223()) {
                    if (imageContent != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("backgroud_color", imageContent.getBackGroundColor());
                        map.put("left_image_url", imageContent.getLeftImageUrl());
                        map.put("text", imageContent.getText());
                        map.put("button_image_url", imageContent.getButtonImageUrl());
                        if (imageContent.safeIsNeedLogin()) {
                            map.put("jump_url", context.getCurrentSchema() + "/redirector/goaoshu.vpage?returnURL=" + imageContent.getUrl());
                        } else {
                            map.put("jump_url", imageContent.getUrl());
                        }
                        Map<String, Object> dotInfo = new HashMap<>();
                        dotInfo.put("s0", imageContent.getContentId());
                        dotInfo.put("s1", imageContent.getDotId());
                        dotInfo.put("s2", imageContent.getBackGroundColor().name());
                        map.put("dot_info", dotInfo);
                        successMessage.add("livecast_image_remind", map);
                    }
                } else {
                    LiveCastIndexRemind.RemindContent remindContent = liveCastRemind.getRemindContent();
                    if (remindContent != null) {
                        Map<String, Object> liveCastRemindMap = new HashMap<>();
                        liveCastRemindMap.put("icon", remindContent.getIconType().name());
                        liveCastRemindMap.put("text", remindContent.getText());
                        if (remindContent.safeIsNeedLogin()) {
                            liveCastRemindMap.put("url", context.getCurrentSchema() + "/redirector/goaoshu.vpage?returnURL=" + remindContent.getUrl());
                        } else {
                            liveCastRemindMap.put("url", remindContent.getUrl());
                        }
                        Map<String, Object> dotInfo = new HashMap<>();
                        dotInfo.put("s0", remindContent.getContentId());
                        dotInfo.put("s1", remindContent.getDotId());
                        liveCastRemindMap.put("dot_info", dotInfo);
                        if (StringUtils.isNotBlank(remindContent.getImageUrl()) && context.isHitLiveCastImageAdGray()) {
                            liveCastRemindMap.put("image_url", remindContent.getImageUrl());
                            dotInfo.put("s0", remindContent.getImageDotId());
                            dotInfo.put("s1", remindContent.getImageContentId());
                        }
                        successMessage.add("livecast_remind", liveCastRemindMap);
                    }
                }
            }
            if (context.isAbove223() && !successMessage.containsKey("livecast_image_remind")) {
                List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService().loadNewAdvertisementData(context.getStudentDetail().getId(), "220111", context.getSys(), context.getVersion());
                if (CollectionUtils.isNotEmpty(newAdMappers)) {
                    NewAdMapper newAdMapper = newAdMappers.get(0);
                    Map<String, Object> map = new HashMap<>();
                    String content = newAdMapper.getContent();
                    LiveCastIndexRemind.BackGroundColor backGroundColor = LiveCastIndexRemind.BackGroundColor.safeParse(content);
                    map.put("backgroud_color", backGroundColor);
                    map.put("left_image_url", context.getCurrentCdnSchema() + "gridfs/" + newAdMapper.getImg());
                    map.put("text", newAdMapper.getName());
                    map.put("button_image_url", context.getCurrentCdnSchema() + "gridfs/" + newAdMapper.getGif());
                    map.put("jump_url", context.getCurrentSchema() + AdvertiseRedirectUtils.redirectUrl(newAdMapper.getId(), 0, context.getVersion(), context.getSys(), "", context.getStudentDetail().getId()));
                    if (Boolean.TRUE.equals(newAdMapper.getLogCollected())) {
                        LogCollector.info("sys_new_ad_show_logs",
                                MiscUtils.map("user_id", context.getStudentDetail().getId(),
                                        "env", RuntimeMode.getCurrentStage(),
                                        "version", context.getVersion(),
                                        "aid", newAdMapper.getId(),
                                        "acode", newAdMapper.getCode(),
                                        "index", 0,
                                        "slotId", "220111",// TODO: 2018/4/27 广告位id
                                        "client_ip", "",
                                        "time", DateUtils.dateToString(new Date()),
                                        "agent", "",
                                        "uuid", UUID.randomUUID().toString(),
                                        "system", context.getSys()
                                ));
                    }
                    successMessage.add("livecast_image_remind", map);
                }
            }
        }


        //2.2.2又他么的新增了更多学习工具
        Map<String, Object> moreStudyToolsMap = moreStudyToolsMap(context);
        if (moreStudyToolsMap != null) {
            successMessage.add("more_study_tools_map", moreStudyToolsMap);
        }

        try {
            downLatch.await();
        } catch (InterruptedException e) {
            logger.warn("downLatch await error, {}", e);
        }

        return successMessage;
    }

    private IndexDataContext getIndexDataContext(User parent, StudentDetail studentDetail) {
        IndexDataContext context = IndexDataContext.empty;
        if (hasSessionKey() && null != studentDetail) {
            context = asyncLoadIndexDataContext(parent, studentDetail);
        }
        context.setCurrentSchema(fetchMainsiteUrlByCurrentSchema());
        context.setCurrentCdnSchema(getCdnBaseUrlStaticSharedWithSep());
        context.setChannel(indexChannel);
        context.setParent(parent);
        context.setStudentDetail(studentDetail);
        context.setLogin(hasSessionKey());
        context.setHasChild(null != studentDetail);
        context.setVersion(getClientVersion());
        context.setSys(getClientSys());
        context.setPosition(IndexDataContext.Position.index);

        if (null == studentDetail) {
            context.setHitAdIgnoreGray(false);
            context.setHitLiveCastAdIgnoreGray(false);
            context.setHitLiveCastImageAdGray(false);
            context.setHitShutiaoGray(false);
        } else {
            boolean hitAdIgnoreGray = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(studentDetail, "jztapp", "indexAdBlack", true);
            context.setHitAdIgnoreGray(hitAdIgnoreGray);

            boolean hitLiveCastAdIgnoreGray = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(studentDetail, "jztapp", "liveCastAdBlack", true);
            context.setHitLiveCastAdIgnoreGray(hitLiveCastAdIgnoreGray);

            boolean hitLiveCastImageAdGray = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(studentDetail, "jztapp", "liveCastImageAd", true);
            context.setHitLiveCastImageAdGray(hitLiveCastImageAdGray);

            boolean hitShutiaoGray = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(studentDetail, "jztapp", "shutiaoEntry", true);
            context.setHitShutiaoGray(hitShutiaoGray);
        }
        return context;
    }

    private Map<String, Object> moreStudyToolsMap(IndexDataContext indexDataContext) {
        List<StudyEntry> moreStudyToolsList;
        if (!indexDataContext.isLogin()) {
            if (StringUtils.isNotBlank(indexDataContext.getVersion()) && VersionUtil.compareVersion(indexDataContext.getVersion(), "2.3.0") >= 0) {
                moreStudyToolsList = noLoginMoreStudyToolsEntryList_V3(indexDataContext);
            } else {
                moreStudyToolsList = noLoginMoreStudyToolsEntryList(indexDataContext);
            }
        } else {
            if (indexDataContext.isShenhe()) {
                if (StringUtils.isNotBlank(indexDataContext.getVersion()) && VersionUtil.compareVersion(indexDataContext.getVersion(), "2.3.0") >= 0) {
                    moreStudyToolsList = shenheMoreStudyToolsEntryList_V3(indexDataContext);
                } else {
                    moreStudyToolsList = shenheMoreStudyToolsEntryList(indexDataContext);
                }
            } else {
                if (!indexDataContext.isHasChild()) {
                    if (StringUtils.isNotBlank(indexDataContext.getVersion()) && VersionUtil.compareVersion(indexDataContext.getVersion(), "2.3.0") >= 0) {
                        moreStudyToolsList = noChildMoreStudyToolsEntryList_V3(indexDataContext);
                    } else {
                        moreStudyToolsList = noChildMoreStudyToolsEntryList(indexDataContext);
                    }
                } else {
                    if (StringUtils.isNotBlank(indexDataContext.getVersion()) && VersionUtil.compareVersion(indexDataContext.getVersion(), "2.3.0") >= 0) {
                        moreStudyToolsList = normalMoreStudyToolsEntryList_V3(indexDataContext);
                    } else {
                        moreStudyToolsList = normalMoreStudyToolsEntryList(indexDataContext);
                    }
                }
            }
        }
        dealIconCdnAndGlobalMSg(moreStudyToolsList, indexDataContext);
        moreStudyToolsList.forEach(t -> dealDefaultLabel2Desc(t, indexDataContext, false));
        Map<String, Object> map = new HashMap<>();
        map.put("title", "更多学习工具");
        map.put("entry_list", moreStudyToolsList);
        return map;
    }

    private IndexDataContext asyncLoadIndexDataContext(User parent, StudentDetail studentDetail) {
        IndexDataContext context = new IndexDataContext();
        context.setParent(parent);
        context.setStudentDetail(studentDetail);
        context.setVersion(getClientVersion());
        context.setSys(getClientSys());
        final CountDownLatch latch = new CountDownLatch(3);
        AlpsThreadPool.getInstance().submit(() -> {
            // sst2FairyLandProductMap
            try {
                Map<SelfStudyType, FairylandProduct> sst2FairyLandProductMap = new HashMap<>();
                if (parent != null && studentDetail != null) {
                    List<FairylandProduct> parentAvailableFairylandProducts =
                            businessVendorServiceClient.getParentAvailableFairylandProducts(parent, studentDetail, FairyLandPlatform.PARENT_APP, FairylandProductType.APPS);

                    for (FairylandProduct fairylandProduct : parentAvailableFairylandProducts) {
                        OrderProductServiceType orderProductServiceType = OrderProductServiceType.safeParse(fairylandProduct.getAppKey());
                        if (orderProductServiceType == OrderProductServiceType.Unknown) {
                            continue;
                        }
                        SelfStudyType selfStudyType = SelfStudyType.fromOrderType(OrderProductServiceType.valueOf(fairylandProduct.getAppKey()));
                        if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN) {
                            continue;
                        }
                        sst2FairyLandProductMap.put(selfStudyType, fairylandProduct);
                    }
                }
                context.setSst2FairyLandProductMap(sst2FairyLandProductMap);
            } catch (Exception e) {
                logger.error("generate sst2FairyLandProductMap error : ", e);
                context.setSst2FairyLandProductMap(new HashMap<>());
            } finally {
                latch.countDown();
            }

        });

        AlpsThreadPool.getInstance().submit(() -> {
            // selfStudyTypeDayRangeMap
            try {
                if (studentDetail != null) {
                    Map<SelfStudyType, DayRange> selfStudyTypeDayRangeMap = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentDetail.getId(), true);
                    context.setSelfStudyTypeDayRangeMapKeepExpired(selfStudyTypeDayRangeMap);
                    Map<SelfStudyType, DayRange> map = new HashMap<>();
                    Date now = new Date();
                    selfStudyTypeDayRangeMap.forEach((k, v) -> {
                        if (v.getEndDate().after(now))
                            map.put(k, v);
                    });
                    context.setSelfStudyTypeDayRangeMa(map);
                } else {
                    context.setSelfStudyTypeDayRangeMa(new HashMap<>());
                }
                boolean inUserBlackList = parent != null && userBlacklistServiceClient.isInUserBlackList(parent)
                        || (studentDetail != null && userBlacklistServiceClient.isInUserBlackList(studentDetail));
                context.setInBlackList(inUserBlackList);
            } catch (Exception e) {
                logger.error("generate selfStudyTypeDayRangeMap error : ", e);
                context.setSelfStudyTypeDayRangeMa(new HashMap<>());
            } finally {
                latch.countDown();
            }
        });

        AlpsThreadPool.getInstance().submit(() -> {
            try {
                if (studentDetail != null) {
                    Map<String, String> appKey2DescMap =
                            businessVendorServiceClient.fetchUserUseNumDesc(
                                    allShelfSSTList.stream().filter(t -> t.getOrderProductServiceType() != null && OrderProductServiceType.safeParse(t.getOrderProductServiceType()) != OrderProductServiceType.Unknown).map(t -> t.getOrderProductServiceType()).collect(Collectors.toList()), studentDetail);
                    context.setAppKey2DescMap(appKey2DescMap);
                } else {
                    context.setAppKey2DescMap(Collections.emptyMap());
                }
            } catch (Exception e) {
                logger.error("generate appKey2DescMap error : ", e);
                context.setAppKey2DescMap(Collections.emptyMap());
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<SelfStudyType, AlpsFuture<StudyAppData>> studyDataFutureMap
                = getUserStudyDataFutureMap(SSTOrderMap.keySet(), parent, studentDetail);
        context.setStudyAppDataFutureMap(studyDataFutureMap);
        context.setCurrentSchema(fetchMainsiteUrlByCurrentSchema());
        context.setCurrentCdnSchema(getCdnBaseUrlStaticSharedWithSep());
        context.setParent(parent);
        context.setStudentDetail(studentDetail);
        context.setLogin(parent != null);
        context.setHasChild(studentDetail != null);
        context.setShenhe(parent == null ? false : isShenhe(parent.getId()));
        context.setHasZuoYeCuoTi(studentHasZuoyecuotiEntry(studentDetail));
        return context;
    }

    private Map<String, Object> hotActivityMap(IndexDataContext indexContext) {

        Map<StudyEntry.ActivityPosition, NewAdMapper> positionAdMapperMap = new HashMap<>();
        Arrays.stream(StudyEntry.ActivityPosition.values()).forEach(t -> {
            List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService().loadNewAdvertisementData(indexContext.getStudentDetail().getId(), t.getAdSlotId(), indexContext.getSys(), indexContext.getVersion());
            if (CollectionUtils.isNotEmpty(newAdMappers)) {
                NewAdMapper newAdMapper = newAdMappers.get(0);
                positionAdMapperMap.put(t, newAdMapper);
            }
        });
        if (MapUtils.isEmpty(positionAdMapperMap)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("title", "热门活动推荐");
        List<StudyEntry> entryList = new ArrayList<>();
        Map<Integer, NewAdMapper> indexAdMap = new HashMap<>();
        int index = 0;
        for (StudyEntry.ActivityPosition activityPosition : StudyEntry.ActivityPosition.values()) {
            if (entryList.size() == 3) {
                break;
            }
            NewAdMapper newAdMapper = positionAdMapperMap.get(activityPosition);
            if (newAdMapper == null) {
                continue;
            }
            if (entryList.size() >= 2 && activityPosition == StudyEntry.ActivityPosition.BACKUP) {
                continue;
            }
            indexAdMap.put(index, newAdMapper);
            StudyEntry studyEntry = StudyEntry.newInstance(activityPosition, newAdMapper);
            if (studyEntry != null) {
                entryList.add(studyEntry);
                //这里有个非常恶心的逻辑，首先这里至少有2个广告；第一个一定是长方形图；如果有2个广告，第二个广告也是长方形图；如果有3个广告，第二个 第三个是正方形广告
                //长方形用ad的git，正方形是img
                if (entryList.size() == 1) {
                    studyEntry.setIconUrl(indexContext.getCurrentCdnSchema() + "gridfs/" + newAdMapper.getGif());
                } else if (entryList.size() == 2) {
                    entryList.get(1).setIconUrl(indexContext.getCurrentCdnSchema() + "gridfs/" + newAdMapper.getGif());
                } else if (entryList.size() == 3) {
                    entryList.get(1).setIconUrl(indexContext.getCurrentCdnSchema() + "gridfs/" + indexAdMap.get(1).getImg());
                    entryList.get(2).setIconUrl(indexContext.getCurrentCdnSchema() + "gridfs/" + newAdMapper.getImg());
                }
                studyEntry.setFunctionKey(indexContext.getCurrentSchema() + AdvertiseRedirectUtils.redirectUrl(newAdMapper.getId(), 0, indexContext.getVersion(), indexContext.getSys(), "", indexContext.getStudentDetail().getId()));
                if (Boolean.TRUE.equals(newAdMapper.getLogCollected())) {
                    LogCollector.info("sys_new_ad_show_logs",
                            MiscUtils.map("user_id", indexContext.getStudentDetail().getId(),
                                    "env", RuntimeMode.getCurrentStage(),
                                    "version", indexContext.getVersion(),
                                    "aid", newAdMapper.getId(),
                                    "acode", newAdMapper.getCode(),
                                    "index", 0,
                                    "slotId", activityPosition.getAdSlotId(),
                                    "client_ip", "",
                                    "time", DateUtils.dateToString(new Date()),
                                    "agent", "",
                                    "uuid", UUID.randomUUID().toString(),
                                    "system", indexContext.getSys()
                            ));
                }
                index++;
            }
        }
        if (entryList.size() <= 1) {
            return null;
        }
        int size = entryList.size();
        for (int i = 0; i < size; i++) {
            StudyEntry studyEntry = entryList.get(i);
            switch (i) {
                case 0:
                    studyEntry.setLabelColor(StudyEntry.LabelColor.ORANGE.getColor());
                    break;
                case 1:
                    studyEntry.setLabelColor(StudyEntry.LabelColor.YELLOW.getColor());
                    break;
                case 2:
                    studyEntry.setLabelColor(StudyEntry.LabelColor.BLUE.getColor());
                    break;
                default:
                    studyEntry.setLabelColor(StudyEntry.LabelColor.BLUE.getColor());
                    break;
            }
        }
        map.put("entry_list", entryList);
        return map;
    }

    private final static Date EAGLETSINOLOGY_CLASSROOM_DATE = RuntimeMode.isUsingProductionData() ? DateUtils.stringToDate("2018-04-02 23:59:59") : DateUtils.stringToDate("2018-03-02 23:59:59");

    /**
     * 2.2.2版本开始，只有登录有孩子有班级才显示已选
     * 2.2.2开始，固定第一位是家长奖励，然后是两个运营位，然后是6个家长自选的，外加一个更多（更多这个坑接口不返回，壳自己处理）；共计是十个坑；
     */
    private Map<String, Object> mySelectedStudyToolsMap(IndexDataContext indexDataContext) {
        List<StudyEntry> entryList;
        Map<String, Object> map = new HashMap<>();
        map.put("title", "我的学习必备");
        if (indexDataContext.isShenhe()) {
            entryList = shenheSelectedToolsEntryList(indexDataContext);
            entryList.forEach(t -> dealDefaultLabel2Desc(t, indexDataContext, false));
            dealIconCdnAndGlobalMSg(entryList, indexDataContext);
            map.put("show_more", false);
            map.put("entry_list", entryList);
            return map;
        }
        if (indexDataContext.isAbove223()
                && (!indexDataContext.isLogin() || indexDataContext.getStudentDetail() == null || indexDataContext.getStudentDetail().getClazz() == null)) {
            return null;
        }

        boolean showMore = false;
        if (!indexDataContext.isLogin()) {
            entryList = noLoginMySelectedToolsEntryList(indexDataContext);
        } else {
            if (indexDataContext.isShenhe()) {
                entryList = shenheSelectedToolsEntryList(indexDataContext);
            } else {
                if (!indexDataContext.isHasChild()) {
                    entryList = noChildMySelectedToolsEntryList(indexDataContext);
                } else {
                    if (indexDataContext.getStudentDetail() != null && indexDataContext.getStudentDetail().getClazz() != null && !indexDataContext.getStudentDetail().getClazz().isTerminalClazz()) {
                        showMore = true;
                    }
                    entryList = mySelectedToolsEntry(indexDataContext);
                }
            }
        }
        map.put("show_more", showMore);
        entryList.forEach(t -> dealDefaultLabel2Desc(t, indexDataContext, false));

        LinkedList<StudyEntry> entryLinkedList = new LinkedList<>(entryList);
        //2.2.2版本第一位放固定家长奖励入口
        boolean showParentRewardEntry = indexDataContext.isAbove223()
                && indexDataContext.getStudentDetail() != null
                && indexDataContext.getStudentDetail().getClazz() != null
                && (
                (indexDataContext.getStudentDetail().isPrimaryStudent() && !indexDataContext.getStudentDetail().getClazz().isTerminalClazz())
                        || indexDataContext.getStudentDetail().isInfantStudent()
        );
        if (showParentRewardEntry) {
            entryLinkedList.addFirst(parentRewardEntry(indexDataContext));
        }
        dealIconCdnAndGlobalMSg(entryLinkedList, indexDataContext);

        //低于2.2.2，放在后面。高于2.2.2，则把广告入口放到运营位
        Date nowDate = new Date();

        Map<SelfStudyType, DayRange> selfStudyTypeDayRangeMap = indexDataContext.getSelfStudyTypeDayRangeMa();
        if (entryLinkedList.size() < 6 || (indexDataContext.isAbove223() && entryLinkedList.size() < 9)) {
            if (indexDataContext.getStudentDetail() != null
                    && indexDataContext.getStudentDetail().getClazz() != null
                    && indexDataContext.getStudentDetail().getClazz().isPrimaryClazz()
                    && !indexDataContext.getStudentDetail().getClazz().isTerminalClazz()) {
                DayRange dayRange = selfStudyTypeDayRangeMap.get(SelfStudyType.EAGLETSINOLOGY_CLASSROOM);

                if (nowDate.before(EAGLETSINOLOGY_CLASSROOM_DATE) || (dayRange != null && dayRange.getEndDate().after(nowDate))) {
                    //加一个活动的广告
                    StudyEntry adEntry = StudyEntry.newInstance(SelfStudyType.UNKNOWN, indexDataContext.getVersion());
                    adEntry.setName("小鹰国学堂");
                    adEntry.setLabel("24节课解密国学");
                    adEntry.setIconUrl(indexDataContext.getCurrentCdnSchema() + "/public/skin/parentMobile/images/app_icon/icon_EagletSinologyClassRoom.png");
                    adEntry.touchFunctionTypeH5()
                            .touchFunctionKey(indexDataContext.getCurrentSchema() + "/view/mobile/activity/parent/sinology/index.vpage?order_ref=330181&rel=xxbb");
                    adEntry.toucheTag(fetchTag(SelfStudyType.UNKNOWN, IndexDataContext.Position.index, true));
                    if (indexDataContext.isAbove223()) {
                        entryLinkedList.add(1, adEntry);
                        adEntry.setIconUrl(indexDataContext.getCurrentCdnSchema() + "/public/skin/parentMobile/images/app_icon/circle_guoxuetang.png");
                    } else {
                        entryLinkedList.addLast(adEntry);
                    }
                }
            }
        }


        //加9.9直播客入口 http://project.17zuoye.net/redmine/issues/65096
        if ((entryLinkedList.size() < 6 || (indexDataContext.isAbove223() && entryLinkedList.size() < 9)) && indexDataContext.getParent() != null) {
            List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService().loadNewAdvertisementData(indexDataContext.getParent().getId(), "220106", indexDataContext.getSys(), indexDataContext.getVersion());
            if (CollectionUtils.isNotEmpty(newAdMappers)) {
                NewAdMapper newAdMapper = newAdMappers.get(0);
                if (newAdMapper != null) {
                    StudyEntry adEntry = StudyEntry.newInstance(SelfStudyType.UNKNOWN, indexDataContext.getVersion());
                    adEntry.setName(newAdMapper.getName());
                    adEntry.setLabel(newAdMapper.getContent());
                    adEntry.setIconUrl(indexDataContext.getCurrentCdnSchema() + "gridfs/" + newAdMapper.getImg());
                    adEntry.touchFunctionTypeH5()
                            .touchFunctionKey(newAdMapper.getUrl());
                    adEntry.toucheTag(fetchTag(SelfStudyType.UNKNOWN, IndexDataContext.Position.index, true));
                    if (indexDataContext.isAbove223()) {
                        entryLinkedList.add(1, adEntry);
                    } else {
                        entryLinkedList.addLast(adEntry);
                    }
                }
            }
        }


        //薯条英语入口
        if (entryLinkedList.size() < 6 || (indexDataContext.isAbove223() && entryLinkedList.size() < 9)) {

            if (indexDataContext.getStudentDetail() != null
                    && indexDataContext.getStudentDetail().getClazz() != null
                    && indexDataContext.getStudentDetail().getClazz().isPrimaryClazz()
                    && !indexDataContext.getStudentDetail().getClazz().isTerminalClazz()
                    && VersionUtil.compareVersion(indexDataContext.getVersion(), "2.2.6.0") > 0
                    && !indexDataContext.isInBlackList()) {
                StudyEntry adEntry = StudyEntry.newInstance(SelfStudyType.UNKNOWN, indexDataContext.getVersion());
                adEntry.setName("薯条英语");
                adEntry.touchFunctionTypeNative()
                        .touchFunctionKey(StudyEntry.FunctionKey.FRIESENGLISH);
                adEntry.toucheTag(fetchTag(SelfStudyType.UNKNOWN, IndexDataContext.Position.index, true));
                entryLinkedList.add(1, adEntry);
                adEntry.setIconUrl(indexDataContext.getCurrentCdnSchema() + "/public/skin/parentMobile/images/app_icon/circle_shutiao.png");
            }
        }

        map.put("entry_list", entryLinkedList);
        if ("ios".equalsIgnoreCase(indexDataContext.getSys()) || VersionUtil.compareVersion(indexDataContext.getVersion(), "2.0.5.1210") >= 0) {
            map.put("more_reminder_id", "xxxx123ccd32");
        }
        return map;
    }

    private StudyEntry parentRewardEntry(IndexDataContext indexDataContext) {
        StudyEntry entry = StudyEntry.newInstance(SelfStudyType.UNKNOWN, indexDataContext.getVersion());
        entry.setName("家长奖励");
        entry.setIconUrl("/public/skin/parentMobile/images/app_icon/circle_parent_reward.gif");
        entry.setFunctionType(StudyEntry.FunctionType.H5);
        String url = indexDataContext.getCurrentSchema() + "/view/mobile/parent/rewards/detail.vpage?ref=topball";
        boolean inGray = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(indexDataContext.getStudentDetail(), "ParentApp", "wk");
        if (inGray) {
            url += "&useNewCore=wk";
        }
        entry.setFunctionKey(url);
        return entry;
    }

    private void dealIconCdnAndGlobalMSg(List<StudyEntry> entryList, IndexDataContext indexDataContext) {
        //添加全局的消息
        Map<SelfStudyType, MySelfStudyEntryGlobalMsg> mySelfStudyEntryGlobalMsgMap = mySelfStudyGlobalMsgServiceClient.getMySelfStudyEntryGlobalMsgMap();
        String currentCdnSchema = indexDataContext.getCurrentCdnSchema();
        entryList.forEach(t -> {
            if (t == null) {
                return;
            }
            t.setIconUrl(currentCdnSchema + t.getIconUrl());
            MySelfStudyEntryGlobalMsg mySelfStudyEntryGlobalMsg = mySelfStudyEntryGlobalMsgMap.get(t.getSelfStudyType());
            if (mySelfStudyEntryGlobalMsg != null && StringUtils.isNotBlank(mySelfStudyEntryGlobalMsg.getText())) {
                t.touchGlobalMsg(mySelfStudyEntryGlobalMsg);
            }
        });
    }

    private void dealDefaultLabel2Desc(StudyEntry studyEntry, IndexDataContext indexDataContext, boolean force) {
        if (studyEntry == null) {
            return;
        }
        SelfStudyType selfStudyType = studyEntry.getSelfStudyType();
        if (selfStudyType == null) {
            return;
        }
        if (!force && StringUtils.isNotBlank(studyEntry.getLabel())) {
            return;
        }
        String defaultLabel = SSTDescMap.get(selfStudyType);
        if (StringUtils.isNotBlank(defaultLabel)) {
            studyEntry.setLabel(defaultLabel);
        } else {
            Map<SelfStudyType, FairylandProduct> sst2FairyLandProductMap = indexDataContext.getSst2FairyLandProductMap();
            if (sst2FairyLandProductMap == null) {
                return;
            }
            FairylandProduct fairylandProduct = sst2FairyLandProductMap.get(selfStudyType);
            if (fairylandProduct == null) {
                return;
            }
            studyEntry.setLabel(fairylandProduct.getProductDesc());
        }
    }

    private List<StudyEntry> mySelectedToolsEntry(IndexDataContext indexDataContext) {
        StudyToolShelf studyToolShelf = parentSelfStudyService.loadStudentStudyToolShelf(indexDataContext.getStudentDetail().getId());
        List<SelfStudyType> selfStudyTypes;
        if (studyToolShelf == null) {
            List<SelfStudyType> defaultSStList = new ArrayList<>();
            boolean zuoyecuotiIn = indexDataContext.isHasZuoYeCuoTi();
            if (zuoyecuotiIn) {
                defaultSStList.add(SelfStudyType.ZUOYECUOITI);
            }
            defaultSStList.add(SelfStudyType.PICLISTEN_ENGLISH);
            defaultSStList.add(SelfStudyType.ALBUM);
            defaultSStList.add(SelfStudyType.DUBBING);
            defaultSStList = defaultSStList.stream().sorted(Comparator.comparing(o -> SSTOrderMap.getOrDefault(o, 100000))).collect(Collectors.toList());
            String lock = "initStudentStudyTools_" + indexDataContext.getStudentDetail().getId();
            try {
                AtomicLockManager.getInstance().acquireLock(lock);
                parentSelfStudyService.updateStudentStudyToolShelf(indexDataContext.getStudentDetail().getId(), defaultSStList);
            } catch (CannotAcquireLockException ignore) {

            } finally {
                AtomicLockManager.getInstance().releaseLock(lock);
                selfStudyTypes = defaultSStList;
            }

        } else {
            selfStudyTypes = studyToolShelf.toSelfStudyTypeList().stream().filter(t -> t != SelfStudyType.LIVECAST).collect(Collectors.toList()); // FIXME: 2017/10/27 暂时过滤掉直播客
        }

        return selfStudyTypes.stream().map(sst -> processEntry(sst, indexDataContext, true, false, false)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static Map<SelfStudyType, String> CIRCLE_ICON_URL_MAP = new HashMap<>();

    static {
        CIRCLE_ICON_URL_MAP.put(SelfStudyType.PICLISTEN_ENGLISH, "/public/skin/parentMobile/images/app_icon/circle_piclisten.png");
        CIRCLE_ICON_URL_MAP.put(SelfStudyType.ALBUM, "/public/skin/parentMobile/images/app_icon/icon_album_yuan.png");
        CIRCLE_ICON_URL_MAP.put(SelfStudyType.DUBBING, "/public/skin/parentMobile/images/app_icon/circle_dubbing.png");
        CIRCLE_ICON_URL_MAP.put(SelfStudyType.ZUOYECUOITI, "/public/skin/parentMobile/images/app_icon/circle_zuoyecuoti.png");
        CIRCLE_ICON_URL_MAP.put(SelfStudyType.READING_ENGLISH, "/public/skin/parentMobile/images/app_icon/circle_huiben.png");
    }

    private StudyEntry processEntry(SelfStudyType selfStudyType,
                                    IndexDataContext indexDataContext, boolean isMySelected, boolean useRecIcon, boolean selfStudy) {

        StudyEntry studyEntry = StudyEntry.newInstance(selfStudyType, indexDataContext.getVersion());

        boolean useCircleIcon = indexDataContext.isAbove223() && isMySelected && indexDataContext.getPosition() == IndexDataContext.Position.index;
        if (useCircleIcon && CIRCLE_ICON_URL_MAP.get(selfStudyType) != null) {
            studyEntry.setIconUrl(CIRCLE_ICON_URL_MAP.get(selfStudyType));
        }
        StudyEntry.Tag tag = fetchTag(selfStudyType, indexDataContext.getPosition(), isMySelected);
        //作业错题本 可能没有
        if (selfStudyType == SelfStudyType.ZUOYECUOITI) {
            studyEntry.touchFunctionTypeH5()
                    .touchFunctionKey(indexDataContext.getCurrentSchema() + selfStudyType.getH5Url())
                    .toucheTag(tag)
                    .touchLabel("错题巩固举一反三");
        } else if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) {
            //点读机 点读机如有有记录要先进点读报告页面 md
            if (!indexDataContext.getStudyAppDataFutureMap().containsKey(SelfStudyType.PICLISTEN_ENGLISH)) {
                studyEntry.toucheTag(tag)
                        .touchLabel(SSTDescMap.get(SelfStudyType.PICLISTEN_ENGLISH))
                        .touchFunctionTypeNative()
                        .touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN);
            } else {
                StudyAppData picListenDataFuture = indexDataContext.getStudyAppDataFutureMap().get(SelfStudyType.PICLISTEN_ENGLISH).getUninterruptibly();
                String picListenProgress = getProgressFromData(picListenDataFuture);
                if (StringUtils.isBlank(picListenProgress) && SSTDescMap.containsKey(SelfStudyType.PICLISTEN_ENGLISH)) {
                    picListenProgress = SSTDescMap.get(SelfStudyType.PICLISTEN_ENGLISH);
                }
                if (StringUtils.isNotBlank(picListenProgress)) {
                    studyEntry.toucheTag(tag)
                            .touchLabel(picListenProgress)
                            .touchFunctionTypeH5()
                            .touchFunctionKey(indexDataContext.getCurrentSchema() + "/view/mobile/parent/read_report/index.vpage?order_referrer=330019");
                } else {
                    studyEntry.toucheTag(tag)
                            .touchLabel(picListenProgress)
                            .touchFunctionTypeNative()
                            .touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN);
                }
            }
        } else if (selfStudyType == SelfStudyType.READING_ENGLISH) {
            //绘本
            if (!indexDataContext.getStudyAppDataFutureMap().containsKey(SelfStudyType.READING_ENGLISH)) {
                return null;
            }

            StudyAppData readingDataFuture = indexDataContext.getStudyAppDataFutureMap().get(SelfStudyType.READING_ENGLISH).getUninterruptibly();
            String readingProgress = getProgressFromData(readingDataFuture);
            if (StringUtils.isBlank(readingProgress) && SSTDescMap.containsKey(SelfStudyType.READING_ENGLISH)) {
                readingProgress = SSTDescMap.get(SelfStudyType.READING_ENGLISH);
            }
            studyEntry.toucheTag(tag)
                    .touchLabel(readingProgress)
                    .touchFunctionTypeH5()
                    .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.READING_ENGLISH.getH5Url());
        } else if (selfStudyType == SelfStudyType.ALBUM) {
            //然后是订阅专辑
            if (!indexDataContext.getStudyAppDataFutureMap().containsKey(SelfStudyType.ALBUM)) {
                return null;
            }

            StudyAppData albumDataFuture = indexDataContext.getStudyAppDataFutureMap().get(SelfStudyType.ALBUM).getUninterruptibly();
            String albumProgress = getProgressFromData(albumDataFuture);
            if (StringUtils.isBlank(albumProgress) && SSTDescMap.containsKey(SelfStudyType.ALBUM)) {
                albumProgress = SSTDescMap.get(SelfStudyType.ALBUM);
            }
            studyEntry.toucheTag(tag)
                    .touchLabel(albumProgress)
                    .touchFunctionTypeH5()
                    .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.ALBUM.getH5Url());
        } else if (selfStudyType == SelfStudyType.LIVECAST) {
            if (!indexDataContext.getStudyAppDataFutureMap().containsKey(SelfStudyType.LIVECAST)) {
                return null;
            }

            StudyAppData liveCastDataFuture = indexDataContext.getStudyAppDataFutureMap().get(SelfStudyType.LIVECAST).getUninterruptibly();
            String progress = getProgressFromData(liveCastDataFuture);
            studyEntry.touchLabel(progress)
                    .touchFunctionTypeNative()
                    .touchFunctionKey(StudyEntry.FunctionKey.OFFICIAL_ACCOUNT)
                    .touchExtra(MapUtils.m(RES_OFFICIAL_ACCOUNT_ID, "13", RES_OFFICIAL_ACCOUNT_NAME, "直播课"));
        } else if (selfStudyType == SelfStudyType.DUBBING) {
            if (!indexDataContext.getStudyAppDataFutureMap().containsKey(SelfStudyType.DUBBING)) {
                return null;
            }

            StudyAppData dubbingDataFuture = indexDataContext.getStudyAppDataFutureMap().get(SelfStudyType.DUBBING).getUninterruptibly();
            String dubingProress = getProgressFromData(dubbingDataFuture);
            if (StringUtils.isBlank(dubingProress) && SSTDescMap.containsKey(SelfStudyType.DUBBING)) {
                dubingProress = SSTDescMap.get(SelfStudyType.DUBBING);
            }
            studyEntry.toucheTag(tag)
                    .touchLabel(dubingProress)
                    .touchFunctionTypeNative()
                    .touchFunctionKey(StudyEntry.FunctionKey.DUBBING);
        } else if (selfStudyType == SelfStudyType.XUEBA_VIDEO) {
            String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "XUEBAVIDEO_SWITCH");
            if (configValue == null || !"on".equalsIgnoreCase(configValue)) {
                return null;
            }
            studyEntry.toucheTag(tag)
                    .touchLabel("聚焦知识难点，巩固学习成果（甘肃电信专享）")
                    .touchFunctionTypeH5();
            if (RuntimeMode.isUsingTestData()) {
                studyEntry.touchFunctionKey("http://17xue-student.test.17zuoye.net/views/activity/telecompartner/index.vpage?t=1&m=487&r=0&d=1");
            } else if (RuntimeMode.isStaging()) {

            } else if (RuntimeMode.isProduction()) {
                studyEntry.touchFunctionKey("https://www.17zuoye.com/redirector/goaoshu.vpage?returnURL=https%3A%2F%2Fxue.17xueba.com%2Fm%2Fauth%2Fsfc.vpage%3Ft%3D1%26m%3D4735%26r%3D0%26d%3D3");
            } else {
                studyEntry.touchFunctionKey("https://17xue-student.test.17zuoye.net/views/activity/telecompartner/index.vpage?t=1&m=487&r=0&d=1");
            }
        } else {
            // 接下来是所有增值产品的入口
            FairylandProduct fairylandProduct = indexDataContext.getSst2FairyLandProductMap().get(selfStudyType);
            if (fairylandProduct == null) {
                return null;
            }

            StudyAppData appDataAlpsFuture = null;
            if (!indexDataContext.getStudyAppDataFutureMap().containsKey(selfStudyType)) {
                studyEntry.touchLabel(fairylandProduct.getProductDesc());
            } else {
                appDataAlpsFuture = indexDataContext.getStudyAppDataFutureMap().get(selfStudyType).getUninterruptibly();
                String progress = getProgressFromData(appDataAlpsFuture);
                if (StringUtils.isBlank(progress)) {
                    studyEntry.setLabel(fairylandProduct.getProductDesc());
                } else {
                    studyEntry.setLabel(progress);
                }
            }
            DayRange endDayRange = indexDataContext.getSelfStudyTypeDayRangeMapKeepExpired().get(selfStudyType);
            //如果是阿芬提语数英,进报告页面
            if (selfStudyType == SelfStudyType.FEE_COURSE_UNKUNOW) { //错题精讲进 H5
                String redirectUrl = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
                if (StringUtils.isNotBlank(redirectUrl)) {
                    studyEntry.touchFunctionTypeH5();
                    studyEntry.touchFunctionKey(indexDataContext.getCurrentSchema() + redirectUrl);
                }
            } else {
                //配音100分icon设置
                if (studyEntry.getSelfStudyType() == SelfStudyType.LISTEN_WORLD) {
                    studyEntry.setIconUrl(iconImageHeader + SelfStudyType.LISTEN_WORLD.getIconUrl());
                }

                //已付费，进入游戏内
                if (endDayRange != null && endDayRange.getEndDate().after(DayRange.current().getEndDate())) {
                    //如果获取不到,或者家长通不支持，则跳转到详情页。否则跳转到游戏内
                    String redirectUrl = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
                    if (StringUtils.isNotBlank(redirectUrl)) {
                        VendorApps vendorApps = vendorLoaderClient.loadVendor(fairylandProduct.getAppKey());
                        studyEntry.touchFunctionTypeNative()
                                .touchFunctionKey(StudyEntry.FunctionKey.FAIRYLAND_APP)
                                .touchExtra(generateFairylandExtra(fairylandProduct, vendorApps));
                    } else {
                        studyEntry.touchFunctionTypeH5()
                                .touchFunctionKey(url2ProductInfo(indexDataContext.getStudentDetail().getId(), fairylandProduct, indexDataContext.getCurrentSchema(), indexDataContext.getChannel()));
                    }
                    //阿分题小 U都进这个报告页 购买的进入报告
                    if (!selfStudy) {
                        if (selfStudyType.isAfenti() && indexDataContext.getStudentDetail() != null && indexDataContext.getStudentDetail().getClazz() != null) {
                            String xiaoUReportUrl = "/zion/nova-report";
                            studyEntry.touchFunctionTypeH5()
                                    .touchFunctionKey(indexDataContext.getCurrentSchema() + xiaoUReportUrl + "?subject=" + selfStudyType.getSubject() + "&sid=" + indexDataContext.getStudentDetail().getId());
                        }
                    }
                } else {
                    studyEntry.touchFunctionTypeH5()
                            .touchFunctionKey(url2ProductInfo(indexDataContext.getStudentDetail().getId(), fairylandProduct, indexDataContext.getCurrentSchema(), indexDataContext.getChannel()));
                }
            }
            if (endDayRange != null) {
                if (endDayRange.getEndDate().after(DayRange.current().getEndDate())) {
                    studyEntry.toucheTag(fetchTag(selfStudyType, indexDataContext.getPosition(), StudyEntry.Tag.payed, isMySelected));
                } else if (DateUtils.calculateDateDay(endDayRange.getEndDate(), 10).after(DayRange.current().getEndDate())) {
                    studyEntry.toucheTag(fetchTag(selfStudyType, indexDataContext.getPosition(), StudyEntry.Tag.renew, isMySelected));
                }
            }
            //如果有 notify，则把 notify 优先显示
            if (appDataAlpsFuture != null && StringUtils.isNotBlank(appDataAlpsFuture.getNotifyContent()) && StringUtils.isNotBlank(appDataAlpsFuture.getNotifyUniqueId())) {
                studyEntry.touchLabelReminder(appDataAlpsFuture.getNotifyContent(), appDataAlpsFuture.getNotifyUniqueId());
            }

            if (useCircleIcon) {
                studyEntry.setIconUrl("gridfs/" + fairylandProduct.getBackgroundImage());
            } else if (useRecIcon) {
                studyEntry.setIconUrl("gridfs/" + fairylandProduct.getProductRectIcon());
            } else {
                studyEntry.setIconUrl("gridfs/" + fairylandProduct.getProductIcon());
            }
        }
        return studyEntry;
    }

    private String url2ProductInfo(Long sid, FairylandProduct product, String mainSiteSchema, String channel) {
        if (product == null) {
            return mainSiteSchema + "/view/mobile/parent/learning_app/detail.vpage?showAppList=true" + "&sid=" + sid + "&rel=xxx";
        }
        return mainSiteSchema + "/karp/commodity_details/index/list?" + "sid=" + sid + "&productType=" + product.getAppKey() + "&order_refer=" + channel;
    }


    /**
     * 读取 自学类型对应的数据
     *
     * @param selfStudyTypes 需要获取数据的 sst
     * @param parent         家长
     * @param studentDetail  学生
     * @return 自学产品
     */
    private Map<SelfStudyType, AlpsFuture<StudyAppData>> getUserStudyDataFutureMap(Collection<SelfStudyType> selfStudyTypes, User parent, StudentDetail studentDetail) {
        Map<SelfStudyType, AlpsFuture<StudyAppData>> map = new HashMap<>();
        for (SelfStudyType selfStudyType : selfStudyTypes) {
            AlpsFuture<StudyAppData> appDataFuture = mySelfStudyService.loadStudyAppData(studentDetail.getId(), selfStudyType);
            map.put(selfStudyType, appDataFuture);
        }
        AlpsFuture<StudyAppData> picListenFuture = mySelfStudyService.loadStudyAppData(studentDetail.getId(), SelfStudyType.PICLISTEN_ENGLISH);
        map.put(SelfStudyType.PICLISTEN_ENGLISH, picListenFuture);
        if (selfStudyTypes.contains(SelfStudyType.ALBUM)) {
            AlpsFuture<StudyAppData> albumFuture = mySelfStudyService.loadStudyAppData(parent.getId(), SelfStudyType.ALBUM);
            map.put(SelfStudyType.ALBUM, albumFuture);
        }
        if (selfStudyTypes.contains(SelfStudyType.LIVECAST)) {
            AlpsFuture<StudyAppData> liveCastFuture = mySelfStudyService.loadStudyAppData(parent.getId(), SelfStudyType.LIVECAST);
            map.put(SelfStudyType.LIVECAST, liveCastFuture);
        }
        map.put(SelfStudyType.ZUOYECUOITI, mySelfStudyService.loadStudyAppData(studentDetail.getId(), SelfStudyType.ZUOYECUOITI));
        AlpsFuture<StudyAppData> dubbingFuture = mySelfStudyService.loadStudyAppData(studentDetail.getId(), SelfStudyType.DUBBING);
        map.put(SelfStudyType.DUBBING, dubbingFuture);
        return map;
    }

    private List<StudyEntry> shenheMoreStudyToolsEntryList(IndexDataContext indexDataContext) {
        List<StudyEntry> list = new ArrayList<>(1);
        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.ALBUM.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.ALBUM, indexDataContext.getPosition(), true)));
        return list;
    }

    private List<StudyEntry> shenheMoreStudyToolsEntryList_V3(IndexDataContext indexDataContext) {
        List<StudyEntry> list = new ArrayList<>(1);
        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.ALBUM.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.ALBUM, indexDataContext.getPosition(), true))
                .touchLabelTextColor(StudyEntry.LabelColor.WHITE)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/peiyin_img.png"));
        return list;
    }


    private List<StudyEntry> shenheSelectedToolsEntryList(IndexDataContext indexDataContext) {
        List<StudyEntry> list = new ArrayList<>(3);
        list.add(processEntry(SelfStudyType.ALBUM, indexDataContext, true, false, false));
        list.add(processEntry(SelfStudyType.DUBBING, indexDataContext, true, false, false));
        list.add(processEntry(SelfStudyType.READING_ENGLISH, indexDataContext, true, false, false));
        return list;
    }


    private List<StudyEntry> normalMoreStudyToolsEntryList(IndexDataContext indexDataContext) {
        IndexDataContext.Position position = indexDataContext.getPosition();
        List<StudyEntry> list = new ArrayList<>(4);
        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN)
                .toucheTag(fetchTag(SelfStudyType.PICLISTEN_ENGLISH, position, true)));

        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.READING_ENGLISH.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.READING_ENGLISH, position, true)));

        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.ALBUM.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.ALBUM, position, true)));

        list.add(processEntry(SelfStudyType.DUBBING, indexDataContext, false, false, false));

        if (indexDataContext.getStudentDetail() != null
                && VersionUtil.compareVersion(indexDataContext.getVersion(), "2.2.6.0") > 0
                && !indexDataContext.isInBlackList()) {
            list.add(StudyEntry.newInstance(SelfStudyType.CHIPS_ENGLISH, indexDataContext.getVersion())
                    .touchFunctionTypeNative()
                    .touchFunctionKey(StudyEntry.FunctionKey.FRIESENGLISH)
                    .toucheTag(fetchTag(SelfStudyType.CHIPS_ENGLISH, position, true)));
        }

        return list;
    }

    private List<StudyEntry> normalMoreStudyToolsEntryList_V3(IndexDataContext indexDataContext) {
        IndexDataContext.Position position = indexDataContext.getPosition();
        List<StudyEntry> list = new ArrayList<>(4);
        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN)
                .toucheTag(fetchTag(SelfStudyType.PICLISTEN_ENGLISH, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/diandu_img.png"));

        StudyEntry studyEntry = processEntry(SelfStudyType.DUBBING, indexDataContext, false, false, false);
        studyEntry.touchLabelTextColor(StudyEntry.LabelColor.WHITE)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/peiyin_img.png");
        list.add(studyEntry);

        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.READING_ENGLISH.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.READING_ENGLISH, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/huiben_img.png"));

        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.ALBUM.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.ALBUM, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.WHITE)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/yinshipin_img.png"));

        return list;
    }

    private List<StudyEntry> noLoginMoreStudyToolsEntryList(IndexDataContext indexDataContext) {
        IndexDataContext.Position position = indexDataContext.getPosition();
        List<StudyEntry> list = new ArrayList<>(4);
        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.PICLISTEN_ENGLISH, position, true)));

        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.READING_ENGLISH, position, true)));

        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.ALBUM, position, true)));

        list.add(StudyEntry.newInstance(SelfStudyType.DUBBING, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.DUBBING, position, true)));

        return list;
    }

    private List<StudyEntry> noLoginMoreStudyToolsEntryList_V3(IndexDataContext indexDataContext) {
        IndexDataContext.Position position = indexDataContext.getPosition();
        List<StudyEntry> list = new ArrayList<>(4);
        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.PICLISTEN_ENGLISH, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/diandu_img.png"));

        list.add(StudyEntry.newInstance(SelfStudyType.DUBBING, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.DUBBING, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.WHITE)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/peiyin_img.png"));

        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.READING_ENGLISH, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/huiben_img.png"));

        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.ALBUM, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.WHITE)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/yinshipin_img.png"));


        return list;
    }

    private List<StudyEntry> noLoginMySelectedToolsEntryList(IndexDataContext indexDataContext) {
        IndexDataContext.Position position = indexDataContext.getPosition();
        List<StudyEntry> list = new ArrayList<>(4);
        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.PICLISTEN_ENGLISH, position, true)));
        list.add(StudyEntry.newInstance(SelfStudyType.DUBBING, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.DUBBING, position, true)));
        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.ALBUM, position, true)));
        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.LOGIN)
                .toucheTag(fetchTag(SelfStudyType.READING_ENGLISH, position, true)));
        return list.stream().sorted(Comparator.comparing(o -> SSTOrderMap.getOrDefault(o.getSelfStudyType(), 100000)))
                .collect(Collectors.toList());
    }

    private List<StudyEntry> noChildMoreStudyToolsEntryList(IndexDataContext indexDataContext) {
        IndexDataContext.Position position = indexDataContext.getPosition();
        List<StudyEntry> list = new ArrayList<>(4);
        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN)
                .toucheTag(fetchTag(SelfStudyType.PICLISTEN_ENGLISH, position, true)));

        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.READING_ENGLISH.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.READING_ENGLISH, position, true)));

        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.ALBUM.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.ALBUM, position, true)));

        list.add(StudyEntry.newInstance(SelfStudyType.DUBBING, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD)
                .toucheTag(fetchTag(SelfStudyType.DUBBING, position, true)));
        if (VersionUtil.compareVersion(indexDataContext.getVersion(), "2.2.6.0") > 0
                && (indexDataContext.getParent() != null && !userBlacklistServiceClient.isInUserBlackList(indexDataContext.getParent()))) {
            list.add(StudyEntry.newInstance(SelfStudyType.CHIPS_ENGLISH, indexDataContext.getVersion())
                    .touchFunctionTypeNative()
                    .touchFunctionKey(StudyEntry.FunctionKey.FRIESENGLISH)
                    .toucheTag(fetchTag(SelfStudyType.CHIPS_ENGLISH, position, true)));
        }


        return list;
    }

    private List<StudyEntry> noChildMoreStudyToolsEntryList_V3(IndexDataContext indexDataContext) {
        IndexDataContext.Position position = indexDataContext.getPosition();
        List<StudyEntry> list = new ArrayList<>(4);
        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN)
                .toucheTag(fetchTag(SelfStudyType.PICLISTEN_ENGLISH, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/diandu_img.png"));

        list.add(StudyEntry.newInstance(SelfStudyType.DUBBING, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD)
                .toucheTag(fetchTag(SelfStudyType.DUBBING, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.WHITE)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/peiyin_img.png"));

        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.READING_ENGLISH.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.READING_ENGLISH, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/huiben_img.png"));

        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.ALBUM.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.ALBUM, position, true))
                .touchLabelTextColor(StudyEntry.LabelColor.WHITE)
                .touchBackImgUrl("http://17zy-content-video.oss-cn-beijing.aliyuncs.com/class/2018t/yinshipin_img.png"));

        return list;
    }

    private List<StudyEntry> noChildMySelectedToolsEntryList(IndexDataContext indexDataContext) {
        IndexDataContext.Position position = indexDataContext.getPosition();
        List<StudyEntry> list = new ArrayList<>(3);
        list.add(StudyEntry.newInstance(SelfStudyType.PICLISTEN_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.PIC_LISTEN)
                .toucheTag(fetchTag(SelfStudyType.PICLISTEN_ENGLISH, position, true)));
        list.add(StudyEntry.newInstance(SelfStudyType.DUBBING, indexDataContext.getVersion())
                .touchFunctionTypeNative()
                .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD)
                .toucheTag(fetchTag(SelfStudyType.DUBBING, position, true)));
        list.add(StudyEntry.newInstance(SelfStudyType.ALBUM, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.ALBUM.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.ALBUM, position, true)));
        list.add(StudyEntry.newInstance(SelfStudyType.READING_ENGLISH, indexDataContext.getVersion())
                .touchFunctionTypeH5()
                .touchFunctionKey(indexDataContext.getCurrentSchema() + SelfStudyType.READING_ENGLISH.getH5Url())
                .toucheTag(fetchTag(SelfStudyType.READING_ENGLISH, position, true)));
        return list.stream().sorted(Comparator.comparing(o -> SSTOrderMap.getOrDefault(o.getSelfStudyType(), 100000)))
                .collect(Collectors.toList());
    }


    private Map<String, Object> refinedLessonMap(IndexDataContext indexDataContext) {
        if (indexDataContext.isHasChild()) {
            AlpsFuture<List<LiveCastIndexRefinedLessons.LessonInfo>> lessonsFuture = mySelfStudyService.loadStudentLiveCastRefinedLessons(indexDataContext.getStudentDetail());
            Map<String, Object> map = new HashMap<>();
            map.put(RES_TITLE, "精品课推荐");
            map.put(RES_MORE_URL, indexDataContext.getCurrentSchema() + getLiveCastMoreUrl());
            List<LiveCastIndexRefinedLessons.LessonInfo> lessonInfoList = lessonsFuture.getUninterruptibly();
            if (CollectionUtils.isEmpty(lessonInfoList)) {
                return null;
            }
            map.put("entry_list", lessonInfoList.stream().map(lessonInfo -> convertStudyEntry(lessonInfo, indexDataContext.getCurrentSchema())).collect(Collectors.toList()));
            return map;
        } else {
            return null;
        }
    }

    private StudyEntry convertStudyEntry(LiveCastIndexRefinedLessons.LessonInfo lessonInfo, String mainSiteSchem) {
        StudyEntry studyEntry = new StudyEntry();
        studyEntry.setSelfStudyType(SelfStudyType.LIVECAST);
        studyEntry.setLabel(lessonInfo.getLabel());
        studyEntry.setLabelTextColor(StudyEntry.LabelColor.WHITE.getColor());
        studyEntry.setName(lessonInfo.getTitle());
        studyEntry.setBottomText(lessonInfo.getDesc());
        studyEntry.setIconUrl(lessonInfo.getCoverUrl());
        studyEntry.touchFunctionTypeH5();
        if (SafeConverter.toBoolean(lessonInfo.getNeedLogin())) {
            studyEntry.touchFunctionKey(mainSiteSchem + "/redirector/goaoshu.vpage?returnURL=" + lessonInfo.getUrl());
        } else {
            studyEntry.touchFunctionKey(lessonInfo.getUrl());
        }
        studyEntry.setDotId(lessonInfo.getDotId());
        studyEntry.setWatchCount(lessonInfo.getWatchCount());
        return studyEntry;
    }


    private static List<SelfStudyType> subjectSyncTrainingSSTList = new ArrayList<>();
    private static List<SelfStudyType> capabilityUpgradingSSTList = new ArrayList<>();
    private static List<SelfStudyType> afterSchoolExtendSSTList = new ArrayList<>();
    private static List<SelfStudyType> allShelfSSTList = new ArrayList<>();
    private static Map<SelfStudyType, StudyEntry.Category> categoryMap = new HashMap<>();

    {
        afterSchoolExtendSSTList.add(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW);
        afterSchoolExtendSSTList.add(SelfStudyType.ANIMAL_LAND);
        afterSchoolExtendSSTList.add(SelfStudyType.DINOSAUR_LAND);
        afterSchoolExtendSSTList.add(SelfStudyType.SCIENCE_LAND);
        allShelfSSTList.addAll(afterSchoolExtendSSTList);

        capabilityUpgradingSSTList.add(SelfStudyType.ZOUMEI_ENGLISH);
        capabilityUpgradingSSTList.add(SelfStudyType.ARITHMETIC);
        capabilityUpgradingSSTList.add(SelfStudyType.CHINESE_HERO);
        capabilityUpgradingSSTList.add(SelfStudyType.SYN_PRACTICE_CHINESE);
        allShelfSSTList.addAll(capabilityUpgradingSSTList);

        subjectSyncTrainingSSTList.add(SelfStudyType.AFENTI_ENGLISH);
        subjectSyncTrainingSSTList.add(SelfStudyType.AFENTI_MATH);
        subjectSyncTrainingSSTList.add(SelfStudyType.AFENTI_CHINESE);
        allShelfSSTList.addAll(subjectSyncTrainingSSTList);

        categoryMap.put(SelfStudyType.WALKMAN_ENGLISH, StudyEntry.Category.英语);
        categoryMap.put(SelfStudyType.ARITHMETIC, StudyEntry.Category.数学);
        categoryMap.put(SelfStudyType.CHINESE_HERO, StudyEntry.Category.语文);
        categoryMap.put(SelfStudyType.SYN_PRACTICE_CHINESE, StudyEntry.Category.语文);
        categoryMap.put(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW, StudyEntry.Category.百科);
        categoryMap.put(SelfStudyType.ANIMAL_LAND, StudyEntry.Category.动物);
        categoryMap.put(SelfStudyType.DINOSAUR_LAND, StudyEntry.Category.动物);
        categoryMap.put(SelfStudyType.SCIENCE_LAND, StudyEntry.Category.科学);
        categoryMap.put(SelfStudyType.ZOUMEI_ENGLISH, StudyEntry.Category.英语);
    }

    private Map<String, Object> afterSchoolExtendMap(IndexDataContext indexDataContext) {
        return processNormalSStEntryMap(afterSchoolExtendSSTList, "课外学习拓展", indexDataContext, true, false, true, indexDataContext.isAbove223());
    }

    private Map<String, Object> capabilityUpgradingMap(IndexDataContext indexDataContext) {
        return processNormalSStEntryMap(capabilityUpgradingSSTList, "专项能力提升", indexDataContext, true, false, true, indexDataContext.isAbove223());
    }

    private Map<String, Object> subjectSyncTrainingMap(IndexDataContext indexDataContext) {
        return processNormalSStEntryMap(subjectSyncTrainingSSTList, "学科同步训练", indexDataContext, false, true, false, false);
    }

    private Map<String, Object> processNormalSStEntryMap(List<SelfStudyType> sstList, String title, IndexDataContext indexDataContext, boolean forceDefaultDesc, boolean useAlias, boolean withCategory, boolean useRecIcon) {
        Map<String, Object> map = new HashMap<>();
        map.put(RES_TITLE, title);
        Map<String, String> appKey2DescMap = indexDataContext.getAppKey2DescMap() == null ? Collections.emptyMap() : indexDataContext.getAppKey2DescMap();
        List<StudyEntry> studyEntries = sstList.stream()
                .sorted(Comparator.comparing(o -> SSTOrderMap.getOrDefault(o, 100000)))
                .map(t -> {
                    StudyEntry studyEntry = processEntry(t, indexDataContext, false, useRecIcon, false);
                    if (studyEntry == null) {
                        return null;
                    }
                    String desc = appKey2DescMap.get(t.getOrderProductServiceType());
                    if (StringUtils.isNotBlank(desc)) {
                        studyEntry.setBottomText(desc);
                    }
                    if (t == SelfStudyType.AFENTI_ENGLISH) {
                        studyEntry.setRecommendText("北师大附小老师推荐");
                    }
                    if (withCategory) {
                        StudyEntry.Category category = categoryMap.get(t);
                        if (category != null) {
                            studyEntry.setCategory(category);
                        }
                    }
                    return studyEntry;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        dealIconCdnAndGlobalMSg(studyEntries, indexDataContext);
        studyEntries.forEach(t -> dealDefaultLabel2Desc(t, indexDataContext, forceDefaultDesc));
        if (useAlias) {
            dealAlias(studyEntries);
        }
        map.put("entry_list", studyEntries);
        return map;
    }


    private void dealAlias(List<StudyEntry> studyEntries) {
        studyEntries.forEach(t -> {
            switch (t.getSelfStudyType()) {
                case AFENTI_ENGLISH:
                    t.setName("小U英语智能同步练习");
                    break;
                case AFENTI_MATH:
                    t.setName("小U数学智能同步练习");
                    break;
                case AFENTI_CHINESE:
                    t.setName("小U语文智能同步练习");
                    break;
            }
        });
    }

    private String getLiveCastMoreUrl() {
        if (RuntimeMode.le(Mode.TEST)) {
            return "redirector/go17Xue.vpage?returnURL=http://17xue-student.test.17zuoye.net/m/auth/jzt/entry.vpage";
        } else if (RuntimeMode.isStaging()) {
            return "redirector/go17Xue.vpage?returnURL=https://17xue-student.staging.17zuoye.net/m/auth/jzt/entry.vpage";
        } else if (RuntimeMode.isProduction()) {
            return "redirector/goaoshu.vpage?returnURL=https://micro.17zuoye.com/micro";
        } else {
            return "redirector/go17Xue.vpage?returnURL=http://17xue-student.test.17zuoye.net/m/auth/jzt/entry.vpage";
        }
    }

    private String getLiveCastUrl() {
        if (RuntimeMode.le(Mode.TEST)) {
            return "/redirector/goaoshu.vpage?returnURL=https://17xue-student.test.17zuoye.net/m/course/center/index.vpage";
        } else {
            String url = "https://xue.17xueba.com/m/course/center/index.vpage?channelId=3783";
            try {
                String encodeUrl = URLEncoder.encode(url, "UTF-8");
                return "/redirector/goaoshu.vpage?returnURL=" + encodeUrl;
            } catch (UnsupportedEncodingException e) {
                return "/redirector/goaoshu.vpage?returnURL=" + url;
            }
        }
    }
}
