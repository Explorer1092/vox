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

package com.voxlearning.washington.controller.mobile;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.reminder.api.ReminderService;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.AsyncNewsCacheService;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.controller.open.v1.util.JxtNewsUtil;
import com.voxlearning.washington.mapper.AlbumAbilityTagPlanBConfig;
import com.voxlearning.washington.mapper.AlbumTagCommentConfig;
import com.voxlearning.washington.mapper.ParentChannelAppConfig;
import lombok.Getter;
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


/**
 * 1.9.0---专辑相关（1.9.0之前的专辑相关在：JxtNewsController）
 * 2.2.2---资讯改版（收入“趣味音视频相关”）
 * Created by jiang wei on 2017/5/11.
 */
@Controller
@Slf4j
@RequestMapping(value = "/userMobile/jxtAlbum")
public class JxtAlbumController extends AbstractMobileJxtController {

    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;
    private static final Map<Integer, Long> clazzLevelNewsTagMap = new HashMap<>();

    static {
        clazzLevelNewsTagMap.put(1, 218L);
        clazzLevelNewsTagMap.put(2, 219L);
        clazzLevelNewsTagMap.put(3, 220L);
        clazzLevelNewsTagMap.put(4, 221L);
        clazzLevelNewsTagMap.put(5, 222L);
        clazzLevelNewsTagMap.put(6, 223L);
    }

    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject
    private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;

    @ImportService(interfaceClass = AsyncNewsCacheService.class)
    private AsyncNewsCacheService asyncNewsCacheService;
    @ImportService(interfaceClass = ReminderService.class)
    private ReminderService reminderService;

    private List<Long> tag_sort = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            tag_sort.add(294L);
            tag_sort.add(292L);
            tag_sort.add(293L);
        } else {
            tag_sort.add(435L);
            tag_sort.add(433L);
            tag_sort.add(434L);
        }

    }

    /**
     * 获取我的订阅首页列表（包括四个专辑，和所有订阅专辑的更新文章）
     */
    @RequestMapping(value = "getSubAlbumTopPage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSubAlbumList() {
        User user = currentUser();
        Long userId = user == null ? 0L : user.getId();
        if (userId == 0L) {
            return MapMessage.errorMessage("请登录后查看");
        }
        Map<Long, List<ParentNewsAlbumSubRecord>> albumSubRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(userId));
        if (MapUtils.isEmpty(albumSubRecordByUserId) || CollectionUtils.isEmpty(albumSubRecordByUserId.get(userId))) {
            return MapMessage.successMessage().add("update_news_list", Collections.EMPTY_LIST).add("album_list", Collections.EMPTY_LIST);
        }
        List<ParentNewsAlbumSubRecord> parentNewsAlbumSubRecords = albumSubRecordByUserId.get(userId);
        List<String> albumIds = parentNewsAlbumSubRecords.stream().filter(ParentNewsAlbumSubRecord::getIsSub).sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate())).map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toList());
        Map<String, JxtNewsAlbum> albumByAlbumIds = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds);
        List<JxtNewsAlbum> jxtNewsAlbums = albumByAlbumIds.values().stream()
                .filter(e -> SafeConverter.toBoolean(e.getFree(), Boolean.TRUE))
                .filter(e -> e.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE)
                .sorted(Comparator.comparingInt(o -> albumIds.indexOf(o.getId())))
                .collect(Collectors.toList());
        //取每个专辑的第一篇资讯的类型作为专辑的类型
        //Map<String, JxtNewsContentType> albumType = generateAlbumType(jxtNewsAlbums);
        //过滤掉图文专辑
        jxtNewsAlbums = jxtNewsAlbums.stream().filter(p -> p.getJxtNewsAlbumContentType() != null && p.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT).collect(Collectors.toList());
        List<Map<String, Object>> topAlbumList = generateSubAlbumInPageTop(jxtNewsAlbums, userId);
        List<Map<String, Object>> updateNewsList = generateUpdateNewsList(jxtNewsAlbums);
        //清除首页专辑更新的提醒
        reminderService.clearUserReminder(userId, ReminderPosition.PARENT_APP_INDEX_MY_STUDY_ALBUM);
        return MapMessage.successMessage().add("album_list", topAlbumList).add("total_count", jxtNewsAlbums.size()).add("update_news_list", updateNewsList);
    }


    /**
     * 获取订阅专辑的列表
     */
    @Deprecated
    @RequestMapping(value = "getJxtNewsAlbumList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsAlbumList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long userId = user.getId();
        Map<Long, List<ParentNewsAlbumSubRecord>> albumSubRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(userId));
        if (MapUtils.isEmpty(albumSubRecordByUserId) || CollectionUtils.isEmpty(albumSubRecordByUserId.get(userId))) {
            return MapMessage.successMessage("没有订阅专辑");
        }
        List<ParentNewsAlbumSubRecord> parentNewsAlbumSubRecords = albumSubRecordByUserId.get(userId);
        List<String> albumIds = parentNewsAlbumSubRecords.stream().filter(ParentNewsAlbumSubRecord::getIsSub).sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate())).map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toList());
        Map<String, JxtNewsAlbum> jxtNewsAlbumByAlbumIds = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds);
        if (MapUtils.isEmpty(jxtNewsAlbumByAlbumIds)) {
            return MapMessage.successMessage("订阅专辑获取失败");
        }
        List<JxtNewsAlbum> jxtNewsAlbums = jxtNewsAlbumByAlbumIds.values().stream()
                .filter(e -> SafeConverter.toBoolean(e.getFree(), Boolean.TRUE) && e.getOnline())
                .filter(e -> e.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE)
                .sorted(Comparator.comparingInt(o -> albumIds.indexOf(o.getId())))
                .collect(Collectors.toList());
        //取每个专辑的第一篇资讯的类型作为专辑的类型
        //Map<String, JxtNewsContentType> albumType = generateAlbumType(jxtNewsAlbums);
        //过滤掉图文专辑
        jxtNewsAlbums = jxtNewsAlbums.stream().filter(p -> p.getJxtNewsAlbumContentType() != null && p.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT).collect(Collectors.toList());
        List<Map<String, Object>> mapList = generateSubAlbumList(jxtNewsAlbums, userId);
        return MapMessage.successMessage().add("album_list", mapList);
    }

    /**
     * 专辑详情
     */
    @RequestMapping(value = "albumDetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage albumDetail() {
        String albumId = getRequestString("album_id");
        JxtNewsAlbum jxtNewsAlbum = jxtNewsLoaderClient.getJxtNewsAlbum(albumId);
        User user = currentUser();
        if (jxtNewsAlbum == null) {
            return MapMessage.errorMessage("您要查看的专辑不存在");
        }
        //记一下专辑阅读的数
        asyncNewsCacheService
                .JxtNewsCacheManager_incrCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_READ_COUNT, albumId)
                .awaitUninterruptibly();
        Map<String, Object> albumDetailMap = generateAlbumDetailMap(jxtNewsAlbum, user);

        return MapMessage.successMessage().add("album_detail", albumDetailMap).add("relative_album_list", generateAlbumRecommendList(jxtNewsAlbum, user));
    }

    /**
     * 智能学习-最适合的专辑
     */
    @RequestMapping(value = "smartLearningAlbum.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSmartLearningAlbums() {
        User user = currentUser();
        Long studentId = getRequestLong("sid");
        //这个是前端选择的能力标签。非手动选择适合的这个字段为空即可.这个参数不为空。则直接用这个能力标签
        String selectTagIds = getRequestString("tag_ids");
        //请求来源。目前只需要区分是否是首页的调用
        //来源为"index"的只返回每科2个。其余的来源全部返回
        String from = getRequestString("from");

        //这个本身就是按照订阅数降序排列的
        List<String> rankAlbumIds = asyncNewsCacheService.JxtNewsCacheManager_loadAlbumSubRank().getUninterruptibly();
        List<JxtNewsAlbum> onlineJxtNewsAlbum = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum();
        Set<Long> tagIds = new HashSet<>();
        List<JxtNewsAlbum> albumList = new ArrayList<>();
        //1、家长手动选择第一优先
        //2、当前孩子有非毕业班时，没有数据用配置的年级标签兜底
        //好坑。。这俩是拿来处理一个来源字段给前端打点。
        Set<JxtNewsAlbum> abilityAlbums = new HashSet<>();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        Set<Long> parentClazzLevel = getParentClazzLevel(Collections.singleton(studentDetail));
        if (StringUtils.isNotBlank(selectTagIds)) {
            List<Long> jsonToList = JsonUtils.fromJsonToList(selectTagIds, Long.class);
            if (CollectionUtils.isNotEmpty(jsonToList)) {
                tagIds.addAll(jsonToList);
            }
            List<JxtNewsAlbum> tagAlbums = onlineJxtNewsAlbum.stream()
                    .filter(e -> SafeConverter.toBoolean(e.getFree(), Boolean.TRUE))
                    .filter(e -> e.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE)
                    .filter(p -> tagIds.stream().anyMatch(e -> p.getTagList().contains(e)))
                    .filter(p -> CollectionUtils.isEmpty(parentClazzLevel) || parentClazzLevel.stream().anyMatch(e -> p.getTagList().contains(e)))
                    .sorted((o1, o2) -> rankAlbumIds.indexOf(o1.getId()) - rankAlbumIds.indexOf(o2.getId()))
                    .collect(Collectors.toList());
            albumList.addAll(tagAlbums);
        } else if (user != null && studentDetail != null && studentDetail.getClazz() != null && !studentDetail.getClazz().isTerminalClazz()) {
            //大数据的接口转化过来的薄弱标签
            Map<Integer, Set<Long>> subjectTagIds = getFitJxtNewsAlbumWithBigData(studentId);
            //必须返回3个学科的数据。如果孩子本身班级没有某个学科。用学段兜底方案返回这个学科
            Set<Integer> totalSubject = new HashSet<>();
            totalSubject.add(Subject.ENGLISH.getId());
            totalSubject.add(Subject.MATH.getId());
            totalSubject.add(Subject.CHINESE.getId());
            for (Integer subjectId : totalSubject) {
                Set<Long> subjectTagId = subjectTagIds.get(subjectId);
                if (CollectionUtils.isNotEmpty(subjectTagId)) {
                    tagIds.addAll(subjectTagId);
                    List<JxtNewsAlbum> subjectAlbums = onlineJxtNewsAlbum.stream()
                            .filter(p -> subjectTagId.stream().anyMatch(e -> p.getTagList().contains(e)))
                            .filter(p -> CollectionUtils.isEmpty(parentClazzLevel) || parentClazzLevel.stream().anyMatch(e -> p.getTagList().contains(e)))
                            .sorted((o1, o2) -> rankAlbumIds.indexOf(o1.getId()) - rankAlbumIds.indexOf(o2.getId()))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(subjectAlbums)) {
                        if ("index".equals(from) && subjectAlbums.size() >= 2) {
                            subjectAlbums = subjectAlbums.subList(0, 2);
                        }
                        albumList.addAll(subjectAlbums);
                        abilityAlbums.addAll(subjectAlbums);
                        continue;
                    }
                }
                //到这里就是要么没有这个学科的tag或者学科没有找到专辑了。直接用学段兜底方案中的对应学科数据
                List<AlbumAbilityTagPlanBConfig> tagPlanBConfigs;
                if (studentDetail.isJuniorStudent()) {
                    tagPlanBConfigs = getFitJxtNewsAlbumWithConfig(Arrays.asList(5, 6));
                } else if (studentDetail.isInfantStudent()) {
                    tagPlanBConfigs = getFitJxtNewsAlbumWithConfig(Arrays.asList(1, 2));
                } else {
                    tagPlanBConfigs = getFitJxtNewsAlbumWithConfig(Collections.singleton(studentDetail.getClazzLevelAsInteger()));
                }
                tagPlanBConfigs.stream()
                        .filter(p -> Objects.equals(p.getSubjectId(), subjectId))
                        .filter(p -> MapUtils.isNotEmpty(p.getTagMap()) && CollectionUtils.isNotEmpty(p.getAlbumIds()))
                        .forEach(p -> {
                            tagIds.addAll(p.getTagMap().keySet());
                            List<JxtNewsAlbum> backupList = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(p.getAlbumIds()).values()
                                    .stream()
                                    .filter(JxtNewsAlbum::getOnline)
                                    .filter(e -> p.getTagMap().keySet().stream().anyMatch(t -> e.getTagList().contains(t)))
                                    .sorted((o1, o2) -> rankAlbumIds.indexOf(o1.getId()) - rankAlbumIds.indexOf(o2.getId()))
                                    .collect(Collectors.toList());
                            if ("index".equals(from) && backupList.size() >= 2) {
                                backupList = backupList.subList(0, 2);
                            }
                            albumList.addAll(backupList);
                        });
            }
        } else {
            //没选择
            //当前孩子不是未毕业班级。直接返回3.4年级的配置
            getFitJxtNewsAlbumWithConfig(Arrays.asList(3, 4))
                    .stream()
                    .filter(p -> MapUtils.isNotEmpty(p.getTagMap()) && CollectionUtils.isNotEmpty(p.getAlbumIds()))
                    .forEach(p -> {
                        tagIds.addAll(p.getTagMap().keySet());
                        List<JxtNewsAlbum> backupList = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(p.getAlbumIds()).values()
                                .stream()
                                .filter(JxtNewsAlbum::getOnline)
                                .filter(e -> p.getTagMap().keySet().stream().anyMatch(t -> e.getTagList().contains(t)))
                                .sorted((o1, o2) -> rankAlbumIds.indexOf(o1.getId()) - rankAlbumIds.indexOf(o2.getId()))
                                .collect(Collectors.toList());
                        if ("index".equals(from) && backupList.size() >= 2) {
                            backupList = backupList.subList(0, 2);
                        }
                        albumList.addAll(backupList);
                    });
        }
        Map<String, List<Map<String, Object>>> abilityAlbumInfoMap = new LinkedHashMap<>();
        //所有需要用作归类的tag
        Map<Long, JxtNewsTag> jxtNewsTagMap = jxtNewsLoaderClient.findTagsByIds(tagIds);
        //排个序。保证每次归类结果一样
        List<Long> sortedTagIds = jxtNewsTagMap.values().stream().sorted(Comparator.comparingInt(o -> tag_sort.indexOf(o.getParentId()))).map(JxtNewsTag::getId).collect(Collectors.toList());
        //为了在最后做map的整体排序，做一个TagName的排好序的List
        List<String> sortedTagNames = jxtNewsTagMap.values().stream().sorted(Comparator.comparingInt(o -> tag_sort.indexOf(o.getParentId()))).map(JxtNewsTag::getTagName).collect(Collectors.toList());
        //取出来的所有专辑再按收藏数排序
        Collections.sort(albumList, (o1, o2) -> rankAlbumIds.indexOf(o1.getId()) - rankAlbumIds.indexOf(o2.getId()));
        if (CollectionUtils.isNotEmpty(albumList)) {
            //查用户订阅的专辑
            Set<String> subAlbumIds = new HashSet<>();
            if (user != null) {
                Map<Long, List<ParentNewsAlbumSubRecord>> map = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(user.getId()));
                if (MapUtils.isNotEmpty(map)) {
                    subAlbumIds = map.get(user.getId()).stream().filter(ParentNewsAlbumSubRecord::getIsSub).map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toSet());
                }
            }
            //所有专辑的订阅数
