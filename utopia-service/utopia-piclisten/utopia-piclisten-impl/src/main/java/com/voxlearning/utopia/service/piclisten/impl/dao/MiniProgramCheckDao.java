package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.piclisten.api.entity.MiniProgramCheck;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


/**
 * @author RA
 */

@Named
public class MiniProgramCheckDao extends AlpsStaticMongoDao<MiniProgramCheck, String> {


    @Override
    protected void calculateCacheDimensions(MiniProgramCheck document, Collection<String> dimensions) {
        dimensions.add(MiniProgramCheck.ck_id(document.getId()));
        dimensions.add(MiniProgramCheck.ck_uid(document.getUid()));
    }


    public MiniProgramCheck loadByUid(Long uid) {
        Criteria criteria = Criteria.where("uid").is(uid);
        Sort sort = new Sort(Sort.Direction.DESC, "_id");
        Query query = Query.query(criteria).with(sort).limit(1);

        List<MiniProgramCheck> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


}
