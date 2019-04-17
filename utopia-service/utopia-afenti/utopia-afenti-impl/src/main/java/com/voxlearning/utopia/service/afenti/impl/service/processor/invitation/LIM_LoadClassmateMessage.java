package com.voxlearning.utopia.service.afenti.impl.service.processor.invitation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.LoadInvitationMsgContext;
import com.voxlearning.utopia.service.afenti.api.context.PopupTextContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 加载同班同学开通或者续费信息
 * 必须在LoadBaseMessage与SplitUserList之后
 *
 * @author peng.zhang.a
 * @since 16-7-19
 */
@Named
public class LIM_LoadClassmateMessage extends SpringContainerSupport implements IAfentiTask<LoadInvitationMsgContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Override
    public void execute(LoadInvitationMsgContext context) {
        Set<Long> paidOrderClassmates = asyncAfentiCacheService
                .AfentiPaidSuccessClassmatesCacheManager_loadPaidClassmateUserIds(context.getUser().getId(), context.getSubject())
                .take();

        String message = "";
        if (CollectionUtils.isNotEmpty(paidOrderClassmates)) {
            List<String> names = paidOrderClassmates
                    .stream()
                    .filter(p -> context.getClassmateMap().containsKey(p)
                            && StringUtils.isNotEmpty(context.getClassmateMap().get(p).fetchRealname()))
                    .map(p -> context.getClassmateMap().get(p).fetchRealname())
                    .limit(2)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(names)) {
                int classmateNum = CollectionUtils.isNotEmpty(paidOrderClassmates) ? paidOrderClassmates.size() : 0;
                String classmateName = StringUtils.join(names.toArray(), "，");
                if (classmateName.length() > 8) {
                    classmateName = classmateName.substring(0, 4) + "...等" + classmateNum + "位";
                } else {
                    classmateName = classmateNum > 2 ? classmateName + "等" + classmateNum + "位" : classmateName;
                }
                message = StringUtils.formatMessage(PopupTextContext.CLASSMATE_PAID_MSG.desc, classmateName, context.getSubject().getValue());
            }
        }

        context.getResult().put("classmatePaidMsg", message);
    }
}
