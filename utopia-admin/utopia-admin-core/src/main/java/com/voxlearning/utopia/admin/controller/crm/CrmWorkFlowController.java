package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmWechatMessageServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowContext;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016/11/14
 * @deprecated Move to WechatWorkflowController
 */
@Controller
@RequestMapping("/crm/workflow")
@Deprecated
public class CrmWorkFlowController extends CrmAbstractController {

    @Inject private WorkFlowServiceClient workFlowServiceClient;
    @Inject private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject private CrmWechatMessageServiceClient crmWechatMessageServiceClient;

    @RequestMapping(value = "createwechatmsg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createWechatMsg(Model model) {
        int wt = getRequestInt("wechatType", -1);
        String userId = getRequestString("userId").replaceAll("\r", "");
        WechatType wechatType = WechatType.of(wt);
        String firstInfo = getRequestString("first");
        String keyword1 = getRequestString("k1");
        String keyword2 = getRequestString("k2");
        String remark = getRequestString("remark");
        String url = getRequestString("url");
        String sendTime = getRequestString("sendTime");
        String isUstalk = getRequestString("isUstalk");
        List<String> userIdList = Arrays.stream(userId.split("\n"))
                .map(SafeConverter::toLong)
                .filter(id -> id > 0L)
                .collect(Collectors.toSet()).stream().map(SafeConverter::toString).collect(Collectors.toList());
        StringBuilder errMsg = new StringBuilder();
        if (wechatType == null || (wechatType != WechatType.PARENT && wechatType != WechatType.TEACHER)) {
            errMsg.append("<br>请选择正确的消息发送端");
        }
        if (StringUtils.isBlank(keyword1) && StringUtils.isBlank(keyword2)) {
            errMsg.append("<br>Keyword请至少填写一项");
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            errMsg.append("<br>请填写正确的【用户ID】");
        }
        if (errMsg.length() > 0) {
            model.addAttribute("error", errMsg.toString());
            return MapMessage.errorMessage(errMsg.toString());
        }

        WechatNoticeProcessorType noticeType;
        if (wechatType == WechatType.PARENT) {
            noticeType = WechatNoticeProcessorType.ParentOperationalNotice;
        } else if (wechatType == WechatType.TEACHER) {
            noticeType = WechatNoticeProcessorType.TeacherOperationNotice;
        } else {
            model.addAttribute("error", "请选择正确的消息发送端");
            return MapMessage.errorMessage("请选择正确的消息发送端！");
        }

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();

        WorkFlowRecord workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setStatus("init");
        workFlowRecord.setSourceApp("admin");
        workFlowRecord.setTaskName("crm批量发送微信");
        StringBuilder content = new StringBuilder();
        content.append("发送微信/用户").append(userIdList.toString()).append("/内容:");
        if (StringUtils.isNotBlank(keyword1)) {
            content.append(keyword1).append(";");
        }
        if (StringUtils.isNotBlank(keyword2)) {
            content.append(keyword2).append(";");
        }
        if (StringUtils.isNotBlank(remark)) {
            content.append(remark).append(";");
        }
        workFlowRecord.setTaskContent(content.toString());
        workFlowRecord.setTaskDetailUrl(url);
        workFlowRecord.setLatestProcessorName(adminUser.getRealName());
        workFlowRecord.setCreatorName(adminUser.getRealName());
        workFlowRecord.setCreatorAccount(adminUser.getAdminUserName());
        MapMessage mapMessage = workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord);

        if (mapMessage.isSuccess()) {
            workFlowRecord = (WorkFlowRecord) mapMessage.get("workFlowRecord");

            WechatWfMessage wechatWfMessage = new WechatWfMessage();
            wechatWfMessage.setStatus("lv1");
            wechatWfMessage.setFirstInfo(firstInfo);
            wechatWfMessage.setIsUstalk(Objects.equals(isUstalk, "ustalk"));
            wechatWfMessage.setKeyword1(keyword1);
            wechatWfMessage.setKeyword2(keyword2);
            wechatWfMessage.setNoticeType(noticeType.name());
            wechatWfMessage.setRecordId(workFlowRecord.getId());
            wechatWfMessage.setUrl(url);
            wechatWfMessage.setUserIds(JsonUtils.toJson(userIdList));
            wechatWfMessage.setWechatType(wechatType.name());
            wechatWfMessage.setSendTime(DateUtils.stringToDate(sendTime));
            wechatWfMessage.setRemark(remark);
            crmWechatMessageServiceClient.persist(wechatWfMessage);

            WorkFlowContext workFlowContext = new WorkFlowContext();
            workFlowContext.setWorkFlowRecord(workFlowRecord);
            workFlowContext.setWorkFlowName("admin_wechat_batch_send");
            workFlowContext.setSourceApp("admin");
            workFlowContext.setProcessNotes("自动发送消息");
            workFlowContext.setProcessorAccount(adminUser.getAdminUserName());
            workFlowContext.setProcessorName("admin:" + adminUser.getRealName());
            mapMessage = workFlowServiceClient.agree(workFlowContext);
            if (!mapMessage.isSuccess()) {
                errMsg.append("<br>mapMessage.getInfo()");
                return MapMessage.errorMessage(errMsg.toString());
            }
        } else {
            errMsg.append("<br>消息发送失败");
            model.addAttribute("error", errMsg.toString());
            return MapMessage.errorMessage(errMsg.toString());
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "checkwechatmsg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkWechatMsg() {
        //workFlowRecordId  wechatWfMessageId 处理类型 几审
        Long workFlowRecordId = getRequestLong("workFlowRecordId", 0L);
        String operationType = getRequestString("operationType");
        if (workFlowRecordId == 0L || StringUtils.isBlank(operationType)) {
            return MapMessage.errorMessage("参数错误");
        }
        List<WorkFlowRecord> workFlowRecordList = workFlowLoaderClient.loadWorkFlowRecords(Collections.singleton(workFlowRecordId)).values().stream().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(workFlowRecordList)) {
            return MapMessage.errorMessage("WorkFlowRecord:" + workFlowRecordId + "不存在");
        }

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        WorkFlowRecord workFlowRecord = workFlowRecordList.get(0);
        WorkFlowContext workFlowContext = new WorkFlowContext();
        workFlowContext.setWorkFlowRecord(workFlowRecord);
        workFlowContext.setWorkFlowName("admin_wechat_batch_send");
        workFlowContext.setSourceApp("admin");
        workFlowContext.setProcessNotes("自动发送消息");
        workFlowContext.setProcessorAccount(adminUser.getAdminUserName());
        workFlowContext.setProcessorName("admin:" + adminUser.getRealName());

        MapMessage mapMessage = null;
        if (Objects.equals(operationType, "send")) {//直接发送
            mapMessage = workFlowServiceClient.agree(workFlowContext);
        } else if (Objects.equals(operationType, "reject")) {//拒绝
            mapMessage = workFlowServiceClient.reject(workFlowContext);
        } else if (Objects.equals(operationType, "raiseup")) {//转给上一级
            mapMessage = workFlowServiceClient.raiseup(workFlowContext);
        } else {
            return MapMessage.errorMessage("操作类型" + operationType + "不存在");
        }

        return mapMessage;
    }

}
