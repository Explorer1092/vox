package com.voxlearning.utopia.service.user.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.connection.IMongoConnection;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.utopia.entity.crm.CrmClazzSummary;
import com.voxlearning.utopia.entity.student.report.ReportStatus;
import com.voxlearning.utopia.service.config.client.ReportStatusServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CrmClazzSummaryDao
 *
 * @author song.wang
 * @date 2017/7/14
 */
@Named
@CacheBean(type = CrmClazzSummary.class)
@CacheDimension(value = CacheDimensionDistribution.OTHER_FIELDS)
public class CrmClazzSummaryDao extends AsyncDynamicMongoPersistence<CrmClazzSummary, String> {

    @Inject
    private ReportStatusServiceClient reportStatusServiceClient;

    @Override
    protected void calculateCacheDimensions(CrmClazzSummary crmClazzSummary, Collection<String> collection) {
        collection.add(CacheKeyGenerator.generateCacheKey(CrmClazzSummary.class, "SID", crmClazzSummary.getSchoolId()));
    }

    @Override
    protected String calculateDatabase(String template, CrmClazzSummary document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, CrmClazzSummary document) {
        ReportStatus reportStatus = reportStatusServiceClient.getReportStatusService()
                .loadReportStatus()
                .getUninterruptibly();
        if (reportStatus == null) {
            return null;
        }
        Map<String, Object> latestCollection = reportStatus.getLatestCollection();
        if (latestCollection == null) {
            return null;
        }
        Map map = (Map) latestCollection.get("vox_clazz_summary");
        if (map == null) {
            return null;
        }
        String collectionName = (String) map.get("collection_name");
        return StringUtils.isNotBlank(collectionName) ? collectionName : null;
    }

    @Override
    public List<CrmClazzSummary> query(Query query){
        MongoNamespace mongoNamespace = calculateIdMongoNamespace("Id");
        IMongoConnection connection = createMongoConnection(mongoNamespace);
        return $executeQuery(connection, query).getUninterruptibly();
    }

    @CacheMethod
    public List<CrmClazzSummary> loadBySchoolId(@CacheParameter("SID") Long schoolId){
        Criteria criteria = Criteria.where("schoolId").is(schoolId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<CrmClazzSummary>> loadBySchoolIds(@CacheParameter(value = "SID", multiple = true) Collection<Long> schoolIds) {
        Criteria criteria = Criteria.where("schoolId").in(schoolIds);
        List<CrmClazzSummary> classList = query(Query.query(criteria));
        Map<Long, List<CrmClazzSummary>> classMap = new HashMap<>();
        classMap = classList.stream()
                .filter(t -> t != null)
                .collect(Collectors.groupingBy(CrmClazzSummary::getSchoolId));
        return classMap;
    }
}
