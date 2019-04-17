package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.invoice.AgentInvoiceService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * MaterialDetailController
 *
 * @author song.wang
 * @date 2016/9/13
 */
@Controller
@RequestMapping("/workspace/material")
@Slf4j
public class MaterialDetailController extends AbstractAgentController {

    @Inject
    private AgentInvoiceService agentInvoiceService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String workRecordIndex(Model model) {
        return "workspace/material/index";
    }

    @RequestMapping(value = "export_list.vpage")
    public void exportList() {
        Date date = requestDate("date");
        if(date == null){
            date = new Date();
        }
        Date startDate = DayUtils.getFirstDayOfMonth(date);
        Date endDate = DayUtils.getLastDayOfMonth(date);
        endDate = DateUtils.addDays(endDate, 1);
        try {
            List<Map<String, Object>> invoiceList = agentInvoiceService.searchMaterialDetailList(startDate, endDate);
            XSSFWorkbook workbook = generateWorkBook(invoiceList);
            if(workbook == null){
                getResponse().getWriter().write("下载失败");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String filename = "发货单列表" + DateUtils.dateToString(new Date(), "yyyy-MM-dd") + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                getResponse().getWriter().write("不能下载");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }catch (Exception e){

        }

    }

    private XSSFWorkbook generateWorkBook(List<Map<String, Object>> dataList) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            sheet.setColumnWidth(0, 2000);
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 15000);
            sheet.setColumnWidth(7, 5000);
            sheet.setColumnWidth(8, 5000);
            sheet.setColumnWidth(9, 5000);
            sheet.setColumnWidth(10, 5000);
            sheet.setColumnWidth(11, 5000);
            sheet.setColumnWidth(12, 5000);

            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFRow firstRow = sheet.createRow(0);
            XssfUtils.setCellValue(firstRow, 0, cellStyle, "序号");
            XssfUtils.setCellValue(firstRow, 1, cellStyle, "部门");
            XssfUtils.setCellValue(firstRow, 2, cellStyle, "人员");
            XssfUtils.setCellValue(firstRow, 3, cellStyle, "秋季总费用");
            XssfUtils.setCellValue(firstRow, 4, cellStyle, "订单号");
            XssfUtils.setCellValue(firstRow, 5, cellStyle, "申请日期");
            XssfUtils.setCellValue(firstRow, 6, cellStyle, "物流信息");
            XssfUtils.setCellValue(firstRow, 7, cellStyle, "申请品类");
            XssfUtils.setCellValue(firstRow, 8, cellStyle, "申请数量");
            XssfUtils.setCellValue(firstRow, 9, cellStyle, "单价");
            XssfUtils.setCellValue(firstRow, 10, cellStyle, "物料费用");
            XssfUtils.setCellValue(firstRow, 11, cellStyle, "快递费用");
            XssfUtils.setCellValue(firstRow, 12, cellStyle, "剩余费用");

