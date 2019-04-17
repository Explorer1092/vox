package com.voxlearning.utopia.agent.controller.monitor;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.bean.export.DataIsEmptyException;
import com.voxlearning.utopia.agent.bean.export.XSSFWorkbookExportService;
import com.voxlearning.utopia.agent.bean.monitor.ClassAlterTableData;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.export.XSSFWorkbookExportServiceImpl;
import com.voxlearning.utopia.agent.service.montortool.MonitorToolService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 监控工具
 * Created by yaguang.wang
 * on 2017/7/28.
 */

@Controller
@RequestMapping("/monitor/class_alter")
@Slf4j
public class MonitorToolController extends AbstractAgentController {
    private final static String EXPORT_CLAZZ_ALTER_REPORT = "/config/templates/export_class_alter_data_report_template.xlsx";
    @Inject private MonitorToolService monitorToolService;

    @RequestMapping(value = "view.vpage", method = RequestMethod.GET)
    public String view() {
        return "rebuildViewDir/monitor/class_alter";
    }

    @RequestMapping(value = "download.vpage", method = RequestMethod.POST)
    public void download(HttpServletResponse response) {
        List<ClassAlterTableData> exportInfo = monitorToolService.exportClazzAlter();
        String filename = "换班任务统计" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
        try {
            XSSFWorkbookExportService xssfWorkbookExportService = new XSSFWorkbookExportServiceImpl(exportInfo, EXPORT_CLAZZ_ALTER_REPORT, true);
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
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                response.getWriter().write("所查询的数据不存在");
            } catch (IOException ignored) {
            }
        } catch (Exception ex) {
            logger.error("export clazz alter info is failed", ex);
        }
    }
}
