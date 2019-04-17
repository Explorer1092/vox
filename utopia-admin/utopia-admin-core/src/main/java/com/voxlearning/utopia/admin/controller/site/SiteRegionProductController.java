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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.RegionTag;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Longlong Yu
 * @since 下午9:44,13-11-26.
 */
@Controller
@RequestMapping("/site/regionproduct")
public class SiteRegionProductController extends SiteAbstractController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolServiceClient schoolServiceClient;

    /**
     * 增加产品区域
     */
    @RequestMapping(value = "regionproducthomepage.vpage", method = RequestMethod.GET)
    public String regionProductHomepage(Model model) {
        model.addAttribute("productTypeList", getAvailableProductTypes());
        return "site/regionproduct/regionproducthomepage";
    }

    @RequestMapping(value = "regionproducthomepage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage regionProductHomepagePost() {
        String productRegionCodeSetStr = getRequestParameter("productRegionCode", "");
        String productType = getRequestParameter("productType", "");

        RegionTag rt = raikouSystem.getRegionLoader()
                .__db_loadRegionTagsIncludeDisabled()
                .asMap(RegionTag::getId)
                .get(productType);
        if (rt == null)
            return MapMessage.errorMessage("无效的产品类型");

        Set<Integer> productRegionCodeSet = new HashSet<>();
        try {
            String[] productRegionCodeStrList = productRegionCodeSetStr.split("[,，\\s]+");
            for (String it : productRegionCodeStrList) {
                if (StringUtils.isNotBlank(it)) {
                    productRegionCodeSet.addAll(siteService.getCountyCodeList(Integer.valueOf(it)));
                }
            }
        } catch (Exception ignored) {
            return MapMessage.errorMessage("存在不符合规范的区域编码");
        }

        if (productRegionCodeSet.size() == 0)
            return MapMessage.errorMessage("不存在有效的区域编码");

        if (raikouSystem.getRegionService().attachRegionTag(productRegionCodeSet, productType).isSuccess()) {
            addAdminLog("site-regionproduct-" + getCurrentAdminUser().getAdminUserName() + "增加产品开放区域", "", ""
                    , "产品类型： " + productType + ", 区域编码： " + StringUtils.join(productRegionCodeSet, ','));
            // 对于产品黑名单区域，关闭该区域中的所有学校的payOpen
            if (StringUtils.equals(productType, "AfentiBlackListRegions")) {
                AlpsFutureBuilder.<Long, Boolean>newBuilder()
                        .ids(raikouSystem.querySchoolLocations(productRegionCodeSet)
                                .transform()
                                .asList()
                                .stream()
                                .filter(t -> Boolean.TRUE.equals(t.getPayOpen()))
                                .map(School::getId)
                                .collect(Collectors.toSet()))
                        .generator(schoolServiceClient.getSchoolService()::disableSchoolPayOpen)
                        .buildList()
                        .awaitUninterruptibly();
            }
            return MapMessage.successMessage("增加产品开放区域成功");
        } else {
            return MapMessage.errorMessage("增加产品开放区域失败");
        }
    }

    /**
     * 查询区域产品
     */
    @RequestMapping(value = "regionproductlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String regionProductList(Model model) {

        model.addAttribute("productTypeList", getAvailableProductTypes());
        if (isRequestGet())
            return "site/regionproduct/regionproductlist";

        String regionTag = getRequestParameter("regionTag", "");
        int regionCode = getRequestInt("regionCode", -1);
        Map<String, Object> conditionMap = new HashMap<>();

        List<ExRegion> exRegionList = new ArrayList<>();

        if (regionCode >= 0) {
            conditionMap.put("regionCode", regionCode);

            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            if (RegionType.COUNTY == exRegion.fetchRegionType())
                exRegionList.add(exRegion);
            else
                exRegionList = raikouSystem.getRegionBuffer().loadChildRegions(regionCode);

            List<ExRegion> tempList = new ArrayList<>();
            for (ExRegion it : exRegionList) {
                if (RegionType.COUNTY == it.fetchRegionType())
                    tempList.add(it);
                else
                    tempList.addAll(raikouSystem.getRegionBuffer().loadChildRegions(it.getCityCode()));
            }

            exRegionList = new ArrayList<>();
            for (ExRegion it : tempList) {
                if (RegionType.COUNTY == it.fetchRegionType()) {
                    exRegionList.add(it);
                }
            }

            if (StringUtils.isNotBlank(regionTag)) {
                conditionMap.put("regionTag", regionTag);
                for (int i = 0; i < exRegionList.size(); i++) {
                    ExRegion it = exRegionList.get(i);
                    // it包含regionTag，下一个
                    if (it.containsTag(regionTag)) continue;
                    // 从RegionList中移除(i-- : 指针减一)，并循环下一个
                    exRegionList.remove(i--);
                }
            }

        } else if (StringUtils.isNotBlank(regionTag)) {
            conditionMap.put("regionTag", regionTag);
            Set<Integer> regionCodes = raikouSystem.getRegionBuffer().findByTag(regionTag);
            Map<Integer, ExRegion> regionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
            exRegionList = new LinkedList<>(regionMap.values());
        }

        if (exRegionList != null) {
            Collections.sort(exRegionList, new Comparator<ExRegion>() {
                @Override
                public int compare(ExRegion former, ExRegion later) {
                    if (former.getProvinceCode() != later.getProvinceCode())
                        return (former.getProvinceCode() < later.getProvinceCode()) ? -1 : 1;
                    else if (former.getCityCode() != later.getCityCode())
                        return (former.getCityCode() < later.getCityCode()) ? -1 : 1;
                    else {
                        if (former.getCountyCode() == later.getCountyCode()) return 0;
                        return (former.getCountyCode() < later.getCountyCode()) ? -1 : 1;
                    }
                }
            });
        }

        model.addAttribute("exRegionList", exRegionList);
        model.addAttribute("conditionMap", conditionMap);
        return "site/regionproduct/regionproductlist";
    }

    /**
     * 删除区域产品
     */
    @RequestMapping(value = "deleteregionproduct.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteRegionProduct() {

        int regionCode = getRequestInt("regionCode", -1);
        String regionTag = getRequestParameter("regionTag", "");

        if (regionCode < 0 || StringUtils.isBlank(regionTag))
            return MapMessage.errorMessage("删除失败");

        if (raikouSystem.getRegionService().detachRegionTag(Collections.singleton(regionCode), regionTag).isSuccess()) {
            addAdminLog("site-regionproduct-" + getCurrentAdminUser().getAdminUserName() + "删除产品开放区域", "", ""
                    , "产品类型： " + regionTag + "， 区域编码： " + regionCode);
            return MapMessage.successMessage("删除成功");
        } else {
            return MapMessage.errorMessage("删除失败");
        }
    }

    /**
     * 增加产品tag
     */
    @RequestMapping(value = "createregionproducttag.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String addRegionProductTag(Model model) {

        List<String> availableProductTypes = getAvailableProductTypes();
        model.addAttribute("availableProductTypes", availableProductTypes);
        if (isRequestGet())
            return "site/regionproduct/createregionproducttag";

        String productTag = getRequestParameter("productTag", "");
        String description = getRequestParameter("description", "");
        if (StringUtils.isBlank(productTag)) {
            getAlertMessageManager().addMessageError("产品类型名字不能为空");
            return "site/regionproduct/createregionproducttag";
        }

        if (raikouSystem.getRegionService().createRegionTag(productTag, description).isSuccess()) {
            availableProductTypes.add(productTag);
            addAdminLog("site-regionproduct-" + getCurrentAdminUser().getAdminUserName() + "增加产品类型： " + productTag
                    , "", "", "产品类型描述: " + description);
            getAlertMessageManager().addMessageSuccess("增加产品类型成功");
        } else {
            model.addAttribute("productTag", productTag);
            model.addAttribute("description", description);
            getAlertMessageManager().addMessageError("增加产品类型失败");
        }
        return "site/regionproduct/createregionproducttag";
    }

    /**
     * 删除产品tag
     */
    @RequestMapping(value = "deleteregionproducttag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteRegionProductTag() {

        String productTag = getRequestParameter("productTag", "");
        if (StringUtils.isBlank(productTag))
            return MapMessage.errorMessage("删除产品类型失败，产品类型不能为空");

        if (raikouSystem.getRegionService().deleteRegionTag(productTag).isSuccess()) {
            addAdminLog("site-regionproduct-" + getCurrentAdminUser().getAdminUserName() + "删除产品类型" + productTag);
            return MapMessage.successMessage("删除产品类型成功");
        } else {
            return MapMessage.errorMessage("删除产品类型失败");
        }
    }

    /**
     * 刷新数据库接口
     */
    @RequestMapping(value = "reloadregionproduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reloadRegionProduct() {
        return MapMessage.successMessage("这个功能其实已经没有了");
    }

    /**
     * **************************private method****************************************************
     */
    // 获得当前所有产品类型
    private List<String> getAvailableProductTypes() {
        return raikouSystem.getRegionLoader()
                .__db_loadRegionTagsIncludeDisabled()
                .asList()
                .stream()
                .map(RegionTag::getId)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
