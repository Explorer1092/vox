package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.api.fromUtopia.entity.PsrExamEnSimilarContentEx;
import com.voxlearning.athena.api.fromUtopia.entity.PsrExamEnSimilarItemEx;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.impl.athena.AfentiPushQuestionServiceClient;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
public class IER_GenerateSimilarQuestion extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @Inject private AfentiPushQuestionServiceClient afentiPushQuestionServiceClient;

    @Override
    public void execute(ElfResultContext context) {

        PsrExamEnSimilarContentEx psr = afentiPushQuestionServiceClient.getUtopiaPsrLoader().getPsrExamEnSimilarByQid("AFENTI",
                context.getStudent().getId(), Collections.singletonList(context.getQuestionId()), 1);

        String similarId = UtopiaAfentiConstants.NO_SIMILAR_QUESTION; // 默认没找到

        if (StringUtils.isNotBlank(psr.getErrorContent()) && psr.isSuccess() && MapUtils.isNotEmpty(psr.getSimilarMap())) {
            List<PsrExamEnSimilarItemEx> list = psr.getSimilarMap().getOrDefault(context.getQuestionId(), new ArrayList<>());
            if (CollectionUtils.isNotEmpty(list)) {
                PsrExamEnSimilarItemEx item = list.get(0);
                similarId = item.getEid();
                logger.debug(String.format("User %d eid %s get similar eid: %s similarity: %f",
                        context.getStudent().getId(), context.getQuestionId(), item.getEid(), item.getSimilarity()));
            }
        }

        context.setSimilarId(similarId);
    }
}
