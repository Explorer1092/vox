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

package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.bean.ParentResData;
import com.voxlearning.athena.bean.SearchFilterType;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkPartLoaderClient;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.AbtestMapper;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.vendor.api.AsyncNewsCacheService;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.washington.athena.ParentSearchEngineServiceClient;
import com.voxlearning.washington.athena.RecommendedServiceClient;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.v1.util.JxtNewsUtil;
import com.voxlearning.washington.mapper.ParentChannelAppConfig;
import com.voxlearning.washington.mapper.ParentHotSearchWordsConfig;
import com.voxlearning.washington.support.UserAbtestLoaderClientHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2016-11-21
 */
@Controller
@Slf4j
@RequestMapping(value = "/v2/parent/jxtnews/")
public class ParentJxtNewsApiV2Controller extends AbstractParentApiController {

    private static final Set<Long> testUserId = new HashSet<>();
    private static final Map<Integer, Long> clazzLevelNewsTagMap = new HashMap<>();

    static {
        testUserId.add(211016618L);
        testUserId.add(211016627L);
        testUserId.add(29961845L);
        testUserId.add(29611802L);
        testUserId.add(29868368L);
        testUserId.add(214671934L);
        testUserId.add(214671996L);
        testUserId.add(214671979L);
        testUserId.add(214671957L);
        clazzLevelNewsTagMap.put(1, 218L);
        clazzLevelNewsTagMap.put(2, 219L);
        clazzLevelNewsTagMap.put(3, 220L);
        clazzLevelNewsTagMap.put(4, 221L);
        clazzLevelNewsTagMap.put(5, 222L);
        clazzLevelNewsTagMap.put(6, 223L);
    }

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject
    private UserAdvertisementServiceClient userAdvertisementServiceClient;

    @Inject
    private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;
    @ImportService(interfaceClass = AsyncNewsCacheService.class)
    private AsyncNewsCacheService asyncNewsCacheService;

    @Inject
    private ParentSearchEngineServiceClient parentSearchEngineServiceClient;
    @Inject
    private RecommendedServiceClient recommendedServiceClient;

    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject
    private NewHomeworkPartLoaderClient newHomeworkPartLoaderClient;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private UserAbtestLoaderClientHelper userAbtestLoaderClientHelper;

    private String parentHotSearchWordsKey = "parentHotSearchWords";
    private String parentChannelAppConfigKey = "parentChannelAppConfig";

    //频道列表
    @RequestMapping(value = "channel_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getChannelList() {
        List<JxtNewsChannel> jxtNewsChannels = jxtNewsLoaderClient.getAllOnlineJxtNewsChannels();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        List<Map<String, Object>> channelMapList = new ArrayList<>();
        Long parentId = getCurrentParentId();
        Boolean isHit = Boolean.FALSE;
        //http://wiki.17zuoye.net/pages/viewpage.action?pageId=31200560
        //需求要1.9.0以上整体去掉同步内容模块。不看灰度了。
        if (VersionUtil.checkVersionConfig(">=1.8.6", ver) && VersionUtil.checkVersionConfig("<1.9.0", ver)) {
            isHit = isGaryChannelAppUser(parentId);
        }
        //下面这个逻辑看redmine
        //http://project.17zuoye.net/redmine/issues/46364
        if (isHit || VersionUtil.checkVersionConfig("<1.9.0", ver)) {
            // 根据rank进行排序
            Collections.sort(jxtNewsChannels, (o1, o2) -> {
                long rc1 = ConversionUtils.toLong(o1.getRank());
                long rc2 = ConversionUtils.toLong(o2.getRank());
                return Long.compare(rc1, rc2);
            });
            jxtNewsChannels.forEach(p -> {
                Map<String, Object> channelMap = new HashMap<>();
                channelMap.put(RES_RESULT_TAG_ID, p.getChannelId());
                channelMap.put(RES_RESULT_TAG_NAME, p.getName());
                channelMapList.add(channelMap);
            });
        }
        //在第一个位置加一个推荐
        if (!isHit || VersionUtil.checkVersionConfig("<1.8.6", ver)) {
            Map<String, Object> recommendMap = new HashMap<>();
            recommendMap.put(RES_RESULT_TAG_ID, "");
            recommendMap.put(RES_RESULT_TAG_NAME, "推荐");
            //1.9.0以上的版本推荐是h5页面。
            recommendMap.put(RES_RESULT_TAG_URL, "/view/mobile/parent/album/index.vpage");
            channelMapList.add(0, recommendMap);
        }

        return successMessage().add(RES_RESULT_NEWS_CHANNEL_LIST, channelMapList);
    }

    /**
     * 推荐列表
     * 刷新逻辑太复杂。已经写不下了。
     * 自己看我整理的wiki吧
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=29677314
     * 刷新逻辑的原始wiki
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=29675310
     * 有关实验组和对照组的差别看这个wiki吧
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=29675335
     */

