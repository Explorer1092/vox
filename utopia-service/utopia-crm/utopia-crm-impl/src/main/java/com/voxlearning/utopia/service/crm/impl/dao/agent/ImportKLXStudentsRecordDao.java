package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.ImportKLXStudentsRecord;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by dell on 2017/4/11.
 */
@Named
@CacheBean(type = ImportKLXStudentsRecord.class)
public class ImportKLXStudentsRecordDao  extends StaticCacheDimensionDocumentMongoDao<ImportKLXStudentsRecord,String> {

    protected void calculateCacheDimensions(ImportKLXStudentsRecord source, Collection<String> dimensions) {

    }
    public List<ImportKLXStudentsRecord> loadImportKLXStudentsRecords(Long operatorId ,String sourceType){
        Criteria criteria = Criteria.where("operatorId").is(operatorId).and("sourceType").is(sourceType);
        return query(Query.query(criteria));
    }
}
