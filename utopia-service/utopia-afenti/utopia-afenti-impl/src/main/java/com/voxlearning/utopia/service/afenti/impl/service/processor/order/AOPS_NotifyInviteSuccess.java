package com.voxlearning.utopia.service.afenti.impl.service.processor.order;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.OrderPaySuccessContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiInvitationRecord;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiInvitationRecordPersistence;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiAchievementService;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 所有邀请自己的同学增加成就记录
 *
 * @author peng.zhang.a
 * @since 16-7-26
 */
@Named
public class AOPS_NotifyInviteSuccess extends SpringContainerSupport implements IAfentiTask<OrderPaySuccessContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject AfentiAchievementService afentiAchievementService;
    @Inject AfentiInvitationRecordPersistence afentiInvitationRecordPersistence;

    @Override
    public void execute(OrderPaySuccessContext context) {
        if (context.getUserId() == 0 || StringUtils.isEmpty(context.getProductServiceType())) {
            return;
        }
        Subject subject = AfentiUtils.getSubject(OrderProductServiceType.safeParse(context.getProductServiceType()));

        List<AfentiInvitationRecord> unAcceptRecords = afentiInvitationRecordPersistence.findByInvitedUserIdAndSubject(context.getUserId(), subject)
                .stream()
                .filter(p -> !SafeConverter.toBoolean(p.getAccepted(), false))
                .collect(Collectors.toList());
        afentiInvitationRecordPersistence.updateAccepted(unAcceptRecords);

        List<Long> sendInvitationUserIds = unAcceptRecords.stream().map(AfentiInvitationRecord::getUserId).collect(Collectors.toList());
        sendInvitationUserIds.forEach(userId -> afentiAchievementService.inviteSuccessNotify(userId, subject));
        asyncAfentiCacheService.AfentiSuccessInviteRecordCacheManager_addRecords(sendInvitationUserIds, context.getUserId(), subject)
                .awaitUninterruptibly();

    }
}
