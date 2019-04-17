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
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.jzt.entity.StudyProgressDetail;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.AsyncNewsCacheService;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsAlbumContentType;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsAlbumType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentSelfStudyTypeH5Mapper;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsAlbum;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsTag;
import com.voxlearning.utopia.service.vendor.api.entity.ParentNewsAlbumSubRecord;
import com.voxlearning.utopia.service.zone.api.ZoneLoader;
import com.voxlearning.washington.athena.ParentReportLoaderClient;
import com.voxlearning.washington.mapper.AlbumAbilityTagConfig;
import com.voxlearning.washington.mapper.AlbumAbilityTagPlanBConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2017-6-5
 */
@Controller
@Slf4j
@RequestMapping(value = "parentMobile/studyProgress/")
public class MobileParentStudyProgressController extends AbstractMobileParentController {

    private static final Map<String, String> adviceProgress;
    private static final Map<String, SelfStudyType> adviceSelfStudyType;
    private static final Map<SelfStudyType, String> selfStudyTypeSubTitle;

    static {
        adviceProgress = new HashMap<>();
        adviceProgress.put("词汇", "巩固单词，提升词汇量");
        adviceProgress.put("听", "提高英语语感，体会英文语言运用");
        adviceProgress.put("说", "跟读单词、对话，提高英语口语表达");
        adviceProgress.put("读", "听读结合，提高阅读能力");
        adviceProgress.put("写", "掌握起步写作规律，仿写简单句子");
        adviceSelfStudyType = new HashMap<>();
        adviceSelfStudyType.put("词汇", SelfStudyType.ZOUMEI_ENGLISH);
        adviceSelfStudyType.put("听", SelfStudyType.AFENTI_ENGLISH);
        adviceSelfStudyType.put("说", SelfStudyType.PICLISTEN_ENGLISH);
        adviceSelfStudyType.put("读", SelfStudyType.PICLISTEN_ENGLISH);
        adviceSelfStudyType.put("写", SelfStudyType.AFENTI_ENGLISH);
        adviceSelfStudyType.put("数学运算能力", SelfStudyType.ARITHMETIC);
        selfStudyTypeSubTitle = new HashMap<>();
        selfStudyTypeSubTitle.put(SelfStudyType.ZOUMEI_ENGLISH, "掌握小学阶段2500重点词汇");
        selfStudyTypeSubTitle.put(SelfStudyType.AFENTI_ENGLISH, "智能规划练习题，紧跟课本与薄弱项");
        selfStudyTypeSubTitle.put(SelfStudyType.PICLISTEN_ENGLISH, "会发音的小学课本，同步教材听说读");
        selfStudyTypeSubTitle.put(SelfStudyType.ARITHMETIC, "提升速算能力，培养数学兴趣");
        selfStudyTypeSubTitle.put(SelfStudyType.AFENTI_MATH, "已为您智能规划薄弱专项练习题");
        selfStudyTypeSubTitle.put(SelfStudyType.AFENTI_CHINESE, "已为您智能规划学习路径");

    }