//            Set<String> totalAlbumIds = albumList.stream().map(JxtNewsAlbum::getId).collect(Collectors.toSet());
//            //避免一次查太多。分批查
//            List<List<String>> lists = CollectionUtils.splitList(new ArrayList<>(totalAlbumIds), 20);
//            Map<String, Long> albumSubCountMap = new HashMap<>();
//            for (List<String> list : lists) {
//                Map<String, Long> subCountMap = asyncNewsCacheService.JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_SUB_COUNT, list).getUninterruptibly();
//                if (MapUtils.isNotEmpty(subCountMap)) {
//                    albumSubCountMap.putAll(subCountMap);
//                }
//            }
            //取所有的能力标签，做文案展示做匹配
            Map<String, String> abilityText = generateAlbumAbilityText(albumList);
            Iterator<JxtNewsAlbum> iterator = albumList.iterator();
            while (iterator.hasNext()) {
                JxtNewsAlbum album = iterator.next();
                for (Long tagId : sortedTagIds) {
                    JxtNewsTag jxtNewsTag = jxtNewsTagMap.get(tagId);
                    if (jxtNewsTag == null) {
                        continue;
                    }
                    if (album.getTagList().contains(tagId)) {
                        Map<String, Object> albumMap = new HashMap<>();
                        //id
                        albumMap.put("id", album.getId());
                        //标题
                        albumMap.put("title", album.getTitle());
                        //头图
                        albumMap.put("img", album.getHeadImg());
                        //是否订阅
                        albumMap.put("had_sub", subAlbumIds.contains(album.getId()));
                        //订阅总数
//                        albumMap.put("sub_count", JxtNewsUtil.countFormat(SafeConverter.toLong(albumSubCountMap.get(album.getId()))));
                        albumMap.put("abilities", abilityText.get(album.getId()) != null ? abilityText.get(album.getId()) : "");
                        albumMap.put("come_from_ability", abilityAlbums.contains(album));
                        List<Map<String, Object>> mapList;
                        if (abilityAlbumInfoMap.containsKey(jxtNewsTag.getTagName())) {
                            mapList = abilityAlbumInfoMap.get(jxtNewsTag.getTagName());
                        } else {
                            mapList = new ArrayList<>();
                            abilityAlbumInfoMap.put(jxtNewsTag.getTagName(), mapList);
                        }
                        mapList.add(albumMap);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        //处理没有匹配出来任何专辑的能力标签
        for (Long tagId : sortedTagIds) {
            JxtNewsTag jxtNewsTag = jxtNewsTagMap.get(tagId);
            if (jxtNewsTag == null) {
                continue;
            }
            if (!abilityAlbumInfoMap.containsKey(jxtNewsTag.getTagName())) {
                abilityAlbumInfoMap.put(jxtNewsTag.getTagName(), Collections.emptyList());
            }
        }
        //学生信息
        Map<String, Object> studentInfo = new HashMap<>();

        if (studentDetail != null && studentDetail.getClazz() != null) {
            studentInfo.put("name", studentDetail.fetchRealname());
            studentInfo.put("img", getUserAvatarImgUrl(studentDetail));
            studentInfo.put("clazz_level", studentDetail.getClazzLevelAsInteger());
            studentInfo.put("abilities", jxtNewsTagMap.values().stream().map(JxtNewsTag::getTagName).collect(Collectors.toList()));
        }
        //非线上环境加一个输入大数据返回的薄弱项是什么。测试方便测
        Set<String> testAbilities = new HashSet<>();
        if (RuntimeMode.current().le(Mode.STAGING)) {
            List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
            //group对应的subject
            Map<Long, Integer> groupSubject = new HashMap<>();
            groupMappers.forEach(group -> groupSubject.put(group.getId(), group.getSubject().getId()));
            //大数据接口返回每个学科的薄弱点
            Map<Integer, String> bigDataWeaknessMap = parentReportLoaderClient.getParentReportLoader().getStudyProgressWeakKp(studentId, groupSubject);
            testAbilities = new HashSet<>(bigDataWeaknessMap.values());
        }
        Map<String, List<Map<String, Object>>> resultMap = new LinkedHashMap<>();
        abilityAlbumInfoMap.entrySet().stream().sorted(Comparator.comparingInt(o -> sortedTagNames.indexOf(o.getKey()))).forEachOrdered(e -> resultMap.put(e.getKey(), e.getValue()));


        return MapMessage.successMessage().add("album_info_map", resultMap)
                .add("student_info", studentInfo)
                .add("big_data_weak", testAbilities);
    }

    /**
     * 专辑订阅周排行榜
     */
    @RequestMapping(value = "album_week_rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsAlbumSubRankList() {
        User user = currentUser();
        List<String> rankAlbumIds = asyncNewsCacheService.JxtNewsCacheManager_loadAlbumSubRank().getUninterruptibly();
        List<JxtNewsAlbum> albumList = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(rankAlbumIds).values()
                .stream()
                .filter(e -> SafeConverter.toBoolean(e.getFree(), Boolean.TRUE) && e.getOnline())
                .filter(e -> e.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE)
                .sorted(Comparator.comparingInt(o -> rankAlbumIds.indexOf(o.getId())))
                .collect(Collectors.toList());
        List<JxtNewsAlbum.NewsRecord> newsRecordList = new ArrayList<>();
        albumList.forEach(e -> newsRecordList.addAll(e.getNewsRecordList()));
        Map<String, JxtNews> newsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(newsRecordList)) {
            List<String> newsIds = newsRecordList.stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toList());
            newsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        }
        //查用户订阅的专辑
        Set<String> subAlbumIds = new HashSet<>();
        if (user != null && CollectionUtils.isNotEmpty(albumList)) {
            Map<Long, List<ParentNewsAlbumSubRecord>> map = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(user.getId()));
            if (MapUtils.isNotEmpty(map) && CollectionUtils.isNotEmpty(map.get(user.getId()))) {
                subAlbumIds = map.get(user.getId())
                        .stream()
                        .filter(p -> p.getIsSub() != null && p.getIsSub())
                        .map(ParentNewsAlbumSubRecord::getSubAlbumId)
                        .collect(Collectors.toSet());
            }
        }
