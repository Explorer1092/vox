package com.voxlearning.utopia.service.campaign.impl.dao;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareComment;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 *
 */
@Named
@CacheBean(type = TeacherCoursewareComment.class)
public class TeacherCoursewareCommentDao extends AlpsStaticJdbcDao<TeacherCoursewareComment, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherCoursewareComment document, Collection<String> dimensions) {
        dimensions.add(TeacherCoursewareComment.ck_tid_cid(document.getTeacher_id(), document.getCourseware_id()));
    }

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    protected UtopiaSql utopiaSqlCampaign;

    @CacheMethod
    public List<TeacherCoursewareComment> loadTeacherCoursewareComment(@CacheParameter("TID")Long teacherId,
                                                          @CacheParameter("CID")String coursewareId) {
         Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId).and("COURSEWARE_ID").is(coursewareId);
         return query(Query.query(criteria));
    }

    public Long loadCountByTeacherId(Long teacherId,Date startTime,Date endTime) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        criteria.and("CREATE_TIME").gt(startTime).and("CREATE_TIME").lt(endTime);
        Query query = new Query(criteria);
        return count(query);
    }

    private final static String STATISTICS_TEACHER_COMMENT_NUM_SQL = "SELECT TEACHER_ID,COUNT(TEACHER_ID) NUM FROM " +
            "`VOX_TEACHER_COURSEWARE_COMMENT` WHERE CREATE_TIME BETWEEN :startTime AND :endTime GROUP BY TEACHER_ID";

    @CacheMethod
    public List<Map<String, Object>> loadTeacherCommentNumInfo(Date startTime, Date endTime) {
        utopiaSqlCampaign = utopiaSqlFactory.getUtopiaSql("hs_misc");
        List<Map<String, Object>> result = utopiaSqlCampaign.withSql(STATISTICS_TEACHER_COMMENT_NUM_SQL).
                useParams(MiscUtils.map().add("startTime", startTime).add("endTime", endTime)).queryAll();
        return result;
    }

}
