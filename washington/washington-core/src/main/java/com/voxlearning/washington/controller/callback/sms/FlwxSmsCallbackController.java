package com.voxlearning.washington.controller.callback.sms;

/**
 * 飞龙无线短信发送报告接口
 * Created by Alex on 17/4/1.
 */

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/callback/sms")
@Slf4j
public class FlwxSmsCallbackController extends AbstractController {

    @RequestMapping(value = "flwx.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String onCallback(){
        String report = getRequestString("dxstr");
        if (StringUtils.isBlank(report)) {
            logger.warn("empty sms status report callback received.");
            return "ERROR";
        }

        String[] items = report.split(";");
        for (String item : items) {
            updateSmsStatus(item);
        }

        return "OK";
    }

    private void updateSmsStatus(String reportStatus) {
        String[] status = reportStatus.split(",");
        if (status.length != 5) {
            logger.warn("unknown sms status report callback received. {}", reportStatus);
            return;
        }

        String reportType = StringUtils.trim(status[0]);
        String mobile = StringUtils.trim(status[1]);
        String messageStatus = StringUtils.trim(status[2]);
        String messageId = StringUtils.trim(status[3]);
        String sendTime = StringUtils.trim(status[4]);

        // FIXME update sms status later
        if (RuntimeMode.isTest()) {
            logger.info("sms status report received. reportType:{}, mobile:{}, status:{}, message id:{}, send time:{}", reportType, mobile, messageStatus, messageId, sendTime);
        }

        List<String> smsClients = Collections.singletonList(SmsClientType.flwx.name());
        smsServiceClient.getSmsService().updateSmsReceived(messageId, smsClients, sendTime, messageStatus, "");
    }

}

