package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.campaign.api.TeacherCoursewareEvaluationService;
import com.voxlearning.utopia.service.campaign.api.constant.EvaluationParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareComment;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareDownload;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareStatistics;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareCommentDao;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareDownloadDao;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareStatisticsDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: peng.zhang
 * @Date: 2018/10/12
 */
@Named
@ExposeService(interfaceClass = TeacherCoursewareEvaluationService.class)
public class TeacherCoursewareEvaluationServiceImpl implements TeacherCoursewareEvaluationService {

    @Inject
    private TeacherCoursewareCommentDao teacherCoursewareCommentDao;

    @Inject
    private TeacherCoursewareDownloadDao teacherCoursewareDownloadDao;

    @Inject
    private TeacherCoursewareStatisticsDao teacherCoursewareStatisticsDao;

    @AlpsQueueProducer(queue = "utopia.campaign.teacher.courseware.evaluation.exchange")
    private MessageProducer coursewareProducer;

    @Override
    public List<TeacherCoursewareComment> fetchEvaluationByTeacherId(Long teacherId, String coursewareId) {
        return teacherCoursewareCommentDao.loadTeacherCoursewareComment(teacherId,coursewareId);
    }

    @Override
    public Long loadCountByTeacherId(Long teacherId, Date startTime, Date endTime){
        return teacherCoursewareCommentDao.loadCountByTeacherId(teacherId,startTime,endTime);
    }

    @Override
    public MapMessage createPersonalEvaluation(EvaluationParam param) {
        TeacherCoursewareComment teacherCoursewareComment = new TeacherCoursewareComment();
        if (CollectionUtils.isNotEmpty(param.getLabelList())){
            List<String> labelList = param.getLabelList();
            for (int i = 0 ; i < labelList.size() ; i++){
                switch (i){
                    case 0:
                        teacherCoursewareComment.setComment_one(labelList.get(i));
                        break;
                    case 1:
                        teacherCoursewareComment.setComment_two(labelList.get(i));
                        break;
                    case 2:
                        teacherCoursewareComment.setComment_three(labelList.get(i));
                        break;
                    case 3:
                        teacherCoursewareComment.setComment_four(labelList.get(i));
                        break;
                    case 4:
                        teacherCoursewareComment.setComment_five(labelList.get(i));
                        break;
                }
            }
        }
        teacherCoursewareComment.setTeacher_id(param.getTeacherId());
        teacherCoursewareComment.setCourseware_id(param.getCoursewareId());
        teacherCoursewareComment.setStar(param.getStar());
        teacherCoursewareComment.setKey_word(param.getKeyWord());
        teacherCoursewareComment.setAuthentication(param.getIsAuthentication());
        teacherCoursewareCommentDao.insert(teacherCoursewareComment);
        // 更新其他字段
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("comment_one", teacherCoursewareComment.getComment_one());
        message.put("comment_two", teacherCoursewareComment.getComment_two());
        message.put("comment_three", teacherCoursewareComment.getComment_three());
        message.put("comment_four", teacherCoursewareComment.getComment_four());
        message.put("comment_five", teacherCoursewareComment.getComment_five());
        message.put("id", teacherCoursewareComment.getCourseware_id());
        message.put("star", teacherCoursewareComment.getStar());
        message.put("teacherId", param.getTeacherId());
        message.put("authentication", "Y".equals(teacherCoursewareComment.getAuthentication()));
        coursewareProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        return MapMessage.successMessage().set("id",param.getCoursewareId());
    }

    @Override
    public TeacherCoursewareDownload loadDownloadInfo(Long teacherId){
        List<TeacherCoursewareDownload> dataList = teacherCoursewareDownloadDao.loadCoursewareDownloadInfo(teacherId);
        if (CollectionUtils.isNotEmpty(dataList)) {
            return dataList.get(0);
        }

        return null;
    }

    @Override
    public void createDownloadInfo(Long teacherId, String courseId, Integer alreadyDownloadNum, Integer allowDownloadNum){
        TeacherCoursewareDownload teacherCoursewareDownload = new TeacherCoursewareDownload();
        teacherCoursewareDownload.setTeacher_id(teacherId);
        teacherCoursewareDownload.setCourseware_id(courseId);
        teacherCoursewareDownload.setAlready_download_times(alreadyDownloadNum);
        teacherCoursewareDownload.setAllow_download_times(allowDownloadNum);
        teacherCoursewareDownload.setAlready_lottery_times(0);
        teacherCoursewareDownload.setAllow_lottery_times(0);
        teacherCoursewareDownloadDao.upsert(teacherCoursewareDownload);
    }

    @Override
    public MapMessage incAlreadyDownloadNum(Long teacherId){
        TeacherCoursewareDownload downloadInfo = loadDownloadInfo(teacherId);
        if (downloadInfo != null) {
            downloadInfo.setAlready_download_times(downloadInfo.getAlready_download_times() + 1);
            teacherCoursewareDownloadDao.upsert(downloadInfo);
        } else {
            createDownloadInfo(teacherId, "", 1, 3);
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage incAllowDownloadNum(Long teacherId, Integer delta){
        TeacherCoursewareDownload downloadInfo = loadDownloadInfo(teacherId);
        if (downloadInfo != null) {
            downloadInfo.setAllow_download_times(SafeConverter.toInt(downloadInfo.getAllow_download_times()) + delta);
            teacherCoursewareDownloadDao.upsert(downloadInfo);
        } else {
            createDownloadInfo(teacherId, "", 0, 3 + delta);
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage incAllowLotteryInfo(Long teacherId, Integer lotteryNum){
        TeacherCoursewareDownload downloadInfo = loadDownloadInfo(teacherId);
        if (downloadInfo != null) {
            if (lotteryNum == -1){
                // 如果是 -1 就清空
                downloadInfo.setAllow_lottery_times(0);
            } else {
                downloadInfo.setAllow_lottery_times(SafeConverter.toInt(downloadInfo.getAllow_lottery_times())
                        + lotteryNum);
            }
            teacherCoursewareDownloadDao.upsert(downloadInfo);
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage incAlreadyLotteryInfo(Long teacherId, Integer lotteryNum){
        TeacherCoursewareDownload downloadInfo = loadDownloadInfo(teacherId);
        if (downloadInfo != null) {
            downloadInfo.setAlready_lottery_times(SafeConverter.toInt(downloadInfo.getAlready_lottery_times())
                    + lotteryNum);
            teacherCoursewareDownloadDao.upsert(downloadInfo);
        }

        return MapMessage.successMessage();
    }

    @Override
    public void createStatisticsInfo(Long createTeacherId, String courseId, Long operateTeacherId,
                                     String operateType,String isAuthentication){
        TeacherCoursewareStatistics teacherCoursewareStatistics = new TeacherCoursewareStatistics();
        teacherCoursewareStatistics.setCreate_teacher_id(createTeacherId);
        teacherCoursewareStatistics.setCourseware_id(courseId);
        teacherCoursewareStatistics.setOperate_teacher_id(operateTeacherId);
        teacherCoursewareStatistics.setType(operateType);
        teacherCoursewareStatistics.setAuthentication(isAuthentication);
        teacherCoursewareStatisticsDao.insert(teacherCoursewareStatistics);
    }

    @Override
    public List<Map<String, Object>> fetchTeacherCommentNum(Date startTime, Date endTime){
        return teacherCoursewareCommentDao.loadTeacherCommentNumInfo(startTime,endTime);
    }

}
