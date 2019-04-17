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

package com.voxlearning.utopia.service.wechat.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTicketType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeSnapshot;
import com.voxlearning.utopia.service.wechat.api.entities.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Wechat loader interface definition.
 *
 * @author Xiaohai Zhang
 * @since Jan 19, 2015
 */
@ServiceVersion(version = "20180605")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface WechatLoader extends IPingable {

    WechatFaqCatalog loadWechatFaqCatalog(Long id);

    List<WechatFaqCatalog> loadWechatFaqCatalogs(Integer type);

    WechatFaq loadWechatFaq(Long id);

    List<WechatFaq> loadCatalogWechatFaqs(Long catalogId, WechatType type);

    @Deprecated
    List<WechatFaq> loadWechatFaqForCrm(String sql, Map<String, Object> parameters);

    //根据关键词去匹配关联的问题
    //多个关键词以空格分隔
    List<WechatFaq> matchWechatFaq(String keyword, WechatType type);

    WechatSupportUser loadWechatSupportUser(String openId);

    WechatSign loadUserWechatSign(Long userId, String signDate);

    Map<Long, List<UserWechatRef>> loadUserWechatRefs(Collection<Long> userIds, WechatType wechatType);

    Map<Long, List<UserWechatRef>> loadUserWechatRefs(Collection<Long> userIds);

    User loadWechatUser(String openId);

    User loadWechatUserByUnionId(String unionId);

    List<UserWechatRef> loadUserWechatRefsIncludeDisabled(Long userId, Integer wechatType);

    @CacheMethod(writeCache = false)
    UserMiniProgramRef loadMiniProgramUserRef(@CacheParameter("openId") String openId, @CacheParameter("type")Integer miniProgramType);
    @CacheMethod(writeCache = false)
    UserMiniProgramRef loadMiniProgramUserRef(@CacheParameter("userId") Long userId, @CacheParameter("type")Integer miniProgramType);

    User loadWechatUser(String openId, Long userId);

    boolean haveBindBefore(Long userId, Integer wechatType);

    boolean isBinding(Long userId, Integer wechatType);

    boolean isStudentBinding(Long studentId);

    WechatTicket loadWechatTicket(Long teacherId, WechatTicketType wechatTicketType);

    WechatFaceInviteRecord loadWechatFaceInviteLastRecord(String openId);

    List<WechatNoticeSnapshot> loadWechatNoticeSnapshotByUserId(Long userId, Boolean isHistory);

    @Deprecated
    List<Map<String, Object>> loadNoSubmitLittleChampionChildren(List<User> students);

    @Deprecated
    WechatLittleChampion loadWechatLittleChampionById(Long id);

    @Deprecated
    boolean hasSubmitWechatLittleChampion(Long studentId);

    @Deprecated
    List<WechatLittleChampion> loadWechatLittleChampionByTeacherId(Long teacherId);

    UserWechatRef loadUserWechatRefByUserIdAndWechatType(Long userId, Integer type);
}