    @RequestMapping(value = "recommend_news_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getRecommendJxtNewsList() {
        //这个用来取运营当日上线的文章
        Long maxPushTime = getRequestLong(REQ_NEWS_MAX_PUSH_TIME);
        //这个用作计算每条资讯的show_time时间戳
        Long maxShowTime = getRequestLong(REQ_NEWS_MAX_SHOW_TIME);
        Long minShowTime = getRequestLong(REQ_NEWS_MIN_SHOW_TIME);
        //刷新方式
        Integer refreshType = getRequestInt(REQ_NEWS_REFRESH_TYPE);
        //推荐的资讯总条数
        Integer recommendSize = getRequestInt(REQ_NEWS_RECOMMEND_SIZE);
        //客户端是否有本地数据
        Boolean hadLocalData = getRequestBool(REQ_NEWS_HAD_LOCAL);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_NEWS_MAX_PUSH_TIME, "资讯最新发布时间");
            validateRequired(REQ_NEWS_MAX_SHOW_TIME, "资讯最新显示时间");
            validateRequired(REQ_NEWS_MIN_SHOW_TIME, "资讯最旧显示时间");
            validateRequired(REQ_NEWS_REFRESH_TYPE, "资讯列表刷新方式");
            validateRequired(REQ_NEWS_RECOMMEND_SIZE, "推荐资讯返回条数");
            validateRequired(REQ_NEWS_HAD_LOCAL, "本地资讯");
            //1.8.5以上的版本先把
            if (VersionUtil.compareVersion(ver, "1.8.5") > 0) {
                if (hasSessionKey()) {
                    validateRequest(REQ_NEWS_MAX_PUSH_TIME, REQ_NEWS_MAX_SHOW_TIME, REQ_NEWS_MIN_SHOW_TIME, REQ_NEWS_REFRESH_TYPE, REQ_NEWS_RECOMMEND_SIZE, REQ_NEWS_HAD_LOCAL, REQ_STUDENT_ID);
                } else {
                    validateRequestNoSessionKey(REQ_NEWS_MAX_PUSH_TIME, REQ_NEWS_MAX_SHOW_TIME, REQ_NEWS_MIN_SHOW_TIME, REQ_NEWS_REFRESH_TYPE, REQ_NEWS_RECOMMEND_SIZE, REQ_NEWS_HAD_LOCAL, REQ_STUDENT_ID);
                }
            } else {
                if (hasSessionKey()) {
                    validateRequest(REQ_NEWS_MAX_PUSH_TIME, REQ_NEWS_MAX_SHOW_TIME, REQ_NEWS_MIN_SHOW_TIME, REQ_NEWS_REFRESH_TYPE, REQ_NEWS_RECOMMEND_SIZE, REQ_NEWS_HAD_LOCAL);
                } else {
                    validateRequestNoSessionKey(REQ_NEWS_MAX_PUSH_TIME, REQ_NEWS_MAX_SHOW_TIME, REQ_NEWS_MIN_SHOW_TIME, REQ_NEWS_REFRESH_TYPE, REQ_NEWS_RECOMMEND_SIZE, REQ_NEWS_HAD_LOCAL);
                }
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        JxtNewsRefreshType jxtNewsRefreshType = JxtNewsRefreshType.withUnknown(refreshType);
        if (jxtNewsRefreshType == JxtNewsRefreshType.UNKNOWN) {
            return failMessage(RES_RESULT_NEWS_REFRESH_TYPE_ERROR_MSG);
        }
        User parent = getCurrentParent();
        Long parentId = parent != null ? parent.getId() : null;

        //A/B test的判断
        Boolean isTestGroup = Boolean.FALSE;
        String testId;
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            //线上实验ID
            testId = "5841433b8555ab4f81484692";
        } else {
            //测试环境实验ID
            testId = "58414c47777487844f30618e";
        }
        if (parentId != null) {
            AbtestMapper abtestMapper = userAbtestLoaderClientHelper.generateUserAbtestInfo(parentId, testId);
            //这里根据配置判断是否是实验组
            isTestGroup = abtestMapper != null && StringUtils.equalsIgnoreCase("B组", abtestMapper.getPlanName());
        }
        //测试的特殊帐号也属于实验组
        isTestGroup = isTestGroup || (parentId != null && testUserId.contains(parentId));
        //需要返回的当日运营资讯
        List<JxtNews> returnCurrentDayNews = new ArrayList<>();
        //需要返回的推荐资讯
        List<String> bigDataNewsIds = new ArrayList<>();
        //灰度
        Boolean isHitGray = Boolean.FALSE;
        if (parentId != null) {
            Set<Long> studentIds = parentLoaderClient.loadParentStudentRefs(parentId).stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
            Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
            for (StudentDetail studentDetail : studentDetails) {
                if (getGrayFunctionManagerClient().getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "17Parent", "jxtNewsRecommend", true)) {
                    isHitGray = Boolean.TRUE;
                    break;
                }
            }
        }
        //计算需要返回哪些资讯
        //1.未登录用户直接去取推荐接口的热门推荐
        //2.属于实验组。且中了灰度才走推荐接口。灰度作为一个推荐接口的总开关
        if (parentId == null) {
            bigDataNewsIds = recommendedServiceClient.getRecommendedService()
                    .getRecommendContent(null, recommendSize, jxtNewsRefreshType.getSize(), null);
        } else if (isTestGroup && isHitGray) {
            getJxtNewsListForTestGroup(jxtNewsRefreshType, maxPushTime, recommendSize, parentId, returnCurrentDayNews, bigDataNewsIds);
        } else {
            getJxtNewsListForCompareGroup(jxtNewsRefreshType, parentId, returnCurrentDayNews, hadLocalData);
        }
        //累加推荐的资讯条数
        recommendSize += CollectionUtils.isNotEmpty(bigDataNewsIds) ? bigDataNewsIds.size() : 0;
        //load资讯并根据王磊的结果做排序
        final List<String> recommendIds = bigDataNewsIds;
        List<JxtNews> bigDataNews = jxtNewsLoaderClient.getJxtNewsByNewsIds(bigDataNewsIds).values().stream().filter(p -> p.getPushTime() != null).sorted(Comparator.comparingInt(o -> recommendIds.indexOf(o.getId()))).collect(Collectors.toList());
        //按照实验组和非实验组组织不同的List排列
        List<JxtNews> returnList = generateReturnJxtNewsList(returnCurrentDayNews, bigDataNews, isTestGroup);
        //生成资讯本身的数据
        List<Map<String, Object>> returnMapList = generateNewsInfoList(returnList, "0");
        //为了客户端保存历史列表。生成一个show_time返回给客户端。
        generateShowTime(returnMapList, maxShowTime, minShowTime, jxtNewsRefreshType);
        //处理客户端刷新需要的参数maxPushTime
        if (CollectionUtils.isNotEmpty(returnCurrentDayNews)) {
            maxPushTime = returnCurrentDayNews.get(0).getPushTime().getTime();
        }
        //首次启动。可返回的资讯大于10条。加广告
        if (jxtNewsRefreshType == JxtNewsRefreshType.APP_LOAD && returnList.size() > 10) {
            String slotId = "220801";
            List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(parentId, slotId, getRequestString(REQ_SYS), ver);
            if (CollectionUtils.isNotEmpty(newAdMappers)) {
                Map<String, Object> adInfoMap = generateAdInfoMap(newAdMappers.get(0));
                if (MapUtils.isNotEmpty(adInfoMap)) {
                    returnMapList.add(2, adInfoMap);
                }
            }
        }
        //点读机应用
        List<Map<String, Object>> syncAppList = new ArrayList<>();
        if (parentId == null || parentId != 20001L) {
            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_SYNC_APP_TYPE, ParentHomeworkDynamicToNativeType.POINT_READ.name());
            map.put(RES_BOOK_ID, "");
            map.put(RES_RESULT_SYNC_APP_ICON, getCdnBaseUrlStaticSharedWithSep() + ICO_SYNC_APP_DIAN_DU_BIG);
            map.put(RES_RESULT_SYNC_APP_NAME, "小学课本点读");
            map.put(RES_RESULT_SYNC_APP_SUB_NAME, "智能复读点读机");
            syncAppList.add(map);
        }
        return successMessage().add(RES_RESULT_NEWS_LIST, returnMapList)
                .add(RES_RESULT_NEWS_RECOMMEND_SIZE, recommendSize)
                .add(RES_RESULT_NEWS_MAX_PUSH_TIME, maxPushTime)
                .add(RES_RESULT_SYNC_APP_LIST, syncAppList);
    }

    // 兼容历史tag就是channel，以后支持成channel_news_list.vpage
    @RequestMapping(value = "tag_news_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getNewsListByTag() {
        //由于第一版客户端就是用的REQ_NEWS_TAG_ID参数请求。只是我们修改了tag和channel的定义。所以这里客户端就不改了。后端直接当channelId处理即可
        Long channelId = getRequestLong(REQ_NEWS_TAG_ID);
        Integer currentPage = getRequestInt(REQ_MSG_PAGE);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_NEWS_TAG_ID, "频道ID");
            validateRequired(REQ_MSG_PAGE, "分页页码");
            //1.8.2以上判断学生ID
            if (VersionUtil.compareVersion(ver, "1.8.5") > 0) {
                if (hasSessionKey()) {
                    validateRequest(REQ_NEWS_TAG_ID, REQ_MSG_PAGE, REQ_STUDENT_ID);
                } else {
                    validateRequestNoSessionKey(REQ_NEWS_TAG_ID, REQ_MSG_PAGE, REQ_STUDENT_ID);
                }
            } else {
                if (hasSessionKey()) {
                    validateRequest(REQ_NEWS_TAG_ID, REQ_MSG_PAGE);
                } else {
                    validateRequestNoSessionKey(REQ_NEWS_TAG_ID, REQ_MSG_PAGE);
                }
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        Long parentId = parent != null ? parent.getId() : null;
        //这是频道页的普通资讯列表。过滤掉第三方同步教材的资讯
        //下面还有一个频道页的同步内容资讯列表：既包含同步内容。也包含第三方同步内容
        List<JxtNews> newsListByChannel = jxtNewsLoaderClient.getJxtNewsListByChannel(channelId)
                .stream()
                .filter(p -> p.getPushTime() != null)
                .filter(p -> !p.generateStyleType().equals(JxtNewsStyleType.EXTERNAL_SYNC_TEACHING_MATERIAL.name()))
                .sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()))
                .collect(Collectors.toList());

        // 因为广告要用的频道下面的tagId
        JxtNewsChannel jxtNewsChannel = jxtNewsLoaderClient.getJxtNewsChannelById(channelId);
        if (jxtNewsChannel == null) {
            return failMessage("频道不存在");
        }
        String slotId = "";
        if (jxtNewsChannel.getAdSlotId() != null) {
            slotId = jxtNewsChannel.getAdSlotId().toString();
        }
        List<JxtNews> jxtNewsList = getReturnListWithPageAndSize(newsListByChannel, currentPage, 10, parentId);
        List<Map<String, Object>> mapList = generateNewsInfoList(jxtNewsList, channelId.toString());
        //第一页才出现广告。且数据至少有10条才返回广告。因为这里就是10条一页。所以下面用的==
        if (currentPage == 0 && mapList.size() == 10 && StringUtils.isNotBlank(slotId)) {
            List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(parentId, slotId, getRequestString(REQ_SYS), ver);
            if (CollectionUtils.isNotEmpty(newAdMappers)) {
                Map<String, Object> adInfoMap = generateAdInfoMap(newAdMappers.get(0));
                if (MapUtils.isNotEmpty(adInfoMap)) {
                    mapList.add(2, adInfoMap);
                }
            }
        }
        List<Map<String, Object>> syncMaterialList = new ArrayList<>();
        String tabContent = StringUtils.EMPTY;
        if (currentPage == 0 && VersionUtil.checkVersionConfig(">=1.8.5", ver)) {
            Map<Integer, Subject> subjectMap = Subject.toMap();
            if (MapUtils.isNotEmpty(subjectMap)) {
                Subject subject = subjectMap.values().stream().filter(e -> StringUtils.equals(jxtNewsChannel.getName(), e.getValue())).findFirst().orElse(Subject.UNKNOWN);
                if (subject != Subject.UNKNOWN) {
                    MapMessage studentHomeworkProgress = newHomeworkPartLoaderClient.getStudentHomeworkProgress(studentId, subject, StringUtils.EMPTY);
                    if (studentHomeworkProgress.isSuccess()) {
                        String unitId = SafeConverter.toString(studentHomeworkProgress.get("unitId"), StringUtils.EMPTY);
                        String sectionId = SafeConverter.toString(studentHomeworkProgress.get("sectionId"), StringUtils.EMPTY);
                        //同步教材资讯ID
                        syncMaterialList = generateSyncMaterialList(sectionId, unitId, channelId.toString(), parentId);
                        //提示文案
                        tabContent = generateUnitAndSectionTopTab(sectionId, unitId, syncMaterialList.size());
                    }
                }
            }
        }

        List<Map<String, Object>> channelAppMapListByPage = new ArrayList<>();
        if (currentPage == 0 && VersionUtil.checkVersionConfig(">=1.8.6", ver)) {
            List<Map<String, Object>> channelAppMapList;
            Boolean isHit = isGaryChannelAppUser(parentId);
            //中灰度的显示频道应用
            if (isHit) {
                //用于频道app的左右翻页，目前一页6个
                Integer appCount = 6;
                channelAppMapList = generateChannelAppConfigByChannelId(channelId);
                if (CollectionUtils.isNotEmpty(channelAppMapList)) {
                    for (int i = 0; i <= channelAppMapList.size() / appCount; i++) {
                        Map<String, Object> appPageMap = new HashMap<>();
                        Pageable page = new PageRequest(i, appCount);
                        Page<Map<String, Object>> pageMap = PageableUtils.listToPage(channelAppMapList, page);
                        appPageMap.put(RES_CHANNEL_APP_PAGE, page.getPageNumber());
                        appPageMap.put(RES_CHANNEL_APP_PAGE_CONTENT, pageMap.getContent());
                        channelAppMapListByPage.add(appPageMap);
                    }
                }
            }
        }
        return successMessage().add(RES_RESULT_NEWS_LIST, mapList).add(RES_RESULT_CURRENT_SERVICE_TIME, System.currentTimeMillis()).add(RES_RESULT_SYNC_MATERIAL_LIST, syncMaterialList).add(RES_RESULT_CHANNEL_NOTICE_CONTENT, tabContent).add(RES_CHANNEL_APP_LIST, channelAppMapListByPage);
    }


    /**
     * 频道应用的专辑列表页
     */
    @RequestMapping(value = "tag_album_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getAlbumListByTag() {
        Long channelId = getRequestLong(REQ_NEWS_TAG_ID);
        Integer channelAppId = getRequestInt(REQ_CHANNEL_APP_ID);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_NEWS_TAG_ID, "频道ID");
            validateRequired(REQ_CHANNEL_APP_ID, "频道应用ID");
            if (hasSessionKey()) {
                validateRequest(REQ_NEWS_TAG_ID, REQ_CHANNEL_APP_ID);
            } else {
                validateRequestNoSessionKey(REQ_NEWS_TAG_ID, REQ_CHANNEL_APP_ID);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        List<Map<String, Object>> mapList = generateChannelAlbumList(channelId, channelAppId, ver);
        return successMessage().add(RES_CHANNEL_ALBUM_LIST, mapList);
    }


    /**
     * 保存用户不感兴趣的文章记录
     */
    @RequestMapping(value = "save_dislike_news.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage insertParentDislikeNewsRecord() {
        String newsId = getRequestString(REQ_JXT_NEWS_ID);
        String reason = getRequestString(REQ_JXT_NEWS_DISLIKE_REASON);
        List<Map> reasonMapList;
        try {
            validateRequest(REQ_JXT_NEWS_ID, REQ_JXT_NEWS_DISLIKE_REASON);
            reasonMapList = JsonUtils.fromJsonToList(reason.replaceAll("\n|\r|\t", "").trim(), Map.class);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        if (StringUtils.isBlank(newsId)) {
            return failMessage("未选择要操作的资讯");
        }
        JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(newsId);
        if (jxtNews == null) {
            return failMessage("选择的资讯不存在");
        }
        User parent = getCurrentParent();
        Long parentId = parent != null ? parent.getId() : 0L;
        if (parentId == 0L) {
            return failMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //先判断用户是否对这篇文章做过不感兴趣的标识
        List<ParentDislikeNewsRecord> recordList = jxtNewsLoaderClient.getParentDislikeNewsRecordByUserIdAndNewsId(parentId, newsId);
        if (CollectionUtils.isNotEmpty(recordList)) {
            return failMessage("您已经对此资讯做过操作了");
        }
        List<ParentDislikeNewsRecord> dislikeNewsRecords = generateDislikeNewsRecord(newsId, parentId, reasonMapList);
        if (CollectionUtils.isNotEmpty(dislikeNewsRecords)) {
            jxtNewsServiceClient.insertParentDislikeNewsRecord(dislikeNewsRecords);
        }
        return successMessage();
    }


    /**
     * 收藏资讯
     */
    @RequestMapping(value = "collect_news.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage collectNews() {
        String newsId = getRequestString(REQ_JXT_NEWS_ID);
        User parent = getCurrentParent();

        if (parent == null) {
            return failMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(newsId)) {
            return failMessage("资讯ID不能为空");
        }
        JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(newsId);
        if (jxtNews == null) {
            return failMessage("您要收藏的资讯不存在");
        }
//        if (StringUtils.equals(jxtNews.generateContentType(), JxtNewsContentType.OFFICIAL_ACCOUNT.name())) {
//            return failMessage("暂不支持收藏公众号文章");
//        }
        String userId = String.valueOf(parent.getId());
        List<String> jxtNewsCollectionList;
        JxtNewsCollection jxtNewsCollection = jxtNewsLoaderClient.loadCollectionRecord(userId);
        if (jxtNewsCollection == null || jxtNewsCollection.getColNewsIds() == null) {
            jxtNewsCollectionList = new ArrayList<>();
        } else {
            jxtNewsCollectionList = new ArrayList<>(jxtNewsCollection.getColNewsIds());
        }
        jxtNewsCollectionList.add(newsId);
        MapMessage mapMessage = jxtNewsServiceClient.collectNews(userId, jxtNewsCollectionList);
        if (mapMessage.isSuccess()) {
//            vendorCacheClient.getParentJxtCacheManager().incrCollectCount(newsId);
            asyncNewsCacheService
                    .JxtNewsCacheManager_incrCacheCount(JxtNewsCacheType.JXT_NEWS_COLLECTED_COUNT, newsId)
                    .awaitUninterruptibly();
        }
        return successMessage("收藏成功");
    }


    /**
     * 资讯搜索
     */
    @RequestMapping(value = "search_news.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchNews() {
        String keyWord = getRequestString(REQ_JXT_NEWS_SEARCH_KEY_WORD);
        Integer currentPage = getRequestInt(REQ_MSG_PAGE);
        User parent = getCurrentParent();
        Long parentId = parent != null ? parent.getId() : 0L;
        try {
            validateRequired(REQ_JXT_NEWS_SEARCH_KEY_WORD, "搜索关键字");
            validateRequired(REQ_MSG_PAGE, "分页页码");
            if (hasSessionKey()) {
                validateRequest(REQ_JXT_NEWS_SEARCH_KEY_WORD, REQ_MSG_PAGE);
            } else {
                validateRequestNoSessionKey(REQ_JXT_NEWS_SEARCH_KEY_WORD, REQ_MSG_PAGE);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        MapMessage mapMessage = successMessage();
        Map<Integer, List<ParentResData>> searchMap;
        /*
         * searchMap的key是搜索出来的结果的类型：1、资讯，2、资讯标签，3、机构，4、机构标签
         * 最后一个参数是资讯类型,目前大数据不解析,传一个值占位(用来支持以后的图文、音频、视频的搜索)
         * */
        try {
            searchMap = parentSearchEngineServiceClient.getParentSearchEngineService()
                    .textSearch(keyWord, currentPage * 10, 10, 1);
        } catch (Exception e) {
            logger.error("Failed to load search data" + e);
            return failMessage("加载搜索数据失败");
        }
        if (MapUtils.isNotEmpty(searchMap)) {
            List<ParentResData> searchData = searchMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            List<String> newsIds = searchData.stream().filter(p -> StringUtils.isNotBlank(p.newsId)).sorted((o1, o2) -> Double.compare(o2.score, o1.score)).map(pd -> pd.newsId).collect(Collectors.toList());
            Map<String, JxtNews> jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
            List<JxtNews> searchNewsList = jxtNewsByNewsIds.values().stream().collect(Collectors.toList());
            List<Map<String, Object>> searchMapList = generateNewsInfoList(searchNewsList, "search");
            if (currentPage <= 2) {
                List<Map<String, Object>> tagMapList = generateRandomJxtNewsTagMapList(8);
                mapMessage.add(RES_RESULT_NEWS_TAG_LIST, tagMapList);
            }
            mapMessage.add(RES_RESULT_NEWS_LIST, searchMapList);
        } else if (currentPage == 0 && MapUtils.isEmpty(searchMap)) {
            //搜索结果为空的时候，返回最近一次的精选资讯
            List<String> pushNewsIds = generateRecentPushNewsIds(parentId);
            if (CollectionUtils.isNotEmpty(pushNewsIds)) {
                List<Map<String, Object>> pushNewsMapList = generatePushRecordMapList(pushNewsIds, parentId);
                mapMessage.add(RES_RESULT_SEARCH_EMPTY_LIST, pushNewsMapList);
            }
        }
        return mapMessage;
    }


    /**
     * 资讯搜索V1.1-JZT-1.8.0
     */
    @RequestMapping(value = "search_news_v1.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchNewsV1() {
        String keyWord = getRequestString(REQ_JXT_NEWS_SEARCH_KEY_WORD);
        Integer contentType = getRequestInt(REQ_JXT_NEWS_SEARCH_CONTENT_TYPE, -1);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        Integer source = getRequestInt(REQ_JXT_NEWS_SEARCH_SOURCE);
        String searchJson = getRequestString(REQ_JXT_NEWS_SEARCH_JSON);
        User parent = getCurrentParent();
        Long parentId = parent != null ? parent.getId() : 0L;
        try {
            validateRequired(REQ_JXT_NEWS_SEARCH_KEY_WORD, "搜索关键字");
            validateRequired(REQ_JXT_NEWS_SEARCH_CONTENT_TYPE, "搜索内容类型");
            //恶心的版本判断
            if (VersionUtil.compareVersion(ver, "1.8.5") > 0) {
                //这个是如果不是从作业报告过来的就需要默认的sid来取最新进度。1.8.5直接先加上了。搜索开放同步内容过后这个版本就可以直接支持了。
                //郁闷了。为了支持有没有孩子的家长都能使用这个接口。sid在这里成了不是必须有的。但是又必须得sig了。
//                validateRequired(REQ_STUDENT_ID, "学生ID");
                validateRequired(REQ_JXT_NEWS_SEARCH_SOURCE, "搜索来源");
                //第一版原生这个参数有key .value是空的。所以不校验。只校验sig
//                validateRequired(REQ_JXT_NEWS_SEARCH_JSON, "搜索参数");
                if (hasSessionKey()) {
                    validateRequest(REQ_JXT_NEWS_SEARCH_KEY_WORD, REQ_JXT_NEWS_SEARCH_CONTENT_TYPE, REQ_STUDENT_ID, REQ_JXT_NEWS_SEARCH_SOURCE, REQ_JXT_NEWS_SEARCH_JSON);
                } else {
                    validateRequestNoSessionKey(REQ_JXT_NEWS_SEARCH_KEY_WORD, REQ_JXT_NEWS_SEARCH_CONTENT_TYPE, REQ_STUDENT_ID, REQ_JXT_NEWS_SEARCH_SOURCE, REQ_JXT_NEWS_SEARCH_JSON);
                }
            } else {
                if (hasSessionKey()) {
                    validateRequest(REQ_JXT_NEWS_SEARCH_KEY_WORD, REQ_JXT_NEWS_SEARCH_CONTENT_TYPE);
                } else {
                    validateRequestNoSessionKey(REQ_JXT_NEWS_SEARCH_KEY_WORD, REQ_JXT_NEWS_SEARCH_CONTENT_TYPE);
                }
            }

        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        MapMessage mapMessage = successMessage();
        JxtNewsSearchSource searchSource = JxtNewsSearchSource.of(source);
        //1.8.2以上的版本从作业报告进来的。只出同步教材内容
        if (VersionUtil.compareVersion(ver, "1.8.2") > 0 && searchSource == JxtNewsSearchSource.HOMEWORK_REPORT) {
            Map<String, Object> searchJsonMap = JsonUtils.fromJson(searchJson);
            //这俩参数是通过h5给客户端。客户端再带过来的。所以没有写constants了
//            String bookId = SafeConverter.toString(searchJsonMap.get("book_id"));
            String unitId = SafeConverter.toString(searchJsonMap.get("unit_id"));
            String sectionId = SafeConverter.toString(searchJsonMap.get("section_id"));
            //同步教材进度的资讯列表
            List<Map<String, Object>> newsWithBookInfoList = generateSyncMaterialList(sectionId, unitId, "search", parentId);
            //获取教材进度的单元名称
            String noticeContent = generateUnitAndSectionTopTab(sectionId, unitId, newsWithBookInfoList.size());
            //同步应用
            List<Map<String, Object>> syncAppList = new ArrayList<>();
            if (parent == null || !Objects.equals(parent.getId(), 20001L)) {
                Map<String, Object> map = new HashMap<>();
                map.put(RES_RESULT_SYNC_APP_TYPE, ParentHomeworkDynamicToNativeType.POINT_READ.name());
                map.put(RES_BOOK_ID, "");
                map.put(RES_RESULT_SYNC_APP_ICON, getCdnBaseUrlStaticSharedWithSep() + ICO_SYNC_APP_DIAN_DU);
                map.put(RES_RESULT_SYNC_APP_NAME, "小学课本同步点读");
                map.put(RES_RESULT_SYNC_APP_SUB_NAME, "智能复读点读机");
                syncAppList.add(map);
            }
            return successMessage().add(RES_RESULT_SEARCH_CONTENT_LIST, newsWithBookInfoList).add(RES_RESULT_NEWS_SEARCH_NOTICE_CONTENT, noticeContent).add(RES_RESULT_SYNC_APP_LIST, syncAppList);
        }

        Map<Integer, List<ParentResData>> searchMap;
        /*
         * TODO:这个接口是新版搜索，目前大数据接口支持按照contentType搜索，但专辑聚合的逻辑在还在这里。后续大数据支持。
         * */
        searchMap = parentSearchEngineServiceClient.getParentSearchEngineService()
                .textSearchNew(keyWord, 0, 500, contentType != -1 ? SearchFilterType.ofWithUnKnow(contentType) : null);
        if (MapUtils.isNotEmpty(searchMap)) {
            List<ParentResData> searchData = searchMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            //聚合专辑
            searchData = generateGroupingAlbumData(searchData);
            List<ParentResData> resDataList = searchData.subList(0, searchData.size() > 120 ? 120 : searchData.size());
            List<Map<String, Object>> searchMapList = generateSearchReturnList(resDataList, contentType);
            List<Map<String, Object>> contentTypeList = new ArrayList<>();
            Long audioCount = resDataList.stream().filter(parentResData -> parentResData.contType != null && parentResData.contType.equals(ParentResData.ContentType.AUDIO)).count();
            Long videoCount = resDataList.stream().filter(parentResData -> parentResData.contType != null && parentResData.contType.equals(ParentResData.ContentType.VIDEO)).count();
            if (audioCount > 0) {
                Map<String, Object> audioContentTypeMap = new HashMap<>();
                audioContentTypeMap.put(RES_RESULT_SEARCH_CONTENT_TYPE, SearchFilterType.AUDIO.getIndex());
                audioContentTypeMap.put(RES_RESULT_SEARCH_CONTENT_TYPE_NAME, JxtNewsContentType.AUDIO.getDesc());
                contentTypeList.add(audioContentTypeMap);
            }
            if (videoCount > 0) {
                Map<String, Object> videoContentTypeMap = new HashMap<>();
                videoContentTypeMap.put(RES_RESULT_SEARCH_CONTENT_TYPE, SearchFilterType.VIDEO.getIndex());
                videoContentTypeMap.put(RES_RESULT_SEARCH_CONTENT_TYPE_NAME, JxtNewsContentType.VIDEO.getDesc());
                contentTypeList.add(videoContentTypeMap);
            }
            mapMessage.add(RES_RESULT_SEARCH_CONTENT_LIST, searchMapList).add(RES_RESULT_SEARCH_CONTENT_TYPE_LIST, contentTypeList);
            //非作业报告的搜索结果页面出广告
            //1.8.6以上才出
            //当搜索结果>=3才出广告
            //广告放在第3位
            if (searchSource != JxtNewsSearchSource.HOMEWORK_REPORT && searchMapList.size() >= 3 && VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "1.8.6") > 0) {
                List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                        .loadNewAdvertisementData(parentId, "220808", getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));
                if (CollectionUtils.isNotEmpty(newAdMappers)) {
                    NewAdMapper newAdMapper = newAdMappers.get(0);
                    Map<String, Object> adInfoMap = generateAdInfoMap(newAdMapper);
                    searchMapList.add(2, adInfoMap);
                }
            }
        } else if (MapUtils.isEmpty(searchMap)) {
            //搜索结果为空的时候，返回最近一次的精选资讯
            List<String> pushNewsIds = generateRecentPushNewsIds(parentId);
            if (CollectionUtils.isNotEmpty(pushNewsIds)) {
                List<Map<String, Object>> pushNewsMapList = generatePushRecordMapList(pushNewsIds, parentId);
                mapMessage.add(RES_RESULT_SEARCH_EMPTY_LIST, pushNewsMapList);
            }
        }
        return mapMessage;
    }


    /**
     * 热门搜索的标签
     */
    @RequestMapping(value = "hot_search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage hotSearch() {
        //List<Map<String, Object>> tagMapList = generateRandomJxtNewsTagMapList(8);
        List<Map<String, Object>> hotSearchWords = generateHotSearchWords(8);
        return successMessage().add(RES_RESULT_NEWS_TAG_LIST, hotSearchWords);
    }

    @RequestMapping(value = "update_local_news.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateLocalJxtNews() {
        String newsIdStr = getRequestString(REQ_JXT_NEWS_LOCAL_LIST);
        try {
            validateRequired(REQ_JXT_NEWS_LOCAL_LIST, "资讯ID");
            if (hasSessionKey()) {
                validateRequest(REQ_JXT_NEWS_LOCAL_LIST);
            } else {
                validateRequestNoSessionKey(REQ_JXT_NEWS_LOCAL_LIST);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Set<String> newsIds = new HashSet<>();
        List<Map> localNewsMapList = JsonUtils.fromJsonToList(newsIdStr, Map.class);
        if (CollectionUtils.isEmpty(localNewsMapList)) {
            return failMessage(RES_RESULT_NEWS_LOCAL_ERROR_MSG);
        }
        //客户端上传的资讯ID取出来
        localNewsMapList.forEach(map -> CollectionUtils.addIgnoreNull(newsIds, SafeConverter.toString(map.get(REQ_JXT_NEWS_ID))));
        if (CollectionUtils.isEmpty(newsIds)) {
            return failMessage(RES_RESULT_NEWS_LOCAL_ERROR_MSG);
        }
        //这些ID从库里查出来的资讯
        Map<String, JxtNews> jxtNewsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        //最新的阅读数
        Map<String, Long> newsReadMap = new HashMap<>();
        //所有需要用的tag
        Set<Long> allTagIds = new HashSet<>();
        jxtNewsMap.values().forEach(e -> allTagIds.addAll(e.getTagList()));
        Collection<JxtNewsTag> allTagList = jxtNewsLoaderClient.findTagsByIds(allTagIds).values();
        //取所有上线专辑id
        List<JxtNewsAlbum> allOnlineJxtNewsAlbum = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum();
        List<String> onlineAlbumIds = allOnlineJxtNewsAlbum.stream().map(JxtNewsAlbum::getId).collect(Collectors.toList());
        //处理更新结果
        List<Map<String, Object>> mapList = generateLocalNewsUpdateResult(localNewsMapList, jxtNewsMap, newsReadMap, allTagList, onlineAlbumIds);
        return successMessage().add(RES_RESULT_NEWS_UPDATE_LIST, mapList);
    }

    //实验组获取需要返回的运营资讯列表和推荐资讯列表

    private void getJxtNewsListForTestGroup(JxtNewsRefreshType jxtNewsRefreshType, Long maxPushTime, Integer recommendSize, Long parentId, List<JxtNews> returnCurrentDayNews, List<String> bigDataNewsIds) {
        //运营当日发布的所有资讯
        DayRange dayRange = DayRange.current();
        Date newDayStart = DateUtils.addHours(dayRange.getStartDate(), 4);
        //TODO 他们实验。实验组直接取推荐接口的数据
//        List<JxtNews> allOnlineJxtNews = jxtNewsLoaderClient.getAllOnlineJxtNews();
        List<JxtNews> allOnlineJxtNews = new ArrayList<>();
        List<JxtNews> currentDayTotalNews = allOnlineJxtNews.stream().filter(p -> p.getPushTime() != null && p.getCreateTime().after(newDayStart) && !StringUtils.equalsIgnoreCase(p.getPushUser(), "auto")).collect(Collectors.toList());
        //case A 下拉刷新和中部刷新
        if ((JxtNewsRefreshType.DOWN_GLIDE == jxtNewsRefreshType || JxtNewsRefreshType.MIDDLE_GLIDE == jxtNewsRefreshType)) {
            //下拉/中部刷新时。取maxPushTime之后最近的5条运营资讯正序+5条推荐的数据正序返回。运营数量不够。由推荐数据补。size=10条
            List<JxtNews> latestNews = currentDayTotalNews.stream().filter(p -> p.getPushTime() != null && p.getPushTime().getTime() > maxPushTime).sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime())).collect(Collectors.toList());
            //实验组要返回和运营等量的推荐的。所以取的是size/2
            //对照组优先返回运营的所以直接取size
            List<JxtNews> currentDayNews = getReturnListWithPageAndSize(latestNews, 0, jxtNewsRefreshType.getSize() / 2, parentId);
            //上面那个升序排列仅仅是用来决定取哪部分数据。下面这个降序排列才决定了在客户端的显示顺序
            currentDayNews.sort((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()));
            //加入返回列表
            if (CollectionUtils.isNotEmpty(currentDayNews)) {
                returnCurrentDayNews.addAll(currentDayNews);
            }
            //运营的数量<jxtNewsRefreshType.getSize()才取推荐的资讯
            if (jxtNewsRefreshType.getSize() > returnCurrentDayNews.size()) {
                List<String> recommendIds = recommendedServiceClient.getRecommendedService()
                        .getRecommendContent(parentId, recommendSize, jxtNewsRefreshType.getSize() - returnCurrentDayNews.size(), null);
                bigDataNewsIds.addAll(recommendIds);
            }
        } else {
            //case B 下拉刷新和APP每日第一次启动
            //根据客户端的最大pushTime来取运营资讯倒序、根据recommendSize来取推荐的资讯
            currentDayTotalNews = currentDayTotalNews.stream().filter(p -> (p.getPushTime() != null && p.getPushTime().getTime() > maxPushTime)).sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime())).collect(Collectors.toList());
            List<JxtNews> currentDayNews = getReturnListWithPageAndSize(currentDayTotalNews, 0, currentDayTotalNews.size(), parentId);
            //加入返回列表
            if (CollectionUtils.isNotEmpty(currentDayNews)) {
                returnCurrentDayNews.addAll(currentDayNews);
            }
            //每次请求最小100条。
            //故。此时此刻的运营资讯总量大于50时。取等量的推荐资讯。此时此刻的运营资讯小于50时。取jxtNewsRefreshType.getSize-currentDayTotalNews.size的推荐资讯
            Integer bigDataLength = returnCurrentDayNews.size() < (jxtNewsRefreshType.getSize() / 2) ? jxtNewsRefreshType.getSize() - returnCurrentDayNews.size() : returnCurrentDayNews.size();
            List<String> recommendIds = recommendedServiceClient.getRecommendedService()
                    .getRecommendContent(parentId, recommendSize, bigDataLength, null);
            bigDataNewsIds.addAll(recommendIds);
        }
    }

    //对照组获取需要返回的运营资讯列表
    private void getJxtNewsListForCompareGroup(JxtNewsRefreshType jxtNewsRefreshType, Long parentId, List<JxtNews> returnCurrentDayNews, Boolean hadLocalData) {
        //所有资讯
        List<JxtNews> allOnlineJxtNews = jxtNewsLoaderClient.getAllOnlineJxtNews();
        //已经展示过给该用户的资讯记录
        List<String> showedNewsIds = new ArrayList<>();
        if (parentId != null) {
            JxtNewsParentShowRecord showRecord = jxtNewsLoaderClient.getShowRecord(parentId);
            if (showRecord != null && CollectionUtils.isNotEmpty(showRecord.getShowRecordList())) {
                showedNewsIds.addAll(showRecord.getShowRecordList().stream().filter(p -> p.getExpireTime() != null).sorted((o1, o2) -> o2.getExpireTime().compareTo(o1.getExpireTime())).map(JxtNewsParentShowRecord.ShowRecord::getNewsId).collect(Collectors.toList()));
            }
        }
        //用户剩余的可显示资讯
        //过滤第三方同步教材的资讯
        List<JxtNews> canShowJxtNewsList = allOnlineJxtNews
                .stream()
                .filter(p -> p.getPushTime() != null)
                .filter(p -> !p.generateStyleType().equals(JxtNewsStyleType.EXTERNAL_SYNC_TEACHING_MATERIAL.name()))
                .filter(p -> DateUtils.dayDiff(new Date(), p.getPushTime()) <= 5)
                .filter(p -> !showedNewsIds.contains(p.getId()))
                .sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()))
                .collect(Collectors.toList());
        //这下面是正常的对照组逻辑
        List<JxtNews> returnJxtNewsList = getReturnListWithPageAndSize(canShowJxtNewsList, 0, jxtNewsRefreshType.getSize(), parentId);

        //这里是特殊处理用户在当天升级App之前已经看完了所有的资讯。升级之后资讯为空的问题。所以返回客户的展示历史。并直接返回。不重复记录
        if (CollectionUtils.isEmpty(returnJxtNewsList) && !hadLocalData) {
            Pageable showedPageRequest = new PageRequest(0, jxtNewsRefreshType.getSize());
            Page<String> showedPage = PageableUtils.listToPage(showedNewsIds, showedPageRequest);
            List<String> returnShowedIds = new ArrayList<>(showedPage.getContent());
            returnJxtNewsList = allOnlineJxtNews
                    .stream()
                    .filter(p -> returnShowedIds.contains(p.getId()))
                    .sorted((o1, o2) -> returnShowedIds.indexOf(o1.getId()) - returnShowedIds.indexOf(o2.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(returnJxtNewsList)) {
                returnCurrentDayNews.addAll(returnJxtNewsList);
            }
            return;
        }
        //这是当客户端有本地数据或者有可以看见的资讯时的逻辑
        if (CollectionUtils.isNotEmpty(returnJxtNewsList)) {
            returnCurrentDayNews.addAll(returnJxtNewsList);
            if (parentId != null) {
                //记录已经展示过的资讯id
                //用户存showRecord的IDList需要与返回给前端的List顺序完全倒序
                List<String> needRecordIds = returnJxtNewsList.stream().map(JxtNews::getId).collect(Collectors.toList());
                Object[] array = needRecordIds.toArray();
                CollectionUtils.reverseArray(array);
                List<Object> hadReadIdList = Arrays.asList(array);
                List<String> readIds = new ArrayList<>();
                hadReadIdList.stream().filter(p -> !readIds.contains(SafeConverter.toString(p))).forEach(p -> readIds.add(SafeConverter.toString(p)));
                jxtNewsServiceClient.addShowRecord(parentId, readIds);
            }
        }
    }

    //根据实验组和对照组组织不同的List
    private List<JxtNews> generateReturnJxtNewsList(List<JxtNews> returnCurrentDayNews, List<JxtNews> bigDataNews, Boolean isTestGroup) {
        List<JxtNews> returnList = new ArrayList<>();
        //只有一种资讯。直接返回
        if (CollectionUtils.isEmpty(returnCurrentDayNews) || CollectionUtils.isEmpty(bigDataNews)) {
            if (CollectionUtils.isNotEmpty(returnCurrentDayNews)) {
                return returnCurrentDayNews;
            }
            if (CollectionUtils.isNotEmpty(bigDataNews)) {
                return bigDataNews;
            }
            //两个都为空的时候直接返回一个空list
            return returnList;
        }
        //根据实验组和对照组组织List
        if (isTestGroup) {
            if (CollectionUtils.isNotEmpty(returnCurrentDayNews)) {
                //运营的资讯数量一定是<=推荐的数量的。所以直接循环运营的资讯的次数即可.
                for (int index = 0; index < returnCurrentDayNews.size(); index++) {
                    //加入运营资讯
                    returnList.add(returnCurrentDayNews.get(index));
                    //加入推荐资讯
                    if (bigDataNews.size() > index) {
                        returnList.add(bigDataNews.get(index));
                    }
                }
            }
            if (bigDataNews.size() > returnCurrentDayNews.size()) {
                returnList.addAll(bigDataNews.subList(returnCurrentDayNews.size(), bigDataNews.size()));
            }
        } else {
            returnList.addAll(returnCurrentDayNews);
            returnList.addAll(bigDataNews);
        }
        return returnList;
    }

    //获取需要返回的列表
    private List<JxtNews> getReturnListWithPageAndSize(List<JxtNews> jxtNewsList, int page, int size, Long parentId) {
        if (CollectionUtils.isEmpty(jxtNewsList)) {
            return Collections.emptyList();
        }
        //获取家长region
        Set<Long> studentIds = studentLoaderClient.loadParentStudents(parentId).stream().map(User::getId).collect(Collectors.toSet());
        Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
        Set<Integer> parentRegionIds = getParentRegionCode(studentDetails);
        Set<Long> parentClazzLevels = getParentClazzLevel(studentDetails);
        List<JxtNews> newsList = jxtNewsList.stream()
                .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                        || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && parentId != null && parentId.equals(p.getAvailableUserId()))
                        || (p.generatePushType().equals(JxtNewsPushType.REGION.getType()) && parentRegionIds.stream().anyMatch(e -> p.getRegionCodeList().contains(e))))
                .filter(e -> e.getJxtNewsType() != JxtNewsType.UNKNOWN)
                //这个过滤是说:要么资讯的tag不带年级。要么tag的年级包含家长孩子的年级
                .filter(p -> p.getTagList().stream().allMatch(e -> !clazzLevelNewsTagMap.values().contains(e)) || p.getTagList().stream().anyMatch(parentClazzLevels::contains))
                .sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()))
                .collect(Collectors.toList());
        Pageable pageRequest = new PageRequest(page, size);
        Page<JxtNews> jxtNewsPage = PageableUtils.listToPage(newsList, pageRequest);
        return new ArrayList<>(jxtNewsPage.getContent());
    }

    //生成列表中每条资讯需要的数据
    private List<Map<String, Object>> generateNewsInfoList(List<JxtNews> jxtNewsList, String channelId) {
        if (CollectionUtils.isEmpty(jxtNewsList)) {
            return Collections.emptyList();
        }
        Set<String> newsIds = jxtNewsList.stream().map(JxtNews::getId).collect(Collectors.toSet());
        Map<String, Long> newsReadMap = new HashMap<>();
        Set<Long> allTagIds = new HashSet<>();
        jxtNewsList.forEach(e -> allTagIds.addAll(e.getTagList()));
        Collection<JxtNewsTag> allTagList = jxtNewsLoaderClient.findTagsByIds(allTagIds).values();
        //取所有上线专辑，取出id
        List<JxtNewsAlbum> allOnlineJxtNewsAlbum = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum();
        List<String> onlineAlbumIds = allOnlineJxtNewsAlbum.stream().map(JxtNewsAlbum::getId).collect(Collectors.toList());
        List<Map<String, Object>> mapList = new ArrayList<>();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        for (JxtNews jxtNews : jxtNewsList) {
            Map<String, Object> map = generateNewsInfoMap(jxtNews, newsReadMap, allTagList, onlineAlbumIds, channelId, ver);
            mapList.add(map);
        }
        return mapList;
    }

    //列表页每条资讯的数据结构
    private Map<String, Object> generateNewsInfoMap(JxtNews jxtNews, Map<String, Long> newsReadMap, Collection<JxtNewsTag> allTagList, List<String> onlineAlbumIds, String channelId, String ver) {
        String pmcImgHost = ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host");
        String pmcImgParam;
        if (jxtNews.getJxtNewsType() == JxtNewsType.BIG_IMAGE) {
            pmcImgParam = "?x-oss-process=image/resize,m_lfit,h_360,w_670/format,png";
        } else {
            pmcImgParam = "?x-oss-process=image/resize,m_lfit,h_210,w_315/format,png";
        }
        Map<String, Object> map = new HashMap<>();
        //封面图片
        List<String> imgList = jxtNews.getCoverImgList();
        List<String> imgUrlList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(imgList)) {
            imgList.forEach(p -> imgUrlList.add(pmcImgHost + p + pmcImgParam));
        }
        //资讯TAG
        List<Long> tagIds = jxtNews.getTagList();
        List<JxtNewsTag> tagList = allTagList.stream().filter(p -> tagIds.contains(p.getId())).collect(Collectors.toList());
        List<Map<String, Object>> tagMapList;
        //搜索结果列表不需要这个东西
        if ("search".equals(channelId)) {
            tagMapList = new ArrayList<>();
        } else {
            tagMapList = generateTagMapList(tagList);
        }
        map.put(RES_RESULT_NEWS_IS_AD, Boolean.FALSE);
        //id
        map.put(RES_RESULT_NEWS_ID, jxtNews.getId());
        //标题
        map.put(RES_RESULT_NEWS_TITLE, jxtNews.getTitle());
        //摘要
        if (jxtNews.getJxtNewsType() == JxtNewsType.TEXT) {
            map.put(RES_RESULT_NEWS_DIGEST, jxtNews.getDigest());
        }
        //资讯类型
        map.put(RES_RESULT_NEWS_TYPE, jxtNews.getJxtNewsType().getType());
        //头图列表
        map.put(RES_RESULT_NEWS_IMG_LIST, imgUrlList);

        //文章的内容类型
        map.put(RES_RESULT_NEWS_CONTENT_TYPE, JxtNewsContentType.parse(jxtNews.generateContentType()).getType());
        //动态icon标签
        JxtNewsNativeLabel label = JxtNewsUtil.generateLabel(jxtNews, onlineAlbumIds);
        map.put(RES_RESULT_NEWS_LABEL_NAME, label.getLabelName());
        //标签字体颜色
        map.put(RES_RESULT_NEWS_LABEL_COLOR, label.getColor());
        //标签背景颜色
        map.put(RES_RESULT_NEWS_LABEL_BACK_COLOR, label.getBackgroundColor());

        //这个tag客户端拿去拼不感兴趣的选择界面用的。
        map.put(RES_RESULT_NEWS_TAG_LIST, tagMapList);
