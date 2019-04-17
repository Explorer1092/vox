package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceTemplate;
import com.voxlearning.utopia.service.ai.entity.ChipsOtherServiceUserTemplate;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/1/11
 */
@Named
@CacheBean(type = ChipsOtherServiceUserTemplate.class)
public class ChipsOtherServiceUserTemplateDao extends AsyncStaticMongoPersistence<ChipsOtherServiceUserTemplate, String> {

    @Override
    protected void calculateCacheDimensions(ChipsOtherServiceUserTemplate chipsOtherServiceUserTemplate, Collection<String> collection) {

    }

    public List<ChipsOtherServiceUserTemplate> query(Collection<Long> userIdList, String seviceType, String templateId,
                                                     Date updateBeginDate) {
        Criteria criteria = Criteria.where("serviceType").is(seviceType);
//        if (templateId != null) {
//            criteria.and("templateId").is(templateId);
//        }
        if (updateBeginDate != null) {
            criteria.and("updateDate").lte(updateBeginDate);
        }
        criteria.and("userId").in(userIdList);
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");

        return query(Query.query(criteria).with(sort));
    }
}
