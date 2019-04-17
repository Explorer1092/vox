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

package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Range;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.data.AdminSmsAuditPrivilege;
import com.voxlearning.utopia.admin.persist.entity.AdminLog;
import com.voxlearning.utopia.admin.queue.AdminCommandQueueProducer;
import com.voxlearning.utopia.admin.util.XssfUtils;
import com.voxlearning.utopia.api.constant.SmsTaskStatus;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.entity.misc.SmsTask;
import com.voxlearning.utopia.entity.misc.SmsTaskReceiverRef;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.client.SmsTaskServiceClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping(value = "/opmanager/smstask")
public class SmsTaskController extends OpManagerAbstractController {

    private static final String TRACE_SMS_FLOW_CONDITION = "WHERE `OPERATION`='SMS_TRACE' " +
            "AND `TARGET_ID`=? " +
            "ORDER BY `CREATE_DATETIME` DESC";

    private static final String MAIL_SUFFIX = "@17zuoye.com;";

    private static final String ADMIN_CONFIG_KEY_NAME_LV1 = "ADMIN_SUPERVISOR_LV1";
    private static final String ADMIN_CONFIG_KEY_NAME_LV2 = "ADMIN_SUPERVISOR_LV2";
    private static final String ADMIN_CONFIG_KEY_NAME_LV3 = "ADMIN_SUPERVISOR_LV3";
    private static final String SMS_TASK_MAIL_CC = "SMS_TASK_MAIL_CC";

    private static final List<SmsType> validSmsTypes = Arrays.asList(
            SmsType.CRM_PLATFORM_GENERAL, SmsType.CRM_PLATFORM_AXJ, SmsType.CRM_PLATFORM_ZTKJ,
            SmsType.CRM_LIVECAST_GENERAL, SmsType.CRM_LIVECAST_AXJ, SmsType.CRM_LIVECAST_ZTKJ
    );

    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private SmsTaskServiceClient smsTaskServiceClient;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    @Inject
    AdminCommandQueueProducer adminCommandQueueProducer;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String smsTaskList(Model model) {
        int privilege = getUserPrivilegeLevel();
        String creator = getRequestParameter("creator", null);
        int status = getRequestInt("status");
        String source = getRequestString("source");

        List<SmsTask> allSmsTask = smsTaskServiceClient.getSmsTaskService().loadAllSmsTasksIncludeDisabled()
                .getUninterruptibly();

        if (StringUtils.equals(source, "search")) {
            List<SmsTask> allEnabled = allSmsTask.stream()
                    .filter(e -> !e.isDisabledTrue())
                    .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                    .collect(Collectors.toList());
            List<SmsTask> userSmsTaskList;
            if (StringUtils.isBlank(creator)) {
                userSmsTaskList = new ArrayList<>(allEnabled);
            } else {
                // 根据创建人全部 load 出来
                userSmsTaskList = allEnabled.stream()
                        .filter(e -> StringUtils.equals(creator, e.getCreator()))
                        .collect(Collectors.toList());
            }
            // 再根据状态进行过滤
            userSmsTaskList = userSmsTaskList.stream()
                    .filter(t -> status == 99 || (t.getStatus() >= status && t.getStatus() <= status + 10))
                    .collect(Collectors.toList());
            // 查询条件进行过滤
            model.addAttribute("smsList", generateSmsTaskMap(userSmsTaskList));
        } else {
            model.addAttribute("smsList", generateSmsTaskMap(loadSmsTasks(allSmsTask, privilege)));
        }

        Set<String> creatorList = allSmsTask.stream()
                .filter(e -> !e.isDisabledTrue())
                .map(e -> StringUtils.defaultString(e.getCreator()))
                .collect(Collectors.toCollection(TreeSet::new));

        model.addAttribute("creatorList", creatorList);
        model.addAttribute("level", privilege);
        model.addAttribute("creator", creator);
        model.addAttribute("status", status);
        model.addAttribute("error", getAlertMessageManager().getMessagesError("SMS_DETAIL"));
        model.addAttribute("validSmsTypes", validSmsTypes);
        return "opmanager/smstask/index";
    }


