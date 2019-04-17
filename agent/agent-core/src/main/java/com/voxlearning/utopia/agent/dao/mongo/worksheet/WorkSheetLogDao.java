/**
 * Author:   xianlong.zhang
 * Date:     2018/10/17 18:37
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.dao.mongo.worksheet;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.worksheel.WorkSheetLog;

import javax.inject.Named;
import java.util.List;

@Named
@CacheBean(type = WorkSheetLog.class)
public class WorkSheetLogDao  extends StaticCacheDimensionDocumentMongoDao<WorkSheetLog, String> {

    @CacheMethod
    public List<WorkSheetLog> findLogBySheetId(@CacheParameter(value = "sheetId") Long sheetId) {
        Criteria criteria = Criteria.where("sheetId").is(sheetId);
        return query(Query.query(criteria));
    }
}
