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

package com.voxlearning.utopia.admin.controller.reward;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.YearRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.ZipUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.dao.CrmRewardOrderDao;
import com.voxlearning.utopia.admin.util.XssfUtils;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.entity.campaign.FlowPacketConvert;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.finance.client.FlowPacketConvertServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.mapper.IntegralInfo;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardLoaderClient;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.constant.RewardProductPriceUnit;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;
import com.voxlearning.utopia.service.zone.client.PersonalZoneServiceClient;
import com.voxlearning.utopia.temp.RewardRange;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by XiaoPeng.Yang on 14-8-1.
 */
@Controller
@RequestMapping("/reward/order")
@Slf4j
public class CrmRewardOrderController extends RewardAbstractController {

    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private CrmRewardOrderDao crmRewardOrderDao;
    @Inject private FlowPacketConvertServiceClient flowPacketConvertServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private RewardCenterClient rewardCenterClient;
    @Inject private RewardServiceClient rewardServiceClient;
    @Inject private PrivilegeServiceClient privilegeServiceClient;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject private PersonalZoneServiceClient personalZoneServiceClient;
    @Inject private NewRewardLoaderClient newRewardLoaderClient;

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @RequestMapping(value = "cancelOrder.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage cancelOrder() {
        Long orderId = getRequestLong("orderId");
        RewardOrder order = rewardLoaderClient.getRewardOrderLoader().loadRewardOrder(orderId);
        if (Objects.isNull(order)) {
            return MapMessage.errorMessage(String.format("订单:%s不存在！", orderId));

        }
        if ((newRewardLoaderClient.isSHIWU(NumberUtils.toLong(order.getProductCategory())) && !Objects.equals(order.getStatus(), RewardOrderStatus.SUBMIT.name()))
                || (Objects.equals(order.getProductType(), RewardProductType.JPZX_SHIWU.name()) && !Objects.equals(order.getStatus(), RewardOrderStatus.SUBMIT.name()))) {
            return MapMessage.errorMessage(String.format("已发货的实物订单不可取消！", orderId));
        }
        RewardProduct product = rewardLoaderClient.loadRewardProduct(order.getProductId());
        if (Objects.isNull(product)) {
            return MapMessage.errorMessage(String.format("商品不存在！", orderId));
        }

        MapMessage message = rewardServiceClient.deleteRewardOrder(order);
        if (!message.isSuccess()) {
            return MapMessage.errorMessage(String.format("删除订单失败！", orderId));
        }

        List<RewardCategory> categories = rewardLoaderClient.findRewardProductCategoriesByProductId(order.getProductId());

        for (RewardCategory category : categories) {
            if (Objects.equals(category.getCategoryCode(), "HEAD_WEAR")) {
                Privilege p = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(product.getRelateVirtualItemId());
                if (p != null) {
                    Date startDate = DayRange.current().previous().getStartDate();
                    privilegeServiceClient.getPrivilegeService().grantPrivilege(order.getBuyerId(), p, startDate);
                    personalZoneServiceClient.getPersonalZoneService().resetHeadWear(order.getBuyerId());
                }

            }
            if (Objects.equals(category.getCategoryCode(), "TOBY_WEAR")) {
                rewardCenterClient.cancelTobyDress(order.getProductId(), order.getBuyerId());
            }
        }
        return MapMessage.successMessage(String.format("取消订单成功！", orderId));
    }

    /**
     * 订单管理
     */
    @RequestMapping(value = "ordermanager.vpage", method = RequestMethod.GET)
    public String orderMgnIndex(Model model) {
        model.addAttribute("statusList", RewardOrderStatus.values());
        return "reward/order/ordermanager";
    }

    /**
     * 快递单管理
     */
    @RequestMapping(value = "logisticmanager.vpage", method = RequestMethod.GET)
    public String logisticManager(Model model) {
        return "reward/order/logisticmanager";
    }

    /**
     * 统计信息
     */
    @RequestMapping(value = "statinfo.vpage", method = RequestMethod.GET)
    public String statInfo(Model model) {
        //页面统计默认开始时间有寒暑假的逻辑
        String beginDate;
        String currentYear = String.valueOf(YearRange.current().getYear());
        final DateRange summerRange = new DateRange(DateUtils.stringToDate(currentYear + "-06-01 00:00:00"), DateUtils.stringToDate(currentYear + "-09-01 00:00:00"));
        final DateRange winterRange = new DateRange(DateUtils.stringToDate(currentYear + "-01-01 00:00:00"), DateUtils.stringToDate(currentYear + "-03-01 00:00:00"));
        if (summerRange.contains(new Date())) {
            beginDate = DateUtils.dateToString(summerRange.getStartDate(), "yyyyMM");
        } else if (winterRange.contains(new Date())) {
            beginDate = DateUtils.dateToString(winterRange.getStartDate(), "yyyyMM");
        } else {
            beginDate = DateUtils.dateToString(MonthRange.current().getStartDate(), "yyyyMM");
        }
        model.addAttribute("statusList", RewardOrderStatus.values());
        model.addAttribute("beginDate", beginDate);

        return "reward/order/statinfo";
    }

    @RequestMapping(value = "getstatinfo.vpage", method = RequestMethod.POST)
    String getStatInfo(Model model) {
        Long productId = getRequestLong("productId", 0L);
        String beginDate = getRequestParameter("beginDate", "");
        String endDate = getRequestParameter("endDate", "");
        model.addAttribute("results", loadStatInfo(productId, beginDate, endDate));
        return "reward/order/statinfochip";
    }

    @RequestMapping(value = "getstatinfo1.vpage", method = RequestMethod.POST)
    String getStatInfo1(Model model) {
        String productIdsStr = getRequestString("productIds");
        List<Long> productIds = Arrays.stream(productIdsStr.split(","))
                .filter(StringUtils::isNumeric)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        String beginDateStr = getRequestParameter("beginDate", "");
        String endDateStr = getRequestParameter("endDate", "");
        Date beginDate;
        Date endDate;
        final int DEFAULT_INTERVAL = 7;

        if (StringUtils.isEmpty(beginDateStr)) {
            beginDate = new Date();
            // 从当天的起始时间算起
            beginDate = DateUtils.truncate(beginDate, Calendar.DAY_OF_MONTH);
            beginDateStr = DateUtils.dateToString(beginDate);

            endDate = new Date(DateUtils.roundDateToDay235959InMillis(beginDate, DEFAULT_INTERVAL));
            endDateStr = DateUtils.dateToString(endDate);
        } else if (StringUtils.isEmpty(endDateStr)) {
            endDate = DateUtils.stringToDate(endDateStr);
            // 从一天结束的时间算起
            endDate = new Date(DateUtils.roundDateToDay235959InMillis(endDate, 0));
            endDateStr = DateUtils.dateToString(endDate);

            beginDate = DateUtils.addDays(endDate, DEFAULT_INTERVAL * -1);
            beginDate = DateUtils.truncate(beginDate, Calendar.DAY_OF_MONTH);
            beginDateStr = DateUtils.dateToString(beginDate);
        }

        String orderSatus = getRequestParameter("orderStatus", "");
        model.addAttribute("results", loadStatInfo(productIds, beginDateStr, endDateStr, orderSatus));

        return "reward/order/statinfochip";
    }

