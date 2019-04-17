package com.voxlearning.utopia.service.afenti.impl.service.processor.order;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.constant.PurchaseType;
import com.voxlearning.utopia.service.afenti.api.context.OrderPaySuccessContext;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiInvitationRecordPersistence;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiAchievementService;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiOperationalInfoService;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 增加开通信息
 *
 * @author peng.zhang.a
 * @since 16-7-26
 */
@Named
public class AOPS_NotifyPurchaseInfo extends SpringContainerSupport implements IAfentiTask<OrderPaySuccessContext> {
    @Inject AfentiAchievementService afentiAchievementService;
    @Inject AfentiInvitationRecordPersistence afentiInvitationRecordPersistence;
    @Inject AfentiOperationalInfoService operationalInfoService;
    @Inject StudentLoaderClient studentLoaderClient;
    @Inject UserOrderLoaderClient userOrderLoaderClient;

    @Override
    public void execute(OrderPaySuccessContext context) {
        List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(context.getProductServiceType(), context.getUserId());

        Subject subject = null;
        if (OrderProductServiceType.safeParse(context.getProductServiceType()) == OrderProductServiceType.AfentiExam) {
            subject = Subject.ENGLISH;
        } else if (OrderProductServiceType.safeParse(context.getProductServiceType()) == OrderProductServiceType.AfentiMath) {
            subject = Subject.MATH;
        } else if (OrderProductServiceType.safeParse(context.getProductServiceType()) == OrderProductServiceType.AfentiChinese) {
            subject = Subject.CHINESE;
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getUserId());
        if (studentDetail != null && subject != null) {
            if (userOrders.size() > 1) {
                operationalInfoService.addUserPurchaseInfo(studentDetail, PurchaseType.PAY_RENEWAL, userOrders.get(0).getUpdateDatetime());
            } else if (userOrders.size() == 1) {
                operationalInfoService.addUserPurchaseInfo(studentDetail, PurchaseType.NEW_PAID, userOrders.get(0).getUpdateDatetime());

            }
        }
    }
}
