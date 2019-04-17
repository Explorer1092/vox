package com.voxlearning.utopia.service.vendor.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.vendor.api.entity.VendorUserRef;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class VendorUserRefPersistence extends StaticMySQLPersistence<VendorUserRef, Long> {

    @Override
    protected void calculateCacheDimensions(VendorUserRef document, Collection<String> dimensions) {

    }

    public Page<VendorUserRef> countEffectiveUser(String appkey, Date startDate) {


        Map<String, Long> resultMap = new HashMap<>();
        Criteria criteria = Criteria.where("product_end").gte(startDate)
                .and("disabled").is(Boolean.FALSE);


        Query query = Query.query(criteria);
        List<VendorUserRef> result = query(query);

        Map<String, List<VendorUserRef>> collect = result.stream().collect(Collectors.groupingBy(VendorUserRef::getAppKey));

        collect.forEach((k, v) -> {
            resultMap.put(k, Long.valueOf(v.size()));
        });


        return null;
    }

    public boolean disableByChildId(Long childId) {

        Criteria criteria = Criteria.where("USER_ID").is(childId).and("DISABLED").is(false);
        Update update = Update.update("DISABLED", true);

        return $update(update, criteria) > 0;
    }

    public List<VendorUserRef> loadVendorUserRefList(String appkey, Long minUserId, int limit) {

        Criteria criteria = Criteria.where("app_key").is(appkey)
                .and("disabled").is(Boolean.FALSE);

        if (minUserId != null){
            criteria.and("user_id").gt(minUserId);
        }

        Sort sort = new Sort(Sort.Direction.ASC, "user_id");

        Query query = Query.query(criteria).with(sort).limit(limit);

        return query(query);
    }
}
