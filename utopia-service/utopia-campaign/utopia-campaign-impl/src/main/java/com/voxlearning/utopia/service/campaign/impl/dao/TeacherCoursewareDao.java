package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareBookInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewarePageInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


/**
 *
 */
@Named
@CacheBean(type = TeacherCourseware.class)
public class TeacherCoursewareDao extends AlpsStaticMongoDao<TeacherCourseware, String> {

    @Inject
    private TeacherCoursewareVersion teacherCoursewareVersion;

    @Override
    protected void calculateCacheDimensions(TeacherCourseware document, Collection<String> dimensions) {
        dimensions.add(TeacherCourseware.ck_teacher(document.getTeacherId()));
        dimensions.add(TeacherCourseware.ck_Id(document.getId()));
    }

    @CacheMethod
    public List<TeacherCourseware> loadByTeacherId(@CacheParameter("TID") Long teacherId) {
        Criteria criteria = Criteria.where("teacherId").is(teacherId).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    public List<TeacherCourseware> loadAll() {
        return query();
    }

    public List<TeacherCourseware> loadByExamineStatus(TeacherCourseware.ExamineStatus status) {
        Criteria criteria = Criteria.where("examineStatus")
                                    .is(status.name())
                                    .and("disabled")
                                    .is(false);
        return query(Query.query(criteria));
    }

    public void incCanvassNum(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateTime", new Date());
        update.inc("canvassNum", 1);
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCacheById(id);
        }
    }

