package com.voxlearning.utopia.service.vendor.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.derby.persistence.DerbyPersistence;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorAppsResgRef;

import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.vendor.impl.persistence.MDBVendorAppsResgRefPersistence")
public class MDBVendorAppsResgRefPersistence extends DerbyPersistence<MDBVendorAppsResgRef, Long> {

    public List<MDBVendorAppsResgRef> findByAppKey(String appKey) {
        Criteria criteria = Criteria.where("APP_KEY").is(appKey);
        return query(Query.query(criteria));
    }

    public void purge() {
        $remove(new Criteria());
    }
}
