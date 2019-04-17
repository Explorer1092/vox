package com.voxlearning.utopia.service.campaign.impl.dao;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareDownload;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 */
@Named
@CacheBean(type = TeacherCoursewareDownload.class)
public class TeacherCoursewareDownloadDao extends AlpsStaticJdbcDao<TeacherCoursewareDownload, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherCoursewareDownload document, Collection<String> dimensions) {
        dimensions.add(TeacherCoursewareDownload.ck_teacher(document.getTeacher_id()));
    }

    @CacheMethod
    public List<TeacherCoursewareDownload> loadCoursewareDownloadInfo(@CacheParameter("TID")Long teacherId) {
         Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
         return query(Query.query(criteria));
    }

//    public int updateDownloadNum(Long teacherId, Integer downloadNum){
//
//        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
//
//        List<TeacherCoursewareDownload> teacherCoursewareDownloads = query(Query.query(criteria));
//
//        Update update = Update.update("ALREADY_DOWNLOAD_TIMES", downloadNum);
//        int rows = (int) $update(update, criteria);
//        if (rows > 0) {
//            Set<String> dimensions = new HashSet<>();
//            teacherCoursewareDownloads.forEach(e -> calculateCacheDimensions(e, dimensions));
//            getCache().delete(dimensions);
//        }
//        return rows;
//    }
//
//    public int updateAllowDownloadNum(Long teacherId, String courseId, Integer downloadNum){
//
//        Criteria criteria = Criteria.where("COURSEWARE_ID").is(courseId)
//                .and("TEACHER_ID").is(teacherId);
//        List<TeacherCoursewareDownload> teacherCoursewareDownloads = query(Query.query(criteria));
//
//        Update update = Update.update("ALLOW_DOWNLOAD_TIMES", downloadNum);
//        int rows = (int) $update(update, criteria);
//        if (rows > 0) {
//            Set<String> dimensions = new HashSet<>();
//            teacherCoursewareDownloads.forEach(e -> calculateCacheDimensions(e, dimensions));
//            getCache().delete(dimensions);
//        }
//        return rows;
//    }
//
//    public int updateLotteryNum(Long teacherId,String courseId,Integer lotteryNum){
//
//        Criteria criteria = Criteria.where("COURSEWARE_ID").is(courseId)
//                .and("TEACHER_ID").is(teacherId);
//        List<TeacherCoursewareDownload> teacherCoursewareDownloads = query(Query.query(criteria));
//
//        Update update = Update.update("ALLOW_LOTTERY_TIMES", lotteryNum);
//        int rows = (int) $update(update, criteria);
//        if (rows > 0) {
//            Set<String> dimensions = new HashSet<>();
//            teacherCoursewareDownloads.forEach(e -> calculateCacheDimensions(e, dimensions));
//            getCache().delete(dimensions);
//        }
//        return rows;
//    }

}
