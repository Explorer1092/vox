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

package com.voxlearning.utopia.agent.service.notify;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.*;
import com.voxlearning.utopia.agent.dao.mongo.AgentTargetTagDao;
import com.voxlearning.utopia.agent.dao.mongo.tag.AgentTagTargetDao;
import com.voxlearning.utopia.agent.persist.AgentNotifyPersistence;
import com.voxlearning.utopia.agent.persist.AgentNotifyUserPersistence;
import com.voxlearning.utopia.agent.persist.AgentTagPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentNotify;
import com.voxlearning.utopia.agent.persist.entity.AgentNotifyUser;
import com.voxlearning.utopia.agent.persist.entity.AgentTargetTag;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTagTarget;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.messagecenter.AgentMessageService;
import com.voxlearning.utopia.agent.service.mobile.AgentAuditService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 站内通知/消息服务
 * Created by Shuai.Huan on 2014/7/21.
 */
@Named
public class AgentNotifyService extends AbstractAgentService {

    @Inject private AgentNotifyPersistence agentNotifyPersistence;
    @Inject private AgentNotifyUserPersistence agentNotifyUserPersistence;
    @Inject private AgentTargetTagDao agentTargetTagDao;
    @Inject private AgentCacheSystem agentCacheSystem;

    @Inject private AgentMessageService agentMessageService;

    @Inject
    private AgentTagTargetDao agentTagTargetDao;
    @Inject
    private AgentTagPersistence agentTagPersistence;

//    private Map<Integer, AgentNotifyType> agentNotifyTypeMap = AgentNotifyType.toMap();

//    private static final List<AgentNotifyType> mobileAvailableNotify = Arrays.asList(
//            AgentNotifyType.WEEKLY_REPORT.getDesc(),
//            AgentNotifyType.MONTHLY_REPORT.getDesc(),
//            AgentNotifyType.VISIT_SUGGEST.getDesc(),
//            AgentNotifyType.VISIT_REMIND.getDesc(),
//            AgentNotifyType.IMPORTANT_NOTICE.getDesc(),
//            AgentNotifyType.PLATFORM_UPDATE.getDesc(),
//            AgentNotifyType.ALTERATION_REMIND.getDesc()
//    );

    public static final List<AgentNotifyType> AVAILABLE_NOTIFY_TYPE = Arrays.asList(
            AgentNotifyType.ORDER_NOTICE,
            AgentNotifyType.UNIFIED_EXAM_APPLY,
            AgentNotifyType.MODIFY_DICT_SCHOOL_APPLY,
            AgentNotifyType.REVIEW_SCHOOL_CLUE,
            AgentNotifyType.DATA_REPORT_APPLY,
            AgentNotifyType.NEW_REGISTER_TEACHER,
            AgentNotifyType.ALTERATION_REMIND_NEW,
            AgentNotifyType.ORDER_DELIVERY_NOTICE,
            AgentNotifyType.PENDING_AUDIT,
            AgentNotifyType.REVIEW_TEACHER_FAKE,
            AgentNotifyType.PRODUCT_FEEDBACK_NOTICE,
            AgentNotifyType.INTO_SCHOOL_REACH_WARNING,
            AgentNotifyType.INTO_SCHOOL_WARNING,
            AgentNotifyType.SCHOOL_INFO_MODIFY,
            AgentNotifyType.VISIT_FEEDBACK,
            AgentNotifyType.GROUP_MESSAGE,
            AgentNotifyType.GR_RESOURCE_APPLY
    );

    /**
     * 发送消息
     *
     * @param type      消息类型
     * @param content   消息内容
     * @param receivers 消息接受者列表
     */
    public Long sendNotify(Integer type, String content, Collection<Long> receivers) {
        return sendNotify(type, null, content, receivers, null, null);
    }

    public Long sendNotify(Integer type, String title, String content, Collection<Long> receivers, String url) {
        return sendNotify(type, title, content, receivers, null, null, url);
    }

    public Long sendNotify(Integer type, String title, String content, Collection<Long> receivers, String file1, String file2) {
        return sendNotify(type, title, content, receivers, file1, file2, null);
    }

