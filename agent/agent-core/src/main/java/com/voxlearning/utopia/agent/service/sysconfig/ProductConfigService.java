/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.dao.mongo.AgentProductInventoryRecordDao;
import com.voxlearning.utopia.agent.persist.AgentGroupProductPersistence;
import com.voxlearning.utopia.agent.persist.AgentProductPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentGroupProduct;
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import com.voxlearning.utopia.agent.persist.entity.AgentProductInventoryRecord;
import com.voxlearning.utopia.agent.persist.entity.product.AgentProductInventoryChangeInfo;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * Created by Shuai.Huan on 2014/7/17.
 */
@Named
public class ProductConfigService extends AbstractAgentService {

    @Inject private AgentProductPersistence agentProductPersistence;
    @Inject private AgentGroupProductPersistence agentGroupProductPersistence;
    @Inject private AgentProductInventoryRecordDao agentProductInventoryRecordDao;
    @Inject private BaseOrgService baseOrgService;

    /**
     * 获取所有可以查看的商品
     *
     * @return 该用户可以查看的商品
     */
    public List<Map<String, Object>> getAllProducts(AuthCurrentUser user) {

        List<AgentProduct> products = agentProductPersistence.findAll();
        if (CollectionUtils.isEmpty(products)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (AgentProduct agentProduct : products) {
            CollectionUtils.addNonNullElement(result, getAgentProductMapById(user, agentProduct.getId()));
        }
        return result;
    }

    /**
     * 添加商品
     *
     * @param userId 商品的添加人
     * @param productName 商品的名称
     * @param productDesc 商品的介绍
     * @param price  商品的价格
     * @param img1 图片1
     * @param img2 图片2
     * @param img3 图片3
     * @param img4 图片4
     * @return  添加商品的ID
     */
    public MapMessage addProduct(Long userId, String productName, String productDesc, Float price,
                                 String img1, String img2, String img3, String img4,
                                 Integer inventoryOpt, Integer quantities, String quantityChangeDesc,
                                 Boolean primarySchoolVisible,Boolean juniorSchoolVisible, Integer roleVisibleAuthority) {
        quantities = quantities == null? 0: quantities;
        if (StringUtils.isEmpty(productName) || price == null || productExists(productName) || quantities < 0) {
            return MapMessage.errorMessage("商品的参数信息错误！");
        }
        AgentProduct agentProduct = new AgentProduct();
        agentProduct.setProductName(productName);
        agentProduct.setProductDesc(productDesc);
        agentProduct.setPrice(price);
        agentProduct.setDiscountPrice(price);
        agentProduct.setValidFrom(new Date());
        agentProduct.setValidTo(DateUtils.addDays(new Date(), 10000));
        agentProduct.setLatestEditor(userId);
        agentProduct.setProductType(AgentProductType.MATERIAL.getType());
        if (StringUtils.isNotEmpty(img1))
            agentProduct.setProductImg1(img1);
        if (StringUtils.isNotEmpty(img2))
            agentProduct.setProductImg2(img2);
        if (StringUtils.isNotEmpty(img3))
            agentProduct.setProductImg3(img3);
        if (StringUtils.isNotEmpty(img4))
            agentProduct.setProductImg4(img4);

        agentProduct.setInventoryQuantity((agentProduct.getInventoryQuantity() == null ? 0: agentProduct.getInventoryQuantity()) + inventoryOpt * quantities);
        agentProduct.setStatus(1);
        if (agentProduct.getInventoryQuantity() < 0) {
            return MapMessage.errorMessage("设置库存量为负数，请重新设置");
        }
        agentProduct.setPrimarySchoolVisible(primarySchoolVisible); //小学可见
        agentProduct.setJuniorSchoolVisible(juniorSchoolVisible);   //中学可见
        agentProduct.setRoleVisibleAuthority(roleVisibleAuthority); //角色可见权限
        agentProductPersistence.insert(agentProduct);

        if(quantities != 0) {
            // 保存库存变更记录
            addAgentProductInventoryRecord(userId, agentProduct.getId(), 0, agentProduct.getInventoryQuantity(), agentProduct.getInventoryQuantity(), "【调整库存】" + quantityChangeDesc);
        }
        return MapMessage.successMessage().add("id", agentProduct.getId());
    }

    public MapMessage updateProduct(Long id, Long userId, String productName, String productDesc, Float price,
                                    String img1, String img2, String img3, String img4,
                                    Integer inventoryOpt, Integer quantities, String quantityChangeDesc,
                                    Boolean primarySchoolVisible,Boolean juniorSchoolVisible, Integer roleVisibleAuthority) {
        quantities = quantities == null? 0: quantities;
        if (StringUtils.isEmpty(productName) || price == null || (quantities < 0)) {
            return MapMessage.errorMessage("产品信息错误");
        }
        AgentProduct agentProduct = agentProductPersistence.load(id);
        agentProduct.setProductName(productName);
        agentProduct.setProductDesc(productDesc);
        agentProduct.setPrice(price);
        agentProduct.setDiscountPrice(price);
        agentProduct.setValidFrom(new Date());
        agentProduct.setValidTo(DateUtils.addDays(new Date(), 10000));
        agentProduct.setLatestEditor(userId);
        if (StringUtils.isNotEmpty(img1)){
            agentProduct.setProductImg1(img1);
        }else {
            agentProduct.setProductImg1("");
        }
        if (StringUtils.isNotEmpty(img2)){
            agentProduct.setProductImg2(img2);
        }else {
            agentProduct.setProductImg2("");
        }
        if (StringUtils.isNotEmpty(img3)){
            agentProduct.setProductImg3(img3);
        }else {
            agentProduct.setProductImg3("");
        }
        if (StringUtils.isNotEmpty(img4)){
            agentProduct.setProductImg4(img4);
        }else {
            agentProduct.setProductImg4("");
        }
        Integer preQuantity = agentProduct.getInventoryQuantity() == null ? 0: agentProduct.getInventoryQuantity();
        agentProduct.setInventoryQuantity((agentProduct.getInventoryQuantity() == null ? 0: agentProduct.getInventoryQuantity()) + inventoryOpt * quantities);
        if (agentProduct.getInventoryQuantity() < 0) {
            return MapMessage.errorMessage("设置库存量为负数，请重新设置");
        }
        agentProduct.setId(id);
        agentProduct.setPrimarySchoolVisible(primarySchoolVisible); //小学可见
        agentProduct.setJuniorSchoolVisible(juniorSchoolVisible);   //中学可见
        agentProduct.setRoleVisibleAuthority(roleVisibleAuthority);  //角色可见权限值
        agentProductPersistence.replace(agentProduct);

        if(quantities != 0 ){
            // 保存库存变更记录
            addAgentProductInventoryRecord(userId, agentProduct.getId(), preQuantity, agentProduct.getInventoryQuantity(), inventoryOpt * quantities, "【调整库存】" + quantityChangeDesc);
        }
        return MapMessage.successMessage();
    }


    /**
     * 商品是否存在
     *
     * @param productName
     * @return
     */
    public boolean productExists(String productName) {
        if (StringUtils.isEmpty(productName)) {
            return false;
        }
        return getByName(productName) != null;
    }

    /**
     * 删除商品
     *
     * @param productId
     */
    public void deleteProduct(Long productId) {
        if (productId < 0) {
            return;
        }
        agentProductPersistence.delete(productId);
        agentGroupProductPersistence.deleteByProductId(productId);
    }


    public AgentProduct getByName(String productName) {
        return agentProductPersistence.findAll().stream()
                .filter(e -> StringUtils.equals(productName, e.getProductName()))
                .findFirst()
                .orElse(null);
    }

    public List<AgentProduct> getByType(Integer productType) {
        return agentProductPersistence.findAll().stream()
                .filter(e -> Objects.equals(productType, e.getProductType()))
                .collect(Collectors.toList());
    }

    public AgentProduct getById(Long productId) {
        return agentProductPersistence.load(productId);
    }


    public Map<String, Object> getAgentProductMapById(AuthCurrentUser user, Long productId) {

        AgentProduct agentProduct = agentProductPersistence.load(productId);
        if (agentProduct == null || AgentProductType.parse(agentProduct.getProductType()) == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", agentProduct.getId());
        map.put("productName", agentProduct.getProductName());
        map.put("productDesc", agentProduct.getProductDesc());
        map.put("productImg1", agentProduct.getProductImg1());
        map.put("productImg2", agentProduct.getProductImg2());
        map.put("productImg3", agentProduct.getProductImg3());
        map.put("productImg4", agentProduct.getProductImg4());
        map.put("price", agentProduct.getPrice());
        map.put("productType", agentProduct.getProductType());
        map.put("productTypeStr", AgentProductType.parse(agentProduct.getProductType()).getName());
        map.put("inventoryQuantity", agentProduct.getInventoryQuantity());
        map.put("status", agentProduct.getStatus());
        map.put("primarySchoolVisible", agentProduct.getPrimarySchoolVisible());
        map.put("juniorSchoolVisible", agentProduct.getJuniorSchoolVisible());
        map.put("roleVisibleAuthority", agentProduct.getRoleVisibleAuthority());
        return map;
    }

    public List<AgentGroupProduct> getByGroupIds(List<Long> groupIds) {
        return agentGroupProductPersistence.findByGroupIds(groupIds);
    }

    // 根据用户群组和商品的群组来判断用户是否拥有查看商品的权限
    // 管理员和全国总监可以查看所有商品
//    private boolean canViewProduct(AuthCurrentUser user, Long productId) {
//        if (user == null || productId == null) {
//            return false;
//        }
//
//        // 商品如果未设置区域限定则都可以看
//        AgentProduct product = agentProductPersistence.load(productId);
//        if (!product.getRegionLimit()) {
//            return true;
//        }
//
//        // 管理员和全国总监可以查看所有商品
//        if (user.isAdmin() || user.isCountryManager()) {
//            return true;
//        }
//
//        List<AgentGroupProduct> groupProductList = agentGroupProductPersistence.cache().findByProductId(productId);
//        if (groupProductList == null || groupProductList.size() == 0) {
//            return false;
//        }
//
//        List<AgentGroup> userGroupList = baseUserService.getUserGroupList(user.getUserId());
//        if (userGroupList == null || userGroupList.size() == 0) {
//            return false;
//        }
//
//        for (AgentGroupProduct groupProduct : groupProductList) {
//            for (AgentGroup group : userGroupList) {
//                if (groupProduct.getGroupId().equals(group.getId())) {
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }

//    private List<AgentGroup> getProductViewableGroupList(AuthCurrentUser user, Long productId) {
//        List<AgentGroupProduct> groupProductList = agentGroupProductPersistence.findByProductId(productId);
//        if (groupProductList == null || groupProductList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<AgentGroup> retGroupList = new ArrayList<>();
//
//        // 管理员可以查看所有商品
//        if (user.isAdmin()) {
//            for (AgentGroupProduct groupProduct : groupProductList) {
//                AgentGroup group = baseGroupService.getComposedAgentGroupById(groupProduct.getGroupId());
//                if (group != null && !retGroupList.contains(group)) {
//                    retGroupList.add(group);
//                }
//            }
//        } else {
//            List<AgentGroup> userGroupList = baseUserService.deepGetUserManagedGroupList(user.getUserId());
//            if (userGroupList == null || userGroupList.size() == 0) {
//                return Collections.emptyList();
//            }
//
//            for (AgentGroup group : userGroupList) {
//                for (AgentGroupProduct groupProduct : groupProductList) {
//                    if (group.getId().equals(groupProduct.getGroupId()) && !retGroupList.contains(group)) {
//                        retGroupList.add(group);
//                    }
//                }
//            }
//        }
//
//        return retGroupList;
//    }

    public boolean updateProduct(AgentProduct agentProduct){
        if(agentProduct == null || agentProduct.getId() == null){
            return false;
        }
        agentProductPersistence.replace(agentProduct);
        return true;
    }

    public void addAgentProductInventoryRecord(Long userId, Long productId, Integer preQuantity, Integer afterQuantity, Integer quantityChange, String comment){
        if(quantityChange == null || quantityChange == 0){
            return;
        }

        AgentProduct product = getById(productId);
        if(product == null){
            return;
        }
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return;
        }
        AgentProductInventoryRecord inventoryRecord = new AgentProductInventoryRecord();
        inventoryRecord.setUserId(userId);
        inventoryRecord.setUserName(user.getRealName());
        inventoryRecord.setProductId(productId);
        inventoryRecord.setProductName(product.getProductName());
        inventoryRecord.setPreQuantity(preQuantity);
        inventoryRecord.setAfterQuantity(afterQuantity);
        inventoryRecord.setQuantityChange(quantityChange);
        inventoryRecord.setComment(comment);
        agentProductInventoryRecordDao.insert(inventoryRecord);

    }

