package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xinxin
 * @since 22/8/2016
 */
@Named
public class CR_ActionSubmitAfentiAnswer extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Inject private ActionServiceClient actionServiceClient;

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Override
    public void execute(CastleResultContext context) {
        // TODO: 2018/2/1 阿分题转移了，这个待处理
        Long studentId = context.getStudent().getId();
        Subject subject = context.getSubject();

        if (asyncAfentiCacheService.SubmitResultActionCacheManager_sended(studentId, subject).take()) return;

        switch (context.getSubject()) {
            case ENGLISH: {
                actionServiceClient.submitAfentiEnglishAnswer(context.getStudent().getId());
                break;
            }
            case MATH: {
                actionServiceClient.submitAfentiMathAnswer(context.getStudent().getId());
                break;
            }
            case CHINESE: {
                actionServiceClient.submitAfentiChineseAnswer(context.getStudent().getId());
                break;
            }
            default:
        }
    }
}
