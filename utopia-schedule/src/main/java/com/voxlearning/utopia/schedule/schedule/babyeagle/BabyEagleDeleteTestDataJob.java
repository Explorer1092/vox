package com.voxlearning.utopia.schedule.schedule.babyeagle;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.BabyEagleType;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleClassHour;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.StudentLearnCourseRecord;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.StudentLearnCourseRecordForChinaCulture;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleChinaCultureLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleChinaCultureServiceClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @author fugui.chang
 * @since 2017/7/13
 */
@Named
@ScheduledJobDefinition(
        jobName = "小鹰学堂删除测试数据",
        jobDescription = "小鹰学堂删除测试数据,手工执行",
        disabled = {Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 11 30 7 ? ",
        ENABLED = false
)
@ProgressTotalWork(100)
public class BabyEagleDeleteTestDataJob extends ScheduledJobWithJournalSupport {

    @Inject
    private BabyEagleLoaderClient babyEagleLoaderClient;
    @Inject
    private BabyEagleServiceClient babyEagleServiceClient;
    @Inject
    private BabyEagleChinaCultureLoaderClient babyEagleChinaCultureLoaderClient;
    @Inject
    private BabyEagleChinaCultureServiceClient babyEagleChinaCultureServiceClient;


//    {
//        "flag":true,
//        "type":"byCourseId",
//        "courseId":""
//    }

//    {
//        "flag":true,
//        "type":"byClassHourId",
//        "classHourId":""
//    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Boolean flag = (Boolean) parameters.get("flag");
        if (!SafeConverter.toBoolean(flag)) {
            logger.warn("BabyEagleDeleteTestDataJobWarn flag wrong; flag : {}", flag);
            return;
        }

        String type = (String) parameters.get("type");
        BabyEagleType babyEagleType = BabyEagleType.valueOf((String) parameters.get("babyEagleType"));
        if (babyEagleType == null)
            return;
        if (StringUtils.equals(type, "byCourseId")) {
            //根据内容id,删除内容,删除内容下的所有课时,删除所有课时对应的学生记录
            String courseId = (String) parameters.get("courseId");
            deleteByCourseId(courseId, babyEagleType);
        } else if (StringUtils.equals(type, "byClassHourId")) {
            //根据课时id,删除课时以及课时下的所有学生记录
            String classHourId = (String) parameters.get("classHourId");
            deleteByClassHourId(classHourId, babyEagleType);
        } else {
            logger.warn("BabyEagleDeleteTestDataJobWarn type wrong; type : {}", type);
        }
    }

    private void deleteByCourseId(String courseId, BabyEagleType babyEagleType) {
        if (StringUtils.isEmpty(courseId)) {
            return;
        }
        List<BabyEagleClassHour> babyEagleClassHours = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleClassHourByCourseIdFromDB(courseId).getUninterruptibly();
        for (BabyEagleClassHour babyEagleClassHour : babyEagleClassHours) {
            deleteByClassHourId(babyEagleClassHour.getId(), babyEagleType);
        }
        babyEagleServiceClient.getRemoteReference().deleteBabyEagleCourseInfo(courseId);
    }


    private void deleteByClassHourId(String classHourId, BabyEagleType babyEagleType) {
        if (StringUtils.isEmpty(classHourId)) {
            return;
        }

        if (babyEagleType == BabyEagleType.BaseSchool) {
            List<StudentLearnCourseRecord> studentLearnCourseRecords = babyEagleLoaderClient.getBabyEagleLoader().loadStudentLearnCourseRecordListByClassHourId(classHourId).getUninterruptibly();
            if (CollectionUtils.isNotEmpty(studentLearnCourseRecords)) {
                //删除StudentLearnCourseRecord
                for (StudentLearnCourseRecord studentLearnCourseRecord : studentLearnCourseRecords) {
                    babyEagleServiceClient.getRemoteReference().deleteStudentLearnCourseRecord(studentLearnCourseRecord.getId());
                    babyEagleServiceClient.getRemoteReference().updateStudentLearnInfo(studentLearnCourseRecord.getStudentId());
                }
            }
        } else if (babyEagleType == BabyEagleType.ChinaCulture) {
            List<StudentLearnCourseRecordForChinaCulture> studentLearnCourseRecords = babyEagleChinaCultureLoaderClient.getRemoteReference().loadStudentLearnCourseRecordListByClassHourId(classHourId).getUninterruptibly();
            if (CollectionUtils.isNotEmpty(studentLearnCourseRecords)) {
                //删除StudentLearnCourseRecord
                for (StudentLearnCourseRecordForChinaCulture studentLearnCourseRecord : studentLearnCourseRecords) {
                    babyEagleChinaCultureServiceClient.getRemoteReference().deleteStudentLearnCourseRecord(studentLearnCourseRecord.getId());
                }
            }
        }
        //删除classHourId
        babyEagleServiceClient.getRemoteReference().deleteClassHour(classHourId);
    }
}
