package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.order.api.constants.OrderActiveType;
import com.voxlearning.utopia.service.order.api.constants.OrderProductAmortizeType;
import com.voxlearning.utopia.service.order.api.constants.OrderProductSalesType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItemRef;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ApplyToType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by haitian.gan on 2018/2/23.
 */
@Named
@ScheduledJobDefinition(
        jobName = "绘本数据自动导入成付费商品",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT},
        ENABLED = false,
        cronExpression = "0 0 8 * * ? "
)
@ProgressTotalWork(100)
public class AutoImportPicBookOrderProductJob extends ScheduledJobWithJournalSupport {

    @Inject private UserOrderServiceClient usrOrderSrv;
    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Override
    @SuppressWarnings("unchecked")
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        // 之前手工录入的，滤掉先
        List<String> filterIds = Arrays.asList(
                "PBP_10300000456372",
                "PBP_10300000449197",
                "PBP_10300000487024",
                "PBP_10300000428888",
                "PBP_10300000512044",
                "PBP_10300000308589");

        // 获得所有的绘本数据
        Map<String, PictureBookPlus> picBookMap = pictureBookPlusServiceClient.toMap();
        Collection<PictureBookPlus> allPicBooks = picBookMap.values();

        List<PictureBookPlus> picBooks = allPicBooks.stream()
                .filter(pb -> pb.getApplyTo() != null)
                .filter(pb -> pb.getApplyTo().contains(ApplyToType.SELF.name()))
                .filter(pb -> StringUtils.isNotBlank(pb.getIosFileUrl()))
                .filter(pb -> CollectionUtils.isNotEmpty(pb.getNewClazzLevels()))
                .filter(pb -> pb.getNewClazzLevels().contains(PictureBookNewClazzLevel.L5B))
                .filter(pb -> "ONLINE".equals(pb.getStatus()))
                .collect(Collectors.toList());

        OrderProductServiceType productType = OrderProductServiceType.ELevelReading;
        OrderProductSalesType salesType = OrderProductSalesType.TIME_BASED;
        MapMessage resultMsg;

        for (PictureBookPlus picBook : picBooks) {
            if (filterIds.contains(picBook.getId()))
                continue;

            OrderProduct newProduct = new OrderProduct();
            newProduct.setName(picBook.getCname());
            newProduct.setDesc(picBook.getCname());
            newProduct.setProductType(productType == null ? null : productType.name());
            newProduct.setSalesType(salesType);
            newProduct.setPrice(new BigDecimal(1d));
            newProduct.setOriginalPrice(new BigDecimal(1d));
            newProduct.setStatus("ONLINE");

            resultMsg = usrOrderSrv.saveOrderProduct(newProduct);
            String pId = MapUtils.getString(resultMsg, "id");

            OrderProductItem item = new OrderProductItem();
            item.setName(picBook.getCname());
            item.setDesc(picBook.getCname());
            item.setProductType(productType == null ? null : productType.name());
            item.setSalesType(salesType);
            item.setActiveType(OrderActiveType.ITEM);
            item.setAmortizeType(OrderProductAmortizeType.BUY_MONTH_BASE);
            item.setPeriod(3285);
            item.setOriginalPrice(new BigDecimal(1));
            item.setRepurchaseAllowed(false);
            item.setAppItemId(picBook.getId());

            resultMsg = usrOrderSrv.saveOrderProductItem(item);
            String itemId = MapUtils.getString(resultMsg, "id");

            OrderProductItemRef ref = new OrderProductItemRef();
            ref.setProductId(pId);
            ref.setProductItemId(itemId);
            usrOrderSrv.saveOrderProductItemRef(ref);
        }

    }
}
