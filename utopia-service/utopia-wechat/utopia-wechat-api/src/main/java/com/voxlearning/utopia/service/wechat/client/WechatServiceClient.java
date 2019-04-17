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

package com.voxlearning.utopia.service.wechat.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.wechat.api.WechatService;
import com.voxlearning.utopia.service.wechat.api.constants.*;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeUpdater;
import com.voxlearning.utopia.service.wechat.api.entities.*;
import com.voxlearning.utopia.service.wechat.api.mapper.BindResultMapper;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class WechatServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(WechatServiceClient.class);

    @Getter
    @ImportService(interfaceClass = WechatService.class)
    private WechatService wechatService;

    public MapMessage signIn(final Long userId, final String signDate) {
        if (userId == null || signDate == null) {
            return MapMessage.errorMessage();
        }

        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            return builder.keyPrefix("WechatServiceClient:signIn")
                    .keys(userId, signDate)
                    .callback(() -> wechatService.signIn(userId, signDate))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            logger.error("Failed to sign in wechat (userId={},signDate={}): DUPLICATED OPERATION", userId, signDate);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to sign in wechat (userId={},signDate={})", userId, signDate, ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage saveWechatShippingAddress(final Long userId, final String phone, final String detailAddress, final String postCode) {
        if (userId == null) {
            return MapMessage.errorMessage();
        }
        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            return builder.keyPrefix("WechatServiceClient:saveWechatShippingAddress")
                    .keys(userId)
                    .callback(() -> wechatService.saveWechatShippingAddress(userId, phone, detailAddress, postCode))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            logger.error("Failed to save wechat shipping address (userId={}): DUPLICATED OPERATION", userId);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to save wechat shipping address (userId={})", userId, ex);
            return MapMessage.errorMessage();
        }
    }


    public MapMessage submitQuestion(String openId, String question, SourceType sourceType) {
        return wechatService.submitQuestion(openId, question, sourceType);
    }


    public MapMessage bindUserAndWechat(Long userId, String openId, String source, Integer type) {
        return wechatService.bindUserAndWechat(userId, openId, source, type);
    }

    public MapMessage bindUserAndMiniProgramOrRelogin(Long userId, String openId, String source, MiniProgramType type) {
        return wechatService.bindUserAndMiniProgramOrRelogin(userId, openId, source, type);
    }


    public MapMessage unbindUserAndWechat(String openId) {
        return wechatService.unbindUserAndWechat(openId);
    }


    public MapMessage follow(String openId, Integer wechatType) {
        return wechatService.follow(openId, wechatType);
    }


    public void processWechatNotice(WechatNoticeProcessorType type, Clazz clazz, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType) {
        wechatService.processWechatNotice(type, clazz, teacher, extensionInfo, wechatType);
    }


    public void processWechatNotice(WechatNoticeProcessorType type, Long clazzId, Long groupId, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType) {
        wechatService.processWechatNotice(type, clazzId, groupId, teacher, extensionInfo, wechatType);
    }


    public void processWechatNotice(WechatNoticeProcessorType type, List<User> students, Teacher teacher, Long clazzId, Map<String, Object> extensionInfo, WechatType wechatType) {
        wechatService.processWechatNotice(type, students, teacher, clazzId, extensionInfo, wechatType);
    }


    public void processWechatNotice(WechatNoticeProcessorType type, Long userId, Map<String, Object> extensionInfo, WechatType wechatType) {
        wechatService.processWechatNotice(type, userId, extensionInfo, wechatType);
    }


    public void processWechatNotice(WechatNoticeProcessorType type, Long userId, String openId, Map<String, Object> extensionInfo) {
        wechatService.processWechatNotice(type, userId, openId, extensionInfo);
    }


    public void processWechatNotice(WechatNoticeProcessorType type, Map<Long, List<String>> user_openId_map, Map<String, Object> extensionInfo) {
        wechatService.processWechatNotice(type, user_openId_map, extensionInfo);
    }


    public void processWithStudents(WechatNoticeProcessorType type, Collection<Long> studentIds, Map<String, Object> extensionInfo, WechatType wechatType) {
        wechatService.processWithStudents(type, studentIds, extensionInfo, wechatType);
    }


    public List<Map<String, Object>> loadNoticeByMessageType(Integer type) {
        return wechatService.loadNoticeByMessageType(type);
    }

    public int updateNoticeStateTo5(List<Long> noticeIds) {
        return wechatService.updateNoticeStateTo5(noticeIds);
    }

    public List<Map<String, Object>> loadNoticeByMessageTypeForCrm(Integer type) {
        return wechatService.loadNoticeByMessageTypeForCrm(type);
    }


    public List<WechatQuestion> findWechatQuestionByCreateTimeOrState(int state, Date start, Date end) {
        return wechatService.findWechatQuestionByCreateTimeOrState(state, start, end);
    }


    public MapMessage processWechatQuestion(Long id, String desc, int state, String operator) {
        return wechatService.processWechatQuestion(id, desc, state, operator);
    }


    public List<WechatNotice> loadAfentiEKUnsentMessage(Long id) {
        return wechatService.loadAfentiEKUnsentMessage(id);
    }


    public void updateNoticeState(String openId, String messageId, WechatNoticeState state, String errorCode) {
        wechatService.updateNoticeState(openId, messageId, state, errorCode);
    }


    public void updateNoticeState(Long id, WechatNoticeState state, String errorCode) {
        wechatService.updateNoticeState(id, state, errorCode);
    }


    public void updateNoticeMessageId(Long id, String messageId) {
        wechatService.updateNoticeMessageId(id, messageId);
    }


    public void updateMessageStateByType(Integer type) {
        wechatService.updateMessageStateByType(type);
    }


    public void deleteMessageStateByType(Integer type) {
        wechatService.deleteMessageStateByType(type);
    }


    public MapMessage persistWechatNotice(WechatNotice notice) {
        return wechatService.persistWechatNotice(notice);
    }


    public MapMessage addWechatSupportUser(String openId, String name) {
        return wechatService.addWechatSupportUser(openId, name);
    }


    public MapMessage addOrUpdateWechatFaq(WechatFaq wechatFaq) {
        return wechatService.addOrUpdateWechatFaq(wechatFaq);
    }


    public void changeFaqsStatus(List<Long> ids, String status) {
        wechatService.changeFaqsStatus(ids, status);
    }


    public void removeFaqs(List<Long> ids) {
        wechatService.removeFaqs(ids);
    }


    public Map<Long, Set<Long>> studentBindWechatParentMap(Collection<Long> studentIds) {
        return wechatService.studentBindWechatParentMap(studentIds);
    }


    public int sendNoticeMessage(Long id) {
        return wechatService.sendNoticeMessage(id);
    }


    public void saveWechatTicket(WechatTicket wechatTicket) {
        wechatService.saveWechatTicket(wechatTicket);
    }


    public void saveWechatFaceInviteRecord(WechatFaceInviteRecord record) {
        wechatService.saveWechatFaceInviteRecord(record);
    }


    public void saveWechatRedPackHistory(WechatRedPackHistory packHistory) {
        wechatService.saveWechatRedPackHistory(packHistory);
    }


    @Deprecated
    public Long saveWechatLittleChampion(WechatLittleChampion champion) {
        return wechatService.saveWechatLittleChampion(champion);
    }


    @Deprecated
    public void updateWechatLittleChampion(WechatLittleChampion champion) {
        wechatService.updateWechatLittleChampion(champion);
    }


    public List<Map<String, Object>> loadNoticeTypes() {
        return wechatService.loadNoticeTypes();
    }


    public List<Map<String, Object>> loadSqlExecutors() {
        return wechatService.loadSqlExecutors();
    }


    public void updateNoticeSqlState(long id, int state, long count) {
        wechatService.updateNoticeSqlState(id, state, count);
    }

    public void processDrawTogetherWechatNotice(User user, String clazzName, String text, String picUrl) {
        if (user == null) {
            return;
        }
        Teacher teacher = new Teacher();
        Map<String, Object> extensionInfo = new HashMap<>();
        extensionInfo.put("clazzName", clazzName);
        extensionInfo.put("comment", text);
        extensionInfo.put("picUrl", picUrl);
        try {
            wechatService.processWechatNotice(
                    WechatNoticeProcessorType.DrawTogetherNotice,
                    Collections.singletonList(user),
                    teacher,
                    0L,
                    extensionInfo,
                    WechatType.PARENT);
        } catch (Exception ex) {
            logger.error("Failed to process draw together wechat notice", ex);
        }
    }

    public void processTravelAmericaWechatNotice(User user, String clazzName, String url, Date sendTime) {
        if (user == null) {
            return;
        }
        Teacher teacher = new Teacher();
        Map<String, Object> extensionInfo = new HashMap<>();
        extensionInfo.put("clazzName", StringUtils.defaultString(clazzName));
        extensionInfo.put("url", StringUtils.defaultString(url));
        if (sendTime != null) {
            extensionInfo.put("sendTime", sendTime);
        }

        try {
            wechatService.processWechatNotice(
                    WechatNoticeProcessorType.TravelAmericaNotice,
                    Collections.singletonList(user),
                    teacher,
                    0L,
                    extensionInfo,
                    WechatType.PARENT);
        } catch (Exception ex) {
            logger.error("Failed to process travel america wechat notice", ex);
        }
    }

    public boolean updateNoticeMessageId2(Long id, String messageId) {
        if (id == null) {
            logger.error("updateNoticeMessageId - Illegal args : id = {}", id);
            return false;
        }
        WechatNoticeUpdater updater = new WechatNoticeUpdater(id, messageId);
        return sendWechatNoticeUpdateMessage(updater);
    }

    public boolean updateNoticeState2(Long id, WechatNoticeState state, String errorCode) {
        if (id == null) {
            logger.error("updateNoticeState - Illegal args : id = {}", id);
            return false;
        }
        WechatNoticeUpdater updater = new WechatNoticeUpdater(id, state, errorCode);
        return sendWechatNoticeUpdateMessage(updater);
    }

    public boolean updateNoticeState2(String openId, String messageId, WechatNoticeState state, String errorCode) {
        if (StringUtils.isBlank(openId) || StringUtils.isBlank(messageId)) {
            logger.error("updateNoticeState - Illegal args : openId = {}, messageId = {}", openId, messageId);
            return false;
        }
        WechatNoticeUpdater updater = new WechatNoticeUpdater(openId, messageId, state, errorCode);
        return sendWechatNoticeUpdateMessage(updater);
    }

    private boolean sendWechatNoticeUpdateMessage(WechatNoticeUpdater updater) {
        try {
            Message message = Message.newMessage().withStringBody(updater.serialize());
            wechatService.sendMessage(message);
        } catch (Exception e) {
            logger.error("sendWechatNoticeUpdateMessage - WechatQueueSender send message Excp : {}", e);
            return false;
        }
        return true;
    }

    public MapMessage bindUserAndWechat(Long userId, String openId, String source, Integer type, Consumer<Long> callBack) {
        try {
            MapMessage message = wechatService.bindUserAndWechat(userId, openId, source, type);
            if (message.isSuccess() && null != callBack) {
                BindResultMapper result = (BindResultMapper) message.get("result");
                if (null != result && result.getRunTask()) {
                    callBack.accept(userId);
                }
            }

            return message;
        } catch (Exception ex) {
            logger.error("Bind user to wechat failed,uid:{},openId:{},source:{},type:{}", userId, openId, source, type, ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage unbindUserAndWechat(String openId, Consumer<Long> callBack) {
        try {
            MapMessage message = wechatService.unbindUserAndWechat(openId);
            if (message.isSuccess() && null != callBack) {
                BindResultMapper result = (BindResultMapper) message.get("result");
                if (null != result && result.getRunTask()) {
                    callBack.accept(result.getUserId());
                }
            }

            return message;
        } catch (Exception ex) {
            logger.error("Unbind user from wechat failed,openId:{}", openId, ex);
            return MapMessage.errorMessage();
        }
    }


    public AlpsFuture<Object> getFromPersistenceCache(String key) {
        return wechatService.getFromPersistenceCache(key);
    }


    public AlpsFuture<Boolean> addIntoPersistenceCache(String key, int expirationInSeconds, Object value) {
        return wechatService.addIntoPersistenceCache(key, expirationInSeconds, value);
    }


    public void sendMessage(Message message) {
        wechatService.sendMessage(message);
    }

}
