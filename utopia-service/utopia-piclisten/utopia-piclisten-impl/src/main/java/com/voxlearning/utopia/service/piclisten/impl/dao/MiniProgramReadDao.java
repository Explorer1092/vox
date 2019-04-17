package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.piclisten.api.entity.MiniProgramRead;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


/**
 * @author RA
 */

@Named
public class MiniProgramReadDao extends AlpsStaticMongoDao<MiniProgramRead, String> {


    @Override
    protected void calculateCacheDimensions(MiniProgramRead document, Collection<String> dimensions) {
        dimensions.add(MiniProgramRead.ck_id(document.getId()));
        dimensions.add(MiniProgramRead.ck_uid(document.getUid()));
    }


    public MiniProgramRead loadByUid(Long uid) {
        Criteria criteria = Criteria.where("uid").is(uid);
        Query query = Query.query(criteria);

        List<MiniProgramRead> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


}