    @Inject
    private ParentReportLoaderClient parentReportLoaderClient;
    @ImportService(interfaceClass = ZoneLoader.class)
    private ZoneLoader zoneLoader;
    @ImportService(interfaceClass = AsyncNewsCacheService.class)
    private AsyncNewsCacheService asyncNewsCacheService;

    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getStudentStudyProgressDetail() {
        User parent = currentParent();
        Long studentId = getRequestLong("sid");
        String subjectName = getRequestString("subject");
        if (studentId == 0) {
            return MapMessage.errorMessage("学生ID不正确");
        }
        Subject subject = Subject.ofWithUnknown(subjectName);
        if (subject == Subject.UNKNOWN) {
            return MapMessage.errorMessage("学科不正确");
        }
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false)
                .stream()
                .filter(p -> p.getSubject() == subject)
                .findFirst()
                .orElse(null);
        if (groupMapper == null) {
            return MapMessage.errorMessage("学科不正确");
        }
        Map<Long, Integer> groupSubject = new HashMap<>();
        groupSubject.put(groupMapper.getId(), groupMapper.getSubject().getId());
        Map<Integer, StudyProgressDetail> progressDetailMap = parentReportLoaderClient.getParentReportLoader().getStudyProgressDetail(studentId, groupSubject);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        Map<String, Object> studyProgressMap = new HashMap<>();
        Map<String, Object> albumMap = new HashMap<>();
        String bigDataJson = "";
        ParentSelfStudyTypeH5Mapper h5Mapper = null;
        if (MapUtils.isNotEmpty(progressDetailMap) && progressDetailMap.containsKey(groupMapper.getSubject().getId())) {
            StudyProgressDetail progressDetail = progressDetailMap.get(groupMapper.getSubject().getId());
            if (progressDetail != null) {
                bigDataJson = JsonUtils.toJson(progressDetail);
                //错题数
                studyProgressMap.put("wrong_count", progressDetail.getWrongQestNum());
                //薄弱点
                studyProgressMap.put("weak_point", progressDetail.getWeakKp());
                //提升建议
                studyProgressMap.put("advice_progress", adviceProgressContent(progressDetail));
                if (progressDetail.getHomeworkRate() != null) {
                    //完成率
                    studyProgressMap.put("finish_rate", SafeConverter.toString(new BigDecimal(progressDetail.getHomeworkRate() * 100).setScale(0, BigDecimal.ROUND_HALF_UP).intValue()) + "%");
                }
                //平均分
                if (progressDetail.getScore() != null) {
                    Boolean needScoreLevel = Boolean.FALSE;
                    if (studentDetail != null && grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList")) {
                        needScoreLevel = Boolean.TRUE;
                    }
                    String score;
                    if (needScoreLevel) {
                        score = ScoreLevel.processLevel(progressDetail.getScore().intValue()).getLevel();
                    } else {
                        score = SafeConverter.toString(new BigDecimal(progressDetail.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
                    }
                    studyProgressMap.put("score", score);
                }
                //累计题数
                studyProgressMap.put("total_count", progressDetail.getAllQuestionNum());

                //推荐的自学产品
                SelfStudyType selfStudyType = selfStudyType(progressDetail);
                if (selfStudyType != null) {
                    Map<SelfStudyType, ParentSelfStudyTypeH5Mapper> h5MapperMap = loadEntryMapper(Collections.singleton(selfStudyType), parent, studentDetail, false, "330026", false);
                    if (h5MapperMap.containsKey(selfStudyType) && h5MapperMap.get(selfStudyType) != null) {
                        h5Mapper = h5MapperMap.get(selfStudyType);
                        h5Mapper.setSubTitle(selfStudyTypeSubTitle.get(selfStudyType));
                    }
                }

                //推荐的专辑
                //这个本身就是按照订阅数降序排列的
                List<String> rankAlbumIds = asyncNewsCacheService.JxtNewsCacheManager_loadAlbumSubRank().getUninterruptibly();
                JxtNewsAlbum jxtNewsAlbum = null;
                if (parent != null && studentDetail != null && studentDetail.getClazz() != null && !studentDetail.getClazz().isTerminalClazz() && StringUtils.isNotBlank(progressDetail.getWeakKp())) {
                    Map<Integer, String> subjectWeak = new HashMap<>();
                    subjectWeak.put(groupMapper.getSubject().getId(), progressDetail.getWeakKp());
                    //根据薄弱项去取tagId
                    List<AlbumAbilityTagConfig> tagConfigList = generateParentAlbumAbilityTagConfig(subjectWeak);
                    Set<Long> tagIds = new HashSet<>();
                    tagConfigList.stream().filter(p -> p.getDataTags().get(progressDetail.getWeakKp()) != null).forEach(p -> tagIds.addAll(p.getDataTags().get(progressDetail.getWeakKp())));
                    if (CollectionUtils.isNotEmpty(tagIds)) {

                        jxtNewsAlbum = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum().stream()
                                .filter(p -> tagIds.stream().anyMatch(e -> p.getTagList().contains(e)))
                                .sorted((o1, o2) -> rankAlbumIds.indexOf(o1.getId()) - rankAlbumIds.indexOf(o2.getId()))
                                .findFirst()
                                .orElse(null);
                    }
                } else {
                    //兜底方案。用3-4年级的
                    AlbumAbilityTagPlanBConfig tagPlanBConfig = getFitJxtNewsAlbumWithConfig(Arrays.asList(3, 4))
                            .stream()
                            .filter(p -> Objects.equals(p.getSubjectId(), groupMapper.getSubject().getId()))
                            .filter(p -> MapUtils.isNotEmpty(p.getTagMap()) && CollectionUtils.isNotEmpty(p.getAlbumIds()))
                            .findFirst()
                            .orElse(null);
                    if (tagPlanBConfig != null && CollectionUtils.isNotEmpty(tagPlanBConfig.getAlbumIds())) {
                        jxtNewsAlbum = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(tagPlanBConfig.getAlbumIds())
                                .values()
                                .stream()
                                .filter(p -> p.getOnline() != null && p.getOnline())
                                .sorted((o1, o2) -> rankAlbumIds.indexOf(o1.getId()) - rankAlbumIds.indexOf(o2.getId()))
                                .findFirst()
                                .orElse(null);
                    }
                }
                if (jxtNewsAlbum != null) {
                    //Map<String, JxtNewsContentType> albumType = generateAlbumType(Collections.singleton(jxtNewsAlbum));
                    albumMap.put("icon", jxtNewsAlbum.getHeadImg());
                    albumMap.put("title", jxtNewsAlbum.getTitle());
                    albumMap.put("id", jxtNewsAlbum.getId());
                    albumMap.put("type", jxtNewsAlbum.getJxtNewsAlbumContentType() != null ? jxtNewsAlbum.getJxtNewsAlbumContentType().name() : "");
                }
            }
        }
        return MapMessage.successMessage().add("study_progress", studyProgressMap).add("self_study", h5Mapper).add("album", albumMap).add("bigDataJson", bigDataJson);
    }

    @RequestMapping(value = "masterRank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getStudentStudyMasterRank() {
        Long sid = getRequestLong("sid");
        if (sid == 0L) {
            return MapMessage.errorMessage();
        }
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(sid);
        if (clazz == null) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage().add("master_rank_list", generateStudyMasterRankList(clazz, sid));
    }

    @RequestMapping(value = "studyProducts.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getStudentStudyProducts() {
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage();
        }
        Long sid = getRequestLong("sid");
        if (sid == 0L) {
            return MapMessage.errorMessage();
        }
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(sid);
        if (clazz == null) {
            return MapMessage.errorMessage();
        }
        List<Map<String, Object>> masterCountRank = zoneLoader.studyMasterCountRank(clazz, sid);
        if (masterCountRank.size() >= 10) {
            masterCountRank = masterCountRank.subList(0, 10);
        }
        List<Long> studentIds = masterCountRank.stream().map(e -> SafeConverter.toLong(e.get("studentId"))).collect(Collectors.toList());
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        List<Map<String, Object>> albumMapList = generateAlbumsByClazzParents(clazz);
        return MapMessage.successMessage().add("album_list", albumMapList).add("self_study_list", generateSelfStudyProducts(studentIds, studentDetail, user));
    }