//        map.put(RES_RESULT_NEWS_READ_COUNT, JxtNewsUtil.countFormat(SafeConverter.toLong(newsReadMap.get(jxtNews.getId()))));
        map.put(RES_RESULT_NEWS_READ_COUNT, "推荐");
        map.put(RES_RESULT_NEWS_SOURCE, jxtNews.getSource());
        //更新时间。用作cdn的时间戳
        map.put(RES_RESULT_NEWS_UPDATE_TIME, jxtNews.getUpdateTime().getTime());
        //资讯的上线时间
        map.put(RES_RESULT_NEWS_PUSH_TIME, jxtNews.getPushTime().getTime());
        //服务器时间。客户端删除历史数据时使用
        map.put(RES_RESULT_CURRENT_SERVICE_TIME, System.currentTimeMillis());
        //资讯详情页的view地址
        Boolean isNewWebView = Boolean.FALSE;
        if (jxtNews.getJxtNewsContentType() != null && (jxtNews.getJxtNewsContentType().equals(JxtNewsContentType.AUDIO) || jxtNews.getJxtNewsContentType().equals(JxtNewsContentType.VIDEO)) && StringUtils.isNotBlank(jxtNews.getAlbumId()) && VersionUtil.checkVersionConfig(">=1.9.0", ver)) {
            ParentResData.ContentType contentType;
            switch (jxtNews.getJxtNewsContentType()) {
                case AUDIO:
                    contentType = ParentResData.ContentType.AUDIO;
                    isNewWebView = Boolean.TRUE;
                    break;
                case VIDEO:
                    contentType = ParentResData.ContentType.VIDEO;
                    isNewWebView = Boolean.TRUE;
                    break;
                default:
                    contentType = ParentResData.ContentType.IMGANDTEXT;
            }
            map.put(RES_RESULT_NEWS_NEWS_VIEW_URL, JxtNewsUtil.generateAlbumDetailView(jxtNews.getAlbumId(), channelId, contentType, jxtNews.getId(), ver));
            map.put(RES_RESULT_IS_VIDEO_WEB_VIEW, isNewWebView);
        } else {
            map.put(RES_RESULT_IS_VIDEO_WEB_VIEW, Boolean.FALSE);
            map.put(RES_RESULT_NEWS_NEWS_VIEW_URL, JxtNewsUtil.generateJxtNewsDetailView(jxtNews, "list_" + channelId, ver));
        }

        map.put(RES_RESULT_JXT_NEWS_LIST_SHOW_TYPE, JxtNewsListShowType.NEWS.getType());
        return map;
    }

    //把广告按照列表的数据结构返回
    private Map<String, Object> generateAdInfoMap(NewAdMapper newAdMapper) {
        if (newAdMapper == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>();
        map.put(RES_RESULT_NEWS_IS_AD, Boolean.TRUE);
//        map.put(RES_RESULT_AD_ID, newAdMapper.getId());
        //标题
        map.put(RES_RESULT_NEWS_TITLE, newAdMapper.getContent());
        //图片
        //为了跟资讯参数保持一致。这里用资讯的参数
        map.put(RES_RESULT_NEWS_IMG_LIST, Collections.singletonList(combineCdbUrl(newAdMapper.getImg())));

        String link = AdvertiseRedirectUtils.redirectUrl(newAdMapper.getId(), 0, getRequestString(REQ_APP_NATIVE_VERSION), getRequestString(REQ_SYS), "", 0L);
        //广告的跳转地址
        map.put(RES_RESULT_AD_URL, link);
        //动态icon的名称
        map.put(RES_RESULT_NEWS_LABEL_NAME, JxtNewsNativeLabel.AD.getLabelName());
        //标签字体颜色
        map.put(RES_RESULT_NEWS_LABEL_COLOR, JxtNewsNativeLabel.AD.getColor());
        //标签背景颜色
        map.put(RES_RESULT_NEWS_LABEL_BACK_COLOR, JxtNewsNativeLabel.AD.getBackgroundColor());
        return map;
    }

    //组织show_time
    private void generateShowTime(List<Map<String, Object>> jxtNewsMapList, Long maxShowTime, Long minShowTime, JxtNewsRefreshType refreshType) {
        if (CollectionUtils.isEmpty(jxtNewsMapList)) {
            return;
        }
        if (maxShowTime == 0) {
            maxShowTime = SafeConverter.toLong(System.currentTimeMillis() + "000");
        }
        if (minShowTime == 0) {
            minShowTime = SafeConverter.toLong(System.currentTimeMillis() + "000");
        }
        //在屏幕最上方的资讯的showTime最大。
        //jxtNewsMapList里面运营的资讯已经是按照pushTime倒序排列了。不需要再处理了
        if (refreshType == JxtNewsRefreshType.DOWN_GLIDE || refreshType == JxtNewsRefreshType.MIDDLE_GLIDE) {
            for (int index = 0; index < jxtNewsMapList.size(); index++) {
                //第一条的showTime最大。比请求的maxShowTime参数还要大
                long showTime = maxShowTime + (jxtNewsMapList.size() - index);
                jxtNewsMapList.get(index).put(RES_RESULT_NEWS_SHOW_TIME, showTime);
            }
        } else {
            for (int index = 0; index < jxtNewsMapList.size(); index++) {
                //第一条的showTime最大。但比请求的minShowTime(或者每日首次请求时根据时间戳算出来的minShowTime)要小
                //第一个元素index=0.所以必须是-(index+1).否则就与客户端已有的minShowTime重复了。
                long showTime = minShowTime - (index + 1);
                jxtNewsMapList.get(index).put(RES_RESULT_NEWS_SHOW_TIME, showTime);
            }
        }
    }


    //生成通用的tagList
    private List<Map<String, Object>> generateTagMapList(Collection<JxtNewsTag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> tagMapList = new ArrayList<>();
        tags.forEach(p -> {
            Map<String, Object> tagIdNameMap = new HashMap<>();
            tagIdNameMap.put(RES_RESULT_TAG_ID, p.getId());
            tagIdNameMap.put(RES_RESULT_TAG_NAME, p.getTagName());
            tagMapList.add(tagIdNameMap);
        });
        return tagMapList;
    }

    //处理客户端本地资讯的更新结果
    private List<Map<String, Object>> generateLocalNewsUpdateResult(List<Map> localNewsList, Map<String, JxtNews> latestNewsMap,
                                                                    Map<String, Long> newsReadMap, Collection<JxtNewsTag> allTagList,
                                                                    List<String> onlineAlbumIds) {
        if (CollectionUtils.isEmpty(localNewsList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        for (Map map : localNewsList) {
            Map<String, Object> updateResult = new HashMap<>();
            String newsId = SafeConverter.toString(map.get(REQ_JXT_NEWS_ID));
            Long localUpdateTime = SafeConverter.toLong(map.get(REQ_JXT_NEWS_LOCAL_UPDATE_TIME));
            //请求的资讯ID
            updateResult.put(RES_RESULT_NEWS_ID, newsId);
            if (StringUtils.isBlank(newsId) || localUpdateTime <= 0) {
                //更新结果
                updateResult.put(RES_RESULT_NEWS_UPDATE_RESULT, JxtLocalNewsUpdateResult.DELETE.getType());
                mapList.add(updateResult);
                continue;
            }
            JxtNews jxtNews = latestNewsMap.get(newsId);
            if (jxtNews == null) {
                updateResult.put(RES_RESULT_NEWS_UPDATE_RESULT, JxtLocalNewsUpdateResult.DELETE.getType());
            } else if (!jxtNews.getOnline()) {
                updateResult.put(RES_RESULT_NEWS_UPDATE_RESULT, JxtLocalNewsUpdateResult.DELETE.getType());
            } else if (jxtNews.getUpdateTime().getTime() > localUpdateTime) {
                updateResult.put(RES_RESULT_NEWS_UPDATE_RESULT, JxtLocalNewsUpdateResult.MODIFY.getType());
                //更新之后的最新资讯内容
                //只有推荐这个频道客户端有历史记录的问题。所以这里channelId默认就是0
                updateResult.put(RES_RESULT_NEWS_UPDATE_INFO, generateNewsInfoMap(jxtNews, newsReadMap, allTagList, onlineAlbumIds, "0", ver));
            } else {
                updateResult.put(RES_RESULT_NEWS_UPDATE_RESULT, JxtLocalNewsUpdateResult.UN_MODIFY.getType());
            }
            mapList.add(updateResult);
        }
        return mapList;
    }


    //随机从tagList中取tag
    private List<Map<String, Object>> generateRandomJxtNewsTagMapList(Integer tagNum) {
        List<JxtNewsTag> tagList = jxtNewsLoaderClient.findAllTagsWithoutDisabled();
        Collections.shuffle(tagList);
        if (tagNum > 0) {
            tagList = tagList.subList(0, tagList.size() > tagNum ? tagNum : tagList.size());
        } else {
            tagList = Collections.emptyList();
        }
        return generateTagMapList(tagList);
    }

    //生成最近一次的精选资讯
    private List<String> generateRecentPushNewsIds(Long parentId) {
        Set<Long> studentIds = studentLoaderClient.loadParentStudents(parentId).stream().map(User::getId).collect(Collectors.toSet());
        Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
        Set<Integer> parentRegionIds = getParentRegionCode(studentDetails);
        List<JxtNewsPushRecord> jxtNewsPushRecordList = jxtNewsLoaderClient.getAllOnlineJxtNewsPushRecord();
        //按推送类型和排序过滤出最近的一条精选推送
        JxtNewsPushRecord recentPushRecord = jxtNewsPushRecordList.stream()
                .filter(e -> CollectionUtils.isNotEmpty(e.getJxtNewsIdList()))
                .filter(e -> e.getStartTime() != null && e.getStartTime().before(new Date()))
                .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                        || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && parentId.equals(p.getAvailableUserId()))
                        || (p.generatePushType().equals(JxtNewsPushType.REGION.getType()) && parentRegionIds.stream().anyMatch(e -> p.getRegionCodeList().contains(e))))
                .sorted((o1, o2) -> o2.getStartTime().compareTo(o1.getStartTime()))
                .findFirst().orElse(null);
        List<String> pushNewsIds = new ArrayList<>();
        if (recentPushRecord != null) {
            pushNewsIds.addAll(recentPushRecord.getJxtNewsIdList());
        }
        return pushNewsIds;
    }

    //生成不感兴趣的记录
    private List<ParentDislikeNewsRecord> generateDislikeNewsRecord(String newsId, Long parentId, List<Map> reasonMapList) {
        if (StringUtils.isBlank(newsId) || parentId == 0L) {
            return Collections.emptyList();
        }
        List<ParentDislikeNewsRecord> dislikeNewsRecords = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(reasonMapList)) {
            for (Map r : reasonMapList) {
                if (ParentJxtNewsDislikeReasonType.parse(SafeConverter.toString(r.get(REQ_JXT_NEWS_DISLIKE_REASON_TYPE))) == ParentJxtNewsDislikeReasonType.UNKNOWN) {
                    continue;
                }
                ParentDislikeNewsRecord parentDislikeNewsRecord = new ParentDislikeNewsRecord();
                parentDislikeNewsRecord.setId(ParentDislikeNewsRecord.generateId(parentId, newsId));
                parentDislikeNewsRecord.setNewsId(newsId);
                parentDislikeNewsRecord.setUserId(parentId);
                parentDislikeNewsRecord.setReason(ParentJxtNewsDislikeReasonType.parse(SafeConverter.toString(r.get(REQ_JXT_NEWS_DISLIKE_REASON_TYPE))));
                parentDislikeNewsRecord.setReasonDetail(SafeConverter.toString(r.get(REQ_JXT_NEWS_DISLIKE_REASON_VALUE)));
                dislikeNewsRecords.add(parentDislikeNewsRecord);
            }
        } else {
            ParentDislikeNewsRecord dislikeNewsRecord = new ParentDislikeNewsRecord();
            dislikeNewsRecord.setId(ParentDislikeNewsRecord.generateId(parentId, newsId));
            dislikeNewsRecord.setUserId(parentId);
            dislikeNewsRecord.setNewsId(newsId);
            dislikeNewsRecords.add(dislikeNewsRecord);
        }
        return dislikeNewsRecords;
    }

    //获取家长的regionCode
    private Set<Integer> getParentRegionCode(Collection<StudentDetail> studentDetails) {
        if (CollectionUtils.isEmpty(studentDetails)) {
            return new HashSet<>();
        }
        Set<Integer> parentRegionIds = new HashSet<>();
        for (StudentDetail studentDetail : studentDetails) {
            parentRegionIds.add(studentDetail.getStudentSchoolRegionCode());
            parentRegionIds.add(studentDetail.getCityCode());
            parentRegionIds.add(studentDetail.getRootRegionCode());
        }
        return parentRegionIds;
    }

    //获取家长的孩子的年级对应的tagId做过滤用
    private Set<Long> getParentClazzLevel(Collection<StudentDetail> studentDetails) {
        if (CollectionUtils.isEmpty(studentDetails)) {
            return new HashSet<>();
        }
        return studentDetails.stream().filter(studentDetail -> studentDetail.getClazzLevelAsInteger() != null && clazzLevelNewsTagMap.containsKey(studentDetail.getClazzLevelAsInteger())).map(studentDetail -> clazzLevelNewsTagMap.get(studentDetail.getClazzLevelAsInteger())).collect(Collectors.toSet());
    }

    //专辑聚合
    private List<ParentResData> generateGroupingAlbumData(List<ParentResData> searchResult) {
        if (CollectionUtils.isEmpty(searchResult)) {
            return Collections.emptyList();
        }
        Map<String, ParentResData> resMap = new HashMap<>();
        searchResult.stream().filter(e -> StringUtils.isNotBlank(e.id)).forEach(e -> resMap.put(e.id, e));
        List<String> newsIds = searchResult.stream().filter(p -> StringUtils.isNotBlank(p.id)).sorted((o1, o2) -> Double.compare(o2.score, o1.score)).map(pd -> pd.id).collect(Collectors.toList());
        Map<String, JxtNews> jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        Map<String, List<JxtNews>> jxtNewsMapByAlbumId = jxtNewsByNewsIds.values().stream().filter(e -> StringUtils.isNotBlank(e.getAlbumId())).collect(Collectors.groupingBy(JxtNews::getAlbumId));
        if (MapUtils.isNotEmpty(jxtNewsMapByAlbumId)) {
            Map<String, JxtNewsAlbum> jxtNewsAlbumByAlbumIds = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(jxtNewsMapByAlbumId.keySet());
            List<String> albumIds = jxtNewsMapByAlbumId.entrySet().stream().filter(jxtNewsByAlbumId -> jxtNewsByAlbumId.getValue().size() >= 3).map(Map.Entry::getKey).collect(Collectors.toList());
            List<JxtNews> removeList = new ArrayList<>();
            removeList.addAll(jxtNewsMapByAlbumId.values().stream().filter(jxtNewsByAlbumId -> jxtNewsByAlbumId.size() >= 3).flatMap(Collection::stream).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(removeList)) {
                List<ParentResData> removeData = new ArrayList<>();
                removeList.forEach(e -> {
                    ParentResData parentResData = resMap.get(e.getId());
                    if (parentResData != null) {
                        removeData.add(parentResData);
                    }
                });
                searchResult.removeAll(removeData);
            }
            albumIds.forEach(e -> {
                JxtNewsAlbum jxtNewsAlbum = jxtNewsAlbumByAlbumIds.get(e);
                if (jxtNewsAlbum != null) {
                    ParentResData albumRes = new ParentResData();
                    List<String> resNewsIds = jxtNewsAlbum.getNewsRecordList().stream().filter(p -> resMap.containsKey(p.getNewsId())).map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toList());
                    List<ParentResData> albumResDataList = new ArrayList<>();
                    if (CollectionUtils.isEmpty(resNewsIds)) {
                        return;
                    }
                    resNewsIds.forEach(c -> {
                        ParentResData parentResData = resMap.get(c);
                        if (parentResData == null) {
                            return;
                        }
                        albumResDataList.add(parentResData);
                    });
                    ParentResData parentResData = albumResDataList.stream().sorted((o1, o2) -> Double.compare(o2.score, o1.score)).findFirst().orElse(null);
                    albumRes.id = jxtNewsAlbum.getId();
                    albumRes.score = parentResData != null ? parentResData.score : 0;
                    albumRes.contType = parentResData != null ? parentResData.contType : ParentResData.ContentType.IMGANDTEXT;
                    albumRes.idType = 2;
                    searchResult.add(albumRes);
                }
            });
        }
        List<ParentResData> resDataList;
        resDataList = searchResult.stream().filter(p -> StringUtils.isNotBlank(p.id)).sorted((o1, o2) -> Double.compare(o2.score, o1.score)).collect(Collectors.toList());
        return resDataList;
    }

    //通过搜索出的id来聚合、组装返回数据
    private List<Map<String, Object>> generateSearchReturnList(List<ParentResData> dataList, Integer contentType) {
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> mapList = new ArrayList<>();
        //资讯id
        List<String> newsIds = dataList.stream().filter(e -> StringUtils.isNotBlank(e.id) && e.idType == 1).map(e -> e.id).collect(Collectors.toList());
        //专辑id
        List<String> albumIds = dataList.stream().filter(e -> StringUtils.isNotBlank(e.id) && e.idType == 2).map(e -> e.id).collect(Collectors.toList());
        Map<String, JxtNews> jxtNewsByNewsIds = new HashMap<>();
        Map<String, JxtNewsAlbum> jxtNewsAlbumByAlbumIds = new HashMap<>();
        if (CollectionUtils.isNotEmpty(newsIds)) {
            jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
            if (VersionUtil.checkVersionConfig(">=1.9.0", ver) && MapUtils.isNotEmpty(jxtNewsByNewsIds)) {
                Map<String, JxtNewsAlbumType> newsAlbumTypeMap = generateAlbumNewsType(jxtNewsByNewsIds.values());
                jxtNewsByNewsIds = jxtNewsByNewsIds.values().stream().filter(e -> (StringUtils.isBlank(e.getAlbumId()) && (JxtNewsStyleType.KOL_RECOMMEND_NEWS != e.getJxtNewsStyleType() && JxtNewsStyleType.KOL_VOLUNTEER_NEWS != e.getJxtNewsStyleType())) || (newsAlbumTypeMap.get(e.getId()) != null && JxtNewsAlbumType.EXTERNAL_MIZAR != newsAlbumTypeMap.get(e.getId()))).collect(Collectors.toMap(JxtNews::getId, Function.identity()));
            }
        }
        if (CollectionUtils.isNotEmpty(albumIds)) {
            jxtNewsAlbumByAlbumIds = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds);
            if (VersionUtil.checkVersionConfig(">=1.9.0", ver) && MapUtils.isNotEmpty(jxtNewsAlbumByAlbumIds)) {
                // Map<String, JxtNewsContentType> albumType = generateAlbumType(jxtNewsAlbumByAlbumIds.values());
                jxtNewsAlbumByAlbumIds = jxtNewsAlbumByAlbumIds.values()
                        .stream()
                        .filter(e -> e.generateJxtNewsAlbumType() != null && e.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE)
                        .filter(e -> e.getFree() == null || e.getFree())
                        .filter(e -> e.getJxtNewsAlbumContentType() != null && e.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT).collect(Collectors.toMap(JxtNewsAlbum::getId, Function.identity()));
            }
        }
        //加到newsIds里一块查出来，后面取专辑的阅读数
        jxtNewsAlbumByAlbumIds.values()
                .forEach(e -> {
                    if (CollectionUtils.isNotEmpty(e.getNewsRecordList())) {
                        newsIds.addAll(e.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet()));
                    }
                });
        Map<String, Long> newsReadMap = new HashMap<>();
