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

package com.voxlearning.ucenter.controller;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * @author changyuan.liu
 * @since 2015.12.16
 */
@Controller
@RequestMapping("/map")
public class MapController extends AbstractWebController {

    @Inject private RaikouSystem raikouSystem;

    @RequestMapping(value = "nodes.vpage", method = RequestMethod.GET)
    @ResponseBody
    public List nodes() {
        int pcode = SafeConverter.toInt(getRequestParameter("id", "0"));
        return this.getNodes(pcode);
    }

    @SuppressWarnings("unchecked")
    private List getNodes(int pcode) {
        List<ExRegion> regionAll = new ArrayList<>();
        //处理直辖市，如果是直辖市，合并市区和县，直接显示下一级，直辖市 (北京、天津、上海、重庆)
        ArrayList<Integer> directCityList = new ArrayList<>(Arrays.asList(110000, 120000, 310000, 500000));
        if (directCityList.contains(pcode)) {
            List<ExRegion> regions = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
            for (Region vo : regions) {
                List<ExRegion> region = raikouSystem.getRegionBuffer().loadChildRegions(vo.getCode());
                regionAll.addAll(region);
            }
        } else {
            regionAll = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
        }

        List nodes = new ArrayList<>();
        List nodeSXQ = new ArrayList<>();
        for (ExRegion region : regionAll) {
            // FIXME: 通过区编码查学校总有null出现，在这里加点日志看几天，确保后端数据没有问题
            try {
                Assert.notNull(region.getCode());
            } catch (Exception ex) {
                logger.error("REGION {} CODE IS NULL! PARENT CODE IS {}", region.getName(), pcode, ex);
                continue;
            }

            if (region.getName().equals("市辖区")) {
                nodeSXQ.add(MiscUtils.map()
                        .add("id", region.getCode())
                        .add("text", region.getName())
                        .add("state", region.fetchRegionType() != RegionType.COUNTY ? "closed" : "open")
                        .add("checked", false)
                );
            } else {
                nodes.add(MiscUtils.map()
                        .add("id", region.getCode())
                        .add("text", region.getName())
                        .add("state", region.fetchRegionType() != RegionType.COUNTY ? "closed" : "open")
                        .add("checked", false)
                );
            }
        }
        nodes.addAll(nodeSXQ);
        Collections.sort(nodes, (o1, o2) -> {
            int rc1 = ConversionUtils.toInt(((Map) o1).get("id"));
            int rc2 = ConversionUtils.toInt(((Map) o2).get("id"));
            return Integer.compare(rc1, rc2);
        });

        // FIXME temp fix remove duplicated region
        List retNodes = new ArrayList<>();
        Set<Integer> addedCodes = new HashSet<>();
        for (int i = 0; i < nodes.size(); i++) {
            Map<String, Object> item = (Map<String, Object>) nodes.get(i);
            Integer regionCode = SafeConverter.toInt(item.get("id"));
            if (!addedCodes.contains(regionCode)) {
                retNodes.add(item);
                addedCodes.add(regionCode);
            }
        }

        return retNodes;
    }
}
