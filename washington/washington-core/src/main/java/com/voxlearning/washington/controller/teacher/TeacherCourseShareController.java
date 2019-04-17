package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareDownload;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareContestServiceClient;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareEvaluationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

/**
 * 课件评分享相关接口
 *
 * @Author: peng.zhang
 * @Date: 2018/10/11
 */
@Controller
@RequestMapping("/courseware/share")
public class TeacherCourseShareController extends AbstractController {

    @Inject
    private TeacherCoursewareEvaluationServiceClient evaluationServiceClient;

    @Inject
    private TeacherCoursewareContestServiceClient teacherCoursewareContestServiceClient;

    public static final Integer FREE_DOWNLOAD_TIMES = 3;

    public static final Integer SHARE_ADD_DOWNLOAD_TIMES = 5;

    public static final Integer NORMAL_ADD_DOWNLOAD_TIMES = 1;

    public static final Integer EMPTY_DOWNLOAD_TIMES = 0;

    public static final String DOWNLOAD_TYPE = "download";

    public static final String SHARE_TYPE = "share";

    /**
     * 更新下载次数
     * @return
     */
    @RequestMapping(value = "updateDownloadInfo.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage updateDownloadInfo(){
        String coursewareId = getRequestString("coursewareId");
        if (StringUtils.isEmpty(coursewareId)) {
            return MapMessage.errorMessage();
        }

        updateTotalDownloadTimes(coursewareId);

        return MapMessage.successMessage();

        /*TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录");
        }

        Long teacherId = teacherDetail.getId();
        String coursewareId = getRequestString("coursewareId");
        Long createTeacherId = getRequestLong("createTeacherId");
        TeacherCoursewareDownload downloadInfo = evaluationServiceClient.fetchDownLoadInfo(teacherId);
        String isAuthentication = teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS ? "Y" : "N";

        evaluationServiceClient.createStatisticsInfo(createTeacherId, coursewareId, teacherId,DOWNLOAD_TYPE, isAuthentication);

        // 首次下载
        if (downloadInfo == null){
            evaluationServiceClient.createDownloadInfo(teacherId, coursewareId, NORMAL_ADD_DOWNLOAD_TIMES, FREE_DOWNLOAD_TIMES);
            updateTotalDownloadTimes(coursewareId);
            return MapMessage.successMessage();
        } else {
            // 不是分享就正常的加次数,首先判断下载次数是否用完
            Boolean result = addDownloadTimes(downloadInfo, teacherId, coursewareId);
            return result ? MapMessage.successMessage() : MapMessage.errorMessage().setInfo("下载次数已用完");
        }*/
    }

    /**
     * 更新作品的总下载次数
     * @param coursewareId
     */
    public void updateTotalDownloadTimes(String coursewareId){
        TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.
                fetchCoursewareDetailById(coursewareId);
        Integer downloadNum = teacherCourseware.getDownloadNum() == null ? 0
                : teacherCourseware.getDownloadNum();
        teacherCoursewareContestServiceClient.updateDownloadNum(coursewareId,downloadNum + 1 );
    }

    /**
     * 更新个人下载次数表信息
     * @param teacherCoursewareDownload 个人下载次数及抽奖次数表
     * @param teacherId 老师 ID
     * @param coursewareId 课件 ID
     * @return
     */
    public boolean addDownloadTimes(TeacherCoursewareDownload teacherCoursewareDownload, Long teacherId, String coursewareId){
        try {
            if (teacherCoursewareDownload.getAlready_download_times() < teacherCoursewareDownload.getAllow_download_times()) {
                evaluationServiceClient.incAlreadyDownloadNum(teacherId);
                updateTotalDownloadTimes(coursewareId);
                return true;
            } else {
                return false;
            }
        } catch (CannotAcquireLockException e) {
            return false;
        }
    }