//        //所有专辑的订阅数
//        Set<String> totalAlbumIds = albumList.stream().map(JxtNewsAlbum::getId).collect(Collectors.toSet());
//        //避免一次查太多。分批查
//        List<List<String>> lists = CollectionUtils.splitList(new ArrayList<>(totalAlbumIds), 20);
//        Map<String, Long> albumSubCountMap = new HashMap<>();
//        for (List<String> list : lists) {
//            Map<String, Long> subCountMap = asyncNewsCacheService.JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_SUB_COUNT, list).getUninterruptibly();
//            if (MapUtils.isNotEmpty(subCountMap)) {
//                albumSubCountMap.putAll(subCountMap);
//            }
//        }
        Map<String, String> abilityText = generateAlbumAbilityText(albumList);
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (JxtNewsAlbum album : albumList) {
            if (CollectionUtils.isNotEmpty(album.getNewsRecordList())) {
                JxtNews jxtNews = newsMap.get(album.getNewsRecordList().get(0).getNewsId());
                if (jxtNews != null) {
                    if (StringUtils.equals(jxtNews.generateContentType(), JxtNewsContentType.IMG_AND_TEXT.name())) {
                        continue;
                    }
                }
            }
            Map<String, Object> albumMap = new HashMap<>();
            //id
            albumMap.put("id", album.getId());
            //标题
            albumMap.put("title", album.getTitle());
            //头图=前3位用大图
            if (rankAlbumIds.indexOf(album.getId()) < 3) {
                albumMap.put("img", album.getBigImgUrl());
            } else {
                albumMap.put("img", album.getHeadImg());
            }
            //是否订阅
            albumMap.put("had_sub", subAlbumIds.contains(album.getId()));
            //订阅总数
//            albumMap.put("sub_count", JxtNewsUtil.countFormat(SafeConverter.toLong(albumSubCountMap.get(album.getId()))));
            albumMap.put("abilities", abilityText.get(album.getId()) != null ? abilityText.get(album.getId()) : "");
            mapList.add(albumMap);
        }
        return MapMessage.successMessage().add("album_info_list", mapList);
    }

    /**
     * 查询用户默认可选的能力标签
     */

    @RequestMapping(value = "commonSmartLearningTag.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSmartLearningTag() {
        User parent = currentParent();
        Long studentId = getRequestLong("sid");
        //根据能力标签的tagId取出学科标签
        //这里要按学科排序。因为是固定不变的。已经确认数据库是语文-数学-英语的顺序。故直接倒序排列了。
        List<JxtNewsTag> subjectTags = jxtNewsLoaderClient.findTagByTagParentId(Collections.singleton(BASIC_ABILITY_TAG_ID))
                .stream()
                .sorted(Comparator.comparingInt(o -> tag_sort.indexOf(o.getId())))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(subjectTags)) {
            return MapMessage.successMessage();
        }
        //根据学科tag查询每个学科下的能力标签
        List<JxtNewsTag> abilityTags = jxtNewsLoaderClient.findTagByTagParentId(subjectTags.stream().map(JxtNewsTag::getId).collect(Collectors.toSet()));
        if (CollectionUtils.isEmpty(abilityTags)) {
            return MapMessage.successMessage();
        }
        //查询这些能力标签的备注
        Map<Long, String> tagCommentMap = generateParentAlbumAbilityTagConfig(abilityTags.stream().map(JxtNewsTag::getId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(AlbumTagCommentConfig::getTagId, AlbumTagCommentConfig::getComment));
        LinkedHashMap<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        //家长上次给孩子选择的标签
        Set<Long> selectTagIds = new HashSet<>();
        if (parent != null && studentId != 0) {
            selectTagIds = asyncNewsCacheService.SmartLearningTagCacheManager_loadUserSmartLearningTag(studentId).getUninterruptibly();
        }
        final Set<Long> studentTagIds = selectTagIds;
        for (JxtNewsTag tag : subjectTags) {
            if (tag == null) {
                continue;
            }
            List<Map<String, Object>> mapList = new ArrayList<>();
            abilityTags.stream().filter(p -> Objects.equals(p.getParentId(), tag.getId())).forEach(e -> {
                Map<String, Object> signalMap = new HashMap<>();
                signalMap.put("id", e.getId());
                signalMap.put("name", e.getTagName());
                signalMap.put("comment", SafeConverter.toString(tagCommentMap.get(e.getId())));
                signalMap.put("is_select", studentTagIds.contains(e.getId()));
                mapList.add(signalMap);
            });
            map.put(tag.getTagName(), mapList);
        }
        return MapMessage.successMessage().add("ability_tag_map", map);
    }

    @RequestMapping(value = "saveSmartLearningTag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSmartLearningTag() {
        User parent = currentParent();
        Long studentId = getRequestLong("sid");
        //没登录。不保存。也不报错
        if (parent == null) {
            return MapMessage.successMessage();
        }
        //没孩子。不保存。也不报错
        if (studentId == 0) {
            return MapMessage.successMessage();
        }
        String selectTagIds = getRequestString("tag_ids");
        List<Long> tagIds = JsonUtils.fromJsonToList(SafeConverter.toString(selectTagIds), Long.class);
        //保存
        asyncNewsCacheService.SmartLearningTagCacheManager_saveUserSmartLearningTag(studentId, new HashSet<>(tagIds));
        return MapMessage.successMessage();
    }


    /**
     * h5版的专辑应用的专辑列表页
     */
    @RequestMapping(value = "tag_album_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAlbumListByTag() {
        Long channelId = getRequestLong("tag") != 0L ? getRequestLong("tag") : getRequestLong("tag_id");
        Integer channelAppId = getRequestInt("channel") != 0 ? getRequestInt("channel") : getRequestInt("channel_app_id");
        User parent = currentParent();
        List<ParentChannelAppConfig> parentChannelAppConfigList = generateChannelAppConfig();
        if (CollectionUtils.isEmpty(parentChannelAppConfigList)) {
            return MapMessage.errorMessage("您要查看的专辑分类不存在");
        }
        ParentChannelAppConfig parentChannelAppConfig = parentChannelAppConfigList.stream().filter(config -> channelId.equals(SafeConverter.toLong(config.getChannelId())) && channelAppId.equals(SafeConverter.toInt(config.getAppId()))).findFirst().orElse(null);
        if (parentChannelAppConfig == null) {
            return MapMessage.errorMessage("您要查看的专辑分类不存在");
        }
        List<String> albumIds = parentChannelAppConfig.getAlbumIds();
        List<JxtNewsAlbum> albumList = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds).values()
                .stream()
                .filter(JxtNewsAlbum::getOnline)
                .filter(p -> p.getJxtNewsAlbumContentType() != null && p.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.UNKNOWN && p.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT)
                .sorted((o1, o2) -> albumIds.indexOf(o1.getId()) - albumIds.indexOf(o2.getId()))
                .collect(Collectors.toList());
        //当前登录用户的已订阅专辑
        Set<String> subAlbumIds = new HashSet<>();
        if (parent != null) {
            List<ParentNewsAlbumSubRecord> subRecordList = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(parent.getId())).get(parent.getId());
            if (CollectionUtils.isNotEmpty(subRecordList)) {
                subAlbumIds = subRecordList.stream().filter(p -> p.getIsSub() != null && p.getIsSub()).map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toSet());
            }
        }
        AlpsFuture<Map<String, Long>> albumReadCount = asyncNewsCacheService.JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_READ_COUNT, albumIds);
        List<Map<String, Object>> mapList = new ArrayList<>();
        //每个专辑的能力标签
        Map<String, String> albumAbilityText = generateAlbumAbilityText(albumList);
        for (JxtNewsAlbum album : albumList) {
            Map<String, Object> albumMap = new HashMap<>();
            //id
            albumMap.put("id", album.getId());
            //标题
            albumMap.put("title", album.getTitle());
            //专辑类型
            albumMap.put("type", album.getJxtNewsAlbumContentType().name());
            //头图
            albumMap.put("img", album.getHeadImg());
            //是否订阅
            albumMap.put("had_sub", subAlbumIds.contains(album.getId()));
            //阅读数
            albumMap.put("read_num", JxtNewsUtil.countFormat(SafeConverter.toLong(albumReadCount.getUninterruptibly().get(album.getId()))));
            albumMap.put("count", album.getNewsRecordList().size());
            albumMap.put("abilities", albumAbilityText.get(album.getId()) != null ? albumAbilityText.get(album.getId()) : "");
            mapList.add(albumMap);
        }
        return MapMessage.successMessage().add("album_list", mapList);
    }

    @RequestMapping(value = "tag_latest_news.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLatestAlbumNews() {
        Set<String> allNewsIds = new HashSet<>();
        Long userId = currentUserId();
        //把所有资讯ID取出来
        List<JxtNewsAlbum> albumList = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum()
                .stream()
                .filter(p -> p.getJxtNewsAlbumContentType() != null && p.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.UNKNOWN && p.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT)
                .filter(p -> CollectionUtils.isNotEmpty(p.getNewsRecordList()))
                .collect(Collectors.toList());
        albumList.forEach(p -> p.getNewsRecordList().forEach(e -> allNewsIds.add(e.getNewsId())));
        Map<String, JxtNewsAlbum> albumMap = albumList.stream().collect(Collectors.toMap(JxtNewsAlbum::getId, e -> e));
        List<JxtNews> jxtNewsList = jxtNewsLoaderClient.getJxtNewsByNewsIds(allNewsIds).values()
                .stream()
                .filter(p -> p.getOnline() != null && p.getOnline())
                .sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()))
                .limit(100)
                .collect(Collectors.toList());
        Set<Integer> parentRegionIds = new HashSet<>();
        if (userId != null && userId > 0L) {
            Set<Long> studentIds = studentLoaderClient.loadParentStudents(userId).stream().map(User::getId).collect(Collectors.toSet());
            Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
            for (StudentDetail studentDetail : studentDetails) {
                parentRegionIds.add(studentDetail.getStudentSchoolRegionCode());
                parentRegionIds.add(studentDetail.getCityCode());
                parentRegionIds.add(studentDetail.getRootRegionCode());
            }
        }
        //当前用户可见的资讯
        jxtNewsList = jxtNewsList.stream()
                .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                        || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && userId != null && userId.equals(p.getAvailableUserId()))
                        || (p.generatePushType().equals(JxtNewsPushType.REGION.getType()) && parentRegionIds.stream().anyMatch(e -> p.getRegionCodeList().contains(e))))
                .filter(e -> e.getJxtNewsType() != JxtNewsType.UNKNOWN)
                .filter(p -> !StringUtils.equals(p.generateContentType(), JxtNewsContentType.IMG_AND_TEXT.name()))
                .sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()))
                .collect(Collectors.toList());
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<Long, JxtNewsTag> subjectTagMaps = jxtNewsLoaderClient.findTagsByIds(tag_sort);
        for (JxtNews news : jxtNewsList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", news.getId());
            map.put("img", CollectionUtils.isEmpty(news.getCoverImgList()) ? "" : news.getCoverImgList().get(0));
            map.put("title", news.getTitle());
            map.put("update_time", news.getUpdateTime());
            map.put("news_content_type", news.getJxtNewsContentType().name());

            map.put("play_time", StringUtils.isNotBlank(news.getPlayTime()) ? JxtNewsUtil.formatTime(SafeConverter.toInt(news.getPlayTime())) : "");
            //这里前端要显示时间。由于已经给前端写好参数了。那这个返回值参数就不变了。
            map.put("create_time", DateUtils.dateToString(news.getPushTime(), "MM-dd"));
            //专辑属性
            map.put("album_id", news.getAlbumId());
            JxtNewsAlbum album = albumMap.get(news.getAlbumId());
            if (album != null) {
                //学科标签
                Long subjectTagId = album.getTagList().stream().filter(p -> tag_sort.contains(p)).findFirst().orElse(0L);
                JxtNewsTag jxtNewsTag = subjectTagMaps.get(subjectTagId);
                map.put("subject", jxtNewsTag == null ? "" : jxtNewsTag.getTagName());
                //集数
                JxtNewsAlbum.NewsRecord newsRecord = album.getNewsRecordList().stream().filter(p -> Objects.equals(p.getNewsId(), news.getId())).findFirst().orElse(null);
                map.put("episode", newsRecord == null ? "" : newsRecord.getRank());
                map.put("album_type", album.getJxtNewsAlbumContentType().name());
                map.put("album_img", album.getHeadImg());
            }

            mapList.add(map);
        }
        return MapMessage.successMessage().add("latest_news_list", mapList);
    }

    /**
     * 2.2.2老师推荐、作业同步（其实是1.9.0的老接口，数据都基本一致，所以直接拿来用）
     *
     * @return
     */
    @RequestMapping(value = "personal_recommend_news.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getPersonalRecommendNews() {
        Long studentId = getRequestLong("sid");
        //学生进度对应的同步内容
        List<String> homeworkProgressNewsIds = new ArrayList<>();
        Set<Subject> groupSubjects = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false).stream().map(GroupMapper::getSubject).collect(Collectors.toSet());
        Set<String> unitIds = new HashSet<>();
        Set<String> sectionIds = new HashSet<>();
        for (Subject subject : groupSubjects) {
            MapMessage homeworkProgress = newHomeworkPartLoaderClient.getStudentHomeworkProgress(studentId, subject, "");
            if (homeworkProgress.isSuccess()) {
                String unitId = SafeConverter.toString(homeworkProgress.get("unitId"), StringUtils.EMPTY);
                String sectionId = SafeConverter.toString(homeworkProgress.get("sectionId"), StringUtils.EMPTY);
                if (StringUtils.isNotBlank(unitId)) {
                    unitIds.add(unitId);
                }
                if (StringUtils.isNotBlank(sectionId)) {
                    sectionIds.add(sectionId);
                }
            }
        }
        jxtNewsLoaderClient.getJxtNewsBookRefByUnitIdsOrSectionIds(unitIds, sectionIds)
                .values()
                .forEach(p -> homeworkProgressNewsIds.addAll(p.getNewsIdList()));

        //学生班级不为空。查这个班级所有老师推荐的当前年级的资讯
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        //返回的推荐中资讯和推荐老师的对应关系
        Map<String, Long> recommendNewsAndTeacherId = new HashMap<>();
        //返回的所有资讯
        List<String> recommendNewsIds = new ArrayList<>();
        if (clazz != null) {
            List<Long> teacherIds = deprecatedClazzLoaderClient.getRemoteReference().loadClazzTeacherIds(clazz.getId());
            Map<Long, AlpsFuture<List<JxtNewsTeacherRecommend>>> recommendListByTeacherIds = jxtNewsLoaderClient.getTeacherRecommendListByTeacherIds(teacherIds, SchoolYear.newInstance().year());
            Set<JxtNewsTeacherRecommend> recommendSet = new HashSet<>();
            for (Long teacherId : recommendListByTeacherIds.keySet()) {
                recommendSet.addAll(recommendListByTeacherIds.get(teacherId).getUninterruptibly());
            }
            recommendSet.stream()
                    .filter(p -> clazz.getClazzLevel() == null || Objects.equals(p.getClazzLevel(), clazz.getClazzLevel().getLevel()))
                    .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                    .forEach(recommend -> {
                        if (recommendNewsIds.size() < 4 && !recommendNewsIds.contains(recommend.getRecommendId())) {
                            recommendNewsIds.add(recommend.getRecommendId());
                            recommendNewsAndTeacherId.put(recommend.getRecommendId(), recommend.getTeacherId());
                        }
                    });
        }

        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(recommendNewsAndTeacherId.values());

        //所有返回的资讯
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<JxtNews> totalNewsList = new ArrayList<>();
        //先展示同步拓展。有老师推荐的保留老师推荐的标签。
        jxtNewsLoaderClient.getJxtNewsByNewsIds(homeworkProgressNewsIds)
                .values()
                .stream()
                .filter(p -> p.getOnline() != null && p.getOnline())
                .filter(p -> !recommendNewsIds.contains(p.getId()))
                .sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()))
                .limit(6)
                .forEach(totalNewsList::add);
        //老师推荐的顺序。按照推荐时间倒序
        jxtNewsLoaderClient.getJxtNewsByNewsIds(recommendNewsIds)
                .values()
                .stream()
                .filter(p -> p.getOnline() != null && p.getOnline())
                .sorted((o1, o2) -> recommendNewsIds.indexOf(o1.getId()) - recommendNewsIds.indexOf(o2.getId()))
                .forEach(totalNewsList::add);
        if (CollectionUtils.isNotEmpty(totalNewsList)) {
            Set<String> albumIds = totalNewsList.stream().filter(e -> StringUtils.isNotBlank(e.getAlbumId())).map(JxtNews::getAlbumId).collect(Collectors.toSet());
            Set<String> newsIds = totalNewsList.stream().map(JxtNews::getId).collect(Collectors.toSet());
            AlpsFuture<Map<String, Long>> newsReadCount = asyncNewsCacheService.JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, newsIds);
            Map<String, JxtNewsAlbum> albumMap = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds);
            Map<String, String> albumAbilityText = generateAlbumAbilityText(albumMap.values());
            for (JxtNews news : totalNewsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", news.getId());
                map.put("title", news.getTitle());
                map.put("img", CollectionUtils.isEmpty(news.getCoverImgList()) ? "" : news.getCoverImgList().get(0));
                map.put("type", news.generateContentType());
                map.put("album_id", news.getAlbumId());
                map.put("update_time", news.getUpdateTime().getTime());
                if (StringUtils.isNotBlank(news.getAlbumId())) {
                    map.put("count", albumMap.get(news.getAlbumId()).getNewsRecordList().size());
                    map.put("abilities", SafeConverter.toString(albumAbilityText.get(news.getAlbumId())));
                }
                map.put("read_num", JxtNewsUtil.countFormat(SafeConverter.toLong(newsReadCount.getUninterruptibly().get(news.getId()))));
                if (StringUtils.equals(news.generateContentType(), JxtNewsContentType.IMG_AND_TEXT.name())) {
                    map.put("digest", news.getDigest());
                }
                if (recommendNewsIds.contains(news.getId())) {
                    Teacher teacher = teacherMap.get(recommendNewsAndTeacherId.get(news.getId()));
                    map.put("label", teacher == null ? "" : teacher.fetchRealname() + "老师推荐");
                } else if (homeworkProgressNewsIds.contains(news.getId())) {
                    map.put("label", "同步拓展");
                }
                mapList.add(map);
            }
        }

        //判断当前学生是否是黑名单用户，用户自学产品切tab
        Boolean isBlackList = Boolean.FALSE;
        if (studentId != 0L) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                isBlackList = Boolean.TRUE;
            } else {
                Map<Long, Boolean> blackListByStudent = userBlacklistServiceClient.isInBlackListByStudent(Collections.singleton(studentDetail));
                if (studentDetail.getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED || studentDetail.getClazz() == null || (MapUtils.isNotEmpty(blackListByStudent) && blackListByStudent.get(studentDetail.getId()) != null && blackListByStudent.get(studentDetail.getId()))) {
                    isBlackList = Boolean.TRUE;
                }
            }

        }
        return MapMessage.successMessage().add("personal_recommend_list", mapList).add("is_black_list_student", isBlackList);
    }

    @RequestMapping(value = "get_app_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getConfigAppList() {
        List<ParentChannelAppConfig> parentChannelAppConfigs = generateChannelAppConfig();
        if (CollectionUtils.isEmpty(parentChannelAppConfigs)) {
            return MapMessage.successMessage().add("app_list", Collections.emptyList());
        }
        parentChannelAppConfigs = parentChannelAppConfigs.stream().sorted(Comparator.comparingInt(o -> SafeConverter.toInt(o.getAppRank()))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentChannelAppConfigs)) {
            return MapMessage.successMessage().add("app_list", Collections.emptyList());
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        parentChannelAppConfigs.forEach(e -> {
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("tag_id", SafeConverter.toLong(e.getChannelId()));
            configMap.put("channel_app_id", SafeConverter.toInt(e.getAppId()));
            configMap.put("app_name", e.getAppName());
            String imgUrl = getCdnBaseUrlStaticSharedWithSep() + e.getImgUrl();
            configMap.put("img_url", imgUrl);
            returnList.add(configMap);
        });
        return MapMessage.successMessage().add("app_list", returnList);
    }


    @RequestMapping(value = "get_tags.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getConfigAppListNew() {
        List<ParentChannelAppConfig> parentChannelAppConfigs = generateChannelAppConfig();
        if (CollectionUtils.isEmpty(parentChannelAppConfigs)) {
            return MapMessage.successMessage().add("tags", Collections.emptyList());
        }
        parentChannelAppConfigs = parentChannelAppConfigs.stream().sorted(Comparator.comparingInt(o -> SafeConverter.toInt(o.getAppRank()))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentChannelAppConfigs)) {
            return MapMessage.successMessage().add("tags", Collections.emptyList());
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        parentChannelAppConfigs.forEach(e -> {
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("tag", SafeConverter.toLong(e.getChannelId()));
            configMap.put("channel", SafeConverter.toInt(e.getAppId()));
            configMap.put("name", e.getAppName());
            String imgUrl = getCdnBaseUrlStaticSharedWithSep() + e.getImgUrl();
            configMap.put("img", imgUrl);
            returnList.add(configMap);
        });
        return MapMessage.successMessage().add("tags", returnList);
    }

    @RequestMapping(value = "get_top_button_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTopButtonList() {
        User parent = currentParent();
        Long studentId = getRequestLong("sid");
        List<Map<String, Object>> mapList = new ArrayList<>();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        for (AlbumPageTopButton button : AlbumPageTopButton.values()) {
            if (button.getOnline()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", button.getName());
                map.put("url", button.getUrl());
                map.put("icon", getCdnBaseUrlStaticSharedWithSep() + button.getIcon());
                map.put("is_need_login", button.getIsNeedLogin());
                mapList.add(map);
            }
        }
        //非小学或者20001不显示提高模块。直接返回了。
        boolean showMore = studentDetail != null && studentDetail.getClazz() != null && !studentDetail.getClazz().isTerminalClazz() && studentDetail.isPrimaryStudent()
                && !userBlacklistServiceClient.isInUserBlackList(parent);
        if (!showMore) {
            return MapMessage.successMessage().add("nav_list", mapList);
        }
        //判断是否在黑名单
        Boolean isBlackList = Boolean.FALSE;
        Map<Long, Boolean> blackListByStudent = userBlacklistServiceClient.isInBlackListByStudent(Collections.singleton(studentDetail));
        if (studentDetail.getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED || studentDetail.getClazz() == null || (MapUtils.isNotEmpty(blackListByStudent) && blackListByStudent.get(studentDetail.getId()) != null && blackListByStudent.get(studentDetail.getId()))) {
            isBlackList = Boolean.TRUE;
        }
        //需要展示提高的用户
        Map<String, Object> map = new HashMap<>();
        map.put("name", AlbumPageTopButton.PROGRESS.getName());
        map.put("url", isBlackList ? AlbumPageTopButton.PROGRESS.getUrl() + "?tab=free" : AlbumPageTopButton.PROGRESS.getUrl());
        map.put("icon", getCdnBaseUrlStaticSharedWithSep() + AlbumPageTopButton.PROGRESS.getIcon());
        map.put("is_need_login", AlbumPageTopButton.PROGRESS.getIsNeedLogin());
        mapList.add(0, map);
        return MapMessage.successMessage().add("nav_list", mapList);
    }

    /**
     * 获取用户的历史记录列表
     */
    @RequestMapping(value = "get_news_read_history.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getNewsReadHistory() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        List<JxtNewsReadHistory> newsReadHistory = jxtNewsLoaderClient.getNewsReadHistory(parent.getId());
        return MapMessage.successMessage().add("news_history_list", generateNewsHistoryList(newsReadHistory));
    }

    /**
     * 删除用户的历史记录
     */
    @RequestMapping(value = "remove_news_read_history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeNewsReadHistory() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage();
        }
        String newsIds = getRequestString("news_ids");
        if (StringUtils.isBlank(newsIds)) {
            return MapMessage.errorMessage();
        }
        List<String> newsIdList = JsonUtils.fromJsonToList(SafeConverter.toString(newsIds), String.class);
        if (CollectionUtils.isEmpty(newsIdList)) {
            return MapMessage.errorMessage();
        }
        List<String> historyIds = new ArrayList<>();
        newsIdList.forEach(e -> {
            String historyId = JxtNewsReadHistory.generateId(parent.getId(), e);
            historyIds.add(historyId);
        });
        jxtNewsServiceClient.removeJxtNewsReadHistory(historyIds);
        return MapMessage.successMessage();
    }

    /**
     * 更新、添加用户的历史记录
     */
    @RequestMapping(value = "upsert_news_read_history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertNewsReadHistory() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage();
        }
        String newsId = getRequestString("news_id");
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage();
        }
        JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(newsId);
        if (jxtNews == null) {
            return MapMessage.errorMessage();
        }
        try {
            AtomicLockManager.instance()
                    .wrapAtomic(jxtNewsServiceClient)
                    .keyPrefix("JxtAlbumController::upsertNewsReadHistory")
                    .keys(parent.getId(), newsId)
                    .proxy()
                    .upsertJxtNewsReadHistory(parent.getId(), newsId);
        } catch (CannotAcquireLockException ignore) {
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("更新阅读记录失败");
        }
        return MapMessage.successMessage();
    }


    /**
     * 2.2.2-我的节目盒
     */
    @RequestMapping(value = "myBox.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage myBox() {
        User parent = currentParent();
        long sid = getRequestLong("sid");
        boolean inBox = getRequestBool("is_in_box");
        if (inBox && parent == null) {
            return noLoginResult;
        }
        List<JxtNewsAlbum> albumList = generateMyBoxList(parent);
        List<String> recommendIds = new ArrayList<>();
        List<Map<String, Object>> returnList = new ArrayList<>();
        if (!inBox) {
            //如果订阅专辑是空，就按当前学生的年级来取配置中的专辑
            if (CollectionUtils.isEmpty(albumList)) {
                List<AlbumAbilityTagPlanBConfig> fitAlbumList;
                if (sid != 0L) {
                    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
                    ClazzLevel clazzLevel = studentDetail.getClazzLevel();
                    //这个是之前的逻辑，如果学生是小学非毕业班，就根据学生的年级取配置中的专辑
                    if (clazzLevel != null && studentDetail.isPrimaryStudent() && !studentDetail.getClazz().isTerminalClazz()) {
                        fitAlbumList = getFitJxtNewsAlbumWithConfig(Collections.singleton(clazzLevel.getLevel()));
                        //其余情况都取3、4年级的专辑作为兜底方案
                    } else {
                        fitAlbumList = getFitJxtNewsAlbumWithConfig(Arrays.asList(3, 4));
                    }
                } else {
                    fitAlbumList = getFitJxtNewsAlbumWithConfig(Arrays.asList(3, 4));
                }
                List<String> finalRecommendIds = recommendIds;
                //用于后面拼装数据的时候，判断是否有推荐标签
                fitAlbumList.forEach(e -> finalRecommendIds.addAll(e.getAlbumIds()));
                //随机一下
                Collections.shuffle(finalRecommendIds);
                //取前两个
                recommendIds = finalRecommendIds.subList(0, 2);
                albumList = new ArrayList<>(jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(recommendIds).values());
            }
        }
        if (CollectionUtils.isNotEmpty(albumList)) {
            Map<String, String> albumAbilityText = generateAlbumAbilityText(albumList);
            Set<String> albumIds = albumList.stream().map(JxtNewsAlbum::getId).collect(Collectors.toSet());
            AlpsFuture<Map<String, Long>> albumReadCount = asyncNewsCacheService.JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_READ_COUNT, albumIds);
            List<String> finalRecommendIds1 = recommendIds;
            albumList.forEach(e -> {
                Map<String, Object> returnMap = new HashMap<>();
                returnMap.put("album_id", e.getId());
                returnMap.put("title", e.getTitle());
                returnMap.put("abilities", SafeConverter.toString(albumAbilityText.get(e.getId())));
                returnMap.put("count", e.getNewsRecordList().size());
                returnMap.put("read_num", JxtNewsUtil.countFormat(SafeConverter.toLong(albumReadCount.getUninterruptibly().get(e.getId()))));
                returnMap.put("img_url", e.getHeadImg());
                if (finalRecommendIds1.contains(e.getId())) {
                    returnMap.put("is_recommend", Boolean.TRUE);
                } else {
                    returnMap.put("is_recommend", Boolean.FALSE);
                }
                returnList.add(returnMap);
            });
        }
        return MapMessage.successMessage().add("album_list", returnList);
    }

    /**
     * 2.2.2-人气推荐
     */
    @RequestMapping(value = "recommendList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage recommendList() {
        User parent = currentParent();
        boolean inBox = getRequestBool("is_in_box");
        List<JxtNewsAlbum> albumList = generateAlbumListFromRankRecord(inBox, parent);
        Map<String, String> albumAbilityText = generateAlbumAbilityText(albumList);
        Set<String> albumIds = albumList.stream().map(JxtNewsAlbum::getId).collect(Collectors.toSet());
        AlpsFuture<Map<String, Long>> albumReadCount = asyncNewsCacheService.JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_READ_COUNT, albumIds);
        List<String> subAlbumIds = new ArrayList<>();
        if (parent != null) {
            Map<Long, List<ParentNewsAlbumSubRecord>> subRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(parent.getId()));
            if (MapUtils.isNotEmpty(subRecordByUserId)) {
                List<ParentNewsAlbumSubRecord> subRecordList = subRecordByUserId.get(parent.getId());
                if (CollectionUtils.isNotEmpty(subRecordList)) {
                    subAlbumIds = subRecordList.stream().filter(e -> e.getIsSub() != null && e.getIsSub()).map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toList());
                }
            }
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        List<String> finalSubAlbumIds = subAlbumIds;
        albumList.forEach(e -> {
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("album_id", e.getId());
            returnMap.put("title", e.getTitle());
            returnMap.put("abilities", SafeConverter.toString(albumAbilityText.get(e.getId())));
            returnMap.put("count", e.getNewsRecordList().size());
            returnMap.put("read_num", JxtNewsUtil.countFormat(SafeConverter.toLong(albumReadCount.getUninterruptibly().get(e.getId()))));
            returnMap.put("img_url", e.getHeadImg());
            returnMap.put("had_sub", finalSubAlbumIds.contains(e.getId()));
            returnList.add(returnMap);
        });
        return MapMessage.successMessage().add("recommend_list", returnList);
    }

    //2.2.2-我的节目盒
    private List<JxtNewsAlbum> generateMyBoxList(User parent) {
        if (parent == null) {
            return Collections.emptyList();
        }
        Map<Long, List<ParentNewsAlbumSubRecord>> albumSubRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(parent.getId()));
        if (MapUtils.isEmpty(albumSubRecordByUserId)) {
            return Collections.emptyList();
        }
        List<ParentNewsAlbumSubRecord> albumSubRecordList = albumSubRecordByUserId.get(parent.getId());
        if (CollectionUtils.isEmpty(albumSubRecordList)) {
            return Collections.emptyList();
        }
        List<String> subAlbumIds = albumSubRecordList
                .stream()
                .filter(ParentNewsAlbumSubRecord::getIsSub)
                .map(ParentNewsAlbumSubRecord::getSubAlbumId)
                .collect(Collectors.toList());

        return jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(subAlbumIds)
                .values()
                .stream()
                .filter(e -> SafeConverter.toBoolean(e.getFree(), Boolean.TRUE) && e.getOnline())
                .filter(e -> e.getNewsRecordList().size() != 0)
                .filter(e -> JxtNewsAlbumType.INSIDE == e.generateJxtNewsAlbumType()
                        && JxtNewsAlbumContentType.IMG_AND_TEXT != e.getJxtNewsAlbumContentType())
                .collect(Collectors.toList());
    }

    //2.2.2-根据参数来判断是首页，还是节目盒的页面。因为逻辑有点不同
    private List<JxtNewsAlbum> generateAlbumListFromRankRecord(Boolean isBox, User parent) {
        List<String> rankAlbumIds = asyncNewsCacheService.JxtNewsCacheManager_loadAlbumSubRank().getUninterruptibly();
        List<JxtNewsAlbum> albumList = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(rankAlbumIds).values()
                .stream()
                .filter(e -> SafeConverter.toBoolean(e.getFree(), Boolean.TRUE) && e.getOnline())
                .filter(e -> e.getNewsRecordList().size() != 0)
                .filter(e -> e.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE)
                .filter(e -> e.getJxtNewsAlbumContentType() != null && e.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT)
                .sorted(Comparator.comparingInt(o -> rankAlbumIds.indexOf(o.getId())))
                .limit(40)
                .collect(Collectors.toList());
        if (isBox) {
            //查用户订阅的专辑
            Set<String> subAlbumIds = new HashSet<>();
            if (parent != null) {
                Map<Long, List<ParentNewsAlbumSubRecord>> map = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(parent.getId()));
                if (MapUtils.isNotEmpty(map) && CollectionUtils.isNotEmpty(map.get(parent.getId()))) {
                    subAlbumIds = map.get(parent.getId())
                            .stream()
                            .filter(p -> p.getIsSub() != null && p.getIsSub())
                            .map(ParentNewsAlbumSubRecord::getSubAlbumId)
                            .collect(Collectors.toSet());
                }
            }
            Set<String> finalSubAlbumIds = subAlbumIds;
            List<JxtNewsAlbum> recommendAlbums = albumList.stream().filter(e -> !finalSubAlbumIds.contains(e.getId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(recommendAlbums)) {
                Collections.shuffle(recommendAlbums);
                recommendAlbums = recommendAlbums.subList(0, 6);
            }
            return recommendAlbums;
        } else {
            Collections.shuffle(albumList);
            albumList = albumList.subList(0, 4);
            return albumList;
        }
    }


    //生成页面顶部专辑列表
    private List<Map<String, Object>> generateSubAlbumInPageTop(List<JxtNewsAlbum> jxtNewsAlbums, Long userId) {
        if (CollectionUtils.isEmpty(jxtNewsAlbums)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
//        jxtNewsAlbums = jxtNewsAlbums.size() >= 4 ? jxtNewsAlbums.subList(0, 4) : jxtNewsAlbums;
        Map<String, Long> albumsUpdateCountMap = generateAlbumsIsUpdate(jxtNewsAlbums, userId);
        List<JxtNewsAlbum.NewsRecord> newsRecordList = new ArrayList<>();
        jxtNewsAlbums.forEach(e -> newsRecordList.addAll(e.getNewsRecordList()));
        Map<String, JxtNews> newsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(newsRecordList)) {
            List<String> newsIds = newsRecordList.stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toList());
            newsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        }
        Map<String, JxtNews> finalNewsMap = newsMap;
        jxtNewsAlbums.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("album_id", e.getId());
            map.put("title", e.getTitle());
            Long updateCount = albumsUpdateCountMap.get(e.getId());
            if (updateCount != null && updateCount != 0L) {
                map.put("is_new", Boolean.TRUE);
            } else {
                map.put("is_new", Boolean.FALSE);
            }
            if (CollectionUtils.isNotEmpty(e.getNewsRecordList())) {
                JxtNews jxtNews = finalNewsMap.get(e.getNewsRecordList().get(0).getNewsId());
                if (jxtNews != null) {
                    map.put("content_type", jxtNews.getJxtNewsContentType() != null ? jxtNews.getJxtNewsContentType().name() : JxtNewsContentType.IMG_AND_TEXT.name());
                }
            }
            map.put("online", e.getOnline());
            map.put("img_url", e.getHeadImg());
            map.put("online", e.getOnline());
            returnList.add(map);
        });
        return returnList;
    }


    //生成首页更新文章的列表
    private List<Map<String, Object>> generateUpdateNewsList(List<JxtNewsAlbum> jxtNewsAlbums) {
        if (CollectionUtils.isEmpty(jxtNewsAlbums)) {
            return Collections.emptyList();
        }
        Set<JxtNewsAlbum.NewsRecord> newsRecordList = new HashSet<>();
        Map<String, Date> newsDateMap = new HashMap<>();
        jxtNewsAlbums = jxtNewsAlbums.stream().filter(JxtNewsAlbum::getOnline).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(jxtNewsAlbums)) {
            return Collections.emptyList();
        }
        //取所有专辑里的文章
        jxtNewsAlbums.forEach(e -> newsRecordList.addAll(e.getNewsRecordList()));
        //拿到专辑的封面，用于后面拼返回数据
        Map<String, String> albumImgMap = jxtNewsAlbums.stream().collect(Collectors.toMap(JxtNewsAlbum::getId, JxtNewsAlbum::getHeadImg));
        Map<String, Date> newsCreateDateMap = newsRecordList.stream().collect(Collectors.toMap(JxtNewsAlbum.NewsRecord::getNewsId, JxtNewsAlbum.NewsRecord::getCreateTime));
        Map<String, Integer> newsRankMap = newsRecordList.stream().collect(Collectors.toMap(JxtNewsAlbum.NewsRecord::getNewsId, JxtNewsAlbum.NewsRecord::getRank));
        Set<String> newsIds = newsRecordList.stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet());
        Map<String, JxtNews> jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        List<JxtNews> onlineNewsList = jxtNewsByNewsIds.values().stream().filter(JxtNews::getOnline).collect(Collectors.toList());
        onlineNewsList.forEach(p -> {
            if (p.getPushTime() != null && newsCreateDateMap.get(p.getId()) != null) {
                if (p.getPushTime().before(newsCreateDateMap.get(p.getId()))) {
                    newsDateMap.put(p.getId(), newsCreateDateMap.get(p.getId()));
                } else {
                    newsDateMap.put(p.getId(), p.getPushTime());
                }
            }
        });
        if (onlineNewsList.size() >= 50) {
            onlineNewsList = onlineNewsList.stream().sorted((o1, o2) -> newsDateMap.get(o2.getId()).compareTo(newsDateMap.get(o1.getId()))).limit(50).collect(Collectors.toList());
        } else {
            onlineNewsList = onlineNewsList.stream().sorted((o1, o2) -> newsDateMap.get(o2.getId()).compareTo(newsDateMap.get(o1.getId()))).collect(Collectors.toList());
        }
        return generateAlbumUpdateNewsList(onlineNewsList, newsRankMap, albumImgMap, newsDateMap);
    }


    //生成我的订阅的news更新列表
    private List<Map<String, Object>> generateAlbumUpdateNewsList(List<JxtNews> newsList, Map<String, Integer> newsRankMap, Map<String, String> albumImgMap, Map<String, Date> albumNewsTimeMap) {
        if (CollectionUtils.isEmpty(newsList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        newsList.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("news_id", e.getId());
            map.put("album_id", e.getAlbumId());
            map.put("title", e.getTitle());
            map.put("update_time", e.getUpdateTime());
            map.put("content_type", e.getJxtNewsContentType() != null ? e.getJxtNewsContentType().name() : JxtNewsContentType.IMG_AND_TEXT.name());
            if (StringUtils.isNotBlank(e.getPlayTime())) {
                map.put("play_time", JxtNewsUtil.formatTime(SafeConverter.toInt(e.getPlayTime())));
            }
            if (MapUtils.isNotEmpty(albumNewsTimeMap)) {
                map.put("online_time", albumNewsTimeMap.get(e.getId()) != null ? DateUtils.dateToString(albumNewsTimeMap.get(e.getId()), DateUtils.FORMAT_SQL_DATE) : "");
            }
            if (e.getJxtNewsType() != null && e.getJxtNewsType() == JxtNewsType.BIG_IMAGE) {
                map.put("news_img", e.getCoverImgList().get(0));
            }
            if (MapUtils.isNotEmpty(albumImgMap)) {
                map.put("album_img", albumImgMap.get(e.getAlbumId()) != null ? combineCdbUrl(albumImgMap.get(e.getAlbumId())) : "");
            }
            if (MapUtils.isNotEmpty(newsRankMap) && newsRankMap.get(e.getId()) != null) {
                map.put("episode", newsRankMap.get(e.getId()));
            }
            returnList.add(map);
        });
        return returnList;
    }

    //批量生成专辑是否更新,这里搞个数字的，是为了以后万一把红点改成数字。。。
    private Map<String, Long> generateAlbumsIsUpdate(Collection<JxtNewsAlbum> jxtNewsAlbums, Long userId) {
        if (CollectionUtils.isEmpty(jxtNewsAlbums) || userId == 0L) {
            return Collections.emptyMap();
        }
        Map<String, Long> map = new HashMap<>();
        //取当前用户的所有订阅记录
        List<ParentShowAlbumRecord> showAlbumRecordByUserId = jxtNewsLoaderClient.getParentShowAlbumRecordByUserId(userId);
        Set<ParentShowAlbumRecord> parentShowAlbumRecords = new HashSet<>(showAlbumRecordByUserId);
        //取专辑的所有文章
        Set<String> newsIds = new HashSet<>();
        Set<String> onlineNewsIds = new HashSet<>();
        Map<String, JxtNews> jxtNewsByNewsIds = new HashMap<>();
        jxtNewsAlbums.forEach(e -> newsIds.addAll(e.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toList())));
        if (CollectionUtils.isNotEmpty(newsIds)) {
            jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
            if (MapUtils.isNotEmpty(jxtNewsByNewsIds)) {
                onlineNewsIds = jxtNewsByNewsIds.values().stream().filter(JxtNews::getOnline).map(JxtNews::getId).collect(Collectors.toSet());
            }
        }
        final Set<String> finalOnlineNewsIds = onlineNewsIds;
        //如果一个都看过，都是新的
        if (CollectionUtils.isEmpty(parentShowAlbumRecords)) {
            jxtNewsAlbums.forEach(e -> {
                Long empty_count = 0L;
                List<JxtNewsAlbum.NewsRecord> newsRecordList = e.getNewsRecordList();
                if (CollectionUtils.isNotEmpty(newsRecordList)) {
                    empty_count = newsRecordList.stream().filter(p -> CollectionUtils.isNotEmpty(finalOnlineNewsIds) && finalOnlineNewsIds.contains(p.getNewsId())).count();
                }
                map.put(e.getId(), empty_count);
            });
            return map;
        }
        Map<String, Date> albumUpdateMap = parentShowAlbumRecords.stream().collect(Collectors.toMap(ParentShowAlbumRecord::getAlbumId, ParentShowAlbumRecord::getUpdateTime));
        final Map<String, JxtNews> finalJxtNewsByNewsIds = jxtNewsByNewsIds;
        //判断专辑是否有更新
        jxtNewsAlbums.forEach(e -> {
            List<JxtNewsAlbum.NewsRecord> newsRecordList = e.getNewsRecordList();
            Long count = 0L;
            if (CollectionUtils.isNotEmpty(newsRecordList)) {
                Date showUpdateDate = albumUpdateMap.get(e.getId());
                if (showUpdateDate != null) {
                    //列表页中的文章更新数，根据文章加入专辑的时间和文章的上线时间与用户的浏览时间进行判断
                    count = newsRecordList.stream().filter(p -> CollectionUtils.isNotEmpty(finalOnlineNewsIds) && finalOnlineNewsIds.contains(p.getNewsId()))
                            .filter(p -> finalJxtNewsByNewsIds.get(p.getNewsId()) != null && finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime() != null
                                    && p.getCreateTime().before(finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime()) ? finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime().after(showUpdateDate) : p.getCreateTime().after(showUpdateDate))
                            .count();
                } else {
                    //没有该专辑的浏览记录，表示这个用户订阅了，但是一直没打开过这个专辑，这种情况下，这个专辑里的文章都算更新
                    count = newsRecordList.stream().filter(p -> CollectionUtils.isNotEmpty(finalOnlineNewsIds) && finalOnlineNewsIds.contains(p.getNewsId())).count();
                }
            }
            map.put(e.getId(), count);
        });
        return map;
    }

    //生成该用户订阅专辑内更新的文章
    private Map<String, Date> generateUpdateNewsInAlbum(Collection<JxtNewsAlbum> jxtNewsAlbums, Long userId) {
        if (CollectionUtils.isEmpty(jxtNewsAlbums) || userId == 0L) {
            return Collections.emptyMap();
        }
        Map<String, Date> newsDateMap = new HashMap<>();
        //浏览记录
        List<ParentShowAlbumRecord> parentShowAlbumRecordByUserId = jxtNewsLoaderClient.getParentShowAlbumRecordByUserId(userId);
        //所有专辑内的文章
        List<JxtNewsAlbum.NewsRecord> newsRecordList = new ArrayList<>();
        jxtNewsAlbums.stream().filter(e -> CollectionUtils.isNotEmpty(e.getNewsRecordList())).forEach(e -> newsRecordList.addAll(e.getNewsRecordList()));
        if (CollectionUtils.isEmpty(newsRecordList)) {
            return Collections.emptyMap();
        }
        Set<String> newsIds = new HashSet<>();
        /*
        * 这里搞出一个key是资讯id,value是专辑id的map,
        * 因为用户的浏览记录是已专辑为维度的，
        * 所以，利用这个map可以更好的确定出文章对应的浏览时间
        * */
        newsRecordList.forEach(e -> newsIds.add(e.getNewsId()));
        Map<String, JxtNews> newsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        Map<String, String> newsAlbumMap = newsMap.values().stream().collect(Collectors.toMap(JxtNews::getId, JxtNews::getAlbumId));
        Set<ParentShowAlbumRecord> parentShowAlbumRecords = new HashSet<>(parentShowAlbumRecordByUserId);
        if (CollectionUtils.isEmpty(parentShowAlbumRecords)) {
            return Collections.emptyMap();
        }
        //用户浏览记录
        Map<String, Date> dateMap = parentShowAlbumRecords.stream().collect(Collectors.toMap(ParentShowAlbumRecord::getAlbumId, ParentShowAlbumRecord::getUpdateTime));

        newsRecordList.stream().filter(p -> newsMap.get(p.getNewsId()) != null && newsMap.get(p.getNewsId()).getPushTime() != null)
                /*
                * 判断出资讯的上线时间和资讯加入专辑的时间中最新的时间，用最新的那个时间与用户的浏览时间进行比较
                * 之前的逻辑是下面这段代码，备查
                * p.getCreateTime().before(finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime()) ? finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime().after(parentShowAlbumRecord.getUpdateTime()) : p.getCreateTime().after(parentShowAlbumRecord.getUpdateTime()))
                * */
                .forEach(e -> {
                    if (newsAlbumMap.get(e.getNewsId()) != null && dateMap.get(newsAlbumMap.get(e.getNewsId())) != null) {
                        if (e.getCreateTime().before(newsMap.get(e.getNewsId()).getPushTime())) {
                            if (newsMap.get(e.getNewsId()).getPushTime().after(dateMap.get(newsAlbumMap.get(e.getNewsId())))) {
                                newsDateMap.put(e.getNewsId(), newsMap.get(e.getNewsId()).getPushTime());
                            }
                        } else {
                            if (e.getCreateTime().after(dateMap.get(newsAlbumMap.get(e.getNewsId())))) {
                                newsDateMap.put(e.getNewsId(), e.getCreateTime());
                            }
                        }
                    }
                });
        return newsDateMap;
    }

    // 生成已订阅列表
    private List<Map<String, Object>> generateSubAlbumList(List<JxtNewsAlbum> albums, Long userId) {
        if (CollectionUtils.isEmpty(albums)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        Map<String, String> abilityText = generateAlbumAbilityText(albums);
        Map<String, Long> updateAlbumMap = generateAlbumsIsUpdate(albums, userId);
        List<JxtNewsAlbum.NewsRecord> newsRecordList = new ArrayList<>();
        albums.forEach(e -> newsRecordList.addAll(e.getNewsRecordList()));
        Map<String, JxtNews> newsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(newsRecordList)) {
            List<String> newsIds = newsRecordList.stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toList());
            newsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        }
        Map<String, JxtNews> finalNewsMap = newsMap;
        albums.forEach((JxtNewsAlbum e) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("album_id", e.getId());
            map.put("title", e.getTitle());
            map.put("abilities", abilityText.get(e.getId()) != null ? abilityText.get(e.getId()) : "");
            String updateText = generateAlbumUpdateTimeText(e.getUpdateDateList());
            map.put("update_time_text", updateText);
            map.put("img_url", e.getHeadImg() != null ? e.getHeadImg() : "");
            if (updateAlbumMap.get(e.getId()) != null && updateAlbumMap.get(e.getId()) != 0L) {
                map.put("is_new", Boolean.TRUE);
            } else {
                map.put("is_new", Boolean.FALSE);
            }
            if (CollectionUtils.isNotEmpty(e.getNewsRecordList())) {
                JxtNews jxtNews = finalNewsMap.get(e.getNewsRecordList().get(0).getNewsId());
                if (jxtNews != null) {
                    if (StringUtils.equals(jxtNews.generateContentType(), JxtNewsContentType.IMG_AND_TEXT.name())) {
                        return;
                    }
                    map.put("content_type", jxtNews.getJxtNewsContentType() != null ? jxtNews.getJxtNewsContentType().name() : JxtNewsContentType.IMG_AND_TEXT.name());
                }
            }
            returnList.add(map);
        });
        return returnList;
    }

    private Map<String, Object> generateAlbumDetailMap(JxtNewsAlbum jxtNewsAlbum, User user) {
        Map<String, Object> map = new HashMap<>();
        if (jxtNewsAlbum == null) {
            return map;
        }
        ParentNewsAlbumSubRecord albumSubRecordById = null;
        List<JxtNewsTag> abilityTags = generateAllAbilityTags();
        Map<String, Date> updateMap = new HashMap<>();
        if (user != null) {
            String subId = ParentNewsAlbumSubRecord.generateId(user.getId(), jxtNewsAlbum.getId());
            albumSubRecordById = jxtNewsLoaderClient.getAlbumSubRecordById(subId);
            if (albumSubRecordById != null && albumSubRecordById.getIsSub()) {
                jxtNewsServiceClient.upsertAlbumShowRecord(user.getId(), jxtNewsAlbum.getId(), new Date());
            }
            updateMap = generateUpdateNewsInAlbum(Collections.singleton(jxtNewsAlbum), user.getId());
        }
        //取这个专辑的订阅数
//        Map<String, Long> subCountMap = asyncNewsCacheService
//                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_SUB_COUNT, Collections.singleton(jxtNewsAlbum.getId()))
//                .take();
        //专辑名称
        map.put("title", jxtNewsAlbum.getTitle());
        //专辑作者
        map.put("author", jxtNewsAlbum.getAuthor());
        //专辑封面
        map.put("img_url", jxtNewsAlbum.getHeadImg());
        //专辑封面大图
        map.put("big_img_url", jxtNewsAlbum.getBigImgUrl());
        //专辑订阅数
//        Long subCount = MapUtils.getLong(subCountMap, jxtNewsAlbum.getId(), 0L);
//        map.put("sub_count", JxtNewsUtil.countFormat(subCount));
        //专辑内容简介
        map.put("content", jxtNewsAlbum.getDetail());
        //专辑是否订阅
        if (albumSubRecordById != null && albumSubRecordById.getIsSub()) {
            map.put("is_sub", Boolean.TRUE);
        } else {
            map.put("is_sub", Boolean.FALSE);
        }
        if (CollectionUtils.isNotEmpty(jxtNewsAlbum.getTagList())) {
            List<JxtNewsTag> jxtNewsTags = abilityTags.stream().filter(p -> jxtNewsAlbum.getTagList().stream().anyMatch(o -> o.equals(p.getId()))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(jxtNewsTags)) {
                StringBuilder abilities = new StringBuilder();
                for (JxtNewsTag tag : jxtNewsTags) {
                    if (jxtNewsTags.indexOf(tag) < jxtNewsTags.size() - 1) {
                        abilities.append(tag.getTagName()).append(",");
                    } else {
                        abilities.append(tag.getTagName());
                    }
                }
                map.put("abilities", SafeConverter.toString(abilities));
            }
        }
        map.put("update_date_text", generateAlbumUpdateTimeText(jxtNewsAlbum.getUpdateDateList()));
        map.put("news_list", generateAlbumNewsDetail(jxtNewsAlbum.getNewsRecordList(), updateMap));

        return map;
    }


    private List<Map<String, Object>> generateAlbumRecommendList(JxtNewsAlbum jxtNewsAlbum, User user) {
        if (jxtNewsAlbum == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        List<JxtNewsTag> abilityTags = generateAllAbilityTags();
        Set<Long> tagIds = abilityTags.stream().map(JxtNewsTag::getId).collect(Collectors.toSet());
        List<JxtNewsAlbum> recommendAlbumList = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum().stream()
                .filter(e -> SafeConverter.toBoolean(e.getFree(), Boolean.TRUE))
                .filter(e -> e.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE)
                .filter(e -> e.getJxtNewsAlbumContentType() != null && e.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT)
                .filter(e -> e.getTagList().stream().anyMatch(t -> tagIds.contains(t) && jxtNewsAlbum.getTagList().stream().anyMatch(t::equals)))
                .collect(Collectors.toList());
        List<String> subAlbumIds = new ArrayList<>();
        if (user != null) {
            Map<Long, List<ParentNewsAlbumSubRecord>> subRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(user.getId()));
            if (MapUtils.isNotEmpty(subRecordByUserId) && CollectionUtils.isNotEmpty(subRecordByUserId.get(user.getId()))) {
                subAlbumIds = subRecordByUserId.get(user.getId()).stream().map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isNotEmpty(subAlbumIds)) {
            List<String> finalSubAlbumIds = subAlbumIds;
            recommendAlbumList = recommendAlbumList.stream().filter(e -> !finalSubAlbumIds.contains(e.getId())).collect(Collectors.toList());

        }
        if (recommendAlbumList.size() >= 2) {
            recommendAlbumList = recommendAlbumList.subList(0, 2);
        } else {
            List<String> rankAlbumIds = asyncNewsCacheService.JxtNewsCacheManager_loadAlbumSubRank().take();
            rankAlbumIds = rankAlbumIds.subList(0, 2 - recommendAlbumList.size());
            Map<String, JxtNewsAlbum> rankAlbumMap = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(rankAlbumIds);
            if (MapUtils.isNotEmpty(rankAlbumMap)) {
                List<JxtNewsAlbum> rankAlbums = rankAlbumMap.values().stream().sorted(Comparator.comparingInt(rankAlbumIds::indexOf)).collect(Collectors.toList());
                recommendAlbumList.addAll(rankAlbums);
            }
        }
        List<String> finalSubAlbumIds = subAlbumIds;
        Map<String, String> abilityText = generateAlbumAbilityText(recommendAlbumList);
        recommendAlbumList.forEach(e -> {
            Map<String, Object> albumMap = new HashMap<>();
            //id
            albumMap.put("id", e.getId());
            //标题
            albumMap.put("title", e.getTitle());
            albumMap.put("img", e.getHeadImg());
            //是否订阅
            if (CollectionUtils.isNotEmpty(finalSubAlbumIds)) {
                albumMap.put("had_sub", finalSubAlbumIds.contains(e.getId()));
            }
            albumMap.put("abilities", abilityText.get(e.getId()) != null ? abilityText.get(e.getId()) : "");
            returnList.add(albumMap);
        });
        return returnList;
    }


    private List<Map<String, Object>> generateAlbumNewsDetail(List<JxtNewsAlbum.NewsRecord> newsRecords, Map<String, Date> updateMap) {
        if (CollectionUtils.isEmpty(newsRecords)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        List<String> newsIds = newsRecords.stream().sorted((o1, o2) -> Integer.compare(o2.getRank(), o1.getRank())).map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toList());
        Set<JxtNewsAlbum.NewsRecord> recordSet = new HashSet<>(newsRecords);
        Map<String, JxtNewsAlbum.NewsRecord> newsRankMap = recordSet.stream().collect(Collectors.toMap(JxtNewsAlbum.NewsRecord::getNewsId, Function.identity()));
        Map<String, JxtNews> jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);

        if (MapUtils.isNotEmpty(jxtNewsByNewsIds)) {
            List<JxtNews> jxtNewsList = jxtNewsByNewsIds.values().stream().filter(JxtNews::getOnline).sorted(Comparator.comparingInt(o -> newsIds.indexOf(o.getId()))).collect(Collectors.toList());
            jxtNewsList.forEach(e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("news_id", e.getId());
                map.put("title", e.getTitle());
                map.put("content_type", e.getJxtNewsContentType() != null ? e.getJxtNewsContentType().name() : JxtNewsContentType.IMG_AND_TEXT.name());
                map.put("play_time", StringUtils.isNotBlank(e.getPlayTime()) ? JxtNewsUtil.formatTime(SafeConverter.toInt(e.getPlayTime())) : "");
                if (MapUtils.isNotEmpty(updateMap) && updateMap.get(e.getId()) != null) {
                    map.put("online_time", DateUtils.dateToString(updateMap.get(e.getId()), DateUtils.FORMAT_SQL_DATE));
                } else {
                    map.put("online_time", e.getPushTime().after(newsRankMap.get(e.getId()).getCreateTime()) ? DateUtils.dateToString(e.getPushTime(), DateUtils.FORMAT_SQL_DATE) : DateUtils.dateToString(newsRankMap.get(e.getId()).getCreateTime(), DateUtils.FORMAT_SQL_DATE));
                }
                map.put("update_time", e.getUpdateTime());
                if (newsRankMap.get(e.getId()) != null) {
                    map.put("episode", newsRankMap.get(e.getId()).getRank());
                }
                if (MapUtils.isNotEmpty(updateMap) && updateMap.get(e.getId()) != null) {
                    map.put("is_new", Boolean.TRUE);
                } else {
                    map.put("is_new", Boolean.FALSE);
                }
                if (StringUtils.isNotBlank(e.getVideo_url())) {
                    map.put("video_url", e.getVideo_url());
                }
                if (e.getJxtNewsType() != null && e.getJxtNewsType() == JxtNewsType.BIG_IMAGE) {
                    map.put("img_url", getCdnBaseUrlStaticSharedWithSep() + "/gridfs/" + e.getCoverImgList().get(0));
                }
                returnList.add(map);
            });
        }
        return returnList;
    }

    //生成专辑的更新时间的文案
    private String generateAlbumUpdateTimeText(List<JxtNewsAlbum.AlbumUpdateDate> albumUpdateDates) {
        if (CollectionUtils.isEmpty(albumUpdateDates)) {
            return StringUtils.EMPTY;
        }
        String updateDateSeriesText = "周{0}至周{1}{2}更新";
        List<Integer> weekDays = albumUpdateDates.stream().map(JxtNewsAlbum.AlbumUpdateDate::getWeekDay).sorted(Integer::compareTo).collect(Collectors.toList());
        if (weekDays.contains(8)) {
            return "已完结";
        }
        Boolean isSerial = Boolean.FALSE;
        for (int i = 0; i < weekDays.size() - 1; i++) {
            if (weekDays.get(i + 1) - weekDays.get(i) != 1) {
                break;
            } else {
                isSerial = Boolean.TRUE;
            }
        }
        if (isSerial) {
            updateDateSeriesText = MessageFormat.format(updateDateSeriesText, generateChineseNum(weekDays.get(0)), generateChineseNum(weekDays.get(weekDays.size() - 1)), albumUpdateDates.get(0).getUpdateTime() != null ? albumUpdateDates.get(0).getUpdateTime() : "");
            return updateDateSeriesText;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("周");
            for (Integer weekDay : weekDays) {
                if (weekDay == null) {
                    return StringUtils.EMPTY;
                }
                if (weekDays.indexOf(weekDay) != weekDays.size() - 1) {
                    stringBuilder.append(generateChineseNum(weekDay)).append("、");
                } else {
                    stringBuilder.append(generateChineseNum(weekDay));
                }
            }
            if (StringUtils.isBlank(albumUpdateDates.get(0).getUpdateTime())) {
                return StringUtils.EMPTY;
            }
            stringBuilder.append(albumUpdateDates.get(0).getUpdateTime() != null ? albumUpdateDates.get(0).getUpdateTime() : "");
            stringBuilder.append("更新");
            return SafeConverter.toString(stringBuilder);
        }
    }

    private String generateChineseNum(Integer num) {
        char[] cnArr = new char[]{'一', '二', '三', '四', '五', '六', '日'};
        if (num >= cnArr.length) {
            return "";
        }
        return SafeConverter.toString(cnArr[num - 1]);
    }


    //生成tag的comment
    private List<AlbumTagCommentConfig> generateParentAlbumAbilityTagConfig(Collection<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return Collections.emptyList();
        }

        List<AlbumTagCommentConfig> parentAlbumTagCommentConfigList;

        String parentAlbumTagCommentConfigKey = "parentAlbumTagComment";
        parentAlbumTagCommentConfigList = pageBlockContentServiceClient.loadConfigList("parentAlbumAbilityTag", parentAlbumTagCommentConfigKey, AlbumTagCommentConfig.class);
        if (CollectionUtils.isEmpty(parentAlbumTagCommentConfigList)) {
            return Collections.emptyList();
        }
        parentAlbumTagCommentConfigList = parentAlbumTagCommentConfigList.stream().filter(e -> tagIds.contains(e.getTagId())).collect(Collectors.toList());
        return parentAlbumTagCommentConfigList;
    }

    private Map<String, String> generateAlbumAbilityText(Collection<JxtNewsAlbum> albums) {
        List<JxtNewsTag> jxtNewsTags = generateAllAbilityTags();
        Map<String, String> albumAbilityTagMap = new HashMap<>();
        for (JxtNewsAlbum album : albums) {
            if (CollectionUtils.isNotEmpty(album.getTagList())) {
                Set<String> tagSets = jxtNewsTags.stream().filter(p -> album.getTagList().stream().anyMatch(o -> o.equals(p.getId()))).limit(2).map(JxtNewsTag::getTagName).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(tagSets)) {
                    String abilityText = StringUtils.join(tagSets, " ");
                    albumAbilityTagMap.put(album.getId(), abilityText);
                }
            }
        }
        return albumAbilityTagMap;
    }

