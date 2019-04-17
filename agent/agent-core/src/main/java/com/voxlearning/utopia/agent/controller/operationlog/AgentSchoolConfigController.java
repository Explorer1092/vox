package com.voxlearning.utopia.agent.controller.operationlog;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.bean.export.DataIsEmptyException;
import com.voxlearning.utopia.agent.bean.export.XSSFWorkbookExportService;
import com.voxlearning.utopia.agent.bean.showtable.ShowTableInfoService;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentSchoolConfigLog;
import com.voxlearning.utopia.agent.service.export.XSSFWorkbookExportServiceImpl;
import com.voxlearning.utopia.agent.service.log.AgentSchoolConfigLogService;
import com.voxlearning.utopia.agent.service.table.ShowTableInfoServiceImpl;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by yaguang.wang
 * on 2017/3/28.
 */
@Controller
@RequestMapping("/operationlog/schoolconfig")
@Slf4j
public class AgentSchoolConfigController extends AbstractAgentController {
    private static final String EXPORT_CONFIG_SCHOOL_LOG_TEM = "/config/templates/export_config_school_log_template.xlsx";

    @Inject private AgentSchoolConfigLogService agentSchoolConfigLogService;

    @RequestMapping(value = "logpage.vpage", method = RequestMethod.GET)
    public String schoolConfigPage(Model model) {
        model.addAttribute("startDate", DateUtils.dateToString(DateUtils.getFirstDayOfMonth(new Date()), DateUtils.FORMAT_SQL_DATE));
        model.addAttribute("endDate", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
        return "/operationlog/schoolconfig";
    }

    @RequestMapping(value = "search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String searchLogByDay() {
        Date startDate = getRequestDate("startDate");
        Date endDate = getRequestDate("endDate");
        if (endDate != null) {
            endDate = DateUtils.getDayEnd(endDate);
        }
        List<AgentSchoolConfigLog> logs = agentSchoolConfigLogService.findSchoolConfigLogByDay(startDate, endDate);
        ShowTableInfoService service = new ShowTableInfoServiceImpl(logs);
        return service.tableInfoToJson();
    }

    @RequestMapping(value = "exportlog.vpage", method = RequestMethod.GET)
    public void exportLog(HttpServletResponse response) {
        Date startDate = getRequestDate("startDate");
        Date endDate = getRequestDate("endDate");
        List<AgentSchoolConfigLog> logs = agentSchoolConfigLogService.findSchoolConfigLogByDay(startDate, endDate);
        String filename = "学校负责人调整-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
        XSSFWorkbookExportService xssfWorkbookExportService = new XSSFWorkbookExportServiceImpl(logs, EXPORT_CONFIG_SCHOOL_LOG_TEM, false);
        try {
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
            logger.error("export config school log  is failed", ex);
        }
    }
}
