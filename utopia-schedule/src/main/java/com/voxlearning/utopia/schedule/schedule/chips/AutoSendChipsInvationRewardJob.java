
package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.api.AiOrderProductService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.data.ChipsUserCourseMapper;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author songtao
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语自动发送邀请奖励",
        jobDescription = "每天01:00执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 1 * * ?"
)
@ProgressTotalWork(100)
public class AutoSendChipsInvationRewardJob extends ScheduledJobWithJournalSupport {

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    @AlpsQueueProducer(queue = "utopia.chips.english.invitation.reward.queue")
    private MessageProducer invationMessageProducer;

    private UtopiaSql utopiaSql;

    @ImportService(interfaceClass =  AiOrderProductService.class)
    private AiOrderProductService aiOrderProductService;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        List<OrderProduct> orderProductList = userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream().filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                .collect(Collectors.toList());
        Date now = new Date();
        String sql = "SELECT INVITER, COUNT(*) AS NUM FROM VOX_CHIP_ENGLISH_INVITATION WHERE PRODUCT_ID = ? AND DISABLED = '0' AND SEND = '0' GROUP BY INVITER ";

        AtomicInteger index = new AtomicInteger(1);
        Map<String, ChipsEnglishProductTimetable> timetableMap = chipsEnglishContentLoader.loadChipsEnglishProductTimetableByIds(orderProductList.stream().map(OrderProduct::getId).collect(Collectors.toSet()));

        for (OrderProduct orderProduct : orderProductList) {
            ChipsEnglishProductTimetable timetable = timetableMap.get(orderProduct.getId());
            if (timetable == null ||  timetable.getBeginDate() == null) {
                continue;
            }
            Date beginDate = timetable.getBeginDate();
            if (DateUtils.dayDiff(now, beginDate) != 2L) {
                continue;
            }

//            OrderProductItem item = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId()).stream().filter(e -> !Boolean.TRUE.equals(e.getDisabled())).findFirst().orElse(null);
//            if (item == null) {
//                continue;
//            }

            utopiaSql.withSql(sql).useParamsArgs(orderProduct.getId()).queryAll((rs, rowNum) -> {
                int num = rs.getInt("NUM");
                long uId = rs.getLong("INVITER");
//                AppPayMapper appPayMapper = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.ChipsEnglish.name(), uId, true);
//                if (appPayMapper != null && appPayMapper.getAppStatus() == 2 && CollectionUtils.isNotEmpty(appPayMapper.getValidItems())
//                        && appPayMapper.getValidItems().contains(item.getId())) {
//                    Map<String, Object> message = new HashMap<>();
//                    message.put("P", orderProduct.getId());
//                    message.put("U", uId);
//                    message.put("N", num);
//                    invationMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
//                    if (index.get() % 100 == 0) {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                        }
//                    }
//                    index.getAndIncrement();
//                }

                List<ChipsUserCourseMapper> courseMapperList = aiOrderProductService.loadUserAllCourseInfo(uId);
                //判断该用户有没有买过该产品
                ChipsUserCourseMapper courseMapper = courseMapperList.stream().filter(m -> m.getProductId().equals(orderProduct.getId())).findFirst().orElse(null);
                if (courseMapper != null) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("P", orderProduct.getId());
                    message.put("U", uId);
                    message.put("N", num);
                    invationMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
                    if (index.get() % 100 == 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                    index.getAndIncrement();
                }
                return null;
            });
        }
    }

}