    @RequestMapping(value = "smsdetail.vpage", method = RequestMethod.GET)
    public String editSms(Model model) {
        Long smsId = getRequestLong("smsId");
        String mode = getRequestString("mode");
        SmsTask smsTask = smsTaskServiceClient.getSmsTaskService()
                .loadSmsTaskFromDB(smsId)
                .getUninterruptibly();
        if (StringUtils.equals("view", mode)) {
            if (smsTask == null) {
                getAlertMessageManager().addMessageError("无效的任务ID：" + smsId, "SMS_DETAIL");
                return "redirect: /opmanager/smstask/index.vpage";
            }
            model.addAttribute("smsTask", generateSingleSmsTask(smsTask, true));
            if (smsTask.getStatus() == 31 || smsTask.getStatus() == 32) {
                List<SmsTaskReceiverRef> userRefs = smsServiceClient.getSmsService()
                        .loadBySmsTaskId(smsId, false)
                        .getUninterruptibly();
                // 成功列表
                List<SmsTaskReceiverRef> successList = userRefs.stream()
                        .filter(t -> t.getStatus() == 1).collect(Collectors.toList());
                model.addAttribute("successList", successList);

                // 失败列表
                List<SmsTaskReceiverRef> failedList = userRefs.stream()
                        .filter(t -> t.getStatus() == 9).collect(Collectors.toList());
                model.addAttribute("failedList", failedList);
            }
        }
        model.addAttribute("mode", mode);
        model.addAttribute("validSmsTypes", validSmsTypes);
        return "opmanager/smstask/detail";
    }

