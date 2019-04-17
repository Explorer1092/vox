/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.task;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.AgentTaskDetailService;
import com.voxlearning.utopia.agent.service.task.TaskService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/15.
 */
@Controller
@RequestMapping("/task")
@Slf4j
public class TaskController extends AbstractAgentController {

    @Inject private TaskService taskService;
    @Inject private AgentTaskDetailService agentTaskDetailService;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private UserOrderServiceClient userOrderServiceClient;

    private static final String EXPORT_ORDER_INFO_TEMPLATE = "/config/templates/export_order_info_template.xlsx";

    @RequestMapping(value = "todolist/exportOrderInfo.vpage", method = RequestMethod.GET)
    public void exportOrderInfo() {
        try {
            String filename = "订单下载信息下载-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            List<Map<String, Object>> dataList = taskService.getAssignments(getCurrentUserId());
            XSSFWorkbook workbook = convertToOrderInfo(dataList);
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            assert workbook != null;
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception ex) {
            logger.error("export school dict info is failed", ex);
        }
    }

    private XSSFWorkbook convertToOrderInfo(List<Map<String, Object>> orderData) {
        Resource resource = new ClassPathResource(EXPORT_ORDER_INFO_TEMPLATE);
        if (!resource.exists()) {
            logger.error("exportSchoolTasks - template not exists");
            return null;
        }
        try {
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            if (CollectionUtils.isEmpty(orderData)) {
                return workbook;
            }
            int index = 1;
            for (Map<String, Object> data : orderData) {
                if (data == null) {
                    continue;
                }
                if (Objects.equals(data.get("orderTypeStr"), "材料购买") || Objects.equals(data.get("orderTypeStr"), "保证金收款")) {
                    continue;
                }
                XSSFRow row = sheet.createRow(index++);
                createCell(row, 0, cellStyle, format(data.get("orderId")));
                createCell(row, 1, cellStyle, format(data.get("creator")));
                createCell(row, 2, cellStyle, format(data.get("orderTypeStr")));
                createCell(row, 3, cellStyle, format(data.get("orderProducts")));
                createCell(row, 4, cellStyle, format(data.get("orderAmount")));
                createCell(row, 5, cellStyle, format(data.get("orderNotes")));
                createCell(row, 6, cellStyle, format(data.get("orderStatusStr")));
                createCell(row, 7, cellStyle, format(StringUtils.formatMessage("收货人:{},收货人电话:{},收货地址:{}。", data.get("consignee"), data.get("mobile"), data.get("address"))));
                createCell(row, 8, cellStyle, format(data.get("usableCashAmount")));
            }
            return workbook;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 设置单个单元格的样式和内容
     */
    private XSSFCell createCell(XSSFRow row, int index, XSSFCellStyle style, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
        return cell;
    }

    /**
     * 设置字符类型文字
     */
    private String format(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @RequestMapping(value = "todolist/index.vpage", method = RequestMethod.GET)
    String todoIndex(Model model) {
        model.addAttribute("assignments", taskService.getAssignments(getCurrentUserId()));
        return "task/todolist/index";
    }

    @RequestMapping(value = "todolist/finish_task_detail_state.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage finishTaskDetailStatus() {
        String taskDetailId = getRequestString("taskDetailId");
        Long userId = getCurrentUserId();
        return agentTaskDetailService.finishTaskDetailStatus(taskDetailId, userId);
    }

    @RequestMapping(value = "todolist/approveorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage approveOrder(Long id, Integer orderType, String comment) {

        MapMessage mapMessage = new MapMessage();
        try {
            taskService.approveOrder(id, orderType, getCurrentUser(), comment);

            asyncLogService.logTask(getCurrentUser(), getRequest().getRequestURI(), "Task Approved",
                    "id：" + id + " comment:" + comment);

            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            log.error("通过订单失败,taskId:{},orderType:{},msg:{}", id, orderType, ex.getMessage(), ex);
            mapMessage.setSuccess(false);
            mapMessage.setInfo("操作失败!" + ex.getMessage());
            return mapMessage;
        }
        return mapMessage;
    }

    @RequestMapping(value = "todolist/confirmorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage confirmOrder(Long id) {

        MapMessage mapMessage = new MapMessage();
        try {
            taskService.confirmOrder(id, getCurrentUser());

            asyncLogService.logTask(getCurrentUser(), getRequest().getRequestURI(), "Task Confirmed",
                    "id：" + id);

            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            log.error("通过订单失败,taskId:{},msg:{}", id, ex.getMessage(), ex);
            mapMessage.setSuccess(false);
            mapMessage.setInfo("操作失败!" + ex.getMessage());
            return mapMessage;
        }
        return mapMessage;
    }

    @RequestMapping(value = "todolist/rejectorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage rejectOrder(Long id, String comment) {

        MapMessage mapMessage = new MapMessage();
        try {
            taskService.rejectOrder(id, getCurrentUser(), comment);

            asyncLogService.logTask(getCurrentUser(), getRequest().getRequestURI(), "Task Rejcted",
                    "id：" + id + " comment:" + comment);

            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            log.error("通过订单失败,taskId:{},msg:{}", id, ex.getMessage(), ex);
            mapMessage.setSuccess(false);
            mapMessage.setInfo("操作失败!" + ex.getMessage());
            return mapMessage;
        }
        return mapMessage;
    }

    @RequestMapping(value = "todolist/loadorderhistories.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage loadOrderHistory(Long orderId) {

        MapMessage mapMessage = new MapMessage();
        try {
            List<Map<String, Object>> histories = taskService.loadOrderHistory(orderId);
            mapMessage.setSuccess(true);
            mapMessage.set("value", histories);
        } catch (Exception ex) {
            log.error("加载订单历史列表失败,orderId:{},msg:{}", ex.getMessage(), ex);
            mapMessage.setSuccess(false);
            mapMessage.setInfo("操作失败!" + ex.getMessage());
            return mapMessage;
        }
        return mapMessage;
    }


    @RequestMapping(value = "donelist/index.vpage", method = RequestMethod.GET)
    String doneIndex(Model model) {
        model.addAttribute("doneList", taskService.getMyProceedOrderList(getCurrentUserId()));
        return "task/donelist/index";
    }

    @RequestMapping(value = "failorderlist/index.vpage", method = RequestMethod.GET)
    String failOrderList(Model model) {
        Long userId = getRequestLong("userId");
        List<UserOrderPaymentHistory> paymentHistories = new ArrayList<>();
        if (userId != 0) {
            paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId).stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.RefundFail)
                    .collect(Collectors.toList());
        }
        model.addAttribute("failList", paymentHistories);
        model.addAttribute("userId", userId);
        return "task/failorderlist/index";
    }

    @RequestMapping(value = "failorderlist/updatepaymenthistory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updatePaymentHistory() {
        Long userId = getRequestLong("userId");
        String orderId = getRequestString("orderId");
        if (userId == 0 || StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage("参数错误");
        }
        List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId);
        if (CollectionUtils.isEmpty(paymentHistories)) {
            return MapMessage.errorMessage("请刷新页面重试");
        }
        UserOrderPaymentHistory history = paymentHistories.stream().filter(h -> Objects.equals(orderId, h.getOrderId()))
                .filter(h -> h.getPaymentStatus() == PaymentStatus.RefundFail)
                .findFirst().orElse(null);
        if (history == null) {
            return MapMessage.errorMessage("数据不存在，请刷新页面重试");
        }
        // 修改为退款成功
        return userOrderServiceClient.updatePaymentHistoryStatus(history, PaymentStatus.Refund);
    }


