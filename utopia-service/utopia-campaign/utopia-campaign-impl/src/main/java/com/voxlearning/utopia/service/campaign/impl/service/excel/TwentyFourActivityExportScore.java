package com.voxlearning.utopia.service.campaign.impl.service.excel;

import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.service.campaign.impl.service.StudentActivityServiceImpl;
import com.voxlearning.utopia.service.campaign.impl.service.excel.model.TwentyFourScoreExcel;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.slf4j.Logger;

import java.util.*;

@ThreadSafe
public class TwentyFourActivityExportScore extends AbstractActivityExportScore<TwentyFourScoreExcel> {

    private static final Logger log = LoggerFactory.getLogger(TwentyFourActivityExportScore.class);

    public TwentyFourActivityExportScore(ActivityConfigServiceClient activityConfigServiceClient,
                                         EmailServiceClient emailServiceClient,
                                         StudentLoaderClient studentLoaderClient,
                                         StudentActivityServiceImpl studentActivityService,
                                         RaikouSystem raikouSystem) {
        super(activityConfigServiceClient, emailServiceClient, studentLoaderClient, studentActivityService, raikouSystem);
    }

    @Override
    protected String filePrefix() {
        return "单人最高分";
    }

    @Override
    protected String[] getRowTitle() {
        return new String[]{
                "日期", "学校ID", "学校名称", "年级", "班级ID", "班级名称",
                "学生ID", "学生姓名", "成绩", "重置次数", "跳过次数"
        };
    }

    @Override
    protected WriteRow<TwentyFourScoreExcel> row() {
        return (row, cellNum, data) -> {
            addCell(row, cellNum++, data.getDate());
            addCell(row, cellNum++, data.getSchoolId());
            addCell(row, cellNum++, data.getSchoolName());
            addCell(row, cellNum++, data.getClazzLevel());
            addCell(row, cellNum++, data.getClazzId());
            addCell(row, cellNum++, data.getClazzName());
            addCell(row, cellNum++, data.getStudentId());
            addCell(row, cellNum++, data.getStudentName());
            addCell(row, cellNum++, data.getScore());
            addCell(row, cellNum++, data.getResetCount());
            addCell(row, cellNum, data.getSkipCount());
        };
    }

    @Override
    protected List<TwentyFourScoreExcel> getExcelList(ActivityConfig activityConfig) {
        List<TwentyFourScoreExcel> resultList = new ArrayList<>();

        Long count = studentActivityService.loadAllTwofourRecordCount(activityConfig.getId());
        if (RuntimeMode.isProduction() && count > 20000) {
            log.error("活动成绩导出结果过大,请人工参与! activityId:" + activityConfig.getId());
            return Collections.emptyList();
        }

        List<TwoFourPointEntityRecord> recordList = studentActivityService.loadAllTwofourRecords(activityConfig.getId());

        for (TwoFourPointEntityRecord record : recordList) {
            StudentDetail sd = studentLoaderClient.loadStudentDetail(record.getUserId());
            if (sd == null) continue;

            Map<Long, Integer> scoreMap = record.getScoreMap();
            Map<Integer, Set<Long>> maxScoreDays = getMaxScoreDays(scoreMap);

            for (Map.Entry<Integer, Set<Long>> entry : maxScoreDays.entrySet()) {
                Integer score = entry.getKey();
                Set<Long> days = entry.getValue();
                for (Long dayTimeStamp : days) {
                    TwentyFourScoreExcel excel = new TwentyFourScoreExcel();

                    String date = DateFormatUtils.format(new Date(dayTimeStamp), "MM-dd");
                    Long schoolId = Optional.ofNullable(sd.getClazz()).map(Clazz::getSchoolId).orElse(0L);
                    String schoolName = sd.getStudentSchoolName();
                    String clazzLevel = "";
                    if (sd.getClazz() != null && sd.getClazz().getClazzLevel() != null) {
                        clazzLevel = SafeConverter.toString(sd.getClazz().getClazzLevel().getLevel());
                    }
                    Long clazzId = sd.getClazzId();
                    String clazzName = "";
                    if (sd.getClazz() != null && sd.getClazz().getClazzLevel() != null) {
                        clazzName = sd.getClazz().formalizeClazzName();
                    }
                    Long studentId = record.getUserId();
                    String studentName = sd.fetchRealname();
                    Long resetCount = record.getResetCount() == null ? 0 : record.getResetCount();
                    Long skipCount = record.getSkipCount() == null ? 0 : record.getSkipCount();

                    excel.setDate(date);
                    excel.setSchoolId(SafeConverter.toString(schoolId));
                    excel.setSchoolName(SafeConverter.toString(schoolName));
                    excel.setClazzLevel(SafeConverter.toString(clazzLevel));
                    excel.setClazzId(SafeConverter.toString(clazzId));
                    excel.setClazzName(SafeConverter.toString(clazzName));
                    excel.setStudentId(SafeConverter.toString(studentId));
                    excel.setStudentName(SafeConverter.toString(studentName));
                    excel.setScore(SafeConverter.toString(score));
                    excel.setResetCount(SafeConverter.toString(resetCount));
                    excel.setSkipCount(SafeConverter.toString(skipCount));

                    resultList.add(excel);
                }
            }
        }
        return resultList;
    }


}
