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

package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.region.api.constant.RegionConstants;
import com.voxlearning.utopia.service.region.api.entities.RegionTag;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * TODO: 实现得够烂，完全没有代码的美感。
 * Created by Alex on 14-9-3.
 */
@Named
public class RegionConfigService extends AbstractAgentService {

    // 增加付费黑名单地区以及非教育产品黑订单地区 By Wyc 2016-04-08
    // 删除非教育产品黑订单地区tag增加家长端黑名单　modify by peng.zhang.a　2016-10-10
    public static final String PAYMENT_BLACK_LIST_REGIONS = RegionConstants.TAG_PAYMENT_BLACKLIST_REGIONS;
    public static final String PARENT_PAYMENT_BLACKLIST_REGIONS = RegionConstants.TAG_PARENT_PAYMENT_BLACKLIST_REGIONS;

    // 增加展示腾讯广告联盟广告地区配置 By Wyc 2016-07-29
    public static final String TENCENT_ADVERTISEMENT_REGIONS = RegionConstants.TAG_TENCENT_ADVERTISEMENT_REGIONS;
    public static final String TENCENT_ADVERTISEMENT_NEWS_REGIONS = RegionConstants.TAG_TENCENT_ADVERTISEMENT_NEWS_REGIONS;

    // 增值产品灰度地区配置
    private static final Map<String, String> grayRegionProduct = OrderProductServiceType.getAllGrayRegionProducts();

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private AgentRegionService agentRegionService;

    public List<Map<String, Object>> loadRegionProductConfig(String product, AuthCurrentUser user) {
        Map<String, Map<String, Object>> allRegionTree = agentRegionService.getAllRegionTreeCopy();
        allRegionTree.remove("0"); // 去除区域未知

        Collection<Integer> cardRegions = raikouSystem.getRegionBuffer().findByTag(product);
        for (Integer regionCode : cardRegions) {
            Map<String, Object> regionInfo = allRegionTree.get(String.valueOf(regionCode));
            if (regionInfo == null) {
                // 这种区域找不到了，一定是后台给区域删掉了，这里也干掉吧
                raikouSystem.getRegionService()
                        .detachRegionTag(Collections.singleton(regionCode), product);
                continue;
            }
            regionInfo.put("selected", Boolean.TRUE);
            allRegionTree.put(String.valueOf(regionCode), regionInfo);
        }

        List<Map<String, Object>> retList = new ArrayList<>();

        // 此处不做权限的判断，而通过功能权限来控制是否可以访问本节点 By Wyc 2016-05-09
        Set<String> allKeySet = allRegionTree.keySet();
        for (String regionCode : allKeySet) {
            Map<String, Object> regionItem = allRegionTree.get(regionCode);
            if (regionItem.get("pcode") == null) {
                retList.add(regionItem);
            }
        }

        return retList;
    }

    public MapMessage saveRegionConfig(String product, String regionList, AuthCurrentUser user) {
        // 自动加入Tag数据
        checkTagAvailable();

        // 根据用户管辖区域的不同来确定要取消设置的区域列表和要设置的区域列表
        List<Integer> untagedRegions = new ArrayList<>();
        List<Integer> tagedRegions = new ArrayList<>();

        String[] regionCodeList = regionList.split(",");

        Collection<Integer> alreadyTaggedRegions = raikouSystem.getRegionBuffer().findByTag(product);

        for (Integer taggedRegion : alreadyTaggedRegions) {
            String strRegionCode = String.valueOf(taggedRegion);
            if (!regionList.contains(strRegionCode)) {
                untagedRegions.add(taggedRegion);
            }
        }

        for (String regionCode : regionCodeList) {
            if (StringUtils.isEmpty(regionCode)) {
                continue;
            }

            Integer regionCodeInt = Integer.parseInt(regionCode);
            tagedRegions.add(regionCodeInt);
        }

        if (untagedRegions.size() > 0) {
            Set<Integer> tobeUntagRegions = new HashSet<>();
            tobeUntagRegions.addAll(untagedRegions);

            raikouSystem.getRegionService()
                    .detachRegionTag(tobeUntagRegions, product);
        }
        if (tagedRegions.size() > 0) {
            raikouSystem.getRegionService()
                    .attachRegionTag(tagedRegions, product);
        }

        return MapMessage.successMessage("区域设置保存成功!");
    }

    private RegionTag loadRegionTag(String id) {
        return raikouSystem.getRegionLoader()
                .__db_loadRegionTagsIncludeDisabled()
                .asMap(RegionTag::getId)
                .get(id);
    }

    private void checkTagAvailable() {

        // 增值产品灰度地区配置
        grayRegionProduct.forEach((productName, description) -> {
            String grayRegionKey = productName + RegionConstants.TAG_GRAY_REGION_SUFFIX;
            if (loadRegionTag(grayRegionKey) == null) {
                raikouSystem.getRegionService().createRegionTag(grayRegionKey, description);
            }
        });

        // 增加付费黑名单地区以及非教育产品黑订单地区 By Wyc 2016-04-08
        if (loadRegionTag(PAYMENT_BLACK_LIST_REGIONS) == null) {
            raikouSystem.getRegionService().createRegionTag(PAYMENT_BLACK_LIST_REGIONS, "学生付费黑名单地区");
        }

        // 删除非教育产品黑订单地区tag增加家长端黑名单　modify by peng.zhang.a　2016-10-10
        if (loadRegionTag(PARENT_PAYMENT_BLACKLIST_REGIONS) == null) {
            raikouSystem.getRegionService().createRegionTag(PARENT_PAYMENT_BLACKLIST_REGIONS, "家长付费黑名单地区");
        }

        // 增加展示腾讯广告联盟广告地区配置 By Wyc 2016-07-29
        if (loadRegionTag(TENCENT_ADVERTISEMENT_REGIONS) == null) {
            raikouSystem.getRegionService().createRegionTag(TENCENT_ADVERTISEMENT_REGIONS, "展示腾讯广告联盟闪屏广告地区");
        }
        if (loadRegionTag(TENCENT_ADVERTISEMENT_NEWS_REGIONS) == null) {
            raikouSystem.getRegionService().createRegionTag(TENCENT_ADVERTISEMENT_NEWS_REGIONS, "展示腾讯广告联盟资讯广告地区");
        }

        // 城市级别Tag
        for (AgentCityLevelType level : AgentCityLevelType.values()) {
            if (loadRegionTag(level.name()) == null) {
                raikouSystem.getRegionService().createRegionTag(level.name(), level.value);
            }
        }
    }

}
