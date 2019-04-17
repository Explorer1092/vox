package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkDict;

import javax.inject.Named;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/15
 */
@Named
public class HomeworkDictPersistence extends NoCacheStaticMySQLPersistence<HomeworkDict, String> {

    public List<HomeworkDict> fetchHomeworkDictList() {
        Query query = Query.query(new Criteria());
        return query(query);
    }

}
