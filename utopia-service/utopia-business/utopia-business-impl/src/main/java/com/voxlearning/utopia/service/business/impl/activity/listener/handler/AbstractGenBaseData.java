package com.voxlearning.utopia.service.business.impl.activity.listener.handler;

import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.business.impl.activity.service.ActivityReportServiceImpl;
import com.voxlearning.utopia.service.business.impl.activity.service.StudentActivityServiceClient;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportBaseData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportStudentData;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public abstract class AbstractGenBaseData implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AbstractGenBaseData.class);

    public static final String SCORE_FILE_KEY = "score_file_key";
    public static final String ALL_SCORE_FILE_KEY = "all_score_file_key";

    @StorageClientLocation(storage = "plat-doc-content")
    private StorageClient fileStorageClient;

    @Inject
    public StudentActivityServiceClient studentActivityServiceClient;
    @Inject
    public ActivityConfigServiceClient activityConfigServiceClient;
    @Inject
    public ActivityReportServiceImpl activityReportService;
    @Inject
    private EmailServiceClient emailServiceClient;

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSqlOrder;

    @Override
    public void afterPropertiesSet() throws Exception {
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("hs_misc");
    }

    public static long timeStringToSeconds(String time) {
        String[] split = time.split(":");
        long hour = SafeConverter.toLong(split[0]);
        long minute = SafeConverter.toLong(split[1]);
        long seconds = SafeConverter.toLong(split[2]);
        return hour * (60 * 60) + minute * 60 + seconds;
    }

    public void fillTitle(XSSFRow titleRow, String[] titles) {
        for (int i = 0; i < titles.length; i++) {
            XSSFCell cell0 = titleRow.createCell(i);
            cell0.setCellValue(titles[i]);
        }
    }

    public String genFileName(String activityId, int totalPage, int page) {
        return System.currentTimeMillis() + RandomStringUtils.randomNumeric(4) + totalPage + "-" + page;
    }

    public String upload(InputStream inputStream, String fileName) {
        String yyyyMMddHHmm = DateFormatUtils.format(new Date(), "yyyy/MM/dd");
        String filePath = "activity_report/" + yyyyMMddHHmm;
        if (!RuntimeMode.isProduction()) {
            filePath = filePath + "/" + RuntimeMode.getCurrentStage();
        }
        String cdnUrl = "https://v.17xueba.com/" + fileStorageClient.upload(inputStream, fileName + ".xlsx", filePath);
        return cdnUrl;
    }

    public String uploadWorkbook(XSSFWorkbook workbook, String fileName) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            workbook.write(os);
            byte[] content = os.toByteArray();
            InputStream is = new ByteArrayInputStream(content);
            return upload(is, fileName);
        } catch (Exception e) {
            logger.error("生成活动报告excel文件异常", e);
        } finally {
            try {
                os.close();
                workbook.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return "";
    }

    public void calcAndSaveStudentData(List<ActivityReportBaseData> userDatas) {
        ActivityReportBaseData maxBaseData = userDatas.get(0);
        try {
            ActivityReportStudentData studentData = new ActivityReportStudentData();
            studentData.setActivityId(maxBaseData.getActivityId());
            studentData.setClazzId(maxBaseData.getClazzId());
            studentData.setStudentId(maxBaseData.getUserId());
            studentData.setStudentName(maxBaseData.getUserName());
            studentData.setDayCount(userDatas.size());
            studentData.setMaxScore(maxBaseData.getScore());

            if (Objects.equals(maxBaseData.getActivityType(), ActivityTypeEnum.SUDOKU.name())) {
                studentData.setMinTime(maxBaseData.getTakeTimes());
            }
            activityReportService.saveActivityReportStudentData(studentData);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public void sendExcelByEmail(String emailContent, ActivityConfig activityConfig) {
        String title = activityConfig.getTitle();
        String email = activityConfig.getEmail();

        String content = "ID：" + activityConfig.getId() + "\n" +
                "类型：" + activityConfig.getType().getName() + "\n" +
                "标题：" + activityConfig.getTitle() + "\n";

        content += emailContent;

        if (RuntimeMode.isProduction()) {
            if (StringUtils.isNotEmpty(email)) {
                emailServiceClient.createPlainEmail()
                        .to(email)
                        .subject(title + "的活动数据")
                        .body(content)
                        .send();
            }
        } else {
            emailServiceClient.createPlainEmail()
                    .to("junbao.zhang@17zuoye.com")
                    .subject("【" + RuntimeMode.current() + "】 " + title + "的活动数据")
                    .body(content)
                    .send();
        }

        // 邮件有收不到的情况,便于从日志拿到成绩链接
        StringBuilder sb = new StringBuilder();
        sb.append("活动成绩邮件 ")
                .append("结束时间：")
                .append(DateFormatUtils.format(activityConfig.getEndTime(), "yyyy-MM-dd"))
                .append("  ")
                .append("收件人：")
                .append(activityConfig.getEmail())
                .append("  ")
                .append(content.replace("\n", "  "));

        logger.info(sb.toString());
    }

    public void deleteBaseData(String activityId) {
        String deleteSql = "DELETE FROM VOX_ACTIVITY_REPORT_BASE_DATA WHERE ACTIVITY_ID='" + activityId + "'";
        utopiaSqlOrder.withSql(deleteSql).executeUpdate();
    }
}
