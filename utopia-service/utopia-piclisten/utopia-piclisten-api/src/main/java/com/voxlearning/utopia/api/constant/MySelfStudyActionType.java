package com.voxlearning.utopia.api.constant;

/**
 * Created by jiangpeng on 2016/10/20.
 */
public enum MySelfStudyActionType {

    /**
     * 购买,续费,更新到期时间
     */
    PURCHASE,

    /**
     * 更新最后一次学习时间,学习进度
     */
    UPDATE_PROGRESS,

    /**
     * 更新图标
     */
    UPDATE_ICON,

    /**
     * 更新直播课的状态
     * 请使用UPDATE_SHOW
     */
    @Deprecated
    UPDATE_LIVECAST_STATUS,


    /**
     * 更新是否显示
     */
    UPDATE_SHOW,

    /**
     * 全局消息
     */
    UPDATE_GLOBAL_MSG,


    /**
     * 更新用户的消息提醒
     */
    UPDATE_USER_NOTIFY,
    ;


}
