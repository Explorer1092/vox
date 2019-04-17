/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.utopia.service.mizar.api.entity.oa.*;
import com.voxlearning.utopia.service.mizar.api.service.OfficialAccountsService;
import com.voxlearning.utopia.service.mizar.base.AbstractOfficialAccountsService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Summer Yang on 2016/7/4.
 */
public class OfficialAccountsServiceClient extends AbstractOfficialAccountsService {

    @ImportService(interfaceClass = OfficialAccountsService.class)
    private OfficialAccountsService remoteReference;

    public OfficialAccounts loadAccountByIdIncludeDisabled(Long accountId) {
        if (accountId == null) {
            return null;
        }
        String key = CacheKeyGenerator.generateCacheKey(OfficialAccounts.class, accountId);
        OfficialAccounts entity = CacheSystem.CBS.getCache("flushable").load(key);
        if (entity != null) {
            return entity;
        } else {
            return remoteReference.loadAccountById(accountId);
        }
    }

    public List<OfficialAccountsArticle> loadArticlesByAccountsIdIncludeOffline(Long accountId) {
        if (accountId == null) {
            return null;
        }
        String key = OfficialAccountsArticle.ck_accountId(accountId);
        List<OfficialAccountsArticle> articles = CacheSystem.CBS.getCache("flushable").load(key);
        if (CollectionUtils.isEmpty(articles)) {
            // 获取
            return remoteReference.loadArticlesByAccountsId(accountId);
        } else {
            return articles;
        }
    }

    public OfficialAccounts loadAccountById(Long accountId) {
        if (accountId == null) {
            return null;
        }
        String key = CacheKeyGenerator.generateCacheKey(OfficialAccounts.class, accountId);
        OfficialAccounts entity = CacheSystem.CBS.getCache("flushable").load(key);
        if (entity == null) {
            entity = remoteReference.loadAccountById(accountId);
        }
        if (entity != null && !entity.getDisabled() && entity.getStatus() == OfficialAccounts.Status.Online) {
            return entity;
        } else {
            return null;
        }
    }

    @Override
    public OfficialAccounts loadAccountByKey(String accountKey) {
        if (StringUtils.isBlank(accountKey)) {
            return null;
        }
        String key = OfficialAccounts.ck_key(accountKey);
        OfficialAccounts entity = CacheSystem.CBS.getCache("flushable").load(key);
        if (entity == null) {
            entity = remoteReference.loadAccountByKey(accountKey);
        }
        if (entity != null && !entity.getDisabled() && entity.getStatus() == OfficialAccounts.Status.Online) {
            return entity;
        } else {
            return null;
        }
    }

    public OfficialAccounts loadAccountsByKeyIncludeOffline(String accountKey) {
        if (StringUtils.isBlank(accountKey)) {
            return null;
        }
        String key = OfficialAccounts.ck_key(accountKey);
        OfficialAccounts entity = CacheSystem.CBS.getCache("flushable").load(key);
        if (entity == null) {
            entity = remoteReference.loadAccountByKey(accountKey);
        }

        return entity;
    }

    @Override
    public MapMessage createAccounts(OfficialAccounts accounts) {
        return remoteReference.createAccounts(accounts);
    }

    @Override
    public MapMessage updateAccounts(Long accountId, OfficialAccounts accounts) {
        return remoteReference.updateAccounts(accountId, accounts);
    }

    @Override
    public MapMessage uploadAccountImg(Long accountId, String fileName) {
        return remoteReference.uploadAccountImg(accountId, fileName);
    }

    @Override
    public List<OfficialAccountsArticle> loadArticlesByAccountsId(Long accountId) {
        return remoteReference.loadArticlesByAccountsId(accountId);
    }

    @Override
    public List<OfficialAccountsArticle> loadArticlesByAccountsId(
            Long accountId,
            Date startDate,
            Date endDate,
            Collection<String> statusList) {
        return remoteReference.loadArticlesByAccountsId(accountId, startDate, endDate, statusList);
    }


