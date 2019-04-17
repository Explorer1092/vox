package com.voxlearning.washington.controller.open.v1.dubbing;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.open.AbstractApiController;

import static com.voxlearning.washington.controller.open.ApiConstants.REQ_STUDENT_ID;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_USER_ERROR_MSG;

/**
 * @author shiwei.liao
 * @since 2017-9-7
 */
public class AbstractDubbingApiController extends AbstractApiController {

    @Override
    public void validateRequest(String... paramKeys) {
        User user = getApiRequestUser();
        if (user == null) {
            logValidateError("error_user");
            throw new IllegalArgumentException(RES_RESULT_USER_ERROR_MSG);
        }
        if (user.fetchUserType() == UserType.PARENT) {
            String[] newParamKeys = ArrayUtils.add(paramKeys, REQ_STUDENT_ID);
            super.validateRequest(newParamKeys);
        } else {
            super.validateRequest(paramKeys);
        }
    }

    public Long getApiStudentId() {
        User user = getApiRequestUser();
        if (user == null) {
            return null;
        }
        if (user.fetchUserType() == UserType.PARENT) {
            return getRequestLong(REQ_STUDENT_ID);
        } else {
            return user.getId();
        }
    }
}
