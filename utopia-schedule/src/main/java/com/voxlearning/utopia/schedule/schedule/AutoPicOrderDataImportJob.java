package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicyCallback;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.mizar.api.entity.order.PicOrderInfo;
import com.voxlearning.utopia.service.mizar.api.service.PicOrderInfoService;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/3/7.
 */
@Named
@ScheduledJobDefinition(
        jobName = "人教订单数据导出",
        jobDescription = "用于人教订单数据在Mizar端的查询",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 10 5 * * ? "
)
@ProgressTotalWork(100)
public class AutoPicOrderDataImportJob extends ScheduledJobWithJournalSupport {


    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;


    @ImportService(interfaceClass = PicOrderInfoService.class)
    private PicOrderInfoService picOrderInfoService;

    private UtopiaSql utopiaSql;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    private Integer tableCount;

    @PostConstruct
    private void init() {
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
            tableCount = 2;
        } else {
            utopiaSql = utopiaSqlFactory.getUtopiaSql("order");
            tableCount = 100;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<Map<String, Object>> mapList;


        Date startDate = DateUtils.stringToDate(DateUtils.getYesterdaySqlDate(), DateUtils.FORMAT_SQL_DATE);
        Date endDate = DateUtils.stringToDate(DateUtils.getTodaySqlDate(), DateUtils.FORMAT_SQL_DATE);

        if (RuntimeMode.isStaging()) {
            String start = SafeConverter.toString(parameters.get("startDate"));
            String end = SafeConverter.toString(parameters.get("endDate"));
            startDate = DateUtils.stringToDate(start, DateUtils.FORMAT_SQL_DATE);
            endDate = DateUtils.stringToDate(end, DateUtils.FORMAT_SQL_DATE);
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startTime", startDate);
        paramMap.put("endTime", endDate);
        //查出orderId,userId,productId,productName
        mapList = generateUserOrderInfo(paramMap);
        progressMonitor.worked(5);
        Set<Long> userIds = new HashSet<>();
        Set<String> productIds = new HashSet<>();
        Set<String> groupOrderIds = new HashSet<>();
        for (Map<String, Object> orderMap : mapList) {
            productIds.add(SafeConverter.toString(orderMap.get("productId")));
            if (SafeConverter.toString(orderMap.get("productType")).equals(OrderProductServiceType.GroupProduct.name())) {
                groupOrderIds.add(SafeConverter.toString(orderMap.get("orderId")));
                userIds.add(SafeConverter.toLong(orderMap.get("ext")));
            } else {
                userIds.add(SafeConverter.toLong(orderMap.get("userId")));
            }
        }
        List<UserActivatedProduct> activatedProductList = new ArrayList<>();
        Map<String, Map<String, Date>> orderDateMap = new HashMap<>();
        Map<String, List<String>> orderBookMap = new HashMap<>();
        //用userId取activatedProduct,为了后面能够取出serviceTime
        if (CollectionUtils.isNotEmpty(userIds)) {
            userIds.stream().forEach(e -> activatedProductList.addAll(userOrderLoaderClient.loadUserActivatedProductList(e)));
        }
        //用productId取出productItemId,与之前的activatedProduct对比,取出serviceTime
        Map<String, List<String>> productItemMap = new HashMap<>();
        //取教材信息
        Map<String, String> bookIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(productIds)) {
            Map<String, List<OrderProductItem>> stringListMap = userOrderLoaderClient.loadProductItemsByProductIds(productIds);
            //人教订单不只有一个item
            stringListMap.entrySet().stream().filter(productItem -> CollectionUtils.isNotEmpty(productItem.getValue())).forEach(productItem -> {
                productItem.getValue().forEach(p -> {
                    if (OrderProductServiceType.safeParse(p.getProductType()) != OrderProductServiceType.PicListenBook) {
                        return;
                    }
                    List<String> currentProductIds;
                    if (productItemMap.containsKey(p.getId())) {
                        currentProductIds = productItemMap.get(p.getId());
                        currentProductIds.add(productItem.getKey());
                        productItemMap.put(p.getId(), currentProductIds);
                    } else {
                        currentProductIds = new ArrayList<>();
                        currentProductIds.add(productItem.getKey());
                        productItemMap.put(p.getId(), currentProductIds);
                    }
//                        productItemMap.put(p.getId(), productItem.getKey());
                    bookIdMap.put(p.getId(), p.getAppItemId());
                });
            });
        }
        if (CollectionUtils.isNotEmpty(activatedProductList)) {
            activatedProductList.stream().filter(activatedProduct -> productItemMap.keySet().contains(activatedProduct.getProductItemId())).forEach(activatedProduct -> {
                Map<String, Date> map = new HashMap<>();
//                Map<String, String> bookMap = new HashMap<>();
                map.put("serviceStartTime", activatedProduct.getServiceStartTime());
                map.put("serviceEndTime", activatedProduct.getServiceEndTime());
//                bookMap.put("bookId", bookIdMap.get(activatedProduct.getProductItemId()));
                productItemMap.get(activatedProduct.getProductItemId()).forEach(e -> {
                    List<String> bookIds;
                    if (orderBookMap.containsKey(e)) {
                        bookIds = orderBookMap.get(e);
                        bookIds.add(bookIdMap.get(activatedProduct.getProductItemId()));
                        orderBookMap.put(e, bookIds);
                    } else {
                        bookIds = new ArrayList<>();
                        bookIds.add(bookIdMap.get(activatedProduct.getProductItemId()));
                        orderBookMap.put(e, bookIds);
                    }
//                    orderBookMap.put(productItemMap.get(activatedProduct.getProductItemId()), bookMap);
                    orderDateMap.put(e, map);
                });
            });
        }
        List<String> orderIds = mapList.stream().map(map -> SafeConverter.toString(map.get("orderId"))).collect(Collectors.toList());
        paramMap.put("orderIds", orderIds);
        if (CollectionUtils.isEmpty(orderIds)) {
            return;
        }
        List<Map<String, Object>> orderHistoryList = generateUserOrderPaymentInfo(paramMap);
        progressMonitor.worked(30);
        List<Map<String, Object>> resultList = new ArrayList<>();
        Set<String> bookIds = new HashSet<>();
        for (Map<String, Object> historyMap : orderHistoryList) {
            try {
                mapList.stream().filter(orderMap -> SafeConverter.toString(historyMap.get("orderId")).equals(SafeConverter.toString(orderMap.get("orderId")))).forEach(orderMap -> {
                    Map<String, Date> dateMap = orderDateMap.get(SafeConverter.toString(orderMap.get("productId")));
                    List<String> bookInfo = orderBookMap.get(SafeConverter.toString(orderMap.get("productId")));
                    if (MapUtils.isNotEmpty(dateMap)) {
                        historyMap.put("serviceStartTime", dateMap.get("serviceStartTime"));
                        historyMap.put("serviceEndTime", dateMap.get("serviceEndTime"));
                    }
                    if (CollectionUtils.isNotEmpty(bookInfo)) {
                        historyMap.put("bookId", bookInfo);
                        bookIds.addAll(bookInfo);
                    }
                    if (groupOrderIds.contains(SafeConverter.toString(orderMap.get("orderId")))) {
                        historyMap.put("payAmount", 20);
                    }
                    historyMap.put("productName", orderMap.get("productName"));
                    historyMap.put("userId", orderMap.get("userId"));
                    resultList.add(historyMap);
                });
            } catch (Exception e) {
                logger.warn("fail add", e);
            }

            logger.info("resultListSize:{}", resultList.size());
        }
        List<Map<String, Object>> currentResultList = new ArrayList<>();
        Set<String> currentBookIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(bookIds)) {
            currentBookIds = newContentLoaderClient.loadBooks(bookIds).values().stream().filter(e -> StringUtils.equals(e.getPublisher(), "人民教育出版社")
                    || StringUtils.equals(e.getPublisher(), "山东科学技术出版社")
                    || StringUtils.equals(e.getPublisher(), "上海教育出版社")
                    || StringUtils.equals(e.getPublisher(), "辽宁师范大学出版社"))
                    .map(NewBookProfile::getId)
                    .collect(Collectors.toSet());
            Set<String> finalCurrentBookIds = currentBookIds;
            currentResultList = resultList.stream().filter(e -> {
                List<String> bids = (List<String>) e.get("bookId");
                return bids.stream().anyMatch(finalCurrentBookIds::contains);
            }).collect(Collectors.toList());
        }
        progressMonitor.worked(50);
        List<PicOrderInfo> picOrderInfos = generatePicOrderInfoList(currentResultList, currentBookIds);
        if (CollectionUtils.isNotEmpty(picOrderInfos)) {
            picOrderInfos.stream().filter(order -> order != null).forEach(order -> {
                picOrderInfoService.insertPicOrderInfo(order);
            });
        }
        progressMonitor.done();
    }

    private List<Map<String, Object>> generateUserOrderInfo(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        RoutingPolicyExecutorBuilder.getInstance()
                .<List<Map<String, Object>>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(new UtopiaRoutingDataSourcePolicyCallback<List<Map<String, Object>>>() {
                    @Override
                    public List<Map<String, Object>> doInRoutingPolicy() {
                        for (int tableIndex = 0; tableIndex < tableCount; tableIndex++) {
                            paramMap.put("tableIndex", tableIndex);
                            String queryStr = "SELECT " +
                                    "o.ID AS orderId,o.PRODUCT_NAME AS productName,o.PRODUCT_ID AS productId,USER_ID AS userId,EXT_ATTRIBUTES AS ext,ORDER_PRODUCT_SERVICE_TYPE AS productType " +
                                    "FROM " +
                                    "VOX_USER_ORDER_:tableIndex o " +
                                    "WHERE " +
                                    "(o.ORDER_PRODUCT_SERVICE_TYPE='PicListenBook' " +
                                    "OR o.ORDER_PRODUCT_SERVICE_TYPE='GroupProduct') " +
                                    "AND (o.PAYMENT_STATUS='Paid' " +
                                    "OR o.PAYMENT_STATUS='Refund') " +
                                    "AND o.UPDATE_DATETIME>=:startTime " +
                                    "AND o.UPDATE_DATETIME<:endTime";
                            resultList.addAll(utopiaSql.withSql(queryStr).useParams(paramMap).queryAll());
                        }
                        return resultList;
                    }
                })
                .execute();
        return resultList;
    }


    private List<Map<String, Object>> generateUserOrderPaymentInfo(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        RoutingPolicyExecutorBuilder.getInstance()
                .<List<Map<String, Object>>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(new UtopiaRoutingDataSourcePolicyCallback<List<Map<String, Object>>>() {
                    @Override
                    public List<Map<String, Object>> doInRoutingPolicy() {
                        for (int tableIndex = 0; tableIndex < tableCount; tableIndex++) {
                            paramMap.put("tableIndex", tableIndex);
                            String queryStr = "SELECT " +
                                    "PAY_AMOUNT AS payAmount,PAYMENT_STATUS AS paymentStatus,ORDER_ID AS orderId,UPDATE_DATETIME AS orderCreateTime " +
                                    "FROM " +
                                    "VOX_USER_ORDER_PAYMENT_HISTORY_:tableIndex " +
                                    "WHERE " +
                                    "ORDER_ID IN (:orderIds) " +
                                    "AND (PAYMENT_STATUS='Paid' " +
                                    "OR PAYMENT_STATUS='Refund') " +
                                    "AND CREATE_DATETIME>=:startTime " +
                                    "AND CREATE_DATETIME<:endTime";
                            resultList.addAll(utopiaSql.withSql(queryStr).useParams(paramMap).queryAll());
                        }
                        return resultList;
                    }
                })
                .execute();


        return resultList;
    }

    @SuppressWarnings("unchecked")
    private List<PicOrderInfo> generatePicOrderInfoList(List<Map<String, Object>> list, Set<String> bookIds) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<PicOrderInfo> orderInfoList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            PicOrderInfo orderInfo = new PicOrderInfo();
            orderInfo.setOrderId(SafeConverter.toString(map.get("orderId")));
            orderInfo.setUserId(SafeConverter.toLong(map.get("userId")));
            orderInfo.setProductName(SafeConverter.toString(map.get("productName")));
            orderInfo.setPayAmount(new BigDecimal(SafeConverter.toString(map.get("payAmount"))));
            orderInfo.setPaymentStatus(SafeConverter.toString(map.get("paymentStatus")));
            orderInfo.setServiceStartTime(map.get("serviceStartTime") != null ? DateUtils.stringToDate(SafeConverter.toString(map.get("serviceStartTime")), DateUtils.FORMAT_SQL_DATETIME) : null);
            orderInfo.setServiceEndTime(map.get("serviceStartTime") != null ? DateUtils.stringToDate(SafeConverter.toString(map.get("serviceEndTime")), DateUtils.FORMAT_SQL_DATETIME) : null);
            orderInfo.setOrderCreateTime(DateUtils.stringToDate(SafeConverter.toString(map.get("orderCreateTime")), DateUtils.FORMAT_SQL_DATETIME));
            if (CollectionUtils.isNotEmpty(bookIds)) {
                List<String> bookIdList = (List<String>) map.get("bookId");
                orderInfo.setBookId(bookIdList.stream().filter(bookIds::contains).findFirst().orElse(""));
            }
            orderInfoList.add(orderInfo);
        }

        return orderInfoList;
    }
}
