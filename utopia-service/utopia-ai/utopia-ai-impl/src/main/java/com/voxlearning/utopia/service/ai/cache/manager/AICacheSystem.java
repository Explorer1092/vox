package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.Getter;

import javax.inject.Named;

@Named
public class AICacheSystem extends SpringContainerSupport {
    @Getter
    private UserDialogueTalkSceneResultCacheManager userDialogueTalkSceneResultCacheManager;

    @Getter
    private UserTalkFeedSessionCacheManager userTalkFeedSessionCacheManager;

    @Getter
    private UserTaskTalkSceneResultCacheManager userTaskTalkSceneResultCacheManager;

    @Getter
    private UserTaskRoleResultCacheManager userTaskRoleResultCacheManager;

    @Deprecated
    @Getter
    private UserShareRecordCacheManager userShareRecordCacheManager;


    @Getter
    private UserTaskRoleResultV2CacheManager userTaskRoleResultV2CacheManager;

    @Getter
    private AiLoaderRedDotCacheManager aiLoaderRedDotCacheManager;

    @Getter
    private UserDialogueVideoCacheManager userDialogueVideoCacheManager;

    @Getter
    private WechatUserDialogueTalkSceneResultCacheManager wechatUserDialogueTalkSceneResultCacheManager;

    @Getter
    private ChipsRepeatRequestCacheManager chipsRepeatRequestCacheManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        UtopiaCache persistence = CacheSystem.CBS.getCache("persistence");
        userDialogueTalkSceneResultCacheManager = new UserDialogueTalkSceneResultCacheManager(persistence);
        userTaskTalkSceneResultCacheManager = new UserTaskTalkSceneResultCacheManager(persistence);
        userTaskRoleResultCacheManager = new UserTaskRoleResultCacheManager(persistence);
        userShareRecordCacheManager = new UserShareRecordCacheManager(persistence);
        aiLoaderRedDotCacheManager = new AiLoaderRedDotCacheManager(persistence);
        userTaskRoleResultV2CacheManager = new UserTaskRoleResultV2CacheManager(persistence);
        userDialogueVideoCacheManager = new UserDialogueVideoCacheManager(persistence);
        userTalkFeedSessionCacheManager = new UserTalkFeedSessionCacheManager(persistence);

        UtopiaCache flushAble = CacheSystem.CBS.getCache("flushable");
        chipsRepeatRequestCacheManager = new ChipsRepeatRequestCacheManager(flushAble);

        UtopiaCache unflushAble = CacheSystem.CBS.getCache("unflushable");
        wechatUserDialogueTalkSceneResultCacheManager = new WechatUserDialogueTalkSceneResultCacheManager(unflushAble);

    }
}
