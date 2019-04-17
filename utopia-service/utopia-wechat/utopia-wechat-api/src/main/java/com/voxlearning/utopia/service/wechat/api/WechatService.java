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

package com.voxlearning.utopia.service.wechat.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.wechat.api.constants.*;
import com.voxlearning.utopia.service.wechat.api.data.CreateQrcodeReq;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.api.entities.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Wechat service interface definition.
 *
 * @author Xin Xin
 * @author Maofeng Lu
 * @author Shuai Huan
 * @author Guohong Tan
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @since Apr 16, 2014
 */
@ServiceVersion(version = "20190114")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface WechatService extends IPingable {

    // only parent user can sign in wechat
    MapMessage signIn(Long userId, String signDate);

    MapMessage saveWechatShippingAddress(Long userId, String phone, String detailAddress, String postCode);

    MapMessage submitQuestion(String openId, String question, SourceType sourceType);

    MapMessage bindUserAndWechat(Long userId, String openId, String source, Integer type);

    MapMessage bindUserAndWechat(Long userId, String openId, String unionId, String source, Integer type);

    MapMessage bindUserAndMiniProgramOrRelogin(Long userId, String openId, String source, MiniProgramType type);

    MapMessage unbindUserAndWechat(String openId);

    MapMessage unbindUserAndWechatWithUserIdAndType(Long userId, Integer wechatType);

    MapMessage follow(String openId, Integer wechatType);

    void processWechatNotice(WechatNoticeProcessorType type, Clazz clazz, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType);

    void processWechatNotice(WechatNoticeProcessorType type, Long clazzId, Long groupId, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType);

    void processWechatNotice(WechatNoticeProcessorType type, List<User> students, Teacher teacher, Long clazzId, Map<String, Object> extensionInfo, WechatType wechatType);

    void processWechatNotice(WechatNoticeProcessorType type, Long userId, Map<String, Object> extensionInfo, WechatType wechatType);

    @NoResponseWait
    void processWechatNoticeNoWait(WechatNoticeProcessorType type, Long userId, Map<String, Object> extensionInfo, WechatType wechatType);

    @NoResponseWait
    void processWechatNoticeByOpenId(WechatNoticeProcessorType type, String openId, Map<String, Object> extensionInfo, WechatType wechatType);

    void processWechatNotice(WechatNoticeProcessorType type, Long userId, String openId, Map<String, Object> extensionInfo);

    void processWechatNotice(WechatNoticeProcessorType type, Map<Long, List<String>> user_openId_map, Map<String, Object> extensionInfo);

    /**
     * 发送模板消息
     * @return
     */
    MapMessage processWechatTemplateMessageNotice(Long userId, String messageTypeName, Map<String, WechatTemplateData> templateDataMap, Map<String, Object> activityParamMap);


    void processWithStudents(WechatNoticeProcessorType type, Collection<Long> studentIds, Map<String, Object> extensionInfo, WechatType wechatType);

    List<Map<String, Object>> loadNoticeByMessageType(Integer type);

    int updateNoticeStateTo5(List<Long> noticeIds);

    List<Map<String, Object>> loadNoticeByMessageTypeForCrm(Integer type);

    List<WechatQuestion> findWechatQuestionByCreateTimeOrState(int state, Date start, Date end);

    MapMessage processWechatQuestion(Long id, String desc, int state, String operator);

    List<WechatNotice> loadAfentiEKUnsentMessage(Long id);

    void updateNoticeState(String openId, String messageId, WechatNoticeState state, String errorCode);

    void updateNoticeState(Long id, WechatNoticeState state, String errorCode);

    void updateNoticeMessageId(Long id, String messageId);

    void updateMessageStateByType(Integer type);

    void deleteMessageStateByType(Integer type);

    MapMessage persistWechatNotice(WechatNotice notice);

    MapMessage addWechatSupportUser(String openId, String name);

    MapMessage addOrUpdateWechatFaq(WechatFaq wechatFaq);

    void changeFaqsStatus(List<Long> ids, String status);

    void removeFaqs(List<Long> ids);

    Map<Long, Set<Long>> studentBindWechatParentMap(Collection<Long> studentIds);

    int sendNoticeMessage(Long id);

    void saveWechatTicket(WechatTicket wechatTicket);

    void saveWechatFaceInviteRecord(WechatFaceInviteRecord record);

    void saveWechatRedPackHistory(WechatRedPackHistory packHistory);

    @Deprecated
    Long saveWechatLittleChampion(WechatLittleChampion champion);

    @Deprecated
    void updateWechatLittleChampion(WechatLittleChampion champion);

    List<Map<String, Object>> loadNoticeTypes();

    List<Map<String, Object>> loadSqlExecutors();

    void updateNoticeSqlState(long id, int state, long count);

    // ========================================================================
    // 临时的方法

    @Async
    AlpsFuture<Object> getFromPersistenceCache(String key);

    @Async
    AlpsFuture<Boolean> addIntoPersistenceCache(String key, int expirationInSeconds, Object value);

    // 临时的方法
    // ========================================================================

    @NoResponseWait
    void sendMessage(Message message);

    String createQrcode(CreateQrcodeReq createQrcodeReq);
}
