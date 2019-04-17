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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Shuang li
 * @since 下午8:44,18-10-15.
 */
@Controller
@RequestMapping("/site/order")
public class SiteOrderController extends SiteAbstractController {

    // 批量查询用户购买增值产品
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String orderIndex(Model model) {
        return "site/order/index";
    }

    // Validation Check
    @RequestMapping(value = "ordervalitioncheck.vpage", method = RequestMethod.POST)
    @ResponseBody
    private MapMessage orderValitionCheck(@RequestParam String keyList) {
        int limit = 1000;
        if (StringUtils.isBlank(keyList)) {
            return MapMessage.errorMessage("参数错误");
        }
        String[] keyLines = keyList.split("\\n");
        if (keyLines.length > limit) {
            return MapMessage.errorMessage("输入参数行数，已达到数量上限");
        }
        if (!keyList.contains(" ") && !keyList.contains("\t")) {
            return MapMessage.errorMessage("参数错误");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "downloadorderdetail.vpage", method = RequestMethod.POST)
    public void downloadStatInfo(@RequestParam String keyList, HttpServletResponse response) {
        List<UserAuthentication> userList = new ArrayList<>();

        orderValitionCheck(keyList);

        String[] keyLines = keyList.split("\\n");
        Set<Long> exportedUserIds = new HashSet<>();
        List<Map<String, Object>> result = new ArrayList<>();

        for (String keyLine : keyLines) {
            List<Long> userIdList = new ArrayList<>();
            String keys = null;
            keys = keyLine.replace("\t", " ");
            if (!keys.contains(" ")) {
                getAlertMessageManager().addMessageError("参数错误");
                return;
            }
            String a1 = keys.substring(0, keys.indexOf(" "));
            String a2 = keys.substring(keys.indexOf(" ") + 1, keys.length());

            List<User> candidates = userLoaderClient.loadUserByToken(a1);
            for (User candidate : candidates) {
                if (candidate.isStudent()) {
                    userIdList.add(candidate.getId());

                    Set<Long> parentId = studentLoaderClient.loadStudentParentRefs(candidate.getId()).stream()
                            .filter(c -> !c.isDisabledTrue()).map(StudentParentRef::getParentId).collect(Collectors.toSet());
                    if (!CollectionUtils.isEmpty(parentId)) userIdList.addAll(parentId);
                } else if (candidate.isParent()) {
                    userIdList.add(candidate.getId());
                    Set<Long> childrenId = parentLoaderClient.loadParentStudentRefs(candidate.getId()).stream()
                            .filter(c -> !c.isDisabledTrue()).map(StudentParentRef::getStudentId).collect(Collectors.toSet());
                    if (!CollectionUtils.isEmpty(childrenId)) userIdList.addAll(childrenId);
                }
            }

            String a2temp = null;
            a2temp = a2.replace("/", "-");
            Date checkTime = DateUtils.stringToDate(a2temp.trim(), DateUtils.FORMAT_SQL_DATETIME);

            for (Long userId : userIdList) {
                // 已经处理过的用户就不需要处理了
                if (exportedUserIds.contains(userId)) {
                    continue;
                }

                User user = userLoaderClient.loadUser(userId);

                List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId);
                paymentHistories = paymentHistories.stream()
                        .filter(p -> p.getPaymentStatus() == PaymentStatus.Paid)
                        .filter(p -> p.getPayDatetime().after(checkTime))
                        .collect(Collectors.toList());

                // 没有付费记录
                if (CollectionUtils.isEmpty(paymentHistories)) {
                    if (user.isParent() && user.getCreateTime().after(checkTime)) {
                        Map<String, Object> orderDetailMap1 = new HashMap<>();
                        orderDetailMap1.put("id", a1);
                        orderDetailMap1.put("consultDate", a2.trim());
                        orderDetailMap1.put("userID", userId);
                        orderDetailMap1.put("isParent", "Y");
                        orderDetailMap1.put("parentCreateDate", DateUtils.dateToString(user.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
                        orderDetailMap1.put("productName", "-");
                        orderDetailMap1.put("orderNo", "-");
                        orderDetailMap1.put("productPrice", "-");
                        orderDetailMap1.put("payedDate", "-");
                        orderDetailMap1.put("isRefund", "-");
                        result.add(orderDetailMap1);
                    }
                    continue;
                }

                for (UserOrderPaymentHistory paymentHistory : paymentHistories) {
                    // 获取订单信息
                    int orderTableMod = SafeConverter.toInt(userId % 100);
                    UserOrder userOrder = userOrderLoaderClient.loadUserOrder(paymentHistory.getOrderId() + "_" + orderTableMod);

                    if (userOrder == null || OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) == OrderProductServiceType.YiQiXue) {
                        continue;
                    }

                    Map<String, Object> orderDetailMap = new HashMap<>();
                    orderDetailMap.put("id", a1);
                    orderDetailMap.put("consultDate", a2.trim());
                    orderDetailMap.put("userID", userId);
                    orderDetailMap.put("isParent", user.isParent() ? "Y" : "N");
                    orderDetailMap.put("parentCreateDate", user.isParent() ? DateUtils.dateToString(user.getCreateTime(), "yyyy-MM-dd HH:mm:ss") : "-");
                    orderDetailMap.put("productName", userOrder.getProductName());
                    orderDetailMap.put("orderNo", userOrder.getId());
                    orderDetailMap.put("productPrice", userOrder.getOrderPrice().setScale(2, BigDecimal.ROUND_HALF_UP));
                    orderDetailMap.put("payedDate", DateUtils.dateToString(paymentHistory.getPayDatetime(), "yyyy-MM-dd HH:mm:ss"));
                    orderDetailMap.put("isRefund", userOrder.getPaymentStatus() == PaymentStatus.Paid ? "否" : "是");
                    result.add(orderDetailMap);
                }

                exportedUserIds.add(userId);
            }
        }
        XSSFWorkbook xssfWorkbook = convertToXSSI(result);
        String filename = "批量查询用户购买增值产品详情" + "-" + DateUtils.dateToString(new Date()) + ".xlsx";
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            xssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException ignored) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                logger.error("download cheating teacher list exception!");
            }
        }
    }

    private XSSFWorkbook convertToXSSI(List<Map<String, Object>> dataList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("账号/手机号码");
        firstRow.createCell(1).setCellValue("咨询日期");
        firstRow.createCell(2).setCellValue("用户ID");
        firstRow.createCell(3).setCellValue("是否家长");
        firstRow.createCell(4).setCellValue("家长注册时间");
        firstRow.createCell(5).setCellValue("购买产品名称");
        firstRow.createCell(6).setCellValue("购买产品单号");
        firstRow.createCell(7).setCellValue("购买产品价位");
        firstRow.createCell(8).setCellValue("购买产品时间");
        firstRow.createCell(9).setCellValue("是否退款");
        int rowNum = 1;
        for (Map<String, Object> data : dataList) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            xssfRow.setHeightInPoints(20);
            xssfRow.createCell(0).setCellValue(ConversionUtils.toString(data.get("id")));
            xssfRow.createCell(1).setCellValue(ConversionUtils.toString(data.get("consultDate")));
            xssfRow.createCell(2).setCellValue(ConversionUtils.toString(data.get("userID")));
            xssfRow.createCell(3).setCellValue(ConversionUtils.toString(data.get("isParent")));
            xssfRow.createCell(4).setCellValue(ConversionUtils.toString(data.get("parentCreateDate")));
            xssfRow.createCell(5).setCellValue(ConversionUtils.toString(data.get("productName")));
            xssfRow.createCell(6).setCellValue(ConversionUtils.toString(data.get("orderNo")));
            xssfRow.createCell(7).setCellValue(ConversionUtils.toString(data.get("productPrice")));
            xssfRow.createCell(8).setCellValue(ConversionUtils.toString(data.get("payedDate")));
            xssfRow.createCell(9).setCellValue(ConversionUtils.toString(data.get("isRefund")));
        }
        for (int i = 0; i < 10; i++) {
            xssfSheet.autoSizeColumn((short) i);
        }
        return xssfWorkbook;
    }
}
