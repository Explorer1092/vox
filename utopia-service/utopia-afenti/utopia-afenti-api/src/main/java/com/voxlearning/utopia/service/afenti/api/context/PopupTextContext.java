package com.voxlearning.utopia.service.afenti.api.context;

/**
 * @author peng.zhang.a
 * @since 16-8-19
 */
public enum PopupTextContext {
    INVITATION_SUCCESS_POPUP_MSG("在你的邀请下{}同学已经开通阿分题{},快去勋章馆看看你的邀请达人勋章进度吧"),
    INVITE_SUCCESS_MSG("及时告知你邀请的同学去开通哦，邀请开通的同学越多获得的邀请达人勋章也会越多"),
    CLASSMATE_PAID_MSG("{}同学开通（续费）了阿分题{}来提高成绩");

    public String desc;

    PopupTextContext(String desc) {
        this.desc = desc;
    }

}
