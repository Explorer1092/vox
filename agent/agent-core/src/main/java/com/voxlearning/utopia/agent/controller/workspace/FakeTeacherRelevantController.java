package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.bean.CancleFakeTeacherInfo;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.workspace.FakeTeacherRelevantService;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 老师取消判假记录
 * Created by zt on 2017/3/8.
 */
@Controller
@RequestMapping("/workspace/faketeacher")
@Slf4j
public class FakeTeacherRelevantController extends AbstractAgentController {

    @Inject private FakeTeacherRelevantService fakeTeacherRelevantService;
    /**
     * 取消老师判假操作列表
     */
    @RequestMapping(value = "/cancel_faketeacher.vpage", method = RequestMethod.GET)
    public String cancelFaketeacher(Model model){
        Date startDate  =  getRequestDate("startDate")==null?DateUtils.stringToDate("2013-01-01",DateUtils.FORMAT_SQL_DATE):getRequestDate("startDate");
        Date endDate  =  getRequestDate("endDate")==null?(new Date()):getRequestDate("endDate");
        if(getRequestDate("startDate")!=null){
            model.addAttribute("startDate",startDate);
            model.addAttribute("endDate",endDate);
        }
        model.addAttribute("cancelFaketeacherList",fakeTeacherRelevantService.cancleFakeTeacherList(startDate,endDate));
        return "workspace/faketeacher/cancel_faketeacher";
    }

    /**
     *下载曲啸老师判假操作exl
     */
    @RequestMapping(value = "/dowmload_cancel_faketeacher.vpage", method = RequestMethod.GET)
    public void dowmloadCancelFakeTeacher(HttpServletResponse response){
        try{
          //  Date startDate  =  getRequestDate("startDate")==null?DateUtils.addDays(new Date(),-30):getRequestDate("startDate");
            Date startDate  =  getRequestDate("startDate")==null?DateUtils.stringToDate("2013-01-01",DateUtils.FORMAT_SQL_DATE):getRequestDate("startDate");
            Date endDate  =  getRequestDate("endDate")==null?(new Date()):getRequestDate("endDate");
            List<CancleFakeTeacherInfo> usrList = fakeTeacherRelevantService.cancleFakeTeacherList(startDate,endDate);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet schoolSheet = workbook.createSheet("进校");
            schoolSheet.setColumnWidth(0, 4000);
            schoolSheet.setColumnWidth(1, 5000);
            schoolSheet.setColumnWidth(2, 5000);
            schoolSheet.setColumnWidth(3, 4000);
            schoolSheet.setColumnWidth(4, 5000);
            schoolSheet.setColumnWidth(5, 5000);
            schoolSheet.setColumnWidth(6, 10000);
            HSSFCellStyle borderStyle = workbook.createCellStyle();
            // 设置单元格边框样式
            borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
            borderStyle.setBorderTop(CellStyle.BORDER_THIN);
            borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
            borderStyle.setBorderRight(CellStyle.BORDER_THIN);
            Row firstRow = HssfUtils.createRow(schoolSheet, 0, 6, borderStyle);
            HssfUtils.setCellValue(firstRow, 0, borderStyle, "日期");
            HssfUtils.setCellValue(firstRow, 1, borderStyle, "大区");
            HssfUtils.setCellValue(firstRow, 2, borderStyle, "部门");
            HssfUtils.setCellValue(firstRow, 3, borderStyle, "操作人");
            HssfUtils.setCellValue(firstRow, 4, borderStyle, "老师id");
            HssfUtils.setCellValue(firstRow, 5, borderStyle, "老师姓名");
            HssfUtils.setCellValue(firstRow, 6, borderStyle, "取消判假原因");

            int rowNum = 1 ;
            for(CancleFakeTeacherInfo usr : usrList){
                Row row = HssfUtils.createRow(schoolSheet, rowNum, 6, borderStyle);
                HssfUtils.setCellValue(row, 0, borderStyle, usr.getCancleDate());
                HssfUtils.setCellValue(row, 1, borderStyle, usr.getRegion());
                HssfUtils.setCellValue(row, 2, borderStyle, usr.getDepartment());
                HssfUtils.setCellValue(row, 3, borderStyle, usr.getOperationName());
                HssfUtils.setCellValue(row, 4, borderStyle,usr.getTeacherId());
                HssfUtils.setCellValue(row, 5, borderStyle, usr.getTeacherName());//DD 教师姓名
                HssfUtils.setCellValue(row, 6, borderStyle, usr.getReason());
                rowNum++;
            }
            String filename =null;
            if(getRequestDate("startDate") ==null){
                 filename = "取消判假老师记录下载(所有).xls";
            }else{
                filename = "取消判假老师记录下载(" + DateUtils.dateToString(startDate, DateUtils.FORMAT_SQL_DATE)+"-"+ DateUtils.dateToString(endDate, DateUtils.FORMAT_SQL_DATE) + ").xls";
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception ex) {
            log.error("下载失败!", ex.getMessage(), ex);
        }
    }

}
