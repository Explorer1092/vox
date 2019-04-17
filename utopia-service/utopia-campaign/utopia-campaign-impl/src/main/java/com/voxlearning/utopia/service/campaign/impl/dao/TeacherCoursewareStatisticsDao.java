package com.voxlearning.utopia.service.campaign.impl.dao;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareStatistics;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Named
@CacheBean(type = TeacherCoursewareStatistics.class)
public class TeacherCoursewareStatisticsDao extends AlpsStaticJdbcDao<TeacherCoursewareStatistics, Long> {

    @Override
    public void afterPropertiesSet() {
        utopiaSqlCampaign = utopiaSqlFactory.getUtopiaSql("hs_misc");
    }

    @Override
    protected void calculateCacheDimensions(TeacherCoursewareStatistics document, Collection<String> dimensions) {
        dimensions.add(TeacherCoursewareStatistics.ck_opuser(document.getType(), document.getOperate_teacher_id()));
        dimensions.add(TeacherCoursewareStatistics.ck_openuser(document.getType(), document.getWechatOpenId()));
    }

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    protected UtopiaSql utopiaSqlCampaign;

//    private final static String STATISTICS_BY_COURSEWARE_ID_SQL = "SELECT COURSEWARE_ID coursewareId,COUNT(1) num " +
//            "FROM ( SELECT COURSEWARE_ID,OPERATE_TEACHER_ID,TYPE FROM `VOX_TEACHER_COURSEWARE_STATISTICS`" +
//            " WHERE CREATE_TIME BETWEEN :startTime AND :endTime GROUP BY COURSEWARE_ID,OPERATE_TEACHER_ID,TYPE )" +
//            " a GROUP BY COURSEWARE_ID ORDER BY NUM DESC";
    private final static String STATISTICS_BY_COURSEWARE_ID_SQL = "SELECT COURSEWARE_ID coursewareId, COUNT(1) num FROM (\n" +
        "SELECT DISTINCT OPERATE_TEACHER_ID AS TEACHER_ID, COURSEWARE_ID, TYPE FROM VOX_TEACHER_COURSEWARE_STATISTICS WHERE TYPE='download' AND CREATE_TIME BETWEEN :startTime AND :endTime\n" +
        "UNION ALL\n" +
        "SELECT OPERATE_TEACHER_ID AS TEACHER_ID, COURSEWARE_ID, TYPE FROM VOX_TEACHER_COURSEWARE_STATISTICS WHERE TYPE='share' AND CREATE_TIME BETWEEN :startTime AND :endTime\n" +
        "UNION ALL\n" +
        "SELECT DISTINCT TEACHER_ID, COURSEWARE_ID, 'comment' from VOX_TEACHER_COURSEWARE_COMMENT WHERE CREATE_TIME BETWEEN :startTime AND :endTime\n" +
        ") t GROUP BY coursewareId\n" +
        "ORDER BY NUM DESC";

    private final static String DOWNLOAD_STAT_SQL = "SELECT COURSEWARE_ID,COUNT(1) NUM " +
            "FROM ( SELECT COURSEWARE_ID,OPERATE_TEACHER_ID,TYPE FROM `VOX_TEACHER_COURSEWARE_STATISTICS`" +
            " WHERE CREATE_TIME BETWEEN :startTime AND :endTime AND TYPE='download' GROUP BY COURSEWARE_ID,OPERATE_TEACHER_ID,TYPE)" +
            " a GROUP BY COURSEWARE_ID ORDER BY NUM DESC";

