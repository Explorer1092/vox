package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class CrmAdvertiseRegionService {

    @Inject private RaikouSystem raikouSystem;

    // 获取区域树
    public List<Map<String, Object>> buildRegionTree(Collection<Integer> regions) {
        Map<String, Map<String, Object>> allRegionTree = buildAllRegionTree();
        if (CollectionUtils.isNotEmpty(regions)) {
            for (Integer regionCode : regions) {
                Map<String, Object> regionInfo = allRegionTree.get(String.valueOf(regionCode));
                if (regionInfo == null) continue;
                regionInfo.put("selected", Boolean.TRUE);
                allRegionTree.put(String.valueOf(regionCode), regionInfo);
            }
        }
        List<Map<String, Object>> retList = new ArrayList<>();

        Set<String> allKeySet = allRegionTree.keySet();
        for (String regionCode : allKeySet) {
            Map<String, Object> regionItem = allRegionTree.get(regionCode);
            if (regionItem.get("pcode") == null) {
                retList.add(regionItem);
            }
        }
        return retList;
    }

    // 这个方法在agentService里还有一个， 建议统一放到regionClient里， 这么写太重复了。
    private Map<String, Map<String, Object>> buildAllRegionTree() {
        Map<Integer, ExRegion> allRegions = raikouSystem.getRegionBuffer().loadAllRegions();
        List<Region> regions = new ArrayList<>(allRegions.values());
        Map<String, Map<String, Object>> retMap = new LinkedHashMap<>();
        for (Region region : regions) {
            Map<String, Object> regionItemMap = new HashMap<>();
            regionItemMap.put("title", region.getName());
            regionItemMap.put("key", String.valueOf(region.getCode()));
            if (region.getPcode() != 0) {
                regionItemMap.put("pcode", String.valueOf(region.getPcode()));
            }
            regionItemMap.put("children", new ArrayList());
            retMap.put(String.valueOf(region.getCode()), regionItemMap);
        }

        // 第二次循环，根据Id和ParentID构建父子关系
        for (Region region : regions) {
            Integer pcode = region.getPcode();
            if (pcode == 0) {
                continue;
            }
            Map<String, Object> parentObj = retMap.get(String.valueOf(pcode));
            Map<String, Object> childObj = retMap.get(String.valueOf(region.getCode()));

            // 如果父节点存在，将此结点加入到父结点的子节点中
            if (parentObj != null) {
                List children = (List) parentObj.get("children");
                if (!children.contains(childObj)) {
                    children.add(childObj);
                }
            }
        }

        // 加一个无地区的选择项
        Map<String, Object> regionItemMap = new HashMap<>();
        regionItemMap.put("title", "无地区");
        regionItemMap.put("key", "999999");  // 特殊地区
        retMap.put("999999", regionItemMap);

        return retMap;
    }
}
