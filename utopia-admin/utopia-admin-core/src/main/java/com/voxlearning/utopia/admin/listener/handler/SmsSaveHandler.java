package com.voxlearning.utopia.admin.listener.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author chunlin.yu
 * @create 2018-05-21 17:55
 **/
@Named
public class SmsSaveHandler  extends SpringContainerSupport {

    @Inject
    private SmsServiceClient smsServiceClient;

    public void handle(long taskId, String targets) {
        Set<String> tokens = Stream.of(targets.replaceAll("\\s", ",").split(","))
                .filter(t -> StringUtils.isNumeric(t) && SafeConverter.toLong(t) != 0L)
                .collect(Collectors.toSet());
        List<com.voxlearning.utopia.service.sms.entity.SmsTaskReceiverRef> resultList = new ArrayList<>();
        for (String token : tokens) {
            com.voxlearning.utopia.service.sms.entity.SmsTaskReceiverRef inst = new com.voxlearning.utopia.service.sms.entity.SmsTaskReceiverRef();
            inst.setSmsTaskId(taskId);
            inst.setSmsReceiver(token);
            inst.setMobileToken(MobileRule.isMobile(token));
            inst.setStatus(0);
            inst.setNotes(null);
            resultList.add(inst);
        }
        smsServiceClient.getSmsService().addSmsTaskReceiverRefs(resultList);
    }
}
