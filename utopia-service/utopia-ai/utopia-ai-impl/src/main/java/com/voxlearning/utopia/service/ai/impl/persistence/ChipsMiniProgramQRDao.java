package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.ChipsMiniProgramQR;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = ChipsMiniProgramQR.class)
public class ChipsMiniProgramQRDao extends AsyncStaticMongoPersistence<ChipsMiniProgramQR, String> {
    @Override
    protected void calculateCacheDimensions(ChipsMiniProgramQR document, Collection<String> dimensions) {

    }

    public Page<ChipsMiniProgramQR> loadExculedDisabledPageable(int page, int pageSize) {
        Criteria criteria = Criteria.where("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");
        PageRequest pageRequest  = new PageRequest(page - 1, pageSize);
        Query query = Query.query(criteria);
        return new PageImpl<>(query(query.with(pageRequest).with(sort)), pageRequest, count(query));
    }

    public void disabled(String id) {
        Criteria criteria = Criteria.where("_id").is(id).and("disabled").is(false);
        Update update = new Update();
        update.set("disabled", true);
        $executeUpdateMany(createMongoConnection(), criteria, update);
    }
}
