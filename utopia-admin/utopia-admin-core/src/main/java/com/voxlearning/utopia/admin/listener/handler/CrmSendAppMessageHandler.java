package com.voxlearning.utopia.admin.listener.handler;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.api.tag.UserTagService;
import com.voxlearning.utopia.service.crm.api.constants.crm.AppPushMsgConstants;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.consumer.loader.crm.CrmAppPushLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmAppPushServiceClient;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class CrmSendAppMessageHandler extends SpringContainerSupport {

    private static final String APP_KEY = "17Agent";
    private static final String PLATFORM_SECRET_KEY_TEST = "YtfaPhAO0#95";    // 调用平台接口使用的 secretKey
    private static final String PLATFORM_SECRET_KEY = "LtaO3Y9oENjw";    // 调用平台接口使用的 secretKey

    @Inject private CrmAppPushLoaderClient crmAppPushLoaderClient;
    @Inject private CrmAppPushServiceClient crmAppPushServiceClient;
    @Inject private WorkFlowServiceClient workFlowServiceClient;
    @ImportService(interfaceClass = UserTagService.class)
    private UserTagService userTagService;

    public void handle(String mqmsg, Long recordId, String status) {
        if (StringUtils.isBlank(mqmsg) || recordId == null || StringUtils.isBlank(status)) {
            logger.warn("WechatQueueHandler not handle error: mqmsg is {},recordId is {}", mqmsg, recordId);
            return;
        }

        // 更新wechatWfMessage中的状态
        if (Objects.equals(mqmsg, "agree_init")) {
            crmAppPushServiceClient.updateAppPushWfMessageStatus(recordId, status);
        } else if (mqmsg.startsWith("agree_lv")) {
            sendAppPushMessage(recordId);
            crmAppPushServiceClient.updateAppPushWfMessageStatus(recordId, status);
        } else if (mqmsg.startsWith("reject_lv")) {
            crmAppPushServiceClient.updateAppPushWfMessageStatus(recordId, status);
        } else if (mqmsg.startsWith("raiseup_lv")) {
            crmAppPushServiceClient.updateAppPushWfMessageStatus(recordId, status);
        } else {
            logger.warn("WechatQueueHandler not handle error: mqmsg is {},recordId is {}", mqmsg, recordId);
        }
    }

    private MapMessage sendAppPushMessage(Long recordId) {
        AppPushWfMessage appPushMsg = crmAppPushLoaderClient.findByRecord(recordId);
        if (appPushMsg == null) {
            logger.error("批量发送AppPush消息失败 admin_send_app_push:{} because WechatWfMessage is null", recordId);
            return MapMessage.errorMessage("批量发送AppPush消息失败 admin_send_app_push:{} because WechatWfMessage is null", recordId);
        }
        // 如果已经处理过就直接越过吧
        if ("success".equals(appPushMsg.getSendStatus()) && "processed".equals(appPushMsg.getStatus())) {
            return MapMessage.successMessage();
        }
        MapMessage result = MapMessage.successMessage();
        if(appPushMsg.getPushType() == AppPushMsgConstants.TargetTagGroup){
            List<List<Map<String, Object>>> tagGroups = appPushMsg.getTargetTagGroups();
            if(CollectionUtils.isEmpty(tagGroups)){
                return MapMessage.errorMessage("请选择标签组");
            }
            List<List<String>> tagList = new ArrayList<>();
            tagGroups.forEach(p -> {
                List<String> itemList = new ArrayList<>();
                for(Map<String, Object> tag : p){
                    String tagId = SafeConverter.toString(tag.get("tagId"));
                    if(StringUtils.isNotBlank(tagId)){
                        itemList.add(tagId);
                    }
                }
                if(CollectionUtils.isNotEmpty(itemList)){
                    tagList.add(itemList);
                }
            });
            String secretKey;
            if(RuntimeMode.lt(Mode.STAGING)) {
                secretKey = PLATFORM_SECRET_KEY_TEST;
            }else {
                secretKey = PLATFORM_SECRET_KEY;
            }
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("messageId", appPushMsg.getId());
            result = userTagService.tagCombinationCalc(tagList, APP_KEY, appPushMsg.getId(), generateAppKeySig(paramMap, secretKey));
        }else {
            result = crmAppPushServiceClient.publish(appPushMsg);
        }
        // 记录一下处理的历史
        WorkFlowProcessHistory history = new WorkFlowProcessHistory(
                recordId, "admin", "history", "history", WorkFlowProcessResult.agree, JsonUtils.toJson(result), WorkFlowType.ADMIN_SEND_APP_PUSH
        );
        workFlowServiceClient.insertWorkFlowHistory(history);
        // 更新记录的状态
        crmAppPushServiceClient.updateSendStatus(appPushMsg.getId());
        return result;
    }

    private String generateAppKeySig(Map<String, String> paramMap, String secretKey){
        if(paramMap == null){
            paramMap = new HashMap<>();
        }
        if(!paramMap.containsKey("app_key")){
            paramMap.put("app_key", APP_KEY);
        }
        return DigestSignUtils.signMd5(paramMap, secretKey);
    }

}
