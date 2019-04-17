package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.activity.TeacherScholarshipRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TeacherScholarshipRecord.class)
public class TeacherScholarShipRecordDao extends AlpsStaticJdbcDao<TeacherScholarshipRecord,Long>{

    @Override
    protected void calculateCacheDimensions(TeacherScholarshipRecord document, Collection<String> dimensions) {
        dimensions.add(TeacherScholarshipRecord.ck_teacher_id(document.getTeacherId()));
    }

    @CacheMethod
    public TeacherScholarshipRecord loadByTeacherId(@CacheParameter("TEACHER_ID") Long teacherId){
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public TeacherScholarshipRecord upsertRecord(TeacherScholarshipRecord record){
        return upsert(record);
    }

    /**
     *
     * @param teacherId
     * @param termReviewNumDelta    期末复习布置次数增量
     * @param basicReviewNumDelta   基础必过布置次数增量
     * @param finishRate       完成人数增量
     * @param score            总分数增量
     * @return
     */
    public boolean updateTeacherRecordVariable(Long teacherId,
                                               int termReviewNumDelta,
                                               int basicReviewNumDelta,
                                               double finishRate,
                                               double score){

        TeacherScholarshipRecord org = loadByTeacherId(teacherId);
        if(org == null)
            return false;

        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);

        Update update = new Update();
        update.inc("TERM_REVIEW_NUM",termReviewNumDelta);
        update.inc("BASIC_REVIEW_NUM",basicReviewNumDelta);
        update.set("FINISH_RATE",finishRate);
        update.set("SCORE",score);

        long count = $update(update,criteria);
        if(count <= 0)
            return false;

        evictDocumentCache(org);
        return true;
    }

    public boolean updateDailyLottery(){
        Update update = Update.update("DAILY_LOTTERY",false);
        Criteria criteria = new Criteria();
        List<TeacherScholarshipRecord> originals = query(Query.query(criteria));
        if (originals.isEmpty()) {
            return false;
        }
        long count = $update(update,criteria);
        if (count > 0) {
            evictDocumentCache(originals);
        }
        return true;
    }

    public boolean updateWeekLottery(){
        Update update = Update.update("WEEK_LOTTERY",false);
        Criteria criteria = new Criteria();
        List<TeacherScholarshipRecord> originals = query(Query.query(criteria));
        if (originals.isEmpty()) {
            return false;
        }
        long count = $update(update,criteria);
        if (count > 0) {
            evictDocumentCache(originals);
        }
        return true;
    }

}