//    //取专辑的类型
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

    //获取家长的孩子的年级对应的tagId做过滤用
    private Set<Long> getParentClazzLevel(Collection<StudentDetail> studentDetails) {
        if (CollectionUtils.isEmpty(studentDetails)) {
            return new HashSet<>();
        }
        return studentDetails.stream().filter(studentDetail -> studentDetail != null && studentDetail.getClazzLevelAsInteger() != null && clazzLevelNewsTagMap.containsKey(studentDetail.getClazzLevelAsInteger())).map(studentDetail -> clazzLevelNewsTagMap.get(studentDetail.getClazzLevelAsInteger())).collect(Collectors.toSet());
    }

    //生成频道页应用的列表
    private List<ParentChannelAppConfig> generateChannelAppConfig() {

        String parentChannelAppConfigKey = "parentChannelAppConfig";
        List<ParentChannelAppConfig> parentChannelAppConfigList = pageBlockContentServiceClient.loadConfigList("parentChannelAppConfig", parentChannelAppConfigKey, ParentChannelAppConfig.class);
        if (CollectionUtils.isEmpty(parentChannelAppConfigList)) {
            return Collections.emptyList();
        }
        parentChannelAppConfigList = parentChannelAppConfigList.stream().filter(e -> StringUtils.equals("H5", e.getAppType())).collect(Collectors.toList());
        return parentChannelAppConfigList;
    }

    @Getter
    private enum AlbumPageTopButton {
        PROGRESS("提高", "/public/skin/parentMobile/images/app_icon/album_progress.png", "/view/mobile/parent/learning_app/detail.vpage?rel=tigao&order_refer=330088", Boolean.TRUE, Boolean.FALSE),
        PERSONAL("个性化", "/public/skin/parentMobile/images/app_icon/album_personal.png", "/view/mobile/parent/album/subscript_fit.vpage?tab=fit", Boolean.FALSE, Boolean.TRUE),
        SUBSCRIBE("订阅", "/public/skin/parentMobile/images/app_icon/album_subscribe.png", "/view/mobile/parent/album/my_subscript.vpage", Boolean.TRUE, Boolean.TRUE),
        LATEST("最新", "/public/skin/parentMobile/images/app_icon/album_latest.png", "/view/mobile/parent/album/newest.vpage", Boolean.FALSE, Boolean.TRUE);

        private final String name;
        private final String icon;
        private final String url;
        private final Boolean isNeedLogin;
        private final Boolean online;

        AlbumPageTopButton(String name, String icon, String url, Boolean isNeedLogin, Boolean online) {
            this.icon = icon;
            this.name = name;
            this.url = url;
            this.isNeedLogin = isNeedLogin;
            this.online = online;
        }
    }

    private List<Map<String, Object>> generateNewsHistoryList(List<JxtNewsReadHistory> readHistories) {
        if (CollectionUtils.isEmpty(readHistories)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        //取出历史中的资讯Id,查出所有的资讯
        Set<String> newsIds = readHistories.stream().map(JxtNewsReadHistory::getNewsId).collect(Collectors.toSet());
        Map<String, JxtNews> jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        //按MM-dd整理历史
        Map<String, List<JxtNewsReadHistory>> dateNewsMap = readHistories.stream().sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime())).collect(Collectors.groupingBy(e -> DateUtils.dateToString(e.getUpdateTime(), "MM-dd")));
        dateNewsMap.entrySet().forEach(dateNews -> {
            Map<String, Object> map = new HashMap<>();
            List<Map<String, Object>> dateNewsList = new ArrayList<>();
            dateNews.getValue().stream().sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime())).forEach(history -> {
                JxtNews jxtNews = jxtNewsByNewsIds.get(history.getNewsId());
                if (jxtNews != null) {
                    Map<String, Object> historyMap = new HashMap<>();
                    historyMap.put("news_id", jxtNews.getId());
                    historyMap.put("title", jxtNews.getTitle());
                    if (CollectionUtils.isNotEmpty(jxtNews.getCoverImgList())) {
                        historyMap.put("img_url", jxtNews.getCoverImgList().get(0));
                    }
                    if (StringUtils.isNotBlank(jxtNews.getAlbumId())) {
                        historyMap.put("album_id", jxtNews.getAlbumId());
                    }
                    historyMap.put("online", jxtNews.getOnline());
                    dateNewsList.add(historyMap);
                }
            });
            //如果这一天有历史才显示
            if (CollectionUtils.isNotEmpty(dateNewsList)) {
                map.put("view_time", dateNews.getKey());
                map.put("news_list", dateNewsList);
                returnList.add(map);
            }
        });
        return returnList;
    }


}