    // 接收CRM端发送过来的请求，在Marketing创建退款单
    @RequestMapping(value = "crm/refundorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createCrmRefundOrder() {
        // 获取生成订单需要的参数
        String creator = getRequestString("userId");
        String creatorName = getRequestString("userName");
        String orderId = getRequestString("orderId");
        String productName = getRequestString("productName");
        Double amount = getRequestDouble("amount");
        Float refundAmount = SafeConverter.toFloat(getRequestParameter("refundAmount", ""));
        String transactionId = getRequestString("transactionId");
        String payMethod = getRequestString("payMethod");
        String adminUser = getRequestString("adminUser");
        String adminUserName = getRequestString("adminUserName");
        String memo = getRequestString("memo");

        Map<String, Object> notesMap = new HashMap<>();
        notesMap.put("orderId", orderId);
        notesMap.put("productName", productName);
        notesMap.put("transactionId", transactionId);
        notesMap.put("amount", amount);
        notesMap.put("payMethod", payMethod);
        notesMap.put("memo", memo);
        String orderNotes = JsonUtils.toJson(notesMap);
        // 初始化一个Agent订单，直接提交给财务审查
        AgentOrder agentOrder = new AgentOrder();
        agentOrder.setCreator(SafeConverter.toLong(creator));
        agentOrder.setCreatorName(creatorName);
        agentOrder.setOrderAmount(refundAmount);
        agentOrder.setLatestProcessor(0L);
        agentOrder.setLatestProcessorName(adminUserName);
        agentOrder.setOrderNotes(orderNotes);
        return taskService.createRefundOrder(agentOrder, creator, adminUser);
    }

