package com.voxlearning.utopia.agent.service.invoice;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.constants.AgentLogisticsStatus;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.persist.AgentInvoicePersistence;
import com.voxlearning.utopia.agent.persist.AgentInvoiceProductPersistence;
import com.voxlearning.utopia.agent.persist.AgentProductPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentInvoice;
import com.voxlearning.utopia.agent.persist.entity.AgentInvoiceProduct;
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AgentInvoiceService
 *
 * @author song.wang
 * @date 2016/9/7
 */
@Named
public class AgentInvoiceService extends AbstractAgentService {

    @Inject
    private AgentInvoicePersistence agentInvoicePersistence;

    @Inject
    private AgentInvoiceProductPersistence agentInvoiceProductPersistence;
    @Inject
    private AgentProductPersistence agentProductPersistence;
    @Inject
    private AgentOrderLoaderClient agentOrderLoaderClient;
    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;
    @Inject
    private BaseOrgService baseOrgService;

    @Inject
    AgentNotifyService agentNotifyService;

    @Inject
    WorkFlowLoaderClient workFlowLoaderClient;

    public List<Map<String, Object>> searchInvoice(Long invoiceId, String logisticsId, AgentLogisticsStatus logisticsStatus, Date startDate, Date endDate){
        List<AgentInvoice> invoiceList = agentInvoicePersistence.findInvoiceList(invoiceId, logisticsId, logisticsStatus, startDate, endDate);
        if(CollectionUtils.isEmpty(invoiceList)){
            return Collections.emptyList();
        }
        return invoiceList.stream().filter(p -> !p.getDisabled()).map(this::generateInvoiceData).collect(Collectors.toList());
    }


    private Map<String, Object> generateInvoiceData(AgentInvoice agentInvoice){
        Map<String, Object> retMap = new HashMap<>();
        Map<String, Object> beanMap = BeanMapUtils.tansBean2Map(agentInvoice);
        if(beanMap != null){
            retMap.putAll(beanMap);
        }
        List<AgentOrder> agentOrders = agentOrderLoaderClient.findAgentOrderByInvoiceId(agentInvoice.getId());
        if (CollectionUtils.isNotEmpty(agentOrders)){
            Set<Long> orderIds = agentOrders.stream().map(AgentOrder::getId).collect(Collectors.toSet());
            retMap.put("orderIds",StringUtils.join(orderIds,","));
        }
        List<AgentInvoiceProduct> invoiceProductList = agentInvoiceProductPersistence.findByInvoiceId(agentInvoice.getId());
        if(CollectionUtils.isNotEmpty(invoiceProductList)){
            List<Map<String, Object>> productList = invoiceProductList.stream().map(this::generateProductData).collect(Collectors.toList());
            retMap.put("productList", productList);
        }
        return retMap;
    }

    private Map<String, Object> generateProductData(AgentInvoiceProduct invoiceProduct){
        Map<String, Object> retMap = new HashMap<>();
        Map<String, Object> beanMap = BeanMapUtils.tansBean2Map(invoiceProduct);
        if(beanMap != null){
            retMap.putAll(beanMap);
        }
        AgentProduct agentProduct = agentProductPersistence.load(invoiceProduct.getProductId());
        if(agentProduct != null){
            retMap.put("productName", agentProduct.getProductName());
        }
        return retMap;
    }