    /**
     * 获得公众号发布次数的限制信息
     *
     * @param accountId
     * @return
     */
    public MapMessage getPublishNumsLimit(Long accountId) {
        MapMessage resultMsg = MapMessage.successMessage();
        Date now = new Date();

        OfficialAccounts accounts = loadAccountByIdIncludeDisabled(accountId);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在!");

        List<String> statusList = Arrays.asList(
                OfficialAccountsArticle.Status.Offline,
                OfficialAccountsArticle.Status.Published)
                .stream().map(s -> s.toString())
                .collect(Collectors.toList());

        // 计算今天发布的文章次数
        Date todayEnd = DateUtils.ceiling(now, Calendar.DAY_OF_MONTH);
        Date todayBegin = DateUtils.truncate(now, Calendar.DAY_OF_MONTH);

        int publishNumsInDay = loadArticlesByAccountsId(
                accountId, todayBegin, todayEnd, statusList)
                .stream()
                .collect(Collectors.groupingBy(OfficialAccountsArticle::getBundleId))
                .size();

        resultMsg.add("publishLeftNumsD",
                Math.max(accounts.getMaxPublishNumsD() - publishNumsInDay, 0));

        // 计算当月发布的文章次数
        Date monthEnd = DateUtils.ceiling(now, Calendar.MONTH);
        Date monthBegin = DateUtils.truncate(now, Calendar.MONTH);

        int publishNumsInMonth = loadArticlesByAccountsId(
                accountId, monthBegin, monthEnd, statusList)
                .stream()
                .collect(Collectors.groupingBy(OfficialAccountsArticle::getBundleId))
                .size();

        resultMsg.add("publishLeftNumsM",
                Math.max(accounts.getMaxPublishNumsM() - publishNumsInMonth, 0));

        return resultMsg;
    }

    /**
     * 判断公众号是否还存有剩余的发布次数
     *
     * @param accountId
     * @return
     */
    public boolean hasSubmitNumsLeft(Long accountId) {
        MapMessage resultMsg = getPublishNumsLimit(accountId);
        if (resultMsg.isSuccess()) {
            Integer publishLeftNumsD = SafeConverter.toInt(resultMsg.get("publishLeftNumsD"));
            Integer publishLeftNumsM = SafeConverter.toInt(resultMsg.get("publishLeftNumsM"));

            if (publishLeftNumsD <= 0 || publishLeftNumsM <= 0)
                return false;
            else
                return true;
        }

        return false;
    }

    @Override
    public OfficialAccountsArticle loadArticleById(Long articleId) {
        return remoteReference.loadArticleById(articleId);
    }

    @Override
    public MapMessage createAccountArticle(OfficialAccountsArticle accountsArticle) {
        return remoteReference.createAccountArticle(accountsArticle);
    }

    @Override
    public MapMessage updateAccountArticle(Long articleId, OfficialAccountsArticle article) {
        return null;
    }

    @Override
    public MapMessage updateAccountStatus(Long accountId, OfficialAccounts.Status online) {
        return remoteReference.updateAccountStatus(accountId, online);
    }

    @Override
    public MapMessage updateArticleStatus(Long accountId, String bundleId, OfficialAccountsArticle.Status status) {
        return remoteReference.updateArticleStatus(accountId, bundleId, status);
    }

    public Map<Integer, List<OfficialAccountsTarget>> loadAccountTargetsGroupByType(Long accountId) {
        if (accountId == null) {
            return Collections.emptyMap();
        }
        String cacheKey = OfficialAccountsTarget.ck_accountId(accountId);
        List<OfficialAccountsTarget> list = CacheSystem.CBS.getCache("flushable").load(cacheKey);
        if (list == null) {
            return remoteReference.loadAccountTargetsGroupByType(accountId);
        }
        return list.stream().collect(Collectors.groupingBy(OfficialAccountsTarget::getTargetType, Collectors.toList()));
    }

    @Override
    public MapMessage saveAccountTargets(Long accountId, Integer type, Collection<String> targetList, Boolean append) {
        return remoteReference.saveAccountTargets(accountId, type, targetList, append);
    }

    @Override
    public MapMessage clearAccountTargets(Long accountId, Integer type) {
        return remoteReference.clearAccountTargets(accountId, type);
    }

    @Override
    public List<OfficialAccounts> loadUserOfficialAccounts(Long parentId) {
        return remoteReference.loadUserOfficialAccounts(parentId);
    }

    @Override
    public List<Map<String, Object>> loadArticlesByAccountsIdAndCreateDate(Long accountId, Date startDate, Long parentId) {
        return remoteReference.loadArticlesByAccountsIdAndCreateDate(accountId, startDate, parentId);
    }

