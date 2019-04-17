package com.voxlearning.utopia.service.afenti.impl.service.internal;

import com.voxlearning.utopia.service.afenti.api.constant.PurchaseType;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanUserRankStatPersistence;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 16-11-30
 * <p>
 * 阿分题运营消息相关的类
 */
@Named
public class AfentiOperationalInfoService extends UtopiaAfentiSpringBean {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject AfentiLearningPlanUserRankStatPersistence afentiLearningPlanUserRankStatPersistence;

    public boolean addUserPurchaseInfo(StudentDetail studentDetail, PurchaseType purchaseType, Date createDate) {
        asyncAfentiCacheService.AfentiPurchaseInfosCacheManager_addRecord(studentDetail, purchaseType, createDate)
                .awaitUninterruptibly();
        return true;
    }

    public boolean addUserRewardInfo(StudentDetail studentDetail, Integer integral) {
        if (studentDetail == null || integral == 0) {
            return false;
        }
        asyncAfentiCacheService.AfentiRewardInfosCacheManager_addRecord(studentDetail, integral)
                .awaitUninterruptibly();
        return true;
    }

    /**
     * 只有开通阿分题任意一款产品才会积累成就
     */
    public boolean addUserRewardInfo(StudentDetail studentDetail) {
        try {
            //开通用户则保存更新奖励
            int integralSum = afentiLearningPlanUserRankStatPersistence.queryTotalIntegarl(studentDetail.getId());
            if (integralSum > 0) {
                asyncAfentiCacheService.AfentiRewardInfosCacheManager_addRecord(studentDetail, integralSum)
                        .awaitUninterruptibly();
            }
        } catch (Exception e) {
            logger.error("addUserRewardInfo error", e);
        }

        return true;
    }

    public List<Map<String, Object>> loadUserPurchaseInfos(StudentDetail studentDetail) {
        return asyncAfentiCacheService.AfentiPurchaseInfosCacheManager_getRecords(studentDetail).take();
    }

    public List<Map<String, Object>> loadUserRewardInfos(StudentDetail studentDetail) {
        return asyncAfentiCacheService.AfentiRewardInfosCacheManager_getRecords(studentDetail).take();
    }


}
