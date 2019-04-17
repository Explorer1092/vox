/**
 * Author:   xianlong.zhang
 * Date:     2018/10/17 18:33
 * Description: 客服工单
 * History:
 */
package com.voxlearning.utopia.agent.dao.mongo.worksheet;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentArticleUser;
import com.voxlearning.utopia.agent.persist.entity.worksheel.WorkSheet;

import javax.inject.Named;
import java.util.List;

@Named
@CacheBean(type = WorkSheet.class)
public class WorkSheetDao extends StaticCacheDimensionDocumentMongoDao<WorkSheet, String> {

    public List<WorkSheet> findWorkSheetListByUserPhone(String phone){
        Criteria criteria = Criteria.where("user.phone").is(phone);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public WorkSheet findWorkSheetBySheetId(@CacheParameter(value = "sheetId") Long sheetId){
        Criteria criteria = Criteria.where("sheetId").is(sheetId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
}
