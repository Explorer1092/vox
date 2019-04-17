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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.entity.o2o.TrusteeOrderRecord;
import com.voxlearning.utopia.entity.o2o.TrusteeReserveRecord;
import com.voxlearning.utopia.service.config.client.BusinessActivityManagerClient;
import com.voxlearning.utopia.service.config.client.LegacyBusinessActivityServiceClient;
import com.voxlearning.utopia.service.order.consumer.TrusteeOrderServiceClient;
import com.voxlearning.utopia.service.user.api.entities.BusinessActivity;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/6/28.
 * <p>
 * 通用导流、报名、加群活动管理
 */
@Controller
@RequestMapping("/opmanager/businessactivity")
public class CrmBusinessActivityController extends OpManagerAbstractController {

    @Inject private LegacyBusinessActivityServiceClient legacyBusinessActivityServiceClient;
    @Inject private BusinessActivityManagerClient businessActivityManagerClient;
    @Inject private TrusteeOrderServiceClient trusteeOrderServiceClient;

    // 列表页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        // 入口关闭  功能关闭
//        return "opmanager/index";
        // 获取全部的活动列表
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        String type = getRequestString("type");
        Pageable pageable = new PageRequest(page - 1, 10);

        List<BusinessActivity> activities = businessActivityManagerClient.getBusinessActivityManager()
                .loadAllBusinessActivitiesFromDB()
                .getUninterruptibly()
                .stream()
                .filter(a -> StringUtils.isBlank(type) || a.getActivityType().name().equals(type))
                .collect(Collectors.toList());
        Page<BusinessActivity> businessActivityPage = PageableUtils.listToPage(activities, pageable);
        model.addAttribute("businessActivityPage", businessActivityPage);
        model.addAttribute("types", BusinessActivity.ActivityType.values());
        model.addAttribute("currentPage", businessActivityPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", businessActivityPage.getTotalPages());
        model.addAttribute("hasPrev", businessActivityPage.hasPrevious());
        model.addAttribute("hasNext", businessActivityPage.hasNext());
        model.addAttribute("type", type);
        return "opmanager/businessactivity/index";
    }

    // 添加编辑跳转
    @RequestMapping(value = "activitydetail.vpage", method = RequestMethod.GET)
    public String activityDetail(Model model) {
        Long aid = getRequestLong("aid");
        if (aid != 0L) {
            BusinessActivity activity = businessActivityManagerClient.getBusinessActivityManager()
                    .loadBusinessActivityFromDB(aid)
                    .getUninterruptibly();
            if (activity != null) {
                model.addAttribute("activity", activity);
            }
        }
        model.addAttribute("aid", aid);
        model.addAttribute("status", BusinessActivity.Status.values());
        model.addAttribute("types", BusinessActivity.ActivityType.values());
        return "opmanager/businessactivity/activitydetail";
    }

