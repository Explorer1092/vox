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

package com.voxlearning.utopia.service.zone.mdb;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentDDL;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.api.constant.Currency;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@DocumentConnection(configName = "utopia")
@DocumentTable(table = "MDB_CLAZZ_ZONE_PRODUCT")
@DocumentDDL(path = "ddl/mdb/MDB_CLAZZ_ZONE_PRODUCT.ddl")
public class MDBClazzZoneProduct implements Serializable {
    private static final long serialVersionUID = 1381343828304334730L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    @DocumentField("ID") private Long id;
    @DocumentField("NAME") private String name;
    @DocumentField("PRICE") private Integer price;
    @DocumentField("CURRENCY") private Currency currency;
    @DocumentField("SPECIES") private String species;
    @DocumentField("SUBSPECIES") private String subspecies;
    @DocumentField("PERIOD_OF_VALIDITY") private Long periodOfValidity;

    public ClazzZoneProduct transform() {
        ClazzZoneProduct t = new ClazzZoneProduct();
        t.setId(id);
        t.setName(name);
        t.setPrice(price);
        t.setCurrency(currency);
        t.setSpecies(species);
        t.setSubspecies(subspecies);
        t.setPeriodOfValidity(periodOfValidity);
        return t;
    }
}
