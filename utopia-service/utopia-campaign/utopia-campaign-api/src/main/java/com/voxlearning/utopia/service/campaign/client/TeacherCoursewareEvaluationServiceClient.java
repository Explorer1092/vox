package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.service.campaign.api.TeacherCoursewareEvaluationService;
import com.voxlearning.utopia.service.campaign.api.constant.EvaluationParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareComment;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareDownload;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 课件评价服务调用方
 *
 * @Author: peng.zhang
 * @Date: 2018/10/12
 */
@Slf4j
public class TeacherCoursewareEvaluationServiceClient {

    @ImportService(interfaceClass = TeacherCoursewareEvaluationService.class)
    private TeacherCoursewareEvaluationService teacherCoursewareEvaluationService;

    public List<TeacherCoursewareComment> fetchEvaluationByTeacherId(Long teacherId,
                                                                     String coursewareId){
        if(null == teacherId || StringUtils.isBlank(coursewareId)) {
            return Collections.emptyList();
        }
        return teacherCoursewareEvaluationService.fetchEvaluationByTeacherId(teacherId,coursewareId);
    }

    public Long loadCountByTeacherId(Long teacherId, Date startTime, Date endTime){
        if(null == teacherId) {
            return 0L;
        }
        return teacherCoursewareEvaluationService.loadCountByTeacherId(teacherId,startTime,endTime);
    }

    public Date getDateBefore(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return now.getTime();
    }

    /**
     * 是否可以评价
     * @param teacherId
     * @return
     */
    public Boolean couldEvaluate(Long teacherId){
        Date now = new Date();
        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();
        calendarStart.setTime(now);
        calendarEnd.setTime(now);
        calendarStart.set(Calendar.HOUR,0);
        calendarStart.set(Calendar.MINUTE,0);
        calendarStart.set(Calendar.SECOND,0);
        calendarStart.set(Calendar.MILLISECOND,0);
        calendarEnd.set(Calendar.HOUR,23);
        calendarEnd.set(Calendar.MINUTE,59);
        calendarEnd.set(Calendar.SECOND,59);
        calendarEnd.set(Calendar.MILLISECOND,999);
        Long evaluationCount = loadCountByTeacherId(teacherId,calendarStart.getTime(),calendarEnd.getTime());
        if (evaluationCount < DEFAULT_EVALUATION_COUNT){
            return true;
        } else {
            return false;
        }
    }

    public MapMessage createEvaluation(EvaluationParam evaluationParam){
        try {
            // 一颗到四颗星,至少有一个标签;五颗星,可以没有标签
            Integer star = evaluationParam.getStar();
            if (!Objects.equals(star, FIVE_STAR) && CollectionUtils.isEmpty(evaluationParam.getLabelList())){
                return MapMessage.errorMessage().setInfo("星级少于五星的标签不能为空");
            }

            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherCoursewareEvaluationServiceClient.createEvaluation")
                    .keys(evaluationParam.getTeacherId())
                    .callback(() -> teacherCoursewareEvaluationService.createPersonalEvaluation(evaluationParam))
                    .build()
                    .execute();
//
//            MapMessage message = teacherCoursewareEvaluationService.createPersonalEvaluation(evaluationParam);
//            if (!message.isSuccess()){
//                return MapMessage.errorMessage().setInfo(message.getInfo());
//            }
//            return MapMessage.successMessage();
        } catch (Exception e){
            log.error("createEvaluation failed", e);
            return MapMessage.errorMessage().set("error",e);
        }
    }

    public Boolean couldDownloadCourseware(Long teacherId){
        TeacherCoursewareDownload downloadInfo = teacherCoursewareEvaluationService.loadDownloadInfo(teacherId);
        if (downloadInfo == null) {
            return true;
        }

        Integer allowDownloadTimes = SafeConverter.toInt(downloadInfo.getAllow_download_times());
        Integer alreadyDownloadTimes = SafeConverter.toInt(downloadInfo.getAlready_download_times());

        return allowDownloadTimes == 0 || allowDownloadTimes > alreadyDownloadTimes;
    }

    public MapMessage incAlreadyDownloadNum(Long teacherId){
        return AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("TeacherCoursewareEvaluationServiceClient.updateDownloadInfo")
                .keys(teacherId)
                .callback(() -> teacherCoursewareEvaluationService.incAlreadyDownloadNum(teacherId))
                .build()
                .execute();
    }

    public MapMessage incAllowDownloadNum(Long teacherId, Integer downloadNum){
        return AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("TeacherCoursewareEvaluationServiceClient.updateDownloadInfo")
                .keys(teacherId)
                .callback(() -> teacherCoursewareEvaluationService.incAllowDownloadNum(teacherId, downloadNum))
                .build()
                .execute();
    }

    public MapMessage incAllowLotteryNum(Long teacherId, Integer lotteryNum){
        return AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("TeacherCoursewareEvaluationServiceClient.updateDownloadInfo")
                .keys(teacherId)
                .callback(() -> teacherCoursewareEvaluationService.incAllowLotteryInfo(teacherId, lotteryNum))
                .build()
                .execute();
    }

    public MapMessage incAlreadyLotteryNum(Long teacherId, Integer lotteryNum){
        return AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("TeacherCoursewareEvaluationServiceClient.updateDownloadInfo")
                .keys(teacherId)
                .callback(() -> teacherCoursewareEvaluationService.incAlreadyLotteryInfo(teacherId, lotteryNum))
                .build()
                .execute();
    }

    public TeacherCoursewareDownload fetchDownLoadInfo(Long teacherId){
        if(null == teacherId) {
            return null;
        }
        return teacherCoursewareEvaluationService.loadDownloadInfo(teacherId);
    }

    public MapMessage createDownloadInfo(Long teacherId, String courseId, Integer alreadyDownloadNum,
                                         Integer downloadNum){
        try {
            teacherCoursewareEvaluationService.createDownloadInfo(teacherId, courseId, alreadyDownloadNum, downloadNum);
            return MapMessage.successMessage();
        } catch (Exception e){
            log.error("createDownloadInfo failed", e);
            return MapMessage.errorMessage().set("error", e);
        }
    }

    public MapMessage createStatisticsInfo(Long createTeacherId,
                                           String courseId,
                                           Long operateTeacherId,
                                           String operateType,
                                           String isAuthentication){
        try {
            teacherCoursewareEvaluationService.createStatisticsInfo(createTeacherId,
                    courseId,
                    operateTeacherId,
                    operateType,
                    isAuthentication);
            return MapMessage.successMessage();
        } catch (Exception e){
            log.error("createStatisticsInfo failed", e);
            return MapMessage.errorMessage().set("error", e);
        }
    }

    public List<Map<String, Object>> fetchTeacherCommentNum(Date startTime, Date endTime){
        return teacherCoursewareEvaluationService.fetchTeacherCommentNum(startTime,endTime);
    }

    public static final Integer DEFAULT_EVALUATION_COUNT = 5;

    public static final Integer FIVE_STAR = 5;
}
