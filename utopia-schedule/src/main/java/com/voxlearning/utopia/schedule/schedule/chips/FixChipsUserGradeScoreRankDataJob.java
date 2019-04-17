package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
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
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Named
@ScheduledJobDefinition(
        jobName = "薯条英语修复用户等级报告排名数据",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 */1 * * ? ",
        ENABLED = false
)
@ProgressTotalWork(100)
public class FixChipsUserGradeScoreRankDataJob extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @AlpsQueueProducer(queue = "utopia.chips.user.grade.score.rank.fix.data.queue")
    private MessageProducer chipsGradeScoreRankFixDataMessageProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        List<String> productIdList = (List<String>) (parameters.get("productId"));
        if (CollectionUtils.isEmpty(productIdList)) {
            productIdList = userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream()
                    .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                    .filter(e -> OrderProductServiceType.ChipsEnglish.name().equals(e.getProductType()))
                    .filter(e -> StringUtils.isNotBlank(e.getAttributes()))
                    .filter(e -> {
                        Map<String, Object> map = JsonUtils.fromJson(e.getAttributes());
                        if (MapUtils.isEmpty(map)) {
                            return false;
                        }
                        boolean shortProduct = SafeConverter.toBoolean(map.get("short"));
                        if (!shortProduct) {
                            return false;
                        }
                        int rank = SafeConverter.toInt(map.get("rank"));
                        if (rank == 8) {
                            return true;
                        }
                        return false;
                    })
                    .map(OrderProduct::getId)
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(productIdList)) {
            return;
        }

        for(String productId : productIdList) {
            List<ChipsEnglishClass> clazzList = chipsEnglishClazzService.selectChipsEnglishClassByProductId(productId);
            if (CollectionUtils.isEmpty(clazzList)) {
                continue;
            }

            clazzList.forEach(clazz ->
                    chipsGradeScoreRankFixDataMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(new ClazzMap(clazz.getId()))))
            );
        }
    }

    private static class ClazzMap extends HashMap<String, Object> {
        ClazzMap(Long clazzId) {
            this.put("C", clazzId);
        }
    }
}