    @RequestMapping(value = "saveStudyMasterStatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveStudyMasterOpenStatus() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage();
        }
        ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(parent.getId());
        MapMessage mapMessage;
        if (parentExtAttribute != null) {
            parentExtAttribute.setStudyMasterIsOpen(Boolean.FALSE);
            mapMessage = parentServiceClient.updateParentExtAttribute(parentExtAttribute);
        } else {
            parentExtAttribute = new ParentExtAttribute(parent.getId());
            parentExtAttribute.setStudyMasterIsOpen(Boolean.FALSE);
            mapMessage = parentServiceClient.generateParentExtAttribute(parentExtAttribute);
        }
        return mapMessage;
    }


    private List<Map<String, Object>> generateStudyMasterRankList(Clazz clazz, Long selfStudentId) {
        if (clazz == null || selfStudentId == null || selfStudentId == 0L) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> mapList = zoneLoader.studyMasterCountRank(clazz, selfStudentId);
        for (Map<String, Object> studyMap : mapList) {
            Map<String, Object> studyMasterMap = new HashMap<>();
            Long studentId = SafeConverter.toLong(studyMap.get("studentId"));
            studyMasterMap.put("student_id", studentId);
            String studentImg = SafeConverter.toString(studyMap.get("studentImg"));
            studyMasterMap.put("student_img", getUserAvatarImgUrl(studentImg));
            studyMasterMap.put("sm_count", studyMap.get("smCount"));
            studyMasterMap.put("student_name", studyMap.get("studentName"));
            if (selfStudentId.equals(studentId)) {
                studyMasterMap.put("is_me", Boolean.TRUE);
            } else {
                studyMasterMap.put("is_me", Boolean.FALSE);
            }
            mapList.set(mapList.indexOf(studyMap), studyMasterMap);
        }

        return mapList;
    }


    private List<Map<String, Object>> generateAlbumsByClazzParents(Clazz clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        List<User> parents = parentLoaderClient.loadClazzParents(clazz.getId());
        List<Long> parentIds = parents.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, List<ParentNewsAlbumSubRecord>> subRecordByUserId = new HashMap<>();
        if (parentIds.size() <= 5) {
            subRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(parentIds);
        } else {
            List<List<Long>> parentIdList = CollectionUtils.splitList(new ArrayList<>(parentIds), parentIds.size() / 5);
            for (List<Long> parentList : parentIdList) {
                Map<Long, List<ParentNewsAlbumSubRecord>> subRecords = jxtNewsLoaderClient.getAlbumSubRecordByUserId(parentList);
                if (MapUtils.isNotEmpty(subRecords)) {
                    subRecordByUserId.putAll(subRecords);
                }
            }
        }
//        Map<Long, List<ParentNewsAlbumSubRecord>> subRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(parentIds);
        List<ParentNewsAlbumSubRecord> subRecordList = new ArrayList<>();
        subRecordByUserId.values().forEach(subRecordList::addAll);
        Map<String, List<ParentNewsAlbumSubRecord>> subRecordByAlbumId = subRecordList.stream().filter(ParentNewsAlbumSubRecord::getIsSub).collect(Collectors.groupingBy(ParentNewsAlbumSubRecord::getSubAlbumId));
        List<String> albumIds = subRecordByAlbumId.keySet().stream().sorted((o1, o2) -> Integer.compare(subRecordByAlbumId.get(o2).size(), subRecordByAlbumId.get(o1).size())).collect(Collectors.toList());
        List<JxtNewsAlbum> albumList;
        if (CollectionUtils.isEmpty(albumIds)) {
            //班级内没有订阅专辑的情况
            albumIds = asyncNewsCacheService.JxtNewsCacheManager_loadAlbumSubRank().take();
            albumList = filterAlbumList(albumIds);
            albumList.subList(0, 5);
            Collections.shuffle(albumList);
            albumList = albumList.subList(0, 2);
        } else {
            albumList = filterAlbumList(albumIds);
            if (albumList.size() >= 5) {
                albumList = albumList.subList(0, 5);
                Collections.shuffle(albumList);
                albumList = albumList.subList(0, 2);
            } else if (albumList.size() < 5 && albumList.size() > 2) {
                albumList = albumList.subList(0, albumList.size());
                Collections.shuffle(albumList);
                albumList = albumList.subList(0, 2);
            }
        }
        List<JxtNewsTag> jxtNewsTags = generateAllAbilityTags();
        albumList.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("album_id", e.getId());
            map.put("title", e.getTitle());
            map.put("img", e.getHeadImg());
            JxtNewsTag jxtNewsTag = jxtNewsTags.stream().filter(p -> e.getTagList().stream().anyMatch(o -> o.equals(p.getId()))).findFirst().orElse(null);
            map.put("ability", jxtNewsTag != null ? jxtNewsTag.getTagName() : "");
            returnList.add(map);
        });

        return returnList;
    }

    //取提升建议
    private String adviceProgressContent(StudyProgressDetail progressDetail) {
        if (progressDetail == null || progressDetail.getSubjectId() == null) {
            return "";
        }
        String adviceProgressContent = "";
        switch (progressDetail.getSubjectId()) {
            case 103:
                if (StringUtils.isNotBlank(progressDetail.getWeakKp())) {
                    adviceProgressContent = adviceProgress.get(progressDetail.getWeakKp());
                }
                break;
            case 102:
                if (StringUtils.isNotBlank(progressDetail.getWeakKp())) {
                    adviceProgressContent = "练习薄弱项同类习题";
                }
                break;
            case 101:
                if (progressDetail.getLayoutTimes() != null) {
                    adviceProgressContent = "语文学习在于积累，多用多练定会稳步提升哦";
                }
                break;
            default:
                adviceProgressContent = "";
        }
        //薄弱想没有找到提升建议
        if (StringUtils.isBlank(adviceProgressContent)) {
            if (progressDetail.getLayoutTimes() == null) {
                adviceProgressContent = "上周没有作业，适度自学可检验学习成果哦";
            } else if (progressDetail.getAllQuestionNum() == null) {
                adviceProgressContent = "按时完成作业，巩固所学知识";
            } else if (SafeConverter.toInt(progressDetail.getWrongQestNum()) != 0) {
                adviceProgressContent = "订正错题，培养预习、复习的好习惯";
            } else {
                adviceProgressContent = "棒棒哒，上周所学知识都掌握，继续保持提升更多吧";
            }
        }
        return adviceProgressContent;
    }

    //取推荐的自学产品
    private SelfStudyType selfStudyType(StudyProgressDetail progressDetail) {
        if (progressDetail == null || progressDetail.getSubjectId() == null) {
            return null;
        }
        SelfStudyType type = null;
        if (StringUtils.isNotBlank(progressDetail.getWeakKp())) {
            type = adviceSelfStudyType.get(progressDetail.getWeakKp());
        }
        //没有配的自学产品。全部根据学科走默认值
        if (type == null) {
            if (progressDetail.getSubjectId() == 103) {
                return SelfStudyType.AFENTI_ENGLISH;
            } else if (progressDetail.getSubjectId() == 102) {
                return SelfStudyType.AFENTI_MATH;
            } else {
                return SelfStudyType.AFENTI_CHINESE;
            }
        }
        return type;
    }


    private List<ParentSelfStudyTypeH5Mapper> generateSelfStudyProducts(Collection<Long> studentIds, StudentDetail studentDetail, User parent) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return Collections.emptyList();
        }
        //黑名单用户在这里就返回了
        Map<Long, Boolean> blackListByStudent = userBlacklistServiceClient.isInBlackListByStudent(Collections.singleton(studentDetail));
        if ((MapUtils.isNotEmpty(blackListByStudent) && blackListByStudent.get(studentDetail.getId()) != null && blackListByStudent.get(studentDetail.getId())) || studentDetail.getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED) {
            return Collections.emptyList();
        }
        //为了跟该用户能用的应用做匹配，这里取该用户的appKeys与下面同班同学用的app做比较。
        List<FairylandProduct> availableFairylandProducts = businessVendorServiceClient.getParentAvailableFairylandProducts(parent, studentDetail, FairyLandPlatform.PARENT_APP, null);
        if (CollectionUtils.isEmpty(availableFairylandProducts)) {
            return Collections.emptyList();
        }
        Set<String> appKeys = availableFairylandProducts.stream().map(FairylandProduct::getAppKey).collect(Collectors.toSet());
        SelfStudyType hitStudyType = null;
        for (Long sid : studentIds) {
            //没有批量方法啊。。。
            List<UserActivatedProduct> activatedProducts = userOrderLoaderClient.loadUserActivatedProductList(sid);
            if (CollectionUtils.isNotEmpty(activatedProducts)) {
                activatedProducts = activatedProducts.stream().filter(e -> OrderProductServiceType.isJztOpenApp(e.getProductServiceType())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(activatedProducts)) {
                    activatedProducts = activatedProducts.stream().filter(e -> appKeys.contains(e.getProductServiceType())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(activatedProducts)) {
                        continue;
                    }
                    Collections.shuffle(activatedProducts);
                    hitStudyType = SelfStudyType.fromOrderType(OrderProductServiceType.safeParse(activatedProducts.get(0).getProductServiceType()));
                    break;
                }
            }
        }
        if (hitStudyType == null) {
            hitStudyType = SelfStudyType.AFENTI_ENGLISH;
        }
        Set<SelfStudyType> studyProducts = new TreeSet<>();
        studyProducts.add(SelfStudyType.PICLISTEN_ENGLISH);
        studyProducts.add(hitStudyType);
        List<ParentSelfStudyTypeH5Mapper> selfStudyMapperList = new ArrayList<>();
        Map<SelfStudyType, ParentSelfStudyTypeH5Mapper> studyTypeH5MapperMap = loadEntryMapper(studyProducts, parent, studentDetail, Boolean.FALSE, "330025", false);
        studyTypeH5MapperMap.entrySet().forEach(e -> {
            e.getValue().setSubTitle(selfStudyTypeSubTitle.get(e.getKey()) != null ? selfStudyTypeSubTitle.get(e.getKey()) : "");
            selfStudyMapperList.add(e.getValue());
        });
        return selfStudyMapperList;
    }

    //取专辑的类型
