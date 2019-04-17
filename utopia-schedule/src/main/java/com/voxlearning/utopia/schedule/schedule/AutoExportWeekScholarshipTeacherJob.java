package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.util.JobOssManageUtils;
import com.voxlearning.utopia.service.business.consumer.TeacherActivityServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "每周“期末教师先锋奖”参与老师的数据导出任务",
        jobDescription = "每周日下午5点运行",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 17 ? * SUN"
)
@ProgressTotalWork(100)
public class AutoExportWeekScholarshipTeacherJob  extends ScheduledJobWithJournalSupport {

    private UtopiaSql utopiaSql;

    @Inject
    private EmailServiceClient emailServiceClient;

    @Inject
    private TeacherActivityServiceClient teacherActivityServiceClient;

    @Override
    public void afterPropertiesSet() {
        utopiaSql = UtopiaSqlFactory.instance().getUtopiaSql("hs_misc");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        List<Map<String, Object>> result = utopiaSql.withSql(
                "SELECT TEACHER_ID,LAST_ASSIGN_DATE,BASIC_REVIEW_NUM,TERM_REVIEW_NUM,TERM_REVIEW_CHECKED,FINISH_RATE,MAX_GROUP_FINISH_NUM,SCORE FROM VOX_TEACHER_SCHOLARSHIP_RECORD " +
                        " WHERE " +
                        " (SCORE > 80 OR FINISH_RATE > 0.8 OR MAX_GROUP_FINISH_NUM > 30)" +
                        " AND WEEK_LOTTERY = 1")
                .queryAll();

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow topRow = sheet.createRow(0);
        XSSFCell topCell0 = topRow.createCell(0);
        topCell0.setCellValue("老师ID");
        XSSFCell topCell1 = topRow.createCell(1);
        topCell1.setCellValue("最近布置作业日期");
        XSSFCell topCell2 = topRow.createCell(2);
        topCell2.setCellValue("基础必过");
        XSSFCell topCell3 = topRow.createCell(3);
        topCell3.setCellValue("期末复习");
        XSSFCell topCell4 = topRow.createCell(4);
        topCell4.setCellValue("检查作业");
        XSSFCell topCell5 = topRow.createCell(5);
        topCell5.setCellValue("作业完成率");
        XSSFCell topCell6 = topRow.createCell(6);
        topCell6.setCellValue("最多的组的完成数量");
        XSSFCell topCell7 = topRow.createCell(7);
        topCell7.setCellValue("作业平均分数");

        for(int i=0; i<result.size(); i++){
            Map<String,Object> temp = result.get(i);
            XSSFRow dataRow = sheet.createRow(i+1);
            Long teacherId = SafeConverter.toLong(temp.get("TEACHER_ID"));
            Date assignDate = (Date)temp.get("LAST_ASSIGN_DATE");
            Integer basicNum = SafeConverter.toInt(temp.get("BASIC_REVIEW_NUM"));
            Integer reviewNum = SafeConverter.toInt(temp.get("TERM_REVIEW_NUM"));
            Integer checkedNum = SafeConverter.toInt(temp.get("TERM_REVIEW_CHECKED"));
            Double finishRate = SafeConverter.toDouble(temp.get("FINISH_RATE"));
            Integer finishNum = SafeConverter.toInt(temp.get("MAX_GROUP_FINISH_NUM"));
            Double score = SafeConverter.toDouble(temp.get("SCORE"));
            XSSFCell dataCell0 = dataRow.createCell(0);
            dataCell0.setCellValue(teacherId);
            XSSFCell dataCell1 = dataRow.createCell(1);
            dataCell1.setCellValue(DateUtils.dateToString(assignDate,DateUtils.FORMAT_SQL_DATE));
            XSSFCell dataCell2 = dataRow.createCell(2);
            dataCell2.setCellValue(basicNum);
            XSSFCell dataCell3 = dataRow.createCell(3);
            dataCell3.setCellValue(reviewNum);
            XSSFCell dataCell4 = dataRow.createCell(4);
            dataCell4.setCellValue(checkedNum);
            XSSFCell dataCell5 = dataRow.createCell(5);
            dataCell5.setCellValue(finishRate);
            XSSFCell dataCell6 = dataRow.createCell(6);
            dataCell6.setCellValue(finishNum);
            XSSFCell dataCell7 = dataRow.createCell(7);
            dataCell7.setCellValue(score);
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] content = os.toByteArray();
        InputStream is = new ByteArrayInputStream(content);
        try {
            String filePath = JobOssManageUtils.upload(is,content.length,"xlsx");
            //跑完发送邮件
            emailServiceClient.createPlainEmail()
                    .to("yong.liu@17zuoye.com")
                    .cc("te.wang@17zuoye.com")
                    .subject("每周期末教师先锋奖参与老师的数据")
                    .body("数据请点击此链接下载："+filePath)
                    .send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //抽完讲所有的状态置为false
        teacherActivityServiceClient.updateWeekLottery();

    }
}
