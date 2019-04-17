package com.voxlearning.utopia.service.campaign.impl.service.excel;

import com.voxlearning.alps.core.util.FileUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.service.campaign.impl.service.StudentActivityServiceImpl;
import com.voxlearning.utopia.service.campaign.impl.service.excel.model.BaseExportScoreExcel;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignOssManageUtils;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

abstract class AbstractActivityExportScore<T extends BaseExportScoreExcel> {

    private static final Logger log = LoggerFactory.getLogger(AbstractActivityExportScore.class);

    private ActivityConfigServiceClient activityConfigServiceClient;
    private EmailServiceClient emailServiceClient;
    StudentLoaderClient studentLoaderClient;
    StudentActivityServiceImpl studentActivityService;
    RaikouSystem raikouSystem;


    AbstractActivityExportScore(ActivityConfigServiceClient activityConfigServiceClient,
                                EmailServiceClient emailServiceClient,
                                StudentLoaderClient studentLoaderClient,
                                StudentActivityServiceImpl studentActivityService,
                                RaikouSystem raikouSystem) {

        this.activityConfigServiceClient = activityConfigServiceClient;
        this.emailServiceClient = emailServiceClient;
        this.studentLoaderClient = studentLoaderClient;
        this.studentActivityService = studentActivityService;
        this.raikouSystem = raikouSystem;
    }

    protected abstract String filePrefix();

    protected abstract String[] getRowTitle();

    protected abstract WriteRow<T> row();

    protected abstract List<T> getExcelList(ActivityConfig activityConfig);

    public void exportExcel(String activityId, String email) {
        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(activityId);
        // 老活动也想导出数据,做兼容处理
        if (activityConfig == null) {
            activityConfig = new ActivityConfig();
            activityConfig.setId(activityId);
            activityConfig.setTitle(activityId);
        }
        List<T> excelList = getExcelList(activityConfig);
        if (excelList.isEmpty()) return;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        createSudokuTableHead(sheet, getRowTitle());

        int rowNum = 1;
        for (T excel : excelList) {
            int cellNum = 0;
            XSSFRow row = sheet.createRow(rowNum++);
            row().write(row, cellNum, excel);
        }

        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            byte[] content = outStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);

            if (RuntimeMode.isDevelopment()) {
                String fileName = activityConfig.getTitle() + ".xlsx";
                FileUtils.writeByteArrayToFile(new File("/activity_export/" + fileName), content);
            }

            String filePath = CampaignOssManageUtils.upload(byteArrayInputStream, content.length, "xlsx");
            emailServiceClient.createPlainEmail()
                    .to(StringUtils.isEmpty(email) ? "junbao.zhang@17zuoye.com" : email)
                    .cc("junbao.zhang@17zuoye.com")
                    .subject(String.format("“%s”活动%s导出结果", activityConfig.getTitle(), filePrefix()))
                    .body("文件下载地址：" + filePath)
                    .send();
        } catch (Exception e) {
            log.error("活动成绩导出失败：activityId is " + activityConfig.getId(), e);
        }
    }

    void addCell(XSSFRow row, int cellIndex, String cellValue) {
        XSSFCell cell = row.createCell(cellIndex);
        cell.setCellValue(cellValue);
    }

    private void createSudokuTableHead(XSSFSheet sheet, String[] rowTitle) {
        XSSFRow row = sheet.createRow(0);
        for (int i = 0; i < rowTitle.length; i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellValue(rowTitle[i]);
        }
    }

    /**
     * 查找分数最高的日期, 可能两天都是10分
     *
     * @param map key 是时间戳, value 是分数
     * @return key 是分数 value 是时间戳 set
     */
    Map<Integer, Set<Long>> getMaxScoreDays(Map<Long, Integer> map) {
        if (map == null || map.isEmpty()) {
            return new HashMap<>();
        }

        Integer maxScore = null; // 最高分
        Map<Integer, Set<Long>> scoreDays = new HashMap<>();//key 分数 value 时间戳

        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            Long day = entry.getKey(); // 时间戳
            Integer score = entry.getValue(); // 分数

            if (maxScore == null || score >= maxScore) {
                maxScore = score;
            }

            Set<Long> daySet = scoreDays.get(score);
            if (daySet == null) {
                daySet = new LinkedHashSet<>();
            }
            daySet.add(day);
            scoreDays.put(score, daySet);
        }

        Map<Integer, Set<Long>> result = new HashMap<>();
        result.put(maxScore, scoreDays.get(maxScore));
        return result;
    }
}