    public Long sendNotify(Integer type, String title, String content, Collection<Long> receivers,
                           String file1, String file2, String url) {
        return sendNotifyWithTags(type, title, content, receivers, file1, file2, url, null);
    }
    public Long sendNotifyWithTags(Integer type, String title, String content, Collection<Long> receivers,
                                   String file1, String file2, String url, List<Long> tagIds) {

        if (type == null || AgentNotifyType.fetchByType(type) == null || StringUtils.isBlank(content) || CollectionUtils.isEmpty(receivers)) {
            return null;
        }

        AgentNotify agentNotify = new AgentNotify();
        agentNotify.setNotifyType(AgentNotifyType.fetchByType(type).getDesc());
        if (StringUtils.isNotEmpty(title)) {
            agentNotify.setNotifyTitle(title);
        }
        if (StringUtils.length(content) <= 150) {
            agentNotify.setNotifyContent(content);
        } else {
            agentNotify.setNotifyContent(StringUtils.substring(content, 0, 150) + "...");
        }
        if (StringUtils.isNotEmpty(file1)) {
            agentNotify.setFile1(file1);
        }
        if (StringUtils.isNotEmpty(file2)) {
            agentNotify.setFile2(file2);
        }
        if (StringUtils.isNotEmpty(url)) {
            agentNotify.setNotifyUrl(url);
        }
        Long notifyId = agentNotifyPersistence.persist(agentNotify);
        receivers = CollectionUtils.toLinkedHashSet(receivers);
        saveNotifyUser(type,notifyId,receivers);
//        for (Long receiver : receivers) {
//            AgentNotifyUser agentNotifyUser = new AgentNotifyUser();
//            agentNotifyUser.setNotifyId(notifyId);
//            agentNotifyUser.setUserId(receiver);
//            agentNotifyUser.setNotifyType(AgentNotifyType.fetchByType(type));
//            agentNotifyUser.setReadFlag(false);
//            agentNotifyUserPersistence.persist(agentNotifyUser);
//
//            if(AVAILABLE_NOTIFY_TYPE.contains(AgentNotifyType.fetchByType(type)) && agentCacheSystem.getUserUnreadNotifyCount(receiver) != null){
//                agentCacheSystem.incrUserUnreadNotifyCount(receiver);
//            }
//        }

        if(CollectionUtils.isNotEmpty(tagIds)){
            addNotifyTags(notifyId, tagIds);
        }
        return notifyId;
    }