    @RequestMapping(value = "getcompleteorderstatinfo.vpage", method = RequestMethod.POST)
    String getCompleteOrderstatinfo(Model model) {
        String month = getRequestString("month");
        List<Map<String, Object>> dataList = loadCompleteOrderStatInfo(month);
        // 拼装数据
        Map<Long, Object> dataMap = new HashMap<>();
        for (Map<String, Object> map : dataList) {
            if (dataMap.containsKey(SafeConverter.toLong(map.get("pid")))) {
                Map<String, Object> statMap = (Map<String, Object>) dataMap.get(SafeConverter.toLong(map.get("pid")));
                String status = SafeConverter.toString(map.get("status"));
                RewardOrderStatus orderStatus = RewardOrderStatus.valueOf(status);
                if (orderStatus == RewardOrderStatus.DELIVER) {
                    statMap.put("deliverCount", map.get("total"));
                } else if (orderStatus == RewardOrderStatus.EXCEPTION) {
                    statMap.put("exceptionCount", map.get("total"));
                } else if (orderStatus == RewardOrderStatus.PREPARE) {
                    statMap.put("prepareCount", map.get("total"));
                }
            } else {
                Map<String, Object> statMap = new HashMap<>();
                statMap.put("pid", map.get("pid"));
                statMap.put("pname", map.get("pname"));
                statMap.put("sname", map.get("sname"));
                String status = SafeConverter.toString(map.get("status"));
                RewardOrderStatus orderStatus = RewardOrderStatus.valueOf(status);
                if (orderStatus == RewardOrderStatus.DELIVER) {
                    statMap.put("deliverCount", map.get("total"));
                } else if (orderStatus == RewardOrderStatus.EXCEPTION) {
                    statMap.put("exceptionCount", map.get("total"));
                } else if (orderStatus == RewardOrderStatus.PREPARE) {
                    statMap.put("prepareCount", map.get("total"));
                }
                dataMap.put(SafeConverter.toLong(map.get("pid")), statMap);
            }
        }
        model.addAttribute("results", dataMap.values());
        return "reward/order/costatinfochip";
    }

    @RequestMapping(value = "getorderlist.vpage", method = RequestMethod.POST)
    public String getOrderList(Model model) {

        Integer pageNumber = getRequestInt("pageNumber", 1);
        Long userId = getRequestLong("userId", 0L);
        String status = getRequestString("status");
        String startDate = getRequestParameter("startDate", "");
        String endDate = getRequestParameter("endDate", "");
        Pageable pageable = new PageRequest(pageNumber - 1, 10);

        String tplName = "reward/order/orderlistchip";
        model.addAttribute("pageNumber", pageNumber);

        Date startTime;
        if (!StringUtils.isEmpty(startDate))
            startTime = DateUtils.stringToDate(startDate, "yyyy-MM-dd");
        else {
            model.addAttribute("error", "起始时间不能为空!");
            return tplName;
        }

        Date endTime;
        if (!StringUtils.isEmpty(endDate))
            endTime = DateUtils.stringToDate(endDate, "yyyy-MM-dd");
        else {
            model.addAttribute("error", "结束时间不能为空!");
            return tplName;
        }

        long dayDiff = DateUtils.dayDiff(endTime, startTime);
        if (dayDiff > 30) {
            model.addAttribute("error", "时间跨度不能超过30天");
            return tplName;
        }

        Page<RewardOrder> orderPage = crmRewardOrderDao.find(userId, startTime, endTime, status, pageable);
        model.addAttribute("orderPage", orderPage);

        return tplName;
    }

    @RequestMapping(value = "getlogisticlist.vpage", method = RequestMethod.POST)
    public String getLogisticList(Model model) {
        Integer pageNumber = getRequestInt("pageNumber", 1);
        Long logisticId = getRequestLong("logisticId", 0L);
        String logisticNo = getRequestString("logisticNo");
        String month = getRequestParameter("month", "");
        String isBack = getRequestString("isBack");
        Pageable pageable = new PageRequest(pageNumber - 1, 10);

        Page<RewardLogistics> logisticsPage = crmRewardService
                .getRewardLogisticPage(pageable, logisticId, logisticNo, month, isBack)
                .getUninterruptibly();

        model.addAttribute("logisticsPage", logisticsPage);
        model.addAttribute("pageNumber", pageNumber);
        return "reward/order/logisticlistchip";
    }

    @RequestMapping(value = "getactivitystatinfo.vpage", method = RequestMethod.POST)
    @SneakyThrows
    public String getActivityStatInfo(Model model) {
        String tplName = "reward/order/activitystatinfochip";

        Long activityId = getRequestLong("activityId");
        RewardActivity activity = rewardLoaderClient.loadRewardActivity(activityId);
        if (activity == null) {
            model.addAttribute("errorMsg", "活动不存在!");
            return tplName;
        }

        String startDateStr = getRequestString("startDate");
        if (StringUtils.isEmpty(startDateStr)) {
            model.addAttribute("errorMsg", "查询起始日期不能为空!");
            return tplName;
        }

        Date startDate = DateUtils.stringToDate(startDateStr, DateUtils.FORMAT_SQL_DATE);
        Date startTime = DateUtils.getDayStart(startDate);
        Date endTime = DateUtils.getDayEnd(startDate);

        List<Map<String, Object>> result = RoutingPolicyExecutorBuilder.getInstance()
                .<List<Map<String, Object>>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    String sql =
                            " select ACTIVITY_ID as activityId,price as priceUnit,count(price) as totalNums " +
                                    " from VOX_REWARD_ACTIVITY_RECORD " +
                                    " where ACTIVITY_ID = ? and " +
                                    " CREATE_DATETIME >= ? and CREATE_DATETIME <= ? " +
                                    " group by price";

                    return utopiaSqlReward.withSql(sql).useParamsArgs(activityId, startTime, endTime).queryAll();
                }).execute();

        model.addAttribute("results", result);
        return tplName;
    }

    @RequestMapping(value = "getundoneflowpack.vpage", method = RequestMethod.GET)
    @SneakyThrows
    public String getUndoneFlowPack(Model model) {
        String tplName = "reward/order/flowpackhistorychip";

        List<FlowPacketConvert> result = flowPacketConvertServiceClient.getFlowPacketConvertService()
                .findTobeCheckResultList()
                .getUninterruptibly();

        model.addAttribute("results", result);
        return tplName;
    }

    @RequestMapping(value = "getflowpackhistory.vpage", method = RequestMethod.GET)
    @SneakyThrows
    public String getFlowPackHistory(Model model) {
        String tplName = "reward/order/flowpackhistorychip";

        Long teacherId = getRequestLong("teacherId");

        List<FlowPacketConvert> result = flowPacketConvertServiceClient.getFlowPacketConvertService()
                .findUserChargingHistory(teacherId)
                .getUninterruptibly()
                .stream()
                .sorted((p1, p2) -> p2.getUpdateDatetime().compareTo(p1.getUpdateDatetime()))
                .collect(Collectors.toList());

        model.addAttribute("results", result);
        return tplName;
    }