    public int loadArticleRedCount(Long accountId, Date startDate) {
        if (accountId == null) {
            return 0;
        }
        String key = OfficialAccountsArticle.ck_accountId(accountId);
        List<OfficialAccountsArticle> articles = CacheSystem.CBS.getCache("flushable").load(key);
        if (CollectionUtils.isEmpty(articles)) {
            // 获取
            articles = remoteReference.loadArticlesByAccountsId(accountId);
        }
        if (CollectionUtils.isEmpty(articles)) {
            return 0;
        }
        Map<String, List<OfficialAccountsArticle>> articleMap = articles.stream()
                // 这里由创建时间改为发布时间，修复
                .filter(a -> (a.getPublishDatetime() != null && a.getPublishDatetime().after(startDate)) &&
                        a.getStatus() == OfficialAccountsArticle.Status.Published)
                .collect(Collectors.groupingBy(OfficialAccountsArticle::getBundleId, Collectors.toList()));
        return MapUtils.isEmpty(articleMap) ? 0 : articleMap.size();

    }

    public List<OfficialAccountsTools> loadAccountToolsByAccountId(Long accountId) {
        if (accountId == 0L) {
            return Collections.emptyList();
        }
        String key = OfficialAccountsTools.ck_AccountId(accountId);
        List<OfficialAccountsTools> toolsList = CacheSystem.CBS.getCache("flushable").load(key);
        if (CollectionUtils.isEmpty(toolsList)) {
            // 获取
            return remoteReference.loadAccountToolsByAccountId(accountId);
        } else {
            return toolsList;
        }
    }

    @Override
    public MapMessage deleteAccountTool(Long toolId) {
        return remoteReference.deleteAccountTool(toolId);
    }

    @Override
    public void saveAccountTool(OfficialAccountsTools tools) {
        remoteReference.saveAccountTool(tools);
    }

    @Override
    public List<OfficialAccounts> loadAllOfficialAccounts() {
        return remoteReference.loadAllOfficialAccounts();
    }

    public boolean isFollow(Long accountId, Long parentId) {
        return remoteReference.isFollow(accountId, parentId);
    }

    public MapMessage updateFollowStatus(Long parentId, Long accountId, UserOfficialAccountsRef.Status refStatus) {
        return remoteReference.updateFollowStatus(parentId, accountId, refStatus);
    }

    /**
     * @param parentIds  要发送的用户ID
     * @param title      消息标题
     * @param content    消息内容
     * @param linkUrl    链接URL 全路径
     * @param extInfoStr 扩展字段 JSON字符串  {"accountsKey":""} 为必须字段
     * @return 是否成功
     */
    public MapMessage sendMessage(List<Long> parentIds, String title, String content, String linkUrl, String extInfoStr, Boolean sendPush) {
        return remoteReference.sendMessage(parentIds, title, content, linkUrl, extInfoStr, sendPush);
    }

    @Override
    public MapMessage sendGlobalMessage(String accountsKey, String title, String content, Integer linkType, String linkUrl, Boolean sendPush,Integer durationTime) {
        return remoteReference.sendGlobalMessage(accountsKey, title, content, linkType, linkUrl, sendPush,durationTime);
    }

    @Override
    public MapMessage updateArticle(long articleId, OfficialAccountsArticle article) {
        return remoteReference.updateArticle(articleId, article);
    }

    @Override
    public List<OfficialAccountsArticle> loadArticlesByBundleId(String bundleId) {
        return remoteReference.loadArticlesByBundleId(bundleId);
    }

    /**
     * 按照公众号的模板(一大三小)，返回文章列表。没有数据的补齐为空~
     *
     * @param bundleId
     * @return
     */
    public List<OfficialAccountsArticle> loadArticlesInTpl(String bundleId) {
        List<OfficialAccountsArticle> articles = new ArrayList<>();
        if (StringUtils.isNotEmpty(bundleId))
            articles = loadArticlesByBundleId(bundleId);

        int orgSize = articles.size();

        String existBundleId = null;
        OfficialAccountsArticle.Status existStatus = OfficialAccountsArticle.Status.Online;
        Date existPublishTime = null;
        if (orgSize > 0) {
            existBundleId = articles.get(0).getBundleId();
            existStatus = articles.get(0).getStatus();
            existPublishTime = articles.get(0).getPublishDatetime();
        }

        // 文章不足4个的话，补齐空的文章，空文章的bundleid用老的
        OfficialAccountsArticle newArticle;
        for (int i = 0; i < 4 - orgSize; i++) {
            newArticle = new OfficialAccountsArticle();
            newArticle.setBundleId(existBundleId);
            newArticle.setStatus(existStatus);
            newArticle.setPublishDatetime(existPublishTime);

            articles.add(newArticle);
        }

        return articles;
    }

    @Override
    public List<UserOfficialAccountsRef> loadUserOfficialAccoutnsRef(Long userId) {
        return remoteReference.loadUserOfficialAccoutnsRef(userId);
    }

