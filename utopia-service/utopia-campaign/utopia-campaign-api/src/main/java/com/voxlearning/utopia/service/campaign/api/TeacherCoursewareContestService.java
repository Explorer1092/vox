package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.constant.AwardParam;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareBookInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewarePageInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareStatistics;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181111")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TeacherCoursewareContestService {

    MapMessage createSimpleCourseware(Long teacherId);

    MapMessage upsertCourseware(TeacherCourseware courseware);

    MapMessage updateCoursewareBookInfo(String id, TeacherCoursewareBookInfo bookInfo);

    MapMessage updateCoursewareFileInfo(String id, String fileUrl, String fileName);

    MapMessage updateCoursewareContent(String id, String title, String description);

    MapMessage updateCourseToExamining(String id);

    MapMessage deleteCoureware(String id);

    MapMessage updateCourseToExamine(String id, String updater);

    MapMessage updateCourseExamined(String id, String updater, boolean pass, String extInfo);

    List<TeacherCourseware> fetchCourseWareListByTeacher(Long teacherId);

    List<TeacherCourseware> fetchCourseWareListByExamStatus(String examStatus);

    TeacherCourseware fetchCourseWareDetailById(String id);

    /**
     * only for job
     */

    List<TeacherCourseware> loadExaminingCoursewareList();

    long countByTeacherId(TeacherCoursewareParam param);

    Long count(TeacherCoursewareParam param);

    List<TeacherCourseware> fetchTeacherCoursewarByPage(TeacherCoursewarePageInfo teacherCoursewarePageInfo);

    MapMessage updateCoursewareWordFileInfo(String id, String fileUrl, String fileName);

    MapMessage updateCoursewarePictureFileInfo(String id, String fileUrl, String fileName);

    MapMessage updateCover(String id, String fileUrl, String fileName,Boolean isUserUpload);

    MapMessage deletePicture(String id, List<Map<String, String>> pictureList);

    MapMessage updateCompressedFiles(String id, String fileUrl, String fileName);

    MapMessage updateStatus(String id, String status);

    MapMessage updateFileUrl(String id, String fileUrl, String name);

    List<TeacherCourseware> fetchNewestInfo(int limitNum);

    List<TeacherCourseware> fetchAllTeacherCoursewarByPage(TeacherCoursewarePageInfo teacherCoursewarePageInfo);

    MapMessage updateNewWordUrl(String id, String fileUrl );

    MapMessage cleanPptCoursewareInfo(String id);

    VersionedBufferData<List<TeacherCourseware>> loadTeacherCoursewareBufferData(Long version);

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void resetTeacherCoursewareBuffer();

    MapMessage updateVisitNum(String courseId, Integer visitNum);

    MapMessage updateDownloadNum(String courseId, Integer visitNum);

    MapMessage updateCommentNum(String courseId, Integer commentNum);

    MapMessage updatePptCoursewareFile(String id, String fileUrl, String fileName);

    MapMessage updateZipFile(String id, String fileUrl);

    MapMessage updateResourceName(String id, String pptName, String docName);

    MapMessage updateResourceName(String id, String pptName, String docName, String unPackagePptName);

    MapMessage updateCoursewareAwardInfo(AwardParam param);

    Long fetchMaxDownloadCourse();

    List<Map<String, Object>> fetchStatisticsInfoByCourseId(Date startTime, Date endTime);

    List<Map<String, Object>> fetchStatisticsInfoByTeacherId(Date startTime, Date endTime);

    List<Map<String, Object>> fetchDownloadStatInfo(Date startTime, Date endTime);

    List<TeacherCoursewareStatistics> loadByCoursewareId(String coursewareId, String operationType, Date startTime, Date endTime);

    List<TeacherCoursewareStatistics> loadTeacherCanvassRecords(Long tid);

    /**
     * 投票
     */
    MapMessage canvassVote(TeacherDetail teacher, String courseId, Long createTeacherId);
    MapMessage canvassVote(String openId, String courseId, Long createTeacherId);

    /**
     * 拉票
     */
    MapMessage canvassHelper(String courseId, Long teacherId);

    MapMessage canvassHelper(String courseId, String openId);

    /**
     * 对当前作品的剩余次数
     */
    Map<String,Integer> surplus(Long teacherId, String openId, String courseId);

    /**
     * 获取课件大赛人气榜日榜数据
     * @param subjectName 学科
     * @param date 日期，yyyyMMdd格式
     * @return 人气榜日榜数据
     */
    MapMessage loadDailyPopularityRanking(String subjectName, String date);

    /**
     * 获取课件大赛人气榜周榜数据
     * @param subjectName 学科
     * @param week 期数，从10/22开始，第一周是1，第二周是2，以此类推
     * @return 人气榜周榜数据
     */
    MapMessage loadWeeklyPopularityRanking(String subjectName, Integer week);

    /**
     * 获取课件大赛人气榜总榜数据
     * @param subjectName 学科
     * @return
     */
    MapMessage loadTotalPopularityRanking(String subjectName);

    /**
     * 获取课件大赛达人榜日榜数据
     * @param date
     * @return
     */
    MapMessage loadDailyTalentRanking(String date);

    /**
     * 获取课件大赛达人榜周榜数据
     * @param week 期数
     * @return
     */
    MapMessage loadWeeklyTalentRanking(Integer week);

    /**
     * 获取课件大赛达人榜总榜数据
     * @return
     */
    MapMessage loadTotalTalentRanking();

    /**
     * 获取课件大赛榜单前三，达人榜和人气榜，取日榜前三，优秀作品榜，取周榜前三
     * @return
     */
    MapMessage loadTop3Ranking();

    /**
     * 获取人气作品榜宣传信息
     * @return
     */
    MapMessage loadPopularityShowInfo();

    /**
     * 获取达人作品榜宣传信息
     * @return
     */
    MapMessage loadTalentShowInfo();

    /**
     * 获取高分作品榜宣传信息
     * @return
     */
    MapMessage loadExcellentShowInfo();

    List<Map<String, Object>> updateCourseInfo(List<Map<String, Object>> rankingData);

    Integer loadCourseShareNum(String courseId);

    MapMessage loadCanvassData(String subject);

    MapMessage loadUserCanvassInfo(String subject, TeacherDetail teacher, String openId);

}
