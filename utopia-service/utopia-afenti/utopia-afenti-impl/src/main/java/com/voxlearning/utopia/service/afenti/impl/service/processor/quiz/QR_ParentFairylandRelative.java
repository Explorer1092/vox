package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/11/30
 */
@Named
public class QR_ParentFairylandRelative extends SpringContainerSupport implements IAfentiTask<QuizResultContext> {

    @Inject private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;

    @Override
    public void execute(QuizResultContext context) {
        Clazz clazz = context.getStudent().getClazz();
        if (clazz == null) return;

        String name = context.getStudent().fetchRealnameIfBlankId();
        String content = "同班" + name + "在单元测验中获得了100分";

        OrderProductServiceType appKey;
        switch (context.getSubject()) {
            case ENGLISH: {
                appKey = OrderProductServiceType.AfentiExam;
                break;
            }
            case MATH: {
                appKey = OrderProductServiceType.AfentiMath;
                break;
            }
            default:
                return;
        }

        asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                .ParentFairylandClassmatesUsageCacheManager_record(clazz.getId(), appKey, context.getStudent().getId(), content)
                .awaitUninterruptibly();
    }
}