    // 接收CRM端发送过来的请求，在Marketing创建余额提现申请
    @RequestMapping(value = "crm/cashwithdraw.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createCrmCashWithdraw() {
        // 获取生成订单需要的参数
        String creator = getRequestString("userId");
        String creatorName = getRequestString("userName");
        Float refundAmount = SafeConverter.toFloat(getRequestParameter("amount", ""));
        Map<String, String> crmRequestMap = JsonUtils.fromJsonToMapStringString(getRequestString("transactionIds"));
        String adminUser = getRequestString("adminUser");
        String adminUserName = getRequestString("adminUserName");
        // 初始化一个Agent订单，直接提交给财务审查
        AgentOrder agentOrder = new AgentOrder();
        agentOrder.setCreator(SafeConverter.toLong(creator));
        agentOrder.setCreatorName(creatorName);
        agentOrder.setOrderAmount(refundAmount);
        agentOrder.setLatestProcessor(0L);
        agentOrder.setLatestProcessorName(adminUserName);
        agentOrder.setOrderNotes(JsonUtils.toJson(crmRequestMap.keySet()));
        return taskService.createCashWithdraw(agentOrder, creator, adminUser);
    }

    @RequestMapping(value = "todolist/financeflow.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage getCrmFinanceFlowByOrderId(Long agentOrderId) {
        try {
            return taskService.loadCrmFinanceFlow(agentOrderId);
        } catch (Exception ex) {
            log.error("读取,orderId:{},msg:{}", ex.getMessage(), ex);
            return MapMessage.errorMessage("操作失败!" + ex.getMessage());
        }
    }

    // 支付宝 微信 批量退款
    @RequestMapping(value = "todolist/batchrefund.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchRefund() {
        String orderIds = getRequestString("orderIds");
        String comment = getRequestString("comment");
        if (StringUtils.isBlank(orderIds) || StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return AtomicLockManager.instance().wrapAtomic(taskService)
                    .keyPrefix("taskService.batchRefund")
                    .keys(getCurrentUser().getUserId())
                    .proxy().batchRefundUserOrder(getCurrentUser(), orderIds, comment);
        } catch (CannotAcquireLockException ex) {
            logger.error("Agent batch refund error npc {}: DUPLICATED OPERATION， agentProcessIds {} ", orderIds);
            return MapMessage.errorMessage();
        }
    }
}
