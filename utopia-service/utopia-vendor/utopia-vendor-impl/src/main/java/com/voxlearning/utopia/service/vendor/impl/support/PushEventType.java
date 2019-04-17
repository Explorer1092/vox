package com.voxlearning.utopia.service.vendor.impl.support;

/**
 * @author xinxin
 * @since 5/8/18
 */
public enum PushEventType {
    /**
     * 生成家长奖励
     */
    PARENT_REWARD_GENERATE,
    /**
     * 发放家长奖励
     */
    PARENT_REWARD_SEND,

    COMMON_SEND_PUSH_UIDS;

    public static PushEventType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