    public void addNotifyTags(Long notifyId, List<Long> tagIds){
        if(CollectionUtils.isEmpty(tagIds)){
            return;
        }
        AgentNotify agentNotify = agentNotifyPersistence.load(notifyId);
        if(agentNotify == null){
            return;
        }
//        AgentTargetTag targetTags = agentTargetTagDao.loadByTarget(notifyId, AgentTargetType.NOTIFIY);
//        if(targetTags == null){
//            targetTags = new AgentTargetTag();
//            targetTags.setTargetId(notifyId);
//            targetTags.setTargetType(AgentTargetType.NOTIFIY);
//            targetTags.setTags(new ArrayList<>());
//        }
//
//        for(AgentTag tag : tags){
//            targetTags.addTag(tag);
//        }
//
//        agentTargetTagDao.upsert(targetTags);

        List<AgentTagTarget> newTagTargetList = new ArrayList<>();
        Map<String, List<AgentTagTarget>> tagTargetMap = agentTagTargetDao.loadByTargetIdsAndType(Collections.singleton(SafeConverter.toString(notifyId)), AgentTagTargetType.NOTIFY);
        List<AgentTagTarget> tagTargetList = tagTargetMap.get(SafeConverter.toString(notifyId));
        if (CollectionUtils.isNotEmpty(tagTargetList)){
            List<AgentTagTarget> rejectNotifyTagList = tagTargetList.stream().filter(p -> tagIds.contains(p.getTagId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(rejectNotifyTagList)){
                tagIds.forEach(tagId -> {
                    AgentTagTarget tagTarget = new AgentTagTarget();
                    tagTarget.setTagId(tagId);
                    tagTarget.setTargetId(SafeConverter.toString(notifyId));
                    tagTarget.setTargetType(AgentTagTargetType.NOTIFY);
                    newTagTargetList.add(tagTarget);
                });
            }
        }
        if (CollectionUtils.isNotEmpty(newTagTargetList)){
            agentTagTargetDao.inserts(newTagTargetList);
        }
    }

    /**
     * 设置消息已读
     *
     * @param userId   用户id
     * @param notifyId 通知id
     */
    public MapMessage readNotify(Long userId, Long notifyId) {
        if (userId == null || notifyId == null) {
            return MapMessage.errorMessage("参数异常");
        }
        boolean success = false;
        AgentNotify agentNotify = agentNotifyPersistence.load(notifyId);
        if (agentNotify != null) {
            AgentNotifyUser agentNotifyUser = agentNotifyUserPersistence.findByUserIdAndNotifyId(userId, notifyId);
            if (agentNotifyUser != null) {
                agentNotifyUser.setReadFlag(true);
                success = agentNotifyUserPersistence.update(agentNotifyUser.getId(), agentNotifyUser);
                if(AVAILABLE_NOTIFY_TYPE.contains(agentNotifyUser.getNotifyType())){
                    agentCacheSystem.decrUserUnreadNotifyCount(userId);
                }
                agentNotify.setOpenNum(agentNotify.getOpenNum() == null ? 1 : agentNotify.getOpenNum() + 1);
                agentNotifyPersistence.update(notifyId,agentNotify);
            }
        }
        MapMessage message = new MapMessage();
        message.setSuccess(success);
        return message;
    }

    /**
     * 获取通知一览
     *
     * @param userId 用户id
     * @return 通知一览
     */
    public List<Map<String, Object>> getNotifiesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> agentNotifies = new LinkedList<>();
        List<AgentNotifyUser> agentNotifyUsers = agentNotifyUserPersistence.findByUserId(userId);
        if (CollectionUtils.isNotEmpty(agentNotifyUsers)) {
            for (AgentNotifyUser agentNotifyUser : agentNotifyUsers) {
                AgentNotify agentNotify = agentNotifyPersistence.load(agentNotifyUser.getNotifyId());
                if (agentNotify != null){
                    agentNotifies.add(mapNotifyInfo(setNotifyTag(agentNotify), agentNotifyUser.getReadFlag()));
                }
            }
            return agentNotifies;
        }
        return Collections.emptyList();
    }


