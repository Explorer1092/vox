package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.admin.entity.CrmTeacherTransferSchoolRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author fugui.chang
 * @since 2017/2/13
 */
@Named
public class CrmTeacherTransferSchoolRecordDao  extends AlpsStaticMongoDao<CrmTeacherTransferSchoolRecord,String> {
    @Override
    protected void calculateCacheDimensions(CrmTeacherTransferSchoolRecord document, Collection<String> dimensions) {
    }

    public List<CrmTeacherTransferSchoolRecord> findCrmTeacherTransferSchoolRecords(Boolean isSourceSchoolDict, CrmTeacherTransferSchoolRecord.ChangeType changeType, Boolean authenticationState, CrmTeacherTransferSchoolRecord.CheckResult checkResult){
        //isSourceSchoolDict 为null表示查询时不分是否是重点校;authenticationState为null表示查询时老师不分是否认证;changeType为null表示查询时不分是否是带班转校
        if(checkResult == null){
            return Collections.emptyList();
        }

        Criteria criteria = Criteria.where("checkResult").is(checkResult);
        if(changeType!=null){
            criteria.and("changeType").is(changeType);
        }
        if(isSourceSchoolDict!=null){
            criteria.and("isSourceSchoolDict").is(isSourceSchoolDict);
        }
        if(authenticationState!=null){
            criteria.and("authenticationState").is(authenticationState);
        }

        return query(Query.query(criteria));
    }

}
