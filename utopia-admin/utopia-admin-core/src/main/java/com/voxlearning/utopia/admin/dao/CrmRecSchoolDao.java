package com.voxlearning.utopia.admin.dao;

import com.mongodb.WriteConcern;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.CrmRecSchool;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jiang wei on 2016/7/26.
 */

@Named
public class CrmRecSchoolDao extends AlpsStaticMongoDao<CrmRecSchool, String> {

    @Override
    protected void calculateCacheDimensions(CrmRecSchool source, Collection<String> dimensions) {

    }


    public List<CrmRecSchool> findSchoolsByAllType(Integer provinceId, Integer cityId, Integer countyId, String schoolName, String status, String verify) {

        Criteria criteriaProvinceId = Criteria.where("province_id").is(provinceId);
        Criteria criteriaCityId = Criteria.where("city_id").is(cityId);
        Criteria criteriaCountyId = Criteria.where("county_id").is(countyId);
        Pattern p = Pattern.compile(".*"+schoolName+".*");
        Criteria criteriaSchoolName = Criteria.where("name").regex(p);
        Criteria criteriaStatus = Criteria.where("status").is(status);
        Criteria criteriaVerify = Criteria.where("verify").is(verify);
        Criteria andCriteria = Criteria.and(criteriaProvinceId, criteriaCityId, criteriaSchoolName, criteriaStatus,
                criteriaVerify, criteriaCountyId);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "dt"));
        Query query = Query.query(andCriteria).with(sort);
        return query(query);
    }

    public List<CrmRecSchool> findSchoolsByRegion(Integer provinceId, Integer cityId, Integer countyId, String status, String verify) {

        Criteria criteriaProvinceId = Criteria.where("province_id").is(provinceId);
        Criteria criteriaCityId = Criteria.where("city_id").is(cityId);
        Criteria criteriaCountyId = Criteria.where("county_id").is(countyId);
        Criteria criteriaStatus = Criteria.where("status").is(status);
        Criteria criteriaVerify = Criteria.where("verify").is(verify);
        Criteria andCriteria = Criteria.and(criteriaProvinceId, criteriaCityId, criteriaStatus,
                criteriaVerify, criteriaCountyId);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "dt"));
        Query query = Query.query(andCriteria).with(sort);
        return query(query);
    }

    public List<CrmRecSchool> findSchoolsByCityAndProvince(Integer provinceId, Integer cityId, String status, String verify) {

        Criteria criteriaProvinceId = Criteria.where("province_id").is(provinceId);
        Criteria criteriaCityId = Criteria.where("city_id").is(cityId);
        Criteria criteriaStatus = Criteria.where("status").is(status);
        Criteria criteriaVerify = Criteria.where("verify").is(verify);
        Criteria andCriteria = Criteria.and(criteriaProvinceId, criteriaCityId, criteriaStatus,
                criteriaVerify);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "dt"));
        Query query = Query.query(andCriteria).with(sort);
        return query(query);
    }


    public List<CrmRecSchool> findSchoolsByCityAndName(Integer provinceId, Integer cityId, String schoolName, String status, String verify) {

        Criteria criteriaProvinceId = Criteria.where("province_id").is(provinceId);
        Criteria criteriaCityId = Criteria.where("city_id").is(cityId);
        Pattern p = Pattern.compile(".*"+schoolName+".*");
        Criteria criteriaSchoolName = Criteria.where("name").regex(p);
        Criteria criteriaStatus = Criteria.where("status").is(status);
        Criteria criteriaVerify = Criteria.where("verify").is(verify);
        Criteria andCriteria = Criteria.and(criteriaProvinceId, criteriaCityId, criteriaSchoolName, criteriaStatus,
                criteriaVerify);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "dt"));
        Query query = Query.query(andCriteria).with(sort);
        return query(query);
    }

    public List<CrmRecSchool> findSchoolsByStatus(String status, String verify) {

        Criteria criteriaStatus = Criteria.where("status").is(status);
        Criteria criteriaVerify = Criteria.where("verify").is(verify);
        Criteria andCriteria = Criteria.and(criteriaStatus,
                criteriaVerify);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "dt"));
        Query query = Query.query(andCriteria).with(sort);
        return query(query);
    }


    public BsonDocument updateCrmRecSchool(CrmRecSchool crmRecSchool) {
        Criteria criteria = Criteria.where("_id").is(crmRecSchool.getId());
        Bson filter = criteriaTranslator.translate(criteria);
        Update update = new Update();
        update.set("name", crmRecSchool.getSchoolName());
        update.set("addr", crmRecSchool.getAddr());
        update.set("blat", crmRecSchool.getBlat());
        update.set("blon", crmRecSchool.getBlon());
        update.set("verify", crmRecSchool.getVerify());
        update.set("verify_mode", crmRecSchool.getVerifyMode());
        update.set("status", crmRecSchool.getStatus());
        update.set("audit_result", crmRecSchool.getAuditResult());
        update.set("auditor", crmRecSchool.getAuditor());
        update.set("update_time", crmRecSchool.getUpdateTime());
        return createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED).findOneAndUpdate(filter, updateTranslator.translate(update));
    }

    public List<CrmRecSchool> findSchoolById(Integer schoolId) {

        Criteria criteriaSchoolId = Criteria.where("id").is(schoolId);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "dt"));
        Query query = Query.query(criteriaSchoolId).with(sort);

        return query(query);
    }


    public List<CrmRecSchool> findSchoolByIdAndStatus(Integer schoolId,String status,String verify) {

        Criteria criteriaSchoolId = Criteria.where("id").is(schoolId);
        Criteria criteriaSchoolStatus = Criteria.where("status").is(status);
        Criteria criteriaSchoolVerify = Criteria.where("verify").is(verify);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "dt"));
        Criteria andCriteria = Criteria.and(criteriaSchoolId,
                criteriaSchoolStatus,criteriaSchoolVerify);
        Query query = Query.query(andCriteria).with(sort);

        return query(query);
    }


}
