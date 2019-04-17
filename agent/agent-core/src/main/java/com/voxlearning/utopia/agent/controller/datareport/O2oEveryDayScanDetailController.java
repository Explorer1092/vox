package com.voxlearning.utopia.agent.controller.datareport;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.export.DataIsEmptyException;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.agent.bean.export.XSSFWorkbookExportService;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.datareport.DataReportService;
import com.voxlearning.utopia.agent.service.export.XSSFWorkbookExportServiceImpl;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by yaguang.wang
 * on 2017/4/11.
 */
@Controller
@Slf4j
@RequestMapping("/workspace/everydayscan")
public class O2oEveryDayScanDetailController extends AbstractAgentController {

    private static final String EXPORT_SCHOOL_KLX_EVERYDAY_SCAN_DATA = "/config/templates/export_school_klx_everyday_scan_data.xlsx";
    private static final String EXPORT_TEACHER_KLX_EVERYDAY_SCAN_DATA = "/config/templates/export_teacher_klx_everyday_scan_data.xlsx";

    @Inject private DataReportService dataReportService;

    public static final String SCHOOL_SCAN_TYPE = "school";
    public static final String TEACHER_SCAN_TYPE = "teacher";

    @RequestMapping(value = "detail_page.vpage", method = RequestMethod.GET)
    public String everyDayScanDetailPage(Model model) {
        model.addAttribute("schoolType", SCHOOL_SCAN_TYPE);
        model.addAttribute("teacherType", TEACHER_SCAN_TYPE);
        String date = DateUtils.dateToString(DayUtils.addDay(new Date(), -1), "yyyy-MM");
        model.addAttribute("date", date);
        return "datareport/o2o_everyday_scan_detail";
    }

    @RequestMapping(value = "export_scan_detail.vpage", method = RequestMethod.GET)
    public void exportScanDetail(HttpServletResponse response) {
        String type = getRequestString("type");
        Date startDate = getRequestMonth("startDate");
        String filename = "";
        try {
            if (!getCurrentUser().isCountryManager()) {
                throw new DataIsEmptyException();
            }
            List<ExportAble> export = dataReportService.loadEveryDayScanDetail(type, startDate);
            XSSFWorkbookExportService xssfWorkbookExportService = null;
            if (Objects.equals(type, SCHOOL_SCAN_TYPE)) {
                filename = "快乐学学校每日扫描量" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
                xssfWorkbookExportService = new XSSFWorkbookExportServiceImpl(export, EXPORT_SCHOOL_KLX_EVERYDAY_SCAN_DATA);
            }
            if (Objects.equals(type, TEACHER_SCAN_TYPE)) {
                filename = "快乐学老师每日扫描量" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
                xssfWorkbookExportService = new XSSFWorkbookExportServiceImpl(export, EXPORT_TEACHER_KLX_EVERYDAY_SCAN_DATA);
            }
            //TODO 这里需要生成 Excel
            Workbook workbook = xssfWorkbookExportService.convertToSXSSFWorkbook();
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            assert workbook != null;
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());

            outStream.close();
            if (workbook instanceof SXSSFWorkbook){
                ((SXSSFWorkbook) workbook).dispose();
            }
        } catch (DataIsEmptyException ex) {
            try {
                response.getWriter().write("所查询的数据不存在");
            } catch (IOException ignored) {
            }
        } catch (Exception ex) {
            logger.error("export school dict info is failed", ex);
        }
    }
}