//    @RequestMapping(value = "downloadauditingorder.vpage", method = RequestMethod.GET)
//    public void downloadAuditingOrder(HttpServletResponse response) {
//        Map<String, Object> param = new HashMap<>();
//        param.put("status", RewardOrderStatus.IN_AUDIT.name());
//        List<RewardOrder> exportList = rewardManagementClient.loadExportRewardOrdersByParameters(param);
//
//        XSSFWorkbook xssfWorkbook = convertToAuditingXSSF(exportList);
//
//        String filename = "奖品中心审核中订单" + "-" + DateUtils.dateToString(new Date()) + ".xlsx";
//        try {
//            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            xssfWorkbook.write(outStream);
//            outStream.flush();
//            HttpRequestContextUtils.currentRequestContext().downloadFile(
//                    filename,
//                    "application/vnd.ms-excel",
//                    outStream.toByteArray());
//        } catch (IOException ignored) {
//            try {
//                response.getWriter().write("不能下载");
//                response.sendError(HttpServletResponse.SC_FORBIDDEN);
//            } catch (IOException e) {
//                log.error("download auditing order exception!");
//            }
//        }
//    }

    @RequestMapping(value = "downloadcompleteorderstatinfo.vpage", method = RequestMethod.GET)
    public void downloadCompleteOrderStatInfo(HttpServletResponse response) {
        String month = getRequestString("month");
        List<Map<String, Object>> data = loadCompleteOrderStatInfo(month);
        HSSFWorkbook hssfWorkbook = convertToCostatHSSF(data);
        String filename = "奖品中心发货单统计信息" + "-" + DateUtils.dateToString(new Date()) + ".xls";
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
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
                log.error("download auditing order exception!");
            }
        }
    }

    @RequestMapping(value = "downloadstatinfo.vpage", method = RequestMethod.GET)
    public void downloadStatInfo(HttpServletResponse response) {

        Integer queryMode = getRequestInt("queryMode", 0);
        HSSFWorkbook hssfWorkbook = null;
        if (queryMode == 0) {
            Long productId = getRequestLong("productId", 0L);
            String beginDate = getRequestParameter("beginDate", "");
            String endDate = getRequestParameter("endDate", "");
            List<Map<String, Object>> collection = loadStatInfo(productId, beginDate, endDate);
            hssfWorkbook = convertToStatHSS(collection);
        } else if (queryMode == 1) {
            String productIdsStr = getRequestString("productIds");
            List<Long> productIds = Arrays.stream(productIdsStr.split(","))
                    .filter(StringUtils::isNumeric)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            String beginDate = getRequestParameter("beginDate", "");
            String endDate = getRequestParameter("endDate", "");
            String orderStatus = getRequestParameter("orderStatus", "");
            hssfWorkbook = convertToStatHSS(loadStatInfo(productIds, beginDate, endDate, orderStatus));
        }

        String filename = "奖品中心统计信息" + "-" + DateUtils.dateToString(new Date()) + ".xls";
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
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
                log.error("download auditing order exception!");
            }
        }
    }

    @RequestMapping(value = "downloaddeliverinfo.vpage", method = RequestMethod.GET)
    public void downloadDeliverInfo(HttpServletResponse response) {
        // 都改成走从库的配置
        List<Integer> cityCodes = RoutingPolicyExecutorBuilder.getInstance()
                .<List<Integer>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    String citySql = "SELECT DISTINCT CITY_CODE from VOX_REWARD_COMPLETE_ORDER WHERE CREATE_DATETIME > ? AND DISABLED = FALSE;";
                    return utopiaSqlReward.withSql(citySql)
                            .useParamsArgs(MonthRange.current().getStartDate())
                            .queryColumnValues(Integer.class);
                })
                .execute();

        try {
            Map<String, Integer> regionNameIndexMap = new HashMap<>();
            Map<Long, RewardProduct> productMap = rewardLoaderClient.loadRewardProductMap();

            String zipName = "奖品中心发货单-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE) + ".zip";
            ZipArchiveOutputStream zipOutputStream = getZipOutputStreamForDownloading(zipName);
            for (Integer cityCode : cityCodes) {

                List<RewardCompleteOrder> completeOrders = RoutingPolicyExecutorBuilder.getInstance()
                        .<List<RewardCompleteOrder>>newExecutor()
                        .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                        .callback(() -> {

                            String sql = "SELECT ID,BUYER_ID, " +
                                    "BUYER_NAME, " +
                                    "PRODUCT_ID," +
                                    "PRODUCT_NAME, " +
                                    "PROVINCE_NAME, " +
                                    "CITY_NAME, " +
                                    "COUNTY_NAME, " +
                                    "SCHOOL_NAME, " +
                                    "SCHOOL_ID, " +
                                    "CLAZZ_NAME, " +
                                    "DETAIL_ADDRESS, " +
                                    "POST_CODE, " +
                                    "LOGISTIC_TYPE, " +
                                    "RECEIVER_NAME, " +
                                    "RECEIVER_ID, " +
                                    "PHONE, " +
                                    "SKU_NAME, " +
                                    "QUANTITY, " +
                                    "UNIT, " +
                                    "LOGISTICS_ID" +
                                    " FROM VOX_REWARD_COMPLETE_ORDER WHERE CITY_CODE=? AND CREATE_DATETIME > ? AND DISABLED = FALSE;";

                            return utopiaSqlReward.withSql(sql).useParamsArgs(
                                    cityCode,
                                    MonthRange.current().getStartDate()).queryAll((rs, rowNum) -> {

                                RewardCompleteOrder order = new RewardCompleteOrder();
                                order.setId(rs.getLong("ID"));
                                order.setBuyerId(rs.getLong("BUYER_ID"));
                                order.setBuyerName(rs.getString("BUYER_NAME"));

                                Long productId = SafeConverter.toLong(rs.getString("PRODUCT_ID"));
                                // 这里不从库里面取，而是取最新的产品名字
                                RewardProduct product = productMap.get(productId);
                                if (product != null)
                                    order.setProductName(product.getProductName());
                                else
                                    order.setProductName(rs.getString("PRODUCT_NAME"));

                                order.setProvinceName(rs.getString("PROVINCE_NAME"));
                                order.setCityName(rs.getString("CITY_NAME"));
                                order.setCountyName(rs.getString("COUNTY_NAME"));
                                order.setSchoolName(rs.getString("SCHOOL_NAME"));
                                order.setSchoolId(rs.getLong("SCHOOL_ID"));
                                order.setClazzName(rs.getString("CLAZZ_NAME"));
                                order.setDetailAddress(rs.getString("DETAIL_ADDRESS"));
                                order.setPostCode(rs.getString("POST_CODE"));
                                order.setLogisticType(rs.getString("LOGISTIC_TYPE"));
                                order.setReceiverName(rs.getString("RECEIVER_NAME"));
                                order.setReceiverId(rs.getLong("RECEIVER_ID"));
                                order.setSensitivePhone(rs.getString("PHONE"));
                                order.setSkuName(rs.getString("SKU_NAME"));
                                order.setQuantity(rs.getInt("QUANTITY"));
                                order.setUnit(rs.getString("UNIT"));
                                order.setLogisticsId(rs.getString("LOGISTICS_ID"));
                                return order;

                            });
                        })
                        .execute();

                String regionName = "未知";
                String cityName = StringUtils.trim(MiscUtils.firstElement(completeOrders).getCityName());
                String proviceName = StringUtils.trim(MiscUtils.firstElement(completeOrders).getProvinceName());
                if (StringUtils.isNotBlank(cityName)) {
                    regionName = proviceName + "-" + cityName;
                }

                Integer index = regionNameIndexMap.get(regionName);
                if (index == null) {
                    regionNameIndexMap.put(regionName, 1);
                } else {
                    index = index + 1;
                    regionName = regionName + "-" + index;
                }

                String filename = "奖品中心发货单" + "-" + regionName + ".xlsx";
                XSSFWorkbook xssfWorkbook = convertToDeliverHSS(completeOrders);
                @Cleanup final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                xssfWorkbook.write(outStream);
                outStream.flush();
                ZipUtils.addZipEntry(zipOutputStream, filename, outStream.toByteArray());
            }
            zipOutputStream.flush();
            zipOutputStream.close();
        } catch (IOException ignored) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                log.error("download deliver order exception!");
            }
        }
    }

    @RequestMapping(value = "downloadlogistics.vpage", method = RequestMethod.GET)
    public void downLoadLogistics(HttpServletResponse response) {
        String month = DateUtils.dateToString(new Date(), "yyyyMM");
        // 这里面要过滤失效的快递单
        List<RewardLogistics> data = crmRewardService.$findRewardLogisticsList(month)
                .stream()
                .filter(l -> BooleanUtils.isFalse(l.getDisabled()))
                .collect(Collectors.toList());

        XSSFWorkbook xssfWorkbook = convertToLogisticHSS(data);
        String filename = "奖品中心快递单" + "-" + DateUtils.dateToString(new Date()) + ".xls";
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
                log.error("download logistic exception!");
            }
        }
    }

    @RequestMapping(value = "updateuserorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUserOrder() {
        String userIds = getRequestParameter("userIds", "");
        String reason = getRequestParameter("reason", "");
        String status = getRequestParameter("status", "");
        if (StringUtils.isBlank(userIds) || StringUtils.isBlank(reason) || StringUtils.isBlank(status)) {
            return MapMessage.errorMessage("参数不全");
        }
        RewardOrderStatus orderStatus = RewardOrderStatus.valueOf(status);
        String[] idArray = userIds.trim().split("\n");
        MapMessage message;
        try {
            message = atomicLockManager.wrapAtomic(rewardServiceClient)
                    .keys(getCurrentAdminUser().getFakeUserId())
                    .keyPrefix("AdminBatchUpdateUserOrder")
                    .proxy()
                    .batchUpdateUserOrder(idArray, reason, orderStatus);
            addAdminLog("管理员" + getCurrentAdminUser().getAdminUserName() + "批量修改了用户订单， 修改状态为：" + orderStatus.getDescription());
        } catch (DuplicatedOperationException ex) {
            message = MapMessage.errorMessage("正在执行，不要重复点击");
        } catch (Exception ex) {
            message = MapMessage.errorMessage("执行失败");
        }
        return message;
    }

    @RequestMapping(value = "getusercount.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage getUserCount() {
        // 改为实时查询，key值拼上日期
        Date yesterday = DateUtils.calculateDateDay(new Date(), -1);
        String yesterdayStr = DateUtils.dateToString(yesterday, DateUtils.FORMAT_SQL_DATE);

        String summaryKey = RewardOrderSummary.getUserCountSummaryKey() + "_" + yesterdayStr;
        CacheObject<String> cacheObject = CacheSystem.CBS.getCache("unflushable").get(summaryKey);
        if (cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue())) {
            return MapMessage.successMessage(cacheObject.getValue());
        } else {
            //return MapMessage.successMessage("数据正在生成，请明日再试");
            Date beginDate = MonthRange.current().getStartDate();
            if (!MonthRange.current().contains(yesterday)) {
                beginDate = MonthRange.current().previous().getStartDate();
            }

            Date ucBeginDate = beginDate;
            // 是假期
            if (RewardRange.isVacation()) {
                final DateRange summerRange = RewardRange.getSummerRange();
                final DateRange winterRange = RewardRange.getWinterRange();
                if (summerRange.contains(new Date())) {
                    ucBeginDate = summerRange.getStartDate();
                } else if (winterRange.contains(new Date())) {
                    ucBeginDate = winterRange.getStartDate();
                }
            }

            // 查询参数
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("beginDate", ucBeginDate);
            paramMap.put("endDate", DateUtils.stringToDate(yesterdayStr + " 23:59:59"));


            List<Map<String, Object>> userCountResult = RoutingPolicyExecutorBuilder.getInstance()
                    .<List<Map<String, Object>>>newExecutor()
                    .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                    .callback(() -> {
                        String queryStr = "SELECT " +
                                "o.PRODUCT_TYPE AS P_TYPE, o.UNIT AS UNIT, COUNT(DISTINCT o.BUYER_ID) AS COUNT " +
                                "FROM " +
                                "VOX_REWARD_ORDER o " +
                                "WHERE " +
                                "o.DISABLED = FALSE " +
                                "AND o.CREATE_DATETIME>=:beginDate " +
                                "AND o.CREATE_DATETIME<=:endDate " +
                                "GROUP BY o.PRODUCT_TYPE , o.UNIT";

                        return utopiaSqlReward.withSql(queryStr).useParams(paramMap).queryAll();
                    }).execute();

            /**
             *  由于新老后台改版共存,订单表 PRODUCT_TYPE 字段存的既有老的 JPZX_SHIWU(RewardProductType.java) 又有新的 1 (OneLevelCategoryType.java)
             *  这里把实物和虚拟分类进行合并, 等后台和下单接口全部统一再改这块
             */
            Collection<UserCount> userCountList = hackUserCountList(userCountResult);

            StringBuilder cacheStr = new StringBuilder();
            cacheStr.append("统计时间段:").append(DateUtils.dateToString(ucBeginDate, DateUtils.FORMAT_SQL_DATE)).append("至").append(yesterdayStr).append("\n\n");
            userCountList.forEach(r -> {
                RewardProductType productType = RewardProductType.parse(r.getType());
                String typeName = productType == null ? "" : productType.getDescription();
                if (StringUtils.isEmpty(typeName)) return;

                String unit = r.getUnit();
                Long count = SafeConverter.toLong(r.getCount());

                if (RewardProductPriceUnit.学豆.name().equals(unit)) {
                    cacheStr.append(" 学生人数(").append(typeName).append("):").append(count);
                } else if (RewardProductPriceUnit.园丁豆.name().equals(unit)) {
                    cacheStr.append(" 小学老师人数(").append(typeName).append(")：").append(count);
                } else {
                    cacheStr.append(" 中学老师人数(").append(typeName).append(")：").append(count);
                }
            });

            CacheSystem.CBS.getCache("unflushable").set(summaryKey,
                    DateUtils.getCurrentToDayEndSecond(), cacheStr.toString());

            return MapMessage.successMessage(cacheStr.toString());
        }
    }

    @NotNull
    private Collection<UserCount> hackUserCountList(List<Map<String, Object>> userCountResult) {
        List<UserCount> userCountList = userCountResult.stream().map(map -> {
            UserCount userCount = new UserCount();
            String pType = MapUtils.getString(map, "P_TYPE");

            // 1 和 JPZX_SHIWU 是实物,其他都归为虚拟
            if (Objects.equals(pType, "JPZX_SHIWU") || Objects.equals(pType, "1")) {
                pType = "JPZX_SHIWU";
            } else {
                pType = "JPZX_TIYAN";
            }
            String unit = MapUtils.getString(map, "UNIT");
            userCount.setType(pType);
            userCount.setUnit(unit);
            userCount.setCount(MapUtils.getInteger(map, "COUNT"));
            return userCount;
        }).collect(Collectors.toList());

        Map<UserCount, UserCount> map = new HashMap<>();
        for (UserCount userCount : userCountList) {
            UserCount mapItem = map.get(userCount);
            if (mapItem == null) {
                map.put(userCount, userCount);
            } else {
                mapItem.setCount(mapItem.getCount() + userCount.getCount());
            }
        }

        return map.values();
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode(of = {"type", "unit"})
    private static class UserCount {
        private String type;    // 商品类型
        private String unit;    // 学豆类型
        private Integer count;  // 数量
    }

    @RequestMapping(value = "importlogisticexcel.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importLogisticExcel() {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return MapMessage.errorMessage("上传请求失败");
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile file = multipartRequest.getFile("sourceFile");
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = sourceFile");
                return MapMessage.errorMessage("文件找不到");
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return MapMessage.errorMessage("错误的文件类型");
            }
            @Cleanup InputStream in = file.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            return dealImportLogistic(workbook);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return MapMessage.errorMessage("请求异常，请联系管理员");
        }
    }

    private MapMessage dealImportLogistic(XSSFWorkbook workbook) {
        List<String> errorList = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            return MapMessage.errorMessage("上传失败，请确认数据是否存在");
        }
        int totalRow = sheet.getLastRowNum();
        if (totalRow > 20000) {
            return MapMessage.errorMessage("每次最多支持2万条数据上传，请分批次上传");
        }

        // 上个月的时间范围
        MonthRange lastMonthRange = MonthRange.current().previous();

        int rows = 1;
        int successRow = 0;
        while (true) {
            try {
                XSSFRow row = sheet.getRow(rows++);
                if (row == null) {
                    break;
                }

                boolean delivered = false;

                Long logisticId = XssfUtils.getLongCellValue(row.getCell(0));
                if (logisticId == null) {
                    errorList.add((rows) + "行快递单ID为空。");
                    continue;
                }
                RewardLogistics logistics = crmRewardService.$loadRewardLogistics(logisticId);
                if (logistics == null) {
                    errorList.add((rows) + "行快递单不存在。");
                    continue;
                }
                String companyName = XssfUtils.getStringCellValue(row.getCell(1));
                if (StringUtils.isBlank(companyName)) {
                    errorList.add((rows) + "行快递公司为空。");
                    continue;
                }
                logistics.setCompanyName(companyName);
                String logisticNo = XssfUtils.getStringCellValue(row.getCell(2));
                if (StringUtils.isBlank(logisticNo)) {
                    errorList.add((rows) + "行物流单号为空。");
                    continue;
                }

                // 如果是物流编码为空，第一次导入的情况，置上发货时间
                if (StringUtils.isEmpty(logistics.getLogisticNo())) {
                    delivered = true;
                    logistics.setDeliveredTime(new Date());
                }

                logistics.setLogisticNo(logisticNo);

                logistics.setIsBack(true);

                Double price = XssfUtils.getDoubleCellValue(row.getCell(6));
                logistics.setPrice(price);

                String userName = XssfUtils.getStringCellValue(row.getCell(7));
                if (StringUtils.isBlank(userName)) {
                    errorList.add((rows) + "行收货人姓名不存在");
                    continue;
                }
                logistics.setReceiverName(userName);

                String phone = XssfUtils.getStringCellValue(row.getCell(8));
                if (StringUtils.isBlank(phone)) {
                    errorList.add((rows) + "行收货人电话不存在");
                    continue;
                }
                // 电话加密
                logistics.setSensitivePhone(sensitiveUserDataServiceClient.encodeMobile(phone));

                String address = XssfUtils.getStringCellValue(row.getCell(13));
                if (StringUtils.isBlank(address)) {
                    errorList.add((rows) + "行收货人地址不存在");
                    continue;
                }
                logistics.setDetailAddress(address);
                logistics.setUpdateDatetime(new Date());
                // 用户订单信息异常的快递单不允许导入
                List<RewardOrder> orderList = rewardManagementClient.loadRewardOrderByLogisticId(logisticId);
                if (CollectionUtils.isEmpty(orderList)) {
                    errorList.add((rows) + "行没有关联订单存在");
                    continue;
                }
                // 更新快递单信息
                crmRewardService.$upsertRewardLogistics(logistics);

                // 导回的重新按照订单处理 只修改配货中的状态
                for (RewardOrder order : orderList) {
                    if (StringUtils.equals(order.getStatus(), RewardOrderStatus.PREPARE.name())) {
                        // 更新订单为已发货
                        rewardManagementClient.updateRewardOrderStatus(order.getId(), "管理员" + getCurrentAdminUser().getAdminUserName() + "导回快递单", RewardOrderStatus.DELIVER);
                        // 更新发货单为已发货
                        rewardManagementClient.updateRewardCompleteOrderStatus(order.getCompleteId(), RewardOrderStatus.DELIVER);
                    }
                }

                // 如果是发货的情况，则发通知提醒
                // 暂时只针对学生快递发提醒
                if (delivered && logistics.getType() == RewardLogistics.Type.STUDENT) {
                    Long receiverId = logistics.getReceiverId();
                    TeacherDetail td = teacherLoaderClient.loadTeacherDetail(receiverId);

                    String unit = "园丁豆";
                    if (td.isJuniorTeacher())
                        unit = "学豆";

                    IntegralInfo integralInfo = new IntegralInfo();
                    integralInfo.setUserId(td.getId());
                    integralInfo.setIntegralType(IntegralType.REWARD_COLLECTION_REWARD.getType());
                    integralInfo.setIntegral(500);
                    integralInfo.setComment("奖品代收货老师" + unit + "奖励");
                    integralInfo.setUniqueKey("LOGISTICS_ID_" + logistics.getId());
                    boolean duplicate = integralServiceClient.checkDuplicate(integralInfo);
                    if (!duplicate) {
                        MapMessage mapMessage = integralServiceClient.reward(integralInfo);
                        if (!mapMessage.isSuccess()) {
                            throw new RuntimeException("send teacher integral error. errorMessage: " + mapMessage.getInfo());
                        }
                    }

                    String msgNoticePart = "亲爱的老师，您已成为代收本校学生奖品的幸运老师。上月您学生兑换的奖品已寄出，"+ unit +"奖励也一并发放了, 请注意接收哦。物流公司：" + logistics.getCompanyName() + "，"
                            + "快递单号：" + logistics.getLogisticNo();
                    // 发pc端信息
                    teacherLoaderClient.sendTeacherMessage(receiverId, msgNoticePart);

                    // 发弹窗
                    userPopupServiceClient.createPopup(receiverId)
                            .content(msgNoticePart)
                            .type(PopupType.DEFAULT_AD)
                            .category(PopupCategory.LOWER_RIGHT)
                            .create();

                    // 发app消息
                    AppMessage msg = new AppMessage();
                    msg.setUserId(receiverId);
                    msg.setMessageType(TeacherMessageType.ACTIVIY.getType());
                    msg.setContent(msgNoticePart);
                    msg.setTitle("代收学生奖品已寄出");
                    msg.setCreateTime(new Date().getTime());
                    // 点击跳领取奖励页面
                   // msg.setLinkUrl("/view/mobile/teacher/collectreward?logisticId=" + logisticId.toString());
                   //  msg.setLinkType(1);
                    Long mainId = teacherLoaderClient.loadMainTeacherId(receiverId);
                    if (mainId != null && mainId > 0L) {
                        msg.setUserId(mainId);
                    }

                    messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);

                    // 发push
                    Map<String, Object> jpushExtInfo = new HashMap<>();
                    jpushExtInfo.put("teacherId", "");
                    jpushExtInfo.put("url", "");
                    jpushExtInfo.put("tag", TeacherMessageType.ACTIVIY.name());
                    jpushExtInfo.put("shareContent", "");
                    jpushExtInfo.put("shareUrl", "");

                    String jpushContent = "学生兑换的奖品已发出，快递公司：" + logistics.getCompanyName() +
                            "，物流单号:" + logistics.getLogisticNo() + "，请注意查收哦。";

                    appMessageServiceClient.sendAppJpushMessageByIds(
                            jpushContent,
                            AppMessageUtils.getMessageSource("17Teacher", td),
                            Collections.singletonList(logistics.getReceiverId()),
                            jpushExtInfo);

                    Set<Long> distributeTeacherIds = new HashSet<>();
                    Set<Long> sentStuIds = new HashSet<>();
                    // 给此单包括的所有学生发通知
                    List<RewardCompleteOrder> stuOrders = crmRewardService.findCompleteOrderByLogisticsId(logisticId);
                    stuOrders.forEach(o -> {
                        // 同一个学生只用发一次
                        if(sentStuIds.contains(o.getBuyerId()))
                            return;

                        String notifyMsg = String.format(
                                "你兑换的奖品已经寄出啦~学校代收奖品老师是%s，班内分发奖品老师是%s，可找老师领取奖品哦。物流公司：%s，快递单号：%s",
                                logistics.getReceiverName(),
                                o.getReceiverName(),
                                logistics.getCompanyName(),
                                logistics.getLogisticNo());

                        messageCommandServiceClient.getMessageCommandService().sendUserMessage(o.getBuyerId(),notifyMsg);

                        AppMessage appMessage = new AppMessage();
                        appMessage.setUserId(o.getBuyerId());
                        appMessage.setContent(notifyMsg);
                        appMessage.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType());
                        appMessage.setTitle("兑换的奖品已经寄出啦");

                        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);
                        sentStuIds.add(o.getBuyerId());

                        // 不能重复发
                        if(!distributeTeacherIds.contains(o.getReceiverId())){
                            String tNotifyMsg = String.format(
                                    "您班内学生兑换奖品已经寄出啦~本校代收奖品老师是%s，希望您帮忙将奖品分发给本班同学们，感谢您的辛勤付出。物流公司：%s，快递单号：%s 。",
                                    logistics.getReceiverName(),
                                    logistics.getCompanyName(),
                                    logistics.getLogisticNo());

                            teacherLoaderClient.sendTeacherMessage(o.getReceiverId(), tNotifyMsg);

                            AppMessage tMsg = new AppMessage();
                            tMsg.setUserId(o.getReceiverId());
                            tMsg.setMessageType(TeacherMessageType.ACTIVIY.getType());
                            tMsg.setContent(tNotifyMsg);
                            tMsg.setTitle("您班内学生兑换奖品已寄出");
                            tMsg.setCreateTime(new Date().getTime());
                            // 点击跳领取奖励页面

                            Long mainTid = teacherLoaderClient.loadMainTeacherId(o.getReceiverId());
                            if (mainTid != null && mainTid > 0L) {
                                tMsg.setUserId(mainTid);
                            }

                            messageCommandServiceClient.getMessageCommandService().createAppMessage(tMsg);

                            distributeTeacherIds.add(o.getReceiverId());
                        }
                    });
                }

                successRow++;
            } catch (Exception ex) {
                errorList.add((rows) + "行添加失败");
                logger.error("read excel failed", ex);
            }
        }
        return MapMessage.successMessage().add("errorList", errorList).add("successRow", successRow);
    }


    private HSSFWorkbook convertToCostatHSSF(List<Map<String, Object>> list) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow firstRow = hssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("奖品名称");
        firstRow.createCell(1).setCellValue("单品名称");
        firstRow.createCell(2).setCellValue("总兑换数量");

        int rowNum = 1;
        for (Map<String, Object> object : list) {
            HSSFRow hssfRow = hssfSheet.createRow(rowNum++);
            hssfRow.setHeightInPoints(20);
            hssfRow.createCell(0).setCellValue(object.get("pname").toString());
            hssfRow.createCell(1).setCellValue(object.get("sname").toString());
            hssfRow.createCell(2).setCellValue(object.get("total").toString());
        }
        //1-8行的列宽为256像素 15在这里表示一个像素
        for (int i = 0; i < 9; i++) {
            hssfSheet.setColumnWidth(i, 400 * 15);
        }
        return hssfWorkbook;
    }

    private HSSFWorkbook convertToStatHSS(List<Map<String, Object>> collection) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow firstRow = hssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("奖品名称");
        firstRow.createCell(1).setCellValue("奖品类型");
        firstRow.createCell(2).setCellValue("单品名称");
        firstRow.createCell(3).setCellValue("总兑换数量");
        firstRow.createCell(4).setCellValue("老师兑换数量");
        firstRow.createCell(5).setCellValue("（园丁豆）总价");
        firstRow.createCell(6).setCellValue("学生兑换数量");
        firstRow.createCell(7).setCellValue("（学豆）总价");
        firstRow.createCell(8).setCellValue("中学老师兑换数量");
        firstRow.createCell(9).setCellValue("（学豆）总价");
        firstRow.createCell(10).setCellValue("总学豆");

        int rowNum = 1;
        RewardProductType productType;
        double total = 0;
        for (Object object : collection) {
            Map<String, Object> data = (Map) object;
            HSSFRow hssfRow = hssfSheet.createRow(rowNum++);
            hssfRow.setHeightInPoints(20);
            hssfRow.createCell(0).setCellValue(data.get("productName").toString());

            productType = RewardProductType.parse(ConversionUtils.toString(data.get("productType")));
            hssfRow.createCell(1).setCellValue(productType == null ? "未知" : productType.getDescription());

            hssfRow.createCell(2).setCellValue(data.get("skuName").toString());
            hssfRow.createCell(3).setCellValue(data.get("totalCount").toString());
            hssfRow.createCell(4).setCellValue(ConversionUtils.toString(data.get("teacherQuantity"), ""));
            hssfRow.createCell(5).setCellValue(ConversionUtils.toString(data.get("teacherTotalPrice"), ""));
            hssfRow.createCell(6).setCellValue(ConversionUtils.toString(data.get("studentQuantity"), ""));
            hssfRow.createCell(7).setCellValue(ConversionUtils.toString(data.get("studentTotalPrice"), ""));
            hssfRow.createCell(8).setCellValue(ConversionUtils.toString(data.get("juniorTeacherQuantity"), ""));
            hssfRow.createCell(9).setCellValue(ConversionUtils.toString(data.get("juniorTeacherTotalPrice"), ""));

            total = SafeConverter.toDouble(hssfRow.getCell(5).getStringCellValue()) * 10
                    + SafeConverter.toDouble(hssfRow.getCell(7).getStringCellValue())
                    + SafeConverter.toDouble(hssfRow.getCell(9).getStringCellValue());

            hssfRow.createCell(10).setCellValue(total);
        }
        //1-8行的列宽为256像素 15在这里表示一个像素
        for (int i = 0; i < 9; i++) {
            hssfSheet.setColumnWidth(i, 400 * 15);
        }
        return hssfWorkbook;
    }

    private XSSFWorkbook convertToLogisticHSS(List<RewardLogistics> data) {
        List<Long> schoolIdList = data.stream().filter(e -> e.getSchoolId() != null).map(RewardLogistics::getSchoolId).collect(Collectors.toList());
        Map<Long, School> schoolMap = new HashMap<>();
        for(int i = 0; i < schoolIdList.size(); i += 200) {
            Map<Long, School> res = schoolLoaderClient.getSchoolLoader().loadSchools(schoolIdList.subList(i, Math.min(schoolIdList.size(), i + 200))).getUninterruptibly();
            if (MapUtils.isEmpty(res)) {
                continue;
            }
            schoolMap.putAll(res);
        }
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("快递单ID");
        firstRow.createCell(1).setCellValue("物流公司");
        firstRow.createCell(2).setCellValue("物流单号");
        firstRow.createCell(3).setCellValue("类型");
        firstRow.createCell(4).setCellValue("配送方式");
        firstRow.createCell(5).setCellValue("是否导回");
        firstRow.createCell(6).setCellValue("物流价格");
        firstRow.createCell(7).setCellValue("收货人");
        firstRow.createCell(8).setCellValue("收货人电话");
        firstRow.createCell(9).setCellValue("学校ID");
        firstRow.createCell(10).setCellValue("学校名称");
        firstRow.createCell(11).setCellValue("省");
        firstRow.createCell(12).setCellValue("市");
        firstRow.createCell(13).setCellValue("区");
        firstRow.createCell(14).setCellValue("详细地址");
        firstRow.createCell(15).setCellValue("学段");
        XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
        cellStyle.setWrapText(true);
        int rowNum = 1;
        for (RewardLogistics logistics : data) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            xssfRow.createCell(0).setCellValue(logistics.getId());
            xssfRow.createCell(1).setCellValue(logistics.getCompanyName());
            xssfRow.createCell(2).setCellValue(logistics.getLogisticNo());
            xssfRow.createCell(3).setCellValue(logistics.getType().getDescription());
            xssfRow.createCell(4).setCellValue(logistics.getLogisticType());
            xssfRow.createCell(5).setCellValue(logistics.getIsBack() != null && logistics.getIsBack() ? "是" : "否");
            xssfRow.createCell(6).setCellValue(logistics.getPrice() == null ? 0 : logistics.getPrice());
            xssfRow.createCell(7).setCellValue(logistics.getReceiverName());
            //导出订单,  需要真实手机号
            xssfRow.createCell(8).setCellValue(sensitiveUserDataServiceClient.loadRewardLogisticPhone(logistics.getId(), "crm_export"));
            xssfRow.createCell(9).setCellValue(logistics.getSchoolId());
            xssfRow.createCell(10).setCellValue(logistics.getSchoolName());
            xssfRow.createCell(11).setCellValue(logistics.getProvinceName());
            xssfRow.createCell(12).setCellValue(logistics.getCityName());
            xssfRow.createCell(13).setCellValue(logistics.getCountyName());
            xssfRow.createCell(14).setCellValue(logistics.getDetailAddress());
            xssfRow.createCell(15).setCellValue(Optional.ofNullable(schoolMap.get(logistics.getSchoolId()))
                                                                     .map(e -> SchoolLevel.safeParse(e.getLevel(), null))
                                                                     .map(e -> e.getDescription()).orElse(""));
            xssfSheet.setColumnWidth(2, 300 * 15);
            xssfSheet.setColumnWidth(10, 300 * 15);
            xssfSheet.setColumnWidth(14, 600 * 15);
        }
        return xssfWorkbook;
    }

    private XSSFWorkbook convertToDeliverHSS(List<RewardCompleteOrder> completeOrders) {
        List<Long> schoolIdList = completeOrders.stream().filter(e -> e.getSchoolId() != null).map(RewardCompleteOrder::getSchoolId).collect(Collectors.toList());
        Map<Long, School> schoolMap = new HashMap<>();
        for(int i = 0; i < schoolIdList.size(); i += 200) {
            Map<Long, School> res = schoolLoaderClient.getSchoolLoader().loadSchools(schoolIdList.subList(i, Math.min(schoolIdList.size(), i + 200))).getUninterruptibly();
            if (MapUtils.isEmpty(res)) {
                continue;
            }
            schoolMap.putAll(res);
        }
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("用户ID");
        firstRow.createCell(1).setCellValue("用户姓名");
        firstRow.createCell(2).setCellValue("省");
        firstRow.createCell(3).setCellValue("市");
        firstRow.createCell(4).setCellValue("区");
        firstRow.createCell(5).setCellValue("学校ID");
        firstRow.createCell(6).setCellValue("学校");
        firstRow.createCell(7).setCellValue("班级");
        firstRow.createCell(8).setCellValue("收货人");
        firstRow.createCell(9).setCellValue("电话");
        firstRow.createCell(10).setCellValue("配送方式");
        firstRow.createCell(11).setCellValue("详细地址");
        firstRow.createCell(12).setCellValue("奖品明细");
        firstRow.createCell(13).setCellValue("奖品数量");
        firstRow.createCell(14).setCellValue("单位");
        firstRow.createCell(15).setCellValue("老师科目");
        firstRow.createCell(16).setCellValue("是否校园大使");
        firstRow.createCell(17).setCellValue("快递单ID");
        firstRow.createCell(18).setCellValue("学段");
        XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
        cellStyle.setWrapText(true);
        int rowNum = 1;
        Map<Long, List<RewardCompleteOrder>> userOrderData = new HashMap<>();
        for (RewardCompleteOrder order : completeOrders) {
            if (userOrderData.containsKey(order.getBuyerId())) {
                userOrderData.get(order.getBuyerId()).add(order);
            } else {
                List<RewardCompleteOrder> userOrderList = new ArrayList<>();
                userOrderList.add(order);
                userOrderData.put(order.getBuyerId(), userOrderList);
            }
        }
        for (Map.Entry<Long, List<RewardCompleteOrder>> entry : userOrderData.entrySet()) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            RewardCompleteOrder order = entry.getValue().get(0);
            xssfRow.createCell(0).setCellValue(order.getBuyerId());
            xssfRow.createCell(1).setCellValue(order.getBuyerName());
            xssfRow.createCell(2).setCellValue(order.getProvinceName());
            xssfRow.createCell(3).setCellValue(order.getCityName());
            xssfRow.createCell(4).setCellValue(order.getCountyName());
            xssfRow.createCell(5).setCellValue(order.getSchoolId());
            xssfRow.createCell(6).setCellValue(order.getSchoolName());
            xssfRow.createCell(7).setCellValue(order.getClazzName());
            xssfRow.createCell(8).setCellValue(order.getReceiverName());

            //导出订单,  需要真实手机号
            xssfRow.createCell(9).setCellValue(sensitiveUserDataServiceClient.loadRewardCompleteOrderPhone(order.getId(), "crm_export"));

            xssfRow.createCell(10).setCellValue(order.getLogisticType());
            xssfRow.createCell(11).setCellValue(order.getDetailAddress());
            Map<String, Integer> productMap = new HashMap<>();
            for (RewardCompleteOrder completeOrder : entry.getValue()) {
                String productKey = completeOrder.getProductName() + "_" + completeOrder.getSkuName();
                if (productMap.containsKey(productKey)) {
                    productMap.put(productKey, productMap.get(productKey) + completeOrder.getQuantity());
                } else {
                    productMap.put(productKey, completeOrder.getQuantity());
                }
            }
            String productDetail = "";
            int count = 0;
            for (Map.Entry<String, Integer> productEntry : productMap.entrySet()) {
                if (StringUtils.isNotBlank(productDetail)) {
                    productDetail = productDetail + "\r\n" + productEntry.getKey() + "_X" + productEntry.getValue();
                } else {
                    productDetail = productDetail + productEntry.getKey() + "_X" + productEntry.getValue();
                }
                count = count + productEntry.getValue();
            }
            XSSFCell cell = xssfRow.createCell(12);
            cell.setCellValue(new XSSFRichTextString(productDetail));
            cell.setCellStyle(cellStyle);
            xssfRow.createCell(13).setCellValue(count);
            xssfRow.createCell(14).setCellValue(order.getUnit());
            // 老师科目和是否校园大使
            TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(order.getReceiverId());
            if (detail != null) {
                xssfRow.createCell(15).setCellValue(detail.getSubject() == null ? "" : detail.getSubject().getValue());
                xssfRow.createCell(16).setCellValue(detail.isSchoolAmbassador() ? "是" : "否");
            }
            xssfRow.createCell(17).setCellValue(order.getLogisticsId());
            xssfRow.createCell(18).setCellValue(Optional.ofNullable(schoolMap.get(order.getSchoolId()))
                    .map(e -> SchoolLevel.safeParse(e.getLevel(), null))
                    .map(e -> e.getDescription()).orElse(""));
            xssfSheet.setColumnWidth(6, 600 * 15);
            xssfSheet.setColumnWidth(7, 300 * 15);
            xssfSheet.setColumnWidth(9, 300 * 15);
            xssfSheet.setColumnWidth(11, 600 * 15);
            xssfSheet.setColumnWidth(12, 400 * 15 * 3);
        }
        return xssfWorkbook;
    }

    private XSSFWorkbook convertToAuditingXSSF(List<RewardOrder> exportList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        for (int i = 0; i < 3; i++) {
            xssfSheet.setColumnWidth(i, 400 * 15);
        }
        XSSFRow firstRow = xssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("用户ID");
        firstRow.createCell(1).setCellValue("用户姓名");
        firstRow.createCell(2).setCellValue("订单ID");
        int rowNum = 1;
        for (RewardOrder reward : exportList) {
            XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
            xssfRow.setHeightInPoints(20);
            xssfRow.createCell(0).setCellValue(reward.getBuyerId());
            xssfRow.createCell(1).setCellValue(reward.getBuyerName());
            xssfRow.createCell(2).setCellValue(reward.getId());
        }

        return xssfWorkbook;
    }

    private List<Map<String, Object>> loadCompleteOrderStatInfo(String month) {
        if (StringUtils.isBlank(month)) return Collections.emptyList();
        MonthRange range = MonthRange.newInstance(DateUtils.stringToDate(month, "yyyyMM").getTime());

        // 改成走从库查询
        return RoutingPolicyExecutorBuilder.getInstance()
                .<List<Map<String, Object>>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {

                    String sql = "SELECT PRODUCT_ID AS pid, " +
                            "PRODUCT_NAME AS pname, " +
                            "SKU_NAME AS sname, " +
                            "SUM(QUANTITY) AS total, " +
                            "STATUS as status " +
                            "FROM VOX_REWARD_COMPLETE_ORDER " +
                            "WHERE CREATE_DATETIME>? AND CREATE_DATETIME<?" +
                            "GROUP BY PRODUCT_ID, PRODUCT_NAME, SKU_NAME, STATUS";

                    return utopiaSqlReward.withSql(sql)
                            .useParamsArgs(range.getStartDate(), range.getEndDate())
                            .queryAll();
                })
                .execute();
    }

    /**
     * 按照具体日期以及多个订单号来统计商品销售信息
     *
     * @param productIds
     * @param startDateStr
     * @param endDateStr
     * @return
     */
    private List<Map<String, Object>> loadStatInfo(List<Long> productIds, String startDateStr, String endDateStr, String orderStauts) {
        if (CollectionUtils.isEmpty(productIds)) {
            return Collections.emptyList();
        }
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("productIds", productIds);
        queryParams.put("orderStatus", orderStauts);

        Date endDate = DateUtils.stringToDate(endDateStr, DateUtils.FORMAT_SQL_DATE);
        Date endTime = new Date(DateUtils.roundDateToDay235959InMillis(endDate, 0));

        endDateStr = DateUtils.dateToString(endTime);
        queryParams.put("beginDate", startDateStr);
        queryParams.put("endDate", endDateStr);

        List<Map<String, Object>> data = RoutingPolicyExecutorBuilder.getInstance()
                .<List<Map<String, Object>>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    String whereStr = "SELECT o.PRODUCT_ID, o.SKU_ID, o.SKU_NAME, SUM(o.QUANTITY) AS QUANTITY, " +
                            "SUM(o.TOTAL_PRICE) AS TOTAL_PRICE, o.UNIT, COUNT(DISTINCT o.BUYER_ID) AS USER_COUNT FROM " +
                            "( " +
                            "SELECT PRODUCT_ID, SKU_ID, SKU_NAME, QUANTITY, TOTAL_PRICE, UNIT, BUYER_ID " +
                            "FROM VOX_REWARD_ORDER WHERE DISABLED = FALSE " +
                            "AND CREATE_DATETIME>=:beginDate " +
                            "AND CREATE_DATETIME<=:endDate " +
                            "AND PRODUCT_ID in (:productIds)" +
                            (StringUtils.isNotEmpty(orderStauts) ? " AND STATUS=:orderStatus" : "") +
                            ") o " +
                            "GROUP BY o.PRODUCT_ID, o.SKU_NAME, o.SKU_ID, o.UNIT " +
                            "ORDER BY QUANTITY DESC";

                    return utopiaSqlReward.withSql(whereStr).useParams(queryParams).queryAll();
                }).execute();

        Map<Long, RewardProduct> productMap = rewardLoaderClient.loadRewardProductMap();

        // 补充最新的商品名称
        if (data != null) {
            for (Map<String, Object> dataItem : data) {
                Long productId = MapUtils.getLong(dataItem, "PRODUCT_ID");
                RewardProduct rewardProduct = productMap.get(productId);
                if (rewardProduct == null) {
                    dataItem.put("PRODUCT_NAME", "");
                } else {
                    dataItem.put("PRODUCT_NAME", rewardProduct.getProductName());
                }
            }
        }

        Map<String, Object> results = new HashMap<>();
        for (Map<String, Object> map : data) {
            String key = map.get("PRODUCT_ID").toString() + "_" + map.get("SKU_ID").toString();
            if (results.containsKey(key)) {
                Map<String, Object> stat = (Map<String, Object>) results.get(key);
                if (map.get("UNIT").toString().equals(RewardProductPriceUnit.园丁豆.name())) {
                    stat.put("teacherTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("teacherCount", map.get("USER_COUNT").toString());
                    stat.put("teacherQuantity", map.get("QUANTITY").toString());
                } else if (map.get("UNIT").toString().equals(RewardProductPriceUnit.学豆.name())) {
                    stat.put("studentTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("studentCount", map.get("USER_COUNT").toString());
                    stat.put("studentQuantity", map.get("QUANTITY").toString());
                } else {
                    stat.put("juniorTeacherQuantity", map.get("QUANTITY").toString());
                    stat.put("juniorTeacherTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("juniorTeacherCount", map.get("USER_COUNT").toString());
                }

                int orgTotalCount = SafeConverter.toInt(stat.get("totalCount"));
                stat.put("totalCount", orgTotalCount + SafeConverter.toInt(map.get("QUANTITY").toString()));

                results.put(key, stat);
            } else {
                Map<String, Object> stat = new HashMap<>();
                stat.put("skuName", map.get("SKU_NAME").toString());
                stat.put("skuId", map.get("SKU_ID").toString());
                stat.put("productName", map.get("PRODUCT_NAME").toString());
                stat.put("productId", map.get("PRODUCT_ID").toString());

                RewardProduct product = productMap.get(SafeConverter.toLong(map.get("PRODUCT_ID")));
                if (product != null) {
                    stat.put("productType", product.getProductType());
                }

                if (map.get("UNIT").toString().equals(RewardProductPriceUnit.学豆.name())) {
                    stat.put("studentTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("studentCount", map.get("USER_COUNT").toString());
                    stat.put("studentQuantity", map.get("QUANTITY").toString());
                } else if (map.get("UNIT").toString().equals(RewardProductPriceUnit.园丁豆.name())) {
                    stat.put("teacherTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("teacherCount", map.get("USER_COUNT").toString());
                    stat.put("teacherQuantity", map.get("QUANTITY").toString());
                } else {
                    stat.put("juniorTeacherQuantity", map.get("QUANTITY").toString());
                    stat.put("juniorTeacherTotalPrice", map.get("TOTAL_PRICE").toString());
                    stat.put("juniorTeacherCount", map.get("USER_COUNT").toString());
                }

                stat.put("totalCount", map.get("QUANTITY").toString());
                results.put(key, stat);
            }
        }

        ArrayList queryResult = new ArrayList();
        queryResult.addAll(results.values());

        return queryResult;
    }

    private List<Map<String, Object>> loadStatInfo(Long productId, String beginMonth, String endMonth) {
        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);
        String whereStr = "SELECT " +
                " PRODUCT_ID as productId," +
                " PRODUCT_NAME AS productName, " +
                " SKU_NAME AS skuName, " +
                " SUM(STUDENT_COUNT) AS studentQuantity, " +
                " SUM(TEACHER_COUNT) AS teacherQuantity, " +
                " SUM(JUNIOR_TEACHER_COUNT) AS juniorTeacherQuantity, " +
                " SUM(STUDENT_PRICE) AS studentTotalPrice, " +
                " SUM(TEACHER_PRICE) AS teacherTotalPrice, " +
                " SUM(JUNIOR_TEACHER_PRICE) AS juniorTeacherTotalPrice, " +
                " SUM(STUDENT_COUNT) + SUM(TEACHER_COUNT) + SUM(JUNIOR_TEACHER_COUNT) AS totalCount " +
                " FROM " +
                " VOX_REWARD_ORDER_SUMMARY " +
                "WHERE 1=1 ";
        if (productId != null && productId != 0L) {
            whereStr = whereStr + " AND PRODUCT_ID=:productId";
        }
        if (StringUtils.isNotBlank(beginMonth)) {
            params.put("beginMonth", SafeConverter.toInt(beginMonth));
            whereStr = whereStr + " AND MONTH>=:beginMonth";
        }
        if (StringUtils.isNotBlank(endMonth)) {
            params.put("endMonth", SafeConverter.toInt(endMonth));
            whereStr = whereStr + " AND MONTH<=:endMonth";
        }
        whereStr = whereStr + " GROUP BY PRODUCT_ID, SKU_NAME";
        Map<Long, RewardProduct> productMap = rewardLoaderClient.loadRewardProductMap();
        List<Map<String, Object>> result = utopiaSqlReward.withSql(whereStr).useParams(params).queryAll();

        // 补充上奖品类型的字段
        result.forEach(m -> {
            Long pId = SafeConverter.toLong(m.get("productId"));
            RewardProduct product = productMap.get(pId);
            if (product != null) {
                m.put("productType", product.getProductType());
            }
        });

        return result;
    }
}