    @RequestMapping(value = "savesms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSmsTask() {
        try {
            String source = getRequestString("source");
            Long smsId = getRequestLong("smsId");
            String smsText = getRequestString("smsText");
            Date sendTime = DateUtils.stringToDate(getRequestString("sendtime"), DateUtils.FORMAT_SQL_DATETIME);
            Integer userType = getRequestInt("userType");
            //String userTokens = getRequestString("target");
            String purpose = getRequestString("purpose");
            String smsType = getRequestString("smsType");
            XSSFWorkbook targetFile = readRequestWorkbook("targetFile");
            if (null == targetFile) {
                return MapMessage.errorMessage("请上传接收用户Excel文件");
            }
            List<String> targetsFromExcel = getTargetsFromExcel(targetFile);

            // 保存短信内容
            SmsTask smsTask = SmsTask.newInstance();
            smsTask.setId(smsId);
            smsTask.setCreator(getCurrentAdminUser().getAdminUserName());
            smsTask.setCreatorName(getCurrentAdminUser().getRealName());
            smsTask.setSmsText(smsText);
            smsTask.setSmsSendtime(sendTime);
            smsTask.setReceiveUserType(userType);
            smsTask.setSmsPurpose(purpose);
            smsTask.setSmsType(smsType);

            // validate Parameters
            MapMessage validMsg = validateSmsTask(smsTask, targetsFromExcel, source);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }

            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            return builder.keyPrefix("CRM:SAVE_SMS")
                    .keys(smsId)
                    .callback(() -> saveSmsDetails(smsTask, targetsFromExcel, source))
                    .build()
                    .execute();

        } catch (DuplicatedOperationException dup) {
            return MapMessage.errorMessage("请勿重复提交");
        } catch (Exception ex) {
            logger.error("Save sms task failed: {}", ex);
            return MapMessage.errorMessage("保存短信失败: {}", ex);
        }
    }

    @RequestMapping(value = "download_template.vpage", method = RequestMethod.GET)
    public void exportDataReport(HttpServletResponse response) {
        try {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            Sheet sheet = workbook.createSheet("模板");
            sheet.createFreezePane(0, 1, 0, 1);
            sheet.setColumnWidth(0,20*256);
            Font font = workbook.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) 10);
            CellStyle firstRowStyle = workbook.createCellStyle();
            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            firstRowStyle.setFont(font);
            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
            Row firstRow = sheet.createRow(0);
            setCellValue(firstRow, 0, firstRowStyle, "用户ID或手机号");
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            String fileName = "数据模板" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        } catch (Exception ex) {
        }
    }

    @RequestMapping(value = "deletesms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteSmsTask(@RequestParam Long smsId) {
        try {
            smsTaskServiceClient.getSmsTaskService()
                    .disableSmsTask(smsId)
                    .awaitUninterruptibly();
            addAdminLog("删除短信任务", smsId);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Save sms task failed: {}, smsId:{}", ex, smsId);
            return MapMessage.errorMessage("删除短信任务失败:{}", ex);
        }
    }

    @RequestMapping(value = "sumbitsms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitSms() {
        Long smsId = getRequestLong("smsId");
        try {
            // 提交之前做状态检查, 防止重复操作
            SmsTask smsTask = smsTaskServiceClient.getSmsTaskService()
                    .loadSmsTaskFromDB(smsId)
                    .getUninterruptibly();
            if (smsTask == null || smsTask.isDisabledTrue()) {
                return MapMessage.errorMessage("该短信已经被删除，请刷新重新加载页面！");
            }
            if (smsTask.getStatus() != SmsTaskStatus.DRAFT.getType()) {
                return MapMessage.errorMessage("该短信已被提交!");
            }
            // 管理员提交给上一级管理员审核
            SmsTaskStatus status;
            switch (getUserPrivilegeLevel()) {
                case AdminSmsAuditPrivilege.SUPERVISOR_LV3:
                    status = SmsTaskStatus.PENDING_SUPERIOR_LV3;
                    break;
                case AdminSmsAuditPrivilege.SUPERVISOR_LV2:
                    status = SmsTaskStatus.PENDING_SUPERIOR_LV3;
                    break;
                case AdminSmsAuditPrivilege.SUPERVISOR_LV1:
                    status = SmsTaskStatus.PENDING_SUPERIOR_LV2;
                    break;
                case AdminSmsAuditPrivilege.ORDINARY:
                default:
                    status = SmsTaskStatus.PENDING_SUPERIOR_LV1;
                    break;
            }
            MapMessage msg = changeSmsTaskStatus(smsTask, status);
            if (!msg.isSuccess()) {
                return msg;
            }
            sendSmsTaskNotify(smsTask, status, null);
            saveOperationLog("提交审核", smsId, "无");
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Save sms task failed: {}, smsId:{}", ex, smsId);
            return MapMessage.errorMessage("短信提交审核失败:{}", ex);
        }
    }

    @RequestMapping(value = "approvesms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveSms() {
        Long smsId = getRequestLong("smsId");
        int priority = getRequestInt("priority", 0);
        SmsType smsType = SmsType.of(getRequestString("smsType"));
        if (!validSmsTypes.contains(smsType)) {
            return MapMessage.errorMessage("无效的短信类型");
        }
        try {
            int privilege = getUserPrivilegeLevel();
            if (!AdminSmsAuditPrivilege.hasApproveRejectPrivilege(privilege)) {
                return MapMessage.errorMessage("您没有批准的权限！");
            }
            // 批准之前做状态检查, 防止重复操作
            SmsTask smsTask = smsTaskServiceClient.getSmsTaskService()
                    .loadSmsTaskFromDB(smsId)
                    .getUninterruptibly();
            if (smsTask == null || smsTask.isDisabledTrue()) {
                return MapMessage.errorMessage("该短信已经被删除，请刷新重新加载页面！");
            }
            if (smsTask.getStatus() > SmsTaskStatus.CAN_BE_AUDIT_IF_LT.getType()) {
                return MapMessage.errorMessage("该短信已被管理员处理!");
            }
            smsTask.setPriority(priority);
            smsTask.setSmsType(smsType.name());
            MapMessage msg = changeSmsTaskStatus(smsTask, SmsTaskStatus.AUDIT_APPROVED);
            if (!msg.isSuccess()) {
                return msg;
            }
            sendSmsTaskNotify(smsTask, SmsTaskStatus.AUDIT_APPROVED, null);
            saveOperationLog("审批通过,优先级：" + priority, smsId, "无");
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed to approve sms , smsId:{} ex:{}", smsId, ex);
            return MapMessage.errorMessage("批准短信失败:{}", ex);
        }
    }

    @RequestMapping(value = "rejectsms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rejectSms() {
        Long smsId = getRequestLong("smsId");
        String comment = getRequestString("comment");
        try {
            int privilege = getUserPrivilegeLevel();
            if (!AdminSmsAuditPrivilege.hasApproveRejectPrivilege(privilege)) {
                return MapMessage.errorMessage("您没有驳回的权限！");
            }
            // 驳回之前做状态检查, 防止重复操作
            SmsTask smsTask = smsTaskServiceClient.getSmsTaskService()
                    .loadSmsTaskFromDB(smsId)
                    .getUninterruptibly();
            if (smsTask == null || smsTask.isDisabledTrue()) {
                return MapMessage.errorMessage("该短信已经被删除，请刷新重新加载页面！");
            }
            if (smsTask.getStatus() > SmsTaskStatus.CAN_BE_AUDIT_IF_LT.getType()) {
                return MapMessage.errorMessage("该短信已被管理员处理!");
            }
            MapMessage msg = changeSmsTaskStatus(smsTask, SmsTaskStatus.AUDIT_REJECTED);
            if (!msg.isSuccess()) {
                return msg;
            }
            sendSmsTaskNotify(smsTask, SmsTaskStatus.AUDIT_REJECTED, comment);
            saveOperationLog("驳回申请", smsId, comment);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed to reject sms , smsId:{} ex:{}", smsId, ex);
            return MapMessage.errorMessage("驳回短信失败:{}", ex);
        }
    }

    @RequestMapping(value = "raiseup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage raiseUp() {
        Long smsId = getRequestLong("smsId");
        String comment = getRequestString("comment");
        try {
            int privilege = getUserPrivilegeLevel();
            SmsTaskStatus status;
            if (privilege == AdminSmsAuditPrivilege.SUPERVISOR_LV1) {
                status = SmsTaskStatus.PENDING_SUPERIOR_LV2;
            } else if (privilege == AdminSmsAuditPrivilege.SUPERVISOR_LV2) {
                status = SmsTaskStatus.PENDING_SUPERIOR_LV3;
            } else {
                return MapMessage.errorMessage("您没有向上级提交申请的权限！");
            }
            // 转上级之前做状态检查, 防止重复操作
            SmsTask smsTask = smsTaskServiceClient.getSmsTaskService()
                    .loadSmsTaskFromDB(smsId)
                    .getUninterruptibly();
            if (smsTask == null || smsTask.isDisabledTrue()) {
                return MapMessage.errorMessage("该短信已经被删除，请刷新重新加载页面！");
            }
            if (smsTask.getStatus() == status.getType()) {
                return MapMessage.errorMessage("该短信已被管理员处理!");
            }
            MapMessage msg = changeSmsTaskStatus(smsTask, status);
            if (!msg.isSuccess()) {
                return msg;
            }
            sendSmsTaskNotify(smsTask, SmsTaskStatus.AUDIT_REJECTED, comment);
            saveOperationLog("转上级审批", smsId, comment);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed to raise up sms , smsId:{} ex:{}", smsId, ex);
            return MapMessage.errorMessage("转上级操作失败:{} ", ex);
        }
    }

    @RequestMapping(value = "tracesms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage traceSmsFlow(@RequestParam Long smsId) {
        try {
            if (smsId == null) {
                return MapMessage.errorMessage("无效的ID");
            }
            List<AdminLog> traceFlows = adminLogPersistence.withSelectFromTable(TRACE_SMS_FLOW_CONDITION)
                    .useParamsArgs(smsId).queryAll();

            return MapMessage.successMessage().add("trace", generateSmsTraceMap(traceFlows));
        } catch (Exception ex) {
            logger.error("Failed to load sms trace smsId:{}, ex:{}", smsId, ex);
            return MapMessage.errorMessage("查询短信流转记录失败:{}", ex);
        }
    }

    /**
     * 如果是修改过的内容，直接弃掉之前的记录，重新生成一条记录
     */
    private MapMessage saveSmsDetails(SmsTask smsTask, List<String> userTokens, String source) {
        if (StringUtils.equals(source, "new")) {
            MapMessage result =smsServiceClient.getSmsService().addSmsTask(smsTask).getUninterruptibly();
            if (!result.isSuccess()){
                return MapMessage.errorMessage("SmsTask添加失败");
            }
            long taskId = SafeConverter.toLong(result.get("taskId"));
            if (taskId == 0){
                return MapMessage.errorMessage("SmsTask添加失败");
            }
            Map<Integer, Set<String>> integerSetMap = userTokens.stream().collect(Collectors.groupingBy(p -> userTokens.indexOf(p) / 2000, Collectors.toSet()));
            integerSetMap.forEach((k,list) -> {
                sendMessageToQueue(taskId,list);
            });
            saveOperationLog("新建短信", taskId, "无");
        } else {
            MapMessage message = smsServiceClient.getSmsService()
                    .updateSmsDetails(smsTask.getId(), smsTask)
                    .getUninterruptibly();
            if (!message.isSuccess()) {
                return message;
            }
            saveOperationLog("修改短信", smsTask.getId(), "无");
        }
        return MapMessage.successMessage();
    }

    private void sendMessageToQueue(Long taskId,Set<String> targets){
        Map<String, Object> command = new HashMap<>();
        command.put("command", "admin_sms_bach_save");
        command.put("targets",StringUtils.join(targets,","));
        command.put("taskId",taskId);
        Message message = Message.newMessage();
        String json = JsonUtils.toJson(command);
        message.withPlainTextBody(json);
        adminCommandQueueProducer.getProducer().produce(message);
    }


    // private methods

    /**
     * 根据用户的权限，查出用户可见的短信列表
     */
    private List<SmsTask> loadSmsTasks(List<SmsTask> allSmsTask, int privilege) {
        Set<Integer> userPrivileges = AdminSmsAuditPrivilege.getUserPrivileges(privilege)
                .stream().map(SmsTaskStatus::getType).collect(Collectors.toSet());

        final String creator = getCurrentAdminUser().getAdminUserName();
        // 根据用户名全部load出来做filter
        return allSmsTask.stream()
                .filter(e -> !e.isDisabledTrue())
                .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                .filter(t -> creator.equals(t.getCreator()) || userPrivileges.contains(t.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 包装查询出来的短信信息，便于前端展示
     */
    private List<Map<String, Object>> generateSmsTaskMap(List<SmsTask> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> results = new ArrayList<>();
        tasks.forEach(t -> results.add(generateSingleSmsTask(t, false)));
        return results;
    }

    private Map<String, Object> generateSingleSmsTask(SmsTask smsTask, boolean isDetail) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", smsTask.getId());
        map.put("creator", smsTask.getCreatorName());
        map.put("smsType", smsTask.getSmsType());
        String formatDate = DateUtils.dateToString(smsTask.getSmsSendtime(), DateUtils.FORMAT_SQL_DATETIME);
        map.put("sendTime", formatDate.replace(" ", "<br/>"));
        map.put("smsSendtime", formatDate);
        map.put("smsText", smsTask.getSmsText());
        map.put("ut", smsTask.getReceiveUserType());
        String userType = "身份：" + UserType.of(smsTask.getReceiveUserType()).getDescription() +
                "<br>人数：" + smsServiceClient.getSmsService().getSmsTaskRefCount(smsTask.getId()).getUninterruptibly();
        map.put("userType", userType);
        map.put("purpose", smsTask.getSmsPurpose());
        map.put("status", smsTask.getStatus());
        if (isDetail) {
            List<String> userTokens = smsServiceClient.getSmsService()
                    .loadBySmsTaskId(smsTask.getId(), true)
                    .getUninterruptibly()
                    .stream().map(SmsTaskReceiverRef::getSmsReceiver).collect(Collectors.toList());
            map.put("target", StringUtils.join(userTokens, "\r\n"));
        }
        map.put("statusDesc", SmsTaskStatus.of(smsTask.getStatus()).getDesc());
        Integer priority = smsTask.getPriority();
        String ps = "☆";
        if (priority > 0 && priority < 10) {
            ps = "";
            while (priority >= 0) {
                ps += "★";
                priority -= 2;
            }
        }
        map.put("priority", ps);
        map.put("priorityVal", smsTask.getPriority());
        map.put("smsType", smsTask.getSmsType());
        return map;
    }

    /**
     * 包装查询出来的短信流转日志信息，便于前端展示
     */
    private List<Map<String, String>> generateSmsTraceMap(List<AdminLog> traceLog) {
        if (CollectionUtils.isEmpty(traceLog)) {
            return Collections.emptyList();
        }
        List<Map<String, String>> results = new ArrayList<>();
        traceLog.forEach(t -> {
            Map<String, String> map = new HashMap<>();
            map.put("operator", t.getAdminUserName());
            map.put("operation", t.getTargetStr());
            map.put("comment", t.getComment());
            map.put("createtime", DateUtils.dateToString(t.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
            results.add(map);
        });
        return results;
    }

    /**
     * 验证短信的各项参数的有效性
     */
    private MapMessage validateSmsTask(SmsTask smsTask, List<String> users, String source) {
        if (smsTask == null) {
            return MapMessage.errorMessage("参数错误");
        }
        StringBuilder msg = new StringBuilder();
        // 校验短信内容
        if (StringUtils.isBlank(smsTask.getSmsText())) {
            msg.append("\r\n短信内容不能为空！");
        }
        String badWord = badWordCheckerClient.checkConversationBadWord(smsTask.getSmsText());
        if (StringUtils.isNotBlank(badWord)) {
            msg.append("\r\n请删除敏感词：").append(badWord);
        }
        // 校验用户类型只能是 学生/老师/家长
        if (!Range.between(1, 3).contains(smsTask.getReceiveUserType())) {
            msg.append("\r\n接收用户类型错误！");
        }
        // 校验发送时间
        if (smsTask.getSmsSendtime().before(new Date())) {
            msg.append("\r\n发送时间不能早于当前时间！");
        }
        if (!validSmsTypes.contains(SmsType.of(smsTask.getSmsType()))) {
            msg.append("\r\n无效的短信发送类型！");
        }

        // 校验发送用户
        if (StringUtils.equals("new", source) && CollectionUtils.isEmpty(users)) {
            msg.append("\r\n发送用户不能为空！");
        }
        if (StringUtils.isBlank(smsTask.getSmsPurpose())) {
            msg.append("\r\n短信发送目的不能为空！");
        }
        // 返回校验信息
        if (msg.length() > 0) {
            return MapMessage.errorMessage(msg.toString());
        }
        return MapMessage.successMessage();
    }

    /**
     * 更新当前短信的状态
     */
    private MapMessage changeSmsTaskStatus(SmsTask smsTask, SmsTaskStatus status) {
        try {
            if (status == SmsTaskStatus.AUDIT_APPROVED || status == SmsTaskStatus.AUDIT_REJECTED) {
                AuthCurrentAdminUser adminUser = getCurrentAdminUser();
                smsTask.setAuditor(adminUser.getAdminUserName());
                smsTask.setAuditorName(adminUser.getRealName());
                smsTask.setAuditDatetime(new Date());
            }
            smsTask.setStatus(status.getType());
            return smsServiceClient.getSmsService()
                    .updateSmsDetails(smsTask.getId(), smsTask)
                    .getUninterruptibly();
        } catch (Exception ex) {
            return MapMessage.errorMessage("更改短信状态失败: smsId:{}, status:{}, ex:{}", smsTask.getId(), status, ex);
        }
    }

    /**
     * TODO 调整一下这个的记录方式
     * 统一记录操作的日志的格式，便于之后查询
     */
    private void saveOperationLog(String operation, Long targetId, String comment) {
        addAdminLog("SMS_TRACE", targetId, operation, comment, null);
    }

    /**
     * TODO
     * 1. 提交的时候发送给1级管理员审核 (submitsms.vpage)
     * 2. 1级管理员转上级的时候发送给2级管理员审核 (raiseup.vpage)
     * 3. 2级管理员转上级的时候发送给3级管理员 (raiseup.vpage)
     * 4. 管理员审批/驳回，发送给创建人 (approvesms.vpage/rejectsms.vpage)
     */
    private void sendSmsTaskNotify(SmsTask smsTask, SmsTaskStatus status, String comment) {
        String receiver;
        String operation;
        try {
            switch (status) {
                case PENDING_SUPERIOR_LV1:
                    receiver = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), ADMIN_CONFIG_KEY_NAME_LV1);
                    operation = "点击链接前往审核";
                    break;
                case PENDING_SUPERIOR_LV2:
                    receiver = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), ADMIN_CONFIG_KEY_NAME_LV1);
                    operation = "点击链接前往审核";
                    break;
                case PENDING_SUPERIOR_LV3:
                    receiver = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), ADMIN_CONFIG_KEY_NAME_LV1);
                    operation = "点击链接前往审核";
                    break;
                case AUDIT_APPROVED:
                    receiver = smsTask.getCreator();
                    operation = "已通过审核";
                    break;
                case AUDIT_REJECTED:
                    receiver = smsTask.getCreator();
                    operation = "未通过审核，拒绝原因：" + comment;
                    break;
                default:
                    return;
            }
            String receiveEmails = StringUtils.join(receiver.split(","), MAIL_SUFFIX).concat(MAIL_SUFFIX);
            receiveEmails = receiveEmails.substring(0, receiveEmails.length() - 1);

            String ccUsers = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), SMS_TASK_MAIL_CC);
            String ccEmails = StringUtils.join(ccUsers.split(","), MAIL_SUFFIX).concat(MAIL_SUFFIX);
            ccEmails = ccEmails.substring(0, ccEmails.length() - 1);
            if (StringUtils.isBlank(ccEmails)) {
                ccEmails = "yuechen.wang@17zuoye.com";
            }
            String sendtime = DateUtils.dateToString(smsTask.getSmsSendtime(), DateUtils.FORMAT_SQL_DATETIME);
            String url = getAdminBaseUrl() + "/opmanager/smstask/index.vpage";
            Map<String, Object> content = new HashMap<>();
            content.put("creator", smsTask.getCreatorName());
            content.put("sendtime", sendtime);
            content.put("url", url);
            content.put("operation", operation);
            content.put("text", smsTask.getSmsText());
            emailServiceClient.createTemplateEmail(EmailTemplate.adminsmstask)
                    .to(receiveEmails)
                    .cc(ccEmails)
                    .subject("运营平台通知(来自：" + RuntimeMode.getCurrentStage() + "环境)")
                    .content(content)
                    .send();
        } catch (Exception ex) {
            // 发生异常
            emailServiceClient.createTemplateEmail(EmailTemplate.office)
                    .to("yuechen.wang@17zuoye.com")
                    .subject("运营平台通知(来自：" + RuntimeMode.getCurrentStage() + "环境)")
                    .content(MiscUtils.m("info", ex))
                    .send();
        }
    }

    private int getUserPrivilegeLevel() {
        String userName = getCurrentAdminUser().getAdminUserName();
        try {
            if (commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), ADMIN_CONFIG_KEY_NAME_LV3).contains(userName)) {
                return AdminSmsAuditPrivilege.SUPERVISOR_LV3;
            } else if (commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), ADMIN_CONFIG_KEY_NAME_LV2).contains(userName)) {
                return AdminSmsAuditPrivilege.SUPERVISOR_LV2;
            } else if (commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), ADMIN_CONFIG_KEY_NAME_LV1).contains(userName)) {
                return AdminSmsAuditPrivilege.SUPERVISOR_LV1;
            } else {
                return AdminSmsAuditPrivilege.ORDINARY;
            }
        } catch (Exception ex) {
            logger.error("can not find config :{}", ex);
            return AdminSmsAuditPrivilege.ORDINARY;
        }
    }


    private List<String> getTargetsFromExcel(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        List<String> targets = new ArrayList<>();
        int rows = 1;
        if (null != sheet) {
            while (true) {
                try {
                    XSSFRow row = sheet.getRow(rows++);
                    if (row == null) {
                        break;
                    }
                    String target = XssfUtils.getStringCellValue(row.getCell(0));
                    if (StringUtils.isNotEmpty(target)) {
                        targets.add(target);
                    }
                } catch (Exception ex) {
                    logger.error("read excel failed", ex);
                    break;
                }
            }
        }
        return targets;
    }


    private XSSFWorkbook readRequestWorkbook(String name) {
        HttpServletRequest request = getRequest();
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

    private void setCellValue(Row row, int column, CellStyle style, Object value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        if (null != style) {
            cell.setCellStyle(style);
        }
        String info = value == null ? "" : String.valueOf(value).trim();
        if (!NumberUtils.isDigits(info)) {
            cell.setCellValue(info);
        } else {
            cell.setCellValue(SafeConverter.toLong(info));
        }
    }

}
