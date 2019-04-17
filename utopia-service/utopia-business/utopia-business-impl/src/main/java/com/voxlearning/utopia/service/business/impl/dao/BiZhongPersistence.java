package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.campaign.BiZhong;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Summer on 2016/12/19.
 */
@Named
public class BiZhongPersistence extends AlpsStaticJdbcDao<BiZhong, Long> {

    @Override
    protected void calculateCacheDimensions(BiZhong document, Collection<String> dimensions) {
    }

    public List<BiZhong> findAll() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public int disabledByWorkNo(Long workNo) {
        Criteria criteria = Criteria.where("WORK_NO").is(workNo);
        Update update = Update.update("DISABLED", true);
        return (int) $update(update, criteria);
    }
}