    /**
     * 根据商品ids获取商品信息
     * @param productIds
     * @return
     */
    public List<Map<String, Object>> getProductsByIds(Collection<Long> productIds) {
        Map<Long, AgentProduct> products = agentProductPersistence.loads(productIds);
        if (MapUtils.isEmpty(products)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        products.values().forEach(item -> {
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("id",item.getId());
            dataMap.put("productName",item.getProductName());
            dataMap.put("inventoryQuantity",item.getInventoryQuantity());
            dataList.add(dataMap);
        });
        return dataList;
    }


    /**
     * 批量修改库存
     * @param productInventoryChangeInfoListStr
     * @return
     */
    public MapMessage batchUpdateProductInventory(Long userId,String productInventoryChangeInfoListStr) {
        List<String> errorInfoList = new ArrayList<>();
        List<AgentProductInventoryChangeInfo> productInventoryChangeInfoList = JsonUtils.fromJsonToList(productInventoryChangeInfoListStr, AgentProductInventoryChangeInfo.class);
        Map<Long, AgentProductInventoryChangeInfo> productInventoryChangeInfoMap = productInventoryChangeInfoList.stream().collect(Collectors.toMap(AgentProductInventoryChangeInfo::getId, Function.identity(), (o1, o2) -> o1));
        Set<Long> productIds = productInventoryChangeInfoMap.keySet();
        Map<Long, AgentProduct> productMap = agentProductPersistence.loads(productIds);
        productIds.forEach(item -> {
            AgentProductInventoryChangeInfo productInventoryChangeInfo = productInventoryChangeInfoMap.get(item);
            if (null != productInventoryChangeInfo){
                //如果是减少库存
                if (null != productInventoryChangeInfo.getInventoryOpt() && Objects.equals(productInventoryChangeInfo.getInventoryOpt(), AgentProductInventoryChangeInfo.INVENTORY_OPT_REDUCE)){
                    AgentProduct agentProduct = productMap.get(item);
                    if (null != agentProduct){
                        int sum = agentProduct.getInventoryQuantity() + (productInventoryChangeInfo.getInventoryOpt() * productInventoryChangeInfo.getQuantity());
                        if (sum < 0){
                            errorInfoList.add(agentProduct.getProductName()+",库存修改不符合要求；");
                        }
                    }
                }
            }
        });
        if (CollectionUtils.isNotEmpty(errorInfoList)){
            return MapMessage.errorMessage().add("errorInfoList",errorInfoList);
        }

        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        return builder.keyPrefix("AgentProductQuantity")
                .keys(userId)
                .callback(() -> {
                    Integer successNum = 0;
                    for (Long productId : productIds) {
                        AgentProduct agentProduct = productMap.get(productId);
                        AgentProductInventoryChangeInfo productInventoryChangeInfo = productInventoryChangeInfoMap.get(productId);
                        if (agentProduct == null || productInventoryChangeInfo == null) {
                            continue;
                        }
                        Integer preQuantity = agentProduct.getInventoryQuantity() == null ? 0 : agentProduct.getInventoryQuantity();
                        agentProduct.setInventoryQuantity(agentProduct.getInventoryQuantity() + (productInventoryChangeInfo.getInventoryOpt() * productInventoryChangeInfo.getQuantity()));
                        //更新商品
                        if (updateProduct(agentProduct)) {
                            //保存库存变更记录
                            addAgentProductInventoryRecord(userId, agentProduct.getId(), preQuantity, agentProduct.getInventoryQuantity(), productInventoryChangeInfo.getInventoryOpt() * productInventoryChangeInfo.getQuantity(), "【调整库存】" + productInventoryChangeInfo.getQuantityChangeDesc());
                            successNum++;
                        }
                    }
                    return MapMessage.successMessage().add("successInfo", "已为您成功变更" + successNum + "种商品库存");
                })
                .build()
                .execute();
    }

    public Map<String, Object> beforeBatchOnlineProduct(Collection<Long> productIds) {
        Map<String,Object> dataMap = new HashMap<>();
        Map<Long, AgentProduct> productMap = agentProductPersistence.loads(productIds);
        if (MapUtils.isEmpty(productMap)) {
            return new HashMap<>();
        }
        int haveInventoryNum = 0;
        int haveNoInventoryNum = 0;
        for (AgentProduct product : productMap.values()){
            //有货
            if (product.getInventoryQuantity() > 0){
                haveInventoryNum ++;
                //无货
            }else {
                haveNoInventoryNum ++;
            }
        }
        dataMap.put("haveInventoryNum",haveInventoryNum);
        dataMap.put("haveNoInventoryNum",haveNoInventoryNum);
        return dataMap;
    }

}