    public void createInvoiceFromOrder(){
        List<AgentOrder> orderList = agentOrderLoaderClient.findAgentOrderByStatus(AgentOrderStatus.APPROVED);
        if(CollectionUtils.isEmpty(orderList)){
            return;
        }
        Map<String, List<AgentOrder>> userOrderMap = orderList.stream().filter(p -> p.getInvoiceId() == null)
                .collect(Collectors.groupingBy(p -> StringUtils.join(p.getConsignee(), "_", p.getMobile(), "_", SafeConverter.toString(p.getProvince(), ""), SafeConverter.toString(p.getCity(), ""), SafeConverter.toString(p.getCounty(), ""), p.getAddress())));
        userOrderMap.forEach((k, v) -> {
            // 生成发货单
            AgentInvoice agentInvoice = new AgentInvoice();
            agentInvoice.setLogisticsStatus(AgentLogisticsStatus.PACKAGING);
            AgentOrder firstOrder = v.get(0);
            agentInvoice.setUserId(firstOrder.getRealCreator());
            agentInvoice.setUserName(firstOrder.getCreatorName());
            agentInvoice.setConsignee(firstOrder.getConsignee());
            agentInvoice.setMobile(firstOrder.getMobile());
            agentInvoice.setProvince(SafeConverter.toString(firstOrder.getProvince(), ""));
            agentInvoice.setCity(SafeConverter.toString(firstOrder.getCity(), ""));
            agentInvoice.setCounty(SafeConverter.toString(firstOrder.getCounty(), ""));
            agentInvoice.setAddress(firstOrder.getAddress());
            agentInvoicePersistence.insert(agentInvoice);
            // 生成发货单商品列表
            List<AgentOrderProduct> orderProductList = new ArrayList<>();
            v.stream().forEach(p -> orderProductList.addAll(agentOrderLoaderClient.findAgentOrderProductByOrderId(p.getId())));
            Map<Long, List<AgentOrderProduct>> orderProductMap = orderProductList.stream().collect(Collectors.groupingBy(AgentOrderProduct::getProductId));
            List<AgentInvoiceProduct> invoiceProductList = orderProductMap.entrySet().stream().map(this::createInvoiceProduct).collect(Collectors.toList());
            invoiceProductList.forEach(p -> p.setInvoiceId(agentInvoice.getId()));
            agentInvoiceProductPersistence.inserts(invoiceProductList);

            v.forEach(p -> {
                p.setInvoiceId(agentInvoice.getId());
                p.setOrderStatus(AgentOrderStatus.PENDING_REGION_MANAGER.getStatus());
                agentOrderServiceClient.replaceAgentOrder(p); // 更新订单
            });
        });

    }

    private AgentInvoiceProduct createInvoiceProduct(Map.Entry<Long, List<AgentOrderProduct>> mapEntry){
        Long productId = mapEntry.getKey();
        List<AgentOrderProduct> orderProductList = mapEntry.getValue();
        if(CollectionUtils.isEmpty(orderProductList)){
            return null;
        }
        AgentOrderProduct firstOrderProduct = orderProductList.get(0);
        AgentInvoiceProduct invoiceProduct = new AgentInvoiceProduct();
        invoiceProduct.setProductId(productId);
        invoiceProduct.setProductType(firstOrderProduct.getProductType());
        Integer quantity = orderProductList.stream().map(AgentOrderProduct::getProductQuantity).reduce(0, (a, b) -> a + b);
        invoiceProduct.setProductQuantity(quantity);
        return invoiceProduct;
    }

