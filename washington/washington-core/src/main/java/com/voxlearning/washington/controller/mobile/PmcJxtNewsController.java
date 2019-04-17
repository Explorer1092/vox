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
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.business.api.ParentNewsService;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.entity.misc.ZyParentNewsArticle;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.oa.UserOfficialAccountsRef;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.AsyncNewsCacheService;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.washington.athena.RecommendedServiceClient;
import com.voxlearning.washington.controller.open.v1.util.JxtNewsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SkipCacheControl;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * @author shiwei.liao
 * @since 2016-9-27
 */
@Controller
@Slf4j
@RequestMapping(value = "/pmc/")
public class PmcJxtNewsController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = AsyncNewsCacheService.class)
    private AsyncNewsCacheService asyncNewsCacheService;

    @Inject
    private RecommendedServiceClient recommendedServiceClient;

    @ImportService(interfaceClass = ParentNewsService.class)
    private ParentNewsService parentNewsService;


    //资讯详情页需要缓存到cdn的数据
    @RequestMapping(value = "pmc_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    @SkipCacheControl
    public MapMessage getJxtNewsCdnData() {
        String newsId = getRequestString("newsId");

        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage("资讯ID不能为空");
        }

        JxtNews jxtNews;
        // 兼容旧的数据
        String contentType = getRequestString("content_type");
        String styleType = getRequestString("style_type");
        if (contentType.equals(JxtNewsStyleType.OFFICIAL_ACCOUNT.toString())
                || styleType.equals(JxtNewsStyleType.OFFICIAL_ACCOUNT.toString())) {
            jxtNews = jxtNewsLoaderClient.getJxtNewsWithoutBuffer(newsId);
        } else {
            jxtNews = jxtNewsLoaderClient.getJxtNews(newsId);
        }

        if (jxtNews == null) {
            return MapMessage.errorMessage("您要查看的资讯不存在");
        }

        MapMessage mapMessage = MapMessage.successMessage();
        try {
            handleJxtNewsCdnData(jxtNews, getRequest(), getResponse(), mapMessage);
        } catch (Exception e) {
            logger.error("pmc_data error", e);
        }

        return mapMessage;
    }

    //资讯详情页获取除正文外的动态数据
    @RequestMapping(value = "pmc_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsRelationData() {
        String newsId = getRequestString("newsId");
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage("资讯ID不能为空");
        }

        JxtNews jxtNews;
        String contentType = getRequestString("content_type");
        String styleType = getRequestString("style_type");
        if (contentType.equals(JxtNewsStyleType.OFFICIAL_ACCOUNT.toString())
                || styleType.equals(JxtNewsStyleType.OFFICIAL_ACCOUNT.toString())) {
            jxtNews = jxtNewsLoaderClient.getJxtNewsWithoutBuffer(newsId);
        } else {
            jxtNews = jxtNewsLoaderClient.getJxtNews(newsId);
        }

        if (jxtNews == null) {
            return MapMessage.errorMessage("您要查看的资讯不存在");
        }
        asyncNewsCacheService.JxtNewsCacheManager_incrCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, jxtNews.getId());
        User user = currentUser();
        Map<String, Object> jxtNewsDetailMap;

        if (contentType.equals(JxtNewsStyleType.OFFICIAL_ACCOUNT.toString()) ||
                styleType.equals(JxtNewsStyleType.OFFICIAL_ACCOUNT.toString())) {
            jxtNewsDetailMap = generateOfficialAccountDetails(jxtNews, user);
        }
        // 资讯列表里面的公众号文章，即要有公众号的头信息，又要有评论和相关推荐
        else if (contentType.equals(JxtNewsStyleType.OFFICIAL_ACCOUNT_SUBMIT.toString()) ||
                styleType.equals(JxtNewsStyleType.OFFICIAL_ACCOUNT_SUBMIT.toString())) {
            jxtNewsDetailMap = generateJxtNewsDetailMap(jxtNews, user);
            jxtNewsDetailMap.putAll(generateOfficialAccountDetails(jxtNews, user));
        } else {
            jxtNewsDetailMap = generateJxtNewsDetailMap(jxtNews, user);
        }
        LogCollector.info("jxt_news_read_record", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "userid", user == null ? "" : user.getId(),
                "userType", user == null ? "" : user.getUserType(),
                "newsid", jxtNews.getId(),
                "albumId", jxtNews.getAlbumId(),
                "createtime", new Date().getTime()));
        return MapMessage.successMessage().add("jxtNewsDetailMap", jxtNewsDetailMap);
    }

    /**
     * 获得公众号的详细信息
     *
     * @return
     */
    @RequestMapping(value = "/pmc_account_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadAccountDetail() {

        Long accountId = getRequestLong("accountId");
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在");

        MapMessage resultMsg = MapMessage.successMessage();
        resultMsg.add("accountInfo", accounts);

        if (currentUser() == null) {
            resultMsg.add("isFollowed", false);
        } else {
            resultMsg.add("isFollowed", officialAccountsServiceClient.isFollow(
                    accountId, currentUser().getId()));
        }

        return resultMsg;
    }

    @RequestMapping(value = "/follow_official_account.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage followOfficialAccount() {

        if (currentUser() == null)
            return MapMessage.errorMessage("未登录，无法关注!");

        Long accountId = getRequestLong("accountId");
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在");

        boolean status = getRequestBool("followStatus", false);
        return officialAccountsServiceClient.updateFollowStatus(
                currentUser().getId(),
                accountId,
                status ? UserOfficialAccountsRef.Status.Follow : UserOfficialAccountsRef.Status.UnFollow);
    }

    /**
     * 生成公众号的详细页面
     *
     * @param news
     * @param user
     * @return
     */
    private Map<String, Object> generateOfficialAccountDetails(JxtNews news, User user) {
        Map<String, Object> detailMap = new HashMap<>();

        Long accountId = SafeConverter.toLong(news.getRelateId());
        detailMap.put("accountId", accountId);

        detailMap.put("sourceUrl", news.getSourceUrl());
        detailMap.put("accountName", news.getRelateContent());

//        Map<String, Long> readCountMap = vendorCacheClient.getParentJxtCacheManager().loadReadCount(Collections.singletonList(news.getId()));
//        Map<String, Long> readCountMap = asyncNewsCacheService
//                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, Collections.singletonList(news.getId()))
//                .take();

//        Long readCount = MapUtils.getLong(readCountMap, news.getId(), 0L);
//        detailMap.put("readCount", JxtNewsUtil.countFormat(readCount));

        if (user == null) {
            detailMap.put("isFollowed", false);
        } else {
            detailMap.put("isFollowed", officialAccountsServiceClient.isFollow(accountId, user.getId()));
        }

        detailMap.put("free", null == news.getFree() ? true : news.getFree());
        return detailMap;
    }


    private Boolean validateRequestMethod(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"GET".equals(request.getMethod())) {
            String responseStr = "Only GET method supported";
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            response.setContentLength(responseStr.length());
            response.getWriter().write(responseStr);
            return false;
        }
        return true;
    }

    private Boolean validatePmcData(Object data, HttpServletResponse response) throws IOException {
        if (null == data) {
            String responseStr = "No context found for request";
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            response.setContentLength(responseStr.length());
            response.getWriter().write(responseStr);
            return false;
        }

        return true;
    }


    //资讯详情页CDN接口数据
    private void handleJxtNewsCdnData(JxtNews jxtNews,
                                      HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse,
                                      MapMessage mapMessage) throws IOException, ServletException {
        //only supports 'GET' method
        if (!validateRequestMethod(httpServletRequest, httpServletResponse)) {
            return;
        }

        if (!validatePmcData(jxtNews, httpServletResponse)) {
            return;
        }

        ZyParentNewsArticle parentNewsArticle = parentNewsService.loadArticleById(jxtNews.getArticleId());

        Date update = jxtNews.getUpdateTime();

        long ims = httpServletRequest.getDateHeader("If-Modified-Since");
        if (ims >= 0 && update != null && update.getTime() <= ims + 999) {//1秒内的误差忽略
            httpServletResponse.setStatus(SC_NOT_MODIFIED);
            httpServletResponse.flushBuffer();
            return;
        }
        mapMessage.add("title", jxtNews.getTitle());
        mapMessage.add("source", jxtNews.getSource());
        mapMessage.add("sourceUrl", jxtNews.getSourceUrl());
        //这里是此请求的是一个专题
        if (JxtNewsContentType.SUBJECT == jxtNews.getJxtNewsContentType()) {
            mapMessage.add("jxtNewsContentType", JxtNewsContentType.SUBJECT.name());
            mapMessage.add("content", generateSubjectDetailJsonString(jxtNews));
        } else {
            //这里此请求的是一篇资讯。但是如果属于某个专题会附带这篇资讯的的一个专题Id
            //如果这篇资讯属于某个专题，前端会把专题id传过来，后端需要把专题的头部区域和尾部区域内容与文章内容一起返回给前端；
            String subjectId = getRequestString("subjectId");
            String content;
            String albumNewsContent = "";
            if (StringUtils.isBlank(subjectId)) {
                content = parentNewsArticle.getContent();
                if (parentNewsArticle.getContent_by_album_news() != null) {
                    albumNewsContent = parentNewsArticle.getContent_by_album_news();
                }
            } else {
                JxtNewsSubject subject = jxtNewsLoaderClient.getJxtNewsSubject(subjectId);
                String headContent = subject == null ? "" : SafeConverter.toString(subject.getHeadContent(), "");
                String tailContent = subject == null ? "" : SafeConverter.toString(subject.getTailContent(), "");
                content = headContent + parentNewsArticle.getContent() + tailContent;
            }

            mapMessage.add("content", content);
            mapMessage.add("albumNewsContent", albumNewsContent);
        }
        mapMessage.add("serverTime", new Date().toString());


        if (update != null) {
            httpServletResponse.addDateHeader("Last-Modified", update.getTime());
        }

        // 外部关联业务的id和文案
        if (StringUtils.isNotEmpty(jxtNews.getRelateId())) {
            mapMessage.put("relateId", jxtNews.getRelateId());
            mapMessage.put("relateContent", jxtNews.getRelateContent());
        }

        httpServletResponse.setStatus(SC_OK);
    }

    @RequestMapping(value = "pmc_gray.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage isHitPmcGray() {
        return MapMessage.successMessage().add("isHit", Boolean.TRUE);
    }


    //资讯详情页动态数据
    private Map<String, Object> generateJxtNewsDetailMap(JxtNews jxtNews, User user) {
        String parent_other_icon = "/public/skin/parentMobile/images/dynamic/avatar_parents_other.png";
        Map<String, Object> map = new HashMap<>();
        if (jxtNews == null) {
            return map;
        }
        String newsId = jxtNews.getId();
        map.put("free", null == jxtNews.getFree() ? true : jxtNews.getFree());
        //标题
        map.put("title", jxtNews.getTitle());
        //老师推荐
        if (user != null && user.isParent()) {
            Map<Long, List<JxtNewsTeacherRecommend>> recommendByParentMap = generateTeacherRecommendByParent(user);
            List<JxtNewsTeacherRecommend> teacherRecommends = new ArrayList<>();
            if (MapUtils.isNotEmpty(recommendByParentMap)) {
                for (Map.Entry<Long, List<JxtNewsTeacherRecommend>> recommend : recommendByParentMap.entrySet()) {
                    if (CollectionUtils.isNotEmpty(recommend.getValue())) {
                        teacherRecommends.addAll(recommend.getValue().stream().filter(e -> StringUtils.equals(jxtNews.getId(), e.getRecommendId())).collect(Collectors.toList()));
                    }
                }
                if (CollectionUtils.isNotEmpty(teacherRecommends)) {
                    Teacher teacher = teacherLoaderClient.loadTeacher(teacherRecommends.get(0).getTeacherId());
                    if (teacher != null) {
                        map.put("teacherName", teacher.fetchRealname());
                    }
                }
            }
        }
        //发布时间（这里取的是更新时间）
        Date publishDate = jxtNews.getCreateTime();
        map.put("publishDate", com.voxlearning.alps.calendar.DateUtils.dateToString(publishDate, com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE));

        //文章来源
        map.put("source", jxtNews.getSource());

        //原文链接
        map.put("sourceUrl", jxtNews.getSourceUrl());

        //是否显示广告
        map.put("showAd", jxtNews.getShowAd() == null ? Boolean.FALSE : jxtNews.getShowAd());


        //标签默认显示在下面
        map.put("tagPosition", "down");
        List<Long> tagList = Optional.ofNullable(jxtNews.getTagList()).orElse(new ArrayList<>());

        List<Map<String, Object>> relativeList = new ArrayList<>();
        //相关阅读只取三篇
        Set<String> newsIds = new HashSet<>();
        List<JxtNews> relativeNewsList = new ArrayList<>();
        Boolean isBigData = Boolean.FALSE;
        List<String> newsIdList = new ArrayList<>();
        if (RuntimeMode.gt(Mode.TEST)) {
            newsIdList = recommendedServiceClient.getRecommendedService()
                    .getRelatedContent(user != null ? user.getId() : null, jxtNews.getId(), 0, 3);
        }
        if (CollectionUtils.isNotEmpty(newsIdList)) {
            newsIds.addAll(newsIdList);
            Map<String, JxtNews> jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
            if (MapUtils.isNotEmpty(jxtNewsByNewsIds)) {
                relativeNewsList = jxtNewsByNewsIds.values().stream().collect(Collectors.toList());
            }
            isBigData = Boolean.TRUE;
        } else {
            relativeNewsList = generateRelativeNews(jxtNews, user, tagList);
            newsIds = relativeNewsList.stream().map(JxtNews::getId).collect(Collectors.toSet());
        }
        //把原文加进去一起查出来
        newsIds.add(newsId);
//        //各种数据
        if (jxtNews.getJxtNewsContentType() != JxtNewsContentType.SUBJECT) {
            Set<String> albumIds = relativeNewsList.stream().filter(p -> StringUtils.isNotBlank(p.getAlbumId())).map(JxtNews::getAlbumId).collect(Collectors.toSet());
            Map<String, JxtNewsAlbum> newsAlbumMap = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds);
            for (JxtNews news : relativeNewsList) {
                Map<String, Object> relativeMap = new HashMap<>();
                relativeMap.put("newsId", news.getId());
                relativeMap.put("title", news.getTitle());
                relativeMap.put("img", news.getCoverImgList());
                relativeMap.put("jxt_news_type", news.getJxtNewsType());
                //文章的内容类型
                if (news.getJxtNewsContentType() != null) {
                    relativeMap.put("jxt_news_content_type", news.getJxtNewsContentType());
                } else {
                    relativeMap.put("jxt_news_content_type", "");
                }
                //相关阅读列表的角标
                if (StringUtils.isNotBlank(news.getAlbumId()) && newsAlbumMap.get(news.getAlbumId()) != null && newsAlbumMap.get(news.getAlbumId()).getOnline()) {
                    relativeMap.put("album_id", news.getAlbumId());
                    relativeMap.put("subscript", "album");
                } else if (news.generatePushType() == 3) {
                    relativeMap.put("subscript", "location");
                } else if (!StringUtils.equals(news.generateContentType(), JxtNewsContentType.UNKNOWN.name())) {
                    relativeMap.put("subscript", news.generateContentType());
                }
                relativeMap.put("source", news.getSource());
                String digest = news.getDigest();
                if (news.getJxtNewsType() == JxtNewsType.TEXT) {
                    relativeMap.put("digest", digest);
                }
                relativeMap.put("is_big_data", isBigData);
                relativeMap.put("update_time", DateUtils.dateToString(news.getUpdateTime(), DateUtils.FORMAT_SQL_DATE));
                relativeList.add(relativeMap);
            }
        }

        map.put("relativeList", relativeList);
        //原文标签列表
        Collection<JxtNewsTag> newsTags = jxtNewsLoaderClient.findTagsByIds(tagList).values();
        List<Map<String, Object>> newsTagsMapList = new ArrayList<>();
        newsTags.forEach(p -> {
            Map<String, Object> tagIdNameMap = new HashMap<>();
            tagIdNameMap.put("tagId", p.getId());
            tagIdNameMap.put("tagName", p.getTagName());
            newsTagsMapList.add(tagIdNameMap);
        });

        map.put("tagList", newsTagsMapList);
        //精选留言
        List<Map<String, Object>> commentMapList = new ArrayList<>();
        List<JxtNewsComment> jxtNewsCommentList = jxtNewsLoaderClient.getCommentListByTypeId(newsId);
        jxtNewsCommentList = jxtNewsCommentList.stream()
                .filter(e -> (!e.getIsDisabled() && e.getIsShow()) || (!e.getIsDisabled() && user != null && Objects.equals(user.getId(), e.getUserId())))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .limit(50)
                .collect(Collectors.toList());
        Set<String> commentIds = jxtNewsCommentList.stream().map(JxtNewsComment::getId).collect(Collectors.toSet());
        Map<String, Long> commentIdVoteMap = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_COMMENT_VOTE_COUNT, commentIds)
                .take();
        Set<Long> userIds = jxtNewsCommentList.stream().map(JxtNewsComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
        Map<Long, List<Clazz>> parentClazzListMap = deprecatedClazzLoaderClient.getRemoteReference().loadParentClazzs(userIds);
        Map<Long, List<StudentParentRef>> parentStudentMap = parentLoaderClient.loadParentStudentRefs(userIds);
        //这里开始取评论者的城市信息
        Map<Long, Long> parentAndFirstChild = parentStudentMap.values().stream().filter(CollectionUtils::isNotEmpty).map(studentParentRefs -> studentParentRefs.get(0)).collect(Collectors.toMap(StudentParentRef::getParentId, StudentParentRef::getStudentId));
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(parentAndFirstChild.values());
        Map<Long, Integer> parentCodeMap = new HashMap<>();
        //用studentDetail的Citycode获取城市信息
        for (StudentDetail studentDetail : studentDetailMap.values()) {
            parentAndFirstChild.entrySet().stream().filter(parentStudent -> Objects.equals(studentDetail.getId(), parentStudent.getValue())).forEach(parentStudent -> parentCodeMap.put(parentStudent.getKey(), studentDetail.getCityCode()));
        }
        Map<Long, String> parentCityMap = new HashMap<>();
        //对应评论者和城市信息
        for (Map.Entry<Long, Integer> parentCityCode : parentCodeMap.entrySet()) {
            ExRegion firstChildExRegion = raikouSystem.loadRegion(parentCityCode.getValue());
            if (firstChildExRegion != null) {
                String cityName = firstChildExRegion.getName();
                parentCityMap.put(parentCityCode.getKey(), cityName);
            }
        }

        //所有评论的官方回复
        Map<String, List<JxtNewsComment>> commentListMap = jxtNewsLoaderClient.getCommentListByTypeIds(commentIds);
        Set<String> replyIds = commentListMap.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .stream()
                .filter(e -> e.getUserId() == null)
                .map(JxtNewsComment::getId)
                .collect(Collectors.toSet());

        //所有官方回复的点赞map
//        Map<String, Long> replyVoteMap = vendorCacheClient.getParentJxtCacheManager().loadVoteCount(replyIds);
        Map<String, Long> replyVoteMap = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_COMMENT_VOTE_COUNT, replyIds)
                .take();

        //评论和官方回复的对应map
        Map<String, String> commentReplyMap = new HashMap<>();
        commentListMap.entrySet().forEach(e -> {
            if (CollectionUtils.isNotEmpty(e.getValue())) {
                commentReplyMap.put(e.getKey(), e.getValue().get(0).getId());
            }
        });

        Map<JxtNewsComment, Long> commentVoteMap = new HashMap<>();
        //评论排序要按照评论和官方回复的点赞之和倒序
        jxtNewsCommentList.forEach(e -> {
            long commentVoteCount = SafeConverter.toLong(commentIdVoteMap.get(e.getId()));
            String replyId = SafeConverter.toString(commentReplyMap.get(e.getId()), "");
            long replyVoteCount = SafeConverter.toLong(replyVoteMap.get(replyId));
            commentVoteMap.put(e, commentVoteCount + replyVoteCount);
        });

        //评论按评论点赞数和官方回复点赞之和倒序排序、点赞数相同按时间排序
        List<Map.Entry<JxtNewsComment, Long>> entryList = new ArrayList<>(commentVoteMap.entrySet());
        Comparator<Map.Entry<JxtNewsComment, Long>> c = (o1, o2) -> o2.getValue().compareTo(o1.getValue());
        c = c.thenComparing((o1, o2) -> o2.getKey().getCreateTime().compareTo(o1.getKey().getCreateTime()));

        entryList = entryList.stream().sorted(c).collect(Collectors.toList());
        List<JxtUserVoteRecord> voteRecords = new ArrayList<>();
        if (user != null) {
            //是否收藏过
            JxtNewsCollection jxtNewsCollection = jxtNewsLoaderClient.loadCollectionRecord(String.valueOf(user.getId()));
            map.put("hasCollected", jxtNewsCollection != null && CollectionUtils.isNotEmpty(jxtNewsCollection.getColNewsIds()) && jxtNewsCollection.getColNewsIds().stream().anyMatch(p -> p.equals(jxtNews.getId())));
            //点赞记录
            voteRecords = jxtLoaderClient.getVoteRecordByUserId(user.getId());
        }
        final List<JxtUserVoteRecord> userTotalVoteList = voteRecords;

        //逐条处理评论
        entryList.forEach(e -> {
            Map<String, Object> commentMap = new HashMap<>();
            JxtNewsComment jxtNewsComment = e.getKey();
            String commentId = jxtNewsComment.getId();
            String userName = jxtNewsComment.getUserName();
            String comment = jxtNewsComment.getComment();
            Date publishTime = jxtNewsComment.getCreateTime();
            Long commentVoteCount = SafeConverter.toLong(commentIdVoteMap.get(e.getKey().getId()));
            Long userId = jxtNewsComment.getUserId();
            User commentUser = userMap.get(userId);
            if (commentUser.isParent() && CollectionUtils.isNotEmpty(parentClazzListMap.entrySet())) {
                if (CollectionUtils.isNotEmpty(parentClazzListMap.get(userId))) {
                    String clazzLevel = parentClazzListMap.get(userId).get(0).getClazzLevel().getDescription();
                    commentMap.put("clazzLevel", clazzLevel);
                }

            }

            if (commentUser.isParent() && StringUtils.isNotBlank(parentCityMap.get(userId))) {
                String cityName = parentCityMap.get(userId);
                commentMap.put("cityName", cityName);
            }

            String imgUrl = commentUser.getProfile().getImgUrl();

            //官方回复
            List<JxtNewsComment> commentReplyList = commentListMap.containsKey(commentId) ? commentListMap.get(commentId) : Collections.emptyList();

            //这里用o.getUserId() == null来判断是否是官方回复了。有点略坑。再想想吧。
            JxtNewsComment commentReply = CollectionUtils.isNotEmpty(commentReplyList) ? commentReplyList.stream().filter(o -> o.getUserId() == null).collect(Collectors.toList()).get(0) : null;
            if (commentReply != null) {
                if (!commentReply.getIsDisabled()) {
                    commentMap.put("reply", commentReply.getComment());
                    commentMap.put("reply_time", DateUtils.dateToString(commentReply.getCreateTime(), "MM-dd HH:mm"));
                    commentMap.put("reply_name", "家长通小编回复");
                    commentMap.put("reply_vote_count", SafeConverter.toLong(replyVoteMap.get(commentReply.getId())));
                    commentMap.put("reply_id", commentReply.getId());
                    //是否点赞过回复
                    String replyTypeAndId = JxtUserVoteRecord.generateTypeAndId(JxtVoteType.JXT_NEWS_COMMENT, commentReply.getId());
                    commentMap.put("reply_has_voted", userTotalVoteList.stream().anyMatch(p -> replyTypeAndId.equals(p.getTypeAndId())));
                }
            }

            //展示回复的条件：回复存在，回复isShow是true或者这是自己的评论
            commentMap.put("is_show", commentReply != null && (SafeConverter.toBoolean(commentReply.getIsShow()) || (user != null && Objects.equals(userId, user.getId()))));

            commentMap.put("icon", StringUtils.isNotBlank(imgUrl) ? getUserAvatarImgUrl(imgUrl) : getCdnBaseUrlStaticSharedWithSep() + parent_other_icon);
            commentMap.put("commentId", commentId);
            if (user != null && Objects.equals(userId, user.getId())) {
                commentMap.put("is_by_me", Boolean.TRUE);
            } else {
                commentMap.put("is_by_me", Boolean.FALSE);
            }
            commentMap.put("userName", userName);
            commentMap.put("comment", comment);
            commentMap.put("publishTime", com.voxlearning.alps.calendar.DateUtils.dateToString(publishTime, "MM-dd HH:mm"));
            commentMap.put("commentVoteCount", JxtNewsUtil.countFormat(commentVoteCount));
            //是否点赞过
            String typeAndId = JxtUserVoteRecord.generateTypeAndId(JxtVoteType.JXT_NEWS_COMMENT, commentId);
            commentMap.put("hasVoted", userTotalVoteList.stream().anyMatch(p -> typeAndId.equals(p.getTypeAndId())));
            if (commentMap.get("is_by_me").equals(Boolean.TRUE)) {
                commentMapList.add(0, commentMap);
            } else {
                commentMapList.add(commentMap);
            }
        });
        map.put("commentList", commentMapList);


        //加群文案和群号
        map.put("chat_group_content", jxtNews.getChatGroupWelcomeContent());
        map.put("chat_group_id", jxtNews.getChatGroupId());
        map.put("online", jxtNews.getOnline());
        return map;
    }

    private String generateSubjectDetailJsonString(JxtNews jxtNews) {
        User user = currentUser();
        Long userId = user == null ? null : user.getId();
        Set<Integer> parentRegionIds = new HashSet<>();
        if (user != null) {
            Set<Long> studentIds = studentLoaderClient.loadParentStudents(user.getId()).stream().map(User::getId).collect(Collectors.toSet());
            Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
            for (StudentDetail studentDetail : studentDetails) {
                parentRegionIds.add(studentDetail.getStudentSchoolRegionCode());
                parentRegionIds.add(studentDetail.getCityCode());
                parentRegionIds.add(studentDetail.getRootRegionCode());
            }
        }

        Map<String, Object> map = new HashMap<>();
        String subjectId = jxtNews.getArticleId();
        JxtNewsSubject subject = jxtNewsLoaderClient.getJxtNewsSubject(subjectId);
        if (subject == null) {
            return "";
        }

        //专题标题
        map.put("subjectTitle", subject.getTitle());

        //专题id
        map.put("subject_id", subjectId);

        //头图
        map.put("headImg", StringUtils.isBlank(subject.getHeadImg()) ? "" : combineCdbUrl(subject.getHeadImg()));

        //引言
        map.put("introduction", StringUtils.isBlank(subject.getIntroduction()) ? "" : subject.getIntroduction());

        //内容分类
        //分类和分类排名的map
        Map<String, Integer> categoryRankMap = subject.getCategoryRankMap();
        if (MapUtils.isNotEmpty(categoryRankMap)) {
            List<String> categoryList = new ArrayList<>();
            categoryRankMap.entrySet().stream()
                    .sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o1.getValue()), SafeConverter.toInt(o2.getValue())))
                    .forEach(e -> categoryList.add(e.getKey()));


            //资讯分类列表
            //每个分类名对应的资讯列表map
            Map<String, List<String>> categoryNewsMap = subject.getCategoryNewsMap();
            Set<String> allNewsIds = categoryNewsMap.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            List<JxtNews> newsList = jxtNewsLoaderClient.getJxtNewsByNewsIds(allNewsIds).values()
                    .stream()
                    .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                            || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && userId != null && userId.equals(p.getAvailableUserId()))
                            || (p.generatePushType().equals(JxtNewsPushType.REGION.getType()) && parentRegionIds.stream().anyMatch(e -> p.getRegionCodeList().contains(e))))
                    .filter(e -> e.getJxtNewsType() != JxtNewsType.UNKNOWN)
                    .collect(Collectors.toList());

            Map<String, JxtNews> jxtNewsMap = newsList.stream()
                    .collect(Collectors.toMap(JxtNews::getId, Function.identity()));

