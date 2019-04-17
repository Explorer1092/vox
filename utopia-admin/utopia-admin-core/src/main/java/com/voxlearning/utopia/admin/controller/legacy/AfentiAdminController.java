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

package com.voxlearning.utopia.admin.controller.legacy;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.support.WorkbookUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.afenti.AfentiOrder;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.payment.WechatAbstractPaymentGateway;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.afenti.client.UserPicBookServiceClient;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.business.consumer.BusinessUserOrderServiceClient;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.CommonConfig;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.coupon.api.entities.Coupon;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.constants.*;
import com.voxlearning.utopia.service.order.api.entity.*;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.order.api.util.UserOrderUtil;
import com.voxlearning.utopia.service.order.client.AfentiOrderServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderRefundServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.piclisten.api.PicListenBookOrderService;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowRefer;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowType;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.FinanceFlowContext;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.utopia.service.vendor.api.constant.TextBookSdkType;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.wonderland.api.entity.WonderlandPromotionChannel;
import com.voxlearning.utopia.service.wonderland.client.WonderlandPromotionChannelServiceClient;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/legacy/afenti")
@NoArgsConstructor
@SuppressWarnings("deprecation")
public class AfentiAdminController extends AbstractAdminLegacyController {

    public static final String CONFIG_CATEGORY_NAME = "PRIMARY_PLATFORM_GENERAL";
    public static final String CONFIG_KEY_ORDER_REFUND_NOTIFY_EMAIL_THIRD_PARTY = "ORDER_REFUND_EMAIL_THIRD";
    public static final String CONFIG_KEY_ORDER_REFUND_NOTIFY_EMAIL_CC = "ORDER_REFUND_EMAIL_CC";
    private static final String EXAMPLE_PATH = "/config/templates/loisticsInfo_template.xlsx";

    private static final Map<String, Double> chipsRefundTextMap = new HashMap<>();
//该订单已经退款200元!
    static {
        chipsRefundTextMap.put("54410185509427358_58", 200.0);
        chipsRefundTextMap.put("54359126009774439_39", 200.0);
        chipsRefundTextMap.put("54409305014207915_15", 200.0);
        chipsRefundTextMap.put("54442846571341688_88", 200.0);
        chipsRefundTextMap.put("5440881959172777_7", 200.0);
        chipsRefundTextMap.put("54427396898678086_86", 200.0);
        chipsRefundTextMap.put("54398095114034032_32", 200.0);
        chipsRefundTextMap.put("54432558976567010_10", 200.0);
        chipsRefundTextMap.put("54436362986147570_70", 200.0);
        chipsRefundTextMap.put("5455393015449280_0", 200.0);
        chipsRefundTextMap.put("54470696636740736_36", 200.0);
        chipsRefundTextMap.put("5436753689454838_8", 200.0);
        chipsRefundTextMap.put("54339026269911741_41", 100.0);
        chipsRefundTextMap.put("54332770479930741_41", 100.0);
    }

    private static List<OrderProductServiceType> productTypeList = OrderProductServiceType.getAllValidTypes()
            .stream().filter(t -> t != OrderProductServiceType.Unknown && !t.isOrderClosed())
            .collect(Collectors.toList());
    private static List<PaymentStatus> paymentStatusList = Arrays.asList(PaymentStatus.values());

    private static List<OrderProductServiceType> levelReadingProductTypes = Arrays.asList(OrderProductServiceType.ELevelReading, OrderProductServiceType.CLevelReading, OrderProductServiceType.MLevelReading);

    @ImportService(interfaceClass = PicListenBookOrderService.class)
    private PicListenBookOrderService picListenBookOrderService;
    @Inject
    private TextBookManagementLoaderClient textBookManagementLoaderClient;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private BusinessUserOrderServiceClient businessUserOrderServiceClient;
    @Inject
    private UserOrderServiceClient userOrderServiceClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private CouponLoaderClient couponLoaderClient;
    @Inject
    private FinanceServiceClient financeServiceClient;
    @Inject
    private AfentiOrderServiceClient afentiOrderServiceClient;
    @Inject
    private PaymentGatewayManager paymentGatewayManager;
    @Inject
    private WonderlandPromotionChannelServiceClient wonderlandPromotionChannelServiceClient;
    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;
    @Inject
    private UserPicBookServiceClient userPicBookServiceClient;
    @Inject
    private UserOrderRefundServiceClient userOrderRefundServiceClient;

