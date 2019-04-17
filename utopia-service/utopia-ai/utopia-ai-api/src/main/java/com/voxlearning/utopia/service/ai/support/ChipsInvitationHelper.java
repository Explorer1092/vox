package com.voxlearning.utopia.service.ai.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;

public class ChipsInvitationHelper {
    private static String TEST_COUPON_ID = "5b18af0f8edbc82a16ed9cf4";
    private static String ONLINE_COUPON_ID = "5b30982993826b4d737b44e8";

    public static String getCouponId() {
        return RuntimeMode.lt(Mode.STAGING) ? TEST_COUPON_ID : ONLINE_COUPON_ID;
    }
}
