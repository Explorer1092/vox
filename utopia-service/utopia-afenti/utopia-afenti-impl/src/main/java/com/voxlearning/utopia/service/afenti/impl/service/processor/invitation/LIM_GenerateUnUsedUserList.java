package com.voxlearning.utopia.service.afenti.impl.service.processor.invitation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.LoadInvitationMsgContext;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiInvitationRecordPersistence;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author peng.zhang.a
 * @since 16-7-20
 */
@Named
public class LIM_GenerateUnUsedUserList extends SpringContainerSupport implements IAfentiTask<LoadInvitationMsgContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject private AfentiInvitationRecordPersistence afentiInvitationRecordPersistence;

    @Override
    public void execute(LoadInvitationMsgContext context) {
        Long studentId = context.getUser().getId();
        List<Map<String, Object>> unUsedUserList = new ArrayList<>();

        // 可以重复邀请，但对于一个用户每天只能邀请一次
        Set<Long> todayInviteUserIdSet = asyncAfentiCacheService
                .AfentiInviteUserRecordCacheManager_loadRecord(studentId, context.getSubject())
                .take();

        // 将自己加入到List第一位
        if (context.getExpiredUserMap().containsKey(studentId)) {
            unUsedUserList.add(context.getExpiredUserMap().get(studentId));
            context.getExpiredUserMap().remove(studentId);
        } else if (context.getNotPurchaseMap().containsKey(studentId)) {
            unUsedUserList.add(context.getNotPurchaseMap().get(studentId));
            context.getNotPurchaseMap().remove(studentId);
        }

        // 增加是否邀请状态并需要将所有的userId转换为字符串
        unUsedUserList.addAll(context.getExpiredUserMap().values());
        unUsedUserList.addAll(context.getNotPurchaseMap().values());
        unUsedUserList.forEach(value -> {
            Long uid = (Long) value.getOrDefault("userId", 0);
            if (todayInviteUserIdSet.contains(uid)) {
                value.put("inviteStatus", "INVITED");
            } else {
                value.put("inviteStatus", "UNINVITED");
            }
            value.put("userId", String.valueOf(uid));
        });

        context.getResult().put("unUsedUserList", unUsedUserList);
    }
}
