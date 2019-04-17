package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageShareType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsAlbum;
import com.voxlearning.utopia.service.vendor.api.entity.ParentNewsAlbumSubRecord;
import com.voxlearning.utopia.service.vendor.api.entity.ParentShowAlbumRecord;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2016/11/4.
 */
@Named
@ScheduledJobDefinition(
        jobName = "家长通资讯订阅专辑更新时的任务",
        jobDescription = "每天17:00运行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT},
        cronExpression = "0 0 17 * * ? "
)
@ProgressTotalWork(100)
public class AutoJxtNewsAlbumJob extends ScheduledJobWithJournalSupport {

    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject
    private OfficialAccountsServiceClient officialAccountsServiceClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    private UtopiaSql utopiaSql;

    @PostConstruct
    private void init() {
        this.utopiaSql = utopiaSqlFactory.getUtopiaSql("hs_misc");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<JxtNewsAlbum> updateAlbums = new ArrayList<>();
        Set<String> newsIds = new HashSet<>();
        Set<String> allNewsIds = new HashSet<>();
        //取所有上线的专辑
        List<JxtNewsAlbum> allOnlineAlbums = jxtNewsLoaderClient.getAllOnlineJxtNewsAlbum();
        if (CollectionUtils.isNotEmpty(allOnlineAlbums)) {
            //所有的文章id
            allOnlineAlbums.stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getNewsRecordList()))
                    .forEach(e -> allNewsIds.addAll(e.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet())));
            //把专辑里createTime在24小时之内的文章加入一个set
            allOnlineAlbums.stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getNewsRecordList()))
                    .forEach(e -> newsIds.addAll(e.getNewsRecordList().stream().filter(r -> DateUtils.hourDiff(new Date(), r.getCreateTime()) <= 24).map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet())));
            //从所有的newsId中把24小时之内加入的去掉，剩下的就是24小时之前加入的。
            /*
            * 这样做的原因是:目前运营会把下线的文章提前几天加入专辑，而在几天后定时上线，这种情况下，直接取24小时之内加入专辑的文章发消息会丢失一部分文章。导致收不到消息
            *
            * */
            allNewsIds.removeAll(newsIds);
            if (CollectionUtils.isNotEmpty(newsIds)) {
                Map<String, JxtNews> jxtNewsByNewsIds = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIds);
                Map<String, JxtNews> jxtNewsByNewsIdsBefore = jxtNewsLoaderClient.getJxtNewsByNewsIds(allNewsIds);
                Set<String> updateNewsIds = new HashSet<>();
                //从24小时内加入专辑的文章中，过滤出已经上线的文章
                if (MapUtils.isNotEmpty(jxtNewsByNewsIds)) {
                    updateNewsIds.addAll(jxtNewsByNewsIds.values().stream().filter(JxtNews::getOnline).map(JxtNews::getId).collect(Collectors.toSet()));
                }
                //从24小时之前加入专辑的文章中，过滤出24小时内上线并且上线状态为true的文章
                if (MapUtils.isNotEmpty(jxtNewsByNewsIdsBefore)) {
                    updateNewsIds.addAll(jxtNewsByNewsIdsBefore.values().stream().filter(e -> e.getOnline() && e.getPushTime() != null && DateUtils.hourDiff(new Date(), e.getPushTime()) <= 24).map(JxtNews::getId).collect(Collectors.toSet()));
                }
                final Set<String> finalUpdateNewsIds = updateNewsIds;
                //判断哪些专辑的文章id在updateNewsIds里面，只要有文章id在这个updateNewsIds里面，就说明，这个专辑有更新
                updateAlbums = allOnlineAlbums.stream().filter(e -> e.getNewsRecordList().stream().anyMatch(p -> finalUpdateNewsIds.contains(p.getNewsId()))).collect(Collectors.toList());
            }
            progressMonitor.worked(5);
            if (CollectionUtils.isNotEmpty(updateAlbums)) {
                List<String> updateIds = updateAlbums.stream().map(JxtNewsAlbum::getId).collect(Collectors.toList());
                //根据更新的专辑id取专辑的订阅记录
                int threadCount = updateIds.size();
                final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
                Set<ParentNewsAlbumSubRecord> albumSubRecordSet = new HashSet<>();
                for (String updateId : updateIds) {
                    AlpsThreadPool.getInstance().submit(() -> {
                        Pageable pageable = new PageRequest(0, 1000);
                        Page<ParentNewsAlbumSubRecord> subRecordByPage = jxtNewsLoaderClient.getParentSubAlbumRecordByAlbumIdAndPage(Collections.singleton(updateId), pageable);
                        if (subRecordByPage.getTotalElements() == 0) {
                            return;
                        }
                        albumSubRecordSet.addAll(subRecordByPage.getContent());
                        while (subRecordByPage.hasNext()) {
                            pageable = subRecordByPage.nextPageable();
                            subRecordByPage = jxtNewsLoaderClient.getParentSubAlbumRecordByAlbumIdAndPage(Collections.singleton(updateId), pageable);
                            albumSubRecordSet.addAll(subRecordByPage.getContent());
                        }
                        countDownLatch.countDown();
                    });
                }
                countDownLatch.await();
                Map<String, List<ParentNewsAlbumSubRecord>> albumSubRecordByAlbumId = albumSubRecordSet.stream().filter(ParentNewsAlbumSubRecord::getIsSub).collect(Collectors.groupingBy(ParentNewsAlbumSubRecord::getSubAlbumId));
                Map<String, JxtNewsAlbum> updateAlbumMap = updateAlbums.stream().collect(Collectors.toMap(JxtNewsAlbum::getId, (p) -> p));
                //取出更新专辑所有的资讯id
                Set<String> updateNewsIds = new HashSet<>();
                updateAlbumMap.values().stream().forEach(e -> updateNewsIds.addAll(e.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet())));
                Map<String, JxtNews> updateJxtNewsMap = jxtNewsLoaderClient.getJxtNewsByNewsIds(updateNewsIds);
                //把专辑的订阅记录都加到这个list中，后面在根据用户id转成map
                List<ParentNewsAlbumSubRecord> updateAlbumSubRecords = new ArrayList<>();
                if (MapUtils.isNotEmpty(albumSubRecordByAlbumId)) {
                    updateAlbumSubRecords = albumSubRecordByAlbumId.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
                }
                Set<String> ids = new HashSet<>();
                updateAlbumSubRecords.stream().filter(p -> p.getUserId() != null && StringUtils.isNotBlank(p.getSubAlbumId()) && p.getIsSub()).forEach(e -> {
                    String showId = ParentShowAlbumRecord.generateId(e.getUserId(), e.getSubAlbumId());
                    ids.add(showId);
                });
                //避免一次性查的太多，分批取
                List<List<String>> showIdList = CollectionUtils.splitList(new ArrayList<>(ids), ids.size() / 100 + 1);
                Map<String, ParentShowAlbumRecord> parentShowAlbumRecordByIds = new HashMap<>();
                for (List<String> showIds : showIdList) {
                    Map<String, ParentShowAlbumRecord> showAlbumRecordMap = jxtNewsLoaderClient.getParentShowAlbumRecordByIds(showIds);
                    if (MapUtils.isNotEmpty(showAlbumRecordMap)) {
                        parentShowAlbumRecordByIds.putAll(showAlbumRecordMap);
                    }
                }
                //Map<String, ParentShowAlbumRecord> parentShowAlbumRecordByIds = jxtNewsLoaderClient.getParentShowAlbumRecordByIds(ids);
                //logger.info("getShowRecord:" + parentShowAlbumRecordByIds.size());
                Map<Long, List<ParentNewsAlbumSubRecord>> updateAlbumSubRecordsMapByUserId = updateAlbumSubRecords.stream().filter(ParentNewsAlbumSubRecord::getIsSub).collect(Collectors.groupingBy(ParentNewsAlbumSubRecord::getUserId));
                Map<Long, List<String>> userAlbumTitleMap = new HashMap<>();
                progressMonitor.subTask(95, updateAlbumSubRecordsMapByUserId.size());
                //按用户发消息
                for (Map.Entry<Long, List<ParentNewsAlbumSubRecord>> updateSubRecordByUser : updateAlbumSubRecordsMapByUserId.entrySet()) {
                    Long userId = updateSubRecordByUser.getKey();
                    //这个用户订阅的并且有更新的专辑的title
                    List<String> albumTitles = new ArrayList<>();
                    for (ParentNewsAlbumSubRecord e : updateSubRecordByUser.getValue()) {
                        JxtNewsAlbum album = updateAlbumMap.get(e.getSubAlbumId());
                        ParentShowAlbumRecord parentShowAlbumRecord = null;
                        if (album != null) {
                            parentShowAlbumRecord = parentShowAlbumRecordByIds.get(ParentShowAlbumRecord.generateId(userId, album.getId()));
                        }
                        Long albumNum = 0L;
                        if (parentShowAlbumRecord != null) {
                            final ParentShowAlbumRecord finalParentShowAlbumRecord = parentShowAlbumRecord;
                            albumNum = album.getNewsRecordList().stream().filter(p -> (updateJxtNewsMap.get(p.getNewsId()).getPushTime() != null && p.getCreateTime().before(updateJxtNewsMap.get(p.getNewsId()).getPushTime())) ? updateJxtNewsMap.get(p.getNewsId()).getPushTime().after(finalParentShowAlbumRecord.getUpdateTime()) : p.getCreateTime().after(finalParentShowAlbumRecord.getUpdateTime())).count();
                        }
                        if (albumNum != 0L) {
                            albumTitles.add(album.getTitle());
                        }
                    }


                    if (CollectionUtils.isNotEmpty(albumTitles)) {
                        userAlbumTitleMap.put(userId, albumTitles);
//                    generateMessage(userId, albumTitles);
                    }
                    progressMonitor.worked(1);
                }

                sendOfficialAccountsMsg(userAlbumTitleMap, jobJournalLogger);

            }
        }
        progressMonitor.done();
        //logger.info("sendMessageFinish");
    }

    /**
     * 发送公众号模板消息
     *
     * @param userAlbumTitlesMap 用户公众号标题对照
     */
    private void sendOfficialAccountsMsg(Map<Long, List<String>> userAlbumTitlesMap, JobJournalLogger logger) {
        //消息的标题
        String title = "订阅专辑更新提醒";
        String accountsKey = "album";
        String url = "/view/mobile/parent/album/my_subscript.vpage";
        userAlbumTitlesMap.entrySet().forEach(e -> {
            String content;
            List<String> titles = e.getValue();
            if (CollectionUtils.isEmpty(titles))// 不匹配就返回
                return;
            //消息内容
            if (titles.size() == 1) {
                content = "您订阅的" + "《" + titles.get(0) + "》" + "专辑更新了内容，点击查看";
            } else if (titles.size() == 2) {
                content = "您订阅的" + "《" + titles.get(0) + "》" + "、" + "《" + titles.get(1) + "》" + "专辑更新了内容，点击查看";
            } else {
                content = "您订阅的" + "《" + titles.get(0) + "》" + "、" + "《" + titles.get(1) + "》" + "等专辑更新了内容，点击查看";
            }
            content = title + "：" + content;
            sendJpushMessage(content, url, e.getKey());
        });
//        //公众号的key
//        Map<String, Object> extInfoStrMap = new HashMap<>();
//        extInfoStrMap.put("accountsKey", accountsKey);
//        String extInfoStr = JsonUtils.toJson(extInfoStrMap);
//        // 关注状态列表
//        List<String> statusList = Arrays.asList(UserOfficialAccountsRef.Status.Follow.name()
//                , UserOfficialAccountsRef.Status.AutoFollow.name());
//
//        Map<String, Object> queryParamsMap = new HashMap<>();
//        queryParamsMap.put("accountsKey", accountsKey);
//        queryParamsMap.put("statusList", statusList);
//
//        for (int tableIndex = 0; tableIndex < 100; tableIndex++) {
//            queryParamsMap.put("tableIndex", tableIndex);
//
//            String queryUserSql = "SELECT USER_ID FROM VOX_USER_OFFICIAL_ACCOUNTS_REF_:tableIndex " +
//                    " WHERE ACCOUNTS_KEY = :accountsKey " +
//                    " AND STATUS in (:statusList) ";
//            try {
//
//                utopiaSql.withSql(queryUserSql)
//                        .useParams(queryParamsMap)
//                        .queryAll(SingleColumnRowMapper.newInstance(Long.class))
//                        .forEach(userId -> {
//
//                            String content;
//                            List<String> titles = userAlbumTitlesMap.get(userId);
//                            if (CollectionUtils.isEmpty(titles))// 不匹配就返回
//                                return;
//
////                            //消息内容
////                            if (titles.size() == 1) {
////                                content = "您订阅的" + "《" + titles.get(0) + "》" + "专辑更新了内容，点击查看";
////                            } else if (titles.size() == 2) {
////                                content = "您订阅的" + "《" + titles.get(0) + "》" + "、" + "《" + titles.get(1) + "》" + "专辑更新了内容，点击查看";
////                            } else {
////                                content = "您订阅的" + "《" + titles.get(0) + "》" + "、" + "《" + titles.get(1) + "》" + "等专辑更新了内容，点击查看";
////                            }
//
//                            officialAccountsServiceClient.sendMessage(
//                                    Collections.singletonList(userId),
//                                    title,
//                                    content,
//                                    url,
//                                    extInfoStr,
//                                    false);
//                        });
//                logger.log("VOX_USER_OFFICIAL_ACCOUNTS_REF_{} completed execution!", tableIndex);
//            } catch (Exception e) {
//                logger.log("Error! VOX_USER_OFFICIAL_ACCOUNTS_REF_{} is failed! message:{}", tableIndex, e.getMessage());
//                break;
//            }

    }

    private void sendJpushMessage(String content, String url, Long parentId) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(url)) {
            return;
        }
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("url", ProductConfig.getMainSiteBaseUrl() + url);
        jpushExtInfo.put("tag", ParentMessageTag.资讯.name());
        jpushExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW.name());
        jpushExtInfo.put("shareContent", "");
        jpushExtInfo.put("shareUrl", "");
        jpushExtInfo.put("s", ParentAppPushType.JXT_NEWS.name());
        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, Collections.singletonList(parentId), jpushExtInfo);
    }
