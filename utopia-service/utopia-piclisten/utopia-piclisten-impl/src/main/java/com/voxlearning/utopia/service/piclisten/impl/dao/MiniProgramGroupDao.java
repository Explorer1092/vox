package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.piclisten.api.entity.MiniProgramGroup;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


/**
 * @author RA
 */

@Named
public class MiniProgramGroupDao extends AlpsStaticMongoDao<MiniProgramGroup, String> {


    @Override
    protected void calculateCacheDimensions(MiniProgramGroup document, Collection<String> dimensions) {
        dimensions.add(MiniProgramGroup.ck_id(document.getId()));
        dimensions.add(MiniProgramGroup.ck_uid(document.getUid()));
        dimensions.add(MiniProgramGroup.ck_gid(document.getGid()));
    }


    public List<MiniProgramGroup> loadByGid(String gid) {
        Criteria criteria = Criteria.where("gid").is(gid);
        Query query = Query.query(criteria);
        return  query(query);

    }

    public boolean hasBind(Long uid,Long pid, String gid) {
        Criteria criteria = Criteria.where("uid").is(uid).and("pid").is(pid).and("gid").is(gid);
        Query query = Query.query(criteria);
        if (query(query).size() > 0) {
            return true;
        }
        return false;
    }


}
