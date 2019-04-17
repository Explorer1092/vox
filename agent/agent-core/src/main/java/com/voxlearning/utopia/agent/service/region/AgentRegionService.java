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

package com.voxlearning.utopia.agent.service.region;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/9.
 */
@Named
public class AgentRegionService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private BaseOrgService baseOrgService;
    private Map<String, Map<String, Object>> allRegionTree;
    private Map<String, Set<Integer>> cityLevelRegions;
    private long expireTime = 0;
    private long cityLevelExpireTime = 0;

    public List<Map<String, Object>> loadUserRegionTree(AuthCurrentUser user, boolean withFullPath) {
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Map<String, Object>> allRegionTree = getAllRegionTreeCopy();

        if (user.isAdmin()) {
            // 管理员总是获取所有的Region
            Set<String> allKeySet = allRegionTree.keySet();
            for (String regionCode : allKeySet) {
                Map<String, Object> regionItem = allRegionTree.get(regionCode);
                if (regionItem.get("pcode") == null) {
                    retList.add(regionItem);
                }
            }
        } else {
            List<Integer> userRegionList = new ArrayList<>();
            List<AgentGroupRegion> groupRegionList = baseOrgService.getManagedRegionList(user.getUserId());
            if (CollectionUtils.isNotEmpty(groupRegionList)) {
                userRegionList = groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toList());
            }
            if (withFullPath) {
                Map<String, Map<String, Object>> regionBuffer = new HashMap<>();
                for (Integer regionCode : userRegionList) {
                    Map<String, Object> regionItem = allRegionTree.get(String.valueOf(regionCode));
                    regionBuffer.put(String.valueOf(regionCode), regionItem);

                    // 复杂了，得取父节点，去掉兄弟节点, 先恶心着，等有时间再优化
                    if (regionItem != null && regionItem.get("pcode") != null) {
                        String pcode = String.valueOf(regionItem.get("pcode"));
                        Map<String, Object> pregionItem1 = regionBuffer.get(pcode);
                        if (pregionItem1 == null) {  // 可能是市，可能是省
                            pregionItem1 = allRegionTree.get(pcode);
                            Map<String, Object> pregion1 = new HashMap<>();
                            pregion1.put("title", pregionItem1.get("title"));
                            pregion1.put("key", pregionItem1.get("key"));
                            pregion1.put("pcode", pregionItem1.get("pcode"));
                            List<Map<String, Object>> children = new ArrayList<>();
                            children.add(regionItem);
                            pregion1.put("children", children);
                            regionBuffer.put(pcode, pregion1);
                            regionItem = pregion1;
                        } else {
                            List<Map<String, Object>> children = (List<Map<String, Object>>) pregionItem1.get("children");
                            children.add(regionItem);
                            regionItem = pregionItem1;
                        }

                        if (regionItem.get("pcode") != null) {  // 如果还有省
                            pcode = String.valueOf(regionItem.get("pcode"));
                            Map<String, Object> pregionItem2 = regionBuffer.get(pcode);
                            if (pregionItem2 == null) {
                                pregionItem2 = allRegionTree.get(pcode);
                                Map<String, Object> pregion2 = new HashMap<>();
                                pregion2.put("title", pregionItem2.get("title"));
                                pregion2.put("key", pregionItem2.get("key"));
                                pregion2.put("pcode", pregionItem2.get("pcode"));
                                List<Map<String, Object>> children = new ArrayList<>();
                                children.add(regionItem);
                                pregion2.put("children", children);
                                regionBuffer.put(pcode, pregion2);
                                regionItem = pregion2;
                            } else {
                                List<Map<String, Object>> children = (List<Map<String, Object>>) pregionItem2.get("children");
                                children.add(pregionItem1);
                                regionItem = pregionItem2;
                            }
                        }
                    }

                    CollectionUtils.addNonNullElement(retList, regionItem);
                }
            } else {
                for (Integer regionCode : userRegionList) {
                    CollectionUtils.addNonNullElement(retList, allRegionTree.get(String.valueOf(regionCode)));
                }
            }
        }

        return retList;
    }

    public synchronized Map<String, Map<String, Object>> getAllRegionTreeCopy() {
        return buildAllRegionTree();
    }

    public synchronized Map<String, Map<String, Object>> getAllRegionTree() {
        if (allRegionTree == null || System.currentTimeMillis() > expireTime) {
            allRegionTree = buildAllRegionTree();
            expireTime = System.currentTimeMillis() + 60 * 60 * 1000;
        }

        return allRegionTree;
    }

    public String getRegionName(Integer regionCode) {
        Map<String, Map<String, Object>> regionTree = getAllRegionTree();
        if (regionTree.containsKey(String.valueOf(regionCode))) {
            return String.valueOf(regionTree.get(String.valueOf(regionCode)).get("title"));
        } else {
            return "未知";
        }
    }

    public List<String> getRegionNames(List<Integer> regionCodes) {
        List<String> retRegionNames = new ArrayList<>();
        for (Integer regionCode : regionCodes) {
            retRegionNames.add(getRegionName(regionCode));
        }
        return retRegionNames;
    }

    public String getProvinceName(Integer regionCode) {
        Map<String, Map<String, Object>> regionTree = getAllRegionTree();
        String pcode = String.valueOf(regionTree.get(String.valueOf(regionCode)).get("pcode"));
        if (regionTree.get(pcode).get("pcode") != null) {
            pcode = String.valueOf(regionTree.get(pcode).get("pcode"));
        }
        return String.valueOf(regionTree.get(pcode).get("title"));
    }

    public List<Integer> getChildRegionIds(Integer regionCode) {
        Map<String, Map<String, Object>> regionTree = getAllRegionTree();
        Map<String, Object> regionInfo = regionTree.get(String.valueOf(regionCode));
        if (regionInfo == null) {
            return Collections.emptyList();
        }

        List<Integer> retRegionIds = new ArrayList<>();
        List<Map<String, Object>> subRegionList = (List<Map<String, Object>>) regionInfo.get("children");
        if (subRegionList != null && subRegionList.size() > 0) {
            for (Map<String, Object> subRegion : subRegionList) {
                retRegionIds.add(Integer.parseInt(String.valueOf(subRegion.get("key"))));
            }
        }
        return retRegionIds;
    }

    // FIXME: 这个方法有点重复，region tree 都是构建好的了
    public Map<String, Map<String, Object>> buildAllRegionTree() {
        Map<Integer, ExRegion> allRegions = raikouSystem.getRegionBuffer().loadAllRegions();
        List<Region> regions = new ArrayList<>(allRegions.values());

        Map<String, Map<String, Object>> retMap = new HashMap<>();
        for (Region region : regions) {
            // 转换成要使用的HashMap对象
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

        // 添加区域未知
        String unknowRegionCode = "0";
        Map<String, Object> unknownRegionMap = new HashMap<>();
        unknownRegionMap.put("title", "区域未知");
        unknownRegionMap.put("key", unknowRegionCode);
        unknownRegionMap.put("children", new ArrayList());

        retMap.put(unknowRegionCode, unknownRegionMap);

        return retMap;
    }

    public List<Integer> getCountyCodes(Collection<Integer> regionList) {
        List<Integer> retCountyList = new ArrayList<>();

        if (CollectionUtils.isEmpty(regionList)) {
            return retCountyList;
        }

        for (Integer regionCode : regionList) {
            retCountyList.addAll(getCountyCodes(regionCode));
        }

        return retCountyList;
    }

    public List<Integer> getCountyCodes(Integer regionCode) {
        List<Integer> retCountyList = new ArrayList<>();

        if (regionCode == null) {
            return retCountyList;
        }
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion == null) {
            return retCountyList;
        }

        if (exRegion.fetchRegionType() == RegionType.COUNTY) {
            retCountyList.add(exRegion.getId());
        } else if (exRegion.fetchRegionType() == RegionType.CITY) {
            exRegion.getChildren().forEach(p -> retCountyList.add(p.getId()));
        } else if (exRegion.fetchRegionType() == RegionType.PROVINCE) {
            exRegion.getChildren().forEach(p -> {
                if (CollectionUtils.isNotEmpty(p.getChildren())) {
                    p.getChildren().forEach(k -> retCountyList.add(k.getId()));
                }
            });
        }
        return retCountyList;
    }

    public void markSelectedRegion(List<Map<String, Object>> orgTree, List<String> regionList) {
        if (orgTree == null || orgTree.size() == 0 || regionList == null || regionList.size() == 0) {
            return;
        }

        for (Map<String, Object> regionItem : orgTree) {
            markSelectedRegion(regionItem, regionList);
        }
    }

    private void markSelectedRegion(Map<String, Object> regionItem, List<String> regionList) {
        String regionCode = (String) regionItem.get("key");
        if (regionList.contains(regionCode)) {
            regionItem.put("selected", Boolean.TRUE);
        }

        List children = (List) regionItem.get("children");
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                Map<String, Object> subRegionItem = (Map<String, Object>) children.get(i);
                markSelectedRegion(subRegionItem, regionList);
            }
        }
    }

    public Map<Object, Object> buildUserRegionMapTree(AuthCurrentUser user) {
        if (user == null) {
            return null;
        }
        List<Map<String, Object>> tops = loadUserRegionTree(user, true);
        if (tops == null || tops.isEmpty()) {
            return null;
        }
        return buildRegionTree(tops);
    }

    public Map<Object, Object> buildAllTopRegionTree() {
        Map<String, Map<String, Object>> allRegionTree = getAllRegionTreeCopy();
        List<Map<String, Object>> topList = new ArrayList<>();
        // 管理员总是获取所有的Region
        Set<String> allKeySet = allRegionTree.keySet();
        for (String regionCode : allKeySet) {
            Map<String, Object> regionItem = allRegionTree.get(regionCode);
            if (regionItem.get("pcode") == null) {
                topList.add(regionItem);
            }
        }
        return buildRegionTree(topList);
    }

    public Map<Object, Object> buildRegionTree(List<Map<String, Object>> tops) {
        if (tops == null || tops.isEmpty()) {
            return null;
        }
        Map<Object, Object> topRegions = new LinkedHashMap<>();
        for (Map<String, Object> top : tops) {
            Map<Object, Object> topRegion = new HashMap<>();
            topRegion.put("code", top.get("key"));
            topRegion.put("name", top.get("title"));
            Collection<Map<String, Object>> middles = (Collection<Map<String, Object>>) top.get("children");
            if (middles != null) {
                Map<Object, Object> middleRegions = new LinkedHashMap<>(middles.size());
                for (Map<String, Object> middle : middles) {
                    Map<Object, Object> middleRegion = new HashMap<>();
                    middleRegion.put("code", middle.get("key"));
                    middleRegion.put("name", middle.get("title"));
                    Collection<Map<String, Object>> bottoms = (Collection<Map<String, Object>>) middle.get("children");
                    if (bottoms != null) {
                        Map<Object, Object> bottomRegions = new LinkedHashMap<>(bottoms.size());
                        for (Map<String, Object> bottom : bottoms) {
                            Map<Object, Object> bottomRegion = new HashMap<>();
                            bottomRegion.put("code", bottom.get("key"));
                            bottomRegion.put("name", bottom.get("title"));
                            bottomRegions.put(bottomRegion.get("code"), bottomRegion);
                        }
                        middleRegion.put("children", bottomRegions);
                    }
                    middleRegions.put(middleRegion.get("code"), middleRegion);
                }
                topRegion.put("children", middleRegions);
            }
            topRegions.put(topRegion.get("code"), topRegion);
        }
        final String unknowRegionCode = "0";
        topRegions.remove(unknowRegionCode);
        return topRegions;
    }


}