//        Map<String, Long> albumReadCountMap = vendorCacheClient.getParentJxtCacheManager().loadAlbumReadCount(albumIds);
        Map<String, Long> albumReadCountMap = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_READ_COUNT, albumIds)
                .take();

        //取所有上线专辑，取出id
        List<JxtNewsAlbum> allOnlineJxtNewsAlbum = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum();
        List<String> onlineAlbumIds = allOnlineJxtNewsAlbum.stream().map(JxtNewsAlbum::getId).collect(Collectors.toList());
        final Map<String, JxtNews> finalJxtNewsByNewsIds = jxtNewsByNewsIds;
        final Map<String, JxtNewsAlbum> finalJxtNewsAlbumByAlbumIds = jxtNewsAlbumByAlbumIds;
        dataList.forEach(e -> {
            if (e.idType == 1) {
                JxtNews jxtNews = finalJxtNewsByNewsIds.get(e.id);
                if (jxtNews != null) {
                    //搜索结果列表不需要是否感兴趣的功能。所以参数allTagList直接传的空
                    Map<String, Object> newsInfoMap = generateNewsInfoMap(jxtNews, newsReadMap, Collections.emptyList(), onlineAlbumIds, "search", ver);
                    mapList.add(newsInfoMap);
                }
            } else if (e.idType == 2) {
                JxtNewsAlbum jxtNewsAlbum = finalJxtNewsAlbumByAlbumIds.get(e.id);
                if (jxtNewsAlbum != null) {
                    //不需要专辑文章标题、不需要计算专辑更新时间，传一个空map
                    Map<String, Object> albumDetail = generateAlbumDetail(jxtNewsAlbum, e.contType, "search", albumReadCountMap, newsReadMap, new HashMap<>(), new HashMap<>(), ver);
                    mapList.add(albumDetail);
                }
            }
        });
        //在默认搜索时才有“大家还在搜”
        if (contentType == -1) {
            //“大家还在搜”加入指定位置
            if (mapList.size() >= 10 && mapList.size() < 20) {
                Map<String, Object> tagMap = generateSearchTagList();
                mapList.add(10, tagMap);
            } else if (mapList.size() >= 20 && mapList.size() < 30) {
                for (int i = 1; i <= 2; i++) {
                    Map<String, Object> tagMap = generateSearchTagList();
                    mapList.add(i * 10 + i - 1, tagMap);
                }
            } else if (mapList.size() >= 30) {
                for (int i = 1; i <= 3; i++) {
                    Map<String, Object> tagMap = generateSearchTagList();
                    mapList.add(i * 10 + i - 1, tagMap);
                }
            }
        }
        return mapList;
    }

    private Map<String, Object> generateAlbumDetail(JxtNewsAlbum album, Enum contentType, String channelId, Map<String, Long> albumReadCountMap, Map<String, Long> newsReadCountMap, Map<String, String> albumArticleTitleMap, Map<String, Boolean> albumUpdateDateMap, String ver) {
        String pmcImgHost = ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host");
        String pmcImgParam = "?x-oss-process=image/resize,m_lfit,h_210,w_315/format,png";
        if (album == null) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put(RES_RESULT_NEWS_ID, album.getId());
        map.put(RES_RESULT_NEWS_TITLE, album.getTitle());
        List<String> imgList = new ArrayList<>();
        if (StringUtils.isNotBlank(album.getHeadImg())) {
            imgList.add(pmcImgHost + album.getHeadImg() + pmcImgParam);
        }
        map.put(RES_RESULT_NEWS_IMG_LIST, imgList);
        map.put(RES_RESULT_NEWS_DIGEST, jxtNewsLoaderClient.removeHtml(album.getDetail()));
        map.put(RES_RESULT_NEWS_SOURCE, album.getAuthor());
//        map.put(RES_RESULT_NEWS_READ_COUNT, JxtNewsUtil.countFormat(generateAlbumReadCount(album, albumReadCountMap, newsReadCountMap)));
        map.put(RES_RESULT_NEWS_READ_COUNT, "推荐");
        //动态icon标签
        JxtNewsNativeLabel jxtNewsNativeLabel = JxtNewsUtil.generateSearchAlbumLabel(contentType);
        if (jxtNewsNativeLabel != null) {
            //标签
            map.put(RES_RESULT_NEWS_LABEL_NAME, jxtNewsNativeLabel.getLabelName());
            //标签字体颜色
            map.put(RES_RESULT_NEWS_LABEL_COLOR, jxtNewsNativeLabel.getColor());
            //标签背景颜色
            map.put(RES_RESULT_NEWS_LABEL_BACK_COLOR, jxtNewsNativeLabel.getBackgroundColor());
            if (jxtNewsNativeLabel == JxtNewsNativeLabel.AUDIO_ALBUM) {
                map.put(RES_RESULT_NEWS_CONTENT_TYPE, JxtNewsContentType.AUDIO.getType());
            } else if (jxtNewsNativeLabel == JxtNewsNativeLabel.VIDEO_ALBUM) {
                map.put(RES_RESULT_NEWS_CONTENT_TYPE, JxtNewsContentType.VIDEO.getType());
            } else if (jxtNewsNativeLabel == JxtNewsNativeLabel.IMG_AND_TEXT_ALBUM) {
                map.put(RES_RESULT_NEWS_CONTENT_TYPE, JxtNewsContentType.IMG_AND_TEXT.getType());
            }
        }
        if (VersionUtil.checkVersionConfig(">=1.9.0", ver)) {
            if ((contentType.equals(ParentResData.ContentType.AUDIO) || contentType.equals(ParentResData.ContentType.VIDEO))) {
                map.put(RES_RESULT_IS_VIDEO_WEB_VIEW, Boolean.TRUE);
            } else {
                map.put(RES_RESULT_IS_VIDEO_WEB_VIEW, Boolean.FALSE);
            }
        }
        map.put(RES_RESULT_NEWS_NEWS_VIEW_URL, JxtNewsUtil.generateAlbumDetailView(album.getId(), channelId, contentType, "", ver));
        map.put(RES_RESULT_JXT_NEWS_LIST_SHOW_TYPE, JxtNewsListShowType.ALBUM.getType());

        if (MapUtils.isNotEmpty(albumUpdateDateMap) && albumUpdateDateMap.get(album.getId()) != null) {
            map.put(RES_RESULT_ALBUM_IS_NEW, albumUpdateDateMap.get(album.getId()));
        }
        if (MapUtils.isNotEmpty(albumArticleTitleMap) && albumArticleTitleMap.get(album.getId()) != null) {
            map.put(RES_RESULT_ALBUM_FIRST_ARTICLE_TITLE, albumArticleTitleMap.get(album.getId()));
        }
        return map;
    }

    private Map<String, Object> generateSearchTagList() {
        Map<String, Object> tagMap = new HashMap<>();
        List<Map<String, Object>> tagMapList = generateRandomJxtNewsTagMapList(8);
        tagMap.put(RES_RESULT_JXT_NEWS_LIST_SHOW_TYPE, JxtNewsListShowType.SEARCH_TAG.getType());
        tagMap.put(RES_RESULT_SEARCH_TAG_LIST, tagMapList);
        return tagMap;
    }

    private List<Map<String, Object>> generatePushRecordMapList(List<String> pushNewsIds, Long parentId) {
        if (CollectionUtils.isEmpty(pushNewsIds)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> pushNewsMapList;
        Map<String, JxtNews> jxtNewsByPushRecord = jxtNewsLoaderClient.getJxtNewsByNewsIds(pushNewsIds);
        List<JxtNews> pushJxtNewsList = jxtNewsByPushRecord.values().stream().filter(e -> e.getPushTime() != null).collect(Collectors.toList());
        List<JxtNews> returnPushNewsList = getReturnListWithPageAndSize(pushJxtNewsList, 0, 5, parentId);
        pushNewsMapList = generateNewsInfoList(returnPushNewsList, "search");
        return pushNewsMapList;

    }

    //生成专辑的阅读数
    private Long generateAlbumReadCount(JxtNewsAlbum jxtNewsAlbum, Map<String, Long> albumCountMap, Map<String, Long> newsCountMap) {
        if (jxtNewsAlbum == null || MapUtils.isEmpty(albumCountMap) || MapUtils.isEmpty(newsCountMap)) {
            return 0L;
        }
        Set<String> newsIds = jxtNewsAlbum.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet());
        Long albumReadCount = SafeConverter.toLong(albumCountMap.get(jxtNewsAlbum.getId()));
        Long newsCount = 0L;
        for (String newsId : newsIds) {
            newsCount += SafeConverter.toLong(newsCountMap.get(newsId));
        }
        return albumReadCount + newsCount;
    }

    //生成热门搜索词
    private List<Map<String, Object>> generateHotSearchWords(Integer randomNum) {

        List<ParentHotSearchWordsConfig> hotSearchWordsConfigList =
                pageBlockContentServiceClient.loadConfigList("parentHotSearchWordsConfig", parentHotSearchWordsKey, ParentHotSearchWordsConfig.class);
        if (CollectionUtils.isEmpty(hotSearchWordsConfigList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> wordsList = new ArrayList<>();
        Collections.shuffle(hotSearchWordsConfigList);
        hotSearchWordsConfigList = hotSearchWordsConfigList.subList(0, hotSearchWordsConfigList.size() > randomNum ? randomNum : hotSearchWordsConfigList.size());
        hotSearchWordsConfigList.forEach(e -> {
            Map<String, Object> wordMap = new HashMap<>();
            wordMap.put(RES_RESULT_TAG_ID, e.getId());
            wordMap.put(RES_RESULT_TAG_NAME, e.getWord());
            wordsList.add(wordMap);
        });
        return wordsList;
    }

    //生成频道页的同步内容列表
    private List<Map<String, Object>> generateSyncMaterialList(String sectionId, String unitId, String channelId, Long parentId) {
        if (StringUtils.isBlank(unitId) && StringUtils.isBlank(sectionId)) {
            return Collections.emptyList();
        }
        List<String> jxtNewsIds = jxtNewsLoaderClient.getJxtNewsBookRefByUnitOrSection(unitId, sectionId);
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(jxtNewsIds)) {
            /*
             * 排序：先按字符串顺序，爱学堂 清大百年 微信公众号 作文库
             *       再按发布时间排序
             * */
            Comparator<JxtNews> jxtNewsBookRefComparator = (a, b) -> Collator.getInstance(Locale.CHINESE).compare(a.getSource(), b.getSource());
            jxtNewsBookRefComparator.thenComparing((a, b) -> b.getPushTime().compareTo(a.getPushTime()));
            Map<String, JxtNews> jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(jxtNewsIds);
            //获取家长region
            Set<Long> studentIds = studentLoaderClient.loadParentStudents(parentId).stream().map(User::getId).collect(Collectors.toSet());
            Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
            Set<Integer> parentRegionCode = getParentRegionCode(studentDetails);
            List<JxtNews> jxtNewsList = jxtNewsByNewsIds.values().stream()
                    .filter(p -> StringUtils.isNotBlank(p.getSource()) && p.getOnline())
                    .filter(p1 -> p1.generateStyleType().equals(JxtNewsStyleType.EXTERNAL_SYNC_TEACHING_MATERIAL.name()) || p1.generateStyleType().equals(JxtNewsStyleType.SYNC_TEACHING_MATERIAL.name()))
                    //北京、上海不显示清大百年和爱学堂
                    .filter(e -> CollectionUtils.isEmpty(parentRegionCode)
                            || (CollectionUtils.isNotEmpty(parentRegionCode) && !(parentRegionCode.contains(110000) || parentRegionCode.contains(310000)))
                            || (CollectionUtils.isNotEmpty(parentRegionCode) && (parentRegionCode.contains(110000) || parentRegionCode.contains(310000)) && !(StringUtils.equals(e.getSource(), "清大百年学习网") || StringUtils.equals(e.getSource(), "爱学堂"))))
                    .sorted(jxtNewsBookRefComparator)
                    .collect(Collectors.toList());
            jxtNewsList = jxtNewsList.size() <= 8 ? jxtNewsList : jxtNewsList.subList(0, 7);
            mapList = generateNewsInfoList(jxtNewsList, channelId);
        }
        return mapList;
    }

    //根据sectionId和unitId生成对应的名称，生成列表顶部的tab
    //这里加入subject是为了拼unit和section的名字，因为英语作业取名字用的是alias，但其他学科的alias为空，只能取name
    private String generateUnitAndSectionTopTab(String sectionId, String unitId, Integer newsNum) {
        if ((StringUtils.isBlank(sectionId) && StringUtils.isBlank(unitId)) || newsNum == null || newsNum <= 0) {
            return StringUtils.EMPTY;
        }
        String tabContent = StringUtils.EMPTY;
        Map<String, Object> unitAndSectionMap = new HashMap<>();
        NewBookCatalog unitBookCatalog = null;
        NewBookCatalog sectionBookCatalog = null;
        if (StringUtils.isNotBlank(sectionId)) {
            Map<String, NewBookCatalog> sectionMap = newContentLoaderClient.loadBookCatalogByCatalogIds(Collections.singleton(sectionId));
            if (MapUtils.isNotEmpty(sectionMap) && sectionMap.get(sectionId) != null) {
                sectionBookCatalog = sectionMap.get(sectionId);
            }
        }
        if (StringUtils.isNotBlank(unitId)) {
            Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(Collections.singleton(unitId));
            if (MapUtils.isNotEmpty(unitMap) && unitMap.get(unitId) != null) {
                unitBookCatalog = unitMap.get(unitId);
            }
        }
        if (unitBookCatalog != null) {
            if (unitBookCatalog.getSubjectId() == Subject.ENGLISH.getId()) {
                unitAndSectionMap.put("unit", unitBookCatalog.getAlias());
            } else {
                unitAndSectionMap.put("unit", unitBookCatalog.getName());
            }
        }
        if (sectionBookCatalog != null) {
            if (sectionBookCatalog.getSubjectId() == Subject.ENGLISH.getId()) {
                unitAndSectionMap.put("section", sectionBookCatalog.getAlias());
            } else {
                unitAndSectionMap.put("section", sectionBookCatalog.getName());
            }
        }
        if (MapUtils.isNotEmpty(unitAndSectionMap)) {
            if (StringUtils.isNotBlank(SafeConverter.toString(unitAndSectionMap.get("unit"))) || StringUtils.isNotBlank(SafeConverter.toString(unitAndSectionMap.get("section")))) {
                tabContent = "为您推荐{0} {1}同步内容{2}篇";
                tabContent = MessageFormat.format(tabContent, SafeConverter.toString(unitAndSectionMap.get("unit"), StringUtils.EMPTY), SafeConverter.toString(unitAndSectionMap.get("section"), StringUtils.EMPTY), newsNum);
            }
        }
        return tabContent;
    }

    //生成频道页应用的列表
    private List<ParentChannelAppConfig> generateChannelAppConfig() {

        List<ParentChannelAppConfig> parentChannelAppConfigList =
                pageBlockContentServiceClient.loadConfigList("parentChannelAppConfig", parentChannelAppConfigKey, ParentChannelAppConfig.class);

        if (CollectionUtils.isEmpty(parentChannelAppConfigList)) {
            return Collections.emptyList();
        }

        parentChannelAppConfigList = parentChannelAppConfigList.stream().filter(e -> StringUtils.equals("Native", e.getAppType())).collect(Collectors.toList());
        return parentChannelAppConfigList;
    }

    //按channelId生成频道应用
    private List<Map<String, Object>> generateChannelAppConfigByChannelId(Long channelId) {
        List<ParentChannelAppConfig> parentChannelAppConfigList = generateChannelAppConfig();
        List<Map<String, Object>> configList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(parentChannelAppConfigList)) {
            parentChannelAppConfigList.stream().filter(config -> channelId.equals(SafeConverter.toLong(config.getChannelId()))).sorted((o1, o2) -> Integer.valueOf(SafeConverter.toInt(o1.getAppRank())).compareTo(SafeConverter.toInt(o2.getAppRank()))).forEach(c -> {
                Map<String, Object> configMap = new HashMap<>();
                configMap.put(RES_CHANNEL_APP_ID, c.getAppId());
                configMap.put(RES_CHANNEL_APP_NAME, c.getAppName());
                String imgUrl = getCdnBaseUrlStaticSharedWithSep() + c.getImgUrl();
                configMap.put(RES_CHANNEL_APP_IMG_URL, imgUrl);
                configMap.put(RES_CHANNEL_JUMP_URL, c.getJumpUrl());
                configMap.put(RES_CHANNEL_JUMP_TYPE, c.getJumpType());
                configList.add(configMap);
            });
        }
        return configList;
    }


    private List<Map<String, Object>> generateChannelAlbumList(Long channelId, Integer channelAppId, String ver) {
        if (channelId == null || channelAppId == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        List<ParentChannelAppConfig> parentChannelAppConfigList = generateChannelAppConfig();
        if (CollectionUtils.isNotEmpty(parentChannelAppConfigList)) {
            ParentChannelAppConfig parentChannelAppConfig = parentChannelAppConfigList.stream().filter(config -> channelId.equals(SafeConverter.toLong(config.getChannelId())) && channelAppId.equals(SafeConverter.toInt(config.getAppId()))).findFirst().orElse(null);
            if (parentChannelAppConfig != null) {
                List<String> albumIds = parentChannelAppConfig.getAlbumIds();
                Map<String, JxtNewsAlbum> albumMap = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds);
                Map<String, String> albumNewsMap = new HashMap<>();
                //用来判断专辑更新的时间
                Map<String, Boolean> albumNewsUpdateTimeMap = new HashMap<>();
                //用来判断专辑的内容类型：音、视频要跳新专辑详情页，图文跳老页面
                Map<String, JxtNewsContentType> albumContentTypeMap = new HashMap<>();
                //取出专辑最新一篇文章
                if (MapUtils.isNotEmpty(albumMap)) {
                    Set<String> newsIds = new HashSet<>();
                    albumMap.values().forEach(e -> {
                        if (CollectionUtils.isNotEmpty(e.getNewsRecordList())) {
                            newsIds.addAll(e.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet()));
                        }
                    });
                    //取出所有的文章
                    Map<String, JxtNews> jxtNewsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds).values().stream().filter(JxtNews::getOnline).collect(Collectors.toMap(JxtNews::getId, e -> e));
                    //文章的阅读数和专辑的阅读数
//                    Map<String, Long> newsReadMap = asyncNewsCacheService
//                            .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, newsIds)
//                            .take();
                    Map<String, Long> newsReadMap = new HashMap<>();
                    Map<String, Long> albumReadCountMap = asyncNewsCacheService
                            .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_READ_COUNT, albumIds)
                            .take();
                    for (JxtNewsAlbum album : albumMap.values()) {
                        Map<String, Date> newsDateMap = new HashMap<>();
                        //比对文章的更新时间并取出更新时间和文章标题
                        album.getNewsRecordList().stream()
                                .filter(e -> jxtNewsMap.keySet().contains(e.getNewsId()))
                                .filter(p -> jxtNewsMap.get(p.getNewsId()) != null && jxtNewsMap.get(p.getNewsId()).getPushTime() != null)
                                .forEach(o -> {
                                    if (jxtNewsMap.get(o.getNewsId()).getPushTime().after(o.getCreateTime())) {
                                        newsDateMap.put(o.getNewsId(), jxtNewsMap.get(o.getNewsId()).getPushTime());
                                    } else {
                                        newsDateMap.put(o.getNewsId(), o.getCreateTime());
                                    }
                                });
                        Map.Entry<String, Date> entry = newsDateMap.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).findFirst().orElse(null);
                        if (entry != null && DateUtils.dayDiff(new Date(), entry.getValue()) <= 2) {
                            albumNewsUpdateTimeMap.put(album.getId(), Boolean.TRUE);
                        } else {
                            albumNewsUpdateTimeMap.put(album.getId(), Boolean.FALSE);
                        }
                        if (entry != null) {
                            albumNewsMap.put(album.getId(), jxtNewsMap.get(entry.getKey()).getTitle());
                        }
                        if (CollectionUtils.isNotEmpty(album.getNewsRecordList()) && StringUtils.isNotBlank(album.getNewsRecordList().get(0).getNewsId())) {
                            JxtNews jxtNews = jxtNewsMap.get(album.getNewsRecordList().get(0).getNewsId());
                            if (jxtNews != null) {
                                albumContentTypeMap.put(album.getId(), jxtNews.getJxtNewsContentType());
                            }
                        }
                    }
                    List<JxtNewsAlbum> albumList = albumMap.values().stream().sorted((o1, o2) -> generateAlbumReadCount(o2, albumReadCountMap, newsReadMap).compareTo(generateAlbumReadCount(o1, albumReadCountMap, newsReadMap))).collect(Collectors.toList());
                    for (JxtNewsAlbum jxtNewsAlbum : albumList) {
                        JxtNewsContentType jxtNewsContentType = albumContentTypeMap.get(jxtNewsAlbum.getId());
                        ParentResData.ContentType contentType;
                        Boolean isNewWebView = Boolean.FALSE;
                        if (jxtNewsContentType != null) {
                            switch (jxtNewsContentType) {
                                case AUDIO:
                                    contentType = ParentResData.ContentType.AUDIO;
                                    isNewWebView = Boolean.TRUE;
                                    break;
                                case VIDEO:
                                    contentType = ParentResData.ContentType.VIDEO;
                                    isNewWebView = Boolean.TRUE;
                                    break;
                                default:
                                    contentType = ParentResData.ContentType.IMGANDTEXT;
                            }
                        } else {
                            contentType = ParentResData.ContentType.IMGANDTEXT;
                        }
                        Map<String, Object> map = generateAlbumDetail(jxtNewsAlbum, contentType, SafeConverter.toString(channelId), albumReadCountMap, newsReadMap, albumNewsMap, albumNewsUpdateTimeMap, ver);
                        if (VersionUtil.checkVersionConfig(">=1.9.0", ver)) {
                            map.put(RES_RESULT_IS_VIDEO_WEB_VIEW, isNewWebView);
                        }
                        returnList.add(map);
                    }
                }

            }
        }
        return returnList;
    }


    private Boolean isGaryChannelAppUser(Long parentId) {
        Boolean isHit = Boolean.FALSE;
        //判断灰度
        if (parentId != null) {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
            if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(studentIds)) {
                    Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);
                    if (MapUtils.isNotEmpty(studentDetailMap)) {
                        isHit = studentDetailMap.values().stream().anyMatch(studentDetail -> grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ParentNewsChannel", "ChannelApp"));
                    }
                }
            }
        }
        return isHit;
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


    private Map<String, JxtNewsAlbumType> generateAlbumNewsType(Collection<JxtNews> newsCollection) {
        if (CollectionUtils.isEmpty(newsCollection)) {
            return Collections.emptyMap();
        }
        Map<String, JxtNewsAlbumType> albumTypeMap = new HashMap<>();
        Set<String> albumIds = newsCollection.stream().filter(e -> StringUtils.isNotBlank(e.getAlbumId())).map(JxtNews::getAlbumId).collect(Collectors.toSet());
        Map<String, JxtNewsAlbum> albumMap = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds);
        albumMap.values().forEach(e -> {
            albumTypeMap.put(e.getId(), e.generateJxtNewsAlbumType());
        });

        return albumTypeMap;
    }


}