    public void incCanvassHelper(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateTime", new Date());
        update.inc("canvassHelperNum", 1);
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCacheById(id);
        }
    }

    public void updateStatusToExaming(String id) {
        Criteria criteria = Criteria.where("_id").is(id)
                .and("disabled").is(false);
        Update update = new Update()
                .set("status", TeacherCourseware.Status.EXAMINING.name())
                .set("examineStatus", TeacherCourseware.ExamineStatus.WAITING.name())
                .set("examineUpdateTime", new Date())
                .set("examiner", "")
                .set("examineExt", "")
                .set("updateTime", new Date());
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }

    }

    public void delete(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().set("disabled", true)
                                    .set("updateTime", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    /**
     * @see TeacherCoursewareDao#updateExamineStatus(java.lang.String, com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware.ExamineStatus, java.lang.String, java.lang.String)
     */
    @Deprecated
    public long updateExamineStatus(String id, TeacherCourseware.ExamineStatus from, TeacherCourseware.ExamineStatus to, String updater, String extInfo) {
        return updateExamineStatus(id, to, updater, extInfo);
    }

    public long updateExamineStatus(String id, TeacherCourseware.ExamineStatus to, String updater, String extInfo) {
        Criteria criteria = Criteria.where("_id").is(id)
                .and("disabled").is(false);

        Update update = new Update()
                .set("examineStatus", to)
                .set("examiner", updater)
                .set("examineUpdateTime", new Date());
        if (StringUtils.isNotBlank(extInfo)) {
            update.set("examineExt", extInfo);
        }

        if (to == TeacherCourseware.ExamineStatus.FAILED) {
            update.set("status", TeacherCourseware.Status.REJECTED);
        }

        if (to == TeacherCourseware.ExamineStatus.PASSED) {
            update.set("status", TeacherCourseware.Status.PUBLISHED);
        }

        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0L) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
        return count;
    }

    public void updateContent(String id, String title, String description) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateTime", new Date());
        if (StringUtils.isNotBlank(title)) {
            update.set("title", title);
        }
        if (StringUtils.isNotBlank(description)) {
            update.set("description", description);
        }

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public void updateStatus(String id, String status) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateTime", new Date());
        if (StringUtils.isNotBlank(status)) {
            update.set("status", status);
        }

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public void updateFileUrl(String id, String fileUrl,String name) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateTime", new Date());
        if (StringUtils.isNotBlank(fileUrl)) {
            update.set("coursewareFile", fileUrl);
        }
        if (StringUtils.isNotBlank(name)){
            update.set("coursewareFileName", name);
        }
        update.set("needPackage", true);

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public void updateWordFileUrl(String id, String fileUrl,String name) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateTime", new Date());
        if (StringUtils.isNotBlank(fileUrl)) {
            update.set("wordUrl", fileUrl);
        }
        if (StringUtils.isNotBlank(name)){
            update.set("wordName", name);
        }
        update.set("needPackage", true);

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public boolean updateFileInfo(String id, String fileUrl, String fileName) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateTime", new Date());

        if (StringUtils.isNotBlank(fileUrl)) {
            update.set("coursewareFile", fileUrl);
        }

        if (StringUtils.isNotBlank(fileName)) {
            update.set("coursewareFileName", fileName);
        }

        update.set("needPackage", true);

        update.set("coursewareFilePreview", Collections.emptyList());
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
            return true;
        }
        return false;
    }

    public void updateFilePreview(String id, List<String> filePreview) {
        if (CollectionUtils.isEmpty(filePreview)) {
            return;
        }
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("coursewareFilePreview", filePreview);

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public void updateWordPreview(String id, List<String> filePreview) {
        if (CollectionUtils.isEmpty(filePreview)) {
            return;
        }
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("wordFilePreview", filePreview);

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public void updateBookInfo(String id, TeacherCoursewareBookInfo content) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("updateTime", new Date());
        if (StringUtils.isNotBlank(content.getBookId())) {
            update.set("bookId", content.getBookId());
        }

        if (content.getSubject() != null) {
            update.set("subject", content.getSubject());
        }

        if (StringUtils.isNotBlank(content.getUnitId())) {
            update.set("unitId", content.getUnitId());
        }

        if (content.getClazzLevel() != null) {
            update.set("clazzLevel", content.getClazzLevel());
        }

        if (content.getTermType() != null) {
            update.set("termType", content.getTermType());
        }

        if (content.getLessonId() != null){
            update.set("lessonId", content.getLessonId());
        }
//        if (content.getSerieId() != null){
//            update.set("serieId", content.getSerieId());
//        }
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    private void cleanCacheById(String id) {
        TeacherCourseware teacherCourseware = load(id);
        if (teacherCourseware != null) {
            Set<String> cacheIds = new HashSet<>();
            cacheIds.add(TeacherCourseware.ck_teacher(teacherCourseware.getTeacherId()));
            cacheIds.add(TeacherCourseware.ck_Id(id));
            getCache().deletes(cacheIds);
        }
    }

    public void cleanPictureCache(String id){
        cleanCacheById(id);
    }

    public long countByTeacherId(TeacherCoursewareParam param){
        Criteria criteria = Criteria.where("teacherId").is(param.getTeacherId()).and("disabled").is(false);
        if (StringUtils.isNotBlank(param.getStatus())){
            criteria.and("status").is(param.getStatus());
        }
        Query query = new Query(criteria);
        return count(query);
    }

    public Long count(TeacherCoursewareParam param){
        Criteria criteria = Criteria.where("disabled").is(false);
        if (StringUtils.isNotBlank(param.getStatus())){
            criteria.and("status").is(param.getStatus());
        }
        Query query = new Query(criteria);
        return count(query);
    }

    public List<TeacherCourseware> fetchCoursewareByPage(TeacherCoursewarePageInfo pageInfo){
        Criteria criteria = Criteria.where("teacherId").is(pageInfo.getTeacherId()).and("disabled").is(false);
        if (StringUtils.isNotBlank(pageInfo.getStatus())){
            criteria.and("status").is(pageInfo.getStatus());
        }
        Query query = Query.query(criteria);
        query.limit(pageInfo.getPageSize()).skip(pageInfo.getPageNum() * pageInfo.getPageSize());
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        query.with(sort);
        return query(query);
    }

    public boolean updateWordFileInfo(String id, String fileUrl, String fileName) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("wordUpdateTime", new Date());

        if (StringUtils.isNotBlank(fileUrl)) {
            update.set("wordUrl", fileUrl);
        }

        if (StringUtils.isNotBlank(fileName)) {
            update.set("wordName", fileName);
        }
        update.set("needPackage", true);

        update.set("wordFilePreview", Collections.emptyList());
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
            return true;
        }
        return false;
    }

    public boolean updatePictureFileInfo(String id, List<Map<String,String>> urlNameList) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("pictureUpdateTime", new Date());

        update.set("picturePreview", urlNameList);
        update.set("needPackage", true);
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
            return true;
        }
        return false;
    }

    public boolean updateCover(String id, String fileUrl, String fileName,Boolean isUserUpload) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("coverUpdateTime", new Date());

        if (StringUtils.isNotBlank(fileUrl)) {
            update.set("coverUrl", fileUrl);
        }

        if (StringUtils.isNotBlank(fileName)) {
            update.set("coverName", fileName);
        }

        if (isUserUpload != null) {
            update.set("isUserUpload",isUserUpload);
        }

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
            return true;
        }
        return false;
    }

    public boolean deletePicture(String id,List<Map<String,String>> pictureList) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("pictureUpdateTime", new Date());

        if (CollectionUtils.isNotEmpty(pictureList)) {
            update.set("picturePreview", pictureList);
            update.set("needPackage", true);
        }

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
            return true;
        }
        return false;
    }

    public boolean updateCompressedFile(String id, String fileUrl, String fileName) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("compressedFileUpdateTime", new Date());

        if (StringUtils.isNotBlank(fileUrl)) {
            update.set("compressedFileUrl", fileUrl);
        }

        if (StringUtils.isNotBlank(fileName)) {
            update.set("compressedFileName", fileName);
        }

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
            return true;
        }
        return false;
    }

    @CacheMethod
    public List<TeacherCourseware> fetchNewestCourseware(int limitNum){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("status").is(TeacherCourseware.Status.EXAMINING.name());
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        Query query = new Query(criteria).with(sort).limit(limitNum);
        return query(query);
    }

    public List<TeacherCourseware> fetchAllCoursewareByPage(TeacherCoursewarePageInfo pageInfo){
        Criteria criteria = Criteria.where("disabled").is(false);
        if (StringUtils.isNotBlank(pageInfo.getStatus())){
            criteria.and("status").is(pageInfo.getStatus());
        }
        Query query = Query.query(criteria);
        query.limit(pageInfo.getPageSize()).skip(pageInfo.getPageNum() * pageInfo.getPageSize());
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        query.with(sort);
        return query(query);
    }

    public void updateNewWordFileUrl(String id, String fileUrl ) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        if (StringUtils.isNotBlank(fileUrl)) {
            update.set("wordUrl", fileUrl);
        }
        update.set("isConvert",true);
        update.set("needPackage", true);

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public void incrementBufferVersion() {
        teacherCoursewareVersion.increment();
    }

    public void updateVisitNum(String courseId,Integer visitNum){
        Criteria criteria = Criteria.where("_id").is(courseId);
        Update update = new Update();
        update.set("visitNum",visitNum);
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(courseId);
            incrementBufferVersion();
        }
    }

    public void updateDownloadNum(String courseId,Integer downloadNum){
        Criteria criteria = Criteria.where("_id").is(courseId);
        Update update = new Update();
        update.set("downloadNum",downloadNum);
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(courseId);
            incrementBufferVersion();
        }
    }

    public void updateCommentNum(String courseId,Integer commentNum){
        Criteria criteria = Criteria.where("_id").is(courseId);
        Update update = new Update();
        update.set("commentNum",commentNum);
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(courseId);
        }
    }

    public void updateLabelInfo(String courseId,Map<String,Integer> labelInfo){
        Criteria criteria = Criteria.where("_id").is(courseId);
        Update update = new Update();
        update.set("labelInfo",labelInfo);
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(courseId);
            incrementBufferVersion();
        }
    }

    public void updateScoreInfo(String courseId,Integer totalScore){
        Criteria criteria = Criteria.where("_id").is(courseId);
        Update update = new Update();
        update.set("totalScore",totalScore);
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(courseId);
            incrementBufferVersion();
        }
    }

    public void updatePptCoursewareFile(String id, String fileUrl, String fileName) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        if (StringUtils.isNotBlank(fileUrl)) {
            update.set("pptCoursewareFile", fileUrl);
            update.set("pptCoursewareFileName", fileName);
            update.set("needPackage", true);
        }

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public void updateZipFile(String id, String fileUrl) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        if (StringUtils.isNotBlank(fileUrl)) {
            update.set("zipFileUrl", fileUrl);
            update.set("needPackage", false);
        }

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public List<TeacherCourseware> fetchMaxDownloadCourse(){
        Criteria criteria = new Criteria().and("status").is(TeacherCourseware.Status.PUBLISHED.name());
        Sort downloadSort =  new Sort(Sort.Direction.DESC,"downloadNum");
        Query query = new Query(criteria);
        query.with(downloadSort).skip(0).limit(1);
        return query(query);
    }

    public void updateCourseStarInfo(String id, Map<Integer,Integer> starInfo, Boolean authenticate){
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        if ( authenticate ) {
            update.set("authenticatedStarInfo", starInfo);
        } else {
            update.set("generalStarInfo", starInfo);
        }

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public void updateResourceName(String id, String pptName, String docName, String unPackagePptName) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        if (StringUtils.isNotEmpty(pptName)) {
            update.set("coursewareFileName", pptName);
        }
        if (StringUtils.isNotEmpty(unPackagePptName)) {
            update.set("pptCoursewareFileName", unPackagePptName);
        }
        if (StringUtils.isNotEmpty(docName)) {
            update.set("wordName", docName);
        }
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }

    public boolean updateAwardInfo(String id, List<Map<String,String>> pictureList,String awardLevelName,Integer awardLevelId,String awardIntroduction) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("awardPictureTime", new Date());

        update.set("awardPicturePreview", pictureList);
        update.set("awardLevelName", awardLevelName);
        update.set("awardLevelId", awardLevelId);
        update.set("awardIntroduction", awardIntroduction);
        update.set("needPackage", true);
        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
            return true;
        }
        return false;
    }

    public void cleanPptCoursewareInfo(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.unset("pptCoursewareFile");
        update.unset("pptCoursewareFileName");
        update.set("needPackage", true);

        TeacherCourseware teacherCourseware = executeFindOneAndUpdate(createMongoConnection(), criteria, update);
        if (teacherCourseware != null) {
            cleanCacheById(id);
            incrementBufferVersion();
        }
    }
}
