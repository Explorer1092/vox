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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.YearRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.ArrayUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.business.api.ParentNewsService;
import com.voxlearning.utopia.entity.misc.ZyParentNewsArticle;
import com.voxlearning.utopia.entity.misc.ZyParentNewsRawArticle;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.news.client.JxtNewsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.AsyncNewsCacheService;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.controller.open.v1.util.JxtNewsUtil;
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

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_NEED_RELOGIN_CODE;

/**
 * @author malong
 * @since 2016/7/8
 */
@Controller
@Slf4j
@RequestMapping(value = "/userMobile/jxtNews")
public class JxtNewsController extends AbstractMobileController {

    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @ImportService(interfaceClass = AsyncNewsCacheService.class)
    private AsyncNewsCacheService asyncNewsCacheService;

    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject
    private JxtNewsServiceClient jxtNewsServiceClient;
    @ImportService(interfaceClass = ParentNewsService.class)
    private ParentNewsService parentNewsService;
    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;

    /**
     * 获取精选资讯列表
     */
    @RequestMapping(value = "getPushRecordList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getPushRecordList() {
        int currentPage = getRequestInt("currentPage");
        User user = currentUser();
        Long userId = user == null ? 0L : user.getId();
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

        List<JxtNewsPushRecord> jxtNewsPushRecordList = jxtNewsLoaderClient.getAllOnlineJxtNewsPushRecord();

        jxtNewsPushRecordList = jxtNewsPushRecordList.stream()
                .filter(e -> CollectionUtils.isNotEmpty(e.getJxtNewsIdList()))
                .filter(e -> e.getStartTime() != null && e.getStartTime().before(new Date()))
                .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                        || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && userId.equals(p.getAvailableUserId()))
                        || (p.generatePushType().equals(JxtNewsPushType.REGION.getType()) && parentRegionIds.stream().anyMatch(e -> p.getRegionCodeList().contains(e))))
                .collect(Collectors.toList());
        jxtNewsPushRecordList = jxtNewsPushRecordList.stream().sorted((o1, o2) -> o2.getStartTime().compareTo(o1.getStartTime())).collect(Collectors.toList());
        currentPage = currentPage < 1 ? 1 : currentPage;
        Pageable request = new PageRequest(currentPage - 1, 5);
        Page<JxtNewsPushRecord> pushRecordPage = PageableUtils.listToPage(jxtNewsPushRecordList, request);
        List<List<Map<String, Object>>> list = generatePushRecordList(pushRecordPage.getContent(), userId);
        return MapMessage.successMessage().add("pushRecordList", list).add("totalPage", pushRecordPage.getTotalPages()).add("currentPage", currentPage);
    }

    /**
     * h5资讯列表
     */
    @RequestMapping(value = "getJxtNewsList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsList() {
        Long channelId = getRequestLong("channel_id", Long.MIN_VALUE);
        Long tagId = getRequestLong("tag_id", Long.MIN_VALUE);
        int currentPage = getRequestInt("current_page");
        User user = currentUser();
        if (channelId != Long.MIN_VALUE) {
            return getChannelNewsList(channelId, currentPage, user);
        } else if (tagId != Long.MIN_VALUE) {
            // load标签下的文章
            return getTagNewsList(tagId, currentPage, user);
        } else {
            // 推荐列表下来的文章
            Long userId = user == null ? 0L : user.getId();
            List<JxtNews> jxtNewses = jxtNewsLoaderClient.getAllOnlineJxtNews();
            return getReturnList(jxtNewses, currentPage, userId);
        }
    }

    /**
     * 资讯列表(新文章)
     */
    @RequestMapping(value = "getJxtNewsListAfterTime.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsListAfterTime() {
        Integer inputPage = getRequestInt("currentPage");

        User user = currentUser();
        LogCollector.info("backend-general",
                MiscUtils.map(
                        "op", "JxtNewsController.getJxtNewsListAfterTime",
                        "ver", getRequestString("app_version"),
                        "uid", user == null ? 0 : user.getId()
                ));
        if (user == null) {
            return MapMessage.errorMessage("");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        List<JxtNews> jxtNewses = jxtNewsLoaderClient.getAllOnlineJxtNews();
        //已经展示过给该用户的资讯记录
        List<String> showedNewsIds = new ArrayList<>();
        JxtNewsParentShowRecord showRecord = jxtNewsLoaderClient.getShowRecord(user.getId());
        if (showRecord != null && CollectionUtils.isNotEmpty(showRecord.getShowRecordList())) {
            showedNewsIds.addAll(showRecord.getShowRecordList().stream().map(JxtNewsParentShowRecord.ShowRecord::getNewsId).collect(Collectors.toList()));
        }
        jxtNewses = jxtNewses.stream().filter(p -> !showedNewsIds.contains(p.getId())).collect(Collectors.toList());
        //currentPage==1就代表是首次进来的
        //不传这个参数就代表 是我进来后下拉刷新的

        Set<Integer> parentRegionIds = new HashSet<>();
        Long userId = user.getId();
        if (userId != null && userId > 0L) {
            Set<Long> studentIds = studentLoaderClient.loadParentStudents(userId).stream().map(User::getId).collect(Collectors.toSet());
            Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
            for (StudentDetail studentDetail : studentDetails) {
                parentRegionIds.add(studentDetail.getStudentSchoolRegionCode());
                parentRegionIds.add(studentDetail.getCityCode());
                parentRegionIds.add(studentDetail.getRootRegionCode());
            }
        }
        jxtNewses = jxtNewses.stream()
                .filter(p -> DateUtils.dayDiff(new Date(), p.getPushTime()) <= 5)
                .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                        || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && userId != null && userId.equals(p.getAvailableUserId()))
                        || (p.generatePushType().equals(JxtNewsPushType.REGION.getType()) && parentRegionIds.stream().anyMatch(e -> p.getRegionCodeList().contains(e))))
                .filter(e -> e.getJxtNewsType() != JxtNewsType.UNKNOWN)
                .sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()))
                .collect(Collectors.toList());
        int totalCount = jxtNewses.size();
        //处理下异常分页。小于第一页。直接从第一页开始。前端让路伟查了。
        Integer currentPage = inputPage < 1 ? 1 : inputPage;
        int startIndex = (currentPage - 1) * 8;
        int endIndex = totalCount > startIndex + 8 ? startIndex + 8 : totalCount;
        if (startIndex > endIndex) {
            jxtNewses = new ArrayList<>();
        } else {
            jxtNewses = jxtNewses.subList(startIndex, endIndex);
        }
        if (CollectionUtils.isEmpty(jxtNewses)) {
            //取config里配的tag
            List<Map<String, Object>> tagMapList = generateTagListFromConfig("JXT_NEWS_LIST_TAG_CONFIG");
            mapMessage.add("tagList", tagMapList);
            mapMessage.add("newMapList", Collections.emptyList());
        } else {
            //记录已经展示过的资讯id
            //用户存showRecord的IDList需要与返回给前端的List顺序完全倒序
            List<String> needRecordIds = jxtNewses.stream().map(JxtNews::getId).collect(Collectors.toList());
            Object[] array = needRecordIds.toArray();
            CollectionUtils.reverseArray(array);
            List<Object> hadReadIdList = Arrays.asList(array);
            List<String> readIds = new ArrayList<>();
            hadReadIdList.stream().filter(p -> !readIds.contains(SafeConverter.toString(p))).forEach(p -> readIds.add(SafeConverter.toString(p)));
            jxtNewsServiceClient.addShowRecord(userId, readIds);
            List<Map<String, Object>> newMapList = generateJxtNewMapList(jxtNewses);
            mapMessage.add("newMapList", newMapList);
            mapMessage.add("tagList", Collections.emptyList());
        }
        if (inputPage == 1) {
            //首次进入。需要返回上一次浏览的8篇记录
            //需要给两个Map  readMapList    newMapList
            //把已曝光列表做倒序
            Object[] array = showedNewsIds.toArray();
            CollectionUtils.reverseArray(array);
            List<Object> hadReadIdList = Arrays.asList(array);
            totalCount = showedNewsIds.size();
            startIndex = (currentPage - 1) * 100;
            endIndex = totalCount > startIndex + 100 ? startIndex + 100 : totalCount;

            if (startIndex > endIndex) {
                hadReadIdList = new ArrayList<>();
            } else {
                hadReadIdList = hadReadIdList.subList(startIndex, endIndex);
            }
            List<String> readIds = new ArrayList<>();
            hadReadIdList.stream().filter(p -> !readIds.contains(SafeConverter.toString(p))).forEach(p -> readIds.add(SafeConverter.toString(p)));
            Collection<JxtNews> hadReadNewsList = jxtNewsLoaderClient.getJxtNewsByNewsIds(readIds).values().stream().filter(JxtNews::getOnline).sorted((o1, o2) -> readIds.indexOf(o1.getId()) - readIds.indexOf(o2.getId())).collect(Collectors.toList());
            List<Map<String, Object>> readMapList = generateJxtNewMapList(hadReadNewsList);
            mapMessage.add("readMapList", readMapList);
            return mapMessage;
        } else if (inputPage == 0) {
            return mapMessage;
        } else {
            return MapMessage.errorMessage("参数错误");
        }
    }


    /**
     * 资讯列表(历史文章)
     */
    @RequestMapping(value = "getJxtNewsListBeforeTime.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsListBeforeTime() {
        int currentPageBeforeFiveDays = getRequestInt("currentPage");
        Long tagId = getRequestLong("tag_id");
        User user = currentUser();
        LogCollector.info("backend-general",
                MiscUtils.map(
                        "op", "JxtNewsController.getJxtNewsListBeforeTime",
                        "ver", getRequestString("app_version"),
                        "uid", user == null ? 0 : user.getId()
                ));
        if (tagId != 0) {
            return getTagNewsList(tagId, currentPageBeforeFiveDays, user);
        }
        Long userId = user == null ? 0L : user.getId();
        List<JxtNews> jxtNewses = jxtNewsLoaderClient.getAllOnlineJxtNews();
        List<JxtNews> beforeDaysList = jxtNewses.stream().filter(jxtNews -> DateUtils.dayDiff(new Date(), jxtNews.getUpdateTime()) > 5).collect(Collectors.toList());

        return getReturnList(beforeDaysList, currentPageBeforeFiveDays, userId);
    }


    /**
     * 频道(标签)列表
     */
    @RequestMapping(value = "getChannelList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getChannelList() {
        User user = currentUser();
        LogCollector.info("backend-general",
                MiscUtils.map(
                        "op", "JxtNewsController.getChannelList",
                        "ver", getRequestString("app_version"),
                        "uid", user == null ? 0 : user.getId()
                ));
        // 取config里配的tag
        List<JxtNewsChannel> jxtNewsChannels = jxtNewsLoaderClient.getAllOnlineJxtNewsChannels();
        // 根据rank进行排序
        Collections.sort(jxtNewsChannels, (o1, o2) -> {
            long rc1 = ConversionUtils.toLong(o1.getRank());
            long rc2 = ConversionUtils.toLong(o2.getRank());
            return Long.compare(rc1, rc2);
        });
        List<Map<String, Object>> channelMapList = new ArrayList<>();
        jxtNewsChannels.forEach(p -> {
            Map<String, Object> channelMap = new HashMap<>();
            channelMap.put("tagId", p.getChannelId());
            channelMap.put("tagName", p.getName());
            // 有配广告就放一个广告位
            if (p.getAdSlotId() != null && p.getAdSlotId() != 0) {
                channelMap.put("adId", p.getId().toString());
            }
            channelMapList.add(channelMap);
        });
        return MapMessage.successMessage().add("channelList", channelMapList);
    }


    /**
     * 获取文章的具体信息
     */
    @RequestMapping(value = "getParentNewsArticleById.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getParentNewsArticleById() {
        String id = getRequestString("id");
        if (StringUtils.isEmpty(id)) {
            // 为了兼容已经发送出去的文章"复制推广连接"
            id = getRequestString("articleId");
        }
        String type = getRequestString("type");
        String title, content;
        if (StringUtils.equals(type, "raw")) {
            ZyParentNewsRawArticle zyParentNewsRawArticle = parentNewsService.loadRawArticleById(id);
            if (zyParentNewsRawArticle == null) {
                return MapMessage.errorMessage("您要查看的文章不存在");
            }
            title = zyParentNewsRawArticle.getTitle();
            content = zyParentNewsRawArticle.getContent();
        } else {
            ZyParentNewsArticle zyParentNewsArticle = parentNewsService.loadArticleById(id);
            if (zyParentNewsArticle == null) {
                return MapMessage.errorMessage("您要查看的文章不存在");
            }
            title = zyParentNewsArticle.getTitle();
            content = zyParentNewsArticle.getContent();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        return MapMessage.successMessage().add("parentNewsArticleDetail", map);
    }

    /**
     * 文章分享记录
     */
    @RequestMapping(value = "saveShareRecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveShareRecord() {
        String newsId = getRequestString("newsId");
        User user = currentUser();

        JxtNewsSharedRecord jxtNewsSharedRecord = new JxtNewsSharedRecord();
        if (user != null) {
            String userName = generateUserName(user);
            jxtNewsSharedRecord.setUserId(user.getId());
            jxtNewsSharedRecord.setUserName(userName);
            jxtNewsSharedRecord.setUserType(user.getUserType());
        }
        jxtNewsSharedRecord.setNewsId(newsId);
        String id = jxtNewsServiceClient.saveJxtNewsSharedRecord(jxtNewsSharedRecord);
        if (id != null) {
            return MapMessage.successMessage("分享成功");
        }
        return MapMessage.errorMessage("分享失败");
    }

    @RequestMapping(value = "jxt_news_be_gray.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage isJxtNewsAdGrayRegion() {
        User user = currentUser();
        LogCollector.info("backend-general",
                MiscUtils.map(
                        "op", "JxtNewsController.isJxtNewsAdGrayRegion",
                        "ver", getRequestString("app_version"),
                        "uid", user == null ? 0 : user.getId()
                ));

        return MapMessage.successMessage().add("hit_ad_gray", Boolean.FALSE);

//        User user = currentUser();
//        //未登录用户直接显示广告
//        if (user == null) {
//            return MapMessage.successMessage().add("hit_ad_gray", Boolean.FALSE);
//        }
//        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
//        //没有孩子的直接显示广告
//        if (CollectionUtils.isEmpty(studentParentRefs)) {
//            return MapMessage.successMessage().add("hit_ad_gray", Boolean.FALSE);
//        }
//        Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
//        Set<Integer> studentRegionCodes = studentLoaderClient.loadStudentDetails(studentIds).values().stream().filter(p -> p.getClazz() != null).map(StudentDetail::getStudentSchoolRegionCode).collect(Collectors.toSet());
//        for (Integer region : studentRegionCodes) {
//            boolean hitAdGray = regionServiceClient.checkRegionGrayStatus(region, RegionConstants.TAG_TENCENT_ADVERTISEMENT_NEWS_REGIONS);
//            if (hitAdGray) {
//                return MapMessage.successMessage().add("hit_ad_gray", Boolean.TRUE);
//            }
//        }
//        return MapMessage.successMessage().add("hit_ad_gray", Boolean.FALSE);
    }

    /*
    *
    * 收藏文章
    *
    * */
    @RequestMapping(value = "collect_news.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage collectNews() {
        String newsId = getRequestString("news_id");
        User user = currentUser();

        if (user == null) {
            return MapMessage.errorMessage("请重新登录账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage("资讯ID不能为空");
        }
        JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(newsId);
        if (jxtNews == null) {
            return MapMessage.errorMessage("您要收藏的资讯不存在");
        }
        if (jxtNews.getJxtNewsStyleType() == JxtNewsStyleType.OFFICIAL_ACCOUNT) {
            return MapMessage.errorMessage("暂不支持收藏公众号文章");
        }
        String userId = String.valueOf(user.getId());
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
        return MapMessage.successMessage("收藏成功");

    }

    /*
    *
    * 删除收藏文章
    *
    * */
    @RequestMapping(value = "del_collection.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delCollection() {
        String newsId = getRequestString("news_id");
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage("资讯ID不能为空");
        }
        String userId = String.valueOf(user.getId());
        JxtNewsCollection jxtNewsCollection = jxtNewsLoaderClient.loadCollectionRecord(userId);
        if (jxtNewsCollection == null) {
            return MapMessage.errorMessage("删除收藏失败");
        }
        List<String> jxtNewsCollectionList = jxtNewsCollection.getColNewsIds();
        jxtNewsCollectionList.remove(newsId);
        MapMessage mapMessage = jxtNewsServiceClient.delCollection(userId, jxtNewsCollectionList);
        if (mapMessage.isSuccess()) {
//            vendorCacheClient.getParentJxtCacheManager().decrCollectCount(newsId);
            asyncNewsCacheService
                    .JxtNewsCacheManager_decrCacheCount(JxtNewsCacheType.JXT_NEWS_COLLECTED_COUNT, newsId)
                    .awaitUninterruptibly();
        }
        return MapMessage.successMessage("删除收藏成功");

    }

    /*
    *
    * 获取收藏文章的列表
    *
    * */
    @RequestMapping(value = "collection_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCollectionList() {
        User user = currentUser();
        int currentPage = getRequestInt("currentPage");

        if (user != null) {
            String userId = String.valueOf(user.getId());
            JxtNewsCollection jxtNewsCollection = jxtNewsLoaderClient.loadCollectionRecord(userId);
            if (jxtNewsCollection == null) {
                List<JxtNews> collectionEmptyList = new ArrayList<>();
                return getCollectionReturnList(collectionEmptyList, currentPage, user.getId());
            }
            List<String> jxtNewsCollectionList = jxtNewsCollection.getColNewsIds();
            Collections.reverse(jxtNewsCollectionList);
            Map<String, JxtNews> collectionMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(jxtNewsCollectionList);
            if (MapUtils.isEmpty(collectionMap)) {
                List<JxtNews> MapEmptyList = new ArrayList<>();
                return getCollectionReturnList(MapEmptyList, currentPage, user.getId());
            }
            Collection<JxtNews> newsColList = collectionMap.values().stream().filter(p -> p.getJxtNewsStyleType() != JxtNewsStyleType.OFFICIAL_ACCOUNT).collect(Collectors.toList());
            return getCollectionReturnList(newsColList, currentPage, user.getId());
        }
        return MapMessage.errorMessage("获取收藏列表失败");
    }


    /*
    *
    * 删除评论
    *
    * */
    @RequestMapping(value = "delete_self_comment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delSelfComment() {
        String commentId = getRequestString("comment_id");
        if (StringUtils.isBlank(commentId)) {
            return MapMessage.errorMessage("评论id不能为空");
        }
        JxtNewsComment jxtNewsComment = jxtNewsLoaderClient.getCommentById(commentId);
        if (jxtNewsComment == null) {
            return MapMessage.errorMessage("评论不存在");
        }
        jxtNewsComment.setIsDisabled(Boolean.TRUE);
        jxtNewsServiceClient.saveJxtNewsComment(jxtNewsComment);
        return MapMessage.successMessage("评论删除成功");

    }

    /**
     * 根据tagId获取tag中的文章
     * 在h5中尽然找不到调用这个接口的地方，是不是可以删除了！
     */
    @RequestMapping(value = "load_jxt_news_by_tag.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsByTag() {
        Long channelId = getRequestLong("channel_id", Long.MIN_VALUE);
        Long tagId = getRequestLong("tag_id", Long.MIN_VALUE);
        if (tagId == Long.MIN_VALUE && channelId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("分类或者频道ID不能为空");
        }
        int currentPage = getRequestInt("current_page");
        User user = currentUser();
        if (channelId != Long.MIN_VALUE) {
            return getChannelNewsList(channelId, currentPage, user);
        } else {
            // load标签下的文章
            return getTagNewsList(tagId, currentPage, user);
        }
    }

    /**
     * 获取通知列表
     */
    @RequestMapping(value = "getJxtNewsNoticeList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsNoticeList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long userId = user.getId();

        List<JxtNewsComment> jxtNewsCommentList = jxtNewsLoaderClient.getAllOnlineCommentsByUserId(userId);
        Set<String> newsIds = jxtNewsCommentList.stream().map(JxtNewsComment::getNewsId).collect(Collectors.toSet());
        Map<String, JxtNews> jxtNewsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        jxtNewsCommentList = jxtNewsCommentList.stream()
                .filter(e -> com.voxlearning.alps.calendar.DateUtils.dayDiff(new Date(), e.getUpdateTime()) < 30)
                .sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()))
                .collect(Collectors.toList());

        List<Map<String, Object>> noticeList = new ArrayList<>();

        jxtNewsCommentList.forEach(e -> {
            Map<String, Object> noticeMap = new HashMap<>();
            noticeMap.put("title", "我的评论被筛选为精选评论：");
            String pickTime = com.voxlearning.alps.calendar.DateUtils.dateToString(e.getUpdateTime(), "yyyy-MM-dd");
            String comment = e.getComment();
            String articleTitle = jxtNewsMap.get(e.getNewsId()).getTitle();
            String newsId = e.getNewsId();
            noticeMap.put("comment", comment);
            noticeMap.put("articleTitle", articleTitle);
            noticeMap.put("newsId", newsId);
            noticeMap.put("pickTime", pickTime);
            noticeList.add(noticeMap);
        });
        return MapMessage.successMessage().add("noticeInfo", noticeList);
    }


    /**
     * 通知提醒消失
     */
    @RequestMapping(value = "dismissNoticeCount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage dismissNoticeCount() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long userId = user.getId();

        //调用通知列表请求后，删除通知数量的缓存
//        jxtNewsServiceClient.deleteJxtNewsNoticeCacheByUserId(userId);
//        vendorCacheClient.getParentJxtCacheManager().deleteTabNoticeCount(userId.toString());
        asyncNewsCacheService
                .JxtNewsCacheManager_decrCacheCount(JxtNewsCacheType.JXT_NEWS_TAB_NOTICE_COUNT, userId.toString())
                .awaitUninterruptibly();

        return MapMessage.successMessage();
    }


    /**
     * 获取订阅专辑的列表
     */
    @RequestMapping(value = "getJxtNewsAlbumList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsAlbumList() {
        User user = currentUser();
        LogCollector.info("backend-general",
                MiscUtils.map(
                        "op", "JxtNewsController.getJxtNewsAlbumList",
                        "ver", getRequestString("app_version"),
                        "uid", user == null ? 0 : user.getId()
                ));
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long userId = user.getId();
        Map<Long, List<ParentNewsAlbumSubRecord>> albumSubRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(userId));
        if (MapUtils.isEmpty(albumSubRecordByUserId) || CollectionUtils.isEmpty(albumSubRecordByUserId.get(userId))) {
            return MapMessage.successMessage("没有订阅专辑");
        }
        return generateReturnSubAlbumList(albumSubRecordByUserId.get(userId), userId);
    }


    /**
     * 订阅专辑
     */
    @RequestMapping(value = "subAlbum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage subAlbum() {
        String albumId = getRequestString("albumId");
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(albumId)) {
            return MapMessage.errorMessage("未选择要订阅的专辑");
        }
        JxtNewsAlbum jxtNewsAlbum = jxtNewsLoaderClient.getJxtNewsAlbum(albumId);
        if (jxtNewsAlbum == null) {
            return MapMessage.errorMessage("您要订阅的专辑不存在");
        }
        Long userId = user.getId();
        String subId = ParentNewsAlbumSubRecord.generateId(userId, albumId);
        ParentNewsAlbumSubRecord albumSubRecordById = jxtNewsLoaderClient.getAlbumSubRecordById(subId);
        if (albumSubRecordById != null && albumSubRecordById.getIsSub()) {
            return MapMessage.errorMessage("您已经订阅过该专辑");
        }

        MapMessage mapMessage = jxtNewsServiceClient.subAlbum(userId, albumId);
        if (mapMessage.isSuccess()) {
            Map<Long, List<ParentNewsAlbumSubRecord>> albumSubRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(userId));
            if (MapUtils.isNotEmpty(albumSubRecordByUserId) && CollectionUtils.isNotEmpty(albumSubRecordByUserId.get(userId))) {
                List<ParentNewsAlbumSubRecord> newsAlbumSubRecords = albumSubRecordByUserId.get(userId).stream().filter(ParentNewsAlbumSubRecord::getIsSub).collect(Collectors.toList());
                //为了兼容1.9.0的过滤条件这里不得不取专辑做一下过滤。。。http://wiki.17zuoye.net/pages/viewpage.action?pageId=30545778
                Set<String> albumIds = newsAlbumSubRecords.stream().map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toSet());
                List<JxtNewsAlbum> albumList = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds).values().stream()
                        .filter(e -> SafeConverter.toBoolean(e.getFree(), Boolean.TRUE))
                        .filter(e -> e.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE)
                        .collect(Collectors.toList());
                //取每个专辑的第一篇资讯的类型作为专辑的类型
                //Map<String, JxtNewsContentType> albumType = generateAlbumType(albumList);
                //过滤掉图文专辑
                albumList = albumList.stream().filter(p -> p.getJxtNewsAlbumContentType() != null && p.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT).collect(Collectors.toList());
                String updateText = albumList.size() + "个订阅";
                mySelfStudyService.updateSelfStudyProgress(userId, SelfStudyType.ALBUM, updateText);
                mySelfStudyService.updateIcon(userId, SelfStudyType.ALBUM, StringUtils.isNotBlank(jxtNewsAlbum.getHeadImg()) ? "gridfs/" + jxtNewsAlbum.getHeadImg() : "");
            }
            return mapMessage.setInfo("订阅成功");
        }

        return mapMessage.setInfo("订阅失败");
    }


    /**
     * 取消订阅专辑
     */
    @RequestMapping(value = "delSubAlbum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delSubAlbum() {
        String albumId = getRequestString("albumId");
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(albumId)) {
            return MapMessage.errorMessage("未选择要取消订阅的专辑");
        }
        JxtNewsAlbum jxtNewsAlbum = jxtNewsLoaderClient.getJxtNewsAlbum(albumId);
        if (jxtNewsAlbum == null) {
            return MapMessage.errorMessage("您要取消订阅的专辑不存在");
        }
        if (null != jxtNewsAlbum.getFree() && !jxtNewsAlbum.getFree()) {
            return MapMessage.errorMessage("已购买的专辑不能取消订阅哦");
        }
        Long userId = user.getId();
        String subId = ParentNewsAlbumSubRecord.generateId(userId, albumId);
        ParentNewsAlbumSubRecord albumSubRecordById = jxtNewsLoaderClient.getAlbumSubRecordById(subId);
        if (albumSubRecordById == null) {
            return MapMessage.errorMessage("您还未订阅过该专辑");
        }
        if (!albumSubRecordById.getIsSub()) {
            return MapMessage.errorMessage("您已经取消订阅该专辑了");
        }
        MapMessage mapMessage = jxtNewsServiceClient.cancelSubAlbum(userId, albumId);
        if (mapMessage.isSuccess()) {
            Map<Long, List<ParentNewsAlbumSubRecord>> albumSubRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(userId));
            if (MapUtils.isNotEmpty(albumSubRecordByUserId) && CollectionUtils.isNotEmpty(albumSubRecordByUserId.get(userId))) {
                List<ParentNewsAlbumSubRecord> newsAlbumSubRecords = albumSubRecordByUserId.get(userId).stream().filter(ParentNewsAlbumSubRecord::getIsSub).collect(Collectors.toList());
                //为了兼容1.9.0的过滤条件这里不得不取专辑做一下过滤。。。http://wiki.17zuoye.net/pages/viewpage.action?pageId=30545778
                Set<String> albumIds = newsAlbumSubRecords.stream().map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toSet());
                List<JxtNewsAlbum> albumList = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds).values().stream()
                        .filter(e -> SafeConverter.toBoolean(e.getFree(), Boolean.TRUE))
                        .filter(e -> e.generateJxtNewsAlbumType() == JxtNewsAlbumType.INSIDE)
                        .collect(Collectors.toList());
                //取每个专辑的第一篇资讯的类型作为专辑的类型
                //Map<String, JxtNewsContentType> albumType = generateAlbumType(albumList);
                //过滤掉图文专辑
                albumList = albumList.stream().filter(p -> p.getJxtNewsAlbumContentType() != null && p.getJxtNewsAlbumContentType() != JxtNewsAlbumContentType.IMG_AND_TEXT).collect(Collectors.toList());
                int albumCount = albumList.size();
                if (albumList.size() > 0) {
                    String updateText = albumCount + "个订阅";
                    Set<String> bookAlbumIdSet = albumList.stream().map(JxtNewsAlbum::getId).collect(Collectors.toSet());
                    ParentNewsAlbumSubRecord parentNewsAlbumSubRecord = newsAlbumSubRecords.stream()
                            .filter(t -> bookAlbumIdSet.contains(t.getSubAlbumId()))
                            .sorted((o1, o2) -> o2.getUpdateDate().compareTo(o1.getUpdateDate())).findFirst().orElse(null);
                    mySelfStudyService.updateSelfStudyProgress(userId, SelfStudyType.ALBUM, updateText);
                    if (parentNewsAlbumSubRecord != null && StringUtils.isNotBlank(parentNewsAlbumSubRecord.getSubAlbumId())) {
                        JxtNewsAlbum firstAlbum = jxtNewsLoaderClient.getJxtNewsAlbum(parentNewsAlbumSubRecord.getSubAlbumId());
                        if (firstAlbum != null) {
                            mySelfStudyService.updateIcon(userId, SelfStudyType.ALBUM, StringUtils.isNotBlank(firstAlbum.getHeadImg()) ? "gridfs/" + firstAlbum.getHeadImg() : "");
                        }
                    } else {
                        mySelfStudyService.updateIcon(userId, SelfStudyType.ALBUM, "");
                    }
                } else {
                    mySelfStudyService.updateIcon(userId, SelfStudyType.ALBUM, "");
                    mySelfStudyService.updateSelfStudyProgress(userId, SelfStudyType.ALBUM, "");
                }
            }
            return mapMessage.setInfo("取消订阅成功");
        }
        return mapMessage.setInfo("取消订阅失败");
    }

    /**
     * 置顶专辑
     */
    @RequestMapping(value = "topAlbum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage topAlbum() {
        String albumId = getRequestString("albumId");
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(albumId)) {
            return MapMessage.errorMessage("未选择要置顶的专辑");
        }
        JxtNewsAlbum jxtNewsAlbum = jxtNewsLoaderClient.getJxtNewsAlbum(albumId);
        if (jxtNewsAlbum == null) {
            return MapMessage.errorMessage("您要置顶的专辑不存在");
        }
        Long userId = user.getId();
        String subId = ParentNewsAlbumSubRecord.generateId(userId, albumId);
        ParentNewsAlbumSubRecord albumSubRecordById = jxtNewsLoaderClient.getAlbumSubRecordById(subId);
        if (albumSubRecordById == null || !albumSubRecordById.getIsSub()) {
            return MapMessage.errorMessage("您还未订阅该专辑，不能置顶");
        }
        if (albumSubRecordById.getIsTop() != null && albumSubRecordById.getIsTop()) {
            return MapMessage.errorMessage("您已经置顶过该专辑了");
        }
        MapMessage mapMessage = jxtNewsServiceClient.topAlbum(userId, albumId);

        return mapMessage.setInfo("置顶成功");

    }

    /**
     * 取消置顶专辑
     */
    @RequestMapping(value = "cancelTopAlbum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelTopAlbum() {
        String albumId = getRequestString("albumId");
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(albumId)) {
            return MapMessage.errorMessage("未选择要取消置顶的专辑");
        }
        JxtNewsAlbum jxtNewsAlbum = jxtNewsLoaderClient.getJxtNewsAlbum(albumId);
        if (jxtNewsAlbum == null) {
            return MapMessage.errorMessage("您要取消订阅的专辑不存在");
        }
        Long userId = user.getId();
        String subId = ParentNewsAlbumSubRecord.generateId(userId, albumId);
        ParentNewsAlbumSubRecord albumSubRecordById = jxtNewsLoaderClient.getAlbumSubRecordById(subId);
        if (albumSubRecordById == null || !albumSubRecordById.getIsSub()) {
            return MapMessage.errorMessage("您还未订阅该专辑，不能取消置顶");
        }
        if (albumSubRecordById.getIsTop() != null && !albumSubRecordById.getIsTop()) {
            return MapMessage.errorMessage("您已经取消置顶过该专辑了");
        }
        MapMessage mapMessage = jxtNewsServiceClient.cancelTopAlbum(userId, albumId);

        return mapMessage.setInfo("取消置顶成功");

    }

    /**
     * 专辑详情
     */
    @RequestMapping(value = "albumDetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage albumDetail() {
        String albumId = getRequestString("albumId");
        JxtNewsAlbum jxtNewsAlbum = jxtNewsLoaderClient.getJxtNewsAlbum(albumId);
        User user = currentUser();
        if (jxtNewsAlbum == null) {
            return MapMessage.errorMessage("您要查看的专辑不存在");
        }
        //记一下专辑阅读的数
//        vendorCacheClient.getParentJxtCacheManager().incrAlbumReadCount(albumId);
        asyncNewsCacheService
                .JxtNewsCacheManager_incrCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_READ_COUNT, albumId)
                .awaitUninterruptibly();
        Map<String, Object> albumDetailMap = generateAlbumDetailMap(jxtNewsAlbum, user);
        return MapMessage.successMessage().add("albumDetailMap", albumDetailMap);
    }

    //苹果审核页面要的接口
    @RequestMapping(value = "newsDetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadNewsDetailForApple() {
        String newsId = getRequestString("news_id");
        JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(newsId);
        if (jxtNews == null) {
            return MapMessage.errorMessage("您要查看的资讯不存在");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("content_type", jxtNews.generateContentType());
        map.put("img_url", jxtNews.getCoverImgList());
        map.put("title", jxtNews.getTitle());
        map.put("update_time", jxtNews.getUpdateTime().getTime());
        return MapMessage.successMessage().add("news", map);
    }

    private List<List<Map<String, Object>>> generatePushRecordList(List<JxtNewsPushRecord> jxtNewsPushRecordList, Long userId) {
        List<List<Map<String, Object>>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(jxtNewsPushRecordList)) {
            return list;
        }
        Set<String> newsIds = new HashSet<>();
        jxtNewsPushRecordList.forEach(e -> newsIds.addAll(e.getJxtNewsIdList()));

        List<JxtNews> jxtNewsList = jxtNewsLoaderClient.getAllOnlineJxtNews();
        Map<String, JxtNews> jxtNewsMap = jxtNewsList.stream().collect(Collectors.toMap(JxtNews::getId, Function.identity()));
        // 取用户所有的订阅记录，后面判断用户是否订阅
        Set<String> userSubAlbumIds = new HashSet<>();
        if (userId != null && userId != 0L) {
            Map<Long, List<ParentNewsAlbumSubRecord>> albumSubRecordByUserId = jxtNewsLoaderClient.getAlbumSubRecordByUserId(Collections.singleton(userId));
            List<ParentNewsAlbumSubRecord> albumSubRecordList = albumSubRecordByUserId.get(userId);
            if (CollectionUtils.isNotEmpty(albumSubRecordList)) {
                userSubAlbumIds = albumSubRecordList.stream().filter(ParentNewsAlbumSubRecord::getIsSub).map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toSet());
            }
        }
        //资讯id和title的map
        Map<String, String> idTitleMap = jxtNewsList.stream()
                .filter(e -> newsIds.contains(e.getId()))
                .collect(Collectors.toMap(JxtNews::getId, JxtNews::getTitle));

        for (JxtNewsPushRecord jxtNewsPushRecord : jxtNewsPushRecordList) {
            List<Map<String, Object>> jxtNewsMapList = new ArrayList<>();
            List<String> jxtNewsIdList = jxtNewsPushRecord.getJxtNewsIdList();
            Map<String, String> jxtNewsCoverImgMap = jxtNewsPushRecord.getJxtNewsCoverImgMap();
            for (String id : jxtNewsIdList) {
                JxtNews jxtNews = jxtNewsMap.get(id);
                if (jxtNews != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("newsId", id);
                    map.put("imgUrl", combineCdbUrl(MapUtils.getString(jxtNewsCoverImgMap, id, "")));
                    map.put("jxt_news_type", jxtNews.getJxtNewsType());
                    //文章的内容类型
                    if (jxtNews.getJxtNewsContentType() != null) {
                        map.put("jxt_news_content_type", jxtNews.getJxtNewsContentType());
                    }
                    if (jxtNews.generatePushType() == 3 && jxtNews.getJxtNewsContentType() == JxtNewsContentType.IMG_AND_TEXT) {
                        map.put("subscript", "location");
                    } else if (!StringUtils.equals(jxtNews.generateContentType(), JxtNewsContentType.UNKNOWN.name())) {
                        map.put("subscript", jxtNews.getJxtNewsContentType());
                    }
                    if (StringUtils.isNotBlank(jxtNews.getAlbumId())) {
                        map.put("album_id", jxtNews.getAlbumId());
                        if (userSubAlbumIds.contains(jxtNews.getAlbumId())) {
                            map.put("is_sub", Boolean.TRUE);
                        } else {
                            map.put("is_sub", Boolean.FALSE);
                        }
                    }
                    map.put("articleTitle", MapUtils.getString(idTitleMap, id, ""));
                    map.put("updateTime", jxtNews.getUpdateTime().getTime());
                    //下面这个是为了给前端显示用的，上面这个是前端从cdn取资讯比较时间用的
                    map.put("update_time", DateUtils.dateToString(jxtNews.getUpdateTime(), DateUtils.FORMAT_SQL_DATE));
                    jxtNewsMapList.add(map);
                }
            }
            if (CollectionUtils.isNotEmpty(jxtNewsMapList)) {
                Map<String, Object> timeMap = new HashMap<>();
                Date startTime = jxtNewsPushRecord.getStartTime();
                Date currentDate = new Date();
                if (startTime != null) {
                    if (DateUtils.isSameDay(currentDate, startTime)) {
                        timeMap.put("time", DateUtils.dateToString(startTime, "HH:mm"));
                    } else if (!DateUtils.isSameDay(currentDate, startTime) && YearRange.newInstance(currentDate.getTime()).getYear() == YearRange.newInstance(startTime.getTime()).getYear()) {
                        timeMap.put("time", DateUtils.dateToString(startTime, "MM-dd HH:mm"));
                    } else {
                        timeMap.put("time", DateUtils.dateToString(startTime, "yyyy-MM-dd HH:mm"));
                    }
                }
                jxtNewsMapList.add(timeMap);
                list.add(jxtNewsMapList);
            }
        }
        return list;
    }

    private List<Map<String, Object>> generateJxtNewMapList(Collection<JxtNews> jxtNewses) {
        Set<String> newsIds = jxtNewses.stream().map(JxtNews::getId).collect(Collectors.toSet());
//        Map<String, Long> newsVoteMap = vendorCacheClient.getParentJxtCacheManager().loadVoteCount(newsIds);
//        Map<String, Long> newsVoteMap = asyncNewsCacheService
//                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_VOTE_COUNT, newsIds)
//                .take();
//        Map<String, Long> newsReadMap = vendorCacheClient.getParentJxtCacheManager().loadReadCount(newsIds);
//        Map<String, Long> newsReadMap = asyncNewsCacheService
//                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, newsIds)
//                .take();

        Set<Long> allTagIds = new HashSet<>();
        jxtNewses.forEach(e -> allTagIds.addAll(e.getTagList()));
        Collection<JxtNewsTag> allTagList = jxtNewsLoaderClient.findTagsByIds(allTagIds).values();
        //取所有上线专辑，取出id
        List<JxtNewsAlbum> allOnlineJxtNewsAlbum = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum();
        List<String> onlineAlbumIds = allOnlineJxtNewsAlbum.stream().map(JxtNewsAlbum::getId).collect(Collectors.toList());
        List<Map<String, Object>> list = new ArrayList<>();
        jxtNewses.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            //标题
            String title = e.getTitle();
//            //封面图片
//            List<String> imgList = e.getCoverImgList();
//            List<String> imgUrlList = new ArrayList<>();
//            if (CollectionUtils.isNotEmpty(imgList)) {
//                imgList.forEach(p -> imgUrlList.add(combineCdbUrl(p)));
//            }
            //文章摘要
            String digest = e.getDigest();
            //标签
            List<Map<String, Object>> tagMapList = new ArrayList<>();
            List<Long> tagIds = e.getTagList();
            if (CollectionUtils.isNotEmpty(tagIds)) {
                List<JxtNewsTag> tagList = allTagList.stream().filter(p -> tagIds.contains(p.getId())).collect(Collectors.toList());
                tagList.forEach(p -> {
                    Map<String, Object> tagIdNameMap = new HashMap<>();
                    tagIdNameMap.put("tagId", p.getId());
                    tagIdNameMap.put("tagName", p.getTagName());
                    tagMapList.add(tagIdNameMap);
                });
            }
            //点赞数
//            Long voteCount = SafeConverter.toLong(newsVoteMap.get(e.getId()));
            //阅读数
//            Long readCount = SafeConverter.toLong(newsReadMap.get(e.getId()));
            //文章来源
            String source = e.getSource();

            map.put("newsId", e.getId());
            map.put("albumId", SafeConverter.toString(e.getAlbumId()));
            map.put("title", title);
            map.put("imgList", e.getCoverImgList());
            map.put("jxt_news_type", e.getJxtNewsType());
            //文章的内容类型
            if (e.getJxtNewsContentType() != null) {
                map.put("jxt_news_content_type", e.getJxtNewsContentType());
            }
            if (e.getJxtNewsType() == JxtNewsType.TEXT) {
                map.put("digest", digest);
            }

            // 内容样式
            map.put("jxt_news_style_type", e.getJxtNewsStyleType() == null ?
                    JxtNewsStyleType.NEWS : e.getJxtNewsStyleType());

            if (StringUtils.isNotBlank(e.getAlbumId()) && onlineAlbumIds.contains(e.getAlbumId())) {
                map.put("subscript", "album");
            } else if (e.generatePushType() == 3) {
                map.put("subscript", "location");
            } else if (!StringUtils.equals(e.generateContentType(), JxtNewsContentType.UNKNOWN.name())) {
                map.put("subscript", e.getJxtNewsContentType());
            }
            map.put("tagList", tagMapList);
//            map.put("voteCount", JxtNewsUtil.countFormat(voteCount));
//            map.put("readCount", JxtNewsUtil.countFormat(readCount));
            map.put("source", source);
            //更新时间。用作cdn的时间戳
            map.put("updateTime", e.getUpdateTime().getTime());
            //上下线状态
            map.put("online", e.getOnline());
            list.add(map);

        });
        return list;
    }


    private MapMessage getReturnList(Collection<JxtNews> jxtNewsList, int currentPage, Long userId) {
        List<JxtNews> newsList = new ArrayList<>(jxtNewsList);
        if (CollectionUtils.isEmpty(newsList)) {
            return MapMessage.successMessage()
                    .add("jxtNewsList", Collections.emptyList())
                    .add("totalPage", 0)
                    .add("currentPage", 1);
        }
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

        newsList = jxtNewsList.stream()
                .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                        || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && userId != null && userId.equals(p.getAvailableUserId()))
                        || (p.generatePushType().equals(JxtNewsPushType.REGION.getType()) && parentRegionIds.stream().anyMatch(e -> p.getRegionCodeList().contains(e))))
                .filter(e -> e.getJxtNewsType() != JxtNewsType.UNKNOWN)
                .sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime()))
                .collect(Collectors.toList());
        int totalCount = newsList.size();
        int totalPage = new BigDecimal(totalCount).divide(new BigDecimal(20), BigDecimal.ROUND_UP).intValue();

        //处理下异常分页。小于第一页。直接从第一页开始。前端让路伟查了。
        currentPage = currentPage < 1 ? 1 : currentPage;
        int startIndex = (currentPage - 1) * 20;
        int endIndex = totalCount > startIndex + 20 ? startIndex + 20 : totalCount;
        if (startIndex > endIndex) {
            newsList = new ArrayList<>();
        } else {
            newsList = newsList.subList(startIndex, endIndex);
        }
        List<Map<String, Object>> mapList = generateJxtNewMapList(newsList);

        return MapMessage.successMessage()
                .add("jxtNewsList", mapList)
                .add("totalPage", totalPage)
                .add("currentPage", currentPage);
    }

    private MapMessage getCollectionReturnList(Collection<JxtNews> jxtNewsList, int currentPage, Long userId) {
        List<JxtNews> newsList = new ArrayList<>(jxtNewsList);
        if (CollectionUtils.isEmpty(newsList)) {
            return MapMessage.successMessage()
                    .add("jxtNewsList", Collections.emptyList())
                    .add("totalPage", 0)
                    .add("currentPage", 1);
        }
        newsList = newsList.stream()
                .filter(e -> userId == null || SafeConverter.toLong(e.getAvailableUserId()) == 0 || userId.equals(e.getAvailableUserId()))
                .filter(e -> e.getJxtNewsType() != JxtNewsType.UNKNOWN)
                .collect(Collectors.toList());

        int totalCount = newsList.size();
        int totalPage = new BigDecimal(totalCount).divide(new BigDecimal(10), BigDecimal.ROUND_UP).intValue();

        int startIndex = (currentPage - 1) * 10;
        int endIndex = totalCount > startIndex + 10 ? startIndex + 10 : totalCount;
        if (startIndex > endIndex) {
            newsList = new ArrayList<>();
        } else {
            newsList = newsList.subList(startIndex, endIndex);
        }
        List<Map<String, Object>> mapList = generateJxtNewMapList(newsList);

        return MapMessage.successMessage()
                .add("jxtNewsList", mapList)
                .add("totalPage", totalPage)
                .add("currentPage", currentPage);
    }

    /**
     * @param channelId
     * @return 那频道的第一个tag的name，如果没有则返回""
     */
    private String getChannelFirstTagName(Long channelId) {
        JxtNewsChannel jxtNewsChannel = jxtNewsLoaderClient.getJxtNewsChannelById(channelId);
        String tagName;
        if (jxtNewsChannel != null && CollectionUtils.isNotEmpty(jxtNewsChannel.getTagIds())) {
            Long tagId = jxtNewsChannel.getTagIds().get(0);
            JxtNewsTag jxtNewsTag = jxtNewsLoaderClient.findTagById(tagId);
            tagName = jxtNewsTag == null ? "" : jxtNewsTag.getTagName();
        } else {
            tagName = "";
        }
        return tagName;
    }

    //按tag取出相应tag的资讯
    private MapMessage getTagNewsList(Long tagId, int currentPage, User user) {
        List<JxtNews> jxtNewsListByTag = jxtNewsLoaderClient.getJxtNewsListByTag(tagId);
        Long userId = user == null ? null : user.getId();
        JxtNewsTag tagById = jxtNewsLoaderClient.findTagById(tagId);
        MapMessage mapMessage = getReturnList(jxtNewsListByTag, currentPage, userId);
        return mapMessage.add("tagName", tagById == null ? "" : tagById.getTagName());
    }

    // 按channel取出相应资讯
    private MapMessage getChannelNewsList(Long channelId, int currentPage, User user) {
        List<JxtNews> jxtNewsListByChannel = jxtNewsLoaderClient.getJxtNewsListByChannel(channelId);
        Long userId = user == null ? null : user.getId();
        // 寻找channel下的第一个tag
        String tagName = getChannelFirstTagName(channelId);
        MapMessage mapMessage = getReturnList(jxtNewsListByChannel, currentPage, userId);
        return mapMessage.add("tagName", tagName);
    }


    // redmine:34095   暂且在这里写死 将来会直接从CRM里读取
    private final static Map adMap = new HashedMap() {{
        put("95", "220803");
        put("96", "220804");
        put("97", "220805");
        put("23", "220806");
        put("123", "220807");
    }};


    private List<Map<String, Object>> generateTagListFromConfig(String keyName) {


        if (StringUtils.isBlank(keyName)) {
            return Collections.emptyList();
        }

        //取commonConfig里配置的tag
        String tagValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), keyName).replaceAll("\\s*", "");
        if (StringUtils.isBlank(tagValue)) {
            return Collections.emptyList();
        }
        String[] tagArray = tagValue.split(",");
        if (ArrayUtils.isEmpty(tagArray)) {
            return Collections.emptyList();
        }
        Long[] tagIds = new Long[tagArray.length];
        for (int i = 0; i < tagArray.length; i++) {
            tagIds[i] = SafeConverter.toLong(tagArray[i]);
        }
        List<Long> tagIdList = Arrays.asList(tagIds);
        //根据取到的id获取相应的tag
        Collection<JxtNewsTag> jxtNewsTags = jxtNewsLoaderClient.findTagsByIds(tagIdList).values();

        List<Map<String, Object>> tagMapList = new ArrayList<>();
        jxtNewsTags.forEach(p -> {
            Map<String, Object> tagIdNameMap = new HashMap<>();
            tagIdNameMap.put("tagId", p.getId());
            tagIdNameMap.put("tagName", p.getTagName());

            if (keyName.equals("JXT_NEWS_LIST_CHANNEL_CONFIG") && adMap.get(p.getId().toString()) != null) {
                tagIdNameMap.put("adSlotId", adMap.get(p.getId().toString()));
            }

            tagMapList.add(tagIdNameMap);
        });
        return tagMapList;

    }

    private MapMessage generateReturnSubAlbumList(List<ParentNewsAlbumSubRecord> list, Long userId) {
        List<String> parentTopList = list.stream().filter(ParentNewsAlbumSubRecord::getIsSub).filter(p -> p.getIsTop() != null && p.getIsTop()).map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toList());
        List<String> parentSubList = list.stream().filter(ParentNewsAlbumSubRecord::getIsSub).filter(p -> p.getIsTop() == null || !p.getIsTop()).map(ParentNewsAlbumSubRecord::getSubAlbumId).collect(Collectors.toList());
        Set<String> albumSet = new HashSet<>();
        albumSet.addAll(parentTopList);
        albumSet.addAll(parentSubList);
        Map<String, JxtNewsAlbum> jxtNewsAlbumByAlbumIds = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumSet);
        List<JxtNewsAlbum> topList = jxtNewsAlbumByAlbumIds.values()
                .stream()
                .filter(p -> parentTopList.stream().anyMatch(p.getId()::contains) && p.getOnline())
                .sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()))
                .collect(Collectors.toList());
        List<JxtNewsAlbum> subList = jxtNewsAlbumByAlbumIds.values()
                .stream()
                .filter(p -> parentSubList.stream().anyMatch(p.getId()::contains) && p.getOnline())
                .sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()))
                .collect(Collectors.toList());
        List<Map<String, Object>> topMapList = generateSubAlbumListInfo(topList, userId);
        List<Map<String, Object>> subMapList = generateSubAlbumListInfo(subList, userId);

        return MapMessage.successMessage().add("topMapList", topMapList).add("subMapList", subMapList);
    }

    private List<Map<String, Object>> generateSubAlbumListInfo(List<JxtNewsAlbum> list, Long userId) {
        List<Map<String, Object>> albumList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return albumList;
        }
        Set<String> newsIds = new HashSet<>();
        Set<String> onlineNewsIds = new HashSet<>();
        Map<String, JxtNews> jxtNewsByNewsIds = new HashMap<>();
        list.forEach(e -> newsIds.addAll(e.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toList())));
        if (CollectionUtils.isNotEmpty(newsIds)) {
            jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
            if (MapUtils.isNotEmpty(jxtNewsByNewsIds)) {
                onlineNewsIds = jxtNewsByNewsIds.values().stream().filter(JxtNews::getOnline).map(JxtNews::getId).collect(Collectors.toSet());
            }
        }
        final Set<String> finalOnlineNewsIds = onlineNewsIds;
        final Map<String, JxtNews> finalJxtNewsByNewsIds = jxtNewsByNewsIds;
        list.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            List<JxtNewsAlbum.NewsRecord> newsRecordList = e.getNewsRecordList();
            Long count = 0L;
            String updateTime = "";
            if (CollectionUtils.isNotEmpty(newsRecordList)) {
                ParentShowAlbumRecord parentShowAlbumRecord = jxtNewsLoaderClient.getParentShowAlbumRecord(ParentShowAlbumRecord.generateId(userId, e.getId()));
                if (parentShowAlbumRecord != null) {
                    //列表页中的文章更新数，根据文章加入专辑的时间和文章的上线时间与用户的浏览时间进行判断
                    count = newsRecordList.stream().filter(p -> CollectionUtils.isNotEmpty(finalOnlineNewsIds) && finalOnlineNewsIds.contains(p.getNewsId()))
                            .filter(p -> finalJxtNewsByNewsIds.get(p.getNewsId()) != null && finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime() != null
                                    && p.getCreateTime().before(finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime()) ? finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime().after(parentShowAlbumRecord.getUpdateTime()) : p.getCreateTime().after(parentShowAlbumRecord.getUpdateTime()))
                            .count();
                    /*
                    * 列表页的更新时间通过文章上线时间的排序和文章加入时间的排序取各个排序中的第一个进行比较，哪个时间更近取哪个时间更近就取哪个时间。
                    * 如果专辑中没有文章，就取专辑的更新时间作为列表页显示的更新时间。
                    * */
                    JxtNewsAlbum.NewsRecord firstNewsRecordByPushTime = newsRecordList.stream().filter(p -> CollectionUtils.isNotEmpty(finalOnlineNewsIds) && finalOnlineNewsIds.contains(p.getNewsId()))
                            .filter(p -> finalJxtNewsByNewsIds.get(p.getNewsId()) != null && finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime() != null)
                            .sorted((o1, o2) -> finalJxtNewsByNewsIds.get(o2.getNewsId()).getPushTime().compareTo(finalJxtNewsByNewsIds.get(o1.getNewsId()).getPushTime()))
                            .findFirst().orElse(null);
                    JxtNewsAlbum.NewsRecord firstNewsRecordByCreateTime = newsRecordList.stream().filter(p -> CollectionUtils.isNotEmpty(finalOnlineNewsIds) && finalOnlineNewsIds.contains(p.getNewsId()))
                            .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                            .findFirst().orElse(null);
                    if (firstNewsRecordByCreateTime != null && firstNewsRecordByPushTime != null) {
                        Date firstPushTime = finalJxtNewsByNewsIds.get(firstNewsRecordByPushTime.getNewsId()).getPushTime();
                        Date firstCreateTime = firstNewsRecordByCreateTime.getCreateTime();
                        if (firstPushTime.after(firstCreateTime)) {
                            updateTime = DateUtils.dateToString(firstPushTime, DateUtils.FORMAT_SQL_DATE);
                        } else {
                            updateTime = DateUtils.dateToString(firstCreateTime, DateUtils.FORMAT_SQL_DATE);
                        }
                    }
                }
            }
            map.put("albumId", e.getId());
            map.put("title", e.getTitle());
            map.put("imgUrl", combineCdbUrl(e.getHeadImg()));
            map.put("updateTime", StringUtils.isNotBlank(updateTime) ? updateTime : DateUtils.dateToString(e.getUpdateTime(), DateUtils.FORMAT_SQL_DATE));
            map.put("updateCount", count);
            map.put("digest", e.getDetail());
            albumList.add(map);
        });
        return albumList;
    }

    private Map<String, Object> generateAlbumDetailMap(JxtNewsAlbum jxtNewsAlbum, User user) {
        Map<String, Object> map = new HashMap<>();
        if (jxtNewsAlbum == null) {
            return map;
        }
        //专辑对应的产品ID（如果专辑是免费的，这个字段没有值）
        map.put("productId", jxtNewsAlbum.getOrderProductId());
        map.put("price", jxtNewsAlbum.getPrice());
        map.put("originalPrice", jxtNewsAlbum.getOriginalPrice());
        map.put("free", null == jxtNewsAlbum.getFree() ? true : jxtNewsAlbum.getFree());

        //取这个专辑的订阅数
//        Map<String, Long> subCountMap = vendorCacheClient.getParentJxtCacheManager().loadAlbumSubCount(Collections.singleton(jxtNewsAlbum.getId()));
//        Map<String, Long> subCountMap = asyncNewsCacheService
//                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_SUB_COUNT, Collections.singleton(jxtNewsAlbum.getId()))
//                .take();
        //取这个专辑的阅读数
//        Map<String, Long> albumReadCountMap = vendorCacheClient.getParentJxtCacheManager().loadAlbumReadCount(Collections.singleton(jxtNewsAlbum.getId()));
//        Map<String, Long> albumReadCountMap = asyncNewsCacheService
//                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_READ_COUNT, Collections.singleton(jxtNewsAlbum.getId()))
//                .take();
        //专辑名称
        map.put("title", jxtNewsAlbum.getTitle());
        //专辑作者
        map.put("author", jxtNewsAlbum.getAuthor());
        //专辑封面
        map.put("imgUrl", combineCdbUrl(jxtNewsAlbum.getHeadImg()));
        //专辑订阅数
//        Long subCount = MapUtils.getLong(subCountMap, jxtNewsAlbum.getId(), 0L);
//        map.put("subCount", JxtNewsUtil.countFormat(subCount));
        //专辑内容简介
        map.put("detail", jxtNewsAlbum.getDetail());
        //专辑中文章的list
        List<JxtNewsAlbum.NewsRecord> allNewsList = new ArrayList<>();
        //取最近更新的资讯
        List<JxtNewsAlbum.NewsRecord> updateNewsList = new ArrayList<>();
        //取之前已经在该专辑下的资讯，
        List<JxtNewsAlbum.NewsRecord> oldNewsList = new ArrayList<>();
        Map<String, JxtNews> jxtNewsMap;
        if (CollectionUtils.isNotEmpty(jxtNewsAlbum.getNewsRecordList())) {
            Set<String> newsIds = jxtNewsAlbum.getNewsRecordList().stream().filter(p -> StringUtils.isNotBlank(p.getNewsId())).map(JxtNewsAlbum.NewsRecord::getNewsId).
                    collect(Collectors.toSet());
            jxtNewsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
            Set<String> allOnlineNewsIds = jxtNewsMap.values().stream().filter(p -> p != null && p.getOnline()).map(JxtNews::getId).collect(Collectors.toSet());
            //取出所有上线的文章
            allNewsList = jxtNewsAlbum.getNewsRecordList().stream().filter(p -> StringUtils.isNotBlank(p.getNewsId()) && allOnlineNewsIds.contains(p.getNewsId())).collect(Collectors.toList());
            if (user != null) {
                String subId = ParentNewsAlbumSubRecord.generateId(user.getId(), jxtNewsAlbum.getId());
                ParentNewsAlbumSubRecord albumSubRecordById = jxtNewsLoaderClient.getAlbumSubRecordById(subId);
                //是否已经订阅
                if (albumSubRecordById != null && albumSubRecordById.getIsSub()) {
                    //取订阅用户的浏览记录
                    ParentShowAlbumRecord parentShowAlbumRecord = jxtNewsLoaderClient.getParentShowAlbumRecord(ParentShowAlbumRecord.generateId(user.getId(), jxtNewsAlbum.getId()));
                    if (parentShowAlbumRecord != null) {
                        final Map<String, JxtNews> finalJxtNewsByNewsIds = jxtNewsMap;
                        //取用户浏览记录更新时间之后的文章作为新文章
                        updateNewsList = allNewsList.stream().filter(p -> finalJxtNewsByNewsIds.get(p.getNewsId()) != null && finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime() != null
                                //判断出资讯的上线时间和资讯加入专辑的时间中最新的时间，用最新的那个时间与用户的浏览时间进行比较
                                && p.getCreateTime().before(finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime()) ? finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime().after(parentShowAlbumRecord.getUpdateTime()) : p.getCreateTime().after(parentShowAlbumRecord.getUpdateTime()))
                                .collect(Collectors.toList());
                        //取用户浏览记录更新时间之前的文章作为旧文章
                        oldNewsList = allNewsList.stream().filter(p -> finalJxtNewsByNewsIds.get(p.getNewsId()) != null && finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime() != null
                                && p.getCreateTime().before(finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime()) ? finalJxtNewsByNewsIds.get(p.getNewsId()).getPushTime().before(parentShowAlbumRecord.getUpdateTime()) : p.getCreateTime().before(parentShowAlbumRecord.getUpdateTime()))
                                .collect(Collectors.toList());
                    } else {
                        oldNewsList = allNewsList;
                    }
                    //记一下浏览记录
                    jxtNewsServiceClient.upsertAlbumShowRecord(user.getId(), jxtNewsAlbum.getId(), new Date());
                    map.put("isSubAlbum", Boolean.TRUE);
                    map.put("payed", true);
                } else {
                    //没有订阅的就只取专辑下的东西，不走用户更新逻辑
                    oldNewsList = allNewsList;
                    map.put("isSubAlbum", Boolean.FALSE);
                    map.put("payed", false);
                }
            } else {
                oldNewsList = allNewsList;
            }
        }
        //专辑标签
        List<Long> tagIds = jxtNewsAlbum.getTagList();
        Collection<JxtNewsTag> albumTags = jxtNewsLoaderClient.findTagsByIds(tagIds).values();
        List<Map<String, Object>> tagMapList = albumTags
                .stream()
                .map(t -> MiscUtils.m(
                        "tagId", t.getId(),
                        "tagName", t.getTagName()
                ))
                .collect(Collectors.toList());
        map.put("tagList", tagMapList);
        //专辑内容
        //取最近更新的资讯，按排序数字排序
        List<String> updateNewsIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(updateNewsList)) {
            Comparator<JxtNewsAlbum.NewsRecord> updateComparator = (a, b) -> Integer.compare(SafeConverter.toInt(b.getRank()), SafeConverter.toInt(a.getRank()));
            updateComparator = updateComparator.thenComparing((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
            updateNewsIds = updateNewsList.stream()
                    .sorted(updateComparator)
                    .map(JxtNewsAlbum.NewsRecord::getNewsId)
                    .collect(Collectors.toList());
        }
        //取之前已经在该专辑下的资讯，按排序数字排序
        List<String> oldNewsIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(oldNewsList)) {
            Comparator<JxtNewsAlbum.NewsRecord> oldComparator = (a, b) -> Integer.compare(SafeConverter.toInt(b.getRank()), SafeConverter.toInt(a.getRank()));
            oldComparator = oldComparator.thenComparing((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
            oldNewsIds = oldNewsList.stream()
                    .sorted(oldComparator)
                    .map(JxtNewsAlbum.NewsRecord::getNewsId)
                    .collect(Collectors.toList());
        }
        //Set<String> newsIds = new HashSet<>();
        List<String> newsIds = new ArrayList<>();
        newsIds.addAll(updateNewsIds);
        newsIds.addAll(oldNewsIds);
        //一次把专辑里的资讯都取出来
        Map<String, JxtNews> jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
        //分成两个，用于传给前端
        final List<String> finalUpdateNewsIds = updateNewsIds;
        List<JxtNews> updateNews = jxtNewsByNewsIds.values()
                .stream()
                .filter(p -> finalUpdateNewsIds.stream().anyMatch(p.getId()::contains) && p.getOnline())
                .sorted((o1, o2) -> finalUpdateNewsIds.indexOf(o1.getId()) - finalUpdateNewsIds.indexOf(o2.getId()))
                .collect(Collectors.toList());
        final List<String> finalOldNewsIds = oldNewsIds;
        List<JxtNews> albumNews = jxtNewsByNewsIds.values()
                .stream()
                .filter(p -> finalOldNewsIds.stream().anyMatch(p.getId()::contains) && p.getOnline())
                .sorted((o1, o2) -> finalOldNewsIds.indexOf(o1.getId()) - finalOldNewsIds.indexOf(o2.getId()))
                .collect(Collectors.toList());
        //取专辑里所有的newsId
        Set<String> currentAlbumNewsIds = allNewsList.stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet());
        //取所有的news的阅读数
//        Map<String, Long> currentAlbumNewsReadCount = vendorCacheClient.getParentJxtCacheManager().loadReadCount(currentAlbumNewsIds);
//        Map<String, Long> currentAlbumNewsReadCount = asyncNewsCacheService
//                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, currentAlbumNewsIds)
//                .take();
        Map<String, Long> currentAlbumNewsReadCount = new HashMap<>();
        //最近更新的资讯
        List<Map<String, Object>> updateNewsMapList = generateAlbumNewsMap(updateNews, updateNewsList, currentAlbumNewsReadCount);
        //之前加入的资讯
        List<Map<String, Object>> albumNewsMapList = generateAlbumNewsMap(albumNews, oldNewsList, currentAlbumNewsReadCount);
        map.put("updateNewsMapList", updateNewsMapList);
        map.put("albumNewsMapList", albumNewsMapList);
        Long newsReadCountSum = currentAlbumNewsReadCount.values().stream().mapToLong(e -> e).sum();
        //专辑阅读数
//        Long readCount = MapUtils.getLong(albumReadCountMap, jxtNewsAlbum.getId(), 0L);
//        map.put("readCount", JxtNewsUtil.countFormat(readCount + newsReadCountSum));
        //专辑期数
        map.put("number", CollectionUtils.isEmpty(allNewsList) ? 0 : allNewsList.size());
        //取相关推荐的专辑
        List<JxtNewsAlbum> jxtNewsAlbumList = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum();
        jxtNewsAlbumList = jxtNewsAlbumList.stream()
                .filter(p -> CollectionUtils.isNotEmpty(p.getTagList()) && p.getTagList().stream().anyMatch(tagIds::contains))
                .collect(Collectors.toList());
        //随机
        Collections.shuffle(jxtNewsAlbumList);
        //取三篇，不足三篇都取出来
        jxtNewsAlbumList = jxtNewsAlbumList.subList(0, jxtNewsAlbumList.size() > 3 ? 3 : jxtNewsAlbumList.size());
        List<Map<String, Object>> relativeAlbums = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(jxtNewsAlbumList)) {
            Set<String> albumIds = jxtNewsAlbumList.stream().map(JxtNewsAlbum::getId).collect(Collectors.toSet());
            Set<String> relativeNewsIds = new HashSet<>();
            //取相关推荐的newsId
            for (JxtNewsAlbum p : jxtNewsAlbumList) {
                relativeNewsIds.addAll(p.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet()));
            }
            //取相关推荐中所有已上线的资讯
            Map<String, JxtNews> relativeNewsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(relativeNewsIds);
            List<JxtNews> relativeNewsList;
            if (MapUtils.isNotEmpty(relativeNewsMap)) {
                relativeNewsList = relativeNewsMap.values().stream().filter(JxtNews::getOnline).collect(Collectors.toList());
                relativeNewsIds = relativeNewsList.stream().map(JxtNews::getId).collect(Collectors.toSet());
            }
            //取相关推荐中文章的阅读数
//            Map<String, Long> relativeNewsReadCountMap = vendorCacheClient.getParentJxtCacheManager().loadReadCount(relativeNewsIds);
//            Map<String, Long> relativeNewsReadCountMap = asyncNewsCacheService
//                    .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, relativeNewsIds)
//                    .take();
            //取相关推荐的阅读数、订阅数
//            Map<String, Long> relativeReadCountMap = vendorCacheClient.getParentJxtCacheManager().loadAlbumReadCount(albumIds);
//            Map<String, Long> relativeReadCountMap = asyncNewsCacheService
//                    .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_READ_COUNT, albumIds)
//                    .take();
//            Map<String, Long> relativeSubCountMap = vendorCacheClient.getParentJxtCacheManager().loadAlbumSubCount(albumIds);
//            Map<String, Long> relativeSubCountMap = asyncNewsCacheService
//                    .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_SUB_COUNT, albumIds)
//                    .take();
            final Set<String> finalRelativeNewsIds = relativeNewsIds;
            jxtNewsAlbumList.forEach(t -> {
                Map<String, Object> relativeMap = new HashMap<>();
                //专辑id
                relativeMap.put("albumId", t.getId());
                //专辑名称
                relativeMap.put("title", t.getTitle());
                //专辑封面
                relativeMap.put("imgUrl", combineCdbUrl(t.getHeadImg()));
                //专辑简介
                relativeMap.put("digest", t.getDetail());
                Long relativeNewsReadCount = 0L;
                //取该专辑的newsId
//                Set<String> relativeIds = relativeNewsReadCountMap.keySet().stream().filter(p -> t.getNewsRecordList().stream().anyMatch(o -> StringUtils.equals(o.getNewsId(), p))).collect(Collectors.toSet());
//                for (String newsId : relativeIds) {
//                    relativeNewsReadCount += SafeConverter.toLong(relativeNewsReadCountMap.get(newsId));
//                }
//                Long relativeReadCount = SafeConverter.toLong(relativeReadCountMap.get(t.getId()));
                //专辑阅读数
//                relativeMap.put("readCount", JxtNewsUtil.countFormat(relativeReadCount + relativeNewsReadCount));
                //专辑订阅数
//                Long relativeSubCount = SafeConverter.toLong(relativeSubCountMap.get(t.getId()));
//                relativeMap.put("subAlbumCount", JxtNewsUtil.countFormat(relativeSubCount));
                //专辑期数
                Long totalCount = finalRelativeNewsIds.stream().filter(o -> t.getNewsRecordList().stream().anyMatch(p -> StringUtils.equals(p.getNewsId(), o))).count();
                relativeMap.put("number", SafeConverter.toInt(totalCount));
                relativeAlbums.add(relativeMap);
            });
            map.put("relativeMapList", relativeAlbums);
        }

        return map;
    }

    private List<Map<String, Object>> generateAlbumNewsMap(Collection<JxtNews> list, List<JxtNewsAlbum.NewsRecord> recordList, Map<String, Long> countMap) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return mapList;
        }
        Map<String, Date> recordTimeMap = new HashMap<>();
        Map<String, Integer> recordRankMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(recordList)) {
            recordTimeMap = recordList.stream().collect(Collectors.toMap(JxtNewsAlbum.NewsRecord::getNewsId, JxtNewsAlbum.NewsRecord::getCreateTime));
            recordRankMap = recordList.stream().collect(Collectors.toMap(JxtNewsAlbum.NewsRecord::getNewsId, JxtNewsAlbum.NewsRecord::getRank));
        }
        final Map<String, Date> finalRecordTimeMap = recordTimeMap;
        final Map<String, Integer> finalRecordRankMap = recordRankMap;
        list.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            //资讯id
            map.put("newsId", e.getId());
            //资讯标题
            map.put("title", e.getTitle());
            //资讯类型
            map.put("contentType", e.generateContentType());
            //是否仅付费可见
            map.put("free", e.getFree());
            //音频、视频的播放时长
            if (StringUtils.isNotBlank(e.getPlayTime())) {
                map.put("playTime", JxtNewsUtil.formatTime(SafeConverter.toInt(e.getPlayTime())));
            }
            //取文章的更新时间
            map.put("createTime", e.getPushTime() != null && finalRecordTimeMap.get(e.getId()) != null && e.getPushTime().after(finalRecordTimeMap.get(e.getId())) ? DateUtils.dateToString(e.getPushTime(), "MM-dd") : DateUtils.dateToString(finalRecordTimeMap.get(e.getId()), "MM-dd"));
            //取该资讯的阅读数
//            map.put("readCount", JxtNewsUtil.countFormat(SafeConverter.toLong(countMap.get(e.getId()))));
            //1.9.0先取专辑中文章的rank做集数
            if (finalRecordRankMap.get(e.getId()) != null) {
                map.put("episode", finalRecordRankMap.get(e.getId()));
            }
            mapList.add(map);
        });
        return mapList;
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

}
