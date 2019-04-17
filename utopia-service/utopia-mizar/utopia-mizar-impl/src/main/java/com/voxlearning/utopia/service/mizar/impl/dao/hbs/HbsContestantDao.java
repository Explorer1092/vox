package com.voxlearning.utopia.service.mizar.impl.dao.hbs;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsContestant;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by haitian.gan on 2017/2/15.
 */
@Named
@CacheBean(type=HbsContestant.class,expiration = @UtopiaCacheExpiration(value = 300))
public class HbsContestantDao extends AlpsStaticJdbcDao<HbsContestant,Long>{

    public HbsContestant getByStudentId(Long studentId){
        Criteria criteria = Criteria.where("stu_id").is(studentId);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public HbsContestant loadByStudentId(@CacheParameter("UID") Long studentId){
        Criteria criteria = Criteria.where("stu_id").is(studentId);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public HbsContestant loadByIdCardNo(@CacheParameter("ID_CARD_NO") String idCardNo){
        Criteria criteria = Criteria.where("paper_no").is(idCardNo);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public HbsContestant loadByPhoneNumber(@CacheParameter("MOBILE") String phoneNumber){
        Criteria criteria = Criteria.where("patriarch_phone").is(phoneNumber);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @Override
    protected void calculateCacheDimensions(HbsContestant document, Collection<String> dimensions) {
        dimensions.add(document.ck_uid(document.getUserId()));
        dimensions.add(document.ck_mobile(document.getPhoneNumber()));
        dimensions.add(document.ck_idcardno(document.getIdCardNo()));
    }

    public int updatePhoneNumber(Long stuId,String phoneNumber) {
        HbsContestant origin = loadByStudentId(stuId);
        if(origin == null)
            return 0;

        Criteria criteria = Criteria.where("stu_id").is(stuId);
        Update update = Update.update("patriarch_phone",phoneNumber);
        int rows =  (int)$update(update,criteria);
        if(rows > 0){
            evictDocumentCache(origin);
        }

        return rows;
    }
}
