package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.constants.AgentLogisticsStatus;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.invoice.AgentInvoiceService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * InvoiceController
 *
 * @author song.wang
 * @date 2016/9/7
 */
@Controller
@RequestMapping("/workspace/invoice")
@Slf4j
public class InvoiceController extends AbstractAgentController {

    @Inject
    private AgentInvoiceService agentInvoiceService;
    @Inject
    private BaseExcelService baseExcelService;

    @RequestMapping("/list.vpage")
    @OperationCode("d5b594db30984684")
    public String index(Model model) {
        Long invoiceId = getRequestLong("invoiceId"); // 发货单ID
        String logisticsId = getRequestString("logisticsId");// 物流单号
        AgentLogisticsStatus logisticsStatus = AgentLogisticsStatus.nameOf(getRequestString("logisticsStatus")); // 物流状态
        Date startDate = requestDate("startDate", DateUtils.addDays(new Date(), -30));
        Date endDate = requestDate("endDate");
        List<Map<String, Object>> invoiceList = agentInvoiceService.searchInvoice(invoiceId, logisticsId, logisticsStatus, startDate, endDate == null ? null : DateUtils.addDays(endDate, 1));
        model.addAttribute("invoiceList", invoiceList);
        model.addAttribute("invoiceId", invoiceId == 0 ? null : invoiceId);
        model.addAttribute("logisticsId", logisticsId);
        model.addAttribute("logisticsStatus", logisticsStatus);
        model.addAttribute("startDate", startDate == null ? null : formatDate(startDate));
        model.addAttribute("endDate", endDate == null ? null : formatDate(endDate));
        model.addAttribute("logisticsStatusList", AgentLogisticsStatus.values());
        return "workspace/invoice/invoice";
    }

    @RequestMapping("/generate_invoice.vpage")
    @ResponseBody
    public MapMessage generateInvoice() {
        agentInvoiceService.createInvoiceFromOrder();
        return MapMessage.successMessage();
    }


    @RequestMapping(value = "download_template.vpage")
    public void downloadTemplate() {
        baseExcelService.downloadTemplate("/config/templates/invoice_logistics_template.xlsx", "发货单物流信息" + DateUtils.dateToString(new Date(), "yyyy-MM-dd"));
    }

