package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.entity.ChipsActiveServiceRecord;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsActiveServiceRecordDao;
import com.voxlearning.utopia.service.ai.impl.service.ChipsActiveServiceImpl;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;

/**
 * 主动服务相关记录处理
 */
@Named
public class CRQ_UpsertActiveServiceRecord extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @Inject
    private ChipsActiveServiceRecordDao chipsActiveServiceRecordDao;
    @Inject
    private ChipsActiveServiceImpl chipsActiveService;

    @Override
    public void execute(ChipsQuestionResultContext context) {
        // 不是单元最后一课，不处理
        if (!context.getChipsQuestionResultRequest().getUnitLast()) {
            return;
        }

        Long userId = context.getUserId();
        String unitId = context.getUnit().getId();
        try {
            ChipsEnglishClass clazz = chipsUserService.loadClazzByUserAndBook(userId, context.getChipsQuestionResultRequest().getBookId());
            if (clazz != null) {
                String productId = clazz.getProductId();
                Long classId = clazz.getId();
                Map<Long, Boolean> registerWxUser = chipsActiveService.isRegisterWxUser(Collections.singleton(userId));
                Map<Long, Boolean> testOrRefundUser = chipsActiveService.isTestOrRefundUser(Collections.singleton(userId), productId);
                if (RuntimeMode.gt(Mode.TEST)) {
                    if (!registerWxUser.get(userId) || testOrRefundUser.get(userId)) {
                        logger.warn("测试用户或者退款用户userId: " + userId + ";productId: " + productId);
                        return;
                    }
                }
                // 插入主动服务记录
                ChipsActiveServiceRecord chipsActiveServiceRecord = ChipsActiveServiceRecord.valueOf(ChipsActiveServiceType.SERVICE, classId, userId, unitId, ChipsActiveServiceRecord.RemarkStatus.Zero);
                chipsActiveServiceRecordDao.insertIfAbsent(chipsActiveServiceRecord.getId(), chipsActiveServiceRecord);

                // 更新催课提醒记录
                chipsActiveServiceRecordDao.updateToSerivced(ChipsActiveServiceType.REMIND, classId, userId, unitId);
            }
        } catch (Exception e) {
            logger.error("an error occurred when insert aiActiveServiceRecord error, type=" + ChipsActiveServiceType.SERVICE.getDesc() + ", userId=" + userId + ", unitId=" + unitId, e);
        }
    }

}
