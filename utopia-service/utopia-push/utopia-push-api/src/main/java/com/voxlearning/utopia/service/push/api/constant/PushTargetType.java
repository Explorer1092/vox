package com.voxlearning.utopia.service.push.api.constant;

/**
 * @author xinxin
 * @since 10/11/2016
 */
public enum PushTargetType {
    TAG,
    ALIAS;

    public static PushTargetType convert(PushTarget target) {
        if (target == PushTarget.BATCH) {
            return ALIAS;
        } else if (target == PushTarget.TAG) {
            return TAG;
        }

        return null;
    }
}
