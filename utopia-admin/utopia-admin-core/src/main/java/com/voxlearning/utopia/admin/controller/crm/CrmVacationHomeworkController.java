package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamForExport;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamRegistrationLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewVacationHomeworkPackagePanorama;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/crm/vacation/homework")
public class CrmVacationHomeworkController extends CrmAbstractController {
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private VacationHomeworkReportLoaderClient vacationHomeworkReportLoaderClient;
    @Inject
    private NewExamRegistrationLoaderClient newExamRegistrationLoaderClient;
    @Inject
    private VacationHomeworkServiceClient vacationHomeworkServiceClient;


    @RequestMapping(value = "export.vpage", method = RequestMethod.GET)
    public void exportNewExamRecord(HttpServletResponse response) {
        String examId = getRequestString("examId");
        String fileName = "模考" + examId + "报告.xls";
        List<NewExamForExport> examForExports = newExamRegistrationLoaderClient.loadByNewExam(examId);
        String[] titles = {"学生ID", "学生姓名", "所在学校", "所在班级", "开始考试时间", "交卷时间", "成绩"};
        exportExcel(fileName, titles, response, examForExports);
    }

    private void exportExcel(String fileName, Object[] title, HttpServletResponse response, List<NewExamForExport> examForExports) {
        WritableWorkbook workbook = null;
        try {
            OutputStream os = response.getOutputStream();// 取得输出流
            response.reset();// 清空输出流
            response.setHeader("Content-disposition", "attachment; filename=" + new String(fileName.getBytes("GB2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");// 定义输出类型
            workbook = Workbook.createWorkbook(os);
            WritableSheet sheet = workbook.createSheet("Sheet1", 0);
            jxl.SheetSettings sheetSettings = sheet.getSettings();
            sheetSettings.setProtected(false);

            WritableFont NormalFont = new WritableFont(WritableFont.ARIAL, 10);
            WritableFont BoldFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);

            // 用于标题居中
            WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);
            wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
            wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
            wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
            wcf_center.setWrap(false); // 文字是否换行

            // 用于正文居左
            WritableCellFormat wcf_left = new WritableCellFormat(NormalFont);
            wcf_left.setBorder(Border.NONE, BorderLineStyle.THIN); // 线条
            wcf_left.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
            wcf_left.setAlignment(Alignment.LEFT); // 文字水平对齐
            wcf_left.setWrap(false); // 文字是否换行
            for (int i = 0; i < title.length; i++) {
                sheet.addCell(new Label(i, 0, title[i].toString(), wcf_center));
            }//列，行
            int j = 1;
            for (NewExamForExport examForExport : examForExports) {
                sheet.addCell(new Label(0, j, SafeConverter.toString(examForExport.getStudentId(), ""), wcf_center));
                sheet.addCell(new Label(1, j, SafeConverter.toString(examForExport.getStudentName(), ""), wcf_center));
                sheet.addCell(new Label(2, j, SafeConverter.toString(examForExport.getSchoolName(), ""), wcf_center));
                sheet.addCell(new Label(3, j, SafeConverter.toString(examForExport.getClazzName(), ""), wcf_center));
                sheet.addCell(new Label(4, j, SafeConverter.toString(examForExport.getBeginTime(), ""), wcf_center));
                sheet.addCell(new Label(5, j, SafeConverter.toString(examForExport.getSubmitTime(), ""), wcf_center));
                sheet.addCell(new Label(6, j, SafeConverter.toString(examForExport.getScore(), ""), wcf_center));
                j++;
            }
            workbook.write();
        } catch (Exception e) {
            logger.error("统考数据Excel文件导出失败", e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }


    }


    /**
     * 假期作业列表
     * CRM使用
     */
    @RequestMapping(value = "report/list.vpage", method = RequestMethod.GET)
    public String historyIndex(Model model) {
        String path = "crm/teachernew/teachervacationhomeworkhistory";
        Long teacherId = SafeConverter.toLong(this.getRequestString("teacherId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher != null) {
            model.addAttribute("newVacationHomeworkHistoryList", vacationHomeworkReportLoaderClient.allVacationHomeworkHistory(teacher));
        }
        model.addAttribute("teacherId", teacherId);
        return path;
    }

    /**
     * 删除寒假作业
     * @return
     */
    @RequestMapping(value = "report/crmdeletehomework.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage crmDeleteVacationHomework() {
        String packageId = this.getRequestString("packageId");
        return vacationHomeworkServiceClient.crmDeleteVacationHomework(packageId);
    }

    /**
     * 恢复假期作业
     * @return
     */
    @RequestMapping(value = "report/resumeHomework.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage resumeHomework() {
        String packageId = this.getRequestString("packageId");
        return vacationHomeworkServiceClient.resumeVacationHomework(packageId);
    }

    /**
     * 每个包信息展示
     */
    @RequestMapping(value = "report/packagereport.vpage", method = RequestMethod.GET)
    public String packageReport(Model model) {
        String path = "crm/teachernew/teachervacationhomeworkpackagereport";
        String packageId = this.getRequestString("packageId");
        model.addAttribute("packageId", packageId);
        if (StringUtils.isBlank(packageId)) {
            model.addAttribute("success", false);
            return path;
        }
        MapMessage mapMessage = vacationHomeworkReportLoaderClient.packageReport(packageId, null, null, false);
        if (mapMessage.isSuccess()) {
            model.addAttribute("success", true);
            NewVacationHomeworkPackagePanorama newVacationHomeworkPackagePanorama = (NewVacationHomeworkPackagePanorama) mapMessage.get("vacationHomeworkPackagePanorama");
            model.addAttribute("newVacationHomeworkPackagePanorama", newVacationHomeworkPackagePanorama);
            return path;
        } else {
            model.addAttribute("success", false);
            return path;
        }
    }


    /**
     * 学生个人假期作业报告
     */

    @RequestMapping(value = "studentpackage.vpage", method = RequestMethod.GET)
    public String studentPackageReport(Model model) {
        String path = "crm/teachernew/teachervacationhomeworkstudentreport";
        String packageId = this.getRequestString("packageId");
        if (StringUtils.isBlank(packageId)) {
            model.addAttribute("success", false);
            return path;
        }
        Long studentId = this.getRequestLong("studentId");
        if (studentId == 0L) {
            model.addAttribute("success", false);
            return path;
        }
        model.addAttribute("studentId", studentId);
        MapMessage mapMessage = vacationHomeworkReportLoaderClient.studentPackageReport(packageId, studentId);
        if (!mapMessage.isSuccess()) {
            model.addAttribute("success", false);
            return path;
        } else {
            model.addAttribute("success", true);
            model.addAttribute("book", mapMessage.get("book"));
            model.addAttribute("weekPlans", mapMessage.get("weekPlans"));
            return path;
        }
    }

    /**
     * 假期作业每个包对应详细情况
     */
    @RequestMapping(value = "report/homeworkreport.vpage", method = RequestMethod.GET)
    public String homeworkReport(Model model) {
        String homeworkId = this.getRequestString("homeworkId");
        String path = "crm/homework/uservacationhomeworkdetail";
        if (StringUtils.isBlank(homeworkId)) {
            model.addAttribute("success", false);
            return path;
        }
        model.addAllAttributes(vacationHomeworkReportLoaderClient.studentVacationNewHomeworkDetail(homeworkId));
        model.addAttribute("success", true);
        return path;
    }

    @RequestMapping(value = "report/remove/cachebookinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage removeVacationHomeworkWinterPlanCacheMapper() {
        String strKeys = this.getRequestString("strKeys");
        if (StringUtils.isBlank(strKeys)) {
            return MapMessage.errorMessage("strBookIds is blank");
        }
        List<String> keys = Arrays.asList(strKeys.split(","));
        vacationHomeworkServiceClient.removeCache(keys);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "autosubmitdubbing.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage autoSubmitDubbing() {
        String homeworkId = getRequestString("homeworkId");
        return vacationHomeworkServiceClient.autoSubmitDubbingHomework(homeworkId);
    }
}
