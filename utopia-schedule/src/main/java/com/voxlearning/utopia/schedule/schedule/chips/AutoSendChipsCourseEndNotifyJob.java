
package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author songtao
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语课程到期提醒",
        jobDescription = "每天18点执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 18 * * ?"
)
@ProgressTotalWork(100)
public class AutoSendChipsCourseEndNotifyJob extends ScheduledJobWithJournalSupport {

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    @AlpsQueueProducer(queue = "utopia.chips.course.end.notify.queue")
    private MessageProducer courseBeginNotifyMessageProducer;

    private UtopiaSql utopiaSqlOrder;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("order");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        Date now = new Date();
        List<OrderProduct> orderProductList = userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                .filter(e -> {
                    String ettr = e.getAttributes();
                    if (StringUtils.isBlank(ettr)) {
                        return false;
                    }
                    Map<String, Object> map = JsonUtils.fromJson(ettr);
                    return !MapUtils.isEmpty(map);
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderProductList)) {
            return;
        }
        Set<String> productIds = orderProductList.stream().map(OrderProduct::getId).collect(Collectors.toSet());
        Map<String, ChipsEnglishProductTimetable> productTimetableMap = chipsEnglishContentLoader.loadChipsEnglishProductTimetableByIds(productIds);
        if (MapUtils.isEmpty(productTimetableMap)) {
            return;
        }

        orderProductList = orderProductList.stream()
                .filter(e -> {
                    ChipsEnglishProductTimetable timetable = productTimetableMap.get(e.getId());
                    if (timetable == null || timetable.getEndDate() == null || CollectionUtils.isEmpty(timetable.getCourses())) {
                        return false;
                    }
                    List<ChipsEnglishProductTimetable.Course> courseList = timetable.getCourses().stream().filter(course -> course.getBeginDate().before(now)).collect(Collectors.toList());

                    int remainCourse=timetable.getCourses().size()-courseList.size();
                    return remainCourse==2;
                })
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(orderProductList)) {
            return;
        }
        String sql = "SELECT DISTINCT `USER_ID` FROM `VOX_USER_ACTIVATED_PRODUCT` WHERE PRODUCT_SERVICE_TYPE = 'ChipsEnglish' AND `DISABLED` = '0'";
        Set<Long> userIds = new HashSet<>();
        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> utopiaSqlOrder.withSql(sql).queryAll(((rs, rowNum) -> {
                    userIds.add(rs.getLong("USER_ID"));
                    return null;
                })))
                .execute();
        int index = 1;
        for (OrderProduct orderProduct : orderProductList) {
            for (Long id : userIds) {
                Map<String, Object> message = new HashMap<>();
                message.put("P", orderProduct.getId());
                message.put("U", id);
                courseBeginNotifyMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
                if (index % 100 == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
                index++;
            }
        }
    }
}
