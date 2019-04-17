package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "自动导入老师任务3的奖励信息",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT},
        ENABLED = false,
        cronExpression = "0 0 8 * * ? "
)
public class AutoImportTeacherTask3Reward extends ScheduledJobWithJournalSupport {

    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String fileUrl = SafeConverter.toString(parameters.get("fileUrl"));
        if(StringUtils.isBlank(fileUrl)){
            logger.error("TT_Job:file url is empty!");
            return;
        }

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(fileUrl).execute();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getOriginalResponse());
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        for (int rowIndex = 1; ; rowIndex++) {
            if(rowIndex % 1000 == 0){
                logger.info("TT_Job:finish num:{}",rowIndex);
                Thread.sleep(1000);// 怕跑太快
            }

            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null)
                break;

            XSSFCell cell = row.getCell(5);//excel第6列，学习ID
            Long schoolId = getLongCellValue(cell);
            if (schoolId == null || schoolId <= 0)
                continue;

            SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(schoolId)
                    .getUninterruptibly();
            if (extInfo == null){
                extInfo = new SchoolExtInfo();
                extInfo.setId(schoolId);
            }

            Integer reward = getIntCellValue(row.getCell(6));//excel第7列，单价
            Map<String, Integer> rewardMap = new HashMap<>();
            rewardMap.put("tpl4", reward);

            extInfo.setRewardMap(rewardMap);
            schoolExtServiceClient.getSchoolExtService().upsertSchoolExtInfo(extInfo);
        }

    }

    public static Long getLongCellValue(XSSFCell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue()).longValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return ConversionUtils.toLong(cell.getStringCellValue().trim());
        }

        return null;
    }

    public static Integer getIntCellValue(XSSFCell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return new Double(cell.getNumericCellValue()).intValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return ConversionUtils.toInt(cell.getStringCellValue().trim());
        }

        return null;
    }

}
