package com.voxlearning.utopia.service.afenti.impl.service.processor.login;

import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiPopupMessageService;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Ruib
 * @since 2016/12/6
 */
@Named
public class L_LoadPopup extends SpringContainerSupport implements IAfentiTask<LoginContext> {
    @Inject private AfentiPopupMessageService afentiPopupMessageService;

    @Override
    public void execute(LoginContext context) {

        // 排行榜弹窗，邀请弹窗
        MapMessage mapMessage = afentiPopupMessageService.fetchPopupMessage(context.getStudent(), context.getSubject());
        if (mapMessage.isSuccess()) {
            context.getResult().put("rankPopupInfo", mapMessage.getOrDefault("rankPopupInfo", null));
            context.getResult().put("invitationPopupInfo", mapMessage.getOrDefault("invitationPopupInfo", null));
        }
        // 家长奖励弹窗
        mapMessage = afentiPopupMessageService.fetchParentRewardPopupMessage(context);
        if (mapMessage.isSuccess()) {
            context.getResult().put("parentRewardPopupInfo", mapMessage.getOrDefault("popupInfo", null));
        }
        // 教材年级不等于本学期上册  弹窗提醒
        Integer bookClazzLevel = context.getBook().book.getClazzLevel();
        Integer studentClazzLevel = context.getStudent().getClazzLevel().getLevel();
        if (!Objects.equals(bookClazzLevel, studentClazzLevel) ||
                context.getBook().book.getTermType() == null ||
                context.getBook().book.getTermType() != Term.上学期.getKey()) {
            context.getResult().put("changeBookFlag", true);
        }


        // 无用的
        context.getResult().put("lotteryPopupInfo", new ArrayList<>());
        context.getResult().put("monsterNianSwitch", false);
    }
}