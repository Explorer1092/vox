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

package com.voxlearning.utopia.service.wechat.impl.service;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.WechatService;
import com.voxlearning.utopia.service.wechat.api.constants.*;
import com.voxlearning.utopia.service.wechat.api.data.CreateQrcodeReq;
import com.voxlearning.utopia.service.wechat.api.data.CreateQrcodeType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.api.entities.*;
import com.voxlearning.utopia.service.wechat.api.mapper.BindResultMapper;
import com.voxlearning.utopia.service.wechat.consumer.helpers.WechatCodeManager;
import com.voxlearning.utopia.service.wechat.impl.dao.*;
import com.voxlearning.utopia.service.wechat.impl.queue.WechatQueueProducer;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.WechatNoticeProcessor;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.WechatNoticeProcessorManager;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.WechatNoticeService;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message.WechatTemplateMessageProcessor;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message.WechatTemplateMessageProcessorManager;
import com.voxlearning.utopia.service.wechat.impl.support.WechatCacheClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Default {@link WechatService} implementation.
 *
 * @author Xin Xin
 * @author Shuai Huan
 * @author Xiaohai Zhang
 * @since Apr 16, 2014
 */
@Named("com.voxlearning.utopia.service.wechat.impl.service.WechatServiceImpl")
@ExposeServices({
        @ExposeService(interfaceClass = WechatService.class,version = @ServiceVersion(version = "20180620")),
        @ExposeService(interfaceClass = WechatService.class,version = @ServiceVersion(version = "20190114"))
})
public class WechatServiceImpl extends SpringContainerSupport implements WechatService {

    @Inject private WechatQueueProducer wechatQueueProducer;

    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserWechatRefPersistence userWechatRefPersistence;
    @Inject private WechatFaceInviteRecordPersistence wechatFaceInviteRecordPersistence;
    @Inject private WechatFaqPersistence wechatFaqPersistence;
    @Inject private WechatFollowerPersistence wechatFollowerPersistence;
    @Inject private WechatLittleChampionPersistence wechatLittleChampionPersistence;
    @Inject private WechatLoaderImpl wechatLoader;
    @Inject private WechatNoticePersistence wechatNoticePersistence;
    @Inject private WechatNoticeProcessorManager wechatNoticeProcessorManager;
    @Inject private WechatNoticeService wechatNoticeService;
    @Inject private WechatQuestionPersistence wechatQuestionPersistence;
    @Inject private WechatRedPackHistoryPersistence wechatRedPackHistoryPersistence;
    @Inject private WechatShippingAddressPersistence wechatShippingAddressPersistence;
    @Inject private WechatSignPersistence wechatSignPersistence;
    @Inject private WechatSupportUserPersistence wechatSupportUserPersistence;
    @Inject private WechatTicketPersistence wechatTicketPersistence;
    @Inject private WechatCacheClient wechatCacheClient;
    @Inject private UserMiniProgramRefPersistence userMiniProgramRefPersistence;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private WechatTemplateMessageProcessorManager wechatTemplateMessageProcessorManager;
    @Inject private WechatCodeManager wechatCodeManager;

