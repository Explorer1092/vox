package com.voxlearning.washington.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.ApiConstants.REQ_CHANNEL;
import static com.voxlearning.washington.controller.open.ApiConstants.REQ_SESSION_KEY;

public class AbstractOrderPaymentController extends AbstractController {

    protected void validateRequired(String paramKey, Object... msgParams) {
        String paramValue = getRequestString(paramKey);
        if (StringUtils.isEmpty(paramValue)) {
            logValidateError("required_" + paramKey);
            throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_REQUIRED_MSG, msgParams));
        }
    }

    protected void logValidateError(String reason) {
        try {
            User curUser = currentUser();
            com.voxlearning.alps.spi.bootstrap.LogCollector.info("app_validate_request_error_logs",
                    MiscUtils.map(
                            "app_key", getRequestString(REQ_APP_KEY),
                            "system", getRequestString(REQ_SYS),
                            "version", getRequestString(REQ_APP_NATIVE_VERSION),
                            "has_session_key", StringUtils.isNotBlank(getRequestString(REQ_SESSION_KEY)),
                            "reason", reason,
                            "user_id", (curUser != null ? curUser.getId() : 0),
                            "uri", getRequest().getRequestURI(),
                            "env", RuntimeMode.getCurrentStage(),
                            "time", com.voxlearning.alps.calendar.DateUtils.dateToString(new Date()),
                            "params", getRequestAllParamsStr(),
                            "channel", getRequestString(REQ_CHANNEL)
                    ));
        } catch (Exception e) {
            // ignore it
        }
    }

    private String getRequestAllParamsStr() {
        Iterator iterator = getRequest().getParameterMap().entrySet().iterator();
        StringBuilder param = new StringBuilder();
        int i = 0;
        while (iterator.hasNext()) {
            i++;
            Map.Entry entry = (Map.Entry) iterator.next();
            if (i == 1)
                param.append("?").append(entry.getKey()).append("=");
            else
                param.append("&").append(entry.getKey()).append("=");
            if (entry.getValue() instanceof String[]) {
                param.append(((String[]) entry.getValue())[0]);
            } else {
                param.append(entry.getValue());
            }
        }
        return param.toString();
    }
}
