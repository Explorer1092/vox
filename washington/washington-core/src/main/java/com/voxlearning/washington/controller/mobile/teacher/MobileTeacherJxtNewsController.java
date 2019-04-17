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

package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsStyleType;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsAlbum;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsBookRef;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsTeacherRecommend;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.washington.mapper.TeacherLearningAlbumConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;


/**
 * @author shiwei.liao
 * @since 2017-3-27
 */
@Controller
@Slf4j
@RequestMapping(value = "/teacherMobile/jxtnews/")
public class MobileTeacherJxtNewsController extends AbstractMobileTeacherController {

    private static final String TEACHER_LEARNING_ALBUM_CONFIG = "teacherLearningAlbumConfig";
    private String TEACHER_LEARNING_ALBUM_CONFIG_KEY = "teacherLearningAlbumConfig";
    private static final String TEACHER_LEARNING_ALBUM_TOP_CONFIG = "teacherLearningAlbumTopConfig";
    private String TEACHER_LEARNING_ALBUM_TOP_CONFIG_KEY = "teacherLearningAlbumTopConfig";

    private static final String TEACHER_RECOMMEND_COUNT_KEY = "teacher_recommend_count_";
    private static final String TEACHER_RECOMMEND_SHOW_KEY = "teacher_recommend_show_";
    private static final String TEACHER_SHARE_COUNT_KEY = "teacher_share_count_";

    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;

    @Inject private RaikouSDK raikouSDK;

