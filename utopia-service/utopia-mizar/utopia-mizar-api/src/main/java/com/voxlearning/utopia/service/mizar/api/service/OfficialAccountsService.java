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

package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.oa.*;
import com.voxlearning.utopia.service.mizar.api.loader.MizarLoader;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Summer Yang on 2016/7/4.
 */
@ServiceVersion(version = "1.3.STABLE")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface OfficialAccountsService extends IPingable {
    List<OfficialAccounts> loadAllOfficialAccounts();

    OfficialAccounts loadAccountById(Long accountId);

    OfficialAccounts loadAccountByKey(String accountKey);

    MapMessage createAccounts(OfficialAccounts accounts);

    MapMessage updateAccounts(Long accountId, OfficialAccounts accounts);

    MapMessage uploadAccountImg(Long accountId, String fileName);

    List<OfficialAccountsArticle> loadArticlesByAccountsId(Long accountId);

    List<OfficialAccountsArticle> loadArticlesByAccountsId(Long accountId,Date startDate,Date endDate,Collection<String> statusList);

    OfficialAccountsArticle loadArticleById(Long articleId);

    MapMessage createAccountArticle(OfficialAccountsArticle accountsArticle);

    MapMessage updateAccountArticle(Long articleId, OfficialAccountsArticle article);

    MapMessage updateAccountStatus(Long accountId, OfficialAccounts.Status online);

    MapMessage updateArticleStatus(Long accountId, String bundleId, OfficialAccountsArticle.Status status);

    Map<Integer, List<OfficialAccountsTarget>> loadAccountTargetsGroupByType(Long accountId);

    MapMessage saveAccountTargets(Long accountId, Integer type, Collection<String> targetList, Boolean append);

    MapMessage clearAccountTargets(Long accountId, Integer type);

    @CacheMethod(
            type = OfficialAccountsService.class,
            expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed)
    )
    List<OfficialAccounts> loadUserOfficialAccounts(@CacheParameter("USER_ACCOUNTS") Long parentId);

    List<Map<String, Object>> loadArticlesByAccountsIdAndCreateDate(Long accountId, Date startDate, Long parentId);

    List<OfficialAccountsTools> loadAccountToolsByAccountId(Long accountId);

    MapMessage deleteAccountTool(Long toolId);

    void saveAccountTool(OfficialAccountsTools tools);

    boolean isFollow(Long accountId, Long parentId);

    MapMessage updateFollowStatus(Long parentId, Long accountId, UserOfficialAccountsRef.Status refStatus);

    MapMessage sendMessage(List<Long> parentIds, String title, String content, String linkUrl, String extInfoStr, Boolean sendPush);

    MapMessage sendGlobalMessage(String accountsKey,String title,String content,Integer linkType,String linkUrl,Boolean sendPush,Integer durationTime);

    MapMessage updateArticle(long articleId,OfficialAccountsArticle article);

    List<OfficialAccountsArticle> loadArticlesByBundleId(String bundleId);

    List<UserOfficialAccountsRef> loadUserOfficialAccoutnsRef(Long userId);
}
