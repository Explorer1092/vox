package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.campaign.api.TeacherCoursewareContestService;
import com.voxlearning.utopia.service.campaign.api.buffer.TeacherCoursewareBuffer;
import com.voxlearning.utopia.service.campaign.api.constant.AwardParam;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareBookInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewarePageInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareStatistics;
import com.voxlearning.utopia.service.campaign.cache.TeacherCourseCache;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants.RANKING_START_DATE;

public class TeacherCoursewareContestServiceClient implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(TeacherCoursewareContestServiceClient.class);

    @Getter
    @ImportService(interfaceClass = TeacherCoursewareContestService.class)
    private TeacherCoursewareContestService teacherCoursewareContestService;

    private ManagedNearBuffer<List<TeacherCourseware>, TeacherCoursewareBuffer> teacherCoursewareBuffer;

    @Override
    public void afterPropertiesSet() throws Exception {
        NearBufferBuilder<List<TeacherCourseware>, TeacherCoursewareBuffer> builder = NearBufferBuilder.newBuilder();
        builder.nearBufferClass(TeacherCoursewareBuffer.class);
        builder.reloadNearBuffer(5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> teacherCoursewareContestService.loadTeacherCoursewareBufferData(-1L));
        builder.reloadNearBuffer((version, attributes) -> teacherCoursewareContestService.loadTeacherCoursewareBufferData(version));
        teacherCoursewareBuffer = builder.build();
    }

    public MapMessage createSimpleCourseware(TeacherDetail teacherDetail) {
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("用户为空");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("createSimpleCourseware")
                    .keys(teacherDetail.getId())
                    .callback(()-> teacherCoursewareContestService.createSimpleCourseware(teacherDetail.getId()))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed createSimpleCourseware for user {}", teacherDetail.getId(), ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage upsertCourseware(TeacherCourseware courseware) {
        return teacherCoursewareContestService.upsertCourseware(courseware);
    }

    public TeacherCourseware fetchCoursewareDetailById(String id) {
        if(StringUtils.isBlank(id)) {
            return null;
        }
        return teacherCoursewareContestService.fetchCourseWareDetailById(id);
    }

    public List<TeacherCourseware> fetchCourseWareListByTeacher(Long teacherId) {
        if(teacherId == null) {
            return Collections.emptyList();
        }
        return teacherCoursewareContestService.fetchCourseWareListByTeacher(teacherId);
    }

    public List<TeacherCourseware> fetchCourseWareListByPage(TeacherCoursewarePageInfo teacherCoursewarePageInfo){
        if(teacherCoursewarePageInfo.getTeacherId() == null) {
            return Collections.emptyList();
        }
        return teacherCoursewareContestService.fetchTeacherCoursewarByPage(teacherCoursewarePageInfo);
    }

    public MapMessage updateBookInfo(String id, TeacherCoursewareBookInfo bookInfo) {
        if(StringUtils.isBlank(id) || bookInfo == null) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateBookInfo")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCoursewareBookInfo(id, bookInfo))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateBookInfo id:{},content:{}", id, bookInfo, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage updateContent(String id, String title, String description) {
        if(StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateContent")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCoursewareContent(id, title, description))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateBookInfo id:{},title:{},description:{}", id, title, description, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }


    public MapMessage updateFileInfo(String id, String fileUrl, String fileName) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(fileUrl)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateFileInfo")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCoursewareFileInfo(id, fileUrl, fileName))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateFileInfo id:{},fileUrl:{}, fileName:{}", id, fileUrl, fileName, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage updateToExamining(String id) {
        if(StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateToExamining")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCourseToExamining(id))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateToExamining id:{} ", id, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage deleteCourseware(String id) {
        if(StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("deleteCourseware")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.deleteCoureware(id))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed deleteCourseware id:{} ", id, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }


    public MapMessage updateCourseToExamine(String id, String examiner) {
        if(StringUtils.isBlank(id) || StringUtils.isBlank(examiner)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateCourseToExamine")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCourseToExamine(id, examiner))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateCourseToExamine id:{},examiner:{} ", id, examiner, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage examineCourseToEnd(String id, String examiner, boolean pass, String extInfo) {
        if(StringUtils.isBlank(id) || StringUtils.isBlank(examiner)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("examineCourseToEnd")
                    .keys(examiner)
                    .callback(()-> teacherCoursewareContestService.updateCourseExamined(id, examiner, pass, extInfo))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed examineCourseToEnd id:{},examiner:{}, pass:{}", id, examiner, pass, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public long fetchTeacherCoursewareNumByTeacherId(TeacherCoursewareParam param){
        return teacherCoursewareContestService.countByTeacherId(param);
    }

    public long fetchTeacherCoursewareNum(TeacherCoursewareParam param){
        return teacherCoursewareContestService.count(param);
    }

    public MapMessage updateWordFileInfo(String id, String fileUrl, String fileName) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateWordFileInfo")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCoursewareWordFileInfo(id, fileUrl, fileName))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateWordFileInfo id:{},fileUrl:{}, fileName:{}", id, fileUrl, fileName, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage updatePictureFileInfo(String id, String fileUrl, String fileName) {
        if(StringUtils.isBlank(id) || StringUtils.isBlank(fileUrl) || StringUtils.isBlank(fileName)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updatePictureInfo")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCoursewarePictureFileInfo(id, fileUrl, fileName))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updatePictureInfo id:{},fileUrl:{}, fileName:{}", id, fileUrl, fileName, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage updateCompressedFileInfo(String id, String fileUrl, String fileName) {
        if(StringUtils.isBlank(id) || StringUtils.isBlank(fileUrl) || StringUtils.isBlank(fileName)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateCompressedInfo")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCompressedFiles(id, fileUrl, fileName))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateCompressedInfo id:{},fileUrl:{}, fileName:{}", id, fileUrl, fileName, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage updateCoverFile(String id, String fileUrl, String fileName,Boolean isUserUpload) {
        if(StringUtils.isBlank(id) || StringUtils.isBlank(fileUrl) || StringUtils.isBlank(fileName)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateCoverFileInfo")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCover(id, fileUrl, fileName,isUserUpload))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateCoverFileInfo id:{},fileUrl:{}, fileName:{}", id, fileUrl, fileName, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage deletePictureUrl(String id,List<Map<String,String>> pictureList) {
        return teacherCoursewareContestService.deletePicture(id,pictureList);
    }

    public List<TeacherCourseware> fetchNewestInfo(int limitNum){
        return teacherCoursewareContestService.fetchNewestInfo(limitNum);
    }

    public MapMessage updateStatus(String id,String status) {
        return teacherCoursewareContestService.updateStatus(id,status);
    }

    public MapMessage updateFileUrl(String id,String fileUrl, String name) {
        return teacherCoursewareContestService.updateFileUrl(id,fileUrl,name);
    }

    public List<TeacherCourseware> fetchAllCourseWareListByPage(TeacherCoursewarePageInfo teacherCoursewarePageInfo){
        return teacherCoursewareContestService.fetchAllTeacherCoursewarByPage(teacherCoursewarePageInfo);
    }

    public MapMessage updateNewWordFile(String id,String fileUrl){
        return teacherCoursewareContestService.updateNewWordUrl(id,fileUrl);
    }

    public List<TeacherCourseware> loadTeacherCoursewareAll() {
        return teacherCoursewareBuffer.getNativeBuffer().dump().getData();
    }

    public TeacherCourseware loadTeacherCoursewareById(String id) {
        return teacherCoursewareBuffer.getNativeBuffer().loadById(id);
    }

    public MapMessage updateVisitNum(String courseId,Integer visitNum){
        return teacherCoursewareContestService.updateVisitNum(courseId,visitNum);
    }

    public MapMessage updateDownloadNum(String courseId,Integer downloadNum){
        return teacherCoursewareContestService.updateDownloadNum(courseId,downloadNum);
    }

    public MapMessage updateCommentNum(String courseId,Integer commentNum){
        return teacherCoursewareContestService.updateCommentNum(courseId,commentNum);
    }

    public MapMessage updatePptCoursewareFile(String id,String fileUrl,String fileName){
        return teacherCoursewareContestService.updatePptCoursewareFile(id, fileUrl, fileName);
    }

    public MapMessage updateZipFile(String id, String fileUrl) {
        return teacherCoursewareContestService.updateZipFile(id, fileUrl);
    }

    public MapMessage updateResourceName(String id, String pptName, String docName) {
        return teacherCoursewareContestService.updateResourceName(id, pptName, docName);
    }

    public MapMessage updateAwardInfo(String id, String fileUrl, String fileName,String awardLevelName,Integer awardLevelId,String awardIntroduction) {
        if(StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().setInfo("参数异常");
        }
        AwardParam param = new AwardParam();
        param.setId(id);
        param.setFileUrl(fileUrl);
        param.setFileName(fileName);
        param.setAwardLevelName(awardLevelName);
        param.setAwardLevelId(awardLevelId);
        param.setAwardIntroduction(awardIntroduction);
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateAwardInfo")
                    .keys(id)
                    .callback(()-> teacherCoursewareContestService.updateCoursewareAwardInfo(param))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateAwardInfo id:{},fileUrl:{}, fileName:{}", id, fileUrl, fileName, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public Long fetchMaxDownloadCourse(){
        return teacherCoursewareContestService.fetchMaxDownloadCourse();
    }

    public List<Map<String, Object>> fetchStatisticsInfoByCourseId(Date startTime, Date endTime){
        return teacherCoursewareContestService.fetchStatisticsInfoByCourseId(startTime,endTime);
    }

    public List<Map<String, Object>> fetchStatisticsInfoByTeacherId(Date startTime, Date endTime){
        return teacherCoursewareContestService.fetchStatisticsInfoByTeacherId(startTime,endTime);
    }

    public List<Map<String, Object>> fetchDownloadStatInfo(Date startTime, Date endTime){
        return teacherCoursewareContestService.fetchDownloadStatInfo(startTime,endTime);
    }

    public List<TeacherCoursewareStatistics> loadByCoursewareId(String coursewareId, String operationType,
                                                                Date startTime, Date endTime){
        return teacherCoursewareContestService.loadByCoursewareId(coursewareId,operationType,startTime,endTime);
    }

    public MapMessage loadDailyPopularityRanking(String subjectName, String date){
        return teacherCoursewareContestService.loadDailyPopularityRanking(subjectName,date);
    }

    public MapMessage loadWeeklyPopularityRanking(String subjectName, Integer week){
        return teacherCoursewareContestService.loadWeeklyPopularityRanking(subjectName,week);
    }

    public MapMessage loadTotalPopularityRanking(String subjectName){
        return teacherCoursewareContestService.loadTotalPopularityRanking(subjectName);
    }

    public MapMessage loadDailyTalentRanking(String date){
        return teacherCoursewareContestService.loadDailyTalentRanking(date);
    }

    public MapMessage loadWeeklyTalentRanking(Integer week){
        return teacherCoursewareContestService.loadWeeklyTalentRanking(week);
    }

    public MapMessage loadTotalTalentRanking(){
        return teacherCoursewareContestService.loadTotalTalentRanking();
    }

    public MapMessage loadTop3Ranking(){
        return teacherCoursewareContestService.loadTop3Ranking();
    }

    public MapMessage loadPopularityShowInfo(){
        return teacherCoursewareContestService.loadPopularityShowInfo();
    }

    public MapMessage loadTalentShowInfo(){
        return teacherCoursewareContestService.loadTalentShowInfo();
    }

    public MapMessage loadExcellentShowInfo() {
        return teacherCoursewareContestService.loadExcellentShowInfo();
    }

    public List<Map<String, Object>> updateCourseInfo(List<Map<String, Object>> rankingData) {
        return teacherCoursewareContestService.updateCourseInfo(rankingData);
    }

    public Integer getWeekPopularityTopRank(String subject, String courseId) {
        if (StringUtils.isBlank(subject) || StringUtils.isBlank(courseId)) {
            return 0;
        }

        // 人气作品周榜前三
        Integer week = (int) DateUtils.dayDiff(new Date(), RANKING_START_DATE) / 7;
        MapMessage weekPopularityTop3Data = loadWeeklyPopularityRanking(subject, week);
        if (weekPopularityTop3Data.isSuccess()) {
            List<Map<String, Object>> weekPopularityTop3 = (List<Map<String, Object>>) weekPopularityTop3Data.get("data");
            for (int i = 0; i < weekPopularityTop3.size(); i++) {
                Map<String, Object> data = weekPopularityTop3.get(i);
                if (StringUtils.equals(courseId, SafeConverter.toString(data.get("coursewareId")))) {
                    return i + 1;
                }

                if (i >= 2) {
                    break;
                }
            }
        }

        return 0;
    }

    public Integer getMonthExcellentTopRank(String subject, String courseId) {
        if (StringUtils.isBlank(subject) || StringUtils.isBlank(courseId)) {
            return 0;
        }

        // 高分作品月榜前三
        Integer month = 0;
        Date month1Start = DateUtils.stringToDate("2018-11-19 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
        Date month2Start = DateUtils.stringToDate("2018-12-17 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
        Date curTime = new Date();
        if (curTime.getTime() >= month2Start.getTime()) {
            month = 2;
        } else if (curTime.getTime() >= month1Start.getTime()) {
            month = 1;
        }

        if (Objects.equals(month, 1) || Objects.equals(month, 2)) {
            List<Map<String, Object>> monthExcellentTopData = TeacherCourseCache.loadExcellentMonthlyRankData(subject, month);
            if (CollectionUtils.isNotEmpty(monthExcellentTopData)) {
                for (int i = 0; i < monthExcellentTopData.size(); i++) {
                    Map<String, Object> data = monthExcellentTopData.get(i);
                    if (StringUtils.equals(courseId, SafeConverter.toString(data.get("coursewareId")))) {
                        return i + 1;
                    }

                    if (i >= 2) {
                        break;
                    }
                }
            }
        }

        return 0;
    }

    public Integer loadCourseShareNum(String courseId) {
        return teacherCoursewareContestService.loadCourseShareNum(courseId);
    }

    public MapMessage loadCanvassData(String subject) {
        return teacherCoursewareContestService.loadCanvassData(subject);
    }

    public MapMessage loadUserCanvassInfo(String subject, TeacherDetail teacher, String openId) {
        return teacherCoursewareContestService.loadUserCanvassInfo(subject, teacher, openId);
    }

    public MapMessage canvassVote(TeacherDetail teacher, String courseId, Long createTeacherId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("canvassVote")
                    .keys(teacher.getId())
                    .callback(()-> teacherCoursewareContestService.canvassVote(teacher, courseId, createTeacherId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed canvassVote id:{}, teacher id:{}", courseId, teacher.getId(), ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage canvassVote(String openId, String courseId, Long createTeacherId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("canvassVote")
                    .keys(openId)
                    .callback(()-> teacherCoursewareContestService.canvassVote(openId, courseId, createTeacherId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage( "正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed canvassVote id:{}, open id:{}", courseId, openId, ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    public MapMessage canvassHelper(String courseId, Long teacherId) {
        return teacherCoursewareContestService.canvassHelper(courseId, teacherId);
    }

    public MapMessage canvassHelper(String courseId, String openId) {
        return teacherCoursewareContestService.canvassHelper(courseId, openId);
    }

    public Map<String,Integer> surplus(Long teacherId, String openId, String courseId) {
        return teacherCoursewareContestService.surplus(teacherId, openId, courseId);
    }

    public void clearCourseShareNum(String courseId) {
        TeacherCourseCache.clearCourseShareNumCache(courseId);
    }

}
