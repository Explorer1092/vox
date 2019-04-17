package com.voxlearning.utopia.service.business.impl.activity.listener.handler;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.data.ActivityReport;
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.business.impl.activity.entity.ReportContext;
import com.voxlearning.utopia.service.business.impl.activity.service.ActivityReportServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportBaseData;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class GenTwoFourBaseData extends AbstractGenBaseData {

    private static final Logger logger = LoggerFactory.getLogger(GenTwoFourBaseData.class);

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private ActivityReportServiceClient activityReportServiceClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;

    public void execute(ReportContext reportContext) {
        ActivityConfig activity = reportContext.getActivity();
        StringBuilder sbScore = new StringBuilder("成绩：").append("\n");
        StringBuilder sbMaxScore = new StringBuilder("单人最高成绩：").append("\n");

        if (reportContext.isWriteDatabase()) {
            deleteBaseData(activity.getId());
            activityReportService.deleteActivityReportStudentDataById(activity.getId());
        }

        int totalPages = 1;
        for (int i = 0; i < totalPages; i++) {
            String pageString = SafeConverter.toString(i + 1);
            logger.info("ActivityReportListener excel activityId:{} progress: {}/{}", activity.getId(), pageString, totalPages);

            Page<TwoFourPointEntityRecord> entryRecords = studentActivityServiceClient.loadTwentyFourRecordPage(activity.getId(), i, 30000);
            totalPages = entryRecords.getTotalPages();
            List<ActivityReportBaseData> baseData = genTwoFourPointActivityReportData(reportContext, entryRecords.getContent(), activity.getId());
            Map<String, String> map = genTwentyFourActivityReportExcel(baseData, genFileName(activity.getId(), totalPages, i + 1));

            sbScore.append("第").append(pageString).append("页：").append(map.get(SCORE_FILE_KEY)).append("\n");
            sbMaxScore.append("第").append(pageString).append("页：").append(map.get(ALL_SCORE_FILE_KEY)).append("\n");
        }

        if (reportContext.isWriteDatabase()) {
            ActivityReport report = new ActivityReport();
            report.setTotal(reportContext.getUserCount());
            report.setHScore(reportContext.getMaxScore());
            report.setLScore(reportContext.getMinScore());
            report.setAvgScore(reportContext.getAvgScore());
            activity.setReport(report);
            activityConfigServiceClient.getActivityConfigService().updateActivityConfig(activity);
        }

        sendExcelByEmail(sbScore.append("\n").append(sbMaxScore).toString(), activity);
    }

    public List<ActivityReportBaseData> genTwoFourPointActivityReportData(ReportContext context, List<TwoFourPointEntityRecord> twoFourPointEntityRecords, String activityId) {
        List<ActivityReportBaseData> activityReportBaseDataList = new LinkedList<>();
        List<ActivityReportBaseData> userActivityReportBaseDataList = new LinkedList<>();

        for (TwoFourPointEntityRecord twoFourPointEntityRecord : twoFourPointEntityRecords) {
            try {
                userActivityReportBaseDataList.clear();

                Long userId = twoFourPointEntityRecord.getUserId();
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
                if (studentDetail.getClazz() == null) continue;
                String userName = studentDetail.getProfile().getRealname();
                Long clazzId = studentDetail.getClazzId();
                String clazzName = studentDetail.getClazz().getClassName();
                ClazzLevel clazzLevel = studentDetail.getClazzLevel();
                Long schoolId = studentDetail.getClazz().getSchoolId();
                School school = raikouSystem.loadSchool(schoolId);
                String schoolName = school.getShortName();
                Integer regionCode = school.getRegionCode();
                ExRegion region = raikouSystem.loadRegion(regionCode);
                String regionName = region.getCountyName();
                Integer cityCode = region.getCityCode();
                String cityName = region.getCityName();
                Integer provinceCode = region.getProvinceCode();
                String provinceName = region.getProvinceName();

                Map<Long, Integer> scoreMap = twoFourPointEntityRecord.getScoreMap();
                if (MapUtils.isNotEmpty(scoreMap)) {
                    context.addUser(studentDetail.getId());

                    Iterator<Long> it = scoreMap.keySet().iterator();
                    while (it.hasNext()) {
                        ActivityReportBaseData activityReportBaseData = new ActivityReportBaseData();
                        activityReportBaseData.setProvinceCode(provinceCode);
                        activityReportBaseData.setProvinceName(provinceName);
                        activityReportBaseData.setCityCode(cityCode);
                        activityReportBaseData.setCityName(cityName);
                        activityReportBaseData.setRegionCode(regionCode);
                        activityReportBaseData.setRegionName(regionName);
                        activityReportBaseData.setSchoolId(schoolId);
                        activityReportBaseData.setSchoolName(schoolName);
                        activityReportBaseData.setClazzLevel(clazzLevel.getLevel());
                        activityReportBaseData.setClazzId(clazzId);
                        activityReportBaseData.setClazzName(clazzName);
                        activityReportBaseData.setUserId(userId);
                        activityReportBaseData.setUserName(userName);
                        activityReportBaseData.setActivityType(ActivityTypeEnum.TWENTY_FOUR.name());
                        activityReportBaseData.setActivityId(activityId);
                        Long playDateTime = it.next();
                        activityReportBaseData.setPlayActivityDate(DateUtils.dateToString(new Date(playDateTime)));
                        Integer score = scoreMap.get(playDateTime);
                        activityReportBaseData.setScore(score);
                        activityReportBaseData.setSkipCount(twoFourPointEntityRecord.getSkipCount());
                        activityReportBaseData.setResetCount(twoFourPointEntityRecord.getResetCount());
                        userActivityReportBaseDataList.add(activityReportBaseData);
                        context.addScore(score);
                    }
                    userActivityReportBaseDataList.sort((o1, o2) -> o2.getScore().compareTo(o1.getScore()));
                    userActivityReportBaseDataList.get(0).setIsTopScore(1);
                    context.addMaxScore(userActivityReportBaseDataList.get(0).getScore());
                    activityReportBaseDataList.addAll(userActivityReportBaseDataList);
                    if (context.isWriteDatabase()) {
                        activityReportServiceClient.saveActivityReportBaseDatas(userActivityReportBaseDataList);
                        calcAndSaveStudentData(userActivityReportBaseDataList);
                    }
                }
            } catch (Exception e) {
                logger.error("二十四点活动数据" + twoFourPointEntityRecord.getId() + "生成活动报告基础异常", e);
            }
        }
        return activityReportBaseDataList;
    }

    public Map<String, String> genTwentyFourActivityReportExcel(List<ActivityReportBaseData> allActivityReportBaseDatas, String fileName) {
        Map<String, String> result = new HashMap<>();

        String[] titles = new String[]{"日期", "学校ID", "学校名称", "年级", "班级ID", "班级名称", "学生ID", "学生姓名", "成绩", "重置次数", "跳过次数", "地区"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFWorkbook workbook2 = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFSheet sheet2 = workbook2.createSheet();
        XSSFRow titleRow = sheet.createRow(0);
        XSSFRow titleRow2 = sheet2.createRow(0);
        fillTitle(titleRow, titles);
        fillTitle(titleRow2, titles);
        int row = 0;
        int row2 = 0;

        if (CollectionUtils.isNotEmpty(allActivityReportBaseDatas)) {
            for (ActivityReportBaseData data : allActivityReportBaseDatas) {
                writeLine(sheet, row, data);
                row++;
                if (Objects.equals(data.getIsTopScore(), 1)) {
                    writeLine(sheet2, row2, data);
                    row2++;
                }
            }
        }

        result.put(SCORE_FILE_KEY, uploadWorkbook(workbook, fileName));
        result.put(ALL_SCORE_FILE_KEY, uploadWorkbook(workbook2, "max_" + fileName));
        return result;
    }

    private void writeLine(XSSFSheet sheet, int i, ActivityReportBaseData data) {
        XSSFRow dataRow = sheet.createRow(i + 1);
        XSSFCell dataCell0 = dataRow.createCell(0);
        dataCell0.setCellValue(data.getPlayActivityDate().substring(0, 10));
        XSSFCell dataCell1 = dataRow.createCell(1);
        dataCell1.setCellValue(data.getSchoolId());
        XSSFCell dataCel12 = dataRow.createCell(2);
        dataCel12.setCellValue(data.getSchoolName());
        XSSFCell dataCel13 = dataRow.createCell(3);
        dataCel13.setCellValue(ClazzLevel.getDescription(data.getClazzLevel()));
        XSSFCell dataCel14 = dataRow.createCell(4);
        dataCel14.setCellValue(data.getClazzId());
        XSSFCell dataCel15 = dataRow.createCell(5);
        dataCel15.setCellValue(data.getClazzName());
        XSSFCell dataCel16 = dataRow.createCell(6);
        dataCel16.setCellValue(data.getUserId());
        XSSFCell dataCel17 = dataRow.createCell(7);
        dataCel17.setCellValue(data.getUserName());
        XSSFCell dataCel18 = dataRow.createCell(8);
        dataCel18.setCellValue(data.getScore());
        XSSFCell dataCel19 = dataRow.createCell(9);
        dataCel19.setCellValue(data.getResetCount());
        XSSFCell dataCel110 = dataRow.createCell(10);
        dataCel110.setCellValue(data.getSkipCount());
        XSSFCell dataCel111 = dataRow.createCell(11);
        dataCel111.setCellValue(data.getRegionName());
    }

}
