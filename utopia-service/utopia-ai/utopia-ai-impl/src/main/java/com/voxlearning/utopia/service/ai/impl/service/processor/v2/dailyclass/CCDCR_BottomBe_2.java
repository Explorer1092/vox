package com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.ai.data.RecommendProductConfig;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class CCDCR_BottomBe_2 extends AbstractAiSupport implements IAITask<ChipsContentDailyClassContext> {


    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Override
    public void execute(ChipsContentDailyClassContext context) {

        List<RecommendProductConfig> recommendProductConfigList = chipsContentService.loadRecommendProductConfig();
        if (CollectionUtils.isEmpty(recommendProductConfigList)) {
            return;
        }

        RecommendProductConfig recommendProductConfig = recommendProductConfigList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getOriginalBook()) && e.getOriginalBook().equals(context.getBookRef().getBookId()))
                .findFirst().orElse(null);
        if (recommendProductConfig == null) {
            return;
        }

        Date now = new Date();
        if (recommendProductConfig.getOpenDate() == null || now.before(recommendProductConfig.getOpenDate())) {
            return;
        }

        if (context.getUserBoughtBooks().contains(recommendProductConfig.getRecommendBook())) {
            return;
        }

        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(recommendProductConfig.getRecommendProduct());
        if (orderProduct == null) {
            return;
        }
        ChipsEnglishProductTimetable chipsEnglishProductTimetable = chipsEnglishProductTimetableDao.load(recommendProductConfig.getRecommendProduct());
        if (chipsEnglishProductTimetable == null) {
            return;
        }

        StringBuffer linkStr = new StringBuffer();
        linkStr.append(ProductConfig.getMainSiteBaseUrl())
                .append("/view/mobile/parent/parent_ai/formal_robin")
                .append("?beginDate=")
                .append(DateUtils.dateToString(chipsEnglishProductTimetable.getBeginDate(), "yyyy年MM月dd日"))
                .append("&productId=")
                .append(recommendProductConfig.getRecommendProduct())
                .append("&price=")
                .append(orderProduct.getPrice().setScale(0, BigDecimal.ROUND_HALF_UP))
                .append("&grade=")
                .append(recommendProductConfig.getGrade())
                .append("&productName=")
                .append(orderProduct.getName())
                .append("&originalPrice=")
                .append(orderProduct.getOriginalPrice().setScale(0, BigDecimal.ROUND_HALF_UP));
        Map<String, Object> buttomBe = new HashMap<>();
        buttomBe.put("linkUrl", linkStr.toString());
        buttomBe.put("image", recommendProductConfig.getBeImage());
        context.getExtMap().put("bottomBe", buttomBe);
    }
}
