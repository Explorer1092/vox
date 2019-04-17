package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiActivityType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Ruib
 * @since 2016/8/15
 */
@ServiceVersion(version = "20181106")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AfentiActivityService extends IPingable {

    Map<String, Object> fetchActivityData(StudentDetail student, AfentiActivityType type, Subject subject);

    MapMessage receiveLoginReward(StudentDetail student, Subject subject);

    MapMessage loadLoginReportData(Long studentId, String orderProductServiceType);

}