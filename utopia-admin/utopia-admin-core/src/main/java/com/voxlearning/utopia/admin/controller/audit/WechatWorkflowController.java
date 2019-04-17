package com.voxlearning.utopia.admin.controller.audit;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;
import com.voxlearning.utopia.service.crm.consumer.loader.crm.CrmWechatMessageLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmWechatMessageServiceClient;
import com.voxlearning.utopia.service.crm.tools.WechatWorkFlowUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowContext;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/audit/wechat")
public class WechatWorkflowController extends CrmAbstractController {

    @Inject private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject private WorkFlowServiceClient workFlowServiceClient;
    @Inject private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject private CrmWechatMessageServiceClient crmWechatMessageServiceClient;
    @Inject private CrmWechatMessageLoaderClient crmWechatMessageLoaderClient;

    @RequestMapping(value = "wechatapply.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String wechatApply(Model model) {
        Long recordId = getRequestLong("id");
        WechatWfMessage wechatWfMessage = crmWechatMessageLoaderClient.loadByRecordId(recordId);
        if (wechatWfMessage == null) {
            return "audit/wechat/wechatedit";
        }
        model.addAttribute("wechatMsg", wechatWfMessage);
        int lineCnt = 0;
        if (wechatWfMessage.getSendType() == 2) {
            try {
                lineCnt = WorkbookFactory.create(new URL(wechatWfMessage.getFileUrl()).openStream()).getSheetAt(0).getLastRowNum();
            } catch (Exception ignored) {
            }
        }

        // 是否可以审核
        boolean audit = (StringUtils.equals("lv1", wechatWfMessage.getStatus())
                || StringUtils.equals("lv2", wechatWfMessage.getStatus()));
        audit &= Objects.isNull(getRequestParameter("ct", null));
        model.addAttribute("audit", audit);
        model.addAttribute("workflowRecord", workFlowLoaderClient.loadWorkFlowRecord(recordId));

        List<WorkFlowProcessHistory> histories = workFlowLoaderClient.loadWorkFlowProcessHistoryByWorkFlowId(recordId)
                .stream()
                .sorted(Comparator.comparing(WorkFlowProcessHistory::getUpdateDatetime).reversed())
                .collect(Collectors.toList());
        model.addAttribute("histories", histories);

        model.addAttribute("lineCnt", lineCnt);
        return "audit/wechat/wechatview";
    }