    @RequestMapping(value = "import_logistics_info.vpage")
    @ResponseBody
    public MapMessage importLogisticsInfo() {
        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "sourceExcelFile");
        if (workbook == null) {
            return MapMessage.errorMessage("导入失败");
        }
        return agentInvoiceService.updateLogisticsInfoFromExcel(workbook);
    }

    @RequestMapping(value = "export_list.vpage")
    public void exportList() {
        Long invoiceId = getRequestLong("invoiceId"); // 发货单ID
        String logisticsId = getRequestString("logisticsId");// 物流单号
        AgentLogisticsStatus logisticsStatus = AgentLogisticsStatus.nameOf(getRequestString("logisticsStatus")); // 物流状态
        Date startDate = requestDate("startDate");
        Date endDate = requestDate("endDate");
        try {
            List<Map<String, Object>> invoiceList = agentInvoiceService.searchInvoice(invoiceId, logisticsId, logisticsStatus, startDate, endDate);
            XSSFWorkbook workbook = generateWorkBook(invoiceList);
            if (workbook == null) {
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
        } catch (Exception e) {

        }

    }


    private XSSFWorkbook generateWorkBook(List<Map<String, Object>> invoiceList) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 8000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 15000);
            sheet.setColumnWidth(6, 5000);
            sheet.setColumnWidth(7, 5000);
            sheet.setColumnWidth(8, 5000);
            sheet.setColumnWidth(9, 5000);
            sheet.setColumnWidth(10, 5000);
            sheet.setColumnWidth(11, 15000);
            sheet.setColumnWidth(12, 8000);
            sheet.setColumnWidth(13, 8000);

            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            XSSFRow firstRow = sheet.createRow(0);
            XssfUtils.setCellValue(firstRow, 0, cellStyle, "发货单ID");
            XssfUtils.setCellValue(firstRow, 1, cellStyle, "物流公司");
            XssfUtils.setCellValue(firstRow, 2, cellStyle, "物流单号");
            XssfUtils.setCellValue(firstRow, 3, cellStyle, "物流价格（元）");

            XssfUtils.setCellValue(firstRow, 4, cellStyle, "发货日期");
            XssfUtils.setCellValue(firstRow, 5, cellStyle, "订单编号");


            XssfUtils.setCellValue(firstRow, 6, cellStyle, "物流状态");
            XssfUtils.setCellValue(firstRow, 7, cellStyle, "物料明细");
            XssfUtils.setCellValue(firstRow, 8, cellStyle, "收货人");
            XssfUtils.setCellValue(firstRow, 9, cellStyle, "收货人电话");
            XssfUtils.setCellValue(firstRow, 10, cellStyle, "省");
            XssfUtils.setCellValue(firstRow, 11, cellStyle, "市");
            XssfUtils.setCellValue(firstRow, 12, cellStyle, "区");
            XssfUtils.setCellValue(firstRow, 13, cellStyle, "收货人地址");

            if (CollectionUtils.isNotEmpty(invoiceList)) {
                Integer index = 1;
                for (Map<String, Object> item : invoiceList) {
                    XSSFRow row = sheet.createRow(index++);
                    XssfUtils.setCellValue(row, 0, cellStyle, (Long) item.get("id"));
                    XssfUtils.setCellValue(row, 1, cellStyle, (String) item.get("logisticsCompany"));
                    XssfUtils.setCellValue(row, 2, cellStyle, (String) item.get("logisticsId"));
                    Float logisticsPrice = (Float) item.get("logisticsPrice");
                    XssfUtils.setCellValue(row, 3, cellStyle, logisticsPrice == null ? "" : String.valueOf(logisticsPrice));

                    XssfUtils.setCellValue(row, 4, cellStyle, item.get("deliveryDate") == null ? "":DateUtils.dateToString((Date) item.get("deliveryDate"), "yyyy-MM-dd"));

                    XssfUtils.setCellValue(row, 5, cellStyle, SafeConverter.toString(item.get("orderIds"), ""));

                    XssfUtils.setCellValue(row, 6, cellStyle, ((AgentLogisticsStatus) item.get("logisticsStatus")).getValue());

                    StringBuilder productInfo = new StringBuilder("");
                    String productInfoStr = "";
                    List<Map<String, Object>> productList = (List<Map<String, Object>>) item.get("productList");
                    if (CollectionUtils.isNotEmpty(productList)) {
                        for (Map<String, Object> product : productList) {
                            productInfo.append("，  ").append((String) product.get("productName")).append("*").append((Integer) product.get("productQuantity"));
                        }
                        productInfoStr = productInfo.substring(1);
                    }
                    XssfUtils.setCellValue(row, 7, cellStyle, productInfoStr);
                    XssfUtils.setCellValue(row, 8, cellStyle, (String) item.get("consignee"));
                    XssfUtils.setCellValue(row, 9, cellStyle, (String) item.get("mobile"));
                    XssfUtils.setCellValue(row, 10, cellStyle, SafeConverter.toString(item.get("province"), ""));
                    XssfUtils.setCellValue(row, 11, cellStyle, SafeConverter.toString(item.get("city"), ""));
                    XssfUtils.setCellValue(row, 12, cellStyle, SafeConverter.toString(item.get("county"), ""));
                    XssfUtils.setCellValue(row, 13, cellStyle, (String) item.get("address"));
                }
            }
            return workbook;
        } catch (Exception ex) {
            logger.error("generateWorkBook error:", ex);
            return null;
        }
    }

    @RequestMapping(value = "revocation_invoice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage revocationInvoice() {
        Long invoiceId = getRequestLong("invoiceId");
        return agentInvoiceService.revocationInvoice(invoiceId);
    }

}