    /**
     * 分享对应的操作
     * @param teacherCoursewareDownload 个人下载次数及抽奖次数表
     * @param teacherId 老师 ID
     * @param coursewareId 课件 ID
     */
    public void shareOperation(TeacherCoursewareDownload teacherCoursewareDownload, Long teacherId, String coursewareId){
        try {
            // 分享一次增加 5 次下载次数
            evaluationServiceClient.incAllowDownloadNum(teacherId, SHARE_ADD_DOWNLOAD_TIMES);

            // 分享一次增加 1 次抽奖次数
            addLotteryNum(teacherCoursewareDownload, teacherId);
        } catch (CannotAcquireLockException e) {
            // do nothing here
        }
    }

    /**
     * 获取几天前的时间
     * @param d
     * @param day
     * @return
     */
    public static Date getDateBefore(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return now.getTime();
    }

    /**
     * 更新抽奖次数信息
     * @param teacherCoursewareDownload 个人下载次数及抽奖次数表
     * @param teacherId
     */
    public void addLotteryNum(TeacherCoursewareDownload teacherCoursewareDownload, Long teacherId){
        Date dayBeforeSeven = getDateBefore(new Date(),7);
        if (teacherCoursewareDownload.getAllow_lottery_times() >= 10 && dayBeforeSeven.before(teacherCoursewareDownload.getCreateTime())){
            // 次数大于 10 并且创建时间在一周之内, 不能新增抽奖次数
            // 次数为 0
            evaluationServiceClient.incAllowLotteryNum(teacherId, 0);
        } else if (teacherCoursewareDownload.getAllow_lottery_times() >= 10 && dayBeforeSeven.after(teacherCoursewareDownload.getCreateTime())){
            // 次数大于 10 并且创建时间在一周之外,重置允许抽奖次数
            // 设置 -1 即清空允许抽奖次数(重置)
            evaluationServiceClient.incAllowLotteryNum(teacherId, -1);
        } else {
            evaluationServiceClient.incAllowLotteryNum(teacherId, 1);
        }
    }

    public void addAllowLotteryNum(Long teacherId,Integer lotteryNum){
        evaluationServiceClient.incAlreadyLotteryNum(teacherId,1);
    }

    /**
     * 分享操作更新下载次数和抽奖次数
     * @return
     */
    @RequestMapping(value = "downloadAndLottery.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage updateDownloadAndLottery(){
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录");
        }
        Long teacherId = teacherDetail.getId();
        Long createTeacherId = getRequestLong("createTeacherId");
        String coursewareId = getRequestString("coursewareId");
        TeacherCoursewareDownload downLoadInfo = evaluationServiceClient.fetchDownLoadInfo(teacherId);
        if (downLoadInfo == null) {
            evaluationServiceClient.createDownloadInfo(teacherId,coursewareId,EMPTY_DOWNLOAD_TIMES, FREE_DOWNLOAD_TIMES);
            downLoadInfo = evaluationServiceClient.fetchDownLoadInfo(teacherId);
        }

        String isAuthentication = teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS ? "Y" : "N";
        // 分享过来的对抽奖次数和下载次数进行处理
        evaluationServiceClient.createStatisticsInfo(createTeacherId, coursewareId, teacherId, SHARE_TYPE, isAuthentication);
        shareOperation(downLoadInfo, teacherId, coursewareId);
        teacherCoursewareContestServiceClient.clearCourseShareNum(coursewareId);
        return MapMessage.successMessage();
    }

    /**
     * 是否可以下载
     * @return true 和 false
     */
    @RequestMapping(value = "couldDownload.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage couldDownload(){
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage().setInfo("请先登录");
            }

            Long teacherId = teacherDetail.getId();
            String coursewareId = getRequestString("coursewareId");
            if (null == teacherId || StringUtils.isBlank(coursewareId)){
                return MapMessage.errorMessage().setInfo("参数错误");
            }

            Boolean result = evaluationServiceClient.couldDownloadCourseware(teacherId);
            return MapMessage.successMessage().set("couldDownload",result);
        } catch (Exception e){
            return MapMessage.errorMessage().set("error",e.getMessage());
        }
    }

}
