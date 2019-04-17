package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
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
public class CR_ParentFairylandRelative extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Inject private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;

    @Override
    public void execute(CastleResultContext context) {
        Clazz clazz = context.getStudent().getClazz();
        if (clazz == null) return;

        long rn = context.getHistories().stream().filter(h -> h.getRightNum() > 0).count();
        String name = context.getStudent().fetchRealnameIfBlankId();

        String content;
        OrderProductServiceType appKey;
        switch (context.getSubject()) {
            case ENGLISH: {
                content = "同班" + name + "学习了" + rn + "个英语知识点";
                appKey = OrderProductServiceType.AfentiExam;
                break;
            }
            case MATH: {
                content = "同班" + name + "学习了" + rn + "个数学知识点";
                appKey = OrderProductServiceType.AfentiMath;
                break;
            }
            case CHINESE: {
                content = "同班" + name + "学习了" + rn + "个语文知识点";
                appKey = OrderProductServiceType.AfentiChinese;
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
