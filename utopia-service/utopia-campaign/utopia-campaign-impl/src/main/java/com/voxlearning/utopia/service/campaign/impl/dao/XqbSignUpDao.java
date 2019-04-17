package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.utopia.entity.activity.XqbSignUp;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Named
public class XqbSignUpDao extends AlpsStaticJdbcDao<XqbSignUp,Long> {

    @Override
    protected void calculateCacheDimensions(XqbSignUp document, Collection<String> dimensions) {

    }

    public List<XqbSignUp> loadUnderLineInPage(Date endDate, Integer pageSize, Integer pageNum){
        if(pageSize == null || pageNum == null || endDate == null)
            return Collections.emptyList();

        int actualPageNum = pageNum - 1;
        if(actualPageNum < 0 || pageSize <= 0 )
            return Collections.emptyList();

        Criteria criteria = Criteria.where("ONLINE").is(false).and("CREATE_DATETIME").lte(endDate);
        PageRequest pageRequest = new PageRequest(actualPageNum,pageSize);
        return query(Query.query(criteria).with(pageRequest));
    }
}
