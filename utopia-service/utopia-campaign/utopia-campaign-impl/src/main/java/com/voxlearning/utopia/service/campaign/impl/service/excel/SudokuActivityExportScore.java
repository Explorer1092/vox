package com.voxlearning.utopia.service.campaign.impl.service.excel;

import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.activity.SudokuUserRecord;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.service.campaign.impl.service.StudentActivityServiceImpl;
import com.voxlearning.utopia.service.campaign.impl.service.excel.model.SudokuScoreExcel;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ThreadSafe
public class SudokuActivityExportScore extends AbstractActivityExportScore<SudokuScoreExcel> {

    private static final Logger log = LoggerFactory.getLogger(SudokuActivityExportScore.class);

    public SudokuActivityExportScore(ActivityConfigServiceClient activityConfigServiceClient,
                                     EmailServiceClient emailServiceClient,
                                     StudentLoaderClient studentLoaderClient,
                                     StudentActivityServiceImpl studentActivityService,
                                     RaikouSystem raikouSystem) {
        super(activityConfigServiceClient, emailServiceClient, studentLoaderClient, studentActivityService, raikouSystem);
    }

    @Override
    protected String filePrefix() {
        return "";
    }

    @Override
    protected String[] getRowTitle() {
        return new String[]{
                "日期", "活动ID", "活动标题", "学生ID", "学生姓名", "年级",
                "班级ID", "班级名称", "学校ID", "学校名称", "区", "市", "省",
                "游戏模式", "题量限制", "时间限制", "完成题目数", "完成题目消耗时间(单位:秒)", "结束时间"
        };
    }

    @Override
    protected WriteRow<SudokuScoreExcel> row() {
        return (row, cellNum, data) -> {
            addCell(row, cellNum++, data.getCurDate());
            addCell(row, cellNum++, data.getId());
            addCell(row, cellNum++, data.getTitle());
            addCell(row, cellNum++, data.getStudentId());
            addCell(row, cellNum++, data.getStudentName());
            addCell(row, cellNum++, data.getClazzLevel());
            addCell(row, cellNum++, data.getClazzId());
            addCell(row, cellNum++, data.getClazzName());
            addCell(row, cellNum++, data.getSchoolId());
            addCell(row, cellNum++, data.getSchoolName());
            addCell(row, cellNum++, data.getAreaName());
            addCell(row, cellNum++, data.getCityName());
            addCell(row, cellNum++, data.getProvinceName());
            addCell(row, cellNum++, data.getPatternName());
            addCell(row, cellNum++, data.getLimitAmount());
            addCell(row, cellNum++, data.getLimitTime());
            addCell(row, cellNum++, data.getFinshCount());
            addCell(row, cellNum++, data.getFinshTime());
            addCell(row, cellNum, data.getEndTime());
        };
    }

    @Override
    protected List<SudokuScoreExcel> getExcelList(ActivityConfig activityConfig) {
        String id = activityConfig.getId();
        String title = activityConfig.getTitle();
        String patternName = activityConfig.getRules().getPattern().getName();
        Integer limitAmount = activityConfig.getRules().getLimitAmount();
        Integer limitTime = activityConfig.getRules().getLimitTime();

        Long count = studentActivityService.loadAllCountByActivityId(id);
        if (RuntimeMode.isProduction() && count > 20000) {
            log.error("活动成绩导出结果过大,请人工参与! activityId:" + activityConfig.getId());
            return Collections.emptyList();
        }
        List<SudokuUserRecord> sudokuUserRecords = studentActivityService.loadAllSudokuRecords(id);

        // 乱序就乱序吧, excel 排序也是很强大的
        List<SudokuScoreExcel> excelList = sudokuUserRecords.parallelStream().map(item -> {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(item.getUserId());
            String clazzId = studentDetail.getClazz() == null ? "" : SafeConverter.toString(studentDetail.getClazz().getId());
            String clazzName = studentDetail.getClazz() == null ? "" : studentDetail.getClazz().getClassName();
            String clazzLevel = studentDetail.getClazz() == null ? "" : studentDetail.getClazz().getClassLevel();
            String schoolName = studentDetail.getStudentSchoolName() == null ? "" : studentDetail.getStudentSchoolName();
            String areaName = "", cityName = "", provinceName = "";
            Integer schoolRegionCode = studentDetail.getStudentSchoolRegionCode();
            if (schoolRegionCode != null) {
                ExRegion area = raikouSystem.loadRegion(schoolRegionCode);
                areaName = area.getCountyName();
                cityName = area.getCityName();
                provinceName = area.getProvinceName();
            }

            Long timeConsuming = 0L;
            try {
                timeConsuming = item.getTimes().stream()
                        .filter(i -> i.getEndTime() != null)
                        .map(i -> timeStringToSeconds(i.getTime()))
                        .reduce(0L, (o1, o2) -> o1 + o2);
            } catch (Exception e) {
                timeConsuming = item.getTimes().stream()
                        .filter(i -> i.getEndTime() != null)
                        .map(i -> i.getEndTime().getTime() - i.getBeginTime().getTime())
                        .reduce(0L, (o1, o2) -> o1 + o2) / 1000;
            }

            SudokuScoreExcel excel = new SudokuScoreExcel();
            excel.setId(id);
            excel.setTitle(title);
            excel.setCurDate(item.getCurDate());
            excel.setStudentId(SafeConverter.toString(studentDetail.getId()));
            excel.setStudentName(studentDetail.getProfile().getRealname());
            excel.setClazzId(clazzId);
            excel.setClazzName(clazzName);
            excel.setClazzLevel(clazzLevel);
            excel.setSchoolId(StringUtils.isEmpty(schoolName) ? "" : studentDetail.getClazz().getSchoolId() + "");
            excel.setSchoolName(schoolName);
            excel.setAreaName(areaName);
            excel.setCityName(cityName);
            excel.setProvinceName(provinceName);
            excel.setPatternName(patternName);
            excel.setLimitAmount(SafeConverter.toString(limitAmount));
            excel.setLimitTime(SafeConverter.toString(limitTime));
            excel.setFinshCount(SafeConverter.toString(item.getCorrectCount()));
            excel.setFinshTime(SafeConverter.toString(timeConsuming));
            Date endTime = item.getEndTime();
            excel.setEndTime(endTime == null ? "" : DateFormatUtils.format(endTime, "yyyy-MM-dd HH:mm:ss"));
            return excel;
        }).collect(Collectors.toList());
        return excelList;
    }

    private static long timeStringToSeconds(String time) {
        String[] split = time.split(":");
        long hour = SafeConverter.toLong(split[0]);
        long minute = SafeConverter.toLong(split[1]);
        long seconds = SafeConverter.toLong(split[2]);
        return hour * (60 * 60) + minute * 60 + seconds;
    }
}