    @RequestMapping(value = "top_news.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTopNews() {
        User teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long teacherId = teacher.getId();
        //涉及包班制。前端有切学科的功能。如果老师id参数不为空。则用前端的传的id
        if (getRequestLong("teacher_id") != 0) {
            teacherId = getRequestLong("teacher_id");
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        List<TeacherLearningAlbumConfig> albumConfigs = generateTeacherLearningAlbumWithConfig(teacherDetail, TEACHER_LEARNING_ALBUM_TOP_CONFIG, TEACHER_LEARNING_ALBUM_TOP_CONFIG_KEY);
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(albumConfigs)) {
            Set<String> allAlbumIds = new HashSet<>();
            albumConfigs.stream().filter(p -> CollectionUtils.isNotEmpty(p.getAlbumIds())).forEach(p -> allAlbumIds.addAll(p.getAlbumIds()));
            jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(allAlbumIds).values()
                    .stream()
                    .filter(p -> p.getOnline() != null && p.getOnline())
                    //free这个字段老数据是为null的
                    .filter(p -> p.getFree() == null || p.getFree())
                    .sorted((o1, o2) -> o2.getOnlineTime().compareTo(o1.getOnlineTime()))
                    .limit(3)
                    .forEach(p -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("album_name", p.getTitle());
                        map.put("album_id", p.getId());
                        map.put("album_img", combineCdbUrl(p.getHeadImg()));
                        mapList.add(map);
                    });
        }
        //处理一下主副账号。把副账号对应的学科和teacherId传给前端
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        List<Teacher> teacherList = teacherLoaderClient.loadTeachers(teacherIds)
                .values()
                .stream()
                .filter(p -> p.getSubject() != null)
                .sorted((o1, o2) -> o1.getSubject().getKey() - o2.getSubject().getKey())
                .collect(Collectors.toList());
        List<Map<String, Object>> teacherSubjectList = new ArrayList<>();
        teacherList.forEach(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("teacher_id", t.getId());
            map.put("subject_name", t.getSubject().getValue());
            teacherSubjectList.add(map);
        });
        return MapMessage.successMessage().add("top_news_list", mapList).add("subject_list", teacherSubjectList);
    }

    @RequestMapping(value = "daily_recommend.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getDailyRecommendNews() {
        User teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long teacherId = teacher.getId();
        //涉及包班制。前端有切学科的功能。如果老师id参数不为空。则用前端的传的id
        if (getRequestLong("teacher_id") != 0) {
            teacherId = getRequestLong("teacher_id");
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        MapMessage mapMessage = newHomeworkPartLoaderClient.getTeacherHomeworkProgress(teacherDetail.getId(), teacherDetail.getSubject());
        //老师的进度汇总
        Set<String> unitIds = new HashSet<>();
        Set<String> sectionIds = new HashSet<>();
        if (mapMessage.isSuccess()) {
            Map<Long, Map<String, Object>> progressMap = (Map) mapMessage.get("homeworkProgress");
            progressMap.values().forEach(map -> {
                if (StringUtils.isNotBlank(SafeConverter.toString(map.get("unitId")))) {
                    unitIds.add(SafeConverter.toString(map.get("unitId")));
                }
                if (StringUtils.isNotBlank(SafeConverter.toString(map.get("sectionId")))) {
                    sectionIds.add(SafeConverter.toString(map.get("sectionId")));
                }
            });
        }
        //1.从老师进度取6篇资讯
        //老师进度对应的同步内容
        Map<String, JxtNewsBookRef> newsBookRefMap = jxtNewsLoaderClient.getJxtNewsBookRefByUnitIdsOrSectionIds(unitIds, sectionIds);
        //老师推荐过的资讯id
        //涉及包班制的问题。所有推荐都存在主账号下面了。所以这里得查主账号。
        List<JxtNewsTeacherRecommend> teacherRecommendList = jxtNewsLoaderClient.getTeacherRecommendListByTeacherId(teacher.getId(), SchoolYear.newInstance().year()).getUninterruptibly();
        Set<String> teacherHadRecommendNewsIds = teacherRecommendList.stream().map(JxtNewsTeacherRecommend::getRecommendId).collect(Collectors.toSet());
        //同步内容对应的全部资讯id
        Set<String> progressNewsIds = new HashSet<>();
        newsBookRefMap.values().forEach(ref -> progressNewsIds.addAll(ref.getNewsIdList()));
        //未推荐过的同步内容资讯id
        Set<String> notRecommendProgressNewsIds = progressNewsIds.stream().filter(p -> !teacherHadRecommendNewsIds.contains(p)).collect(Collectors.toSet());
        //需要过滤针对北京上海做特殊处理
        Set<Integer> filterRegion = new HashSet<>();
        filterRegion.add(110000);
        filterRegion.add(310000);
        List<JxtNews> teacherProgressNews = jxtNewsLoaderClient.getJxtNewsByNewsIds(notRecommendProgressNewsIds).values()
                .stream()
                .filter(p -> p.getOnline() != null && p.getOnline())
                .filter(p -> !filterRegion.contains(teacherDetail.getRootRegionCode()) || filterRegion.contains(teacherDetail.getRootRegionCode()) && !(StringUtils.equals(p.getSource(), "清大百年学习网") || StringUtils.equals(p.getSource(), "爱学堂")))
                .filter(p -> p.getJxtNewsStyleType() == JxtNewsStyleType.SYNC_TEACHING_MATERIAL || p.getJxtNewsStyleType() == JxtNewsStyleType.EXTERNAL_SYNC_TEACHING_MATERIAL)
                .sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()))
                .collect(Collectors.toList());
        //只取6个
        teacherProgressNews = new ArrayList<>(teacherProgressNews.subList(0, teacherProgressNews.size() > 6 ? 6 : teacherProgressNews.size()));

        //2.从专辑里面取2篇资讯
        //先把老师可见的资讯取出来
        //同步内容为空取4个。否则。取2个
        int needCount = CollectionUtils.isEmpty(teacherProgressNews) ? 4 : 2;
        //先从缓存里面取数据
        Set<String> cacheNewsIds = washingtonCacheSystem.CBS.persistence.load(TEACHER_RECOMMEND_SHOW_KEY + teacherDetail.getId());

        List<JxtNews> cacheRecommendList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(cacheNewsIds)) {
            cacheRecommendList = jxtNewsLoaderClient.getJxtNewsByNewsIds(cacheNewsIds)
                    .values()
                    .stream()
                    .filter(p -> p.getOnline() != null && p.getOnline())
                    .filter(p -> p.getFree() == null || p.getFree())
                    .filter(p -> !teacherHadRecommendNewsIds.contains(p.getId()))
                    .collect(Collectors.toList());
        }
        if (cacheRecommendList.size() < needCount) {
            int needNewCount = needCount - cacheRecommendList.size();
            List<JxtNewsAlbum> teacherAlbumList = getLearningJxtNewsAlbum(teacherDetail);
            //把可见专辑转化成资讯id.同时过滤掉已经推荐过的id
            //还要过滤掉已缓存的数据
            Set<String> cachedRecommendIds = cacheRecommendList.stream().map(JxtNews::getId).collect(Collectors.toSet());
            Set<String> teacherNewIds = new HashSet<>();
            teacherAlbumList.forEach(p -> teacherNewIds.addAll(p.getNewsRecordList().stream().filter(e -> !teacherHadRecommendNewsIds.contains(e.getNewsId())).filter(e -> !cachedRecommendIds.contains(e.getNewsId())).map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toList())));
            List<JxtNews> jxtNewses = jxtNewsLoaderClient.getJxtNewsByNewsIds(teacherNewIds)
                    .values()
                    .stream()
                    .filter(p -> p.getOnline() != null && p.getOnline())
                    .filter(p -> p.getFree() == null || p.getFree())
                    .filter(p -> !teacherHadRecommendNewsIds.contains(p.getId()))
                    .collect(Collectors.toList());
            //打乱
            Collections.shuffle(jxtNewses);
            //汇总、去重
            if (CollectionUtils.isNotEmpty(jxtNewses)) {
                //把结果重新缓存到缓存
                cacheRecommendList.addAll(new ArrayList<>(jxtNewses.subList(0, jxtNewses.size() > needNewCount ? needNewCount : jxtNewses.size())));
            }
            Set<String> needCachedRecommendIds = cacheRecommendList.stream().map(JxtNews::getId).collect(Collectors.toSet());
            washingtonCacheSystem.CBS.persistence.set(TEACHER_RECOMMEND_SHOW_KEY + teacherDetail.getId(), 3600 * 6, needCachedRecommendIds);
        }
        if (CollectionUtils.isNotEmpty(cacheRecommendList)) {
            teacherProgressNews.addAll(cacheRecommendList);
        }
        //如果同步内容和专辑里面的资讯都取完了。就取已推荐的最近8篇
        if (CollectionUtils.isEmpty(teacherProgressNews)) {
            Set<String> needReturnHadRecommendIds = teacherRecommendList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).limit(8).map(JxtNewsTeacherRecommend::getRecommendId).collect(Collectors.toSet());
            teacherProgressNews = jxtNewsLoaderClient.getJxtNewsByNewsIds(needReturnHadRecommendIds)
                    .values()
                    .stream()
                    .filter(p -> p.getOnline() != null && p.getOnline())
                    .filter(p -> p.getFree() == null || p.getFree())
                    .collect(Collectors.toList());
        }
        teacherProgressNews = teacherProgressNews.stream().filter(p -> p.getOnline() != null && p.getOnline()).sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime())).collect(Collectors.toList());
        List<Map<String, Object>> newsInfoList = generateTeacherRecommendNewsInfoList(teacherProgressNews);
        return MapMessage.successMessage().add("jxt_news_list", newsInfoList);
    }

    @RequestMapping(value = "learning_album.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLearningAlbum() {
        User teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long teacherId = teacher.getId();
        //涉及包班制。前端有切学科的功能。如果老师id参数不为空。则用前端的传的id
        if (getRequestLong("teacher_id") != 0) {
            teacherId = getRequestLong("teacher_id");
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        List<TeacherLearningAlbumConfig> albumConfigs = generateTeacherLearningAlbumWithConfig(teacherDetail, TEACHER_LEARNING_ALBUM_CONFIG, TEACHER_LEARNING_ALBUM_CONFIG_KEY);
        Map<String, List<TeacherLearningAlbumConfig>> albumConfigMap = albumConfigs.stream().collect(Collectors.groupingBy(TeacherLearningAlbumConfig::getCategoryName));
        Map<String, List<Map<String, Object>>> categoryMap = new HashMap<>();
        for (String categoryName : albumConfigMap.keySet()) {
            List<TeacherLearningAlbumConfig> categoryList = albumConfigMap.get(categoryName);
            if (CollectionUtils.isEmpty(categoryList)) {
                continue;
            }
            //合并老师多个年级下的所有专辑
            Set<String> categoryAlbumIds = new HashSet<>();
            categoryList.stream().filter(p -> CollectionUtils.isNotEmpty(p.getAlbumIds())).forEach(p -> categoryAlbumIds.addAll(p.getAlbumIds()));
            List<Map<String, Object>> albumList = new ArrayList<>();
            categoryMap.put(categoryName, albumList);
            //处理所有专辑
            jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(categoryAlbumIds)
                    .values()
                    .stream()
                    .filter(p -> p.getOnline() != null && p.getOnline())
                    //free这个字段老数据是为null的
                    .filter(p -> p.getFree() == null || p.getFree())
                    .sorted((o1, o2) -> o2.getOnlineTime().compareTo(o1.getOnlineTime()))
                    .forEach(p -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("album_name", p.getTitle());
                        map.put("album_id", p.getId());
                        map.put("album_img", combineCdbUrl(p.getHeadImg()));
                        albumList.add(map);
                    });
        }
        return MapMessage.successMessage().add("learning_album_map", categoryMap);
    }

    @RequestMapping(value = "clazz_level_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTeacherClazzLevelList() {
        User teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //都要支持包班制 老师子账号也要查出来。。。。
        Set<Long> relTeacherIdSet = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        List<GroupTeacherMapper> teacherMappers = deprecatedGroupLoaderClient.loadTeacherGroups(relTeacherIdSet, false).values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        Set<Long> teacherClazzIds = teacherMappers.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toSet());
        Map<ClazzLevel, List<Clazz>> clazzLevelListMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(teacherClazzIds)
                .stream()
                .filter(p -> !p.isTerminalClazz())
                .collect(Collectors.groupingBy(Clazz::getClazzLevel));
        List<ClazzLevel> clazzLevelList = clazzLevelListMap.keySet().stream().sorted((o1, o2) -> o1.getLevel() - o2.getLevel()).collect(Collectors.toList());
        List<Map<String, Object>> mapList = new ArrayList<>();
        clazzLevelList.forEach(clazzLevel -> {
            Map<String, Object> map = new HashMap<>();
            map.put("clazz_level", clazzLevel.getLevel());
            map.put("level_name", clazzLevel.getDescription());
            mapList.add(map);
        });
        return MapMessage.successMessage().add("clazz_level_list", mapList);
    }

    @RequestMapping(value = "recommend.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recommendNewsToParent() {
        User teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //推荐资讯ID
        String newsId = getRequestString("news_id");
        //需要推荐到的年级
        Integer level = getRequestInt("clazz_level");
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage("资讯ID错误").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        ClazzLevel clazzLevel = ClazzLevel.parse(level);
        if (clazzLevel == null) {
            return MapMessage.errorMessage("年级错误").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(newsId);
        if (jxtNews == null || jxtNews.getOnline() == null || !jxtNews.getOnline()) {
            return MapMessage.errorMessage("您要推荐的资讯不存在").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        //要推荐的groupId
        List<GroupTeacherMapper> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(teacher.getId(), false);
        Set<Long> teacherClazzIds = teacherGroups.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toSet());
        Set<Long> matchClazzLevelIds = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(teacherClazzIds)
                .stream()
                .filter(p -> clazzLevel == p.getClazzLevel())
                .map(Clazz::getId)
                .collect(Collectors.toSet());
        Set<Long> needSendIMMessageGroupIds = teacherGroups.stream().filter(p -> matchClazzLevelIds.contains(p.getClazzId())).map(GroupTeacherMapper::getId).collect(Collectors.toSet());
        //保存推荐记录
        JxtNewsTeacherRecommend recommend = new JxtNewsTeacherRecommend();
        recommend.setId(JxtNewsTeacherRecommend.generateId(SchoolYear.newInstance().year(), teacher.getId(), level));
        recommend.setClazzLevel(level);
        recommend.setJie(SafeConverter.toString(ClassJieHelper.fromClazzLevel(clazzLevel)));
        recommend.setTeacherId(teacher.getId());
        recommend.setRecommendId(newsId);
        recommend.setGroupIds(needSendIMMessageGroupIds);
        jxtNewsServiceClient.saveJxtNewsTeacherRecommend(recommend);
//        Long count = washingtonCacheSystem.CBS.persistence.incr(TEACHER_RECOMMEND_COUNT_KEY + teacher.getId(), 1, 1, DateUtils.getCurrentToDayEndSecond());
        //推荐成功加学豆
//        Integer integralCount = 0;
//        if (count == 1) {
//            IntegralHistory history = new IntegralHistory();
//            history.setUserId(teacher.getId());
//            history.setComment("每日推荐学习资源奖励");
//            history.setUniqueKey("TEACHER-RECOMMEND:" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
//            history.setIntegral(50);
//            history.setIntegralType(IntegralType.TEACHER_RECOMMEND_NEWS_REWARD.getType());
//            MapMessage mapMessage = AtomicLockManager.getInstance()
//                    .wrapAtomic(userIntegralService)
//                    .keyPrefix("TEACHER-RECOMMEND-REWARD")
//                    .keys(teacher.getId())
//                    .proxy()
//                    .changeIntegral(history);
//            if (!mapMessage.isSuccess()) {
//                logger.error("add TEACHER-RECOMMEND-REWARD error! teacherId:{}", teacher.getId());
//            } else {
//                integralCount = 5;
//            }
//        }
        //推荐成功发班群消息
        String recommendContent = "老师推荐了一个学习资源：" + jxtNews.getTitle();

        //新的极光push
        List<String> groupTags = new LinkedList<>();
        needSendIMMessageGroupIds.forEach(p -> groupTags.add(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(p))));
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("url", "/view/mobile/teacher/information/detail" + "?newsId=" + jxtNews.getId() + "&rel=teachershare" + "&ut=" + jxtNews.getUpdateTime().getTime() + "&style_type=" + jxtNews.generateStyleType() + "&content_type=" + jxtNews.generateContentType());
        jpushExtInfo.put("studentId", "");
        jpushExtInfo.put("s", ParentAppPushType.RECOMMEND_NEWS.name());
        appMessageServiceClient.sendAppJpushMessageByTags(recommendContent, AppMessageSource.PARENT, groupTags, null, jpushExtInfo);
        return MapMessage.successMessage();
    }

    @Deprecated
    @RequestMapping(value = "share.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage shareNews() {
        User teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        String newsId = getRequestString("news_id");
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage("资讯ID错误").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
        JxtNews news = jxtNewsLoaderClient.getJxtNews(newsId);
        if (news == null) {
            return MapMessage.errorMessage("您要分享的资讯不存在").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
//        Long count = washingtonCacheSystem.CBS.persistence.incr(TEACHER_SHARE_COUNT_KEY + teacher.getId(), 1, 1, DateUtils.getCurrentToDayEndSecond());
//        if (count == 1) {
//            IntegralHistory history = new IntegralHistory();
//            history.setUserId(teacher.getId());
//            history.setComment("分享学习资源到微信QQ奖励");
//            history.setUniqueKey("TEACHER-SHARE:" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
//            history.setIntegral(20);
//            history.setIntegralType(IntegralType.TEACHER_SHARE_NEWS_QQ_REWARD.getType());
//            MapMessage mapMessage = AtomicLockManager.getInstance()
//                    .wrapAtomic(userIntegralService)
//                    .keyPrefix("TEACHER-SHARE-REWARD")
//                    .keys(teacher.getId())
//                    .proxy()
//                    .changeIntegral(history);
//            if (!mapMessage.isSuccess()) {
//                logger.error("add TEACHER-SHARE-REWARD error! teacherId:{}", teacher.getId());
//            }
//        }
        return MapMessage.successMessage();
    }

    //老师所有可见的专辑。
    private List<JxtNewsAlbum> getLearningJxtNewsAlbum(User teacher) {
        if (teacher == null) {
            return Collections.emptyList();
        }
        //老师专辑的配置
        List<TeacherLearningAlbumConfig> learningAlbumConfigs = generateTeacherLearningAlbumWithConfig(teacher, TEACHER_LEARNING_ALBUM_CONFIG, TEACHER_LEARNING_ALBUM_CONFIG_KEY);
        if (CollectionUtils.isEmpty(learningAlbumConfigs)) {
            return Collections.emptyList();
        }
        Set<String> albumIds = new HashSet<>();
        learningAlbumConfigs.forEach(p -> albumIds.addAll(p.getAlbumIds()));
        List<JxtNewsAlbum> jxtNewsAlbumList = new ArrayList<>(jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds).values());
        return jxtNewsAlbumList.stream()
                .filter(p -> p.getOnline() != null && p.getOnline())
                .filter(p -> p.getFree() == null || p.getFree())
                .collect(Collectors.toList());
    }

    //获取老师资讯的配置
    private List<TeacherLearningAlbumConfig> generateTeacherLearningAlbumWithConfig(User teacher, String configName, String configKeyName) {
        if (teacher == null) {
            return Collections.emptyList();
        }
        List<TeacherLearningAlbumConfig> configList = pageBlockContentServiceClient.loadConfigList(configName, configKeyName, TeacherLearningAlbumConfig.class);
        if (CollectionUtils.isEmpty(configList)) {
            return configList;
        }
        //老师的学科和年级
        List<GroupTeacherMapper> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(teacher.getId(), false);
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());
        Set<Subject> teacherSubjects = teacherGroups.stream().map(GroupMapper::getSubject).collect(Collectors.toSet());
        Set<ClazzLevel> teacherClazzLevels = clazzs.stream().map(Clazz::getClazzLevel).collect(Collectors.toSet());
        configList = configList.stream()
                .filter(p -> CollectionUtils.isNotEmpty(p.getAlbumIds()))
                .filter(p -> Subject.safeParse(p.getSubjectName()) != null && teacherSubjects.contains(Subject.safeParse(p.getSubjectName())))
                .filter(p -> ClazzLevel.parse(p.getClazzLevel()) != null && teacherClazzLevels.contains(ClazzLevel.parse(p.getClazzLevel())))
                .sorted((o1, o2) -> SafeConverter.toInt(o2.getRank()) - SafeConverter.toInt(o1.getRank()))
                .collect(Collectors.toList());
        return configList;
    }

    private List<Map<String, Object>> generateTeacherRecommendNewsInfoList(List<JxtNews> jxtNewsList) {
        if (CollectionUtils.isEmpty(jxtNewsList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
//        Set<String> newsIds = jxtNewsList.stream().map(JxtNews::getId).collect(Collectors.toSet());
//        Map<String, Long> readCountMap = asyncNewsCacheService.JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, newsIds).getUninterruptibly();

        jxtNewsList.forEach(news -> {
            Map<String, Object> map = new HashMap<>();
            //阅读数
//            Long readCount = SafeConverter.toLong(readCountMap.get(news.getId()));
            map.put("news_id", news.getId());
            map.put("title", news.getTitle());
            map.put("img_list", news.getCoverImgList());
            map.put("jxt_news_type", news.getJxtNewsType());
            //文章的内容类型
            if (news.getJxtNewsContentType() != null) {
                map.put("jxt_news_content_type", news.getJxtNewsContentType());
            }
            if (news.getJxtNewsType() == JxtNewsType.TEXT) {
                map.put("digest", news.getDigest());
            }
            // 内容样式
            map.put("jxt_news_style_type", news.getJxtNewsStyleType() == null ? JxtNewsStyleType.NEWS : news.getJxtNewsStyleType());
//            map.put("read_count", JxtNewsUtil.countFormat(readCount));
            //更新时间。用作cdn的时间戳
            map.put("update_time", news.getUpdateTime().getTime());
            mapList.add(map);
        });
        return mapList;
    }
}
