package com.voxlearning.utopia.admin.listener.handler;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;
import com.voxlearning.utopia.service.crm.consumer.loader.crm.CrmWechatMessageLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmWechatMessageServiceClient;
import com.voxlearning.utopia.service.crm.tools.WechatWorkFlowUtils;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;
import org.apache.poi.ss.usermodel.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016/11/16
 */
@Named
public class WechatQueueHandler extends SpringContainerSupport {

    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private CrmWechatMessageLoaderClient crmWechatMessageLoaderClient;
    @Inject private CrmWechatMessageServiceClient crmWechatMessageServiceClient;
    @Inject private WorkFlowServiceClient workFlowServiceClient;


    public void handle(String mqmsg, Long recordId, String status) {
        if (StringUtils.isBlank(mqmsg) || recordId == null || StringUtils.isBlank(status)) {
            logger.warn("WechatQueueHandler not handle error: mqmsg is {},recordId is {}", mqmsg, recordId);
            return;
        }

        if (Objects.equals(mqmsg, "agree_init")) {//更新wechatWfMessage中的状态
            updateWechatWfMessageStatusByRecordId(recordId, status);
        } else if (mqmsg.startsWith("agree_lv")) {
            MapMessage result = sendWechatNotice(recordId);
            // 记录一下处理的历史
            WorkFlowProcessHistory history = new WorkFlowProcessHistory(
                    recordId, "admin", "history", "history", WorkFlowProcessResult.agree, JsonUtils.toJson(result), WorkFlowType.ADMIN_WECHAT_NOTICE
            );
            workFlowServiceClient.insertWorkFlowHistory(history);
            // 更新记录的状态
            updateWechatWfMessageStatusByRecordId(recordId, status);
        } else if (mqmsg.startsWith("reject_lv")) {
            updateWechatWfMessageStatusByRecordId(recordId, status);
        } else if (mqmsg.startsWith("raiseup_lv")) {
            updateWechatWfMessageStatusByRecordId(recordId, status);
        } else {
            logger.warn("WechatQueueHandler not handle error: mqmsg is {},recordId is {}", mqmsg, recordId);
        }

    }