    // 添加编辑 post
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveActivityDetail() {
        // 获取参数
        Long aid = getRequestLong("aid");
        try {
            BusinessActivity activity;
            if (aid == 0L) {
                activity = new BusinessActivity();
            } else {
                activity = businessActivityManagerClient.getBusinessActivityManager()
                        .loadBusinessActivityFromDB(aid)
                        .getUninterruptibly();
                if (activity == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
            }
            requestFillEntity(activity);
            activity.setActivityType(BusinessActivity.ActivityType.valueOf(getRequestString("activityType")));
            activity.setStatus(BusinessActivity.Status.valueOf(getRequestString("status")));
            // 保存实体
            MapMessage returnMsg;
            String op = "新建活动";
            if (aid == 0L) {
                returnMsg = legacyBusinessActivityServiceClient.getLegacyBusinessActivityService()
                        .createActivity(activity);
                businessActivityManagerClient.getBusinessActivityManager()
                        .reloadBusinessActivityBuffer();
                aid = SafeConverter.toLong(returnMsg.get("id"));
                // 新建的要重新生成一个URL 放进去
                String url = getMainHostBaseUrl() + "/seattle/index.vpage?id=" + aid;
                BusinessActivity newActivity = businessActivityManagerClient.getBusinessActivityManager()
                        .loadBusinessActivityFromDB(aid)
                        .getUninterruptibly();
                newActivity.setActivityUrl(url);
                returnMsg = legacyBusinessActivityServiceClient.getLegacyBusinessActivityService()
                        .updateActivity(aid, newActivity);
                businessActivityManagerClient.getBusinessActivityManager()
                        .reloadBusinessActivityBuffer();
            } else {
                activity.setUpdateDatetime(new Date());
                returnMsg = legacyBusinessActivityServiceClient.getLegacyBusinessActivityService()
                        .updateActivity(aid, activity);
                businessActivityManagerClient.getBusinessActivityManager()
                        .reloadBusinessActivityBuffer();
                op = "编辑活动";
            }
            returnMsg.setInfo(returnMsg.isSuccess() ? "保存成功！" : "保存失败!");
            saveOperationLog("SEATTLE_TRACE", aid, op, returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Save activity error! id={}, ex={}", aid, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存活动失败:{}", ex.getMessage(), ex);
        }
    }

    // 查看报名
    @RequestMapping(value = "checkHistory.vpage", method = RequestMethod.GET)
    public String checkHistory(Model model) {
        Long aid = getRequestLong("aid");
        String type = getRequestString("type");
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 10);
        long totalCount = 0;
        int totalPage = 0;
        boolean hasPrevious = false;
        boolean hasNext = false;
        try {
            BusinessActivity.ActivityType activityType = BusinessActivity.ActivityType.valueOf(type);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (activityType == BusinessActivity.ActivityType.Pay) {
                // 付费的 trustee_order_record
                Page<TrusteeOrderRecord> orderRecordsPage = trusteeOrderServiceClient.loadTrusteeOrderPageByActivityId(aid, pageable);
                dataList = orderRecordsToMap(orderRecordsPage.getContent());
                totalCount = orderRecordsPage.getTotalElements();
                totalPage = orderRecordsPage.getTotalPages();
                hasPrevious = orderRecordsPage.hasPrevious();
                hasNext = orderRecordsPage.hasNext();
            } else if (activityType == BusinessActivity.ActivityType.Reserve || activityType == BusinessActivity.ActivityType.Subscribe) {
                // 报名的或者预约的
                Page<TrusteeReserveRecord> reserveRecordsPage = trusteeOrderServiceClient.loadTrusteeReservesPageByActivityId(aid, pageable);
                dataList = reserveRecordsToMap(reserveRecordsPage.getContent());
                totalCount = reserveRecordsPage.getTotalElements();
                totalPage = reserveRecordsPage.getTotalPages();
                hasPrevious = reserveRecordsPage.hasPrevious();
                hasNext = reserveRecordsPage.hasNext();
            }
            // 查询具体的报名或者支付信息
            model.addAttribute("dataListPage", new PageImpl<>(dataList, pageable, totalCount));
            model.addAttribute("currentPage", totalPage < page ? 1 : page);
            model.addAttribute("totalPage", totalPage);
            model.addAttribute("hasPrev", hasPrevious);
            model.addAttribute("hasNext", hasNext);
            model.addAttribute("type", type);
            model.addAttribute("aid", aid);
            return "opmanager/businessactivity/checkHistory";
        } catch (Exception ex) {
            model.addAttribute("dataListPage", new PageImpl<>(new ArrayList<>(), pageable, totalCount));
            model.addAttribute("currentPage", totalPage < page ? 1 : page);
            model.addAttribute("totalPage", totalPage);
            model.addAttribute("hasPrev", hasPrevious);
            model.addAttribute("hasNext", hasNext);
            model.addAttribute("type", type);
            model.addAttribute("aid", aid);
            return "opmanager/businessactivity/checkHistory";
        }
    }