//            Map<String, Long> readCountMap = vendorCacheClient.getParentJxtCacheManager().loadReadCount(allNewsIds);
//            Map<String, Long> readCountMap = asyncNewsCacheService
//                    .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, allNewsIds)
//                    .take();
            //每篇资讯以及资讯排序对应的map
            Map<String, Integer> newsRankMap = subject.getNewsRankMap();
            List<Map<String, Object>> jxtNewsList = new ArrayList<>();
            //分类列表
            List<String> categoryNameList = new ArrayList<>();
            categoryList.forEach(e -> {
                Map<String, Object> categoryMap = new HashMap<>();


                //该分类下的所有资讯
                List<String> newsIds = categoryNewsMap.get(e);
                //该分类下的所有资讯及资讯排序对应map
                Map<String, Integer> rankMap = new LinkedHashMap<>();
                newsIds.forEach(p -> {
                    if (newsRankMap.keySet().contains(p)) {
                        rankMap.put(p, newsRankMap.get(p));
                    }
                });
                List<Map<String, Object>> jxtNewsDetailList = new ArrayList<>();
                rankMap.entrySet().stream()
                        .sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o1.getValue()), SafeConverter.toInt(o2.getValue())))
                        .forEach(m -> {
                            String newsId = m.getKey();
                            JxtNews news = jxtNewsMap.get(newsId);
                            if (news != null && news.getOnline()) {
                                Map<String, Object> jxtNewsDetailMap = new HashMap<>();
                                jxtNewsDetailMap.put("newsId", newsId);
                                jxtNewsDetailMap.put("jxt_news_content_type", news.getJxtNewsContentType() == null ? "" : news.getJxtNewsContentType().name());
                                jxtNewsDetailMap.put("jxtNewsTitle", news.getTitle());
                                String digest = news.getDigest();
                                if (news.getJxtNewsType() == JxtNewsType.TEXT) {
                                    jxtNewsDetailMap.put("digest", digest);
                                }

                                if (news.generatePushType() == 3 && news.getJxtNewsContentType() == JxtNewsContentType.IMG_AND_TEXT) {
                                    jxtNewsDetailMap.put("subscript", "location");
                                } else if (!StringUtils.equals(news.generateContentType(), JxtNewsContentType.UNKNOWN.name())) {
                                    jxtNewsDetailMap.put("subscript", news.getJxtNewsContentType());
                                } else if (StringUtils.isNotBlank(news.getAlbumId())) {
                                    jxtNewsDetailMap.put("subscript", "album");
                                }
//                                Long readCount = MapUtils.getLong(readCountMap, newsId, 0L);
//                                jxtNewsDetailMap.put("readCount", JxtNewsUtil.countFormat(readCount));
                                jxtNewsDetailMap.put("source", news.getSource());
                                jxtNewsDetailMap.put("jxt_news_type", news.getJxtNewsType().name());
                                List<String> imgList = news.getCoverImgList();
                                List<String> imgUrlList = new ArrayList<>();
                                if (com.voxlearning.alps.core.util.CollectionUtils.isNotEmpty(imgList)) {
                                    imgList.forEach(i -> imgUrlList.add(generatePmcAliYunImgUrl(i)));
                                }
                                jxtNewsDetailMap.put("imgList", imgUrlList);
                                jxtNewsDetailMap.put("updateTime", news.getUpdateTime().getTime());
                                jxtNewsDetailList.add(jxtNewsDetailMap);
                            }
                        });
                if (jxtNewsDetailList.size() > 0) {
                    categoryNameList.add(e);
                    categoryMap.put("categoryName", e);
                    categoryMap.put("jxtNewsDetailList", jxtNewsDetailList);
                    jxtNewsList.add(categoryMap);
                }
            });
            map.put("categoryList", categoryNameList);
            map.put("jxtNewsList", jxtNewsList);
        }

        //广告
        List<Map<String, String>> advertisementList = subject.getAdList();
        if (CollectionUtils.isNotEmpty(advertisementList)) {
            map.put("advertisementType", subject.getAdvertisementType());
            List<Map<String, String>> adList = new ArrayList<>();
            advertisementList.forEach(e -> {
                Map<String, String> adMap = new HashMap<>();
                adMap.put("img", combineCdbUrl(e.get("img")));
                adMap.put("url", SafeConverter.toString(e.get("adUrl"), ""));
                adList.add(adMap);
            });
            map.put("advertisementList", adList);
        }

        return JsonUtils.toJson(map);
    }


    protected String generateNewsAliYunImgUrl(String url) {
        return StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + url;
    }

    private Map<Long, List<JxtNewsTeacherRecommend>> generateTeacherRecommendByParent(User parent) {
        if (parent == null) {
            return Collections.emptyMap();
        }
        Map<Long, List<JxtNewsTeacherRecommend>> recommendMap = new HashMap<>();
        //取家长的孩子
        Map<Long, List<StudentParentRef>> studentRefs = parentLoaderClient.loadParentStudentRefs(Collections.singleton(parent.getId()));
        if (MapUtils.isNotEmpty(studentRefs) && CollectionUtils.isNotEmpty(studentRefs.get(parent.getId()))) {
            Set<Long> studentIds = studentRefs.get(parent.getId()).stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
            //取孩子的group
            Map<Long, List<GroupMapper>> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentIds, false);
            if (MapUtils.isNotEmpty(studentGroups)) {
                Set<Long> groupIds = studentGroups.values().stream().flatMap(Collection::stream).map(GroupMapper::getId).collect(Collectors.toSet());
                //取group的老师
                Map<Long, Teacher> teacherMap = teacherLoaderClient.loadGroupSingleTeacher(groupIds);
                //取老师的主帐号
                if (MapUtils.isNotEmpty(teacherMap)) {
                    Set<Long> teacherIds = teacherMap.values().stream().filter(e -> e != null).map(Teacher::getId).collect(Collectors.toSet());
                    Map<Long, Long> mainTeacherIdMap = teacherLoaderClient.loadMainTeacherIds(teacherIds);
                    Set<Long> mainTeacherIds = mainTeacherIdMap.values().stream().collect(Collectors.toSet());
                    //把本身账号加进去。可能本身账号就是主帐号
                    mainTeacherIds.addAll(teacherIds);
                    if (CollectionUtils.isNotEmpty(mainTeacherIds)) {
                        Map<Long, AlpsFuture<List<JxtNewsTeacherRecommend>>> alpsFutureMap = jxtNewsLoaderClient.getTeacherRecommendListByTeacherIds(mainTeacherIds, SchoolYear.newInstance().year());
                        for (Long teacherId : alpsFutureMap.keySet()) {
                            recommendMap.put(teacherId, alpsFutureMap.get(teacherId).getUninterruptibly());
                        }
                    }
                }
            }
        }
        return recommendMap;
    }


    private List<JxtNews> generateRelativeNews(JxtNews news, User user, List<Long> tagList) {
        if (news == null) {
            return Collections.emptyList();
        }
        List<JxtNews> jxtNewses = jxtNewsLoaderClient.getAllOnlineJxtNews();
        jxtNewses = jxtNewses.stream().sorted((o1, o2) -> o2.getPushTime().compareTo(o1.getPushTime())).collect(Collectors.toList());
        Set<Integer> parentRegionIds = new HashSet<>();
        if (user != null) {
            Set<Long> studentIds = studentLoaderClient.loadParentStudents(user.getId()).stream().map(User::getId).collect(Collectors.toSet());
            Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
            for (StudentDetail studentDetail : studentDetails) {
                parentRegionIds.add(studentDetail.getStudentSchoolRegionCode());
                parentRegionIds.add(studentDetail.getCityCode());
                parentRegionIds.add(studentDetail.getRootRegionCode());
            }
        }


        jxtNewses = jxtNewses.stream()
                //去掉本篇资讯
                .filter(e -> !e.getId().equals(news.getId()))
                //过滤推送类型
                .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                        || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && user != null && user.getId().equals(p.getAvailableUserId()))
                        || (p.generatePushType().equals(JxtNewsPushType.REGION.getType()) && parentRegionIds.stream().anyMatch(e -> p.getRegionCodeList().contains(e))))
                //过滤出跟本篇资讯有相同tag的资讯
                .filter(p -> CollectionUtils.isNotEmpty(p.getTagList()) && CollectionUtils.isNotEmpty(tagList) && p.getTagList().stream().anyMatch(tagList::contains))
                .collect(Collectors.toList());

        jxtNewses = jxtNewses.subList(0, jxtNewses.size() > 3 ? 3 : jxtNewses.size());

        return jxtNewses;
    }
}
