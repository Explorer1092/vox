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

package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBudget;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialCost;
import com.voxlearning.utopia.agent.persist.entity.material.AgentOrderCityCost;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.service.sysconfig.ProductConfigService;
import com.voxlearning.utopia.agent.service.workspace.UserOrderService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品购买用Controller Class
 * <p/>
 * Created by Alex on 2014/8/14.
 */
@Controller
@RequestMapping("/workspace")
@Slf4j
public class UserOrderController extends AbstractAgentController {

    //允许物料申请的下学期开始时间和结束时间，认为2-7为下学期，其他为上学期
    private static final int SECOND_SEMESTER_BEGIN_MONTH = 2;
    private static final int SECOND_SEMESTER_END_MONTH = 7;

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private ProductConfigService productConfigService;
    @Inject
    private UserOrderService userOrderService;
    @Inject
    private BaseUserService baseUserService;
    @Inject private AgentOrderLoaderClient agentOrderLoaderClient;

    @Inject private AgentMaterialBudgetService agentMaterialBudgetService;

    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;
    @Inject private BaseOrgService baseOrgService;

    @RequestMapping(value = "repair/repair_order_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage repairOrderData() {
        return userOrderService.repairOrderData();
    }

    // 商品列表页
    @RequestMapping(value = "purchase/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        AuthCurrentUser currentUser = getCurrentUser();
        // 所有可见的商品列表
        List<Map<String, Object>> allProducts = productConfigService.getAllProducts(getCurrentUser());
         /*
        1.勾选了小学可见，部门结构下有“小学”业务类型的可见此商品；
        2.勾选了中学可见，部门结构下有“初中”或“高中”业务类型的可见此商品
         */
        List<Map<String, Object>> productList = new ArrayList<>();
        List<AgentServiceType> userServiceTypes = baseOrgService.getUserServiceTypes(getCurrentUserId());
        if (CollectionUtils.isNotEmpty(userServiceTypes)) {
            //小学
            if (userServiceTypes.contains(AgentServiceType.JUNIOR_SCHOOL)) {
                //小学可见
                List<Map<String, Object>> primarySchoolVisibleProductList = allProducts.stream().filter(item -> null != item && null != item.get("primarySchoolVisible") && SafeConverter.toBoolean(item.get("primarySchoolVisible"))).collect(Collectors.toList());
                productList.addAll(primarySchoolVisibleProductList);

                //过滤掉筛序之后的数据
                allProducts.removeAll(primarySchoolVisibleProductList);
            }
            //初中或高中
            if (userServiceTypes.contains(AgentServiceType.MIDDLE_SCHOOL) || userServiceTypes.contains(AgentServiceType.SENIOR_SCHOOL)) {
                //中学可见
                productList.addAll(allProducts.stream().filter(item -> null != item && null != item.get("juniorSchoolVisible") && SafeConverter.toBoolean(item.get("juniorSchoolVisible"))).collect(Collectors.toList()));
            }
        }

        AgentRoleType agentRoleType = baseOrgService.getUserRole(currentUser.getUserId());
        productList = productList.stream()
                .filter(p -> (p.get("status") != null && (Integer) p.get("status") == 2))
                .filter(p -> (AgentRoleType.RoleAuthority.valueOf(agentRoleType.name()).getAuthKey() & (Integer) p.get("roleVisibleAuthority")) > 0)
                .collect(Collectors.toList());
        AgentOrder myOrder = agentOrderLoaderClient.loadUserDraftOrder(getCurrentUserId());
        if (myOrder != null) {
            if (CollectionUtils.isNotEmpty(myOrder.getOrderProductList())) {
               /* allProducts = allProducts.stream().filter(p -> !myOrder.getOrderProductList().stream()
                        .map(AgentOrderProduct::getProductId).collect(Collectors.toSet())
                        .contains(SafeConverter.toLong(p.get("id")))).collect(Collectors.toList());*/
                myOrder.setOrderProductList(myOrder.getOrderProductList().stream().filter(p -> productConfigService.getById(p.getProductId()) != null).collect(Collectors.toList()));
                model.addAttribute("productkind", myOrder.getOrderProductList().size());
            }
        }
        productList = productList.stream().sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("inventoryQuantity")), SafeConverter.toInt(o1.get("inventoryQuantity")))).collect(Collectors.toList());
        model.addAttribute("productList", productList);
        AgentMaterialCost userMaterialCost = agentMaterialBudgetService.getUserMaterialCostByUserId(getCurrentUserId());
        if (null != userMaterialCost) {
            model.addAttribute("userBalance", userMaterialCost.getBalance());
        } else {
            model.addAttribute("userBalance", 0);
        }
        // Draft Order
        return "workspace/order/orderindex";
    }

    // 购物车页
    @RequestMapping(value = "purchase/shopping_cart.vpage", method = RequestMethod.GET)
    public String shoppingCart(Model model) {
        AgentOrder myOrder = agentOrderLoaderClient.loadUserDraftOrder(getCurrentUserId());
        if (myOrder != null) {
            model.addAttribute("myOrder", myOrder);
            if (CollectionUtils.isNotEmpty(myOrder.getOrderProductList())) {
                model.addAttribute("productkind", myOrder.getOrderProductList().size());
            }
        }
        List<Map<String, Object>> allProducts = productConfigService.getAllProducts(getCurrentUser());
        AgentRoleType agentRoleType = baseOrgService.getUserRole(getCurrentUser().getUserId());
        allProducts = allProducts.stream()
                .filter(p -> (p.get("status") != null && (Integer) p.get("status") == 2))
                .filter(p -> (AgentRoleType.RoleAuthority.valueOf(agentRoleType.name()).getAuthKey() & (Integer) p.get("roleVisibleAuthority")) > 0)
                .collect(Collectors.toList());
        model.addAttribute("regionTree", buildAllRegionJsonTree());
        model.addAttribute("productList", allProducts);
        model.addAttribute("paymentMode", createPaymentMode(myOrder != null ? myOrder.getPaymentMode() : null, getCurrentUser().isBusinessDeveloper()));
        //model.addAttribute("cityCostMonths", creaetCityCostMonths(myOrder != null ? myOrder.getCityCostMonth() : null));
        AgentMaterialCost userMaterialCost = agentMaterialBudgetService.getUserMaterialCostByUserId(getCurrentUserId());
        if (null != userMaterialCost) {
            model.addAttribute("userBalance", userMaterialCost.getBalance());
        } else {
            model.addAttribute("userBalance", 0);
        }
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(getCurrentUserId());
        if (CollectionUtils.isNotEmpty(groupUserList)) {
            Long groupId = groupUserList.get(0).getGroupId();
            AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
            AgentOrderCityCost agentOrderCityCost = null;
            if (null != myOrder) {
                agentOrderCityCost = agentMaterialBudgetService.getAgentOrderCityCost(myOrder.getId());
                model.addAttribute("orderCityCost", agentOrderCityCost);
            }
            if (AgentGroupRoleType.City.equals(agentGroup.fetchGroupRoleType())) {
                List<AgentGroupRegion> groupRegionByGroup = baseOrgService.getGroupRegionByGroup(groupId);
                Set<Integer> regionCodeSet = groupRegionByGroup.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet());
                Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodeSet);
                Map<Integer, ExRegion> cityExRegionMap = new HashMap<>();
                exRegionMap.forEach((k, v) -> {
                    RegionType regionType = v.fetchRegionType();
                    if (regionType == RegionType.CITY || regionType == RegionType.COUNTY) {
                        if (!cityExRegionMap.containsKey(v.getCityCode())) {
                            ExRegion exRegion = new ExRegion();
                            exRegion.setCityCode(v.getCityCode());
                            exRegion.setCityName(v.getCityName());
                            cityExRegionMap.put(v.getCityCode(), exRegion);
                        }
                        //如果是省
                    } else {
                        List<ExRegion> cityRegionList = v.getChildren().stream().filter(t -> t.fetchRegionType() == RegionType.CITY).collect(Collectors.toList());
                        cityRegionList.forEach(item -> {
                            if (!cityExRegionMap.containsKey(item.getCityCode())) {
                                ExRegion exRegion = new ExRegion();
                                exRegion.setCityCode(item.getCityCode());
                                exRegion.setCityName(item.getCityName());
                                cityExRegionMap.put(item.getCityCode(), exRegion);
                            }
                        });
                    }
                });
                List<Map<String, Object>> userRegions = new ArrayList<>();
                AgentOrderCityCost finalAgentOrderCityCost = agentOrderCityCost;
                cityExRegionMap.forEach((k, v) -> {
                    RegionType regionType = v.fetchRegionType();
                    if (regionType == RegionType.CITY || regionType == RegionType.COUNTY) {
                        Double balanceSum = 0d;
                        List<AgentMaterialBudget> latest6MonthCityBudget = agentMaterialBudgetService.getLatest6MonthCityBudget(groupId, v.getCityCode());
                        if (CollectionUtils.isNotEmpty(latest6MonthCityBudget)) {
                            for (int i = 0; i < latest6MonthCityBudget.size(); i++) {
                                AgentMaterialBudget item = latest6MonthCityBudget.get(i);
                                balanceSum = MathUtils.doubleAdd(balanceSum, item.getBalance());
                            }
                        }
                        Map<String, Object> bean = new HashMap<String, Object>();
                        bean.put("regionCode", v.getCityCode());
                        bean.put("regionName", v.getCityName());
                        bean.put("balance", balanceSum);
                        if (null != finalAgentOrderCityCost && Objects.equals(v.getCityCode(), finalAgentOrderCityCost.getRegionCode())) {
                            bean.put("selected", true);
                        }
                        userRegions.add(bean);
                    }

                });
                model.addAttribute("userRegions", userRegions);
            }
        }

        AgentOrder oldAgentOrder = agentOrderLoaderClient.findAgentOrderByCreator(getCurrentUserId()).stream().filter(p -> Objects.equals(p.getOrderStatus(), AgentOrderStatus.UNCHECKED.getStatus()) ||
                Objects.equals(p.getOrderStatus(), AgentOrderStatus.PENDING_FINANCIAL.getStatus()) ||
                Objects.equals(p.getOrderStatus(), AgentOrderStatus.PENDING_REGION_MANAGER.getStatus()) ||
                Objects.equals(p.getOrderStatus(), AgentOrderStatus.APPROVED.getStatus()) ||
                Objects.equals(p.getOrderStatus(), AgentOrderStatus.CONFIRMED.getStatus()) ||
                Objects.equals(p.getOrderStatus(), AgentOrderStatus.FINISHED.getStatus()) ||
                Objects.equals(p.getOrderStatus(), AgentOrderStatus.DELIVERED.getStatus())
        ).findFirst().orElse(null);
        if (oldAgentOrder != null) {
            model.addAttribute("oldAgentOrder", oldAgentOrder);
        }
        return "workspace/order/shoppingcart";
    }

    // 支付方式
    private List<Map<String, Object>> createPaymentMode(Integer selectPaymentMode, Boolean isBd) {
        List<Map<String, Object>> paymentMode = new ArrayList<>();
        Arrays.stream(AgentOrderPaymentMode.values()).forEach(p -> {
            Map<String, Object> mode = new HashMap<>();
            mode.put("payId", p.getPayId());
            mode.put("payDes", p.getPayDes());
            if (Objects.equals(selectPaymentMode, p.getPayId())) {
                mode.put("selected", true);
            }
            paymentMode.add(mode);
        });
        return paymentMode;
    }

    // 城市费用月份
    private List<Map<String, Object>> creaetCityCostMonths(Integer selectCityCostMonth) {
        List<Map<String, Object>> cityCostMonths = new ArrayList<>();
        Calendar nowTime = Calendar.getInstance();
        List<Integer> costMonths = getCityCostMonths(nowTime.get(Calendar.YEAR), nowTime.get(Calendar.MONTH) + 1);

        for (int j = 0; j < costMonths.size(); j++) {
            int yearMonth = costMonths.get(j);
            int year = yearMonth / 100;
            int month = yearMonth % 100;

            Map<String, Object> cityCostMonth = new HashMap<>();
            cityCostMonth.put("month", yearMonth);
            cityCostMonth.put("monthName", StringUtils.formatMessage("{}年{}月城市费用", year, month));
            if (Objects.equals(selectCityCostMonth, yearMonth)) {
                cityCostMonth.put("selected", true);
            }
            cityCostMonths.add(cityCostMonth);
        }
        return cityCostMonths;
    }

    /**
     * 计算当前时间可以申请城市支持费用的月份
     *
     * @param currentYear
     * @param currentMonth
     * @return
     */
    private List<Integer> getCityCostMonths(int currentYear, int currentMonth) {
        List<Integer> showYearMonth = new ArrayList<>();
        if (currentMonth < SECOND_SEMESTER_BEGIN_MONTH) {
            for (int i = SECOND_SEMESTER_END_MONTH + 1; i <= 12; i++) {
                showYearMonth.add((currentYear - 1) * 100 + i);
            }
            for (int i = 1; i <= currentMonth; i++) {
                showYearMonth.add(currentYear * 100 + i);
            }
        } else if (currentMonth >= SECOND_SEMESTER_BEGIN_MONTH && currentMonth <= SECOND_SEMESTER_END_MONTH) {
            for (int i = SECOND_SEMESTER_BEGIN_MONTH; i <= currentMonth; i++) {
                showYearMonth.add(currentYear * 100 + i);
            }
        } else if (currentMonth > SECOND_SEMESTER_END_MONTH) {
            for (int i = SECOND_SEMESTER_END_MONTH + 1; i <= currentMonth; i++) {
                showYearMonth.add(currentYear * 100 + i);
            }
        }
        return showYearMonth;
    }

    @RequestMapping(value = "purchase/addorderproduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addOrderProduct(Long productId) {
        MapMessage message = new MapMessage();
        AuthCurrentUser user = getCurrentUser();
        try {
            AgentOrder myOrder = agentOrderLoaderClient.loadUserDraftOrder(getCurrentUserId());
            if (myOrder == null) {
                myOrder = userOrderService.createUserOrder(user, productId);
            } else {
                // 检查订单中是否已有该商品
                List<AgentOrderProduct> orderProductList = myOrder.getOrderProductList();
                if (orderProductList != null && orderProductList.size() > 0) {
                    for (AgentOrderProduct orderProduct : orderProductList) {
                        if (orderProduct.getProductId().equals(productId)) {
                            message.setSuccess(false);
                            message.setInfo("订单中已有该商品!");
                            return message;
                        }
                    }
                }

                userOrderService.addOrderProduct(myOrder, productId);
            }

            asyncLogService.logOrder(getCurrentUser(), getRequest().getRequestURI(), "add order product",
                    "orderId:" + myOrder.getId() + " productId:" + productId);

            message.setSuccess(true);
            return message;
        } catch (Exception e) {
            log.error("添加商品错误", e);
            message.setSuccess(false);
            message.setInfo("操作失败!" + e.getMessage());
            return message;
        }
    }

    //删除订单产品
    @RequestMapping(value = "purchase/delorderproduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delOrderProduct(Long orderProductId) {
        MapMessage message = new MapMessage();
        try {
            AgentOrder myOrder = agentOrderLoaderClient.loadUserDraftOrder(getCurrentUserId());
            if (myOrder == null) {
                message.setSuccess(false);
                message.setInfo("订单不存在!");
                return message;
            }

            userOrderService.removeOrderProduct(myOrder, orderProductId);
            asyncLogService.logOrder(getCurrentUser(), getRequest().getRequestURI(), "remove order product",
                    "orderId:" + myOrder.getId() + " productId:" + orderProductId);

            message.setSuccess(true);
            return message;
        } catch (Exception e) {
            log.error("添加商品错误", e);
            message.setSuccess(false);
            message.setInfo("操作失败!" + e.getMessage());
            return message;
        }
    }

    @RequestMapping(value = "purchase/modifierorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifierOrder(@RequestBody String orderJson) {
        Map<String, Object> orderInfo = JsonUtils.fromJson(orderJson);
        if (MapUtils.isEmpty(orderInfo)) {
            return MapMessage.errorMessage("数据传送失败");
        }
        List productQuantity = (List) orderInfo.get("productQuantity");
        AgentOrder myOrder = agentOrderLoaderClient.loadUserDraftOrder(getCurrentUserId());
        if (myOrder == null) {
            myOrder = userOrderService.createUserOrder(getCurrentUser(), null);
            //return MapMessage.errorMessage("订单不存在!");
        }
        myOrder.setOrderNotes(SafeConverter.toString(orderInfo.get("orderNotes")));
        myOrder.setMobile(SafeConverter.toString(orderInfo.get("mobile")));
        myOrder.setConsignee(SafeConverter.toString(orderInfo.get("consignee")));
        myOrder.setProvince(SafeConverter.toString(orderInfo.get("province"), ""));
        myOrder.setCity(SafeConverter.toString(orderInfo.get("city"), ""));
        myOrder.setCounty(SafeConverter.toString(orderInfo.get("county"), ""));
        myOrder.setAddress(SafeConverter.toString(orderInfo.get("address")));
        myOrder.setPaymentMode(SafeConverter.toInt(orderInfo.get("paymentMode")));
        Integer costRegionCode = SafeConverter.toInt(orderInfo.get("costRegionCode"));
        myOrder.setPaymentVoucher(SafeConverter.toString(orderInfo.get("paymentVoucher")));
        asyncLogService.logOrder(getCurrentUser(), getRequest().getRequestURI(), "modifier order",
                StringUtils.formatMessage("orderId:{},productQuantity:{}", myOrder.getId(), productQuantity));
        if (AgentOrderPaymentMode.CITY_COST.getPayId().equals(myOrder.getPaymentMode()) && null != costRegionCode && costRegionCode > 0) {
            AgentOrderCityCost agentOrderCityCost = new AgentOrderCityCost();
            agentOrderCityCost.setOrderId(myOrder.getId());
            agentOrderCityCost.setRegionCode(costRegionCode);
            agentMaterialBudgetService.upsertAgentOrderCityCost(agentOrderCityCost);
        } else {
            agentMaterialBudgetService.removeAgentOrderCityCost(myOrder.getId());
        }
        return userOrderService.updateOrder(myOrder, productQuantity, false);
    }

    @RequestMapping(value = "purchase/submitorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("1ba635dd15d244de")
    public MapMessage submitOrder(@RequestBody String orderJson) {
        Map<String, Object> orderInfo = JsonUtils.fromJson(orderJson);
        if (MapUtils.isEmpty(orderInfo)) {
            return MapMessage.errorMessage("数据传送失败");
        }
        List productQuantity = (List) orderInfo.get("productQuantity");
        String orderNotes = SafeConverter.toString(orderInfo.get("orderNotes"));
        String consignee = SafeConverter.toString(orderInfo.get("consignee"));
        String mobile = SafeConverter.toString(orderInfo.get("mobile"));
        String province = SafeConverter.toString(orderInfo.get("province"), "");
        String city = SafeConverter.toString(orderInfo.get("city"), "");
        String county = SafeConverter.toString(orderInfo.get("county"), "");
        String address = SafeConverter.toString(orderInfo.get("address"));
        String paymentVoucher = SafeConverter.toString(orderInfo.get("paymentVoucher"));
        Integer paymentMode = SafeConverter.toInt(orderInfo.get("paymentMode"));
        Integer costRegionCode = SafeConverter.toInt(orderInfo.get("costRegionCode"));

        List<String> productIdList = (List<String>) orderInfo.get("productIdList");
        List<String> materialBudgetIdList = (List<String>) orderInfo.get("materialBudgetIdList");
        AgentOrder myOrder = agentOrderLoaderClient.loadUserDraftOrder(getCurrentUserId());
        if (myOrder == null) {
            return MapMessage.errorMessage("订单不存在!");
        }
        Map<Long, AgentOrderProduct> orderProductMap = myOrder.getOrderProductList().stream().collect(Collectors.toMap(AgentOrderProduct::getId, Function.identity(), (o1, o2) -> o1));
        //过滤掉库存不足的商品
        List<Long> productIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(productIdList)) {
            productIdList.forEach(item -> productIds.add(SafeConverter.toLong(ConversionUtils.toString(item))));
        }
        List<AgentOrderProduct> orderProductList = myOrder.getOrderProductList().stream().filter(item -> null != item && !productIds.contains(SafeConverter.toLong(item.getProductId()))).collect(Collectors.toList());

        //删除库存不足商品
        List<AgentOrderProduct> noInventoryOrderProductList = myOrder.getOrderProductList().stream().filter(item -> null != item && productIds.contains(SafeConverter.toLong(item.getProductId()))).collect(Collectors.toList());
        noInventoryOrderProductList.forEach(item -> {
            userOrderService.removeOrderProduct(myOrder, item.getId());
        });

        List productQuantityFinal = new ArrayList();

        Float cost = 0f;
        if (CollectionUtils.isEmpty(orderProductList)) {
            MapMessage.errorMessage("请选择商品！");
        } else {
            myOrder.setOrderProductList(orderProductList);

            List<Long> orderProductIds = orderProductList.stream().map(AgentOrderProduct::getId).collect(Collectors.toList());

            for (Object item : productQuantity) {
                if (null != item && item instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) item;
                    Long orderProductId = SafeConverter.toLong(map.get("id"));
                    if (orderProductIds.contains(orderProductId)) {
                        productQuantityFinal.add(item);
                    } else {
                        AgentOrderProduct orderProduct = orderProductMap.get(orderProductId);
                        if (null != orderProduct) {
                            cost = MathUtils.floatAdd(cost, orderProduct.getProductQuantity() * orderProduct.getPrice());
                        }
                    }
                }
            }
        }
        myOrder.setOrderAmount(MathUtils.floatSub(myOrder.getOrderAmount(), cost));
        agentOrderServiceClient.saveOrder(myOrder);
        asyncLogService.logOrder(getCurrentUser(), getRequest().getRequestURI(), "submit order",
                "orderId:" + myOrder.getId() + " productQuantity:" + productQuantityFinal + " orderNotes:" + orderNotes);
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("AgentSubmitOrder")
                    .keys(myOrder.getCreator())
                    .callback(() -> userOrderService.doSubmitOrder(myOrder, productQuantityFinal, orderNotes,
                            consignee, mobile, province, city, county, address, paymentVoucher, paymentMode,
                            costRegionCode, getCurrentUser(), materialBudgetIdList))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("该部门城市费用订单请求过多，请稍后再试");
        }