    @RequestMapping(value = "downloadinfo.vpage", method = RequestMethod.GET)
    public void downloadInfo(HttpServletResponse response) {
        Long aid = getRequestLong("aid");
        String type = getRequestString("type");
        try {
            BusinessActivity.ActivityType activityType = BusinessActivity.ActivityType.valueOf(type);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (activityType == BusinessActivity.ActivityType.Pay) {
                // 付费的 trustee_order_record
                List<TrusteeOrderRecord> orderRecords = trusteeOrderServiceClient.loadTrusteeOrderByActivityId(aid);
                List<Map<String, Object>> payList = new ArrayList<>();
                CollectionUtils.splitList(orderRecords, 100)
                        .forEach(orders -> payList.addAll(orderRecordsToMap(orders)));
                dataList.addAll(payList);
            } else if (activityType == BusinessActivity.ActivityType.Reserve || activityType == BusinessActivity.ActivityType.Subscribe) {
                // 报名的或者预约的
                List<TrusteeReserveRecord> reserveRecords = trusteeOrderServiceClient.loadTrusteeReservesByActivityId(aid);
                dataList = reserveRecordsToMap(reserveRecords);
            }
            XSSFWorkbook xssfWorkbook = convertToHSS(dataList);
            String filename = "通用导流活动" + "-" + aid + "-" + type + "-" + DateUtils.dateToString(new Date()) + ".xls";
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
                logger.error("download seattle excel exception!");
            }
        }
    }

    private XSSFWorkbook convertToHSS(List<Map<String, Object>> dataList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("报名/支付时间");
        firstRow.createCell(1).setCellValue("学号");
        firstRow.createCell(2).setCellValue("学生姓名");
        firstRow.createCell(3).setCellValue("家长号");
        firstRow.createCell(4).setCellValue("称谓");
        firstRow.createCell(5).setCellValue("年级");
        firstRow.createCell(6).setCellValue("预约手机号");
        firstRow.createCell(7).setCellValue("学生手机号");
        firstRow.createCell(8).setCellValue("家长手机号");
        firstRow.createCell(9).setCellValue("状态");
        firstRow.createCell(10).setCellValue("用户备注");
        firstRow.createCell(11).setCellValue("来源");
        firstRow.createCell(12).setCellValue("外部流水号");
        firstRow.createCell(13).setCellValue("支付渠道");
        XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
        cellStyle.setWrapText(true);
        int rowNum = 1;
        for (Map<String, Object> record : dataList) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            xssfRow.createCell(0).setCellValue(DateUtils.dateToString((Date) record.get("successDate")));
            xssfRow.createCell(1).setCellValue(SafeConverter.toString(record.get("studentId")));
            xssfRow.createCell(2).setCellValue(SafeConverter.toString(record.get("studentName")));
            xssfRow.createCell(3).setCellValue(SafeConverter.toString(record.get("parentId")));
            xssfRow.createCell(4).setCellValue(SafeConverter.toString(record.get("callName")));
            xssfRow.createCell(5).setCellValue(SafeConverter.toString(record.get("clazzName")));
            xssfRow.createCell(6).setCellValue(SafeConverter.toString(record.get("mobile")));
            xssfRow.createCell(7).setCellValue(SafeConverter.toString(record.get("studentMobile")));
            xssfRow.createCell(8).setCellValue(SafeConverter.toString(record.get("parentMobile")));
            xssfRow.createCell(9).setCellValue(SafeConverter.toString(record.get("status")));
            xssfRow.createCell(10).setCellValue(SafeConverter.toString(record.get("remark")));
            xssfRow.createCell(11).setCellValue(SafeConverter.toString(record.get("track")));
            xssfRow.createCell(12).setCellValue(SafeConverter.toString(record.get("outTradeNo")));
            xssfRow.createCell(13).setCellValue(SafeConverter.toString(record.get("payChannel")));
            xssfSheet.setColumnWidth(0, 300 * 15);
            xssfSheet.setColumnWidth(6, 300 * 15);
            xssfSheet.setColumnWidth(7, 300 * 15);
            xssfSheet.setColumnWidth(8, 300 * 15);
            xssfSheet.setColumnWidth(10, 900 * 15);
            xssfSheet.setColumnWidth(12, 300 * 15);
        }
        return xssfWorkbook;
    }


    private List<Map<String, Object>> reserveRecordsToMap(List<TrusteeReserveRecord> reserveRecords) {
        if (CollectionUtils.isEmpty(reserveRecords)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (TrusteeReserveRecord reserveRecord : reserveRecords) {
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", reserveRecord.getStudentId());
            map.put("parentId", reserveRecord.getParentId());
            map.put("status", reserveRecord.getStatus());
            map.put("successDate", reserveRecord.getUpdateDatetime());
            map.put("mobile", reserveRecord.getSensitiveMobile());
            map.put("remark", "");
            map.put("track", reserveRecord.getTrack());
            map.put("outTradeNo", "");
            dataList.add(map);
        }
        return dataList;
    }

    @RequestMapping(value = "getMobile.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMobile() {
        MapMessage map = new MapMessage();
        String id = getRequestString("id");
        TrusteeOrderRecord orderRecord = trusteeOrderServiceClient.loadTrusteeOrder(id);
        Set<Long> studentIds = new HashSet<>();
        studentIds.add(orderRecord.getStudentId());
        Map<Long, UserAuthentication> studentAuthMap = userLoaderClient.loadUserAuthentications(studentIds);
        Set<Long> parentIds = new HashSet<>();
        parentIds.add(orderRecord.getParentId());
        Map<Long, UserAuthentication> parentAuthMap = userLoaderClient.loadUserAuthentications(parentIds);
        UserAuthentication studentAuth = studentAuthMap.get(orderRecord.getStudentId());
        UserAuthentication parentAuth = parentAuthMap.get(orderRecord.getParentId());
        if (studentAuth != null && studentAuth.isMobileAuthenticated()) {
            map.put("studentMobile", sensitiveUserDataServiceClient.showUserMobile(orderRecord.getStudentId(), "trustee-order-show", SafeConverter.toString(orderRecord.getStudentId())));
        }
        if (parentAuth != null && parentAuth.isMobileAuthenticated()) {
            map.put("parentMobile", sensitiveUserDataServiceClient.showUserMobile(orderRecord.getParentId(), "trustee-order-show", SafeConverter.toString(orderRecord.getStudentId())));
        }
        return map;
    }

    private List<Map<String, Object>> orderRecordsToMap(List<TrusteeOrderRecord> orderRecords) {
        if (CollectionUtils.isEmpty(orderRecords)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Set<Long> studentIds = orderRecords.stream().map(TrusteeOrderRecord::getStudentId).collect(Collectors.toSet());
        Set<Long> parentIds = orderRecords.stream().map(TrusteeOrderRecord::getParentId).collect(Collectors.toSet());
        Map<Long, StudentDetail> detailMap = studentLoaderClient.loadStudentDetails(studentIds);
        Map<Long, List<StudentParentRef>> refMap = studentLoaderClient.loadStudentParentRefs(studentIds);
//        Map<Long, UserAuthentication> studentAuthMap = userLoaderClient.loadUserAuthentications(studentIds);
//        Map<Long, UserAuthentication> parentAuthMap = userLoaderClient.loadUserAuthentications(parentIds);
        for (TrusteeOrderRecord orderRecord : orderRecords) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",orderRecord.getId());
            map.put("studentId", orderRecord.getStudentId());
            StudentDetail detail = detailMap.get(orderRecord.getStudentId());
            if (detail != null) {
                map.put("clazzName", detail.getClazz() == null ? "" : detail.getClazz().formalizeClazzName());
                map.put("studentName", detail.fetchRealname());
            }
            List<StudentParentRef> refList = refMap.get(orderRecord.getStudentId());
            if (CollectionUtils.isNotEmpty(refList)) {
                StudentParentRef ref = refList.stream().filter(r -> Objects.equals(r.getParentId(), orderRecord.getParentId())).findAny().orElse(null);
                if (ref != null) {
                    map.put("callName", ref.getCallName());
                }
            }
//            UserAuthentication studentAuth = studentAuthMap.get(orderRecord.getStudentId());
//            UserAuthentication parentAuth = parentAuthMap.get(orderRecord.getParentId());
//            if (studentAuth != null && studentAuth.isMobileAuthenticated()) {
//                map.put("studentMobile", sensitiveUserDataServiceClient.showUserMobile(orderRecord.getStudentId(), "trustee-order-show", SafeConverter.toString(orderRecord.getStudentId())));
//            }
//            if (parentAuth != null && parentAuth.isMobileAuthenticated()) {
//                map.put("parentMobile", sensitiveUserDataServiceClient.showUserMobile(orderRecord.getParentId(), "trustee-order-show", SafeConverter.toString(orderRecord.getStudentId())));
//            }
            map.put("parentId", orderRecord.getParentId());
            map.put("status", orderRecord.getStatus());
            map.put("successDate", orderRecord.getUpdateTime());
            map.put("remark", orderRecord.getRemark());
            map.put("track", orderRecord.getTrack());
            map.put("outTradeNo", orderRecord.getOutTradeNo());
            map.put("payChannel", orderRecord.getPayMethod());
            dataList.add(map);
        }
        return dataList;
    }

}