    public int getUnreadNotifiesCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return (int) agentNotifyUserPersistence.findByUserId(userId)
                .stream()
                .filter(t -> !SafeConverter.toBoolean(t.getReadFlag()))
                .count();
    }


    private Map<String, Object> mapNotifyInfo(AgentNotify notify, boolean readFlag) {
        Map<String, Object> info = BeanMapUtils.tansBean2Map(notify);
        info.put("readFlag", readFlag);
        return info;
    }

    private AgentNotify setNotifyTag(AgentNotify notify){
//        AgentTargetTag targetTags = agentTargetTagDao.loadByTarget(notify.getId(), AgentTargetType.NOTIFIY);
        Map<String, List<AgentTagTarget>> tagTargetMap = agentTagTargetDao.loadByTargetIdsAndType(Collections.singleton(SafeConverter.toString(notify.getId())), AgentTagTargetType.NOTIFY);
        List<AgentTagTarget> tagTargetList = tagTargetMap.get(SafeConverter.toString(notify.getId()));
        if (CollectionUtils.isNotEmpty(tagTargetList)){
            Set<Long> tagIds = tagTargetList.stream().map(AgentTagTarget::getTagId).collect(Collectors.toSet());
            Map<Long, com.voxlearning.utopia.agent.persist.entity.tag.AgentTag> tagMap = agentTagPersistence.loads(tagIds);

            List<String> tagList = new ArrayList<>();
            tagTargetList.forEach(item -> {
                com.voxlearning.utopia.agent.persist.entity.tag.AgentTag tag = tagMap.get(item.getTagId());
                if (tag != null && Objects.equals(tag.getName(), "驳回")){
                    tagList.add("REJECT");
                }
            });
            notify.setTagList(tagList);
        }
        return notify;
    }

    private List<AgentNotifyUser> getNotifyUserListByType(Long userId, List<AgentNotifyType> typeList) {
        if (userId == null || CollectionUtils.isEmpty(typeList)) {
            return Collections.emptyList();
        }
        List<AgentNotifyUser> agentNotifyUsers = agentNotifyUserPersistence.findByUserId(userId);
        if (CollectionUtils.isEmpty(agentNotifyUsers)) {
            return Collections.emptyList();
        }
        return agentNotifyUsers.stream().filter(notify -> notify.getNotifyType() != null && typeList.contains(notify.getNotifyType())).collect(Collectors.toList());
    }

    private List<Map<String, Object>> generateNotifyInfo(List<AgentNotifyUser> agentNotifyUsers){
        if(CollectionUtils.isEmpty(agentNotifyUsers)){
            return Collections.emptyList();
        }
        Map<Long, Boolean> notifyReadMap = agentNotifyUsers.stream().collect(Collectors.toMap(AgentNotifyUser::getNotifyId, AgentNotifyUser::getReadFlag,(o1, o2) -> o1));
        return agentNotifyPersistence.loads(notifyReadMap.keySet()).values().stream()
                .sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime()))
                .map(p -> mapNotifyInfo(setNotifyTag(p == null ? new AgentNotify() : p),
                        Boolean.TRUE.equals(notifyReadMap.get(p == null ? 0L : p.getId()))))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getNotifyListByType(Long userId, List<AgentNotifyType> typeList){
        List<AgentNotifyUser> agentNotifyUsers = getNotifyUserListByType(userId, typeList);
        return generateNotifyInfo(agentNotifyUsers);
    }

    private int getUnreadNotifyCountByType(Long userId, List<AgentNotifyType> typeList){
        List<AgentNotifyUser> agentNotifyUsers = getNotifyUserListByType(userId, typeList);
        if (CollectionUtils.isEmpty(agentNotifyUsers)) {
            return 0;
        }
        return (int)agentNotifyUsers.stream().filter(p -> p.getReadFlag() == null || !p.getReadFlag()).count();
    }

    private Map<String, Map<String, Object>> getUnreadCategoryDataByType(Long userId, List<AgentNotifyType> typeList){
        List<AgentNotifyUser> agentNotifyUsers = getNotifyUserListByType(userId, typeList);
        if (CollectionUtils.isEmpty(agentNotifyUsers)) {
            return Collections.emptyMap();
        }
        Map<String, List<AgentNotifyUser>> categoryList = agentNotifyUsers.stream().collect(Collectors.groupingBy(p -> p.getNotifyType().getCategory(), Collectors.toList()));
        Map<String, Map<String, Object>> retMap = new HashMap<>();
        categoryList.forEach((k, v) -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("unreadCount", (int)v.stream().filter(p -> p.getReadFlag() == null || !p.getReadFlag()).count());
            AgentNotifyUser lastestNotifyUser = CollectionUtils.isNotEmpty(v)? v.get(0) : null;
            dataMap.put("notifyId", lastestNotifyUser == null? null : lastestNotifyUser.getNotifyId());
            if(Objects.equals("new_teacher", k) && lastestNotifyUser != null){
                Date startTime = DateUtils.stringToDate(DateUtils.dateToString(lastestNotifyUser.getCreateDatetime(), "yyyyMMdd"), "yyyyMMdd");
                dataMap.put("new_register_teacher_count", (int)v.stream().filter(p -> p.getCreateDatetime().after(startTime)).count());
            }
            retMap.put(k, dataMap);
        });
        return retMap;
    }

    public int getTotalUnreadNotifyCount(MapMessage mapMessage, Long userId){
        Map<String, Integer> unreadCategoryCountMap = new HashMap<>();

        // 获取未读通知数量
        Map<String, Map<String, Object>> unreadCategoryDataMap = this.getUnreadCategoryDataByType(userId, AgentNotifyService.AVAILABLE_NOTIFY_TYPE);
        unreadCategoryDataMap.forEach((k, v) -> {
            if(mapMessage != null){
                int unreadCount = SafeConverter.toInt(v.get("unreadCount"));
                unreadCategoryCountMap.put(k, unreadCount);
                Long notifyId = (Long)v.get("notifyId");
                if(notifyId != null){
                    AgentNotify notify = agentNotifyPersistence.load(notifyId);
                    if(notify != null){
                        if(Objects.equals("new_teacher", k)){ // 新老师注册
                            Integer newTeacherCount = (Integer)v.get("new_register_teacher_count");
                            List<Map<String, Object>> notifyList = getLastRegisteredTeacher(userId, AgentNotifyService.AVAILABLE_NOTIFY_TYPE);
                            if(CollectionUtils.isNotEmpty(notifyList)){
                                String notifyContent = SafeConverter.toString(notifyList.get(0).get("notifyContent"));
                                String[] arr = notifyContent.split("@");
                                StringBuffer sbf = new StringBuffer();
                                if(arr.length > 2){
                                    sbf.append(arr[0]).append(", ").append(StringUtils.isNotBlank(arr[1]) ? Subject.valueOf(arr[1]).getValue() : Subject.UNKNOWN.getValue()).append(", ").append(arr[2]);
                                }
                                v.put("title", sbf.toString());
                            } else {
                                v.put("title", notify.getNotifyTitle());
                            }
                        }else {
                            v.put("title", notify.getNotifyTitle());
                        }
                        v.put("content", notify.getNotifyContent());
                        v.put("time", DateUtils.dateToString(notify.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
                    }
                }
            }
        });
        if(mapMessage != null){
            mapMessage.putAll(unreadCategoryDataMap);
        }
        return unreadCategoryCountMap.values().stream().reduce(0, (x, y) -> x + y);
    }

    /**
     * 获取最新注册的老师
     * @param userId
     */
    public List<Map<String, Object>> getLastRegisteredTeacher(Long userId, List<AgentNotifyType> typeList){
        List<AgentNotifyUser> notifyUsers =  agentNotifyUserPersistence.findByUserId(userId).stream().filter(p -> p.getNotifyType()==AgentNotifyType.NEW_REGISTER_TEACHER).collect(Collectors.toList());
        List<AgentNotifyUser>  list = new ArrayList<AgentNotifyUser>();
        if( CollectionUtils.isNotEmpty(notifyUsers)){
            list.add(notifyUsers.get(0));
            return generateNotifyInfo(list);
        }
        return Collections.emptyList();
    }

    //系统消息发送到个人
    public void saveNotifyUser(Integer type, Long notifyId,Collection<Long> receivers){
//        Set<Long> userIds = CollectionUtils.toLinkedHashSet(receivers);
        for (Long receiver : receivers) {
            AgentNotifyUser agentNotifyUser = new AgentNotifyUser();
            agentNotifyUser.setNotifyId(notifyId);
            agentNotifyUser.setUserId(receiver);
            agentNotifyUser.setNotifyType(AgentNotifyType.fetchByType(type));
            agentNotifyUser.setReadFlag(false);
            agentNotifyUserPersistence.persist(agentNotifyUser);

            if(AVAILABLE_NOTIFY_TYPE.contains(AgentNotifyType.fetchByType(type)) && agentCacheSystem.getUserUnreadNotifyCount(receiver) != null){
                agentCacheSystem.incrUserUnreadNotifyCount(receiver);
            }
        }
        agentMessageService.sendSilentMessageByIds(receivers, AgentPushType.NEW_MESSAGE);
    }

    public AgentNotify getNotifyById(Long id){
        return agentNotifyPersistence.load(id);
    }

    public void updateNotify(AgentNotify agentNotify){
        agentNotifyPersistence.update(agentNotify.getId(),agentNotify);
    }

    public List<AgentNotifyUser> fingNotifyUserListByNotifyId(Long notifyId){
        return agentNotifyUserPersistence.findByNotifyId(notifyId);
    }

}
