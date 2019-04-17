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

package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.api.constant.Currency;
import com.voxlearning.utopia.service.zone.api.constant.ClazzZoneProductSpecies;
import com.voxlearning.utopia.service.zone.api.constant.ClazzZoneProductSubspecies;
import com.voxlearning.utopia.service.zone.mdb.MDBClazzZoneProduct;
import lombok.Getter;
import lombok.Setter;

/**
 * Clazz zone product definition.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @serial
 * @since 14-5-12
 */
@Getter
@Setter
@DocumentConnection(configName = "main")
@DocumentTable(table = "VOX_CLAZZ_ZONE_PRODUCT")
public class ClazzZoneProduct implements CacheDimensionDocument {
    private static final long serialVersionUID = 3135668049406212427L;

    public static final Long CLAZZ_ZONE_DEFAULT_BUBBLE = 0L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC) private Long id;  // 产品Id
    @DocumentField("NAME") private String name;                                     // 产品名称
    @DocumentField("PRICE") private Integer price;                                  // 产品价格
    @DocumentField("CURRENCY") private Currency currency;                           // 产品使用货币
    @DocumentField("SPECIES") private String species;                               // 产品种类
    @DocumentField("SUBSPECIES") private String subspecies;                         // 产品亚种
    @DocumentField("PERIOD_OF_VALIDITY") private Long periodOfValidity;             // 产品有效期，精确到天

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    public ClazzZoneProductSpecies fetchSpecies() {
        try {
            return ClazzZoneProductSpecies.valueOf(species);
        } catch (Exception ex) {
            return null;
        }
    }

    public ClazzZoneProductSubspecies fetchSubspecies() {
        try {
            return ClazzZoneProductSubspecies.valueOf(subspecies);
        } catch (Exception ex) {
            return null;
        }
    }

    public MDBClazzZoneProduct transform() {
        MDBClazzZoneProduct t = new MDBClazzZoneProduct();
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
