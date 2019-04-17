/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.derby.persistence.DerbyPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.zone.mdb.MDBClazzZoneProduct;

import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.zone.impl.persistence.MDBClazzZoneProductPersistence")
public class MDBClazzZoneProductPersistence extends DerbyPersistence<MDBClazzZoneProduct, Long> {

    public List<MDBClazzZoneProduct> findBySpecies(String species) {
        Criteria criteria = Criteria.where("SPECIES").is(species);
        return query(Query.query(criteria).with(new Sort(Sort.Direction.ASC, "ID")));
    }
}