    @Override
    public MapMessage signIn(Long userId, String signDate) {
        if (userId == null || signDate == null) {
            return MapMessage.errorMessage();
        }
        User parent = userLoaderClient.loadUser(userId, UserType.PARENT);
        if (parent == null) {
            logger.warn("User {} not found or disabled", userId);
            return MapMessage.errorMessage("User not found or disabled");
        }
        if (wechatSignPersistence.findByUserIdAndSignDate(userId, signDate) != null) {
            return MapMessage.errorMessage("User has signed this month");
        }
        WechatSign wechatSign = new WechatSign();
        wechatSign.setUserId(userId);
        wechatSign.setSignDate(signDate);
        wechatSignPersistence.insert(wechatSign);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage saveWechatShippingAddress(Long userId, String phone, String detailAddress, String postCode) {
        if (wechatShippingAddressPersistence.findByUserId(userId) == null) {
            WechatShippingAddress address = new WechatShippingAddress();
            address.setUserId(userId);
            address.setDetailAddress(detailAddress);
            address.setPostCode(postCode);
            address.setSensitivePhone(sensitiveUserDataServiceClient.encodeMobile(phone));
            wechatShippingAddressPersistence.insert(address);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage submitQuestion(String openId, String question, SourceType sourceType) {
        if (StringUtils.isEmpty(openId) || StringUtils.isEmpty(question)) {
            return MapMessage.errorMessage("无效的参数");
        }

        WechatQuestion wechatQuestion = WechatQuestion.newInstance(
                openId,
                StringUtils.filterEmojiForMysql(question),
                WechatQuestionState.WAITING,
                sourceType
        );
        wechatQuestionPersistence.insert(wechatQuestion);
        return MapMessage.successMessage().add("id", wechatQuestion.getId());
    }

    @Override
    public List<WechatQuestion> findWechatQuestionByCreateTimeOrState(int state, Date start, Date end) {
        return wechatQuestionPersistence.findWechatQuestionByCreateTimeOrState(state, start, end);
    }

    @Override
    public MapMessage processWechatQuestion(Long id, String desc, int state, String operator) {
        MapMessage message = new MapMessage();
        try {
            if (StringUtils.isEmpty(desc)) {
                message.setSuccess(false);
                message.setInfo("备注不能为空");
                return message;
            }

            WechatQuestion wechatQuestion = wechatQuestionPersistence.load(id);
            if (null == wechatQuestion) {
                message.setSuccess(false);
                message.setInfo("反馈id无效");
                return message;
            }
            wechatQuestion.setReply(desc);
            wechatQuestion.setReplyer(operator);
            wechatQuestion.setState(state);
            wechatQuestionPersistence.replace(wechatQuestion);

            message.setSuccess(true);
            message.setInfo("处理完成");
        } catch (Exception ex) {
            logger.error("处理注册验证反馈失败，[id:{},desc:{},state:{}],msg:{}", id, desc, state, ex.getMessage(), ex);
            message.setSuccess(false);
            message.setInfo("处理失败");
        }
        return message;
    }

    @Override
    public MapMessage bindUserAndWechat(Long userId, String openId, String source, Integer type) {
        UserWechatRef refexist = userWechatRefPersistence.findByOpenId(openId);
        if (null != refexist) {
            return MapMessage.successMessage().add("id", refexist.getId());
        }
        UserWechatRef ref = new UserWechatRef();
        ref.setUserId(userId);
        ref.setOpenId(openId);
        ref.setType(type);
        ref.setDisabled(Boolean.FALSE);
        ref.setCreateDatetime(new Date());
        ref.setUpdateDatetime(new Date());
        if (null != source) {
            ref.setSource(source);
        }
        userWechatRefPersistence.insert(ref);
        Long id = ref.getId();
        return MapMessage.successMessage().add("id", id).add("result", new BindResultMapper(userId, true));
    }

    @Override
    public MapMessage bindUserAndWechat(Long userId, String openId, String unionId, String source, Integer type) {
        if (userId == null || StringUtils.isBlank(openId) || StringUtils.isBlank(unionId) || type == null) {
            return MapMessage.errorMessage("参数错误");
        }
        UserWechatRef userWechatRef = userWechatRefPersistence.findByUnionId(unionId);
        if (userWechatRef != null) {
            return MapMessage.successMessage();
        }
        userWechatRef = new UserWechatRef();
        userWechatRef.setUserId(userId);
        userWechatRef.setOpenId(openId);
        userWechatRef.setType(type);
        userWechatRef.setUnionId(unionId);
        userWechatRef.setDisabled(Boolean.FALSE);
        userWechatRef.setCreateDatetime(new Date());
        userWechatRef.setUpdateDatetime(new Date());
        if (null != source) {
            userWechatRef.setSource(source);
        }
        userWechatRefPersistence.insert(userWechatRef);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage bindUserAndMiniProgramOrRelogin(Long userId, String openId, String source, MiniProgramType type) {
        UserMiniProgramRef findRef = userMiniProgramRefPersistence.findByOpenId(openId, type.getType());
        if (null != findRef) {
            // 干掉之前的记录  重新绑定新的
            userMiniProgramRefPersistence.deleteUserMiniProgramRefByOpenId(openId, type.getType());
        }
        UserMiniProgramRef refForUser = userMiniProgramRefPersistence.findByUserId(userId, type.getType());
        if (refForUser != null) {
            // 干掉之前的记录  重新绑定新的
            userMiniProgramRefPersistence.deleteUserMiniProgramRefByUserId(userId, type.getType());
        }

        UserMiniProgramRef ref = new UserMiniProgramRef();
        ref.setUserId(userId);
        ref.setOpenId(openId);
        ref.setType(type.getType());
        ref.setDisabled(Boolean.FALSE);
        ref.setCreateDatetime(new Date());
        ref.setUpdateDatetime(new Date());
        ref.setLastLoginDate(new Date());
        if (null != source) {
            ref.setSource(source);
        }
        userMiniProgramRefPersistence.insert(ref);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage unbindUserAndWechat(String openId) {
        UserWechatRef ref = userWechatRefPersistence.findByOpenId(openId);
        Long userId = null;
        if (null != ref) {
            userId = ref.getUserId();
            ref.setDisabled(true);
            ref.setUpdateDatetime(new Date());
            userWechatRefPersistence.replace(ref);
            wechatNoticePersistence.deleteMessage(openId);
        }
        wechatFollowerPersistence.unFollow(openId);
        return MapMessage.successMessage().add("result", new BindResultMapper(userId, userId != null));
    }

    @Override
    public MapMessage unbindUserAndWechatWithUserIdAndType(Long userId, Integer wechatType) {
        List<UserWechatRef> userWechatRefs = userWechatRefPersistence.findByUserId(userId, wechatType);
        if (CollectionUtils.isNotEmpty(userWechatRefs)) {
            userWechatRefs.forEach(ref -> {
                if (null != ref) {
                    ref.setDisabled(true);
                    ref.setUpdateDatetime(new Date());
                    userWechatRefPersistence.replace(ref);
                    wechatNoticePersistence.deleteMessage(ref.getOpenId());
                    wechatFollowerPersistence.unFollow(ref.getOpenId());
                }
            });
        }
        return MapMessage.successMessage().add("result", new BindResultMapper(userId, userId != null));
    }

    @Override
    public MapMessage follow(String openId, Integer wechatType) {
        if (StringUtils.isEmpty(openId) || wechatType == null || WechatType.of(wechatType) == null) {
            return MapMessage.errorMessage();
        }
        WechatFollower follower = wechatFollowerPersistence.findByOpenId(openId);
        if (null == follower) {
            follower = new WechatFollower();
            follower.setType(wechatType);
            follower.setOpenId(openId);
            wechatFollowerPersistence.insert(follower);
        }
        return MapMessage.successMessage();
    }

    @Override
    public void processWechatNotice(WechatNoticeProcessorType type, Clazz clazz, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType) {
        WechatNoticeProcessor processor = wechatNoticeProcessorManager.get(type);
        if (processor != null) {
            processor.process(clazz, teacher, extensionInfo, wechatType);
        }
    }

    @Override
    public void processWechatNotice(WechatNoticeProcessorType type, Long clazzId, Long groupId, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType) {
        WechatNoticeProcessor processor = wechatNoticeProcessorManager.get(type);
        if (processor != null) {
            processor.process(clazzId, groupId, teacher, extensionInfo, wechatType);
        }
    }

    @Override
    public void processWechatNotice(WechatNoticeProcessorType type, List<User> students, Teacher teacher, Long clazzId, Map<String, Object> extensionInfo, WechatType wechatType) {
        WechatNoticeProcessor processor = wechatNoticeProcessorManager.get(type);
        if (processor != null) {
            processor.processWithSpecificUsers(students, teacher, clazzId, extensionInfo, wechatType);
        }
    }

    @Override
    public void processWithStudents(WechatNoticeProcessorType type, Collection<Long> studentIds, Map<String, Object> extensionInfo, WechatType wechatType) {
        WechatNoticeProcessor processor = wechatNoticeProcessorManager.get(type);
        if (processor != null) {
            processor.processWithStudents(studentIds, extensionInfo, wechatType);
        }
    }

    @Override
    public void processWechatNotice(WechatNoticeProcessorType type, Long userId, Map<String, Object> extensionInfo, WechatType wechatType) {
        WechatNoticeProcessor processor = wechatNoticeProcessorManager.get(type);
        if (processor != null) {
            processor.processSingleUser(userId, extensionInfo, wechatType);
        }
    }

    @Override
    public void processWechatNoticeNoWait(WechatNoticeProcessorType type, Long userId, Map<String, Object> extensionInfo, WechatType wechatType) {
        processWechatNotice(type, userId, extensionInfo, wechatType);
    }

    @Override
    public void processWechatNoticeByOpenId(WechatNoticeProcessorType type, String openId, Map<String, Object> extensionInfo, WechatType wechatType) {
        WechatNoticeProcessor processor = wechatNoticeProcessorManager.get(type);
        if (processor != null) {
            // 只根据openId发送就只直接假定用户id是10000
            Long userId = 10000L;
            processor.processSingleUserOpenId(userId, openId, extensionInfo);
        }
    }

    @Override
    public void processWechatNotice(WechatNoticeProcessorType type, Long userId, String openId, Map<String, Object> extensionInfo) {
        WechatNoticeProcessor processor = wechatNoticeProcessorManager.get(type);
        if (processor != null) {
            processor.processSingleUserOpenId(userId, openId, extensionInfo);
        }
    }

    @Override
    public void processWechatNotice(WechatNoticeProcessorType type, Map<Long, List<String>> user_openId_map, Map<String, Object> extensionInfo) {
        WechatNoticeProcessor processor = wechatNoticeProcessorManager.get(type);
        if (processor != null) {
            processor.processMultiUsersMultiOpenIds(user_openId_map, extensionInfo);
        }
    }

    @Override
    public MapMessage processWechatTemplateMessageNotice(Long userId, String messageTypeName, Map<String, WechatTemplateData> templateDataMap, Map<String, Object> activityParamMap) {
        WechatTemplateMessageProcessor messageProcessor = wechatTemplateMessageProcessorManager.get(WechatTemplateMessageType.of(messageTypeName));
        if (messageProcessor != null) {
            messageProcessor.process(userId, templateDataMap, activityParamMap);
        }
        return MapMessage.successMessage();
    }

    @Override
    public List<Map<String, Object>> loadNoticeByMessageType(Integer type) {
        return wechatNoticeService.loadNoticeByMessageType(type);
    }

    @Override
    public int updateNoticeStateTo5(List<Long> noticeIds) {
        return wechatNoticeService.updateNoticeStateTo5(noticeIds);
    }

    @Override
    public List<Map<String, Object>> loadNoticeByMessageTypeForCrm(Integer type) {
        return wechatNoticeService.loadNoticeByMessageTypeForCrm(type);
    }

    @Override
    public List<WechatNotice> loadAfentiEKUnsentMessage(Long id) {
        return wechatNoticeService.loadAfentiEKUnsentMessage(id);
    }

    @Override
    public void updateNoticeState(String openId, String messageId, WechatNoticeState state, String errorCode) {
        wechatNoticeService.updateNoticeState(openId, messageId, state, errorCode);
    }

    @Override
    public void updateNoticeState(Long id, WechatNoticeState state, String errorCode) {
        wechatNoticeService.updateNoticeState(id, state, errorCode);
    }

    @Override
    public void updateNoticeMessageId(Long id, String messageId) {
        wechatNoticeService.updateNoticeMessageId(id, messageId);
    }

    @Override
    public void updateMessageStateByType(Integer type) {
        wechatNoticeService.updateMessageStateByType(type);
    }

    @Override
    public void deleteMessageStateByType(Integer type) {
        wechatNoticeService.deleteMessageStateByType(type);
    }

    @Override
    public MapMessage persistWechatNotice(WechatNotice notice) {
        if (notice == null) {
            return MapMessage.errorMessage();
        }
        Long id = wechatNoticePersistence.persist(notice);
        return MapMessage.successMessage().add("id", id);
    }

    @Override
    public MapMessage addWechatSupportUser(String openId, String name) {
        WechatSupportUser user = new WechatSupportUser();
        user.setOpenId(openId);
        user.setName(name);
        user.setDisabled(false);
        user.setCreateDatetime(new Date());
        user.setUpdateDatetime(new Date());
        wechatSupportUserPersistence.insert(user);
        return MapMessage.successMessage().add("id", user.getId());
    }

    @Override
    public MapMessage addOrUpdateWechatFaq(WechatFaq wechatFaq) {
        if (wechatFaq == null) {
            return MapMessage.errorMessage();
        }
        try {
            long id;
            if (wechatFaq.getId() != null) {
                id = wechatFaq.getId();
                wechatFaqPersistence.replace(wechatFaq);
            } else {
                wechatFaqPersistence.insert(wechatFaq);
                id = wechatFaq.getId();
            }
            return MapMessage.successMessage().add("id", id);
        } catch (Exception ex) {
            logger.error("Failed to create/update wechatFaq", ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public void changeFaqsStatus(List<Long> ids, String status) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        if (!status.equals("published") && !status.equals("draft")) {
            return;
        }
        wechatFaqPersistence.changeFaqsStatus(ids, status);
    }

    @Override
    public void removeFaqs(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        wechatFaqPersistence.removeFaqs(ids);
    }

    @Override
    public int sendNoticeMessage(Long id) {
        if (id == null) {
            return 0;
        }
        Date expireTime = DateUtils.addHours(new Date(), 2);
        return wechatNoticePersistence.updateStateAndExpireTimeById(id, WechatNoticeState.WAITTING.getType(), expireTime);
    }

    @Override
    public Map<Long, Set<Long>> studentBindWechatParentMap(Collection<Long> studentIds) {
        Map<Long, List<StudentParentRef>> studentParentMap = studentLoaderClient.loadStudentParentRefs(studentIds);
        List<Long> parentIds = new ArrayList<>();
        for (Long studentId : studentParentMap.keySet()) {
            for (StudentParentRef studentParentRef : studentParentMap.get(studentId)) {
                parentIds.add(studentParentRef.getParentId());
            }
        }
        //查询绑定微信的家长
        Map<Long, List<UserWechatRef>> parentWechatRefMap = wechatLoader.loadUserWechatRefs(parentIds, WechatType.PARENT);
        Map<Long, Set<Long>> studentWechatRefMap = new HashMap<>();
        for (Long studentId : studentParentMap.keySet()) {
            for (StudentParentRef studentParentRef : studentParentMap.get(studentId)) {
                Long parentId = studentParentRef.getParentId();
                List<UserWechatRef> userWechatRefList = parentWechatRefMap.get(parentId);
                if (userWechatRefList != null && userWechatRefList.size() > 0) {
                    if (studentWechatRefMap.get(studentId) != null) {
                        studentWechatRefMap.get(studentId).add(parentId);
                    } else {
                        Set<Long> pids = new HashSet<>();
                        pids.add(parentId);
                        studentWechatRefMap.put(studentId, pids);
                    }
                }
            }
        }
        return studentWechatRefMap;
    }

    @Override
    public void saveWechatTicket(WechatTicket wechatTicket) {
        wechatTicketPersistence.insert(wechatTicket);
    }

    @Override
    public void saveWechatFaceInviteRecord(WechatFaceInviteRecord record) {
        wechatFaceInviteRecordPersistence.insert(record);
    }

    @Override
    public void saveWechatRedPackHistory(WechatRedPackHistory packHistory) {
        wechatRedPackHistoryPersistence.insert(packHistory);
    }

    @Override
    @Deprecated
    public Long saveWechatLittleChampion(WechatLittleChampion champion) {
        wechatLittleChampionPersistence.insert(champion);
        return champion.getId();
    }

    @Override
    @Deprecated
    public void updateWechatLittleChampion(WechatLittleChampion champion) {
        wechatLittleChampionPersistence.replace(champion);
    }

    @Override
    public List<Map<String, Object>> loadNoticeTypes() {
        return wechatNoticeService.loadNoticeTypes();
    }

    @Override
    public List<Map<String, Object>> loadSqlExecutors() {
        return wechatNoticeService.loadSqlExecutors();
    }

    @Override
    public void updateNoticeSqlState(long id, int state, long count) {
        wechatNoticeService.updateNoticeSqlState(id, state, count);
    }

    @Override
    public AlpsFuture<Object> getFromPersistenceCache(String key) {
        UtopiaCache persistence = wechatCacheClient.getWechatCacheSystem().CBS.persistence;
        Object ret = persistence.load(key);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<Boolean> addIntoPersistenceCache(String key, int expirationInSeconds, Object value) {
        UtopiaCache persistence = wechatCacheClient.getWechatCacheSystem().CBS.persistence;
        boolean ret = persistence.add(key, expirationInSeconds, value);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public void sendMessage(Message message) {
        if (message != null) {
            wechatQueueProducer.getProducer().produce(message);
        }
    }

    public String createQrcode(CreateQrcodeReq createQrcodeReq) {
        String resultStr = "";
        String showQrcodeUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=";
        int createQrcodeType = createQrcodeReq.getCreateQrcodeType();
        long sceneId = SafeConverter.toLong(createQrcodeReq.getSceneId());
        String sceneStr = SafeConverter.toString(createQrcodeReq.getSceneStr());
        long expireSeconds = SafeConverter.toLong((createQrcodeReq.getExpireSeconds()));
        String data = checkCreateQrcodeReq(expireSeconds, createQrcodeType, sceneId, sceneStr);
        if (StringUtils.isNotBlank(data)) {
            String qrcodeTicket = generateQrcodeTicket(data);
            if (StringUtils.isNotBlank(qrcodeTicket)) {
                resultStr = showQrcodeUrl + qrcodeTicket;
            }
        }
        return resultStr;
    }

    public String generateQrcodeTicket(String data) {
        String qrcodeTicketUrl = "https://api.weixin.qq.com/cgi-bin/qrcode/create";
        String accessToken = wechatCodeManager.generateAccessToken(WechatType.CHIPS);
        String ticket = "";
        if (StringUtils.isNotBlank(accessToken)) {
            // 生成微信动态二维码的ticket
            String url = qrcodeTicketUrl + "?access_token=" + accessToken;
            String r = HttpRequestExecutor.defaultInstance().post(url).json(data).execute().getResponseString();
            Map<String, String> mapTicket = JsonUtils.fromJsonToMapStringString(r);
            if (null == mapTicket) {
                throw new UtopiaRuntimeException("生成ticket请求微信未响应");
            }
            if (mapTicket.containsKey("ticket")) {
                ticket = mapTicket.get("ticket");
            } else {
                throw new UtopiaRuntimeException("生成ticket失败，" + r);
            }
        }
        return ticket;
    }

    private String checkCreateQrcodeReq(long expireSeconds, int createQrcodeType, long sceneId, String sceneStr) {
        String result = "";
        JSONObject dataMap = new JSONObject();
        Map<String, Object> actionInfoMap = new HashMap<>();
        Map<String, Object> sceneMap = new HashMap<>();
        CreateQrcodeType qrcodeType = CreateQrcodeType.of(createQrcodeType);

        if (!Objects.isNull(qrcodeType)) {
            if (Objects.equals(CreateQrcodeType.TEMPORARY_INT_QR.getType(), createQrcodeType)) {
                if (sceneId > 0L && sceneId <= 4294967295L) {
                    sceneMap.put("scene_id", sceneId);
                }
            } else if (Objects.equals(CreateQrcodeType.LIMIT_INT_QR.getType(), createQrcodeType)) {
                if (sceneId > 0L && sceneId <= 100000L) {
                    sceneMap.put("scene_id", sceneId);
                }
            } else {
                if (sceneStr.length() > 0 && sceneStr.length() <= 64) {
                    sceneMap.put("scene_str", sceneStr);
                }
            }
            if (!sceneMap.isEmpty()) {
                actionInfoMap.put("scene", sceneMap);
                dataMap.put("action_info", actionInfoMap);
                dataMap.put("action_name", qrcodeType.getQRscene());
                if (Objects.equals(CreateQrcodeType.TEMPORARY_INT_QR.getType(), createQrcodeType) || Objects.equals(CreateQrcodeType.TEMPORARY_STR_QR.getType(), createQrcodeType)) {
                    if (expireSeconds > 0L && expireSeconds <= 2592000L) {
                        dataMap.put("expire_seconds", expireSeconds);
                    }
                }
                result = dataMap.toJSONString();
            }
        }
        return result;
    }
}
