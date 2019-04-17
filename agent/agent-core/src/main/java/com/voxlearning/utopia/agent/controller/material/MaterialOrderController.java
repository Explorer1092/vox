package com.voxlearning.utopia.agent.controller.material;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentLogisticsStatus;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.AgentProductPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentInvoice;
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBudget;
import com.voxlearning.utopia.agent.persist.entity.material.AgentOrderCityCost;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.invoice.AgentInvoiceService;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderPaymentMode;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物料订单
 *
 * @author chunlin.yu
 * @create 2018-02-22 18:14
 **/
@Controller
@RequestMapping(value = "/materialbudget/order")
public class MaterialOrderController extends AbstractAgentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    WorkFlowLoaderClient workFlowLoaderClient;

    @Inject
    AgentOrderLoaderClient agentOrderLoaderClient;

    @Inject
    BaseOrgService baseOrgService;

    @Inject
    AgentProductPersistence agentProductPersistence;

    @Inject
    AgentMaterialBudgetService agentMaterialBudgetService;

    @Inject
    AgentInvoiceService agentInvoiceService;

    @RequestMapping("order.vpage")
    @OperationCode("5476a5f271f54352")
    public String budget(Model model) {
        model.addAttribute("applyStatus", ApplyStatus.values());
        return "/materialbudget/order";
    }

    /**
     * 订单列表
     *
     * @return
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage orderList() {
        MapMessage mapMessage = doSearch();
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        List<AgentOrder> orderList = (List<AgentOrder>) mapMessage.get("orderList");
        List<Map<String, Object>> applyMapList = new ArrayList<>();
        List<Map<String, Object>> materialApplyMapList = orderList.stream().map(p -> this.convertMaterialApply(p)).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(materialApplyMapList)) {
            applyMapList.addAll(materialApplyMapList);
        }
        MapMessage resultMessage = MapMessage.successMessage();
        resultMessage.add("applyMapList", applyMapList);
        return resultMessage;
    }

    private MapMessage doSearch() {
        Date endDate = requestDate("endDate");
        Date startDate = requestDate("startDate");
        String orderCreator = requestString("creator");
        Long orderId = requestLong("orderId");
        if (null == endDate || startDate == null) {
            return MapMessage.errorMessage("请输入开始日期和结束日期");
        }
        if (startDate.before(DateUtils.addDays(endDate, -31))) {
            return MapMessage.errorMessage("查询时间范围不能大于31天");
        }
        Set<Long> userIds = new HashSet<>();
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isBusinessDeveloper()) {
            userIds.add(getCurrentUserId());
        } else {
            List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(getCurrentUserId());
            List<AgentGroupUser> allGroupUsersByGroupId = baseOrgService.getAllGroupUsersByGroupId(groupUserByUser.get(0).getGroupId());
            List<AgentUser> managedGroupUsers = baseOrgService.getUsers(allGroupUsersByGroupId.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
            userIds = managedGroupUsers.stream().map(AgentUser::getId).collect(Collectors.toSet());

            if (null != orderCreator) {
                userIds.clear();
                for (int i = 0; i < managedGroupUsers.size(); i++) {
                    if (managedGroupUsers.get(i).getRealName().equals(orderCreator)) {
                        userIds.add(managedGroupUsers.get(i).getId());
                    }
                }
                if (CollectionUtils.isEmpty(userIds)) {
                    return MapMessage.errorMessage("您如输入的用户不存在或者不在您的管理下");
                }
            }
        }
        String applyStatusStr = requestString("applyStatus");
        ApplyStatus applyStatus = ApplyStatus.nameOf(applyStatusStr);
        List<AgentOrder> orderList = agentOrderLoaderClient.loads(startDate, DateUtils.addDays(endDate, 1), userIds, orderId, applyStatus);
        return MapMessage.successMessage().add("orderList", orderList);
    }

    @RequestMapping(value = "exportOrder.vpage", method = RequestMethod.GET)
    public void exportOrder(HttpServletResponse response) {
        try {
            MapMessage mapMessage = doSearch();
            if (!mapMessage.isSuccess()) {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                response.getWriter().write(mapMessage.getInfo());
                return;
            }
            List<AgentOrder> orderList = (List<AgentOrder>) mapMessage.get("orderList");
            List<Map<String, Object>> orderMapList = dealOrderDetailList(orderList);
            SXSSFWorkbook workbook = generateOrderWorkBook(orderMapList);
            if (workbook == null) {
                getResponse().getWriter().write("下载失败");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String filename = "订单列表----" + DateUtils.dateToString(new Date(), "yyyy-MM-dd hh:mm:ss") + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
                outStream.close();
                workbook.dispose();
            } catch (IOException ignored) {
                getResponse().getWriter().write("不能下载");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception ex) {
            logger.error("export school dict info is failed", ex);
        }
    }


    public List<Map<String, Object>> dealOrderDetailList(List<AgentOrder> orderList) {
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> orderMapList = new ArrayList<>();
        Map<Long, List<AgentGroup>> userGroups = baseOrgService.getUserGroups(orderList.stream().map(item -> SafeConverter.toLong(item.getRealCreator())).collect(Collectors.toSet()));
        Map<Long, List<AgentOrderProduct>> orderProductListMap = agentOrderLoaderClient.findAgentOrderProductByOrderIds(orderList.stream().map(item -> SafeConverter.toLong(item.getId())).collect(Collectors.toList()));
        Map<Long, WorkFlowRecord> workFlowRecordMap = workFlowLoaderClient.loadWorkFlowRecords(orderList.stream().filter(item -> null != item.getWorkflowId()).map(AgentOrder::getWorkflowId).collect(Collectors.toList()));
        Map<Long, AgentInvoice> agentInvoiceMap = agentInvoiceService.findAgentInvoiceByIds(orderList.stream().filter(item -> null != item.getInvoiceId()).map(AgentOrder::getInvoiceId).collect(Collectors.toList()));
        orderList.forEach((AgentOrder item) -> {
            Map<String, Object> p = BeanMapUtils.tansBean2Map(item);
            String userId = item.getRealCreator();
            if (StringUtils.isNotBlank(userId) && !userId.startsWith("admin.")) {
                Long agentUserId = SafeConverter.toLong(userId);
                List<AgentGroup> groupList = userGroups.get(agentUserId);
                if (CollectionUtils.isNotEmpty(groupList)) {
                    String groupName = groupList.stream().map(AgentGroup::getGroupName).reduce("", (x, y) -> StringUtils.join(x, ",", y));
                    if (StringUtils.isNotBlank(groupName)) {
                        groupName = groupName.substring(1);
                    }
                    p.put("groupName", groupName);
                }
            }
            List<AgentOrderProduct> orderProductList = orderProductListMap.get((Long) p.get("id"));
            if (CollectionUtils.isNotEmpty(orderProductList)) {
                p.put("orderRowCount", orderProductList.size());
                List<Map<String, Object>> orderProductMapList = orderProductList.stream().map(this::generateOrderProductData).collect(Collectors.toList());
                p.put("productList", orderProductMapList);
            } else {
                p.put("orderRowCount", 1);
            }
            String applyStatusStr = "";
            if (item.getWorkflowId() != null) {
                WorkFlowRecord workFlowRecord = workFlowRecordMap.get(item.getWorkflowId());
                if (workFlowRecord != null) {
                    if (Objects.equals(workFlowRecord.getStatus(), "init")) {
                        applyStatusStr = "市经理审核中";
                    } else if (Objects.equals(workFlowRecord.getStatus(), "lv1")) {
                        applyStatusStr = "财务审核中";
                    } else if (Objects.equals(workFlowRecord.getStatus(), "lv2")) {
                        applyStatusStr = "销运审核中";
                    } else if (Objects.equals(workFlowRecord.getStatus(), "lv3")) {
                        Integer orderStatus = item.getOrderStatus();
                        AgentOrderStatus agentOrderStatus = AgentOrderStatus.of(orderStatus);
                        if (null != agentOrderStatus && agentOrderStatus != AgentOrderStatus.APPROVED) {
                            applyStatusStr = agentOrderStatus.getDesc();
                        } else {
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
            if (AgentOrderPaymentMode.CITY_COST.getPayId().equals(item.getPaymentMode())) {
                AgentOrderCityCost agentOrderCityCost = agentMaterialBudgetService.getAgentOrderCityCost(item.getId());
                if (null != agentOrderCityCost) {
                    Integer regionCode = agentOrderCityCost.getRegionCode();
                    ExRegion regionFromBuffer = raikouSystem.loadRegion(regionCode);
                    if (null != regionFromBuffer) {
                        Map<String, Double> costs = agentOrderCityCost.getCosts();
                        StringBuilder sb = new StringBuilder();
                        if (MapUtils.isNotEmpty(costs)) {
                            costs.forEach((k, v) -> {
                                AgentMaterialBudget agentMaterialBudget = agentMaterialBudgetService.getAgentMaterialBudget(k);
                                if (null != agentMaterialBudget) {
                                    sb.append(regionFromBuffer.getCityName()).append(agentMaterialBudget.getMonth()).append("月").append(v).append("元；");
                                }
                            });
                        }
                        p.put("cityCosts", sb.toString());
                    }
                }
            }
            if (item.getInvoiceId() != null) {
                AgentInvoice agentInvoice = agentInvoiceMap.get(item.getInvoiceId());
                if (null != agentInvoice) {
                    Date deliveryDate = agentInvoice.getDeliveryDate();
                    if (null != deliveryDate) {
                        p.put("deliveryDate", DateUtils.dateToString(deliveryDate, "yyyy-MM-dd"));
                    }
                    AgentLogisticsStatus logisticsStatus = agentInvoice.getLogisticsStatus();
                    if (null != logisticsStatus) {
                        p.put("logisticsStatus", logisticsStatus.getValue());
                    }
                    p.put("logisticsId", agentInvoice.getLogisticsId());
                    p.put("logisticsCompany", agentInvoice.getLogisticsCompany());
                    p.put("logisticsPrice", agentInvoice.getLogisticsPrice());
                }
            }


            orderMapList.add(p);
        });
        return orderMapList;
    }


    private Map<String, Object> generateOrderProductData(AgentOrderProduct orderProduct) {
        Map<String, Object> retMap = new HashMap<>();
        Map<String, Object> beanMap = BeanMapUtils.tansBean2Map(orderProduct);
        if (beanMap != null) {
            retMap.putAll(beanMap);
        }
        AgentProduct agentProduct = agentProductPersistence.load(orderProduct.getProductId());
        if (agentProduct != null) {
            retMap.put("productName", agentProduct.getProductName());
            retMap.put("productPrice", agentProduct.getPrice());
            retMap.put("productCacheAmount", MathUtils.doubleMultiply(agentProduct.getPrice(), orderProduct.getProductQuantity()));
        }
        return retMap;
    }


    private SXSSFWorkbook generateOrderWorkBook(List<Map<String, Object>> dataList) {
        try {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            SXSSFSheet sheet = workbook.createSheet();
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 3000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 5000);
            sheet.setColumnWidth(7, 5000);
            sheet.setColumnWidth(8, 5000);
            sheet.setColumnWidth(9, 10000);
            sheet.setColumnWidth(10, 5000);
            sheet.setColumnWidth(11, 5000);
            sheet.setColumnWidth(12, 5000);
            sheet.setColumnWidth(13, 5000);
            sheet.setColumnWidth(15, 5000);
            sheet.setColumnWidth(16, 5000);
            sheet.setColumnWidth(17, 5000);
            sheet.setColumnWidth(18, 5000);
            sheet.setColumnWidth(19, 5000);
            sheet.setColumnWidth(20, 5000);
            sheet.setColumnWidth(21, 5000);
            sheet.setColumnWidth(22, 5000);
            sheet.setColumnWidth(23, 5000);

            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setWrapText(true);
            cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
            SXSSFRow firstRow = sheet.createRow(0);
            XssfUtils.setCellValue(firstRow, 0, cellStyle, "申请日期");
            XssfUtils.setCellValue(firstRow, 1, cellStyle, "订单号");
            XssfUtils.setCellValue(firstRow, 2, cellStyle, "部门");
            XssfUtils.setCellValue(firstRow, 3, cellStyle, "申请人");
            XssfUtils.setCellValue(firstRow, 4, cellStyle, "申请品类");
            XssfUtils.setCellValue(firstRow, 5, cellStyle, "合计");
            XssfUtils.setCellValue(firstRow, 6, cellStyle, "支付方式");

            XssfUtils.setCellValue(firstRow, 7, cellStyle, "城市费用支付明细");
            XssfUtils.setCellValue(firstRow, 8, cellStyle, "审核状态");
            XssfUtils.setCellValue(firstRow, 9, cellStyle, "发货单号");
            XssfUtils.setCellValue(firstRow, 10, cellStyle, "收货人");
            XssfUtils.setCellValue(firstRow, 11, cellStyle, "收货人电话");
            XssfUtils.setCellValue(firstRow, 12, cellStyle, "省");
            XssfUtils.setCellValue(firstRow, 13, cellStyle, "市");
            XssfUtils.setCellValue(firstRow, 14, cellStyle, "区");
            XssfUtils.setCellValue(firstRow, 15, cellStyle, "收货人地址");

            XssfUtils.setCellValue(firstRow, 16, cellStyle, "发货日期");
            XssfUtils.setCellValue(firstRow, 17, cellStyle, "物流状态");
            XssfUtils.setCellValue(firstRow, 18, cellStyle, "物流单号");
            XssfUtils.setCellValue(firstRow, 19, cellStyle, "物流公司");
            XssfUtils.setCellValue(firstRow, 20, cellStyle, "物流费用");
            if (CollectionUtils.isNotEmpty(dataList)) {
                Integer index = 1;
                for (int no = 0; no < dataList.size(); no++) {
                    Map<String, Object> item = dataList.get(no);
                    SXSSFRow row = sheet.createRow(index++);
                    XssfUtils.setCellValue(row, 0, cellStyle, item.get("orderTime") == null ? "" : DateUtils.dateToString((Date) item.get("orderTime"), "yyyy-MM-dd"));
                    XssfUtils.setCellValue(row, 1, cellStyle, (Long) item.get("id"));
                    XssfUtils.setCellValue(row, 2, cellStyle, item.get("groupName") == null ? "" : (String) item.get("groupName"));
                    XssfUtils.setCellValue(row, 3, cellStyle, item.get("creatorName") == null ? "" : (String) item.get("creatorName"));


                    List<Map<String, Object>> orderProductMapList = (List<Map<String, Object>>) item.get("productList");
                    List<String> productStrList = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(orderProductMapList)) {
                        for (Map<String, Object> p : orderProductMapList) {
                            Float productPrice = SafeConverter.toFloat(p.get("productPrice"));
                            float productCacheAmount = SafeConverter.toFloat(p.get("productCacheAmount"));
                            StringBuilder sb = new StringBuilder();
                            sb.append((String) p.get("productName")).append("：").append(productPrice).append("元*")
                                    .append((Integer) p.get("productQuantity")).append("件=")
                                    .append(productCacheAmount);
                            productStrList.add(sb.toString());
                        }
                    }
                    XssfUtils.setCellValue(row, 4, cellStyle, StringUtils.join(productStrList, "\n"));


                    Float orderAmount = (Float) item.get("orderAmount");
                    XssfUtils.setCellValue(row, 5, cellStyle, orderAmount == null ? 0 : orderAmount);
                    Integer paymentMode = (Integer) item.get("paymentMode");
                    String paymentModeStr = "";
                    if (paymentMode != null) {
                        if (paymentMode == 1) {
                            paymentModeStr = "物料费用";
                        } else if (paymentMode == 2) {
                            paymentModeStr = "城市支持费用";
                        } else if (paymentMode == 3) {
                            paymentModeStr = "自付";
                        }
                    }
                    XssfUtils.setCellValue(row, 6, cellStyle, paymentModeStr);
                    XssfUtils.setCellValue(row, 7, cellStyle, item.get("cityCosts") == null ? "" : (String) item.get("cityCosts"));
                    Object applyStatus = item.get("applyStatus");
                    XssfUtils.setCellValue(row, 8, cellStyle, applyStatus == null ? "" : String.valueOf(applyStatus));
                    XssfUtils.setCellValue(row, 9, cellStyle, item.get("invoiceId") == null ? "" : String.valueOf(item.get("invoiceId")));
                    XssfUtils.setCellValue(row, 10, cellStyle, item.get("consignee") == null ? "" : String.valueOf(item.get("consignee")));
                    XssfUtils.setCellValue(row, 11, cellStyle, item.get("mobile") == null ? "" : String.valueOf(item.get("mobile")));
                    XssfUtils.setCellValue(row, 12, cellStyle, item.get("province") == null ? "" : String.valueOf(item.get("province")));
                    XssfUtils.setCellValue(row, 13, cellStyle, item.get("city") == null ? "" : String.valueOf(item.get("city")));
                    XssfUtils.setCellValue(row, 14, cellStyle, item.get("county") == null ? "" : String.valueOf(item.get("county")));
                    XssfUtils.setCellValue(row, 15, cellStyle, item.get("address") == null ? "" : String.valueOf(item.get("address")));


                    XssfUtils.setCellValue(row, 16, cellStyle, item.get("deliveryDate") == null ? "" : String.valueOf(item.get("deliveryDate")));
                    XssfUtils.setCellValue(row, 17, cellStyle, item.get("logisticsStatus") == null ? "" : String.valueOf(item.get("logisticsStatus")));
                    XssfUtils.setCellValue(row, 18, cellStyle, item.get("logisticsId") == null ? "" : String.valueOf(item.get("logisticsId")));
                    XssfUtils.setCellValue(row, 19, cellStyle, item.get("logisticsCompany") == null ? "" : String.valueOf(item.get("logisticsCompany")));
                    XssfUtils.setCellValue(row, 20, cellStyle, item.get("logisticsPrice") == null ? "" : String.valueOf(item.get("logisticsPrice")));
                }
            }
            return workbook;
        } catch (Exception ex) {
            logger.error("generateWorkBook error:", ex);
            return null;
        }
    }

    private void mergeCell(Sheet sheet, int startRow, int endRow, int startCol, int endCol) {


        sheet.addMergedRegion(new CellRangeAddress(
                startRow, //first row (0-based)
                endRow, //last row  (0-based)
                startCol, //first column (0-based)
                endCol  //last column  (0-based)
        ));
    }

    private Map<String, Object> convertMaterialApply(AgentOrder apply) {
        if (apply == null) {
            return null;
        }
        AgentOrderType orderType = AgentOrderType.of(apply.getOrderType());
        if (orderType == null || AgentOrderType.BUY_MATERIAL != orderType) {
            return null;
        }

        Map<String, Object> retMap = BeanMapUtils.tansBean2Map(apply);
        String groupName = "";
        String parentGroupName = "";
        if (apply.getAccount() != null) {
            Map<String, String> result = getOrgInfoByUserId(SafeConverter.toLong(apply.getAccount()));
            groupName = result.get("groupName");
            parentGroupName = result.get("parentGroupName");
        }
        retMap.put("groupName", groupName);
        retMap.put("parentGroupName", parentGroupName);
        List<AgentOrderProduct> orderProducts = agentOrderLoaderClient.findAgentOrderProductByOrderId(SafeConverter.toLong(retMap.get("id")));
        retMap.put("orderProducts", createProductInfo(orderProducts));
        retMap.put("consigneeInfo", createConsigneeInfo(apply));

        ApplyStatus applyStatus = apply.getStatus();
        String applyStatusStr = "";
        if (applyStatus != null) {
            applyStatusStr = applyStatus.getDesc();
        }
        if (apply.getWorkflowId() != null) {
            WorkFlowRecord workFlowRecord = workFlowLoaderClient.loadWorkFlowRecord(apply.getWorkflowId());
            if (Objects.equals(workFlowRecord.getStatus(), "init")) {
                applyStatusStr = "市经理审核中";
            } else if (Objects.equals(workFlowRecord.getStatus(), "lv1")) {
                applyStatusStr = "财务审核中";
            } else if (Objects.equals(workFlowRecord.getStatus(), "lv2")) {
                applyStatusStr = "销运审核中";
            } else if (Objects.equals(workFlowRecord.getStatus(), "lv3")) {
                applyStatusStr = "审核通过";
            } else if (Objects.equals(workFlowRecord.getStatus(), "reject") || Objects.equals(workFlowRecord.getStatus(), "rejected")) {
                applyStatusStr = "已驳回";
            } else if (Objects.equals(workFlowRecord.getStatus(), "revoke")) {
                applyStatusStr = "撤回";
            }
        }
        retMap.put("applyStatus", applyStatusStr);


        //fixme 产品展示
        return retMap;
    }

    private Map<String, String> getOrgInfoByUserId(Long userId) {
        Map<String, String> result = new HashMap<>();
        List<AgentGroup> groupList = baseOrgService.getUserGroups(userId);
        if (CollectionUtils.isNotEmpty(groupList)) {
            AgentGroup group = groupList.get(0);
            result.put("groupName", group.getGroupName());
            AgentGroup parentGroup = baseOrgService.getParentGroupByRole(group.getId(), AgentGroupRoleType.Region);
            result.put("parentGroupName", parentGroup == null ? "" : parentGroup.getGroupName());
        }
        return result;
    }

    private List<String> createProductInfo(List<AgentOrderProduct> orderProducts) {
        List<String> productInfo = new ArrayList<>();
        orderProducts.forEach(p -> productInfo.add(StringUtils.formatMessage("{}:{}元*{}，{}元 ", p.getProductName(), p.getPrice(), p.getProductQuantity(), p.getPrice() * p.getProductQuantity())));
        return productInfo;
    }

    private List<String> createConsigneeInfo(AgentOrder apply) {
        List<String> consigneeInfo = new ArrayList<>();
        consigneeInfo.add("收货人：" +
                apply.getConsignee());
        consigneeInfo.add("收货地址："
                + (apply.getProvince() == null ? "" : apply.getProvince())
                + (apply.getCity() == null ? "" : apply.getCity())
                + (apply.getCounty() == null ? "" : apply.getCounty())
                + apply.getAddress());
        consigneeInfo.add("联系电话：" +
                apply.getMobile());
        return consigneeInfo;
    }
}
