
package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.campaign.api.TeacherCoursewareContestService;
import com.voxlearning.utopia.service.campaign.api.buffer.TeacherCoursewareBuffer;
import com.voxlearning.utopia.service.campaign.api.constant.AwardParam;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareBookInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewarePageInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareStatistics;
import com.voxlearning.utopia.service.campaign.cache.TeacherCourseCache;
import com.voxlearning.utopia.service.campaign.helper.DynamicRankingHelper;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareDao;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareStatisticsDao;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareVersion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants.*;

@Named
@Slf4j
@ExposeService(interfaceClass = TeacherCoursewareContestService.class)
public class TeacherCoursewareContestServiceImpl implements TeacherCoursewareContestService, InitializingBean {

    @Inject
    private TeacherCoursewareDao teacherCoursewareDao;
    @Inject
    private TeacherResourceServiceImpl teacherResourceService;

    @AlpsQueueProducer(queue = "utopia.campaign.teacher.courseware.file.exchange")
    private MessageProducer coursewareProducer;

    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private TeacherCoursewareVersion teacherCoursewareVersion;

    @Inject
    private TeacherCoursewareStatisticsDao teacherCoursewareStatisticsDao;

    public static final Integer MAX_UPLOAD_PICTURE_COUNT = 5;

    private ManagedNearBuffer<List<TeacherCourseware>, TeacherCoursewareBuffer> teacherCoursewareBuffer;

    private static List<Subject> VALID_SUBJECTS = Arrays.asList(Subject.ENGLISH, Subject.MATH, Subject.CHINESE);

    @Override
    public void afterPropertiesSet() throws Exception {
        NearBufferBuilder<List<TeacherCourseware>, TeacherCoursewareBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("TeacherCoursewareBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(TeacherCoursewareBuffer.class);
        builder.reloadNearBuffer(10, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = teacherCoursewareVersion.current();
            List<TeacherCourseware> list = teacherCoursewareDao.loadByExamineStatus(TeacherCourseware.ExamineStatus.PASSED);
            return new VersionedBufferData<>(version, list);
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = teacherCoursewareVersion.current();
            if (oldVersion < currentVersion) {
                List<TeacherCourseware> list = teacherCoursewareDao.loadByExamineStatus(TeacherCourseware.ExamineStatus.PASSED);
                return new VersionedBufferData<>(currentVersion, list);
            }
            return null;
        });
        teacherCoursewareBuffer = builder.build();
    }