    /**
     * 获得用户最近一次关注公众号的时间
     * @param userId
     * @param accountId
     * @return
     */
    public Date getFollowAccountDate(Long userId,Long accountId){
        UserOfficialAccountsRef userOARef = loadUserOfficialAccoutnsRef(userId)
                .stream()
                .filter(ref -> Objects.equals(ref.getOfficialAccountsId(),accountId))
                .findAny()
                .orElse(null);

        if(userOARef != null
                && (userOARef.getStatus() == UserOfficialAccountsRef.Status.Follow ||
                userOARef.getStatus() == UserOfficialAccountsRef.Status.AutoFollow ))
            return userOARef.getUpdateDatetime();
        else
            return null;
    }

    // 查询文章
    public List<Map<String, Object>> queryArticle(Long accountId, String startDate, String endDate, String status) {
        List<OfficialAccountsArticle> articles = loadArticlesByAccountsIdIncludeOffline(accountId);
        // 合并
        List<Map<String, Object>> articlesList = collectArticleData(articles);
        articlesList.sort((o1, o2) -> {
            Date time1 = DateUtils.stringToDate(SafeConverter.toString(o1.get("sendTime")));
            Date time2 = DateUtils.stringToDate(SafeConverter.toString(o2.get("sendTime")));
            return time2.compareTo(time1);
        });

        Stream<Map<String, Object>> articlesStream = articlesList.stream();

        if (StringUtils.isNotEmpty(startDate)) {
            Date start = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE);
            articlesStream = articlesStream.filter(data -> {
                Date publishTime = (Date) data.get("publishDatetime");
                return start != null && publishTime != null && publishTime.after(start);
            });
        }

        if (StringUtils.isNotEmpty(endDate)) {
            Date end = DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date realEnd = calendar.getTime();

            articlesStream = articlesStream.filter(data -> {
                Date publishTime = (Date) data.get("publishDatetime");
                return realEnd != null && publishTime != null && publishTime.before(realEnd);
            });
        }

        if (StringUtils.isNotEmpty(status)) {
            articlesStream = articlesStream.filter(data -> StringUtils.isEmpty(status)
                    || status.equals(data.get("status").toString()));
        }

