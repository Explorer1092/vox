package com.voxlearning.utopia.service.ai.impl.service.processor.dailyclass;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.ai.context.AIUserDailyClassContext;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonBookRef;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.exception.ProductNotExitException;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;

import javax.inject.Named;
import java.util.Date;
import java.util.Optional;

@Named
public class ADC_LoadUserProduct extends AbstractAiSupport implements IAITask<AIUserDailyClassContext> {

    @Override
    public void execute(AIUserDailyClassContext context) {
        AIUserLessonBookRef aiUserLessonBookRef;
        try {
            aiUserLessonBookRef = chipsUserService.fetchOrInitBookRef(context.getUser().getId());
        } catch (ProductNotExitException e) {
            initNotBuyContext(context, getUrl(RuntimeMode.current(), "view/mobile/parent/parent_ai/be"));
            return;
        }
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(aiUserLessonBookRef.getProductId());
        if (product == null) {
            initNotBuyContext(context, getUrl(RuntimeMode.current(), "view/mobile/parent/parent_ai/be"));
            return;
        }
        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(aiUserLessonBookRef.getProductId());
        Date beginDate = Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getBeginDate).orElse(new Date());
        Date endDate = Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getEndDate).orElse(new Date());
        context.setBeginDate(beginDate);
        context.setEndDate(endDate);
        context.setBookId(aiUserLessonBookRef.getBookId());
        context.setClassName(aiUserLessonBookRef.getBookName());
    }

    private void initNotBuyContext(AIUserDailyClassContext context, String url) {
        context.getExtMap().put("data", "401");
        context.getExtMap().put("message", "无购买");
        context.getExtMap().put("redirect", url);
        context.terminateTask();
    }

    private String getUrl(Mode mode, String path) {
        switch (mode) {
            case STAGING:
                return "https://www.staging.17zuoye.net/" + path;
            case PRODUCTION:
                return "https://www.17zuoye.com/" + path;
            default:
                return "https://www.test.17zuoye.net/" + path;
        }
    }
}
