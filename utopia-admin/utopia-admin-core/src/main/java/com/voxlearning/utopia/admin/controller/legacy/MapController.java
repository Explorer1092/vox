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

package com.voxlearning.utopia.admin.controller.legacy;


import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.annotation.AdminSystemPath;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/legacy/map")
@SuppressWarnings("deprecation")
public class MapController extends AbstractAdminLegacyController {

    @Inject private RaikouSystem raikouSystem;

    @AdminSystemPath("/legacy/map/nodes-id")
    @RequestMapping(value = "nodes-{id}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public List nodes2(@PathVariable(value = "id") Integer pcode) {
        return this.nodes(pcode);
    }

    @RequestMapping(value = "nodes.vpage", method = RequestMethod.GET)
    @ResponseBody
    public List nodes(@RequestParam(value = "id", required = false) Integer pcode) {
        pcode = (pcode == null) ? 0 : pcode;
        List<ExRegion> regions = raikouSystem.getRegionBuffer().loadChildRegions(pcode);

        List<Map<String, Object>> nodes = new ArrayList<>();
        for (ExRegion region : regions) {
            nodes.add(MiscUtils.<String, Object>map()
                    .add("id", region.getCode())
                    .add("text", region.getName())
                    .add("state", region.fetchRegionType() != RegionType.COUNTY ? "closed" : "open")
                    .add("checked", false)
            );
        }
        return nodes;
    }

    @AdminSystemPath("/legacy/map/region-id")
    @RequestMapping(value = "region-{id}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map region2(@PathVariable(value = "id") Integer pcode) {
        return this.region(pcode);
    }

    @RequestMapping(value = "region.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map region(@RequestParam(value = "id", required = false) Integer pcode) {
        pcode = (pcode == null) ? 0 : pcode;
        ExRegion region = raikouSystem.loadRegion(pcode);
        if (pcode == 0 || region == null) {
            return MiscUtils.<String, Object>map()
                    .add("acode", 0)
                    .add("ccode", 0)
                    .add("pcode", 0)
                    .add("type", 0);
        } else return MiscUtils.<String, Object>map()
                .add("acode", region.getCountyCode())
                .add("ccode", region.getCityCode())
                .add("pcode", region.getProvinceCode())
                .add("type", region.fetchRegionType().getType());
    }
}
