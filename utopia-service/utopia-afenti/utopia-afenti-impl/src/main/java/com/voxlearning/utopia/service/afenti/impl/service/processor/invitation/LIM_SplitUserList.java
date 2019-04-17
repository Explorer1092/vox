package com.voxlearning.utopia.service.afenti.impl.service.processor.invitation;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.constant.UseAppStatus;
import com.voxlearning.utopia.service.afenti.api.context.LoadInvitationMsgContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;

import javax.inject.Named;
import java.util.*;

/**
 * @author peng.zhang.a
 * @since 16-7-19
 * 拆分同班同学同学为两个类别，正在使用中与未使用
 */
@Named
public class LIM_SplitUserList extends SpringContainerSupport implements IAfentiTask<LoadInvitationMsgContext> {

    @Override
    public void execute(LoadInvitationMsgContext context) {
        Long studentId = context.getUser().getId();

        if (MapUtils.isNotEmpty(context.getClassmateMap())) {
            context.getClassmateMap().forEach((userId, user) -> {
                UserActivatedProduct history = context.getUserOrderMap().getOrDefault(userId, new ArrayList<>())
                        .stream()
                        .filter(t -> Objects.equals(t.getProductServiceType(), context.getOrderProductServiceType()))
                        .findFirst()
                        .orElse(null);

                Map<String, Object> userMsg = new HashMap<>();
                userMsg.put("userName", user.fetchRealnameIfBlankId());
                userMsg.put("imageUrl", user.fetchImageUrl());
                userMsg.put("userId", user.getId());
                if (history == null) {
                    context.getNotPurchaseMap().put(user.getId(), userMsg);
                } else if (history.getServiceEndTime().after(new Date())) {
                    context.getUsingUserMap().put(user.getId(), userMsg);
                } else {
                    context.getExpiredUserMap().put(user.getId(), userMsg);
                }
            });
        }

        // 用户自己状态
        if (context.getExpiredUserMap().containsKey(studentId)) {
            context.getResult().put("useAppStatus", UseAppStatus.Expired);
        } else if (context.getUsingUserMap().containsKey(studentId)) {
            context.getResult().put("useAppStatus", UseAppStatus.Using);
        } else {
            context.getResult().put("useAppStatus", UseAppStatus.NotBuy);
        }
    }
}