            if (CollectionUtils.isNotEmpty(dataList)) {
                Integer index = 1;
                for (int no = 0; no < dataList.size(); no ++) {
                    Map<String, Object> item = dataList.get(no);
                    int userStartRowIndex = index;
                    Integer userRowCount = (Integer)item.get("userRowCount");
                    if(userRowCount != null){
                        for(int i = 0; i < userRowCount; i++){
                            sheet.createRow(index++);
                        }
                        mergeCell(sheet, userStartRowIndex,index -1, 0, 0);
                        mergeCell(sheet, userStartRowIndex,index -1, 1, 1);
                        mergeCell(sheet, userStartRowIndex,index -1, 2, 2);
                        mergeCell(sheet, userStartRowIndex,index -1, 3, 3);
                        mergeCell(sheet, userStartRowIndex,index -1, 11, 11);
                        mergeCell(sheet, userStartRowIndex,index -1, 12, 12);
                    }

                    XSSFRow row = sheet.getRow(userStartRowIndex);
                    XssfUtils.setCellValue(row, 0, cellStyle, no + 1);
                    XssfUtils.setCellValue(row, 1, cellStyle, item.get("groupName") == null ? "" : (String)item.get("groupName"));
                    XssfUtils.setCellValue(row, 2, cellStyle, item.get("userName") == null ? "" : (String)item.get("userName"));
                    Float materielBudget = (Float) item.get("materielBudget");
                    XssfUtils.setCellValue(row, 3, cellStyle, materielBudget == null ? 0 : materielBudget);
                    Float logisticsPrice = (Float) item.get("logisticsPrice");
                    XssfUtils.setCellValue(row, 11, cellStyle, logisticsPrice == null ? 0 : logisticsPrice);
                    Float cachAmount = (Float) item.get("usableCashAmount");
                    XssfUtils.setCellValue(row, 12, cellStyle, cachAmount == null ? 0 : cachAmount);


                    List<Map<String, Object>> orderMapList = (List<Map<String, Object>>) item.get("orderList");

                    if(CollectionUtils.isEmpty(orderMapList)){
                        continue;
                    }

                    int orderStartRow = userStartRowIndex;
                    for(Map<String, Object> orderMap : orderMapList){
                        XSSFRow orderRow = sheet.getRow(orderStartRow);
                        Integer orderRowCount = (Integer)orderMap.get("orderRowCount");
                        mergeCell(sheet, orderStartRow, orderStartRow + orderRowCount -1, 4, 4);
                        mergeCell(sheet, orderStartRow, orderStartRow + orderRowCount -1, 5, 5);
                        mergeCell(sheet, orderStartRow, orderStartRow + orderRowCount -1, 6, 6);

                        XssfUtils.setCellValue(orderRow, 4, cellStyle, (Long)orderMap.get("id"));
                        if(orderMap.get("orderTime") == null){
                            XssfUtils.setCellValue(orderRow, 5, cellStyle, DateUtils.dateToString((Date)orderMap.get("createDatetime"), "yyyy-MM-dd"));
                        }else{
                            XssfUtils.setCellValue(orderRow, 5, cellStyle, DateUtils.dateToString((Date)orderMap.get("orderTime"), "yyyy-MM-dd"));
                        }
                        XssfUtils.setCellValue(orderRow, 6, cellStyle, (String)orderMap.get("logisticsInfo"));

                        List<Map<String, Object>> orderProductMapList = (List<Map<String, Object>>) orderMap.get("productList");
                        if(CollectionUtils.isNotEmpty(orderProductMapList)){
                            int productStartRow = orderStartRow;
                            for(Map<String, Object> p : orderProductMapList){
                                XSSFRow productRow = sheet.getRow(productStartRow);
                                XssfUtils.setCellValue(productRow, 7, cellStyle, (String)p.get("productName"));
                                XssfUtils.setCellValue(productRow, 8, cellStyle, (Integer)p.get("productQuantity"));
                                Float productPrice = (Float) p.get("productPrice");
                                XssfUtils.setCellValue(productRow, 9, cellStyle, productPrice == null ? 0 : productPrice);
                                Double productCacheAmount = (Double) p.get("productCacheAmount");
                                XssfUtils.setCellValue(productRow, 10, cellStyle, productCacheAmount == null ? 0 : productCacheAmount);
                                productStartRow ++;
                            }
                        }


                        orderStartRow += orderRowCount;

                    }
                }
            }
            return workbook;
        } catch (Exception ex) {
            logger.error("generateWorkBook error:", ex);
            return null;
        }
    }

    private void mergeCell(XSSFSheet sheet, int startRow, int endRow, int startCol, int endCol){


        sheet.addMergedRegion(new CellRangeAddress(
                startRow, //first row (0-based)
                endRow, //last row  (0-based)
                startCol, //first column (0-based)
                endCol  //last column  (0-based)
        ));
    }

    @RequestMapping(value = "export_order_list.vpage")
    public void exportOrderList() {
        Date startDate = requestDate("orderStartDate");
        Date endDate = requestDate("orderEndDate");
        if(endDate == null){
            endDate = DateUtils.stringToDate(DateUtils.dateToString(new Date(), "yyyyMMdd"), "yyyyMMdd");
        }
        if(startDate == null){
            startDate = DayUtils.getFirstDayOfMonth(endDate);
        }
        Date tempDate = endDate;
        endDate = DateUtils.addDays(endDate, 1);
        try {
            List<Map<String, Object>> orderList = agentInvoiceService.searchOrderDetailList(startDate, endDate);
            XSSFWorkbook workbook = generateOrderWorkBook(orderList);
            if(workbook == null){
                getResponse().getWriter().write("下载失败");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String filename = "订单列表" + DateUtils.dateToString(startDate, "yyyy-MM-dd") + "--" + DateUtils.dateToString(tempDate, "yyyy-MM-dd") + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                getResponse().getWriter().write("不能下载");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }catch (Exception e){

        }
    }

    private XSSFWorkbook generateOrderWorkBook(List<Map<String, Object>> dataList) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 3000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 5000);
            sheet.setColumnWidth(7, 5000);
            sheet.setColumnWidth(8, 5000);
            sheet.setColumnWidth(9, 10000);
            sheet.setColumnWidth(10, 5000);
            sheet.setColumnWidth(11, 5000);
            sheet.setColumnWidth(12, 5000);
            sheet.setColumnWidth(13, 5000);
            sheet.setColumnWidth(14, 15000);

            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFRow firstRow = sheet.createRow(0);
            XssfUtils.setCellValue(firstRow, 0, cellStyle, "申请日期");
            XssfUtils.setCellValue(firstRow, 1, cellStyle, "订单号");
            XssfUtils.setCellValue(firstRow, 2, cellStyle, "部门");
            XssfUtils.setCellValue(firstRow, 3, cellStyle, "人员");
            XssfUtils.setCellValue(firstRow, 4, cellStyle, "申请品类");
            XssfUtils.setCellValue(firstRow, 5, cellStyle, "申请数量");
            XssfUtils.setCellValue(firstRow, 6, cellStyle, "单价");
            XssfUtils.setCellValue(firstRow, 7, cellStyle, "总价");
            XssfUtils.setCellValue(firstRow, 8, cellStyle, "合计");
            XssfUtils.setCellValue(firstRow, 9, cellStyle, "物流单号");
            XssfUtils.setCellValue(firstRow, 10, cellStyle, "秋季总费用");
            XssfUtils.setCellValue(firstRow, 11, cellStyle, "剩余费用");
            XssfUtils.setCellValue(firstRow, 12, cellStyle, "订单状态");
            XssfUtils.setCellValue(firstRow, 13, cellStyle, "支付方式");
            XssfUtils.setCellValue(firstRow, 14, cellStyle, "订单备注");

            if (CollectionUtils.isNotEmpty(dataList)) {
                Integer index = 1;
                for (int no = 0; no < dataList.size(); no ++) {
                    Map<String, Object> item = dataList.get(no);
                    int orderStartRowIndex = index;
                    Integer orderRowCount = (Integer)item.get("orderRowCount");
                    if(orderRowCount != null){
                        for(int i = 0; i < orderRowCount; i++){
                            sheet.createRow(index++);
                        }
                        mergeCell(sheet, orderStartRowIndex,index -1, 0, 0);
                        mergeCell(sheet, orderStartRowIndex,index -1, 1, 1);
                        mergeCell(sheet, orderStartRowIndex,index -1, 2, 2);
                        mergeCell(sheet, orderStartRowIndex,index -1, 3, 3);
                        mergeCell(sheet, orderStartRowIndex,index -1, 8, 8);
                        mergeCell(sheet, orderStartRowIndex,index -1, 9, 9);
                        mergeCell(sheet, orderStartRowIndex,index -1, 10, 10);
                        mergeCell(sheet, orderStartRowIndex,index -1, 11, 11);
                        mergeCell(sheet, orderStartRowIndex,index -1, 12, 12);
                        mergeCell(sheet, orderStartRowIndex,index -1, 13, 13);
                    }

                    XSSFRow row = sheet.getRow(orderStartRowIndex);
                    XssfUtils.setCellValue(row, 0, cellStyle, item.get("orderTime") == null? "" : DateUtils.dateToString((Date)item.get("orderTime"), "yyyy-MM-dd"));
                    XssfUtils.setCellValue(row, 1, cellStyle, (Long)item.get("id"));
                    XssfUtils.setCellValue(row, 2, cellStyle, item.get("groupName") == null ? "" : (String)item.get("groupName"));
                    XssfUtils.setCellValue(row, 3, cellStyle, item.get("creatorName") == null ? "" : (String)item.get("creatorName"));

                    Float orderAmount = (Float) item.get("orderAmount");
                    XssfUtils.setCellValue(row, 8, cellStyle, orderAmount == null ? 0 : orderAmount);
                    XssfUtils.setCellValue(row, 9, cellStyle, (String)item.get("logisticsInfo"));

                    Float materielBudget = (Float) item.get("materielBudget");
                    XssfUtils.setCellValue(row, 10, cellStyle, materielBudget == null ? 0 : materielBudget);
                    Float cachAmount = (Float) item.get("usableCashAmount");
                    XssfUtils.setCellValue(row, 11, cellStyle, cachAmount == null ? 0 : cachAmount);
                    Object applyStatus = item.get("applyStatus");
                    XssfUtils.setCellValue(row, 12, cellStyle, applyStatus == null ? "" : String.valueOf(applyStatus));
                    Integer paymentMode = (Integer) item.get("paymentMode");
                    String paymentModeStr = "";
                    if(paymentMode != null){
                        if(paymentMode == 1){
                            paymentModeStr = "物料费用";
                        }else if(paymentMode == 2){
                            paymentModeStr = "城市支持费用";
                            Integer cityCostMonth = (Integer) item.get("cityCostMonth");
                            if(cityCostMonth != null){
                                paymentModeStr += "(" + cityCostMonth + "月)";
                            }
                        }else if(paymentMode == 3){
                            paymentModeStr = "自付";
                        }
                    }
                    XssfUtils.setCellValue(row, 13, cellStyle, paymentModeStr);
                    XssfUtils.setCellValue(row, 14, cellStyle, SafeConverter.toString(item.get("orderNotes")));

                    List<Map<String, Object>> orderProductMapList = (List<Map<String, Object>>) item.get("productList");
                    if(CollectionUtils.isNotEmpty(orderProductMapList)){
                        int productStartRow = orderStartRowIndex;
                        for(Map<String, Object> p : orderProductMapList){
                            XSSFRow productRow = sheet.getRow(productStartRow);
                            XssfUtils.setCellValue(productRow, 4, cellStyle, (String)p.get("productName"));
                            XssfUtils.setCellValue(productRow, 5, cellStyle, (Integer)p.get("productQuantity"));
                            Float productPrice = (Float) p.get("productPrice");
                            XssfUtils.setCellValue(productRow, 6, cellStyle, productPrice == null ? 0 : productPrice);
                            Double productCacheAmount = (Double) p.get("productCacheAmount");
                            XssfUtils.setCellValue(productRow, 7, cellStyle, productCacheAmount == null ? 0 : productCacheAmount);
                            productStartRow ++;
                        }
                    }
                }
            }
            return workbook;
        } catch (Exception ex) {
            logger.error("generateWorkBook error:", ex);
            return null;
        }
    }



    @RequestMapping(value = "export_invoice_list.vpage")
    public void exportInvoiceList() {
        Date startDate = requestDate("invoiceStartDate");
        Date endDate = requestDate("invoiceEndDate");
        if(endDate == null){
            endDate = DateUtils.stringToDate(DateUtils.dateToString(new Date(), "yyyyMMdd"), "yyyyMMdd");
        }
        if(startDate == null){
            startDate = DayUtils.getFirstDayOfMonth(endDate);
        }
        Date tempDate = endDate;
        endDate = DateUtils.addDays(endDate, 1);
        try {
            List<Map<String, Object>> invoiceList = agentInvoiceService.searchInvoiceDetailList(startDate, endDate);
            XSSFWorkbook workbook = generateInvoiceWorkBook(invoiceList);
            if(workbook == null){
                getResponse().getWriter().write("下载失败");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String filename = "发货单列表" + DateUtils.dateToString(startDate, "yyyy-MM-dd") + "--" + DateUtils.dateToString(tempDate, "yyyy-MM-dd") + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                getResponse().getWriter().write("不能下载");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }catch (Exception e){

        }

    }

    private XSSFWorkbook generateInvoiceWorkBook(List<Map<String, Object>> dataList) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 3000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 5000);
            sheet.setColumnWidth(7, 5000);
            sheet.setColumnWidth(8, 10000);
            sheet.setColumnWidth(9, 5000);
            sheet.setColumnWidth(10, 5000);
            sheet.setColumnWidth(11, 5000);

            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFRow firstRow = sheet.createRow(0);
            XssfUtils.setCellValue(firstRow, 0, cellStyle, "创建日期");
            XssfUtils.setCellValue(firstRow, 1, cellStyle, "发货单号");
            XssfUtils.setCellValue(firstRow, 2, cellStyle, "收货人");
            XssfUtils.setCellValue(firstRow, 3, cellStyle, "收货人电话");
            XssfUtils.setCellValue(firstRow, 4, cellStyle, "省");
            XssfUtils.setCellValue(firstRow, 5, cellStyle, "市");
            XssfUtils.setCellValue(firstRow, 6, cellStyle, "区");
            XssfUtils.setCellValue(firstRow, 7, cellStyle, "收货人地址");
            XssfUtils.setCellValue(firstRow, 8, cellStyle, "物流单号");
            XssfUtils.setCellValue(firstRow, 9, cellStyle, "物流公司");
            XssfUtils.setCellValue(firstRow, 10, cellStyle, "物流费用");
            XssfUtils.setCellValue(firstRow, 11, cellStyle, "订单号");

            if (CollectionUtils.isNotEmpty(dataList)) {
                Integer index = 1;
                for (int no = 0; no < dataList.size(); no ++) {
                    Map<String, Object> item = dataList.get(no);
                    int currentRowIndex = index;
                    sheet.createRow(index++);


                    XSSFRow row = sheet.getRow(currentRowIndex);
                    XssfUtils.setCellValue(row, 0, cellStyle, item.get("createDatetime") == null? "" : DateUtils.dateToString((Date)item.get("createDatetime"), "yyyy-MM-dd"));
                    XssfUtils.setCellValue(row, 1, cellStyle, (Long)item.get("id"));

                    XssfUtils.setCellValue(row, 2, cellStyle, item.get("consignee") == null ? "" : (String)item.get("consignee"));
                    XssfUtils.setCellValue(row, 3, cellStyle, item.get("mobile") == null ? "" : (String)item.get("mobile"));
                    XssfUtils.setCellValue(row, 4, cellStyle, SafeConverter.toString(item.get("province"), ""));
                    XssfUtils.setCellValue(row, 5, cellStyle, SafeConverter.toString(item.get("city"), ""));
                    XssfUtils.setCellValue(row, 6, cellStyle, SafeConverter.toString(item.get("county"), ""));
                    XssfUtils.setCellValue(row, 7, cellStyle, item.get("address") == null ? "" : (String)item.get("address"));
                    XssfUtils.setCellValue(row, 8, cellStyle, item.get("logisticsId") == null ? "" : (String)item.get("logisticsId"));
                    XssfUtils.setCellValue(row, 9, cellStyle, item.get("logisticsCompany") == null ? "" : (String)item.get("logisticsCompany"));
                    Float logisticsPrice = (Float) item.get("logisticsPrice");
                    XssfUtils.setCellValue(row, 10, cellStyle, logisticsPrice == null ? 0 : logisticsPrice);
                    XssfUtils.setCellValue(row, 11, cellStyle, item.get("orderIdList") == null ? "" : (String)item.get("orderIdList"));
                }
            }
            return workbook;
        } catch (Exception ex) {
            logger.error("generateWorkBook error:", ex);
            return null;
        }
    }


}