    @RequestMapping(value = "createwechatmsg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createWechatMsg(Model model) {
        try {
            // 赋值
            WechatWfMessage wechatMessage = requestWechatMessage();

            MapMessage validMessage = WechatWorkFlowUtils.validateMessage(wechatMessage);
            if (!validMessage.isSuccess()) {
                return validMessage;
            }

            // 初始化工作流
            AuthCurrentAdminUser adminUser = getCurrentAdminUser();

            WorkFlowRecord workFlowRecord = new WorkFlowRecord();
            workFlowRecord.setStatus("init");
            workFlowRecord.setSourceApp("admin");
            workFlowRecord.setTaskName("crm批量发送微信");
            workFlowRecord.setTaskContent(WechatWorkFlowUtils.generateContent(wechatMessage));
            workFlowRecord.setTaskDetailUrl(wechatMessage.getUrl());
            workFlowRecord.setLatestProcessorName(adminUser.getRealName());
            workFlowRecord.setCreatorName(adminUser.getRealName());
            workFlowRecord.setCreatorAccount(adminUser.getAdminUserName());
            workFlowRecord.setWorkFlowType(WorkFlowType.ADMIN_WECHAT_NOTICE);
            MapMessage mapMessage = workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }

            workFlowRecord = (WorkFlowRecord) mapMessage.get("workFlowRecord");
            wechatMessage.setRecordId(workFlowRecord.getId());
            wechatMessage.setStatus(workFlowRecord.getStatus());
            crmWechatMessageServiceClient.persist(wechatMessage);

            WorkFlowContext workFlowContext = new WorkFlowContext();
            workFlowContext.setWorkFlowRecord(workFlowRecord);
            workFlowContext.setWorkFlowName(WorkFlowType.ADMIN_WECHAT_NOTICE.getWorkflowName());
            workFlowContext.setSourceApp("admin");
            workFlowContext.setProcessNotes("发送微信模板消息");
            workFlowContext.setProcessorAccount(adminUser.getAdminUserName());
            workFlowContext.setProcessorName(adminUser.getAdminUserName());
            mapMessage = workFlowServiceClient.agree(workFlowContext);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed create wechat message workflow", ex);
            return MapMessage.errorMessage("提交申请失败");
        }
    }

    @RequestMapping(value = "uploadexcel.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadSource(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("请选择上传的文件！");
        }
        int rowIndex = 0;
        StringBuilder errorMsg = new StringBuilder();
        Set<String> keySet = new HashSet<>();
        try {
            String suffix = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            if (!"xls".equals(suffix) && !"xlsx".equals(suffix)) {
                return MapMessage.errorMessage("无效的文件");
            }

            // 再简单校验一下内容
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getLastRowNum() > 50000) {
                return MapMessage.errorMessage("单个文件请控制在5W行以内");
            }

            while (true) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    break;
                }
                Long uid = getCellValueLong(row.getCell(0));
                Date st = row.getCell(1) == null ? null : row.getCell(1).getDateCellValue();
                if (uid == null && st == null) break;

                if (uid == null || uid == 0L) {
                    errorMsg.append("第").append(rowIndex + 1).append("行，无效的用户ID： ").append(uid).append("\n");
                }

                if (st == null) {
                    errorMsg.append("第").append(rowIndex + 1).append("行，无效的发送时间").append("\n");
                }

                if (uid != null && uid != 0L && st != null) {
                    String key = StringUtils.join(uid, "_", st.getTime());
                    if (keySet.contains(key)) {
                        errorMsg.append("第").append(rowIndex + 1).append("行，重复的用户ID和发送时间").append("\n");
                    } else {
                        keySet.add(key);
                    }
                }
                rowIndex++;
            }

            if (keySet.isEmpty()) {
                return MapMessage.errorMessage("请认真填写Excel里的内容");
            }

            if (errorMsg.length() > 0) {
                return MapMessage.errorMessage(errorMsg.toString());
            }

            String fileName = AdminOssManageUtils.upload(file, "wechat");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("文件保存失败！");
            }
            return MapMessage.successMessage().add("fileUrl", fileName);
        } catch (Exception ex) {
            logger.error("Failed to excel of wechat user info @Row={} ", rowIndex, ex);
            return MapMessage.errorMessage("上传文件失败：" + ex.getMessage());
        }

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
        List<WorkFlowRecord> workFlowRecordList = new ArrayList<>(
                workFlowLoaderClient.loadWorkFlowRecords(Collections.singleton(workFlowRecordId)).values()
        );

        if (CollectionUtils.isEmpty(workFlowRecordList)) {
            return MapMessage.errorMessage("WorkFlowRecord:" + workFlowRecordId + "不存在");
        }

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        WorkFlowRecord workFlowRecord = workFlowRecordList.get(0);
        WorkFlowContext workFlowContext = new WorkFlowContext();
        workFlowContext.setWorkFlowRecord(workFlowRecord);
        workFlowContext.setWorkFlowName(WorkFlowType.ADMIN_WECHAT_NOTICE.getWorkflowName());
        workFlowContext.setSourceApp("admin");
        workFlowContext.setProcessNotes("自动发送消息");
        workFlowContext.setProcessorAccount(adminUser.getAdminUserName());
        workFlowContext.setProcessorName(adminUser.getAdminUserName());

        MapMessage mapMessage = null;
        if (Objects.equals(operationType, "send")) {//直接发送
            workFlowContext.setProcessNotes("同意发送微信模板消息");
            mapMessage = workFlowServiceClient.agree(workFlowContext);
        } else if (Objects.equals(operationType, "reject")) {//拒绝
            workFlowContext.setProcessNotes("驳回发送微信模板消息申请");
            mapMessage = workFlowServiceClient.reject(workFlowContext);
            workFlowContext.setProcessNotes("同意发送微信模板消息");
        } else if (Objects.equals(operationType, "raiseup")) {//转给上一级
            workFlowContext.setProcessNotes("发送微信模板消息转上级审核");
            mapMessage = workFlowServiceClient.raiseup(workFlowContext);
        } else {
            return MapMessage.errorMessage("操作类型" + operationType + "不存在");
        }

        return mapMessage;
    }

    private WechatWfMessage requestWechatMessage() {
        // 赋值
        WechatWfMessage wechatMessage = new WechatWfMessage();
        requestFillEntity(wechatMessage);

        // 发送端
        int wt = getRequestInt("wechatType");
        WechatType wechatType = WechatType.of(wt);
        if (wechatType == null) {
            return null;
        }
        wechatMessage.setWechatType(wechatType.name());

        // 用户ID
        UserType userType = wechatMessage.fetchUserType();
        String users = getRequestString("userList");
        List<Long> userIds = Arrays.stream(users.split("\n"))
                .map(t -> t.replaceAll("\\s", ""))
                .filter(p -> p.startsWith(String.valueOf(userType.getType())))
                .filter(StringUtils::isNotBlank)
                .map(SafeConverter::toLong)
                .filter(t -> t > 0L)
                .distinct()
                .collect(Collectors.toList());
        wechatMessage.setUserIds(JsonUtils.toJson(userIds));

        // 其他参数
        if (wechatType == WechatType.PARENT) {
            wechatMessage.setNoticeType(WechatNoticeProcessorType.ParentOperationalNotice.name());
        } else if (wechatType == WechatType.TEACHER) {
            wechatMessage.setNoticeType(WechatNoticeProcessorType.TeacherOperationNotice.name());
        }

        wechatMessage.setDisabled(false);
        return wechatMessage;
    }

    private Long getCellValueLong(Cell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        }
        try {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return new Double(cell.getNumericCellValue()).longValue();
            }
            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                return ConversionUtils.toLong(cell.getStringCellValue().trim());
            }
        } catch (Exception ignored) {
            return 0L;
        }
        return null;
    }

}