//    private MapMessage generateMessage(Long userId, List<String> titles) {
//        //消息的标题
//        String title = "订阅专辑更新提醒";
//        //消息内容
//        String content;
//        if (titles.size() == 1) {
//            content = "您订阅的" + "《" + titles.get(0) + "》" + "专辑更新了内容，点击查看";
//        } else if (titles.size() == 2) {
//            content = "您订阅的" + "《" + titles.get(0) + "》" + "、" + "《" + titles.get(1) + "》" + "专辑更新了内容，点击查看";
//        } else {
//            content = "您订阅的" + "《" + titles.get(0) + "》" + "、" + "《" + titles.get(1) + "》" + "等专辑更新了内容，点击查看";
//        }
//        String accountsKey = "album";
//        //公众号的key
//        Map<String, Object> extInfoStrMap = new HashMap<>();
//        extInfoStrMap.put("accountsKey", accountsKey);
//
//        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByKey(accountsKey);
//        // 未关注的用户，不推送
//        if (accounts != null) {
//            if(officialAccountsServiceClient.isFollow(accounts.getId(),userId)){
//                //消息的跳转链接
//                String url = "view/mobile/parent/information/album_subscribe";
//                String extInfoStr = JsonUtils.toJson(extInfoStrMap);
//                return officialAccountsServiceClient.sendMessage(Collections.singletonList(userId), title, content, url, extInfoStr, true);
//            }else
//                return MapMessage.errorMessage("用户未关注公众号，忽略push消息！");
//        } else
//            return MapMessage.errorMessage("公众号不存在!");
//    }


}
