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

package com.voxlearning.utopia.service.vendor.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.HourRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.push.api.entity.AppJpushMessageRetry;
import com.voxlearning.utopia.service.push.api.support.PushContext;
import com.voxlearning.utopia.service.push.api.support.PushRetryContext;
import com.voxlearning.utopia.service.push.client.AppJpushMessageRetryServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.VendorManagement;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNoticeType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNotice;
import com.voxlearning.utopia.service.vendor.api.entity.VendorNotify;
import com.voxlearning.utopia.service.vendor.consumer.JxtLoaderClient;
import com.voxlearning.utopia.service.vendor.impl.dao.VendorNotifyPersistence;
import com.voxlearning.utopia.service.vendor.impl.listener.InternalPushRetryQueueSender;
import com.voxlearning.utopia.service.vendor.impl.push.umeng.PushContextBuilder;
import com.voxlearning.utopia.service.vendor.impl.service.AppMessageServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Spring
@Named
@Service(interfaceClass = VendorManagement.class)
@ExposeServices({
        @ExposeService(interfaceClass = VendorManagement.class, version = @ServiceVersion(version = "20160305")),
        @ExposeService(interfaceClass = VendorManagement.class, version = @ServiceVersion(version = "20171115")),
})
public class VendorManagementImpl extends SpringContainerSupport implements VendorManagement {

    @Inject private AppJpushMessageRetryServiceClient appJpushMessageRetryServiceClient;
    @Inject
    private JxtLoaderClient jxtLoaderClient;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private InternalPushRetryQueueSender internalPushRetryQueueSender;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private VendorNotifyPersistence vendorNotifyPersistence;
    @Inject
    private AppMessageServiceImpl appMessageService;


    @Deprecated
    @Override
    public void scheduleReloadCache() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void scheduleDeleteJpushRetryMessage() {
        AlpsThreadPool.getInstance().submit(() -> {
            logger.info("删除WashingtonDatabase下vox_app_jpush_message_retry任务开始");

            //前一天的日期:
            long time = DayRange.current().previous().getEndTime();
            logger.info("删除{}之前的消息", DateUtils.dateToString(DayRange.current().getEndDate(), DateUtils.FORMAT_SQL_DATETIME));
            MapMessage deleteMessage = appJpushMessageRetryServiceClient.getAppJpushMessageRetryService()
                    .cleanUp(time)
                    .getUninterruptibly();

            if (deleteMessage.isSuccess()) {
                logger.info("本次共删除vox_app_jpush_message_retry{}条消息", deleteMessage.get("count"));
            } else {
                logger.info(deleteMessage.getInfo());
            }
            logger.info("删除WashingtonDatabase下vox_app_jpush_message_retry任务结束");
        });
    }

    @Override
    public void scheduleDeleteVendorNotify() {
        AlpsThreadPool.getInstance().submit(() -> {
            logger.info("清除VOX_VENDOR_NOTIFY开始");
            Date date = DateUtils.calculateDateDay(new Date(), -90);
            Criteria criteria = Criteria.where("UPDATE_DATETIME").lt(date);
            long deleted = vendorNotifyPersistence.$remove(criteria);
            logger.info("已经清除 VOX_VENDOR_NOTIFY 共 '{}' 条", deleted);
            logger.info("删除VOX_VENDOR_NOTIFY结束");
        });
    }

    @Override
    public List<VendorNotify> findUndeliveriedNotify() {
        return vendorNotifyPersistence.findUndeliveriedNotify();
    }

    @Override
    public int findTodayDeliveryFailedNotifyCount() {
        return vendorNotifyPersistence.findTodayDeliveryFailedNotifyCount();
    }

    @Override
    public List<VendorNotify> findTodayDeliveryFailedNotify() {
        return vendorNotifyPersistence.findTodayDeliveryFailedNotify();
    }

