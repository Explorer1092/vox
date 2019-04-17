package com.voxlearning.utopia.service.business.impl.activity.listener.handler;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.data.ActivityReport;
import com.voxlearning.utopia.entity.activity.SudokuUserRecord;
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
import java.util.stream.Collectors;

@Named
public class GenSudokuBaseData extends AbstractGenBaseData {

    private static final Logger logger = LoggerFactory.getLogger(GenSudokuBaseData.class);

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

            Page<SudokuUserRecord> entryRecords = studentActivityServiceClient.loadSudokuRecordPage(activity.getId(), i, 50000);
            totalPages = entryRecords.getTotalPages();
            List<ActivityReportBaseData> baseData = genSudokuActivityReportData(reportContext, entryRecords.getContent(), activity.getId());
            Map<String, String> map = genSudoActivityReportExcel1(baseData, activity, genFileName(activity.getId(), totalPages, i + 1));

            sbScore.append("第").append(pageString).append("页：").append(map.get(SCORE_FILE_KEY)).append("\n");
            sbMaxScore.append("第").append(pageString).append("页：").append(map.get(ALL_SCORE_FILE_KEY)).append("\n");
        }

        if (reportContext.isWriteDatabase()) {
            ActivityReport report = new ActivityReport();
            report.setTotal(reportContext.getUserCount());
            report.setHScore(reportContext.getMaxScore());
            report.setLScore(reportContext.getMinScore());
            report.setAvgScore(reportContext.getAvgScore());
            report.setAvgTime(reportContext.getAvgTime());
            activity.setReport(report);
            activityConfigServiceClient.getActivityConfigService().updateActivityConfig(activity);
        }

        sendExcelByEmail(sbScore.append("\n").append(sbMaxScore).toString(), activity);
    }

    public List<ActivityReportBaseData> genSudokuActivityReportData(ReportContext context, List<SudokuUserRecord> sudokuUserRecords, String activityId) {
        List<ActivityReportBaseData> activityReportBaseDataList = new LinkedList<>();
        for (SudokuUserRecord sudokuUserRecord : sudokuUserRecords) {
            try {
                Long userId = sudokuUserRecord.getUserId();
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

                LinkedList<SudokuUserRecord.QuestionTime> questionTimes = sudokuUserRecord.getTimes();

                Long takeTimes = 0L;
                //计算做题发挥时间
                if (CollectionUtils.isNotEmpty(questionTimes)) {
                    for (SudokuUserRecord.QuestionTime temp : questionTimes) {
                        if (Objects.nonNull(temp.getTime())) {
                            takeTimes += timeStringToSeconds(temp.getTime());
                            context.addTakeTime(takeTimes);
                        }
                    }
                }
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
                activityReportBaseData.setActivityType(ActivityTypeEnum.SUDOKU.name());
                activityReportBaseData.setActivityId(activityId);
                activityReportBaseData.setPlayActivityDate(DateUtils.dateToString(sudokuUserRecord.getCreateTime()));
                activityReportBaseData.setTakeTimes(takeTimes);
                activityReportBaseData.setScore(sudokuUserRecord.getCorrectCount());
                activityReportBaseData.setExercises(sudokuUserRecord.getCorrectCount());
                if (Objects.nonNull(sudokuUserRecord.getEndTime())) {
                    activityReportBaseData.setEndTime(sudokuUserRecord.getEndTime());
                }
                activityReportBaseDataList.add(activityReportBaseData);
                context.addUser(studentDetail.getId());
                context.addScore(sudokuUserRecord.getCorrectCount());
            } catch (Exception e) {
                logger.error("数独活动数据" + sudokuUserRecord.getId() + "生成活动报告基础异常", e);
            }
        }

        Map<Long, List<ActivityReportBaseData>> mapData = activityReportBaseDataList.stream().collect(Collectors.groupingBy(ActivityReportBaseData::getUserId));
        Iterator<Long> userIdKey = mapData.keySet().iterator();
        while (userIdKey.hasNext()) {
            Long userIdkey = userIdKey.next();
            List<ActivityReportBaseData> userIdtoData = mapData.get(userIdkey);
            userIdtoData.sort(Comparator.comparing(ActivityReportBaseData::getScore).reversed().thenComparing(ActivityReportBaseData::getTakeTimes));
            ActivityReportBaseData topOne = userIdtoData.get(0);
            topOne.setIsTopScore(1);
            context.addMaxScore(topOne.getScore());
            if (context.isWriteDatabase()) {
                calcAndSaveStudentData(userIdtoData);
            }
        }
        if (context.isWriteDatabase()) {
            activityReportServiceClient.saveActivityReportBaseDatas(activityReportBaseDataList);
        }
        return activityReportBaseDataList;
    }

    public Map<String, String> genSudoActivityReportExcel1(List<ActivityReportBaseData> allActivityReportBaseDatas, ActivityConfig activityConfig, String fileName) {
        Map<String, String> result = new HashMap<>();

        String[] titles = new String[]{"活动id", "游戏名称", "学生id", "学生姓名", "班级", "学校id", "学校名", "区", "市", "省", "游戏模式", "题量限制"
                , "时间限制(分钟)", "完成题目数", "完成总时间(秒)", "结束时间", "年级"};

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
                writeLine(activityConfig, sheet, row, data);
                row++;
                if (Objects.equals(data.getIsTopScore(), 1)) {
                    writeLine(activityConfig, sheet2, row2, data);
                    row2++;
                }
            }
        }

        result.put(SCORE_FILE_KEY, uploadWorkbook(workbook, fileName));
        result.put(ALL_SCORE_FILE_KEY, uploadWorkbook(workbook2, "max_" + fileName));
        return result;
    }

    private void writeLine(ActivityConfig activityConfig, XSSFSheet sheet, int i, ActivityReportBaseData data) {
        XSSFRow dataRow = sheet.createRow(i + 1);
        XSSFCell dataCell0 = dataRow.createCell(0);
        dataCell0.setCellValue(data.getActivityId());
        XSSFCell dataCell1 = dataRow.createCell(1);
        dataCell1.setCellValue(activityConfig.getTitle());
        XSSFCell dataCel12 = dataRow.createCell(2);
        dataCel12.setCellValue(data.getUserId());
        XSSFCell dataCel13 = dataRow.createCell(3);
        dataCel13.setCellValue(data.getUserName());
        XSSFCell dataCel14 = dataRow.createCell(4);
        dataCel14.setCellValue(data.getClazzName());
        XSSFCell dataCel15 = dataRow.createCell(5);
        dataCel15.setCellValue(data.getSchoolId());
        XSSFCell dataCel16 = dataRow.createCell(6);
        dataCel16.setCellValue(data.getSchoolName());
        XSSFCell dataCel17 = dataRow.createCell(7);
        dataCel17.setCellValue(data.getRegionName());
        XSSFCell dataCel18 = dataRow.createCell(8);
        dataCel18.setCellValue(data.getCityName());
        XSSFCell dataCel19 = dataRow.createCell(9);
        dataCel19.setCellValue(data.getProvinceName());
        XSSFCell dataCel110 = dataRow.createCell(10);
        XSSFCell dataCel111 = dataRow.createCell(11);
        XSSFCell dataCel112 = dataRow.createCell(12);
        if (Objects.isNull(activityConfig.getRules().getPattern())) {
            dataCel110.setCellValue("");
        } else {
            dataCel110.setCellValue(activityConfig.getRules().getPattern().getName());
        }
        if (Objects.isNull(activityConfig.getRules().getLimitAmount())) {
            dataCel111.setCellValue("");
        } else {
            dataCel111.setCellValue(activityConfig.getRules().getLimitAmount());
        }
        if (Objects.isNull(activityConfig.getRules().getLimitTime())) {
            dataCel112.setCellValue("");
        } else {
            dataCel112.setCellValue(activityConfig.getRules().getLimitTime());
        }
        XSSFCell dataCel113 = dataRow.createCell(13);
        if (Objects.isNull(data.getExercises())) {
            dataCel113.setCellValue("");
        } else {
            dataCel113.setCellValue(data.getExercises());
        }
        XSSFCell dataCel114 = dataRow.createCell(14);
        if (Objects.isNull(data.getTakeTimes())) {
            dataCel114.setCellValue("");
        } else {
            dataCel114.setCellValue(data.getTakeTimes());
        }
        XSSFCell dataCel115 = dataRow.createCell(15);
        if (Objects.isNull(data.getEndTime())) {
            dataCel115.setCellValue("");
        } else {
            dataCel115.setCellValue(DateUtils.dateToString(data.getEndTime()));
        }
        XSSFCell dataCel116 = dataRow.createCell(16);
        if (Objects.isNull(data.getClazzLevel())) {
            dataCel116.setCellValue("");
        } else {
            dataCel116.setCellValue(data.getClazzLevel());
        }
    }

}
