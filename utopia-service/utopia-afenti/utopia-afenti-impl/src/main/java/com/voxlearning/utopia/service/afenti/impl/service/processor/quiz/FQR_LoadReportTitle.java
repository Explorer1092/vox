package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQuizType;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizReportContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class FQR_LoadReportTitle extends SpringContainerSupport implements IAfentiTask<FetchQuizReportContext> {
    @Inject private AfentiLoaderImpl afentiLoader;
    @Inject private NewContentLoaderClient newContentLoaderClient;

    @Override
    public void execute(FetchQuizReportContext context) {

        if (context.getType() == AfentiQuizType.UNIT_QUIZ) {
            // 获取单元排序
            List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader
                    .loadAfentiLearningPlanUnitRankManagerByNewBookId(context.getBookId())
                    .stream()
                    .filter(rm -> StringUtils.equals(rm.getNewUnitId(), context.getUnitId()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(ranks)) {
                logger.error("FQR_LoadReportTitle Cannot find rank manager for user {}, subject {}, unit {}",
                        context.getStudent().getId(), context.getSubject(), context.getContentId());
                context.errorResponse();
                return;
            }
            context.getResult().put("title", "第" + ranks.get(0).fetchUnitRank() + "单元 单元测试");
        } else {
            NewBookProfile book = newContentLoaderClient.loadBook(context.getBookId());
            if (book == null) {
                logger.error("FQR_LoadReportTitle Cannot find book for user {}, subject {}, book {}",
                        context.getStudent().getId(), context.getSubject(), context.getContentId());
                context.errorResponse();
                return;
            }
            context.getResult().put("title", book.getName() + context.getSubject().getValue() + "期中测试卷");
        }
    }
}
