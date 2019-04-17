package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareComment;
import com.voxlearning.utopia.service.campaign.api.constant.EvaluationParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareDownload;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 课件大赛评价服务提供方
 *
 * @Author: peng.zhang
 * @Date: 2018/10/12
 */
@ServiceVersion(version = "20181015")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TeacherCoursewareEvaluationService {

    List<TeacherCoursewareComment> fetchEvaluationByTeacherId(Long teacherId, String coursewareId);

    Long loadCountByTeacherId(Long teacherId, Date startTime, Date endTime);

    MapMessage createPersonalEvaluation(EvaluationParam param);

    TeacherCoursewareDownload loadDownloadInfo(Long teacherId);

    void createDownloadInfo(Long teacherId, String courseId, Integer alreadyDownloadNum, Integer allowDownloadNum);

    MapMessage incAlreadyDownloadNum(Long teacherId);

    MapMessage incAllowDownloadNum(Long teacherId, Integer downloadNum);

    MapMessage incAllowLotteryInfo(Long teacherId, Integer lotteryNum);

    MapMessage incAlreadyLotteryInfo(Long teacherId, Integer lotteryNum);

    void createStatisticsInfo(Long createTeacherId, String courseId, Long operateTeacherId,
                              String operateType, String isAuthentication);

    List<Map<String, Object>> fetchTeacherCommentNum(Date startTime, Date endTime);
}