    @Override
    public MapMessage createSimpleCourseware(Long teacherId) {
        if (teacherId == null) {
            return MapMessage.errorMessage("老师Id为空");
        }

        List<TeacherCourseware> teacherCoursewareList = teacherCoursewareDao.loadByTeacherId(teacherId);
        // todo 10 放开
        if (CollectionUtils.isNotEmpty(teacherCoursewareList) && teacherCoursewareList.size() >= 100) {
            return MapMessage.errorMessage("数量已经超过了10个");
        }

        TeacherCourseware teacherCourseware = new TeacherCourseware();
        String id = teacherId + "-" + RandomUtils.nextObjectId();
        teacherCourseware.setId(id);
        teacherCourseware.setTeacherId(teacherId);

        // 补充老师姓名
        User user = teacherLoaderClient.loadTeacher(teacherId);
        if (user != null && !user.isDisabledTrue()) {
            teacherCourseware.setTeacherName(user.fetchRealname());
        }

        teacherCourseware.setDisabled(false);
        teacherCourseware.setStatus(TeacherCourseware.Status.DRAFT);
        Date now = new Date();
        teacherCourseware.setCreateTime(now);
        teacherCourseware.setUpdateTime(now);
        teacherCourseware.setCommentNum(0);
        teacherCourseware.setTotalScore(0);
        teacherCourseware.setVisitNum(0);
        teacherCourseware.setCanvassNum(0);

        teacherCoursewareDao.insert(teacherCourseware);
        teacherCoursewareDao.incrementBufferVersion();

        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage upsertCourseware(TeacherCourseware courseware) {
        teacherCoursewareDao.upsert(courseware);
        teacherCoursewareDao.incrementBufferVersion();
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateCoursewareBookInfo(String id, TeacherCoursewareBookInfo content) {
        if (StringUtils.isBlank(id) || content == null) {
            return MapMessage.errorMessage("参数错误");
        }

        teacherCoursewareDao.updateBookInfo(id, content);

        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage updateCoursewareFileInfo(String id, String fileUrl, String fileName) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
//        boolean res = teacherCoursewareDao.updateFileInfo(id, fileUrl, fileName);
        if (fileName.contains("ppt")) {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("CID", id);
            coursewareProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        }
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage updateCoursewareContent(String id, String title, String description) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.updateContent(id, title, description);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateCourseToExamining(String id) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.updateStatusToExaming(id);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteCoureware(String id) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.delete(id);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateCourseToExamine(String id, String updater) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        long count = teacherCoursewareDao.updateExamineStatus(id, TeacherCourseware.ExamineStatus.WAITING, TeacherCourseware.ExamineStatus.EXAMINING, updater, null);
        if (count <= 0) {
            return MapMessage.errorMessage("该课件已经在审核中或者不存在，请检查");
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateCourseExamined(String id, String updater, boolean pass, String extInfo) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        long count = teacherCoursewareDao.updateExamineStatus(id, pass ? TeacherCourseware.ExamineStatus.PASSED : TeacherCourseware.ExamineStatus.FAILED,
                updater, extInfo);
        if (count <= 0) {
            return MapMessage.errorMessage("该课件已经在审核完或者不存在，请检查");
        }
        return MapMessage.successMessage();
    }


    @Override
    public List<TeacherCourseware> fetchCourseWareListByTeacher(Long teacherId) {
        return teacherCoursewareDao.loadByTeacherId(teacherId);
    }

    @Override
    public List<TeacherCourseware> fetchCourseWareListByExamStatus(String examStatus) {
        try {
            TeacherCourseware.ExamineStatus status = TeacherCourseware.ExamineStatus.valueOf(examStatus);
            return teacherCoursewareDao.loadByExamineStatus(status);
        } catch (Exception e) {
            log.error("fetchCourseWareListByExamStatus error", e);
            return Collections.emptyList();
        }
    }

    @Override
    public TeacherCourseware fetchCourseWareDetailById(String id) {
        return teacherCoursewareDao.load(id);
    }

    @Override
    public List<TeacherCourseware> loadExaminingCoursewareList() {
        return teacherCoursewareDao.loadByExamineStatus(TeacherCourseware.ExamineStatus.EXAMINING);
    }

    @Override
    public long countByTeacherId(TeacherCoursewareParam param){
        return teacherCoursewareDao.countByTeacherId(param);
    }

    @Override
    public Long count(TeacherCoursewareParam param){
        return teacherCoursewareDao.count(param);
    }

    @Override
    public List<TeacherCourseware> fetchTeacherCoursewarByPage(TeacherCoursewarePageInfo teacherCoursewarePageInfo){
        return teacherCoursewareDao.fetchCoursewareByPage(teacherCoursewarePageInfo);
    }

    @Override
    public MapMessage updateCoursewareWordFileInfo(String id, String fileUrl, String fileName){
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        boolean res = teacherCoursewareDao.updateWordFileInfo(id, fileUrl, fileName);
        // todo word 消息重写
//        if (res) {
//            Map<String, Object> message = new LinkedHashMap<>();
//            message.put("CID", id);
//            coursewareProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
//        }
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage updateCoursewarePictureFileInfo(String id, String fileUrl, String fileName){
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.cleanPictureCache(id);
        TeacherCourseware teacherCourseware = teacherCoursewareDao.load(id);
        List<Map<String,String>> pictureList = teacherCourseware.getPicturePreview();
        if (pictureList == null){
            pictureList = new ArrayList<>();
        }
        if (pictureList.size() >= MAX_UPLOAD_PICTURE_COUNT){
            return MapMessage.errorMessage().setInfo("图片数量已超过最大限制");
        }
        Map<String,String> pictureMap = new HashMap<>();
        pictureMap.put("url",fileUrl);
        pictureMap.put("name",fileName);
        pictureList.add(pictureMap);
        boolean res = teacherCoursewareDao.updatePictureFileInfo(id,pictureList);
        return MapMessage.successMessage().set("id", id).set("pictureList",pictureList);
    }

    @Override
    public MapMessage updateCover(String id, String fileUrl, String fileName,Boolean isUserUpload){
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        boolean res = teacherCoursewareDao.updateCover(id, fileUrl, fileName,isUserUpload);
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage deletePicture(String id,List<Map<String,String>> pictureList){
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        boolean res = teacherCoursewareDao.deletePicture(id,pictureList);
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage updateCompressedFiles(String id, String fileUrl, String fileName){
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        boolean res = teacherCoursewareDao.updateFileInfo(id, fileUrl, fileName);
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage updateStatus(String id, String status){
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.updateStatus(id,status);
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage updateFileUrl(String id, String fileUrl, String name){
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        String fileUrlLowerCase = fileUrl.toLowerCase();
        if (fileUrlLowerCase.contains("ppt") || fileUrlLowerCase.contains("zip") || fileUrlLowerCase.contains("rar")) {
            teacherCoursewareDao.updateFileUrl(id, fileUrl, name);
        } else if (fileUrl.contains("doc")){
            teacherCoursewareDao.updateWordFileUrl(id,fileUrl,name);
        }
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public List<TeacherCourseware> fetchNewestInfo(int limitNum){
        return teacherCoursewareDao.fetchNewestCourseware(limitNum);
    }

    @Override
    public List<TeacherCourseware> fetchAllTeacherCoursewarByPage(TeacherCoursewarePageInfo teacherCoursewarePageInfo){
        return teacherCoursewareDao.fetchAllCoursewareByPage(teacherCoursewarePageInfo);
    }

    @Override
    public MapMessage updateNewWordUrl(String id, String fileUrl ){
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.updateNewWordFileUrl(id,fileUrl);
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage cleanPptCoursewareInfo(String id) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.cleanPptCoursewareInfo(id);
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public VersionedBufferData<List<TeacherCourseware>> loadTeacherCoursewareBufferData(Long version) {
        TeacherCoursewareBuffer nativeBuffer = teacherCoursewareBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    @Override
    public void resetTeacherCoursewareBuffer() {
        teacherCoursewareBuffer.reset();
    }

    @Override
    public MapMessage updateVisitNum(String courseId, Integer visitNum){
        if (StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.updateVisitNum(courseId,visitNum);
        return MapMessage.successMessage().set("id", courseId);
    }

    @Override
    public MapMessage updateDownloadNum(String courseId, Integer visitNum){
        if (StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.updateDownloadNum(courseId,visitNum);
        return MapMessage.successMessage().set("id", courseId);
    }

    @Override
    public MapMessage updateCommentNum(String courseId, Integer commentNum){
        if (StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.updateCommentNum(courseId,commentNum);
        return MapMessage.successMessage().set("id", courseId);
    }


    @Override
    public MapMessage updatePptCoursewareFile(String id, String fileUrl, String fileName) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.updatePptCoursewareFile(id, fileUrl, fileName);
        return MapMessage.successMessage().set("id", id);
    }

    @Override
    public MapMessage updateZipFile(String id, String fileUrl) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.updateZipFile(id, fileUrl);
        teacherResourceService.updateCoursewareUrl(id, fileUrl);
        return MapMessage.successMessage().set("id", id);
    }

    /**
     * 后门,统一更新资源的名称
     *
     * @return
     */
    @Override
    public MapMessage updateResourceName(String id, String pptName, String docName, String unPackagePptName) {
        try {
            teacherCoursewareDao.updateResourceName(id, pptName, docName, unPackagePptName);
            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage();
    }

    /**
     * 如果有解压出来的 ppt 说明要修改的字段是 pptCoursewareFileName
     */
    @Override
    public MapMessage updateResourceName(String id, String pptName, String docName) {
        TeacherCourseware load = teacherCoursewareDao.load(id);
        if (StringUtils.isNotEmpty(load.getPptCoursewareFileName())) {
            return updateResourceName(id, "", docName, pptName);
        } else {
            return updateResourceName(id, pptName, docName, "");
        }
    }

    @Override
    public MapMessage updateCoursewareAwardInfo(AwardParam param){
        if (StringUtils.isBlank(param.getId())) {
            return MapMessage.errorMessage("参数错误");
        }
        teacherCoursewareDao.cleanPictureCache(param.getId());
        TeacherCourseware teacherCourseware = teacherCoursewareDao.load(param.getId());
        List<Map<String,String>> pictureList = teacherCourseware.getAwardPicturePreview();
        if (pictureList == null){
            pictureList = new ArrayList<>();
        }
        pictureList.clear();
        Map<String,String> pictureMap = new HashMap<>();
        pictureMap.put("url",param.getFileUrl());
        pictureMap.put("name",param.getFileName());
        pictureList.add(pictureMap);
        boolean res = teacherCoursewareDao.updateAwardInfo(param.getId(),pictureList,param.getAwardLevelName(),param.getAwardLevelId(),param.getAwardIntroduction());
        return MapMessage.successMessage().set("id", param.getId()).set("pictureList",pictureMap);
    }


    /**
     * 查询最多下载次数
     */
    @Override
    public Long fetchMaxDownloadCourse() {
        List<TeacherCourseware> maxDownloadCourse = teacherCoursewareDao.fetchMaxDownloadCourse();
        if (CollectionUtils.isNotEmpty(maxDownloadCourse)){
            Integer maxDownloadNum = maxDownloadCourse.get(0).getDownloadNum();
            return maxDownloadNum == null ? 1L : Long.valueOf(maxDownloadNum);
        } else {
            return 1L;
        }
    }

    @Override
    public List<Map<String, Object>> fetchStatisticsInfoByCourseId(Date startTime, Date endTime){
        return teacherCoursewareStatisticsDao.loadStatisticsInfoByCourseId(startTime,endTime);
    }

    @Override
    public List<Map<String, Object>> fetchStatisticsInfoByTeacherId(Date startTime,Date endTime){
        return teacherCoursewareStatisticsDao.loadStatisticsInfoByTeacherId(startTime,endTime);
    }

    @Override
    public List<Map<String, Object>> fetchDownloadStatInfo(Date startTime, Date endTime) {
        return teacherCoursewareStatisticsDao.loadDownloadStatInfo(startTime,endTime);
    }

    @Override
    public List<TeacherCoursewareStatistics> loadByCoursewareId(String coursewareId, String operationType,
                                                                Date startTime, Date endTime){
        return teacherCoursewareStatisticsDao.loadByCoursewareId(coursewareId,operationType,startTime,endTime);
    }

    @Override
    public List<TeacherCoursewareStatistics> loadTeacherCanvassRecords(Long tid) {
        return teacherCoursewareStatisticsDao.loadTeacherOpinfo(OP_CANVASS, tid);
    }

    @Override
    public MapMessage canvassVote(TeacherDetail teacher, String courseId, Long createTeacherId) {
        if (teacher == null || StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误!");
        }

        DayRange todayRange = DayRange.current();
        List<TeacherCoursewareStatistics> canvassRecords = teacherCoursewareStatisticsDao.loadTeacherOpinfo(OP_CANVASS, teacher.getId())
                .stream()
                .filter(p -> todayRange.contains(p.getCreateTime()))
                .collect(Collectors.toList());

        // 认证老师每天10票,每作品2票，非认证老师每天5票，每作品1票
        int totalCanvassLimit = 5;
        int courseCanvassLimit = 1;
        boolean authed = teacher.fetchCertificationState() == AuthenticationState.SUCCESS;
        if (authed) {
            totalCanvassLimit = 10;
            courseCanvassLimit = 2;
        }

        if (canvassRecords.size() >= totalCanvassLimit) {
            if (authed) {
                return MapMessage.errorMessage("今日投票次数已经消耗完，分享出去为当前作品拉票。");
            } else {
                return MapMessage.errorMessage("未认证用户每天可投票5次，认证用户每天可投票10次，去认证吧~~~");
            }
        }

        List<TeacherCoursewareStatistics> courseCanvassRecords = canvassRecords.stream()
                .filter(p -> StringUtils.equals(p.getCourseware_id(), courseId))
                .collect(Collectors.toList());

        if (courseCanvassRecords.size() >= courseCanvassLimit) {
            if (authed) {
                return MapMessage.errorMessage("每个作品每天只有2票哦，去看看其他作品吧~~~");
            } else {
                return MapMessage.errorMessage("未认证用户每天每个作品每天只有1票哦，去认证或者看看其他作品吧~~~");
            }
        }

        // 投票记录
        TeacherCoursewareStatistics teacherCoursewareStatistics = new TeacherCoursewareStatistics();
        teacherCoursewareStatistics.setCreate_teacher_id(createTeacherId);
        teacherCoursewareStatistics.setCourseware_id(courseId);
        teacherCoursewareStatistics.setOperate_teacher_id(teacher.getId());
        teacherCoursewareStatistics.setType(OP_CANVASS);
        teacherCoursewareStatistics.setAuthentication(authed ? "Y" : "N");
        teacherCoursewareStatisticsDao.insert(teacherCoursewareStatistics);

        // 投票数+1
        teacherCoursewareDao.incCanvassNum(courseId);
        TeacherCourseware load = teacherCoursewareDao.load(courseId);

        return MapMessage.successMessage()
                .add("canvassNum", load.getCanvassNum())
                .add("totalSurplus", Math.max(totalCanvassLimit - (canvassRecords.size() + 1), 0))
                .add("surplus", Math.max(courseCanvassLimit - (courseCanvassRecords.size() + 1), 0));
    }

    @Override
    public MapMessage canvassVote(String openId, String courseId, Long createTeacherId) {
        if (StringUtils.isBlank(openId) || StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误!");
        }

        DayRange todayRange = DayRange.current();
        List<TeacherCoursewareStatistics> canvassRecords = teacherCoursewareStatisticsDao.loadUserOpinfo(OP_CANVASS, openId)
                .stream()
                .filter(p -> todayRange.contains(p.getCreateTime()))
                .collect(Collectors.toList());

        // 认证老师每天10票,每作品2票，非认证老师每天5票，每作品1票
        int totalCanvassLimit = 5;
        int courseCanvassLimit = 1;

        if (canvassRecords.size() >= totalCanvassLimit) {
            return MapMessage.errorMessage("未认证用户每天可投票5次，认证用户每天可投票10次，去认证吧~~~");
        }

        List<TeacherCoursewareStatistics> courseCanvassRecords = canvassRecords.stream()
                .filter(p -> StringUtils.equals(p.getCourseware_id(), courseId))
                .collect(Collectors.toList());

        if (courseCanvassRecords.size() >= courseCanvassLimit) {
            return MapMessage.errorMessage("未认证用户每天每个作品每天只有1票哦，去认证或者看看其他作品吧~~~");
        }

        // 投票记录
        TeacherCoursewareStatistics teacherCoursewareStatistics = new TeacherCoursewareStatistics();
        teacherCoursewareStatistics.setCreate_teacher_id(createTeacherId);
        teacherCoursewareStatistics.setCourseware_id(courseId);
        teacherCoursewareStatistics.setOperate_teacher_id(0L);
        teacherCoursewareStatistics.setType(OP_CANVASS);
        teacherCoursewareStatistics.setAuthentication("N");
        teacherCoursewareStatistics.setWechatOpenId(openId);
        teacherCoursewareStatisticsDao.insert(teacherCoursewareStatistics);

        // 投票数+1
        teacherCoursewareDao.incCanvassNum(courseId);
        TeacherCourseware load = teacherCoursewareDao.load(courseId);

        return MapMessage.successMessage()
                .add("canvassNum", load.getCanvassNum())
                .add("totalSurplus", Math.max(totalCanvassLimit - (canvassRecords.size() + 1), 0))
                .add("surplus", Math.max(courseCanvassLimit - (courseCanvassRecords.size() + 1), 0));
    }

    @Override
    public MapMessage canvassHelper(String courseId, Long teacherId) {
        teacherCoursewareDao.incCanvassHelper(courseId);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage canvassHelper(String courseId, String openId) {
        teacherCoursewareDao.incCanvassHelper(courseId);
        return MapMessage.successMessage();
    }

    public Map<String,Integer> surplus(Long teacherId, String openId, String courseId) {
        if ((teacherId == null && StringUtils.isEmpty(openId))) {
            return Collections.emptyMap();
        }

        TeacherDetail teacherDetail = null;
        List<TeacherCoursewareStatistics> todayRecords;

        if (teacherId != null) {
            teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            todayRecords = teacherCoursewareStatisticsDao.loadTeacherOpinfo(OP_CANVASS, teacherDetail.getId());
        } else {
            todayRecords = teacherCoursewareStatisticsDao.loadUserOpinfo(OP_CANVASS, openId);
        }

        DayRange todayRange = DayRange.current();

        todayRecords = todayRecords
                .stream()
                .filter(p -> todayRange.contains(p.getCreateTime()))
                .collect(Collectors.toList());

        // 认证老师每天10票,每作品2票，非认证老师每天5票，每作品1票
        int totalCanvassLimit = 5;
        int courseCanvassLimit = 1;
        boolean authed = teacherDetail != null && teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS;
        if (authed) {
            totalCanvassLimit = 10;
            courseCanvassLimit = 2;
        }

        List<TeacherCoursewareStatistics> todayItemRecords = todayRecords.stream()
                .filter(p -> StringUtils.equals(p.getCourseware_id(), courseId))
                .collect(Collectors.toList());

        int surplus = courseCanvassLimit - todayItemRecords.size();
        int totalSurplus = totalCanvassLimit - todayRecords.size();
        return MapUtils.map("surplus", surplus, "totalSurplus", totalSurplus);
    }

    @Override
    public MapMessage loadDailyPopularityRanking(String subjectName, String date) {
        Subject subject = Subject.ofWithUnknown(subjectName);
        if (!VALID_SUBJECTS.contains(subject)) {
            return MapMessage.errorMessage("无效的学科参数:" + subjectName);
        }

        Date rankDay = DateUtils.stringToDate(date, DateUtils.FORMAT_SQL_DATE);
        Date todayStart = DateUtils.getTodayStart();
        if (rankDay == null || rankDay.before(RANKING_START_DATE) || rankDay.getTime() >= todayStart.getTime()) {
            return MapMessage.errorMessage("无效的日期:" + date);
        }

        // 先检查缓存里面有没有，如果有直接返回
        String cacheKey = "TS_PopularityRanking_D1_" + subjectName + "_" + date;
        List<Map<String, Object>> rankingData = TeacherCourseCache.getCache().load(cacheKey);
        if (rankingData != null) {
            return MapMessage.successMessage().add("data", rankingData);
        }

        // 重新计算
        DayRange curTimeRange = DayRange.newInstance(rankDay.getTime());
        DayRange prevTimeRange = curTimeRange.previous();

        List<Map<String, Object>> curData = teacherCoursewareStatisticsDao.loadStatisticsInfoByCourseId(curTimeRange.getStartDate(), curTimeRange.getEndDate());
        List<Map<String, Object>> prevData = new ArrayList<>();
        if (prevTimeRange.getStartDate().getTime() >= RANKING_START_DATE.getTime()) {
            prevData = teacherCoursewareStatisticsDao.loadStatisticsInfoByCourseId(prevTimeRange.getStartDate(), prevTimeRange.getEndDate());
        }

        // 计算返回值
        List<Map<String, Object>> retValue = new ArrayList<>();
        assemblyPopularResult(curData, subject, prevData, retValue);
        TeacherCourseCache.getCache().set(cacheKey, DateUtils.getCurrentToMonthEndSecond(), retValue);

        return MapMessage.successMessage().add("data", retValue);
    }

    @Override
    public MapMessage loadWeeklyPopularityRanking(String subjectName, Integer week) {
        Subject subject = Subject.ofWithUnknown(subjectName);
        if (!VALID_SUBJECTS.contains(subject)) {
            return MapMessage.errorMessage("无效的学科参数:" + subjectName);
        }

        // 期数判断
        Date curTime = new Date();
        int maxWeek = (int) DateUtils.dayDiff(curTime, RANKING_START_DATE) / 7;
        if (week < 1 || week > maxWeek) {
            return MapMessage.errorMessage("无效的期数:" + week);
        }

        if (week > 7) {
            week = 7;
        }

        // 先检查缓存里面有没有，如果有直接返回
        String cacheKey = "TS_PopularityRanking_W1_" + subjectName + "_" + week;
        List<Map<String, Object>> rankingData = TeacherCourseCache.getCache().load(cacheKey);
        if (rankingData != null) {
            return MapMessage.successMessage().add("data", rankingData);
        }

        // 重新计算
        WeekRange curTimeRange = WeekRange.newInstance(DateUtils.addWeeks(RANKING_START_DATE, week - 1).getTime());
        WeekRange prevTimeRange = curTimeRange.previous();

        List<Map<String, Object>> curData = teacherCoursewareStatisticsDao.loadStatisticsInfoByCourseId(curTimeRange.getStartDate(), curTimeRange.getEndDate());
        List<Map<String, Object>> prevData = new ArrayList<>();
        if (prevTimeRange.getStartDate().getTime() >= RANKING_START_DATE.getTime()) {
            prevData = teacherCoursewareStatisticsDao.loadStatisticsInfoByCourseId(prevTimeRange.getStartDate(), prevTimeRange.getEndDate());
        }

        // 计算返回值，同日榜
        List<Map<String, Object>> retValue = new ArrayList<>();
        assemblyPopularResult(curData, subject, prevData, retValue);
        TeacherCourseCache.getCache().set(cacheKey, getCacheEnd(), retValue);

        return MapMessage.successMessage().add("data", retValue);
    }

    @Override
    public MapMessage loadTotalPopularityRanking(String subjectName) {
        Subject subject = Subject.ofWithUnknown(subjectName);
        if (!VALID_SUBJECTS.contains(subject)) {
            return MapMessage.errorMessage("无效的学科参数:" + subjectName);
        }

        Date yesterday = DateUtils.getDayEnd(DateUtils.calculateDateDay(new Date(),-1));
        Date dayBeforeYesterday = DateUtils.getDayEnd(DateUtils.calculateDateDay(new Date(),-2));

        //String date = DateUtils.dateToString(yesterday, DateUtils.FORMAT_SQL_DATE);
        // 先检查缓存里面有没有，如果有直接返回
        String cacheKey = "TS_PopularityRanking_T1_" + subjectName + "_" + CACHE_END_DATE;
        List<Map<String, Object>> rankingData = TeacherCourseCache.getCache().load(cacheKey);
        if (rankingData != null) {
            return MapMessage.successMessage().add("data", rankingData);
        }

        List<Map<String, Object>> curData = teacherCoursewareStatisticsDao.loadStatisticsInfoByCourseId(RANKING_START_DATE, yesterday);
        List<Map<String, Object>> prevData = teacherCoursewareStatisticsDao.loadStatisticsInfoByCourseId(RANKING_START_DATE, dayBeforeYesterday);

        // 计算返回值
        List<Map<String, Object>> retValue = new ArrayList<>();
        assemblyPopularResult(curData, subject, prevData, retValue);
        TeacherCourseCache.getCache().set(cacheKey, getCacheEnd(), retValue);
        return MapMessage.successMessage().add("data", retValue);
    }

    /**
     * 人气榜组装最终结果
     * @param curData
     * @param subject
     * @param prevData
     * @param retValue
     */
    public void assemblyPopularResult(List<Map<String, Object>> curData, Subject subject, List<Map<String, Object>> prevData, List<Map<String, Object>> retValue){

        for (Map<String, Object> item : curData) {
            String courseId = SafeConverter.toString(item.get("coursewareId"));

            TeacherCourseware courseware = teacherCoursewareDao.load(courseId);
            // 同学科
            if (subject != courseware.getSubject()) {
                continue;
            }

            // 状态
            if (courseware.getExamineStatus() != TeacherCourseware.ExamineStatus.PASSED || SafeConverter.toBoolean(courseware.getDisabled())) {
                continue;
            }

            Map<String, Object> newItem = new HashMap<>();
            newItem.putAll(item);

            newItem.put("awardLevelName",courseware.getAwardLevelName());
            newItem.put("teacherId",courseware.getTeacherId());
            newItem.put("awardLevelId",courseware.getAwardLevelId());
            newItem.put("title",courseware.getTitle());
            newItem.put("totalScore",courseware.getTotalScore());
            newItem.put("coverUrl",courseware.getCoverUrl());
            newItem.put("createDate",DateUtils.dateToString(courseware.getUpdateTime(), DateUtils.FORMAT_SQL_DATE));
            newItem.put("downloadNum",courseware.getDownloadNum());
            newItem.put("commentNum",courseware.getCommentNum());
            newItem.put("visitNum",courseware.getVisitNum());

            // 提交老师信息
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(courseware.getTeacherId());
            newItem.put("teacherName", teacher.fetchRealname());
            newItem.put("schoolName", teacher.getTeacherSchoolName());

            retValue.add(newItem);

            if (retValue.size() >= 200) {
                break;
            }
        }

        // 计算动态排名变化
        DynamicRankingHelper.calcDynamicRank(retValue, prevData, "coursewareId");
    }

    /**
     * 组装达人榜数据
     * @param curData 最新数据
     * @param prevData 最新数据上一次数据
     * @param retValue 返回结果
     */
    public void assemblyTalentResult(List<Map<String, Object>> curData, List<Map<String, Object>> prevData, List<Map<String, Object>> retValue){

        for (Map<String, Object> item : curData) {
            Long teacherId = SafeConverter.toLong(item.get("teacherId"));

            Map<String, Object> newItem = new HashMap<>();
            newItem.putAll(item);

            // 提交老师信息
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
            newItem.put("teacherName", teacher.fetchRealname());
            newItem.put("teacherSubject", teacher.getSubject() == null ? "" : teacher.getSubject().getValue());
            newItem.put("schoolName", teacher.getTeacherSchoolName());
            // avatar
            String avatar = teacher.fetchImageUrl();
            newItem.put("avatar", avatar);

            retValue.add(newItem);

            if (retValue.size() >= 200) {
                break;
            }
        }

        // 计算动态排名变化
        DynamicRankingHelper.calcDynamicRank(retValue, prevData, "totalNum");
    }

    @Override
    public MapMessage loadDailyTalentRanking(String date){

        Date rankDay = DateUtils.stringToDate(date, DateUtils.FORMAT_SQL_DATE);
        Date todayStart = DateUtils.getTodayStart();
        if (rankDay == null || rankDay.before(RANKING_START_DATE) || !rankDay.before(todayStart)) {
            return MapMessage.errorMessage("无效的日期:" + date);
        }

        // 先检查缓存里面有没有，如果有直接返回
        String cacheKey = "TS_TalentRanking_D1_" + date;
        List<Map<String, Object>> rankingData = TeacherCourseCache.getCache().load(cacheKey);
        if (rankingData != null) {
            return MapMessage.successMessage().add("data", rankingData);
        }

        // 重新计算
        DayRange curTimeRange = DayRange.newInstance(rankDay.getTime());
        DayRange prevTimeRange = curTimeRange.previous();

        List<Map<String, Object>> curData = teacherCoursewareStatisticsDao.loadStatisticsInfoByTeacherId(curTimeRange.getStartDate(), curTimeRange.getEndDate());
        List<Map<String, Object>> prevData = new ArrayList<>();
        if (prevTimeRange.getStartDate().getTime() >= RANKING_START_DATE.getTime()) {
            prevData = teacherCoursewareStatisticsDao.loadStatisticsInfoByTeacherId(prevTimeRange.getStartDate(), prevTimeRange.getEndDate());
        }

        // 计算返回值
        List<Map<String, Object>> retValue = new ArrayList<>();
        assemblyTalentResult(curData, prevData, retValue);
        TeacherCourseCache.getCache().set(cacheKey, DateUtils.getCurrentToMonthEndSecond(), retValue);

        return MapMessage.successMessage().add("data", retValue);
    }

    @Override
    public MapMessage loadWeeklyTalentRanking(Integer week){
        // 期数判断
        Date curTime = new Date();
        Integer maxWeek = (int) DateUtils.dayDiff(curTime, RANKING_START_DATE) / 7;
        if (week < 1 || week > maxWeek) {
            return MapMessage.errorMessage("无效的期数:" + week);
        }

        if (week > 7) {
            week = 7;
        }

        // 先检查缓存里面有没有，如果有直接返回
        String cacheKey = "TS_TalentRanking_W1_" + week;
        List<Map<String, Object>> rankingData = TeacherCourseCache.getCache().load(cacheKey);
        if (rankingData != null) {
            return MapMessage.successMessage().add("data", rankingData);
        }

        // 重新计算
        WeekRange curTimeRange = WeekRange.newInstance(DateUtils.addWeeks(RANKING_START_DATE, week - 1).getTime());
        WeekRange prevTimeRange = curTimeRange.previous();

        List<Map<String, Object>> curData = teacherCoursewareStatisticsDao.loadStatisticsInfoByTeacherId(curTimeRange.getStartDate(), curTimeRange.getEndDate());
        List<Map<String, Object>> prevData = new ArrayList<>();
        if (prevTimeRange.getStartDate().getTime() >= RANKING_START_DATE.getTime()) {
            prevData = teacherCoursewareStatisticsDao.loadStatisticsInfoByTeacherId(prevTimeRange.getStartDate(), prevTimeRange.getEndDate());
        }

        // 计算返回值，同日榜
        List<Map<String, Object>> retValue = new ArrayList<>();
        assemblyTalentResult(curData, prevData, retValue);
        TeacherCourseCache.getCache().set(cacheKey, getCacheEnd(), retValue);

        return MapMessage.successMessage().add("data", retValue);
    }

    @Override
    public MapMessage loadTotalTalentRanking(){

        Date yesterday = DateUtils.getDayEnd(DateUtils.calculateDateDay(new Date(),-1));
        Date dayBeforeYesterday = DateUtils.getDayEnd(DateUtils.calculateDateDay(new Date(),-2));

        //String date = DateUtils.dateToString(yesterday, DateUtils.FORMAT_SQL_DATE);
        // 先检查缓存里面有没有，如果有直接返回
        String cacheKey = "TS_TalentRanking_T1_" + CACHE_END_DATE;
        List<Map<String, Object>> rankingData = TeacherCourseCache.getCache().load(cacheKey);
        if (rankingData != null) {
            return MapMessage.successMessage().add("data", rankingData);
        }

        List<Map<String, Object>> curData = teacherCoursewareStatisticsDao.loadStatisticsInfoByTeacherId(RANKING_START_DATE, yesterday);
        List<Map<String, Object>> prevData = teacherCoursewareStatisticsDao.loadStatisticsInfoByTeacherId(RANKING_START_DATE, dayBeforeYesterday);

        // 计算返回值
        List<Map<String, Object>> retValue = new ArrayList<>();
        assemblyTalentResult(curData, prevData, retValue);
        TeacherCourseCache.getCache().set(cacheKey, getCacheEnd(), retValue);
        return MapMessage.successMessage().add("data", retValue);
    }

    @Override
    public MapMessage loadTop3Ranking(){
        String yesterday = DateUtils.dateToString(DateUtils.addDays(new Date(), -1), DateUtils.FORMAT_SQL_DATE);

        // 人气榜取总榜前三
        List<Map<String, Object>> popularTop3 = new ArrayList<>();

        // 获取三科榜单
        for (Subject subject : VALID_SUBJECTS) {
            MapMessage popularData = loadTotalPopularityRanking(subject.name());
            if (!popularData.isSuccess()) {
                log.warn("loadDailyPopularityRanking failed, subject:{}, date:{}", subject, yesterday);
                continue;
            }

            List<Map<String, Object>> popularList = (List<Map<String, Object>>) popularData.get("data");
            if (CollectionUtils.isNotEmpty(popularList) && popularList.size() > 6) {
                popularTop3.addAll(popularList.subList(0, 6));
            } else if (popularList != null) {
                popularTop3.addAll(popularList);
            }
        }

        // 再次排序, 取前三
        Collections.sort(popularTop3, (o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("num")), SafeConverter.toInt(o1.get("num"))));
        if (popularTop3.size() > 6) {
            popularTop3 = new ArrayList<>(popularTop3.subList(0, 6));
        }

        // 达人榜取总榜前五
        List<Map<String, Object>> talentTop3 = new ArrayList<>();

        MapMessage talentData = loadTotalTalentRanking();
        if (!talentData.isSuccess()) {
            log.warn("loadDailyTalentRanking failed, date:{}", yesterday);
        } else {
            List<Map<String, Object>> talentList = (List<Map<String, Object>>) talentData.get("data");
            if (CollectionUtils.isNotEmpty(talentList) && talentList.size() > 5) {
                talentTop3.addAll(talentList.subList(0, 5));
            } else if (talentList != null){
                talentTop3.addAll(talentList);
            }
        }

        // 优秀作品榜，取总榜前三
        List<Map<String, Object>> excellentTop3 = new ArrayList<>();
        //String today = DateUtils.dateToString(DateUtils.addDays(new Date(), -1), DateUtils.FORMAT_SQL_DATE);

        // 获取三科榜单
        for (Subject subject : VALID_SUBJECTS) {
            List<Map<String, Object>> excellentData = TeacherCourseCache.loadExcellentTotalRankData(subject.name(), null);

            if (CollectionUtils.isNotEmpty(excellentData) && excellentData.size() > 6) {
                excellentTop3.addAll(excellentData.subList(0, 6));
            } else if (excellentData != null) {
                excellentTop3.addAll(excellentData);
            }
        }

        // 再次排序, 取前三
        Collections.sort(excellentTop3, (o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))));
        if (excellentTop3.size() > 6) {
            excellentTop3 = new ArrayList<>(excellentTop3.subList(0, 6));
        }

        return MapMessage.successMessage()
                .add("popularTop3", updateCourseInfo(popularTop3))
                .add("talentTop3", talentTop3)
                .add("excellentTop3", updateCourseInfo(excellentTop3));
    }

    @Override
    public List<Map<String, Object>> updateCourseInfo(List<Map<String, Object>> rankingData) {
        List<TeacherCourseware> coursewareList = teacherCoursewareBuffer.getNativeBuffer().dump().getData();

        Map<String, TeacherCourseware> coursewareMap = coursewareList.stream()
                .collect(Collectors.toMap(TeacherCourseware::getId, Function.identity()));

        for (Map<String, Object> newItem : rankingData) {
            TeacherCourseware courseware = coursewareMap.get(SafeConverter.toString(newItem.get("coursewareId")));
            if (courseware == null) {
                continue;
            }

            newItem.put("totalScore",courseware.getTotalScore());
            newItem.put("downloadNum",courseware.getDownloadNum());
            newItem.put("commentNum",courseware.getCommentNum());
            newItem.put("visitNum",courseware.getVisitNum());
            newItem.put("awardLevelName",courseware.getAwardLevelName());
            newItem.put("awardLevelId",courseware.getAwardLevelId());
        }

        return rankingData;
    }

    @Override
    public MapMessage loadPopularityShowInfo() {
        MapMessage retObj = MapMessage.successMessage();
        // 期数和日期
        Date curTime = new Date();
        Integer week = (int) DateUtils.dayDiff(curTime, RANKING_START_DATE) / 7;

        if (week > 7) {
            week = 7;
        }

        // 查缓存
        String cacheKey = "TS_PopularityShowInfo1_" + week;
        Map<String, Object> cachedData = TeacherCourseCache.getCache().load(cacheKey);
        if (cachedData != null) {
            return retObj.add("data", cachedData);
        }

        Map<String, Object> retData = new HashMap<>();

        WeekRange weekRange = WeekRange.newInstance(DateUtils.addWeeks(RANKING_START_DATE, week - 1).getTime());
        retData.put("week", week);
        retData.put("weekStart", weekRange.getStartDate());
        retData.put("weekEnd", weekRange.getEndDate());

        // 总打榜作品数与作品浏览总数
        int newCourses = 0;
        int totalViewCount = 0;
        List<TeacherCourseware> coursewareList = teacherCoursewareBuffer.getNativeBuffer().dump().getData();
        for (TeacherCourseware courseware : coursewareList) {
            if (courseware.getStatus() != TeacherCourseware.Status.PUBLISHED) {
                continue;
            }
//
//            // 本周打榜作品数
//            if (courseware.getExamineUpdateTime() != null && weekRange.contains(courseware.getExamineUpdateTime())) {
//                newCourses++;
//            }
            newCourses++;
            totalViewCount += SafeConverter.toInt(courseware.getVisitNum());
        }

        retData.put("newCourses", newCourses);
        retData.put("totalViewCount", totalViewCount);

        // 累积人气值
        int totalPopularity = 0;
        Date yesterday = DateUtils.getTodayStart();
        List<Map<String, Object>> curData = teacherCoursewareStatisticsDao.loadStatisticsInfoByCourseId(RANKING_START_DATE, yesterday);
        for (Map<String, Object> item : curData) {
            totalPopularity += SafeConverter.toInt(item.get("NUM"));
        }

        retData.put("totalPopularity", totalPopularity);

        // 各科人气周榜TOP3
        for (Subject subject : VALID_SUBJECTS) {
            MapMessage popularData = loadWeeklyPopularityRanking(subject.name(), week);
            if (!popularData.isSuccess()) {
                log.warn("loadWeeklyPopularityRanking failed, subject:{}, week:{}", subject, week);
                continue;
            }

            List<Map<String, Object>> top3 = new ArrayList<>();
            List<Map<String, Object>> popularList = (List<Map<String, Object>>) popularData.get("data");
            if (CollectionUtils.isNotEmpty(popularList) && popularList.size() > 3) {
                top3.addAll(popularList.subList(0, 3));
            } else if (popularList != null) {
                top3.addAll(popularList);
            }

            top3 = updateCourseInfo(top3);
            retData.put(subject.name().toLowerCase() + "Top3", top3);
        }

        TeacherCourseCache.getCache().set(cacheKey, 24*60*60*20, retData);

        return retObj.add("data", retData);
    }

    @Override
    public MapMessage loadTalentShowInfo() {
        MapMessage retObj = MapMessage.successMessage();
        // 期数和日期
        Date curTime = new Date();
        Integer week = (int) DateUtils.dayDiff(curTime, RANKING_START_DATE) / 7;

        if (week > 7) {
            week = 7;
        }

        // 查缓存
        String cacheKey = "TS_TalentShowInfo1_" + week;
        Map<String, Object> cachedData = TeacherCourseCache.getCache().load(cacheKey);
        if (cachedData != null) {
            return retObj.add("data", cachedData);
        }

        Map<String, Object> retData = new HashMap<>();

        WeekRange weekRange = WeekRange.newInstance(DateUtils.addWeeks(RANKING_START_DATE, week - 1).getTime());
        retData.put("week", week);
        retData.put("weekStart", weekRange.getStartDate());
        retData.put("weekEnd", weekRange.getEndDate());

        // 作品总点评量
        int totalCommentCount = 0;
        List<TeacherCourseware> coursewareList = teacherCoursewareBuffer.getNativeBuffer().dump().getData();
        for (TeacherCourseware courseware : coursewareList) {
            if (courseware.getStatus() != TeacherCourseware.Status.PUBLISHED) {
                continue;
            }

            totalCommentCount += SafeConverter.toInt(courseware.getCommentNum());
        }

        retData.put("totalCommentCount", totalCommentCount);

        // 作品总点评人数和总分享量
        Date yesterday = DateUtils.getTodayStart();
        retData.put("totalCommentUsers", teacherCoursewareStatisticsDao.loadTotalCommentStatInfo(RANKING_START_DATE, yesterday));
        retData.put("totalShareCount", teacherCoursewareStatisticsDao.loadTotalShareStatInfo(RANKING_START_DATE, yesterday));

        // 点评达人榜
        // 达人榜取总榜前五
        List<Map<String, Object>> talentTop3 = new ArrayList<>();
        MapMessage talentData = loadWeeklyTalentRanking(week);
        if (!talentData.isSuccess()) {
            log.warn("loadWeeklyTalentRanking failed, week:{}", week);
        } else {
            List<Map<String, Object>> talentList = (List<Map<String, Object>>) talentData.get("data");
            if (CollectionUtils.isNotEmpty(talentList) && talentList.size() > 5) {
                talentTop3.addAll(talentList.subList(0, 5));
            } else if (talentList != null){
                talentTop3.addAll(talentList);
            }
        }

        retData.put("talentTop3", talentTop3);

        TeacherCourseCache.getCache().set(cacheKey, 24*60*60*20, retData);

        return retObj.add("data", retData);
    }

    @Override
    public MapMessage loadExcellentShowInfo() {
        MapMessage retObj = MapMessage.successMessage();

        // 期数和日期
        String periodName = "上辑";
        String periodStart = "09/25";
        String periodEnd = "11/18";
        Integer month = 1;

        Date period2Start = DateUtils.stringToDate("2018-12-17 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
        Date curTime = new Date();
        if (curTime.getTime() >= period2Start.getTime()) {
            periodName = "下辑";
            periodStart = "11/19";
            periodEnd = "12/16";
            month = 2;
        }

        // 查缓存
        String cacheKey = "TS_ExcellentShowInfo1_" + month;
        Map<String, Object> cachedData = TeacherCourseCache.getCache().load(cacheKey);
        if (cachedData != null) {
            return retObj.add("data", cachedData);
        }

        Map<String, Object> retData = new HashMap<>();

        retData.put("month", month);
        retData.put("periodName", periodName);
        retData.put("periodStart", periodStart);
        retData.put("periodEnd", periodEnd);

        // 打榜作品累计, 浏览数,评价数
        int newCourses = 0;
        int totalViewCount = 0;
        int totalCommentCount = 0;

        List<TeacherCourseware> coursewareList = teacherCoursewareBuffer.getNativeBuffer().dump().getData();
        for (TeacherCourseware courseware : coursewareList) {
            if (courseware.getStatus() != TeacherCourseware.Status.PUBLISHED) {
                continue;
            }
            newCourses++;
            totalViewCount += SafeConverter.toInt(courseware.getVisitNum());
            totalCommentCount += SafeConverter.toInt(courseware.getCommentNum());
        }

        retData.put("newCourses", newCourses);
        retData.put("totalViewCount", totalViewCount);
        retData.put("totalCommentCount", totalCommentCount);

        // 三科作品高分合集
        for (Subject subject : VALID_SUBJECTS) {
            List<Map<String, Object>> excellentData = TeacherCourseCache.loadExcellentMonthlyRankData(subject.name(), month);
            if (CollectionUtils.isEmpty(excellentData)) {
                log.warn("loadWeeklyPopularityRanking failed, subject:{}, month:{}", subject, month);
                continue;
            }

            List<Map<String, Object>> top3 = new ArrayList<>();
            if (excellentData.size() > 3) {
                top3.addAll(excellentData.subList(0, 3));
            } else {
                top3.addAll(excellentData);
            }

            top3 = updateCourseInfo(top3);
            retData.put(subject.name().toLowerCase() + "Top3", top3);
        }

        TeacherCourseCache.getCache().set(cacheKey, 24*60*60*20, retData);

        return retObj.add("data", retData);
    }

    @Override
    public Integer loadCourseShareNum(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return 0;
        }

        Integer shareNum = TeacherCourseCache.loadCourseShareNum(courseId);
        if (shareNum != null) {
            return shareNum;
        }

        shareNum = teacherCoursewareStatisticsDao.loadByCoursewareId(courseId, "share", RANKING_START_DATE, new Date()).size();

        TeacherCourseCache.setCourseShareNum(courseId, shareNum);

        return shareNum;
    }

    @Override
    public MapMessage loadCanvassData(String subject) {
        if (StringUtils.isBlank(subject)) {
            return MapMessage.errorMessage("参数错误");
        }

        List<Map<String, Object>> canvassData = internalLoadCanvassList(subject);

        return MapMessage.successMessage().add("data", canvassData);
    }

    @Override
    public MapMessage loadUserCanvassInfo(String subject, TeacherDetail teacher, String openId) {
        if (StringUtils.isBlank(subject)) {
            return MapMessage.errorMessage("参数错误");
        }

        List<Map<String, Object>> canvassData = internalLoadCanvassList(subject);

        // 补充投票数 和 当前用户的投票信息
        List<String> courseIds = canvassData.stream().map(p -> SafeConverter.toString(p.get("coursewareId"))).collect(Collectors.toList());
        Map<String, TeacherCourseware> coursewareInfo = teacherCoursewareDao.loads(courseIds);

        DayRange dayRange = DayRange.current();
        List<TeacherCoursewareStatistics> canvssRecords = new ArrayList<>();
        if (teacher != null) {
            canvssRecords = teacherCoursewareStatisticsDao.loadTeacherOpinfo(OP_CANVASS, teacher.getId())
                    .stream()
                    .filter(p -> dayRange.contains(p.getCreateTime()))
                    .collect(Collectors.toList());
        } else if (StringUtils.isNotEmpty(openId)){
            canvssRecords = teacherCoursewareStatisticsDao.loadUserOpinfo(OP_CANVASS, openId)
                    .stream()
                    .filter(p -> dayRange.contains(p.getCreateTime()))
                    .collect(Collectors.toList());
        }

        Map<String, Integer> userCanvass = canvssRecords.stream()
                .collect(Collectors.groupingBy(p -> p.getCourseware_id(), Collectors.summingInt(p -> 1)));

        int canvassLimit = 1;
        int canvassCount = 5;
        if (teacher != null && teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
            canvassLimit = 2;
            canvassCount = 10;
        }

        for (Map<String, Object> item : canvassData) {
            String courseId = SafeConverter.toString(item.get("coursewareId"));
            if (coursewareInfo.containsKey(courseId)) {
                item.put("totalCanvassNum", SafeConverter.toInt(coursewareInfo.get(courseId).getCanvassNum()));
            } else {
                item.put("totalCanvassNum", 0);
            }

            item.put("surplus", canvassLimit - userCanvass.getOrDefault(courseId, 0));
        }

        return MapMessage.successMessage().add("totalSurplus", canvassCount - canvssRecords.size())
                .add("canvassData", canvassData);
    }

    private List<Map<String, Object>> internalLoadCanvassList(String subject) {
        String cacheKey = "TS_CANVASS_DATA_" + subject;
        List<Map<String, Object>> canvassData = TeacherCourseCache.getCache().load(cacheKey);
        if (canvassData != null) {
            return canvassData;
        }

        String dataKey = "2018-12-20";
        Date curTime = new Date();
        /*if (RuntimeMode.lt(Mode.PRODUCTION)) {
            dataKey = DateUtils.dateToString(DateUtils.addDays(curTime, -1), DateUtils.FORMAT_SQL_DATE);
        }*/

        // 需求变了, 井凯悦说英语前100，数学前58，语文前42
        int dataSize = 42;
        if (Objects.equals(Subject.ENGLISH.name(), subject)) {
            dataSize = 100;
        } else if (Objects.equals(Subject.MATH.name(), subject)) {
            dataSize = 58;
        }

        List<Map<String, Object>> rankData = TeacherCourseCache.loadCanvassTopData(subject, dataKey);
        if (rankData == null) rankData = new ArrayList<>();
        canvassData = new ArrayList<>();

        // 评价数大于 n 才展示 语文 36 数学 33 英语 50
        if ("chinese".equalsIgnoreCase(subject)) {
            rankData = rankData.stream().filter(i -> MapUtils.getLong(i, "commentNum") >= 36).collect(Collectors.toList());
        } else if ("math".equalsIgnoreCase(subject)) {
            rankData = rankData.stream().filter(i -> MapUtils.getLong(i, "commentNum") >= 33).collect(Collectors.toList());
        } else {
            rankData = rankData.stream().filter(i -> MapUtils.getLong(i, "commentNum") >= 50).collect(Collectors.toList());
        }

        if (rankData.size() > dataSize) {
            canvassData.addAll(rankData.subList(0, dataSize));
        } else {
            canvassData.addAll(rankData);
        }

        if (CollectionUtils.isNotEmpty(canvassData)) {
            TeacherCourseCache.getCache().set(cacheKey, 86400 * 29, canvassData);
        }

        return canvassData;
    }

    public static void main(String[] args) {
        Date curTime = new Date();
        Integer week = (int) DateUtils.dayDiff(curTime, RANKING_START_DATE) / 7;
        System.out.println(week);

        Date time = DateUtils.stringToDate("2018-11-04 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
        week = (int) DateUtils.dayDiff(time, RANKING_START_DATE) / 7;
        System.out.println(week);
        time = DateUtils.stringToDate("2018-11-05 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
        week = (int) DateUtils.dayDiff(time, RANKING_START_DATE) / 7;
        System.out.println(week);

        System.out.println(DateUtils.dayDiff(curTime, RANKING_START_DATE));

        WeekRange weekRange = WeekRange.newInstance(RANKING_START_DATE.getTime());
        System.out.println(weekRange.getStartDate());
        System.out.println(weekRange.getEndDate());
        weekRange = weekRange.next();
        System.out.println(weekRange.getStartDate());
        System.out.println(weekRange.getEndDate());
        weekRange = WeekRange.newInstance(DateUtils.addWeeks(RANKING_START_DATE, 2).getTime());
        System.out.println(weekRange.getStartDate());
        System.out.println(weekRange.getEndDate());

        DayRange range = DayRange.newInstance(RANKING_START_DATE.getTime());
//        System.out.println(range.getStartDate());
//        System.out.println(range.getEndDate());
//        System.out.println(range.previous().getStartDate());
//        System.out.println(range.previous().getEndDate());

        Calendar calendar = Calendar.getInstance();
        Date time1 = DateUtils.stringToDate("2018-11-05 00:59:59", DateUtils.FORMAT_SQL_DATETIME);
        calendar.setTime(time1);
        System.out.println(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY);



        System.out.println("=================");
        curTime = DateUtils.stringToDate("2018-12-12 00:00:00");
        Integer maxWeek = (int) DateUtils.dayDiff(curTime, RANKING_START_DATE) / 7;

        System.out.println(maxWeek);

        for (int i=1; i <= maxWeek; i++) {
            WeekRange curTimeRange = WeekRange.newInstance(DateUtils.addWeeks(RANKING_START_DATE, i - 1).getTime());
            System.out.println( i + "==" + DateUtils.dateToString(curTimeRange.getStartDate(), "yyyy-MM-dd") + "---" + DateUtils.dateToString(curTimeRange.getEndDate(), "yyyy-MM-dd"));
        }

        List<String> data1 = Arrays.asList("1", "2", "3", "4");
        System.out.println(data1.subList(0, 3));

    }
}
