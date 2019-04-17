package com.voxlearning.utopia.service.vendor.impl.push.processor.newhomework;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.vendor.api.constant.HomeworkVendorMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNoticeType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNotice;
import com.voxlearning.utopia.service.vendor.impl.push.NewHomeworkVendorMessageAbstractProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016-9-12
 */
@Named
public class NewHomeworkVendorMessageProcessor_OfflineHomework extends NewHomeworkVendorMessageAbstractProcessor {

    public NewHomeworkVendorMessageProcessor_OfflineHomework() {
        this.messageType = HomeworkVendorMessageType.OFFLINE_HOMEWORK;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        newHomeworkVendorMessageProcessorManager.register(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doProcess(Map<String, Object> messageMap) {
        HomeworkVendorMessageType type = HomeworkVendorMessageType.ofWithUnKnow(SafeConverter.toInt(messageMap.get("messageType")));
        if (type != this.messageType) {
            return;
        }
        Map<String, Object> extInfo = (Map) (messageMap.get("extInfo"));
        Map<String, String> offlineHomeworkMap = (Map) extInfo.get("groupOfflineHomeworkIdMap");
        if (MapUtils.isEmpty(offlineHomeworkMap)) {
            return;
        }
        //group-offlineHomeworkId对应关系
        Map<Long, String> offlineHomeworkIdMap = new HashMap<>();
        for (String groupIdStr : offlineHomeworkMap.keySet()) {
            offlineHomeworkIdMap.put(SafeConverter.toLong(groupIdStr), offlineHomeworkMap.get(groupIdStr));
        }
        //hadTeacherRemindMap已经发送提醒的group.全部默认false
        Map<Long, Boolean> hadTeacherRemindMap = new HashMap<>();
        offlineHomeworkIdMap.keySet().forEach(p -> hadTeacherRemindMap.put(p, Boolean.FALSE));

        JxtNotice jxtNotice = new JxtNotice();
        jxtNotice.setTeacherId(SafeConverter.toLong(extInfo.get("teacherId")));
        jxtNotice.setGroupIds(offlineHomeworkIdMap.keySet());
        jxtNotice.setGroupOfflineHomeworkIdMap(offlineHomeworkIdMap);
        jxtNotice.setNoticeType(JxtNoticeType.OFFLINE_HOMEWORK.getType());
        jxtNotice.setContent(SafeConverter.toString(extInfo.get("content")));
        jxtNotice.setExpireTime(SafeConverter.toDate(extInfo.get("endTime")));
        jxtNotice.setAutoRemind(Boolean.FALSE);
        jxtNotice.setHadTeacherRemindMap(hadTeacherRemindMap);
        jxtNotice.setNeedFeedBack(SafeConverter.toBoolean(extInfo.get("needFeedBack")));
        jxtServiceClient.saveJxtNotice(jxtNotice);
    }
}
