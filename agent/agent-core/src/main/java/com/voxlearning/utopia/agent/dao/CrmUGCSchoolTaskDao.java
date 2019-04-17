package com.voxlearning.utopia.agent.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchoolTask;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author Jia HuanYin
 * @since 2016/1/7
 */
@Named("agent.CrmUGCSchoolTaskDao")
public class CrmUGCSchoolTaskDao extends StaticMongoDao<CrmUGCSchoolTask, String> {

    @Override
    protected void calculateCacheDimensions(CrmUGCSchoolTask source, Collection<String> dimensions) {
    }
}