//    private Map<String, JxtNewsContentType> generateAlbumType(Collection<JxtNewsAlbum> albumCollection) {
//        if (CollectionUtils.isEmpty(albumCollection)) {
//            return Collections.emptyMap();
//        }
//        Set<String> firstNewsIds = new HashSet<>();
//        albumCollection.forEach(p -> {
//            if (CollectionUtils.isNotEmpty(p.getNewsRecordList())) {
//                firstNewsIds.add(p.getNewsRecordList().get(0).getNewsId());
//            }
//        });
//        Map<String, JxtNewsContentType> albumType = new HashMap<>();
//        jxtNewsLoaderClient.getJxtNewsByNewsIds(firstNewsIds)
//                .values()
//                .forEach(p -> albumType.put(p.getAlbumId(), JxtNewsContentType.parse(p.generateContentType())));
//        return albumType;
//    }


    private List<JxtNewsAlbum> filterAlbumList(List<String> albumIds) {
        if (CollectionUtils.isEmpty(albumIds)) {
            return Collections.emptyList();
        }

        Map<String, JxtNewsAlbum> albumByAlbumIds = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds);
        //Map<String, JxtNewsContentType> contentTypeMap = generateAlbumType(albumByAlbumIds.values());
        return albumByAlbumIds.values().stream()
                .filter(e -> e.getJxtNewsAlbumContentType() != null && e.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT)
                .filter(p -> p.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE || null == p.getFree() || p.getFree())
                .sorted(Comparator.comparingInt(o -> albumIds.indexOf(o.getId())))
                .collect(Collectors.toList());
    }
}
