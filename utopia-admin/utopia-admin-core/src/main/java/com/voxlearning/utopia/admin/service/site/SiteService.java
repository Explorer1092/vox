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

package com.voxlearning.utopia.admin.service.site;

import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Longlong Yu
 * @since 下午4:02,13-11-27.
 */
@Named
public class SiteService {

    @Inject private RaikouSystem raikouSystem;

    /**
     * 找出省、市编码对应的所有区编码
     */
    public List<Integer> getCountyCodeList(Integer parentCode) {

        ExRegion region = raikouSystem.loadRegion(parentCode);
        if (region == null) return Collections.emptyList();

        List<ExRegion> countyExRegionList = new ArrayList<>();
        if (RegionType.PROVINCE == region.fetchRegionType()) {
            List<ExRegion> cityExRegionList = raikouSystem.getRegionBuffer().loadChildRegions(region.getProvinceCode());

            for (ExRegion exRegion : cityExRegionList) {
                countyExRegionList.addAll(raikouSystem.getRegionBuffer().loadChildRegions(exRegion.getCityCode()));
            }
        } else if (RegionType.CITY == region.fetchRegionType()) {
            countyExRegionList.addAll(raikouSystem.getRegionBuffer().loadChildRegions(region.getCityCode()));
        } else {
            countyExRegionList.add(region);
        }

        List<Integer> ret = new ArrayList<>();
        for (ExRegion exRegion : countyExRegionList) {
            ret.add(exRegion.getCountyCode());
        }
        return ret;
    }


}
