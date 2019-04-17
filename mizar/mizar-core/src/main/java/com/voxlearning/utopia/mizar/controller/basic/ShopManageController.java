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

package com.voxlearning.utopia.mizar.controller.basic;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.entity.MizarTreeNode;
import com.voxlearning.utopia.mizar.entity.ShopQueryContext;
import com.voxlearning.utopia.mizar.service.basic.MizarShopManager;
import com.voxlearning.utopia.service.mizar.api.constants.MizarAuditStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;
import com.voxlearning.utopia.service.mizar.api.constants.MizarShopStatusType;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCategory;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基本信息-机构管理 Controller
 * Created by yuechen.wang on 16/9/26.
 */
@Controller
@RequestMapping(value = "/basic/shop")
public class ShopManageController extends AbstractMizarController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private MizarShopManager mizarShopManager;

    // 机构列表页
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1));
        Pageable pageable = new PageRequest(pageIndex - 1, PAGE_SIZE);//从0始开始
        String shopName = getRequestString("shopName");
        Boolean cooperator = getRequestBool("cooperator", null); // 是否合作机构
        Boolean vip = getRequestBool("vip", null); // 是否VIP机构

        // 初始化参数
        ShopQueryContext queryContext = new ShopQueryContext(pageable);
        queryContext.setShopName(shopName);
        queryContext.setCooperator(cooperator);
        queryContext.setVip(vip);

        Page<MizarShop> shopPage = mizarShopManager.page(getCurrentUser(), queryContext);

        //处理审核状态的机构列表
        List<MizarEntityChangeRecord> changeRecordList = mizarChangeRecordLoaderClient.loadByEntityType(MizarEntityType.SHOP.getCode());
        if (Objects.nonNull(changeRecordList)) {
            List<String> changeShopIdList = changeRecordList.stream().filter(o -> StringUtils.isNotBlank(o.getTargetId()))
                    .filter(o -> StringUtils.equals(MizarAuditStatus.PENDING.name(), o.getAuditStatus())) //非待审核状态的机构信息
                    .map(MizarEntityChangeRecord::getTargetId).collect(Collectors.toList());
            model.addAttribute("changeShopIdList", changeShopIdList);
        }

        model.addAllAttributes(queryContext.toParamMap());
        model.addAttribute("shopList", shopPage.getContent());
        model.addAttribute("totalPage", Integer.max(1, shopPage.getTotalPages()));
        model.addAttribute("currentPage", pageIndex);
        return "basic/shop/list";
    }

    // 新增机构
    @RequestMapping(value = "/add.vpage")
    public String addShop(Model model) {
        initShopModel(model, null);
        return "/basic/shop/add";
    }

    // 编辑机构
    @RequestMapping(value = "/edit.vpage")
    public String editShop(Model model) {
        String type = requestString("type");
        String shopId = requestString("id");
        // 没有带ID参数的跳转到新增页面
        if (StringUtils.isBlank(shopId)) {
            return "redirect: /basic/shop/add.vpage";
        }
        MizarShop mizarShop = mizarLoaderClient.loadShopById(shopId);
        // 无效的机构ID
        if (mizarShop == null) {
            return "redirect: /basic/shop/index.vpage";
        }
        initShopModel(model, mizarShop);
        model.addAttribute("mizarShop", mizarShop);
        MizarBrand mizarBrand = mizarLoaderClient.loadBrandById(mizarShop.getBrandId());
        model.addAttribute("mizarBrand", mizarBrand);
        if (StringUtils.equals(type, "detail")) {
            //详情页面
            List<MizarUser> userList = mizarUserLoaderClient.loadByShopId(mizarShop.getId());
            model.addAttribute("userList", userList);
            return "/basic/shop/detail";
        }
        //修改页面
        return "/basic/shop/edit";
    }

    /**
     * BD和运营人员新建机构的时候允许先生成实体，并生成一条变更申请
     * 如果没有变更项，不保存
     */
    @RequestMapping(value = "/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveShop() {
        String shopId = "New";
        try {
            MizarShop mizarShop = requestEntity(MizarShop.class);
            List<Map<String, Object>> teachersInfo = getTeachersInfo();
            mizarShop.setFaculty(teachersInfo);
            mizarShop.setVip(SafeConverter.toBoolean(getRequestBool("vip")));
            mizarShop.setBaiduGps(SafeConverter.toBoolean(getRequestBool("baiduGps")));
            mizarShop.setCooperator(SafeConverter.toBoolean(getRequestBool("cooperator")));

            // ID 为空的时候认为是新建的情况
            shopId = mizarShop.getId();
            if (StringUtils.isBlank(shopId)) {
                return mizarShopManager.create(mizarShop, getCurrentUser());
            } else {
                return mizarShopManager.modify(mizarShop, getCurrentUser());
            }
        } catch (Exception ex) {
            logger.error("Failed save mizar shop, sid={}", shopId, ex);
            return MapMessage.errorMessage("机构保存失败：" + ex.getMessage());
        }
    }

    // 变更状态
    @RequestMapping(value = "changestatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeShopStatus() {
        if (!getCurrentUser().isOperator()) {
            return MapMessage.errorMessage("您没有操作权限");
        }
        String shopId = getRequestString("sid");
        String status = getRequestString("status");
        try {
            MizarShopStatusType shopStatus = MizarShopStatusType.parse(status);
            MizarShop shop = mizarLoaderClient.loadShopById(shopId);
            if (shopStatus == null || shop == null) {
                return MapMessage.errorMessage("无效的参数");
            }
            //  状态已经变更的话，直接返回
            if (status.equals(shop.getShopStatus())) {
                return MapMessage.successMessage();
            }
            shop.setShopStatus(status);
            return mizarServiceClient.saveMizarShop(shop);
        } catch (Exception ex) {
            logger.error("Failed change Shop status, sid={}, status={}", shopId, status, ex);
            return MapMessage.errorMessage("状态变更失败：" + ex.getMessage());
        }
    }

    private void initShopModel(Model model, MizarShop shop) {
        Map<Integer, ExRegion> regionMap = raikouSystem.getRegionBuffer().loadAllRegions();
        List<MizarCategory> categories = mizarLoaderClient.loadAllMizarCategory();
        model.addAttribute("regions", generateRegion(regionMap)); // 地区联动
        model.addAttribute("categoryList", generateCategory(categories)); // 分类联动
        Map<String, Object> params = new HashMap<>();
        if (shop != null) {
            Integer regionCode = shop.getRegionCode();
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            params.put("provCode", exRegion == null ? null : exRegion.getProvinceCode());
            params.put("cityCode", exRegion == null ? null : exRegion.getCityCode());
            params.put("countyCode", exRegion == null ? null : exRegion.getCountyCode());
            params.put("firstCat", shop.getFirstCategory() == null ? null : shop.getFirstCategory().stream().findFirst().orElse(null));
            params.put("secondCat", shop.getSecondCategory() == null ? null : shop.getSecondCategory().stream().findFirst().orElse(null));
        }
        model.addAttribute("initParam", params);
    }

    // 地区三级联动
    private List<MizarTreeNode> generateRegion(Map<Integer, ExRegion> regionMap) {
        if (regionMap == null || regionMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<Region> regions = new ArrayList<>(regionMap.values());
        List<MizarTreeNode> result = new ArrayList<>();
        Map<String, MizarTreeNode> retMap = new HashMap<>();
        for (Region region : regions) {
            // 转换成要使用的HashMap对象
            MizarTreeNode node = new MizarTreeNode();
            node.setName(region.getName());
            node.setCode(String.valueOf(region.getCode()));
            node.setChildren(new ArrayList<>());
            // 先将省一级加入
            if (region.getPcode() == 0) {
                result.add(node);
            }
            retMap.put(String.valueOf(region.getCode()), node);
        }

        // 第二次循环，根据Id和ParentID构建父子关系
        for (Region region : regions) {
            Integer pcode = region.getPcode();
            if (pcode == 0) {
                continue;
            }

            MizarTreeNode parentNode = retMap.get(String.valueOf(pcode));
            MizarTreeNode childNode = retMap.get(String.valueOf(region.getCode()));

            if (parentNode == null) {
                if (!result.contains(childNode)) {
                    result.add(childNode);
                }
            } else {
                List<MizarTreeNode> children = parentNode.getChildren();
                if (!children.contains(childNode)) {
                    children.add(childNode);
                }
            }
        }
        return result;
    }

    // 机构分类联动
    private List<MizarTreeNode> generateCategory(List<MizarCategory> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }
        List<MizarTreeNode> result = new ArrayList<>();
        Map<String, List<MizarCategory>> categoryGroup = categories.stream().collect(Collectors.groupingBy(MizarCategory::getFirstCategory));
        for (Map.Entry<String, List<MizarCategory>> category : categoryGroup.entrySet()) {
            MizarTreeNode firstCategory = new MizarTreeNode();
            firstCategory.setName(category.getKey());
            firstCategory.setCode(category.getKey());
            List<MizarTreeNode> secondCategoryList = category.getValue().stream()
                    .map(c -> {
                        MizarTreeNode node = new MizarTreeNode();
                        node.setName(c.getSecondCategory());
                        node.setCode(c.getSecondCategory());
                        node.setChildren(Collections.emptyList());
                        return node;
                    }).collect(Collectors.toList());
            firstCategory.setChildren(secondCategoryList);
            result.add(firstCategory);
        }
        return result;
    }

}
