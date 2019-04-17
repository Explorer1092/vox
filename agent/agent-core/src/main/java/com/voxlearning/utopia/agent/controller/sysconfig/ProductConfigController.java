/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import com.voxlearning.utopia.agent.persist.entity.AgentProductInventoryRecord;
import com.voxlearning.utopia.agent.persist.entity.product.AgentProductInventoryChangeInfo;
import com.voxlearning.utopia.agent.service.sysconfig.AgentProductInventoryRecordService;
import com.voxlearning.utopia.agent.service.sysconfig.ProductConfigService;
import com.voxlearning.utopia.agent.service.user.GroupConfigService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 发布时需要改脚本：这个目录不能同步：
 * agent\\agent-webapp\\target\\agent-webapp\\public\\upload
 * Created by Shuai.Huan on 2014/7/17.
 */

@Controller
@RequestMapping("/sysconfig/product")
@Slf4j
public class ProductConfigController extends AbstractAgentController {
    @Inject
    private ProductConfigService productConfigService;
    @Inject
    private GroupConfigService groupConfigService;
    @Inject
    private AgentProductInventoryRecordService agentProductInventoryRecordService;
    @Inject
    private AgentOrderLoaderClient agentOrderLoaderClient;
    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @OperationCode("6e84ca8b540746c9")
    String index(Model model) {
        List<Map<String, Object>> productList = productConfigService.getAllProducts(getCurrentUser());
        productList = productList.stream().sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("inventoryQuantity")), SafeConverter.toInt(o1.get("inventoryQuantity")))).collect(Collectors.toList());
        model.addAttribute("products", productList);
        return "sysconfig/product/productindex";
    }


    @RequestMapping(value = "addproduct.vpage", method = RequestMethod.GET)
    String toAddPage(Model model) {
        long id = getRequestLong("id", 0);
        if (id != 0) {
            model.addAttribute("productId", id);
            model.addAttribute("product", productConfigService.getAgentProductMapById(getCurrentUser(), id));
        }
        model.addAttribute("productTypes", AgentProductType.toKeyValuePairs());
        return "sysconfig/product/addproduct";
    }

    @RequestMapping(value = "addproduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addProduct(String productName, String productDesc, Float price, Integer inventoryOpt, Model model) {
        Long id = getRequestLong("productId", 0);
        Boolean primarySchoolVisible = getRequestBool("primarySchoolVisible");  //小学可见
        Boolean juniorSchoolVisible = getRequestBool("juniorSchoolVisible");    //中学可见
        Integer roleVisibleAuthority = getRequestInt("roleVisibleAuthority");
        try {
            Integer quantity = getRequestInt("quantity");
            String quantityChangeDesc = getRequestString("quantityChangeDesc");
            if(quantity != 0 && StringUtils.isBlank(quantityChangeDesc)){
                return MapMessage.errorMessage("请填写库存变更原因");
            }
            //final String namePrefix = "marketing-production-";
            String img1 = getRequestString("img1");
            String img2 = getRequestString("img2");
            String img3 = getRequestString("img3");
            String img4 = getRequestString("img4");

            // add log info
            asyncLogService.logProductModified(getCurrentUser(), getRequest().getRequestURI(), "saveProduct " + id,
                    id + "," + productName + "," + price + "," );
            asyncLogService.logProductModified(getCurrentUser(), getRequest().getRequestURI(), "saveProduct " + id,
                    id + "," + img1 + "," + img2 + "," + img3 + "," + img4);

            if(StringUtils.isNotBlank(productDesc) && productDesc.length() > 180){
                return MapMessage.errorMessage("商品描述不能超过180字！");
            }

            if (id == 0) {
                if (productConfigService.productExists(productName)) {
                    return MapMessage.errorMessage("此商品已经存在!");
                }
                return productConfigService.addProduct(getCurrentUserId(), productName, productDesc,
                        price, img1, img2, img3, img4, inventoryOpt, quantity, quantityChangeDesc,primarySchoolVisible,juniorSchoolVisible, roleVisibleAuthority);
            } else {
                AgentProduct agentProduct = productConfigService.getByName(productName);
                if (agentProduct != null && !agentProduct.getId().equals(id)) {
                    model.addAttribute("error", "此商品已经存在!");
                    model.addAttribute("productId", id);
                    model.addAttribute("product", productConfigService.getAgentProductMapById(getCurrentUser(), id));
                    return MapMessage.errorMessage("此商品已经存在!");
                }
                AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
                return builder.keyPrefix("AgentProductQuantity")
                        .keys(id)
                        .callback(() -> {
                            try {
                                return productConfigService.updateProduct(id, getCurrentUserId(), productName, productDesc, price, img1, img2, img3, img4, inventoryOpt, quantity, quantityChangeDesc,primarySchoolVisible,juniorSchoolVisible, roleVisibleAuthority);
                            } catch (Exception ex) {
                                return MapMessage.errorMessage("修改产品信息失败");
                            }
                        })
                        .build()
                        .execute();
            }
        } catch (Exception ex) {
            log.error("添加/编辑商品失败,productName:{},productDesc:{},price:{}," +
                            "msg:{}",
                    productName, productDesc, price, ex.getMessage(), ex
            );
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "delproduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage delProduct(Long id) {
        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        return builder.keyPrefix("AgentProductQuantity")
                .keys(id)
                .callback(() -> {
                    MapMessage message = new MapMessage();
                    try {
                        // add log info
                        asyncLogService.logProductModified(getCurrentUser(), getRequest().getRequestURI(), "saveProduct " + id,
                                String.valueOf(id));

                        productConfigService.deleteProduct(id);
                        return message.setSuccess(true);
                    } catch (Exception ex) {
                        log.error("删除权限失败,roleId:{},msg:{}", id, ex.getMessage(), ex);
                        message.setSuccess(false);
                        message.setInfo("操作失败!" + ex.getMessage());
                        return message;
                    }
                })
                .build()
                .execute();
    }




    /**
     * 加载group=>region信息
     *
     * @return
     */
    @RequestMapping(value = "update_status.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage updateStatus() {
        Long productId = getRequestLong("id");
        Integer status = requestInteger("status");
        AgentProduct agentProduct = productConfigService.getById(productId);
        if(agentProduct == null){
            return MapMessage.errorMessage("该物料不存在");
        }
        if(status == null){
            return MapMessage.errorMessage("商品状态错误");
        }
        if(Objects.equals(status, agentProduct.getStatus())){
            return MapMessage.successMessage();
        }
        agentProduct.setStatus(status);
        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        return builder.keyPrefix("AgentProductQuantity")
                .keys(productId)
                .callback(() -> {
                    if (productConfigService.updateProduct(agentProduct)) {
                        if (status == 1) { // 商品下架了, 清除购物车里面的商品
                            cleanOrder(productId);
                        }
                        return MapMessage.successMessage();
                    }
                    return MapMessage.errorMessage("操作失败");
                }).build()
                .execute();
    }

    private void cleanOrder(Long productId) {
        List<AgentOrder> draftOrderList = agentOrderLoaderClient.findAgentOrderByStatus(AgentOrderStatus.DRAFT);
        if (CollectionUtils.isNotEmpty(draftOrderList)) {
            for (AgentOrder order : draftOrderList) {
                AgentOrder agentOrder = agentOrderLoaderClient.getOrderById(order.getId());
                List<AgentOrderProduct> orderProductList = agentOrder.getOrderProductList();
                if (CollectionUtils.isNotEmpty(orderProductList)) {
                    for (AgentOrderProduct orderProduct : orderProductList) {
                        if (Objects.equals(orderProduct.getProductId(), productId)) {
                            agentOrderServiceClient.deleteAgentOrderProduct(orderProduct.getId());
                            break;
                        }
                    }
                }
            }
        }
    }

    @RequestMapping(value = "update_status_all.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateStatusAll(@RequestBody String postData) {
        if (StringUtils.isBlank(postData)) {
            return MapMessage.errorMessage("参数错误");
        }
        Map<String, Object> jsonMap = JsonUtils.fromJson(postData);
        if (jsonMap.get("status") == null) {
            return MapMessage.errorMessage("需要更新的状态错误");
        }
        Integer status = SafeConverter.toInt(jsonMap.get("status"));
        List<String> productIds = (List<String>) jsonMap.get("pid");
        if (CollectionUtils.isEmpty(productIds)) {
            return MapMessage.errorMessage("无需要更新的产品");
        }

        List<Long> productIdList = new ArrayList<>();
        productIds.forEach(item -> productIdList.add(SafeConverter.toLong(item)));
        List<String> finalProductIds = new ArrayList<>();
        List<String> haveInventoryProductIds = new ArrayList<>();
        String onlineFlag = "";
        if (null != jsonMap.get("onlineFlag")){
            onlineFlag = (String)jsonMap.get("onlineFlag");
        }
        //上架有货商品
        if (status == 2 && onlineFlag.equals("onlineHaveInventory")){
            List<Map<String, Object>> productList = productConfigService.getProductsByIds(productIdList);
            productList.forEach(item -> {
                //有货
                if (SafeConverter.toInt(item.get("inventoryQuantity")) > 0){
                    haveInventoryProductIds.add(SafeConverter.toString(item.get("id")));
                }
            });
            finalProductIds.addAll(haveInventoryProductIds);
        }else {
            finalProductIds.addAll(productIds);
        }

        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        return builder.keyPrefix("AgentProductQuantity")
                .keys(finalProductIds)
                .callback(() -> {
                    Integer failed = 0;
                    Integer successItem = 0;
                    Integer summary = 0;
                    for (String productId : finalProductIds) {
                        AgentProduct agentProduct = productConfigService.getById(SafeConverter.toLong(productId));
                        if (agentProduct == null) {
                            failed++;
                            continue;
                        }
                        if (Objects.equals(status, agentProduct.getStatus())) {
                            successItem++;
                        }
                        agentProduct.setStatus(status);
                        if (productConfigService.updateProduct(agentProduct)) {
                            successItem++;
                            if (status == 1) { // 商品下架了, 清除购物车里面的商品
                                cleanOrder(SafeConverter.toLong(productId));
                            }
                        }
                        summary++;
                    }
                    return MapMessage.successMessage().add("failed", failed).add("successItem", successItem).add("summary", summary);
                })
                .build()
                .execute();
    }

    @RequestMapping(value = "product_records.vpage", method = RequestMethod.GET)
    String productInventoryRecord(Model model) {
        Long productId = getRequestLong("id");
        List<Map<String, Object>> recordMapList = new ArrayList<>();
        List<AgentProductInventoryRecord> recordList = agentProductInventoryRecordService.findByProductId(productId);
        if(CollectionUtils.isNotEmpty(recordList)){
            recordList.stream().forEach(p -> {
                Map<String, Object> dataMap = BeanMapUtils.tansBean2Map(p);
                dataMap.put("createTime", DateUtils.dateToString(p.getCreateTime(), "yyyy-MM-dd"));
                recordMapList.add(dataMap);
            });
        }
        model.addAttribute("recordList", recordMapList);
        return "sysconfig/product/product_record";
    }

    /**
     * 批量管理库存商品回显
     * @return
     */
    @RequestMapping(value = "product_list_by_ids.vpage", method = RequestMethod.GET)
    @ResponseBody
    MapMessage getProductsByIds() {
        MapMessage mapMessage = MapMessage.successMessage();
        String productIdList = requestString("productIdList");
        if (StringUtils.isBlank(productIdList)){
            return MapMessage.errorMessage("请选择商品");
        }
        List<Long> productIds = JsonUtils.fromJsonToList(productIdList, Long.class);
        mapMessage.put("dataList", productConfigService.getProductsByIds(productIds));
        return mapMessage;
    }

    /**
     * 批量管理库存保存
     * @return
     */
    @RequestMapping(value = "batch_update_product_inventory.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage batchUpdateProductInventory() {
        String productInventoryChangeInfoList = requestString("productInventoryChangeInfoList");
        if (StringUtils.isBlank(productInventoryChangeInfoList)){
            return MapMessage.errorMessage("请填写商品库存变更信息");
        }
        return productConfigService.batchUpdateProductInventory(getCurrentUserId(),productInventoryChangeInfoList);
    }


    /**
     * 批量上架商品之前
     * @return
     */
    @RequestMapping(value = "before_batch_online_product.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage beforeBatchOnlineProduct() {
        String productIdList = requestString("productIdList");
        if (StringUtils.isBlank(productIdList)){
            return MapMessage.errorMessage("请选择商品");
        }
        List<Long> productIds = JsonUtils.fromJsonToList(productIdList, Long.class);
        return MapMessage.successMessage().add("dataMap",productConfigService.beforeBatchOnlineProduct(productIds));
    }

}
