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

package com.voxlearning.utopia.mizar.controller.order;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.XssfUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.mizar.api.constants.PicOrderInfoType;
import com.voxlearning.utopia.service.mizar.api.entity.order.PicOrderInfo;
import com.voxlearning.utopia.service.mizar.api.service.PicOrderInfoService;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrderAmortizeHistory;
import com.voxlearning.utopia.service.order.api.service.PicListenBookOrderService;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.TextBookManagementLoader;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/2/24.
 */
@Controller
@RequestMapping(value = "order/")
public class PicOrderController extends AbstractMizarController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @ImportService(interfaceClass = PicOrderInfoService.class)
    private PicOrderInfoService picOrderInfoService;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @ImportService(interfaceClass = TextBookManagementLoader.class)
    private TextBookManagementLoader textBookManagementLoader;
    @ImportService(interfaceClass = PicListenBookOrderService.class)
    private PicListenBookOrderService picListenBookOrderService;


    /**
     * 查询人教订单数据统计情况
     */
    @RequestMapping(value = "picorder/count.vpage", method = RequestMethod.GET)
    public String findPicOrderCount(Model model) {
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        String publisherFlag = getRequestString("publisher_flag");
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate) || StringUtils.isBlank(publisherFlag)) {
            return "order/ordercount";
        }
        PicOrderInfoType picOrderInfoType = PicOrderInfoType.valueOf(publisherFlag);
        if (!Arrays.asList(PicOrderInfoType.values()).contains(picOrderInfoType)) {
            return "order/ordercount";
        }
        Date startTime = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE);
        Date endTime = DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE);
        List<PicOrderInfo> orderCountList = picOrderInfoService.getOrderCount(startTime, endTime);
        List<String> bookIds = orderCountList.stream().map(PicOrderInfo::getBookId).collect(Collectors.toList());
        Set<String> currentBooks = newContentLoaderClient.loadBooks(bookIds).values().stream().filter(e -> StringUtils.equals(e.getPublisher(), "人民教育出版社")).map(NewBookProfile::getId).collect(Collectors.toSet());
        orderCountList = orderCountList.stream().filter(e -> !e.getDisabled() && currentBooks.contains(e.getBookId())).collect(Collectors.toList());
        Map<String, Object> countDetail = generateOrderCountDetail(orderCountList);
        BigDecimal orderMoneyCount = new BigDecimal(0);
        BigDecimal backMoneyCount = new BigDecimal(0);
        Long backOrderCount = 0L;
        Long orderCount = 0L;
        if (MapUtils.isNotEmpty(countDetail)) {
            orderMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("orderMoneyCount")));
            backMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("backMoneyCount")));
            backOrderCount = SafeConverter.toLong(countDetail.get("backOrderCount"));
            orderCount = SafeConverter.toLong(countDetail.get("orderCount"));
        }
        //总单数
        orderCount = orderCount + backOrderCount;
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("backOrderCount", backOrderCount);
        model.addAttribute("orderMoneyCount", orderMoneyCount);
        model.addAttribute("backMoneyCount", backMoneyCount);
        model.addAttribute("realIncome", orderMoneyCount.subtract(backMoneyCount).doubleValue());

        return "order/ordercount";
    }


    /**
     * 查询山科订单数据统计情况
     */
    @RequestMapping(value = "sk_picorder/sk_count.vpage", method = RequestMethod.GET)
    public String findPicOrderCountBySK(Model model) {
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        String publisherFlag = getRequestString("publisher_flag");
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate) || StringUtils.isBlank(publisherFlag)) {
            return "order/ordercountsk";
        }
        PicOrderInfoType picOrderInfoType = PicOrderInfoType.valueOf(publisherFlag);
        if (!Arrays.asList(PicOrderInfoType.values()).contains(picOrderInfoType)) {
            return "order/ordercountsk";
        }
        Date startTime = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE);
        Date endTime = DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE);
        List<PicOrderInfo> orderCountList = picOrderInfoService.getOrderCount(startTime, endTime);
        List<String> bookIds = orderCountList.stream().map(PicOrderInfo::getBookId).collect(Collectors.toList());
        Set<String> currentBooks = newContentLoaderClient.loadBooks(bookIds).values().stream().filter(e -> StringUtils.equals(e.getPublisher(), "山东科学技术出版社")).map(NewBookProfile::getId).collect(Collectors.toSet());
        orderCountList = orderCountList.stream().filter(e -> !e.getDisabled() && currentBooks.contains(e.getBookId())).collect(Collectors.toList());
        Map<String, Object> countDetail = generateOrderCountDetail(orderCountList);
        BigDecimal orderMoneyCount = new BigDecimal(0);
        BigDecimal backMoneyCount = new BigDecimal(0);
        Long backOrderCount = 0L;
        Long orderCount = 0L;
        if (MapUtils.isNotEmpty(countDetail)) {
            orderMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("orderMoneyCount")));
            backMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("backMoneyCount")));
            backOrderCount = SafeConverter.toLong(countDetail.get("backOrderCount"));
            orderCount = SafeConverter.toLong(countDetail.get("orderCount"));
        }
        //总单数
        orderCount = orderCount + backOrderCount;
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("backOrderCount", backOrderCount);
        model.addAttribute("orderMoneyCount", orderMoneyCount);
        model.addAttribute("backMoneyCount", backMoneyCount);
        model.addAttribute("realIncome", orderMoneyCount.subtract(backMoneyCount).doubleValue());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "order/ordercountsk";
    }

    /**
     * 查询沪教订单数据统计情况
     */
    @RequestMapping(value = "sh_picorder/sh_count.vpage", method = RequestMethod.GET)
    public String findPicOrderCountBySH(Model model) {
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
            return "order/ordercountsh";
        }
        Date startTime = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE);
        Date endTime = DayRange.newInstance(DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE).getTime()).getEndDate();
        Set<String> productIds = generatePublisherBookProduct("沪教版");
        List<UserOrderAmortizeHistory> historyList = picListenBookOrderService.getOrderCount(startTime, endTime, productIds);
        Map<String, Object> countDetail = generateOrderCountDetailByHistory(historyList);
        BigDecimal orderMoneyCount = new BigDecimal(0);
        BigDecimal backMoneyCount = new BigDecimal(0);
        Long backOrderCount = 0L;
        Long orderCount = 0L;
        if (MapUtils.isNotEmpty(countDetail)) {
            orderMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("orderMoneyCount")));
            backMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("backMoneyCount")));
            backOrderCount = SafeConverter.toLong(countDetail.get("backOrderCount"));
            orderCount = SafeConverter.toLong(countDetail.get("orderCount"));
        }
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("backOrderCount", backOrderCount);
        model.addAttribute("orderMoneyCount", orderMoneyCount);
        model.addAttribute("backMoneyCount", backMoneyCount);
        model.addAttribute("realIncome", orderMoneyCount.subtract(backMoneyCount).doubleValue());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "order/ordercountsh";
    }


    /**
     * 查询辽师大订单数据统计情况
     */
    @RequestMapping(value = "ln_picorder/ln_count.vpage", method = RequestMethod.GET)
    public String findPicOrderCountByLn(Model model) {
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        String publisherFlag = getRequestString("publisher_flag");
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate) || StringUtils.isBlank(publisherFlag)) {
            return "order/ordercountln";
        }
        PicOrderInfoType picOrderInfoType = PicOrderInfoType.valueOf(publisherFlag);
        if (!Arrays.asList(PicOrderInfoType.values()).contains(picOrderInfoType)) {
            return "order/ordercountln";
        }
        Date startTime = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE);
        Date endTime = DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE);
        List<PicOrderInfo> orderCountList = picOrderInfoService.getOrderCount(startTime, endTime);
        List<String> bookIds = orderCountList.stream().map(PicOrderInfo::getBookId).collect(Collectors.toList());
        Set<String> currentBooks = newContentLoaderClient.loadBooks(bookIds).values().stream().filter(e -> StringUtils.equals(e.getPublisher(), "辽宁师范大学出版社")).map(NewBookProfile::getId).collect(Collectors.toSet());
        orderCountList = orderCountList.stream().filter(e -> !e.getDisabled() && currentBooks.contains(e.getBookId())).collect(Collectors.toList());
        Map<String, Object> countDetail = generateOrderCountDetail(orderCountList);
        BigDecimal orderMoneyCount = new BigDecimal(0);
        BigDecimal backMoneyCount = new BigDecimal(0);
        Long backOrderCount = 0L;
        Long orderCount = 0L;
        if (MapUtils.isNotEmpty(countDetail)) {
            orderMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("orderMoneyCount")));
            backMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("backMoneyCount")));
            backOrderCount = SafeConverter.toLong(countDetail.get("backOrderCount"));
            orderCount = SafeConverter.toLong(countDetail.get("orderCount"));
        }
        //总单数
        orderCount = orderCount + backOrderCount;
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("backOrderCount", backOrderCount);
        model.addAttribute("orderMoneyCount", orderMoneyCount);
        model.addAttribute("backMoneyCount", backMoneyCount);
        model.addAttribute("realIncome", orderMoneyCount.subtract(backMoneyCount).doubleValue());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "order/ordercountln";
    }

    /**
     * 查询人教某一天的详细统计信息
     */
    @RequestMapping(value = "picorder/detailcount.vpage", method = RequestMethod.GET)
    public String findPicOrderDetail(Model model) {
        String queryDate = getRequestString("queryDate");
        String publisherFlag = getRequestString("publisher_flag");
        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1)) - 1;
        if (StringUtils.isBlank(queryDate) || StringUtils.isBlank(publisherFlag)) {
            return "order/orderdetailcount";
        }
        PicOrderInfoType picOrderInfoType = PicOrderInfoType.valueOf(publisherFlag);
        if (!Arrays.asList(PicOrderInfoType.values()).contains(picOrderInfoType)) {
            return "order/orderdetailcount";
        }
        Date queryTime = DateUtils.stringToDate(queryDate, DateUtils.FORMAT_SQL_DATE);
        Pageable pageable = new PageRequest(pageIndex, 10);
        Page<PicOrderInfo> currentDayOrderDetailByPage = picOrderInfoService.getCurrentDayOrderDetailByPage(queryTime, pageable, picOrderInfoType.getDesc());
        List<Map<String, Object>> mapList = generatePicOrderDetail(currentDayOrderDetailByPage.getContent());
        model.addAttribute("returnList", mapList);
        model.addAttribute("pageIndex", pageIndex + 1);
        model.addAttribute("totalPages", currentDayOrderDetailByPage.getTotalPages());
        model.addAttribute("queryDate", queryDate);
        return "order/orderdetailcount";
    }


    /**
     * 查询山科某一天的详细统计信息
     */
    @RequestMapping(value = "sk_picorder/sk_detailcount.vpage", method = RequestMethod.GET)
    public String findPicOrderDetailBySk(Model model) {
        String queryDate = getRequestString("queryDate");
        String publisherFlag = getRequestString("publisher_flag");
        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1)) - 1;
        if (StringUtils.isBlank(queryDate) || StringUtils.isBlank(publisherFlag)) {
            return "order/orderdetailcountsk";
        }
        PicOrderInfoType picOrderInfoType = PicOrderInfoType.valueOf(publisherFlag);
        if (!Arrays.asList(PicOrderInfoType.values()).contains(picOrderInfoType)) {
            return "order/orderdetailcountsk";
        }
        Date queryTime = DateUtils.stringToDate(queryDate, DateUtils.FORMAT_SQL_DATE);
        Pageable pageable = new PageRequest(pageIndex, 10);
        Page<PicOrderInfo> currentDayOrderDetailByPage = picOrderInfoService.getCurrentDayOrderDetailByPage(queryTime, pageable, picOrderInfoType.getDesc());
        List<Map<String, Object>> mapList = generatePicOrderDetail(currentDayOrderDetailByPage.getContent());
        model.addAttribute("returnList", mapList);
        model.addAttribute("pageIndex", pageIndex + 1);
        model.addAttribute("totalPages", currentDayOrderDetailByPage.getTotalPages());
        model.addAttribute("queryDate", queryDate);
        return "order/orderdetailcountsk";
    }


    /**
     * 查询沪教某一天的详细统计信息
     */
    @RequestMapping(value = "sh_picorder/sh_detailcount.vpage", method = RequestMethod.GET)
    public String findPicOrderDetailBySh(Model model) {
        String queryDate = getRequestString("queryDate");
        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1)) - 1;
        if (StringUtils.isBlank(queryDate)) {
            return "order/orderdetailcountsh";
        }
        Date queryTime = DateUtils.stringToDate(queryDate, DateUtils.FORMAT_SQL_DATE);
        Pageable pageable = new PageRequest(pageIndex, 10);
        Set<String> productIds = generatePublisherBookProduct("沪教版");
        Page<UserOrderAmortizeHistory> historyByPage = picListenBookOrderService.getCurrentDayOrderDetailByPage(queryTime, pageable, productIds);
        List<Map<String, Object>> mapList = generatePicOrderDetailByHistory(historyByPage.getContent());
        model.addAttribute("returnList", mapList);
        model.addAttribute("pageIndex", pageIndex + 1);
        model.addAttribute("totalPages", historyByPage.getTotalPages());
        model.addAttribute("queryDate", queryDate);
        return "order/orderdetailcountsh";
    }


    /**
     * 查询辽师大某一天的详细统计信息
     */
    @RequestMapping(value = "ln_picorder/ln_detailcount.vpage", method = RequestMethod.GET)
    public String findPicOrderDetailByLn(Model model) {
        String queryDate = getRequestString("queryDate");
        String publisherFlag = getRequestString("publisher_flag");
        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1)) - 1;
        if (StringUtils.isBlank(queryDate) || StringUtils.isBlank(publisherFlag)) {
            return "order/orderdetailcountln";
        }
        PicOrderInfoType picOrderInfoType = PicOrderInfoType.valueOf(publisherFlag);
        if (!Arrays.asList(PicOrderInfoType.values()).contains(picOrderInfoType)) {
            return "order/orderdetailcountln";
        }
        Date queryTime = DateUtils.stringToDate(queryDate, DateUtils.FORMAT_SQL_DATE);
        Pageable pageable = new PageRequest(pageIndex, 10);
        Page<PicOrderInfo> currentDayOrderDetailByPage = picOrderInfoService.getCurrentDayOrderDetailByPage(queryTime, pageable, picOrderInfoType.getDesc());
        List<Map<String, Object>> mapList = generatePicOrderDetail(currentDayOrderDetailByPage.getContent());
        model.addAttribute("returnList", mapList);
        model.addAttribute("pageIndex", pageIndex + 1);
        model.addAttribute("totalPages", currentDayOrderDetailByPage.getTotalPages());
        model.addAttribute("queryDate", queryDate);
        return "order/orderdetailcountln";
    }

    /**
     * 下载当天的订单数据
     */
    @RequestMapping(value = {"picorder/downloadOrderDetail.vpage", "sk_picorder/downloadOrderDetail.vpage", "ln_picorder/downloadOrderDetail.vpage"}, method = RequestMethod.GET)
    public void downloadOrderDetail() throws Exception {
        String queryDate = getRequestString("queryDate");
        String publisherFlag = getRequestString("publisher_flag");
        if (StringUtils.isBlank(queryDate) || StringUtils.isBlank(publisherFlag)) {
            return;
        }
        PicOrderInfoType picOrderInfoType = PicOrderInfoType.valueOf(publisherFlag);
        if (!Arrays.asList(PicOrderInfoType.values()).contains(picOrderInfoType)) {
            return;
        }
        Date queryTime = DateUtils.stringToDate(queryDate, DateUtils.FORMAT_SQL_DATE);
        String fileName = "订单详情-" + queryDate + ".xlsx";
        List<List<String>> dataList = generateDownLoadData(queryTime, picOrderInfoType.getDesc());
        XSSFWorkbook xssfWorkbook = convertDateDataToHSSfWorkbook(dataList);
        fileName = XssfUtils.generateFilename(fileName);
        @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            xssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException e) {
            logger.error("download order detail error!", e);
        }

    }


    /**
     * 查询译林订单数据统计情况
     */
    @RequestMapping(value = "yl_picorder/yl_count.vpage", method = RequestMethod.GET)
    public String findPicOrderCountByYl(Model model) {
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
            return "order/ordercountyl";
        }
        Date startTime = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE);
        Date endTime = DayRange.newInstance(DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE).getTime()).getEndDate();
        Set<String> productIds = generatePublisherBookProduct("译林版");
        List<UserOrderAmortizeHistory> historyList = picListenBookOrderService.getOrderCount(startTime, endTime, productIds);
        Map<String, Object> countDetail = generateOrderCountDetailByHistory(historyList);
        BigDecimal orderMoneyCount = new BigDecimal(0);
        BigDecimal backMoneyCount = new BigDecimal(0);
        Long backOrderCount = 0L;
        Long orderCount = 0L;
        if (MapUtils.isNotEmpty(countDetail)) {
            orderMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("orderMoneyCount")));
            backMoneyCount = new BigDecimal(SafeConverter.toString(countDetail.get("backMoneyCount")));
            backOrderCount = SafeConverter.toLong(countDetail.get("backOrderCount"));
            orderCount = SafeConverter.toLong(countDetail.get("orderCount"));
        }
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("backOrderCount", backOrderCount);
        model.addAttribute("orderMoneyCount", orderMoneyCount);
        model.addAttribute("backMoneyCount", backMoneyCount);
        model.addAttribute("realIncome", orderMoneyCount.subtract(backMoneyCount).doubleValue());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "order/ordercountyl";
    }


    /**
     * 查询译林某一天的详细统计信息
     */
    @RequestMapping(value = "yl_picorder/yl_detailcount.vpage", method = RequestMethod.GET)
    public String findPicOrderDetailByYl(Model model) {
        String queryDate = getRequestString("queryDate");
        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1)) - 1;
        if (StringUtils.isBlank(queryDate)) {
            return "order/orderdetailcountyl";
        }
        Date queryTime = DateUtils.stringToDate(queryDate, DateUtils.FORMAT_SQL_DATE);
        Pageable pageable = new PageRequest(pageIndex, 10);
        Set<String> productIds = generatePublisherBookProduct("译林版");
        Page<UserOrderAmortizeHistory> historyByPage = picListenBookOrderService.getCurrentDayOrderDetailByPage(queryTime, pageable, productIds);
        List<Map<String, Object>> mapList = generatePicOrderDetailByHistory(historyByPage.getContent());
        model.addAttribute("returnList", mapList);
        model.addAttribute("pageIndex", pageIndex + 1);
        model.addAttribute("totalPages", historyByPage.getTotalPages());
        model.addAttribute("queryDate", queryDate);
        return "order/orderdetailcountyl";
    }

    /**
     * 下载译林当天的订单数据
     */
    @RequestMapping(value = {"yl_picorder/downloadOrderDetail.vpage", "sh_picorder/downloadOrderDetail.vpage"}, method = RequestMethod.GET)
    public void downloadYlOrderDetail() throws Exception {
        String queryDate = getRequestString("queryDate");
        String publishName = getRequestString("publishName");
        Date queryTime = DateUtils.stringToDate(queryDate, DateUtils.FORMAT_SQL_DATE);
        String fileName = "订单详情-" + queryDate + ".xlsx";
        Set<String> productIds = generatePublisherBookProduct(publishName);
        List<List<String>> dataList = generateDownLoadDataFormHistory(queryTime, productIds);
        XSSFWorkbook xssfWorkbook = convertDateDataToHSSfWorkbookForHistory(dataList);
        fileName = XssfUtils.generateFilename(fileName);
        @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            xssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException e) {
            logger.error("download order detail error!", e);
        }

    }


    private XSSFWorkbook convertDateDataToHSSfWorkbook(List<List<String>> dataList) {
        String[] dateDataTitle = new String[]{
                "订单ID", "用户姓名", "产品名称", "订单状态",
                "订单金额", "教材年级", "教材学期", "教材科目",
                "订单有效期（天）", "订单创建时间", "用户学校所在城市"
        };
        int[] dateDataWidth = new int[]{
                5000, 5000, 5000, 5000,
                5000, 5000, 5000, 5000,
                4000, 4000, 4000
        };
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        try {
            xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, dataList);
        } catch (Exception e) {
            logger.error("generate order detail xlsx error!", e);
        }
        return xssfWorkbook;
    }

    private XSSFWorkbook convertDateDataToHSSfWorkbookForHistory(List<List<String>> dataList) {
        String[] dateDataTitle = new String[]{
                "订单ID", "用户姓名", "产品名称", "订单状态",
                "订单金额", "教材年级", "教材学期", "教材科目",
                "订单有效期（天）", "订单创建时间", "用户学校所在城市", "商品类型"
        };
        int[] dateDataWidth = new int[]{
                5000, 5000, 5000, 5000,
                5000, 5000, 5000, 5000,
                4000, 4000, 4000, 4000
        };
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        try {
            xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, dataList);
        } catch (Exception e) {
            logger.error("generate order detail xlsx error!", e);
        }
        return xssfWorkbook;
    }


    private List<Map<String, Object>> generatePicOrderDetail(List<PicOrderInfo> orderInfos) {
        if (CollectionUtils.isEmpty(orderInfos)) {
            return Collections.emptyList();
        }
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            orderInfos = orderInfos.stream().filter(e -> !e.getDisabled()).collect(Collectors.toList());
        } else {
            orderInfos = orderInfos.stream().filter(e -> !e.getDisabled() && e.getPayAmount().compareTo(new BigDecimal(0.01)) > 0.01).collect(Collectors.toList());
        }
        List<Long> userIds = orderInfos.stream().map(PicOrderInfo::getUserId).collect(Collectors.toList());
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
        Map<Long, User> studentMap = new HashMap<>();
        Map<Long, User> parentMap = new HashMap<>();
        userMap.entrySet().stream().filter(map -> map.getValue().isParent()).forEach(map -> parentMap.put(map.getKey(), map.getValue()));
        userMap.entrySet().stream().filter(map -> map.getValue().isStudent()).forEach(map -> studentMap.put(map.getKey(), map.getValue()));
        Set<Long> studentIds = new HashSet<>();
        studentIds.addAll(studentMap.keySet());
        Map<Long, List<StudentParentRef>> parentStudentRefs = parentLoaderClient.loadParentStudentRefs(parentMap.keySet());
        Map<Long, Long> parentFirstStudent = new HashMap<>();
        parentStudentRefs.entrySet().stream().filter(entry -> CollectionUtils.isNotEmpty(entry.getValue())).forEach(entry -> parentFirstStudent.put(entry.getKey(), entry.getValue().get(0).getStudentId()));
        studentIds.addAll(parentFirstStudent.values());
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);
        Map<Long, String> userNameMap = studentDetailMap.values().stream().collect(Collectors.toMap(User::getId, User::fetchRealname));
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (PicOrderInfo order : orderInfos) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("productName", order.getProductName());
            map.put("userName", userNameMap.get(parentFirstStudent.get(order.getUserId())) != null ? userNameMap.get(parentFirstStudent.get(order.getUserId())) : userNameMap.get(order.getUserId()));
            map.put("payAmount", order.getPayAmount());
            if (StringUtils.equals(order.getPaymentStatus(), PaymentStatus.Paid.name())) {
                map.put("payStatus", "已支付");
            } else if (StringUtils.equals(order.getPaymentStatus(), PaymentStatus.Refund.name())) {
                map.put("payStatus", "已退款");
            }
            map.put("serviceTime", order.getServiceStartTime() != null && order.getServiceEndTime() != null ? DateUtils.dayDiff(order.getServiceEndTime(), order.getServiceStartTime()) : "");
            map.put("orderCreateTime", DateUtils.dateToString(order.getOrderCreateTime()));
            StudentDetail studentDetail = studentDetailMap.get(order.getUserId()) != null ? studentDetailMap.get(order.getUserId()) : studentDetailMap.get(parentFirstStudent.get(order.getUserId()));
            if (studentDetail != null) {
                Integer studentSchoolRegionCode = studentDetail.getStudentSchoolRegionCode();
                if (studentSchoolRegionCode != null) {
                    ExRegion regionFromBuffer = raikouSystem.loadRegion(studentSchoolRegionCode);
                    map.put("cityName", regionFromBuffer.getCityName());
                }
            }
            if (order.getBookId() != null) {
                NewBookProfile newBookProfile = newContentLoaderClient.loadBook(order.getBookId());
                if (newBookProfile != null) {
                    map.put("clazzLevel", newBookProfile.getClazzLevel());
                    if (newBookProfile.getTermType() == 1) {
                        map.put("termType", "上学期");
                    } else if (newBookProfile.getTermType() == 2) {
                        map.put("termType", "下学期");
                    }
                    map.put("subject", Subject.fromSubjectId(newBookProfile.getSubjectId()).getValue());
                }
            }
            mapList.add(map);
        }
        return mapList;
    }


    private List<List<String>> generateDownLoadData(Date queryTime, String flag) {
        List<PicOrderInfo> downloadCurrentDayOrderDetail = picOrderInfoService.downloadCurrentDayOrderDetail(queryTime);
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            downloadCurrentDayOrderDetail = downloadCurrentDayOrderDetail.stream().filter(e -> !e.getDisabled() && e.getProductName().contains(flag)).collect(Collectors.toList());
        } else {
            downloadCurrentDayOrderDetail = downloadCurrentDayOrderDetail.stream().filter(e -> !e.getDisabled() && e.getPayAmount().compareTo(new BigDecimal(0.01)) > 0.01 && e.getProductName().contains(flag)).collect(Collectors.toList());
        }
        List<Long> userIds = downloadCurrentDayOrderDetail.stream().map(PicOrderInfo::getUserId).collect(Collectors.toList());
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
        Map<Long, User> studentMap = new HashMap<>();
        Map<Long, User> parentMap = new HashMap<>();
        userMap.entrySet().stream().filter(map -> map.getValue().isParent()).forEach(map -> parentMap.put(map.getKey(), map.getValue()));
        userMap.entrySet().stream().filter(map -> map.getValue().isStudent()).forEach(map -> studentMap.put(map.getKey(), map.getValue()));
        Set<Long> studentIds = new HashSet<>();
        studentIds.addAll(studentMap.keySet());
        Map<Long, List<StudentParentRef>> parentStudentRefs = parentLoaderClient.loadParentStudentRefs(parentMap.keySet());
        Map<Long, Long> parentFirstStudent = new HashMap<>();
        parentStudentRefs.entrySet().stream().filter(entry -> CollectionUtils.isNotEmpty(entry.getValue())).forEach(entry -> parentFirstStudent.put(entry.getKey(), entry.getValue().get(0).getStudentId()));
        studentIds.addAll(parentFirstStudent.values());
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);
        Map<Long, String> userNameMap = studentDetailMap.values().stream().collect(Collectors.toMap(User::getId, User::fetchRealname));


        List<List<String>> dataList = new ArrayList<>();
        for (PicOrderInfo orderInfo : downloadCurrentDayOrderDetail) {
            List<String> list = new ArrayList<>();
            list.add(orderInfo.getOrderId());
            list.add(userNameMap.get(parentFirstStudent.get(orderInfo.getUserId())) != null ? userNameMap.get(parentFirstStudent.get(orderInfo.getUserId())) : userNameMap.get(orderInfo.getUserId()));
            list.add(orderInfo.getProductName());
            if (StringUtils.equals(orderInfo.getPaymentStatus(), PaymentStatus.Paid.name())) {
                list.add("已支付");
            } else if (StringUtils.equals(orderInfo.getPaymentStatus(), PaymentStatus.Refund.name())) {
                list.add("已退款");
            }
            list.add(SafeConverter.toString(orderInfo.getPayAmount()));
            if (orderInfo.getBookId() != null) {
                NewBookProfile newBookProfile = newContentLoaderClient.loadBook(orderInfo.getBookId());
                if (newBookProfile != null) {
                    list.add(SafeConverter.toString(newBookProfile.getClazzLevel()));
                    if (newBookProfile.getTermType() == 1) {
                        list.add("上学期");
                    } else if (newBookProfile.getTermType() == 2) {
                        list.add("下学期");
                    }
                    list.add(newBookProfile.getSubjectId() != null ? Subject.fromSubjectId(newBookProfile.getSubjectId()).getValue() : "");
                }
            } else {
                list.add("");
                list.add("");
                list.add("");
            }
            list.add(orderInfo.getServiceStartTime() != null && orderInfo.getServiceEndTime() != null ? SafeConverter.toString(DateUtils.dayDiff(orderInfo.getServiceEndTime(), orderInfo.getServiceStartTime())) : "");
            list.add(DateUtils.dateToString(orderInfo.getOrderCreateTime()));
            StudentDetail studentDetail = studentDetailMap.get(orderInfo.getUserId()) != null ? studentDetailMap.get(orderInfo.getUserId()) : studentDetailMap.get(parentFirstStudent.get(orderInfo.getUserId()));
            if (studentDetail != null) {
                Integer studentSchoolRegionCode = studentDetail.getStudentSchoolRegionCode();
                if (studentSchoolRegionCode != null) {
                    ExRegion regionFromBuffer = raikouSystem.loadRegion(studentSchoolRegionCode);
                    list.add(regionFromBuffer.getCityName());
                }
            } else {
                list.add("");
            }
            dataList.add(list);
        }
        return dataList;
    }


    private Map<String, Object> generateOrderCountDetail(List<PicOrderInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        BigDecimal orderMoneyCount = new BigDecimal(0);
        BigDecimal backMoneyCount = new BigDecimal(0);
        Long backOrderCount = 0L;
        Long orderCount = 0L;
        Map<String, Object> returnMap = new HashMap<>();
        for (PicOrderInfo picOrderInfo : list) {
            if (picOrderInfo.getPaymentStatus() == null || picOrderInfo.getPayAmount() == null) {
                continue;
            }
            if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
                //取所有的付款金额
                if (StringUtils.equals(picOrderInfo.getPaymentStatus(), PaymentStatus.Paid.name())) {
                    orderMoneyCount = orderMoneyCount.add(picOrderInfo.getPayAmount());
                    orderCount++;
                }
                //取所有的退款金额
                if (StringUtils.equals(picOrderInfo.getPaymentStatus(), PaymentStatus.Refund.name())) {
                    backMoneyCount = backMoneyCount.add(picOrderInfo.getPayAmount());
                    backOrderCount++;
                }
            } else {
                //取所有的付款金额
                if (StringUtils.equals(picOrderInfo.getPaymentStatus(), PaymentStatus.Paid.name()) && picOrderInfo.getPayAmount().compareTo(new BigDecimal(0.01)) > 0.01) {
                    orderMoneyCount = orderMoneyCount.add(picOrderInfo.getPayAmount());
                    orderCount++;
                }

                //取所有的退款金额
                if (StringUtils.equals(picOrderInfo.getPaymentStatus(), PaymentStatus.Refund.name()) && picOrderInfo.getPayAmount().compareTo(new BigDecimal(0.01)) > 0.01) {
                    backMoneyCount = backMoneyCount.add(picOrderInfo.getPayAmount());
                    backOrderCount++;
                }
            }
        }
        returnMap.put("orderCount", orderCount);
        returnMap.put("backOrderCount", backOrderCount);
        returnMap.put("backMoneyCount", backMoneyCount);
        returnMap.put("orderMoneyCount", orderMoneyCount);

        return returnMap;
    }

    private Map<String, Object> generateOrderCountDetailByHistory(List<UserOrderAmortizeHistory> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        BigDecimal orderMoneyCount = new BigDecimal(0);
        BigDecimal backMoneyCount = new BigDecimal(0);
        Long backOrderCount = 0L;
        Long orderCount = 0L;
        Map<String, Object> returnMap = new HashMap<>();
        Set<String> orderIds = new HashSet<>();
        for (UserOrderAmortizeHistory history : list) {
            if (history.getPaymentStatus() == null || history.getPayAmount() == null) {
                continue;
            }
            if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
                //取所有的付款金额
                if (history.getPaymentStatus() == PaymentStatus.Paid) {
                    orderMoneyCount = orderMoneyCount.add(history.getPayAmount());
                    if (orderIds.add(history.getOrderId())) {
                        orderCount++;
                    }
                }
                //取所有的退款金额
                if (history.getPaymentStatus() == PaymentStatus.Refund) {
                    backMoneyCount = backMoneyCount.add(history.getPayAmount());
                    if (orderIds.add(history.getOrderId())) {
                        orderCount++;
                    }
                    backOrderCount++;
                }
            } else {
                //取所有的付款金额
                if (history.getPaymentStatus() == PaymentStatus.Paid && history.getPayAmount().compareTo(new BigDecimal(0.01)) > 0.01) {
                    orderMoneyCount = orderMoneyCount.add(history.getPayAmount());
                    if (orderIds.add(history.getOrderId())) {
                        orderCount++;
                    }
                }

                //取所有的退款金额
                if (history.getPaymentStatus() == PaymentStatus.Refund && history.getPayAmount().compareTo(new BigDecimal(0.01)) > 0.01) {
                    backMoneyCount = backMoneyCount.add(history.getPayAmount());
                    if (orderIds.add(history.getOrderId())) {
                        orderCount++;
                    }
                    backOrderCount++;
                }
            }
        }
        returnMap.put("orderCount", orderCount);
        returnMap.put("backOrderCount", backOrderCount);
        returnMap.put("backMoneyCount", backMoneyCount);
        returnMap.put("orderMoneyCount", orderMoneyCount);

        return returnMap;
    }


    private List<Map<String, Object>> generatePicOrderDetailByHistory(List<UserOrderAmortizeHistory> historyList) {
        if (CollectionUtils.isEmpty(historyList)) {
            return Collections.emptyList();
        }
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            historyList = historyList.stream().filter(e -> !e.getDisabled()).collect(Collectors.toList());
        } else {
            historyList = historyList.stream().filter(e -> !e.getDisabled() && e.getPayAmount().compareTo(new BigDecimal(0.01)) > 0.01).collect(Collectors.toList());
        }
        List<Long> userIds = historyList.stream().map(UserOrderAmortizeHistory::getUserId).collect(Collectors.toList());
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
        Map<Long, User> studentMap = new HashMap<>();
        Map<Long, User> parentMap = new HashMap<>();
        userMap.entrySet().stream().filter(map -> map.getValue().isParent()).forEach(map -> parentMap.put(map.getKey(), map.getValue()));
        userMap.entrySet().stream().filter(map -> map.getValue().isStudent()).forEach(map -> studentMap.put(map.getKey(), map.getValue()));
        Set<Long> studentIds = new HashSet<>(studentMap.keySet());
        Map<Long, List<StudentParentRef>> parentStudentRefs = parentLoaderClient.loadParentStudentRefs(parentMap.keySet());
        Map<Long, Long> parentFirstStudent = new HashMap<>();
        parentStudentRefs.entrySet().stream().filter(entry -> CollectionUtils.isNotEmpty(entry.getValue())).forEach(entry -> parentFirstStudent.put(entry.getKey(), entry.getValue().get(0).getStudentId()));
        studentIds.addAll(parentFirstStudent.values());
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);
        Map<Long, String> userNameMap = studentDetailMap.values().stream().collect(Collectors.toMap(User::getId, User::fetchRealname));
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (UserOrderAmortizeHistory history : historyList) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", history.getOrderId());
            map.put("productName", history.getProductName());
            map.put("userName", userNameMap.get(parentFirstStudent.get(history.getUserId())) != null ? userNameMap.get(parentFirstStudent.get(history.getUserId())) : userNameMap.get(history.getUserId()));
            map.put("payAmount", history.getPayAmount());
            if (history.getPaymentStatus() == PaymentStatus.Paid) {
                map.put("payStatus", "已支付");
            } else if (history.getPaymentStatus() == PaymentStatus.Refund) {
                map.put("payStatus", "已退款");
            }
            map.put("serviceTime", history.getServiceStartTime() != null && history.getServiceEndTime() != null ? DateUtils.dayDiff(history.getServiceEndTime(), history.getServiceStartTime()) : "");
            map.put("orderCreateTime", DateUtils.dateToString(history.getPayDatetime()));
            StudentDetail studentDetail = studentDetailMap.get(history.getUserId()) != null ? studentDetailMap.get(history.getUserId()) : studentDetailMap.get(parentFirstStudent.get(history.getUserId()));
            if (studentDetail != null) {
                Integer studentSchoolRegionCode = studentDetail.getStudentSchoolRegionCode();
                if (studentSchoolRegionCode != null) {
                    ExRegion regionFromBuffer = raikouSystem.loadRegion(studentSchoolRegionCode);
                    map.put("cityName", regionFromBuffer.getCityName());
                }
            }
            if (StringUtils.isNotBlank(history.getProductItemId())) {
                OrderProductItem orderProductItem = userOrderLoaderClient.loadOrderProductItemById(history.getProductItemId());
                if (orderProductItem != null) {
                    NewBookProfile newBookProfile = newContentLoaderClient.loadBook(orderProductItem.getAppItemId());
                    if (newBookProfile != null) {
                        map.put("clazzLevel", newBookProfile.getClazzLevel());
                        if (newBookProfile.getTermType() == 1) {
                            map.put("termType", "上学期");
                        } else if (newBookProfile.getTermType() == 2) {
                            map.put("termType", "下学期");
                        }
                        map.put("subject", Subject.fromSubjectId(newBookProfile.getSubjectId()).getValue());
                    }
                }

            }
            if (history.getOrderProductServiceType() != null) {
                map.put("productType", history.getOrderProductServiceType());
            }
            mapList.add(map);
        }
        return mapList;
    }

    private Set<String> generatePublisherBookProduct(String shortPublisherName) {
        if (StringUtils.isBlank(shortPublisherName)) {
            return Collections.emptySet();
        }
        Set<String> bookIds = textBookManagementLoader.getTextBookManagementList().stream().filter(e -> StringUtils.equals(shortPublisherName, e.getShortPublisherName())).map(TextBookManagement::getBookId).collect(Collectors.toSet());
        Set<String> productIds = new HashSet<>();
        Map<String, List<OrderProduct>> productByAppItemIds = userOrderLoaderClient.loadOrderProductByAppItemIds(bookIds);
        productByAppItemIds.forEach((itemId, productList) -> productList.forEach(product -> productIds.add(product.getId())));

        return productIds;
    }

    private List<List<String>> generateDownLoadDataFormHistory(Date queryTime, Collection<String> productIds) {
        List<UserOrderAmortizeHistory> histories = picListenBookOrderService.downloadDetailData(queryTime, productIds);
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            histories = histories.stream().filter(e -> !e.getDisabled()).collect(Collectors.toList());
        } else {
            histories = histories.stream().filter(e -> !e.getDisabled() && e.getPayAmount().compareTo(new BigDecimal(0.01)) > 0.01).collect(Collectors.toList());
        }
        List<Long> userIds = histories.stream().map(UserOrderAmortizeHistory::getUserId).collect(Collectors.toList());
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
        Map<Long, User> studentMap = new HashMap<>();
        Map<Long, User> parentMap = new HashMap<>();
        userMap.entrySet().stream().filter(map -> map.getValue().isParent()).forEach(map -> parentMap.put(map.getKey(), map.getValue()));
        userMap.entrySet().stream().filter(map -> map.getValue().isStudent()).forEach(map -> studentMap.put(map.getKey(), map.getValue()));
        Set<Long> studentIds = new HashSet<>();
        studentIds.addAll(studentMap.keySet());
        Map<Long, List<StudentParentRef>> parentStudentRefs = parentLoaderClient.loadParentStudentRefs(parentMap.keySet());
        Map<Long, Long> parentFirstStudent = new HashMap<>();
        parentStudentRefs.entrySet().stream().filter(entry -> CollectionUtils.isNotEmpty(entry.getValue())).forEach(entry -> parentFirstStudent.put(entry.getKey(), entry.getValue().get(0).getStudentId()));
        studentIds.addAll(parentFirstStudent.values());
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);
        Map<Long, String> userNameMap = studentDetailMap.values().stream().collect(Collectors.toMap(User::getId, User::fetchRealname));


        List<List<String>> dataList = new ArrayList<>();
        for (UserOrderAmortizeHistory history : histories) {
            List<String> list = new ArrayList<>();
            list.add(history.getOrderId());
            list.add(userNameMap.get(parentFirstStudent.get(history.getUserId())) != null ? userNameMap.get(parentFirstStudent.get(history.getUserId())) : userNameMap.get(history.getUserId()));
            list.add(history.getProductName());
            if (history.getPaymentStatus() == PaymentStatus.Paid) {
                list.add("已支付");
            } else if (history.getPaymentStatus() == PaymentStatus.Refund) {
                list.add("已退款");
            }
            list.add(SafeConverter.toString(history.getPayAmount().setScale(2, BigDecimal.ROUND_DOWN)));
            if (StringUtils.isNotBlank(history.getProductItemId())) {
                OrderProductItem orderProductItem = userOrderLoaderClient.loadOrderProductItemById(history.getProductItemId());
                if (orderProductItem != null) {
                    NewBookProfile newBookProfile = newContentLoaderClient.loadBook(orderProductItem.getAppItemId());
                    if (newBookProfile != null) {
                        list.add(SafeConverter.toString(newBookProfile.getClazzLevel()));
                        if (newBookProfile.getTermType() == 1) {
                            list.add("上学期");
                        } else if (newBookProfile.getTermType() == 2) {
                            list.add("下学期");
                        }
                        list.add(newBookProfile.getSubjectId() != null ? Subject.fromSubjectId(newBookProfile.getSubjectId()).getValue() : "");
                    }
                } else {
                    list.add("");
                    list.add("");
                    list.add("");
                }
            } else {
                list.add("");
                list.add("");
                list.add("");
            }
            list.add(history.getServiceStartTime() != null && history.getServiceEndTime() != null ? SafeConverter.toString(DateUtils.dayDiff(history.getServiceEndTime(), history.getServiceStartTime())) : "");
            list.add(DateUtils.dateToString(history.getPayDatetime()));
            StudentDetail studentDetail = studentDetailMap.get(history.getUserId()) != null ? studentDetailMap.get(history.getUserId()) : studentDetailMap.get(parentFirstStudent.get(history.getUserId()));
            if (studentDetail != null) {
                Integer studentSchoolRegionCode = studentDetail.getStudentSchoolRegionCode();
                if (studentSchoolRegionCode != null) {
                    ExRegion regionFromBuffer = raikouSystem.loadRegion(studentSchoolRegionCode);
                    list.add(regionFromBuffer.getCityName());
                } else {
                    list.add("");
                }
            } else {
                list.add("");
            }

            if (history.getOrderProductServiceType() != null) {
                if (OrderProductServiceType.safeParse(history.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook) {
                    list.add("点读机");
                } else if (OrderProductServiceType.safeParse(history.getOrderProductServiceType()) == OrderProductServiceType.WalkerMan) {
                    list.add("随身听");
                } else {
                    list.add("");
                }
            } else {
                list.add("");
            }
            dataList.add(list);
        }
        return dataList;
    }
}