        return articlesStream.collect(Collectors.toList());
    }

    /**
     * 将文章汇总
     *
     * @param articles
     * @return
     */
    private List<Map<String, Object>> collectArticleData(List<OfficialAccountsArticle> articles) {

        if (CollectionUtils.isEmpty(articles)) {
            return Collections.emptyList();
        }

        Map<String, List<OfficialAccountsArticle>> articleMap = articles.stream()
                .collect(Collectors.groupingBy(OfficialAccountsArticle::getBundleId, Collectors.toList()));

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Map.Entry<String, List<OfficialAccountsArticle>> entry : articleMap.entrySet()) {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("sendTime", DateUtils.dateToString(entry.getValue().get(0).getCreateDatetime()));
            List<OfficialAccountsArticle> articleList = entry.getValue();
            if (CollectionUtils.isNotEmpty(articleList)) {
                articleList.sort((o1, o2) -> o1.getId().compareTo(o2.getId()));
            }
            objectMap.put("articles", articleList);
            objectMap.put("status", entry.getValue().get(0).getStatus());
            objectMap.put("bundleId", entry.getValue().get(0).getBundleId());
            objectMap.put("isSend", entry.getValue().get(0).getHasSend());
            objectMap.put("publishDatetime", entry.getValue().get(0).getPublishDatetime());

            // 转换格式
            Date publishTime = entry.getValue().get(0).getPublishDatetime();
            if (publishTime == null)
                objectMap.put("publishTime", "");
            else
                objectMap.put("publishTime", DateUtils.dateToString(publishTime));

            objectMap.put("publishUser", entry.getValue().get(0).getPublishUser());
            dataList.add(objectMap);
        }

        return dataList;
    }

    /**
     * 保存文章
     *
     * @param articleList
     * @param sendJpush
     * @param bindSid
     * @param accountId
     * @return
     */
    public MapMessage saveArticle(
            List<Map<String, Object>> articleList,
            boolean sendJpush,
            boolean bindSid,
            Long accountId) {

        try {
            // 获取文章列表
            if (CollectionUtils.isEmpty(articleList)) {
                return MapMessage.errorMessage("文章内容不能为空");
            }

            if (accountId == 0L) {
                return MapMessage.errorMessage("公众号ID不存在");
            }

            // 添加文章内容
            // 生成批次ID
            String bundleId = UUID.randomUUID().toString();
            String publishTime;
            String status;
            for (int i = 0; i < articleList.size(); i++) {
                OfficialAccountsArticle accountsArticle = new OfficialAccountsArticle();

                accountsArticle.setId(SafeConverter.toLong(articleList.get(i).get("id")));
                accountsArticle.setStatus(OfficialAccountsArticle.Status.Online);
                accountsArticle.setImgUrl(SafeConverter.toString(articleList.get(i).get("imgUrl")));
                accountsArticle.setAccountId(accountId);
                accountsArticle.setArticleTitle(SafeConverter.toString(articleList.get(i).get("title")));
                accountsArticle.setArticleUrl(SafeConverter.toString(articleList.get(i).get("articleUrl")));
                accountsArticle.setMaterialId(SafeConverter.toString(articleList.get(i).get("materialId")));

                // 三要素如果有为空的话，则从当前条记录以后都不保存
                if (StringUtils.isEmpty(accountsArticle.getArticleTitle()) ||
                        StringUtils.isEmpty(accountsArticle.getArticleUrl()) ||
                        StringUtils.isEmpty(accountsArticle.getImgUrl())) {
                    break;
                }

                // 如果是修改的话，沿用之前老的bundleid
                String existBundleId = SafeConverter.toString(articleList.get(i).get("bundleId"));
                if (StringUtils.isNotEmpty(existBundleId)) {
                    accountsArticle.setBundleId(existBundleId);

                    status = SafeConverter.toString(articleList.get(i).get("status"));
                    if (StringUtils.isNotEmpty(status))
                        accountsArticle.setStatus(OfficialAccountsArticle.Status.valueOf(status));
                    else
                        accountsArticle.setStatus(OfficialAccountsArticle.Status.Online);

                    publishTime = SafeConverter.toString(articleList.get(i).get("publishDatetime"));
                    if (StringUtils.isNotEmpty(publishTime)) {
                        accountsArticle.setPublishDatetime(
                                FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss").parse(publishTime));
                    }
                } else
                    accountsArticle.setBundleId(bundleId);

                accountsArticle.setHasSend(sendJpush);
                accountsArticle.setBindSid(bindSid);

                if (accountsArticle.getId() != 0L) {
                    updateArticle(accountsArticle.getId(), accountsArticle);
                } else
                    createAccountArticle(accountsArticle);

            }

            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    public MapMessage publishArticle(
            String bundleId,
            String userName,
            Function<Map<String, Object>, Void> jpushCallback) {

        if (StringUtils.isEmpty(bundleId) || StringUtils.isEmpty(userName))
            return MapMessage.errorMessage("参数错误");

        List<OfficialAccountsArticle> articles = loadArticlesByBundleId(bundleId);

        // 置上发布时间和操作员
        articles.forEach(a -> {
            a.setPublishDatetime(new Date());
            a.setPublishUser(userName);
            a.setStatus(OfficialAccountsArticle.Status.Published);

            updateArticle(a.getId(), a);
        });

        if (articles.size() <= 0)
            return MapMessage.errorMessage("文章不存在！");

        boolean sendJpush = articles.get(0).getHasSend();
        String jpushContent = articles.get(0).getArticleTitle();
        long accountId = articles.get(0).getAccountId();
        if (sendJpush) {
            // 有配置 按照tag发送
            /*List<String> pushSchoolOrRegionTags = new ArrayList<>();
            // 发送 目前公众号只当家长端发送
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("studentId", "");
            jpushExtInfo.put("url", "");
            jpushExtInfo.put("tag", ParentMessageTag.公众号.name());
            jpushExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW);
            jpushExtInfo.put("shareContent", "");
            jpushExtInfo.put("shareUrl", "");

            OfficialAccounts accounts = loadAccountById(accountId);
            jpushExtInfo.put("ext_tab_message_type", accounts.getId());
            jpushExtInfo.put("officialAccountName", accounts.getName());
            jpushExtInfo.put("officialAccountID", accounts.getId());
            jpushContent = accounts.getName() + "：" + jpushContent;
            // 根据tag发送
            pushSchoolOrRegionTags.add(JpushUserTag.OFFICIAL_ACCOUNT_FOLLOW.generateTag(accounts.getAccountsKey()));
            appMessageServiceClient.sendAppJpushMessageByTags(jpushContent, AppMessageSource.PARENT,
                    pushSchoolOrRegionTags, null, jpushExtInfo, 0);*/
        }

        return MapMessage.successMessage();
    }

}