    @Override
    public void scheduleRemindExpireJxtNotice() {
        HourRange hourRange = HourRange.current().next();
        List<JxtNotice> jxtNoticeList = jxtLoaderClient.getExpireRemindNotice(hourRange.getStartDate(), hourRange.getEndDate());
        if (CollectionUtils.isEmpty(jxtNoticeList)) {
            return;
        }
        //通知中老师名下的group
        Map<Long, Set<Long>> teacherNoticeGroupIds = new HashMap<>();
        jxtNoticeList.forEach(p -> {
            if (p.getNoticeType() == JxtNoticeType.ClAZZ_AFFAIR.getType() && p.getTeacherId() != null && CollectionUtils.isNotEmpty(p.getGroupIds()) && p.getAutoRemind() == Boolean.TRUE) {
                Set<Long> groupIds = teacherNoticeGroupIds.get(p.getTeacherId());
                if (groupIds == null) {
                    groupIds = new HashSet<>();
                }
                groupIds.addAll(p.getGroupIds());
                teacherNoticeGroupIds.put(p.getTeacherId(), groupIds);
            }
        });
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(jxtNoticeList.stream().map(JxtNotice::getTeacherId).collect(Collectors.toSet()));
        //平台上老师名下当前的group
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherMap.keySet(), false);
        Map<Long, Set<Long>> teacherGroupIds = new HashMap<>();
        teacherGroups.keySet().forEach(key -> teacherGroupIds.put(key, teacherGroups.get(key).stream().map(GroupTeacherMapper::getId).collect(Collectors.toSet())));

        teacherNoticeGroupIds.keySet().forEach(key -> {
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(key);
            Long teacherId = mainTeacherId == null ? key : mainTeacherId;
            //这里才是取所有的学科
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
            List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
            List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
            String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";

            String messageContent = teacherMap.get(key) == null ? "" : teacherMap.get(key).fetchRealnameIfBlankId() + subjectsStr + "老师发布的通知还有1小时到期！";
            Set<Long> noticeGroupIds = teacherNoticeGroupIds.get(key);
            Set<Long> teacherGroupId = teacherGroupIds.get(key);
            if (CollectionUtils.isNotEmpty(noticeGroupIds) && CollectionUtils.isNotEmpty(teacherGroupId)) {
                //过滤换班之后已经不在老师名下的班级
                noticeGroupIds = noticeGroupIds.stream().filter(teacherGroupId::contains).collect(Collectors.toSet());
                int time = (int) Math.ceil(noticeGroupIds.size() / 20d);
                time = time == 0 ? 1 : time;
                List<List<Long>> groupIdList = CollectionUtils.splitList(new ArrayList<>(noticeGroupIds), time);
                for (List<Long> list : groupIdList) {
                    //新的极光push
                    Map<String, Object> jpushExtInfo = new HashMap<>();
                    jpushExtInfo.put("studentId", "");
                    jpushExtInfo.put("s", ParentAppPushType.JXT_NOTICE.name());
                    jpushExtInfo.put("url", "");
                    List<String> groupTags   = new LinkedList<>();
                    list.forEach(p->groupTags.add(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(p))));
                    appMessageService.sendAppJpushMessageByTags(messageContent,
                            AppMessageSource.PARENT,
                            groupTags,
                            null,
                            jpushExtInfo);
                }
            }
        });
    }

    @Override
    public void scheduleAutoAppJpushMessageRetry() {
        //每次取数据库3000条。
        //重发条件
        //status: 0
        //retryCount: >0 AND <4
        List<AppJpushMessageRetry> retryList = appJpushMessageRetryServiceClient.getAppJpushMessageRetryService()
                .loadRetryList()
                .getUninterruptibly();

        if (CollectionUtils.isNotEmpty(retryList)) {
            Long current = System.currentTimeMillis();
            for (AppJpushMessageRetry retry : retryList) {
                if (retry == null) {
                    continue;
                }
                if (!DayRange.current().contains(retry.getCreateTime())) {
                    continue;
                }
                Long updateTime = retry.getUpdateTime();
                Double shouldWait = Math.pow(5, retry.getRetryCount()) * 60 * 1000;
                if ((current - updateTime) < shouldWait) {
                    continue;
                }

                if (retry.getPushType() == PushType.UMENG_ANDRIOD || retry.getPushType() == PushType.UMENG_IOS) {
                    PushContext context = JsonUtils.fromJson(retry.getNotify(), PushContext.class);
                    if (null == context) return;

                    PushContext rtyContext = PushContextBuilder.retryInstance(context)
                            .context();
                    ((PushRetryContext) rtyContext).setId(retry.getId().toString());
                    ((PushRetryContext) rtyContext).setPushType(retry.getPushType());

                    internalPushRetryQueueSender.sendUmengNotify(rtyContext);
                } else {
                    String message = retry.getNotify();
                    Map<String, Object> map = JsonUtils.convertJsonObjectToMap(message);
                    map.put("_id", retry.getId().toString());
                    map.put("retryCount", retry.getRetryCount());
                    internalPushRetryQueueSender.sendJpushNotify(map);
                }
            }
        }
    }
}
