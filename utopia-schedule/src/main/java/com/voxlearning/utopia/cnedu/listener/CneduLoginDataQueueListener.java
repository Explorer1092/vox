package com.voxlearning.utopia.cnedu.listener;


import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.monitor.FlightController;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.cnedu.listener.helper.CneduReportHelper;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main",
                        queue = "utopia.queue.user.cnedu.login"),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.queue.user.cnedu.login")
        },
        maxPermits = 12
)
@Slf4j
public class CneduLoginDataQueueListener implements MessageListener {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    @Override
    public void onMessage(Message message) {

        FlightController.disableLog();

        String messageText = message.getBodyAsString();
        Map data = JsonUtils.fromJson(messageText);
        if (data == null || data.size() == 0) {
            log.warn("empty or incorrect cnedu login data received, message body is:{}", messageText);
            return;
        }

        if (RuntimeMode.isTest()) {
            log.warn("cnedu login data received, message body is:{}", messageText);
        }

        Long userId = SafeConverter.toLong(data.get("uid"));
        int userType = SafeConverter.toInt(data.get("usertype"));
        OperationSourceType operationSourceType = OperationSourceType.ofWithUnknown(SafeConverter.toString(data.get("source")));
        String appType = SafeConverter.toString(data.get("apptype"));

        // 发送登录详细信息给央馆
        String reportLoginLog = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "login.report.cnedu");
        if (StringUtils.equals(reportLoginLog, "-1")) {
            return;
        }

        if (!StringUtils.equals(reportLoginLog, "*") && !SafeConverter.toString(userId).endsWith(reportLoginLog)) {
            return;
        }

        if (Objects.equals(userType, UserType.STUDENT.getType())) {
            StudentDetail detail = studentLoaderClient.loadStudentDetail(userId);
            if (detail != null && detail.getStudentSchoolRegionCode() != null) {
                ExRegion region = raikouSystem.loadRegion(detail.getStudentSchoolRegionCode());
                CneduReportHelper.sendStudentLoginInfo2Cnedu(detail, region, operationSourceType, appType);
            }
        } else if (Objects.equals(userType, UserType.TEACHER.getType())) {
            TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(userId);
            ExRegion region = raikouSystem.loadRegion(detail.getRegionCode());
            CneduReportHelper.sendTeacherLoginInfo2Cnedu(detail, region, operationSourceType, appType);
        } else if (Objects.equals(userType, UserType.PARENT.getType())) {
//            User parent = userLoaderClient.loadUser(userId);
//            List<StudentParentRef> sprList = parentLoaderClient.loadParentStudentRefs(userId);
//            if (CollectionUtils.isNotEmpty(sprList)) {
//                Long studentId = sprList.get(0).getStudentId();
//                StudentDetail detail = studentLoaderClient.loadStudentDetail(studentId);
//                if (detail != null && detail.getStudentSchoolRegionCode() != null) {
//                    ExRegion region = regionServiceClient.getRegionFromBuffer(detail.getStudentSchoolRegionCode());
//                    CneduReportHelper.sendParentLoginInfo2Cnedu(parent, detail, region, operationSourceType);
//                }
//            }
        }
    }
}