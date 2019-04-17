package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.galaxy.service.coin.api.DPCoinService;
import com.voxlearning.galaxy.service.coin.api.entity.CoinHistory;
import com.voxlearning.galaxy.service.coin.api.support.CoinHistoryBuilder;
import com.voxlearning.galaxy.service.mall.api.DPCommodityOrderLoader;
import com.voxlearning.galaxy.service.mall.api.DPCommodityOrderService;
import com.voxlearning.galaxy.service.mall.api.constant.*;
import com.voxlearning.galaxy.service.mall.api.entity.CommodityOrder;
import com.voxlearning.galaxy.service.mall.api.entity.CommodityOrderImportHistory;
import com.voxlearning.galaxy.service.mall.api.entity.CommodityOrderLog;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CommodityOrderMapper;
import com.voxlearning.utopia.admin.util.XssfUtils;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author malong
 * @since 2018/06/15
 */
@Controller
@RequestMapping(value = "opmanager/commodity/order")
@Slf4j
public class CommodityOrderManager extends AbstractAdminSystemController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = DPCommodityOrderLoader.class)
    private DPCommodityOrderLoader dpCommodityOrderLoader;
    @ImportService(interfaceClass = DPCommodityOrderService.class)
    private DPCommodityOrderService dpCommodityOrderService;
    @ImportService(interfaceClass = DPCoinService.class)
    private DPCoinService dpCoinService;
    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        int page = getRequestInt("page", 1);
        if (page <= 0) {
            page = 1;
        }
        OrderQueryMessage queryMessage = queryMessageBuilder(model);
        Pageable pageable = new PageRequest(page - 1, 10);
        Page<CommodityOrder> orderPage = dpCommodityOrderLoader.crmLoadOrder(queryMessage, pageable);
        List<CommodityOrderMapper> orderMappers = new ArrayList<>();
        if (orderPage != null) {
            model.addAttribute("currentPage", orderPage.getTotalPages() < page ? 1 : page);
            model.addAttribute("totalPage", orderPage.getTotalPages());
            model.addAttribute("hasPrev", orderPage.hasPrevious());
            model.addAttribute("hasNext", orderPage.hasNext());
            if (CollectionUtils.isNotEmpty(orderPage.getContent())) {
                orderPage.getContent().stream()
                        .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                        .forEach(order -> orderMappers.add(convert(order)));
            }
        }

        Map<String, Object> orderStatusMap = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            orderStatusMap.put(status.name(), status.getDesc());
        }
        Map<String, Object> categoryMap = new LinkedHashMap<>();
        for (CommodityCategory category : CommodityCategory.values()) {
            categoryMap.put(category.name(), category.getDesc());
        }
        Map<String, Object> sendWayMap = new LinkedHashMap<>();
        for (SendWay sendWay : SendWay.values()) {
            sendWayMap.put(sendWay.name(), sendWay.getDesc());
        }
        Map<String, Object> sendStatusMap = new LinkedHashMap<>();
        for (SendStatus sendStatus : SendStatus.values()) {
            sendStatusMap.put(sendStatus.name(), sendStatus.getDesc());
        }

        model.addAttribute("orderStatusMap", orderStatusMap);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("sendWayMap", sendWayMap);
        model.addAttribute("sendStatusMap", sendStatusMap);
        model.addAttribute("orderList", orderMappers);

        return "opmanager/commodity/order/list";
    }

    @RequestMapping(value = "getOrder.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getOrder() {
        String orderId = getRequestString("orderId");
        CommodityOrder order = dpCommodityOrderLoader.loadById(orderId);
        Map<String, Object> map = new HashMap<>();
        Integer categoryLevel = 1;
        if (order != null) {
            map.put("aOrderId", order.getId());
            String phone = SensitiveLib.decodeMobile(order.getPhone());
            map.put("aPhone", phone);
            map.put("aCommodityName", order.getCommodityName());
            map.put("aSendStatus", order.getSendStatus());
            map.put("aSendWay", order.getSendWay());
            map.put("aLogisticCode", order.getLogisticsCode());
            map.put("aRemark", order.getRemark());
            categoryLevel = order.getCommodityCategory().getLevel();
        }
        return MapMessage.successMessage().add("order", map).add("categoryLevel", categoryLevel);
    }

    @RequestMapping(value = "getPhone.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getPhone() {
        String encodePhone = getRequestString("encode_phone");
        String phone = StringUtils.isNotBlank(encodePhone) ? SensitiveLib.decodeMobile(encodePhone) : "";
        return MapMessage.successMessage().add("phone", phone);
    }

    @RequestMapping(value = "saveOrder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveOrder() {
        String id = getRequestString("id");
        CommodityOrder order = dpCommodityOrderLoader.loadById(id);
        if (order == null) {
            return MapMessage.errorMessage("订单不存在，订单id：" + id);
        }
        String sendStatus = getRequestString("sendStatus");
        SendStatus status = SendStatus.parse(sendStatus);
        String sendWay = getRequestString("sendWay");
        SendWay way = SendWay.parse(sendWay);
        String logisticsCode = getRequestString("logisticsCode");
        MapMessage mapMessage = validateOrderInfo(order, status, way, logisticsCode);
        if (mapMessage.isSuccess()) {
            String remark = getRequestString("remark");
            if (remark.length() > 200) {
                return MapMessage.errorMessage("备注过长");
            }
            order.setSendStatus(status);
            order.setSendWay(way);
            order.setLogisticsCode(logisticsCode);
            order.setRemark(remark);
            dpCommodityOrderService.saveCommodityOrder(order, getCurrentAdminUser().getAdminUserName());
        } else {
            return mapMessage;
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "returnCoin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage returnCoin() {
        String id = getRequestString("orderId");
        CommodityOrder order = dpCommodityOrderLoader.loadById(id);
        if (order == null) {
            return MapMessage.errorMessage("订单不存在，订单id：" + id);
        }
        if (order.getOrderStatus() != OrderStatus.PAID) {
            return MapMessage.errorMessage("订单不是已付币状态");
        }
        Integer coinType;
        if (RuntimeMode.current().gt(Mode.TEST)) {
            coinType = 25;
        } else {
            coinType = 40;
        }
        try {
            CoinHistory coinHistory = new CoinHistoryBuilder().withUserId(order.getUserId())
                    .withType(coinType)
                    .withCount(order.getCoin())
                    .build();
            MapMessage mapMessage = dpCoinService.changeCoin(coinHistory);
            if (mapMessage.isSuccess()) {
                order.setOrderStatus(OrderStatus.CANCEL);
                CommodityOrder modified = dpCommodityOrderService.saveCommodityOrder(order, getCurrentAdminUser().getAdminUserName());
                if (modified != null) {
                    return MapMessage.successMessage();
                }
            }
            return mapMessage;
        } catch (Exception ex) {
            logger.error("return coin error, orderId:{}, studentId:{}", id, order.getUserId(), ex);
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        String id = getRequestString("orderId");
        CommodityOrder order = dpCommodityOrderLoader.loadById(id);
        if (order == null) {
            return "opmanager/commodity/order/list";
        }
        Map<String, Object> map = new HashMap<>();
        //兑换单信息
        map.put("orderId", order.getId());
        map.put("orderStatus", order.getOrderStatus() == null ? "" : order.getOrderStatus().getDesc());
        map.put("createDate", DateUtils.dateToString(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        map.put("finishDate", DateUtils.dateToString(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));

        //商品信息
        CommodityCategory category = order.getCommodityCategory();
        map.put("commodityId", order.getCommodityId());
        map.put("commodityName", order.getCommodityName());
        map.put("commodityCategory", category.getDesc());
        map.put("purchase", order.getPurchase());
        map.put("dispatchPrice", order.getDispatchPrice());
        map.put("coin", order.getCoin());

        generateUserInfo(map, order);
        model.addAttribute("order", map);
        return "opmanager/commodity/order/detail";
    }

    @RequestMapping(value = "logList.vpage", method = RequestMethod.GET)
    public String logList(Model model) {
        String orderId = getRequestString("id");
        List<CommodityOrderLog> orderLogs = dpCommodityOrderLoader.loadOrderLogs(orderId)
                .stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
        List<Map<String, Object>> list = new ArrayList<>();
        for (CommodityOrderLog orderLog : orderLogs) {
            Map<String, Object> map = new HashMap<>();
            map.put("opType", orderLog.getOperateType().getDesc());
            map.put("opList", orderLog.getOpList());
            map.put("opDate", DateUtils.dateToString(orderLog.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            map.put("operator", orderLog.getOperator());
            map.put("remark", orderLog.getRemark());
            list.add(map);
        }
        model.addAttribute("logs", list);
        return "opmanager/commodity/order/log";
    }

    @RequestMapping(value = "templateExcel.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage generateTemplateFile() {
        String templateUrl;
        String cacheKey = "COMMODITY_ORDER_TEMPLATE_FILE";
        CacheObject<String> cacheObject = CacheSystem.CBS.getCache("persistence").get(cacheKey);
        if (cacheObject != null && cacheObject.getValue() != null) {
            templateUrl = SafeConverter.toString(cacheObject.getValue());
            return MapMessage.successMessage().add("templateUrl", templateUrl);
        }
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("订单ID");
        row.createCell(1).setCellValue("寄送方式");
        row.createCell(2).setCellValue("运单号");
        row.createCell(3).setCellValue("寄送状态");
        row = sheet.createRow(1);
        row.createCell(0).setCellValue(100000001);
        row.createCell(1).setCellValue(1);
        row.createCell(2).setCellValue("619859187499");
        row.createCell(3).setCellValue(1);

        row = sheet.createRow(10);
        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        Cell cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("备注：导入时去掉表头，案例和备注即可；虚拟商品无需填写寄送方式和运单号");

        row = sheet.createRow(11);
        row.createCell(0).setCellValue("1.寄送方式填写对应编号即可，详情如下");
        row = sheet.createRow(12);
        row.createCell(0).setCellValue("寄送方式编号");
        row.createCell(1).setCellValue("寄送方式");
        int rowNum = 13;
        for (SendWay sendWay : SendWay.values()) {
            row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(sendWay.getCode());
            row.createCell(1).setCellValue(sendWay.getDesc());
            rowNum++;
        }

        row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue("2.寄送状态填写对应编号即可，详情如下");
        rowNum++;
        row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue("寄送状态编号");
        row.createCell(1).setCellValue("寄送状态");
        for (SendStatus sendStatus : SendStatus.values()) {
            row = sheet.createRow(++rowNum);
            row.createCell(0).setCellValue(sendStatus.getCode());
            row.createCell(1).setCellValue(sendStatus.getDesc());
        }

        try {
            @Cleanup ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            byte[] content = os.toByteArray();
            @Cleanup InputStream is = new ByteArrayInputStream(content);
            String env = "mall/";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = "mall/test/";
            }
            String fileName = "批量导入运单模板.xls";
            String realName = storageClient.upload(is, fileName, env);
            templateUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
            CacheSystem.CBS.getCache("persistence").set(cacheKey, 0, templateUrl);
            return MapMessage.successMessage().add("templateUrl", templateUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return MapMessage.errorMessage("获取模板文件失败, excp:{}", e);
        }
    }

    @RequestMapping(value = "batchImport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchImport() {
        CommodityOrderImportHistory history = new CommodityOrderImportHistory();
        XSSFWorkbook workbook = readExcel("order_file", history);
        if (workbook == null) {
            return MapMessage.errorMessage("不支持的文件类型或者上传失败");
        }
        List<Map<String, Object>> dataList = getDataList(workbook);
        List<String> orderIds = dataList.stream().map(data -> SafeConverter.toString(data.get("orderId"))).collect(Collectors.toList());
        Map<String, CommodityOrder> orderMap = dpCommodityOrderLoader.loadByIds(orderIds);
        List<Map<String, Object>> failList = new ArrayList<>();
        String operator = getCurrentAdminUser().getAdminUserName();
        for (Map<String, Object> map : dataList) {
            String orderId = SafeConverter.toString(map.get("orderId"));
            Integer sendWay = SafeConverter.toInt(map.get("sendWay"));
            String logisticsCode = SafeConverter.toString(map.get("logisticsCode"), "");
            Integer sendStatus = SafeConverter.toInt(map.get("sendStatus"));

            SendStatus status = SendStatus.parseFromCode(sendStatus);
            if (status == null) {
                failList.add(recordFailInfo(orderId, sendWay, logisticsCode, sendStatus, "寄送状态错误"));
                continue;
            }
            CommodityOrder order = orderMap.get(orderId);
            if (order == null) {
                failList.add(recordFailInfo(orderId, sendWay, logisticsCode, sendStatus, "订单不存在"));
            } else {
                if (order.getCommodityCategory().getLevel() == 1) {
                    //只有实物商品需要寄送方式和运单号
                    SendWay way = SendWay.parseFromCode(sendWay);
                    if (way == null) {
                        failList.add(recordFailInfo(orderId, sendWay, logisticsCode, sendStatus, "寄送方式错误"));
                        continue;
                    }
                    order.setSendWay(way);
                    order.setLogisticsCode(logisticsCode);
                    if (status.getCode() <= 3) {
                        failList.add(recordFailInfo(orderId, sendWay, logisticsCode, sendStatus, "寄送状态错误"));
                    }
                } else {
                    //虚拟商品寄送状态不能大于3
                    if (status.getCode() > 3) {
                        failList.add(recordFailInfo(orderId, sendWay, logisticsCode, sendStatus, "寄送状态错误"));
                    }
                }
                order.setSendStatus(status);
                CommodityOrder modified = dpCommodityOrderService.saveCommodityOrder(order, operator);
                if (modified == null) {
                    failList.add(recordFailInfo(orderId, sendWay, logisticsCode, sendStatus, "订单数据更新失败"));
                }
            }
        }

        String fileUrl = "";
        if (CollectionUtils.isNotEmpty(failList)) {
            fileUrl = generateExcel(failList);
        }
        history.setId(RandomUtils.nextObjectId());
        history.setOpType(OrderBatchType.BATCH_IMPORT);
        history.setOperator(operator);
        history.setUrl(fileUrl);
        dpCommodityOrderService.saveImportHistory(history);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "batchOutput.vpage", method = RequestMethod.GET)
    @ResponseBody
    public void batchOutput() {
        String orderIds = getRequestString("orderIds");
        OrderQueryMessage queryMessage = new OrderQueryMessage();
        if (StringUtils.isNotBlank(orderIds)) {
            String[] orderIdArr = orderIds.split(",");
            queryMessage.setOrderIds(Arrays.asList(orderIdArr));
        } else {
            String category = getRequestString("category");
            String startDateStr = getRequestString("startDate");
            String endDateStr = getRequestString("endDate");
            CommodityCategory commodityCategory = CommodityCategory.parse(category);
            Date startDate = null;
            Date endDate = null;
            if (StringUtils.isNotBlank(startDateStr)) {
                startDate = DateUtils.stringToDate(startDateStr, "yyyy-MM-dd");
            }
            if (StringUtils.isNotBlank(endDateStr)) {
                endDate = DateUtils.stringToDate(endDateStr, "yyyy-MM-dd");
            }
            if (commodityCategory != null) {
                queryMessage.setCommodityCategory(category);
                queryMessage.setStartDate(startDate);
                queryMessage.setEndDate(endDate);
            }
        }

        List<CommodityOrder> orders = dpCommodityOrderLoader.crmQueryOrderList(queryMessage);
        if (CollectionUtils.isNotEmpty(orders)) {
            CommodityCategory commodityCategory = orders.get(0).getCommodityCategory();
            CommodityOrderImportHistory history = new CommodityOrderImportHistory();
            history.setId(RandomUtils.nextObjectId());
            history.setOpType(OrderBatchType.BATCH_OUTPUT);
            history.setOperator(getCurrentAdminUser().getAdminUserName());
            XSSFWorkbook workbook = generateOutExcel(orders, commodityCategory);
            String fileName = "批量导出运单-" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss") + ".xlsx";
            try {
                @Cleanup ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                outputStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outputStream.toByteArray()
                );
                history.setUrl("已导出");
                dpCommodityOrderService.saveImportHistory(history);
            } catch (Exception ex) {
                history.setUrl("导出失败");
                dpCommodityOrderService.saveImportHistory(history);
            }

        }
    }

    @RequestMapping(value = "batchList.vpage", method = RequestMethod.GET)
    public String batchList(Model model) {
        int page = getRequestInt("page", 1);
        if (page <= 0) {
            page = 1;
        }
        Pageable pageable = new PageRequest(page - 1, 10);
        List<CommodityOrderImportHistory> histories = dpCommodityOrderLoader.loadAll()
                .stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
        List<Map<String, Object>> list = new ArrayList<>();
        for (CommodityOrderImportHistory history : histories) {
            Map<String, Object> map = new HashMap<>();
            map.put("opType", history.getOpType().getDesc());
            map.put("fileName", history.getFileName());
            map.put("date", DateUtils.dateToString(history.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            map.put("url", history.getUrl());
            map.put("operator", history.getOperator());
            list.add(map);
        }
        Page<Map<String, Object>> batchPage = PageableUtils.listToPage(list, pageable);
        model.addAttribute("batchPage", batchPage);
        model.addAttribute("currentPage", batchPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", batchPage.getTotalPages());
        model.addAttribute("hasPrev", batchPage.hasPrevious());
        model.addAttribute("hasNext", batchPage.hasNext());
        return "opmanager/commodity/order/batch";
    }

    private MapMessage validateOrderInfo(CommodityOrder order, SendStatus sendStatus, SendWay sendWay, String logisticsCode) {
        if (sendStatus == null) {
            return MapMessage.errorMessage("寄送状态错误");
        }
        if (sendWay == null) {
            return MapMessage.errorMessage("寄送方式错误");
        }

        CommodityCategory category = order.getCommodityCategory();
        if (category.getLevel() != 1) {
            if (sendStatus.getCode() > 3) {
                return MapMessage.errorMessage("虚拟商品寄送状态错误");
            }
            if (sendWay.getCode() != 1) {
                return MapMessage.errorMessage("虚拟商品寄送方式错误");
            }
        } else {
            if (sendStatus.getCode() <= 3) {
                return MapMessage.errorMessage("实物商品寄送状态错误");
            }
            if (sendWay.getCode() == 1) {
                return MapMessage.errorMessage("实物商品寄送方式错误");
            }
            if (StringUtils.isBlank(logisticsCode)) {
                return MapMessage.errorMessage("实物商品需要填写运单号");
            }
        }
        return MapMessage.successMessage();
    }

    private XSSFWorkbook generateOutExcel(List<CommodityOrder> orders, CommodityCategory category) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        int rowNum = 0;
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("订单ID");
        row.createCell(1).setCellValue("订单状态");
        row.createCell(2).setCellValue("学生ID");
        if (category.getLevel() == 1) {
            row.createCell(3).setCellValue("收件人姓名");
            row.createCell(4).setCellValue("联系方式");
            row.createCell(5).setCellValue("省");
            row.createCell(6).setCellValue("市");
            row.createCell(7).setCellValue("区");
            row.createCell(8).setCellValue("详细地址");
            row.createCell(9).setCellValue("寄送方式");
            row.createCell(10).setCellValue("配送费");
            row.createCell(11).setCellValue("运单号");
            row.createCell(12).setCellValue("寄送状态");
            row.createCell(13).setCellValue("学习币数量");
            row.createCell(14).setCellValue("采购价");
            row.createCell(15).setCellValue("商品分类");
            row.createCell(16).setCellValue("商品名称");
            row.createCell(17).setCellValue("订单备注");
        } else if (category.getLevel() == 2) {
            row.createCell(3).setCellValue("学生姓名");
            row.createCell(4).setCellValue("家长电话");
            row.createCell(5).setCellValue("年龄");
            row.createCell(6).setCellValue("年级");
            row.createCell(7).setCellValue("寄送方式");
            row.createCell(8).setCellValue("寄送状态");
            row.createCell(9).setCellValue("配送费");
            row.createCell(10).setCellValue("学习币数量");
            row.createCell(11).setCellValue("采购价");
            row.createCell(12).setCellValue("商品分类");
            row.createCell(13).setCellValue("商品名称");
            row.createCell(14).setCellValue("订单备注");
        } else {
            row.createCell(3).setCellValue("学生姓名");
            row.createCell(4).setCellValue("电话");
            row.createCell(5).setCellValue("寄送方式");
            row.createCell(6).setCellValue("寄送状态");
            row.createCell(7).setCellValue("配送费");
            row.createCell(8).setCellValue("学习币数量");
            row.createCell(9).setCellValue("采购价");
            row.createCell(10).setCellValue("商品分类");
            row.createCell(11).setCellValue("商品名称");
            row.createCell(12).setCellValue("订单备注");
        }

        for (CommodityOrder order : orders) {
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getId());
            row.createCell(1).setCellValue(order.getOrderStatus() == null ? "" : order.getOrderStatus().getDesc());
            row.createCell(2).setCellValue(order.getUserId());
            if (category.getLevel() == 1) {
                row.createCell(3).setCellValue(order.getUserName());
                row.createCell(4).setCellValue(SensitiveLib.decodeMobile(order.getPhone()));
                ExRegion exRegion = raikouSystem.loadRegion(order.getCountyCode());
                row.createCell(5).setCellValue(exRegion == null ? "" : exRegion.getProvinceName());
                row.createCell(6).setCellValue(exRegion == null ? "" : exRegion.getCityName());
                row.createCell(7).setCellValue(exRegion == null ? "" : exRegion.getCountyName());
                row.createCell(8).setCellValue(safeConverterString(order.getAddress()));
                row.createCell(9).setCellValue(order.getSendWay() == null ? "" : order.getSendWay().getDesc());
                row.createCell(10).setCellValue(order.getDispatchPrice());
                row.createCell(11).setCellValue(safeConverterString(order.getLogisticsCode()));
                row.createCell(12).setCellValue(order.getSendStatus() == null ? "" : order.getSendStatus().getDesc());
                row.createCell(13).setCellValue(order.getCoin());
                row.createCell(14).setCellValue(order.getPurchase());
                row.createCell(15).setCellValue(order.getCommodityCategory() == null ? "" : order.getCommodityCategory().getDesc());
                row.createCell(16).setCellValue(order.getCommodityName());
                row.createCell(17).setCellValue(safeConverterString(order.getRemark()));
            } else if (category.getLevel() == 2) {
                Student student = studentLoaderClient.loadStudent(order.getUserId());
                row.createCell(3).setCellValue(student == null ? "" : student.fetchRealname());
                row.createCell(4).setCellValue(SensitiveLib.decodeMobile(order.getPhone()));
                row.createCell(5).setCellValue(order.getAge());
                row.createCell(6).setCellValue(getClazzLevel(order.getClazzLevel()));
                row.createCell(7).setCellValue(order.getSendStatus() == null ? "" : order.getSendStatus().getDesc());
                row.createCell(8).setCellValue(order.getSendWay() == null ? "" : order.getSendWay().getDesc());
                row.createCell(9).setCellValue(order.getDispatchPrice());
                row.createCell(10).setCellValue(order.getCoin());
                row.createCell(11).setCellValue(order.getPurchase());
                row.createCell(12).setCellValue(order.getCommodityCategory() == null ? "" : order.getCommodityCategory().getDesc());
                row.createCell(13).setCellValue(order.getCommodityName());
                row.createCell(14).setCellValue(order.getRemark());
            } else {
                Student student = studentLoaderClient.loadStudent(order.getUserId());
                row.createCell(3).setCellValue(student == null ? "" : student.fetchRealname());
                row.createCell(4).setCellValue(SensitiveLib.decodeMobile(order.getPhone()));
                row.createCell(5).setCellValue(order.getSendStatus() == null ? "" : order.getSendStatus().getDesc());
                row.createCell(6).setCellValue(order.getSendWay() == null ? "" : order.getSendWay().getDesc());
                row.createCell(7).setCellValue(order.getDispatchPrice());
                row.createCell(8).setCellValue(order.getCoin());
                row.createCell(9).setCellValue(order.getPurchase());
                row.createCell(10).setCellValue(order.getCommodityCategory() == null ? "" : order.getCommodityCategory().getDesc());
                row.createCell(11).setCellValue(order.getCommodityName());
                row.createCell(12).setCellValue(order.getRemark());
            }
        }

        return workbook;
    }

    private String safeConverterString(Object value) {
        return SafeConverter.toString(value, "");
    }

    private String generateExcel(List<Map<String, Object>> failList) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        int rowNum = 0;
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("订单ID");
        row.createCell(1).setCellValue("寄送方式");
        row.createCell(2).setCellValue("运单号");
        row.createCell(3).setCellValue("寄送状态");
        row.createCell(4).setCellValue("错误原因");
        for (Map<String, Object> failInfo : failList) {
            String orderId = SafeConverter.toString(failInfo.get("orderId"), "");
            Integer sendWay = SafeConverter.toInt(failInfo.get("sendWay"));
            String logisticsCode = SafeConverter.toString(failInfo.get("logisticsCode"), "");
            Integer sendStatus = SafeConverter.toInt(failInfo.get("sendStatus"));
            String reason = SafeConverter.toString(failInfo.get("reason"));
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(orderId);
            row.createCell(1).setCellValue(sendWay);
            row.createCell(2).setCellValue(logisticsCode);
            row.createCell(3).setCellValue(sendStatus);
            row.createCell(4).setCellValue(reason);
        }
        return uploadExcel(workbook);
    }

    private String uploadExcel(XSSFWorkbook workbook) {
        try {
            @Cleanup ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            byte[] content = os.toByteArray();
            @Cleanup InputStream is = new ByteArrayInputStream(content);
            String env = "mall/";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = "mall/test/";
            }
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + ".xls";
            String realName = storageClient.upload(is, fileName, path);
            return StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private Map<String, Object> recordFailInfo(String orderId, Integer sendWay, String logisticsCode, Integer sendStatus, String reason) {
        Map<String, Object> failMap = new HashMap<>();
        failMap.put("orderId", orderId);
        failMap.put("sendWay", sendWay);
        failMap.put("logisticsCode", logisticsCode);
        failMap.put("sendStatus", sendStatus);
        failMap.put("reason", reason);
        return failMap;
    }

    private List<Map<String, Object>> getDataList(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        List<Map<String, Object>> list = new ArrayList<>();
        int rowIndex = 0;
        while (true) {
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", XssfUtils.getStringCellValue(row.getCell(0)));
            map.put("sendWay", XssfUtils.getStringCellValue(row.getCell(1)));
            map.put("logisticsCode", XssfUtils.getStringCellValue(row.getCell(2)));
            map.put("sendStatus", XssfUtils.getStringCellValue(row.getCell(3)));
            list.add(map);
            rowIndex++;
        }
        return list;
    }

    //读excel数据
    private XSSFWorkbook readExcel(String name, CommodityOrderImportHistory history) {
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
            if (history != null) {
                history.setFileName(fileName);
            }
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

    //收件信息
    private void generateUserInfo(Map<String, Object> map, CommodityOrder order) {
        CommodityCategory category = order.getCommodityCategory();
        map.put("phone", SensitiveLib.decodeMobile(order.getPhone()));
        if (category.getLevel() == 1) {
            map.put("userName", order.getUserName());
            ExRegion exRegion = raikouSystem.loadRegion(order.getCountyCode());
            String address = "";
            if (exRegion != null) {
                address += exRegion.getProvinceName() + exRegion.getCityName() + exRegion.getCountyName() + order.getAddress();
            }
            map.put("address", address);
            map.put("logisticsCode", order.getLogisticsCode());
        } else if (category.getLevel() == 2) {
            map.put("userName", order.getUserName());
            map.put("age", order.getAge());
            map.put("clazzLevel", getClazzLevel(order.getClazzLevel()));
        }
        map.put("sendWay", order.getSendWay() == null ? "" : order.getSendWay().getDesc());
        map.put("sendStatus", order.getSendStatus() == null ? "" : order.getSendStatus().getDesc());
        map.put("remark", order.getRemark());
        map.put("categoryLevel", order.getCommodityCategory() == null ? 1 : order.getCommodityCategory().getLevel());
    }

    private String getClazzLevel(Integer clazzLevel) {
        for (ClazzLevel level : ClazzLevel.values()) {
            if (level.getLevel() == clazzLevel) {
                return level.getDescription();
            }
        }
        return "";
    }

    private OrderQueryMessage queryMessageBuilder(Model model) {
        String phone = getRequestString("phone");
        Long studentId = getRequestLong("studentId");
        String orderId = getRequestString("orderId");
        String orderStatus = getRequestString("orderStatus");
        String category = getRequestString("category");
        String logisticsCode = getRequestString("logisticsCode");
        String sendWay = getRequestString("sendWay");
        String sendStatus = getRequestString("sendStatus");
        String startDateStr = getRequestString("startDate");
        String endDateStr = getRequestString("endDate");
        if (StringUtils.isNotBlank(phone)) {
            model.addAttribute("phone", phone);
        }
        if (studentId > 0) {
            model.addAttribute("studentId", studentId);
        }
        if (StringUtils.isNotBlank(orderId)) {
            model.addAttribute("orderId", orderId);
        }
        if (StringUtils.isNotBlank(orderStatus)) {
            model.addAttribute("orderStatus", orderStatus);
        }
        if (StringUtils.isNotBlank(category)) {
            model.addAttribute("category", category);
        }
        if (StringUtils.isNotBlank(logisticsCode)) {
            model.addAttribute("logisticsCode", logisticsCode);
        }
        if (StringUtils.isNotBlank(sendWay)) {
            model.addAttribute("sendWay", sendWay);
        }
        if (StringUtils.isNotBlank(sendStatus)) {
            model.addAttribute("sendStatus", sendStatus);
        }
        if (StringUtils.isNotBlank(startDateStr)) {
            model.addAttribute("startDateStr", startDateStr);
        }
        if (StringUtils.isNotBlank(endDateStr)) {
            model.addAttribute("endDateStr", endDateStr);
        }

        OrderQueryMessage queryMessage = new OrderQueryMessage();
        queryMessage.setPhone(phone);
        queryMessage.setUserId(studentId);
        if (StringUtils.isNotBlank(orderId)) {
            queryMessage.setOrderIds(Collections.singletonList(orderId));
        }
        queryMessage.setOrderStatus(orderStatus);
        queryMessage.setCommodityCategory(category);
        queryMessage.setLogisticsCode(logisticsCode);
        queryMessage.setSendWay(sendWay);
        queryMessage.setSendStatus(sendStatus);
        if (StringUtils.isNotBlank(startDateStr)) {
            Date startDate = DateUtils.stringToDate(startDateStr, "yyyy-MM-dd HH:mm");
            queryMessage.setStartDate(startDate);
        }
        if (StringUtils.isNotBlank(endDateStr)) {
            Date endDate = DateUtils.stringToDate(endDateStr, "yyyy-MM-dd HH:mm");
            queryMessage.setEndDate(endDate);
        }
        return queryMessage;
    }

    private CommodityOrderMapper convert(CommodityOrder order) {
        CommodityOrderMapper mapper = new CommodityOrderMapper();
        mapper.setId(order.getId());
        mapper.setStudentId(order.getUserId());
        mapper.setPhone(order.getPhone());
        mapper.setSendStatus(order.getSendStatus() == null ? "" : order.getSendStatus().getDesc());
        mapper.setSendWay(order.getSendWay() == null ? "" : order.getSendWay().getDesc());
        mapper.setLogisticsCode(order.getLogisticsCode());
        mapper.setCoin(order.getCoin());
        mapper.setCommodityName(order.getCommodityName());
        mapper.setCommodityCategory(order.getCommodityCategory() == null ? "" : order.getCommodityCategory().getDesc());
        mapper.setCategoryLevel(order.getCommodityCategory() == null ? 1 : order.getCommodityCategory().getLevel());
        mapper.setOrderStatus(order.getOrderStatus() == null ? "" : order.getOrderStatus().getDesc());
        return mapper;
    }
}