    private MapMessage sendWechatNotice(Long recordId) {
        WechatWfMessage wechatMessage = crmWechatMessageLoaderClient.loadByRecordId(recordId);
        if (wechatMessage == null) {
            logger.warn("微信批量发送消息失败 admin_wechat_batch_send:{} because WechatWfMessage is null", recordId);
            return MapMessage.errorMessage();
        }

        MapMessage validMessage = WechatWorkFlowUtils.validateMessage(wechatMessage);
        if (!validMessage.isSuccess()) {
            return validMessage;
        }

        List<Map<String, Object>> usersToSend = new ArrayList<>();
        int sendType = wechatMessage.getSendType();
        if (sendType == 1) {
            Date sendTime = wechatMessage.getSendTime() == null ? new Date() : wechatMessage.getSendTime();
            usersToSend = JsonUtils.fromJsonToList(wechatMessage.getUserIds(), Long.class)
                    .stream()
                    .filter(id -> id > 0L)
                    .distinct()
                    .map(id -> MapUtils.m("uid", id, "sendTime", sendTime))
                    .collect(Collectors.toList());

        } else if (sendType == 2) {
            if (StringUtils.isBlank(wechatMessage.getFileUrl())) {
                return MapMessage.errorMessage("无效的文件链接");
            }
            try {
                // Read From Excel
                URL fileUrl = new URL(wechatMessage.getFileUrl());
                InputStream in = fileUrl.openStream();
                Workbook workbook = WorkbookFactory.create(in);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 0;
                while (true) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        break;
                    }

                    if (row.getCell(0) == null || row.getCell(1) == null) {
                        break;
                    }

                    Long uid = getCellValueLong(row.getCell(0));
                    Date st = row.getCell(1).getDateCellValue();
                    if (uid == null || st == null) break;
                    usersToSend.add(MapUtils.m("uid", uid, "sendTime", st));
                    rowIndex++;
                }
            } catch (Exception ex) {
                logger.error("Failed to Read Wechat User Excel Info, fileUrl={}", wechatMessage.getFileUrl(), ex);
                return MapMessage.errorMessage("解析Excel附件失败：" + ex.getMessage());
            }
        }

        String batchNo = RandomUtils.randomString(10);
        WechatType wechatType = WechatType.valueOf(wechatMessage.getWechatType());
        WechatNoticeProcessorType noticeType = WechatNoticeProcessorType.valueOf(wechatMessage.getNoticeType());
        // 业务处理逻辑
        Map<String, Object> extensionInfo = MapUtils.m(
                "first", wechatMessage.getFirstInfo(),
                "keyword1", wechatMessage.getKeyword1(),
                "keyword2", wechatMessage.getKeyword2(),
                "remark", wechatMessage.getRemark(),
                "url", wechatMessage.getUrl(),
                "isWkt", wechatMessage.getIsWkt()
        );

        batchSendPromotWechatMessage(
                usersToSend, extensionInfo, wechatMessage.getUrl(), noticeType, wechatType, wechatMessage.isUtk(), batchNo
        );
        return MapMessage.successMessage();
    }

    private Long getCellValueLong(Cell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        }
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue()).longValue();
        }
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return ConversionUtils.toLong(cell.getStringCellValue().trim());
        }
        return null;
    }

    private void updateWechatWfMessageStatusByRecordId(Long recordId, String status) {
        crmWechatMessageServiceClient.updateMessageStatusByRecord(recordId, status);
    }

    /**
     * Copy From SiteWechatMessageController#batchSendPromotWechatMessage()
     */
    private void batchSendPromotWechatMessage(List<Map<String, Object>> usersToSend,
                                              Map<String, Object> extensionInfo,
                                              String url,
                                              WechatNoticeProcessorType noticeType,
                                              WechatType wechatType,
                                              boolean isUsTalk,
                                              String batchNo) {
        if (CollectionUtils.isEmpty(usersToSend)) {
            return;
        }
        AlpsThreadPool.getInstance().submit(() -> {
            for (Map<String, Object> userToSend : usersToSend) {
                Long uid = SafeConverter.toLong(userToSend.get("uid"));
                // 处理URL中的userId参数
                if (StringUtils.isNoneBlank(url) && url.contains("#userId#")) {
                    extensionInfo.put("url", url.replace("#userId#", String.valueOf(uid)));
                }
                extensionInfo.put("sendTime", userToSend.get("sendTime"));
                if (isUsTalk) {
                    // ustalk为了提高用户微信消息的打开率，需要在文案上加上孩子的名字，并给某些字段加色，着重显示
                    List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(uid);
                    long studentId = 0;
                    String studentName = "";
                    if (studentParentRefs.size() > 0) {
                        // 有孩子，注入到extensionInfo中去
                        studentId = studentParentRefs.get(0).getStudentId();
                        User student = userLoaderClient.loadUser(studentId, UserType.STUDENT);
                        studentName = student == null ? "" : student.fetchRealname();

                    }
                    extensionInfo.put("studentName", studentName);
                    extensionInfo.put("studentId", studentId);
                    extensionInfo.put("type", "ustalkpromot");

                    // 文言中替换studentName
                    extensionInfo.put("first", SafeConverter.toString(extensionInfo.get("first")).replace("#studentName#", studentName));
                    extensionInfo.put("keyword1", SafeConverter.toString(extensionInfo.get("keyword1")).replace("#studentName#", studentName));
                    extensionInfo.put("keyword2", SafeConverter.toString(extensionInfo.get("keyword2")).replace("#studentName#", studentName));
                    extensionInfo.put("remark", SafeConverter.toString(extensionInfo.get("remark")).replace("#studentName#", studentName));
                }
                wechatServiceClient.processWechatNotice(noticeType, uid, extensionInfo, wechatType);
            }
        });
    }
}
