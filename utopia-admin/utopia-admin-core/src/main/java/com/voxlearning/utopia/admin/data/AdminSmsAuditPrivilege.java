package com.voxlearning.utopia.admin.data;

import com.voxlearning.utopia.api.constant.SmsTaskStatus;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Yuechen.Wang on 2016/4/18.
 */

public class AdminSmsAuditPrivilege {

    public static final int ORDINARY = 0;
    public static final int SUPERVISOR_LV1 = 1;
    public static final int SUPERVISOR_LV2 = 2;
    public static final int SUPERVISOR_LV3 = 4;

    private static final List<SmsTaskStatus> PRIVILEGE_ORDINARY = Arrays.asList(
            SmsTaskStatus.DRAFT,
            SmsTaskStatus.AUDIT_APPROVED,
            SmsTaskStatus.AUDIT_REJECTED,
            SmsTaskStatus.PENDING_SUPERIOR_LV1,
            SmsTaskStatus.PENDING_SUPERIOR_LV2,
            SmsTaskStatus.PENDING_SUPERIOR_LV3,
            SmsTaskStatus.SEND_SUCCESS,
            SmsTaskStatus.SEND_FAILED
    );

    private static final List<SmsTaskStatus> PRIVILEGE_LV1 = Arrays.asList(
            SmsTaskStatus.PENDING_SUPERIOR_LV1

    );

    private static final List<SmsTaskStatus> PRIVILEGE_LV2 = Arrays.asList(
            SmsTaskStatus.PENDING_SUPERIOR_LV2
    );


    private static final List<SmsTaskStatus> PRIVILEGE_LV3 = Arrays.asList(
            SmsTaskStatus.PENDING_SUPERIOR_LV3
    );


    public static List<SmsTaskStatus> getUserPrivileges(int privilege) {
        switch (privilege) {
            case SUPERVISOR_LV3:
                return PRIVILEGE_LV3;
            case SUPERVISOR_LV2:
                return PRIVILEGE_LV2;
            case SUPERVISOR_LV1:
                return PRIVILEGE_LV1;
            default:
                return PRIVILEGE_ORDINARY;
        }
    }

    public static boolean hasApproveRejectPrivilege(int privilege) {
        return privilege > 0;
    }
}