//        return userOrderService.submitOrder(myOrder, productQuantityFinal, orderNotes, consignee, mobile, province, city, county, address, paymentVoucher,
//                paymentMode, costRegionCode, getCurrentUser(), materialBudgetIdList);
    }


    /**
     * 获取城市对应的近六个月的城市费用
     *
     * @return
     */
    @RequestMapping(value = "purchase/get_latest6_month_city_budget.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLatest6MonthCityBudget() {
        Integer costRegionCode = getRequestInt("costRegionCode");
        AgentGroupUser agentGroupUser = baseOrgService.getGroupUserByUser(getCurrentUserId()).stream().findFirst().orElse(null);
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<AgentMaterialBudget> materialBudgetList = agentMaterialBudgetService.getLatest6MonthCityBudget(agentGroupUser.getGroupId(), costRegionCode);
        materialBudgetList.forEach(item -> {
            if (null != item.getBalance() && item.getBalance() > 0) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("id", item.getId());
                dataMap.put("cityName", item.getRegionName());
                StringBuilder monthBuilder = new StringBuilder();
                String monthStr = null != item.getMonth() ? SafeConverter.toString(item.getMonth()) : "";
                if (StringUtils.isNotBlank(monthStr)) {
                    monthBuilder.append(monthStr.substring(0, 4));
                    monthBuilder.append("-");
                    monthBuilder.append(monthStr.substring(4, 6));
                }
                dataMap.put("month", monthBuilder);
                dataMap.put("balance", item.getBalance());
                dataList.add(dataMap);
            }
        });
        return MapMessage.successMessage().add("dataList", dataList);
    }

    /**
     * 提交订单之前接口
     *
     * @return
     */
    @RequestMapping(value = "purchase/before_submit_order.vpage")
    @ResponseBody
    public MapMessage beforeSubmitOrder() {
        AgentOrder myOrder = agentOrderLoaderClient.loadUserDraftOrder(getCurrentUserId());
        if (myOrder == null) {
            return MapMessage.errorMessage("订单不存在!");
        }
        List<AgentOrderProduct> orderProductList = myOrder.getOrderProductList();
        if (CollectionUtils.isEmpty(orderProductList)) {
            return MapMessage.errorMessage("请选择商品！");
        }
        return userOrderService.beforeSubmitOrder(orderProductList);
    }

//    public static void main(String[] args) {
//        List<Long> list = new ArrayList<>();
//        list.add(123l);
//        list.add(456l);
//        System.out.println("result:"+list.contains(12l));
//    }

}
