package com.voxlearning.utopia.service.campaign.impl.service.excel;

import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.activity.TangramEntryRecord;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.service.campaign.impl.service.StudentActivityServiceImpl;
import com.voxlearning.utopia.service.campaign.impl.service.excel.model.TangramScoreExcel;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.slf4j.Logger;

import java.util.*;

@ThreadSafe
public class TangramAllActivityExportScore extends TangramActivityExportScore {

    private static final Logger log = LoggerFactory.getLogger(TangramAllActivityExportScore.class);

    public TangramAllActivityExportScore(ActivityConfigServiceClient activityConfigServiceClient,
                                         EmailServiceClient emailServiceClient,
                                         StudentLoaderClient studentLoaderClient,
                                         StudentActivityServiceImpl studentActivityService,
                                         RaikouSystem raikouSystem) {
        super(activityConfigServiceClient, emailServiceClient, studentLoaderClient, studentActivityService, raikouSystem);
    }

    @Override
    protected String filePrefix() {
        return "所有成绩";
    }

    @Override
    protected List<TangramScoreExcel> getExcelList(ActivityConfig activityConfig) {
        List<TangramScoreExcel> resultList = new ArrayList<>();

        Long count = studentActivityService.loadAllTangramRecordsForStatCount(activityConfig.getId());
        if (RuntimeMode.isProduction() && count > 20000) {
            log.error("活动成绩导出结果过大,请人工参与! activityId:" + activityConfig.getId());
            return Collections.emptyList();
        }

        List<TangramEntryRecord> recordList = studentActivityService.loadAllTangramRecordsForStat(activityConfig.getId());

        for (TangramEntryRecord record : recordList) {
            StudentDetail sd = studentLoaderClient.loadStudentDetail(record.getUserId());
            if (sd == null) continue;

            Map<Long, Integer> scoreMap = record.getScoreMap();
            Map<Integer, Set<Long>> maxScoreDays = getMaxScoreDays(scoreMap);

            for (Map.Entry<Integer, Set<Long>> entry : maxScoreDays.entrySet()) {
                Integer score = entry.getKey();
                Set<Long> days = entry.getValue();
                for (Long dayTimeStamp : days) {
                    TangramScoreExcel excel = new TangramScoreExcel();

                    String date = DateFormatUtils.format(new Date(dayTimeStamp), "MM-dd");
                    String area = Optional.ofNullable(raikouSystem.loadRegion(sd.getStudentSchoolRegionCode()))
                            .map(Region::getName)
                            .orElse("未知");
                    Long schoolId = Optional.ofNullable(sd.getClazz()).map(Clazz::getSchoolId).orElse(0L);
                    String schoolName = sd.getStudentSchoolName();
                    String clazzLevel = "";
                    if (sd.getClazz() != null && sd.getClazz().getClazzLevel() != null) {
                        clazzLevel = SafeConverter.toString(sd.getClazz().getClazzLevel().getDescription());
                    }
                    Long clazzId = sd.getClazzId();
                    String clazzName = "";
                    if (sd.getClazz() != null && sd.getClazz().getClazzLevel() != null) {
                        clazzName = sd.getClazz().formalizeClazzName();
                    }
                    Long studentId = record.getUserId();
                    String studentName = sd.fetchRealname();

                    excel.setDate(date);
                    excel.setArea(area);
                    excel.setSchoolId(SafeConverter.toString(schoolId));
                    excel.setSchoolName(SafeConverter.toString(schoolName));
                    excel.setClazzLevel(SafeConverter.toString(clazzLevel));
                    excel.setClazzId(SafeConverter.toString(clazzId));
                    excel.setClazzName(SafeConverter.toString(clazzName));
                    excel.setStudentId(SafeConverter.toString(studentId));
                    excel.setStudentName(SafeConverter.toString(studentName));
                    excel.setScore(SafeConverter.toString(score));

                    resultList.add(excel);
                }
            }
        }
        return resultList;
    }


}