    @Inject
    private SmsServiceClient smsServiceClient;

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    @RequestMapping(value = "main.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String main(Model model) {
        int activeTab = getRequestInt("activeTab");
        Long userId = getRequestLong("userId");
        String orderId = getRequestString("orderId");
        OrderProductServiceType productType = OrderProductServiceType.safeParse(getRequestString("type"));
        String paymentStatus = getRequestString("paymentStatus");
        String startDate = getRequestString("startDate");

        String endDate = getRequestString("endDate");
        Date dateStart = StringUtils.isBlank(startDate) ? null : DayRange.newInstance(DateUtils.stringToDate(startDate, "yyyy-MM-dd").getTime()).getStartDate();
        Date dateEnd = StringUtils.isBlank(endDate) ? null : DayRange.newInstance(DateUtils.stringToDate(endDate, "yyyy-MM-dd").getTime()).getEndDate();
        User user = null;
        UserOrder userOrder;
        if (StringUtils.isNotBlank(orderId)) {
            userOrder = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
            if (userOrder != null) {
                userId = userOrder.getUserId();
            }
        }
        if (userId != null && userId != 0L) {
            user = userLoaderClient.loadUser(userId);
        }
        List<UserOrder> userOrderList = new ArrayList<>();
        List<UserActivatedProduct> userActivatedProducts = new ArrayList<>();
        List<UserOrderPaymentHistory> userOrderPaymentHistoryList = new ArrayList<>();
        // 订单查询增加过滤条件 By Wyc 2016-08-09
        if (user != null) {
            userOrderList = userOrderLoaderClient.loadUserOrderListIncludedCanceled(userId).stream()
                    .filter(o -> o.getOrderProductServiceType() != null)
                    .filter(o -> StringUtils.isBlank(paymentStatus) || PaymentStatus.valueOf(paymentStatus) == o.getPaymentStatus())
                    .filter(o -> productType == OrderProductServiceType.Unknown || OrderProductServiceType.safeParse(o.getOrderProductServiceType()) == productType)
                    .filter(o -> dateStart == null || o.getUpdateDatetime().after(dateStart))
                    .filter(o -> dateEnd == null || o.getUpdateDatetime().before(dateEnd))
                    .sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())).collect(Collectors.toList());
            userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(userId).stream()
                    .filter(p -> productType == OrderProductServiceType.Unknown || OrderProductServiceType.safeParse(p.getProductServiceType()) == productType)
                    .sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime())).collect(Collectors.toList());
            userOrderPaymentHistoryList = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId).stream()
                    .filter(h -> StringUtils.isBlank(paymentStatus) || h.getPaymentStatus() == PaymentStatus.valueOf(paymentStatus))
                    .sorted((o1, o2) -> o2.getPayDatetime().compareTo(o1.getPayDatetime())).collect(Collectors.toList());
        }
        List<UserOrder> userOldOrderList = new ArrayList<>();
        List<UserOrder> userNewOrderList = new ArrayList<>();
        for (UserOrder order : userOrderList) {
            if (StringUtils.isBlank(order.getOldOrderId())) {
                userNewOrderList.add(order);
            } else {
                userOldOrderList.add(order);
            }
        }
        //添加批量退款订单的列表，按摊销表去统计记录
        List<Map<String, Object>> userOrderListResult = mapAfentiOrderList(userNewOrderList, userOrderPaymentHistoryList);
        List<Map<String, Object>> batchRefundOrderList = getBatchRefundOrderList(userOrderListResult);

        model.addAttribute("user", user);
        model.addAttribute("orderId", orderId);
        model.addAttribute("userOrderPaymentHistoryList", userOrderPaymentHistoryList);
        model.addAttribute("userOrderList", userOrderListResult);
        model.addAttribute("userOldOrderList", mapAfentiOldOrderList(userOldOrderList));
        model.addAttribute("batchRefundOrderList", batchRefundOrderList);
        model.addAttribute("userActivatedProducts", userActivatedProducts);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("type", productType);
        // 添加排序
        Collections.sort(productTypeList, (o1, o2) -> o1.name().compareTo(o2.name()));
        model.addAttribute("productTypeList", productTypeList);
        model.addAttribute("paymentStatusList", paymentStatusList);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "legacy/afenti/main";
    }

    private List<Map<String, Object>> getBatchRefundOrderList(List<Map<String, Object>> userOrderListResult) {
        List<Map<String, Object>> batchRefundOrderList = new LinkedList<>();
        List<Map<String, Object>> zstbatchRefundOrderList = new LinkedList<>();

        for (Map<String, Object> tempOrder : userOrderListResult) {
            String userOrderId = (String) tempOrder.get("id");
            UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(userOrderId);
            if (null != order) {
                // 获取付费记录
                List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId())
                        .stream()
                        .filter(e -> StringUtils.equals(e.getOrderId(), order.getId()))
                        .collect(Collectors.toList());
                UserOrderPaymentHistory paymentHistory = paymentHistories
                        .stream()
                        .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                        .findFirst().orElse(null);
                Calendar c = Calendar.getInstance();
                c.add(Calendar.MONTH, -12);
                if (paymentHistory == null) {
                    continue;
                }
                if (paymentHistory.getPayDatetime().before(c.getTime())) {
                    continue;
                }
                if (paymentHistory != null) {
                    MapMessage result = userOrderRefundServiceClient.getUserOrderRefundService().loadOrderRefundDetail(order.getUserId(), order.getId());
                    if (result.isSuccess()) {
                        List<Map<String, Object>> itemList = (List<Map<String, Object>>) result.get("itemList");
                        int itemListSize = itemList.size();
                        String refundType = (String) result.get("type");
                        Map<String, List<ChipsEnglishProductTimetable.Course>> itemToCourseMap;
                        if (order.getOrderType() == OrderType.chips_english) {
                            Map<String, List<OrderProductItem>> productToItemMap = loadOrderProductItem(order);
                            itemToCourseMap = itemToTimeTableCourse(productToItemMap);
                        } else {
                            itemToCourseMap = Collections.emptyMap();
                        }
                        for (int i = itemList.size() - 1; i >= 0; i--) {
                            Map<String, Object> refundOrderMap = new LinkedHashMap<>();
                            refundOrderMap.put("orderId", userOrderId);
                            Map<String, Object> itemTemp = itemList.get(i);
                            String itemId = (String) itemTemp.get("itemId");
                            OrderProductItem productItem = userOrderLoaderClient.loadOrderProductItemById(itemId);
                            if (productItem == null) {
                                logger.info("orderId:{},itemId:{}", userOrderId, itemId);
                                continue;
                            }
                            refundOrderMap.put("itemId", itemId);
                            refundOrderMap.put("itemName", itemTemp.get("itemName"));
                            refundOrderMap.put("itemType", itemTemp.get("itemType"));
                            refundOrderMap.put("serviceStartTime", itemTemp.get("serviceStartTime"));
                            refundOrderMap.put("serviceEndTime", itemTemp.get("serviceEndTime"));
                            BigDecimal payAmount = (BigDecimal) itemTemp.get("payAmount");
                            refundOrderMap.put("payAmount", payAmount);
                            if (payAmount.compareTo(BigDecimal.ZERO) == 0) {
                                itemList.remove(itemTemp);
                                continue;
                            }
                            Object refunded = itemTemp.get("refunded");
                            if (refunded == null || SafeConverter.toBoolean(refunded)) {
                                continue;
                            }

                            refundOrderMap.put("payMethod", paymentHistory.getPayMethod());
                            refundOrderMap.put("payDate", paymentHistory.getPayDatetime());
                            //时间类的折算金额才会有数据
                            if ("yiqixueBased".equals(refundType)) {
                                refundOrderMap.put("salesType", "ITEM_BASED");
                                refundOrderMap.put("convertAmount", "-");
                                refundOrderMap.put("refundableAmount", payAmount);
                                refundOrderMap.put("refundAmount", payAmount);
                            } else {
                                if (productItem.getSalesType() == OrderProductSalesType.TIME_BASED) {
                                    refundOrderMap.put("salesType", "TIME_BASED");
                                    if (order.getOrderType() == OrderType.chips_english) {
                                        Double refund = chipsRefundTextMap.get(order.genUserOrderId());
                                        List<ChipsEnglishProductTimetable.Course> courseList = Optional.ofNullable(itemTemp.get("itemId"))
                                                .map(e -> itemToCourseMap.get(e.toString())).orElse(Collections.emptyList());
                                        Double itemRefund = Optional.ofNullable(refund).map(e -> e / itemListSize).orElse(0.0);
                                        BigDecimal chipsRefundAmount = loadOrderRefundDetail(payAmount.subtract(BigDecimal.valueOf(itemRefund)), courseList);
                                        refundOrderMap.put("refundableAmount", chipsRefundAmount);
                                        refundOrderMap.put("convertAmount", chipsRefundAmount);
                                        refundOrderMap.put("refundAmount", chipsRefundAmount);
                                    } else {
                                        BigDecimal convertAmount = (BigDecimal) itemTemp.get("refundableAmount");
                                        refundOrderMap.put("convertAmount", convertAmount);
                                        refundOrderMap.put("refundableAmount", itemTemp.get("refundableAmount"));
                                        refundOrderMap.put("refundAmount", itemTemp.get("refundableAmount"));
                                    }
                                } else {
                                    refundOrderMap.put("salesType", "ITEM_BASED");
                                    refundOrderMap.put("convertAmount", "-");
                                    refundOrderMap.put("refundableAmount", itemTemp.get("refundableAmount"));
                                    refundOrderMap.put("refundAmount", itemTemp.get("refundableAmount"));
                                }
                            }
                            //已退金额
                            BigDecimal refundedAmount = (BigDecimal) itemTemp.get("refundedAmount");
                            refundOrderMap.put("refundedAmount", refundedAmount);
                            zstbatchRefundOrderList.add(refundOrderMap);
                        }
                    }
                }
            }
        }
        //将订单做成全额退的列表
        for (Map<String, Object> refundMap : zstbatchRefundOrderList) {
            refundMap.put("red", "0");//1为标红色，0不变
            Map<String, Object> qetRefundMap = new LinkedHashMap<>();
            qetRefundMap.putAll(refundMap);
            BigDecimal refundAmount = (BigDecimal) refundMap.get("refundAmount");
            if (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                refundMap.put("refundType", "1");
                Date serviceEndTime = (Date) refundMap.get("serviceEndTime");
                String salesType = (String) refundMap.get("salesType");
                if ("TIME_BASED".equals(salesType)) {
                    if (serviceEndTime != null && serviceEndTime.after(new Date())) {
                        batchRefundOrderList.add(refundMap);//折算退，如果过期了就不显示了
                    }
                } else {
                    batchRefundOrderList.add(refundMap);
                }
            }
            BigDecimal payAmount = (BigDecimal) refundMap.get("payAmount");
            BigDecimal refundedAmount = (BigDecimal) refundMap.get("refundedAmount");
            BigDecimal qetRefundAmount = payAmount.subtract(refundedAmount);
            qetRefundMap.put("refundAmount", qetRefundAmount);
            qetRefundMap.put("refundType", "2");
            String salesType = (String) qetRefundMap.get("salesType");
            Date serviceEndTime = (Date) qetRefundMap.get("serviceEndTime");
            if ("TIME_BASED".equals(salesType) && serviceEndTime != null && serviceEndTime.before(new Date())) {
                qetRefundMap.put("red", "1");
            }
            batchRefundOrderList.add(qetRefundMap);
        }
        return batchRefundOrderList;
    }

    /**
     * 物流信息模板下载
     *
     * @param response
     */
    @RequestMapping(value = "downloadExample.vpage", method = RequestMethod.GET)
    public void downloadExample(HttpServletResponse response) {
        try {
            Resource resource = new ClassPathResource(EXAMPLE_PATH);
            if (!resource.exists()) {
                logger.error("example is not exists");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            HttpRequestContextUtils.currentRequestContext().downloadFile("订单物流信息模板.xlsx",
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception ex) {
            logger.error("download example is failed", ex);
        }
    }

    @RequestMapping(value = "uploadLoisticsInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadLoisticsInfo(HttpServletRequest request) {
        MultiValueMap<String, MultipartFile> multiValuedMap = ((DefaultMultipartHttpServletRequest) request).getMultiFileMap();
        List<MultipartFile> multipartFiles = multiValuedMap.get("file");
        try {
            MultipartFile file = multipartFiles.get(0);
            if (file == null || file.isEmpty()) {
                logger.error("getRequestWorkbook - Empty MultipartFile with name['{}']");
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                return MapMessage.errorMessage().add("info", "不是excel文件");
            }
            @Cleanup InputStream in = file.getInputStream();
            Workbook wb = WorkbookFactory.create(in);
            //获取excel中的内容，用户ID，订单ID，物流公司，物流单号
            Sheet sheet = wb.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (Objects.isNull(row)) {
                    continue;
                }

                String userId = WorkbookUtils.getCellValue(row.getCell(0));
                String orderId = WorkbookUtils.getCellValue(row.getCell(1));
                String logisticsCompany = WorkbookUtils.getCellValue(row.getCell(2));
                String logisticsNum = WorkbookUtils.getCellValue(row.getCell(3));
                String mobilePhone = WorkbookUtils.getCellValue(row.getCell(4));
                String userOrderId = orderId + "_" + (SafeConverter.toLong(userId) % 100);
                UserOrder userOrder = userOrderLoaderClient.loadUserOrder(userOrderId);
                if (Objects.nonNull(userOrder)) {
                    userOrderServiceClient.addUserOrderLoisticsInfo(userOrder, logisticsCompany, logisticsNum);
                    try {
                        if (StringUtils.isNotBlank(mobilePhone)) {
                            String smsContent = "发货成功：尊敬的用户您好，您购买的《" + userOrder.getProductName() + "》已发货，" + logisticsCompany + " " + logisticsNum + "，请您留意收货哦。";
                            SmsMessage sms = new SmsMessage();
                            sms.setMobile(mobilePhone);
                            sms.setSmsContent(smsContent);
                            sms.setType(SmsType.AGENT_PAY.name());
                            smsServiceClient.getSmsService().sendSms(sms);
                        }
                    } catch (Exception e) {
                        logger.error("发送短信失败,订单号：" + userOrderId + "，手机号：" + mobilePhone, e);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            logger.error("导入物流信息失败", e);
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage();
    }

    // 获取订单支付记录
    @RequestMapping(value = "loadpaidhistory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadPaidHistory(@RequestParam String orderId) {
        UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
        if (order == null) {
            return MapMessage.errorMessage("订单不存在");
        }
        List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId());
        if (CollectionUtils.isEmpty(paymentHistories)) {
            return MapMessage.errorMessage("无记录");
        }
        List<UserOrderPaymentHistory> paymentHistory = paymentHistories.stream().filter(h -> h.getOrderId().equals(order.getId()))
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("historyList", paymentHistory);
    }

    // 查询订单退款进度
    @RequestMapping(value = "loadrefundprocess.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadRefundProcess(@RequestParam String orderId) {
        UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
        if (order == null) {
            return MapMessage.errorMessage("订单不存在");
        }
        String payMethod = "";
        String transactionId = "";
        if (StringUtils.isBlank(order.getOldOrderId())) {
            List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId());
            if (CollectionUtils.isEmpty(paymentHistories)) {
                return MapMessage.errorMessage("无支付记录");
            }
            UserOrderPaymentHistory paymentHistory = paymentHistories.stream().filter(h -> h.getOrderId().equals(order.getId()))
                    .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
                    .findFirst().orElse(null);
            if (paymentHistory == null) {
                return MapMessage.errorMessage("无支付记录");
            }
            payMethod = paymentHistory.getPayMethod();
            transactionId = paymentHistory.getOuterTradeId();
        } else {
            AfentiOrder afentiOrder = afentiOrderServiceClient.getAfentiOrderService().loadAfentiOrder(order.getOldOrderId()).getUninterruptibly();
            if (afentiOrder != null) {
                payMethod = afentiOrder.getPayMethod();
                transactionId = afentiOrder.getExtTradeNo();
            }
        }
        if (StringUtils.isBlank(payMethod) || !payMethod.contains("wechat")) {
            return MapMessage.errorMessage("仅支持微信支付方式的查看");
        }
        PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(payMethod);
        if (paymentGateway == null) {
            return MapMessage.errorMessage("不支持的支付方式");
        }
        try {
            WechatAbstractPaymentGateway wechatAbstractPaymentGateway = (WechatAbstractPaymentGateway) paymentGateway;
            return wechatAbstractPaymentGateway.refundQuery(orderId, transactionId);
        } catch (Exception ignore) {
            return MapMessage.errorMessage("查看失败");
        }
    }

    // 获取用户7日统计
    @RequestMapping(value = "loadusersummaryweek.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadUserSummaryWeek(@RequestParam Long userId) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userId);
        if (CollectionUtils.isEmpty(paymentHistories)) {
            return MapMessage.errorMessage("无记录");
        }
        Date day7 = DayRange.newInstance(DateUtils.nextDay(new Date(), -7).getTime()).getStartDate();
        List<UserOrderPaymentHistory> paymentHistory7 = paymentHistories.stream().filter(h -> h.getPayDatetime().after(day7))
                .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
                .collect(Collectors.toList());
        Double payPriceSum7 = paymentHistory7
                .stream()
                .mapToDouble(value -> value.getPayAmount().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                .sum();
        List<UserOrderPaymentHistory> paymentHistory1 = paymentHistories.stream().filter(h -> DayRange.current().contains(h.getPayDatetime()))
                .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
                .collect(Collectors.toList());
        Double payPriceSum1 = paymentHistory1
                .stream()
                .mapToDouble(value -> value.getPayAmount().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                .sum();
        List<UserOrderPaymentHistory> refundHistory = paymentHistories.stream().filter(h -> DayRange.current().contains(h.getCreateDatetime()))
                .filter(h -> h.getPaymentStatus() == PaymentStatus.Refund || h.getPaymentStatus() == PaymentStatus.RefundFail ||
                        h.getPaymentStatus() == PaymentStatus.Refunding)
                .collect(Collectors.toList());
        Double refundPriceSumDay = refundHistory.stream()
                .mapToDouble(value -> value.getPayAmount().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())
                .sum();
        return MapMessage.successMessage().add("userId", userId).add("userName", user.fetchRealname())
                .add("count", paymentHistory1.size()).add("sumPrice", payPriceSum1)
                .add("count7", paymentHistory7.size()).add("sumPrice7", payPriceSum7)
                .add("refundPriceSumDay", refundPriceSumDay);
    }

    @RequestMapping(value = "manuallypay.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String manuallypay(HttpServletRequest request, Model model) {

        String orderId;
        String extTradeNo;
        BigDecimal payAmount;

        try {
            orderId = getRequestParameter("orderId", "").replaceAll("\\s", "");
            extTradeNo = getRequestParameter("extTradeNo", "").replaceAll("\\s", "");
            payAmount = new BigDecimal(getRequestParameter("payAmount", "0").replaceAll("\\s", ""));
        } catch (Exception e) {
            return "legacy/afenti/manuallypay";
        }

        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if (userOrder == null) {
            model.addAttribute("errorMessage", "订单号有误");
        } else {
            if (!userOrder.canBePaid()) {
                model.addAttribute("errorMessage", "订单不可付款（已经付款成功？）:" + userOrder.getOrderStatus() + "/" + userOrder.getPaymentStatus());
            }
        }
        if (request.getMethod().equals("POST")) {
            if (StringUtils.isBlank(extTradeNo)) {
                model.addAttribute("errorMessage", "请提供外部支付单号");
            }
            if (payAmount.doubleValue() < 0) {
                model.addAttribute("errorMessage", "金额有误");
            }
            if (!model.containsAttribute("errorMessage")) {
                PaymentCallbackContext paymentCallbackContext = new PaymentCallbackContext("manually", PaymentGateway.CallbackAction_Notify);
                paymentCallbackContext.setVerifiedPaymentData(new PaymentVerifiedData());
                paymentCallbackContext.getVerifiedPaymentData().setExternalTradeNumber(extTradeNo);
                paymentCallbackContext.getVerifiedPaymentData().setExternalUserId("");
                paymentCallbackContext.getVerifiedPaymentData().setPayAmount(payAmount);
                paymentCallbackContext.getVerifiedPaymentData().setTradeNumber(orderId);

                userOrder = businessUserOrderServiceClient.processUserOrderPayment(paymentCallbackContext);
                if (userOrder.getPaymentStatus() == PaymentStatus.Paid) {
                    model.addAttribute("orderId", orderId);
                    return redirect("main.vpage");
                } else {
                    model.addAttribute("errorMessage", "订单付款失败");
                }
            }
        }

        model.addAttribute("orderId", orderId);
        model.addAttribute("extTradeNo", extTradeNo);
        model.addAttribute("payAmount", payAmount);
        return "legacy/afenti/manuallypay";
    }

    @RequestMapping(value = "cancelAfentiOrder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map cancelAfentiOrder(@RequestParam String orderId) {

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        try {
            UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
            if (order == null) {
                map.put("status", "false");
                map.put("message", "不存在此订单！");
                return map;
            }
            MapMessage mapMessage = userOrderServiceClient.cancelOrder(orderId);
            // 添加用户备注
            if (mapMessage.isSuccess()) {
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(order.getUserId());
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.取消订单.name());
                userServiceRecord.setOperationContent("管理员取消用户订单");
                userServiceRecord.setComments("订单ID:" + orderId + ", 商品:" + order.getOrderProductServiceType() + ", 商品名:" + order.getProductName() + ", 价格:" + order.getOrderPrice());
                userServiceClient.saveUserServiceRecord(userServiceRecord);

            } else {
                map.put("status", "false");
                map.put("message", "操作失败！");
            }
        } catch (Exception ex) {
            logger.error("取消阿分题订单{}失败，原因：{}", orderId, ex.getMessage(), ex);
            map.put("status", "false");
            map.put("message", "操作失败！" + ex.getMessage());
            return map;
        }

        addAdminLog("取消阿分题订单", orderId, "操作成功！由管理员" + getCurrentAdminUser().getAdminUserName() + "操作.");

        map.put("status", "true");
        map.put("message", "操作成功！");
        return map;
    }


    @RequestMapping(value = "delayafentiactivationhistory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map delay(@RequestParam String historyId, @RequestParam int delayDays, @RequestParam String delayReason) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("activeTab", "1");
        try {
            if (delayReason.trim().length() == 0) {
                map.put("status", "false");
                map.put("message", "延期原因不能为空！");
                return map;
            }
            //如果是点读机产品并且是非自研，则不能延期
            UserActivatedProduct userActivatedProduct = userOrderLoaderClient.loadUserActivatedProductById(historyId);
            if (null != userActivatedProduct) {
                OrderProductItem orderProductItem = userOrderLoaderClient.loadOrderProductItemById(userActivatedProduct.getProductItemId());
                if (null != orderProductItem) {
                    TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(orderProductItem.getAppItemId());

                    if (null != sdkInfo && null != sdkInfo.getSdkType() && sdkInfo.getSdkType() != TextBookSdkType.none) {
                        map.put("status", "false");
                        map.put("message", "非自研点读机产品，第三方合作商不支持延期操作");
                        return map;
                    }
                }
            }

            UserActivatedProduct product = afentiAdminService.delayActivationHistory(historyId, delayDays);
            User user = userLoaderClient.loadUser(product.getUserId());
            //添加用户备注
            if (null != user) {
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(user.getId());
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.订单相关.name());
                userServiceRecord.setOperationContent("管理员对订单延期");
                userServiceRecord.setComments("商品:" + product.getProductServiceType() + ", 时间:" + delayDays + ", 原因:" + delayReason);
                userServiceClient.saveUserServiceRecord(userServiceRecord);
                map.put("userid", String.valueOf(user.getId()));
            } else {
                map.put("userid", "");
            }
        } catch (Exception ex) {
            map.put("status", "false");
            map.put("message", "操作失败！" + ex.getMessage());
            return map;
        }
        addAdminLog("用户产品激活历史延期", historyId, "操作成功！由管理员" + getCurrentAdminUser().getAdminUserName() + "操作.");
        map.put("status", "true");
        map.put("message", "操作成功！");
        return map;
    }

    @RequestMapping(value = "addorder.vpage", method = RequestMethod.GET)
    public String addOrder(Model model) {
        Long userId;
        String productType;
        String productName;
        try {
            userId = Long.parseLong(getRequestParameter("userId", "").replaceAll("\\s", ""));
            productType = getRequestString("productType");
            productName = getRequestString("productName");
        } catch (Exception e) {
            return redirect("/legacy/afenti/main.vpage");
        }
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return redirect("/legacy/afenti/main.vpage");
        }

        List<OrderProductServiceType> types = Arrays.asList(OrderProductServiceType.values());
        model.addAttribute("productTypes", types.stream().filter(t -> !t.isOrderClosed()).collect(Collectors.toList()));

        List<UserActivatedProduct> activatedProductList = userOrderLoaderClient.loadUserActivatedProductList(userId).stream()
                .filter(p -> p.getServiceEndTime().after(new Date())).collect(Collectors.toList());

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        List<OrderProduct> availableProductList = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(studentDetail);
        List<OrderProduct> availableProducts = getAfentiProductInfos(activatedProductList, availableProductList);
        // 薯条英语暂时放开入口
        if (user.isParent()) {
            availableProducts = userOrderLoaderClient.loadAvailableProduct().stream().filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish
                    || OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.PicListenBook).collect(Collectors.toList());
        } else {
            availableProducts = availableProducts.stream().filter(e -> OrderProductServiceType.safeParse(e.getProductType()) != OrderProductServiceType.PicListenBook &&
                    OrderProductServiceType.safeParse(e.getProductType()) != OrderProductServiceType.ChipsEnglish).collect(Collectors.toList());
        }
        model.addAttribute("user", user);
        model.addAttribute("productType", productType);
        model.addAttribute("productName", productName);
        if (availableProducts == null || availableProducts.isEmpty()) {
            model.addAttribute("message", "用户" + user.getProfile().getRealname() + "(ID:" + user.getId() + ")不存在可购买的订单");
        } else {
            availableProducts.sort(Comparator.comparing(o -> o.getProductType()));
            List<Map<String, Object>> infoMaps = new ArrayList<>();
            for (OrderProduct info : availableProducts) {
                Map<String, Object> infoMap = new HashMap<>();
                infoMap.put("productName", info.getName());
                infoMap.put("productId", info.getId());
                infoMap.put("totalPriceOriginal", info.getOriginalPrice());
                infoMap.put("totalPriceGeneric", info.getPrice());
                infoMap.put("productServiceType", info.getProductType());
                infoMaps.add(infoMap);
            }
            model.addAttribute("availableProducts", infoMaps);
        }
        return "legacy/afenti/addorder";
    }

    private List<OrderProduct> getAfentiProductInfos(List<UserActivatedProduct> activatedProductList, List<OrderProduct> availableProductList) {
        List<OrderProduct> availableProducts = new ArrayList<>();
        if (activatedProductList != null) {
            for (OrderProduct product : availableProductList) {
                Boolean mark = true;
                for (UserActivatedProduct activatedProduct : activatedProductList) {
                    if (Objects.equals(product.getProductType(), activatedProduct.getProductServiceType())) {
                        mark = false;
                        break;
                    }
                }
                if (mark) {
                    availableProducts.add(product);
                }
            }
        } else {
            availableProducts = availableProductList;
        }
        return availableProducts;
    }

    @RequestMapping(value = "postorder.vpage")
    @ResponseBody
    public Map<String, Object> postOrder(Model model) {
        Map<String, Object> msg = new HashMap<>();
        Long userId = getRequestLong("userId");
        String productId = getRequestString("productId");

        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        if (null == product) {
            msg.put("message", "无效的产品");
            return msg;
        }
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            msg.put("message", "no user");
            return msg;
        }
        // 产品价格有灰度在这里处理
        List<OrderProduct> availableProductList = null;
        if (!user.isParent()) {
            StudentDetail detail = studentLoaderClient.loadStudentDetail(userId);
            availableProductList = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(detail);
            model.addAttribute("user", detail);
        }
        if (CollectionUtils.isNotEmpty(availableProductList)) {
            product = availableProductList.stream().filter(o -> o.getId().equals(productId))
                    .findFirst().orElse(null);
        }
        if (null == product) {
            msg.put("message", "无效的产品");
            return msg;
        }

        UserOrder order = UserOrder.newOrder(OrderType.app, user.getId());
        if (OrderProductServiceType.safeParse(product.getProductType()) == OrderProductServiceType.PicListen || OrderProductServiceType.safeParse(product.getProductType()) == OrderProductServiceType.PicListenBook || OrderProductServiceType.safeParse(product.getProductType()) == OrderProductServiceType.FollowRead) {
            order.setOrderType(OrderType.pic_listen);
        }
        if (OrderProductServiceType.safeParse(product.getProductType()) == OrderProductServiceType.ChipsEnglish) {
            order.setOrderType(OrderType.chips_english);
        }
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setUserId(user.getId());
        order.setUserName(user.getProfile().getRealname());
        order.setOrderProductServiceType(product.getProductType());
        order.setProductAttributes(product.getAttributes());
        order.setOrderPrice(product.getPrice());
        order.setOrderReferer("crm");
        MapMessage message = userOrderServiceClient.saveUserOrder(order);
        if (!message.isSuccess()) {
            msg.put("message", StringUtils.defaultIfBlank(message.getInfo(), "生成订单失败"));
            return msg;
        }
        addAdminLog("生成订单", order.genUserOrderId(), null, order);
        msg.put("message", "用户" + user.getProfile().getRealname() + "(ID:" + user.getId() + ")购买" + product.getName() + "产品的订单已经成功提交");
        return msg;
    }

    @RequestMapping(value = "/loadcoupon.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadCoupon() {
        String orderId = getRequestString("oid");
        try {
            UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
            if (null == order || StringUtils.isBlank(order.getCouponRefId())) return MapMessage.errorMessage("无效订单");
            CouponUserRef ref = couponLoaderClient.loadCouponUserRefById(order.getCouponRefId());
            if (ref == null) {
                return MapMessage.errorMessage();
            }
            Coupon coupon = couponLoaderClient.loadCouponById(ref.getCouponId());
            if (coupon == null) {
                return MapMessage.errorMessage();
            }
            return MapMessage.successMessage().add("couponId", coupon.getId())
                    .add("couponName", coupon.getName())
                    .add("couponType", coupon.getCouponType().getDesc())
                    .add("typeValue", coupon.getTypeValue().doubleValue());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    /*****************************************************************
     * 新的打包退款接口
     *****************************************************/

    // 获取可退款产品列表 (新打包产品形态)
    @RequestMapping(value = "/refundproducts.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refundProducts() {
        String orderId = getRequestString("oid");

        try {
            UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
            if (null == order) return MapMessage.errorMessage("无效订单");

            List<UserOrderProductRef> refs = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());

            if (CollectionUtils.isEmpty(refs)) {
                return MapMessage.errorMessage("不支持单个产品退款");
            }
            // 获取摊销记录
            List<UserOrderAmortizeHistory> amortizeHistories = userOrderLoaderClient.loadOrderAmortizeHistory(order.genUserOrderId());
            if (CollectionUtils.isEmpty(amortizeHistories)) {
                return MapMessage.errorMessage("找不到付款历史");
            }
            Map<String, List<UserOrderAmortizeHistory>> productMap = amortizeHistories.stream()
                    .collect(Collectors.groupingBy(UserOrderAmortizeHistory::getProductId));
            // 组装数据
            List<Map<String, Object>> dataList = new ArrayList<>();
            Date now = new Date();
            for (Map.Entry<String, List<UserOrderAmortizeHistory>> entry : productMap.entrySet()) {
                Map<String, Object> data = new HashMap<>();
                List<UserOrderAmortizeHistory> amortizeHistoryList = entry.getValue();
                if (CollectionUtils.isEmpty(amortizeHistoryList)) {
                    continue;
                }
                data.put("orderId", orderId);
                data.put("productId", amortizeHistoryList.get(0).getProductId());
                data.put("productName", amortizeHistoryList.get(0).getProductName());
                BigDecimal payAmount = new BigDecimal(0);
                List<UserOrderAmortizeHistory> paidHis = entry.getValue().stream().filter(p -> p.getPaymentStatus() == PaymentStatus.Paid)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(paidHis)) {
                    for (UserOrderAmortizeHistory history : paidHis) {
                        payAmount = payAmount.add(history.getPayAmount());
                    }
                }
                data.put("payAmount", payAmount);
                UserOrderAmortizeHistory refundHis = entry.getValue().stream().filter(p -> p.getPaymentStatus() == PaymentStatus.Refund).findFirst().orElse(null);
                data.put("hasRefund", refundHis != null);
                String notifyInfo = "";
                if (levelReadingProductTypes.contains(amortizeHistoryList.get(0).getOrderProductServiceType())) {
                    OrderProductItem productItem = userOrderLoaderClient.loadOrderProductItemById(amortizeHistoryList.get(0).getProductItemId());
                    notifyInfo = levelReadingNotifyInfo(productItem, amortizeHistoryList.get(0).getUserId(), amortizeHistoryList.get(0).getPayDatetime(), now);
                }
                data.put("notifyInfo", notifyInfo);
                dataList.add(data);
            }
            return MapMessage.successMessage().add("data", dataList);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private String levelReadingNotifyInfo(OrderProductItem productItem, Long userId, Date checkFrom, Date checkTo) {
        String notifyInfo = "";
        if (DateUtils.dayDiff(checkTo, checkFrom) > 7) {
            notifyInfo = "已过退款期，理论上不给退";
        }

        String appItemId = productItem != null ? productItem.getAppItemId() : "";
        if (StringUtils.isNotBlank(appItemId)) {
            Map<String, Boolean> bookMap = userPicBookServiceClient.loadReadStatus(userId, Arrays.asList(appItemId));
            notifyInfo = MapUtils.isNotEmpty(bookMap) && bookMap.get(appItemId) != null && bookMap.get(appItemId) ? "已阅读，理论上不给退" : notifyInfo;
        }
        return notifyInfo;
    }

    // 计算可退天数
    @RequestMapping(value = "/multirefunddays.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage multiRefundDays() {
        String orderId = getRequestString("oid");
        String productId = getRequestString("productId");

        try {
            UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
            if (null == order) return MapMessage.errorMessage("无效订单");

            List<UserOrderProductRef> refList = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
            if (CollectionUtils.isEmpty(refList)) {
                return MapMessage.errorMessage("没有打包的产品");
            }
            UserOrderProductRef ref = refList.stream().filter(o -> StringUtils.equals(o.getProductId(), productId)).findFirst().orElse(null);
            if (ref == null) {
                return MapMessage.errorMessage("找不到对应的产品");
            }
            List<UserOrderAmortizeHistory> amortizeHistoryList = userOrderLoaderClient.loadOrderAmortizeHistory(order.genUserOrderId());
            // 是否已经退款
            UserOrderAmortizeHistory refundHis = amortizeHistoryList.stream()
                    .filter(o -> StringUtils.equals(o.getProductId(), productId))
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.Refund)
                    .findFirst().orElse(null);
            if (refundHis != null) {
                return MapMessage.errorMessage("该产品已退款");
            }
            long maxDays = 0;
            long minDays = 0;
            String refundType = "period";
            // 付费历史
            UserOrderAmortizeHistory paidHis = amortizeHistoryList.stream()
                    .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
                    .filter(h -> StringUtils.equals(h.getProductId(), productId))
                    .findFirst().orElse(null);
            if (paidHis == null) {
                return MapMessage.successMessage().add("max", maxDays).add("min", minDays).add("refundType", "allRefund")
                        .add("productId", productId);
            }
            if (null != paidHis.getServiceStartTime() && null != paidHis.getServiceEndTime()) {
                maxDays = DateUtils.dayDiff(paidHis.getServiceEndTime(), paidHis.getServiceStartTime());
                if (paidHis.getServiceStartTime().toInstant().isAfter(Instant.now())) {
                    minDays = maxDays;
                } else if (paidHis.getServiceEndTime().toInstant().isAfter(Instant.now())) {
                    minDays = DateUtils.dayDiff(paidHis.getServiceEndTime(), new Date());
                } else {
                    minDays = maxDays;
                }
            }
            return MapMessage.successMessage().add("max", maxDays).add("min", minDays).add("productType", refundType).add("entityReward", false)
                    .add("productId", productId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/multirefundamount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage multiCalculateRefundAmount() {
        String orderId = getRequestString("oid");
        String productId = getRequestString("productId");
        Integer refundDays = getRequestInt("days", 0);

        try {
            UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
            if (null == order) return MapMessage.errorMessage("无效订单");
            Double amount = afentiAdminService.calculateRefundAmountForMulti(refundDays, order, productId);
            if (amount <= 0) return MapMessage.errorMessage("可退金额为0");
            return MapMessage.successMessage().add("refundAmount", amount);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return MapMessage.errorMessage();
    }


    // 发起退款 多个产品的订单
    @RequestMapping(value = "multirefund.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage multiRefund() {
        String orderId = getRequestString("oid");
        String data = getRequestString("data");
        String memo = getRequestString("memo");
        try {
            if (StringUtils.isBlank(data)) {
                return MapMessage.errorMessage("数据错误");
            }
            String[] refundDataArray = StringUtils.split(data, ",");
            if (refundDataArray == null || refundDataArray.length <= 0) {
                return MapMessage.errorMessage("数据错误");
            }
            Map<String, Double> dataMap = new HashMap<>();
            for (String s : refundDataArray) {
                String[] productArray = StringUtils.split(s, "|");
                if (productArray.length != 2) {
                    return MapMessage.errorMessage("数据格式错误");
                }
                dataMap.put(productArray[0], SafeConverter.toDouble(productArray[1]));
            }
            if (StringUtils.isBlank(memo)) return MapMessage.errorMessage("请输入备注信息");
            UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
            if (null == order) return MapMessage.errorMessage("无效订单");
            List<UserOrderAmortizeHistory> amortizeHistoryList = userOrderLoaderClient.loadOrderAmortizeHistory(order.genUserOrderId());
            // 获取付费摊销记录
            if (CollectionUtils.isEmpty(amortizeHistoryList)) {
                return MapMessage.errorMessage("找不到付款记录");
            }
            // 获取付费记录
            UserOrderPaymentHistory paymentHistory = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId()).stream()
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                    .filter(o -> StringUtils.equals(o.getOrderId(), order.getId()))
                    .findFirst().orElse(null);
            if (paymentHistory == null) {
                return MapMessage.errorMessage("找不到订单付款记录");
            }

            double totalRefundAmount = 0;
            for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
                totalRefundAmount = totalRefundAmount + entry.getValue();
            }
            if (Double.compare(totalRefundAmount, paymentHistory.getPayAmount().doubleValue()) > 0) {
                if (RuntimeMode.current().ge(Mode.STAGING)) {
                    return MapMessage.errorMessage("退款金额不能大于付款金额");
                } else {
                    totalRefundAmount = paymentHistory.getPayAmount().doubleValue();
                }
            }

            // 看看是否有退款记录了
            OrderRefundHistory history = userOrderLoaderClient.loadOrderRefundHistoryById(paymentHistory.getOuterTradeId());
            if (history != null) {
                if (history.getStatus() == RefundHistoryStatus.REFUNDING) {
                    return MapMessage.errorMessage("该订单有一笔未处理退款请求，请处理完后再进行退款");
                }
                if (Double.compare(totalRefundAmount + history.getRefundFee().doubleValue(), paymentHistory.getPayAmount().doubleValue()) > 0) {
                    if (RuntimeMode.current().ge(Mode.STAGING)) {
                        return MapMessage.errorMessage("退款金额不能大于付款金额");
                    }
                }
            }

            // 进行退款处理
            for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
                String productId = entry.getKey();
                Double refundAmount = entry.getValue();
                if (refundAmount <= 0) {
                    return MapMessage.errorMessage("错误的退款金额");
                }
                List<UserOrderAmortizeHistory> paidHis = amortizeHistoryList.stream().filter(o -> StringUtils.equals(o.getProductId(), productId))
                        .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(paidHis)) {
                    return MapMessage.errorMessage("无该产品[" + productId + "]付款记录");
                }
                List<UserOrderAmortizeHistory> refundAmortizeHistoryList = new ArrayList<>();

                // 总退款金额
                BigDecimal totalRefundPrice = new BigDecimal(refundAmount);
                // 计算单个退款金额
                BigDecimal sgPrice = totalRefundPrice.divide(new BigDecimal(paidHis.size()), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal tempTotalPrice = new BigDecimal(0);
                for (int i = 0; i < paidHis.size(); i++) {
                    UserOrderAmortizeHistory amortizeHistory = paidHis.get(i);

                    UserOrderAmortizeHistory refundHis = amortizeHistoryList.stream().filter(o -> StringUtils.equals(o.getProductId(), productId))
                            .filter(o -> StringUtils.equals(o.getProductItemId(), amortizeHistory.getProductItemId()))
                            .filter(o -> o.getPaymentStatus() == PaymentStatus.Refund).findFirst().orElse(null);
                    if (refundHis != null) {
                        return MapMessage.errorMessage(amortizeHistory.getProductName() + "-不能重复退款");
                    }

                    // 将订单退款 处理有效期
                    MapMessage refundMsg = userOrderServiceClient.refundOrderForMulti(amortizeHistory);
                    if (!refundMsg.isSuccess()) return refundMsg;

                    // 记录退款摊销历史
                    UserOrderAmortizeHistory refundAmortizeHistory = new UserOrderAmortizeHistory();
                    refundAmortizeHistory.setOrderId(order.genUserOrderId());
                    refundAmortizeHistory.setPaymentHistoryId(paymentHistory.getId());
                    refundAmortizeHistory.setUserId(amortizeHistory.getUserId());
                    refundAmortizeHistory.setAmortizeType(amortizeHistory.getAmortizeType());
                    refundAmortizeHistory.setOrderProductServiceType(amortizeHistory.getOrderProductServiceType());
                    refundAmortizeHistory.setOuterTradeId(amortizeHistory.getOuterTradeId());
                    if (i == paidHis.size() - 1) {
                        refundAmortizeHistory.setPayAmount(totalRefundPrice.subtract(tempTotalPrice));
                    } else {
                        refundAmortizeHistory.setPayAmount(sgPrice);
                    }
                    refundAmortizeHistory.setPayDatetime(new Date());
                    refundAmortizeHistory.setPaymentStatus(PaymentStatus.Refund);
                    refundAmortizeHistory.setPayMethod(amortizeHistory.getPayMethod());
                    refundAmortizeHistory.setPeriod(0);
                    refundAmortizeHistory.setProductId(amortizeHistory.getProductId());
                    refundAmortizeHistory.setProductName(amortizeHistory.getProductName());
                    refundAmortizeHistory.setProductItemId(amortizeHistory.getProductItemId());
                    refundAmortizeHistory.setGrade(order.getGrade());
                    refundAmortizeHistory.setRegionCode(order.getRegionCode());
                    refundAmortizeHistory.setSchoolId(order.getSchoolId());
                    refundAmortizeHistoryList.add(refundAmortizeHistory);
                    tempTotalPrice = tempTotalPrice.add(sgPrice);
                }
                userOrderServiceClient.saveUserAmortizeHistory(refundAmortizeHistoryList);
            }

            PaymentStatus paymentStatus = PaymentStatus.Refunding;
            RefundHistoryStatus refundStatus = RefundHistoryStatus.SUBMIT;
            // qpay 直接置成失败
            if (StringUtils.equals(paymentHistory.getPayMethod(), PaymentConstants.PaymentGatewayName_QQ_StudentApp)) {
                paymentStatus = PaymentStatus.RefundFail;
                refundStatus = RefundHistoryStatus.FAIL;
            }
            // 记录退款流水
            UserOrderPaymentHistory refundPaymentHistory = new UserOrderPaymentHistory();
            refundPaymentHistory.setOrderId(order.getId());
            refundPaymentHistory.setPayAmount(new BigDecimal(totalRefundAmount));
            refundPaymentHistory.setPayDatetime(new Date());
            refundPaymentHistory.setUserId(order.getUserId());
            refundPaymentHistory.setPaymentStatus(paymentStatus);
            refundPaymentHistory.setOuterTradeId(paymentHistory.getOuterTradeId());
            refundPaymentHistory.setPayMethod(paymentHistory.getPayMethod());
            refundPaymentHistory.setComment(memo);
            userOrderServiceClient.saveUserOrderPaymentHistory(refundPaymentHistory);

            // 判断是否作业币
            if (StringUtils.isNotBlank(paymentHistory.getPayMethod())
                    && Objects.equals(paymentHistory.getPayMethod(), PaymentConstants.PaymentGatewayName_17Zuoye)) {
                // 作业币支付的，退到作业币账户
                if (!createRefundFinanceFlow(order, totalRefundAmount)) {
                    return MapMessage.errorMessage("作业币退款失败，订单已退！");
                }
            } else {

                if (history != null) {
                    if (history.getStatus() == RefundHistoryStatus.SUCCESS) {
                        history.setRefundFee(new BigDecimal(totalRefundAmount));
                    } else {
                        double refundFee = history.getRefundFee().doubleValue() + totalRefundAmount;
                        history.setRefundFee(new BigDecimal(refundFee));
                    }
                    history.setStatus(RefundHistoryStatus.SUBMIT);
                    history.setUpdateDatetime(new Date());
                    userOrderServiceClient.saveOrUpdateRefundHistory(history);
                } else {
                    // 添加财务处理退款任务
                    OrderRefundHistory refundHistory = new OrderRefundHistory();
                    refundHistory.setId(paymentHistory.getOuterTradeId());
                    refundHistory.setPayMethod(paymentHistory.getPayMethod());
                    refundHistory.setRefundFee(new BigDecimal(totalRefundAmount));
                    refundHistory.setOrderId(order.genUserOrderId());
                    refundHistory.setStatus(refundStatus);
                    refundHistory.setUserId(order.getUserId());
                    refundHistory.setCreateDatetime(new Date());
                    refundHistory.setUpdateDatetime(new Date());
                    userOrderServiceClient.saveOrUpdateRefundHistory(refundHistory);
                }
            }
            // 记录CRM操作日志
            String operation = "订单号:" + orderId + ",产品名称:" + order.getProductName() + ",退款金额:" + totalRefundAmount + ",备注：" + memo;
            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(order.getUserId());
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.订单退款.name());
            userServiceRecord.setOperationContent("订单退款");
            userServiceRecord.setComments(operation);
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            return MapMessage.successMessage("退款申请成功");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    /*****************************************************************
     * end
     *****************************************************/

    @RequestMapping(value = "/refunddays.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refundDays() {
        String orderId = getRequestString("oid");

        try {
            UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
            if (null == order) return MapMessage.errorMessage("无效订单");

            if (!order.canBeRefund()) {
                return MapMessage.errorMessage("订单状态不可退款");
            }
            long maxDays = 0;
            long minDays = 0;
            String refundType = "period";
            // 道具类特殊处理
            if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.ValueAddedLiveTimesCard ||
                    OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.EnglishStoryBook ||
                    OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.GrowingWorldProp) {
                refundType = "times";
            }
            // 打包全退
            if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.GroupProduct) {
                refundType = "allRefund";
            }
            // 付费历史
            UserOrderPaymentHistory history = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId()).stream()
                    .filter(h -> Objects.equals(h.getOrderId(), order.getId()))
                    .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
                    .findFirst().orElse(null);
            if (history == null) {
                return MapMessage.successMessage().add("max", maxDays).add("min", minDays).add("refundType", "allRefund");
            }
            if (null != history.getServiceStartTime() && null != history.getServiceEndTime()) {
                maxDays = DateUtils.dayDiff(history.getServiceEndTime(), history.getServiceStartTime());
                if (history.getServiceStartTime().toInstant().isAfter(Instant.now())) {
                    minDays = maxDays;
                } else if (history.getServiceEndTime().toInstant().isAfter(Instant.now())) {
                    minDays = DateUtils.dayDiff(history.getServiceEndTime(), new Date());
                } else {
                    minDays = maxDays;
                }
            }

            String notifyInfo = "";
            if (levelReadingProductTypes.contains(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
                List<OrderProductItem> proItems = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
                if (CollectionUtils.isNotEmpty(proItems)) {
                    notifyInfo = levelReadingNotifyInfo(proItems.get(0), order.getUserId(), history.getServiceStartTime(), new Date());
                }
            }

            return MapMessage.successMessage().add("max", maxDays).add("min", minDays).add("productType", refundType).add("entityReward", false).add("notifyInfo", notifyInfo);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "loadrefund.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadReundOrderDetail() {
        String orderId = getRequestString("oid");
        UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
        if (null == order) return MapMessage.errorMessage("无效订单");
        // 获取付费记录
        List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId()).stream().filter(e -> StringUtils.equals(e.getOrderId(), order.getId())).collect(Collectors.toList());
        UserOrderPaymentHistory paymentHistory = paymentHistories.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                .findFirst().orElse(null);
        if (paymentHistory == null) {
            return MapMessage.errorMessage("找不到订单付款记录");
        }
        MapMessage result = userOrderRefundServiceClient.getUserOrderRefundService().loadOrderRefundDetail(order.getUserId(), order.getId());
        if (result.isSuccess()) {
            String notifyInfo = "";
            if (levelReadingProductTypes.contains(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
                List<OrderProductItem> proItems = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
                if (CollectionUtils.isNotEmpty(proItems)) {
                    notifyInfo = levelReadingNotifyInfo(proItems.get(0), order.getUserId(), paymentHistory.getServiceStartTime(), new Date());
                }
            }
            if (order.getOrderType() == OrderType.chips_english) {
                Map<String, List<OrderProductItem>> productToItemMap = loadOrderProductItem(order);
                Map<String, List<ChipsEnglishProductTimetable.Course>> itemToCourseMap = itemToTimeTableCourse(productToItemMap);
                Object itemListObj = result.get("itemList");
                if (itemListObj != null) {
                    Double refund = chipsRefundTextMap.get(order.genUserOrderId());
                    List<Map<String, Object>> mapList = (List<Map<String, Object>>) itemListObj;
                    if (CollectionUtils.isNotEmpty(mapList)) {
                        mapList.forEach(m -> {
                            List<ChipsEnglishProductTimetable.Course> courseList = Optional.ofNullable(m.get("itemId"))
                                    .map(e -> itemToCourseMap.get(e.toString())).orElse(Collections.emptyList());
                            BigDecimal payAmount = Optional.ofNullable(m.get("payAmount")).map(p -> (BigDecimal) p).orElse(BigDecimal.ZERO);
                            Double itemRefund = Optional.ofNullable(refund).map(e -> e / mapList.size()).orElse(0.0);
                            BigDecimal chipsRefundAmount = loadOrderRefundDetail( payAmount.subtract(BigDecimal.valueOf(itemRefund)), courseList);
                            m.put("refundableAmount", chipsRefundAmount);
                        });
                    }
                }
                result.set("chipsRefundedText", chipsRefundTextMap.get(orderId) == null ? "" : "该订单已经退款" + chipsRefundTextMap.get(orderId) + "元");
            }
            return result.set("orderId", paymentHistory.genUserOrderId())
                    .set("createTime", DateUtils.dateToString(order.getCreateDatetime()))
                    .set("payTime", DateUtils.dateToString(paymentHistory.getPayDatetime()))
                    .set("hasEntityReward", false)
                    .set("payStatus", paymentHistory.getPaymentStatus().getDesc())
                    .set("userId", order.getUserId())
                    .set("outerTrade", paymentHistory.getOuterTradeId())
                    .set("payMethod", paymentHistory.getPayMethod())
                    .set("notifyInfo", notifyInfo);
        } else {
            return result;
        }
    }


    @RequestMapping(value = "refundorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refundorder() {
        String orderId = getRequestString("oid");
        String memo = getRequestString("memo");
        String orderData = getRequestString("orderData");

        if (StringUtils.isBlank(orderData) || StringUtils.isBlank(memo) || StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage("参数错误");
        }

        List<OrderItem> orderItems = JsonUtils.fromJsonToList(orderData, OrderItem.class);
        if (CollectionUtils.isEmpty(orderItems) ||
                orderItems.stream().filter(e -> Double.compare(e.getRefundAmount(), 0d) <= 0).findFirst().orElse(null) != null) {
            return MapMessage.errorMessage("参数错误");
        }

        UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
        if (order == null) {
            return MapMessage.errorMessage("无效订单");
        }

        List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId()).stream().filter(e -> StringUtils.equals(e.getOrderId(), order.getId())).collect(Collectors.toList());
        UserOrderPaymentHistory paymentHistory = paymentHistories.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                .filter(o ->!o.getPayMethod().equals(PaymentConstants.PaymentGatewayName_17Zuoye_give))
                .findFirst().orElse(null);
        if (paymentHistory == null) {
            return MapMessage.errorMessage("找不到订单付款记录");
        }

        double totalRefundAmount = orderItems.stream().mapToDouble(OrderItem::getRefundAmount).sum();
        // 看看是否有退款记录了
        OrderRefundHistory history = userOrderLoaderClient.loadOrderRefundHistoryById(paymentHistory.getOuterTradeId());
        //放过测试的情况
        if(StringUtils.isNotBlank(paymentHistory.getOuterTradeId())){
            if (!"test".equals(paymentHistory.getOuterTradeId())) {
                if (history != null) {
                    if (history.getStatus() == RefundHistoryStatus.SUBMIT || history.getStatus() == RefundHistoryStatus.REFUNDING) {
                        return MapMessage.errorMessage("该订单有一笔未处理退款请求，请处理完后再进行退款");
                    }
                }
            }
        }

        List<UserOrderAmortizeHistory> amortizeList = userOrderLoaderClient.loadOrderAmortizeHistory(order.genUserOrderId());
        Map<String, BigDecimal> itemRe = new HashMap<>();
        if (orderItems.size() == 1 && "renjiaowholeBased".equals(orderItems.get(0).getRefundType())) {
            Map<String, BigDecimal> newItems = null;
            //包含奖学金重新计算摊销金额
            if(Objects.nonNull(order.getGiveBalance()) && order.getGiveBalance().compareTo(BigDecimal.ZERO) > 0) {
                newItems = getItemRealAmortize(order, paymentHistory);
            }
            //人教打包商品整体退
            List<String> itemIds = new LinkedList<>();
            List<Map> renjiaoItemsData = orderItems.get(0).getItemList();
            BigDecimal renjiaoRefundAmount = new BigDecimal(orderItems.get(0).getRefundAmount());
            BigDecimal renjiaoPartRefundAmount = new BigDecimal(0);
//            BigDecimal renjiaoTotalPaidAmount = amortizeList.stream().filter(e -> e.getPaymentStatus() == PaymentStatus.Paid).map(UserOrderAmortizeHistory::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal renjiaoTotalPaidAmount = paymentHistory.getPayAmount();
            if(Objects.nonNull(order.getGiveBalance()) && order.getGiveBalance().compareTo(BigDecimal.ZERO) > 0){
                renjiaoTotalPaidAmount = paymentHistory.getPayAmount().add(order.getGiveBalance());
            }
            for (int i = 0; i < renjiaoItemsData.size(); i++) {
                Map itemData = renjiaoItemsData.get(i);
                String itemId = (String) itemData.get("itemId");
                if (i == renjiaoItemsData.size() - 1) {
                    BigDecimal renjiaoItemRefundAmount = renjiaoRefundAmount.subtract(renjiaoPartRefundAmount);
                    itemRe.put(itemId, renjiaoItemRefundAmount);
                } else {
                    //各自item的金额/总金额 * 输入的退款金额
                    BigDecimal itemPaidAmount = BigDecimal.ZERO;
                    if(MapUtils.isNotEmpty(newItems)){
                        itemPaidAmount = newItems.get(itemId);
                    }else{
                        UserOrderAmortizeHistory itemPayAmortize = amortizeList.stream().filter(e -> e.getProductItemId().equals(itemId)).filter(e -> e.getPaymentStatus() == PaymentStatus.Paid).findFirst().orElse(null);
                        if (itemPayAmortize != null) {
                            itemPaidAmount = itemPayAmortize.getPayAmount();
                        }
                    }
                    BigDecimal renjiaoItemRefundAmount = renjiaoRefundAmount.multiply(itemPaidAmount).divide(renjiaoTotalPaidAmount, 2, RoundingMode.HALF_UP);
                    renjiaoPartRefundAmount = renjiaoPartRefundAmount.add(renjiaoItemRefundAmount);
                    itemRe.put(itemId, renjiaoItemRefundAmount);
                }
                itemIds.add(itemId);
            }
            amortizeList = amortizeList.stream().filter(e -> itemIds.contains(e.getProductItemId())).collect(Collectors.toList());
            List<UserOrderAmortizeHistory> refundAmortizeList = amortizeList.stream().filter(e -> e.getPaymentStatus() == PaymentStatus.Refund || e.getPaymentStatus() == PaymentStatus.Refunding).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(refundAmortizeList)) {
                return MapMessage.errorMessage("已经有退款了");
            }
        } else {
            List<String> orderItemIds = orderItems.stream().map(OrderItem::getItemId).collect(Collectors.toList());
            amortizeList = amortizeList.stream().filter(e -> orderItemIds.contains(e.getProductItemId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(amortizeList) && amortizeList.stream().filter(e -> OrderProductServiceType.safeParse(e.getOrderProductServiceType()) != OrderProductServiceType.YiQiXue).filter(e -> e.getPaymentStatus() == PaymentStatus.Refund).findFirst().orElse(null) != null) {
                //一起学的ITEM放过这个判断
                return MapMessage.errorMessage("已经有退款了");
            }
            orderItems.forEach(e -> {
                itemRe.put(e.getItemId(), new BigDecimal(Double.toString(e.getRefundAmount())));
            });
        }

        MapMessage refundMessage = CollectionUtils.isNotEmpty(amortizeList) ? userOrderRefundServiceClient.refund(order.getUserId(), order.getId(), itemRe, getCurrentAdminUser().getAdminUserName(), memo) :
                userOrderRefundServiceClient.refundNoAmortizeHistoryOrder(order.getUserId(), order.getId(), new BigDecimal(String.valueOf(totalRefundAmount)), getCurrentAdminUser().getAdminUserName(), memo);

        if (refundMessage != null && refundMessage.isSuccess()) {
            // 道具类扣减道具n
            if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.ValueAddedLiveTimesCard ||
                    OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.GrowingWorldProp ||
                    OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.EnglishStoryBook) {
                afentiAdminService.changeCardTimes(order);
            }
            if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook ||
                    (CollectionUtils.isNotEmpty(amortizeList) && amortizeList.stream().filter(e -> OrderProductServiceType.safeParse(e.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook).findFirst().orElse(null) != null)) {
                // 与第三方同步订单取消状态
                notifyThirdPartyCancelOrderIfNecessary(order);
            }
            // 发送邮件通知第三方
            sendThirdPartyEmailNotify(order, totalRefundAmount, paymentHistory.getOuterTradeId());
            return MapMessage.successMessage();
        } else {
            return refundMessage != null ? refundMessage : MapMessage.errorMessage("退款失败");
        }
    }

    private Map<String, BigDecimal> getItemRealAmortize(UserOrder order, UserOrderPaymentHistory paidHistory) {
        Set<String> productList = new HashSet<>();
        List<UserOrderProductRef> orderProducts = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
        if (CollectionUtils.isNotEmpty(orderProducts)) {
            for (UserOrderProductRef orderProductRef : orderProducts) {
                productList.add(orderProductRef.getProductId());
            }
        } else {
            productList.add(order.getProductId());
        }
        Map<String, List<OrderProductItem>> orderItems = userOrderLoaderClient.loadProductItemsByProductIds(productList);
        return UserOrderUtil.calculateAmortizeAmount(order, orderProducts, orderItems, paidHistory.getPayAmount().add(order.getGiveBalance()));
    }

    @RequestMapping(value = "batchrefundorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchRefundorder() {
        String memo = getRequestString("memo");
        String orderData = getRequestString("orderData");

        if (StringUtils.isBlank(orderData)) {
            return MapMessage.errorMessage("参数错误");
        }

        List<OrderItem> orderItemList = JsonUtils.fromJsonToList(orderData, OrderItem.class);
        if (CollectionUtils.isEmpty(orderItemList) ||
                orderItemList.stream().filter(e -> Double.compare(e.getRefundAmount(), 0d) <= 0).findFirst().orElse(null) != null) {
            return MapMessage.errorMessage("参数错误");
        }

        Map<String, List<OrderItem>> orderItemMap = orderItemList.stream().collect(Collectors.groupingBy(OrderItem::getOrderId, Collectors.toList()));
        List<Map<String, Object>> refundResults = new LinkedList<>();
        StringBuilder recordRefundSuccessOrderIds = new StringBuilder();
        StringBuilder recordRefundfailOrderIds = new StringBuilder();
        BigDecimal recordTotalRefundAmt = BigDecimal.ZERO;
        Integer recordOrderNum = 0;
        for (Map.Entry<String, List<OrderItem>> entry : orderItemMap.entrySet()) {
            String orderId = entry.getKey();
            List<OrderItem> orderItems = entry.getValue();
            UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
            if (order == null) {
                continue;
            }

            List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId()).stream().filter(e -> StringUtils.equals(e.getOrderId(), order.getId())).collect(Collectors.toList());
            UserOrderPaymentHistory paymentHistory = paymentHistories.stream()
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                    .findFirst().orElse(null);
            if (paymentHistory == null) {
                continue;
            }

            double totalRefundAmount = orderItems.stream().mapToDouble(OrderItem::getRefundAmount).sum();
            // 看看是否有退款记录了
            OrderRefundHistory history = userOrderLoaderClient.loadOrderRefundHistoryById(paymentHistory.getOuterTradeId());
            //放过测试的情况
            if (!"test".equals(paymentHistory.getOuterTradeId())) {
                if (history != null) {
                    if (history.getStatus() == RefundHistoryStatus.SUBMIT || history.getStatus() == RefundHistoryStatus.REFUNDING) {
                        MapMessage.errorMessage("该订单有一笔未处理退款请求，请处理完后再进行退款");
                    }
                }
            }

            List<String> orderItemIds = orderItems.stream().map(OrderItem::getItemId).collect(Collectors.toList());
            List<UserOrderAmortizeHistory> refundAmortizeList = userOrderLoaderClient.loadOrderAmortizeHistory(order.genUserOrderId()).stream().filter(e -> orderItemIds.contains(e.getProductItemId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(refundAmortizeList) && refundAmortizeList.stream().filter(e -> OrderProductServiceType.safeParse(e.getOrderProductServiceType()) != OrderProductServiceType.YiQiXue).filter(e -> e.getPaymentStatus() == PaymentStatus.Refund).findFirst().orElse(null) != null) {
//                return MapMessage.errorMessage("已经有退款了");
                continue;
            }

            Map<String, BigDecimal> itemRe = new HashMap<>();
            orderItems.forEach(e -> {
                itemRe.put(e.getItemId(), new BigDecimal(Double.toString(e.getRefundAmount())));
            });

            MapMessage refundMessage = CollectionUtils.isNotEmpty(refundAmortizeList) ?
                    userOrderRefundServiceClient.refund(order.getUserId(), order.getId(), itemRe, getCurrentAdminUser().getAdminUserName(), memo) :
                    userOrderRefundServiceClient.refundNoAmortizeHistoryOrder(order.getUserId(), order.getId(), new BigDecimal(String.valueOf(totalRefundAmount)), getCurrentAdminUser().getAdminUserName(), memo);

            if (refundMessage != null && refundMessage.isSuccess()) {
                // 道具类扣减道具n
                if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.ValueAddedLiveTimesCard ||
                        OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.GrowingWorldProp ||
                        OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.EnglishStoryBook) {
                    afentiAdminService.changeCardTimes(order);
                }
                if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook ||
                        (CollectionUtils.isNotEmpty(refundAmortizeList) && refundAmortizeList.stream().filter(e -> OrderProductServiceType.safeParse(e.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook).findFirst().orElse(null) != null)) {
                    // 与第三方同步订单取消状态
                    notifyThirdPartyCancelOrderIfNecessary(order);
                }
                // 发送邮件通知第三方
                sendThirdPartyEmailNotify(order, totalRefundAmount, paymentHistory.getOuterTradeId());
                //return MapMessage.successMessage();
                recordTotalRefundAmt = recordTotalRefundAmt.add(new BigDecimal(totalRefundAmount));
                recordOrderNum = recordOrderNum + 1;
                recordRefundSuccessOrderIds.append(orderId).append(",");
            } else {
                recordRefundfailOrderIds.append(orderId).append(",");
                if (refundMessage != null) {
                    Map<String, Object> errMap = new LinkedHashMap<>();
                    errMap.put("orderId", orderId);
                    errMap.put("errInfo", refundMessage.getInfo());
                    refundResults.add(errMap);
                } else {
                    Map<String, Object> errMap = new LinkedHashMap<>();
                    errMap.put("orderId", orderId);
                    errMap.put("errInfo", "退款失败");
                    refundResults.add(errMap);
                }
            }
        }
        MapMessage retMap = new MapMessage();
        if (refundResults.size() == 0) {
            retMap.setSuccess(true);
            retMap.setInfo("批量退款申请成功");
        } else if (orderItemMap.size() == refundResults.size()) {
            String info = "批量退款申请失败\n";
            for (Map<String, Object> errMap : refundResults) {
                String orderId = (String) errMap.get("orderId");
                String errInfo = (String) errMap.get("errInfo");
                info += "订单号:" + orderId + "   退款错误信息:" + errInfo + "\n";
            }
            retMap.setSuccess(true);
            retMap.setInfo(info);
        } else {
            String info = "部分订单退款申请成功,部分订单退款申请失败\n";
            for (Map<String, Object> errMap : refundResults) {
                String orderId = (String) errMap.get("orderId");
                String errInfo = (String) errMap.get("errInfo");
                info += "订单号:" + orderId + "   退款错误信息:" + errInfo + "\n";
            }
            retMap.setSuccess(true);
            retMap.setInfo(info);
        }
        //记录退款日志
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.批量退款.name());
        userServiceRecord.setOperationContent("退款总笔数：" + recordOrderNum + "。退款总金额：" + recordTotalRefundAmt.setScale(2, BigDecimal.ROUND_HALF_UP));
        userServiceRecord.setComments("批量申请退款成功的订单：" + recordRefundSuccessOrderIds.toString() + "；退款失败的订单：" + recordRefundfailOrderIds.toString());
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        return retMap;
    }

    @Getter
    @Setter
    private static class OrderItem implements Serializable {
        private static final long serialVersionUID = -4693267728454561883L;
        private String orderId;
        private String itemId;
        private double refundAmount;
        private List<Map> itemList;
        private String refundType;
    }

    // 计算订单的可退金额
    @RequestMapping(value = "/refundamount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage calculateRefundAmount() {
        String orderId = getRequestString("oid");
        Integer refundDays = getRequestInt("days", 0);

        try {
            UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
            if (null == order) return MapMessage.errorMessage("无效订单");
            if (!order.canBeRefund()) {
                return MapMessage.errorMessage("订单状态不可退款");
            }
            // 只能按倒序退订单
            MapMessage message = afentiAdminService.isLatestOrder(order);
            if (!message.isSuccess())
                return message;
            UserOrderPaymentHistory history = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId()).stream()
                    .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
                    .filter(h -> StringUtils.isNotBlank(h.getOrderId()) && h.getOrderId().equals(order.getId()))
                    .findFirst().orElse(null);
            Double amount = afentiAdminService.calculateRefundAmount(history, refundDays, order);
            if (amount <= 0) return MapMessage.errorMessage("可退金额为0");
            return MapMessage.successMessage().add("refundAmount", amount);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return MapMessage.errorMessage();
    }

    // 发起退款流程
    // 1 记录财务流水  2 退款历史中插入数据
    @RequestMapping(value = "refund.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refund() {
        String orderId = getRequestString("oid");
        Integer refundDays = getRequestInt("days", 0);
        String memo = getRequestString("memo");
        try {
            if (StringUtils.isBlank(memo)) return MapMessage.errorMessage("请输入备注信息");
            UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
            if (null == order) return MapMessage.errorMessage("无效订单");

            if (!order.canBeRefund()) {
                return MapMessage.errorMessage("订单状态不可退款");
            }
            UserOrderPaymentHistory history = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId()).stream()
                    .filter(h -> Objects.equals(h.getOrderId(), order.getId()))
                    .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
                    .findFirst().orElse(null);
            // 退款金额
            Double amount = afentiAdminService.calculateRefundAmount(history, refundDays, order);
            if (amount <= 0) {
                return MapMessage.errorMessage("可退金额为0");
            }
            //将订单退款
            MapMessage refundMsg = userOrderServiceClient.refundOrder(order.genUserOrderId());
            if (!refundMsg.isSuccess()) return refundMsg;

            // 获取流水号支付方式
            String outTradeNo = "";
            String payMethod = "";
            if (StringUtils.isNotBlank(order.getOldOrderId())) {
                AfentiOrder afentiOrder = afentiOrderServiceClient.getAfentiOrderService()
                        .loadAfentiOrder(order.getOldOrderId())
                        .getUninterruptibly();
                if (afentiOrder != null) {
                    outTradeNo = afentiOrder.getExtTradeNo();
                    payMethod = afentiOrder.getPayMethod();
                }
            } else {
                outTradeNo = history.getOuterTradeId();
                payMethod = history.getPayMethod();
            }

            // qpay 直接置成失败
            PaymentStatus paymentStatus = PaymentStatus.Refunding;
            RefundHistoryStatus refundStatus = RefundHistoryStatus.SUBMIT;
            if (StringUtils.equals(payMethod, PaymentConstants.PaymentGatewayName_QQ_StudentApp)) {
                paymentStatus = PaymentStatus.RefundFail;
                refundStatus = RefundHistoryStatus.FAIL;
            }
            // 记录退款流水
            UserOrderPaymentHistory paymentHistory = new UserOrderPaymentHistory();
            paymentHistory.setOrderId(order.getId());
            paymentHistory.setPayAmount(new BigDecimal(amount));
            paymentHistory.setPayDatetime(new Date());
            paymentHistory.setUserId(order.getUserId());
            paymentHistory.setPaymentStatus(paymentStatus);
            paymentHistory.setOuterTradeId(outTradeNo);
            paymentHistory.setPayMethod(payMethod);
            paymentHistory.setComment(memo);
            userOrderServiceClient.saveUserOrderPaymentHistory(paymentHistory);

            // 记录退款摊销历史
            List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
            OrderProductItem item = itemList.get(0);
            UserOrderAmortizeHistory amortizeHistory = new UserOrderAmortizeHistory();
            amortizeHistory.setOrderId(order.genUserOrderId());
            amortizeHistory.setPaymentHistoryId(history.getId());

            amortizeHistory.setUserId(order.getUserId());
            amortizeHistory.setAmortizeType(item.getAmortizeType());
            amortizeHistory.setOrderProductServiceType(item.getProductType());
            amortizeHistory.setOuterTradeId(outTradeNo);
            amortizeHistory.setPayAmount(new BigDecimal(amount));
            amortizeHistory.setPayDatetime(new Date());
            amortizeHistory.setPaymentStatus(PaymentStatus.Refund);
            amortizeHistory.setPayMethod(payMethod);
            amortizeHistory.setPeriod(0);
            amortizeHistory.setProductId(order.getProductId());
            amortizeHistory.setProductName(order.getProductName());
            amortizeHistory.setProductItemId(item.getId());
            amortizeHistory.setGrade(order.getGrade());
            amortizeHistory.setRegionCode(order.getRegionCode());
            amortizeHistory.setSchoolId(order.getSchoolId());
            userOrderServiceClient.saveUserAmortizeHistory(Collections.singletonList(amortizeHistory));

            // 判断是否作业币
            if (StringUtils.isNotBlank(payMethod) && Objects.equals(payMethod, PaymentConstants.PaymentGatewayName_17Zuoye)) {
                // 作业币支付的，退到作业币账户
                if (!createRefundFinanceFlow(order, amount)) {
                    return MapMessage.errorMessage("作业币退款失败，订单已退！");
                }
            } else {
                // 添加财务处理退款任务
                OrderRefundHistory refundHistory = new OrderRefundHistory();
                refundHistory.setId(outTradeNo);
                refundHistory.setPayMethod(payMethod);
                refundHistory.setRefundFee(new BigDecimal(amount));
                refundHistory.setOrderId(order.genUserOrderId());
                refundHistory.setStatus(refundStatus);
                refundHistory.setUserId(order.getUserId());
                refundHistory.setCreateDatetime(new Date());
                refundHistory.setUpdateDatetime(new Date());
                userOrderServiceClient.saveOrUpdateRefundHistory(refundHistory);
            }

            // 道具类扣减道具
            if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.ValueAddedLiveTimesCard ||
                    OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.GrowingWorldProp ||
                    OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.EnglishStoryBook) {
                afentiAdminService.changeCardTimes(order);
            }
            // 与第三方同步订单取消状态
            notifyThirdPartyCancelOrderIfNecessary(order);
            // 发送邮件通知第三方
            sendThirdPartyEmailNotify(order, amount, outTradeNo);

            // 记录CRM操作日志
            String operation = "订单号:" + orderId + ",产品名称:" + order.getProductName() + ",退款金额:" + amount + ",备注：" + memo;
            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(order.getUserId());
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.订单退款.name());
            userServiceRecord.setOperationContent("订单退款");
            userServiceRecord.setComments(operation);
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            return MapMessage.successMessage("退款申请成功");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private boolean createRefundFinanceFlow(UserOrder order, Double refundAmount) {
        // 获取退款的账户
        User user = userLoaderClient.loadUser(order.getUserId());
        if (user == null) {
            return false;
        }
        Long financeUserId = user.getId();
        if (user.fetchUserType() == UserType.STUDENT) {
            // 获取学生的家长账户 作业币支付由于开通了家长账户的支付， 在这里要找到对应的支付账户进行退款
            List<StudentParent> parents = parentLoaderClient.loadStudentParents(user.getId());
            if (CollectionUtils.isNotEmpty(parents)) {
                for (StudentParent parent : parents) {
                    List<FinanceFlow> flowList = financeServiceClient.getFinanceService()
                            .findUserFinanceFlows(parent.getParentUser().getId())
                            .getUninterruptibly();
                    if (CollectionUtils.isEmpty(flowList)) {
                        continue;
                    }
                    FinanceFlow flow = flowList.stream().filter(f -> StringUtils.equals(f.getOrderId(), order.getId()) ||
                            StringUtils.equals(f.getOrderId(), order.genUserOrderId())).findFirst().orElse(null);
                    if (flow != null) {
                        financeUserId = parent.getParentUser().getId();
                    }
                }
            }
        }
        FinanceFlowContext refundContext = FinanceFlowContext.instance()
                .userId(financeUserId)
                .type(FinanceFlowType.Refund)
                .state(FinanceFlowState.SUCCESS)
                .refer(FinanceFlowRefer.CRM)
                .orderId(order.genUserOrderId())
                .amount(new BigDecimal(refundAmount))
                .memo(order.getProductName())
                .payAmount(new BigDecimal(refundAmount));
        boolean result = financeServiceClient.getFinanceService().refund(refundContext).getUninterruptibly();
        PaymentStatus paymentStatus = PaymentStatus.Refund;
        if (!result) {
            paymentStatus = PaymentStatus.RefundFail;
        }
        // 更新退款状态
        UserOrderPaymentHistory paymentHistory = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId()).stream()
                .filter(h -> h.getPaymentStatus() == PaymentStatus.Refunding)
                .filter(h -> Objects.equals(h.getOrderId(), order.getId()))
                .findFirst().orElse(null);
        if (paymentHistory != null) {
            userOrderServiceClient.updatePaymentHistoryStatus(paymentHistory, paymentStatus);
        }
        return result;
    }


    private void sendThirdPartyEmailNotify(UserOrder order, Double refundAmount, String outNo) {
        Map<String, Object> emailMap = getNotifyEmail_ThirdParty();
        if (!emailMap.containsKey(order.getOrderProductServiceType())) return;

        String email = emailMap.get(order.getOrderProductServiceType()).toString();
        if (StringUtils.isBlank(email)) return;

        emailServiceClient.createPlainEmail()
                .to(email)
                .cc(getNotifyEmail_Cc())
                .subject("订单退款提醒")
                .body("订单" + order.getId() + "已被退款: 用户ID(" + order.getUserId() + ") 产品名称(" + order.getProductName() + ") 退款金额(" + refundAmount + ") 外部订单号(" + outNo + ")")
                .send();
    }

    //查询第三方通知邮件配置
    private Map<String, Object> getNotifyEmail_ThirdParty() {
        CommonConfig config = crmConfigService.$loadCommonConfigs().stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .filter(e -> CONFIG_CATEGORY_NAME.equals(e.getCategoryName()))
                .filter(e -> CONFIG_KEY_ORDER_REFUND_NOTIFY_EMAIL_THIRD_PARTY.equals(e.getConfigKeyName()))
                .sorted((o1, o2) -> {
                    long u1 = SafeConverter.toLong(o1.getUpdateDatetime());
                    long u2 = SafeConverter.toLong(o2.getUpdateDatetime());
                    return Long.compare(u2, u1);
                })
                .findFirst()
                .orElse(null);

        if (null == config) return Collections.emptyMap();
        Map<String, Object> emailMap = JsonUtils.fromJson(config.getConfigKeyValue());
        if (MapUtils.isEmpty(emailMap)) return Collections.emptyMap();

        return emailMap;
    }

    //查询通知邮件抄送配置
    private String getNotifyEmail_Cc() {
        CommonConfig config = crmConfigService.$loadCommonConfigs().stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .filter(e -> CONFIG_CATEGORY_NAME.equals(e.getCategoryName()))
                .filter(e -> CONFIG_KEY_ORDER_REFUND_NOTIFY_EMAIL_CC.equals(e.getConfigKeyName()))
                .sorted((o1, o2) -> {
                    long u1 = SafeConverter.toLong(o1.getUpdateDatetime());
                    long u2 = SafeConverter.toLong(o2.getUpdateDatetime());
                    return Long.compare(u2, u1);
                })
                .findFirst()
                .orElse(null);
        if (null == config) return null;
        return config.getConfigKeyValue();
    }

    private List<Map<String, Object>> mapAfentiOrderList(List<UserOrder> orderList, List<UserOrderPaymentHistory> userOrderPaymentHistoryList) {
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }
        Map<String, BigDecimal> refundableCountMap = new HashMap<>();
        for (int i = 0; i < orderList.size(); i += 20) {
            Map<String, BigDecimal> map = userOrderRefundServiceClient.getUserOrderRefundService().loadOrderRefundableCount(orderList.get(0).getUserId(),
                    orderList.subList(i, Math.min(orderList.size(), i + 20)).stream().map(UserOrder::getId).collect(Collectors.toList()));
            if (MapUtils.isNotEmpty(map)) {
                refundableCountMap.putAll(map);
            }
        }
        Map<String, List<UserOrderPaymentHistory>> userOrderPayHitoryMap = CollectionUtils.isEmpty(userOrderPaymentHistoryList) ? new HashMap<>() :
                userOrderPaymentHistoryList.stream().collect(Collectors.groupingBy(UserOrderPaymentHistory::getOrderId));
        Date now = new Date();
        List<Map<String, Object>> results = new ArrayList<>();
        Map<Long, WonderlandPromotionChannel> channelMap = wonderlandPromotionChannelServiceClient.getWonderlandPromotionChannelService().queryAll()
                .stream().collect(Collectors.toMap(WonderlandPromotionChannel::getId, Function.identity()));
        Map<String, BigDecimal> chipsRefundAmountMap = calChipsRefundAmount(orderList, userOrderPaymentHistoryList);
        for (UserOrder order : orderList) {
            List<UserOrderPaymentHistory> paymentHistory = userOrderPayHitoryMap.get(order.getId());
            // 计算订单可退款金额
            double refundAmount = refundableCountMap.get(order.getId()) != null ? refundableCountMap.get(order.getId()).doubleValue() : 0d;
            if (order.getOrderType() == OrderType.chips_english) {
                refundAmount = Optional.ofNullable(chipsRefundAmountMap.get(order.getId())).map(e -> e.doubleValue()).orElse(0.0);
            }
//            if (order.canBeRefund() && paymentHistory != null) {
//                UserOrderPaymentHistory paidHis = paymentHistory.stream().filter(h -> Objects.equals(h.getOrderId(), order.getId()))
//                        .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
//                        .findFirst().orElse(null);
//                refundAmount = afentiAdminService.calculateRefundAmount(order, paidHis);
//            }
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.genUserOrderId());
            orderMap.put("userId", order.getUserId());
            orderMap.put("productName", order.getProductName());
            orderMap.put("createDatetime", order.getCreateDatetime());
            orderMap.put("updateDatetime", order.getUpdateDatetime());
            orderMap.put("productServiceType", order.getOrderProductServiceType());
            orderMap.put("totalPrice", order.getOrderPrice());
            orderMap.put("orderStatus", order.getOrderStatus().getDesc());
            orderMap.put("refundAmount", refundAmount > 0 ? refundAmount : "-");
            orderMap.put("payStatus", order.getPaymentStatus().getDesc());
            orderMap.put("activateDatetime", order.getCreateDatetime());

            String refer = "";
            String[] channelIds = StringUtils.split(order.getOrderReferer(), ",");
            if (channelIds != null) {
                for (String channel : channelIds) {
                    WonderlandPromotionChannel realChannel = channelMap.get(SafeConverter.toLong(channel));
                    refer = refer + (realChannel == null ? channel : realChannel.getDescription()) + "|";
                }
                refer = StringUtils.substring(refer, 0, refer.length() - 1);
            }
            orderMap.put("orderReferer", refer);
            orderMap.put("canBePaid", order.canBePaid());
            //如果是一起学的订单，只要还有可退款金额，canBeRefund就是true
            if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.YiQiXue && refundAmount > 0) {
                orderMap.put("canBeRefund", true);
            } else {
                orderMap.put("canBeRefund", order.getPaymentStatus() == PaymentStatus.Paid);
            }

            List<UserOrderProductRef> refList = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
            //一笔订单包含多个product的可以多次退款，
            if (CollectionUtils.isNotEmpty(refList) && refList.size() > 1) {
                //查摊销表，判断是否是所有的product都已经退完
                List<UserOrderAmortizeHistory> refundAmortizeHistories = userOrderLoaderClient.loadOrderAmortizeHistory(order.genUserOrderId()).stream().filter(o -> o.getPaymentStatus() == PaymentStatus.Refund).collect(Collectors.toList());
                if (refundAmortizeHistories.size() < refList.size()) {
                    orderMap.put("canBeRefund", true);
                }
            }
            //一笔订单包含多个ITEM的情况,判断是否展示退款按钮
            List<UserOrderAmortizeHistory> userOrderAmortizePaid = userOrderLoaderClient.loadOrderAmortizeHistory(order.genUserOrderId()).stream().filter(o -> o.getPaymentStatus() == PaymentStatus.Paid).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(refList) && CollectionUtils.isNotEmpty(userOrderAmortizePaid) && userOrderAmortizePaid.size() > 1) {
                List<UserOrderAmortizeHistory> userOrderAmortizeRefund = userOrderLoaderClient.loadOrderAmortizeHistory(order.genUserOrderId()).stream().filter(o -> o.getPaymentStatus() == PaymentStatus.Refund).collect(Collectors.toList());
                if (userOrderAmortizeRefund.size() < userOrderAmortizePaid.size()) {
                    orderMap.put("canBeRefund", true);
                }
            }

            orderMap.put("bindCoupon", StringUtils.isNotBlank(order.getCouponRefId()));
            orderMap.put("outOfDate", order.canBeRefund() && refundAmount > 0
                    && CollectionUtils.isNotEmpty(paymentHistory) && paymentHistory.get(0).getServiceEndTime() != null
                    && now.after(paymentHistory.get(0).getServiceEndTime()));

            boolean canChangeBook = false;
            if (order.getPaymentStatus() == PaymentStatus.Paid) {
                if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.WalkerMan) {
                    canChangeBook = true;
                } else if (AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
                    List<UserOrderProductRef> userOrderProductRefs = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
                    if (CollectionUtils.isNotEmpty(userOrderProductRefs)) {
                        UserOrderProductRef userOrderProductRef = userOrderProductRefs.stream().filter(ref -> OrderProductServiceType.safeParse(ref.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook).findFirst().orElse(null);
                        if (null != userOrderProductRef) {
                            canChangeBook = true;
                        }
                    }
                }
            }
            orderMap.put("canChangeBook", canChangeBook);
            results.add(orderMap);
        }
        return results;
    }

    private List<Map<String, Object>> mapAfentiOldOrderList(List<UserOrder> orderList) {
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }
        Date now = new Date();
        List<Map<String, Object>> results = new ArrayList<>();
        for (UserOrder order : orderList) {
            // 计算订单可退款金额
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.genUserOrderId());
            orderMap.put("userId", order.getUserId());
            orderMap.put("productName", order.getProductName());
            orderMap.put("createDatetime", order.getCreateDatetime());
            orderMap.put("updateDatetime", order.getUpdateDatetime());
            orderMap.put("productServiceType", order.getOrderProductServiceType());
            orderMap.put("totalPrice", order.getOrderPrice());
            orderMap.put("orderStatus", order.getOrderStatus().getDesc());
            orderMap.put("payStatus", order.getPaymentStatus().getDesc());
            orderMap.put("activateDatetime", order.getCreateDatetime());
            orderMap.put("orderReferer", order.getOrderReferer());
            orderMap.put("canBePaid", order.canBePaid());
            // 历史订单要显示出来信息  支付方式  支付金额  流水号  服务开始时间  服务结束时间
            AfentiOrder afentiOrder = afentiOrderServiceClient.getAfentiOrderService()
                    .loadAfentiOrder(order.getOldOrderId())
                    .getUninterruptibly();
            if (afentiOrder != null) {
                orderMap.put("payMethod", afentiOrder.getPayMethod());
                orderMap.put("payAmount", afentiOrder.getPayAmount());
                orderMap.put("outTradeNo", afentiOrder.getExtTradeNo());
                orderMap.put("serviceStartTime", afentiOrder.getServiceStartDatetime());
                orderMap.put("serviceEndTime", afentiOrder.getServiceEndDatetime());
                orderMap.put("refundAmount", afentiOrder.getPayAmount() != null && afentiOrder.getPayAmount() > 0 ? afentiOrder.getPayAmount() : "-");
                orderMap.put("canBeRefund", order.canBeRefund() && afentiOrder.getPayAmount() != null && afentiOrder.getPayAmount() > 0);
                orderMap.put("outOfDate", order.canBeRefund() && afentiOrder.getPayAmount() != null && afentiOrder.getPayAmount() > 0
                        && afentiOrder.getServiceEndDatetime() != null
                        && now.after(afentiOrder.getServiceEndDatetime()));
            }
            results.add(orderMap);
        }
        return results;
    }

    @RequestMapping(value = "booksforchange.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage booksForChange() {
        String orderId = getRequestString("oid");
        if (StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
            if (null == userOrder) {
                return MapMessage.errorMessage("未查询到订单");
            }
            if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) != OrderProductServiceType.WalkerMan && OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook && !AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()))) {
                return MapMessage.errorMessage("此订单不支持换购教材");
            }
            if (userOrder.getPaymentStatus() != PaymentStatus.Paid) {
                return MapMessage.errorMessage("此订单还未支付，不需要换购");
            }

            //查出当前订单买的教材是哪个出版社的
            //如果是阿分提提高版，要通过接口查对应的点读机产品id，不能使用订单里的产品id
            List<OrderProduct> orderProducts = userOrderLoaderClient.loadAvailableProductForCrm();
            OrderProduct orderProduct = getProductInOrderForChangeBook(userOrder, orderProducts);
            if (null == orderProduct) {
                return MapMessage.errorMessage("订单中的产品未知");
            }

            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId());
            if (CollectionUtils.isEmpty(orderProductItems)) {
                return MapMessage.errorMessage("此订单点读机产品没有子产品，不可换购");
            }
            if (orderProductItems.size() > 1) {
                return MapMessage.errorMessage("此订单点读机产品子产品大于1个，不可换购");
            }
            OrderProductItem orderProductItem = orderProductItems.get(0);
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(orderProductItem.getAppItemId());
            if (null == newBookProfile) {
                return MapMessage.errorMessage("当前订单对应的教材未知,不能换购");
            }
            String publisher = newBookProfile.getShortPublisher();
            if (StringUtils.isBlank(publisher)) {
                return MapMessage.errorMessage("当前订单教材的出版社未知，不能换购");
            }

            //查出与当前订单教材相同出版社的教材
            orderProducts = orderProducts.stream().filter(p -> "ONLINE".equals(p.getStatus())).collect(Collectors.toList());
            List<OrderProduct> productsToChange = new ArrayList<>();
            for (OrderProduct product : orderProducts) {
                if (product.getId().equals(userOrder.getProductId())) {
                    continue;
                }
                if (OrderProductServiceType.WalkerMan == OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) && OrderProductServiceType.WalkerMan != OrderProductServiceType.safeParse(product.getProductType())) {
                    continue;
                }
                List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
                if (CollectionUtils.isEmpty(items) || items.size() > 1) {
                    continue;   //如果此商品对应的子产品没有或大于1个，不能换购成这个商品
                }

                NewBookProfile bookProfile = newContentLoaderClient.loadBook(items.get(0).getAppItemId());
                if (null == bookProfile) {
                    continue;
                }
                if (publisher.equals(bookProfile.getShortPublisher())) {
                    productsToChange.add(product);
                }
            }

            return MapMessage.successMessage().add("books", productsToChange);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private OrderProduct getProductInOrderForChangeBook(UserOrder userOrder, List<OrderProduct> orderProducts) {
        OrderProduct orderProduct = null;
        if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook || OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) == OrderProductServiceType.WalkerMan) {
            orderProduct = orderProducts.stream().filter(p -> p.getId().equals(userOrder.getProductId())).findFirst().orElse(null);
        } else if (AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()))) {
            List<UserOrderProductRef> userOrderProductRefs = userOrderLoaderClient.loadOrderProducts(userOrder.getUserId(), userOrder.getId());
            if (CollectionUtils.isNotEmpty(userOrderProductRefs)) {
                UserOrderProductRef userOrderProductRef = userOrderProductRefs.stream().filter(ref -> OrderProductServiceType.safeParse(ref.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook).findFirst().orElse(null);
                if (null != userOrderProductRef) {
                    orderProduct = orderProducts.stream().filter(p -> p.getId().equalsIgnoreCase(userOrderProductRef.getProductId())).findFirst().orElse(null);
                }
            }
        }
        return orderProduct;
    }

    private Long getUserIdActivatedInOrderForChangeBook(UserOrder userOrder) {
        if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook || OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) == OrderProductServiceType.WalkerMan) {
            return userOrder.getUserId();
        } else if (AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()))) {
            List<UserOrderProductRef> userOrderProductRefs = userOrderLoaderClient.loadOrderProducts(userOrder.getUserId(), userOrder.getId());
            if (CollectionUtils.isNotEmpty(userOrderProductRefs)) {
                UserOrderProductRef userOrderProductRef = userOrderProductRefs.stream().filter(ref -> OrderProductServiceType.safeParse(ref.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook).findFirst().orElse(null);
                if (null != userOrderProductRef) {
                    return userOrderProductRef.getRelatedUserId();
                }
            }
        }
        return null;
    }

    @RequestMapping(value = "changebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBook() {
        String orderId = getRequestString("oid");
        String productId = getRequestString("pid");
        if (StringUtils.isBlank(orderId) || StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
            if (null == userOrder) {
                return MapMessage.errorMessage("未查询到订单");
            }
            if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) != OrderProductServiceType.WalkerMan && OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook && !AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()))) {
                return MapMessage.errorMessage("此订单不支持换购教材");
            }
            if (userOrder.getPaymentStatus() != PaymentStatus.Paid) {
                return MapMessage.errorMessage("此订单还未支付，不需要换购");
            }

            //查出当前订单买的教材是哪个出版社的
            List<OrderProduct> orderProducts = userOrderLoaderClient.loadAvailableProductForCrm();
            OrderProduct newOrderProduct = orderProducts.stream().filter(p -> p.getId().equals(productId)).findFirst().orElse(null);
            OrderProduct orderProduct = getProductInOrderForChangeBook(userOrder, orderProducts);
            if (null == orderProduct) {
                return MapMessage.errorMessage("订单中的产品未知");
            }

            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId());
            if (CollectionUtils.isEmpty(orderProductItems)) {
                return MapMessage.errorMessage("此订单点读机产品没有子产品，不可换购");
            }
            if (orderProductItems.size() > 1) {
                return MapMessage.errorMessage("此订单点读机产品子产品大于1个，不可换购");
            }
            OrderProductItem orderProductItem = orderProductItems.get(0);
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(orderProductItem.getAppItemId());
            if (null == newBookProfile) {
                return MapMessage.errorMessage("当前订单对应的教材未知,不能换购");
            }
            String publisher = newBookProfile.getShortPublisher();
            if (StringUtils.isBlank(publisher)) {
                return MapMessage.errorMessage("当前订单教材的出版社未知，不能换购");
            }

            //要换的教材必须与当前订单里的教材同一个出版社
            List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(productId);
            if (CollectionUtils.isEmpty(items)) {
                return MapMessage.errorMessage("要换购的产品没有子产品，不能换购");
            }
            if (items.size() > 1) {
                return MapMessage.errorMessage("要换购的产品子产品大于1个，不能换购");
            }
            NewBookProfile bookProfile = newContentLoaderClient.loadBook(items.get(0).getAppItemId());
            if (null == bookProfile) {
                return MapMessage.errorMessage("要换购的教材不存在，不能换购");
            }
            if (!publisher.equals(bookProfile.getShortPublisher())) {
                return MapMessage.errorMessage("要换购的教材与当前订单里的教材不是同一个出版社，不能换购");
            }

            //查出激活历史，等待更新
            Long activatedUserId = getUserIdActivatedInOrderForChangeBook(userOrder);
            if (null == activatedUserId) {
                return MapMessage.errorMessage("未查询到激活历史所属用户");
            }
            List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(activatedUserId);
            if (CollectionUtils.isEmpty(userActivatedProducts)) {
                return MapMessage.errorMessage("没找到用户的产品激活历史，不能换购");
            }
            UserActivatedProduct userActivatedProduct = userActivatedProducts.stream()
                    .filter(a -> null != a.getProductItemId())
                    .filter(a -> a.getProductItemId().equals(orderProductItem.getId())).findFirst().orElse(null);
            if (null == userActivatedProduct) {
                return MapMessage.errorMessage("当前订单没有激活历史");
            }

            TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(items.get(0).getAppItemId());
            if (null != sdkInfo && StringUtils.isNotBlank(sdkInfo.getSdkBookIdV2())) {
                MapMessage ret = picListenBookOrderService.notifyThirdPartyChangeOrderBook(userOrder.genUserOrderId(), sdkInfo.getSdkBookIdV2());
                if (!ret.isSuccess()) {
                    return MapMessage.errorMessage("换购失败，" + ret.getInfo());
                }
            }


            //更新激活历史里的productItemId
            List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(userOrder.getOrderProductServiceType(), userOrder.getUserId());
            long orderCount = userOrders.stream()
                    .filter(order -> order.getProductId().equals(userOrder.getProductId()))
                    .filter(order -> order.getOrderStatus() == OrderStatus.Confirmed)
                    .count();
            OrderProductItem newItem = items.get(0);
            // 更新摊销表里的itemId
            List<UserOrderAmortizeHistory> amortizeHistoryList = userOrderLoaderClient.loadOrderAmortizeHistory(userOrder.genUserOrderId());
            if (CollectionUtils.isNotEmpty(amortizeHistoryList)) {
                UserOrderAmortizeHistory history = amortizeHistoryList.stream().filter(h -> Objects.equals(h.getUserId(), userActivatedProduct.getUserId()))
                        .filter(h -> Objects.equals(h.getProductItemId(), orderProductItem.getId()))
                        .findFirst().orElse(null);
                if (null != history) {
                    history.setProductId(productId);
                    history.setProductName(newOrderProduct.getName());
                    history.setProductItemId(newItem.getId());
                    history.setUpdateDatetime(new Date());
                    userOrderServiceClient.updateUserOrderAmortizeHistory(history);
                }
            }
            if (orderCount == 1) {
                //只有一个订单，直接更新激活记录
                userActivatedProduct.setProductItemId(newItem.getId());
                userActivatedProduct.setUpdateDatetime(new Date());
                userOrderServiceClient.updateUserActivatedProduct(userActivatedProduct);
            } else {
                //有多个订单，需要把激活记录拆出来建一个新的
                userActivatedProduct.setServiceEndTime(DateUtils.addDays(userActivatedProduct.getServiceEndTime(), 0 - newItem.getPeriod()));
                userActivatedProduct.setUpdateDatetime(new Date());
                userOrderServiceClient.updateUserActivatedProduct(userActivatedProduct);

                UserActivatedProduct newUserActivatedProduct = new UserActivatedProduct();
                newUserActivatedProduct.setUserId(userActivatedProduct.getUserId());
                newUserActivatedProduct.setProductServiceType(userActivatedProduct.getProductServiceType());
                newUserActivatedProduct.setSchoolId(userActivatedProduct.getSchoolId());
                newUserActivatedProduct.setProductItemId(newItem.getId());
                newUserActivatedProduct.setServiceStartTime(userActivatedProduct.getServiceEndTime());
                newUserActivatedProduct.setServiceEndTime(DateUtils.addDays(newUserActivatedProduct.getServiceStartTime(), newItem.getPeriod()));
                newUserActivatedProduct.setDisabled(false);
                newUserActivatedProduct.setCreateDatetime(new Date());
                newUserActivatedProduct.setUpdateDatetime(new Date());
                userOrderServiceClient.saveUserActivatedProduct(newUserActivatedProduct);
            }

            //修改订单数据
            //更新userOrder里的productId
            userOrderServiceClient.getUserOrderService().updateUserOrderForExchange(userOrder, newOrderProduct.getId(), newOrderProduct.getName(), userOrder.getExtAttributes());

            //write operation log
            addAdminLog("点读机教材换购", orderId, "管理员" + getCurrentAdminUser().getAdminUserName() + "操作由" + orderProduct.getName() + "(id:" + orderProduct.getId() + ")换购为" + newOrderProduct.getName() + "(id:" + newOrderProduct.getId() + ")");
            UserServiceRecord serviceRecord = new UserServiceRecord();
            serviceRecord.setOperationType(UserServiceRecordOperationType.客服添加.name());
            serviceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            serviceRecord.setUserId(userOrder.getUserId());
            serviceRecord.setOperationContent("管理员" + getCurrentAdminUser().getAdminUserName() + "操作由" + orderProduct.getName() + "(id:" + orderProduct.getId() + ")换购为" + newOrderProduct.getName() + "(id:" + newOrderProduct.getId() + ")");
            serviceRecord.setComments("教材换购");
            userServiceClient.saveUserServiceRecord(serviceRecord);


            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private void notifyThirdPartyCancelOrderIfNecessary(UserOrder order) {
        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook || AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
            picListenBookOrderService.notifyThirdPartyCancelOrder(order.genUserOrderId());
        }
    }

    /**
     * 计算薯条英语的可退款金额， 订单维度的
     * chipsRefundTextMap
     * @param orderList
     * @param userOrderPaymentHistoryList
     * @return
     */
    private Map<String, BigDecimal> calChipsRefundAmount(List<UserOrder> orderList, List<UserOrderPaymentHistory> userOrderPaymentHistoryList) {
        List<UserOrder> chipsOrderList = Optional.ofNullable(orderList).map(l -> l.stream().filter(e -> e.getOrderType() == OrderType.chips_english).collect(Collectors.toList())).orElse(null);
        if (CollectionUtils.isEmpty(chipsOrderList)) {
            return Collections.emptyMap();
        }
        Map<String, UserOrderPaymentHistory> paymentHistoryMap = Optional.ofNullable(userOrderPaymentHistoryList)
                .map(l -> l.stream().filter(p -> p.getPaymentStatus() == PaymentStatus.Paid).collect(Collectors.toMap(UserOrderPaymentHistory::getOrderId, Function.identity(), (k1, k2) -> k1)))
                .orElse(Collections.emptyMap());
        return chipsOrderList.stream().collect(Collectors.toMap(UserOrder::getId, o -> calChipsRefundAmount(o, paymentHistoryMap.get(o.getId())), (k1, k2) -> k2));
    }

    private BigDecimal calChipsRefundAmount(UserOrder order, UserOrderPaymentHistory paymentHistory) {
        if (order == null || StringUtils.isBlank(order.getProductId()) || paymentHistory == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal payAmount = Optional.ofNullable(paymentHistory.getPayAmount()).map(a -> {
            Double d = chipsRefundTextMap.get(order.genUserOrderId());
            if (d == null) {
                return a;
            } else {
                return a.subtract(BigDecimal.valueOf(d));
            }
        }).orElse(BigDecimal.ZERO);
        Date startDate = DayRange.current().getStartDate();
        ChipsEnglishProductTimetable timetable = chipsEnglishContentLoader.loadChipsEnglishProductTimetableById(order.getProductId());
        if (timetable == null || !startDate.before(timetable.getEndDate())) {
            return BigDecimal.ZERO;
        }
        if (CollectionUtils.isEmpty(timetable.getCourses())) {
            return payAmount;
        }
        long remainCount = timetable.getCourses().stream().filter(c -> c.getBeginDate().after(startDate)).count();//未开课的单元数量
        BigDecimal refundCount = payAmount.multiply(BigDecimal.valueOf(remainCount))
                .divide(BigDecimal.valueOf(timetable.getCourses().size()), 2, RoundingMode.FLOOR);
//        BigDecimal refundCount = Optional.ofNullable(payAmount).map(d -> d.multiply(BigDecimal.valueOf(remainCount))
//                .divide(BigDecimal.valueOf(timetable.getCourses().size()), 2, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        return refundCount;
    }

    /**
     * 薯条英语可退费金额
     * @param payAmount
     * @param courseList
     * @return
     */
    private BigDecimal loadOrderRefundDetail(BigDecimal payAmount, List<ChipsEnglishProductTimetable.Course> courseList) {
        if (CollectionUtils.isEmpty(courseList)) {
            return payAmount;
        }
        Date startDate = DayRange.current().getStartDate();
        long remainCount = courseList.stream().filter(c -> c.getBeginDate().after(startDate)).count();//未开课的单元数量
        BigDecimal refundCount = Optional.ofNullable(payAmount)
                .map(d -> d.multiply(BigDecimal.valueOf(remainCount)).divide(BigDecimal.valueOf(courseList.size()), 2, RoundingMode.FLOOR))
                .orElse(BigDecimal.ZERO);
        return refundCount;
    }

    private Map<String, List<OrderProductItem>> loadOrderProductItem(UserOrder userOrder) {
        List<UserOrderProductRef> userOrderProductRefs = userOrderLoaderClient.loadOrderProducts(userOrder.getUserId(), userOrder.getId());
        Map<String, List<OrderProductItem>> map = new HashMap<>();
        if (CollectionUtils.isEmpty(userOrderProductRefs)) {
            List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(userOrder.getProductId())
                    .stream()
                    .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                    .collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(itemList)) {
                map.put(userOrder.getProductId(), itemList);
//            }
        } else {
            for (UserOrderProductRef userOrderProductRef : userOrderProductRefs) {
                List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(userOrderProductRef.getProductId()).stream()
                        .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                        .collect(Collectors.toList());
//                if (CollectionUtils.isNotEmpty(itemList)) {
                    map.put(userOrderProductRef.getProductId(), itemList);
//                }
            }
        }
        return map;
    }

    /**
     * 薯条英语子产品对应的课程表
     * @param productToItemMap
     * @return
     */
    private Map<String, List<ChipsEnglishProductTimetable.Course>> itemToTimeTableCourse(Map<String, List<OrderProductItem>> productToItemMap) {
        if (MapUtils.isEmpty(productToItemMap)) {
            return Collections.emptyMap();
        }
        Map<String, List<ChipsEnglishProductTimetable.Course>> itemToCourseMap = new HashMap<>();
        productToItemMap.forEach((k, v) -> {
            ChipsEnglishProductTimetable timetable = chipsEnglishContentLoader.loadChipsEnglishProductTimetableById(k);
            Map<String, List<ChipsEnglishProductTimetable.Course>> bookToCourseMap = Optional.ofNullable(timetable)
                    .map(t -> t.getCourses().stream().collect(Collectors.groupingBy(ChipsEnglishProductTimetable.Course::getBookId)))
                    .orElse(Collections.emptyMap());
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            v.forEach(item -> {
                List<ChipsEnglishProductTimetable.Course> courseList = bookToCourseMap.get(item.getAppItemId());
                if (CollectionUtils.isNotEmpty(courseList)) {
                    itemToCourseMap.put(item.getId(), courseList);
                }
            });
        });
        return itemToCourseMap;
    }

}
