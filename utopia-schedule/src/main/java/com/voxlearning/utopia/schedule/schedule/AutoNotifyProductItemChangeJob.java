package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.util.CollectionUtils;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-04-24 15:04
 */

@Named
@ScheduledJobDefinition(
        jobName = "自动通知商品信息变更任务",
        jobDescription = "自动通知变更的商品，每天早上8点运行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 8 * * ?"
)
@ProgressTotalWork(100)
public class AutoNotifyProductItemChangeJob  extends ScheduledJobWithJournalSupport {

    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private EmailServiceClient emailServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        logger.info("通知商品信息变更任务开始执行");
        //查询前一天有变动的orderproductItem信息
        Date changeDay = DateUtils.calculateDateDay(new Date(),-1);
        Date startDate = DateUtils.getDayStart(changeDay);
        Date endDate = DateUtils.getDayEnd(changeDay);
        List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadAllOrderProductItems();
        orderProductItemList = orderProductItemList.stream().filter(o -> o.getUpdateDatetime().compareTo(endDate) <= 0
                &&  o.getUpdateDatetime().compareTo(startDate) >= 0).collect(Collectors.toList());

        //组装数据
        StringBuffer changeContent = new StringBuffer();
        changeContent.append("产品ID，")
                .append("产品名称，")
                .append("产品类型，")
                .append("销售方式，")
                .append("摊销类型 <br/>");
        for(OrderProductItem orderProductItem : orderProductItemList){
            changeContent.append(orderProductItem.getId()+",")
                    .append(orderProductItem.getName()+",")
                    .append(orderProductItem.getProductType()+",")
                    .append(orderProductItem.getSalesType().name()+",")
                    .append(orderProductItem.getAmortizeType().name()+"<br/>");
        }

        Map<String, Object> content = new HashMap<>();
        content.put("changeContent",changeContent);
        if(CollectionUtils.isEmpty(orderProductItemList)){
            content.put("changeContent","无变更");
        }else{
            content.put("changeContent",changeContent.toString());
        }
        if (RuntimeMode.isProduction()) {
            emailServiceClient.createTemplateEmail(EmailTemplate.productitemchangenotify)
                    .to("nan.jiang@17zuoye.com;xinfei.li@17zuoye.com;xiaodan.liu@17zuoye.com;miao.yu@17zuoye.com")
                    .cc("yong.liu@17zuoye.com;zhilong.hu@17zuoye.com")
                    .subject("商品信息变更通知")
                    .content(content)
                    .send();
        }
        logger.info("通知商品信息变更任务执行完毕");

    }
}