    private final static String STATISTICS_BY_TEACHER_ID_SQL = "SELECT TEACHER_ID as teacherId, COUNT(1) AS totalNum FROM \n" +
        "( SELECT DISTINCT OPERATE_TEACHER_ID AS TEACHER_ID, COURSEWARE_ID, TYPE FROM VOX_TEACHER_COURSEWARE_STATISTICS WHERE TYPE='download' AND CREATE_TIME BETWEEN :startTime AND :endTime \n" +
            "UNION ALL \n" +
            "SELECT OPERATE_TEACHER_ID AS TEACHER_ID, COURSEWARE_ID, TYPE FROM VOX_TEACHER_COURSEWARE_STATISTICS WHERE TYPE='share' AND CREATE_TIME BETWEEN :startTime AND :endTime \n" +
            "UNION ALL \n" +
            "SELECT DISTINCT TEACHER_ID, COURSEWARE_ID, 'comment' from VOX_TEACHER_COURSEWARE_COMMENT WHERE CREATE_TIME BETWEEN :startTime AND :endTime) t\n" +
        "GROUP BY TEACHER_ID\n" +
        "ORDER BY totalNum desc limit 100";

    // 作品总点评人数
    private static final String STAT_COMMENT_NUM_SQL = "SELECT count(DISTINCT TEACHER_ID) AS NUM from VOX_TEACHER_COURSEWARE_COMMENT WHERE CREATE_TIME BETWEEN :startTime AND :endTime";

    private static final String STAT_SHARE_NUM_SQL = "SELECT count(1) as NUM from VOX_TEACHER_COURSEWARE_STATISTICS where TYPE='share' and CREATE_TIME BETWEEN :startTime AND :endTime";

    public List<Map<String, Object>> loadStatisticsInfoByCourseId(Date startTime, Date endTime) {
        List<Map<String, Object>> result = utopiaSqlCampaign.withSql(STATISTICS_BY_COURSEWARE_ID_SQL).
                useParams(MiscUtils.map().add("startTime", startTime).add("endTime", endTime)).queryAll();
        return result;
    }

    public List<Map<String, Object>> loadDownloadStatInfo(Date startTime, Date endTime) {
        List<Map<String, Object>> result = utopiaSqlCampaign.withSql(DOWNLOAD_STAT_SQL).
                useParams(MiscUtils.map().add("startTime", startTime).add("endTime", endTime)).queryAll();
        return result;
    }

    public Long loadTotalCommentStatInfo(Date startTime, Date endTime) {
        return utopiaSqlCampaign.withSql(STAT_COMMENT_NUM_SQL).useParams(MiscUtils.map()
                        .add("startTime", startTime)
                        .add("endTime", endTime)).queryValue(Long.class);
    }

    public Long loadTotalShareStatInfo(Date startTime, Date endTime) {
        return utopiaSqlCampaign.withSql(STAT_SHARE_NUM_SQL).useParams(MiscUtils.map()
                .add("startTime", startTime)
                .add("endTime", endTime)).queryValue(Long.class);
    }

    public List<Map<String, Object>> loadStatisticsInfoByTeacherId(Date startTime, Date endTime) {
        List<Map<String, Object>> result = utopiaSqlCampaign.withSql(STATISTICS_BY_TEACHER_ID_SQL).
                useParams(MiscUtils.map().add("startTime", startTime).add("endTime", endTime)).queryAll();
        return result;
    }

    public List<TeacherCoursewareStatistics> loadByCoursewareId(String coursewareId,String operationType,
                                                                Date startTime, Date endTime) {
        Criteria criteria = Criteria.where("COURSEWARE_ID").is(coursewareId);
        if (StringUtils.isNotEmpty(operationType)){
            criteria.and("TYPE").is(operationType);
        }
        criteria.and("CREATE_TIME").gt(startTime).lt(endTime);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<TeacherCoursewareStatistics> loadTeacherOpinfo(@CacheParameter("OP") String op, @CacheParameter("TID")Long teacherId) {
        Criteria criteria = Criteria.where("OPERATE_TEACHER_ID").is(teacherId).and("TYPE").is(op);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<TeacherCoursewareStatistics> loadUserOpinfo(@CacheParameter("OP") String op, @CacheParameter("OID")String openId) {
        Criteria criteria = Criteria.where("WECHAT_OPEN_ID").is(openId).and("TYPE").is(op);
        return query(Query.query(criteria));
    }

}
