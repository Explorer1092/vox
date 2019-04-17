package com.voxlearning.utopia.service.afenti.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.AfentiActivityService;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiActivityType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.*;

/**
 * @author Ruib
 * @since 2016/8/15
 */
public class AfentiActivityServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(AfentiActivityServiceClient.class);

    @ImportService(interfaceClass = AfentiActivityService.class) private AfentiActivityService afentiActivityService;

    public Map<String, Object> fetchActivityData(StudentDetail student, AfentiActivityType type, Subject subject) {
        if (null == student || null == student.getClazz() || null == type) return Collections.emptyMap();

        try {
            return afentiActivityService.fetchActivityData(student, type, subject);
        } catch (Exception ex) {
            logger.error("Failed getting activity {} data for student {}", type, student.getId(), ex);
            return Collections.emptyMap();
        }
    }

    public MapMessage receiveLoginReward(StudentDetail student, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        try {
            return afentiActivityService.receiveLoginReward(student, subject);
        } catch (Exception ex) {
            logger.error("Failed receiveLoginReward {} data for student {}", student.getId(), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage loadLoginReportData(Long studentId, OrderProductServiceType productServiceType) {
        if (null == studentId) return MapMessage.errorMessage();
        List<OrderProductServiceType> serviceTypes = Arrays.asList(OrderProductServiceType.AfentiChinese, OrderProductServiceType.AfentiMath,
                OrderProductServiceType.AfentiExam);
        if (!serviceTypes.contains(productServiceType)) {
            return MapMessage.errorMessage("错误的产品类型");
        }
        try {
            return afentiActivityService.loadLoginReportData(studentId, productServiceType == null ? "" : productServiceType.name());
        } catch (Exception ex) {
            logger.error("Failed getting loginReport {} data for student {}", productServiceType, studentId, ex);
            return MapMessage.errorMessage();
        }

    }
}