    // 根据excel更新发货单的物流信息
    public MapMessage updateLogisticsInfoFromExcel(XSSFWorkbook workbook){
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet != null) {
            int rowNo = 1; // 从第二行开始
            while(true){
                XSSFRow row = sheet.getRow(rowNo++);
                if (row == null) {
                    break;
                }

                // 获取发货单
                Long invoiceId = XssfUtils.getLongCellValue(row.getCell(0));
                if (invoiceId == null) {
                    continue;
                }

                AgentInvoice agentInvoice = agentInvoicePersistence.load(invoiceId);
                if(agentInvoice == null){
                    continue;
                }

                String logisticsCompany = StringUtils.trim(XssfUtils.getStringCellValue(row.getCell(1))); // 物流公司
                String logisticsId = StringUtils.trim(XssfUtils.getStringCellValue(row.getCell(2))); // 物流编号
                Float logisticsPrice = XssfUtils.getFloatCellValue(row.getCell(3)); // 物流价格
                String deliveryDateString = XssfUtils.getStringCellValue(row.getCell(4)); // 发货时间
                Date deliveryDate = null;
                if (StringUtils.isEmpty(deliveryDateString)){
                    return MapMessage.errorMessage("第" + rowNo + "行发货日期不能为空");
                }
                try {
                    deliveryDate = DateUtils.stringToDate(deliveryDateString,"yyyyMMdd");
                    if (null != deliveryDateString && deliveryDate == null){
                        return MapMessage.errorMessage("第" + rowNo + "行发货日期不正确");
                    }
                    agentInvoice.setDeliveryDate(deliveryDate);
                }catch (Exception e){
                    return MapMessage.errorMessage("第" + rowNo + "行发货日期不正确");
                }

                String logisticsInfo = "";
                if(StringUtils.isNotBlank(logisticsCompany)){
                    agentInvoice.setLogisticsCompany(logisticsCompany);
                    logisticsInfo = logisticsInfo + "物流公司：" + logisticsCompany;
                }
                if(StringUtils.isNotBlank(logisticsId)){
                    agentInvoice.setLogisticsId(logisticsId);
                    agentInvoice.setLogisticsStatus(AgentLogisticsStatus.DELIVERED);
                    logisticsInfo = logisticsInfo + "，物流单号：" + logisticsId;
                    logisticsInfo = logisticsInfo + "，发货日期：" + DateUtils.dateToString(deliveryDate, "yyyy-MM-dd");
                }
//                Float userLogisticsPriceDiff = 0f;
                if(logisticsPrice != null){
                    //获取原来的物流价格
                    Float preLogisticsPrice = agentInvoice.getLogisticsPrice() == null ? 0f : agentInvoice.getLogisticsPrice();
//                    userLogisticsPriceDiff = logisticsPrice - preLogisticsPrice;
                    agentInvoice.setLogisticsPrice(logisticsPrice);
                }
                agentInvoicePersistence.replace(agentInvoice);


                // 发货员在将物流费回传到系统后，仅保存在系统中即可，不扣除申请人账户余额
//                // 用户账户余额扣去物流价格
//                if(userLogisticsPriceDiff != 0){
//                    String userId = agentInvoice.getUserId();
//                    if(StringUtils.isNotBlank(userId) && !userId.startsWith("admin.")) {
//                        Long agentUserId = SafeConverter.toLong(userId);
//                        AgentUser user = baseOrgService.getUser(agentUserId);
//                        if(user != null){
//                            float newCashAmount = MathUtils.floatSub(user.getCashAmount(), userLogisticsPriceDiff);
//                            user.setCashAmount(newCashAmount < 0 ? 0 : newCashAmount);
//                            float newUsableCashAmount = MathUtils.floatSub(user.getUsableCashAmount(), userLogisticsPriceDiff);
//                            user.setUsableCashAmount(newUsableCashAmount < 0 ? 0 : newUsableCashAmount);
//                            agentUserServiceClient.update(user.getId(), user);
//                        }
//                    }
//                }

                // 更新订单的物流信息
                List<AgentOrder> agentOrderList = agentOrderLoaderClient.findAgentOrderByInvoiceId(invoiceId);
                if(CollectionUtils.isNotEmpty(agentOrderList)){
                    for(AgentOrder order : agentOrderList){
                        order.setLogisticsInfo(logisticsInfo);
                        order.setOrderStatus(AgentOrderStatus.DELIVERED.getStatus());
                    }
                    agentOrderList.forEach(agentOrderServiceClient::replaceAgentOrder);
                    //购买用户发送发货提醒
                    sendNotify(agentOrderList,logisticsCompany,logisticsId);
                }
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 发货提醒
     * @param agentOrderList
     * @param logisticsCompany
     * @param logisticsId
     */
    private void sendNotify(List<AgentOrder> agentOrderList,String logisticsCompany,String logisticsId){
        if (CollectionUtils.isNotEmpty(agentOrderList)){
            agentOrderList.forEach(item -> {
                String message = StringUtils.formatMessage("您于{}提交的订单（订单号：{}，金额：{}元）已发货，快递单号：{}（{}）。",
                        DateUtils.dateToString(item.getOrderTime(), "yyyy年MM月dd日"), item.getId(), item.getOrderAmount(), logisticsId, logisticsCompany);
                agentNotifyService.sendNotify(AgentNotifyType.ORDER_DELIVERY_NOTICE.getType(),"物料发货", message,
                        Collections.singletonList(item.getCreator()),null);
            });
        }
    }


    public List<Map<String, Object>> searchMaterialDetailList(Date startDate, Date endDate){
        List<AgentInvoice> invoiceList = agentInvoicePersistence.findInvoiceList(null, null, null, startDate, endDate);
        if(CollectionUtils.isEmpty(invoiceList)){
            return Collections.emptyList();
        }

        // 将发货单根据用户分组
        Map<String, List<AgentInvoice>> userInvoiceMap = invoiceList.stream().collect(Collectors.groupingBy(AgentInvoice::getUserId));

        // 计算每个人的物流费用
        Map<String, Float> userLogisticsMap = new HashMap<>();
        Map<String, List<AgentOrder>> userOrderListMap = new HashMap<>();
        userInvoiceMap.forEach((k, v) -> {
            //计算每个人的物流费用
            Float logisticsPrice = v.stream().map(AgentInvoice::getLogisticsPrice).filter(p -> p != null).reduce(0f, (x, y) -> MathUtils.floatAdd(x, y));
            userLogisticsMap.put(k, logisticsPrice);
            // 计算每个人对应的订单列表
            List<AgentOrder> agentOrderList = new ArrayList<>();
            v.stream().map(p -> agentOrderLoaderClient.findAgentOrderByInvoiceId(p.getId())).filter(CollectionUtils::isNotEmpty).forEach(agentOrderList::addAll);
            userOrderListMap.put(k, agentOrderList);
        });

        List<Map<String, Object>> retMap = userOrderListMap.entrySet().stream().map(this::generateOrderData).collect(Collectors.toList());
        // 设置物流费用
        retMap.forEach(p -> {
            Float logisticsPrice = userLogisticsMap.get(p.get("userId"));
            if(logisticsPrice != null){
                p.put("logisticsPrice", logisticsPrice);
            }
        });

        return retMap;
    }

    private Map<String, Object> generateOrderData(Map.Entry<String, List<AgentOrder>> userOrderList){
        Map<String, Object> retMap = new HashMap<>();
        String userId = userOrderList.getKey();
        retMap.put("userId", userId);
        if(StringUtils.isNotBlank(userId) && !userId.startsWith("admin.")){
            Long agentUserId = SafeConverter.toLong(userId);
            List<AgentGroup> groupList = baseOrgService.getUserGroups(agentUserId);
            if(CollectionUtils.isNotEmpty(groupList)){
                String groupName = groupList.stream().map(AgentGroup::getGroupName).reduce("", (x, y) -> StringUtils.join(x, ",", y));
                if(StringUtils.isNotBlank(groupName)){
                    groupName = groupName.substring(1);
                }
                retMap.put("groupName", groupName);
            }

            AgentUser agentUser = baseOrgService.getUser(agentUserId);
            if(agentUser != null){
                retMap.put("materielBudget", agentUser.getMaterielBudget());
                retMap.put("usableCashAmount", agentUser.getUsableCashAmount());
            }
        }

        List<AgentOrder> orderList = userOrderList.getValue();
        if(CollectionUtils.isNotEmpty(orderList)){
            for(AgentOrder order : orderList){
                if(StringUtils.isNotBlank(order.getCreatorName())){
                    retMap.put("userName", order.getCreatorName() + "(" + order.getRealCreator() + ")");
                    break;
                }
            }

            List<Map<String, Object>> orderMapList = orderList.stream().map(BeanMapUtils::tansBean2Map).collect(Collectors.toList());
            orderMapList.forEach(p -> {
                List<AgentOrderProduct> orderProductList = agentOrderLoaderClient.findAgentOrderProductByOrderId((Long) p.get("id"));

                if(CollectionUtils.isNotEmpty(orderProductList)){
                    p.put("orderRowCount", orderProductList.size());
                    List<Map<String, Object>> orderProductMapList = orderProductList.stream().map(this::generateOrderProductData).collect(Collectors.toList());
                    p.put("productList", orderProductMapList);
                }else{
                    p.put("orderRowCount", 1);
                }
            });
            retMap.put("orderList", orderMapList);
        }

        int userRowCount = 0;
        List<Map<String, Object>> orderMapList = (List<Map<String, Object>>) retMap.get("orderList");
        if(orderList == null){
            userRowCount = 1;
        }else {
            for (Map<String, Object> item : orderMapList) {
                userRowCount += (Integer)item.get("orderRowCount");
            }
        }
        retMap.put("userRowCount", userRowCount);

        return retMap;
    }

    private Map<String, Object> generateOrderProductData(AgentOrderProduct orderProduct){
        Map<String, Object> retMap = new HashMap<>();
        Map<String, Object> beanMap = BeanMapUtils.tansBean2Map(orderProduct);
        if(beanMap != null){
            retMap.putAll(beanMap);
        }
        AgentProduct agentProduct = agentProductPersistence.load(orderProduct.getProductId());
        if(agentProduct != null){
            retMap.put("productName", agentProduct.getProductName());
            retMap.put("productPrice", agentProduct.getPrice());
            retMap.put("productCacheAmount", MathUtils.doubleMultiply(agentProduct.getPrice(), orderProduct.getProductQuantity()));
        }
        return retMap;
    }

    public List<Map<String, Object>> searchOrderDetailList(Date startDate, Date endDate){
        List<AgentOrder> orderList = new ArrayList<>();
        orderList.addAll(agentOrderLoaderClient.findAgentOrderByOrderTime(AgentOrderStatus.UNCHECKED, startDate, endDate)); // 待审核
        orderList.addAll(agentOrderLoaderClient.findAgentOrderByOrderTime(AgentOrderStatus.APPROVED, startDate, endDate)); // 已通过
        orderList.addAll(agentOrderLoaderClient.findAgentOrderByOrderTime(AgentOrderStatus.REJECTED, startDate, endDate)); // 已拒绝
        orderList.addAll(agentOrderLoaderClient.findAgentOrderByOrderTime(AgentOrderStatus.PENDING_REGION_MANAGER, startDate, endDate));
        orderList.addAll(agentOrderLoaderClient.findAgentOrderByOrderTime(AgentOrderStatus.DELIVERED, startDate, endDate));
        if(CollectionUtils.isEmpty(orderList)){
            return Collections.emptyList();
        }

        List<Map<String, Object>> orderMapList = new ArrayList<>();
        orderList.forEach(item -> {
            Map<String, Object> p = BeanMapUtils.tansBean2Map(item);
            String userId = item.getRealCreator();
            if(StringUtils.isNotBlank(userId) && !userId.startsWith("admin.")){
                Long agentUserId = SafeConverter.toLong(userId);
                List<AgentGroup> groupList = baseOrgService.getUserGroups(agentUserId);
                if(CollectionUtils.isNotEmpty(groupList)){
                    String groupName = groupList.stream().map(AgentGroup::getGroupName).reduce("", (x, y) -> StringUtils.join(x, ",", y));
                    if(StringUtils.isNotBlank(groupName)){
                        groupName = groupName.substring(1);
                    }
                    p.put("groupName", groupName);
                }

                AgentUser agentUser = baseOrgService.getUser(agentUserId);
                if(agentUser != null){
                    p.put("materielBudget", agentUser.getMaterielBudget());
                    p.put("usableCashAmount", agentUser.getUsableCashAmount());
                }
            }
            List<AgentOrderProduct> orderProductList = agentOrderLoaderClient.findAgentOrderProductByOrderId((Long) p.get("id"));
            if(CollectionUtils.isNotEmpty(orderProductList)){
                p.put("orderRowCount", orderProductList.size());
                List<Map<String, Object>> orderProductMapList = orderProductList.stream().map(this::generateOrderProductData).collect(Collectors.toList());
                p.put("productList", orderProductMapList);
            }else{
                p.put("orderRowCount", 1);
            }
            String applyStatusStr = "";
            if (item.getWorkflowId() != null) {
                WorkFlowRecord workFlowRecord = workFlowLoaderClient.loadWorkFlowRecord(item.getWorkflowId());
                if (workFlowRecord != null){
                    if (Objects.equals(workFlowRecord.getStatus(), "init")) {
                        applyStatusStr = "市经理审核中";
                    } else if (Objects.equals(workFlowRecord.getStatus(), "lv1")) {
                        applyStatusStr = "财务审核中";
                    } else if (Objects.equals(workFlowRecord.getStatus(), "lv2")) {
                        applyStatusStr = "销运审核中";
                    } else if (Objects.equals(workFlowRecord.getStatus(), "lv3")) {
                        Integer orderStatus = item.getOrderStatus();
                        AgentOrderStatus agentOrderStatus = AgentOrderStatus.of(orderStatus);
                        if (null != agentOrderStatus && agentOrderStatus != AgentOrderStatus.APPROVED){
                            applyStatusStr = agentOrderStatus.getDesc();
                        }else {
                            applyStatusStr = "审核通过";
                        }
                    } else if (Objects.equals(workFlowRecord.getStatus(), "reject") || Objects.equals(workFlowRecord.getStatus(), "rejected")) {
                        applyStatusStr = "已驳回";
                    } else if (Objects.equals(workFlowRecord.getStatus(), "revoke")) {
                        applyStatusStr = "撤回";
                    }
                }
            }
            p.put("applyStatus", applyStatusStr);
            orderMapList.add(p);
        });
        return orderMapList;
    }


    public List<Map<String, Object>> searchInvoiceDetailList(Date startDate, Date endDate){
        List<AgentInvoice> invoiceList = agentInvoicePersistence.findInvoiceList(null, null, null, startDate, endDate);
        if(CollectionUtils.isEmpty(invoiceList)){
            return Collections.emptyList();
        }
        List<Map<String, Object>> invoiceMapList = invoiceList.stream().map(BeanMapUtils::tansBean2Map).collect(Collectors.toList());
        invoiceMapList.forEach(p -> {
            List<AgentOrder> orderList = agentOrderLoaderClient.findAgentOrderByInvoiceId((Long) p.get("id"));
            if(CollectionUtils.isNotEmpty(orderList)){
                String orderIdListStr = orderList.stream().map(k -> String.valueOf(k.getId())).reduce("", (x, y) -> StringUtils.join(String.valueOf(x), ",", String.valueOf(y)));
                if(StringUtils.isNotBlank(orderIdListStr)){
                    orderIdListStr = orderIdListStr.substring(1);
                }
                p.put("orderIdList", orderIdListStr);
            }

        });
        return invoiceMapList;
    }

    public MapMessage revocationInvoice(Long invoiceId) {
        AgentInvoice invoice = findAgentInvoiceById(invoiceId);
        if (invoice == null) {
            return MapMessage.errorMessage("发货单已经不存在");
        }
        if (Objects.equals(invoice.getLogisticsStatus(), AgentLogisticsStatus.DELIVERED)) {
            return MapMessage.errorMessage("返货单已经在发货中");
        }
        List<AgentOrder> orders = agentOrderLoaderClient.findAgentOrderByInvoiceId(invoiceId);
        if (CollectionUtils.isEmpty(orders)) {
            return MapMessage.errorMessage("订单已经不存在");
        }
        orders.forEach(p -> {
            agentOrderServiceClient.updateAgentOrderInvoiceId(p.getId());
        });
        List<AgentInvoiceProduct> agentInvoiceProducts = agentInvoiceProductPersistence.findByInvoiceId(invoiceId);
        agentInvoiceProducts.forEach(p -> {
            p.setDisabled(true);
            agentInvoiceProductPersistence.upsert(p);
        });
        invoice.setDisabled(true);
        agentInvoicePersistence.replace(invoice);
        return MapMessage.successMessage();
    }


    public AgentInvoice findAgentInvoiceById(Long id) {
        AgentInvoice result = agentInvoicePersistence.load(id);
        return result != null && !result.isDisabled() ? result : null;
    }

    public Map<Long, AgentInvoice> findAgentInvoiceByIds(Collection<Long> ids) {
        Map<Long, AgentInvoice> agentInvoiceMap = agentInvoicePersistence.loads(ids);
        Map<Long, AgentInvoice> result = new HashMap<>();
        agentInvoiceMap.forEach((k,v) -> {
            if (v != null && !v.isDisabled()){
                result.put(k,v);
            }
        });
        return result;
    }
}
