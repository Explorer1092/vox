package com.voxlearning.utopia.service.afenti.impl.service.processor.login;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.api.entity.UserAfentiGuide;
import com.voxlearning.utopia.service.afenti.impl.persistence.UserAfentiGuidePersistence;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType.*;

/**
 * 获取首页需要的状态信息，例如是否有新的错题，是否有新的勋章之类的
 *
 * @author Ruib
 * @since 2016/7/12
 */
@Named
public class L_LoadConditions extends SpringContainerSupport implements IAfentiTask<LoginContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject private UserAfentiGuidePersistence userAfentiGuidePersistence;

    @Override
    public void execute(LoginContext context) {
        // 引导流程
        UserAfentiGuide guide = userAfentiGuidePersistence.load(context.getStudent().getId()).initialize();
        context.getResult().put("guide", JsonUtils.toJson(guide.getGuides()));

        // 各种小红点
        Map<AfentiPromptType, Boolean> prompts = asyncAfentiCacheService
                .AfentiPromptCacheManager_fetch(context.getStudent().getId(), context.getSubject())
                .take();
        context.getResult().put("promptInvite", prompts.getOrDefault(invitation, false));
        context.getResult().put("promptElf", prompts.getOrDefault(elf, false));
        context.getResult().put("promptAchievement", prompts.getOrDefault(achievement, false));
        context.getResult().put("promptRank", prompts.getOrDefault(rank, false));
    }
}
