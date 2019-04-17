package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
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
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.mizar.api.entity.order.PicOrderInfo;
import com.voxlearning.utopia.service.mizar.api.service.PicOrderInfoService;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import lombok.Data;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/5
 */
@Named
@ScheduledJobDefinition(
        jobName = "点读机+小U订单数据导出",
        jobDescription = "用于人教订单数据在Mizar端的查询",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 10 5 * * ? "
)
@ProgressTotalWork(100)
public class AutoPicOrderDataImportFromProductRefJob extends ScheduledJobWithJournalSupport {

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
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        Date startDate = DateUtils.stringToDate(DateUtils.getYesterdaySqlDate(), DateUtils.FORMAT_SQL_DATE);
        Date endDate = DateUtils.stringToDate(DateUtils.getTodaySqlDate(), DateUtils.FORMAT_SQL_DATE);


        String start = SafeConverter.toString(parameters.get("startDate"));
        String end = SafeConverter.toString(parameters.get("endDate"));

        if ((RuntimeMode.isStaging() || RuntimeMode.isTest() || RuntimeMode.isDevelopment()) && StringUtils.isNotBlank(start) && StringUtils.isNotBlank(end)) {
            startDate = DateUtils.stringToDate(start, DateUtils.FORMAT_SQL_DATE);
            endDate = DateUtils.stringToDate(end, DateUtils.FORMAT_SQL_DATE);
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startTime", startDate);
        paramMap.put("endTime", endDate);


        List<Map<String, Object>> mapList = generateUserOrderInfo(paramMap);
        progressMonitor.worked(5);
        Set<String> productIds = new HashSet<>();
        Map<String, PaymentStatus> paymentStatusMap = new HashMap<>();
        Map<String, Date> orderUpdateDateMap = new HashMap<>();
        for (Map<String, Object> orderMap : mapList) {
            String fixId = SafeConverter.toString(orderMap.get("orderId")) + "_" + SafeConverter.toLong(orderMap.get("userId")) % 100;
            UserOrder userOrder = userOrderLoaderClient.loadUserOrder(fixId);
            if (userOrder == null || userOrder.getPaymentStatus() != PaymentStatus.Paid) {
                continue;
            }
            paymentStatusMap.put(userOrder.getId(), userOrder.getPaymentStatus());
            orderUpdateDateMap.put(userOrder.getId(), userOrder.getUpdateDatetime());
            productIds.add(SafeConverter.toString(orderMap.get("productId")));
        }
        Map<String, ProductItemBookMapper> mapperMap = new HashMap<>();
        Set<String> bookIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(productIds)) {
            Map<String, List<OrderProductItem>> itemsByProductIds = userOrderLoaderClient.loadProductItemsByProductIds(productIds);
            itemsByProductIds.forEach((k, v) -> {
                ProductItemBookMapper productItemBookMapper = new ProductItemBookMapper();
                productItemBookMapper.setItemId(v.get(0).getId());
                productItemBookMapper.setBookId(v.get(0).getAppItemId());
                bookIds.add(v.get(0).getAppItemId());
                productItemBookMapper.setPeriod(v.get(0).getPeriod());
                mapperMap.put(k, productItemBookMapper);
            });
            Set<String> currentBookIds = newContentLoaderClient.loadBooks(bookIds).values().stream().filter(e -> StringUtils.equals(e.getPublisher(), "人民教育出版社")
                    || StringUtils.equals(e.getPublisher(), "山东科学技术出版社")
                    || StringUtils.equals(e.getPublisher(), "上海教育出版社")
                    || StringUtils.equals(e.getPublisher(), "辽宁师范大学出版社"))
                    .map(NewBookProfile::getId)
                    .collect(Collectors.toSet());
            progressMonitor.worked(30);
            ISimpleProgressMonitor iSimpleProgressMonitor = progressMonitor.subTask(65, mapList.size());
            for (Map<String, Object> map : mapList) {
                if (!paymentStatusMap.keySet().contains(SafeConverter.toString(map.get("orderId")))) {
                    continue;
                }
                PicOrderInfo picOrderInfo = new PicOrderInfo();
                String productId = SafeConverter.toString(map.get("productId"));
                String orderId = SafeConverter.toString(map.get("orderId"));
                if (SafeConverter.toInt(generatePicOrderInfo(orderId, paymentStatusMap.get(orderId).name()).get("count")) >= 1) {
                    continue;
                }
                if (!currentBookIds.contains(mapperMap.get(productId).getBookId())) {
                    continue;
                }
                picOrderInfo.setOrderId(orderId);
                picOrderInfo.setBookId(mapperMap.get(productId).getBookId());
                picOrderInfo.setServiceStartTime(map.get("order_create_time") != null ? DateUtils.stringToDate(SafeConverter.toString(map.get("order_create_time")), DateUtils.FORMAT_SQL_DATETIME) : null);
                picOrderInfo.setServiceEndTime(map.get("order_create_time") != null && mapperMap.get(productId) != null && mapperMap.get(productId).getPeriod() != null ? DateUtils.addDays(DateUtils.stringToDate(SafeConverter.toString(map.get("order_create_time")), DateUtils.FORMAT_SQL_DATETIME), mapperMap.get(productId).getPeriod()) : null);
                picOrderInfo.setOrderCreateTime(orderUpdateDateMap.get(orderId));
                picOrderInfo.setPaymentStatus(paymentStatusMap.get(orderId).name());
                picOrderInfo.setProductName(SafeConverter.toString(map.get("productName")));
                picOrderInfo.setPayAmount(new BigDecimal(SafeConverter.toString(map.get("price"))));
                picOrderInfo.setUserId(SafeConverter.toLong(map.get("relatedUserId")));
                picOrderInfoService.insertPicOrderInfo(picOrderInfo);
                iSimpleProgressMonitor.worked(1);
            }
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
                                    "o.ORDER_ID AS orderId,o.PRODUCT_NAME AS productName,o.PRODUCT_ID AS productId,USER_ID AS userId,PRODUCT_PRICE AS price,CREATE_DATETIME AS order_create_time,RELATED_USER_ID AS relatedUserId " +
                                    "FROM " +
                                    "VOX_USER_ORDER_PRODUCT_REF_:tableIndex o " +
                                    "WHERE " +
                                    "o.ORDER_PRODUCT_SERVICE_TYPE='PicListenBook' " +
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


    private Map<String, Object> generatePicOrderInfo(String orderId, String payment_status) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderId", orderId);
        paramMap.put("payment_status", payment_status);
        RoutingPolicyExecutorBuilder.getInstance()
                .<Map<String, Object>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(new UtopiaRoutingDataSourcePolicyCallback<Map<String, Object>>() {
                    @Override
                    public Map<String, Object> doInRoutingPolicy() {
                        String queryStr = "SELECT " +
                                "COUNT(1) AS count " +
                                "FROM " +
                                "VOX_PIC_ORDER_INFO " +
                                "WHERE " +
                                "ORDER_ID=:orderId " +
                                "AND PAYMENT_STATUS=:payment_status";
                        resultMap.putAll(utopiaSql.withSql(queryStr).useParams(paramMap).queryRow());
                        return resultMap;
                    }
                })
                .execute();
        return resultMap;
    }

    @Data
    private class ProductItemBookMapper implements Serializable {
        private static final long serialVersionUID = -6157580184812210636L;
        private String itemId;
        private String bookId;
        private Integer period;
    }
}
